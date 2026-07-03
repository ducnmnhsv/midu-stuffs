# Order 2FA — Integration Specification

**Feature:** Xác thực 2 lớp — Login & Giao dịch
**TT134 Reference:** Điều 7 (Xác thực), Điều 8 (Phương thức xác thực & TTL), Điều 10 (Xử lý Giao dịch), Điều 11 (Rủi ro)
**Priority:** P0
**Version:** 1.3
**Status:** Draft — updated 2026-06-30

---

## 0. Flow Overview

> **Architecture note:** Core (Lotte) đã có sẵn khả năng verify Smart OTP cho đặt lệnh. NHSV Pro app đã handle TOTP code generation (SmartOTPInput từ Login S-OTP). Với Smart OTP, TradeX chỉ cần proxy OTP code kèm order data sang Core — Core verify và process order atomically, không cần challenge/proof-token riêng. Proof token chỉ cần thiết cho Biometric path (verify qua kbfinance/verifyPassword với `pinType: "BIOMETRIC"`, tách biệt với Core).

### 0.1 Bức tranh toàn cảnh — model xác thực theo phiên

> **Quyết định thiết kế v1.3:** Login Smart OTP = Session Auth. Không có màn hình Session Auth riêng. Hard gate: bắt buộc kích hoạt Smart OTP trước khi giao dịch.

```
┌─────────────────────────────────────────────────────────────────────┐
│  STAGE 1+2 (MERGED): LOGIN WITH SMART OTP                          │
│                                                                     │
│  Password + Smart OTP (Case A) → verifyOTP → JWT embed sAm+sAt    │
│                                  → Vào app, giao dịch tự do        │
│                                                                     │
│  Password + SMS OTP (chưa kích hoạt Smart OTP)                     │
│  → JWT không có sAm → Hard gate: bắt buộc kích hoạt Smart OTP     │
│  → Sau kích hoạt → re-login với Smart OTP → sAm embedded → app    │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│  STAGE 3: PER-TRANSACTION — chỉ áp dụng cho rút tiền ≥ 10M        │
│                                                                     │
│  Rút tiền ≥ 10M  →  Biometric server-side (Điều 12k4a)            │
│                      verify qua kbfinance/verifyPassword            │
│                      KHÔNG cho phép fallback Smart OTP              │
└─────────────────────────────────────────────────────────────────────┘
```

### 0.2 Method priority

| Method | Điều kiện | Áp dụng cho |
|---|---|---|
| **Smart OTP (TOTP)** | Bắt buộc — đã kích hoạt | Login verifyOTP → sAm/sAt embedded → GDCK + rút tiền < 10M |
| **SMS OTP** | Chỉ cho login khi Smart OTP chưa kích hoạt | Không cover GDCK — user phải kích hoạt Smart OTP trước |
| **Biometric server-side** | Enrolled (`t_biometric`) | Rút tiền ≥ 10M, bắt buộc, không fallback |

> Smart OTP là **bắt buộc** cho session auth. Không có SMS OTP fallback cho GDCK. User chưa kích hoạt Smart OTP → hard gate, phải kích hoạt trước khi giao dịch.

### 0.3 Architecture — path theo method

| Path | Trigger | Verify | Ghi log |
|---|---|---|---|
| **Smart OTP** | Login verifyOTP với `otpType=SMART_OTP` | Core verify (lotte-bridge `verifySmartOtp`) | `sAm=SOFT_OTP`, `sAt=login_time` — embedded trong JWT |
| **Biometric server-side** | Rút tiền ≥ 10M (per-transaction) | `kbfinance/verifyPassword` với `pinType=BIOMETRIC` → proof token | `auth_method=BIOMETRIC`, `auth_time=biometric_verify_time` (riêng từng giao dịch) |

---

## 0a. Login Flow — sAm/sAt embedded tại verifyOTP

> **Không có màn hình Session Auth riêng.** Login với Smart OTP = session auth. `sAm`/`sAt` được embed ngay trong JWT tại bước verifyOTP.

