# Service Agreement — API Specification

**Feature:** Service Agreement (Hợp đồng/Điều khoản dịch vụ)
**TT134 Reference:** Điều 5, Khoản 4 — Cung cấp dịch vụ phải thể hiện bằng hợp đồng hoặc điều khoản của hợp đồng
**Priority:** P1
**Version:** 2.0
**Status:** Draft

---

## 0. Flow Overview

```
[Đăng ký/Kích hoạt]
    │
    ├─ 1. Xác minh SĐT (3-Layer)
    │      ├─ Layer 1: Carrier API (GSMA Open Gateway)
    │      │   ├─ Number Verification: SĐT active + thuộc subscriber?
    │      │   └─ SIM Swap Check: SIM có bị đổi gần đây?
    │      │
    │      ├─ Layer 2: VNeID/RAR (Phase 2)
    │      │   └─ App-to-app identity verification qua C06
    │      │
    │      ├─ Layer 3: OTP + eKYC (Fallback)
    │      │   ├─ OTP đến SĐT + cross-check hồ sơ
    │      │   └─ eKYC (CCCD + face matching)
    │      │
    │      └─ (Nếu thay đổi SĐT) → Biometric L2 + re-verify
    │
    ├─ 2. Hiển thị Điều khoản dịch vụ
    │      ├─ Lấy nội dung T&C mới nhất (versioned)
    │      ├─ Phương thức GD online & loại GD tương ứng
    │      └─ Rủi ro & trách nhiệm bồi thường
    │
    ├─ 3. Đồng ý/Chấp nhận điều khoản
    │      ├─ Accept → ghi nhận lịch sử
    │      └─ Reject → không thể tiếp tục sử dụng dịch vụ
    │
    └─ 4. Contract execution (Pháp chế)
            └─ Lưu hợp đồng điện tử (Phase 2)
```

| Bước | Mô tả | BE/FE |
|------|-------|-------|
| 1 | Xác minh SĐT thuộc quyền sử dụng hợp pháp của KH (Carrier API → VNeID → OTP fallback) | BE + FE |
| 2 | Hiển thị điều khoản dịch vụ (T&C, trading methods, rủi ro) | BE + FE |
| 3 | KH chấp nhận điều khoản → ghi nhận lịch sử | BE + FE |
| 4 | Tạo/lưu hợp đồng điện tử (Phase 2, tùy chọn) | BE |

## 0a. UX Principles

| Principle | Mô tả |
|-----------|-------|
| **Minh bạch** | Toàn bộ điều khoản, phương thức GD, rủi ro hiển thị rõ ràng trước khi KH đồng ý |
| **Xác minh chủ quyền SĐT** | Carrier API (GSMA Open Gateway) hoặc VNeID — xác minh từ bên thứ ba, không chỉ OTP nội bộ |
| **Auditable** | Mọi hành động xem/đồng ý/từ chối terms đều được ghi log |
| **Không block flow** | Terms acceptance được tích hợp vào flow đăng ký, không tạo thêm bước riêng lẻ |
| **Version rõ ràng** | KH luôn biết mình đang đồng ý với phiên bản điều khoản nào |

## 0b. BE / FE Responsibility Split

| Component | Backend | Frontend (App) |
|-----------|---------|----------------|
| Phone verification | Carrier API verify + OTP fallback | Input SĐT, carrier verify screen, OTP screen |
| Phone change | Biometric L2 + carrier re-verify + OTP | Xác thực lại + nhập SĐT mới |
| Terms list | GET /api/v1/terms (latest + version history) | TermsDisplayScreen |
| Terms acceptance | POST /api/v1/terms/:versionId/accept | Button "Tôi đồng ý", confirm modal |
| Terms check | GET /api/v1/terms/status | Hiển thị trạng thái đã đồng ý/chưa |
| Trading methods | GET /api/v1/trading-methods | Danh sách phương thức GD |
| Risk disclosure | GET /api/v1/risk-disclosure | Nội dung rủi ro & trách nhiệm |
| Admin: terms mgmt | POST/PUT/DELETE /api/v1/admin/terms | Admin UI (Phase 2) |

---

## 1. Overview

### 1.1 Problem

Khoản 4 Điều 5 TT134 yêu cầu hợp đồng/điều khoản dịch vụ với khách hàng phải bao gồm:

1. **Phương thức giao dịch trực tuyến & loại GD tương ứng** — Hiện tại chưa có API hoặc registry tập trung để FE/contract lấy danh sách này. Mỗi loại giao dịch (đặt lệnh cơ bản, đặt lệnh điều kiện, TP/SL, OCO) có thể hỗ trợ trên các kênh khác nhau (Mobile App, Web Trading, API).

2. **Số điện thoại di động đăng ký** — Hiện tại SĐT đang được quản lý trong hệ thống nhưng chưa có cơ chế xác minh quyền sở hữu SIM hợp pháp từ bên thứ ba (carrier network hoặc C06). Yêu cầu "biện pháp xác minh" không thể chỉ dựa trên OTP + cross-check nội bộ.

3. **Rủi ro & trách nhiệm bồi thường** — Chưa có hệ thống quản lý nội dung rủi ro và tracking xác nhận từ KH.

Ngoài ra, chưa có:
- Terms & Conditions versioning và acceptance tracking
- API cho FE/Web hiển thị nội dung hợp đồng động
- Audit trail cho việc KH xem/đồng ý điều khoản

### 1.2 Goals

