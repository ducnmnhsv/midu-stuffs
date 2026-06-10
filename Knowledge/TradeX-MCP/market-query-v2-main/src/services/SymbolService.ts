import { Inject, Service } from 'typedi';
import * as Ajv from 'ajv';
import {
  SymbolLatestRequest,
  SymbolPeriodRequest,
  SymbolPeriodResponse,
  SymbolQuoteRequest,
  // SymbolQuoteResponse,
  SymbolQuoteMinuteRequest,
  SymbolQuoteMinuteResponse,
  SymbolQuoteTickRequest,
  SymbolQuoteTickResponse,
  ForeignerDailyRequest,
  ForeignerDailyResponse,
  StockRankingTradeRequest,
  StockRankingTradeResponse,
  StockRankingUpDownRequest,
  StockRankingUpDownResponse,
  SymbolStaticInfoRequest,
  IndexStockListRequest,
  IndexStockListResponse,
  StockRankingTopRequest,
  StockRankingTopResponse,
  TopForeignerTradingRequest,
  TopForeignerTradingResponse,
  SymbolDailyReturnsRequest,
  SymbolDailyReturnsResponse,
  SymbolDailyReturnsInitRequest,
  SymbolTickSizeMatchRequest,
  SymbolTickSizeMatchResponse,
  ForeignerSummaryRequest,
  ForeignerSummaryResponse,
} from 'tradex-models-market';
import {
  symbolLatestRequestValidator,
  symbolPeriodRequestValidator,
  symbolQuoteRequestValidator,
  symbolQuoteMinuteRequestValidator,
  symbolQuoteTickRequestValidator,
  foreignerDailyRequestValidator,
  stockRankingTradeRequestValidator,
  stockRankingUpDownRequestValidator,
  symbolStaticInfoRequestValidator,
  indexStockListRequestValidator,
  stockRankingTopRequestValidator,
  foreignerSummaryRequestValidator,
  topForeignerTradingRequestValidator,
  symbolDailyReturnsRequestValidator,
  symbolTickSizeMatchRequestValidator,
  symbolDailyReturnsInitRequestValidator,
} from 'tradex-models-market-validator';
import { Errors, Logger, Utils } from 'tradex-common';
import {
  DEFAULT_PAGE_SIZE,
  INVALID_PARAMETER,
  DEFAULT_CEILING_SEQUENCE,
  MARKET_TIMEZONE,
  MarketTypeEnum,
  UpDownTypeEnum,
  StockRankingTradeSortTypeEnum,
  DEFAULT_OFFSET,
  SecuritiesTypeEnum,
  TopSortTypeEnum,
  POWER_STOCK_SORT_THRESHOLD,
  DEFAULT_TOP_FOREIGNER_TRADING,
  DEFAULT_QUERY_DAILY_RETURN_DAYS,
  MONGO_MAX_SAFE_ARRAY_SIZE,
  DEFAULT_FLOOR_DATE,
  DEFAULT_DAILY_FROM_DATE,
  DEFAULT_DAILY_FETCH_COUNT,
  FOREIGNER_SUMMARY_SORT_TYPE,
  PRICE_BOARD_CATEGORY,
  ForeignerRankingTypeEnum,
} from '../constants';
import RedisService, { REDIS_KEY } from './RedisService';
import { ISymbolInfo } from '../models/db/ISymbolInfo';
import {
  toSymbolLatestResponse,
  toSymbolQuoteResponse,
  toSymbolQuoteMinutesResponse,
  toSymbolQuoteTickResponse,
  toStockRankingTradeResponse,
  toForeignerDailyResponse,
  toSymbolStaticInfoResponse,
  toIndexStockListResponse,
  toStockRankingTopResponse,
  toStockRankingUpdownResponse,
  toTopForeignerTradingResponse,
  toSymbolTickSizeMatchResponse,
  toForeignerSummaryResponse,
  toMinuteChartResponse,
  toPriceBoardResponse,
  toPtDealTotalResponse,
  toQuoteResponse,
  toSymbolStatisticsResponse,
  toStockRankingPeriodResponse,
  toSymbolStockRightResponse,
  toForeignerRankingResponse,
} from '../utils/ResponseUtils';
import { BulkWriteResult, FilterQuery, UnorderedBulkOperation } from 'mongodb';
import { ISymbolDaily } from '../models/db/ISymbolDaily';
import { SymbolDailyRepository } from '../repositories/SymbolDailyRepository';
import { ISymbolQuote } from '../models/db/ISymbolQuote';
import { SymbolQuoteRepository } from '../repositories/SymbolQuoteRepository';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';
import { IForeignerDaily } from '../models/db/IForeignerDaily';
import { ForeignerDailyRepository } from '../repositories/ForeignerDailyRepository';
import { IndexStockListRepository } from '../repositories/IndexStockListRepository';
import { SymbolInfoRepository } from '../repositories/SymbolInfoRepository';
import { IGroupedSymbolDailyResponse } from '../models/response/IGroupedSymbolDailyResponse';
import { getKeySymbolQuoteTick, validateRequest } from '../utils/parse';
import CommonService from './common/CommonService';
import { SymbolRepository } from '../repositories/SymbolRepository';
import { ISymbol } from '../models/db/ISymbol';
import { ISymbolInfoExtend } from '../models/db/ISymbolInfoExtend';
import { SymbolInfoExtendRepository } from '../repositories/SymbolInfoExtendRepository';
import { IAvgTradingVolume10 } from '../models/request/IAvgTradingVolume10';
import { SymbolQuoteMinutesRepository } from '../repositories/SymbolQuoteMinutesRepository';
import IMinuteChartResponse from '../models/response/IMinuteChartResponse';
import { IMinuteChartRequest } from '../models/request/IMinuteChartRequest';
import { IPriceBoardRequest } from '../models/request/IPriceBoardRequest';
import IPriceBoardResponse from '../models/response/IPriceBoardResponse';
import { IIndexStockList } from '../models/db/IIndexStockList';
import { IPtDealTotalRequest } from '../models/request/PtDealTotalRequest';
import IPtDealTotalResponse from '../models/response/PtDealTotalResponse';
import { IDealNoticeData as DealNoticeData } from '../models/db/IDealNoticeData';
import CacheService from './CacheService';
import { ICalculateMinuteRequest } from '../models/request/ICalculateMinuteRequest';
import { IQuoteRequest } from '../models/request/IQuoteRequest';
import IQuoteResponse from '../models/response/IQuoteResponse';
import { ISymbolLatestResponse } from '../models/response/ISymbolLatestResponse';
import { ISymbolStaticInfoResponse } from '../models/response/ISymbolStaticInfoResponse';
import { ISymbolStatisticsRequest } from '../models/request/ISymbolStatisticsRequest';
import { ISymbolStatistics, ISymbolStatisticsResponse } from '../models/db/ISymbolStatistics';
import { IQueryStockRankingPeriod } from '../models/request/IQueryStockRankingPeriod';
import {
  IRedisStockRankingPeriodItemResponse,
  IRedisStockRankingPeriodResponse,
  IStockRankingPeriodResponse,
} from '../models/response/IStockRankingPeriodResponse';
import { IIndexListRequest } from '../models/request/IIndexListRequest';
import { ISymbolStockRightRequest } from '../models/request/ISymbolStockRightRequest';
import { ISymbolStockRightResponse } from '../models/response/ISymbolStockRightResponse';
import { ISymbolStockRight } from '../models/db/ISymbolStockRighte';
import { IForeignerRankingRequest } from '../models/request/IForeignerRankingRequest';
import { IForeignerRankingResponse } from '../models/response/IForeignerRankingResponse';
import { IGetNotificationRequest } from '../models/request/IGetNotificationRequest';
import { IGetNotification, IGetNotificationResponse } from '../models/response/IGetNotificationResponse';
import { IDailyAccumulativeVNIndexRequest } from '../models/request/IDailyAccumulativeVNIndexRequest';
import { IDailyAccumulativeVNIndexResponse } from '../models/response/IDailyAccumulativeVNIndexResponse';
import { isHoliday } from '../utils/DefaultUtils';
import ListQuoteMeta from '../models/redis/ListQuoteMeta';
import QuotePartition from '../models/redis/QuotePartition';
import { SymbolQuoteResponse } from '../models/response/SymbolQuoteResponse';

