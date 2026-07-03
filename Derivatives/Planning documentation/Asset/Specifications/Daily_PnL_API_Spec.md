# Daily P&L API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - Daily Profit/Loss  
**Version:** 1.4  
**Date:** April 8, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Query lãi/lỗ theo ngày (realized + unrealized + fee + tax) trong khoảng thời gian. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/Lotte_DR_API_Specs.md) §2.1.8 — DRACC-037 (dr-daily-profit-loss).

---

## 1. Overview

### 1.1 Purpose

Daily P&L API tra cứu lãi/lỗ theo từng ngày trong khoảng thời gian, bao gồm lãi/lỗ đã thực hiện, chưa thực hiện, phí và thuế (net P&L), theo tài khoản/sản phẩm/hợp đồng.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Daily P&L | GET | `/api/v1/derivatives/asset/dailyPnl` |

### 1.3 Response Format Standards

**Success (Query) — TradeX (mọi trang):** năm field tổng hợp ở **root** (flat) + `items` + `nextKey`. FE **chỉ** truyền `nextKey` khi lấy trang tiếp; không gửi `query_type`. Backend tự gọi Lotte `query_type = 0` hoặc `1` theo có/không `nextKey`.

**Lần đầu (Lotte trả `total` + `data_list`):** map `total` → các field `total*`.

**Trang sau (Lotte chỉ trả `data_list`):** `items` là `data_list` mới; **cùng một bộ** `totalRealizedProfitLoss` … `totalNetProfitLoss` như lần đầu — backend **tự giữ** từ bản tổng Lotte lần `query_type = 0` (cache theo khóa truy vấn + user, TTL ngắn) hoặc gọi bổ sung theo policy (xem §5.2). Không tính tổng bằng `sum(items)`.

```json
{
  "totalRealizedProfitLoss": 25000000,
  "totalUnrealizedProfitLoss": 130000,
  "totalFee": 20000,
  "totalTax": 100000,
  "totalNetProfitLoss": 24850000,
  "items": [
    {
      "date": "20260325",
      "accountNumber": "039C110257",
      "accountName": "Nguyễn Văn A",
      "productCode": "VN30F",
      "symbolCode": "41I1FA000",
      "productName": "HDTLVN30",
      "realizedProfitLoss": 12500000,
      "unrealizedProfitLoss": -1000000,
      "fee": 12100,
      "tax": 20000,
      "netProfitLoss": 11500000
    }
  ],
  "nextKey": "20251027039C110257VN30F     41I1FB000"
}
```

**Hết trang:** `nextKey` = `null` (hoặc omit theo convention JSON API — ưu tiên `null` cho rõ).

```json
{
  "totalRealizedProfitLoss": 25000000,
  "totalUnrealizedProfitLoss": 130000,
  "totalFee": 20000,
  "totalTax": 100000,
  "totalNetProfitLoss": 24850000,
  "items": [],
  "nextKey": null
}
```

**Error:**
```json
{
  "code": "ERROR_CODE",
  "message": "Error message"
}
```

or