| # | Goal | TT134 Ref |
|---|------|-----------|
| G1 | Xác minh SĐT thuộc quyền sử dụng hợp pháp của KH (Carrier API + OTP fallback, Phase 2 VNeID) | Điều 5.4 |
| G2 | API quản lý SĐT: đăng ký, thay đổi, xác minh, có biometric L2 cho thay đổi | Điều 5.4 |
| G3 | Terms & Conditions versioning + acceptance tracking | Điều 5.4 |
| G4 | API trả về danh sách phương thức GD online & loại GD tương ứng | Điều 5.4 |
| G5 | API trả về nội dung rủi ro & trách nhiệm bồi thường | Điều 5.4 |
| G6 | Audit trail cho mọi thao tác liên quan đến hợp đồng/dịch vụ | Điều 5.4, 13 |

### 1.3 Non-Goals

- ❌ Smart contract / chữ ký số hợp đồng điện tử — thuộc Phase 2 (Pháp chế)
- ❌ eKYC flow (CCCD scan, face matching) — thuộc hệ thống eKYC riêng (Layer 3 fallback)
- ❌ Carrier API gateway implementation detail — spec riêng cho carrier integration
- ❌ Lưu trữ hợp đồng PDF — thuộc Phase 2 / document management system
- ❌ Realtime notification cho terms update — thuộc Alert System (P1)

---

## 2. Current State Analysis

### 2.1 Existing Phone Management

Hiện tại hệ thống lưu SĐT trong `t_user` hoặc `t_client`:

```
t_user
├── id
├── phone_country_code       (e.g., "+84")
├── phone_number             (e.g., "912345678")
├── phone_verified           (boolean? — cần xác nhận)
├── ...other fields
```

**Key observations:**
- **Có SĐT trong hệ thống** nhưng chưa chắc đã có cơ chế xác minh quyền sở hữu
- **Chưa có verified_at / verified_method** — không track được SĐT đã được xác minh khi nào, bằng cách nào
- **Chưa có history** — khi KH thay đổi SĐT, mất trace SĐT cũ
- **Chưa có carrier API integration** — không thể xác minh SĐT qua nhà mạng (yêu cầu của Circular 08/2026/TT-BKHCN)
- **Chưa có VNeID/RAR** — không thể xác minh qua National Population Database (C06)

### 2.2 Existing Terms & Conditions

Hiện tại chưa có hệ thống quản lý điều khoản dịch vụ tập trung.

| Requirement | Current State | Gap |
|-------------|---------------|-----|
| Terms versioning | Không có | ❌ |
| Acceptance tracking | Không có | ❌ |
| API lấy nội dung terms | Không có | ❌ |
| Trading methods registry | Không có | ❌ |
| Risk disclosure content | Không có | ❌ |

### 2.3 Existing Trading Methods

Hệ thống hiện tại hỗ trợ các loại giao dịch nhưng chưa có API/registry mapping:
- Online trading qua Mobile App (iOS/Android) — có sẵn
- Online trading qua Web Trading platform — có sẵn
- API trading (third-party) — có sẵn

Chưa có API động trả về mapping giữa loại giao dịch (order type) và phương thức giao dịch (channel).

---

## 3. Proposed Architecture

### 3.1 Carrier API Integration Layer

Thêm service `carrier-gateway` để abstract hóa việc gọi GSMA Open Gateway CAMARA APIs từ các nhà mạng:

```text
[NHSV Backend]
    │
    ├─ carrier-gateway (service mới)
    │   ├─ Viettel API Gateway   ─── GSMA Open Gateway
    │   ├─ VinaPhone API Gateway ─── GSMA Open Gateway
    │   └─ MobiFone API Gateway  ─── GSMA Open Gateway
    │
    ├─ VNeID / RAR-C06 (Phase 2)
    │
    └─ Smart-OTP (fallback)
```

**Carrier API Gateway endpoints:**

| API | Description | CAMARA Spec |
|-----|-------------|-------------|
| `POST /verify-number` | Xác minh SĐT đang active + thuộc subscriber | Number Verification API |
| `POST /check-sim-swap` | Kiểm tra SIM có bị đổi gần đây không | SIM Swap API |
| `GET /operators` | Danh sách nhà mạng hỗ trợ | — |

**Response format (Number Verification):**

```json
{
  "operator": "VIETTEL",
  "devicePhoneNumber": "+84912345678",
  "verificationResult": "CONFIRMED",
  "verificationTimestamp": "2026-06-22T10:30:00+07:00"
}
```

- `verificationResult`: `CONFIRMED` / `NOT_CONFIRMED` / `UNAVAILABLE`
- Nếu `UNAVAILABLE` → fallback sang OTP + cross-check

### 3.2 Phone Management Extension

Thêm bảng `t_phone_verification` để track lịch sử xác minh:

```sql
-- Bảng track lịch sử SĐT
CREATE TABLE t_phone_record (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    phone_country_code  VARCHAR(5)  NOT NULL,
    phone_number        VARCHAR(20) NOT NULL,
    is_primary          TINYINT(1)  DEFAULT 1,
    verified_at         DATETIME    NULL,
    verified_method     VARCHAR(50) NULL,       -- 'CARRIER_API', 'VNEID', 'OTP', 'OTP_ID_CROSS_CHECK'
    verified_by         VARCHAR(50) NULL,       -- 'CARRIER_VIETTEL', 'CARRIER_VINAPHONE', 'CARRIER_MOBIFONE', 'VNEID', 'SYSTEM'
    carrier_ref_id     VARCHAR(100) NULL,       -- Reference ID từ carrier API response
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    superseded_at       DATETIME    NULL,       -- khi có SĐT mới thay thế
    superseded_by       BIGINT      NULL,       -- FK → t_phone_record.id

    INDEX idx_user_id (user_id),
    INDEX idx_phone (phone_country_code, phone_number)
);

-- Bảng OTP verification sessions (fallback)
CREATE TABLE t_otp_verification (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    phone_record_id BIGINT NOT NULL,
    otp_type        VARCHAR(30) NOT NULL,       -- 'REGISTER', 'CHANGE', 'VERIFY'
    otp_ref_id      VARCHAR(64) NOT NULL,       -- reference từ Smart-OTP
    status          VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'VERIFIED', 'EXPIRED', 'FAILED'
    attempts        INT DEFAULT 0,
    max_attempts    INT DEFAULT 5,
    expired_at      DATETIME NOT NULL,
    verified_at     DATETIME NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_otp_ref (otp_ref_id)
);

-- Bảng carrier verification log (audit)
CREATE TABLE t_carrier_verification (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    phone_record_id     BIGINT NOT NULL,
    carrier_operator    VARCHAR(20) NOT NULL,     -- 'VIETTEL', 'VINAPHONE', 'MOBIFONE'
    api_endpoint        VARCHAR(100) NOT NULL,    -- 'verify-number', 'check-sim-swap'
    request_payload     JSON NULL,
    response_status     VARCHAR(20) NOT NULL,     -- 'CONFIRMED', 'NOT_CONFIRMED', 'UNAVAILABLE', 'ERROR'
    response_body       JSON NULL,
    http_status_code    INT NULL,
    duration_ms         INT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_carrier (carrier_operator, response_status)
);
```

