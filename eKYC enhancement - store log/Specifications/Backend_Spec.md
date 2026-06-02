# Backend Specification: eKYC Attempt History

**Version:** 2.0 | **Date:** 2026-05-24 | **Service:** ekyc-admin

---

## 0. DB Schema & Migration

### 0.1 Bảng mới: `ekyc_attempt_log`

Ghi lại mọi lần thử eKYC. **Append-only** — không update, không delete.

```sql
CREATE TABLE ekyc_attempt_log (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,

  -- ── Định danh khách hàng ──
  identifier_id   VARCHAR(20)  NOT NULL COMMENT 'Số CCCD/CMND',
  phone_no        VARCHAR(20)           COMMENT 'Số điện thoại',
  attempt_number  INT          NOT NULL COMMENT 'Thứ tự lần thử (1, 2, 3...)',
  attempt_at      DATETIME     NOT NULL COMMENT 'Thời điểm submit',

  -- ── Link tới kết quả cuối ──
  final_ekyc_id   BIGINT                COMMENT 'FK → e_kyc.id. Set khi TK được mở thành công',

  -- ── Kết quả lần thử ──
  outcome         VARCHAR(30)  NOT NULL COMMENT 'VNPT_FAILED / MRZ_FAILED / FACE_COMPARE_FAILED / LOTTE_REJECTED / USER_ABANDONED / SUCCESS',
  failure_step    VARCHAR(50)           COMMENT 'VNPT_OCR / VNPT_LIVENESS / MRZ_VALIDATION / MRZ_CROSS_CHECK / FACE_COMPARE / LOTTE_SUBMIT / CONTRACT_SIGN',
  failure_code    VARCHAR(100)          COMMENT 'Error code từ VNPT hoặc Lotte',
  failure_message VARCHAR(500)          COMMENT 'Mô tả lỗi',

  -- ── VNPT OCR Results ──
  vnpt_status_code     INT              COMMENT '0 = success',
  vnpt_citizen_id      VARCHAR(20)      COMMENT 'Số CCCD VNPT đọc được',
  vnpt_name            VARCHAR(100)     COMMENT 'Họ tên VNPT đọc được',
  vnpt_card_type       VARCHAR(10)      COMMENT 'Loại thẻ: CMND / CC',
  vnpt_citizen_id_prob DOUBLE           COMMENT 'Confidence score số CCCD (0-1)',
  vnpt_mrz_valid_score INT              COMMENT 'Điểm MRZ hợp lệ (0-10)',

  -- ── Fraud Detection ──
  vnpt_is_tampered            VARCHAR(5)  COMMENT '"Y"=hợp lệ / "N"=bị chỉnh sửa (tampering.is_legal)',
  vnpt_id_fake_warning        VARCHAR(50) COMMENT 'Cảnh báo số CCCD giả',
  vnpt_id_fake_prob           DOUBLE      COMMENT 'Xác suất số CCCD giả (0-1)',
  vnpt_duplication_warning    BOOLEAN     COMMENT 'CCCD bị dùng trùng',
  vnpt_dob_fake_warning       BOOLEAN     COMMENT 'Cảnh báo ngày sinh giả',
  vnpt_address_fake_warning   BOOLEAN     COMMENT 'Cảnh báo địa chỉ giả',
  vnpt_issuedate_fake_warning BOOLEAN     COMMENT 'Cảnh báo ngày cấp giả',
  vnpt_name_fake_warning      VARCHAR(50) COMMENT 'Cảnh báo tên giả',

  -- ── Card Integrity (mặt trước) ──
  vnpt_front_recaptured   VARCHAR(20) COMMENT 'Ảnh chụp lại từ màn hình? (checking_result_front.recaptured_result)',
  vnpt_front_edited_prob  DOUBLE      COMMENT 'Xác suất ảnh bị chỉnh sửa (checking_result_front.edited_prob)',
  vnpt_front_photocopied  VARCHAR(20) COMMENT 'Ảnh photocopy? (checking_result_front.check_photocopied_result)',

  -- ── Card Integrity (mặt sau) ──
  vnpt_back_recaptured    VARCHAR(20) COMMENT 'Tương tự, mặt sau',
  vnpt_back_edited_prob   DOUBLE,
  vnpt_back_photocopied   VARCHAR(20),

  -- ── Image Quality (mặt trước) ──
  vnpt_front_blur_score      DOUBLE COMMENT 'quality_front.blur_score — cao = nét',
  vnpt_front_luminance_score DOUBLE COMMENT 'quality_front.luminance_score — cao = đủ sáng',

  -- ── Image Quality (mặt sau) ──
  vnpt_back_blur_score       DOUBLE,
  vnpt_back_luminance_score  DOUBLE,

  -- ── Cross-Validation (khớp 2 mặt) ──
  vnpt_match_id         VARCHAR(10) COMMENT 'match_front_back.match_id',
  vnpt_match_name       VARCHAR(10) COMMENT 'match_front_back.match_name',
  vnpt_match_bod        VARCHAR(10) COMMENT 'match_front_back.match_bod',
  vnpt_match_valid_date VARCHAR(10) COMMENT 'match_front_back.match_valid_date',

  -- ── Extended OCR Fields ──
  vnpt_nationality       VARCHAR(50)  COMMENT 'Quốc tịch — object.nationality',
  vnpt_citizen_id_chip   VARCHAR(20)  COMMENT 'Số CCCD trên chip (có thể khác mặt thẻ) — object.citizenIdChip',

  -- ── MRZ (Machine Readable Zone) ──
  mrz_line1            VARCHAR(50)  COMMENT 'Dòng 1 MRZ thô — object.mrz[0] (IDVNM...)',
  mrz_line2            VARCHAR(50)  COMMENT 'Dòng 2 MRZ thô — object.mrz[1] (9001151M300...)',
  mrz_overall_prob     DOUBLE       COMMENT 'Độ tin cậy tổng thể đọc MRZ (0-1) — object.mrz_prob',

  -- ── MRZ Cross-Check (App so sánh MRZ với OCR visual) ──
  mrz_cross_check      VARCHAR(20)  COMMENT 'Kết quả tổng hợp: PASS / PARTIAL_FAIL / FAIL / SKIPPED',
  mrz_check_id         VARCHAR(10)  COMMENT 'Số CCCD MRZ vs OCR: MATCH / MISMATCH',
  mrz_check_dob        VARCHAR(10)  COMMENT 'Ngày sinh MRZ vs OCR: MATCH / MISMATCH',
  mrz_check_gender     VARCHAR(10)  COMMENT 'Giới tính MRZ vs OCR: MATCH / MISMATCH',
  mrz_check_expiry     VARCHAR(10)  COMMENT 'Ngày hết hạn MRZ vs OCR: MATCH / MISMATCH',

  -- ── Liveness Results (SDK — LOG_LIVENESS_*) ──
  liveness_card_front_result VARCHAR(20)  COMMENT 'Kết quả liveness mặt trước CCCD: success/failure',
  liveness_card_rear_result  VARCHAR(20)  COMMENT 'Kết quả liveness mặt sau CCCD: success/failure',
  liveness_face_result       VARCHAR(20)  COMMENT 'Kết quả liveness khuôn mặt: success/failure',
  face_mask_result           VARCHAR(20)  COMMENT 'Kết quả phát hiện mặt nạ: success/failure',
  fake_liveness_prob         DOUBLE       COMMENT 'Xác suất liveness giả (0-1)',
  fake_print_photo_prob      DOUBLE       COMMENT 'Xác suất ảnh in/photo giả (0-1)',

  -- ── Face Compare (SDK — LOG_COMPARE) ──
  face_compare_msg  VARCHAR(20)  COMMENT 'Kết quả so khớp khuôn mặt: MATCH / NOMATCH',
  face_compare_prob DOUBLE       COMMENT 'Độ tương đồng khuôn mặt (0-1, ngưỡng thường ≥ 0.7)',

  -- ── Image Storage (S3 / MinIO) ──
  image_front_url VARCHAR(500) COMMENT 'URL ảnh mặt trước CCCD đã upload lên S3/MinIO',
  image_back_url  VARCHAR(500) COMMENT 'URL ảnh mặt sau CCCD đã upload lên S3/MinIO',

  -- ── Raw Data (full audit) ──
  vnpt_raw_data LONGTEXT COMMENT 'Full VNPT JSON response — dùng cho debug',

  INDEX idx_ekyc_attempt_identifier  (identifier_id),
  INDEX idx_ekyc_attempt_phone       (phone_no),
  INDEX idx_ekyc_attempt_at          (attempt_at),
  INDEX idx_ekyc_attempt_final_ekyc  (final_ekyc_id),
  INDEX idx_ekyc_attempt_outcome     (outcome),

  CONSTRAINT fk_ekyc_attempt_final
    FOREIGN KEY (final_ekyc_id) REFERENCES e_kyc(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Lịch sử mọi lần thử eKYC — append only';
```

