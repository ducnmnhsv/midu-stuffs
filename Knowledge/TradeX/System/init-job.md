# Init Job - Lấy Giá Đầu Ngày

> **Related:** [Symbol Info API](./symbol-info-api.md) | [Market Data Channels](./market-data-channels.md) | [Index](../_index.md)

---

## Overview

Init Job là job chạy đầu ngày để:

1. **Download danh sách mã** từ Lotte API
2. **Query giá & thông tin** cho tất cả symbols
3. **Lưu vào Redis/MongoDB** để phục vụ runtime
4. **Upload `symbol_static.json`** lên MinIO/S3 cho client download

---

## Data Flow Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DAILY INIT JOB                                      │
│                     (Chạy trước giờ mở cửa ~8:30)                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MARKET-COLLECTOR-LOTTE                                   │
│                                                                             │
│   LotteApiSymbolInfoService.downloadSymbol()                                │
│                                                                             │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│   │ GET /api/   │  │ GET /api/   │  │ GET /api/   │  │ GET /api/   │       │
│   │ symbolNames │  │ indexList   │  │ symbolPrices│  │ bestBidAsks │       │
│   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘       │
│          │                │                │                │              │
│          └────────────────┴────────────────┴────────────────┘              │
│                                    │                                        │
│                                    ▼                                        │
│                    ┌──────────────────────────────┐                        │
│                    │   MERGE → List<SymbolInfo>   │                        │
│                    │   (~2000 mã: Stock, Index,   │                        │
│                    │    ETF, CW, Futures)         │                        │
│                    └──────────────┬───────────────┘                        │
└───────────────────────────────────│─────────────────────────────────────────┘
                                    │
            ┌───────────────────────┴───────────────────────┐
            ▼                                               ▼
┌───────────────────────────────┐         ┌─────────────────────────────────┐
│  Option A: Direct Init        │         │  Option B: Via Kafka (default)  │
│  (enableInitMarket=true)      │         │                                 │
│                               │         │  Topic: symbolInfoUpdate        │
│  marketInit.init()            │         │                                 │
│  ├─ Save to Redis             │         │  Message Structure:             │
│  ├─ Save to MongoDB           │         │  {                              │
│  └─ Upload to MinIO/S3        │         │    groupId: "xxx",              │
│                               │         │    command: {cleanAll: true},   │
│                               │         │    symbolInfos: [...]           │
│                               │         │  }                              │
└───────────────────────────────┘         └───────────────┬─────────────────┘
                                                          │
                                                          ▼
                               ┌─────────────────────────────────────────────┐
                               │              REALTIME-V2                    │
                               │                                             │
                               │  InitService.handleSymbolInfoUpdate()       │
                               │  ┌───────────────────────────────────────┐  │
                               │  │ 1. Collect all messages (by groupId)  │  │
                               │  │ 2. Wait timeout (60s) or all received │  │
                               │  │ 3. Pause all real-time threads        │  │
                               │  │ 4. Clean old cache (if cleanAll)      │  │
                               │  │ 5. Update cache                       │  │
                               │  │ 6. Resume threads                     │  │
                               │  │ 7. marketInit.init()                  │  │
                               │  └───────────────────────────────────────┘  │
                               └─────────────────────────────────────────────┘
                                                          │
                                                          ▼
                               ┌─────────────────────────────────────────────┐
                               │              MarketInit.init()              │
                               │                                             │
                               │  1. Save SymbolInfo → Redis (Hash)          │
                               │     Key: realtime_mapSymbolInfo             │
                               │                                             │
                               │  2. Save SymbolInfo → MongoDB               │
                               │     Collection: symbolInfo                  │
                               │                                             │
                               │  3. uploadMarketDataFile() → MinIO/S3       │
                               │     File: symbol_static.json                │
                               └─────────────────────────────────────────────┘
