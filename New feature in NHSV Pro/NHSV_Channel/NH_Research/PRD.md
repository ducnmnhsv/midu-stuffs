# PRD — NH Research Content Tab (A-04)

**Product:** NHSV Pro · Mobile App  
**Feature ID:** A-04  
**PM:** Midu (Nguyễn Minh Đức)  
**Status:** 📋 Draft  
**Version:** 1.1 · 2026-06-22

---

## 1. Bối cảnh & Vấn đề

Phòng Phân tích NHSV sản xuất đều đặn các báo cáo phân tích thị trường và doanh nghiệp — nhưng hiện tại kênh phân phối duy nhất là email nội bộ và trang web NHSV. User của NHSV Pro không có cách xem báo cáo này trực tiếp trong app.

Hậu quả: nội dung phân tích chất lượng cao do NHSV sản xuất không tiếp cận được user đúng lúc họ đang trading, và team Phân tích mất đi một kênh phân phối hiệu quả đến tệp khách hàng của mình.

Qua khảo sát cạnh tranh với KIS, KBSV, và SSI, cả ba đối thủ đều có tab nội dung phân tích in-app với PDF viewer. NHSV Pro hiện chưa có.

---

## 2. Mục tiêu

| Mục tiêu | Chỉ số đo lường |
| --- | --- |
| User tiếp cận được báo cáo NHSV ngay trong app | % user active xem ít nhất 1 bài/tháng sau release |
| Giảm thời gian Phòng Phân tích publish nội dung | Thời gian từ lúc có file PDF → user thấy trên app (mục tiêu: < 5 phút) |
| Tăng session length trên NHSV Pro | So sánh avg. session duration trước/sau 30 ngày release |

Mục tiêu phụ: chuẩn bị nền tảng nội dung để kích hoạt push notification (phụ thuộc X-03), tăng daily open rate.

---

## 3. Đối tượng người dùng

**Primary:** Retail investor dài hạn, quan tâm phân tích cơ bản. Đây là nhóm user cao cấp của NHSV Pro — thường là khách hàng đã mở tài khoản, có danh mục đang hold.

**Secondary:** Day trader muốn nắm bắt diễn biến thị trường và tin tức doanh nghiệp để ra quyết định ngắn hạn.

---

## 4. Phạm vi tính năng (In Scope)

### 4.1 Mobile App — Trải nghiệm user

#### Tab NH Research trong NHSV Channel

Tab này nằm trong Kênh tin tức NHSV, thay thế vị trí hiện tại của Tin tức. Sau khi A-03 (Tab Reorder) hoàn thành, thứ tự tab sẽ là: Khuyến nghị → NH Research → Tin tức → Thị trường.

Trong tab NH Research, user thấy 3 bộ lọc danh mục: Thị trường (mặc định), Doanh nghiệp, Vĩ mô. Mỗi danh mục tương ứng một call API riêng để lọc kết quả.

#### Danh sách bài viết

Mỗi bài viết hiển thị dạng card với: nhãn danh mục (màu sắc phân biệt từng loại), ngày publish, tiêu đề đậm, và đoạn mô tả ngắn. Nếu bài có đính kèm PDF, card hiển thị thêm chỉ báo file.

Nội dung bài viết hỗ trợ song ngữ (Tiếng Việt và Tiếng Anh). Mobile hiển thị ngôn ngữ phù hợp với cài đặt của user — fallback về Tiếng Việt nếu bản Tiếng Anh chưa có.

User tap card → mở màn hình chi tiết bài viết. Danh sách tải tiếp khi scroll đến cuối (infinite scroll hoặc nút "Xem thêm" — Mobile team quyết định).

#### Chi tiết bài viết và PDF Viewer

Màn hình chi tiết hiển thị toàn bộ nội dung bài viết theo ngôn ngữ của user. Nếu có PDF, user thấy tên file + dung lượng + nút "Mở xem". Tap vào mở PDF Viewer in-app hỗ trợ pinch-to-zoom và điều hướng trang. PDF được cache trong session — mở lần hai không tải lại.

#### Push Notification (phụ thuộc X-03)

Khi Phòng Phân tích publish bài mới, user nhận notification. Tap vào notification deeplink thẳng vào tab NH Research (`nhsvpro://channel/nh-research?category={category}`). Tính năng này chỉ available sau khi X-03 (Push Notification Infrastructure) hoàn thành.

#### Deeplink

Ngoài deeplink từ push notification, app cần hỗ trợ 2 loại deeplink cho NH Research (dùng chung cho notification, universal link, share link, v.v.):

