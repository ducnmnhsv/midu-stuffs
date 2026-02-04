# Order - Derivatives Orders

> **Category:** Orders (Regular & Conditional)  
> **Audience:** PM, BA, Stakeholders  
> **Last Updated:** February 3, 2026

---

## 📋 Overview

This category covers **order operations** for Derivatives trading, including:
- Regular Orders (Buy, Sell, Cancel, Modify)
- Conditional Orders (Stop, OCO, Trailing) - Coming Soon

---

## 🎯 Feature Status

| Feature | Status | Description |
|---------|--------|-------------|
| **Regular Orders** | ✅ Complete | Buy/Sell/Cancel/Modify operations |
| **Query Unmatch Orders** | ✅ Complete | List cancellable/modifiable orders |
| **Conditional Orders** | 📋 Planned | Stop orders, OCO, Trailing |
| **Order History** | 📋 Planned | Historical order queries |

---

## 📁 Documentation

### Planning/ - PM-Friendly (NO CODE)

| Document | Status | Description |
|----------|--------|-------------|
| [01_Regular_Orders_Business](./Planning/01_Regular_Orders_Business.md) | ✅ Complete | Business requirements for regular orders |
| [02_Order_Flow](./Planning/02_Order_Flow.md) | ✅ Complete | Order flow and architecture |
| [03_Order_Types](./Planning/03_Order_Types.md) | ✅ Complete | Order types and validation rules |
| 04_Conditional_Orders_Business.md | 📋 Planned | Conditional orders business requirements |

### Specifications/ - For Developers

| Document | Status | Description |
|----------|--------|-------------|
| [Regular_Orders_API_Spec](./Specifications/Regular_Orders_API_Spec.md) | ✅ Complete | Complete API mapping (TradeX → Lotte) |
| [Price_Mechanism_Spec](./Specifications/Price_Mechanism_Spec.md) | ✅ Complete | Price calculation technical details |

**📘 API Standards & Templates** (TradeX-wide, applies to all projects):
- **[TradeX API Conventions](../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md)** - Complete guide (standards + how-to)
- **[API Spec Template](../../../TradeX%20Knowledge/API%20Standards/tradex-api-spec-template.md)** - Copy for new specs

### Issues/ - Active Tasks

*No active issues currently*

### Archive/ - Historical

*To be organized as needed*

---

## 🎯 Regular Orders

### What They Do

Regular orders are standard trading operations:
- **Buy** - Open or increase long position
- **Sell** - Open or increase short position  
- **Cancel** - Cancel pending order
- **Modify** - Change price/quantity of pending order

### Key Features

- ✅ Support all order types (LO, ATO, ATC, MOK, MAK, MTL)
- ✅ Real-time order validation
- ✅ Query cancellable/modifiable orders
- ✅ Integration with Lotte backend

### Business Flow

```
Trader → Place Order → Validation → Send to Exchange → Order Book → Match/Pending
                                                                      ↓
Trader ← Notification ← System ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← Matched
```

---

## 📊 Order Types

### Regular Order Types

| Type | Code | Session | Description |
|------|------|---------|-------------|
| **LO** | 2 | All | Limit Order - Giá cố định |
| **ATO** | 3 | Opening | At-The-Opening - Khớp mở cửa |
| **MAK** | 4 | Continuous | Market At Kill - Khớp tối đa |
| **MOK** | 5 | Continuous | Market Or Kill - Khớp hết hoặc hủy |
| **ATC** | 6 | Closing | At-The-Close - Khớp đóng cửa |
| **MTL** | 9 | Continuous | Market To Limit - Chuyển thành LO |

### Validity Types

| Code | Type | Description |
|------|------|-------------|
| 0 | DAY | Valid for current day |
| 2 | ATO | Opening session only |
| 3 | IOC | Immediate Or Cancel |
| 4 | FOK | Fill Or Kill |
| 7 | ATC | Closing session only |

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
│ rest-proxy  │  → API Gateway
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   tuxedo    │  → Regular Order Processing
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Lotte API   │  → Exchange Backend
└─────────────┘
```

**Services:**
- `rest-proxy` - Routes requests, handles auth
- `tuxedo` - Processes regular orders
- `order-v2` - Handles conditional orders (future)

---

## 🎯 Conditional Orders (Coming Soon)

### Planned Features

- **Stop Orders** - Trigger at price level
- **OCO Orders** - One-Cancels-Other
- **Trailing Orders** - Dynamic stop loss
- **Bull/Bear Orders** - Market sentiment based

### Lotte APIs

- DRORD-005: Stop Buy
- DRORD-006: Stop Sell
- DRORD-023/024: Modify Stop
- DRORD-025/026: Cancel Stop

---

## 📖 How to Use

### For PM

1. **Understand business flow** - Read Planning/ docs
2. **Know order types** - Reference tables above
3. **Write user stories** - Use business requirements
4. **Skip technical details** - Leave Specifications/ to developers

### For BA

1. **Business analysis** - Planning/ folder
2. **API details** - Specifications/ folder (if needed)
3. **Test scenarios** - Create in Planning/

### For Developers

1. **Business context** - Read Planning/ docs
2. **Implementation** - Specifications/ has complete API mapping
3. **Code references** - Source files listed in specs

---

## 🔗 Related Documentation

| Resource | Location |
|----------|----------|
| Lotte API Specs | `../Documentation/[API specs]Lotte_DR.md` |
| Market Data | `../Market/` |
| TradeX Knowledge | `/TradeX Knowledge/regular-order-api-mapping.md` |
| Project Rules | `AGENTS.md` |

---

## ⚠️ Important Notes

### For PM Documentation

This folder follows **PM-friendly rules**:
- ✅ Planning/ = Business focus, NO CODE
- ✅ Specifications/ = Technical details, code OK
- ✅ Diagrams over implementation
- ✅ Business value first

**Rule:** `.cursor/rules/derivatives-pm-documentation.mdc`

---

**Status:** Active Development  
**Next Milestone:** Conditional Orders Planning  
**Maintained By:** BA/PM Team
