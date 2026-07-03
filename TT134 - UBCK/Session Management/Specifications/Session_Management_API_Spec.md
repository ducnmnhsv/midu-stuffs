# Session Management — API Specification

**Feature:** Session Management (Quản lý phiên đăng nhập)
**TT134 Reference:** Điều 5 (Xác thực), Điều 6 (Đăng nhập), Điều 13 (Giám sát)
**Priority:** P0
**Version:** 1.0
**Status:** Draft

---

## 0. Flow Overview

```
[Login] → [Session limit check] → [Allow/Reject]
                                         │
                              [Device List Screen]
                              → Xem thiết bị đang login
                              → Revoke thiết bị cũ
                              → Revoke all
                                         │
                              [Session timeout]
                              → Cảnh báo T-5 phút
                              → Extend session
                              → Expire → LoginScreen
```

| Bước | Mô tả | BE/FE |
|------|-------|-------|
| 1 | Login → BE check concurrent session limit (max 3) | BE + FE |
| 2 | User xem danh sách thiết bị trong Settings | FE |
| 3 | User revoke session từ xa → soft-delete token | BE + FE |
| 4 | Cảnh báo session timeout T-5 phút, extend session | FE |

## 0a. UX Principles

| Principle | Mô tả |
|-----------|-------|
| **Minh bạch** | Luôn hiển thị số thiết bị đang login, thời gian session còn lại |
| **Không silent reject** | Khi đạt limit, từ chối login với thông báo rõ ràng + danh sách thiết bị |
| **Cảnh báo trước** | Cảnh báo T-5 phút trước khi session hết hạn, có nút kéo dài |

## 0b. BE / FE Responsibility Split

| Component | Backend (AAA) | Frontend (App) |
|-----------|---------------|----------------|
| Session limit | enforceSessionLimit() trong generateToken() | Hiển thị warning khi gần đầy |
| Device list | GET /api/v1/sessions | DeviceListScreen |
| Revoke session | DELETE /api/v1/sessions/:id | Swipe + confirm UI |
| Revoke all | DELETE /api/v1/sessions | Button + confirm modal |
| Timeout info | GET /api/v1/sessions/timeout | Banner T-5 phút, extend button |
| Admin revoke | DELETE /api/v1/admin/sessions/:id | Admin UI (Phase 2) |

---

## 1. Overview

### 1.1 Problem

Current system cho phép **unlimited concurrent sessions** — user có thể login trên nhiều thiết bị cùng lúc, mỗi lần login tạo một `t_refresh_token` mới. Không có:

- Giới hạn số thiết bị được login đồng thời
- UI để user xem và quản lý các thiết bị đang login
- Thông báo khi có thiết bị mới login (cross-cutting — tham chiếu Alert System)
- Cơ chế force logout từ xa (hiện tại `revokeToken` cần biết `refresh_token_id` cụ thể)
- Thời gian session timeout không được hiển thị rõ cho user

### 1.2 Goals

| # | Goal | TT134 Ref |
|---|------|-----------|
| G1 | Giới hạn concurrent sessions (tối đa N thiết bị/user) | Điều 5.2, 6.1 |
| G2 | API quản lý sessions: list, revoke 1 session, revoke all | Điều 13.2 |
| G3 | Session timeout policy hiển thị rõ cho user | Điều 6.3 |
| G4 | Thông báo realtime khi new device login | Điều 13.4 |
| G5 | Force revoke session từ xa (admin) | Điều 13.5 |
| G6 | Device history: xem lịch sử login/logout gần đây | Điều 13.2 |

### 1.3 Non-Goals

- ❌ Session recording (logs) — thuộc Audit Log spec (P1)
- ❌ Realtime notification channel — thuộc Alert System spec (P1)
- ❌ Device fingerprinting details — thuộc Device Fingerprinting spec
- ❌ Biometric authentication — thuộc Biometric System spec

---

## 2. Current State Analysis

### 2.1 Existing Session Model

