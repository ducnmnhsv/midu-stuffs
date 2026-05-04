# Open DR Sub Account — Derivatives Planning Documentation

**Category:** Mở tiểu khoản phái sinh (sub 80) online  
**Project:** TradeX Derivatives — NHSV Pro  
**Status:** Planning  
**Last Updated:** May 4, 2026

---

## Overview

Tài liệu nhóm nghiệp vụ cho phép **khách hàng đăng ký mở tiểu khoản phái sinh trực tuyến**, gồm: kiểm tra điều kiện tài khoản, khởi tạo và ký **hợp đồng điện tử (FPT eContract)**, xác thực **OTP email**, và **quản lý trạng thái trên admin**.  

**Phạm vi hiện tại:** Không chi tiết hóa API Lotte (tạo sub 80, mapping KV thanh toán) cho đến khi đối tác cung cấp tài liệu — xem mục Out of scope trong PRD.

---

## Quick Access

| Document | Description |
|----------|-------------|
| [PRD v2.0 — Mở tiểu khoản phái sinh online (chi tiết kỹ thuật)](./Planning/PRD_Open_Derivatives_Sub_Account_Online_v2.md) | PRD chi tiết end-to-end: FE pre-check, BE eligibility, FPT eContract, Lotte DRACC-038, OneSignal + Email |
| [PRD v1.0 — Mở tiểu khoản phái sinh online (planning)](./Planning/PRD_Open_Derivatives_Sub_Account_Online.md) | PRD khung planning ban đầu (kế thừa) |
| [BE Jira backlog — sub 80 online (BA / copy-paste Jira)](./Issues/BE_Jira_Backlog_Sub_Account_Online.md) | 12 story BE theo góc nhìn nghiệp vụ (giá trị, quy tắc, AC); chi tiết kỹ thuật tham chiếu PRD |

### Folder structure (chuẩn Derivatives)

| Folder | Purpose |
|--------|---------|
| **Planning/** | PRD, BRD — PM-friendly, không code triển khai |
| **Specifications/** | API / tích hợp chi tiết (sẽ bổ sung sau khi chốt thiết kế) |
| **Issues/** | Issue triển khai theo feature |
| **Archive/** | Tài liệu lịch sử |

`Issues/` có backlog Jira-style cho BE; `Specifications/` và `Archive/` bổ sung khi có spec kỹ thuật chi tiết hoặc khi archive.

---

## Scope tóm tắt

| In scope | Out of scope (tạm) |
|----------|---------------------|
| Kiểm tra đủ điều kiện mở sub (USR 002, EKY 007, quy tắc TKCK, trạng thái sub) | API Lotte tạo sub 80, mapping KV, thông báo sau provisioning |
| Khởi tạo HĐĐT FPT (tên tài liệu, ký ảnh, hiệu lực 90 ngày, OTP email) | Chi tiết template FPT từng field (theo file đính kèm nghiệp vụ) |
| Callback / cập nhật trạng thái ký; quản trị trên admin | Triển khai code, schema DB |

---

**Maintained By:** PM/BA  
**Related:** TradeX Knowledge — luồng FPT eContract (eKYC) là tham chiếu kiến trúc tương tự, không thay thế BRD riêng của tính năng này.
