# Top Stock Influence API Specification

**Document Type:** API Specification  
**Category:** Market Watch — Nhóm dẫn dắt thị trường  
**Version:** 1.1  
**Date:** July 1, 2026

> **Note:** TradeX-native proxy API (Vietstock External). Không qua Lotte/Core. Data type: EOD + polling 15–30s trong giờ giao dịch.

---

## 1. Overview

### 1.1 Purpose

Top Stock Influence API trả về danh sách cổ phiếu có mức đóng góp điểm (ảnh hưởng) lớn nhất tới một chỉ số/sàn trong phiên giao dịch. FE dùng để render bar chart "Nhóm dẫn dắt thị trường" trên màn hình Market Watch.

**Business context:**
- Cung cấp insight nhanh về "ai đang dẫn dắt thị trường hôm nay".
- Hỗ trợ retail investor nhận biết mã kéo index tăng / giảm mạnh nhất.
- Module thường xuyên truy cập trong Market Watch của NHSV Pro.

**Data source:** Vietstock External API — proxied qua TradeX (`rest-proxy` → `market-query-v2` → Vietstock).

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
- Vietstock error message pass-through AS-IS

---

## 2. Business Rules

### 2.1 Exchange / Index Enum

| TradeX `exchange` | Vietstock `CatID` | Mô tả |
|-------------------|-------------------|-------|
| `HOSE` | `1` | Sàn TP.HCM (VN-Index) |
| `HNX` | `2` | Sàn Hà Nội (HNX-Index) |
| `UPCOM` | `3` | Sàn UPCoM |
| `VN30` | `4` | Rổ VN30 |
| `HNX30` | `5` | Rổ HNX30 |

### 2.2 InfluenceType Enum

| TradeX `influenceType` | Vietstock `Type` | Mô tả |
|------------------------|-----------------|-------|
| `ALL` | `0` | Cả tăng và giảm (default) |
| `INCREASE` | `1` | Chỉ mã đóng góp dương |
| `DECREASE` | `2` | Chỉ mã đóng góp âm |

### 2.3 Direction Enum (Response)

| Vietstock `OrderType` | TradeX `direction` | Bar color |
|-----------------------|-------------------|-----------|
| `1` | `INCREASE` | Xanh (UP color) |
| `2` | `DECREASE` | Đỏ (DOWN color) |

### 2.4 Validation Rules

| Rule | Field | Error Code | Condition |
|------|-------|------------|-----------|
| Required | `exchange` | `FIELD_IS_REQUIRED` | Missing |
| Required | `tradingDate` | `FIELD_IS_REQUIRED` | Missing |
| Enum check | `exchange` | `INVALID_VALUE` | Không thuộc HOSE/HNX/UPCOM/VN30/HNX30 |
| Enum check | `influenceType` | `INVALID_VALUE` | Không thuộc ALL/INCREASE/DECREASE |

### 2.5 Default Values

| Parameter | Default | Description |
|-----------|---------|-------------|
| `tradingDate` | Current trading date (yyyy-MM-dd) | FE nên truyền explicit |
| `top` | `20` | Số mã tối đa trả về |
| `influenceType` | `ALL` | Lấy cả tăng và giảm |

---

## 3. API: Get Top Stock Influence

### 3.1 Request

**Endpoint:** `GET /api/v1/marketWatch/topStockInfluence`

**Upstream Vietstock:** `GET https://api-demo.vietstock.vn/demo/topstockinfluence`

**Auth:** JWT token (không cần `accountNumber` / `deviceUniqueId`)

