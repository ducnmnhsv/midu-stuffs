# Daily P&L API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - Daily Profit/Loss  
**Version:** 1.0  
**Date:** March 9, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Query lãi/lỗ theo ngày (realized + unrealized + fee + tax) trong khoảng thời gian. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) §2.1.8 — DRACC-037 (dr-daily-profit-loss).

---

## 1. Overview

### 1.1 Purpose

Daily P&L API tra cứu lãi/lỗ theo từng ngày trong khoảng thời gian, bao gồm lãi/lỗ đã thực hiện, chưa thực hiện, phí và thuế (net P&L), theo tài khoản/sản phẩm/hợp đồng.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Daily P&L | GET | `/api/v1/derivatives/asset/dailyPnl` |

### 1.3 Response Format Standards

**Success (Query):**
```json
{
  "items": [...],
  "pagination": {
    "hasMore": true,
    "nextKey": "...",
    "totalRecords": 50
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
- Query: List of daily P&L records + pagination
- Pass-through Lotte messages AS-IS

---

## 2. Business Rules

### 2.1 Date Rules

| Rule | Value | Description |
|------|-------|-------------|
| Date Format | `yyyyMMdd` | e.g., `20260301` |
| Validation | `dateFrom ≤ dateTo` | Start date must be ≤ end date |
| Max Range | 90 days | `dateTo - dateFrom ≤ 90 days` (TradeX limit; confirm with Lotte) |

### 2.2 Search Type

| TradeX Value | Lotte `search_type` | Description |
|--------------|---------------------|-------------|
| `false` | false | Tra cứu từng tài khoản (mặc định) |
| `true` | true | Tra cứu toàn bộ (cần quyền back-office) |

### 2.3 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, dateFrom, dateTo, productCode | `FIELD_IS_REQUIRED` |
| Date Format | Must be yyyyMMdd | `INVALID_DATE_FORMAT` |
| Date Range | dateFrom ≤ dateTo | `INVALID_DATE_RANGE` |
| Date Range Limit | Max 90 days | `DATE_RANGE_EXCEEDED` |
| Fetch Count | 1 ≤ fetchCount ≤ 100 | `INVALID_FETCH_COUNT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.4 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Daily P&L

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/dailyPnl`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-daily-profit-loss` (DRACC-037) — Lotte_DR §2.1.8. Lotte Method: POST; TradeX exposes GET với query parameters, backend proxy chuyển sang POST body.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh |
| `dateFrom` | String | ✅ | - | Từ ngày (yyyyMMdd) |
| `dateTo` | String | ✅ | - | Tới ngày (yyyyMMdd) |
| `productCode` | String | ✅ | - | Mã sản phẩm |
| `searchAllAccounts` | Boolean | ❌ | false | true: Tra cứu toàn bộ, false: Tra cứu từng TK |
| `contractCode` | String | ❌ | "%" | Mã hợp đồng (filter) |
| `branch` | String | ❌ | "%" | Chi nhánh |
| `department` | String | ❌ | "%" | Phòng giao dịch |
| `nextKey` | String | ❌ | "0" | Pagination token |
| `fetchCount` | Number | ❌ | 50 | Records per page (max: 100) |

### 3.2 Request Mapping

**TradeX → Lotte (backend builds POST body):**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `account_no` | Direct | Số tài khoản |
| `dateFrom` | String | ✅ | `start_date` | Direct | Từ ngày |
| `dateTo` | String | ✅ | `end_date` | Direct | Tới ngày |
| `productCode` | String | ✅ | `product_code` | Direct | Mã sản phẩm |
| `searchAllAccounts` | Boolean | ❌ | `search_type` | Direct (true/false) | Loại tra cứu |
| `contractCode` | String | ❌ | `contract_code` | Default: "%" | Mã hợp đồng |
| `branch` | String | ❌ | `branch` | Default: "%" | Chi nhánh |
| `department` | String | ❌ | `department` | Default: "%" | Phòng giao dịch |
| `nextKey` | String | ❌ | `next_key` | Default: "0" | Pagination |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto (or from token) | hts_user_id tra cứu |
| *(Backend)* | - | - | `password` | From session / secure store | Mật khẩu mã hóa (không truyền từ client) |

**Note:** Lotte yêu cầu `password` (mã hóa). TradeX **không** nhận password từ client; backend lấy credential từ session/JWT hoặc secure store khi gọi Lotte.

**Default Logic:**
```typescript
const searchType = request.searchAllAccounts ?? false;
const contractCode = request.contractCode ?? '%';
const branch = request.branch ?? '%';
const department = request.department ?? '%';
const nextKey = request.nextKey ?? '0';

// Validate date range
const daysDiff = calculateDaysDiff(dateFrom, dateTo);
if (daysDiff > 90) {
  throw new ValidationError('DATE_RANGE_EXCEEDED');
}
```

### 3.3 Response Mapping

**Success (200):**

