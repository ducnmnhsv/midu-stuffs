# [TT134-P0-03] Rút tiền < 10M — Bắt buộc Smart OTP

**TT134 Reference:** Điều 12 khoản 4a — Xác thực giao dịch rút, chuyển tiền < 10M  
**Priority:** 🔴 P0  
**Deadline:** 28/08/2026  
**Sub-group:** Order 2FA  
**Phụ thuộc C06:** Không — dùng Smart OTP (lotte-bridge)  
**Phụ thuộc nội bộ:** Smart OTP project (3rd party integration)  

---

## 1. Bối cảnh

**Điều 12 khoản 4 TT134** quy định:
- Rút tiền < 10M: áp dụng tối thiểu **một trong các hình thức xác thực Điều 7** (SMS OTP, Soft OTP/Smart OTP, Voice OTP, FIDO, v.v.)
- Rút tiền ≥ 10M: áp dụng **Điều 9** (sinh trắc học khuôn mặt — blocked by C06 gate)

Hiện tại, flow rút tiền < 10M **chưa yêu cầu bất kỳ xác thực thứ hai nào** — chỉ dùng access token. Đây là gap compliance cần fix trước 28/08/2026.

**Lựa chọn implementation:** Smart OTP (Soft OTP) là phương án chính. SMS OTP hợp lệ theo TT134 và là fallback khi Smart OTP chưa được activate.

> **Lưu ý 1:** Rút tiền ≥ 10M yêu cầu Điều 9 (sinh trắc học) — bị block bởi quyết định C06. Issue này chỉ scope **< 10M**.
> **Lưu ý 2:** Scope bao gồm cả **chuyển tiền** ra khỏi TK GDCK (Điều 12 k4: "rút, **chuyển tiền**").

---

## 2. Current vs Target State

| Flow | Hiện tại | Target (TT134) | Scope issue này |
|---|---|---|---|
| Rút tiền < 10M | ❌ Chỉ access token | ✅ Bất kỳ Điều 7 method (Smart OTP / SMS OTP) | ✅ IN SCOPE |
| Chuyển tiền < 10M | ❌ Chỉ access token | ✅ Bất kỳ Điều 7 method | ✅ IN SCOPE |
| Rút/chuyển tiền ≥ 10M | ❌ Chỉ access token | Điều 9 (biometric) | 🔒 Blocked GATE |
| Đặt lệnh GDCK | ❌ Chưa có Điều 9 | Điều 9 (biometric) đầu phiên + same device | 🔒 Blocked GATE (STT 12) |

---

## 3. Flow Target

```
User nhập số tiền rút (< 10M)
        │
        ▼
FE: POST /api/v1/cash/withdraw/initiate
        │ response: withdrawId, requireOtp: true
        ▼
FE: Hiển thị màn hình nhập Smart OTP (6 digits TOTP)
        │ user nhập code
        ▼
FE: POST /api/v1/cash/withdraw/confirm
        │ body: { withdrawId, smartOtpCode }
        ▼
BE: Verify Smart OTP với Lotte Core
        │
        ├─ OTP valid → process withdrawal → success
        └─ OTP invalid/expired → return error → user retry (max 5 lần)
```

---

## 4. Scope of Work

### 4.1 BE Tasks

