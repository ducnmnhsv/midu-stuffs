# Cash Statement API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Cash Transaction - Cash Statement  
**Version:** 1.0  
**Date:** February 23, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Tra cứu lịch sử thanh toán (payment/settlement history) theo khoảng thời gian. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) (27/02/2026) §2.1.4.

---

## 1. Overview

### 1.1 Purpose

Cash Statement API tra cứu **lịch sử thanh toán** phái sinh trong khoảng thời gian, bao gồm:
- Ngày giao dịch và ngày thanh toán
- Lãi/Lỗ đã thực hiện
- Phí giao dịch và thuế
- Số tiền mặt khả dụng
- Trạng thái thanh toán (lỗ/lãi, phí, thuế)

Khác với Transaction History (chi tiết từng giao dịch), Cash Statement là **sao kê thanh toán** theo ngày – tổng hợp P/L, phí, thuế.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Cash Statement | GET | `/api/v1/derivatives/cash/statement` |

### 1.3 Response Format Standards

**Success (200):**
- **Body:** Array of statement objects directly (no wrapper).
- **Empty result:** Return `[]` with HTTP 200 (no data from Lotte = empty array).
- **Pagination:** When Lotte returns `next_data`, TradeX returns it in response header `X-Next-Key` so the body stays a pure array. Client sends `nextKey` query param for load more.

```json
[
  {
    "tradeDate": "20260220",
    "settleDate": "20260221",
    "realizedPnL": 1500000,
    "fee": 125000,
    "tax": 0,
    "availableCash": 50000000,
    "shortfallAmount": 0,
    "realizedPnLPaymentStatus": "YES",
    "feePaymentStatus": "YES",
    "taxPaymentStatus": "YES",
    "bankFee": 0
  }
]
```

**Empty (200):**
```json
[]
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
- Query: Response body = array of items; no data from Lotte → `[]` with 200
- Pass-through Lotte messages AS-IS

---

## 2. Business Rules

### 2.1 Payment Status Codes (Yes/No)

Lotte trả về `Y` / `N`. TradeX map 1:1 sang `YES` / `NO`.

| Lotte Value | TradeX Value | Meaning |
|-------------|--------------|---------|
| `Y` | `YES` | Đã thanh toán |
| `N` | `NO` | Chưa thanh toán |
| *(other/empty)* | `NO` | Default khi Lotte trả giá trị khác |

### 2.2 Date Rules

| Rule | Value | Description |
|------|-------|-------------|
| Date Format | `yyyyMMdd` | e.g., `20260223` |
| Default Date | Today | If `fromDate` or `toDate` missing |
| Max Range | 30 days | `toDate - fromDate ≤ 30 days` |
| Validation | `fromDate ≤ toDate` | Start date must be ≤ end date |

### 2.3 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber | `FIELD_IS_REQUIRED` |
| Date Format | Must be yyyyMMdd | `INVALID_DATE_FORMAT` |
| Date Range | fromDate ≤ toDate | `INVALID_DATE_RANGE` |
| Date Range Limit | Max 30 days | `DATE_RANGE_EXCEEDED` |
| Fetch Count | 1 ≤ fetchCount ≤ 100 | `INVALID_FETCH_COUNT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.4 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Cash Statement

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/cash/statement`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-payment-history` (DRACC-023) — Lotte_DR §2.1.4. Request: `acnt`, `next_data` (N, lần đầu có thể " "), `date_fr`, `date_to`.

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh |
| `fromDate` | String | ❌ | Today | Ngày bắt đầu (yyyyMMdd) |
| `toDate` | String | ❌ | Today | Ngày kết thúc (yyyyMMdd) |
| `nextKey` | String | ❌ | - | Pagination token (load more – gửi từ pagination.nextKey lần trước) |
| `fetchCount` | Number | ❌ | 20 | Số bản ghi mỗi trang (1–100) |

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt` | Direct | Số tài khoản |
| `fromDate` | String | ❌ | `date_fr` | Default: today | Từ ngày |
| `toDate` | String | ❌ | `date_to` | Default: today | Đến ngày |
| `nextKey` | String | ❌ | `next_data` | Default: "0" (first page) | Pagination token – Load more |
| `fetchCount` | Number | ❌ | `row_count` | Default: 20 | Số bản ghi/trang (1–100) |
| *(JWT)* `userId` | - | - | `user_id` | Auto | Username |
| *(JWT)* `name` | - | - | `hts_user_nm` | Auto | Name |
| *(JWT)* `identifierNumber` | - | - | `idno` | Auto | ID |
| *(Request IP)* | - | - | `cli_ip_addr` | Auto | IP |
| *(Header)* | - | - | `lang_code` | Map (§2.4) | Language |

**Default Logic:**
```typescript
const today = new Date().toISOString().split('T')[0].replace(/-/g, '');
const fromDate = request.fromDate || today;
const toDate = request.toDate || today;
const fetchCount = request.fetchCount ?? 20;   // Default 20 records
const nextKey = request.nextKey || '0';        // "0" = first page

