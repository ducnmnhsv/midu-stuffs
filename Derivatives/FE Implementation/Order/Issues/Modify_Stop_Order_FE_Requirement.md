# [Epic DR-FE-ORD] Story ORD.Sn: Modify Stop Order – FE Implementation

> **Jira:** (key khi tạo, e.g. NHMTS-xxx)  
> **Epic:** DR-FE-ORD  
> **Module:** Order  
> **Priority:** P1  
> **Status:** 📋 Ready for FE

---

## User Story

**As a** Trader (người dùng Derivatives trên NHSV Pro)  
**I want to** sửa lệnh điều kiện (stop order) đã đặt – trigger price, giá đặt, số lượng, ngày hiệu lực – từ màn Orderbook  
**So that** tôi có thể điều chỉnh lệnh stop mà không cần hủy và đặt lại.

---

## Background

Trader xem sổ lệnh / lịch sử lệnh Derivatives trên màn **Orderbook**. Khi có **lệnh điều kiện (stop order)** đang chờ, trader bấm **Edit** để mở màn **Modify stop order**. Màn này dùng chung phần layout cao cấp với Modify normal order (thông tin mã, hai tab Bid/Ask và Chart) nhưng **form sửa lệnh** khác: gồm **Trigger price**, **Order price** (giá trong khoảng CE–RE), **Quantity** (tối đa theo checkAvailability, Buy/Sell theo lệnh gốc), **Affected date** (một ngày, không chọn quá khứ, mở date picker khi chọn), và **Max quantity** (read-only từ checkAvailability). Backend cung cấp cùng nhóm API Derivatives: **order/history**, **order/checkAvailability**, **order/modify** (request body riêng cho stop order). FE có thể tham khảo màn **ModifyStopOrder** (Equity) tại `src/screens/ModifyStopOrder/` nhưng phải dùng endpoint Derivatives và bám design Figma. Entry point là nút **Edit** trên một dòng **stop order** trong Orderbook; sau khi sửa thành công, hiển thị message và orderNumber trả về từ API.

---

## Acceptance Criteria

- [ ] **AC-01: Điều hướng từ Orderbook sang màn Modify stop order**  
  Khi user bấm nút **Edit** trên một dòng lệnh **stop order** (lệnh điều kiện) trong màn Orderbook (sổ lệnh Derivatives), app điều hướng tới màn **Modify stop order** và truyền đủ thông tin lệnh cần sửa qua navigation params (ví dụ orderId hoặc object lệnh từ API history). Màn Modify stop order chỉ mở khi Edit được bấm trên lệnh stop; không mở khi bấm Edit trên lệnh thường (normal order).

- [ ] **AC-02: Hiển thị layout chung và tab Bid/Ask, Chart theo Figma**  
  Màn Modify stop order có phần **layout chung** giống Modify normal order: thông tin mã (giá hiện tại, change, change rate; CE/FL/RE; vol, basis), hai tab **Bid/Ask** và **Chart** (mặc định Bid/Ask). Tab **Bid/Ask** hiển thị bid/ask top 3; khi chọn tab **Chart** thì hiển thị phần chart thay cho Bid/Ask theo Figma (node 40006583-230683). Giao diện bám design NHSV Pro.

- [ ] **AC-03: Form sửa lệnh – Trigger price, Order price, Quantity, Affected date, Max quantity**  
  **Trigger price:** Có field nhập giá kích hoạt; validation theo spec Derivatives (ví dụ so với giá hiện tại nếu Backend yêu cầu). **Order price:** Giá đặt khi lệnh đã kích hoạt; **bắt buộc** nằm trong khoảng **CE – RE** của mã; nếu nằm ngoài khoảng thì hiển thị toast/error “Giá nằm ngoài khoảng …” thống nhất với các màn khác. **Quantity:** Lớn hơn hoặc bằng 0; tối đa = kết quả từ `GET /api/v1/derivatives/order/checkAvailability` (Buy/Sell theo lệnh gốc); không cho nhập vượt quá. **Affected date:** Chỉ cho chọn **một ngày**; **không** cho chọn ngày trong quá khứ, chỉ ngày hiện tại hoặc tương lai; ngày bắt đầu = ngày kết thúc (một ngày duy nhất). Khi user chọn ngày thì mở **date picker modal** theo design. **Max quantity:** Chỉ đọc; hiển thị sau khi gọi checkAvailability thành công, theo hướng Buy/Sell của lệnh gốc.

