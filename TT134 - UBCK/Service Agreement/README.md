# Service Agreement — Hợp đồng/Điều khoản dịch vụ

> **Điều 5, Khoản 4** — Cung cấp dịch vụ phải thể hiện bằng hợp đồng/điều khoản với KH
> **Priority:** 🟡 P1

---

## 1. TT134 Reference

Khoản 4 Điều 5 yêu cầu hợp đồng/điều khoản dịch vụ với KH phải bao gồm:

| # | Yêu cầu | Mô tả |
|---|---------|-------|
| 1 | **Phương thức GD trực tuyến & loại GD tương ứng** | Hiển thị danh sách phương thức GD online + loại GD |
| 2 | **SĐT đăng ký + xác minh** | SĐT phải được xác minh thuộc quyền sử dụng hợp pháp của KH |
| 3 | **Rủi ro & trách nhiệm bồi thường** | Nêu rõ rủi ro & trách nhiệm của mỗi bên |

---

## 2. Folder structure

```
Service Agreement/
├── README.md
└── Specifications/
    ├── Service_Agreement_API_Spec.md
    └── Service_Agreement_Spec.html
```

---

## 3. Kanban Cards

| Card | Title | Pri | Status | Deadline |
|---|---|---|---|---|
| `stt3` | Xác minh SĐT thuộc quyền sử dụng hợp pháp KH | ⚪ P3 | 🔍 Research | TBD |

---

## 4. Current Gap

- **Phone verification service:** Chưa có cơ chế xác minh quyền sở hữu SIM (yêu cầu xác minh từ bên thứ ba)
- **Carrier API integration:** Chưa tích hợp GSMA Open Gateway / CAMARA APIs (Viettel/VinaPhone/MobiFone)
- **VNeID/RAR integration:** Chưa tích hợp RAR Center C06 — Đề án 06
- **Terms & Conditions management:** Chưa có version control, acceptance tracking
- **Trading methods registry:** Chưa có API trả về dynamic danh sách phương thức GD
- **Risk disclosure system:** Chưa có cơ chế hiển thị & xác nhận rủi ro

---

## 5. Verification Architecture (3-Layer)

```
Layer 1: Carrier API (Phase 1 — Primary)
├── GSMA Open Gateway CAMARA Number Verification API
├── GSMA Open Gateway CAMARA SIM Swap API
└── OTP fallback (khi carrier API unavailable)

Layer 2: VNeID / RAR Center C06 (Phase 2 — Stronger)
├── App-to-app flow: NHSV App → VNeID → xác thực sinh trắc
└── RAR Center trả về identity token (CCCD, fullName, dob, face)

Layer 3: eKYC (Fallback / Existing)
├── ekyc-admin — CCCD scan + face matching (Lotte)
├── FPT eContract
└── Chỉ dùng khi Layer 1 & 2 không khả thi
```

---

## 6. Phạm vi

- Phone Verification Service (Phase 1 — Carrier API + OTP fallback)
- Phone Verification Service (Phase 2 — VNeID/RAR)
- Terms & Conditions Management (versioning, acceptance tracking)
- Trading Methods & Risk Disclosure (API + admin config)

---

## 7. Dependencies

| Dep | Type | Status |
|---|---|---|
| Device Fingerprinting | ⬅️ Related | 🟡 P0 in progress |
| Session Management | ⬅️ Related | ✅ Spec done |
| Biometric System (phone change flow) | ⬅️ Optional | 🔒 Blocked GATE |
| Smart-OTP (OTP fallback) | ⬅️ Related | 🔄 Implementing |
| Carrier API Gateway (service mới) | 🆕 New service | 📋 Chưa bắt đầu |
| VNeID Integration (Phase 2) | 🔌 External | 📋 Chưa bắt đầu |

---

## 8. Research References

- **April 2025:** Viettel, VinaPhone, MobiFone signed MoU for GSMA Open Gateway adoption
- **Circular 08/2026/TT-BKHCN** (Effective April 2026): Carriers phải verify SIM với 4 fields (ID, full name, DOB, face) vs National Population DB
- **Banks integrated VNeID:** Vietcombank, TPBank, BIDV, NCB, HDBank, BVBank, PVcomBank, Nam A Bank
- **UBCK + C06 Đề án 06:** Cooperation plan đã ký cho investor identity verification via VNeID

---

## 9. Output

- ✅ [`Specifications/Service_Agreement_API_Spec.md`](./Specifications/Service_Agreement_API_Spec.md)
- ✅ [`Specifications/Service_Agreement_Spec.html`](./Specifications/Service_Agreement_Spec.html) (PM-readable)
- 📋 BE implementation plan
- 📋 Carrier API Gateway service design

---

**Document Status:** ✅ Spec done · 📋 Implementation pending
**For:** BE / FE / BA (Pháp chế)
**Next Steps:** (1) Cập nhật spec với carrier API flow → (2) PM/Legal review terms templates → (3) stt3 research vendor
