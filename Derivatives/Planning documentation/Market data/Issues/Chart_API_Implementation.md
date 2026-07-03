# Chart API Implementation for Derivatives

**Issue Type:** Feature Request / Enhancement  
**Priority:** High  
**Component:** Market Data - Chart  
**Related Module:** `market-query-v2`, `realtime-v2`, `market-collector-lotte`  
**Created:** February 3, 2026  
**Status:** 📋 Ready for Dev Team Review

---

## 📋 Executive Summary

### Problem Statement

API `/tradingview/history` currently only supports **Equity** symbols (VCB, VNM, HPG).  
Need to support **Derivatives** symbols (VN30F2501, VN30F2502, etc.) WITHOUT requiring frontend changes.

### Current vs Target Behavior

**Current:**
```
GET /tradingview/history?symbol=VCB&resolution=D
✅ Works for Equity (VCB, VNM, HPG)
❌ Doesn't work for Derivatives (VN30F2501)
```

**Target:**
```
GET /tradingview/history?symbol=VN30F2501&resolution=D
✅ Should work for Derivatives
✅ Still works for Equity
```

### Solution Approach

**3-Step Implementation:**

1. **Collect Data** - Daily/minute OHLCV from WebSocket + Lotte DRMKT-003 API
2. **Store Data** - MongoDB `symbolDaily` + Redis `SYMBOL_QUOTE_MINUTE_*` with derivatives marker  
3. **Update Query Logic** - Auto-detect symbol type and route to appropriate data source

### Timeline & Effort

**Total Estimate:** 2.5-3.5 weeks (12-18 working days)

| Phase | Duration |
|-------|----------|
| Data collection | 3-5 days |
| Storage setup | 2-3 days |
| Query logic | 2-3 days |
| Testing | 3-4 days |
| UAT | 2-3 days |

### Key Success Criteria

- [ ] Chart works for all derivatives symbols (VN30F2501, VN30F2502, etc.)
- [ ] Supports all resolutions: D, W, M, 1, 5, 15, 30, 60 minutes
- [ ] Response format identical to equity (TradingView format)
- [ ] Equity charts still work (no regression)
- [ ] API response time < 500ms

---

## 📊 Business Context

### Current Situation

Frontend hiện đang sử dụng API `GET /tradingview/history` để hiển thị chart (candlestick) cho **Equity** (cổ phiếu) trên TradingView library. 

API này **chưa hỗ trợ** dữ liệu **Derivatives** (Phái sinh).

### Business Need

Để hoàn thiện tính năng Derivatives trên app, cần enable chart cho các mã phái sinh (VN30F2501, VN30F2502, etc.) mà **không yêu cầu FE thay đổi cách gọi API**.

### Expected Outcome

User có thể xem chart cho mã phái sinh tương tự như cách xem chart cho cổ phiếu hiện tại.

---

## 🔍 Technical Background

### Current Data Flow (Equity Only)

```
Client Request
    ↓
GET /tradingview/history?symbol=VCB&resolution=D
    ↓
rest-proxy → Kafka → market-query-v2
    ↓
FeedService.queryTradingViewHistory()
    ↓
    ├─ Resolution D/W/M → getDailyPeriodHistory()
    │   └─ Query MongoDB: symbolDaily collection (Equity data)
    │   └─ Query Redis: SYMBOL_DAILY (today)
    │
    └─ Resolution 1/5/15/30/60 → getQuoteMinuteHistory()
        └─ Query Redis: SYMBOL_QUOTE_MINUTE_* (Equity data)
        └─ Query MongoDB: symbolQuoteMinutes (Equity data)
```

### Required Data Flow (Support Both)

```
Client Request
    ↓
GET /tradingview/history?symbol=VN30F2501&resolution=D
    ↓
rest-proxy → Kafka → market-query-v2
    ↓
FeedService.queryTradingViewHistory()
    ↓
[NEW] Detect symbol type (Equity vs Derivatives)
    ↓
    ├─ If Equity → Use existing logic (no change)
    │
    └─ If Derivatives → Use derivatives data source
        ↓
        ├─ Resolution D/W/M → Query derivatives daily data
        │   └─ MongoDB: symbolDaily (with derivatives marker)
        │   └─ Redis: SYMBOL_DAILY (derivatives)
        │
        └─ Resolution 1/5/15/30/60 → Query derivatives minute data
            └─ Redis: SYMBOL_QUOTE_MINUTE_* (derivatives)
            └─ MongoDB: symbolQuoteMinutes (derivatives)
```