```
t_refresh_token
├── id                          (PK, auto-increment)
├── client_id                   (FK → t_client)
├── user_id                     (FK → t_user)
├── service_user_id
├── login_method_id
├── token                       (UUID v4 — the refresh token)
├── extend_data                 (JSON: sgIds, conId, sc, su, ud, pl, gt, osV, appV, rTtl, aTtl, sId)
├── source_ip
├── device_type                 (ANDROID / IOS / WEB)
├── expired_at                  (default: now + 86400s = 24h)
├── parent_id                   (self-ref FK — token chaining on refresh)
├── mac_address
├── platform                    (e.g., "iOS", "Android")
├── os_version
├── app_version
├── created_at
└── updated_at
```

**Key observations:**
- **No session limit**: User có thể có N `t_refresh_token` records active
- **No `is_active` flag**: Active session = not expired token
- **No `last_active_at`**: Không biết session gần nhất dùng lúc nào
- **No `device_name`**: Chỉ có deviceType, platform, osVersion
- **revokeToken**: Xóa record khỏi DB → không track history
- **parentId**: tracking refresh chain nhưng không dùng cho session management

### 2.2 Authentication Flow

```
Login → generateToken() → createRefreshToken() → insert t_refresh_token
                                                      ↓
API call → auth middleware → verify JWT → check token hợp lệ
                                                      ↓
Refresh → refreshAccessToken() → tạo accessToken mới từ refreshToken cũ
                                                      ↓
Logout → revokeToken() → deleteRefreshToken() → xóa record
```

### 2.3 Gaps vs TT134

| Requirement | Current State | Gap |
|-------------|---------------|-----|
| Giới hạn concurrent devices (Điều 5.2) | No limit | ❌ |
| User thấy danh sách device đang active (Điều 13.2) | No UI, no API | ❌ |
| Logout từ xa (Điều 13.5) | revokeToken cần token string | ❌ |
| Hiển thị thời gian session timeout (Điều 6.3) | No display | ❌ |
| Thông báo new device login (Điều 13.4) | No notification | ❌ |

---

## 3. Proposed Architecture

### 3.1 Data Model Extension

Add new column(s) to `t_refresh_token`:

```sql
ALTER TABLE t_refresh_token
  ADD COLUMN device_name    VARCHAR(255)  NULL AFTER device_type,
  ADD COLUMN last_active_at DATETIME      NULL AFTER expired_at,
  ADD COLUMN is_revoked     TINYINT(1)   DEFAULT 0 AFTER parent_id,
  ADD INDEX idx_user_active (user_id, expired_at, is_revoked);
```

**Notes:**
- `device_name`: User-friendly name (e.g., "iPhone 15 Pro", "Samsung Galaxy S24")
- `last_active_at`: Updated mỗi khi access token được refresh
- `is_revoked`: Soft-delete flag — không xóa record để giữ lịch sử

### 3.2 Configuration

```javascript
// Default config (in env.js or conf)
conf.session = {
  maxConcurrentSessions: {
    DEFAULT: 3,    // User thường
    VIP: 5,        // User VIP
    ADMIN: 10,     // Admin
  },
  warningThreshold: 1,  // Cảnh báo khi còn 1 slot trống
  deviceHistoryDays: 90 // Lưu lịch sử thiết bị
};
```

### 3.3 Concurrent Session Enforcement

```
Login Request
    │
    ├─ Count active sessions (not expired, not revoked) for user_id
    │
    ├─ If count < maxConcurrentSessions[userTier] → ALLOW (create new token)
    │
    └─ If count >= maxConcurrentSessions[userTier] → REJECT with:
         - Error code: MAX_SESSIONS_REACHED
         - Message: "Bạn đã đăng nhập trên tối đa {N} thiết bị.
                     Vui lòng đăng xuất thiết bị khác trước khi đăng nhập."
         - List current active sessions (truncated, no token data)
```

**Enforcement Point:** Trong `generateToken()` hoặc `createRefreshToken()`, trước khi insert token mới.

**Edge Cases:**
- **Refresh token refresh** (khi access token hết hạn) → KHÔNG đếm là session mới, không trigger limit check
- **Biometric re-login** → KHÔNG đếm là session mới (cùng device)
- **VIP user** → maxSessions config riêng, lấy từ user profile scope

