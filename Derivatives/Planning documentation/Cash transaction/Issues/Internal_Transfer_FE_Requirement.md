# Internal Transfer Screen – FE Requirement

**Issue Type:** Feature Request (Frontend)
**Priority:** High
**Component:** NHSV Pro – Cash Transaction Tab (Derivatives)
**Related Module:** Cash Transaction – Internal Transfer
**Created:** February 27, 2026  
**Updated:** (BE đã implement APIs theo spec)  
**Status:** ✅ Ready for FE Implementation

---

## 📋 Executive Summary

### Objective

Xây dựng màn hình **Internal Transfer** trong tab Cash Transaction của Derivatives trên NHSV Pro. Màn hình cho phép user chuyển tiền nội bộ giữa các sub-accounts trong cùng tài khoản phái sinh.

### Trigger

Màn hình xuất hiện khi user chọn **sub 80** (Derivatives Margin sub-account).

### Design Reference (Figma)

- **Request Form:** [13_05 Internal Transfer – Request](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244395&t=Hkbonf9r1expHBzf-11)
- **Confirmation Dialog:** [13_06 Internal Transfer – Dialog](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244358&t=Hkbonf9r1expHBzf-11)
- **History:** [13_05 Internal Transfer – History](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244323&t=Hkbonf9r1expHBzf-11)

### APIs (BE đã implement theo [Internal_Transfer_API_Spec](../Internal%20transfer/Internal_Transfer_API_Spec.md))

- **Available Balance** — GET `/api/v1/derivatives/account/availableBalance`: lấy Available cash (field `availableBalance`).
- **Internal Transfer** — POST `/api/v1/derivatives/transfer/cash`: thực hiện chuyển tiền.
- **Transfer History** — GET `/api/v1/derivatives/transfer/cash/history`: lịch sử chuyển tiền (cho tab History).

> **Note:** FE chỉ gọi TradeX API. Chi tiết request/response, error format xem [Internal_Transfer_API_Spec.md](../Internal%20transfer/Internal_Transfer_API_Spec.md).

---

## 🎯 Requirements theo Use Case User

Các requirement được nhóm theo luồng sử dụng của user: từ mở màn hình → nhập form → xác nhận → gọi API.

---

### Use Case 1: Mở màn hình Internal Transfer (Screen Access & Layout)

**Khi nào:** User chọn sub 80 (Derivatives Margin).

**User thấy / hệ thống làm:**

Màn Internal Transfer chỉ hiển thị khi user đang ở **sub 80**. Header hiển thị account + sub theo format `{accountNumber} - 80`.

Primary tabs gồm 3 tab ngang dạng pill: **Withdraw**, **Internal transfer** (active), **Cash in advance**. Tab active: nền `#2F4A4B`, chữ trắng; tab inactive: nền `#EEF1F4`, chữ `#808A9D`.

Secondary tabs bên dưới: **Request** (mặc định active) và **History**. Tab active: chữ `#028D96`, gạch chân 3px; tab inactive: chữ `#2F4A4B`.

**Cấu trúc màn hình:**

```
Cash Transaction Screen (sub 80)
│
├── Primary Tabs: [Withdraw] [Internal transfer ✓] [Cash in advance]
│
└── Secondary Tabs:
    ├── Request (active)
    │   └── (form – Use Case 2, 3)
    └── History
        └── (list + filter – Use Case 7)
```

---

### Use Case 2: Hiển thị thông tin Available amount

**Khi nào:** User vào màn hình (focus) và sau khi transfer thành công.

**User thấy / hệ thống làm:**

Hệ thống gọi GET `/api/v1/derivatives/account/availableBalance` và lấy field `availableBalance`. Hiển thị với label "Available amount", value là số từ API, layout một dòng (label trái, value phải). Số được format nguyên, có dấu phẩy ngăn cách hàng nghìn (ví dụ: `100,000,000`).

