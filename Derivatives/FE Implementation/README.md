# Derivatives – FE Implementation

> **Mục đích:** Tạo và quản lý issue requirement cho Frontend (NHSV Pro App) – tính năng Derivatives.  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** 2026-02-10  
> **Status:** Active

---

## Quick Navigation

| Section | Description |
|---------|-------------|
| [Overview](#overview) | Mục đích và quy ước |
| [Folder Structure](#folder-structure) | Cấu trúc thư mục |
| [Documentation Map](#documentation-map) | Index theo module |
| [Data & Design References](#data--design-references) | Nguồn dữ liệu & Figma |

---

## Overview

Folder này dùng để **tạo và lưu issue requirement cho FE dev** khi triển khai Derivatives trên app NHSV Pro (React Native). Mỗi issue mô tả rõ màn hình/component cần sửa, nguồn dữ liệu (BE/Planning docs), và acceptance criteria.

**Quy ước:**

- **Chỉ đọc** repo FE (`nhsv-mts-rn`) khi viết issue; artifact (mô tả issue, AC) lưu tại đây hoặc Jira/Bitbucket.
- Mỗi **module** (Market, Order, …) có thư mục riêng, bên trong có **Issues/** và tùy chọn **References/** (Figma, API links).
- Naming: PascalCase với underscore, không dùng ngoặc vuông (ví dụ: `Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md`).

---

## Folder Structure

```
FE Implementation/
├── README.md                    ← Bạn đang ở đây
├── Market/                      ← Market data & symbol list
│   ├── Issues/                  ← Issue requirement cho FE
│   │   └── Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md
│   └── References/               ← (Optional) Figma, API links
├── Order/                       ← Order flow, TPSL (khi có)
│   └── Issues/
└── Archive/                     ← Issue đã hoàn thành / deprecated
```

---

## Documentation Map

### Market

| Issue | Mô tả ngắn | Screens | Status |
|-------|------------|---------|--------|
| [Derivatives_Symbol_List_PS_DR_Search_CurrentPrice](./Market/Issues/Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md) | Hiển thị danh sách mã Derivatives với index name PS/DR tại Search & Current price | Search, Current price | 📋 Ready |

### Order

*(Sẽ bổ sung khi có issue FE cho Order.)*

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
| Current price | [NHSV Pro – Current price (node 40004829-278373)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-278373) |

### FE Repo (read-only)

- **Path:** `nhsv-mts-rn` (workspace riêng).
- **Screens liên quan:** `src/screens/SearchScreen/`, `src/screens/SearchSymbolScreen/`, `src/screens/CurrentPriceScreen/`.

---

## How to Use

1. **Tạo issue mới:** Thêm file trong `{Module}/Issues/`, tên file PascalCase + underscore, nội dung theo template (Background, Data source, Screens/Components, AC, Figma/References).
2. **Cập nhật README:** Thêm dòng vào bảng [Documentation Map](#documentation-map) tương ứng module.
3. **Hoàn thành:** Chuyển issue sang `Archive/` (hoặc đánh dấu Completed trong file) và cập nhật status trong README.

---

**Prepared by:** BA/PM Team  
**Rule reference:** `.cursor/rules/fe-repo-derivatives-issues.mdc`
