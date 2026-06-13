# Audit Log

> **Điều 5, 10** — Hệ thống CNTT & Xử lý giao dịch
> **Priority:** 🟡 P1

## Yêu cầu TT134

Hệ thống phải ghi lại đầy đủ audit trail:
- Ai đã làm gì, khi nào, trên thiết bị nào
- Dữ liệu nào bị thay đổi
- Phải phục vụ được điều tra và báo cáo

## Current Gap

Chỉ có `AccessTokenHistory` cho token lifecycle. Chưa có audit log cho:
- Data modification (user profile, account info)
- Transaction operations (order, transfer)
- Admin operations (config change)
- Login/logout activities

## Phạm vi

- Audit event model (who, what, when, where, device fingerprint)
- Audit log storage (append-only, immutable)
- Audit query API (phục vụ compliance investigation)
- Integration với các service (aaa, rest-proxy, ws-v2)
- Data retention policy (tối thiểu 5 năm theo Điều 13)

## Output dự kiến

- Audit log spec (event types, schema, storage)
- BE implementation: audit service / middleware
- Audit query API cho compliance

## Dependencies

- Device Fingerprinting (để ghi device fingerprint trong audit events)
- [Biometric System](../Biometric%20System/) — Audit log cho mọi template read/write, append-only log store cho biometric events

**Document Status:** 🆕 New
**For:** BE / DevOps
**Next Steps:** Tạo audit event schema spec
