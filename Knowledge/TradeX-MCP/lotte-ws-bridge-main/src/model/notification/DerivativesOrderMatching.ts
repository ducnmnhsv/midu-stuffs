import { TradexNotification } from 'tradex-common';

export default class DerivativesOrderMatching implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  series?: string;
  sb_tp?: string;
  sb_tp_en?: string;
  mth_qty?: string;
  mth_pri?: string;
  mth_time?: string;
  date_fmt?: string;

  getTemplate(): string {
    return 'lottehpt_derivatives_order_matching';
  }
}
