# BE Issue: Biometric Log Storage — eKYC Attempt Log

**Issue Type:** Backend Task  
**Feature:** eKYC Biometric Attempt Log Storage  
**Service:** `ekyc-admin` (Java / JHipster)  
**Integration type:** TradeX-native  
**Priority:** High  
**Spec:** `eKYC/Specifications/Biometric_Log_Spec.md`  
**Date:** 2026-07-01

---

## User Story

> As a compliance officer, I want to see the full biometric verification log for each eKYC attempt — including OCR results, liveness scores, fraud detection flags, and face compare outcome — so that I can audit any identity verification event and investigate suspected fraud cases.

---

## Context & Problem

Hiện tại `CustomEKycService.java:211` **xóa** attempt cũ khi user retry (`customEKycRepo.deleteAll(...)`), dẫn đến không có audit trail. `ekyc_ext.raw_data` chỉ lưu JSON blob không query được từng field.

Cần tạo bảng `ekyc_attempt_log` có cấu trúc và endpoint nhận log từ App sau mỗi lần VNPT SDK trả kết quả.

---

## Tasks

### Task 1 — Liquibase Changeset: tạo table `ekyc_attempt_log`

**File:** `src/main/resources/config/liquibase/changelog/<timestamp>_added_entity_EKycAttemptLog.xml`

Tạo changeset tạo bảng `ekyc_attempt_log` theo schema trong Spec. Bao gồm:

- Cột `id` BIGINT AUTO_INCREMENT PRIMARY KEY
- Cột `e_kyc_id` BIGINT NULL (FK → `e_kyc.id`)
- Cột `identifier_id` VARCHAR(50) NOT NULL
- Cột `user_id` VARCHAR(100) NULL
- Cột `vnpt_status_code` INT NULL
- Cột `vnpt_message` VARCHAR(500) NULL
- Cột `server_version` VARCHAR(50) NULL
- Cột `challenge_code` VARCHAR(200) NULL
- Các cột OCR: `ocr_citizen_id`, `ocr_name`, `ocr_card_type`, `ocr_birth_day`, `ocr_gender`, `ocr_nationality`, `ocr_citizen_id_chip`, `ocr_issue_date`, `ocr_valid_date`
- Cột confidence scores: `citizen_id_prob` DOUBLE, `mrz_valid_score` INT, `mrz_prob` DOUBLE
- Cột `tampering_is_legal` VARCHAR(5)
- Cột fraud detection: `id_fake_prob` DOUBLE, `dupplication_warning` BOOLEAN, `dob_fake_warning` BOOLEAN, `address_fake_warning` BOOLEAN
- Cột image quality (front/back): blur score, luminance
- Cột card check (front): `card_front_recapture_result`, `card_front_edited_prob`, `card_front_photocopy_result`
- Cột match front-back: `match_front_back_id`, `match_front_back_name`, `match_front_back_bod`, `match_front_back_valid_date`
- Cột liveness: `liveness_card_front`, `liveness_card_rear`, `liveness_face`, `fake_liveness_prob`, `fake_print_photo_prob`
- Cột `mask_result` VARCHAR(20)
- Cột face compare: `face_compare_msg` VARCHAR(20), `face_compare_prob` DOUBLE
- Cột `attempt_result` VARCHAR(20) NOT NULL
- Cột `vnpt_raw_data` LONGTEXT NULL (full VNPT JSON response — để debug/audit)
- Cột `created_at`, `updated_at` DATETIME NOT NULL
- Index trên: `identifier_id`, `e_kyc_id`, `created_at`, `attempt_result`

Schema đầy đủ: xem `eKYC/Specifications/Biometric_Log_Spec.md` Section 4.

---

### Task 2 — JPA Entity: `EKycAttemptLog.java`

**Package:** `com.nhsv.ekyc.domain`  
**File:** `EKycAttemptLog.java`

Tạo JPA Entity map đến bảng `ekyc_attempt_log`. Bao gồm:

