import { IBidOfferOddLotUpdate } from '../../IBidOfferOddLotUpdate';
import { IBidOfferItem, ISymbolInfo } from '../../ISymbolInfo';
import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteOddLotResponse extends ILotteCommonResponse {
  data_list: ILotteOddLotDataList[];
}

export interface ILotteOddLotDataList {
  list: ILotteOddLotData[];
  hasNext: string;
  nextKey: string;
}

export interface ILotteOddLotData {
  market: string;
  code: string;
  last: number;
  status: number;
  change: number;
  changeRate: number;
  vol: number;
  totalVol: number;
  ptVol: number;
  matchedVol: number;
  refPrice: number;
  avgPrice: number;
  avgPriceStatus: number;
  ceiling: number;
  ceilingStatus: number;
  floor: number;
  floorStatus: number;
  open: number;
  openStatus: number;
  high: number;
  highStatus: number;
  low: number;
  lowStatus: number;
  bid1: number;
  bid1Status: number;
  bid1Size: number;
  offer1: number;
  offer1Status: number;
  offer1Size: number;
  bid2: number;
  bid2Status: number;
  bid2Size: number;
  offer2: number;
  offer2Status: number;
  offer2Size: number;
  bid3: number;
  bid3Status: number;
  bid3Size: number;
  offer3: number;
  offer3Status: number;
  offer3Size: number;
  projectOpen: number;
  projectOpenStatus: number;
  controlCode: number;
  foreignBuyVol: number;
  foreignSellVol: number;
  totalOfferSize: number;
  totalBidSize: number;
  nOffer: number;
  nBid: number;
}

export const toSymbolInfo = (
  data: ILotteOddLotData,
  symbolInfo: ISymbolInfo,
  bidOfferOddLot: IBidOfferOddLotUpdate
): ISymbolInfo => {
  // if (data.last && data.last > 0 && symbolInfo.last !== data.last) {
  //   bidOfferOddLot.last = data.last;
  // }
  // if (data.change && data.change > 0 && symbolInfo.change !== data.change) {
  //   bidOfferOddLot.change = data.change;
  // }
  // if (data.changeRate && data.changeRate > 0 && symbolInfo.rate !== data.changeRate) {
  //   bidOfferOddLot.rate = data.changeRate;
  // }
  // symbolInfo.priorVolume
  // symbolInfo.tradingVolume = data.totalVol;
  // symbolInfo.ptTradingVolume = data.ptVol;
  // symbolInfo.matchingVolume = data.matchedVol;
  // symbolInfo.referencePrice = data.refPrice;
  // symbolInfo.averagePrice = data.avgPrice;
  // symbolInfo.ceilingPrice = data.ceiling;
  // symbolInfo.floorPrice = data.floor;
  // symbolInfo.open = data.open;
  // symbolInfo.high = data.high;
  // symbolInfo.low = data.low;
  if (symbolInfo.oddlotBidOfferList == null) {
    symbolInfo.oddlotBidOfferList = [{}, {}, {}];
  }
  const bidOffer1: IBidOfferItem = {};
  if (data.bid1 && symbolInfo.oddlotBidOfferList[0].bidPrice !== data.bid1) {
    bidOffer1.bidPrice = data.bid1;
  }
  if (data.bid1Size && symbolInfo.oddlotBidOfferList[0].bidVolume !== data.bid1Size) {
    bidOffer1.bidVolume = data.bid1Size;
  }
  if (data.offer1 && symbolInfo.oddlotBidOfferList[0].offerPrice !== data.offer1) {
    bidOffer1.offerPrice = data.offer1;
  }
  if (data.offer1Size && symbolInfo.oddlotBidOfferList[0].offerVolume !== data.offer1Size) {
    bidOffer1.offerVolume = data.offer1Size;
  }
  const bidOffer2: IBidOfferItem = {};
  if (data.bid2 && symbolInfo.oddlotBidOfferList[1].bidPrice !== data.bid2) {
    bidOffer2.bidPrice = data.bid2;
  }
  if (data.bid2Size && symbolInfo.oddlotBidOfferList[1].bidVolume !== data.bid2Size) {
    bidOffer2.bidVolume = data.bid2Size;
  }
  if (data.offer2 && symbolInfo.oddlotBidOfferList[1].offerPrice !== data.offer2) {
    bidOffer2.offerPrice = data.offer2;
  }
  if (data.offer2Size && symbolInfo.oddlotBidOfferList[1].offerVolume !== data.offer2Size) {
    bidOffer2.offerVolume = data.offer2Size;
  }
  const bidOffer3: IBidOfferItem = {};
  if (data.bid3 && symbolInfo.oddlotBidOfferList[2].bidPrice !== data.bid3) {
    bidOffer3.bidPrice = data.bid3;
  }
  if (data.bid3Size && symbolInfo.oddlotBidOfferList[2].bidVolume !== data.bid3Size) {
    bidOffer3.bidVolume = data.bid3Size;
  }
  if (data.offer3 && symbolInfo.oddlotBidOfferList[2].offerPrice !== data.offer3) {
    bidOffer3.offerPrice = data.offer3;
  }
  if (data.offer3Size && symbolInfo.oddlotBidOfferList[2].offerVolume !== data.offer3Size) {
    bidOffer3.offerVolume = data.offer3Size;
  }
  if (
    bidOffer1 !== symbolInfo.oddlotBidOfferList[0] ||
    bidOffer2 !== symbolInfo.oddlotBidOfferList[1] ||
    bidOffer3 !== symbolInfo.oddlotBidOfferList[2]
  ) {
    bidOfferOddLot.bidOfferList = [];
    bidOfferOddLot.bidOfferList.push(bidOffer1);
    bidOfferOddLot.bidOfferList.push(bidOffer2);
    bidOfferOddLot.bidOfferList.push(bidOffer3);
  }
  // symbolInfo.expectedPrice = data.projectOpen;
  // symbolInfo.foreignerBuyVolume = data.foreignBuyVol;
  // symbolInfo.foreignerSellVolume = data.foreignSellVol;
  // symbolInfo.totalOfferVolume = data.totalOfferSize;
  // symbolInfo.totalBidVolume = data.totalBidSize;
  // symbolInfo.totalBidValue = data.nBid;
  // symbolInfo.totalOfferValue = data.nOffer;
  return bidOfferOddLot;
};
