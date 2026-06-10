# SmartOTP (S-OTP) — API Mapping Specification

**Document Type:** API Mapping / Specification  
**Category:** Smart OTP - multi channels  
**Version:** 1.0  
**Date:** May 7, 2026

> **Scope:** (1) TradeX SMS OTP (Send/Verify) để kích hoạt luồng S-OTP (txType = `SMART_OTP`) — đây chỉ là **bước xác thực đầu vào** trước khi TradeX gọi API kích hoạt SmartOTP. (2) Mapping Core (Lotte) ↔ TradeX cho nghiệp vụ **đăng ký SmartOTP** và **xác thực SmartOTP**. (3) Bổ sung field `sotp_stat` / `sotp_sec` từ Lotte vào response **`POST /rest/api/v1/login`** và **`POST /rest/api/v1/login/sec/verifyOTP`** để FE suy ra trạng thái đăng ký S-OTP và so khớp thiết bị (kết hợp `sotpKey` lưu local).

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
| Login (SmartOTP context fields) | POST | `/rest/api/v1/login` |
| Verify secondary OTP after login | POST | `/rest/api/v1/login/sec/verifyOTP` |

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

### 2.3 Login response: `sotp_stat` / `sotp_sec` và suy luận “đúng thiết bị”

| Lotte field | TradeX field (đề xuất) | Ý nghĩa gần đúng |
| --- | --- | --- |
| `sotp_stat` | `sotpStatus` | Trạng thái đăng ký S-OTP trên Core (ví dụ `Y` / `N` — **chốt valid values với Lotte**) |
| `sotp_sec` | `sotpKey` | Khóa sinh mã S-OTP hiện đang gắn với tài khoản trên Core (cùng ý nghĩa với `sotp_sec` sau register trong spec SOTP-102) |

**Giới hạn:** Hai field này **không** chứa `deviceId`. Để FE biết **thiết bị hiện tại có trùng với thiết bị đã đăng ký hay không**, cần **so sánh** `sotpKey` từ API với `sotpKey` đã lưu **secure storage** trên máy sau khi user kích hoạt S-OTP trên thiết bị đó.

| Điều kiện (gợi ý FE) | Diễn giải UX |
| --- | --- |
| `sotpStatus !== Y` (hoặc tương đương “chưa đăng ký”) | Chưa đăng ký S-OTP trên Core → luồng kích hoạt như tài liệu quy trình |
| `sotpStatus === Y` và `sotpKey` **khớp** `sotpKey` local đã lưu | Đây là **thiết bị đã đăng ký** (active trên máy này) |
| `sotpStatus === Y` và **không có** `sotpKey` local (cài mới / xóa app / chưa từng kích hoạt trên máy này) | Đã có đăng ký trên Core nhưng **máy này chưa có `sotpKey`** → có thể là thiết bị khác đã kích hoạt, hoặc máy mất dữ liệu local → UI: kích hoạt lại / chuyển thiết bị |
| `sotpStatus === Y` và có `sotpKey` local nhưng **không khớp** `sotpKey` | Key trên Core đã đổi (thường do **kích hoạt lại trên thiết bị khác**) → máy hiện tại **không** còn là thiết bị active |

**Lưu ý bảo mật:** Trả `sotpKey` xuống client trong login response là **nhạy cảm**. Nên chốt với Security/Lotte: có thể chỉ trả xuống khi thật sự cần cho offline generate, hoặc chỉ dùng server-side và FE dùng flag/so khớp hash thay vì raw key.

### 2.4 Language Mapping

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
| *(Header)* `Accept-Language` | - | - | `lang_code` | Map (§2.4) | Nếu upstream có hỗ trợ |

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
| `data_list[0].sotp_sec` | `sotpKey` | String | Direct | Khóa tạo mã S-OTP (TTL code ~60s theo Core spec) |

**TradeX Response example (200):**

