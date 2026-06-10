import { Errors, Kafka, Logger, Utils } from 'tradex-common';
import { Inject, Service } from 'typedi';
import config from '../config';
import { IContext } from '../models/IContext';
import { AuthenticationService } from '../services/AuthenticationService';
import { AccountService } from '../services/AccountService';
import { OrderService } from '../services/OrderService';
import { NotificationService } from '../services/NotificationService';
import { BalanceService } from '../services/BalanceService';
import { TransferService } from '../services/TransferService';
import { LoanService } from '../services/LoanServices';
import { RightService } from '../services/RightService';
import { EkycService } from '../services/EkycService';
import { MarketService } from '../services/MarketService';
import { OddLotService } from '../services/OddLotService';
import { InjectRepository } from 'typeorm-typedi-extensions';
import { HeaderTokenUserDataRepository } from '../repositories/HeaderTokenUserDataRepository';
import { HeaderTokenUserData } from '../models/db/HeaderTokenUserData';
import { RightHistoryService } from '../services/right-history/RightHistoryService';
import { parseRightTypeFromUri } from '../services/right-history/types/RightHistoryType';

@Service()
export default class RequestHandler {
  @Inject()
  private authenticationService: AuthenticationService;
  @Inject()
  private accountService: AccountService;
  @Inject()
  private orderService: OrderService;
  @Inject()
  private notificationService: NotificationService;
  @Inject()
  private balanceService: BalanceService;
  @Inject()
  private transferService: TransferService;
  @Inject()
  private loanService: LoanService;
  @Inject()
  private rightService: RightService;
  @Inject()
  private ekycService: EkycService;
  @Inject()
  private marketService: MarketService;
  @Inject()
  private oddLotService: OddLotService;
  @Inject()
  private rightHistoryService: RightHistoryService;
  @InjectRepository()
  private headerTokenUserDataRepository: HeaderTokenUserDataRepository;

