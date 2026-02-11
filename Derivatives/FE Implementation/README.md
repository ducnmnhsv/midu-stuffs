# Derivatives – FE Implementation

> **Mục đích:** Tạo và quản lý issue requirement cho Frontend (NHSV Pro App) – tính năng Derivatives.  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** 2026-02-11  
> **Status:** Active

---

## Quick Navigation

| Section | Description |
|---------|-------------|
| [Overview](#overview) | Mục đích và quy ước |
| [Issue Standard (Agile / Jira)](#issue-standard-agile--jira) | Epic, Story, Task – chuẩn Jira |
| [Folder Structure](#folder-structure) | Cấu trúc thư mục |
| [Documentation Map](#documentation-map) | Index theo module |
| [Data & Design References](#data--design-references) | Nguồn dữ liệu & Figma |

---

## Overview

Folder này dùng để **tạo và lưu issue requirement cho FE dev** khi triển khai Derivatives trên app NHSV Pro (React Native). Mỗi issue là **một Jira Story** theo chuẩn Agile (Epic → Story → Task), có User Story, Acceptance Criteria và Tasks.

**Quy ước:**

- **Chỉ đọc** repo FE (`nhsv-mts-rn`) khi viết issue; artifact (mô tả issue, AC) lưu tại đây hoặc Jira/Bitbucket.
- Mỗi **module** (Market, Order, …) có thư mục riêng, bên trong có **Issues/** và tùy chọn **References/** (Figma, API links).
- Naming file: Theo **feature** – PascalCase + underscore (ví dụ: `Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md`). Không dùng Figma node ID trong tên file. Link Figma đặt trong References; dùng Figma MCP khi cần map node → feature.
- **Chuẩn issue:** Mỗi file = 1 Story; có Epic ID, Story ID (e.g. MKT.S1), User Story, AC, Tasks. Chi tiết: [ISSUE_STANDARD.md](./ISSUE_STANDARD.md).

---

## Issue Standard (Agile / Jira)

Tất cả issue trong `Issues/` tuân theo chuẩn **Epic → Story → Task** để đồng bộ với Jira:

| Cấp | Ý nghĩa | Ví dụ |
|-----|---------|--------|
| **Epic** | Nhóm tính năng / module | DR-FE-MKT, DR-FE-ORD |
| **Story** | Một Jira Story (User Story + AC) | MKT.S1, ORD.S2 |
| **Task** | Sub-task triển khai (optional) | T1, T2, T3 |

- **Template & mapping file ↔ Jira:** [ISSUE_STANDARD.md](./ISSUE_STANDARD.md)
- Khi tạo Jira: tạo Epic trước (DR-FE-MKT, DR-FE-ORD), rồi tạo Story link to Epic; có thể tạo sub-tasks từ mục **Tasks** trong từng file.

---

## Folder Structure

```
FE Implementation/
├── README.md                    ← Bạn đang ở đây
├── ISSUE_STANDARD.md            ← Chuẩn Epic/Story/Task, template Jira
├── Market/                      ← Epic DR-FE-MKT
│   ├── Issues/                  ← Stories MKT.S1, MKT.S2, MKT.S3
│   │   ├── Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md
│   │   ├── Derivatives_Home_Chart_Market_List_PriceTable.md
│   │   └── Derivatives_Current_Price_Screen.md
│   └── References/              ← Figma, API links
├── Order/                       ← Epic DR-FE-ORD
│   └── Issues/                  ← Stories ORD.S1, ORD.S2, ORD.S3
│       ├── Order_Availability_Check_Integration.md
│       ├── Derivatives_Order_Entry_Integration.md
│       └── TP_SL_UI_Copy_Implementation.md
└── Archive/                     ← Issue đã hoàn thành / deprecated
```

---

## Documentation Map

### Market (Epic DR-FE-MKT)

| Story | File | Mô tả ngắn | Screens | Status |
|-------|------|------------|---------|--------|
| **MKT.S1** | [Derivatives_Symbol_List_PS_DR_Search_CurrentPrice](./Market/Issues/Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md) | Index name PS/DR tại Search & Current price | Search, Current price | 📋 Ready |
| **MKT.S2** | [Derivatives_Home_Chart_Market_List_PriceTable](./Market/Issues/Derivatives_Home_Chart_Market_List_PriceTable.md) | Home chart Index, Market list Index/BOND, bảng giá ngang & error state | Home, Market, Price table | 📋 Ready |
| **MKT.S3** | [Derivatives_Current_Price_Screen](./Market/Issues/Derivatives_Current_Price_Screen.md) | Basic info, Bid/Ask 3&10 bước, Thống kê lệnh, Aggressive matched | Current price | 📋 Ready |

### Order (Epic DR-FE-ORD)

| Story | File | Mô tả ngắn | Screens | Status |
|-------|------|------------|---------|--------|
| **ORD.S1** | [Order_Availability_Check_Integration](./Order/Issues/Order_Availability_Check_Integration.md) | Check max quantity khi đặt lệnh Derivatives | Order entry | 📋 Ready |
| **ORD.S2** | [Derivatives_Order_Entry_Integration](./Order/Issues/Derivatives_Order_Entry_Integration.md) | Đặt lệnh / hủy / sửa / Unmatch Derivatives | Order entry, Unmatch list | 📋 Ready |
| **ORD.S3** | [TP_SL_UI_Copy_Implementation](./Order/Issues/TP_SL_UI_Copy_Implementation.md) | TP/SL UI copy & validation (EN/VI) | TP/SL setup | 🔴 Blocked |

---

## Data & Design References

### Nguồn dữ liệu (Backend / Planning)

- **Dữ liệu đầu ngày (init):** `symbol_static.json` – danh sách mã (equity + derivatives).  
  Chi tiết: [Market/Planning/01_Integration_Plan.md](../Planning%20documentation/Market/Planning/01_Integration_Plan.md).
- **Nhận diện Derivatives:** `t === "FUTURES"` và/hoặc `m === "derivatives"`.
- **SymbolInfo API:** `/api/v2/market/symbolInfo`, `/api/v2/market/symbol/latest` – format giống Planning (field `t`, `m`, `n1`, `n2`, …).

### Figma

| Màn hình | Link |
|----------|------|
| Current price | [NHSV Pro – Current price](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-278373) |

### FE Repo (read-only)

- **Path:** `nhsv-mts-rn` (workspace riêng).
- **Screens liên quan:** `src/screens/SearchScreen/`, `src/screens/SearchSymbolScreen/`, `src/screens/CurrentPriceScreen/`.

---

## How to Use

1. **Tạo issue mới:** Đọc [ISSUE_STANDARD.md](./ISSUE_STANDARD.md). Thêm file trong `{Module}/Issues/`, **tên file = tên feature** (PascalCase + underscore); nội dung theo template. Nếu bắt đầu từ Figma: dùng **Figma MCP** (`get_design_context`) để xem node → xác định feature → đặt tên file.
2. **Cập nhật README:** Thêm dòng vào bảng [Documentation Map](#documentation-map) với Epic + Story ID.
3. **Jira:** Tạo Epic (DR-FE-MKT / DR-FE-ORD) nếu chưa có; tạo Story link to Epic; điền Jira key vào ô **Jira:** trong file issue. Sub-tasks có thể tạo từ mục **Tasks**.
4. **Hoàn thành:** Chuyển issue sang `Archive/` (hoặc đánh dấu ✅ Done trong file) và cập nhật status trong README.

---

**Prepared by:** BA/PM Team  
**Rule reference:** `.cursor/rules/fe-repo-derivatives-issues.mdc`
