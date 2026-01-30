# Backend Requirements Specification - Derivatives Integration

> **Document ID:** DER-REQ-001  
> **Version:** 1.0  
> **Created:** 2025-01-30  
> **For:** Backend Development Team

---

## Overview

Tài liệu này mô tả chi tiết các yêu cầu kỹ thuật để BE developer triển khai tích hợp phái sinh vào hệ thống TradeX.

**Key Principle:** ISOLATION - Derivatives code path PHẢI được cô lập để không ảnh hưởng đến equity flow hiện tại.

---

## Module 1: Init Job Enhancement

### REQ-INIT-001: Lotte DR API Client

**Service:** `market-collector-lotte`

**Task:** Tạo client class để gọi Lotte Derivatives APIs

**Files to create/modify:**
- `src/main/java/com/tradex/service/api/LotteDrApiService.java` (NEW)
- `src/main/java/com/tradex/dto/derivative/*` (NEW)

**Implementation:**

```java
@Service
public class LotteDrApiService {
    
    private final RestTemplate restTemplate;
    private final LotteAuthService authService;
    
    @Value("${lotte.api.base-url}")
    private String baseUrl;
    
    /**
     * DRMKT-001: Lấy danh sách tất cả mã phái sinh
     * 
     * Endpoint: POST /tuxsvc/market/dr/stock-board
     * Auth: OAuth2 Bearer + API KEY header
     * 
     * @return List of derivative symbols with prices
     */
    public CompletableFuture<List<DerivativeStockBoard>> getStockBoard() {
        String url = baseUrl + "/tuxsvc/market/dr/stock-board";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.set("apiKey", authService.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>("{}", headers);
        
        return CompletableFuture.supplyAsync(() -> {
            ResponseEntity<DRStockBoardResponse> response = 
                restTemplate.exchange(url, HttpMethod.POST, request, DRStockBoardResponse.class);
            
            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getDataList().getListItems();
            }
            throw new LotteApiException("Failed to get DR stock board");
        });
    }
    
    /**
     * DRMKT-002: Lấy thông tin chi tiết của một mã phái sinh
     * 
     * Endpoint: GET /tuxsvc/market/dr/stock-price?code={code}
     * Auth: OAuth2 Bearer + API KEY header
     * 
     * @param code Mã hợp đồng (VN30F2501, VN30F2502...)
     * @return Detailed price info
     */
    public CompletableFuture<DerivativeStockPrice> getStockPrice(String code) {
        String url = baseUrl + "/tuxsvc/market/dr/stock-price?code=" + code;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.set("apiKey", authService.getApiKey());
        
        HttpEntity<?> request = new HttpEntity<>(headers);
        
        return CompletableFuture.supplyAsync(() -> {
            ResponseEntity<DRStockPriceResponse> response = 
                restTemplate.exchange(url, HttpMethod.GET, request, DRStockPriceResponse.class);
            
            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getDataList();
            }
            throw new LotteApiException("Failed to get DR stock price for: " + code);
        });
    }
}
```

**DTOs to create:**

```java
// DerivativeStockBoard.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DerivativeStockBoard {
    private String code;           // Mã hợp đồng: VN30F2501
    private Double last;           // Giá hiện tại
    private Double change;         // Thay đổi
    @JsonProperty("change_rate")
    private Double changeRate;     // % thay đổi
    private Long vol;              // Khối lượng
    private Double ceiling;        // Giá trần
    private Double floor;          // Giá sàn
    @JsonProperty("ref_price")
    private Double refPrice;       // Giá tham chiếu
    private Double open;
    private Double high;
    private Double low;
    private Double bid1, bid2, bid3;
    private Double offer1, offer2, offer3;
    @JsonProperty("bid1_size")
    private Long bid1Size;
    @JsonProperty("bid2_size")
    private Long bid2Size;
    @JsonProperty("bid3_size")
    private Long bid3Size;
    @JsonProperty("offer1_size")
    private Long offer1Size;
    @JsonProperty("offer2_size")
    private Long offer2Size;
    @JsonProperty("offer3_size")
    private Long offer3Size;
    private Long oi;               // Open Interest
    @JsonProperty("exp_date")
    private Integer expDate;       // Ngày đáo hạn (yyyyMMdd)
    @JsonProperty("control_code")
    private String controlCode;    // Trạng thái thị trường
    @JsonProperty("foreign_buy_vol")
    private Long foreignBuyVol;
    @JsonProperty("foreign_sell_vol")
    private Long foreignSellVol;
}

// DerivativeStockPrice.java
@Data
@NoArgsConstructor
public class DerivativeStockPrice {
    private String code;
    private String name;
    private Double ceiling, floor, open, high, low, last, change;
    @JsonProperty("ref_price")
    private Double refPrice;
    @JsonProperty("average_price")
    private Double averagePrice;
    private Long volume, amount;
    @JsonProperty("bid_offer_list")
    private List<BidOfferItem> bidOfferList;
    @JsonProperty("total_vis_bid_size")
    private Long totalVisBidSize;
    @JsonProperty("total_vis_offer_size")
    private Long totalVisOfferSize;
    @JsonProperty("open_interest")
    private Long openInterest;
    @JsonProperty("base_code")
    private String baseCode;       // VN30
    @JsonProperty("first_trd_date")
    private String firstTrdDate;   // 20250101
    @JsonProperty("end_trd_date")
    private String endTrdDate;     // 20250130
    @JsonProperty("remain_date")
    private Integer remainDate;
    @JsonProperty("theory_price")
    private Double theoryPrice;
    @JsonProperty("theory_basis")
    private Double theoryBasis;
    @JsonProperty("foreign_buy_vol")
    private Long foreignBuyVol;
    @JsonProperty("foreign_sell_vol")
    private Long foreignSellVol;
}
```

