# Cash Statement API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Cash Transaction - Cash Statement  
**Version:** 1.5  
**Date:** April 3, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Tra cứu **các giao dịch tiền phát sinh** trên tài khoản (monetary transactions) theo khoảng thời gian. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) **§2.1.6 DRACC-035** — `dr-monetary-transaction`.

---

## 1. Overview

### 1.1 Purpose

Cash Statement API tra cứu **các giao dịch tiền phát sinh** (monetary transactions) trên tài khoản phái sinh trong khoảng thời gian (map **DRACC-035**), bao gồm:
- Ngày phát sinh, số thứ tự giao dịch, phân loại nghiệp vụ
- Tiền phát sinh tăng / giảm, lũy kế
- Số dư đầu kỳ / cuối kỳ, số dư chờ thanh toán
- Diễn giải, mã nghiệp vụ
- (Tùy Lotte) Tên ngân hàng vay, tiền ký quỹ

Khác với DRACC-023 (Lịch sử thanh toán – P/L, phí, thuế theo ngày), DRACC-035 là **danh sách từng dòng giao dịch tiền** (tăng/giảm, lũy kế).

**Lọc theo loại (query `transactionType`):** Mỗi giá trị TradeX map sang một hoặc nhiều lần gọi Lotte với field `type`. Riêng `OTHERS` → **7 lần gọi** Lotte với `type` lần lượt `03`, `10`, `11`, `12`, `13`, `14`, `15`, sau đó **gộp** kết quả thành một mảng. Trên **mọi dòng** response, field `transactionType` **trùng** giá trị `transactionType` client gửi lên (không map lại từ `trans_type`).

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Cash Statement | GET | `/api/v1/derivatives/cash/statement` |

### 1.3 Response Format Standards

**Success (200):**
- **Body:** Array of monetary transaction objects directly (no wrapper). Map từ Lotte DRACC-035 `data_list`.
- **Empty result:** Return `[]` with HTTP 200 (no data from Lotte = empty array).
- **Pagination:** Khi Lotte trả `next_key`, TradeX trả header `X-Next-Key`; client gửi lại `nextKey`. Với `transactionType` = `OTHERS`, `nextKey` là **opaque** (trạng thái 7 luồng) — §2.5.4.

