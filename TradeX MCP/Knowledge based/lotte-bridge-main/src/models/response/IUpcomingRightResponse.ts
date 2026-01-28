export type IUpcomingRightResponse = IUpcomingRightItem[];

export interface IUpcomingRightItem {
  symbol: string;
  rightType: string;
  baseDate: string;
  quantity: number | null;
  inquiryDate: string;
  oddLotAmount: number | null;
  oddLotPaidDate: string;
  receivedAmount: number | null;
  dividendPayDate: string;
  nextKey: string;
}