**Acceptance Criteria:**
- [ ] API calls return valid data for all derivative symbols
- [ ] Proper error handling with meaningful messages
- [ ] OAuth2 + API KEY authentication working
- [ ] Retry mechanism for transient failures
- [ ] Unit tests with mocked responses

---

### REQ-INIT-002: Derivative Symbol Download Service

**Service:** `market-collector-lotte`

**Task:** Tạo service download và merge derivatives vào init flow

**Files to modify:**
- `src/main/java/com/tradex/service/LotteApiSymbolInfoService.java` (MODIFY)
- `src/main/java/com/tradex/service/DerivativeSymbolService.java` (NEW)

**Implementation - New Service:**

```java
@Service
@Slf4j
public class DerivativeSymbolService {
    
    @Autowired
    private LotteDrApiService drApiService;
    
    /**
     * Download tất cả mã phái sinh và convert sang SymbolInfo
     * 
     * IMPORTANT: Method này PHẢI có try-catch bao quanh toàn bộ
     * Nếu fail, return empty list - KHÔNG throw exception
     * 
     * @return List<SymbolInfo> với market="derivatives"
     */
    public List<SymbolInfo> downloadDerivativeSymbols() {
        try {
            log.info("Starting derivative symbols download...");
            
            // Step 1: Get list of all derivative codes
            List<DerivativeStockBoard> stockBoards = drApiService.getStockBoard()
                .get(30, TimeUnit.SECONDS);
            
            if (stockBoards == null || stockBoards.isEmpty()) {
                log.warn("No derivative symbols found from Lotte API");
                return Collections.emptyList();
            }
            
            log.info("Found {} derivative symbols", stockBoards.size());
            
            // Step 2: Get detailed info for each symbol
            List<SymbolInfo> derivativeSymbols = new ArrayList<>();
            
            for (DerivativeStockBoard board : stockBoards) {
                try {
                    DerivativeStockPrice price = drApiService.getStockPrice(board.getCode())
                        .get(10, TimeUnit.SECONDS);
                    
                    SymbolInfo symbolInfo = mergeToSymbolInfo(board, price);
                    derivativeSymbols.add(symbolInfo);
                    
                } catch (Exception e) {
                    log.warn("Failed to get price for derivative: {}, error: {}", 
                        board.getCode(), e.getMessage());
                    // Continue with next symbol
                }
            }
            
            log.info("Successfully downloaded {} derivative symbols", derivativeSymbols.size());
            return derivativeSymbols;
            
        } catch (Exception e) {
            log.error("Failed to download derivative symbols, continuing without derivatives", e);
            return Collections.emptyList();  // GRACEFUL DEGRADATION
        }
    }
    
    /**
     * Merge board data và price data thành SymbolInfo
     */
    private SymbolInfo mergeToSymbolInfo(DerivativeStockBoard board, DerivativeStockPrice price) {
        SymbolInfo info = new SymbolInfo();
        
        // === Identification ===
        info.setCode(board.getCode());
        info.setName(price.getName());
        info.setNameEn(price.getName());  // Use same name for EN
        info.setType(SymbolType.FUTURES.name());
        info.setMarket("derivatives");     // KEY IDENTIFIER
        info.setMarketType(extractMarketType(board.getCode()));  // VN30F
        
        // === Price Data ===
        info.setOpen(board.getOpen());
        info.setHigh(board.getHigh());
        info.setLow(board.getLow());
        info.setLast(board.getLast());
        info.setChange(board.getChange());
        info.setRate(board.getChangeRate());
        info.setCeilingPrice(board.getCeiling());
        info.setFloorPrice(board.getFloor());
        info.setReferencePrice(board.getRefPrice());
        info.setAveragePrice(price.getAveragePrice());
        
        // === Volume Data ===
        info.setTradingVolume(board.getVol());
        info.setTradingValue(price.getAmount());
        info.setOpenInterest(board.getOi());
        
        // === Bid/Offer Data ===
        info.setBidOfferList(buildBidOfferList(board));
        info.setTotalBidVolume(board.getBid1Size() + board.getBid2Size() + board.getBid3Size());
        info.setTotalOfferVolume(board.getOffer1Size() + board.getOffer2Size() + board.getOffer3Size());
        
        // === Foreigner Data ===
        info.setForeignerBuyVolume(board.getForeignBuyVol());
        info.setForeignerSellVolume(board.getForeignSellVol());
        
        // === Derivatives-specific ===
        info.setBaseCode(price.getBaseCode());
        info.setFirstTradingDate(price.getFirstTrdDate());
        info.setLastTradingDate(price.getEndTrdDate());
        info.setRemainingDays(price.getRemainDate());
        info.setTheoryPrice(price.getTheoryPrice());
        info.setBasis(price.getTheoryBasis());
        
        // === Session ===
        info.setSessions(convertControlCode(board.getControlCode()));
        
        return info;
    }
    
    private String extractMarketType(String code) {
        // VN30F2501 -> VN30F
        if (code != null && code.length() >= 5) {
            return code.substring(0, 5);
        }
        return "FUTURES";
    }
    
    private List<BidOfferItem> buildBidOfferList(DerivativeStockBoard board) {
        List<BidOfferItem> list = new ArrayList<>();
        list.add(new BidOfferItem(board.getBid1(), board.getBid1Size(), 
                                   board.getOffer1(), board.getOffer1Size()));
        list.add(new BidOfferItem(board.getBid2(), board.getBid2Size(), 
                                   board.getOffer2(), board.getOffer2Size()));
        list.add(new BidOfferItem(board.getBid3(), board.getBid3Size(), 
                                   board.getOffer3(), board.getOffer3Size()));
        return list;
    }
    
    private String convertControlCode(String controlCode) {
        // P=ATO, O/R=LO, I=BREAK, A=ATC, C=PLO, K=CLOSED
        switch (controlCode) {
            case "P": return "ATO";
            case "O": case "R": return "LO";
            case "I": return "INTERMISSION";
            case "A": return "ATC";
            case "C": return "PLO";
            case "K": case "G": return "CLOSED";
            default: return "LO";
        }
    }
}
```

