import { TradexNotification } from 'tradex-common';

export default class DepositStock implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  stk_qty?: string;
  stk_cd?: string;

  getTemplate(): string {
    return 'lottehpt_deposit_stock';
  }
}
