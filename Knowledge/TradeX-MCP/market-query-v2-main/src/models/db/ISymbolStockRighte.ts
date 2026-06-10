export interface ISymbolStockRight {
  dividend: IDividend;
  withoutcon: IBonus;
  withcon: IIssue;
  metaDate: string;
}

export interface IDividend {
  baseDate: string;
  baseRate: string;
  stkDividRate: string;
  cashDividRate: string;
  frcStkPrice: number;
  frcPayDate: string;
  cashPayDate: string;
  rcpDate: string;
}

export interface IBonus {
  baseDate: string;
  baseRate: string;
  dividRate: string;
  frcStkPrice: number;
  frcPayDate: string;
  rcpDate: string;
}

export interface IIssue {
  baseDate: string;
  baseRate: string;
  dividRate: string;
  issuePrice: number;
  applyPeriod: string;
  transferPeriod: string;
  rcpDate: string;
}
