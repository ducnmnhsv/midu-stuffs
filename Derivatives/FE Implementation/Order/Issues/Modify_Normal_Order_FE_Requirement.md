# [Epic DR-FE-ORD] Story ORD.Sn: Modify Normal Order – FE Implementation

> **Jira:** (key khi tạo, e.g. NHMTS-xxx)  
> **Epic:** DR-FE-ORD  
> **Module:** Order  
> **Priority:** P1  
> **Status:** 📋 Ready for FE

---

## User Story

**As a** Trader (người dùng Derivatives trên NHSV Pro)  
**I want to** sửa lệnh thường đã đặt (thay đổi giá và/hoặc số lượng) từ màn Orderbook  
**So that** tôi có thể điều chỉnh lệnh chờ khớp mà không cần hủy và đặt lại.

---

## Background

Trader xem sổ lệnh / lịch sử lệnh Derivatives trên màn **Orderbook**. Khi có lệnh thường (normal order) đang chờ khớp, trader cần thao tác **Edit** để mở màn **Modify normal order**, tại đó có thể thay đổi **giá đặt** và **số lượng** trong giới hạn cho phép (giá trong khoảng trần–sàn, số lượng không vượt quá khả năng từ API checkAvailability). Backend cung cấp ba API Derivatives: **order/history** (lấy chi tiết lệnh gốc), **order/checkAvailability** (lấy số lượng tối đa có thể đặt theo hướng Buy/Sell), **order/modify** (gửi yêu cầu sửa lệnh). FE có thể tham khảo cấu trúc màn **ModifyOrderBook** (Equity) tại `src/screens/ModifyOrderBook/` nhưng phải dùng endpoint Derivatives `/api/v1/derivatives/order/...` và đảm bảo UI bám design Figma (symbol info, CE/FL/RE, vol, basis, hai tab Bid/Ask và Chart, form Price/Quantity/Max quantity, nút Cancel/Modify). Entry point là nút **Edit** trên một dòng lệnh thường trong Orderbook; sau khi sửa thành công, màn hình hiển thị message và orderNumber trả về từ API.

---

## Acceptance Criteria

- [ ] **AC-01: Điều hướng từ Orderbook sang màn Modify normal order**  
  Khi user bấm nút **Edit** trên một dòng lệnh **normal order** trong màn Orderbook (sổ lệnh / lịch sử lệnh Derivatives), app điều hướng tới màn **Modify normal order** và truyền đủ thông tin lệnh cần sửa (ví dụ orderId hoặc object lệnh từ API history) qua navigation params. Màn Modify normal order chỉ mở khi Edit được bấm trên lệnh thường, không mở khi bấm Edit trên lệnh điều kiện (stop order).

- [ ] **AC-02: Hiển thị layout và nội dung theo Figma – thông tin mã và tab**  
  Màn Modify normal order hiển thị đúng theo design: (1) **Thông tin mã:** giá hiện tại, change, change rate; giá CE, FL, RE; vol, basis. (2) **Hai tab:** Bid/Ask và Chart; **mặc định** chọn tab **Bid/Ask**. (3) Trên tab **Bid/Ask** hiển thị bid/ask của **top 3** giao dịch. (4) Khi user chọn tab **Chart**, vùng Bid/Ask được thay bằng phần chart theo Figma (node Chart). Toàn bộ bố cục, màu sắc, font theo file Figma NHSV Pro – Modify order (node-id 40005008-236104) và Modify order Chart (40006583-230683).

- [ ] **AC-03: Hiển thị thông tin lệnh gốc từ API order/history**  
  Phần “lệnh gốc” hiển thị: **Loại lệnh hiện tại** (Buy hoặc Sell), **Giá đặt lệnh gốc**, **Số lượng** (orderQuantity), **Loại lệnh** (Normal order). Dữ liệu lấy từ API `GET /api/v1/derivatives/order/history` (hoặc endpoint chi tiết lệnh theo spec Backend). Nếu chưa có dữ liệu (đang load), có xử lý loading/placeholder phù hợp; khi lỗi API thì hiển thị thông báo lỗi rõ ràng.

