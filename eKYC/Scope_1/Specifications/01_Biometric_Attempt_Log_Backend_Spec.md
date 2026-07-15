# Backend Specification: eKYC Attempt History

**Version:** 2.1 | **Date:** 2026-07-08 | **Service:** ekyc-admin

> **Revision 2026-07-08:** Đối chiếu spec với sample log thực tế do dev gửi (OCR CCCD 2 mặt VNPT, liveness, face compare) — phát hiện & sửa vài chỗ mapping sai field, đồng thời bổ sung field mới mà data thực tế có nhưng spec bản trước bỏ sót (QR cross-check, phát hiện nhiều khuôn mặt, phát hiện đổi mặt/deepfake, VNPT logID). Chi tiết từng thay đổi được đánh dấu ⚠️ tại vị trí tương ứng. Sample thực tế (đã ẩn danh) xem Section 0.6.

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
  vnpt_citizen_id      VARCHAR(20)      COMMENT 'Số CCCD VNPT đọc được — ⚠️ sửa 2026-07-08: field nguồn thực tế là object.id, KHÔNG PHẢI object.citizenId (field này không tồn tại) — xem mapping 0.4',
  vnpt_old_citizen_id  VARCHAR(20)      COMMENT 'Số CMND cũ (nếu có) — object.citizen_id, giá trị "-" khi khách chưa từng có CMND. Field mới 2026-07-08.',
  vnpt_name            VARCHAR(100)     COMMENT 'Họ tên VNPT đọc được',
  vnpt_card_type       VARCHAR(10)      COMMENT 'Loại thẻ: CMND / CC',
  vnpt_citizen_id_prob DOUBLE           COMMENT 'Confidence score số CCCD (0-1)',
  vnpt_mrz_valid_score INT              COMMENT 'Điểm MRZ hợp lệ (0-100 theo sample thực tế, không phải 0-10 như comment cũ)',

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
  vnpt_match_sex        VARCHAR(10) COMMENT 'match_front_back.match_sex — ⚠️ sửa 2026-07-08: cột cũ "vnpt_match_valid_date" bị xóa vì field match_front_back.match_valid_date KHÔNG tồn tại trong response thực tế; field thật trả về là match_sex',

  -- ── Extended OCR Fields ──
  vnpt_nationality       VARCHAR(50)  COMMENT 'Quốc tịch — object.nationality',
  vnpt_citizen_id_chip   VARCHAR(20)  COMMENT 'Số CCCD giải mã từ QR/chip — ⚠️ sửa 2026-07-08: field nguồn thực tế là object.dict_qr.SoCCCD (KHÔNG có field object.citizenIdChip). Dùng để đối chiếu với vnpt_citizen_id (đọc từ mặt thẻ bằng OCR) — 2 giá trị lệch nhau là dấu hiệu nghi ngờ.',

  -- ── QR Code Cross-Check (đối chiếu QR/chip vs OCR mặt thẻ) — mới 2026-07-08 ──
  vnpt_qr_match_summary VARCHAR(20)  COMMENT 'PASS nếu toàn bộ 4 field match_qr.* = "yes"; FAIL nếu có field = "no"; SKIPPED nếu response không có match_qr. BE tự tính khi parse vnptRawData, không cần App gửi thêm.',

  -- ── MRZ (Machine Readable Zone) ──
  mrz_line1            VARCHAR(50)  COMMENT 'Dòng 1 MRZ thô — object.mrz[0] (IDVNM...)',
  mrz_line2            VARCHAR(50)  COMMENT 'Dòng 2 MRZ thô — object.mrz[1] (ngày sinh/giới tính/hết hạn...)',
  mrz_line3            VARCHAR(50)  COMMENT 'Dòng 3 MRZ thô — object.mrz[2] (họ tên). Mới 2026-07-08: CCCD gắn chip (định dạng TD1) trả về 3 dòng MRZ chứ không phải 2 như bản spec trước giả định.',
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

  -- ── Card Liveness Fraud Detail (mặt trước / mặt sau) ──
  -- ⚠️ sửa 2026-07-08: 2 cột "fake_liveness_prob"/"fake_print_photo_prob" ở bản spec v2.0 bị gán nhầm
  -- nguồn "LOG_LIVENESS_FACE" — sample thực tế cho thấy 2 field này thực ra nằm trong
  -- LOG_LIVENESS_CARD_FRONT và LOG_LIVENESS_CARD_REAR (mỗi mặt thẻ 1 giá trị riêng), KHÔNG có ở face liveness.
  -- Tách thành cột riêng theo mặt để tránh 2 lần ghi đè lẫn nhau nếu dùng chung 1 cột.
  liveness_card_front_fake_prob       DOUBLE  COMMENT 'Xác suất liveness giả của ảnh mặt trước CCCD (0-1) — LOG_LIVENESS_CARD_FRONT.fake_liveness_prob',
  liveness_card_front_fake_print_prob DOUBLE  COMMENT 'Xác suất ảnh mặt trước là ảnh in/chụp lại màn hình (0-1) — LOG_LIVENESS_CARD_FRONT.fake_print_photo_prob',
  liveness_card_front_face_swapping   BOOLEAN COMMENT 'Phát hiện đổi mặt/deepfake trên ảnh mặt trước — LOG_LIVENESS_CARD_FRONT.face_swapping. Field mới 2026-07-08.',
  liveness_card_rear_fake_prob        DOUBLE  COMMENT 'Tương tự liveness_card_front_fake_prob, cho mặt sau',
  liveness_card_rear_fake_print_prob  DOUBLE  COMMENT 'Tương tự liveness_card_front_fake_print_prob, cho mặt sau',
  liveness_card_rear_face_swapping    BOOLEAN COMMENT 'Tương tự liveness_card_front_face_swapping, cho mặt sau',
  liveness_face_multiple_faces        BOOLEAN COMMENT 'Phát hiện nhiều khuôn mặt trong ảnh liveness khuôn mặt — LOG_LIVENESS_FACE.multiple_faces_details. Field mới 2026-07-08 — dấu hiệu gian lận (ảnh có 2 người, hoặc dùng ảnh người khác).',

  -- ── Face Compare (SDK — LOG_COMPARE) ──
  face_compare_msg            VARCHAR(20)  COMMENT 'Kết quả so khớp khuôn mặt: MATCH / NOMATCH',
  face_compare_prob           DOUBLE       COMMENT 'Độ tương đồng khuôn mặt (0-1, ngưỡng thường ≥ 0.7)',
  face_compare_match_warning  VARCHAR(20)  COMMENT 'Cảnh báo bổ sung, tách biệt với msg — LOG_COMPARE.match_warning. Field mới 2026-07-08.',
  face_compare_multiple_faces BOOLEAN      COMMENT 'Phát hiện nhiều khuôn mặt trong ảnh so khớp — LOG_COMPARE.multiple_faces_details. Field mới 2026-07-08.',

  -- ── VNPT Log IDs (tracing — đối chiếu chéo với hệ thống VNPT khi audit/tranh chấp) — mới 2026-07-08 ──
  -- App đã có sẵn các ID này trong luồng /lotte/ekycs hiện tại (ocrLogId, cardLivenessLogId, v.v.)
  -- — chỉ cần App gửi kèm khi gọi /ekycs/attempt-log, không cần tính toán thêm.
  vnpt_ocr_log_id                 VARCHAR(150) COMMENT 'logID của LOG_OCR — dùng để VNPT support tra soát phiên OCR cụ thể',
  vnpt_card_liveness_front_log_id VARCHAR(150) COMMENT 'logID của LOG_LIVENESS_CARD_FRONT',
  vnpt_card_liveness_rear_log_id  VARCHAR(150) COMMENT 'logID của LOG_LIVENESS_CARD_REAR',
  vnpt_face_liveness_log_id       VARCHAR(150) COMMENT 'logID của LOG_LIVENESS_FACE',
  vnpt_face_compare_log_id        VARCHAR(150) COMMENT 'logID của LOG_COMPARE',
  vnpt_face_mask_log_id           VARCHAR(150) COMMENT 'logID của LOG_MASK_FACE',

  -- ── Image Storage (S3 / MinIO) ──
  image_front_url VARCHAR(500) COMMENT 'URL ảnh mặt trước CCCD đã upload lên S3/MinIO',
  image_back_url  VARCHAR(500) COMMENT 'URL ảnh mặt sau CCCD đã upload lên S3/MinIO',

  -- ── Raw Data (full audit) ──
  -- Lưu ý: 2 cột dưới đây cùng đảm bảo AUDIT ĐẦY ĐỦ toàn bộ output SDK trả về —
  -- không chỉ các field đã được parse vào cột riêng ở trên.
  vnpt_raw_data LONGTEXT COMMENT 'Raw JSON của LOG_OCR — response gốc từ VNPT server (OCR/biometric/fraud). Base64-decoded.',
  sdk_raw_logs  LONGTEXT COMMENT 'Raw JSON gộp của 7 SDK log key còn lại: LOG_LIVENESS_CARD_FRONT, LOG_LIVENESS_CARD_REAR, LOG_LIVENESS_FACE, LOG_MASK_FACE, LOG_COMPARE, LOG_PATH_IMAGE_FRONT, LOG_PATH_IMAGE_BACK — nguyên văn, KHÔNG qua xử lý/lọc field. Đảm bảo audit được cả field SDK trả về mà BE chưa map cột riêng.',

  -- ── Terms Consent (sub-feature 05) ──
  terms_agreed_at DATETIME    NULL         COMMENT 'Thời điểm khách hàng tick đồng ý điều khoản hợp đồng (UTC). Ghi qua call riêng từ màn hình Xác nhận điều khoản.',
  terms_version   VARCHAR(20) DEFAULT 'v1' COMMENT 'Phiên bản nội dung điều khoản tại thời điểm đồng ý — cập nhật thủ công khi nội dung điều khoản thay đổi.',

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
            <column name="vnpt_old_citizen_id" type="varchar(20)"/>
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
            <column name="vnpt_match_sex" type="varchar(10)"/>
            <column name="vnpt_nationality" type="varchar(50)"/>
            <column name="vnpt_citizen_id_chip" type="varchar(20)"/>
            <column name="vnpt_qr_match_summary" type="varchar(20)"/>
            <column name="mrz_line1" type="varchar(50)"/>
            <column name="mrz_line2" type="varchar(50)"/>
            <column name="mrz_line3" type="varchar(50)"/>
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
            <column name="liveness_card_front_fake_prob" type="double"/>
            <column name="liveness_card_front_fake_print_prob" type="double"/>
            <column name="liveness_card_front_face_swapping" type="boolean"/>
            <column name="liveness_card_rear_fake_prob" type="double"/>
            <column name="liveness_card_rear_fake_print_prob" type="double"/>
            <column name="liveness_card_rear_face_swapping" type="boolean"/>
            <column name="liveness_face_multiple_faces" type="boolean"/>
            <column name="face_compare_msg" type="varchar(20)"/>
            <column name="face_compare_prob" type="double"/>
            <column name="face_compare_match_warning" type="varchar(20)"/>
            <column name="face_compare_multiple_faces" type="boolean"/>
            <column name="vnpt_ocr_log_id" type="varchar(150)"/>
            <column name="vnpt_card_liveness_front_log_id" type="varchar(150)"/>
            <column name="vnpt_card_liveness_rear_log_id" type="varchar(150)"/>
            <column name="vnpt_face_liveness_log_id" type="varchar(150)"/>
            <column name="vnpt_face_compare_log_id" type="varchar(150)"/>
            <column name="vnpt_face_mask_log_id" type="varchar(150)"/>
            <column name="image_front_url" type="varchar(500)"/>
            <column name="image_back_url" type="varchar(500)"/>
            <column name="vnpt_raw_data" type="longtext"/>
            <column name="sdk_raw_logs" type="longtext"/>
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

