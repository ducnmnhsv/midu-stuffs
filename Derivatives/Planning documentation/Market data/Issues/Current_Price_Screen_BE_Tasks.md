# BE Tasks – Current price screen (statistic, quote abv/asv)

> **Liên quan:** FE Issue [Derivatives_Current_Price_Screen](./Derivatives_Current_Price_Screen.md) (MKT.S2)  
> **Mục đích:** Bổ sung BE tasks chưa được mention trong 01_Integration_Plan cho màn Current price  
> **Created:** 2026-02-11

---

## Tổng quan

Màn Current price cần 4 nguồn dữ liệu:

| Phần | API / Nguồn | Trạng thái Planning |
|------|-------------|----------------------|
| Basic info, Bid/Ask | symbolInfo | ✅ Đã có (symbolInfo, bidoffer.dr) |
| Thống kê lệnh | `/api/v2/market/symbol/{symbol}/statistic` | ⚠️ Cần xác nhận cho derivatives |
| Aggressive matched | `/api/v2/market/symbol/{symbol}/quote` – abv, asv | ⚠️ Cần xác nhận abv/asv cho derivatives |

---

## BE Tasks bổ sung

### BE-CP-1: API statistic cho derivatives

| Trường | Giá trị |
|--------|---------|
| **Task** | Đảm bảo `GET /rest/api/v2/market/symbol/{symbol}/statistic` trả đúng cho mã phái sinh |
| **Service** | realtime-v2, market-query-v2 |
| **Mô tả** | API statistic hiện đọc từ Redis. Statistic cho equity được realtime-v2 cập nhật khi nhận `quoteUpdate` từ Kafka. Với derivatives: cần **quoteUpdateDR** trigger logic update statistic tương tự (TotalBuyVolume, TotalSellVolume, TradingVolume, prices array, …). Nếu chưa có: thêm DerivativeQuoteConsumer hoặc mở rộng QuoteService để xử lý quoteUpdateDR → updateStatistic cho mã derivatives. |
| **Acceptance** | Gọi `/api/v2/market/symbol/VN30F2501/statistic` trả về data thống kê (không 404, không empty khi có giao dịch). |
| **Estimate** | 1–2d |

---

### BE-CP-2: API quote / symbolInfo – abv, asv cho derivatives

| Trường | Giá trị |
|--------|---------|
| **Task** | Đảm bảo response `quote` và `symbolInfo` có field **abv** (aggressive buy volume), **asv** (aggressive sell volume) cho derivatives |
| **Service** | realtime-v2, market-query-v2 |
| **Mô tả** | FE màn Current price dùng abv, asv để hiển thị "Aggressive matched" như cơ sở. Cần map từ Lotte (matchedBy + matchingVolume hoặc field tương đương trong auto.dr.qt) sang abv, asv. Nếu Lotte không có sẵn: có thể suy từ mb (matchedBy) + mv (matchingVolume): BID → abv += mv, ASK → asv += mv (accumulate trong phiên). |
| **Acceptance** | Response `/api/v2/market/symbol/{symbol}/quote` và symbolInfo cho mã derivatives chứa abv, asv (number). |
| **Estimate** | 0.5–1d |

---

### BE-CP-3: WebSocket statistic realtime (optional)

| Trường | Giá trị |
|--------|---------|
| **Task** | Nếu equity có WebSocket `market.statistic` cho realtime thống kê, derivatives cần channel tương đương |
| **Service** | ws-v2, realtime-v2 |
| **Mô tả** | Hiện statistic được publish qua Kafka topic `statisticUpdate`. Cần xác nhận: derivatives quoteUpdateDR có trigger publish statistic qua channel không? Nếu equity có `market.statistic.{symbol}` hoặc tương tự, derivatives cần tương đương. |
| **Acceptance** | FE subscribe và nhận statistic update realtime cho mã derivatives (nếu design yêu cầu). |
| **Estimate** | 0.5–1d (nếu cần) |
| **Priority** | P1 (optional – FE có thể poll hoặc dùng initial load) |

---

## Checklist với 01_Integration_Plan

| Nội dung Planning | Đã cover? |
|-------------------|-----------|
| symbolInfo cho derivatives | ✅ FR-API-001, AGG-* |
| market.quote.dr.{code} | ✅ WS-*, 4.3.4 |
| market.bidoffer.dr.{code} | ✅ WS-*, 4.3.5 |
| Statistic API cho derivatives | ❌ → BE-CP-1 |
| Quote abv/asv cho derivatives | ❌ → BE-CP-2 |
| Statistic WebSocket (nếu có) | ❌ → BE-CP-3 |

---

## Reference

- [01_Integration_Plan.md](../Planning/01_Integration_Plan.md)
- [symbol-info-api.md](../../../../Knowledge/TradeX/System/symbol-info-api.md)
- FE Issue: [Derivatives_Current_Price_Screen](./Derivatives_Current_Price_Screen.md)
