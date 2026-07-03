# Chart API Requirements - Derivatives Integration

> **Document ID:** DER-REQ-CHART-001  
> **Version:** 1.0  
> **Created:** 2025-01-28  
> **Module:** Market Data - Chart  
> **Related:** [02_BE_REQUIREMENTS_SPEC.md](./02_BE_REQUIREMENTS_SPEC.md) | [00_EXECUTIVE_SUMMARY.md](./00_EXECUTIVE_SUMMARY.md)

---

## Overview

FE hiện tại sử dụng API `/tradingview/history` để lấy dữ liệu chart (candlestick) cho TradingView library. API này cần được mở rộng để hỗ trợ dữ liệu phái sinh (Futures).

**Key Points:**
- FE KHÔNG cần thay đổi cách gọi API
- BE cần bổ sung data source và storage cho phái sinh
- Giữ nguyên format response của TradingView

---

## 1. Current Implementation Analysis

### 1.1 API Endpoint

```
GET /tradingview/history
```

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `symbol` | string | Yes | Mã chứng khoán (VCB, VN30F2501) |
| `from` | integer | Yes | Unix timestamp (UTC) - leftmost bar |
| `to` | integer | Yes | Unix timestamp (UTC) - rightmost bar |
| `resolution` | string | Yes | D, W, M, 1, 5, 15, 30, 60 |
| `isAdjusted` | boolean | No | Giá đã điều chỉnh hay chưa |
| `countback` | integer | No | Số bars cần lấy |

### 1.2 Current Data Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     CURRENT FLOW (EQUITY ONLY)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Client → GET /tradingview/history?symbol=VCB&resolution=D&from=...&to=... │
│                                                                             │
│       ↓                                                                     │
│                                                                             │
│  rest-proxy → Kafka → market-query-v2                                       │
│                                                                             │
│       ↓                                                                     │
│                                                                             │
│  FeedService.queryTradingViewHistory()                                      │
│       │                                                                     │
│       ├── Resolution = D/W/M → getDailyPeriodHistory()                     │
│       │       └── Query MongoDB: symbolDaily collection                     │
│       │       └── Query Redis: SYMBOL_DAILY (today data)                   │
│       │                                                                     │
│       └── Resolution = 1/5/15/30/60 → getQuoteMinuteHistory()              │
│               └── Query Redis: SYMBOL_QUOTE_MINUTE_* (today)               │
│               └── Query MongoDB: symbolQuoteMinutes (history)              │
│                                                                             │
│       ↓                                                                     │
│                                                                             │
│  Response: { s: "ok", t: [...], c: [...], o: [...], h: [...], l: [...] }   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.3 Current Data Sources

| Resolution | Data Source | Collection/Key |
|------------|-------------|----------------|
| D (Daily) | MongoDB + Redis | `symbolDaily` + `SYMBOL_DAILY` |
| W (Weekly) | MongoDB (computed) | `symbolDaily` (grouped) |
| M (Monthly) | MongoDB (computed) | `symbolDaily` (grouped) |
| 1, 5, 15, 30, 60 | Redis + MongoDB | `SYMBOL_QUOTE_MINUTE_*` + `symbolQuoteMinutes` |

### 1.4 Current Service Code

```typescript
// FeedService.ts
public async queryTradingViewHistory(request: ITradingViewHistoryRequest): Promise<TradingViewHistoryResponse> {
    validateRequest(request, tradingViewHistoryRequestValidator);
    
    if (RESOLUTION_MINUTE.includes(request.resolution)) {
        return this.getQuoteMinuteHistory(request);  // 1, 5, 15, 30, 60
    } else {
        return this.getDailyPeriodHistory(request);  // D, W, M
    }
}
```

---

## 2. Derivatives Data Source

### 2.1 Lotte API: DRMKT-003 - Future Quote By Minute

**Endpoint:** `POST /tuxsvc/market/dr/minutely-derivatives`

**Request:**

```json
{
    "code": "VN30F2501",
    "time_unit": "0",       // 0=1min, 1=5min, 2=10min, 3=30min
    "base_time": "30000101",
    "next_time": "235959"
}
```

**Response:**

```json
{
    "error_code": "0000",
    "success": true,
    "data_list": {
        "has_next": true,
        "next_key": "20250128093000",
        "list_items": [
            {
                "time": "093000",       // HHmmss
                "last": 1285.5,
                "change": 12.5,
                "open": 1280.0,
                "high": 1290.0,
                "low": 1278.0,
                "volume": 1250,
                "value": 160812500,
                "change_rate": 0.98
            }
        ]
    }
}
```

