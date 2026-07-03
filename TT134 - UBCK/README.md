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

> 📋 **Status / deadline / dependency: xem board trung tâm [`Tracking/kanban.html`](../Tracking/kanban.html)** — nguồn duy nhất là `Tracking/tasks.js` (rule C7 CLAUDE.md). Filter Area = TT134.
> README này KHÔNG maintain status nữa. Bảng dưới chỉ là **index doc theo ID**.

| ID (board) | Title | Doc |
|---|---|---|
| `TT134-STT7` | **TT134-P0-01** OTP TTL Compliance | [Order 2FA](./Order%202FA/Issues/OTP_TTL_Compliance_Issue.md) |
| `TT134-STT35` | **TT134-P0-02** Device ID Logging | [Device Fingerprinting](./Device%20Fingerprinting/Issues/Device_ID_Logging_Issue.md) |
| `TT134-STT14A` | **TT134-P0-03** Rút tiền &lt;10M — **scope merged vào STT5** | [Order 2FA](./Order%202FA/Issues/Withdraw_Under10M_SmartOTP_Issue.md) |
| `TT134-BE7` | **BE-7** Smart OTP Biometric Activation (Điều 8.5b) | [Order 2FA](./Order%202FA/Issues/BE7_SmartOTP_Biometric_Activation.md) |
| `TT134-STT5` | **TT134-P0-04** Session Auth — Login Smart OTP = sAm/sAt embedded | [Order 2FA/Spec](./Order%202FA/Specifications/Order_2FA_Integration_Spec.md) |
| `TT134-STT36` | Mobile security: 1 device/TK + anti-tampering | [Device Fingerprinting](./Device%20Fingerprinting/) |
| `TT134-STT8` | Sinh trắc học FIDO (FAR &lt;0.01%, FRR &lt;5%, PAD) | [Biometric System](./Biometric%20System/) |
| `TT134-STT10` | Biometric rules: lock ≤10, timeout 3p, verify CSDL QG | [Biometric System](./Biometric%20System/) |
| `TT134-STT12` | GDCK online: biometric đầu/phiên | [Biometric System](./Biometric%20System/) |
| `TT134-STT14B` | Rút tiền ≥10M: biometric bắt buộc | [Biometric System](./Biometric%20System/) |
| `TT134-STT40` | Lưu trữ thông tin sinh trắc học KH | [Data Security](./Data%20Security/) |
| `TT134-STT3` | Xác minh SĐT thuộc quyền sử dụng hợp pháp KH | [Service Agreement](./Service%20Agreement/) |
| `TT134-GATE` | **★ GATE** BOM Decision: C06 vs VNPT | (BOM/IT) |
| `TT134-STT9` | PAD Certification | [Biometric System](./Biometric%20System/) |

**Lưu ý sync:** Khi update status/deadline → chỉ sửa `Tracking/tasks.js`. `TT134_Kanban.html` cũ đã **deprecated** — giữ lại để tra task breakdown chi tiết (BE/FE/Core tasks per card), không update status ở đó nữa.

---

## 3. Dependency Map

```
🔌 EXTERNAL                  🔴 P0 ACTIVE                                  🟡 P1 PLAN              🟢 P2 / 🔒 BLOCKED
─────────────────────        ──────────────────────                        ───────────────         ──────────────────────
Smart OTP go-live ─────────► stt7 (P0-01 OTP TTL) ───────────────────────► be7 (S-OTP Biometric)
                  └────────► stt5 (P0-04 Session Auth) ←─ merges stt14a
                                                           │ [Phase 3 done]
AAA biometric flag ────────► be7                           └─────────────────► Session Mgmt (P1)
                                                                             └► Alert System (P1)
(internal) ────────────────► stt35 (P0-02 Device ID) ─────────────────────► Audit Log (P1) ──► Compliance Report (P2)
                                                     │                                    └───► Data Security (P2)
                             ├─ STT5 + STT35 share ─┘
                             │  DB migration (1 script)
                             └─ Order/Withdrawal logging middleware (1 middleware)

stt7 (OTP TTL) ────────────► be7

★ BOM GATE ────────────────────────────────────────────────────────────────────────────────────► Biometric System
                                                                                               ► stt8 · stt10 · stt12 · stt14b · stt40
```

**Resolved by STT5 + STT35:**
- ✅ `stt14a` — merged, không implement riêng
- ✅ Double OTP trong order flow — FE-SA-4 removes OTP from ConfirmOrdersScreen
- ✅ `POST /api/v1/session/auth` — loại bỏ, không build

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

