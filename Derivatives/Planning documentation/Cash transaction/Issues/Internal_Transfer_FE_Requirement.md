# Internal Transfer Screen – FE Requirement

**Issue Type:** Feature Request (Frontend)
**Priority:** High
**Component:** NHSV Pro – Cash Transaction Tab (Derivatives)
**Related Module:** Cash Transaction – Internal Transfer
**Created:** February 27, 2026
**Status:** 📋 Ready for FE Dev Review

---

## 📋 Executive Summary

### Objective

Xây dựng màn hình **Internal Transfer** trong tab Cash Transaction của Derivatives trên NHSV Pro. Màn hình cho phép user chuyển tiền nội bộ giữa các sub-accounts trong cùng tài khoản phái sinh.

### Trigger

Màn hình xuất hiện khi user chọn **sub 80** (Derivatives Margin sub-account).

### Design Reference (Figma)

| Screen | Figma Link |
|--------|------------|
| **Request Form** | [13_05 Internal Transfer – Request](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244395&t=Hkbonf9r1expHBzf-11) |
| **Confirmation Dialog** | [13_06 Internal Transfer – Dialog](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244358&t=Hkbonf9r1expHBzf-11) |

### APIs

| API | Method | Endpoint | Mục đích |
|-----|--------|----------|---------|
| Available Balance | GET | `/api/v1/derivatives/account/available-balance` *(new – BE cần implement)* | Lấy Available cash (field `availableBalance`) |
| Internal Transfer | POST | `/api/v1/derivatives/transfer/cash` | Thực hiện chuyển tiền |

> **Note:** FE chỉ gọi TradeX API. BE chịu trách nhiệm wrap Lotte endpoint `/tuxsvc/der/account/dr-available-balance` và expose ra TradeX endpoint `/api/v1/derivatives/account/available-balance`. FE không biết và không gọi Lotte trực tiếp.

---

## 🖼️ Screen Overview

### Screen Structure

```
Cash Transaction Screen (sub 80)
│
├── Primary Tabs: [Withdraw] [Internal transfer ✓] [Cash in advance]
│
└── Secondary Tabs:
    ├── Request (active)
    │   ├── Available amount:  100,000,000       ← from API
    │   ├── Receiving account: [039C101991-01 ▼] ← dropdown
    │   ├── Amount:            [Enter amount ] [All]
    │   ├── Note:              [Transfer to... ]
    │   ├── Notes: Trading time: 8.30AM - 4.00 PM ...
    │   └── [Transfer] button
    └── History
        └── (transfer history list – separate issue)
```

### Confirmation Dialog

```
╔═══════════════════════════════╗
║ Internal transfer          [X]║
╠═══════════════════════════════╣
║ Sending account   039C101991-80║
║ Receiving account 039C101991-01║
║ Amount            10,000,000  ║
║ Note              Transfer to …║
╠═══════════════════════════════╣
║ [    Cancel    ] [ Confirm → ]║
╚═══════════════════════════════╝
```

---

## 📝 Detailed Requirements

### REQ-1: Screen Access – Sub 80

- Màn hình Internal Transfer chỉ visible khi user đang ở **sub 80** (Derivatives Margin).
- **Header**: hiển thị account + sub theo format `{accountNumber} - 80`.

### REQ-2: Tab Structure

**Primary Tabs** (3 tabs ngang, dạng pill):

| Tab | Label | Sub |
|-----|-------|-----|
| Tab 1 | Withdraw | sub 80 |
| Tab 2 | Internal transfer | sub 80 |
| Tab 3 | Cash in advance | sub 80 |

- Active tab: background `#2F4A4B`, text `white`.
- Inactive tab: background `#EEF1F4`, text `#808A9D`.

**Secondary Tabs** (dưới primary):

| Tab | Label |
|-----|-------|
| Tab 1 | Request |
| Tab 2 | History |

- Active tab: text `#028D96`, underline `#028D96`, thickness `3px`.
- Inactive tab: text `#2F4A4B`, no underline.

> **History tab**: Nằm ngoài scope của issue này. FE có thể placeholder / empty state trước.

### REQ-3: Available Amount

- **Label**: "Available amount"
- **Value**: Lấy từ response của `GET /api/v1/derivatives/account/available-balance`, field `availableBalance`.
- **Format**: Số nguyên, format có dấu phẩy ngăn cách hàng nghìn. Ví dụ: `100,000,000`.
- **Khi nào gọi**: Khi vào màn hình (focus) và sau khi transfer thành công.
- **Display**: Row dạng `label | value` (label trái, value phải).

### REQ-4: Receiving Account (Dropdown)

