# Order 2FA (Xác thực giao dịch 2 lớp)

> **Điều 10** — Xử lý Giao dịch & Xác nhận
> **Priority:** 🔴 P0

## Yêu cầu TT134

Các giao dịch quan trọng (đặt lệnh, chuyển tiền, thay đổi thông tin) phải được xác thực bằng OTP hoặc phương thức xác thực thứ hai.

## Current State

- **Smart-OTP** (TOTP-based) đang trong quá trình implement — xem [`../../Smart-OTP/`](../../Smart-OTP/)
- `ConfirmOrdersScreen` có sẵn trên FE nhưng chưa tích hợp 2FA
- SmartOTP Login (`SOtpLoginPinScreen`) đã có trên FE

## Phạm vi

- Order confirmation flow với SmartOTP TOTP
- 2FA cho các giao dịch quan trọng (chuyển tiền, thay đổi SĐT, thay đổi thông tin cá nhân)
- SMS OTP fallback khi thiết bị mất/inactive
- Tích hợp với ConfirmOrdersScreen hiện tại

## Liên kết

- [Smart-OTP Spec](../../Smart-OTP/Specifications/) — API mapping & flow
- [Smart-OTP Issues](../../Smart-OTP/Issues/) — Implementation tasks
- [Smart-OTP Test Cases](../../Smart-OTP/Test%20Cases/) — Test sheets
- [Biometric System](../Biometric%20System/) — L2 Verification (dùng biometric làm 2FA cho giao dịch ≥10tr, lockout khi fail 10 lần)

## Output dự kiến

- Order 2FA integration spec (bổ sung cho Smart-OTP)
- FE update plan cho ConfirmOrdersScreen
- Test plan cho giao dịch có 2FA

**Document Status:** 🆕 New (references Smart-OTP)
**For:** BE / FE / QA
**Next Steps:** Review Smart-OTP status & xác định gaps
