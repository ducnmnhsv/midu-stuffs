import { TradexNotification } from 'tradex-common';

export default class RegisterBuyIssueStock implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  trd_amt?: string;
  trd_qty?: string;
  stk_cd?: string;
  dpo?: string;

  getTemplate(): string {
    return 'lottehpt_register_buy_issue_stock';
  }
}