- Annotation `@Entity`, `@Table(name = "ekyc_attempt_log")`
- `@Id @GeneratedValue` cho `id`
- `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "e_kyc_id")` cho FK đến `EKyc`
- Field `identifierId` String, `userId` String
- Tất cả fields VNPT data map theo Spec Section 4
- Enum `AttemptResult { PASS, FAIL, PENDING }` hoặc dùng String — theo convention hiện tại của project
- `@CreationTimestamp` cho `createdAt`, `@UpdateTimestamp` cho `updatedAt`

---

### Task 3 — Repository: `EKycAttemptLogRepository.java`

**Package:** `com.nhsv.ekyc.repository`  
**File:** `EKycAttemptLogRepository.java`

```java
public interface EKycAttemptLogRepository 
    extends JpaRepository<EKycAttemptLog, Long>, JpaSpecificationExecutor<EKycAttemptLog> {

    // Tìm theo identifierId (cho admin search)
    Page<EKycAttemptLog> findByIdentifierIdOrderByCreatedAtDesc(
        String identifierId, Pageable pageable);

    // Link e_kyc_id sau khi APPROVED
    @Modifying
    @Query("UPDATE EKycAttemptLog a SET a.eKyc.id = :ekycId WHERE a.id = :attemptLogId")
    void updateEkycId(@Param("attemptLogId") Long attemptLogId, @Param("ekycId") Long ekycId);
}
```

---

### Task 4 — Service: `EKycAttemptLogService.java`

**Package:** `com.nhsv.ekyc.service`  
**File:** `EKycAttemptLogService.java`

Implement các method sau:

#### 4a. `saveAttemptLog(EKycAttemptLogRequest request, String userId)`

- Parse request DTO → `EKycAttemptLog` entity
- Tính `attemptResult`:
  - `PASS` nếu `livenessCardFront`, `livenessCardRear`, `livenessFace`, `maskResult` đều = `"success"` VÀ `faceCompareMsg` = `"MATCH"`
  - `FAIL` nếu bất kỳ field nào = `"failure"` hoặc `faceCompareMsg` = `"NOMATCH"`
  - `PENDING` nếu thiếu data (null)
- Set `userId` từ tham số (extracted từ JWT token ở Resource layer)
- Gọi `repository.save()` → return `id`

#### 4b. `updateFinalEkycId(Long attemptLogId, Long ekycId)`

- Gọi `repository.updateEkycId(attemptLogId, ekycId)`
- Dùng ở Task 6 sau khi Lotte APPROVED

#### 4c. `searchAttempts(EKycAttemptSearchRequest request, Pageable pageable)`

- Build JPA Specification từ filter params: `identifierId`, `attemptResult`, `fromDate`, `toDate`, `hasEkycId`
- Return `Page<EKycAttemptLogDTO>`

#### 4d. `getAttemptDetail(Long id)`

- `repository.findById(id)` → throw `ObjectNotFoundException` nếu không tìm thấy
- Return full DTO

---

### Task 5 — REST Resource: `EKycAttemptLogResource.java`

**Package:** `com.nhsv.ekyc.web.rest`  
**File:** `EKycAttemptLogResource.java`

Implement endpoint `POST /ekycs/attempt-log`:

```java
@RestController
@RequestMapping("/ekycs")
public class EKycAttemptLogResource {

    @PostMapping("/attempt-log")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EKycAttemptLogIdResponse> saveAttemptLog(
        @Valid @RequestBody EKycAttemptLogRequest request,
        @CurrentUser UserPrincipal currentUser
    ) {
        // Validate: identifierId required
        if (StringUtils.isBlank(request.getIdentifierId())) {
            throw new InvalidParameterException("identifierId");
        }
        Long id = ekycAttemptLogService.saveAttemptLog(request, currentUser.getUsername());
        return ResponseEntity.ok(new EKycAttemptLogIdResponse(id));
    }
}
```

**Request DTO:** `EKycAttemptLogRequest.java` — map theo Spec Section 5.  
**Response DTO:** `EKycAttemptLogIdResponse.java` — `{ Long id }`.

---

### Task 6 — Modify `CustomEKycService.java`: link attemptLog sau khi APPROVED

**File:** `CustomEKycService.java`  
**Điểm inject:** Sau `eKycRepository.save(ekyc)` khi `status = APPROVED`

Thêm logic sau khi lưu eKyc thành công:

```java
// Hiện tại (dòng ~211):
eKyc = eKycRepository.save(ekyc);
// ...

// THÊM MỚI: Link attemptLog với ekycId vừa tạo
// attemptLogId được truyền vào từ request context (App gửi kèm attemptLogId)
if (request.getAttemptLogId() != null) {
    ekycAttemptLogService.updateFinalEkycId(request.getAttemptLogId(), eKyc.getId());
}
```

**Lưu ý:**
- App cần gửi `attemptLogId` (nhận từ response của `POST /ekycs/attempt-log`) vào body của `POST /lotte/ekycs`.
- Cần thêm field `attemptLogId` vào request model của `POST /lotte/ekycs` (Long, optional).
- Nếu `attemptLogId` null → bỏ qua link, không throw error.

---

### Task 7 — Admin REST: `GET /api/admin/ekyc/attempts/search` và `/{id}`

**Package:** `com.nhsv.ekyc.web.rest.admin`  
**File:** `AdminEKycAttemptLogResource.java`

```java
@RestController
@RequestMapping("/api/admin/ekyc/attempts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEKycAttemptLogResource {

    @GetMapping("/search")
    public ResponseEntity<EKycAttemptPageResponse> search(
        @RequestParam(required = false) String identifierId,
        @RequestParam(required = false) String attemptResult,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) Boolean hasEkycId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // Build request, gọi service.searchAttempts()
        // Return EKycAttemptPageResponse { totalCount, attempts }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EKycAttemptDetailResponse> detail(@PathVariable Long id) {
        // Gọi service.getAttemptDetail(id)
        // Nếu không tìm thấy → 404 OBJECT_NOT_FOUND
    }
}
```

Query params, response format: xem Spec Section 6 và Section 7.

---

## Acceptance Criteria

- [ ] Bảng `ekyc_attempt_log` được tạo qua Liquibase changeset — migration chạy thành công trên dev/staging.
- [ ] `POST /ekycs/attempt-log` nhận request, lưu DB, trả về `{ id }` — HTTP 200.
- [ ] `POST /ekycs/attempt-log` thiếu `identifierId` → trả `400 INVALID_PARAMETER`.
- [ ] `attempt_result` được tính đúng logic: `PASS` / `FAIL` / `PENDING`.
- [ ] Sau khi Lotte APPROVED, `ekyc_attempt_log.e_kyc_id` được update đúng.
- [ ] `GET /api/admin/ekyc/attempts/search` trả về danh sách có pagination (`totalCount`, `attempts[]`).
- [ ] `GET /api/admin/ekyc/attempts/search` filter theo `identifierId`, `attemptResult`, `fromDate`, `toDate` hoạt động đúng.
- [ ] `GET /api/admin/ekyc/attempts/{id}` trả về full detail — HTTP 200.
- [ ] `GET /api/admin/ekyc/attempts/{id}` với ID không tồn tại → `404 OBJECT_NOT_FOUND`.
- [ ] Admin endpoints yêu cầu `ROLE_ADMIN` — user thường nhận `403 FORBIDDEN`.
- [ ] Mỗi retry của user tạo row mới, không xóa row cũ.
- [ ] Bảng `ekyc_ext` hiện tại không bị thay đổi hoặc xóa.

---

## Implementation Notes

- **JHipster pattern:** Theo convention hiện tại của project — nếu project dùng MapStruct thì tạo Mapper tương ứng.
- **No image storage:** Field `imgFront` / `imgBack` (Base64 images từ VNPT) **không** lưu vào DB — chỉ lưu metadata/scores. Image storage là scope riêng (cần `ImageStorageService` + MinIO/S3).
- **Privacy:** PM sẽ quyết định retention policy (bao lâu giữ log). Chưa implement auto-purge trong scope này.
- **`ekyc_ext` không xóa:** Giữ nguyên bảng cũ. `ekyc_attempt_log` là bổ sung cấu trúc, không thay thế.
- **Partial data OK:** App có thể gửi partial data (nhiều field null) — BE vẫn lưu, không reject.

---

**Document Status:** ✅ Complete | For: BE Dev (ekyc-admin team) | Next Steps: Dev implement Task 1-7 theo thứ tự, QA verify theo Acceptance Criteria, sau đó Admin UI team tạo FE issue riêng cho admin page
