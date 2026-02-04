# Symbol Info API

> **Part of:** [TradeX Knowledge Base](./_index.md)  
> **Topic:** API `/api/v2/market/symbol/latest` & SymbolInfo aggregation  
> **Last Updated:** 2025-01-28

---

## Related Documents

| Document | Relationship |
|----------|--------------|
| [_index.md](./_index.md) | Parent - System overview |
| [market-data-channels.md](./market-data-channels.md) | Related - Real-time data sources |
| [init-job.md](./init-job.md) | Related - Daily init job, initial data population |

---

## Overview

`SymbolInfo` là **object trung tâm** chứa tất cả thông tin về một mã chứng khoán, được **tổng hợp từ nhiều nguồn data** và lưu trong Redis.

**Key APIs:**
- `GET /api/v2/market/symbol/latest` - Lấy thông tin giá mới nhất
- `GET /api/v2/market/symbol/staticInfo` - Lấy thông tin tĩnh của mã

---

## 1. Data Aggregation Architecture

### 1.1 Overview Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         LOTTE SECURITIES WEBSOCKET                          │
│                                                                             │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│   │ auto.qt  │  │ auto.bo  │  │ auto.ix  │  │ auto.dn  │  │  extra   │     │
│   │ (Quote)  │  │(BidOffer)│  │ (Index)  │  │  (Deal)  │  │  (ATO)   │     │
│   └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘     │
└────────│─────────────│─────────────│─────────────│─────────────│───────────┘
         │             │             │             │             │
         ▼             ▼             ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       MARKET-COLLECTOR-LOTTE                                │
│                                                                             │
│   Parse & Transform → Publish to Kafka                                      │
└─────────────────────────────────────────────────────────────────────────────┘
         │             │             │             │             │
         ▼             ▼             ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              KAFKA                                          │
│  ┌──────────┐ ┌──────────────┐ ┌──────────┐ ┌──────────────┐ ┌───────────┐  │
│  │quoteUpd. │ │bidOfferUpd.  │ │marketStat│ │dealNoticeUpd │ │extraUpdate│  │
│  └────┬─────┘ └──────┬───────┘ └────┬─────┘ └──────┬───────┘ └─────┬─────┘  │
└───────│──────────────│──────────────│──────────────│───────────────│────────┘
        │              │              │              │               │
        ▼              ▼              ▼              ▼               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          REALTIME-V2 (Java)                                 │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        SymbolInfo (In-Memory Cache)                  │   │
│  │                                                                      │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │   │
│  │  │ QuoteService │  │BidOfferServ. │  │ExtraQuoteSrv │               │   │
│  │  │ updateQuote()│  │updateBidOffer│  │updateExtra() │               │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘               │   │
│  │         │                 │                 │                        │   │
│  │         ├─────────────────┼─────────────────┤                        │   │
│  │         ▼                 ▼                 ▼                        │   │
│  │  ┌────────────────────────────────────────────────────────────────┐ │   │
│  │  │                    SYMBOL INFO OBJECT                          │ │   │
│  │  │                                                                │ │   │
│  │  │  From Quote:     │ From BidOffer:  │ From Extra:               │ │   │
│  │  │  - last, change  │ - bidOfferList  │ - expectedPrice           │ │   │
│  │  │  - rate, volume  │ - totalBid/Offer│ - expectedChange          │ │   │
│  │  │  - foreigner data│ - session       │ - expectedRate            │ │   │
│  │  │  - matchedBy     │                 │ - expectedVolume          │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│                       marketRedisDao.setSymbolInfo()                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    REDIS: realtime_mapSymbolInfo                            │
│                                                                             │
│   Key: SYMBOL_INFO                                                          │
│   Hash: { "VCB": {...}, "VNM": {...}, "VN": {...}, ... }                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                    ┌────────────────┴────────────────┐
                    ▼                                 ▼
┌───────────────────────────────┐    ┌───────────────────────────────────────┐
│      MARKET-QUERY-V2          │    │              WS-V2                     │
│         (Node.js)             │    │            (Node.js)                   │
│                               │    │                                        │
│  /api/v2/market/symbol/latest │    │  WebSocket: market.quote.{code}       │
│  /api/v2/market/symbol/static │    │             market.bidoffer.{code}    │
│                               │    │                                        │
│  Read from Redis              │    │  Read from Redis on subscribe         │
└───────────────────────────────┘    └───────────────────────────────────────┘
                    │                                 │
                    ▼                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NHSV PRO APP                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Data Sources & Update Services

