import { Inject, Service } from 'typedi';
import RedisService, { REDIS_KEY } from '../RedisService';
import { ISymbolQuoteMinutes } from '../../models/db/ISymbolQuoteMinutes';
import { ISymbolInfo } from '../../models/db/ISymbolInfo';
import { Logger, Utils } from 'tradex-common';
import { FilterQuery } from 'mongodb';
import { PERIOD_TYPE, SecuritiesTypeEnum } from '../../constants';
import { getKeySymbolQuoteMinute, getMonthKey, getSixMonthKey, getWeekKey } from '../../utils/parse';
import { SymbolPeriodResponse } from 'tradex-models-market';
import { ISymbolDaily } from '../../models/db/ISymbolDaily';
import { toSymbolDailyResponse, toSymbolWeeklyOrMonthlyResponse } from '../../utils/ResponseUtils';
import { ISymbolWeeklyOrMonthly } from '../../models/db/ISymbolWeeklyOrMonthly';
import { SymbolQuoteMinutesRepository } from '../../repositories/SymbolQuoteMinutesRepository';
import { SymbolDailyRepository } from '../../repositories/SymbolDailyRepository';
import CacheService from '../CacheService';

@Service()
export default class CommonService {
  @Inject()
  private readonly cacheService: CacheService;
  @Inject()
  private readonly redisService: RedisService;
  @Inject()
  private readonly symbolQuoteMinutesRepository: SymbolQuoteMinutesRepository;
  @Inject()
  private readonly symbolDailyRepository: SymbolDailyRepository;

  public async actualQueryQuoteMinuteThenGrouped(
    symbol: string,
    fromTime: Date,
    toTime: Date,
    fetchCount: number,
    minuteUnit: number
  ): Promise<ISymbolQuoteMinutes[]> {
    //get a bit more, group it, then take (fetchCount) first grouped records
    const symbolInfo: ISymbolInfo = await this.cacheService.getSymbolInfo(symbol);
    let limit: number = (fetchCount + 1) * minuteUnit;
    const t1 = new Date().getTime();
    let redisSQMList: ISymbolQuoteMinutes[] = await this.redisService.lrange(
      `${REDIS_KEY.SYMBOL_QUOTE_MINUTE}_${symbol}`,
      0,
      -1
    );
    const t2 = new Date().getTime();
    Logger.info(`redis take_____________________________ ${t2 - t1}`);
    if (
      redisSQMList.length === 0 &&
      symbolInfo.type === SecuritiesTypeEnum.FUTURES &&
      symbolInfo.code === symbolInfo.refCode
    ) {
      const latestRecordOfRefCode = await this.symbolDailyRepository
        .findBy({ refCode: symbol }, 1, 0, { date: -1 })
        .toArray();
      redisSQMList = await this.redisService.lrange(
        `${REDIS_KEY.SYMBOL_QUOTE_MINUTE}_${latestRecordOfRefCode[0].code}`,
        0,
        -1
      );
    }
    if (redisSQMList != null) {
      redisSQMList = redisSQMList.filter((item: ISymbolQuoteMinutes) => {
        if (typeof item.date === 'string') {
          item.date = new Date(item.date);
        } else if (typeof item.date === 'number') {
          item.date = new Date(item.date);
        }
        return item.date <= toTime && item.date >= fromTime;
      });
      redisSQMList = redisSQMList.splice(0, limit);
      limit = limit - redisSQMList.length;
    } else {
      redisSQMList = [];
    }
    const t3 = new Date().getTime();
    Logger.info(`check last record take_____________________________ ${t3 - t2}`);

    //group based on minute unit
    const minuteDict = {};
    const processItem = (current: ISymbolQuoteMinutes) => {
      const key = getKeySymbolQuoteMinute(current, minuteUnit);
      const placeHolderRecord: ISymbolQuoteMinutes = minuteDict[key];
      if (placeHolderRecord == null) {
        minuteDict[key] = current;
      } else {
        placeHolderRecord.periodTradingVolume = placeHolderRecord.periodTradingVolume + current.periodTradingVolume;
        placeHolderRecord.high = Utils.round(Math.max(placeHolderRecord.high, current.high));
        placeHolderRecord.low = Utils.round(Math.min(placeHolderRecord.low, current.low));
        if (placeHolderRecord.milliseconds < current.milliseconds) {
          placeHolderRecord.last = Utils.round(current.last);
          placeHolderRecord.milliseconds = current.milliseconds;
          placeHolderRecord.tradingValue = Utils.round(current.tradingValue);
          placeHolderRecord.tradingVolume = Utils.round(current.tradingVolume);
        } else {
          placeHolderRecord.open = Utils.round(current.open);
        }
        minuteDict[key] = placeHolderRecord;
      }
    };
    redisSQMList.forEach(processItem);

    //query mongo
    if (limit > 0) {
      if (symbolInfo == null) {
        Logger.info(`No symbol info ${symbol} in redis, skip query db`);
      } else {
        //normally, mongodb won't have today records, but if manually dump or save by job, it will
        //so skip duplicate record
        let newToTime: Date = new Date(toTime);
        if (redisSQMList.length > 0) {
          //get the earliest time in redis to be newToTime
          const redisLastTime = redisSQMList[redisSQMList.length - 1].date;
          if (redisLastTime < toTime) {
            newToTime = redisSQMList[redisSQMList.length - 1].date;
          }
        }
        const query: FilterQuery<ISymbolQuoteMinutes> = {
          date: {
            $lte: newToTime,
            $gte: fromTime,
          },
        };
        if (symbolInfo.type === SecuritiesTypeEnum.FUTURES && symbolInfo.code === symbolInfo.refCode) {
          //redis save symbolInfo futures in code (like "VN30F1901"), need to query from db by refCode (like "VN30F1M")
          query.refCode = symbolInfo.refCode;
        } else {
          query.code = symbol;
        }
        await this.symbolQuoteMinutesRepository.findBy(query, limit, { date: -1 }).forEach(processItem);
      }
    }
    const t5 = new Date().getTime();
    Logger.info(`convert take_____________________________ ${t5 - t3}`);

    return Object.values(minuteDict).splice(0, fetchCount);
  }

