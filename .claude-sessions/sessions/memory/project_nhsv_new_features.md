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
├── List of issues.html          ← PM reference, giữ lại
├── Event_Calendar/
├── NHSV_Channel/
│   ├── NH_Research/
│   ├── Khuyen_Nghi/
│   ├── Layout_Restructure/
│   ├── Tab_Reorder/
│   └── Search/
├── Market_Watch/
│   ├── GTGD_Chart/              ← có PRD.md + BE_Issue.md
│   ├── Market_Leaders/
│   ├── Foreign_Trading/
│   └── Sector_Performance/
├── Admin_Tool/
└── Realtime_Pipeline/
```

---

## Sprint Scope — Items được confirm thêm vào

Từ competitive review (2026-06-15), các improvements sau được PM approve thêm vào sprint:

### Event_Calendar (A-02)
- **ADD:** Push notification T-3 ngày trước ngày GDKHQ

### NHSV_Channel / NH_Research (A-04) + Khuyen_Nghi (A-05)
- **ADD:** P&L since recommendation date trên Danh mục cơ bản
- **ADD:** Sort/filter by rating level trên NHSV Rating
- **ADD:** Status indicator (Còn hiệu lực / Đạt target / Đã cắt lỗ) trên Kỹ thuật hằng ngày
- **ADD:** Push notification khi có bài mới — FE cần implement deeplink để navigate vào đúng màn hình

### X-03 Push Notification Infrastructure ← NEW work item
Cross-cutting, phục vụ cả Event_Calendar lẫn NH_Research + Khuyen_Nghi:
- BE: notification service — trigger mới (T-3 GDKHQ + publish content)
- FE: deeplink handler (`nhsvpro://channel/nh-research`, `nhsvpro://channel/khuyen-nghi`, v.v.)
- Admin Tool (X-01): nút "Publish & Notify" khi upload bài

**How to apply:** Khi tiếp tục session, bước tiếp theo là cập nhật `List of issues.html` và tạo design doc cho X-03.

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

## Việc còn dang dở (cần làm khi quay lại)

1. Cập nhật `List of issues.html` — thêm improvements đã confirm + X-03 + backlog section
2. Tạo design doc tại `docs/superpowers/specs/2026-06-15-nhsv-feature-improvements.md`
3. Tạo spec file cho X-03 Push Notification Infrastructure
