# Unrealized P&L API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - Unrealized Profit/Loss  
**Version:** 1.0  
**Date:** February 13, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Query unrealized P&L for open positions with summary.

---

## 1. Overview

### 1.1 Purpose

Unrealized P&L API tra cứu lãi/lỗ chưa thực hiện của các vị thế mở, bao gồm tổng hợp (summary) và chi tiết từng position.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Unrealized P&L | GET | `/api/v1/derivatives/asset/unrealizedPnl` |

### 1.3 Response Format Standards

**Success (Query):**
```json
{
  "summary": { ... },
  "positions": [ ... ],
  "pagination": {
    "hasMore": false,
    "nextKey": null,
    "totalRecords": 2
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
- Query: Summary + position list
- Pass-through Lotte messages AS-IS

---

## 2. Business Rules

### 2.1 P&L Calculation

**Long Position:**
```
unrealizedPnL = (currentPrice - averagePrice) × quantity × contractSize
unrealizedPnLPercent = (unrealizedPnL / costValue) × 100
```

**Short Position:**
```
unrealizedPnL = (averagePrice - currentPrice) × quantity × contractSize
unrealizedPnLPercent = (unrealizedPnL / costValue) × 100
```

### 2.2 Position Types

| Side | Lotte Code | TradeX Enum | Description |
|------|------------|-------------|-------------|
| Long | `L` | `LONG` | Mua/Nắm giữ - profit when price increases |
| Short | `S` | `SHORT` | Bán khống - profit when price decreases |

### 2.3 Date Rules

| Rule | Value | Description |
|------|-------|-------------|
| Date Format | `yyyyMMdd` | e.g., `20260213` |
| Default Date | Today | If `inquiryDate` missing |
| Max Date | Today | Cannot query future dates |
| Validation | `inquiryDate ≤ today` | Must be historical or today |

### 2.4 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, subNumber | `FIELD_IS_REQUIRED` |
| Date Format | Must be yyyyMMdd | `INVALID_DATE_FORMAT` |
| Date Limit | inquiryDate ≤ today | `INVALID_INQUIRY_DATE` |
| Fetch Count | 1 ≤ fetchCount ≤ 100 | `INVALID_FETCH_COUNT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.5 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Unrealized P&L

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/unrealizedPnl`

**Lotte Endpoint:** `[RootURL]/tuxsvc/der/account/dr-unrealized-pnl` (DRACC-031)

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh |
| `subNumber` | String | ✅ | - | Tiểu khoản |
| `inquiryDate` | String | ❌ | Today | Ngày tra cứu (yyyyMMdd) |
| `symbol` | String | ❌ | All | Filter by symbol |
| `nextKey` | String | ❌ | - | Pagination token |
| `fetchCount` | Number | ❌ | 50 | Records per page (max: 100) |

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `subNumber` | String | ✅ | `sub` | Direct | Tiểu khoản |
| `inquiryDate` | String | ❌ | `inquiry_data` | Default: today | Ngày tra cứu |
| `symbol` | String | ❌ | `sym` | Direct | Filter |
| `nextKey` | String | ❌ | `next_key` | Direct | Pagination |
| `fetchCount` | Number | ❌ | `row_count` | Default: 50 | Page size |
| *(JWT)* `userId` | - | - | `user_id` | Auto | Username |
| *(JWT)* `name` | - | - | `hts_user_nm` | Auto | Name |
| *(JWT)* `identifierNumber` | - | - | `idno` | Auto | ID |
| *(Request IP)* | - | - | `cli_ip_addr` | Auto | IP |
| *(Header)* | - | - | `lang_code` | Map (§2.5) | Language |

**Default Date Logic:**
```typescript
const today = new Date().toISOString().split('T')[0].replace(/-/g, '');
const inquiryDate = request.inquiryDate || today;