```json
{
  "code": "INVALID_PARAMETER",
  "params": [ ... ]
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Query: **`totalRealizedProfitLoss`**, **`totalUnrealizedProfitLoss`**, **`totalFee`**, **`totalTax`**, **`totalNetProfitLoss`** (từ Lotte `total` lần đầu; lặp lại trên mọi trang) + **`items`** + root **`nextKey`**
- Không bọc `pagination`; không object `totals` lồng nhau
- Pass-through Lotte messages AS-IS (error path)

---

## 2. Business Rules

### 2.1 Date Rules

| Rule | Value | Description |
|------|-------|-------------|
| Date Format | `yyyyMMdd` | e.g., `20260301` |
| Validation | `dateFrom ≤ dateTo` | Start date must be ≤ end date |
| Max Range | 90 days | `dateTo - dateFrom ≤ 90 days` (TradeX limit; confirm with Lotte) |

### 2.2 Search Type

| TradeX Value | Lotte `search_type` | Description |
|--------------|---------------------|-------------|
| `false` | false | Tra cứu từng tài khoản (mặc định) |
| `true` | true | Tra cứu toàn bộ (cần quyền back-office) |

### 2.3 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, dateFrom, dateTo, productCode | `FIELD_IS_REQUIRED` |
| Date Format | Must be yyyyMMdd | `INVALID_DATE_FORMAT` |
| Date Range | dateFrom ≤ dateTo | `INVALID_DATE_RANGE` |
| Date Range Limit | Max 90 days | `DATE_RANGE_EXCEEDED` |
| Fetch Count | 1 ≤ fetchCount ≤ 100 | `INVALID_FETCH_COUNT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.4 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Daily P&L

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/dailyPnl`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-daily-profit-loss` (DRACC-037) — Lotte_DR §2.1.8. Lotte Method: POST; TradeX exposes GET với query parameters, backend proxy chuyển sang POST body.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh |
| `dateFrom` | String | ✅ | - | Từ ngày (yyyyMMdd) |
| `dateTo` | String | ✅ | - | Tới ngày (yyyyMMdd) |
| `productCode` | String | ✅ | - | Mã sản phẩm |
| `searchAllAccounts` | Boolean | ❌ | false | true: Tra cứu toàn bộ, false: Tra cứu từng TK |
| `contractCode` | String | ❌ | "%" | Mã hợp đồng (filter) |
| `branch` | String | ❌ | "%" | Chi nhánh |
| `department` | String | ❌ | "%" | Phòng giao dịch |
| `nextKey` | String | ❌ | *(không gửi)* | Copy từ **`nextKey`** của response TradeX trước đó. Có giá trị hợp lệ → backend gọi Lotte `query_type = 1` + `next_key`. Không gửi → lần đầu (`query_type = 0`) |
| `fetchCount` | Number | ❌ | 100 | Số bản ghi mỗi lần gọi Lotte (`row_count`), khuyến nghị 100 để giảm số trang (max: 100) |

**Query mode (Lotte `query_type`) — backend suy luận (không bắt buộc client truyền số):**

| Điều kiện gọi TradeX | Lotte `query_type` | `total` từ Lotte | `data_list` |
|----------------------|--------------------|------------------|-------------|
| `nextKey` absent, empty, hoặc sentinel `"0"` | `0` | Có (nếu Lotte trả) | Trang đầu |
| `nextKey` có giá trị từ response trước | `1` | **Không** (Lotte không trả `total`) | Các dòng tiếp theo |

**Quy tắc Lotte (đã xác nhận tích hợp):** chỉ khi `query_type = 0` Lotte mới trả object **`total`**. Với `query_type = 1` + **`next_key`** Lotte chỉ trả **`data_list`** tiếp theo (không có `total`). TradeX vẫn trả đủ năm field **`total*`** cho FE trên mọi trang — backend tự lấy từ lần `query_type = 0` (cache) hoặc policy bổ sung (§5.2).

**FE:** không gửi `query_type`. Có `nextKey` trên query string → trang tiếp; không có (hoặc sentinel theo BE) → lần đầu.

### 3.2 Request Mapping

**TradeX → Lotte (backend builds POST body):**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `account_no` | Direct | Số tài khoản |
| `dateFrom` | String | ✅ | `start_date` | Direct | Từ ngày |
| `dateTo` | String | ✅ | `end_date` | Direct | Tới ngày |
| `productCode` | String | ✅ | `product_code` | Direct | Mã sản phẩm |
| `searchAllAccounts` | Boolean | ❌ | `search_type` | Direct (true/false) | Loại tra cứu |
| `contractCode` | String | ❌ | `contract_code` | Default: "%" | Mã hợp đồng |
| `branch` | String | ❌ | `branch` | Default: "%" | Chi nhánh |
| `department` | String | ❌ | `department` | Default: "%" | Phòng giao dịch |
| `nextKey` | String | ❌ | `next_key` | Lần đầu: không set hoặc `"0"` theo contract Lotte; lần sau: giá trị từ item/`total` response trước | Phân trang; kèm `query_type = 1` khi không phải lần đầu |
| `fetchCount` | Number | ❌ | `row_count` | Default: 100 (TradeX); map 1:1, max 100 | Số dòng Lotte trả về mỗi request — **nên dùng 100** khi cần lấy đủ bản ghi, hạn chế phân trang |
| *(Derived)* | - | - | `query_type` | `0` nếu không có `nextKey` hợp lệ; `1` nếu client gửi `nextKey` từ lần gọi trước | Không expose trực tiếp cho client nếu backend tự suy luận; hoặc optional `queryType` nếu product muốn explicit |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto (or from token) | hts_user_id tra cứu |
| *(Backend)* | - | - | `password` | From session / secure store | Mật khẩu mã hóa (không truyền từ client) |

