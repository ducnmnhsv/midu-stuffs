# Creator Meta — Validation handoff

**Task:** #2 done · Handoff to validator (Task #3).

---

## File paths (absolute)

1. `/Users/ducnguyen/Documents/project/tradex-monitoring/New feature in NHSV Pro/Market_Watch/Top_Stock_Influence_Spec.md`
2. `/Users/ducnguyen/Documents/project/tradex-monitoring/New feature in NHSV Pro/Market_Watch/Sector_Treemap_Spec.md`

---

## Validator checklist

### File-level

- [ ] Cả 2 files tồn tại đúng path `New feature in NHSV Pro/Market_Watch/`.
- [ ] Tên file PascalCase + underscore (`Top_Stock_Influence_Spec.md`, `Sector_Treemap_Spec.md`).
- [ ] Không có brackets `[ISSUE]` hay prefix đặc biệt.

### Structure (cả 2 files)

- [ ] Title H1 đúng (`# Nhóm dẫn dắt thị trường — Feature Specification`, `# Biến động ngành (Sector Treemap) — Feature Specification`).
- [ ] Đầy đủ 6 sections: Overview, User Story, UI/UX Behavior, API Integration, Data Mapping (Spec 1) / Tab → Cell Size Mapping (Spec 2), Edge Cases.
- [ ] User Story đúng format `As a [user], I want to... so that...`.
- [ ] Footer C5: `Document Status: 📋 Draft | For: FE Dev, BE Dev, QA | Next Steps: Review with tech lead`.

### Spec 1 — Top Stock Influence

- [ ] Bảng request params đầy đủ 4 params: `CatID`, `TradeDate`, `Top`, `Type` với valid values rõ ràng.
- [ ] Default call ghi rõ: `CatID=1 (HOSE), Top=20, Type=0`.
- [ ] Bảng response fields đủ 12 fields (`StockCode`, `ClosePrice`, `Change`, `PerChange`, `KLCPLH`, `MarketCap`, `Weight`, `BasicIndex`, `InfluencePercent`, `InfluenceIndex`, `OrderType`, `Row`).
- [ ] Bar color rule: `OrderType=1` → green, `OrderType=2` → red.
- [ ] Y-axis support giá trị âm được note.
- [ ] Data Mapping table có UI Element → API Field.
- [ ] Edge cases: no data, market closed, single bar có mention.

### Spec 2 — Sector Treemap

- [ ] 2 API endpoints được tách rõ section riêng (4.1, 4.2).
- [ ] Join key được ghi rõ: `sectorindex.ID = GetDetailSector.VSTSectorID`.
- [ ] Gọi song song được ghi trong Join strategy (4.3).
- [ ] Tab → Cell Size Mapping table có 5 rows: Giá trị GD/Val, Khối lượng GD/Vol, Vốn hóa/MarketCapital, KLNN mua/ForeignBuyVol, KLNN bán/ForeignSellVol.
- [ ] Cell color rule rõ: green >0, red <0, grey =0 (5 levels gradient).
- [ ] Nút Toàn màn hình được mention trong UI Behavior.
- [ ] Edge cases: no data, market closed, single bar (single sector), ID mismatch cả 2 chiều có mention.

### Convention compliance

- [ ] CommonMark strict, ATX headers, fenced code blocks có language ID (hoặc plain text với ngữ cảnh hợp lý).
- [ ] Folder placement đúng (`New feature in NHSV Pro/{FeatureArea}/`).
- [ ] Markdown tables render hợp lệ.
- [ ] Không dùng TradeX API conventions (đây không phải API Spec) — không cần kiểm `tradex-api-conventions.md`.

---

## Notes for validator

- Đây là **Feature Spec** (PM/BA + FE/BE/QA audience), không phải **API Spec**, nên không áp `Knowledge/TradeX/API Standards/*` checklist.
- Vietstock API là external API → naming giữ nguyên theo Vietstock (StockCode, CatID, etc.), không cần TradeX naming.
- Rule C3 (no code blocks trong Planning/) không áp dụng vì file nằm trong `New feature in NHSV Pro/`, không phải `Planning/`. Tuy nhiên code blocks đã được hạn chế chỉ dùng cho cell label sample, join logic pseudo và default call example.