### 2.2 Data Availability

| Resolution | Equity Source | Derivatives Source |
|------------|---------------|-------------------|
| **D (Daily)** | VietStock API + VNDirect crawl | **DRMKT-002** (stock-price) history |
| **W/M** | Computed from Daily | Computed from Daily |
| **1 min** | WebSocket → Redis/Mongo | **DRMKT-003** (minutely-derivatives) |
| **5 min** | Computed from 1 min | **DRMKT-003** (time_unit=1) |
| **10 min** | ❌ Not supported | **DRMKT-003** (time_unit=2) |
| **30 min** | Computed from 1 min | **DRMKT-003** (time_unit=3) |

---

## 3. Requirements Specification

### REQ-CHART-001: Daily Chart Data Collection

**Service:** `market-collector-lotte`

**Task:** Download và lưu dữ liệu daily cho phái sinh

**Approach:**
1. Trong Init Job, sau khi download SymbolInfo, lấy thêm daily history
2. Hoặc tạo scheduled job riêng để crawl daily data

**Implementation:**

```java
@Service
@Slf4j
public class DerivativeDailyService {
    
    @Autowired
    private LotteDrApiService drApiService;
    
    @Autowired
    private SymbolDailyRepository symbolDailyRepository;
    
    /**
     * Download daily OHLCV data cho derivatives
     * Gọi sau khi Init Job hoàn thành
     */
    public CompletableFuture<Void> downloadDerivativeDailyHistory(List<String> derivativeCodes) {
        for (String code : derivativeCodes) {
            try {
                // Lotte DR không có API daily history riêng
                // → Cần crawl từ minutely và aggregate
                // Hoặc compute từ WebSocket data trong ngày
                
                List<DerivativeMinutelyItem> minuteData = drApiService.getMinutelyDerivatives(
                    code, "0", null, null
                ).get(30, TimeUnit.SECONDS);
                
                if (minuteData != null && !minuteData.isEmpty()) {
                    SymbolDaily daily = aggregateToDaily(code, minuteData);
                    symbolDailyRepository.save(daily);
                    log.info("Saved daily data for derivative: {}", code);
                }
                
            } catch (Exception e) {
                log.warn("Failed to download daily for {}: {}", code, e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }
    
    private SymbolDaily aggregateToDaily(String code, List<DerivativeMinutelyItem> minutes) {
        SymbolDaily daily = new SymbolDaily();
        daily.setCode(code);
        daily.setDate(new Date());
        
        // Aggregate from minute data
        daily.setOpen(minutes.get(0).getOpen());
        daily.setHigh(minutes.stream().mapToDouble(m -> m.getHigh()).max().orElse(0));
        daily.setLow(minutes.stream().mapToDouble(m -> m.getLow()).min().orElse(0));
        daily.setLast(minutes.get(minutes.size() - 1).getLast());
        daily.setTradingVolume(minutes.stream().mapToLong(m -> m.getVolume()).sum());
        daily.setTradingValue(minutes.stream().mapToDouble(m -> m.getValue()).sum());
        
        return daily;
    }
}
```

**Acceptance Criteria:**
- [ ] Daily data cho derivatives được lưu vào `symbolDaily` collection
- [ ] Data có marker `market: "derivatives"` hoặc `type: "FUTURES"`
- [ ] Error handling không ảnh hưởng equity flow

---

### REQ-CHART-002: Minute Chart Data Collection

**Service:** `realtime-v2`

**Task:** Lưu minute data từ WebSocket vào Redis và MongoDB

**Current Flow (Equity):**
```
WebSocket → Kafka (quoteUpdate) → realtime-v2 → Redis (SYMBOL_QUOTE_MINUTE_*)
                                             → MongoDB (symbolQuoteMinutes) [end of day]
```

**New Flow (Derivatives):**
```
WebSocket → Kafka (quoteUpdateDR) → realtime-v2 → Redis (SYMBOL_QUOTE_MINUTE_*)
                                               → MongoDB (symbolQuoteMinutes) [end of day]
```

**Implementation:**

