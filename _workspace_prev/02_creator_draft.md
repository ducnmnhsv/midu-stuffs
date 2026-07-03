# Creator Draft Summary — eKYC Biometric Log Storage

**Task:** #2 — Tạo BE Issue + Spec document cho Biometric Log Storage
**Owner:** creator
**Status:** Done

---

## Files created

1. `eKYC/Specifications/Biometric_Log_Spec.md`
2. `eKYC/Issues/BE_Issue_Biometric_Log_Storage.md`

---

## Spec: Biometric_Log_Spec.md

Cấu trúc 9 sections + footer:

- **1. Overview & Business Context** — gap hiện tại (no audit trail, raw blob), business goals (audit, compliance, admin visibility, fraud review).
- **2. Current State — Gap Analysis** — bảng so sánh hiện tại vs cần làm cho từng component.
- **3. eKYC Flow with Biometric Log** — ASCII flow diagram từ App → VNPT SDK → `POST /ekycs/attempt-log` → DB → `POST /lotte/ekycs` → `updateFinalEkycId`.
- **4. DB Schema** — DDL đầy đủ cho `ekyc_attempt_log` (35+ cột, indexes), quan hệ với `e_kyc` (1:n, nullable FK) và `ekyc_ext` (vẫn giữ).
- **5. API: POST /ekycs/attempt-log** — TradeX-native, request body JSON (nested objects: ocr, scores, fraud, imageQuality, cardCheck, matchFrontBack, liveness, faceCompare), response `{ id }`, auto-populated fields (userId từ JWT, attempt_result logic).
- **6. API: GET /api/admin/ekyc/attempts/search** — query params (identifierId, attemptResult, fromDate, toDate, hasEkycId, page, size), response `{ totalCount, attempts[] }` với 25+ fields per item.
- **7. API: GET /api/admin/ekyc/attempts/{id}** — full detail response, 404 OBJECT_NOT_FOUND.
- **8. Error Handling** — bảng 5 scenarios (400, 404, 401, 403, 500).
- **9. Business Rules & Validation** — 6 rules (identifierId required, lưu kể cả VNPT fail, link ekycId, attempt_result logic, không xóa cũ, giữ ekyc_ext).
- **Footer C5:** `Document Status: ✅ Complete | For: BE Dev (ekyc-admin team) | Next Steps: ...`

---

## Issue: BE_Issue_Biometric_Log_Storage.md

Cấu trúc: User Story + Context + 7 Tasks + Acceptance Criteria + Implementation Notes + footer:

- **User Story:** Compliance officer muốn xem full biometric log (OCR, liveness, fraud, face compare) để audit.
- **Task 1** — Liquibase changeset: tạo `ekyc_attempt_log` table với đầy đủ cột + indexes.
- **Task 2** — JPA Entity `EKycAttemptLog.java`: annotations, FK relationship, all fields, AttemptResult enum.
- **Task 3** — Repository `EKycAttemptLogRepository.java`: `findByIdentifierId`, `updateEkycId` (@Modifying query), extends JpaSpecificationExecutor.
- **Task 4** — Service `EKycAttemptLogService.java`: 4 methods: `saveAttemptLog` (parse + attempt_result logic), `updateFinalEkycId`, `searchAttempts` (JPA Spec), `getAttemptDetail`.
- **Task 5** — REST Resource `EKycAttemptLogResource.java`: `POST /ekycs/attempt-log`, `@PreAuthorize("isAuthenticated()")`, validate identifierId, return `{ id }`.
- **Task 6** — Modify `CustomEKycService.java`: sau khi APPROVED, gọi `updateFinalEkycId(attemptLogId, ekycId)`; App cần gửi `attemptLogId` trong body `POST /lotte/ekycs`.
- **Task 7** — Admin REST `AdminEKycAttemptLogResource.java`: `GET /api/admin/ekyc/attempts/search` + `/{id}`, `@PreAuthorize("hasRole('ADMIN')")`.
- **Acceptance Criteria:** 12 criteria bao gồm DB migration, API responses, attempt_result logic, link ekycId, pagination, filter, 404, RBAC, no-delete-on-retry, ekyc_ext unchanged.
- **Implementation Notes:** JHipster pattern, no image storage in scope, privacy retention TBD, ekyc_ext không xóa, partial data OK.
- **Footer C5:** `Document Status: ✅ Complete | For: BE Dev (ekyc-admin team) | Next Steps: ...`

---

## Key design decisions

- **Integration type:** TradeX-native (không qua Lotte/Core) — lưu thẳng vào DB ekyc-admin.
- **Only `identifierId` required** trong request — các field VNPT đều optional vì App có thể có partial data.
- **`attempt_result` auto-calculated** phía BE (PASS/FAIL/PENDING), App không cần tính.
- **Không xóa `ekyc_ext`** — giữ backward compatibility, `ekyc_attempt_log` bổ sung cấu trúc.
- **Image (Base64) không lưu vào DB** — out of scope, cần ImageStorageService riêng.
- **`attemptLogId` flow:** App gọi `POST /ekycs/attempt-log` trước → nhận `id` → gửi `attemptLogId` vào body `POST /lotte/ekycs` → BE link sau khi APPROVED.
