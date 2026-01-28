import { TradexNotification } from 'tradex-common';

export default class AdvancePaymentSecuritiesSales implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  lnd_amt?: string;
  dpo?: string;

  getTemplate(): string {
    return 'lottehpt_advance_payment_securities_sales';
  }
}