- **Label**: "Receiving account"
- **Type**: Dropdown selector.
- **Source**: Danh sách sub-accounts available để nhận tiền (khác sub hiện tại).
- **Display format**: `{accountNumber}-{subNumber}` (ví dụ: `039C101991-01`).
- **Default**: Sub-account đầu tiên trong danh sách.
- **Mapping to API**: Giá trị chọn → `receivedAccountNumber` + `receivedSubNumber` trong body POST.

> ❓ **Open question**: API nào để lấy danh sách receiving accounts? Cần confirm với BE. Có thể dùng danh sách sub-accounts từ account info đã có.

### REQ-5: Amount Field

- **Label**: "Amount"
- **Type**: Text input, keyboard số.
- **Placeholder**: "Enter amount"
- **"All" button** (bên phải input): Khi nhấn → set `amount = Available cash` (từ REQ-3).
- **Validation**:
  - Nếu amount > Available cash → hiển thị error inline: `"Vượt quá số tiền cho phép"`.
  - Nếu field trống khi nhấn "Transfer" → hiển thị error inline: `"Field is required"`.
- **Format hiển thị**: Hiển thị số có dấu phẩy khi user nhập (number formatting).
- **Mapping to API**: `amount` (Number) trong body POST.

### REQ-6: Note Field

- **Label**: "Note"
- **Type**: Text input.
- **Placeholder / Auto-fill**: Tự động điền `"Transfer to {receivingAccount}"` khi user chọn receiving account. User có thể chỉnh sửa.
- **Background**: `#F2F6FB` (phân biệt với Amount field).
- **Required**: Theo API spec, `note` là required. Nếu trống → error `"Field is required"`.
- **Mapping to API**: `note` trong body POST.

### REQ-7: Notes Section (Static Text)

Hiển thị text tĩnh bên dưới form:

```
Notes:
Trading time: 8.30AM - 4.00 PM
From Monday to Friday (except public holidays)
```

- Font size: 12px (`Small/Regular`), color: `#808A9D`.

### REQ-8: Transfer Button

- **Label**: "Transfer"
- **Style**: Full-width, background `#028D96`, text white, border-radius `8px`, height `40px`.
- **Enabled**: Luôn enabled (validation xảy ra khi nhấn).
- **Action**: Khi nhấn → validate form → nếu hợp lệ, hiện **Confirmation Dialog** (REQ-9).

### REQ-9: Confirmation Dialog

Hiển thị dạng modal overlay khi nhấn "Transfer" và form hợp lệ.

**Header**: Title "Internal transfer" + nút đóng [X].

**Content** (4 rows dạng `label | value`):

| Label | Value |
|-------|-------|
| Sending account | `{currentAccountNumber}-{currentSubNumber}` (ví dụ: `039C101991-80`) |
| Receiving account | `{receivedAccountNumber}-{receivedSubNumber}` |
| Amount | `{amount}` (formatted, ví dụ: `10,000,000`) |
| Note | `{note}` |

**Footer buttons**:

| Button | Style | Action |
|--------|-------|--------|
| Cancel | Secondary (`#EEF1F5`, text `#2F4A4B`) | Đóng dialog, quay lại form |
| Confirm | Primary (`#028D96`, text white) | Gọi API POST, xử lý response |

### REQ-10: API Call – POST /api/v1/derivatives/transfer/cash

Khi nhấn "Confirm" trong dialog:

**Request body:**

```json
{
  "accountNumber": "039C101991",
  "subNumber": "80",
  "receivedAccountNumber": "039C101991",
  "receivedSubNumber": "01",
  "amount": 10000000,
  "note": "Transfer to 039C101991-01"
}
```

**Success (HTTP 200)**:
- Đóng dialog.
- Hiển thị toast/snackbar success.
- Làm mới Available amount (gọi lại API REQ-3).
- Reset form (Amount, Note về default).

**Error (HTTP 4xx/5xx)**:
- Đóng dialog.
- Hiển thị error message từ response (`message` field).
- Không reset form (để user retry).

**Loading state**:
- Button "Confirm" hiển thị loading spinner khi đang call API.
- Disable "Confirm" và "Cancel" trong lúc loading để tránh double submit.

---

## 🗂️ Codebase Reference (nhsv-mts-rn)

| Item | Path |
|------|------|
| Repo | `/Users/ducnguyen/Documents/project/nhsv-mts-rn` |
| StockTransfer screen (reference) | `src/screens/StockTransferScreen/` |
| Screen registration | `src/navigation/ScreenNames.ts` |
| Redux sagas (reference) | `src/reduxs/sagas/StockTransfer/` |

**Màn hình mới cần tạo:**

