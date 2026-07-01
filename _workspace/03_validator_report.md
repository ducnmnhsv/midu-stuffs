# Validator Report — eKYC Biometric Log

**Date:** 2026-07-01
**Validator:** tradex-validator
**Pipeline stage:** Phase 3 — Convention Check & Finalize

---

## Spec file: `eKYC/Specifications/Biometric_Log_Spec.md`

**Result:** PASS_WITH_WARNINGS

### Checks

| Check | Result | Ghi chú |
|-------|--------|---------|
| File naming (PascalCase + underscore) | PASS | `Biometric_Log_Spec.md` đúng convention |
| API fields camelCase TradeX | PASS | Tất cả request/response fields dùng camelCase TradeX (`identifierId`, `vnptStatusCode`, v.v.) — không có Lotte field names |
| DB table/column name snake_case | PASS | `ekyc_attempt_log`, tất cả cột snake_case nhất quán |
| Integration type khai báo rõ | PASS | `TradeX-native (internal DB only — không qua Lotte/Core)` ở đầu file |
| Mutation response `{ id }` | PASS | `POST /ekycs/attempt-log` trả `{ "id": 1042 }` — đúng chuẩn TradeX-native |
| Query response có envelope | PASS | `GET /api/admin/ekyc/attempts/search` có `{ totalCount, attempts[] }` |
| Error codes SCREAMING_SNAKE_CASE | PASS | `INVALID_PARAMETER`, `OBJECT_NOT_FOUND`, `TOKEN_EXPIRED`, `FORBIDDEN`, `INTERNAL_SERVER_ERROR` |
| Footer C5 format | PASS | `Document Status: ✅ Complete \| For: BE Dev (ekyc-admin team) \| Next Steps: ...` |
| URL camelCase (TradeX convention) | PASS | `/ekycs/attempt-log`, `/api/admin/ekyc/attempts/search` — đúng pattern JHipster của ekyc-admin |
| `identifierId` required trong POST | PASS | Ghi rõ **Y** trong Request Fields table, có note riêng |
| Admin API filter params hợp lý | PASS | `identifierId`, `attemptResult`, `fromDate`, `toDate`, `hasEkycId`, `page`, `size` — đầy đủ |
| VNPT fields tách thành cột riêng | PASS | 30+ cột riêng, INDEX trên `identifier_id`, `attempt_result`, `e_kyc_id`, `created_at` |
| Table of Contents anchor typo | FIXED | Sửa `ekcyattemptssearch` → `ekyattemptssearch` trong 2 anchor links |
| `vnpt_raw_data LONGTEXT` trong schema | WARNING | Không có cột này trong `ekyc_attempt_log` — thiết kế intentional: raw blob giữ ở `ekyc_ext.raw_data` (không xóa), bảng mới chỉ lưu structured fields. Justify rõ ở Section 2 và 9. Hợp lệ nhưng khác checklist validator ban đầu — PM confirm nếu cần thêm redundant raw column. |

### Fixes đã áp dụng

- **Typo Table of Contents (dòng 21-22):** Sửa anchor links từ `#...ekcyattemptssearch` / `#...ekcyattemptsid` thành đúng spelling `eky`.

---

## Issue file: `eKYC/Issues/BE_Issue_Biometric_Log_Storage.md`

**Result:** PASS

### Checks

| Check | Result | Ghi chú |
|-------|--------|---------|
| File naming | PASS | `BE_Issue_Biometric_Log_Storage.md` — PascalCase + underscore |
| Integration type khai báo | PASS | `TradeX-native` ở header |
| Footer C5 format | PASS | Đúng format — đầy đủ Status/For/Next Steps |
| Tasks khớp spec | PASS | Task 1-7 cover đầy đủ: Liquibase, Entity, Repository, Service, Resource, CustomEKycService hook, Admin REST |
| Acceptance Criteria rõ ràng | PASS | 12 criteria cụ thể, testable, khớp từng task |
| Code snippets Java hợp lệ | PASS | Repository interface, Resource controller, CustomEKycService hook — đúng JHipster pattern |
| No Lotte field names | PASS | Không reference Lotte fields (`acnt_no`, v.v.) |
| Folder placement | PASS | `eKYC/Issues/` — đúng file routing |
| `attemptLogId` linking flow | PASS | Task 6 mô tả rõ: App gửi `attemptLogId` vào `POST /lotte/ekycs`, BE link `e_kyc_id` sau APPROVED |

---

## Overall: PASS_WITH_WARNINGS

Cả 2 files đạt chuẩn convention TradeX. Warning duy nhất về `vnpt_raw_data` là thiết kế intentional và được justify rõ trong spec — không phải lỗi convention. Files ready for handoff to BE Dev.

---

## Files đã lưu

- `eKYC/Specifications/Biometric_Log_Spec.md` — PASS_WITH_WARNINGS (1 typo anchor đã fix)
- `eKYC/Issues/BE_Issue_Biometric_Log_Storage.md` — PASS (không thay đổi)
