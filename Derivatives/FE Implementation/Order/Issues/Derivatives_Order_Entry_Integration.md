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
- [ ] **AC-06** Màn lệnh chờ khớp: với tài khoản Derivatives đã chọn, gọi `GET /api/v1/derivatives/order/todayUnmatch?accountNumber={account}` và hiển thị danh sách theo response mapping (§6.3).
- [ ] **AC-07** Trước khi gọi API: kiểm tra required (accountNumber, symbolCode, sellBuyType, orderType, orderQuantity); với LO kiểm tra orderPrice; với MOK/MAK/MTL không gửi giá hoặc gửi null theo spec.
- [ ] **AC-08** Unmatch list **empty state:** Khi `orders.length === 0`, hiển thị thông báo *"Chưa có lệnh chờ khớp"* (VI) / *"No pending orders"* (EN). Không hiển thị bảng rỗng không có message.

### Navigate flow

- **Vào đặt lệnh:** Từ Current price (Buy/Sell) hoặc Watchlist / Search → chọn symbol Derivatives → navigate tới Trade tab với symbol + side pre-filled.
- **Vào Unmatch list:** Từ Trade tab (OrderHistoryInTrade) hoặc Order History (OrderHistory screen) → tab/mode Derivatives → danh sách lệnh chờ khớp.
- **Back:** Từ Order entry / Unmatch → quay về màn trước (Current price hoặc Trade tab).

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
| Query Unmatch | GET | `/api/v1/derivatives/order/todayUnmatch?accountNumber={account}` |

**Place order – Success (200):** `{ "message": "...", "orderNumber": "..." }`  
**Error:** `{ "code": "ORDER_PLACE_1005", "message": "..." }` hoặc `{ "code": "INVALID_PARAMETER", "params": [...] }`  
**Order types:** LO, ATO, ATC, MOK, MAK, MTL (price bắt buộc với LO; null với MOK, MAK, MTL).

**todayUnmatch – Query params (bắt buộc):**

| Param | Required | Description |
|-------|----------|-------------|
| `accountNumber` | ✅ | Số tài khoản Derivatives đang chọn |

**todayUnmatch – Response mapping (TradeX → FE hiển thị):**

| Response Field | Type | Hiển thị |
|----------------|------|----------|
| `orderNumber` | String | Số lệnh |
| `symbolCode` | String | Mã CK |
| `sellBuyType` | String | BUY/SELL (side) |
| `orderType` | String | Loại lệnh (LO, MTL, …) |
| `orderPrice` | Number | Giá |
| `orderQuantity` | Number | KL đặt |
| `matchedQuantity` | Number | KL khớp |
| `unmatchedQuantity` | Number | KL chưa khớp |
| `status` | String | PENDING / PENDING_MODIFY |
| `orderTime` | String | Thời gian đặt |

---

## Screens & Components (FE reference)

| Vị trí | FE Path (nhsv-mts-rn) | Mô tả |
|--------|----------------------|-------|
| Order entry | `src/screens/TradeTab/` – TradeOneTouchForm, OrderHistoryInTrade | Form đặt lệnh; Unmatch list mini trong Trade tab |
| Unmatch list (full) | `src/screens/OrderHistory/` – OrderBook, OrderHistoryTab | Danh sách lệnh chờ khớp; nút Hủy/Sửa |
| Modify | `src/screens/ModifyOrderBook/` | Màn sửa lệnh |
| Cancel / Modify API | `src/reduxs/` hoặc `src/services/` | Gọi cancel/modify API; hiển thị message success/error |

**FE repo (read-only):** `nhsv-mts-rn`. Equity dùng `getTodayUnmatch` → `/lotte/equity/order/todayUnmatch`; Derivatives cần API mới `/api/v1/derivatives/order/todayUnmatch`.

---

## Technical Notes

- Auto-populated: userId, name, identifierNumber, sourceIp do BE lấy từ JWT và request IP; FE không gửi.
- deviceUniqueId: Bắt buộc; map sang Lotte `cli_mac_addr`.
- Order type mapping: LO→2, ATO→3, … (BE map); FE gửi mã TradeX: LO, ATO, …
- Có thể dùng chung form cho Equity và Derivatives, khác endpoint và body field names.

---

## References

| Loại | Link |
|------|------|
| **API Spec** | [Regular_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md) – §6 todayUnmatch |
| **Planning** | [01_Regular_Orders_Business.md](../../../Planning%20documentation/Order/Planning/01_Regular_Orders_Business.md) |
| **Figma – Order form** | _(bổ sung node-id khi có)_ |
| **Figma – Unmatch list** | _(bổ sung node-id khi có)_ |
| **Figma – Success/Error toast** | _(bổ sung node-id khi có)_ |
