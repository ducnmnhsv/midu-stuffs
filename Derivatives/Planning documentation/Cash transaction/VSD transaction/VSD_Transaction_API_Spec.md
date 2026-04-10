# VSD Transaction API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Cash Transaction - VSD (Vietnam Securities Depository)  
**Version:** 1.2  
**Date:** April 8, 2026

> **Note:** VSD margin deposit/withdrawal transactions (DRACC-009, DRACC-021, DRACC-032, DRACC-033, DRACC-034). **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) (04/03/2026) — §2.2.1, 2.2.4, 2.2.5, 2.2.6, 2.2.7.

---

## 1. Overview

### 1.1 Purpose

VSD Transaction cho phép nộp/rút tiền ký quỹ từ/về VSD (Vietnam Securities Depository), bao gồm:
- **Query VSD balance**: Check số dư và số tiền có thể rút
- **Nộp tiền ký quỹ (C05)**: Chuyển tiền từ NHSV vào VSD
- **Rút tiền ký quỹ (C10)**: Chuyển tiền từ VSD về NHSV
- Query danh sách ngân hàng
- Tính phí giao dịch
- Tra cứu lịch sử

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Bank List | GET | `/api/v1/derivatives/transfer/vsd/banks` |
| Get VSD Balance | GET | `/api/v1/derivatives/transfer/vsd/balance` |
| Calculate Fee (Deposit) | POST | `/api/v1/derivatives/transfer/vsd/deposit/fee` |
| Deposit Margin (C05) | POST | `/api/v1/derivatives/transfer/vsd/deposit` |
| Withdraw Margin (C10) | POST | `/api/v1/derivatives/transfer/vsd/withdraw` |
| Query History | GET | `/api/v1/derivatives/transfer/vsd/history` |

### 1.3 Response Format Standards

**Success (Deposit/Withdraw):**
```json
{
  "success": true
}
```

**Success (Calculate Fee):**
```json
{
  "feeAmount": 50000,
  "adjustedAmount": 9950000,
  "receivedAmount": 9950000,
  "feeType": "01"
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
- NO `success: true/false` field in general responses
- **Exception:** Deposit/Withdraw mutations return `{ "success": true }` (boolean only)
- Fee calculation: Return fee details object
- Query: Return items array with pagination support
- Pass-through Lotte messages AS-IS (for errors)

---

## 2. Business Rules

### 2.1 Transaction Types

| Type Code | Name | Direction | Description |
|-----------|------|-----------|-------------|
| `C05` | Deposit | NHSV → VSD | Nộp tiền ký quỹ vào VSD |
| `C10` | Withdraw | VSD → NHSV | Rút tiền ký quỹ từ VSD |

### 2.2 Bank Routing Rules

**Bank Direction Logic:**

| Transaction | Direction | `os_biccode_bank_type` | Rule |
|-------------|-----------|------------------------|------|
| **Deposit (C05)** | NHSV → VSD | `.R` → `.C` | Source bank type = R, Dest bank type = C |
| **Withdraw (C10)** | VSD → NHSV | `.C` → `.R` | Source bank type = C, Dest bank type = R |

**Example (from DRACC-032 response):**

```json
{
  "os_biccode_bank_type": "VSDWCBVX.R",  // NHSV registered account
  "os_bank_acc_nm": "CONG TY CHUNG KHOAN NH"
}
```

```json
{
  "os_biccode_bank_type": "VSDWCBVX.C",  // VSD custody account
  "os_bank_acc_nm": "VSD KY QUY KHACH HANG NH C"
}
```

**Mapping:**
- **Deposit (C05):** `sourceBankAccountNumber` = bank with `.R`, `destinationBankAccountNumber` = bank with `.C`
- **Withdraw (C10):** `sourceBankAccountNumber` = bank with `.C`, `destinationBankAccountNumber` = bank with `.R`

### 2.3 Validation Rules

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | Varies by API (see each section) | `FIELD_IS_REQUIRED` |
| Positive Amount | amount > 0 | `INVALID_VALUE` |
| Bank Account Required | sourceBankAccountNumber, destinationBankAccountNumber must exist in bank list | `INVALID_BANK_ACCOUNT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

**Note:** Business rules (balance, limits) are validated by Lotte Core & VSD.

### 2.4 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V0307] Giao dịch thành công"` |
| `en` | `E` | `"[E0307] Transaction successful"` |
| `ko` | `K` | `"[K0307] 거래 성공"` |

---