Refresh Available amount khi vào màn hình và sau khi POST transfer thành công (Use Case 6).

---

### Use Case 3: Hiển thị và tương tác Form Request

**Khi nào:** User đang ở tab Request.

**User thấy / hệ thống làm:**

**Receiving account (dropdown).** Label "Receiving account". Nguồn dữ liệu là danh sách sub-accounts có thể nhận tiền (khác sub hiện tại). Hiển thị theo format `{accountNumber}-{subNumber}` (ví dụ: `039C101991-01`). Mặc định chọn sub-account đầu tiên trong danh sách. Giá trị chọn map sang `receivedAccountNumber` và `receivedSubNumber` trong body POST.

> ❓ **Open:** API nào lấy danh sách receiving accounts? Có thể dùng danh sách sub-accounts từ account info có sẵn.

**Amount (input).** Label "Amount". Text input, bàn phím số, placeholder "Enter amount". Nút "All" bên cạnh: khi nhấn thì set amount bằng Available amount (từ Use Case 2). Số hiển thị có dấu phẩy khi user nhập. Validation: nếu amount > Available cash thì hiển thị error inline "Vượt quá số tiền cho phép"; nếu trống khi nhấn Transfer thì "Field is required". Giá trị map sang field `amount` (Number) trong body POST.

**Note (input).** Label "Note". Khi user chọn Receiving account thì tự động điền `"Transfer to {receivingAccount}"`, user có thể sửa. Background `#F2F6FB`. Theo API, `note` là required; nếu trống thì hiển thị "Field is required". Giá trị map sang field `note` trong body POST.

**Notes (static text).** Dưới form hiển thị đoạn tĩnh: "Notes: Trading time: 8.30AM - 4.00 PM. From Monday to Friday (except public holidays)." Font 12px, màu `#808A9D`.

**Nút Transfer.** Label "Transfer". Full-width, nền `#028D96`, chữ trắng, border-radius 8px, height 40px. Luôn enabled; validation chạy khi nhấn. Khi nhấn: validate form; nếu hợp lệ thì hiển thị Confirmation Dialog (Use Case 4).

---

### Use Case 4: Hiển thị Confirmation Dialog

**Khi nào:** User nhấn "Transfer" và form hợp lệ (Amount, Note có giá trị, Amount ≤ Available).

**User thấy / hệ thống làm:**

Dialog hiển thị dạng modal overlay. Header có title "Internal transfer" và nút đóng [X]. Nội dung gồm 4 dòng dạng label | value: Sending account (`{currentAccountNumber}-{currentSubNumber}`, ví dụ `039C101991-80`), Receiving account (`{receivedAccountNumber}-{receivedSubNumber}`), Amount (`{amount}` có dấu phẩy, ví dụ `10,000,000`), Note (`{note}`). Footer có hai nút: **Cancel** (secondary) và **Confirm** (primary).

**Wireframe dialog:**

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

Cancel: nền `#EEF1F5`, chữ `#2F4A4B`. Confirm: nền `#028D96`, chữ trắng.

---

### Use Case 5: Hủy xác nhận (Cancel trong Dialog)

**Khi nào:** User nhấn "Cancel" trong Confirmation Dialog.

**Hệ thống làm:** Đóng dialog và quay lại form, giữ nguyên toàn bộ dữ liệu đã nhập (Amount, Note, Receiving account).

---

### Use Case 6: Xác nhận chuyển tiền – Gọi API Transfer

**Khi nào:** User nhấn "Confirm" trong Confirmation Dialog.

**Hệ thống làm:**

**Gửi request.** Method POST `/api/v1/derivatives/transfer/cash`. Body ví dụ:

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

**Loading state.** Nút "Confirm" hiển thị loading spinner. Disable cả "Confirm" và "Cancel" khi đang gọi API để tránh double submit.

