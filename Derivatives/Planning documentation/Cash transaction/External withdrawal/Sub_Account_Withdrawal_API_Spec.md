# Sub Account Withdrawal API Specification (Derivatives)

**Document Type:** API Specification
**Category:** Derivatives Cash Transaction - Sub Account Withdrawal (Rút tiền sub phái sinh về ngân hàng)
**Version:** 0.1 (Draft — pending PM confirmation)
**Date:** July 9, 2026

**Integration type:** Lotte-integrated (via `lotte-bridge` → Core Lotte)

> **Note:** Rút tiền mặt từ sub phái sinh về **tài khoản ngân hàng ngoài** (external bank account) — DRACC-039 (History), DRACC-040 (Execute), DRACC-041 (Balance). Đây là domain **mới, khác** VSD Transaction (nộp/rút margin NHSV↔VSD, DRACC-009/021/032/033/034) và Internal Transfer (chuyển giữa các sub, DRACC-019/020). **Tham chiếu Lotte:** `derivatives_api_doc.txt` §3.2 GIAO DỊCH TIỀN PHÁI SINH, lines 4079–4786. Changelog Lotte: "6/7/2026 - HuongLT - Thêm mới DRACC-039,040,041" → 3 API hoàn toàn mới phía Lotte.
>
> ⚠️ **Đây là bản DRAFT.** 6 Open Questions ở §10 **chưa được PM xác nhận với HuongLT/VanND** — field list và response schema có thể thay đổi sau khi có xác nhận, đặc biệt Open Question #1 (request field của DRACC-041) và #2 (nguồn `bankAccount`).

---

## Table of Contents

