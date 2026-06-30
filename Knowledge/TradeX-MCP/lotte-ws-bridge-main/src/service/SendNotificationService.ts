import { TradexNotification } from 'tradex-common';
import { v4 as uuid } from 'uuid';
import ChangePassword from '../model/notification/ChangePassword';
import { IChangePasswordRequest } from '../model/request/IChangePasswordRequest';
import ReceiveStockTransfer from '../model/notification/ReceiveStockTransfer';
import { IReceiveStockTransferRequest } from '../model/request/IReceiveStockTransferRequest';
import { IDepositStockRequest } from '../model/request/IDepositStockRequest';
import DepositStock from '../model/notification/DepositStock';
import AnnouncementBuyOrderMatching from '../model/notification/AnnouncementBuyOrderMatching';
import { IAnnouncementBuyOrderMatchingRequest } from '../model/request/IAnnouncementBuyOrderMatchingRequest';
import { IAnnouncementSellOrderMatchingRequest } from '../model/request/IAnnouncementSellOrderMatchingRequest';
import AnnouncementSellOrderMatching from '../model/notification/AnnouncementSellOrderMatching';
import { IRegisterBuyIssueStockRequest } from '../model/request/IRegisterBuyIssueStockRequest';
import RegisterBuyIssueStock from '../model/notification/RegisterBuyIssueStock';
import { ICancelBuyIssueStockRequest } from '../model/request/ICancelBuyIssueStockRequest';
import CancelBuyIssueStock from '../model/notification/CancelBuyIssueStock';
import { ISendMoneyRequest } from '../model/request/ISendMoneyRequest';
import SendMoney from '../model/notification/SendMoney';
import { IWithdrawMoneyRequest } from '../model/request/IWithdrawMoneyRequest';
import WithdrawMoney from '../model/notification/WithdrawMoney';
import { IDividendPaymentRequest } from '../model/request/IDividendPaymentRequest';
import DividendPayment from '../model/notification/DividendPayment';
import { IRightToBuyStockIssueRequest } from '../model/request/IRightToBuyStockIssueRequest';
import RightToBuyStockIssue from '../model/notification/RightToBuyStockIssue';
import { INoticeReceiveBonusStockRequest } from '../model/request/INoticeReceiveBonusStockRequest';
import NoticeReceiveBonusStock from '../model/notification/NoticeReceiveBonusStock';
import { IStockDividendsPaymentRequest } from '../model/request/IStockDividendsPaymentRequest';
import StockDividendsPayment from '../model/notification/StockDividendsPayment';
import { IAdvancePaymentSecuritiesSalesRequest } from '../model/request/IAdvancePaymentSecuritiesSalesRequest';
import AdvancePaymentSecuritiesSales from '../model/notification/AdvancePaymentSecuritiesSales';
import { IMarginDisbursementRequest } from '../model/request/IMarginDisbursementRequest';
import MarginDisbursement from '../model/notification/MarginDisbursement';
import { IDerivativesPositionWarningLevel1Request } from '../model/request/IDerivativesPositionWarningLevel1Request';
import DerivativesPositionWarningLevel1 from '../model/notification/DerivativesPositionWarningLevel1';
import { IDerivativesPositionWarningLevel2Request } from '../model/request/IDerivativesPositionWarningLevel2Request';
import DerivativesPositionWarningLevel2 from '../model/notification/DerivativesPositionWarningLevel2';
import { IDerivativesOrderMatchingRequest } from '../model/request/IDerivativesOrderMatchingRequest';
import DerivativesOrderMatching from '../model/notification/DerivativesOrderMatching';
import { IDerivativesVmProfitPaymentRequest } from '../model/request/IDerivativesVmProfitPaymentRequest';
import DerivativesVmProfitPayment from '../model/notification/DerivativesVmProfitPayment';
import { IDerivativesVmLossPaymentRequest } from '../model/request/IDerivativesVmLossPaymentRequest';
import DerivativesVmLossPayment from '../model/notification/DerivativesVmLossPayment';
import { IDerivativesMarginCallLevel2Request } from '../model/request/IDerivativesMarginCallLevel2Request';
import DerivativesMarginCallLevel2 from '../model/notification/DerivativesMarginCallLevel2';
import { IDerivativesMarginCallLevel3Request } from '../model/request/IDerivativesMarginCallLevel3Request';
import DerivativesMarginCallLevel3 from '../model/notification/DerivativesMarginCallLevel3';
import { IDerivativesMarginDepositRequest } from '../model/request/IDerivativesMarginDepositRequest';
import DerivativesMarginDeposit from '../model/notification/DerivativesMarginDeposit';
import { IDerivativesMarginWithdrawalRequest } from '../model/request/IDerivativesMarginWithdrawalRequest';
import DerivativesMarginWithdrawal from '../model/notification/DerivativesMarginWithdrawal';

