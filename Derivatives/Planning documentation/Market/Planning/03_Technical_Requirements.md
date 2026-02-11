# Technical Requirements - High Level

**Document Type:** Technical Requirements  
**Module:** Derivatives Market Data  
**Audience:** Backend Lead, Architect  
**Last Updated:** February 3, 2026

---

## Overview

High-level technical requirements for implementing derivatives market data support in TradeX backend.

**Full detailed specifications are split into:**
- [SymbolInfo API](../Specifications/SymbolInfo_API.md)
- [WebSocket Integration](../Specifications/WebSocket_Integration.md)
- [Chart API](../Specifications/Chart_API_Spec.md)
- [Data Storage](../Specifications/Data_Storage.md)

---

## Core Requirements

### REQ-1: Isolation Principle

**Requirement:** Derivatives implementation MUST NOT impact existing equity functionality

**Implementation:**
- Separate Kafka topics (`quoteUpdateDR`, `bidOfferUpdateDR`)
- Separate message handlers
- Graceful degradation if derivatives fail
- Feature flags for easy enable/disable

**Validation:** Logic xử lý lỗi tách biệt: khi phái sinh lỗi chỉ ghi log cảnh báo và tiếp tục với cơ sở; không ảnh hưởng equity.

---

### REQ-2: Symbol Type Detection

**Requirement:** System must automatically detect symbol type (equity vs derivatives) and derivative sub-type.

**Key identifiers:**
- **Derivatives:** `t` (type) = `"FUTURES"` và **`m`** = `"INDEX"` hoặc `"BOND"` (cùng field `m` FE dùng cho HOSE/HNX/UPCOM). Suy từ ký tự thứ 3 của mã (41I → INDEX, 41B → BOND) tại init; mọi API trả symbol phải có `m` = INDEX | BOND cho mã phái sinh.

---

### REQ-3: Data Aggregation Strategy

**Requirement:** Use same strategy as equity (WebSocket → Redis → API)

**NOT:**
- ❌ Call Lotte REST API on every request
- ❌ Different caching strategy
- ❌ Special handling for derivatives

**YES:**
- ✅ Aggregate from WebSocket real-time
- ✅ Store in Redis (same pattern as equity)
- ✅ API reads from cache

---

### REQ-4: Performance

**Requirements:**
- API response time: < 500ms (same as equity)
- Redis cache hit rate: > 90%
- No degradation on equity performance

---

### REQ-5: Backward Compatibility

**Requirements:**
- All existing equity APIs unchanged
- No breaking changes to response format
- Frontend requires zero code changes

---

## Services Affected

| Service | Changes | Risk | Status |
|---------|---------|------|--------|
| `market-collector-lotte` | New handlers, API client | Medium | ✅ Complete |
| `realtime-v2` | New Kafka consumers | Low | ✅ Complete |
| `market-query-v2` | NO CHANGES | None | ✅ No Action |
| `ws-v2` | New channel patterns | Low | ✅ Complete |

---

## Configuration

```yaml
derivatives:
  enabled: true
  init:
    enabled: true
    failSafe: true
  websocket:
    enabled: true
```

---

## See Also

- **Detailed Specs:** [../Specifications/](../Specifications/)
- **Planning Docs:** [../Planning/](../Planning/)
- **Active Issues:** [../Issues/](../Issues/)
