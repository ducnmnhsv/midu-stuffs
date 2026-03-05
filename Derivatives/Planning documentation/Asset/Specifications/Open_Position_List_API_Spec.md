# Open Positions API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - Open Positions  
**Version:** 1.0  
**Date:** February 13, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Query open positions with real-time P&L. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) (27/02/2026) §2.1.1.

---

## 1. Overview

### 1.1 Purpose

Open Positions API tra cứu danh sách vị thế mở (positions) đang nắm giữ, bao gồm thông tin khối lượng, giá, lãi/lỗ chưa thực hiện.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Open Positions | GET | `/api/v1/derivatives/asset/openPositions` |

### 1.3 Response Format Standards

**Success (Query):**
```json
{
  "positions": [...],
  "pagination": {
    "hasMore": true,
    "nextKey": "...",
    "totalRecords": 150
  }
}
```

**Error:**
```json
{
  "code": "ERROR_CODE",
  "message": "Error message"
}
```

or

```json
{
  "code": "INVALID_PARAMETER",
  "params": [ ... ]
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Query: Rich data arrays
- Pass-through Lotte messages AS-IS (including `[CODE]` prefix)

---

## 2. Business Rules

### 2.1 Position Types

| Side | Description | P&L Calculation |
|------|-------------|-----------------|
| `LONG` | Mua/Nắm giữ | (currentPrice - averagePrice) × quantity |
| `SHORT` | Bán khống | (averagePrice - currentPrice) × quantity |

### 2.2 Quantity Types

| Field | Description |
|-------|-------------|
| `totalQuantity` | Tổng KL nắm giữ |
| `availableQuantity` | KL khả dụng (có thể đóng ngay) |
| `blockedQuantity` | KL bị khóa (có lệnh chờ) |

**Formula:** `totalQuantity = availableQuantity + blockedQuantity`

### 2.3 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, subNumber | `FIELD_IS_REQUIRED` |
| Fetch Count | 1 ≤ fetchCount ≤ 100 | `INVALID_FETCH_COUNT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.4 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Open Positions

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/openPositions`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-open-positions` (DRACC-003) — Lotte_DR §2.1.1. Method POST; request: `acnt`, `next_data`, `hts_user_id`.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản phái sinh |
| `subNumber` | String | ✅ | Tiểu khoản |
| `symbol` | String | ❌ | Filter by symbol (optional) |
| `nextKey` | String | ❌ | Pagination token |
| `fetchCount` | Number | ❌ | Records per page (default: 50, max: 100) |

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `subNumber` | String | ✅ | `sub` | Direct | Tiểu khoản |
| `symbol` | String | ❌ | `sym` | Direct | Filter symbol |
| `nextKey` | String | ❌ | `next_key` | Direct | Pagination token |
| `fetchCount` | Number | ❌ | `row_count` | Default: 50 | Records per page |
| *(JWT)* `userId` | - | - | `user_id` | Auto | Username from token |
| *(JWT)* `name` | - | - | `hts_user_nm` | Auto | Name from token |
| *(JWT)* `identifierNumber` | - | - | `idno` | Auto | ID from token |
| *(Request IP)* | - | - | `cli_ip_addr` | Auto | Client IP |
| *(Header)* | - | - | `lang_code` | Map (§2.4) | Language code (V/E/K) |

### 3.3 Response Mapping

**Success (200):**

**Position Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `sym` | `symbol` | String | Direct | Mã hợp đồng |
| `sym_nm` | `symbolName` | String | Direct | Tên hợp đồng |
| `side` | `side` | Enum | `L→LONG`, `S→SHORT` | Long/Short |
| `tot_qty` | `totalQuantity` | Number | Parse int | Tổng KL |
| `aval_qty` | `availableQuantity` | Number | Parse int | KL khả dụng |
| `blk_qty` | `blockedQuantity` | Number | Parse int | KL bị khóa |
| `avg_prc` | `averagePrice` | Number | Parse float | Giá mua TB |
| `cur_prc` | `currentPrice` | Number | Parse float | Giá hiện tại |
| `mkt_val` | `marketValue` | Number | Parse float | Giá trị thị trường |
| `cost_val` | `costValue` | Number | Parse float | Giá vốn |
| `upnl` | `unrealizedPnL` | Number | Parse float | Lãi/lỗ chưa thực hiện |
| `upnl_rt` | `unrealizedPnLPercent` | Number | Parse float | % lãi/lỗ |
| `mrgn_req` | `marginRequired` | Number | Parse float | Ký quỹ yêu cầu |
| `open_dt` | `openDate` | String | Direct | Ngày mở (yyyyMMdd) |
| `upd_dt` | `lastUpdated` | String | ISO 8601 | Thời điểm cập nhật |

**Pagination:**

