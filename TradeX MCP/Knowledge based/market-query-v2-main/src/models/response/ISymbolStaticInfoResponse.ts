import { SymbolStaticInfoResponse } from 'tradex-models-market';

export interface ISymbolStaticInfoResponse extends SymbolStaticInfoResponse {
  inav?: string;
  iidx?: string;
}
