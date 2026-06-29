# Order 2FA — Integration Specification

**Feature:** Xác thực giao dịch 2 lớp (Order 2FA)
**TT134 Reference:** Điều 10 (Xử lý Giao dịch & Xác nhận), Điều 7 (Xác thực)
**Priority:** P0
**Version:** 1.0
**Status:** Draft

---

## 0. Flow Overview

> **Architecture note:** Core (Lotte) đã có sẵn khả năng verify Smart OTP cho đặt lệnh. NHSV Pro app đã handle TOTP code generation (SmartOTPInput từ Login S-OTP). Với Smart OTP, TradeX chỉ cần proxy OTP code kèm order data sang Core — Core verify và process order atomically, không cần challenge/proof-token riêng. Proof token chỉ cần thiết cho Biometric L2 (verify local trên AAA, tách biệt với Core).

```
[User chọn orders] → [Check giá trị]
     │
     ├─ < 10tr → không cần 2FA (silent)
     │
     ├─ 10-100tr → Biometric L2 (Face ID)
     │     → Verify RSA signature (AAA) → Proof token → Confirm order
     │
     └─ ≥ 100tr → Smart OTP (TOTP)
           → App generate code → User enter → Confirm order kèm otpCode
           → lotte-bridge → Core (verify OTP + process order atomic)
     
     [Fallback] Smart OTP unavailable/locked → SMS OTP
           → Send OTP → User enter → Confirm order kèm otpCode
           → lotte-bridge → Core
```

**Hai path khác nhau về architecture:**

| Path | 2FA Challenge | Verify | Confirm |
|---|---|---|---|
| **Smart OTP** | Không cần — app gen code ngay | Core verify atomic | POST confirm kèm `smartOtpCode` |
| **Biometric L2** | Cần — tạo challenge để bind RSA signature | AAA verify locally → proof token | POST confirm kèm `X-2FA-Token` |
| **SMS OTP fallback** | Không cần challenge | Core verify (qua lotte-bridge) | POST confirm kèm `smsOtpCode` |

## 0a. UX Principles

| Principle | Mô tả |
|-----------|-------|
| **Tự động chọn method** | Hệ thống tự chọn method mạnh nhất — không bắt user chọn |
| **Fallback mượt mà** | Smart OTP locked/unavailable → SMS OTP tự động, không block |
| **Minh bạch về bảo mật** | Hiển thị lý do cần 2FA, số tiền, SĐT nhận OTP |
| **Reuse existing** | SmartOTPInput component và TOTP generation đã có từ Login flow — reuse, không build mới |

## 0b. BE / FE Responsibility Split

| Component | Backend | Frontend (App) |
|-----------|---------|----------------|
| **Smart OTP generate** | N/A — không cần | ✅ **Đã có**: TOTP generation trong SmartOTPInput (Login S-OTP) — reuse |
| **Smart OTP verify** | lotte-bridge proxy sang Core — Core verify + process order atomic | SmartOTPInput (6-digit) |
| **Biometric L2 verify** | AAA: RSA signature verify locally → trả proof token | BiometricPromptHandler |
| **SMS OTP** | POST /otp/send (`purpose: ORDER_PLACEMENT`) + Core verify | SMSOTPInput (+ countdown + resend) |
| **Proof token** | Chỉ cần cho Biometric path: JWT 60s, single-use, txHash-bound | Gửi X-2FA-Token header (Biometric path only) |
| **Confirm order** | Middleware check: `smartOtpCode` OR `smsOtpCode` OR `X-2FA-Token` | Update saga flow |

---

## 1. Overview

### 1.1 Problem

Hiện tại, việc xác nhận giao dịch (đặt lệnh, chuyển tiền, chuyển chứng khoán) **chỉ dựa trên access token** — nghĩa là sau khi user đã login, tất cả các hành động confirm đều không yêu cầu xác thực thêm. TT134 yêu cầu các giao dịch quan trọng phải có **xác thực lớp thứ hai** (2FA).

### 1.2 Requirements

| # | Requirement | TT134 Ref |
|---|-------------|-----------|
| R1 | Xác thực giao dịch bằng OTP **hoặc phương thức xác thực thứ hai** | Điều 10.2 |
| R2 | Giao dịch ≥ 10 triệu đồng cần xác thực mạnh hơn (biometric L2) | Điều 10.3 |
| R3 | Hỗ trợ SMS OTP fallback khi thiết bị chính không khả dụng | Điều 7.5 |
| R4 | Lockout tài khoản khi xác thực thất bại quá số lần cho phép | Điều 11.3 |
| R5 | Cảnh báo cho user khi có giao dịch bất thường | Điều 13.4 |
| R6 | Audit trail cho mọi lần xác thực giao dịch | Điều 13.2 |