> ⚠️ **Lưu ý 2026-07-08:** Bảng dưới đây dùng tên field theo JSON gốc VNPT trả về (snake_case, ví dụ `object.id`, `object.mrz_valid_score`) — đã đối chiếu với sample thực tế do dev gửi (Section 0.6). Việc map sang tên property Java (`citizenId`, `mrzValidScore`...) là do Jackson deserialize theo naming strategy của DTO `VNPTDataBase64`, không phải tên field JSON gốc. Các dòng có ⚠️ là chỗ bản v2.0 map sai/tham chiếu field không tồn tại.

| `ekyc_attempt_log` column | VNPT JSON field (gốc) | Ghi chú |
|--------------------------|----------------------|---------|
| `vnpt_status_code` | `statusCode` | |
| `vnpt_citizen_id` | `object.id` | ⚠️ sửa 2026-07-08 — field cũ ghi `object.citizenId` không tồn tại |
| `vnpt_old_citizen_id` | `object.citizen_id` | Mới — số CMND cũ, `"-"` nếu không có |
| `vnpt_name` | `object.name` | |
| `vnpt_card_type` | `object.card_type` | |
| `vnpt_citizen_id_prob` | `object.citizen_id_prob` | ⚠️ Cẩn thận: đây KHÔNG phải confidence của field `id` (số CCCD) — VNPT dùng `citizen_id_prob` cho field `citizen_id` (CMND cũ). Confidence của `id` là mảng `object.id_probs` (theo từng ký tự) — chưa có cột riêng, xem trong `vnpt_raw_data` khi cần |
| `vnpt_mrz_valid_score` | `object.mrz_valid_score` | Thang điểm thực tế 0-100 (sample trả `100`), không phải 0-10 |
| `vnpt_is_tampered` | `object.tampering.is_legal` | |
| `vnpt_id_fake_warning` | `object.id_fake_warning` | |
| `vnpt_id_fake_prob` | `object.id_fake_prob` | |
| `vnpt_duplication_warning` | `object.dupplication_warning` | Giữ nguyên chính tả "dupplication" theo VNPT (lỗi chính tả gốc) |
| `vnpt_dob_fake_warning` | `object.dob_fake_warning` | |
| `vnpt_address_fake_warning` | `object.address_fake_warning` | |
| `vnpt_issuedate_fake_warning` | `object.issuedate_fake_warning` | |
| `vnpt_name_fake_warning` | `object.name_fake_warning` | |
| `vnpt_front_recaptured` | `object.checking_result_front.recaptured_result` | |
| `vnpt_front_edited_prob` | `object.checking_result_front.edited_prob` | |
| `vnpt_front_photocopied` | `object.checking_result_front.check_photocopied_result` | |
| `vnpt_back_recaptured` | `object.checking_result_back.recaptured_result` | |
| `vnpt_back_edited_prob` | `object.checking_result_back.edited_prob` | |
| `vnpt_back_photocopied` | `object.checking_result_back.check_photocopied_result` | |
| `vnpt_front_blur_score` | `object.quality_front.blur_score` | |
| `vnpt_front_luminance_score` | `object.quality_front.luminance_score` | |
| `vnpt_back_blur_score` | `object.quality_back.blur_score` | |
| `vnpt_back_luminance_score` | `object.quality_back.luminance_score` | |
| `vnpt_match_id` | `object.match_front_back.match_id` | |
| `vnpt_match_name` | `object.match_front_back.match_name` | |
| `vnpt_match_bod` | `object.match_front_back.match_bod` | |
| `vnpt_match_sex` | `object.match_front_back.match_sex` | ⚠️ sửa 2026-07-08 — cột cũ map `matchFrontBack.matchValidDate`, field này KHÔNG tồn tại; field thật là `match_sex` |
| `vnpt_nationality` | `object.nationality` | |
| `vnpt_citizen_id_chip` | `object.dict_qr.SoCCCD` | ⚠️ sửa 2026-07-08 — cột cũ map `object.citizenIdChip` (không tồn tại). Giá trị đúng nằm trong QR decode (`dict_qr`), dùng đối chiếu với `vnpt_citizen_id` (OCR mặt thẻ) |
| `vnpt_qr_match_summary` | Tính từ `object.match_qr.*` | Mới — BE tự suy ra: `PASS` nếu cả 4 field con = `"yes"`, ngược lại `FAIL` |
| `mrz_line1` | `object.mrz[0]` | |
| `mrz_line2` | `object.mrz[1]` | |
| `mrz_line3` | `object.mrz[2]` | Mới — dòng họ tên trong MRZ 3 dòng (định dạng TD1) |
| `mrz_overall_prob` | `object.mrz_prob` | |
| `vnpt_raw_data` | Toàn bộ JSON response (serialize) | Bao gồm cả `dict_qr`, `new_post_code`/`post_code` (địa chỉ có cấu trúc), `match_qr` chi tiết — các field này chưa có cột riêng, tra trực tiếp trong raw JSON khi điều tra sâu |

