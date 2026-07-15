# Backend Specification: eKYC Compliance Journey Log

**Version:** 2.0 | **Date:** 2026-07-15 | **Service:** `ekyc-admin`

> **Revision 2026-07-15 — Redesign toàn diện, thay thế `Journey_API_Reference.md` (v1.0, 2026-07-08):**
> - Đổi granularity: từ **API-call-based** (11 endpoint nghiệp vụ có sẵn, interceptor bắt request/response) sang **screen-based** (App chủ động gọi 1 API log riêng tại mỗi bước, gửi full state màn hình).
> - Payload lưu **đầy đủ** (kể cả base64 ảnh), không chỉ `response_summary` như thiết kế cũ.
> - **Gộp sub-feature 05 (Contract Terms Checkbox Log)** vào đây — step `TERMS_AND_CONDITIONS_CONFIRMATION` đã bao phủ trọn vẹn mục đích của 05 (lưu `isAgree` + timestamp). Sub-feature 05 bị xóa, xem `README.md` Review Log 2026-07-15.
> - Bổ sung 2 step OTP (`EKYC_SEND_OTP`, `EKYC_VERIFY_OTP`) và 1 step ký hợp đồng (`ECONTRACT_SIGN_COMPLETED`, ghi qua webhook FPT — không qua App).
> - Scope vẫn giữ nguyên quyết định 2026-07-06(a): **chỉ giữ hành trình mở tài khoản THÀNH CÔNG**; hành trình vãng lai/chưa hoàn tất (Story 2 trong buổi brainstorm PM) — đã đặt sang Scope 2, chưa thiết kế chi tiết.

---

## 0. Vì sao redesign

Thiết kế cũ (`Journey_API_Reference.md`) dựa trên interceptor bắt 11 API nghiệp vụ có sẵn (EKYC_CREATE, SEND_OTP...) và chỉ lưu `response_summary`. Khi đối chiếu với mẫu log thực tế PM cung cấp (`result.json`/`journey.html`), phát hiện:

1. Nhiều field compliance cần lưu (`occupation`, `tax_cd`, `isAgree` — text điều khoản đầy đủ) **chỉ tồn tại ở local state App**, không đi qua bất kỳ API nghiệp vụ nào cho tới lúc submit cuối — interceptor sẽ không bắt được.
2. Mục tiêu là **bằng chứng pháp lý/compliance** (đủ để tra soát khi có tranh chấp), không phải phân tích — nên cần lưu **nguyên trạng** field khách hàng thấy/nhập tại từng màn hình, không phải tóm tắt.

→ Giải pháp: App gọi 1 endpoint log chuyên dụng tại mỗi màn hình, gửi kèm toàn bộ state hiện tại.

---

## 1. Kiến trúc tổng quan

```
App (nhsv-mts-rn) → mỗi khi khách hoàn tất 1 trong 10 bước đầu (xem Section 2)
       │
       ▼
POST /api/v1/ekycs/journey-log   (service: ekyc-admin)
       │  body: { sessionId, phoneNo?, identifierId?, step, status, payload }
       ▼
Ghi real-time, 1 row / 1 lần gọi, vào bảng ekyc_journey_log
       │
       ├─ Khi step = ACCOUNT_OPENING_COMPLETED → session được giữ vĩnh viễn từ đây
       │
       ▼
Scheduled job (mỗi giờ) → xóa toàn bộ row của session nào
   quá 8h (EKYC_SESSION_ID_EXPIRE_TIME, xem ekyc-admin Constants.java:235)
   chưa có ACCOUNT_OPENING_COMPLETED

─────────────────────────────────────────────────────────────

FPT eContract webhook → EContractCustomServiceImpl.getEContractStatus()
   (đã tồn tại — service nhận callback ký hợp đồng từ FPT)
       │  khi customerSignatueStatus chuyển "signed"
       ▼
Backend TỰ ghi thêm 1 row step = ECONTRACT_SIGN_COMPLETED
   (khóa liên kết: e_kyc_id, không qua App, không qua endpoint trên)

─────────────────────────────────────────────────────────────

Khi cần tra soát (compliance/audit) → chạy export tool (Section 8)
   → query DB theo sessionId/identifierId/eKycId → sinh file HTML tĩnh
```

**Vì sao 2 cơ chế ghi khác nhau trong cùng 1 bảng:**

| | 10 step đầu (App-facing) | `ECONTRACT_SIGN_COMPLETED` |
|---|---|---|
| Ai ghi | App gọi API | Backend tự ghi |
| Vì sao | Data chỉ tồn tại ở local state App (form chưa submit) | Nguồn dữ liệu là webhook FPT — single source of truth, không phụ thuộc App còn mở hay không, thiết bị nào |
| Khóa liên kết | `sessionId` (App generate) | `e_kyc_id` (đã có ổn định từ lúc `ACCOUNT_OPENING_COMPLETED`, không phụ thuộc session/device) |

