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
| **Execution** | Market To Limit (MTL) khi trigger |
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
| Giá trigger | `ACTIVE` → `TRIGGERED` | Đẩy lệnh MTL sang Lotte |
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
| `executedOrderNumber` | String | Số lệnh MTL đã đẩy sang Lotte |
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
| `TRIGGERED` | Đã trigger và đẩy lệnh MTL | ❌ | ❌ |
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
| `TRIGGERED` | Lotte rejects MTL | `FAILED` | Lotte error response |

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
- Execute MTL order when condition met

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
2. Calculate MTL quantity = current position quantity
3. Build MTL order:
   - **Order Type: MTL (Market To Limit)**
   - Direction: Opposite of position (BUY position → SELL order, SELL position → BUY order)
   - Quantity: Current position quantity
   - Account: Same as TP/SL order
   - Symbol: Same as TP/SL order
4. Push MTL order to Lotte via `lotte-bridge`:
   - For SELL (close LONG): Call DRORD-030 API
   - For BUY (close SHORT): Call DRORD-029 API
5. Save executed order number
6. Send notification to user

**Why MTL (Market To Limit)?**
- **Fast execution**: Market order priority, matches immediately at best available price
- **Price protection**: Unmatched quantity converts to limit order, prevents severe slippage
- **Better than MOK**: MOK cancels unmatched portion entirely, MTL keeps it as limit order
- **Risk management**: Balances speed (market) with safety (limit conversion)

---

## 10. WebSocket Real-time Updates

### 10.1 Overview

WebSocket integration cung cấp real-time updates cho TP/SL orders, đảm bảo FE luôn hiển thị trạng thái mới nhất mà không cần polling.

**Service:** `ws-v2` (Node.js WebSocket server)

### 10.2 Channel Pattern

**Format:**
```
tpsl.{accountNumber}
```

**Examples:**
```
tpsl.0001234567    → Updates for account 0001234567
tpsl.9999888777    → Updates for account 9999888777
```

**Connection Flow:**
1. FE connects to WebSocket server
2. FE subscribes to channel: `tpsl.{accountNumber}`
3. Backend publishes events to this channel whenever TP/SL status changes
4. FE receives events in real-time and updates UI

### 10.3 Event Types

#### Event 1: TPSL_CREATED

**Trigger:** User successfully creates new TP/SL order

**Payload:**
```json
{
  "event": "TPSL_CREATED",
  "timestamp": "2026-02-05T10:30:00.000Z",
  "data": {
    "tpslOrderId": "12345",
    "orderNumber": "2026020500012",
    "accountNumber": "0001234567",
    "symbolCode": "VN30F2602",
    "conditionType": "PRICE_BASED",
    "status": "PENDING",
    "takeProfit": {
      "triggerPrice": 1280.0
    },
    "stopLoss": {
      "triggerPrice": 1220.0
    }
  }
}
```

**FE Actions:**
- Add new TP/SL to active orders list
- Show toast notification: "✅ Đã tạo lệnh TP/SL"
- Update UI badge count

#### Event 2: TPSL_ACTIVATED

**Trigger:** Original order matched, TP/SL moves from PENDING → ACTIVE

**Payload:**
```json
{
  "event": "TPSL_ACTIVATED",
  "timestamp": "2026-02-05T10:31:00.000Z",
  "data": {
    "tpslOrderId": "12345",
    "status": "ACTIVE",
    "entryPrice": 1250.0,
    "currentQuantity": 10
  }
}
```

**FE Actions:**
- Update status in list: PENDING → ACTIVE
- Show entryPrice
- Update icon: 🟡 → 🟢

#### Event 3: TPSL_TRIGGERED ⭐

**Trigger:** Price reaches trigger level, MTL order sent to Lotte

