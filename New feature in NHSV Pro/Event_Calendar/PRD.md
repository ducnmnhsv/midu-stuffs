# PRD — Lịch sự kiện (A-02)

**Product:** NHSV Pro · Mobile App
**Feature ID:** A-02
**PM:** Midu (Nguyễn Minh Đức)
**Status:** 📋 Draft
**Version:** 1.1 · 2026-06-19

---

## 1. Bối cảnh & Vấn đề

Nhà đầu tư cần theo dõi ngày giao dịch không hưởng quyền (GDKHQ) để ra quyết định mua/bán trước khi bị cắt quyền nhận cổ tức. Hiện tại NHSV Pro không cung cấp thông tin này trong app — user phải tra cứu thủ công trên Vietstock, CafeF, hoặc tự nhớ kiểm tra định kỳ.

Hậu quả: user bỏ lỡ cơ hội thu cổ tức vì không biết ngày GDKHQ đang đến gần, và phải rời app để tra cứu thông tin mà NHSV Pro có thể cung cấp trực tiếp. Đây là điểm yếu rõ ràng so với KIS, KBSV đều đã có lịch sự kiện in-app.

---

## 2. Mục tiêu

| Mục tiêu | Chỉ số đo lường |
|---|---|
| User nắm được sự kiện cổ tức sắp diễn ra ngay trong app | % user active xem ít nhất 1 sự kiện/tuần sau release |
| Giảm exit rate trên Home screen | Đo qua GA4 exit rate trước/sau 30 ngày release |
| Tăng open rate qua push notification | CTR notification Event Calendar vs notification hiện có |

---

## 3. Đối tượng người dùng

**Primary:** Retail investor trung và dài hạn đang hold cổ phiếu, quan tâm đến cổ tức. Cần biết ngày GDKHQ để quyết định giữ hay bán trước ngày chốt quyền.

**Secondary:** Day trader muốn tận dụng hiệu ứng giá xung quanh ngày GDKHQ.

---

## 4. Phạm vi v1

### Trong scope

- Hiển thị sự kiện cổ tức tiền mặt (cash dividend) và cổ phiếu thưởng / cổ tức bằng cổ phiếu
- Cửa sổ thời gian: hôm nay đến hôm nay + 14 ngày
- Sự kiện hôm nay được đánh dấu badge "HÔM NAY"
- Sort theo ngày GDKHQ tăng dần; trong cùng ngày sort theo mã A→Z
- Nhóm theo ngày (date grouping)
- Filter theo sàn: Tất cả / HOSE / HNX / Upcom
- Màn hình detail khi tap vào từng sự kiện
- BE sync job định kỳ từ Vietstock
- Tự động ẩn sự kiện đã qua ngày GDKHQ
- Push notification D-1 trước GDKHQ qua OneSignal

### Ngoài scope (v1)

- Tìm kiếm mã CK trong lịch sự kiện
- Lịch sử sự kiện đã qua
- Loại sự kiện khác: AGM, kết quả kinh doanh, phát hành thêm
- Manual override dữ liệu từ admin
- User tự opt-in/out theo từng mã cụ thể

---

## 5. Luồng người dùng

### Luồng 1 — Khám phá thụ động (golden path)

1. User mở NHSV Pro → vào Trang chủ
2. Thấy section "📅 Lịch sự kiện" ở vị trí thứ 3 trên Home (sau Hoạt động tích cực)
3. Lướt danh sách — các sự kiện nhóm theo ngày, sự kiện hôm nay nổi bật badge đỏ
4. Tap filter "HOSE" → danh sách re-filter chỉ còn mã HOSE
5. Tap vào event card → vào màn hình Chi tiết sự kiện
6. Xem đầy đủ thông tin (ngày GDKHQ, tỷ lệ, ngày ĐKCC)
7. Tap CTA "Xem cổ phiếu VCB →" → navigate đến màn hình giao dịch

### Luồng 2 — Từ push notification (D-1)

1. 08:00 sáng ngày T-1 trước GDKHQ, user nhận notification: "📅 Sự kiện ngày mai — VCB"
2. Tap notification → app mở thẳng màn hình Chi tiết sự kiện tương ứng
3. Hoạt động đúng ở cả 3 trạng thái: foreground, background, cold start

### Luồng 3 — Filter state persistence

1. User chọn filter "HNX" → tap vào sự kiện → vào detail screen → back
2. Filter "HNX" vẫn được giữ nguyên, không reset về "Tất cả"

