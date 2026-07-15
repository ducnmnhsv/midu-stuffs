# Top Stock Influence API Specification

**Document Type:** API Specification  
**Category:** Market Watch — Nhóm dẫn dắt thị trường  
**Version:** 1.3  
**Date:** July 15, 2026

> **Note:** TradeX-native aggregation API. BE tự tính từ dữ liệu nội bộ TradeX (indexStockList + symbolInfo), **không gọi Vietstock**, không qua Lotte/Core. BE tính lại theo interval **2 phút/lần**, cache kết quả; API trả từ cache.

> **Changelog v1.2:** Đổi data source từ Vietstock External API → TradeX tự tính (công thức §2.4). **I/O request/response giữ nguyên so với v1.1** — xem lưu ý về `tradingDate` tại §3.1.

> **Changelog v1.3:** Đổi logic `influenceType=ALL` — từ "top N theo `|điểm|` tuyệt đối, gộp cả 2 chiều" sang "top N **mỗi chiều**" (trả cặp DECREASE + INCREASE). Lý do: `top` nhỏ hoặc thị trường lệch mạnh 1 phía làm mất hẳn 1 chiều khỏi response (VD `top=20` thực tế chỉ ra 1 mã INCREASE). **Breaking change về cardinality** — số bản ghi trả về khi `ALL` có thể lên tới `2 × top` thay vì đúng bằng `top`. Xem thảo luận tại NHMTS-1017.

---

## 1. Overview

### 1.1 Purpose

Top Stock Influence API trả về danh sách cổ phiếu có mức đóng góp điểm (ảnh hưởng) lớn nhất tới một chỉ số/sàn trong phiên giao dịch. FE dùng để render bar chart "Nhóm dẫn dắt thị trường" trên màn hình Market Watch.

**Business context:**
- Cung cấp insight nhanh về "ai đang dẫn dắt thị trường hôm nay".
- Hỗ trợ retail investor nhận biết mã kéo index tăng / giảm mạnh nhất.
- Module thường xuyên truy cập trong Market Watch của NHSV Pro.

**Data source (nội bộ TradeX):**

| Nguồn | API tương đương | Dữ liệu lấy |
|-------|-----------------|-------------|
| Index stock list (MongoDB `c_index_stock_list`) | `GET /api/v2/market/indexStockList/{indexCode}` | Danh sách mã thành phần của chỉ số |
| Symbol info (Redis snapshot) | `GET /api/v2/market/symbolInfo?symbolList=...` | `mc` (vốn hóa), `ra` (% thay đổi), `ch`, `c`, `lq` của từng mã |
| Index value | `symbolInfo` của chính mã index (VNINDEX...) | `c` = giá trị chỉ số hiện tại (`indexValue`) |

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Top Stock Influence | GET | `/api/v1/marketWatch/topStockInfluence` |

### 1.3 Response Format Standards

**Success (200):**
```json
[
  {
    "stockCode": "VCB",
    "closePrice": 92500,
    "change": 1500,
    "perChange": 1.65,
    "influenceIndex": 1.25,
    "influencePercent": 0.48,
    "direction": "INCREASE",
    "marketCap": 450000000000000,
    "weight": 12.5,
    "baseIndex": 1265.3,
    "sharesOutstanding": 4700000000,
    "row": 1
  }
]
```

**Error:**
```json
{
  "code": "ERROR_CODE",
  "message": "Error message"
}
```

hoặc validation error:
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "exchange", "messageParams": ["exchange"] }
  ]
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Response: array trực tiếp (không wrap envelope)
- Response fields **giữ nguyên như v1.1** — FE không cần thay đổi model

---

## 2. Business Rules

### 2.1 Exchange → IndexCode Enum

`exchange` (request) map sang `indexCode` dùng để gọi index stock list + lấy index value:

| TradeX `exchange` | `indexCode` nội bộ | Mô tả |
|-------------------|--------------------|-------|
| `HOSE` | `VNINDEX` | Sàn TP.HCM (VN-Index) |
| `HNX` | `HNXINDEX` | Sàn Hà Nội (HNX-Index) |
| `UPCOM` | `UPCOMINDEX` | Sàn UPCoM |
| `VN30` | `VN30` | Rổ VN30 |
| `HNX30` | `HNX30` | Rổ HNX30 |

### 2.2 InfluenceType Enum

