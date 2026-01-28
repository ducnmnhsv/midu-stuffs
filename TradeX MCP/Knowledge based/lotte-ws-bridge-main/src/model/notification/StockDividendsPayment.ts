import { TradexNotification } from 'tradex-common';

export default class StockDividendsPayment implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  stk_cd?: string;
  stk_qty?: string;

  getTemplate(): string {
    return 'lottehpt_stock_dividends_payment';
  }
}
