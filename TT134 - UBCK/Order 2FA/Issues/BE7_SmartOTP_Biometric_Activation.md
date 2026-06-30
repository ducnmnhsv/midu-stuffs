# BE-7: Smart OTP Activation — SMS OTP + Biometric (Điều 8.5b)

**TT134 Reference:** Điều 8 khoản 5b — Khi Soft OTP dùng lần đầu hoặc trên thiết bị mới → bắt buộc verify SMS OTP + biometric trước khi activate  
**Parent issue:** [TT134-P0-01 OTP TTL Compliance](./OTP_TTL_Compliance_Issue.md)  
**Priority:** 🔴 P0 — Critical  
**Deadline:** 14/08/2026  
**For:** BE Lead · FE Lead · Pháp chế

---

## 1. Bối cảnh

Điều 8.5b yêu cầu: khi KH kích hoạt Smart OTP lần đầu hoặc trên thiết bị mới, hệ thống phải xác thực **đồng thời SMS OTP + biometric** trước khi cho phép activation.

**"Biometric" ở đây là gì?**  
Không phải biometric C06/VNPT (đang blocked bởi BOM GATE). Đây là **device-level biometric** — Face ID / Touch ID do OS cấp (iOS LocalAuthentication, Android BiometricPrompt) — kết hợp với cơ chế RSA signature đã có sẵn trong AAA service.

---

## 2. Luồng hiện tại vs. luồng TT134

### Hiện tại

```
FE → /smartOtp/activationOtp/send
FE → /smartOtp/activationOtp/verify  →  otpKey
FE → /smartOtp/register(otpKey)      →  Core SOTP-102  ✓
```

### Sau BE-7

```
FE → /smartOtp/activationOtp/send
FE → /smartOtp/activationOtp/verify        →  otpKey
FE → /smartOtp/biometricCheck              →  biometricRegistered: true/false  [MỚI]
       ├─ false → flow đăng ký biometric   →  /aaa/biometricRegister  [đã có]
       └─ true  → tiếp tục
FE → trigger Face ID / Touch ID (device OS)
     ký username.toUpperCase() bằng RSA private key (Secure Enclave / Keystore)
FE → /smartOtp/biometricVerify(signatureValue, deviceId)  →  biometricToken  [MỚI]
FE → /smartOtp/register(otpKey + biometricToken)          →  Core SOTP-102   [SỬA]
```

---

## 3. Kết quả kiểm tra AAA service

AAA (`aaa-main`) đã có đầy đủ hạ tầng. **Không cần xây mới phần AAA.**

### Bảng `t_biometric` — đã có

| Column | Mục đích |
|---|---|
| `user_id`, `username` | Định danh tài khoản |
| `public_key` | RSA public key của thiết bị (client tạo, server lưu) |
| `device_id` | Binding thiết bị |
| `status` | `ACTIVE` / `INACTIVE` |
| `biometric_type` | Face ID, Touch ID, ... |
| `is_deleted`, `delete_reason` | Soft delete khi đổi thiết bị / đổi password |

### Internal Kafka URI đã có trong AAA

| URI | Hàm | Dùng cho BE-7 |
|---|---|---|
| `POST /api/v1/biometricRegister` | `registerBiometric()` | Đăng ký public key RSA — yêu cầu password |
| `POST /api/v1/verifyPwdBiometric` | `verifyPwdBiometric()` | Verify RSA-SHA256 signature → xác nhận user cầm thiết bị |
| `POST /api/v1/queryBiometricStatus` | `queryBiometricStatus()` | Check `t_biometric` ACTIVE chưa theo `userId + deviceId` |
| `POST /api/v1/verifyBiometricOtp` | `verifyBiometricOTP()` | Verify OTP khi kích hoạt biometric lần đầu |

**Cơ chế verify đã production-ready** (`BiometricService.ts:381`):
```typescript
const verify = crypto.createVerify("RSA-SHA256");
verify.update(username.toUpperCase());
if (!verify.verify(publicKey, signature, "base64")) {
  throw new Errors.GeneralError(BIOMETRIC_KEYS.VERIFY_FAILED);
}
```

### Env flags đã có

| Flag | Default | Ý nghĩa |
|---|---|---|
| `TRADEX_ENV_ENABLE_BIOMETRIC` | `false` | Bật/tắt biometric toàn hệ thống |
| `TRADEX_ENV_BIOMETRIC_VALIDATE_PASSWORD` | `false` | Bắt verify password khi register biometric |

---

## 4. Thay đổi cần làm

### 4.1 SmartOTP service — Endpoint mới: `POST /api/v1/smartOtp/biometricCheck`

Gọi đầu tiên trong activation flow để FE biết cần làm gì.

**Request:**
```json
{
  "deviceId": "device-uuid-string"
}
```

**Logic:**
1. Gọi AAA `POST /api/v1/queryBiometricStatus` với `{ userId (từ JWT), deviceId }`
2. Trả về kết quả

