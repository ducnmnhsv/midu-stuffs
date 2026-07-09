# Validator Report — NHMTS-88 (Admin Notification)

**Ngày:** 2026-07-09
**Kết quả:** PASS_WITH_WARNINGS (sau khi tự sửa 4 lỗi)

## Chi tiết kiểm tra

### Admin_Notification_API_Spec.md

- [FAIL→FIXED] URL naming: `PATCH /api/v1/notifications/read-all` dùng kebab-case — đã sửa thành `readAll` (cả bảng endpoint và ví dụ response).
- [WARN→FIXED] Thiếu bảng DTO naming `{Resource}{Action}Request/Response` — đã thêm (`NotificationSendRequest`, `NotificationSendResponse`, ...).
- [WARN→FIXED] Section "Trường tự động điền" chỉ là 1 dòng text — đã format thành bảng đúng template (field/nguồn/ghi chú).
- [WARN→FIXED] BR-017 dùng HTTP `501` — không nằm trong bảng HTTP status chuẩn TradeX (200/400/401/403/404/422/500) — đã đổi thành 422 `NOTIFICATION_AUDIENCE_TYPE_NOT_SUPPORTED`.
- [PASS] URL còn lại: camelCase, `/api/v1/...` ✅
- [PASS] Integration Type label: khai báo rõ TradeX-native ngay đầu doc ✅
- [PASS] Mutation response: `{ id }` là field chính (field phụ `status`/`recipientsEstimate` — deviation có chủ đích, đã ghi chú trong spec) ✅
- [PASS] Query response: `{ totalCount, notifications }` đúng envelope chuẩn ✅
- [PASS] Error 400: `INVALID_PARAMETER` + params ✅; 422 tradex-native SCREAMING_SNAKE_CASE + messageParams ✅
- [PASS] Footer C5 ✅

### FE_Admin_Notification_Issue.md

- [PASS] Tiêu đề có `[FE]` ✅
- [PASS] Footer C5 ✅
- [PASS-INTENTIONAL] Không có đường dẫn `src/...` — deviation có chủ đích theo user feedback (`feedback_fe_issue_format`: FE issue viết theo góc nhìn PO, không nêu code/file path). User rule override checklist.
- [PASS] Nhất quán với spec: endpoint (`POST .../send`, `GET .../notifications`, `GET .../{id}`), field (`fetchCount`/`nextKey`, `type`/`status`), error codes (`INVALID_PARAMETER`, `NOTIFICATION_SEND_FAILED`), BR-008/BR-009 phản ánh đúng trong luồng UI ✅

## Đã sửa trong spec

1. `read-all` → `readAll` (2 chỗ)
2. Thêm bảng "Trường tự động điền"
3. Thêm bảng DTO Naming
4. BR-017: 501 → 422 `NOTIFICATION_AUDIENCE_TYPE_NOT_SUPPORTED`

## Vấn đề còn lại (cần user/BE Lead xác nhận — đã nằm trong Open Questions của spec)

- Q4: convention API admin-facing (TradeX chuẩn vs `GenericResponse` envelope của nhsv-admin hiện tại) — quyết định này có thể thay đổi toàn bộ response format của 3 API admin-facing trong spec.
- Q1–Q3: owner API app-facing, auth service-to-service, migrate schema cũ.

## File đã kiểm tra

- `NHMTS-88 Store push notification/Admin_Notification_API_Spec.md`
- `NHMTS-88 Store push notification/FE_Admin_Notification_Issue.md`