### 3.3 Terms & Conditions Data Model

```sql
-- Bảng terms versions
CREATE TABLE t_terms_version (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_tag     VARCHAR(20)  NOT NULL,       -- e.g., "v1.0", "v1.1", "v2.0"
    title_vi        TEXT NOT NULL,                -- Tiêu đề tiếng Việt
    title_en        TEXT NULL,                    -- Tiêu đề tiếng Anh
    content_vi      LONGTEXT NOT NULL,            -- Nội dung tiếng Việt (HTML/Markdown)
    content_en      LONGTEXT NULL,                -- Nội dung tiếng Anh
    effective_from  DATETIME NOT NULL,            -- Ngày hiệu lực
    effective_to    DATETIME NULL,                -- NULL = đang hiệu lực
    status          VARCHAR(20) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE', 'DEPRECATED'
    published_by    BIGINT NULL,                  -- Admin user ID
    change_summary  TEXT NULL,                    -- Tóm tắt thay đổi
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_version_tag (version_tag),
    INDEX idx_status (status, effective_from)
);

-- Bảng acceptance tracking
CREATE TABLE t_terms_acceptance (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    terms_version_id BIGINT NOT NULL,
    action          VARCHAR(20) NOT NULL,         -- 'ACCEPTED', 'REJECTED', 'VIEWED'
    ip_address      VARCHAR(45) NOT NULL,
    device_fingerprint_id BIGINT NULL,            -- FK → Device Fingerprinting
    accepted_at     DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_version (user_id, terms_version_id),
    INDEX idx_user (user_id),
    FOREIGN KEY (terms_version_id) REFERENCES t_terms_version(id)
);
```

### 3.4 Trading Methods & Risk Disclosure Data Model

```sql
-- Bảng trading methods (config động)
CREATE TABLE t_trading_method (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    method_code     VARCHAR(50)  NOT NULL,        -- 'MOBILE_APP', 'WEB_TRADING', 'API'
    method_name_vi  VARCHAR(200) NOT NULL,        -- "App Mobile NHSV Pro"
    method_name_en  VARCHAR(200) NULL,            -- "NHSV Pro Mobile App"
    description_vi  TEXT NULL,
    description_en  TEXT NULL,
    is_active       TINYINT(1) DEFAULT 1,
    display_order   INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_method_code (method_code)
);

-- Bảng mapping trading method → order types
CREATE TABLE t_trading_method_order_type (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    trading_method_id BIGINT NOT NULL,
    order_type_code VARCHAR(50) NOT NULL,         -- 'REGULAR', 'STOP_LIMIT', 'TP_SL', 'OCO', 'ATS'
    order_type_name_vi VARCHAR(200) NOT NULL,     -- "Lệnh thường", "Lệnh dừng", "Lệnh TP/SL"
    order_type_name_en VARCHAR(200) NULL,
    is_active       TINYINT(1) DEFAULT 1,

    INDEX idx_method (trading_method_id),
    FOREIGN KEY (trading_method_id) REFERENCES t_trading_method(id)
);

-- Bảng risk disclosure content (versioned)
CREATE TABLE t_risk_disclosure (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_tag     VARCHAR(20) NOT NULL,
    content_vi      LONGTEXT NOT NULL,
    content_en      LONGTEXT NULL,
    effective_from  DATETIME NOT NULL,
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_version_tag (version_tag),
    INDEX idx_status (status)
);
```

### 3.5 Configuration

```javascript
conf.serviceAgreement = {
  phoneVerification: {
    maxOtpAttempts: 5,
    otpTtlSeconds: 120,
    requireBiometricForChange: true,          // Đổi SĐT cần biometric L2
    carrierApi: {
      preferred: true,                        // Ưu tiên carrier API trước
      fallbackToOtp: true,                    // Fallback OTP nếu carrier unavailable
      numberVerificationEnabled: true,
      simSwapCheckEnabled: true,
      timeoutMs: 5000,                        // Timeout gọi carrier API
      cacheTtlSeconds: 300,                   // Cache kết quả carrier 5 phút
    },
    vneid: {
      enabled: false,                         // Phase 2
      requireForPhoneChange: false,
    },
  },
  terms: {
    forceAcceptOnNewVersion: true,            // Bắt buộc accept khi có version mới
    gracePeriodDays: 7,                       // Thời gian grace trước khi force
  },
  tradingMethods: {
    cacheTtlSeconds: 3600,                    // Cache 1h vì ít thay đổi
  }
};
```

---

## 4. API Endpoints

### 4.1 Phone Verification — Initiate Verification

```
POST /api/v1/phone/verify
Authorization: Bearer <access_token>
```

