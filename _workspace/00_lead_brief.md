# Lead Brief — Market Watch: Nhóm dẫn dắt & Biến động ngành

**Request:** Tạo Feature Spec cho 2 tính năng Market Watch trong NHSV Pro, sử dụng Vietstock External API.

**Output destination:** `New feature in NHSV Pro/Market_Watch/`

---

## Tính năng 1: Nhóm dẫn dắt thị trường

**UI reference:** Bar chart (vertical), trục X = mã CK, trục Y = Mức độ đóng góp (điểm), bar xanh = tăng / đỏ = giảm. Có dropdown chọn sàn (HOSE, HNX, VN30...).

**API:** `GET https://api-demo.vietstock.vn/demo/topstockinfluence`

Input params:
- `CatID`: 1=HOSE, 2=HNX, 3=UPCOM, 4=VN30, 5=HNX30
- `TradeDate`: yyyy-MM-dd
- `Top`: số lượng cổ phiếu (default 10)
- `Type`: 0=all, 1=top tăng, 2=top giảm

Output fields:
- `StockCode` — mã CK
- `ClosePrice` — giá đóng cửa
- `Change` — thay đổi giá
- `PerChange` — % thay đổi
- `KLCPLH` — khối lượng cổ phiếu lưu hành
- `MarketCap` — vốn hóa
- `Weight` — trọng số
- `BasicIndex` — chỉ số đóng cửa ngày trước
- `InfluencePercent` — % ảnh hưởng
- `InfluenceIndex` — chỉ số ảnh hưởng (điểm đóng góp, Y-axis)
- `OrderType` — 1=tăng, 2=giảm
- `Row`

---

## Tính năng 2: Biến động ngành (Sector Treemap)

**UI reference:** Treemap chart, màu green/red theo PerChange. Tabs: Giá trị GD | Khối lượng GD | Vốn hóa (mode mở rộng thêm: KLNN mua | KLNN bán | GTGD | KLGD | Vốn hoá). Kích thước ô = giá trị metric được chọn. Có nút "Toàn màn hình".

### API 1: sectorindex (danh sách ngành + giá trị giao dịch)
`GET https://api-demo.vietstock.vn/demo/sectorindex`

Input:
- `Type`: 0=sort Vstock tăng, 1=sort Vstock giảm, 2=sort thay đổi tăng, 3=sort thay đổi giảm
- `LanguageID`: 1=Vietnamese, 2=English
- `TradingDate`: datetime

Output:
- `ID` — mã ngành
- `Name` — tên ngành
- `Close` — giá đóng cửa ngành
- `Change` — thay đổi
- `PerChange` — % thay đổi (màu treemap cell)
- `TradingDate`
- `Vol` — khối lượng khớp (tab Khối lượng GD → cell size)
- `Val` — giá trị khớp (tab Giá trị GD → cell size)
- `ForeignBuyVol` — KLNN mua
- `ForeignSellVol` — KLNN bán
- `Row`

### API 2: GetDetailSector (market cap + chỉ số định giá)
`GET https://api-demo.vietstock.vn/demo/GetDetailSector`

Input:
- `languageID`: 1=Vietnamese, 2=English

Output:
- `SectorName` — tên ngành
- `SectorLevel` — cấp ngành
- `TradingDate`
- `MarketCapital` — vốn hóa (tab Vốn hóa → cell size)
- `PE`, `PB`, `EPS`, `ROA`, `ROE` — chỉ số định giá
- `PerChange1W`, `PerChange1M`, `PerChange3M`, `PerChange6M`, `PerChange52W`
- `Max52WCloseIndex`, `Min52WCloseIndex`
- `VSTSectorID` — key để join với sectorindex.ID

**Join key:** `sectorindex.ID` = `GetDetailSector.VSTSectorID`

---

## Output yêu cầu

Creator cần tạo 2 file spec:
1. `New feature in NHSV Pro/Market_Watch/Top_Stock_Influence_Spec.md` — Feature spec tính năng Nhóm dẫn dắt
2. `New feature in NHSV Pro/Market_Watch/Sector_Treemap_Spec.md` — Feature spec tính năng Biến động ngành

Format: Feature Spec (không phải API Spec). Bao gồm: Overview, User Story, UI/UX Behavior, API Integration, Data Mapping, Edge Cases, Footer theo C5.
