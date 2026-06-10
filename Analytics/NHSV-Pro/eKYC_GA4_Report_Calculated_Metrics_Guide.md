# eKYC GA4 — Hướng dẫn Report & Calculated Metrics

Hướng dẫn thiết lập report và theo dõi các **calculated metrics** cho luồng eKYC trong GA4 (Firebase project **tradex-nhsv**), theo spec [GA Tracking - In app NHSV Pro - eKYC flow (v2)].

---

## 1. Chuẩn bị: Custom dimensions & Custom metrics trong GA4

Để các calculated metrics và report hoạt động đúng, cần **đăng ký** các tham số event làm custom dimensions/metrics trong GA4.

**Đường dẫn:** GA4 → **Admin** → **Data display** → **Custom definitions** → **Create custom dimension** / **Create custom metric**.

### 1.1 Custom dimensions (đăng ký)

| Dimension name (GA4) | Scope | Event parameter (từ app) | Mô tả |
|---------------------|--------|---------------------------|--------|
| `ekyc_status` | Event | (set khi gửi event: in_progress, completed, drop_off) | Trạng thái luồng eKYC |
| `dropoff_screen` | Event | `screen_name` (trong event `ekyc_drop_off`) | Màn user thoát |
| `error_code` | Event | `error_code` (trong event `ekyc_error`) | Mã lỗi chi tiết |
| `error_type` | Event | `error_type` (trong event `ekyc_error`) | Loại lỗi (scan_id_fail, lotte, …) |
| `referrer_type` | Event | `referrer_type` (trong event `ekyc_complete`) | Nguồn giới thiệu (staff, customer, ads, others) |
| `broker_manager` | Event | `broker_manager` (trong event `ekyc_complete`) | Có chọn quản lý hay không (yes/no) |
| `screen_name` | Event | `screen_name` (trong `ekyc_screen_view`, `ekyc_drop_off`, …) | Tên màn trong eKYC |

**Lưu ý:** Trong GA4, **Event parameter** phải khớp đúng tên parameter app gửi (ví dụ `screen_name`, `error_type`). Sau khi tạo, data mới sẽ bắt đầu được gắn dimension; data cũ không backfill.

### 1.2 Custom metrics (đăng ký)

| Metric name (GA4) | Scope | Event parameter | Unit | Mô tả |
|-------------------|--------|------------------|------|--------|
| `time_spent` | Event | `time_spent` | Time (seconds) | Thời gian trên từng màn (trong `ekyc_screen_view`) |
| `total_time_spent` | Event | `total_time_spent` | Time (seconds) | Tổng thời gian luồng eKYC (trong `ekyc_complete`) |
| `total_attempts` | Event | `total_attempts` | Standard | Số lần thử trước khi thành công (trong `ekyc_scan_id_success`, `ekyc_scan_face_success`) |
| `attempt_count` | Event | `attempt_count` | Standard | Số lần thử (trong `ekyc_scan_id_attempt`, `ekyc_scan_face_attempt` khi đã implement) |

**Cách tạo:** Custom definitions → Create custom metric → chọn **Event** scope, nhập **Event parameter** đúng tên app gửi, chọn Unit (Time (seconds) hoặc Standard).

---

## 2. Giới hạn Calculated metrics trong GA4

- **Standard property:** tối đa **5** calculated metrics.
- **360 property:** tối đa **50** calculated metrics.
- Calculated metric **không thể** dùng “số lần event X” trực tiếp (GA4 không có metric “Count of ekyc_start”). Chỉ dùng được: metric có sẵn (ví dụ Event count, Sessions) và **custom metrics** đã tạo từ event parameters.

**Hệ quả:** Các chỉ số dạng **tỷ lệ** (completion_rate, dropoff_rate, error_rate) = (Count event A / Count event B) * 100 **không thể** tạo đủ bằng một formula trong GA4. Cần dùng **Exploration** (xem Event count theo Event name) rồi tính tay / export / hoặc dùng **Looker Studio**.