| # | Task | Service | Ưu tiên |
|---|---|---|---|
| BE-1 | Update `/api/v1/cash/withdraw/confirm`: enforce Smart OTP verify | cash-service / lotte-bridge | Cao |
| BE-2 | Tạo/update `requireSmartOtp` flag cho withdrawal transaction dưới ngưỡng | cash-service | Cao |
| BE-3 | Proxy Smart OTP verify sang Lotte Core (nếu chưa có cho withdrawal) | lotte-bridge | Cao |
| BE-4 | Return `requireOtp: true`, `otpType: "SMART_OTP"` trong initiate response | cash-service | Cao |
| BE-5 | **[Điều 8.5d]** Lockout: max N lần sai (N ≤ 10, NHSV định) → lock Smart OTP vĩnh viễn. Sau lock → response `SMART_OTP_LOCKED`, FE fallback sang SMS OTP | cash-service + Smart OTP service | Cao |
| BE-6 | Audit log: ghi `smartOtpVerifiedAt`, `verificationMethod: SMART_OTP \| SMS_OTP` | audit service | Trung bình |
| BE-7 | **[Core request]** Yêu cầu Core thêm field `purpose` vào Smart OTP verify API. Core cần nhận và validate đúng purpose để phân biệt nghiệp vụ xác thực | lotte-bridge → Core | **🔴 Critical** |
| BE-8 | **[Core request]** Xác nhận mapping `purpose` enum giữa TradeX và Core. TradeX gửi: `LOGIN`, `CASH_WITHDRAWAL`, `ORDER_PLACEMENT` — Core có thể dùng tên khác | lotte-bridge | Cao |
| BE-9 | Khi initiate withdrawal: query Smart OTP status của user → trả về `otpType` phù hợp (`SMART_OTP` hoặc `SMS_OTP`) ngay trong initiate response, không để FE tự detect sau khi verify fail | cash-service | Cao |
| BE-10 | SMS OTP fallback: khi `otpType: SMS_OTP` → trigger `POST /api/v1/otp/send` với `purpose: CASH_WITHDRAWAL` và `transactionRef: withdrawId`. OTP phải bind với `withdrawId` — không dùng generic OTP session | cash-service / SMS service | Cao |

**`purpose` values cho Smart OTP verify (TT134 scope)**

| Value | Nghiệp vụ | Điều TT134 |
|---|---|---|
| `LOGIN` | Đăng nhập lần đầu / thiết bị mới | Điều 7 |
| `CASH_WITHDRAWAL` | Rút tiền < 10M | Điều 12 k4a |
| `ORDER_PLACEMENT` | Đặt lệnh ≥ 100M | Điều 10 |

> Các nghiệp vụ khác (thay đổi người chăm sóc, chuyển tiền nội bộ) **không nằm trong scope Smart OTP** theo yêu cầu TT134.

### 4.2 FE Tasks

| # | Task | Screen | Ưu tiên |
|---|---|---|---|
| FE-1 | Sau bước nhập số tiền rút → navigate to OTP screen; loại OTP screen được quyết định bởi `otpType` từ initiate response | WithdrawalScreen | Cao |
| FE-2 | Reuse `SmartOTPInput` component (đã có từ Login S-OTP) khi `otpType: SMART_OTP` | SmartOTPInput | Cao |
| FE-3 | Handle `OTP_INVALID`, `OTP_EXPIRED`, `MAX_RETRY_EXCEEDED` errors | WithdrawalOTPScreen | Cao |
| FE-4 | Hiển thị challenge session countdown 2 phút (TTL per Điều 8.5đ); code bên trong tự refresh mỗi 30s theo TOTP | WithdrawalOTPScreen | Trung bình |
| FE-5 | Khi `otpType: SMS_OTP` (từ initiate response) → render SMS OTP screen trực tiếp, không qua Smart OTP screen. Hiển thị banner nhẹ giải thích lý do | WithdrawalOTPScreen | Cao |
| FE-6 | Khi Smart OTP bị lock giữa chừng (`SMART_OTP_LOCKED` từ verify response) → switch sang SMS OTP screen + warning banner | WithdrawalOTPScreen | Cao |

---

## 5. Acceptance Criteria

```
-- Xác thực (Điều 12 k4a) --
AC-1: Rút tiền < 10M không thể hoàn thành nếu không có Điều 7 OTP hợp lệ (Smart OTP hoặc SMS OTP fallback)
AC-2: Chuyển tiền ra khỏi TK GDCK < 10M cũng áp dụng cùng yêu cầu xác thực

-- Lockout Soft OTP (Điều 8.5d) --
AC-3: Nhập sai Smart OTP liên tiếp quá N lần (N ≤ 10, NHSV định) → Smart OTP bị khóa vĩnh viễn
AC-4: Sau lock Smart OTP → tự động fallback sang SMS OTP để hoàn thành giao dịch
AC-5: Smart OTP chỉ được mở khóa khi KH yêu cầu và CSH đã verify danh tính — không auto-unlock

-- TTL (Điều 8.5đ) --
AC-6: Smart OTP code có hiệu lực ≤ 2 phút kể từ lúc tạo; nhập sau 2 phút → OTP_EXPIRED
AC-7: Fallback SMS OTP có hiệu lực ≤ 5 phút kể từ lúc gửi (Điều 8.1b)

-- Enrollment (Điều 8.5b) --
AC-8: User chưa activate Smart OTP → fallback sang SMS OTP (không chặn giao dịch)
AC-9: User activate Smart OTP trên thiết bị mới → phải qua SMS OTP + biometric trước

-- Audit Log (Điều 18 k5) --
AC-10: Audit log ghi đủ: withdrawalId/transferId, userId, amount, destAccount, authMethod, authTime, deviceId, sourceIp
```