**Request Body:** `IPhoneVerifyRequest`

```json
{
  "phoneCountryCode": "+84",
  "phoneNumber": "912345678",
  "purpose": "REGISTER",
  "fullName": "Nguyễn Văn A",
  "dob": "1990-01-15",
  "preferMethod": "CARRIER_API"
}
```

**Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `phoneCountryCode` | string | ✅ | Mã quốc gia, ví dụ "+84" |
| `phoneNumber` | string | ✅ | Số điện thoại (không bao gồm mã quốc gia) |
| `purpose` | string | ✅ | Mục đích: `REGISTER`, `CHANGE`, `VERIFY` |
| `fullName` | string | ✅ | Họ tên đầy đủ (dùng cho OTP fallback và cross-check) |
| `dob` | string | ✅ | Ngày sinh (yyyy-MM-dd) (dùng cho OTP fallback) |
| `preferMethod` | string | ❌ | `CARRIER_API` (default), `OTP` |

**Response:** `IPhoneVerifyResponse`

```json
{
  "status": "carrier_confirmed",
  "method": "CARRIER_API",
  "carrierRefId": "carrier_tx_abc123",
  "phoneMasked": "+84***345678",
  "verificationResult": "CONFIRMED",
  "operator": "VIETTEL",
  "simSwapChecked": false
}
```

**Response scenarios:**

| Status | Method | Description |
|--------|--------|-------------|
| `carrier_confirmed` | CARRIER_API | Carrier xác nhận SĐT thuộc subscriber |
| `carrier_unavailable` | — | Carrier API không available → FE fallback sang OTP |
| `otp_sent` | OTP | OTP đã gửi (fallback), FE chuyển sang OTP screen |
| `vneid_required` | VNEID | Yêu cầu xác thực VNeID (Phase 2) |

**Success Codes:**

- `200 OK` — Verification initiated

**Error Codes:**

| Code | HTTP | Description |
|------|------|-------------|
| `INVALID_PHONE` | 400 | Định dạng SĐT không hợp lệ |
| `CARRIER_NETWORK_ERROR` | 502 | Lỗi kết nối đến carrier API |
| `CARRIER_NOT_SUPPORTED` | 400 | Nhà mạng không hỗ trợ GSMA Open Gateway |
| `CARRIER_SIM_SWAP_DETECTED` | 400 | SIM vừa được đổi gần đây, yêu cầu xác minh bổ sung |
| `OTP_TOO_FREQUENT` | 429 | Yêu cầu OTP quá thường xuyên |
| `MAX_ATTEMPTS_EXCEEDED` | 429 | Đã vượt quá số lần yêu cầu tối đa |
| `PHONE_ALREADY_REGISTERED` | 409 | SĐT đã được đăng ký bởi người dùng khác |

**Business Rules:**

- BE tự động chọn method theo thứ tự ưu tiên: Carrier API → OTP fallback
- Nếu `preferMethod=OTP` → bỏ qua carrier API, gửi OTP ngay
- Gọi carrier Number Verification API để xác minh SĐT
- Nếu SIM Swap API enabled → kiểm tra SIM có bị đổi gần đây không
- Mỗi user chỉ được gửi tối đa 5 lần/giờ
- Gửi notification đến thiết bị đã login khi có request verify

---

### 4.2 Phone Verification — Verify OTP

```
POST /api/v1/phone/verify-otp
Authorization: Bearer <access_token>
```

**Request Body:** `IPhoneOtpVerifyRequest`

```json
{
  "otpRefId": "otp_ref_uuid_123",
  "otpCode": "123456"
}
```

**Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `otpRefId` | string | ✅ | OTP reference ID từ request-otp |
| `otpCode` | string | ✅ | Mã OTP (6 digits) |

**Response:** `IPhoneOtpVerifyResponse`

```json
{
  "status": "verified",
  "phoneRecordId": 456,
  "isPrimary": true,
  "verifiedAt": "2026-06-22T10:30:00+07:00"
}
```

**Success Codes:**

- `200 OK` — verified thành công

**Error Codes:**

| Code | HTTP | Description |
|------|------|-------------|
| `OTP_INVALID` | 400 | Mã OTP không đúng |
| `OTP_EXPIRED` | 400 | OTP đã hết hạn |
| `OTP_MAX_ATTEMPTS` | 429 | Đã nhập sai OTP quá số lần cho phép |
| `VERIFICATION_EXPIRED` | 400 | Session xác minh đã hết hạn |

**Business Rules:**

- Tối đa 5 lần nhập OTP sai → block OTP session
- Sau khi verify thành công → ghi nhận vào `t_phone_record` và `t_otp_verification`
- Nếu `purpose=REGISTER` → set SĐT này làm primary
- Nếu `purpose=CHANGE` → SĐT cũ `superseded_at = now()`, SĐT mới là primary
- Log event PHONE_VERIFIED (Audit Log)

---

### 4.3 Phone — Get Current Phone Info

```
GET /api/v1/phone
Authorization: Bearer <access_token>
```

**Response:** `IPhoneInfoResponse`

```json
{
  "phoneCountryCode": "+84",
  "phoneNumber": "912345678",
  "phoneMasked": "+84***345678",
  "isVerified": true,
  "verifiedAt": "2026-06-22T10:30:00+07:00",
  "verifiedMethod": "OTP_ID_CROSS_CHECK"
}
```

**Success Codes:**

- `200 OK`

**Business Rules:**
- Chỉ expose masked phone number trong response
- `isVerified` = true nếu có `verified_at` không null ở `t_phone_record` active

---

### 4.4 Phone — Initiate Phone Number Change

```
POST /api/v1/phone/change
Authorization: Bearer <access_token>
```

**Request Body:** `IPhoneChangeRequest`

