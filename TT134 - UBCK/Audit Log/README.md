# Audit Log — Nhật ký Hệ thống

> **Điều 5, 10** — Hệ thống CNTT & Xử lý giao dịch
> **Priority:** 🟡 P1

---

## 1. TT134 Reference

| Điều | Yêu cầu |
|---|---|
| Điều 5 | Hệ thống CNTT phải có đầy đủ audit trail |
| Điều 10 | Xử lý giao dịch phải log đầy đủ: ai, làm gì, khi nào, trên thiết bị nào, dữ liệu nào thay đổi |
| Điều 13 | Data retention tối thiểu 5 năm |

---

## 2. Folder structure

```
Audit Log/
└── README.md   ← chỉ có README, chưa có spec
```

---

## 3. Kanban Cards

> Chưa có Kanban card riêng.

---

## 4. Current Gap

Chỉ có `AccessTokenHistory` cho token lifecycle. Chưa có audit log cho:
- Data modification (user profile, account info)
- Transaction operations (order, transfer)
- Admin operations (config change)
- Login/logout activities

---

## 5. Phạm vi

- Audit event model (who, what, when, where, device fingerprint)
- Audit log storage (append-only, immutable)
- Audit query API (phục vụ compliance investigation)
- Integration với các service (aaa, rest-proxy, ws-v2)
- Data retention policy (≥ 5 năm theo Điều 13)

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| `stt35` Device ID Logging (P0-02) | ⬅️ Blocked by | 🟡 Ready, in progress |
| Biometric System — audit cho mọi template read/write | ⬅️ Related | 🔒 Blocked GATE |

> Cần `deviceUniqueId` từ P0-02 để ghi device fingerprint vào audit events.

---

## 7. Cards block bởi feature này

- **Compliance Reporting** — nguồn data cho reports
- **Data Security** — log PII access events

---

## 8. Output dự kiến

- Audit log spec (event types, schema, storage)
- BE implementation: audit service / middleware
- Audit query API cho compliance

---

**Document Status:** 🆕 README only · 📋 Spec chưa có
**For:** BE / DevOps
**Next Steps:** Chờ stt35 (P0-02) xong → tạo audit event schema spec
