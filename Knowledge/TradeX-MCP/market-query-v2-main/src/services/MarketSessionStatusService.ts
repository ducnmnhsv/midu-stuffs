import { Inject, Service } from 'typedi';
import { IMarketSessionStatus } from '../models/db/IMarketSessionStatus';
import * as Ajv from 'ajv';
import { Errors, Logger } from 'tradex-common';
import { INVALID_PARAMETER, MarketTypeEnum } from '../constants';
import { toMarketSessionStatusResponse } from '../utils/ResponseUtils';
import { MarketSessionStatusRequest, MarketSessionStatusResponse } from 'tradex-models-market';
import { marketSessionStatusRequestValidator } from 'tradex-models-market-validator';
import RedisService, { REDIS_KEY } from './RedisService';

@Service()
export default class MarketSessionStatusService {
  @Inject()
  private redisService: RedisService;

  public async queryMarketSessionStatus(request: MarketSessionStatusRequest): Promise<MarketSessionStatusResponse[]> {
    const validator: Ajv.ValidateFunction = marketSessionStatusRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    const marketSessionStatuses: IMarketSessionStatus[] = await this.redisService.hgetall(REDIS_KEY.MARKET_STATUS);
    Logger.info(`all status: ${JSON.stringify(marketSessionStatuses)}`);
    if (marketSessionStatuses.length < 1) {
      Logger.info(`redis empty data`);
      return [];
    }

    const response: MarketSessionStatusResponse[] = [];
    for (const value of marketSessionStatuses) {
      if (
        (request.market == null || request.market === MarketTypeEnum.ALL || value.market === request.market) &&
        (request.type == null || value.type === request.type)
      ) {
        response.push(toMarketSessionStatusResponse(value));
      }
    }
    return response;
  }
}
