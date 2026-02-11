# [Epic DR-FE-ORD] Story ORD.S2: Tích hợp màn đặt lệnh / hủy / sửa lệnh Derivatives

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-ORD – Derivatives Order FE  
> **Module:** Order  
> **Screens:** Đặt lệnh (Place order), Danh sách lệnh chờ khớp (Unmatch), Hủy/Sửa lệnh  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-10

---

## User Story

**As a** trader  
**I want to** đặt lệnh, xem lệnh chờ khớp, hủy và sửa lệnh cho mã Derivatives (VN30F*, 41B*, …) qua đúng API và thấy kết quả/lỗi rõ ràng  
**So that** tôi giao dịch phái sinh trong cùng app với flow nhất quán và thông báo đúng từ sàn.

---

## Acceptance Criteria

- [ ] **AC-01** Khi user chọn symbol Derivatives, FE gọi API Derivatives (`/api/v1/derivatives/order*`). Khi chọn symbol Equity, giữ flow API Equity hiện tại.
- [ ] **AC-02** Đặt lệnh: Gửi đúng body theo [Regular_Orders_API_Spec](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md): accountNumber, symbolCode, sellBuyType, orderType, orderPrice (null nếu MOK/MAK/MTL), orderQuantity, deviceUniqueId. Header: Authorization, Content-Type, Accept-Language.
- [ ] **AC-03** Success (200): hiển thị `message` (từ Lotte) và `orderNumber` cho user. Không dựa vào field `success`.
- [ ] **AC-04** Lỗi 4xx/5xx: hiển thị `message` (pass-through từ Lotte) hoặc `params` (validation). Hỗ trợ đa ngôn ngữ qua Accept-Language.
- [ ] **AC-05** Với lệnh Derivatives chờ khớp: Hủy gọi cancel API, Sửa gọi modify API với params đúng spec. Hiển thị message success/error.
- [ ] **AC-06** Màn lệnh chờ khớp: với tài khoản/symbol Derivatives, gọi GET todayUnmatch Derivatives và hiển thị đúng định dạng (orderNumber, symbol, side, price, quantity, orderType, …).
- [ ] **AC-07** Trước khi gọi API: kiểm tra required (accountNumber, symbolCode, sellBuyType, orderType, orderQuantity); với LO kiểm tra orderPrice; với MOK/MAK/MTL không gửi giá hoặc gửi null theo spec.

---

## Tasks (Implementation)

- [ ] **T1** Phân biệt symbol Derivatives vs Equity; routing gọi API Derivatives (`/api/v1/derivatives/order*`) khi symbol là Derivatives.
- [ ] **T2** Place order: POST `/api/v1/derivatives/order` với body đúng spec; xử lý response (message, orderNumber) và error (code, message, params); types không dùng `success`.
- [ ] **T3** Unmatch list: GET `/api/v1/derivatives/order/todayUnmatch`; hiển thị danh sách; nút Hủy/Sửa.
- [ ] **T4** Cancel: PUT `/api/v1/derivatives/order/cancel`; Modify: PUT `/api/v1/derivatives/order/modify`; hiển thị message success/error.
- [ ] **T5** Validation FE: required fields; LO → orderPrice bắt buộc; MOK/MAK/MTL → price null theo spec.

---

## Background / Context

Backend **Regular Orders (Derivatives)** đã live: đặt lệnh, hủy, sửa, xem lệnh chờ khớp. FE cần màn đặt lệnh và quản lý lệnh hỗ trợ **symbol Derivatives** qua đúng API. Response: không có `success`; dùng HTTP status + `message`/`orderNumber` (success) hoặc `code`/`message`/`params` (error).

---

## Data Source

- **API Spec:** [Regular_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md)
- **Planning:** [Order/README](../../../Planning%20documentation/Order/README.md), [01_Regular_Orders_Business](../../../Planning%20documentation/Order/Planning/01_Regular_Orders_Business.md)

**Endpoints:**

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place Order | POST | `/api/v1/derivatives/order` |
| Modify Order | PUT | `/api/v1/derivatives/order/modify` |
| Cancel Order | PUT | `/api/v1/derivatives/order/cancel` |
| Query Unmatch | GET | `/api/v1/derivatives/order/todayUnmatch` |

**Place order – Success (200):** `{ "message": "...", "orderNumber": "..." }`  
**Error:** `{ "code": "ORDER_PLACE_1005", "message": "..." }` hoặc `{ "code": "INVALID_PARAMETER", "params": [...] }`  
**Order types:** LO, ATO, ATC, MOK, MAK, MTL (price bắt buộc với LO; null với MOK, MAK, MTL).

---

## Screens & Components (FE reference)

| Vị trí | Mô tả |
|--------|--------|
| Order entry | Symbol Derivatives → POST order với body đúng spec; hiển thị message + orderNumber khi thành công. |
| Unmatch list | Derivatives → GET todayUnmatch; danh sách + nút Hủy/Sửa. |
| Cancel / Modify | Gọi cancel/modify API; hiển thị message success/error. |

**FE repo (read-only):** Order screens: `src/screens/`; API/redux: `src/reduxs/` hoặc `src/services/`; interfaces: `src/interfaces/`.

---

## Technical Notes

- Auto-populated: userId, name, identifierNumber, sourceIp do BE lấy từ JWT và request IP; FE không gửi.
- deviceUniqueId: Bắt buộc; map sang Lotte `cli_mac_addr`.
- Order type mapping: LO→2, ATO→3, … (BE map); FE gửi mã TradeX: LO, ATO, …
- Có thể dùng chung form cho Equity và Derivatives, khác endpoint và body field names.

---

## References

- [Regular_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md)
- [01_Regular_Orders_Business.md](../../../Planning%20documentation/Order/Planning/01_Regular_Orders_Business.md)
