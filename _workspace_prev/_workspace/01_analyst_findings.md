# Analyst Findings: Send Notification from Admin Portal — Phase 1 (NHMTS-88)

**Analyst:** Claude (tradex-analyst)
**Date:** 2026-07-09
**Sources verified (code, not assumption):**
- `Knowledge/TradeX-MCP/notification-main/src/main/java/com/techx/tradex/notification/services/OneSignalService.java`
- `Knowledge/TradeX-MCP/notification-main/src/main/java/com/techx/tradex/notification/controllers/RequestHandler.java`
- `Knowledge/TradeX-MCP/notification-main/src/main/resources/application.yaml`
- `Knowledge/TradeX-MCP/nhsv-admin/src/main/java/com/difisoft/nhsv/admin/consumers/RequestHandler.java`
- `Knowledge/TradeX-MCP/nhsv-admin/src/main/java/com/difisoft/nhsv/admin/service/RequestSenderService.java`
- `Knowledge/TradeX-MCP/nhsv-admin/src/main/resources/config/liquibase/changelog/` (naming convention)
- `Knowledge/TradeX-MCP/rest-proxy-main/src/app/routes/mts/News.ts`

## Tổng quan API (spec đã soạn)
- Endpoint mới: `POST /api/v1/admin/notifications/send` + 2 GET + 3 API app-facing
- Kafka Topic theo spec: **N/A** — spec giả định nhsv-admin gọi OneSignal API trực tiếp (HTTP)
- Nguồn: khớp với draft spec người dùng cung cấp (chưa merge vào Knowledge/)

## ⚠️ Phát hiện quan trọng — Có hệ thống OneSignal integration ĐÃ TỒN TẠI, spec đang đề xuất trùng lặp

### 1. `notification-main` đã là điểm tích hợp OneSignal duy nhất của TradeX
- Service Java (`spring.application.name: notification`, Kafka topic **`notification`**) đã có `OneSignalService.sendNotification()` gọi `OneSignal.createNotification(apiKey, request)` — đúng OneSignal API mà spec Admin Portal định gọi lại từ đầu.
- `application.yaml` đã có sẵn credential cho app NHSV Pro: `oneSignalMap.nhsv.{appId,apiKey}` — đây chính là OneSignal App ID/API Key dùng cho notification tới NHSV Pro subscribers.
- **Hệ quả:** nếu nhsv-admin BE gọi OneSignal trực tiếp như spec vẽ, TradeX sẽ có **2 nơi giữ cùng 1 OneSignal API Key** (`notification-main` và `nhsv-admin`) — rủi ro bảo mật/khó xoay key, lệch kiến trúc hiện có (mọi service khác đều đi qua Kafka topic `notification`, không service nào gọi OneSignal trực tiếp).

### 2. `nhsv-admin` đã có sẵn Kafka producer — route qua `notification-main` là khả thi, không phải xây mới
- `nhsv-admin` đã có `RequestSenderService` + `consumers/RequestHandler.java` + dependency `kafka-clients` (dùng cho CopyTrading OTP hiện tại) → nhsv-admin **đã biết cách gửi Kafka message sang service khác**, không cần thêm hạ tầng Kafka mới.
- Đường đi đúng chuẩn kiến trúc: `nhsv-admin (Admin Portal BE) → Kafka topic "notification" (method=ONESIGNAL) → notification-main → OneSignal`, giống mọi luồng OneSignal khác trong hệ thống hiện nay (order_match, alarm, stop_order_activation, smart_otp).

### 3. NHƯNG `OneSignalService` hiện tại chỉ hỗ trợ template `.ftl` cố định, không hỗ trợ nội dung admin tự soạn + broadcast toàn bộ subscriber
- Nội dung push hiện tại luôn render từ file `.ftl` (FreeMarker) theo `template` key cố định trong `templatesMap.tradex` (`order_match_notification`, `alarm_notification`, `stop_order_activation_notification`, `stop_order_activation_failed_notification`, `smart_otp_notification`) — không có cơ chế nhận `title`/`body` tự do do admin nhập tay.
- `filters` trong `OneSignalConfiguration` hiện tại được dùng để target **1 user cụ thể** (`filter: userid = X`) hoặc theo `partner_kis` — **không có code path nào set `included_segments: ["Total Subscriptions"]`** để broadcast toàn bộ app. Đây là field mới hoàn toàn cần thêm.
- → Muốn giữ đúng kiến trúc (đi qua `notification-main`), **BE phải sửa `notification-main`**: thêm 1 method mới (ví dụ `ONESIGNAL_BROADCAST`) hoặc mở rộng `NotificationMessage`/`OneSignalConfiguration` để nhận raw `headings`/`contents` theo 2 ngôn ngữ trực tiếp từ Kafka message (không qua `.ftl`), và set `included_segments`.

### 4. Cơ chế "lưu lại lịch sử notification để user đọc lại" đã có tiền lệ — nhưng cho hệ thống cũ "paave", không dùng lại được
- Sau khi gọi OneSignal thành công, `OneSignalService` forward qua Kafka topic `paave-notification` → `internal:/api/v1/notification/storeNotification` để lưu cho **hệ thống "paave" cũ** (không phải NHSV Pro / không có trong TradeX-MCP hiện tại — đây là sản phẩm tiền nhiệm, đã được ghi chú trong CLAUDE.md là dễ nhầm với `lotte-bridge`).
- → Không tái sử dụng được cho NHSV Pro. `t_notification`/`t_notification_recipient_state` theo spec vẫn là thiết kế mới, đúng đắn, chỉ là vị trí đặt (DB nào, service nào ghi) cần chốt lại theo kiến trúc ở mục 5.

