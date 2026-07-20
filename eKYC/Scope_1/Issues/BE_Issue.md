# BE Issue: eKYC Attempt Log & Compliance Journey Log Storage

**Issue Type:** Backend Task
**Feature:** Sub-feature 01 (Biometric Attempt Log) + Sub-feature 07 (Compliance Journey Log) — gộp chung 1 issue vì cùng service, cùng đợt implement Scope 1
**Service:** `ekyc-admin` (Java / JHipster)
**Integration type:** TradeX-native
**Priority:** High
**Spec:** `../Specifications/BE_Spec.md` (Phần A = Sub-feature 01, Phần B = Sub-feature 07)
**Date:** 2026-07-16 (gộp 2 issue — 01 đã có sẵn từ 2026-07-08, 07 mới tạo lần này); spec 2 file gộp thành 1 ngày 2026-07-20

---

## User Story

> As a compliance officer, I want to see the full biometric verification log for each eKYC attempt — including OCR results, liveness scores, fraud detection flags, face compare outcome, and the raw SDK response — so that I can audit any identity verification event and investigate suspected fraud cases, even for fields not yet mapped to a dedicated column.

> As a compliance/audit officer, I want a complete, screen-by-screen record of every successful account-opening journey — including fields that only ever existed in the App's local state (occupation, tax code, full terms-consent text) — so that I can reconstruct exactly what the customer saw and confirmed if a dispute or regulatory audit arises.

---

## Context & Problem

**Sub-feature 01:** Hiện tại `CustomEKycService.java:211` **xóa** attempt cũ khi user retry (`customEKycRepo.deleteAll(...)`), dẫn đến không có audit trail. `ekyc_ext.raw_data` chỉ lưu JSON blob không query được từng field.

**Sub-feature 07:** Không có bất kỳ bản ghi nào cho các bước trung gian của hành trình mở TK. Nhiều field compliance cần lưu (`occupation`, `tax_cd`, toàn văn `isAgree`) chỉ tồn tại ở local state App, không đi qua API nghiệp vụ nào cho tới lúc submit cuối — nếu không log riêng tại từng màn hình, dữ liệu này mất vĩnh viễn và không thể tái dựng khi có tranh chấp.

Cả 2 sub-feature dùng chung service `ekyc-admin` nhưng **2 bảng độc lập** (`ekyc_attempt_log` append-only vs `ekyc_journey_log` có purge sau 8h nếu bỏ dở) — không gộp schema vì chính sách retention xung đột nhau (xem README Review Log 2026-07-06b).

---

# Phần A — Sub-feature 01: Biometric Attempt Log

> ⚠️ **Schema đã được đối chiếu lại (2026-07-01)** để khớp với model `outcome` / `failure_step` dùng xuyên suốt PRD, `Dashboard_API_Spec.md`, và `FE_Issue_Admin_Attempt_History.md`. Bản trước của issue này dùng model `attempt_result` (PASS/FAIL/PENDING) riêng biệt — đã bị loại bỏ vì không tương thích với các API khác trong hệ thống.

> ⚠️ **Schema đã được đối chiếu lại lần 2 (2026-07-08)** với sample log thực tế do dev gửi (OCR CCCD 2 mặt VNPT + liveness + face compare). Phát hiện vài chỗ bản v1 map sai tên field JSON gốc, và vài field VNPT trả về nhưng spec bỏ sót — chi tiết đầy đủ xem `BE_Spec.md` (Phần A) Section 0.4/0.5 (các dòng đánh dấu ⚠️) và sample thực tế đã ẩn danh ở Section 0.6. Tóm tắt:
> - **Sửa:** `vnpt_citizen_id` phải lấy từ `object.id` (không phải `object.citizenId` — field không tồn tại); `vnpt_citizen_id_chip` lấy từ `object.dict_qr.SoCCCD` (không phải `object.citizenIdChip`); cột `vnpt_match_valid_date` đổi thành `vnpt_match_sex` (field `match_valid_date` không tồn tại); `fake_liveness_prob`/`fake_print_photo_prob` tách thành 6 cột riêng theo mặt trước/sau vì 2 field này nằm ở `LOG_LIVENESS_CARD_FRONT`/`REAR`, không phải `LOG_LIVENESS_FACE` như spec v1 giả định.
> - **Thêm mới:** `mrz_line3` (MRZ CCCD gắn chip có 3 dòng, không phải 2), `vnpt_old_citizen_id` (CMND cũ), `vnpt_qr_match_summary` (đối chiếu QR chip vs OCR), `face_swapping` detection (2 cột, theo mặt trước/sau), `multiple_faces` detection (2 cột: face compare + face liveness), `face_compare_match_warning`, và 6 cột VNPT logID (`vnpt_ocr_log_id`, `vnpt_card_liveness_front_log_id`, `vnpt_card_liveness_rear_log_id`, `vnpt_face_liveness_log_id`, `vnpt_face_compare_log_id`, `vnpt_face_mask_log_id`) — các logID này App **đã có sẵn** trong luồng `/lotte/ekycs` hiện tại, chỉ cần gửi kèm.

