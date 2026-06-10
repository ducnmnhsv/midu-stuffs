# Market Data Channels

> **Part of:** [TradeX Knowledge Base](./_index.md)  
> **Topic:** Real-time Market Data via WebSocket  
> **Last Updated:** 2025-01-28

---

## Related Documents

| Document | Relationship |
|----------|--------------|
| [_index.md](./_index.md) | Parent - System overview |
| [symbol-info-api.md](./symbol-info-api.md) | Related - API uses same data sources |
| [init-job.md](./init-job.md) | Related - Daily init populates initial data |
| *(coming soon)* order-flow.md | Related - Order affects market data |

---

## Overview

Tài liệu này mô tả cách market data được truyền từ Lotte Securities đến NHSV Pro App thông qua WebSocket channels.

**Key Channels:**
- `market.quote.{symbol}` - Giá, khối lượng, ĐTNN
- `market.bidoffer.{symbol}` - Sổ lệnh (order book)

---

## 1. Data Flow Architecture

### 1.1 Pipeline Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATA FLOW PIPELINE                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Lotte WS ──► market-collector-lotte ──► Kafka ──► realtime-v2        │
│                      (Java)               │          (Java)             │
│                                           │                             │
│                                           └──► ws-v2 ──► Mobile App     │
│                                                (Node.js)                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Data Source

| Item | Detail |
|------|--------|
| **Provider** | Lotte Securities |
| **Protocol** | WebSocket |
| **Server** | `172.33.30.23:9900` |
| **Format** | Pipe-delimited text messages |

### 1.3 Services Involved

| Service | Tech | Role |
|---------|------|------|
| `market-collector-lotte` | Java | Thu thập & parse data từ Lotte |
| `realtime-v2` | Java | Xử lý business logic, lưu Redis |
| `ws-v2` | Node.js | Publish data đến client qua WebSocket |

### 1.4 Kafka Topics

| Topic | Content | Consumer |
|-------|---------|----------|
| `quoteUpdate` | Thông tin giá, KL giao dịch | realtime-v2, ws-v2 |
| `bidOfferUpdate` | Dữ liệu sổ lệnh (bid/ask) | realtime-v2, ws-v2 |
| `marketStatus` | Trạng thái phiên giao dịch | realtime-v2, ws-v2 |

---

## 2. Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    LOTTE SECURITIES WEBSOCKET                               │
│                       (172.33.30.23:9900)                                   │
│                                                                             │
│   ┌──────────────────────┐      ┌──────────────────────┐                    │
│   │ Channel: auto.qt     │      │ Channel: auto.bo     │                    │
│   │ (Quote/Price data)   │      │ (Bid/Offer data)     │                    │
│   │ Pipe-delimited       │      │ Pipe-delimited       │                    │
│   └──────────┬───────────┘      └──────────┬───────────┘                    │
└──────────────│──────────────────────────────│───────────────────────────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MARKET-COLLECTOR-LOTTE (Java)                            │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │ WsConnection.java                                                       │ │
│  │ ├── handleStockQuote()   → QuoteUpdate object                          │ │
│  │ └── handleStockBidAsk()  → BidOfferUpdate object                       │ │
│  │                                                                         │ │
│  │ Transformations:                                                        │ │
│  │ • Parse pipe-delimited → Java objects                                   │ │
│  │ • Convert time: Vietnam TZ → UTC                                        │ │
│  │ • Map codes: Lotte codes → TradeX codes                                 │ │
│  │ • Map session: Control codes → Session types                            │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  Output: Publish to Kafka topics                                            │
└─────────────────────────────────────────────────────────────────────────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              KAFKA                                          │
│        ┌─────────────┐              ┌─────────────────┐                     │
│        │ quoteUpdate │              │ bidOfferUpdate  │                     │
│        └──────┬──────┘              └────────┬────────┘                     │
└───────────────│──────────────────────────────│──────────────────────────────┘
                │                              │
         ┌──────┴──────┐                ┌──────┴──────┐
         ▼             ▼                ▼             ▼