```json
{
  "newPhoneCountryCode": "+84",
  "newPhoneNumber": "987654321",
  "fullName": "Nguyễn Văn A",
  "dob": "1990-01-15",
  "biometricProofToken": "bio_jwt_token..."
}
```

**Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `newPhoneCountryCode` | string | ✅ | Mã quốc gia |
| `newPhoneNumber` | string | ✅ | Số điện thoại mới |
| `fullName` | string | ✅ | Họ tên (cross-check) |
| `dob` | string | ✅ | Ngày sinh (cross-check) |
| `biometricProofToken` | string | ✅ | JWT token từ biometric L2 verification |

**Response:**

```json
{
  "status": "otp_sent_to_new_phone",
  "otpRefId": "otp_ref_uuid_456",
  "expiresInSeconds": 120,
  "phoneMasked": "+84***654321"
}
```

**Success Codes:**

- `200 OK`

**Error Codes:**

| Code | HTTP | Description |
|------|------|-------------|
| `BIOMETRIC_REQUIRED` | 403 | Cần xác thực biometric L2 để đổi SĐT |
| `BIOMETRIC_TOKEN_INVALID` | 403 | Biometric token không hợp lệ hoặc đã hết hạn |
| `SAME_AS_CURRENT` | 400 | SĐT mới trùng với SĐT hiện tại |
| `CROSS_CHECK_FAILED` | 400 | Thông tin cá nhân không khớp |

**Business Rules:**

- **Bắt buộc biometric L2** trước khi cho phép đổi SĐT (gọi qua Biometric System)
- Sau OTP verify → SĐT cũ bị `superseded_at`, SĐT mới active
- Gửi notification đến SĐT cũ + thiết bị đang login cảnh báo SĐT đã thay đổi
- Log event PHONE_CHANGED (Audit Log)

---

### 4.5 Phone — Phone Change History

```
GET /api/v1/phone/history
Authorization: Bearer <access_token>
```

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `limit` | int | ❌ | Số lượng bản ghi tối đa (default 10) |
| `before` | string (ISO) | ❌ | Lấy lịch sử trước thời điểm này |

**Response:** `IPhoneHistoryResponse`

```json
{
  "records": [
    {
      "phoneRecordId": 456,
      "phoneMasked": "+84***654321",
      "isPrimary": true,
      "verifiedAt": "2026-06-22T10:30:00+07:00",
  "verifiedMethod": "CARRIER_API",
  "carrierOperator": "VIETTEL",
  "carrierRefId": "carrier_tx_abc123"
    },
    {
      "phoneRecordId": 123,
      "phoneMasked": "+84***345678",
      "isPrimary": false,
      "verifiedAt": "2020-01-15T08:00:00+07:00",
      "verifiedMethod": "OTP",
      "supersededAt": "2026-06-22T10:35:00+07:00"
    }
  ]
}
```

**Business Rules:**

- Chỉ expose masked phone number
- Sắp xếp theo thời gian giảm dần
- `isPrimary=true` = SĐT đang sử dụng

---

### 4.6 Terms — Get Latest Terms

```
GET /api/v1/terms/latest
Authorization: Bearer <access_token>
```

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `lang` | string | ❌ | Ngôn ngữ: `vi` (default), `en` |

**Response:** `ITermsResponse`

```json
{
  "versionId": 5,
  "versionTag": "v2.0",
  "title": "Điều khoản sử dụng dịch vụ giao dịch chứng khoán trực tuyến",
  "content": "<h2>1. Phương thức giao dịch</h2><p>...</p><h2>2. Xác thực</h2><p>...</p>",
  "effectiveFrom": "2026-07-01T00:00:00+07:00",
  "changeSummary": "Bổ sung điều khoản về xác minh SĐT và phương thức GD theo TT134",
  "isAccepted": true,
  "acceptedAt": "2026-07-01T08:30:00+07:00"
}
```

**Success Codes:**

- `200 OK`

**Error Codes:**

| Code | HTTP | Description |
|------|------|-------------|
| `NO_ACTIVE_TERMS` | 404 | Không có điều khoản nào đang hiệu lực |

**Business Rules:**

- Luôn trả về version ACTIVE mới nhất (theo `effective_from`)
- `isAccepted` = true nếu user đã `ACCEPTED` version này
- `content` có thể là HTML hoặc Markdown tùy cấu hình
- Cache ở FE (ko gọi lại mỗi lần) — chỉ gọi khi terms status chưa accept

---

### 4.7 Terms — Get Specific Version

```
GET /api/v1/terms/:versionId
Authorization: Bearer <access_token>
```

**Parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `versionId` | number | ID của terms version |

**Response:** (Giống 4.6)

**Success Codes:**

- `200 OK`

**Error Codes:**

| Code | HTTP | Description |
|------|------|-------------|
| `TERMS_VERSION_NOT_FOUND` | 404 | Không tìm thấy version này |

---

### 4.8 Terms — List All Versions

```
GET /api/v1/terms
Authorization: Bearer <access_token>
```

**Response:** `ITermsListResponse`

```json
{
  "versions": [
    {
      "versionId": 5,
      "versionTag": "v2.0",
      "title": "Điều khoản sử dụng dịch vụ giao dịch chứng khoán trực tuyến",
      "status": "ACTIVE",
      "effectiveFrom": "2026-07-01T00:00:00+07:00",
      "changeSummary": "Bổ sung điều khoản về xác minh SĐT và phương thức GD theo TT134",
      "isAccepted": true,
      "acceptedAt": "2026-07-01T08:30:00+07:00"
    },
    {
      "versionId": 4,
      "versionTag": "v1.1",
      "title": "Điều khoản sử dụng dịch vụ giao dịch chứng khoán trực tuyến",
      "status": "DEPRECATED",
      "effectiveFrom": "2025-06-01T00:00:00+07:00",
      "effectiveTo": "2026-06-30T23:59:59+07:00",
      "changeSummary": "Cập nhật theo TT134 draft",
      "isAccepted": true,
      "acceptedAt": "2025-06-01T09:00:00+07:00"
    }
  ],
  "totalVersions": 5,
  "pendingAcceptance": false
}
```