## Task 1 — Liquibase Changeset: tạo table `ekyc_attempt_log`

**File:** `src/main/resources/config/liquibase/changelog/20260524000001_add_ekyc_attempt_log.xml`

Tạo changeset tạo bảng `ekyc_attempt_log` theo schema đầy đủ trong `BE_Spec.md` (Phần A) Section 0.1 + 0.3. Bao gồm:

- `id` BIGINT AUTO_INCREMENT PRIMARY KEY
- `identifier_id` VARCHAR(20) NOT NULL, `phone_no` VARCHAR(20)
- `attempt_number` INT NOT NULL, `attempt_at` DATETIME NOT NULL
- `final_ekyc_id` BIGINT (FK → `e_kyc.id`, `ON DELETE SET NULL`)
- `outcome` VARCHAR(30) NOT NULL, `failure_step` VARCHAR(50), `failure_code` VARCHAR(100), `failure_message` VARCHAR(500)
- VNPT OCR: `vnpt_status_code`, `vnpt_citizen_id` (← `object.id`, KHÔNG phải `object.citizenId`), `vnpt_old_citizen_id` (← `object.citizen_id`, mới), `vnpt_name`, `vnpt_card_type`, `vnpt_citizen_id_prob`, `vnpt_mrz_valid_score`
- Fraud detection: `vnpt_is_tampered`, `vnpt_id_fake_warning`, `vnpt_id_fake_prob`, `vnpt_duplication_warning`, `vnpt_dob_fake_warning`, `vnpt_address_fake_warning`, `vnpt_issuedate_fake_warning`, `vnpt_name_fake_warning`
- Card integrity (front/back): `vnpt_front_recaptured`, `vnpt_front_edited_prob`, `vnpt_front_photocopied`, `vnpt_back_recaptured`, `vnpt_back_edited_prob`, `vnpt_back_photocopied`
- Image quality (front/back): `vnpt_front_blur_score`, `vnpt_front_luminance_score`, `vnpt_back_blur_score`, `vnpt_back_luminance_score`
- Cross-validation: `vnpt_match_id`, `vnpt_match_name`, `vnpt_match_bod`, `vnpt_match_sex` (← `match_front_back.match_sex` — **đổi tên** từ `vnpt_match_valid_date`, field đó không tồn tại)
- Extended OCR: `vnpt_nationality`, `vnpt_citizen_id_chip` (← `object.dict_qr.SoCCCD`, KHÔNG phải `object.citizenIdChip`)
- **QR cross-check (mới):** `vnpt_qr_match_summary` (PASS/FAIL/SKIPPED — BE tự tính từ `object.match_qr.*`)
- MRZ raw: `mrz_line1`, `mrz_line2`, `mrz_line3` (mới — CCCD gắn chip trả 3 dòng MRZ, không phải 2), `mrz_overall_prob`
- MRZ cross-check: `mrz_cross_check`, `mrz_check_id`, `mrz_check_dob`, `mrz_check_gender`, `mrz_check_expiry`
- Liveness (SDK): `liveness_card_front_result`, `liveness_card_rear_result`, `liveness_face_result`, `face_mask_result`
- **Card liveness fraud detail (đổi cấu trúc — tách theo mặt):** `liveness_card_front_fake_prob`, `liveness_card_front_fake_print_prob`, `liveness_card_front_face_swapping` (mới), `liveness_card_rear_fake_prob`, `liveness_card_rear_fake_print_prob`, `liveness_card_rear_face_swapping` (mới), `liveness_face_multiple_faces` (mới) — **thay thế** 2 cột cũ `fake_liveness_prob`/`fake_print_photo_prob` (spec v1 gán nhầm nguồn `LOG_LIVENESS_FACE`, thực tế 2 field này nằm ở `LOG_LIVENESS_CARD_FRONT`/`REAR`)
- Face compare: `face_compare_msg`, `face_compare_prob` (thang 0-100), `face_compare_match_warning` (mới), `face_compare_multiple_faces` (mới)
- **VNPT log IDs (mới — tra soát chéo khi audit/tranh chấp):** `vnpt_ocr_log_id`, `vnpt_card_liveness_front_log_id`, `vnpt_card_liveness_rear_log_id`, `vnpt_face_liveness_log_id`, `vnpt_face_compare_log_id`, `vnpt_face_mask_log_id` — App đã có sẵn các ID này (`ocrLogId`, `cardLivenessLogId`,...) trong luồng `/lotte/ekycs`
- Image storage: `image_front_url` VARCHAR(500), `image_back_url` VARCHAR(500)
- **Raw audit (2 cột):**
  - `vnpt_raw_data` LONGTEXT — raw JSON `LOG_OCR`
  - `sdk_raw_logs` LONGTEXT — raw JSON gộp 7 SDK log key còn lại
