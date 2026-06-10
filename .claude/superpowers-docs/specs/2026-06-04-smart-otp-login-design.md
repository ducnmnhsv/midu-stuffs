# Smart OTP — Login Flow Integration Design

**Date:** 2026-06-04
**Status:** Awaiting implementation
**Scope:** Full spec — BE API changes + FE logic + QA test cases
**Related docs:**
- [`Smart OTP - multi channels/SmartOTP_WTS_HTS_Scope_Analysis.md`](../../Smart%20OTP%20-%20multi%20channels/SmartOTP_WTS_HTS_Scope_Analysis.md)
- [`Smart OTP - multi channels/Specifications/SmartOTP_API_Mapping.md`](../../Smart%20OTP%20-%20multi%20channels/Specifications/SmartOTP_API_Mapping.md)
- [`docs/NHSV-Pro-Smart-OTP-PRD.md`](../NHSV-Pro-Smart-OTP-PRD.md)

---

## 1. Overview

Tích hợp Smart OTP vào luồng đăng nhập NHSV Pro (mobile app), đảm bảo backward compatible với user chưa kích hoạt và UX mượt mà cho mọi case.

**Approach:** Backend-driven + FE local key check (Hybrid)
- BE trả `otpType` trong Step 1 response — FE render UI theo đó
- FE bổ sung check local `sotpKey` để phân biệt "đúng thiết bị" vs "mất key / cài lại app"

**Không tạo endpoint mới.** Tất cả API đều đã có sẵn.

---

## 2. Current Login Flow (Baseline)

```
POST /rest/api/v1/login
  body: { grant_type: "password_otp", username, password }
  → temp accessToken

POST /rest/api/v1/notifyMobileOtpNhsv
  → SMS OTP gửi đến user

POST /rest/api/v1/login/sec/verifyOTP
  body: { otpValue: "1234" }
  → final accessToken + refreshToken → login success
```

---

## 3. API Changes

### 3.1 `POST /rest/api/v1/login` — Response thêm 2 fields

**Before:**
```json
{
  "accessToken": "temp_token_xxx",
  "userData": { "mfaData": "RSA_encrypted..." }
}
```

**After:**
```json
{
  "accessToken": "temp_token_xxx",
  "userData": { "mfaData": "RSA_encrypted..." },
  "sotpStatus": "Y",
  "otpType": "SMART_OTP"
}
```

| Field | Type | Values | Source |
|---|---|---|---|
| `sotpStatus` | String | `"Y"` / `"N"` | Map từ `sotp_stat` của Lotte |
| `otpType` | String | `"SMART_OTP"` / `"SMS_OTP"` | BE derive: `sotpStatus == "Y"` → `"SMART_OTP"`, else `"SMS_OTP"` |

### 3.2 `POST /rest/api/v1/login/sec/verifyOTP` — Request thêm 1 optional field

**Before:**
```json
{ "otpValue": "1234" }
```

**After:**
```json
{ "otpValue": "123456", "otpType": "SMART_OTP" }
```

| Field | Type | Required | Default | Ghi chú |
|---|---|---|---|---|
| `otpValue` | String | ✅ | — | 4 digits (SMS) hoặc 6 digits (Smart OTP) |
| `otpType` | String | ❌ | `"SMS_OTP"` | Không truyền → BE dùng flow SMS hiện tại (backward compatible) |

**BE routing logic:**
```
if otpType == "SMART_OTP"
  → POST /rest/api/v1/smartOtp/verify  (xác thực TOTP code)
else  (SMS_OTP hoặc absent)
  → flow verify hiện tại (apache-crypt hash lookup)  — không đổi
```

### 3.3 `POST /rest/api/v1/otp/send` — Dùng lại, không thay đổi interface

FE truyền `txType: "SMART_OTP"` cho luồng kích hoạt lại trong Case C.

---

## 4. FE Decision Tree

Sau khi nhận response từ Step 1 (`/rest/api/v1/login`):

```
sotpStatus == "N"
  → Case B: SMS OTP flow

sotpStatus == "Y" AND local sotpKey tồn tại
  → Case A: Smart OTP flow

sotpStatus == "Y" AND KHÔNG có local sotpKey
  → Case C: Special screen (reinstall / lost device)
```

