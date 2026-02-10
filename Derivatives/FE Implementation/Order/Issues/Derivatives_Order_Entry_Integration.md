# FE Issue: Tích hợp màn đặt lệnh / hủy / sửa lệnh Derivatives

> **Module:** Order  
> **Screens:** Đặt lệnh (Place order), Danh sách lệnh chờ khớp (Unmatch), Hủy/Sửa lệnh  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-10

---

## 1. Background

Backend **Regular Orders (Derivatives)** đã live: đặt lệnh (Buy/Sell), hủy lệnh, sửa lệnh, xem lệnh chờ khớp. FE cần đảm bảo **màn đặt lệnh và quản lý lệnh** hỗ trợ **symbol Derivatives** (VN30F*, 41B*, …) qua đúng API và hiển thị đúng response/error.

**Phạm vi:**

- Cho **symbol Derivatives** (`t === "FUTURES"` hoặc `m === "derivatives"`): gọi API Derivatives (`/api/v1/derivatives/order*`).
- Cho symbol Equity: giữ nguyên flow hiện tại (API equity).
- Response format: không có `success: true/false`; dùng HTTP status + `message` / `orderNumber` (success) hoặc `code` + `message` / `params` (error).

---

## 2. Data Source

- **API Spec:** [Regular_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md)
- **Planning:** [Order/README](../../../Planning%20documentation/Order/README.md), [01_Regular_Orders_Business](../../../Planning%20documentation/Order/Planning/01_Regular_Orders_Business.md)

**Endpoints:**

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place Order | POST | `/api/v1/derivatives/order` |
| Modify Order | PUT | `/api/v1/derivatives/order/modify` |
| Cancel Order | PUT | `/api/v1/derivatives/order/cancel` |
| Query Unmatch | GET | `/api/v1/derivatives/order/todayUnmatch` |

**Place order – Success (200):**
```json
{
  "message": "[V0307] Bạn đã thực hiện lệnh Mua...",
  "orderNumber": "2026020500012"
}
```

**Error (4xx):**
```json
{
  "code": "ORDER_PLACE_1005",
  "message": "[V3120] Không đủ ký quỹ..."
}
```
hoặc validation:
```json
{
  "code": "INVALID_PARAMETER",
  "params": [{ "code": "FIELD_IS_REQUIRED", "param": "accountNumber" }]
}
```

**Order types:** LO, ATO, ATC, MOK, MAK, MTL (map theo spec). Price bắt buộc với LO; null với MOK, MAK, MTL.

---

## 3. Screens & Components (FE reference)

| Vị trí | Mô tả |
|--------|--------|
| **Order entry** | Màn đặt lệnh: khi symbol là Derivatives, gọi `POST /api/v1/derivatives/order` với body đúng spec (accountNumber, symbolCode, sellBuyType, orderType, orderPrice, orderQuantity, deviceUniqueId). Hiển thị `message` + `orderNumber` khi thành công. |
| **Unmatch list** | Danh sách lệnh chờ khớp: với Derivatives, gọi `GET /api/v1/derivatives/order/todayUnmatch` (accountNumber, symbol…). Hiển thị danh sách, nút Hủy / Sửa. |
| **Cancel** | Gọi `PUT /api/v1/derivatives/order/cancel` với orderNumber (và params theo spec). Hiển thị message success/error. |
| **Modify** | Gọi `PUT /api/v1/derivatives/order/modify` với orderNumber, orderPrice, orderQuantity (theo spec). Hiển thị message success/error. |

**FE repo (read-only):**

- Order screens: `src/screens/` (OrderScreen, PlaceOrderScreen, UnmatchOrdersScreen, …).
- API client / redux: `src/reduxs/` hoặc `src/services/` – tách request Derivatives (`/api/v1/derivatives/order*`) khỏi Equity.
- Interfaces: `src/interfaces/` – request/response types theo spec (không dùng `success`; dùng `message`, `orderNumber`, `code`, `params`).

---

## 4. Acceptance Criteria

- [ ] **AC1 – Phân biệt symbol**  
  Khi user chọn symbol Derivatives (VN30F*, 41B*, …), FE gọi API Derivatives. Khi chọn symbol Equity, giữ flow API Equity hiện tại.

- [ ] **AC2 – Đặt lệnh (Place)**  
  Gửi đúng body theo [Regular_Orders_API_Spec](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md): accountNumber, symbolCode, sellBuyType, orderType, orderPrice (null nếu MOK/MAK/MTL), orderQuantity, deviceUniqueId. Header: Authorization, Content-Type, Accept-Language.

- [ ] **AC3 – Hiển thị kết quả đặt lệnh**  
  Success (200): hiển thị `message` (từ Lotte) và `orderNumber` cho user. Không dựa vào field `success`.

- [ ] **AC4 – Xử lý lỗi**  
  Lỗi 4xx/5xx: hiển thị `message` (pass-through từ Lotte) hoặc `params` (validation). Không ẩn message; hỗ trợ đa ngôn ngữ qua Accept-Language.

- [ ] **AC5 – Hủy / Sửa lệnh**  
  Với lệnh Derivatives chờ khớp: Hủy gọi cancel API, Sửa gọi modify API với params đúng spec. Hiển thị message success/error.

- [ ] **AC6 – Lệnh chờ khớp (Unmatch)**  
  Màn danh sách lệnh chờ khớp: với tài khoản/symbol Derivatives, gọi GET todayUnmatch Derivatives và hiển thị đúng định dạng (orderNumber, symbol, side, price, quantity, orderType, …).

- [ ] **AC7 – Validation FE**  
  Trước khi gọi API: kiểm tra required (accountNumber, symbolCode, sellBuyType, orderType, orderQuantity); với LO kiểm tra orderPrice; với MOK/MAK/MTL không gửi giá hoặc gửi null theo spec.

---

## 5. Figma & References

- **API Spec:** [Regular_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md)
- **Business:** [01_Regular_Orders_Business.md](../../../Planning%20documentation/Order/Planning/01_Regular_Orders_Business.md)

---

## 6. Notes for Dev

- **Auto-populated:** userId, name, identifierNumber, sourceIp do BE lấy từ JWT và request IP; FE không gửi trong body.
- **deviceUniqueId:** Bắt buộc; map sang Lotte `cli_mac_addr` (device ID theo convention app).
- **Order type mapping:** LO→2, ATO→3, MAK→4, MOK→5, ATC→6, MTL→9 (BE map; FE gửi mã TradeX: LO, ATO, …).
- Có thể dùng chung component form đặt lệnh cho Equity và Derivatives, chỉ khác endpoint và body field names (symbolCode vs symbol, …) theo từng API.