```

---

## Lotte APIs được gọi

| API | Endpoint | Data lấy được |
|-----|----------|---------------|
| **symbolNames** | `/api/symbolNames` | Danh sách mã, tên VN/EN, loại (stock/etf/cw...), sàn |
| **indexList** | `/api/indexList` | Danh sách Index (VN30, HNX, UPCOM...) |
| **symbolPrices** | `/api/symbolPrices` | OHLCV, change, ceiling/floor/ref, foreigner data |
| **bestBidAsks** | `/api/bestBidAsks` | Sổ lệnh 10 bước, totalBid/Offer, expectedPrice |
| **indexDaily** | `/api/indexDaily` | Giá Index, close hôm qua (referencePrice) |

---

## Data Merge Process

### Input Sources

```
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│  symbolNames    │   │  symbolPrices   │   │  bestBidAsks    │
├─────────────────┤   ├─────────────────┤   ├─────────────────┤
│ code            │   │ open            │   │ bidOfferList    │
│ vietnameseName  │   │ high            │   │ totalBidVolume  │
│ englishName     │   │ low             │   │ totalOfferVolume│
│ type            │   │ last            │   │ expectedPrice   │
│ exchange        │   │ change          │   │ controlCode     │
└─────────────────┘   │ changeRate      │   │ matchVolume     │
                      │ tradingVolume   │   └─────────────────┘
                      │ ceiling/floor   │
                      │ refPrice        │
                      │ foreignBuyVol   │
                      │ foreignSellVol  │
                      │ foreignRoom     │
                      │ high52/low52    │
                      └─────────────────┘
```

### Output: SymbolInfo

```java
// Merge logic in LotteApiSymbolInfoService.merge()
SymbolInfo symbolInfo = new SymbolInfo();

// From symbolNames
symbolInfo.setCode(symbolName.getCode());
symbolInfo.setName(symbolName.getVietnameseName());
symbolInfo.setNameEn(symbolName.getEnglishName());
symbolInfo.setType(convertType(symbolName.getType()));  // STOCK, ETF, CW, INDEX...
symbolInfo.setMarketType(symbolName.getExchange());     // HOSE, HNX, UPCOM

// From symbolPrices
symbolInfo.setOpen(symbolPrice.getOpen());
symbolInfo.setHigh(symbolPrice.getHigh());
symbolInfo.setLow(symbolPrice.getLow());
symbolInfo.setLast(symbolPrice.getLast());
symbolInfo.setChange(symbolPrice.getChange());
symbolInfo.setRate(symbolPrice.getChangeRate());
symbolInfo.setTradingVolume(symbolPrice.getTradingVolume());
symbolInfo.setCeilingPrice(symbolPrice.getCeiling());
symbolInfo.setFloorPrice(symbolPrice.getFloor());
symbolInfo.setReferencePrice(symbolPrice.getRefPrice());
symbolInfo.setForeignerBuyVolume(symbolPrice.getForeignBuyVol());
symbolInfo.setForeignerSellVolume(symbolPrice.getForeignSellVol());
symbolInfo.setForeignerTotalRoom(symbolPrice.getForeignTotalRoom());
symbolInfo.setForeignerCurrentRoom(symbolPrice.getForeignCurrRoom());

// From bestBidAsks
symbolInfo.setBidOfferList(parseBidOfferList(bidAsk));
symbolInfo.setTotalBidVolume(bidAsk.getTotalBidVolume());
symbolInfo.setTotalOfferVolume(bidAsk.getTotalOfferVolume());
symbolInfo.setExpectedPrice(bidAsk.getExpectedPrice());
symbolInfo.setSessions(bidAsk.getControlCode());
```

---

## Symbol Types

| Type | Enum | Examples | Count (approx) |
|------|------|----------|----------------|
| **Stock** | `STOCK` | VCB, VNM, FPT | ~1700 |
| **Index** | `INDEX` | VNINDEX, VN30, HNX | ~15 |
| **ETF** | `ETF` | E1VFVN30, FUEVFVND | ~20 |
| **Covered Warrant** | `CW` | CVNM2201, CFPT2301 | ~200 |
| **Futures** | `FUTURES` | VN30F2401, VN30F2402 | ~4 |
| **Bond** | `BOND` | (disabled by default) | - |

---

## Storage Destinations

### 1. Redis (Runtime Cache)

```
Key: realtime_mapSymbolInfo
Type: Hash
Fields: { "VCB": {...}, "VNM": {...}, ... }
```

**Usage:** Phục vụ real-time queries, update từ market data

### 2. MongoDB (Persistent Backup)

```
Collection: symbolInfo
Documents: { _id: "VCB", code: "VCB", name: "...", ... }
```

**Usage:** Backup, recovery khi Redis mất data

### 3. MinIO/S3 (File Storage)

```yaml
Bucket: market
File: symbol_static.json
Path: market/symbol_static.json
```

**Usage:** Client download khi app start

---

## File: symbol_static.json

### Config

```yaml
# application.yaml
app:
  marketConf:
    symbolStaticBucket: market
    symbolStaticFile: symbol_static.json
    fileConfig:
      defaultType: MINIO
      minio:
        baseUrl: https://file.nhsv-dev.tradex.vn