```json
{
  "message": "SUCCESS",
  "sotpStatus": "ACTIVE",
  "sotpKey": "VGswjj4354"
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
| `sotpKey` | String | ✅ | `secret` | Direct | Key tạo S-OTP theo Core spec |
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

## 7. Login & Secondary OTP — SmartOTP Fields (Lotte → TradeX)

Bổ sung mapping cho response của **`POST /rest/api/v1/login`** và **`POST /rest/api/v1/login/sec/verifyOTP`** khi luồng đăng nhập lấy thêm ngữ cảnh SmartOTP từ Lotte (theo field `sotp_stat`, `sotp_sec`).

### 7.1 Lotte → TradeX (response payload)

**Nguyên tắc naming TradeX:** không expose trực tiếp tên snake_case Lotte; map sang camelCase có nghĩa.

| Lotte Field | TradeX Field | Type | Required | Transform | Description |
| --- | --- | --- | ---: | --- | --- |
| `sotp_stat` | `sotpStatus` | String | ❌\* | Direct | Trạng thái đăng ký S-OTP trên Core (ví dụ `Y`/`N` — **chốt enum với Lotte**) |
| `sotp_sec` | `sotpKey` | String | ❌\* | Direct | Khóa sinh mã S-OTP đang hiệu lực trên Core |
| `error_code` | - | - | - | Check `0000` | Áp dụng wrapper Lotte nếu có; TradeX response login giữ chuẩn TradeX |

\* Có mặt khi Lotte/Core trả kèm trong payload login/mfa; nếu không có → FE coi như chưa có ngữ cảnh S-OTP.

### 7.2 Vị trí đề xuất trong JSON response (abstract)

Tùy contract hiện tại của `login` / `verifyOTP` (userInfo, `data`, v.v.), hai field có thể đặt:

- **Option A (ưu tiên):** trong object **user** / **userInfo** (cùng cấp username, account…) — vì gắn tài khoản.
- **Option B:** trong object **`smartOtp`** lồng bên trong response để tách domain.

**Example (minh họa — chỉ là contract đề xuất):**

```json
{
  "accessToken": "...",
  "userInfo": {
    "username": "...",
    "sotpStatus": "Y",
    "sotpKey": "9hADD7pCtfb8"
  }
}
```

### 7.3 Quy tắc FE (tóm tắt)

Chi tiết bảng quyết định xem **§2.3**. Tóm lại:

- **`sotpStatus`** → đã có đăng ký trên Core hay chưa (theo enum Lotte).
- **So khớp thiết bị:** so `sotpKey` (server) với `sotpKey` đã lưu sau kích hoạt trên **thiết bị này**; không có `deviceId` trong 2 field này.

---

## 8. Error Handling Summary

### 8.1 Error Response Format

- **400**: `INVALID_PARAMETER` + `params[]`
- **401/403**: `UNAUTHORIZED` / `TOKEN_EXPIRED` / `FORBIDDEN`
- **422**: pass-through Core/OTP-service error (prefix theo operation)
- **500**: `INTERNAL_ERROR`

---

## 9. Implementation Notes

### 9.1 Service Architecture (logical)

| Component | Role |
|---|---|
| `rest-proxy` | API gateway, JWT validation, routing |
| SmartOTP orchestration service | Validate flow: send/verify SMS OTP → register smartotp → device binding |
| `lotte-bridge` (or core connector) | Call Core endpoints SOTP-102 / SOTP-101 |
| OTP service (used by eKYC template) | Send/verify SMS OTP with `id/idType/txType` |

### 9.2 Key Security Notes (to be finalized)

- **`sotpKey` lifecycle**: Core register (SOTP-102) trả về `sotp_sec` (sotpKey). TradeX nên lưu **server-side** theo `userId` + `deviceId` (và/hoặc encrypt-at-rest) để WTS/HTS verify không cần app cầm `sotpKey`.
- **Device binding**: Core spec không có `deviceId`; device binding nên được TradeX enforce (DB + audit + revoke).
- **Rate limit**: Send/verify OTP nên rate-limit theo `phoneNumber` + `userId` + IP.

---

## 10. References

- `Smart OTP - multi channels/SmartOTP_WTS_HTS_Scope_Analysis.md`
- `Smart OTP - multi channels/Quy_trinh_S_OTP.md`
- Core spec extract: `/Users/ducnguyen/Downloads/S-OTP API.docx` (converted to `/tmp/sotp_api.txt` during analysis)
- Core spec (latest): `/Users/ducnguyen/Downloads/S-OTP API 1.docx` (converted to `/.tmp/S-OTP_API_1.txt` during analysis)
- TradeX Add-ons OpenAPI: `TradeX MCP/Knowledge based/documents-main/API_spec_docs/TradeX API - Addons.yaml` — `ekyc-admin/sendOtp`, `ekyc-admin/verifyOtp`

---

**Document Status:** 📋 Draft (needs confirmation on auth model + sotpKey handling)  
**For:** BA/Dev  
**Next Steps:** Chốt endpoint naming + auth model (WTS/HTS), thống nhất cơ chế quản lý `sotpKey`, rồi tạo Jira backlog triển khai BE/FE/QA  
**Estimated Effort:** 2-4 days (BE) + 2-3 days (FE) + 1-2 days (QA)

