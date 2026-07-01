# Biometric Log Storage — API & DB Specification

**Document Type:** API Specification  
**Feature:** eKYC Biometric Attempt Log Storage  
**Integration type:** TradeX-native (internal DB only — không qua Lotte/Core)  
**Service:** `ekyc-admin` (Java / JHipster)  
**Jira:** NHMTS-682 (Derivatives go-live parent; eKYC sub-task)  
**Date:** 2026-07-01  
**Version:** 1.0

---

## Table of Contents

1. [Overview & Business Context](#1-overview--business-context)
2. [Current State — Gap Analysis](#2-current-state--gap-analysis)
3. [eKYC Flow with Biometric Log](#3-ekyc-flow-with-biometric-log)
4. [DB Schema](#4-db-schema)
5. [API: POST /ekycs/attempt-log](#5-api-post-ekycsattempt-log)
6. [API: GET /api/admin/ekyc/attempts/search](#6-api-get-apiadminekyattemptssearch)
7. [API: GET /api/admin/ekyc/attempts/{id}](#7-api-get-apiadminekyattemptsid)
8. [Error Handling](#8-error-handling)
9. [Business Rules & Validation](#9-business-rules--validation)

---

## 1. Overview & Business Context

### Problem

Hiện tại hệ thống eKYC không lưu dữ liệu sinh trắc học từ VNPT SDK. Cụ thể:

- `ekyc_ext.raw_data` chỉ lưu JSON blob không có cấu trúc — không thể query từng field.
- Khi user retry, `CustomEKycService.java:211` **xóa** attempt cũ: không có audit trail.
- Admin không có giao diện xem kết quả liveness / fraud detection / confidence score của từng lần eKYC.

### Business Goal

Lưu toàn bộ kết quả VNPT SDK (OCR + liveness + fraud detection + face compare) vào DB có cấu trúc để:

1. **Audit trail** — truy vết mọi attempt, kể cả retry và reject.
2. **Compliance** — cung cấp bằng chứng kiểm tra danh tính khi cần.
3. **Admin visibility** — xem từng field riêng lẻ trên admin page (không chỉ blob JSON).
4. **Fraud review** — flag các attempt có `idFakeProb` cao, `dupplicationWarning`, `dobFakeWarning`, v.v.

### Scope

- **App (FE):** Gọi `POST /ekycs/attempt-log` sau khi nhận kết quả từ VNPT SDK (dù thành công hay thất bại).
- **BE (ekyc-admin):** Nhận, parse, lưu vào table `ekyc_attempt_log`.
- **Admin REST:** Cung cấp `GET /api/admin/ekyc/attempts/search` để admin query.

---

## 2. Current State — Gap Analysis

| Component | Hiện tại | Cần làm |
|-----------|----------|---------|
| `ekyc_ext.raw_data` | JSON blob không cấu trúc | Bổ sung `ekyc_attempt_log`: cột riêng từng field + `vnpt_raw_data` LONGTEXT |
| `CustomEKycService.java:211` | Xóa attempt cũ khi retry | Giữ lại — chỉ thêm lưu log vào table mới |
| `POST /ekycs/attempt-log` | Chưa có | Tạo mới |
| `GET /api/admin/ekyc/attempts/*` | Chưa có | Tạo mới |
| Admin UI | Chưa có | Out of scope spec này — Admin UI là issue riêng |

---

## 3. eKYC Flow with Biometric Log

```
Mobile App
  │
  ├─ VNPT SDK xử lý: OCR + Card Liveness + Face Liveness + Mask + Compare
  │    SDK trả 8 log keys về Redux:
  │      LOG_OCR, LOG_LIVENESS_CARD_FRONT, LOG_LIVENESS_CARD_REAR,
  │      LOG_LIVENESS_FACE, LOG_MASK_FACE, LOG_COMPARE,
  │      LOG_PATH_IMAGE_FRONT, LOG_PATH_IMAGE_BACK
  │
  ├─ [THÊM MỚI] App gọi POST /ekycs/attempt-log
  │    Body: VNPT raw data từ 8 log keys + identifierId
  │    → ekyc-admin parse và lưu vào ekyc_attempt_log
  │    → Response: { id: <attemptLogId> }
  │
  ├─ App validate locally → nếu pass → POST /lotte/ekycs
  │    Body: rawData (Base64 VNPT response), identifierId, fullName, ...
  │    Lotte response: SUCCESS / REJECTED
  │
  └─ Nếu Lotte SUCCESS → Backend: e_kyc.status = APPROVED
       → CustomEKycService lưu e_kyc record
       → [THÊM MỚI] Gọi updateFinalEkycId(attemptLogId, ekycId)
            → ekyc_attempt_log.e_kyc_id = ekycId
```

---

## 4. DB Schema

### 4.1 Table: `ekyc_attempt_log`

Lưu toàn bộ kết quả VNPT SDK theo từng lần submit. Mỗi attempt là 1 row độc lập.

```sql
CREATE TABLE ekyc_attempt_log (
  -- Primary key
  id                          BIGINT AUTO_INCREMENT PRIMARY KEY,

  -- Link to e_kyc record (nullable: chỉ set sau khi Lotte APPROVED)
  e_kyc_id                    BIGINT NULL,
  FOREIGN KEY (e_kyc_id) REFERENCES e_kyc(id),

  -- User identity
  identifier_id               VARCHAR(50)    NOT NULL COMMENT 'CCCD / CMND của user',
  user_id                     VARCHAR(100)   NULL     COMMENT 'userId từ JWT, nếu đã login',

  -- VNPT top-level
  vnpt_status_code            INT            NULL     COMMENT '0 = success từ VNPT',
  vnpt_message                VARCHAR(500)   NULL,
  server_version              VARCHAR(50)    NULL,
  challenge_code              VARCHAR(200)   NULL     COMMENT 'Transaction ID từ VNPT (trace key)',

  -- OCR fields
  ocr_citizen_id              VARCHAR(20)    NULL,
  ocr_name                    VARCHAR(200)   NULL,
  ocr_card_type               VARCHAR(20)    NULL     COMMENT 'CMND / CC',
  ocr_birth_day               VARCHAR(20)    NULL     COMMENT 'YYMMDD',
  ocr_gender                  VARCHAR(10)    NULL,
  ocr_nationality             VARCHAR(50)    NULL,
  ocr_citizen_id_chip         VARCHAR(50)    NULL     COMMENT 'Số CCCD từ chip',
  ocr_issue_date              VARCHAR(20)    NULL,
  ocr_valid_date              VARCHAR(20)    NULL,

  -- Confidence scores
  citizen_id_prob             DOUBLE         NULL     COMMENT '0-1: xác suất đọc đúng CCCD',
  mrz_valid_score             INT            NULL     COMMENT '0-10',
  mrz_prob                    DOUBLE         NULL,

  -- Card integrity
  tampering_is_legal          VARCHAR(5)     NULL     COMMENT 'Y = hợp lệ, N = giả mạo',

  -- Fraud detection
  id_fake_prob                DOUBLE         NULL     COMMENT '0-1: xác suất thẻ giả',
  dupplication_warning        BOOLEAN        NULL     COMMENT 'true = trùng lặp',
  dob_fake_warning            BOOLEAN        NULL     COMMENT 'true = ngày sinh giả',
  address_fake_warning        BOOLEAN        NULL     COMMENT 'true = địa chỉ giả',

  -- Image quality — front
  quality_front_blur_score    DOUBLE         NULL,
  quality_front_luminance     DOUBLE         NULL,

  -- Image quality — back
  quality_back_blur_score     DOUBLE         NULL,
  quality_back_luminance      DOUBLE         NULL,

  -- Card liveness check (front)
  card_front_recapture_result VARCHAR(20)    NULL     COMMENT 'REAL / SCREEN_SHOT',
  card_front_edited_prob      DOUBLE         NULL,
  card_front_photocopy_result VARCHAR(20)    NULL,

  -- Cross-validation front vs back
  match_front_back_id         VARCHAR(10)    NULL     COMMENT 'MATCH / NO_MATCH',
  match_front_back_name       VARCHAR(10)    NULL,
  match_front_back_bod        VARCHAR(10)    NULL,
  match_front_back_valid_date VARCHAR(10)    NULL,

  -- SDK liveness results (từ App, không từ VNPT trực tiếp)
  liveness_card_front         VARCHAR(20)    NULL     COMMENT 'success / failure',
  liveness_card_rear          VARCHAR(20)    NULL     COMMENT 'success / failure',
  liveness_face               VARCHAR(20)    NULL     COMMENT 'success / failure',
  fake_liveness_prob          DOUBLE         NULL     COMMENT '0-1',
  fake_print_photo_prob       DOUBLE         NULL     COMMENT '0-1',
  mask_result                 VARCHAR(20)    NULL     COMMENT 'success / failure',

  -- Face compare
  face_compare_msg            VARCHAR(20)    NULL     COMMENT 'MATCH / NOMATCH',
  face_compare_prob           DOUBLE         NULL     COMMENT '0-1: confidence score',

  -- Overall outcome
  attempt_result              VARCHAR(20)    NOT NULL COMMENT 'PASS / FAIL / PENDING',

  -- Raw data (full VNPT response for debugging/audit)
  vnpt_raw_data               LONGTEXT       NULL     COMMENT 'Full VNPT JSON response (Base64 decoded)',

  -- Timestamps
  created_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- Indexes
  INDEX idx_identifier_id     (identifier_id),
  INDEX idx_e_kyc_id          (e_kyc_id),
  INDEX idx_created_at        (created_at),
  INDEX idx_attempt_result    (attempt_result)
);
```

### 4.2 Quan hệ với bảng hiện tại

```
e_kyc (1) ──── (0..n) ekyc_attempt_log
             (e_kyc_id FK, nullable)

ekyc_ext (hiện tại) — KHÔNG xóa; vẫn giữ raw_data JSON blob
                       ekyc_attempt_log bổ sung cấu trúc có thể query
```

---

## 5. API: POST /ekycs/attempt-log

### Overview

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /ekycs/attempt-log` |
| **Service** | `ekyc-admin` |
| **Integration type** | TradeX-native (lưu trực tiếp vào DB) |
| **Auth** | Bearer JWT (user token) |
| **Caller** | Mobile App — gọi ngay sau khi VNPT SDK trả kết quả |
| **Timing** | Trước khi gọi `POST /lotte/ekycs` |

### Request Body

```json
{
  "identifierId": "012345678901",
  "vnptStatusCode": 0,
  "vnptMessage": "success",
  "serverVersion": "3.2.1",
  "challengeCode": "TXN-20260701-abc123",
  "ocr": {
    "citizenId": "012345678901",
    "name": "NGUYEN VAN A",
    "cardType": "CC",
    "birthDay": "900101",
    "gender": "Nam",
    "nationality": "VN",
    "citizenIdChip": "012345678901",
    "issueDate": "20200101",
    "validDate": "20300101"
  },
  "scores": {
    "citizenIdProb": 0.98,
    "mrzValidScore": 9,
    "mrzProb": 0.97
  },
  "tamperingIsLegal": "Y",
  "fraud": {
    "idFakeProb": 0.02,
    "dupplicationWarning": false,
    "dobFakeWarning": false,
    "addressFakeWarning": false
  },
  "imageQuality": {
    "frontBlurScore": 0.91,
    "frontLuminanceScore": 0.88,
    "backBlurScore": 0.90,
    "backLuminanceScore": 0.85
  },
  "cardCheck": {
    "frontRecaptureResult": "REAL",
    "frontEditedProb": 0.01,
    "frontPhotocopyResult": "ORIGINAL"
  },
  "matchFrontBack": {
    "matchId": "MATCH",
    "matchName": "MATCH",
    "matchBod": "MATCH",
    "matchValidDate": "MATCH"
  },
  "liveness": {
    "cardFront": "success",
    "cardRear": "success",
    "face": "success",
    "fakeLivenessProb": 0.03,
    "fakePrintPhotoProb": 0.02
  },
  "maskResult": "success",
  "faceCompare": {
    "msg": "MATCH",
    "prob": 0.95
  }
}
```

### Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `identifierId` | String | **Y** | CCCD / CMND của user |
| `vnptStatusCode` | Integer | N | 0 = success từ VNPT |
| `vnptMessage` | String | N | Message từ VNPT |
| `serverVersion` | String | N | Version VNPT server |
| `challengeCode` | String | N | Transaction ID — trace key |
| `ocr.*` | Object | N | OCR fields từ LOG_OCR |
| `scores.*` | Object | N | Confidence scores |
| `tamperingIsLegal` | String | N | `Y` / `N` |
| `fraud.*` | Object | N | Fraud detection flags |
| `imageQuality.*` | Object | N | Blur/luminance scores |
| `cardCheck.*` | Object | N | Card liveness check kết quả |
| `matchFrontBack.*` | Object | N | Cross-validation front vs back |
| `liveness.*` | Object | N | SDK liveness results |
| `maskResult` | String | N | `success` / `failure` |
| `faceCompare.*` | Object | N | Face compare result |

> **Lưu ý:** Chỉ `identifierId` là bắt buộc. Các field còn lại optional vì App có thể chỉ có partial data tùy từng lần VNPT SDK trả về.

### Response (200 OK)

```json
{
  "id": 1042
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | ID của `ekyc_attempt_log` record vừa tạo |

### Auto-Populated Fields (Backend)

| Field DB | Nguồn | Ghi chú |
|----------|-------|---------|
| `user_id` | JWT Token | Lấy từ `userData.username` nếu user đã login |
| `attempt_result` | Logic BE | `PASS` nếu tất cả liveness/compare thành công; `FAIL` nếu có failure; `PENDING` nếu chưa đủ data |
| `created_at` | DB | Auto `CURRENT_TIMESTAMP` |

---

## 6. API: GET /api/admin/ekyc/attempts/search

### Overview

| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/admin/ekyc/attempts/search` |
| **Service** | `ekyc-admin` |
| **Integration type** | TradeX-native (query DB) |
| **Auth** | Admin JWT (ROLE_ADMIN) |
| **Caller** | Admin page |

### Query Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `identifierId` | String | N | Filter theo CCCD / CMND | `012345678901` |
| `attemptResult` | String | N | `PASS` / `FAIL` / `PENDING` | `FAIL` |
| `fromDate` | String | N | Format: `yyyyMMdd` | `20260601` |
| `toDate` | String | N | Format: `yyyyMMdd` | `20260701` |
| `hasEkycId` | Boolean | N | `true` = chỉ lấy attempt đã link với e_kyc | `true` |
| `page` | Integer | N | 0-based page index (default: 0) | `0` |
| `size` | Integer | N | Page size (default: 20, max: 100) | `20` |

### Response (200 OK)

```json
{
  "totalCount": 45,
  "attempts": [
    {
      "id": 1042,
      "ekycId": 201,
      "identifierId": "012345678901",
      "userId": "user_abc",
      "challengeCode": "TXN-20260701-abc123",
      "attemptResult": "PASS",
      "vnptStatusCode": 0,
      "ocrCitizenId": "012345678901",
      "ocrName": "NGUYEN VAN A",
      "ocrCardType": "CC",
      "ocrBirthDay": "900101",
      "citizenIdProb": 0.98,
      "mrzValidScore": 9,
      "tamperingIsLegal": "Y",
      "idFakeProb": 0.02,
      "dupplicationWarning": false,
      "dobFakeWarning": false,
      "addressFakeWarning": false,
      "livenessCardFront": "success",
      "livenessCardRear": "success",
      "livenessFace": "success",
      "fakeLivenessProb": 0.03,
      "maskResult": "success",
      "faceCompareMsg": "MATCH",
      "faceCompareProb": 0.95,
      "cardFrontRecaptureResult": "REAL",
      "matchFrontBackId": "MATCH",
      "createdAt": "2026-07-01T10:30:00"
    }
  ]
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `totalCount` | Integer | Tổng số records khớp filter |
| `attempts[]` | Array | Danh sách attempt logs |
| `attempts[].id` | Long | ID attempt log |
| `attempts[].ekycId` | Long (nullable) | ID e_kyc nếu đã APPROVED |
| `attempts[].identifierId` | String | CCCD / CMND |
| `attempts[].userId` | String | User ID |
| `attempts[].challengeCode` | String | VNPT transaction ID |
| `attempts[].attemptResult` | String | `PASS` / `FAIL` / `PENDING` |
| `attempts[].vnptStatusCode` | Integer | VNPT status (0 = success) |
| `attempts[].ocrCitizenId` | String | CCCD từ OCR |
| `attempts[].ocrName` | String | Tên từ OCR |
| `attempts[].ocrCardType` | String | `CMND` / `CC` |
| `attempts[].ocrBirthDay` | String | YYMMDD |
| `attempts[].citizenIdProb` | Double | Confidence score CCCD |
| `attempts[].mrzValidScore` | Integer | MRZ score 0-10 |
| `attempts[].tamperingIsLegal` | String | `Y` / `N` |
| `attempts[].idFakeProb` | Double | Xác suất thẻ giả (0-1) |
| `attempts[].dupplicationWarning` | Boolean | Cảnh báo trùng lặp |
| `attempts[].dobFakeWarning` | Boolean | Cảnh báo ngày sinh giả |
| `attempts[].addressFakeWarning` | Boolean | Cảnh báo địa chỉ giả |
| `attempts[].livenessCardFront` | String | `success` / `failure` |
| `attempts[].livenessCardRear` | String | `success` / `failure` |
| `attempts[].livenessFace` | String | `success` / `failure` |
| `attempts[].fakeLivenessProb` | Double | Xác suất liveness giả |
| `attempts[].maskResult` | String | `success` / `failure` |
| `attempts[].faceCompareMsg` | String | `MATCH` / `NOMATCH` |
| `attempts[].faceCompareProb` | Double | Face compare confidence |
| `attempts[].cardFrontRecaptureResult` | String | `REAL` / `SCREEN_SHOT` |
| `attempts[].matchFrontBackId` | String | Cross-validation CCCD |
| `attempts[].createdAt` | String | ISO 8601 datetime |

---

## 7. API: GET /api/admin/ekyc/attempts/{id}

### Overview

| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/admin/ekyc/attempts/{id}` |
| **Auth** | Admin JWT (ROLE_ADMIN) |
| **Purpose** | Xem chi tiết 1 attempt log (tất cả fields) |

### Path Parameter

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Long | **Y** | ID của attempt log |

### Response (200 OK)

Trả về toàn bộ fields của record, bao gồm tất cả fields trong response search ở trên, cộng thêm:

```json
{
  "id": 1042,
  "ekycId": 201,
  "identifierId": "012345678901",
  "serverVersion": "3.2.1",
  "ocrGender": "Nam",
  "ocrNationality": "VN",
  "ocrCitizenIdChip": "012345678901",
  "ocrIssueDate": "20200101",
  "ocrValidDate": "20300101",
  "mrzProb": 0.97,
  "qualityFrontBlurScore": 0.91,
  "qualityFrontLuminance": 0.88,
  "qualityBackBlurScore": 0.90,
  "qualityBackLuminance": 0.85,
  "cardFrontEditedProb": 0.01,
  "cardFrontPhotocopyResult": "ORIGINAL",
  "matchFrontBackName": "MATCH",
  "matchFrontBackBod": "MATCH",
  "matchFrontBackValidDate": "MATCH",
  "fakePrintPhotoProb": 0.02,
  "createdAt": "2026-07-01T10:30:00",
  "updatedAt": "2026-07-01T10:30:05"
}
```

### Error Response (404)

```json
{
  "code": "OBJECT_NOT_FOUND",
  "message": "eKYC attempt log not found"
}
```

---

## 8. Error Handling

| Scenario | HTTP Status | Error Code | Description |
|----------|-------------|------------|-------------|
| `identifierId` thiếu | 400 | `INVALID_PARAMETER` + `FIELD_IS_REQUIRED` | Required field validation |
| `id` không tồn tại | 404 | `OBJECT_NOT_FOUND` | Detail endpoint |
| Token hết hạn | 401 | `TOKEN_EXPIRED` | Auth error |
| Không có quyền Admin | 403 | `FORBIDDEN` | Admin endpoints |
| DB error | 500 | `INTERNAL_SERVER_ERROR` | System error |

---

## 9. Business Rules & Validation

| Rule | Condition | Action |
|------|-----------|--------|
| `identifierId` required | Thiếu field | 400 `INVALID_PARAMETER` |
| Lưu kể cả khi VNPT trả lỗi | `vnptStatusCode != 0` | Vẫn lưu với `attemptResult = FAIL` |
| Link `e_kyc_id` | Sau khi Lotte APPROVED | Update `ekyc_attempt_log.e_kyc_id` |
| `attempt_result` logic | `PASS` = tất cả liveness/compare success; `FAIL` = có ít nhất 1 failure; `PENDING` = thiếu data | Backend auto-set |
| Không xóa attempt cũ | Khi user retry | Mỗi attempt là 1 row mới độc lập |
| `ekyc_ext` vẫn giữ | Migration | Không xóa bảng cũ — `ekyc_attempt_log` bổ sung thêm |

---

**Document Status:** ✅ Complete | For: BE Dev (ekyc-admin team) | Next Steps: BE Dev implement theo issue `BE_Issue_Biometric_Log_Storage.md`, sau đó Admin UI team tạo FE issue riêng
