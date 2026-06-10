import { Inject, Service } from 'typedi';
import LotteCommonDao from './LotteCommonDao';
import { IContext } from '../models/IContext';
import { ILotteAccountInfoResponse } from '../models/response/lotte/ILotteAccountInfoResponse';
import config from '../config';
import { ILotteAssetInfoResponse } from '../models/response/lotte/ILotteAssetInfoResponse';
import { ILotteBuyableResponse } from '../models/response/lotte/ILotteBuyableResponse';
import { ILotteCashBalanceResponse } from '../models/response/lotte/ILotteCashBalanceResponse';
import { ILotteSellableResponse } from '../models/response/lotte/ILotteSellableResponse';
import { ILotteAccountInfoRequest } from '../models/request/lotte/ILotteAccountInfoRequest';
import { ILotteAssetInfoRequest } from '../models/request/lotte/ILotteAssetInfoRequest';
import { ILotteBuyableRequest } from '../models/request/lotte/ILotteBuyableRequest';
import { ILotteCashBalanceRequest } from '../models/request/lotte/ILotteCashBalanceRequest';
import { ILotteSellableRequest } from '../models/request/lotte/ILotteSellableRequest';
import { ILotteAccountLoanHistoryRequest } from '../models/request/lotte/ILotteAccountLoanHistoryRequest';
import { ILotteAccountLoanHistoryResponse } from '../models/response/lotte/ILotteAccountLoanHistoryResponse';
import { ILotteAccountBalanceRetrieveRequest } from '../models/request/lotte/ILotteAccountBalanceRetrieveRequest';
import { ILotteAccountBalanceRetrieveResponse } from '../models/response/lotte/ILotteAccountBalanceRetrieveResponse';
import { ILotteMarginAccountRequest } from '../models/request/lotte/ILotteMarginAccountRequest';
import { ILotteMarginAccountResponse } from '../models/response/lotte/ILotteMarginAccountResponse';
import { ILotteVsdStatusAccountRequest } from '../models/request/lotte/ILotteVsdStatusAccountRequest';
import { ILotteVsdStatusAccountResponse } from '../models/response/lotte/ILotteVsdStatusAccountResponse';
import { ILotteNotificationStatusResponse } from '../models/response/lotte/ILotteNotificationStatusResponse';
import { ILotteAccountContractStatusRequest } from '../models/request/lotte/ILotteAccountContractStatusRequest';
import { ILotteNotificationStatusRequest } from '../models/request/lotte/ILotteNotificationStatusRequest';
import { ILotteContractStatusResponse } from '../models/response/lotte/ILotteContractStatusResponse';
import { ILotteEstAssetLoanInfoRequest } from '../models/request/lotte/ILotteEstAssetLoanInfoRequest';
import { ILotteEstAssetLoanInfoResponse } from '../models/response/lotte/ILotteEstAssetLoanInfoResponse';
import { ILotteCashDepositHistoryRequest } from '../models/request/lotte/ILotteCashDepositHistoryRequest';
import { ILotteCashDepositHistoryResponse } from '../models/response/lotte/ILotteCashDepositHistoryResponse';

@Service()
export class LotteAccountDao {
  @Inject()
  private lotteCommonDao: LotteCommonDao;

  getAccountInfo(request: ILotteAccountInfoRequest, ctx: IContext): Promise<ILotteAccountInfoResponse> {
    return this.lotteCommonDao.get<ILotteAccountInfoResponse>(
      config.lotte.apis.getAccountInfo,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getAssetInfo(request: ILotteAssetInfoRequest, ctx: IContext): Promise<ILotteAssetInfoResponse> {
    return this.lotteCommonDao.get<ILotteAssetInfoResponse>(
      config.lotte.apis.getAssetInfo,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getBuyable(request: ILotteBuyableRequest, ctx: IContext): Promise<ILotteBuyableResponse> {
    return this.lotteCommonDao.post<ILotteBuyableResponse>(config.lotte.apis.getBuyable, null, request, ctx);
  }

  getCashBalance(request: ILotteCashBalanceRequest, ctx: IContext): Promise<ILotteCashBalanceResponse> {
    return this.lotteCommonDao.post<ILotteCashBalanceResponse>(config.lotte.apis.getCashBalance, null, request, ctx);
  }

  getLoanHistory(request: ILotteAccountLoanHistoryRequest, ctx: IContext): Promise<ILotteAccountLoanHistoryResponse> {
    return this.lotteCommonDao.post<ILotteAccountLoanHistoryResponse>(
      config.lotte.apis.getAccountLoanHistory,
      null,
      request,
      ctx
    );
  }

  getCashDepositHistory(request: ILotteCashDepositHistoryRequest, ctx: IContext): Promise<ILotteCashDepositHistoryResponse> {
    return this.lotteCommonDao.post<ILotteCashDepositHistoryResponse>(
      config.lotte.apis.getCashDepositHistory,
      null,
      request,
      ctx
    );
  }

  getSellable(request: ILotteSellableRequest, ctx: IContext): Promise<ILotteSellableResponse> {
    return this.lotteCommonDao.post<ILotteSellableResponse>(config.lotte.apis.getSellable, null, request, ctx);
  }

  balanceRetrieving(
    request: ILotteAccountBalanceRetrieveRequest,
    ctx: IContext
  ): Promise<ILotteAccountBalanceRetrieveResponse> {
    return this.lotteCommonDao.post<ILotteAccountBalanceRetrieveResponse>(
      config.lotte.apis.balanceRetrieving,
      null,
      request,
      ctx
    );
  }

  marginRatio(request: ILotteMarginAccountRequest, ctx: IContext): Promise<ILotteMarginAccountResponse> {
    return this.lotteCommonDao.get<ILotteMarginAccountResponse>(
      config.lotte.apis.marginRatio,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  vsdStatus(request: ILotteVsdStatusAccountRequest, ctx: IContext): Promise<ILotteVsdStatusAccountResponse> {
    return this.lotteCommonDao.get<ILotteVsdStatusAccountResponse>(
      config.lotte.apis.getVsdStatus,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  contractStatus(request: ILotteAccountContractStatusRequest, ctx: IContext): Promise<ILotteContractStatusResponse> {
    return this.lotteCommonDao.post<ILotteContractStatusResponse>(
      config.lotte.apis.getContractStatus,
      null,
      request,
      ctx
    );
  }

  updateNotificationStatus(
    request: ILotteNotificationStatusRequest,
    ctx: IContext
  ): Promise<ILotteNotificationStatusResponse> {
    return this.lotteCommonDao.post<ILotteNotificationStatusResponse>(
      config.lotte.apis.postNotificationStatus,
      null,
      request,
      ctx
    );
  }

  getNotificationStatus(
    request: ILotteNotificationStatusRequest,
    ctx: IContext
  ): Promise<ILotteNotificationStatusResponse> {
    return this.lotteCommonDao.post<ILotteNotificationStatusResponse>(
      config.lotte.apis.getNotificationStatus,
      null,
      request,
      ctx
    );
  }

  getEstAssetLoanInfo(request: ILotteEstAssetLoanInfoRequest, ctx: IContext): Promise<ILotteEstAssetLoanInfoResponse> {
    return this.lotteCommonDao.post<ILotteEstAssetLoanInfoResponse>(
      config.lotte.apis.inquiryMarginRate,
      null,
      request,
      ctx
    );
  }
}