BE filter trên kết quả đã tính, không còn map sang tham số upstream. **Lưu ý (v1.3):** với `ALL`, `top` áp dụng **theo từng chiều** — không còn "top N gộp theo `|điểm|` tuyệt đối" như v1.2 — để đảm bảo bar chart luôn thấy được cả 2 chiều ảnh hưởng kể cả khi `top` nhỏ hoặc thị trường lệch mạnh 1 phía.

| TradeX `influenceType` | BE filter | Số bản ghi trả về | Mô tả |
|------------------------|-----------|--------------------|-------|
| `ALL` | Nhóm `DECREASE` (`influenceIndex < 0`, sort tăng dần — âm nhất trước) lấy `top` mã đầu, **nối tiếp** nhóm `INCREASE` (`influenceIndex >= 0`, sort giảm dần — dương lớn nhất trước) lấy `top` mã đầu | Tối đa `2 × top` — ít hơn nếu 1 chiều không đủ mã, có thể chỉ còn 1 chiều nếu chiều kia rỗng | Cả tăng và giảm, đảm bảo mỗi chiều tối đa `top` mã (default) |
| `INCREASE` | `influenceIndex >= 0` — sort giảm dần | Tối đa `top` | Chỉ mã đóng góp dương |
| `DECREASE` | `influenceIndex < 0` — sort tăng dần (âm nhất trước) | Tối đa `top` | Chỉ mã đóng góp âm |

> **Ví dụ:** `top=1`, `influenceType=ALL` → trả tối đa 2 bản ghi: 1 mã `DECREASE` ảnh hưởng âm nhất + 1 mã `INCREASE` ảnh hưởng dương nhất. Nếu thị trường giảm toàn diện (không có mã nào `INCREASE`) → chỉ trả 1 bản ghi `DECREASE`, không lỗi.

### 2.3 Direction Enum (Response)

Derived từ dấu của `influenceIndex`:

| Điều kiện | TradeX `direction` | Bar color |
|-----------|--------------------|-----------|
| `influenceIndex >= 0` | `INCREASE` | Xanh (UP color) |
| `influenceIndex < 0` | `DECREASE` | Đỏ (DOWN color) |

### 2.4 Calculation Formula (Core Logic)

Với mỗi chỉ số (`indexCode`), chạy theo interval **2 phút/lần**:

**Bước 1 — Lấy danh sách mã:** đọc `stockList` của `indexCode` từ index stock list.

**Bước 2 — Lấy symbol info:** lấy `mc`, `ra`, `ch`, `c`, `lq` của toàn bộ mã trong list (bulk symbolInfo). Đồng thời lấy `indexValue` = giá trị hiện tại (`c`) của chính mã index.

**Bước 3 — Tính toán:**

```
sumMc       = Σ mc                       // tổng vốn hóa toàn bộ mã trong list
weight(mã)  = (mc / sumMc) × 100         // % trọng số vốn hóa trong chỉ số
value(mã)   = (mc / sumMc) × ra          // % đóng góp vào biến động chỉ số
điểm(mã)    = (value / 100) × indexValue // quy ra điểm index
```

Trong đó `ra` = % thay đổi giá của mã, `indexValue` = giá trị chỉ số tại thời điểm tính.

**Bước 4 — Sort & cache:** cache lưu **2 danh sách con theo chiều**, đầy đủ không cắt `top`:
- `DECREASE`: các mã có điểm âm, sort tăng dần (âm nhất trước)
- `INCREASE`: các mã có điểm dương/0, sort giảm dần (dương lớn nhất trước)

API đọc cache theo `influenceType`:
- `ALL`: lấy `top` mã đầu của `DECREASE`, nối tiếp `top` mã đầu của `INCREASE`; đánh lại `row` từ 1 theo thứ tự nối (DECREASE trước, INCREASE sau).
- `INCREASE` / `DECREASE`: lấy `top` mã đầu của danh sách con tương ứng, đánh `row` từ 1.

**Quy tắc loại trừ khi tính:**
- Mã có `mc` null hoặc `mc = 0` → loại khỏi danh sách và **không** cộng vào `sumMc`.
- Mã có `ra` null (chưa có khớp lệnh) → `value = 0`, giữ trong danh sách nhưng thực tế sẽ không lọt top.
- `indexValue` null → không tính được đợt này, giữ nguyên cache của lần tính trước.

