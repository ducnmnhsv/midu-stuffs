# NHSV Pro — New Features

**Nguồn:** Đề xuất Phòng Phân tích & Phòng CLKD  
**Cập nhật:** 2026-06-15  
**Phạm vi:** NHSV Pro Mobile App

---

## Tổng quan

> 📋 **Status hiện hành: xem [Tracking/kanban.html](../Tracking/kanban.html)** (filter Area = NHSV-Pro) — nguồn duy nhất `Tracking/tasks.js` (rule C7). Bảng dưới chỉ mô tả scope/dependency, KHÔNG maintain status nữa.

| Feature Area | Work items | Docs có sẵn | Status |
|-------------|-----------|-------------|--------|
| [Event Calendar](#event_calendar) | 1 | Spec | Todo |
| [NHSV Channel](#nhsv_channel) | 5 | NH Research, Khuyến nghị | Todo |
| [Market Watch](#market_watch) | 5 | GTGD Chart (PRD + BE Issue) | B-02 Ready for dev |
| [Admin Tool](#admin_tool) | 1 | — | Blocked (cần A-04, A-05 done) |
| [Realtime Pipeline](#realtime_pipeline) | 1 | — | Blocker cho Event Calendar + Market Watch |

---

## Feature Areas

### Event_Calendar

**Tính năng:** Lịch sự kiện cổ tức (tiền mặt + cổ phiếu) trên Home Screen, sắp xếp theo ngày GDKHQ.

| Layer | Nguồn dữ liệu | Upload | Phụ thuộc |
|-------|--------------|--------|-----------|
| BE + Mobile FE | Nguồn công khai | Auto realtime | Realtime_Pipeline |

**Docs:** [Spec.html](Event_Calendar/Spec.html)

---

### NHSV_Channel

**Tính năng:** Tái cấu trúc "Kênh tin tức NHSV" — thêm 2 tab mới, reorder, thêm search.

#### Sub-features

| ID | Feature | Layer | Phụ thuộc | Docs | Status |
|----|---------|-------|-----------|------|--------|
| A-01 | [Layout Restructure](NHSV_Channel/Layout_Restructure/) | Mobile FE | — | — | Todo |
| A-03 | [Tab Reorder](NHSV_Channel/Tab_Reorder/) | Mobile FE | NH_Research, Khuyen_Nghi | — | Todo |
| A-04 | [NH Research](NHSV_Channel/NH_Research/) | BE + Mobile FE + Admin | Admin_Tool | [Spec.html](NHSV_Channel/NH_Research/Spec.html) | Specced |
| A-05 | [Khuyến nghị](NHSV_Channel/Khuyen_Nghi/) | BE + Mobile FE + Admin | Admin_Tool, Realtime_Pipeline | [Feature Spec](NHSV_Channel/Khuyen_Nghi/Specifications/Feature_Specification.md) | Specced |
| A-06 | [Search theo mã CK](NHSV_Channel/Search/) | Mobile FE | NH_Research, Khuyen_Nghi | — | Todo |

---

### Market_Watch

**Tính năng:** Tab Market Watch trên màn hình Thị trường — 4 sub-tab dữ liệu realtime.

| ID | Sub-tab | Dạng biểu đồ | Phụ thuộc | Docs | Status |
|----|---------|-------------|-----------|------|--------|
| B-01 | [Container](Market_Watch/) | — | Realtime_Pipeline | — | Todo |
| B-02 | [GTGD Chart](Market_Watch/GTGD_Chart/) | Area chart 2 phiên | Realtime_Pipeline | [PRD](Market_Watch/GTGD_Chart/PRD.md) · [BE Issue](Market_Watch/GTGD_Chart/BE_Issue.md) | Ready for dev |
| B-03 | [Market Leaders](Market_Watch/Market_Leaders/) | Horizontal bar | Realtime_Pipeline | — | Todo |
| B-04 | [Foreign Trading](Market_Watch/Foreign_Trading/) | Bar chart mua/bán ròng | Realtime_Pipeline | — | Todo |
| B-05 | [Sector Performance](Market_Watch/Sector_Performance/) | Bar/Treemap theo ngành | Realtime_Pipeline | — | Todo |

---

### Admin_Tool

**Tính năng:** Tool nội bộ cho Phòng Phân tích + Phòng QTRR upload nội dung lên app.

| Scope | Layer | Phụ thuộc | Docs | Status |
|-------|-------|-----------|------|--------|
| Upload NH Research, Khuyến nghị, NHSV Rating, Kỹ thuật hằng ngày | BE + Admin Web | NH_Research, Khuyen_Nghi done | — | Blocked |

---

### Realtime_Pipeline

**Tính năng:** Backend data pipeline cấp dữ liệu realtime cho Market Watch + Event Calendar.

| Scope | Layer | Blocker cho | Docs | Status |
|-------|-------|-------------|------|--------|
| Market Watch (B-02→B-05), Event Calendar (A-02) | BE / Infra | Event_Calendar, Market_Watch | — | **Cần làm rõ Q1** |

---

## Open Questions

| # | Feature | Câu hỏi | Owner |
|---|---------|---------|-------|
| Q1 | Realtime_Pipeline | Nhà cung cấp dữ liệu realtime là ai? (FiinGroup / Vietstock / HOSE-HNX direct?) — ảnh hưởng estimate + cost | IT |
| Q2 | Khuyen_Nghi (A-05.2) | Phòng QTRR gửi NHSV Rating theo format nào? (Email Excel / Google Sheet?) — ảnh hưởng thiết kế Admin_Tool | Phòng QTRR |
| Q3 | Khuyen_Nghi (A-05.3) | Vietstock gửi data Kỹ thuật hằng ngày qua kênh nào? Có API tự động được không? | Phòng PT |

---

## Dependency Map

```
Realtime_Pipeline ──┬──→ Event_Calendar
                    ├──→ Market_Watch/GTGD_Chart
                    ├──→ Market_Watch/Market_Leaders
                    ├──→ Market_Watch/Foreign_Trading
                    ├──→ Market_Watch/Sector_Performance
                    └──→ NHSV_Channel/Khuyen_Nghi (giá realtime)

Admin_Tool ─────────┬──→ NHSV_Channel/NH_Research
                    └──→ NHSV_Channel/Khuyen_Nghi

NHSV_Channel ───────┬──→ NHSV_Channel/Tab_Reorder
(NH_Research        └──→ NHSV_Channel/Search
 + Khuyen_Nghi)
```

---

**Document Status:** In progress  
**For:** PM, Tech Lead  
**Next Steps:** Clarify Q1/Q2/Q3 → Estimate → Prioritize sprint
