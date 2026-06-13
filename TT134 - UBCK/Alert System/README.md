# Alert System

> **Điều 8** — Hệ thống Cảnh báo
> **Priority:** 🟡 P1

## Yêu cầu TT134

Hệ thống phải cảnh báo khách hàng khi:
- Có đăng nhập từ thiết bị lạ / địa điểm lạ
- Phát sinh giao dịch (đặt lệnh, chuyển tiền)
- Thay đổi thông tin cá nhân (SĐT, email, địa chỉ)
- Phát hiện hành vi bất thường

## Current Gap

Hiện tại chỉ có locked-account modal (5 lần sai mật khẩu). Chưa có push notification cho các sự kiện bảo mật.

## Phạm vi

- Push notification (Firebase/OneSignal) cho sự kiện login lạ
- Push notification cho giao dịch phát sinh
- Push notification cho thay đổi thông tin
- Email/SMS notification fallback
- In-app notification center

## Output dự kiến

- Alert system spec (event types, channels, templates)
- Push notification setup (FCM/APNs)
- BE event publishing service
- FE notification screen

## Dependencies

- OneSignal (đã có trong app — thấy trong Pods)
- Device Fingerprinting (để phát hiện login từ thiết bị lạ)

**Document Status:** 🆕 New
**For:** BE / FE / DevOps
**Next Steps:** Tạo event catalog + notification spec