```java
@Component
@Slf4j
public class DerivativeQuoteMinuteService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private SymbolQuoteMinutesRepository quoteMinutesRepository;
    
    /**
     * Lưu quote data vào Redis (minute cache)
     * Gọi từ DerivativeQuoteConsumer khi nhận message từ Kafka
     */
    public void saveQuoteMinute(DerivativeQuoteDTO quote) {
        String redisKey = "SYMBOL_QUOTE_MINUTE_" + quote.getCode();
        
        SymbolQuoteMinute minute = new SymbolQuoteMinute();
        minute.setCode(quote.getCode());
        minute.setDate(parseTime(quote.getTime()));
        minute.setOpen(quote.getOpen());
        minute.setHigh(quote.getHigh());
        minute.setLow(quote.getLow());
        minute.setLast(quote.getLast());
        minute.setTradingVolume(quote.getTradingVolume());
        minute.setTradingValue(quote.getTradingValue());
        minute.setType("FUTURES");
        minute.setMarket("derivatives");
        
        // Push to Redis list
        String json = objectMapper.writeValueAsString(minute);
        redisTemplate.opsForList().rightPush(redisKey, json);
        
        // Trim to keep only today's data (max ~400 items for 8h trading)
        redisTemplate.opsForList().trim(redisKey, -500, -1);
    }
    
    /**
     * Scheduled job: End of day, flush Redis to MongoDB
     */
    @Scheduled(cron = "0 30 15 * * MON-FRI")  // 15:30 mỗi ngày
    public void flushMinutesToMongo() {
        List<String> derivativeCodes = getDerivativeCodes();
        
        for (String code : derivativeCodes) {
            String redisKey = "SYMBOL_QUOTE_MINUTE_" + code;
            List<String> items = redisTemplate.opsForList().range(redisKey, 0, -1);
            
            if (items != null && !items.isEmpty()) {
                List<SymbolQuoteMinute> minutes = items.stream()
                    .map(json -> objectMapper.readValue(json, SymbolQuoteMinute.class))
                    .collect(Collectors.toList());
                
                quoteMinutesRepository.saveAll(minutes);
                log.info("Flushed {} minute records for {}", minutes.size(), code);
            }
        }
    }
}
```

**Acceptance Criteria:**
- [ ] Minute data được lưu vào Redis key `SYMBOL_QUOTE_MINUTE_{code}`
- [ ] End of day, data được flush vào MongoDB `symbolQuoteMinutes`
- [ ] Data có đủ fields: code, date, open, high, low, last, volume, value

---

### REQ-CHART-003: FeedService Update (market-query-v2)

**Service:** `market-query-v2`

**Task:** Update FeedService để query data phái sinh

**Analysis:**

FeedService hiện tại đã support query theo `symbol`. Vấn đề:
1. **CacheService.getSymbolInfo()** - Đã query từ Redis, sẽ tự động có derivatives nếu Init Job bổ sung
2. **CommonService.querySymbolHistory()** - Query từ MongoDB `symbolDaily`, sẽ tự động có derivatives
3. **Redis SYMBOL_QUOTE_MINUTE** - Sẽ tự động có derivatives nếu realtime-v2 bổ sung

**KẾT LUẬN:** `market-query-v2` **KHÔNG CẦN THAY ĐỔI CODE** nếu:
1. Init Job bổ sung derivatives vào Redis/MongoDB (đã có trong 02_BE_REQUIREMENTS_SPEC.md)
2. realtime-v2 bổ sung minute data cho derivatives vào Redis

**Optional Enhancement - Filter by market:**

```typescript
// FeedService.ts - OPTIONAL: Add market filter
public async queryTradingViewHistory(request: ITradingViewHistoryRequest): Promise<TradingViewHistoryResponse> {
    validateRequest(request, tradingViewHistoryRequestValidator);
    
    // Get symbol info to determine market type
    const symbolInfo: ISymbolInfo = await this.cacheService.getSymbolInfo(request.symbol);
    
    if (symbolInfo == null) {
        return { s: 'no_data' };
    }
    
    // Log for monitoring
    if (symbolInfo.market === 'derivatives' || symbolInfo.type === 'FUTURES') {
        Logger.info(`Chart query for derivative: ${request.symbol}`);
    }
    
    // Rest of logic remains unchanged
    if (RESOLUTION_MINUTE.includes(request.resolution)) {
        return this.getQuoteMinuteHistory(request);
    } else {
        return this.getDailyPeriodHistory(request);
    }
}
```

**Acceptance Criteria:**
- [ ] API `/tradingview/history?symbol=VN30F2501` trả về data đúng format
- [ ] Resolution D/W/M hoạt động với derivatives
- [ ] Resolution 1/5/15/30/60 hoạt động với derivatives (nếu có minute data)

---

### REQ-CHART-004: Lotte DR Minute API Client

**Service:** `market-collector-lotte`

**Task:** Tạo API client để gọi DRMKT-003 (minutely-derivatives)

**Implementation:**

