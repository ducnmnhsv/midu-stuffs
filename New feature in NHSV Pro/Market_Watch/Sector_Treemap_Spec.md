# Biến động ngành (Sector Treemap) — Feature Specification

## 1. Overview

Tính năng **Biến động ngành** hiển thị toàn cảnh các ngành trong thị trường dưới dạng **treemap chart**. Kích thước mỗi ô = giá trị metric đang chọn (giá trị giao dịch / khối lượng / vốn hóa / KLNN). Màu sắc ô = % thay đổi của ngành (xanh tăng / đỏ giảm).

**Business purpose:**

- Người dùng nhìn nhanh thấy ngành nào đang dẫn dắt, ngành nào đang chìm.
- Cho phép so sánh nhiều metric (GT giao dịch, KL giao dịch, vốn hóa) chỉ qua thao tác chuyển tab.
- Hỗ trợ ra quyết định phân bổ danh mục theo ngành.

**Data source:** Vietstock External API — 2 endpoints proxied qua TradeX.

**Integration type:** TradeX-native (Vietstock proxy — không qua Lotte/Core).

---

## 2. User Story

> **As an** NHSV Pro retail investor,
> **I want to** xem treemap các ngành theo nhiều góc nhìn (giá trị GD, khối lượng GD, vốn hóa, KLNN) với màu sắc biểu thị tăng/giảm,
> **so that** tôi nhanh chóng đánh giá được sức khoẻ từng ngành và nhận diện cơ hội đầu tư theo ngành.

**Acceptance criteria:**

- User chuyển tab thay đổi metric kích thước ô (cell size).
- Màu sắc ô phản ánh đúng chiều và biên độ thay đổi của ngành.
- Tap vào ô hiển thị chi tiết đầy đủ: index, %, vốn hóa, PE/PB, lịch sử 1W/1M/3M/6M/52W.
- Nút "Toàn màn hình" mở treemap ở chế độ landscape full-screen.

---

## 3. UI/UX Behavior

### 3.1 Layout

- Section trong màn hình **Market Watch**, dưới section "Nhóm dẫn dắt thị trường".
- Header section:
  - Title: "Biến động ngành".
  - Right: nút **Toàn màn hình** (icon expand).
- **Tabs ngang (sticky):** `Giá trị GD` | `Khối lượng GD` | `Vốn hóa`.
  - Mode mở rộng (optional, hidden by default): `KLNN mua` | `KLNN bán`.
- Body: **Treemap chart** — các ô xếp theo thuật toán treemap, kích thước tỉ lệ với metric.

### 3.2 Treemap rendering

- **Cell size:** tỉ lệ với metric của tab đang chọn (xem section 5).
- **Cell color** (dựa trên `perChange`):
  - `> +2%` → green đậm
  - `0..+2%` → green nhạt
  - `0` → grey
  - `-2..0%` → red nhạt
  - `< -2%` → red đậm

### 3.3 Cell label

```
Line 1: sectorName
Line 2: perChange % (ví dụ: +1.85%) — màu theo chiều tăng/giảm
Line 3 (chỉ khi cell đủ rộng): giá trị metric đang chọn (formatted)
```

### 3.4 Interactions

| Action | Behavior |
|---|---|
| Tap tab metric | Animate cell size về metric mới (transition smooth) |
| Tap vào cell ngành | **Drill-down:** chuyển sang stock treemap của ngành đó (không mở bottom sheet) |
| Tap vào cell mã CK (drill-down view) | Mở bottom sheet chi tiết mã |
| Tap nút Back (`< sectorName`) | Quay về sector treemap, giữ nguyên tab đang chọn |
| Tap nút Toàn màn hình | Chuyển sang landscape, treemap chiếm toàn bộ màn hình |
| Pull-to-refresh | Refetch cả API song song (sector view: 2 APIs; drill-down: thêm API 3) |

### 3.5 Drill-down view (Stock treemap trong ngành)

