# FE Issue: Admin eKYC — Dashboard Analytics

**Service:** ekyc-admin (Admin UI)  
**Priority:** Medium  
**Type:** New Feature

---

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

BA/PM và team Security không có số liệu tổng hợp về eKYC: tỉ lệ fail theo nguyên nhân, xu hướng theo ngày, fraud detection metrics. Mọi phân tích đang phải query thủ công từ DB.

### Current vs Target

| Hiện tại | Sau khi hoàn thành |
|---------|-------------------|
| Không có Dashboard eKYC | Dashboard với 7 KPI cards + 4 charts |
| Phân tích phải query DB thủ công | BA/PM xem số liệu realtime trong < 3s |
| Không biết nguyên nhân fail phổ biến | Bar chart breakdown theo failure step |
| Không phát hiện pattern fraud | Fraud detection panel riêng |

### Solution Approach (HIGH-LEVEL)

Thêm 1 trang Dashboard vào Admin UI với:
- **7 KPI cards** — tổng hợp metrics 7 ngày
- **4 charts** — tỉ lệ kết quả, nguyên nhân fail, xu hướng theo ngày, fraud breakdown
- **Bảng top N** — khách hàng có số lần thử cao nhất

### Timeline

Backend API `/api/admin/ekyc/dashboard` cần sẵn sàng trước → FE implement sau.

### Success Criteria

- [ ] BA/PM xem được tỉ lệ fail theo nguyên nhân mà không cần hỏi Dev
- [ ] Dashboard load < 3s (metrics có thể được cache phía BE)
- [ ] Fraud detection panel giúp Security nhận biết case đáng ngờ
- [ ] Bảng top N cho thấy khách hàng cần hỗ trợ đặc biệt

---

## 🔍 Technical Background (PM CAN SKIP)

### Backend API

**Endpoint:** `GET /api/admin/ekyc/dashboard?fromDate={date}&toDate={date}`  
**Auth:** Admin role required  
**Response time SLA:** < 3s (BE nên cache hoặc pre-aggregate)

**Response structure:**

```json
{
  "period": { "from": "2025-05-15", "to": "2025-05-21" },

  "kpi": {
    "totalSubmissions": 312,
    "approvedCount": 248,
    "approvalRate": 0.795,
    "failedAndPendingCount": 64,
    "avgAttemptsPerSuccess": 1.4,
    "fraudDetectedCount": 7,
    "avgProcessingMinutes": 12.3,
    "contractSignRate": 0.91
  },

  "outcomeBreakdown": [
    { "outcome": "APPROVED",        "count": 248 },
    { "outcome": "REJECTED",        "count": 41  },
    { "outcome": "PENDING",         "count": 23  }
  ],

  "failureStepBreakdown": [
    { "step": "VNPT_OCR",        "label": "Đọc thông tin CCCD",           "count": 28 },
    { "step": "VNPT_LIVENESS",   "label": "Xác minh khuôn mặt trực tiếp", "count": 15 },
    { "step": "FACE_COMPARE",    "label": "So sánh khuôn mặt",            "count": 9  },
    { "step": "LOTTE_SUBMIT",    "label": "Gửi thông tin lên hệ thống",   "count": 5  },
    { "step": "MRZ_VALIDATION",  "label": "Kiểm tra MRZ",                 "count": 3  },
    { "step": "CONTRACT_SIGN",   "label": "Ký hợp đồng điện tử",          "count": 4  }
  ],

  "dailyTrend": [
    { "date": "2025-05-15", "total": 38, "approved": 30 },
    { "date": "2025-05-16", "total": 45, "approved": 36 },
    { "date": "2025-05-17", "total": 52, "approved": 41 },
    { "date": "2025-05-18", "total": 41, "approved": 33 },
    { "date": "2025-05-19", "total": 58, "approved": 47 },
    { "date": "2025-05-20", "total": 44, "approved": 35 },
    { "date": "2025-05-21", "total": 34, "approved": 26 }
  ],

  "fraudBreakdown": {
    "totalFraudFlags": 7,
    "byType": [
      { "type": "id_fake",      "label": "Số CCCD giả",      "count": 3 },
      { "type": "tampering",    "label": "Ảnh bị chỉnh sửa", "count": 2 },
      { "type": "duplication",  "label": "CCCD trùng lặp",   "count": 1 },
      { "type": "face_compare", "label": "Khuôn mặt không khớp (< 70%)", "count": 1 }
    ]
  },

  "topMultipleAttempts": [
    {
      "identifierId": "038xxxxxxxx",
      "fullName": "Nguyễn Văn A",
      "phoneNo": "09xxxxxxxx",
      "attemptCount": 5,
      "accountStatus": "APPROVED",
      "mainFailureStep": "VNPT_OCR"
    }
  ]
}
```

