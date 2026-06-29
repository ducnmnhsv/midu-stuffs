# TT134 / UBCK — IT Features Tracking

> Dự thảo Thông tư của Bộ Tài chính về giao dịch điện tử trên thị trường chứng khoán.
> Mục tiêu: xây dựng các tính năng IT đáp ứng yêu cầu tuân thủ.

## Cấu trúc

Mỗi folder bên dưới đại diện cho một nhóm tính năng / yêu cầu IT, được map tới các Điều trong TT134.

| Folder | Điều liên quan | Trạng thái |
|--------|----------------|------------|
| [`Biometric System/`](./Biometric%20System/) | Điều 7, 9, 10, 11 — Hệ thống sinh trắc học | 📋 Draft plan · 🔒 Blocked GATE |
| [`Device Fingerprinting/`](./Device%20Fingerprinting/) | Điều 7 — Định danh thiết bị | ✅ Spec + 🔴 P0 Issue: Device ID Logging |
| [`Session Management/`](./Session%20Management/) | Điều 7 — Kiểm soát phiên đăng nhập | ✅ MD + HTML spec (v1.1) |
| [`Order 2FA/`](./Order%202FA/) | Điều 10 — Xác thực giao dịch 2 lớp | ✅ Spec + 🔴 P0 Issues: OTP TTL, Rút tiền <10M |
| [`Alert System/`](./Alert%20System/) | Điều 8 — Hệ thống cảnh báo | 🆕 Chưa bắt đầu |
| [`Service Agreement/`](./Service%20Agreement/) | Điều 5.4 — Hợp đồng/Điều khoản dịch vụ | ✅ MD + HTML spec (v1.0) |
| [`Audit Log/`](./Audit%20Log/) | Điều 5, 10 — Audit trail | 🆕 Chưa bắt đầu |
| [`Data Security/`](./Data%20Security/) | Điều 9 — Mã hóa & che dấu dữ liệu | 🆕 Chưa bắt đầu |
| [`Risk Controls/`](./Risk%20Controls/) | Điều 11 — Quản lý rủi ro & hạn mức | 🆕 Chưa bắt đầu |
| [`Compliance Reporting/`](./Compliance%20Reporting/) | Điều 13 — Báo cáo tuân thủ & lưu trữ | 🆕 Chưa bắt đầu |

## References

- [Smart-OTP (root project)](../Smart-OTP/) — TOTP-based authentication feature, core cho Order 2FA
- [Knowledge/TradeX System](../Knowledge/TradeX/System/) — Production system documentation
- [Knowledge/TradeX-MCP/aaa-main](../Knowledge/TradeX-MCP/aaa-main/) — AAA Service (auth, token, biometric)

## Spec Format

Mỗi feature có:
- **Markdown spec** (`.md`) — chi tiết kỹ thuật cho BE/FE
- **HTML spec** (`.html`) — PM-readable, effective HTML style, dark mode, flow overview, BE/FE responsibility grid, UX principles

## Kanban & Issue Tracking

- **[TT134_Kanban.html](./TT134_Kanban.html)** — Kanban board toàn bộ 12 items, filter theo Priority, modal detail, dark mode

### P0 Issues (Bắt đầu ngay — không cần chờ GATE)

| Issue | Sub-group | Deadline |
|---|---|---|
| [TT134-P0-01: OTP TTL Compliance](./Order%202FA/Issues/OTP_TTL_Compliance_Issue.md) | Order 2FA | 🔴 14/08/2026 |
| [TT134-P0-02: Device ID Logging](./Device%20Fingerprinting/Issues/Device_ID_Logging_Issue.md) | Device Fingerprinting | 🔴 28/08/2026 |
| [TT134-P0-03: Rút tiền <10M Smart OTP](./Order%202FA/Issues/Withdraw_Under10M_SmartOTP_Issue.md) | Order 2FA | 🔴 28/08/2026 |

**Document Status:** ✅ Active
**For:** PM / BA / Dev
**Next Steps:** Kickoff P0 issues ngay · BOM GATE decision 30/06 để unblock P2 items