// Validate not future
if (inquiryDate > today) {
  throw new ValidationError('INVALID_INQUIRY_DATE');
}
```

### 3.3 Response Mapping

**Success (200):**

**Summary Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `tot_upnl` | `totalUnrealizedPnL` | Number | Parse float | Tổng lãi/lỗ chưa thực hiện |
| `tot_upnl_rt` | `totalUnrealizedPnLPercent` | Number | Parse float | % lãi/lỗ |
| `tot_mkt_val` | `totalMarketValue` | Number | Parse float | Giá trị thị trường |
| `tot_cost_val` | `totalCostValue` | Number | Parse float | Giá vốn |
| `tot_qty` | `totalQuantity` | Number | Parse int | Tổng KL |
| `inq_dt` | `inquiryDate` | String | Direct | Ngày tra cứu |

**Position Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `sym` | `symbol` | String | Direct | Mã CK |
| `sym_nm` | `symbolName` | String | Direct | Tên CK |
| `side` | `side` | Enum | `L→LONG`, `S→SHORT` | Long/Short |
| `qty` | `quantity` | Number | Parse int | Khối lượng |
| `avg_buy_prc` | `averageBuyPrice` | Number | Parse float | Giá mua TB |
| `cur_prc` | `currentPrice` | Number | Parse float | Giá hiện tại |
| `mkt_val` | `marketValue` | Number | Parse float | Giá trị TT |
| `cost_val` | `costValue` | Number | Parse float | Giá vốn |
| `upnl` | `unrealizedPnL` | Number | Parse float | Lãi/lỗ |
| `upnl_rt` | `unrealizedPnLPercent` | Number | Parse float | % lãi/lỗ |
| `prc_ch` | `priceChange` | Number | Parse float | Thay đổi giá |
| `prc_ch_rt` | `priceChangePercent` | Number | Parse float | % thay đổi |

**Pagination:**

| Lotte Field | TradeX Field | Type | Description |
|-------------|--------------|------|-------------|
| `has_more` | `pagination.hasMore` | Boolean | Còn trang sau |
| `next_key` | `pagination.nextKey` | String | Next page token |
| `tot_rec` | `pagination.totalRecords` | Number | Tổng số records |

**Response Structure:**
```json
{
  "summary": {
    "totalUnrealizedPnL": 15000000,
    "totalUnrealizedPnLPercent": 7.5,
    "totalMarketValue": 215000000,
    "totalCostValue": 200000000,
    "totalQuantity": 50,
    "inquiryDate": "20260213"
  },
  "positions": [
    {
      "symbol": "VN30F2403",
      "symbolName": "VN30 Future Mar 2024",
      "side": "LONG",
      "quantity": 20,
      "averageBuyPrice": 1250.5,
      "currentPrice": 1280.0,
      "marketValue": 102400000,
      "costValue": 100040000,
      "unrealizedPnL": 2360000,
      "unrealizedPnLPercent": 2.36,
      "priceChange": 29.5,
      "priceChangePercent": 2.36
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
  "summary": {
    "totalUnrealizedPnL": 0,
    "totalUnrealizedPnLPercent": 0,
    "totalMarketValue": 0,
    "totalCostValue": 0,
    "totalQuantity": 0,
    "inquiryDate": "20260213"
  },
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
| `accountNumber` / `subNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `inquiryDate` | `INVALID_DATE_FORMAT` | `["inquiryDate"]` | Wrong format (not yyyyMMdd) |
| `inquiryDate` | `INVALID_INQUIRY_DATE` | `["inquiryDate"]` | Future date |
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
| `1005` | `UNREALIZED_PNL_1005` | Lỗi hệ thống |
| `1006` | `UNREALIZED_PNL_1006` | Tài khoản không tồn tại |

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
  "code": "UNREALIZED_PNL_{LOTTE_CODE}",
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
| Lotte Business | `UNREALIZED_PNL_{LOTTE_CODE}` | `UNREALIZED_PNL_1005` | 422 |
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
- TradeX validates: Required fields, date format, date not future, fetch count
- Lotte validates: Business rules (account exists, positions exist)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Return P&L data with summary
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `UNREALIZED_PNL_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `UNREALIZED_PNL_1005`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

**5. Default Values:**
- `inquiryDate`: Default to today if not provided
- `symbol`: No filter (show all positions)
- `fetchCount`: Default 50, max 100

**6. Pagination:**
- Default page size: 50 records
- Max page size: 100 records
- Use `nextKey` pattern (map to Lotte `next_key`)
- Sort: By unrealizedPnL descending (biggest profit/loss first)

**7. Current Price:**
- Get from market data service (real-time or last close)
- If market closed → Use closing price
- Update frequency: Real-time during trading hours

### 5.3 Data Transformation

**Summary Calculation:**
```typescript
// Verify Lotte calculation
const summary = {
  totalUnrealizedPnL: parseFloat(lotteResponse.tot_upnl),
  totalUnrealizedPnLPercent: parseFloat(lotteResponse.tot_upnl_rt),
  // ... etc
};
```

**P&L Calculation Verification:**
```typescript
// Verify Lotte calculation matches formula
const calculatedPnL = calculatePnL(
  position.side,
  position.quantity,
  position.averageBuyPrice,
  position.currentPrice,
  contractSize
);

if (Math.abs(calculatedPnL - position.unrealizedPnL) > 0.01) {
  logger.warn('PnL mismatch', { 
    calculated: calculatedPnL, 
    lotte: position.unrealizedPnL 
  });
}
```

**Position Mapping:**
```typescript
return {
  positions: lotteResponse.positions.map(pos => ({
    symbol: pos.sym,
    side: pos.side === 'L' ? 'LONG' : 'SHORT',
    unrealizedPnL: parseFloat(pos.upnl),
    unrealizedPnLPercent: parseFloat(pos.upnl_rt),
    // ... map all fields
  })),
  // ... etc
};
```

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
