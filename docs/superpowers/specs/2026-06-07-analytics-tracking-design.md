# Thiết Kế Hệ Thống Analytics Tracking
**NHSV Pro — GA4 Analytics**

- **Ngày:** 2026-06-07
- **Tác giả:** Duc Nguyen (BA/PM)
- **Trạng thái:** Bản nháp — Chờ triển khai
- **GA4 Property:** NHSV Pro (`properties/478227972`)

---

## 1. Mục Tiêu

Xây dựng hệ thống analytics toàn diện cho NHSV Pro nhằm hỗ trợ ra quyết định sản phẩm dựa trên dữ liệu.

**Mục tiêu cụ thể:**
1. **Retention & Engagement** — Hiểu cách user quay lại app và sử dụng tính năng nào (cả app-level lẫn feature-level)
2. **Tối ưu eKYC Funnel** — Nắm rõ điểm drop-off, lỗi phát sinh và thời gian hoàn thành
3. **Trading Activation** — Theo dõi hành trình từ đăng ký đến giao dịch đầu tiên

**Đối tượng sử dụng:**
- **Đội PM/BA** — GA4 Explore dashboards (feature adoption, funnel analysis)
- **Ban quản lý** — KPI scorecard (DAU/MAU, tỷ lệ retention, tỷ lệ conversion)

---

## 2. Kiến Trúc

### Hướng tiếp cận: Centralized Analytics Service

Toàn bộ code analytics đi qua một service layer duy nhất. Không có screen hay saga nào gọi Firebase trực tiếp.

```
Screen / Redux Saga
      ↓
analyticsService.trackXxx()     ← API công khai duy nhất
      ↓
events.ts (tên event type-safe)
      ↓
Firebase GA4 Analytics
```

**Cấu trúc file trong nhsv-mts-rn:**
```
src/utils/analytics/
├── analyticsService.ts     ← Public API — mọi tracking call đều vào đây
├── events.ts               ← Hằng số tên event + kiểu TypeScript cho params
├── userProperties.ts       ← Hàm set user properties
└── ekycTracking.ts         ← Helper riêng cho eKYC (timestamp, theo dõi step)
```

**Nguyên tắc:**
- `analyticsService.ts` là file DUY NHẤT import `@react-native-firebase/analytics`
- Tất cả lệnh `analytics().logEvent()` trực tiếp hiện tại trong screens/sagas được refactor qua service

---

## 3. Quy Ước Đặt Tên Event

**Pattern:** `{danh từ}_{động từ}` — danh từ trước, hành động sau

```
✅ feature_viewed          (không phải view_feature)
✅ order_initiated         (không phải initiate_order)
✅ ekyc_step_entered       (không phải enter_ekyc_step)
✅ notification_opened
✅ symbol_searched
```

**Tham số chuẩn có trong mọi event:**
```typescript
{
  screen_name: string      // màn hình hiện tại (tái sử dụng GA4 dimension có sẵn)
  timestamp_ms: number     // epoch ms — dùng để tính khoảng thời gian giữa các event
}
```

---

## 4. Danh Mục Event

### 4.1 — Sử Dụng Tính Năng (P1)

Theo dõi tính năng nào user thực sự tương tác, không chỉ xem qua.

| Event | Tham số chính | Trigger |
|-------|--------------|---------|
| `feature_viewed` | `feature_name`, `screen_name` | User vào tab/màn hình chính |
| `feature_action` | `feature_name`, `action_type`, `screen_name` | User thực hiện thao tác có nghĩa (lọc, tap cổ phiếu, cuộn) |
| `tab_switched` | `from_tab`, `to_tab` | User chuyển tab bottom navigation |
| `symbol_searched` | `query`, `result_count` | User gõ vào ô tìm kiếm |
| `symbol_selected` | `symbol`, `source` | User tap vào mã cổ phiếu |
| `stock_detail_viewed` | `symbol`, `source`, `screen_name` | User mở màn hình chi tiết cổ phiếu |

**Giá trị enum `feature_name`:**
```
market_equity | market_derivatives | market_watchlist | market_topstocks |
market_cw | market_etf | market_index | order_trade | order_onetouch |
portfolio | asset | orderbook | news | education | broker_chat |
cash_flow | rights | margin
```

**Enum `source` cho symbol_selected:**
```
search | watchlist | market_table | top_stocks | recently_viewed | notification
```

---

### 4.2 — Trading Funnel (P0)

**Khoảng trống nghiêm trọng** — hiện tại không có event trading nào. Đây là core business metrics.

