# FE Issue: Admin eKYC — Dashboard Analytics

**Service:** ekyc-admin (Admin UI)
**Priority:** Medium
**Type:** New Feature
**Blocked by:** BE Dashboard API (`GET /api/admin/ekyc/dashboard`) phải live trước — xem `../Specifications/03_Dashboard_API_Spec.md`

---

## Executive Summary

### Problem Statement

BA/PM và team Security không có số liệu tổng hợp về eKYC — tỉ lệ fail theo nguyên nhân, xu hướng theo ngày, fraud detection metrics. Mọi phân tích đang phải query thủ công từ DB.

### Current vs Target

| Hiện tại | Sau khi hoàn thành |
|---------|-------------------|
| Không có Dashboard eKYC | Dashboard với 7 KPI cards + 4 charts |
| Phân tích phải query DB thủ công | BA/PM xem số liệu realtime trong < 3s |
| Không biết nguyên nhân fail phổ biến | Bar chart breakdown theo failure step |
| Không phát hiện pattern fraud | Fraud detection panel riêng |

### Success Criteria

- [ ] BA/PM xem được tỉ lệ fail theo nguyên nhân mà không cần hỏi Dev
- [ ] Dashboard load < 3s
- [ ] Fraud detection panel giúp Security nhận biết case đáng ngờ
- [ ] Filter theo date range hoạt động đúng cho tất cả KPI và chart

---

## Technical Background

**Endpoint:** `GET /api/admin/ekyc/dashboard?fromDate={date}&toDate={date}`
**Auth:** Admin role required
**Response time SLA:** < 3s (BE có cache)

Full request/response spec: xem `../Specifications/03_Dashboard_API_Spec.md`.

---

## Detailed Requirements

### Route & Filter bar

**Route:** `/admin/ekyc/dashboard`

**Filter bar:**
```
Khoảng thời gian: [7 ngày qua ▾]   [Áp dụng]
```
- Options: 7 ngày qua / 30 ngày qua / Tùy chọn (date range picker)
- Mặc định: 7 ngày qua

---

### Hàng 1 — KPI Cards (4 cards)

| Card | Metric | Đơn vị | API field |
|------|--------|--------|-----------|
| Tổng eKYC | Unique customers submit | lượt | `kpi.totalSubmissions` |
| Tỉ lệ thành công | % APPROVED | % | `kpi.approvalRate` |
| Thất bại / Pending | Số case fail + pending | case | `kpi.failedAndPendingCount` |
| Số lần thử TB | TB số lần thử / TK thành công | lần | `kpi.avgAttemptsPerSuccess` |

**Card style:** số lớn + label nhỏ bên dưới + màu icon theo loại.

### Hàng 2 — KPI Cards (3 cards)

| Card | Metric | Đơn vị | API field | Màu icon |
|------|--------|--------|-----------|---------|
| Phát hiện gian lận | Unique customers có fraud flag | case | `kpi.fraudDetectedCount` | 🔴 đỏ |
| Thời gian xử lý TB | Submit → APPROVED | phút | `kpi.avgProcessingMinutes` | 🔵 xanh |
| Tỉ lệ ký HĐ | Ký HĐ sau APPROVED | % | `kpi.contractSignRate` | 🟢 xanh lá |

---

### Chart 1 — Tỉ lệ kết quả eKYC (Donut)

- Dữ liệu: `outcomeBreakdown`
- Màu: APPROVED = xanh lá, REJECTED = đỏ, PENDING = vàng
- Center label: tổng số submissions
- Legend: bên phải với count + %

### Chart 2 — Nguyên nhân thất bại (Horizontal Bar)

- Dữ liệu: `failureStepBreakdown`
- X-axis: số lượng case; Y-axis: label tiếng Việt
- Sort: descending theo count; Color: gradient đỏ

### Chart 3 — Xu hướng theo ngày (Line Chart)

- Dữ liệu: `dailyTrend`
- 2 lines: Tổng (xanh nhạt) và Thành công (xanh đậm)
- X-axis: ngày (DD/MM)
- Tooltip khi hover: `total`, `approved`, và % success

### Chart 4 — Fraud Detection (Bar + Summary)

- Dữ liệu: `fraudBreakdown`
- Layout: summary counter bên trái + bar chart bên phải
- Summary: "X case có fraud flags trong kỳ"
- Bar chart: `fraudBreakdown.byType` — horizontal bar, màu cam/đỏ
- Click vào bar → filter sang trang Danh sách eKYC với fraud type (nice-to-have)

---

### Bảng — Khách hàng nhiều lần thử

**Title:** "Top khách hàng nhiều lần thử nhất"

| Cột | API field | Ghi chú |
|-----|-----------|--------|
| Họ tên | `fullName` | — |
| Số CCCD | `identifierId` | — |
| Số lần thử | `attemptCount` | Badge số, màu đỏ nếu ≥ 3 |
| Kết quả | `accountStatus` | Pill: APPROVED / PENDING / REJECTED |
| Nguyên nhân fail chính | `mainFailureStep` | Label tiếng Việt |
| Hành trình | — | Nút [Xem →] → link sang `/admin/ekyc/attempts/:identifierId` |

**Empty state:** "Không có case nào thử nhiều hơn 1 lần trong kỳ này"

---

## Edge Cases

- Kỳ chưa có data: hiển thị tất cả KPI = 0, charts rỗng với message "Chưa có dữ liệu trong khoảng thời gian này"
- `fraudDetectedCount = 0`: Chart 4 hiển thị "Không phát hiện case gian lận trong kỳ"
- API timeout (> 3s): skeleton loader → error state với nút "Thử lại"
- `avgProcessingMinutes = null` (chưa có approved case): hiển thị "—"

---

**Document Status:** 📋 Pending | For: FE Dev (ekyc-admin UI) | Next Steps: BE implement `03_Dashboard_API_Spec.md` → FE implement 7 KPI cards + 4 charts
