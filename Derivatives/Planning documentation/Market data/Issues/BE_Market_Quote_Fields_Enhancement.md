# [BE] Market Quote Fields Enhancement — Basis (`bs`) + Accumulated Active Volume (`asv` / `asb`)

> Two related BE enhancements to the derivatives market quote model, bundled because both add computed fields to the same `realtime-v2` SymbolInfo model and both surface through the same REST + socket quote channels.

---

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

Người dùng xem một mã phái sinh (HĐTL VN30) cần hai thông tin quan trọng mà hệ thống hiện chưa cung cấp sẵn:

1. **Basis (chênh lệch cơ sở)** — khoảng cách giữa giá hợp đồng tương lai và giá chỉ số cơ sở. Đây là chỉ báo cốt lõi để nhà đầu tư phái sinh đánh giá premium/discount của hợp đồng. Hiện tại Frontend đang **tự tính** giá trị này, dẫn đến nguy cơ sai lệch giữa các màn hình, tính sai trong phiên định kỳ (ATO/ATC), và lặp lại logic ở nhiều nơi.

2. **Khối lượng khớp chủ động lũy kế (Mua chủ động / Bán chủ động)** — tổng khối lượng khớp được phân tách theo bên chủ động đặt lệnh. Đây là chỉ báo dòng tiền (order flow) giúp nhà đầu tư đánh giá áp lực mua/bán trong phiên. Hệ thống hiện **không trả** hai giá trị này trong response.

### Current vs Target

| Hạng mục | Hiện tại (Current) | Mục tiêu (Target) |
|----------|--------------------|--------------------|
| Basis | FE tự tính từ giá HĐTL và giá chỉ số | BE tính sẵn, trả field `bs` qua REST + socket |
| Tính Basis trong phiên ATO/ATC | Không nhất quán (FE dùng giá khớp) | Dùng đúng giá tham chiếu theo phiên (giá dự kiến trong ATO/ATC) |
| KL mua chủ động lũy kế | Không có | BE tính sẵn, trả field `asv` |
| KL bán chủ động lũy kế | Không có | BE tính sẵn, trả field `asb` |
| Reset đầu phiên | — | `asv` / `asb` reset về 0 khi sang phiên mới |

### Solution Approach (HIGH-LEVEL)

- **Basis:** BE xác định chỉ số cơ sở tương ứng với mã phái sinh, lấy giá mới nhất của cả hai chân (HĐTL và chỉ số) đã có sẵn trong cache realtime, rồi tính hiệu số và lưu vào model quote. Trong phiên ATO/ATC, BE dùng giá khớp dự kiến thay cho giá khớp tức thời để đảm bảo con số phản ánh đúng trạng thái phiên.
- **Khối lượng chủ động:** Mỗi lệnh khớp đã mang thông tin bên chủ động (mua hay bán). BE cộng dồn khối lượng từng lệnh khớp vào đúng bộ đếm (`asv` mua chủ động / `asb` bán chủ động) theo từng phiên giao dịch, và đặt lại về 0 khi bắt đầu phiên mới.
- Cả hai field được tính một lần tại tầng realtime và tự động xuất hiện ở **cả** REST API lẫn socket vì hai kênh này cùng đọc một model dữ liệu.

### Timeline

Cần BE xác nhận trong buổi grooming. Ước lượng sơ bộ: nhỏ–trung bình (cả hai đều là phép tính O(1) trên mỗi tick quote, không có truy vấn chéo tốn kém).

### Success Criteria

1. Field `bs` xuất hiện trong response quote của mã phái sinh, dấu (+/−) chính xác.
2. `bs` tính đúng trong cả phiên liên tục và phiên định kỳ (ATO/ATC).
3. Field `asv` (mua chủ động) và `asb` (bán chủ động) xuất hiện trong response quote.
4. Giá trị `asv` / `asb` khớp với tổng KL khớp theo từng chiều chủ động trong phiên.
5. `asv` / `asb` reset đúng tại đầu mỗi phiên giao dịch.
6. Cả ba field cập nhật realtime qua socket.
7. Không suy giảm hiệu năng của luồng quote.

---

## 🔍 Technical Background (PM CAN SKIP)

### Field naming

| Khái niệm | Field name | Ý nghĩa |
|-----------|-----------|---------|
| Basis | `bs` | Giá HĐTL − Giá chỉ số cơ sở (điểm, có thể âm) |
| Mua chủ động lũy kế | `asv` | Tổng KL khớp với bên **mua** chủ động trong phiên |
| Bán chủ động lũy kế | `asb` | Tổng KL khớp với bên **bán** chủ động trong phiên |
| Mã chỉ số cơ sở | `bc` | Mã chỉ số cơ sở, vd `"VN30"` |

### Aggressor-side mapping (chiều chủ động)

Field `mb` (matchedBy) đánh dấu bên chủ động của lệnh khớp:

| `mb` | Cộng vào |
|------|----------|
| `ASK` | `asv` (mua chủ động) |
| `BID` | `asb` (bán chủ động) |

Raw Lotte: `parts[24]` → `"83"` = ASK, `"66"` = BID (đã được `market-collector-lotte` parse sẵn).

### Data-source fields

