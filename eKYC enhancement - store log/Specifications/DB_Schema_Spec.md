# DB Schema Specification: eKYC Attempt History

**Version:** 2.0 | **Date:** 2026-05-24 | **Service:** ekyc-admin

---

## 1. Bảng mới: `ekyc_attempt_log`

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
  -- mrz_valid_score đã có ở trên (VNPT OCR Results section)

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

  -- Indexes
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

---

## 2. Thay đổi bảng `e_kyc`

Thêm 2 cột nullable — backward compatible, không ảnh hưởng code hiện tại.

```sql
ALTER TABLE e_kyc
  ADD COLUMN total_attempts    INT      NULL COMMENT 'Tổng số lần thử eKYC trước khi mở TK thành công',
  ADD COLUMN first_attempt_at  DATETIME NULL COMMENT 'Thời điểm lần thử eKYC đầu tiên';
```

---

## 3. Liquibase Changeset

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

            <!-- VNPT OCR -->
            <column name="vnpt_status_code" type="int"/>
            <column name="vnpt_citizen_id" type="varchar(20)"/>
            <column name="vnpt_name" type="varchar(100)"/>
            <column name="vnpt_card_type" type="varchar(10)"/>
            <column name="vnpt_citizen_id_prob" type="double"/>
            <column name="vnpt_mrz_valid_score" type="int"/>

            <!-- Fraud Detection -->
            <column name="vnpt_is_tampered" type="varchar(5)"/>
            <column name="vnpt_id_fake_warning" type="varchar(50)"/>
            <column name="vnpt_id_fake_prob" type="double"/>
            <column name="vnpt_duplication_warning" type="boolean"/>
            <column name="vnpt_dob_fake_warning" type="boolean"/>
            <column name="vnpt_address_fake_warning" type="boolean"/>
            <column name="vnpt_issuedate_fake_warning" type="boolean"/>
            <column name="vnpt_name_fake_warning" type="varchar(50)"/>

            <!-- Card Integrity -->
            <column name="vnpt_front_recaptured" type="varchar(20)"/>
            <column name="vnpt_front_edited_prob" type="double"/>
            <column name="vnpt_front_photocopied" type="varchar(20)"/>
            <column name="vnpt_back_recaptured" type="varchar(20)"/>
            <column name="vnpt_back_edited_prob" type="double"/>
            <column name="vnpt_back_photocopied" type="varchar(20)"/>

            <!-- Image Quality -->
            <column name="vnpt_front_blur_score" type="double"/>
            <column name="vnpt_front_luminance_score" type="double"/>
            <column name="vnpt_back_blur_score" type="double"/>
            <column name="vnpt_back_luminance_score" type="double"/>

            <!-- Cross Validation -->
            <column name="vnpt_match_id" type="varchar(10)"/>
            <column name="vnpt_match_name" type="varchar(10)"/>
            <column name="vnpt_match_bod" type="varchar(10)"/>
            <column name="vnpt_match_valid_date" type="varchar(10)"/>

            <!-- Extended OCR Fields -->
            <column name="vnpt_nationality" type="varchar(50)"/>
            <column name="vnpt_citizen_id_chip" type="varchar(20)"/>

            <!-- MRZ Raw Data -->
            <column name="mrz_line1" type="varchar(50)"/>
            <column name="mrz_line2" type="varchar(50)"/>
            <column name="mrz_overall_prob" type="double"/>

            <!-- MRZ Cross-Check -->
            <column name="mrz_cross_check" type="varchar(20)"/>
            <column name="mrz_check_id" type="varchar(10)"/>
            <column name="mrz_check_dob" type="varchar(10)"/>
            <column name="mrz_check_gender" type="varchar(10)"/>
            <column name="mrz_check_expiry" type="varchar(10)"/>

            <!-- Liveness Results -->
            <column name="liveness_card_front_result" type="varchar(20)"/>
            <column name="liveness_card_rear_result" type="varchar(20)"/>
            <column name="liveness_face_result" type="varchar(20)"/>
            <column name="face_mask_result" type="varchar(20)"/>
            <column name="fake_liveness_prob" type="double"/>
            <column name="fake_print_photo_prob" type="double"/>

            <!-- Face Compare -->
            <column name="face_compare_msg" type="varchar(20)"/>
            <column name="face_compare_prob" type="double"/>

            <!-- Image Storage -->
            <column name="image_front_url" type="varchar(500)"/>
            <column name="image_back_url" type="varchar(500)"/>

            <!-- Raw Data -->
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

---

## 4. Mapping VNPT Response → `ekyc_attempt_log`

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
| `vnpt_mrz_valid_score` | `object.mrz_valid_score` _(đã có)_ |
| `vnpt_raw_data` | Toàn bộ JSON response (serialize) |

---

## 5. Mapping SDK Log Keys → `ekyc_attempt_log`

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

### Nguồn dữ liệu theo loại lần thử

| Loại lần thử | image_front/back_url | liveness_* / face_compare_* |
|-------------|---------------------|------------------------------|
| **Post-submit** (App gọi `/lotte/ekycs`) | BE extract từ `VNPTDataBase64.Img.imgFront/imgBack`, upload S3 | App gửi kèm trong request body |
| **Pre-submit** (App log riêng qua `/ekycs/attempt-log`) | App gửi `imageFrontBase64` / `imageBackBase64`, BE upload S3 | App gửi kèm trong request body |

---

**Document Status:** Draft v2.0 | **For:** Backend Dev | **Next Steps:** Implement Liquibase migration + Entity class + Image upload service
