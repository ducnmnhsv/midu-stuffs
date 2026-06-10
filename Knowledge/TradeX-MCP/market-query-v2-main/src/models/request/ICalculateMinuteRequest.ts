import { BaseRequest } from 'tradex-models-common';

export type ICalculateMinuteRequest = BaseRequest & {
  symbolList?: string[];
};
