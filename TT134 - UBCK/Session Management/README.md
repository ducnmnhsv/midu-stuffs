# Session Management

> **Điều 7** — Xác thực & Kiểm soát truy cập
> **Priority:** 🔴 P0

## Yêu cầu TT134

- Giới hạn concurrent session (một tài khoản chỉ login được trên một thiết bị tại một thời điểm)
- Session timeout phải hiển thị thông báo cho khách hàng (không silent expire)
- Hỗ trợ revoke session từ xa

## Current Gap

TokenService quản lý refresh token nhưng chưa có cơ chế concurrent session control. Token có TTL (15m access / 24h refresh) nhưng không kiểm soát số lượng session active.

## Phạm vi

- Concurrent session policy (1 session/device, kick session cũ khi login mới)
- Session timeout UX (cảnh báo trước khi hết hạn)
- Remote session revoke (hiển thị danh sách thiết bị đang login, cho phép revoke)
- Refresh token rotation

## Output dự kiến

- Session management spec
- BE implementation plan
- FE screen: danh sách thiết bị đang đăng nhập

## Dependencies

- Device Fingerprinting (để định danh thiết bị trong session list)
- [Biometric System](../Biometric%20System/) — L2 Verification: xác thực biometric cho giao dịch đầu tiên mỗi phiên đăng nhập

**Document Status:** 🆕 New
**For:** BE / FE
**Next Steps:** Tạo API spec + UX flow