**Modify existing service:**

```java
// LotteApiSymbolInfoService.java - MODIFY downloadSymbol()

@Service
public class LotteApiSymbolInfoService {
    
    @Autowired
    private DerivativeSymbolService derivativeSymbolService;  // NEW
    
    @Value("${derivatives.init.enabled:true}")
    private boolean derivativesEnabled;  // Feature flag
    
    public CompletableFuture<Void> downloadSymbol() {
        // === EXISTING CODE (UNCHANGED) ===
        List<SymbolInfo> equitySymbols = downloadEquitySymbols();
        
        // === NEW CODE (ADDITIVE) ===
        List<SymbolInfo> derivativeSymbols = Collections.emptyList();
        
        if (derivativesEnabled) {
            try {
                derivativeSymbols = derivativeSymbolService.downloadDerivativeSymbols();
                log.info("Added {} derivative symbols to init", derivativeSymbols.size());
            } catch (Exception e) {
                log.warn("Derivative symbols download failed, continuing with equity only", e);
                // DON'T THROW - Continue with equity
            }
        }
        
        // === MERGE ===
        List<SymbolInfo> allSymbols = new ArrayList<>(equitySymbols);
        allSymbols.addAll(derivativeSymbols);
        
        // === EXISTING CODE (UNCHANGED) - Save to Redis, MongoDB, MinIO ===
        return saveSymbols(allSymbols);
    }
}
```

**Acceptance Criteria:**
- [ ] Derivatives download DOES NOT affect equity download on failure
- [ ] SymbolInfo objects have `market="derivatives"` set
- [ ] All derivative-specific fields are populated
- [ ] Feature flag `derivatives.init.enabled` controls behavior
- [ ] Proper logging for monitoring

---

### REQ-INIT-003: SymbolInfo Model Update

**Service:** `tradex-common-java`

**Task:** Thêm các fields mới cho derivatives vào SymbolInfo model

**Files to modify:**
- `src/main/java/com/tradex/model/SymbolInfo.java`

**New Fields:**

```java
// Add these fields to existing SymbolInfo.java

/**
 * Market identifier to distinguish equity vs derivatives
 * Values: null (default, equity), "derivatives"
 */
private String market;

/**
 * Open Interest - Outstanding contracts (Derivatives only)
 */
private Long openInterest;

/**
 * Base/Underlying asset code (e.g., "VN30" for VN30F2501)
 */
private String baseCode;

/**
 * First trading date of the contract (yyyyMMdd)
 */
private String firstTradingDate;

/**
 * Last trading date / Expiry date (yyyyMMdd)
 */
private String lastTradingDate;

/**
 * Number of days remaining until expiry
 */
private Integer remainingDays;

/**
 * Theoretical price calculated by exchange
 */
private Double theoryPrice;

/**
 * Basis = Futures Price - Spot Price
 */
private Double basis;
```

**Acceptance Criteria:**
- [ ] New fields added with proper annotations
- [ ] JSON serialization/deserialization working
- [ ] Backward compatible (existing code unaffected)
- [ ] MongoDB schema auto-update supported

---

## Module 2: SymbolInfo API - Giữ nguyên cơ chế

### REQ-API-001: Không thay đổi market-query-v2

> **QUAN TRỌNG:** FE hiện tại sử dụng WebSocket để nhận giá real-time, KHÔNG sử dụng `/api/v2/market/symbol/latest`.
> Để hạn chế thay đổi ở FE, cơ chế cho phái sinh PHẢI giống với cơ sở.

**Service:** `market-query-v2`

**Task:** KHÔNG cần thay đổi code

**Lý do:**
1. `market-query-v2` chỉ đọc data từ Redis
2. Data đã được aggregate bởi `realtime-v2` (từ WebSocket)
3. Khi `realtime-v2` update SymbolInfo cho phái sinh vào Redis → API tự động hoạt động