---

## 4. API Endpoints

### 4.1 List Active Sessions

```
GET /api/v1/sessions
Authorization: Bearer <access_token>
```

**Response:** `ISessionListRes`

```json
{
  "sessions": [
    {
      "sessionId": 12345,
      "deviceType": "IOS",
      "deviceName": "iPhone 15 Pro",
      "platform": "iOS",
      "osVersion": "18.2",
      "appVersion": "3.12.0",
      "ipAddress": "192.168.1.100",
      "loginMethod": "password_otp",
      "loggedInAt": "2026-06-13T08:30:00+07:00",
      "lastActiveAt": "2026-06-13T14:15:00+07:00",
      "expiresAt": "2026-06-14T08:30:00+07:00",
      "isCurrent": true
    }
  ],
  "maxSessions": 3,
  "remainingSlots": 2
}
```

**Success Codes:**
- `200 OK` — danh sách sessions

**Business Rules:**
- Chỉ trả về sessions chưa expired và chưa revoked
- `isCurrent: true` cho session hiện tại (match `refresh_token_id` từ access token)
- `loggedInAt` = `created_at` của refresh token
- `lastActiveAt` = thời điểm refresh access token gần nhất
- Không expose refresh token string hay access token data
- Sắp xếp: current session đầu tiên, sau đó theo `lastActiveAt` DESC

---

### 4.2 Revoke Specific Session

```
DELETE /api/v1/sessions/:sessionId
Authorization: Bearer <access_token>
```

**Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| `sessionId` | number | ID của session cần revoke (từ list API) |

**Response:**
```json
{
  "status": "revoked",
  "sessionId": 12345
}
```

**Success Codes:**
- `200 OK` — revoked thành công

**Error Codes:**
- `400 INVALID_SESSION` — sessionId không tồn tại hoặc đã expired
- `403 CANNOT_REVOKE_CURRENT` — không thể revoke session hiện tại
- `403 UNAUTHORIZED` — session không thuộc user này

**Business Rules:**
- Soft-delete: set `is_revoked = 1`, không xóa record
- KHÔNG cho phép revoke session hiện tại (phải dùng logout)
- Nếu session đã expired → return `INVALID_SESSION` (không cần revoke)
- Log event `SESSION_REVOKED` cho Audit Log
- Nếu device có FCM/APNS token → gửi notification "Thiết bị [name] đã bị đăng xuất từ xa"

---

### 4.3 Revoke All Other Sessions

```
DELETE /api/v1/sessions
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "confirm": true,
  "reason": "LOGOUT_ALL_DEVICES"
}
```

**Response:**
```json
{
  "status": "revoked",
  "revokedCount": 3,
  "keptSessionId": 12345
}
```

**Success Codes:**
- `200 OK` — revoked tất cả sessions khác

**Error Codes:**
- `400 INVALID_CONFIRMATION` — missing `confirm: true`
- `400 NO_OTHER_SESSIONS` — không có session nào khác để revoke

---

### 4.4 Get Session Timeout Info

```
GET /api/v1/sessions/timeout
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "accessTokenTtl": 900,
  "refreshTokenTtl": 86400,
  "accessTokenExpiresAt": "2026-06-13T14:30:00+07:00",
  "sessionExpiresAt": "2026-06-14T08:30:00+07:00",
  "sessionDurationMinutes": 1440,
  "remainingMinutes": 1020
}
```

**Success Codes:**
- `200 OK`

**Business Rules:**
- `sessionExpiresAt` = `expiredAt` của refresh token hiện tại
- `remainingMinutes` = thời gian còn lại trước khi session hết hạn
- Thông tin này dùng để hiển thị trên FE (ví dụ: "Phiên đăng nhập hết hạn lúc 08:30")

---

### 4.5 (Admin) Revoke User Session

```
DELETE /api/v1/admin/sessions/:sessionId
Authorization: Bearer <admin_access_token>
```

**Permissions:** Scope `admin:session:revoke`

**Response:**
```json
{
  "status": "revoked",
  "sessionId": 12345,
  "userId": 67890
}
```

---