### 0.2 Thay đổi bảng `e_kyc`

Thêm 2 cột nullable — backward compatible, không ảnh hưởng code hiện tại.

```sql
ALTER TABLE e_kyc
  ADD COLUMN total_attempts    INT      NULL COMMENT 'Tổng số lần thử eKYC trước khi mở TK thành công',
  ADD COLUMN first_attempt_at  DATETIME NULL COMMENT 'Thời điểm lần thử eKYC đầu tiên';
```

### 0.3 Liquibase Changeset

**File:** `src/main/resources/config/liquibase/changelog/20260524000001_add_ekyc_attempt_log.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.9.xsd">

    <changeSet id="20260524000001" author="duc.nguyen">
        <createTable tableName="ekyc_attempt_log">
            <column name="id" type="bigint" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="identifier_id" type="varchar(20)">
                <constraints nullable="false"/>
            </column>
            <column name="phone_no" type="varchar(20)"/>
            <column name="attempt_number" type="int">
                <constraints nullable="false"/>
            </column>
            <column name="attempt_at" type="datetime">
                <constraints nullable="false"/>
            </column>
            <column name="final_ekyc_id" type="bigint"/>
            <column name="outcome" type="varchar(30)">
                <constraints nullable="false"/>
            </column>
            <column name="failure_step" type="varchar(50)"/>
            <column name="failure_code" type="varchar(100)"/>
            <column name="failure_message" type="varchar(500)"/>
            <column name="vnpt_status_code" type="int"/>
            <column name="vnpt_citizen_id" type="varchar(20)"/>
            <column name="vnpt_name" type="varchar(100)"/>
            <column name="vnpt_card_type" type="varchar(10)"/>
            <column name="vnpt_citizen_id_prob" type="double"/>
            <column name="vnpt_mrz_valid_score" type="int"/>
            <column name="vnpt_is_tampered" type="varchar(5)"/>
            <column name="vnpt_id_fake_warning" type="varchar(50)"/>
            <column name="vnpt_id_fake_prob" type="double"/>
            <column name="vnpt_duplication_warning" type="boolean"/>
            <column name="vnpt_dob_fake_warning" type="boolean"/>
            <column name="vnpt_address_fake_warning" type="boolean"/>
            <column name="vnpt_issuedate_fake_warning" type="boolean"/>
            <column name="vnpt_name_fake_warning" type="varchar(50)"/>
            <column name="vnpt_front_recaptured" type="varchar(20)"/>
            <column name="vnpt_front_edited_prob" type="double"/>
            <column name="vnpt_front_photocopied" type="varchar(20)"/>
            <column name="vnpt_back_recaptured" type="varchar(20)"/>
            <column name="vnpt_back_edited_prob" type="double"/>
            <column name="vnpt_back_photocopied" type="varchar(20)"/>
            <column name="vnpt_front_blur_score" type="double"/>
            <column name="vnpt_front_luminance_score" type="double"/>
            <column name="vnpt_back_blur_score" type="double"/>
            <column name="vnpt_back_luminance_score" type="double"/>
            <column name="vnpt_match_id" type="varchar(10)"/>
            <column name="vnpt_match_name" type="varchar(10)"/>
            <column name="vnpt_match_bod" type="varchar(10)"/>
            <column name="vnpt_match_valid_date" type="varchar(10)"/>
            <column name="vnpt_nationality" type="varchar(50)"/>
            <column name="vnpt_citizen_id_chip" type="varchar(20)"/>
            <column name="mrz_line1" type="varchar(50)"/>
            <column name="mrz_line2" type="varchar(50)"/>
            <column name="mrz_overall_prob" type="double"/>
            <column name="mrz_cross_check" type="varchar(20)"/>
            <column name="mrz_check_id" type="varchar(10)"/>
            <column name="mrz_check_dob" type="varchar(10)"/>
            <column name="mrz_check_gender" type="varchar(10)"/>
            <column name="mrz_check_expiry" type="varchar(10)"/>
            <column name="liveness_card_front_result" type="varchar(20)"/>
            <column name="liveness_card_rear_result" type="varchar(20)"/>
            <column name="liveness_face_result" type="varchar(20)"/>
            <column name="face_mask_result" type="varchar(20)"/>
            <column name="fake_liveness_prob" type="double"/>
            <column name="fake_print_photo_prob" type="double"/>
            <column name="face_compare_msg" type="varchar(20)"/>
            <column name="face_compare_prob" type="double"/>
            <column name="image_front_url" type="varchar(500)"/>
            <column name="image_back_url" type="varchar(500)"/>
            <column name="vnpt_raw_data" type="longtext"/>
        </createTable>

        <addForeignKeyConstraint
            constraintName="fk_ekyc_attempt_final"
            baseTableName="ekyc_attempt_log" baseColumnNames="final_ekyc_id"
            referencedTableName="e_kyc" referencedColumnNames="id"
            onDelete="SET NULL"/>

        <createIndex tableName="ekyc_attempt_log" indexName="idx_ekyc_attempt_identifier">
            <column name="identifier_id"/>
        </createIndex>
        <createIndex tableName="ekyc_attempt_log" indexName="idx_ekyc_attempt_phone">
            <column name="phone_no"/>
        </createIndex>
        <createIndex tableName="ekyc_attempt_log" indexName="idx_ekyc_attempt_at">
            <column name="attempt_at"/>
        </createIndex>
        <createIndex tableName="ekyc_attempt_log" indexName="idx_ekyc_attempt_outcome">
            <column name="outcome"/>
        </createIndex>
        <createIndex tableName="ekyc_attempt_log" indexName="idx_ekyc_attempt_final_ekyc">
            <column name="final_ekyc_id"/>
        </createIndex>
    </changeSet>

    <changeSet id="20260524000002" author="duc.nguyen">
        <addColumn tableName="e_kyc">
            <column name="total_attempts" type="int"/>
            <column name="first_attempt_at" type="datetime"/>
        </addColumn>
    </changeSet>

</databaseChangeLog>
```

