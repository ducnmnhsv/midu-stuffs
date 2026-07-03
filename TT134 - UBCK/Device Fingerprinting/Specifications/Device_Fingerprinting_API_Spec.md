# Device Fingerprinting — API Specification

**Document Type:** Technical Specification
**Version:** 1.0
**Date:** June 13, 2026
**Owner:** BE Lead

---

## 1. Mục tiêu

Tạo cơ chế **định danh thiết bị độc lập** (server-verified), không phụ thuộc hoàn toàn vào `deviceId` do client gửi lên. Dùng cho:

- **Điều 7 TT134** — Xác thực & kiểm soát truy cập: phát hiện login từ thiết bị lạ
- **Biometric device binding** — Bind biometric với fingerprint, không chỉ `deviceId`
- **Session Management** — Hiển thị danh sách thiết bị đang đăng nhập
- **Alert System** — Cảnh báo khi có thiết bị mới truy cập

---

## 2. Current System Analysis

### 2.1 Device signals hiện đang collect

| Signal | Source | Lưu ở đâu | Trust level |
|--------|--------|-----------|-------------|
| `device_id` | Client (SDK) | `t_biometric.device_id`, `t_refresh_token` | ❌ Low — client tự set |
| `platform` | Client | `t_biometric`, `t_refresh_token`, JWT | ✅ Medium |
| `osVersion` | Client | `t_biometric`, `t_refresh_token`, JWT | ✅ Medium |
| `appVersion` | Client | `t_biometric`, `t_refresh_token`, JWT | ✅ Medium |
| `macAddress` | Client | `t_biometric`, `t_refresh_token` | ❌ Low — không còn reliable (iOS 14+) |
| `sourceIp` | Server (request IP) | `t_refresh_token`, AAA process | ✅ **Trusted** |
| `deviceType` | Client | `t_refresh_token.device_type` | ✅ Medium |

### 2.2 Vấn đề hiện tại

1. `device_id` do client gửi — có thể giả mạo, không đủ tin cậy để định danh thiết bị
2. Không có fingerprint hash để so sánh giữa các lần login
3. Biometric binding dùng `deviceId` (line 140 BiometricService: `serviceBiometric.deviceId.set(request.deviceId || request.device_id || "")`)
4. WTS (macOS/Safari) không có cơ chế lấy device ID ổn định

---

## 3. Giải pháp: Device Fingerprinting

### 3.1 Kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT APP                              │
│  Collect signals:                                               │
│  • device_name + model    (hardware)                            │
│  • os_version + platform  (software)                            │
│  • app_version + build    (app)                                 │
│  • timezone + locale      (environment)                         │
│  • screen_resolution      (optional heuristic)                  │
│  • device_id (nếu có từ vendor SDK)                             │
└──────────────────────────┬──────────────────────────────────────┘
                           │ POST /api/v1/device/fingerprint/register
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    FINGERPRINT SERVICE                           │
│                                                                  │
│  Server-side:                                                   │
│  1. Generate fingerprint_hash = HMAC-SHA256(                    │
│       signals + server_salt + sourceIp_range)                   │
│  2. Store: user_id, fingerprint_hash, signals (encrypted),      │
│     first_seen, last_seen, trusted                              │
│  3. Return fingerprint_id → embed in JWT                        │
│                                                                  │
│  Verification:                                                  │
│  • On login: compare current_fingerprint vs stored fingerprints  │
│  • Match → "known device"                                       │
│  • No match → "new device" → flag + optional alert              │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Fingerprint Generation

**Input signals** (collect từ client app):

| Signal | Type | Required | Ghi chú |
|--------|------|----------|---------|
| `deviceName` | string | ✅ | Tên thiết bị (iPhone 15 Pro, Galaxy S24) |
| `deviceModel` | string | ✅ | Mã model (iPhone16,1) |
| `platform` | string | ✅ | ios / android / web |
| `osVersion` | string | ✅ | 18.0, 14.5 |
| `appVersion` | string | ✅ | 3.5.0 |
| `appBuild` | string | ✅ | 20260601 |
| `timezone` | string | ✅ | Asia/Saigon |
| `locale` | string | ✅ | vi-VN |
| `screenResolution` | string | optional | 1179x2556 (heuristic) |
| `deviceId` | string | optional | Từ vendor SDK nếu có |

**Server bổ sung:**
- `sourceIp` → network range (/24)
- `userAgent` → if WTS/macOS
- `fingerprintSalt` → server secret (rotate quarterly)

**Hash algorithm:**
```
fingerprint_hash = HMAC-SHA256(
  key: fingerprintSalt,
  message: sorted(signal_key=value...) + "|" + ip_range
)
```

### 3.3 Data Model

**Bảng `t_device_fingerprint`** (PostgreSQL hoặc MySQL mới, tách biệt với `t_biometric`):

