# FE Issue: Admin eKYC — Admin UI

**Service:** ekyc-admin (Admin UI)  
**Priority:** Medium  
**Type:** New Feature

---

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

1. **Dashboard:** BA/PM và team Security không có số liệu tổng hợp về eKYC — tỉ lệ fail theo nguyên nhân, xu hướng theo ngày, fraud detection metrics. Mọi phân tích đang phải query thủ công từ DB.

2. **Tra cứu:** Team vận hành không có công cụ tra cứu lịch sử eKYC của khách hàng. Khi có case hỗ trợ "tại sao khách không mở được TK?", Ops phải hỏi Dev để tra DB thủ công — tốn thời gian và không scalable.

### Current vs Target

| Hiện tại | Sau khi hoàn thành |
|---------|-------------------|
| Không có Dashboard eKYC | Dashboard với 7 KPI cards + 4 charts |
| Phân tích phải query DB thủ công | BA/PM xem số liệu realtime trong < 3s |
| Không biết nguyên nhân fail phổ biến | Bar chart breakdown theo failure step |
| Không phát hiện pattern fraud | Fraud detection panel riêng |
| Không có màn tra cứu lịch sử eKYC | Màn hình tìm kiếm theo CCCD/SĐT |
| Ops phải hỏi Dev tra DB | Ops tự tra cứu trong < 1 phút |
| Không biết user fail mấy lần, lý do gì | Xem timeline đầy đủ từng lần thử |

### Features Overview

- **Feature A — Dashboard Analytics:** 1 trang dashboard với 7 KPI cards + 4 charts + bảng top N khách hàng nhiều lần thử
- **Feature B — Tra cứu Hành trình:** 3 màn hình — Search → Timeline → Chi tiết lần thử

### Timeline

Backend APIs cần sẵn sàng trước → FE implement sau.

### Success Criteria

- [ ] BA/PM xem được tỉ lệ fail theo nguyên nhân mà không cần hỏi Dev
- [ ] Dashboard load < 3s
- [ ] Fraud detection panel giúp Security nhận biết case đáng ngờ
- [ ] Ops có thể tìm được hành trình của bất kỳ khách hàng nào trong < 1 phút
- [ ] Thấy được lý do fail cụ thể (ví dụ: "ảnh mặt trước quá mờ, blur_score = 0.23")
- [ ] Link được sang thông tin tài khoản đã mở thành công

---

## 🔍 Technical Background (PM CAN SKIP)

### Feature A — Dashboard Analytics

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
    { "date": "2025-05-15", "total": 38, "approved": 30 }
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

### Feature B — Tra cứu Hành trình

**Base:** `GET /api/admin/ekyc/attempts`

| Endpoint | Mô tả |
|---------|-------|
| `GET /search?identifierId=xxx` | Tìm kiếm theo CCCD/SĐT |
| `GET /{identifierId}` | Lấy danh sách lần thử |
| `GET /{identifierId}/{attemptNumber}` | Chi tiết một lần thử |

Chi tiết response: xem [Backend_Spec.md](../Specifications/Backend_Spec.md) Section 4.

---

## 📝 Detailed Requirements (PM CAN SKIP)

---

### Feature A — Dashboard Analytics

#### Route & Filter

**Route:** `/admin/ekyc/dashboard`

**Filter bar:**
```
Khoảng thời gian: [7 ngày qua ▾]   [Áp dụng]
```
- Options: 7 ngày qua / 30 ngày qua / Tùy chọn (date range picker)
- Mặc định: 7 ngày qua

#### Hàng 1 — KPI Cards (4 cards)

| Card | Metric | Đơn vị | Nguồn |
|------|--------|--------|-------|
| Tổng eKYC | `totalSubmissions` | lượt | — |
| Tỉ lệ thành công | `approvalRate` | % | `approvedCount / totalSubmissions` |
| Thất bại / Pending | `failedAndPendingCount` | case | — |
| Số lần thử TB | `avgAttemptsPerSuccess` | lần | per TK thành công |

**Card style:** số lớn + label nhỏ bên dưới + màu icon theo loại.

#### Hàng 2 — KPI Cards (3 cards)