---

## 📝 Detailed Requirements

### REQ-1: Symbol Type Detection

**Description:** Automatically detect whether a symbol is Equity or Derivatives

**Implementation Area:** `market-query-v2/src/services/FeedService.ts`

**Logic:**
```typescript
function isDerivativesSymbol(symbol: string): boolean {
    // Option 1: Pattern matching
    if (/^VN30F\d{4}$/.test(symbol)) {
        return true;
    }
    
    // Option 2: SymbolInfo lookup
    const symbolInfo = await cacheService.getSymbolInfo(symbol);
    return symbolInfo?.symbolType === "FUTURES";
}
```

**Acceptance Criteria:**
- [x] Can detect VN30F2501, VN30F2502, etc. as derivatives
- [x] Can detect VCB, VNM, HPG, etc. as equity
- [x] Detection method is efficient (no unnecessary DB queries)

---

### REQ-2: Daily Data Collection for Derivatives

**Description:** Collect and store daily OHLCV data for derivatives symbols

**Implementation Area:** `market-collector-lotte` (Java)

**Data Source Options:**
- **Option A:** WebSocket real-time data → aggregate end-of-day
- **Option B:** Lotte DRMKT-003 API (minutely-derivatives) → aggregate to daily
- **Option C:** (If available) Lotte daily history API

**Storage:**
- Collection: `symbolDaily` (existing collection)
- Add marker field: `market: "derivatives"` or `symbolType: "FUTURES"`
- Same schema as equity daily data

**Acceptance Criteria:**
- [x] Daily data for derivatives stored in MongoDB `symbolDaily`
- [x] Data includes: date, open, high, low, close, volume, value
- [x] Can distinguish derivatives data from equity data
- [x] Init job collects historical daily data (last 3-6 months)
- [x] Daily job updates yesterday's data

---

### REQ-3: Minute Data Collection for Derivatives

**Description:** Collect and store minute OHLCV data for derivatives symbols

**Implementation Area:** `realtime-v2` (Java)

**Data Source:** WebSocket real-time feed (already collecting via `market-collector-lotte`)

**Storage:**
- Redis: `SYMBOL_QUOTE_MINUTE_{resolution}` (e.g., `SYMBOL_QUOTE_MINUTE_1`, `SYMBOL_QUOTE_MINUTE_5`)
- MongoDB: `symbolQuoteMinutes` collection
- Same structure as equity minute data

**Acceptance Criteria:**
- [x] Minute data for derivatives stored in Redis (resolutions: 1, 5, 15, 30, 60)
- [x] Data persisted to MongoDB `symbolQuoteMinutes` daily
- [x] Minute candles computed correctly from WebSocket ticks
- [x] Can query last 3-7 days of minute history

---

### REQ-4: Query Logic Update in FeedService

**Description:** Update `FeedService.queryTradingViewHistory()` to support derivatives

**Implementation Area:** `market-query-v2/src/services/FeedService.ts`

**Current Code:**
```typescript
public async queryTradingViewHistory(request: ITradingViewHistoryRequest): Promise<TradingViewHistoryResponse> {
    validateRequest(request, tradingViewHistoryRequestValidator);
    
    if (RESOLUTION_MINUTE.includes(request.resolution)) {
        return this.getQuoteMinuteHistory(request);  // Only queries equity data
    } else {
        return this.getDailyPeriodHistory(request);  // Only queries equity data
    }
}
```