## 3. API: Get Bank List

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/vsd/banks`

**Lotte Endpoint:** `[Root URL APIKEY]/tsol/apikey/tuxsvc/der/account/list_sec_bank_actn_dr` (DRACC-032) — Lotte_DR §2.2.5

**Lotte Doc:** DRACC-032

**Query Parameters:** None

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| *(Empty)* | - | - | - | Empty JSON `{}` | No parameters |

**Note:** DRACC-032 requires empty JSON object in request body.

### 3.3 Response Mapping

**Success (200):**

| TradeX Field | Type | Description |
|--------------|------|-------------|
| `banks` | Array | Bank account list (see Bank Object) |

**Bank Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `os_brch_code` | `branchCode` | String | Direct | Mã chi nhánh |
| `os_bank_code` | `bankCode` | String | Direct | Mã chi nhánh NH |
| `os_bank_acc_num` | `bankAccountNumber` | String | Direct | Số TK NH |
| `os_bank_acc_nm` | `bankAccountName` | String | Direct | Tên TK NH |
| `os_bank_type_nm` | `bankTypeName` | String | Direct | Loại TK NH |
| `os_biccode_bank_type` | `bicCodeBankType` | String | Direct | Mã BIC bank type |

**Bank Type Classification:**

| `bicCodeBankType` Suffix | Type | Purpose | Used For |
|---------------------------|------|---------|----------|
| `.R` | Registered | NHSV registered account | Deposit source, Withdraw dest |
| `.C` | Custody | VSD custody account | Deposit dest, Withdraw source |

**Example Response:**
```json
{
  "banks": [
    {
      "branchCode": "100",
      "bankCode": "01201004",
      "bankAccountNumber": "129000065475",
      "bankAccountName": "CONG TY CHUNG KHOAN NH",
      "bankTypeName": "CMR.Tài khoản tiền mặt đăng ký",
      "bicCodeBankType": "VSDWCBVX.R"
    },
    {
      "branchCode": "100",
      "bankCode": "01201004",
      "bankAccountNumber": "121000065473",
      "bankAccountName": "VSD KY QUY KHACH HANG NH C",
      "bankTypeName": "CMC.Tài khoản tiền ký quỹ của khách hàng",
      "bicCodeBankType": "VSDWCBVX.C"
    }
  ]
}
```

---

## 4. API: Get VSD Balance

### 4.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/vsd/balance`

**Lotte Endpoints:** DRACC-031 `dr-balance-securities-info` (vsdCashBalance, withdrawableCash); DRACC-018 (availableBalance ← `total_deposit`).

**Lotte Doc:** DRACC-031, DRACC-018

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `inquiryDate` | String | ❌ | Ngày tra cứu (yyyyMMdd). Nếu không truyền → mặc định = today khi gọi Lotte |

### 4.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `account_no` | Direct | Số tài khoản |
| `inquiryDate` | String | ❌ | `inquiry_date` | Default **today** (yyyyMMdd) nếu không truyền | Ngày tra cứu |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User from token |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `inquiryDate` | Nếu không có → dùng ngày hiện tại (yyyyMMdd); nếu có thì format yyyyMMdd | (empty) → `"20260306"`; `"20260209"` |
| `hts_user_id` | Auto from JWT token | From authentication |

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | API | TradeX Field | Type | Transform | Description |
|-------------|-----|--------------|------|-----------|-------------|
| `margin_cash_balance_vsd` | DRACC-031 | `vsdCashBalance` | Number | Parse to number | Số dư tiền ký quỹ tại VSD |
| `withdrawable_margin_cash` | DRACC-031 | `withdrawableCash` | Number | Parse to number | Tiền ký quỹ có thể rút |
| `total_deposit` | DRACC-018 | `availableBalance` | Number | Parse to number | Số dư tiền mặt khả dụng |

**Example Response:**
```json
{
  "vsdCashBalance": 50000000,
  "withdrawableCash": 30000000,
  "availableBalance": 45000000
}
```

**Note:** 
- `vsdCashBalance`, `withdrawableCash` từ DRACC-031; `availableBalance` từ DRACC-018 (`total_deposit`).
- DRACC-031 có 40+ fields; chỉ lấy 2 field balance cần thiết.
- Parse string amounts to numbers.

**Field Details:**

| Field | Vietnamese | Description |
|-------|-----------|-------------|
| `vsdCashBalance` | Số dư tiền ký quỹ tại VSD | Total margin cash deposited at VSD (DRACC-031: `margin_cash_balance_vsd`) |
| `withdrawableCash` | Tiền ký quỹ có thể rút | Amount available to withdraw from margin (DRACC-031: `withdrawable_margin_cash`) |
| `availableBalance` | Số dư tiền mặt khả dụng | Available cash balance (DRACC-018: `total_deposit`) |

---

## 5. API: Calculate Deposit Fee

### 5.1 Request

**Endpoint:** `POST /api/v1/derivatives/transfer/vsd/deposit/fee`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/get_trd_fee` (DRACC-033) — Method: POST

**Lotte Doc:** DRACC-033

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 5.2 Request Mapping

**TradeX Request (from FE):**

| TradeX Field | Type | Required | Description |
|--------------|------|----------|-------------|
| `accountNumber` | String | ✅ | Tài khoản |
| `subNumber` | String | ❌ | Sub (default: `"80"`) |
| `amount` | Number | ✅ | Số tiền nộp |

**Backend Processing (before calling Lotte):**

1. Call DRACC-032 to get bank list
2. Filter banks by type:
   - `sourceBankAccountNumber` = First bank with `.R` suffix (NHSV account)
   - `destinationBankAccountNumber` = First bank with `.C` suffix (VSD account)
3. Use filtered bank accounts in Lotte request

**TradeX → Lotte Mapping:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `is_acnt_no` | Direct | Tài khoản (Lotte 04/03) |
| `subNumber` | String | ❌ | `is_sub_no` | Default `"00"` | Sub |
| `amount` | Number | ✅ | `is_trd_amt` | Convert to String | Số tiền nộp |
| *(Auto from DRACC-032)* `sourceBankAccountNumber` | String | - | `is_send_bank` | Get from bank list (`.R`) | TK NH chuyển (Y) |
| *(Auto from DRACC-032)* `destinationBankAccountNumber` | String | - | `is_recv_bank` | Get from bank list (`.C`) | TK NH nhận (N) |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User from token |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `subNumber` | Default `"00"` if empty | `null` → `"00"` |
| `amount` | Convert Number to String | `10000000` → `"10000000"` |
| `sourceBankAccountNumber` | Auto-select first `.R` bank | `"129000065475"` |
| `destinationBankAccountNumber` | Auto-select first `.C` bank | `"121000065473"` |

### 5.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `data_list[0].trade_fee_amt` | `feeAmount` | Number | Parse to number | Phí chuyển khoản |
| `data_list[0].adjusted_amt` | `adjustedAmount` | Number | Parse to number | Số tiền điều chỉnh |
| `data_list[0].real_trd_amt` | `receivedAmount` | Number | Parse to number | Số tiền thực nhận |
| `data_list[0].fee_type` | `feeType` | String | Direct | Phân loại tính phí |

**Example Response:**
```json
{
  "feeAmount": 50000,
  "adjustedAmount": 9950000,
  "receivedAmount": 9950000,
  "feeType": "01"
}
```

**Note:** `feeType` field is used in DRACC-034 deposit request.

---

## 6. API: Deposit Margin (C05)

### 6.1 Request

**Endpoint:** `POST /api/v1/derivatives/transfer/vsd/deposit`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr_cw_cash_trans` (DRACC-034)

