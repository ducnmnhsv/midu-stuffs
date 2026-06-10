import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteMarketRightInfoResponse extends ILotteCommonResponse {
  data_list: ILotteMarketRightInfoData[];
}

export interface ILotteMarketRightInfoData {
  dividend_basedate: string;
  dividend_baserate: string;
  dividend_stockrate: string;
  dividend_cashrate: string;
  dividend_cashpaydate: string;
  dividend_oddlotpaydate: string;
  dividend_oddlotprice: string;
  dividend_effectdate: string;
  withoutcon_basedate: string;
  withoutcon_baserate: string;
  withoutcon_allocrate: string;
  withoutcon_oddlotprice: string;
  withoutcon_oddlotpaydate: string;
  withoutcon_effectdate: string;
  withcon_basedate: string;
  withcon_baserate: string;
  withcon_allocrate: string;
  withcon_issueprice: string;
  withcon_subscdatebegin: string;
  withcon_subscdateend: string;
  withcon_transferdatebegin: string;
  withcon_transferdateend: string;
  withcon_rcpdate: string;
}
