---
name: project-knowledge-locations
description: Where to find TradeX system knowledge and documentation files
metadata: 
  node_type: memory
  type: reference
  originSessionId: 9a68fa6c-766a-4b45-858d-3338ee7aa7ff
---

**TradeX Knowledge/** (SSOT cho system knowledge):
- `System/` — Live production mechanisms (market data channels, symbol info API, init job, eKYC flow)
- `API Standards/` — 2 files: `tradex-api-conventions.md` (all-in-one guide) + `tradex-api-spec-template.md` (template)
- `Planning/` — Future features (regular order API mapping)

**Rule:** Check TradeX Knowledge/ TRƯỚC khi scan codebase. Sau phân tích mới → lưu findings vào đây.

**TradeX MCP/Knowledge based/** — microservice source code (khi cần trace implementation):
- `rest-proxy-main/` — API gateway, routes, scopeData.json
- `order-v2-main/` — Order service
- `realtime-v2-main/` — Real-time processing
- `documents-main/` — Existing API specs, BA docs, DB schemas

**Workspace tradex-monitoring:**
- `Derivatives/Planning documentation/{Category}/` — Derivatives docs (Planning/, Specifications/, Issues/, Archive/)
- `CLAUDE.md` — Project config, orchestrator routing, domain knowledge

**FE repo:** `/Users/ducnguyen/Documents/project/nhsv-mts-rn` (read-only reference)

**Personal tracking:** `/Users/nguyenduc/Personal/Repositories/Midu-path/Career/`