**Payload:**
```json
{
  "event": "TPSL_TRIGGERED",
  "timestamp": "2026-02-05T11:00:00.000Z",
  "data": {
    "tpslOrderId": "12345",
    "status": "TRIGGERED",
    "triggeredType": "TAKE_PROFIT",
    "triggerPrice": 1280.0,
    "actualPrice": 1280.5,
    "executedOrderNumber": "2026020500099",
    "quantity": 10
  }
}
```

**FE Actions:**
- **Play sound alert** (critical event)
- **Show prominent popup:**
  ```
  🎯 TP/SL Đã Kích Hoạt!
  
  Lệnh TP cho VN30F2602
  Kích hoạt tại: 1280.0
  Khối lượng: 10 hợp đồng
  
  [Xem Chi Tiết]
  ```
- Update status: ACTIVE → TRIGGERED
- Refresh positions list (position may be closed)
- Move to history section

#### Event 4: TPSL_CANCELLED

**Trigger:** User cancels or system auto-cancels TP/SL

**Payload:**
```json
{
  "event": "TPSL_CANCELLED",
  "timestamp": "2026-02-05T12:00:00.000Z",
  "data": {
    "tpslOrderId": "12345",
    "status": "CANCELLED",
    "cancelReason": "USER_REQUESTED",
    "cancelledBy": "USER"
  }
}
```

**Cancel Reasons:**
- `USER_REQUESTED` - User clicked cancel
- `ORIGINAL_ORDER_CANCELLED` - Parent order cancelled
- `POSITION_CLOSED` - Position fully closed
- `SYSTEM_CLEANUP` - End of day cleanup

**FE Actions:**
- Update status: → CANCELLED
- Show toast: "ℹ️ Lệnh TP/SL đã hủy"
- Move to history
- Update icon: ⚫

#### Event 5: TPSL_EXPIRED

**Trigger:** End of trading session (15:00) or contract expires

**Payload:**
```json
{
  "event": "TPSL_EXPIRED",
  "timestamp": "2026-02-05T15:00:00.000Z",
  "data": {
    "tpslOrderId": "12345",
    "status": "EXPIRED",
    "expireReason": "END_OF_SESSION"
  }
}
```

**Expire Reasons:**
- `END_OF_SESSION` - Trading session ended (15:00)
- `CONTRACT_EXPIRED` - Contract expiry date reached

**FE Actions:**
- Update status: → EXPIRED
- Show toast: "⏰ Lệnh TP/SL đã hết hạn"
- Move to history
- Update icon: ⚪

#### Event 6: TPSL_FAILED ⚠️

**Trigger:** Lotte rejects MTL order when TP/SL triggers

**Payload:**
```json
{
  "event": "TPSL_FAILED",
  "timestamp": "2026-02-05T11:00:05.000Z",
  "data": {
    "tpslOrderId": "12345",
    "status": "FAILED",
    "errorCode": "INSUFFICIENT_MARGIN",
    "errorMessage": "Không đủ ký quỹ",
    "triggeredType": "TAKE_PROFIT",
    "triggerPrice": 1280.0
  }
}
```

**FE Actions:**
- **Show error popup:**
  ```
  ⚠️ TP/SL Không Thể Thực Hiện
  
  Lệnh TP cho VN30F2602 bị lỗi
  Lý do: Không đủ ký quỹ
  
  [Xem Chi Tiết] [Thử Lại]
  ```
- Update status: → FAILED
- Allow retry (create new TP/SL)
- Update icon: 🔴

#### Event 7: TPSL_MODIFIED

**Trigger:** User successfully modifies TP/SL configuration

**Payload:**
```json
{
  "event": "TPSL_MODIFIED",
  "timestamp": "2026-02-05T13:00:00.000Z",
  "data": {
    "tpslOrderId": "12345",
    "takeProfit": {
      "triggerPrice": 1290.0
    },
    "stopLoss": {
      "triggerPrice": 1210.0
    }
  }
}
```

**FE Actions:**
- Update TP/SL configuration in list
- Show toast: "✅ Đã cập nhật lệnh TP/SL"
- Refresh display with new trigger prices

### 10.4 Connection Management

