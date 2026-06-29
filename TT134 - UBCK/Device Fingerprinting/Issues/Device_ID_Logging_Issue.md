# [TT134-P0-02] Device ID Logging — GDCK & Rút tiền

**TT134 Reference:** Điều 7 — Xác thực & kiểm soát thiết bị  
**Priority:** 🔴 P0  
**Deadline:** 28/08/2026  
**Sub-group:** Device Fingerprinting  
**Phụ thuộc C06:** Không — internal TradeX task  

---

## 1. Bối cảnh

TT134 Điều 7 yêu cầu hệ thống phải ghi nhận và lưu trữ thông tin định danh thiết bị (`deviceUniqueId`) cho các giao dịch quan trọng: giao dịch chứng khoán (GDCK) và rút tiền. Hiện tại, TradeX đã có `device_id` trong một số flow nhưng **chưa đảm bảo logging nhất quán** vào audit trail cho hai loại giao dịch này.

---

## 2. Current State

| Field | Status | Nguồn | Vấn đề |
|---|---|---|---|
| `device_id` | ✅ Có trong JWT/header | Client SDK | Client-controlled, không server-verified |
| `deviceUniqueId` | ⚠️ Partial | `t_biometric`, `t_refresh_token` | Không log vào giao dịch GDCK / rút tiền |
| `fingerprint_hash` | 📋 Planned | Device Fingerprinting spec | Chưa implement |
| `sourceIp` | ✅ Có | Server (request IP) | Đã log nhưng chưa gắn vào transaction log |

---

## 3. Scope of Work

### 3.1 BE Tasks

| # | Task | Service | Ưu tiên |
|---|---|---|---|
| BE-1 | Thêm `deviceUniqueId` vào transaction log cho GDCK (đặt lệnh, hủy lệnh) | order-service / lotte-bridge | Cao |
| BE-2 | Thêm `deviceUniqueId` vào transaction log cho rút tiền (cash withdrawal) | cash-service / lotte-bridge | Cao |
| BE-3 | Thêm `sourceIp` vào transaction log (nếu chưa có) | order-service, cash-service | Cao |
| BE-4 | Tạo/update schema: `t_transaction_log` thêm column `device_unique_id`, `source_ip` | DB migration | Cao |
| BE-5 | Validate `deviceUniqueId` từ request header — reject nếu thiếu (với flag cấu hình) | API middleware | Trung bình |
| BE-6 | Expose `deviceUniqueId` trong response audit API (nếu có) | AAA / audit service | Thấp |

### 3.2 FE Tasks

| # | Task | Screen | Ưu tiên |
|---|---|---|---|
| FE-1 | Đảm bảo `deviceUniqueId` luôn được gửi trong header `X-Device-Id` cho các API đặt lệnh | Order screens | Cao |
| FE-2 | Đảm bảo `deviceUniqueId` luôn được gửi trong header `X-Device-Id` cho API rút tiền | Withdrawal screen | Cao |
| FE-3 | Nếu không lấy được `deviceUniqueId` → show error, không cho thực hiện giao dịch | Both | Trung bình |

---

## 4. Data Schema

### 4.1 Transaction log enrichment

```sql
-- Thêm columns vào t_transaction_log (hoặc bảng tương đương)
ALTER TABLE t_transaction_log
  ADD COLUMN device_unique_id VARCHAR(128),
  ADD COLUMN source_ip        VARCHAR(45),
  ADD COLUMN device_platform  VARCHAR(20),  -- ios | android | web
  ADD COLUMN app_version      VARCHAR(20);
```

### 4.2 Request header convention

```
X-Device-Id: {deviceUniqueId}   -- required cho GDCK + withdrawal
X-Platform:  ios | android | web
X-App-Version: 2.x.x
```

---

## 5. Acceptance Criteria

```
AC-1: Mọi request đặt lệnh GDCK đều có deviceUniqueId trong transaction log
AC-2: Mọi request rút tiền đều có deviceUniqueId trong transaction log
AC-3: deviceUniqueId và sourceIp được lưu cùng với transactionId, userId, timestamp
AC-4: Audit query: có thể truy vấn "tất cả giao dịch từ deviceId X" trong 90 ngày
AC-5: Nếu request thiếu X-Device-Id header và feature flag enabled → trả 400 MISSING_DEVICE_ID
AC-6: FE không cho phép thực hiện GDCK/rút tiền khi không có deviceUniqueId
```

---

## 6. API Impact

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

## 7. Note — Phân biệt `deviceId` vs `deviceUniqueId`

| Field | Nguồn | Trust level | Dùng cho |
|---|---|---|---|
| `device_id` | Client tự tạo (UUID) | Low | Session binding hiện tại |
| `deviceUniqueId` | OS-level unique ID (IDFV iOS, Android ID) | Medium | TT134 compliance logging |
| `fingerprint_hash` | Server-computed (Phase 2) | High | Device fraud detection |

> TT134 P0 yêu cầu tối thiểu log `deviceUniqueId`. `fingerprint_hash` là Phase 2 (Device Fingerprinting spec).

---

## 8. Dependencies

| Dependency | Owner | Status |
|---|---|---|
| DB migration access | BE Lead / DBA | 📋 Chưa bắt đầu |
| FE SDK: đảm bảo `deviceUniqueId` available | FE Lead | ❓ Cần verify |
| API middleware: extract header + log | BE Lead | 📋 Chưa bắt đầu |

---

Document Status: 📋 Draft | For: BE / FE / DBA | Next Steps: BE-1/BE-2 viết migration script, FE-1/FE-2 verify SDK availability