| Card | Metric | Đơn vị | Màu icon |
|------|--------|--------|---------|
| Phát hiện gian lận | `fraudDetectedCount` | case | 🔴 đỏ |
| Thời gian xử lý TB | `avgProcessingMinutes` | phút | 🔵 xanh |
| Tỉ lệ ký HĐ | `contractSignRate` | % | 🟢 xanh lá |

#### Charts

**Chart 1 — Tỉ lệ kết quả eKYC (Donut)**
- Dữ liệu: `outcomeBreakdown`
- Màu: APPROVED = xanh lá, REJECTED = đỏ, PENDING = vàng
- Center label: tổng số submissions
- Legend: bên phải với count + %

**Chart 2 — Nguyên nhân thất bại (Horizontal Bar)**
- Dữ liệu: `failureStepBreakdown`
- X-axis: số lượng case; Y-axis: label tiếng Việt
- Sort: descending theo count; Color: gradient đỏ

**Chart 3 — Xu hướng theo ngày (Line Chart)**
- 2 lines: Tổng (xanh nhạt) và Thành công (xanh đậm)
- X-axis: ngày (DD/MM)
- Tooltip: khi hover hiện `total`, `approved`, và % success

**Chart 4 — Fraud Detection (Bar + Summary)**
- Layout: summary counter bên trái + bar chart bên phải
- Summary: "X case có fraud flags trong kỳ"
- Bar chart: `fraudBreakdown.byType` — horizontal bar, màu cam/đỏ
- Click vào bar → filter sang trang Danh sách eKYC với fraud type (nice-to-have)

#### Bảng — Khách hàng nhiều lần thử

**Title:** "Top khách hàng nhiều lần thử nhất"

| Cột | Nguồn | Ghi chú |
|-----|-------|--------|
| Họ tên | `fullName` | — |
| Số CCCD | `identifierId` | — |
| Số lần thử | `attemptCount` | Badge số, màu đỏ nếu ≥ 3 |
| Kết quả | `accountStatus` | Pill: APPROVED / PENDING / REJECTED |
| Nguyên nhân fail chính | `mainFailureStep` | Label tiếng Việt |
| Hành trình | — | Nút [Xem →] → link sang `/admin/ekyc/attempts/:identifierId` |

**Empty state:** "Không có case nào thử nhiều hơn 1 lần trong kỳ này"

#### Edge Cases

- Kỳ chưa có data: hiển thị tất cả KPI = 0, charts rỗng với message "Chưa có dữ liệu trong khoảng thời gian này"
- `fraudDetectedCount = 0`: Chart 4 hiển thị "Không phát hiện case gian lận trong kỳ"
- API timeout (> 3s): skeleton loader → error state với nút "Thử lại"

---

### Feature B — Tra cứu Hành trình

#### Màn hình 1: Tìm kiếm

**Route:** `/admin/ekyc/attempts`

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│ Tra cứu hành trình eKYC                                 │
├──────────────────────────┬──────────────────────────────┤
│ Số CCCD/CMND             │ Số điện thoại                │
│ [________________]       │ [________________]           │
│                    [Tìm kiếm]                           │
└─────────────────────────────────────────────────────────┘
```

**Kết quả:**

| Trường | Nguồn |
|--------|-------|
| Số CCCD | `identifierId` |
| Họ tên | `fullName` từ `e_kyc` |
| SĐT | `phoneNo` |
| Tổng lần thử | `totalAttempts` |
| Trạng thái TK | `accountStatus` + `accountNumber` |
| Thời gian mở TK | `accountOpenedAt` |

**Nút action:** [Xem hành trình →] → navigate sang Màn hình 2  
**Empty state:** "Không tìm thấy khách hàng với thông tin này"  
**Validation:** Phải nhập ít nhất 1 trong 2 trường (CCCD hoặc SĐT)

#### Màn hình 2: Customer Journey (Timeline)

**Route:** `/admin/ekyc/attempts/:identifierId`

**Header:**
```
Khách hàng: Nguyễn Văn A
CCCD: 038xxx | SĐT: 09xxx
Tổng lần thử: 3 (2 thất bại, 1 thành công)
Thời gian: 15/05/2025 → 18/05/2025 (3 ngày)
```

**Tài khoản đã mở (nếu có):**
```
✅ Tài khoản: 039C123456
[Xem chi tiết tài khoản →]   ← link sang trang quản lý TK hiện có
```

**Timeline items** — mỗi lần thử là 1 item:
```
[Lần 1 — 15/05/2025 09:23] ❌ Thất bại
  Lý do: Ảnh mờ (VNPT_OCR)
  Chất lượng ảnh trước: Mờ (0.23 / 1.0)
  [Xem chi tiết →]