> **Ghi chú (approximation):** Σ điểm của tất cả mã ≈ mức thay đổi của chỉ số, không bằng tuyệt đối — vì công thức dùng `mc` và `indexValue` tại thời điểm hiện tại thay vì free-float weight chính thức của sở. Chấp nhận được cho mục đích hiển thị "nhóm dẫn dắt".

### 2.5 Validation Rules

| Rule | Field | Error Code | Condition |
|------|-------|------------|-----------|
| Required | `exchange` | `FIELD_IS_REQUIRED` | Missing |
| Required | `tradingDate` | `FIELD_IS_REQUIRED` | Missing |
| Enum check | `exchange` | `INVALID_VALUE` | Không thuộc HOSE/HNX/UPCOM/VN30/HNX30 |
| Enum check | `influenceType` | `INVALID_VALUE` | Không thuộc ALL/INCREASE/DECREASE |

### 2.6 Default Values

| Parameter | Default | Description |
|-----------|---------|-------------|
| `tradingDate` | Current trading date (yyyy-MM-dd) | Chỉ validate format — xem lưu ý §3.1 |
| `top` | `20` | Số mã tối đa trả về **mỗi chiều** khi `ALL` (tổng tối đa `2×top`); số mã tối đa trả về khi `INCREASE`/`DECREASE` |
| `influenceType` | `ALL` | Lấy cả tăng và giảm (trả cặp — xem §2.2) |

---

## 3. API: Get Top Stock Influence

### 3.1 Request

**Endpoint:** `GET /api/v1/marketWatch/topStockInfluence`

**Auth:** JWT token (không cần `accountNumber` / `deviceUniqueId`)

**Default call:**
```
GET /api/v1/marketWatch/topStockInfluence?exchange=HOSE&tradingDate={today}&top=20&influenceType=ALL
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `exchange` | String | ✅ | - | Sàn / rổ chỉ số — bind với dropdown UI |
| `tradingDate` | String | ✅ | - | Ngày giao dịch (yyyy-MM-dd) — xem lưu ý bên dưới |
| `top` | Number | ❌ | `20` | Số lượng mã trả về **mỗi chiều** khi `ALL` (tổng tối đa `2×top` bản ghi); số lượng mã trả về khi `INCREASE`/`DECREASE` |
| `influenceType` | String | ❌ | `ALL` | Loại ảnh hưởng: `ALL`, `INCREASE`, `DECREASE` |

> ⚠️ **Lưu ý `tradingDate` (thay đổi behavior so với v1.1):** Param giữ nguyên để FE không phải đổi contract, nhưng BE **không còn hỗ trợ query lịch sử**. Dữ liệu luôn là **snapshot gần nhất** do BE tính real-time mỗi 2 phút (ngoài giờ giao dịch = snapshot cuối phiên gần nhất). BE chỉ validate format của `tradingDate`, không dùng để lookup.

### 3.2 Data Sources & Internal Mapping

Thay cho request mapping sang Vietstock (v1.1), BE tự tổng hợp từ nguồn nội bộ:

| Bước | Nguồn nội bộ | Input | Output |
|------|--------------|-------|--------|
| 1 | Index stock list | `indexCode` (map từ `exchange`, §2.1) | `stockList: string[]` |
| 2 | SymbolInfo (bulk) | `symbolList = stockList` | `s`, `c`, `ch`, `ra`, `mc`, `lq` mỗi mã |
| 3 | SymbolInfo (index) | `symbolList = indexCode` | `c` = `indexValue` |
| 4 | Tính toán (§2.4) | Kết quả bước 2 + 3 | Danh sách đã sort, cached |

### 3.3 Response Field Derivation

Response fields **không đổi so với v1.1** — chỉ đổi nguồn dữ liệu:

| TradeX Field | Type | Nguồn mới | UI Usage |
|--------------|------|-----------|----------|
| `stockCode` | String | symbolInfo `s` | X-axis label, tooltip header |
| `closePrice` | Number | symbolInfo `c` (giá khớp gần nhất) | Tooltip — giá hiện tại |
| `change` | Number | symbolInfo `ch` | Tooltip — Δ giá tuyệt đối |
| `perChange` | Number (%) | symbolInfo `ra` | Tooltip — % thay đổi giá |
| `sharesOutstanding` | Number | symbolInfo `lq` (listedQuantity) | Tooltip (optional) |
| `marketCap` | Number | symbolInfo `mc` | Tooltip — vốn hóa (VND) |
| `weight` | Number (%) | **Computed:** `(mc / sumMc) × 100` | Tooltip — trọng số trong chỉ số |
| `baseIndex` | Number | `indexValue` — giá trị chỉ số dùng trong công thức | Tính toán nội bộ |
| `influencePercent` | Number (%) | **Computed:** `value = (mc / sumMc) × ra` | Tooltip phụ |
| `influenceIndex` | Number | **Computed:** `điểm = (value / 100) × indexValue` | **Y-axis value (bar height)** |
| `direction` | String | **Derived:** dấu của `influenceIndex` (§2.3) | **Bar color rule** |
| `row` | Number | **Computed:** thứ tự sau khi nối 2 nhóm DECREASE→INCREASE (§2.2, §2.4) | Sort key ascending |

> **Semantic note `baseIndex`:** v1.1 map từ Vietstock `BasicIndex`; v1.2 = giá trị chỉ số hiện tại tại thời điểm BE tính. Field name và type giữ nguyên; FE hiện chỉ dùng nội bộ nên không ảnh hưởng.

### 3.4 Error Mapping

**Validation Error (400) — TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `exchange` | `FIELD_IS_REQUIRED` | `["exchange"]` | Missing |
| `exchange` | `INVALID_VALUE` | `["exchange"]` | Không hợp lệ |
| `tradingDate` | `FIELD_IS_REQUIRED` | `["tradingDate"]` | Missing |
| `influenceType` | `INVALID_VALUE` | `["influenceType"]` | Không hợp lệ |

**Auth Error (401/403):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `UNAUTHORIZED` | Token không hợp lệ hoặc đã hết hạn | Invalid JWT |
| `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Token expired |

