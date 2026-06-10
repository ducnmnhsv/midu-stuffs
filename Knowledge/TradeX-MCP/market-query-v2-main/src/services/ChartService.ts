import { Inject, Service } from 'typedi';
import { Errors, Logger, Utils } from 'tradex-common';
import { ChartRepository } from '../repositories/ChartRepository';
import { INVALID_PARAMETER, MARKET_INDEX_ENUM, StatusResponseEnum } from '../constants';
import { ObjectID } from 'mongodb';
import { convertFromChart, covertFromChartToChartLoadInfo } from '../utils/ResponseUtils';
import { IChart } from '../models/db/IChart';
import {
  MarketLiquidityRequest,
  MarketLiquidityResponse,
  TradingViewDeleteChartRequest,
  TradingViewDeleteChartResponse,
  TradingViewListChartRequest,
  TradingViewListChartResponse,
  TradingViewLoadChartRequest,
  TradingViewLoadChartResponse,
  TradingViewSaveChartRequest,
  TradingViewSaveChartResponse,
  TradingViewUpdateChartRequest,
  TradingViewUpdateChartResponse,
} from 'tradex-models-market';
import * as Ajv from 'ajv';
import {
  marketLiquidityRequestValidator,
  tradingViewDeleteChartRequestValidator,
  tradingViewListChartRequestValidator,
  tradingViewLoadChartRequestValidator,
  tradingViewSaveChartRequestValidator,
  tradingViewUpdateChartRequestValidator,
} from 'tradex-models-market-validator';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';
import { SymbolQuoteMinutesRepository } from '../repositories/SymbolQuoteMinutesRepository';
import { SymbolInfoRepository } from '../repositories/SymbolInfoRepository';
import RedisService, { REDIS_KEY } from './RedisService';

@Service()
export default class ChartService {
  @Inject()
  public chartRepository: ChartRepository;
  @Inject()
  public symbolQuoteMinutesRepository: SymbolQuoteMinutesRepository;
  @Inject()
  public symbolInfoRepository: SymbolInfoRepository;
  @Inject()
  private readonly redisService: RedisService;

  public async saveChart(request: TradingViewSaveChartRequest): Promise<TradingViewSaveChartResponse> {
    const validator: Ajv.ValidateFunction = tradingViewSaveChartRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    let status: string = StatusResponseEnum.OK.valueOf();
    let id: string;
    try {
      const result = await this.chartRepository.save({
        ownerSource: request.client,
        ownerId: `${request.headers.token.userId}`,
        content: request.content,
        name: request.name,
        resolution: request.resolution,
        symbol: request.symbol,
        lastModified: new Date(),
      });
      id = result.insertedId.toHexString();
    } catch (err) {
      Logger.error(err);
      status = StatusResponseEnum.ERROR.valueOf();
    }
    return {
      id: id,
      status: status,
    };
  }

  public async updateChart(request: TradingViewUpdateChartRequest): Promise<TradingViewUpdateChartResponse> {
    const validator: Ajv.ValidateFunction = tradingViewUpdateChartRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    let status: string = StatusResponseEnum.OK.valueOf();
    try {
      await this.chartRepository.update(
        {
          _id: new ObjectID(request.chart),
          ownerSource: request.client,
          ownerId: request.user,
        },
        {
          $set: {
            content: request.content,
            name: request.name,
            resolution: request.resolution,
            symbol: request.symbol,
            lastModified: new Date(),
          },
        },
      );
    } catch (err) {
      Logger.error(err);
      status = StatusResponseEnum.ERROR.valueOf();
    }
    return {
      status: status,
    };
  }

  public async listChart(request: TradingViewListChartRequest): Promise<TradingViewListChartResponse> {
    const validator: Ajv.ValidateFunction = tradingViewListChartRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    let status: string = StatusResponseEnum.OK.valueOf();
    let data = [];
    try {
      data = await this.chartRepository.findByUserAndClient(`${request.headers.token.userId}`, request.client).map(convertFromChart).toArray();
    } catch (err) {
      Logger.error(err);
      status = StatusResponseEnum.ERROR.valueOf();
    }
    return {
      status: status,
      data: data,
    };
  }

