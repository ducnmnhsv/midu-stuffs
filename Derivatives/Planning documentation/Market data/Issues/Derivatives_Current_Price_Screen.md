# [Epic DR-FE-MKT] Story MKT.S2: Màn Current price – Basic info, Bid/Ask, Thống kê lệnh, Aggressive matched

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-MKT – Derivatives Market FE  
> **Module:** Market  
> **Screens:** Current price (giá hiện tại)  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-11

---

## User Story

**As a** trader / user  
**I want to** xem màn Current price (chi tiết mã) với 3 tab Quote / Matched / Chart, Basic info, Bid/Ask, thống kê lệnh, Aggressive matched, và nút Buy/Sell để vào Trade  
**So that** tôi có đủ thông tin thị trường và có thể đặt lệnh phái sinh nhanh chóng.

---

## Acceptance Criteria

### Data flow (bắt buộc)

- [ ] **AC-01** Tất cả dữ liệu **initial** lấy từ **symbolInfo** (GET `/api/v2/market/symbolInfo?symbolList=[symbol]` hoặc từ cache symbol_static/latest đã có).
- [ ] **AC-02** Sau khi có dữ liệu initial từ symbolInfo, FE subscribe WebSocket để nhận **realtime updates**:  
  - Derivatives: `market.quote.dr.{code}`, `market.bidoffer.dr.{code}`  
  - Merge update vào UI ngay khi nhận message.

### Basic info & Bid/Ask

- [ ] **AC-03** Phần **Basic info** hiển thị từ symbolInfo (s, n1/n2, m, c, ch, ra, o, h, l, re, ce, fl, … theo design Figma).
- [ ] **AC-04** Phần **Bid/Ask** hiển thị sổ lệnh từ symbolInfo (bb, bo – 10 bước từ BE).
- [ ] **AC-05** Bid/Ask: dropdown cho phép chọn **3 bước** hoặc **10 bước** giá; khi click vào selector hiển thị 2 options (3 / 10); FE slice array bb/bo theo lựa chọn.

### Thống kê lệnh

- [ ] **AC-06** Phần **Thống kê lệnh** lấy từ API `GET /rest/api/v2/market/symbol/{symbol}/statistic`.
- [ ] **AC-07** Hiển thị đúng các field thống kê theo response (tradingVolume, totalBuyVolume, totalSellVolume, totalBuyRaito, totalSellRaito, prices array, …) theo design Figma.

### Aggressive matched

- [ ] **AC-08** Phần **Aggressive matched** lấy từ API `GET /rest/api/v2/market/symbol/{symbol}/quote` – dùng field **abv** (aggressive buy volume), **asv** (aggressive sell volume) như cơ sở (equity).
- [ ] **AC-09** Nếu quote/symbolInfo **chưa có abv/asv** (BE chưa trả cho derivatives): FE **fallback** hiển thị **"—"** hoặc **0**. Ghi rõ trong UI là đang chờ dữ liệu nếu có design. Xác nhận với BE khi API symbol/quote trả abv, asv cho derivatives.

### Tab Matched

- [ ] **AC-10** Tab **Matched** hiển thị danh sách lệnh khớp của mã phái sinh hiện tại – **cơ chế giống cơ sở** (equity) nhưng data cho symbol derivatives (VN30F*, 41B*, …). API/WebSocket tương tự equity, route theo symbol type.

### Tab Chart

- [ ] **AC-11** Tab **Chart** hiển thị biểu đồ cho mã phái sinh hiện tại – **xử lý giống cơ sở**, symbol = mã đang xem (derivatives).
- [ ] **AC-12** Chart hỗ trợ các timeframe: **1 minute**, **5 minutes**, **15 minutes**, **30 minutes** (theo Figma).

### Buy/Sell buttons

- [ ] **AC-13** Nút **Buy** và **Sell** khi click → navigate tới màn **Trade** (với symbol hiện tại, pre-fill side Buy/ Sell).

### Realtime & Error

- [ ] **AC-14** Khi nhận WebSocket `market.quote.dr.{code}`: cập nhật quote, aggressive matched (nếu message có abv/asv hoặc mb+mv).
- [ ] **AC-15** Khi nhận WebSocket `market.bidoffer.dr.{code}`: cập nhật Bid/Ask.
- [ ] **AC-16** Xử lý lỗi load (network, 4xx/5xx): hiển thị state lỗi theo design; cho phép retry.

---

## Tasks (Implementation)