  public async actualQueryQuoteMinuteHistory(
    symbol: string,
    symbolInfo: ISymbolInfo,
    fromTime: Date,
    toTime: Date,
    minuteUnit: number,
    countBack: number
  ): Promise<ISymbolQuoteMinutes[]> {
    const now = new Date();
    if (fromTime.getTime() - now.getTime() > 0) {
        // fromTime over now. should not have response
      if (countBack == 0) {
        return [];
      } else {
        fromTime = now;
        toTime = now;
      }
    }

    const fromTimeDayBase = Math.floor(fromTime.getTime() / 86400000);
    const nowDayBase = Math.floor(now.getTime() / 86400000);
    const newToTime = toTime.getTime() > now.getTime() ? now : toTime;
    const toTimeDayBase = Math.floor(newToTime.getTime() / 86400000);

    const finalResults: ISymbolQuoteMinutes[] = [];

    const minuteDict = {};
    let processItemCount = 0;
    const processItem = (current: ISymbolQuoteMinutes) => {
      processItemCount++;
      const key = getKeySymbolQuoteMinute(current, minuteUnit);
      const placeHolderRecord: ISymbolQuoteMinutes = minuteDict[key];
      if (placeHolderRecord == null) {
        minuteDict[key] = current;
        // important
        finalResults.push(current);
      } else {
        placeHolderRecord.periodTradingVolume = placeHolderRecord.periodTradingVolume + current.periodTradingVolume;
        placeHolderRecord.high = Utils.round(Math.max(placeHolderRecord.high, current.high));
        placeHolderRecord.low = Utils.round(Math.min(placeHolderRecord.low, current.low));
        if (placeHolderRecord.milliseconds < current.milliseconds) {
          placeHolderRecord.last = Utils.round(current.last);
          placeHolderRecord.milliseconds = current.milliseconds;
          placeHolderRecord.tradingValue = Utils.round(current.tradingValue);
          placeHolderRecord.tradingVolume = Utils.round(current.tradingVolume);
        } else {
          placeHolderRecord.open = Utils.round(current.open);
        }
        minuteDict[key] = placeHolderRecord;
      }
    };

    if (toTimeDayBase - nowDayBase === 0) {
      // same day with now. we should query from redis first
      let redisSQMList: ISymbolQuoteMinutes[] = await this.redisService.lrange(
        `${REDIS_KEY.SYMBOL_QUOTE_MINUTE}_${symbol}`,
        0,
        -1
      );
      if (
        redisSQMList.length === 0 &&
        symbolInfo.type === SecuritiesTypeEnum.FUTURES &&
        symbolInfo.code === symbolInfo.refCode
      ) {
        const latestRecordOfRefCode = await this.symbolDailyRepository
          .findBy({ refCode: symbol }, 1, 0, { date: -1 })
          .toArray();
        redisSQMList = await this.redisService.lrange(
          `${REDIS_KEY.SYMBOL_QUOTE_MINUTE}_${latestRecordOfRefCode[0].code}`,
          0,
          -1
        );
      }
      Logger.info('query from redis size: ', redisSQMList.length, fromTime.getTime(), toTime.getTime());
      if (redisSQMList != null) {
        redisSQMList = redisSQMList.filter((item: ISymbolQuoteMinutes) => {
          if (typeof item.date === 'string') {
            item.date = new Date(item.date);
          } else if (typeof item.date === 'number') {
            item.date = new Date(item.date);
          }
          return item.date.getTime() <= toTime.getTime() && (countBack > 0 || item.date.getTime() >= fromTime.getTime());
        });
      } else {
        redisSQMList = [];
      }
      redisSQMList.forEach(processItem);
      Logger.info('query from redis size: ', redisSQMList.length, finalResults.length, countBack);
    }
    //group based on minute

    //query mongo
    let lastMongoItem: ISymbolQuoteMinutes | null = null;
    if (fromTimeDayBase < nowDayBase || finalResults.length < countBack) {
      // has history day or the number of point is not enough. we should query from mongo else {
      //normally, mongodb won't have today records, but if manually dump or save by job, it will
      //so skip duplicate record
      const query: FilterQuery<ISymbolQuoteMinutes> = {
        date: {
          $lte: toTimeDayBase < nowDayBase ? newToTime : new Date(nowDayBase * 86400000),
          $gte: fromTime,
        },
      };
      if (symbolInfo.type === SecuritiesTypeEnum.FUTURES && symbolInfo.code === symbolInfo.refCode) {
        //redis save symbolInfo futures in code (like "VN30F1901"), need to query from db by refCode (like "VN30F1M")
        query.refCode = symbolInfo.refCode;
      } else {
        query.code = symbol;
      }
      await this.symbolQuoteMinutesRepository.findByNoPage(query, { date: -1 }).forEach(it => {
        lastMongoItem = it;
        processItem(it);
      });
      Logger.info(' query from mongo size: ', processItemCount, finalResults.length, countBack);
    }
    if (finalResults.length < countBack) {
      const query: FilterQuery<ISymbolQuoteMinutes> = {
        date: {
          $lt: lastMongoItem?.date ?? now,
        },
      };
      if (symbolInfo.type === SecuritiesTypeEnum.FUTURES && symbolInfo.code === symbolInfo.refCode) {
        //redis save symbolInfo futures in code (like "VN30F1901"), need to query from db by refCode (like "VN30F1M")
        query.refCode = symbolInfo.refCode;
      } else {
        query.code = symbol;
      }
      await this.symbolQuoteMinutesRepository.findTopWithMatch((countBack - finalResults.length + 1) * minuteUnit, query, { date: -1 }).forEach(it => {
        lastMongoItem = it;
        processItem(it);
      });
      Logger.info('query from mongo size for countback: ', processItemCount, finalResults.length, countBack);
    }
    return finalResults;
  }