### Cơ chế hoạt động

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              FLOW HIỆN TẠI (Equity) - ĐÃ HOẠT ĐỘNG                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Lotte WS (auto.qt, auto.bo)                                               │
│       │                                                                     │
│       ▼                                                                     │
│  market-collector-lotte → Kafka → realtime-v2 → Redis                      │
│                                                                             │
│  /api/v2/market/symbolInfo → market-query-v2 → Read from Redis → Response  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│              FLOW MỚI (Derivatives) - TƯƠNG TỰ                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Lotte WS (auto.dr.qt, auto.dr.bo)                                         │
│       │                                                                     │
│       ▼                                                                     │
│  market-collector-lotte → Kafka → realtime-v2 → Redis                      │
│       (NEW handlers)        (NEW topics)   (NEW consumers)                 │
│                                                                             │
│  /api/v2/market/symbolInfo → market-query-v2 → Read from Redis → Response  │
│                               (KHÔNG ĐỔI)                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### REQ-API-002: Data aggregation tại realtime-v2

**Service:** `realtime-v2`

**Task:** Aggregate data phái sinh từ WebSocket vào Redis (giống cơ sở)

**Files to modify:**
- `src/main/java/com/tradex/consumer/DerivativeQuoteConsumer.java` (NEW)
- `src/main/java/com/tradex/consumer/DerivativeBidOfferConsumer.java` (NEW)
- `src/main/java/com/tradex/service/DerivativeSymbolInfoService.java` (NEW)

**Nguyên tắc:**
1. Consume từ Kafka topics: `quoteUpdateDR`, `bidOfferUpdateDR`
2. Update vào SymbolInfo object trong cache
3. Save vào Redis key: `realtime_mapSymbolInfo` (CÙNG key với equity)
4. API tự động trả về data mới nhất

**Implementation:**

```java
@Component
@Slf4j
public class DerivativeQuoteConsumer {
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private MarketRedisDao marketRedisDao;
    
    /**
     * Consume quote updates từ Kafka và aggregate vào SymbolInfo
     * GIỐNG LOGIC với equity QuoteService
     */
    @KafkaListener(topics = "quoteUpdateDR", groupId = "realtime-v2-dr")
    public void onQuoteUpdate(ConsumerRecord<String, String> record) {
        try {
            DerivativeQuoteDTO quote = objectMapper.readValue(record.value(), DerivativeQuoteDTO.class);
            
            // Lấy SymbolInfo từ cache (đã được init từ init job)
            SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(quote.getCode());
            
            if (symbolInfo == null) {
                log.warn("SymbolInfo not found for derivative: {}", quote.getCode());
                return;
            }
            
            // Update fields từ quote (giống equity)
            symbolInfo.setLast(quote.getLast());
            symbolInfo.setChange(quote.getChange());
            symbolInfo.setRate(quote.getChangeRate());
            symbolInfo.setOpen(quote.getOpen());
            symbolInfo.setHigh(quote.getHigh());
            symbolInfo.setLow(quote.getLow());
            symbolInfo.setTradingVolume(quote.getTradingVolume());
            symbolInfo.setMatchingVolume(quote.getMatchingVolume());
            symbolInfo.setMatchBy(quote.getMatchedBy());
            symbolInfo.setTotalBidVolume(quote.getTotalBidVolume());
            symbolInfo.setTotalOfferVolume(quote.getTotalOfferVolume());
            symbolInfo.setForeignerBuyVolume(quote.getForeignerBuyVolume());
            symbolInfo.setForeignerSellVolume(quote.getForeignerSellVolume());
            symbolInfo.setTime(quote.getTime());
            symbolInfo.setUpdatedAt(new Date());
            
            // Save to Redis (CÙNG key với equity)
            marketRedisDao.setSymbolInfo(symbolInfo);
            
        } catch (Exception e) {
            log.error("Error processing DR quote update", e);
        }
    }
}

@Component
@Slf4j
public class DerivativeBidOfferConsumer {
    
    @KafkaListener(topics = "bidOfferUpdateDR", groupId = "realtime-v2-dr")
    public void onBidOfferUpdate(ConsumerRecord<String, String> record) {
        try {
            DerivativeBidOfferDTO bidOffer = objectMapper.readValue(record.value(), DerivativeBidOfferDTO.class);
            
            SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(bidOffer.getCode());
            
            if (symbolInfo == null) {
                return;
            }
            
            // Update bid/offer list (giống equity)
            symbolInfo.setBidOfferList(bidOffer.getBidOfferList());
            symbolInfo.setTotalBidVolume(bidOffer.getTotalBidVolume());
            symbolInfo.setTotalOfferVolume(bidOffer.getTotalOfferVolume());
            symbolInfo.setSessions(convertControlCode(bidOffer.getControlCode()));
            
            // ATO/ATC: update expected price
            if (bidOffer.getExpectedPrice() != null && bidOffer.getExpectedPrice() > 0) {
                symbolInfo.setExpectedPrice(bidOffer.getExpectedPrice());
            }
            
            // Save to Redis
            marketRedisDao.setSymbolInfo(symbolInfo);
            
        } catch (Exception e) {
            log.error("Error processing DR bidoffer update", e);
        }
    }
}
```