### 0.4 Mapping VNPT Response → `ekyc_attempt_log`

| `ekyc_attempt_log` column | `VNPTDataBase64` field |
|--------------------------|----------------------|
| `vnpt_status_code` | `statusCode` |
| `vnpt_citizen_id` | `object.citizenId` |
| `vnpt_name` | `object.name` |
| `vnpt_card_type` | `object.cardType` |
| `vnpt_citizen_id_prob` | `object.citizenIdProb` |
| `vnpt_mrz_valid_score` | `object.mrzValidScore` |
| `vnpt_is_tampered` | `object.tampering.isLegal` |
| `vnpt_id_fake_warning` | `object.idFakeWarning` |
| `vnpt_id_fake_prob` | `object.idFakeProb` |
| `vnpt_duplication_warning` | `object.dupplicationWarning` |
| `vnpt_dob_fake_warning` | `object.dobFakeWarning` |
| `vnpt_address_fake_warning` | `object.addressFakeWarning` |
| `vnpt_issuedate_fake_warning` | `object.issuedateFakeWarning` |
| `vnpt_name_fake_warning` | `object.nameFakeWarning` |
| `vnpt_front_recaptured` | `object.checkingResultFront.recapturedResult` |
| `vnpt_front_edited_prob` | `object.checkingResultFront.editedProb` |
| `vnpt_front_photocopied` | `object.checkingResultFront.checkPhotocopiedResult` |
| `vnpt_back_recaptured` | `object.checkingResultBack.recapturedResult` |
| `vnpt_back_edited_prob` | `object.checkingResultBack.editedProb` |
| `vnpt_back_photocopied` | `object.checkingResultBack.checkPhotocopiedResult` |
| `vnpt_front_blur_score` | `object.qualityFront.blurScore` |
| `vnpt_front_luminance_score` | `object.qualityFront.luminanceScore` |
| `vnpt_back_blur_score` | `object.qualityBack.blurScore` |
| `vnpt_back_luminance_score` | `object.qualityBack.luminanceScore` |
| `vnpt_match_id` | `object.matchFrontBack.matchId` |
| `vnpt_match_name` | `object.matchFrontBack.matchName` |
| `vnpt_match_bod` | `object.matchFrontBack.matchBod` |
| `vnpt_match_valid_date` | `object.matchFrontBack.matchValidDate` |
| `vnpt_nationality` | `object.nationality` |
| `vnpt_citizen_id_chip` | `object.citizenIdChip` |
| `mrz_line1` | `object.mrz[0]` |
| `mrz_line2` | `object.mrz[1]` |
| `mrz_overall_prob` | `object.mrz_prob` |
| `vnpt_raw_data` | Toàn bộ JSON response (serialize) |

### 0.5 Mapping SDK Log Keys → `ekyc_attempt_log`

Các trường này **không có trong VNPT OCR response** — do SDK trả về qua `EkycBridge` (React Native). Được App gửi lên qua `POST /ekycs/attempt-log` khi lần thử **thất bại trước khi submit** (pre-submit failure).

| `ekyc_attempt_log` column | SDK Log Key | Giá trị |
|--------------------------|------------|---------|
| `liveness_card_front_result` | `LOG_LIVENESS_CARD_FRONT` | `"success"` / `"failure"` |
| `liveness_card_rear_result` | `LOG_LIVENESS_CARD_REAR` | `"success"` / `"failure"` |
| `liveness_face_result` | `LOG_LIVENESS_FACE` | `"success"` / `"failure"` |
| `face_mask_result` | `LOG_MASK_FACE` | `"success"` / `"failure"` |
| `fake_liveness_prob` | Trong `LOG_LIVENESS_FACE` JSON | `fakeLivenessProb` (0–1) |
| `fake_print_photo_prob` | Trong `LOG_LIVENESS_FACE` JSON | `fakePrintPhotoProb` (0–1) |
| `face_compare_msg` | `LOG_COMPARE` JSON | `"MATCH"` / `"NOMATCH"` |
| `face_compare_prob` | `LOG_COMPARE` JSON | Similarity score (0–1) |
| `image_front_url` | `LOG_PATH_IMAGE_FRONT` → upload S3 | URL sau khi BE upload |
| `image_back_url` | `LOG_PATH_IMAGE_BACK` → upload S3 | URL sau khi BE upload |
| `mrz_cross_check` | App tính toán | `PASS` / `PARTIAL_FAIL` / `FAIL` / `SKIPPED` |
| `mrz_check_id` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |
| `mrz_check_dob` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |
| `mrz_check_gender` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |
| `mrz_check_expiry` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |

**Nguồn dữ liệu theo loại lần thử:**

| Loại lần thử | image_front/back_url | liveness_* / face_compare_* |
|-------------|---------------------|------------------------------|
| **Post-submit** (App gọi `/lotte/ekycs`) | BE extract từ `VNPTDataBase64.Img.imgFront/imgBack`, upload S3 | App gửi kèm trong request body |
| **Pre-submit** (App log riêng qua `/ekycs/attempt-log`) | App gửi `imageFrontBase64` / `imageBackBase64`, BE upload S3 | App gửi kèm trong request body |

