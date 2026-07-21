# TradeX Knowledge Base

> **Purpose:** Tài liệu tổng hợp về TradeX Backend System  
> **Audience:** Product Manager, Business Analyst, Developers  
> **System:** TradeX - Backend của NHSV Pro App

---

## 📁 Knowledge Structure

```
Knowledge/TradeX/
├── System/              # ✅ Production - What's actually running
├── API Standards/       # 📘 Conventions - Universal standards
└── Planning/            # 📋 Future - Features in planning phase
```

---

## 📂 Folders

### [System/](./System/) - Live Production Knowledge

**What:** Mechanisms actually running in TradeX production

| Document | Category | Description |
|----------|----------|-------------|
| [market-data-channels.md](./System/market-data-channels.md) | Market Data | WebSocket channels, Lotte → Client flow |
| [symbol-info-api.md](./System/symbol-info-api.md) | Market Data | SymbolInfo aggregation mechanism |
| [symbolinfo-api-fields-guide.md](./System/symbolinfo-api-fields-guide.md) | Market Data | **Field reference** — Các field của `/api/v2/market/symbolInfo` |
| [foreigner-trading-api-mapping.md](./System/foreigner-trading-api-mapping.md) | Market Data | Giao dịch khối ngoại — 4 API hiện có (per-symbol), gap: chưa có API tổng hợp cấp sàn |
| [init-job.md](./System/init-job.md) | System | Daily init job, symbol_static.json |
| [ekyc-flow-upload-lotte-fpt-econtract.md](./System/ekyc-flow-upload-lotte-fpt-econtract.md) | eKYC | Upload ảnh Lotte + khởi tạo HĐĐT FPT (full flow) |

**When to use:** Need to understand how current system works

---

### [API Standards/](./API%20Standards/) - Universal Conventions

**What:** Standards for ALL TradeX APIs (immutable unless system-wide change)

| Document | Type | Description |
|----------|------|-------------|
| [tradex-api-conventions.md](./API%20Standards/tradex-api-conventions.md) | Complete | Full standards + How-to guide (~750 lines) |
| [tradex-api-spec-template.md](./API%20Standards/tradex-api-spec-template.md) | Template | Copy for new specs (~370 lines) |

**When to use:** Creating new APIs, need to follow standards

**Why only 2 files?** Merged redundant docs to save quota, single source of truth

---

### [Planning/](./Planning/) - Future Features

**What:** Features in planning phase (not yet implemented)

| Document | Status | Description |
|----------|--------|-------------|
| [regular-order-api-mapping.md](./Planning/regular-order-api-mapping.md) | 📋 Planning | Regular orders (Buy/Sell/Cancel/Modify) — lotte-bridge / tuxedo |
| [conditional-order-api-mapping.md](./Planning/conditional-order-api-mapping.md) | ✅ Complete | Conditional orders (Stop / OCO / Trailing / Bull-Bear) — `order-v2` |

**When to use:** Planning new features, understanding future direction

---

## 🎯 Quick Navigation by Role

### For PM

**Understand Production:**
- System architecture → `System/` folder
- Market data flow → `System/market-data-channels.md`
- Daily operations → `System/init-job.md`

**Create New Features:**
- API standards → `API Standards/api-conventions.md` (quick ref)
- Future planning → `Planning/` folder

### For BA

**Analyze System:**
- Current mechanisms → `System/` folder
- Data flows & integration → System docs

**Create API Specs:**
1. Read `API Standards/tradex-api-conventions.md` (includes how-to)
2. Copy `API Standards/tradex-api-spec-template.md`
3. Follow standards & checklist

### For Developers

**Understand Architecture:**
- Service interactions → `System/` docs
- Data flows → System diagrams

**Implement APIs:**
- Follow `API Standards/tradex-api-conventions.md` (all-in-one)

---

## 📚 System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NHSV PRO APP                                      │
│                    (iOS / Android / Web)                                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
          ┌─────────────────┐             ┌─────────────────┐
          │   REST API      │             │   WebSocket     │
          │  (rest-proxy)   │             │    (ws-v2)      │
          └────────┬────────┘             └────────┬────────┘
                   │                               │
                   ▼                               │
          ┌─────────────────────────────────────────────────┐
          │                    KAFKA                        │
          └─────────────────────────────────────────────────┘
                   │
     ┌─────────────┼─────────────┬─────────────┬─────────────┐
     ▼             ▼             ▼             ▼             ▼
┌─────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐
│   aaa   │ │  order-v2 │ │realtime-v2│ │ market-   │ │notification│
│ (auth)  │ │ (trading) │ │ (market)  │ │ query-v2  │ │  (alert)  │
└─────────┘ └───────────┘ └───────────┘ └───────────┘ └───────────┘
                   │             │             │
                   ▼             ▼             ▼
          ┌─────────────────────────────────────────────────┐
          │           LOTTE SECURITIES API                  │
          │      (Trading + Market Data Provider)           │
          └─────────────────────────────────────────────────┘
