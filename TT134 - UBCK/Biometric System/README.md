# Biometric System — Sinh trắc học

> **Điều 7, 9, 10, 11** — Xác thực sinh trắc học server-side
> **Priority:** 🟢 P2 · 🔒 **Blocked GATE** (BOM Decision: C06 vs VNPT — 30/06/2026)

---

## 1. TT134 Reference

| Điều | Yêu cầu |
|---|---|
| Điều 7 | Xác thực biometric khi đăng nhập (optional) |
| Điều 9 | Server-side biometric verification (face matching vs CSDL Quốc gia) |
| Điều 10 | Lock sau ≤ 10 lần verify fail; timeout 3 phút mỗi session biometric |
| Điều 11 | Risk-based: bắt buộc biometric cho rút tiền ≥ 10M, GDCK đầu phiên |
| Điều 20 | Lưu trữ biometric data (nếu vendor là VNPT, NHSV phải build DB) |

---

## 2. Folder structure

```
Biometric System/
├── README.md
└── Specifications/
    └── Biometric_System_Spec.html   ← (đổi tên từ biometric.html)
```

---

## 3. Kanban Cards (5 cards blocked GATE)

| Card | Title | Pri | Status | Deadline |
|---|---|---|---|---|
| `stt8` | Sinh trắc học FIDO (FAR &lt;0.01%, FRR &lt;5%, PAD) | 🟢 P2 | 🔒 Blocked GATE | 30/10/2026 |
| `stt10` | Biometric rules: lock ≤10, timeout 3p, verify CSDL QG | 🟢 P2 | 🔒 Blocked GATE | TBD |
| `stt12` | GDCK online: biometric đầu/phiên | 🟢 P2 | 🔒 Blocked GATE | 30/10/2026 |
| `stt14b` | Rút tiền ≥10M: biometric bắt buộc | 🟢 P2 | 🔒 Blocked GATE | TBD (28/08 nếu VNPT) |
| `stt40` | Lưu trữ thông tin sinh trắc học KH | ⚪ P3 | 🔒 Blocked GATE | TBD |
| `stt9` | PAD Certification | ✅ P0 | ✅ Done | — |

---

## 4. ⚠️ Phân biệt 2 loại biometric trong dự án

| | **Device-level biometric** (Điều 8.5b) | **Server-side biometric** (Điều 9) |
|---|---|---|
| Cơ chế | Face ID / Touch ID (OS-level) | C06/VNPT face matching vs CSDL |
| Vendor | Apple / Google (free) | C06 hoặc VNPT |
| Trạng thái | ✅ **Có thể implement ngay** | 🔒 **Blocked GATE** |
| AAA infra | Đã có (`t_biometric` + RSA verify) | Chưa có |
| Card | `be7` (Order 2FA) | `stt8`, `stt10`, `stt12`, `stt14b`, `stt40` |

**=> BE-7 (be7) thuộc folder Order 2FA, KHÔNG bị blocked GATE.** Folder này chỉ scope server-side biometric.

---

## 5. GATE Decision Status

| Vendor option | Pros | Cons |
|---|---|---|
| **C06 (Bộ Công an)** | ✅ Verify vs CSDL Quốc gia (theo Điều 9 chính xác nhất). ✅ NHSV không cần build biometric DB | ⚠️ Setup AgentGW/HSM/VPN S2S phức tạp. ⚠️ Admin credentials risk |
| **VNPT (interim)** | ✅ Setup nhanh hơn — có thể kịp 28/08/2026. ✅ FIDO certified | ⚠️ Verify vs ảnh lưu (không phải CSDL QG). ⚠️ NHSV phải build biometric DB (Điều 20) |

**BOM Decision Day:** 30/06/2026

---

## 6. Phạm vi (sau GATE)

- **stt8:** FIDO biometric impl, FAR < 0.01%, FRR < 5%, PAD
- **stt10:** Lockout sau 10 fail + timeout 3 phút + verify CSDL QG
- **stt12:** Trigger biometric cho GDCK đầu phiên
- **stt14b:** Enforce biometric cho rút tiền ≥ 10M (bind với withdrawalId, anti-replay)
- **stt40:** [If VNPT] Build `t_biometric_data` với AES-256-GCM + KMS

---

## 7. Dependencies

| Dep | Type | Status |
|---|---|---|
| **★ BOM GATE Decision** | ⬅️ Blocked by | ⏳ 30/06/2026 |
| Vendor (C06 hoặc VNPT) integration | 🔌 External | ⏳ Sau GATE |
| FE FIDO biometric capture | 📱 FE | ⏳ Sau GATE |

---

## 8. Output

- ✅ [`Specifications/Biometric_System_Spec.html`](./Specifications/Biometric_System_Spec.html) — draft plan, dark mode
- 📋 BE implementation plan (chờ GATE)
- 📋 Integration spec C06/VNPT (chờ GATE)

---

**Document Status:** 📋 Draft plan · 🔒 Blocked GATE
**For:** BOM / IT / BE / FE / Pháp chế
**Next Steps:** (1) BOM decision 30/06/2026 → (2) Vendor kickoff → (3) Implementation song song với P0 đang chạy