### REQ-API-003: Response format

Khi FE gọi `/api/v2/market/symbolInfo?symbolList=VN30F2501`, response sẽ giống format cơ sở:

```json
{
  "s": "VN30F2501",
  "m": "derivatives",
  "t": "FUTURES",
  "c": 1285.5,
  "ch": 12.5,
  "ra": 0.98,
  "o": 1275.0,
  "h": 1290.0,
  "l": 1270.0,
  "ce": 1350.0,
  "fl": 1220.0,
  "re": 1273.0,
  "vo": 125000,
  "tb": 5000,
  "to": 4500,
  "bb": [{"p": 1285.0, "v": 1200}, ...],
  "bo": [{"p": 1285.5, "v": 1000}, ...],
  "fr": {"bv": 5000, "sv": 3000},
  "oi": 45000,
  "bc": "VN30",
  "ed": "20250130",
  "rd": 15
}
```

### Acceptance Criteria

- [ ] `realtime-v2` có consumers mới cho DR Kafka topics
- [ ] Data phái sinh được aggregate vào Redis (cùng key với equity)
- [ ] `market-query-v2` KHÔNG cần thay đổi code
- [ ] API `/api/v2/market/symbolInfo` trả về data phái sinh đã aggregate
- [ ] FE KHÔNG cần thay đổi cơ chế gọi API

---

## Module 3: Real-time WebSocket Integration

### REQ-WS-001: Lotte DR WebSocket Subscription

**Service:** `market-collector-lotte`

**Task:** Subscribe Lotte derivatives WebSocket channels

**Files to create/modify:**
- `src/main/java/com/tradex/websocket/DerivativeWebSocketHandler.java` (NEW)
- `src/main/java/com/tradex/websocket/LotteWebSocketClient.java` (MODIFY)

**New Handler:**

```java
@Component
@Slf4j
public class DerivativeWebSocketHandler {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Kafka topics for derivatives
    private static final String DR_QUOTE_TOPIC = "quoteUpdateDR";
    private static final String DR_BIDOFFER_TOPIC = "bidOfferUpdateDR";
    
    /**
     * Handle Future Quote message (RMK-011)
     * Channel: pro.pub.auto.dr.qt./{code}
     * 
     * Message format: pipe-separated values
     */
    public void onQuoteMessage(String rawMessage) {
        try {
            String[] parts = rawMessage.split("\\|");
            
            if (parts.length < 34) {
                log.warn("Invalid DR quote message length: {}", parts.length);
                return;
            }
            
            DerivativeQuoteDTO quote = DerivativeQuoteDTO.builder()
                .service(parts[0])
                .code(parts[3])
                .time(parts[2])
                .open(parseDoubleSafe(parts[6]))
                .high(parseDoubleSafe(parts[8]))
                .low(parseDoubleSafe(parts[10]))
                .last(parseDoubleSafe(parts[12]))
                .change(parseDoubleSafe(parts[14]))
                .changeRate(parseDoubleSafe(parts[15]))
                .averagePrice(parseDoubleSafe(parts[16]))
                .referencePrice(parseDoubleSafe(parts[17]))
                .tradingValue(parseLongSafe(parts[18]))
                .tradingVolume(parseLongSafe(parts[19]))
                .matchingVolume(parseLongSafe(parts[20]))
                .matchedBy(parts[21].equals("B") ? "BID" : "ASK")
                .bidPrice(parseDoubleSafe(parts[22]))
                .offerPrice(parseDoubleSafe(parts[24]))
                .totalBidVolume(parseLongSafe(parts[28]))
                .totalOfferVolume(parseLongSafe(parts[29]))
                .foreignerBuyVolume(parseLongSafe(parts[32]))
                .foreignerSellVolume(parseLongSafe(parts[33]))
                .market("derivatives")
                .build();
            
            // Publish to Kafka
            String json = objectMapper.writeValueAsString(quote);
            kafkaTemplate.send(DR_QUOTE_TOPIC, quote.getCode(), json);
            
            log.debug("Published DR quote for: {}", quote.getCode());
            
        } catch (Exception e) {
            log.error("Error processing DR quote message: {}", rawMessage, e);
            // Don't rethrow - isolate error
        }
    }
    
    /**
     * Handle Future Bid/Offer message (RMK-012)
     * Channel: pro.pub.auto.dr.bo./{code}
     */
    public void onBidOfferMessage(String rawMessage) {
        try {
            String[] parts = rawMessage.split("\\|");
            
            if (parts.length < 80) {
                log.warn("Invalid DR bidoffer message length: {}", parts.length);
                return;
            }
            
            DerivativeBidOfferDTO bidOffer = DerivativeBidOfferDTO.builder()
                .service(parts[0])
                .code(parts[3])
                .time(parts[2])
                .controlCode(parts[4])
                .expectedPrice(parseDoubleSafe(parts[5]))
                .bidOfferList(parseBidOfferList(parts))  // Parse 10 levels
                .totalBidVolume(parseLongSafe(parts[73]))
                .totalOfferVolume(parseLongSafe(parts[74]))
                .market("derivatives")
                .build();
            
            // Publish to Kafka
            String json = objectMapper.writeValueAsString(bidOffer);
            kafkaTemplate.send(DR_BIDOFFER_TOPIC, bidOffer.getCode(), json);
            
        } catch (Exception e) {
            log.error("Error processing DR bidoffer message", e);
        }
    }
    
    /**
     * Parse 10 price levels from message
     * Fields [13] to [72], 6 fields per level
     */
    private List<BidOfferItem> parseBidOfferList(String[] parts) {
        List<BidOfferItem> list = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            int baseIndex = 13 + (i * 6);
            
            BidOfferItem item = new BidOfferItem();
            item.setBidPrice(parseDoubleSafe(parts[baseIndex]));
            item.setBidVolume(parseLongSafe(parts[baseIndex + 2]));
            item.setOfferPrice(parseDoubleSafe(parts[baseIndex + 3]));
            item.setOfferVolume(parseLongSafe(parts[baseIndex + 5]));
            
            list.add(item);
        }
        
        return list;
    }
    
    private Double parseDoubleSafe(String value) {
        try {
            return StringUtils.hasText(value) ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Long parseLongSafe(String value) {
        try {
            return StringUtils.hasText(value) ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
```

