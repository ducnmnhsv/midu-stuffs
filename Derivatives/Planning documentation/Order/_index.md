# Derivatives Orders Documentation

**Category:** Orders (Lệnh)  
**Last Updated:** February 3, 2026

---

## Overview

Folder này chứa tài liệu phân tích và thiết kế cho Orders category của Derivatives (Phái sinh), bao gồm cả lệnh thường và lệnh điều kiện.

---

## Documents Index

| # | Document | Status | Description |
|---|----------|--------|-------------|
| 00 | [Analysis Summary](./00_ANALYSIS_SUMMARY.md) | ✅ Complete | Summary of regular orders analysis session |
| 01 | [Regular Orders API Mapping](./01_REGULAR_ORDERS_API_MAPPING.md) | ✅ Complete | TradeX → Lotte API mapping cho lệnh thường (Buy/Sell/Cancel/Modify) |
| 02 | *Conditional Orders* | 🔜 Coming | Lệnh điều kiện (Stop, OCO, Trailing) - mapping to Lotte |
| 03 | *Order Status & Lifecycle* | 📋 Planned | Order states, transitions, error handling |
| 04 | *Order History & Queries* | 📋 Planned | Query APIs, filters, pagination |
| -- | [Summary Price Mechanism](./summary%20price%20mechanism.md) | ✅ Existing | Price calculation mechanism |
| -- | [README - Completion](./README-COMPLETION.md) | ✅ Complete | Completion report and checklist |

---

## Regular Orders (Lệnh Thường)

### Document: 01_REGULAR_ORDERS_API_MAPPING.md

**Scope:**
- ✅ Buy Order (Mua) - DRORD-029
- ✅ Sell Order (Bán) - DRORD-030
- ✅ Cancel Order (Hủy) - DRORD-031
- ✅ Modify Order (Sửa) - DRORD-032
- ✅ Query Unmatch Orders - DRORD-011

**Content:**
- Complete TradeX → Lotte field mappings
- Request/Response structures
- Order types reference (LO, ATO, ATC, MOK, MAK, MTL)
- Derivatives vs Equity differences
- Implementation notes with source file references

**Key APIs:**
```
POST /api/v1/derivatives/order          → dr-buy-by-user / dr-sell-by-user
PUT  /api/v1/derivatives/order/cancel   → dr-can-by-user
PUT  /api/v1/derivatives/order/modify   → dr-mod-by-user
GET  /api/v1/derivatives/order/todayUnmatch → dr-nmth-order
```

**Lotte DRORD Codes:**
- DRORD-029: Buy by User
- DRORD-030: Sell by User
- DRORD-031: Cancel by User
- DRORD-032: Modify by User
- DRORD-011: List of Cancellable/Modifiable Orders

---

## Conditional Orders (Lệnh Điều Kiện) - Coming Soon

### Planned Coverage

**Stop Orders:**
- DRORD-005: Stop Order Buy (Điều kiện Mua)
- DRORD-006: Stop Order Sell (Điều kiện Bán)
- DRORD-023/024: Modify Stop Orders
- DRORD-025/026: Cancel Stop Orders
- DRORD-016: Query Stop Orders History

**Additional Order Types:**
- OCO Orders (One-Cancels-Other)
- Trailing Orders
- Bull/Bear Orders

**Reference:**
- TradeX `order-v2` service handles conditional orders
- Documentation exists in `TradeX MCP/Knowledge based/order-v2-main/ORDER_V2_DOCUMENTATION.md`
- Need to map to Lotte DRORD APIs

---

## Related Documentation

### In This Project

| Location | Content |
|----------|---------|
| `Derivatives/Documentation/[API specs]Lotte_DR.md` | Lotte official API specifications |
| `Derivatives/Planning documentation/Market data/` | Market data integration docs |
| `TradeX Knowledge/regular-order-api-mapping.md` | General order mapping (cross-market) |

### External References

| Service | Location |
|---------|----------|
| `order-v2` | `TradeX MCP/Knowledge based/order-v2-main/` |
| `rest-proxy` | `TradeX MCP/Knowledge based/rest-proxy-main/` |
| `lotte-bridge` | `TradeX MCP/Knowledge based/lotte-bridge-main/` |

---

## Architecture Overview

