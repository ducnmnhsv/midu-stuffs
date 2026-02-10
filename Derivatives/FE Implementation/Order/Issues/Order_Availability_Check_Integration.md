# FE Issue: Tích hợp Order Availability Check (Max quantity) khi đặt lệnh Derivatives

> **Module:** Order  
> **Screens:** Màn đặt lệnh (Order entry / Place order)  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-10

---

## 1. Background

Trước khi đặt lệnh Derivatives, trader cần biết **số lượng tối đa** có thể đặt (dựa trên margin, position limits, thanh khoản). Backend cung cấp API **Order Availability Check** (DRORD-028) trả về `availableQuantity` và `availableLiquidity`. FE cần gọi API này và sử dụng kết quả để:

- Hiển thị max quantity (hoặc giới hạn input quantity) trên form đặt lệnh.
- (Tùy chọn) Hiển thị available liquidity để trader tham khảo khi đặt lệnh lớn.

**Phạm vi:** Chỉ áp dụng cho **Derivatives** (symbol có `t === "FUTURES"` hoặc `m === "derivatives"`). Màn đặt lệnh Equity không đổi.

---

## 2. Data Source

- **API:** `GET /api/v1/derivatives/order/checkAvailability`
- **Query params:** `accountNumber` (required), `symbol` (required), `sellBuyType` (optional: `BUY` | `SELL`)
- **Planning doc:** [Order_Availability_Check_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md)

**Response khi có `sellBuyType`:**
```json
{
  "availableQuantity": 100,
  "availableLiquidity": 150
}
```

**Response khi không truyền `sellBuyType`:** BE gọi Lotte 2 lần (BUY + SELL), trả về:
```json
{
  "buy": { "availableQuantity": 100, "availableLiquidity": 150 },
  "sell": { "availableQuantity": 50, "availableLiquidity": 200 }
}
```

**Error:** Theo chuẩn TradeX (`code`, `message` hoặc `params`). Pass-through message từ Lotte (đa ngôn ngữ theo `Accept-Language`).

---

## 3. Screens & Components (FE reference)

| Vị trí | Mô tả |
|--------|--------|
| **Order entry** | Màn đặt lệnh Derivatives (Buy/Sell). Khi user chọn symbol Derivatives + account + side (BUY/SELL), FE gọi `checkAvailability` với `accountNumber`, `symbol`, `sellBuyType`. |
| **Quantity field** | Dùng `availableQuantity` để: (1) set max value cho input quantity, và/hoặc (2) hiển thị hint "Tối đa: X lot". Validation: không cho submit nếu quantity > availableQuantity. |
| **Optional** | Hiển thị `availableLiquidity` (ví dụ tooltip hoặc dòng nhỏ) để user biết thanh khoản thị trường. |

**FE repo (read-only):**

- Order/Place order screens: `src/screens/` (tìm màn đặt lệnh theo convention dự án, ví dụ OrderScreen, PlaceOrderScreen, DerivativesOrderScreen).
- API client: `src/reduxs/` hoặc `src/services/` – thêm request tới `/api/v1/derivatives/order/checkAvailability`.
- Interfaces: `src/interfaces/` – thêm type cho response `{ availableQuantity, availableLiquidity }` và nested `{ buy, sell }`.

---

## 4. Acceptance Criteria

- [ ] **AC1 – Gọi API khi vào màn đặt lệnh Derivatives**  
  Khi user mở màn đặt lệnh và chọn symbol Derivatives + account + side (BUY/SELL), FE gọi `GET /api/v1/derivatives/order/checkAvailability` với `accountNumber`, `symbol`, `sellBuyType` tương ứng.

- [ ] **AC2 – Dùng availableQuantity**  
  Số lượng tối đa nhập được (hoặc max của slider/stepper) không vượt quá `availableQuantity` trả về từ API. Nếu user nhập > availableQuantity, hiển thị lỗi validation và không cho submit.

- [ ] **AC3 – Xử lý lỗi API**  
  Nếu API trả lỗi (4xx/5xx), hiển thị message từ BE (`message` hoặc `params`) cho user; không block toàn bộ màn hình (cho phép user thử lại hoặc đổi account/symbol).

- [ ] **AC4 – Ngôn ngữ**  
  Gửi header `Accept-Language` theo ngôn ngữ app (vi/en). Message lỗi từ BE đã đa ngôn ngữ.

- [ ] **AC5 – Chỉ áp dụng Derivatives**  
  Logic check availability chỉ chạy khi symbol là Derivatives (`t === "FUTURES"` hoặc `m === "derivatives"`). Màn đặt lệnh Equity không gọi API này.

- [ ] **AC6 – (Optional) Nested buy/sell**  
  Nếu FE gọi API không truyền `sellBuyType`, nhận response dạng `{ buy: {...}, sell: {...} }` và dùng đúng phần theo side hiện tại.

---

## 5. Figma & References

- **API Spec:** [Order_Availability_Check_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md)
- **Order Planning:** [Order/README](../../../Planning%20documentation/Order/README.md) – Implementation status, Order flow

---

## 6. Notes for Dev

- **Timing:** Gọi check availability sau khi user đã chọn account + symbol + side; có thể gọi khi mở màn hình hoặc khi thay đổi symbol/side. Cân nhắc debounce nếu gọi theo từng thay đổi.
- **Caching:** Không cache kết quả lâu (margin và liquidity thay đổi); có thể refetch khi user quay lại tab hoặc sau vài giây.
- **Auth:** Gửi `Authorization: Bearer {JWT}` như các API TradeX khác.