### 0.5 Mapping SDK Log Keys → `ekyc_attempt_log`

Các trường này **không có trong VNPT OCR response** — do SDK trả về qua `EkycBridge` (React Native). Được App gửi lên qua `POST /ekycs/attempt-log` khi lần thử **thất bại trước khi submit** (pre-submit failure).

| `ekyc_attempt_log` column | SDK Log Key | Giá trị |
|--------------------------|------------|---------|
| `liveness_card_front_result` | `LOG_LIVENESS_CARD_FRONT` | `"success"` / `"failure"` (JSON field: `liveness`) |
| `liveness_card_rear_result` | `LOG_LIVENESS_CARD_REAR` | `"success"` / `"failure"` (JSON field: `liveness`) |
| `liveness_face_result` | `LOG_LIVENESS_FACE` | `"success"` / `"failure"` (JSON field: `liveness`) |
| `face_mask_result` | `LOG_MASK_FACE` | `"success"` nếu `masked = "no"`, `"failure"` nếu `masked = "yes"` |
| `liveness_card_front_fake_prob` | `LOG_LIVENESS_CARD_FRONT` | `fake_liveness_prob` (0–1) — ⚠️ sửa 2026-07-08, xem note bên dưới |
| `liveness_card_front_fake_print_prob` | `LOG_LIVENESS_CARD_FRONT` | `fake_print_photo_prob` (0–1) |
| `liveness_card_front_face_swapping` | `LOG_LIVENESS_CARD_FRONT` | `face_swapping` (boolean) — mới 2026-07-08 |
| `liveness_card_rear_fake_prob` | `LOG_LIVENESS_CARD_REAR` | `fake_liveness_prob` (0–1) |
| `liveness_card_rear_fake_print_prob` | `LOG_LIVENESS_CARD_REAR` | `fake_print_photo_prob` (0–1) |
| `liveness_card_rear_face_swapping` | `LOG_LIVENESS_CARD_REAR` | `face_swapping` (boolean) — mới 2026-07-08 |
| `liveness_face_multiple_faces` | `LOG_LIVENESS_FACE` | `multiple_faces_details` → true nếu bất kỳ face nào = true — mới 2026-07-08 |
| `face_compare_msg` | `LOG_COMPARE` JSON | `"MATCH"` / `"NOMATCH"` |
| `face_compare_prob` | `LOG_COMPARE` JSON | `prob` — similarity score, sample thực tế trả thang **0–100** (VD `98.596`), không phải 0–1 như comment cũ. BE cần chuẩn hóa đơn vị khi lưu (khuyến nghị: giữ nguyên thang 0–100 để khớp với `matchingRate` đã dùng ở `/lotte/ekycs`, xem note reuse bên dưới) |
| `face_compare_match_warning` | `LOG_COMPARE` JSON | `match_warning` (`"yes"`/`"no"`) — mới 2026-07-08 |
| `face_compare_multiple_faces` | `LOG_COMPARE` JSON | `multiple_faces_details` → true nếu bất kỳ face nào = true — mới 2026-07-08 |
| `image_front_url` | `LOG_PATH_IMAGE_FRONT` → upload S3 | URL sau khi BE upload |
| `image_back_url` | `LOG_PATH_IMAGE_BACK` → upload S3 | URL sau khi BE upload |
| `mrz_cross_check` | App tính toán | `PASS` / `PARTIAL_FAIL` / `FAIL` / `SKIPPED` |
| `mrz_check_id` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |
| `mrz_check_dob` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |
| `mrz_check_gender` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |
| `mrz_check_expiry` | App so MRZ parsed vs OCR | `MATCH` / `MISMATCH` |
| `vnpt_ocr_log_id` | `logID` trong `LOG_OCR` | App đã có field này (`ocrLogId`) sẵn trong luồng `/lotte/ekycs` hiện tại — gửi kèm |
| `vnpt_card_liveness_front_log_id` | `logID` trong `LOG_LIVENESS_CARD_FRONT` | App đã có sẵn (`cardLivenessLogId`) |
| `vnpt_card_liveness_rear_log_id` | `logID` trong `LOG_LIVENESS_CARD_REAR` | App đã có sẵn (`cardRearLogId`) |
| `vnpt_face_liveness_log_id` | `logID` trong `LOG_LIVENESS_FACE` | App đã có sẵn (`faceLivenessLogId`) |
| `vnpt_face_compare_log_id` | `logID` trong `LOG_COMPARE` | App đã có sẵn (`compareLogId`) |
| `vnpt_face_mask_log_id` | `logID` trong `LOG_MASK_FACE` | App đã có sẵn (`faceMaskLogId`) |

