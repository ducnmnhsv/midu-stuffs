import { Inject, Service } from 'typedi';
import { ISymbolInfo } from '../models/db/ISymbolInfo';
import RedisService, { REDIS_KEY } from './RedisService';
import { SecuritiesTypeEnum } from '../constants';
import IMinuteChartResponse from '../models/response/IMinuteChartResponse';

@Service()
export default class CacheService {
  @Inject()
  private readonly redisService: RedisService;

  private futuresInfoSummaryMap: Map<string, ISymbolInfo>; // container futures 1M, 2M, 1Q, 2Q, 3Q

  public cacheMinuteChart: Map<string, Promise<IMinuteChartResponse>>;

  public async init() {
    this.futuresInfoSummaryMap = new Map<string, ISymbolInfo>();
    this.cacheMinuteChart = new Map<string, Promise<IMinuteChartResponse>>();
    const symbolInfoList: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    for (let i = 0; i < symbolInfoList.length; i++) {
      const symbolInfo: ISymbolInfo = symbolInfoList[i];
      if (symbolInfo.type === SecuritiesTypeEnum.FUTURES) {
        const futuresInfoSummary: ISymbolInfo = {
          _id: symbolInfo.refCode,
          code: symbolInfo.refCode,
          refCode: symbolInfo.refCode,
          type: symbolInfo.type,
          name: symbolInfo.refCode,
          nameEn: symbolInfo.refCode,
          marketType: symbolInfo.marketType,
        };
        this.futuresInfoSummaryMap.set(futuresInfoSummary.code, futuresInfoSummary);
      }
    }
  }

  public async getAllSymbolInfo(): Promise<ISymbolInfo[]> {
    const symbolInfoList: ISymbolInfo[] = await this.redisService.hgetall<ISymbolInfo>(REDIS_KEY.SYMBOL_INFO);
    return symbolInfoList.concat(Array.from(this.futuresInfoSummaryMap.values()));
  }

  public async getSymbolInfo(code: string): Promise<ISymbolInfo> {
    let symbolInfo: ISymbolInfo = await this.redisService.getSymbolInfo(code);
    if (symbolInfo == null) {
      symbolInfo = this.futuresInfoSummaryMap.get(code);
    }
    return symbolInfo;
  }
}
