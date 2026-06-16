# Event Calendar — Push Notification Spec

## Scope

Thông báo nhắc nhở user trước ngày GDKHQ (giao dịch không hưởng quyền). Trigger duy nhất thuộc Event Calendar.

---

## Trigger

| Trigger ID | Điều kiện | Thời điểm gửi |
|---|---|---|
| `TRIGGER_GDKHQ_REMINDER` | Sự kiện GDKHQ sắp diễn ra | T-3 ngày (8:30 SA) |

---

## Notification Template

```
Title: Sắp đến ngày GDKHQ — {ticker}
Body:  {ticker} có ngày giao dịch không hưởng quyền vào {date}. Xem chi tiết lịch sự kiện.
```

---

## Deeplink

| Trigger | Deeplink | Màn hình đích |
|---|---|---|
| `TRIGGER_GDKHQ_REMINDER` | `nhsvpro://event-calendar` | Event Calendar (A-02) |

---

## BE — Scheduler Job

- Chạy hàng ngày lúc 8:00 SA
- Query events có `ex_date = today + 3 days`
- Gửi push đến toàn bộ user đã opt-in notification

FCM cho Android, APNs cho iOS.

---

## FE — Deeplink Handler

Đăng ký route `nhsvpro://event-calendar` trong `App.tsx` — hoạt động ở cả foreground, background, và cold start.

---

## Open Questions

| # | Câu hỏi | Owner |
|---|---|---|
| Q1 | NHSV Pro hiện đã tích hợp FCM chưa? | IT/BE |
| Q2 | User segment: tất cả user hay chỉ user đang hold mã GDKHQ? | PM + BE |
| Q3 | Rate limit: có giới hạn số notification gửi trong 1 ngày không? | IT |

---

## Dependencies

```
Event_Calendar/Push_Notification ←→ NHSV_Channel/Push_Notification
  (cùng dùng notification service; implement chung 1 lần ở BE)
```

---

Document Status: 📋 Draft | For: IT/BE, Mobile FE | Next Steps: Confirm Q1–Q3 trước khi estimate
