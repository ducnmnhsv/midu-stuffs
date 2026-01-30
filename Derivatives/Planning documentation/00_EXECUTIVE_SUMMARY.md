# Derivatives Integration - Executive Summary

> **Project:** TradeX Derivatives Integration  
> **Date:** 2025-01-30  
> **Status:** Planning Complete

---

## TL;DR

Bổ sung giao dịch phái sinh (Futures) vào hệ thống TradeX Backend với 2 nguyên tắc:
1. **ISOLATION** - Không ảnh hưởng hệ thống equity đang chạy ổn định
2. **MINIMIZE FE CHANGES** - Giữ nguyên cơ chế FE, data phái sinh được aggregate từ WebSocket như cơ sở

---

## Scope

| In Scope | Out of Scope |
|----------|--------------|
| ✅ Init job lấy mã phái sinh | ❌ Order/Trading API |
| ✅ SymbolInfo API (aggregate từ WS) | ❌ Account derivatives |
| ✅ Real-time WebSocket | ❌ P&L calculation |
| ✅ symbol_static.json | ❌ Mobile app changes |
| ✅ Giữ nguyên cơ chế FE | ❌ Thay đổi flow FE |

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────┐
│                    DATA SOURCES                                 │
├────────────────────────────────────────────────────────────────┤
│  EQUITY (Existing)              DERIVATIVES (New)               │
│  /api/v2/symbols/*              /tuxsvc/market/dr/*             │
│  auto.qt, auto.bo               auto.dr.qt, auto.dr.bo          │
└──────────────────────┬─────────────────────┬───────────────────┘
                       │                     │
                       ▼                     ▼
┌────────────────────────────────────────────────────────────────┐
│                MARKET-COLLECTOR-LOTTE                          │
│  • Equity handlers (unchanged)                                  │
│  • Derivative handlers (new, isolated)                          │
└──────────────────────┬─────────────────────┬───────────────────┘
                       │                     │
                       ▼                     ▼
┌────────────────────────────────────────────────────────────────┐
│                        KAFKA                                    │
│  quoteUpdate (equity)         quoteUpdateDR (derivatives)       │
│  bidOfferUpdate (equity)      bidOfferUpdateDR (derivatives)    │
└──────────────────────┬─────────────────────┬───────────────────┘
                       │                     │
                       ▼                     ▼
┌────────────────────────────────────────────────────────────────┐
│                     REALTIME-V2                                 │
│  • Equity consumers (unchanged)                                 │
│  • Derivative consumers (new, isolated)                         │
│  → Redis: realtime_mapSymbolInfo                               │
└──────────────────────┬─────────────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
┌──────────────────┐    ┌───────────────────────────────────────┐
│  MARKET-QUERY-V2 │    │              WS-V2                    │
│  /symbol/latest  │    │  market.quote.{code}      (equity)    │
│  + derivatives   │    │  market.quote.dr.{code}   (derivatives)│
└──────────────────┘    └───────────────────────────────────────┘
```

---

## Key Identifier

| Field | Equity | Derivatives |
|-------|--------|-------------|
| `t` (type) | STOCK, ETF, CW, INDEX | FUTURES |
| `m` (market) | HOSE, HNX, UPCOM | **derivatives** |

**Format trong symbol_static.json:**

```json
// Equity (hiện tại)
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

// Derivatives (mới)
{
    "s": "VN30F2501",
    "m": "derivatives",           // ← KEY IDENTIFIER
    "n1": "HĐ Tương lai VN30 Tháng 01/2025",
    "n2": "VN30 Index Futures Jan 2025",
    "t": "FUTURES",
    "re": 1273.0,
    "ce": 1350.0,
    "fl": 1220.0,
    "lq": 0,
    "bc": "VN30",                 // Mã cơ sở
    "ed": "20250130",             // Ngày đáo hạn
    "rd": 15                      // Số ngày còn lại
}
```

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Init job fail ảnh hưởng equity | Try-catch + continue với empty list |
| WebSocket fail ảnh hưởng equity | Separate handlers + error isolation |
| API latency tăng | Feature flag để disable nếu cần |

**Key Code Pattern:**

```java
try {
    derivativeSymbols = downloadDerivatives();
} catch (Exception e) {
    log.warn("Derivatives failed, continuing with equity only");
    derivativeSymbols = Collections.emptyList();  // GRACEFUL DEGRADATION
}
```

---

## Deliverables

| # | Document | Description | Audience |
|---|----------|-------------|----------|
| 1 | [00_EXECUTIVE_SUMMARY.md](./00_EXECUTIVE_SUMMARY.md) | Tổng quan dự án | PO, Tech Lead |
| 2 | [01_DERIVATIVES_INTEGRATION_PLAN.md](./01_DERIVATIVES_INTEGRATION_PLAN.md) | Technical Plan chi tiết | Architect, BE Lead |
| 3 | [02_BE_REQUIREMENTS_SPEC.md](./02_BE_REQUIREMENTS_SPEC.md) | Specs cho BE Developer | BE Developers |
| 4 | [03_BA_BUSINESS_REQUIREMENTS.md](./03_BA_BUSINESS_REQUIREMENTS.md) | Business Requirements (BRD) | PM, BA, Stakeholders |
| 5 | [04_USE_CASES_AND_TEST_SCENARIOS.md](./04_USE_CASES_AND_TEST_SCENARIOS.md) | Use Cases & Test Scenarios | BA, QA Team |

---

## Services Affected

| Service | Changes | Risk Level |
|---------|---------|------------|
| `market-collector-lotte` | New handlers, API client | Medium |
| `realtime-v2` | New Kafka consumers, aggregate data | Low |
| `market-query-v2` | **KHÔNG ĐỔI** (đọc từ Redis) | None |
| `ws-v2` | New channel support | Low |
| `tradex-common-java` | Model update (thêm fields) | Low |

> **Lưu ý:** `market-query-v2` không cần thay đổi vì nó chỉ đọc data từ Redis.
> Data đã được aggregate bởi `realtime-v2` → API tự động hoạt động.

---

## Work Estimation

| Phase | Tasks | Duration |
|-------|-------|----------|
| Init Job | API client + download + merge | 5-6 days |
| Data Aggregation | realtime-v2 consumers + Redis update | 3-4 days |
| WebSocket | Subscription + publish | 4-5 days |
| Testing | Unit + Integration + E2E | 3-4 days |
| **Total** | | **15-19 days** |

> **Lưu ý:** `market-query-v2` KHÔNG cần thay đổi → Giảm effort cho API layer.

---

## Success Criteria

1. ✅ `symbol_static.json` contains derivatives with `market: "derivatives"`
2. ✅ `/api/v2/market/symbol/latest` returns derivatives data
3. ✅ WebSocket `market.quote.dr.{code}` publishes real-time data
4. ✅ **Zero regression** on equity functionality
5. ✅ Feature flags allow easy enable/disable

---

## Configuration

```yaml
derivatives:
  enabled: true
  init:
    enabled: true
    failSafe: true      # Continue if fails
  websocket:
    enabled: true
```

---

## Next Steps

1. Review and approve planning documents
2. Create Jira stories from requirements
3. Technical design review with BE team
4. Begin implementation (Init Job first)

---

## References

| Document | Location |
|----------|----------|
| Lotte DR API Specs | `Derivatives/Documentation/[API specs]Lotte_DR.md` |
| Lotte DR WebSocket | `Derivatives/Documentation/Websocket_DR_Lotte.md` |
| TradeX Init Job | `TradeX Knowledge/init-job.md` |
| TradeX SymbolInfo API | `TradeX Knowledge/symbol-info-api.md` |

---

*End of Executive Summary*
