# [TT134-P0-01] OTP TTL Compliance — SMS / Thẻ ma trận / Soft OTP

**TT134 Reference:** Điều 9 — Yêu cầu OTP  
**Priority:** 🔴 P0  
**Deadline:** 14/08/2026  
**Sub-group:** Order 2FA  
**Phụ thuộc C06:** Không — xử lý nội bộ  

---

## 1. Bối cảnh

TT134 Điều 9 quy định thời hạn hiệu lực (TTL) tối đa cho các loại OTP được sử dụng trong xác thực giao dịch. Hiện tại, cấu hình TTL trong hệ thống (Lotte Core + TradeX SMS) **chưa được kiểm tra và confirm** theo đúng yêu cầu TT134.

---

## 2. Yêu cầu TT134

| Loại OTP | TTL tối đa theo TT134 | Trạng thái hiện tại |
|---|---|---|
| SMS OTP | 3 phút (180s) | ❓ Chưa confirm |
| Thẻ ma trận (Matrix card) | 1 lần dùng (one-time) | ❓ Chưa confirm |
| Soft OTP / Smart OTP (TOTP) | 30s per code (TOTP standard) | ❓ Chưa confirm |

---

## 3. Scope of Work

### 3.1 BE Tasks

| # | Task | Service | Ưu tiên |
|---|---|---|---|
| BE-1 | Audit cấu hình SMS OTP TTL hiện tại trong Lotte Core config | lotte-bridge / Lotte Core | Cao |
| BE-2 | Update SMS OTP TTL về ≤ 180s nếu khác | lotte-bridge config / TradeX SMS | Cao |
| BE-3 | Xác nhận Matrix card đã one-time (không reusable) | Core Lotte | Cao |
| BE-4 | Verify TOTP window (Smart OTP) — đảm bảo 1 window (30s), không allow drift > 1 | Smart OTP service | Trung bình |
| BE-5 | Update nội dung SMS template: thêm "OTP có hiệu lực trong X phút" | TradeX SMS service | Cao |
| BE-6 | Log TTL expiry events vào audit trail | AAA / audit service | Trung bình |

### 3.2 FE Tasks

| # | Task | Screen | Ưu tiên |
|---|---|---|---|
| FE-1 | Hiển thị countdown timer OTP trên màn hình nhập SMS OTP | SMSOTPInput | Cao |
| FE-2 | Disable "Gửi lại" (resend) button trong thời gian TTL còn hiệu lực | SMSOTPInput | Cao |
| FE-3 | Hiển thị error message rõ ràng khi OTP hết hạn ("OTP đã hết hạn, vui lòng yêu cầu mã mới") | SMSOTPInput | Cao |
| FE-4 | Soft OTP: hiển thị countdown 30s, tự động refresh code | SmartOTPInput | Trung bình |

---

## 4. Acceptance Criteria

```
AC-1: SMS OTP expire sau đúng ≤ 180 giây kể từ lúc gửi
AC-2: Nhập SMS OTP sau khi hết hạn → trả lỗi "OTP expired" (HTTP 400 / error code OTP_EXPIRED)
AC-3: Matrix card OTP không thể dùng lại sau khi đã verify thành công
AC-4: Smart OTP (TOTP) không chấp nhận code ngoài window 30s hiện tại (drift ≤ 1 window)
AC-5: SMS nội dung có thông báo thời hạn (VD: "Mã OTP của bạn là XXXXXX. Có hiệu lực trong 3 phút.")
AC-6: FE hiển thị countdown timer đồng bộ với TTL server-side
AC-7: Log đầy đủ: otp_generated_at, otp_expires_at, otp_used_at (hoặc otp_expired_at)
```

---

## 5. API Impact

### Endpoint bị ảnh hưởng

| Method | Endpoint | Thay đổi |
|---|---|---|
| POST | `/api/v1/otp/send` | Thêm `expires_at` trong response |
| POST | `/api/v1/otp/verify` | Kiểm tra TTL, trả `OTP_EXPIRED` nếu quá hạn |
| POST | `/api/v1/smart-otp/verify` | Validate TOTP window, không allow drift > 1 |

### Response thay đổi — `/api/v1/otp/send`

```json
{
  "success": true,
  "data": {
    "otpType": "SMS",
    "expiresAt": "2026-08-01T10:03:00Z",
    "ttlSeconds": 180,
    "maskedPhone": "090****123"
  }
}
```

### Error code mới

| Code | HTTP | Mô tả |
|---|---|---|
| `OTP_EXPIRED` | 400 | OTP đã hết hạn |
| `OTP_ALREADY_USED` | 400 | OTP đã được sử dụng (matrix card) |

---

## 6. Testing Notes

- Test case: Gửi OTP, chờ > 180s, nhập → expect OTP_EXPIRED
- Test case: Dùng matrix card OTP lần 2 → expect OTP_ALREADY_USED
- Test case: TOTP nhập code của step T-2 → expect invalid
- Test SMS nội dung có đủ thông báo TTL

---

## 7. Dependencies

| Dependency | Owner | Status |
|---|---|---|
| Lotte Core — config SMS OTP TTL | IT / Lotte support | ❓ Cần confirm |
| Smart OTP service config | BE Lead | 🔄 Pending Smart OTP go-live |
| TradeX SMS template update | BE Lead | 📋 Chưa bắt đầu |

---

Document Status: 📋 Draft | For: BE / FE / QA | Next Steps: BE-1 audit TTL config ngay, FE-1/FE-2 sau khi confirm TTL value
