# Analyst Findings — Market Watch (Nhóm dẫn dắt & Biến động ngành)

**Source:** Vietstock External API (api-demo.vietstock.vn)
**Consumer:** NHSV Pro mobile app — Market Watch screen
**Prepared for:** Creator (Task #2 — Feature Spec generation)

---

## Feature 1 — Nhóm dẫn dắt thị trường (Top Stock Influence)

### 1.1 API

| Attribute | Value |
|---|---|
| **Endpoint** | `GET https://api-demo.vietstock.vn/demo/topstockinfluence` |
| **Method** | GET |
| **Auth** | Demo endpoint (no auth in spec) |
| **Refresh type** | On-demand (user mở screen / đổi filter); recommend polling 15–30s khi market open hoặc realtime via socket nếu Vietstock cung cấp |
| **Use case** | Hiển thị top N cổ phiếu tác động mạnh nhất đến index của một sàn vào một ngày giao dịch |

### 1.2 Input parameters

| Param | Type | Required | Valid values | Mô tả |
|---|---|---|---|---|
| `CatID` | int | Yes | `1`=HOSE, `2`=HNX, `3`=UPCOM, `4`=VN30, `5`=HNX30 | Sàn / rổ chỉ số → bind vào dropdown UI |
| `TradeDate` | string (yyyy-MM-dd) | Yes | Ngày giao dịch hợp lệ (working day) | Default = current trading date |
| `Top` | int | No | Default `10`; recommended 5–20 | Số lượng mã trả về |
| `Type` | int | Yes | `0`=all, `1`=top tăng (đóng góp dương), `2`=top giảm (đóng góp âm) | Có thể bind vào filter chip "Tăng/Giảm/Tất cả" |

### 1.3 Output fields

| Field | Type | Mô tả | Role trong UI |
|---|---|---|---|
| `StockCode` | string | Mã cổ phiếu | **X-axis label** (mỗi bar = 1 mã) |
| `ClosePrice` | number | Giá đóng cửa hiện tại | Tooltip / detail row |
| `Change` | number | Thay đổi giá tuyệt đối | Tooltip |
| `PerChange` | number (%) | % thay đổi giá | Tooltip / detail row |
| `KLCPLH` | number | Khối lượng CP lưu hành | Tooltip (optional) |
| `MarketCap` | number | Vốn hóa thị trường | Tooltip (optional) |
| `Weight` | number | Trọng số của mã trong chỉ số | Tooltip / debug |
| `BasicIndex` | number | Index đóng cửa ngày trước (baseline) | Tính toán nội bộ |
| `InfluencePercent` | number (%) | % ảnh hưởng đến index | Detail / tooltip phụ |
| `InfluenceIndex` | number | **Số điểm đóng góp vào index** | **Y-axis value (bar height)** |
| `OrderType` | int | `1`=tăng (đóng góp dương), `2`=giảm (đóng góp âm) | **Bar color rule** — 1 → xanh, 2 → đỏ |
| `Row` | int | Số thứ tự / index hàng | Sort key (ascending) |

### 1.4 UI mapping (Bar Chart)

```
X-axis  ← StockCode
Y-axis  ← InfluenceIndex (điểm đóng góp; có thể âm/dương)
Bar color:
  OrderType == 1  → Green (UP_COLOR)
  OrderType == 2  → Red   (DOWN_COLOR)
Sort order ← Row (ascending) → backend đã sort theo độ ảnh hưởng giảm dần
Bar label / tooltip:
  Header: StockCode
  Line 1: InfluenceIndex (đóng góp X.XX điểm)
  Line 2: ClosePrice · Change · PerChange%
  Line 3 (optional): MarketCap, Weight
Filter UI:
  Dropdown sàn      → CatID
  Date picker       → TradeDate
  Toggle Top N      → Top
  Chip Tăng/Giảm/All → Type
```

---

## Feature 2 — Biến động ngành (Sector Treemap)

Treemap chart, kích thước ô = metric của tab đang chọn, màu = `PerChange` (xanh/đỏ).

### 2.1 API 1 — sectorindex (danh sách ngành + KL/GT giao dịch + KLNN)

| Attribute | Value |
|---|---|
| **Endpoint** | `GET https://api-demo.vietstock.vn/demo/sectorindex` |
| **Method** | GET |
| **Refresh** | On-demand + polling 15–30s khi market open |
| **Use case** | List ngành cùng các metric giao dịch trong ngày |

**Input:**

| Param | Type | Required | Valid values | Mô tả |
|---|---|---|---|---|
| `Type` | int | Yes | `0`=sort Vstock tăng, `1`=sort Vstock giảm, `2`=sort thay đổi tăng, `3`=sort thay đổi giảm | Sort order do BE thực hiện |
| `LanguageID` | int | Yes | `1`=VI, `2`=EN | NHSV Pro mặc định `1` |
| `TradingDate` | datetime | Yes | Ngày giao dịch hợp lệ | Default = current trading date |

**Output:**

| Field | Type | Mô tả | Role |
|---|---|---|---|
| `ID` | int | **Mã ngành (VSTSectorID)** | **Join key** với API 2 |
| `Name` | string | Tên ngành | Cell label |
| `Close` | number | Index đóng cửa của ngành | Tooltip |
| `Change` | number | Thay đổi tuyệt đối | Tooltip |
| `PerChange` | number (%) | % thay đổi | **Cell color** (green/red gradient theo magnitude) |
| `TradingDate` | datetime | Ngày dữ liệu | Meta |
| `Vol` | number | Khối lượng khớp | Cell size — tab **Khối lượng GD** |
| `Val` | number | Giá trị khớp (VND) | Cell size — tab **Giá trị GD** |
| `ForeignBuyVol` | number | KL nước ngoài mua | Cell size — tab **KLNN mua** |
| `ForeignSellVol` | number | KL nước ngoài bán | Cell size — tab **KLNN bán** |
| `Row` | int | Sort index | Sort |

### 2.2 API 2 — GetDetailSector (vốn hóa + định giá + lịch sử)

| Attribute | Value |
|---|---|
| **Endpoint** | `GET https://api-demo.vietstock.vn/demo/GetDetailSector` |
| **Method** | GET |
| **Refresh** | Có thể cache lâu hơn (vốn hóa & định giá ít thay đổi intraday) |
| **Use case** | Cung cấp `MarketCapital` cho tab Vốn hóa + chỉ số định giá cho tooltip mở rộng |

**Input:**

| Param | Type | Required | Valid values | Mô tả |
|---|---|---|---|---|
| `languageID` | int | Yes | `1`=VI, `2`=EN | NHSV Pro mặc định `1` |

**Output:**

| Field | Type | Mô tả | Role |
|---|---|---|---|
| `VSTSectorID` | int | **Mã ngành** | **Join key** với API 1 (`sectorindex.ID`) |
| `SectorName` | string | Tên ngành | Fallback label |
| `SectorLevel` | int | Cấp ngành | Filter (chọn cấp 1 / cấp 2…) |
| `TradingDate` | datetime | Ngày dữ liệu | Meta |
| `MarketCapital` | number | Vốn hóa ngành | Cell size — tab **Vốn hóa** |
| `PE` | number | P/E ngành | Tooltip mở rộng |
| `PB` | number | P/B ngành | Tooltip mở rộng |
| `EPS` | number | EPS ngành | Tooltip mở rộng |
| `ROA` | number (%) | ROA ngành | Tooltip mở rộng |
| `ROE` | number (%) | ROE ngành | Tooltip mở rộng |
| `PerChange1W` | number (%) | % đổi 1 tuần | Tooltip / detail |
| `PerChange1M` | number (%) | % đổi 1 tháng | Tooltip / detail |
| `PerChange3M` | number (%) | % đổi 3 tháng | Tooltip / detail |
| `PerChange6M` | number (%) | % đổi 6 tháng | Tooltip / detail |
| `PerChange52W` | number (%) | % đổi 52 tuần | Tooltip / detail |
| `Max52WCloseIndex` | number | Đỉnh 52 tuần | Tooltip mở rộng |
| `Min52WCloseIndex` | number | Đáy 52 tuần | Tooltip mở rộng |

### 2.3 Join logic

```
sectors = sectorindex.list                              // ID, Name, PerChange, Vol, Val, ForeignBuyVol, ForeignSellVol
details = GetDetailSector.list keyed by VSTSectorID     // MarketCapital, PE, PB, EPS, ROA, ROE, lịch sử

For each s in sectors:
    d = details[s.ID]                                   // join: sectorindex.ID == GetDetailSector.VSTSectorID
    cell = {
        id:        s.ID,
        label:     s.Name (fallback d.SectorName),
        color:     colorByPerChange(s.PerChange),       // gradient red↔green
        sizeMetric: pickByTab(currentTab, s, d),        // see mapping below
        meta:      { ...s, ...d }
    }
```

Edge case: nếu `details[s.ID]` không tồn tại → vẫn render cell với `MarketCapital = null`; tab **Vốn hóa** sẽ exclude hoặc hiển thị placeholder.

### 2.4 Tab → field mapping

| Tab UI | Source API | Field → Cell size | Ghi chú |
|---|---|---|---|
| **Giá trị GD** | sectorindex | `Val` | Default tab; đơn vị VND |
| **Khối lượng GD** | sectorindex | `Vol` | Đơn vị: cổ phiếu |
| **Vốn hóa** | GetDetailSector | `MarketCapital` | Cần join; cache lâu hơn |
| **KLNN mua** *(mode mở rộng)* | sectorindex | `ForeignBuyVol` | |
| **KLNN bán** *(mode mở rộng)* | sectorindex | `ForeignSellVol` | |
| **GTGD** *(mode mở rộng)* | sectorindex | `Val` | Alias của tab Giá trị GD |
| **KLGD** *(mode mở rộng)* | sectorindex | `Vol` | Alias của tab Khối lượng GD |
| **Vốn hoá** *(mode mở rộng)* | GetDetailSector | `MarketCapital` | Alias của tab Vốn hóa |

### 2.5 UI mapping (Treemap)

```
Cell size  ← metric chọn theo tab (xem bảng 2.4)
Cell color ← PerChange:
   >  +2%   → green đậm
   0..+2%   → green nhạt
   0        → grey
  -2..0%    → red nhạt
   < -2%    → red đậm
Cell label:
  Line 1: Name (s.Name)
  Line 2: PerChange % (color-coded)
  Line 3 (nếu cell đủ rộng): giá trị metric đang chọn (formatted)
Tooltip (tap cell):
  Header:  Name · Close · Change · PerChange%
  Block 1: Vol, Val, ForeignBuyVol, ForeignSellVol
  Block 2: MarketCapital, PE, PB, EPS, ROA, ROE
  Block 3: PerChange 1W/1M/3M/6M/52W · Max52W / Min52W
Controls:
  Tabs ngang (sticky)              → switch sizeMetric
  Sort hint (mặc định bê BE sort)  → có thể bind Type param khi user đổi sort
  Fullscreen toggle                → render lại treemap chiếm toàn màn hình (horizontal layout)
```

---

## 3. Notes — Data types & Edge cases

### 3.1 Data type notes

- Tất cả số liệu giá/khối lượng nên parse `number` (có thể là `decimal`); FE format theo locale `vi-VN` (dấu `.` ngăn cách hàng nghìn).
- `PerChange*` là **đơn vị %** (đã nhân 100), không phải tỉ lệ thập phân.
- `TradingDate` từ API có thể là ISO datetime hoặc `yyyy-MM-dd HH:mm:ss` — FE chuẩn hóa về `Date` object trước khi format.
- `MarketCap` / `MarketCapital`: đơn vị VND (giá trị lớn → format `tỷ` / `nghìn tỷ`).
- `CatID`, `Type`, `OrderType`, `LanguageID`, `SectorLevel`: enum integer — FE define constants.
- Vietstock demo endpoint có thể trả `null` cho field thiếu dữ liệu → FE phải null-safe.

### 3.2 Edge cases cần cover trong spec

**Chung:**
- **Ngoài giờ giao dịch / weekend / holiday:** API có thể trả data của phiên gần nhất hoặc empty. FE cần hiển thị banner "Dữ liệu phiên gần nhất: dd/MM/yyyy".
- **Empty array:** Treemap / bar chart phải có empty state ("Chưa có dữ liệu cho ngày này").
- **API timeout / 5xx:** Hiển thị retry button, giữ snapshot data cũ nếu có.
- **Slow network:** Skeleton loading; tránh layout shift khi data update polling.

**Feature 1 (Top Influence):**
- `Type=1` (top tăng) nhưng thị trường giảm toàn diện → có thể trả ít hơn `Top` records (hoặc rỗng).
- `TradeDate` ngày tương lai / không hợp lệ → BE trả empty; FE disable date picker cho future dates.
- `InfluenceIndex` có thể âm (đặc biệt khi `Type=0` mix tăng/giảm) → trục Y phải support giá trị âm (hoặc render bar đi xuống từ baseline 0).
- Khi đổi `CatID` (sàn), `Weight` / `BasicIndex` reset theo rổ mới → FE phải invalidate cache theo `(CatID, TradeDate)`.

**Feature 2 (Sector Treemap):**
- **Sector trong API 1 không có trong API 2** (mismatched ID): cell vẫn hiển thị từ sectorindex; ẩn tab Vốn hóa cho cell đó hoặc fallback `null`.
- **Sector trong API 2 không có trong API 1**: bỏ qua (không hiển thị cell vì không có `PerChange` để color).
- **PerChange = 0**: cell màu trung tính (grey), không green/red.
- **Metric = 0 hoặc null** (vd `ForeignBuyVol = 0` cho ngành không có giao dịch NN): treemap algorithm sẽ render cell rất nhỏ / ẩn → cần min cell size threshold hoặc placeholder.
- **`MarketCapital = null`** ở tab Vốn hóa: exclude cell hoặc hiển thị overlay "—".
- **Hai API trả về với `TradingDate` khác nhau** (vd API 2 cache cũ): hiển thị `TradingDate` nhỏ nhất + warning nhẹ.
- **`SectorLevel`**: nếu mix nhiều cấp, treemap có thể bị double-count. Recommend filter mặc định ở 1 cấp duy nhất (vd `SectorLevel = 1`) hoặc dùng tham số filter từ FE.
- **Số lượng ngành lớn (>30)**: cell quá nhỏ → label không đọc được. Cân nhắc giới hạn top N hoặc cho zoom.

### 3.3 Performance & caching gợi ý cho Creator

- `topstockinfluence`: cache theo key `(CatID, TradeDate, Top, Type)`, TTL 15–30s trong giờ giao dịch.
- `sectorindex`: cache theo `(Type, LanguageID, TradingDate)`, TTL 15–30s.
- `GetDetailSector`: cache theo `languageID`, TTL 5–15 phút (data ít biến động intraday).
- Cân nhắc gọi song song API 1 + API 2 cho treemap; render khi cả 2 hoàn tất hoặc render API 1 trước rồi enrich từ API 2.

---

Document Status: ✅ Ready | For: Creator (Task #2) | Next Steps: Tạo 2 file Feature Spec tại `New feature in NHSV Pro/Market_Watch/`