  public async actualQuerySymbolPeriod(
    symbolInfo: ISymbolInfo,
    periodType: string,
    fetchCount: number,
    baseDate: Date
  ): Promise<SymbolPeriodResponse[]> {
    //get a bit more, sort date: -1, group it, then take (fetchCount) first grouped records
    let dayUnit: number = 1;
    switch (periodType) {
      case PERIOD_TYPE.DAILY:
        dayUnit = 1;
        break;
      case PERIOD_TYPE.WEEKLY:
        dayUnit = 7;
        break;
      case PERIOD_TYPE.MONTHLY:
        dayUnit = 31;
        break;
      case PERIOD_TYPE.SIX_MONTH:
        dayUnit = 31 * 6;
        break;
      default:
        return [];
    }
    const limit = (fetchCount + 1) * dayUnit;

    let query: FilterQuery<ISymbolDaily> = {
      code: symbolInfo.code,
      date: { $lt: baseDate },
    };

    if (symbolInfo.type === SecuritiesTypeEnum.FUTURES && symbolInfo.code === symbolInfo.refCode) {
      query = {
        refCode: symbolInfo.refCode,
        date: { $lt: baseDate },
      };
    }

    let symbolDailyList: ISymbolDaily[] = await this.symbolDailyRepository
      .findBy(query, limit, 0, { date: -1 })
      .toArray();

    if (symbolDailyList.length === 0) {
      return [];
    }

    if (baseDate > Utils.getStartOfDate(new Date()) && symbolDailyList[0].date > Utils.getStartOfDate(new Date())) {
      let currentSymbolDaily: ISymbolDaily = await this.redisService.hget<ISymbolDaily>(
        REDIS_KEY.SYMBOL_DAILY,
        symbolInfo.code
      );
      if (!currentSymbolDaily) {
        currentSymbolDaily = await this.redisService.hget<ISymbolDaily>(REDIS_KEY.SYMBOL_DAILY, symbolInfo.code);
      }
      if (currentSymbolDaily != null) {
        symbolDailyList[0] = currentSymbolDaily;
      }
    }

    if (periodType === PERIOD_TYPE.DAILY) {
      symbolDailyList = symbolDailyList.splice(0, fetchCount);
      return symbolDailyList.map(toSymbolDailyResponse);
    }
    //calculate weekly or monthly
    let finalPeriodList: ISymbolWeeklyOrMonthly[] = [];
    let currentKey: string | null = null;
    let currentList: ISymbolDaily[] = [];
    for (const current of symbolDailyList) {
      let key: string;
      if (periodType === PERIOD_TYPE.WEEKLY) {
        key = getWeekKey(current);
      } else if (periodType === PERIOD_TYPE.MONTHLY) {
        key = getMonthKey(current);
      } else {
        key = getSixMonthKey(current);
      }
      if (currentList.length > 0 && (currentKey == null || currentKey !== key)) {
        const calculatedRecord = this.manualCalculateWeeklyMonthly(currentList);
        finalPeriodList.push(calculatedRecord);
        currentList = [];
        currentKey = key;
      }
      currentList.push(current);
    }
    if (currentList.length > 0) {
      const calculatedRecord = this.manualCalculateWeeklyMonthly(currentList);
      finalPeriodList.push(calculatedRecord);
    }
    finalPeriodList = finalPeriodList.splice(0, fetchCount);
    return finalPeriodList.map(toSymbolWeeklyOrMonthlyResponse);
  }