Khi user tap một ô ngành, màn hình chuyển sang **stock-level treemap** của ngành đó.

**Layout:**

```
Header bar:
  [< sectorName]                    [icon fullscreen]
  
Subheader:
  sectorName · perChange%           ← breadcrumb context

Tabs (sticky): giống sector view, cùng tab đang chọn
Treemap: các ô = mã CK trong ngành
```

**Cell rendering (stock):**

- Cell size: cùng metric với tab đang chọn (field lấy từ API 3)
- Cell color: `perChange` của từng mã (xanh/đỏ)
- Cell label:
  - Line 1: `stockCode` (VD: `SHB`, `VCB`)
  - Line 2: `perChange%`
- Màu `perChange = 0` ở stock view: **amber/gold** (phân biệt với neutral grey của sector view)

**Behavior:**

- Tab switch trong drill-down view → re-render treemap cùng ngành với metric mới (gọi lại API 3 nếu cần)
- Back button → trở về sector view, tab giữ nguyên

### 3.6 Bottom sheet — Stock detail (khi tap mã trong drill-down)

```
Header:  stockCode · sectorName (sub) · perChange%
Block 1: tradingVolume · tradingValue · foreignBuyVolume · foreignSellVolume
Block 2: marketCap · pe · pb
Block 3: (reserved — có thể expand sau)
```

### 3.7 Bottom sheet — Sector detail (deprecated)

> ⚠️ **Đã thay bởi drill-down (3.5).** Sector bottom sheet không cần thiết nữa vì tap = drill-down. Sector-level data (pe, pb, lịch sử 1W/1M...) có thể expose qua long-press nếu cần trong tương lai.

---

## 4. API Integration

Tính năng dùng **2 TradeX endpoints gọi song song** và **merge theo join key**.

### 4.1 sectorIndex

**TradeX endpoint:** `GET /api/v1/marketWatch/sectorIndex`

**Upstream:** Vietstock `GET https://api-demo.vietstock.vn/demo/sectorindex`

**Data type:** Realtime (polling 15–30s khi market open)

**Request parameters:**

| Param | Type | Required | Values | Description |
|---|---|---|---|---|
| `tradingDate` | string | Yes | `yyyy-MM-dd HH:mm:ss` | Ngày giao dịch; default = current |
| `sortOrder` | string | No | `VSTOCK_ASC`, `VSTOCK_DESC`, `CHANGE_ASC`, `CHANGE_DESC` | Default `VSTOCK_ASC` |

`LanguageID` — TradeX tự hardcode `1` (VI); FE không truyền.

**Response fields:**

| Field | Type | Description | UI Usage |
|---|---|---|---|
| `sectorId` | number | Mã ngành | **Join key** với sectorDetail |
| `sectorName` | string | Tên ngành | Cell label Line 1 |
| `close` | number | Index đóng cửa ngành | Bottom sheet header |
| `change` | number | Thay đổi tuyệt đối | Bottom sheet header |
| `perChange` | number (%) | % thay đổi | **Cell color** + Line 2 label |
| `tradingDate` | string | Ngày dữ liệu | Meta / banner |
| `tradingVolume` | number | Khối lượng khớp | Cell size — tab Khối lượng GD |
| `tradingValue` | number | Giá trị khớp (VND) | Cell size — tab Giá trị GD |
| `foreignBuyVolume` | number | KL nước ngoài mua | Cell size — tab KLNN mua |
| `foreignSellVolume` | number | KL nước ngoài bán | Cell size — tab KLNN bán |
| `row` | number | Sort index | Sort |

**BE mapping — sectorIndex:**

| Vietstock field | TradeX field |
|---|---|
| `ID` | `sectorId` |
| `Name` | `sectorName` |
| `Close` | `close` |
| `Change` | `change` |
| `PerChange` | `perChange` |
| `TradingDate` | `tradingDate` |
| `Vol` | `tradingVolume` |
| `Val` | `tradingValue` |
| `ForeignBuyVol` | `foreignBuyVolume` |
| `ForeignSellVol` | `foreignSellVolume` |
| `Row` | `row` |
| `Type` (request) | `sortOrder`: `VSTOCK_ASC`→`0`, `VSTOCK_DESC`→`1`, `CHANGE_ASC`→`2`, `CHANGE_DESC`→`3` |

