# SmartOTP (S-OTP) — API Mapping Specification

**Document Type:** API Mapping / Specification  
**Category:** Smart OTP - multi channels  
**Version:** 1.0  
**Date:** May 7, 2026

> **Scope:** (1) TradeX SMS OTP (Send/Verify) để kích hoạt luồng S-OTP (txType = `SMART_OTP`) — đây chỉ là **bước xác thực đầu vào** trước khi TradeX gọi API kích hoạt SmartOTP. (2) Mapping Core (Lotte) ↔ TradeX cho nghiệp vụ **đăng ký SmartOTP** và **xác thực SmartOTP**.

---

## 1. Overview

### 1.1 Purpose

Tài liệu này mô tả mapping API cho luồng **S-OTP / SmartOTP** theo 2 chặng:

- **Chặng A (TradeX tự triển khai):** Xác thực OTP SMS gửi về SĐT khách hàng khi user bắt đầu kích hoạt SmartOTP trên app (Send/Verify OTP, `txType=SMART_OTP`).
- **Chặng B (TradeX ↔ Core/Lotte):** Thực hiện gọi Core để **đăng ký SmartOTP** và phục vụ các kênh (WTS/HTS) **verify SmartOTP**.

### 1.2 API Endpoints (TradeX)

| Operation | Method | Endpoint |
| --- | ---: | --- |
| Send Activation SMS OTP | POST | `/api/v1/smartOtp/activationOtp/send` |
| Verify Activation SMS OTP | POST | `/api/v1/smartOtp/activationOtp/verify` |
| Register SmartOTP (Core) | POST | `/api/v1/smartOtp/register` |
| Verify SmartOTP (Core) | POST | `/api/v1/smartOtp/verify` |

### 1.3 Response Format Standards (TradeX)

**Success (Mutation):**

```json
{
  "message": "SUCCESS",
  "data": {}
}
```

**Error (Validation – 400):**

```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "phoneNumber" }
  ]
}
```

**Error (Auth – 401/403):**

```json
{
  "code": "UNAUTHORIZED",
  "message": "Token không hợp lệ hoặc đã hết hạn"
}
```

**Error (Core business – 422, pass-through):**

```json
{
  "code": "SMARTOTP_REGISTER_1005",
  "message": "ERROR_DESC_FROM_CORE"
}
```

---

## 2. Business Rules

### 2.1 Activation OTP (SMS OTP) Rules

| Rule | Description | Error Code |
| --- | --- | --- |
| OTP TTL | OTP tồn tại \(≈ 60s\) | `OTP_EXPIRED` |
| Wrong OTP limit | Sai OTP quá giới hạn → khóa theo rule của OTP service | `INCORRECT_OTP_MAX` / `PHONENO_LOCK_INCORRECT_OTP_MAX` |
| txType | Khi kích hoạt SmartOTP, `txType` phải là `SMART_OTP` | `INVALID_PARAMETER` |

### 2.2 SmartOTP Device Binding Rules (Phase 1)

Theo scope hiện tại, mỗi tài khoản chỉ active SmartOTP trên **một thiết bị MTS** tại một thời điểm; kích hoạt trên thiết bị mới sẽ làm thiết bị cũ inactive.

### 2.3 Language Mapping

| Accept-Language | Core `lang_code` | Note |
| --- | --- | --- |
| `vi` | `V` | Default |
| `en` | `E` | |
| `ko` | `K` | |

---

## 3. API: Send Activation SMS OTP (TradeX)

### 3.1 Request

**Endpoint:** `POST /api/v1/smartOtp/activationOtp/send`

**Upstream template (reference):** TradeX Add-ons OpenAPI: `POST /api/v1/ekyc-admin/sendOtp` (structure id/idType/txType)

**Headers:**

