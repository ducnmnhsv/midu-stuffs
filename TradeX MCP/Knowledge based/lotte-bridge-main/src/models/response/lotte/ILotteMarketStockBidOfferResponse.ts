import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteMarketStockBidOfferResponse extends ILotteCommonResponse {
  data_list: ILotteMarketStockBidOfferData[];
}

export interface ILotteMarketStockBidOfferData {
  code: string;
  ceiling: string;
  ceilingStatus: string;
  floor: string;
  floorStatus: string;
  refPrice: string;
  avgPrice: string;
  avgPriceStatus: string;
  open: string;
  openStatus: string;
  high: string;
  highStatus: string;
  low: string;
  lowStatus: string;
  last: string;
  status: string;
  change: string;
  changeRate: string;
  volume: string;
  ptVol: string;
  totalVol: string;
  projectOpen: string;
  projectOpenStatus: string;
  controlCode: string; // e.g., "O" — can consider using enum if needed
  time: string;
  totalBidSize: string;
  totalOfferSize: string;
  marketName: string;
  matchedVol: string;
  bidOfferList: IBidOffer[];
}

export interface IBidOffer {
  bid: string;
  bidStatus: string;
  bidSize: string;
  offer: string;
  offerStatus: string;
  offerSize: string;
}