---

## 6. API Changes

### `/api/v1/cash/withdraw/initiate` — Response update

```json
{
  "success": true,
  "data": {
    "withdrawId": "WD_20260801_001",
    "amount": 5000000,
    "requireOtp": true,
    "otpType": "SMART_OTP",
    "fallbackOtpType": "SMS_OTP"
  }
}
```

### `/api/v1/cash/withdraw/confirm` — Request update

```json
{
  "withdrawId": "WD_20260801_001",
  "smartOtpCode": "123456"
}
```

### Error codes mới

| Code | HTTP | Mô tả |
|---|---|---|
| `OTP_REQUIRED` | 400 | Giao dịch yêu cầu OTP, chưa cung cấp |
| `OTP_INVALID` | 400 | OTP không đúng |
| `OTP_EXPIRED` | 400 | OTP đã quá 2 phút (Smart OTP) hoặc 5 phút (SMS OTP) |
| `SMART_OTP_NOT_ACTIVATED` | 200 | User chưa kích hoạt Smart OTP → FE tự động fallback SMS OTP |
| `SMART_OTP_LOCKED` | 200 | Smart OTP đã bị khóa → FE fallback SMS OTP |

---

## 7. UX Considerations

| Scenario | UX Behavior |
|---|---|
| Smart OTP chưa activate | Toast: "Vui lòng kích hoạt Smart OTP để rút tiền" → button "Kích hoạt ngay" |
| OTP sai, còn lần thử | Shake animation, hiển thị "Mã OTP không đúng. Còn X lần thử." |
| OTP sai, còn 1 lần | Warning: "Bạn còn 1 lần thử. Nhập sai sẽ khóa Smart OTP vĩnh viễn." |
| Smart OTP locked | Màn hình: "Smart OTP đã bị khóa. Vui lòng liên hệ NHSV hỗ trợ để mở khóa." → **không có countdown** → button "Dùng SMS OTP thay thế" |
| OTP expired (> 2 phút) | "Mã OTP đã hết hạn, vui lòng nhập mã mới" |

---

## 8. SMS OTP Fallback — Phân tích hệ thống

### 8.1 Ba trường hợp kích hoạt fallback

| Case | Trigger | Thời điểm phát hiện | Hành động hệ thống |
|---|---|---|---|
| **Case 1** — Smart OTP chưa activate | Initiate withdrawal: BE query Smart OTP status → `NOT_ACTIVATED` | Ngay từ đầu, trước khi show OTP screen | Initiate trả về `otpType: SMS_OTP`, reason: `SMART_OTP_NOT_ACTIVATED` |
| **Case 2** — Smart OTP bị lock (trước đó) | Initiate: BE query → status `LOCKED` | Ngay từ đầu | Initiate trả về `otpType: SMS_OTP`, reason: `SMART_OTP_LOCKED` |
| **Case 3** — Smart OTP lock ngay trong lúc rút | Verify Smart OTP → sai N lần → lock kích hoạt | Giữa chừng, user đang ở Smart OTP screen | Verify response `SMART_OTP_LOCKED` → FE switch sang SMS OTP screen |

### 8.2 System Flow — Case 1 & 2 (Phát hiện sớm tại Initiate)

