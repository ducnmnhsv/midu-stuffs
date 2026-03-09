# Asset - Derivatives Planning Documentation

**Category:** Derivatives Asset Management  
**Status:** Draft  
**Last Updated:** February 13, 2026

---

## Overview

Tài liệu tích hợp Asset management cho phái sinh, bao gồm:
- Vị thế mở (Open Positions)
- Thông tin tài sản (Asset Info)
- Lịch sử giao dịch (Transaction History)
- Lãi/lỗ chưa thực hiện (Unrealized P&L)

---

## Quick Access

### Planning Documents

| Document | Description |
|----------|-------------|
| [01_Integration_Plan](./Planning/01_Integration_Plan.md) | Kế hoạch tích hợp tổng thể |
| [02_Business_Requirements](./Planning/02_Business_Requirements.md) | Yêu cầu nghiệp vụ |
| [03_Technical_Requirements](./Planning/03_Technical_Requirements.md) | Yêu cầu kỹ thuật |
| [04_Use_Cases_Testing](./Planning/04_Use_Cases_Testing.md) | Use cases và testing |
| [05_DRACC037_Daily_PnL_Analysis](./Planning/05_DRACC037_Daily_PnL_Analysis.md) | Phân tích API Lãi/lỗ theo ngày (DRACC-037) |

### API Specifications

| Document | Lotte Code | Status |
|----------|------------|--------|
| [Open_Position_List_API_Spec](./Specifications/Open_Position_List_API_Spec.md) | DRACC-003 | ✅ Complete |
| [Asset_Info_API_Spec](./Specifications/Asset_Info_API_Spec.md) | DRACC-018 | ✅ Complete |
| [Transaction_History_API_Spec](./Specifications/Transaction_History_API_Spec.md) | DRACC-023 | ✅ Complete |
| [Unrealized_PnL_API_Spec](./Specifications/Unrealized_PnL_API_Spec.md) | DRACC-031 | ✅ Complete |
| [Daily_PnL_API_Spec](./Specifications/Daily_PnL_API_Spec.md) | DRACC-037 | ✅ Complete |
| [MU_Breach_Account_API_Spec](./Specifications/MU_Breach_Account_API_Spec.md) | DRACC-036 | ✅ Complete |

**Note:** All specs follow template format from `Regular_Orders_API_Spec.md`

---

## API Summary

| API | Method | Endpoint | Lotte Code |
|-----|--------|----------|------------|
| Get Open Positions | GET | `/api/v1/derivatives/asset/openPositions` | DRACC-003 |
| Get Asset Info | GET | `/api/v1/derivatives/asset/info` | DRACC-018 |
| Get Transaction History | GET | `/api/v1/derivatives/asset/transactionHistory` | DRACC-023 |
| Get Unrealized P&L | GET | `/api/v1/derivatives/asset/unrealizedPnl` | DRACC-031 |
| Get Daily P&L | GET | `/api/v1/derivatives/asset/dailyPnl` | DRACC-037 |
| Get MU Breach Account | GET | `/api/v1/derivatives/asset/muBreachAccount` | DRACC-036 |

**Common Patterns:**
- All use GET method với query parameters
- Pagination: `nextKey` + `fetchCount` (default 50, max 100)
- Field naming: `subNumber` (NOT `subAccountNumber`)
- Default values: `subNumber="80"` (Asset Info), dates=today

---

## Key Conventions

### URL Pattern
```
GET /api/v1/derivatives/asset/{resource}
```

### Request Parameters
- `accountNumber` (required)
- `subNumber` (required, except Asset Info với default "80")
- `nextKey` (pagination token)
- `fetchCount` (records per page: 1-100, default: 50)

### Response Structure

**List APIs:**
```json
{
  "items": [...],  // positions / transactions
  "pagination": {
    "hasMore": true,
    "nextKey": "...",
    "totalRecords": 150
  }
}
```

**Info API:**
```json
{
  "accountNumber": "...",
  "cashBalance": 500000000,
  ...
}
```

---

## Document Structure

Each API spec follows Regular Orders template:
- **§1 Overview** - Purpose, Endpoints, Response Standards
- **§2 Business Rules** - Common rules, validation, mappings
- **§3 API Details** - Request, Request Mapping, Response Mapping, Error Mapping
- **§4 Error Handling** - Consolidated error codes
- **§5 Implementation Notes** - Architecture, principles, transformation

---

## Navigation

- **Parent:** [Derivatives Planning Documentation](../../README.md)
- **Planning Folder:** [Planning/](./Planning/)
- **Specifications Folder:** [Specifications/](./Specifications/)

---

**Document Owner:** BA/PM Team  
**Implementation Team:** Backend Dev Team  
**Review Status:** Pending Dev Review
