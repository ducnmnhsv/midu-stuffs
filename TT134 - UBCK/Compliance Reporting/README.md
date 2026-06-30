# Compliance Reporting — Báo cáo Tuân thủ

> **Điều 13** — Báo cáo & Lưu trữ dữ liệu
> **Priority:** 🟢 P2

---

## 1. TT134 Reference

| Điều | Yêu cầu |
|---|---|
| Điều 13 | Xuất báo cáo cho UBCK theo yêu cầu |
| Điều 13 | Dữ liệu lưu trữ tối thiểu 5 năm |
| Điều 13 | KH có quyền yêu cầu xuất dữ liệu cá nhân |

---

## 2. Folder structure

```
Compliance Reporting/
└── README.md   ← chỉ có README, chưa có spec
```

---

## 3. Kanban Cards

> Chưa có Kanban card riêng.

---

## 4. Current Gap

- Chưa có compliance reporting module
- Chưa có data retention policy rõ ràng
- Chưa có data export API cho KH

---

## 5. Phạm vi

- Compliance report templates (login history, transaction history, device history)
- Data retention & archiving (5-year policy)
- Data export API (GDPR-style, user requests their data)
- Report generation scheduler

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| Audit Log | ⬅️ Blocked by | 📋 Spec chưa có |
| Session Management + Device Fingerprinting | ⬅️ Source data | 🟡 Spec done / 🟡 P0 in progress |

---

## 7. Output dự kiến

- Compliance reporting spec
- Report template designs
- Data retention policy
- BE data export API

---

**Document Status:** 🆕 README only · 📋 Spec chưa có
**For:** PM / BE / DevOps
**Next Steps:** Chờ Audit Log spec → xác định report templates cần thiết
