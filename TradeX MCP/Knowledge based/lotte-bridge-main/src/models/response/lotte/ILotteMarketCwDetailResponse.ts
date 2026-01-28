import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteMarketCwDetailResponse extends ILotteCommonResponse {
  data_list: ILotteMarketCwDetailData[];
}

export interface ILotteMarketCwDetailData {
  cwType: string;
  cwUnderType: string;
  cwExecuteType: string;
  issuerNm: string;
  cwUnderSymbol: string;
  cwMaturityDate: string;
  cwLastTradeDate: string;
  cwMulti: string;
  cwExpiredRate: string;
  cwExpiredPrice: number | string;
  cwSettlementMethod: string;
  cwCode: string;
  underClassicPrice: string;
  profitLossStatus: string;
  priceDiff: string;
  breanEvenPoint: string;
  expiredDay: string;
  cwListStockQuantity: string;
}
