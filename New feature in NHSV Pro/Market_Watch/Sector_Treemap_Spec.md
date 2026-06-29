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
| Tap vào cell | Mở bottom sheet với chi tiết ngành |
| Tap nút Toàn màn hình | Chuyển sang landscape, treemap chiếm toàn bộ màn hình |
| Pull-to-refresh | Refetch cả 2 API song song |

### 3.5 Bottom sheet (khi tap cell)

```
Header:  sectorName · close · change · perChange%
Block 1: tradingVolume · tradingValue · foreignBuyVolume · foreignSellVolume
Block 2: marketCap · pe · pb · eps · roa · roe
Block 3: perChange1W / perChange1M / perChange3M / perChange6M / perChange52W
         high52W / low52W
```

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

### 4.3 Join strategy

- **Join key:** `sectorIndex.sectorId` = `sectorDetail.sectorId`.
- **Strategy:** Gọi **song song** cả 2 endpoints. Render khi cả 2 hoàn tất, hoặc render sectorIndex trước rồi enrich từ sectorDetail khi sẵn sàng.
- **Filter mặc định:** `sectorLevel = 1` để tránh double-count đa cấp.

```
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

### 4.4 Error handling

| Tình huống | HTTP | TradeX code |
|---|---|---|
| sectorIndex timeout / 5xx | 500 | `INTERNAL_SERVER_ERROR` → empty state |
| sectorDetail timeout / 5xx | 500 | `INTERNAL_SERVER_ERROR` → vẫn render, disable tab Vốn hóa |
| Vietstock trả empty array | 200 | — (FE xử lý empty state) |

---

## 5. Tab → Cell Size Mapping

| Tab | TradeX Field | Source endpoint |
|---|---|---|
| Giá trị GD | `tradingValue` | sectorIndex |
| Khối lượng GD | `tradingVolume` | sectorIndex |
| Vốn hóa | `marketCap` | sectorDetail |
| KLNN mua | `foreignBuyVolume` | sectorIndex |
| KLNN bán | `foreignSellVolume` | sectorIndex |

**Cell color (mọi tab):** `sectorIndex.perChange` (gradient red ↔ green).

---

## 6. Edge Cases

| Case | Behavior |
|---|---|
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
| **Slow network** | Skeleton treemap; tránh layout shift khi merge data |

---

Document Status: 📋 Draft | For: FE Dev, BE Dev, QA | Next Steps: Review with tech lead