## 5. Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| `MAX_SESSIONS_REACHED` | 403 | Đã đạt tối đa số thiết bị được phép đăng nhập |
| `INVALID_SESSION` | 400 | Session ID không tồn tại hoặc không hợp lệ |
| `CANNOT_REVOKE_CURRENT` | 403 | Không thể tự revoke session của chính mình |
| `SESSION_NOT_FOUND` | 404 | Không tìm thấy session |
| `INVALID_CONFIRMATION` | 400 | Thiếu confirmation khi revoke all |
| `NO_OTHER_SESSIONS` | 400 | Không có session khác để revoke |

---

## 6. Session Limit Enforcement — Detailed Logic

```
function enforceSessionLimit(userId, userTier, newSessionDeviceType, newSessionDeviceName):
    maxSessions = getMaxSessions(userTier)    // DEFAULT=3, VIP=5
    activeSessions = SELECT FROM t_refresh_token
                     WHERE user_id = :userId
                       AND expired_at > NOW()
                       AND is_revoked = 0
                       AND id != :currentSessionId   // nếu là refresh, không update

    if len(activeSessions) >= maxSessions:
        // Có thể thay đổi policy: FIFO hoặc REJECT
        // Policy 1 (recommended): REJECT — user phải chủ động revoke
        throw MAX_SESSIONS_REACHED

        // Policy 2 (future): FIFO — auto-revoke oldest inactive session
        // oldestSession = min(activeSessions, key=last_active_at)
        // revokeSession(oldestSession.id)
        // Log SESSION_FIFO_EVICTED

    // ALLOW — tạo token mới
```

**Chọn Policy:** **REJECT** (Phase 1) — user kiểm soát chủ động. FIFO là Phase 2 tùy chọn.

---

## 7. Session Timeout UX Flow

### 7.1 Current Flow

```
[Refresh token hết hạn] → [API gọi refresh token bị lỗi]
    → [OnRefreshTokenInvalid.ts handle]
    → [Alert "Phiên đăng nhập đã hết hạn"] → [Navigate to LoginScreen]
```

### 7.2 Proposed Flow

```
T-5 phút: [FE check remainingMinutes]
    → [Hiển thị inline toast/banner: "Phiên đăng nhập sẽ hết hạn trong 5 phút"]
    → [Cho phép user "Kéo dài phiên" (nếu muốn)]

T-0: [Session hết hạn]
    → [Alert "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"]
    → [Clear local state] → [Navigate to LoginScreen]

Đóng app trong background:
    → [Khi app mở lại, check remainingMinutes]
    → [Nếu < 0: show session expired screen]
    → [Nếu < 5: show warning + extend option]
```

### 7.3 FE Implementation Notes

- **Session timer:** Tính từ `sessionExpiresAt` trả về từ API /sessions/timeout
- **Check interval:** Mỗi 60 giây (hoặc khi app resume từ background)
- **Warning threshold:** 5 phút trước khi hết hạn
- **Extend session:** Gọi refresh token → cập nhật `refExpiredTime`
- **Biometric auto-login:** Nếu user đã login biometric, có thể silent refresh session (trong giới hạn cho phép)

---

## 8. Security Considerations

| Concern | Mitigation |
|---------|------------|
| Session ID enumeration | Session ID là auto-increment integer → cần check ownership (userId từ token) |
| Revoke session của user khác | Mỗi session API check ownership bằng userId từ access token |
| Revoke all không confirm | Yêu cầu `confirm: true` trong body |
| Token leaking | Session list API KHÔNG trả về token string |
| Unlimited session creation | Enforce `maxSessions` trước khi tạo token mới |
| Brute-force session ID | Rate limit: 10 requests/phút cho DELETE /sessions/:id |
| Admin abuse | Audit log cho mọi admin session revoke |

---

## 9. Integration Points

