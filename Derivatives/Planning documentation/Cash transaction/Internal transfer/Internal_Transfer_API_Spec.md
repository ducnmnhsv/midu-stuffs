# Internal Transfer API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Cash Transaction - Internal Transfer  
**Version:** 1.0  
**Date:** February 9, 2026

> **Note:** Internal cash transfer between derivatives sub-accounts (DRACC-019, DRACC-020)

---

## 1. Overview

### 1.1 Purpose

Internal Transfer cho phép chuyển tiền giữa các sub-accounts trong tài khoản phái sinh, bao gồm:
- Chuyển giữa sub-accounts trong cùng tài khoản
- Chuyển giữa các tài khoản khác nhau (nếu được phép)

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Transfer Cash | POST | `/api/v1/derivatives/transfer/cash` |
| Query History | GET | `/api/v1/derivatives/transfer/cash/history` |

### 1.3 Response Format Standards

**Success (Transfer):**
```json
{
  "transactionDate": "20260209",
  "outSequenceNumber": "001",
  "outPreviousCashBalance": 50000000,
  "outCashBalance": 40000000,
  "inSequenceNumber": "002",
  "inPreviousCashBalance": 5000000,
  "inCashBalance": 15000000
}
```

**Success (Query):**
```json
{
  "items": [...],
  "nextData": ""
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
  "params": [...]
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Transfer: Return transaction details with balances
- Query: Return items array with pagination support
- Pass-through Lotte messages AS-IS

---

## 2. Business Rules

### 2.1 Transfer Rules

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, subNumber, receivedAccountNumber, receivedSubNumber, amount, note | `FIELD_IS_REQUIRED` |
| Positive Amount | amount > 0 | `INVALID_VALUE` |
| Sufficient Balance | Sender must have sufficient balance | Lotte validates |
| Same Sub Restriction | Cannot transfer to same account+sub | Lotte validates |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

**Note:** Business rules (balance, limits) are validated by Lotte Core.

### 2.2 History Query Rules

| Rule | Description |
|------|-------------|
| Date Range | fromDate ≤ toDate, format: yyyyMMdd |
| Maximum Range | Recommended: 3-6 months (Lotte limitation) |
| Pagination | Use `nextData` for subsequent pages |

### 2.3 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V0307] Chuyển tiền thành công"` |
| `en` | `E` | `"[E0307] Transfer successful"` |
| `ko` | `K` | `"[K0307] 성공적으로 전송되었습니다"` |

---

## 3. API: Internal Cash Transfer

### 3.1 Request

**Endpoint:** `POST /api/v1/derivatives/transfer/cash`

**Lotte Endpoint:** `[Root URL APIKEY]` (specific endpoint TBD - DRACC-019)

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `snd_actn` | Uppercase | Số TK chuyển |
| `subNumber` | String | ✅ | `snd_sub` | Default `"00"` | Sub chuyển |
| `receivedAccountNumber` | String | ✅ | `rcv_actn` | Uppercase | Số TK nhận |
| `receivedSubNumber` | String | ✅ | `rcv_sub` | Direct | Sub nhận |
| `amount` | Number | ✅ | `amount` | Direct | Số tiền |
| `note` | String | ✅ | `remark` | Direct | Nội dung |
| *(JWT)* `userId` | - | - | `user_id` | Auto | User from token |
| *(Header)* | - | - | `lang_code` | Map (§2.3) | Language code |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `accountNumber` | Always uppercase | `"0001234567"` → `"0001234567"` |
| `subNumber` | Default `"00"` if empty | `null` → `"00"` |
| `receivedAccountNumber` | Always uppercase | `"0001234567"` → `"0001234567"` |

### 3.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `error_code` | - | - | Check = `"0000"` | Success indicator |
| `data_list[0].date` | `transactionDate` | String | Direct | Ngày GD (yyyyMMdd) |
| `data_list[0].no` | `outSequenceNumber` | String | Direct | Số TT của sub nguồn |
| `data_list[0].balance_before_transfer` | `outPreviousCashBalance` | Number | Parse to number | Số dư trước (nguồn) |
| `data_list[0].balance_after_transfer` | `outCashBalance` | Number | Parse to number | Số dư sau (nguồn) |
| `data_list[0].no_receive` | `inSequenceNumber` | String | Direct | Số TT của sub đích |
| `data_list[0].balance_before_transfer_receive` | `inPreviousCashBalance` | Number | Parse to number | Số dư trước (đích) |
| `data_list[0].balance_after_transfer_receive` | `inCashBalance` | Number | Parse to number | Số dư sau (đích) |

