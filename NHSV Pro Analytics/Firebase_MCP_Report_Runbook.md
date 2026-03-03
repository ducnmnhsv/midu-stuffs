# Firebase MCP — Runbook gọi Report

Dùng khi muốn **lấy báo cáo qua Firebase MCP** (Cursor). Chỉ **Crashlytics** (crash/exception), không có GA4/eKYC.

---

## Điều kiện

- Firebase MCP đã bật và kết nối (Cursor Settings → MCP).
- Đã chạy `firebase login` (hoặc tương đương) cho project tương ứng.

---

## App IDs (project nhsv-pro-1422025 — NHSV Pro)

| App | App Id |
|-----|--------|
| Android Prod | `1:195382997861:android:5e663ec7edc98fe308e78c` |
| Android UAT | `1:195382997861:android:0c8ab079d4751e2308e78c` |
| iOS Prod | `1:195382997861:ios:8f83aecb1178b35908e78c` |
| iOS UAT | `1:195382997861:ios:e3affe4f134cca6708e78c` |

*(Lấy mới bằng MCP: `firebase_list_apps`.)*

---

## Các report có thể gọi

Tool: **`crashlytics_get_report`** (server: `user-Firebase`).

| report | Ý nghĩa |
|--------|---------|
| `topIssues` | Top issues (crash/exception), sort theo số event. |
| `topVariants` | Top issue variants (cần filter `issueId`). |
| `topVersions` | Số event theo version app. |
| `topOperatingSystems` | Số event theo OS. |
| `topAndroidDevices` | Số event theo thiết bị Android. |
| `topAppleDevices` | Số event theo thiết bị Apple. |

---

## Template gọi (AI / agent)

**1. Top Issues (7 ngày mặc định, FATAL + NON_FATAL)**

- **Tool:** `crashlytics_get_report`
- **Arguments:**
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

**2. Top Issues — khoảng thời gian tùy chọn (ISO 8601)**

```json
{
  "appId": "1:195382997861:android:5e663ec7edc98fe308e78c",
  "report": "topIssues",
  "filter": {
    "intervalStartTime": "2025-02-24T00:00:00Z",
    "intervalEndTime": "2025-03-03T23:59:59Z",
    "issueErrorTypes": ["FATAL", "NON_FATAL"]
  },
  "pageSize": 20
}
```

**3. Top Versions (phân bố crash theo version)**

```json
{
  "appId": "1:195382997861:android:5e663ec7edc98fe308e78c",
  "report": "topVersions",
  "pageSize": 10
}
```

**4. Top Operating Systems**

```json
{
  "appId": "1:195382997861:android:5e663ec7edc98fe308e78c",
  "report": "topOperatingSystems",
  "pageSize": 10
}
```

---

## Đọc hướng dẫn Crashlytics (trước khi gọi)

Nên đọc resource **Crashlytics Reports Guide** qua MCP:

- **Resource URI:** `firebase://guides/crashlytics/reports`
- **Cách đọc:** Gọi `fetch_mcp_resource` (server: `user-Firebase`, uri: `firebase://guides/crashlytics/reports`) hoặc tool `firebase_read_resources` với `uris: ["firebase://guides/crashlytics/reports"]`.

---

## Lưu ý

- **404 / entity not found:** App có thể chưa có dữ liệu Crashlytics hoặc Crashlytics chưa được link đúng; kiểm tra Firebase Console → Crashlytics.
- **eKYC / GA4:** Report funnel eKYC, completion rate, v.v. **không** có trong Firebase MCP; dùng GA4 Exploration hoặc Looker Studio (xem [eKYC_GA4_Report_Calculated_Metrics_Guide.md](./eKYC_GA4_Report_Calculated_Metrics_Guide.md)).