  init() {
    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    /* tslint:disable:no-unused-expression */
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      [config.clusterId],
      (message: Kafka.IKafkaMessage) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions
    );
  }

  private handleRequest: Kafka.Handle = async (message: Kafka.IMessage) => {
    if (message == null || message.data == null) {
      return Promise.reject(new Errors.SystemError());
    }
    const ctx: IContext = {
      id: message.msgHandlerUniqueId || `${message.transactionId}_${message.messageId}`,
      txId: `${message.transactionId}`,
      orgMsg: message,
    };
    await this.addHeadersForClientCredentials(message, ctx);
    if (message.uri === 'post:/api/v1/lotte/login') {
      return this.authenticationService.authenticate(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/login/otp') {
      return this.authenticationService.authenticateOtp(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/login/otp') {
      return this.authenticationService.getAuthenticateOtpInfo(message.data, ctx);
    } else if (
      message.uri === 'post:/api/v1/lotte/login/sec/verifyOTP' ||
      message.uri === 'post:/api/v1/login/otp/verify' ||
      message.uri === 'post:/api/v1/lotte/login/verify'
    ) {
      return this.authenticationService.verifyOTP(message.data, ctx);
    } else if (message.uri === 'put:/api/v1/lotte/equity/account/changePassword') {
      return this.authenticationService.changePassword(message.data, ctx);
    } else if (message.uri === 'put:/api/v1/lotte/equity/account/changePin') {
      return this.authenticationService.changePin(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/account/resetPassword/init') {
      return this.authenticationService.resetPasswordInit(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/account/resetPassword/verifyOtp') {
      return this.authenticationService.resetPasswordVerifyOtp(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/account/resetPassword') {
      return this.authenticationService.resetPassword(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/info') {
      return this.accountService.getAccountInfo(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/assetInfo') {
      return this.accountService.getAssetInfo(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/profitLoss') {
      return this.accountService.getAccountProfitLoss(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/buyable') {
      return this.accountService.getBuyable(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/cashBalance') {
      return this.accountService.getCashBalance(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/loanHistory') {
      return this.accountService.getLoanHistory(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/sellable') {
      return this.accountService.getSellable(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/order') {
      return this.orderService.enterNormalOrder(message.data, ctx);
    } else if (message.uri === 'put:/api/v1/lotte/equity/order/cancel') {
      return this.orderService.cancelNormalOrder(message.data, ctx);
    } else if (message.uri === 'put:/api/v1/lotte/equity/order/modify') {
      return this.orderService.modifyNormalOrder(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/order/history') {
      return this.orderService.getHistoryOrder(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/order/todayUnmatch') {
      return this.orderService.getTodayUnmatchOrder(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/order/confirm') {
      return this.orderService.confirmOrder(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/order/confirm') {
      return this.orderService.searchOrderConfirm(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/order/advance') {
      return this.orderService.enterAdvanceOrder(message.data, ctx);
    } else if (message.uri === 'put:/api/v1/lotte/equity/order/advance/cancel') {
      return this.orderService.cancelAdvanceOrder(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/order/advance/history') {
      return this.orderService.getHistoryOrderAdvanced(message.data, ctx);
    } else if (message.uri === '/api/v1/equity/account/notification') {
      return this.notificationService.getNotification(message.data, ctx);
    } else if (message.uri === '/api/v1/equity/account/notification/maintenance') {
      return this.notificationService.getMaintenanceNotfication(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/profitLoss/history') {
      return this.balanceService.getProfitLossHistory(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/stockBalance') {
      return this.balanceService.getStockBalance(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/banks') {
      return this.balanceService.getAccountBanks(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/withdraw/banks') {
      return this.balanceService.getWithdrawBanks(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/withdraw/history') {
      return this.balanceService.getWithdrawHistory(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/withdraw/request') {
      return this.balanceService.requestWithdraw(message.data, ctx);
    } else if (message.uri === 'put:/api/v1/lotte/equity/withdraw/cancel') {
      return this.balanceService.cancelWithdraw(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/transfer/stock') {
      return this.transferService.transferStock(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/transfer/stock/balance') {
      return this.transferService.getTransferStockBalance(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/transfer/stock/history') {
      return this.transferService.getTransferStockHistory(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/transfer/cash') {
      return this.transferService.transferCash(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/transfer/cash/history') {
      return this.transferService.getTransferCashHistory(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/loan/register') {
      return this.loanService.registerLoan(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/loan/available') {
      return this.loanService.loanAvailable(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/loan/history') {
      return this.loanService.loanHistory(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/loan/detail') {
      return this.loanService.loanDetail(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/rights/available') {
      return this.rightService.rightAvailable(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/rights/detail') {
      return this.rightService.rightDetail(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/rights/history') {
      return this.rightService.rightHistory(message.data, ctx);
    } else if (message.uri.startsWith('get:/api/v1/lotte/equity/rights/history/')) {
      const rightType = parseRightTypeFromUri(message.uri);
      if (rightType) {
        return this.rightHistoryService.getRightHistory(message.data, rightType, ctx);
      }
    } else if (message.uri === 'get:/api/v1/equity/rights/upcoming') {
      return this.rightService.upcomingRights(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/rights/register') {
      return this.rightService.registerRight(message.data, ctx);
    } else if (message.uri === 'put:/api/v1/lotte/equity/rights/cancel') {
      return this.rightService.cancelRight(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/ekycs/banks') {
      return this.ekycService.getBankList(ctx);
    } else if (message.uri === 'get:/api/v1/ekycs/branch') {
      return this.ekycService.getListBranch(ctx);
    } else if (message.uri === 'get:/api/v1/ekycs/banks/{id}/branches') {
      return this.ekycService.getBanksListBranch(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/equity/account/checkNationalId') {
      return this.ekycService.checkNationalId(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/margin') {
      return this.accountService.getMarginAccount(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/ekycs/partner') {
      return this.ekycService.getPartnerName(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/vsdStatus') {
      return this.accountService.getAccountVsdStatus(message.data, ctx);
    } else if (message.uri === 'get:/api/v2/market/stock/ranking/period') {
      return this.marketService.getStockRankingPeriod(message.data, ctx);
    } else if (message.uri === 'get:/api/v2/market/symbol/{symbol}/right') {
      return this.marketService.getMarketRightInfo(message.data, ctx);
    } else if (message.uri === 'get:/api/v2/market/cw/{symbol}/detail') {
      return this.marketService.getMarketCwDetail(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/ekycs/account/exist') {
      return this.ekycService.checkAccountOpeningStatus(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/contractStatus') {
      return this.accountService.getAccountContractStatus(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/lotte/equity/account/notification/settings') {
      return this.accountService.updateNotificationStatus(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/notification/settings') {
      return this.accountService.getNotificationStatus(message.data, ctx);
    } else if (message.uri === 'get:/api/v2/market/symbol/oddlotLatest') {
      return this.oddLotService.getOddlotLatest(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/loan/estimatedFee') {
      return this.loanService.loanEstimatedFee(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/equity/transfer/stock/confirm') {
      return this.transferService.transferStockConfirm(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/equity/transfer/stock/confirm') {
      return this.transferService.getTransferStockConfirm(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/equity/transfer/cash/confirm') {
      return this.transferService.transferCashConfirm(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/equity/transfer/cash/confirm') {
      return this.transferService.getTransferCashConfirm(message.data, ctx);
    } else if (message.uri === 'post:/api/v1/equity/loan/confirm') {
      return this.transferService.loanConfirm(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/equity/loan/confirm') {
      return this.transferService.getloanConfirm(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/order/orderBook') {
      return this.orderService.getOrderBook(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/estAssetLoanInfo') {
      return this.accountService.getEstAssetLoanInfo(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/subAccount') {
      return this.balanceService.getSubList(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/account/navHistory') {
      return this.balanceService.queryNavHistory(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/market/stock/latest') {
      return this.marketService.getMarketStockLatest(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/market/symbol/bidOffer/{symbol}') {
      return this.marketService.getMarketStockBidOffer(message.data, ctx);
    } else if (message.uri === 'get:/api/v1/lotte/equity/cash/deposit/history') {
      return this.accountService.getCashDepositHistory(message.data, ctx);
    } else if (new RegExp(/tradexStopOrderForward/g).test(message.uri)) {
      return this.forwardRequest(message, ctx);
    }
    return Promise.reject(new Errors.UriNotFound());
  };

  private async addHeadersForClientCredentials(message: Kafka.IMessage, ctx: IContext) {
    const data = message.data;
    if (
      data?.headers?.token?.grantType === 'client_credentials' &&
      !config.apiNotRequireQueryHeaderTokenUserData.includes(message.uri)
    ) {
      const accountNumber = data?.accountNumber?.toUpperCase();
      if (Utils.isEmpty(accountNumber)) {
        new Errors.InvalidParameterError().add('FIELD_IS_REQUIRED', 'accountNumber', ['accountNumber']).throwErr();
      }
      const headerTokenUserData: HeaderTokenUserData = await this.headerTokenUserDataRepository.findOne({
        where: { accountNumber },
      });

      if (headerTokenUserData) {
        Object.assign(message.data.headers.token, { userData: JSON.parse(headerTokenUserData.userData) });
      } else {
        throw new Errors.ObjectNotFoundError();
      }
    }
  }

  private forwardRequest(message: Kafka.IMessage, ctx: IContext) {
    const forward = config.forwards.find((f) => f.pattern.match(message.uri));
    if (forward) {
      Logger.warn(`${ctx.id} forward request to topic ${forward.topic} with uri ${forward.uri}`);
      Kafka.getInstance().sendForwardMessage(message, forward.topic, forward.uri);
      throw new Errors.NoForwardResponseError();
    }
    throw new Errors.UriNotFound();
  }
}
