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

> **Kiến trúc:** 11 step đầu do **App gọi** `POST /api/v1/ekycs/journey-log` real-time tại từng màn hình (step 1 mới — `PERSONAL_DATA_PROCESSING_CONSENT`, bổ sung 2026-07-20); step thứ 12 (`ECONTRACT_SIGN_COMPLETED`) do **Backend tự ghi** qua webhook FPT có sẵn — App không liên quan tới step này. Retention: chỉ giữ hành trình **thành công** (`ACCOUNT_OPENING_COMPLETED`); hành trình bỏ dở >8h bị purge tự động. Chi tiết đầy đủ: `BE_Spec.md` (Phần B).

## Yêu cầu chức năng

1. **Ghi lại từng bước của hành trình mở tài khoản** — mỗi khi khách hoàn tất 1 trong 11 bước đầu (từ đồng ý xử lý dữ liệu cá nhân tới hoàn tất mở tài khoản — xem danh sách đầy đủ ở `BE_Spec.md` Phần B Section 2), hệ thống nhận và lưu lại **toàn bộ** thông tin khách đã thấy/nhập tại đúng bước đó, kể cả khi bước đó thất bại — không tóm tắt, không lọc field.
2. **Tự động ghi nhận thời điểm khách ký hợp đồng điện tử** — bước thứ 12, riêng bước này hệ thống tự ghi nhận ngay khi đối tác FPT xác nhận khách đã ký xong, không cần App gửi gì, và không phụ thuộc khách có đang mở app hay không (khách có thể ký muộn hơn, ở phiên/thiết bị khác).
3. **Chỉ giữ lại log của hành trình mở tài khoản thành công** — xem "Quy tắc nghiệp vụ" bên dưới để biết chính xác "thành công" được xác định tại đâu.
4. **Công cụ tra soát nội bộ cho compliance/audit** (không blocking go-live) — khi cần tra một hành trình cụ thể, dev có công cụ tra theo mã hành trình/CCCD/mã tài khoản, xuất ra 1 file xem được nội bộ — không phải màn hình sống, không public.

## Quy tắc nghiệp vụ

- **Mốc "thành công" là khi hồ sơ mở tài khoản được hệ thống xác nhận đã nhận** (bước gửi hồ sơ hoàn chỉnh, bước 9 trong PRD) — **không phải** khi khách ký hợp đồng (bước 10), và **không phải** khi nhân viên duyệt hồ sơ lần cuối (bước 11). Đây là quyết định đã chốt (PRD mục 4.4): nếu sau đó hồ sơ bị nhân viên từ chối, log hành trình **vẫn được coi là thành công** theo định nghĩa này và giữ lại vĩnh viễn, vì mốc chốt nằm ở bước gửi hồ sơ, không phải bước duyệt cuối.
- **Hành trình không đạt mốc "thành công" trong vòng 8 giờ kể từ lúc bắt đầu** (không có lỗi rõ ràng, khách chỉ đơn giản không tiếp tục) → toàn bộ log của hành trình đó bị xóa hoàn toàn, không giữ lại bước nào — kể cả bước đồng ý xử lý dữ liệu cá nhân đã ghi ở bước 1.
- **Việc ghi log không được làm chậm hoặc chặn luồng chính mở tài khoản của khách** — đây là instrumentation nền, khách hàng không nhìn thấy và không bị ảnh hưởng nếu việc ghi log gặp lỗi.
- **Không kiểm tra tính hợp lệ nội dung nghiệp vụ của dữ liệu App gửi lên** — hệ thống chỉ lưu lại đúng nguyên trạng những gì App gửi, không đánh giá đúng/sai của từng field bên trong.
- **Hai loại nhật ký độc lập, không share dữ liệu:** log hành trình (mục này) và log lần thử xác thực khuôn mặt/giấy tờ (sub-feature 01, Phần A) tách biệt hoàn toàn, vì chính sách lưu trữ ngược nhau (01 giữ mọi lần thử kể cả fail vĩnh viễn; 07 xóa hành trình không thành công) — chỉ liên kết qua CCCD/mã tài khoản khi cần đối chiếu chéo.

## Acceptance Criteria

- [ ] Cả 11 bước App-facing đều được lưu đúng thời điểm, đúng thứ tự, gắn với 1 mã hành trình xuyên suốt từ đầu tới cuối (kể cả khi 1 bước thất bại).
- [ ] Bước "Đồng ý xử lý dữ liệu cá nhân" (mới, bổ sung 2026-07-20) được lưu đúng, ngay trước bước gửi OTP.
- [ ] Thiếu thông tin bắt buộc trong yêu cầu ghi log → hệ thống báo lỗi rõ ràng, không lưu row rác.
- [ ] Dữ liệu khách nhập/xác nhận tại mỗi bước được lưu **đầy đủ, nguyên trạng** — không bị cắt bớt field, kể cả ảnh sinh trắc học ở bước quét khuôn mặt.
- [ ] Khi FPT xác nhận khách ký hợp đồng xong, hệ thống tự động ghi nhận đúng khách, đúng thời điểm — không cần App gọi gì thêm, không phụ thuộc App còn mở hay không.
- [ ] Sau 8 giờ, hành trình bỏ dở (chưa đạt mốc gửi hồ sơ thành công) bị xóa hoàn toàn — verify bằng cách để 1 hành trình test dừng giữa đường, đợi qua 8h, tra lại không còn thấy log nào của hành trình đó.
- [ ] Hành trình đã đạt mốc "thành công" — log được giữ lại vĩnh viễn, không bị xóa theo thời gian, **kể cả khi hồ sơ bị nhân viên từ chối sau đó**.
- [ ] Với 1 hành trình mở tài khoản thành công đầy đủ (từ đồng ý dữ liệu cá nhân tới ký hợp đồng), tra lại thấy đủ 12 bước, đúng thứ tự thời gian, đúng khách.