---

## 6. Quy tắc hiển thị (Display Logic)

### Điều kiện hiển thị theo ngày GDKHQ

| Điều kiện | Kết quả |
|---|---|
| `gdkhq_date = TODAY` | Hiển thị · Badge đỏ "HÔM NAY" · Nằm ở section đầu tiên |
| `TODAY < gdkhq_date ≤ TODAY+14` | Hiển thị bình thường · Không có badge |
| `gdkhq_date > TODAY+14` | Ẩn · Không fetch về app |
| `gdkhq_date < TODAY` | Ẩn · Lọc ra khỏi API response, không bao giờ hiển thị lại |

> **TODAY** là múi giờ server ICT (+7). Mọi so sánh ngày dùng DATE trên server, không dùng UTC timestamp thô.

### Sắp xếp và nhóm

- **Sort chính:** `gdkhq_date ASC` — sự kiện gần nhất lên đầu
- **Sort phụ:** `stock_code ASC` — trong cùng ngày, mã A→Z
- **Nhóm theo ngày:** mỗi nhóm có date header theo format:
  - `gdkhq_date = TODAY` → header teal: **"HÔM NAY · DD/MM"**
  - `gdkhq_date = TODAY+1` → "Ngày mai · DD/MM"
  - Còn lại → xám: "Tx · DD/MM · N ngày nữa" (Tx = thứ trong tuần, N = số ngày còn lại tính từ asOfDate)

---

## 7. Screen Spec — Mobile App

### 7.1 Event Calendar (inline Home + standalone screen)

**Section header:** "📅 Lịch sự kiện" đặt ở vị trí thứ 3 trên Home screen (sau Hoạt động tích cực). Cũng có thể điều hướng đến EventScreen standalone từ navigation.

**Filter bar:** 4 pill buttons ngang. Mặc định: "Tất cả" (nền navy, text trắng). Các pill còn lại: nền xám nhạt, text xám. Khi chọn: active pill đổi nền navy. Hành vi khi switch sàn: gọi lại API với param `exchange` hoặc re-filter client-side từ data đã cache — Mobile team tự quyết approach tối ưu.

**Date group header:**