| Source | Kafka Topic | Service | Updates to SymbolInfo |
|--------|-------------|---------|----------------------|
| Quote (auto.qt) | `quoteUpdate` | `QuoteService.updateQuote()` | last, change, rate, volume, foreigner |
| BidOffer (auto.bo) | `bidOfferUpdate` | `BidOfferService.updateBidOffer()` | bidOfferList, totalBid/Offer, session |
| Extra Quote | `extraUpdate` | `ExtraQuoteService.updateExtra()` | expectedPrice, expectedChange, expectedRate |
| Market Status | `marketStatus` | `MarketStatusService.update()` | sessions |
| Deal Notice | `dealNoticeUpdate` | `DealNoticeService.update()` | PT trading data |
| Static Data | startup/job | `InitService` | name, type, ceilingPrice, floorPrice |

---

## 2. SymbolInfo Data Structure

### 2.1 Full Interface (TypeScript)

```typescript
interface ISymbolInfo {
  // === IDENTIFICATION ===
  _id: string;              // Same as code
  code: string;             // Symbol code (VCB, VNM, VN...)
  type: string;             // STOCK | INDEX | FUTURES | CW | ETF
  name: string;             // Vietnamese name
  nameEn: string;           // English name
  marketType: string;       // HOSE | HNX | UPCOM | VN30F
  
  // === PRICE DATA (from Quote) ===
  open: number;             // Giá mở cửa
  high: number;             // Giá cao nhất
  low: number;              // Giá thấp nhất
  last: number;             // Giá hiện tại
  change: number;           // Thay đổi
  rate: number;             // % thay đổi
  averagePrice: number;     // Giá trung bình
  
  // === REFERENCE PRICES (static) ===
  ceilingPrice: number;     // Giá trần
  floorPrice: number;       // Giá sàn
  referencePrice: number;   // Giá tham chiếu
  
  // === VOLUME DATA (from Quote) ===
  tradingVolume: number;    // Khối lượng giao dịch
  tradingValue: number;     // Giá trị giao dịch
  matchingVolume: number;   // KL khớp lệnh cuối
  matchBy: string;          // ASK | BID (bên khớp)
  turnoverRate: number;     // Tỷ lệ quay vòng
  
  // === BID/OFFER DATA (from BidOffer) ===
  bidOfferList: IBidOfferItem[];  // Sổ lệnh 10 bước giá
  totalBidVolume: number;         // Tổng KL dư mua
  totalOfferVolume: number;       // Tổng KL dư bán
  bidPrice: number;               // Giá mua tốt nhất
  offerPrice: number;             // Giá bán tốt nhất
  sessions: string;               // Phiên: ATO|LO|ATC|PLO
  bidofferTime: string;           // Thời gian cập nhật sổ lệnh
  
  // === EXPECTED DATA (from Extra, ATO/ATC only) ===
  expectedPrice: number;    // Giá dự kiến khớp
  expectedChange: number;   // Thay đổi dự kiến
  expectedRate: number;     // % dự kiến
  expectedVolume: number;   // KL dự kiến
  
  // === FOREIGNER DATA (from Quote) ===
  foreignerBuyVolume: number;     // KL mua ĐTNN
  foreignerSellVolume: number;    // KL bán ĐTNN
  foreignerBuyValue: number;      // GT mua ĐTNN
  foreignerSellValue: number;     // GT bán ĐTNN
  foreignerTotalRoom: number;     // Tổng room NN
  foreignerCurrentRoom: number;   // Room NN còn lại
  
  // === METADATA ===
  time: string;             // Thời gian cập nhật quote
  sequence: number;         // Số thứ tự quote
  updatedAt: Date;          // Thời gian cập nhật
  listedQuantity: number;   // KL niêm yết
  
  // === INDEX specific ===
  indexType: string;        // DOMESTIC | FOREIGN
  upCount: number;          // Số mã tăng
  downCount: number;        // Số mã giảm
  ceilingCount: number;     // Số mã trần
  floorCount: number;       // Số mã sàn
  unchangedCount: number;   // Số mã đứng giá
}
```

