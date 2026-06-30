import { TradexNotification } from 'tradex-common';

export default class DerivativesMarginCallLevel3 implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  CU?: string;

  getTemplate(): string {
    return 'lottehpt_derivatives_margin_call_level_3';
  }
}