  public async querySymbolHistory(
    symbolInfo: ISymbolInfo,
    periodType: string,
    fromDate: Date,
    toDate: Date
  ): Promise<SymbolPeriodResponse[]> {
    let query: FilterQuery<ISymbolDaily> = {
      code: symbolInfo.code,
      date: { $lt: toDate, $gte: fromDate },
    };

    if (symbolInfo.type === SecuritiesTypeEnum.FUTURES && symbolInfo.code === symbolInfo.refCode) {
      query = {
        refCode: symbolInfo.refCode,
        date: { $lt: toDate, $gte: fromDate },
      };
    }

    const symbolDailyList: ISymbolDaily[] = await this.symbolDailyRepository
      .findByNoPage(query, { date: -1 })
      .toArray();
    Logger.info('query length: ', symbolDailyList.length);
    if (symbolDailyList.length === 0) {
      return [];
    }
    const baseDateNow = Math.floor(new Date().getTime() / 86400000);
    if (Math.floor(toDate.getTime() / 86400000) >= baseDateNow) {
      let currentSymbolDaily: ISymbolDaily = await this.redisService.hget<ISymbolDaily>(
        REDIS_KEY.SYMBOL_DAILY,
        symbolInfo.code
      );
      if (!currentSymbolDaily) {
        currentSymbolDaily = await this.redisService.hget<ISymbolDaily>(
          REDIS_KEY.SYMBOL_DAILY,
          symbolDailyList[0].code
        );
      }
      if (currentSymbolDaily != null) {
        // symbolDailyList[0].date > Utils.getStartOfDate(new Date())
        if (Math.floor(symbolDailyList[0].date.getTime() / 86400000) >= baseDateNow) {
          symbolDailyList[0] = currentSymbolDaily;
        } else {
          symbolDailyList.unshift(currentSymbolDaily);
        }
      }
    }

    if (periodType === PERIOD_TYPE.DAILY) {
      return symbolDailyList.map(toSymbolDailyResponse);
    }
    //calculate weekly or monthly
    const finalPeriodList: ISymbolWeeklyOrMonthly[] = [];
    let currentKey: string | null = null;
    let currentList: ISymbolDaily[] = [];
    for (const current of symbolDailyList) {
      let key: string;
      if (periodType === PERIOD_TYPE.WEEKLY) {
        key = getWeekKey(current);
      } else if (periodType === PERIOD_TYPE.MONTHLY) {
        key = getMonthKey(current);
      } else {
        key = getSixMonthKey(current);
      }

      if (currentList.length > 0 && currentKey !== key) {
        const calculatedRecord = this.manualCalculateWeeklyMonthly(currentList);
        finalPeriodList.push(calculatedRecord);
        currentList = [];
      }

      currentList.push(current);
      currentKey = key;
    }

    if (currentList.length > 0) {
      const calculatedRecord = this.manualCalculateWeeklyMonthly(currentList);
      finalPeriodList.push(calculatedRecord);
    }

    return finalPeriodList.map(toSymbolWeeklyOrMonthlyResponse);
  }


