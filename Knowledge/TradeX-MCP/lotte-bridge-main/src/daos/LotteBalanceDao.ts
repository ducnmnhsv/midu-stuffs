import { Inject, Service } from 'typedi';
import LotteCommonDao from './LotteCommonDao';
import { IContext } from '../models/IContext';
import { ILotteProfitLossHistoryResponse } from '../models/response/lotte/ILotteProfitLossHistoryResponse';
import config from '../config';
import { ILotteStockBalanceResponse } from '../models/response/lotte/ILotteStockBalanceResponse';
import { ILotteBankListResponse } from '../models/response/lotte/ILotteBankListResponse';
import { ILotteWithdrawHistoryResponse } from '../models/response/lotte/ILotteWithdrawHistoryResponse';
import { ILotteWithdrawRequestResponse } from '../models/response/lotte/ILotteWithdrawRequestResponse';
import { ILotteWithdrawRequestRequest } from '../models/request/lotte/ILotteWithdrawRequestRequest';
import { ILotteTransferCashRequest } from '../models/request/lotte/ILotteTransferCashRequest';
import { ILotteTransferStockRequest } from '../models/request/lotte/ILotteTransferStockRequest';
import { ILotteTransferCashResponse } from '../models/response/lotte/ILotteTransferCashResponse';
import { ILotteTransferStockResponse } from '../models/response/lotte/ILotteTransferStockResponse';
import { ILotteTransferStockBalanceResponse } from '../models/response/lotte/ILotteTransferStockBalanceResponse';
import { ILotteTransferStockHistoryRequest } from '../models/request/lotte/ILotteTransferStockHistoryRequest';
import { ILotteTransferStockHistoryResponse } from '../models/response/lotte/ILotteTransferStockHistoryResponse';
import { ILotteTransferCashHistoryRequest } from '../models/request/lotte/ILotteTransferCashHistoryRequest';
import { ILotteTransferCashHistoryResponse } from '../models/response/lotte/ILotteTransferCashHistoryResponse';
import { ILotteProfitLossHistoryRequest } from '../models/request/lotte/ILotteProfitLossHistoryRequest';
import { ILotteStockBalanceRequest } from '../models/request/lotte/ILotteStockBalanceRequest';
import { ILotteBankListRequest } from '../models/request/lotte/ILotteBankListRequest';
import { ILotteWithdrawHistoryRequest } from '../models/request/lotte/ILotteWithdrawHistoryRequest';
import { ILotteCancelWithdrawRequest } from '../models/request/lotte/ILotteCancelWithdrawRequest';
import { ILotterCancelWithdrawResponse } from '../models/response/lotte/ILotteCancelWithdrawResponse';
import { ILotteTransferStockBalanceRequest } from '../models/request/lotte/ILotteTransferStockBalanceRequest';
import { ILotteRegisterLoanResponse } from '../models/response/lotte/ILotteRegisterLoanResponse';
import { ILotteRegisterLoanRequest } from '../models/request/lotte/ILotteRegisterLoanRequest';
import { ILotteLoanAvailableRequest } from '../models/request/lotte/ILotteLoanAvailableRequest';
import { ILotteLoanAvailableResponse } from '../models/response/lotte/ILotteLoanAvailableResponse';
import { ILotteLoanHistoryRequest } from '../models/request/lotte/ILotteLoanHistoryRequest';
import { ILotteLoanHistoryResponse } from '../models/response/lotte/ILotteLoanHistoryResponse';
import { ILotteLoanDetailRequest } from '../models/request/lotte/ILotteLoanDetailRequest';
import { ILotteLoanDetailResponse } from '../models/response/lotte/ILotteLoanDetailResponse';
import { ILotteRightAvailableRequest } from '../models/request/lotte/ILotteRightAvailableRequest';
import { ILotteRightAvailableResponse } from '../models/response/lotte/ILotteRightAvailableResponse';
import { ILotteRightDetailRequest } from '../models/request/lotte/ILotteRightDetailRequest';
import { ILotteRightDetailResponse } from '../models/response/lotte/ILotteRightDetailResponse';
import { ILotteRightHistoryResponse } from '../models/response/lotte/ILotteRightHistoryResponse';
import { ILotteRightHistoryRequest } from '../models/request/lotte/ILotteRightHistoryRequest';
import { ILotteRegisterRightRequest } from '../models/request/lotte/ILotteRegisterRightRequest';
import { ILotteRegisterRightResponse } from '../models/response/lotte/ILotteRegisterRightResponse';
import { ILotteCancelRightRequest } from '../models/request/lotte/ILotteCancelRightRequest';
import { ILotteCancelRightResponse } from '../models/response/lotte/ILotteCancelRightResponse';
import { ILotteAccountStkBalanceRequest } from '../models/request/lotte/ILotteAccountStkBalanceRequest';
import { ILotteAccountStkBalanceResponse } from '../models/response/lotte/ILotteAccountStkBalanceResponse';
import { ILotteLoanEstimatedFeeRequest } from '../models/request/lotte/ILotteLoanEstimatedFeeRequest';
import { ILotteLoanEstimatedFeeResponse } from '../models/response/lotte/ILotteLoanEstimatedFeeResponse';
import { ILotteTransferCashConfirmRequest } from '../models/request/lotte/ILotteTransferCashConfirmRequest';
import { ILotteTransferCashConfirmResponse } from '../models/response/lotte/ILotteTransferCashConfirmResponse';
import { ILotteGetTransferCashConfirmRequest } from '../models/request/lotte/ILotteGetTransferCashConfirmRequest';
import { ILotteLoanConfirmRequest } from '../models/request/lotte/ILotteLoanConfirmRequest';
import { ILotteLoanConfirmResponse } from '../models/response/lotte/ILotteLoanConfirmResponse';
import { ILotteGetLoanConfirmRequest } from '../models/request/lotte/ILotteGetLoanConfirmRequest';
import { ILotteGetLoanConfirmResponse } from '../models/response/lotte/ILotteGetLoanConfirmResponse';
import { ILotteGetTransferCashConfirmResponse } from '../models/response/lotte/ILotteGetTransferCashConfirmResponse';
import { ILotteTransferStockConfirmRequest } from '../models/request/lotte/ILotteTransferStockConfirmRequest';
import { ILotteTransferStockConfirmResponse } from '../models/response/lotte/ILotteTransferStockConfirmResponse';
import { ILotteGetTransferStockConfirmRequest } from '../models/request/lotte/ILotteGetTransferStockConfirmRequest';
import { ILotteGetTransferStockConfirmResponse } from '../models/response/lotte/ILotteGetTransferStockConfirmResponse';
import { ILotteSubListRequest } from '../models/request/lotte/ILotteSubListRequest';
import { ILotteSubListResponse } from '../models/response/lotte/ILotteSubListResponse';
import { ILotteQueryNavHistoryRequest } from '../models/request/lotte/ILotteQueryNavHistoryRequest';
import { ILotteQueryNavHistoryResponse } from '../models/response/lotte/ILotteQueryNavHistoryResponse';
import { ILotteRightHistoryOtherRequest, ILotteRightHistoryOtherResponse } from '../models/right-history/other';
import { ILotteRightHistoryIssueRequest, ILotteRightHistoryIssueResponse } from '../models/right-history/issue';
import {
  ILotteRightHistoryBonusSharesRequest,
  ILotteRightHistoryBonusSharesResponse,
} from '../models/right-history/bonus-shares';
import {
  ILotteRightHistoryDividendRequest,
  ILotteRightHistoryDividendResponse,
} from '../models/right-history/dividend';
import { ILotteRightHistoryBondRequest, ILotteRightHistoryBondResponse } from '../models/right-history/bond';
import {
  ILotteRightHistoryConversionRequest,
  ILotteRightHistoryConversionResponse,
} from '../models/right-history/conversion';
import {
  ILotteRightHistoryBondInterestRequest,
  ILotteRightHistoryBondInterestResponse,
} from '../models/right-history/bond-interest';
import { ILotteUpcomingRightRequest } from '../models/request/lotte/ILotteUpcomingRightRequest';
import { ILotteUpcomingRightResponse } from '../models/response/lotte/ILotteUpcomingRightResponse';