let sendNotification: TradexNotification.SendNotification = TradexNotification.getInstance();

export function getSendNotification() {
  if (sendNotification == null) {
    sendNotification = TradexNotification.getInstance();
  }
  return sendNotification;
}

export function notifyOneSignal(
  data: TradexNotification.ITemplateData,
  acnt_no: string,
  extraFilter?: TradexNotification.IFilter[]
) {
  const conf: TradexNotification.OneSignalConfiguration = new TradexNotification.OneSignalConfiguration();
  conf.domain = 'nhsv';

  conf.filters = [];
  if (extraFilter != null) {
    conf.filters = conf.filters.concat(extraFilter);
  }
  const filter: TradexNotification.IFilter = {
    field: 'tag',
    key: 'accountNumber',
    relation: '=',
    value: `${acnt_no}`,
  };
  conf.filters.push(filter);
  getSendNotification().sendPushNotification(uuid(), conf, data);
}

export function sendOneSignalChangePassword(request: IChangePasswordRequest): void {
  const changePassword: ChangePassword = new ChangePassword();
  changePassword.flag = request.flag;
  notifyOneSignal(changePassword, request.acnt_no);
}

export function sendOneSignalDividendPayment(request: IDividendPaymentRequest): void {
  const dividendPayment: DividendPayment = new DividendPayment();
  dividendPayment.acnt_no = request.acnt_no;
  dividendPayment.sub_no = request.sub_no;
  dividendPayment.trd_amt = request.trd_amt;
  dividendPayment.stk_cd = request.stk_cd;
  dividendPayment.percent = request.percent;
  dividendPayment.dpo = request.dpo;
  notifyOneSignal(dividendPayment, request.acnt_no);
}

export function sendOneSignalReceiveStockTransfer(request: IReceiveStockTransferRequest): void {
  const receiveStockTransfer: ReceiveStockTransfer = new ReceiveStockTransfer();
  receiveStockTransfer.acnt_no = request.acnt_no;
  receiveStockTransfer.sub_no = request.sub_no;
  receiveStockTransfer.stk_cd_kl_stk_qty = request['stk_cd KL stk_qty, stk_cd2 KL stk_qty2'];
  notifyOneSignal(receiveStockTransfer, request.acnt_no);
}

export function sendOneSignalRightToBuyStockIssue(request: IRightToBuyStockIssueRequest): void {
  const rightToBuyStockIssue: RightToBuyStockIssue = new RightToBuyStockIssue();
  rightToBuyStockIssue.trd_dt = request.trd_dt;
  rightToBuyStockIssue.stk_cd = request.stk_cd;
  rightToBuyStockIssue.rate = request.rate;
  notifyOneSignal(rightToBuyStockIssue, request.acnt_no);
}

export function sendOneSignalNoticeReceiveBonusStock(request: INoticeReceiveBonusStockRequest): void {
  const noticeReceiveBonusStock: NoticeReceiveBonusStock = new NoticeReceiveBonusStock();
  noticeReceiveBonusStock.acnt_no = request.acnt_no;
  noticeReceiveBonusStock.sub_no = request.sub_no;
  noticeReceiveBonusStock.stk_cd = request.stk_cd;
  noticeReceiveBonusStock.stk_qty = request.stk_qty;
  notifyOneSignal(noticeReceiveBonusStock, request.acnt_no);
}

export function sendOneSignalDepositStock(request: IDepositStockRequest): void {
  const depositStock: DepositStock = new DepositStock();
  depositStock.acnt_no = request.acnt_no;
  depositStock.sub_no = request.sub_no;
  depositStock.stk_cd = request.stk_cd;
  depositStock.stk_qty = request.stk_qty;
  notifyOneSignal(depositStock, request.acnt_no);
}

