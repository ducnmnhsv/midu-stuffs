# Order - Derivatives Orders

> **Module:** Orders (Regular & Conditional)  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** February 24, 2026  
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
| 📋 Order Availability Check (Max quantity) | ❌ Algorithmic trading |
| 📋 Conditional Orders (Stop, OCO, Trailing) | |
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
| Order Availability Check | 📋 Spec Complete | [Order_Availability_Check_API_Spec](./Specifications/Order_Availability_Check_API_Spec.md) | High | 1-2 weeks |
| **Stop Orders** | 📋 Spec Complete | [Stop_Orders_API_Spec](./Specifications/Stop_Orders_API_Spec.md) | High | 3-5 weeks |
| Conditional Orders (OCO, Trailing) | 📋 Planned | TBD | Medium | 4-6 weeks |
| Order History | 📋 Planned | TBD | Medium | 2-3 weeks |

---

## 📚 Documentation Map

### Planning & Requirements (PM-Friendly, NO CODE)

| # | Document | Type | Audience | Description |
|---|----------|------|----------|-------------|
| 01 | [Regular Orders Business](./Planning/01_Regular_Orders_Business.md) | BRD | PM, BA, Stakeholders | Business requirements, user stories |
| 02 | [Order Flow](./Planning/02_Order_Flow.md) | Architecture | PM, BA, Architect | System flow, data flow diagrams |
| 03 | [Order Types](./Planning/03_Order_Types.md) | Spec | PM, BA, QA | Order types, validation rules |
| 04 | [TP/SL Orders Business](./Planning/04_TPSL_Orders_Business.md) | BRD | PM, BA, Stakeholders | TP/SL requirements, trigger mechanism, MTL order type |

**⚠️ Important:** Planning/ docs follow `.cursor/rules/derivatives-pm-documentation.mdc`:
- ✅ Business logic, diagrams, user stories
- ❌ NO code blocks (Java, TypeScript, etc.)
- ❌ NO implementation details (class names, methods)

### Technical Specifications (For Developers)

| Document | Focus Area | Target Audience | Lines |
|----------|------------|-----------------|-------|
| [Regular Orders API Spec](./Specifications/Regular_Orders_API_Spec.md) | Complete API mapping (TradeX → Lotte) | BE Developers | ~750 |
| [Order Availability Check API Spec](./Specifications/Order_Availability_Check_API_Spec.md) | DRORD-028 - Check max order quantity before placing order | BE Developers | ~600 |
| [Stop Orders API Spec](./Specifications/Stop_Orders_API_Spec.md) | DRORD-005/006/023/024/025/026 - Stop order (Place/Modify/Cancel) | BE Developers | ~300 |
| [TP/SL UI Copy](./Specifications/TP_SL_UI_Copy.md) | TP/SL tooltips, validation messages | FE, UX | ~90 |

**📄 Design Decisions & Technical Analysis:**
- [Order Availability Response Design](./Specifications/Order_Availability_Response_Design.md) - Why API response contains only availability data (no margin fields)
- [Real-time Availability Analysis](./Specifications/Real_time_Availability_Analysis.md) - WebSocket vs polling strategies for real-time availability updates

**📘 TradeX-Wide API Standards:**
- [TradeX API Conventions](../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md) - Complete guide (standards + how-to)
- [API Spec Template](../../../TradeX%20Knowledge/API%20Standards/tradex-api-spec-template.md) - Copy for new specs

**⚠️ mdm_tp (Kênh thực hiện):** Khi Lotte yêu cầu `mdm_tp`, Backend **derive** từ platform/channel (giống Equity) – FE **không** gửi. Xem [tradex-api-conventions.md §1.1](../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md#11-mdm_tp-kênh-thực-hiện--derived-fe-không-truyền).

### Active Issues (Ready for Development)

**Current Issues:**

| Issue | Status | Priority | Blocker |
|-------|--------|----------|---------|
| [Trade Screen FE Requirement (Normal/Quick Order)](./Issues/Trade_Screen_FE_Requirement.md) | 📋 Ready | High | — |
| [TP/SL Tracking Mechanism](./Issues/TPSL_Tracking_Mechanism_Discussion.md) | 🔴 BLOCKED | High | Waiting for Core order lifecycle events |
| [Stop Order: TradeX-Native Design](./Issues/Stop_Order_TradeX_Native_Design.md) | 📋 Đề xuất | High | Chờ PM duyệt hướng thiết kế |

**Issue Summary:**
- **Trade Screen FE:** Màn Trade Derivatives với 2 mode UI (Lệnh thường / Lệnh nhanh) theo Figma; logic đặt lệnh dùng chung. Xem [Trade_Screen_FE_Requirement](./Issues/Trade_Screen_FE_Requirement.md).
- **TP/SL:** Cần track order lifecycle (cancel, modify) nhưng Core không cung cấp events
- **Stop Order:** Lotte thiếu API query/modify/socket → Đề xuất TradeX-Native (lưu & monitor, khi trigger đẩy DRORD-029/030). Xem [Stop_Order_TradeX_Native_Design](./Issues/Stop_Order_TradeX_Native_Design.md)

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

**Lotte APIs:** DRORD-005, 006, 023, 024, 025, 026 (Place/Modify/Cancel)

**Spec:** [Stop_Orders_API_Spec](./Specifications/Stop_Orders_API_Spec.md)

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
