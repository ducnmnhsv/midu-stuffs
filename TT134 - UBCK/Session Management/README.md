# Session Management — Kiểm soát phiên đăng nhập

> **Điều 7** — Xác thực & Kiểm soát truy cập
> **Priority:** 🟡 P1

---

## 1. TT134 Reference

| Điều | Khoản | Yêu cầu |
|---|---|---|
| Điều 7 | — | Giới hạn concurrent session (1 TK chỉ login 1 thiết bị tại 1 thời điểm) |
| Điều 7 | — | Session timeout phải hiển thị thông báo (không silent expire) |
| Điều 7 | — | Hỗ trợ revoke session từ xa |

---

## 2. Folder structure

```
Session Management/
├── README.md
└── Specifications/
    ├── Session_Management_API_Spec.md
    └── Session_Management_Spec.html
```

---

## 3. Kanban Cards

| Card | Title | Pri | Status | Deadline |
|---|---|---|---|---|
| (planned) | Session Management implementation | 🟡 P1 | 🆕 Spec done, no card | TBD |

> Currently feature folder — no specific Kanban card yet. Sẽ tạo card khi kickoff.

---

## 4. Current Gap

`TokenService` quản lý refresh token nhưng chưa có cơ chế concurrent session control. Token có TTL (15m access / 24h refresh) nhưng không kiểm soát số lượng session active.

---

## 5. Phạm vi

- Concurrent session policy (1 session/device, kick session cũ khi login mới)
- Session timeout UX (cảnh báo trước khi hết hạn)
- Remote session revoke (hiển thị danh sách thiết bị đang login, cho phép revoke)
- Refresh token rotation

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| `stt35` Device ID Logging (P0-02) | ⬅️ Blocked by | 🟡 Ready, in progress |
| Biometric System — L2 Verification | ⬅️ Optional | 🔒 Blocked GATE |

> Cần `deviceUniqueId` server-side từ P0-02 để bind session với device và list thiết bị đang login.

---

## 7. Output

- ✅ [`Specifications/Session_Management_API_Spec.md`](./Specifications/Session_Management_API_Spec.md) v1.1
- ✅ [`Specifications/Session_Management_Spec.html`](./Specifications/Session_Management_Spec.html) (PM-readable)
- 📋 BE implementation plan
- 📋 FE screen: danh sách thiết bị đang đăng nhập

---

**Document Status:** ✅ Spec done · 📋 Implementation pending
**For:** BE / FE
**Next Steps:** Chờ stt35 (P0-02) xong → kickoff implementation cuối Q3
