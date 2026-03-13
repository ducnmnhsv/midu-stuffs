# Daily P&L API Analysis (DRACC-037)

**Document Type:** API Analysis  
**Category:** Derivatives Asset - Daily Profit/Loss  
**Version:** 1.0  
**Date:** March 13, 2026

> **Note:** Phân tích API Lotte DRACC-037 (dr-daily-profit-loss) và mapping sang TradeX. Spec đầy đủ: [Daily_PnL_API_Spec](./Daily_PnL_API_Spec.md). **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) §2.1.8.

---

## 1. Overview

### 1.1 Purpose

Daily P&L API (DRACC-037) tra cứu lãi/lỗ theo từng ngày trong khoảng thời gian: lãi/lỗ đã thực hiện, chưa thực hiện, phí, thuế, net P&L — theo tài khoản/sản phẩm/hợp đồng. Tài liệu này mô tả luồng Lotte ↔ TradeX và bảng mapping.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Daily P&L | GET | `/api/v1/derivatives/asset/dailyPnl` |

**Lotte:** `[Root URL APIKEY]/tuxsvc/der/account/dr-daily-profit-loss` (POST). TradeX expose GET; backend proxy chuyển sang POST body.

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
- Pass-through Lotte messages AS-IS (code format `DAILY_PNL_{LOTTE_CODE}`)

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

## 3. API: Get Daily P&L — Analysis

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/dailyPnl`

**Lotte (DRACC-037):** [Lotte_DR.md §2.1.8](../../../Documentation/[API%20specs]Lotte_DR.md) — URL: `[Root URL APIKEY]/tuxsvc/der/account/dr-daily-profit-loss`; Method: POST; Request body JSON.

**Query Parameters (TradeX):**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh → Lotte `account_no` |
| `dateFrom` | String | ✅ | - | Từ ngày (yyyyMMdd) → Lotte `start_date` |
| `dateTo` | String | ✅ | - | Tới ngày (yyyyMMdd) → Lotte `end_date` |
| `productCode` | String | ✅ | - | Mã sản phẩm → Lotte `product_code` |
| `searchAllAccounts` | Boolean | ❌ | false | true: Tra cứu toàn bộ → Lotte `search_type` |
| `contractCode` | String | ❌ | "%" | Mã hợp đồng → Lotte `contract_code` |
| `branch` | String | ❌ | "%" | Chi nhánh → Lotte `branch` |
| `department` | String | ❌ | "%" | Phòng giao dịch → Lotte `department` |
| `nextKey` | String | ❌ | "0" | Pagination token → Lotte `next_key` |
| `fetchCount` | Number | ❌ | 50 | Records per page (max: 100) — TradeX only |

### 3.2 Request Mapping (TradeX → Lotte)

**Theo Lotte_DR §2.1.8 — Request Data (JSON):**

| Lotte Field | Type | Required | Mô tả (Lotte_DR) |
|-------------|------|----------|-------------------|
| `account_no` | String | Y | Tên/số tài khoản |
| `password` | String | Y | Mật khẩu đã mã hóa |
| `start_date` | String | Y | Từ ngày (YYYYMMDD) |
| `end_date` | String | Y | Tới ngày (YYYYMMDD) |
| `product_code` | String | Y | Mã sản phẩm |
| `search_type` | Boolean | Y | true: toàn bộ, false: từng TK |
| `next_key` | String | Y | Default "0" |
| `branch` | String | Y | Default "%" |
| `department` | String | Y | Default "%" |
| `contract_code` | String | Y | Default "%" |
| `hts_user_id` | String | Y | hts_user_id tra cứu |

**Mapping TradeX → Lotte:**

| TradeX (query / context) | Lotte Field | Transform |
|--------------------------|-------------|-----------|
| `accountNumber` | `account_no` | Direct |
| `dateFrom` | `start_date` | Direct |
| `dateTo` | `end_date` | Direct |
| `productCode` | `product_code` | Direct |
| `searchAllAccounts` | `search_type` | Direct (true/false) |
| `contractCode` | `contract_code` | Default: "%" |
| `branch` | `branch` | Default: "%" |
| `department` | `department` | Default: "%" |
| `nextKey` | `next_key` | Default: "0" |
| User đăng nhập (JWT/session) | `hts_user_id` | Auto |
| Backend secure store | `password` | Auto (không truyền từ client) |

*Lotte không có:* `row_count` / `fetchCount`. TradeX dùng `fetchCount` cho pagination phía client.

### 3.3 Response Mapping (Lotte → TradeX)

**Lotte_DR §2.1.8 — Response Data:** `error_code`, `error_desc`, `success`, `data_list`.  
**Object Types (DataResponse)** — mỗi phần tử trong `data_list`:

| Lotte Field (trong data_list[]) | Type | Mô tả (Lotte_DR) |
|--------------------------------|------|-------------------|
| `date` | String | Ngày giao dịch |
| `account_no` | String | Số tài khoản |
| `account_name` | String | Tên tài khoản |
| `product_code` | String | Mã sản phẩm |
| `contract_code` | String | Mã hợp đồng |
| `product_name` | String | Tên sản phẩm |
| `realized_profit_loss` | String | Lãi lỗ đã thực hiện |
| `unrealized_profit_loss` | String | Lãi lỗ chưa thực hiện |
| `fee` | String | Phí |
| `net_profit_loss` | String | Net lãi lỗ |
| `tax` | String | Thuế |
| `next_key` | String | Biến key (pagination) |

**Item Object (Lotte DataResponse → TradeX `items[]`):**

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

**Response Structure (TradeX):**
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

**8. Điểm cần làm rõ với Lotte:**
- Giới hạn chính xác cho `start_date`/`end_date` (vd. max 90 ngày)
- Có trả `totalRecords` hay không; nếu có thì field nào
- Vị trí `next_key` (trong từng item hay chỉ ở top-level)

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
**For:** PM / BA / Dev  
**Next Steps:** Triển khai theo [Daily_PnL_API_Spec](./Daily_PnL_API_Spec.md); xác nhận với Lotte giới hạn khoảng ngày và cách trả `totalRecords`/`next_key`.