  public async loadChart(request: TradingViewLoadChartRequest): Promise<TradingViewLoadChartResponse> {
    const validator: Ajv.ValidateFunction = tradingViewLoadChartRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    let status: string = StatusResponseEnum.OK.valueOf();
    let data: any;
    let message: string;
    try {
      const chart: IChart = await this.chartRepository.findByUserAndClientAndChartId(
        `${request.headers.token.userId}`,
        request.client,
        request.chart,
      );
      data = covertFromChartToChartLoadInfo(chart);
    } catch (err) {
      Logger.error(err);
      status = StatusResponseEnum.ERROR.valueOf();
      message = 'IChart not found';
    }
    return {
      status: status,
      data: data,
      message: message,
    };
  }

  public async deleteChart(request: TradingViewDeleteChartRequest): Promise<TradingViewDeleteChartResponse> {
    const validator: Ajv.ValidateFunction = tradingViewDeleteChartRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    let status: string = StatusResponseEnum.OK.valueOf();
    try {
      await this.chartRepository.deleteChart(`${request.headers.token.userId}`, request.client, request.chart);
    } catch (err) {
      Logger.error(err);
      status = StatusResponseEnum.ERROR.valueOf();
    }
    return {
      status: status,
    };
  }

  public async queryMarketLiquidity(request: MarketLiquidityRequest): Promise<MarketLiquidityResponse> {
    const validator: Ajv.ValidateFunction = marketLiquidityRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    if (request.dateList == null || request.dateList.length === 0) {
      request.dateList = [Utils.formatDateToDisplay(new Date())];
    }

    const response = {};

    //check if redis have data or not, if not, mean redis data have been saved to db, query db and return instead
    const redisSymbolQuoteMinuteList: ISymbolQuoteMinutes[] = await this.redisService.lrange(
      `${REDIS_KEY.SYMBOL_QUOTE_MINUTE}_${MARKET_INDEX_ENUM[request.market]}`,
      0,
      -1,
    );
    const isRedisStillHaveData: boolean = redisSymbolQuoteMinuteList.length > 0;

    for (const date of request.dateList) {
      const singleDate = date == null ? new Date() : Utils.convertStringToDate(date);

      //check if need to query realtime from redis
      if (Utils.compareDateOnly(singleDate, new Date()) === 0 && isRedisStillHaveData) {
        response[date] = ChartService.getMinuteLiquidityList(redisSymbolQuoteMinuteList);
      } else {
        response[date] = await this.querySingleDateMarketLiquidity(singleDate, request.market);
      }
    }
    return response;
  }

  private async querySingleDateMarketLiquidity(singleDate: Date, market: string): Promise<[string, number][]> {
    const allDayQuoteMinuteList: ISymbolQuoteMinutes[] = await this.symbolQuoteMinutesRepository
      .findBy(
        {
          code: MARKET_INDEX_ENUM[market],
          date: {
            $gt: Utils.getStartOfDate(singleDate),
            $lt: Utils.getEndOfDate(singleDate),
          },
        },
        Number.MAX_SAFE_INTEGER,
      )
      .toArray();

    return ChartService.getMinuteLiquidityList(allDayQuoteMinuteList);
  }

  private static getMinuteLiquidityList(quoteMinuteList: ISymbolQuoteMinutes[]): [string, number][] {
    const minuteLiquidityList = [];

    for (const quoteMinute of quoteMinuteList) {
      const minute = Utils.formatDateToDisplay(quoteMinute.date, 'hhmm');
      const minuteLiquidityPoint = [minute, quoteMinute.tradingValue];
      minuteLiquidityList.push(minuteLiquidityPoint);
    }
    return minuteLiquidityList.sort((a: [string, number], b: [string, number]) => {
      return a[0].localeCompare(b[0]);
    });
  }
}