### 1.3 Current State

| Item | Status | Notes |
|------|--------|-------|
| ConfirmOrdersScreen | ✅ Có sẵn | Hiển thị orders/chuyển tiền/chuyển CK cần xác nhận |
| POST confirm order API | ✅ Có sẵn | Gọi qua `rest-proxy` → Core (Lotte) |
| Smart-OTP (TOTP) | 🏗️ Đang implement | 6-digit TOTP app-based |
| Smart-OTP activation | 🏗️ Đang implement | SMS OTP → register → device binding |
| Biometric enrollment | 📋 Draft plan | RSA key pair per deviceId |
| Login biometric | ✅ Có sẵn | Face ID / fingerprint cho login |
| Login Smart-OTP (SOtpLoginPinScreen) | ✅ Có sẵn | TOTP PIN screen sau khi login |
| SMS OTP service | ✅ Có sẵn | TradeX SMS OTP (txType-based) |

### 1.4 Transactions Requiring 2FA

| Transaction Type | 2FA Required | Threshold | Method |
|-----------------|-------------|-----------|--------|
| Order (đặt lệnh) | ✅ Có | ≥ 100tr VND | Smart-OTP TOTP |
| Order (đặt lệnh) | ✅ Có | ≥ 10tr VND | Biometric L2 |
| Cash transfer (chuyển tiền) | ✅ Có | Mọi giá trị | Smart-OTP TOTP |
| Stock transfer (chuyển CK) | ✅ Có | Mọi giá trị | Smart-OTP TOTP |
| Profile change (đổi SĐT/email) | ✅ Có | N/A | SMS OTP + Smart-OTP |
| Biometric enrollment | ✅ Có | N/A | SMS OTP |
| Password change | ✅ Có | N/A | SMS OTP |

> **Note:** Threshold values cần được confirm với compliance team. Đây là đề xuất ban đầu dựa trên TT134.

---

## 2. Proposed Architecture

### 2.1 2FA Verification Flow

**Path A — Smart OTP (≥ 100tr)**

```
User chọn items → tap "Confirm"
    │
    ├─ FE check totalValue ≥ 100tr → hiển thị Smart OTP screen
    │   [SmartOTPInput — reuse từ Login S-OTP, app đã gen TOTP code]
    │
    ├─ User nhập 6-digit TOTP code → tap "Xác nhận"
    │
    └─ FE: POST /api/v1/order/confirm
           { orders: [...], smartOtpCode: "123456", purpose: "ORDER_PLACEMENT" }
               │
               └─ lotte-bridge → Core
                       Core: verify Smart OTP + process order (atomic)
                       ├─ OTP valid → order confirmed → trả kết quả
                       └─ OTP invalid → trả lỗi → FE retry (max N lần)
```

**Path B — Biometric L2 (10tr ~ 100tr)**

```
User chọn items → tap "Confirm"
    │
    ├─ FE check 10tr ≤ totalValue < 100tr → trigger Biometric prompt
    │
    ├─ FE: POST /api/v1/transactions/2fa/challenge
    │       { transactionType: "ORDER_CONFIRM", transactionData: {...} }
    │       → nhận challengeId + transactionHash
    │
    ├─ FE: BiometricPromptHandler.sign(challengeId + transactionHash)
    │
    ├─ FE: POST /api/v1/transactions/2fa/verify
    │       { challengeId, method: "BIOMETRIC", signature }
    │       → nhận proofToken (JWT 60s, 1-time)
    │
    └─ FE: POST /api/v1/order/confirm kèm X-2FA-Token: proofToken
```

**Path C — SMS OTP Fallback**

```
[Smart OTP locked / unavailable] → FE switch sang SMS path
    │
    ├─ FE: POST /api/v1/otp/send
    │       { purpose: "ORDER_PLACEMENT", transactionRef: pendingOrderRef }
    │
    ├─ User nhập SMS OTP
    │
    └─ FE: POST /api/v1/order/confirm
           { orders: [...], smsOtpCode: "123456", purpose: "ORDER_PLACEMENT" }
               │
               └─ lotte-bridge → Core (verify SMS OTP + process order)
```

### 2.2 2FA Proof Token

