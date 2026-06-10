import { Inject, Service } from 'typedi';
import { IContext } from '../models/IContext';
import LotteCommonDao from './LotteCommonDao';
import config from '../config';
import { ILotteAdvancedOrderRequest, ILotteNornalOrderRequest } from '../models/request/lotte/ILotteEnterOrderRequest';
import { ILotteModifyOrderRequest } from '../models/request/lotte/ILotteModifyOrderRequest';
import {
  ILotteHistoryOrderAdvancedRequest,
  ILotteHistoryOrderRequest,
} from '../models/request/lotte/ILotteHistoryOrderRequest';
import {
  ILotteHistoryOrderAdvancedResponse,
  ILotteHistoryOrderResponse,
} from '../models/response/lotte/ILotteHistoryOrderResponse';
import { ILotteTodayUnmatchOrderRequest } from '../models/request/lotte/ILotteTodayUnmatchOrderRequest';
import { ILotteTodayUnmatchOrderResponse } from '../models/response/lotte/ILotteTodayUnmatchOrderResponse';
import {
  ILotteOrderConfirmHistoryRequest,
  ILotteOrderConfirmRequest,
} from '../models/request/lotte/ILotteOrderConfirmRequest';
import {
  ILotteOrderConfirmHistoryResponse,
  ILotteOrderConfirmResponse,
} from '../models/response/lotte/ILotteOrderConfirmResponse';
import {
  ILotteAdvancedOrderResponse,
  ILotteNormalOrderResponse,
} from '../models/response/lotte/ILotteEnterOrderResponse';
import {
  ILotteCancelOrderAdvancedRequest,
  ILotteCancelOrderNormalRequest,
} from '../models/request/lotte/ILotteCancelOrderRequest';
import {
  ILotteCancelOrderAdvancedResponse,
  ILotteCancelOrderNormalResponse,
} from '../models/response/lotte/ILotteCancelOrderResponse';
import { ILotteModifyOrderNormalResponse } from '../models/response/lotte/ILotteModifyNormalOrderResponse';
import { ILotteOrderBookRequest } from '../models/request/lotte/ILotteOrderBookRequest';
import { ILotteOrderBookResponse } from '../models/response/lotte/ILotteOrderBookResponse';

@Service()
export class LotteOrderDao {
  @Inject()
  private lotteCommonDao: LotteCommonDao;

  enterBuyNormalOrder(request: ILotteNornalOrderRequest, ctx: IContext): Promise<ILotteNormalOrderResponse> {
    return this.lotteCommonDao.post<ILotteNormalOrderResponse>(config.lotte.apis.buyNormalOrder, null, request, ctx);
  }

  enterSellNormalOrder(request: ILotteNornalOrderRequest, ctx: IContext): Promise<ILotteNormalOrderResponse> {
    return this.lotteCommonDao.post<ILotteNormalOrderResponse>(config.lotte.apis.sellNormalOrder, null, request, ctx);
  }

  cancelNormalOrder(request: ILotteCancelOrderNormalRequest, ctx: IContext): Promise<ILotteCancelOrderNormalResponse> {
    return this.lotteCommonDao.post<ILotteCancelOrderNormalResponse>(
      config.lotte.apis.cancelNormalOrder,
      null,
      request,
      ctx
    );
  }

  modifyNormalOrder(request: ILotteModifyOrderRequest, ctx: IContext): Promise<ILotteModifyOrderNormalResponse> {
    return this.lotteCommonDao.post<ILotteModifyOrderNormalResponse>(
      config.lotte.apis.modifyNormalOrder,
      null,
      request,
      ctx
    );
  }

  getHistoryOrder(request: ILotteHistoryOrderRequest, ctx: IContext): Promise<ILotteHistoryOrderResponse> {
    return this.lotteCommonDao.get<ILotteHistoryOrderResponse>(
      config.lotte.apis.getHistoryOrder,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getTodayUnmatchOrder(
    request: ILotteTodayUnmatchOrderRequest,
    ctx: IContext
  ): Promise<ILotteTodayUnmatchOrderResponse> {
    return this.lotteCommonDao.get<ILotteTodayUnmatchOrderResponse>(
      config.lotte.apis.getTodayUnmatchOrder,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  confirmOrder(request: ILotteOrderConfirmRequest, ctx: IContext): Promise<ILotteOrderConfirmResponse> {
    return this.lotteCommonDao.post<ILotteOrderConfirmResponse>(config.lotte.apis.confirmOrder, null, request, ctx);
  }

  searchOrderConfirm(
    request: ILotteOrderConfirmHistoryRequest,
    ctx: IContext
  ): Promise<ILotteOrderConfirmHistoryResponse> {
    return this.lotteCommonDao.get<ILotteOrderConfirmHistoryResponse>(
      config.lotte.apis.searchOrderConfirm,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  enterBuyAdvancedOrder(request: ILotteAdvancedOrderRequest, ctx: IContext): Promise<ILotteAdvancedOrderResponse> {
    return this.lotteCommonDao.post<ILotteAdvancedOrderResponse>(config.lotte.apis.buyAdvanceOrder, null, request, ctx);
  }

  enterSellAdvancedOrder(request: ILotteAdvancedOrderRequest, ctx: IContext): Promise<ILotteAdvancedOrderResponse> {
    return this.lotteCommonDao.post<ILotteAdvancedOrderResponse>(
      config.lotte.apis.sellAdvanceOrder,
      null,
      request,
      ctx
    );
  }

  cancelAdvancedOrder(
    request: ILotteCancelOrderAdvancedRequest,
    ctx: IContext
  ): Promise<ILotteCancelOrderAdvancedResponse> {
    return this.lotteCommonDao.post<ILotteCancelOrderAdvancedResponse>(
      config.lotte.apis.cancelAdvanceOrder,
      null,
      request,
      ctx
    );
  }

  getHistoryOrderAdvanced(
    request: ILotteHistoryOrderAdvancedRequest,
    ctx: IContext
  ): Promise<ILotteHistoryOrderAdvancedResponse> {
    return this.lotteCommonDao.get<ILotteHistoryOrderAdvancedResponse>(
      config.lotte.apis.getHistoryAdvanceOrder,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getOrderBook(request: ILotteOrderBookRequest, ctx: IContext): Promise<ILotteOrderBookResponse> {
    return this.lotteCommonDao.get<ILotteOrderBookResponse>(
      config.lotte.apis.orderBook,
      null,
      request,
      null,
      null,
      ctx
    );
  }
}
