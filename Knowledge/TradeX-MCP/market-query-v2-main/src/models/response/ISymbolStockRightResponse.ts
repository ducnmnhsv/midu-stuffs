export interface ISymbolStockRightResponse {
  div: IDividendResponse;
  bonus: IBonusResponse;
  issue: IIssueResponse;
  md: string;
}

export interface IDividendResponse {
  bd: string;
  br: string;
  cr: string;
  cpd: string;
  fpd: string;
  fp: number;
  ed: string;
  sr: string;
}

export interface IBonusResponse {
  bd: string;
  br: string;
  r: string;
  p: number;
  pd: string;
  ed: string;
}

export interface IIssueResponse {
  bd: string;
  br: string;
  r: string;
  p: number;
  ap: string;
  tp: string;
  ed: string;
}