---

## 2. Danh sách 11 step

| # | `step` | Ai ghi | Nguồn | Ghi chú |
|---|---|---|---|---|
| 1 | `EKYC_SEND_OTP` | App | Bổ sung 2026-07-15 | Gửi OTP xác thực SĐT |
| 2 | `EKYC_VERIFY_OTP` | App | Bổ sung 2026-07-15 | Xác thực OTP thành công |
| 3 | `GO_TO_ID_CARD_GUIDE` | App | result.json (PM cung cấp) | Khách xem hướng dẫn chụp CCCD; nhập SĐT/email/nationality/occupation |
| 4 | `EKYC_FACE_SCAN` | App | result.json | Toàn bộ kết quả VNPT SDK (OCR, liveness, face-compare) — kể cả base64 ảnh |
| 5 | `PERSONAL_INFORMATION` | App | result.json | Thông tin cá nhân xác nhận (birth_dt, address, FATCA...) |
| 6 | `ACCOUNT_INFORMATION` | App | result.json | Loại tài khoản, chi nhánh, margin/derivative flags |
| 7 | `BANK_INFORMATION` | App | result.json | Tài khoản ngân hàng nhận tiền |
| 8 | `INVESTMENT_INFORMATION` | App | result.json | Mục tiêu đầu tư, khẩu vị rủi ro |
| 9 | `TERMS_AND_CONDITIONS_CONFIRMATION` | App | result.json | Đồng ý điều khoản hợp đồng — **thay thế hoàn toàn sub-feature 05 cũ** |
| 10 | `ACCOUNT_OPENING_COMPLETED` | App | result.json | **Mốc chốt** — session được giữ vĩnh viễn kể từ đây |
| 11 | `ECONTRACT_SIGN_COMPLETED` | **Backend** (webhook FPT) | Bổ sung 2026-07-15 | Khách ký hợp đồng điện tử thành công — xem Section 6 |

> Các API query thuần túy của luồng eKYC cũ (bank list, branch list, partner validate, econtract URL polling) **không** log riêng ở tầng này — không phải hành động compliance-relevant của khách hàng, chỉ là dữ liệu hỗ trợ màn hình.

---

## 3. DB Schema & Migration

### 3.1 Bảng mới: `ekyc_journey_log`

```sql
CREATE TABLE ekyc_journey_log (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,

  session_id      VARCHAR(64)  NOT NULL COMMENT 'Định danh 1 hành trình — App generate khi bắt đầu, gửi kèm 10 step đầu. NULL cho step ECONTRACT_SIGN_COMPLETED (dùng e_kyc_id).',
  phone_no        VARCHAR(20)           COMMENT 'Khóa phụ trước khi có eKycId',
  identifier_id   VARCHAR(20)           COMMENT 'Số CCCD — có từ step EKYC_FACE_SCAN trở đi',
  e_kyc_id        BIGINT                COMMENT 'FK → e_kyc.id. NULL cho tới step ACCOUNT_OPENING_COMPLETED. Là khóa liên kết chính cho step ECONTRACT_SIGN_COMPLETED.',

  step            VARCHAR(50)  NOT NULL COMMENT 'Enum theo 11 step — xem Section 2',
  status          VARCHAR(20)  NOT NULL COMMENT 'SUCCESS / FAILED',
  payload         LONGTEXT     NOT NULL COMMENT 'Toàn bộ field App/webhook gửi cho step này — giữ nguyên, kể cả base64 ảnh',

  created_at      DATETIME     NOT NULL COMMENT 'Timestamp ghi log — real-time, không buffer',

  INDEX idx_journey_session    (session_id),
  INDEX idx_journey_identifier (identifier_id),
  INDEX idx_journey_phone      (phone_no),
  INDEX idx_journey_ekyc       (e_kyc_id),
  INDEX idx_journey_step       (step),
  INDEX idx_journey_created    (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Nhật ký hành trình mở tài khoản thành công — compliance/audit, export tĩnh khi cần. Hành trình không đạt ACCOUNT_OPENING_COMPLETED trong 8h bị xóa (xem Section 7).';
```

### 3.2 Liquibase Changeset