- **Terms consent (sub-feature 05 — 2 cột):**
  - `terms_agreed_at` DATETIME NULL — timestamp UTC khi khách hàng tick checkbox "Đồng ý điều khoản"
  - `terms_version` VARCHAR(20) DEFAULT 'v1' — phiên bản nội dung điều khoản
- Index: `identifier_id`, `phone_no`, `attempt_at`, `outcome`, `final_ekyc_id`

Schema + Liquibase XML đầy đủ: xem `BE_Spec.md` (Phần A) Section 0.1 và 0.3.

Kèm changeset thứ 2: thêm 2 cột `total_attempts`, `first_attempt_at` vào `e_kyc` (xem Section 0.2).

---

## Task 2 — JPA Entity: `EKycAttemptLog.java`

**Package:** `com.nhsv.ekyc.domain`
**File:** `EKycAttemptLog.java`

Copy nguyên cấu trúc field từ `BE_Spec.md` (Phần A) Section 1 (đã viết sẵn đầy đủ). Chú ý:

- `@Lob @Column(name = "vnpt_raw_data") private String vnptRawData;`
- `@Lob @Column(name = "sdk_raw_logs") private String sdkRawLogs;` — **field mới**, lưu nguyên văn, không transform.
- `outcome` là String, không phải Enum trong bản spec hiện tại (theo convention project nếu khác thì điều chỉnh).

---

## Task 3 — Repository: `EKycAttemptLogRepository.java`

**Package:** `com.nhsv.ekyc.repository`
**File:** `EKycAttemptLogRepository.java`

Implement theo `BE_Spec.md` (Phần A) Section 3:

```java
@Repository
public interface EKycAttemptLogRepository extends JpaRepository<EKycAttemptLog, Long> {

    List<EKycAttemptLog> findByIdentifierIdOrderByAttemptNumberAsc(String identifierId);

    List<EKycAttemptLog> findByPhoneNoOrderByAttemptNumberAsc(String phoneNo);

    int countByIdentifierId(String identifierId);

    @Query("SELECT MIN(a.attemptAt) FROM EKycAttemptLog a WHERE a.identifierId = :identifierId")
    ZonedDateTime findFirstAttemptAt(@Param("identifierId") String identifierId);

    @Modifying
    @Query("UPDATE EKycAttemptLog a SET a.finalEkycId = :ekycId WHERE a.identifierId = :identifierId")
    void updateFinalEkycId(@Param("identifierId") String identifierId, @Param("ekycId") Long ekycId);

    @Modifying
    @Query("""
        UPDATE EKycAttemptLog a
        SET a.outcome = :outcome, a.failureStep = :step,
            a.failureCode = :code, a.failureMessage = :message
        WHERE a.identifierId = :identifierId AND a.attemptNumber = :attemptNumber
        """)
    void updateOutcome(@Param("identifierId") String identifierId, @Param("attemptNumber") int attemptNumber,
                        @Param("outcome") String outcome, @Param("step") String step,
                        @Param("code") String code, @Param("message") String message);
}
```

---

## Task 4 — Service: `EKycAttemptLogService.java`

**Package:** `com.nhsv.ekyc.service`
**File:** `EKycAttemptLogService.java`

Implement theo `BE_Spec.md` (Phần A) Section 5 + Section 7.4 (BE Processing Logic). Method chính: `processAttemptLog(EKycAttemptLogRequest req, String identifierId, String phoneNo)`.

#### 4a. Tính `attemptNumber`

```java
int attemptNumber = eKycAttemptLogRepository.countByIdentifierId(identifierId) + 1;
```

#### 4b. Parse `vnptRawData` (nếu có) → extract toàn bộ VNPT fields

Tái dùng method `buildAttemptLog()` — xem `BE_Spec.md` (Phần A) Section 2.3. Set cả `log.setVnptRawData(req.getVnptRawData())` (as-is).

#### 4c. Lưu `sdkRawLogs` — **KHÔNG parse, KHÔNG transform**

```java
if (StringUtils.isNotBlank(req.getSdkRawLogs())) {
    log.setSdkRawLogs(req.getSdkRawLogs());
}
```

> Mục đích của field này là audit — không cần BE hiểu cấu trúc bên trong. Lưu nguyên văn App gửi lên.

#### 4d. Map MRZ + liveness + face compare fields từ request