---

## 📝 Detailed Requirements (PM CAN SKIP)

### Route & Layout

**Route:** `/admin/ekyc/dashboard`

**Filter bar:**
```
Khoảng thời gian: [7 ngày qua ▾]   [Áp dụng]
```
- Options: 7 ngày qua / 30 ngày qua / Tùy chọn (date range picker)
- Mặc định: 7 ngày qua

---

### Hàng 1 — KPI Cards (4 cards)

| Card | Metric | Đơn vị | Nguồn |
|------|--------|--------|-------|
| Tổng eKYC | `totalSubmissions` | lượt | — |
| Tỉ lệ thành công | `approvalRate` | % | `approvedCount / totalSubmissions` |
| Thất bại / Pending | `failedAndPendingCount` | case | — |
| Số lần thử TB | `avgAttemptsPerSuccess` | lần | per TK thành công |

**Card style:** số lớn + label nhỏ bên dưới + màu icon theo loại.

---

### Hàng 2 — KPI Cards (3 cards)

| Card | Metric | Đơn vị | Màu icon |
|------|--------|--------|---------|
| Phát hiện gian lận | `fraudDetectedCount` | case | 🔴 đỏ |
| Thời gian xử lý TB | `avgProcessingMinutes` | phút | 🔵 xanh |
| Tỉ lệ ký HĐ | `contractSignRate` | % | 🟢 xanh lá |

---

### Charts

#### Chart 1 — Tỉ lệ kết quả eKYC (Donut)
- **Dữ liệu:** `outcomeBreakdown`
- **Màu:** APPROVED = xanh lá, REJECTED = đỏ, PENDING = vàng
- **Center label:** tổng số submissions
- **Legend:** bên phải với count + %

#### Chart 2 — Nguyên nhân thất bại (Horizontal Bar)
- **Dữ liệu:** `failureStepBreakdown`
- **X-axis:** số lượng case
- **Y-axis:** label tiếng Việt (dùng `label` field)
- **Sort:** descending theo count
- **Color:** gradient đỏ

#### Chart 3 — Xu hướng theo ngày (Line Chart)
- **Dữ liệu:** `dailyTrend`
- **2 lines:** Tổng (xanh nhạt) và Thành công (xanh đậm)
- **X-axis:** ngày (DD/MM)
- **Tooltip:** khi hover hiện `total`, `approved`, và % success

#### Chart 4 — Fraud Detection (Bar + Summary)
- **Layout:** summary counter bên trái + bar chart bên phải
- **Summary:** "X case có fraud flags trong kỳ"
- **Bar chart:** `fraudBreakdown.byType` — horizontal bar, màu cam/đỏ
- **Click vào bar** → filter sang trang Danh sách eKYC với fraud type tương ứng (nice-to-have, không blocking)

---

### Bảng — Khách hàng nhiều lần thử

**Title:** "Top khách hàng nhiều lần thử nhất"

**Columns:**

| Cột | Nguồn | Ghi chú |
|-----|-------|--------|
| Họ tên | `fullName` | — |
| Số CCCD | `identifierId` | — |
| Số lần thử | `attemptCount` | Badge số, màu đỏ nếu ≥ 3 |
| Kết quả | `accountStatus` | Pill: APPROVED / PENDING / REJECTED |
| Nguyên nhân fail chính | `mainFailureStep` | Label tiếng Việt |
| Hành trình | — | Nút [Xem →] → link sang `/admin/ekyc/attempts/:identifierId` |

**Empty state:** "Không có case nào thử nhiều hơn 1 lần trong kỳ này"

---

### Xử lý edge cases

- Kỳ chưa có data (vừa go-live): hiển thị tất cả KPI = 0, charts rỗng với message "Chưa có dữ liệu trong khoảng thời gian này"
- `fraudDetectedCount = 0`: Chart 4 hiển thị "Không phát hiện case gian lận trong kỳ" thay vì chart rỗng
- API timeout (> 3s): hiển thị skeleton loader → error state với nút "Thử lại"

---

**Document Status:** Draft | **For:** FE Dev (ekyc-admin UI) + BE Dev | **Next Steps:** BE implement `GET /api/admin/ekyc/dashboard` → FE implement Dashboard page
