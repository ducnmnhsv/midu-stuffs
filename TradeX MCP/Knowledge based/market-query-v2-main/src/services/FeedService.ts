import { IQuerySymbolHistoryEventsResponse, toQuerySymbolHistoryResponse } from './../models/response/IQuerySymbolHistoryEventsResponse';
import { SymbolHistoryEventsRepository } from './../repositories/SymbolHistoryEventsRepository';
import { Inject, Service } from 'typedi';
import ConfigResponse from '../models/response/ConfigResponse';
import { Errors, Utils } from 'tradex-common';
import * as Ajv from 'ajv';
import {
  TradingViewHistoryResponse,
  TradingViewSymbolSearchRequest,
  TradingViewSymbolSearchResponse,
  TradingViewSymbolInfoRequest,
  SymbolPeriodResponse,
} from 'tradex-models-market';
import {
  tradingViewHistoryRequestValidator,
  tradingViewSymbolInfoRequestValidator,
  tradingViewSymbolSearchRequestValidator,
} from 'tradex-models-market-validator';
import { ISymbolInfo } from '../models/db/ISymbolInfo';
import { parseSymbolQuoteMinuteList, parseTradingviewDailyPeriodList, parseSymbolInfo, toQuerySymbolSearchResponse } from '../utils/ResponseUtils';
import {
  INVALID_PARAMETER,
  RESOLUTION_MINUTE,
  DEFAULT_FLOOR_DATE,
  PERIOD_TYPE,
  RESOLUTION_PERIOD,
  SecuritiesTypeEnum,
  IndexTypeEnum,
} from '../constants';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';
import { validateRequest } from '../utils/parse';
import CommonService from './common/CommonService';
import CacheService from './CacheService';
import { IQuerySymbolHistoryEventsRequest } from '../models/request/IQuerySymbolHistoryEventsRequest';
import { ITradingViewHistoryRequest } from '../models/request/ITradingViewHistoryRequest';

@Service()
export default class FeedService {
  @Inject()
  private readonly cacheService: CacheService;
  @Inject()
  private readonly commonService: CommonService;
  @Inject()
  private readonly symbolHistoryEventsRepo: SymbolHistoryEventsRepository;

  public async queryConfig(): Promise<ConfigResponse> {
    return new ConfigResponse();
  }

  public async queryTradingViewHistory(request: ITradingViewHistoryRequest): Promise<TradingViewHistoryResponse> {
    validateRequest(request, tradingViewHistoryRequestValidator);
    if (request.from > request.to) {
      throw new Errors.GeneralError('TO_MUST_BE_GREATER_THAN_OR_EQUAL_FROM');
    }

    if (RESOLUTION_MINUTE.includes(request.resolution)) {
      return this.getQuoteMinuteHistory(request);
    } else {
      return this.getDailyPeriodHistory(request);
    }
  }

  public async getQuoteMinuteHistory(request: ITradingViewHistoryRequest): Promise<TradingViewHistoryResponse> {
    //query list, if list empty, return 1 record < fromTime
    const fromTime: Date = new Date(request.from * 1000);
    let toTime: Date = new Date(request.to * 1000);
    if (request.lastTime != null && request.lastTime < request.to) {
      const newToTime = new Date(request.lastTime * 1000);
      newToTime.setSeconds(0);
      newToTime.setSeconds(newToTime.getSeconds() - 1);
      toTime = new Date(newToTime);
    }

    const symbolInfo: ISymbolInfo = await this.cacheService.getSymbolInfo(request.symbol);

    if (symbolInfo == null) {
      return {};
    }

    let noData = false;
    let countback: number = Number(request.countback || 0);
    const minuteUnit = parseInt(request.resolution, 10);
    const symbolQuoteMinuteList: ISymbolQuoteMinutes[] = await this.commonService.actualQueryQuoteMinuteHistory(
      request.symbol,
      symbolInfo,
      fromTime,
      toTime,
      minuteUnit,
      countback,
    );

    if (symbolQuoteMinuteList == null || symbolQuoteMinuteList.length < countback) {
      noData = true;
    }

    if (symbolQuoteMinuteList != null || symbolQuoteMinuteList.length > 0) {
      return parseSymbolQuoteMinuteList(symbolQuoteMinuteList, null, noData);
    }

    //query normally, but if list = empty, return nextTimeToDate: $lt fromTime date
    //return nextTime of 1 record which $lt: fromTime
    const nextTimeToDate: Date = new Date(fromTime);
    nextTimeToDate.setSeconds(0);
    nextTimeToDate.setSeconds(nextTimeToDate.getSeconds() - 1);
    const nextQuoteMinute: ISymbolQuoteMinutes[] = await this.commonService.actualQueryQuoteMinuteThenGrouped(
      request.symbol,
      DEFAULT_FLOOR_DATE,
      nextTimeToDate,
      1,
      1,
    );
    let nextTime: number;
    if (nextQuoteMinute.length === 0) {
      nextTime = null;
    } else {
      const nextTimeDate: Date = nextQuoteMinute[0].date;
      nextTimeDate.setMilliseconds(0);
      nextTimeDate.setSeconds(0);
      nextTime = nextTimeDate.getTime() / 1000;
    }
    return parseSymbolQuoteMinuteList([], nextTime, noData);
  }

