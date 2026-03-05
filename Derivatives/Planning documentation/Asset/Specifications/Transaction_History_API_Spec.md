# Transaction History API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - Transaction History  
**Version:** 1.0  
**Date:** February 13, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Query transaction history with date range and type filter. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) (27/02/2026) — DRACC-023 (dr-payment-history) cho lịch sử thanh toán; nếu có API riêng transaction history thì cập nhật khi có tài liệu.

---

## 1. Overview

### 1.1 Purpose

Transaction History API tra cứu lịch sử giao dịch phái sinh trong khoảng thời gian, hỗ trợ filter theo loại giao dịch và phân trang.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Transaction History | GET | `/api/v1/derivatives/asset/transactionHistory` |

### 1.3 Response Format Standards

**Success (Query):**
```json
{
  "transactions": [...],
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
- Pass-through Lotte messages AS-IS

---

## 2. Business Rules

### 2.1 Transaction Types

| TradeX Value | Lotte Value | Description |
|--------------|-------------|-------------|
| `ALL` | *(empty)* | Tất cả giao dịch |
| `BUY` | `B` | Mua |
| `SELL` | `S` | Bán |
| `DEPOSIT` | `D` | Nộp tiền |
| `WITHDRAW` | `W` | Rút tiền |

### 2.2 Transaction Status

| Lotte Code | TradeX Enum | Vietnamese | English |
|------------|-------------|------------|---------|
| `C` | `COMPLETED` | Hoàn thành | Completed |
| `P` | `PENDING` | Đang xử lý | Pending |
| `F` | `FAILED` | Thất bại | Failed |
| `X` | `CANCELLED` | Đã hủy | Cancelled |

### 2.3 Date Rules

| Rule | Value | Description |
|------|-------|-------------|
| Date Format | `yyyyMMdd` | e.g., `20260213` |
| Default Date | Today | If `dateFrom` or `dateTo` missing |
| Max Range | 30 days | `dateTo - dateFrom ≤ 30 days` |
| Validation | `dateFrom ≤ dateTo` | Start date must be ≤ end date |

### 2.4 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, subNumber | `FIELD_IS_REQUIRED` |
| Date Format | Must be yyyyMMdd | `INVALID_DATE_FORMAT` |
| Date Range | dateFrom ≤ dateTo | `INVALID_DATE_RANGE` |
| Date Range Limit | Max 30 days | `DATE_RANGE_EXCEEDED` |
| Fetch Count | 1 ≤ fetchCount ≤ 100 | `INVALID_FETCH_COUNT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.5 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Transaction History

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/transactionHistory`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-payment-history` (DRACC-023 — Lịch sử thanh toán) — Lotte_DR §2.1.4. Request: `acnt`, `next_data`, `date_fr`, `date_to`.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh |
| `subNumber` | String | ✅ | - | Tiểu khoản |
| `dateFrom` | String | ❌ | Today | Ngày bắt đầu (yyyyMMdd) |
| `dateTo` | String | ❌ | Today | Ngày kết thúc (yyyyMMdd) |
| `transactionType` | String | ❌ | `ALL` | Filter: ALL/BUY/SELL/DEPOSIT/WITHDRAW |
| `nextKey` | String | ❌ | - | Pagination token |
| `fetchCount` | Number | ❌ | 50 | Records per page (max: 100) |

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `subNumber` | String | ✅ | `sub` | Direct | Tiểu khoản |
| `dateFrom` | String | ❌ | `date_fr` | Default: today | Từ ngày |
| `dateTo` | String | ❌ | `date_to` | Default: today | Đến ngày |
| `transactionType` | String | ❌ | `tr_tp` | Map (§2.1) | Loại GD |
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
const dateFrom = request.dateFrom || today;
const dateTo = request.dateTo || today;

