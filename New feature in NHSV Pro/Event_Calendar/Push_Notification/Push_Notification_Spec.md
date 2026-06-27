# Push Notification Spec — Event Calendar (A-02)

**Product:** NHSV Pro · Mobile App
**Feature ID:** A-02
**PM:** Midu (Nguyễn Minh Đức)
**Status:** ✅ Approved
**Version:** 2.0 · 2026-06-23

---

## Scope

Thông báo nhắc nhở user trước ngày GDKHQ của mã cổ phiếu họ đang nắm giữ. Chỉ gửi đúng target — không broadcast toàn bộ user.

---

## Trigger

| Trigger ID | Điều kiện | Thời điểm gửi |
|---|---|---|
| `TRIGGER_GDKHQ_REMINDER` | Sự kiện GDKHQ sắp diễn ra và user đang hold mã đó | T-3 ngày (08:00) |

> **Open Q4:** PRD Section 12 ghi D-1, spec này ghi T-3 — cần PM xác nhận trước khi implement.

---

## Notification Template

```
Title: Sắp đến ngày GDKHQ — {stockCode}
Body:  {stockCode} có ngày giao dịch không hưởng quyền vào {gdkhqDate}. Xem chi tiết lịch sự kiện.
```

---

## Deeplink

| Trigger | Deeplink | Màn hình đích |
|---|---|---|
| `TRIGGER_GDKHQ_REMINDER` | `nhsvpro://event-calendar/{eventId}` | EventDetailScreen (A-02) |

Data payload kèm theo notification:

```json
{ "eventId": "{eventId}" }
```

---

## Approach: Portfolio Snapshot + Eligible Pool

Portfolio của user nằm ở Core Lotte, chỉ truy cập qua lotte-bridge — không thể JOIN trực tiếp với NHSV DB. Giải pháp tách thành 2 phase độc lập:

- **Phase 1 (06:00):** Sync portfolio từ Core Lotte → snapshot table trong NHSV DB
- **Phase 2 (07:50):** JOIN snapshot với event_calendar → build và gửi notification

Phase 2 không phụ thuộc Core Lotte tại thời điểm gửi. Nếu Core down lúc 06:00, notification vẫn gửi dựa trên snapshot ngày hôm trước.

---

## Data Model

### notification_eligible_pool

Danh sách user đủ điều kiện nhận notification: có device token OneSignal active, đã login trong 1 năm qua.

| Column | Type | Ghi chú |
|---|---|---|
| user_id | BIGINT PK | |
| added_at | DATETIME NOT NULL | Lần đầu vào pool |
| last_seen_at | DATETIME NOT NULL | Lần login / token refresh gần nhất |
| portfolio_synced_at | DATETIME | Lần cuối sync portfolio từ Core. NULL = chưa sync |

### user_portfolio_snapshot

Snapshot danh mục cổ phiếu của từng user, sync từ Core Lotte hàng ngày.

| Column | Type | Ghi chú |
|---|---|---|
| user_id | BIGINT NOT NULL | |
| stock_code | VARCHAR(10) NOT NULL | |
| quantity | DECIMAL(15,4) NOT NULL | Số lượng đang nắm giữ |
| synced_at | DATETIME NOT NULL | |
| PRIMARY KEY | (user_id, stock_code) | |

---

## Job Flow hàng ngày

### Login event — Real-time

Khi user login, FE gửi device token lên BE. BE thực hiện:

```sql
INSERT INTO notification_eligible_pool (user_id, added_at, last_seen_at)
VALUES (?, NOW(), NOW())
ON CONFLICT (user_id) DO UPDATE SET last_seen_at = NOW()
```

Đây là cơ chế chính build pool — không cần query OneSignal API.

---

### 05:30 — Reconcile job

Safety net: bổ sung user login hôm qua chưa có trong pool do edge case:

```sql
INSERT IGNORE INTO notification_eligible_pool (user_id, added_at, last_seen_at)
SELECT user_id, NOW(), updated_at
FROM user_devices
WHERE updated_at > NOW() - INTERVAL 1 DAY
  AND user_id NOT IN (SELECT user_id FROM notification_eligible_pool)
```

---

### 06:00 — Portfolio sync job

Sync portfolio từ Core Lotte cho user trong pool chưa được sync hôm nay.

**Bước 1 — Lấy danh sách cần sync:**

```sql
SELECT user_id FROM notification_eligible_pool
WHERE portfolio_synced_at IS NULL
   OR portfolio_synced_at < NOW() - INTERVAL 1 DAY
```