```
Case A — Smart OTP user (sotpStatus=Y, local key có sẵn):

[Nhập user/pass] → POST /login → sotpStatus=Y + otpType=SMART_OTP
       → [Smart OTP input — 6 chữ số]
       → POST /login/sec/verifyOTP { otpType: "SMART_OTP" }
       → BE embed sAm=SOFT_OTP, sAt=<login_time> trong JWT
       → JWT final → Vào app, giao dịch tự do toàn phiên ✓

Case C — Smart OTP user, cài lại app (sotpStatus=Y, local key mất):

[Nhập user/pass] → POST /login → sotpStatus=Y, nhưng không có local key
       → [Special screen: kích hoạt lại / SMS fallback]
       → PRIMARY: kích hoạt lại Smart OTP (otp/send → otp/verify → smartOtp/register)
           → sotpKey mới lưu vào Keychain
           → [Smart OTP input] → verifyOTP với Smart OTP → sAm embedded → app ✓
       → SECONDARY: SMS fallback (tạm thời, để login)
           → verifyOTP SMS → JWT không có sAm
           → Hard gate: phải kích hoạt Smart OTP → sau đó re-login ✓

Case B — Chưa có Smart OTP (sotpStatus=N):

[Nhập user/pass] → POST /login → sotpStatus=N + otpType=SMS_OTP
       → [SMS OTP input — 4 chữ số]
       → POST /login/sec/verifyOTP (SMS)
       → JWT final không có sAm
       → Hard gate: "Kích hoạt Smart OTP để giao dịch"
           → Activation flow (SMS OTP → PIN → register → sotpKey saved)
           → Re-login → Case A → sAm embedded → app ✓
```

**Lưu ý:**
- `sAm`/`sAt` chỉ được embed khi `otpType=SMART_OTP` trong verifyOTP — không embed cho SMS OTP login
- `sAt` = thời điểm login, hợp lệ toàn phiên (session timeout hoặc logout mới reset)
- Hard gate: FE kiểm tra JWT sau login — nếu không có `sAm` → redirect mandatory Smart OTP activation, không vào home

## 0b. UX Principles

| Principle | Mô tả |
|-----------|-------|
| **Smart OTP bắt buộc** | Tất cả user phải có Smart OTP để giao dịch. Không có SMS OTP fallback cho GDCK. |
| **Zero extra step** | User đã có Smart OTP: login → giao dịch ngay. Không màn hình trung gian. |
| **Hard gate activation** | User chưa có Smart OTP: mandatory activation trước khi vào home. Clear CTA, không cho skip. |
| **Minh bạch** | Hard gate hiển thị lý do rõ ràng: "Smart OTP cần thiết để đặt lệnh theo quy định TT134" |
| **Reuse existing** | SmartOTPInput và TOTP generation đã có từ Login S-OTP flow — reuse, không build mới |

## 0c. BE / FE Responsibility Split

| Component | Backend | Frontend (App) |
|-----------|---------|----------------|
| **Device detect** | Check deviceId vs last_device_id → trigger login 2FA nếu mới | Gửi deviceId trong header mỗi request |
| **Smart OTP generate** | N/A — không cần | ✅ **Đã có**: TOTP generation trong SmartOTPInput (Login S-OTP) — reuse |
| **Smart OTP verify** | lotte-bridge proxy sang Core — Core verify + process order atomic | SmartOTPInput (6-digit) |
| **Biometric L2 verify** | AAA: RSA signature verify locally → trả proof token | BiometricPromptHandler |
| **SMS OTP** | POST /otp/send (`purpose: ORDER_PLACEMENT`) + Core verify | SMSOTPInput (+ countdown + resend) |
| **Proof token** | Chỉ cần cho Biometric path: JWT 60s, single-use, txHash-bound | Gửi X-2FA-Token header (Biometric path only) |
| **session_device_authed flag** | Set true sau khi Smart OTP verify thành công ở login | Không cần hỏi Smart OTP đầu phiên nếu flag = true |
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

