# Validator Report

## Top_Stock_Influence_Spec.md

- **Status:** PASS
- **Path:** `/Users/ducnguyen/Documents/project/tradex-monitoring/New feature in NHSV Pro/Market_Watch/Top_Stock_Influence_Spec.md`

### Checklist results
- [x] File-level: tồn tại đúng path, PascalCase + underscore, không có brackets/prefix.
- [x] Structure: H1 đúng (`# Nhóm dẫn dắt thị trường — Feature Specification`); đủ 6 sections (Overview, User Story, UI/UX Behavior, API Integration, Data Mapping, Edge Cases).
- [x] User Story format `As an ... I want to ... so that ...` đúng chuẩn.
- [x] Footer C5: `Document Status: 📋 Draft | For: FE Dev, BE Dev, QA | Next Steps: Review with tech lead` — present (line 148).
- [x] Request params 4 fields (CatID, TradeDate, Top, Type) đầy đủ với valid values rõ ràng.
- [x] Default call ghi rõ: `CatID=1 (HOSE), Top=20, Type=0` (section 4.2).
- [x] Response fields đủ 12 fields: StockCode, ClosePrice, Change, PerChange, KLCPLH, MarketCap, Weight, BasicIndex, InfluencePercent, InfluenceIndex, OrderType, Row.
- [x] Bar color rule: `OrderType=1` → green, `OrderType=2` → red (section 3.2 + Data Mapping).
- [x] Y-axis hỗ trợ giá trị âm được note (section 3.2 + Edge Cases).
- [x] Data Mapping table với UI Element → API Field (section 5, 10 rows).
- [x] Edge cases: no data, market closed, single bar, negative bar, timeout, slow network, future date, null values, CatID switch — đầy đủ.

### Issues found
- None.

### Fixes applied
- None.

---

## Sector_Treemap_Spec.md

- **Status:** PASS
- **Path:** `/Users/ducnguyen/Documents/project/tradex-monitoring/New feature in NHSV Pro/Market_Watch/Sector_Treemap_Spec.md`

### Checklist results
- [x] File-level: tồn tại đúng path, PascalCase + underscore, không có brackets/prefix.
- [x] Structure: H1 đúng (`# Biến động ngành (Sector Treemap) — Feature Specification`); đủ 6 sections.
- [x] User Story format đúng chuẩn.
- [x] Footer C5 present (line 214).
- [x] 2 API endpoints tách riêng — section 4.1 (sectorindex) và 4.2 (GetDetailSector).
- [x] Join key ghi rõ: `sectorindex.ID = GetDetailSector.VSTSectorID` (section 4.3).
- [x] Gọi song song được ghi trong Join strategy (section 4.3, gạch đầu dòng "Strategy").
- [x] Tab → Cell Size Mapping đủ 5 rows: Giá trị GD/Val, Khối lượng GD/Vol, Vốn hóa/MarketCapital, KLNN mua/ForeignBuyVol, KLNN bán/ForeignSellVol.
- [x] Cell color rule: green >0, red <0, grey =0; có 5 levels gradient (`> +2%`, `0..+2%`, `0`, `-2..0%`, `< -2%`).
- [x] Nút "Toàn màn hình" được mention (section 3.1 header + section 3.4 Interactions + Acceptance criteria).
- [x] Edge cases: no data, market closed, single sector, ID mismatch cả 2 chiều (API 1 thiếu trong API 2 và ngược lại), PerChange=0, metric=null, TradingDate mismatch, SectorLevel mix, timeout — đầy đủ.

### Convention compliance (cả 2 files)
- [x] CommonMark strict, ATX headers, fenced code blocks (plain text với ngữ cảnh hợp lý).
- [x] Folder placement đúng: `New feature in NHSV Pro/Market_Watch/`.
- [x] Markdown tables render hợp lệ.
- [x] Feature Spec — không áp tradex-api-conventions, naming Vietstock giữ nguyên (StockCode, CatID, VSTSectorID, ...).

### Issues found
- None.

### Fixes applied
- None.

---

## Overall: PASS

Cả 2 files đều đạt 100% checklist của Task #3. Không có lỗi nhỏ cần sửa, không có lỗi lớn cần báo lại creator. Files đã sẵn sàng finalize tại `New feature in NHSV Pro/Market_Watch/`.