---

## 3. Calculated metrics trong GA4 (giới hạn 5 cái / property)

Trong GA4, **Event count** trong công thức calculated metric là **tổng số event** (toàn property), không phải “số lần event ekyc_complete”. Do đó **không thể** tạo công thức dạng “Sum(total_time_spent) / Count(ekyc_complete)” trực tiếp trong Custom definitions.

- **average_time_to_complete**, **completion_rate**, **dropoff_rate**, **error_rate**, v.v.: nên tính trong **Exploration** (filter theo Event name, lấy Event count từng event rồi tính tay/export) hoặc trong **Looker Studio** (calculated field từ blended data).
- Nếu có nhu cầu dùng 5 slot calculated metrics cho mục đích khác (ví dụ tỷ lệ doanh thu), có thể dùng; với eKYC, ưu tiên **Exploration + Looker Studio** để có đủ các tỷ lệ và trung bình theo spec.

---

## 4. Report trong GA4: Exploration “eKYC — Event counts & rates”

Dùng Exploration để xem **số lần từng event** và từ đó tính (tay hoặc export) các calculated metrics trong spec.

### 4.1 Tạo Exploration

1. GA4 → **Explore** → **Create new exploration** → chọn **Free form**.
2. Đặt tên: **eKYC — Event counts & rates**.

### 4.2 Cấu hình

| Phần | Thiết lập |
|------|------------|
| **Dimensions** | Kéo **Event name** vào Rows. |
| **Metrics** | Kéo **Event count** vào Values. Nếu đã đăng ký custom metric **Total time spent** (từ `ekyc_complete`), kéo thêm vào. |
| **Filter** | **Include** → Event name **matches regex**: `^ekyc_|^econtract_signing$` (hoặc liệt kê: `ekyc_start`, `ekyc_screen_view`, `ekyc_drop_off`, `ekyc_complete`, `ekyc_error`, `ekyc_scan_id_success`, `ekyc_scan_face_success`, `econtract_signing`). |
| **Date range** | Chọn khoảng thời gian cần theo dõi. |

Kết quả: bảng **Event name | Event count** (và Total time spent nếu có). Từ đây có thể:

- **completion_rate** = (Event count của `ekyc_complete` / Event count của `ekyc_start`) × 100.
- **dropoff_rate** = (Event count của `ekyc_drop_off` / Event count của `ekyc_start`) × 100.
- **error_rate** = (Event count của `ekyc_error` / Event count của `ekyc_start`) × 100.

### 4.3 Exploration theo screen (drop-off & time per screen)

1. Tạo Exploration **Free form** mới, tên: **eKYC — By screen**.
2. **Dimensions:** **screen_name** (custom dimension từ parameter `screen_name`).
3. **Metrics:** **Event count**; nếu có custom metric **time_spent**, thêm **Sum(time_spent)** hoặc **Average(time_spent)**.
4. **Filter:** Event name = `ekyc_screen_view` **hoặc** `ekyc_drop_off`.
5. Có thể thêm **Segment** hoặc filter thêm theo Event name để tách:
   - Số lần xem màn (ekyc_screen_view) → tính **average_time_per_screen** (trung bình time_spent theo screen_name).
   - Số lần thoát theo màn (ekyc_drop_off) → **dropoff_rate** theo từng màn.

---

## 5. Bảng tham chiếu: Spec calculated metrics → Cách lấy trong GA4/Looker