```java
@Service
@Slf4j
public class LotteDrApiService {
    
    // ... existing code ...
    
    /**
     * DRMKT-003: Lấy dữ liệu giá theo phút cho phái sinh
     * 
     * @param code Mã hợp đồng (VN30F2501)
     * @param timeUnit 0=1min, 1=5min, 2=10min, 3=30min
     * @param baseTime Key để lấy records trước đó (yyyyMMdd)
     * @param nextTime Key để lấy records trước đó (HHmmss)
     */
    public CompletableFuture<List<DerivativeMinutelyItem>> getMinutelyDerivatives(
            String code, 
            String timeUnit,
            String baseTime,
            String nextTime) {
        
        String url = baseUrl + "/tsol/apikey/tuxsvc/market/dr/minutely-derivatives";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.set("apiKey", authService.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        body.put("time_unit", timeUnit != null ? timeUnit : "0");
        if (baseTime != null) body.put("base_time", baseTime);
        if (nextTime != null) body.put("next_time", nextTime);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        return CompletableFuture.supplyAsync(() -> {
            ResponseEntity<DRMinutelyResponse> response = 
                restTemplate.exchange(url, HttpMethod.POST, request, DRMinutelyResponse.class);
            
            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getDataList().getListItems();
            }
            throw new LotteApiException("Failed to get DR minutely data");
        });
    }
}

// DTOs
@Data
public class DRMinutelyResponse {
    private String errorCode;
    private String errorDesc;
    private boolean success;
    private DRMinutelyDataList dataList;
}

@Data
public class DRMinutelyDataList {
    private boolean hasNext;
    private String nextKey;
    private List<DerivativeMinutelyItem> listItems;
}

@Data
public class DerivativeMinutelyItem {
    private String time;        // HHmmss
    private Double last;
    private Double change;
    private Double open;
    private Double high;
    private Double low;
    private Long volume;
    private Double value;
    private Double changeRate;
    private Byte lastStatus;
    private Byte changeStatus;
    private Byte openStatus;
    private Byte highStatus;
    private Byte lowStatus;
}
```

**Acceptance Criteria:**
- [ ] API client hoạt động với authentication
- [ ] Parse response đúng format
- [ ] Support pagination (hasNext, nextKey)
- [ ] Error handling

---

## 4. Data Flow Diagram (After Implementation)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CHART DATA FLOW (EQUITY + DERIVATIVES)                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      DATA COLLECTION                                  │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │  EQUITY:                      │  DERIVATIVES:                        │   │
│  │  • WebSocket (auto.qt)        │  • WebSocket (auto.dr.qt)            │   │
│  │  • VietStock/VNDirect crawl   │  • DRMKT-003 (minutely-derivatives)  │   │
│  └──────────────────────────────┴───────────────────────────────────────┘   │
│                    │                           │                            │
│                    ▼                           ▼                            │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         KAFKA                                        │   │
│  │  quoteUpdate          │          quoteUpdateDR                       │   │
│  └──────────────────────┴───────────────────────────────────────────────┘   │
│                    │                           │                            │
│                    ▼                           ▼                            │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      REALTIME-V2                                     │   │
│  │                                                                       │   │
│  │  MINUTE DATA:                                                        │   │
│  │  Redis: SYMBOL_QUOTE_MINUTE_{code}                                   │   │
│  │  MongoDB: symbolQuoteMinutes (end of day flush)                      │   │
│  │                                                                       │   │
│  │  DAILY DATA:                                                         │   │
│  │  Redis: SYMBOL_DAILY                                                 │   │
│  │  MongoDB: symbolDaily                                                │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                              │                                              │
│                              ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    MARKET-QUERY-V2                                   │   │
│  │                                                                       │   │
│  │  GET /tradingview/history?symbol=VN30F2501&resolution=D              │   │
│  │       │                                                               │   │
│  │       ├── FeedService.queryTradingViewHistory()                      │   │
│  │       │       │                                                       │   │
│  │       │       ├── D/W/M: CommonService.querySymbolHistory()          │   │
│  │       │       │           └── MongoDB: symbolDaily                   │   │
│  │       │       │           └── Redis: SYMBOL_DAILY (today)            │   │
│  │       │       │                                                       │   │
│  │       │       └── 1/5/15/30/60: getQuoteMinuteHistory()              │   │
│  │       │                   └── Redis: SYMBOL_QUOTE_MINUTE_*           │   │
│  │       │                   └── MongoDB: symbolQuoteMinutes            │   │
│  │       │                                                               │   │
│  │       └── Response: TradingView format (s, t, c, o, h, l, v)         │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Response Format

### 5.1 TradingView History Response (Unchanged)