### 2.2 BidOfferItem Structure

```typescript
interface IBidOfferItem {
  bidPrice: number;         // Giá mua
  bidVolume: number;        // KL mua
  bidVolumeChange: number;  // Thay đổi KL mua
  offerPrice: number;       // Giá bán
  offerVolume: number;      // KL bán
  offerVolumeChange: number;// Thay đổi KL bán
}
```

---

## 3. API Endpoints

### 3.1 GET `/api/v2/market/symbol/latest`

**Purpose:** Lấy thông tin giá & khối lượng mới nhất của danh sách mã.

**Request:**
```json
{
  "symbolList": ["VCB", "VNM", "FPT"]
}
```

**Flow:**
```
Client → rest-proxy → Kafka (market-query-v2) 
       → SymbolService.querySymbolLatestNormal()
       → Redis.hmget(SYMBOL_INFO, symbolList)
       → toSymbolLatestResponse() → Response
```

**Response Example:**
```json
[
  {
    "s": "VCB",                    // symbol
    "t": "STOCK",                  // type
    "ti": "103025",                // time (UTC)
    "o": 95000,                    // open
    "h": 95800,                    // high
    "l": 94500,                    // low
    "c": 95500,                    // current/last
    "ch": 500,                     // change
    "ra": 0.52,                    // rate %
    "vo": 5234100,                 // volume
    "va": 498750000000,            // value
    "mv": 1200,                    // matchingVolume
    "mb": "ASK",                   // matchedBy
    "a": 95350,                    // averagePrice
    "tb": 1250000,                 // totalBidVolume
    "to": 980000,                  // totalOfferVolume
    "tor": 0.85,                   // turnoverRate
    "ce": 102100,                  // ceilingPrice
    "fl": 88700,                   // floorPrice
    "re": 95000,                   // referencePrice
    "ss": "LO",                    // session
    "fr": {
      "bv": 150000,                // foreignerBuyVolume
      "sv": 80000,                 // foreignerSellVolume
      "cr": 1234567890,            // foreignerCurrentRoom
      "tr": 1500000000             // foreignerTotalRoom
    },
    "bb": [                        // bidOfferList - bids
      {"p": 95400, "v": 12000},
      {"p": 95300, "v": 8500},
      {"p": 95200, "v": 15000}
    ],
    "bo": [                        // bidOfferList - offers
      {"p": 95500, "v": 5000},
      {"p": 95600, "v": 9000},
      {"p": 95700, "v": 7500}
    ]
  }
]
```

### 3.2 GET `/api/v2/market/symbol/staticInfo`

**Purpose:** Lấy thông tin tĩnh của mã (không thay đổi trong ngày).

**Request:**
```json
{
  "symbolList": ["VCB", "VNM"]
}
```

**Response includes:**
- code, name, nameEn, type
- marketType, securitiesType
- ceilingPrice, floorPrice, referencePrice
- listedQuantity
- foreignerTotalRoom

---

## 4. Update Mechanisms

### 4.1 QuoteService.updateQuote()

**Trigger:** Khi nhận được `quoteUpdate` từ Kafka

**Updates:**
```java
// From QuoteService.java line 327-334
SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(symbolQuote.getCode());
if (symbolInfo != null) {
    ConvertUtils.updateByQuote(symbolInfo, symbolQuote);
    marketRedisDao.setSymbolInfo(symbolInfo);
    symbolQuote.setSequence(symbolInfo.getQuoteSequence());
}
```

**Fields updated:**
- last, change, rate
- high, low, open
- tradingVolume, tradingValue, matchingVolume
- matchedBy, averagePrice
- foreignerBuyVolume, foreignerSellVolume
- foreignerCurrentRoom
- time, sequence

### 4.2 BidOfferService.updateBidOffer()

**Trigger:** Khi nhận được `bidOfferUpdate` từ Kafka

