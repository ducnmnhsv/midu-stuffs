# Compliance Reporting

> **Điều 13** — Báo cáo & Dữ liệu Tuân thủ
> **Priority:** 🟢 P2

## Yêu cầu TT134

- Hệ thống phải xuất được báo cáo cho UBCK theo yêu cầu
- Dữ liệu phải được lưu trữ tối thiểu 5 năm
- Khách hàng có quyền yêu cầu xuất dữ liệu cá nhân (data portability)

## Current Gap

- Chưa có compliance reporting module
- Chưa có data retention policy rõ ràng
- Chưa có data export API cho KH

## Phạm vi

- Compliance report templates (login history, transaction history, device history)
- Data retention & archiving (5-year policy)
- Data export API (GDPR-style, user requests their data)
- Report generation scheduler

## Output dự kiến

- Compliance reporting spec
- Report template designs
- Data retention policy
- BE data export API

## Dependencies

- Audit Log (nguồn dữ liệu cho reports)
- Session Management + Device Fingerprinting (device lịch sử)

**Document Status:** 🆕 New
**For:** PM / BE / DevOps
**Next Steps:** Xác định report templates cần thiết
