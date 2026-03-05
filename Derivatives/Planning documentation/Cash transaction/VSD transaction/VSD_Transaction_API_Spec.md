# VSD Transaction API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Cash Transaction - VSD (Vietnam Securities Depository)  
**Version:** 1.0  
**Date:** February 9, 2026

> **Note:** VSD margin deposit/withdrawal transactions (DRACC-009, DRACC-021, DRACC-032, DRACC-033, DRACC-034). **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) (27/02/2026) — §2.2.1, 2.2.4, 2.2.5, 2.2.6, 2.2.7.

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

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-balance-securities-info` (DRACC-031)

**Lotte Doc:** DRACC-031

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `inquiryDate` | String | ✅ | Ngày tra cứu (yyyyMMdd) |

### 4.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `account_no` | Direct | Số tài khoản |
| `inquiryDate` | String | ✅ | `inquiry_date` | Direct (yyyyMMdd) | Ngày tra cứu |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User from token |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `inquiryDate` | Format: yyyyMMdd | `"20260209"` |
| `hts_user_id` | Auto from JWT token | From authentication |

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `margin_cash_balance_vsd` | `vsdCashBalance` | Number | Parse to number | Số dư tiền ký quỹ tại VSD |
| `withdrawable_margin_cash` | `withdrawableCash` | Number | Parse to number | Tiền ký quỹ có thể rút |

**Example Response:**
```json
{
  "vsdCashBalance": 50000000,
  "withdrawableCash": 30000000
}
```

**Note:** 
- Only returns 2 specific fields from DRACC-031 (which has 40+ fields)
- Other balance fields available in DRACC-031 but not needed for VSD transactions
- Parse string amounts to numbers

**Field Details:**

| Field | Vietnamese | Description |
|-------|-----------|-------------|
| `vsdCashBalance` | Số dư tiền ký quỹ tại VSD | Total margin cash deposited at VSD |
| `withdrawableCash` | Tiền ký quỹ có thể rút | Amount available to withdraw from margin |

---

## 5. API: Calculate Deposit Fee

### 4.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/vsd/deposit/fee`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/get_trd_fee` (DRACC-033)

**Lotte Doc:** DRACC-033

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 4.2 Request Mapping

**TradeX Request (from FE):**

| TradeX Field | Type | Required | Description |
|--------------|------|----------|-------------|
| `accountNumber` | String | ✅ | Tài khoản |
| `subNumber` | String | ❌ | Sub (default: `"00"`) |
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
| `accountNumber` | String | ✅ | `is_act_no` | Direct | Tài khoản |
| `subNumber` | String | ❌ | `is_sub_no` | Default `"00"` | Sub |
| `amount` | Number | ✅ | `is_trd_amt` | Convert to String | Số tiền nộp |
| *(Auto from DRACC-032)* `sourceBankAccountNumber` | String | - | `is_send_bank` | Get from bank list (`.R`) | TK NH chuyển |
| *(Auto from DRACC-032)* `destinationBankAccountNumber` | String | - | `is_recv_bank` | Get from bank list (`.C`) | TK NH nhận |
| *(JWT)* `userId` | - | - | `user_id` | Auto | User from token |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `subNumber` | Default `"00"` if empty | `null` → `"00"` |
| `amount` | Convert Number to String | `10000000` → `"10000000"` |
| `sourceBankAccountNumber` | Auto-select first `.R` bank | `"129000065475"` |
| `destinationBankAccountNumber` | Auto-select first `.C` bank | `"121000065473"` |

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `data_list[0].trade_fee_amt` | `feeAmount` | Number | Parse to number | Phí giao dịch |
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

### 5.1 Request

**Endpoint:** `POST /api/v1/derivatives/transfer/vsd/deposit`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr_cw_cash_trans` (DRACC-034)

**Lotte Doc:** DRACC-034

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 5.2 Request Mapping

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
| `note` | String | ❌ | `is_ante` | Default `""` | Diễn giải |
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

### 5.3 Response Mapping

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

### 6.1 Request

**Endpoint:** `POST /api/v1/derivatives/transfer/vsd/withdraw`

**Lotte Endpoint:** `[RootURL]/tools/vcs/der/account/dr-withdrawal-deposit` (DRACC-009)

**Lotte Doc:** DRACC-009

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 6.2 Request Mapping

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
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Tài khoản |
| `amount` | Number | ✅ | `trd_amt` | Convert to String | Số tiền rút |
| `note` | String | ❌ | `cnte` | Default `""` | Diễn giải |
| *(Auto from DRACC-032)* `sourceBankAccountNumber` | String | - | `src_acnt` | Get from bank list (`.C`) | TK NH chuyển |
| *(Auto from DRACC-032)* `destinationBankAccountNumber` | String | - | `des_acnt` | Get from bank list (`.R`) | TK NH nhận |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User from token |
| *(Fixed)* | - | - | `trd_tp` | Fixed: `"C10"` | Transaction type (Withdraw) |

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
- Method is POST (not GET) for mutation operation

