# Order 2FA — Xác thực giao dịch 2 lớp

> **Điều 8, 10, 12** — Xác thực OTP + Xác nhận giao dịch
> **Priority:** 🔴 P0 (3 issues)

---

## 1. TT134 Reference

| Điều | Khoản | Yêu cầu |
|---|---|---|
| Điều 8 | k1–k5 | TTL OTP (SMS ≤ 5p, Smart OTP ≤ 2p), lockout ≤ 10 lần, activate trên thiết bị mới |
| Điều 10 | — | Giao dịch quan trọng phải xác thực bằng OTP/phương thức thứ hai |
| Điều 12 | k4a | Rút tiền &lt; 10M phải xác thực bằng Smart OTP |
| Điều 8 | k5b | Smart OTP activation thiết bị mới → SMS OTP + biometric |

---

## 2. Folder structure

```
Order 2FA/
├── README.md
├── Specifications/
│   ├── Order_2FA_Integration_Spec.md
│   └── Order_2FA_Spec.html
└── Issues/
    ├── OTP_TTL_Compliance_Issue.md         ← stt7 / P0-01
    ├── Withdraw_Under10M_SmartOTP_Issue.md ← stt14a / P0-03
    └── BE7_SmartOTP_Biometric_Activation.md ← be7 / sub-issue Điều 8.5b
```

---

## 3. Kanban Cards

| Card | Title | Pri | Status | Deadline |
|---|---|---|---|---|
| `stt7` | TT134-P0-01 OTP TTL Compliance | 🔴 P0 | 🟡 Ready | 14/08/2026 |
| `stt14a` | TT134-P0-03 Rút tiền &lt;10M Smart OTP | 🔴 P0 | 🟡 Ready | 28/08/2026 |
| `be7` | BE-7 Smart OTP Biometric Activation | 🔴 P0 | 📋 Draft | 14/08/2026 |
| `stt5` | Smart OTP giao dịch đầu/phiên | 🟡 P1 | 📦 Backlog | 16/10/2026 |

---

## 4. Current State

- **Smart-OTP** (TOTP-based) đang trong quá trình implement — xem [`../../Smart-OTP/`](../../Smart-OTP/)
- `ConfirmOrdersScreen` có sẵn trên FE nhưng chưa tích hợp 2FA
- `SOtpLoginPinScreen` đã có trên FE
- AAA service đã có sẵn `t_biometric` + `verifyPwdBiometric` (RSA-SHA256) → BE-7 không cần build AAA mới

---

## 5. Phạm vi

- Order confirmation flow với Smart OTP TOTP
- 2FA cho các giao dịch quan trọng (chuyển tiền, thay đổi SĐT, thay đổi thông tin cá nhân)
- SMS OTP fallback khi thiết bị mất/inactive
- Tích hợp với ConfirmOrdersScreen hiện tại
- Device-level biometric (Face ID/Touch ID) cho activate Smart OTP thiết bị mới

---

## 6. Dependencies

| Dep | Type | Status |
|---|---|---|
| Smart OTP 3rd party go-live | 🔌 External | ⏳ ETA TBD |
| Core: purpose field support | 🔌 External (Lotte) | 📋 Cần raise |
| AAA biometric flag (TRADEX_ENV_ENABLE_BIOMETRIC) | ⚙️ Config | 📋 Cần confirm DevOps |
| FE RSA Keystore implementation | 📱 FE | ❓ Cần FE Lead xác nhận |

---

## 7. Liên kết

- [`../../Smart-OTP/Specifications/`](../../Smart-OTP/Specifications/) — Smart OTP API mapping & flow
- [`../../Smart-OTP/Issues/`](../../Smart-OTP/Issues/) — Implementation tasks
- [`../../Knowledge/TradeX-MCP/aaa-main/`](../../Knowledge/TradeX-MCP/aaa-main/) — AAA Service biometric infrastructure
- [`../Biometric System/`](../Biometric%20System/) — L2 Verification (biometric cho giao dịch ≥10M, Điều 9 — blocked GATE)

---

**Document Status:** ✅ Active (3 P0 issues ready)
**For:** BE / FE / QA / Pháp chế
**Next Steps:** (1) Kickoff stt7 + stt14a ngay → (2) BE-7 chờ FE confirm RSA Keystore → (3) Raise Core purpose field