**Note:** Lotte yêu cầu `password` (mã hóa). TradeX **không** nhận password từ client; backend lấy credential từ session/JWT hoặc secure store khi gọi Lotte.

**Default Logic:**
```typescript
const searchType = request.searchAllAccounts ?? false;
const contractCode = request.contractCode ?? '%';
const branch = request.branch ?? '%';
const department = request.department ?? '%';
const nextKey = request.nextKey?.trim();
const queryType = nextKey && nextKey !== '0' ? 1 : 0;

// Validate date range
const daysDiff = calculateDaysDiff(dateFrom, dateTo);
if (daysDiff > 90) {
  throw new ValidationError('DATE_RANGE_EXCEEDED');
}
```

### 3.3 Response Mapping

**Lotte success envelope (tham chiếu):** `error_code`, `error_desc`, `success`, `total_record`, `data_list`, và **`total`** (object) — **`total` chỉ có khi `query_type = 0`**.

**Success (200):**

**Item Object — mỗi phần tử `data_list[]` (P&L theo ngày + tài khoản + sản phẩm + hợp đồng):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `date` | `date` | String | Direct | Ngày (yyyyMMdd) |
| `account_no` | `accountNumber` | String | Direct | Số tài khoản phái sinh |
| `account_name` | `accountName` | String | Direct | Tên chủ tài khoản |
| `product_code` | `productCode` | String | Direct | Mã sản phẩm (VD: VN30F) |
| `contract_code` | `symbolCode` | String | Trim | Mã hợp đồng; Lotte có thể pad khoảng trắng — nên `trim` khi hiển thị/so khớp |
| `product_name` | `productName` | String | Direct | Tên sản phẩm (VD: HDTLVN30) |
| `realized_profit_loss` | `realizedProfitLoss` | Number | Parse float | Lãi lỗ đã thực hiện |
| `unrealized_profit_loss` | `unrealizedProfitLoss` | Number | Parse float | Lãi lỗ chưa thực hiện |
| `fee` | `fee` | Number | Parse float | Phí |
| `tax` | `tax` | Number | Parse float | Thuế |
| `net_profit_loss` | `netProfitLoss` | Number | Parse float | Net lãi lỗ |
| `next_key` | *(internal / phân trang)* | String | — | Dùng để suy ra root `nextKey` trang sau; **không** bắt buộc nhét vào từng phần tử `items[]` cho client |

**Tổng hợp — object Lotte `total` → các field root `total*` (TradeX):**

| Lotte Field (`total`) | TradeX Field (root) | Type | Transform | Description |
|------------------------|---------------------|------|-----------|-------------|
| `realized_profit_loss` | `totalRealizedProfitLoss` | Number | Parse float | Tổng LL đã thực hiện (phạm vi truy vấn Lotte) |
| `unrealized_profit_loss` | `totalUnrealizedProfitLoss` | Number | Parse float | Tổng LL chưa thực hiện |
| `fee` | `totalFee` | Number | Parse float | Tổng phí |
| `tax` | `totalTax` | Number | Parse float | Tổng thuế |
| `net_profit_loss` | `totalNetProfitLoss` | Number | Parse float | Tổng net LL |

**Trang phân trang (`query_type = 1`):** Lotte không trả `total`. TradeX vẫn trả **cùng năm field `total*`** như đã lấy ở lần gọi `query_type = 0` của **cùng một truy vấn** (backend cache / policy §5.2). **Không** suy ra từ `sum(items)`.

**Phân trang (root):**

| Nguồn Lotte | TradeX Field | Type | Description |
|-------------|--------------|------|-------------|
| `next_key` (ưu tiên: phần tử cuối `data_list`; fallback: `total.next_key` khi có ở lần đầu) | `nextKey` | String \| null | Gửi lại trên request cho trang tiếp; `null` khi hết (hoặc không còn cursor hợp lệ) |
| `total_record` (root) | *(optional)* `totalRecords` | Number \| null | Nếu product cần: parse số khi chuỗi khác rỗng; không bắt buộc trong contract tối thiểu |