  public async querySymbolCountBackHistory(
    symbolInfo: ISymbolInfo,
    periodType: string,
    countback: number,
    toDate: Date
  ): Promise<SymbolPeriodResponse[]> {
    let query: FilterQuery<ISymbolDaily> = {
      code: symbolInfo.code,
      date: { $lt: toDate },
    };

    if (symbolInfo.type === SecuritiesTypeEnum.FUTURES && symbolInfo.code === symbolInfo.refCode) {
      query = {
        refCode: symbolInfo.refCode,
        date: { $lt: toDate },
      };
    }

    let limit: number = countback +1;
    if (periodType === PERIOD_TYPE.WEEKLY) {
      limit = limit * 7;
    } else if (periodType === PERIOD_TYPE.MONTHLY) {
      limit = limit * 31;
    } else if (periodType === PERIOD_TYPE.SIX_MONTH) {
      limit = limit * 31 * 6;
    }

    let symbolDailyList: ISymbolDaily[] = await this.symbolDailyRepository
      .findBy(query, limit, 0, { date: -1 })
      .toArray();

    if (symbolDailyList.length === 0) {
      return [];
    }
    const baseDateNow = Math.floor(new Date().getTime() / 86400000);
    if (Math.floor(toDate.getTime() / 86400000) >= baseDateNow) {
      let currentSymbolDaily: ISymbolDaily = await this.redisService.hget<ISymbolDaily>(
        REDIS_KEY.SYMBOL_DAILY,
        symbolInfo.code
      );
      if (!currentSymbolDaily) {
        currentSymbolDaily = await this.redisService.hget<ISymbolDaily>(
          REDIS_KEY.SYMBOL_DAILY,
          symbolDailyList[0].code
        );
      }
      if (currentSymbolDaily != null) {
        if (Math.floor(symbolDailyList[0].date.getTime() / 86400000) >= baseDateNow) {
          symbolDailyList[0] = currentSymbolDaily;
        } else {
          symbolDailyList.unshift(currentSymbolDaily);
        }
      }
    }

    if (periodType === PERIOD_TYPE.DAILY) {
      return symbolDailyList.slice(0, countback).map(toSymbolDailyResponse);
    }
    //calculate weekly or monthly
    const finalPeriodList: ISymbolWeeklyOrMonthly[] = [];
    let currentKey: string | null = null;
    let currentList: ISymbolDaily[] = [];
    for (const current of symbolDailyList) {
      let key: string;
      if (periodType === PERIOD_TYPE.WEEKLY) {
        key = getWeekKey(current);
      } else if (periodType === PERIOD_TYPE.MONTHLY) {
        key = getMonthKey(current);
      } else {
        key = getSixMonthKey(current);
      }

      if (currentList.length > 0 && currentKey !== key) {
        const calculatedRecord = this.manualCalculateWeeklyMonthly(currentList);
        finalPeriodList.push(calculatedRecord);
        currentList = [];
      }

      currentList.push(current);
      currentKey = key;
    }

    if (currentList.length > 0) {
      const calculatedRecord = this.manualCalculateWeeklyMonthly(currentList);
      finalPeriodList.push(calculatedRecord);
    }

    return finalPeriodList.slice(0, countback).map(toSymbolWeeklyOrMonthlyResponse);
  }