Để tránh replay attack và đảm bảo OTP chỉ dùng cho 1 transaction, cần tạo **2FA proof token** — access token ngắn hạn, 1-time, gắn với transaction data:

```json
// 2FA Token Payload
{
  "sub": "2fa_proof",
  "userId": 12345,
  "transactionType": "ORDER_CONFIRM",
  "transactionHash": "sha256(...orderData...)",
  "method": "SMARTOTP",
  "iat": 1718200000,
  "exp": 1718200060   // 60s TTL
}
```

**Flow:**
1. User confirm → backend tạo `2faChallenge` (transactionHash, method, expiresAt)
2. User verify OTP/biometric → backend kiểm tra → trả về 2FA proof token
3. User gọi POST confirm order API kèm 2FA token trong header `X-2FA-Token`
4. Backend verify 2FA token + match transactionHash → xử lý order
5. 2FA token chỉ dùng 1 lần (revoke ngay sau khi dùng)

---

## 3. API Endpoints

### 3.1 Confirm Order — Smart OTP Path (≥ 100tr)

> Core verify Smart OTP và process order atomic. Không cần challenge/proof token riêng.

```
POST /api/v1/order/confirm
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "accountNumber": "123456",
  "subNumber": "001",
  "orders": [
    { "orderDate": "2026-06-13", "orderNumber": "123456" }
  ],
  "smartOtpCode": "482916",
  "purpose": "ORDER_PLACEMENT"
}
```

| Field | Type | Mô tả |
|---|---|---|
| `smartOtpCode` | string | 6-digit TOTP code từ NHSV Pro app |
| `purpose` | enum | `ORDER_PLACEMENT` — bắt buộc để Core log đúng nghiệp vụ |

**Response (success):**
```json
{
  "status": "confirmed",
  "orderCount": 2,
  "totalValue": 150000000,
  "confirmedOrders": [
    { "orderNumber": "123456", "status": "SUCCESS", "message": "Lệnh đã được gửi đến Sở giao dịch" }
  ]
}
```

**Error Codes:**
| Code | HTTP | Mô tả |
|---|---|---|
| `SMART_OTP_REQUIRED` | 401 | Lệnh ≥ 100tr cần Smart OTP, chưa cung cấp |
| `OTP_INVALID` | 400 | OTP sai |
| `OTP_EXPIRED` | 400 | OTP quá 2 phút |
| `SMART_OTP_LOCKED` | 403 | Smart OTP đã bị lock — FE fallback SMS OTP |
| `SMART_OTP_NOT_ACTIVATED` | 403 | Chưa activate Smart OTP — FE fallback SMS OTP |
| `ORDER_PLACE_XXXX` | 422 | Lỗi từ Core (pass-through) |

---

### 3.2 Confirm Order — SMS OTP Fallback Path

```
POST /api/v1/order/confirm
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "accountNumber": "123456",
  "subNumber": "001",
  "orders": [
    { "orderDate": "2026-06-13", "orderNumber": "123456" }
  ],
  "smsOtpCode": "482916",
  "purpose": "ORDER_PLACEMENT"
}
```

> Cùng endpoint với Smart OTP — BE phân biệt qua field `smartOtpCode` vs `smsOtpCode`. Chỉ 1 trong 2 được phép có trong 1 request.

---

### 3.3 2FA Challenge — Biometric L2 Path (10tr ~ 100tr)

> Challenge chỉ cần cho Biometric vì AAA verify RSA signature locally (không qua Core), cần binding transactionHash để prevent replay.

```
POST /api/v1/transactions/2fa/challenge
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "transactionType": "ORDER_CONFIRM",
  "transactionData": { "orders": [...], "totalValue": 50000000 },
  "preferredMethod": "BIOMETRIC"
}
```

**Response:**
```json
{
  "challengeId": "ch_abc123",
  "transactionHash": "sha256hash...",
  "requiredMethod": "BIOMETRIC",
  "expiresAt": "2026-06-13T10:30:00+07:00"
}
```

**Business Rules:**
- `transactionHash` = SHA256(`transactionData` + `userId` + `challengeId`) — server-side
- Challenge TTL: 5 phút; 1 challenge active per user tại 1 thời điểm

---

### 3.4 Verify Biometric + Get Proof Token

```
POST /api/v1/transactions/2fa/verify
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "challengeId": "ch_abc123",
  "method": "BIOMETRIC",
  "signature": "<RSA-SHA256 signature of challengeId+transactionHash>"
}
```

**Response:**
```json
{
  "proofToken": "eyJhbGciOiJSUzI1NiJ9...",
  "proofTokenExpiresAt": "2026-06-13T10:30:15+07:00"
}
```