**Modify WebSocket Client:**

```java
// LotteWebSocketClient.java - ADD subscription logic

@Component
public class LotteWebSocketClient {
    
    @Autowired
    private DerivativeWebSocketHandler drHandler;
    
    @Value("${derivatives.websocket.enabled:true}")
    private boolean drEnabled;
    
    private List<String> derivativeCodes;
    
    /**
     * Subscribe to derivative channels
     * Call this after successful connection
     */
    public void subscribeDerivatives(List<String> codes) {
        if (!drEnabled) {
            log.info("Derivative WebSocket disabled by config");
            return;
        }
        
        this.derivativeCodes = codes;
        
        for (String code : codes) {
            // Subscribe Quote channel
            String quoteChannel = "sub/pro.pub.auto.dr.qt./" + code;
            sendMessage(quoteChannel);
            
            // Subscribe BidOffer channel
            String boChannel = "sub/pro.pub.auto.dr.bo./" + code;
            sendMessage(boChannel);
        }
        
        log.info("Subscribed to {} derivative channels", codes.size() * 2);
    }
    
    @Override
    protected void onMessage(String message) {
        // Route to appropriate handler based on channel
        if (message.startsWith("pro.pub.auto.dr.qt")) {
            drHandler.onQuoteMessage(message);
        } else if (message.startsWith("pro.pub.auto.dr.bo")) {
            drHandler.onBidOfferMessage(message);
        } else {
            // Existing equity handler
            equityHandler.onMessage(message);
        }
    }
}
```

**Acceptance Criteria:**
- [ ] Successfully subscribes to all derivative codes
- [ ] Quote messages parsed correctly
- [ ] BidOffer messages with 10 levels parsed correctly
- [ ] Messages published to correct Kafka topics
- [ ] Error handling doesn't affect equity flow

---

### REQ-WS-002: Realtime-v2 Kafka Consumer

**Service:** `realtime-v2`

**Task:** Consume derivatives Kafka messages và update Redis

**Files to create/modify:**
- `src/main/java/com/tradex/consumer/DerivativeQuoteConsumer.java` (NEW)
- `src/main/java/com/tradex/consumer/DerivativeBidOfferConsumer.java` (NEW)

**Implementation:**

```java
@Component
@Slf4j
public class DerivativeQuoteConsumer {
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private MarketRedisDao marketRedisDao;
    
    @KafkaListener(topics = "quoteUpdateDR", groupId = "realtime-v2-dr")
    public void onQuoteUpdate(ConsumerRecord<String, String> record) {
        try {
            DerivativeQuoteDTO quote = objectMapper.readValue(record.value(), DerivativeQuoteDTO.class);
            
            SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().get(quote.getCode());
            
            if (symbolInfo == null) {
                log.warn("SymbolInfo not found for derivative: {}", quote.getCode());
                return;
            }
            
            // Update SymbolInfo with quote data
            updateSymbolInfoFromQuote(symbolInfo, quote);
            
            // Save to Redis
            marketRedisDao.setSymbolInfo(symbolInfo);
            
            log.debug("Updated derivative quote: {}", quote.getCode());
            
        } catch (Exception e) {
            log.error("Error processing DR quote update", e);
        }
    }
    
    private void updateSymbolInfoFromQuote(SymbolInfo info, DerivativeQuoteDTO quote) {
        info.setLast(quote.getLast());
        info.setChange(quote.getChange());
        info.setRate(quote.getChangeRate());
        info.setOpen(quote.getOpen());
        info.setHigh(quote.getHigh());
        info.setLow(quote.getLow());
        info.setTradingVolume(quote.getTradingVolume());
        info.setTradingValue(quote.getTradingValue());
        info.setMatchingVolume(quote.getMatchingVolume());
        info.setMatchBy(quote.getMatchedBy());
        info.setAveragePrice(quote.getAveragePrice());
        info.setTotalBidVolume(quote.getTotalBidVolume());
        info.setTotalOfferVolume(quote.getTotalOfferVolume());
        info.setForeignerBuyVolume(quote.getForeignerBuyVolume());
        info.setForeignerSellVolume(quote.getForeignerSellVolume());
        info.setTime(quote.getTime());
        info.setUpdatedAt(new Date());
    }
}
```