**Lotte Doc:** DRACC-034

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 6.2 Request Mapping

**TradeX Request (from FE):**

| TradeX Field | Type | Required | Description |
|--------------|------|----------|-------------|
| `accountNumber` | String | ✅ | Tài khoản |
| `subNumber` | String | ❌ | Sub (default: `"00"`) |
| `amount` | Number | ✅ | Số tiền nộp |
| `note` | String | ❌ | Diễn giải (optional, empty string if not provided) |
| `feeAmount` | Number | ✅ | Phí (from DRACC-033) |
| `adjustedAmount` | Number | ✅ | Số tiền điều chỉnh (from DRACC-033) |
| `receivedAmount` | Number | ✅ | Số thực nhận (from DRACC-033) |
| `feeType` | String | ✅ | Phân loại tính phí (from DRACC-033) |

**Backend Processing (before calling Lotte):**

1. Call DRACC-032 to get bank list
2. Filter banks by type:
   - `sourceBankAccountNumber` = First bank with `.R` suffix (NHSV account)
   - `destinationBankAccountNumber` = First bank with `.C` suffix (VSD account)
3. Use filtered bank accounts in Lotte request

**TradeX → Lotte Mapping:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `is_acnt_no` | Direct | Tài khoản |
| `subNumber` | String | ❌ | `is_sub_no` | Default `"00"` | Sub |
| `amount` | Number | ✅ | `is_dpo_block` | Convert to String | Số tiền nộp |
| `note` | String | ❌ | `is_cnte` | Default `""` (Lotte: Y) | Diễn giải (Lotte 04/03) |
| `feeAmount` | Number | ✅ | `is_fee_amt` | Convert to String | Phí (from DRACC-033) |
| `adjustedAmount` | Number | ✅ | `is_adj_amt` | Convert to String | Số tiền điều chỉnh (from DRACC-033) |
| `receivedAmount` | Number | ✅ | `is_acc_amt` | Convert to String | Số thực nhận (from DRACC-033) |
| `feeType` | String | ✅ | `is_fee_calc_tp` | Direct | Phân loại tính phí (from DRACC-033) |
| *(Auto from DRACC-032)* `sourceBankAccountNumber` | String | - | `is_in_bank_dest` | Get from bank list (`.R`) | TK NH chuyển |
| *(Auto from DRACC-032)* `destinationBankAccountNumber` | String | - | `is_in_bank_src` | Get from bank list (`.C`) | TK NH nhận |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User from token |
| *(Config)* | - | - | `dept_no1` | Fixed value | Mã chi nhánh (from config) |

**Important Notes:**

1. **Fee Fields:** Must call DRACC-033 (Calculate Fee) first to get:
   - `feeAmount` → `is_fee_amt`
   - `adjustedAmount` → `is_adj_amt`
   - `receivedAmount` → `is_acc_amt`
   - `feeType` → `is_fee_calc_tp`

2. **Bank Accounts (Auto-handled by BE):**
   - FE does NOT send bank accounts
   - BE calls DRACC-032 to get bank list
   - BE filters and selects:
     - `sourceBankAccountNumber` = First bank with `.R` suffix (NHSV account)
     - `destinationBankAccountNumber` = First bank with `.C` suffix (VSD account)

3. **Field Name Confusion (Lotte API):**
   - `is_in_bank_src` (source in Lotte) = **destination** in TradeX (VSD account, `.C`)
   - `is_in_bank_dest` (dest in Lotte) = **source** in TradeX (NHSV account, `.R`)
   - This is reversed naming in Lotte API!

### 6.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | - | Check = `"0000"` | Success indicator |
| `success` | `success` | Direct boolean | `true` if both `error_code="0000"` and `success=true` |

**Response Logic:**

```typescript
// BE checks both conditions
if (lotteResponse.error_code === "0000" && lotteResponse.success === true) {
  return { success: true };
} else {
  throw new Error(...); // Business error
}
```

**Example Response:**
```json
{
  "success": true
}
```

