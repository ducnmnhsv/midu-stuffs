# Order - Derivatives Orders

> **Module:** Orders (Regular & Conditional)  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** February 4, 2026  
> **Status:** Regular Orders Complete | Conditional Orders Planned

---

## 📋 Quick Navigation

| Section | Description |
|---------|-------------|
| [Overview](#-overview) | Business context and scope |
| [Architecture](#%EF%B8%8F-architecture-high-level) | High-level system design |
| [Implementation Status](#-implementation-status) | Current progress |
| [Documentation Map](#-documentation-map) | All docs index |
| [Active Issues](#-active-issues) | Ready-for-dev tasks |
| [How to Use](#-how-to-use) | Role-based guide |

---

## 🎯 Overview

### Mission

Enable traders to execute derivatives orders (regular and conditional) through NHSV Pro App with speed, flexibility, and control comparable to leading competitors.

### Scope

| In Scope | Out of Scope |
|----------|--------------|
| ✅ Regular Orders (Buy, Sell, Cancel, Modify) | ❌ Batch order operations |
| ✅ Query cancellable/modifiable orders | ❌ Advanced order routing |
| 📋 Conditional Orders (Stop, OCO, Trailing) | ❌ Algorithmic trading |
| 📋 Order History queries | |

**Legend:** ✅ Complete | 📋 Planned | ❌ Out of scope

---

## 🏗️ Architecture (High-Level)

```
┌─────────────┐
│   Client    │
│  NHSV Pro   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ rest-proxy  │  → API Gateway (Auth, Routing)
└──────┬──────┘
       │
       ├──────────────┬─────────────┐
       ▼              ▼             ▼
┌─────────────┐ ┌─────────┐ ┌─────────────┐
│   tuxedo    │ │ order-v2│ │ lotte-bridge│
│ (Regular)   │ │ (Cond.) │ │ (Core API)  │
└──────┬──────┘ └────┬────┘ └──────┬──────┘
       │             │             │
       └─────────────┴─────────────┘
                     │
                     ▼
              ┌─────────────┐
              │ Lotte API   │  → Exchange Backend
              └─────────────┘
```

**Key Services:**
- `rest-proxy` - Routes requests, handles auth
- `tuxedo` - Processes regular orders
- `lotte-bridge` - Core API integration
- `order-v2` - Handles conditional orders (future)

---

## 📊 Implementation Status

### ✅ Completed Features

| Feature | Status | Services | Documents |
|---------|--------|----------|-----------|
| Regular Orders (Buy/Sell) | ✅ Live | tuxedo, lotte-bridge | Planning/01-03, Specs/Regular_Orders |
| Cancel Orders | ✅ Live | tuxedo, lotte-bridge | Specs/Regular_Orders |
| Modify Orders | ✅ Live | tuxedo, lotte-bridge | Specs/Regular_Orders |
| Query Unmatch Orders | ✅ Live | tuxedo, lotte-bridge | Specs/Regular_Orders |
| Price Mechanism | ✅ Live | tuxedo | Specs/Price_Mechanism |

### 📋 Pending Implementation

| Feature | Status | Issue | Priority | Estimate |
|---------|--------|-------|----------|----------|
| Conditional Orders | 📋 Planned | TBD | High | 4-6 weeks |
| Order History | 📋 Planned | TBD | Medium | 2-3 weeks |

---

## 📚 Documentation Map

### Planning & Requirements (PM-Friendly, NO CODE)

| # | Document | Type | Audience | Description |
|---|----------|------|----------|-------------|
| 01 | [Regular Orders Business](./Planning/01_Regular_Orders_Business.md) | BRD | PM, BA, Stakeholders | Business requirements, user stories |
| 02 | [Order Flow](./Planning/02_Order_Flow.md) | Architecture | PM, BA, Architect | System flow, data flow diagrams |
| 03 | [Order Types](./Planning/03_Order_Types.md) | Spec | PM, BA, QA | Order types, validation rules |
| 04 | Conditional Orders Business | BRD | PM, BA | *(Planned)* Conditional orders requirements |

**⚠️ Important:** Planning/ docs follow `.cursor/rules/derivatives-pm-documentation.mdc`:
- ✅ Business logic, diagrams, user stories
- ❌ NO code blocks (Java, TypeScript, etc.)
- ❌ NO implementation details (class names, methods)

### Technical Specifications (For Developers)

| Document | Focus Area | Target Audience | Lines |
|----------|------------|-----------------|-------|
| [Regular Orders API Spec](./Specifications/Regular_Orders_API_Spec.md) | Complete API mapping (TradeX → Lotte) | BE Developers | ~750 |
| [TP/SL UI Copy](./Specifications/TP_SL_UI_Copy.md) | TP/SL tooltips, validation messages | FE, UX | ~90 |

**📘 TradeX-Wide API Standards:**
- [TradeX API Conventions](../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md) - Complete guide (standards + how-to)
- [API Spec Template](../../../TradeX%20Knowledge/API%20Standards/tradex-api-spec-template.md) - Copy for new specs

### Active Issues (Ready for Development)

**Current:** No active issues

**Future Issues:**
- Conditional Orders Implementation (when approved)
- Order History Implementation (when approved)

### Archive (Historical Documents)

**Current:** No archived documents

---

## 🎯 Feature Details

### Regular Orders

**What They Do:**
- **Buy** - Open or increase long position
- **Sell** - Open or increase short position  
- **Cancel** - Cancel pending order
- **Modify** - Change price/quantity of pending order

**Supported Order Types:**
- ✅ LO (Limit Order) - All sessions
- ✅ ATO (At-The-Opening) - Opening session
- ✅ ATC (At-The-Close) - Closing session
- ✅ MOK (Market Or Kill) - Continuous session
- ✅ MAK (Market At Kill) - Continuous session
- ✅ MTL (Market To Limit) - Continuous session

**Business Flow:**
```
Trader → Place Order → Validation → Send to Exchange → Order Book → Match/Pending
                                                                      ↓
Trader ← Notification ← System ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← Matched
```

### Conditional Orders (Coming Soon)

**Planned Features:**
- **Stop Orders** - Trigger at price level
- **OCO Orders** - One-Cancels-Other
- **Trailing Orders** - Dynamic stop loss
- **Bull/Bear Orders** - Market sentiment based

**Lotte APIs:**
- DRORD-005: Stop Buy
- DRORD-006: Stop Sell
- DRORD-023/024: Modify Stop
- DRORD-025/026: Cancel Stop

---

## 👥 How to Use This Documentation

### For PM

**Focus:** Business value and user impact

**Start with:**
1. Read [Overview](#-overview) - Understand mission and scope
2. Read [Planning/01_Regular_Orders_Business](./Planning/01_Regular_Orders_Business.md) - Business requirements
3. Review [Implementation Status](#-implementation-status) - What's done vs planned
4. Check [Feature Details](#-feature-details) - Order types and flows

**Skip:**
- Specifications/ folder (technical details for developers)

### For BA

**Focus:** Requirements analysis and testing

**Start with:**
1. Read all Planning/ docs (01-03) - Complete business context
2. Review [Order Types](#-feature-details) - Validation rules
3. Check [Active Issues](#-active-issues) - Tasks ready for analysis
4. Reference Specifications/ when needed - API contracts for test cases

**Workflow:**
- Planning/ = Understanding requirements
- Specifications/ = Defining test scenarios

### For Developers

**Focus:** Implementation and technical specs

**Start with:**
1. Read [Planning/02_Order_Flow](./Planning/02_Order_Flow.md) - System architecture
2. Read [Specifications/Regular_Orders_API_Spec](./Specifications/Regular_Orders_API_Spec.md) - Complete API mapping
3. Follow [TradeX API Conventions](../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md) - Coding standards
4. Check [Active Issues](#-active-issues) - Implementation tasks

**Code Examples:** All in Specifications/ folder

### For QA

**Focus:** Test scenarios and validation

**Start with:**
1. Read [Planning/03_Order_Types](./Planning/03_Order_Types.md) - Validation rules
2. Review [Specifications/Regular_Orders_API_Spec](./Specifications/Regular_Orders_API_Spec.md) - Test cases
3. Create test plans based on business requirements
4. Reference Planning/01 - User stories for acceptance criteria

---

## 📦 Related Folders

| Folder | Content | Status |
|--------|---------|--------|
| [Market data/](../Market%20data/) | Market data integration, WebSocket, SymbolInfo | ✅ Complete |
| [Account/](../Account/) | Account management (future) | 📋 Planned |
| [Asset/](../Asset/) | Portfolio and positions (future) | 📋 Planned |

---

## 🔗 External References

| Resource | Location | Description |
|----------|----------|-------------|
| Lotte API Specs | `../Documentation/[API specs]Lotte_DR.md` | Complete Lotte API documentation |
| TradeX Knowledge | `/TradeX Knowledge/Planning/regular-order-api-mapping.md` | General order patterns |
| Project Rules | `/AGENTS.md` | AI agent instructions and skills |

---

## ⚠️ Important Notes

### Documentation Standards

This folder follows **Derivatives Documentation Structure**:
- **Skill:** `.cursor/skills/derivatives-doc-structure/SKILL.md`
- **Rule:** `.cursor/rules/derivatives-pm-documentation.mdc`

**Key Principles:**
- ✅ Single entry point (README.md)
- ✅ Clear separation (Planning vs Specs vs Issues vs Archive)
- ✅ Consistent naming (PascalCase with underscores)
- ✅ PM-friendly Planning/ (NO CODE)
- ✅ Developer-friendly Specifications/ (CODE OK)

### File Size Guidelines

- ✅ README.md: Comprehensive but < 400 lines
- ✅ Planning docs: < 500 lines each
- ✅ Specifications: < 700 lines each (split if larger)
- ✅ Issues: < 800 lines each

### Naming Conventions

**✅ Good:**
```
README.md
Planning/01_Integration_Plan.md
Specifications/Order_API_Spec.md
Issues/Order_Implementation.md
```

**❌ Bad:**
```
_index.md
[ISSUE] Order API.md
00_EXECUTIVE_SUMMARY.md
order-api-spec.md
```

---

**Prepared By:** BA Team  
**Last Review:** February 4, 2026  
**Document Version:** 2.0  
**Status:** ✅ Active Development | Regular Orders Complete
