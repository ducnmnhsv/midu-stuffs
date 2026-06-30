import { TradexNotification } from 'tradex-common';

export default class DerivativesMarginDeposit implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  trd_amt?: string;
  trd_dt_fmt?: string;
  new_vsd_dpo?: string;

  getTemplate(): string {
    return 'lottehpt_derivatives_margin_deposit';
  }
}
