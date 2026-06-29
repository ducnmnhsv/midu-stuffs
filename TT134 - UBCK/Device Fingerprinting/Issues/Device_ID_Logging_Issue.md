# [TT134-P0-02] Device ID Logging — GDCK & Rút tiền

**TT134 Reference:** Điều 18 k3/4/5 — Yêu cầu phần mềm ứng dụng & nhật ký giao dịch  
**Priority:** 🔴 P0  
**Deadline:** 28/08/2026  
**Sub-group:** Device Fingerprinting  
**Phụ thuộc C06:** Không — internal TradeX task  

---

## 1. Bối cảnh

TT134 **Điều 18 khoản 3, 4, 5** yêu cầu phần mềm ứng dụng phải lưu trữ thông tin định danh thiết bị và ghi nhật ký đầy đủ cho **giao dịch chứng khoán (GDCK)** và **giao dịch rút, chuyển tiền**. Hiện tại, TradeX đã có `device_id` trong một số flow nhưng **chưa đảm bảo logging đầy đủ theo đúng format Điều 18** vào transaction log.

---

## 2. Yêu cầu TT134 — Điều 18 (văn bản chính thức)

**Điều 18 khoản 3a** — Phần mềm ứng dụng phải lưu trữ:
> *"Thông tin định danh thiết bị di động là số IMEI hoặc Serial, hoặc WLAN MAC, hoặc Android ID hoặc thông tin định danh thiết bị di động khác. **Thông tin định danh máy tính là địa chỉ MAC hoặc kết hợp các thông tin liên quan đến máy tính để định danh máy tính**"*

**Điều 18 khoản 4** — Nhật ký GDCK phải gồm đầy đủ:
> tên KH, **loại hình KH**, số TK, **thời gian nhận lệnh (ngày, giờ phút)**, mã CK, **phương thức giao dịch**, **loại lệnh**, **số lượng và giá giao dịch**, **hình thức xác thực giao dịch**, **thời gian xác thực giao dịch**, **thông tin định danh thiết bị**, **địa chỉ IP mạng**

**Điều 18 khoản 5** — Nhật ký rút, **chuyển tiền** ra khỏi tài khoản GDCK phải gồm:
> tên KH, số TK, số tiền, **tài khoản ngân hàng nhận tiền**, thời gian giao dịch, **hình thức xác thực giao dịch**, **thông tin định danh thiết bị**, **địa chỉ IP mạng**

## 3. Current State

| Field | Status | Nguồn | Vấn đề |
|---|---|---|---|
| `device_id` | ✅ Có trong JWT/header | Client SDK | Client-controlled, không server-verified |
| `deviceUniqueId` (IMEI/Serial/AndroidID) | ⚠️ Partial | `t_biometric`, `t_refresh_token` | **Chưa log vào transaction log GDCK / rút tiền** |
| `fingerprint_hash` | 📋 Planned | Device Fingerprinting spec | Chưa implement |
| `sourceIp` | ✅ Có | Server (request IP) | Có trong flow nhưng chưa log vào transaction log |
| `authMethod` (hình thức xác thực) | ❓ Chưa confirm | N/A | Điều 18 k4/k5 yêu cầu log phương thức xác thực |

---

## 4. Scope of Work

### 4.1 BE Tasks

| # | Task | Service | Ưu tiên |
|---|---|---|---|
| BE-1 | Audit và enrich nhật ký GDCK: đảm bảo đủ tất cả fields Điều 18 k4 (loại hình KH, phương thức GD, loại lệnh, SL/giá, hình thức xác thực, **thời gian xác thực**, device ID, IP) | order-service / lotte-bridge | Cao |
| BE-2 | Audit và enrich nhật ký rút tiền + **chuyển tiền**: đảm bảo đủ fields Điều 18 k5 (hình thức xác thực, device ID, IP, TK ngân hàng nhận) | cash-service / lotte-bridge | Cao |
| BE-3 | DB migration: thêm `auth_method`, `auth_time`, `device_unique_id`, `source_ip` vào `t_order_log` và `t_withdrawal_log` | DBA | Cao |
| BE-4 | Hỗ trợ device ID cho **WTS (web)**: ghi địa chỉ MAC máy tính hoặc fingerprint máy tính (Điều 18 k3a) | TradeX web service | Cao |
| BE-5 | Validate `deviceUniqueId` từ request header — reject nếu thiếu (feature flag) | API middleware | Trung bình |
| BE-6 | Expose transaction log qua audit API (nội bộ + UBCK khi có yêu cầu) | AAA / audit service | Thấp |