**Note:** 
- Parse string amounts to numbers (maintain precision, no rounding)
- Both sender and receiver balances returned in single response

**Error (422) - Lotte Business Rules:**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | `code` | Prefix: `DERIVATIVES_CASH_TRANSFER_{code}` | Example: `"DERIVATIVES_CASH_TRANSFER_1005"` |
| `error_desc` | `message` | Pass-through AS-IS | Lotte error message |

**Common Lotte Error Codes:**

| Lotte Code | TradeX Code | Description (Vietnamese) |
|------------|-------------|--------------------------|
| `1005` | `DERIVATIVES_CASH_TRANSFER_1005` | Số dư không đủ |
| `2001` | `DERIVATIVES_CASH_TRANSFER_2001` | Tài khoản không hợp lệ |
| `3001` | `DERIVATIVES_CASH_TRANSFER_3001` | Sub không tồn tại |
| `4001` | `DERIVATIVES_CASH_TRANSFER_4001` | Không thể chuyển cùng sub |

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `subNumber` | `FIELD_IS_REQUIRED` | `["subNumber"]` | Missing |
| `receivedAccountNumber` | `FIELD_IS_REQUIRED` | `["receivedAccountNumber"]` | Missing |
| `receivedSubNumber` | `FIELD_IS_REQUIRED` | `["receivedSubNumber"]` | Missing |
| `amount` | `FIELD_IS_REQUIRED` | `["amount"]` | Missing |
| `amount` | `INVALID_VALUE` | `["amount", "value", ">0"]` | ≤ 0 |
| `note` | `FIELD_IS_REQUIRED` | `["note"]` | Missing |

**Auth Error (401):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `UNAUTHORIZED` | Token không hợp lệ hoặc đã hết hạn | Invalid token |
| `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Token expired |

**Auth Error (403):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `FORBIDDEN` | Không có quyền truy cập | No permission |
| `UNAUTHORIZED_ACCOUNT` | Tài khoản không thuộc quyền sở hữu của bạn | Account ownership failed |

---

## 4. API: Query Transfer History

### 4.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/cash/history`

**Lotte Endpoint:** `[Root URL APIKEY]/tsol/apikey/tuxsvc/der/account/dr-fund-transfer-history` (DRACC-020)

**Lotte Doc:** DRACC-020

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `fromDate` | String | ✅ | Từ ngày (yyyyMMdd) |
| `toDate` | String | ✅ | Đến ngày (yyyyMMdd) |
| `nextData` | String | ❌ | Pagination key (empty for first page) |

### 4.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `account` | Uppercase | Số TK |
| `fromDate` | String | ✅ | `date_fr` | Direct (yyyyMMdd) | Từ ngày |
| `toDate` | String | ✅ | `date_to` | Direct (yyyyMMdd) | Đến ngày |
| `nextData` | String | ❌ | `next_data` | Default `""` | Pagination key |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `accountNumber` | Always uppercase | `"0001234567"` → `"0001234567"` |
| `fromDate` | Format: yyyyMMdd | `"20260101"` |
| `toDate` | Format: yyyyMMdd | `"20260209"` |
| `nextData` | Empty string if not provided | `null` → `""` |

### 4.3 Response Mapping

**Success (200):**

| TradeX Field | Type | Description |
|--------------|------|-------------|
| `items` | Array | Transfer history list (see Transfer Object) |
| `nextData` | String | Next pagination key (empty if no more data) |

**Transfer Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `trade_date` | `transactionDate` | String | Direct | Ngày GD (yyyyMMdd) |
| `send_acc` | `sendAccountNumber` | String | Direct | Số TK chuyển |
| `send_acc_sub` | `sendSubNumber` | String | Direct | Sub chuyển |
| `send_acc_name` | `sendAccountName` | String | Direct | Tên TK chuyển |
| `seq_no` | `sequenceNumber` | String | Direct | Số TT GD |
| `amount` | `amount` | Number | Parse to number | Số tiền |
| `recv_acc` | `receivedAccountNumber` | String | Direct | Số TK nhận |
| `recv_acc_sub` | `receivedSubNumber` | String | Direct | Sub nhận |
| `recv_acc_name` | `receivedAccountName` | String | Direct | Tên TK nhận |
| `remarks` | `note` | String | Direct | Nội dung |
| `trading_channel` | `tradingChannel` | String | Direct | Kênh GD |
| `iscanceled` | `isCanceled` | Boolean | `"Y"→true`, `"N"→false` | Trạng thái hủy |
| `next_data` | - | - | Extract to top level | Next key |