**Bước 2 — Gọi lotte-bridge:** Concurrency max 20–50 calls song song. Lỗi từng user được log và skip — không dừng toàn bộ job. Chỉ update `portfolio_synced_at` khi call thành công HTTP 200.

**Bước 3 — Upsert snapshot:**

```sql
INSERT INTO user_portfolio_snapshot (user_id, stock_code, quantity, synced_at)
VALUES (?, ?, ?, NOW())
ON CONFLICT (user_id, stock_code)
DO UPDATE SET quantity = EXCLUDED.quantity, synced_at = NOW()
```

Xóa records không còn trong portfolio (user đã bán hết): `DELETE WHERE user_id = ? AND stock_code NOT IN (holdings từ Core)`.

**Bước 4 — Update pool:**

```sql
UPDATE notification_eligible_pool SET portfolio_synced_at = NOW() WHERE user_id = ?
```

---

### 07:50 — Notification build + send job

```sql
SELECT pool.user_id, ec.stock_code, ec.event_type_label,
       ec.rate_display, ec.gdkhq_date, ec.event_id
FROM event_calendar ec
JOIN user_portfolio_snapshot ups ON ec.stock_code = ups.stock_code
JOIN notification_eligible_pool pool ON ups.user_id = pool.user_id
WHERE ec.gdkhq_date = CURDATE() + INTERVAL 3 DAY
```

Gửi qua OneSignal/FCM batch API lúc 08:00. Retry 3 lần nếu thất bại, cách nhau 5 phút. Alert nếu thất bại hoàn toàn.

---

### Weekly (Chủ nhật 02:00) — Prune job

```sql
DELETE FROM notification_eligible_pool WHERE last_seen_at < NOW() - INTERVAL 1 YEAR;
DELETE FROM user_portfolio_snapshot
  WHERE user_id NOT IN (SELECT user_id FROM notification_eligible_pool);
```

---

## FE — Deeplink Handler

Đăng ký route `nhsvpro://event-calendar/{eventId}` trong `Linking.tsx`. Parse `eventId` → navigate đến `EventDetailScreen`. Hoạt động ở cả foreground, background, và cold start.

---

## Scale Characteristics

| Giai đoạn | Pool size | Core calls/ngày | Thời gian sync (est.) |
|---|---|---|---|
| Hiện tại | < 10K | ~10K worst case | ~3–5 phút |
| Scale 50K | 50K | ~5K–10K (incremental) | ~5–10 phút |
| Scale 200K | 200K | ~20K–40K (incremental) | ~15–20 phút |

Incremental logic — chỉ sync user có `portfolio_synced_at < 1 day` — giữ Core calls không tăng tuyến tính.

---

## Graceful Degradation

| Tình huống | Hành vi |
|---|---|
| Core Lotte down lúc 06:00 | Dùng snapshot ngày hôm trước. Notification vẫn gửi, data lệch tối đa 1 ngày |
| Lotte-bridge lỗi từng user | Log + skip, tiếp tục job |
| OneSignal/FCM down lúc 08:00 | Retry 3 lần × 5 phút. Alert nếu thất bại hoàn toàn |

---

## Open Questions

| # | Câu hỏi | Owner |
|---|---|---|
| Q1 | lotte-bridge có endpoint "get portfolio cho user X" không? Format response? | BE Lead |
| Q2 | `user_devices` là tên bảng thực tế trong NHSV DB không? | BE Lead |
| Q3 | OneSignal hay FCM/APNs trực tiếp? (ảnh hưởng batch API ở bước 07:50) | IT |
| Q4 | Trigger T-3 hay D-1? PRD Section 12 ghi D-1, spec này ghi T-3 — cần thống nhất | PM |

---

## Dependencies

```
Event_Calendar/Push_Notification ←→ NHSV_Channel/Push_Notification
  (cùng dùng notification service; implement chung 1 lần ở BE)
```

---

## Work Breakdown

| Task | Mô tả |
|---|---|
| BE-N1 | Tạo bảng `notification_eligible_pool` + migration |
| BE-N2 | Tạo bảng `user_portfolio_snapshot` + migration |
| BE-N3 | Login hook: UPSERT eligible pool khi user login |
| BE-N4 | Reconcile job (05:30) |
| BE-N5 | Portfolio sync job (06:00): lotte-bridge batch, concurrency, incremental |
| BE-N6 | Notification build + send job (07:50): JOIN query + OneSignal/FCM batch |
| BE-N7 | Prune job (weekly) |

---

Document Status: ✅ Approved | For: BE Lead, IT, Mobile FE | Next Steps: Confirm Q1–Q4 trước khi estimate BE-N1 đến BE-N7