**Error Codes:**
| Code | HTTP | Mô tả |
|---|---|---|
| `BIOMETRIC_VERIFY_FAILED` | 400 | Signature không hợp lệ |
| `CHALLENGE_EXPIRED` | 400 | Challenge quá 5 phút |
| `TOO_MANY_ATTEMPTS` | 429 | Vượt số lần thử → fallback SMS OTP |

---

### 3.5 Confirm Order — Biometric Path (kèm proof token)

```
POST /api/v1/order/confirm
Authorization: Bearer <access_token>
X-2FA-Token: <proofToken>
```

**Request Body:** (existing order structure, không kèm otpCode)

**Business Rules:**
- Proof token TTL: 60s, single-use, txHash-bound
- Middleware verify token → match transactionHash → revoke → process order

---

### 3.6 Send SMS OTP cho Order (khi cần fallback)

```
POST /api/v1/otp/send
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "purpose": "ORDER_PLACEMENT",
  "transactionRef": "<pendingOrderRef>"
}
```

> SMS OTP khi gửi phải kèm mục đích: `"[NHSV] Mã xác thực ĐẶT LỆNH: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai."`

---

### 3.7 Transaction Types Khác (tham khảo)

| Transaction | Endpoint | `purpose` | 2FA method |
|---|---|---|---|
| Rút tiền < 10M | POST /cash/withdraw/confirm | `CASH_WITHDRAWAL` | Smart OTP / SMS OTP (kèm trong body) |
| Stock transfer | POST /stock-transfer/confirm | `STOCK_TRANSFER` | Smart OTP / SMS OTP (kèm trong body) |
| Profile change | POST /user/profile | — | SMS OTP (spec riêng) |

---

## 4. Biometric L2 for Large Transactions (≥ 10tr)

### 4.1 Concept

Biometric L2 là verify biometric **ngay tại thời điểm confirm transaction** — khác với L1 (login biometric). User phải quét Face ID / vân tay lần nữa để xác nhận giao dịch.

### 4.2 Flow

```
User confirm orders (10tr ~ 100tr)
    │
    ├─ FE detect: totalValue ≥ 10tr
    │
    ├─ Gọi POST /api/v1/transactions/2fa/challenge (preferredMethod: BIOMETRIC)
    │
    ├─ Hiển thị biometric prompt (Face ID / Touch ID)
    │   └─ FAIL → thử lại (tối đa 3 lần) → fallback SMS OTP
    │
    ├─ Biometric success → gọi POST /transactions/2fa/verify
    │   ├─ method: BIOMETRIC
    │   └─ code: biometric_signature (RSA-SHA256 sign challengeId + transactionHash)
    │
    └─ Nhận proofToken → gọi POST confirm order kèm X-2FA-Token
```

### 4.3 Biometric Signature

```javascript
// FE: sign challenge data with biometric private key
const signature = await BiometricService.signData(
  challengeId + transactionHash,
  privateKey  // từ biometric enrollment
);

// BE: verify signature với public key đã lưu
const isValid = crypto.verify(
  'sha256',
  Buffer.from(challengeId + transactionHash),
  publicKey,  // từ t_biometric (lúc enrollment)
  Buffer.from(signature, 'base64')
);
```

### 4.4 Biometric L2 Enrollment Check

| Condition | Action |
|-----------|--------|
| User có biometric enrolled + private key available | Cho phép Biometric L2 |
| User chưa enroll biometric | Yêu cầu enroll (redirect) hoặc fallback Smart-OTP |
| User đã enroll nhưng device không match | Fallback SMS OTP |
| User đã bị lockout biometric | Fallback Smart-OTP |

---

## 5. SMS OTP Fallback

### 5.1 When to Use

- User chưa kích hoạt Smart-OTP
- User mất thiết bị đã đăng ký Smart-OTP
- Biometric L2 thất bại sau N lần (max 3)
- User chọn "Gửi OTP qua SMS" từ màn hình 2FA

### 5.2 API Endpoints

```
POST /api/v1/orderOtp/send
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "transactionType": "ORDER_CONFIRM",
  "phoneNumber": "09*****678"
}
```

```
POST /api/v1/orderOtp/verify
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "challengeId": "ch_abc123",
  "code": "482916"
}
```

**Business Rules:**
- SMS OTP TTL: 60 giây
- Resend cooldown: 30 giây
- Max 5 OTP requests/giờ
- Phone number masked trong response (chỉ hiện 4 số cuối)
- OTP gửi đến SĐT đã đăng ký (từ user profile), không cho nhập SĐT khác