**Acceptance Criteria:**
- [ ] Consumes from `quoteUpdateDR` and `bidOfferUpdateDR` topics
- [ ] Updates SymbolInfo correctly in cache
- [ ] Saves to Redis
- [ ] Proper error handling

---

### REQ-WS-003: WS-v2 Publisher Update

**Service:** `ws-v2`

**Task:** Publish derivatives data to client WebSocket

**Files to modify:**
- `src/services/WebSocketPublisher.ts`
- `src/consumers/KafkaConsumer.ts`

**Implementation:**

```typescript
// KafkaConsumer.ts - ADD new topic subscription

const TOPICS = [
  'quoteUpdate',        // Existing equity
  'bidOfferUpdate',     // Existing equity
  'quoteUpdateDR',      // NEW: Derivatives
  'bidOfferUpdateDR'    // NEW: Derivatives
];

consumer.subscribe({ topics: TOPICS });

// Handle message based on topic
consumer.on('message', (topic, message) => {
  switch(topic) {
    case 'quoteUpdate':
      wsPublisher.publishQuote(message, 'equity');
      break;
    case 'bidOfferUpdate':
      wsPublisher.publishBidOffer(message, 'equity');
      break;
    case 'quoteUpdateDR':
      wsPublisher.publishQuote(message, 'derivatives');
      break;
    case 'bidOfferUpdateDR':
      wsPublisher.publishBidOffer(message, 'derivatives');
      break;
  }
});

// WebSocketPublisher.ts

class WebSocketPublisher {
  
  publishQuote(data: any, market: string) {
    const code = data.code;
    
    // Determine channel based on market type
    const channel = market === 'derivatives' 
      ? `market.quote.dr.${code}`   // New channel for derivatives
      : `market.quote.${code}`;     // Existing equity channel
    
    // Publish to subscribed clients
    this.io.to(channel).emit('data', {
      channel,
      data: this.formatQuoteResponse(data, market)
    });
  }
  
  formatQuoteResponse(data: any, market: string) {
    const response = {
      s: data.code,
      m: market,   // "derivatives" or "equity"
      c: data.last,
      ch: data.change,
      ra: data.rate,
      vo: data.tradingVolume,
      // ... other fields
    };
    
    // Add derivatives-specific fields
    if (market === 'derivatives') {
      response.oi = data.openInterest;
    }
    
    return response;
  }
}
```

**Client Subscription:**

```javascript
// Client can subscribe to:
// Equity: socket.emit('subscribe', { channel: 'market.quote.VCB' })
// Derivatives: socket.emit('subscribe', { channel: 'market.quote.dr.VN30F2501' })
```

**Acceptance Criteria:**
- [ ] New Kafka topics subscribed
- [ ] Derivatives published to separate channels
- [ ] Existing equity channels unaffected
- [ ] Client can subscribe to DR channels

---

## Module 4: Configuration

### REQ-CONFIG-001: Feature Flags

**Files to modify:**
- `application.yaml` in all affected services

**Configuration:**

```yaml
# market-collector-lotte/application.yaml
derivatives:
  enabled: true
  init:
    enabled: true          # Enable in init job
    failSafe: true         # Continue if fails
  websocket:
    enabled: true          # Enable WS subscription
    reconnectOnError: true

# realtime-v2/application.yaml
derivatives:
  kafka:
    topics:
      quote: quoteUpdateDR
      bidOffer: bidOfferUpdateDR

# ws-v2/application.yaml (or .env)
DERIVATIVES_ENABLED=true
DERIVATIVES_CHANNEL_PREFIX=market.quote.dr
```

**Acceptance Criteria:**
- [ ] Feature flags work correctly
- [ ] Can disable derivatives without code change
- [ ] Logs indicate feature flag status

---

## Testing Checklist

### Unit Tests

- [ ] `LotteDrApiServiceTest` - API client tests
- [ ] `DerivativeSymbolServiceTest` - Download and merge tests
- [ ] `DerivativeWebSocketHandlerTest` - Message parsing tests
- [ ] `SymbolServiceTest` - API filter tests

### Integration Tests

- [ ] Init job with derivatives enabled
- [ ] Init job with derivatives disabled
- [ ] Init job with Lotte DR API failure (graceful degradation)
- [ ] WebSocket message flow
- [ ] API response with mixed equity/derivatives

### E2E Tests

| Test ID | Scenario | Expected Result |
|---------|----------|-----------------|
| E2E-001 | Full init with derivatives | symbol_static.json has derivatives |
| E2E-002 | API /symbol/latest with VN30F2501 | Returns derivative data |
| E2E-003 | WS subscribe market.quote.dr.VN30F2501 | Receives updates |
| E2E-004 | Lotte DR API down during init | Equity init succeeds |
| E2E-005 | Lotte DR WS disconnect | Equity WS continues |

---

## Module 5: Output Format - symbol_static.json

### REQ-OUTPUT-001: Format chuẩn cho symbol_static.json

**IMPORTANT:** Output file `symbol_static.json` PHẢI follow format hiện tại của hệ thống.

**Format hiện tại (Equity):**
```json
{
    "s": "A32",
    "m": "UPCOM",
    "n1": "CTCP 32",
    "n2": "32 Joint Stock Company",
    "t": "STOCK",
    "re": 34800.0,
    "ce": 40000.0,
    "fl": 29600.0,
    "lq": 6800000
}
```

