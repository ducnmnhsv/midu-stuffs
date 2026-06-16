---
name: project-nhsv-new-features
description: "Trạng thái và scope decisions cho \"New feature in NHSV Pro\" — sprint hiện tại vs backlog"
metadata: 
  node_type: memory
  type: project
  originSessionId: 6a875e6f-3dd0-49c1-8a43-0bea0b7403e7
---

## Context

Workspace: `New feature in NHSV Pro/` trong tradex-monitoring repo.  
Nguồn: Đề xuất Phòng Phân tích & Phòng CLKD cho NHSV Pro mobile app.  
Primary user: Mix Long-term investor + Day trader.

**Why:** Đây là sprint tính năng mới từ yêu cầu nội bộ, cần PM track tiến độ và dev implement.

---

## Folder Structure (cập nhật 2026-06-16)

Push Notification tách riêng theo domain để làm độc lập. Admin_Tool nằm trong NHSV_Channel.

```
New feature in NHSV Pro/
├── README.md
├── List of issues.html          ← PM reference, 14 work items
├── Planning/
│   └── Feature_Improvements_2026Q2.md
├── _Demo/
│   └── index.html               ← demo prototype, NHSV DS aligned
├── Event_Calendar/
│   ├── Spec.html
│   └── Push_Notification/
│       └── Push_Notification_Spec.md   ← GDKHQ reminder only
├── NHSV_Channel/
│   ├── NH_Research/
│   │   ├── Spec.html
│   │   ├── admin-demo.html      ← detailed NH Research admin demo
│   │   └── jira-issues.md
│   ├── Khuyen_Nghi/
│   │   └── Spec.html
│   ├── Admin_Tool/              ← unified admin: NH Research + Khuyen Nghi + Push Notif
│   │   └── admin-demo.html
│   └── Push_Notification/
│       └── Push_Notification_Spec.md   ← NH Research + Khuyen Nghi publish notifications
├── Market_Watch/
│   ├── GTGD_Chart/ (PRD.md + BE_Issue.md)
│   ├── Market_Leaders/
│   ├── Foreign_Trading/
│   └── Sector_Performance/
└── Realtime_Pipeline/
```

**Quyết định folder structure:**
- Admin_Tool trong NHSV_Channel — chỉ serve NHSV Channel features
- Push Notification tách Event_Calendar vs NHSV_Channel để làm song song độc lập
- Layout_Restructure, Search, Tab_Reorder đã xóa (đã integrate vào NHSV_Channel)

---

## Sprint Scope

Từ competitive review (2026-06-15), tất cả đã được update vào `List of issues.html`:

- **A-02** Event Calendar: push notification T-3 ngày trước GDKHQ
- **A-04/A-05** NHSV Channel: P&L since KN date · Sort/filter by rating S/A/B/C/D · Status indicator (Còn hiệu lực / Đạt target / Đã cắt lỗ) · Push notification khi publish bài mới
- **X-03** (was cross-cutting, now split): notification service BE + FE deeplink handler

---

## Demo Prototype — `_Demo/index.html`

Đã align với NHSV Pro Design System:
- Font: Lato · Primary: `#028D96` · Header gradient teal
- Tất cả emoji → Lucide inline SVG
- 3 improvements trong mockup: P&L row, sort/filter pills, status badges

---

## Backlog (xem lại Q3)

- GTGD Chart: đa phiên T-1/T-5/T-20; surge indicator
- Market Leaders: tách Contributors + Gainers/Losers
- Foreign Trading: rolling 5-phiên trend
- Sector Performance: Treemap + drill down
- UX: "NEW" badge, global search, portfolio overlay, share card

---

## Việc còn dang dở

Không còn tồn đọng. Tất cả tasks hoàn thành:
1. `List of issues.html` — 14 work items, X-03 added
2. `Planning/Feature_Improvements_2026Q2.md`
3. `_Demo/index.html` — NHSV DS aligned, 3 improvements in mockups
4. `NHSV_Channel/Admin_Tool/admin-demo.html` — unified admin demo
5. Folder structure restructured (2026-06-16)