**Updates:**
```java
// From BidOfferService.java line 37-41
SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(bidOffer.getCode());
if (symbolInfo != null) {
    ConvertUtils.updateByBidOffer(symbolInfo, bidOffer);
    marketRedisDao.setSymbolInfo(symbolInfo);
    bidOffer.setSequence(symbolInfo.getBidAskSequence());
}
```

**Fields updated:**
- bidOfferList (10 levels)
- totalBidVolume, totalOfferVolume
- session (ATO/LO/ATC/PLO)
- bidofferTime

### 4.3 ExtraQuoteService.updateExtra()

**Trigger:** Khi nhận được `extraUpdate` từ Kafka (trong phiên ATO/ATC)

**Fields updated:**
- expectedPrice
- expectedChange
- expectedRate
- expectedVolume

---

## 5. Redis Storage

### 5.1 Key Structure

```
Key: realtime_mapSymbolInfo (Hash)
Field: {symbol_code}
Value: JSON serialized SymbolInfo object
```

### 5.2 Example Redis Data

```bash
HGET realtime_mapSymbolInfo VCB
```

```json
{
  "code": "VCB",
  "type": "STOCK",
  "marketType": "HOSE",
  "name": "Ngân hàng TMCP Ngoại thương Việt Nam",
  "last": 95500,
  "change": 500,
  "rate": 0.52,
  "tradingVolume": 5234100,
  "totalBidVolume": 1250000,
  "totalOfferVolume": 980000,
  "bidOfferList": [
    {"bidPrice": 95400, "bidVolume": 12000, "offerPrice": 95500, "offerVolume": 5000},
    {"bidPrice": 95300, "bidVolume": 8500, "offerPrice": 95600, "offerVolume": 9000}
  ],
  "sessions": "LO",
  "foreignerBuyVolume": 150000,
  "foreignerSellVolume": 80000,
  "sequence": 12345,
  "updatedAt": "2025-01-28T03:30:25.123Z"
}
```

---

## 6. Business Logic Summary

### 6.1 Data Freshness

| Data Type | Update Frequency | Source |
|-----------|------------------|--------|
| Price (last, change, rate) | Every tick (~ms) | quoteUpdate |
| Volume | Every tick | quoteUpdate |
| BidOffer list | Every tick | bidOfferUpdate |
| Foreigner data | Every tick | quoteUpdate |
| Expected price | Only ATO/ATC | extraUpdate |
| Session status | Session change | marketStatus |

### 6.2 SymbolInfo vs WebSocket Channels

| Need | Use API | Use WebSocket |
|------|---------|---------------|
| Initial load nhiều mã | `/api/v2/market/symbol/latest` | - |
| Real-time 1 mã | - | `market.quote.{code}` |
| Sổ lệnh real-time | - | `market.bidoffer.{code}` |
| Snapshot + subscribe | API first, then WS | Both |

### 6.3 Typical Client Flow

```
1. App Start
   └─→ GET /api/v2/market/symbol/latest?symbolList=[watchlist]
       └─→ Receive full SymbolInfo for all symbols

2. Subscribe Real-time
   └─→ WebSocket subscribe: market.quote.VCB, market.bidoffer.VCB
       └─→ Receive incremental updates

3. Merge Updates
   └─→ Client merges WebSocket data into cached SymbolInfo
```

---

## 7. Source Code References

| Component | File | Key Methods |
|-----------|------|-------------|
| Quote Handler | `realtime-v2/.../services/QuoteService.java` | `updateQuote()` |
| BidOffer Handler | `realtime-v2/.../services/BidOfferService.java` | `updateBidOffer()` |
| Extra Handler | `realtime-v2/.../services/ExtraQuoteService.java` | `updateExtra()` |
| API Handler | `market-query-v2/.../services/SymbolService.ts` | `querySymbolLatestNormal()` |
| Request Router | `market-query-v2/.../consumers/RequestHandler.ts` | `handleRequest()` |
| Model | `market-query-v2/.../models/db/ISymbolInfo.ts` | Interface definition |
| Redis Key | `realtime-v2/.../constants/Constants.java` | `REDIS_KEY_SYMBOL_INFO` |

---

**Navigation:** [← Back to Index](./_index.md) | [Market Data Channels](./market-data-channels.md)
