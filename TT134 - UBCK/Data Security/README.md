# Data Security

> **Điều 9** — Bảo mật Dữ liệu
> **Priority:** 🟢 P2

## Yêu cầu TT134

- Dữ liệu cá nhân của khách hàng phải được mã hóa khi lưu trữ (at rest)
- Thông tin nhạy cảm (SĐT, CMND/CCCD, email, địa chỉ) phải được che dấu (masking) khi hiển thị
- Quản lý khóa bảo mật (key management) phải an toàn

## Current Gap

- Password đã được RSA encrypt + HSM (OK)
- Biometric public key lưu trong DB (OK, nhưng ở dạng plain text public key)
- **Data at rest**: MySQL chưa encrypt ở table/column level
- **Data masking**: chưa có masking cho các field nhạy cảm trên API response
- **Key rotation**: chưa có policy

## Phạm vi

- Column-level encryption cho PII data (SĐT, CMND, email)
- Data masking service (auto-mask trong API response)
- Key management & rotation policy
- Audit ai đã access dữ liệu PII

## Output dự kiến

- Data security spec (encryption architecture)
- Data masking rules per field/role
- Implementation plan (DB migration, API changes)

## Dependencies

- Audit Log (để ghi lại PII access)
- [Biometric System](../Biometric%20System/) — Biometric template encryption (AES-256-GCM + KMS), biometric_db isolation

**Document Status:** 🆕 New
**For:** BE / DevOps / Security
**Next Steps:** Tạo data classification & encryption spec
