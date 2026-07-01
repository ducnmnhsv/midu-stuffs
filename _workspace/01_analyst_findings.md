# Analyst Findings — eKYC Biometric Log

**Source:** Knowledge/TradeX-MCP/ekyc-admin-main (Java JHipster), eKYC/ documentation
**Prepared for:** Creator (Task #2 — BE Issue + Spec)
**Date:** 2026-07-01

---

## 1. eKYC Flow Overview

```
Mobile App
  ├─ VNPT SDK xử lý: OCR + Card Liveness + Face Liveness + Face Mask + Face Compare
  │  SDK trả 8 log keys về Redux:
  │    LOG_OCR, LOG_LIVENESS_CARD_FRONT, LOG_LIVENESS_CARD_REAR,
  │    LOG_LIVENESS_FACE, LOG_MASK_FACE, LOG_COMPARE,
  │    LOG_PATH_IMAGE_FRONT, LOG_PATH_IMAGE_BACK
  │
  ├─ App validate locally → nếu pass → POST /lotte/ekycs
  │    Body: rawData (Base64 VNPT response), identifierId, fullName, ...
  │    Lotte response: SUCCESS / REJECTED
  │
  └─ Nếu Lotte SUCCESS → Backend: e_kyc.status = APPROVED
```

**Critical issue hiện tại:**
```java
// CustomEKycService.java:211 — XÓA attempt cũ khi user retry
customEKycRepo.deleteAll(existedPendingEKycs.subList(1, size));
// → Không có audit trail, không có biometric log
```

### Endpoints liên quan

| Endpoint | File | Status |
|----------|------|--------|
| `POST /lotte/ekycs` | CustomEKycService.lotteEKycService() | ✅ Exists |
| Provisioning | TtlOpenAccountService.openAccountTTL() | ✅ Exists |
| `POST /ekycs/attempt-log` | EKycAttemptLogResource (NEW) | ❌ Chưa có |
| `GET /api/admin/ekyc/attempts/*` | Admin REST (NEW) | ❌ Chưa có |

---

## 2. VNPT Biometric Response Structure

**Model file:** `ekyc-admin/.../models/request/VNPTDataBase64.java`

```
VNPTDataBase64 {
  statusCode: int           // 0 = success
  message: String
  serverVersion: String
  challengeCode: String     // Transaction ID (trace key)

  object: {
    // OCR
    citizenId: String       // So CCCD
    name: String
    cardType: String        // "CMND" / "CC"
    birthDay: String        // YYMMDD
    gender: String
    nationality: String
    citizenIdChip: String   // Tu chip (co the khac mat the)
    issueDate / validDate: String

    // Confidence scores
    citizenIdProb: double   // 0-1
    mrzValidScore: int      // 0-10
    mrzProb: double
    mrz: List<String>       // [line1, line2]

    // Card integrity
    tampering.isLegal: String   // "Y" = hop le, "N" = gia mao

    // Fraud detection
    idFakeProb: double
    dupplicationWarning: bool
    dobFakeWarning: bool
    addressFakeWarning: bool

    // Image quality (front & back)
    qualityFront.blurScore: double
    qualityFront.luminanceScore: double
    qualityBack.blurScore / luminanceScore: double

    // Card checking
    checkingResultFront.recapturedResult: String  // "REAL"/"SCREEN_SHOT"
    checkingResultFront.editedProb: double
    checkingResultFront.checkPhotocopiedResult: String

    // Cross-validation front vs back
    matchFrontBack.matchId/matchName/matchBod/matchValidDate: String
  },

  imgs: {
    imgFront: String    // Base64 anh mat truoc
    imgBack: String     // Base64 anh mat sau
  }
}
```

### SDK-only fields (App gui them, VNPT khong tra)

| SDK Key | Field | Example |
|---------|-------|---------|
| LOG_LIVENESS_CARD_FRONT | .object.liveness | "success"/"failure" |
| LOG_LIVENESS_CARD_REAR | .object.liveness | "success"/"failure" |
| LOG_LIVENESS_FACE | .object.liveness | "success"/"failure" |
| LOG_LIVENESS_FACE | .object.fake_liveness_prob | 0.05 |
| LOG_LIVENESS_FACE | .object.fake_print_photo_prob | 0.02 |
| LOG_MASK_FACE | .object.mask_result | "success"/"failure" |
| LOG_COMPARE | .object.msg | "MATCH"/"NOMATCH" |
| LOG_COMPARE | .object.prob | 0.91 |

---

## 3. DB Schema Hien Tai

```sql
e_kyc {
  id, identifier_id, full_name, phone_no,
  status ENUM(PENDING, APPROVED, REJECTED, WAITING_CONFIRMATION),
  front_image_url, back_image_url,
  matching_rate DOUBLE,
  card_liveness_log_id, face_liveness_log_id, compare_log_id,
  created_at, updated_at
  -- THIEU: cac field sinh trac hoc chi tiet, fraud flags
}

ekyc_ext {
  id, e_kyc_id FK,
  raw_data LONGTEXT,  -- JSON tho (unstructured)
  log_id VARCHAR(255)
}
```

---

## 4. Admin Page Hien Tai

Chua co gi — toan bo la planned:
- Biometric detail view: chua co
- Attempt log lookup: chua co
- Dashboard analytics: chua co

---

## 5. Diem Inject De Xuat

### A. Sau khi account opening thanh cong (PRIORITY)
**File:** CustomEKycService.java
**Vi tri:** Sau eKycRepository.save(ekyc) khi status = APPROVED

### B. Endpoint moi nhan log tu App
**File moi:** EKycAttemptLogResource.java
**Endpoint:** POST /ekycs/attempt-log
App goi sau moi lan submit

### C. Sau khi Lotte reject
**File:** LotteEKycService.java
**Vi tri:** Catch block cua lotteEKycNoAsync()

---

## 6. Gaps / Unknowns

| Gap | Ghi chu |
|-----|---------|
| Image storage (MinIO/S3) | Can implement ImageStorageService |
| Pre-Submit API contract | Creator can define DTO chi tiet |
| Privacy/retention | PM can quyet dinh: giu log bao lau? |

---

**Document Status:** Complete | For: Creator | Next Steps: Tao BE Issue + Spec