**File:** `src/main/resources/config/liquibase/changelog/20260715000001_add_ekyc_journey_log.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.9.xsd">

    <changeSet id="20260715000001" author="duc.nguyen">
        <createTable tableName="ekyc_journey_log">
            <column name="id" type="bigint" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="session_id" type="varchar(64)"/>
            <column name="phone_no" type="varchar(20)"/>
            <column name="identifier_id" type="varchar(20)"/>
            <column name="e_kyc_id" type="bigint"/>
            <column name="step" type="varchar(50)">
                <constraints nullable="false"/>
            </column>
            <column name="status" type="varchar(20)">
                <constraints nullable="false"/>
            </column>
            <column name="payload" type="longtext">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="datetime">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <createIndex tableName="ekyc_journey_log" indexName="idx_journey_session">
            <column name="session_id"/>
        </createIndex>
        <createIndex tableName="ekyc_journey_log" indexName="idx_journey_identifier">
            <column name="identifier_id"/>
        </createIndex>
        <createIndex tableName="ekyc_journey_log" indexName="idx_journey_phone">
            <column name="phone_no"/>
        </createIndex>
        <createIndex tableName="ekyc_journey_log" indexName="idx_journey_ekyc">
            <column name="e_kyc_id"/>
        </createIndex>
        <createIndex tableName="ekyc_journey_log" indexName="idx_journey_step">
            <column name="step"/>
        </createIndex>
        <createIndex tableName="ekyc_journey_log" indexName="idx_journey_created">
            <column name="created_at"/>
        </createIndex>
    </changeSet>

</databaseChangeLog>
```

---

## 4. Entity: `EKycJourneyLog.java`

**File:** `domain/EKycJourneyLog.java`

```java
@Entity
@Table(name = "ekyc_journey_log")
public class EKycJourneyLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "identifier_id")
    private String identifierId;

    @Column(name = "e_kyc_id")
    private Long eKycId;

    @Column(name = "step", nullable = false)
    private String step;   // enum JourneyStepEnum — xem Section 2

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS / FAILED

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload; // JSON nguyên văn — không parse thành cột riêng

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    // getters/setters omitted for brevity
}
```

**Enum `JourneyStepEnum`** (package `constant`):

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
    ECONTRACT_SIGN_COMPLETED     // backend-only, xem Section 6
}
```

---

## 5. API Contract — 10 step App-facing

**`POST /api/v1/ekycs/journey-log`** — service `ekyc-admin`, integration type **TradeX-native** (theo `tradex-api-conventions.md` Response Format Standards).

```typescript
// Request
{
  sessionId: string,           // bắt buộc — App generate 1 lần/hành trình, gửi kèm mọi step
  phoneNo?: string,             // bắt buộc tới khi có identifierId
  identifierId?: string,        // có từ step EKYC_FACE_SCAN trở đi
  step: string,                  // bắt buộc — 1 trong 10 step Section 2 (trừ ECONTRACT_SIGN_COMPLETED)
  status: "SUCCESS" | "FAILED",
  payload: object                // bắt buộc — toàn bộ field màn hình đó, giữ nguyên (kể cả base64 ảnh ở EKYC_FACE_SCAN)
}

