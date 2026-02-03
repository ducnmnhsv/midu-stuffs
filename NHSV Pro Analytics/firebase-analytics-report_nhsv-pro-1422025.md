# Báo cáo phân tích tổng hợp Firebase Analytics (GA4) — NHSV Pro

**Firebase project**: `nhsv-pro-1422025` (Name: *NHSV Pro*)  
**Project number**: `195382997861`  
**Thời gian báo cáo (assumption)**: 30 ngày gần nhất (rolling) + so sánh 30 ngày liền trước  
**Phạm vi**: Mobile app (iOS/Android), bao gồm Prod & UAT

> Lưu ý quan trọng: Hiện **Prod và UAT đang chung 1 Firebase project** → số liệu DAU/retention/funnel có nguy cơ bị “bẩn” bởi traffic test. Cần lọc/segment rõ ràng (mục “Data Hygiene”).

---

## 1) Inventory (đã xác nhận từ Firebase)

### Apps trong project

- **Android — NHSV Pro Prod**
  - `appId`: `1:195382997861:android:5e663ec7edc98fe308e78c`
  - `package`: `com.nhsv.nhsvpro`
- **Android — NHSV Pro UAT**
  - `appId`: `1:195382997861:android:0c8ab079d4751e2308e78c`
  - `package`: `com.nhsv.uat.nhsvpro`
- **iOS — NHSV Pro Prod**
  - `appId`: `1:195382997861:ios:8f83aecb1178b35908e78c`
  - `bundleId`: `com.nhsv.nhsvpro`
- **iOS — NHSV Pro UAT**
  - `appId`: `1:195382997861:ios:e3affe4f134cca6708e78c`
  - `bundleId`: `com.nhsv.uat.nhsvpro`

---

## 2) Executive Summary (điền số từ GA4/Firebase)

### North-star (mobile trading app)

- **Active traders (proxy)**: số user có phát sinh *order_submit_success* trong kỳ
- **Order conversion**: \(order\_submit\_success / order\_init\)
- **Retention D7**: % user quay lại trong 7 ngày sau first_open
- **Crash-free users** (nếu dùng Crashlytics): %

### KPI snapshot (30 ngày gần nhất)

- **Users**: DAU / WAU / MAU: `___ / ___ / ___`
- **Stickiness**: DAU/MAU: `___%`
- **New users**: `___`
- **Engaged sessions per user**: `___`
- **Avg engagement time / user**: `___`
- **D1 / D7 / D30 retention**: `___% / ___% / ___%`
- **Funnel order**:
  - *order_init*: `___`
  - *order_submit_success*: `___`
  - **Conversion**: `___%`
- **Stability**:
  - Crash-free users: `___%`
  - Crash-free sessions: `___%`

### 3 insights chính (ghi ngắn gọn)

1. `...`
2. `...`
3. `...`

### 3 hành động ưu tiên (actionable)

1. `...`
2. `...`
3. `...`

---

## 3) Core dashboard — Metric cần thiết cho NHSV Pro

### 3.1 Acquisition (Thu hút)

**Mục tiêu**: biết user đến từ đâu, chất lượng ra sao.

- **New users / First opens**
- **User acquisition** theo:
  - Source / Medium / Campaign
  - Platform (iOS/Android)
  - App (Prod vs UAT)
- **Install → First_open conversion** (nếu có liên kết store + campaign)
- **Cost metrics** (nếu chạy ads & link cost import trong GA4)

**Khuyến nghị chart**
- Trend new users theo ngày + breakdown by channel
- Top campaigns (new users + retention D7)

### 3.2 Activation (Kích hoạt)

**Mục tiêu**: user mới “vào được giá trị” trong 1–3 phiên đầu.

Các mốc activation điển hình cho app chứng khoán:
- `first_open`
- `login_success` (hoặc `sign_in`)
- `kyc_submitted` / `kyc_approved` (nếu có)
- `watchlist_add`
- `symbol_view` hoặc `quote_view`
- `price_alert_create` (nếu có)

**KPI**
- **Activation rate**: % new users đạt mốc A trong 24h/72h
- Median time-to-activation (first_open → login_success / watchlist_add)

### 3.3 Engagement (Tương tác)

**Mục tiêu**: user dùng app thường xuyên, đọc giá, theo dõi danh mục.

**KPI**
- DAU/WAU/MAU, stickiness
- Engaged sessions / user
- Avg engagement time / user
- Screen views / user (hoặc page_view tương đương)
- Top screens (Home/Watchlist/Quote/Order/Portfolio)

**Trading app specific**
- **Market data consumption proxy**:
  - `symbol_view` / `quote_subscribe` / `orderbook_view` (nếu track)
- **Notification engagement**:
  - open rate: `notification_open` / `notification_received`

### 3.4 Retention (Giữ chân)

