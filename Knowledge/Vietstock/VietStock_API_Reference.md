# VietStock Demo API — Field Reference

**Source:** `_API_VietStock_Demo_Document_V1.docx` (extracted 2026-07-02)
**Base URL (demo):** `https://api-demo.vietstock.vn/demo/`
**Base URL (production chart):** `https://api.vietstock.vn/ta/history` (TradingView format — đang dùng trong TradeX)

> **Convention docx:** "EOD" = updated at end of day (hoặc cuối phiên); "REALTIME" = cập nhật intraday.
> Một số API label EOD nhưng thực tế có data intraday — cần verify với BE khi tích hợp.

---

## Quick Index

| # | Endpoint | Method | Type | Mô tả | TradeX Status |
| --- | --- | --- | --- | --- | --- |
| I.1 | `/exchangehistory` | GET | EOD | Lịch sử chỉ số thị trường | 🔄 Lotte |
| I.2 | `/stockdeals` | GET | REALTIME | Tick data khớp lệnh | 🔄 Lotte |
| I.3 | `/orderhistory` | GET | EOD | Lịch sử đặt lệnh | 🔄 Lotte |
| I.4 | `/stockbestprice` | GET | REALTIME | 3 giá tốt nhất | 🔄 Lotte |
| I.5 | `/stocktrading` | GET | EOD | Dữ liệu giao dịch cổ phiếu | 🔄 Lotte |
| I.6 | `/foreignhistory` | GET | EOD | Lịch sử nước ngoài | 🔄 Lotte |
| II.1 | `/history` | GET | REALTIME | OHLCV chart (index/stock/deriv/CW) | ✅ TradeX dùng |
| II.2 | `/GetDetailIndex` | GET | REALTIME | PE/PB/EPS/ROE theo sàn/chỉ số | ❌ Chưa tích hợp |
| II.3 | `/topstockinfluence` | GET | EOD | Top cổ phiếu tác động chỉ số | ❌ Chưa tích hợp |
| II.4 | `/sectorindex` | GET | EOD | Chỉ số ngành (KLGD/GTGD/KLNN) | ❌ Chưa tích hợp |
| II.5 | `/GetListStockBySector` | GET | EOD | Danh sách mã theo ngành | ❌ Chưa tích hợp |
| II.6 | `/GetDetailSector` | GET | REALTIME | Thống kê ngành (PE/PB/MarketCap) | ❌ Chưa tích hợp |

---

## II.4 — `/sectorindex` — Chỉ số ngành

**Dùng cho:** Sector Treemap — sector view (2 API đầu tiên trong spec)

**Method:** GET · **Type:** EOD (label trong doc; verify với BE khi tích hợp)

**Input:**

| Param | Type | Values | Ghi chú |
| --- | --- | --- | --- |
| `Type` | Number | 0=VS tăng, 1=VS giảm, 2=change tăng, 3=change giảm | Sort order |
| `LanguageID` | Number | 1=VI, 2=EN | TradeX hardcode 1 |
| `TradingDate` | Datetime | `yyyy-MM-dd HH:mm:ss` | Default = current |

**Output — confirmed field names:**

| Vietstock field | Type | TradeX field | Mô tả |
| --- | --- | --- | --- |
| `ID` | Number | `sectorId` | **Join key** — dùng làm input `sectorID` của `/GetListStockBySector` |
| `Name` | String | `sectorName` | Tên ngành |
| `Close` | Number | `close` | Chỉ số đóng cửa |
| `Change` | Number | `change` | Thay đổi tuyệt đối |
| `PerChange` | Number (%) | `perChange` | % thay đổi — dùng cho **cell color** treemap |
| `TradingDate` | String | `tradingDate` | Ngày giao dịch |
| `Vol` | Number | `tradingVolume` | KL khớp — tab **KLGD** |
| `Val` | Number | `tradingValue` | GT khớp — tab **GTGD** |
| `ForeignBuyVol` | Number | `foreignBuyVolume` | KL NN mua — tab **KLNN mua** |
| `ForeignSellVol` | Number | `foreignSellVolume` | KL NN bán — tab **KLNN bán** |
| `Row` | Number | `row` | Sort index |