  public async getDailyPeriodHistory(request: ITradingViewHistoryRequest): Promise<TradingViewHistoryResponse> {
    //query all from mongo, update today record from redis, sort date: -1, return date: 1
    const toTime: Date = Utils.getEndOfDate(
      new Date((request.lastTime != null && request.lastTime < request.to ? request.lastTime - 86400 : request.to) * 1000),
    );
    const fromTime: Date = Utils.getStartOfDate(new Date(request.from * 1000));
    let periodType = PERIOD_TYPE.DAILY;
    const resolution = request.resolution;
    if (RESOLUTION_PERIOD.DAILY.indexOf(resolution) > -1) {
      periodType = PERIOD_TYPE.DAILY;
    } else if (RESOLUTION_PERIOD.WEEKLY.indexOf(resolution) > -1) {
      periodType = PERIOD_TYPE.WEEKLY;
    } else if (RESOLUTION_PERIOD.MONTHLY.indexOf(resolution) > -1) {
      periodType = PERIOD_TYPE.MONTHLY;
    } else if (RESOLUTION_PERIOD.SIX_MONTHLY.indexOf(resolution) > -1) {
      periodType = PERIOD_TYPE.SIX_MONTH;
    }

    const symbolInfo: ISymbolInfo = await this.cacheService.getSymbolInfo(request.symbol);

    const symbolPeriodResponseList: SymbolPeriodResponse[] = await this.commonService.querySymbolHistory(symbolInfo, periodType, fromTime, toTime);

    const countback: number = Number(request.countback || 0);
    if (countback >= 0 && symbolPeriodResponseList.length < countback) {
      const symbolPeriodCountBackList: SymbolPeriodResponse[] = await this.commonService.querySymbolCountBackHistory(
        symbolInfo,
        periodType,
        countback,
        toTime,
      );
      let noData = false;
      if (symbolPeriodCountBackList.length < countback) {
        noData = true;
      }
      return parseTradingviewDailyPeriodList(symbolPeriodCountBackList, noData);
    }
    return parseTradingviewDailyPeriodList(symbolPeriodResponseList);
  }

  public async querySymbolSearch(request: TradingViewSymbolSearchRequest): Promise<TradingViewSymbolSearchResponse[]> {
    const validator: Ajv.ValidateFunction = tradingViewSymbolSearchRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const symbolInfoList: ISymbolInfo[] = await this.cacheService.getAllSymbolInfo();
    if (symbolInfoList.length < 1) {
      return [];
    }

    const result: ISymbolInfo[] = [];
    const priorityResult: ISymbolInfo[] = [];
    let finalResult: ISymbolInfo[];
    for (let i = 0; i < symbolInfoList.length; i++) {
      const symbol: ISymbolInfo = symbolInfoList[i];

      // ignore foreign index
      if (symbol.type === SecuritiesTypeEnum.INDEX && symbol.indexType === IndexTypeEnum.FOREIGN) {
        continue;
      }

      if (request.type != null && request.type !== '' && request.type.toUpperCase() !== symbol.type.toUpperCase()) {
        continue;
      }

      if (request.exchange != null && request.exchange !== '' && request.exchange.toUpperCase() !== symbol.marketType.toUpperCase()) {
        continue;
      }

      if (request.query != null) {
        if (symbol.code.toUpperCase().includes(request.query.toUpperCase())) {
          priorityResult.push(symbol);
          continue;
        }
        if (
          (symbol.name == null || !symbol.name.toUpperCase().includes(request.query.toUpperCase())) &&
          (symbol.nameEn == null || !symbol.nameEn.toUpperCase().includes(request.query.toUpperCase()))
        ) {
          continue;
        }
      }
      result.push(symbol);
    }
    if (request.limit != null) {
      finalResult = priorityResult.concat(result).slice(0, request.limit);
      return finalResult.map(toQuerySymbolSearchResponse);
    }
    finalResult = priorityResult.concat(result);
    return finalResult.map(toQuerySymbolSearchResponse);
  }

  public async querySymbolInfo(request: TradingViewSymbolInfoRequest): Promise<any> {
    const validator: Ajv.ValidateFunction = tradingViewSymbolInfoRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const data = request.symbol.split(':');
    const exchange = (data.length > 1 ? data[0] : '').toUpperCase();
    const code = data.length > 1 ? data[1] : request.symbol;

    const symbolInfo: ISymbolInfo = await this.cacheService.getSymbolInfo(code);

    if (symbolInfo != null) {
      if (symbolInfo.code === code && (exchange == null || exchange === '' || exchange === symbolInfo.marketType.toUpperCase())) {
        return parseSymbolInfo(symbolInfo);
      }
    } else {
      return parseSymbolInfo(null);
    }
  }

  public async querySymbolHistoryEvents(request: IQuerySymbolHistoryEventsRequest): Promise<IQuerySymbolHistoryEventsResponse> {
    const symbolEventList = await this.symbolHistoryEventsRepo
      .findBy(
        {
          stock: request.symbol,
          eventDate: {
            $gt: Utils.getStartOfDate(Utils.convertStringToDate(request.from)),
            $lte: Utils.getEndOfDate(Utils.convertStringToDate(request.to)),
          },
          language: request.headers['accept-language'] ? (request.headers['accept-language'] === 'vi' ? 'VI' : 'EN') : 'EN',
        },
        {
          eventDate: -1,
        },
      )
      .toArray();
    return toQuerySymbolHistoryResponse(symbolEventList);
  }
}
