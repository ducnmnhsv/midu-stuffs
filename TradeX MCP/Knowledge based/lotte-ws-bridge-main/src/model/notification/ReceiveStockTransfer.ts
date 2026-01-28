import { TradexNotification } from 'tradex-common';

export default class ReceiveStockTransfer implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  stk_cd_kl_stk_qty?: string;

  getTemplate(): string {
    return 'lottehpt_receive_stock_transfer';
  }
}
