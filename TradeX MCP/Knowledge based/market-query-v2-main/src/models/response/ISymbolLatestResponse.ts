import { IHighLowYearItem } from './IHighLowYearItem';

export interface ISymbolLatestResponse {
  /**
   * symbol code
   */
  s?: string;
  /**
   * type: INDEX/STOCK/FUTURES/CW
   */
  t?: 'INDEX' | 'STOCK' | 'FUTURES' | 'CW';
  /**
   * open price
   */
  o?: number;
  /**
   * high price
   */
  h?: number;
  /**
   * low price
   */
  l?: number;
  /**
   * close price
   */
  c?: number;
  mc?: number;
  /**
   * average price
   */
  a?: number;
  /**
   * expected price
   */
  ep?: number;
  /**
   * expected change
   */
  exc?: number;
  /**
   * expected rate
   */
  exr?: number;
  /**
   * expected volume
   */
  exv?: number;
  /**
   * exercise price
   */
  exp?: number;
  // turnoverrate
  tor?: number;
  /**
   * change
   */
  ch?: number;
  /**
   * rate
   */
  ra?: number;
  /**
   * trading volume
   */
  vo?: number;
  /**
   * trading value
   */
  va?: number;
  /**
   * basis (for futures)
   */
  ba?: number;
  /**
   * open interest
   */
  oi?: number;
  /**
   * match volume
   */
  mv?: number;
  /**
   * match by (CEILING/FLOOR)
   */
  mb?: 'CEILING' | 'FLOOR' | '';
  /**
   * Bid Offer Time
   */
  bot?: string;
  /**
   * best bid
   */
  bb?: {
    /**
     * price
     */
    p?: number;
    /**
     * volume
     */
    v?: number;
    /**
     * volume change
     */
    c?: number;
  }[];
  /**
   * best offer
   */
  bo?: {
    /**
     * price
     */
    p?: number;
    /**
     * volume
     */
    v?: number;
    /**
     * volume change
     */
    c?: number;
  }[];
  /**
   * session
   */
  ss?: string;
  /**
   * total Bid Volume
   */
  tb?: number;
  /**
   * total Offer Volume
   */
  to?: number;
  /**
   * index change
   */
  ic?: {
    /**
     * ceiling count
     */
    ce?: number;
    /**
     * floor count
     */
    fl?: number;
    /**
     * up count
     */
    up?: number;
    /**
     * down count
     */
    dw?: number;
    /**
     * unChange count
     */
    uc?: number;
    /**
     * trade count
     */
    tc?: number;
    /**
     * unTrade count
     */
    utc?: number;
  };
  /**
   * foreigner
   */
  fr?: {
    /**
     * buy volume
     */
    bv?: number;
    /**
     * sell volume
     */
    sv?: number;
    /**
     * net buy value
     */
    nva?: number;
    /**
     * net buy volume
     */
    nvo?: number;
    /**
     * total room
     */
    tr?: number;
    /**
     * current room
     */
    cr?: number;
  };
  /**
   * break even
   */
  be?: number;
  /**
   * % premium
   */
  pe?: number;

  pvo?: number; // put through volume
  pva?: number; // put through value
  hly?: IHighLowYearItem[]; // high low year data

  inav?: string;
  iidx?: string;
}