| Field | Type | Mô tả |
|-------|------|-------|
| `fingerprint_id` | UUID PK | Primary key |
| `user_id` | FK → users.id | Chủ sở hữu |
| `fingerprint_hash` | VARCHAR(64) | HMAC-SHA256 hash |
| `signals` | JSONB | Encrypted signals payload |
| `first_seen_at` | DATETIME | Lần đầu xuất hiện |
| `last_seen_at` | DATETIME | Lần gần nhất |
| `trusted` | BOOL | Trusted device? (đã verify qua OTP) |
| `device_label` | VARCHAR(100) | User-set: "iPhone của tôi" |
| `status` | ENUM | active / inactive / untrusted |
| `ip_range` | VARCHAR(45) | /24 subnet của IP đầu tiên |
| `biometric_bound` | BOOL | Đã enroll biometric trên device này? |

**Indexes:**
- UNIQUE `(user_id, fingerprint_hash)` — tránh duplicate
- INDEX `(user_id, trusted)` — filter trusted devices
- INDEX `(fingerprint_hash)` — lookup nhanh

### 3.4 API Endpoints

#### `POST /api/v1/device/fingerprint/register`

Đăng ký fingerprint khi: login lần đầu, hoặc login với thiết bị mới.

**Request:**
```json
{
  "signals": {
    "deviceName": "iPhone 15 Pro",
    "deviceModel": "iPhone16,1",
    "platform": "ios",
    "osVersion": "18.0",
    "appVersion": "3.5.0",
    "appBuild": "20260601",
    "timezone": "Asia/Saigon",
    "locale": "vi-VN",
    "screenResolution": "1179x2556",
    "deviceId": "F47AC10B-58CC-4372-A567-0E02B2C3D479"
  },
  "label": "iPhone của tôi"
}
```

**Success Response (201):**
```json
{
  "message": "SUCCESS",
  "data": {
    "fingerprintId": "uuid-xxx",
    "isNewDevice": true,
    "requiresVerification": true
  }
}
```

**Khi fingerprint đã tồn tại (200):**
```json
{
  "message": "SUCCESS",
  "data": {
    "fingerprintId": "uuid-xxx",
    "isNewDevice": false,
    "lastSeen": "2026-06-10T08:30:00Z"
  }
}
```

#### `GET /api/v1/device/fingerprint/list`

Danh sách thiết bị của user (dùng cho màn hình quản lý phiên).

**Response:**
```json
{
  "message": "SUCCESS",
  "data": {
    "devices": [
      {
        "fingerprintId": "uuid-xxx",
        "label": "iPhone của tôi",
        "platform": "ios",
        "deviceName": "iPhone 15 Pro",
        "lastSeen": "2026-06-13T09:15:00Z",
        "trusted": true,
        "currentDevice": true
      }
    ],
    "total": 5
  }
}
```

#### `DELETE /api/v1/device/fingerprint/{fingerprintId}`

Xóa thiết bị (user chủ động untrust).

**Success:**
```json
{ "message": "SUCCESS" }
```

**Error (untether):**
```json
{
  "code": "CANNOT_UNTRUST_CURRENT_DEVICE",
  "message": "Không thể xóa thiết bị đang đăng nhập"
}
```

#### `POST /api/v1/device/fingerprint/trust`

Trust một thiết bị (sau khi verify qua OTP/biometric).

**Request:**
```json
{
  "fingerprintId": "uuid-xxx",
  "otpToken": "..."
}
```

**Success:**
```json
{ "message": "SUCCESS" }
```

#### `POST /api/v1/device/fingerprint/verify`

So sánh fingerprint hiện tại với danh sách đã lưu — dùng trong auth flow.

**Request:**
```json
{
  "signals": { "...": "..." }
}
```

**Response (known device):**
```json
{
  "message": "SUCCESS",
  "data": {
    "known": true,
    "fingerprintId": "uuid-xxx",
    "trusted": true
  }
}
```

**Response (new device):**
```json
{
  "message": "SUCCESS",
  "data": {
    "known": false,
    "requiresTrust": true,
    "suggestedLabel": "iPhone 15 Pro"
  }
}
```

### 3.5 Integration với Auth Flow

#### Login Flow mới (có fingerprint)

```
1. User gửi username + password
2. AAA verify credentials
3. AAA gọi fingerprint/verify (nếu request có signals)
   │
   ├─ Known + Trusted → tiếp tục login (no extra step)
   ├─ Known + Untrusted → check xem có cần verify lại không
   └─ New Device → gắn flag "new_device" vào response
                      → FE show modal "Xác nhận thiết bị mới"
                      → Gửi OTP → trust device → tiếp tục
4. Generate token + embed fingerprintId
```

#### JWT Payload bổ sung

```typescript
interface IAccessToken {
  // ... existing fields
  fId?: string;  // fingerprintId — device fingerprint
  fT?: boolean;  // isTrusted — device đã trust chưa
}
```

#### RefreshToken bổ sung

```typescript
// Thêm field:
fingerprintId: FieldModel<string> = this.field("fingerprint_id", this.getRow);
```

---

## 4. Security Considerations