export function sendOneSignalStockDividendsPayment(request: IStockDividendsPaymentRequest): void {
  const stockDividendsPayment: StockDividendsPayment = new StockDividendsPayment();
  stockDividendsPayment.acnt_no = request.acnt_no;
  stockDividendsPayment.sub_no = request.sub_no;
  stockDividendsPayment.stk_cd = request.stk_cd;
  stockDividendsPayment.stk_qty = request.stk_qty;
  notifyOneSignal(stockDividendsPayment, request.acnt_no);
}

export function sendOneSignalAnnouncementBuyOrderMatching(request: IAnnouncementBuyOrderMatchingRequest): void {
  const buyOrderMatching: AnnouncementBuyOrderMatching = new AnnouncementBuyOrderMatching();
  buyOrderMatching.acnt_no = request.acnt_no;
  buyOrderMatching.sub_no = request.sub_no;
  buyOrderMatching.stk_cd = request.stk_cd;
  buyOrderMatching.mth_qty = request.mth_qty;
  buyOrderMatching.mth_pri = request.mth_pri;
  buyOrderMatching.mth_time = request.mth_time;
  notifyOneSignal(buyOrderMatching, request.acnt_no);
}

export function sendOneSignalAnnouncementSellOrderMatching(request: IAnnouncementSellOrderMatchingRequest): void {
  const sellOrderMatching: AnnouncementSellOrderMatching = new AnnouncementSellOrderMatching();
  sellOrderMatching.acnt_no = request.acnt_no;
  sellOrderMatching.sub_no = request.sub_no;
  sellOrderMatching.stk_cd = request.stk_cd;
  sellOrderMatching.mth_qty = request.mth_qty;
  sellOrderMatching.mth_pri = request.mth_pri;
  sellOrderMatching.mth_time = request.mth_time;
  notifyOneSignal(sellOrderMatching, request.acnt_no);
}

export function sendOneSignalRegisterBuyIssueStock(request: IRegisterBuyIssueStockRequest): void {
  const registerBuyIssueStock: RegisterBuyIssueStock = new RegisterBuyIssueStock();
  registerBuyIssueStock.acnt_no = request.acnt_no;
  registerBuyIssueStock.sub_no = request.sub_no;
  registerBuyIssueStock.trd_amt = request.trd_amt;
  registerBuyIssueStock.trd_qty = request.trd_qty;
  registerBuyIssueStock.stk_cd = request.stk_cd;
  registerBuyIssueStock.dpo = request.dpo;
  notifyOneSignal(registerBuyIssueStock, request.acnt_no);
}

export function sendOneSignalCancelBuyIssueStock(request: ICancelBuyIssueStockRequest): void {
  const cancelBuyIssueStock: CancelBuyIssueStock = new CancelBuyIssueStock();
  cancelBuyIssueStock.acnt_no = request.acnt_no;
  cancelBuyIssueStock.sub_no = request.sub_no;
  cancelBuyIssueStock.trd_amt = request.trd_amt;
  cancelBuyIssueStock.trd_qty = request.trd_qty;
  cancelBuyIssueStock.stk_cd = request.stk_cd;
  cancelBuyIssueStock.dpo = request.dpo;
  notifyOneSignal(cancelBuyIssueStock, request.acnt_no);
}

export function sendOneSignalAdvancePaymentSecuritiesSales(request: IAdvancePaymentSecuritiesSalesRequest): void {
  const advancePaymentSecuritiesSales: AdvancePaymentSecuritiesSales = new AdvancePaymentSecuritiesSales();
  advancePaymentSecuritiesSales.acnt_no = request.acnt_no;
  advancePaymentSecuritiesSales.sub_no = request.sub_no;
  advancePaymentSecuritiesSales.lnd_amt = request.lnd_amt;
  advancePaymentSecuritiesSales.dpo = request.dpo;
  notifyOneSignal(advancePaymentSecuritiesSales, request.acnt_no);
}

export function sendOneSignalSendMoney(request: ISendMoneyRequest): void {
  const sendMoney: SendMoney = new SendMoney();
  sendMoney.acnt_no = request.acnt_no;
  sendMoney.sub_no = request.sub_no;
  sendMoney.trd_amt = request.trd_amt;
  sendMoney.trd_tm = request.trd_tm;
  sendMoney.trd_dt = request.trd_dt;
  sendMoney.dpo = request.dpo;
  notifyOneSignal(sendMoney, request.acnt_no);
}

