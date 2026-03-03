# System Knowledge

> **What:** Actual mechanisms of TradeX system currently in production  
> **Status:** ✅ Live & Running  
> **Audience:** PM, BA, Developers

---

## Purpose

Documents về **cơ chế thực tế** đang chạy trong TradeX production system.

**NOT included here:**
- ❌ Planning documents (future features)
- ❌ API conventions (see `../API Standards/`)
- ❌ Proposals or RFCs

---

## Documents

| Document | Category | Description | Status |
|----------|----------|-------------|--------|
| [market-data-channels.md](./market-data-channels.md) | Market Data | WebSocket channels, data flow, Lotte → Client | ✅ Live |
| [symbol-info-api.md](./symbol-info-api.md) | Market Data | SymbolInfo API aggregation mechanism | ✅ Live |
| [init-job.md](./init-job.md) | System | Daily init job, symbol_static.json generation | ✅ Live |
| [ekyc-flow-upload-lotte-fpt-econtract.md](./ekyc-flow-upload-lotte-fpt-econtract.md) | eKYC | Luồng đầy đủ: upload ảnh Lotte + khởi tạo HĐĐT FPT | ✅ Live |
| [ekyc-signature-from-fpt-econtract.md](./ekyc-signature-from-fpt-econtract.md) | eKYC | Luồng lấy ảnh chữ ký từ FPT eContract → ekyc-admin → Lotte | ✅ Live |

---

## What Belongs Here?

### ✅ DO Include

- **System mechanisms** actually running in production
- **Data flows** between services (market-collector → realtime-v2 → ws-v2)
- **APIs** currently in use (symbolInfo, market channels)
- **Jobs & processes** running daily/hourly (init job, data sync)
- **Integration patterns** with external systems (Lotte, VietStock)

### ❌ DON'T Include

- **Future features** → Goes to `../Planning/`
- **API conventions** → Goes to `../API Standards/`
- **Proposals/RFCs** → Goes to `../Planning/`
- **Project-specific planning** → Goes to projects (e.g., Derivatives/Planning)

---

## Document Template

When adding new system knowledge:

```markdown
# [Mechanism Name]

> **Status:** ✅ Live | 🚧 In Development | 📋 Planned  
> **Last Updated:** YYYY-MM-DD

## Overview
[What this mechanism does]

## Architecture
[System diagram]

## Data Flow
[How data moves through services]

## Implementation
[Key services, files, technologies]

## Business Context
[Why this exists, what problem it solves]
```

---

## How to Use

### For PM
- Understand how current system works
- Explain features to stakeholders
- Write requirements based on existing patterns

### For BA
- Map business processes to technical implementation
- Understand data sources and flows
- Create test scenarios

### For Developers
- Understand architecture before making changes
- Reference existing patterns
- Debug production issues

---

**Last Updated:** 2026-02-04  
**Maintainer:** BA/PM Team
