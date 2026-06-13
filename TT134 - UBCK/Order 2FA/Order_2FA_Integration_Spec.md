# Order 2FA — Integration Specification

**Feature:** Xác thực giao dịch 2 lớp (Order 2FA)
**TT134 Reference:** Điều 10 (Xử lý Giao dịch & Xác nhận), Điều 7 (Xác thực)
**Priority:** P0
**Version:** 1.0
**Status:** Draft

---

## 0. Flow Overview

```
[User chọn orders] → [Check giá trị]
     │
     ├─ < 10tr → không cần 2FA (silent)
     │
     ├─ 10-100tr → Biometric L2 (Face ID)
     │     → Verify signature → Proof token → Confirm
     │
     └─ ≥ 100tr → Smart-OTP TOTP
           → Verify OTP → Proof token → Confirm
     
     [Fallback] Nếu method chính unavailable → SMS OTP
```

| Bước | Mô tả | BE/FE |
|------|-------|-------|
| 1 | FE check tổng giá trị giao dịch, tự động chọn method | FE |
| 2 | Tạo 2FA challenge (POST /transactions/2fa/challenge) | BE |
| 3 | User xác thực: Biometric L2 / OTP input | FE |
| 4 | Verify + trả proof token (JWT 60s, 1-time) | BE |
| 5 | Confirm order kèm X-2FA-Token header | BE |

## 0a. UX Principles

| Principle | Mô tả |
|-----------|-------|
| **Tự động chọn method** | Hệ thống tự chọn method mạnh nhất — không bắt user chọn |
| **Fallback mượt mà** | Nếu method chính không khả dụng → tự động SMS OTP, không block |
| **Minh bạch về bảo mật** | Hiển thị lý do cần 2FA, số tiền, SĐT nhận OTP |

## 0b. BE / FE Responsibility Split

| Component | Backend (AAA) | Frontend (App) |
|-----------|---------------|----------------|
| 2FA Challenge | t_2fa_challenge, POST challenge | TwoFAChallengeModal |
| Smart-OTP verify | Proxy qua Lotte Core verify TOTP | SmartOTPInput (6-digit) |
| Biometric L2 verify | RSA signature verify (local) | BiometricPromptHandler |
| SMS OTP | POST orderOtp/send + verify | SMSOTPInput (+ resend) |
| Proof token | JWT 60s, single-use, txHash-bound | Gửi X-2FA-Token header |
| Confirm order | require2FA middleware | Update saga flow |

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

```
User chọn items → tap "Confirm"
    │
    ├─ (1) Check transaction value
    │   ├─ < 10tr → No 2FA needed (access token sufficient)
    │   ├─ 10tr ~ 100tr → Yêu cầu Biometric L2
    │   └─ ≥ 100tr → Yêu cầu Smart-OTP TOTP
    │
    ├─ (2) Check 2FA device availability
    │   ├─ Biometric available? → prompt biometric (Face ID / Touch ID)
    │   ├─ Smart-OTP available? → prompt OTP input
    │   └─ Neither? → fallback to SMS OTP
    │
    ├─ (3) User cung cấp 2FA credential
    │   ├─ /api/v1/smartOtp/verify (TOTP)
    │   ├─ /api/v1/biometric/verify (Biometric L2)
    │   └─ /api/v1/orderOtp/send + /orderOtp/verify (SMS OTP)
    │
    └─ (4) On success → gọi POST confirm order API kèm 2FA proof token
        ├─ 2FA token (ngắn hạn, 1-time, bound to transaction)
        └─ Order được gửi đến Core
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

### 3.1 Initiate 2FA Challenge

```
POST /api/v1/transactions/2fa/challenge
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "transactionType": "ORDER_CONFIRM",
  "transactionData": {
    "orders": [
      {
        "orderDate": "2026-06-13",
        "orderNumber": "123456"
      }
    ],
    "totalValue": 150000000
  },
  "preferredMethod": "SMARTOTP"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `transactionType` | enum | `ORDER_CONFIRM`, `CASH_TRANSFER`, `STOCK_TRANSFER`, `PROFILE_CHANGE` |
| `transactionData` | object | Dữ liệu transaction (transactionHash được tính từ data này) |
| `preferredMethod` | enum | `SMARTOTP`, `BIOMETRIC`, `SMS_OTP` |

**Response:**
```json
{
  "challengeId": "ch_abc123",
  "transactionHash": "sha256hash...",
  "requiredMethod": "SMARTOTP",
  "expiresAt": "2026-06-13T10:30:00+07:00",
  "methods": [
    {
      "type": "SMARTOTP",
      "available": true,
      "hint": "Nhập mã OTP từ ứng dụng Smart-OTP"
    },
    {
      "type": "BIOMETRIC",
      "available": false,
      "reason": "Chưa đăng ký biometric"
    },
    {
      "type": "SMS_OTP",
      "available": true,
      "hint": "Mã OTP sẽ được gửi đến SĐT 09*****678",
      "phoneMask": "09*****678"
    }
  ]
}
```

**Success Codes:**
- `201 CREATED` — challenge created

**Error Codes:**
- `400 INVALID_TRANSACTION_TYPE`
- `400 UNSUPPORTED_METHOD`
- `403 MAX_SESSIONS_REACHED` (nếu user chưa set thiết bị 2FA)
- `422 TRANSACTION_VALUE_EXCEEDS_LIMIT` (nếu vượt hạn mức, cần tăng cường 2FA)