- [ ] **AC-04: Validation Affected date – không quá khứ, một ngày**  
  Date picker **chặn** chọn ngày trong quá khứ (disable hoặc ẩn). Chỉ cho chọn **một ngày** (start date = end date). Sau khi user xác nhận trong date picker, giá trị hiển thị trên form là ngày đã chọn và được gửi trong request modify (format theo spec Backend, ví dụ yyyyMMdd).

- [ ] **AC-05: Thông tin lệnh gốc từ API order/history**  
  Phần thông tin lệnh gốc (Buy/Sell, giá đặt gốc, quantity, loại Stop order) lấy từ API `GET /api/v1/derivatives/order/history` (hoặc endpoint chi tiết lệnh theo spec). Có xử lý loading và lỗi; khi có dữ liệu thì hiển thị đúng để user biết lệnh đang sửa.

- [ ] **AC-06: Nút Cancel và nút Modify; hiển thị kết quả**  
  **Cancel:** Bấm **Cancel** → đóng màn, không gọi API modify. **Modify:** Nút **Modify** chỉ **enable** khi có ít nhất một thay đổi so với lệnh gốc (trigger price, order price, quantity hoặc affected date). Khi bấm **Modify**: (1) Gọi `POST /api/v1/derivatives/order/modify` với payload dành cho stop order (trigger price, order price, quantity, affected date, orderId/params theo spec). (2) Khi thành công, API trả về `message` và `orderNumber` → hiển thị **message** và **orderNumber** trên màn (toast hoặc inline). Khi API lỗi thì hiển thị thông báo lỗi từ response.

- [ ] **AC-07: Tích hợp API Derivatives và tách biệt với Equity**  
  Màn chỉ gọi endpoint **Derivatives**: `/api/v1/derivatives/order/history`, `/api/v1/derivatives/order/checkAvailability`, `POST /api/v1/derivatives/order/modify` (body cho stop order). Request/response theo spec Backend. Không ảnh hưởng flow **ModifyStopOrder** Equity (vẫn dùng API equity).

---

## Tasks (Implementation)

- [ ] **T1** Thêm/xác định route và params màn Modify stop order (Derivatives); xử lý navigate từ Orderbook khi bấm Edit (chỉ với stop order).
- [ ] **T2** Implement layout chung (symbol info, tab Bid/Ask & Chart) tái dùng hoặc đồng bộ với Modify normal order.
- [ ] **T3** Implement form: Trigger price, Order price (validate CE–RE), Quantity (max từ checkAvailability), Affected date (date picker, một ngày, không quá khứ), Max quantity (read-only).
- [ ] **T4** Gọi order/history và binding thông tin lệnh gốc; gọi checkAvailability và hiển thị Max quantity.
- [ ] **T5** Gọi order/modify khi bấm Modify; hiển thị message và orderNumber khi thành công; xử lý lỗi.

---

## Technical Notes

- **APIs:** `GET /api/v1/derivatives/order/history`, `GET /api/v1/derivatives/order/checkAvailability`, `POST /api/v1/derivatives/order/modify` (request body cho stop order theo spec).
- **FE tham khảo:** `src/screens/ModifyStopOrder/` (Equity) – Trigger price, Order price, Quantity, From/To date; **không** dùng API equity trong màn Derivatives.
- **Date:** Affected date = một ngày; format gửi API theo spec (ví dụ yyyyMMdd).

---

## References

- Figma – Modify order (layout chung + form): [NHSV-Pro – Modify order](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104) (node `40005008-236104`).
- Figma – Tab Chart: [NHSV-Pro – Modify order Chart](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683) (node `40006583-230683`).
- [Stop Orders API Spec](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md) – Modify/Cancel stop order.
- [Order Availability Check API Spec](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md)

---

**Document Status:** 📋 Ready for FE  
**Next Steps:** Backend cung cấp chi tiết API order/history và modify cho stop order; FE triển khai theo AC.