| Field | Ý nghĩa | Topic nguồn |
|-------|---------|-------------|
| `c` | current/last price | `quoteUpdate` |
| `mb` | matchedBy — bên chủ động (ASK/BID) | `quoteUpdate` |
| `mv` | matchingVolume — KL của lệnh khớp **gần nhất** | `quoteUpdate` |
| `vo` | volume — tổng KL khớp lũy kế phiên | `quoteUpdate` |
| `ss` | session — ATO/LO/ATC/PLO/CLOSED | `bidOfferUpdate` |
| `ep` | expectedPrice — giá khớp dự kiến (chỉ có trong ATO/ATC) | `extraUpdate` |

### Basis calculation — logic đề xuất (cần BE xác nhận)

| Phiên | Công thức đề xuất |
|-------|-------------------|
| Liên tục (LO) | `basis = futures.c − underlying.c` |
| ATO / ATC | `basis = futures.ep − underlying.ep` (dùng giá dự kiến cho **cả hai chân** để nhất quán; fallback về `c` nếu `ep` null) |

Đây là câu trả lời đề xuất cho câu hỏi mở của FE ("ATO/ATC dùng expectedPrice hay currentPrice?") → **dùng `expectedPrice` trong ATO/ATC, `currentPrice` ngoài ATO/ATC** — nhưng là quyết định thiết kế, cần BE chốt.

---

## 📝 Detailed Requirements (PM CAN SKIP)

### Pipeline & implementation surface

```
Lotte WS → market-collector-lotte (Java) → Kafka
         → realtime-v2 (Java, ghi Redis) + ws-v2 (Node, publish WS)
REST /api/v2/market/symbol/{code}/quote  ← đọc Redis (do realtime-v2 ghi)
Socket market.quote.{code}               ← publish bởi ws-v2 (rút gọn tên field)
```

**Hệ quả:** Tính `bs`/`asv`/`abv` ở **`realtime-v2`** thì cả REST và socket cùng có (chung model). `ws-v2/parser.js` cần thêm short-key vào payload publish.

| Tầng | Component | Thay đổi |
|------|-----------|----------|
| Collector | `market-collector-lotte` `WsConnection.handleStockQuote()` | Đã cung cấp `mb`, `mv` — nhiều khả năng không đổi |
| Realtime | `realtime-v2` `QuoteUpdateHandler` / `QuoteService.updateQuote()` | Cộng dồn `asv`/`asb` theo phiên từ `mb`+`mv`; tính `bs` từ HĐTL vs chỉ số `bc`; ghi Redis SymbolInfo |
| Realtime | `realtime-v2` `MarketStatusService` (reset path) | Reset `asv`/`asb` = 0 tại đầu phiên |
| Underlying lookup | `realtime-v2` `IndexStockListRepository` / symbol metadata | Resolve HĐTL → chỉ số cơ sở (`bc`); lấy giá live/expected của chỉ số |
| WS publish | `ws-v2` `parser.js` `convertDataPublishV2Quote()` | Thêm short-key `bs`, `asv`, `asb` vào payload |
| REST | `market-query-v2` (`/symbol/{code}/quote`) | Đảm bảo nhóm field phái sinh gồm `bs`, `asv`, `asb` được trả |

### Symbol → underlying index mapping (input từ FE spec — cần BE xác nhận)

| Prefix | Underlying |
|--------|-----------|
| `41I1xxxxx` | VN30 |
| `41I2xxxxx` | VN100 |

Field `bc` (baseCode) đã có sẵn chỗ chứa mã chỉ số cơ sở. Quy tắc prefix `41Ix` **chưa được xác nhận** trong TradeX Knowledge — xem clarify-questions.

### Performance

Cả hai phép tính là O(1) trên mỗi tick: basis = một phép trừ (giá chỉ số cơ sở đã cache trong Redis); asv/abv = một phép cộng accumulator theo `mb`. Reset phiên là thao tác clear một lần tại ranh giới phiên. Không có truy vấn chéo symbol tốn kém.

### Clarify-questions cho BE (cần chốt trong grooming) — 5 mục

1. **Channel `market.quote.dr`:** Brief nhắc kênh `market.quote.dr`. Knowledge chỉ document `market.quote.{symbol}` (vd `41I1G6000`). Suffix `.dr` không có trong Knowledge — có thể là typo hoặc suffix instance/env. **Cần xác nhận tên kênh chính xác.**
2. **Quy tắc prefix `41I1`/`41I2` → VN30/VN100:** Là input từ FE spec, chưa verify với symbol scheme của Lotte hay code live. BE resolve underlying bằng prefix hay bằng field metadata sẵn có? **Cần xác nhận.**
3. **VN100 futures:** Knowledge chỉ document HĐTL VN30. Sự tồn tại của HĐTL VN100 cần xác nhận.
4. **Giá tham chiếu Basis trong ATO/ATC:** Đề xuất dùng `ep`, nhưng là quyết định thiết kế của BE.
5. **REST hiện có expose nhóm field phái sinh chưa:** Cần xác nhận `/symbol/{code}/quote` hiện đã trả nhóm field phái sinh để bổ sung `bs`/`asv`/`asb`.

> Lưu ý nguồn: Phân tích dựa trên TradeX Knowledge (authoritative). FE codebase `nhsv-mts-rn` không truy cập được trong phiên phân tích — các tham chiếu FE chưa được verify.

---

**Document Status:** ✅ Validated — sẵn sàng cho BE grooming
**For:** Backend team (realtime-v2, ws-v2, market-query-v2), team-lead, FE (naming alignment)
**Next Steps:** BE trả lời 5 clarify-questions → implement tại `realtime-v2` + `ws-v2/parser.js` → verify REST + socket payload có đủ `bs`, `asv`, `asb`