**Note:** 
- TradeX returns simple boolean instead of message
- Only returns `{ "success": true }` when both Lotte conditions met
- Any other case throws error (422)

---

## 7. API: Withdraw Margin (C10)

### 7.1 Request

**Endpoint:** `POST /api/v1/derivatives/transfer/vsd/withdraw`

**Lotte Endpoint:** `[RootURL]/tsol/apikey/tuxsvc/der/account/dr-withdrawal-deposit` (DRACC-009)

**Lotte Doc:** DRACC-009 — **Method Lotte:** GET (TradeX vẫn expose POST cho mutation)

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 7.2 Request Mapping

**TradeX Request (from FE):**

| TradeX Field | Type | Required | Description |
|--------------|------|----------|-------------|
| `accountNumber` | String | ✅ | Tài khoản |
| `amount` | Number | ✅ | Số tiền rút |
| `note` | String | ❌ | Diễn giải (optional, empty string if not provided) |

**Backend Processing (before calling Lotte):**

1. Call DRACC-032 to get bank list
2. Filter banks by type (reverse of deposit):
   - `sourceBankAccountNumber` = First bank with `.C` suffix (VSD account)
   - `destinationBankAccountNumber` = First bank with `.R` suffix (NHSV account)
3. Use filtered bank accounts in Lotte request

**TradeX → Lotte Mapping:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Tài khoản (Lotte 2.2.1) |
| `amount` | Number | ✅ | `trd_amt` | Convert to String | Số tiền rút |
| `note` | String | ❌ | `cnte` | Default `""` (Lotte: Y) | Diễn giải |
| *(Auto from DRACC-032)* `sourceBankAccountNumber` | String | - | `src_acnt` | Get from bank list (`.C`) | TK NH chuyển |
| *(Auto from DRACC-032)* `destinationBankAccountNumber` | String | - | `des_acnt` | Get from bank list (`.R`) | TK NH nhận |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User from token |
| *(Fixed)* | - | - | `trd_tp` | Fixed: `"C10"` | Phân loại (C10 rút tiền) |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `amount` | Convert Number to String | `10000000` → `"10000000"` |
| `note` | Default empty string if not provided | `null` → `""` |
| `trd_tp` | Always `"C10"` for withdraw | Fixed value |
| `sourceBankAccountNumber` | Auto-select first `.C` bank | `"121000065473"` |
| `destinationBankAccountNumber` | Auto-select first `.R` bank | `"129000065475"` |

**Bank Accounts (Auto-handled by BE):**
- FE does NOT send bank accounts
- BE calls DRACC-032 to get bank list
- BE filters and selects (reverse direction from deposit):
  - `sourceBankAccountNumber` = First bank with `.C` suffix (VSD account)
  - `destinationBankAccountNumber` = First bank with `.R` suffix (NHSV account)

### 7.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | - | Check = `"0000"` | Success indicator |
| `success` | `success` | Direct boolean | `true` if both `error_code="0000"` and `success=true` |
| `data_list[0].scrt_err_msg` | - | Optional: log/pass | Message thực hiện thành công (Lotte 04/03) |

**Response Logic:**

```typescript
// BE checks both conditions
if (lotteResponse.error_code === "0000" && lotteResponse.success === true) {
  return { success: true };
} else {
  throw new Error(...); // Business error
}
```

**Example Response:**
```json
{
  "success": true
}
```

**Note:** 
- TradeX returns simple boolean instead of message
- Only returns `{ "success": true }` when both Lotte conditions met
- Any other case throws error (422)
- Method is POST (not GET) for mutation operation
- Lotte API DRACC-009 uses **GET**; bridge layer maps TradeX POST → Lotte GET

---

## 8. API: Query VSD Transaction History

### 8.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/vsd/history`

**Lotte Endpoint:** (URL not specified in docs) (DRACC-021)

**Lotte Doc:** DRACC-021

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Tài khoản |
| `transactionType` | String | ✅ | `C05` (Deposit), `C10` (Withdraw), `%` (All) |
| `fromDate` | String | ❌ | Từ ngày (yyyyMMdd). Nếu không truyền → mặc định = today khi gọi Lotte |
| `toDate` | String | ❌ | Đến ngày (yyyyMMdd). Nếu không truyền → mặc định = today khi gọi Lotte |
| `fetchCount` | Number | ❌ | Số bản ghi cần lấy. Map sang Lotte `row_count`; nếu không truyền thì không gửi sang Lotte |
| `nextData` | String | ❌ | Pagination key |

### 8.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt` | Direct | Tài khoản |
| `transactionType` | String | ✅ | `type` | Direct | `C05`/`C10`/`%` |
| `fromDate` | String | ❌ | `date_fr` | Default **today** (yyyyMMdd) nếu không truyền | Từ ngày |
| `toDate` | String | ❌ | `date_to` | Default **today** (yyyyMMdd) nếu không truyền | Đến ngày |
| `fetchCount` | Number | ❌ | `row_count` | Chỉ gửi sang Lotte khi có giá trị; không truyền thì không gửi | Số bản ghi trả về |
| `nextData` | String | ❌ | `next_data` | Default `"000000000000000"` | Next key |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `fromDate` | Nếu không có → dùng ngày hiện tại (yyyyMMdd) | (empty) → `"20260306"` |
| `toDate` | Nếu không có → dùng ngày hiện tại (yyyyMMdd) | (empty) → `"20260306"` |
| `fetchCount` | Chỉ gửi `row_count` sang Lotte khi client truyền `fetchCount` | `20` → `row_count: 20`; (empty) → không gửi |
| `nextData` | Default `"000000000000000"` if not provided | `null` → `"000000000000000"` |
| `transactionType` | Validate: `C05`, `C10`, or `%` | - |