**Required Changes:**
```typescript
public async queryTradingViewHistory(request: ITradingViewHistoryRequest): Promise<TradingViewHistoryResponse> {
    validateRequest(request, tradingViewHistoryRequestValidator);
    
    // [NEW] Detect symbol type
    const isDerivatives = await this.isDerivativesSymbol(request.symbol);
    
    if (RESOLUTION_MINUTE.includes(request.resolution)) {
        if (isDerivatives) {
            return this.getDerivativesQuoteMinuteHistory(request);  // [NEW] Derivatives logic
        } else {
            return this.getQuoteMinuteHistory(request);  // Existing equity logic
        }
    } else {
        if (isDerivatives) {
            return this.getDerivativesDailyPeriodHistory(request);  // [NEW] Derivatives logic
        } else {
            return this.getDailyPeriodHistory(request);  // Existing equity logic
        }
    }
}
```

**Acceptance Criteria:**
- [x] API works for equity symbols (no regression)
- [x] API works for derivatives symbols (new feature)
- [x] Response format unchanged (TradingView format)
- [x] Error handling for unsupported symbols
- [x] Performance acceptable (< 500ms for typical queries)

---

## 🧪 Test Scenarios

### Test Case 1: Daily Chart for Derivatives

**Request:**
```
GET /tradingview/history?symbol=VN30F2501&resolution=D&from=1704067200&to=1738713600
```

**Expected Response:**
```json
{
  "s": "ok",
  "t": [1704067200, 1704153600, 1704240000, ...],
  "c": [1285.5, 1290.3, 1288.0, ...],
  "o": [1280.0, 1285.5, 1289.5, ...],
  "h": [1290.0, 1295.0, 1293.0, ...],
  "l": [1278.0, 1283.0, 1286.0, ...],
  "v": [12500, 15800, 14200, ...]
}
```

**Validation:**
- Status: "ok"
- Data arrays have same length
- Timestamps in ascending order
- OHLC values realistic (high >= open/close/low, low <= open/close/high)

---

### Test Case 2: 1-Minute Chart for Derivatives

**Request:**
```
GET /tradingview/history?symbol=VN30F2501&resolution=1&from=1738656000&to=1738742400
```

**Expected Response:**
```json
{
  "s": "ok",
  "t": [1738656060, 1738656120, 1738656180, ...],  // Every 60 seconds
  "c": [1285.5, 1285.8, 1286.0, ...],
  "o": [1285.3, 1285.5, 1285.9, ...],
  "h": [1285.8, 1286.0, 1286.2, ...],
  "l": [1285.2, 1285.4, 1285.8, ...],
  "v": [120, 150, 180, ...]
}
```

**Validation:**
- 1-minute intervals (60 seconds between timestamps)
- Real-time data available for current trading day
- Historical data available for past 3-7 days

---

### Test Case 3: Weekly/Monthly Chart for Derivatives

**Request (Weekly):**
```
GET /tradingview/history?symbol=VN30F2501&resolution=W&from=1704067200&to=1738713600
```

**Expected Response:**
```json
{
  "s": "ok",
  "t": [1704067200, 1704672000, 1705276800, ...],  // Weekly timestamps
  "c": [1290.3, 1295.8, 1292.0, ...],
  "o": [1280.0, 1290.5, 1295.5, ...],
  "h": [1295.0, 1298.0, 1296.0, ...],
  "l": [1278.0, 1289.0, 1291.0, ...],
  "v": [125000, 158000, 142000, ...]
}
```

**Validation:**
- Weekly data computed from daily data
- Same logic as equity weekly/monthly

---

### Test Case 4: Symbol Not Found

**Request:**
```
GET /tradingview/history?symbol=INVALID123&resolution=D&from=1704067200&to=1738713600
```

**Expected Response:**
```json
{
  "s": "no_data",
  "nextTime": null
}
```

---

### Test Case 5: Equity Still Works (Regression Test)

**Request:**
```
GET /tradingview/history?symbol=VCB&resolution=D&from=1704067200&to=1738713600
```

**Expected Response:**
```json
{
  "s": "ok",
  "t": [...],
  "c": [...],
  "o": [...],
  "h": [...],
  "l": [...],
  "v": [...]
}
```

**Validation:**
- Equity chart still works as before
- No performance degradation
- No data inconsistencies

---

## 💡 Implementation Approaches

### Approach 1: Separate Collections (Recommended)

**Pros:**
- Clear separation of equity vs derivatives data
- Easy to query and maintain
- No risk of mixing data

**Cons:**
- Need to update query logic to check both sources
- Slightly more code

