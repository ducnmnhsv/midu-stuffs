import { TradexNotification } from 'tradex-common';

export default class NoticeReceiveBonusStock implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  stk_cd?: string;
  stk_qty?: string;

  getTemplate(): string {
    return 'lottehpt_notice_receive_bonus_stock';
  }
}
