import { TradexNotification } from 'tradex-common';

export default class AnnouncementSellOrderMatching implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  stk_cd?: string;
  mth_qty?: string;
  mth_pri?: string;
  mth_time?: string;

  getTemplate(): string {
    return 'lottehpt_announcement_sell_order_matching';
  }
}
