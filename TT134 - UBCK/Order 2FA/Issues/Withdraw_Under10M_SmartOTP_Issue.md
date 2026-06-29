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
| BE-6 | Audit log: ghi `smartOtpVerifiedAt`, `verificationMethod: SMART_OTP` | audit service | Trung bình |

### 4.2 FE Tasks

| # | Task | Screen | Ưu tiên |
|---|---|---|---|
| FE-1 | Sau bước nhập số tiền rút → navigate to Smart OTP screen | WithdrawalScreen | Cao |
| FE-2 | Reuse `SmartOTPInput` component (đã có từ Login S-OTP) | SmartOTPInput | Cao |
| FE-3 | Handle `OTP_INVALID`, `OTP_EXPIRED`, `MAX_RETRY_EXCEEDED` errors | WithdrawalOTPScreen | Cao |
| FE-4 | Hiển thị challenge session countdown 2 phút (TTL per Điều 8.5đ); code bên trong tự refresh mỗi 30s theo TOTP | WithdrawalOTPScreen | Trung bình |
| FE-5 | Nếu Smart OTP chưa active → fallback sang SMS OTP tự động (không chặn giao dịch, không force redirect enrollment) | WithdrawalScreen | Cao |
| FE-6 | Nếu Smart OTP bị locked → show locked screen + button "Dùng SMS OTP thay thế" | WithdrawalOTPScreen | Cao |

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

## 8. Dependencies

| Dependency | Owner | Status | Risk |
|---|---|---|---|
| Smart OTP go-live (3rd party integration) | Smart OTP team | 🔄 Pending handoff | 🔴 HIGH — nếu Smart OTP chưa live trước 28/08, P0 bị miss |
| lotte-bridge: Smart OTP verify cho withdrawal | BE Lead | 📋 Chưa confirm có sẵn | Trung bình |
| FE: SmartOTPInput component reusable | FE Lead | ✅ Đã có (Login flow) | Thấp |

> **⚠️ Critical Risk:** Issue này **hoàn toàn phụ thuộc** vào Smart OTP 3rd party go-live. Cần confirm ETA của Smart OTP trước khi estimate deadline này.

---

Document Status: 📋 Draft | For: BE / FE / Smart OTP team | Next Steps: Confirm Smart OTP ETA → kickoff BE-1/FE-1 song song