const { validate } = Utils;
const { GeneralError, InvalidParameterError } = Errors;

@Service()
export default class SymbolService {
  @Inject()
  private readonly redisService: RedisService;
  @Inject()
  private readonly cacheService: CacheService;
  @Inject()
  private readonly symbolDailyRepository: SymbolDailyRepository;
  @Inject()
  private readonly symbolQuoteMinuteRepo: SymbolQuoteMinutesRepository;
  @Inject()
  private readonly symbolQuoteRepository: SymbolQuoteRepository;
  @Inject()
  private readonly foreignerDailyRepository: ForeignerDailyRepository;
  @Inject()
  private readonly indexStockListRepository: IndexStockListRepository;
  @Inject()
  private readonly symbolInfoRepository: SymbolInfoRepository;
  @Inject()
  private readonly symbolRepository: SymbolRepository;
  @Inject()
  private readonly symbolInfoExtendRepo: SymbolInfoExtendRepository;
  @Inject()
  private readonly commonService: CommonService;

  public async queryIndexList(request: IIndexListRequest): Promise<string[]> {
    if (request.market == null || request.market.trim() === '') {
      request.market = MarketTypeEnum.ALL;
    }
    const t1 = new Date().getTime();
    const allSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    const response: string[] = [];
    allSymbolInfo.forEach((symbolInfo: ISymbolInfo) => {
      if (symbolInfo.type === SecuritiesTypeEnum.INDEX) {
        if (request.market === MarketTypeEnum.ALL) {
          response.push(symbolInfo.code);
        } else {
          if (symbolInfo.marketType === request.market) {
            response.push(symbolInfo.code);
          }
        }
      }
    });
    const t2 = new Date().getTime();
    Logger.info(`querySymbolLatest, redis take: ${t2 - t1}`);
    return response;
  }

  public async querySymbolRight(request: ISymbolStockRightRequest): Promise<ISymbolStockRightResponse> {
    if (request.symbol == null || request.symbol === '') {
      return null;
    }
    const key = `${REDIS_KEY.SYMBOL_STOCK_RIGHT}_${request.symbol}`;
    const symbolRight: ISymbolStockRight = await this.redisService.get(key);
    if (symbolRight != null) {
      return toSymbolStockRightResponse(symbolRight);
    } else {
      return null;
    }
  }

  public async queryAccountNotification(request: IGetNotificationRequest): Promise<IGetNotificationResponse[]> {
    const type: string = request.type != null ? request.type : 'ALL';
    const keyword: string = request.keyword;
    const fromDate: string = request.fromDate != null ? request.fromDate : DEFAULT_DAILY_FROM_DATE;
    const toDate: string = request.toDate != null ? request.toDate : new Date().toISOString().slice(0, 10);
    const pageSize: number = request.pageSize != null ? request.pageSize : 20;
    const pageNumber: number = request.pageNumber != null ? request.pageNumber : 0;
    //Get all notification from redis
    const allNotification: IGetNotification[] = await this.redisService.hgetall<IGetNotification>(REDIS_KEY.NOTIFICATION + type);
    //Filter notification by keyword
    let filteredNotification: IGetNotification[] = allNotification;
    if (keyword != null) {
      filteredNotification = allNotification.filter((notification: IGetNotification) => {
        return notification.title.includes(keyword) || notification.content.includes(keyword);
      });
    }
    //Filter notification by date
    const fromDateObj: Date = Utils.convertStringToDate(fromDate);
    const toDateObj: Date = Utils.convertStringToDate(toDate);
    filteredNotification = filteredNotification.filter((notification: IGetNotification) => {
      const notificationDate: Date = Utils.convertStringToDate(notification.date);
      return notificationDate >= fromDateObj && notificationDate <= toDateObj;
    });
    //Sort notification by date
    filteredNotification = filteredNotification.sort((a: IGetNotification, b: IGetNotification) => {
      return Utils.compareDateOnly(Utils.convertStringToDate(a.date), Utils.convertStringToDate(b.date));
    });
    //Paginate notification
    const start: number = pageNumber * pageSize;
    const end: number = start + pageSize;
    const paginatedNotification: IGetNotification[] = filteredNotification.slice(start, end);
    //Convert to response
    return paginatedNotification.map((notification: IGetNotification) => {
      return {
        sendDate: notification.date,
        sendTime: notification.time,
        author: notification.writer,
        title: notification.title,
        content: notification.content,
      };
    });
  }

  public async queryIndexStockList(request: IndexStockListRequest): Promise<IndexStockListResponse> {
    const validator: Ajv.ValidateFunction = indexStockListRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const query: FilterQuery<ISymbolQuote> = {
      _id: request.indexCode,
    };
    const indexStockList: IIndexStockList = await this.indexStockListRepository.findOneBy(query);
    return toIndexStockListResponse(indexStockList);
  }

  public async querySymbolQuote(request: SymbolQuoteRequest): Promise<SymbolQuoteResponse[]> {
    const validator: Ajv.ValidateFunction = symbolQuoteRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    // query redis. Redis are sorted from latest to oldest
    let meta: ListQuoteMeta = await this.redisService.getDe(`${REDIS_KEY.SYMBOL_QUOTE_META}_${request.symbol}`, new ListQuoteMeta());
    if (meta == null || meta.partitions.length === 0) {
      const p = new QuotePartition();
      p.partition = -1;
      p.fromVolume = 0;
      meta = new ListQuoteMeta();
      meta.partitions.push(p);
    }

    const defaultPartition = meta.partitions[0];
    meta.partitions.push(defaultPartition);
    const lastTradingVolume =
      request.lastTradingVolume != null && request.lastTradingVolume > 0 ? request.lastTradingVolume : Number.MAX_SAFE_INTEGER;
    const symbolQuotes: ISymbolQuote[] = [];
    for (let i = meta.partitions.length - 1; i > 0; i--) {
      // not include default one more time
      const partition = meta.partitions[i];
      if (partition.fromVolume < lastTradingVolume) {
        const redisKey: string =
          partition.partition < 0
            ? `${REDIS_KEY.SYMBOL_QUOTE}_${request.symbol}`
            : `${REDIS_KEY.SYMBOL_QUOTE}_${request.symbol}_${partition.partition}`;
        const listSymbolQuote: ISymbolQuote[] = await this.redisService.lrange(redisKey, 0, -1);
        for (let i = 0; i < listSymbolQuote.length; i++) {
          const it = listSymbolQuote[i];
          if (it.tradingVolume < lastTradingVolume) {
            symbolQuotes.push(it);
            if (symbolQuotes.length >= fetchCount) {
              break;
            }
          }
        }
        if (symbolQuotes.length >= fetchCount) {
          break;
        }
      } else {
        continue;
      }
    }

    return symbolQuotes.map(toSymbolQuoteResponse);
  }

