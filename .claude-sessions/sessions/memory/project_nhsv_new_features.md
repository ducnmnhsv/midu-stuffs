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

## Folder Structure (đã thống nhất)

Tổ chức theo **feature area** (không theo screen name — khó maintain):

```
New feature in NHSV Pro/
├── README.md
├── List of issues.html          ← PM reference, 14 work items (cập nhật 2026-06-16)
├── Planning/
│   └── Feature_Improvements_2026Q2.md  ← prose-only, C3 compliant
├── Push_Notification/
│   └── Specifications/
│       └── X03_Push_Notification_Infrastructure.md
├── _Demo/
│   └── index.html               ← demo prototype, NHSV DS aligned
├── Event_Calendar/
├── NHSV_Channel/
│   ├── NH_Research/
│   ├── Khuyen_Nghi/
│   ├── Layout_Restructure/
│   ├── Tab_Reorder/
│   └── Search/
├── Market_Watch/
│   ├── GTGD_Chart/
│   ├── Market_Leaders/
│   ├── Foreign_Trading/
│   └── Sector_Performance/
├── Admin_Tool/
└── Realtime_Pipeline/
```

---

## Sprint Scope — Items được confirm thêm vào (cập nhật 2026-06-16)

Từ competitive review (2026-06-15), tất cả đã được update vào `List of issues.html`:

### Event_Calendar (A-02)
- Push notification T-3 ngày trước ngày GDKHQ

### NHSV_Channel / NH_Research (A-04) + Khuyen_Nghi (A-05)
- P&L since recommendation date trên Danh mục cơ bản
- Sort/filter by rating level (S/A/B/C/D) trên NHSV Rating
- Status indicator (Còn hiệu lực / Đạt target / Đã cắt lỗ) trên Kỹ thuật hằng ngày
- Push notification khi có bài mới (A-04 + A-05)

### X-03 Push Notification Infrastructure
Cross-cutting, phục vụ cả Event_Calendar lẫn NH_Research + Khuyen_Nghi:
- BE: notification service — trigger: TRIGGER_GDKHQ_REMINDER, TRIGGER_NH_RESEARCH_PUBLISH, TRIGGER_KHUYEN_NGHI_PUBLISH
- FE: deeplink handler (`nhsvpro://event-calendar`, `nhsvpro://channel/nh-research`, `nhsvpro://channel/khuyen-nghi`)
- Admin Tool (X-01): nút "Publish & Notify" khi upload bài

---

## Demo Prototype — `_Demo/index.html`

Đã align với NHSV Pro Design System (session 2026-06-16):
- Font: Lato (thay Inter)
- Primary: `#028D96` teal (thay navy)
- Header gradient: `linear-gradient(90deg,#00A9B4 0%,#01B483 100%)`
- Market colors: green `#07A461` (up), red `#DA1004` (down)
- Tất cả emoji icon → Lucide inline SVG
- 3 improvements đã có trong mockup: P&L row, sort/filter pills, status badges

---

## Backlog (chưa làm — xem lại sau)

Market Watch improvements (từ competitive review):
- GTGD Chart: so sánh đa phiên T-1/T-5/T-20; surge indicator
- Market Leaders: tách view Contributors + Gainers/Losers
- Foreign Trading: rolling 5-phiên trend; header summary
- Sector Performance: dùng Treemap thay bar; drill down by sector

User Flow & Cross-cutting UX:
- Discoverability: "NEW" badge trên tab mới
- Search: global search ở header NHSV Channel
- Portfolio overlay trên Market Watch
- Share khuyến nghị card

---

## Việc còn dang dở

Không còn việc tồn đọng từ sessions trước. Tất cả 3 tasks ban đầu đã complete:
1. ✅ `List of issues.html` — 14 work items, X-03 added
2. ✅ `Planning/Feature_Improvements_2026Q2.md` — prose-only planning doc
3. ✅ `Push_Notification/Specifications/X03_Push_Notification_Infrastructure.md`
4. ✅ `_Demo/index.html` — NHSV DS aligned, emoji removed, 3 improvements in mockups