**Business Rules:**
- `transactionHash` = SHA256(`transactionData` + `userId` + `challengeId`) — server-side, không trust client hash
- Challenge TTL: 5 phút (có thể config)
- Mỗi user chỉ có 1 challenge active tại 1 thời điểm
- Nếu user có nhiều phương thức 2FA, hệ thống chọn method mạnh nhất hoặc theo preference

---

### 3.2 Verify 2FA Challenge

```
POST /api/v1/transactions/2fa/verify
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "challengeId": "ch_abc123",
  "method": "SMARTOTP",
  "code": "482916",
  "transactionHash": "sha256hash..."
}
```

| Field | Type | Description |
|-------|------|-------------|
| `challengeId` | string | ID từ response khởi tạo challenge |
| `method` | enum | Phương thức xác thực |
| `code` | string | OTP code (cho SMARTOTP/SMS_OTP) hoặc `biometric_proof` (cho BIOMETRIC) |
| `transactionHash` | string | Hash để verify data integrity (optional — server có thể tính lại) |

**Response:**
```json
{
  "status": "verified",
  "proofToken": "eyJhbGciOiJSUzI1NiJ9...",
  "proofTokenExpiresAt": "2026-06-13T10:30:15+07:00"
}
```

**Success Codes:**
- `200 OK` — verified, trả về proofToken

**Error Codes:**
- `400 CHALLENGE_EXPIRED` — challenge đã hết hạn
- `400 INVALID_CODE` — OTP sai
- `400 CHALLENGE_NOT_FOUND`
- `429 TOO_MANY_ATTEMPTS` — vượt quá số lần thử (lockout)
- `422 MAX_FAILED_ATTEMPTS` — đã đạt giới hạn fail, tài khoản bị khóa 30 phút

**Business Rules:**
- Smart-OTP: verify qua `/api/v1/smartOtp/verify` (Core Lotte)
- Biometric: verify qua `/api/v1/biometric/verify` (AAA service)
- SMS OTP: verify qua `/api/v1/orderOtp/verify` (AAA service)
- Thử sai 5 lần → lock 30 phút (configurable)
- Proof token TTL: 60 giây, single-use

---

### 3.3 Confirm Order with 2FA Proof

```
POST /api/v1/order/confirm
Authorization: Bearer <access_token>
X-2FA-Token: <proofToken>
Content-Type: application/json
```

**Request Body:** (existing structure)
```json
{
  "accountNumber": "123456",
  "subNumber": "001",
  "orders": [
    {
      "orderDate": "2026-06-13",
      "orderNumber": "123456"
    }
  ]
}
```

**Response:**
```json
{
  "status": "confirmed",
  "orderCount": 2,
  "totalValue": 150000000,
  "confirmedOrders": [
    {
      "orderNumber": "123456",
      "status": "SUCCESS",
      "message": "Lệnh đã được gửi đến Sở giao dịch"
    }
  ]
}
```

**Success Codes:**
- `200 OK` — confirmed

**Error Codes:**
- `401 INVALID_2FA_TOKEN` — proof token không hợp lệ
- `401 2FA_TOKEN_EXPIRED` — proof token đã hết hạn
- `401 2FA_TRANSACTION_MISMATCH` — proof token không match với transaction này
- `422 ORDER_PLACE_XXXX` — lỗi từ Core (pass-through)

**Backend Logic:**
```javascript
// Middleware kiểm tra 2FA
function require2FA(req, res, next) {
  const proofToken = req.headers['x-2fa-token'];
  if (!proofToken) return res.status(401).json({ code: '2FA_REQUIRED' });

  try {
    const decoded = jwt.verify(proofToken, conf.getJwt().publicKey);
    if (decoded.sub !== '2fa_proof') throw new Error('invalid token type');

    // Kiểm tra transaction match
    const txHash = sha256(req.body + decoded.userId + decoded.challengeId);
    if (txHash !== decoded.transactionHash) {
      throw new Error('transaction mismatch');
    }

    // 1-time use
    revokeProofToken(decoded.jti);

    req.twoFA = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ code: 'INVALID_2FA_TOKEN' });
  }
}
```

---

### 3.4 Update Order 2FA for Other Transaction Types

Tương tự flow trên cho:

**Cash Transfer Confirm:**
```
POST /api/v1/cash-transfer/confirm
X-2FA-Token: <proofToken>
```

**Stock Transfer Confirm:**
```
POST /api/v1/stock-transfer/confirm
X-2FA-Token: <proofToken>
```

**Profile Change:**
```
POST /api/v1/user/profile
X-2FA-Token: <proofToken>
```

> Các endpoint này đã tồn tại, chỉ cần thêm middleware 2FA check.

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

| Question | Proposed Decision |
|----------|-------------------|
| Threshold 10tr/100tr có đúng không? | Cần confirm với compliance |
| SmartOTP device binding: 1 device/user? | Hiện tại 1 device, future multi-device |
| User chưa enroll Smart-OTP: force enroll hay SMS fallback? | SMS fallback (Phase 1), force enroll (Phase 2) |
| Biometric L2 có verify qua Lotte Core không? | Không — verify local trên AAA (RSA) |
| Admin transactions có cần 2FA không? | Có — nhưng admin có thể bypass trên màn hình confirm (configurable) |

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
