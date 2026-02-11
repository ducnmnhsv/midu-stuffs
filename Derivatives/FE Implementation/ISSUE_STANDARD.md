# FE Implementation – Chuẩn Issue Agile / Jira

> **Mục đích:** Chuẩn hóa issue trong folder FE Implementation thành format Jira-ready (Epic → Story → Task) cho FE developer.  
> **Áp dụng:** Mọi issue trong `Market/Issues/`, `Order/Issues/`, v.v.

---

## 1. Cấu trúc Agile

| Cấp | Ý nghĩa | Jira type | Ví dụ |
|-----|---------|-----------|--------|
| **Epic** | Nhóm tính năng / module lớn | Epic | DR-FE-MKT: Derivatives Market FE |
| **Story** | Giá trị người dùng, có thể demo | Story | MKT.S1: Hiển thị index name PS/DR tại Search & Current price |
| **Task** | Công việc kỹ thuật, có thể giao dev | Task (sub-task) | T1: Thêm helper getSymbolIndexName; T2: Cập nhật ItemSymbol |

- Mỗi **file** trong `Issues/` tương ứng **một Jira Story** (có thể tạo sub-tasks từ mục Tasks).
- **Epic** dùng để nhóm Stories theo module (Market, Order, …); Epic ID đặt ở đầu mỗi issue.

---

## Nên tạo theo Feature hay Category?

Áp dụng mô hình **theo feature** để dễ keep track:

- **Category = Epic** (Market, Order, Asset, ...): nhóm tính năng theo domain.
- **Feature = Story** (ví dụ: Symbol_Index_Name, Home_Chart, Market_List, Price_Table): mỗi issue là **một tính năng** có thể demo, tên rõ ràng.
- **Task = sub-task** trong từng Story: công việc kỹ thuật (component, API, state).

**Quy tắc đặt tên file issue:**

- Tên file = tên **feature** (PascalCase + underscore): `Derivatives_Symbol_Index_Name`, `Derivatives_Home_Chart`, …
- **KHÔNG** dùng Figma node ID trong tên file (tránh `Derivatives_Figma_UI_Alignment_40005821`).
- Link Figma đặt trong mục **References** của từng issue; dùng Figma MCP để map node → feature khi tạo issue mới.

---

## 2. Quy ước ID

| Loại | Format | Ví dụ |
|------|--------|--------|
| Epic | `DR-FE-{MODULE}` | DR-FE-MKT, DR-FE-ORD |
| Story | `{MODULE}.S{seq}` | MKT.S1, MKT.S2, ORD.S1 |
| Task | `T{seq}` | T1, T2, T3 |
| Jira key | (điền khi tạo Jira) | NHMTS-xxx |

**MODULE:** MKT = Market, ORD = Order (mở rộng sau: AST = Asset, PORT = Portfolio, …).

---

## 3. Template cho mỗi Issue (Story)

Mỗi file trong `{Module}/Issues/` theo cấu trúc sau:

```markdown
# [Epic DR-FE-XXX] Story MXX.Sn: <Title>

> **Jira:** (key khi tạo, e.g. NHMTS-xxx)  
> **Epic:** DR-FE-XXX  
> **Module:** Market | Order | ...  
> **Priority:** P0 | P1  
> **Status:** 📋 Ready for FE | 🔴 Blocked | ✅ Done

---

## User Story

**As a** [người dùng – e.g. Trader, User]  
**I want to** [hành động / tính năng]  
**So that** [lợi ích / kết quả].

---

## Acceptance Criteria

- [ ] **AC-01** [Mô tả kiểm tra được]
- [ ] **AC-02** [...]

---

## Tasks (Implementation)

*(Optional – dùng làm sub-task trong Jira)*

- [ ] **T1** [Công việc kỹ thuật 1]
- [ ] **T2** [...]

---

## Background / Context

[Data source, API, Planning doc, Screens & Components – giữ nội dung chi tiết hiện có]

---

## Technical Notes

[API endpoint, FE path, helper gợi ý, dependencies]

---

## References

- Figma: [links]
- Planning: [links]
```

---

## 4. Definition of Done (gợi ý)

Story coi là **Done** khi:

- [ ] Tất cả AC được QA/PM xác nhận.
- [ ] Code review đã pass (nếu có quy định).
- [ ] Không làm regression cho Equity / flow hiện có (nếu liên quan).
- [ ] Figma/design đã match (nếu có).

---

## 5. Mapping file ↔ Jira

| File (repo) | Epic | Story ID | Jira Story (ví dụ) |
|-------------|------|----------|---------------------|
| Market/Issues/Derivatives_Symbol_List_PS_DR_Search_CurrentPrice.md | DR-FE-MKT | MKT.S1 | NHMTS-xxx |
| Market/Issues/Derivatives_Home_Chart_Market_List_PriceTable.md | DR-FE-MKT | MKT.S2 | NHMTS-xxx |
| Market/Issues/Derivatives_Current_Price_Screen.md | DR-FE-MKT | MKT.S3 | NHMTS-xxx |
| Order/Issues/Order_Availability_Check_Integration.md | DR-FE-ORD | ORD.S1 | NHMTS-xxx |
| Order/Issues/Derivatives_Order_Entry_Integration.md | DR-FE-ORD | ORD.S2 | NHMTS-xxx |
| Order/Issues/TP_SL_UI_Copy_Implementation.md | DR-FE-ORD | ORD.S3 | NHMTS-xxx |

Khi tạo Jira: tạo Epic trước (DR-FE-MKT, DR-FE-ORD), rồi tạo Story link to Epic; có thể tạo sub-tasks từ mục **Tasks** trong từng file.
