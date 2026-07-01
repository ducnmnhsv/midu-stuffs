# Dashboard Analytics API Specification

**Version:** 1.0 | **Date:** 2026-07-01 | **Service:** ekyc-admin
**Consumer:** Admin UI — Feature A (Dashboard Analytics)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Endpoint](#2-endpoint)
3. [Request Parameters](#3-request-parameters)
4. [Response Structure](#4-response-structure)
5. [Field Definitions & DB Queries](#5-field-definitions--db-queries)
6. [Caching Strategy](#6-caching-strategy)
7. [Error Handling](#7-error-handling)
8. [Business Rules & Edge Cases](#8-business-rules--edge-cases)

---

## 1. Overview

Dashboard aggregation endpoint cho Admin UI. Trả về số liệu tổng hợp về eKYC attempts trong một khoảng thời gian: KPI metrics, outcome breakdown, failure analysis, fraud detection, và daily trend.

**Nguồn dữ liệu chính:** `ekyc_attempt_log` (bảng mới, xem `Backend_Spec.md` Section 0).

**Đơn vị đo lường:** Tất cả KPI và breakdown đều tính theo **unique customer** (`DISTINCT identifier_id`), không phải tổng số lần thử — để phản ánh số người dùng thực sự, không bị inflate bởi retry.

> **Ngoại lệ:** `dailyTrend` và `topMultipleAttempts` dùng total attempts (có thể có nhiều lần/user).

---

## 2. Endpoint

```
GET /api/admin/ekyc/dashboard
```

| Property | Value |
|----------|-------|
| **Method** | GET |
| **Auth** | `ROLE_ADMIN` (JWT) |
| **Response time SLA** | < 3s (cache required — xem Section 6) |
| **Content-Type** | `application/json` |

---

## 3. Request Parameters

| Param | Type | Required | Default | Format | Mô tả |
|-------|------|----------|---------|--------|-------|
| `fromDate` | String | No | 7 ngày trước `toDate` | `yyyy-MM-dd` | Ngày bắt đầu (inclusive) |
| `toDate` | String | No | Ngày hiện tại | `yyyy-MM-dd` | Ngày kết thúc (inclusive) |
| `topN` | Integer | No | 10 | — | Số lượng rows trong `topMultipleAttempts` |

**Ví dụ:**
```
GET /api/admin/ekyc/dashboard?fromDate=2026-06-24&toDate=2026-07-01
GET /api/admin/ekyc/dashboard                          # 7 ngày qua mặc định
GET /api/admin/ekyc/dashboard?fromDate=2026-06-01&toDate=2026-06-30&topN=5
```

**Validation:**
- `fromDate` phải ≤ `toDate`
- Khoảng thời gian tối đa: 90 ngày — nếu vượt quá, trả `400 BAD_REQUEST`
- Nếu chỉ truyền `fromDate` mà không có `toDate`: `toDate` = ngày hiện tại

---

## 4. Response Structure

```json
{
  "period": {
    "from": "2026-06-24",
    "to": "2026-07-01"
  },

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
    { "outcome": "APPROVED",  "label": "Thành công",       "count": 248 },
    { "outcome": "REJECTED",  "label": "Bị từ chối",       "count": 41  },
    { "outcome": "PENDING",   "label": "Đang chờ",         "count": 23  }
  ],

  "failureStepBreakdown": [
    { "step": "VNPT_OCR",       "label": "Đọc thông tin CCCD",            "count": 28 },
    { "step": "VNPT_LIVENESS",  "label": "Xác minh khuôn mặt trực tiếp",  "count": 15 },
    { "step": "FACE_COMPARE",   "label": "So sánh khuôn mặt",             "count": 9  },
    { "step": "LOTTE_SUBMIT",   "label": "Gửi thông tin lên hệ thống",    "count": 5  },
    { "step": "MRZ_VALIDATION", "label": "Kiểm tra MRZ",                  "count": 3  },
    { "step": "CONTRACT_SIGN",  "label": "Ký hợp đồng điện tử",           "count": 4  }
  ],

  "dailyTrend": [
    { "date": "2026-06-24", "total": 38, "approved": 30 },
    { "date": "2026-06-25", "total": 45, "approved": 36 },
    { "date": "2026-06-26", "total": 42, "approved": 33 }
  ],

  "fraudBreakdown": {
    "totalFraudFlags": 7,
    "byType": [
      { "type": "id_fake",      "label": "Số CCCD giả",                  "count": 3 },
      { "type": "tampering",    "label": "Ảnh bị chỉnh sửa",             "count": 2 },
      { "type": "duplication",  "label": "CCCD trùng lặp",               "count": 1 },
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

## 5. Field Definitions & DB Queries

> Tất cả query dưới đây filter theo `attempt_at BETWEEN :fromDate AND :toDate + 1 DAY` (end-of-day inclusive).

### 5.1 `kpi` object

| Field | Đơn vị | DB Query |
|-------|--------|----------|
| `totalSubmissions` | Unique customers | `COUNT(DISTINCT identifier_id) FROM ekyc_attempt_log WHERE attempt_at IN range` |
| `approvedCount` | Unique customers | `COUNT(DISTINCT identifier_id) WHERE final_ekyc_id IS NOT NULL AND attempt_at IN range` |
| `approvalRate` | Decimal 0–1 | `approvedCount / totalSubmissions` (BE computed, round 3 decimals) |
| `failedAndPendingCount` | Unique customers | `totalSubmissions - approvedCount` |
| `avgAttemptsPerSuccess` | Decimal 1 chữ số | AVG của max(attempt_number) GROUP BY identifier_id — chỉ với các identifier_id đã APPROVED |
| `fraudDetectedCount` | Unique customers | `COUNT(DISTINCT identifier_id)` WHERE có ít nhất 1 fraud flag (xem Section 5.4) |
| `avgProcessingMinutes` | Decimal 1 chữ số | AVG của `(attempt_at của lần SUCCESS) - (attempt_at của lần 1)` tính bằng phút — chỉ với approved cases |
| `contractSignRate` | Decimal 0–1 | `COUNT(DISTINCT identifier_id WHERE outcome != 'USER_ABANDONED' AND final_ekyc_id IS NOT NULL)` / `approvedCount` |

> **Lưu ý `avgProcessingMinutes`:** Nếu user APPROVED ở lần 1 → processing time = 0 phút (instant). Nếu không có approved case nào trong kỳ → trả `null`.

### 5.2 `outcomeBreakdown`

Tính theo trạng thái **cuối cùng** của mỗi customer (không phải mỗi attempt):

```sql
SELECT
  CASE
    WHEN MAX(final_ekyc_id) IS NOT NULL THEN 'APPROVED'
    WHEN MAX(CASE WHEN outcome = 'USER_ABANDONED' THEN 1 END) = 1 THEN 'PENDING'
    ELSE 'REJECTED'
  END AS outcome,
  COUNT(DISTINCT identifier_id) AS count
FROM ekyc_attempt_log
WHERE attempt_at BETWEEN :from AND :to
GROUP BY outcome
```

Label mapping:

| outcome | label |
|---------|-------|
| `APPROVED` | Thành công |
| `REJECTED` | Bị từ chối |
| `PENDING` | Đang chờ |

### 5.3 `failureStepBreakdown`

Đếm số **unique customers** bị fail tại mỗi bước (dùng failure_step của lần thử gần nhất bị fail):

```sql
SELECT
  failure_step AS step,
  COUNT(DISTINCT identifier_id) AS count
FROM ekyc_attempt_log
WHERE attempt_at BETWEEN :from AND :to
  AND outcome != 'SUCCESS'
  AND failure_step IS NOT NULL
GROUP BY failure_step
ORDER BY count DESC
```

Label mapping:

| step | label |
|------|-------|
| `VNPT_OCR` | Đọc thông tin CCCD |
| `VNPT_LIVENESS` | Xác minh khuôn mặt trực tiếp |
| `VNPT_QUALITY` | Chất lượng ảnh kém |
| `FACE_COMPARE` | So sánh khuôn mặt |
| `MRZ_VALIDATION` | Kiểm tra MRZ |
| `MRZ_CROSS_CHECK` | Khớp MRZ với thông tin thẻ |
| `LOTTE_SUBMIT` | Gửi thông tin lên hệ thống |
| `CONTRACT_SIGN` | Ký hợp đồng điện tử |

### 5.4 `dailyTrend`

```sql
SELECT
  DATE(attempt_at) AS date,
  COUNT(DISTINCT identifier_id) AS total,
  COUNT(DISTINCT CASE WHEN final_ekyc_id IS NOT NULL THEN identifier_id END) AS approved
FROM ekyc_attempt_log
WHERE attempt_at BETWEEN :from AND :to
GROUP BY DATE(attempt_at)
ORDER BY date ASC
```

> Các ngày không có data vẫn được trả về trong array với `total: 0, approved: 0` — để FE vẽ line chart liên tục.

### 5.5 `fraudBreakdown`

**Định nghĩa "fraud flag" per type:**

| type | Điều kiện DB |
|------|-------------|
| `id_fake` | `vnpt_id_fake_prob > 0.5` OR `vnpt_id_fake_warning IS NOT NULL` |
| `tampering` | `vnpt_is_tampered = 'N'` |
| `duplication` | `vnpt_duplication_warning = true` |
| `face_compare` | `face_compare_prob < 0.70` AND `face_compare_prob IS NOT NULL` |

```sql
-- totalFraudFlags: unique customers với ít nhất 1 flag
SELECT COUNT(DISTINCT identifier_id) AS total
FROM ekyc_attempt_log
WHERE attempt_at BETWEEN :from AND :to
  AND (
    vnpt_id_fake_prob > 0.5
    OR vnpt_id_fake_warning IS NOT NULL
    OR vnpt_is_tampered = 'N'
    OR vnpt_duplication_warning = true
    OR (face_compare_prob IS NOT NULL AND face_compare_prob < 0.70)
  )

-- byType: 4 queries riêng, UNION hoặc computed in Service layer
```

> **Note:** Một customer có thể có nhiều fraud flag types — mỗi type đếm riêng, tổng `byType.count` có thể > `totalFraudFlags`.

### 5.6 `topMultipleAttempts`

```sql
SELECT
  a.identifier_id,
  e.full_name AS fullName,
  e.phone_no AS phoneNo,
  COUNT(a.id) AS attemptCount,
  CASE WHEN MAX(a.final_ekyc_id) IS NOT NULL THEN 'APPROVED' ELSE 'PENDING' END AS accountStatus,
  -- failure step xuất hiện nhiều nhất
  (SELECT failure_step FROM ekyc_attempt_log
   WHERE identifier_id = a.identifier_id AND failure_step IS NOT NULL
   GROUP BY failure_step ORDER BY COUNT(*) DESC LIMIT 1) AS mainFailureStep
FROM ekyc_attempt_log a
LEFT JOIN e_kyc e ON e.identifier_id = a.identifier_id
WHERE a.attempt_at BETWEEN :from AND :to
GROUP BY a.identifier_id
HAVING COUNT(a.id) > 1
ORDER BY attemptCount DESC
LIMIT :topN
```

---

## 6. Caching Strategy

**SLA:** < 3s. Với dataset lớn, aggregation query cần cache.

| Layer | Strategy | TTL |
|-------|----------|-----|
| **Application cache** (Spring Cache / Redis) | Cache key = `dashboard:{fromDate}:{toDate}:{topN}` | 10 phút |
| **Invalidation** | Xóa cache khi có insert vào `ekyc_attempt_log` (event-driven) hoặc TTL expire | — |
| **Fallback** | Nếu cache miss và query > 3s: trả partial response với `"cached": false` trong metadata | — |

**Implementation gợi ý:**

```java
@Cacheable(value = "ekycDashboard", key = "#fromDate + '_' + #toDate + '_' + #topN")
public EKycDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int topN) {
    // ...
}
```

---

## 7. Error Handling

| HTTP Status | Error Code | Trường hợp |
|-------------|------------|-----------|
| `400` | `INVALID_DATE_RANGE` | `fromDate > toDate` |
| `400` | `DATE_RANGE_TOO_LARGE` | Khoảng thời gian > 90 ngày |
| `400` | `INVALID_DATE_FORMAT` | Ngày không đúng format `yyyy-MM-dd` |
| `401` | — | Token không hợp lệ hoặc hết hạn |
| `403` | `FORBIDDEN` | User không có `ROLE_ADMIN` |
| `503` | `DASHBOARD_UNAVAILABLE` | Query timeout hoặc DB không khả dụng |

**Error response format (TradeX standard):**
```json
{
  "errorCode": "DATE_RANGE_TOO_LARGE",
  "message": "Maximum date range is 90 days",
  "timestamp": "2026-07-01T10:30:00Z"
}
```

---

## 8. Business Rules & Edge Cases

| Rule | Xử lý |
|------|-------|
| Kỳ không có data | Trả response đầy đủ với tất cả KPI = `0`, arrays rỗng `[]` — không trả `null` |
| `fraudDetectedCount = 0` | `fraudBreakdown.byType` = `[]`, `totalFraudFlags` = `0` |
| `approvedCount = 0` | `approvalRate = 0.0`, `avgAttemptsPerSuccess = null`, `contractSignRate = null` |
| `topMultipleAttempts` không có ai retry | Trả `[]` — FE hiển thị empty state |
| Customer có attempt trong range nhưng APPROVED ngoài range | Vẫn tính vào `totalSubmissions`, `accountStatus` phản ánh trạng thái thực tế (APPROVED) |
| Ngày có 0 submissions (trong dailyTrend) | Vẫn trả row `{ "date": "...", "total": 0, "approved": 0 }` |
| `fromDate = toDate` (1 ngày) | Hợp lệ — trả data của ngày đó |

---

## 9. DTO Reference

```java
// Request
@GetMapping("/api/admin/ekyc/dashboard")
public ResponseEntity<EKycDashboardResponse> getDashboard(
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate fromDate,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate toDate,
    @RequestParam(defaultValue = "10") @Min(1) @Max(50) int topN
)

// Response root
EKycDashboardResponse {
  period: PeriodDto { from: LocalDate, to: LocalDate }
  kpi: KpiDto
  outcomeBreakdown: List<OutcomeBreakdownItem>
  failureStepBreakdown: List<FailureStepBreakdownItem>
  dailyTrend: List<DailyTrendItem>
  fraudBreakdown: FraudBreakdownDto
  topMultipleAttempts: List<TopAttemptItem>
}

KpiDto {
  totalSubmissions: long
  approvedCount: long
  approvalRate: double           // 3 decimal places
  failedAndPendingCount: long
  avgAttemptsPerSuccess: Double  // nullable
  fraudDetectedCount: long
  avgProcessingMinutes: Double   // nullable
  contractSignRate: Double       // nullable
}

OutcomeBreakdownItem { outcome: String, label: String, count: long }
FailureStepBreakdownItem { step: String, label: String, count: long }
DailyTrendItem { date: LocalDate, total: long, approved: long }
FraudBreakdownDto { totalFraudFlags: long, byType: List<FraudTypeItem> }
FraudTypeItem { type: String, label: String, count: long }
TopAttemptItem {
  identifierId: String
  fullName: String
  phoneNo: String
  attemptCount: long
  accountStatus: String
  mainFailureStep: String        // nullable
}
```

---

**Document Status:** ✅ Complete | For: BE Dev (ekyc-admin) | Next Steps: Implement Service layer + Repository queries + Cache config → notify FE Dev khi endpoint live