### 8.3 Response Mapping

**Success (200):**

| TradeX Field | Type | Description |
|--------------|------|-------------|
| `items` | Array | Transaction history list (see Transaction Object) |
| `nextData` | String \| null | Next pagination key (Lotte `next_data`; thường lấy từ phần tử cuối `data_list` hoặc theo contract Core) |

**Core / Lotte raw — ví dụ một dòng `data_list` (DRACC-021):**

```json
{
  "acnt": "039C110257",
  "acnt_sub": "80",
  "type": "C05",
  "amount": "31673210",
  "target_acnt": "121000065473",
  "note": "AUTO DEPOSIT MR",
  "user_executes": "AUTO",
  "status_vtb": "Bank chuyển khoản",
  "status_bos": "04.Nộp thành công",
  "status_vsd": "VSD nhận tiền",
  "trading_channel": "Tại quầy",
  "source_acnt": "129000065475",
  "reg_date": "2025/10/29",
  "fees": "5500",
  "amount_received": "31673210",
  "next_data": "202510290000003"
}
```

**Transaction Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `acnt` | `accountNumber` | String | Direct | Tài khoản |
| `acnt_sub` | `subNumber` | String | Direct | Số sub |
| `type` | `transactionType` | String | Direct | `C05` = Deposit (Nộp), `C10` = Withdraw (Rút); dùng kèm mã BOS để tra bảng ý nghĩa |
| `amount` | `amount` | Number | Parse to number | Số tiền GD |
| `target_acnt` | `targetAccountNumber` | String | Direct | TK đích |
| `note` | `note` | String | Direct | Ghi chú |
| `user_executes` | `executedBy` | String | Direct | User thực hiện |
| `status_vtb` | `statusVTB` | String | Direct | Trạng thái VTB (chuỗi hiển thị từ Core) |
| `status_bos` | *(internal)* | String | — | Chuỗi gốc dạng `{mã}.{mô tả}`; BE parse nội bộ |
| *(parsed từ `status_bos`)* | *(internal `bosStatusCode`)* | String \| null | **`parseBosCode`** — §rule bên dưới | Hai chữ số trước dấu `.` (vd. `"04"`); **không** trả riêng field này cho client trừ khi product bật optional `statusBOSCode` |
| *(`type` + `bosStatusCode`)* | `statusBOS` | String | Tra **§8.3.1 — cột TradeX `statusBOS`** | Hằng có nghĩa dạng **SCREAMING_SNAKE_CASE** (vd. `DEPOSIT_SUCCESSFULLY`); dùng cho điều kiện FE/i18n key — **không** trả mã `"04"` tại field này |
| *(`type` + `bosStatusCode`)* | `statusBOSDescriptionVi` | String \| null | Cùng dòng §8.3.1 | Ý nghĩa tiếng Việt chuẩn hoá |
| *(`type` + `bosStatusCode`)* | `statusBOSDescriptionEn` | String \| null | Cùng dòng §8.3.1 | Ý nghĩa tiếng Anh |
| `status_vsd` | `statusVSD` | String | Direct | Trạng thái VSD |
| `trading_channel` | `tradingChannel` | String | Direct | Kênh GD |
| `source_acnt` | `sourceAccountNumber` | String | Direct | TK nguồn (Core: `source_acnt`; alias cũ `source_actn` nếu environment khác → BE chuẩn hoá một field) |
| `reg_date` | `registrationDate` | String | Chuẩn hoá **yyyyMMdd** | Core có thể trả `yyyy/MM/dd` (vd. `2025/10/29` → `"20251029"`) |
| `fees` | `feeAmount` | Number | Parse to number | Phí |
| `amount_received` | `receivedAmount` | Number | Parse to number | Số tiền thực nhận |
| `next_data` | *(→ root `nextData`)* | String \| null | Đưa lên root response / theo dòng cuối | Phân trang |

**Rule `parseBosCode` (Lotte / Core — mặc định):**

- Lấy **đúng hai ký tự số** ngay **trước dấu `.`** đầu tiên trong `status_bos`.
- Ví dụ: `"04.Nộp thành công"` → **`"04"`**; `"01.Tạo mới"` → **`"01"`**.
- Gợi ý: regex `^(\d{2})\.` sau `trim`. Không khớp hoặc cặp `(type, bosStatusCode)` không có trong §8.3.1 → `statusBOS` = **`UNKNOWN_BOS`** (hoặc `null` nếu convention API — chốt một giá trị duy nhất toàn hệ thống); `statusBOSDescriptionVi` / `…En` = `null`.

**`transactionType` (TradeX response):**

| Value | Meaning |
|-------|---------|
| `C05` | Deposit — Nộp tiền ký quỹ |
| `C10` | Withdraw — Rút tiền ký quỹ |

#### 8.3.1 Bảng mapping: Lotte `type` + mã BOS (2 số) → TradeX `statusBOS` + mô tả

Tra theo **`(transactionType` / Lotte `type`, `bosStatusCode`)** sau `parseBosCode`. Giá trị **`statusBOS`** là **chuỗi cố định** do TradeX định nghĩa (ổn định cho FE, không đổi theo ngôn ngữ Core).