| Lotte Field | TradeX Field | Type | Description |
|-------------|--------------|------|-------------|
| `has_more` | `pagination.hasMore` | Boolean | Còn trang sau |
| `next_key` | `pagination.nextKey` | String | Next page token |
| `tot_rec` | `pagination.totalRecords` | Number | Tổng số records |

**Side Mapping:**

| Lotte Code | TradeX Enum | Vietnamese |
|------------|-------------|------------|
| `L` | `LONG` | Mua/Nắm giữ |
| `S` | `SHORT` | Bán/Bán khống |

**Response Structure:**
```json
{
  "positions": [
    {
      "symbol": "VN30F2403",
      "symbolName": "VN30 Future Mar 2024",
      "side": "LONG",
      "totalQuantity": 20,
      "availableQuantity": 15,
      "blockedQuantity": 5,
      "averagePrice": 1250.5,
      "currentPrice": 1280.0,
      "marketValue": 102400000,
      "costValue": 100040000,
      "unrealizedPnL": 2360000,
      "unrealizedPnLPercent": 2.36,
      "marginRequired": 25000000,
      "openDate": "20260210",
      "lastUpdated": "2026-02-13T10:30:00Z"
    }
  ],
  "pagination": {
    "hasMore": false,
    "nextKey": null,
    "totalRecords": 1
  }
}
```

**Empty Result:**
```json
{
  "positions": [],
  "pagination": {
    "hasMore": false,
    "nextKey": null,
    "totalRecords": 0
  }
}
```

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `subNumber` | `FIELD_IS_REQUIRED` | `["subNumber"]` | Missing |
| `fetchCount` | `INVALID_FETCH_COUNT` | `["fetchCount", "1", "100"]` | < 1 or > 100 |

**Auth Error (401):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `UNAUTHORIZED` | Token không hợp lệ hoặc đã hết hạn | Invalid token |
| `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Token expired |

**Auth Error (403):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `FORBIDDEN` | Không có quyền truy cập | No permission |
| `UNAUTHORIZED_ACCOUNT` | Tài khoản không thuộc quyền sở hữu của bạn | Account ownership check failed |

**Business Error (422) - Lotte Pass-Through:**

| Lotte Code | TradeX Code | Description |
|------------|-------------|-------------|
| `1005` | `OPEN_POSITIONS_1005` | Lỗi hệ thống |
| `1006` | `OPEN_POSITIONS_1006` | Tài khoản không tồn tại |
| `2001` | `OPEN_POSITIONS_2001` | Không có dữ liệu |

---

## 4. Error Handling Summary

### 4.1 Error Response Format

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "accountNumber", "messageParams": ["accountNumber"] }
  ]
}
```

**Auth Error (401/403):**
```json
{
  "code": "UNAUTHORIZED" / "TOKEN_EXPIRED" / "FORBIDDEN" / "UNAUTHORIZED_ACCOUNT",
  "message": "Error message"
}
```

**Business Error (422) - Lotte Pass-Through:**
```json
{
  "code": "OPEN_POSITIONS_{LOTTE_CODE}",
  "message": "[V3120] Lotte error message"
}
```

**Server Error (500):**
```json
{
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

### 4.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business | `OPEN_POSITIONS_{LOTTE_CODE}` | `OPEN_POSITIONS_1005` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

### 4.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|------------------|
| `1005` | Lỗi hệ thống | System error |
| `1006` | Tài khoản không tồn tại | Account not found |
| `2001` | Không có dữ liệu | No data available |

---

## 5. Implementation Notes

### 5.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| `asset-v2` | Business logic, validation (if needed) |
| **Kafka** | Service communication |

### 5.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, data types, format, account ownership
- Lotte validates: Business rules (positions exist, margin)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Return position data
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `OPEN_POSITIONS_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `OPEN_POSITIONS_1005`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

**5. Pagination:**
- Default page size: 50 records
- Max page size: 100 records
- Use `nextKey` pattern (map to Lotte `next_key`)
- Sort: By symbol ASC (alphabetical)

**6. Real-time Data:**
- `currentPrice`: Get from market data service
- `unrealizedPnL`: Calculate in real-time based on current price
- Update frequency: Real-time during trading hours

### 5.3 Data Transformation

**Numbers:**
```typescript
return {
  totalQuantity: parseInt(lotteResponse.tot_qty),
  averagePrice: parseFloat(lotteResponse.avg_prc),
  unrealizedPnL: parseFloat(lotteResponse.upnl),
  // ... etc
};
```

**Enums:**
```typescript
const sideMap = { 'L': 'LONG', 'S': 'SHORT' };
const side = sideMap[lotteResponse.side];
```

**Timestamps:**
```typescript
const lastUpdated = new Date(lotteResponse.upd_dt).toISOString();
```

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