### 5. `nhsv-admin` chưa có bất kỳ notification entity/migration nào
- Không tìm thấy Liquibase changelog, domain entity, hay REST resource nào liên quan notification trong `nhsv-admin` hiện tại (chỉ có `notification-middleware.ts` ở frontend — là toast/redux middleware cho UI admin, không liên quan push).
- `nhsv-admin` dùng JHipster + Liquibase, changelog đặt tên theo Jira ticket (`NHSV-XX.xml`) trong `src/main/resources/config/liquibase/changelog/` — 2 bảng mới (`t_notification`, `t_notification_recipient_state`) nên được viết dưới dạng **Liquibase changeSet** (ví dụ `NHMTS-88.xml`), không phải raw `CREATE TABLE` SQL script như bản draft.

### 6. Không liên quan (đã loại trừ)
- `/news/notification` trong `rest-proxy-main/src/app/routes/mts/News.ts` là API "đánh dấu đã xem tin thị trường" (market/stock news feed), khác hoàn toàn khái niệm push notification — không có xung đột.
- Webhook NHMTS-870/871 (nhận từ OneSignal Dashboard) chưa deploy, đúng như spec đã ghi nhận — không ảnh hưởng.

## Business Rules bị ảnh hưởng bởi phát hiện trên
- BR-011 (spec): "INSERT vào t_notification đồng bộ ngay sau khi OneSignal xác nhận" — nếu route qua Kafka/`notification-main` (bất đồng bộ theo `@Async`), nhsv-admin **không nhận được response đồng bộ từ OneSignal** để biết khi nào insert. Cần 1 trong 2:
  - (a) `notification-main` tự ghi kết quả gửi vào DB `nhsv-admin` (cross-service write — không đúng chuẩn service ownership), hoặc
  - (b) `notification-main` gửi response/callback về `nhsv-admin` qua Kafka request-response (cơ chế `requestResponseListener` đã có sẵn trong `application.yaml`: `${spring.application.name}.{uuid}`) để nhsv-admin insert sau khi nhận callback thành công.
  - (c) Hoặc **chấp nhận trade-off của spec gốc**: nhsv-admin gọi OneSignal trực tiếp (bỏ qua notification-main) — đơn giản hơn, giao hàng nhanh hơn, nhưng lệch kiến trúc và trùng credential như mục 1.

## Khuyến nghị cho quyết định PM/BE (không tự chọn thay)
Đây là quyết định kiến trúc cần Midu + BE lead chốt trước khi viết spec chi tiết:

| Phương án | Ưu điểm | Nhược điểm |
|---|---|---|
| **A. Đi qua `notification-main`** (đúng chuẩn kiến trúc hiện có) | Không trùng OneSignal credential; tái dùng retry/error-handling đã có; nhất quán với mọi luồng push khác | Phải sửa `notification-main` (thêm broadcast method + raw content), thêm callback/response flow để nhsv-admin biết khi nào insert `t_notification` — tốn thời gian BE hơn |
| **B. nhsv-admin gọi OneSignal trực tiếp** (đúng như draft spec hiện tại) | Nhanh, không đụng service Java khác | 2 nơi giữ OneSignal API Key; lệch kiến trúc so với toàn bộ hệ thống; nợ kỹ thuật cần dọn khi có Phase 2 (segment) |

## Việc cần chỉnh sửa trong TradeX nếu chọn Phương án A
1. `notification-main`: thêm `MethodEnum` mới (vd `ONESIGNAL_BROADCAST`) hoặc mở rộng `NotificationMessage`/`OneSignalConfiguration` để nhận `headings`/`contents` trực tiếp (không qua `.ftl`) + set `included_segments: ["Total Subscriptions"]`.
2. `notification-main`: thêm response callback về nhsv-admin qua `requestResponseListener` topic đã có sẵn (thay vì `@Async` fire-and-forget như hiện tại).
3. `nhsv-admin`: dùng `RequestSenderService` đã có sẵn để gửi Kafka message sang topic `notification`; thêm 2 bảng mới bằng Liquibase changelog (`NHMTS-88.xml`), theo đúng schema `t_notification`/`t_notification_recipient_state` trong spec.
4. `rest-proxy` / App-facing: cần route mới `/api/v1/notifications*` (app-facing) — kiểm tra service nào sẽ implement (nhsv-admin lộ API này trực tiếp cho app, hay qua rest-proxy như các domain khác) — spec hiện chưa nêu rõ service nào phục vụ 4 API app-facing (tab 4).

## Việc cần chỉnh sửa nếu chọn Phương án B (giữ spec gốc)
1. `nhsv-admin`: thêm OneSignal SDK/HTTP client + config `oneSignal.nhsv.{appId,apiKey}` riêng (trùng với `notification-main`).
2. `nhsv-admin`: 2 bảng mới bằng Liquibase changelog (như trên).
3. Không cần sửa `notification-main`.
4. Cùng câu hỏi mở về service nào phục vụ API app-facing như Phương án A.

## Convention Check
- Naming request/response: OK — theo đúng camelCase TradeX convention, không dùng field Lotte.
- DB migration: **VẤN ĐỀ** — spec dùng raw `CREATE TABLE`, nhsv-admin cần Liquibase changelog.
- Response format mutation (`{ notificationId, status, recipientsEstimate }`): lệch chuẩn tradex-native `{ id }` — nên xem lại theo `tradex-api-conventions.md` trước khi finalize (không thuộc phạm vi phân tích hệ thống này, cần đối chiếu ở bước tạo spec/creator).

---
**Document Status:** ✅ Complete | For: PM (Midu), BE Lead | Next Steps: Midu chọn Phương án A hoặc B ở bảng trên trước khi tạo Jira subtask BE