**Example response:**

```json
[
  {
    "ID": 1,
    "Name": "Bán buôn",
    "Close": 38.077628,
    "Change": -0.177095,
    "PerChange": -0.462936,
    "TradingDate": "2022-09-16T00:00:00",
    "Vol": 17084201.0,
    "Val": 163889288400.0,
    "ForeignBuyVol": 8500.0,
    "ForeignSellVol": 379696.0,
    "Row": 1
  }
]
```

---

## II.5 — `/GetListStockBySector` — Danh sách mã theo ngành

**Dùng cho:** Sector Treemap — drill-down (API thứ 3, source [1])

**Method:** GET · **Type:** EOD → **cache 24h per sectorID** (không thay đổi intraday)

**Input:**

| Param | Type | Ghi chú |
| --- | --- | --- |
| `sectorID` | Number | = `sectorindex.ID` (confirmed join key) |
| `languageID` | Int | 1=VI, 2=EN; TradeX hardcode 1 |

**Output — confirmed field names:**

| Vietstock field | Type | TradeX field | Mô tả |
| --- | --- | --- | --- |
| `StockCode` | String | `stockCode` | **Join key** — map sang `/rest/api/v2/market/symbol/latest` field `s` |
| `FullName` | String | `stockName` | Tên công ty đầy đủ |
| `Exchange` | String | `exchange` | HOSE / HNX / UPCoM |
| `SectorID` | Number | _(internal)_ | Sub-sector ID — khác với input `sectorID` |
| `SectorName` | String | _(internal)_ | Tên sub-sector |
| `IndustryID` | Number | _(internal)_ | Mã ngành NAICS (top-level) |
| `SubIndustry` | Number | _(internal)_ | Mã ngành NAICS (sub-level) |
| `CatID` | Int | _(internal)_ | 1=HOSE, 2=HNX, 3=UPCoM |
| `STT` | Number | _(internal)_ | Số thứ tự |

> **⚠️ Sector ID mapping đã xác nhận:** `sectorindex.ID = 1` ("Bán buôn") → `GetListStockBySector?sectorID=1` trả danh sách stocks của "Bán buôn". Join key trực tiếp.
>
> **⚠️ Sub-sector note:** `SectorID` trong response (e.g. 6023, 6024) là sub-sector level — **khác** với `sectorID` input (1 = top-level). Chỉ dùng `StockCode` để join với TradeX market data.

**Example response:**

```json
[
  {
    "STT": 1,
    "Exchange": "HOSE",
    "StockCode": "AAT",
    "IndustryID": 600,
    "SubIndustry": 602,
    "SectorID": 6023,
    "SectorName": "Bán buôn quần áo, vải và vật tư liên quan",
    "FullName": "CTCP Tiên Sơn Thanh Hóa",
    "CatID": 1
  }
]
```

---

## II.6 — `/GetDetailSector` — Thống kê ngành

**Dùng cho:** Sector Treemap — `sectorDetail` source (section 4.2 trong spec)

**Method:** GET · **Type:** REALTIME

**Input:** `languageID` (1=VI, 2=EN) — TradeX hardcode 1. Không có filter sector, trả tất cả.

**Output — confirmed field names:**

| Vietstock field | Type | TradeX field | Mô tả |
| --- | --- | --- | --- |
| `VSTSectorID` | Number | `sectorId` | **Join key** với `sectorindex.ID` |
| `SectorName` | String | `sectorName` | Tên ngành |
| `SectorLevel` | Number | `sectorLevel` | Cấp ngành — filter `= 1` tránh double-count |
| `TradingDate` | DateTime | `tradingDate` | Ngày dữ liệu |
| `MarketCapital` | Number | `marketCap` | Vốn hóa ngành — tab **Vốn hóa** |
| `PE` | Number | `pe` | P/E ngành |
| `PB` | Number | `pb` | P/B ngành |
| `EPS` | Number | `eps` | EPS ngành |
| `ROA` | Number | `roa` | ROA ngành |
| `ROE` | Number | `roe` | ROE ngành |
| `PerChange1W` | Number (%) | `perChange1W` | % thay đổi 1 tuần |
| `PerChange1M` | Number (%) | `perChange1M` | % thay đổi 1 tháng |
| `PerChange3M` | Number (%) | `perChange3M` | % thay đổi 3 tháng |
| `PerChange6M` | Number (%) | `perChange6M` | % thay đổi 6 tháng |
| `PerChange52W` | Number (%) | `perChange52W` | % thay đổi 52 tuần |
| `Max52WCloseIndex` | Number | `high52W` | Đỉnh 52 tuần |
| `Min52WCloseIndex` | Number | `low52W` | Đáy 52 tuần |