**Server Error (500):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `INTERNAL_SERVER_ERROR` | Lỗi hệ thống, vui lòng thử lại sau | Cache rỗng + không đọc được index stock list / symbolInfo |

---

## 4. Error Handling Summary

### 4.1 Error Response Format

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "exchange", "messageParams": ["exchange"] }
  ]
}
```

**Auth Error (401/403):**
```json
{
  "code": "UNAUTHORIZED",
  "message": "Token không hợp lệ hoặc đã hết hạn"
}
```

**Server Error (500):**
```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

**Empty data (200 — chưa có snapshot, ví dụ trước ATO ngày giao dịch đầu tiên):**
```json
[]
```
→ FE xử lý empty state: "Chưa có dữ liệu cho ngày này."

### 4.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| TradeX Internal | `INTERNAL_SERVER_ERROR` | Data source fail + cache rỗng | 500 |

---

## 5. UI/UX Behavior

### 5.1 Layout

- Section nằm trong màn hình **Market Watch**, dưới header tổng quan index.
- Header section: Title "Nhóm dẫn dắt thị trường" + **Dropdown sàn** (right-aligned).
- Dropdown default: `HOSE`. Các tuỳ chọn: HOSE, HNX, UPCOM, VN30, HNX30.
- Body: **Bar chart vertical** chiếm toàn bộ chiều ngang của section.

### 5.2 Bar Chart

- **X-axis:** `stockCode`, sort theo `row` ascending (BE đã sort).
- **Y-axis:** `influenceIndex`. Hỗ trợ giá trị âm — bar đi xuống từ baseline 0.
- **Bar color:** `INCREASE` → UP color (xanh) · `DECREASE` → DOWN color (đỏ).
- **Default:** tối đa 40 bar (20 `DECREASE` + 20 `INCREASE`), sàn HOSE, `influenceType = ALL`. Số bar thực tế có thể ít hơn nếu 1 chiều không đủ mã.

### 5.3 Interactions

| Action | Behavior |
|--------|----------|
| Chọn sàn từ dropdown | Gọi lại API với `exchange` mới; skeleton loading trong khi chờ |
| Tap vào bar | Hiển thị tooltip / bottom sheet với chi tiết mã |
| Pull-to-refresh | Refetch với param hiện tại |
| Tap label mã (X-axis) | (Optional v2) Điều hướng vào chi tiết cổ phiếu |

### 5.4 Tooltip Detail khi tap bar

```
VCB
Đóng góp: +1.25 điểm  (+0.48%)
Giá đóng cửa: 92,500  ·  +1,500  (+1.65%)
Vốn hóa: 450,000 tỷ
Trọng số trong index: 12.5%
```

### 5.5 Data → UI Mapping

