# TP/SL Orders API Specification

**Document Type:** API Specification  
**Category:** Orders - Take Profit / Stop Loss Orders  
**Version:** 2.0  
**Date:** February 5, 2026

> **Note:** TradeX-native feature, không map sang Lotte.

---

## 1. Overview

### 1.1 Purpose

TP/SL (Take Profit / Stop Loss) cho phép khách hàng tự động đóng vị thế khi giá đạt mục tiêu lợi nhuận hoặc cắt lỗ.

### 1.2 API Endpoints

| Operation | Method | Endpoint | Auth |
|-----------|--------|----------|------|
| Place TP/SL | POST | `/api/v1/derivatives/tpslOrder` | JWT |
| Modify TP/SL | PUT | `/api/v1/derivatives/tpslOrder/modify` | JWT |
| Cancel TP/SL | PUT | `/api/v1/derivatives/tpslOrder/cancel` | JWT |
| Query Active | GET | `/api/v1/derivatives/tpslOrder/active` | JWT |
| Query History | GET | `/api/v1/derivatives/tpslOrder/history` | JWT |

### 1.3 Key Characteristics

| Feature | Description |
|---------|-------------|
| **Integration** | TradeX-native (không phụ thuộc Lotte) |
| **Condition Types** | Price-based hoặc Offset-based |
| **Flexibility** | Chỉ cần TP hoặc SL hoặc cả 2 |
| **Execution** | Market Order (MOK) khi trigger |
| **Validity** | Day Order (expire 15:00 hôm nay) |
| **Lifecycle** | Phụ thuộc lệnh gốc (cancel nếu lệnh gốc cancel/không khớp) |

---

## 2. Business Rules

### 2.1 Condition Types

| Type | Description | Example |
|------|-------------|---------|
| **Price-Based** | Trigger khi giá thị trường đạt mức set | TP = 1300, SL = 1230 |
| **Offset-Based** | Trigger khi giá +/- offset points từ entry | TP offset = +50, SL offset = -20 |

### 2.2 Validation Rules

**For BUY Position:**

| Rule | Description | Error Code |
|------|-------------|------------|
| TP Price | MUST be > entry price | `TP_PRICE_MUST_BE_HIGHER_THAN_ENTRY` |
| SL Price | MUST be < entry price | `SL_PRICE_MUST_BE_LOWER_THAN_ENTRY` |
| TP Offset | MUST be positive (> 0) | `TP_OFFSET_MUST_BE_POSITIVE` |
| SL Offset | MUST be positive (> 0) | `SL_OFFSET_MUST_BE_POSITIVE` |

**For SELL Position:**

| Rule | Description | Error Code |
|------|-------------|------------|
| TP Price | MUST be < entry price | `TP_PRICE_MUST_BE_LOWER_THAN_ENTRY` |
| SL Price | MUST be > entry price | `SL_PRICE_MUST_BE_HIGHER_THAN_ENTRY` |
| TP Offset | MUST be positive (> 0) | `TP_OFFSET_MUST_BE_POSITIVE` |
| SL Offset | MUST be positive (> 0) | `SL_OFFSET_MUST_BE_POSITIVE` |

### 2.3 Lifecycle Dependency Rules

| Event | TP/SL Status Change | Logic |
|-------|---------------------|-------|
| Lệnh gốc chưa khớp | `PENDING` | Chờ lệnh gốc khớp lần đầu |
| Lệnh gốc khớp (dù 1 phần) | `PENDING` → `ACTIVE` | Activate ngay, entry price = avg matched price |
| Lệnh gốc cancel | `PENDING/ACTIVE` → `CANCELLED` | TP/SL follow lệnh gốc |
| Lệnh gốc modify | TP/SL chuyển sang lệnh mới | Giữ nguyên configuration |
| Giá trigger | `ACTIVE` → `TRIGGERED` | Đẩy lệnh MOK sang Lotte |
| End of session (15:00) | `PENDING/ACTIVE` → `EXPIRED` | Day Order expiry |

### 2.4 Partial Fill Handling