// Validate range
const daysDiff = calculateDaysDiff(fromDate, toDate);
if (daysDiff > 30) {
  throw new ValidationError('DATE_RANGE_EXCEEDED');
}
if (fetchCount < 1 || fetchCount > 100) {
  throw new ValidationError('INVALID_FETCH_COUNT');
}
```

**Note:** Request đã có `accountNumber`; response không trả lại account/accountName/subNumber (client đã biết từ context).

### 3.3 Response Mapping

**Success (200):**

**Statement Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `trade_date` | `tradeDate` | String | Direct | Ngày giao dịch (yyyyMMdd) |
| `settle_date` | `settleDate` | String | Direct | Ngày thanh toán (yyyyMMdd) |
| `loss_profit` | `realizedPnL` | Number | Parse float | Lãi/Lỗ đã thực hiện (variation margin) |
| `fee` | `fee` | Number | Parse float | Phí giao dịch |
| `tax` | `tax` | Number | Parse float | Thuế |
| `available_cash` | `availableCash` | Number | Parse float | Số tiền mặt khả dụng |
| `amount_miss` | `shortfallAmount` | Number | Parse float | Số tiền thiếu (shortfall) |
| `vm_payment_status` | `realizedPnLPaymentStatus` | String | Map (§2.1) | Đã thanh toán lỗ/lãi: `YES` \| `NO` (Lotte Y/N) |
| `fee_payment_status` | `feePaymentStatus` | String | Map (§2.1) | Đã thanh toán phí: `YES` \| `NO` |
| `tax_payment_status` | `taxPaymentStatus` | String | Map (§2.1) | Đã thanh toán thuế: `YES` \| `NO` |
| `bank_fee` | `bankFee` | Number | Parse float | Phí chuyển khoản ngân hàng |

**Pagination:**

| Lotte Field | TradeX | Description |
|-------------|--------|-------------|
| `next_data` | Response header `X-Next-Key` | Token trang tiếp theo – client gửi lại qua query param `nextKey` để load more. Rỗng hoặc absent = hết trang. |

**Response Structure (body = array):**
```json
[
  {
    "tradeDate": "20260220",
    "settleDate": "20260221",
    "realizedPnL": 1500000,
    "fee": 125000,
    "tax": 0,
    "availableCash": 50000000,
    "shortfallAmount": 0,
    "realizedPnLPaymentStatus": "YES",
    "feePaymentStatus": "YES",
    "taxPaymentStatus": "YES",
    "bankFee": 0
  }
]
```

**Empty Result (no data from Lotte):**
- HTTP 200, body = `[]`

**Load More:** Client gửi `nextKey` (từ header `X-Next-Key` lần trước) khi gọi trang tiếp. Không truyền `nextKey` = trang đầu tiên.

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `fromDate` / `toDate` | `INVALID_DATE_FORMAT` | `["fromDate"]` | Wrong format (not yyyyMMdd) |
| `fromDate` / `toDate` | `INVALID_DATE_RANGE` | `["fromDate", "toDate"]` | fromDate > toDate |
| `fromDate` / `toDate` | `DATE_RANGE_EXCEEDED` | `["30"]` | Range > 30 days |
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
| `1005` | `CASH_STATEMENT_1005` | Lỗi hệ thống |
| `1006` | `CASH_STATEMENT_1006` | Tài khoản không tồn tại |

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
  "code": "CASH_STATEMENT_{LOTTE_CODE}",
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
| Lotte Business | `CASH_STATEMENT_{LOTTE_CODE}` | `CASH_STATEMENT_1005` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

### 4.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|-------------------|
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
| **Kafka** | Service communication |

### 5.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, date format, date range
- Lotte validates: Business rules (account exists, data availability)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Return statement data
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `CASH_STATEMENT_{LOTTE_CODE}`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

**5. Pagination (Load More):**
- `fetchCount`: Default 20, min 1, max 100 – số bản ghi mỗi trang
- `nextKey`: Token trang tiếp – không truyền = trang đầu; truyền giá trị từ response header `X-Next-Key` lần trước = load more
- Response body = array; pagination token in header `X-Next-Key`. No data from Lotte → 200 với body `[]`
- Lotte: `next_data` "0" = first page; `row_count` = fetchCount (verify Lotte support)

**6. Lotte Method:**
- Lotte API là **POST** (body: acnt, next_data, date_fr, date_to)
- TradeX expose **GET** với query params (RESTful)

### 5.3 Data Transformation

**Numbers and status:**
```typescript
const mapYesNo = (v) => (String(v).toUpperCase() === 'Y' ? 'YES' : 'NO');

