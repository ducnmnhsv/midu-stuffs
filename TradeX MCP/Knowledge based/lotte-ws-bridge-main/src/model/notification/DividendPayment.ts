import { TradexNotification } from 'tradex-common';

export default class DividendPayment implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  trd_amt?: string;
  stk_cd?: string;
  percent?: string;
  dpo?: string;

  getTemplate(): string {
    return 'lottehpt_dividend_payment';
  }
}