### 4.2 FE Tasks

| # | Task | Screen | Ưu tiên |
|---|---|---|---|
| FE-1 | Đảm bảo `deviceUniqueId` (IMEI/Serial/AndroidID) luôn gửi trong `X-Device-Id` header cho API đặt lệnh GDCK | Order screens | Cao |
| FE-2 | Đảm bảo `deviceUniqueId` luôn gửi trong `X-Device-Id` header cho API **rút tiền và chuyển tiền** | Withdrawal + Transfer screens | Cao |
| FE-3 | Nếu không lấy được `deviceUniqueId` → show error, không cho thực hiện giao dịch | Both | Trung bình |
| FE-4 | WTS (web): collect và gửi device fingerprint (MAC address hoặc browser fingerprint) cho API đặt lệnh + rút/chuyển tiền | WTS Order/Withdrawal | Cao |

---

## 5. Data Schema

### 5.1 Transaction log enrichment — per Điều 18 k3/4/5

```sql
-- Nhật ký GDCK (Điều 18 khoản 4) — full required fields
ALTER TABLE t_order_log
  ADD COLUMN customer_type    VARCHAR(20),   -- INDIVIDUAL | ORGANIZATION (loại hình KH)
  ADD COLUMN trade_method     VARCHAR(20),   -- ONLINE | PHONE (phương thức GD)
  ADD COLUMN order_type       VARCHAR(20),   -- LO | ATO | ATC | MP... (loại lệnh)
  ADD COLUMN auth_method      VARCHAR(50),   -- SMS_OTP | SOFT_OTP | BIOMETRIC | FIDO
  ADD COLUMN auth_time        TIMESTAMP,     -- thời gian xác thực giao dịch
  ADD COLUMN device_unique_id VARCHAR(128),  -- IMEI / Serial / Android ID / WLAN MAC / PC fingerprint
  ADD COLUMN source_ip        VARCHAR(45);   -- địa chỉ IP mạng

-- Nhật ký rút, chuyển tiền (Điều 18 khoản 5) — full required fields
ALTER TABLE t_withdrawal_log
  ADD COLUMN dest_bank_account VARCHAR(64),  -- tài khoản ngân hàng nhận tiền
  ADD COLUMN auth_method       VARCHAR(50),
  ADD COLUMN device_unique_id  VARCHAR(128),
  ADD COLUMN source_ip         VARCHAR(45);
-- Lưu ý: auth_time không listed trong k5 nhưng nên thêm để consistency
```

### 5.2 Request header convention

```
X-Device-Id: {deviceUniqueId}   -- required cho GDCK + withdrawal
X-Platform:  ios | android | web
X-App-Version: 2.x.x
```

---

## 6. Acceptance Criteria

```
-- Nhật ký GDCK (Điều 18 k4) --
AC-1: Log GDCK gồm đủ: tên KH, loại hình KH, số TK, thời gian nhận lệnh (ngày+giờ+phút), mã CK, phương thức GD, loại lệnh, SL+giá, hình thức xác thực, thời gian xác thực, device ID, IP
AC-2: authMethod và auth_time được log cho mọi GDCK online

-- Nhật ký rút, chuyển tiền (Điều 18 k5) --
AC-3: Log rút tiền gồm đủ: tên KH, số TK, số tiền, TK ngân hàng nhận, thời gian GD, hình thức xác thực, device ID, IP
AC-4: Log chuyển tiền nội bộ (transfer ra khỏi TK GDCK) cũng áp dụng format Điều 18 k5

-- Device ID (Điều 18 k3a) --
AC-5: Mobile: deviceUniqueId = IMEI hoặc Serial hoặc WLAN MAC hoặc Android ID
AC-6: Web (WTS): deviceId = MAC address hoặc server-computed PC fingerprint

-- General --
AC-7: Audit query: truy vấn được "tất cả giao dịch từ device X" trong 90 ngày
AC-8: Nếu thiếu X-Device-Id header và feature flag enabled → trả 400 MISSING_DEVICE_ID
AC-9: FE không cho phép thực hiện GDCK/rút/chuyển tiền khi không có deviceUniqueId
```

---

## 7. API Impact

### Header mới (required)

| Header | Bắt buộc | Mô tả |
|---|---|---|
| `X-Device-Id` | ✅ Có | Unique ID của thiết bị từ SDK |
| `X-Platform` | Recommend | `ios`, `android`, `web` |
| `X-App-Version` | Recommend | App version string |