---

### 4.2 sectorDetail

**TradeX endpoint:** `GET /api/v1/marketWatch/sectorDetail`

**Upstream:** Vietstock `GET https://api-demo.vietstock.vn/demo/GetDetailSector`

**Data type:** Cache 5–15 phút (vốn hóa & định giá ít biến động intraday)

**Request parameters:** Không có param từ FE. TradeX tự hardcode `languageID=1`.

**Response fields:**

| Field | Type | Description | UI Usage |
|---|---|---|---|
| `sectorId` | number | Mã ngành | **Join key** với sectorIndex |
| `sectorName` | string | Tên ngành | Fallback label |
| `sectorLevel` | number | Cấp ngành | Filter (chỉ cấp 1) |
| `tradingDate` | string | Ngày dữ liệu | Meta |
| `marketCap` | number | Vốn hóa ngành (VND) | Cell size — tab Vốn hóa |
| `pe` | number | P/E ngành | Bottom sheet Block 2 |
| `pb` | number | P/B ngành | Bottom sheet Block 2 |
| `eps` | number | EPS ngành | Bottom sheet Block 2 |
| `roa` | number (%) | ROA ngành | Bottom sheet Block 2 |
| `roe` | number (%) | ROE ngành | Bottom sheet Block 2 |
| `perChange1W` | number (%) | % đổi 1 tuần | Bottom sheet Block 3 |
| `perChange1M` | number (%) | % đổi 1 tháng | Bottom sheet Block 3 |
| `perChange3M` | number (%) | % đổi 3 tháng | Bottom sheet Block 3 |
| `perChange6M` | number (%) | % đổi 6 tháng | Bottom sheet Block 3 |
| `perChange52W` | number (%) | % đổi 52 tuần | Bottom sheet Block 3 |
| `high52W` | number | Đỉnh 52 tuần | Bottom sheet Block 3 |
| `low52W` | number | Đáy 52 tuần | Bottom sheet Block 3 |

**BE mapping — sectorDetail:**

| Vietstock field | TradeX field |
|---|---|
| `VSTSectorID` | `sectorId` |
| `SectorName` | `sectorName` |
| `SectorLevel` | `sectorLevel` |
| `TradingDate` | `tradingDate` |
| `MarketCapital` | `marketCap` |
| `PE` | `pe` |
| `PB` | `pb` |
| `EPS` | `eps` |
| `ROA` | `roa` |
| `ROE` | `roe` |
| `PerChange1W` | `perChange1W` |
| `PerChange1M` | `perChange1M` |
| `PerChange3M` | `perChange3M` |
| `PerChange6M` | `perChange6M` |
| `PerChange52W` | `perChange52W` |
| `Max52WCloseIndex` | `high52W` |
| `Min52WCloseIndex` | `low52W` |

---

### 4.3 sectorStocks (API thứ 3 — Drill-down)

**TradeX endpoint:** `GET /api/v1/marketWatch/sectorStocks`

**Data type:** Realtime (polling 15–30s khi market open)

**Architecture — TradeX BE aggregate từ 2 nguồn:**

```text
Client → GET /api/v1/marketWatch/sectorStocks?sectorId=X
           │
           ├─ [1] Vietstock /GetListStockBySector?sectorID=X  (EOD — cache 24h)
           │       → danh sách StockCode trong ngành
           │
           └─ [2] TradeX /rest/api/v2/market/symbol/latest?codes={stockCodes}
                   → toàn bộ trading metrics realtime (poll 15–30s)

           TradeX BE merge [1] join [2] on stockCode → response
```

