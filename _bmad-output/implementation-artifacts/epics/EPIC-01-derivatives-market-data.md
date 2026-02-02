# EPIC-01: Derivatives Market Data

## Goal
Enable NHSV Pro users to view **derivatives market data** (VN30 futures) including:
- Symbol list (contracts)
- Real-time quote updates
- Real-time bid/offer (order book)

## Background / Context
TradeX currently supports equity market data reliably. The derivatives rollout must:
- **Not impact equity** (stability is top priority)
- **Minimize FE changes** by keeping the same consumption mechanism: app uses WebSocket for real-time and (optionally) `/api/v2/market/symbolInfo` sourced from aggregated real-time data.

## In Scope
- Add derivatives symbols to daily initialization output (`symbol_static.json`)
- Ingest derivatives real-time data from Lotte WebSocket
- Aggregate derivatives data into the same SymbolInfo cache used by equity
- Publish derivatives updates to TradeX WebSocket channels for client subscription

## Out of Scope
- Derivatives order placement / trading
- Derivatives account / balance / positions
- P&L, margin, settlement logic
- Major FE flow changes (switching to `/api/v2/market/symbol/latest`, rewriting FE aggregation)

## Key Business Rules
- **BR-01 (Trading calendar)**: No init job on weekends/holidays.
- **BR-02 (Pre-market readiness)**: Init completes before market open.
- **BR-03 (Graceful degradation)**: If derivatives fail, equity remains fully functional.
- **BR-04 (Market identification)**: Derivatives must be distinguishable using `m = "derivatives"`.

## User Value
- Users can see VN30 futures on the same app without switching platforms.
- Consistent real-time experience aligned with equity.

## Deliverables (Stories)
- E1.S1: Daily init adds derivatives symbols to `symbol_static.json`
- E1.S2: Ingest derivatives real-time data from Lotte WS
- E1.S3: Aggregate derivatives into SymbolInfo cache for `/api/v2/market/symbolInfo`
- E1.S4: Publish derivatives to TradeX WebSocket channels
- E1.S5: Extend SymbolInfo to include derivatives-specific fields (backward compatible)

## Definition of Done
- Epic acceptance criteria met for all stories
- No regression on equity market data (init + real-time + WS)
- Monitoring/logging available for derivatives health signals

