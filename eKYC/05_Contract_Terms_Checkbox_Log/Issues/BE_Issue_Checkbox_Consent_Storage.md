# [BE] eKYC — Lưu timestamp đồng ý điều khoản qua attempt-log (Compliance)

**Feature:** eKYC / 05_Contract_Terms_Checkbox_Log
**Type:** BE — Compliance (extension of sub-feature 01)
**Priority:** Medium
**Service:** `ekyc-admin` (Java / JHipster)
**Mục đích:** Reuse `POST /ekycs/attempt-log` để nhận và lưu `terms_agreed_at` — không tạo endpoint mới

---

## Bối cảnh

Thay vì tạo endpoint riêng, sub-feature 05 mở rộng `POST /ekycs/attempt-log` (đã build trong sub-feature 01) để nhận thêm trường `termsAgreedAt`. App gọi endpoint này thêm một lần từ màn hình Xác nhận điều khoản — chỉ gửi `identifierId` + `termsAgreedAt`, không kèm dữ liệu VNPT.

**Lưu ý:** Các task trong issue này phụ thuộc hoàn toàn vào sub-feature 01. Implement sau khi sub-feature 01 hoàn thành.

---

## Yêu cầu

### Task 1 — DB: Thêm cột vào `ekyc_attempt_log`

Thêm vào Liquibase changeset **hiện có** của sub-feature 01 (hoặc tạo changeset mới):

```sql
ALTER TABLE ekyc_attempt_log
  ADD COLUMN terms_agreed_at  DATETIME    NULL         COMMENT 'Thời điểm khách hàng tick đồng ý điều khoản (UTC)',
  ADD COLUMN terms_version    VARCHAR(20) DEFAULT 'v1' COMMENT 'Phiên bản nội dung điều khoản';
```

Cũng cập nhật schema SQL trong `Backend_Spec.md` Section 0.1 (đã được cập nhật — xem file).

---

### Task 2 — DTO: Nhận `termsAgreedAt` trong request

Trong DTO của `POST /ekycs/attempt-log`, thêm field optional:

```java
@JsonProperty("termsAgreedAt")
private String termsAgreedAt;  // ISO 8601 UTC — nullable, không break client cũ
```

---

### Task 3 — Service: Xử lý terms-only request

`POST /ekycs/attempt-log` cần xử lý 2 trường hợp:

| Trường hợp | Payload | Hành động |
|-----------|---------|-----------|
| VNPT log (existing) | `identifierId` + `outcome` + toàn bộ VNPT data | Insert record mới |
| Terms consent (mới) | Chỉ `identifierId` + `termsAgreedAt` | Update `terms_agreed_at` trên record hiện có |

**Logic phân biệt:** Nếu request không có `outcome` (null/absent) → đây là terms-consent update.

```java
if (request.getOutcome() == null && request.getTermsAgreedAt() != null) {
    // Terms-consent update path
    EkycAttemptLog latest = attemptLogRepo
        .findTopByIdentifierIdOrderByAttemptAtDesc(request.getIdentifierId());
    if (latest != null) {
        latest.setTermsAgreedAt(parseUtc(request.getTermsAgreedAt()));
        latest.setTermsVersion("v1");
        attemptLogRepo.save(latest);
    }
} else {
    // Insert new record (existing path)
    ...
}
```

---

### Task 4 — Entity: Cập nhật JPA

```java
// EkycAttemptLog.java
@Column(name = "terms_agreed_at")
private LocalDateTime termsAgreedAt;

@Column(name = "terms_version", length = 20)
private String termsVersion;
```

---

## Acceptance Criteria

- [ ] Migration chạy thành công, schema không ảnh hưởng dữ liệu cũ
- [ ] `POST /ekycs/attempt-log` với `{identifierId, termsAgreedAt}` (không có `outcome`) → update `terms_agreed_at` trên record cuối của identifierId
- [ ] Record cũ (không có terms call) có `terms_agreed_at = NULL` — không lỗi
- [ ] Timestamp lưu đúng UTC (verify: tick 14:00 ICT → DB = 07:00 UTC)
- [ ] Insert path (VNPT log) không bị ảnh hưởng

---

## Files cần thay đổi

| File | Thay đổi |
|------|----------|
| Liquibase changelog (sub-feature 01) | Thêm 2 cột `terms_agreed_at`, `terms_version` |
| `EkycAttemptLog.java` | Thêm 2 JPA field |
| `EkycAttemptLogDTO.java` | Thêm `termsAgreedAt` request field |
| `EkycAttemptLogService.java` | Xử lý terms-consent update path |

---

## Liên kết

- **FE Issue:** [FE_Issue_Checkbox_Analytics_Log.md](FE_Issue_Checkbox_Analytics_Log.md)
- **Sub-feature 01 BE Spec:** [01_Biometric_Attempt_Log/Specifications/Backend_Spec.md](../../01_Biometric_Attempt_Log/Specifications/Backend_Spec.md)
- **PRD:** [Planning/PRD_eKYC_v2.md](../../Planning/PRD_eKYC_v2.md) — Section 4.9

---

Document Status: 📋 Ready for Dev | For: BE Developer | Next Steps: Implement sau khi sub-feature 01 Task 1–3 hoàn thành