| Scenario | TP/SL Quantity | Logic |
|----------|----------------|-------|
| Lệnh gốc khớp toàn bộ | = Total matched quantity | Standard case |
| Lệnh gốc khớp 1 phần | = Current position quantity | Dynamic adjustment |
| Lệnh gốc khớp thêm | Auto-update TP/SL qty | Real-time sync |
| User modify lệnh | TP/SL follow lệnh mới | Position-based, not order-based |

**Example Flow:**
```
Step 1: User đặt BUY 10 @ 1250 + TP/SL → Status = PENDING
Step 2: Khớp 3 @ 1250 → TP/SL = ACTIVE, qty = 3, entry = 1250
Step 3: Khớp thêm 4 @ 1251 → TP/SL auto-update: qty = 7, entry = 1250.57
Step 4: User modify giá → TP/SL chuyển sang lệnh mới, qty = 7
```

---

## 3. API: Place TP/SL Order

### 3.1 Request

**Endpoint:** `POST /api/v1/derivatives/tpslOrder`

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional)

**Body Structure:**

| Field | Type | Required | Description | Sample |
|-------|------|----------|-------------|--------|
| `accountNumber` | String | ✅ | Số tài khoản | `"0001234567"` |
| `orderNumber` | String | ✅ | Số hiệu lệnh gốc | `"2026020500012"` |
| `conditionType` | String | ✅ | `PRICE_BASED` / `OFFSET_BASED` | `"PRICE_BASED"` |
| `takeProfit` | Object | ❌ | TP configuration (null = no TP) | See below |
| `stopLoss` | Object | ❌ | SL configuration (null = no SL) | See below |
| `deviceUniqueId` | String | ✅ | Device ID | `"A1B2C3D4E5F6"` |

**TakeProfit/StopLoss Object (Price-Based):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `triggerPrice` | Number | ✅ | Giá trigger |

**TakeProfit/StopLoss Object (Offset-Based):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `offsetPoints` | Number | ✅ | Số điểm offset (always positive) |

**Constraints:**
- At least ONE of `takeProfit` or `stopLoss` MUST be provided
- Both CAN be provided together
- If `conditionType = PRICE_BASED` → use `triggerPrice`
- If `conditionType = OFFSET_BASED` → use `offsetPoints`

### 3.2 Response Mapping

**Success (200):**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Number | TP/SL order ID (database numeric ID) |

**Note:** Ultra-simple response. HTTP 200 = success. Query `/active` for details.

**Validation Error (400):**

| Field | Type | Description |
|-------|------|-------------|
| `code` | String | `INVALID_PARAMETER` |
| `params` | Array | Field-level errors with `code`, `param`, `messageParams` |

**Business Error (422):**

| Field | Type | Description |
|-------|------|-------------|
| `code` | String | Error code (see Error Mapping) |
| `params` | Array | Empty `[]` |
| `messageParams` | Array | Context values for i18n |

### 3.3 Error Mapping

| HTTP | Error Code | messageParams | Condition |
|------|------------|---------------|-----------|
| 400 | `FIELD_IS_REQUIRED` | `["fieldName"]` | Missing required field |
| 400 | `AT_LEAST_ONE_REQUIRED` | `["takeProfit", "stopLoss"]` | Both TP & SL missing |
| 400 | `INVALID_CONDITION_TYPE` | `["PRICE_BASED", "OFFSET_BASED"]` | Invalid conditionType value |
| 422 | `TP_PRICE_MUST_BE_HIGHER_THAN_ENTRY` | `[entryPrice, tpPrice]` | BUY: TP ≤ entry |
| 422 | `SL_PRICE_MUST_BE_LOWER_THAN_ENTRY` | `[entryPrice, slPrice]` | BUY: SL ≥ entry |
| 422 | `TP_OFFSET_MUST_BE_POSITIVE` | `[offset]` | Offset ≤ 0 |
| 422 | `ORDER_NOT_MATCHED` | `[]` | Lệnh gốc chưa khớp (for price-based with immediate activation requirement) |
| 422 | `ORDER_NOT_FOUND` | `[]` | Không tìm thấy lệnh gốc |
| 422 | `TPSL_ALREADY_EXISTS` | `[orderNumber]` | TP/SL đã tồn tại cho lệnh này |
| 422 | `UNABLE_TO_TPSL_ORDERS_OF_ANOTHER_ACCOUNT` | `[]` | Lệnh không thuộc account này |
| 401 | `UNAUTHORIZED` / `TOKEN_EXPIRED` | - | Auth issues |