---

## 6. Error Codes Summary

| Code | HTTP | Description |
|------|------|-------------|
| `2FA_REQUIRED` | 401 | Cần xác thực 2 lớp cho transaction này |
| `2FA_PROOF_INVALID` | 401 | Proof token không hợp lệ |
| `2FA_PROOF_EXPIRED` | 401 | Proof token đã hết hạn |
| `2FA_TRANSACTION_MISMATCH` | 401 | Proof token không match với transaction |
| `CHALLENGE_EXPIRED` | 400 | Challenge đã hết hạn (quá 5 phút) |
| `CHALLENGE_NOT_FOUND` | 400 | Challenge ID không tồn tại |
| `INVALID_CODE` | 400 | OTP/code sai |
| `TOO_MANY_ATTEMPTS` | 429 | Vượt quá số lần thử cho phép |
| `MAX_FAILED_ATTEMPTS` | 422 | Đã đạt max fail → lockout tài khoản |
| `BIOMETRIC_NOT_ENROLLED` | 400 | Chưa đăng ký biometric |
| `BIOMETRIC_LOCKOUT` | 403 | Biometric bị tạm khóa do sai quá nhiều lần |
| `SMS_OTP_LIMIT_REACHED` | 429 | Vượt quá giới hạn SMS OTP/giờ |

---

## 7. Security Considerations

| Concern | Mitigation |
|---------|------------|
| OTP replay attack | Challenge single-use, proof token 60s TTL, transactionHash binding |
| OTP interception (Man-in-the-middle) | TOTP code 30s window, SMS OTP 60s TTL, rate limit |
| Biometric signature replay | Signature chứa challengeId + transactionHash (unique per transaction) |
| 2FA token reuse | Proof token revoked ngay sau khi dùng lần đầu |
| OTP brute force | Max 5 attempts/challenge → lock 30 phút |
| SMS OTP flooding | Rate limit: 5 SMS/giờ/user, cooldown 30s |
| Phishing (OTP steal) | Hiển thị transaction details trong OTP screen (SĐT, số tiền) |
| Device theft | Biometric lockout sau N lần fail, fallback SMS cho phép user thu hồi |

---

## 8. FE Integration Plan

### 8.1 ConfirmOrdersScreen Updates

**Current Flow:**
```
ConfirmOrdersScreen
  ├── HeaderOnlineConfirm (tabs: Order / Cash / Stock / Loan)
  ├── ConfirmOrdersHeader (filter/search)
  ├── ConfirmOrdersTable (list items + checkbox)
  └── ModalBottom (confirm button)
      └── ConfirmOrdersRequest saga → POST API → done
```

**Proposed Flow:**
```
ConfirmOrdersScreen
  ├── (unchanged) HeaderOnlineConfirm
  ├── (unchanged) ConfirmOrdersHeader
  ├── (unchanged) ConfirmOrdersTable
  └── ModalBottom (confirm button)
      ├── Check totalValue
      ├── If need 2FA:
      │     └── Show 2FA prompt modal
      │           ├── Biometric prompt (Face ID)
      │           └── OTP input (SmartOTP / SMS fallback)
      └── Else:
            └── Existing flow (no 2FA)
```

### 8.2 New FE Components

| Component | Description |
|-----------|-------------|
| `TwoFAChallengeModal` | Modal hiển thị lựa chọn phương thức 2FA |
| `SmartOTPInput` | 6-digit OTP input (TOTP) |
| `SMSOTPInput` | OTP input + "Gửi lại" button + countdown |
| `BiometricPromptHandler` | Wrapper cho biometric prompt (Face ID / Touch ID) |
| `TwoFAProviderSelector` | Chọn phương thức 2FA (nếu có nhiều option) |

### 8.3 Saga Updates

| Saga | Change |
|------|--------|
| `ConfirmOrdersRequest.ts` | Check transaction value → thêm 2FA flow nếu cần |
| Others (cash, stock, loan) | Thêm 2FA check tương tự |

### 8.4 Auth Saga Updates

| Saga | Change |
|------|--------|
| `OnRefreshTokenInvalid.ts` | Check `2FA_REQUIRED` error → navigate đến ConfirmOrdersScreen (không logout) |

---

## 9. Implementation Plan

### Phase 1: Backend Core (Estimated: 8 days)