---

## 5. User Journeys

### Case A — Đã kích hoạt, đúng thiết bị

*Precondition: `sotpStatus = "Y"`, local `sotpKey` tồn tại*

```
Step 1: POST /rest/api/v1/login
  → otpType: "SMART_OTP"

FE: KHÔNG gọi notifyMobileOtpNhsv
FE: Hiển thị Smart OTP UI
    → "Mở app → nhập PIN → lấy mã 6 chữ số"
    → Input 6 digits
    → Link: "Không lấy được mã?" → FE xoá local sotpKey → hiển thị Special Screen (Case C)

Step 2: POST /rest/api/v1/login/sec/verifyOTP
  body: { otpValue: "XXXXXX", otpType: "SMART_OTP" }
  → Login thành công
```

---

### Case B — Chưa kích hoạt Smart OTP

*Precondition: `sotpStatus = "N"`*

```
Step 1: POST /rest/api/v1/login
  → otpType: "SMS_OTP"

FE: Gọi POST /rest/api/v1/notifyMobileOtpNhsv  ← giữ nguyên hiện tại
FE: Hiển thị SMS OTP UI  ← giữ nguyên hiện tại

Step 2: POST /rest/api/v1/login/sec/verifyOTP
  body: { otpValue: "XXXX" }  ← backward compatible, không cần otpType
  → Login thành công

Post-login:
  Phase 1 (soft gate):
    → Banner tại home screen: "Kích hoạt Smart OTP để bảo mật hơn"
    → User có thể dismiss trong phiên, thấy lại lần login tiếp theo

  Phase 2 (hard gate, triển khai sau x tuần):
    → Block toàn bộ app
    → Redirect bắt buộc sang Smart OTP Activation screen
    → Không thể bỏ qua
```

---

### Case C — Đã kích hoạt, không có local key (cài lại app / đổi máy)

*Precondition: `sotpStatus = "Y"`, KHÔNG có local `sotpKey`*

```
Step 1: POST /rest/api/v1/login
  → otpType: "SMART_OTP"

FE: Phát hiện không có local sotpKey
FE: Hiển thị Special Screen

  ┌─────────────────────────────────────────────┐
  │  Thiết bị này chưa có Smart OTP             │
  │                                             │
  │  Smart OTP của bạn chưa được thiết lập      │
  │  trên thiết bị này. Kích hoạt lại để        │
  │  tiếp tục.                                  │
  │                                             │
  │  [PRIMARY]   Kích hoạt lại Smart OTP        │
  │  [SECONDARY] Đăng nhập bằng SMS OTP lần này │
  └─────────────────────────────────────────────┘
```

**Nhánh PRIMARY — In-flow re-activation:**

```
POST /rest/api/v1/otp/send
  body: { txType: "SMART_OTP" }
  → SMS activation OTP gửi đến user

User nhập SMS OTP + tạo PIN mới (6 số)

POST /rest/api/v1/otp/verify
  body: { otpValue: "XXXXXX" }
  → OTP hợp lệ

POST /rest/api/v1/smartOtp/register
  → Activation success
  → Response: sotpKey
  → FE lưu sotpKey vào local secure storage

→ Redirect về Smart OTP UI (Case A)
→ User mở app → nhập PIN → lấy mã

POST /rest/api/v1/login/sec/verifyOTP
  body: { otpValue: "XXXXXX", otpType: "SMART_OTP" }
  → Login thành công, vào app với Smart OTP đã setup
```

**Nhánh SECONDARY — SMS fallback:**

```
POST /rest/api/v1/notifyMobileOtpNhsv  ← cùng endpoint hiện tại
→ SMS OTP gửi đến user

FE: Chuyển sang SMS OTP UI

POST /rest/api/v1/login/sec/verifyOTP
  body: { otpValue: "XXXX", otpType: "SMS_OTP" }
  → Login thành công

→ NGAY LẬP TỨC hard redirect sang Smart OTP Activation screen
  (không dismiss được — user đã từng setup, cần re-activate)
```

> **Constraint cần confirm với BE:** Nhánh PRIMARY dùng temp `accessToken` từ Step 1 để gọi `/rest/api/v1/otp/send` và `/rest/api/v1/smartOtp/register`. Cần xác nhận temp token có đủ permission cho các endpoints này.