---

## 1. Entity mới: `EKycAttemptLog`

**File:** `domain/EKycAttemptLog.java`

```java
@Entity
@Table(name = "ekyc_attempt_log")
public class EKycAttemptLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier_id", nullable = false)
    private String identifierId;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "attempt_at", nullable = false)
    private ZonedDateTime attemptAt;

    @Column(name = "final_ekyc_id")
    private Long finalEkycId;

    @Column(name = "outcome", nullable = false)
    private String outcome;  // VNPT_FAILED / LOTTE_REJECTED / USER_ABANDONED / SUCCESS

    @Column(name = "failure_step")
    private String failureStep;  // VNPT_OCR / VNPT_LIVENESS / FACE_COMPARE / LOTTE_SUBMIT / CONTRACT_SIGN

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "failure_message")
    private String failureMessage;

    // VNPT OCR
    @Column(name = "vnpt_status_code")
    private Integer vnptStatusCode;

    @Column(name = "vnpt_citizen_id")
    private String vnptCitizenId;

    @Column(name = "vnpt_name")
    private String vnptName;

    @Column(name = "vnpt_card_type")
    private String vnptCardType;

    @Column(name = "vnpt_citizen_id_prob")
    private Double vnptCitizenIdProb;

    @Column(name = "vnpt_mrz_valid_score")
    private Integer vnptMrzValidScore;

    // Fraud Detection
    @Column(name = "vnpt_is_tampered")
    private String vnptIsTampered;

    @Column(name = "vnpt_id_fake_warning")
    private String vnptIdFakeWarning;

    @Column(name = "vnpt_id_fake_prob")
    private Double vnptIdFakeProb;

    @Column(name = "vnpt_duplication_warning")
    private Boolean vnptDuplicationWarning;

    @Column(name = "vnpt_dob_fake_warning")
    private Boolean vnptDobFakeWarning;

    @Column(name = "vnpt_address_fake_warning")
    private Boolean vnptAddressFakeWarning;

    @Column(name = "vnpt_issuedate_fake_warning")
    private Boolean vnptIssuedateFakeWarning;

    @Column(name = "vnpt_name_fake_warning")
    private String vnptNameFakeWarning;

    // Card Integrity
    @Column(name = "vnpt_front_recaptured")
    private String vnptFrontRecaptured;

    @Column(name = "vnpt_front_edited_prob")
    private Double vnptFrontEditedProb;

    @Column(name = "vnpt_front_photocopied")
    private String vnptFrontPhotocopied;

    @Column(name = "vnpt_back_recaptured")
    private String vnptBackRecaptured;

    @Column(name = "vnpt_back_edited_prob")
    private Double vnptBackEditedProb;

    @Column(name = "vnpt_back_photocopied")
    private String vnptBackPhotocopied;

    // Image Quality
    @Column(name = "vnpt_front_blur_score")
    private Double vnptFrontBlurScore;

    @Column(name = "vnpt_front_luminance_score")
    private Double vnptFrontLuminanceScore;

    @Column(name = "vnpt_back_blur_score")
    private Double vnptBackBlurScore;

    @Column(name = "vnpt_back_luminance_score")
    private Double vnptBackLuminanceScore;

    // Cross-validation
    @Column(name = "vnpt_match_id")
    private String vnptMatchId;

    @Column(name = "vnpt_match_name")
    private String vnptMatchName;

    @Column(name = "vnpt_match_bod")
    private String vnptMatchBod;

    @Column(name = "vnpt_match_valid_date")
    private String vnptMatchValidDate;

    // Extended OCR Fields
    @Column(name = "vnpt_nationality")
    private String vnptNationality;

    @Column(name = "vnpt_citizen_id_chip")
    private String vnptCitizenIdChip;

    // MRZ Raw Data
    @Column(name = "mrz_line1")
    private String mrzLine1;

    @Column(name = "mrz_line2")
    private String mrzLine2;

    @Column(name = "mrz_overall_prob")
    private Double mrzOverallProb;

    // MRZ Cross-Check (computed by App, sent to BE)
    @Column(name = "mrz_cross_check")
    private String mrzCrossCheck;   // PASS / PARTIAL_FAIL / FAIL / SKIPPED

    @Column(name = "mrz_check_id")
    private String mrzCheckId;      // MATCH / MISMATCH

    @Column(name = "mrz_check_dob")
    private String mrzCheckDob;

    @Column(name = "mrz_check_gender")
    private String mrzCheckGender;

    @Column(name = "mrz_check_expiry")
    private String mrzCheckExpiry;

    // Liveness Results (SDK logs)
    @Column(name = "liveness_card_front_result")
    private String livenessCardFrontResult;

    @Column(name = "liveness_card_rear_result")
    private String livenessCardRearResult;

    @Column(name = "liveness_face_result")
    private String livenessFaceResult;

    @Column(name = "face_mask_result")
    private String faceMaskResult;

    @Column(name = "fake_liveness_prob")
    private Double fakeLivenessProb;

    @Column(name = "fake_print_photo_prob")
    private Double fakePrintPhotoProb;

    // Face Compare
    @Column(name = "face_compare_msg")
    private String faceCompareMsg;   // MATCH / NOMATCH

    @Column(name = "face_compare_prob")
    private Double faceCompareProb;

    // Image Storage (S3 / MinIO)
    @Column(name = "image_front_url")
    private String imageFrontUrl;

    @Column(name = "image_back_url")
    private String imageBackUrl;

    // Raw Data
    @Lob
    @Column(name = "vnpt_raw_data")
    private String vnptRawData;

    // getters/setters omitted for brevity
}
```

---

## 2. Thay đổi `CustomEKycService.java`

> **Lưu ý kiến trúc:** `CustomEKycService` (handler của `/lotte/ekycs`) **không tự ghi `ekyc_attempt_log`**.
> Việc ghi log là trách nhiệm của endpoint `POST /ekycs/attempt-log` (Section 5).
> `CustomEKycService` chỉ cần:
> 1. Xử lý duplicate PENDING records (logic cũ giữ nguyên)
> 2. Update `final_ekyc_id` + `total_attempts` + `first_attempt_at` khi account APPROVED

### 2.1 Inject repository mới

```java
// Thêm vào constructor injection:
private final EKycAttemptLogRepository eKycAttemptLogRepository;
```

### 2.2 ~~Lưu attempt log trước block duplicate handling~~ — KHÔNG CẦN

~~Ngay trước `existedPendingEKycs.sort(...)` (line ~208)~~