| Transaction Type | Auth method | Fallback | Auth log |
|---|---|---|---|
| Login — thiết bị mới / lần đầu | Smart OTP | SMS OTP | Ghi vào session |
| **Đầu phiên (sau mọi login)** | **Smart OTP — bắt buộc** | **SMS OTP** | `session_auth_method`, `session_auth_time` |
| **GDCK — mọi giá trị** | **Session auth (đã xác thực đầu phiên)** | — | Reuse `session_auth_method/time` |
| Cash transfer (chuyển tiền) | Session auth | — | Reuse `session_auth_method/time` |
| Stock transfer (chuyển CK) | Session auth | — | Reuse `session_auth_method/time` |
| **Rút tiền < 10M** | **Session auth** | — | Reuse `session_auth_method/time` |
| **Rút tiền ≥ 10M** | **Biometric server-side (Điều 12k4a)** | **Không cho fallback** | `auth_method=BIOMETRIC`, `auth_time=thời điểm biometric verify` |
| Profile change (đổi SĐT/email) | SMS OTP + Smart OTP | — | — |
| Smart OTP enrollment (BE-7) | SMS OTP + Biometric device | — | — |
| Password change | SMS OTP | — | — |

> **Điểm thay đổi so với v1.0:** Không còn tier theo giá trị lệnh GDCK (< 10tr / 10tr–100tr / ≥ 100tr). Thay bằng model session-auth — xác thực 1 lần đầu phiên, cover toàn bộ GDCK. Rút tiền ≥ 10M vẫn yêu cầu biometric per-transaction theo Điều 12k4a.

---

## 2. Proposed Architecture

### 2.1 2FA Verification Flow

**Path A — Smart OTP Login → sAm/sAt embedded (GDCK + rút tiền < 10M)**

```
[Login với Smart OTP]
    │
    ├─ POST /rest/api/v1/login → sotpStatus=Y + otpType=SMART_OTP
    │
    ├─ User nhập TOTP 6-digit → POST /rest/api/v1/login/sec/verifyOTP
    │       { otpValue: "123456", otpType: "SMART_OTP" }
    │       → BE embed sAm=SOFT_OTP, sAt=<login_time> trong JWT final
    │
    └─ Từ đây: mọi GDCK và rút tiền < 10M → FE gọi thẳng confirm API
               không cần OTP thêm, BE middleware tự lấy sAm/sAt từ JWT để log
```

```
[GDCK — đặt lệnh bất kỳ giá trị]
    │
    └─ FE: POST /rest/api/v1/lotte/equity/order (hoặc derivatives/order)
           { accountNumber, orders: [...] }
           (không có OTP field — session đã auth qua login)
               │
               └─ lotte-bridge → Core (process order)
                   BE middleware log: auth_method=sAm, auth_time=sAt từ JWT
```

**Path B — Hard gate Smart OTP activation (user chưa kích hoạt)**

```
[Login với SMS OTP — sotpStatus=N]
    │
    ├─ POST /rest/api/v1/login/sec/verifyOTP (SMS) → JWT không có sAm
    │
    ├─ FE: JWT parse → sAm không tồn tại → Hard gate (không vào home)
    │       Màn hình: "Kích hoạt Smart OTP để giao dịch"
    │       Lý do rõ: "Theo quy định TT134, Smart OTP là bắt buộc"
    │
    ├─ User kích hoạt Smart OTP (otp/send → otp/verify → smartOtp/register)
    │       → sotpKey mới lưu vào Keychain
    │
    └─ "Đăng nhập lại với Smart OTP để bắt đầu giao dịch"
           → Re-login → Path A → sAm embedded → vào app ✓
```

**Path C — Biometric server-side (rút tiền ≥ 10M, per-transaction)**

```
User tap "Rút tiền" với amount ≥ 10M
    │
    ├─ FE check biometric enrollment status
    │   └─ Chưa enroll → BLOCK, hiển thị yêu cầu đăng ký biometric
    │      (không cho fallback Smart OTP — Điều 12k4a bắt buộc)
    │
    ├─ FE: POST /api/v1/transactions/2fa/challenge
    │       { transactionType: "WITHDRAWAL", transactionData: { amount, destAccount } }
    │       → nhận challengeId + transactionHash
    │
    ├─ FE: BiometricPromptHandler.sign(challengeId + transactionHash)
    │   └─ FAIL → thông báo lỗi, không cho tiếp tục
    │
    ├─ FE: POST /api/v1/transactions/2fa/verify
    │       { challengeId, method: "BIOMETRIC", signature }
    │       → BE: verify RSA signature → gọi kbfinance/verifyPassword (pinType=BIOMETRIC)
    │       → nhận proofToken (JWT 60s, 1-time)
    │
    └─ FE: POST /api/v1/cash/withdraw/confirm kèm X-2FA-Token: proofToken
           BE log: auth_method=BIOMETRIC, auth_time=thời điểm biometric verify
```