| UI Element | TradeX Field | Format |
|------------|--------------|--------|
| Dropdown sàn | `exchange` (request) | String: `HOSE`, `HNX`... |
| Bar X-axis label | `stockCode` | Plain string |
| Bar Y-axis value | `influenceIndex` | Số thực, hỗ trợ âm |
| Bar color | `direction` | `INCREASE` → green · `DECREASE` → red |
| Bar order (left → right) | `row` | Ascending |
| Tooltip — giá đóng cửa | `closePrice` | Format `vi-VN` thousand separator |
| Tooltip — Δ giá | `change` + `perChange` | `+/-value (+/-x.xx%)` |
| Tooltip — vốn hóa | `marketCap` | Format `tỷ` / `nghìn tỷ` |
| Tooltip — trọng số | `weight` | `x.xx%` |
| Tooltip — đóng góp | `influenceIndex` + `influencePercent` | `+/-x.xx điểm (+/-x.xx%)` |

---

## 6. Edge Cases

| Case | Behavior |
|------|----------|
| **Empty array** (chưa có snapshot cache) | Empty state: "Chưa có dữ liệu cho ngày này." |
| **Market closed / weekend / holiday** | BE giữ snapshot cuối phiên gần nhất; banner: "Dữ liệu phiên gần nhất: dd/MM/yyyy" |
| **Single bar** | Render đầy đủ chart, không vỡ layout |
| **`influenceIndex` âm** (influenceType = ALL) | Y-axis hỗ trợ giá trị âm, baseline 0 |
| **API timeout / 5xx** | Error state + retry button; giữ snapshot data cũ nếu có |
| **Slow network** | Skeleton loading; tránh layout shift khi polling |
| **`influenceType = INCREASE`, thị trường giảm toàn diện** | Trả ít hơn `top` records hoặc rỗng → empty state |
| **`influenceType = ALL`, thị trường lệch mạnh 1 phía** (VD toàn bộ giảm) | Chiều còn lại (`INCREASE`) trả rỗng — response chỉ còn danh sách `DECREASE`; không lỗi, không cần đủ `2×top` |
| **`influenceType = ALL`, `top` lớn hơn số mã có sẵn ở 1 chiều** | Chiều đó trả toàn bộ số mã có (ít hơn `top`), không lỗi, không pad thêm |
| **`tradingDate` là ngày tương lai / quá khứ** | BE bỏ qua giá trị (chỉ validate format) — data luôn là snapshot gần nhất. FE disable date picker |
| **Đổi `exchange`** | Invalidate cache theo `exchange`; reset chart state |
| **Mã có `mc` null / 0** (mới niêm yết, thiếu static data) | BE loại khỏi tính toán, không cộng vào `sumMc` |
| **Mã có `ra` null** (chưa có khớp lệnh) | `value = 0` → không lọt top; không crash |
| **`indexValue` null** (index chưa có quote) | BE giữ cache lần tính trước; không ghi đè bằng data lỗi |
| **`perChange` / `closePrice` null** | FE null-safe: hiển thị `—` thay vì crash |

---

## 7. Implementation Notes

### 7.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `market-query-v2` | Scheduled aggregation job (2 phút/lần) + serve API từ cache |
| MongoDB `c_index_stock_list` | Nguồn danh sách mã thành phần index (do lotte-bridge sync job 1h sáng → realtime-v2 ghi) |
| Redis symbolInfo snapshot | Nguồn `mc`, `ra`, `ch`, `c`, `lq` + index value |

**Flow:**
```
[Scheduler 2 phút] market-query-v2
    → đọc stockList theo indexCode (Mongo)
    → đọc symbolInfo bulk + index value (Redis)
    → tính sumMc / weight / value / điểm (§2.4)
    → sort + cache full list theo indexCode

[Request] Client → rest-proxy → market-query-v2
    → đọc cache theo indexCode
    → filter influenceType + slice top
    → response
```

### 7.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, enum values
- Không còn upstream validation (Vietstock đã bỏ)

**2. Computation Strategy:**
- BE tính **theo schedule 2 phút/lần**, KHÔNG tính on-request (tránh N×symbolInfo mỗi request)
- Cache full list per `indexCode`; `top` + `influenceType` apply lúc serve request
- FE chịu trách nhiệm format display (thousand separator, đơn vị %, tỷ)

**3. Polling Strategy:**
- Data chỉ đổi mỗi 2 phút → FE polling **mỗi 2 phút** trong giờ giao dịch (09:00–15:00 VN), thay cho 15–30s ở v1.1
- Ngoài giờ: Load 1 lần (snapshot cuối phiên), không polling
- FE tự quản lý polling lifecycle theo trading session state