---

## 8. API: Query VSD Transaction History

### 7.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/vsd/history`

**Lotte Endpoint:** (URL not specified in docs) (DRACC-021)

**Lotte Doc:** DRACC-021

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Tài khoản |
| `transactionType` | String | ✅ | `C05` (Deposit), `C10` (Withdraw), `%` (All) |
| `fromDate` | String | ✅ | Từ ngày (yyyyMMdd) |
| `toDate` | String | ✅ | Đến ngày (yyyyMMdd) |
| `nextData` | String | ❌ | Pagination key |

### 7.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt` | Direct | Tài khoản |
| `transactionType` | String | ✅ | `type` | Direct | `C05`/`C10`/`%` |
| `fromDate` | String | ✅ | `date_fr` | Direct (yyyyMMdd) | Từ ngày |
| `toDate` | String | ✅ | `date_to` | Direct (yyyyMMdd) | Đến ngày |
| `nextData` | String | ❌ | `next_data` | Default `"000000000000000"` | Next key |

**Transformation Details:**

| Field | Rule | Example |
|-------|------|---------|
| `nextData` | Default `"000000000000000"` if not provided | `null` → `"000000000000000"` |
| `transactionType` | Validate: `C05`, `C10`, or `%` | - |

### 7.3 Response Mapping

**Success (200):**

| TradeX Field | Type | Description |
|--------------|------|-------------|
| `items` | Array | Transaction history list (see Transaction Object) |
| `nextData` | String | Next pagination key |

**Transaction Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `acnt` | `accountNumber` | String | Direct | Tài khoản |
| `acnt_sub` | `subNumber` | String | Direct | Số sub |
| `type` | `transactionType` | String | Direct | Phân loại (C05/C10) |
| `amount` | `amount` | Number | Parse to number | Số tiền GD |
| `target_actn` | `targetAccountNumber` | String | Direct | TK đích |
| `note` | `note` | String | Direct | Ghi chú |
| `user_executes` | `executedBy` | String | Direct | User thực hiện |
| `status_vtb` | `statusVTB` | String | Direct | Trạng thái VTB |
| `status_bos` | `statusBOS` | String | Direct | Trạng thái BOS |
| `status_vsd` | `statusVSD` | String | Direct | Trạng thái VSD |
| `trading_channel` | `tradingChannel` | String | Direct | Kênh GD |
| `source_actn` | `sourceAccountNumber` | String | Direct | TK nguồn |
| `reg_date` | `registrationDate` | String | Direct | Ngày đăng ký |
| `fees` | `feeAmount` | Number | Parse to number | Phí |
| `amount_received` | `receivedAmount` | Number | Parse to number | Số tiền thực nhận |
| `next_data` | - | - | Extract to top level | Next key |

**Example Response:**
```json
{
  "items": [
    {
      "accountNumber": "0001234567",
      "subNumber": "00",
      "transactionType": "C05",
      "amount": 10000000,
      "targetAccountNumber": "121000065473",
      "note": "Nộp ký quỹ",
      "executedBy": "USER001",
      "statusVTB": "SUCCESS",
      "statusBOS": "APPROVED",
      "statusVSD": "COMPLETED",
      "tradingChannel": "WEB",
      "sourceAccountNumber": "129000065475",
      "registrationDate": "20260209",
      "feeAmount": 50000,
      "receivedAmount": 9950000
    }
  ],
  "nextData": ""
}
```

**Status Fields:**
- `statusVTB`: VTB system status
- `statusBOS`: Back Office System status
- `statusVSD`: VSD system status

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
| `accountNumber` | `acnt_no`, `acnt`, `is_acnt_no`, `is_act_no` | Direct |
| `subNumber` | `sub_no`, `is_sub_no`, `acnt_sub` | Default `"00"` |
| `amount` | `trd_amt`, `is_trd_amt`, `is_dpo_block`, `amount` | Number → String (request), String → Number (response) |
| `note` | `cnte`, `is_ante`, `note` | Direct |
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
- [ ] Add date format validation
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
- [ ] Add pagination support
- [ ] Add date range validation
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update Swagger documentation

---

## 12. Related Documents

| Document | Location | Description |
|----------|----------|-------------|
| Lotte API Specs | `/Derivatives/Documentation/[API specs]Lotte_DR.md` | Section 2.2.1 (DRACC-009), 2.2.4 (DRACC-021), 2.2.5 (DRACC-032), 2.2.6 (DRACC-033), 2.2.7 (DRACC-034) |
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

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
