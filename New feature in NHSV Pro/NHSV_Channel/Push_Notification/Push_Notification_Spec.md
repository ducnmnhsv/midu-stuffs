# NHSV Channel — Push Notification Spec

## Scope

Thông báo khi admin publish bài mới trong NHSV Channel — NH Research và Khuyến nghị. Trigger từ Admin Tool (X-01) thông qua nút "Publish & Notify".

---

## Trigger Types

| Trigger ID | Nguồn | Điều kiện | Thời điểm gửi |
|---|---|---|---|
| `TRIGGER_NH_RESEARCH_PUBLISH` | Admin Tool — NH Research | Admin nhấn Publish & Notify | Ngay khi publish |
| `TRIGGER_KHUYEN_NGHI_PUBLISH` | Admin Tool — Khuyến nghị | Admin nhấn Publish & Notify | Ngay khi publish |

---

## Notification Templates

> **Cập nhật 2026-07-09:** template NH Research đã được chốt **song ngữ** và chuyển vào spec nền chung `NHMTS-88 Store push notification/Admin_Notification_API_Spec.md` (section "Trigger tự động — NH Research publish"). Bảng dưới là bản tham chiếu.

### TRIGGER_NH_RESEARCH_PUBLISH (đã chốt — song ngữ)

```
Title VI: Báo cáo mới từ NH Research
Title EN: New report from NH Research
Body VI:  {title} — {categoryVi}. Nhấn để đọc ngay trên NHSV Pro.
Body EN:  {title} — {categoryEn}. Tap to read on NHSV Pro.
Category: THI_TRUONG → Thị trường/Market · DOANH_NGHIEP → Doanh nghiệp/Company · VI_MO → Vĩ mô/Macro
```

### TRIGGER_KHUYEN_NGHI_PUBLISH

```
Title: Khuyến nghị mới từ NHSV
Body:  {title} — {subTab: Danh mục cơ bản | Kỹ thuật hằng ngày}
```

---

## Deeplink

| Trigger | Deeplink | Màn hình đích |
|---|---|---|
| `TRIGGER_NH_RESEARCH_PUBLISH` | `nhsvpro://channel/nh-research?category={category}` | A-04 NH Research |
| `TRIGGER_KHUYEN_NGHI_PUBLISH` | `nhsvpro://channel/khuyen-nghi?tab={subTab}` | A-05 Khuyến nghị |

FE cần parse query params `category` và `tab` để navigate đến đúng sub-tab.

---

## Admin Tool Integration

Thêm vào form upload của NH Research và Khuyến nghị:
- Toggle "Gửi push notification khi publish" (default: ON)
- Preview notification text trước khi submit
- Sau publish thành công: hiển thị "Đã gửi notification đến X thiết bị"

API / cơ chế (đã chốt 2026-07-09 — theo NHMTS-88):

```
NH Research publish (toggle ON)
  → internal call NotificationSendService trong cùng service nhsv-admin
  → notificationType=NEWS, audienceType=ALL (Phase 1 gửi tất cả subscriber)
  → ghi t_notification, hiện trong "Lịch sử đã gửi" của Admin Portal
```

- KHÔNG tạo endpoint riêng — dùng chung notification service của NHMTS-88 (`Admin_Notification_API_Spec.md`).
- Push thất bại không rollback publish — admin nhận cảnh báo, gửi lại thủ công từ composer (BR-019).
- Segment theo category (`userSegment`) dời Phase 2 — contract `audienceType=SEGMENT` đã chừa sẵn.

---

## FE — Deeplink Handler

Đăng ký 2 routes trong `App.tsx`:
- `nhsvpro://channel/nh-research` với param `category`
- `nhsvpro://channel/khuyen-nghi` với param `tab`

Hoạt động ở foreground, background, cold start.

Test cases tối thiểu:
- App foreground: nhận notification → tap → navigate đúng sub-tab
- App background: tap → foreground → navigate
- App closed (cold start): tap → app mở → navigate
- Query params được parse đúng

---

## Open Questions

| # | Câu hỏi | Owner |
|---|---|---|
| Q1 | ~~FCM đã tích hợp chưa?~~ → **Đã chốt: dùng OneSignal** qua notification service NHMTS-88 | ~~IT/BE~~ Đã đóng |
| Q2 | ~~User segment: tất cả user hay theo category preference?~~ → **Đã chốt: Phase 1 gửi tất cả (audienceType=ALL)**, segment theo category dời Phase 2 | ~~PM + BE~~ Đã đóng |
| Q3 | Rate limit per ngày? | IT |

---

## Dependencies

```
NHSV_Channel/Push_Notification
  ← Admin_Tool (cần Publish & Notify UI)
  → A-04 NH Research (content publish event)
  → A-05 Khuyến nghị (content publish event)
  ←→ Event_Calendar/Push_Notification (chung BE notification service)
```

---

Document Status: 📋 Draft | For: IT/BE, Mobile FE, Admin FE | Next Steps: Confirm Q1–Q3 trước khi estimate
