export interface IOddlotLatestResponse {
  s?: string;
  vo?: number;
  va?: number;
  bot?: string;
  mc?: string;
  bb?: IPriceVolItem[];
  bo?: IPriceVolItem[];
  fr?: IForeign;
  tb?: number;
  to?: number;
  pva?: number;
  pvo?: number;
}

export interface IPriceVolItem {
  p?: number;
  v?: number;
}

export interface IForeign {
  bv?: number;
  sv?: number;
}