> Không thêm gì vào đây. App đã gọi `POST /ekycs/attempt-log` riêng với đầy đủ SDK data.
> BE không có MRZ, liveness chi tiết, ảnh — chỉ App mới có trực tiếp từ SDK.

### 2.3 Method `buildAttemptLog` — dùng bởi `EKycAttemptLogService` (Section 5)

> Method này được gọi từ `EKycAttemptLogService.processAttemptLog()` — không gọi trực tiếp từ `CustomEKycService`.

```java
private EKycAttemptLog buildAttemptLog(EKycAddReq req, int attemptNumber) {
    EKycAttemptLog log = new EKycAttemptLog();
    log.setIdentifierId(req.getIdentifierId());
    log.setPhoneNo(req.getPhoneNo());
    log.setAttemptNumber(attemptNumber);
    log.setAttemptAt(ZonedDateTime.now());
    log.setOutcome("SUCCESS"); // default — sẽ update sang LOTTE_REJECTED nếu fail sau đó

    // Map VNPT data
    if (StringUtils.isNotBlank(req.getRawData())) {
        log.setVnptRawData(req.getRawData());
        try {
            VNPTDataBase64 vnpt = objectMapper.readValue(
                Base64.getDecoder().decode(req.getRawData()), VNPTDataBase64.class
            );
            log.setVnptStatusCode(vnpt.getStatusCode());
            VNPTDataBase64.VNPTObject obj = vnpt.getObject();
            if (obj != null) {
                log.setVnptCitizenId(obj.getCitizenId());
                log.setVnptName(obj.getName());
                log.setVnptCardType(obj.getCardType());
                log.setVnptCitizenIdProb(obj.getCitizenIdProb());
                log.setVnptMrzValidScore(obj.getMrzValidScore());

                // Fraud flags
                if (obj.getTampering() != null) {
                    log.setVnptIsTampered(obj.getTampering().getIsLegal());
                }
                log.setVnptIdFakeWarning(obj.getIdFakeWarning());
                log.setVnptIdFakeProb(obj.getIdFakeProb());
                log.setVnptDuplicationWarning(obj.isDupplicationWarning());
                log.setVnptDobFakeWarning(obj.isDobFakeWarning());
                log.setVnptAddressFakeWarning(obj.isAddressFakeWarning());
                log.setVnptIssuedateFakeWarning(obj.isIssuedateFakeWarning());
                log.setVnptNameFakeWarning(obj.getNameFakeWarning());

                // Card integrity - front
                if (obj.getCheckingResultFront() != null) {
                    log.setVnptFrontRecaptured(obj.getCheckingResultFront().getRecapturedResult());
                    log.setVnptFrontEditedProb(obj.getCheckingResultFront().getEditedProb());
                    log.setVnptFrontPhotocopied(obj.getCheckingResultFront().getCheckPhotocopiedResult());
                }
                // Card integrity - back
                if (obj.getCheckingResultBack() != null) {
                    log.setVnptBackRecaptured(obj.getCheckingResultBack().getRecapturedResult());
                    log.setVnptBackEditedProb(obj.getCheckingResultBack().getEditedProb());
                    log.setVnptBackPhotocopied(obj.getCheckingResultBack().getCheckPhotocopiedResult());
                }

                // Image quality
                if (obj.getQualityFront() != null) {
                    log.setVnptFrontBlurScore(obj.getQualityFront().getBlurScore());
                    log.setVnptFrontLuminanceScore(obj.getQualityFront().getLuminanceScore());
                }
                if (obj.getQualityBack() != null) {
                    log.setVnptBackBlurScore(obj.getQualityBack().getBlurScore());
                    log.setVnptBackLuminanceScore(obj.getQualityBack().getLuminanceScore());
                }

                // Cross-validation
                if (obj.getMatchFrontBack() != null) {
                    log.setVnptMatchId(obj.getMatchFrontBack().getMatchId());
                    log.setVnptMatchName(obj.getMatchFrontBack().getMatchName());
                    log.setVnptMatchBod(obj.getMatchFrontBack().getMatchBod());
                    log.setVnptMatchValidDate(obj.getMatchFrontBack().getMatchValidDate());
                }

                // Extended OCR
                log.setVnptNationality(obj.getNationality());
                log.setVnptCitizenIdChip(obj.getCitizenIdChip());
            }

            // ── MRZ Raw Data ──
            // vnpt.getMrz() trả về List<String> — 2 dòng MRZ thô
            if (vnpt.getMrz() != null && vnpt.getMrz().size() >= 2) {
                log.setMrzLine1(vnpt.getMrz().get(0));
                log.setMrzLine2(vnpt.getMrz().get(1));
            }
            log.setMrzOverallProb(vnpt.getMrzProb());
            // MRZ cross-check fields được App tính toán và gửi kèm request
            // → set từ req.getMrzCrossCheck(), req.getMrzCheckId(), v.v.
            // → xem Section 5 (pre-submit API) và Section 3 (post-submit request)

            // ── Image upload (post-submit path) ──
            // VNPTDataBase64.Img chứa imgFront / imgBack dưới dạng base64
            if (vnpt.getImg() != null) {
                if (StringUtils.isNotBlank(vnpt.getImg().getImgFront())) {
                    String frontUrl = imageStorageService.uploadBase64(
                        vnpt.getImg().getImgFront(),
                        "ekyc/" + req.getIdentifierId() + "/attempt-" + attemptNumber + "-front.jpg"
                    );
                    log.setImageFrontUrl(frontUrl);
                }
                if (StringUtils.isNotBlank(vnpt.getImg().getImgBack())) {
                    String backUrl = imageStorageService.uploadBase64(
                        vnpt.getImg().getImgBack(),
                        "ekyc/" + req.getIdentifierId() + "/attempt-" + attemptNumber + "-back.jpg"
                    );
                    log.setImageBackUrl(backUrl);
                }
            }
        } catch (Exception e) {
            // Parse fail → vẫn lưu raw_data, không throw
            logger.warn("Failed to parse VNPT rawData for attempt log", e);
        }
    }
    return log;
}
```

### 2.4 Update outcome khi Lotte reject

Trong `LotteEKycService.java`, khi call `lotteEKycService.lotteEKycNoAsync()` throw exception:

```java
// Trong catch block sau khi Lotte reject:
eKycAttemptLogRepository.updateOutcome(
    req.getIdentifierId(),
    latestAttemptNumber,
    "LOTTE_REJECTED",
    "LOTTE_SUBMIT",
    lotteErrorCode,
    lotteErrorMessage
);
```

### 2.5 Update `final_ekyc_id` khi account approved