### 2.2 Session Auth Token — IAccessToken bổ sung

BE cần thêm 2 field vào `IAccessToken` (hiện có: `uId`, `cId`, `lm`, `rId`, `sc`, `rls`, `step`, `pl`, `gt`, `osV`, `appV`, `madr`):

```typescript
interface IAccessToken {
  // ... existing fields (giữ nguyên)
  sAm?: string;    // sessionAuthMethod: "SOFT_OTP" (chỉ giá trị này — SMS OTP không được embed)
  sAt?: number;    // sessionAuthTime: Unix timestamp (giây) — thời điểm login
}
```

`sAm`/`sAt` được embed **tại bước `generateToken()` trong verifyOTP** khi `otpType=SMART_OTP`. Không có endpoint session/auth riêng. Middleware của order và withdraw/confirm đọc 2 field này từ JWT để ghi vào `t_order_log.auth_method` / `auth_time` — không cần FE gửi thêm.

`sAm`/`sAt` carry qua refresh token trong cùng ngày giao dịch. Reset khi: logout, session timeout, hoặc refresh token expire.

### 2.3 2FA Proof Token — Biometric path (rút tiền ≥ 10M)

Proof token chỉ dùng cho biometric path. Tránh replay attack bằng cách bind với transactionHash:

```json
{
  "sub": "2fa_proof",
  "userId": 12345,
  "transactionType": "WITHDRAWAL",
  "transactionHash": "sha256(...withdrawalData...)",
  "method": "BIOMETRIC",
  "iat": 1718200000,
  "exp": 1718200060
}
```

**Flow:**
1. FE gọi challenge API → nhận `challengeId` + `transactionHash`
2. FE trigger biometric prompt → ký `challengeId + transactionHash` bằng RSA private key
3. FE gọi verify API với signature → BE verify RSA + call `kbfinance/verifyPassword (pinType=BIOMETRIC)` → trả proof token
4. FE gọi `POST /cash/withdraw/confirm` kèm `X-2FA-Token`
5. Proof token dùng 1 lần, TTL 60s, revoke ngay sau khi dùng

---

## 3. API Endpoints

> **v1.3:** Không có `POST /api/v1/session/auth` endpoint. sAm/sAt được embed trong verifyOTP (login flow). Smart OTP Login (07_BE_Task) là prerequisite trực tiếp.

### 3.0 verifyOTP — điểm embed sAm/sAt (thay đổi existing endpoint)

```
POST /rest/api/v1/login/sec/verifyOTP
Authorization: Bearer <temp_access_token>
Content-Type: application/json
```

**Request Body (Smart OTP — sẽ embed sAm/sAt):**
```json
{
  "otpValue": "482916",
  "otpType": "SMART_OTP"
}
```

**JWT final (khi otpType=SMART_OTP):**
```typescript
{
  // ... existing fields
  sAm: "SOFT_OTP",    // embedded tại generateToken()
  sAt: 1751260800     // Unix timestamp của login time
}
```

> Không embed sAm/sAt khi otpType = SMS_OTP. FE parse JWT sau login — thiếu sAm → hard gate activation.

---

### 3.1 Confirm Order — Session Auth (mọi giá trị GDCK)

