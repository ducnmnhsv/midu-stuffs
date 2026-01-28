export interface ISymbolStatistics {
  code?: string;
  type?: string;
  date?: string;
  time?: string;
  tradingVolume?: number;
  totalBuyVolume?: number;
  totalBuyRaito?: number;
  totalSellVolume?: number;
  totalSellRaito?: number;
  totalUnkownVolume?: number;
  totalUnkownRaito?: number;
  prices?: IPriceSteps[];
}

export interface IPriceSteps {
  price?: number;
  matchedVolume?: number;
  matchedRaito?: number;
  matchedBuyVolume?: number;
  buyRaito?: number;
  matchedSellVolume?: number;
  sellRaito?: number;
  matchedUnknowVolume?: number;
  unknowRaito?: number;
}

export interface ISymbolStatisticsResponse {
  s?: string;
  t?: string;
  d?: string;
  ti?: string;
  vo?: number;
  tbv?: number;
  tbr?: number;
  tsv?: number;
  tsr?: number;
  tuv?: number;
  tur?: number;
  ps?: IPriceStepsResponse[];
}

export interface IPriceStepsResponse {
  p?: number;
  av?: number;
  ar?: number;
  ab?: number;
  br?: number;
  as?: number;
  sr?: number;
  au?: number;
  ur?: number;
}