Trong provisioning handler (sau khi `e_kyc.status = APPROVED`):

```java
// Link tất cả attempt của identifier_id này về e_kyc thành công
eKycAttemptLogRepository.updateFinalEkycId(ekyc.getIdentifierId(), ekyc.getId());

// Cập nhật stats vào e_kyc
int totalAttempts = eKycAttemptLogRepository.countByIdentifierId(ekyc.getIdentifierId());
ZonedDateTime firstAttemptAt = eKycAttemptLogRepository.findFirstAttemptAt(ekyc.getIdentifierId());
ekyc.setTotalAttempts(totalAttempts);
ekyc.setFirstAttemptAt(firstAttemptAt);
customEKycRepo.save(ekyc);
```

---

## 3. Repository: `EKycAttemptLogRepository`

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
    void updateOutcome(
        @Param("identifierId") String identifierId,
        @Param("attemptNumber") int attemptNumber,
        @Param("outcome") String outcome,
        @Param("step") String step,
        @Param("code") String code,
        @Param("message") String message
    );
}
```

---

## 4. API Endpoints cho Admin UI

**Base path:** `/api/admin/ekyc/attempts`  
**Auth:** Admin role required

### 4.1 Tìm kiếm theo CCCD hoặc SĐT

```
GET /api/admin/ekyc/attempts/search
  ?identifierId={cccd}   (optional)
  &phoneNo={phone}       (optional)

Response 200:
{
  "identifierId": "038xxxxxxxx",
  "fullName": "Nguyễn Văn A",
  "phoneNo": "09xxxxxxxx",
  "totalAttempts": 3,
  "successfulAttempt": 3,
  "accountNumber": "039C123456",
  "accountStatus": "APPROVED",
  "firstAttemptAt": "2025-05-15T09:23:14+07:00",
  "accountOpenedAt": "2025-05-18T10:05:00+07:00"
}
```

### 4.2 Lấy danh sách các lần thử

```
GET /api/admin/ekyc/attempts/{identifierId}

Response 200:
{
  "attempts": [
    {
      "attemptNumber": 1,
      "attemptAt": "2025-05-15T09:23:14+07:00",
      "outcome": "VNPT_FAILED",
      "failureStep": "VNPT_OCR",
      "failureCode": "VNPT_IMAGE_BLURRED",
      "failureMessage": "Ảnh mặt trước quá mờ",
      "vnptFrontBlurScore": 0.23,
      "vnptIssTampered": "Y"
      // ... summary fields
    },
    {
      "attemptNumber": 3,
      "outcome": "SUCCESS",
      "finalEkycId": 12345
      // ...
    }
  ]
}
```

### 4.3 Chi tiết một lần thử

```
GET /api/admin/ekyc/attempts/{identifierId}/{attemptNumber}

Response 200:
{
  "attemptNumber": 1,
  "attemptAt": "...",
  "outcome": "VNPT_FAILED",

  "vnptOcr": {
    "citizenId": "038xxx",
    "name": "NGUYEN VAN A",
    "cardType": "CC",
    "citizenIdProb": 0.95,
    "mrzValidScore": 8,
    "nationality": "Việt Nam",
    "citizenIdChip": "038xxxxxxxx"
  },
  "fraudDetection": {
    "isTampered": "Y",
    "idFakeWarning": null,
    "idFakeProb": 0.02,
    "duplicationWarning": false,
    "dobFakeWarning": false,
    "addressFakeWarning": false,
    "issuedateFakeWarning": false
  },
  "cardIntegrity": {
    "frontRecaptured": "REAL",
    "frontEditedProb": 0.01,
    "frontPhotocopied": "ORIGINAL",
    "backRecaptured": "REAL",
    "backEditedProb": 0.01
  },
  "imageQuality": {
    "frontBlurScore": 0.23,
    "frontLuminanceScore": 0.75,
    "backBlurScore": 0.72,
    "backLuminanceScore": 0.80
  },
  "crossValidation": {
    "matchId": "MATCH",
    "matchName": "MATCH",
    "matchBod": "MATCH",
    "matchValidDate": "MATCH"
  },
  "livenessResults": {
    "cardFrontResult": "success",
    "cardRearResult": "success",
    "faceResult": "failure",
    "maskResult": "success",
    "fakeLivenessProb": 0.12,
    "fakePrintPhotoProb": 0.05
  },
  "faceCompare": {
    "msg": "MATCH",
    "prob": 0.91
  },
  "images": {
    "frontUrl": "https://minio.example.com/ekyc/038xxx/attempt-1-front.jpg",
    "backUrl": "https://minio.example.com/ekyc/038xxx/attempt-1-back.jpg"
  },
  "mrz": {
    "line1": "IDVNM030207010063<<<<<<<<<<<<<<<",
    "line2": "0301230M3001158VNM<<<<<<<<<<<<<<4",
    "overallProb": 0.97,
    "validScore": 9,
    "crossCheck": "PASS",
    "checks": {
      "id":     "MATCH",
      "dob":    "MATCH",
      "gender": "MATCH",
      "expiry": "MATCH"
    }
  }
}
```

---

## 5. Service: `EKycAttemptLogService`

**File:** `service/EKycAttemptLogService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EKycAttemptLogService {

    private final EKycAttemptLogRepository eKycAttemptLogRepository;
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;

    /**
     * Xử lý request từ POST /ekycs/attempt-log.
     * Được gọi bởi EKycAttemptLogResource.
     */
    public EKycAttemptLogResponseDTO processAttemptLog(
            EKycAttemptLogRequest req,
            String identifierId,  // lấy từ JWT, không từ request body
            String phoneNo
    ) {
        int attemptNumber = eKycAttemptLogRepository.countByIdentifierId(identifierId) + 1;

        EKycAttemptLog log = new EKycAttemptLog();
        log.setIdentifierId(identifierId);
        log.setPhoneNo(phoneNo);
        log.setAttemptNumber(attemptNumber);
        log.setAttemptAt(ZonedDateTime.now());
        log.setOutcome(req.getOutcome());
        log.setFailureStep(req.getFailureStep());
        log.setFailureCode(req.getFailureCode());
        log.setFailureMessage(req.getFailureMessage());

        // Extract VNPT fields từ rawData nếu có
        if (StringUtils.isNotBlank(req.getVnptRawData())) {
            buildAttemptLog(log, req.getVnptRawData(), identifierId, attemptNumber);
        }

        // Map MRZ fields từ request (SDK-only, không có trong rawData)
        log.setMrzLine1(req.getMrzLine1());
        log.setMrzLine2(req.getMrzLine2());
        log.setMrzOverallProb(req.getMrzProb());
        log.setVnptMrzValidScore(req.getMrzValidScore());
        log.setMrzCrossCheck(req.getMrzCrossCheck());
        log.setMrzCheckId(req.getMrzCheckId());
        log.setMrzCheckDob(req.getMrzCheckDob());
        log.setMrzCheckGender(req.getMrzCheckGender());
        log.setMrzCheckExpiry(req.getMrzCheckExpiry());

        // Map liveness & face compare fields
        log.setLivenessCardFrontResult(req.getLivenessCardFrontResult());
        log.setLivenessCardRearResult(req.getLivenessCardRearResult());
        log.setLivenessFaceResult(req.getLivenessFaceResult());
        log.setFaceMaskResult(req.getFaceMaskResult());
        log.setFakeLivenessProb(req.getFakeLivenessProb());
        log.setFakePrintPhotoProb(req.getFakePrintPhotoProb());
        log.setFaceCompareMsg(req.getFaceCompareMsg());
        log.setFaceCompareProb(req.getFaceCompareProb());

        // Upload ảnh nếu có
        if (StringUtils.isNotBlank(req.getImageFrontBase64())) {
            String frontUrl = imageStorageService.uploadBase64(
                req.getImageFrontBase64(),
                "ekyc/" + identifierId + "/attempt-" + attemptNumber + "-front.jpg"
            );
            log.setImageFrontUrl(frontUrl);
        }
        if (StringUtils.isNotBlank(req.getImageBackBase64())) {
            String backUrl = imageStorageService.uploadBase64(
                req.getImageBackBase64(),
                "ekyc/" + identifierId + "/attempt-" + attemptNumber + "-back.jpg"
            );
            log.setImageBackUrl(backUrl);
        }

        EKycAttemptLog saved = eKycAttemptLogRepository.save(log);
        return new EKycAttemptLogResponseDTO(saved.getId());
    }
}
```

**DTO:**

```java
// Request DTO
public class EKycAttemptLogRequest {
    @NotBlank String outcome;
    String failureStep;
    String failureCode;
    String failureMessage;
    String vnptRawData;
    // MRZ fields
    String mrzLine1; String mrzLine2; Double mrzProb; Integer mrzValidScore;
    String mrzCrossCheck; String mrzCheckId; String mrzCheckDob;
    String mrzCheckGender; String mrzCheckExpiry;
    // Liveness & face compare
    String livenessCardFrontResult; String livenessCardRearResult;
    String livenessFaceResult; String faceMaskResult;
    Double fakeLivenessProb; Double fakePrintPhotoProb;
    String faceCompareMsg; Double faceCompareProb;
    // Images (base64)
    String imageFrontBase64;
    String imageBackBase64;
}

