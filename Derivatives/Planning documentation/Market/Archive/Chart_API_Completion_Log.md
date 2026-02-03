# ✅ Completion Summary: Chart API Issue Created

**Date:** February 3, 2026  
**Task:** Create implementation issue for Chart API derivatives support  
**Category:** Derivatives → Market Data  
**Status:** Complete

---

## 📋 What Was Created

### 1. **Main Issue Document**
**File:** `[ISSUE] Chart API Support for Derivatives.md`  
**Type:** Comprehensive implementation issue for dev team  
**Size:** 650+ lines

**Contains:**
- ✅ Business context and rationale
- ✅ Current vs target state comparison
- ✅ Technical background & data flow diagrams
- ✅ 4 detailed requirements (REQ-1 to REQ-4)
- ✅ 5 test scenarios with expected responses
- ✅ 3 implementation approaches with pros/cons
- ✅ Dependencies & blockers analysis
- ✅ Acceptance criteria checklist
- ✅ Timeline estimate (2.5-3.5 weeks)
- ✅ 6 key questions for dev team
- ✅ Reference documents

### 2. **Quick Brief Document**
**File:** `[BRIEF] Chart API for Derivatives.md`  
**Type:** One-page summary  
**Purpose:** Quick reference for stakeholders

**Contains:**
- Problem statement
- Current vs target behavior
- What needs to be done (3 items)
- Success criteria
- Effort estimate
- Key questions

### 3. **Updated Index**
**File:** `_index.md`  
**Type:** Market data documentation index  
**Updates:**
- Added new issue documents to table
- Updated status indicators
- Added implementation status section
- Added chart API details section

### 4. **Updated Executive Summary**
**File:** `00_EXECUTIVE_SUMMARY.md`  
**Updates:**
- Chart API marked with 📋 Issue Created status
- Added legend for status indicators

---

## 🎯 Issue Overview

### Problem Statement

API `/tradingview/history` currently only supports **Equity** symbols (VCB, VNM, HPG).  
It needs to support **Derivatives** symbols (VN30F2501, VN30F2502) WITHOUT requiring frontend changes.

### Solution Approach

**3-Step Implementation:**

1. **Collect Data**
   - Daily OHLCV from WebSocket/Lotte API
   - Minute OHLCV from WebSocket aggregation
   
2. **Store Data**
   - MongoDB `symbolDaily` (with derivatives marker)
   - Redis `SYMBOL_QUOTE_MINUTE_*` (derivatives keys)
   
3. **Update Query Logic**
   - Detect symbol type (equity vs derivatives)
   - Route to appropriate data source
   - Return same format response

### Key Technical Points

**Symbol Detection:**
```typescript
function isDerivativesSymbol(symbol: string): boolean {
    return /^VN30F\d{4}$/.test(symbol) || 
           symbolInfo?.symbolType === "FUTURES";
}
```

**Updated Service Logic:**
```typescript
queryTradingViewHistory(request) {
    const isDerivatives = this.isDerivativesSymbol(request.symbol);
    
    if (isDerivatives) {
        return this.getDerivativesHistory(request);  // NEW
    } else {
        return this.getEquityHistory(request);  // EXISTING
    }
}
```

---

## 📊 Requirements Summary

### REQ-1: Symbol Type Detection
- Auto-detect equity vs derivatives
- No manual configuration needed
- Pattern matching or SymbolInfo lookup

### REQ-2: Daily Data Collection
- Source: WebSocket or DRMKT-003 API
- Storage: MongoDB `symbolDaily`
- Marker: `market: "derivatives"` or `symbolType: "FUTURES"`

### REQ-3: Minute Data Collection
- Source: WebSocket real-time feed
- Storage: Redis + MongoDB
- Resolutions: 1, 5, 15, 30, 60 minutes

### REQ-4: Query Logic Update
- Service: `market-query-v2/FeedService.ts`
- Add derivatives data source routing
- Keep equity logic unchanged (no regression)

---

## 🧪 Test Scenarios Provided

| Test | Description | Expected |
|------|-------------|----------|
| TC-1 | Daily chart for VN30F2501 | Full OHLCV data, D resolution |
| TC-2 | 1-minute chart for VN30F2501 | Minute candles, 60s intervals |
| TC-3 | Weekly/Monthly aggregation | Computed from daily |
| TC-4 | Invalid symbol | "no_data" response |
| TC-5 | Equity regression test | VCB still works |