#### Subscription Flow

**Step 1: Connect**
```
FE establishes WebSocket connection to ws-v2 server
URL: wss://nhsvpro.nhsv.vn/ws
Auth: JWT token in connection params or initial message
```

**Step 2: Subscribe**
```
FE sends subscription message:
{
  "action": "subscribe",
  "channel": "tpsl.0001234567"
}
```

**Step 3: Receive Confirmation**
```
Server responds:
{
  "action": "subscribed",
  "channel": "tpsl.0001234567",
  "status": "success"
}
```

**Step 4: Receive Events**
```
Server pushes events as they occur (see Event Types above)
```

**Step 5: Unsubscribe (when leaving screen)**
```
FE sends:
{
  "action": "unsubscribe",
  "channel": "tpsl.0001234567"
}
```

#### Reconnection Handling

**When connection drops:**
1. FE detects disconnect
2. FE attempts automatic reconnect (exponential backoff: 1s, 2s, 4s, 8s, max 30s)
3. On reconnect success:
   - Re-subscribe to `tpsl.{accountNumber}` channel
   - Call REST API `/active` to sync current state
   - Display any missed critical events (TRIGGERED, FAILED)

**Sync after reconnect:**
```
GET /api/v1/derivatives/tpslOrder/active?accountNumber={account}

→ Compare with local state
→ Update any status changes that occurred during disconnect
→ Show notification if critical events were missed
```

#### Multi-Device Sync

**Scenario:** User has app open on mobile + web simultaneously

**Behavior:**
- Both devices subscribe to same channel: `tpsl.{accountNumber}`
- When event occurs, **both devices receive same event simultaneously**
- Both devices update UI in sync
- No conflict or lag between devices

**Example:**
```
User cancels TP/SL on mobile
  → Mobile sends cancel API
  → Backend updates DB
  → Backend publishes TPSL_CANCELLED event to channel
  → Both mobile + web receive event
  → Both update UI: show "Cancelled" status
```

### 10.5 Backend Architecture

**Services Involved:**
- `order-v2`: TP/SL business logic
- `ws-v2`: WebSocket server
- Kafka: Event streaming

**Event Flow:**
1. Status change → Publish to Kafka topic `tpsl-updates`
2. ws-v2 consumes Kafka → Broadcast to WebSocket channel
3. FE receives event → Update UI

### 10.6 Performance Considerations

**Channel Isolation:** Each account has dedicated channel `tpsl.{accountNumber}`

**Message Size:** Keep payload minimal (~500 bytes per event)

**Rate Limiting:** Max 100 events/second per account

**Delivery:** At-least-once delivery, FE should deduplicate by `tpslOrderId`

### 10.7 Testing Requirements

**Connection Tests:**
- Connect with valid/invalid JWT
- Subscribe/unsubscribe to channels
- Disconnect handling

**Event Delivery Tests:**
- Verify all 7 event types delivered correctly
- Test reconnection with state sync
- Test multi-device sync (same account, different devices)

---

## 11. Push Notifications Integration

### 11.1 Overview

Push notifications via OneSignal provide critical alerts to users even when app is closed or in background. This complements WebSocket (which only works when app is active).

**Service:** `notification-service` (Java) integrates with OneSignal API

**Purpose:**
- Alert users about critical TP/SL events when app is inactive
- Ensure users don't miss important status changes
- Drive engagement by bringing users back to app

### 11.2 Event Triggers for Push Notifications

**Rule:** Only send push for **3 critical events** to avoid spam

| Event | Send Push? | Priority | Rationale |
|-------|-----------|----------|-----------|
| **TRIGGERED** | ✅ YES | 🔴 HIGH | User needs to know position was closed |
| **AUTO_CANCELLED** (original order cancelled) | ✅ YES | 🟡 MEDIUM | TP/SL no longer active, user may want to recreate |
| **FAILED** (Lotte rejects MTL order) | ✅ YES | 🔴 CRITICAL | TP/SL triggered but failed to execute, urgent action needed |
| User-initiated cancel | ❌ NO | - | User already knows |
| TP/SL created | ❌ NO | - | Not critical |
| TP/SL modified | ❌ NO | - | Not critical |
| Session expired (15:00) | ❌ NO | - | Expected, not urgent |