```json
[
  {
    "transactionDate": "20260220",
    "transactionId": "12345",
    "transactionType": "CASH_DEPOSIT",
    "moneyIncrease": 1500000,
    "moneyDecrease": 0,
    "cumulative": 50000000,
    "description": "Diễn giải giao dịch",
    "dateAndIdTrans": "20260220-12345",
    "businessCode": "VM",
    "startBalance": 48500000,
    "endBalance": 50000000,
    "startDateTrans": "20260201",
    "pendingBalance": "0",
    "endDateTrans": "20260220",
    "bankName": "Ngân hàng ABC",
    "deposit": "0"
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
- Pass-through Lotte messages AS-IS (DRACC-035 error_desc)

---

## 2. Business Rules

### 2.1 DRACC-035 Request Fields (TradeX → Lotte)

| Lotte Field | Required (TradeX) | Default khi gọi Lotte | Description |
|-------------|-------------------|------------------------|-------------|
| `start_date` | ❌ | **Today** | Từ ngày (YYYYMMDD) |
| `end_date` | ❌ | **Today** | Tới ngày (YYYYMMDD) |
| `account_no` | ✅ | - | Số tài khoản |
| `sub_no` | ❌ | **"80"** | Tiểu khoản |
| `bank_code` | ❌ | **"%"** | Mã ngân hàng cho vay |
| `type` | ✅ | (sinh từ query `transactionType`, §2.5.2) | Phân loại tra cứu — mỗi lần gọi Lotte một giá trị |
| `next_key` | ❌ | "0" | Pagination |
| `hts_user_id` | Auto | (config) | Từ JWT/config |

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
| Required Fields | `accountNumber`, `transactionType` | `FIELD_IS_REQUIRED` |
| `transactionType` | Phải thuộc enum §2.5 | `INVALID_PARAMETER` + `INVALID_VALUE` |
| Date Format | Must be yyyyMMdd | `INVALID_DATE_FORMAT` |
| Date Range | fromDate ≤ toDate | `INVALID_DATE_RANGE` |
| Date Range Limit | Max 30 days | `DATE_RANGE_EXCEEDED` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.4 Language Mapping (Accept-Language → Lotte lang_code)

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

### 2.5 Query `transactionType` → Lotte `type` và response

#### 2.5.1 Giá trị hợp lệ (query)

Enum **SCREAMING_SNAKE** (query param `transactionType`):

`CASH_DEPOSIT` | `CASH_WITHDRAW` | `MARGIN_DEPOSIT` | `MARGIN_WITHDRAW` | `PROFIT_SETTLEMENT` | `LOSS_SETTLEMENT` | `DAILY_FEE_SETTLEMENT` | `TAX_SETTLEMENT` | `OTHERS`

#### 2.5.2 Map sang Lotte field `type` (DRACC-035)

**Giả định:** Lotte dùng **cùng bộ mã** cho request `type` với `trans_type` trên từng dòng (`01`…`15`, chuỗi 2 ký tự). **Cần xác nhận với Lotte/Core** khi tích hợp; nếu khác, chỉ sửa bảng dưới, không đổi contract TradeX.

| TradeX `transactionType` (query) | Hành vi TradeX → Lotte |
|----------------------------------|------------------------|
| `CASH_DEPOSIT` | 1 lần gọi, `type` = `01` |
| `CASH_WITHDRAW` | 1 lần gọi, `type` = `02` |
| `MARGIN_DEPOSIT` | 1 lần gọi, `type` = `04` |
| `MARGIN_WITHDRAW` | 1 lần gọi, `type` = `05` |
| `PROFIT_SETTLEMENT` | 1 lần gọi, `type` = `06` |
| `LOSS_SETTLEMENT` | 1 lần gọi, `type` = `07` |
| `DAILY_FEE_SETTLEMENT` | 1 lần gọi, `type` = `08` |
| `TAX_SETTLEMENT` | 1 lần gọi, `type` = `09` |
| `OTHERS` | **7 lần gọi** với `type` lần lượt: `03`, `10`, `11`, `12`, `13`, `14`, `15` (cùng `start_date`, `end_date`, `account_no`, `sub_no`, `bank_code`, `hts_user_id`; `next_key` theo §2.5.4) |

#### 2.5.3 Gộp kết quả khi `transactionType` = `OTHERS`

1. Thu `data_list` từ cả 7 response (bỏ qua response lỗi nghiệp vụ theo quy tắc chung API — hoặc fail fast nếu một lần gọi lỗi; **mặc định khuyến nghị:** nếu bất kỳ lần gọi nào lỗi → trả lỗi cho client, không gộp một phần).
2. **Dedupe:** khóa gợi ý `date_and_id_trans`; nếu trùng, giữ một bản (log cảnh báo nếu cần).
3. **Sort:** `trans_date` tăng dần, cùng ngày thì `trans_id` tăng dần (hoặc theo quy ước sản phẩm — ghi rõ trong code cho đồng nhất FE).

#### 2.5.4 Pagination (`nextKey` / `X-Next-Key`)

| `transactionType` | Cách xử lý |
|-------------------|------------|
| Một trong 8 loại đơn | Passthrough: client `nextKey` → Lotte `next_key`; header `X-Next-Key` = `next_key` Lotte trả về (như hiện tại). |
| `OTHERS` | Mỗi mã Lotte (`03`, `10`, …) có `next_key` riêng. TradeX dùng **`nextKey` opaque** (do BE định nghĩa, ví dụ encode JSON/base64 hoặc blob đã ký) lưu trạng thái 7 luồng. Khi còn trang ở **bất kỳ** luồng nào → trả header `X-Next-Key`; khi cả 7 đều hết → không trả hoặc giá trị terminal (vd. rỗng / `0` theo quy ước BE). |

#### 2.5.5 Response — field `transactionType` trên từng dòng

Với **mọi** phần tử trong mảng response, TradeX set:

`transactionType` = **đúng** giá trị query `transactionType` của request hiện tại (echo input).

**Không** map lại từ Lotte `trans_type` trên body response (kể cả khi Lotte trả `trans_type` khác lệ thuyết — client chỉ tin nhãn filter đã chọn).

---

## 3. API: Get Cash Statement

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/cash/statement`