// Response DTO
public record EKycAttemptLogResponseDTO(Long attemptId) {}
```

**REST Controller:**

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
            @AuthenticationPrincipal UserDetails userDetails  // lấy identifierId từ JWT
    ) {
        String identifierId = extractIdentifierId(userDetails);
        String phoneNo      = extractPhoneNo(userDetails);
        return eKycAttemptLogService.processAttemptLog(req, identifierId, phoneNo);
    }
}
```

---

## 7. API: eKYC Attempt Log (App → TradeX)

> **Nguyên tắc thiết kế quan trọng:**
> - `POST /lotte/ekycs` — **KHÔNG THAY ĐỔI**. Chỉ gửi những gì Lotte cần. Không thêm field mới.
> - `POST /ekycs/attempt-log` — API TradeX mới. **App gọi cho MỌI lần thử** để lưu log user journey vào hệ thống TradeX (không liên quan Lotte).

### 7.0 Tổng quan luồng gọi API

```
──── Pre-submit failure (SDK thất bại trước khi gọi Lotte) ────

App ──[VNPT SDK]──► OCR / Liveness / MRZ fail
        │
        ▼
App ──► POST /ekycs/attempt-log  {outcome: VNPT_FAILED, images: required}
        └─► BE lưu ekyc_attempt_log, upload ảnh S3
        └─► Response: { attemptId: 123 }
        (kết thúc — KHÔNG gọi /lotte/ekycs)


──── Post-submit (SDK pass, App gọi Lotte) ────

App ──[VNPT SDK]──► All checks pass
        │
        ├──► POST /lotte/ekycs  { rawData, identifierId, ... }  ← KHÔNG ĐỔI
        │         └─► Lotte response (success / reject)
        │
        └──► POST /ekycs/attempt-log  {
                  outcome: SUCCESS | LOTTE_REJECTED,
                  vnptRawData: "<same base64>",   ← BE extract VNPT fields từ đây
                  mrzLine1/2, mrzProb, mrzCrossCheck,  ← SDK-only fields
                  livenessFaceResult, faceCompareMsg, ...
                  images: optional (chỉ cần nếu muốn lưu)
              }
              └─► Response: { attemptId: 456 }
```

> **Lý do App gọi cả 2 endpoint song song (hoặc tuần tự) trong post-submit:**
> `/lotte/ekycs` gửi rawData để Lotte xử lý. `POST /ekycs/attempt-log` gửi SDK-specific data
> (MRZ, liveness chi tiết, face compare, ảnh) mà BE không có từ rawData VNPT.

---

### 7.1 Endpoint

```
POST /ekycs/attempt-log
Authorization: Bearer {accessToken}
Content-Type: application/json
```

> Auth token được dùng để lấy `identifierId` và `phoneNo` — App **không cần truyền thủ công** nếu thông tin đã có trong JWT.

---

### 7.2 Request Body

```json
{
  // ── Kết quả lần thử ──
  "outcome":        "VNPT_FAILED",
  "failureStep":    "VNPT_OCR",
  "failureCode":    "IMAGE_BLURRED",
  "failureMessage": "Ảnh mặt trước quá mờ — blur_score 0.23 < ngưỡng 0.5",

  // ── VNPT rawData (chỉ post-submit) ──
  // Nếu cung cấp: BE tự extract toàn bộ VNPT fields (OCR, fraud, image quality, v.v.)
  // Nếu không có (pre-submit fail): BE chỉ lưu các field được truyền rõ ràng bên dưới
  "vnptRawData":    "<base64 VNPT response>",

  // ── MRZ (App tính toán từ SDK) ──
  "mrzLine1":       "IDVNM030207010063<<<<<<<<<<<<<<<",
  "mrzLine2":       "0301230M3001158VNM<<<<<<<<<<<<<<4",
  "mrzProb":        0.97,
  "mrzValidScore":  9,
  "mrzCrossCheck":  "PASS",
  "mrzCheckId":     "MATCH",
  "mrzCheckDob":    "MATCH",
  "mrzCheckGender": "MATCH",
  "mrzCheckExpiry": "MATCH",

  // ── Liveness & Face Compare (SDK logs) ──
  "livenessCardFrontResult": "success",
  "livenessCardRearResult":  "success",
  "livenessFaceResult":      "success",
  "faceMaskResult":          "success",
  "fakeLivenessProb":        0.05,
  "fakePrintPhotoProb":      0.02,
  "faceCompareMsg":          "MATCH",
  "faceCompareProb":         0.92,

  // ── Ảnh OCR (REQUIRED khi outcome = VNPT_FAILED hoặc failureStep = VNPT_OCR) ──
  // App lấy từ LOG_PATH_IMAGE_FRONT / LOG_PATH_IMAGE_BACK (SDK trả về local path)
  // Convert sang base64 trước khi gửi. Max 5 MB mỗi ảnh sau decode.
  "imageFrontBase64": "data:image/jpeg;base64,/9j/...",
  "imageBackBase64":  "data:image/jpeg;base64,/9j/..."
}
```