```json
{
    "s": "ok",
    "t": [1706400000, 1706486400, 1706572800],
    "c": [1285.5, 1290.0, 1287.5],
    "o": [1280.0, 1285.5, 1290.0],
    "h": [1295.0, 1298.0, 1292.0],
    "l": [1275.0, 1282.0, 1285.0],
    "v": [125000, 130000, 115000]
}
```

| Field | Description |
|-------|-------------|
| `s` | Status: "ok", "no_data", "error" |
| `t` | Array of timestamps (Unix, seconds) |
| `c` | Array of close prices |
| `o` | Array of open prices |
| `h` | Array of high prices |
| `l` | Array of low prices |
| `v` | Array of volumes |

### 5.2 No Data Response

```json
{
    "s": "no_data",
    "nextTime": 1706400000
}
```

---

## 6. Testing Checklist

### 6.1 Unit Tests

- [ ] `LotteDrApiService.getMinutelyDerivatives()` - Parse response
- [ ] `DerivativeDailyService.aggregateToDaily()` - Aggregate logic
- [ ] `DerivativeQuoteMinuteService.saveQuoteMinute()` - Redis save

### 6.2 Integration Tests

| Test ID | Scenario | Expected |
|---------|----------|----------|
| CHART-001 | `/tradingview/history?symbol=VN30F2501&resolution=D` | Returns daily bars |
| CHART-002 | `/tradingview/history?symbol=VN30F2501&resolution=1` | Returns minute bars |
| CHART-003 | `/tradingview/history?symbol=VN30F2501&resolution=W` | Returns weekly bars |
| CHART-004 | Symbol not found | Returns `{ s: "no_data" }` |
| CHART-005 | From > To | Returns error |

### 6.3 E2E Tests

| Test ID | Scenario | Expected |
|---------|----------|----------|
| E2E-CHART-001 | TradingView library load VN30F2501 | Chart renders correctly |
| E2E-CHART-002 | Switch resolution D → 1 | Data updates |
| E2E-CHART-003 | Scroll back to load history | More data loads |

---

## 7. Configuration

```yaml
# market-collector-lotte/application.yaml
derivatives:
  chart:
    enabled: true
    minuteApi:
      url: /tuxsvc/market/dr/minutely-derivatives
      timeUnits:
        "1": "0"    # 1 min
        "5": "1"    # 5 min
        "10": "2"   # 10 min
        "30": "3"   # 30 min

# realtime-v2/application.yaml
derivatives:
  chart:
    flushCron: "0 30 15 * * MON-FRI"  # 15:30 daily
    redisKeyPrefix: SYMBOL_QUOTE_MINUTE_
```

---

## 8. Implementation Priority

| Priority | Requirement | Dependency | Effort |
|----------|-------------|------------|--------|
| P0 | REQ-CHART-002: Minute data collection | WebSocket integration done | 2-3 days |
| P0 | REQ-CHART-001: Daily data collection | Init Job done | 1-2 days |
| P1 | REQ-CHART-004: Lotte DR Minute API | None | 1 day |
| P2 | REQ-CHART-003: FeedService (optional) | None | 0.5 day |

**Total Estimated Effort:** 4-6 days

---

## 9. Key Insight

> **QUAN TRỌNG:** `market-query-v2` FeedService **KHÔNG CẦN THAY ĐỔI CODE**.
> 
> Nó chỉ query data từ Redis và MongoDB. Miễn là:
> 1. `realtime-v2` lưu minute data vào đúng Redis key và MongoDB collection
> 2. Daily data có trong `symbolDaily` collection
> 
> → API `/tradingview/history` sẽ **tự động hoạt động** với derivatives.

---

## 10. FE Impact

**Frontend KHÔNG cần thay đổi:**
- API endpoint giữ nguyên: `/tradingview/history`
- Request parameters giữ nguyên
- Response format giữ nguyên (TradingView standard)
- Chỉ cần truyền symbol là mã phái sinh (VN30F2501)

---

## References

| Document | Description |
|----------|-------------|
| [02_BE_REQUIREMENTS_SPEC.md](./02_BE_REQUIREMENTS_SPEC.md) | Main BE Requirements |
| [Lotte DR API Specs](../../../Documentation/Lotte_DR_API_Specs.md) | DRMKT-003 specification |
| `Knowledge/TradeX/init-job.md` | Current Init Job mechanism |

---

*End of Chart API Requirements*

---

Document Status: 📋 | For: PM/Dev | Next Steps: Review nội dung, cập nhật status trên Tracking/tasks.js