**Cancel Status Mapping:**

| Lotte `iscanceled` | TradeX `isCanceled` | Description |
|--------------------|---------------------|-------------|
| `"Y"` | `true` | Đã hủy |
| `"N"` | `false` | Chưa hủy |

**Empty Result:**
```json
{
  "items": [],
  "nextData": ""
}
```

**Pagination Flow:**

| Request # | nextData Input | nextData Output | Status |
|-----------|---------------|-----------------|--------|
| 1 | `""` | `"ABC123"` | More data available |
| 2 | `"ABC123"` | `"DEF456"` | More data available |
| 3 | `"DEF456"` | `""` | End of data |

### 4.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `fromDate` | `FIELD_IS_REQUIRED` | `["fromDate"]` | Missing |
| `toDate` | `FIELD_IS_REQUIRED` | `["toDate"]` | Missing |
| `fromDate` | `INVALID_FORMAT` | `["fromDate", "format", "yyyyMMdd"]` | Invalid date format |
| `toDate` | `INVALID_FORMAT` | `["toDate", "format", "yyyyMMdd"]` | Invalid date format |
| Date Range | `INVALID_VALUE` | `["toDate", "must_be_after", "fromDate"]` | toDate < fromDate |

**Error (422) - Lotte Business Rules:**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | `code` | Prefix: `DERIVATIVES_TRANSFER_HISTORY_{code}` | Example: `"DERIVATIVES_TRANSFER_HISTORY_1005"` |
| `error_desc` | `message` | Pass-through AS-IS | Lotte error message |

**Common Lotte Error Codes:**

| Lotte Code | TradeX Code | Description (Vietnamese) |
|------------|-------------|--------------------------|
| `1005` | `DERIVATIVES_TRANSFER_HISTORY_1005` | Lỗi tra cứu lịch sử |
| `2001` | `DERIVATIVES_TRANSFER_HISTORY_2001` | Tài khoản không hợp lệ |
| `3001` | `DERIVATIVES_TRANSFER_HISTORY_3001` | Khoảng thời gian không hợp lệ |

---

## 5. Field Mapping Reference

### 5.1 Common Patterns

| TradeX Field Pattern | Lotte Field Pattern | Transform Rule |
|---------------------|-------------------|----------------|
| `accountNumber` | `account` / `snd_actn` / `acnt_no` | Always uppercase |
| `subNumber` | `sub_no` / `snd_sub` | Default `"00"` if empty |
| `amount` | `amount` | Number (TradeX) ↔ String (Lotte) |
| `note` | `remark` / `remarks` / `cnte` | Direct mapping |
| `transactionDate` | `date` / `trade_date` | Format: yyyyMMdd |

### 5.2 Type Transformations

**Request (TradeX → Lotte):**

| TradeX Type | Lotte Type | Example |
|-------------|------------|---------|
| Number | Number | `10000000` → `10000000` |
| String | String | `"Transfer"` → `"Transfer"` |
| String (account) | String (uppercase) | `"0001234567"` → `"0001234567"` |

**Response (Lotte → TradeX):**

| Lotte Type | TradeX Type | Transformation |
|------------|-------------|----------------|
| String (amount) | Number | `"10000000"` → `10000000` |
| String (`"Y"`/`"N"`) | Boolean | `"Y"` → `true`, `"N"` → `false` |
| String (date) | String | `"20260209"` → `"20260209"` |

### 5.3 Naming Consistency

**Important:** Derivatives uses different naming than Equity!

| Purpose | Derivatives | Equity (different!) |
|---------|-------------|---------------------|
| received account | `receivedAccountNumber` | `receivedAccountNumber` |
| received sub | `receivedSubNumber` | `receivedSubNumber` |

**Action:** Always use Derivatives naming. Do NOT copy Equity patterns.

---

## 6. Error Handling Standards