  public async queryQuoteData(request: IQuoteRequest): Promise<IQuoteResponse> {
    if (request.symbol == null) {
      return null;
    }
    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    let startIndex = request.lastIndex == null ? 0 : request.lastIndex + 1;
    let endIndex = fetchCount + startIndex;
    let meta: ListQuoteMeta = await this.redisService.getDe(`${REDIS_KEY.SYMBOL_QUOTE_META}_${request.symbol}`, new ListQuoteMeta());
    if (meta == null || meta.partitions.length === 0) {
      const p = new QuotePartition();
      p.partition = -1;
      p.fromVolume = 0;
      p.totalItems = 0;
      meta = new ListQuoteMeta();
      meta.partitions.push(p);
    }
    const defaultPartition = meta.partitions[0];
    const defaultSize = await this.redisService.llen(`${REDIS_KEY.SYMBOL_QUOTE}_${request.symbol}`);
    defaultPartition.totalItems = defaultSize;
    meta.partitions.push(defaultPartition);

    let totalIndex = 0;
    let symbolQuotes: ISymbolQuote[] = [];
    for (let i = meta.partitions.length - 1; i > 0; i--) {
      // not include default one more time
      const partition = meta.partitions[i];
      const redisKey: string =
        partition.partition < 0
          ? `${REDIS_KEY.SYMBOL_QUOTE}_${request.symbol}`
          : `${REDIS_KEY.SYMBOL_QUOTE}_${request.symbol}_${partition.partition}`;
      const previousTotalIndex = totalIndex;
      totalIndex += partition.totalItems;

      if (symbolQuotes.length >= fetchCount || totalIndex < startIndex) {
        continue;
      } else {
        const pStartIndex = startIndex - previousTotalIndex;
        let pEndIndex = -1; // take all the rest
        if (totalIndex >= startIndex + fetchCount) {
          pEndIndex = startIndex + fetchCount - previousTotalIndex;
        }
        const listSymbolQuote: ISymbolQuote[] = await this.redisService.lrange(redisKey, pStartIndex, pEndIndex);
        if (symbolQuotes.length === 0) {
          symbolQuotes = listSymbolQuote;
        } else {
          symbolQuotes = [...symbolQuotes, ...listSymbolQuote];
        }
        startIndex = totalIndex;
      }
    }

    return toQuoteResponse(symbolQuotes, totalIndex, endIndex < totalIndex ? endIndex : totalIndex);
  }

  public async querySymbolStatistics(request: ISymbolStatisticsRequest): Promise<ISymbolStatisticsResponse> {
    if (request.symbol == null || request.pageSize == null || request.pageNumber == null) {
      return null;
    }
    const pageSize: number = request.pageSize;
    const pageNumber: number = request.pageNumber;
    const symbolStatistics: ISymbolStatistics = await this.redisService.hget(REDIS_KEY.SYMBOL_STATISTICS, request.symbol);
    return toSymbolStatisticsResponse(symbolStatistics, pageSize, pageNumber, request.sortBy);
  }

  public async querySymbolQuoteMinutes(request: SymbolQuoteMinuteRequest): Promise<SymbolQuoteMinuteResponse[]> {
    //query from redis, then from mongo, then group based on minute unit
    validateRequest(request, symbolQuoteMinuteRequestValidator);

    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const minuteUnit: number = request.minuteUnit;
    const today = new Date();
    const fromTime =
      request.fromTime != null ? Utils.convertStringToDate(request.fromTime, Utils.DATETIME_DISPLAY_FORMAT) : Utils.getStartOfDate(today);
    const toTime = request.toTime != null ? Utils.convertStringToDate(request.toTime, Utils.DATETIME_DISPLAY_FORMAT) : Utils.getEndOfDate(today);
    const responseList: ISymbolQuoteMinutes[] = await this.commonService.actualQueryQuoteMinuteThenGrouped(
      request.symbol,
      fromTime,
      toTime,
      fetchCount,
      minuteUnit,
    );
    return responseList.map((item: ISymbolQuoteMinutes) => toSymbolQuoteMinutesResponse(item, minuteUnit));
  }

  public async queryMinuteChart(request: IMinuteChartRequest): Promise<IMinuteChartResponse> {
    if (request.symbol == null) {
      return null;
    }
    if (this.cacheService.cacheMinuteChart.has(request.symbol)) {
      Logger.info('minuteChart is already in cache _______________________________');
      return this.cacheService.cacheMinuteChart.get(request.symbol);
    }
    const promise = this.queryMinuteChartBySymbol(request.symbol);
    this.cacheService.cacheMinuteChart.set(request.symbol, promise);
    promise
      .then(() => {
        this.cacheService.cacheMinuteChart.delete(request.symbol);
      })
      .catch((error: any) => {
        Logger.error(`error while query minuteChart: ${error}`);
        this.cacheService.cacheMinuteChart.delete(request.symbol);
      });

    return promise;
  }

  public async queryMinuteChartBySymbol(symbol: string): Promise<IMinuteChartResponse> {
    const symbolQuoteMinutes: ISymbolQuoteMinutes[] = await this.redisService.lrange(`${REDIS_KEY.SYMBOL_QUOTE_MINUTE}_${symbol}`, 0, -1);
    return toMinuteChartResponse(symbolQuoteMinutes);
  }