```
User nhập số tiền rút → tap "Tiếp tục"
        │
        ▼
FE: POST /api/v1/cash/withdraw/initiate
        │
        ▼
BE: Query Smart OTP status (userId)
        │
        ├─ Status: ACTIVATED
        │       └─ Response: { otpType: "SMART_OTP", withdrawId, requireOtp: true }
        │               └─ FE → Smart OTP screen
        │
        └─ Status: NOT_ACTIVATED | LOCKED
                └─ Response: { otpType: "SMS_OTP", withdrawId, requireOtp: true,
                                reason: "SMART_OTP_NOT_ACTIVATED" | "SMART_OTP_LOCKED",
                                maskedPhone: "090****123" }
                        │
                        ▼
                FE: Render SMS OTP screen ngay (không đi qua Smart OTP screen)
                        │
                        ▼
                FE: POST /api/v1/otp/send
                    { purpose: "CASH_WITHDRAWAL", transactionRef: withdrawId }
                        │
                        ▼
                BE: Gửi SMS "Mã xác thực RÚT TIỀN: {OTP}. Hiệu lực 5 phút."
                    OTP được bind với withdrawId (không dùng lại được cho withdrawal khác)
                        │
                        ▼
                User nhập OTP → FE: POST /api/v1/otp/verify
                    { purpose: "CASH_WITHDRAWAL", otpCode, transactionRef: withdrawId }
                        │
                        ├─ Valid → BE trả proofToken → FE gọi confirm
                        └─ Invalid/Expired → retry (max 5 lần SMS OTP)
```

### 8.3 System Flow — Case 3 (Lock giữa chừng)

```
User đang ở Smart OTP screen (đã initiate với otpType: SMART_OTP)
        │
User nhập sai OTP lần N (N = giới hạn NHSV config)
        │
        ▼
FE: POST /api/v1/smart-otp/verify
    { purpose: "CASH_WITHDRAWAL", otpCode: "xxx", transactionRef: withdrawId }
        │
        ▼
BE: Lock Smart OTP → Response: { code: "SMART_OTP_LOCKED" }
        │
        ▼
FE: Switch sang SMS OTP screen
    + Warning banner: "Smart OTP đã bị khóa. Đang chuyển sang SMS OTP."
        │
        ▼
FE: POST /api/v1/otp/send
    { purpose: "CASH_WITHDRAWAL", transactionRef: withdrawId }
    [Cùng withdrawId — giao dịch vẫn valid, chỉ đổi method xác thực]
        │
        ▼
[Tiếp tục như Case 1]
```

> **Rule quan trọng:** `withdrawId` phải được giữ nguyên khi switch method. Không initiate lại — chỉ đổi OTP method. BE phải accept cả Smart OTP và SMS OTP cho cùng một `withdrawId`.

### 8.4 UX SMS Fallback — Chi tiết màn hình

**State A — Smart OTP (default, đã activate)**
```
┌──────────────────────────────────┐
│  Xác thực rút tiền               │
│  5.000.000 VND                   │
│                                  │
│  Nhập mã Smart OTP               │
│  ┌────────────────────────────┐  │
│  │   _  _  _  _  _  _        │  │
│  └────────────────────────────┘  │
│  Mã hết hạn sau: 01:47           │
│                                  │
│           [Xác nhận]             │
└──────────────────────────────────┘
```

**State B — SMS OTP (fallback, Smart OTP chưa activate)**
```
┌──────────────────────────────────┐
│  ℹ Bạn chưa kích hoạt Smart OTP │  ← banner nhẹ (xanh dương)
│  Kích hoạt để xác thực nhanh hơn │  ← link, không phải CTA chính
├──────────────────────────────────┤
│  Xác thực rút tiền               │
│  5.000.000 VND                   │
│                                  │
│  Mã OTP đã gửi đến 090****123   │
│  ┌────────────────────────────┐  │
│  │   _  _  _  _  _  _        │  │
│  └────────────────────────────┘  │
│  Mã hết hạn sau: 04:32   [Gửi lại — 04:32] │
│                                  │
│           [Xác nhận]             │
└──────────────────────────────────┘
```

**State C — SMS OTP (fallback, Smart OTP bị lock)**
```
┌──────────────────────────────────┐
│  ⚠ Smart OTP đã bị khóa        │  ← banner vàng/cam
│  Liên hệ NHSV để mở khóa →     │  ← link CSH
├──────────────────────────────────┤
│  [màn hình SMS OTP như State B]  │
└──────────────────────────────────┘
```

**State D — Smart OTP lock xảy ra giữa chừng (Case 3)**
```
[Smart OTP screen đang hiển thị]
        ↓ (sau khi nhập sai lần cuối)
[Toast ngắn: "Smart OTP đã bị khóa"]
        ↓ (auto-transition ~1.5s)
[State C screen hiển thị]
```