> **Caching:** Source [1] là EOD — stock list trong ngành không đổi trong ngày → cache per `sectorId`, TTL = 24h hoặc đến 18:00 phiên. Source [2] là realtime, poll theo chu kỳ market.

**Request parameters:**

| Param | Type | Required | Description |
| --- | --- | --- | --- |
| `sectorId` | number | Yes | ID ngành — lấy từ `sectorIndex.sectorId` |

**Response fields:**

| Field | Type | Source | UI Usage |
| --- | --- | --- | --- |
| `stockCode` | string | `[1]` Vietstock `StockCode` | Cell label Line 1 |
| `stockName` | string | `[1]` Vietstock `FullName` | Bottom sheet header |
| `exchange` | string | `[1]` Vietstock `Exchange` | Badge (HOSE/HNX/UPCoM) |
| `perChange` | number (%) | `[2]` TradeX field `ra` | **Cell color** + Line 2 label |
| `tradingVolume` | number | `[2]` TradeX field `vo` | Cell size — tab **KLGD** |
| `tradingValue` | number | `[2]` TradeX field `va` | Cell size — tab **GTGD** |
| `foreignBuyVolume` | number | `[2]` TradeX field `fr.bv` | Cell size — tab **KLNN mua** |
| `foreignSellVolume` | number | `[2]` TradeX field `fr.sv` | Cell size — tab **KLNN bán** |
| `marketCap` | number | `[2]` TradeX field `mc` | Cell size — tab **Vốn hóa** |

**BE mapping — Vietstock `/GetListStockBySector` (confirmed từ API doc):**

| Vietstock field | TradeX field | Type | Ghi chú |
| --- | --- | --- | --- |
| `StockCode` | `stockCode` | String | Join key với source [2] field `s` |
| `FullName` | `stockName` | String | Tên công ty đầy đủ |
| `Exchange` | `exchange` | String | HOSE / HNX / UPCoM |
| `SectorID` | _(internal)_ | Number | Verify mapping với `sectorIndex.sectorId` |
| `CatID` | _(internal)_ | Int | 1=HOSE, 2=HNX, 3=UPCoM |

**BE mapping — TradeX `/rest/api/v2/market/symbol/latest` → response:**

| API field | TradeX field | Mô tả |
| --- | --- | --- |
| `s` | `stockCode` | Mã chứng khoán (join key) |
| `ra` | `perChange` | % thay đổi giá |
| `vo` | `tradingVolume` | KL khớp lệnh |
| `va` | `tradingValue` | GT khớp lệnh (VND) |
| `mc` | `marketCap` | Vốn hóa thị trường |
| `fr.bv` | `foreignBuyVolume` | KL nước ngoài mua |
| `fr.sv` | `foreignSellVolume` | KL nước ngoài bán |
| `c` | `close` | Giá đóng cửa / hiện tại |
| `ch` | `change` | Thay đổi tuyệt đối |
| `hly[0].h` | `high52W` | Đỉnh 52 tuần |
| `hly[0].l` | `low52W` | Đáy 52 tuần |

> ⚠️ **Action cần thiết với BE:** Confirm mapping giữa `sectorIndex.sectorId` (từ Vietstock `/sectorindex`) và `sectorID` input của `/GetListStockBySector` — trong API doc, input `sectorID=1` trả stocks với nhiều `SectorID` con khác nhau (6023, 6024...), cần xác nhận ID nào map đúng.

---

### 4.4 Join strategy

- **Join key:** `sectorIndex.sectorId` = `sectorDetail.sectorId`.
- **Strategy:** Gọi **song song** cả 2 endpoints. Render khi cả 2 hoàn tất, hoặc render sectorIndex trước rồi enrich từ sectorDetail khi sẵn sàng.
- **Filter mặc định:** `sectorLevel = 1` để tránh double-count đa cấp.