**Item Object (mỗi dòng P&L theo ngày):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `date` | `date` | String | Direct | Ngày giao dịch (yyyyMMdd) |
| `account_no` | `accountNumber` | String | Direct | Số tài khoản |
| `account_name` | `accountName` | String | Direct | Tên tài khoản |
| `product_code` | `productCode` | String | Direct | Mã sản phẩm |
| `contract_code` | `contractCode` | String | Direct | Mã hợp đồng |
| `product_name` | `productName` | String | Direct | Tên sản phẩm |
| `realized_profit_loss` | `realizedProfitLoss` | Number | Parse float | Lãi lỗ đã thực hiện |
| `unrealized_profit_loss` | `unrealizedProfitLoss` | Number | Parse float | Lãi lỗ chưa thực hiện |
| `fee` | `fee` | Number | Parse float | Phí |
| `net_profit_loss` | `netProfitLoss` | Number | Parse float | Net lãi lỗ |
| `tax` | `tax` | Number | Parse float | Thuế |

**Pagination:**

| Lotte Field | TradeX Field | Type | Description |
|-------------|--------------|------|-------------|
| (from last item or response) `next_key` | `pagination.nextKey` | String | Next page token; null nếu hết |
| (derived) | `pagination.hasMore` | Boolean | nextKey != null && nextKey != "0" |
| (if available) | `pagination.totalRecords` | Number | Tổng số records (nếu Lotte cung cấp) |

**Response Structure:**
```json
{
  "items": [
    {
      "date": "20260301",
      "accountNumber": "001C123456",
      "accountName": "NGUYEN VAN A",
      "productCode": "VN30",
      "contractCode": "VN30F2403",
      "productName": "Hợp đồng tương lai VN30",
      "realizedProfitLoss": 15000000,
      "unrealizedProfitLoss": -2000000,
      "fee": 500000,
      "tax": 0,
      "netProfitLoss": 12500000
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
  "items": [],
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
| `accountNumber` / `dateFrom` / `dateTo` / `productCode` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `dateFrom` / `dateTo` | `INVALID_DATE_FORMAT` | `["dateFrom"]` | Wrong format (not yyyyMMdd) |
| `dateFrom` / `dateTo` | `INVALID_DATE_RANGE` | `["dateFrom", "dateTo"]` | dateFrom > dateTo |
| `dateFrom` / `dateTo` | `DATE_RANGE_EXCEEDED` | `["90"]` | Range > 90 days |
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
| `1005` | `DAILY_PNL_1005` | Lỗi hệ thống |
| `1006` | `DAILY_PNL_1006` | Tài khoản không tồn tại |

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
  "code": "DAILY_PNL_{LOTTE_CODE}",
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
| Lotte Business | `DAILY_PNL_{LOTTE_CODE}` | `DAILY_PNL_1005` | 422 |
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
| `lotte-bridge` | Lotte API integration, request/response mapping (POST body from GET query) |
| `asset-v2` | Business logic, validation (if needed) |
| **Kafka** | Service communication |

### 5.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, date format, date range (≤ 90 days), fetch count
- Lotte validates: Business rules (account exists, product code, data availability)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Return daily P&L items + pagination
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `DAILY_PNL_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `DAILY_PNL_1005`

**4. Auto-Population:**
- `userId` / `hts_user_id` → From JWT token or session
- `password` → From backend secure store/session (never from client)
- `lang_code` → From `Accept-Language` header

**5. Default Values:**
- `searchAllAccounts`: false
- `contractCode`, `branch`, `department`: "%"
- `nextKey`: "0"
- `fetchCount`: Default 50, max 100

**6. Pagination:**
- Lotte trả `next_key` trong từng dòng hoặc response; dùng để gọi trang tiếp theo
- TradeX: `pagination.nextKey`, `pagination.hasMore`, `pagination.totalRecords` (nếu có)

**7. GET vs POST:**
- Client gọi GET với query parameters
- Backend (lotte-bridge) chuyển sang POST body khi gọi Lotte DRACC-037

### 5.3 Data Transformation

**Numbers:**
```typescript
return {
  date: lotteItem.date,
  accountNumber: lotteItem.account_no,
  accountName: lotteItem.account_name,
  productCode: lotteItem.product_code,
  contractCode: lotteItem.contract_code,
  productName: lotteItem.product_name,
  realizedProfitLoss: parseFloat(lotteItem.realized_profit_loss) || 0,
  unrealizedProfitLoss: parseFloat(lotteItem.unrealized_profit_loss) || 0,
  fee: parseFloat(lotteItem.fee) || 0,
  tax: parseFloat(lotteItem.tax) || 0,
  netProfitLoss: parseFloat(lotteItem.net_profit_loss) || 0,
};
```

**Pagination:**
```typescript
const lastItem = items[items.length - 1];
const nextKey = lastItem?.next_key ?? null;
const hasMore = nextKey != null && nextKey !== '0';
```

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team; confirm với Lotte giới hạn khoảng ngày (start_date/end_date) và cách trả `totalRecords` nếu có.