**Lotte Endpoint (DRACC-035):** `[Root URL APIKEY]/tuxsvc/der/account/dr-monetary-transaction` — Lotte_DR §2.1.6. Method: POST. Request body: `start_date`, `end_date`, `account_no`, `sub_no`, `bank_code`, `type`, `next_key`, `hts_user_id`.

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh (→ Lotte `account_no`) |
| `transactionType` | String | ✅ | - | Loại giao dịch cần tra (enum §2.5.1) → TradeX map sang Lotte `type` (§2.5.2) |
| `fromDate` | String | ❌ | Today | Ngày bắt đầu (yyyyMMdd) → Lotte `start_date` |
| `toDate` | String | ❌ | Today | Ngày kết thúc (yyyyMMdd) → Lotte `end_date` |
| `subNo` | String | ❌ | `"80"` | Tiểu khoản (→ Lotte `sub_no`) |
| `bankCode` | String | ❌ | `"%"` | Mã ngân hàng cho vay (→ Lotte `bank_code`) |
| `nextKey` | String | ❌ | "0" | Pagination token; "0" = trang đầu (→ Lotte `next_key`) |

### 3.2 Request Mapping

**TradeX → Lotte (DRACC-035):**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `account_no` | Direct | Số tài khoản |
| `transactionType` | String | ✅ | `type` | Map (§2.5.2): 1 hoặc 7 lần gọi | Lọc loại → giá trị Lotte `type` mỗi lần gọi |
| `fromDate` | String | ❌ | `start_date` | **Default: today** (yyyyMMdd) | Từ ngày |
| `toDate` | String | ❌ | `end_date` | **Default: today** (yyyyMMdd) | Đến ngày |
| `subNo` | String | ❌ | `sub_no` | **Default: "80"** | Tiểu khoản |
| `bankCode` | String | ❌ | `bank_code` | **Default: "%"** | Mã ngân hàng cho vay |
| `nextKey` | String | ❌ | `next_key` | Default: "0" (first page) | Pagination |
| *(JWT/config)* | - | - | `hts_user_id` | Auto | HTS user (e.g. lthpt01) |

**Default Logic (khi TradeX gọi Lotte):**
- `start_date`, `end_date`: không truyền → mặc định **today** (yyyyMMdd).
- `sub_no`: không truyền → mặc định **"80"**.
- `bank_code`: không truyền → mặc định **"%"**.

```typescript
const today = formatYYYYMMDD(new Date());
const start_date = request.fromDate ?? today;
const end_date = request.toDate ?? today;
const sub_no = request.subNo ?? '80';
const bank_code = request.bankCode ?? '%';
const next_key = request.nextKey ?? '0';

const daysDiff = calculateDaysDiff(start_date, end_date);
if (daysDiff > 30) throw new ValidationError('DATE_RANGE_EXCEEDED');
if (!request.accountNumber || !request.transactionType) {
  throw new ValidationError('FIELD_IS_REQUIRED');
}
const ALLOWED = new Set([
  'CASH_DEPOSIT', 'CASH_WITHDRAW', 'MARGIN_DEPOSIT', 'MARGIN_WITHDRAW',
  'PROFIT_SETTLEMENT', 'LOSS_SETTLEMENT', 'DAILY_FEE_SETTLEMENT', 'TAX_SETTLEMENT', 'OTHERS',
]);
if (!ALLOWED.has(request.transactionType)) {
  throw new ValidationError('INVALID_VALUE', { param: 'transactionType' });
}
```