| Mã nghiệp vụ | Mã BOS (Lotte) | TradeX `statusBOS` | Ý nghĩa (VI) | Ý nghĩa (EN) |
|--------------|----------------|----------------------|--------------|--------------|
| C10 | 01 | `WITHDRAW_NEW` | Tạo mới | New |
| C10 | 02 | `WITHDRAW_APPROVED_TO_VSD` | Duyệt gửi VSD | Approved sending to VSD |
| C10 | 03 | `WITHDRAW_CANCELED` | Hủy giao dịch | Canceled |
| C10 | 04 | `WITHDRAW_SUCCESSFULLY` | Rút thành công | Finish withdrawing successfully |
| C10 | 05 | `WITHDRAW_FAILED` | Rút thất bại | Finish withdrawing failed |
| C10 | 10 | `WITHDRAW_ACCOUNTED` | Hạch toán thành công | Accounted |
| C05 | 01 | `DEPOSIT_NEW` | Tạo mới | New |
| C05 | 02 | `DEPOSIT_APPROVED_TO_BANK` | Duyệt gửi bank | Approved sending to bank |
| C05 | 03 | `DEPOSIT_CANCELED` | Hủy giao dịch | Canceled |
| C05 | 04 | `DEPOSIT_SUCCESSFULLY` | Nộp thành công | Finish depositing successfully |
| C05 | 05 | `DEPOSIT_FAILED` | Nộp thất bại | Finish depositing failed |
| C05 | 10 | `DEPOSIT_ACCOUNTED` | Hạch toán thành công | Accounted |

**Quy ước đặt tên:** `DEPOSIT_*` cho C05, `WITHDRAW_*` cho C10 — tránh trùng ý nghĩa khi cùng mã số BOS (vd. `04` nộp vs rút).

**Example Response (TradeX):**

```json
{
  "items": [
    {
      "accountNumber": "039C110257",
      "subNumber": "80",
      "transactionType": "C05",
      "amount": 31673210,
      "targetAccountNumber": "121000065473",
      "note": "AUTO DEPOSIT MR",
      "executedBy": "AUTO",
      "statusVTB": "Bank chuyển khoản",
      "statusBOS": "DEPOSIT_SUCCESSFULLY",
      "statusBOSDescriptionVi": "Nộp thành công",
      "statusBOSDescriptionEn": "Finish depositing successfully",
      "statusVSD": "VSD nhận tiền",
      "tradingChannel": "Tại quầy",
      "sourceAccountNumber": "129000065475",
      "registrationDate": "20251029",
      "feeAmount": 5500,
      "receivedAmount": 31673210
    }
  ],
  "nextData": "202510290000003"
}
```

**Status fields (tóm tắt):**

- **`statusVTB` / `statusVSD`:** chuỗi hiển thị từ Core (direct).
- **`statusBOS`:** hằng TradeX (**SCREAMING_SNAKE_CASE**, vd. `DEPOSIT_SUCCESSFULLY`) — map từ `(transactionType, bosStatusCode)` theo §8.3.1; **không** trả `"04"` tại field này.
- **`statusBOSDescriptionVi` / `statusBOSDescriptionEn`:** copy chuẩn từ cùng dòng bảng — UI có thể dùng hoặc map từ `statusBOS` qua i18n app.
- **(Optional)** `statusBOSCode`: `"04"` — chỉ nếu product cần hiển thị/audit mã gốc; mặc định spec không bắt buộc.

---

## 9. Error Mapping

### 9.1 Validation Errors (400) - TradeX

**Common Validation Errors:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `amount` | `FIELD_IS_REQUIRED` | `["amount"]` | Missing |
| `amount` | `INVALID_VALUE` | `["amount", "value", ">0"]` | ≤ 0 |
| `transactionType` | `INVALID_VALUE` | `["transactionType", "value", "C05/C10/%"]` | Invalid value (for history) |

**Note:** 
- Bank account fields are NOT validated by TradeX as they are auto-selected by BE from DRACC-032
- `note` field is optional (not validated if empty)

### 9.2 Business Errors (422) - Lotte

**Error Code Patterns:**

| Operation | Pattern | Example |
|-----------|---------|---------|
| Get Banks | `DERIVATIVES_VSD_BANK_LIST_{code}` | `DERIVATIVES_VSD_BANK_LIST_1005` |
| Get Balance | `DERIVATIVES_VSD_BALANCE_{code}` | `DERIVATIVES_VSD_BALANCE_1005` |
| Calculate Fee | `DERIVATIVES_VSD_FEE_{code}` | `DERIVATIVES_VSD_FEE_1005` |
| Deposit | `DERIVATIVES_VSD_DEPOSIT_{code}` | `DERIVATIVES_VSD_DEPOSIT_1005` |
| Withdraw | `DERIVATIVES_VSD_WITHDRAW_{code}` | `DERIVATIVES_VSD_WITHDRAW_1005` |
| History | `DERIVATIVES_VSD_HISTORY_{code}` | `DERIVATIVES_VSD_HISTORY_1005` |

**Common Lotte Error Codes:**