return {
  tradeDate: lotteResponse.trade_date,
  settleDate: lotteResponse.settle_date,
  realizedPnL: parseFloat(lotteResponse.loss_profit) || 0,
  fee: parseFloat(lotteResponse.fee) || 0,
  tax: parseFloat(lotteResponse.tax) || 0,
  availableCash: parseFloat(lotteResponse.available_cash) || 0,
  shortfallAmount: parseFloat(lotteResponse.amount_miss) || 0,
  realizedPnLPaymentStatus: mapYesNo(lotteResponse.vm_payment_status),
  feePaymentStatus: mapYesNo(lotteResponse.fee_payment_status),
  taxPaymentStatus: mapYesNo(lotteResponse.tax_payment_status),
  bankFee: parseFloat(lotteResponse.bank_fee) || 0,
};
```

---

## 6. Related APIs

### 6.1 Cash Transaction

| API Code | Name | Relationship |
|----------|------|--------------|
| DRACC-020 | Transfer History | Lịch sử chuyển nội bộ – có thể liên quan |
| DRACC-021 | VSD Transaction History | Lịch sử nộp/rút VSD – có thể xuất hiện trong sao kê |

### 6.2 Asset (Khác biệt)

| API | Name | Khác biệt |
|-----|------|-----------|
| Transaction History | Lịch sử giao dịch | Chi tiết từng giao dịch (mua/bán, KL, giá) |
| **DRACC-023** | **Cash Statement** | Sao kê thanh toán (P/L, phí, thuế theo ngày) |

---

## 7. Clarification Needed

1. **Payment Status Values:** Đã giả định Lotte trả `Y`/`N` – map sang `YES`/`NO`. Nếu Lotte dùng code khác, điều chỉnh `mapYesNo` khi triển khai.
2. **Pagination:** Lotte trả `next_data` ở đâu (root vs từng record)? Verify và set header `X-Next-Key` tương ứng.
3. **row_count:** Lotte dr-payment-history doc không liệt kê `row_count`. Nếu Lotte không hỗ trợ, TradeX có thể fetch full và slice theo fetchCount, hoặc verify tham số tương đương với Lotte.

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team  
**Estimated Effort:** 2-3 days (BE) + 1-2 days (FE) + 1 day (QA)