**4. Auto-Population:**
- `tradingDate` default = current trading date nếu không truyền (BE chỉ validate format, không dùng lookup)

**5. Caching:**
- BE cache key: `indexCode` (TTL > 2 phút, ghi đè mỗi lần job chạy thành công)
- FE cache key: `(exchange, top, influenceType)`; invalidate khi đổi `exchange` hoặc pull-to-refresh

### 7.3 I/O Compatibility (v1.1 → v1.2)

| Mục | Kết luận |
|-----|----------|
| Request params (tên, type, required) | ✅ Giữ nguyên 100% |
| Response fields (tên, type, cấu trúc array) | ✅ Giữ nguyên 100% — FE model không đổi |
| `tradingDate` behavior | ⚠️ Mất khả năng query lịch sử — luôn trả snapshot gần nhất |
| `baseIndex` semantic | ⚠️ Đổi từ Vietstock `BasicIndex` → `indexValue` hiện tại (FE không dùng hiển thị → không ảnh hưởng) |
| Polling khuyến nghị | ⚠️ 15–30s → 2 phút (khớp interval BE) |

### 7.3b I/O Compatibility (v1.2 → v1.3)

| Mục | Kết luận |
|-----|----------|
| Request params (tên, type, required) | ✅ Giữ nguyên — chỉ đổi ý nghĩa `top` khi `influenceType=ALL` (§2.6, §3.1) |
| Response fields (tên, type) | ✅ Giữ nguyên 100% — không field nào đổi tên/type |
| **Số bản ghi trả về khi `ALL`** | 🔴 **Breaking:** trước đúng bằng `top`, giờ tối đa `2×top` (cặp DECREASE + INCREASE) — FE cần xử lý mảng dài hơn dự kiến, không giả định `length === top` |
| Số bản ghi khi `INCREASE`/`DECREASE` | ✅ Không đổi — vẫn tối đa `top` |
| `row` ordering khi `ALL` | ⚠️ Đổi từ "sort theo `|điểm|` gộp" → "nhóm DECREASE trước, INCREASE sau" — FE render bar chart theo `row` mới, thứ tự trái→phải sẽ khác v1.2 |

### 7.4 Acceptance Criteria

- [ ] Tab Market Watch hiển thị section "Nhóm dẫn dắt thị trường" với bar chart + dropdown sàn.
- [ ] Dropdown default `HOSE`, đổi sàn → gọi lại API + skeleton loading.
- [ ] BE job tính đúng công thức §2.4, chạy interval 2 phút, cache theo `indexCode`.
- [ ] Mã `mc` null/0 bị loại khỏi `sumMc`; `ra` null → value = 0; `indexValue` null → giữ cache cũ.
- [ ] `influenceType` filter đúng: ALL / INCREASE / DECREASE (§2.2).
- [ ] `influenceType=ALL` trả **cặp** — tối đa `top` mã DECREASE + tối đa `top` mã INCREASE (tổng tối đa `2×top`); `row` đánh theo thứ tự DECREASE trước, INCREASE sau (§2.2, §2.4).
- [ ] `influenceType=ALL` khi 1 chiều rỗng (thị trường lệch mạnh 1 phía) → chỉ trả chiều còn lại, không lỗi (§6).
- [ ] Bar chart render đúng `influenceIndex` trên Y-axis, hỗ trợ giá trị âm.
- [ ] Bar color: `INCREASE` → xanh, `DECREASE` → đỏ (theo NHSV design system).
- [ ] Bar order: sort theo `row` ascending (trái → phải).
- [ ] Tap bar → tooltip hiện đủ: `stockCode`, `influenceIndex`, `influencePercent`, `closePrice`, `change`, `perChange`, `marketCap`, `weight`.
- [ ] Loading state: skeleton placeholder trong card.
- [ ] Empty state: message "Chưa có dữ liệu cho ngày này." khi array rỗng.
- [ ] Error state: message + retry button khi API fail; không crash Market Watch.
- [ ] Pull-to-refresh: refetch với param hiện tại.
- [ ] FE polling 2 phút trong giờ giao dịch; ngoài giờ không polling.
- [ ] Null-safe: `closePrice`, `perChange` null → hiển thị `—`.

---

**Document Status:** 📋 Draft | **For:** FE Dev, BE Dev, QA | **Next Steps:** Review với tech lead trước khi implement
