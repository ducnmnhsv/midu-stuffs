# Nhóm dẫn dắt thị trường — Feature Specification

## 1. Overview

Tính năng **Nhóm dẫn dắt thị trường** hiển thị top cổ phiếu có mức độ tác động (đóng góp điểm) mạnh nhất tới chỉ số của một sàn / rổ chỉ số trong một phiên giao dịch. Người dùng có thể nhanh chóng nhận biết những mã đang kéo index lên hoặc kéo index xuống mạnh nhất.

**Business purpose:**

- Cung cấp insight nhanh về "ai đang dẫn dắt thị trường hôm nay".
- Hỗ trợ retail investor ra quyết định theo dõi/giao dịch các mã có ảnh hưởng lớn.
- Là module thường xuyên truy cập trong màn hình Market Watch của NHSV Pro.

**Data source:** Vietstock External API — proxied qua TradeX.

**Integration type:** TradeX-native (Vietstock proxy — không qua Lotte/Core).

---

## 2. User Story

> **As an** NHSV Pro retail investor,
> **I want to** xem nhanh top cổ phiếu có ảnh hưởng mạnh nhất đến VN-Index (hoặc HNX, VN30, UPCOM, HNX30) trong phiên,
> **so that** tôi biết những mã nào đang dẫn dắt thị trường tăng/giảm và có thể đưa ra quyết định giao dịch phù hợp.

**Acceptance criteria:**

- User chọn được sàn (HOSE / HNX / UPCOM / VN30 / HNX30).
- User thấy biểu đồ cột với mã cổ phiếu (X-axis) và mức đóng góp điểm vào index (Y-axis).
- Bar có màu xanh (đóng góp dương) hoặc đỏ (đóng góp âm) phân biệt rõ ràng.
- Tap vào bar hiển thị chi tiết: giá đóng cửa, % thay đổi, mức điểm ảnh hưởng.

---

## 3. UI/UX Behavior

### 3.1 Layout

- Section nằm trong màn hình **Market Watch**, dưới header tổng quan index.
- Header section:
  - Title: "Nhóm dẫn dắt thị trường".
  - **Dropdown sàn** (right-aligned): mặc định `HOSE`, các tuỳ chọn: HOSE, HNX, UPCOM, VN30, HNX30.
- Body: **Bar chart vertical** chiếm toàn bộ chiều ngang của section.

### 3.2 Bar chart

- **X-axis:** mã cổ phiếu (`stockCode`), sắp xếp theo `row` (BE đã sort theo độ ảnh hưởng giảm dần).
- **Y-axis:** mức đóng góp điểm vào index (`influenceIndex`). Hỗ trợ giá trị âm — bar đi xuống từ baseline 0.
- **Bar color:**
  - `direction = INCREASE` → màu xanh (UP color theo design system NHSV Pro).
  - `direction = DECREASE` → màu đỏ (DOWN color).
- **Default:** 20 bar, sàn HOSE, lấy cả tăng và giảm (`influenceType = ALL`).

### 3.3 Interactions

| Action | Behavior |
|---|---|
| Chọn sàn từ dropdown | Gọi lại API với `exchange` mới; loading skeleton trong khi chờ |
| Tap vào bar | Hiển thị tooltip / bottom sheet với chi tiết mã |
| Pull-to-refresh trên màn Market Watch | Refetch data với param hiện tại |
| Tap label mã (X-axis) | (Optional) Điều hướng vào chi tiết cổ phiếu |

### 3.4 Tooltip / Detail khi tap bar

```
stockCode (ví dụ: VCB)
Đóng góp: +1.25 điểm
Giá đóng cửa: 92,500 · +1,500 (+1.65%)
Vốn hóa: 450,000 tỷ
Trọng số trong index: 12.5%
```

---

## 4. API Integration

**TradeX endpoint:** `GET /api/v1/marketWatch/topStockInfluence`

**Upstream:** Vietstock `GET https://api-demo.vietstock.vn/demo/topstockinfluence`

**Data type:** EOD + polling 15–30s trong giờ giao dịch

**Auth:** JWT token (không cần `accountNumber` / `deviceUniqueId`)

### 4.1 Request parameters (TradeX → FE)

| Param | Type | Required | Values | Description |
|---|---|---|---|---|
| `exchange` | string | Yes | `HOSE`, `HNX`, `UPCOM`, `VN30`, `HNX30` | Sàn / rổ chỉ số — bind với dropdown UI |
| `tradingDate` | string | Yes | `yyyy-MM-dd` | Ngày giao dịch; default = current trading date |
| `top` | number | No | Default `20` | Số lượng mã trả về |
| `influenceType` | string | No | `ALL`, `INCREASE`, `DECREASE` | Default `ALL` |

**Default call:**

```
GET /api/v1/marketWatch/topStockInfluence?exchange=HOSE&tradingDate={today}&top=20&influenceType=ALL
```

### 4.2 Response fields (TradeX → FE)