---

## 📅 Timeline Estimate

| Phase | Tasks | Duration |
|-------|-------|----------|
| **Phase 1** | Data Collection (daily + minute) | 3-5 days |
| **Phase 2** | Storage setup (MongoDB/Redis) | 2-3 days |
| **Phase 3** | Query logic update | 2-3 days |
| **Phase 4** | Testing (functional, performance) | 3-4 days |
| **Phase 5** | UAT | 2-3 days |

**Total:** 12-18 working days (2.5-3.5 weeks)

---

## ❓ Key Questions for Dev Team

1. **Historical Data:** Does Lotte provide daily historical data for derivatives?
2. **Data Retention:** Same policy as equity (7 days Redis, unlimited MongoDB)?
3. **Storage Strategy:** Separate collections vs same collection with marker?
4. **Symbol Detection:** Pattern matching vs SymbolInfo lookup?
5. **Backward Compatibility:** Any old derivatives symbol formats to support?
6. **Performance:** Current equity baseline? Target for derivatives?

---

## ✅ Acceptance Criteria

### Functional
- [ ] Works for all derivatives symbols (VN30F2501, VN30F2502, etc.)
- [ ] Supports all resolutions (D, W, M, 1, 5, 15, 30, 60)
- [ ] Response format identical to equity (TradingView format)
- [ ] Equity functionality unchanged (no regression)

### Data Quality
- [ ] Daily data available for 3-6 months history
- [ ] Minute data available for 3-7 days
- [ ] Real-time updates working
- [ ] No gaps in timestamps

### Performance
- [ ] API response < 500ms
- [ ] No impact on equity chart performance
- [ ] Redis cache hit rate > 90%

---

## 📁 File Locations

```
/Derivatives/Planning documentation/Market data/
├── _index.md                                    ← Updated!
├── 00_EXECUTIVE_SUMMARY.md                      ← Updated!
├── [ISSUE] Chart API Support for Derivatives.md ← NEW! (Main issue)
├── [BRIEF] Chart API for Derivatives.md         ← NEW! (Quick ref)
├── 06_CHART_API_REQUIREMENTS.md                 (Detailed technical spec)
└── ... (other planning docs)
```

---

## 🔗 Related Documentation

| Document | Purpose |
|----------|---------|
| `06_CHART_API_REQUIREMENTS.md` | Detailed technical specifications |
| `02_BE_REQUIREMENTS_SPEC.md` | Overall backend requirements |
| `04_USE_CASES_AND_TEST_SCENARIOS.md` | General test scenarios |
| `/Derivatives/Documentation/[API specs]Lotte_DR.md` | Lotte DRMKT-003 API |

---

## 👥 Stakeholders

| Role | Next Action |
|------|-------------|
| **BA** | ✅ Issue created, ready for handoff |
| **PM** | Review and prioritize in backlog |
| **Backend Lead** | Review issue, confirm approach, create technical tasks |
| **Frontend** | No action needed (API compatible) |
| **QA** | Review test scenarios, prepare test plan |

---

## 🎉 Summary

Successfully created comprehensive implementation issue for enabling Chart API support for Derivatives symbols:

✅ **Business context** clearly explained  
✅ **Technical approach** detailed with code examples  
✅ **Requirements** broken down into 4 clear REQs  
✅ **Test scenarios** provided with expected responses  
✅ **Timeline** estimated with phase breakdown  
✅ **Questions** documented for dev team clarification  
✅ **Documentation** properly organized and indexed  

**The issue is ready for dev team review and implementation!** 🚀

---

## 📝 Notes

### Key Insight from Analysis

API `/tradingview/history` được tổng hợp từ:
- **Daily data:** MongoDB `symbolDaily` collection (từ VietStock/VNDirect cho equity)
- **Minute data:** Redis `SYMBOL_QUOTE_MINUTE_*` + MongoDB `symbolQuoteMinutes` (từ WebSocket cho equity)

Cho derivatives, cần enable cùng data sources này nhưng từ Lotte WebSocket và DRMKT-003 API.

### Implementation Philosophy

1. **ISOLATION:** Không động vào equity flow
2. **DETECTION:** Tự động phát hiện loại symbol
3. **ROUTING:** Route đến data source phù hợp
4. **COMPATIBILITY:** Giữ nguyên response format

---

**Document Status:** Complete  
**Ready for:** Dev Team Review  
**Next Step:** Backend Lead assigns to developer