```
src/screens/InternalTransferScreen/
├── index.tsx                          ← Main screen component
├── InternalTransferScreen.styles.ts
├── InternalTransferScreen.type.ts
└── components/
    ├── RequestForm/                   ← Request tab content
    │   ├── index.tsx
    │   ├── AvailableAmountRow.tsx
    │   ├── ReceivingAccountPicker.tsx
    │   ├── AmountInput.tsx
    │   └── NoteInput.tsx
    └── ConfirmDialog/                 ← Confirmation modal
        └── index.tsx
```

> **Note**: Chưa có màn hình này trong codebase hiện tại. FE cần tạo mới hoàn toàn.

---

## 🔌 API Summary

### API 1 – Available Balance (New – BE implement)

| Field | Value |
|-------|-------|
| Method | GET |
| TradeX Endpoint | `/api/v1/derivatives/account/available-balance` |
| Auth | JWT Bearer |
| Params | `accountNumber`, `subNumber` |

**Expected response:**

```json
{
  "availableBalance": 100000000
}
```

### API 2 – Internal Cash Transfer

| Field | Value |
|-------|-------|
| Method | POST |
| Endpoint | `/api/v1/derivatives/transfer/cash` |
| Auth | JWT Bearer |

**Request body:**

```json
{
  "accountNumber": "string",
  "subNumber": "string",
  "receivedAccountNumber": "string",
  "receivedSubNumber": "string",
  "amount": 10000000,
  "note": "string"
}
```

**Success response:**

```json
{
  "transactionDate": "20260227",
  "outSequenceNumber": "001",
  "outPreviousCashBalance": 100000000,
  "outCashBalance": 90000000,
  "inSequenceNumber": "002",
  "inPreviousCashBalance": 5000000,
  "inCashBalance": 15000000
}
```

**Error response:**

```json
{
  "code": "DERIVATIVES_CASH_TRANSFER_1005",
  "message": "[V1234] Số dư không đủ để thực hiện giao dịch"
}
```

---

## ✅ Acceptance Criteria

- [ ] **AC-1**: Màn hình hiển thị khi user chọn sub 80, với header `{accountNumber} - 80`.
- [ ] **AC-2**: Primary tab "Internal transfer" được highlight active.
- [ ] **AC-3**: Secondary tab "Request" active by default, "History" placeholder.
- [ ] **AC-4**: Available amount hiển thị đúng từ API (`total_deposit`), format có dấu phẩy.
- [ ] **AC-5**: Dropdown "Receiving account" hiển thị danh sách sub-accounts (khác sub 80).
- [ ] **AC-6**: Button "All" set amount = Available cash.
- [ ] **AC-7**: Error "Vượt quá số tiền cho phép" khi amount > Available cash.
- [ ] **AC-8**: Error "Field is required" khi Amount hoặc Note trống và nhấn Transfer.
- [ ] **AC-9**: Note field tự động điền `"Transfer to {receivingAccount}"` khi chọn receiving account.
- [ ] **AC-10**: Dialog hiển thị đúng thông tin (Sending, Receiving, Amount, Note).
- [ ] **AC-11**: Nhấn Cancel → đóng dialog, giữ nguyên form.
- [ ] **AC-12**: Nhấn Confirm → gọi API, loading state, không double submit.
- [ ] **AC-13**: Transfer thành công → toast success, refresh Available amount, reset Amount/Note.
- [ ] **AC-14**: Transfer thất bại → hiển thị error message, không reset form.

---

## ❓ Open Questions

| # | Question | Impact |
|---|----------|--------|
| 1 | API nào để lấy danh sách receiving accounts cho dropdown? | REQ-4 blocked nếu chưa có API |
| 2 | BE đã implement `/api/v1/derivatives/account/available-balance` chưa? | REQ-3 blocked |
| 3 | "History" tab cần implement trong cùng issue này không? | Scope |
| 4 | Có giới hạn số tiền tối thiểu/tối đa không? | Validation REQ-5 |
| 5 | Note field có bắt buộc không? (API spec bắt buộc, nhưng Figma không rõ) | REQ-6 |

---

## 📚 Related Documents

| Document | Location |
|----------|----------|
| Internal Transfer API Spec | [Internal_Transfer_API_Spec.md](../Internal%20transfer/Internal_Transfer_API_Spec.md) |
| Cash Transaction README | [README.md](../README.md) |
| Figma – Request Screen | [node-id=40005073-244395](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244395) |
| Figma – Confirmation Dialog | [node-id=40005073-244358](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244358) |

---

**Document Status:** 📋 Ready for Dev Team Review
**For:** FE Developer, BE Developer (API 1 new)
**Next Steps:** Confirm open questions → FE implements screen + BE implements available-balance API