- [ ] **AC-04: Validation và nhập liệu Price**  
  **Price** (giá đặt sửa): (1) Bắt buộc có giá trị; nếu user để trống hoặc null thì hiển thị báo lỗi “không hợp lệ” (hoặc copy tương đương theo quy ước app). (2) Giá phải nằm trong khoảng **trần – sàn** của mã; nếu nằm ngoài khoảng này thì hiển thị **toast** (hoặc error message tương đương) với nội dung “Giá nằm ngoài khoảng trần - sàn”, đồng bộ với cách hiển thị lỗi giá ở các màn khác trong app. Không gửi request modify khi Price chưa hợp lệ.

- [ ] **AC-05: Validation và nhập liệu Quantity; hiển thị Max quantity**  
  **Quantity** (số lượng): (1) Không được nhỏ hơn 0. (2) Số lượng tối đa được phép nhập = kết quả từ API `GET /api/v1/derivatives/order/checkAvailability`; loại (Buy hay Sell) theo **lệnh gốc**. Nếu user nhập vượt quá số lượng này thì cần chặn hoặc báo lỗi rõ ràng (toast/validation message). **Max quantity** là field chỉ đọc (read-only): hiển thị số lượng tối đa ngay sau khi gọi checkAvailability thành công; không cho user nhập vượt quá giá trị này.

- [ ] **AC-06: Nút Cancel và nút Modify**  
  **Cancel:** Khi user bấm **Cancel**, màn Modify normal order đóng lại (pop/back), không gọi bất kỳ API modify nào. **Modify:** Nút **Modify** chỉ **enable** khi có ít nhất một thay đổi so với lệnh gốc (giá hoặc số lượng khác với giá/số lượng hiển thị từ order/history). Khi user bấm **Modify**: (1) Gọi API `POST /api/v1/derivatives/order/modify` với payload đúng spec (price, quantity, orderId/params theo Backend). (2) Khi API trả về thành công (có `message` và `orderNumber`), hiển thị **message** và **orderNumber** trên màn hình (toast hoặc inline theo UX hiện tại). Nếu API lỗi thì hiển thị thông báo lỗi từ response.

- [ ] **AC-07: Tích hợp API và không ảnh hưởng flow Equity**  
  Màn hình chỉ gọi endpoint **Derivatives**: `/api/v1/derivatives/order/history`, `/api/v1/derivatives/order/checkAvailability`, `/api/v1/derivatives/order/modify`. Request/response tuân thủ spec Backend khi có. Implementation không thay đổi hành vi màn Orderbook/Modify **Equity** (ví dụ `ModifyOrderBook` equity vẫn dùng API equity như cũ).

---

## Tasks (Implementation)

- [ ] **T1** Thêm/xác định route và params màn Modify normal order (Derivatives); xử lý navigate từ Orderbook khi bấm Edit (chỉ với normal order).
- [ ] **T2** Implement layout theo Figma: symbol info (giá, CE/FL/RE, vol, basis), hai tab Bid/Ask (top 3) và Chart.
- [ ] **T3** Gọi API order/history (hoặc chi tiết lệnh) và binding thông tin lệnh gốc lên UI.
- [ ] **T4** Implement form Price/Quantity với validation trần–sàn (Price) và max từ checkAvailability (Quantity); hiển thị Max quantity.
- [ ] **T5** Gọi API order/modify khi bấm Modify; hiển thị message và orderNumber khi thành công; xử lý lỗi.

---

## Technical Notes

- **APIs:** `GET /api/v1/derivatives/order/history`, `GET /api/v1/derivatives/order/checkAvailability`, `POST /api/v1/derivatives/order/modify`. Chi tiết request/response theo tài liệu Backend.
- **FE tham khảo:** `src/screens/ModifyOrderBook/` (Equity) – cấu trúc màn, input Price/Quantity, nút Cancel/Modify; **không** dùng API equity trong màn Derivatives.
- **Navigation:** `src/navigation/ScreenParamList.ts` – có thể thêm screen mới cho Derivatives Modify normal order hoặc dùng chung screen với context (derivatives vs equity).

---

## References

- Figma – Modify order (Normal): [NHSV-Pro – Modify order](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104) (node `40005008-236104`).
- Figma – Tab Chart: [NHSV-Pro – Modify order Chart](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683) (node `40006583-230683`).
- [Order Availability Check API Spec](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md)
- [Regular Orders API Spec](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md)

---

**Document Status:** 📋 Ready for FE  
**Next Steps:** Backend cung cấp chi tiết API order/history (request/response) cho lệnh thường; FE triển khai theo AC.