---

### 7.3 Validation Rules

| Field | Rule |
|-------|------|
| `outcome` | **Bắt buộc.** Enum: `VNPT_FAILED` / `FACE_COMPARE_FAILED` / `MRZ_FAILED` / `LOTTE_REJECTED` / `USER_ABANDONED` / `SUCCESS` |
| `failureStep` | Bắt buộc khi `outcome` không phải `SUCCESS` hoặc `USER_ABANDONED` |
| `imageFrontBase64` | **Bắt buộc** khi `failureStep = VNPT_OCR`. Optional trong các trường hợp khác |
| `imageBackBase64` | Cùng rule với `imageFrontBase64` |
| `vnptRawData` | Optional. Khi có, BE extract VNPT fields (override các field vnpt_* được gửi riêng nếu có conflict) |
| `mrzValidScore` | Optional. Nếu null → `mrz_cross_check = SKIPPED` |
| Ảnh size | Max 5 MB mỗi ảnh sau base64 decode |

---

### 7.4 BE Processing Logic

```
1. Parse JWT → lấy identifierId, phoneNo (không yêu cầu App truyền lại)

2. Tính attempt_number:
   attemptNumber = COUNT(*) FROM ekyc_attempt_log
                   WHERE identifier_id = identifierId + 1

3. Nếu có vnptRawData → parse và extract toàn bộ VNPT fields
   (giống buildAttemptLog() trong Section 2.3 — tái dùng method này)

4. Upload ảnh lên S3/MinIO nếu imageFrontBase64 / imageBackBase64 có trong request:
   imageFrontUrl = imageStorageService.uploadBase64(
       req.imageFrontBase64,
       "ekyc/{identifierId}/attempt-{attemptNumber}-front.jpg"
   )
   imageBackUrl = imageStorageService.uploadBase64(...)

   → Lưu ý: chỉ upload khi failureStep = VNPT_OCR hoặc App chủ động gửi ảnh.
     Không upload ảnh cho liveness fail / face compare fail (không liên quan OCR image).

5. Insert ekyc_attempt_log:
   - outcome, failureStep, failureCode, failureMessage
   - Tất cả VNPT fields (từ vnptRawData nếu có, hoặc để null)
   - MRZ fields: mrzLine1, mrzLine2, mrzOverallProb, mrzCrossCheck, mrzCheck*
   - Liveness + face compare fields từ request
   - imageFrontUrl, imageBackUrl (sau khi upload)
   - attemptAt = NOW()

6. Response: 201 Created
   { "attemptId": <id> }
```

---

### 7.5 Outcome → failureStep Mapping (hướng dẫn cho App)

| Scenario | outcome | failureStep | Ảnh bắt buộc? |
|----------|---------|-------------|--------------|
| SDK trả blur/warning OCR | `VNPT_FAILED` | `VNPT_OCR` | ✅ Có |
| MRZ checksum fail | `MRZ_FAILED` | `MRZ_VALIDATION` | ✅ Có (ảnh mặt sau) |
| MRZ ↔ OCR mismatch | `MRZ_FAILED` | `MRZ_CROSS_CHECK` | ✅ Có |
| Liveness card fail | `VNPT_FAILED` | `VNPT_LIVENESS` | ❌ Không |
| Face liveness fail | `FACE_COMPARE_FAILED` | `VNPT_LIVENESS` | ❌ Không |
| Face compare NOMATCH | `FACE_COMPARE_FAILED` | `FACE_COMPARE` | ❌ Không |
| Lotte từ chối | `LOTTE_REJECTED` | `LOTTE_SUBMIT` | ❌ Không |
| Thành công | `SUCCESS` | null | ❌ Không |
| User bỏ qua ký HĐ | `USER_ABANDONED` | `CONTRACT_SIGN` | ❌ Không |

---

### 7.6 Response

```json
// 201 Created
{ "attemptId": 12345 }

// 400 Bad Request
{
  "code": "INVALID_PARAMETER",
  "params": [{ "code": "FIELD_IS_REQUIRED", "param": "outcome" }]
}

// 400 — Ảnh bắt buộc khi OCR fail
{
  "code": "IMAGE_REQUIRED_FOR_OCR_FAILURE",
  "params": [{ "code": "FIELD_IS_REQUIRED", "param": "imageFrontBase64" }]
}
```

---

## 8. Image Storage Service

**Interface:** `ImageStorageService`  
**Impl:** `MinioImageStorageService` / `S3ImageStorageService`

```java
public interface ImageStorageService {
    /**
     * Upload base64-encoded image to object storage.
     * @param base64Data  Base64 string (có thể có prefix "data:image/jpeg;base64,")
     * @param objectKey   Path trong bucket (e.g., "ekyc/038xxx/attempt-1-front.jpg")
     * @return Public/presigned URL của ảnh đã upload
     */
    String uploadBase64(String base64Data, String objectKey);
}
```

**Storage path convention:**
```
ekyc/{identifierId}/attempt-{attemptNumber}-front.jpg
ekyc/{identifierId}/attempt-{attemptNumber}-back.jpg
```

**Lưu ý bảo mật:**
- Bucket phải **private** — không public-read
- Trả về **presigned URL** có TTL (ví dụ 1 giờ) thay vì URL cố định
- Hoặc Admin UI gọi riêng endpoint `GET /api/admin/ekyc/attempts/{id}/image-url?type=front` để lấy presigned URL on demand
- Không lưu base64 vào DB — chỉ lưu URL sau khi upload thành công

---

**Document Status:** Draft v2.0 | **For:** Backend Dev | **Next Steps:** Implement + unit test + image upload integration test
