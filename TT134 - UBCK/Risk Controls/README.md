# Risk Controls — Quản lý Rủi ro

> **Điều 11** — Quản lý Rủi ro
> **Priority:** 🟡 P1

---

## 1. TT134 Reference

| Điều | Yêu cầu |
|---|---|
| Điều 11 | Hiển thị cảnh báo rủi ro cho KH khi giao dịch (risk disclosure) |
| Điều 11 | Hạn mức giao dịch theo từng loại KH |
| Điều 11 | Phát hiện hành vi giao dịch bất thường |

---

## 2. Folder structure

```
Risk Controls/
└── README.md   ← chỉ có README, chưa có spec
```

---

## 3. Kanban Cards

> Chưa có Kanban card riêng.

---

## 4. Current Gap

- Chưa có risk disclosure screen/flow trên app
- Chưa có hạn mức giao dịch theo phân loại KH
- Chưa có cơ chế phát hiện bất thường (threshold-based rules)

---

## 5. Phạm vi

- Risk disclosure screen (hiển thị lần đầu hoặc khi có thay đổi)
- Trading limit config (per customer type)
- Threshold-based anomaly detection
- Risk acceptance flow (KH xác nhận đã hiểu rủi ro)

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| Alert System | ⬅️ Blocked by | 📋 Spec chưa có |
| Biometric System — L4 Lockout (10 lần fail biometric) | ⬅️ Related | 🔒 Blocked GATE |

---

## 7. Output dự kiến

- Risk controls spec
- Risk disclosure mẫu (PM/Legal review)
- BE limit checking service
- FE risk disclosure screen

---

**Document Status:** 🆕 README only · 📋 Spec chưa có
**For:** PM / Legal / BE / FE
**Next Steps:** PM review risk disclosure content + Alert System kickoff