| Lotte Code | Description (Vietnamese) | Description (English) |
|------------|--------------------------|----------------------|
| `1005` | Số dư không đủ | Insufficient balance |
| `2001` | Tài khoản không hợp lệ | Invalid account |
| `3001` | Ngân hàng không hợp lệ | Invalid bank account |
| `4001` | VSD hệ thống bảo trì | VSD system maintenance |
| `5001` | Vượt hạn mức giao dịch | Exceeded transaction limit |

---

## 10. Field Mapping Reference

### 10.1 Common Patterns

| TradeX Field | Lotte Field Variations | Transform Rule |
|--------------|------------------------|----------------|
| `accountNumber` | `acnt_no`, `acnt`, `is_acnt_no` (Lotte 04/03) | Direct |
| `subNumber` | `sub_no`, `is_sub_no`, `acnt_sub` | Default `"00"` |
| `amount` | `trd_amt`, `is_trd_amt`, `is_dpo_block`, `amount` | Number → String (request), String → Number (response) |
| `note` | `cnte`, `is_cnte` (DRACC-034, Lotte 04/03), `note` | Direct |
| `feeAmount` | `trade_fee_amt`, `is_fee_amt`, `fees` | Number ↔ String |

### 10.2 Bank Account Field Mapping

**Deposit (DRACC-034):**

| TradeX Concept | TradeX Field | Lotte Field | Bank Type |
|----------------|--------------|-------------|-----------|
| Source (NHSV) | `sourceBankAccountNumber` | `is_in_bank_dest` | `.R` |
| Destination (VSD) | `destinationBankAccountNumber` | `is_in_bank_src` | `.C` |

**⚠️ Warning:** Lotte field names are reversed (`src`/`dest` swapped)!

**Withdraw (DRACC-009):**

| TradeX Concept | TradeX Field | Lotte Field | Bank Type |
|----------------|--------------|-------------|-----------|
| Source (VSD) | `sourceBankAccountNumber` | `src_acnt` | `.C` |
| Destination (NHSV) | `destinationBankAccountNumber` | `des_acnt` | `.R` |

### 10.3 Type Transformations

**Request (TradeX → Lotte):**

| TradeX Type | Lotte Type | Example |
|-------------|------------|---------|
| Number | String | `10000000` → `"10000000"` |
| String | String | `"Transfer"` → `"Transfer"` |

**Response (Lotte → TradeX):**

| Lotte Type | TradeX Type | Transformation |
|------------|-------------|----------------|
| String (amount) | Number | `"10000000"` → `10000000` |
| String (date) | String | `"20260209"` → `"20260209"` |

---

## 11. Implementation Notes

### 11.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| `derivatives-service` | Business logic, bank routing, validation |
| **Kafka** | Service communication |

### 11.2 Key Principles

**1. Two-Step Deposit Flow:**
```
Step 1: Call DRACC-033 (Calculate Fee)
        → Get feeAmount, adjustedAmount, receivedAmount, feeType
        
Step 2: Call DRACC-034 (Deposit) with fee data from Step 1
        → Execute deposit transaction
```

**2. Bank Routing Logic:**
- **Implementation:** Create helper function to filter banks by `.R` or `.C` suffix
- **Validation:** Ensure selected bank accounts match transaction direction
- **Error:** Return `INVALID_BANK_ACCOUNT` if bank not found or wrong type

**3. Field Name Confusion (DRACC-034):**
- Lotte's `is_in_bank_src` = TradeX's destination (VSD, `.C`)
- Lotte's `is_in_bank_dest` = TradeX's source (NHSV, `.R`)
- **Action:** Document this clearly in code comments!

**4. Validation Strategy:**
- TradeX validates: Required fields, data types, format, bank account existence
- Lotte/VSD validates: Business rules (balance, limits, VSD system rules)
- NO duplicate business logic

**5. Auto-Population:**
- `userId` → From JWT token
- `dept_no1` → From configuration (branch code)
- `lang_code` → From `Accept-Language` header

**6. Amount Precision:**
- Maintain full precision when converting
- NO rounding for financial amounts

### 11.3 Bank Routing Helper

**Pseudo-code:**

```typescript
// Get banks by type from DRACC-032 response
function getBanksByType(banks: Bank[], type: 'R' | 'C'): Bank[] {
  return banks.filter(bank => 
    bank.bicCodeBankType.endsWith(`.${type}`)
  );
}

// Auto-select bank accounts for deposit (called by BE before DRACC-033 & DRACC-034)
function getBankAccountsForDeposit(allBanks: Bank[]): BankAccounts {
  const sourceBanks = getBanksByType(allBanks, 'R');  // NHSV accounts
  const destBanks = getBanksByType(allBanks, 'C');    // VSD accounts
  
  if (sourceBanks.length === 0) {
    throw new Error('No NHSV bank account (.R) found');
  }
  if (destBanks.length === 0) {
    throw new Error('No VSD bank account (.C) found');
  }
  
  return {
    sourceBankAccountNumber: sourceBanks[0].bankAccountNumber,      // .R
    destinationBankAccountNumber: destBanks[0].bankAccountNumber    // .C
  };
}

// Auto-select bank accounts for withdraw (reverse direction)
function getBankAccountsForWithdraw(allBanks: Bank[]): BankAccounts {
  const sourceBanks = getBanksByType(allBanks, 'C');  // VSD accounts
  const destBanks = getBanksByType(allBanks, 'R');    // NHSV accounts
  
  if (sourceBanks.length === 0) {
    throw new Error('No VSD bank account (.C) found');
  }
  if (destBanks.length === 0) {
    throw new Error('No NHSV bank account (.R) found');
  }
  
  return {
    sourceBankAccountNumber: sourceBanks[0].bankAccountNumber,      // .C
    destinationBankAccountNumber: destBanks[0].bankAccountNumber    // .R
  };
}

// Usage in service layer
async function depositMargin(request: DepositRequest): Promise<DepositResponse> {
  // Step 1: Get bank list from DRACC-032
  const bankList = await getBankList();
  
  // Step 2: Auto-select bank accounts
  const { sourceBankAccountNumber, destinationBankAccountNumber } = 
    getBankAccountsForDeposit(bankList.banks);
  
  // Step 3: Calculate fee (DRACC-033)
  const feeData = await calculateFee({
    accountNumber: request.accountNumber,
    subNumber: request.subNumber,
    amount: request.amount,
    sourceBankAccountNumber,      // Auto from bank list
    destinationBankAccountNumber  // Auto from bank list
  });
  
  // Step 4: Execute deposit (DRACC-034)
  return await executeDeposit({
    ...request,
    ...feeData,
    sourceBankAccountNumber,      // Auto from bank list
    destinationBankAccountNumber  // Auto from bank list
  });
}
```