> ⚠️ **Gap đã fix (2026-07-08) — đối chiếu với sample log thực tế do dev gửi:**
> 1. **`fake_liveness_prob`/`fake_print_photo_prob` nằm sai vị trí trong spec.** Bản v2.0 ghi 2 field này lấy từ `LOG_LIVENESS_FACE`, nhưng sample thực tế (2 mẫu OCR CCCD 2 mặt) cho thấy chúng nằm ở `LOG_LIVENESS_CARD_FRONT`/`LOG_LIVENESS_CARD_REAR`, KHÔNG có trong `LOG_LIVENESS_FACE`. Đã tách thành 6 cột riêng theo từng loại check (front/rear × fake_prob/fake_print_prob + face_swapping) thay vì 2 cột dùng chung — tránh ghi đè lẫn nhau.
> 2. **`face_swapping`/`face_swapping_prob`** (phát hiện đổi mặt/deepfake) — field hoàn toàn mới, có trong `LOG_LIVENESS_CARD_FRONT`/`REAR`, spec cũ không có cột nào.
> 3. **`multiple_faces`/`multiple_faces_details`** — cả `LOG_LIVENESS_FACE` và `LOG_COMPARE` đều trả cờ phát hiện nhiều khuôn mặt trong ảnh (dấu hiệu gian lận rõ ràng: ảnh có 2 người, hoặc dùng ảnh người khác chèn vào). Spec cũ không capture.
> 4. **6 VNPT logID** (`ocrLogId`, `cardLivenessLogId`, `cardRearLogId`, `compareLogId`, `faceLivenessLogId`, `faceMaskLogId`) — các field này **đã chạy thật trong production** trên `EKycAddReq` (gửi kèm `/lotte/ekycs`), nhưng chưa từng được đưa vào schema `ekyc_attempt_log`. Rất có giá trị để tra soát chéo với VNPT khi có tranh chấp/audit — App không cần tính toán gì thêm, chỉ cần gửi kèm các ID đã có sẵn.
> 5. **Reuse `matchingRate` có sẵn:** App hiện đã tính `matchingRate` (= `LOG_COMPARE.object.prob`) để gửi Lotte qua `vnptPoint` trong luồng `/lotte/ekycs`. Khi gọi `/ekycs/attempt-log`, App nên gửi lại đúng giá trị này cho `faceCompareProb` thay vì tính lại từ đầu — tránh rủi ro 2 nguồn lệch nhau.
>
> ⚠️ **Gap đã fix (2026-07-01):** Các cột trên chỉ lưu field **đã được App cherry-pick** từ mỗi SDK log key (VD: chỉ lấy `liveness` string và `fakeLivenessProb` từ `LOG_LIVENESS_FACE`, bỏ qua các field khác SDK có thể trả về). Nếu VNPT SDK trả thêm field mới hoặc field ngoài danh sách đã map, dữ liệu đó **mất vĩnh viễn** — không audit được.
>
> **Fix:** thêm cột `sdk_raw_logs` (LONGTEXT) — App gửi **nguyên văn, không lọc field**, JSON gộp của toàn bộ 7 log key còn lại:
> ```json
> {
>   "livenessCardFront": { /* toàn bộ nội dung LOG_LIVENESS_CARD_FRONT SDK trả về */ },
>   "livenessCardRear":  { /* toàn bộ nội dung LOG_LIVENESS_CARD_REAR */ },
>   "livenessFace":      { /* toàn bộ nội dung LOG_LIVENESS_FACE */ },
>   "maskFace":          { /* toàn bộ nội dung LOG_MASK_FACE */ },
>   "compare":           { /* toàn bộ nội dung LOG_COMPARE */ },
>   "pathImageFront":    "...",
>   "pathImageBack":     "..."
> }
> ```
> Kết hợp `vnpt_raw_data` (LOG_OCR) + `sdk_raw_logs` (7 key còn lại) = **toàn bộ output SDK VNPT trả về** được lưu nguyên vẹn cho mục đích audit, độc lập với các cột đã parse. Xem Section 7.2 cho request field `sdkRawLogs`.

**Nguồn dữ liệu theo loại lần thử:**

| Loại lần thử | image_front/back_url | liveness_* / face_compare_* |
|-------------|---------------------|------------------------------|
| **Post-submit** (App gọi `/lotte/ekycs`) | BE extract từ `VNPTDataBase64.Img.imgFront/imgBack`, upload S3 | App gửi kèm trong request body |
| **Pre-submit** (App log riêng qua `/ekycs/attempt-log`) | App gửi `imageFrontBase64` / `imageBackBase64`, BE upload S3 | App gửi kèm trong request body |

---

## 0.6 Sample thực tế (đã ẩn danh) — cho dev tham khảo

> Nguồn: dev gửi 2 bộ log thật từ production (2026-07-02), review 2026-07-08. Tên, số CCCD, ngày sinh, địa chỉ, ảnh path, chữ ký số (`dataSign`), payload base64 (`dataBase64`) đã được **thay bằng giá trị giả** — cấu trúc field, kiểu dữ liệu và các field ít gặp (đặc biệt `match_qr`, `dict_qr`, `face_swapping`, `multiple_faces`) giữ nguyên 100% so với bản gốc để dev đối chiếu khi implement `buildAttemptLog()`.

### 0.6.1 `LOG_OCR` — kết quả OCR CCCD 2 mặt (nguồn của `vnptRawData` / cột `vnpt_*`)

```json
{
  "logID": "<vnpt-log-id>",
  "statusCode": 200,
  "message": "IDG-00000000",
  "server_version": "1.6.16",
  "challengeCode": "INNOVATIONCENTER",
  "imgs": {
    "img_front": "<bucket-path>/front.jpg",
    "img_back": "<bucket-path>/back.jpg"
  },
  "object": {
    "msg": "OK",
    "msg_back": "OK",
    "type_id": 6,
    "back_type_id": 6,
    "card_type": "CĂN CƯỚC",

    "id": "001099999999",
    "id_probs": [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0],
    "citizen_id": "-",
    "citizen_id_prob": 0,

    "name": "NGUYEN VAN A",
    "name_prob": 1,
    "name_fake_warning": "real",
    "name_fake_warning_prob": 0.2,

    "gender": "Nam",
    "birth_day": "01/01/1990",
    "birth_day_prob": 0.999,
    "nationality": "Việt Nam",
    "issue_date": "18/12/2024",
    "issue_date_prob": 0.994,
    "issue_place": "BỘ CÔNG AN",
    "issue_place_prob": 0.999,
    "expiry_date": "01/01/2039",
    "expiry_date_prob": 0.999,
    "valid_date": "01/01/2039",
    "valid_date_prob": 0.999,
    "expire_warning": "no",
    "back_expire_warning": "no",

    "mrz": [
      "IDVNM0099999999001099999999<<4",
      "9001011M3901011VNM<<<<<<<<<<<4",
      "NGUYEN<<VAN<A<<<<<<<<<<<<<<<<<"
    ],
    "mrz_prob": 0.994,
    "mrz_probs": [0.996, 0.999, 0.986],
    "mrz_valid_score": 100,

    "tampering": { "is_legal": "yes", "warning": [] },
    "id_fake_warning": "no",
    "id_fake_prob": 0,
    "dupplication_warning": false,
    "dob_fake_warning": false,
    "address_fake_warning": false,
    "issuedate_fake_warning": false,
    "general_warning": [],
    "corner_warning": "no",
    "back_corner_warning": "no",

    "checking_result_front": {
      "recaptured_result": "0", "recaptured_prob": 0.106,
      "edited_result": "0", "edited_prob": 0.151,
      "check_photocopied_result": "0", "check_photocopied_prob": 0,
      "corner_cut_result": "0", "corner_cut_prob": [0.096, 0.096, 0.096, 0.096]
    },
    "checking_result_back": {
      "recaptured_result": "0", "recaptured_prob": 0.085,
      "edited_result": "0", "edited_prob": 0,
      "check_photocopied_result": "0", "check_photocopied_prob": 0,
      "corner_cut_result": "0", "corner_cut_prob": [0.100, 0.100, 0.100, 0.100]
    },
    "quality_front": {
      "blur_score": 0.208, "luminance_score": 0.675, "bright_spot_score": 0,
      "resolution": [440, 704],
      "final_result": { "blurred_likelihood": "unlikely", "low_resolution_likelihood": "unlikely", "bad_luminance_likelihood": "unlikely", "bright_spot_likelihood": "unlikely" }
    },
    "quality_back": {
      "blur_score": 0.048, "luminance_score": 0.667, "bright_spot_score": 0,
      "resolution": [423, 678],
      "final_result": { "blurred_likelihood": "unlikely", "low_resolution_likelihood": "unlikely", "bad_luminance_likelihood": "unlikely", "bright_spot_likelihood": "unlikely" }
    },

    "match_front_back": { "match_id": "yes", "match_name": "yes", "match_bod": "yes", "match_sex": "yes" },

    "match_qr": {
      "match_id_qr": "yes", "match_name_qr": "yes",
      "match_issue_date_qr": "yes", "match_bod_qr": "yes"
    },
    "dict_qr": {
      "SoCCCD": "001099999999", "SoCMND": "-",
      "name": "Nguyễn Văn A", "gender": "Nam",
      "birth_day": "01011990", "issue_date": "18122024",
      "recent_location": "<địa chỉ đã ẩn danh>"
    },

    "new_post_code": [
      { "type": "address", "city": ["01", "Thành phố Hà Nội", 1], "district": ["", "", 1], "ward": ["00004", "Phường Ba Đình", 1], "detail": "<đã ẩn danh>" },
      { "type": "hometown", "city": ["01", "Thành phố Hà Nội", 1], "district": ["", "", 1], "ward": ["00004", "Phường Ba Đình", 1], "detail": "" }
    ],

    "origin_location": "<đã ẩn danh>",
    "recent_location": "<đã ẩn danh>",
    "place_birth": "<đã ẩn danh>",
    "features": "-",
    "features_prob": 0
  }
}
```