**Example response:**

```json
[
  {
    "SectorName": "Bán buôn",
    "SectorLevel": 1,
    "TradingDate": "2022-09-15T00:00:00",
    "MarketCapital": 94680136427080.0,
    "PE": 18.24,
    "PB": 1.39,
    "EPS": 1159.73,
    "ROA": 0.38,
    "ROE": 1.02,
    "PerChange1W": 0.37,
    "PerChange1M": -4.78,
    "PerChange3M": -0.57,
    "PerChange6M": -28.50,
    "PerChange52W": -19.83,
    "Max52WCloseIndex": 58.72,
    "Min52WCloseIndex": 34.79,
    "VSTSectorID": 1
  }
]
```

---

## II.3 — `/topstockinfluence` — Top cổ phiếu tác động chỉ số

**Dùng cho:** Widget "Nhóm dẫn dắt thị trường" (Top Stock Influence)

**Method:** GET · **Type:** EOD

**Input:**

| Param | Type | Values |
| --- | --- | --- |
| `CatID` | String | 1=HOSE, 2=HNX, 3=UPCOM, 4=VN30, 5=HNX30 |
| `TradeDate` | Date | `yyyy-MM-dd` |
| `Top` | Number | Số lượng cổ phiếu (default 10) |
| `Type` | Number | 0=all, 1=top tăng, 2=top giảm |

**Output — confirmed field names:**

| Vietstock field | Type | Mô tả |
| --- | --- | --- |
| `StockCode` | String | Mã chứng khoán |
| `ClosePrice` | Number | Giá đóng cửa |
| `Change` | Number | Thay đổi tuyệt đối |
| `PerChange` | Number (%) | % thay đổi |
| `KLCPLH` | Number | Khối lượng cổ phiếu lưu hành |
| `MarketCap` | Number | Vốn hóa |
| `Weight` | Number | Trọng số trong chỉ số |
| `BasicIndex` | Number | Chỉ số đóng cửa ngày trước |
| `InfluencePercent` | Number | % ảnh hưởng lên chỉ số |
| `InfluenceIndex` | Number | Điểm ảnh hưởng lên chỉ số |
| `OrderType` | Number | 1=tăng, 2=giảm |
| `Row` | Number | Số thứ tự |

---

## II.2 — `/GetDetailIndex` — Thống kê chỉ số

**Method:** GET · **Type:** REALTIME

**Input:** `catID` (1=HOSE, 2=HNX, 3=UPCoM, 4=VN30, 5=HNX30)

**Output — confirmed field names:**

| Vietstock field | Type | Mô tả |
| --- | --- | --- |
| `IndexName` | String | Tên chỉ số |
| `TradingDate` | DateTime | Ngày giao dịch |
| `PE` | Number | P/E |
| `PB` | Number | P/B |
| `EPS` | Number | EPS |
| `ROA` | Number | ROA |
| `ROE` | Number | ROE |
| `PerChange1W` | Number (%) | % thay đổi 1 tuần |
| `PerChange1M` | Number (%) | 1 tháng |
| `PerChange3M` | Number (%) | 3 tháng |
| `PerChange6M` | Number (%) | 6 tháng |
| `PerChange52W` | Number (%) | 52 tuần |
| `Max52WCloseIndex` | Number | Đỉnh 52 tuần |
| `Min52WCloseIndex` | Number | Đáy 52 tuần |

---

## TradeX Internal — `/rest/api/v2/market/symbol/latest`

