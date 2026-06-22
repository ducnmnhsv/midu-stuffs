# TT134 / UBCK — IT Features Tracking

> Dự thảo Thông tư của Bộ Tài chính về giao dịch điện tử trên thị trường chứng khoán.
> Mục tiêu: xây dựng các tính năng IT đáp ứng yêu cầu tuân thủ.

## Cấu trúc

Mỗi folder bên dưới đại diện cho một nhóm tính năng / yêu cầu IT, được map tới các Điều trong TT134.

| Folder | Điều liên quan | Trạng thái |
|--------|----------------|------------|
| [`Biometric System/`](./Biometric%20System/) | Điều 7, 9, 10, 11 — Hệ thống sinh trắc học (enrollment, verification, liveness, lockout) | 📋 Draft plan |
| [`Device Fingerprinting/`](./Device%20Fingerprinting/) | Điều 7 — Định danh thiết bị | ✅ MD + HTML spec (v2 — BE/FE/UX) |
| [`Session Management/`](./Session%20Management/) | Điều 7 — Kiểm soát phiên đăng nhập | ✅ MD + HTML spec (v1.1 — BE/FE/UX) |
| [`Order 2FA/`](./Order%202FA/) | Điều 10 — Xác thực giao dịch 2 lớp | ✅ MD + HTML spec (v1.1 — BE/FE/UX) |
| [`Alert System/`](./Alert%20System/) | Điều 8 — Hệ thống cảnh báo | 🆕 Chưa bắt đầu |
| [`Service Agreement/`](./Service%20Agreement/) | Điều 5.4 — Hợp đồng/Điều khoản dịch vụ | ✅ MD + HTML spec (v1.0 — BE/FE/BA) |
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

**Document Status:** ✅ Active
**For:** PM / BA / Dev
**Next Steps:** P1 spec creation (Alert System, Audit Log, Risk Controls) — Service Agreement feature added