---

## 4. API: Modify TP/SL Order

### 4.1 Request

**Endpoint:** `PUT /api/v1/derivatives/tpslOrder/modify`

**Body Structure:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `tpslOrderId` | String | ✅ | ID của TP/SL cần sửa |
| `takeProfit` | Object | ❌ | TP mới (null = remove TP) |
| `stopLoss` | Object | ❌ | SL mới (null = remove SL) |
| `deviceUniqueId` | String | ✅ | Device ID |

**Constraints:**
- Có thể sửa TP, SL, hoặc cả 2
- Set `null` để remove TP hoặc SL
- Nhưng KHÔNG được remove cả 2 cùng lúc

### 4.2 Response Mapping

**Success (200):**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Number | ID của TP/SL order đã modify |

**Error (400/422):** Same structure as Place API

### 4.3 Error Mapping

| HTTP | Error Code | messageParams | Condition |
|------|------------|---------------|-----------|
| 400 | `FIELD_IS_REQUIRED` | `["tpslOrderId"]` | Missing required field |
| 422 | `OBJECT_NOT_FOUND` | `[]` | TP/SL order not found |
| 422 | `TPSL_ORDER_INVALID_STATUS` | `[]` | Cannot modify (TRIGGERED/EXPIRED/CANCELLED) |
| 422 | `UNABLE_TO_TPSL_ORDERS_OF_ANOTHER_ACCOUNT` | `[]` | Không thể modify lệnh của account khác |
| 422 | `TP_PRICE_MUST_BE_HIGHER_THAN_ENTRY` | `[entryPrice, tpPrice]` | TP price validation failed |
| 422 | `SL_PRICE_MUST_BE_LOWER_THAN_ENTRY` | `[entryPrice, slPrice]` | SL price validation failed |

---

## 5. API: Cancel TP/SL Order

### 5.1 Request

**Endpoint:** `PUT /api/v1/derivatives/tpslOrder/cancel`

**Body Structure:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `tpslOrderId` | String | ✅ | ID của TP/SL cần hủy |
| `deviceUniqueId` | String | ✅ | Device ID |

### 5.2 Response Mapping

**Success (200):**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Number | ID của TP/SL order đã cancel |

### 5.3 Error Mapping

| HTTP | Error Code | messageParams | Condition |
|------|------------|---------------|-----------|
| 400 | `FIELD_IS_REQUIRED` | `["tpslOrderId"]` | Missing required field |
| 422 | `OBJECT_NOT_FOUND` | `[]` | TP/SL order not found |
| 422 | `TPSL_ORDER_INVALID_STATUS` | `[]` | Cannot cancel (TRIGGERED/EXPIRED/CANCELLED) |
| 422 | `UNABLE_TO_TPSL_ORDERS_OF_ANOTHER_ACCOUNT` | `[]` | Không thể cancel lệnh của account khác |

---

## 6. API: Query Active TP/SL Orders

### 6.1 Request

**Endpoint:** `GET /api/v1/derivatives/tpslOrder/active`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `symbolCode` | String | ❌ | Lọc theo mã (optional) |

### 6.2 Response Mapping

**Success (200):**

| Field | Type | Description |
|-------|------|-------------|
| `totalCount` | Number | Tổng số lệnh TP/SL active |
| `orders` | Array | Danh sách TP/SL orders (see Order Object below) |

**Order Object Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `tpslOrderId` | String | ID của TP/SL order |
| `orderNumber` | String | Số hiệu lệnh gốc |
| `accountNumber` | String | Số tài khoản |
| `symbolCode` | String | Mã hợp đồng (VN30F2502) |
| `codeName` | String | Tên hợp đồng |
| `conditionType` | String | `PRICE_BASED` / `OFFSET_BASED` |
| `entryPrice` | Number | Giá vào lệnh (avg matched price) |
| `currentPrice` | Number | Giá thị trường hiện tại |
| `takeProfit` | Object / null | TP config (see below) |
| `stopLoss` | Object / null | SL config (see below) |
| `status` | String | `PENDING` / `ACTIVE` |
| `createdAt` | String | ISO 8601 timestamp |