| Đích đến | Deeplink |
|---|---|
| Tab NH Research (theo danh mục) | `nhsvpro://channel/nh-research?category={category}` |
| Chi tiết 1 bài viết | `nhsvpro://channel/nh-research?articleId={articleId}` |

Khi app nhận deeplink có `articleId` → mở thẳng màn Article Detail (MOB-04), gọi `GET /api/v1/nhResearch/articles/{articleId}`. Nếu bài không tồn tại hoặc đã bị ẩn/xóa → hiển thị lại error state của MOB-06 ("Article not found"), không crash app.

### 4.2 Admin Tool — Phòng Phân tích

#### Upload và publish bài viết

Người dùng Admin Tool (Phòng Phân tích) có thể tạo bài viết mới bằng cách điền tiêu đề, chọn danh mục, nhập nội dung cho cả hai ngôn ngữ (VIE bắt buộc, ENG tùy chọn), và đính kèm file PDF. Bài viết được publish ngay khi submit (không có approval workflow trong v1).

#### Quản lý bài đã đăng

Danh sách bài viết có bộ lọc theo danh mục và trạng thái. Admin có thể ẩn bài (disable) mà không cần xóa, hoặc xóa mềm khi cần. Chỉnh sửa bài đã đăng được hỗ trợ — bao gồm thay file PDF và cập nhật nội dung song ngữ.

#### Publish & Notify (phụ thuộc X-03)

Khi tạo hoặc chỉnh sửa bài, form có toggle "Gửi push notification". Mặc định là bật. Khi publish, hệ thống tự gửi notification đến toàn bộ user opted-in và hiển thị xác nhận số thiết bị đã nhận.

---

## 5. Ngoài phạm vi (Out of Scope)

- Phân quyền user trong Admin Tool (xem Open Question 4) — dùng rule hiện có của nhsv-admin
- Notification Settings cho user (màn hình opt-in/opt-out) — sẽ làm sau trong sprint riêng
- Like, comment, share bài viết — backlog
- Stock tag enrichment (gắn mã CK vào bài và hiển thị % thay đổi) — backlog, A-06
- Tìm kiếm trong NH Research — A-07 (Search feature)
- Approval workflow cho bài viết — v1 publish ngay, không duyệt
- Machine translation (tự động dịch từ VIE sang ENG) — v1 admin tự nhập tay

---

## 6. User Stories (tóm tắt)

| ID | Vai trò | Nhu cầu | Lý do |
| --- | --- | --- | --- |
| US-01 | Investor | Xem danh sách báo cáo phân tích theo chủ đề | Tìm nhanh báo cáo liên quan đến mã đang quan tâm |
| US-02 | Investor | Đọc nội dung bài viết theo ngôn ngữ của mình | Hiểu nội dung phân tích đúng ngôn ngữ |
| US-03 | Investor | Xem PDF báo cáo mà không cần tải về máy | Tiết kiệm dung lượng, xem ngay khi cần |
| US-04 | Investor | Nhận thông báo khi có báo cáo mới | Không bỏ lỡ báo cáo quan trọng |
| US-05 | Analyst (Admin) | Upload bài song ngữ (VIE + ENG) và publish ngay | Phục vụ cả nhà đầu tư Việt và nước ngoài |
| US-06 | Analyst (Admin) | Ẩn hoặc chỉnh sửa bài đã đăng | Cập nhật khi có thông tin mới hoặc sai sót |

---

## 7. Yêu cầu phi chức năng