**Điểm cần chú ý khi dev đọc payload này:**

- `id` = số CCCD hiện tại (dùng cho `vnpt_citizen_id`) — **khác** `citizen_id` (số CMND cũ, `"-"` nếu chưa từng có, dùng cho `vnpt_old_citizen_id`).
- `mrz` có **3 phần tử** (2 dòng dữ liệu + 1 dòng họ tên) — parse đủ cả 3, đừng giả định chỉ có 2 như tài liệu SDK cũ.
- `match_front_back` không có `match_valid_date` — trường thật là `match_sex`.
- `match_qr` + `dict_qr` là 2 khối hoàn toàn tách biệt với `match_front_back`: một cái so QR-vs-OCR, một cái là data thô decode từ QR — cả hai đều nằm trong `vnptRawData`, BE tự parse, App không cần gửi thêm gì.
- `new_post_code` cấu trúc lồng nhau theo `[city, district, ward]` dạng mảng `[code, name, flag]` — nếu chỉ cần lưu địa chỉ dạng text, ưu tiên dùng field song song `recent_location`/`origin_location` (string phẳng), còn `new_post_code`/`post_code` để trong `vnpt_raw_data` phục vụ tra cứu sâu.

### 0.6.2 SDK log keys còn lại (nguồn của `sdkRawLogs` / các cột liveness, face compare)

```json
{
  "livenessCardFront": {
    "logID": "<vnpt-log-id>",
    "statusCode": 200,
    "object": {
      "liveness": "success",
      "liveness_msg": "Giấy tờ thật",
      "fake_liveness": false,
      "fake_liveness_prob": 0.106,
      "fake_print_photo": false,
      "fake_print_photo_prob": 0,
      "face_swapping": false,
      "face_swapping_prob": 0.036
    }
  },
  "livenessCardRear": {
    "logID": "<vnpt-log-id>",
    "statusCode": 200,
    "object": {
      "liveness": "success",
      "liveness_msg": "Giấy tờ thật",
      "fake_liveness": false,
      "fake_liveness_prob": 0.086,
      "fake_print_photo": false,
      "fake_print_photo_prob": 0,
      "face_swapping": false,
      "face_swapping_prob": 0
    }
  },
  "livenessFace": {
    "logID": "<vnpt-log-id>",
    "statusCode": 200,
    "object": {
      "liveness": "success",
      "liveness_msg": "Người thật",
      "liveness_prob": 0.2,
      "age": 29,
      "gender": "Nam",
      "is_eye_open": "yes",
      "blur_face": "no",
      "blur_face_score": 0,
      "background_warning": "no",
      "multiple_faces_details": { "multiple_face_1": false, "multiple_face_2": false }
    }
  },
  "maskFace": {
    "logID": "<vnpt-log-id>",
    "statusCode": 200,
    "object": { "masked": "no" }
  },
  "compare": {
    "logID": "<vnpt-log-id>",
    "statusCode": 200,
    "object": {
      "msg": "MATCH",
      "result": "Khuôn mặt khớp 98.596%",
      "prob": 98.596,
      "match_warning": "no",
      "multiple_faces": false,
      "multiple_faces_details": { "multiple_face_1": false, "multiple_face_2": false }
    }
  },
  "pathImageFront": "file:///.../ImageCropedFront.png",
  "pathImageBack": "file:///.../ImageCropedBack.png"
}
```

**Điểm cần chú ý:**

