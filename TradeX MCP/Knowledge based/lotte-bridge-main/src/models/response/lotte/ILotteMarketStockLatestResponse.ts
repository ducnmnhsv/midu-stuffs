import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteMarketStockLatestResponse extends ILotteCommonResponse {
  data_list: ILotteMarketStockLatestList[];
}

export type ControlCode =
  // HOSE
  | 'P' // ATO
  | 'O' // CONTINUOUS
  | 'I' // INTERMISSION
  | 'A' // ATC
  | 'C' // PL
  | 'K' // CLOSED
  | 'G' // CLOSED
  // HNX/UPCOM
  | '2'; // INTERMISSION

export interface ILotteMarketStockLatestList {
  list: ILotteMarketStockLatestData[];
}

export interface ILotteMarketStockLatestData {
  code: string; // Mã chứng khoán
  ceiling: string;
  floor: string;
  name: string; // Tên mã
  open: string; // Giá mở cửa
  high: string; // Giá cao nhất
  low: string; // Giá thấp nhất
  last: string; // Giá cuối cùng
  avgPrice: string; // Giá trung bình
  change: string; // Chênh lệch giá
  changeRate: string; // Tỷ lệ thay đổi
  volume: string; // Khối lượng giao dịch
  amount: string; // Giá trị giao dịch
  turnoverRatio: string; // Tỷ lệ quay vòng
  time: string; // Thời gian cập nhật (hhmmss)
  high52: string; // Giá cao nhất 52 tuần
  low52: string; // Giá thấp nhất 52 tuần
  totalAmt: string; // Tổng giá trị giao dịch
  controlCode: ControlCode; // Mã phiên giao dịch (theo sàn)
  foreignBuyVol: string; // Khối lượng mua của NĐTNN
  foreignSellVol: string; // Khối lượng bán của NĐTNN
  foreignTotalRoom: string; // Tổng room nước ngoài
  foreignCurrRoom: string; // Room nước ngoài còn lại
  projectOpen: string; // Mở cửa dự kiến
  ptAmt: string; // Giá trị giao dịch thỏa thuận
  ptVol: string; // Khối lượng giao dịch thỏa thuận
  listedStockQty: string; // Số lượng cổ phiếu niêm yết
}