**TakeProfit/StopLoss Object:**

| Field | Type | Description |
|-------|------|-------------|
| `triggerPrice` | Number | Giá trigger (for price-based) |
| `offsetPoints` | Number | Offset points (for offset-based) |
| `status` | String | `ACTIVE` |
| `remainingPoints` | Number | Số điểm còn lại đến trigger (for display) |

---

## 7. API: Query TP/SL History

### 7.1 Request

**Endpoint:** `GET /api/v1/derivatives/tpslOrder/history`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `fromDate` | String | ❌ | Từ ngày (YYYY-MM-DD) |
| `toDate` | String | ❌ | Đến ngày (YYYY-MM-DD) |
| `status` | String | ❌ | Filter by status: `TRIGGERED`, `CANCELLED`, `EXPIRED` |

### 7.2 Response Mapping

**Success (200):**

| Field | Type | Description |
|-------|------|-------------|
| `totalCount` | Number | Tổng số records |
| `orders` | Array | Danh sách history (see History Object below) |

**History Object Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `tpslOrderId` | String | ID của TP/SL order |
| `orderNumber` | String | Số hiệu lệnh gốc |
| `symbolCode` | String | Mã hợp đồng |
| `entryPrice` | Number | Giá vào lệnh |
| `triggeredType` | String | `TAKE_PROFIT` / `STOP_LOSS` / null |
| `triggerPrice` | Number | Giá trigger (nếu triggered) |
| `triggerTime` | String | Thời gian trigger (ISO 8601) |
| `executedOrderNumber` | String | Số lệnh MOK đã đẩy sang Lotte |
| `executedPrice` | Number | Giá khớp thực tế |
| `executedQuantity` | Number | Khối lượng khớp |
| `profit` | Number | Lãi/lỗ (points × qty × contract_size) |
| `status` | String | `TRIGGERED` / `CANCELLED` / `EXPIRED` |
| `statusName` | String | Tên trạng thái (localized) |

---

## 8. Data Models

### 8.1 Status Flow

```
PENDING → ACTIVE → TRIGGERED (success)
   ↓         ↓           ↓
CANCELLED  EXPIRED  (terminal states)
```

| Status | Description | Can Modify? | Can Cancel? |
|--------|-------------|-------------|-------------|
| `PENDING` | Chờ lệnh gốc khớp | ✅ | ✅ |
| `ACTIVE` | Đang monitor giá | ✅ | ✅ |
| `TRIGGERED` | Đã trigger và đẩy lệnh MOK | ❌ | ❌ |
| `CANCELLED` | Đã hủy (user hoặc system) | ❌ | ❌ |
| `EXPIRED` | Hết hạn (end of session 15:00) | ❌ | ❌ |
| `FAILED` | Trigger failed (Lotte reject) | ❌ | ❌ |

### 8.2 Status Transitions

| From Status | Event | To Status | Trigger |
|-------------|-------|-----------|---------|
| `PENDING` | Original order matched | `ACTIVE` | Order match event |
| `PENDING` | Original order cancelled | `CANCELLED` | Order cancel event |
| `ACTIVE` | Price reaches TP/SL | `TRIGGERED` | Market price monitor |
| `ACTIVE` | User cancels | `CANCELLED` | Cancel API call |
| `ACTIVE` | 15:00 reached | `EXPIRED` | Daily expiry job |
| `ACTIVE` | Original order cancelled | `CANCELLED` | Order cancel event |
| `ACTIVE` | Original order modified | Follow new order | Order modify event |
| `TRIGGERED` | Lotte rejects MOK | `FAILED` | Lotte error response |

---

## 9. Implementation Notes

### 9.1 System Architecture

| Component | Role | Tech |
|-----------|------|------|
| `rest-proxy` | API Gateway, JWT validation | Node.js |
| `order-v2` | TP/SL business logic, validation | Java |
| `market-query-v2` | Real-time price monitoring | Java |
| `ws-v2` | Market data subscription | Node.js |
| **Redis** | Active TP/SL queue | Redis |
| **PostgreSQL** | TP/SL persistent storage | PostgreSQL |
| **Kafka** | Order events, market data | Kafka |