export function sendOneSignalWithdrawMoney(request: IWithdrawMoneyRequest): void {
  const withdrawMoney: WithdrawMoney = new WithdrawMoney();
  withdrawMoney.acnt_no = request.acnt_no;
  withdrawMoney.sub_no = request.sub_no;
  withdrawMoney.trd_amt = request.trd_amt;
  withdrawMoney.trd_tm = request.trd_tm;
  withdrawMoney.trd_dt = request.trd_dt;
  withdrawMoney.dpo = request.dpo;
  notifyOneSignal(withdrawMoney, request.acnt_no);
}

export function sendOneSignalMarginDisbursement(request: IMarginDisbursementRequest): void {
  const marginDisbursement: MarginDisbursement = new MarginDisbursement();
  marginDisbursement.acnt_no = request.acnt_no;
  marginDisbursement.sub_no = request.sub_no;
  marginDisbursement.lnd_amt = request.lnd_amt;
  marginDisbursement.tot_lnd_amt = request.tot_lnd_amt;
  notifyOneSignal(marginDisbursement, request.acnt_no);
}

function formatDisplayDate(input?: string): string | undefined {
  if (input == null || input === '') {
    return input;
  }
  if (input.indexOf('/') >= 0) {
    return input;
  }
  if (/^\d{8}$/.test(input)) {
    return `${input.substring(6, 8)}/${input.substring(4, 6)}/${input.substring(0, 4)}`;
  }
  return input;
}

function translateSideEn(input?: string): string | undefined {
  if (input == null || input === '') {
    return input;
  }
  switch (input.toUpperCase()) {
    case 'MUA':
      return 'BUY';
    case 'B\u00C1N':
    case 'BAN':
      return 'SELL';
    default:
      return input;
  }
}

function withDefaultDerivativesSubNo(sub_no?: string): string {
  if (sub_no == null || sub_no === '') {
    return '80';
  }
  return sub_no;
}

export function sendOneSignalDerivativesOrderMatching(request: IDerivativesOrderMatchingRequest): void {
  const derivativesOrderMatching: DerivativesOrderMatching = new DerivativesOrderMatching();
  derivativesOrderMatching.acnt_no = request.acnt_no;
  derivativesOrderMatching.sub_no = withDefaultDerivativesSubNo();
  derivativesOrderMatching.series = request.series;
  derivativesOrderMatching.sb_tp = request.sb_tp;
  derivativesOrderMatching.sb_tp_en = translateSideEn(request.sb_tp);
  derivativesOrderMatching.mth_qty = request.mth_qty;
  derivativesOrderMatching.mth_pri = request.mth_pri;
  derivativesOrderMatching.mth_time = request.mth_time;
  derivativesOrderMatching.date_fmt = formatDisplayDate(request.date);
  notifyOneSignal(derivativesOrderMatching, request.acnt_no);
}

export function sendOneSignalDerivativesPositionWarningLevel1(request: IDerivativesPositionWarningLevel1Request): void {
  const derivativesPositionWarningLevel1: DerivativesPositionWarningLevel1 = new DerivativesPositionWarningLevel1();
  derivativesPositionWarningLevel1.acnt_no = request.acnt_no;
  derivativesPositionWarningLevel1.sub_no = withDefaultDerivativesSubNo();
  derivativesPositionWarningLevel1.W1 = request.W1;
  derivativesPositionWarningLevel1.commd_cd = request.commd_cd;
  derivativesPositionWarningLevel1.date_fmt = formatDisplayDate(request.date);
  notifyOneSignal(derivativesPositionWarningLevel1, request.acnt_no);
}

export function sendOneSignalDerivativesPositionWarningLevel2(request: IDerivativesPositionWarningLevel2Request): void {
  const derivativesPositionWarningLevel2: DerivativesPositionWarningLevel2 = new DerivativesPositionWarningLevel2();
  derivativesPositionWarningLevel2.acnt_no = request.acnt_no;
  derivativesPositionWarningLevel2.sub_no = withDefaultDerivativesSubNo();
  derivativesPositionWarningLevel2.W2 = request.W2;
  derivativesPositionWarningLevel2.commd_cd = request.commd_cd;
  derivativesPositionWarningLevel2.date_fmt = formatDisplayDate(request.date);
  notifyOneSignal(derivativesPositionWarningLevel2, request.acnt_no);
}