Theo `BE_Spec.md` (Phần A) Section 5 (đã có code mẫu đầy đủ).

#### 4e. Upload ảnh (nếu có `imageFrontBase64` / `imageBackBase64`)

Gọi `imageStorageService.uploadBase64(...)` — xem Task 5 (Image Storage, sub-feature 06, **out of scope** issue này — xem Implementation Notes).

#### 4f. `updateFinalEkycId(String identifierId, Long ekycId)`

Gọi từ `CustomEKycService` (Task 6) sau khi Lotte APPROVED.

---

## Task 5 — REST Resource: `EKycAttemptLogResource.java`

**Package:** `com.nhsv.ekyc.web.rest`
**File:** `EKycAttemptLogResource.java`

Implement endpoint `POST /ekycs/attempt-log` theo `BE_Spec.md` (Phần A) Section 5 (REST Controller) + Section 7 (API contract đầy đủ: request body, validation rules, response, error codes).

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EKycAttemptLogResource {

    private final EKycAttemptLogService eKycAttemptLogService;

    @PostMapping("/ekycs/attempt-log")
    @ResponseStatus(HttpStatus.CREATED)
    public EKycAttemptLogResponseDTO logAttempt(
            @Valid @RequestBody EKycAttemptLogRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String identifierId = extractIdentifierId(userDetails);
        String phoneNo      = extractPhoneNo(userDetails);
        return eKycAttemptLogService.processAttemptLog(req, identifierId, phoneNo);
    }
}
```

**Request DTO:** `EKycAttemptLogRequest.java` — map theo `BE_Spec.md` (Phần A) Section 5 DTO + Section 7.2 (bao gồm `vnptRawData` và **field mới `sdkRawLogs`**).
**Response DTO:** `EKycAttemptLogResponseDTO` — `{ Long attemptId }`.

**Validation rules** (Section 7.3):
- `outcome` bắt buộc.
- `failureStep` bắt buộc khi `outcome` không phải `SUCCESS`/`USER_ABANDONED`.
- `imageFrontBase64`/`imageBackBase64` bắt buộc khi `failureStep = VNPT_OCR`.
- `sdkRawLogs` bắt buộc khi App đã nhận kết quả từ bất kỳ SDK log key nào (liveness/mask/compare) — kể cả khi outcome cuối là `SUCCESS`.

---

## Task 6 — Modify `CustomEKycService.java` / `LotteEKycService.java`

**Điểm inject 1 — Sau khi Lotte reject** (`LotteEKycService.java`, catch block của `lotteEKycNoAsync()`):

```java
eKycAttemptLogRepository.updateOutcome(
    req.getIdentifierId(), latestAttemptNumber,
    "LOTTE_REJECTED", "LOTTE_SUBMIT", lotteErrorCode, lotteErrorMessage
);
```

**Điểm inject 2 — Sau khi account APPROVED** (provisioning handler, sau `eKycRepository.save(ekyc)`):

```java
eKycAttemptLogRepository.updateFinalEkycId(ekyc.getIdentifierId(), ekyc.getId());