// Validate range
const daysDiff = calculateDaysDiff(dateFrom, dateTo);
if (daysDiff > 30) {
  throw new ValidationError('DATE_RANGE_EXCEEDED');
}
```

**Transaction Type Mapping:**
```typescript
const typeMap = {
  'ALL': '',
  'BUY': 'B',
  'SELL': 'S',
  'DEPOSIT': 'D',
  'WITHDRAW': 'W'
};
const lotteType = typeMap[request.transactionType] || '';
```

### 3.3 Response Mapping

**Success (200):**

**Transaction Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `tr_id` | `transactionId` | String | Direct | Mã giao dịch |
| `tr_dt` | `transactionDate` | String | Direct | Ngày GD (yyyyMMdd) |
| `tr_tm` | `transactionTime` | String | Direct | Giờ GD (HH:mm:ss) |
| `sym` | `symbol` | String | Direct | Mã CK |
| `tr_tp` | `transactionType` | Enum | Map (§2.1) | Loại GD |
| `qty` | `quantity` | Number | Parse int | Khối lượng |
| `prc` | `price` | Number | Parse float | Giá |
| `amt` | `amount` | Number | Parse float | Giá trị |
| `fee` | `fee` | Number | Parse float | Phí GD |
| `tax` | `tax` | Number | Parse float | Thuế |
| `net_amt` | `netAmount` | Number | Parse float | Tổng thanh toán |
| `sts` | `status` | Enum | Map (§2.2) | Trạng thái |
| `nt` | `note` | String | Direct | Ghi chú |

**Pagination:**

| Lotte Field | TradeX Field | Type | Description |
|-------------|--------------|------|-------------|
| `has_more` | `pagination.hasMore` | Boolean | Còn trang sau |
| `next_key` | `pagination.nextKey` | String | Next page token |
| `tot_rec` | `pagination.totalRecords` | Number | Tổng số records |

**Response Structure:**
```json
{
  "transactions": [
    {
      "transactionId": "TX20260213001",
      "transactionDate": "20260213",
      "transactionTime": "09:15:30",
      "symbol": "VN30F2403",
      "transactionType": "BUY",
      "quantity": 10,
      "price": 1250.5,
      "amount": 12505000,
      "fee": 25010,
      "tax": 0,
      "netAmount": 12530010,
      "status": "COMPLETED",
      "note": "Khớp lệnh"
    }
  ],
  "pagination": {
    "hasMore": true,
    "nextKey": "TX20260213001",
    "totalRecords": 150
  }
}
```

**Empty Result:**
```json
{
  "transactions": [],
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
| `dateFrom` / `dateTo` | `INVALID_DATE_FORMAT` | `["dateFrom"]` | Wrong format (not yyyyMMdd) |
| `dateFrom` / `dateTo` | `INVALID_DATE_RANGE` | `["dateFrom", "dateTo"]` | dateFrom > dateTo |
| `dateFrom` / `dateTo` | `DATE_RANGE_EXCEEDED` | `["30"]` | Range > 30 days |
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
| `1005` | `TRANSACTION_HISTORY_1005` | Lỗi hệ thống |
| `1006` | `TRANSACTION_HISTORY_1006` | Tài khoản không tồn tại |

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
  "code": "TRANSACTION_HISTORY_{LOTTE_CODE}",
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
| Lotte Business | `TRANSACTION_HISTORY_{LOTTE_CODE}` | `TRANSACTION_HISTORY_1005` | 422 |
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
- TradeX validates: Required fields, date format, date range, fetch count
- Lotte validates: Business rules (account exists, data availability)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Return transaction data
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `TRANSACTION_HISTORY_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `TRANSACTION_HISTORY_1005`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

**5. Default Values:**
- `dateFrom`, `dateTo`: Default to today if not provided
- `transactionType`: Default `ALL` (no filter)
- `fetchCount`: Default 50, max 100

**6. Pagination:**
- Default page size: 50 records
- Max page size: 100 records
- Use `nextKey` pattern (map to Lotte `next_key`)
- Sort: By transaction date + time descending (newest first)

### 5.3 Data Transformation

**Dates:**
```typescript
const today = new Date().toISOString().split('T')[0].replace(/-/g, '');
// "2026-02-13" → "20260213"
```

**Transaction Type:**
```typescript
const statusMap = { 'C': 'COMPLETED', 'P': 'PENDING', 'F': 'FAILED', 'X': 'CANCELLED' };
const status = statusMap[lotteResponse.sts];
```

**Numbers:**
```typescript
return {
  quantity: parseInt(lotteResponse.qty),
  price: parseFloat(lotteResponse.prc),
  amount: parseFloat(lotteResponse.amt),
  // ... etc
};
```

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