1. **2FA Challenge system (3 days)**
   - Tạo `t_2fa_challenge` table:
     ```sql
     CREATE TABLE t_2fa_challenge (
       id          VARCHAR(32) PRIMARY KEY,
       user_id     INT NOT NULL,
       challenge_type VARCHAR(32) NOT NULL,  -- ORDER_CONFIRM, CASH_TRANSFER, etc.
       transaction_hash VARCHAR(64) NOT NULL,
       methods     JSON NOT NULL,            -- available methods
       status      VARCHAR(16) DEFAULT 'PENDING',  -- PENDING, VERIFIED, EXPIRED
       expires_at  DATETIME NOT NULL,
       created_at  DATETIME DEFAULT NOW()
     );
     ```
   - Implement `POST /transactions/2fa/challenge` endpoint
   - Implement `POST /transactions/2fa/verify` endpoint
   - Implement proof token generation + validation

2. **Smart-OTP integration (2 days)**
   - Tích hợp verify Smart-OTP vào 2FA verify flow
   - Kết nối với `/api/v1/smartOtp/verify`
   - Xử lý lỗi pass-through từ Lotte Core

3. **Biometric L2 integration (2 days)**
   - Implement `POST /api/v1/biometric/verify-l2` (khác L1 — dùng transaction data)
   - Verify RSA signature với public key từ enrollment
   - Rate limiting + lockout logic

4. **Middleware + existing endpoints (1 day)**
   - Thêm `require2FA` middleware
   - Apply middleware cho `POST /order/confirm`, `cash-transfer/confirm`, `stock-transfer/confirm`
   - Apply middleware cho `POST /user/profile` (profile change)

### Phase 2: SMS OTP Fallback (Estimated: 3 days)

1. Tạo `POST /api/v1/orderOtp/send` — SMS OTP gửi đến SĐT đã đăng ký
2. Tạo `POST /api/v1/orderOtp/verify` — verify SMS OTP cho transaction
3. Rate limiting + cooldown
4. Audit logging cho SMS OTP events

### Phase 3: FE Integration (Estimated: 5 days)

1. TwoFAChallengeModal + TwoFAProviderSelector
2. SmartOTPInput component (6-digit box)
3. SMSOTPInput (input + resend)
4. BiometricPromptHandler (native module call)
5. Update ConfirmOrders sagas
6. Update Cash/Stock/Loan confirm sagas
7. Update Profile sagas

---

## 10. Dependencies

| Dependency | Blocking | Notes |
|-----------|----------|-------|
| Smart-OTP activation complete | Phase 1 | Cần SmartOTP API để verify TOTP |
| Biometric enrollment spec | Phase 1 | Cần biometric API cho L2 |
| Device Fingerprinting | Phase 3 (optional) | Dùng device fingerprint để bind 2FA methods |
| Session Management | Phase 1 (partial) | 2FA challenge cần access token valid |

---

## 11. Open Questions

| Question | Decision |
|----------|----------|
| Threshold 10tr/100tr có đúng không? | Cần confirm với compliance |
| SmartOTP device binding: 1 device/user? | Hiện tại 1 device, future multi-device |
| User chưa enroll Smart-OTP: force enroll hay SMS fallback? | SMS fallback — không block giao dịch |
| Biometric L2 có verify qua Lotte Core không? | **Không** — verify local trên AAA (RSA signature) |
| Smart OTP verify có qua Core không? | **Có** — Core verify TOTP và process order atomic (đã clarify) |
| SmartOTPInput component có sẵn chưa? | **Có** — NHSV Pro app đã có từ Login S-OTP flow, reuse |
| Admin transactions có cần 2FA không? | Có — configurable |

---

## 12. References

- TT134/2024/TT-BTC Điều 7 — Xác thực
- TT134/2024/TT-BTC Điều 10 — Xử lý giao dịch & xác nhận
- TT134/2024/TT-BTC Điều 11 — Quản lý rủi ro
- TT134/2024/TT-BTC Điều 13 — Giám sát & báo cáo
- [Smart-OTP API Mapping](../../Smart-OTP/Specifications/SmartOTP_API_Mapping.md)
- [Biometric System draft plan](../Biometric%20System/biometric.html)
- [ConfirmOrdersScreen FE](../../../nhsv-mts-rn/src/screens/ConfirmOrdersScreen/)

---

## Status

- **Spec:** Draft v1.0
- **Phase 1:** Pending (blocked by Smart-OTP activation completion)
- **Next:** Backend implementation (Phase 1) after spec review

---

*Last updated: 2026-06-13*