**Response Structure — lần đầu (map từ Lotte có `total` + `data_list`):**
```json
{
  "totalRealizedProfitLoss": 78728000,
  "totalUnrealizedProfitLoss": 0,
  "totalFee": 65000,
  "totalTax": 150378,
  "totalNetProfitLoss": 78512622,
  "items": [
    {
      "date": "20251029",
      "accountNumber": "039C110257",
      "accountName": "Nguyễn Ngọc Hà",
      "productCode": "VN30F",
      "symbolCode": "41I1FB000",
      "productName": "HDTLVN30",
      "realizedProfitLoss": -1260000,
      "unrealizedProfitLoss": 0,
      "fee": 0,
      "tax": 0,
      "netProfitLoss": -1260000
    }
  ],
  "nextKey": "20251027039C110257VN30F     41I1FB000"
}
```

**Phân trang tiếp:** FE gửi cùng filter như lần đầu + `nextKey` từ response trước → backend Lotte `query_type = 1` + `next_key`. Lotte chỉ trả `data_list` (không `total`) — ví dụ raw:

**Lotte raw — trang tiếp (`next_key` + `query_type = 1`) — thực tế:**

```json
{
  "error_code": "0000",
  "error_desc": "",
  "success": true,
  "total_record": "",
  "data_list": [
    {
      "date": "20251024",
      "account_no": "039C110257",
      "account_name": "Nguyễn Ngọc Hà",
      "product_code": "VN30F",
      "contract_code": "41I1FB000",
      "product_name": "HDTLVN30",
      "realized_profit_loss": "-1470000",
      "unrealized_profit_loss": "0",
      "fee": "0",
      "net_profit_loss": "-1470000",
      "next_key": "20251022039C110257VN30F     41I1FB000",
      "tax": "0"
    },
    {
      "date": "20251023",
      "account_no": "039C110257",
      "account_name": "Nguyễn Ngọc Hà",
      "product_code": "VN30F",
      "contract_code": "41I1FB000",
      "product_name": "HDTLVN30",
      "realized_profit_loss": "7280000",
      "unrealized_profit_loss": "0",
      "fee": "0",
      "net_profit_loss": "7280000",
      "next_key": "20251022039C110257VN30F     41I1FB000",
      "tax": "0"
    },
    {
      "date": "20251022",
      "account_no": "039C110257",
      "account_name": "Nguyễn Ngọc Hà",
      "product_code": "VN30F",
      "contract_code": "41I1FB000",
      "product_name": "HDTLVN30",
      "realized_profit_loss": "1230000",
      "unrealized_profit_loss": "0",
      "fee": "5000",
      "net_profit_loss": "1212250",
      "next_key": "20251022039C110257VN30F     41I1FB000",
      "tax": "12750"
    }
  ]
}
```

- **Không có** key `total` trong JSON.  
- `next_key` trên các dòng (và token trang sau) lệch ngày so với trang `query_type = 0` (ví dụ từ `20251027…` → `20251022…`) — backend forward nguyên chuỗi cho lần gọi tiếp.  
- Dòng có phí/thuế: `net_profit_loss` có thể khác `realized_profit_loss` (ví dụ `1230000` − `5000` − `12750` = `1212250`).

**TradeX — cùng trang sau (`query_type = 1`):** `items` map từ `data_list` mới; **`total*`** giữ nguyên bản từ lần Lotte `query_type = 0` (ví dụ số tổng cùng trang đầu):

```json
{
  "totalRealizedProfitLoss": 78728000,
  "totalUnrealizedProfitLoss": 0,
  "totalFee": 65000,
  "totalTax": 150378,
  "totalNetProfitLoss": 78512622,
  "items": [
    {
      "date": "20251024",
      "accountNumber": "039C110257",
      "accountName": "Nguyễn Ngọc Hà",
      "productCode": "VN30F",
      "symbolCode": "41I1FB000",
      "productName": "HDTLVN30",
      "realizedProfitLoss": -1470000,
      "unrealizedProfitLoss": 0,
      "fee": 0,
      "tax": 0,
      "netProfitLoss": -1470000
    },
    {
      "date": "20251023",
      "accountNumber": "039C110257",
      "accountName": "Nguyễn Ngọc Hà",
      "productCode": "VN30F",
      "symbolCode": "41I1FB000",
      "productName": "HDTLVN30",
      "realizedProfitLoss": 7280000,
      "unrealizedProfitLoss": 0,
      "fee": 0,
      "tax": 0,
      "netProfitLoss": 7280000
    },
    {
      "date": "20251022",
      "accountNumber": "039C110257",
      "accountName": "Nguyễn Ngọc Hà",
      "productCode": "VN30F",
      "symbolCode": "41I1FB000",
      "productName": "HDTLVN30",
      "realizedProfitLoss": 1230000,
      "unrealizedProfitLoss": 0,
      "fee": 5000,
      "tax": 12750,
      "netProfitLoss": 1212250
    }
  ],
  "nextKey": "20251022039C110257VN30F     41I1FB000"
}
```

