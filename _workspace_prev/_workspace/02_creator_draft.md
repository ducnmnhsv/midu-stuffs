# Creator Draft Summary — Market Watch Feature Specs

**Task:** #2 — Tạo Feature Spec cho Nhóm dẫn dắt & Biến động ngành
**Owner:** creator
**Status:** Done

---

## Files created

1. `New feature in NHSV Pro/Market_Watch/Top_Stock_Influence_Spec.md`
2. `New feature in NHSV Pro/Market_Watch/Sector_Treemap_Spec.md`

---

## Spec 1 — Top Stock Influence

Cấu trúc 6 sections + footer:

- **1. Overview** — module trong Market Watch, hiển thị top mã tác động mạnh nhất tới index; nguồn Vietstock.
- **2. User Story** — retail investor xem nhanh ai đang dẫn dắt thị trường + acceptance criteria.
- **3. UI/UX Behavior**
  - Layout: title + dropdown sàn (right), bar chart vertical.
  - Bar chart: X = `StockCode`, Y = `InfluenceIndex` (support âm), color theo `OrderType` (1=green, 2=red).
  - Interactions: chọn sàn → refetch; tap bar → tooltip; pull-to-refresh.
  - Tooltip detail: code, đóng góp điểm, giá, %, vốn hóa, trọng số.
- **4. API Integration**
  - Endpoint: `GET https://api-demo.vietstock.vn/demo/topstockinfluence`
  - Default call: `CatID=1, TradeDate=today, Top=20, Type=0`.
  - Bảng params (4 cột) + bảng response fields (12 fields).
- **5. Data Mapping** — bảng map UI element ↔ API field.
- **6. Edge Cases** — empty, market closed, single bar, negative bar, timeout, slow network, future date, đổi CatID, null fields.
- **Footer** rule C5.

---

## Spec 2 — Sector Treemap

Cấu trúc 6 sections + footer:

- **1. Overview** — treemap đa metric, màu theo `PerChange`, 2 API merge.
- **2. User Story** — đánh giá sức khoẻ ngành + acceptance criteria.
- **3. UI/UX Behavior**
  - Tabs ngang sticky: Giá trị GD / Khối lượng GD / Vốn hóa (mở rộng: KLNN mua, KLNN bán).
  - Cell color scale 5 levels (green đậm/nhạt, grey, red nhạt/đậm).
  - Cell label 3 lines (Name, PerChange%, metric value).
  - Interactions: tap tab, tap cell (bottom sheet), nút Toàn màn hình, pull-to-refresh.
  - Bottom sheet 3 blocks: index/Δ, vol/val/foreign, định giá + lịch sử.
- **4. API Integration**
  - API 1 `sectorindex`: 3 params (Type, LanguageID, TradingDate), 11 response fields.
  - API 2 `GetDetailSector`: 1 param (languageID), 17 response fields.
  - Join: `sectorindex.ID = GetDetailSector.VSTSectorID`; gọi song song; default `SectorLevel=1`.
- **5. Tab → Cell Size Mapping** — bảng 5 dòng (Val, Vol, MarketCapital, ForeignBuyVol, ForeignSellVol).
- **6. Edge Cases** — empty, market closed, single sector, ID mismatch (cả 2 chiều), PerChange=0, null metric, null MarketCapital, TradingDate mismatch, mix SectorLevel, >30 ngành, partial API fail, slow network.
- **Footer** rule C5.

---

## Key design decisions

- Cả 2 spec đều **Feature Spec** (PM/BA readable), không phải API Spec — không đặt vào folder Specs/.
- Default value cho Spec 1: Top=20 (theo brief), không phải 10.
- Tab "GTGD/KLGD/Vốn hoá" aliases trong analyst findings → gộp vào 3 tab chính + KLNN mua/bán (mode mở rộng) để tránh trùng lặp UI.
- Edge case partial API fail (API 2) cho Spec 2: disable tab Vốn hóa thay vì block toàn bộ treemap.
- Footer rule C5 áp dụng đúng format: `Document Status: 📋 Draft | For: FE Dev, BE Dev, QA | Next Steps: Review with tech lead`.