```

### Content Structure

```json
[
  {
    "code": "VCB",
    "name": "Ngân hàng TMCP Ngoại thương Việt Nam",
    "nameEn": "Vietcombank",
    "type": "STOCK",
    "marketType": "HOSE",
    "exchange": "HOSE",
    "ceilingPrice": 102100,
    "floorPrice": 88700,
    "referencePrice": 95400,
    "last": 95500,
    "open": 95200,
    "high": 95800,
    "low": 94900,
    "change": 100,
    "rate": 0.1048,
    "tradingVolume": 0,
    "tradingValue": 0,
    "foreignerTotalRoom": 1500000000,
    "foreignerCurrentRoom": 1400000000,
    "foreignerBuyVolume": 0,
    "foreignerSellVolume": 0,
    "listedQuantity": 4729432290,
    "bidOfferList": [
      {"bidPrice": 95400, "bidVolume": 12000, "offerPrice": 95500, "offerVolume": 5000},
      {"bidPrice": 95300, "bidVolume": 8500, "offerPrice": 95600, "offerVolume": 9000}
    ],
    "totalBidVolume": 150000,
    "totalOfferVolume": 120000,
    "highLowYearData": [
      {"highPrice": 105000, "lowPrice": 78000}
    ]
  },
  // ... ~2000 symbols
]
```

### Download URL

```
Production: https://file.nhsvpro.nhsv.vn/market/symbol_static.json
UAT:        https://file.tnhsvpro.nhsv.vn/market/symbol_static.json
```

---

## Trigger Conditions

| Trigger | Service | Method | Khi nào |
|---------|---------|--------|---------|
| **Scheduled Job** | market-collector-lotte | Cron schedule | 8:30 AM hàng ngày |
| **Manual API** | market-collector-lotte | `downloadSymbolFromRequest()` | Gọi API trigger |
| **Startup** | realtime-v2 | `StartupService` | Khi service start (if configured) |

### Kafka API Trigger

```
Topic: market-collector-lotte
Message: { "uri": "downloadSymbol" }
```

---

## Process Flow (realtime-v2)

```java
// InitService.java
public void handleSymbolInfoUpdate(SymbolInfoUpdate request) {
    // 1. Group messages by groupId
    groupMap.computeIfAbsent(request.getCommand().getGroupId(), k -> new GroupCommand());
    
    // 2. Add to group
    groupMap.get(groupId).symbolInfoUpdates.add(request);
    groupMap.get(groupId).totalReceived += 1;
}

// Timer runs every 10s
@Override
public void run() {
    // 3. Check if group is complete
    if (gr.totalReceived >= gr.totalMessages || timeout >= 60s) {
        
        // 4. Pause all real-time threads
        monitorService.pauseAll(() -> {
            
            // 5. Clean old cache if needed
            if (command.getCleanAll()) {
                cacheService.getMapSymbolInfo().clear();
            }
            
            // 6. Update cache with new data
            gr.symbolInfoUpdates.forEach(it -> {
                symbolInfoService.updateBySymbolInfoUpdate(it, false);
            });
            
            // 7. Resume threads
            monitorService.resumeAll();
            
            // 8. Run init
            marketInit.init(cacheService.getMapSymbolInfo().values());
        });
    }
}
```

---

## Holiday Handling

```java
// LotteApiSymbolInfoService.downloadSymbol()
if (holidayService.isHoliday()) {
    log.info("TODAY IS HOLIDAY OR WEEKEND - END downloadSymbol");
    return CompletableFuture.completedFuture(null);
}
```

Init Job sẽ **không chạy** vào:
- Thứ 7, Chủ nhật
- Ngày lễ (configured trong database/config)

---

## Error Handling

| Error | Behavior |
|-------|----------|
| **API timeout** | Retry up to 5 times với 15s delay |
| **Not enough symbols** | Throw error, abort init (threshold: `initThresholdSize`) |
| **Kafka send fail** | Log error, continue |
| **Redis/Mongo fail** | Throw error, abort |

### Threshold Check

```java
if (allSymbols.size() < appConf.getInitThresholdSize()) {
    throw new IllegalStateException("Not enough symbols: " + allSymbols.size() 
        + " while need at least: " + appConf.getInitThresholdSize());
}
```

---

## Configuration

```yaml
# market-collector-lotte application.yaml
app:
  # API connection
  apiConnection:
    baseUrl: https://tnhsvpro.nhsv.vn/lotte/
    symbolNames: api/v2/symbols/names
    symbolPrices: api/v2/symbols/prices
    bestBidAsks: api/v2/symbols/bestBidAsks
    indexListApi: api/v2/indexes/list
    indexDaily: api/v2/indexes/daily
  
  # Init settings
  initThresholdSize: 1500
  enableInitMarket: false      # true = direct init, false = via Kafka
  enableMultipleInstance: true # coordinator lock
  
  # Kafka topics
  topics:
    symbolInfoUpdate: symbolInfoUpdate
  
  # Market file config
  marketConf:
    symbolStaticBucket: market
    symbolStaticFile: symbol_static.json