| Event | Tham số chính | Trigger |
|-------|--------------|---------|
| `order_initiated` | `order_type`, `side`, `symbol`, `market`, `screen_name` | User mở form lệnh / bắt đầu điền |
| `order_confirmed` | `order_type`, `side`, `symbol`, `market` | User bấm xác nhận — lệnh gửi lên API |
| `order_cancelled` | `symbol`, `source` (user_action/system), `screen_name` | Lệnh bị hủy |
| `portfolio_viewed` | `account_type`, `view_mode`, `screen_name` | User mở tab danh mục |
| `cash_flow_viewed` | `transaction_type`, `view_mode`, `screen_name` | User mở CashFlowScreen |

**Enum `order_type`:** `LO | MP | ATO | ATC | MAK | MOK | PLO | TP | SL | OCO | STOP`
**Enum `market`:** `equity | derivatives`
**Enum `side`:** `buy | sell`

**Enum `transaction_type`** (cho `cash_flow_viewed`): `withdraw | internal_transfer | cash_in_advance`
**Enum `view_mode`** (cho `cash_flow_viewed`): `request | history`

> **Ghi chú `cash_flow_viewed`:** Screen có 2 cấp tab — tab loại giao dịch (withdraw/internal_transfer/cash_in_advance) × tab chế độ (request form / history list). Event fire mỗi khi user chuyển tab ở cấp 1. Ví dụ: vào màn hình lần đầu → `transaction_type: withdraw, view_mode: request`; chuyển sang tab History → `transaction_type: withdraw, view_mode: history`.

---

### 4.3 — eKYC Full Funnel (P0)

**Thay thế và mở rộng** các eKYC event hiện tại. Toàn bộ event cũ được deprecated.

| Event mới | Thay thế | Tham số chính |
|-----------|---------|--------------|
| `ekyc_started` | `ekyc_start` | `entry_point`, `timestamp_ms` |
| `ekyc_step_entered` | `ekyc_screen_view` | `step_name`, `step_index`, `timestamp_ms` |
| `ekyc_step_completed` | *(mới)* | `step_name`, `step_index`, `duration_ms` |
| `ekyc_drop_off` | `ekyc_drop_off` (**đang lỗi**) | `step_name`, `step_index`, `time_spent_ms` |
| `ekyc_scan_attempted` | *(mới)* | `doc_type`, `attempt_number` |
| `ekyc_scan_result` | `ekyc_scan_id_success` + `ekyc_error` | `doc_type`, `success`, `error_code?`, `attempt_number` |
| `ekyc_completed` | `ekyc_complete` | `total_duration_ms`, `retry_count` |

**Enum `step_name` (theo thứ tự):**
```
otp_verification → document_selection → scan_id_front → scan_id_back →
scan_face → personal_info → additional_services → bank_registration →
policy_confirm → sign_contract
```

**Enum `doc_type`:** `id_front | id_back | face`

**Logic timestamp (triển khai trong `ekycTracking.ts`):**
```typescript
// Khi vào step: ghi lại thời điểm bắt đầu
ekycTracking.enterStep('scan_id_front')

// Khi hoàn thành step: tự động tính duration
ekycTracking.completeStep('scan_id_front')
// → fire ekyc_step_completed với duration_ms tính toán nội bộ
```