**Empty Result (`data_list` rỗng):**
```json
{
  "totalRealizedProfitLoss": 78728000,
  "totalUnrealizedProfitLoss": 0,
  "totalFee": 65000,
  "totalTax": 150378,
  "totalNetProfitLoss": 78512622,
  "items": [],
  "nextKey": null
}
```

*(Số `total*` trên trang rỗng vẫn là bản tổng từ lần `query_type = 0` nếu đang trong chuỗi phân trang; lần đầu không có `total` từ Lotte thì các `total*` = `0` hoặc `null` theo convention BE.)*

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` / `dateFrom` / `dateTo` / `productCode` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `dateFrom` / `dateTo` | `INVALID_DATE_FORMAT` | `["dateFrom"]` | Wrong format (not yyyyMMdd) |
| `dateFrom` / `dateTo` | `INVALID_DATE_RANGE` | `["dateFrom", "dateTo"]` | dateFrom > dateTo |
| `dateFrom` / `dateTo` | `DATE_RANGE_EXCEEDED` | `["90"]` | Range > 90 days |
| `fetchCount` | `INVALID_FETCH_COUNT` | `["fetchCount", "1", "100"]` | < 1 or > 100 |

**Auth Error (401):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `UNAUTHORIZED` | Token không hợp lệ hoặc đã hết hạn | Invalid token |
| `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Token expired |

**Auth Error (403):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `FORBIDDEN` | Không có quyền truy cập | No permission |
| `UNAUTHORIZED_ACCOUNT` | Tài khoản không thuộc quyền sở hữu của bạn | Account ownership check failed |

**Business Error (422) - Lotte Pass-Through:**

| Lotte Code | TradeX Code | Description |
|------------|-------------|-------------|
| `1005` | `DAILY_PNL_1005` | Lỗi hệ thống |
| `1006` | `DAILY_PNL_1006` | Tài khoản không tồn tại |

---

## 4. Error Handling Summary

