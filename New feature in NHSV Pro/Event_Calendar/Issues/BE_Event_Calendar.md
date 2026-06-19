# [BE] Event Calendar — Lịch sự kiện cổ tức (A-02)

## Tóm tắt

Implement toàn bộ backend cho tính năng Event Calendar: bảng DB, cron job sync từ Vietstock, 3 API endpoint cho mobile app và IT monitoring, và cron job gửi push notification D-1 qua OneSignal.

---

## Task 1 — Database: Tạo 2 bảng + migration

**Bảng `event_calendar`** — lưu dữ liệu đã sync từ Vietstock:

| Column | Type | Ghi chú |
|---|---|---|
| event_id | BIGINT PK | Auto-increment |
| vietstock_event_id | INT UNIQUE | Dùng để UPSERT dedup |
| stock_code | VARCHAR(10) | Mã CK |
| company_name | VARCHAR(300) | Nullable |
| exchange | ENUM('HOSE','HNX','UPCOM') | |
| channel_id | INT | 13/14/15 raw từ Vietstock |
| event_type | ENUM('CASH_DIVIDEND','STOCK_DIVIDEND') | Derive từ channel_id |
| event_type_label | VARCHAR(100) | "Cổ tức tiền mặt" / "Cổ phiếu thưởng" / "Cổ tức bằng cổ phiếu" |
| gdkhq_date | DATE | KEY FIELD. Kiểu DATE (không giờ phút giây). Cần index. |
| ndkcc_date | DATE | Nullable |
| main_rate | DECIMAL(10,4) | Raw từ Vietstock |
| rate_display | VARCHAR(50) | Formatted theo event_type: CASH_DIVIDEND → "2,000đ/CP" · STOCK_DIVIDEND → "20%" |
| event_name | VARCHAR(500) | Nullable. Lưu raw Name từ Vietstock để debug |
| synced_at | DATETIME | Cập nhật mỗi lần upsert |
| created_at | DATETIME | Auto-generated |

Indexes: `gdkhq_date` (primary filter), `UNIQUE(vietstock_event_id)`, composite `(gdkhq_date, exchange)`.

**Bảng `stock_sync_tracker`** — tracking 6-month skip per mã CK:

| Column | Type | Ghi chú |
|---|---|---|
| stock_code | VARCHAR(10) PK | 1 record/mã |
| last_called_at | DATETIME | Thời điểm call Vietstock gần nhất |
| next_eligible_at | DATETIME | `= last_called_at + 6 MONTH`. Skip nếu `NOW() < next_eligible_at` |
| events_found | INT | Nullable. Số events tìm được ở lần call gần nhất |
| updated_at | DATETIME | Auto-updated |

---

## Task 2 — Cron job: Sync từ Vietstock (06:00 ICT hàng ngày)

Gọi Vietstock `GET /stockevents?Code={code}&EventTypeID=1&FromDate=today&ToDate=today+21&LanguageID=1` cho từng mã CK, lọc chỉ lấy `ChannelID IN (13, 14, 15)`, upsert vào `event_calendar`, và cleanup records cũ.

**Luồng chi tiết:**

1. Lấy danh sách mã cần sync: `LEFT JOIN stock_sync_tracker WHERE sst.stock_code IS NULL OR sst.next_eligible_at <= NOW()`. Mã trong cửa sổ 6 tháng bị skip hoàn toàn.
2. Gọi Vietstock concurrent (max 10 parallel). Lỗi từng mã log và tiếp tục, không dừng toàn bộ job. Retry toàn bộ job 3 lần nếu thất bại, cách nhau 10 phút.
3. Filter response: chỉ giữ `ChannelID IN (13, 14, 15)`. Parse `GDKHQDate` → `DATE`. Bỏ record có `GDKHQDate` null.
4. UPSERT vào `event_calendar`: `ON CONFLICT (vietstock_event_id) DO UPDATE SET ...`. Idempotent.
5. Cập nhật `stock_sync_tracker` sau mỗi call thành công (dù có hay không có events): `UPSERT SET last_called_at=NOW(), next_eligible_at=DATE_ADD(NOW(), INTERVAL 6 MONTH)`. Chỉ update khi HTTP 200 — nếu call lỗi không update tracker.
6. Cleanup: `DELETE FROM event_calendar WHERE gdkhq_date < CURDATE()`. Không xóa `stock_sync_tracker`.
7. Log: tổng mã, số bị skip, số được call, số events upserted, số lỗi, thời gian chạy. Alert nội bộ nếu tỷ lệ lỗi > 20%.

**ChannelID mapping:**

| ChannelID | event_type | event_type_label |
|---|---|---|
| 13 | CASH_DIVIDEND | Cổ tức tiền mặt |
| 14 | STOCK_DIVIDEND | Cổ phiếu thưởng |
| 15 | STOCK_DIVIDEND | Cổ tức bằng cổ phiếu |

**Format `rate_display` — đã xác nhận từ data thực (SSI + VIC):**

MainRate từ Vietstock là **decimal ratio**, không phải % hay VNĐ trực tiếp.

- `CASH_DIVIDEND` (ChannelID=13): `round(MainRate × 10,000)` = VNĐ/CP → format `"{n:,}đ/CP"`
  - SSI: MainRate=0.1 → 0.1 × 10,000 = **1,000đ/CP** ✅ (khớp Note: "1,000 đồng/CP")
- `STOCK_DIVIDEND` (ChannelID=14/15): `round(MainRate × 100)` = % → format `"{n}%"`
  - VIC: MainRate=1.0 → 1.0 × 100 = **100%** ✅ (khớp Note: "tỷ lệ 1:1")

