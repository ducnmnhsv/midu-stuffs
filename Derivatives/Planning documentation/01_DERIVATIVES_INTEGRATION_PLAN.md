# Derivatives Integration Plan - TradeX Backend

> **Document ID:** DER-PLAN-001  
> **Version:** 1.0  
> **Created:** 2025-01-30  
> **Status:** Draft  
> **Author:** PM Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Risk Assessment & Mitigation](#2-risk-assessment--mitigation)
3. [Work Breakdown Structure](#3-work-breakdown-structure)
4. [Technical Requirements](#4-technical-requirements)
   - 4.1 [Init Job Enhancement](#41-init-job-enhancement)
   - 4.2 [SymbolInfo API Enhancement](#42-symbolinfo-api-enhancement)
   - 4.3 [Real-time WebSocket Integration](#43-real-time-websocket-integration)
5. [Data Model Changes](#5-data-model-changes)
6. [API Specifications](#6-api-specifications)
7. [Testing Strategy](#7-testing-strategy)
8. [Rollout Plan](#8-rollout-plan)

---

## 1. Executive Summary

### 1.1 Objective

Bổ sung giao dịch phái sinh (Derivatives) vào hệ thống TradeX đang chạy ổn định với cơ sở (Equity). Đảm bảo backward compatibility và zero downtime cho hệ thống hiện tại.

### 1.2 Scope

| Item | Description | Priority |
|------|-------------|----------|
| **Init Job** | Lấy danh sách & thông tin mã phái sinh vào `symbol_static.json` | P0 |
| **SymbolInfo API** | Bổ sung mã phái sinh vào `/api/v2/market/symbolInfo` | P0 |
| **WebSocket Realtime** | Nhận giá phái sinh từ Lotte WS và publish ra client | P0 |
| **Order Integration** | Tích hợp đặt lệnh phái sinh (out of scope cho phase này) | P1 |

### 1.3 Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Market Identifier | Field `"m": "derivatives"` | Phân biệt rõ ràng với cơ sở, backward compatible |
| Symbol Type | `FUTURES` | Theo enum hiện có trong hệ thống |
| WebSocket Channel | `market.quote.dr.{code}` | Tách biệt khỏi equity channels |

---

## 2. Risk Assessment & Mitigation

### 2.1 Critical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Init Job failure** ảnh hưởng cơ sở | High | Medium | Tách riêng logic lấy derivatives, try-catch không ảnh hưởng flow chính |
| **Redis key conflict** | High | Low | Sử dụng prefix `DR_` hoặc field `m` để phân biệt |
| **WebSocket channel conflict** | High | Low | Sử dụng channel pattern riêng cho derivatives |
| **Lotte API không ổn định** | Medium | Medium | Retry mechanism, fallback to cache |
| **Client không hiểu format mới** | Medium | Low | Versioning, feature flag |

### 2.2 Mitigation Strategy - ISOLATION PRINCIPLE

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      ISOLATION ARCHITECTURE                                  │
│                                                                             │
│   ┌─────────────────────────────┐   ┌─────────────────────────────┐        │
│   │     EQUITY (Existing)       │   │     DERIVATIVES (New)       │        │
│   ├─────────────────────────────┤   ├─────────────────────────────┤        │
│   │ • Lotte API: /api/v2/*      │   │ • Lotte API: /tuxsvc/dr/*   │        │
│   │ • WS: auto.qt, auto.bo      │   │ • WS: auto.dr.qt, auto.dr.bo│        │
│   │ • Redis: SYMBOL_INFO        │   │ • Redis: SYMBOL_INFO (m=dr) │        │
│   │ • Type: STOCK, ETF, CW...   │   │ • Type: FUTURES             │        │
│   └─────────────────────────────┘   └─────────────────────────────┘        │
│                                                                             │
│   → Nếu Derivatives fail → Equity vẫn hoạt động bình thường                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 Rollback Plan

| Phase | Condition | Action |
|-------|-----------|--------|
| Init Job | Derivatives API fail | Skip derivatives, log warning, continue equity |
| WebSocket | DR channel error | Disable DR subscription, equity unaffected |
| API | DR symbols not found | Return empty array for DR, equity normal |

---

## 3. Work Breakdown Structure

### 3.1 Phase 1: Init Job Enhancement (P0)

| Task ID | Task | Service | Estimate | Dependencies |
|---------|------|---------|----------|--------------|
| INIT-001 | Tạo Lotte DR API client | market-collector-lotte | 2d | - |
| INIT-002 | Lấy danh sách mã phái sinh | market-collector-lotte | 1d | INIT-001 |
| INIT-003 | Lấy thông tin giá mã phái sinh | market-collector-lotte | 1d | INIT-001 |
| INIT-004 | Merge vào symbol_static.json | market-collector-lotte | 1d | INIT-002, INIT-003 |
| INIT-005 | Update SymbolInfo model | tradex-common-java | 1d | - |
| INIT-006 | Unit test & Integration test | market-collector-lotte | 2d | INIT-004 |

### 3.2 Phase 2: Data Aggregation (P0)

> **Lưu ý:** `market-query-v2` KHÔNG cần thay đổi. Data được aggregate bởi `realtime-v2`.

| Task ID | Task | Service | Estimate | Dependencies |
|---------|------|---------|----------|--------------|
| AGG-001 | Tạo DerivativeQuoteConsumer | realtime-v2 | 1d | INIT-005 |
| AGG-002 | Tạo DerivativeBidOfferConsumer | realtime-v2 | 1d | AGG-001 |
| AGG-003 | Aggregate data vào Redis | realtime-v2 | 0.5d | AGG-002 |
| AGG-004 | Unit test | realtime-v2 | 1d | AGG-003 |

### 3.3 Phase 3: WebSocket Integration (P0)

| Task ID | Task | Service | Estimate | Dependencies |
|---------|------|---------|----------|--------------|
| WS-001 | Subscribe Lotte DR channels | market-collector-lotte | 2d | - |
| WS-002 | Parse DR message format | market-collector-lotte | 1d | WS-001 |
| WS-003 | Publish to Kafka | market-collector-lotte | 1d | WS-002 |
| WS-004 | Update realtime-v2 consumer | realtime-v2 | 1d | WS-003 |
| WS-005 | Update ws-v2 publisher | ws-v2 | 1d | WS-004 |
| WS-006 | E2E test | All | 2d | WS-005 |

---

## 4. Technical Requirements

### 4.1 Init Job Enhancement

#### 4.1.1 Yêu cầu chức năng

**FR-INIT-001**: Hệ thống PHẢI lấy danh sách mã phái sinh từ Lotte API khi chạy daily init job.

**FR-INIT-002**: Hệ thống PHẢI lấy thông tin giá của từng mã phái sinh từ Lotte API.

**FR-INIT-003**: Hệ thống PHẢI merge mã phái sinh vào `symbol_static.json` với identifier rõ ràng.

**FR-INIT-004**: Nếu việc lấy derivatives FAIL, hệ thống PHẢI tiếp tục init job cho equity bình thường.

#### 4.1.2 Lotte APIs

**API 1: Lấy danh sách mã phái sinh**

```yaml
API_ID: DRMKT-001
Endpoint: /tuxsvc/market/dr/stock-board
Method: POST/GET
Authentication: OAuth2 + API KEY
Request: {}  # Empty

Response:
  success: boolean
  error_code: "0000" | "1005"
  data_list:
    list_items:
      - code: "VN30F2501"           # Mã hợp đồng
        last: 1285.5                 # Giá hiện tại
        change: 12.5                 # Thay đổi
        change_rate: 0.98            # % thay đổi
        vol: 125000                  # Khối lượng
        ceiling: 1350.0              # Giá trần
        floor: 1220.0                # Giá sàn
        ref_price: 1273.0            # Giá tham chiếu
        open: 1275.0                 # Giá mở cửa
        high: 1290.0                 # Giá cao nhất
        low: 1270.0                  # Giá thấp nhất
        bid1, bid2, bid3: Double     # Giá mua 1,2,3
        offer1, offer2, offer3: Double # Giá bán 1,2,3
        bid1_size, bid2_size, bid3_size: Long
        offer1_size, offer2_size, offer3_size: Long
        oi: 45000                    # Open Interest
        exp_date: 20250130           # Ngày đáo hạn
        control_code: "O"            # Trạng thái thị trường
        foreign_buy_vol: 5000
        foreign_sell_vol: 3000
```

**API 2: Thông tin chi tiết mã phái sinh**

```yaml
API_ID: DRMKT-002
Endpoint: /tuxsvc/market/dr/stock-price
Method: GET
Authentication: OAuth2 + API KEY

Request:
  code: "VN30F2501"  # Mã hợp đồng

Response:
  success: boolean
  error_code: "0000" | "1005"
  data_list:
    code: "VN30F2501"
    name: "VN30 Future Jan 2025"
    ceiling, floor, open, high, low, last, change: Double
    ref_price, average_price: Double
    volume, amount: Long
    bid, offer: Double
    bid_offer_list: [...]           # Sổ lệnh 10 bước
    total_vis_bid_size, total_vis_offer_size: Long
    open_interest: Long
    base_code: "VN30"               # Mã cơ sở
    first_trd_date: "20250101"      # Ngày giao dịch đầu tiên
    end_trd_date: "20250130"        # Ngày đáo hạn
    remain_date: 15                  # Số ngày còn lại
    theory_price, theory_basis: Double
    foreign_buy_vol, foreign_sell_vol: Long
```

#### 4.1.3 Implementation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      INIT JOB ENHANCED FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    LotteApiSymbolInfoService.downloadSymbol()               │
│                                                                             │
│   EXISTING FLOW (Keep unchanged):                                           │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│   │ symbolNames │  │ symbolPrices│  │ bestBidAsks │  │ indexList   │       │
│   │ (Equity)    │  │ (Equity)    │  │ (Equity)    │  │ (Equity)    │       │
│   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘       │
│          └────────────────┴────────────────┴────────────────┘              │
│                                    │                                        │
│                                    ▼                                        │
│                    ┌──────────────────────────────┐                        │
│                    │   List<SymbolInfo> EQUITY    │                        │
│                    │   (~2000 mã: Stock, ETF...)  │                        │
│                    └──────────────┬───────────────┘                        │
│                                   │                                         │
│   NEW FLOW (Additive):            │                                        │
│   ┌─────────────────────────────────────────────────────────────┐          │
│   │ TRY:                                                         │          │
│   │   ┌────────────────────┐    ┌────────────────────┐          │          │
│   │   │ /dr/stock-board    │    │ /dr/stock-price    │          │          │
│   │   │ (List derivatives) │    │ (Each derivative)  │          │          │
│   │   └─────────┬──────────┘    └─────────┬──────────┘          │          │
│   │             └────────────────────────┬┘                     │          │
│   │                                      ▼                      │          │
│   │             ┌──────────────────────────────────────┐        │          │
│   │             │   List<SymbolInfo> DERIVATIVES       │        │          │
│   │             │   (~4-8 mã: VN30F2501, VN30F2502...) │        │          │
│   │             │   WITH m="derivatives"               │        │          │
│   │             └──────────────┬───────────────────────┘        │          │
│   │                            │                                │          │
│   │ CATCH Exception:           │                                │          │
│   │   Log.warn("Derivatives init failed, continuing...")        │          │
│   │   Return empty list                                         │          │
│   └─────────────────────────────────────────────────────────────┘          │
│                                    │                                        │
│                                    ▼                                        │
│          ┌─────────────────────────────────────────────────┐               │
│          │         MERGE: EQUITY + DERIVATIVES             │               │
│          │         allSymbols = equityList + derivativeList│               │
│          └──────────────────────┬──────────────────────────┘               │
│                                 │                                           │
└─────────────────────────────────│───────────────────────────────────────────┘
                                  │
                                  ▼
                         [Continue existing flow]
                         Save to Redis, MongoDB, MinIO
```

#### 4.1.4 Code Changes - market-collector-lotte

**File: LotteApiService.java - New Methods**

```java
// NEW: Derivatives API methods
public CompletableFuture<List<DerivativeStockBoard>> getDerivativeStockBoard() {
    String url = baseUrl + "/tuxsvc/market/dr/stock-board";
    // POST with empty body, OAuth2 + API KEY
    return httpClient.post(url, "{}", DerivativeStockBoardResponse.class)
        .thenApply(response -> response.getDataList().getListItems());
}

public CompletableFuture<DerivativeStockPrice> getDerivativeStockPrice(String code) {
    String url = baseUrl + "/tuxsvc/market/dr/stock-price?code=" + code;
    // GET with OAuth2 + API KEY
    return httpClient.get(url, DerivativeStockPriceResponse.class)
        .thenApply(response -> response.getDataList());
}
```

**File: LotteApiSymbolInfoService.java - Modified**

```java
public CompletableFuture<List<SymbolInfo>> downloadSymbol() {
    // EXISTING CODE - Equity download (keep unchanged)
    CompletableFuture<List<SymbolInfo>> equityFuture = downloadEquitySymbols();
    
    // NEW CODE - Derivatives download (additive)
    CompletableFuture<List<SymbolInfo>> derivativesFuture = downloadDerivativeSymbols()
        .exceptionally(ex -> {
            log.warn("Derivatives download failed, continuing with equity only", ex);
            return Collections.emptyList();  // Graceful degradation
        });
    
    // Merge both lists
    return equityFuture.thenCombine(derivativesFuture, (equity, derivatives) -> {
        List<SymbolInfo> allSymbols = new ArrayList<>(equity);
        allSymbols.addAll(derivatives);
        return allSymbols;
    });
}

// NEW METHOD
private CompletableFuture<List<SymbolInfo>> downloadDerivativeSymbols() {
    return lotteApiService.getDerivativeStockBoard()
        .thenCompose(stockBoards -> {
            List<CompletableFuture<SymbolInfo>> futures = stockBoards.stream()
                .map(board -> lotteApiService.getDerivativeStockPrice(board.getCode())
                    .thenApply(price -> mergeDerivativeInfo(board, price)))
                .collect(Collectors.toList());
            
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()));
        });
}

// NEW METHOD
private SymbolInfo mergeDerivativeInfo(DerivativeStockBoard board, DerivativeStockPrice price) {
    SymbolInfo info = new SymbolInfo();
    
    // Identification
    info.setCode(board.getCode());
    info.setName(price.getName());
    info.setType(SymbolType.FUTURES);
    info.setMarket("derivatives");  // KEY: Identifier for derivatives
    info.setMarketType("VN30F");    // Or derive from code
    
    // Price data
    info.setOpen(board.getOpen());
    info.setHigh(board.getHigh());
    info.setLow(board.getLow());
    info.setLast(board.getLast());
    info.setChange(board.getChange());
    info.setRate(board.getChangeRate());
    info.setCeilingPrice(board.getCeiling());
    info.setFloorPrice(board.getFloor());
    info.setReferencePrice(board.getRefPrice());
    
    // Volume & Interest
    info.setTradingVolume(board.getVol());
    info.setOpenInterest(board.getOi());
    
    // BidOffer
    info.setBidOfferList(parseBidOfferFromBoard(board));
    info.setTotalBidVolume(board.getBid1Size() + board.getBid2Size() + board.getBid3Size());
    info.setTotalOfferVolume(board.getOffer1Size() + board.getOffer2Size() + board.getOffer3Size());
    
    // Foreigner
    info.setForeignerBuyVolume(board.getForeignBuyVol());
    info.setForeignerSellVolume(board.getForeignSellVol());
    
    // Derivatives-specific
    info.setBaseCode(price.getBaseCode());
    info.setFirstTradingDate(price.getFirstTrdDate());
    info.setLastTradingDate(price.getEndTrdDate());
    info.setRemainingDays(price.getRemainDate());
    info.setTheoryPrice(price.getTheoryPrice());
    info.setBasis(price.getTheoryBasis());
    
    return info;
}
```

### 4.2 SymbolInfo API - Giữ nguyên cơ chế hiện tại

#### 4.2.1 Nguyên tắc quan trọng

> **QUAN TRỌNG:** FE hiện tại đang sử dụng WebSocket để nhận giá real-time, KHÔNG sử dụng `/api/v2/market/symbol/latest`. 
> Để hạn chế thay đổi ở FE, cơ chế cho phái sinh PHẢI giống với cơ sở hiện tại.

#### 4.2.2 Cơ chế hoạt động (giữ nguyên cho cả cơ sở và phái sinh)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CƠ CHẾ HIỆN TẠI (Equity) - GIỮ NGUYÊN                    │
└─────────────────────────────────────────────────────────────────────────────┘

1. App Start:
   └─→ Download symbol_static.json (danh sách mã + thông tin tĩnh)
   
2. Real-time:
   └─→ Subscribe WebSocket channels: market.quote.{code}, market.bidoffer.{code}
   └─→ Nhận updates, merge vào local cache
   
3. API symbolInfo (nếu cần):
   └─→ GET /api/v2/market/symbolInfo?symbolList=[...]
   └─→ Data lấy từ Redis (đã được aggregate từ WebSocket)

┌─────────────────────────────────────────────────────────────────────────────┐
│                    CƠ CHẾ MỚI (Derivatives) - TƯƠNG TỰ                      │
└─────────────────────────────────────────────────────────────────────────────┘

1. App Start:
   └─→ Download symbol_static.json (bao gồm cả mã phái sinh với m="derivatives")
   
2. Real-time:
   └─→ Subscribe WebSocket channels: market.quote.dr.{code}, market.bidoffer.dr.{code}
   └─→ Nhận updates, merge vào local cache
   
3. API symbolInfo (nếu cần):
   └─→ GET /api/v2/market/symbolInfo?symbolList=[VN30F2501,...]
   └─→ Data lấy từ Redis (đã được aggregate từ WebSocket phái sinh)
```

#### 4.2.3 Yêu cầu chức năng

**FR-API-001**: API `/api/v2/market/symbolInfo` PHẢI hoạt động cho cả mã cơ sở và phái sinh.

**FR-API-002**: Data phái sinh trong API PHẢI được tổng hợp từ WebSocket (giống cơ sở).

**FR-API-003**: Response format cho phái sinh PHẢI tương tự cơ sở, chỉ thêm fields đặc thù.

**FR-API-004**: FE KHÔNG cần thay đổi cơ chế gọi API.

#### 4.2.4 Data Flow cho SymbolInfo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     DERIVATIVES DATA AGGREGATION                            │
│                   (Giống cơ chế equity hiện tại)                            │
└─────────────────────────────────────────────────────────────────────────────┘

Lotte DR WebSocket
    │
    ├─► auto.dr.qt (Quote)      ──► Kafka: quoteUpdateDR
    │                                    │
    │                                    ▼
    │                              realtime-v2
    │                                    │
    │                                    ▼
    │                           ┌────────────────────┐
    │                           │  Update SymbolInfo │
    │                           │  trong Redis       │
    │                           │  (aggregate data)  │
    │                           └────────┬───────────┘
    │                                    │
    └─► auto.dr.bo (BidOffer)   ──► Kafka: bidOfferUpdateDR
                                         │
                                         ▼
                                   realtime-v2
                                         │
                                         ▼
                                ┌────────────────────┐
                                │  Update SymbolInfo │
                                │  trong Redis       │
                                │  (aggregate data)  │
                                └────────┬───────────┘
                                         │
                                         ▼
                            ┌─────────────────────────────┐
                            │   Redis: SYMBOL_INFO        │
                            │   VN30F2501: {              │
                            │     s, m, t, c, ch, ra,     │
                            │     bb, bo, tb, to,         │
                            │     oi, bc, ed, rd...       │
                            │   }                         │
                            └─────────────┬───────────────┘
                                          │
                    ┌─────────────────────┴─────────────────────┐
                    ▼                                           ▼
         ┌──────────────────────┐                 ┌──────────────────────┐
         │  /api/v2/market/     │                 │  WebSocket ws-v2     │
         │  symbolInfo          │                 │  market.quote.dr.*   │
         │  (Read from Redis)   │                 │  market.bidoffer.dr.*│
         └──────────────────────┘                 └──────────────────────┘
```

#### 4.2.5 Response Format (tương tự cơ sở)

API `/api/v2/market/symbolInfo` sẽ trả về format giống cơ sở, chỉ thêm fields đặc thù phái sinh:

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

#### 4.2.6 Thay đổi cần thiết ở Backend

| Service | Thay đổi | Mô tả |
|---------|----------|-------|
| realtime-v2 | Consumer mới | Consume `quoteUpdateDR`, `bidOfferUpdateDR` từ Kafka |
| realtime-v2 | Update logic | Aggregate data vào SymbolInfo trong Redis |
| market-query-v2 | Không thay đổi | Đọc từ Redis như bình thường |

**Lưu ý:** market-query-v2 KHÔNG cần thay đổi vì nó chỉ đọc từ Redis. Data đã được aggregate bởi realtime-v2.

### 4.3 Real-time WebSocket Integration

#### 4.3.1 Yêu cầu chức năng

**FR-WS-001**: Hệ thống PHẢI subscribe Lotte DR WebSocket channels khi service start.

**FR-WS-002**: Hệ thống PHẢI parse message format của DR và transform về TradeX format.

**FR-WS-003**: Hệ thống PHẢI publish giá derivatives qua TradeX WebSocket với channel pattern riêng.

**FR-WS-004**: Nếu DR WebSocket fail, hệ thống equity WebSocket PHẢI không bị ảnh hưởng.

#### 4.3.2 Lotte DR WebSocket Channels

**Future Quote Channel (RMK-011)**

```
Subscribe: sub/pro.pub.auto.dr.qt./VN30F2501

Message Format (pipe-separated):
[0]  service: "pro.pub.auto.dr.qt"
[1]  success: "Y"
[2]  time: "103025"
[3]  code: "VN30F2501"
[4]  highTime: "094532"
[5]  lowTime: "101245"
[6]  open.value: "1275.0"
[7]  open.type: "2"
[8]  high.value: "1290.0"
[9]  high.type: "1"
[10] low.value: "1270.0"
[11] low.type: "4"
[12] last.value: "1285.5"
[13] last.type: "2"
[14] change.value: "12.5"
[15] changeRate: "0.98"
[16] averagePrice: "1280.0"
[17] referencePrice: "1273.0"
[18] value: "16000000000"
[19] volume: "125000"
[20] matchedVolume.value: "500"
[21] matchedVolume.type: "B"    // B=Buy, S=Sell
[22] bid.value: "1285.0"
[23] bid.type: "2"
[24] offer.value: "1285.5"
[25] offer.type: "2"
[26] bid_size: "1200"
[27] offer_size: "1000"
[28] total_bid_size: "5000"
[29] total_offer_size: "4500"
[30] total_bid_count: "150"
[31] total_offer_count: "120"
[32] foreignerBuySize: "5000"
[33] foreignerSellSize: "3000"
```

**Future Bid/Offer Channel (RMK-012)**

```
Subscribe: sub/pro.pub.auto.dr.bo./VN30F2501

Message Format (pipe-separated):
[0]  service: "pro.pub.auto.dr.bo"
[1]  success: "Y"
[2]  time: "103025"
[3]  code: "VN30F2501"
[4]  control_code: "O"           // Market status
[5]  project_open.value: "0"     // Expected price (ATO/ATC only)
[6]  project_open.type: "0"
[7]  bid.value: "1285.0"
[8]  bid.type: "2"
[9]  bidSize: "1200"
[10] offer.value: "1285.5"
[11] offer.type: "2"
[12] offerSize: "1000"
[13-72] 10 price levels (6 fields each)
[73] totalBidSize
[74] totalOfferSize
[75] bidOfferSizeDiff
```

#### 4.3.3 TradeX WebSocket Channel Design

**New Channels for Derivatives:**

| TradeX Channel | Source | Description |
|----------------|--------|-------------|
| `market.quote.dr.{code}` | dr.qt | Giá, KL, ĐTNN phái sinh |
| `market.bidoffer.dr.{code}` | dr.bo | Sổ lệnh 10 bước phái sinh |

**Alternative:** Sử dụng chung channel `market.quote.{code}` nhưng thêm field `m: "derivatives"` trong message.

```json
// Option 1: Separate channel (Recommended - ISOLATION)
// Channel: market.quote.dr.VN30F2501
{
  "s": "VN30F2501",
  "m": "derivatives",
  "c": 1285.5,
  "ch": 12.5,
  // ...
}

// Option 2: Shared channel with market identifier
// Channel: market.quote.VN30F2501
{
  "s": "VN30F2501",
  "m": "derivatives",    // Differentiator
  "c": 1285.5,
  // ...
}
```

**Recommendation:** Use **Option 1** (Separate channels) for:
- Clear isolation
- Easy to enable/disable
- No impact on existing equity subscriptions
- Clear logging and monitoring

#### 4.3.4 Implementation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                  DERIVATIVES WEBSOCKET DATA FLOW                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      LOTTE SECURITIES WEBSOCKET                              │
│                                                                             │
│   EQUITY (Existing)                    DERIVATIVES (New)                    │
│   ┌──────────┐  ┌──────────┐          ┌──────────┐  ┌──────────┐          │
│   │ auto.qt  │  │ auto.bo  │          │auto.dr.qt│  │auto.dr.bo│          │
│   └────┬─────┘  └────┬─────┘          └────┬─────┘  └────┬─────┘          │
└────────│─────────────│─────────────────────│─────────────│─────────────────┘
         │             │                     │             │
         ▼             ▼                     ▼             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       MARKET-COLLECTOR-LOTTE                                │
│                                                                             │
│  EQUITY Handler (Existing)            DR Handler (New - Isolated)           │
│  ┌────────────────────────┐          ┌────────────────────────┐            │
│  │ EquityQuoteHandler     │          │ DerivativeQuoteHandler │            │
│  │ EquityBidOfferHandler  │          │ DerivativeBidOfferHndlr│            │
│  └───────────┬────────────┘          └───────────┬────────────┘            │
│              │                                   │                          │
│              ▼                                   ▼                          │
│  ┌────────────────────────┐          ┌────────────────────────┐            │
│  │ Parse pipe-separated   │          │ Parse DR pipe-separated│            │
│  │ Transform to DTO       │          │ Transform to DR DTO    │            │
│  └───────────┬────────────┘          └───────────┬────────────┘            │
└──────────────│───────────────────────────────────│──────────────────────────┘
               │                                   │
               ▼                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              KAFKA                                          │
│  ┌──────────────┐ ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ │
│  │ quoteUpdate  │ │bidOfferUpdate  │ │quoteUpdateDR   │ │bidOfferUpdateDR│ │
│  │  (Equity)    │ │   (Equity)     │ │ (Derivatives)  │ │ (Derivatives)  │ │
│  └──────┬───────┘ └───────┬────────┘ └───────┬────────┘ └───────┬────────┘ │
└─────────│─────────────────│──────────────────│──────────────────│───────────┘
          │                 │                  │                  │
          ▼                 ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          REALTIME-V2 (Java)                                 │
│                                                                             │
│  EQUITY Consumers (Existing)          DR Consumers (New - Isolated)         │
│  ┌────────────────────────┐          ┌────────────────────────┐            │
│  │ QuoteService           │          │ DerivativeQuoteService │            │
│  │ BidOfferService        │          │ DerivativeBOService    │            │
│  └───────────┬────────────┘          └───────────┬────────────┘            │
│              │                                   │                          │
│              ▼                                   ▼                          │
│           Update SymbolInfo             Update SymbolInfo                   │
│           (type=STOCK,ETF...)           (type=FUTURES, m=derivatives)       │
│              │                                   │                          │
│              └───────────────────┬───────────────┘                          │
│                                  ▼                                          │
│                      marketRedisDao.setSymbolInfo()                         │
└──────────────────────────────────│──────────────────────────────────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │  Redis: realtime_mapSymbolInfo│
                    └──────────────┬───────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              WS-V2 (Node.js)                                │
│                                                                             │
│   EQUITY Channels (Existing)          DR Channels (New)                     │
│   ┌────────────────────────┐          ┌────────────────────────┐           │
│   │ market.quote.{code}    │          │ market.quote.dr.{code} │           │
│   │ market.bidoffer.{code} │          │ market.bidoffer.dr.{code}│          │
│   └────────────────────────┘          └────────────────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
                          ┌────────────────┐
                          │  NHSV PRO APP  │
                          └────────────────┘
```

#### 4.3.5 Code Changes - market-collector-lotte

**File: DerivativeWebSocketHandler.java (New)**

```java
@Component
public class DerivativeWebSocketHandler {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private static final String DR_QUOTE_TOPIC = "quoteUpdateDR";
    private static final String DR_BIDOFFER_TOPIC = "bidOfferUpdateDR";
    
    public void onDrQuoteMessage(String message) {
        try {
            String[] parts = message.split("\\|");
            
            DerivativeQuote quote = DerivativeQuote.builder()
                .code(parts[3])
                .time(parts[2])
                .open(parseDouble(parts[6]))
                .high(parseDouble(parts[8]))
                .low(parseDouble(parts[10]))
                .last(parseDouble(parts[12]))
                .change(parseDouble(parts[14]))
                .changeRate(parseDouble(parts[15]))
                .averagePrice(parseDouble(parts[16]))
                .referencePrice(parseDouble(parts[17]))
                .tradingValue(parseLong(parts[18]))
                .tradingVolume(parseLong(parts[19]))
                .matchingVolume(parseLong(parts[20]))
                .matchedBy(parts[21].equals("B") ? "BID" : "ASK")
                .totalBidVolume(parseLong(parts[28]))
                .totalOfferVolume(parseLong(parts[29]))
                .foreignerBuyVolume(parseLong(parts[32]))
                .foreignerSellVolume(parseLong(parts[33]))
                .market("derivatives")  // KEY IDENTIFIER
                .build();
            
            kafkaTemplate.send(DR_QUOTE_TOPIC, quote.getCode(), toJson(quote));
            
        } catch (Exception e) {
            log.error("Error processing DR quote message", e);
            // Don't throw - isolate error
        }
    }
    
    public void onDrBidOfferMessage(String message) {
        try {
            String[] parts = message.split("\\|");
            
            DerivativeBidOffer bidOffer = DerivativeBidOffer.builder()
                .code(parts[3])
                .time(parts[2])
                .controlCode(parts[4])
                .expectedPrice(parseDouble(parts[5]))
                .bidOfferList(parseBidOfferList(parts))  // 10 levels from [13] to [72]
                .totalBidVolume(parseLong(parts[73]))
                .totalOfferVolume(parseLong(parts[74]))
                .market("derivatives")
                .build();
            
            kafkaTemplate.send(DR_BIDOFFER_TOPIC, bidOffer.getCode(), toJson(bidOffer));
            
        } catch (Exception e) {
            log.error("Error processing DR bidoffer message", e);
        }
    }
}
```

---

## 5. Data Model Changes

### 5.1 SymbolInfo Model Enhancement

**File: tradex-common-java - SymbolInfo.java**

```java
@Data
public class SymbolInfo {
    // === EXISTING FIELDS (unchanged) ===
    private String code;
    private String name;
    private String nameEn;
    private String type;           // STOCK, ETF, CW, INDEX, FUTURES
    private String marketType;     // HOSE, HNX, UPCOM, VN30F
    
    // Price fields
    private Double open, high, low, last, change, rate;
    private Double ceilingPrice, floorPrice, referencePrice;
    
    // Volume fields
    private Long tradingVolume, tradingValue;
    private Long totalBidVolume, totalOfferVolume;
    private List<BidOfferItem> bidOfferList;
    
    // Foreigner fields
    private Long foreignerBuyVolume, foreignerSellVolume;
    private Long foreignerTotalRoom, foreignerCurrentRoom;
    
    // === NEW FIELDS FOR DERIVATIVES ===
    
    /**
     * Market identifier: "equity" (default) or "derivatives"
     * Used to differentiate between equity and derivatives symbols
     */
    private String market;
    
    /**
     * Open Interest - Number of outstanding contracts (Derivatives only)
     */
    private Long openInterest;
    
    /**
     * Base/Underlying asset code (e.g., "VN30" for VN30F2501)
     */
    private String baseCode;
    
    /**
     * First trading date of the contract
     */
    private String firstTradingDate;
    
    /**
     * Last trading date / Expiry date of the contract
     */
    private String lastTradingDate;
    
    /**
     * Number of days until expiry
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
}
```

### 5.2 New DTOs for Lotte DR API

**File: DerivativeStockBoard.java**

```java
@Data
public class DerivativeStockBoard {
    private String code;
    private Double last;
    private Double change;
    private Double changeRate;
    private Long vol;
    private Double ceiling;
    private Double floor;
    private Double refPrice;
    private Double open;
    private Double high;
    private Double low;
    private Double bid1, bid2, bid3;
    private Double offer1, offer2, offer3;
    private Long bid1Size, bid2Size, bid3Size;
    private Long offer1Size, offer2Size, offer3Size;
    private Long oi;  // Open Interest
    private Integer expDate;
    private String controlCode;
    private Long foreignBuyVol;
    private Long foreignSellVol;
}
```

**File: DerivativeStockPrice.java**

```java
@Data
public class DerivativeStockPrice {
    private String code;
    private String name;
    private Double ceiling, floor, open, high, low, last, change;
    private Double refPrice, averagePrice;
    private Long volume, amount;
    private List<BidOfferItem> bidOfferList;
    private Long totalVisBidSize, totalVisOfferSize;
    private Long openInterest;
    private String baseCode;
    private String firstTrdDate;
    private String endTrdDate;
    private Integer remainDate;
    private Double theoryPrice;
    private Double theoryBasis;
    private Long foreignBuyVol, foreignSellVol;
}
```

### 5.3 symbol_static.json Format Update

**Current Format (Equity):**
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

**New Format (Derivatives):**
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

**Field Mapping:**

| Field | Full Name | Description | Equity | Derivatives |
|-------|-----------|-------------|--------|-------------|
| s | symbol | Mã chứng khoán | ✅ | ✅ |
| m | market | Thị trường | HOSE/HNX/UPCOM | **derivatives** |
| n1 | name1 | Tên tiếng Việt | ✅ | ✅ |
| n2 | name2 | Tên tiếng Anh | ✅ | ✅ |
| t | type | Loại sản phẩm | STOCK/ETF/CW | **FUTURES** |
| re | reference | Giá tham chiếu | ✅ (VND) | ✅ (Điểm) |
| ce | ceiling | Giá trần | ✅ | ✅ |
| fl | floor | Giá sàn | ✅ | ✅ |
| lq | listedQty | KL niêm yết | ✅ | 0 |
| bc | baseCode | Mã cơ sở | ❌ | ✅ (VN30) |
| ed | endDate | Ngày đáo hạn | ❌ | ✅ (yyyyMMdd) |
| rd | remainDays | Số ngày còn lại | ❌ | ✅ |

**Sample Full File:**
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

---

## 6. API Specifications

### 6.1 New Lotte API Integration

| API ID | Endpoint | Method | Purpose |
|--------|----------|--------|---------|
| DRMKT-001 | `/tuxsvc/market/dr/stock-board` | POST | List all derivative symbols |
| DRMKT-002 | `/tuxsvc/market/dr/stock-price` | GET | Get single derivative detail |

### 6.2 TradeX API Updates

| API | Change Type | Description |
|-----|-------------|-------------|
| `/api/v2/market/symbol/latest` | Enhancement | Include derivatives in response |
| `/api/v2/market/symbol/staticInfo` | Enhancement | Include derivatives |

### 6.3 WebSocket Channel Updates

| Channel | Type | Description |
|---------|------|-------------|
| `market.quote.dr.{code}` | NEW | Derivatives quote updates |
| `market.bidoffer.dr.{code}` | NEW | Derivatives bid/offer updates |

---

## 7. Testing Strategy

### 7.1 Unit Tests

| Test Suite | Service | Coverage |
|------------|---------|----------|
| LotteApiServiceTest | market-collector-lotte | Lotte DR API calls |
| DerivativeMergeTest | market-collector-lotte | SymbolInfo merge logic |
| DerivativeWsParserTest | market-collector-lotte | WS message parsing |
| SymbolServiceTest | market-query-v2 | API filter logic |

### 7.2 Integration Tests

| Test | Components | Scenario |
|------|------------|----------|
| InitJobIntegration | collector → realtime → redis | Full init flow with derivatives |
| WsFlowIntegration | collector → kafka → realtime → ws | Real-time data flow |
| ApiIntegration | rest-proxy → market-query → redis | API response validation |

### 7.3 E2E Test Cases

| TC ID | Description | Expected |
|-------|-------------|----------|
| E2E-DR-001 | Init job với derivatives | symbol_static.json chứa derivatives với m="derivatives" |
| E2E-DR-002 | API /symbol/latest với derivatives | Response có cả equity và derivatives |
| E2E-DR-003 | WebSocket subscribe DR channel | Nhận real-time DR data |
| E2E-DR-004 | Init job khi Lotte DR API fail | Equity vẫn hoạt động, log warning |
| E2E-DR-005 | WS khi DR channel disconnect | Equity WS không ảnh hưởng |

### 7.4 Regression Tests

| Test | Purpose |
|------|---------|
| Equity init unchanged | Verify equity flow không bị ảnh hưởng |
| Equity WS unchanged | Verify equity real-time không đổi |
| API backward compatible | Old clients vẫn nhận được data đúng format |

---

## 8. Rollout Plan

### 8.1 Deployment Phases

| Phase | Environment | Duration | Actions |
|-------|-------------|----------|---------|
| Phase 0 | Dev | 1 week | Implement & unit test |
| Phase 1 | UAT | 1 week | Integration test |
| Phase 2 | Production (shadow) | 1 week | Run parallel, monitor |
| Phase 3 | Production (live) | - | Full rollout |

### 8.2 Feature Flags

```yaml
# Configuration
derivatives:
  enabled: true                    # Master switch
  initJob:
    enabled: true                  # Enable/disable DR in init job
    failSafe: true                 # Continue equity if DR fails
  websocket:
    enabled: true                  # Enable/disable DR WS subscription
    channels:
      quote: true
      bidOffer: true
  api:
    includeInLatest: true          # Include DR in /symbol/latest
```

### 8.3 Monitoring & Alerts

| Metric | Threshold | Alert |
|--------|-----------|-------|
| DR Init Success Rate | < 95% | Warning |
| DR WS Message Latency | > 500ms | Warning |
| DR API Error Rate | > 1% | Critical |
| Equity Performance Impact | > 5% | Critical |

### 8.4 Rollback Triggers

| Condition | Action |
|-----------|--------|
| Equity init fails | Disable DR in init job |
| Equity WS degraded | Disable DR WS subscription |
| API latency increases > 20% | Disable DR in API response |

---

## Appendix A: References

| Document | Location |
|----------|----------|
| Lotte DR API Specs | `Derivatives/Documentation/[API specs]Lotte_DR.md` |
| Lotte DR WebSocket | `Derivatives/Documentation/Websocket_DR_Lotte.md` |
| TradeX Init Job Knowledge | `TradeX Knowledge/init-job.md` |
| TradeX SymbolInfo API Knowledge | `TradeX Knowledge/symbol-info-api.md` |
| TradeX Market Data Channels | `TradeX Knowledge/market-data-channels.md` |

---

## Appendix B: Glossary

| Term | Definition |
|------|------------|
| **Derivatives** | Phái sinh - Hợp đồng tương lai VN30 |
| **Open Interest (OI)** | Số lượng hợp đồng mở chưa đóng |
| **Basis** | Chênh lệch giữa giá Futures và giá Spot |
| **Expiry Date** | Ngày đáo hạn của hợp đồng |
| **Control Code** | Mã trạng thái phiên giao dịch |

---

*Document End*
