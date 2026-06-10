export interface IHistoricalHighLow {
  h: number; // Giá cao nhất trong lịch sử
  l: number; // Giá thấp nhất trong lịch sử
}

export interface IForeignTrading {
  bv: number; // Khối lượng mua của NĐTNN
  sv: number; // Khối lượng bán của NĐTNN
  tr: number; // Tổng giá trị giao dịch của NĐTNN
  cr: number; // Tổng khối lượng còn lại
}

export interface IMarketStockLatestResponse {
  s: string; // Mã chứng khoán
  n: string; // Tên mã
  o: number; // Giá mở cửa
  h: number; // Giá cao nhất trong phiên
  l: number; // Giá thấp nhất trong phiên
  c: number; // Giá đóng cửa
  a: number; // Giá trung bình
  ch: number; // Chênh lệch giá so với tham chiếu
  ra: number; // Phần trăm thay đổi giá
  vo: number; // Khối lượng giao dịch
  va: number; // Giá trị giao dịch
  tor: number; // Tỷ lệ quay vòng
  bot: string; // Thời gian cập nhật dữ liệu (hhmmss)
  hly: IHistoricalHighLow[]; // Danh sách giá cao/thấp lịch sử
  mv: number; // Khối lượng khớp lệnh
  ss: string; // Phiên giao dịch
  fr: IForeignTrading[]; // Giao dịch nước ngoài
  ep: number; // Giá ước tính
  pva: number; // Giá trị giao dịch thỏa thuận
  pvo: number; // Khối lượng giao dịch thỏa thuận
  mc: number; // Vốn hóa thị trường
}