- `fake_liveness_prob`/`fake_print_photo_prob`/`face_swapping` chỉ xuất hiện ở `livenessCardFront`/`livenessCardRear` — **không có** trong `livenessFace` (bản spec trước đây giả định sai vị trí này).
- `livenessFace.liveness_prob` là xác suất liveness **thật** của khuôn mặt (khác ý nghĩa với `fake_liveness_prob` của 2 mặt thẻ) — không nhầm 2 field cùng tên gốc "liveness prob" nhưng khác ngữ nghĩa.
- `compare.prob` trả về thang **0–100** (không phải 0–1) — khớp với `matchingRate` mà App đã tính sẵn cho luồng `/lotte/ekycs`, nên dùng lại giá trị đó thay vì tính lại.
- `multiple_faces_details` xuất hiện ở cả `livenessFace` và `compare` — đều là cờ chống gian lận (ảnh có nhiều hơn 1 khuôn mặt), nên map vào 2 cột riêng (`liveness_face_multiple_faces`, `face_compare_multiple_faces`) vì có thể chỉ 1 trong 2 bước phát hiện được.
- Mỗi khối đều có `logID` riêng — map đúng theo Section 0.5 vào 6 cột `vnpt_*_log_id`.

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

    @Column(name = "vnpt_old_citizen_id")
    private String vnptOldCitizenId;   // object.citizen_id — CMND cũ, "-" nếu không có

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

    @Column(name = "vnpt_match_sex")
    private String vnptMatchSex;   // object.match_front_back.match_sex — thay cho "matchValidDate" không tồn tại

    // Extended OCR Fields
    @Column(name = "vnpt_nationality")
    private String vnptNationality;

    @Column(name = "vnpt_citizen_id_chip")
    private String vnptCitizenIdChip;   // object.dict_qr.SoCCCD (QR/chip decode)

    // QR Cross-Check
    @Column(name = "vnpt_qr_match_summary")
    private String vnptQrMatchSummary;   // PASS / FAIL / SKIPPED — BE tự suy ra từ object.match_qr.*

    // MRZ Raw Data
    @Column(name = "mrz_line1")
    private String mrzLine1;

    @Column(name = "mrz_line2")
    private String mrzLine2;

    @Column(name = "mrz_line3")
    private String mrzLine3;   // object.mrz[2] — dòng họ tên (CCCD gắn chip có 3 dòng MRZ)

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

    // Card Liveness Fraud Detail — tách theo mặt (xem note 2026-07-08 ở Section 0.5)
    @Column(name = "liveness_card_front_fake_prob")
    private Double livenessCardFrontFakeProb;

    @Column(name = "liveness_card_front_fake_print_prob")
    private Double livenessCardFrontFakePrintProb;

    @Column(name = "liveness_card_front_face_swapping")
    private Boolean livenessCardFrontFaceSwapping;

    @Column(name = "liveness_card_rear_fake_prob")
    private Double livenessCardRearFakeProb;

    @Column(name = "liveness_card_rear_fake_print_prob")
    private Double livenessCardRearFakePrintProb;

    @Column(name = "liveness_card_rear_face_swapping")
    private Boolean livenessCardRearFaceSwapping;

    @Column(name = "liveness_face_multiple_faces")
    private Boolean livenessFaceMultipleFaces;

    // Face Compare
    @Column(name = "face_compare_msg")
    private String faceCompareMsg;   // MATCH / NOMATCH

    @Column(name = "face_compare_prob")
    private Double faceCompareProb;   // thang 0-100 theo VNPT thực tế (không phải 0-1)

    @Column(name = "face_compare_match_warning")
    private String faceCompareMatchWarning;

    @Column(name = "face_compare_multiple_faces")
    private Boolean faceCompareMultipleFaces;

    // VNPT Log IDs — đối chiếu chéo khi audit/tranh chấp
    @Column(name = "vnpt_ocr_log_id")
    private String vnptOcrLogId;

    @Column(name = "vnpt_card_liveness_front_log_id")
    private String vnptCardLivenessFrontLogId;

    @Column(name = "vnpt_card_liveness_rear_log_id")
    private String vnptCardLivenessRearLogId;

    @Column(name = "vnpt_face_liveness_log_id")
    private String vnptFaceLivenessLogId;

    @Column(name = "vnpt_face_compare_log_id")
    private String vnptFaceCompareLogId;

    @Column(name = "vnpt_face_mask_log_id")
    private String vnptFaceMaskLogId;

    // Image Storage (S3 / MinIO)
    @Column(name = "image_front_url")
    private String imageFrontUrl;

    @Column(name = "image_back_url")
    private String imageBackUrl;

    // Raw Data — audit đầy đủ, không qua parse/lọc field
    @Lob
    @Column(name = "vnpt_raw_data")
    private String vnptRawData;   // Raw JSON của LOG_OCR (VNPT server response)

    @Lob
    @Column(name = "sdk_raw_logs")
    private String sdkRawLogs;    // Raw JSON gộp của 7 SDK log key còn lại (liveness x3, mask, compare, image paths)

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
                // ⚠️ 2026-07-08: obj.getId() — KHÔNG dùng obj.getCitizenId() (field không tồn tại trong response thật)
                log.setVnptCitizenId(obj.getId());
                log.setVnptOldCitizenId(obj.getCitizenIdOld()); // map JSON field "citizen_id" — CMND cũ, "-" nếu không có
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
                    // ⚠️ 2026-07-08: field thật là match_sex, KHÔNG có match_valid_date
                    log.setVnptMatchSex(obj.getMatchFrontBack().getMatchSex());
                }

                // Extended OCR
                log.setVnptNationality(obj.getNationality());
                // ⚠️ 2026-07-08: obj.getCitizenIdChip() không tồn tại — giá trị đúng nằm trong QR decode
                if (obj.getDictQr() != null) {
                    log.setVnptCitizenIdChip(obj.getDictQr().getSoCCCD());
                }

                // QR Cross-Check — mới 2026-07-08, BE tự suy ra PASS/FAIL, App không cần gửi thêm
                if (obj.getMatchQr() != null) {
                    boolean allYes = "yes".equals(obj.getMatchQr().getMatchIdQr())
                        && "yes".equals(obj.getMatchQr().getMatchNameQr())
                        && "yes".equals(obj.getMatchQr().getMatchIssueDateQr())
                        && "yes".equals(obj.getMatchQr().getMatchBodQr());
                    log.setVnptQrMatchSummary(allYes ? "PASS" : "FAIL");
                } else {
                    log.setVnptQrMatchSummary("SKIPPED");
                }
            }

            // ── MRZ Raw Data ──
            // vnpt.getMrz() trả về List<String> — CCCD gắn chip trả 3 dòng (2 dòng dữ liệu + 1 dòng họ tên)
            if (vnpt.getMrz() != null && vnpt.getMrz().size() >= 2) {
                log.setMrzLine1(vnpt.getMrz().get(0));
                log.setMrzLine2(vnpt.getMrz().get(1));
            }
            if (vnpt.getMrz() != null && vnpt.getMrz().size() >= 3) {
                log.setMrzLine3(vnpt.getMrz().get(2));
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

> ⚠️ **Quan trọng — khách chưa mở TK thành công vẫn phải tìm được.**
> `ekyc_attempt_log` là nguồn dữ liệu **chính** cho search — mọi lần thử (pass hay fail, kể cả pre-submit fail chưa từng gọi tới Lotte) đều tạo 1 row ở đây với `identifier_id`/`phone_no`. Bảng `e_kyc` chỉ **join thêm** khi có (không phải nguồn bắt buộc).
> Trước đây search giả định luôn có `e_kyc` để lấy `fullName`/`accountStatus` — sai với case khách fail ngay từ OCR/liveness (không có `e_kyc` row nào, vì `CustomEKycService` chỉ chạy khi App gọi `/lotte/ekycs`, mà pre-submit fail thì App không gọi).

```
GET /api/admin/ekyc/attempts/search
  ?identifierId={cccd}   (optional)
  &phoneNo={phone}       (optional)
```

**Query logic (BE):**

```sql
-- 1. Lấy toàn bộ attempts theo identifierId/phoneNo — LUÔN có data nếu user đã thử ít nhất 1 lần
SELECT * FROM ekyc_attempt_log
WHERE identifier_id = :identifierId OR phone_no = :phoneNo
ORDER BY attempt_number DESC

-- 2. LEFT JOIN e_kyc qua final_ekyc_id của lần thử gần nhất (nullable — có thể chưa có)
SELECT * FROM e_kyc WHERE id = (SELECT final_ekyc_id FROM ekyc_attempt_log WHERE ... ORDER BY attempt_number DESC LIMIT 1)
```

**`accountStatus` — suy ra từ `outcome` của lần thử gần nhất** (không cần cột riêng, tái dùng enum outcome đã có):

| `outcome` (lần thử gần nhất) | `accountStatus` trả về | Ý nghĩa |
|-------------------------------|------------------------|---------|
| `SUCCESS` | `APPROVED` | TK đã mở — có `e_kyc` + `accountNumber` |
| `LOTTE_REJECTED` | `REJECTED` | Đã gửi Lotte nhưng bị từ chối |
| `USER_ABANDONED` | `ABANDONED` | Đã APPROVED về mặt Lotte nhưng chưa ký HĐ trong 48h |
| `VNPT_FAILED` / `MRZ_FAILED` / `FACE_COMPARE_FAILED` | `NOT_SUBMITTED` | **Chưa từng chạm tới Lotte** — fail ngay ở bước SDK phía App |

**Response 200 — khách đã mở TK thành công:**

```json
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

**Response 200 — khách CHƯA mở TK (chỉ có attempt fail, không có `e_kyc` row nào):**

```json
{
  "identifierId": "038yyyyyyyy",
  "fullName": "NGUYEN VAN B",
  "phoneNo": "09yyyyyyyy",
  "totalAttempts": 2,
  "successfulAttempt": null,
  "accountNumber": null,
  "accountStatus": "NOT_SUBMITTED",
  "firstAttemptAt": "2026-06-30T14:02:00+07:00",
  "accountOpenedAt": null,
  "lastFailureStep": "VNPT_LIVENESS",
  "lastFailureMessage": "Liveness khuôn mặt thất bại — nghi ngờ ảnh giả"
}
```

**Field mapping khi không có `e_kyc`:**

| Field | Nguồn khi có `e_kyc` | Nguồn fallback khi KHÔNG có `e_kyc` |
|-------|---------------------|--------------------------------------|
| `fullName` | `e_kyc.full_name` | `ekyc_attempt_log.vnpt_name` (tên OCR đọc được ở lần thử gần nhất có OCR data) — `null` nếu OCR cũng chưa từng chạy được |
| `phoneNo` | `e_kyc.phone_no` | `ekyc_attempt_log.phone_no` |
| `accountNumber` / `accountOpenedAt` | `e_kyc.account_number` / `e_kyc.created_at` | `null` |
| `lastFailureStep` / `lastFailureMessage` | — (không cần khi APPROVED) | `ekyc_attempt_log.failure_step` / `.failure_message` của lần thử gần nhất |

> Nếu `vnpt_name` cũng null (OCR chưa bao giờ đọc được — VD: fail vì blur ảnh trước khi VNPT kịp OCR), FE hiển thị `identifierId` là định danh chính, không có tên.

### 4.1b Danh sách theo tab — Đã mở TK / Chưa mở TK

> Bổ sung 2026-07-01 — Admin page cần browse theo danh sách (không chỉ tra 1 khách), tách 2 tab để Ops dễ track: khách đã mở TK thành công vs. khách chưa mở TK thành công (dù ở bất kỳ lý do nào: NOT_SUBMITTED / REJECTED / PENDING / ABANDONED).

```
GET /api/admin/ekyc/attempts/list
  ?accountStatus={APPROVED|NOT_APPROVED}   (bắt buộc — FE gửi theo tab đang chọn)
  &keyword={text}                          (optional — match identifierId/phoneNo/fullName)
  &fromDate={date}&toDate={date}           (optional — filter theo attempt_at)
  &page=0&size=20                          (pagination)
```

**Query logic:** `accountStatus=NOT_APPROVED` là giá trị tiện ích — BE hiểu là "outcome của lần thử gần nhất KHÁC `SUCCESS`" (bao gồm cả `NOT_SUBMITTED`, `REJECTED`, `ABANDONED`, và case `e_kyc.status = PENDING` chờ admin duyệt thủ công — VD do fraud flags).

```sql
-- accountStatus = APPROVED
SELECT DISTINCT identifier_id FROM ekyc_attempt_log WHERE final_ekyc_id IS NOT NULL AND outcome = 'SUCCESS' ...

-- accountStatus = NOT_APPROVED
SELECT DISTINCT identifier_id FROM ekyc_attempt_log a
WHERE NOT EXISTS (
  SELECT 1 FROM ekyc_attempt_log a2 WHERE a2.identifier_id = a.identifier_id AND a2.outcome = 'SUCCESS'
) ...
```

**Response 200:**

```json
{
  "totalCount": 3,
  "customers": [
    {
      "identifierId": "041205019876",
      "fullName": "LÊ THỊ HOA",
      "phoneNo": "0987654321",
      "totalAttempts": 2,
      "accountStatus": "NOT_SUBMITTED",
      "accountNumber": null,
      "lastFailureStep": "VNPT_LIVENESS",
      "lastFailureMessage": "Xác minh khuôn mặt trực tiếp thất bại 2 lần",
      "lastUpdatedAt": "2026-06-30T14:02:00+07:00"
    }
  ]
}
```

> Mỗi item trong `customers[]` có cùng shape với response của `/search` (Section 4.1) — chỉ thêm `lastUpdatedAt` để sort danh sách theo hoạt động gần nhất. FE dùng chung 1 component render row cho cả 2 tab.

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
    "oldCitizenId": "-",
    "name": "NGUYEN VAN A",
    "cardType": "CC",
    "citizenIdProb": 0.95,
    "mrzValidScore": 100,
    "nationality": "Việt Nam",
    "citizenIdChip": "038xxxxxxxx",
    "qrMatchSummary": "PASS"
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
    "matchSex": "MATCH"
  },
  "livenessResults": {
    "cardFrontResult": "success",
    "cardRearResult": "success",
    "faceResult": "failure",
    "maskResult": "success",
    "cardFrontFakeProb": 0.12,
    "cardFrontFakePrintProb": 0.05,
    "cardFrontFaceSwapping": false,
    "cardRearFakeProb": 0.09,
    "cardRearFakePrintProb": 0,
    "cardRearFaceSwapping": false,
    "faceMultipleFaces": false
  },
  "faceCompare": {
    "msg": "MATCH",
    "prob": 91.0,
    "matchWarning": "no",
    "multipleFaces": false
  },
  "vnptLogIds": {
    "ocr": "2076b017-7601-11f1-aa3e-475548588d85-4d2d722b-Zuulserver",
    "cardLivenessFront": "1b9acacc-7601-11f1-bdd1-1f31c32b8112-702fe89a-Zuulserver",
    "cardLivenessRear": "20768948-7601-11f1-ac87-b73f3ca2f7f2-6636f4df-Zuulserver",
    "faceLiveness": "23bf2e86-7602-11f1-aa3e-c122f0288833-2fc5eb94-Zuulserver",
    "faceCompare": "23c018e1-7602-11f1-ac87-75a8dc21ac2c-c187695f-Zuulserver",
    "faceMask": "23bc6f8c-7602-11f1-bd72-098ea85a785c-060a2def-Zuulserver"
  },
  "images": {
    "frontUrl": "https://minio.example.com/ekyc/038xxx/attempt-1-front.jpg",
    "backUrl": "https://minio.example.com/ekyc/038xxx/attempt-1-back.jpg"
  },
  "mrz": {
    "line1": "IDVNM030207010063<<<<<<<<<<<<<<<",
    "line2": "0301230M3001158VNM<<<<<<<<<<<<<<4",
    "line3": "NGUYEN<<VAN<A<<<<<<<<<<<<<<<<<",
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

> Cập nhật 2026-07-08: `matchValidDate` → `matchSex` (field cũ không tồn tại), `fakeLivenessProb`/`fakePrintPhotoProb` tách theo mặt trước/sau, thêm `qrMatchSummary`, `faceSwapping`, `multipleFaces`, và object `vnptLogIds` mới — xem lý do ở Section 0.4/0.5.

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

        // Lưu nguyên văn 7 SDK log key còn lại — KHÔNG parse, KHÔNG lọc field.
        // Đảm bảo audit đầy đủ dù App gửi field mà BE chưa có cột riêng để map.
        if (StringUtils.isNotBlank(req.getSdkRawLogs())) {
            log.setSdkRawLogs(req.getSdkRawLogs());
        }

        // Map MRZ fields từ request (SDK-only, không có trong rawData)
        log.setMrzLine1(req.getMrzLine1());
        log.setMrzLine2(req.getMrzLine2());
        log.setMrzLine3(req.getMrzLine3());   // mới 2026-07-08 — dòng họ tên MRZ
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

        // Card liveness fraud detail — tách theo mặt (sửa 2026-07-08, xem Section 0.5)
        log.setLivenessCardFrontFakeProb(req.getLivenessCardFrontFakeProb());
        log.setLivenessCardFrontFakePrintProb(req.getLivenessCardFrontFakePrintProb());
        log.setLivenessCardFrontFaceSwapping(req.getLivenessCardFrontFaceSwapping());
        log.setLivenessCardRearFakeProb(req.getLivenessCardRearFakeProb());
        log.setLivenessCardRearFakePrintProb(req.getLivenessCardRearFakePrintProb());
        log.setLivenessCardRearFaceSwapping(req.getLivenessCardRearFaceSwapping());
        log.setLivenessFaceMultipleFaces(req.getLivenessFaceMultipleFaces());

        log.setFaceCompareMsg(req.getFaceCompareMsg());
        log.setFaceCompareProb(req.getFaceCompareProb());
        log.setFaceCompareMatchWarning(req.getFaceCompareMatchWarning());
        log.setFaceCompareMultipleFaces(req.getFaceCompareMultipleFaces());

        // VNPT log IDs — App đã có sẵn trong luồng /lotte/ekycs, chỉ cần gửi kèm (mới 2026-07-08)
        log.setVnptOcrLogId(req.getOcrLogId());
        log.setVnptCardLivenessFrontLogId(req.getCardLivenessFrontLogId());
        log.setVnptCardLivenessRearLogId(req.getCardLivenessRearLogId());
        log.setVnptFaceLivenessLogId(req.getFaceLivenessLogId());
        log.setVnptFaceCompareLogId(req.getFaceCompareLogId());
        log.setVnptFaceMaskLogId(req.getFaceMaskLogId());

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
    String vnptRawData;   // Raw JSON của LOG_OCR (Base64), BE tự parse ra các cột vnpt_*
    String sdkRawLogs;    // Raw JSON gộp 7 SDK log key còn lại — App gửi NGUYÊN VĂN, không tự lọc field
    // MRZ fields
    String mrzLine1; String mrzLine2; String mrzLine3; Double mrzProb; Integer mrzValidScore;
    String mrzCrossCheck; String mrzCheckId; String mrzCheckDob;
    String mrzCheckGender; String mrzCheckExpiry;
    // Liveness & face compare
    String livenessCardFrontResult; String livenessCardRearResult;
    String livenessFaceResult; String faceMaskResult;
    // Card liveness fraud detail — tách theo mặt (xem Section 0.5, sửa 2026-07-08)
    Double livenessCardFrontFakeProb; Double livenessCardFrontFakePrintProb; Boolean livenessCardFrontFaceSwapping;
    Double livenessCardRearFakeProb; Double livenessCardRearFakePrintProb; Boolean livenessCardRearFaceSwapping;
    Boolean livenessFaceMultipleFaces;
    // Face compare
    String faceCompareMsg; Double faceCompareProb;   // faceCompareProb thang 0-100, khớp matchingRate đã dùng ở /lotte/ekycs
    String faceCompareMatchWarning; Boolean faceCompareMultipleFaces;
    // VNPT log IDs — App đã có sẵn trong luồng /lotte/ekycs, gửi kèm để tra soát chéo (mới 2026-07-08)
    String ocrLogId; String cardLivenessFrontLogId; String cardLivenessRearLogId;
    String faceLivenessLogId; String faceCompareLogId; String faceMaskLogId;
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

  // ── VNPT rawData: LOG_OCR nguyên văn (chỉ post-submit) ──
  // Nếu cung cấp: BE tự extract toàn bộ VNPT fields (OCR, fraud, image quality, v.v.)
  // Nếu không có (pre-submit fail): BE chỉ lưu các field được truyền rõ ràng bên dưới
  "vnptRawData":    "<base64 VNPT response — nguyên văn LOG_OCR>",

  // ── sdkRawLogs: 7 SDK log key còn lại, NGUYÊN VĂN — BẮT BUỘC khi log key đã chạy ──
  // App KHÔNG tự lọc/rút gọn field trước khi gửi — gửi toàn bộ object SDK trả về.
  // Mục đích: audit đầy đủ, không phụ thuộc vào việc BE đã map cột riêng cho field đó chưa.
  "sdkRawLogs": "{\"livenessCardFront\":{...},\"livenessCardRear\":{...},\"livenessFace\":{...},\"maskFace\":{...},\"compare\":{...},\"pathImageFront\":\"...\",\"pathImageBack\":\"...\"}",

  // ── MRZ (App tính toán từ SDK) ── — mrzLine3 mới 2026-07-08, CCCD gắn chip trả 3 dòng MRZ
  "mrzLine1":       "IDVNM030207010063<<<<<<<<<<<<<<<",
  "mrzLine2":       "0301230M3001158VNM<<<<<<<<<<<<<<4",
  "mrzLine3":       "NGUYEN<<VAN<A<<<<<<<<<<<<<<<<<",
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

  // Card liveness fraud detail — tách theo mặt (sửa 2026-07-08: fake_liveness_prob/fake_print_photo_prob
  // thực tế nằm ở LOG_LIVENESS_CARD_FRONT/REAR, không phải LOG_LIVENESS_FACE như bản spec trước)
  "livenessCardFrontFakeProb":      0.05,
  "livenessCardFrontFakePrintProb": 0.02,
  "livenessCardFrontFaceSwapping":  false,
  "livenessCardRearFakeProb":       0.04,
  "livenessCardRearFakePrintProb":  0,
  "livenessCardRearFaceSwapping":   false,
  "livenessFaceMultipleFaces":      false,

  "faceCompareMsg":            "MATCH",
  "faceCompareProb":           98.6,
  "faceCompareMatchWarning":   "no",
  "faceCompareMultipleFaces":  false,

  // ── VNPT log IDs — App đã có sẵn (dùng chung với luồng /lotte/ekycs), gửi kèm để tra soát chéo với VNPT ──
  "ocrLogId":              "2076b017-7601-11f1-aa3e-475548588d85-4d2d722b-Zuulserver",
  "cardLivenessFrontLogId":"1b9acacc-7601-11f1-bdd1-1f31c32b8112-702fe89a-Zuulserver",
  "cardLivenessRearLogId": "20768948-7601-11f1-ac87-b73f3ca2f7f2-6636f4df-Zuulserver",
  "faceLivenessLogId":     "23bf2e86-7602-11f1-aa3e-c122f0288833-2fc5eb94-Zuulserver",
  "faceCompareLogId":      "23c018e1-7602-11f1-ac87-75a8dc21ac2c-c187695f-Zuulserver",
  "faceMaskLogId":         "23bc6f8c-7602-11f1-bd72-098ea85a785c-060a2def-Zuulserver",

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
| `sdkRawLogs` | **Bắt buộc** khi App đã nhận được kết quả từ bất kỳ SDK log key nào (liveness/mask/compare) — kể cả khi outcome cuối là `SUCCESS`. Nếu SDK chưa chạy bước nào (fail ngay ở OCR) → có thể để trống. App gửi **nguyên văn** object SDK trả về, không rút gọn field. |
| `mrzValidScore` | Optional. Nếu null → `mrz_cross_check = SKIPPED` |
| `mrzLine3` | Optional nhưng khuyến nghị luôn gửi cùng `mrzLine1`/`mrzLine2` khi SDK trả về 3 dòng MRZ (CCCD gắn chip) — mới 2026-07-08 |
| `ocrLogId`, `cardLivenessFrontLogId`, `cardLivenessRearLogId`, `faceLivenessLogId`, `faceCompareLogId`, `faceMaskLogId` | Optional nhưng khuyến nghị luôn gửi — App đã tính sẵn các ID này cho luồng `/lotte/ekycs`, không tốn thêm chi phí. Không có các ID này thì không tra soát chéo được với VNPT khi có tranh chấp (mới 2026-07-08) |
| Ảnh size | Max 5 MB mỗi ảnh sau base64 decode |
| `sdkRawLogs` size | Max 1 MB sau decode (cảnh báo nếu vượt — không phải lý do reject request) |

---

### 7.4 BE Processing Logic

```
1. Parse JWT → lấy identifierId, phoneNo (không yêu cầu App truyền lại)

2. Tính attempt_number:
   attemptNumber = COUNT(*) FROM ekyc_attempt_log
                   WHERE identifier_id = identifierId + 1

3. Nếu có vnptRawData → parse và extract toàn bộ VNPT fields
   (giống buildAttemptLog() trong Section 2.3 — tái dùng method này)
   → bao gồm cả vnpt_qr_match_summary (BE tự tính từ object.match_qr.*, xem 2.3)
   và vnpt_citizen_id_chip (lấy từ object.dict_qr.SoCCCD, không phải field citizenIdChip)

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
   - vnptRawData = req.vnptRawData (as-is, không transform)
   - sdkRawLogs = req.sdkRawLogs (as-is, không parse/transform — chỉ lưu để audit)
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

**Document Status:** Draft v2.1 | **For:** Backend Dev | **Next Steps:** Implement + unit test (đặc biệt test case QR mismatch, face-swapping detected, multiple-faces detected) + image upload integration test