**Format mới (Derivatives):**
```json
{
    "s": "VN30F2501",
    "m": "derivatives",
    "n1": "HĐ Tương lai VN30 Tháng 01/2025",
    "n2": "VN30 Index Futures Jan 2025",
    "t": "FUTURES",
    "re": 1273.0,
    "ce": 1350.0,
    "fl": 1220.0,
    "lq": 0,
    "bc": "VN30",
    "ed": "20250130",
    "rd": 15
}
```

### Field Mapping

| Field | Full Name | Type | Required | Equity | Derivatives |
|-------|-----------|------|----------|--------|-------------|
| `s` | symbol | String | ✅ | VCB, FPT, A32 | VN30F2501, VN30F2502 |
| `m` | market | String | ✅ | HOSE, HNX, UPCOM | **"derivatives"** |
| `n1` | name1 | String | ✅ | Tên tiếng Việt | Tên tiếng Việt |
| `n2` | name2 | String | ✅ | Tên tiếng Anh | Tên tiếng Anh |
| `t` | type | String | ✅ | STOCK, ETF, CW | **"FUTURES"** |
| `re` | reference | Double | ✅ | Giá TC (VND) | Giá TC (Điểm) |
| `ce` | ceiling | Double | ✅ | Giá trần (VND) | Giá trần (Điểm) |
| `fl` | floor | Double | ✅ | Giá sàn (VND) | Giá sàn (Điểm) |
| `lq` | listedQty | Long | ✅ | KL niêm yết | 0 (không áp dụng) |
| `bc` | baseCode | String | ❌ | - | VN30 (mã cơ sở) |
| `ed` | endDate | String | ❌ | - | 20250130 (yyyyMMdd) |
| `rd` | remainDays | Integer | ❌ | - | 15 (số ngày còn lại) |

### Quy tắc đặt tên tiếng Việt (n1)

| Mã | Tên tiếng Việt (n1) |
|----|---------------------|
| VN30F2501 | HĐ Tương lai VN30 Tháng 01/2025 |
| VN30F2502 | HĐ Tương lai VN30 Tháng 02/2025 |
| VN30F2503 | HĐ Tương lai VN30 Tháng 03/2025 |
| VN30F2506 | HĐ Tương lai VN30 Tháng 06/2025 |

### Quy tắc đặt tên tiếng Anh (n2)

| Mã | Tên tiếng Anh (n2) |
|----|---------------------|
| VN30F2501 | VN30 Index Futures Jan 2025 |
| VN30F2502 | VN30 Index Futures Feb 2025 |
| VN30F2503 | VN30 Index Futures Mar 2025 |
| VN30F2506 | VN30 Index Futures Jun 2025 |

### Sample Complete File

```json
[
  {
    "s": "VCB",
    "m": "HOSE",
    "n1": "Ngân hàng TMCP Ngoại thương Việt Nam",
    "n2": "Vietcombank",
    "t": "STOCK",
    "re": 95400.0,
    "ce": 102100.0,
    "fl": 88700.0,
    "lq": 4729432290
  },
  {
    "s": "A32",
    "m": "UPCOM",
    "n1": "CTCP 32",
    "n2": "32 Joint Stock Company",
    "t": "STOCK",
    "re": 34800.0,
    "ce": 40000.0,
    "fl": 29600.0,
    "lq": 6800000
  },
  {
    "s": "VN30F2501",
    "m": "derivatives",
    "n1": "HĐ Tương lai VN30 Tháng 01/2025",
    "n2": "VN30 Index Futures Jan 2025",
    "t": "FUTURES",
    "re": 1273.0,
    "ce": 1350.0,
    "fl": 1220.0,
    "lq": 0,
    "bc": "VN30",
    "ed": "20250130",
    "rd": 15
  },
  {
    "s": "VN30F2502",
    "m": "derivatives",
    "n1": "HĐ Tương lai VN30 Tháng 02/2025",
    "n2": "VN30 Index Futures Feb 2025",
    "t": "FUTURES",
    "re": 1275.0,
    "ce": 1352.0,
    "fl": 1222.0,
    "lq": 0,
    "bc": "VN30",
    "ed": "20250227",
    "rd": 43
  }
]
```

### Acceptance Criteria

- [ ] Mã phái sinh có `m: "derivatives"` (KEY để phân biệt)
- [ ] Mã phái sinh có `t: "FUTURES"`
- [ ] Có đầy đủ fields: s, m, n1, n2, t, re, ce, fl, lq
- [ ] Có thêm fields phái sinh: bc, ed, rd
- [ ] `lq` = 0 cho mã phái sinh
- [ ] Tên tiếng Việt (n1) theo format chuẩn
- [ ] Ngày đáo hạn (ed) format yyyyMMdd

---

## Definition of Done

- [ ] All code reviewed and approved
- [ ] Unit tests passing (>80% coverage)
- [ ] Integration tests passing
- [ ] Feature flags documented
- [ ] Logs added for monitoring
- [ ] No regression on equity functionality
- [ ] **symbol_static.json format matches specification**
- [ ] Documentation updated

---

*End of Requirements Specification*