## 7. Cross-issue Implementation Rules

> ⚠️ **Đọc section này trước khi viết bất kỳ issue/spec/task TT134 mới** — tránh tạo duplicate code, duplicate migration, hoặc assign công việc đã được cover bởi issue khác.

### 7.1 DB Migration — 1 script duy nhất

STT5 và STT35 đều modify cùng tables `t_order_log` + `t_withdrawal_log`.

| STT5 thêm | STT35 thêm | Cả hai tables |
|---|---|---|
| `auth_method` | `device_unique_id` | `t_order_log` |
| `auth_time` | `source_ip` | `t_withdrawal_log` |
| — | `dest_bank_account` | `t_withdrawal_log` only |

**Rule:** Gộp thành **1 migration script** duy nhất. Không tạo 2 PR migration riêng. Tên gợi ý: `V{n}__tt134_auth_device_logging.sql`.

### 7.2 Logging Middleware — Không tạo 2 middleware

| Issue | Task | Endpoint | Ghi vào |
|---|---|---|---|
| STT5 | BE-SA-3 | POST /equity/order, POST /derivatives/order, PUT /orders/cancel | `t_order_log.auth_method`, `auth_time` |
| STT35 | BE-1 | Cùng endpoints | `t_order_log.device_unique_id`, `source_ip` |
| STT5 | BE-SA-4 | POST /cash/withdraw/confirm (&lt;10M) | `t_withdrawal_log.auth_method`, `auth_time` |
| STT35 | BE-2 | Cùng endpoint | `t_withdrawal_log.device_unique_id`, `source_ip` |

**Rule:** Implement **1 middleware layer** xử lý tất cả 4 fields cùng lúc cho mỗi endpoint group. Không tách thành 2 middleware STT5 + STT35 riêng.

### 7.3 STT14a — Đã merged, không assign developer riêng

`stt14a` (Rút tiền <10M Smart OTP) đã được merge hoàn toàn vào `stt5` (Session Auth):
- BE: STT5 BE-SA-4 tự động log auth_method/auth_time cho withdrawal <10M
- FE: WithdrawalScreen <10M gọi thẳng `/cash/withdraw/confirm` không cần OTP screen
- CORE: Web trading covered by session auth đầu phiên

**Rule:** **Đóng/close STT14a ticket** khi STT5 done. Không assign BE/FE developer riêng cho STT14a.

### 7.4 BE-SA-1 = Smart OTP Login Task 4 — Không duplicate

STT5 BE-SA-1 (embed sAm/sAt tại verifyOTP) là phần mở rộng trực tiếp của **07_BE_Task Task 4** (verifyOTP routing to Smart OTP).

**Rule:** Coordinate với Smart OTP team. Merge BE-SA-1 logic vào cùng PR của **07_BE_Task**. Không tạo PR riêng cho STT5 BE-SA-1.

### 7.5 Unblocking Sequence — Thứ tự mở issue P1/P2

Các issue P1/P2 sau đây **chỉ nên bắt đầu spec/implementation sau khi STT5 + STT35 Phase 3 hoàn thành**:

| Issue P1/P2 | Phụ thuộc | Lý do chờ |
|---|---|---|
| Audit Log (Điều 5/10) | STT5 Phase 3 + STT35 | Cần auth_method + auth_time + device_unique_id trong logs |
| Alert System (Điều 8) | STT35 | Cần device_unique_id để correlation per-device |
| Session Management (Điều 7) | STT5 | sAt trong JWT cung cấp session auth context |
| Compliance Reporting (Điều 13) | Audit Log done | Cần full audit trail trước |

**Rule:** Không viết spec chi tiết cho Audit Log, Alert System, Session Management khi STT5 + STT35 chưa xong Phase 3.

### 7.6 Checklist khi tạo issue TT134 mới

Trước khi viết issue, xác nhận:

- [ ] Issue này có modify `t_order_log` hoặc `t_withdrawal_log` không? → Nếu có, merge migration với STT5+STT35
- [ ] Issue này có liên quan đến order logging pipeline không? → Nếu có, dùng middleware đã có, không tạo mới
- [ ] Issue này có bị cover bởi Session Auth (STT5) hoặc Device Logging (STT35) không? → Nếu có, đóng/merge thay vì implement riêng
- [ ] Issue này phụ thuộc vào auth_method/auth_time/device_unique_id trong logs không? → Nếu có, chờ STT5+STT35 Phase 3

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
