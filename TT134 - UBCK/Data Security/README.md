# Data Security — Bảo mật Dữ liệu

> **Điều 9** — Bảo mật Dữ liệu
> **Priority:** 🟢 P2

---

## 1. TT134 Reference

| Điều | Yêu cầu |
|---|---|
| Điều 9 | Dữ liệu cá nhân KH phải được mã hóa at rest |
| Điều 9 | Thông tin nhạy cảm (SĐT, CMND/CCCD, email, địa chỉ) phải được mask khi hiển thị |
| Điều 9 | Key management phải an toàn |

---

## 2. Folder structure

```
Data Security/
└── README.md   ← chỉ có README, chưa có spec
```

---

## 3. Kanban Cards

| Card | Title | Pri | Status | Deadline |
|---|---|---|---|---|
| `stt40` | Lưu trữ thông tin sinh trắc học KH | ⚪ P3 | 🔒 Blocked GATE | TBD |

---

## 4. Current Gap

- Password đã được RSA encrypt + HSM (OK)
- Biometric public key lưu trong DB (OK, nhưng plain text public key)
- **Data at rest:** MySQL chưa encrypt ở table/column level
- **Data masking:** chưa có masking cho field nhạy cảm trên API response
- **Key rotation:** chưa có policy

---

## 5. Phạm vi

- Column-level encryption cho PII data (SĐT, CMND, email)
- Data masking service (auto-mask trong API response)
- Key management & rotation policy
- Audit ai đã access dữ liệu PII

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| Audit Log (cần log PII access) | ⬅️ Blocked by | 📋 Spec chưa có |
| Biometric template encryption (stt40 — AES-256-GCM + KMS) | ⬅️ Related | 🔒 Blocked GATE |

---

## 7. Output dự kiến

- Data security spec (encryption architecture)
- Data masking rules per field/role
- Implementation plan (DB migration, API changes)

---

**Document Status:** 🆕 README only · 📋 Spec chưa có
**For:** BE / DevOps / Security
**Next Steps:** Chờ Audit Log spec → tạo data classification & encryption spec