| Field | Type | Description | UI Usage |
|---|---|---|---|
| `stockCode` | string | Mã cổ phiếu | X-axis label, header tooltip |
| `closePrice` | number | Giá đóng cửa hiện tại | Tooltip |
| `change` | number | Thay đổi giá tuyệt đối | Tooltip |
| `perChange` | number (%) | % thay đổi giá | Tooltip |
| `sharesOutstanding` | number | Khối lượng CP lưu hành | Tooltip (optional) |
| `marketCap` | number | Vốn hóa thị trường (VND) | Tooltip |
| `weight` | number | Trọng số mã trong chỉ số | Tooltip |
| `baseIndex` | number | Index đóng cửa ngày trước | Tính toán nội bộ |
| `influencePercent` | number (%) | % ảnh hưởng đến index | Tooltip phụ |
| `influenceIndex` | number | Số điểm đóng góp vào index | **Y-axis value (bar height)** |
| `direction` | string | `INCREASE` hoặc `DECREASE` | **Bar color rule** |
| `row` | number | Sort index | Sort key (ascending) |

### 4.3 BE mapping — TradeX ↔ Vietstock

**Request:**

| TradeX param | Vietstock param | Mapping |
|---|---|---|
| `exchange` | `CatID` | `HOSE`→`1`, `HNX`→`2`, `UPCOM`→`3`, `VN30`→`4`, `HNX30`→`5` |
| `tradingDate` | `TradeDate` | Direct |
| `top` | `Top` | Direct |
| `influenceType` | `Type` | `ALL`→`0`, `INCREASE`→`1`, `DECREASE`→`2` |

**Response:**

| Vietstock field | TradeX field | Transform |
|---|---|---|
| `StockCode` | `stockCode` | camelCase |
| `ClosePrice` | `closePrice` | camelCase |
| `Change` | `change` | camelCase |
| `PerChange` | `perChange` | camelCase |
| `KLCPLH` | `sharesOutstanding` | Rename |
| `MarketCap` | `marketCap` | camelCase |
| `Weight` | `weight` | camelCase |
| `BasicIndex` | `baseIndex` | Rename |
| `InfluencePercent` | `influencePercent` | camelCase |
| `InfluenceIndex` | `influenceIndex` | camelCase |
| `OrderType` | `direction` | `1`→`INCREASE`, `2`→`DECREASE` |
| `Row` | `row` | camelCase |

### 4.4 Error handling

| Tình huống | HTTP | TradeX code |
|---|---|---|
| Thiếu `exchange` hoặc `tradingDate` | 400 | `INVALID_PARAMETER` + `FIELD_IS_REQUIRED` |
| `exchange` không hợp lệ | 400 | `INVALID_PARAMETER` + `INVALID_VALUE` |
| `influenceType` không hợp lệ | 400 | `INVALID_PARAMETER` + `INVALID_VALUE` |
| Vietstock trả empty array | 200 | — (FE xử lý empty state) |
| Vietstock timeout / 5xx | 500 | `INTERNAL_SERVER_ERROR` |

---

## 5. Data Mapping

| UI Element | TradeX Field | Note |
|---|---|---|
| Dropdown sàn | `exchange` (request) | FE gửi string: `HOSE`, `HNX`... |
| Bar X-axis label | `stockCode` | |
| Bar Y-axis value | `influenceIndex` | có thể âm → bar đi xuống |
| Bar color | `direction` | `INCREASE` → green, `DECREASE` → red |
| Bar order (left → right) | `row` ascending | |
| Tooltip — giá đóng cửa | `closePrice` | format `vi-VN` thousand separator |
| Tooltip — Δ giá | `change` + `perChange` | hiển thị `+/-` + % |
| Tooltip — vốn hóa | `marketCap` | format `tỷ` / `nghìn tỷ` |
| Tooltip — trọng số | `weight` | hiển thị dạng % |
| Tooltip — đóng góp | `influenceIndex` + `influencePercent` | đơn vị: điểm |

---

## 6. Edge Cases

| Case | Behavior |
|---|---|
| **No data** (API trả empty array) | Empty state: "Chưa có dữ liệu cho ngày này." |
| **Market closed / weekend / holiday** | Banner: "Dữ liệu phiên gần nhất: dd/MM/yyyy"; data theo phiên gần nhất |
| **Single bar** | Render đầy đủ chart; không vỡ layout |
| **`influenceIndex` âm** (`influenceType = ALL`) | Y-axis hỗ trợ giá trị âm, baseline 0 |
| **API timeout / 5xx** | Error state với retry button; giữ snapshot data cũ nếu có |
| **Slow network** | Skeleton loading; tránh layout shift khi polling |
| **`influenceType = INCREASE` nhưng thị trường giảm toàn diện** | Có thể trả ít hơn `top` records hoặc rỗng → empty state |
| **`tradingDate` là ngày tương lai** | FE disable date picker cho future dates |
| **Đổi `exchange`** | Invalidate cache theo `(exchange, tradingDate)`; reset `weight` / `baseIndex` |
| **`perChange` / `closePrice` null** | FE null-safe: hiển thị `—` thay vì crash |

---

Document Status: 📋 Draft | For: FE Dev, BE Dev, QA | Next Steps: Review with tech lead