---

### Approach 2: Same Collections with Marker Field

**Pros:**
- Reuse existing collections and queries
- Less code duplication
- Unified data model

**Cons:**
- Need to add filters to all queries
- Risk of mixing data if marker not set correctly

---

### Approach 3: Unified Collection + Smart Detection

**Pros:**
- Most flexible
- Can handle future instrument types (options, bonds, etc.)
- Single source of truth

**Cons:**
- Most complex implementation
- Need robust symbol type detection

---

## 📌 Dependencies & Blockers

### Dependencies

1. **Derivatives WebSocket Data** - Must be already flowing and stable
   - Status: ✅ Already implemented
   
2. **SymbolInfo for Derivatives** - Must have derivatives symbols in cache
   - Status: ✅ Already implemented in Init Job
   
3. **Lotte API Access** - DRMKT-003 API available
   - Status: ✅ Available

### Potential Blockers

1. **Historical Data Availability**
   - Question: Does Lotte provide historical daily data for derivatives?
   - If NO: Need to compute from minute data or wait to accumulate history
   
2. **Data Retention**
   - Question: How long should we retain minute data for derivatives?
   - Equity: Currently 7 days in Redis, historical in MongoDB
   
3. **Performance**
   - Question: Will querying both equity and derivatives slow down the API?
   - Need to test with production load

---

## ❓ Key Questions for Dev Team

1. **Historical Data:** Does Lotte provide daily historical data for derivatives? Or do we need to aggregate from minute data?

2. **Data Retention:** Should we use same retention policy as equity (7 days Redis, unlimited MongoDB)?

3. **Storage Strategy:** Separate collections vs same collection with marker field - which approach do you prefer?

4. **Symbol Detection:** Should we detect by pattern (`VN30F\d{4}`) or by SymbolInfo lookup? Which is more reliable?

5. **Backward Compatibility:** Do we need to support old derivatives symbols format (if any)?

6. **Performance:** Current equity query performance baseline? What's acceptable for derivatives?

---

## ✅ Acceptance Criteria (Summary)

### Functional Requirements

- [ ] Chart API works for all derivatives symbols (VN30F2501, VN30F2502, etc.)
- [ ] Supports all resolutions: D, W, M, 1, 5, 15, 30, 60
- [ ] Response format identical to equity (TradingView format)
- [ ] Equity chart functionality unchanged (no regression)
- [ ] Symbol type detection is automatic and accurate

### Data Requirements

- [ ] Daily data available for last 3-6 months
- [ ] Minute data available for last 3-7 days
- [ ] Real-time updates working (WebSocket → Redis → API)
- [ ] Data quality: OHLC values logical, no gaps in timestamps

### Performance Requirements

- [ ] API response time < 500ms for typical queries
- [ ] No impact on equity chart performance
- [ ] Redis cache hit rate > 90% for recent data

### Error Handling

- [ ] Graceful handling of unsupported symbols
- [ ] Proper error messages for invalid parameters
- [ ] Fallback behavior if derivatives data not available

---

## 📚 Reference Documents

| Document | Purpose |
|----------|---------|
| [../Specifications/Chart_API_Spec.md](../Specifications/Chart_API_Spec.md) | Detailed technical spec |
| [../Planning/03_Technical_Requirements.md](../Planning/03_Technical_Requirements.md) | Overall technical requirements |
| [../../../Documentation/Lotte_DR_API_Specs.md](../../../Documentation/Lotte_DR_API_Specs.md) | Lotte DRMKT-003 API |

---

## 👥 Stakeholders

| Role | Name | Responsibility |
|------|------|----------------|
| **BA** | [BA Name] | Requirements, acceptance criteria |
| **PM** | [PM Name] | Priority, timeline, business validation |
| **Backend Lead** | [Dev Name] | Technical design, implementation |
| **Frontend** | [FE Name] | Integration testing, chart validation |
| **QA** | [QA Name] | Test scenarios, regression testing |

---

**Issue Status:** 📋 Ready for Dev Review  
**Created:** February 3, 2026  
**Last Updated:** February 3, 2026  
**Next Step:** Backend Lead assigns to developer