- `Authorization: jwt {ACCESS_TOKEN}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 3.2 Request Mapping

**TradeX → OTP service request:**

| TradeX Field | Type | Required | Upstream Field | Transform | Description |
| --- | --- | ---: | --- | --- | --- |
| `phoneNumber` | String | ✅ | `id` | Direct | SĐT nhận OTP (format E.164 khuyến nghị, ví dụ `+84...`) |
| `txType` | String | ✅ | `txType` | Fixed: `SMART_OTP` | Luôn cố định cho luồng kích hoạt SmartOTP |
| `idType` | String | ✅ | `idType` | Fixed: `PHONE_NO` | Theo template |
| *(Header)* `Accept-Language` | - | - | `lang_code` | Map (§2.3) | Nếu upstream có hỗ trợ |

### 3.3 Response Mapping

**Upstream response (template):** trả về `otpId`, `expiredTime`.

**TradeX Response (200):**

| Upstream Field | TradeX Field | Type | Transform | Description |
|---|---|---|---|---|
| `otpId` | `otpId` | String | Direct | Khóa dùng cho bước verify |
| `expiredTime` | `expiredTime` | String | Direct | Thời điểm hết hạn (format upstream) |

**TradeX Response example:**

```json
{
  "message": "SUCCESS",
  "otpId": "bbc40183-fc8c-4642-a884-7224da9f3387",
  "expiredTime": "20231031100058"
}
```

### 3.4 Error Mapping

**Validation Error (400):**

| Field | Error Code | Condition |
|---|---|---|
| `phoneNumber` | `FIELD_IS_REQUIRED` | Missing |
| `phoneNumber` | `INVALID_VALUE` | Không đúng format |

**Auth Error (401/403):** theo chuẩn TradeX (JWT).

**Business Error (422 – upstream pass-through):**

| Upstream Code | TradeX Code | Description |
|---|---|---|
| `PHONENO_LOCK_INCORRECT_OTP_MAX` | `SMARTOTP_ACTIVATION_OTP_SEND_PHONENO_LOCK_INCORRECT_OTP_MAX` | SĐT bị khóa do sai OTP quá giới hạn |

---

## 4. API: Verify Activation SMS OTP (TradeX)

### 4.1 Request

**Endpoint:** `POST /api/v1/smartOtp/activationOtp/verify`

**Upstream template (reference):** TradeX Add-ons OpenAPI: `POST /api/v1/ekyc-admin/verifyOtp`

**Headers:**
- `Authorization: jwt {ACCESS_TOKEN}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 4.2 Request Mapping

| TradeX Field | Type | Required | Upstream Field | Transform | Description |
|---|---|---:|---|---|---|
| `otpId` | String | ✅ | `otpId` | Direct | `otpId` từ bước Send |
| `otpValue` | String | ✅ | `otpValue` | Direct | OTP 6 số |

### 4.3 Response Mapping

**Upstream response (template):** trả về `otpKey`, `expiredTime`.

| Upstream Field | TradeX Field | Type | Transform | Description |
|---|---|---|---|---|
| `otpKey` | `otpKey` | String | Direct | Token chứng minh đã verify OTP; dùng cho bước kích hoạt register SmartOTP |
| `expiredTime` | `expiredTime` | String | Direct | TTL cho `otpKey` (theo upstream) |

**TradeX Response example:**

```json
{
  "message": "SUCCESS",
  "otpKey": "laksd-alkjdlakjc-123091283i",
  "expiredTime": "20231031100058"
}
```

### 4.4 Error Mapping

**Business Error (422 – upstream pass-through):**

| Upstream Code | TradeX Code | Description |
|---|---|---|
| `INCORRECT_OTP` | `SMARTOTP_ACTIVATION_OTP_VERIFY_INCORRECT_OTP` | OTP sai |
| `OTP_EXPIRED` | `SMARTOTP_ACTIVATION_OTP_VERIFY_OTP_EXPIRED` | OTP hết hạn |
| `INCORRECT_OTP_MAX` | `SMARTOTP_ACTIVATION_OTP_VERIFY_INCORRECT_OTP_MAX` | Sai OTP quá giới hạn |

---

## 5. API: Register SmartOTP (Core/Lotte → TradeX)

> **Core Doc Source:** `S-OTP API 1.docx` — API `SOTP-102: Đăng ký S-OTP`

### 5.1 Request

**Endpoint:** `POST /api/v1/smartOtp/register`

**Core Endpoint:** `POST [RootURL]/tsol/apikey/sotp/v2/register-sotp` (SOTP-102)

