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

Document Status: 🔄 Updated | For: FE Dev, BE Dev, QA | Next Steps: Confirm sectorStocks Vietstock endpoint với BE; Review drill-down UX với design team
