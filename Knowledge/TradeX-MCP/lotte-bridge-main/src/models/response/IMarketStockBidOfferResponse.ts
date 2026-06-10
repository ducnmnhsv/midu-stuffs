export interface IMarketStockBidOfferResponse {
  s: string; // Mã cổ phiếu (symbol), ví dụ: "FPT"
  ce: number; // Giá trần (ceiling)
  fl: number; // Giá sàn (floor)
  re: number; // Giá tham chiếu (reference)
  a: number; // Giá khớp trung bình?
  o: number; // Giá mở cửa (open)
  h: number; // Giá cao nhất (high)
  l: number; // Giá thấp nhất (low)
  c: number; // Giá đóng cửa / cuối cùng (close / last)
  ch: number; // Thay đổi giá (change)
  ra: number; // Tỷ lệ thay đổi (%) (change rate)
  ss: string; // Trạng thái giao dịch (session status), ví dụ: "LO"
  bot: string; // Thời gian cập nhật dữ liệu (hhmmss)
  tb: number; // Tổng khối lượng bên mua (total bid size)
  to: number; // Tổng khối lượng bên bán (total offer size)
  m: string; // Tên thị trường (market), ví dụ: "HOSE"
  mv: number; // Khối lượng khớp (matched volume)
  bb: IBidOfferSimple[]; // Danh sách bên mua (bid book)
  bo: IBidOfferSimple[]; // Danh sách bên bán (offer book)
}

export interface IBidOfferSimple {
  p: number; // Price
  v: number; // Volume
}