**Sửa bug `ekyc_drop_off` (Bug #1):**

Bug hiện tại: event chỉ fire ở ~3/10 trường hợp drop-off thực tế. Cần gắn event vào `AppState` change (app chuyển sang background) + listener `navigation.beforeRemove` cho TẤT CẢ màn hình eKYC — không chỉ các màn hình đang được instrument.

---

### 4.4 — Tín Hiệu Retention (P1)

| Event | Tham số chính | Trigger |
|-------|--------------|---------|
| `app_opened` | `open_source`, `notification_type?` | App lên foreground |
| `session_ended` | `session_duration_ms`, `last_feature` | App xuống background |
| `notification_received` | `notification_type` | Push notification đến |
| `notification_opened` | `notification_type`, `deep_link_target` | User tap vào notification |
| `notification_permission` | `granted` (boolean) | Sau khi hiện prompt xin quyền |

**Enum `open_source`:** `direct | push_order_match | push_otp | push_news | push_promo | widget`
**Enum `notification_type`:** `order_match | otp | news | promo | system`

---

### 4.5 — User Properties (set một lần, segment mọi thứ)

User properties cho phép lọc BẤT KỲ report nào theo phân khúc user. Set khi đăng nhập và cập nhật khi có dữ liệu thay đổi.

```typescript
// Gọi sau khi đăng nhập + sau khi hoàn thành eKYC
analytics().setUserId(sha256(userId))  // hash — không bao giờ gửi ID gốc

analytics().setUserProperties({
  user_type: 'registered_only' | 'ekyc_completed' | 'active_trader',
  account_type: 'normal' | 'margin' | 'none',
  has_derivatives_account: 'true' | 'false',
  preferred_market: 'equity' | 'derivatives' | 'both' | 'unknown',
})
```

**Điều kiện cập nhật:**
- `user_type` → nâng lên `ekyc_completed` khi `ekyc_complete`, nâng lên `active_trader` khi `order_confirmed` đầu tiên
- `preferred_market` → tính lại sau mỗi `order_confirmed`: equity ≥ 70% tổng → `equity`; derivatives ≥ 70% → `derivatives`; còn lại → `both`. Cần tối thiểu 3 lệnh trước khi chuyển khỏi `unknown`

---

## 5. Cấu Hình GA4

### 5.1 — Dimensions Hiện Có (Tái sử dụng / Sửa)

| Dimension | Hành động | Ghi chú |
|-----------|----------|---------|
| `error_code` | ✅ Tái sử dụng | Dùng trong `ekyc_scan_result`, lỗi `order_*` |
| `error_type` | ✅ Tái sử dụng + Sửa tên UI | Hiện UI hiển thị `"error_typ"` — sửa typo trong GA4 Admin |
| `screen_name` | ✅ Tái sử dụng | Hiện UI hiển thị `"dropoff_screen"` — đổi tên thành `"screen_name"` |
| `ekyc_status` | ✅ Tái sử dụng | Giữ cho `ekyc_started` |
| `broker_manager` | ➡️ Giữ nguyên | Ngoài phạm vi |
| `referrer_type` | ➡️ Giữ nguyên | Ngoài phạm vi |

### 5.2 — Dimensions Cần Thêm Mới

**Event-scoped:**
| Tên param | Tên UI | Dùng trong |
|-----------|--------|-----------|
| `feature_name` | Feature Name | `feature_viewed`, `feature_action` |
| `order_type` | Order Type | `order_initiated`, `order_confirmed` |
| `ekyc_step` | eKYC Step | `ekyc_step_entered`, `ekyc_step_completed`, `ekyc_drop_off` |
| `open_source` | App Open Source | `app_opened` |
| `notification_type` | Notification Type | `notification_*` |

**User-scoped (mới hoàn toàn — hiện chưa có):**
| Tên property | Tên UI | Giá trị |
|--------------|--------|---------|
| `user_type` | User Type | registered_only / ekyc_completed / active_trader |
| `account_type` | Account Type | none / normal / margin |
| `has_derivatives_account` | Has Derivatives | true / false |
| `preferred_market` | Preferred Market | equity / derivatives / both / unknown |

### 5.3 — Metrics Hiện Có (Tái sử dụng)

| Metric | Loại | Dùng cho |
|--------|------|---------|
| `time_spent` | SECONDS | Thời gian từng step trong eKYC |
| `total_time_spent` | SECONDS | Tổng thời gian hoàn thành eKYC |
| `total_attempts` | INTEGER | Số lần thử scan lại |

> **Lưu ý:** Event mới gửi duration theo `_ms` (milliseconds) trong tham số event, nhưng GA4 lưu qua metric `time_spent` đã đăng ký dạng SECONDS. Service layer xử lý việc chuyển đổi này.

---

## 6. Bugs Cần Sửa Ngay

| # | Bug | Ảnh hưởng | Cách sửa |
|---|-----|----------|---------|
| 1 | `ekyc_drop_off` chỉ fire ~22% trường hợp drop-off thực tế | Không thể tin vào dữ liệu funnel | Thêm `AppState` + listener `navigation.beforeRemove` vào TẤT CẢ màn hình eKYC |
| 2 | Tên UI `error_type` bị typo thành `"error_typ"` | Report hiển thị nhãn sai | Sửa trong GA4 Admin → Custom definitions |
| 3 | Tên UI `screen_name` đang là `"dropoff_screen"` | Gây hiểu nhầm trong report | Đổi tên trong GA4 Admin → Custom definitions |
| 4 | Không có lệnh `setUserId()` ở bất kỳ đâu | Không thể theo dõi hành trình user qua các session | Thêm vào login success handler |

---

## 7. GA4 Reports & Dashboards

### Dành cho PM/BA — GA4 Explore

**Report 1: Feature Adoption (Mức độ sử dụng tính năng)**
- Biểu đồ: Bar chart — số lần `feature_viewed` theo `feature_name`, theo tuần
- Phụ: Funnel — `feature_viewed` → `feature_action` (phân biệt user thụ động vs chủ động)
- Segment theo `user_type`: active_trader vs registered_only — tính năng nào phục vụ phân khúc nào

**Report 2: eKYC Funnel**
- Biểu đồ: Funnel — 10 bước từ `ekyc_started` → `ekyc_completed`
- Bảng: Thời gian trung bình `time_spent` mỗi bước — đánh dấu bước >120 giây
- Biểu đồ: Tỷ lệ thành công `ekyc_scan_result` theo `doc_type` — so sánh scan CCCD vs scan mặt
- Xu hướng: Tỷ lệ drop-off theo từng bước, theo tuần (đo cải thiện UX sau mỗi release)

**Report 3: Trading Activation**
- Funnel: `ekyc_completed` → `order_initiated` → `order_confirmed` (lọc `is_first_order: true`)
- Phân tích theo `order_type` và `market` — trader mới dùng loại lệnh gì đầu tiên?

### Dành cho Ban Quản Lý — KPI Scorecard

| KPI | Nguồn dữ liệu | Mục tiêu |
|-----|--------------|---------|
| DAU / MAU | Active Users (có sẵn trong GA4) | Theo dõi xu hướng hàng tuần |
| D1 / D7 / D30 Retention | User Retention report (có sẵn) | Lấy baseline sau khi triển khai |
| Tỷ lệ hoàn thành eKYC | `ekyc_completed` / `ekyc_started` | Hiện tại: 58% — mục tiêu 70% |
| Tỷ lệ Trading Activation | users(`order_confirmed`) / users(`ekyc_completed`) | Lấy baseline sau triển khai |
| Tỷ lệ tương tác tính năng | users(`feature_action`) / users(`feature_viewed`) | Lấy baseline sau triển khai |
| Tỷ lệ click Push Notification | `notification_opened` / `notification_received` | Lấy baseline sau triển khai |

---

## 8. Kế Hoạch Triển Khai

### Sprint 1 — P0 (Nền tảng)
- [ ] Tạo cấu trúc thư mục `src/utils/analytics/`
- [ ] Triển khai `analyticsService.ts` bọc Firebase
- [ ] Triển khai `events.ts` với hằng số tên event và kiểu tham số
- [ ] Thêm `setUserId()` vào login success handler
- [ ] Set user properties khi đăng nhập + hoàn thành eKYC
- [ ] Sửa bug `ekyc_drop_off` (AppState + beforeRemove listeners cho tất cả màn hình eKYC)
- [ ] Thêm `ekyc_step_entered` + `ekyc_step_completed` với timestamp tracking (`ekycTracking.ts`)
- [ ] Thêm `order_initiated` + `order_confirmed` (với logic phát hiện `is_first_order`)

### Sprint 2 — P1 (Engagement)
- [ ] Thêm `feature_viewed` + `feature_action` cho tất cả màn hình chính
- [ ] Thêm `tab_switched` vào bottom navigation
- [ ] Thêm `app_opened` với logic phát hiện `open_source`
- [ ] Thêm `session_ended` với duration + `last_feature`
- [ ] Thêm `notification_received` + `notification_opened` vào OneSignal handlers
- [ ] Thêm `symbol_searched` + `symbol_selected` vào search flow

### Sprint 3 — P2 (GA4 & Dashboards)
- [ ] Sửa tên UI custom dimension trong GA4 (`error_typ` → `error_type`, `dropoff_screen` → `screen_name`)
- [ ] Đăng ký 5 event-scoped dimensions mới trong GA4 Admin
- [ ] Đăng ký 4 user-scoped properties trong GA4 Admin
- [ ] Xây dựng report Feature Adoption trong GA4 Explore
- [ ] Xây dựng report eKYC Funnel trong GA4 Explore
- [ ] Xây dựng funnel Trading Activation trong GA4 Explore
- [ ] Xây dựng KPI Scorecard cho Ban quản lý

---

## 9. Phạm Vi & Ràng Buộc

**Trong phạm vi:**
- nhsv-mts-rn (app mobile NHSV Pro)
- Firebase GA4 là nền tảng analytics chính
- Tracking eKYC, Retention và Trading funnel

**Ngoài phạm vi:**
- BigQuery export / Looker Studio (Phase 2 khi team cần query SQL)
- Web analytics
- Tracking phía backend

**Giới hạn GA4 Standard (đang dùng vs giới hạn):**
- Event-scoped custom dimensions: 6 đang dùng + 5 mới = **11 / 50** ✅
- User-scoped custom dimensions: 0 đang dùng + 4 mới = **4 / 25** ✅
- Custom metrics: 3 đang dùng + 0 mới = **3 / 50** ✅

---

**Trạng thái tài liệu:** Bản nháp
**Dành cho:** PM/BA, Đội Mobile Dev
**Bước tiếp theo:** Dev team review → Sprint 1 planning → Triển khai
