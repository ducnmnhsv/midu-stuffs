import { TradexNotification } from 'tradex-common';

export default class DerivativesMarginCallLevel2 implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  CU?: string;
  W2?: string;
  W3?: string;

  getTemplate(): string {
    return 'lottehpt_derivatives_margin_call_level_2';
  }
}
