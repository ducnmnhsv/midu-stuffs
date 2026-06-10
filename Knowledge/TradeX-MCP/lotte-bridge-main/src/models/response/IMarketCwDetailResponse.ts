export interface IMarketCwDetailResponse {
  cwType?: string;
  cwStyle?: string;
  issuer?: string;
  underlyingStock?: string;
  maturityDate?: string;
  lastTradingDate?: string;
  conversionRatio?: number;
  exercisePrice?: number;
  settlementMethod?: string;
  symbol?: string;
  underlyingPrice?: number;
  status?: string;
  priceDiff?: number;
  breakEvenPoint?: number;
  expiredDay?: string;
  issuedQuantity?: number;
}
