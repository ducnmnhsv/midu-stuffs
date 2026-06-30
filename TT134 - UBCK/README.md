# TT134 / UBCK — IT Features Tracking

> Dự thảo Thông tư của Bộ Tài chính về **giao dịch điện tử trên thị trường chứng khoán**.
> Mục tiêu: xây dựng các tính năng IT đáp ứng yêu cầu tuân thủ cho **NHSV Pro**.

📋 **Kanban + Dependency Graph:** [`TT134_Kanban.html`](./TT134_Kanban.html)

---

## 1. Cấu trúc thư mục

```
TT134 - UBCK/
├── README.md                    ← bạn đang đọc (master tracking)
├── TT134_Kanban.html            ← Kanban board + Dep graph interactive
├── _Reference/                  ← Văn bản pháp lý gốc (docx, xlsx)
│
├── Order 2FA/                   ← Điều 8, 10, 12 — Xác thực giao dịch
│   ├── README.md
│   ├── Specifications/          ← .md API spec + .html PM-readable
│   └── Issues/                  ← P0/P1 issues (1 file = 1 issue)
│
├── Device Fingerprinting/       ← Điều 7, 18 — Định danh thiết bị
│   ├── README.md
│   ├── Specifications/
│   └── Issues/
│
├── Session Management/          ← Điều 7 — Kiểm soát phiên
│   ├── README.md
│   └── Specifications/
│
├── Service Agreement/           ← Điều 5.4 — Hợp đồng dịch vụ
│   ├── README.md
│   └── Specifications/
│
├── Biometric System/            ← Điều 7, 9, 10, 11 — Sinh trắc học (🔒 GATE)
│   ├── README.md (TBD)
│   └── Specifications/
│
├── Alert System/                ← Điều 8 — Cảnh báo (chưa bắt đầu)
├── Audit Log/                   ← Điều 5, 10 — Audit trail (chưa bắt đầu)
├── Risk Controls/               ← Điều 11 — Quản lý rủi ro (chưa bắt đầu)
├── Compliance Reporting/        ← Điều 13 — Báo cáo (chưa bắt đầu)
└── Data Security/               ← Điều 9 — Mã hóa & masking (chưa bắt đầu)
```

**Quy ước:**
- Mỗi feature folder = 1 nhóm yêu cầu IT theo Điều TT134
- `Specifications/` = spec kỹ thuật (`.md` cho BE/FE) + spec PM-readable (`.html`)
- `Issues/` = 1 file `.md` cho mỗi issue cụ thể (P0/P1/BE-N…)
- File naming: `PascalCase_Underscore.{md,html}` (vd: `Device_ID_Logging_Issue.md`)

---

## 2. Master Tracking — Issues

ID = card ID trong Kanban. Status sync với Kanban CARDS data.

| ID | Title | Pri | Status | Deadline | Folder | Blocks | Blocked by |
|---|---|---|---|---|---|---|---|
| `stt7` | **TT134-P0-01** OTP TTL Compliance | 🔴 P0 | 🟡 Ready | 14/08/2026 | [Order 2FA](./Order%202FA/Issues/OTP_TTL_Compliance_Issue.md) | stt14a · be7 | — |
| `stt35` | **TT134-P0-02** Device ID Logging | 🔴 P0 | 🟡 Ready | 28/08/2026 | [Device Fingerprinting](./Device%20Fingerprinting/Issues/Device_ID_Logging_Issue.md) | session · alert · audit | — |
| `stt14a` | **TT134-P0-03** Rút tiền &lt;10M Smart OTP | 🔴 P0 | 🟡 Ready | 28/08/2026 | [Order 2FA](./Order%202FA/Issues/Withdraw_Under10M_SmartOTP_Issue.md) | — | Smart OTP go-live · Core purpose field · stt7 |
| `be7` | **BE-7** Smart OTP Biometric Activation (Điều 8.5b) | 🔴 P0 | 📋 Draft | 14/08/2026 | [Order 2FA](./Order%202FA/Issues/BE7_SmartOTP_Biometric_Activation.md) | — | AAA biometric flag · FE RSA Keystore · stt7 |
| `stt5` | Smart OTP giao dịch đầu/phiên | 🟡 P1 | 📦 Backlog | 16/10/2026 | [Order 2FA](./Order%202FA/) | — | Smart OTP go-live |
| `stt36` | Mobile security: 1 device/TK + anti-tampering | 🟡 P1 | 📦 Backlog | 30/10/2026 | [Device Fingerprinting](./Device%20Fingerprinting/) | — | — |
| `stt8` | Sinh trắc học FIDO (FAR &lt;0.01%, FRR &lt;5%, PAD) | 🟢 P2 | 🔒 Blocked GATE | 30/10/2026 | [Biometric System](./Biometric%20System/) | — | BOM GATE |
| `stt10` | Biometric rules: lock ≤10, timeout 3p, verify CSDL QG | 🟢 P2 | 🔒 Blocked GATE | TBD | [Biometric System](./Biometric%20System/) | — | BOM GATE |
| `stt12` | GDCK online: biometric đầu/phiên | 🟢 P2 | 🔒 Blocked GATE | 30/10/2026 | [Biometric System](./Biometric%20System/) | — | BOM GATE |
| `stt14b` | Rút tiền ≥10M: biometric bắt buộc | 🟢 P2 | 🔒 Blocked GATE | TBD | [Biometric System](./Biometric%20System/) | — | BOM GATE |
| `stt40` | Lưu trữ thông tin sinh trắc học KH | ⚪ P3 | 🔒 Blocked GATE | TBD | [Data Security](./Data%20Security/) | — | BOM GATE |
| `stt3` | Xác minh SĐT thuộc quyền sử dụng hợp pháp KH | ⚪ P3 | 🔍 Research | TBD | [Service Agreement](./Service%20Agreement/) | — | Vendor TBD |
| `gate` | **★ GATE** BOM Decision: C06 vs VNPT | 🟣 GATE | ⏳ Pending | 30/06/2026 | (BOM/IT) | stt8 · stt10 · stt12 · stt14b · stt40 | — |
| `stt9` | PAD Certification | ✅ DONE | ✅ Done | — | [Biometric System](./Biometric%20System/) | — | — |

