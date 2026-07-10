# Admin Notification Service — API Specification

**Document Type:** API Specification
**Category:** Notification / Admin Portal
**Project:** NHSV Pro
**Jira:** NHMTS-88 (Epic NHMTS-74 — Notifications)
**Date:** 2026-07-09
**Version:** 2.0 (thay thế draft HTML ban đầu)

> Spec này theo **TradeX API Conventions** (`Knowledge/TradeX/API Standards/tradex-api-conventions.md`).
> **Integration Type:** TradeX-native (nhsv-admin DB trực tiếp — không qua Lotte/Core).

---

## Table of Contents

1. [Overview](#overview)
2. [Kiến trúc — vì sao endpoint này là nền chung](#kiến-trúc--vì-sao-endpoint-này-là-nền-chung)
3. [Tích hợp OneSignal](#tích-hợp-onesignal)
4. [API — Admin-facing](#api--admin-facing)
5. [API — App-facing (Mobile)](#api--app-facing-mobile)
6. [Database Schema](#database-schema)
7. [Business Rules](#business-rules)
8. [Convention Check](#convention-check)
9. [Open Questions](#open-questions)

---

## Overview

### Purpose

Cho phép admin gửi notification (Promotion/News/Daily Report/Reminder) trực tiếp từ **NHSV Admin Portal**, không cần thao tác trên OneSignal Dashboard. Phase 1: chỉ gửi cho **toàn bộ subscriber** ("Tất cả") — UI Admin Portal không có lựa chọn segment nào khác.

### Scope Phase 1

- ✅ Soạn & gửi/hẹn giờ notification tới **Tất cả subscriber** (5 loại: PROMOTION/NEWS/DAILY_REPORT/REMINDER/NORMAL)
- ✅ **Hủy tin đã hẹn giờ** (chưa gửi) ngay trong Admin Portal — gọi OneSignal Cancel API
- ✅ Màn "Lịch sử đã gửi" cho admin
- ✅ **Trigger tự động từ NH Research**: admin publish bài (Publish & Notify) → tự gửi push NEWS tới toàn bộ subscriber theo template song ngữ (internal call trong cùng service `nhsv-admin`)
- ✅ Tab "Lịch sử thông báo" cho app (đọc/đánh dấu đã đọc/ẩn)
- ❌ Segment-based targeting theo tiêu chí OneSignal — **ngoài scope Phase 1** (xem mục kế tiếp — vẫn cần chuẩn bị contract)
- ❌ Collapse ID, Action Buttons — ngoài scope

### Architecture

```
Admin Portal UI → nhsv-admin (POST /api/v1/admin/notifications/send)
                     → gọi OneSignal Create Notification API (trực tiếp, RestTemplate)
                     → INSERT t_notification (đồng bộ, sau khi OneSignal xác nhận)

App → GET/PATCH/DELETE /api/v1/notifications* → đọc t_notification + t_notification_recipient_state
```

---

## Kiến trúc — vì sao endpoint này là nền chung

Trong quá trình phân tích hệ thống (xem `_workspace/01_analyst_findings.md`), phát hiện **2 spec khác trong repo cùng cần "1 BE notification service chung"**:

| Spec | Đặc điểm target | Trạng thái |
|---|---|---|
| `NHSV_Channel/Push_Notification/Push_Notification_Spec.md` (NH Research, Khuyến nghị) | Theo **segment/category** | Backlog |
| `Event_Calendar/Push_Notification/Push_Notification_Spec.md` (nhắc GDKHQ) | Theo **danh sách user_id cụ thể** (không broadcast) | ✅ Approved |

→ `POST /api/v1/admin/notifications/send` được thiết kế để 3 feature này **dùng chung 1 contract**, tránh làm trùng API 3 lần. Field `audienceType` quyết định cách target:

| `audienceType` | Dùng cho | Field kèm theo |
|---|---|---|
| `ALL` | **Admin Portal Phase 1** (duy nhất Phase 1 dùng) | — |
| `SEGMENT` | NH Research / Khuyến nghị (Phase sau) | `userSegment` |
| `USER_LIST` | Event Calendar GDKHQ reminder (Phase sau, gọi service-to-service) | `targetUserIds[]` |

**UI Admin Portal Phase 1 không hiển thị lựa chọn `audienceType`** — luôn set cứng `ALL` phía FE/BE. Field này tồn tại trong contract để tránh breaking change khi 2 feature kia được implement.

---

## Tích hợp OneSignal

**Quyết định:** `nhsv-admin` gọi OneSignal Create Notification API **trực tiếp** (không route qua Kafka/`notification-main`), theo đúng pattern đã có sẵn trong `nhsv-admin` (không phải xây mới):

| Việc | Pattern tái dùng | Vị trí tham chiếu |
|---|---|---|
| HTTP client gọi OneSignal | `RestTemplate` — giống cách `CopyTradingSendOTPServiceImpl` đang gọi 1sms | `service/impl/CopyTradingSendOTPServiceImpl.java` |
| Config `appId`/`apiKey` | Thêm nested class `OneSignal` vào `AppConf.java`, cùng cấu trúc `VietStock`/`SmsServer` đã có | `config/AppConf.java` |
| **Tên field config** | Đặt **giống hệt** `notification-main`: `appId`, `apiKey` (không đổi tên) — để đối chiếu khi rotate key | `notification-main/application.yaml` → `oneSignalMap.nhsv` |
| Retry khi OneSignal lỗi | Thêm `spring-retry` (chưa có trong `pom.xml`) — retry 3 lần, backoff 2s/4s/8s | Mới |

> ⚠️ **Ghi chú bắt buộc trong code/README `nhsv-admin`:** OneSignal integration này **độc lập** với `notification-main` — dùng chung 1 OneSignal App (`nhsv`) nhưng gọi trực tiếp cho mục đích Admin broadcast. Đây là quyết định có chủ đích, **không phải duplicate code cần dọn**. Khi rotate OneSignal API Key, phải update ở **cả 2 nơi**: `notification-main` (`application.yaml`) và `nhsv-admin` (`AppConf`/secret store).

### Mapping TradeX → OneSignal

| TradeX param | OneSignal param | Ghi chú |
|---|---|---|
| `notificationType` | `data.noti_type` | Gửi kèm trong `data` để audit |
| `audienceType=ALL` | `included_segments: ["Total Subscriptions"]` | Hardcode khi `audienceType=ALL` |
| `titleEn`/`titleVi` | `headings.en`/`headings.vi` | |
| `bodyEn`/`bodyVi` | `contents.en`/`contents.vi` | `contents` bắt buộc theo OneSignal |
| `htmlContent` | `data.html_content` | Chỉ gửi khi `notificationType` ∈ {PROMOTION, NEWS}, đã sanitize |
| `imageUrl` | `big_picture` (Android) / `ios_attachments.id` (iOS) | Chỉ PROMOTION/NEWS |
| `actionDeeplink` | `url` | **Mọi loại tin** (BR-008 rev.2) |
| `actionLabelVi`/`actionLabelEn` | `data.action_label_vi` / `data.action_label_en` | App render nút CTA trong inbox — không ảnh hưởng push banner |
| `sendSchedule`/`scheduledAt` | `send_after` | `IMMEDIATE` → không set |
| *(ẩn, hệ thống set)* `priority` | `priority` (10/5) | PROMOTION/NEWS → 10; còn lại → 5 |

### OneSignal Cancel API (cho tính năng hủy tin hẹn giờ)

| Thao tác TradeX | OneSignal API | Ghi chú |
|---|---|---|
| `PUT /api/v1/admin/notifications/{id}/cancel` | `DELETE https://api.onesignal.com/notifications/{onesignal_message_id}?app_id={appId}` | OneSignal chỉ hủy được notification **chưa gửi** (scheduled). Đã gửi rồi → OneSignal trả lỗi → TradeX trả 422. Dùng `onesignal_message_id` đã lưu trong `t_notification` khi tạo. |

---

## API — Admin-facing

### 1. `POST /api/v1/admin/notifications/send`

**Auth:** JWT Bearer (Admin role)

#### Request Body

| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `notificationType` | String | ✅ | `PROMOTION`\|`NEWS`\|`DAILY_REPORT`\|`REMINDER`\|`NORMAL` |
| `titleEn` / `titleVi` | String | ⚠️ ít nhất 1 | Tiêu đề theo ngôn ngữ |
| `bodyEn` / `bodyVi` | String | ⚠️ ít nhất 1 | Nội dung theo ngôn ngữ |
| `htmlContent` | String | ❌ | Rich content hiển thị trong inbox app — chỉ PROMOTION/NEWS (BR-008). Phía Admin UI nhập qua **rich text HTML editor (WYSIWYG)**, không phải textarea HTML thô — API nhận HTML output của editor; BE vẫn sanitize server-side theo whitelist tag cố định (BR-021) bất kể editor phía FE |
| `imageUrl` | String | ❌ | Ảnh banner trong inbox app — chỉ PROMOTION/NEWS (BR-008) |
| `actionDeeplink` | String | ❌ | **Launch URL** (deeplink `nhsv://...` hoặc web URL) — **mọi loại tin** đều gắn được (BR-008 rev.2) |
| `actionLabelVi` / `actionLabelEn` | String | ❌ | Nhãn nút CTA trong inbox app (vd "Khám phá ngay" / "Explore now"). Nếu có `actionDeeplink` mà bỏ trống → mặc định "Xem chi tiết" / "View details" |
| `sendSchedule` | String | ✅ | `IMMEDIATE` \| `SCHEDULED` |
| `scheduledAt` | String (ISO8601) | ⚠️ bắt buộc nếu `SCHEDULED` | Phải > thời điểm hiện tại (BR-009) |
| `audienceType` | String | ✅ | `ALL`\|`SEGMENT`\|`USER_LIST` — **Phase 1 luôn `ALL`** |
| `userSegment` | String | ⚠️ bắt buộc nếu `SEGMENT` | Chưa dùng ở Phase 1 |
| `targetUserIds` | String[] | ⚠️ bắt buộc nếu `USER_LIST` | Chưa dùng ở Phase 1 |

#### Trường tự động điền

| TradeX Field | Nguồn | Ghi chú |
|---|---|---|
| `createdByAdminId` | JWT Token (admin username) | Không nhận từ body |
| `priority` | Derive từ `notificationType` | PROMOTION/NEWS → HIGH; còn lại → NORMAL (BR-012) |
| `sentAt` / `status` | Hệ thống | Set khi OneSignal xác nhận thành công |

#### DTO Naming

| DTO | Tên |
|---|---|
| Request (send) | `NotificationSendRequest` |
| Response (send) | `NotificationSendResponse` |
| Response (cancel) | `NotificationCancelResponse` |
| Response (list admin) | `AdminNotificationListResponse` |
| Response (detail admin) | `AdminNotificationDetailResponse` |
| Response (list app) | `NotificationListResponse` |

#### Response (200) — TradeX-native mutation

```json
{ "id": 128, "status": "SENT", "recipientsEstimate": 12480 }
```

> `id` là field chính theo chuẩn tradex-native (`{ "id": 42 }`) — `status`/`recipientsEstimate` là field phụ bổ sung cho UI, không thay thế `id`.

#### Error

| HTTP | Code | Trường hợp |
|---|---|---|
| 400 | `INVALID_PARAMETER` | Thiếu cả title/body 2 ngôn ngữ; `scheduledAt` không hợp lệ; thiếu `userSegment`/`targetUserIds` theo `audienceType` |
| 422 | `NOTIFICATION_SEND_FAILED` | OneSignal API trả lỗi (rate limit, invalid config) — TradeX-native, dùng `messageParams` |
| 500 | `INTERNAL_SERVER_ERROR` | Lỗi hệ thống nội bộ |

### 2. `PUT /api/v1/admin/notifications/{id}/cancel`

**Auth:** JWT Bearer (Admin role). Hủy 1 notification **đang ở trạng thái SCHEDULED** (đã hẹn giờ, chưa gửi).

Luồng xử lý: (1) validate record tồn tại và đang SCHEDULED → (2) gọi OneSignal Cancel API bằng `onesignal_message_id` → (3) OneSignal xác nhận hủy thành công → update `cancelled_at` + `cancelled_by_admin_id`.

#### Response (200)

```json
{ "id": 126, "status": "CANCELLED" }
```

#### Error

| HTTP | Code | Trường hợp |
|---|---|---|
| 404 | `OBJECT_NOT_FOUND` | `id` không tồn tại |
| 422 | `NOTIFICATION_ALREADY_SENT` | Record đã có `sent_at` (đã gửi rồi — không hủy được) hoặc OneSignal báo đã deliver trước khi kịp hủy (race condition sát giờ gửi) |
| 422 | `NOTIFICATION_ALREADY_CANCELLED` | Record đã hủy trước đó (idempotent-check: trả lỗi rõ ràng thay vì im lặng) |
| 422 | `NOTIFICATION_CANCEL_FAILED` | OneSignal Cancel API trả lỗi khác (network, config) — sau khi hết retry |

> ⚠️ **Race condition sát giờ gửi:** nếu admin bấm hủy đúng lúc OneSignal đang gửi, OneSignal Cancel trả lỗi → TradeX trả `NOTIFICATION_ALREADY_SENT` và **không** đánh dấu cancelled. Record giữ nguyên, đến giờ đồng bộ trạng thái thành SENT. UI phải hiển thị đúng thông báo "Tin đã được gửi, không thể hủy".

### 3. `GET /api/v1/admin/notifications`

Query: `type?`, `status?` (`SENT`\|`SCHEDULED`\|`CANCELLED`), `fetchCount?`, `nextKey?`.

```json
{
  "totalCount": 128,
  "nextKey": "eyJpZCI6MTI4fQ==",
  "notifications": [
    {
      "id": 128,
      "type": "PROMOTION",
      "titleEn": "Special offer this July",
      "titleVi": "Ưu đãi tháng 7",
      "status": "SENT",
      "recipientsEstimate": 12480,
      "sentAt": "2026-07-08T02:00:00Z",
      "scheduledAt": null,
      "priority": "HIGH",
      "createdByAdminId": "ducnm"
    }
  ]
}
```

Trả cả `titleEn`/`titleVi` (không resolve theo `Accept-Language`) — admin cần xem nội dung gốc.

### 4. `GET /api/v1/admin/notifications/{id}`

Trả đầy đủ field (`bodyEn/bodyVi`, `htmlContent`, `imageUrl`, `actionDeeplink`, `cancelledAt`, `cancelledByAdminId`) cho popup chi tiết.

| HTTP | Code |
|---|---|
| 404 | `OBJECT_NOT_FOUND` |

### Trạng thái notification (derive, không lưu cột status riêng)

| Status | Điều kiện |
|---|---|
| `SENT` | `sent_at` có giá trị |
| `SCHEDULED` | `scheduled_at` có giá trị, `sent_at` NULL, `cancelled_at` NULL |
| `CANCELLED` | `cancelled_at` có giá trị |

---

## Trigger tự động — NH Research publish

Khi admin publish bài NH Research với toggle **"Gửi push notification khi publish"** bật (mặc định ON — theo `NHSV_Channel/Push_Notification_Spec.md`), backend tự động gửi push tới **toàn bộ subscriber**.

### Cơ chế

- NH Research admin controller và Notification service **cùng nằm trong `nhsv-admin`** → trigger là **internal method call** (`NotificationSendService.send(...)`), KHÔNG phải HTTP service-to-service. Không cần cơ chế auth riêng (Q2 chỉ còn áp dụng cho Event Calendar sau này).
- Gửi với: `notificationType=NEWS`, `audienceType=ALL`, priority HIGH (auto theo BR-012), nội dung theo template song ngữ bên dưới, `actionDeeplink` theo category bài viết.
- Notification tự động cũng **ghi vào `t_notification`** như tin gửi tay — `created_by_admin_id` = admin đã bấm publish, xuất hiện trong "Lịch sử đã gửi" bình thường.
- **Publish và push tách bạch:** nếu gửi push thất bại (OneSignal lỗi sau retry) → bài viết **vẫn publish thành công**, chỉ hiển thị cảnh báo "Bài đã đăng nhưng gửi thông báo thất bại" + cho phép gửi lại thủ công từ composer. Không rollback publish.

### Template song ngữ — NH Research auto-trigger

| Field | Tiếng Việt | English |
|---|---|---|
| Title | `Báo cáo mới từ NH Research` | `New report from NH Research` |
| Body | `{title} — {categoryVi}. Nhấn để đọc ngay trên NHSV Pro.` | `{title} — {categoryEn}. Tap to read on NHSV Pro.` |
| Nút CTA (`actionLabel`) | `Đọc ngay` | `Read now` |
| Launch URL (`actionDeeplink`) | `nhsvpro://channel/nh-research?category={category}` | *(chung)* |

Placeholder: `{title}` = tiêu đề bài viết (giữ nguyên ngôn ngữ gốc bài); `{category}` mapping:

| Category code | `{categoryVi}` | `{categoryEn}` |
|---|---|---|
| `THI_TRUONG` | Thị trường | Market |
| `DOANH_NGHIEP` | Doanh nghiệp | Company |
| `VI_MO` | Vĩ mô | Macro |

> Khuyến nghị (Khuyến nghị/NHSV Channel khác) sẽ dùng cùng cơ chế khi feature đó implement — template riêng định nghĩa trong spec của feature đó, tham chiếu bảng template chuẩn bên dưới.

---

## Template message song ngữ — gợi ý sẵn trong composer

Khi admin chọn loại tin ở Bước 1, composer prefill template tương ứng (cả VI + EN) — admin sửa placeholder rồi gửi. Template chỉ là gợi ý, sửa tự do.

| Loại | Title VI / EN | Body VI / EN | Nút CTA VI / EN |
|---|---|---|---|
| PROMOTION | `🎁 {tên chương trình}` / `🎁 {promotion name}` | `{ưu đãi chính} — áp dụng đến {ngày}. Nhấn để xem chi tiết.` / `{main offer} — valid until {date}. Tap for details.` | `Khám phá ngay` / `Explore now` |
| NEWS | `{tiêu đề tin}` / `{news headline}` | `{tóm tắt 1 câu}. Xem ngay trên NHSV Pro.` / `{one-line summary}. See it on NHSV Pro.` | `Thử ngay` / `Try now` |
| DAILY_REPORT | `Báo cáo danh mục {dd/MM}` / `Portfolio report {dd/MM}` | `Danh mục của bạn {tăng/giảm} {x}% hôm nay. Xem chi tiết biến động.` / `Your portfolio moved {±x}% today. View details.` | `Xem danh mục` / `View portfolio` |
| REMINDER | `Nhắc: {sự kiện}` / `Reminder: {event}` | `{nội dung} đến hạn vào {ngày}. Kiểm tra ngay để không bỏ lỡ.` / `{item} is due on {date}. Check now.` | `Thanh toán ngay` / `Pay now` |
| NORMAL | `Thông báo từ NHSV` / `Notice from NHSV` | `{nội dung thông báo}.` / `{notice content}.` | `Xem chi tiết` / `View details` |

Quy tắc viết push notification (áp cho cả template):

1. Title ≤ 40 ký tự, Body ≤ 110 ký tự — tránh bị cắt trên lock screen.
2. Body luôn kết bằng call-to-action ngắn ("Nhấn để...", "Xem ngay...").
3. Không viết HOA toàn bộ, tối đa 1 emoji ở Title (chỉ PROMOTION).
4. Số liệu cụ thể > mô tả chung chung ("tăng 2.3%" thay vì "biến động").

---

## Mẫu thông báo hiển thị trong app (rich content)

Style tham chiếu: inbox thông báo SSI iBoard (banner + title + body có emoji bullet/bold highlight + nút CTA full-width). Rich body dùng `htmlContent` (PROMOTION/NEWS) hoặc `body` nhiều dòng (loại khác). 4 mẫu sẵn dùng:

### Mẫu 1 — PROMOTION: Ưu đãi margin

```
Banner:   margin-july2026.png (khuyến nghị 2:1, ~1200×600)
Title:    Margin thảnh thơi, lãi suất hết ý 💰
Rich body:
  NHSV Pro ưu đãi lãi suất margin cho khách hàng mới với 2 gói linh hoạt:
  <b>Chào mừng</b> & <b>Thân thiết</b>.

  👉 Gói Chào mừng: lãi suất chỉ <b>6.8%/năm</b> trong 3 tháng đầu.
  👉 Gói Thân thiết: hoàn tới <b>50% phí giao dịch</b> khi duy trì dư nợ.
  👉 Đăng ký ngay trên NHSV Pro — vài chạm là xong ✨
CTA:      Khám phá ngay  →  nhsv://margin/register
```

### Mẫu 2 — NEWS: Ra mắt Phái sinh

```
Banner:   derivatives-launch.png
Title:    Thị trường lên xuống? Phái sinh cân tất 🚀
Rich body:
  ❌ Chọn đứng ngoài   ✅ <b>Long/Short 2 chiều</b> ngay trên NHSV Pro

  👉 Sinh lời trên mọi xu hướng — tăng hay giảm đều có cơ hội
  🔎 Bảng giá & chart VN30F realtime ngay trong app
  ☝️ Đặt lệnh 1 chạm cùng <b>Stop / OCO / Trailing</b>
CTA:      Thử ngay  →  nhsv://derivatives/trade
```

### Mẫu 3 — DAILY_REPORT: Báo cáo danh mục (plain body nhiều dòng, không banner)

```
Title:    Báo cáo danh mục 09/07 📊
Body:
  Danh mục của bạn +2.3% hôm nay — vượt VN-Index (+0.8%).
  👉 Đóng góp lớn nhất: FPT +4.1%, MWG +2.9%
  👉 Cần chú ý: HPG -1.8%
CTA:      Xem danh mục  →  nhsv://portfolio
```

### Mẫu 4 — REMINDER: Nhắc thanh toán margin (plain, không banner)

```
Title:    Nhắc lịch thanh toán margin ⏰
Body:
  Khoản margin 50.000.000đ đến hạn ngày 12/07.
  👉 Nộp tiền trước 16:00 để tránh phí phạt quá hạn.
CTA:      Thanh toán ngay  →  nhsv://cash/deposit
```

Quy tắc viết rich body (bổ sung cho 4 quy tắc push ở trên):

1. Mở đầu 1 câu hook, sau đó mỗi ý 1 dòng bắt đầu bằng emoji bullet (👉 🔎 ☝️ ✅ ❌).
2. Highlight số liệu/từ khóa bằng `<b>` — không quá 2 highlight/dòng.
3. Tối đa 4-5 dòng bullet — dài hơn thì đưa vào trang đích của deeplink.
4. Nút CTA là động từ hành động ("Khám phá ngay", "Thử ngay", "Thanh toán ngay") — không dùng "Click here"/"OK".

---

## API — App-facing (Mobile)

`account_number`/`userId` lấy từ JWT — FE không truyền tay.

| Method | Path | Mô tả |
|---|---|---|
| `GET` | `/api/v1/notifications` | Lịch sử, filter `type`, phân trang |
| `PATCH` | `/api/v1/notifications/{id}/read` | Đánh dấu đã đọc (idempotent — BR-007) |
| `PATCH` | `/api/v1/notifications/readAll` | Đánh dấu tất cả đã đọc |
| `DELETE` | `/api/v1/notifications/{id}` | Ẩn khỏi inbox (soft, set `is_hidden=true`) |

### `GET /api/v1/notifications`

#### Query Parameters

| Param | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `type` | String | ❌ | Lọc theo loại tin — 1 trong 5 giá trị admin đã gửi: `PROMOTION`\|`NEWS`\|`DAILY_REPORT`\|`REMINDER`\|`NORMAL`. Không truyền → trả tất cả loại. |
| `fromDate` | String (ISO8601 date) | ❌ | Lọc `sentAt >= fromDate` (BR-022). |
| `toDate` | String (ISO8601 date) | ❌ | Lọc `sentAt <= toDate` (BR-022). |
| `fetchCount` | Number | ❌ | Số lượng/trang, default 20, max 100. |
| `nextKey` | String | ❌ | Cursor phân trang, lấy từ response trước. |

```json
{
  "totalCount": 42,
  "nextKey": "eyJpZCI6Ii4uLiJ9",
  "notifications": [
    {
      "id": 128,
      "type": "PROMOTION",
      "title": "Ưu đãi tháng 7",
      "body": "Nhận ngay lãi suất ưu đãi. Nhấn để xem chi tiết.",
      "htmlContent": "<b>Ưu đãi 20%</b>...",
      "imageUrl": "https://cdn.nhsv.vn/promo/july2026.png",
      "actionDeeplink": "nhsv://promotion/july2026",
      "actionLabel": "Khám phá ngay",
      "sentAt": "2026-07-08T02:00:00Z",
      "isRead": false
    }
  ]
}
```

`title`/`body`/`actionLabel` resolve theo `Accept-Language` (vi/en/ko → default vi).

**Cấu trúc render 1 thẻ thông báo trong inbox app** (style tham chiếu: SSI iBoard tab Ưu đãi):

1. Ảnh banner (`imageUrl`) — nếu có
2. Title (đậm) + rich body (`htmlContent` nếu có, fallback `body`) — hỗ trợ xuống dòng, bold, emoji bullet
3. Timestamp
4. **Nút CTA full-width** (`actionLabel`) — chỉ hiện khi có `actionDeeplink`; tap → mở deeplink/web URL

### Mutation Responses

```json
// PATCH .../read
{ "id": 128, "isRead": true }

// PATCH .../readAll
{ "updatedCount": 12 }

// DELETE /notifications/{id}
{ "id": 128, "isHidden": true }
```

| HTTP | Code | Trường hợp |
|---|---|---|
| 404 | `OBJECT_NOT_FOUND` | `id` không tồn tại |
| 401 | `UNAUTHORIZED` | Token không hợp lệ/hết hạn |

---

## Database Schema

**Service:** `nhsv-admin` (JHipster + Liquibase). Tạo 2 entity mới qua **JHipster entity sub-generator** (khuyến nghị, để auto-sinh entity class + repository + changelog đúng convention), hoặc viết tay 1 changelog thủ công đặt tên theo Jira ticket nếu không dùng generator.

> **Lưu ý convention quan trọng:** dùng `id BIGINT AUTO_INCREMENT` làm PK (giống mọi entity JHipster hiện có trong `nhsv-admin`: `Broker`, `SocialLink`, `ChatRoom`...) — **không dùng `VARCHAR(36)` UUID** như draft ban đầu. Field `id` trong response API cũng phải là **Number**, khớp ví dụ `{ "id": 42 }` trong `tradex-api-conventions.md`.

### `t_notification` (1 row / lần gửi — cấp campaign)

```xml
<changeSet id="NHMTS-88-1" author="nhsv-admin">
    <createTable tableName="t_notification">
        <column name="id" type="bigint" autoIncrement="true">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="onesignal_message_id" type="varchar(255)">
            <constraints nullable="false" unique="true"/>
        </column>
        <column name="type" type="varchar(50)"><constraints nullable="false"/></column>
        <column name="title_en" type="varchar(255)"/>
        <column name="title_vi" type="varchar(255)"/>
        <column name="body_en" type="longtext"/>
        <column name="body_vi" type="longtext"/>
        <column name="html_content" type="longtext"/>
        <column name="image_url" type="varchar(2048)"/>
        <column name="action_deeplink" type="varchar(2048)"/>
        <column name="action_label_vi" type="varchar(64)"/>
        <column name="action_label_en" type="varchar(64)"/>
        <column name="priority" type="varchar(10)" defaultValue="NORMAL">
            <constraints nullable="false"/>
        </column>
        <column name="audience_type" type="varchar(20)" defaultValue="ALL">
            <constraints nullable="false"/>
        </column>
        <column name="created_by_admin_id" type="varchar(64)"><constraints nullable="false"/></column>
        <column name="scheduled_at" type="datetime"/>
        <column name="sent_at" type="datetime"/>
        <column name="cancelled_at" type="datetime"/>
        <column name="cancelled_by_admin_id" type="varchar(64)"/>
        <column name="created_at" type="datetime"><constraints nullable="false"/></column>
        <column name="updated_at" type="datetime"/>
    </createTable>
</changeSet>
```

### `t_notification_recipient_state` (per-account, tạo lazy)

```xml
<changeSet id="NHMTS-88-2" author="nhsv-admin">
    <createTable tableName="t_notification_recipient_state">
        <column name="id" type="bigint" autoIncrement="true">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="notification_id" type="bigint"><constraints nullable="false"/></column>
        <column name="account_number" type="varchar(255)"><constraints nullable="false"/></column>
        <column name="is_read" type="boolean" defaultValueBoolean="false"><constraints nullable="false"/></column>
        <column name="read_at" type="datetime"/>
        <column name="is_hidden" type="boolean" defaultValueBoolean="false"><constraints nullable="false"/></column>
        <column name="hidden_at" type="datetime"/>
        <column name="created_at" type="datetime"><constraints nullable="false"/></column>
        <column name="updated_at" type="datetime"/>
    </createTable>
    <addUniqueConstraint tableName="t_notification_recipient_state"
        columnNames="notification_id, account_number"
        constraintName="uq_notification_account"/>
    <addForeignKeyConstraint baseTableName="t_notification_recipient_state" baseColumnNames="notification_id"
        constraintName="fk_recipient_notification"
        referencedTableName="t_notification" referencedColumnNames="id"/>
</changeSet>
```

Row chỉ tạo (upsert) khi 1 account **lần đầu tương tác** (đọc/ẩn) — không fan-out lúc gửi.

### Cột đã bỏ so với schema gốc (webhook NHMTS-870/871, chưa deploy)

| Field | Lý do bỏ |
|---|---|
| `account_number` trên `t_notification` | 1 campaign có nhiều account — chuyển xuống bảng recipient state |
| `is_read`/`read_at` trên `t_notification` | Trạng thái đọc riêng theo từng account |
| `delivered_at` | Chỉ webhook mới cập nhật được, webhook chưa deploy |
| `action_button_text`, `collapse_id` | Ngoài scope Admin Portal |
| `segment_id` | Thay bằng `audience_type` + (Phase sau) bảng phụ lưu chi tiết segment/user_list nếu cần |

---

## Business Rules

| Rule | Nội dung |
|---|---|
| BR-001…BR-007 | *(kế thừa từ draft gốc — không đổi: field required theo loại, idempotent read, v.v.)* |
| **BR-008** *(rev.2)* | `htmlContent`/`imageUrl` chỉ nhận khi `notificationType` ∈ {PROMOTION, NEWS}. **`actionDeeplink` (launch URL) + `actionLabelVi/En` (nhãn nút CTA) nhận với MỌI loại tin** — validate ở cả UI và API. `actionLabel*` chỉ có nghĩa khi có `actionDeeplink` (gửi label không kèm URL → 400 `INVALID_PARAMETER`). |
| **BR-009** | `sendSchedule=SCHEDULED` → `scheduledAt` bắt buộc và phải > hiện tại. Sai → 400 `INVALID_PARAMETER`. |
| **BR-010** | OneSignal trả `id` rỗng (không subscriber nào) → KHÔNG insert `t_notification`, chỉ cảnh báo UI. |
| **BR-011** *(sửa so với draft)* | Vì gọi OneSignal **trực tiếp, đồng bộ** (không qua Kafka async) — INSERT `t_notification` diễn ra **ngay trong cùng request/response** sau khi nhận HTTP response thành công từ OneSignal. Nếu gọi OneSignal thất bại (network/timeout/422 sau khi hết retry) → KHÔNG insert, trả lỗi cho FE. |
| **BR-012** | `priority` set ngầm theo `notificationType`, admin không chỉnh tay ở Phase 1. |
| **BR-013** | `t_notification` lưu cấp campaign — 1 row/lần gửi. |
| **BR-014** | `t_notification_recipient_state` tạo lazy khi account tương tác lần đầu. |
| **BR-015** | Phase 1 chỉ `audienceType=ALL` → mọi account hợp lệ để thấy mọi notification trong lịch sử. |
| **BR-016** | `DELETE /notifications/{id}` chỉ set `is_hidden=true`, không xoá `t_notification`. |
| **BR-017** *(mới)* | `audienceType=SEGMENT`/`USER_LIST` **chưa implement logic gửi** ở Phase 1 — API validate field bắt buộc tương ứng nhưng handler chỉ xử lý nhánh `ALL`; 2 nhánh còn lại trả **422 `NOTIFICATION_AUDIENCE_TYPE_NOT_SUPPORTED`** (theo bảng HTTP status chuẩn TradeX — không dùng 501) cho đến khi Phase 2/feature liên quan implement. |
| **BR-018** *(mới — hủy tin hẹn giờ)* | Chỉ hủy được record đang **SCHEDULED**. Thứ tự bắt buộc: gọi OneSignal Cancel **trước**, thành công mới update `cancelled_at`/`cancelled_by_admin_id`. OneSignal báo đã gửi → trả `NOTIFICATION_ALREADY_SENT`, không update gì. Tin đã hủy **không** hiển thị trong app-facing API (chưa từng gửi tới user). |
| **BR-019** *(mới — NH Research trigger)* | Publish NH Research + toggle ON → internal call gửi push (`NEWS`, `ALL`, template song ngữ, deeplink theo category). Push thất bại **không rollback** publish — hiển thị cảnh báo cho admin và cho gửi lại thủ công. Record tự động ghi `t_notification` với `created_by_admin_id` = admin publish. |
| **BR-020** *(mới — template)* | Composer prefill template song ngữ theo loại tin (bảng Template ở trên) — admin sửa tự do trước khi gửi. Template chỉ là gợi ý phía UI, backend không validate nội dung theo template. |
| **BR-021** *(mới — whitelist tag `htmlContent`)* | Sanitizer server-side (BE) chỉ giữ lại **2 tag**: `<b>` (highlight số liệu/từ khóa) và `<br>` (xuống dòng giữa các bullet). Mọi tag khác (`<script>`, `<a>`, `<img>`, `<ul>/<li>`, `<div>`, style/class attribute...) bị strip hoàn toàn — kể cả khi WYSIWYG editor phía FE sinh ra. Bullet trong rich body dùng **ký tự emoji** (👉 🔎 ☝️ ✅ ❌) đặt đầu dòng, không dùng list tag HTML (khớp 4 mẫu ở mục "Mẫu thông báo hiển thị trong app"). App-side render `htmlContent` phải map đúng 2 tag này sang style native (bold text, line break) — không dùng WebView render HTML thô. |
| **BR-022** *(mới — filter khoảng ngày app-facing)* | `GET /api/v1/notifications` nhận thêm 2 query param tuỳ chọn `fromDate`/`toDate` (ISO8601 date, vd `2026-05-11`) để lọc theo `sentAt` — dùng cho UI lọc khoảng ngày kiểu "11/05/2026 - 10/07/2026" (style tham chiếu SSI iBoard). Không truyền → mặc định trả toàn bộ lịch sử (giới hạn bởi phân trang `fetchCount`/`nextKey`). `toDate` < `fromDate` → 400 `INVALID_PARAMETER`. |

---

## Convention Check

| Mục | Kết quả |
|---|---|
| URL naming (camelCase) | ✅ PASS |
| Integration Type khai báo | ✅ PASS — TradeX-native |
| Response mutation `{ id }` | ✅ PASS (đã sửa từ `notificationId` trong draft) |
| PK type | ✅ PASS — `bigint autoIncrement`, khớp entity JHipster hiện có (đã sửa từ `VARCHAR(36)` UUID trong draft) |
| DB migration | ✅ PASS — Liquibase changeSet, không dùng raw SQL |
| Error format 400 | ✅ PASS — `INVALID_PARAMETER` |
| Language handling | ✅ PASS — `Accept-Language` cho app-facing; admin-facing luôn trả cả 2 ngôn ngữ (có chủ đích, đã ghi chú) |

---

## Open Questions

| # | Câu hỏi | Owner |
|---|---|---|
| Q1 | 4 API app-facing (`/api/v1/notifications*`) — `nhsv-admin` lộ trực tiếp cho app, hay qua `rest-proxy` như các domain khác? Ảnh hưởng đăng ký scope ở `configuration` service (`t_scope`). | BE Lead |
| Q2 | Khi `audienceType=USER_LIST` được implement (Event Calendar, Phase sau) — endpoint bị gọi **service-to-service** (cron job), không phải admin bấm tay. Cơ chế auth nào (service account/internal token) thay JWT admin thông thường? *(NH Research KHÔNG bị ảnh hưởng — cùng service, internal call, đã giải quyết ở section Trigger tự động.)* | BE Lead + IT |
| Q3 | Xác nhận bảng `t_notification` gốc (theo schema webhook cũ NHMTS-870/871) đã tồn tại ở DEV/UAT chưa — quyết định `createTable` mới hay cần migrate dữ liệu cũ. | Midu + BE |
| Q4 | **Convention API admin-facing:** spec này theo chuẩn TradeX (`/api/v1/admin/notifications`, camelCase, response không envelope). Nhưng API admin hiện có trong `nhsv-admin` (vd NH Research: `/api/admin/nh-research`) dùng **kebab-case + envelope `GenericResponse { status, message, data, pageData }`** — FE `nhsv-admin-fe` đã viết API client theo pattern đó. Cần chốt: 3 API admin-facing mới theo chuẩn TradeX hay theo pattern nội bộ nhsv-admin? (4 API app-facing bắt buộc theo chuẩn TradeX vì phục vụ mobile app.) | Midu + BE Lead |

---

Document Status: 📋 Draft | For: BE Developer, FE Developer | Next Steps: Midu review → tạo Jira subtask dưới Epic NHMTS-74; BE Lead trả lời Q1–Q4 trước khi estimate
