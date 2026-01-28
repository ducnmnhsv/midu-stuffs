import { TradexNotification } from 'tradex-common';

export default class MarginDisbursement implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  lnd_amt?: string;
  tot_lnd_amt?: string;

  getTemplate(): string {
    return 'lottehpt_margin_disbursement';
  }
}