**Headers:**
- `Authorization: jwt {ACCESS_TOKEN}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 5.2 Request Mapping

**TradeX → Core:**

| TradeX Field | Type | Required | Core Field | Transform | Description |
|---|---|---:|---|---|---|
| *(JWT)* `userId` | String | - | `hts_user_id` | Auto | User ID (HTS user) |
| `mode` | String | ✅ | `mode` | Fixed: `Y` | `Y`: đăng ký smart OTP (theo Core spec) |
| `otpKey` | String | ✅ | - | Validation only | Token từ bước Verify SMS OTP (TradeX nội bộ) |
| `deviceId` | String | ✅ | - | TradeX only | Dùng để bind device (TradeX quản lý), **không gửi Core** theo Core spec hiện tại |

**Notes:**
- Core SOTP-102 chỉ có `mode`, `hts_user_id`. Các khái niệm device binding / otpKey là phần **TradeX orchestration**.

### 5.3 Response Mapping

**Core Success indicator:** `error_code == "0000"`.

| Core Field | TradeX Field | Type | Transform | Description |
|---|---|---|---|---|
| `error_desc` | `message` | String | Direct | Pass-through |
| `data_list[0].sotp_stat` | `sotpStatus` | String | Direct | Trạng thái S-OTP |
| `data_list[0].sotp_sec` | `secret` | String | Direct | Khóa tạo mã S-OTP (TTL code ~60s theo Core spec) |

**TradeX Response example (200):**

```json
{
  "message": "SUCCESS",
  "sotpStatus": "ACTIVE",
  "secret": "VGswjj4354"
}
```

### 5.4 Error Mapping

**Business Error (422 – Core pass-through):**

| Core `error_code` | TradeX Code | Description |
|---|---|---|
| `1005` | `SMARTOTP_REGISTER_1005` | Core báo không thành công (pass-through `error_desc`) |

---

## 6. API: Verify SmartOTP (Core/Lotte → TradeX)

> **Core Doc Source:** `S-OTP API 1.docx` — API `SOTP-101: Xác thực S-OTP`

### 6.1 Request

**Endpoint:** `POST /api/v1/smartOtp/verify`

**Core Endpoint:** `POST [RootURL]/tsol/apikey/sotp/v2/verify-totp` (SOTP-101)

**Headers:**
- `Authorization: jwt {ACCESS_TOKEN}` (kênh WTS/HTS có thể dùng token riêng; cần chốt auth model)
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 6.2 Request Mapping

| TradeX Field | Type | Required | Core Field | Transform | Description |
|---|---|---:|---|---|---|
| *(JWT)* `userId` | String | - | `user_id` | Auto | User ID |
| `secret` | String | ✅ | `secret` | Direct | Key tạo S-OTP theo Core spec |
| `otpCode` | String | ✅ | `code` | Direct | Mã S-OTP |
| `channel` | String | ✅ | - | TradeX only | `WTS` / `HTS` (audit/rate limit), **không gửi Core** theo Core spec hiện tại |

### 6.3 Response Mapping

| Core Field | TradeX Field | Type | Transform | Description |
|---|---|---|---|---|
| `data_list[0].auth_result` | `authResult` | String | Direct | `AUTHENTICATED` / `NOT_AUTHENTICATED` |
| `error_desc` | `message` | String | Direct | Pass-through |

**TradeX Response example (200):**

```json
{
  "message": "SUCCESS",
  "authResult": "AUTHENTICATED"
}
```

### 6.4 Error Mapping

**Business Error (422 – Core pass-through):**

| Core `error_code` | TradeX Code | Description |
|---|---|---|
| `1005` | `SMARTOTP_VERIFY_1005` | Verify không thành công (pass-through `error_desc`) |

---

## 7. Error Handling Summary

### 7.1 Error Response Format

- **400**: `INVALID_PARAMETER` + `params[]`
- **401/403**: `UNAUTHORIZED` / `TOKEN_EXPIRED` / `FORBIDDEN`
- **422**: pass-through Core/OTP-service error (prefix theo operation)
- **500**: `INTERNAL_ERROR`

---

## 8. Implementation Notes

### 8.1 Service Architecture (logical)

| Component | Role |
|---|---|
| `rest-proxy` | API gateway, JWT validation, routing |
| SmartOTP orchestration service | Validate flow: send/verify SMS OTP → register smartotp → device binding |
| `lotte-bridge` (or core connector) | Call Core endpoints SOTP-102 / SOTP-101 |
| OTP service (used by eKYC template) | Send/verify SMS OTP with `id/idType/txType` |

### 8.2 Key Security Notes (to be finalized)

- **`secret` lifecycle**: Core register (SOTP-102) trả về `sotp_sec` (secret). TradeX nên lưu **server-side** theo `userId` + `deviceId` (và/hoặc encrypt-at-rest) để WTS/HTS verify không cần app cầm `secret`.
- **Device binding**: Core spec không có `deviceId`; device binding nên được TradeX enforce (DB + audit + revoke).
- **Rate limit**: Send/verify OTP nên rate-limit theo `phoneNumber` + `userId` + IP.

---

## 9. References

- `Smart OTP - multi channels/SmartOTP_WTS_HTS_Scope_Analysis.md`
- `Smart OTP - multi channels/Quy_trinh_S_OTP.md`
- Core spec extract: `/Users/ducnguyen/Downloads/S-OTP API.docx` (converted to `/tmp/sotp_api.txt` during analysis)
- Core spec (latest): `/Users/ducnguyen/Downloads/S-OTP API 1.docx` (converted to `/.tmp/S-OTP_API_1.txt` during analysis)
- TradeX Add-ons OpenAPI: `TradeX MCP/Knowledge based/documents-main/API_spec_docs/TradeX API - Addons.yaml` — `ekyc-admin/sendOtp`, `ekyc-admin/verifyOtp`

---

**Document Status:** 📋 Draft (needs confirmation on auth model + secret handling)  
**For:** BA/Dev  
**Next Steps:** Chốt endpoint naming + auth model (WTS/HTS), thống nhất cơ chế quản lý `secret`, rồi tạo Jira backlog triển khai BE/FE/QA  
**Estimated Effort:** 2-4 days (BE) + 2-3 days (FE) + 1-2 days (QA)

