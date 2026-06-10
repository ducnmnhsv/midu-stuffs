# Google Analytics MCP — Thiết lập & dùng trong Cursor

MCP server chính thức của Google để truy vấn **Google Analytics (GA4)** qua Cursor: account/property, report, realtime.

**Repo:** [googleanalytics/google-analytics-mcp](https://github.com/googleanalytics/google-analytics-mcp)

---

## 1. Điều kiện

- **Python 3.10+**
- **pipx** (để chạy `analytics-mcp` không cần clone repo)
- Tài khoản Google Cloud có **quyền đọc** Google Analytics (GA4)
- Đã bật **Google Analytics Admin API** và **Google Analytics Data API** trong GCP project

---

## 2. Cài đặt một lần

### 2.1 Cài pipx (nếu chưa có)

```bash
# macOS (Homebrew)
brew install pipx
pipx ensurepath
```

### 2.2 Bật API trong GCP

1. Vào [Google Cloud Console](https://console.cloud.google.com/) → chọn project (ví dụ project chứa GA4 của NHSV Pro).
2. **APIs & Services** → **Enable APIs and Services**.
3. Bật:
   - [Google Analytics Admin API](https://console.cloud.google.com/apis/library/analyticsadmin.googleapis.com)
   - [Google Analytics Data API](https://console.cloud.google.com/apis/library/analyticsdata.googleapis.com)

### 2.3 Cài gcloud CLI (nếu chưa có)

```bash
# macOS (Homebrew)
brew install --cask google-cloud-sdk
```

Mở terminal mới (hoặc `source` shell config của gcloud) rồi chạy bước 2.4.

### 2.4 Credentials (Application Default Credentials)

File **client secret** (OAuth client JSON) dùng để *đăng nhập*; sau khi đăng nhập, gcloud tạo file credentials *khác* — file đó mới đưa vào `GOOGLE_APPLICATION_CREDENTIALS`.

**Client secret (Desktop app) — dùng cho lệnh login:**

```
/Users/ducnguyen/Downloads/client_secret_195382997861-3keru0vknsvvf5f2aq747le86ivd012m.apps.googleusercontent.com.json
```

Chạy đăng nhập:

```bash
gcloud auth application-default login \
  --scopes https://www.googleapis.com/auth/analytics.readonly,https://www.googleapis.com/auth/cloud-platform \
  --client-id-file="/Users/ducnguyen/Downloads/client_secret_195382997861-3keru0vknsvvf5f2aq747le86ivd012m.apps.googleusercontent.com.json"
```

Sau khi đăng nhập xong, terminal in ra đường dẫn **file credentials** (không phải client secret), ví dụ:

```
Credentials saved to file: [/Users/ducnguyen/.config/gcloud/application_default_credentials.json]
```

**Lưu lại đường dẫn đó** — đây mới là giá trị cho `GOOGLE_APPLICATION_CREDENTIALS` trong bước 2.5.

*(Nếu dùng Service Account: tạo key JSON và dùng biến `GOOGLE_APPLICATION_CREDENTIALS` trỏ tới file key đó.)*

### 2.5 Cấu hình MCP trong Cursor

File **`.cursor/mcp.json`** (trong project tradex-monitoring) đã có sẵn block `analytics-mcp`. Chỉ cần **sửa 2 giá trị**:

| Key | Ý nghĩa | Ví dụ |
|-----|---------|--------|
| `GOOGLE_APPLICATION_CREDENTIALS` | Đường dẫn file **sau khi login** (in ra bởi `gcloud auth application-default login`), **không** dùng file client_secret_*.json | `"/Users/ducnguyen/.config/gcloud/application_default_credentials.json"` |
| `GOOGLE_CLOUD_PROJECT` | GCP Project ID (có GA4) | `"nhsv-pro-1422025"` (từ client secret: 195382997861) |

Ví dụ sau khi sửa (dùng đúng đường dẫn mà gcloud in ra sau bước 2.4):

```json
{
  "mcpServers": {
    "analytics-mcp": {
      "command": "pipx",
      "args": ["run", "analytics-mcp"],
      "env": {
        "GOOGLE_APPLICATION_CREDENTIALS": "/Users/ducnguyen/.config/gcloud/application_default_credentials.json",
        "GOOGLE_CLOUD_PROJECT": "nhsv-pro-1422025"
      }
    }
  }
}
```

Sau khi sửa: **restart Cursor** để MCP load lại.

---

## 3. Tools có sẵn (khi dùng trong Cursor)

| Tool | Mô tả |
|------|--------|
| `get_account_summaries` | Danh sách GA accounts & properties |
| `get_property_details` | Chi tiết một property |
| `list_google_ads_links` | Các link Google Ads của property |
| `run_report` | Chạy report GA4 (Data API) |
| `get_custom_dimensions_and_metrics` | Custom dimensions/metrics của property |
| `run_realtime_report` | Realtime report |

---

## 4. Gợi ý prompt trong Cursor

- *"What can the analytics-mcp server do?"*
- *"Give me details about my Google Analytics property with 'NHSV' in the name"*
- *"What are the most popular events in my GA4 property in the last 180 days?"*
- *"Were most of my users in the last 6 months logged in?"*
- *"What are the custom dimensions and custom metrics in my property?"*

---

## 5. Liên quan tới Firebase MCP & GA4

- **Firebase MCP** (đã dùng): Crashlytics, app list, v.v. — **không** có report GA4.
- **Google Analytics MCP**: Report GA4 (sự kiện, user, realtime, custom dimensions/metrics).

Với **eKYC / GA4** (custom dimensions, funnel): dùng **Google Analytics MCP** (`run_report`, `get_custom_dimensions_and_metrics`) trong Cursor; hoặc GA4 Exploration / Looker Studio như [eKYC_GA4_Report_Calculated_Metrics_Guide.md](./eKYC_GA4_Report_Calculated_Metrics_Guide.md).

---

**Document Status:** ✅  
**For:** Dev/PM dùng Cursor với GA4  
**Next Steps:** Sửa `.cursor/mcp.json` → restart Cursor → thử prompt trên.
