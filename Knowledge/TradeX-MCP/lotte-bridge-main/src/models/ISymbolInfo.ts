export interface ISymbolInfo {
  _id?: string;
  code?: string;
  type?: string;

  name?: string;
  nameEn?: string;
  marketType?: string;
  securitiesType?: string;

  open?: number;
  high?: number;
  low?: number;
  last?: number;
  change?: number;
  rate?: number;
  tradingVolume?: number;
  tradingValue?: number;
  time?: string;

  sequence?: number; // count quote update
  sessions?: string;
  updatedAt?: Date;
  createdAt?: Date;

  // FUTURES/STOCK/CW
  expectedPrice?: number; // at ATO/ATC
  expectedChange?: number; // at ATO/ATC
  expectedRate?: number; // at ATO/ATC
  expectedVolume?: number; // at ATO/ATC
  ceilingFloorEqual?: string;
  ceilingPrice?: number;
  floorPrice?: number;
  referencePrice?: number;
  averagePrice?: number;
  highTime?: string;
  lowTime?: string;
  turnoverRate?: number;
  listedQuantity?: number;
  highPrice52Week?: number;
  lowPrice52Week?: number;
  bidPrice?: number;
  offerPrice?: number;
  totalBidVolume?: number;
  totalOfferVolume?: number;
  totalBidValue?: number;
  totalOfferValue?: number;
  industry?: string;
  matchBy?: string;
  matchingVolume?: number;
  ptTradingValue?: number;
  ptTradingVolume?: number;
  bidOfferList?: IBidOfferItem[];
  oddlotBidOfferList?: IBidOfferItem[];
  bidofferTime?: string;
  // STOCK + CW + INDEX
  priorTradingVolume?: number;

  // STOCK + FUTURES
  rights?: string;
  parValue?: number;
  foreignerBuyVolume?: number;
  foreignerSellVolume?: number;
  foreignerBuyValue?: number;
  foreignerSellValue?: number;
  foreignerTotalRoom?: number;
  foreignerCurrentRoom?: number;

  // STOCK
  bidVolume?: number;
  offerVolume?: number;
  totalBidCount?: number;
  totalOfferCount?: number;

  // for stock, cw
  estimatedData?: IEstimatedData;

  // FIX data:
  fixSecurityType?: string;
  cfiCode?: string;
  currency?: string;
  securityExchange?: string;
  roundLot?: number;
  minTradeVolume?: number;
  contractMultiplier?: number;

  // CW
  issuerName?: string;
  exercisePrice?: number;
  exerciseRatio?: string;
  breakEven?: number;
  cwPremium?: number; // % premium
  impliedVolatility?: number;
  parity?: number;
  tPrice?: number;
  delta?: number;
  gearingRt?: number;
  capitalFulcrumPoint?: number;
  underlyingSymbol?: string;
  underlyingPrice?: number;
  underlyingChange?: number;
  underlyingRate?: number;
  lastTradingDate?: Date;

  // FUTURES/CW
  maturityDate?: Date;
  controlCode?: string;
  changeOfTotalBidVolume?: number;
  changeOfTotalOfferVolume?: number;
  diffBidOffer?: number;
  accumulateBidVolume?: number;
  accumulateBidCount?: number;
  accumulateOfferVolume?: number;
  accumulateOfferCount?: number;

  // FUTURES/INDEX
  refCode?: string;

  // FUTURES
  marketName?: string;
  priorVolume?: number;
  highLowYearData?: IFuturesHighLowYearItem[];
  baseCode?: string;
  baseCodeSecuritiesType?: string;
  openInterest?: number;
  openInterestChange?: number;
  normalForeignerBuyVolume?: number;
  normalForeignerBuyValue?: number;
  normalForeignerSellVolume?: number;
  normalForeignerSellValue?: number;
  ptForeignerTotalBuyVolume?: number;
  ptForeignerTotalBuyValue?: number;
  ptForeignerTotalSellVolume?: number;
  ptForeignerTotalSellValue?: number;
  firstTradingDate?: Date;
  remainDate?: number;
  theoryPrice?: number;
  basis?: number;
  theoryBasis?: number;
  marketBasis?: number;
  disparate?: number;
  disparateRate?: number;
  exchange?: number;

  // INDEX
  indexType?: string; // DOMESTIC/FOREIGN
  upCount?: number;
  ceilingCount?: number;
  downCount?: number;
  floorCount?: number;
  unchangedCount?: number;
  isHighlight?: number;
  tradeCount?: number;
  unTradeCount?: number;
  indexSession?: IIndexQuoteSession[];

  iNAV?: number;
  iIndexValue?: number;
}

export interface IIndexQuoteSession {
  last?: number;
  change?: number;
  rate?: number;
  tradingVolume?: number;
  tradingValue?: number;
}

export interface IEstimatedData {
  ceilingPrice: number;
  floorPrice: number;
}

export interface IFuturesHighLowYearItem {
  highPrice?: number;
  dateOfHighPrice?: string;
  lowPrice?: number;
  dateOfLowPrice?: string;
}

export interface IBidOfferItem {
  bidPrice?: number;
  bidVolume?: number;
  bidVolumeChange?: number;
  offerPrice?: number;
  offerVolume?: number;
  offerVolumeChange?: number;
}