**Xử lý Success (HTTP 200).** Đóng dialog; hiển thị toast/snackbar success; làm mới Available amount (gọi lại GET availableBalance – Use Case 2); reset form (Amount và Note về default, Note có thể về "Transfer to {receivingAccount}" nếu vẫn giữ Receiving account).

**Xử lý Error (HTTP 4xx/5xx).** Đóng dialog; hiển thị message lỗi từ response (field `message`); không reset form để user chỉnh và thử lại.

---

### Use Case 7: Hiển thị Internal Transfer History

**Khi nào:** User chọn tab **History** trên màn Internal Transfer.

**Hệ thống làm:**

Gọi GET `/api/v1/derivatives/transfer/cash/history` với query: `accountNumber` (bắt buộc), `fromDate` (bắt buộc, yyyyMMdd), `toDate` (bắt buộc, yyyyMMdd), `nextData` (tùy chọn, dùng cho trang tiếp theo). FE cần có bộ lọc/date range để user chọn fromDate và toDate; lần đầu vào tab có thể mặc định khoảng ngày hợp lệ (ví dụ 7 ngày gần nhất hoặc theo quy định BE).

Response trả về `items` (mảng các bản ghi) và `nextData` (chuỗi pagination; rỗng khi hết dữ liệu). Mỗi item trong `items` có các field: `transactionDate`, `sendAccountNumber`, `sendSubNumber`, `sendAccountName`, `sequenceNumber`, `amount`, `receivedAccountNumber`, `receivedSubNumber`, `receivedAccountName`, `note`, `tradingChannel`, `isCanceled`. Hiển thị danh sách theo design Figma; map đúng field API sang từng cột/card. Số tiền format có dấu phẩy ngăn cách hàng nghìn. Nếu `isCanceled === true` thì thể hiện trạng thái đã hủy theo design.

Pagination: khi còn dữ liệu (`nextData` khác rỗng), gọi lại API với cùng `accountNumber`, `fromDate`, `toDate` và `nextData` bằng giá trị nhận được từ response trước; append thêm item vào list (infinite scroll hoặc nút "Xem thêm" tùy Figma).

Empty state: khi `items` rỗng (và không có lỗi), hiển thị empty state theo Figma. Error: khi API trả 4xx/5xx, hiển thị message từ response `message`; không clear list đã load trước đó (nếu có).

Sau khi user thực hiện transfer thành công (Use Case 6), có thể refresh lại History (gọi lại API với cùng fromDate/toDate, reset `nextData`) để list cập nhật; hoặc chỉ refresh khi user vào lại tab History, tùy product.

---

## 🗂️ Codebase Reference (nhsv-mts-rn)

- **Repo:** `/Users/ducnguyen/Documents/project/nhsv-mts-rn`
- **StockTransfer screen (reference):** `src/screens/StockTransferScreen/`
- **Screen registration:** `src/navigation/ScreenNames.ts`
- **Redux sagas (reference):** `src/reduxs/sagas/StockTransfer/`

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
    ├── ConfirmDialog/                  ← Confirmation modal
    │   └── index.tsx
    └── HistoryTab/                    ← History tab content
        └── index.tsx                   (list + date filter + pagination)
