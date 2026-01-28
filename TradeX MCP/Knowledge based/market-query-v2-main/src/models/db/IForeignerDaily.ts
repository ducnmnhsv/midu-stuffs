export interface IForeignerDaily {
  _id?: string;
  code?: string;
  foreignerBuyVolume?: number;
  foreignerSellVolume?: number;
  foreignerBuyValue?: number;
  foreignerSellValue?: number;
  foreignerTotalRoom?: number;
  foreignerCurrentRoom?: number;
  foreignerBuyAbleRatio?: number;
  foreignerChangeVolume?: number;
  foreignerHoldVolume?: number;
  foreignerHoldRatio?: number;
  date?: Date;
}