**Note:** Lotte DRACC-035 là POST với body JSON; TradeX expose GET với query params (RESTful). `hts_user_id` lấy từ JWT hoặc cấu hình (default lthpt01 theo Lotte).

### 3.3 Response Mapping

**Success (200):** Map Lotte `data_list` (DRACC-035 DataResponse) → array of objects.

**Monetary Transaction Object (Lotte → TradeX):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `trans_date` | `transactionDate` | String | Direct | Ngày phát sinh |
| `trans_id` | `transactionId` | String | Direct | Số thứ tự giao dịch |
| `trans_type` | `transactionType` | String | **Override** | Luôn = query `transactionType` của request (echo), **không** dùng `trans_type` Lotte trên body |
| `money_increase` | `moneyIncrease` | String/Number | As-is or parse | Tiền phát sinh tăng |
| `money_decrease` | `moneyDecrease` | String/Number | As-is or parse | Tiền phát sinh giảm |
| `cumulative` | `cumulative` | String | Direct | Lũy kế |
| `description` | `description` | String | Direct | Diễn giải |
| `date_and_id_trans` | `dateAndIdTrans` | String | Direct | Ngày và số thứ tự giao dịch |
| `business_code` | `businessCode` | String | Direct | Mã nghiệp vụ |
| `start_balance` | `startBalance` | String/Number | As-is or parse | Số dư đầu kỳ |
| `end_balance` | `endBalance` | String/Number | As-is or parse | Số dư cuối kỳ |
| `start_date_trans` | `startDateTrans` | String | Direct | Ngày đầu tiên phát sinh giao dịch |
| `pending_balance` | `pendingBalance` | String | Direct | Số dư chờ thanh toán |
| `end_date_trans` | `endDateTrans` | String | Direct | Ngày cuối cùng phát sinh giao dịch |
| `bank_name` | `bankName` | String | Direct | Tên ngân hàng vay |
| `deposit` | `deposit` | String | Direct | Tiền ký quỹ |

**Pagination:**

| Lotte Field | TradeX | Description |
|-------------|--------|-------------|
| `next_key` (trong response) | Response header `X-Next-Key` | Token trang tiếp – client gửi lại `nextKey`. Với 8 loại đơn: như Lotte. Với `OTHERS`: token opaque (§2.5.4). |

**Response Structure (body = array):**
```json
[
  {
    "transactionDate": "20260220",
    "transactionId": "12345",
    "transactionType": "CASH_DEPOSIT",
    "moneyIncrease": 1500000,
    "moneyDecrease": 0,
    "cumulative": "50000000",
    "description": "Diễn giải giao dịch",
    "dateAndIdTrans": "20260220-12345",
    "businessCode": "VM",
    "startBalance": 48500000,
    "endBalance": 50000000,
    "startDateTrans": "20260201",
    "pendingBalance": "0",
    "endDateTrans": "20260220",
    "bankName": "Ngân hàng ABC",
    "deposit": "0"
  }
]
```

**Empty Result (no data from Lotte):** HTTP 200, body = `[]`.

