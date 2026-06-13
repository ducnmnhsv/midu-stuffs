# Risk Controls

> **Điều 11** — Quản lý Rủi ro
> **Priority:** 🟡 P1

## Yêu cầu TT134

- Phải hiển thị cảnh báo rủi ro cho khách hàng khi giao dịch (risk disclosure)
- Hạn mức giao dịch theo từng loại khách hàng
- Phát hiện hành vi giao dịch bất thường

## Current Gap

- Chưa có risk disclosure screen/flow trên app
- Chưa có hạn mức giao dịch theo phân loại KH
- Chưa có cơ chế phát hiện bất thường (threshold-based rules)

## Phạm vi

- Risk disclosure screen (hiển thị lần đầu hoặc khi có thay đổi)
- Trading limit config (per customer type)
- Threshold-based anomaly detection
- Risk acceptance flow (KH xác nhận đã hiểu rủi ro)

## Output dự kiến

- Risk controls spec
- Risk disclosure mẫu (PM/Legal review)
- BE limit checking service
- FE risk disclosure screen

## Dependencies

- Alert System (để gửi cảnh báo khi vượt hạn mức hoặc phát hiện bất thường)
- [Biometric System](../Biometric%20System/) — L4 Lockout & Recovery (10 lần fail biometric → khóa giao dịch), consecutive_failures tracking

**Document Status:** 🆕 New
**For:** PM / Legal / BE / FE
**Next Steps:** PM review risk disclosure content