@Service()
export class LotteBalanceDao {
  @Inject()
  private lotteCommonDao: LotteCommonDao;

  getProfitLossHistory(
    request: ILotteProfitLossHistoryRequest,
    ctx: IContext
  ): Promise<ILotteProfitLossHistoryResponse> {
    return this.lotteCommonDao.post<ILotteProfitLossHistoryResponse>(
      config.lotte.apis.getProfitLossHistory,
      null,
      request,
      ctx
    );
  }

  getStockBalance(request: ILotteStockBalanceRequest, ctx: IContext): Promise<ILotteStockBalanceResponse> {
    return this.lotteCommonDao.get<ILotteStockBalanceResponse>(
      config.lotte.apis.getStockBalance,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getBankAccount(request: ILotteBankListRequest, ctx: IContext): Promise<ILotteBankListResponse> {
    return this.lotteCommonDao.get<ILotteBankListResponse>(
      config.lotte.apis.getBankList,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getWithdrawHistory(request: ILotteWithdrawHistoryRequest, ctx: IContext): Promise<ILotteWithdrawHistoryResponse> {
    return this.lotteCommonDao.get<ILotteWithdrawHistoryResponse>(
      config.lotte.apis.getWithdrawHistory,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  requestWithdraw(request: ILotteWithdrawRequestRequest, ctx: IContext): Promise<ILotteWithdrawRequestResponse> {
    return this.lotteCommonDao.post<ILotteWithdrawRequestResponse>(
      config.lotte.apis.requestWithdraw,
      null,
      request,
      ctx
    );
  }

  transferStock(request: ILotteTransferStockRequest, ctx: IContext): Promise<ILotteTransferStockResponse> {
    return this.lotteCommonDao.post<ILotteTransferStockResponse>(config.lotte.apis.transferStock, null, request, ctx);
  }

  tranferCash(request: ILotteTransferCashRequest, ctx: IContext): Promise<ILotteTransferCashResponse> {
    return this.lotteCommonDao.post<ILotteTransferCashResponse>(config.lotte.apis.transferCash, null, request, ctx);
  }

  getTransferStockBalance(
    request: ILotteTransferStockBalanceRequest,
    ctx: IContext
  ): Promise<ILotteTransferStockBalanceResponse> {
    return this.lotteCommonDao.post<ILotteTransferStockBalanceResponse>(
      config.lotte.apis.getTransferStockBalance,
      null,
      request,
      ctx
    );
  }

  getTransferStockHistory(
    request: ILotteTransferStockHistoryRequest,
    ctx: IContext
  ): Promise<ILotteTransferStockHistoryResponse> {
    return this.lotteCommonDao.post<ILotteTransferStockHistoryResponse>(
      config.lotte.apis.getTransferStockHistory,
      null,
      request,
      ctx
    );
  }

  getTransferCashHistory(
    request: ILotteTransferCashHistoryRequest,
    ctx: IContext
  ): Promise<ILotteTransferCashHistoryResponse> {
    return this.lotteCommonDao.get<ILotteTransferCashHistoryResponse>(
      config.lotte.apis.getTransferCashHistory,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  cancelWithdraw(request: ILotteCancelWithdrawRequest, ctx: IContext): Promise<ILotterCancelWithdrawResponse> {
    return this.lotteCommonDao.post<ILotterCancelWithdrawResponse>(
      config.lotte.apis.cancelWithdraw,
      null,
      request,
      ctx
    );
  }

  registerLoan(request: ILotteRegisterLoanRequest, ctx: IContext): Promise<ILotteRegisterLoanResponse> {
    return this.lotteCommonDao.post<ILotteRegisterLoanResponse>(config.lotte.apis.registerLoan, null, request, ctx);
  }

  loanAvailable(request: ILotteLoanAvailableRequest, ctx: IContext): Promise<ILotteLoanAvailableResponse> {
    return this.lotteCommonDao.post<ILotteLoanAvailableResponse>(
      config.lotte.apis.queryLoanAvailable,
      null,
      request,
      ctx
    );
  }

  loanHistory(request: ILotteLoanHistoryRequest, ctx: IContext): Promise<ILotteLoanHistoryResponse> {
    return this.lotteCommonDao.post<ILotteLoanHistoryResponse>(config.lotte.apis.queryLoanHistory, null, request, ctx);
  }

  loanDetail(request: ILotteLoanDetailRequest, ctx: IContext): Promise<ILotteLoanDetailResponse> {
    return this.lotteCommonDao.post<ILotteLoanDetailResponse>(config.lotte.apis.queryLoanDetail, null, request, ctx);
  }

  rightAvailable(request: ILotteRightAvailableRequest, ctx: IContext): Promise<ILotteRightAvailableResponse> {
    return this.lotteCommonDao.post<ILotteRightAvailableResponse>(
      config.lotte.apis.queryRightAvailable,
      null,
      request,
      ctx
    );
  }

  rightDetail(request: ILotteRightDetailRequest, ctx: IContext): Promise<ILotteRightDetailResponse> {
    return this.lotteCommonDao.post<ILotteRightDetailResponse>(config.lotte.apis.queryRightDetail, null, request, ctx);
  }

  rightHistory(request: ILotteRightHistoryRequest, ctx: IContext): Promise<ILotteRightHistoryResponse> {
    return this.lotteCommonDao.post<ILotteRightHistoryResponse>(
      config.lotte.apis.queryRightHistory,
      null,
      request,
      ctx
    );
  }

  rightHistoryOther(request: ILotteRightHistoryOtherRequest, ctx: IContext) {
    return this.lotteCommonDao.post<ILotteRightHistoryOtherResponse>(
      config.lotte.apis.queryRightHistoryOther,
      null,
      request,
      ctx
    );
  }

  rightHistoryIssue(request: ILotteRightHistoryIssueRequest, ctx: IContext) {
    return this.lotteCommonDao.post<ILotteRightHistoryIssueResponse>(
      config.lotte.apis.queryRightHistoryIssue,
      null,
      request,
      ctx
    );
  }

  rightHistoryBonusShares(request: ILotteRightHistoryBonusSharesRequest, ctx: IContext) {
    return this.lotteCommonDao.post<ILotteRightHistoryBonusSharesResponse>(
      config.lotte.apis.queryRightHistoryBonusShares,
      null,
      request,
      ctx
    );
  }

  rightHistoryDividend(request: ILotteRightHistoryDividendRequest, ctx: IContext) {
    return this.lotteCommonDao.post<ILotteRightHistoryDividendResponse>(
      config.lotte.apis.queryRightHistoryDividend,
      null,
      request,
      ctx
    );
  }

  rightHistoryBond(request: ILotteRightHistoryBondRequest, ctx: IContext) {
    return this.lotteCommonDao.post<ILotteRightHistoryBondResponse>(
      config.lotte.apis.queryRightHistoryBond,
      null,
      request,
      ctx
    );
  }

  rightHistoryConversion(request: ILotteRightHistoryConversionRequest, ctx: IContext) {
    return this.lotteCommonDao.post<ILotteRightHistoryConversionResponse>(
      config.lotte.apis.queryRightHistoryConversion,
      null,
      request,
      ctx
    );
  }

  rightHistoryBondInterest(request: ILotteRightHistoryBondInterestRequest, ctx: IContext) {
    return this.lotteCommonDao.post<ILotteRightHistoryBondInterestResponse>(
      config.lotte.apis.queryRightHistoryBondInterest,
      null,
      request,
      ctx
    );
  }

  upcomingRights(request: ILotteUpcomingRightRequest, ctx: IContext): Promise<ILotteUpcomingRightResponse> {
    return this.lotteCommonDao.post<ILotteUpcomingRightResponse>(
      config.lotte.apis.queryUpcomingRights,
      null,
      request,
      ctx
    );
  }

  registerRight(request: ILotteRegisterRightRequest, ctx: IContext): Promise<ILotteRegisterRightResponse> {
    return this.lotteCommonDao.post<ILotteRegisterRightResponse>(config.lotte.apis.registerRight, null, request, ctx);
  }

  cancelRight(request: ILotteCancelRightRequest, ctx: IContext): Promise<ILotteCancelRightResponse> {
    return this.lotteCommonDao.post<ILotteCancelRightResponse>(config.lotte.apis.cancelRight, null, request, ctx);
  }

  inquriyStockBalance(
    request: ILotteAccountStkBalanceRequest,
    ctx: IContext
  ): Promise<ILotteAccountStkBalanceResponse> {
    return this.lotteCommonDao.get<ILotteAccountStkBalanceResponse>(
      config.lotte.apis.inquriyStockBalance,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  loanEstimatedFee(request: ILotteLoanEstimatedFeeRequest, ctx: IContext): Promise<ILotteLoanEstimatedFeeResponse> {
    return this.lotteCommonDao.get<ILotteLoanEstimatedFeeResponse>(
      config.lotte.apis.loanEstimatedFee,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  transferStockConfirm(
    request: ILotteTransferStockConfirmRequest,
    ctx: IContext
  ): Promise<ILotteTransferStockConfirmResponse> {
    return this.lotteCommonDao.post<ILotteTransferStockConfirmResponse>(
      config.lotte.apis.stockTransferConfirm,
      null,
      request,
      ctx
    );
  }

  getTransferStockConfirm(
    request: ILotteGetTransferStockConfirmRequest,
    ctx: IContext
  ): Promise<ILotteGetTransferStockConfirmResponse> {
    return this.lotteCommonDao.post<ILotteGetTransferStockConfirmResponse>(
      config.lotte.apis.getStockTransferConfirm,
      null,
      request,
      ctx
    );
  }

  cashTransferConfirm(
    request: ILotteTransferCashConfirmRequest,
    ctx: IContext
  ): Promise<ILotteTransferCashConfirmResponse> {
    return this.lotteCommonDao.post<ILotteTransferCashConfirmResponse>(
      config.lotte.apis.cashTransferConfirm,
      null,
      request,
      ctx
    );
  }

  getCashTransferConfirm(
    request: ILotteGetTransferCashConfirmRequest,
    ctx: IContext
  ): Promise<ILotteGetTransferCashConfirmResponse> {
    return this.lotteCommonDao.post<ILotteGetTransferCashConfirmResponse>(
      config.lotte.apis.getCashTransferConfirm,
      null,
      request,
      ctx
    );
  }

  loanConfirm(request: ILotteLoanConfirmRequest, ctx: IContext): Promise<ILotteLoanConfirmResponse> {
    return this.lotteCommonDao.post<ILotteLoanConfirmResponse>(config.lotte.apis.loanConfirm, null, request, ctx);
  }

  getLoanConfirm(request: ILotteGetLoanConfirmRequest, ctx: IContext): Promise<ILotteGetLoanConfirmResponse> {
    return this.lotteCommonDao.post<ILotteGetLoanConfirmResponse>(config.lotte.apis.getLoanConfirm, null, request, ctx);
  }

  getSubList(request: ILotteSubListRequest, ctx: IContext): Promise<ILotteSubListResponse> {
    return this.lotteCommonDao.post<ILotteSubListResponse>(config.lotte.apis.subList, null, request, ctx);
  }

  queryNavHistory(request: ILotteQueryNavHistoryRequest, ctx: IContext): Promise<ILotteQueryNavHistoryResponse> {
    return this.lotteCommonDao.post<ILotteQueryNavHistoryResponse>(config.lotte.apis.navHistory, null, request, ctx);
  }
}
