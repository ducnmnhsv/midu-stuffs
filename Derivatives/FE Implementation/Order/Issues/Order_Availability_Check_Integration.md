# [Epic DR-FE-ORD] Story ORD.S1: Tích hợp Order Availability Check (Max quantity) khi đặt lệnh Derivatives

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-ORD – Derivatives Order FE  
> **Module:** Order  
> **Screens:** Màn đặt lệnh (Order entry / Place order)  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-10

---

## User Story

**As a** trader  
**I want to** biết số lượng tối đa có thể đặt (và thanh khoản nếu có) khi đặt lệnh Derivatives  
**So that** tôi không nhập vượt quá khả năng và thấy rõ giới hạn trước khi gửi lệnh.

---

## Acceptance Criteria

- [ ] **AC-01** Khi user mở màn đặt lệnh và chọn symbol Derivatives + account + side (BUY/SELL), FE gọi `GET /api/v1/derivatives/order/checkAvailability` với `accountNumber`, `symbol`, `sellBuyType` tương ứng.
- [ ] **AC-02** Số lượng tối đa nhập được (hoặc max của slider/stepper) không vượt quá `availableQuantity` trả về từ API. Nếu user nhập > availableQuantity, hiển thị lỗi validation và không cho submit.
- [ ] **AC-03** Nếu API trả lỗi (4xx/5xx), hiển thị message từ BE cho user; không block toàn bộ màn hình (cho phép user thử lại hoặc đổi account/symbol).
- [ ] **AC-04** Gửi header `Accept-Language` theo ngôn ngữ app (vi/en). Message lỗi từ BE đã đa ngôn ngữ.
- [ ] **AC-05** Logic check availability chỉ chạy khi symbol là Derivatives (`t === "FUTURES"` hoặc `m === "derivatives"`). Màn đặt lệnh Equity không gọi API này.
- [ ] **AC-06** (Optional) Nếu FE gọi API không truyền `sellBuyType`, nhận response dạng `{ buy: {...}, sell: {...} }` và dùng đúng phần theo side hiện tại.

---

## Tasks (Implementation)

- [ ] **T1** Thêm API client: `GET /api/v1/derivatives/order/checkAvailability` (params: accountNumber, symbol, sellBuyType); types cho response `{ availableQuantity, availableLiquidity }` và `{ buy, sell }`.
- [ ] **T2** Order entry: khi chọn symbol Derivatives + account + side, gọi checkAvailability; lưu kết quả vào state (availableQuantity, availableLiquidity).
- [ ] **T3** Quantity field: set max value = availableQuantity; validation không cho submit nếu quantity > availableQuantity; hiển thị hint "Tối đa: X lot".
- [ ] **T4** (Optional) Hiển thị availableLiquidity (tooltip hoặc dòng nhỏ). Xử lý lỗi API: hiển thị message, nút thử lại.

---

## Background / Context

Trước khi đặt lệnh Derivatives, trader cần biết **số lượng tối đa** có thể đặt (margin, position limits, thanh khoản). Backend cung cấp API **Order Availability Check** trả về `availableQuantity` và `availableLiquidity`. Chỉ áp dụng cho **Derivatives**; màn đặt lệnh Equity không đổi.

---

## Data Source

- **API:** `GET /api/v1/derivatives/order/checkAvailability`
- **Query params:** `accountNumber` (required), `symbol` (required), `sellBuyType` (optional: `BUY` | `SELL`)
- **Spec:** [Order_Availability_Check_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md)

**Response (có sellBuyType):** `{ "availableQuantity": 100, "availableLiquidity": 150 }`  
**Response (không sellBuyType):** `{ "buy": { ... }, "sell": { ... } }`

---

## Screens & Components (FE reference)

| Vị trí | Mô tả |
|--------|--------|
| Order entry | Khi chọn symbol Derivatives + account + side, gọi checkAvailability. |
| Quantity field | Max = availableQuantity; hint "Tối đa: X lot"; validation. |
| Optional | Hiển thị availableLiquidity (tooltip/dòng nhỏ). |

**FE repo (read-only):** Order/Place order: `src/screens/`; API: `src/reduxs/` hoặc `src/services/`; types: `src/interfaces/`.

---

## Technical Notes

- Gọi check availability sau khi user chọn account + symbol + side; có thể gọi khi mở màn hoặc khi thay đổi; cân nhắc debounce.
- Không cache kết quả lâu; có thể refetch khi quay lại tab hoặc sau vài giây.
- Gửi `Authorization: Bearer {JWT}` như các API TradeX khác.

---

## References

- [Order_Availability_Check_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md)
- [Order/README](../../../Planning%20documentation/Order/README.md)