### 6.1 Error Code Patterns

| Operation | Error Code Pattern | Example |
|-----------|-------------------|---------|
| Cash Transfer | `DERIVATIVES_CASH_TRANSFER_{lotte_code}` | `DERIVATIVES_CASH_TRANSFER_1005` |
| Transfer History | `DERIVATIVES_TRANSFER_HISTORY_{lotte_code}` | `DERIVATIVES_TRANSFER_HISTORY_1005` |

### 6.2 Success Criteria

| Lotte `error_code` | Status | Action |
|--------------------|--------|--------|
| `"0000"` | Success | Return mapped response |
| Any other | Failure | Throw error with pattern above |

### 6.3 Error Response Format

**Validation Error:**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "accountNumber" }
  ]
}
```

**Lotte Pass-Through Error:**
```json
{
  "code": "DERIVATIVES_CASH_TRANSFER_1005",
  "message": "[V1234] Số dư không đủ để thực hiện giao dịch"
}
```

---

## 7. Implementation Notes

### 7.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| `derivatives-service` | Business logic, validation (for TradeX-native features) |
| **Kafka** | Service communication |

### 7.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, data types, format, account ownership
- Lotte validates: Business rules (balance, limits, account validity)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Pass-through Lotte `error_desc` AS-IS (if applicable)
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `DERIVATIVES_{OPERATION}_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `DERIVATIVES_CASH_TRANSFER_1005`

**4. Auto-Population:**
- `userId` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

**5. Amount Precision:**
- Maintain full precision when parsing string to number
- NO rounding
- Use appropriate data type (e.g., `Decimal` for financial amounts)

### 7.3 Field Naming Standards

Follow TradeX API Naming Conventions (rule `@tradex-api-naming`):

**Request DTOs:**
- Pattern: `Derivatives{Operation}Request`
- Example: `DerivativesCashTransferRequest`

**Response DTOs:**
- Pattern: `Derivatives{Operation}Response`
- Example: `DerivativesCashTransferResponse`

**Service Methods:**
- Pattern: `{operation}{Entity}`
- Example: `transferCash`, `queryTransferHistory`

### 7.4 Implementation Checklist

**DRACC-019 (Transfer):**
- [ ] Create DTO: `DerivativesCashTransferRequest`
- [ ] Create DTO: `DerivativesCashTransferResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Add validation (required fields, amount > 0)
- [ ] Implement field mapping (uppercase accounts, parse amounts)
- [ ] Add error handling with proper prefixes
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

**DRACC-020 (History):**
- [ ] Create DTO: `DerivativesCashTransferHistoryRequest`
- [ ] Create DTO: `DerivativesCashTransferHistoryResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Add validation (date format, date range)
- [ ] Implement pagination support
- [ ] Implement field mapping (parse amounts, boolean conversion)
- [ ] Add error handling
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

---

## 8. Related Documents

| Document | Location | Description |
|----------|----------|-------------|
| Lotte API Specs | `/Derivatives/Documentation/[API specs]Lotte_DR.md` | Section 2.2.2 (DRACC-019), 2.2.3 (DRACC-020) |
| TradeX API Conventions | `@TradeX Knowledge/API Standards/tradex-api-conventions.md` | API naming & response standards |
| Order API Standards | Rule `@tradex-order-api-response-standards` | Response format patterns |
| Equity Transfer (Reference) | TradeX MCP Knowledge | Similar patterns for Equity market |
| Regular Orders API Spec | `../Order/Specifications/Regular_Orders_API_Spec.md` | API specification format reference |

---

## 9. Open Questions

### 9.1 Technical Questions

1. **DRACC-019 URL:** Specific endpoint needs confirmation (currently `[Root URL APIKEY]`)
2. **Maximum Date Range:** Confirm allowed date range for history query (3 months? 6 months?)
3. **Page Size:** Default/maximum page size for history query?
4. **Real-time Updates:** WebSocket notifications for transfers?

### 9.2 Business Questions

1. **Cross-Account Transfer:** Requires additional authorization? Rules?
2. **Transfer Limits:** Minimum/maximum amounts? Daily limits?
3. **Transaction Fees:** Are there fees for internal transfers?
4. **Business Hours:** Available 24/7 or trading hours only?
5. **Concurrent Transfers:** How to handle multiple simultaneous transfers?

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
