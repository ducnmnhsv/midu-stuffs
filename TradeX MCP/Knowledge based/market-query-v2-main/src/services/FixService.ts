import { Inject, Service } from 'typedi';
import { Errors, Utils } from 'tradex-common';
import { DEFAULT_PAGE_SIZE, INVALID_PARAMETER } from '../constants';
import { FixSecurityListQueryRequest, FixSecurityListQueryResponse } from 'tradex-models-market';
import { fixSecurityListQueryRequestValidator } from 'tradex-models-market-validator';
import * as Ajv from 'ajv';
import { ISymbolInfo } from '../models/db/ISymbolInfo';
import { parseFromSymbolInfoToFixSymbol } from '../utils/ResponseUtils';
import RedisService, { REDIS_KEY } from './RedisService';

@Service()
export default class FixService {
  @Inject()
  private readonly redisService: RedisService;

  public async queryFixSymbolList(request: FixSecurityListQueryRequest): Promise<FixSecurityListQueryResponse[]> {
    const validator: Ajv.ValidateFunction = fixSecurityListQueryRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    if (request.fetchCount == null || request.fetchCount <= 0) {
      request.fetchCount = DEFAULT_PAGE_SIZE;
    }
    const symbolInfoList: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    if (symbolInfoList == null) {
      return [];
    } else if (symbolInfoList.length < 1) {
      return [];
    }

    const sortedList: ISymbolInfo[] = [];
    let limit: number = 0;
    for (const symbolInfo of symbolInfoList) {
      if (symbolInfo.code > request.instrumentCode) {
        if (request.lastUpdatedTime != null && request.lastUpdatedTime !== '') {
          if (symbolInfo.updatedAt > Utils.convertStringToDate(request.lastUpdatedTime, Utils.DATETIME_DISPLAY_FORMAT)) {
            sortedList.push(symbolInfo);
          }
        } else {
          sortedList.push(symbolInfo);
        }
      }
      limit++;
      if (limit === request.fetchCount) {
        break;
      }
    }
    return sortedList.map(parseFromSymbolInfoToFixSymbol);
  }
}