**Load More:** Client gửi `nextKey` (từ header `X-Next-Key` lần trước). Không truyền hoặc `"0"` = trang đầu.

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` / `transactionType` | `FIELD_IS_REQUIRED` | `["<field>"]` | Missing |
| `transactionType` | `INVALID_PARAMETER` | `INVALID_VALUE` + `transactionType` | Không thuộc enum §2.5.1 |
| `fromDate` / `toDate` | `INVALID_DATE_FORMAT` | `["fromDate"]` | Wrong format (not yyyyMMdd) |
| `fromDate` / `toDate` | `INVALID_DATE_RANGE` | `["fromDate", "toDate"]` | fromDate > toDate |
| `fromDate` / `toDate` | `DATE_RANGE_EXCEEDED` | `["30"]` | Range > 30 days |

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
- `nextKey`: Token trang tiếp – không truyền hoặc "0" = trang đầu; giá trị từ response header `X-Next-Key` = load more
- `transactionType` = `OTHERS`: `nextKey`/`X-Next-Key` opaque (§2.5.4), không phải một `next_key` Lotte đơn
- Response body = array; pagination token in header `X-Next-Key`. No data from Lotte → 200 với body `[]`
- Lotte DRACC-035: `next_key` trong request/response

**6. Lotte Method:**
- Lotte DRACC-035 là **POST** (body: start_date, end_date, account_no, sub_no, bank_code, type, next_key, hts_user_id)
- TradeX expose **GET** với query params (RESTful)

### 5.3 Data Transformation

**Lotte `type` từ query `transactionType` (mỗi lần gọi một mã):**
```typescript
const TX_TO_LOTTE_TYPE: Record<string, string[]> = {
  CASH_DEPOSIT: ['01'],
  CASH_WITHDRAW: ['02'],
  MARGIN_DEPOSIT: ['04'],
  MARGIN_WITHDRAW: ['05'],
  PROFIT_SETTLEMENT: ['06'],
  LOSS_SETTLEMENT: ['07'],
  DAILY_FEE_SETTLEMENT: ['08'],
  TAX_SETTLEMENT: ['09'],
  OTHERS: ['03', '10', '11', '12', '13', '14', '15'],
};
```

**`data_list` item → TradeX row** — `transactionType` **luôn** = `request.transactionType`:
```typescript
function mapMonetaryTransaction(item: LotteDataResponse, requestTransactionType: string) {
  return {
    transactionDate: item.trans_date,
    transactionId: item.trans_id,
    transactionType: requestTransactionType,
    moneyIncrease: item.money_increase,
    moneyDecrease: item.money_decrease,
    cumulative: item.cumulative,
    description: item.description,
    dateAndIdTrans: item.date_and_id_trans,
    businessCode: item.business_code,
    startBalance: item.start_balance,
    endBalance: item.end_balance,
    startDateTrans: item.start_date_trans,
    pendingBalance: item.pending_balance,
    endDateTrans: item.end_date_trans,
    bankName: item.bank_name,
    deposit: item.deposit,
  };
}
```

---

## 6. Related APIs

### 6.1 Lotte DR – Cash / Payment

| Lotte Code | Name | Relationship |
|------------|------|--------------|
| **DRACC-035** | **dr-monetary-transaction** | **Nguồn của API này** – Giao dịch tiền phát sinh (từng dòng tăng/giảm, lũy kế) |
| DRACC-023 | dr-payment-history | Lịch sử thanh toán (P/L, phí, thuế theo ngày) – API khác |
| DRACC-020 | Transfer History | Lịch sử chuyển nội bộ |
| DRACC-021 | VSD Transaction History | Lịch sử nộp/rút VSD |

### 6.2 TradeX Naming

| TradeX API | Lotte Map | Mô tả |
|------------|-----------|--------|
| Get Cash Statement | **DRACC-035** | Tra cứu giao dịch tiền phát sinh (monetary transaction) |

---

## 7. Clarification Needed

1. **Lotte `type` vs `trans_type`:** Xác nhận request `type` có đúng bộ mã `01`…`15` như §2.5.2 hay khác (nếu khác → cập nhật bảng map, giữ nguyên enum TradeX).
2. **Pagination:** Lotte trả `next_key` ở root response; với `OTHERS` cần thống nhất **format opaque** `nextKey` (BE) và test load-more đủ 7 luồng.
3. **Số dư (start_balance, end_balance):** Lotte trả String – giữ nguyên hay parse number tùy FE hiển thị.

---

**Document Status:** ✅ Complete (mapped to DRACC-035)  
**Lotte Reference:** [API specs]Lotte_DR.md §2.1.6 DRACC-035 — dr-monetary-transaction  
**For:** BA/Dev  
**Next Steps:** BE: 7-way merge + opaque pagination cho `OTHERS`; xác nhận mã `type` với Lotte  
**Estimated Effort:** 3-5 days (BE, do `OTHERS`) + 1-2 days (FE) + 1-2 days (QA)