1. [Overview](#1-overview)
2. [Business Rules](#2-business-rules)
3. [API: Query Withdrawal History (DRACC-039)](#3-api-query-withdrawal-history-dracc-039)
4. [API: Execute Withdrawal (DRACC-040)](#4-api-execute-withdrawal-dracc-040)
5. [API: Query Available Withdrawal Balance (DRACC-041)](#5-api-query-available-withdrawal-balance-dracc-041)
6. [Error Mapping](#6-error-mapping)
7. [Field Mapping Reference](#7-field-mapping-reference)
8. [Implementation Notes](#8-implementation-notes)
9. [Related Documents](#9-related-documents)
10. [Open Questions](#10-open-questions)

---

## 1. Overview

### 1.1 Purpose

Sub Account Withdrawal cho phép nhà đầu tư rút tiền mặt từ sub phái sinh **về tài khoản ngân hàng ngoài** (external bank account), bao gồm:

- **Tra cứu lịch sử rút tiền** (DRACC-039)
- **Thực hiện rút tiền** (DRACC-040)
- **Tra cứu số dư khả dụng rút** (DRACC-041)

Khác với VSD Transaction (nộp/rút margin NHSV ↔ VSD) và Internal Transfer (chuyển tiền giữa các sub cùng account), nhóm API này chuyển tiền **ra ngoài hệ thống NHSV/VSD**, về một tài khoản ngân hàng do khách hàng chỉ định.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Query Withdrawal History | GET | `/api/v1/derivatives/transfer/cash/withdraw/history` |
| Execute Withdrawal | POST | `/api/v1/derivatives/transfer/cash/withdraw` |
| Query Available Balance | GET | `/api/v1/derivatives/transfer/cash/withdraw/balance` |

**Lotte mapping:**

| Operation | TradeX Endpoint | Lotte Endpoint | Lotte Doc |
|-----------|------------------|-----------------|-----------|
| Query Withdrawal History | `GET /api/v1/derivatives/transfer/cash/withdraw/history` | `POST [RootURL]/.../der/account/cw_dr_acnt_history` | DRACC-039 |
| Execute Withdrawal | `POST /api/v1/derivatives/transfer/cash/withdraw` | `POST [RootURL]/.../der/account/dr-withdrawal` | DRACC-040 |
| Query Available Balance | `GET /api/v1/derivatives/transfer/cash/withdraw/balance` | `GET [RootURL]/.../der/account/dr-avai-withdrawl` | DRACC-041 |

> ⚠️ **Path đã tồn tại (partial stub):** `rest-proxy-main` hiện có sẵn swagger-only stub `GET/POST /api/v1/derivatives/transfer/cash/withdraw` (vcsc + kis) — chưa từng có `lotte-bridge` implementation (spec-only, "vaporware"). Đề xuất **tái sử dụng path này cho DRACC-040/041**, viết lại toàn bộ request/response schema theo bảng field thật ở §4–§5 (schema cũ của stub **không dùng được**). Xem Open Question #5.

### 1.3 Response Format Standards

Theo `tradex-api-conventions.md` — Lotte-integrated pattern:

**Success (Execute Withdrawal — mutation):**
```json
{
  "success": true
}
```

**Success (Query History):**
```json
{
  "items": [...],
  "nextData": "..."
}
```

**Success (Query Balance — single object, không phải array):**
```json
{
  "availableBalance": 74628604,
  "withdrawableAmount": 41033834
}
```

**Error:**
```json
{
  "code": "DERIVATIVES_CASH_WITHDRAW_1005",
  "message": "[V3120] Lỗi từ Lotte"
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success`/`code: "0000"` field cho query response
- **Exception:** Execute Withdrawal (mutation) trả `{ "success": true }` (boolean only) — theo tiền lệ VSD Withdraw (DRACC-009) / VSD Deposit (DRACC-034). Lotte **không** trả object nghiệp vụ nào cho DRACC-040 (`data_list: []` khi thành công), khác pattern order-v2's tradex-native `{id}` — domain này gọi thẳng Lotte, không lưu DB TradeX trước.
- Query: trả `items[]` + `nextData` (History) hoặc object đơn (Balance — luôn 1 dòng/account+sub).
- Pass-through Lotte message AS-IS (cho lỗi).

---

## 2. Business Rules

### 2.1 Status Enum (DRACC-039 History filter)

**Nguyên tắc "No Core names":** filter `status` dùng giá trị TradeX có nghĩa, KHÔNG expose mã ký tự Lotte `proc_tp`.

| TradeX `status` | Lotte `proc_tp` | Ý nghĩa |
|---|---|---|
| `PENDING` | `i` | Chưa duyệt |
| `REJECTED` | `j` | Từ chối |
| `APPROVED` | `k` | Duyệt |

### 2.2 Validation Rules

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | `accountNumber`, `subNumber` bắt buộc ở cả 3 API | `FIELD_IS_REQUIRED` |
| Positive Amount | `amount` (DRACC-040) phải > 0 | `INVALID_VALUE` |
| Account Ownership | `accountNumber` phải thuộc user đã login (JWT) | `INVALID_VALUE` / `UNAUTHORIZED_ACCOUNT` |
| Status Enum | `status` (DRACC-039) chỉ nhận `PENDING`/`REJECTED`/`APPROVED` | `INVALID_VALUE` |

**Note:**
- TradeX validate **light** (required fields, data type, format) — theo Validation Strategy philosophy của `tradex-api-conventions.md`. Số dư khả dụng, hạn mức rút... do Core/Lotte validate đầy đủ.
- TradeX **KHÔNG** validate `amount ≤ withdrawableAmount` — FE nên gọi DRACC-041 trước khi cho user nhập `amount`, hiển thị `withdrawableAmount` để tham khảo, nhưng validation cuối cùng vẫn ở Core.
- Không có bước "Calculate Fee" trung gian (khác VSD Deposit 2-step DRACC-033→034) — DRACC-040 không có fee input field, xem Open Question #6.

### 2.3 Language Mapping

| Accept-Language | Lotte `lang_code` | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V0307] Giao dịch thành công"` |
| `en` | `E` | `"[E0307] Transaction successful"` |
| `ko` | `K` | `"[K0307] 거래 성공"` |

### 2.4 Recommended Flow

```
1. GET .../withdraw/balance (DRACC-041) → hiển thị withdrawableAmount cho user
2. User nhập amount, bankAccount, note
3. POST .../withdraw (DRACC-040) → thực hiện rút tiền
4. GET .../withdraw/history (DRACC-039) → user tra cứu lại lịch sử/trạng thái
```

Không giống VSD Deposit (2-step: Calculate Fee → Deposit), DRACC-040 chỉ có 1 bước — không có API tính phí riêng trong bộ 3 API này.

---

## 3. API: Query Withdrawal History (DRACC-039)

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/cash/withdraw/history`

**Lotte Endpoint:** `[RootURL]/.../der/account/cw_dr_acnt_history` (DRACC-039)

**Lotte Doc:** DRACC-039

**Headers:**
- `Authorization: Bearer {JWT}`
- `Accept-Language: vi` (optional, default: vi)

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|--------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `subNumber` | String | ✅ | Số tiểu khoản |
| `status` | String enum: `PENDING`\|`REJECTED`\|`APPROVED` | ✅ | Trạng thái duyệt — xem §2.1 |
| `fromDate` | String (yyyyMMdd) | ✅ | Từ ngày |
| `toDate` | String (yyyyMMdd) | ✅ | Đến ngày |
| `nextKey` | String | ❌ | Pagination token; trang đầu để trống |
| `fetchCount` | Number | ❌ | Số bản ghi mỗi trang — theo convention GET API chung (§3 `tradex-api-conventions.md`); chỉ gửi sang Lotte khi client truyền |

> ⚠️ `fromDate`/`toDate`/`status` được đánh dấu **Required (Y)** theo bảng field gốc trong doc Lotte (khác VSD History — DRACC-021 — nơi `fromDate`/`toDate` optional với default = today). Nếu PM/HuongLT xác nhận muốn UX nhất quán với VSD (optional + default today), sửa lại `Required` cột này trước khi finalize — flag trong Open Question #4 phần liên quan enum, cần double-check cùng lúc.

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|--------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `subNumber` | String | ✅ | `sub_no` | Direct | Số tiểu khoản |
| `status` | String enum | ✅ | `proc_tp` | Map theo §2.1 (`PENDING`→`i`, `REJECTED`→`j`, `APPROVED`→`k`) | Trạng thái duyệt |
| `fromDate` | String | ✅ | `from_dt` | Direct (yyyyMMdd) | Từ ngày |
| `toDate` | String | ✅ | `to_dt` | Direct (yyyyMMdd) | Đến ngày |
| `nextKey` | String | ❌ | `next_key` | Trang đầu để trống | Pagination |
| `fetchCount` | Number | ❌ | `row_count` | Chỉ gửi khi có giá trị | Số bản ghi/trang |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User từ token |

### 3.3 Response Mapping

**Success (200):**

| TradeX Field | Type | Description |
|--------------|------|--------------|
| `items` | Array | Danh sách lịch sử rút tiền (xem Transaction Object) |
| `nextData` | String \| null | Pagination key (Lotte `next`) |

**Transaction Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|--------------|
| `trdDt` | `transactionDate` | String | Chuẩn hoá yyyyMMdd | Ngày giao dịch |
| `trdTp` | `transactionType` | String | Direct — **cần bảng enum từ HuongLT/VanND** (xem Open Question #4) | Loại giao dịch |
| `bankAcc` | `bankAccount` | String | Direct (giữ "code.name") — xem Open Question #3 | Tài khoản ngân hàng nhận |
| `rmrkCd` | `transactionCode` | String | Direct | Mã diễn giải |
| `rmrkNm` | `transactionName` | String | Direct | Tên diễn giải |
| `cnte` | `content` | String | Direct | Nội dung |
| `trdAmt` | `amount` | Number | Parse String → Number | Số tiền |
| `trdSeqNo` | `transactionSequenceNumber` | String | Direct | Số thứ tự GD |
| `cnfmDt` | `approvalDate` | String | Chuẩn hoá yyyyMMdd | Ngày duyệt |
| `seqNo` | `sequenceNumber` | String | Direct | Số thứ tự |
| `cnclYn` | `status` | String enum | Map sang `PENDING`/`REJECTED`/`APPROVED` — **enum giá trị gốc `cnclYn` chưa đủ rõ, cần xác nhận (Open Question #4)** | Trạng thái |
| `bookTime` | `receivedTime` | String | Direct | Thời gian ghi nhận |
| `bankAcntNo` | `receivingBankAccount` | String | Direct | Số TK NH nhận |
| `feeAmt` | `feeAmount` | Number | Parse String → Number | Phí GD |
| `next` | *(root)* `nextData` | String \| null | Pagination | Next key |

**Example Response:**
```json
{
  "items": [
    {
      "transactionDate": "20260703",
      "transactionType": "...",
      "bankAccount": "0003.Ngân hàng ABC",
      "transactionCode": "...",
      "transactionName": "...",
      "content": "...",
      "amount": 5000000,
      "transactionSequenceNumber": "...",
      "approvalDate": "20260704",
      "sequenceNumber": "...",
      "status": "APPROVED",
      "receivedTime": "...",
      "receivingBankAccount": "...",
      "feeAmount": 5500
    }
  ],
  "nextData": "..."
}
```

**Note:** `transactionType` (`trdTp`) và `status` (`cnclYn`) **chưa có bảng enum đầy đủ** từ doc Lotte — pass-through "Direct"/"best-effort mapping" tạm thời, phải hoàn thiện trước khi implement (Open Question #4).

---

## 4. API: Execute Withdrawal (DRACC-040)

### 4.1 Request

**Endpoint:** `POST /api/v1/derivatives/transfer/cash/withdraw`

**Lotte Endpoint:** `[RootURL]/.../der/account/dr-withdrawal` (DRACC-040)

**Lotte Doc:** DRACC-040

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|--------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `subNumber` | String | ✅ | Số tiểu khoản |
| `amount` | Number | ✅ | Số tiền rút |
| `note` | String | ✅ | Diễn giải |
| `bankAccount` | String | ✅ | Số tài khoản ngân hàng nhận — **nguồn dữ liệu chưa xác định, xem Open Question #2** |

> ⚠️ **KHÔNG dùng** schema stub cũ trong `rest-proxy-main` (`bankAccountNumber`/`beneficiaryBankAccountNumber`/`beneficiaryBankAccountName`/`beneficiaryBankBranch` — 4 field). DRACC-040 Lotte chỉ nhận **1 field duy nhất** `bank_account`.

### 4.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|--------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `subNumber` | String | ✅ | `sub_no` | Direct | Số tiểu khoản |
| `amount` | Number | ✅ | `amount` | Convert Number → String | Số tiền rút |
| `note` | String | ✅ | `remark` | Direct | Diễn giải |
| `bankAccount` | String | ✅ | `bank_account` | Direct | Số TK NH nhận |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User từ token |

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|--------------|
| `error_code` | - | Check = `"0000"` | Success indicator |
| `success` | `success` | Direct boolean | `true` nếu cả `error_code="0000"` và `success=true` |

**Response Logic:**

```typescript
if (lotteResponse.error_code === "0000" && lotteResponse.success === true) {
  return { success: true };
} else {
  throw BusinessError (422);
}
```

**Example Response:**
```json
{
  "success": true
}
```

**Note:**
- Lotte **KHÔNG** trả field nghiệp vụ nào (`data_list: []` khi thành công) — TradeX response chỉ là boolean, giống pattern VSD Withdraw (DRACC-009) / VSD Deposit (DRACC-034).
- **KHÔNG dùng** rich schema `transactionDate`/`sequenceNumber`/`previousCashBalance`/`cashBalance`/`fee`/`receivedCash` của stub cũ trong `rest-proxy-main` — Lotte không cung cấp field nào trong số đó cho DRACC-040.

---

## 5. API: Query Available Withdrawal Balance (DRACC-041)

### 5.1 Request

**Endpoint:** `GET /api/v1/derivatives/transfer/cash/withdraw/balance`

**Lotte Endpoint:** `[RootURL]/.../der/account/dr-avai-withdrawl` (DRACC-041)

**Lotte Doc:** DRACC-041

**Headers:**
- `Authorization: Bearer {JWT}`
- `Accept-Language: vi` (optional, default: vi)

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|--------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `subNumber` | String | ✅ | Số tiểu khoản |

> ⚠️ **Discrepancy đã ghi nhận (Open Question #1):** Bảng field trong doc Lotte cho DRACC-041 liệt kê `amount`, `remark`, `bank_account` là Required (Y) — nhưng **Input Sample thực tế chỉ có** `acnt_no`, `sub_no`, `hts_user_id`. Request Structure ở trên **lấy Input Sample làm ground truth** (một API tra cứu số dư không hợp lý khi cần `amount`/`bank_account`). Cần VanND/HuongLT xác nhận chính thức trước khi finalize.

### 5.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|--------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `subNumber` | String | ✅ | `sub_no` | Direct | Số tiểu khoản |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | User từ token |

### 5.3 Response Mapping

**Success (200) — object đơn (không phải array):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|--------------|
| `security_code` | *(không trả ra FE)* | - | - | Mã CTCK — metadata nội bộ |
| `deposit` | `availableBalance` | Number | Parse String → Number | Số dư tiền |
| `dpo_block` | `blockAmount` | Number | Parse String → Number | Tiền phong toả |
| `waiting_amt_for_withdraw` | `waitingWithdrawAmount` | Number | Parse String → Number | Tiền chờ rút |
| `tot_out_psbamt` | `withdrawableAmount` | Number | Parse String → Number | **Số tiền khả dụng rút** — field quan trọng nhất, FE dùng để giới hạn input `amount` ở DRACC-040 |
| `collect_ver_lisr` | `fillingLossBlockAmount` | Number | Parse String → Number | Tiền phong toả chờ nộp lỗ |
| `dpo_coll_blf` | `marginBlockAmount` | Number | Parse String → Number | Tiền phong toả ký quỹ |
| `dpo_block_ast` | `maturityPaymentBlockAmount` | Number | Parse String → Number | Tiền phong toả chờ thanh toán đáo hạn |
| `dpo_fee_coll` | `managementFeeBlockAmount` | Number | Parse String → Number | Tiền phong toả phí QLTS ký quỹ |

**Example Response:**
```json
{
  "availableBalance": 74628604,
  "blockAmount": 0,
  "waitingWithdrawAmount": 0,
  "withdrawableAmount": 41033834,
  "marginBlockAmount": 0,
  "fillingLossBlockAmount": 0,
  "maturityPaymentBlockAmount": 0,
  "managementFeeBlockAmount": 0
}
```

**Note:** Field name TradeX ở trên giữ tương thích tối đa với schema `DerivativesCashWithdrawInfoResponse` đã có sẵn trong `rest-proxy-main` stub (6/7 field khớp gần 1-1 về ngữ nghĩa), chỉ thêm mới `managementFeeBlockAmount`. Team dev có thể quyết định giữ tên field stub cũ hoàn toàn nếu muốn zero-diff — creator phase đề xuất bộ tên trên làm baseline duy nhất.

---

## 6. Error Mapping

### 6.1 Validation Errors (400) — TradeX

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing (cả 3 API) |
| `subNumber` | `FIELD_IS_REQUIRED` | `["subNumber"]` | Missing (cả 3 API) |
| `amount` | `FIELD_IS_REQUIRED` | `["amount"]` | Missing (DRACC-040) |
| `amount` | `INVALID_VALUE` | `["amount", "value", ">0"]` | ≤ 0 (DRACC-040) |
| `bankAccount` | `FIELD_IS_REQUIRED` | `["bankAccount"]` | Missing (DRACC-040) |
| `status` | `INVALID_VALUE` | `["status", "value", "PENDING/REJECTED/APPROVED"]` | Invalid value (DRACC-039) |
| `fromDate` / `toDate` | `FIELD_IS_REQUIRED` | `["fromDate"]` / `["toDate"]` | Missing (DRACC-039) |

### 6.2 Business Errors (422) — Lotte Pass-Through

| Operation | Pattern | Example |
|-----------|---------|---------|
| Query History | `DERIVATIVES_CASH_WITHDRAW_HISTORY_{code}` | `DERIVATIVES_CASH_WITHDRAW_HISTORY_1005` |
| Execute Withdrawal | `DERIVATIVES_CASH_WITHDRAW_{code}` | `DERIVATIVES_CASH_WITHDRAW_1005` |
| Query Balance | `DERIVATIVES_CASH_WITHDRAW_BALANCE_{code}` | `DERIVATIVES_CASH_WITHDRAW_BALANCE_1005` |

**Common Lotte Error Codes (tham khảo pattern chung, cần xác nhận danh sách đầy đủ cho DRACC-039/040/041):**

| Lotte Code | Description (Vietnamese) | Description (English) |
|------------|--------------------------|------------------------|
| `0000` | Thành công | Success |
| `1005` | Lỗi nghiệp vụ / số dư không đủ | Business error / insufficient balance |
| `2016` | Không có dữ liệu | No data found (not an error, empty result) |

---

## 7. Field Mapping Reference

### 7.1 Common Patterns

| TradeX Field | Lotte Field Variations | Transform Rule |
|--------------|--------------------------|-----------------|
| `accountNumber` | `acnt_no` | Direct |
| `subNumber` | `sub_no` | Direct |
| `amount` | `amount`, `trdAmt` | Number → String (request), String → Number (response) |
| `note` | `remark` | Direct |
| `bankAccount` | `bank_account`, `bankAcc`, `bankAcntNo` | Direct |
| `feeAmount` | `feeAmt` | String → Number |

### 7.2 Type Transformations

**Request (TradeX → Lotte):**

| TradeX Type | Lotte Type | Example |
|-------------|------------|---------|
| Number | String | `5000000` → `"5000000"` |
| String | String | `"Rút tiền"` → `"Rút tiền"` |

**Response (Lotte → TradeX):**

| Lotte Type | TradeX Type | Transformation |
|------------|-------------|------------------|
| String (amount) | Number | `"41033834"` → `41033834` |
| String (date) | String | `"20260703"` → `"20260703"` (chuẩn hoá yyyyMMdd nếu cần) |
| Char code (`proc_tp`, `cnclYn`) | String enum | `i`/`j`/`k` → `PENDING`/`REJECTED`/`APPROVED` |

---

## 8. Implementation Notes

### 8.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing, auto-populate `sourceIp` |
| `lotte-bridge` | Lotte API integration, field mapping, business logic |
| **Kafka** | Service communication (topic `lotte-bridge`) |

### 8.2 Key Principles

1. **Single-step Execute:** Không giống VSD Deposit (2-step: Calculate Fee → Deposit), DRACC-040 chỉ 1 bước — không có API tính phí riêng trong bộ 3 API này (xem Open Question #6).
2. **No bank list lookup:** Không có API "Get Bank List" nào trong bộ DRACC-039/040/041 (khác VSD có DRACC-032 riêng) — `bankAccount` là 1 field string tự do, nguồn dữ liệu **chưa xác định** (Open Question #2).
3. **Boolean-only mutation response:** DRACC-040 response chỉ `{ success: true }`, không có transaction detail — vì Lotte không trả field nghiệp vụ nào.
4. **Reuse stub path (đề xuất):** Path `/api/v1/derivatives/transfer/cash/withdraw` đã tồn tại (swagger-only, chưa implement) trong `rest-proxy-main` — đề xuất tái sử dụng cho DRACC-040 (POST) và DRACC-041 (GET, path `/balance`), viết lại toàn bộ schema (xem Open Question #5).
5. **Validation Strategy:** TradeX validate required fields, type, format; Lotte/Core validate business rules (số dư, hạn mức). Không duplicate logic.
6. **Auto-Population:** `userId` → JWT token; `sourceIp` → Request IP; `lang_code` → `Accept-Language` header.

### 8.3 Implementation Checklist

**DRACC-039 (History):**
- [ ] Create DTO: `DerivativesCashWithdrawHistoryRequest` / `Response`
- [ ] Implement endpoint in `rest-proxy`
- [ ] Implement service method in `lotte-bridge`
- [ ] Hoàn thiện enum `transactionType` (`trdTp`) và `status` (`cnclYn`) sau khi có bảng giá trị đầy đủ từ HuongLT/VanND (Open Question #4)
- [ ] Add pagination support (`nextKey` / `nextData`)
- [ ] Write unit + integration tests

**DRACC-040 (Execute Withdrawal):**
- [ ] Create DTO: `DerivativesCashWithdrawExecuteRequest` / `Response` (thay thế hoàn toàn schema stub cũ)
- [ ] Xác nhận nguồn `bankAccount` trước khi implement (Open Question #2)
- [ ] Implement endpoint in `rest-proxy` (tái sử dụng path stub — Open Question #5)
- [ ] Implement service method in `lotte-bridge`
- [ ] Response chỉ `{ success: true }`, KHÔNG dùng rich schema stub cũ
- [ ] Write unit + integration tests

**DRACC-041 (Balance):**
- [ ] Xác nhận lại Request Structure với HuongLT/VanND (Open Question #1) — **blocking** trước khi implement
- [ ] Create DTO: `DerivativesCashWithdrawBalanceRequest`
- [ ] Create DTO: `DerivativesCashWithdrawBalanceResponse`
- [ ] Implement endpoint in `rest-proxy` (tái sử dụng path stub — Open Question #5)
- [ ] Implement service method in `lotte-bridge`
- [ ] Add `managementFeeBlockAmount` (field mới, không có trong stub cũ)
- [ ] Write unit + integration tests

---

## 9. Related Documents

| Document | Location | Description |
|----------|----------|--------------|
| Analyst Findings | `_workspace/01_analyst_findings.md` | Nguồn dữ liệu gốc cho spec này — field mapping đầy đủ, endpoint proposal, 6 open questions |
| VSD Transaction API Spec | `../VSD transaction/VSD_Transaction_API_Spec.md` | Closest style reference — cùng pattern Lotte-integrated cash-movement, cùng response shape (`error_code`/`success`), cùng bộ 3-API (history + execute + balance) |
| Internal Transfer API Spec | `../Internal transfer/Internal_Transfer_API_Spec.md` | Related cash transaction feature (chuyển giữa sub, không phải rút ra ngoài) |
| Cash Transaction README | `../README.md` | Module overview — cần update thêm sub-module này sau khi spec được duyệt |
| TradeX API Conventions | `@Knowledge/TradeX/API Standards/tradex-api-conventions.md` | API naming & response standards |
| Lotte Derivatives API Doc | `derivatives_api_doc.txt` §3.2, lines 4079–4786 | Nguồn Lotte gốc cho DRACC-039/040/041 |

---

## 10. Open Questions

> Chuyển nguyên trạng từ `_workspace/01_analyst_findings.md` §"Open Questions / Discrepancies" — **PM cần xác nhận với HuongLT/VanND trước khi finalize spec này** (chuyển từ 🔄 sang ✅).

1. **[Quan trọng] DRACC-041 request field discrepancy:** Bảng field trong doc Lotte liệt kê `amount`, `remark`, `bank_account` là Required (Y), nhưng Input Sample thực tế chỉ có `acnt_no`, `sub_no`, `hts_user_id`. Rất có thể là lỗi copy-paste từ DRACC-040. Đã lấy Input Sample làm ground truth ở §5 — cần VanND/HuongLT xác nhận chính thức. Nếu `amount`/`bank_account` thực sự cần cho API tra cứu số dư (bất thường về nghiệp vụ), phải sửa lại toàn bộ §5.1–5.2.

2. **Nguồn `bankAccount` cho DRACC-040:** Không có API "Get Bank List" nào trong bộ DRACC-039/040/041 (khác VSD có DRACC-032). Cần hỏi: (a) FE tự nhập tay số TK ngân hàng mỗi lần rút, hay (b) có sẵn danh sách bank đã đăng ký ở 1 API khác (derivatives account profile, hoặc share với equity `/equity/withdraw/banks`)? Ảnh hưởng trực tiếp UX và quyết định có cần thêm 1 GET banks endpoint mới không.

3. **`bankAcc` format ở response DRACC-039 History:** Ví dụ `"0003.Ngân hàng ABC"` — gộp mã + tên NH trong 1 string (giống style Lotte "code.name" ở VSD `bankTypeName`). Hỏi: TradeX nên giữ nguyên 1 string, hay tách thành `bankCode` + `bankName` 2 field riêng (dễ dùng cho FE hơn)?

4. **Enum giá trị `trdTp` (loại giao dịch) và `cnclYn` (trạng thái duyệt/hủy) ở DRACC-039:** Doc Lotte chưa liệt kê đầy đủ tập giá trị hợp lệ của 2 field này. Cần bảng mapping đầy đủ (giống bảng `statusBOS` trong VSD spec §8.3.1) để định nghĩa enum TradeX tương ứng — không thể tạm map "Direct" khi implement chính thức. **Liên quan:** nếu enum `status` filter ở §3.1 thực ra nên optional + default (giống VSD History `fromDate`/`toDate`), cần xác nhận cùng lúc.

5. **Reuse existing stub path hay tạo path mới hoàn toàn?** `rest-proxy-main` đã có sẵn `GET/POST /api/v1/derivatives/transfer/cash/withdraw` (vcsc + kis) nhưng schema không backed bởi Lotte thật. PM cần quyết định: (a) tái sử dụng path này, viết lại schema theo DRACC-039/040/041 thật (khuyến nghị — field GET-info stub khớp DRACC-041 tới 6/7, khó là trùng hợp), hoặc (b) tạo hẳn namespace mới (vd. `derivatives/cashWithdrawal/...`) để tránh nhầm với stub cũ.

6. **Fee ở DRACC-040:** Không thấy field `fee`/phí trong request/response thật của DRACC-040 (không giống VSD Deposit cần gọi Calculate Fee trước — DRACC-033). Cần hỏi: rút tiền sub phái sinh có tính phí không? Nếu có, phí lấy ở đâu (trừ ngầm phía Lotte, không hiện trong API)?

---

**Document Status:** 🔄 In Progress — pending PM confirmation of Open Questions #1–#6 with HuongLT/VanND
**For:** PM / BA / BE Developer
**Next Steps:** PM xác nhận 6 Open Questions với HuongLT/VanND → cập nhật spec → validator phase check convention → move to `Derivatives/Planning documentation/Cash transaction/External withdrawal/`