**Business Rules:**

- `pendingAcceptance` = true nếu có ACTIVE version mà user chưa ACCEPTED
- Nếu `pendingAcceptance=true`, FE bắt buộc hiển thị terms screen khi đăng nhập
- Sắp xếp: ACTIVE đầu tiên, sau đó theo `effective_from` DESC

---

### 4.9 Terms — Accept / Reject Terms

```
POST /api/v1/terms/:versionId/accept
Authorization: Bearer <access_token>
```

**Request Body:** `ITermsAcceptRequest`

```json
{
  "action": "ACCEPTED",
  "deviceFingerprintId": 789
}
```

**Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `action` | string | ✅ | `ACCEPTED` hoặc `REJECTED` |
| `deviceFingerprintId` | number | ❌ | Device Fingerprint ID (khuyến khích) |

**Response:** `ITermsAcceptResponse`

```json
{
  "status": "accepted",
  "versionId": 5,
  "versionTag": "v2.0",
  "acceptedAt": "2026-07-01T08:30:00+07:00",
  "nextAction": null
}
```

**Success Codes:**

- `200 OK`

**Error Codes:**

| Code | HTTP | Description |
|------|------|-------------|
| `TERMS_VERSION_NOT_FOUND` | 404 | Version không tồn tại |
| `TERMS_VERSION_INACTIVE` | 400 | Version không còn hiệu lực |
| `ALREADY_ACCEPTED` | 409 | User đã accept version này rồi |
| `ACTION_INVALID` | 400 | `action` không hợp lệ |

**Business Rules:**

- `ACCEPTED`: ghi nhận vào `t_terms_acceptance`, trả về `acceptedAt`
- `REJECTED`: ghi nhận reject, log event. User không reject được terms bắt buộc (luôn cho phép reject technical nhưng sẽ block service)
- Log event TERMS_ACCEPTED / TERMS_REJECTED (Audit Log)
- `nextAction`: nếu còn version khác chưa accept → trả về version ID tiếp theo

---

### 4.10 Terms — Check Acceptance Status

```
GET /api/v1/terms/status
Authorization: Bearer <access_token>
```

**Response:** `ITermsStatusResponse`

```json
{
  "hasPendingTerms": false,
  "latestVersionId": 5,
  "latestVersionTag": "v2.0",
  "acceptedVersionId": 5,
  "pendingSince": null,
  "gracePeriodEnd": null,
  "requiresAction": false
}
```

**Business Rules:**

- `hasPendingTerms` = true nếu có ACTIVE version mà user chưa ACCEPTED
- `requiresAction` = true nếu cần force accept (dùng trong auth middleware check)
- `gracePeriodEnd`: ngày cuối cùng được grace trước khi force (config 7 ngày)
- API này được gọi sau login để FE quyết định có show terms screen không

---

### 4.11 Trading Methods — Get All Methods

```
GET /api/v1/trading-methods
Authorization: Bearer <access_token>
```

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `lang` | string | ❌ | `vi` (default), `en` |

**Response:** `ITradingMethodsResponse`

```json
{
  "methods": [
    {
      "methodCode": "MOBILE_APP",
      "methodName": "App Mobile NHSV Pro",
      "description": "Giao dịch qua ứng dụng NHSV Pro trên điện thoại thông minh",
      "orderTypes": [
        { "code": "REGULAR", "name": "Lệnh thường (LO)" },
        { "code": "STOP_LIMIT", "name": "Lệnh dừng (STOP)" },
        { "code": "TP_SL", "name": "Lệnh TP/SL" },
        { "code": "OCO", "name": "Lệnh OCO" }
      ],
      "isActive": true
    },
    {
      "methodCode": "WEB_TRADING",
      "methodName": "Web Trading",
      "description": "Giao dịch qua nền tảng Web Trading",
      "orderTypes": [
        { "code": "REGULAR", "name": "Lệnh thường (LO)" },
        { "code": "STOP_LIMIT", "name": "Lệnh dừng (STOP)" },
        { "code": "TP_SL", "name": "Lệnh TP/SL" },
        { "code": "OCO", "name": "Lệnh OCO" }
      ],
      "isActive": true
    },
    {
      "methodCode": "API",
      "methodName": "API Giao dịch",
      "description": "Giao dịch qua API kết nối trực tiếp",
      "orderTypes": [
        { "code": "REGULAR", "name": "Lệnh thường (LO)" }
      ],
      "isActive": true
    }
  ]
}
```

**Success Codes:**

- `200 OK`

**Business Rules:**

- Dữ liệu lấy từ `t_trading_method` JOIN `t_trading_method_order_type`
- Chỉ trả về methods active
- Cache 1h (configurable), admin clear cache khi thay đổi
- Dùng trong terms display và contract generation

---

### 4.12 Risk Disclosure — Get Content

```
GET /api/v1/risk-disclosure
Authorization: Bearer <access_token>
```

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `lang` | string | ❌ | `vi` (default), `en` |
| `versionId` | number | ❌ | Specific version (default: latest active) |

**Response:** `IRiskDisclosureResponse`

```json
{
  "versionId": 3,
  "versionTag": "v2.0",
  "title": "Cảnh báo rủi ro và trách nhiệm bồi thường",
  "content": "<h2>1. Rủi ro thị trường</h2><p>...</p><h2>2. Rủi ro hệ thống</h2><p>...</p><h2>3. Trách nhiệm của công ty</h2><p>...</p><h2>4. Trách nhiệm của khách hàng</h2><p>...</p>",
  "effectiveFrom": "2026-07-01T00:00:00+07:00",
  "isAccepted": true,
  "acceptedAt": "2026-07-01T08:30:00+07:00"
}
```