> Chi tiết kỹ thuật đầy đủ (schema, API contract, code mẫu) cho các mục trên: `BE_Spec.md` (Phần B).

---

## Acceptance Criteria

> Acceptance Criteria của Sub-feature 07 nằm trong Phần B ở trên (đã viết theo style BA/PO). Mục dưới đây chỉ còn Sub-feature 01.

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

---

## Implementation Notes

- **JHipster pattern:** Theo convention hiện tại của project — nếu project dùng MapStruct thì tạo Mapper tương ứng cho cả 2 sub-feature.
- **Image storage là sub-feature riêng (Scope 2, chưa có tài liệu):** `imageFrontBase64`/`imageBackBase64` (sub-feature 01) → upload lên S3/MinIO cần `ImageStorageService`. Scope/retention policy đang chờ PM confirm — nếu chưa confirm, có thể tạm bỏ qua upload thật và để `image_front_url`/`image_back_url` = null, nhưng **KHÔNG bỏ qua việc lưu `vnpt_raw_data` và `sdk_raw_logs`** (2 cột raw JSON này độc lập với image storage, không cần chờ confirm). Sub-feature 07 lưu ảnh trực tiếp trong `payload` (base64 nguyên văn), không phụ thuộc `ImageStorageService`.
- **Privacy:** PM sẽ quyết định retention policy cho toàn bộ `ekyc_attempt_log` (bao gồm cả raw JSON). Chưa implement auto-purge trong scope này (append-only, không xóa). Riêng `ekyc_journey_log` **đã có** cơ chế purge (xem `BE_Spec.md` Phần B Section 7) nhưng **chưa qua PDPD review** cho việc lưu base64 ảnh sinh trắc học + PII nguyên văn ở hành trình thành công — cần chốt trước khi go-live (xem mục "Cần confirm" bên dưới).
- **`ekyc_ext` không xóa:** Giữ nguyên bảng cũ. `ekyc_attempt_log` là bổ sung cấu trúc, không thay thế.
- **Partial data OK (sub-feature 01):** App có thể gửi partial data (nhiều field null) — BE vẫn lưu, không reject. Ngoại lệ: `sdkRawLogs` nên được gửi đầy đủ mỗi khi SDK đã chạy log key tương ứng — xem validation rule ở Task 5.
- **`sdk_raw_logs` không cần parse phía BE:** Cột này tồn tại thuần cho mục đích audit — BE không cần hiểu cấu trúc JSON bên trong, chỉ lưu và trả về nguyên văn khi query detail.
- **07 độc lập với 01:** Không join, không share bảng — 2 bảng dùng chung service nhưng vận hành độc lập, liên kết qua `identifier_id`/`e_kyc_id` khi cần đối chiếu chéo (xem README "Quan hệ với sub-feature khác").
- **FE issue đi kèm:** App cần sửa ~10-11 màn hình (đã tính 1 màn hình mới cho PERSONAL_DATA_PROCESSING_CONSENT) để gọi `POST /api/v1/ekycs/journey-log` đúng thời điểm — xem `FE_Issue.md` (Phần B). BE endpoint `POST /ekycs/journey-log` nên xong trước để FE có API test. `FE_Issue.md` (Phần A) cover phần FE của sub-feature 01 (App gọi `POST /ekycs/attempt-log`) — gộp chung 1 file, bổ sung 2026-07-20.

---

## Cần chốt trước khi implement Phần B (Sub-feature 07)

- [ ] FE Lead xác nhận effort sửa ~10 màn hình App (xem FE issue đi kèm).
- [ ] PDPD review: payload lưu đầy đủ PII + base64 ảnh sinh trắc học — cần xác nhận cách lưu này (LONGTEXT nguyên văn, không mã hoá field) đáp ứng yêu cầu bảo vệ dữ liệu cá nhân.
- [ ] Xác nhận field `contractNo` có sẵn trực tiếp trên entity `EContract`/`EContractInfo` hay cần bổ sung (dev kiểm tra khi implement — xem `BE_Spec.md` Phần B Section 6).

---

**Document Status:** ✅ Complete | For: BE Dev (ekyc-admin team), BA/PO | Next Steps: BE Lead + FE Lead xác nhận mục "Cần chốt" ở trên → dev implement theo Phần A (Task 1-7) + Phần B (Yêu cầu chức năng + Quy tắc nghiệp vụ) → QA verify theo Acceptance Criteria → Admin UI team implement theo `Scope_2/Issues/02_FE_Issue_Admin_Attempt_History.md` (Scope 2, sau go-live)