┌────────────────┐  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│  realtime-v2   │  │    ws-v2       │  │  realtime-v2   │  │    ws-v2       │
│    (Java)      │  │   (Node.js)    │  │    (Java)      │  │   (Node.js)    │
├────────────────┤  ├────────────────┤  ├────────────────┤  ├────────────────┤
│ • Update Redis │  │ • Compress     │  │ • Update Redis │  │ • Compress     │
│ • Calculate    │  │   field names  │  │ • Update cache │  │   field names  │
│   statistics   │  │ • Publish to   │  │ • Set sequence │  │ • Publish to   │
│ • Handle order │  │   WebSocket    │  │   numbers      │  │   WebSocket    │
└────────────────┘  └───────┬────────┘  └────────────────┘  └───────┬────────┘
                            │                                       │
                            ▼                                       ▼
                   ┌─────────────────────────────────────────────────────────┐
                   │                 WebSocket Channels                      │
                   │  ┌───────────────────┐    ┌────────────────────────┐    │
                   │  │ market.quote.{s}  │    │ market.bidoffer.{s}    │    │
                   │  │ (e.g. VCB, VNM)   │    │ (e.g. VCB, VNM)        │    │
                   │  └───────────────────┘    └────────────────────────┘    │
                   └─────────────────────────────────────────────────────────┘
                                           │
                                           ▼
                              ┌─────────────────────────┐
                              │      NHSV Pro App       │
                              │  (iOS / Android / Web)  │
                              └─────────────────────────┘
