# [Epic DR-FE-MKT] Story MKT.S2: Home Index chart, Market list (Index/BOND), Derivatives bảng giá ngang & Error state

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-MKT – Derivatives Market FE  
> **Module:** Market  
> **Screens:** Home, Market, Derivatives Price Table (horizontal)  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-11

---

## User Story

**As a** trader / user  
**I want to** xem chart thị trường phái sinh Index trên Home, danh sách mã Index/BOND riêng và tab Derivatives trên Market, và bảng giá ngang Derivatives (mặc định Index futures, có dropdown và xử lý lỗi tải)  
**So that** tôi theo dõi và giao dịch phái sinh theo từng nhóm (Index, Gov Bond) và thấy trạng thái lỗi rõ ràng khi dữ liệu không tải được.

---

## Acceptance Criteria

### Home – Chart Index

- [ ] **AC-01** Chart Index derivatives được hiển thị trên Home, dùng dữ liệu từ mã có `m === "INDEX"` (41I1 = VN30, 41I2 = VN100).
- [ ] **AC-02** Nguồn dữ liệu: `symbol_static.json` / symbolInfo; lọc `m === "INDEX"` để lấy danh sách mã phục vụ chart.

### Market – List Index, BOND, tab Derivatives

- [ ] **AC-03** Có khu vực/list riêng cho **Index** (`m === "INDEX"`): chỉ hiển thị các mã có `m === "INDEX"`.
- [ ] **AC-04** Có khu vực/list riêng cho **Gov.Bond future** (`m === "BOND"`): chỉ hiển thị các mã có `m === "BOND"`.
- [ ] **AC-05** Tab **Derivatives** hiển thị tất cả mã phái sinh từ `symbol_static.json` (toàn bộ mã có trong file).

### Bảng giá ngang Derivatives

- [ ] **AC-06** Default selection của bảng giá ngang Derivatives là **Index futures** (dữ liệu tương ứng `m === "INDEX"`).
- [ ] **AC-07** Khi click vào control chọn loại (dropdown/selector), hiển thị danh sách option (Index futures, Gov Bond, v.v.) theo design Figma 40005162-208618.
- [ ] **AC-08** Khi xảy ra **error loading**, hiển thị màn/state error theo Figma 40005162-206999 (copy/retry theo design).

---

## Tasks (Implementation)

- [ ] **T1** Home: Thêm block chart Index derivatives; lọc symbol `m === "INDEX"` từ symbol_static/symbolInfo; tích hợp với component chart hiện có (VN30/VN100 theo 41I1/41I2).
- [ ] **T2** Market: Implement list riêng Index (filter `m === "INDEX"`) và list riêng Gov Bond (filter `m === "BOND"`); tab Derivatives load toàn bộ symbol phái sinh từ symbol_static.json.
- [ ] **T3** Bảng giá ngang: Default = Index futures; component dropdown/selector với danh sách option (Index futures, Gov Bond, …) theo Figma.
- [ ] **T4** Bảng giá ngang: Xử lý error loading (network/empty/parse); hiển thị UI error theo Figma 40005162-206999 (nút retry/copy theo design).

---

## Background / Context

Bổ sung và chỉnh sửa UI cho phần Derivatives trên ba màn: **Home** (chart Index), **Market** (list Index/BOND, tab Derivatives), **Bảng giá ngang** (default Index futures, dropdown, error loading). Dữ liệu từ `symbol_static.json` và API market; nhận diện Derivatives qua `m === "INDEX"` hoặc `m === "BOND"` (và `t === "FUTURES"`).

| Màn | Yêu cầu |
|-----|---------|
| Home | Chart thị trường phái sinh **Index** (`m === "INDEX"`, 41I1 = VN30, 41I2 = VN100). |
| Market | List **Index** riêng, **Gov.Bond** riêng; tab **Derivatives** = tất cả mã từ symbol_static.json. |
| Price table | Default = Index futures; click → dropdown option; error loading → UI theo Figma. |

---

## Data Source & Conventions

- **Nguồn:** Init job → `symbol_static.json`; API `/api/v2/market/symbol/latest`, `/api/v2/market/symbolInfo`.
- **Nhận diện Derivatives:** `t === "FUTURES"` hoặc `m in ["INDEX","BOND"]`.
- **Index:** `m === "INDEX"`; mã 41**I1**xxxx = VN30, 41**I2**xxxx = VN100.
- **Gov Bond:** `m === "BOND"`; mã 41**B**xxxxxx.
- **Planning:** [Market/Planning/01_Integration_Plan.md](../../../Planning%20documentation/Market/Planning/01_Integration_Plan.md) — 4.1.2.1, 5.4.

---

## Technical Notes

- Index name hiển thị (PS/DR, TPCP/GB): xem Story [MKT.S1 – Symbol list PS/DR](./Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md).
- Tab Derivatives: data = toàn bộ symbol phái sinh trong `symbol_static.json` (sau khi app đã load file).
- Error state: xử lý cả lỗi network và lỗi empty/parse; hiển thị thống nhất theo Figma error screen.

---

## References (Figma)

| Màn / State | Figma (NHSV Pro) |
|-------------|-------------------|
| Home | [node-id=40004829-276489](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-276489) |
| Market | [node-id=40004829-277238](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-277238) |
| Price table (default) | [node-id=40005162-207007](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-207007) |
| Price table – option list | [node-id=40005162-208618](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-208618) |
| Price table – Error loading | [node-id=40005162-206999](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-206999) |
