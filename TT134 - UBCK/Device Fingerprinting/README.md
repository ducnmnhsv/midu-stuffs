# Device Fingerprinting — Định danh thiết bị

> **Điều 7, 18** — Xác thực & Logging thiết bị
> **Priority:** 🔴 P0 (1 issue)

---

## 1. TT134 Reference

| Điều | Khoản | Yêu cầu |
|---|---|---|
| Điều 7 | — | Hệ thống phải định danh thiết bị KH khi truy cập/giao dịch |
| Điều 18 | k3a | Mobile: IMEI/Serial/WLAN MAC/Android ID. Web: MAC address hoặc PC fingerprint |
| Điều 18 | k4 | Nhật ký GDCK phải gồm device ID + IP + auth method + auth time |
| Điều 18 | k5 | Nhật ký rút/chuyển tiền: device ID + IP + auth method + TK nhận |

---

## 2. Folder structure

```
Device Fingerprinting/
├── README.md
├── Specifications/
│   ├── Device_Fingerprinting_API_Spec.md
│   └── Device_Fingerprinting_Spec.html
└── Issues/
    └── Device_ID_Logging_Issue.md  ← stt35 / P0-02
```

---

## 3. Kanban Cards

| Card | Title | Pri | Status | Deadline |
|---|---|---|---|---|
| `stt35` | TT134-P0-02 Device ID Logging — GDCK & Rút tiền | 🔴 P0 | 🟡 Ready | 28/08/2026 |
| `stt36` | Mobile security: 1 device/TK + anti-tampering | 🟡 P1 | 📦 Backlog | 30/10/2026 |

---

## 4. Current Gap

| Field | Status | Vấn đề |
|---|---|---|
| `device_id` (client UUID) | ✅ Có trong JWT/header | Client-controlled, không server-verified |
| `deviceUniqueId` (IMEI/Serial/AndroidID) | ⚠️ Partial | Có trong `t_biometric`, `t_refresh_token` nhưng chưa log vào transaction log |
| `fingerprint_hash` | 📋 Planned | Spec đã có, chưa implement |
| `sourceIp` | ✅ Có | Server thấy nhưng chưa log vào transaction log |
| `authMethod` | ❓ Chưa confirm | Điều 18 k4/k5 yêu cầu log phương thức xác thực |

---

## 5. Phạm vi

- **P0-02 (stt35):** Log `deviceUniqueId` + `sourceIp` + `authMethod` vào `t_order_log` + `t_withdrawal_log` theo đúng Điều 18 k4/k5
- **STT 36 (P1):** 1 active device per account + anti-tampering (jailbreak/root detection, Frida/SSL pinning bypass)
- **Phase 2:** Server-computed `fingerprint_hash` cho fraud detection (planned)

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| DB migration access | 🗄️ DBA | 📋 Chưa bắt đầu |
| FE SDK: đảm bảo `deviceUniqueId` available | 📱 FE | ❓ Cần verify |
| API middleware: extract `X-Device-Id` header + log | ⚙️ BE | 📋 Chưa bắt đầu |
| Core (Web Trading): collect MAC/browser fingerprint cho web GDCK + withdraw | 🔌 Lotte Core | 📋 Cần raise |

---

## 7. Cards block bởi feature này

`stt35` (P0-02 Device ID Logging) là tiền đề cho:
- Session Management — cần fingerprint để bind session với device
- Alert System — phát hiện login từ thiết bị lạ
- Audit Log — log device fingerprint vào audit events

---

**Document Status:** ✅ Active (1 P0 issue ready)
**For:** BE / FE / DBA
**Next Steps:** (1) BE viết migration script — `auth_method`, `auth_time`, `device_unique_id`, `source_ip` cho `t_order_log` + `t_withdrawal_log` → (2) FE verify SDK availability → (3) Raise Core