**Default call:**
```
GET /api/v1/marketWatch/topStockInfluence?exchange=HOSE&tradingDate={today}&top=20&influenceType=ALL
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `exchange` | String | ✅ | - | Sàn / rổ chỉ số — bind với dropdown UI |
| `tradingDate` | String | ✅ | - | Ngày giao dịch (yyyy-MM-dd) |
| `top` | Number | ❌ | `20` | Số lượng mã trả về |
| `influenceType` | String | ❌ | `ALL` | Loại ảnh hưởng: `ALL`, `INCREASE`, `DECREASE` |

### 3.2 Request Mapping (TradeX → Vietstock)

| TradeX Field | Type | Required | Vietstock Field | Transform | Description |
|--------------|------|----------|-----------------|-----------|-------------|
| `exchange` | String | ✅ | `CatID` | Enum map (§2.1) | Sàn / rổ chỉ số |
| `tradingDate` | String | ✅ | `TradeDate` | Direct | yyyy-MM-dd |
| `top` | Number | ❌ | `Top` | Direct | Default 20 |
| `influenceType` | String | ❌ | `Type` | Enum map (§2.2) | Default `ALL` → `0` |

### 3.3 Response Mapping (Vietstock → TradeX)

| Vietstock Field | TradeX Field | Type | Transform | UI Usage |
|-----------------|--------------|------|-----------|----------|
| `StockCode` | `stockCode` | String | camelCase | X-axis label, tooltip header |
| `ClosePrice` | `closePrice` | Number | camelCase | Tooltip — giá đóng cửa |
| `Change` | `change` | Number | camelCase | Tooltip — Δ giá tuyệt đối |
| `PerChange` | `perChange` | Number (%) | camelCase | Tooltip — % thay đổi giá |
| `KLCPLH` | `sharesOutstanding` | Number | Rename | Tooltip (optional) |
| `MarketCap` | `marketCap` | Number | camelCase | Tooltip — vốn hóa (VND) |
| `Weight` | `weight` | Number (%) | camelCase | Tooltip — trọng số trong chỉ số |
| `BasicIndex` | `baseIndex` | Number | Rename | Tính toán nội bộ |
| `InfluencePercent` | `influencePercent` | Number (%) | camelCase | Tooltip phụ |
| `InfluenceIndex` | `influenceIndex` | Number | camelCase | **Y-axis value (bar height)** |
| `OrderType` | `direction` | String | Enum map (§2.3) | **Bar color rule** |
| `Row` | `row` | Number | camelCase | Sort key ascending |

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

**Upstream Error (500) — Vietstock timeout/5xx:**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `INTERNAL_SERVER_ERROR` | Lỗi hệ thống, vui lòng thử lại sau | Vietstock không phản hồi |

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

**Empty data (200 — Vietstock trả mảng rỗng):**
```json
[]
```
→ FE xử lý empty state: "Chưa có dữ liệu cho ngày này."

### 4.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Vietstock upstream | `INTERNAL_SERVER_ERROR` | Vietstock 5xx/timeout | 500 |

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
- **Default:** 20 bar, sàn HOSE, `influenceType = ALL`.

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
| **Empty array** (Vietstock trả `[]`) | Empty state: "Chưa có dữ liệu cho ngày này." |
| **Market closed / weekend / holiday** | Banner: "Dữ liệu phiên gần nhất: dd/MM/yyyy"; load data phiên gần nhất |
| **Single bar** | Render đầy đủ chart, không vỡ layout |
| **`influenceIndex` âm** (influenceType = ALL) | Y-axis hỗ trợ giá trị âm, baseline 0 |
| **API timeout / 5xx** | Error state + retry button; giữ snapshot data cũ nếu có |
| **Slow network** | Skeleton loading; tránh layout shift khi polling |
| **`influenceType = INCREASE`, thị trường giảm toàn diện** | Trả ít hơn `top` records hoặc rỗng → empty state |
| **`tradingDate` là ngày tương lai** | FE disable date picker cho future date |
| **Đổi `exchange`** | Invalidate cache theo `(exchange, tradingDate)`; reset chart state |
| **`perChange` / `closePrice` null** | FE null-safe: hiển thị `—` thay vì crash |

---

## 7. Implementation Notes

### 7.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `market-query-v2` | Vietstock proxy, request/response mapping |
| **Vietstock External API** | Data source upstream |

### 7.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, enum values
- Vietstock validates: Business rules (data availability)
- NO duplicate business logic

**2. Data Passthrough:**
- Response map 1-1 từ Vietstock fields → TradeX camelCase fields
- TradeX KHÔNG tổng hợp hay tính toán thêm (trừ enum transform `OrderType` → `direction`)
- FE chịu trách nhiệm format display (thousand separator, đơn vị %, tỷ)

**3. Polling Strategy:**
- Trong giờ giao dịch (09:00–15:00 VN): FE polling mỗi 15–30s
- Ngoài giờ: Load 1 lần (EOD data), không polling
- FE tự quản lý polling lifecycle theo trading session state

**4. Auto-Population:**
- `tradingDate` default = current trading date nếu không truyền (recommend FE truyền explicit)

**5. Caching:**
- Cache key: `(exchange, tradingDate, top, influenceType)`
- Invalidate khi user đổi `exchange` hoặc pull-to-refresh

### 7.3 Acceptance Criteria

- [ ] Tab Market Watch hiển thị section "Nhóm dẫn dắt thị trường" với bar chart + dropdown sàn.
- [ ] Dropdown default `HOSE`, đổi sàn → gọi lại API + skeleton loading.
- [ ] Bar chart render đúng `influenceIndex` trên Y-axis, hỗ trợ giá trị âm.
- [ ] Bar color: `INCREASE` → xanh, `DECREASE` → đỏ (theo NHSV design system).
- [ ] Bar order: sort theo `row` ascending (trái → phải).
- [ ] Tap bar → tooltip hiện đủ: `stockCode`, `influenceIndex`, `influencePercent`, `closePrice`, `change`, `perChange`, `marketCap`, `weight`.
- [ ] Loading state: skeleton placeholder trong card.
- [ ] Empty state: message "Chưa có dữ liệu cho ngày này." khi array rỗng.
- [ ] Error state: message + retry button khi API fail; không crash Market Watch.
- [ ] Pull-to-refresh: refetch với param hiện tại.
- [ ] Null-safe: `closePrice`, `perChange` null → hiển thị `—`.

---

**Document Status:** 📋 Draft | **For:** FE Dev, BE Dev, QA | **Next Steps:** Review với tech lead trước khi implement