  public manualCalculateWeeklyMonthly(symbolDailyList: ISymbolDaily[]): ISymbolWeeklyOrMonthly {
    // symbolDailyList are sorting from latest to the past, like [Friday, Monday, Wed, Tuesday, Monday]
    const firstDay: ISymbolDaily = symbolDailyList[symbolDailyList.length - 1];
    const lastDay: ISymbolDaily = symbolDailyList[0];

    let high: number = symbolDailyList[0].high;
    let low: number = symbolDailyList[0].low;
    let totalTradingValue: number = 0;
    let totalTradingVolume: number = 0;
    let dayCount: number = 0;

    for (const symbolDaily of symbolDailyList) {
      dayCount++;
      if (high < symbolDaily.high) {
        high = symbolDaily.high;
      }
      if (low > symbolDaily.low) {
        low = symbolDaily.low;
      }
      totalTradingValue += symbolDaily.tradingValue;
      totalTradingVolume += symbolDaily.tradingVolume;
    }

    const lastOfPreviousPeriod: number = firstDay.last - firstDay.change;
    const change = lastDay.last - lastOfPreviousPeriod;
    const rate: number = (change / lastOfPreviousPeriod) * 100;

    return {
      _id: firstDay._id,
      code: firstDay.code,
      open: firstDay.open,
      high: high,
      low: low,
      last: lastDay.last,
      change: change,
      rate: rate,
      tradingVolume: totalTradingVolume,
      tradingValue: totalTradingValue,
      date: firstDay.date,
      refCode: firstDay.refCode,
      dayCount: dayCount,
    };
  }
}