> Không cần OTP trong body — session đã auth. BE middleware tự lấy `sAm`/`sAt` từ JWT để log.

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
  "purpose": "ORDER_PLACEMENT"
}
```

| Field | Type | Mô tả |
|---|---|---|
| `purpose` | enum | `ORDER_PLACEMENT` — bắt buộc để Core log đúng nghiệp vụ |

> `auth_method` và `auth_time` trong `t_order_log` được lấy tự động từ `sAm`/`sAt` trong JWT.

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

- User chưa kích hoạt Smart OTP
- Smart OTP bị locked (10 lần sai)
- Biometric L2 thất bại sau 3 lần (giao dịch 10tr–100tr)
- User chọn "Gửi OTP qua SMS" từ màn hình 2FA
- Lần đầu login, chưa có Smart OTP

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
- SMS OTP TTL: **300 giây (5 phút)** — theo Điều 8 TT134 (≤ 5 phút)
- Resend cooldown: 60 giây
- Max 5 OTP requests/giờ
- Phone number masked trong response (chỉ hiện 4 số cuối)
- OTP gửi đến SĐT đã đăng ký (từ user profile), không cho nhập SĐT khác
- SMS content bắt buộc kèm mục đích: `"[NHSV] Mã xác thực ĐẶT LỆNH: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai."`

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
| `TwoFAChallengeModal` | Modal hiển thị phương thức 2FA đang dùng + nút "Dùng phương thức khác" |
| `SmartOTPInput` | 6-digit OTP input (TOTP) — reuse từ Login S-OTP |
| `SMSOTPInput` | OTP input + "Gửi lại" button + countdown 5 phút |
| `BiometricPromptHandler` | Wrapper cho biometric prompt (Face ID / Touch ID) |
| `TwoFAMethodSwitcher` | Nút chuyển sang SMS OTP: "Gửi OTP qua SMS" |

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
| SmartOTP device binding: 1 device/user? | Hiện tại 1 device (`t_sotp_device`), future multi-device |
| User chưa enroll Smart OTP: force enroll hay fallback? | ✅ **Resolved v1.3:** Hard gate — bắt buộc kích hoạt trước khi giao dịch. Không có SMS OTP fallback cho GDCK. |
| Biometric verify có qua Lotte Core không? | **Không** — verify qua `kbfinance/verifyPassword (pinType=BIOMETRIC)` |
| Smart OTP verify qua Core hay AAA? | **Qua Core** — lotte-bridge `verifySmartOtp` → Core. Embedded tại verifyOTP. |
| SmartOTPInput component có sẵn chưa? | **Có** — NHSV Pro app đã có từ Login S-OTP flow, reuse |
| `sAm`/`sAt` lưu trong JWT hay Redis? | ✅ **Resolved v1.3:** JWT — đơn giản, không cần Redis lookup per-request |
| Rút tiền ≥ 10M: nếu user chưa enroll biometric? | Block giao dịch, yêu cầu enroll — không cho fallback Smart OTP (Điều 12k4a) |
| `sAm`/`sAt` có carry qua refresh token không? | ✅ **Resolved v1.3:** Có — carry trong cùng ngày giao dịch. Reset khi logout/session expire. |
| TT134 Điều 18 k4: auth_time = session auth time hay per-transaction? | ✅ **Resolved v1.3:** auth_time = thời điểm login (session auth time). Hợp lệ toàn phiên. |
| Admin transactions có cần session auth không? | Có — áp dụng cùng model |

---

## 12. References

- TT134/2024/TT-BTC Điều 7 — Xác thực
- TT134/2024/TT-BTC Điều 10 — Xử lý giao dịch & xác nhận
- TT134/2024/TT-BTC Điều 11 — Quản lý rủi ro
- TT134/2024/TT-BTC Điều 13 — Giám sát & báo cáo
- [Smart-OTP API Mapping](../../../Smart-OTP/Specifications/SmartOTP_API_Mapping.md)
- [Biometric System draft plan](../../Biometric%20System/Specifications/Biometric_System_Spec.html)
- [ConfirmOrdersScreen FE](../../../nhsv-mts-rn/src/screens/ConfirmOrdersScreen/)

---

## Status

- **Spec:** Draft v1.0
- **Phase 1:** Pending (blocked by Smart-OTP activation completion)
- **Next:** Backend implementation (Phase 1) after spec review

---

*Last updated: 2026-06-30 — v1.3: login Smart OTP = session auth (no separate screen/endpoint), hard gate activation, all open questions resolved*

---

Document Status: 📋 | For: PM/Dev | Next Steps: Review nội dung, cập nhật status trên Tracking/tasks.js