**KPI**
- D1 / D7 / D14 / D30 retention
- Cohort retention theo:
  - Platform
  - Channel
  - App (Prod/UAT)
  - User type (new vs returning)

**Khuyến nghị**
- Tạo cohort “Activated users” (đã `login_success` + `watchlist_add`) và so retention với cohort chung.

### 3.5 Conversion / Trading Funnel (Chuyển đổi giao dịch)

**Mục tiêu**: tối ưu từ xem giá → đặt lệnh → khớp.

**Funnel gợi ý (tối thiểu)**
1. `symbol_view` (hoặc `quote_view`)
2. `order_init` (mở ticket đặt lệnh)
3. `order_confirm` (review)
4. `order_submit_attempt`
5. `order_submit_success`
6. `order_filled` (nếu có realtime fill)

**KPI**
- Conversion từng bước & drop-off
- Time-to-submit (order_init → submit_success)
- Error rate: submit_fail / submit_attempt theo error_code

### 3.6 Stability & Performance (Chất lượng)

**Nguồn**: Firebase Crashlytics + (nếu track) performance.

**KPI**
- Crash-free users/sessions
- Top crash issues (số user ảnh hưởng, trend)
- ANR (Android) nếu có
- App start time / screen load time (nếu dùng Performance Monitoring)

---

## 4) Data Hygiene (bắt buộc vì Prod/UAT chung project)

### Mục tiêu
Tách/loại bỏ traffic test để số liệu PM ra quyết định không bị sai.

### Khuyến nghị thực thi (chọn 1 hoặc kết hợp)

1) **Tách hẳn Firebase project** cho UAT (khuyến nghị dài hạn)
- Ưu: sạch, đơn giản.
- Nhược: cần cấu hình lại, quy trình release.

2) **GA4 Data Filters / Audiences / Comparisons**
- Tạo dimension phân biệt **env**:
  - Dựa trên app package/bundle: `com.nhsv.uat.nhsvpro` vs `com.nhsv.nhsvpro`
  - Hoặc set `user_property`/`event_param`: `env = uat|prod`
- Trong báo cáo mặc định, luôn áp comparison “env = prod”.

3) **Exclude internal traffic**
- Mark traffic từ IP/vpn công ty (nếu phù hợp)
- Exclude `debug_view` / `development` builds

---

## 5) Event & Parameter Spec tối thiểu (để metric đo được)

> Nếu hiện tại app chưa bắn đủ events/params, đây là “Definition of Done” cho analytics instrumentation.

### 5.1 Event cần có

- Auth
  - `login_success`, `login_fail` (param: `method`, `error_code`)
- Watchlist / Quote
  - `watchlist_add` (param: `symbol`, `market`)
  - `symbol_view` (param: `symbol`, `market`, `source_screen`)
- Order
  - `order_init` (param: `symbol`, `side`, `order_type`, `product`)
  - `order_submit_attempt` (param: `symbol`, `side`, `order_type`, `product`)
  - `order_submit_success` (param: `symbol`, `side`, `order_type`, `product`, `latency_ms`)
  - `order_submit_fail` (param: `error_code`, `error_message`, `latency_ms`)
  - `order_filled` (param: `symbol`, `side`, `quantity`, `price`)
- Funds (nếu có)
  - `deposit_initiated`, `deposit_success`, `withdraw_initiated`, `withdraw_success`
- Notifications
  - `notification_received`, `notification_open` (param: `type`, `campaign`)

### 5.2 User properties cần có

- `env`: `prod|uat`
- `account_type`: `real|demo` (nếu có)
- `segment`: `new|existing`
- `kyc_status`: `none|pending|approved|rejected` (nếu hợp lệ về privacy)

---

## 6) Cách lấy số trong Firebase / GA4 (quick steps)

### Firebase console (nhanh)
- Vào Firebase project `nhsv-pro-1422025` → **Analytics**
  - Users / Engagement / Retention overview
  - Events: kiểm tra event names & volume

### GA4 (khuyến nghị để làm report)
- Firebase project → Analytics → mở “View in Google Analytics”
- Dùng **Explore**:
  - Funnel exploration cho “Trading Funnel”
  - Cohort exploration cho retention
  - Segment: `env=prod`, platform, app version

### Xuất số ra báo cáo
- Export table (CSV) từ Explore, hoặc chụp chart + ghi số key KPI.

---

## 7) Phụ lục — Weekly/Monthly PM template (1 trang)

- **Headline**: DAU/MAU, D7 retention, order conversion, crash-free users
- **Acquisition**: top 3 channel, cost/quality (nếu có)
- **Activation**: % login_success trong 24h, % watchlist_add
- **Trading**: funnel + top error_code
- **Stability**: top crashes, trend theo version
- **Next actions**: 3 việc tuần tới + owner + ETA