---

## 6. Endpoint Inventory

| Endpoint | Trạng thái | Thay đổi |
|---|---|---|
| `POST /rest/api/v1/login` | ✅ Có sẵn | Thêm `sotpStatus` + `otpType` vào response |
| `POST /rest/api/v1/notifyMobileOtpNhsv` | ✅ Có sẵn | Không đổi |
| `POST /rest/api/v1/login/sec/verifyOTP` | ✅ Có sẵn | Thêm optional `otpType` vào request body |
| `POST /rest/api/v1/otp/send` | ✅ Có sẵn | Dùng lại với `txType: "SMART_OTP"` |
| `POST /rest/api/v1/otp/verify` | ✅ Có sẵn | Không đổi |
| `POST /rest/api/v1/smartOtp/register` | ✅ Có sẵn | Không đổi |

---

## 7. QA Test Cases

### Happy Paths

| # | Case | Precondition | Steps | Expected |
|---|---|---|---|---|
| TC-01 | Login Smart OTP — đúng thiết bị | `sotpStatus=Y`, local key khớp | Login → Smart OTP screen → nhập 6-digit code | Login thành công, không gọi `notifyMobileOtpNhsv` |
| TC-02 | Login SMS OTP — chưa kích hoạt | `sotpStatus=N` | Login → SMS OTP screen → nhập 4-digit code | Login thành công, `notifyMobileOtpNhsv` được gọi, show banner Phase 1 |
| TC-03 | Case C PRIMARY — kích hoạt lại trong flow | `sotpStatus=Y`, không có local key | Login → Special screen → "Kích hoạt lại" → nhập SMS OTP + PIN → Smart OTP login | Login thành công, local sotpKey được lưu |
| TC-04 | Case C SECONDARY — SMS fallback | `sotpStatus=Y`, không có local key | Login → Special screen → "SMS OTP lần này" → nhập OTP | Login thành công, hard redirect re-activation ngay sau |

### Edge Cases

| # | Case | Expected |
|---|---|---|
| TC-05 | Smart OTP nhập sai mã | Error "Mã không đúng", cho nhập lại |
| TC-06 | Smart OTP hết hạn 60s trước khi submit | Error "Mã đã hết hạn", user quay lại app lấy mã mới |
| TC-07 | Activation OTP sai quá 5 lần (Case C PRIMARY) | Lock flow, hiển thị thông báo thử lại sau |
| TC-08 | Activation OTP hết hạn (Case C PRIMARY) | Cho phép resend OTP |
| TC-09 | `smartOtp/register` thất bại (lỗi Core) | Error toast, giữ nguyên ở Special screen |
| TC-10 | App cũ gọi `verifyOTP` không có `otpType` | BE dùng SMS flow như cũ, không break |
| TC-11 | Phase 2 hard gate: user cố skip activation | Tất cả navigation bị block, không vào được app |

---

## 8. Rollout Plan

| Phase | Mô tả | Trigger |
|---|---|---|
| **Phase 1** | Soft gate: banner gợi ý kích hoạt Smart OTP sau login (Case B) | Ngay khi release |
| **Phase 2** | Hard gate: block toàn bộ app, bắt buộc kích hoạt | x tuần sau Phase 1 (cần confirm timeline) |

---

## 9. Open Questions

| # | Câu hỏi | Owner |
|---|---|---|
| OQ-1 | Temp accessToken từ `/login` Step 1 có đủ permission gọi `/otp/send` và `/smartOtp/register` không? | BE Lead |
| OQ-2 | Enum chính xác của `sotp_stat` từ Lotte (`Y`/`N` hay khác)? | BE Lead |
| OQ-3 | Timeline cụ thể từ Phase 1 → Phase 2 (soft → hard gate)? | PM |
| OQ-4 | Rate limit cho `otpType: "SMART_OTP"` trong `verifyOTP` (sai bao nhiêu lần thì lock)? | BE Lead |

---

**Document Status:** Draft — ready for team review
**For:** BE, FE, QA, PM
**Next Steps:** Confirm open questions → create Jira tickets (BE + FE + QA) → implementation
