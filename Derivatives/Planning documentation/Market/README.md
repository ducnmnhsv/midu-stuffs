# Derivatives Market Data Integration

> **Module:** Market Data  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** February 3, 2026  
> **Status:** Planning Complete + Implementation Issues Created

---

## 📋 Quick Navigation

| Section | Description |
|---------|-------------|
| [Overview](#overview) | Business context and architecture |
| [Implementation Status](#implementation-status) | Current progress and roadmap |
| [Documentation Map](#documentation-map) | All planning docs and specs |
| [Active Issues](#active-issues) | Ready-to-implement tasks |
| [How to Use](#how-to-use) | Guide for different roles |

---

## 🎯 Overview

### Mission

Bổ sung **hiển thị thông tin giá phái sinh** (Futures) vào hệ thống TradeX Backend với 2 nguyên tắc:

1. **ISOLATION** - Không ảnh hưởng hệ thống equity đang chạy ổn định
2. **MINIMIZE FE CHANGES** - Giữ nguyên cơ chế FE, data phái sinh được aggregate từ WebSocket như cơ sở

### Scope

| In Scope | Out of Scope |
|----------|--------------|
| ✅ Init job lấy mã phái sinh | ❌ Order/Trading API (see `/Orders/` folder) |
| ✅ SymbolInfo API (aggregate từ WS) | ❌ Account derivatives |
| ✅ Real-time WebSocket | ❌ P&L calculation |
| ✅ symbol_static.json | ❌ Mobile app changes |
| 📋 **Chart API (TradingView history)** | ❌ Thay đổi flow FE |

**Legend:** ✅ Completed | 📋 Issue Created | ❌ Out of Scope

---

## 🏗️ Architecture

### Data Flow

```
Lotte API/WebSocket
    ↓
market-collector-lotte (Java)
    ↓ Kafka Topics
    ├─ quoteUpdate (equity)
    └─ quoteUpdateDR (derivatives) ← New!
    ↓
realtime-v2 (Java)
    ↓ Redis: realtime_mapSymbolInfo
    ├─ SYMBOL_INFO (equity)
    └─ SYMBOL_INFO (derivatives) ← Merged!
    ↓
    ├─ market-query-v2 → /symbol/latest API
    └─ ws-v2 → WebSocket channels
```

### Key Design Principles

#### 1. ISOLATION Principle

Derivatives implementation KHÔNG được ảnh hưởng equity flow:
- Separate Kafka topics (`quoteUpdateDR`)
- Separate WebSocket handlers
- Graceful degradation if derivatives fail

#### 2. Symbol Type Detection

Phái sinh được phân biệt bằng field `t` (type) = `"FUTURES"` và `m` = `"derivatives"`. **Hai loại** hợp đồng được phân biệt bằng field `dc` (derivative category), suy từ mã hợp đồng (ký tự thứ 3):

| Loại | Mã (quy tắc) | `dc` | Ví dụ |
|------|--------------|------|--------|
| HĐ tương lai chỉ số | 41**I**xxxxxx | `"INDEX"` | VN30F... |
| HĐTL trái phiếu CP | 41**B**xxxxxx | `"BOND"` | 41B... |

```json
{
  "s": "VN30F2501",
  "t": "FUTURES",
  "m": "derivatives",
  "dc": "INDEX",     ← INDEX | BOND (FE dùng để hiển thị index name)
  "c": 1285.5,
  ...
}
```

#### 3. Data Aggregation Strategy

Giống cơ sở (equity):
- SymbolInfo từ WebSocket (NOT từ Lotte REST API)
- Real-time updates → Redis
- Historical data → MongoDB
- Chart data từ aggregated candles

---

## 📊 Implementation Status

### ✅ Completed Features

| Feature | Status | Services | Documents |
|---------|--------|----------|-----------|
| WebSocket Integration | ✅ Live | market-collector-lotte, realtime-v2 | Planning/01, Specs/WebSocket |
| SymbolInfo API | ✅ Live | market-query-v2, ws-v2 | Specs/SymbolInfo_API |
| Init Job (symbol_static.json) | ✅ Live | market-collector-lotte | Planning/01 |
| Real-time Quote/BidOffer | ✅ Live | ws-v2 | Specs/WebSocket |
| Symbol Detection (t field) | ✅ Live | All services | Planning/03 |

### 📋 Pending Implementation

| Feature | Status | Issue | Priority | Estimate |
|---------|--------|-------|----------|----------|
| **Chart API (TradingView)** | 📋 Issue Created | [Issues/Chart_API_Implementation](#chart-api) | High | 2.5-3.5 weeks |

### 🔮 Future Enhancements

| Feature | Priority | Notes |
|---------|----------|-------|
| Order APIs | High | See `/Orders/` folder |
| Account APIs | Medium | Future scope |
| P&L Calculation | Low | Future scope |

---

## 📚 Documentation Map

### Planning & Requirements

Located in `Planning/` folder:

| # | Document | Type | Audience | Description |
|---|----------|------|----------|-------------|
| 01 | [Integration Plan](./Planning/01_Integration_Plan.md) | Planning | Architect, BE Lead | Detailed integration roadmap |
| 02 | [Business Requirements](./Planning/02_Business_Requirements.md) | BRD | PM, BA, Stakeholders | Business requirements document |
| 03 | [Technical Requirements](./Planning/03_Technical_Requirements.md) | Spec | BE Developers | High-level technical requirements |
| 04 | [Use Cases & Testing](./Planning/04_Use_Cases_Testing.md) | Testing | BA, QA Team | Test scenarios and acceptance criteria |

### Technical Specifications

Located in `Specifications/` folder:

| Document | Focus Area | Target Audience |
|----------|------------|-----------------|
| [SymbolInfo API](./Specifications/SymbolInfo_API.md) | SymbolInfo endpoint details | BE Developers |
| [WebSocket Integration](./Specifications/WebSocket_Integration.md) | WS channels, data flow | BE Developers |
| [Chart API Spec](./Specifications/Chart_API_Spec.md) | TradingView history API | BE Developers |
| [Data Storage](./Specifications/Data_Storage.md) | MongoDB/Redis schemas | BE Developers, DBA |

### Active Issues

Located in `Issues/` folder:

| Issue | Priority | Status | Estimate |
|-------|----------|--------|----------|
| [Chart API Implementation](./Issues/Chart_API_Implementation.md) | High | 📋 Ready | 2.5-3.5 weeks |

---

## 🚀 Active Issues

### <a name="chart-api"></a>Chart API Implementation

**Issue:** [Issues/Chart_API_Implementation.md](./Issues/Chart_API_Implementation.md)

**Problem:**  
API `/tradingview/history` currently only supports Equity symbols. Need to support Derivatives symbols (VN30F2501, etc.) WITHOUT frontend changes.

**Solution Approach:**
1. **Collect Data** - Daily/minute OHLCV from WebSocket + Lotte DRMKT-003 API
2. **Store Data** - MongoDB `symbolDaily` + Redis `SYMBOL_QUOTE_MINUTE_*` with derivatives marker
3. **Update Query Logic** - Auto-detect symbol type and route to appropriate data source

**Requirements:**
- Support all resolutions: D, W, M, 1, 5, 15, 30, 60 minutes
- Response format identical to equity (TradingView format)
- No frontend changes required
- Performance: API response < 500ms

**Timeline:** 2.5-3.5 weeks (12-18 working days)

**Status:** 📋 Ready for Dev Team Review

---

## 🧪 Testing Strategy

### Test Levels

1. **Unit Tests** - Individual service methods
2. **Integration Tests** - API endpoints with real data
3. **Performance Tests** - Load testing, latency benchmarks
4. **UAT** - Business validation with real users

### Key Test Scenarios

| Priority | Scenario | Document Reference |
|----------|----------|-------------------|
| P0 | Real-time quote updates | Planning/04 |
| P0 | SymbolInfo API accuracy | Planning/04 |
| P0 | Chart D/W/M resolutions | Issues/Chart_API |
| P0 | Chart minute resolutions | Issues/Chart_API |
| P0 | Symbol type detection | Planning/03 |
| P0 | No equity regression | All documents |

---

## 👥 How to Use This Documentation

### For BA/PM

**Start Here:**
1. Read [Overview](#overview) section above
2. Check [Implementation Status](#implementation-status) for current progress
3. Review active issues in [Issues/](./Issues/) folder
4. Use [Planning/04_Use_Cases_Testing.md](./Planning/04_Use_Cases_Testing.md) for UAT

**When Creating New Issues:**
- Follow structure in [Issues/Chart_API_Implementation.md](./Issues/Chart_API_Implementation.md)
- Include business context, requirements, test scenarios
- Estimate timeline with phase breakdown

### For Developers

**Start Here:**
1. Read [Architecture](#architecture) section above
2. Review [Planning/03_Technical_Requirements.md](./Planning/03_Technical_Requirements.md)
3. Check [Specifications/](./Specifications/) folder for detailed specs
4. Implement using [Issues/](./Issues/) as guide

**For Chart API Implementation:**
- Main issue: [Issues/Chart_API_Implementation.md](./Issues/Chart_API_Implementation.md)
- Technical spec: [Specifications/Chart_API_Spec.md](./Specifications/Chart_API_Spec.md)
- Data storage: [Specifications/Data_Storage.md](./Specifications/Data_Storage.md)

### For QA

**Start Here:**
1. Study [Planning/04_Use_Cases_Testing.md](./Planning/04_Use_Cases_Testing.md)
2. Add test cases from [Issues/](./Issues/) folder
3. Follow [Testing Strategy](#testing-strategy) above

**Test Coverage:**
- All test scenarios marked P0 must be covered
- Regression tests for equity functionality
- Performance benchmarks documented

---

## 📦 Related Folders

| Folder | Content | Status |
|--------|---------|--------|
| `/Derivatives/Planning documentation/Orders/` | Order APIs (Buy/Sell/Cancel/Modify) | ✅ Planning Complete |
| `/Derivatives/Documentation/` | Lotte API specifications | Reference |
| `/TradeX Knowledge/` | Cross-market knowledge base | Reference |

---

## 👥 Stakeholders

| Role | Responsibility |
|------|----------------|
| **BA** | Requirements, issue creation, acceptance criteria |
| **PM** | Priority, roadmap, business validation |
| **Backend Lead** | Technical design, code review, implementation |
| **Frontend** | Integration testing, UI validation |
| **QA** | Test execution, bug tracking, regression testing |

---

## 📅 Timeline

| Phase | Duration | Status |
|-------|----------|--------|
| Planning & Documentation | 2 weeks | ✅ Complete (Jan 2026) |
| WebSocket Implementation | 3 weeks | ✅ Complete (Jan 2026) |
| SymbolInfo API | 2 weeks | ✅ Complete (Jan 2026) |
| **Chart API** | **2.5-3.5 weeks** | 📋 **Issue Created (Feb 2026)** |
| Testing & UAT | 2 weeks | Pending |

---

## 🔧 Services Affected

| Service | Changes | Risk Level | Status |
|---------|---------|------------|--------|
| `market-collector-lotte` | New handlers, API client | Medium | ✅ Complete |
| `realtime-v2` | New Kafka consumers, aggregate data | Low | ✅ Complete |
| `market-query-v2` | NO CHANGES (reads from Redis) | None | ✅ No Action Needed |
| `ws-v2` | New channel support | Low | ✅ Complete |
| `tradex-common-java` | Model updates (add fields) | Low | ✅ Complete |

> **Note:** `market-query-v2` requires NO changes because it reads aggregated data from Redis.

---

## ⚙️ Configuration

```yaml
derivatives:
  enabled: true
  init:
    enabled: true
    failSafe: true      # Continue if derivatives download fails
  websocket:
    enabled: true
    channels:
      - "auto.dr.qt"    # Derivatives quote
      - "auto.dr.bo"    # Derivatives bid/offer
```

---

## 🎯 Success Criteria

1. ✅ `symbol_static.json` contains derivatives with `market: "derivatives"`
2. ✅ `/api/v2/market/symbol/latest` returns derivatives data
3. ✅ WebSocket `market.quote.dr.{code}` publishes real-time data
4. 📋 `/tradingview/history?symbol=VN30F2501` returns chart data (Pending)
5. ✅ **Zero regression** on equity functionality
6. ✅ Feature flags allow easy enable/disable

---

## 📖 References

| Resource | Location |
|----------|----------|
| Lotte DR API Specs | `../../Documentation/[API specs]Lotte_DR.md` |
| Lotte DR WebSocket | `../../Documentation/Websocket_DR_Lotte.md` |
| TradeX Init Job | `../../../TradeX Knowledge/init-job.md` |
| TradeX SymbolInfo API | `../../../TradeX Knowledge/symbol-info-api.md` |
| Orders Planning | `../Orders/README.md` |

---

## 🔄 Document Maintenance

### When to Update

- ✅ New feature implemented → Update [Implementation Status](#implementation-status)
- ✅ New issue created → Add to [Active Issues](#active-issues)
- ✅ Requirements change → Update relevant docs in [Documentation Map](#documentation-map)
- ✅ Testing complete → Update status and add findings

### Folder Structure

```
Market data/
├── README.md                          ← You are here
├── Planning/                          ← Planning & requirements docs
├── Specifications/                    ← Detailed technical specs
├── Issues/                            ← Active implementation issues
└── Archive/                           ← Completed/historical docs
```

---

**Prepared By:** BA Team  
**Last Review:** February 3, 2026  
**Next Review:** After Chart API implementation  
**Document Version:** 2.0 (Refactored)