```

> **Note:** Chưa có màn hình này trong codebase. FE cần tạo mới hoàn toàn.

---

## 🔌 API Summary

### API 1 – Available Balance (✅ BE đã implement)

- **Method:** GET  
- **Endpoint:** `/api/v1/derivatives/account/availableBalance`  
- **Auth:** JWT Bearer  
- **Params:** `accountNumber` (required), `inquiryDate` (optional, yyyyMMdd)  
- **Spec:** [Internal_Transfer_API_Spec.md §3](../Internal%20transfer/Internal_Transfer_API_Spec.md)

**Expected response:**

```json
{
  "availableBalance": 100000000
}
```

### API 2 – Internal Cash Transfer (✅ BE đã implement)

- **Method:** POST  
- **Endpoint:** `/api/v1/derivatives/transfer/cash`  
- **Auth:** JWT Bearer  
- **Spec:** [Internal_Transfer_API_Spec.md §4](../Internal%20transfer/Internal_Transfer_API_Spec.md)

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

### API 3 – Transfer History (✅ BE đã implement)

- **Method:** GET  
- **Endpoint:** `/api/v1/derivatives/transfer/cash/history`  
- **Auth:** JWT Bearer  
- **Query params:** `accountNumber` (required), `fromDate` (required, yyyyMMdd), `toDate` (required, yyyyMMdd), `nextData` (optional, pagination key)  
- **Spec:** [Internal_Transfer_API_Spec.md §5](../Internal%20transfer/Internal_Transfer_API_Spec.md)

**Response:** `items` (array of transfer objects), `nextData` (string). Mỗi item: `transactionDate`, `sendAccountNumber`, `sendSubNumber`, `sendAccountName`, `sequenceNumber`, `amount`, `receivedAccountNumber`, `receivedSubNumber`, `receivedAccountName`, `note`, `tradingChannel`, `isCanceled`.

---

## ✅ Acceptance Criteria (map theo Use Case)

- [ ] **UC1** Màn hình hiển thị khi user chọn sub 80; header `{accountNumber} - 80`; primary tab "Internal transfer" active; secondary tabs Request và History.
- [ ] **UC2** Available amount lấy từ API, format có dấu phẩy; refresh khi vào màn và sau transfer thành công.
- [ ] **UC3** Dropdown Receiving account hiển thị đúng format; Amount có "All", validation "Vượt quá số tiền cho phép" / "Field is required"; Note auto-fill "Transfer to {receivingAccount}"; Notes static text đúng; nút Transfer mở dialog khi form hợp lệ.
- [ ] **UC4** Dialog hiển thị đúng Sending, Receiving, Amount, Note; có Cancel và Confirm.
- [ ] **UC5** Cancel đóng dialog, giữ nguyên form.
- [ ] **UC6** Confirm gọi POST transfer; loading, không double submit; success → toast, refresh Available amount, reset form; error → hiển thị message, không reset form.
- [ ] **UC7** Tab History: gọi GET history với accountNumber, fromDate, toDate; hiển thị list map đúng field API; pagination qua nextData; empty state khi items rỗng; xử lý lỗi hiển thị message; isCanceled thể hiện trạng thái đã hủy.

---

## ❓ Open Questions

1. **API nào để lấy danh sách receiving accounts cho dropdown?** Ảnh hưởng Use Case 3 – cần confirm với BE hoặc dùng data có sẵn.
2. **Có giới hạn số tiền tối thiểu/tối đa không?** Liên quan validation Amount (Use Case 3).
3. **Note field có bắt buộc không?** API spec bắt buộc – liên quan Use Case 3.
4. **History: mặc định fromDate/toDate khi vào tab?** Ví dụ 7 ngày gần nhất hay theo quy định BE.

---

## 📚 Related Documents

- **Internal Transfer API Spec:** [Internal_Transfer_API_Spec.md](../Internal%20transfer/Internal_Transfer_API_Spec.md)
- **Cash Transaction README:** [README.md](../README.md)
- **Figma – Request Screen:** [node-id=40005073-244395](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244395)
- **Figma – Confirmation Dialog:** [node-id=40005073-244358](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244358)
- **Figma – History:** [node-id=40005073-244323](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005073-244323&t=Hkbonf9r1expHBzf-11)

---

**Document Status:** ✅ Ready for FE Implementation  
**For:** FE Developer  
**Next Steps:** FE implement màn Internal Transfer (Request + History) theo Figma & use case trên; gọi APIs theo [Internal_Transfer_API_Spec](../Internal%20transfer/Internal_Transfer_API_Spec.md). Confirm open questions (receiving accounts, default date range cho History) khi cần.