```

---

## 3. Channel: market.quote

### 3.1 Purpose

Cung cấp thông tin **giá và khối lượng giao dịch** của mã chứng khoán.

### 3.2 Lotte Source

| Item | Value |
|------|-------|
| Channel | `auto.qt` (auto quote) |
| Subscription | `sub/pro.pub.auto.qt./` |

### 3.3 Message Example

```json
{
  "s": "VIX",
  "t": "STOCK",
  "ti": "032857",
  "o": 22950,
  "h": 22950,
  "l": 22350,
  "c": 22650,
  "ch": -100,
  "ra": -0.4396,
  "vo": 9981400,
  "va": 225777695000,
  "mv": 200,
  "a": 22600,
  "mb": "ASK",
  "tb": 6026200,
  "to": 3955200,
  "tor": 0.6518,
  "fr": {
    "bv": 913300,
    "sv": 468400,
    "cr": 1399642674,
    "tr": 1531429858
  }
}
```

### 3.4 Field Reference

| Field | Full Name | Type | Description |
|-------|-----------|------|-------------|
| `s` | symbol | string | Mã chứng khoán |
| `t` | type | string | Loại: STOCK / INDEX / FUTURES |
| `ti` | time | string | Thời gian (UTC, format: HHmmss) |
| `o` | open | number | Giá mở cửa |
| `h` | high | number | Giá cao nhất |
| `l` | low | number | Giá thấp nhất |
| `c` | current/last | number | Giá hiện tại |
| `ch` | change | number | Thay đổi giá (so với tham chiếu) |
| `ra` | rate | number | % thay đổi |
| `vo` | volume | number | Khối lượng giao dịch |
| `va` | value | number | Giá trị giao dịch (VND) |
| `mv` | matchingVolume | number | KL khớp lệnh cuối |
| `a` | averagePrice | number | Giá trung bình |
| `mb` | matchedBy | string | Bên chủ động khớp: ASK / BID |
| `tb` | totalBidVolume | number | Tổng KL dư mua |
| `to` | totalOfferVolume | number | Tổng KL dư bán |
| `tor` | turnoverRate | number | Tỷ lệ quay vòng |
| `fr.bv` | foreignerBuyVolume | number | KL mua ĐTNN |
| `fr.sv` | foreignerSellVolume | number | KL bán ĐTNN |
| `fr.cr` | foreignerCurrentRoom | number | Room NN còn lại |
| `fr.tr` | foreignerTotalRoom | number | Tổng room NN |

### 3.5 Lotte Raw Data Mapping

| Lotte Index | TradeX Field | Notes |
|-------------|--------------|-------|
| `parts[2]` | time | Convert VN → UTC |
| `parts[3]` | code | Via codeMapping |
| `parts[6]` | open | |
| `parts[8]` | high | |
| `parts[10]` | low | |
| `parts[12]` | last | |
| `parts[14]` | change | |
| `parts[16]` | rate | |
| `parts[17]` | turnoverRate | |
| `parts[18]` | averagePrice | |
| `parts[21]` | tradingValue | |
| `parts[22]` | tradingVolume | |
| `parts[23]` | matchingVolume | |
| `parts[24]` | matchedBy | `"83"` → ASK, `"66"` → BID |
| `parts[25]` | foreignerBuyVolume | |
| `parts[26]` | foreignerSellVolume | |
| `parts[28]` | foreignerCurrentRoom | |

---

## 4. Channel: market.bidoffer

### 4.1 Purpose

Cung cấp **sổ lệnh (order book)** với các mức giá bid/ask và khối lượng tương ứng.

### 4.2 Lotte Source

| Item | Value |
|------|-------|
| Channel | `auto.bo` (auto bid-offer) |
| Subscription | `sub/pro.pub.auto.bo./` |

### 4.3 Message Example

```json
{
  "s": "VCB",
  "t": "STOCK",
  "ti": "032857",
  "bot": "032857",
  "ss": "LO",
  "tb": 6026200,
  "to": 3955200,
  "bb": [
    {"p": 95500, "v": 12300, "c": 200},
    {"p": 95400, "v": 8500, "c": -100},
    {"p": 95300, "v": 15200, "c": 500}
  ],
  "bo": [
    {"p": 95600, "v": 5600, "c": 100},
    {"p": 95700, "v": 9800, "c": -200},
    {"p": 95800, "v": 7400, "c": 300}
  ],
  "ep": 95550,
  "exv": 125000,
  "exc": 50,
  "exr": 0.05
}
```

### 4.4 Field Reference

#### Root Fields

| Field | Full Name | Type | Description |
|-------|-----------|------|-------------|
| `s` | symbol | string | Mã chứng khoán |
| `t` | type | string | Loại: STOCK / INDEX / FUTURES |
| `ti` | time | string | Thời gian cập nhật (UTC) |
| `bot` | bidOfferTime | string | Thời gian sổ lệnh (duplicate) |
| `ss` | session | string | Loại phiên giao dịch |
| `tb` | totalBidVolume | number | Tổng KL dư mua |
| `to` | totalOfferVolume | number | Tổng KL dư bán |
| `ep` | expectedPrice | number | Giá dự kiến khớp (ATO/ATC) |
| `exv` | expectedVolume | number | KL dự kiến khớp |
| `exc` | expectedChange | number | Thay đổi giá dự kiến |
| `exr` | expectedRate | number | % thay đổi dự kiến |

#### Bid Array (`bb`) - Bên Mua

| Field | Full Name | Type | Description |
|-------|-----------|------|-------------|
| `p` | price | number | Giá mua |
| `v` | volume | number | Khối lượng mua |
| `c` | change | number | Thay đổi KL so với cập nhật trước |

#### Offer Array (`bo`) - Bên Bán

| Field | Full Name | Type | Description |
|-------|-----------|------|-------------|
| `p` | price | number | Giá bán |
| `v` | volume | number | Khối lượng bán |
| `c` | change | number | Thay đổi KL so với cập nhật trước |

### 4.5 Lotte Raw Data Mapping

| Lotte Index | TradeX Field | Notes |
|-------------|--------------|-------|
| `parts[2]` | time | Convert VN → UTC |
| `parts[3]` | code | Via codeMapping |
| `parts[4]` | controlCode | → session type |
| `parts[5]` | expectedPrice | |
| `parts[13 + i*6]` | bidPrice[i] | 10 levels |
| `parts[15 + i*6]` | bidVolume[i] | |
| `parts[16 + i*6]` | offerPrice[i] | |
| `parts[18 + i*6]` | offerVolume[i] | |
| `parts[73]` | totalBidVolume | |
| `parts[74]` | totalOfferVolume | |

---

## 5. Session Types

### 5.1 Overview

Hệ thống xác định loại phiên giao dịch dựa trên **control code** từ Lotte.

> **See also:** [Trading Sessions](./_index.md#trading-sessions-hose) trong _index.md

### 5.2 Control Code Mapping

#### HOSE (Sàn TP.HCM)

| Control Code | Session | Thời gian | Mô tả |
|--------------|---------|-----------|-------|
| `P` | ATO | 09:00-09:15 | Khớp lệnh mở cửa |
| `O`, `R` | LO | 09:15-11:30, 13:00-14:30 | Liên tục |
| `I` | INTERMISSION | 11:30-13:00 | Nghỉ trưa |
| `A` | ATC | 14:30-14:45 | Khớp lệnh đóng cửa |
| `C` | PLO | 14:45-15:00 | Post Limit Order |
| `K`, `G` | CLOSED | Sau 15:00 | Đóng cửa |

#### HNX / UPCOM

| Control Code | Session | Mô tả |
|--------------|---------|-------|
| `P`, `O` | LO | Liên tục |
| `2` | INTERMISSION | Nghỉ trưa |
| `A` | ATC | Khớp lệnh đóng cửa |
| `C` | PLO | Post Limit Order |
| `13`, `97` | CLOSED | Đóng cửa |

### 5.3 Session Impact on Data

| Session | Quote Data | BidOffer Data | Expected Fields |
|---------|------------|---------------|-----------------|
| ATO | ✅ Full | ✅ Full | ✅ `ep`, `exv`, `exc`, `exr` |
| LO | ✅ Full | ✅ Full | ❌ Null |
| ATC | ✅ Full | ✅ Full | ✅ `ep`, `exv`, `exc`, `exr` |
| PLO | ✅ Limited | ✅ Limited | ❌ Null |
| CLOSED | ❌ No updates | ❌ No updates | ❌ Null |

---

## 6. Field Abbreviation Reference

### 6.1 Quick Lookup Table

| Abbr | Full Name | Vietnamese |
|------|-----------|------------|
| `s` | symbol | Mã CK |
| `t` | type | Loại |
| `ti` | time | Thời gian |
| `o` | open | Giá mở cửa |
| `h` | high | Giá cao nhất |
| `l` | low | Giá thấp nhất |
| `c` | current/close | Giá hiện tại |
| `ch` | change | Thay đổi |
| `ra` | rate | Tỷ lệ % |
| `vo` | volume | Khối lượng |
| `va` | value | Giá trị |
| `mv` | matchingVolume | KL khớp cuối |
| `a` | averagePrice | Giá TB |
| `mb` | matchedBy | Bên khớp |
| `tb` | totalBidVolume | Tổng KL mua |
| `to` | totalOfferVolume | Tổng KL bán |
| `tor` | turnoverRate | Tỷ lệ quay vòng |
| `ss` | session | Phiên GD |
| `bb` | bestBids | Giá mua tốt nhất |
| `bo` | bestOffers | Giá bán tốt nhất |
| `p` | price | Giá |
| `v` | volume | Khối lượng |
| `ep` | expectedPrice | Giá dự kiến |
| `exv` | expectedVolume | KL dự kiến |
| `exc` | expectedChange | Thay đổi dự kiến |
| `exr` | expectedRate | % dự kiến |
| `fr` | foreigner | ĐTNN |
| `bv` | buyVolume | KL mua |
| `sv` | sellVolume | KL bán |
| `cr` | currentRoom | Room còn lại |
| `tr` | totalRoom | Tổng room |

---

## 7. Business Interpretation

### 7.1 Analyzing Quote Message

```json
{"s":"VIX","c":22650,"ch":-100,"ra":-0.4396,"mb":"ASK","tb":6026200,"to":3955200}
```

| Observation | Analysis |
|-------------|----------|
| `ch: -100`, `ra: -0.4396` | VIX giảm 100đ (-0.44%) so với tham chiếu |
| `mb: "ASK"` | Lệnh cuối khớp bên bán = Người mua chủ động mua giá cao |
| `tb > to` | 6M > 3.9M = Áp lực mua > Áp lực bán = Xu hướng tích cực |

### 7.2 Analyzing BidOffer Message

```json
{"ss":"ATO","ep":95550,"exv":125000,"bb":[{"p":95500,"v":12300}],"bo":[{"p":95600,"v":5600}]}
```

| Observation | Analysis |
|-------------|----------|
| `ss: "ATO"` | Đang trong phiên khớp lệnh mở cửa |
| `ep: 95550` | Giá dự kiến khớp ATO là 95,550đ |
| `exv: 125000` | Khối lượng dự kiến khớp là 125,000 CP |
| `bb[0].v > bo[0].v` | 12,300 > 5,600 = Nhiều người muốn mua hơn bán |

### 7.3 Common Use Cases for PM

| Question | Data Source | Fields to Check |
|----------|-------------|-----------------|
| Giá hiện tại của VCB? | market.quote.VCB | `c` (current) |
| VCB tăng hay giảm? | market.quote.VCB | `ch`, `ra` |
| Ai đang mua nhiều hơn? | market.quote.VCB | `tb` vs `to`, `mb` |
| ĐTNN đang mua hay bán ròng? | market.quote.VCB | `fr.bv` vs `fr.sv` |
| Giá tốt nhất để mua VCB? | market.bidoffer.VCB | `bo[0].p` (ask price 1) |
| Giá tốt nhất để bán VCB? | market.bidoffer.VCB | `bb[0].p` (bid price 1) |
| Độ sâu thanh khoản? | market.bidoffer.VCB | `bb[]` và `bo[]` arrays |
| Đang ở phiên nào? | market.bidoffer.VCB | `ss` (session) |

---

## Appendix: Source Code References

| Component | File Path | Key Functions |
|-----------|-----------|---------------|
| Lotte Collector | `market-collector-lotte-main/.../ws/WsConnection.java` | `handleStockQuote()`, `handleStockBidAsk()` |
| Kafka Publisher | `market-collector-lotte-main/.../ws/WsConnectionThread.java` | `sendMessageSafe()` |
| Quote Processor | `realtime-v2-main/.../consumers/QuoteUpdateHandler.java` | `handle()` |
| BidOffer Processor | `realtime-v2-main/.../consumers/BidOfferUpdateHandler.java` | `handle()` |
| WS Publisher | `ws-v2-main/market.js` | `processDataPublishV2()` |
| Field Compressor | `ws-v2-main/parser.js` | `convertDataPublishV2Quote()`, `convertDataPublishV2BidOffer()` |
| Topic Mapping | `ws-v2-main/constants.js` | `mapTopicToPublishV2` |

---

**Navigation:** [← Back to Index](./_index.md)
