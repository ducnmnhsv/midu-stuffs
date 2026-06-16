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

### TRIGGER_NH_RESEARCH_PUBLISH

```
Title: Báo cáo mới từ NH Research
Body:  {title} — {category: Thị trường | Doanh nghiệp | Vĩ mô}
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

API:

```
POST /admin/notifications/send
  Body: { triggerType, payload, userSegment? }
  Auth: Admin only
```

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
| Q1 | FCM đã tích hợp chưa? (chung với Event Calendar) | IT/BE |
| Q2 | User segment: tất cả user hay theo category preference? | PM + BE |
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