  public async querySymbolQuoteTick(request: SymbolQuoteTickRequest): Promise<SymbolQuoteTickResponse[]> {
    validateRequest(request, symbolQuoteTickRequestValidator);
    //query from redis, then mongo, group, then calculate periodTradingVolume
    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const toSequence: number = request.toSequence != null ? request.toSequence : DEFAULT_CEILING_SEQUENCE;
    const fromSequence: number = request.fromSequence != null ? request.fromSequence : 0;
    const tickUnit: number = request.tickUnit;

    //get a bit more, group it, then take (fetchCount) first grouped records
    //get (fetchCount+2) because need to get 1 more record than normal (after grouped) to calculate periodTradingVolume
    let limit: number = (fetchCount + 2) * tickUnit;
    let redisSymbolQuoteTickList: ISymbolQuote[] = await this.redisService.lrange(`${REDIS_KEY.SYMBOL_QUOTE}_${request.symbol}`, 0, -1);
    redisSymbolQuoteTickList = redisSymbolQuoteTickList.filter((item: ISymbolQuote) => item.sequence < toSequence && item.sequence > fromSequence);
    //sort from higher sequence to lower sequence
    redisSymbolQuoteTickList = redisSymbolQuoteTickList.sort((a: ISymbolQuote, b: ISymbolQuote) => {
      return -(a.sequence - b.sequence);
    });
    redisSymbolQuoteTickList = redisSymbolQuoteTickList.splice(0, limit);
    limit = limit - redisSymbolQuoteTickList.length;

    //query mongo
    let dbSMTList: ISymbolQuoteMinutes[] = [];
    if (limit > 0) {
      //normally, mongodb won't have today records, but if manually dump or save by job, it will
      //so skip duplicate record
      let newToSequence: number = toSequence;
      if (redisSymbolQuoteTickList.length > 0) {
        //get the earliest sequence in redis to be newToSequence
        const redisEarliestSequence = redisSymbolQuoteTickList[redisSymbolQuoteTickList.length - 1].sequence;
        if (redisEarliestSequence < toSequence) {
          newToSequence = redisSymbolQuoteTickList[redisSymbolQuoteTickList.length - 1].sequence;
        }
      }

      const query: FilterQuery<ISymbolQuote> = {
        code: request.symbol,
        sequence: {
          $lt: newToSequence,
          $gt: fromSequence,
        },
        date: {
          $gte: Utils.getStartOfDate(new Date()),
          $lte: Utils.getEndOfDate(new Date()),
        },
      };
      dbSMTList = await this.symbolQuoteRepository.findBy(query, limit, { sequence: -1 }).toArray();
    }
    const totalSymbolQuoteTickList: ISymbolQuote[] = redisSymbolQuoteTickList.concat(dbSMTList);

    const quoteTickDict = {};
    for (const current of totalSymbolQuoteTickList) {
      const key = getKeySymbolQuoteTick(current, tickUnit);
      if (quoteTickDict[key] == null) {
        quoteTickDict[key] = current;
      } else {
        const placeHolderRecord: ISymbolQuote = quoteTickDict[key];

        placeHolderRecord.high = Utils.round(Math.max(placeHolderRecord.high, current.high));
        placeHolderRecord.low = Utils.round(Math.min(placeHolderRecord.low, current.low));
        if (placeHolderRecord.sequence < current.sequence) {
          //current record have higher sequence, come later
          placeHolderRecord.tradingVolume = current.tradingVolume;
          placeHolderRecord.last = Utils.round(current.last);
          placeHolderRecord.date = current.date;
        } else {
          placeHolderRecord.open = Utils.round(current.open);
        }
        quoteTickDict[key] = placeHolderRecord;
      }
    }
    //get 1 more record to calculate periodTradingVolume
    const groupedList: ISymbolQuote[] = Object.values(quoteTickDict).splice(0, fetchCount + 1);
    let higherSequenceRecord: ISymbolQuote = null;

    const responseList: SymbolQuoteTickResponse[] = [];
    for (const current of groupedList) {
      let periodTradingVolume: number = 0;
      if (higherSequenceRecord != null) {
        //in first loop, higherSequenceRecord = current, skip this block
        periodTradingVolume = higherSequenceRecord.tradingVolume - current.tradingVolume;
        //push the previous record, which have periodTradingVolume calculated
        responseList.push(toSymbolQuoteTickResponse(higherSequenceRecord, periodTradingVolume)); //responseList will only push (fetchCount) time, not (fetchCount+1)
      }

      higherSequenceRecord = current;
    }
    return responseList;
  }

  public async querySymbolLatest(request: SymbolLatestRequest, isOddLot: boolean): Promise<ISymbolLatestResponse[]> {
    const validator: Ajv.ValidateFunction = symbolLatestRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    if (request.symbolList == null || request.symbolList.length === 0) {
      return [];
    }
    const t1 = new Date().getTime();
    const symbolInfos: ISymbolInfo[] = await this.redisService.hmget<ISymbolInfo>(
      isOddLot ? REDIS_KEY.SYMBOL_INFO_ODD_LOT : REDIS_KEY.SYMBOL_INFO,
      request.symbolList,
    );
    const response: ISymbolLatestResponse[] = symbolInfos.filter((it) => it != null).map((it) => toSymbolLatestResponse(it, isOddLot));
    const t2 = new Date().getTime();
    Logger.info(`querySymbolLatest, redis take: ${t2 - t1}`);
    return response;
  }

  public async querySymbolLatestNormal(request: SymbolLatestRequest): Promise<ISymbolLatestResponse[]> {
    return this.querySymbolLatest(request, false);
  }

  public async querySymbolLatestOddLot(request: SymbolLatestRequest): Promise<ISymbolLatestResponse[]> {
    return this.querySymbolLatest(request, true);
  }