### Endpoints bị ảnh hưởng

| Method | Endpoint | Thay đổi |
|---|---|---|
| POST | `/api/v1/orders` | Log `X-Device-Id` + `sourceIp` |
| POST | `/api/v1/orders/cancel` | Log `X-Device-Id` + `sourceIp` |
| POST | `/api/v1/cash/withdraw` | Log `X-Device-Id` + `sourceIp` |
| POST | `/api/v1/cash/withdraw/confirm` | Log `X-Device-Id` + `sourceIp` |

---

## 8. Note — Phân biệt `deviceId` vs `deviceUniqueId`

| Field | Nguồn | Trust level | Dùng cho |
|---|---|---|---|
| `device_id` | Client tự tạo (UUID) | Low | Session binding hiện tại |
| `deviceUniqueId` | OS-level unique ID (IDFV iOS, Android ID) | Medium | TT134 compliance logging |
| `fingerprint_hash` | Server-computed (Phase 2) | High | Device fraud detection |

> TT134 P0 yêu cầu tối thiểu log `deviceUniqueId`. `fingerprint_hash` là Phase 2 (Device Fingerprinting spec).

---

## 9. Core Requirements — Web Trading Channel

> Điều 18 khoản 3a quy định rõ *"Thông tin định danh máy tính là địa chỉ MAC hoặc kết hợp các thông tin liên quan đến máy tính"* — nghĩa là web trading của Core phải thu thập device ID máy tính, song song với mobile. BE-4 và FE-4 của NHSV Pro đã cover TradeX side; Core cần làm tương đương cho web trading system riêng của họ.

### 9.1 Core Tasks

| # | Yêu cầu với Core | Điều TT134 | Ưu tiên |
|---|---|---|---|
| CORE-1 | Thu thập và log **device identifier cho máy tính** khi đặt lệnh và rút/chuyển tiền qua web: ưu tiên địa chỉ MAC; nếu không lấy được MAC → sử dụng server-computed browser/PC fingerprint kết hợp (User-Agent + screen resolution + timezone + ...) | Điều 18 k3a | 🔴 Critical |
| CORE-2 | Nhật ký GDCK của Core web phải ghi đủ **tất cả fields Điều 18 k4**: tên KH, loại hình KH, số TK, thời gian nhận lệnh (ngày+giờ+phút), mã CK, phương thức GD (`WEB`), loại lệnh, SL+giá, hình thức xác thực, thời gian xác thực, device ID, IP | Điều 18 k4 | 🔴 Critical |
| CORE-3 | Nhật ký rút/chuyển tiền của Core web phải ghi đủ **fields Điều 18 k5**: tên KH, số TK, số tiền, TK ngân hàng nhận, thời gian GD, hình thức xác thực, device ID, IP | Điều 18 k5 | 🔴 Critical |
| CORE-4 | Gửi `X-Device-Id` (MAC/PC fingerprint) trong mọi API call đặt lệnh và rút/chuyển tiền từ web trading sang các service phía sau. Cho phép NHSV audit cross-channel nếu cần | Điều 18 k3a | Cao |
| CORE-5 | Nếu không thu thập được device identifier → web trading **không được phép thực hiện giao dịch** (Điều 18 k3a bắt buộc phải lưu trữ) | Điều 18 k3a | Cao |

### 9.2 Cách raise với Core

1. Gửi tham chiếu văn bản Điều 18 k3a, k4, k5 TT134 chính thức
2. Confirm Core có khả năng thu thập MAC/browser fingerprint trên web không, nếu không → thống nhất phương án fallback fingerprint
3. Yêu cầu Core chia sẻ schema nhật ký GDCK và rút/chuyển tiền hiện tại để đối chiếu với yêu cầu Điều 18
4. Deadline: **28/08/2026** — align với NHSV Pro P0

---

## 10. Dependencies (TradeX side)

| Dependency | Owner | Status |
|---|---|---|
| DB migration access | BE Lead / DBA | 📋 Chưa bắt đầu |
| FE SDK: đảm bảo `deviceUniqueId` available | FE Lead | ❓ Cần verify |
| API middleware: extract header + log | BE Lead | 📋 Chưa bắt đầu |

---

Document Status: 📋 Draft | For: BE / FE / DBA | Next Steps: BE-1/BE-2 viết migration script, FE-1/FE-2 verify SDK availability
