import {BaseRequest} from "tradex-models-common";

/**
 * tradingViewHistoryRequest schema
 */
export type ITradingViewHistoryRequest = BaseRequest & {
  /**
   * symbol Code
   */
  symbol: string;
  /**
   * from time, in millisecond / 1000, like 1586933880, 10 number
   */
  from: number;
  /**
   * to time, in millisecond / 1000, like 1586933880, 10 number
   */
  to: number;
  /**
   * resolution, for minute: ['1','3','5','10','15','30','60'], for daily, 'D', '1D', '1W', 'W', '1M', 'M', '6M'
   */
  resolution: "1" | "3" | "5" | "10" | "15" | "30" | "60" | "D" | "1D" | "W" | "1W" | "M" | "1M" | "6M";
  /**
   * Fetch count, default 300 for chart
   */
  fetchCount?: null | number;
  /**
   * datetime of last received record, in millisecond / 1000, like 1586933880, 10 number
   */
  lastTime?: null | number;
  /**
   * countback
   */
  countback?: null | number;
  [k: string]: any;
};