| Component | Integration | Notes |
|-----------|-------------|-------|
| **AAA Service** | Thêm session limit check trong `generateToken()` | Core enforcement point |
| **Login flow** | Login trả về `remainingSlots` nếu gần đầy | Warning trước khi reject |
| **FE — LoginScreen** | Hiển thị warning "Đã đăng nhập N/{max} thiết bị" | Sau khi verify OTP thành công |
| **FE — Settings** | Thêm mục "Quản lý thiết bị" (DeviceListScreen) | Gọi GET /sessions, DELETE /sessions/:id |
| **FE — Auth saga** | Cập nhật session timeout tracking (`OnRefreshTokenInvalid.ts`) | Warning 5 phút trước expire |
| **Alert System (P1)** | Push notification khi new device login + device revoked | Cross-ref khi implement |
| **Audit Log (P1)** | Log SESSION_REVOKED, SESSION_EXPIRED, SESSION_LIMIT_REACHED | Cross-ref khi implement |
| **Device Fingerprinting** | `deviceName` gợi ý từ fingerprint signals | Cross-ref khi implement |

---

## 10. Phase Implementation Plan

### Phase 1: Core Session Management (Estimated: 8 days)

**Mục tiêu:** Add session limit + session list/revoke API + cập nhật FE

**Backend (5 days):**
1. DB migration: add columns `device_name`, `last_active_at`, `is_revoked`
2. Add `session.maxConcurrentSessions` config
3. Implement `enforceSessionLimit()` trong TokenService
4. Tạo 3 endpoints: `GET /sessions`, `DELETE /sessions/:id`, `DELETE /sessions`
5. Add `GET /sessions/timeout` endpoint
6. Update `generateToken()` để gọi enforceSessionLimit
7. Update `createRefreshToken()` để set `device_name`, `last_active_at`
8. Update refresh flow (refreshAccessToken) — update `last_active_at`

**FE (3 days):**
1. DeviceListScreen: list sessions, hiển thị device info
2. Revoke session (swipe/xác nhận)
3. Revoke all other sessions (button + confirm modal)
4. Session timeout banner/warning (dựa trên /sessions/timeout)
5. Login warning: hiển thị số thiết bị đang active nếu >= threshold

### Phase 2: Notification & Admin (Estimated: 5 days)

**Mục tiêu:** Thông báo new device login + admin force revoke

1. Event `SESSION_NEW_DEVICE_LOGIN` → push notification (khi có Alert System)
2. Event `SESSION_REVOKED` → push notification
3. Admin endpoint `DELETE /admin/sessions/:id`
4. Admin UI: user detail → manage sessions tab

### Phase 3: Device History (Estimated: 3 days)

**Mục tiêu:** Lịch sử login/logout cho compliance

1. API `GET /sessions/history?limit=20` — trả về login/logout events gần đây
2. Backfill data từ `t_access_token_history` (đã có từ trước)
3. FE: DeviceHistoryScreen với timeline

---

## 11. Open Questions

| Question | Decision | Rationale |
|----------|----------|-----------|
| `maxSessions` mặc định là 3 hay 5? | **3** (Phase 1) | Balance security vs UX; có thể tăng sau |
| Policy khi đạt limit: REJECT hay FIFO? | **REJECT** (Phase 1) | User control; FIFO gây confusion |
| Admin có bypass session limit không? | **Có** (admin không bị limit) | Admin operations không nên bị chặn |
| Tính session limit cho mỗi loại grant type? | **Gộp chung** | Mọi login method đều tính là 1 session |

---

## 12. References

- TT134/2024/TT-BTC Điều 5.2 — Xác thực nhiều lớp
- TT134/2024/TT-BTC Điều 6.1 — Kiểm soát truy cập
- TT134/2024/TT-BTC Điều 6.3 — Thông báo thời gian phiên đăng nhập
- TT134/2024/TT-BTC Điều 13.2 — Giám sát thiết bị đăng nhập
- TT134/2024/TT-BTC Điều 13.4 — Cảnh báo bất thường
- TT134/2024/TT-BTC Điều 13.5 — Khóa thiết bị từ xa

---

## Status

- **Spec:** Draft v1.0
- **Phase 1:** Pending implementation
- **Next:** Phase 1 backend implementation (after spec review)

---

*Last updated: 2026-06-13*

---

Document Status: 📋 | For: PM/Dev | Next Steps: Review nội dung, cập nhật status trên Tracking/tasks.js
