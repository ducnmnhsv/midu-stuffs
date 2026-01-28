export interface IMarketRightInfoResponse {
  div?: IDividend;
  bonus?: IBonus;
  issue?: IIssue;
}

export interface IDividend {
  bd?: string;
  br?: string;
  sr?: string;
  cr?: string;
  cpd?: string;
  fpd?: string;
  dp?: number;
  fp?: number;
  ed?: string;
}

export interface IBonus {
  bd?: string;
  br?: string;
  r?: string;
  p?: number;
  pd?: string;
  ed?: string;
}

export interface IIssue {
  bd?: string;
  br?: string;
  r?: string;
  p?: number;
  ap?: string;
  tp?: string;
  ed?: string;
}