**Response:**
```json
{
  "biometricRegistered": true,
  "deviceId": "device-uuid-string"
}
```

**FE xử lý:**
- `biometricRegistered: false` → navigate sang flow đăng ký biometric login trước
- `biometricRegistered: true` → tiếp tục activation flow

---

### 4.2 SmartOTP service — Endpoint mới: `POST /api/v1/smartOtp/biometricVerify`

Nhận signature từ FE, gọi AAA verify, trả `biometricToken`.

**Request:**
```json
{
  "signatureValue": "base64-encoded-rsa-signature",
  "deviceId": "device-uuid-string"
}
```

**Logic:**
1. Gọi AAA `POST /api/v1/verifyPwdBiometric` với `{ signatureValue, deviceId, username (từ JWT) }`
2. AAA tìm `t_biometric` theo `username + deviceId`, verify RSA-SHA256 signature
3. Nếu verify OK → issue `biometricToken` (JWT ngắn hạn, TTL 10 phút, `scope: SMARTOTP_ACTIVATION`)
4. Nếu verify fail → trả lỗi tương ứng

**Response (success):**
```json
{
  "biometricToken": "eyJ...",
  "expiresAt": "2026-08-01T10:10:00Z"
}
```

**Error mapping:**

| Trường hợp | Error code | HTTP |
|---|---|---|
| `t_biometric` không tìm thấy / INACTIVE | `BIOMETRIC_NOT_REGISTERED` | 400 |
| Signature sai | `BIOMETRIC_VERIFY_FAILED` | 400 |
| `t_biometric` gắn với thiết bị khác | `BIOMETRIC_ACTIVE_ON_ANOTHER_DEVICE` | 400 |

---

### 4.3 SmartOTP service — Sửa: `POST /api/v1/smartOtp/register`

Thêm validation `biometricToken` bắt buộc trước khi gọi Core SOTP-102.

**Request thêm field:**
```json
{
  "otpKey": "existing-field",
  "biometricToken": "eyJ..."
}
```

**Logic thêm (trước khi gọi Core):**
```
1. Validate biometricToken:
   - Chưa hết hạn (TTL 10 phút)
   - scope == "SMARTOTP_ACTIVATION"
   - userId trong token khớp với JWT của request
   - Token chưa được dùng (mark used sau khi validate)
2. Nếu không hợp lệ → 400 BIOMETRIC_VERIFICATION_REQUIRED
3. Nếu hợp lệ → tiếp tục gọi Core SOTP-102 như hiện tại
```

---

### 4.4 AAA service — `POST /api/v1/biometricRegister` (KHÔNG SỬA CODE)

Flow đăng ký biometric đã có. Chỉ cần đảm bảo:
- `TRADEX_ENV_ENABLE_BIOMETRIC = true` trên prod
- `TRADEX_ENV_BIOMETRIC_VALIDATE_PASSWORD = true` (bắt verify password khi đăng ký)

FE phải generate RSA key pair trên thiết bị trước khi gọi endpoint này:
- iOS: Secure Enclave (private key không exportable)
- Android: Android Keystore (hardware-backed nếu có)
- Gửi lên: `publicKey` (base64 DER format)

---

### 4.5 FE — Sửa màn Kích Hoạt Smart OTP (`01_FE_Issue_Kich_Hoat_SmartOTP.md`)

Thêm vào decision table sau bước status check:

```
Sau khi xác định cần activate (sotpStatus != Y hoặc thiết bị mới):
  ↓
  Gọi /smartOtp/biometricCheck
    ├─ biometricRegistered: false
    │    → Toast: "Vui lòng đăng ký Face ID / Touch ID để kích hoạt Smart OTP"
    │    → Navigate sang màn đăng ký biometric login
    │    → Sau khi đăng ký xong → quay lại activation flow
    │
    └─ biometricRegistered: true
         → Bước 1: Gửi SMS OTP (/smartOtp/activationOtp/send)
         → Bước 2: User nhập SMS OTP → verify → nhận otpKey
         → Bước 3: Hiển thị prompt "Xác thực Face ID / Touch ID để tiếp tục"
                   Trigger device OS biometric
                   Ký username.toUpperCase() bằng RSA private key từ Keystore
                   Gọi /smartOtp/biometricVerify → nhận biometricToken
         → Bước 4: User nhập PIN
         → Bước 5: /smartOtp/register(otpKey + biometricToken + PIN)
```

**UX khi biometric fail:**

| Trường hợp | UX |
|---|---|
| Face ID không nhận dạng | Retry tối đa 3 lần → sau đó yêu cầu passcode thiết bị |
| `BIOMETRIC_NOT_REGISTERED` | Navigate sang đăng ký biometric |
| `BIOMETRIC_ACTIVE_ON_ANOTHER_DEVICE` | "Face ID đang đăng ký trên thiết bị khác. Vui lòng liên hệ NHSV hỗ trợ." |
| `BIOMETRIC_TOKEN_EXPIRED` (10 phút trôi qua) | "Phiên xác thực hết hạn. Vui lòng thực hiện lại." → restart từ đầu |