[Lần 3 — 18/05/2025 10:05] ✅ Thành công
  Matching rate: 92%
  Tài khoản: 039C123456
  [Xem chi tiết →]
```

**Outcome badge colors:** SUCCESS → green | VNPT_FAILED → red | LOTTE_REJECTED → orange | USER_ABANDONED → gray

**Outcome labels:**

| outcome | Hiển thị |
|---------|---------|
| `VNPT_FAILED` | Thất bại — Lỗi xác thực ảnh |
| `LOTTE_REJECTED` | Thất bại — Lỗi từ hệ thống |
| `USER_ABANDONED` | Bỏ dở — Chưa ký hợp đồng |
| `SUCCESS` | Thành công |

**failureStep labels:**

| failureStep | Hiển thị |
|------------|---------|
| `VNPT_OCR` | Đọc thông tin CCCD |
| `VNPT_LIVENESS` | Xác minh khuôn mặt trực tiếp |
| `FACE_COMPARE` | So sánh khuôn mặt |
| `LOTTE_SUBMIT` | Gửi thông tin lên hệ thống |
| `CONTRACT_SIGN` | Ký hợp đồng điện tử |

#### Màn hình 3: Chi tiết lần thử

**Route:** `/admin/ekyc/attempts/:identifierId/:attemptNumber`  
**Breadcrumb:** Tìm kiếm → Nguyễn Văn A → Lần thử #1

**Section 1: Thông tin chung**
```
Lần thử #1 | 15/05/2025 09:23:14
Kết quả: ❌ Thất bại — Ảnh mờ
```

**Section 2: Thông tin OCR (VNPT đọc được)**

| Trường | Giá trị | So sánh với user nhập |
|--------|---------|----------------------|
| Số CCCD | 038xxx | ✓ Khớp |
| Họ tên | NGUYEN VAN A | ✓ Khớp |
| Loại thẻ | CCCD mới | — |
| Confidence số CCCD | 95% | — |
| Điểm MRZ | 8/10 | — |

**Section 3: Chất lượng ảnh**

| Mặt | Độ nét | Độ sáng | Đánh giá |
|-----|--------|---------|---------|
| Mặt trước | 0.23 / 1.0 | 0.75 | ❌ Quá mờ |
| Mặt sau | 0.72 / 1.0 | 0.80 | ✓ Đạt |

> Ngưỡng chấp nhận: blur_score ≥ 0.5, luminance_score ≥ 0.4

**Section 4: Kiểm tra giả mạo**

| Kiểm tra | Kết quả |
|---------|---------|
| Chỉnh sửa ảnh (mặt trước) | ✓ Không phát hiện (1%) |
| Chụp màn hình (mặt trước) | ✓ Ảnh gốc |
| Photocopy (mặt trước) | ✓ Ảnh gốc |
| Tampering tổng thể | ✓ Hợp lệ |
| Số CCCD giả | ✓ Không phát hiện (2%) |
| Trùng lặp CCCD | ✓ Không |

**Section 5: Khớp 2 mặt**

| Trường | Kết quả |
|--------|---------|
| Số CCCD | ✓ Khớp |
| Họ tên | ✓ Khớp |
| Ngày sinh | ✓ Khớp |
| Ngày hết hạn | ✓ Khớp |

**Section 6: Lý do thất bại** (highlight màu đỏ)
```
❌ Ảnh mặt trước quá mờ
   blur_score: 0.23 (yêu cầu ≥ 0.5)
```

#### Edge Cases

- Chưa có data `ekyc_attempt_log` (khách mở TK trước khi feature go-live): hiển thị "Không có lịch sử lần thử"
- `vnptRawData` null (lỗi trước bước VNPT): ẩn các section VNPT, chỉ hiển thị `outcome` và `failureCode`
- `final_ekyc_id` null (chưa mở TK thành công): ẩn section "Tài khoản đã mở"

---

**Document Status:** Draft | **For:** FE Dev (ekyc-admin UI) + BE Dev | **Next Steps:** BE implement Dashboard API + Attempts APIs → FE implement cả 2 features