int totalAttempts = eKycAttemptLogRepository.countByIdentifierId(ekyc.getIdentifierId());
ZonedDateTime firstAttemptAt = eKycAttemptLogRepository.findFirstAttemptAt(ekyc.getIdentifierId());
ekyc.setTotalAttempts(totalAttempts);
ekyc.setFirstAttemptAt(firstAttemptAt);
customEKycRepo.save(ekyc);
```

Chi tiết đầy đủ: `BE_Spec.md` (Phần A) Section 2.

---

## Task 7 — Admin REST: `GET /api/admin/ekyc/attempts/*`

**Package:** `com.nhsv.ekyc.web.rest.admin`
**File:** `AdminEKycAttemptLogResource.java`

Implement 3 endpoints theo `BE_Spec.md` (Phần A) Section 4 (đã có response mẫu đầy đủ):

| Endpoint | Mô tả |
|---------|-------|
| `GET /api/admin/ekyc/attempts/search?identifierId=&phoneNo=` | Tìm summary 1 khách theo CCCD/SĐT |
| `GET /api/admin/ekyc/attempts/list?accountStatus=APPROVED\|NOT_APPROVED&page=&size=` | **Danh sách phân trang theo tab** — nguồn cho 2 tab "Đã mở TK" / "Chưa mở TK" trên Admin UI |
| `GET /api/admin/ekyc/attempts/{identifierId}` | Danh sách các lần thử của 1 khách |
| `GET /api/admin/ekyc/attempts/{identifierId}/{attemptNumber}` | Chi tiết 1 lần thử — bao gồm cả `vnptRawData` và `sdkRawLogs` cho mục đích debug/audit sâu (chỉ hiển thị ở tab riêng trên Admin UI, không phải trong bảng field-by-field chính) |

> Response detail (`/{identifierId}/{attemptNumber}`) nên có thêm object `rawAudit: { vnptRawData, sdkRawLogs }` — tách riêng khỏi các field đã structured, để Admin UI biết đây là dữ liệu debug, không phải data chính hiển thị mặc định.
>
> `/list` với `accountStatus=NOT_APPROVED`: query gộp mọi customer mà lần thử gần nhất **không phải** `outcome = SUCCESS` — xem query logic đầy đủ ở `BE_Spec.md` (Phần A) Section 4.1b.

---

# Phần B — Sub-feature 07: Compliance Journey Log

> **Kiến trúc:** 10 step đầu do **App gọi** `POST /api/v1/ekycs/journey-log` real-time tại từng màn hình; step thứ 11 (`ECONTRACT_SIGN_COMPLETED`) do **Backend tự ghi** qua webhook FPT có sẵn — App không liên quan tới step này. Retention: chỉ giữ hành trình **thành công** (`ACCOUNT_OPENING_COMPLETED`); hành trình bỏ dở >8h bị purge tự động. Chi tiết đầy đủ: `BE_Spec.md` (Phần B).

## Task 8 — Liquibase Changeset: tạo table `ekyc_journey_log`

**File:** `src/main/resources/config/liquibase/changelog/20260715000001_add_ekyc_journey_log.xml`

Tạo bảng `ekyc_journey_log` theo schema đầy đủ ở spec Section 3.1 + 3.2:

- `id` BIGINT AUTO_INCREMENT PRIMARY KEY
- `session_id` VARCHAR(64) — định danh 1 hành trình, App generate; NULL cho step `ECONTRACT_SIGN_COMPLETED`
- `phone_no` VARCHAR(20), `identifier_id` VARCHAR(20)
- `e_kyc_id` BIGINT (FK → `e_kyc.id`) — NULL tới khi `ACCOUNT_OPENING_COMPLETED`, là khóa chính cho step 11
- `step` VARCHAR(50) NOT NULL, `status` VARCHAR(20) NOT NULL (`SUCCESS`/`FAILED`)
- `payload` LONGTEXT NOT NULL — toàn bộ field App/webhook gửi, giữ nguyên kể cả base64 ảnh
- `created_at` DATETIME NOT NULL
- Index: `session_id`, `identifier_id`, `phone_no`, `e_kyc_id`, `step`, `created_at`

XML changeset đầy đủ: spec Section 3.2.

---

## Task 9 — JPA Entity: `EKycJourneyLog.java` + enum `JourneyStepEnum`

**Package:** `com.nhsv.ekyc.domain` (entity), `com.nhsv.ekyc.constant` (enum)

Copy nguyên cấu trúc field từ spec Section 4. Enum `JourneyStepEnum` gồm đúng 11 giá trị theo thứ tự spec Section 2:

```java
public enum JourneyStepEnum {
    EKYC_SEND_OTP,
    EKYC_VERIFY_OTP,
    GO_TO_ID_CARD_GUIDE,
    EKYC_FACE_SCAN,
    PERSONAL_INFORMATION,
    ACCOUNT_INFORMATION,
    BANK_INFORMATION,
    INVESTMENT_INFORMATION,
    TERMS_AND_CONDITIONS_CONFIRMATION,
    ACCOUNT_OPENING_COMPLETED,   // mốc chốt retention
    ECONTRACT_SIGN_COMPLETED     // backend-only
}
```

`payload` map bằng `@Lob`, lưu JSON nguyên văn — **không** parse thành cột riêng (khác hẳn cách tiếp cận của `ekyc_attempt_log`).

---

## Task 10 — Repository: `EKycJourneyLogRepository.java`

**Package:** `com.nhsv.ekyc.repository`

Cần tối thiểu:

```java
@Repository
public interface EKycJourneyLogRepository extends JpaRepository<EKycJourneyLog, Long> {

    List<EKycJourneyLog> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    boolean existsBySessionIdAndStep(String sessionId, String step);

    @Modifying
    @Query(value = """
        DELETE FROM ekyc_journey_log
        WHERE session_id IN (
          SELECT session_id FROM (
            SELECT session_id, MIN(created_at) AS started_at
            FROM ekyc_journey_log
            WHERE session_id IS NOT NULL
            GROUP BY session_id
            HAVING SUM(CASE WHEN step = 'ACCOUNT_OPENING_COMPLETED' THEN 1 ELSE 0 END) = 0
               AND started_at < :cutoff
          ) t
        )
        """, nativeQuery = true)
    int deleteAbandonedSessionsOlderThan(@Param("cutoff") ZonedDateTime cutoff);
}
```

Query purge đầy đủ: spec Section 7.

---

## Task 11 — Service: `EKycJourneyLogService.java`

**Package:** `com.nhsv.ekyc.service`

Hai method chính:

**`processJourneyLog(EKycJourneyLogRequest req)`** — xử lý 10 step App-facing:
- Validate `sessionId`, `step`, `status`, `payload` bắt buộc → thiếu field nào trả `400 INVALID_PARAMETER`.
- Validate `step` thuộc `JourneyStepEnum` (10 giá trị App-facing, loại trừ `ECONTRACT_SIGN_COMPLETED`) → sai giá trị trả `400 INVALID_VALUE`.
- **Không** validate business rule nội dung `payload` — theo triết lý "Light Validation at TradeX" (`tradex-api-conventions.md`).
- Ghi 1 row mới, `created_at = now()`, trả về `{ id }`.

**`logEcontractSigned(Long eKycId, EContractStatusReq request)`** — gọi từ webhook FPT (Task 13), **không** qua REST endpoint public:
- `session_id = NULL`, `e_kyc_id` = tham số truyền vào.
- `payload` gồm: `envelopeId`, `refId`, `contactId`, `contractStatus`, `contractIdAction`, `contractNo` (nếu có), `signFileContent` (base64), `webhookReceivedAt`.
- Chi tiết payload: spec Section 6.

---

## Task 12 — REST Resource: `POST /api/v1/ekycs/journey-log`

**Package:** `com.nhsv.ekyc.web.rest`

```typescript
// Request
{
  sessionId: string,
  phoneNo?: string,
  identifierId?: string,
  step: string,              // 1 trong 10 step App-facing
  status: "SUCCESS" | "FAILED",
  payload: object
}

// Response 200
{ id: number }
```

Integration type: **TradeX-native** (theo `tradex-api-conventions.md` Response Format Standards) — không dùng `success`/`code: "0000"` kiểu Lotte.

---

## Task 13 — Modify `EContractCustomServiceImpl.getEContractStatus()`

**File:** `EContractCustomServiceImpl.java` (đã tồn tại — nhận callback ký hợp đồng từ FPT, đã xác thực chữ ký RSA)

Thêm hook ngay tại điểm xác nhận khách ký xong (spec Section 6):

```java
if (contactId.equals(eContract.getIdentifierId())) {
    if (contactIdAction.equals(ContactIdAction.signed) && contractStatus.equals(ContractStatus.processing)) {
        eContractInfo.setCustomerSignatueStatus(contactIdAction.name());

        // ── THÊM MỚI ──
        eKycJourneyLogService.logEcontractSigned(
            eContract.getEKyc().geteKycId(),
            request  // EContractStatusReq — chứa envelopeId, refId, contactId, contractStatus
        );
        // ──────────────

        ... // logic ký hợp đồng hiện tại giữ nguyên
```

Logic ký hợp đồng hiện tại **không đổi** — chỉ thêm 1 lệnh gọi.

---

## Task 14 — Scheduled Purge Job

**Package:** `com.nhsv.ekyc.scheduler` (hoặc theo convention project hiện tại)

```java
@Scheduled(cron = "0 0 * * * *") // mỗi giờ, đầu giờ
public void purgeAbandonedJourneys() {
    journeyLogRepository.deleteAbandonedSessionsOlderThan(ZonedDateTime.now().minusHours(8));
}
```

Ngưỡng 8h khớp với `EKYC_SESSION_ID_EXPIRE_TIME` thực tế (`ekyc-admin/.../constant/Constants.java:235`) — không hardcode số riêng, tham chiếu cùng hằng số nếu có thể. `ECONTRACT_SIGN_COMPLETED` không bị ảnh hưởng bởi job này (`session_id IS NULL`).

---

## Task 15 — Export Tool (nội bộ, không blocking go-live)

Script CLI trong repo `ekyc-admin`, BE dev chạy tay khi compliance/audit cần tra soát 1 hành trình cụ thể:

- Input: `--sessionId=` hoặc `--identifierId=` hoặc `--eKycId=`.
- Query `ekyc_journey_log` theo khóa tương ứng, `ORDER BY created_at ASC` → render vào template `journey.html` (đã được PM duyệt phần visualization).
- Output: file HTML tĩnh, gửi nội bộ khi có yêu cầu — không publish/host công khai.

> Không phải màn hình sống, không cần đưa vào cùng sprint go-live của Task 8-14 — có thể làm sau khi có case audit thực tế đầu tiên.

---

## Acceptance Criteria

### Sub-feature 01

- [ ] Bảng `ekyc_attempt_log` được tạo qua Liquibase changeset — migration chạy thành công trên dev/staging, đúng schema `BE_Spec.md` (Phần A) Section 0.1 (bao gồm cả `outcome`, `failure_step`, `phone_no`, `image_front_url`, `image_back_url`, `vnpt_raw_data`, `sdk_raw_logs`).
- [ ] `POST /ekycs/attempt-log` nhận request, lưu DB, trả về `{ attemptId }` — HTTP 201.
- [ ] `POST /ekycs/attempt-log` thiếu `outcome` → trả `400 INVALID_PARAMETER`.
- [ ] `POST /ekycs/attempt-log` với `failureStep = VNPT_OCR` mà thiếu `imageFrontBase64` → trả `400 IMAGE_REQUIRED_FOR_OCR_FAILURE`.
- [ ] **`sdkRawLogs` được lưu nguyên văn vào cột `sdk_raw_logs`** — verify bằng cách gửi request có field lạ (không có cột riêng map) trong `sdkRawLogs`, sau đó query DB thấy field đó vẫn còn nguyên trong JSON.
- [ ] **`vnptRawData` được lưu nguyên văn vào cột `vnpt_raw_data`** đồng thời BE vẫn parse đúng các cột `vnpt_*` riêng lẻ từ cùng payload.
- [ ] Sau khi Lotte APPROVED, `ekyc_attempt_log.final_ekyc_id` được update đúng cho toàn bộ attempts cùng `identifier_id`.
- [ ] Sau khi Lotte reject, `outcome`/`failure_step`/`failure_code`/`failure_message` được update đúng.
- [ ] `GET /api/admin/ekyc/attempts/{identifierId}/{attemptNumber}` trả về đầy đủ field structured + object `rawAudit` chứa `vnptRawData` và `sdkRawLogs`.
- [ ] Admin endpoints yêu cầu `ROLE_ADMIN` — user thường nhận `403 FORBIDDEN`.
- [ ] Mỗi lần thử tạo row mới (`attempt_number` tăng dần), không xóa row cũ — kể cả khi user retry nhiều lần.
- [ ] Bảng `ekyc_ext` hiện tại không bị thay đổi hoặc xóa.
- [ ] `e_kyc.total_attempts` và `e_kyc.first_attempt_at` được cập nhật đúng sau khi APPROVED.
- [ ] `GET /api/admin/ekyc/attempts/list?accountStatus=APPROVED` chỉ trả về customer có lần thử gần nhất `outcome = SUCCESS`.
- [ ] `GET /api/admin/ekyc/attempts/list?accountStatus=NOT_APPROVED` trả về đúng customer mà **chưa từng có** lần thử `outcome = SUCCESS` — bao gồm cả customer chỉ có attempt fail ở pre-submit (không có `e_kyc` row nào).
- [ ] `/list` hỗ trợ pagination đúng (`totalCount`, `customers[]`, `page`, `size`).
- [ ] `vnpt_citizen_id` parse đúng từ `object.id` — verify bằng sample thực tế Section 0.6, KHÔNG bị null dù `object.citizenId` không tồn tại trong response.
- [ ] Cả 3 dòng MRZ (`mrz_line1/2/3`) được lưu đủ khi VNPT trả về CCCD gắn chip (mảng `mrz` có 3 phần tử).
- [ ] `vnpt_qr_match_summary` = `PASS` khi cả 4 field `match_qr.*` = `"yes"`, = `FAIL` khi có field `"no"`, = `SKIPPED` khi response không có `match_qr`.
- [ ] `liveness_card_front_fake_prob`/`liveness_card_rear_fake_prob` lưu đúng giá trị riêng biệt theo từng mặt — verify test case có giá trị khác nhau giữa 2 mặt để đảm bảo không bị ghi đè lẫn nhau.
- [ ] 6 cột `vnpt_*_log_id` được lưu đúng khi App gửi kèm — verify bằng cách tra ngược 1 attempt và xác nhận logID khớp với App log gốc.

### Sub-feature 07

- [ ] Bảng `ekyc_journey_log` được tạo qua Liquibase changeset — migration chạy thành công trên dev/staging.
- [ ] `POST /api/v1/ekycs/journey-log` nhận đủ 10 step App-facing, lưu 1 row/lần gọi, trả về `{ id }` — HTTP 200.
- [ ] Thiếu `sessionId`/`step`/`status`/`payload` → trả `400 INVALID_PARAMETER`.
- [ ] `step` không thuộc 10 giá trị App-facing hợp lệ → trả `400 INVALID_VALUE`.
- [ ] `payload` được lưu **nguyên văn** (kể cả base64 ảnh ở `EKYC_FACE_SCAN`) — không bị BE parse/lọc field nào.
- [ ] Khi FPT webhook báo khách ký hợp đồng thành công (`customerSignatueStatus = signed`), BE **tự động** ghi 1 row `step = ECONTRACT_SIGN_COMPLETED`, `session_id = NULL`, `e_kyc_id` đúng khách — không qua endpoint `/journey-log`, không phụ thuộc App còn mở hay không.
- [ ] Scheduled job chạy mỗi giờ, xóa toàn bộ row của session nào **quá 8h** mà chưa có row `ACCOUNT_OPENING_COMPLETED`.
- [ ] Session đã có row `ACCOUNT_OPENING_COMPLETED` — dữ liệu được giữ **vĩnh viễn**, không bị purge job động tới dù có tuổi bao lâu.
- [ ] Row `ECONTRACT_SIGN_COMPLETED` không bị ảnh hưởng bởi purge job (do `session_id IS NULL`, job chỉ xét theo `session_id`).
- [ ] Verify bằng test case thực tế: 1 hành trình đủ 10 step + đợi webhook FPT → tra ra đủ 11 row theo đúng `e_kyc_id`, `ORDER BY created_at ASC` đúng thứ tự nghiệp vụ.

---

## Implementation Notes

- **JHipster pattern:** Theo convention hiện tại của project — nếu project dùng MapStruct thì tạo Mapper tương ứng cho cả 2 sub-feature.
- **Image storage là sub-feature riêng (Scope 2, chưa có tài liệu):** `imageFrontBase64`/`imageBackBase64` (sub-feature 01) → upload lên S3/MinIO cần `ImageStorageService`. Scope/retention policy đang chờ PM confirm — nếu chưa confirm, có thể tạm bỏ qua upload thật và để `image_front_url`/`image_back_url` = null, nhưng **KHÔNG bỏ qua việc lưu `vnpt_raw_data` và `sdk_raw_logs`** (2 cột raw JSON này độc lập với image storage, không cần chờ confirm). Sub-feature 07 lưu ảnh trực tiếp trong `payload` (base64 nguyên văn), không phụ thuộc `ImageStorageService`.
- **Privacy:** PM sẽ quyết định retention policy cho toàn bộ `ekyc_attempt_log` (bao gồm cả raw JSON). Chưa implement auto-purge trong scope này (append-only, không xóa). Riêng `ekyc_journey_log` **đã có** cơ chế purge (Task 14) nhưng **chưa qua PDPD review** cho việc lưu base64 ảnh sinh trắc học + PII nguyên văn ở hành trình thành công — cần chốt trước khi go-live (xem mục "Cần confirm" bên dưới).
- **`ekyc_ext` không xóa:** Giữ nguyên bảng cũ. `ekyc_attempt_log` là bổ sung cấu trúc, không thay thế.
- **Partial data OK (sub-feature 01):** App có thể gửi partial data (nhiều field null) — BE vẫn lưu, không reject. Ngoại lệ: `sdkRawLogs` nên được gửi đầy đủ mỗi khi SDK đã chạy log key tương ứng — xem validation rule ở Task 5.
- **`sdk_raw_logs` không cần parse phía BE:** Cột này tồn tại thuần cho mục đích audit — BE không cần hiểu cấu trúc JSON bên trong, chỉ lưu và trả về nguyên văn khi query detail.
- **07 độc lập với 01:** Không join, không share bảng — 2 bảng dùng chung service nhưng vận hành độc lập, liên kết qua `identifier_id`/`e_kyc_id` khi cần đối chiếu chéo (xem README "Quan hệ với sub-feature khác").
- **FE issue đi kèm:** App cần sửa ~9 màn hình để gọi `POST /api/v1/ekycs/journey-log` đúng thời điểm — xem `FE_Issue.md` (mới tạo cùng đợt). BE endpoint (Task 12) nên xong trước để FE có API test.

---

## Cần chốt trước khi implement Phần B (Sub-feature 07)

- [ ] FE Lead xác nhận effort sửa ~9 màn hình App (xem FE issue đi kèm).
- [ ] PDPD review: payload lưu đầy đủ PII + base64 ảnh sinh trắc học — cần xác nhận cách lưu này (LONGTEXT nguyên văn, không mã hoá field) đáp ứng yêu cầu bảo vệ dữ liệu cá nhân.
- [ ] Xác nhận field `contractNo` có sẵn trực tiếp trên entity `EContract`/`EContractInfo` hay cần bổ sung (dev kiểm tra khi implement Task 13).

---

**Document Status:** ✅ Complete | For: BE Dev (ekyc-admin team) | Next Steps: BE Lead + FE Lead xác nhận mục "Cần chốt" ở trên → dev implement Task 1-14 (Task 15 làm sau, không blocking) theo thứ tự → QA verify theo Acceptance Criteria → Admin UI team implement theo `FE_Issue_Admin_Attempt_History.md` (Scope 2, sau go-live)
