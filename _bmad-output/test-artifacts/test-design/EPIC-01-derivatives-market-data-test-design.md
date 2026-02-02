# EPIC-01 Test Design: Derivatives Market Data

## Scope
This test design covers EPIC-01 stories for derivatives market data:
- E1.S1 Daily init (symbol list + static/reference)
- E1.S2 Real-time ingestion (Lotte WS)
- E1.S3 Aggregation into SymbolInfo cache (API compatibility)
- E1.S4 TradeX WebSocket publishing to clients
- E1.S5 SymbolInfo field extensions (backward compatible)

## Test Principles
- Prioritize **no regression** on equity market data
- Validate **graceful degradation** (derivatives failures do not break equity)
- Validate **format consistency** (`m="derivatives"`, payload shape aligned with equity)

## Test Matrix (by Story)

### E1.S1: Daily init adds derivatives symbols to symbol_static.json
| TC ID | Scenario | Expected |
|------|----------|----------|
| E1.S1-TC-01 | Trading day init | `symbol_static.json` includes derivatives entries with `m="derivatives"` |
| E1.S1-TC-02 | Derivatives APIs fail | Init completes for equity; derivatives skipped; warning logged |
| E1.S1-TC-03 | Partial derivatives details fail | Only valid derivatives included; job completes |

### E1.S2: Ingest derivatives real-time data from Lotte WebSocket
| TC ID | Scenario | Expected |
|------|----------|----------|
| E1.S2-TC-01 | Subscribe all VN30F codes | Updates received per subscribed symbol |
| E1.S2-TC-02 | WS disconnect | Reconnect; equity still normal |
| E1.S2-TC-03 | Malformed message | Skip + warning; continue processing next messages |

### E1.S3: Aggregate derivatives into SymbolInfo cache for /api/v2/market/symbolInfo
| TC ID | Scenario | Expected |
|------|----------|----------|
| E1.S3-TC-01 | After init, query SymbolInfo | Derivatives record contains static fields (re/ce/fl/bc/ed/rd) |
| E1.S3-TC-02 | Quote updates processed | `c/ch/ra/vo` updated in API response |
| E1.S3-TC-03 | Bid/Offer updates processed | `bb/bo/tb/to` updated in API response |
| E1.S3-TC-04 | Update for unknown symbol | Skip + warning; no outage |

### E1.S4: Publish derivatives updates via TradeX WebSocket channels
| TC ID | Scenario | Expected |
|------|----------|----------|
| E1.S4-TC-01 | Subscribe `market.quote.dr.{code}` | Quote updates delivered; `m="derivatives"` present |
| E1.S4-TC-02 | Subscribe `market.bidoffer.dr.{code}` | Bid/offer updates delivered in correct shape |
| E1.S4-TC-03 | Unsubscribe | No more updates received |
| E1.S4-TC-04 | Equity channels in parallel | Equity WS unaffected |

### E1.S5: Extend SymbolInfo to support derivatives-specific fields
| TC ID | Scenario | Expected |
|------|----------|----------|
| E1.S5-TC-01 | Equity serialization | No breaking change; optional fields absent/empty |
| E1.S5-TC-02 | Derivatives storage | Derivatives fields retrievable as expected |
| E1.S5-TC-03 | Mixed cache | Equity + derivatives coexist without conflicts |

## Regression Gate (must-pass)
| Gate | Description |
|------|-------------|
| RG-01 | Equity init job unchanged |
| RG-02 | Equity WS channels unchanged |
| RG-03 | Equity SymbolInfo API unchanged (format + latency) |

