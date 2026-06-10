import { Inject, Service } from 'typedi';
import LotteCommonDao from './LotteCommonDao';
import { IContext } from '../models/IContext';
import config from '../config';
import { ILotteStockRankingPeriodRequest } from '../models/request/lotte/ILotteStockRankingPeriodRequest';
import { ILotteStockRankingPeriodResponse } from '../models/response/lotte/ILotteStockRankingPeriodResponse';
import { ILotteMarketRightInfoRequest } from '../models/request/lotte/ILotteMarketRightInfoRequest';
import { ILotteMarketRightInfoResponse } from '../models/response/lotte/ILotteMarketRightInfoResponse';
import { ILotteOddlotLatestRequest } from '../models/request/lotte/ILotteOddlotLatestRequest';
import { ILotteOddlotLatestResponse } from '../models/response/lotte/ILotteOddlotLatestResponse';
import { ILotteMarketStockLatestRequest } from '../models/request/lotte/ILotteMarketStockLatestRequest';
import { ILotteMarketStockLatestResponse } from '../models/response/lotte/ILotteMarketStockLatestResponse';
import { ILotteMarketStockBidOfferRequest } from '../models/request/lotte/ILotteMarketStockBidOfferRequest';
import { ILotteMarketStockBidOfferResponse } from '../models/response/lotte/ILotteMarketStockBidOfferResponse';
import { ILotteMarketCwDetailRequest } from '../models/request/lotte/ILotteMarketCwDetailRequest';
import { ILotteMarketCwDetailResponse } from '../models/response/lotte/ILotteMarketCwDetailResponse';

@Service()
export class LotteMarketDao {
  @Inject()
  private lotteCommonDao: LotteCommonDao;

  getStockRankingPeriod(
    request: ILotteStockRankingPeriodRequest,
    ctx: IContext
  ): Promise<ILotteStockRankingPeriodResponse> {
    return this.lotteCommonDao.get<ILotteStockRankingPeriodResponse>(
      config.lotte.apis.getStockRankingPeriod,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getMarketRightInfo(request: ILotteMarketRightInfoRequest, ctx: IContext): Promise<ILotteMarketRightInfoResponse> {
    return this.lotteCommonDao.get<ILotteMarketRightInfoResponse>(
      config.lotte.apis.getMarketRightInfo,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getOddlotLatest(request: ILotteOddlotLatestRequest, ctx: IContext): Promise<ILotteOddlotLatestResponse> {
    return this.lotteCommonDao.get<ILotteOddlotLatestResponse>(
      config.lotte.apis.getOddlotLatest,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getMarketStockLatest(
    request: ILotteMarketStockLatestRequest,
    ctx: IContext
  ): Promise<ILotteMarketStockLatestResponse> {
    return this.lotteCommonDao.get<ILotteMarketStockLatestResponse>(
      config.lotte.apis.getMarketStockLatest,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getMarketStockBidOffer(
    request: ILotteMarketStockBidOfferRequest,
    ctx: IContext
  ): Promise<ILotteMarketStockBidOfferResponse> {
    return this.lotteCommonDao.get<ILotteMarketStockBidOfferResponse>(
      config.lotte.apis.getMarketStockBidOffer,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getMarketCwDetail(request: ILotteMarketCwDetailRequest, ctx: IContext): Promise<ILotteMarketCwDetailResponse> {
    return this.lotteCommonDao.post<ILotteMarketCwDetailResponse>(
      config.lotte.apis.getMarketCwDetail,
      null,
      request,
      ctx
    );
  }
}
