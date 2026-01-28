export interface IDealNoticeData {
  _id?: string;
  code?: string;
  time?: string;
  confirmNumber?: string;
  matchVolume?: number;
  matchValue?: number;
  matchPrice?: number;
  ptVolume?: number;
  ptValue?: number;
  isCancel?: boolean;
  date?: Date;
  marketType?: string;
}