---

## 5. Error codes tổng hợp

| Code | HTTP | Mô tả | FE xử lý |
|---|---|---|---|
| `BIOMETRIC_NOT_REGISTERED` | 400 | Chưa đăng ký biometric login trên thiết bị này | Navigate sang đăng ký biometric |
| `BIOMETRIC_VERIFY_FAILED` | 400 | Signature RSA không khớp | Thông báo lỗi, cho retry |
| `BIOMETRIC_ACTIVE_ON_ANOTHER_DEVICE` | 400 | Public key đang gắn với device khác | Liên hệ CSH |
| `BIOMETRIC_VERIFICATION_REQUIRED` | 400 | Gọi /register thiếu `biometricToken` | Bug FE, không được xảy ra |
| `BIOMETRIC_TOKEN_EXPIRED` | 400 | `biometricToken` quá 10 phút | Yêu cầu Face ID lại |

---

## 6. Sequence diagram đầy đủ

```
FE                 SmartOTP-svc          AAA               Core Lotte
 │                      │                 │                     │
 │── biometricCheck ────►│                 │                     │
 │                      │── queryStatus ──►│                     │
 │                      │◄── ACTIVE ───────│                     │
 │◄── registered: true ─│                 │                     │
 │                      │                 │                     │
 │── activationOtp/send ►│                 │                     │
 │◄── otpId ────────────│                 │                     │
 │                      │                 │                     │
 │── activationOtp/     │                 │                     │
 │   verify(otp) ───────►│                 │                     │
 │◄── otpKey ───────────│                 │                     │
 │                      │                 │                     │
 │  [Face ID / Touch ID prompt]           │                     │
 │  [sign username với RSA Keystore]      │                     │
 │                      │                 │                     │
 │── biometricVerify ───►│                 │                     │
 │   (signatureValue)   │── verifyPwd ───►│                     │
 │                      │   Biometric     │                     │
 │                      │◄── verified ────│                     │
 │◄── biometricToken ───│                 │                     │
 │                      │                 │                     │
 │── register(otpKey +  │                 │                     │
 │   biometricToken) ───►│                 │                     │
 │                      │  validate both  │                     │
 │                      │─────────────────────── SOTP-102 ──────►│
 │                      │◄─────────────────────── sotpKey ────────│
 │◄── { sotpStatus,     │                 │                     │
 │      sotpKey } ──────│                 │                     │
```

---

## 7. Trigger — khi nào áp dụng BE-7

Từ decision table của `01_FE_Issue_Kich_Hoat_SmartOTP.md`:

| Case | Áp dụng BE-7? |
|---|---|
| `sotpStatus !== Y` — chưa bao giờ activate | ✅ Lần đầu → bắt buộc |
| `sotpStatus === Y`, `localSotpKey !== sotpKey` — thiết bị khác | ✅ Thiết bị mới → bắt buộc |
| `sotpStatus === Y`, `localSotpKey === sotpKey` — đúng thiết bị | ❌ Không trigger |
| Re-activate sau reset PIN | ✅ Thiết bị mới hoặc mất state → bắt buộc |

---

## 8. Dependencies

| Dependency | Owner | Status | Risk |
|---|---|---|---|
| FE đã implement RSA key pair generation + Keystore/Secure Enclave | FE Lead | ❓ Cần confirm | 🔴 Critical — nếu chưa có, cả flow collapse |
| `TRADEX_ENV_ENABLE_BIOMETRIC` trạng thái trên prod | DevOps / BE | ❓ Cần confirm | 🔴 Critical |
| `biometricToken` issuer: AAA JWT hay SmartOTP tự issue | BE Lead | ❓ Cần chốt | 🟡 P1 |
| Pháp chế confirm fallback: thiết bị không có biometric có dùng password không | Pháp chế | ❓ Chưa hỏi | 🟡 P1 — ảnh hưởng edge case logic |
| AAA `verifyPwdBiometric` endpoint hoạt động đúng trên môi trường NHSV | BE Lead | ❓ Cần test | 🟡 P1 |

---

## 9. Scope KHÔNG nằm trong BE-7

- Biometric server-side face matching (C06/VNPT) → đó là Điều 9, thuộc Biometric GATE (blocked)
- Biometric cho giao dịch GDCK đầu phiên → Điều 9, blocked GATE
- Biometric cho rút tiền ≥ 10M → Điều 9, blocked GATE

BE-7 chỉ scope: **device-level biometric (Face ID/Touch ID) cho việc kích hoạt Smart OTP** — hoàn toàn độc lập với GATE.

---

Document Status: 📋 Draft | For: BE Lead · FE Lead · Pháp chế | Next Steps: (1) FE Lead confirm RSA Keystore đã implement chưa → (2) BE Lead chốt biometricToken issuer → (3) Pháp chế confirm fallback policy → (4) Kickoff implementation