```text
sectors = sectorIndex.list
details = sectorDetail.list keyed by sectorId

For each s in sectors where sectorLevel === 1:
    d = details[s.sectorId]
    cell = {
        sectorId:   s.sectorId,
        label:      s.sectorName,
        color:      colorByPerChange(s.perChange),
        sizeMetric: pickByTab(currentTab, s, d),
        meta:       { ...s, ...d }
    }
```

### 4.5 Error handling

| Tình huống | HTTP | TradeX code |
| --- | --- | --- |
| sectorIndex timeout / 5xx | 500 | `INTERNAL_SERVER_ERROR` → empty state |
| sectorDetail timeout / 5xx | 500 | `INTERNAL_SERVER_ERROR` → vẫn render, disable tab Vốn hóa |
| sectorStocks timeout / 5xx | 500 | `INTERNAL_SERVER_ERROR` → drill-down empty state |
| sectorStocks trả empty array | 200 | — FE hiển thị "Chưa có dữ liệu mã trong ngành này" |
| Vietstock trả empty array | 200 | — (FE xử lý empty state) |

---

## 5. Tab → Cell Size Mapping

| Tab | TradeX Field | Source endpoint |
| --- | --- | --- |
| Giá trị GD | `tradingValue` | sectorIndex |
| Khối lượng GD | `tradingVolume` | sectorIndex |
| Vốn hóa | `marketCap` | sectorDetail |
| KLNN mua | `foreignBuyVolume` | sectorIndex |
| KLNN bán | `foreignSellVolume` | sectorIndex |

**Cell color (mọi tab, sector view):** `sectorIndex.perChange` (gradient red ↔ green).

**Drill-down view — tab → cell size mapping (stock level):**

| Tab | TradeX Field | Source endpoint |
| --- | --- | --- |
| Giá trị GD | `tradingValue` | sectorStocks |
| Khối lượng GD | `tradingVolume` | sectorStocks |
| Vốn hóa | `marketCap` | sectorStocks |
| KLNN mua | `foreignBuyVolume` | sectorStocks |
| KLNN bán | `foreignSellVolume` | sectorStocks |

**Cell color (drill-down, mọi tab):** `sectorStocks.perChange` của từng mã.

---

## 6. Edge Cases

| Case | Behavior |
| --- | --- |
| **No data** (cả 2 API trả empty) | Empty state: "Chưa có dữ liệu cho ngày này." |
| **Market closed / weekend / holiday** | Banner "Dữ liệu phiên gần nhất: dd/MM/yyyy" |
| **Single sector** | Treemap render 1 ô chiếm toàn bộ vùng vẽ |
| **sectorId trong sectorIndex không có trong sectorDetail** | Vẫn render cell; tab Vốn hóa hiển thị `—` hoặc exclude cell đó |
| **sectorId trong sectorDetail không có trong sectorIndex** | Bỏ qua (không có `perChange` để color) |
| **`perChange = 0`** | Cell màu grey neutral |
| **Metric = 0 hoặc null** (`foreignBuyVolume = 0`) | Cell rất nhỏ → áp min cell size threshold hoặc ẩn |
| **`marketCap = null`** ở tab Vốn hóa | Exclude cell hoặc overlay `—` |
| **2 API trả `tradingDate` khác nhau** (sectorDetail cache cũ) | Hiển thị ngày nhỏ hơn + warning nhẹ |
| **Mix nhiều `sectorLevel`** | Default filter `sectorLevel = 1` |
| **Số lượng ngành > 30** | Cân nhắc giới hạn top N hoặc cho phép zoom |
| **sectorDetail fail** | Vẫn render với tabs Giá trị GD / Khối lượng GD / KLNN; disable tab Vốn hóa |
| **sectorStocks fail (drill-down)** | Empty state trong drill-down view; back button vẫn hoạt động |
| **Stock có metric = 0** (ví dụ `foreignBuyVolume = 0`) | Áp min cell size threshold (≥ 8px) hoặc exclude stock đó khỏi treemap |
| **`perChange = 0` ở stock view** | Cell màu amber/gold (phân biệt với neutral grey sector view) |
| **sectorId drill-down không có stocks** | Empty state: "Chưa có dữ liệu mã trong ngành này." |
| **Slow network** | Skeleton treemap; tránh layout shift khi merge data |

