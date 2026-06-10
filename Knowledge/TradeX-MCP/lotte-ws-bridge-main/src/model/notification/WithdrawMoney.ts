import { TradexNotification } from 'tradex-common';

export default class WithdrawMoney implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  trd_amt?: string;
  trd_tm?: string;
  trd_dt?: string;
  dpo?: string;

  getTemplate(): string {
    return 'lottehpt_withdraw_money';
  }
}