| Item | Approach |
|------|----------|
| **Signal tampering** | Client có thể gửi signals giả → server dùng IP + range để cross-check |
| **IP thay đổi** | Chỉ IP range (/24) được dùng trong hash, không phải IP cụ thể |
| **Salt rotation** | `fingerprintSalt` rotate quarterly → device cần re-register |
| **Signal encryption** | JSON signals encrypt AES-256-GCM khi lưu, chỉ decrypt khi verify |
| **False positive** | Cùng signal nhưng IP khác → hash khác → "new device" → cần fallback |
| **False negative rate target** | < 5% cho cùng device trong 30 ngày |

---

## 5. False Positive Handling (Edge Cases)

| Case | Xử lý |
|------|-------|
| User đổi IP (di chuyển) | IP range (/24) giúp giảm false positive |
| User nâng cấp OS | OS version thay đổi → hash khác → detect as "known with changes" |
| User cập nhật app | App build change → partial match → suggest re-register |
| Cùng device, khác app (MTS vs WTS) | `platform` khác → 2 fingerprints riêng, cùng user |
| Xóa cache / reinstall | deviceId mới → fingerprint khác → OTP verify → trust lại |

---

## 6. Integration với các Feature Khác

| Feature | Integration |
|---------|-------------|
| **Biometric System** | Bind biometric với fingerprint_hash, không chỉ deviceId |
| **Session Management** | Device fingerprint list = session device list |
| **Alert System** | New device trigger → push notification "Có thiết bị mới đăng nhập" |
| **Audit Log** | Ghi fingerprint_id trong mọi audit event |
| **Risk Controls** | New device + withdrawal ≥10tr → require biometric verify |

---

## 7. Implementation Plan

### Phase 1 — MVP (Core API + Storage)

| Task | Mô tả | Effort |
|------|-------|--------|
| 1.1 | Tạo `t_device_fingerprint` table + migration | 1 day |
| 1.2 | Implement `POST /fingerprint/register` | 2 days |
| 1.3 | Implement `POST /fingerprint/verify` | 2 days |
| 1.4 | Implement `GET /fingerprint/list` | 1 day |
| 1.5 | Embed fingerprintId vào JWT (IAccessToken) | 1 day |
| 1.6 | Update RefreshToken: add `fingerprintId` | 0.5 day |
| 1.7 | Unit tests + integration tests | 2 days |

**Total Phase 1: ~9.5 days**

### Phase 2 — Trust Flow & Integration

| Task | Mô tả | Effort |
|------|-------|--------|
| 2.1 | Implement `POST /fingerprint/trust` với OTP verify | 2 days |
| 2.2 | Login flow update: fingerprint check after auth | 2 days |
| 2.3 | FE: new device modal + OTP trust screen | 3 days |
| 2.4 | FE: device management screen (list + untrust) | 2 days |
| 2.5 | BiometricService: bind với fingerprint_id | 1 day |
| 2.6 | BiometricSystem: re-enrollment flow check fingerprint | 1 day |

**Total Phase 2: ~11 days**

### Phase 3 — Advanced

| Task | Mô tả | Effort |
|------|-------|--------|
| 3.1 | Fingerprint history tracking (device changes) | 2 days |
| 3.2 | Auto-trust after N successful logins | 1 day |
| 3.3 | Salt rotation mechanism | 1 day |
| 3.4 | WTS/macOS fingerprint support | 2 days |

**Total Phase 3: ~6 days**

---

## 8. Naming (TradeX Convention)

Tuân thủ `tradex-api-naming` (Rule C1):

| TradeX endpoint | Mô tả |
|----------------|-------|
| `POST /api/v1/device/fingerprint/register` | Đăng ký fingerprint |
| `POST /api/v1/device/fingerprint/verify` | Verify fingerprint |
| `GET /api/v1/device/fingerprint/list` | Danh sách device |
| `DELETE /api/v1/device/fingerprint/{id}` | Xóa device |
| `POST /api/v1/device/fingerprint/trust` | Trust device |

**DTO naming:**
- `DeviceFingerprintRegisterRequest/Response`
- `DeviceFingerprintVerifyRequest/Response`
- `DeviceFingerprintListResponse`
- `DeviceFingerprintTrustRequest/Response`

**Table:**
- `t_device_fingerprint`

**Service:**
- `device-fingerprint-svc` (hoặc extend `aaa`)

---

## 9. References

- [Biometric System Spec](../../Biometric%20System/Specifications/Biometric_System_Spec.html) — Biometric device binding
- [Session Management Spec](../../Session%20Management/) — Device list in session UX
- [Alert System Spec](../../Alert%20System/) — New device push notification
- [Audit Log Spec](../../Audit%20Log/) — Fingerprint ID in audit events
- [AAA Service — BiometricService](../../../Knowledge/TradeX-MCP/aaa-main/src/services/BiometricService.ts) — Current deviceId usage
- [AAA Service — TokenService](../../../Knowledge/TradeX-MCP/aaa-main/src/services/TokenService.ts) — JWT structure

---

**Document Status:** ✅ Draft
**For:** BE Lead / FE Lead
**Next Steps:** Review spec → Start Phase 1 implementation
