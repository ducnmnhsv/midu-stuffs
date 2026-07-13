# Jira Issues — A-04 NH Research Feature (+ A-06 Stock Tag Enrichment)

**Epic:** NH Research — Analysis Content Tab  
**Spec ref:** [PRD.md](./PRD.md) · [Spec.html](./Spec.html) · [Admin demo](./admin-demo.html)  
**Integration type:** TradeX-native (internal DB only — không qua Lotte/Core, không cần Lotte mapping, Kafka forward, hay auto-populate `sourceIp`/`deviceUniqueId`)  
**Category values (API):** `MARKET` · `COMPANY` · `MACRO`  
**Category labels (UI):** Thị trường · Doanh nghiệp · Vĩ mô  
**Status values (API):** `PUBLISHED` · `DISABLED` · `DELETED`  
**A-06 add-on:** Stock Tag Enrichment (`BE-09`, `BE-10`, `MOB-08`, `MOB-09`, `ADM-06`) — tag nhiều mã CK/bài + filter, không enrich giá/% thay đổi (xem PRD §4.3)

---

## Table of Contents

- [Backend (10 stories)](#backend)
- [Mobile FE (9 stories)](#mobile-fe)
- [Admin FE (6 stories)](#admin-fe)

---

## Backend

### BE-01 · DB Schema — Create `nh_research_articles` table

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `database` |

**Description**

Create the `nh_research_articles` table to store all NH Research articles published by the Analysis Department. This table is the single source of truth for both the mobile app and admin portal.

**Acceptance Criteria**

- [ ] Table `nh_research_articles` is created with the following columns:
  - `article_id` — PK, auto-generated (BIGINT or UUID)
  - `category` — ENUM `('MARKET', 'COMPANY', 'MACRO')`, NOT NULL
  - `title` — VARCHAR(500), NOT NULL
  - `title_en` — VARCHAR(500), NULL
  - `short_content` — TEXT, NOT NULL
  - `short_content_en` — TEXT, NULL
  - `pdf_url` — VARCHAR(1000), NULL
  - `pdf_filename` — VARCHAR(255), NULL
  - `pdf_size_bytes` — BIGINT, NULL
  - `status` — ENUM `('PUBLISHED', 'DISABLED', 'DELETED')`, DEFAULT `'PUBLISHED'`
  - `published_at` — DATETIME, NOT NULL
  - `created_by` — VARCHAR(100), NOT NULL
  - `created_at` — DATETIME, auto-generated
  - `updated_at` — DATETIME, auto-updated
- [ ] Indexes: `(category, status)`, `(published_at DESC)`, `(status)`
- [ ] Migration script created and tested on UAT DB
- [ ] Rollback migration script included

---

### BE-02 · [Mobile API] GET /api/v1/nhResearch/articles

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `api`, `mobile` |

**Description**

Implement the public article list API consumed by the mobile app. Returns paginated articles filtered by category, sorted newest first. Only `PUBLISHED` articles are returned.

**Endpoint**

```
GET /api/v1/nhResearch/articles
Auth: Bearer token
```

**Request — Headers + Query Params**

| | Type | Notes |
|---|---|---|
| `Accept-Language` header | string | `vi` hoặc `en`. Xác định ngôn ngữ `title` và `shortContent` trả về. Fallback về `vi` nếu bản EN chưa có. |

| Param | Type | Required | Description |
|---|---|---|---|
| `category` | string | No | Filter: `MARKET` \| `COMPANY` \| `MACRO`. Omit → return all |
| `fetchCount` | int | No | Số bài mỗi trang. Default: 20, max: 50 |
| `nextKey` | string | No | Cursor token cho trang tiếp theo. Trang đầu: bỏ qua hoặc `""`. Trang sau: dùng giá trị `nextKey` từ response lần trước |

> Cursor-based pagination — TradeX standard cho mobile infinite scroll. `nextKey` encode vị trí bài cuối cùng trong trang hiện tại (server-side opaque token).

**Response (200)**

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
  "nextKey": "eyJwdWJsaXNoZWRBdCI6IjIwMjYtMDYtMDVUMDk6MDA6MDBaIiwiYXJ0aWNsZUlkIjo1fQ=="
}
```

> `title` và `shortContent` theo `Accept-Language`. Khi bài chưa có bản EN → trả VIE (không báo lỗi). `nextKey = null` khi đã hết dữ liệu.

> `nextKey` là `null` khi đã hết dữ liệu (trang cuối cùng).

**Acceptance Criteria**

- [ ] Returns only articles with `status = 'PUBLISHED'`
- [ ] Filtered correctly by `category` when provided
- [ ] Sorted by `publishedAt DESC` (newest first)
- [ ] `title` và `shortContent` trả về theo `Accept-Language` header. Khi `Accept-Language: en` nhưng bài không có bản EN → trả VIE (không báo lỗi)
- [ ] `shortContent` returned in full — mobile truncates on render
- [ ] `hasPdf` is `true` when `pdf_url` is not null
- [ ] Pagination: `nextKey` trong response dùng để fetch trang tiếp. Trang cuối trả `nextKey: null`
- [ ] `totalCount` phản ánh tổng số bài khớp với filter hiện tại (không phụ thuộc cursor)
- [ ] Invalid `category` value returns HTTP 400:
  ```json
  { "code": "INVALID_PARAMETER", "params": [{ "code": "INVALID_VALUE", "param": "category", "messageParams": ["category", "MARKET|COMPANY|MACRO"] }] }
  ```

---

### BE-03 · [Mobile API] GET /api/v1/nhResearch/articles/{articleId}

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `api`, `mobile` |

**Description**

Implement the article detail API. Returns full article data including PDF metadata. Used when a user taps an article card to open the detail screen.

**Endpoint**

```
GET /api/v1/nhResearch/articles/{articleId}
Auth: Bearer token
```

**Request — Header:** `Accept-Language: vi` hoặc `en`

**Response (200)**

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

> `title` và `shortContent` theo `Accept-Language`. Fallback VIE nếu bản EN chưa có. `pdfUrl`, `pdfFilename`, `pdfSizeBytes` là `null` khi không có PDF.

**Note:** `shortContent` là tên DB column chứa toàn bộ nội dung bài viết. Ở list API (BE-02), mobile tự truncate để hiển thị preview. Ở detail API này, trả về nguyên vẹn — không truncate phía backend.

**Acceptance Criteria**

- [ ] `title` và `shortContent` trả về theo `Accept-Language`. Fallback VIE nếu bản EN chưa có (không báo lỗi)
- [ ] Returns full `shortContent` without truncation (mobile renders as-is)
- [ ] `pdfUrl`, `pdfFilename`, `pdfSizeBytes` are `null` when no PDF attached
- [ ] Returns HTTP 404 + `{"code": "OBJECT_NOT_FOUND"}` if article doesn't exist, is `DELETED`, or is `DISABLED`
- [ ] `pdfUrl` is accessible from mobile (public URL or valid signed URL)

---

### BE-04 · [Admin API] GET /admin/nhResearch/articles

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `api`, `admin` |

**Description**

Admin article list API. Unlike the mobile API, this returns all articles including `DISABLED` ones for management. Supports search by title and filter by category and status.

**Endpoint**

```
GET /admin/nhResearch/articles
Auth: Admin session
```

**Request — Headers + Query Params**

| | Type | Notes |
|---|---|---|
| `Accept-Language` header | string | `vi` hoặc `en`. Xác định ngôn ngữ `title` trong list response. Fallback VIE. |

| Param | Type | Required | Description |
|---|---|---|---|
| `category` | string | No | `MARKET` \| `COMPANY` \| `MACRO` |
| `status` | string | No | `PUBLISHED` \| `DISABLED`. Omit → return all (excl. `DELETED`) |
| `search` | string | No | LIKE search trên cả `title` và `title_en` (case-insensitive) |
| `page` | int | No | Default: 1 — offset-based (xem note bên dưới) |
| `fetchCount` | int | No | Số bài mỗi trang. Default: 20 |

> **Note:** Admin API dùng offset-based pagination (`page`) thay vì cursor-based (`nextKey`) vì management UI cần hiển thị "Trang X / Y" và cho phép jump đến trang bất kỳ. Đây là exception có chủ ý so với TradeX convention — áp dụng cho admin-only APIs.

**Response (200)**

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
```

> `title` theo `Accept-Language`. Fallback VIE nếu bản EN chưa có.

**Acceptance Criteria**

- [ ] Returns `PUBLISHED` + `DISABLED` articles (excludes `DELETED`)
- [ ] When `status` filter provided, returns only articles of that status
- [ ] `search` performs case-insensitive LIKE on cả `title` (VIE) và `title_en` (EN)
- [ ] `title` trong response theo `Accept-Language` — fallback VIE nếu bản EN chưa có
- [ ] Response includes `articleId`, `category`, `title`, `hasPdf`, `status`, `publishedAt`, `createdAt`, `updatedAt`, `createdBy`
- [ ] `totalPages` = `ceil(totalCount / fetchCount)` — dùng cho admin pagination UI
- [ ] Requires admin authentication — returns 401 if not authenticated

---

### BE-05 · [Admin API] POST /admin/nhResearch/articles

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `api`, `admin` |

**Description**

Create a new article. Article is published immediately upon creation (`status = 'PUBLISHED'`, `publishedAt = now()`). No approval workflow in v1.

**Endpoint**

```
POST /admin/nhResearch/articles
Auth: Admin session
Content-Type: application/json
```

**Request Body**

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

| Field | Type | Required | Notes |
|---|---|---|---|
| `category` | string | Yes | `MARKET` \| `COMPANY` \| `MACRO` |
| `vi.title` | string | Yes | Tiêu đề tiếng Việt, max 500 chars |
| `vi.shortContent` | string | Yes | Nội dung tiếng Việt, no max length |
| `en` | object | No | Bỏ qua hoặc `null` nếu chưa có bản EN |
| `en.title` | string | No | Tiêu đề tiếng Anh, max 500 chars |
| `en.shortContent` | string | No | Nội dung tiếng Anh, no max length |
| `pdfUrl` | string | No | URL returned from upload API |
| `pdfFilename` | string | No | Original filename |
| `pdfSizeBytes` | number | No | File size in bytes |

**Response (200)**

```json
{ "id": 42 }
```

**Acceptance Criteria**

- [ ] Validates required fields — returns HTTP 400 `INVALID_PARAMETER` on missing/invalid
- [ ] Validates `category` is one of `MARKET`, `COMPANY`, `MACRO`
- [ ] `vi.title` và `vi.shortContent` là bắt buộc; `en` object là optional
- [ ] Sets `status = 'PUBLISHED'`, `publishedAt = createdAt = now()` automatically
- [ ] Sets `createdBy` from admin session JWT
- [ ] Returns `{"id": <articleId>}` — no extra fields
- [ ] Returns HTTP 200 on success

---

### BE-06 · [Admin API] PUT + DELETE /admin/nhResearch/articles/{id}

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `api`, `admin` |

**Description**

Update an existing article (partial update) and soft-delete. The PUT endpoint also handles visibility toggling (`PUBLISHED` ↔ `DISABLED`) via the `status` field. DELETE performs a soft delete (`status = 'DELETED'`).

**Endpoints**

```
PUT    /admin/nhResearch/articles/{id}
DELETE /admin/nhResearch/articles/{id}
Auth: Admin session
```

**PUT Request Body** (all fields optional)

```json
{
  "vi": { "title": "Tiêu đề đã sửa", "shortContent": "Nội dung VIE cập nhật..." },
  "en": null,
  "status": "DISABLED"
}
```

| Field | Type | Notes |
|---|---|---|
| `category` | string | `MARKET` \| `COMPANY` \| `MACRO` |
| `vi.title` | string | Tiêu đề tiếng Việt, max 500 chars |
| `vi.shortContent` | string | Nội dung tiếng Việt |
| `en` | object? | `null` để xóa toàn bộ bản EN |
| `en.title` | string? | Tiêu đề tiếng Anh |
| `en.shortContent` | string? | Nội dung tiếng Anh |
| `pdfUrl` | string? | `null` to remove PDF |
| `pdfFilename` | string? | |
| `pdfSizeBytes` | number? | |
| `status` | string | `'PUBLISHED'` \| `'DISABLED'` — for visibility toggle |

**Responses**

```json
PUT  200: { "id": 42 }
DEL  200: { "id": 42 }
```

**Acceptance Criteria**

- [ ] PUT supports partial update — only provided fields are updated
- [ ] PUT with `status: 'DISABLED'` hides article from mobile app immediately
- [ ] PUT with `status: 'PUBLISHED'` makes article visible on mobile app
- [ ] PUT does not allow `status: 'DELETED'` — use DELETE for that
- [ ] DELETE sets `status = 'DELETED'` (soft delete) — record retained in DB
- [ ] DELETE does NOT delete PDF from storage
- [ ] `updatedAt` is refreshed on every PUT
- [ ] Returns HTTP 404 if article not found or already deleted

---

### BE-07 · File service — PDF upload to storage

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `file-storage` |

**Description**

Internal file service that accepts a PDF, validates it, saves it to storage (S3-compatible or NHSV server), and returns a publicly accessible URL. Used by BE-08.

> ⚠️ Storage provider and max file size to be confirmed — see Open Question #2 in PRD.

**Acceptance Criteria**

- [ ] Validates file is `.pdf` MIME type — rejects other formats
- [ ] Validates file size does not exceed configured limit (TBD, proposed 20MB)
- [ ] Stores file in designated storage bucket/directory
- [ ] Returns public URL (or signed URL with appropriate TTL — confirm with tech lead)
- [ ] Filename in storage: sanitized, collision-safe (e.g., timestamp prefix)
- [ ] Returns original filename and file size (bytes) along with URL

---

### BE-08 · [Admin API] POST /admin/nhResearch/upload/pdf

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `api`, `admin`, `file-upload` |

**Description**

HTTP endpoint that wraps the file service (BE-07). Admin FE calls this before creating/editing an article to upload the PDF and receive a URL.

**Endpoint**

```
POST /admin/nhResearch/upload/pdf
Auth: Admin session
Content-Type: multipart/form-data
```

**Request**

| Field | Type | Required |
|---|---|---|
| `file` | File | Yes — `.pdf` only |

**Response (200)**

```json
{
  "pdfUrl": "https://storage.nhsv.vn/research/NHSV_TT_10062026_1718006400.pdf",
  "pdfFilename": "NHSV_TT_10062026.pdf",
  "pdfSizeBytes": 2516582
}
```

**Acceptance Criteria**

- [ ] Calls file service (BE-07) to validate and store the file
- [ ] Returns HTTP 400 if file is not PDF:
  ```json
  { "code": "INVALID_PARAMETER", "params": [{ "code": "INVALID_FILE_TYPE", "param": "file", "messageParams": ["file"] }] }
  ```
- [ ] Returns HTTP 400 if file exceeds size limit:
  ```json
  { "code": "INVALID_PARAMETER", "params": [{ "code": "FILE_TOO_LARGE", "param": "file", "messageParams": ["file", "20MB"] }] }
  ```
- [ ] Returns `pdfUrl`, `pdfFilename`, `pdfSizeBytes` on success
- [ ] Admin FE uses returned `pdfUrl` when calling POST/PUT article API

---

### BE-09 · DB Schema — Create `nh_research_article_stocks` join table

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `database`, `a-06` |

**Description**

Create the join table that maps an article to the stock codes tagged on it. One article can have 0..N tagged codes (see A-06 in PRD.md §4.3). This table is the reverse-lookup index that powers the `stockCode` filter on BE-02/BE-10.

**Acceptance Criteria**

- [ ] Table `nh_research_article_stocks` is created with columns:
  - `id` — PK, auto-generated (BIGINT)
  - `article_id` — FK → `nh_research_articles.article_id`, NOT NULL
  - `stock_code` — VARCHAR(10), NOT NULL, stored uppercase
  - `created_at` — DATETIME, auto-generated
- [ ] Indexes: `(article_id)` (fetch tags of one article), `(stock_code)` (reverse lookup — find articles by code)
- [ ] Deleting an article (soft delete on `nh_research_articles`) does NOT cascade-delete rows here — rows are retained for audit; queries always join through `nh_research_articles.status`
- [ ] Migration script created and tested on UAT DB, with rollback script included

---

### BE-10 · Extend NH Research APIs with `stockCodes` tagging + filter

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Backend |
| **Priority** | High |
| **Labels** | `nh-research`, `api`, `a-06` |

**Description**

Extend the existing mobile and admin article APIs (BE-02, BE-03, BE-04, BE-05, BE-06) to read/write `stockCodes`, and add the `stockCode` filter to the mobile list API. Depends on BE-09.

**Changes per endpoint:**

| Endpoint | Change |
|---|---|
| `GET /api/v1/nhResearch/articles` (BE-02) | New optional query param `stockCode` (string) — exact match, case-insensitive, filters to articles tagged with this code. Combinable with `category`. Response items add `stockCodes: string[]` |
| `GET /api/v1/nhResearch/articles/{articleId}` (BE-03) | Response adds `stockCodes: string[]` |
| `GET /admin/nhResearch/articles` (BE-04) | Response items add `stockCodes: string[]` |
| `POST /admin/nhResearch/articles` (BE-05) | Request body adds optional `stockCodes: string[]` |
| `PUT /admin/nhResearch/articles/{id}` (BE-06) | Request body adds optional `stockCodes: string[]` — **full replace** of the tag set (send `[]` to clear all, omit field to leave unchanged) |

**Acceptance Criteria**

- [ ] `stockCode` query param on BE-02 does exact match (not LIKE) against `nh_research_article_stocks.stock_code`, case-insensitive
- [ ] `stockCode` filter combines correctly with `category` filter (AND condition)
- [ ] `stockCodes` in POST (BE-05): optional, max 10 codes, each code max 10 chars, invalid values return HTTP 400 `INVALID_PARAMETER`; codes are uppercased before insert; omitted/`[]` → article created with no tags
- [ ] `stockCodes` in PUT (BE-06): when provided, deletes existing rows for that `article_id` and inserts the new set (atomic replace, not merge); when field is omitted, existing tags are left untouched
- [ ] `stockCodes` always returned as `[]` (never `null`) when article has no tags
- [ ] No behavior change for callers that don't send `stockCode`/`stockCodes` — fully backward compatible with A-04 clients already in production

---

## Mobile FE

### MOB-01 · NH Research tab — Category filter (MARKET / COMPANY / MACRO)

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | High |
| **Labels** | `nh-research`, `mobile`, `home-screen` |

**Description**

Add the NH Research tab inside the "NHSV News Channel" section on the Home screen. The tab contains 3 category filter pills mapping API values to Vietnamese display labels.

**Category mapping**

| API value | Display label |
|---|---|
| `MARKET` | Thị trường |
| `COMPANY` | Doanh nghiệp |
| `MACRO` | Vĩ mô |

**Acceptance Criteria**

- [ ] NH Research tab appears as the first tab in the NHSV News Channel section
- [ ] 3 pill buttons displayed: Thị trường · Doanh nghiệp · Vĩ mô
- [ ] Default selected: Thị trường (sends `category=MARKET` to API)
- [ ] Active pill has distinct visual style (filled background)
- [ ] Switching category triggers a fresh API call with the corresponding `category` value
- [ ] Category state is not persisted across app sessions — always defaults to MARKET

---

### MOB-02 · Article card component

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | High |
| **Labels** | `nh-research`, `mobile`, `component` |

**Description**

Reusable card component for displaying article previews in the list. Shows category color label, date, title, and truncated content.

**Category colors**

| Category | Color |
|---|---|
| MARKET | `#1a4a8a` (blue) |
| COMPANY | `#0e7490` (teal) |
| MACRO | `#5b21b6` (violet) |

**Acceptance Criteria**

- [ ] Displays: colored category label + date, title (bold), shortContent preview (2–3 lines, truncated)
- [ ] Category label color matches the mapping above
- [ ] Date format: `DD/MM` (e.g., `10/06`)
- [ ] Title truncates if more than 2 lines
- [ ] Shows PDF attachment indicator when `hasPdf = true`
- [ ] Tapping card navigates to Article Detail screen
- [ ] Loading skeleton displayed while API is fetching

---

### MOB-03 · Article list screen — Pagination & empty state

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | High |
| **Labels** | `nh-research`, `mobile`, `screen` |

**Description**

The main list screen inside the NH Research tab. Renders article cards from API, handles pagination, and shows empty state when no articles exist for a category.

**API:** `GET /api/v1/nhResearch/articles?category=MARKET&fetchCount=20`

**Pagination flow (cursor-based):**
- Trang đầu: không gửi `nextKey` (hoặc `""`)
- Trang tiếp: gửi `nextKey` = giá trị từ response lần trước
- Hết dữ liệu: `nextKey` trong response là `null` → ẩn "Load more" / dừng infinite scroll

**Acceptance Criteria**

- [ ] Renders `ArticleCard` list from `articles` array in API response
- [ ] Trang đầu: fetch không có `nextKey`; mỗi lần "Load more" / scroll đến cuối gửi `nextKey` từ response trước
- [ ] Khi `nextKey = null` trong response: dừng pagination, ẩn trigger load more
- [ ] Switching category: xóa `nextKey` hiện tại, clear list, fetch trang đầu của category mới
- [ ] Loading skeleton shown during API call
- [ ] Empty state message when `totalCount = 0`: *"Chưa có nội dung. Vui lòng quay lại sau."*
- [ ] Network error state with retry button
- [ ] Lists for different categories do not share cache

---

### MOB-04 · Article detail screen

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | High |
| **Labels** | `nh-research`, `mobile`, `screen` |

**Description**

Full article view. Displays title, category tag, publish date, full `shortContent`, and optionally the PDF attachment section. Opened via push navigation from article card.

**API:** `GET /api/v1/nhResearch/articles/{articleId}`

**Acceptance Criteria**

- [ ] Header: back button + article title (truncated if long)
- [ ] Category tag + publish date shown below header
- [ ] `shortContent` displayed in full — scrollable if long
- [ ] PDF section visible only when `pdfUrl != null`
- [ ] PDF section shows: file icon, `pdfFilename`, formatted size (e.g., `2.4 MB`), "Mở xem" button
- [ ] "Mở xem" button navigates to PDF Viewer screen (MOB-05)
- [ ] PDF section completely hidden when `pdfUrl` is null (no placeholder)
- [ ] Loading state while fetching article detail
- [ ] Error state if API returns 404 or network failure

---

### MOB-05 · In-app PDF viewer

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | Medium |
| **Labels** | `nh-research`, `mobile`, `pdf` |

**Description**

Full-screen PDF viewer opened from the Article Detail screen. Renders the PDF from `pdfUrl` in-app without requiring the user to download or switch apps.

**Acceptance Criteria**

- [ ] Opens in fullscreen (push navigation or modal — team decides)
- [ ] Top bar: filename (truncated) + close button (✕)
- [ ] Bottom navigation bar: previous page / page count / next page
- [ ] Supports pinch-to-zoom
- [ ] Loading indicator while PDF renders
- [ ] PDF cached in session — re-opening same PDF does not re-download
- [ ] iOS: use native `PDFKit` (preferred)
- [ ] Android: use `PdfRenderer` or WebView fallback (Mobile team decides based on current stack)
- [ ] Error toast if PDF fails to load: *"Không thể mở file. Vui lòng thử lại."*

---

### MOB-06 · Error handling & edge cases

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | Medium |
| **Labels** | `nh-research`, `mobile`, `error-handling` |

**Description**

Comprehensive error and edge case coverage across all NH Research screens to ensure a graceful user experience under all conditions.

**Acceptance Criteria**

- [ ] API 4xx/5xx on list screen → error state UI with retry button
- [ ] API 404 on detail screen → show "Article not found" message + back button
- [ ] PDF load failure → toast error (not full-screen error)
- [ ] Very long title (>100 chars) truncated correctly on card and detail header
- [ ] Article with no PDF — PDF section hidden, no layout shift
- [ ] Empty category (0 articles) → empty state, not blank screen
- [ ] Rapid category switching — only last request's response rendered (cancel previous requests)
- [ ] Offline state → cached data if available, otherwise network error state

---

### MOB-07 · Deeplink — Navigate to NH Research tab / Article detail

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | Medium |
| **Labels** | `nh-research`, `mobile`, `deeplink` |

**Description**

App cần xử lý deeplink để mở thẳng tab NH Research hoặc màn Article Detail — dùng chung cho push notification (X-03), universal link, và share link trong tương lai. Đăng ký route handler trong deeplink router hiện có của app (không tạo router riêng cho NH Research).

**Deeplink scheme**

| Đích đến | Deeplink |
|---|---|
| Tab NH Research (theo danh mục) | `nhsvpro://channel/nh-research?category={category}` |
| Chi tiết 1 bài viết | `nhsvpro://channel/nh-research?articleId={articleId}` |

**Acceptance Criteria**

- [ ] Deeplink `nhsvpro://channel/nh-research?category={category}` mở app → tab NH Research, chọn đúng pill category tương ứng (mặc định MARKET nếu `category` không hợp lệ/thiếu)
- [ ] Deeplink `nhsvpro://channel/nh-research?articleId={articleId}` mở app → thẳng màn Article Detail (MOB-04) của `articleId` đó, không qua màn list trước
- [ ] Nếu có cả `category` và `articleId` → ưu tiên mở Article Detail
- [ ] Gọi `GET /api/v1/nhResearch/articles/{articleId}` để lấy data; loading state trong lúc chờ
- [ ] `articleId` không tồn tại / bài đã `DISABLED`/`DELETED` (API trả 404) → hiển thị error state theo MOB-06 ("Article not found" + back button), không crash app
- [ ] Back từ Article Detail (mở qua deeplink) → về Home screen (không có màn list trong stack vì user chưa qua đó)
- [ ] App đang ở background/foreground khi nhận deeplink đều xử lý đúng (không chỉ cold start)
- [ ] Test cả 2 case: app chưa mở (cold start) và app đang chạy nền

---

### MOB-08 · Stock tag chip trên Article card + Article detail

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | High |
| **Labels** | `nh-research`, `mobile`, `a-06` |

**Description**

Hiển thị `stockCodes` (trả về từ BE-02/BE-03 sau BE-10) dưới dạng tag/chip nhỏ trên Article card (MOB-02) và Article Detail (MOB-04). Tap 1 chip → navigate sang Stock Detail screen của mã đó, dùng lại cơ chế navigate/deeplink đã có trong app (cùng pattern với CTA "Xem cổ phiếu {code}" ở tab Khuyến nghị) — không xây màn mới.

**Acceptance Criteria**

- [ ] Article card (MOB-02): tag chip mã CK hiển thị trong `.card-top`, cùng hàng với category badge, trước ngày publish
- [ ] Nếu `stockCodes.length > 3`: hiện 3 mã đầu + chip `+N` (N = số mã còn lại), không hiện tràn dòng
- [ ] Nếu `stockCodes = []`: không hiện khu vực tag, không có khoảng trống thừa (layout giữ nguyên như bài không tag)
- [ ] Article Detail (MOB-04): tag chip hiển thị ngay dưới title, trước phần category + ngày, hoặc cùng khu vực category tag hiện có
- [ ] Tap 1 chip mã → navigate Stock Detail screen với đúng `stockCode` đó
- [ ] Chip không tương tác được (không tap) nếu app chưa hỗ trợ Stock Detail cho mã đó — fallback: vẫn navigate, để Stock Detail screen tự xử lý mã không tồn tại
- [ ] Loading/skeleton state (MOB-01b) không cần render tag chip giả — giữ nguyên skeleton hiện có

---

### MOB-09 · NH Research tab — Filter bài viết theo mã CK

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Mobile FE |
| **Priority** | Medium |
| **Labels** | `nh-research`, `mobile`, `a-06`, `filter` |

**Description**

Thêm 1 input filter mã CK trong tab NH Research (MOB-01), bên cạnh 3 category chip hiện có (Thị trường/Doanh nghiệp/Vĩ mô). Khi user nhập/chọn mã, danh sách bài viết gọi lại `GET /api/v1/nhResearch/articles?category={category}&stockCode={code}` — kết hợp với category đang chọn. Đây là filter theo mã, **không phải full-text search** nội dung/tiêu đề (search đó là A-07, tách biệt scope).

**Acceptance Criteria**

- [ ] Input filter mã CK hiển thị trong tab NH Research, không thay thế 3 category chip hiện có
- [ ] Nhập/chọn mã → fetch lại list với `stockCode` param, giữ nguyên `category` đang chọn, reset `nextKey`/pagination
- [ ] Xóa filter mã (input rỗng) → list trở lại đúng category đang chọn, không còn `stockCode` param
- [ ] Không có bài nào tag mã vừa filter → empty state riêng: *"Chưa có báo cáo nào liên quan đến mã {code}."*
- [ ] Debounce input 250ms trước khi gọi API (đồng bộ pattern debounce đã dùng ở search Khuyến nghị)
- [ ] Filter mã CK không ảnh hưởng đến behaviour deeplink (MOB-07) — deeplink vẫn mở đúng category/article, filter mã chỉ là state tại chỗ trong tab

---

## Admin FE

### ADM-01 · Add NH Research section to nhsv-admin sidebar

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Admin FE |
| **Priority** | High |
| **Labels** | `nh-research`, `admin`, `navigation` |

**Description**

Add "NH Research" as a new menu item in the nhsv-admin sidebar under the Content section. Clicking navigates to the article management page.

**Acceptance Criteria**

- [ ] "NH Research" menu item added under Content section in sidebar
- [ ] Route: `/nhsv-admin/nh-research`
- [ ] Item is highlighted/active when on any NH Research page
- [ ] Article count badge displayed next to menu item (calls admin list API)
- [ ] Access control: confirm with IT team which admin roles can see this menu item (see Open Q #4 in PRD)

---

### ADM-02 · Article management list page

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Admin FE |
| **Priority** | High |
| **Labels** | `nh-research`, `admin`, `page` |

**Description**

Main management page at `/nhsv-admin/nh-research`. Lists all articles with filtering, sorting, and bulk management capabilities.

**API:** `GET /admin/nhResearch/articles`

**Acceptance Criteria**

- [ ] Page header: "NH Research" title + "+ Thêm bài mới" primary button
- [ ] Filter bar: Category dropdown (`MARKET`/`COMPANY`/`MACRO`) + Status dropdown (`PUBLISHED`/`DISABLED`) + title search input
- [ ] Table columns: # | Category | Title | PDF | Status | Upload date | Update date | Created by | Visibility toggle | Actions
- [ ] Category shown as colored badge (MARKET=blue, COMPANY=teal, MACRO=violet)
- [ ] Status shown as colored badge (PUBLISHED=green, DISABLED=gray)
- [ ] PDF column: file size if attached, "—" if not
- [ ] Visibility toggle: inline switch, calls `PUT` with `{status: 'PUBLISHED' | 'DISABLED'}` — no confirmation modal needed
- [ ] Row opacity reduced for DISABLED articles
- [ ] Edit button → opens edit form (ADM-04)
- [ ] Delete button → opens confirmation modal (ADM-05)
- [ ] Pagination: 20 rows per page; hiển thị "Trang X / Y" dùng `page` và `totalPages` từ response
- [ ] Default sort: `createdAt DESC`

---

### ADM-03 · Create article form

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Admin FE |
| **Priority** | High |
| **Labels** | `nh-research`, `admin`, `form` |

**Description**

Form for creating a new article. Accessible via "+ Thêm bài mới" button. Calls POST article API then PDF upload API as a two-step flow.

**API flow:**  
`POST /admin/nhResearch/upload/pdf` → get `pdfUrl` → `POST /admin/nhResearch/articles`

**Form layout — VIE/ENG tab:**

Form có 2 tab ngôn ngữ: **VIE** (mặc định active) và **ENG**. Mỗi tab chứa riêng trường Title và Short Content. Category, PDF upload, và nút Đăng bài nằm ngoài tab (dùng chung cả 2 ngôn ngữ).

```
[ VIE | ENG ]
━━━━━━━━━━━━━━━━━━━━━━━━━
Tiêu đề *          [____________________]
Nội dung *         [____________________]
                   [____________________]
━━━━━━━━━━━━━━━━━━━━━━━━━ (dùng chung)
Danh mục *         [ Thị trường ▼ ]
Đính kèm PDF       [ Upload / Drag-drop ]
[ Hủy ]            [ Đăng bài ]
```

**Acceptance Criteria**

- [ ] Form có 2 tab: VIE (mặc định) và ENG. Tab VIE là bắt buộc, tab ENG là optional
- [ ] Mỗi tab chứa: Title (max 500 chars với char counter) và Short Content (textarea)
- [ ] Category dropdown (dùng chung): `MARKET` (Thị trường) · `COMPANY` (Doanh nghiệp) · `MACRO` (Vĩ mô)
- [ ] PDF upload (dùng chung): drag-and-drop zone + browse button, accepts `.pdf` only
- [ ] PDF upload flow: file selected → auto-upload to `/admin/nhResearch/upload/pdf` → show filename + size + remove option
- [ ] Client-side validation: VIE Title và VIE Short Content là bắt buộc, ENG fields là optional
- [ ] Submit ("Đăng bài"): gửi `{ "vi": {...}, "en": {...} }` — `en` object chỉ include nếu admin đã điền ít nhất 1 field ENG
- [ ] On success: redirect to list page + toast "Đăng bài thành công"
- [ ] On API error: show inline error message, do not redirect
- [ ] "Hủy" button → navigate back without saving

---

### ADM-04 · Edit article form

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Admin FE |
| **Priority** | High |
| **Labels** | `nh-research`, `admin`, `form` |

**Description**

Pre-filled edit form for an existing article. Uses the same layout as create form with additional status toggle. Calls PUT article API on save.

**API:** `PUT /admin/nhResearch/articles/{id}`

**Pre-fill flow (2 API calls):**  
Khi mở edit form → gọi song song:
1. `GET /admin/nhResearch/articles/{id}` với `Accept-Language: vi` → điền tab VIE
2. `GET /admin/nhResearch/articles/{id}` với `Accept-Language: en` → điền tab ENG (nếu response có data)

**Acceptance Criteria**

- [ ] Form layout giống ADM-03: 2 tab VIE/ENG + Category + PDF (dùng chung)
- [ ] Pre-fill tab VIE từ response `Accept-Language: vi`; pre-fill tab ENG từ response `Accept-Language: en`
- [ ] Nếu bài chưa có bản EN → tab ENG để trống, không báo lỗi
- [ ] Category, title, shortContent editable trên từng tab
- [ ] PDF section shows existing file with option to replace or remove
  - Replace: re-upload via `/admin/nhResearch/upload/pdf`, update `pdfUrl`
  - Remove: set `pdfUrl = null` in PUT request
- [ ] Status toggle (`PUBLISHED` / `DISABLED`) visible — allows toggling visibility without leaving the form
- [ ] Submit ("Lưu thay đổi"): gửi `{ "vi": {...}, "en": {...} }` — `en: null` nếu admin xóa hết ENG content
- [ ] Unsaved changes warning if user tries to navigate away
- [ ] On API error: show inline error, do not redirect

---

### ADM-05 · PDF upload component & delete confirmation modal

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Admin FE |
| **Priority** | Medium |
| **Labels** | `nh-research`, `admin`, `component` |

**Description**

Shared components used across create and edit forms: the PDF upload widget and the article delete confirmation modal.

**Acceptance Criteria**

**PDF upload component:**
- [ ] Drag-and-drop area with visual feedback on hover
- [ ] Browse button as fallback
- [ ] Accepts only `.pdf` files — shows inline error for other formats
- [ ] Upload progress indicator while calling `/admin/nhResearch/upload/pdf`
- [ ] On success: shows filename, formatted file size, and remove (✕) button
- [ ] On upload error: shows error message, allows retry
- [ ] Remove button clears the upload state

**Delete confirmation modal:**
- [ ] Triggered by Delete button on list page
- [ ] Shows article title in the confirmation dialog
- [ ] Warning copy: article will be hidden immediately; PDF file is retained on storage
- [ ] Confirm → calls `DELETE /admin/nhResearch/articles/{id}`, on success removes row from list + toast "Đã xóa bài viết"
- [ ] Cancel → closes modal, no action taken

---

### ADM-06 · Mã CK tag input trên form + cột Mã CK trên list

| Field | Value |
|---|---|
| **Type** | Story |
| **Component** | Admin FE |
| **Priority** | Medium |
| **Labels** | `nh-research`, `admin`, `a-06`, `form` |

**Description**

Thêm field "Mã CK liên quan (không bắt buộc)" vào Create form (ADM-03) và Edit form (ADM-04), dùng chung cho cả 2 ngôn ngữ — nằm cùng khu vực Category/PDF, không thuộc tab VIE/ENG. Tái dùng đúng component autocomplete mã CK đã có ở Admin Khuyến nghị ("Mã CK: input với autocomplete từ danh mục"). Thêm cột "Mã CK" vào list page (ADM-02).

**Form layout — bổ sung vào khu vực dùng chung:**

```
Danh mục *         [ Thị trường ▼ ]
Mã CK liên quan    [ VCB ✕ ] [ BID ✕ ] [ + Nhập mã... ]
Đính kèm PDF       [ Upload / Drag-drop ]
```

**Acceptance Criteria**

- [ ] Field "Mã CK liên quan" nằm ở khu vực dùng chung (không phải trong tab VIE/ENG), ngay dưới Category
- [ ] Input có autocomplete gợi ý mã từ danh mục mã CK (tái dùng component đã có ở Khuyến nghị Admin) — không tạo component autocomplete mới
- [ ] Mỗi mã nhập/chọn xong hiện thành chip riêng, có nút ✕ để xóa từng mã
- [ ] Không required — submit không có mã nào vẫn publish được bình thường (`stockCodes: []`)
- [ ] Validate client-side: tối đa 10 mã/bài, không cho nhập trùng mã trong cùng bài
- [ ] Create form (ADM-03): submit gửi `stockCodes` trong body POST (BE-05) — chỉ gửi field khi có ít nhất 1 mã, hoặc gửi `[]` nếu muốn explicit rõ ràng
- [ ] Edit form (ADM-04): pre-fill chip từ `stockCodes` trả về ở GET detail; submit PUT (BE-06) luôn gửi `stockCodes` là **full set hiện tại** trên form (replace, không merge) — kể cả khi admin không đổi gì
- [ ] List page (ADM-02): thêm cột "Mã CK" hiển thị chip mã (tối đa 3 chip + `+N`), hoặc "—" nếu bài không tag mã nào
- [ ] Component autocomplete/chip input được implement dưới dạng shared component để Admin Khuyến nghị và Admin NH Research cùng dùng chung (tránh 2 bản implementation khác nhau)

---

## Summary

| Layer | Stories | Priority breakdown |
|---|---|---|
| Backend | 8 + 2 (A-06) | 9 High · 1 High (BE-10) |
| Mobile FE | 7 + 2 (A-06) | 4 High · 1 High (MOB-08) · 3 Medium · 1 Medium (MOB-09) |
| Admin FE | 5 + 1 (A-06) | 4 High · 2 Medium (incl. ADM-06) |
| **Total** | **25** | |

**Blocked stories:** BE-07 and BE-08 depend on storage provider decision (Open Q #2 in PRD). A-06 stories (`BE-09`, `BE-10`, `MOB-08`, `MOB-09`, `ADM-06`) have no blocker and can be scheduled independently of Q1–Q5.  
**Recommended start:** BE-01 (DB schema) → BE-02/03 (Mobile APIs) in parallel → MOB-01~04 can start once BE-02/03 are deployed to UAT. For A-06: BE-09 (join table) → BE-10 (API changes) → MOB-08/MOB-09 + ADM-06 in parallel once BE-10 is on UAT.

---

*Generated from PRD A-04 — NH Research Feature Spec v1.0 · A-06 stories appended v1.2 (2026-07-13)*