---

---

## 7. FE Implementation Guide — Treemap Rendering Algorithm

> Section này dành riêng cho FE developer. Mục tiêu: render đúng layout như UI design (xem ảnh mẫu) — các ô lớn ở góc trên/trái, ô nhỏ dồn về phía dưới/phải, tỉ lệ ô gần vuông nhất có thể.

### 7.1 Thuật toán: Squarified Treemap

Dùng thuật toán **Squarified Treemap** (Bruls et al., 2000) — tối thiểu hóa aspect ratio của từng ô, tạo ra các ô gần vuông nhất có thể. Đây là thuật toán cho ra layout đúng như UI design.

**Library đề xuất:** `d3-hierarchy` (không cần DOM, chạy được trong React Native).

```js
import { hierarchy, treemap, treemapSquarify } from 'd3-hierarchy';
```

**Cách dùng cơ bản:**

```js
const root = hierarchy({ children: sectors })
  .sum(d => d.sizeMetric ?? 0)
  .sort((a, b) => b.value - a.value); // lớn → nhỏ

treemap()
  .tile(treemapSquarify)
  .size([containerWidth, containerHeight])
  .paddingInner(2)   // khoảng trắng giữa các ô (tạo border effect)
  .paddingOuter(0)
  (root);

// Sau khi chạy, mỗi leaf có: x0, y0, x1, y1 (tọa độ tuyệt đối)
const cells = root.leaves(); // [{ x0, y0, x1, y1, data: sectorData }, ...]
```

### 7.2 Data preparation trước khi đưa vào treemap

```text
1. Filter:  chỉ giữ sectors có sectorLevel === 1
2. Sort:    descending theo sizeMetric (field của tab đang active)
3. Null/0:  nếu sizeMetric === null → set = 0 (ô rất nhỏ, xem section 7.6)
4. Build:   hierarchy như code trên
```

`sizeMetric` theo tab:

| Tab active | sizeMetric field |
|---|---|
| Giá trị GD | `tradingValue` (từ sectorIndex) |
| Khối lượng GD | `tradingVolume` (từ sectorIndex) |
| Vốn hóa | `marketCap` (từ sectorDetail) |
| KLNN mua | `foreignBuyVolume` (từ sectorIndex) |
| KLNN bán | `foreignSellVolume` (từ sectorIndex) |

### 7.3 Container sizing

| Mode | Kích thước container |
|---|---|
| **Portrait — inline (Market Watch)** | width = screen width; height = `width × 1.3` (tỉ lệ dọc) |
| **Landscape — full-screen** | width = screen height; height = screen width (trừ status bar + nav bar) |

- `paddingInner`: **2dp** — tạo khoảng trắng trắng giữa các ô (giống border).
- `borderRadius` mỗi ô: **4dp**.
- Background container: `#FFFFFF` (trắng) — ánh sáng qua gap = border effect.

### 7.4 Cell rendering — từ tọa độ sang View

Mỗi leaf sau khi d3 tính toán cho ra `{ x0, y0, x1, y1 }`. Map sang React Native:

```jsx
cells.map(cell => (
  <View
    key={cell.data.sectorId}
    style={{
      position: 'absolute',
      left:   cell.x0,
      top:    cell.y0,
      width:  cell.x1 - cell.x0,
      height: cell.y1 - cell.y0,
      backgroundColor: colorByPerChange(cell.data.perChange),
      borderRadius: 4,
      justifyContent: 'center',
      alignItems: 'center',
      overflow: 'hidden',
    }}
  >
    <CellLabel cell={cell} />
  </View>
))
```