**Lưu ý sync:** Khi update status/deadline → update **đồng thời** ở README này + Kanban `CARDS` object trong `TT134_Kanban.html`.

---

## 3. Dependency Map

```
🔌 EXTERNAL                  🔴 P0 ACTIVE                  🟡 P1 PLAN              🟢 P2 / 🔒 BLOCKED
─────────────────────        ──────────────────────        ───────────────         ──────────────────────
Smart OTP go-live ─────────► stt7 (P0-01 OTP TTL)
                  └────────► stt14a (P0-03 Rút <10M)
                  └────────► stt5 (Smart OTP đầu/phiên)

AAA biometric flag ────────► be7 (S-OTP Biometric Activation)

(internal) ────────────────► stt35 (P0-02 Device ID) ────► Session Mgmt
                                                     └───► Alert System
                                                     └───► Audit Log ─────────► Compliance Report
                                                                          └───► Data Security

stt7 (OTP TTL) ────────────► stt14a · be7

★ BOM GATE (30/06) ────────────────────────────────────────────────────────────► Biometric System
                                                                              ► stt8 · stt10 · stt12 · stt14b · stt40
```

🔗 **Interactive version:** mở tab "Dependency Graph" trong [`TT134_Kanban.html`](./TT134_Kanban.html)

---

## 4. Feature Folders

| Folder | Điều TT134 | Trạng thái | Issue count |
|---|---|---|---|
| [`Order 2FA/`](./Order%202FA/) | 8, 10, 12 — Xác thực giao dịch | ✅ Spec + 🔴 3 P0 issues | 3 |
| [`Device Fingerprinting/`](./Device%20Fingerprinting/) | 7, 18 — Định danh thiết bị | ✅ Spec + 🔴 1 P0 issue | 1 |
| [`Session Management/`](./Session%20Management/) | 7 — Kiểm soát phiên | ✅ Spec | 0 |
| [`Service Agreement/`](./Service%20Agreement/) | 5.4 — Hợp đồng dịch vụ | ✅ Spec | 0 |
| [`Biometric System/`](./Biometric%20System/) | 7, 9, 10, 11 — Sinh trắc học | 📋 Draft · 🔒 GATE | 0 |
| [`Alert System/`](./Alert%20System/) | 8 — Cảnh báo | 🆕 README only | 0 |
| [`Audit Log/`](./Audit%20Log/) | 5, 10 — Audit trail | 🆕 README only | 0 |
| [`Risk Controls/`](./Risk%20Controls/) | 11 — Quản lý rủi ro | 🆕 README only | 0 |
| [`Compliance Reporting/`](./Compliance%20Reporting/) | 13 — Báo cáo | 🆕 README only | 0 |
| [`Data Security/`](./Data%20Security/) | 9 — Mã hóa & masking | 🆕 README only | 0 |

---

## 5. References

### Cross-project

- [`../Smart-OTP/`](../Smart-OTP/) — TOTP-based authentication (core cho Order 2FA)
- [`../Knowledge/TradeX/System/`](../Knowledge/TradeX/System/) — Production system documentation
- [`../Knowledge/TradeX-MCP/aaa-main/`](../Knowledge/TradeX-MCP/aaa-main/) — AAA Service (auth, token, biometric)

### Văn bản pháp lý gốc

- [`_Reference/1. Du thao_시행규칙 초안.docx`](./_Reference/) — Dự thảo TT134 chính thức
- [`_Reference/Checklist_tuân_thủ_134_준수_체크리스트_NHSV.xlsx`](./_Reference/) — Checklist tuân thủ (NHSV review)

---

## 6. Quy ước tài liệu

| Convention | Quy tắc |
|---|---|
| **Folder name** | Title Case với space (giữ legacy, có URL encoding `%20`) |
| **File name** | `PascalCase_Underscore.{md,html}` (vd: `Device_ID_Logging_Issue.md`) |
| **Markdown** | CommonMark strict · ATX headers (`#`) · fenced code blocks có language ID |
| **Spec format** | Mỗi feature có cặp `.md` (BE/FE detail) + `.html` (PM-readable, dark mode) |
| **Issue file** | Một file = một issue. Đặt trong `<Feature>/Issues/`. ID khớp với Kanban card |
| **Footer** | Mọi spec/issue phải kết thúc bằng: `Document Status: ✅/📋/🔄 \| For: [audience] \| Next Steps: [action]` |

---

**Document Status:** ✅ Active
**For:** PM / BA / BE / FE / DevOps / QA
**Next Steps:** (1) Kickoff 3 P0 issues ngay → (2) Confirm BE-7 dependencies → (3) BOM GATE decision 30/06 để unblock 5 P2 items
