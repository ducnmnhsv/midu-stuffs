import { BaseRequest } from 'tradex-models-common';

export type IAvgTradingVolume10 = BaseRequest & {
  symbolList?: string[];
  [k: string]: any;
};