### 11.4 Implementation Checklist

**DRACC-031 (Get Balance):**
- [ ] Create DTO: `DerivativesVsdBalanceRequest`
- [ ] Create DTO: `DerivativesVsdBalanceResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Default `inquiryDate` to today when not provided; validate format when provided
- [ ] Extract specific fields from large response
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

**DRACC-032 (Get Banks):**
- [ ] Create DTO: `DerivativesVsdBankListResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Add caching (banks list rarely changes)
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

**DRACC-033 (Calculate Fee):**
- [ ] Create DTO: `DerivativesVsdDepositFeeRequest`
- [ ] Create DTO: `DerivativesVsdDepositFeeResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Add validation
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

**DRACC-034 (Deposit):**
- [ ] Create DTO: `DerivativesVsdDepositRequest`
- [ ] Create DTO: `DerivativesVsdDepositResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Implement bank routing validation
- [ ] Handle reversed Lotte field names
- [ ] Add integration with DRACC-033
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

**DRACC-009 (Withdraw):**
- [ ] Create DTO: `DerivativesVsdWithdrawRequest`
- [ ] Create DTO: `DerivativesVsdWithdrawResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Implement bank routing validation (reverse of deposit)
- [ ] Add fixed `trd_tp: "C10"` field
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

**DRACC-021 (History):**
- [ ] Create DTO: `DerivativesVsdHistoryRequest`
- [ ] Create DTO: `DerivativesVsdHistoryResponse`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Default `fromDate`/`toDate` = today (yyyyMMdd) when not provided
- [ ] Map `fetchCount` → `row_count` (only send to Lotte when `fetchCount` present)
- [ ] Add pagination support (`nextData` / `next_data`)
- [ ] Add date format validation when provided
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

---

## 12. Related Documents

| Document | Location | Description |
|----------|----------|-------------|
| Lotte API Specs | `/Derivatives/Documentation/[API specs]Lotte_DR.md` | §2.2.1 (DRACC-009), 2.2.4 (DRACC-021), 2.2.5 (DRACC-032), 2.2.6 (DRACC-033), 2.2.7 (DRACC-034). **Sync:** 04/03/2026 — URL DRACC-009 `tsol/apikey/...`, request `is_acnt_no`/`is_cnte`, response `target_acnt`, `scrt_err_msg`. |
| Internal Transfer API Spec | `../Internal transfer/Internal_Transfer_API_Spec.md` | Similar patterns for cash transaction |
| TradeX API Conventions | `@TradeX Knowledge/API Standards/tradex-api-conventions.md` | API naming & response standards |
| Order API Standards | Rule `@tradex-order-api-response-standards` | Response format patterns |
| Regular Orders API Spec | `../Order/Specifications/Regular_Orders_API_Spec.md` | API specification format reference |

---

## 13. Open Questions

### 13.1 Technical Questions

1. **DRACC-021 URL:** Specific endpoint needs confirmation (currently not specified in docs)
2. **Bank List Caching:** How long to cache bank list? Refresh strategy?
3. **Fee Expiration:** How long is fee calculation valid before re-calculating?
4. **Transaction Status:** How to poll VSD transaction status? Real-time updates?
5. **Concurrent Transactions:** How to handle multiple deposits/withdrawals?

### 13.2 Business Questions

1. **Deposit Limits:** Min/max amounts? Daily limits?
2. **Withdrawal Limits:** Min/max amounts? Daily limits?
3. **Business Hours:** VSD operating hours? Cutoff times?
4. **Transaction Fees:** Fee structure? Who pays (customer/NHSV)?
5. **Failed Transactions:** Retry policy? Reversal process?
6. **VSD Status Mapping:** Complete list of VSD status codes?

---

**Document Status:** ✅ Complete (Lotte_DR 04/03/2026; §8.3: `statusBOS` = hằng TradeX §8.3.1, không mã số)  
**For:** BA/Dev  
**Next Steps:** Implementation — `parseBosCode` → map §8.3.1 → `DEPOSIT_*` / `WITHDRAW_*`; Core `source_acnt`; `reg_date` → `yyyyMMdd`