- [ ] **T1** Load initial: gọi symbolInfo (hoặc dùng cache) cho symbol; populate Basic info + Bid/Ask từ response.
- [ ] **T2** Subscribe WebSocket: `market.quote.dr.{code}`, `market.bidoffer.dr.{code}` khi vào màn; merge update vào state; unsubscribe khi rời màn.
- [ ] **T3** Bid/Ask: component dropdown 3/10 bước; slice bb/bo theo selection; default = 10 (hoặc theo design).
- [ ] **T4** Thống kê lệnh: gọi `GET /rest/api/v2/market/symbol/{symbol}/statistic`; hiển thị theo design; xử lý lỗi.
- [ ] **T5** Aggressive matched: lấy từ quote API (abv, asv); nếu không có trong symbolInfo initial, gọi `GET /rest/api/v2/market/symbol/{symbol}/quote`; cập nhật realtime từ market.quote.dr khi có.
- [ ] **T6** Tab **Matched**: reuse cơ chế equity (API/WSS matched trades) nhưng route cho symbol derivatives.
- [ ] **T7** Tab **Chart**: reuse cơ chế equity (TradingView/API chart); symbol = mã phái sinh hiện tại; timeframes: 1m, 5m, 15m, 30m.
- [ ] **T8** Buy/Sell buttons: onClick → navigate to Trade screen, truyền symbol + side (BUY/SELL).
- [ ] **T9** Error state: loading, retry, empty (nếu API trả empty) theo Figma.

---

## Data Source & APIs

| Phần | API / Nguồn | Realtime |
|------|-------------|----------|
| Basic info | symbolInfo (symbol, n1, n2, m, c, ch, ra, o, h, l, re, ce, fl, …) | `market.quote.dr.{code}` |
| Bid/Ask | symbolInfo (bb, bo – 10 bước) | `market.bidoffer.dr.{code}` |
| Thống kê lệnh | `GET /rest/api/v2/market/symbol/{symbol}/statistic` | (có thể có WebSocket `market.statistic` – xác nhận với BE) |
| Aggressive matched | `GET /rest/api/v2/market/symbol/{symbol}/quote` → abv, asv | `market.quote.dr.{code}` (nếu message chứa abv/asv) |

**Flow:**
1. Load symbolInfo (initial) → render Basic info + Bid/Ask.
2. Gọi statistic API → render Thống kê lệnh.
3. Gọi quote API (hoặc dùng symbolInfo nếu đã có abv/asv) → render Aggressive matched.
4. Subscribe WS → merge updates cho quote, bidoffer.
5. **Tab Matched:** cơ chế như equity, data cho symbol derivatives.
6. **Tab Chart:** cơ chế như equity, symbol = mã hiện tại; timeframes: 1m, 5m, 15m, 30m.
7. **Buy/Sell:** navigate → Trade (symbol, side).

---

## Technical Notes

- **symbolInfo** có thể lấy từ: `GET /api/v2/market/symbolInfo?symbolList=[symbol]` hoặc `/api/v2/market/symbol/latest` (nếu app đã load).
- **Channel Derivatives:** `market.quote.dr.{code}`, `market.bidoffer.dr.{code}` (khác equity `market.quote.{code}`, `market.bidoffer.{code}`).
- Index name (PS/DR, TPCP/GB): xem [MKT.S1 – Derivatives_Market_Display](./Derivatives_Market_Display.md).
- **FE repo:** `src/screens/CurrentPriceScreen/` – cập nhật để hỗ trợ derivatives routing (channel, API).

---

## BE Tasks bổ sung (chưa có trong Planning)

Các phần sau cần xác nhận với BE; nếu chưa có thì bổ sung task:

| # | BE Task | Mô tả | Service |
|---|---------|-------|---------|
| BE-1 | API statistic cho derivatives | `GET /rest/api/v2/market/symbol/{symbol}/statistic` phải trả đúng cho mã phái sinh (VN30F*, 41B*). Realtime-v2 cần update statistic từ `quoteUpdateDR` (tương tự equity từ quoteUpdate). | realtime-v2, market-query-v2 |
| BE-2 | API quote – abv, asv cho derivatives | `GET /rest/api/v2/market/symbol/{symbol}/quote` và symbolInfo phải trả **abv** (aggressive buy volume), **asv** (aggressive sell volume) cho derivatives – map từ Lotte (matchedBy + matchingVolume hoặc field tương đương). | realtime-v2, market-query-v2 |
| BE-3 | WebSocket statistic (optional) | Nếu cơ sở có WebSocket `market.statistic` cho realtime thống kê, derivatives cần channel tương đương hoặc include trong message hiện có. | ws-v2, realtime-v2 |

---

## References

- **Figma – Tab Quote:** [Current price Quote (node 40004829-278373)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-278373&t=7ET4YMgEP2r0vrEW-11)
- **Figma – Tab Matched:** [Current price Matched (node 40004829-287260)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-287260&t=7ET4YMgEP2r0vrEW-11)
- **Figma – Tab Chart:** [Current price Chart (node 40004829-287157)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-287157&t=7ET4YMgEP2r0vrEW-11) – timeframes: 1m, 5m, 15m, 30m
- **Planning:** [Market/Planning/01_Integration_Plan.md](../Planning/01_Integration_Plan.md) – symbolInfo, WebSocket channels
- **Chart API:** [Chart_API_Implementation.md](./Chart_API_Implementation.md) – TradingView history cho derivatives