| Hạng mục | Yêu cầu |
| --- | --- |
| Hiệu năng API | Danh sách bài viết load < 2 giây ở điều kiện mạng 4G |
| PDF load | Trang đầu tiên hiển thị < 3 giây (file trung bình ~3 MB) |
| Cache | PDF cache trong session; không tải lại khi mở lần 2 trong cùng session |
| Error handling | Mọi lỗi API đều có UI phản hồi rõ ràng (empty state, error state, retry) |
| Offline | Hiển thị cache nếu có; nếu không có cache, hiển thị thông báo mất kết nối |
| Storage | Dung lượng tối đa per file PDF: 20 MB (xác nhận với IT — xem Open Q #2) |
| Accessibility | Font size và contrast theo NHSV Pro Design System — không vi phạm WCAG AA |
| Song ngữ | VIE bắt buộc; ENG tùy chọn. Fallback: nếu `titleEn`/`shortContentEn` null → hiển thị bản VIE |

---

## 8. API I/O Specification

> Integration type: **TradeX-native** (internal DB — không qua Lotte/Core)
> Date format: **yyyyMMdd HH:mm:ss** (ví dụ: `20260610 09:15:00`)
> Language: Mobile và Admin GET APIs đọc header `Accept-Language` (`vi` → tiếng Việt, `en` → tiếng Anh). Fallback về VIE nếu bản EN chưa có.
> Admin write: request body nested theo ngôn ngữ — `{ "vi": { "title", "shortContent" }, "en": { "title", "shortContent" } }`
> Acceptance Criteria chi tiết từng story: xem `jira-issues.md`

---

### 8.1 GET /api/v1/nhResearch/articles

**Auth:** Bearer token · **Dùng bởi:** Mobile

**8.1 — Query params:**

| Param | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `category` | String | No | — | `MARKET` \| `COMPANY` \| `MACRO` |
| `fetchCount` | Number | No | 20 | Số bài/trang, tối đa 50 |
| `nextKey` | String | No | — | Cursor token trang tiếp. Trang đầu: bỏ qua |

**8.1 — Request header:** `Accept-Language: vi` hoặc `en`

**8.1 — Response 200:**

```json
{
  "articles": [
    {
      "articleId": 1,
      "category": "MARKET",
      "title": "VƯỢT QUA RUNG LẮC",
      "shortContent": "Thị trường tiếp tục hồi phục...",
      "hasPdf": true,
      "publishedAt": "20260610 09:15:00"
    }
  ],
  "totalCount": 12,
  "nextKey": "eyJhcnRpY2xlSWQiOjV9"
}
```

`title` và `shortContent` trả về theo `Accept-Language`. Nếu bản EN chưa có → fallback về VIE. `nextKey` là `null` khi hết dữ liệu.

**8.1 — Error codes:**

| HTTP | Code | Khi nào |
| --- | --- | --- |
| 400 | `INVALID_PARAMETER` | `category` không hợp lệ |
| 401 | `UNAUTHORIZED` | Token invalid/expired |

---

### 8.2 GET /api/v1/nhResearch/articles/{articleId}

**Auth:** Bearer token · **Dùng bởi:** Mobile

**Input — Path param:** `articleId` (Number) · **Request header:** `Accept-Language: vi` hoặc `en`

**8.2 — Response 200:**

```json
{
  "articleId": 1,
  "category": "MARKET",
  "title": "VƯỢT QUA RUNG LẮC",
  "shortContent": "Toàn bộ nội dung tiếng Việt...",
  "pdfUrl": "https://storage.nhsv.vn/research/NHSV_TT_10062026.pdf",
  "pdfFilename": "NHSV_TT_10062026.pdf",
  "pdfSizeBytes": 2516582,
  "publishedAt": "20260610 09:15:00"
}
```

`title` và `shortContent` trả về theo `Accept-Language` — fallback về VIE nếu bản EN chưa có. `pdfUrl`, `pdfFilename`, `pdfSizeBytes` là `null` khi không có PDF.

**8.2 — Error codes:**

| HTTP | Code | Khi nào |
| --- | --- | --- |
| 401 | `UNAUTHORIZED` | Token invalid/expired |
| 404 | `OBJECT_NOT_FOUND` | Bài không tồn tại, đã xóa, hoặc đang ẩn |

---

### 8.3 GET /admin/nhResearch/articles

**Auth:** Admin session · **Dùng bởi:** Admin FE

**8.3 — Request header:** `Accept-Language: vi` hoặc `en`

**8.3 — Query params:**

| Param | Type | Required | Default | Mô tả |
| --- | --- | --- | --- | --- |
| `category` | String | No | — | `MARKET` \| `COMPANY` \| `MACRO` |
| `status` | String | No | — | `PUBLISHED` \| `DISABLED`. Bỏ qua → trả tất cả (trừ `DELETED`) |
| `search` | String | No | — | Tìm theo `title` hoặc `title_en` (case-insensitive LIKE, tìm cả 2 ngôn ngữ) |
| `page` | Number | No | 1 | Offset-based — admin UI cần "Trang X / Y" |
| `fetchCount` | Number | No | 20 | Số bài/trang |

**8.3 — Response 200:**

```json
{
  "articles": [
    {
      "articleId": 1,
      "category": "MARKET",
      "title": "VƯỢT QUA RUNG LẮC",
      "hasPdf": true,
      "status": "PUBLISHED",
      "publishedAt": "20260610 09:15:00",
      "createdAt": "20260610 09:15:00",
      "updatedAt": "20260610 09:15:00",
      "createdBy": "duc.nguyen"
    }
  ],
  "totalCount": 12,
  "totalPages": 1
}

`title` trong response theo `Accept-Language` — fallback VIE.
```

**8.3 — Error codes:**

| HTTP | Code | Khi nào |
| --- | --- | --- |
| 401 | `UNAUTHORIZED` | Không có session admin |

---

### 8.4 POST /admin/nhResearch/articles

**Auth:** Admin session · **Dùng bởi:** Admin FE · **Content-Type:** application/json

**8.4 — Request body:**

```json
{
  "category": "MARKET",
  "vi": {
    "title": "VƯỢT QUA RUNG LẮC",
    "shortContent": "Toàn bộ nội dung tiếng Việt..."
  },
  "en": {
    "title": "OVERCOMING MARKET VOLATILITY",
    "shortContent": "Full English content..."
  },
  "pdfUrl": "https://storage.nhsv.vn/research/NHSV_TT_10062026_1718006400.pdf",
  "pdfFilename": "NHSV_TT_10062026.pdf",
  "pdfSizeBytes": 2516582
}
```

| Field | Type | Required | Mô tả |
| --- | --- | --- | --- |
| `category` | String | Yes | `MARKET` \| `COMPANY` \| `MACRO` |
| `vi.title` | String | Yes | Tiêu đề tiếng Việt, tối đa 500 ký tự |
| `vi.shortContent` | String | Yes | Nội dung tiếng Việt, không giới hạn |
| `en.title` | String | No | Tiêu đề tiếng Anh, tối đa 500 ký tự |
| `en.shortContent` | String | No | Nội dung tiếng Anh, không giới hạn |
| `pdfUrl` | String | No | URL từ upload API (8.7) |
| `pdfFilename` | String | No | Tên file gốc |
| `pdfSizeBytes` | Number | No | Dung lượng file (bytes) |

`en` object là optional — bỏ qua hoặc `null` nếu chưa có bản tiếng Anh.

**8.4 — Response 200:**

```json
{ "id": 42 }
```

Bài được publish ngay (`status = PUBLISHED`, `publishedAt = createdAt = now()`). `createdBy` lấy từ admin session JWT.

**8.4 — Error codes:**

| HTTP | Code | Khi nào |
| --- | --- | --- |
| 400 | `INVALID_PARAMETER` | Thiếu field bắt buộc hoặc `category` sai |
| 401 | `UNAUTHORIZED` | Không có session admin |

---

### 8.5 PUT /admin/nhResearch/articles/{id}

**Auth:** Admin session · **Dùng bởi:** Admin FE · **Content-Type:** application/json

**Input — Path param:** `id` (Number) · **Request body:** tất cả optional

**8.5 — Request body:**

```json
{
  "category": "MARKET",
  "vi": {
    "title": "VƯỢT QUA RUNG LẮC (đã sửa)",
    "shortContent": "Nội dung VIE cập nhật..."
  },
  "en": {
    "title": "OVERCOMING MARKET VOLATILITY (updated)",
    "shortContent": "Updated English content..."
  },
  "pdfUrl": null,
  "status": "DISABLED"
}
```

| Field | Type | Mô tả |
| --- | --- | --- |
| `category` | String | `MARKET` \| `COMPANY` \| `MACRO` |
| `vi.title` | String | Tiêu đề tiếng Việt, tối đa 500 ký tự |
| `vi.shortContent` | String | Nội dung tiếng Việt |
| `en` | Object? | `null` để xóa toàn bộ bản EN |
| `en.title` | String? | Tiêu đề tiếng Anh |
| `en.shortContent` | String? | Nội dung tiếng Anh |
| `pdfUrl` | String? | `null` để xóa PDF |
| `pdfFilename` | String? | |
| `pdfSizeBytes` | Number? | |
| `status` | String | `PUBLISHED` \| `DISABLED` — toggle visibility |

**8.5 — Response 200:**

```json
{ "id": 42 }
```

**8.5 — Error codes:**

| HTTP | Code | Khi nào |
| --- | --- | --- |
| 400 | `INVALID_PARAMETER` | `status = DELETED` (dùng DELETE thay thế) |
| 401 | `UNAUTHORIZED` | Không có session admin |
| 404 | `OBJECT_NOT_FOUND` | Bài không tồn tại hoặc đã xóa |

---

### 8.6 DELETE /admin/nhResearch/articles/{id}

**Auth:** Admin session · **Dùng bởi:** Admin FE

**Input — Path param:** `id` (Number)

**8.6 — Response 200:**

```json
{ "id": 42 }
```

Soft delete — set `status = DELETED`, record giữ nguyên trong DB. PDF file trên storage không bị xóa.

**8.6 — Error codes:**

| HTTP | Code | Khi nào |
| --- | --- | --- |
| 401 | `UNAUTHORIZED` | Không có session admin |
| 404 | `OBJECT_NOT_FOUND` | Bài không tồn tại hoặc đã bị xóa trước đó |

---

### 8.7 POST /admin/nhResearch/upload/pdf

**Auth:** Admin session · **Dùng bởi:** Admin FE · **Content-Type:** multipart/form-data

**8.7 — Form data:**

| Field | Type | Required | Mô tả |
| --- | --- | --- | --- |
| `file` | File | Yes | Chỉ nhận `.pdf` |

**8.7 — Response 200:**

```json
{
  "pdfUrl": "https://storage.nhsv.vn/research/NHSV_TT_10062026_1718006400.pdf",
  "pdfFilename": "NHSV_TT_10062026.pdf",
  "pdfSizeBytes": 2516582
}
```

Dùng `pdfUrl` trả về này khi gọi POST (8.4) hoặc PUT (8.5).

**8.7 — Error codes:**

| HTTP | Code | Khi nào |
| --- | --- | --- |
| 400 | `INVALID_PARAMETER` (`INVALID_FILE_TYPE`) | File không phải PDF |
| 400 | `INVALID_PARAMETER` (`FILE_TOO_LARGE`) | Vượt giới hạn dung lượng (TBD — Q2) |
| 401 | `UNAUTHORIZED` | Không có session admin |

---

> Danh mục: `MARKET` = Thị trường · `COMPANY` = Doanh nghiệp · `MACRO` = Vĩ mô

---

## 9. Phụ thuộc

| Phụ thuộc | Loại | Ghi chú |
| --- | --- | --- |
| A-03 — Tab Reorder | Trước | NH Research cần đúng vị trí trong tab bar |
| X-01 — Admin Tool | Cùng sprint | Admin FE build trong Admin Tool |
| X-03 — Push Notification | Cùng sprint | Publish & Notify toggle chỉ hoạt động khi X-03 xong |
| Storage service (S3 hoặc NHSV server) | Infra | Quyết định trước khi bắt đầu BE-07 |

---

## 10. Open Questions

| # | Câu hỏi | Tác động | Owner | Deadline |
| --- | --- | --- | --- | --- |
| Q1 | Nội dung `shortContent` giới hạn bao nhiêu ký tự? Hay không giới hạn và mobile tự cắt? | UX + DB schema | PM + BE | Trước khi BE-01 |
| Q2 | Storage provider cho PDF: S3, MinIO, hay server NHSV tự host? Max file size? | BE-07 + BE-08 scope | IT Lead | Trước khi BE-07 |
| Q3 | URL PDF là public URL hay signed URL có TTL? | Security + UX (PDF cache) — **blocker cho MOB-05**: mobile cần biết TTL để quyết định cache strategy | IT Lead | Trước khi BE-03 và MOB-05 |
| Q4 | Role nào trong nhsv-admin được phép upload NH Research? Phòng Phân tích có role riêng chưa? | Admin FE + BE access control | IT + Admin | Trước khi ADM-01 |
| Q5 | Mobile hiển thị ngôn ngữ theo app setting hay device locale? Nếu EN chưa có, hiển thị VIE hay ẩn bài? | UX + Mobile FE | PM + Mobile | Trước khi MOB-01 |

---

## 11. Estimate sơ bộ

| Layer | Stories | Ghi chú |
| --- | --- | --- |
| Backend | 8 stories | Chi tiết trong `jira-issues.md` (BE-01 → BE-08) |
| Mobile FE | 6 stories | MOB-01 → MOB-06; PDF viewer có thể dùng native lib |
| Admin FE | 5 stories | ADM-01 → ADM-05; UI reference tại `Admin_Tool/admin-demo.html` |

Estimate giờ: cần IT team tự estimate sau khi Q1–Q5 được confirm. Blocker lớn nhất là Q2 (storage) và X-03 (push notification).

---

## 12. Tiêu chí release

Tính năng được coi là ready to release khi:

Về phía Mobile App: user xem được danh sách bài viết theo danh mục, đọc chi tiết bài theo ngôn ngữ của mình, và mở PDF in-app mà không cần thoát sang trình duyệt.

Về phía Admin Tool: Phòng Phân tích upload và publish bài song ngữ trong vòng 5 phút, không cần nhờ IT. Bản tiếng Anh là optional — bài chỉ có VIE vẫn publish được bình thường.

Về phía Push Notification: nếu X-03 chưa xong, feature vẫn release được — chỉ bỏ toggle Publish & Notify. Push sẽ bật sau khi X-03 merge.

---

Document Status: 📋 Draft | For: IT Lead, Mobile FE, Admin FE, Phòng Phân tích | Next Steps: Confirm Open Questions Q1–Q5 trước khi bắt đầu estimate sprint
