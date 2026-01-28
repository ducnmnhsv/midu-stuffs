import { Inject, Service } from 'typedi';
import { AdvertiseDataRepository } from '../repositories/AdvertiseDataRepository';
import { DEFAULT_OFFSET, DEFAULT_PAGE_SIZE, MarketTypeEnum, INVALID_PARAMETER } from '../constants';
import { Errors, Logger } from 'tradex-common';
import { IAdvertiseData } from '../models/db/IAdvertiseData';
import { IDealNoticeData as DealNoticeData } from '../models/db/IDealNoticeData';
import * as Ajv from 'ajv';
import { toPutthroughDealResponse, toPutthroughAdvertiseResponse } from '../utils/ResponseUtils';
import { PutthroughAdvertiseRequest, PutthroughDealRequest } from 'tradex-models-market';
import { putthroughAdvertiseRequestValidator, putthroughDealRequestValidator } from 'tradex-models-market-validator';
import RedisService, { REDIS_KEY } from './RedisService';
import { IPutthroughDealResponse } from '../models/response/IPutthroughDealResponse';
import { IPutthroughAdvertiseResponse } from '../models/response/IPutthroughAdvertiseResponse';

@Service()
export default class PutThroughService {
  @Inject()
  public redisService: RedisService;
  @Inject()
  public advertiseDataRepository: AdvertiseDataRepository;

  public async queryPutThroughAdvertise(request: PutthroughAdvertiseRequest): Promise<IPutthroughAdvertiseResponse[]> {
    const validator: Ajv.ValidateFunction = putthroughAdvertiseRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const fetchCount = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const offset = request.offset != null ? request.offset : DEFAULT_OFFSET;

    let advertiseList: IAdvertiseData[] = [];
    if (request.marketType == null || request.marketType === MarketTypeEnum.ALL) {
      const upcom: IAdvertiseData[] = await this.redisService.lrange(`${REDIS_KEY.ADVERTISED}_${MarketTypeEnum.UPCOM}`, 0, -1);
      const hnx: IAdvertiseData[] = await this.redisService.lrange(`${REDIS_KEY.ADVERTISED}_${MarketTypeEnum.HNX}`, 0, -1);
      const hose: IAdvertiseData[] = await this.redisService.lrange(`${REDIS_KEY.ADVERTISED}_${MarketTypeEnum.HOSE}`, 0, -1);
      advertiseList = upcom.concat(hnx, hose);
    } else {
      advertiseList = await this.redisService.lrange(`${REDIS_KEY.ADVERTISED}_${request.marketType}`, 0, -1);
    }

    if (advertiseList.length < 1) {
      Logger.info(`redis empty data`);
      return [];
    }

    const response: IAdvertiseData[] = [];
    let limit: number = 0;
    let skip: number = 0;
    for (const value of advertiseList) {
      if (
        (request.market == null || request.market === MarketTypeEnum.ALL || value.marketType === request.market) &&
        (request.sellBuyType == null || value.sellBuyType === request.sellBuyType)
      ) {
        skip++;
        if (skip < offset) {
          continue;
        }
        response.push(value);
        limit++;
        if (limit === fetchCount) {
          break;
        }
      }
    }
    return response.map(toPutthroughAdvertiseResponse);
  }

  public async queryPutThroughDeal(request: PutthroughDealRequest): Promise<IPutthroughDealResponse[]> {
    const validator: Ajv.ValidateFunction = putthroughDealRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const fetchCount = request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const offset = request.offset != null ? request.offset : DEFAULT_OFFSET;
    let dealNoticeList: DealNoticeData[] = [];
    if (request.marketType == null || request.marketType === MarketTypeEnum.ALL) {
      const upcom: DealNoticeData[] = await this.redisService.lrange(`${REDIS_KEY.DEAL_NOTICE}_${MarketTypeEnum.UPCOM}`, 0, -1);
      const hnx: DealNoticeData[] = await this.redisService.lrange(`${REDIS_KEY.DEAL_NOTICE}_${MarketTypeEnum.HNX}`, 0, -1);
      const hose: DealNoticeData[] = await this.redisService.lrange(`${REDIS_KEY.DEAL_NOTICE}_${MarketTypeEnum.HOSE}`, 0, -1);
      dealNoticeList = upcom.concat(hnx, hose);
    } else {
      dealNoticeList = await this.redisService.lrange(`${REDIS_KEY.DEAL_NOTICE}_${request.marketType}`, 0, -1);
    }

    if (dealNoticeList.length < 1) {
      Logger.info(`redis empty data`);
      return [];
    }

    const response: DealNoticeData[] = [];
    let limit: number = 0;
    let skip: number = 0;
    for (const value of dealNoticeList) {
      if (request.market == null || request.market === MarketTypeEnum.ALL || value.marketType === request.market) {
        skip++;
        if (skip < offset) {
          continue;
        }
        response.push(value);
        limit++;
        if (limit === fetchCount) {
          break;
        }
      }
    }
    return response.map(toPutthroughDealResponse);
  }
}