**Không hiển thị "Kích hoạt Smart OTP" link ở State C/D** — user đang trong flow rút tiền, không nên bị distract bởi enrollment flow khác.

---

## 9. Core Requirements — Web Trading Channel

> Điều 12 khoản 4a áp dụng cho **tất cả kênh online** — web trading của Core phải tuân thủ tương đương với NHSV Pro. Hiện tại web trading cũng chỉ dùng access token cho rút tiền < 10M, là gap compliance cần fix song song.

### 9.1 Core Tasks

| # | Yêu cầu với Core | Điều TT134 | Ưu tiên |
|---|---|---|---|
| CORE-1 | Bắt buộc xác thực tối thiểu Điều 7 (Smart OTP hoặc SMS OTP) cho **rút tiền < 10M trên web trading**. Không cho phép hoàn thành giao dịch chỉ với session token | Điều 12 k4a | 🔴 Critical |
| CORE-2 | Hỗ trợ nhận `purpose` field trong Smart OTP verify request: `CASH_WITHDRAWAL`, `ORDER_PLACEMENT`, `LOGIN`. Core cần phân biệt nghiệp vụ để audit log đúng — xem chi tiết BE-7/BE-8 | Điều 18 k5 | 🔴 Critical |
| CORE-3 | SMS OTP fallback: khi user web chưa activate Smart OTP → web trading phải tự động chuyển sang SMS OTP (không block giao dịch). Nội dung SMS phải kèm mục đích `"Mã xác thực RÚT TIỀN"` + TTL 5 phút | Điều 8.1a, Điều 12 k4a | Cao |
| CORE-4 | Audit log rút tiền trên web phải ghi đủ: `auth_method` (SMART_OTP/SMS_OTP), `auth_time`, `device_id` (MAC/browser fingerprint), `source_ip`, `dest_bank_account` | Điều 18 k5 | Cao |
| CORE-5 | Lockout Smart OTP sau ≤ 10 lần sai liên tiếp trên web — vĩnh viễn, không auto-unlock. Sau lock → fallback SMS OTP để không chặn giao dịch | Điều 8.5d | Cao |

### 9.2 Cách raise với Core

1. Raise CORE-2 (`purpose` field) **ngay lập tức** — đây là prerequisite cho cả TradeX lẫn web trading
2. Raise CORE-1 (bắt buộc OTP rút tiền web) kèm tham chiếu Điều 12 k4a
3. Deadline align với NHSV Pro: **28/08/2026**

---

## 10. Dependencies (TradeX side)

| Dependency | Owner | Status | Risk |
|---|---|---|---|
| Smart OTP go-live (3rd party integration) | Smart OTP team | 🔄 Pending handoff | 🔴 HIGH — nếu Smart OTP chưa live trước 28/08, P0 bị miss |
| **[BE-7] Core hỗ trợ `purpose` field trong Smart OTP verify** | Core team / lotte-bridge | 📋 Chưa raise | 🔴 HIGH — không có `purpose`, Core không phân biệt được nghiệp vụ |
| lotte-bridge: Smart OTP verify cho withdrawal | BE Lead | 📋 Chưa confirm có sẵn | Trung bình |
| FE: SmartOTPInput component reusable | FE Lead | ✅ Đã có (Login flow) | Thấp |
| SMS OTP service: bind OTP với `withdrawId` | BE Lead | 📋 Cần verify — service hiện tại có thể chỉ bind theo session | Trung bình |

> **⚠️ Critical Risk 1:** Issue này **hoàn toàn phụ thuộc** vào Smart OTP 3rd party go-live. Nếu trễ, SMS OTP fallback là path duy nhất để đạt deadline 28/08.
>
> **⚠️ Critical Risk 2:** Core phải hỗ trợ `purpose` field (BE-7) trước khi lotte-bridge có thể implement. Cần raise yêu cầu với Core team ngay.

---

Document Status: 📋 Draft | For: BE / FE / Smart OTP team / Core team | Next Steps: (1) Raise BE-7 `purpose` requirement với Core team → (2) Confirm Smart OTP ETA → (3) Kickoff BE-1/FE-1 song song