  public async queryPriceBoard(request: IPriceBoardRequest): Promise<IPriceBoardResponse> {
    const response: IPriceBoardResponse = {};
    if (request.symbolList == null || request.symbolList.length === 0) {
      return response;
    }
    const category = request.category != null ? request.category : PRICE_BOARD_CATEGORY.FAVORITE_LIST;
    const symbolInfoList: ISymbolInfo[] = (await this.redisService.hmget<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO, request.symbolList)).filter(
      (it) => it != null,
    );
    return toPriceBoardResponse(symbolInfoList, category);
  }

  public async calculateAvgTradingVolume(request?: IAvgTradingVolume10): Promise<any> {
    let stockList: string[] = [];
    if (request == null || request.symbolList == null || request.symbolList.length === 0) {
      stockList = (await this.symbolRepository.findAll().toArray()).map((obj: ISymbol) => obj._id);
    } else {
      stockList = request.symbolList;
    }
    const symbolInfoExtends: ISymbolInfoExtend[] = [];
    const date = new Date();
    for (const symbol of stockList) {
      const symbolDailyList: ISymbolDaily[] = await this.symbolDailyRepository.findBy({ code: symbol }, 10, 0, { _id: -1 }).toArray();
      let totalTradingVolume = 0;
      symbolDailyList.forEach((obj: ISymbolDaily) => (totalTradingVolume += obj.tradingVolume));
      const info: ISymbolInfoExtend = {
        _id: symbol,
        avgTradingVol10: totalTradingVolume / symbolDailyList.length,
        date: date,
      };
      symbolInfoExtends.push(info);
      await this.redisService.hset(REDIS_KEY.SYMBOL_INFO_EXTEND, symbol, info);
    }
    await this.symbolInfoExtendRepo.upsertByBulk(symbolInfoExtends);
  }

  public async querySymbolStaticInfo(request: SymbolStaticInfoRequest): Promise<ISymbolStaticInfoResponse[]> {
    const validator: Ajv.ValidateFunction = symbolStaticInfoRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    if (request.symbolList == null || request.symbolList.length === 0) {
      const allSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
      const allSymbolExtend: ISymbolInfoExtend[] = await this.redisService.hgetall<ISymbolInfoExtend>(REDIS_KEY.SYMBOL_INFO_EXTEND);
      const symbolExtends = {};
      allSymbolExtend.forEach((symbolExtend: ISymbolInfoExtend) => {
        symbolExtends[symbolExtend._id] = symbolExtend;
      });
      return allSymbolInfo.map((symbolInfo: ISymbolInfo) => {
        return toSymbolStaticInfoResponse(symbolInfo, symbolExtends[symbolInfo.code]);
      });
    }

    const symbolInfos: ISymbolInfo[] = await this.redisService.hmget<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO, request.symbolList);
    return symbolInfos.filter((it) => it != null).map((it) => toSymbolStaticInfoResponse(it, null));
  }

  public async querySymbolPeriod(request: SymbolPeriodRequest): Promise<SymbolPeriodResponse[]> {
    validateRequest(request, symbolPeriodRequestValidator);

    //query from mongo, update today from redis, group by period
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const baseDate: Date = request.baseDate != null ? Utils.convertStringToDate(request.baseDate) : tomorrow;
    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;

    const symbolInfo: ISymbolInfo = await this.redisService.getSymbolInfo(request.symbol);

    return this.commonService.actualQuerySymbolPeriod(symbolInfo, request.periodType, fetchCount, baseDate);
  }

  public async querySymbolForeignerDaily(request: ForeignerDailyRequest): Promise<ForeignerDailyResponse> {
    const validator: Ajv.ValidateFunction = foreignerDailyRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const today = new Date();
    today.setHours(today.getHours() + MARKET_TIMEZONE);

    const tomorrow = new Date();
    tomorrow.setHours(tomorrow.getHours() + MARKET_TIMEZONE);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const defaultFromDate = Utils.convertStringToDate(DEFAULT_DAILY_FROM_DATE);

    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    let baseDate: Date = request.baseDate != null ? Utils.convertStringToDate(request.baseDate) : tomorrow;
    let toDate: Date = request.toDate != null ? Utils.convertStringToDate(request.toDate) : today;
    let fromDate: Date = request.toDate != null ? Utils.convertStringToDate(request.fromDate) : defaultFromDate;

    baseDate = Utils.getStartOfDate(baseDate);
    toDate = Utils.getEndOfDate(toDate);
    fromDate = Utils.getStartOfDate(fromDate);

    const query: FilterQuery<IForeignerDaily> = {
      code: request.symbol,
      date: { $gte: fromDate, $lt: baseDate, $lte: toDate },
    };
    const symbolForeignerDailyList: IForeignerDaily[] = await this.foreignerDailyRepository.findBy(query, fetchCount, { date: -1 }).toArray();

    if (symbolForeignerDailyList.length > 0 && Utils.compareDateOnly(toDate, today) >= 0 && Utils.compareDateOnly(baseDate, today) > 0) {
      const symbolForeignerRedis = await this.redisService.hget(REDIS_KEY.FOREIGNER_DAILY, request.symbol);
      const symbolForeignerDaily: IForeignerDaily = this.convertSymbolForeigner(symbolForeignerRedis);
      if (symbolForeignerDaily != null && Utils.compareDateOnly(symbolForeignerDaily.date, symbolForeignerDailyList[0].date) === 0) {
        symbolForeignerDailyList[0] = symbolForeignerDaily;
      }
    }

    return symbolForeignerDailyList.map(toForeignerDailyResponse);
  }

  private convertSymbolForeigner(symbolForeigner): IForeignerDaily {
    if (symbolForeigner != null) {
      if (typeof symbolForeigner.date === 'string') {
        symbolForeigner.date = new Date(symbolForeigner.date);
      } else if (typeof symbolForeigner.date === 'number') {
        symbolForeigner.date = new Date(symbolForeigner.date);
      }
    }
    return symbolForeigner;
  }

  public async queryStockRankingUpDown(request: StockRankingUpDownRequest): Promise<StockRankingUpDownResponse> {
    const validator: Ajv.ValidateFunction = stockRankingUpDownRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const offset: number = request.offset != null ? request.offset : DEFAULT_OFFSET;
    const marketType: string = request.marketType != null ? request.marketType : MarketTypeEnum.ALL;
    const upDownType: string = request.upDownType != null ? request.upDownType : UpDownTypeEnum.DOWN;

    const listSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);

    const listSymbolInfoRanking: ISymbolInfo[] = listSymbolInfo.filter((item: ISymbolInfo) => {
      return item.type === SecuritiesTypeEnum.STOCK;
    });
    let response: StockRankingUpDownResponse;

    if (marketType === MarketTypeEnum.ALL) {
      const hnxList: ISymbolInfo[] = this.filterRankingUpDown(listSymbolInfoRanking, MarketTypeEnum.HNX, upDownType, fetchCount, offset);
      const hoseList: ISymbolInfo[] = this.filterRankingUpDown(listSymbolInfoRanking, MarketTypeEnum.HOSE, upDownType, fetchCount, offset);
      const upcomList: ISymbolInfo[] = this.filterRankingUpDown(listSymbolInfoRanking, MarketTypeEnum.UPCOM, upDownType, fetchCount, offset);

      response = {
        HNX: hnxList.map(toStockRankingUpdownResponse),
        HOSE: hoseList.map(toStockRankingUpdownResponse),
        UPCOM: upcomList.map(toStockRankingUpdownResponse),
      };
    } else {
      const result: ISymbolInfo[] = this.filterRankingUpDown(listSymbolInfo, marketType, upDownType, fetchCount, offset);
      response = {
        [marketType]: result.map(toStockRankingUpdownResponse),
      };
    }

    return response;
  }

  public filterRankingUpDown(
    listSymbolInfo: ISymbolInfo[],
    marketType: string,
    upDownType: string,
    fetchCount: number,
    offset: number,
  ): ISymbolInfo[] {
    const fitlerMarketType: ISymbolInfo[] = listSymbolInfo.filter((item: ISymbolInfo) => {
      return item.marketType === marketType;
    });

    const sortUpDown: ISymbolInfo[] = fitlerMarketType.sort((a: ISymbolInfo, b: ISymbolInfo) => {
      const x = a.rate;
      const y = b.rate;
      if (upDownType === UpDownTypeEnum.UP) {
        return x - y;
      } else {
        return y - x;
      }
    });
    // off set and fetch count
    return sortUpDown.slice(offset, fetchCount);
  }

  public async querySymbolTickSizeMatch(request: SymbolTickSizeMatchRequest): Promise<SymbolTickSizeMatchResponse> {
    const validator: Ajv.ValidateFunction = symbolTickSizeMatchRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const symbol: string = request.symbol;
    // query redis. Redis are sorted from latest to oldest
    const listSymbolQuote: ISymbolQuote[] = await this.redisService.lrange(`${REDIS_KEY.SYMBOL_QUOTE}_${symbol}`, 0, -1);
    return toSymbolTickSizeMatchResponse(listSymbolQuote);
  }

  public async queryStockRankingTop(request: StockRankingTopRequest): Promise<StockRankingTopResponse[]> {
    const validator: Ajv.ValidateFunction = stockRankingTopRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const offset: number = request.offset != null ? request.offset : DEFAULT_OFFSET;
    const marketType: string = request.marketType != null ? request.marketType : MarketTypeEnum.ALL;
    const sortType: string = request.sortType != null ? request.sortType : TopSortTypeEnum.TRADING_VOLUME;
    const upDownType: string = request.upDownType != null ? request.upDownType : UpDownTypeEnum.DOWN;

    const listSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    let stockInfoList: ISymbolInfo[] = listSymbolInfo.filter((item: ISymbolInfo) => {
      return item.type === SecuritiesTypeEnum.STOCK;
    });
    // filter by marketType
    if (marketType !== MarketTypeEnum.ALL) {
      stockInfoList = stockInfoList.filter((item: ISymbolInfo) => {
        return item.marketType === marketType;
      });
    }

    // sort by sort type
    if (sortType !== TopSortTypeEnum.POWER) {
      stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
        let result: number = 0;
        if (sortType === TopSortTypeEnum.TRADING_VOLUME) {
          result = a.tradingVolume - b.tradingVolume;
        } else if (sortType === TopSortTypeEnum.TRADING_VALUE) {
          result = a.tradingValue - b.tradingValue;
        } else if (sortType === TopSortTypeEnum.CHANGE) {
          result = a.change - b.change;
        } else if (sortType === TopSortTypeEnum.RATE) {
          result = a.rate - b.rate;
        }
        return upDownType === UpDownTypeEnum.UP ? result : -result;
      });
    } else {
      if (upDownType === UpDownTypeEnum.DOWN) {
        // DOWN mean sort POWER stock from best to worse, UP mean WEAK stock from worst to better
        // find power stock
        stockInfoList = stockInfoList.filter((stock: ISymbolInfo) => {
          return stock.bidVolume * stock.bidPrice > POWER_STOCK_SORT_THRESHOLD && stock.bidPrice === stock.ceilingPrice;
        });

        // sort by bidVolume * bidPrice
        stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
          const x = a.bidVolume * a.bidPrice;
          const y = b.bidVolume * b.bidPrice;
          return -(x - y);
        });
      } else {
        // find weakest stocks
        stockInfoList = stockInfoList.filter((stock: ISymbolInfo) => {
          return stock.offerVolume * stock.offerPrice > POWER_STOCK_SORT_THRESHOLD && stock.offerPrice === stock.floorPrice;
        });

        // sort by offerVolume * offerPrice
        stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
          const x = a.offerVolume * a.offerPrice;
          const y = b.offerVolume * b.offerPrice;
          // sort by most selled stock to less
          return -(x - y);
        });
      }
    }

    // offset and fetch count
    const response: ISymbolInfo[] = stockInfoList.splice(offset, fetchCount);
    return response.map((item: ISymbolInfo) => {
      return toStockRankingTopResponse(item, sortType, upDownType);
    });
  }

  public async queryForeignerRanking(request: IForeignerRankingRequest): Promise<IForeignerRankingResponse[]> {
    if (request.type == null || request.type === '') {
      throw new Errors.GeneralError('TYPE_IS_REQUIRED');
    }
    const marketType = request.market != null ? request.market : MarketTypeEnum.ALL;
    const type = request.type;
    if (type !== ForeignerRankingTypeEnum.BUY && type !== ForeignerRankingTypeEnum.SELL) {
      throw new Errors.GeneralError('TYPE_IS_INVALID');
    }
    const upDownType = UpDownTypeEnum.DOWN;

    const listSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    let stockInfoList: ISymbolInfo[] = listSymbolInfo.filter((item: ISymbolInfo) => {
      return item.type === SecuritiesTypeEnum.STOCK;
    });

    // filter by marketType
    if (marketType !== MarketTypeEnum.ALL) {
      stockInfoList = stockInfoList.filter((item: ISymbolInfo) => {
        return item.marketType === marketType;
      });
    }

    const intSort = upDownType === UpDownTypeEnum.DOWN.valueOf() ? -1 : 1;

    // sort by foreignerBuyVolume or foreignerSellVolume
    if (type === ForeignerRankingTypeEnum.BUY) {
      stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
        const x = a.foreignerBuyVolume;
        const y = b.foreignerBuyVolume;
        return intSort * (x - y);
      });
    }
    if (type === ForeignerRankingTypeEnum.SELL) {
      stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
        const x = a.foreignerSellVolume;
        const y = b.foreignerSellVolume;
        return intSort * (x - y);
      });
    }

    // offset and fetch count
    const response: ISymbolInfo[] = stockInfoList.splice(0, 10);

    return toForeignerRankingResponse(response);
  }

  public async queryTopForeignerTrading(request: TopForeignerTradingRequest): Promise<TopForeignerTradingResponse[]> {
    const validator: Ajv.ValidateFunction = topForeignerTradingRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_TOP_FOREIGNER_TRADING;
    const offset: number = request.offset != null ? request.offset : DEFAULT_OFFSET;
    const marketType: string = request.marketType != null ? request.marketType : MarketTypeEnum.ALL;
    const upDownType: string = request.upDownType != null ? request.upDownType : UpDownTypeEnum.DOWN;

    const listSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    let stockInfoList: ISymbolInfo[] = listSymbolInfo.filter((item: ISymbolInfo) => {
      return item.type === SecuritiesTypeEnum.STOCK;
    });

    // filter by marketType
    if (marketType !== MarketTypeEnum.ALL) {
      stockInfoList = stockInfoList.filter((item: ISymbolInfo) => {
        return item.marketType === marketType;
      });
    }

    const intSort = upDownType === UpDownTypeEnum.DOWN.valueOf() ? -1 : 1;

    // sort by foreignerBuyVolume * last - foreignerSellVolume * last
    stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
      const x = a.foreignerBuyVolume * a.last - a.foreignerSellVolume * a.last;
      const y = b.foreignerBuyVolume * b.last - b.foreignerSellVolume * b.last;
      return intSort * (x - y);
    });

    // offset and fetch count
    const response: ISymbolInfo[] = stockInfoList.splice(offset, fetchCount);

    return response.map(toTopForeignerTradingResponse);
  }

  public async querySymbolRankingTrade(request: StockRankingTradeRequest): Promise<StockRankingTradeResponse> {
    const validator: Ajv.ValidateFunction = stockRankingTradeRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const offset: number = request.offset != null ? request.offset : DEFAULT_OFFSET;
    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const marketType: string = request.marketType != null ? request.marketType : MarketTypeEnum.ALL;
    let listSymbolRankingTrade: ISymbolInfo[] = [];
    const listSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    if (listSymbolInfo != null) {
      listSymbolRankingTrade = listSymbolInfo.filter((item: ISymbolInfo) => {
        return item.type === SecuritiesTypeEnum.STOCK;
      });
      if (marketType !== MarketTypeEnum.ALL.valueOf()) {
        listSymbolRankingTrade = listSymbolRankingTrade.filter((item: ISymbolInfo) => {
          return item.marketType === marketType;
        });
      }
      if (request.sortType === StockRankingTradeSortTypeEnum.TRADING_VOLUME) {
        listSymbolRankingTrade.sort((a: ISymbolInfo, b: ISymbolInfo) => {
          return b.tradingVolume - a.tradingVolume;
        });
      } else if (request.sortType === StockRankingTradeSortTypeEnum.TRADING_VALUE) {
        listSymbolRankingTrade.sort((a: ISymbolInfo, b: ISymbolInfo) => {
          return b.tradingValue - a.tradingValue;
        });
      } else {
        listSymbolRankingTrade.sort((a: ISymbolInfo, b: ISymbolInfo) => {
          return b.turnoverRate - a.turnoverRate;
        });
      }
    }
    const response: StockRankingTradeResponse[] = [];
    for (let i = offset; i < listSymbolRankingTrade.length; i++) {
      if (i >= fetchCount + offset) {
        break;
      }
      const item: ISymbolInfo = listSymbolRankingTrade[i];
      response.push(toStockRankingTradeResponse(item));
    }
    return response;
  }

  public async updateDailyByDividend(code: string, totalAdjustRate: number, baseDate: Date): Promise<void> {
    Logger.info(`__update daily: ${code} _ ${baseDate}`);
    const query: FilterQuery<ISymbolDaily> = {
      code: code,
      date: { $lt: baseDate },
    };
    const sort: any = {
      date: -1,
    };
    const symbolDailyList: ISymbolDaily[] = await this.symbolDailyRepository.findBy(query, Number.MAX_SAFE_INTEGER, 0, sort).toArray();

    if (symbolDailyList.length > 0) {
      Logger.info(`number of records: ${symbolDailyList.length}`);
      for (let i = 0; i < symbolDailyList.length; i++) {
        const symbolDaily: ISymbolDaily = symbolDailyList[i];
        this.updateAdjustedPriceForDaily(symbolDaily, totalAdjustRate);
      }
      await this.symbolDailyRepository.updateByBulk(symbolDailyList);
    }
  }

  public async updateMinuteByDividend(code: string, totalAdjustRate: number, baseDate: Date): Promise<void> {
    Logger.info(`__update minute: ${code} _ ${baseDate}`);
    const query: FilterQuery<ISymbolQuoteMinutes> = {
      code: code,
      date: { $lt: baseDate },
    };
    const sort: any = {
      date: -1,
    };
    const quoteMinuteList: ISymbolQuoteMinutes[] = await this.symbolQuoteMinuteRepo.findBy(query, Number.MAX_SAFE_INTEGER, sort).toArray();

    if (quoteMinuteList.length > 0) {
      Logger.info(`number of records: ${quoteMinuteList.length}`);
      for (let i = 0; i < quoteMinuteList.length; i++) {
        const symbolQuoteMinutes: ISymbolQuoteMinutes = quoteMinuteList[i];
        this.updateAdjustedPriceForMinute(symbolQuoteMinutes, totalAdjustRate);
      }
      await this.symbolQuoteMinuteRepo.updateByBulk(quoteMinuteList);
    }
  }

  public updateAdjustedPriceForDaily(dailyStock: ISymbolDaily, totalAdjustRate: number): void {
    dailyStock.open = dailyStock.open * totalAdjustRate;
    dailyStock.high = dailyStock.high * totalAdjustRate;
    dailyStock.low = dailyStock.low * totalAdjustRate;
    dailyStock.last = dailyStock.last * totalAdjustRate;
    dailyStock.change = dailyStock.change * totalAdjustRate;
  }

  public updateAdjustedPriceForMinute(quoteMinutes: ISymbolQuoteMinutes, totalAdjustRate: number): void {
    quoteMinutes.open = quoteMinutes.open * totalAdjustRate;
    quoteMinutes.high = quoteMinutes.high * totalAdjustRate;
    quoteMinutes.low = quoteMinutes.low * totalAdjustRate;
    quoteMinutes.last = quoteMinutes.last * totalAdjustRate;
  }

  public async initSymbolDailyReturns(request?: SymbolDailyReturnsInitRequest): Promise<void> {
    const validator: Ajv.ValidateFunction = symbolDailyReturnsInitRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    //if don't have request input, calculate whole db
    const floorDate: Date = request == null || request.floorDate == null ? DEFAULT_FLOOR_DATE : Utils.convertStringToDate(request.floorDate);

    let allUnderlyingSymbolList: string[] = [];
    if (request == null || request.symbolList == null || request.symbolList.length === 0) {
      // find all underlying symbol list
      allUnderlyingSymbolList = await this.getAllUnderlyingSymbolList();
    } else {
      allUnderlyingSymbolList = request.symbolList;
    }

    //get all symbol daily, from floorDate to today
    const groupedSymbolDailyList: IGroupedSymbolDailyResponse[] = await this.symbolDailyRepository
      .queryGroupedSymbolDailyList(allUnderlyingSymbolList, MONGO_MAX_SAFE_ARRAY_SIZE, floorDate)
      .toArray();

    const finalToSavedList: ISymbolDaily[] = [];

    for (const singleCodeData of groupedSymbolDailyList) {
      // list of records of 1 single symbol code
      const itemList: ISymbolDaily[] = singleCodeData.items;

      let previousLastPrice: number = 0;
      // itemList have been sorted with date: -1, so need manual forloop
      for (let i = itemList.length; i > 0; i--) {
        const data: ISymbolDaily = itemList[i - 1];
        if (previousLastPrice === 0) {
          previousLastPrice = data.last;
          continue;
        }
        data.returns = Math.log(data.last / previousLastPrice);
        finalToSavedList.push(data);
        previousLastPrice = data.last;
      }
    }
    Logger.info(`============Done init returns for ${finalToSavedList.length} records of ${allUnderlyingSymbolList.length} symbol========`);
    await this.symbolDailyRepository.updateReturnsByBulk(finalToSavedList);
  }

  public async getAllUnderlyingSymbolList(): Promise<string[]> {
    const allCwList: ISymbolInfo[] = await this.symbolInfoRepository
      .findBy(
        {
          type: SecuritiesTypeEnum.CW,
        },
        Number.MAX_SAFE_INTEGER,
      )
      .toArray();

    const cwSet = new Set<string>();
    for (const cw of allCwList) {
      cwSet.add(cw.underlyingSymbol.trim());
    }
    return [...cwSet];
  }

  public async querySymbolDailyReturns(request: SymbolDailyReturnsRequest): Promise<SymbolDailyReturnsResponse> {
    const validator: Ajv.ValidateFunction = symbolDailyReturnsRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    const numberOfDays = request.numberOfDays == null ? DEFAULT_QUERY_DAILY_RETURN_DAYS : request.numberOfDays;

    const symbolLastPriceList: IGroupedSymbolDailyResponse[] = await this.symbolDailyRepository
      .queryGroupedSymbolDailyList(request.symbolList, numberOfDays)
      .toArray();

    const codeReturnsDict = {};
    for (const daily of symbolLastPriceList) {
      for (const item of daily.items) {
        if (codeReturnsDict[daily._id] == null) {
          codeReturnsDict[daily._id] = [];
        }
        codeReturnsDict[daily._id].push(item.returns);
      }
    }

    return codeReturnsDict;
  }

  public async queryForeignerSummary(request: ForeignerSummaryRequest): Promise<ForeignerSummaryResponse[]> {
    const validator: Ajv.ValidateFunction = foreignerSummaryRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    const fetchCount: number = request.fetchCount != null ? request.fetchCount : DEFAULT_DAILY_FETCH_COUNT;
    const offset: number = request.offset != null ? request.offset : DEFAULT_OFFSET;
    const marketType: string = request.marketType != null ? request.marketType : MarketTypeEnum.ALL;
    const sortType: string = request.sortType != null ? request.sortType : FOREIGNER_SUMMARY_SORT_TYPE.CODE;

    const listSymbolInfo: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    let stockInfoList: ISymbolInfo[] = listSymbolInfo.filter((item: ISymbolInfo) => {
      return item.type === SecuritiesTypeEnum.STOCK;
    });
    // filter by marketType
    if (marketType !== MarketTypeEnum.ALL) {
      stockInfoList = stockInfoList.filter((item: ISymbolInfo) => {
        return item.marketType === marketType;
      });
    }

    // sort by sort type
    if (sortType === FOREIGNER_SUMMARY_SORT_TYPE.NET_VOLUME) {
      stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
        return a.foreignerBuyVolume - a.foreignerSellVolume - (b.foreignerBuyVolume - b.foreignerSellVolume);
      });
    } else {
      if (sortType === FOREIGNER_SUMMARY_SORT_TYPE.NET_VALUE) {
        stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
          return a.foreignerBuyValue - a.foreignerSellValue - (b.foreignerBuyValue - b.foreignerSellValue);
        });
      } else {
        stockInfoList = stockInfoList.sort((a: ISymbolInfo, b: ISymbolInfo) => {
          return a.code.localeCompare(b.code);
        });
      }
    }

    // offset and fetch count
    const response: ISymbolInfo[] = stockInfoList.splice(offset, fetchCount);
    return response.map(toForeignerSummaryResponse);
  }

  public async queryPtDealTotal(request: IPtDealTotalRequest): Promise<IPtDealTotalResponse> {
    const marketType: string = request.marketType != null ? request.marketType : MarketTypeEnum.HOSE;
    const dealNoticeList: DealNoticeData[] = await this.redisService.lrange(`${REDIS_KEY.DEAL_NOTICE}_${marketType}`, 0, -1);
    return toPtDealTotalResponse(dealNoticeList);
  }

  public async calculateQuoteMinute(request: ICalculateMinuteRequest) {
    Logger.info(`start calculateQuoteMinute`);

    let symbolInfoList: ISymbolInfo[] = [];

    if (request.symbolList != null && request.symbolList.length > 0) {
      const filter: FilterQuery<ISymbolInfo> = {
        _id: { $in: request.symbolList },
      };
      symbolInfoList = await this.symbolInfoRepository.findBy(filter).toArray();
    } else {
      symbolInfoList = await this.symbolInfoRepository.findAll().toArray();
    }

    const today: Date = new Date();
    const from: Date = Utils.getStartOfDate(today);
    const to: Date = Utils.getEndOfDate(today);

    Logger.info(`from: ${from} - to: ${to}`);

    for (let i = 0; i < symbolInfoList.length; i++) {
      const symbolInfo: ISymbolInfo = symbolInfoList[i];
      const symbolQuoteMinutesList: ISymbolQuoteMinutes[] = await this.symbolQuoteRepository.querySymbolQuoteMinutes(symbolInfo._id, from, to);
      Logger.info(`symbol: ${symbolInfo._id} - length: ${symbolQuoteMinutesList.length}`);
      if (symbolQuoteMinutesList.length > 0) {
        const bulk: UnorderedBulkOperation = this.symbolQuoteMinuteRepo.createBulk();
        for (let j = 0; j < symbolQuoteMinutesList.length; j++) {
          const symbolQuoteMinutes: ISymbolQuoteMinutes = symbolQuoteMinutesList[j];
          bulk.find({ _id: symbolQuoteMinutes._id }).upsert().update({ $set: symbolQuoteMinutes });
        }
        const result: BulkWriteResult = await bulk.execute();
        if (result.hasWriteErrors()) {
          throw {
            message: result.getWriteErrors(),
            getErrors: () => result.getWriteErrors(),
          };
        }
      }
    }
    Logger.info(`finish calculateQuoteMinute`);
  }

  public async queryStockRankingPeriod(request: IQueryStockRankingPeriod): Promise<IStockRankingPeriodResponse[]> {
    Logger.info(`start queryStockRankingPeriod`);
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.ranking, 'ranking').setRequire().throwValid(invalidParams);
    Utils.validate(request.period, 'period').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const marketType = request.marketType != null ? request.marketType : MarketTypeEnum.ALL;
    const period = request.period;
    const ranking = request.ranking;
    const pageSize: number = request.pageSize != null ? +request.pageSize : +20;
    const pageNumber: number = request.pageNumber != null ? +request.pageNumber : +0;
    const key = `${REDIS_KEY.STOCK_RANKING_PERIOD}_${marketType}_${ranking}_${period}`;
    Logger.info(`key: ${key}`);
    const result: IRedisStockRankingPeriodResponse = await this.redisService.get(key);
    const startIndex = pageNumber * pageSize;
    const endIndex = startIndex + pageSize;
    const resList: IRedisStockRankingPeriodItemResponse[] = result.symbols.slice(startIndex, endIndex);
    Logger.info(`finish queryStockRankingPeriod`);
    return toStockRankingPeriodResponse(resList);
  }

  public async getDailyAccumulativeVNIndex(request: IDailyAccumulativeVNIndexRequest, msgId: string): Promise<IDailyAccumulativeVNIndexResponse[]> {
    const invalidParams = new InvalidParameterError();
    validate(request.fromDate, 'fromDate').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const response: IDailyAccumulativeVNIndexResponse[] = [];

    let fromDate: Date;
    try {
      fromDate = Utils.convertStringToDate(request.fromDate, 'YYYYMMDD');
    } catch (e) {
      Logger.error(`${msgId} error`, e);
      throw new GeneralError('INVALID_FORMAT_FROM_DATE');
    }
    Logger.info(`${msgId} fromDate: ${fromDate}`);

    const today = new Date();
    if (!fromDate || fromDate > today) {
      throw new GeneralError('INVALID_DATE');
    }

    const toDate = new Date();
    toDate.setDate(toDate.getDate() - 1);

    const stockCode: string = 'VN';
    const pageSize: number = Number(request.pageSize || 50);
    const pageNo: number = Number(request.pageNumber || 0);

    Logger.info(`${msgId} pageSize: ${pageSize} pageNo: ${pageNo}`);
    const query: FilterQuery<ISymbolDaily> = {
      code: stockCode,
      date: {
        $gte: Utils.getStartOfDate(fromDate),
        $lte: Utils.getEndOfDate(toDate),
      },
    };
    const sort: any = {
      date: 1,
    };
    const symbolDailyQuery: ISymbolDaily[] = await this.symbolDailyRepository.findBy(query, Number.MAX_SAFE_INTEGER, 0, sort).toArray();

    const totalElements: number = symbolDailyQuery.length;
    let symbolDailyList: ISymbolDaily[] = [];

    if (!isHoliday(today) && !Utils.isWeekend(today)) {
      const totalPages: number = Math.ceil((totalElements + 1) / pageSize);
      const pageOrder: number = pageNo + 1;
      Logger.info(`${msgId} totalPages: ${totalPages} pageOrder: ${pageOrder}`);
      if (totalPages === pageOrder) {
        symbolDailyList = await this.getSymbolDailyListHasCurrentDate(symbolDailyQuery, stockCode, today);
      } else {
        Logger.warn(`${msgId} pageNo is not the last page`);
        symbolDailyList = symbolDailyQuery;
      }
    } else {
      Logger.warn(`${msgId} today is holiday or weekend`);
      symbolDailyList = symbolDailyQuery;
    }
    if (symbolDailyList.length > 0) {
      const lastPriceFirst: number = symbolDailyList[0].last;

      const startIndex = Math.max(pageNo * pageSize, 0);
      const endIndex = Math.min(startIndex + pageSize, symbolDailyList.length);
      Logger.info(`${msgId} symbolDailyList slice from startIndex: ${startIndex} endIndex: ${endIndex}`);
      symbolDailyList = symbolDailyList.slice(startIndex, endIndex);

      if (symbolDailyList) {
        for (const symbolDaily of symbolDailyList) {
          const c: number = symbolDaily.last;
          let nr: number = 0.0;
          if (c !== null && c !== undefined && lastPriceFirst) {
            nr = Utils.round((c / lastPriceFirst - 1) * 100, 2);
          }

          const vnIndexDTO = {
            d: Utils.formatDateToDisplay(symbolDaily.date, 'YYYYMMDD'),
            c: c,
            ch: symbolDaily.change,
            r: symbolDaily.rate ? Utils.round(symbolDaily.rate, 2) : null,
            nr: nr,
          };
          response.push(vnIndexDTO);
        }
      }
    }
    return response;
  }

  public async getSymbolDailyListHasCurrentDate(symbolDailyQuery: ISymbolDaily[], stockCode: string, currentDate: Date): Promise<ISymbolDaily[]> {
    const symbolInfoCurrent: ISymbolInfo = await this.redisService.hget<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO, stockCode);
    Logger.info(`symbolInfo redis: ${symbolInfoCurrent}`);
    if (symbolInfoCurrent) {
      const symbolDaily: ISymbolDaily = {
        code: null,
        open: null,
        high: null,
        low: null,
        tradingVolume: null,
        tradingValue: null,
        date: currentDate,
        last: symbolInfoCurrent.last,
        change: symbolInfoCurrent.change,
        rate: symbolInfoCurrent.rate,
      };
      return [...symbolDailyQuery, symbolDaily];
    } else {
      return symbolDailyQuery;
    }
  }
}