export function sendOneSignalDerivativesVmProfitPayment(request: IDerivativesVmProfitPaymentRequest): void {
  const derivativesVmProfitPayment: DerivativesVmProfitPayment = new DerivativesVmProfitPayment();
  derivativesVmProfitPayment.acnt_no = request.acnt_no;
  derivativesVmProfitPayment.sub_no = withDefaultDerivativesSubNo();
  derivativesVmProfitPayment.trd_amt = request.trd_amt;
  derivativesVmProfitPayment.trd_dt_fmt = formatDisplayDate(request.trd_dt);
  notifyOneSignal(derivativesVmProfitPayment, request.acnt_no);
}

export function sendOneSignalDerivativesVmLossPayment(request: IDerivativesVmLossPaymentRequest): void {
  const derivativesVmLossPayment: DerivativesVmLossPayment = new DerivativesVmLossPayment();
  derivativesVmLossPayment.acnt_no = request.acnt_no;
  derivativesVmLossPayment.sub_no = withDefaultDerivativesSubNo();
  derivativesVmLossPayment.trd_amt = request.trd_amt;
  derivativesVmLossPayment.trd_dt_fmt = formatDisplayDate(request.trd_dt);
  notifyOneSignal(derivativesVmLossPayment, request.acnt_no);
}

export function sendOneSignalDerivativesMarginCallLevel2(request: IDerivativesMarginCallLevel2Request): void {
  const derivativesMarginCallLevel2: DerivativesMarginCallLevel2 = new DerivativesMarginCallLevel2();
  derivativesMarginCallLevel2.acnt_no = request.acnt_no;
  derivativesMarginCallLevel2.sub_no = withDefaultDerivativesSubNo();
  derivativesMarginCallLevel2.CU = request.CU;
  derivativesMarginCallLevel2.W2 = request.W2;
  derivativesMarginCallLevel2.W3 = request.W3;
  notifyOneSignal(derivativesMarginCallLevel2, request.acnt_no);
}

export function sendOneSignalDerivativesMarginCallLevel3(request: IDerivativesMarginCallLevel3Request): void {
  const derivativesMarginCallLevel3: DerivativesMarginCallLevel3 = new DerivativesMarginCallLevel3();
  derivativesMarginCallLevel3.acnt_no = request.acnt_no;
  derivativesMarginCallLevel3.sub_no = withDefaultDerivativesSubNo();
  derivativesMarginCallLevel3.CU = request.CU;
  notifyOneSignal(derivativesMarginCallLevel3, request.acnt_no);
}

export function sendOneSignalDerivativesMarginDeposit(request: IDerivativesMarginDepositRequest): void {
  const derivativesMarginDeposit: DerivativesMarginDeposit = new DerivativesMarginDeposit();
  derivativesMarginDeposit.acnt_no = request.acnt_no;
  derivativesMarginDeposit.sub_no = withDefaultDerivativesSubNo();
  derivativesMarginDeposit.trd_amt = request.trd_amt;
  derivativesMarginDeposit.trd_dt_fmt = formatDisplayDate(request.trd_dt);
  derivativesMarginDeposit.new_vsd_dpo = request.new_vsd_dpo;
  notifyOneSignal(derivativesMarginDeposit, request.acnt_no);
}

export function sendOneSignalDerivativesMarginWithdrawal(request: IDerivativesMarginWithdrawalRequest): void {
  const derivativesMarginWithdrawal: DerivativesMarginWithdrawal = new DerivativesMarginWithdrawal();
  derivativesMarginWithdrawal.acnt_no = request.acnt_no;
  derivativesMarginWithdrawal.sub_no = withDefaultDerivativesSubNo();
  derivativesMarginWithdrawal.trd_amt = request.trd_amt;
  derivativesMarginWithdrawal.trd_dt_fmt = formatDisplayDate(request.trd_dt);
  derivativesMarginWithdrawal.new_vsd_dpo = request.new_vsd_dpo;
  notifyOneSignal(derivativesMarginWithdrawal, request.acnt_no);
}