**Dùng cho:** Sector Treemap drill-down — source [2] (trading metrics per stock)

**Không phải Vietstock** — TradeX internal API, data từ Lotte realtime.

**Output — confirmed field names (từ production response):**

| API field | TradeX field | Type | Mô tả |
| --- | --- | --- | --- |
| `s` | `stockCode` | String | Mã CK — join key với `StockCode` từ `/GetListStockBySector` |
| `t` | `type` | String | STOCK / INDEX / DERIVATIVE |
| `c` | `close` | Number | Giá hiện tại / đóng cửa |
| `ch` | `change` | Number | Thay đổi tuyệt đối |
| `ra` | `perChange` | Number (%) | **% thay đổi** — cell color treemap |
| `o` | `open` | Number | Giá mở cửa |
| `h` | `high` | Number | Giá cao nhất |
| `l` | `low` | Number | Giá thấp nhất |
| `a` | `avgPrice` | Number | Giá trung bình |
| `vo` | `tradingVolume` | Number | **KL khớp** — tab KLGD |
| `va` | `tradingValue` | Number | **GT khớp (VND)** — tab GTGD |
| `mc` | `marketCap` | Number | **Vốn hóa** — tab Vốn hóa |
| `fr.bv` | `foreignBuyVolume` | Number | **KL NN mua** — tab KLNN mua |
| `fr.sv` | `foreignSellVolume` | Number | **KL NN bán** — tab KLNN bán |
| `fr.tr` | `foreignTotalRoom` | Number | Tổng room nước ngoài |
| `fr.cr` | `foreignCurrentRoom` | Number | Room nước ngoài còn lại |
| `hly[0].h` | `high52W` | Number | Đỉnh 52 tuần |
| `hly[0].l` | `low52W` | Number | Đáy 52 tuần |
| `bb` | `bidBook` | Array | Giá mua 3 bước — `[{p, v}]` |
| `bo` | `offerBook` | Array | Giá bán 3 bước — `[{p, v}]` |
| `tb` | `totalBuyVolume` | Number | Tổng KL đặt mua |
| `to` | `totalOfferVolume` | Number | Tổng KL đặt bán |
| `tor` | `turnoverRate` | Number | Tỷ lệ quay vòng |
| `ss` | `sessionStatus` | String | LO / ATO / ATC... |
| `ep` | `expectedPrice` | Number | Giá dự kiến khớp |

**Example response:**

```json
[{
  "s": "SSI", "t": "STOCK",
  "o": 27250, "h": 27500, "l": 27150, "c": 27200, "a": 27350,
  "ch": 0, "ra": 0,
  "vo": 5310900, "va": 145228345000,
  "mc": 67812026784000,
  "hly": [{"h": 44150, "l": 24350}],
  "tb": 632000, "to": 291600,
  "bb": [{"p": 27150, "v": 122000}, {"p": 27100, "v": 194200}, {"p": 27050, "v": 315800}],
  "bo": [{"p": 27200, "v": 58100}, {"p": 27250, "v": 105700}, {"p": 27300, "v": 127800}],
  "fr": {"bv": 183800, "sv": 522900, "tr": 2493089220, "cr": 1707886377},
  "tor": 0.213, "ss": "LO", "ep": 27250
}]
```

---

## Sector Treemap — Data Flow Summary

```text
Sector View (level 1):
  sectorindex (Vietstock EOD) + GetDetailSector (Vietstock REALTIME)
  → join on sectorindex.ID = GetDetailSector.VSTSectorID
  → TradeX endpoint: GET /api/v1/marketWatch/sectorIndex + /sectorDetail

Drill-down (level 2 — stocks in sector):
  GetListStockBySector (Vietstock EOD, cache 24h)  → stock list
  /rest/api/v2/market/symbol/latest (TradeX realtime) → trading metrics
  → join on GetListStockBySector.StockCode = symbol/latest.s
  → TradeX endpoint: GET /api/v1/marketWatch/sectorStocks?sectorId=X
```

---

*Extracted: 2026-07-02 | Source: `_API_VietStock_Demo_Document_V1.docx` | Scope: Sector APIs + TradeX /symbol/latest*