| Calculated metric (spec) | Công thức | Cách lấy trong GA4 / Looker |
|--------------------------|-----------|------------------------------|
| **completion_rate** | (Count `ekyc_complete` / Count `ekyc_start`) × 100 | Exploration: Event name + Event count → lấy 2 số, tính tay hoặc Looker Studio calculated field. |
| **dropoff_rate** | (Count `ekyc_drop_off` / Count `ekyc_start`) × 100 | Tương tự từ Exploration. |
| **average_time_to_complete** | Average(`total_time_spent`) với event `ekyc_complete` | Exploration filter event = `ekyc_complete`, metric Sum(total_time_spent) / Event count. Hoặc GA4 calculated metric (khi filter đúng). |
| **average_time_per_screen** | Average(`time_spent`) theo từng screen | Exploration: dimension screen_name, filter event = `ekyc_screen_view`, metric Average(time_spent). |
| **error_rate** | (Count `ekyc_error` / Count `ekyc_start`) × 100 | Từ Exploration Event name + Event count. |
| **average_id_attempts** | Average(`total_attempts`) với event `ekyc_scan_id_success` | Exploration filter event = `ekyc_scan_id_success`, (Sum total_attempts / Event count). |
| **average_face_attempts** | Average(`total_attempts`) với event `ekyc_scan_face_success` | Tương tự với `ekyc_scan_face_success`. |
| **referrer_type_rate** | Mỗi referrer_type / Total `ekyc_complete` × 100 | Exploration: dimension `referrer_type`, filter event = `ekyc_complete`, metric Event count. |
| **top_referrer_value** | Mode của `referrer_value` theo referrer_type | Exploration hoặc report: dimension referrer_type + referrer_value (nếu đăng ký), xem phân bố. |
| **broker_manager_rate** | Count(broker_manager = yes/no) / Total `ekyc_complete` × 100 | Dimension `broker_manager`, filter event = `ekyc_complete`, Event count. |

**Lưu ý:** `ekyc_scan_id_attempt` và `ekyc_scan_face_attempt` trong spec — app hiện có thể mới gửi `ekyc_scan_id_success` / `ekyc_scan_face_success` với `total_attempts`. Nếu sau này app gửi thêm event attempt với `attempt_count`, có thể dùng để tính retry_rate / average attempts chi tiết hơn.

---

## 6. Looker Studio (khuyến nghị cho dashboard đầy đủ)

Để có **một dashboard** hiển thị sẵn completion_rate, dropoff_rate, average_time_to_complete, … không phải mở nhiều Exploration:

1. **Looker Studio** → Create report → Data source: **Google Analytics 4** (property tradex-nhsv).
2. Kéo **Event name** + **Event count** vào bảng; filter Event name bằng các event eKYC.
3. Dùng **Blended data** hoặc **Calculated field**:
   - Nguồn 1: event `ekyc_start` → metric `Starts = Event count`.
   - Nguồn 2: event `ekyc_complete` → metric `Completes = Event count`.
   - Calculated field: **Completion rate** = `Completes / Starts * 100`.
4. Tương tự tạo **Dropoff rate**, **Error rate** từ Event count của `ekyc_drop_off`, `ekyc_error`.
5. **Average time to complete:** dùng custom metric Sum(`total_time_spent`) / Count(`ekyc_complete`) trong blended data hoặc calculated field.

Như vậy toàn bộ calculated metrics trong spec đều có thể hiển thị trên một report Looker Studio.

---

## 7. Screen names (map spec ↔ app)

Để filter hoặc breakdown theo màn, dùng giá trị `screen_name` đúng với app:

| Spec screen name | Ghi chú (app có thể dùng tên tương đương) |
|------------------|------------------------------------------|
| personal_information | App: `[eKYC]_PersonalInfo` hoặc `personal_information` |
| otp_verification | `[eKYC]_OTPverification` |
| document_selection | `[eKYC]_DocumentSelection` |
| confirmation_info | `[eKYC]_ConfirmationInfo` |
| face_authentication_instruction | `[eKYC]_FaceAuthenticationInstruction` |
| face_authentication_complete | Trong event: `face_authentication_complete` |
| additional_services_registration | `[eKYC]_AdditionalServicesRegistration` |
| bank_account_registration | `bank_account_registration` / `BANK_ACCOUNT_REGISTRATION` |
| others_information | `others_information` |
| terms_and_conditions | `[eKYC]_TnC` |
| create_contract | (nếu có) |
| econtract_signing | `[eKYC]_SignContract` |
| open_account_successfully | `open_account_successfully` |