```

---

## 🏗️ Core Services

> **Kafka topic = `clusterId` / `spring.application.name`** (verified từ config, 2026-06-30). Snapshot code đồng bộ tại `Knowledge/TradeX-MCP/` cùng ngày.

| Service (folder TradeX-MCP) | Tech | Kafka Topic | Role |
|---------|------|-------------|------|
| `rest-proxy` | Node.js | `rest-proxy` (gateway, route theo scope) | API Gateway, scope-based routing |
| `aaa` | Node.js | `aaa` | Authentication, OTP, Smart OTP |
| `lotte-bridge` | Node.js | `lotte-bridge` | Lotte integration: order, account, balance, transfer, change broker, smart OTP |
| `lotte-ws-bridge` | Node.js | `lotte-ws-bridge` | Lotte WS bridge: notification (gồm Derivatives margin/VM/position) |
| `market-query-v2` | Node.js | `market-v2` | Market data queries (pkg `tradex-market`) |
| `ws-v2` | Node.js | — (WebSocket server) | WebSocket server cho client |
| `configuration` | Node.js | `configuration` | Config service |
| `order-v2` | Java (Spring Boot 3.1, Java 17) | `order` | Conditional orders (Stop/OCO/Trailing/Bull-Bear) |
| `realtime-v2` | Java | `realtime-v2` | Real-time processing, EOD snapshot/backfill |
| `market-collector-lotte` | Java | `market-collector-lotte` | Collect market data từ Lotte WS |
| `notification` | Java | `notification` | Push / SMS / email + templates |
| `ekyc-admin` | Java (JHipster) | `ekyc-admin` | eKYC admin: Lotte upload + FPT eContract + OTP |
| `nhsv-admin` | Java (JHipster) | `nhsv-admin` | Admin web tool |
| `tradex-common-java` | Java library | — | Shared models / enums / kafka utils |
| `documents` | — (docs only) | — | API specs / BA / QA / DB docs |

---

## 🌐 Environments

| Environment | URL | Purpose |
|-------------|-----|---------|
| **Production** | https://nhsvpro.nhsv.vn | Live users |
| **UAT** | https://tnhsvpro.nhsv.vn | Testing |

---

## 📊 Trading Sessions (HOSE)

| Session | Time | Order Types | Description |
|---------|------|-------------|-------------|
| Pre-open | 08:30-09:00 | LO | Đặt lệnh trước giờ |
| **ATO** | 09:00-09:15 | ATO, LO | Khớp lệnh mở cửa |
| Morning | 09:15-11:30 | LO, MP | Phiên liên tục sáng |
| Break | 11:30-13:00 | - | Nghỉ trưa |
| Afternoon | 13:00-14:30 | LO, MP | Phiên liên tục chiều |
| **ATC** | 14:30-14:45 | ATC, LO | Khớp lệnh đóng cửa |
| **PLO** | 14:45-15:00 | PLO | Post Limit Order |

---

## 📖 How to Use This Knowledge Base

### Adding New Knowledge

**Determine folder:**

1. **System/** - If it's **already running** in production
   - Services, APIs, jobs, data flows
   - Example: How market data flows through system

2. **API Standards/** - If it's **universal API convention**
   - Applies to ALL projects
   - Rarely changes
   - Example: Error format standards

3. **Planning/** - If it's **being planned** but not implemented
   - General patterns for future features
   - Not project-specific
   - Example: Conditional order patterns

**Wrong folder?**
- ❌ Project-specific planning → Goes to `[Project]/Planning documentation/`
- ❌ Project implementation → Document in project folder

### For AI Agents

1. **Read folder READMEs first** to understand structure
2. **Navigate to appropriate folder** based on need:
   - Understanding production → `System/`
   - Creating APIs → `API Standards/`
   - Planning features → `Planning/`
3. **Only scan codebase** if knowledge doesn't cover topic

---

## 🔄 Document Lifecycle

```
Planning/ (General patterns)
    ↓
    [Project approved]
    ↓
Project/Planning documentation/ (Detailed requirements)
    ↓
    [Implementation]
    ↓
System/ (Live documentation)
```

**Example:**
- `Planning/regular-order-api-mapping.md` (general pattern)
- → `Derivatives/Planning documentation/Order/` (detailed Derivatives specs)
- → `System/order-flow.md` (once live, future)

---

## 📊 Document Status

### System/ (Live)

| Document | Status | Last Updated |
|----------|--------|--------------|
| `market-data-channels.md` | ✅ Live | 2025-01-28 |
| `symbol-info-api.md` | ✅ Live | 2025-01-28 |
| `init-job.md` | ✅ Live | 2025-01-28 |
| `ekyc-flow-upload-lotte-fpt-econtract.md` | ✅ Live | 2026-03-02 |

### API Standards/ (Universal)

| Document | Status | Last Updated |
|----------|--------|--------------|
| `tradex-api-conventions.md` | ✅ Active | 2026-02-04 |
| `tradex-api-spec-template.md` | ✅ Template | 2026-02-04 |

### Planning/ (Future)

| Document | Status | Last Updated |
|----------|--------|--------------|
| `regular-order-api-mapping.md` | 📋 Planning | 2026-02-03 |
| `conditional-order-api-mapping.md` | ✅ Complete | 2026-06-30 |

---

## 🆕 Contributing

**Adding new knowledge:**

1. Choose appropriate folder (System/API Standards/Planning)
2. Create document following folder's template
3. Update folder's README
4. Update this `_index.md`
5. Update `.cursor/rules/tradex-knowledge.mdc` if needed

**Folder-specific guidelines:** See each folder's README

---

*This knowledge base is maintained through AI-assisted analysis of TradeX codebase.*

**Last Updated:** 2026-02-04  
**Structure Version:** 2.0 (Refactored with clear separation)