> **SVG vs View:** Với sector treemap (~15 ô), dùng absolute-positioned `<View>` — đơn giản hơn và performance tốt hơn. Với drill-down (50+ mã CK), cân nhắc `react-native-svg` + `<Rect>` để tránh quá nhiều View nodes.

### 7.5 Cell label — logic hiển thị theo kích thước ô

```text
cellWidth  = cell.x1 - cell.x0
cellHeight = cell.y1 - cell.y0

IF cellWidth >= 80dp AND cellHeight >= 50dp:
    Line 1: sectorName (bold, 13sp) — truncate nếu > 12 ký tự → "Hàng &..."
    Line 2: perChange% (12sp) — prefix "+" nếu dương

ELSE IF cellWidth >= 50dp AND cellHeight >= 35dp:
    Line 1: sectorName (11sp, truncated)
    Line 2: perChange% (10sp)

ELSE IF cellWidth >= 30dp AND cellHeight >= 25dp:
    Line 1: perChange% chỉ (9sp)

ELSE (quá nhỏ):
    Không render text — chỉ hiển thị màu
```

**Truncation rule:** Dùng `numberOfLines={1}` + `ellipsizeMode="tail"` của React Native — không cần tự cắt chuỗi.

### 7.6 Min cell threshold

| Condition | Hành vi |
|---|---|
| `sizeMetric === 0` hoặc `null` | Set giá trị tối thiểu = `0.3% of total sum` để ô vẫn xuất hiện; label ẩn |
| `cellWidth < 12dp` hoặc `cellHeight < 12dp` | Skip render (không thêm View); tránh layout rác |
| `foreignBuyVolume = 0` (tab KLNN mua, nhiều sector = 0) | Các ô size = min threshold, xếp ở góc cuối |

### 7.7 Color mapping — perChange → backgroundColor

```js
function colorByPerChange(pct) {
  if (pct === null || pct === undefined) return '#8993A4'; // grey
  if (pct >  2)  return '#006644'; // green đậm  (+2% trở lên)
  if (pct >  0)  return '#36B37E'; // green nhạt  (0 → +2%)
  if (pct === 0) return '#8993A4'; // grey neutral
  if (pct > -2)  return '#FF5630'; // red nhạt   (-2% → 0)
  return                '#BF2600'; // red đậm    (dưới -2%)
}
```

> Màu text label trong ô: `#FFFFFF` (trắng) — áp dụng cho cả xanh/đỏ/grey.

### 7.8 Tab switch animation

Khi user chọn tab khác, `sizeMetric` thay đổi → layout thay đổi. Animate mượt:

```text
1. Tính layout mới với sizeMetric mới (re-run d3.treemap())
2. Với mỗi ô: animate { left, top, width, height } từ giá trị cũ → mới
3. Duration: 300ms · Easing: ease-in-out
4. Màu (perChange) giữ nguyên trong suốt animation — không đổi theo tab
```

Dùng `Animated.Value` (RN built-in) hoặc `Reanimated 2` (khuyến nghị) cho smooth transition.

**Lưu ý:** Nếu tab mới có ô không tồn tại trong tab cũ (ví dụ sector có `marketCap = null` nên bị ẩn ở tab Vốn hóa), animate ô từ `opacity: 0, scale: 0` → `opacity: 1, scale: 1`.

### 7.9 Performance notes

- **Memoize layout:** Chỉ re-run `d3.treemap()` khi data hoặc `containerSize` thay đổi — wrap trong `useMemo`.
- **Polling update (realtime):** Mỗi 15–30s BE trả data mới → chỉ update `perChange` (màu) và `sizeMetric` (size). Re-run layout và animate.
- **Drill-down cache:** Giữ layout state của sector view khi user drill-down vào ngành — không re-compute khi back.
- **Avoid re-render:** Dùng `React.memo` cho từng cell View nếu sector count > 20.

---

Document Status: 🔄 Updated | For: FE Dev, BE Dev, QA | Next Steps: Confirm sectorStocks Vietstock endpoint với BE; Review drill-down UX với design team