- Hôm nay: nền teal nhạt (#ccfbf1), text teal (#0e7490), bold: "HÔM NAY · DD/MM"
- Ngày mai và xa hơn: nền xám nhạt (#f1f5f9), text xám (#475569): "T6 · DD/MM · N ngày nữa"

**Event card (tappable):**

- Trái: ô mã chứng khoán (nền navy, text trắng, rounded, font bold)
- Giữa: `eventTypeLabel` tiếng Việt (bold, navy) · `gdkhqDate` format DD/MM/YYYY (xám)
- Phải: `rateDisplay` hiển thị theo loại sự kiện — CASH_DIVIDEND: "2,000đ/CP" (green, bold) · STOCK_DIVIDEND: "20%" (green, bold) · badge đỏ "HÔM NAY" nếu `isToday = true`
- Tap card → push navigate đến EventDetailScreen với `{ eventId }`

**States:**

- **Loading:** skeleton 3–4 card trong khi fetch API
- **Empty:** "Không có sự kiện cổ tức trong 14 ngày tới." (cả khi filter sàn không có kết quả)
- **Error:** "Không thể tải dữ liệu" + nút Thử lại. Lỗi chỉ ảnh hưởng section này, không crash Home

### 7.2 Event Detail Screen

**Navigation header:** nút back (←) + title "Chi tiết sự kiện" căn giữa.

**Hero card:**

- Ô mã CK: 36×36px, nền navy, text trắng, rounded 7px
- Tên công ty (nếu có), exchange badge (HOSE/HNX/Upcom)
- Divider, rồi: event type pill + tỷ lệ (`rateDisplay`) font lớn màu xanh lá

Event type pill theo `eventType`:

| eventType | Màu pill | Label |
|---|---|---|
| CASH_DIVIDEND | Teal (#ccfbf1 / #0e7490) | "Cổ tức tiền mặt" |
| STOCK_DIVIDEND | Violet (#ede9fe / #5b21b6) | Theo `eventTypeLabel`: "Cổ phiếu thưởng" hoặc "Cổ tức bằng cổ phiếu" |

**Thông tin ngày:**

- Ngày GDKHQ: format DD/MM/YYYY · kèm badge đỏ "HÔM NAY" nếu `isToday = true`
- Ngày ĐKCC: format DD/MM/YYYY · ẩn dòng này nếu `ndkccDate = null`

**Thông tin cổ tức:**

| Field | CASH_DIVIDEND | STOCK_DIVIDEND |
|---|---|---|
| Label tỷ lệ | "Tỷ lệ thực hiện" | "Tỷ lệ phát hành" |
| Giá trị `rateDisplay` | "2,000đ/CP" | "20%" |
| Loại cổ tức | "Tiền mặt" | Theo `eventTypeLabel` |
| Sàn giao dịch | HOSE / HNX / Upcom | HOSE / HNX / Upcom |

**Footer:** Disclaimer text nhỏ màu xám: "Dữ liệu từ Vietstock. Vui lòng xác nhận tại nguồn chính thức."

**CTA button:** "Xem cổ phiếu {stockCode} →" · nền navy · full-width · navigate đến màn hình giao dịch của mã đó

**Xử lý 404:** Khi API trả `OBJECT_NOT_FOUND` (sự kiện đã bị cleanup vì gdkhqDate đã qua), hiển thị "Sự kiện này đã kết thúc." + nút Quay lại.

**Navigation:** Push navigation từ list (không modal). Back về list giữ nguyên filter state.

---

## 8. Data Model — NHSV DB

### Bảng `event_calendar`

| Column | Type | Ghi chú |
|---|---|---|
| event_id | BIGINT PK | Auto-increment |
| vietstock_event_id | INT UNIQUE NOT NULL | Dùng để UPSERT dedup |
| stock_code | VARCHAR(10) NOT NULL | VD: "VCB", "ACB" |
| company_name | VARCHAR(300) | Nullable |
| exchange | ENUM('HOSE','HNX','UPCOM') NOT NULL | |
| channel_id | INT NOT NULL | 13/14/15 raw từ Vietstock |
| event_type | ENUM('CASH_DIVIDEND','STOCK_DIVIDEND') NOT NULL | Derive từ channel_id |
| event_type_label | VARCHAR(100) NOT NULL | "Cổ tức tiền mặt" / "Cổ phiếu thưởng" / "Cổ tức bằng cổ phiếu" |
| gdkhq_date | DATE NOT NULL | KEY FIELD. Kiểu DATE (bỏ time component) |
| ndkcc_date | DATE | Nullable |
| main_rate | DECIMAL(10,4) NOT NULL | Raw decimal từ Vietstock.MainRate. VD: 0.1 (cash SSI), 1.0 (stock VIC) |
| rate_display | VARCHAR(50) NOT NULL | Formatted theo event_type: CASH_DIVIDEND → "1,000đ/CP" (0.1×10,000) · STOCK_DIVIDEND → "100%" (1.0×100) |
| ndkth_date | DATE | Ngày thực hiện quyền (Vietstock field: NDKTHDate / Time). Nullable — không phải sự kiện nào cũng có. |
| note | VARCHAR(500) | Nullable. Vietstock.Note — text mô tả đã format: "1,000 đồng/CP", "tỷ lệ 1:1" |
| title_event | VARCHAR(500) | Nullable. Vietstock.Title_Event — tiêu đề ngắn của sự kiện |
| file_url | VARCHAR(1000) | Nullable. Vietstock.FileUrl — PDF tài liệu chính thức từ sàn |
| event_name | VARCHAR(500) | Nullable. Vietstock.Name để debug |
| synced_at | DATETIME NOT NULL | Cập nhật mỗi lần upsert |
| created_at | DATETIME NOT NULL | Auto-generated |

**Indexes cần thiết:** `gdkhq_date` (primary filter), `UNIQUE(vietstock_event_id)`, composite `(gdkhq_date, exchange)` (filter + sort).

### Bảng `stock_sync_tracker`

Theo dõi lịch sử call Vietstock theo từng mã — tránh gọi lại trong 6 tháng.

| Column | Type | Ghi chú |
|---|---|---|
| stock_code | VARCHAR(10) PK | 1 record/mã. PK đảm bảo không duplicate |
| last_called_at | DATETIME NOT NULL | Thời điểm call Vietstock gần nhất |
| next_eligible_at | DATETIME NOT NULL | `= last_called_at + 6 MONTH`. Sync job skip nếu `NOW() < next_eligible_at` |
| events_found | INT | Nullable. Số events tìm được ở lần call gần nhất (0 = gọi nhưng không có sự kiện) |
| updated_at | DATETIME NOT NULL | Auto-updated |

> **Tradeoff:** Nếu một doanh nghiệp công bố cổ tức bất thường trong cửa sổ 6 tháng kể từ lần sync trước, sự kiện sẽ không được phát hiện ngay. Đây là đánh đổi có chủ ý để giảm API call Vietstock. Nếu cần bắt trường hợp này, cần thêm cơ chế manual trigger (ngoài scope v1).

---

## 9. Vietstock API — Mapping & Sử dụng

### Endpoint Vietstock

```
GET https://api.vietstock.vn/demo/stockevents
  ?Code={mã_CK}
  &EventTypeID=1
  &FromDate={today}
  &ToDate={today+21}
  &LanguageID=1
```

- `EventTypeID=1`: "Dividend, bonus and additional issue" — bao gồm đủ 3 loại sự kiện cần hiển thị
- `ToDate = today+21`: buffer thêm 7 ngày so với cửa sổ hiển thị 14 ngày, tránh missing data khi sync chậm
- Yêu cầu từng mã CK — **không có bulk endpoint** theo khoảng ngày

### ChannelID → Loại sự kiện

| ChannelID | Mô tả Vietstock | event_type | event_type_label |
|---|---|---|---|
| 13 | Dividend payment in cash | CASH_DIVIDEND | Cổ tức tiền mặt |
| 14 | Bonus share issue | STOCK_DIVIDEND | Cổ phiếu thưởng |
| 15 | Dividend payment by share | STOCK_DIVIDEND | Cổ tức bằng cổ phiếu |

Chỉ giữ ChannelID 13/14/15. Bỏ các loại khác.

### Field Mapping Vietstock → NHSV DB

| Vietstock field | NHSV DB field | Xử lý |
|---|---|---|
| EventID | vietstock_event_id | Dùng để UPSERT (ON CONFLICT DO UPDATE) |
| Code | stock_code | Copy từ input param |
| Exchange | exchange | **Có sẵn trong response** — không cần derive từ bảng danh mục |
| ChannelID | channel_id + event_type + event_type_label | Mapping theo bảng ChannelID ở trên |
| GDKHQDate | gdkhq_date | Parse → DATE (bỏ time component). **Field quan trọng nhất** |
| NDKCCDate | ndkcc_date | Nullable |
| NDKTHDate (= Time) | ndkth_date | Ngày thực hiện quyền / ngày trả cổ tức thực tế. Nullable (VIC = null). Khác với NDKCCDate và GDKHQDate — là ngày tiền được trả về tài khoản. |
| MainRate | main_rate + rate_display | Lưu raw decimal vào main_rate. **Formula đã xác nhận từ data thực:** CASH_DIVIDEND → `round(MainRate × 10,000)` VNĐ/CP, format `"1,000đ/CP"` (SSI: 0.1 × 10,000 = 1,000). STOCK_DIVIDEND → `round(MainRate × 100)`%, format `"100%"` (VIC: 1.0 × 100 = 100%). |
| Note | note | Text mô tả tỷ lệ đã format sẵn từ Vietstock — "1,000 đồng/CP" (cash) hoặc "tỷ lệ 1:1" (stock). Lưu làm fallback/debug. |
| Title_Event | title_event | Tiêu đề sự kiện ngắn gọn — "Trả cổ tức năm 2024 bằng tiền, 1,000 đồng/CP". Lưu để debug/reference. |
| FileUrl | file_url | URL PDF tài liệu chính thức từ sàn. Nullable. Candidate hiển thị trong detail screen v2. |
| Name | event_name | Lưu nguyên văn để debug/reference |

---

## 10. BE — Sync Job

### Lịch chạy

- **Tần suất:** 1 lần/ngày lúc **06:00 AM ICT** (trước giờ mở cửa thị trường 9:00)
- **Trigger:** Cron job hoặc scheduler nội bộ
- **Retry:** Tự retry 3 lần nếu thất bại, cách nhau 10 phút
- **Alert:** Gửi thông báo nội bộ nếu sync thất bại hoàn toàn sau 3 lần retry

### Luồng xử lý từng bước

**Bước 1 — Lấy danh sách mã CK cần sync (có 6-month skip)**

Query bảng danh mục mã CK nội bộ, LEFT JOIN với `stock_sync_tracker`, chỉ lấy mã thỏa:
`WHERE sst.stock_code IS NULL OR sst.next_eligible_at <= NOW()`

Nghĩa là: mã chưa từng được gọi (`IS NULL`) hoặc mã đã hết hạn 6 tháng. Mã đang trong cửa sổ 6 tháng bị loại bỏ khỏi batch.

**Bước 2 — Gọi Vietstock cho từng mã đủ điều kiện**

Concurrent calls với giới hạn max 10 concurrent để tránh rate limit. Lỗi từng mã được log và tiếp tục — không dừng toàn bộ job.

**Bước 3 — Filter và transform**

Từ response, chỉ giữ records có `ChannelID IN (13, 14, 15)`. Parse `GDKHQDate` thành `DATE`. Format `rate_display` từ `MainRate`. Bỏ record có `GDKHQDate` null hoặc không parse được.

**Bước 4 — UPSERT vào event_calendar**

`INSERT INTO event_calendar ... ON CONFLICT (vietstock_event_id) DO UPDATE SET ...`

Idempotent — chạy lại nhiều lần không tạo duplicate. Cập nhật `synced_at = NOW()` mỗi lần upsert.

**Bước 5 — Cập nhật stock_sync_tracker**

Sau khi call Vietstock thành công (dù có hay không có events):
`UPSERT SET last_called_at=NOW(), next_eligible_at=DATE_ADD(NOW(), INTERVAL 6 MONTH)`

**Quan trọng:** Chỉ update tracker khi call thành công HTTP 200. Nếu call lỗi → không update tracker → mã được thử lại ở lần sync tiếp theo.

**Bước 6 — Cleanup records cũ**

`DELETE FROM event_calendar WHERE gdkhq_date < CURDATE()`

Không xóa `stock_sync_tracker` — bảng này cần giữ để skip logic hoạt động đúng.

**Bước 7 — Ghi log và kiểm tra**

Log: tổng mã trong danh mục, tổng bị skip (6-month), tổng được call, tổng events upserted, tổng lỗi, thời gian chạy.
Alert nếu tỷ lệ lỗi > 20% tổng mã được call.
Expose trạng thái qua `GET /admin/sync/eventCalendar/status`.

---

## 11. API Specification

### GET /api/v1/eventCalendar/upcoming

Lấy danh sách sự kiện cổ tức trong 14 ngày tới. Auth: Bearer token.

**Query Parameters**

| Param | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| exchange | string | Không | Filter theo sàn: `HOSE` / `HNX` / `UPCOM`. Null hoặc không truyền = không filter (Tất cả) |

**BE Query:**
`WHERE gdkhq_date >= CURDATE() AND gdkhq_date <= DATE_ADD(CURDATE(), INTERVAL 14 DAY)`
Nếu có exchange: `AND exchange = ?`
Sort: `gdkhq_date ASC, stock_code ASC`

**Response 200**

```json
{
  "asOfDate": "2026-06-19",
  "events": [
    {
      "eventId": "12345",
      "stockCode": "VCB",
      "companyName": "Ngân hàng TMCP Ngoại thương Việt Nam",
      "exchange": "HOSE",
      "eventType": "CASH_DIVIDEND",
      "eventTypeLabel": "Cổ tức tiền mặt",
      "gdkhqDate": "2026-06-19",
      "rateDisplay": "15%",
      "isToday": true
    }
  ],
  "totalCount": 1
}
```

- `asOfDate`: CURDATE() trên server — Mobile dùng để tính date header và badge "HÔM NAY"
- `isToday`: `true` khi `gdkhq_date = CURDATE()` — Mobile dùng để show badge đỏ
- **Empty response:** trả `{ "asOfDate":"...", "events":[], "totalCount":0 }` — không trả 404
- **Cache:** Response có thể cache 30 phút (TTL). Dữ liệu chỉ thay đổi sau sync job 06:00

**Error Responses**

| Code | Mô tả |
|---|---|
| 401 | `{"code":"UNAUTHORIZED"}` — Token hết hạn hoặc không có |
| 403 | `{"code":"FORBIDDEN"}` — Không có quyền truy cập |
| 500 | `{"code":"INTERNAL_SERVER_ERROR"}` — Lỗi server / DB |

---

### GET /api/v1/eventCalendar/{eventId}

Lấy chi tiết một sự kiện. Dùng cho màn hình detail và deep link từ notification. Auth: Bearer token.

**Path Parameter:** `eventId` (string) — ID nội bộ của sự kiện, lấy từ list response.

**Response 200**

```json
{
  "eventId": "12345",
  "stockCode": "VCB",
  "companyName": "Ngân hàng TMCP Ngoại thương Việt Nam",
  "exchange": "HOSE",
  "eventType": "CASH_DIVIDEND",
  "eventTypeLabel": "Cổ tức tiền mặt",
  "gdkhqDate": "2026-06-19",
  "ndkccDate": "2026-06-14",
  "rateDisplay": "15%",
  "isToday": true
}
```

- `ndkccDate`: nullable — FE ẩn dòng Ngày ĐKCC nếu giá trị này là null

**Error Responses**

| Code | Mô tả |
|---|---|
| 404 | `{"code":"OBJECT_NOT_FOUND"}` — eventId không tồn tại hoặc đã bị cleanup (gdkhqDate đã qua) |
| 401 | `{"code":"UNAUTHORIZED"}` |
| 500 | `{"code":"INTERNAL_SERVER_ERROR"}` |

---

### GET /admin/sync/eventCalendar/status

Kiểm tra trạng thái sync job. **Nội bộ IT monitoring — không expose ra mobile app.** Auth: admin role only.

**Response 200**

```json
{
  "lastSyncAt": "2026-06-19T06:02:14+07:00",
  "totalEvents": 142,
  "totalTracked": 1247,
  "totalSkipped": 1089,
  "nextSyncAt": "2026-06-20T06:00:00+07:00",
  "lastError": null
}
```

| Field | Mô tả |
|---|---|
| lastSyncAt | Lần sync thành công gần nhất |
| totalEvents | Số records hiện tại trong event_calendar |
| totalTracked | Tổng mã đã được call Vietstock (có trong stock_sync_tracker) |
| totalSkipped | Số mã bị skip vì next_eligible_at > NOW() |
| nextSyncAt | Lần sync tiếp theo theo lịch cron |
| lastError | Error message gần nhất nếu có, null nếu không có lỗi |

---

## 12. Push Notification

### Trigger và lịch gửi

- **Thời điểm gửi:** D-1 (ngày hôm trước GDKHQ), lúc **08:00 ICT**
- **Cron job:** Chạy lúc 07:50 ICT hàng ngày, query `WHERE gdkhq_date = CURDATE() + INTERVAL 1 DAY`
- **Platform:** OneSignal (đã tích hợp sẵn trong NHSV Pro — xác nhận với IT)
- **Segment:** Toàn bộ user opted-in (xem Open Q #5)

### Nội dung notification

| Field | Nội dung |
|---|---|
| Title | `📅 Sự kiện ngày mai — {stockCode}` |
| Body | `{companyName} thực hiện {eventTypeLabel} với tỷ lệ {rateDisplay}. GDKHQ: {gdkhqDate format DD/MM/YYYY}.` |

Ví dụ cash dividend: "VCB thực hiện Cổ tức tiền mặt với tỷ lệ 2,000đ/CP. GDKHQ: 20/06/2026."

Ví dụ stock dividend: "REE thực hiện Cổ phiếu thưởng với tỷ lệ 20%. GDKHQ: 20/06/2026."

### Deep link từ notification

**Data payload OneSignal** phải kèm `eventId` để FE navigate đúng:

```json
{
  "eventId": "12345"
}
```

**Deep link URL:** `nhsvpro://event-calendar/{eventId}`

FE đăng ký route này trong `Linking.tsx` — parse `eventId` → mở thẳng `EventDetailScreen`. Hoạt động đúng ở cả 3 trạng thái: foreground, background, cold start.

---

## 13. FE — Yêu cầu kỹ thuật

### Hiện trạng codebase

| File | Trạng thái |
|---|---|
| `src/screens/EventScreen/index.tsx` | Scaffold sẵn, hardcoded data, chưa có API call |
| `src/screens/EventDetailScreen/index.tsx` | Placeholder, nhận `{ title, content }` thô |
| `src/navigation/ScreenNames.ts` | `EventScreen`, `EventDetailScreen` đã đăng ký |
| `src/navigation/ScreenParamList.ts` | `EventDetailScreen` nhận `{ title, content }` — cần đổi sang `{ eventId: string }` |
| `src/navigation/Linking.tsx` | Chưa có config deep link cho EventScreen/EventDetailScreen |
| Redux | Chưa có slice/saga nào cho Event Calendar |

### Việc cần làm

**Redux + API:** Tạo slice và saga cho Event Calendar. State cần quản lý: danh sách events, loading/error, `asOfDate`, và exchange filter đang chọn. Gọi `GET /eventCalendar/upcoming` cho list, `GET /eventCalendar/{eventId}` cho detail.

**EventScreen:** Thay hardcoded data bằng data từ Redux. Thêm filter bar (4 pills), date group header theo logic ở mục 6, event card theo spec ở mục 7.1. Cập nhật `ScreenParamList.ts` để `EventDetailScreen` nhận `{ eventId: string }`.

**Event Calendar section trên HomeTab:** Tái sử dụng data và components từ EventScreen. Đặt ở vị trí thứ 3 trên Home. Lỗi API chỉ ảnh hưởng section này.

**EventDetailScreen:** Implement full UI theo spec ở mục 7.2. Gọi `GET /eventCalendar/{eventId}` khi mount. Xử lý 404.

**Deep link:** Config `Linking.tsx` cho `nhsvpro://event-calendar/{eventId}`. Handle cả 3 trạng thái app.

**Filter state:** Lưu filter state trong Redux slice (không dùng local state để tránh mất khi unmount). Khi back từ detail về list, filter giữ nguyên.

---

## 14. Work Breakdown

### Backend (5 tasks)

| Task | Mô tả |
|---|---|
| BE-01 | Tạo bảng event_calendar + stock_sync_tracker + migration |
| BE-02 | Vietstock sync job: 7 bước, 6-month skip, concurrent call, retry, alert |
| BE-03 | API GET /eventCalendar/upcoming: filter, sort, isToday, cache 30 phút |
| BE-04 | API GET /eventCalendar/{eventId} + GET /admin/sync/eventCalendar/status |
| BE-05 | Push notification job qua OneSignal: D-1 query, template, eventId trong payload |

### Mobile FE (6 tasks)

| Task | Mô tả |
|---|---|
| MOB-01 | Redux slice + saga + API client cho 2 endpoint |
| MOB-02 | EventScreen: filter bar, date grouping, event card, loading/empty/error states |
| MOB-03 | Event Calendar section inline trên HomeTab |
| MOB-04 | EventDetailScreen: full UI, 2 variant, CTA, 404 handling |
| MOB-05 | Deep link handler trong Linking.tsx cho nhsvpro://event-calendar/{eventId} |
| MOB-06 | Filter state persistence qua Redux khi navigate back từ detail |

---

## 15. Open Questions — Cần xác nhận trước khi estimate

| # | Câu hỏi | Owner | Mức độ |
|---|---|---|---|
| Q1 | Vietstock API rate limit per minute/hour? Có bulk endpoint trả events theo khoảng ngày cho toàn thị trường không? | BE Lead + IT | Blocking BE-02 |
| Q2 | ✅ **Đã giải quyết từ data thực.** MainRate là decimal ratio — CASH_DIVIDEND: `MainRate × 10,000 = VNĐ/CP` (SSI: 0.1 × 10,000 = 1,000đ/CP) · STOCK_DIVIDEND: `MainRate × 100 = %` (VIC: 1.0 × 100 = 100%). Field `Note` và `Title_Event` cũng chứa text mô tả đã format sẵn để fallback nếu cần. | — | Đóng |
| Q3 | Sync toàn bộ ~1,700 mã HOSE+HNX+Upcom, chỉ HOSE (~400 mã), hay danh sách mã NHSV đang cover? | PM + IT | Non-blocking nhưng ảnh hưởng performance sync job |
| Q4 | OneSignal đã được tích hợp sẵn trong NHSV Pro chưa? | IT | Blocking BE-05 và MOB-05 |
| Q5 | Notification: gửi toàn bộ user opted-in hay chỉ user đang hold mã sắp có GDKHQ? | PM | Blocking BE-05 |

---

## 16. Dependencies

- Vietstock API: cần xác nhận quyền truy cập và rate limit (Q1) trước khi estimate BE-02
- OneSignal: cần xác nhận tích hợp với IT (Q4) trước khi estimate BE-05 và MOB-05
- NHSV Channel Push Notification: cùng dùng notification service — nên implement chung một lần ở BE

---

Document Status: 📋 Draft | For: IT/BE, Mobile FE | Next Steps: Xác nhận Q1–Q5 trước khi estimate; ưu tiên Q1 (Vietstock rate limit) và Q4 (OneSignal status) trước