**Business Rules:**

- `content` do pháp chế soạn thảo, upload qua admin UI
- Acceptance tracking riêng hoặc gộp với terms acceptance
- Hiển thị trong terms screen trước khi KH accept

---

## 5. Error Codes

### 5.1 Phone Verification Errors

| Code | HTTP | Description |
|------|------|-------------|
| `INVALID_PHONE` | 400 | Định dạng số điện thoại không hợp lệ |
| `CARRIER_NETWORK_ERROR` | 502 | Lỗi kết nối đến carrier API |
| `CARRIER_NOT_SUPPORTED` | 400 | Nhà mạng không hỗ trợ GSMA Open Gateway |
| `CARRIER_SIM_SWAP_DETECTED` | 400 | SIM vừa được đổi gần đây, cần xác minh bổ sung |
| `CARRIER_VERIFICATION_FAILED` | 400 | Carrier không xác nhận SĐT thuộc subscriber |
| `OTP_TOO_FREQUENT` | 429 | Yêu cầu OTP quá thường xuyên |
| `OTP_INVALID` | 400 | Mã OTP không đúng |
| `OTP_EXPIRED` | 400 | OTP đã hết hạn |
| `OTP_MAX_ATTEMPTS` | 429 | Đã nhập sai OTP quá số lần cho phép |
| `CROSS_CHECK_FAILED` | 400 | Thông tin cá nhân không khớp với hồ sơ |
| `PHONE_ALREADY_REGISTERED` | 409 | Số điện thoại đã được đăng ký |
| `SAME_AS_CURRENT` | 400 | Số điện thoại mới trùng với số hiện tại |
| `BIOMETRIC_REQUIRED` | 403 | Cần xác thực sinh trắc học (L2) |
| `BIOMETRIC_TOKEN_INVALID` | 403 | Token sinh trắc học không hợp lệ |

### 5.2 Terms Errors

| Code | HTTP | Description |
|------|------|-------------|
| `NO_ACTIVE_TERMS` | 404 | Không có điều khoản nào đang hiệu lực |
| `TERMS_VERSION_NOT_FOUND` | 404 | Version điều khoản không tồn tại |
| `TERMS_VERSION_INACTIVE` | 400 | Version không còn hiệu lực |
| `ALREADY_ACCEPTED` | 409 | Người dùng đã chấp nhận version này |
| `ACTION_INVALID` | 400 | Hành động không hợp lệ |

---

## 6. Phone Verification — Detailed Logic

### 6.1 Carrier API Integration Flow (Primary)

```
[User nhập SĐT]
    │
    ├─ Xác định nhà mạng (đầu số):
    │   ├─ 09x, 086, 088 → Viettel
    │   ├─ 08x, 089 → VinaPhone/VNPT
    │   └─ 07x, 09x → MobiFone
    │
    ├─ Gọi Carrier Number Verification API:
    │   ├─ Request: { phoneNumber, deviceIp, timestamp }
    │   ├─ Response: CONFIRMED ✓
    │   │   └─ → Ghi nhận phone_record (verified_method='CARRIER_API')
    │   │   └─ → Nếu SIM Swap enabled → check thêm
    │   │       ├─ SIM không đổi → verified ✓
    │   │       └─ SIM mới đổi → CARRIER_SIM_SWAP_DETECTED
    │   │                            → Yêu cầu OTP bổ sung
    │   │
    │   └─ Response: UNAVAILABLE / ERROR
    │       └─ → Fallback sang OTP (xem 6.2)
    │
    └─ Log event PHONE_VERIFIED (carrier_method) → Audit Log
```

### 6.2 OTP Fallback Flow (Secondary)

```
[Carrier API unavailable / preferMethod=OTP]
    │
    ├─ Cross-check với hồ sơ nội bộ:
    │   ├─ Họ tên + ngày sinh khớp? → tiếp tục
    │   └─ Không khớp → CROSS_CHECK_FAILED
    │
    ├─ Check SĐT đã được đăng ký chưa:
    │   ├─ Chưa → tiếp tục
    │   └─ Đã đăng ký (bởi user khác) → PHONE_ALREADY_REGISTERED
    │
    ├─ Gửi OTP qua Smart-OTP service
    │
    └─ User nhập OTP:
        ├─ Đúng → phone_record ghi nhận verified (OTP_ID_CROSS_CHECK)
        └─ Sai → tăng attempt count (tối đa 5)
```

### 6.3 Phone Change Flow

```
[User muốn đổi SĐT]
    │
    ├─ Yêu cầu biometric L2 (gọi Biometric System)
    │   ├─ Thành công → nhận biometricProofToken (JWT, 60s TTL)
    │   └─ Thất bại → BIOMETRIC_REQUIRED
    │
    ├─ Nhập SĐT mới
    │
    ├─ Carrier Verify (ưu tiên):
    │   ├─ CONFIRMED → tiếp tục
    │   └─ UNAVAILABLE → OTP fallback
    │
    ├─ Verify OTP (nếu fallback)
    │   ├─ Thành công → supersede SĐT cũ, active SĐT mới
    │   │                Gửi notification đến SĐT cũ + thiết bị cũ
    │   └─ Thất bại → retry (tối đa 5 lần)
    │
    └─ Log event PHONE_CHANGED (Audit Log)
```

---

## 7. Terms Acceptance UX Flow

### 7.1 First-time Registration Flow