### 4.1 Error Response Format

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "accountNumber", "messageParams": ["accountNumber"] }
  ]
}
```

**Auth Error (401/403):**
```json
{
  "code": "UNAUTHORIZED" / "TOKEN_EXPIRED" / "FORBIDDEN" / "UNAUTHORIZED_ACCOUNT",
  "message": "Error message"
}
```

**Business Error (422) - Lotte Pass-Through:**
```json
{
  "code": "DAILY_PNL_{LOTTE_CODE}",
  "message": "[V3120] Lotte error message"
}
```

**Server Error (500):**
```json
{
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

### 4.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business | `DAILY_PNL_{LOTTE_CODE}` | `DAILY_PNL_1005` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

### 4.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|------------------|
| `1005` | Lỗi hệ thống | System error |
| `1006` | Tài khoản không tồn tại | Account not found |
| `2001` | Không có dữ liệu | No data available |

---

## 5. Implementation Notes

### 5.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `lotte-bridge` | Lotte API integration, request/response mapping (POST body from GET query) |
| `asset-v2` | Business logic, validation (if needed) |
| **Kafka** | Service communication |

### 5.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, date format, date range (≤ 90 days), fetch count
- Lotte validates: Business rules (account exists, product code, data availability)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Root **`total*`** (từ Lotte `total` lần đầu; lặp lại khi phân trang) + `items` + root `nextKey`
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `DAILY_PNL_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `DAILY_PNL_1005`

**4. Auto-Population:**
- `userId` / `hts_user_id` → From JWT token or session
- `password` → From backend secure store/session (never from client)
- `lang_code` → From `Accept-Language` header

**5. Default Values:**
- `searchAllAccounts`: false
- `contractCode`, `branch`, `department`: "%"
- `nextKey`: lần đầu không gửi (hoặc sentinel theo Lotte); lần sau copy từ **`nextKey`** response TradeX trước đó
- `fetchCount`: Default **100**, max 100 — map sang Lotte `row_count`. **Khuyến nghị:** giữ `row_count` = 100 khi gọi Lotte để một lần lấy tối đa bản ghi, giảm số vòng phân trang (điều chỉnh nếu Lotte giới hạn khác).

**6. Pagination, `query_type` & tổng hợp cho FE:**
- **FE:** chỉ truyền `nextKey` khi lấy trang tiếp; không gửi `query_type`. Còn lại giữ nguyên filter (account, ngày, product, …) như lần đầu.
- **BE → Lotte:** không có `nextKey` hợp lệ → `query_type = 0` (có `total` + `data_list`). Có `nextKey` → `query_type = 1` + `next_key` (chỉ `data_list`).
- **BE → FE:** mọi response có đủ **`totalRealizedProfitLoss`**, **`totalUnrealizedProfitLoss`**, **`totalFee`**, **`totalTax`**, **`totalNetProfitLoss`** + `items` + root **`nextKey`**. Khi Lotte không trả `total` (trang 2+), các `total*` lấy từ **bản đã lưu** khi xử lý lần `query_type = 0` của cùng khóa truy vấn (user + filter), TTL ngắn (vd. 2–15 phút). **Cache miss:** có thể gọi thêm Lotte `query_type = 0` chỉ để lấy `total` (bỏ qua hoặc không trả `data_list` cho FE) — thống nhất với architecture team.
- **`nextKey` output:** từ phần tử cuối `data_list` hoặc `total.next_key` khi có; `null` khi hết trang.

**7. GET vs POST:**
- Client gọi GET với query parameters
- Backend (lotte-bridge) chuyển sang POST body khi gọi Lotte DRACC-037

### 5.3 Data Transformation

**Item mapping (Lotte `data_list[]` → TradeX `items[]`):**
```typescript
const mapItem = (lotteItem) => ({
  date: lotteItem.date,
  accountNumber: lotteItem.account_no,
  accountName: lotteItem.account_name,
  productCode: lotteItem.product_code,
  symbolCode: String(lotteItem.contract_code ?? '').trim(),
  productName: lotteItem.product_name,
  realizedProfitLoss: parseFloat(lotteItem.realized_profit_loss) || 0,
  unrealizedProfitLoss: parseFloat(lotteItem.unrealized_profit_loss) || 0,
  fee: parseFloat(lotteItem.fee) || 0,
  tax: parseFloat(lotteItem.tax) || 0,
  netProfitLoss: parseFloat(lotteItem.net_profit_loss) || 0,
});

const rawList = lotteResponse.data_list ?? [];
const items = rawList.map(mapItem);

const mapTotalsFromLotteTotal = (t) =>
  t
    ? {
        totalRealizedProfitLoss: parseFloat(t.realized_profit_loss) || 0,
        totalUnrealizedProfitLoss: parseFloat(t.unrealized_profit_loss) || 0,
        totalFee: parseFloat(t.fee) || 0,
        totalTax: parseFloat(t.tax) || 0,
        totalNetProfitLoss: parseFloat(t.net_profit_loss) || 0,
      }
    : null;

// Lần query_type = 0: lưu aggregate vào cache(keyQuery) + trả cho FE
// Lần query_type = 1: đọc aggregate từ cache(keyQuery); không dùng sum(items)
const aggregates =
  mapTotalsFromLotteTotal(lotteResponse.total) ?? loadTotalsFromCache(keyQuery);

const lastRaw = rawList[rawList.length - 1];
const nextKeyOut =
  lastRaw?.next_key ?? lotteResponse.total?.next_key ?? null;
const nextKeyForClient =
  nextKeyOut != null && String(nextKeyOut).trim() !== '' && String(nextKeyOut).trim() !== '0'
    ? String(nextKeyOut).trim()
    : null;

// Response body: { ...aggregates, items, nextKey: nextKeyForClient }
```

**Lotte request — `row_count`:**
```typescript
// TradeX GET query fetchCount → Lotte POST body row_count (default 100, max 100)
const row_count = Math.min(Math.max(request.fetchCount ?? 100, 1), 100);
```

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team; chốt **TTL + khóa cache** cho `total*` khi phân trang; hành vi **cache miss** (gọi lại `query_type = 0` hay trả lỗi). Confirm Lotte: khoảng ngày, `row_count` max, `total_record`. Response: root `total*` + `items` + `nextKey`; map đầy đủ `data_list` §3.3; không `reduce` trên `items` cho tổng toàn phạm vi.