### 11.3 Push Notification Payloads

#### 11.3.1 Event: TRIGGERED

**Scenario:** Price hits trigger level, MTL order successfully sent to Lotte

**OneSignal Payload:**
```json
{
  "app_id": "{ONESIGNAL_APP_ID}",
  "include_player_ids": ["{user_device_id}"],
  "headings": {
    "en": "🎯 TP/SL Triggered",
    "vi": "🎯 TP/SL đã kích hoạt"
  },
  "contents": {
    "en": "TP order for VN30F2602 triggered at 1280.0. Position closed.",
    "vi": "Lệnh TP cho VN30F2602 đã kích hoạt tại 1280.0. Vị thế đã đóng."
  },
  "data": {
    "type": "TPSL_UPDATE",
    "event": "TRIGGERED",
    "tpslOrderId": "12345",
    "accountNumber": "0001234567",
    "symbolCode": "VN30F2602",
    "triggerPrice": 1280.0,
    "triggeredType": "TAKE_PROFIT",
    "executedOrderNumber": "2026020500099",
    "deepLink": "nhsvpro://tpsl/detail/12345"
  },
  "ios_badgeType": "Increase",
  "ios_badgeCount": 1,
  "android_channel_id": "tpsl_critical",
  "priority": 10
}
```

**Deep Link Behavior:**
- Tap notification → App opens to TP/SL detail screen
- Shows triggered order with execution details
- Displays matched price and quantity

#### 11.3.2 Event: AUTO_CANCELLED

**Scenario:** Original order cancelled → TP/SL automatically cancelled

**OneSignal Payload:**
```json
{
  "app_id": "{ONESIGNAL_APP_ID}",
  "include_player_ids": ["{user_device_id}"],
  "headings": {
    "en": "⚠️ TP/SL Auto-Cancelled",
    "vi": "⚠️ TP/SL đã hủy tự động"
  },
  "contents": {
    "en": "TP/SL for VN30F2602 cancelled. Reason: Original order cancelled.",
    "vi": "Lệnh TP/SL cho VN30F2602 đã hủy. Lý do: Lệnh gốc đã bị hủy."
  },
  "data": {
    "type": "TPSL_UPDATE",
    "event": "AUTO_CANCELLED",
    "tpslOrderId": "12345",
    "accountNumber": "0001234567",
    "symbolCode": "VN30F2602",
    "cancelReason": "ORIGINAL_ORDER_CANCELLED",
    "originalOrderNumber": "2026020500012",
    "deepLink": "nhsvpro://tpsl/history"
  },
  "ios_badgeType": "Increase",
  "ios_badgeCount": 1,
  "android_channel_id": "tpsl_medium",
  "priority": 7
}
```

**Deep Link Behavior:**
- Tap notification → App opens to TP/SL history
- Shows cancelled order with reason
- Option to create new TP/SL if position still exists

#### 11.3.3 Event: FAILED

**Scenario:** TP/SL triggered, MTL order sent, but Lotte rejects

**OneSignal Payload:**
```json
{
  "app_id": "{ONESIGNAL_APP_ID}",
  "include_player_ids": ["{user_device_id}"],
  "headings": {
    "en": "🔴 TP/SL Failed",
    "vi": "🔴 TP/SL không thể thực hiện"
  },
  "contents": {
    "en": "TP order for VN30F2602 failed. Reason: Insufficient margin. Action required!",
    "vi": "Lệnh TP cho VN30F2602 bị lỗi. Lý do: Không đủ ký quỹ. Cần xử lý ngay!"
  },
  "data": {
    "type": "TPSL_UPDATE",
    "event": "FAILED",
    "tpslOrderId": "12345",
    "accountNumber": "0001234567",
    "symbolCode": "VN30F2602",
    "triggerPrice": 1280.0,
    "errorCode": "INSUFFICIENT_MARGIN",
    "errorMessage": "Không đủ ký quỹ",
    "deepLink": "nhsvpro://position/detail/VN30F2602",
    "actions": ["CLOSE_POSITION", "ADD_MARGIN"]
  },
  "ios_badgeType": "Increase",
  "ios_badgeCount": 1,
  "android_channel_id": "tpsl_critical",
  "priority": 10,
  "ios_sound": "alarm.wav",
  "android_sound": "alarm"
}
```

