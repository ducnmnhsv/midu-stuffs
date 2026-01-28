export interface IAdvertiseData {
  _id?: string;
  code?: string;
  time?: string;
  secId?: string;
  traderId?: string;
  sellBuyType?: string;
  price?: number;
  quantity?: number;
  ptVolume?: number;
  ptValue?: number;
  contact?: string;
  isCancel?: boolean;
  date?: Date;
  marketType?: string;
}