### Derivatives Order Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    NHSV PRO APP (Client)                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      rest-proxy (API Gateway)                    │
│                    /api/v1/derivatives/order/*                   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
                        ┌─────────┐
                        │  KAFKA  │
                        └────┬────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
      ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
      │   order-v2  │ │   tuxedo    │ │ Other       │
      │(Conditional)│ │  (Regular)  │ │ Services    │
      └─────────────┘ └──────┬──────┘ └─────────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │  Lotte Tuxedo API    │
                  │  (DRORD-029/030/...) │
                  └──────────────────────┘
```

### Service Responsibilities

| Service | Responsibility | Order Types |
|---------|---------------|-------------|
| `rest-proxy` | API Gateway, routing, auth | All |
| `tuxedo` | Regular order processing | Buy, Sell, Cancel, Modify |
| `order-v2` | Conditional order logic | Stop, OCO, Trailing, Bull/Bear |
| `lotte-bridge` | Lotte API integration | N/A (Equity only) |

---

## Order Types Reference

### Regular Order Types (Lotte Codes)

| Code | Type | Vietnamese | Session | Description |
|------|------|------------|---------|-------------|
| `2` | LO | Lệnh giới hạn | All | Limit Order |
| `3` | ATO | Khớp mở cửa | Opening | At-The-Opening |
| `4` | MAK | Market At Kill | Continuous | Execute as much as possible, cancel rest |
| `5` | MOK | Market Or Kill | Continuous | Execute completely or cancel |
| `6` | ATC | Khớp đóng cửa | Closing | At-The-Close |
| `9` | MTL | Market To Limit | Continuous | Market order converts to limit |

### Validity Types

| Code | Type | Vietnamese | Description |
|------|------|------------|-------------|
| `0` | DAY | Lệnh ngày | Valid for current trading day |
| `2` | ATO | Mở cửa | Opening session only |
| `3` | IOC | Immediate Or Cancel | Execute immediately or cancel |
| `4` | FOK | Fill Or Kill | Execute completely or cancel |
| `7` | ATC | Đóng cửa | Closing session only |

---

## Development Status

### Completed ✅
- [x] Regular Orders API Mapping
  - [x] Buy/Sell operations
  - [x] Cancel/Modify operations
  - [x] Query Unmatch Orders
  - [x] Field mappings
  - [x] Order types reference

### In Progress 🔄
- [ ] None currently

### Planned 📋
- [ ] Conditional Orders Mapping
  - [ ] Stop Orders (DRORD-005/006)
  - [ ] Stop Order Modify/Cancel (DRORD-023/024/025/026)
  - [ ] Stop Order History (DRORD-016)
- [ ] Order Status & Lifecycle
- [ ] Order History & Query APIs
- [ ] Error Handling & Validation Rules
- [ ] Order Matching & Execution Flow

---

## Notes for Future Documentation

### Conditional Orders (Next Priority)

**Questions to address:**
1. How does TradeX `order-v2` trigger conditional orders?
2. What's the mapping between `order-v2` stop orders and Lotte DRORD-005/006?
3. How are stop order modifications handled (DRORD-023/024)?
4. What's the price monitoring mechanism?
5. How do OCO orders work with Lotte backend?

**Source files to analyze:**
- `order-v2-main/src/main/java/com/vn/controller/`
- `order-v2-main/src/main/java/com/vn/service/`
- Kafka topic: `order-v2`

### Order History & Queries

**Questions to address:**
1. DRORD-010: Order history API
2. DRORD-016: Conditional order history
3. Pagination, filters, date ranges
4. Status mapping (pending, completed, cancelled, failed)

---

## Usage Guidelines

### For PM
1. Reference this index to understand documentation coverage
2. Use documents when writing stories for order features
3. Check "Planned" section for upcoming documentation needs

### For BA
1. Use completed documents for API analysis
2. Follow same structure when creating new documents
3. Update this index when adding new documents

### For Developers
1. Refer to API mapping documents for implementation
2. Cross-reference with TradeX MCP source code
3. Validate against Lotte official documentation

---

## Document Naming Convention

```
[NN]_[CATEGORY]_[SUBCATEGORY].md

Examples:
- 01_REGULAR_ORDERS_API_MAPPING.md
- 02_CONDITIONAL_ORDERS_API_MAPPING.md
- 03_ORDER_STATUS_LIFECYCLE.md
```

---

**Last Review:** February 3, 2026  
**Next Review:** When conditional orders mapping is completed  
**Maintained By:** BA/PM Team