Nên kiểm tra trong GA4 (DebugView hoặc Exploration) giá trị thực tế của `screen_name` để filter đúng.

---

## 8. Checklist triển khai

- [ ] Đăng ký **Custom dimensions** (screen_name, error_type, error_code, referrer_type, broker_manager, …) trong GA4.
- [ ] Đăng ký **Custom metrics** (time_spent, total_time_spent, total_attempts, attempt_count nếu có).
- [ ] Tạo **Exploration “eKYC — Event counts & rates”** (Event name + Event count, filter ekyc_*).
- [ ] Tạo **Exploration “eKYC — By screen”** (screen_name + Event count / time_spent) cho drop-off và time per screen.
- [ ] (Tùy chọn) Tạo tối đa 5 **Calculated metrics** trong GA4, ưu tiên average_time_to_complete khi filter `ekyc_complete`.
- [ ] (Khuyến nghị) Tạo **Looker Studio report** với blended data + calculated fields cho completion_rate, dropoff_rate, error_rate, average_time_to_complete, referrer_type_rate, broker_manager_rate.

---

## 9. Báo cáo qua Firebase MCP

**Lưu ý:** Firebase MCP **không** cung cấp báo cáo GA4 / Analytics (event eKYC, conversion, v.v.). Các report trong MCP chỉ là **Crashlytics** (crash, exception, ANR).

### 9.1 Loại report có thể gọi qua Firebase MCP

| Report (tool) | Mô tả |
|---------------|--------|
| **crashlytics_get_report** | Báo cáo số liệu Crashlytics: top issues, top versions, top OS, top devices. Dùng để theo dõi crash/exception của app, **không** phải funnel eKYC. |

**Các report có sẵn:** `topIssues`, `topVariants`, `topVersions`, `topOperatingSystems`, `topAppleDevices`, `topAndroidDevices`.

### 9.2 Cách gọi Crashlytics report qua MCP

1. Đảm bảo Firebase MCP đã kết nối (Cursor Settings → MCP → Firebase; đã `firebase login`).
2. Lấy **App ID** của app cần báo cáo (Firebase Console hoặc gọi MCP `firebase_list_apps`).
3. Gọi tool **crashlytics_get_report** với:
   - `appId`: Firebase App ID (ví dụ Android Prod: `1:195382997861:android:5e663ec7edc98fe308e78c`).
   - `report`: một trong `topIssues`, `topVersions`, `topOperatingSystems`, `topAndroidDevices`, `topAppleDevices`.
   - `filter` (tùy chọn): `intervalStartTime`, `intervalEndTime` (ISO 8601, trong 90 ngày); `issueErrorTypes`: `["FATAL","NON_FATAL","ANR"]`.
   - `pageSize`: số dòng (mặc định 10).

**Ví dụ (AI/agent gọi MCP):**

```json
{
  "appId": "1:195382997861:android:5e663ec7edc98fe308e78c",
  "report": "topIssues",
  "filter": {
    "issueErrorTypes": ["FATAL", "NON_FATAL"]
  },
  "pageSize": 15
}
```

### 9.3 eKYC / GA4 report

- **Funnel eKYC, completion_rate, dropoff_rate, v.v.:** làm theo mục 4–6 (GA4 Exploration, Looker Studio), **không** qua Firebase MCP.
- Nếu cần export số liệu GA4 tự động, dùng **GA4 Data API** hoặc **BigQuery export** + script/Looker Studio.

---

**Document Status:** ✅ Ready  
**For:** PM / Analyst — setup GA4 report và theo dõi calculated metrics eKYC  
**Next Steps:** Đăng ký custom definitions trong GA4 → tạo Explorations → (tùy chọn) Looker Studio dashboard. Dùng Firebase MCP chỉ cho Crashlytics reports.