Field `Note` từ Vietstock (VD: "1,000 đồng/CP", "tỷ lệ 1:1") dùng làm fallback debug nếu formula cho kết quả bất thường.

**Các fields mới cần lưu thêm từ response Vietstock:**

| Vietstock field | DB column | Ghi chú |
|---|---|---|
| NDKTHDate (hoặc Time) | ndkth_date | Ngày trả cổ tức thực tế. Nullable. Khác với NDKCCDate và GDKHQDate |
| Note | note | Text mô tả tỷ lệ đã format: "1,000 đồng/CP", "tỷ lệ 1:1". Fallback/debug |
| Title_Event | title_event | Tiêu đề sự kiện ngắn. Debug |
| FileUrl | file_url | URL PDF tài liệu chính thức. Nullable. Candidate v2 |
| Exchange | exchange | **Có sẵn trong response** — không cần derive từ bảng danh mục |

**Lưu ý:** `TODAY` là múi giờ server ICT (+7). Normalize `gdkhq_date` về `DATE` ngay khi nhận từ Vietstock để tránh lỗi cut-off nửa đêm.

**Tradeoff cần document:** Nếu doanh nghiệp công bố cổ tức bất thường trong cửa sổ 6 tháng kể từ lần sync trước, sự kiện sẽ không được phát hiện. Đây là đánh đổi có chủ ý để giảm API call. Xem Open Question Q1 và Q3 trong PRD.

---

## Task 3 — API: GET /eventCalendar/upcoming

Trả danh sách sự kiện cổ tức trong cửa sổ 14 ngày tới.

**Query params:** `exchange` (optional) — `HOSE | HNX | UPCOM`. Null hoặc không truyền = không filter.

**DB query:** `WHERE gdkhq_date >= CURDATE() AND gdkhq_date <= DATE_ADD(CURDATE(), INTERVAL 14 DAY)`. Nếu có exchange filter thêm `AND exchange = ?`. Sort: `gdkhq_date ASC, stock_code ASC`.

**Response 200:**
```
{
  "asOfDate": "YYYY-MM-DD",          // CURDATE() trên server — Mobile dùng để tính date header và badge
  "events": [Event],
  "totalCount": number
}
```

**Event object:**
```
{
  "eventId": string,
  "stockCode": string,
  "companyName": string | null,
  "exchange": "HOSE" | "HNX" | "UPCOM",
  "eventType": "CASH_DIVIDEND" | "STOCK_DIVIDEND",
  "eventTypeLabel": string,
  "gdkhqDate": "YYYY-MM-DD",
  "rateDisplay": string,
  "isToday": boolean               // gdkhq_date = CURDATE()
}
```

**Empty response:** trả `{"asOfDate":"...","events":[],"totalCount":0}` — không trả 404.

**Cache:** Response có thể cache 30 phút (TTL). Dữ liệu chỉ thay đổi sau sync job 06:00.

**Errors:** `401 UNAUTHORIZED`, `403 FORBIDDEN`, `500 INTERNAL_SERVER_ERROR`.

---

## Task 4 — API: GET /eventCalendar/{eventId} + Admin status endpoint

**GET /eventCalendar/{eventId}** — chi tiết một sự kiện (dùng cho detail screen và deep link từ notification).

Response gồm tất cả fields của Event object + `ndkccDate: string | null`.

Lỗi: `404 {"code":"OBJECT_NOT_FOUND"}` nếu eventId không tồn tại hoặc đã bị cleanup (gdkhqDate đã qua).

**GET /admin/sync/eventCalendar/status** — IT monitoring, không expose ra mobile app.

Response:
```
{
  "lastSyncAt": datetime,
  "totalEvents": number,
  "totalTracked": number,
  "totalSkipped": number,     // mã có next_eligible_at > NOW()
  "nextSyncAt": datetime,
  "lastError": string | null
}
```

Auth: admin role only.

---

## Task 5 — Cron job: Push notification qua OneSignal (07:50 ICT hàng ngày)

Query `WHERE gdkhq_date = CURDATE() + INTERVAL 1 DAY` để lấy danh sách sự kiện D-1. Với mỗi sự kiện, gửi notification qua OneSignal đến toàn bộ user opted-in (confirm segment với PM — xem Open Question Q5 trong PRD).

**Nội dung notification:**
- Title: `📅 Sự kiện ngày mai — {stockCode}`
- Body: `{companyName} thực hiện {eventTypeLabel} với tỷ lệ {rateDisplay}. GDKHQ: {gdkhqDate}.`

**Data payload (quan trọng cho FE deep link):** Đính kèm `eventId` vào OneSignal notification data object để FE có thể parse và navigate thẳng đến EventDetailScreen.

---

## Open Questions (từ PRD — cần xác nhận trước khi bắt đầu)

| # | Câu hỏi | Blocking |
|---|---|---|
| Q1 | Vietstock rate limit per minute/hour? Có bulk endpoint không? | Task 2 |
| Q2 | ✅ **Đóng** — data thực xác nhận. SSI (cash) MainRate=0.1 → ×10,000 → 1,000đ/CP. VIC (stock) MainRate=1.0 → ×100 → 100%. | — |
| Q3 | Sync toàn bộ ~1,700 mã hay subset (HOSE only, hoặc mã NHSV đang cover)? | Task 2 |
| Q5 | Notification: toàn bộ user opted-in hay chỉ user đang hold mã đó? | Task 5 |

---

Document Status: 📋 Draft | For: IT/BE Developer | Next Steps: Xác nhận Q1–Q3 với Vietstock trước khi estimate Task 2; PM xác nhận Q5 trước khi estimate Task 5