**Deep Link Behavior:**
- Tap notification → App opens to Position detail
- Shows alert: TP/SL failed to execute
- Quick actions: "Close Position Now" (manual order), "Add Margin"

### 11.4 Anti-Spam Mechanisms

#### 11.4.1 Deduplication (Batch Grouping)

**Rule:** Group notifications for same symbol within 10-second window

**Example:**
- 3 TP/SL orders trigger for VN30F2602 within 5 seconds
- Send 1 grouped notification: "3 TP/SL orders triggered for VN30F2602"

**Grouped Payload:**
```json
{
  "headings": { "vi": "🎯 3 lệnh TP/SL đã kích hoạt" },
  "contents": { "vi": "VN30F2602: 2 lệnh TP, 1 lệnh SL đã kích hoạt" },
  "data": {
    "type": "TPSL_BATCH",
    "symbolCode": "VN30F2602",
    "count": 3,
    "orderIds": ["12345", "12346", "12347"]
  }
}
```

#### 11.4.2 Rate Limiting

**Limits:**
- Max 3 push notifications per minute per account
- Max 1 push per 10 seconds for same tpslOrderId
- Exceeded → Queue and batch at next available slot

#### 11.4.3 User Preferences

**Settings:**
```json
{
  "tpsl_triggered": true,          // User can toggle
  "tpsl_auto_cancelled": true,     // User can toggle
  "tpsl_failed": true              // Always true (cannot disable)
}
```

**Rule:** FAILED events always sent regardless of user preference

#### 11.4.4 Quiet Hours

**Default:** No notifications 22:00 - 07:00 (except FAILED events)

**Rules:**
- FAILED events: Always send immediately (critical)
- Other events: Queue and send at 07:00 next morning

### 11.5 Integration Flow

**High-level Flow:**
1. TP/SL event occurs → Publish to Kafka
2. ws-v2 consumes event → Broadcast WebSocket + Trigger notification-service
3. notification-service checks: event type, rate limit, dedup, user prefs, quiet hours
4. If passed → Call OneSignal API
5. OneSignal delivers push to device
6. User taps → App opens with deep link

### 11.6 OneSignal API Integration

**API Endpoint:** `POST https://onesignal.com/api/v1/notifications`

**Authentication:** REST API Key in Authorization header

**Required Fields:**
- `app_id`: OneSignal application ID
- `include_player_ids`: Array of device player IDs
- `headings`: Notification title (multi-language)
- `contents`: Notification body (multi-language)
- `data`: Custom data for deep linking

**Device Mapping:** Map `userId` → `onesignal_player_id` (stored in database)

### 11.7 Monitoring & Error Handling

**Metrics to Track:**
- Push notifications sent per event type
- Delivery rate, open rate, conversion rate
- Rate limiting triggers
- Deduplication group counts

**Error Handling:**

| OneSignal Error | HTTP | Action |
|-----------------|------|--------|
| Invalid player_id | 400 | Mark device inactive |
| Rate limit exceeded | 429 | Retry with backoff |
| Server error | 500 | Retry 3 times, log |

**Retry Strategy:**
- CRITICAL events (FAILED): Retry up to 5 times
- NON-CRITICAL events: Retry up to 2 times

---

## 12. Error Handling Summary

### 12.1 Error Response Format

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

### 12.2 Complete Error Code Reference

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