// Response 200
{ id: number }
```

**Validation:**
- `sessionId`, `step`, `status`, `payload` bắt buộc → thiếu field nào trả `400 INVALID_PARAMETER` theo chuẩn `tradex-api-conventions.md`.
- `step` phải thuộc `JourneyStepEnum` (10 giá trị App-facing) → sai giá trị trả `400` với `code: INVALID_VALUE`.
- Không validate business rule của payload (nội dung form) — theo triết lý "Light Validation at TradeX" trong `tradex-api-conventions.md`.

---

## 6. `ECONTRACT_SIGN_COMPLETED` — Backend-driven hook (không qua App)

Việc ký hợp đồng diễn ra trong webview FPT; FPT gọi callback thẳng vào `EContractCustomServiceImpl.getEContractStatus()` (đã tồn tại, có xác thực chữ ký RSA). Điểm khách hàng ký xong được xác định tại:

```java
// EContractCustomServiceImpl.java, trong getEContractStatus(...)
if (contactId.equals(eContract.getIdentifierId())) {
    if (contactIdAction.equals(ContactIdAction.signed) && contractStatus.equals(ContractStatus.processing)) {
        eContractInfo.setCustomerSignatueStatus(contactIdAction.name());

        // ── THÊM MỚI 2026-07-15 ──
        eKycJourneyLogService.logEcontractSigned(
            eContract.getEKyc().geteKycId(),
            request  // EContractStatusReq — chứa envelopeId, refId, contactId, contractStatus
        );
        // ─────────────────────────

        ... // logic ký hợp đồng hiện tại giữ nguyên
```

**Payload lưu vào `ekyc_journey_log.payload`:**

```json
{
  "envelopeId": "...",
  "refId": "...",
  "contactId": "...",
  "contractStatus": "processing",
  "contractIdAction": "signed",
  "contractNo": "...",          // từ eContract.getContractNo() nếu có
  "signFileContent": "...",     // base64 file đã ký — nhất quán với quyết định lưu nguyên base64 ở EKYC_FACE_SCAN
  "webhookReceivedAt": "..."
}
```

`e_kyc_id` = `eContract.getEKyc().geteKycId()` (có sẵn tại điểm này). `session_id` = NULL cho step này — không cần, vì `e_kyc_id` đã là khóa liên kết ổn định bất kể khách ký cùng phiên hay quay lại ký ở thiết bị/session khác.

---

## 7. Purge Job (Retention Policy)

Giữ nguyên quyết định 2026-07-06(a): **chỉ giữ hành trình thành công**. Ngưỡng "bỏ dở" = 8 giờ, khớp `EKYC_SESSION_ID_EXPIRE_TIME` thực tế (`ekyc-admin/.../constant/Constants.java:235`).

```java
@Scheduled(cron = "0 0 * * * *") // mỗi giờ, đầu giờ
public void purgeAbandonedJourneys() {
    journeyLogRepository.deleteAbandonedSessionsOlderThan(Duration.ofHours(8));
}
```

```sql
DELETE FROM ekyc_journey_log
WHERE session_id IN (
  SELECT session_id FROM (
    SELECT session_id, MIN(created_at) AS started_at
    FROM ekyc_journey_log
    WHERE session_id IS NOT NULL
    GROUP BY session_id
    HAVING SUM(CASE WHEN step = 'ACCOUNT_OPENING_COMPLETED' THEN 1 ELSE 0 END) = 0
       AND started_at < NOW() - INTERVAL 8 HOUR
  ) t
);
```

Không cần bảng phụ theo dõi trạng thái session — job tự suy ra "chưa hoàn tất" bằng cách kiểm tra thiếu row `ACCOUNT_OPENING_COMPLETED`. `ECONTRACT_SIGN_COMPLETED` không ảnh hưởng job này (luôn xảy ra sau khi session đã được giữ vĩnh viễn).

---

## 8. Export Tool (Compliance Audit)

Theo quyết định Phase 1 "không có màn hình admin" (README Review Log 2026-07-06c) — export là **script nội bộ**, không phải trang UI sống.

- **Vị trí:** script trong repo `ekyc-admin` (BE dev chạy tay khi compliance/audit cần tra soát 1 hành trình cụ thể).
- **Input:** `--sessionId=` hoặc `--identifierId=` hoặc `--eKycId=`.
- **Logic:** query `ekyc_journey_log` theo khóa tương ứng, `ORDER BY created_at ASC` → render vào template `journey.html` (đã được PM duyệt phần visualization — giữ nguyên cấu trúc, chỉ thay nguồn `RECORDS` hardcode bằng data query thật).
- **Output:** file HTML tĩnh, BE dev tự gửi nội bộ khi có yêu cầu (không publish/host công khai).

---

## 9. Quan hệ với sub-feature khác

| Sub-feature | Quan hệ |
|---|---|
| **01 Biometric Attempt Log** | Giữ **độc lập, không gộp**. 01 = mọi lần thử eKYC kể cả fail (audit fraud/biometric chuyên sâu, append-only). 07 (tài liệu này) = toàn cảnh hành trình mở TK, chỉ khi **thành công**. Liên kết qua `identifier_id`/`e_kyc_id` khi cần đối chiếu chéo. |
| **05 Contract Terms Checkbox Log** | **Đã xóa** — thay thế hoàn toàn bởi step `TERMS_AND_CONDITIONS_CONFIRMATION`. Xem `README.md` Review Log 2026-07-15. |
| **Story 2 (hành trình vãng lai + retry eKYC)** | Chưa thiết kế — đặt ở Scope 2. Khi triển khai, cần quyết định lại chính sách retention (hiện tại 07 xóa hoàn toàn hành trình không thành công). |

---

## 10. Việc cần chốt trước khi implement

- [ ] FE Lead xác nhận effort: cần sửa ~8 màn hình App để gọi `POST /ekycs/journey-log` tại đúng thời điểm mỗi step.
- [ ] PDPD review: payload lưu đầy đủ PII + base64 ảnh sinh trắc học — cần xác nhận cách lưu này (LONGTEXT nguyên văn, không mã hoá field) đáp ứng yêu cầu bảo vệ dữ liệu cá nhân.
- [ ] Xác nhận field `contractNo` có sẵn trực tiếp trên entity `EContract`/`EContractInfo` hay cần bổ sung (dev kiểm tra khi implement Section 6).

---

**Document Status:** ✅ Complete | **For:** BE Lead, FE Lead, Backend Dev (ekyc-admin) | **Next Steps:** BE Lead + FE Lead xác nhận Section 10 → implement Scope 1 (01 + 07) → Scope 2 (02, 03, 04, 06, Story 2 vãng lai) mở lại sau
