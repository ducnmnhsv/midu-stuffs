# Alert System — Hệ thống Cảnh báo

> **Điều 8** — Cảnh báo bảo mật
> **Priority:** 🟡 P1

---

## 1. TT134 Reference

| Điều | Yêu cầu |
|---|---|
| Điều 8 | Cảnh báo KH khi: login từ thiết bị lạ, phát sinh giao dịch, thay đổi thông tin, hành vi bất thường |

---

## 2. Folder structure

```
Alert System/
└── README.md   ← chỉ có README, chưa có spec
```

---

## 3. Kanban Cards

> Chưa có Kanban card riêng. Sẽ tạo khi kickoff.

---

## 4. Current Gap

Hiện tại chỉ có locked-account modal (5 lần sai mật khẩu). Chưa có push notification cho các sự kiện bảo mật.

---

## 5. Phạm vi

- Push notification (Firebase/OneSignal) cho login thiết bị lạ
- Push notification cho giao dịch phát sinh
- Push notification cho thay đổi thông tin (SĐT, email)
- Email/SMS notification fallback
- In-app notification center

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| `stt35` Device ID Logging (P0-02) | ⬅️ Blocked by | 🟡 Ready, in progress |
| OneSignal SDK (đã có trong app — Pods) | ⚙️ Infra | ✅ Available |
| Device Fingerprinting | ⬅️ Related | 📋 Spec done |

---

## 7. Output dự kiến

- Alert system spec (event types, channels, templates)
- Push notification setup (FCM/APNs)
- BE event publishing service
- FE notification screen

---

**Document Status:** 🆕 README only · 📋 Spec chưa có
**For:** BE / FE / DevOps
**Next Steps:** Chờ stt35 (P0-02) xong → tạo event catalog + notification spec
