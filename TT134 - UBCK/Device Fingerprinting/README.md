# Device Fingerprinting

> **Điều 7** — Xác thực & Kiểm soát truy cập
> **Priority:** 🔴 P0

## Yêu cầu TT134

Hệ thống phải định danh được thiết bị của khách hàng khi truy cập và giao dịch, không chỉ dựa vào `deviceId` do client gửi lên.

## Current Gap

Hiện tại hệ thống lưu `deviceId` từ client (BiometricService, AccessToken), nhưng không có cơ chế fingerprinting độc lập — client hoàn toàn có thể giả mạo `deviceId`.

## Phạm vi

- Tạo device fingerprint hash từ các thông số thiết bị (platform, OS version, unique hardware IDs)
- Gắn fingerprint vào token/session
- Phát hiện thay đổi thiết bị (device rotation)
- Hỗ trợ WTS (macOS, Safari) — đã identified là gap từ TT134 phân tích

## Output dự kiến

- Spec: Device Fingerprinting API
- BE implementation plan
- FE integration guide

## Dependencies

- Session Management (cần fingerprint để bind session với device)

**Document Status:** 🆕 New
**For:** BE / FE / PM
**Next Steps:** Tạo API spec