```
[Đăng ký tài khoản thành công]
    │
    ├─ GET /api/v1/terms/status → hasPendingTerms = true
    │
    ├─ GET /api/v1/terms/latest → Nội dung T&C
    │
    ├─ GET /api/v1/trading-methods → Danh sách phương thức GD
    │
    ├─ GET /api/v1/risk-disclosure → Rủi ro & trách nhiệm
    │
    ├─ TermsScreen hiển thị:
    │   ├─ Điều khoản dịch vụ (tab chính)
    │   ├─ Phương thức giao dịch (tab 2)
    │   ├─ Rủi ro & trách nhiệm (tab 3)
    │   └─ Button "Tôi đã đọc và đồng ý" + "Từ chối"
    │
    └─ ACCEPTED → POST /api/v1/terms/:versionId/accept
                 → Ghi nhận acceptance
                 → Cho phép sử dụng dịch vụ
```

### 7.2 New Version Available Flow

```
[Terms được cập nhật — admin publish version mới]
    │
    ├─ User đăng nhập → GET /api/v1/terms/status
    │   ├─ hasPendingTerms = false → tiếp tục bình thường
    │   └─ hasPendingTerms = true → TermsRequiredScreen
    │
    ├─ TermsRequiredScreen:
    │   ├─ Hiển thị diff (thay đổi so với version cũ)
    │   ├─ Grace period countdown (nếu còn)
    │   ├─ Button "Tôi đồng ý với phiên bản mới"
    │   └─ Button "Tìm hiểu thêm" → link đến chi tiết
    │
    └─ Nếu không accept trong grace period → block giao dịch
```

---

## 8. Admin Endpoints (Phase 1 — Basic)

### 8.1 Create Terms Version

```
POST /api/v1/admin/terms
Authorization: Bearer <admin_token> (scope: admin:terms:write)
```

**Request Body:** `IAdminTermsCreateRequest`

```json
{
  "versionTag": "v2.0",
  "titleVi": "Điều khoản sử dụng dịch vụ giao dịch chứng khoán trực tuyến",
  "titleEn": "Online Securities Trading Terms of Service",
  "contentVi": "<h2>1. Phương thức giao dịch</h2><p>...</p>",
  "contentEn": "<h2>1. Trading Methods</h2><p>...</p>",
  "effectiveFrom": "2026-07-01T00:00:00+07:00",
  "changeSummary": "Bổ sung theo TT134"
}
```

**Response:**

```json
{
  "versionId": 5,
  "versionTag": "v2.0",
  "status": "ACTIVE",
  "createdAt": "2026-06-22T10:00:00+07:00"
}
```

**Business Rules:**

- Khi tạo version mới → version ACTIVE cũ tự động chuyển thành DEPRECATED
- `effectiveFrom` có thể là ngày trong tương lai (scheduled publish)
- Log event TERMS_CREATED (Audit Log)

### 8.2 Update Trading Method Config

```
PUT /api/v1/admin/trading-methods
Authorization: Bearer <admin_token> (scope: admin:trading-methods:write)
```

**Request Body:** `IAdminTradingMethodsRequest`

```json
{
  "methods": [
    {
      "methodCode": "MOBILE_APP",
      "methodNameVi": "App Mobile NHSV Pro",
      "methodNameEn": "NHSV Pro Mobile App",
      "descriptionVi": "Giao dịch qua ứng dụng NHSV Pro",
      "isActive": true,
      "displayOrder": 1,
      "orderTypes": ["REGULAR", "STOP_LIMIT", "TP_SL", "OCO"]
    }
  ]
}
```

---

## 9. Integration Points

| Service | Endpoint | Integration |
|---------|----------|-------------|
| `aaa` | User profile | Lấy thông tin user (fullName, dob) cho cross-check |
| `Smart-OTP` | OTP service | Gửi OTP, verify OTP (fallback) |
| `Carrier API Gateway` | GSMA Open Gateway CAMARA | Number Verification + SIM Swap APIs (Viettel, VinaPhone, MobiFone) |
| `VNeID / RAR (Phase 2)` | RAR Center C06 | App-to-app identity verification |
| `Biometric System` | Biometric verification | L2 verify cho phone change |
| `Device Fingerprinting` | Device ID | Ghi nhận device trong terms acceptance |
| `Audit Log` | Event logging | Log mọi event phone/terms |
| `eKYC Admin` | eKYC service | Layer 3 fallback (CCCD + face matching) |

---

## 10. Security Considerations

| # | Concern | Mitigation |
|---|---------|------------|
| 1 | Carrier API outage | Fallback to OTP + cross-check; timeout 5s; circuit breaker pattern |
| 2 | Carrier API fraud (spoofed response) | API key + IP whitelist + mutual TLS; verify response signature |
| 3 | SIM swap fraud | Kiểm tra SIM Swap API; nếu phát hiện đổi SIM gần đây → yêu cầu OTP bổ sung |
| 4 | OTP brute force | Rate limit, max 5 attempts, exponential backoff |
| 5 | SĐT thuộc người khác | Carrier API verification (primary) + cross-check (fallback) |
| 6 | Phone change fraud | Carrier re-verify + biometric L2 + notification đến SĐT cũ |
| 7 | Terms acceptance forgery | Device fingerprint + IP logging trong acceptance |
| 8 | Content injection | Admin-only write, input sanitization |

---

**Document Status:** 📋 Draft (v2 — Updated with carrier API + VNeID architecture)
**For:** BE / FE / BA (Pháp chế)
**Next Steps:** 
1. Pháp chế review nội dung terms mẫu
2. Xác nhận cơ chế xác minh SĐT (Carrier API → VNeID → OTP fallback)
3. Research GSMA Open Gateway CAMARA API docs từ Viettel, VinaPhone, MobiFone
4. Thiết kế Carrier API Gateway service (abstract layer + circuit breaker)
5. Phân bổ BE implementation