```

---

## Nhận biết danh sách mã cổ phiếu thuộc VN30 (và index khác)

Init job **market-collector-lotte** chỉ download **symbolNames** (tất cả mã + type, exchange), **không** có bước “lọc mã thuộc VN30” — vì Lotte symbolNames không trả về quan hệ symbol ↔ index.

**Nguồn danh sách mã VN30** là job riêng trên **lotte-bridge**:

| Bước | Service | Mô tả |
|------|---------|--------|
| 1 | **lotte-bridge** | Cron `0,15,30,55 1 * * MON-FRI` (1:00, 1:15, 1:30, 1:55) chạy `IndexService.getIndexList()` |
| 2 | Lotte API | `getIndexList` (mkt_tp=ALL) → danh sách index (symbol/code/exchange: VN30, HNX30, …) |
| 3 | Lotte API | Với mỗi index: `getIndexStockList(mkt_tp=exchange, idx=code)` → danh sách mã cổ phiếu thành phần |
| 4 | Kafka | Gửi topic `indexStockListUpdate`, payload `{ indexCode: "VN30", stockList: ["VCB","VNM",...] }` |
| 5 | **realtime-v2** | `IndexStockListUpdateHandler` consume → `IndexStockService.updateIndexList()` → lưu DB (IndexStockListRepository) |
| 6 | Khi init / cache | **realtime-v2** `CacheService` (sau khi xử lý symbolInfoUpdate) đọc `indexStockListRepository.findById("VN30")` → `getStockList()` để có set mã VN30 (vd: setVn30Trade, ranking…) |

**Kết luận:**

- **Trong init job (market-collector-lotte):** Không có bước “nhận biết danh sách VN30”. Chỉ có download symbolNames + indexList (tên index) + symbolPrices + bestBidAsks.
- **Để có danh sách mã VN30:** Phải dựa vào dữ liệu đã được **lotte-bridge** đồng bộ trước đó (job 1h sáng) qua Kafka → realtime-v2 lưu DB. Khi init/cache, **realtime-v2** đọc từ `IndexStockListRepository` (DB) với `indexCode = "VN30"`.
- **API cho client:** `GET /api/v2/market/indexStockList/{indexCode}` (market-query-v2) đọc từ MongoDB collection `c_index_stock_list` (cùng nguồn do realtime-v2 ghi).

**Thứ tự thực tế:** Job index list (1h sáng) chạy **trước** init job (~8:30) → khi init chạy, DB đã có sẵn VN30/HNX30.

---

## Related Topics

- **[Symbol Info API](./symbol-info-api.md)**: Cách client query symbol info sau khi init
- **[Market Data Channels](./market-data-channels.md)**: Cách giá được update real-time sau init
- **Daily Recovery**: Job recover data nếu Init Job fail

---

## Source Code Reference

| File | Description |
|------|-------------|
| `market-collector-lotte/.../LotteApiSymbolInfoService.java` | Main init service |
| `market-collector-lotte/.../LotteApiService.java` | Lotte API client |
| `realtime-v2/.../InitService.java` | Kafka consumer, init orchestration |
| `realtime-v2/.../SymbolInfoService.java` | Upload file logic |
| `tradex-common-java/.../MarketInit.java` | Core init logic (Redis, Mongo, S3) |

---

*Document created: 2025-01-28*