### 9.2 Key Processes

**1. Lifecycle Management:**
- Listen to **order match events** để activate TP/SL
- Listen to **order cancel events** để cancel TP/SL
- Listen to **order modify events** để transfer TP/SL
- Listen to **position updates** để adjust TP/SL quantity

**2. Real-time Monitoring:**
- Subscribe to `market.quote.{code}` WebSocket channel
- Compare current price vs trigger conditions
- Execute MOK order when condition met

**3. Daily Expiry:**
- Scheduled job runs at 15:00
- Mark all `PENDING/ACTIVE` TP/SL as `EXPIRED`
- Send notifications to users

### 9.3 Monitoring & Trigger Logic

**Price Monitoring:**
- Subscribe to real-time market data via WebSocket
- Check TP/SL conditions every price update
- For BUY position:
  - TP triggers when `currentPrice >= tpPrice`
  - SL triggers when `currentPrice <= slPrice`
- For SELL position:
  - TP triggers when `currentPrice <= tpPrice`
  - SL triggers when `currentPrice >= slPrice`

**Trigger Execution:**
1. Mark TP/SL status = `TRIGGERED`
2. Calculate MOK quantity = current position quantity
3. Push MOK order to Lotte via `lotte-bridge`
4. Save executed order number
5. Send notification to user

---

## 10. Error Handling Summary

### 10.1 Error Response Format

**Validation Error (400):**
```
{
  "code": "INVALID_PARAMETER",
  "params": [ { "code": "...", "param": "...", "messageParams": [...] } ]
}
```

**Business Error (422):**
```
{
  "code": "ERROR_CODE",
  "params": [],
  "messageParams": [...]
}
```

**Auth Error (401):**
```
{
  "code": "UNAUTHORIZED" / "TOKEN_EXPIRED",
  "message": "..."
}
```

### 10.2 Complete Error Code Reference

| Category | Error Code | HTTP | Description |
|----------|------------|------|-------------|
| **Validation** | `FIELD_IS_REQUIRED` | 400 | Missing required field |
| **Validation** | `AT_LEAST_ONE_REQUIRED` | 400 | Both TP & SL missing |
| **Validation** | `INVALID_CONDITION_TYPE` | 400 | Invalid conditionType |
| **Validation** | `ACCOUNT_NUMBER_MUST_BE_REQUIRED` | 400 | Missing accountNumber |
| **Business** | `TP_PRICE_MUST_BE_HIGHER_THAN_ENTRY` | 422 | BUY: TP validation failed |
| **Business** | `SL_PRICE_MUST_BE_LOWER_THAN_ENTRY` | 422 | BUY: SL validation failed |
| **Business** | `TP_PRICE_MUST_BE_LOWER_THAN_ENTRY` | 422 | SELL: TP validation failed |
| **Business** | `SL_PRICE_MUST_BE_HIGHER_THAN_ENTRY` | 422 | SELL: SL validation failed |
| **Business** | `TP_OFFSET_MUST_BE_POSITIVE` | 422 | Offset ≤ 0 |
| **Business** | `SL_OFFSET_MUST_BE_POSITIVE` | 422 | Offset ≤ 0 |
| **Business** | `ORDER_NOT_MATCHED` | 422 | Lệnh gốc chưa khớp |
| **Business** | `ORDER_NOT_FOUND` | 422 | Không tìm thấy lệnh gốc |
| **Business** | `POSITION_NOT_FOUND` | 422 | Không tìm thấy vị thế |
| **Business** | `TPSL_ALREADY_EXISTS` | 422 | TP/SL đã tồn tại |
| **Business** | `OBJECT_NOT_FOUND` | 422 | TP/SL order not found |
| **Business** | `TPSL_ORDER_INVALID_STATUS` | 422 | Cannot modify/cancel |
| **Business** | `UNABLE_TO_TPSL_ORDERS_OF_ANOTHER_ACCOUNT` | 422 | Authorization failed |
| **Auth** | `UNAUTHORIZED` | 401 | Invalid/missing token |
| **Auth** | `TOKEN_EXPIRED` | 401 | Token expired |

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
