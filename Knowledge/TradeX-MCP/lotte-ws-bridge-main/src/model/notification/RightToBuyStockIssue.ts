import { TradexNotification } from 'tradex-common';

export default class RightToBuyStockIssue implements TradexNotification.ITemplateData {
  stk_cd?: string;
  trd_dt?: string;
  rate?: string;

  getTemplate(): string {
    return 'lottehpt_right_to_buy_stock_issue';
  }
}
