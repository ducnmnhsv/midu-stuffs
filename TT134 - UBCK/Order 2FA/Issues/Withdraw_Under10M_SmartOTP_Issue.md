# [TT134-P0-03] Rút tiền < 10M — Bắt buộc Smart OTP

**TT134 Reference:** Điều 10 — Xử lý giao dịch & xác nhận  
**Priority:** 🔴 P0  
**Deadline:** 28/08/2026  
**Sub-group:** Order 2FA  
**Phụ thuộc C06:** Không — dùng Smart OTP (lotte-bridge)  
**Phụ thuộc nội bộ:** Smart OTP project (3rd party integration)  

---

## 1. Bối cảnh

TT134 Điều 10 yêu cầu **tất cả giao dịch rút tiền** phải có xác thực lớp thứ hai, bất kể giá trị. Hiện tại, flow rút tiền < 10M **chưa yêu cầu Smart OTP** — chỉ dùng access token. Đây là gap compliance nghiêm trọng cần fix trước 28/08/2026.

> **Lưu ý:** Rút tiền ≥ 10M yêu cầu **sinh trắc học** (biometric) và bị block bởi quyết định C06. Issue này chỉ scope **< 10M với Smart OTP**.

---

## 2. Current vs Target State

| Flow | Hiện tại | Target (TT134) |
|---|---|---|
| Rút tiền < 10M | ❌ Chỉ access token | ✅ Access token + Smart OTP |
| Rút tiền ≥ 10M | ❌ Chỉ access token | 🔒 Blocked (C06 decision) → biometric |
| Đặt lệnh GDCK | ✅ Đã có OTP (per Order 2FA spec) | ✅ Đúng rồi |

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
| BE-5 | Rate limiting: max 5 lần verify sai → lock withdrawal session (15 phút) | cash-service | Trung bình |
| BE-6 | Audit log: ghi `smartOtpVerifiedAt`, `verificationMethod: SMART_OTP` | audit service | Trung bình |

### 4.2 FE Tasks

| # | Task | Screen | Ưu tiên |
|---|---|---|---|
| FE-1 | Sau bước nhập số tiền rút → navigate to Smart OTP screen | WithdrawalScreen | Cao |
| FE-2 | Reuse `SmartOTPInput` component (đã có từ Login S-OTP) | SmartOTPInput | Cao |
| FE-3 | Handle `OTP_INVALID`, `OTP_EXPIRED`, `MAX_RETRY_EXCEEDED` errors | WithdrawalOTPScreen | Cao |
| FE-4 | Hiển thị countdown 30s cho TOTP, tự refresh | WithdrawalOTPScreen | Trung bình |
| FE-5 | Nếu Smart OTP chưa active → redirect to Smart OTP enrollment screen với deep link back | WithdrawalScreen | Cao |

---

## 5. Acceptance Criteria

```
AC-1: Rút tiền < 10M không thể hoàn thành nếu không có Smart OTP hợp lệ
AC-2: Nhập sai OTP ≤ 5 lần: cho retry với error message rõ ràng
AC-3: Nhập sai OTP > 5 lần: lock withdrawal session 15 phút, hiển thị countdown
AC-4: Smart OTP expired (> 30s): hiển thị "Mã OTP đã hết hạn, vui lòng nhập mã mới"
AC-5: User chưa activate Smart OTP: app redirect sang màn hình kích hoạt Smart OTP
AC-6: Sau verify thành công: withdrawal được process ngay, không yêu cầu confirm thêm
AC-7: Audit log ghi đủ: withdrawalId, userId, deviceId, smartOtpVerifiedAt, amount
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
| `SMART_OTP_REQUIRED` | 400 | Withdrawal yêu cầu Smart OTP, chưa cung cấp |
| `SMART_OTP_NOT_ACTIVATED` | 403 | User chưa kích hoạt Smart OTP |
| `WITHDRAWAL_SESSION_LOCKED` | 429 | Quá số lần sai OTP, session bị lock |

---

## 7. UX Considerations

| Scenario | UX Behavior |
|---|---|
| Smart OTP chưa activate | Toast: "Vui lòng kích hoạt Smart OTP để rút tiền" → button "Kích hoạt ngay" |
| OTP sai, còn < 3 lần | Shake animation, hiển thị "Mã OTP không đúng" |
| OTP sai, còn 1 lần | Warning: "Bạn còn 1 lần thử. Nhập sai sẽ tạm khóa rút tiền 15 phút." |
| Session locked | Screen lock: "Rút tiền tạm khóa. Thử lại sau: HH:MM:SS" |

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
