# Internal Transfer - Derivatives Cash Transaction

> **Module:** Internal Transfer  
> **Category:** Cash Transaction  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** February 9, 2026  
> **Status:** Planning

---

## Overview

Internal transfer module enables users to transfer cash between derivatives sub-accounts within the same account or across different accounts.

### Key Features

| Feature | Lotte API | Status | Description |
|---------|-----------|--------|-------------|
| Cash Transfer | DRACC-019 | 📋 Planning | Transfer cash between sub-accounts |
| Transfer History | DRACC-020 | 📋 Planning | Query transfer history with pagination |

---

## Quick Access

| Document | Description | Audience |
|----------|-------------|----------|
| [Internal_Transfer_API_Spec.md](./Internal_Transfer_API_Spec.md) | Complete API specification for DRACC-019 & DRACC-020 | BA, Developers |

---

## API Summary

### DRACC-019: Internal Cash Transfer

**TradeX Endpoint:** `POST /api/v1/derivatives/transfer/cash`

**Purpose:** Transfer cash between derivatives sub-accounts (internal transfer)

**Key Request Fields:**
- `accountNumber` - Sending account
- `subNumber` - Sending sub
- `receivingAccountNumber` - Receiving account
- `receivingSubNumber` - Receiving sub
- `amount` - Transfer amount
- `note` - Transfer note

**Key Response Fields:**
- Transaction date & sequence numbers
- Balance before & after (both sender & receiver)

---

### DRACC-020: Transfer History Query

**TradeX Endpoint:** `GET /api/v1/derivatives/transfer/cash/history`

**Purpose:** Query internal transfer history with pagination support

**Key Request Parameters:**
- `accountNumber` - Account to query
- `fromDate` - Start date (yyyyMMdd)
- `toDate` - End date (yyyyMMdd)
- `nextData` - Pagination key

**Key Response Fields:**
- Transaction details (date, accounts, amounts)
- Transfer notes and status
- Next pagination key

---

## Use Cases

### UC-1: Transfer Between Sub-Accounts (Same Account)

**Scenario:** User transfers cash from main sub (00) to sub-account (01)

```
Account: 0001234567
From Sub: 00 (Main)
To Sub: 01 (Trading)
Amount: 10,000,000 VND
```

**API Call:** DRACC-019

---

### UC-2: View Transfer History

**Scenario:** User views all transfers in last month

```
Account: 0001234567
From: 2026-01-01
To: 2026-02-09
```

**API Call:** DRACC-020 (with pagination)

---

## Business Rules

### Transfer Rules

1. **Required Fields:** All fields mandatory for transfer request
2. **Amount Validation:** Must be positive number, sufficient balance required
3. **Same Sub Restriction:** Cannot transfer to same account+sub combination
4. **Real-time Execution:** Transfer executes immediately (atomic operation)
5. **Transaction IDs:** Each side (sender/receiver) gets unique sequence number

### History Query Rules

1. **Date Range:** Required, with maximum range limit (e.g., 3-6 months)
2. **Pagination:** Use `nextData` for subsequent pages
3. **Account Filter:** Shows all transfers (sent & received) for the account

---

## Technical Notes

### Field Naming Consistency

**Important:** Derivatives uses different naming than Equity:

| Field Purpose | Derivatives | Equity (different!) |
|--------------|-------------|---------------------|
| Receiving account | `receivingAccountNumber` | `receivedAccountNumber` |
| Receiving sub | `receivingSubNumber` | `receivedSubNumber` |

Keep Derivatives naming consistent across all APIs.

### Auto-populated Fields

Following TradeX conventions (see rule `@tradex-order-api-response-standards`):

- `userId` → From JWT Token
- `username` → From JWT Token

These fields are NOT in request body.

### Amount Handling

- **Request:** Number type
- **Lotte:** String type
- **Response:** Parse back to Number type
- **Precision:** Maintain full precision (no rounding)

---

## Implementation Status

### Current Phase: Planning

**Completed:**
- ✅ API specs analysis (DRACC-019, DRACC-020)
- ✅ Field mapping documentation
- ✅ Business rules definition
- ✅ Use case identification

**Next Steps:**
1. Create DTOs for request/response
2. Implement API endpoints in `rest-proxy`
3. Implement service layer in `lotte-bridge`
4. Add validation & error handling
5. Write tests (unit + integration)
6. Update Swagger documentation

---

## Related Modules

| Module | Location | Relationship |
|--------|----------|--------------|
| VSD Transaction | `../VSD transaction/` | Related cash transaction feature |
| Equity Transfer | TradeX MCP Knowledge | Similar pattern for Equity market |

---

## Reference Documents

- **Lotte API Specs:** `/Derivatives/Documentation/[API specs]Lotte_DR.md` (Section 2.2.2, 2.2.3)
- **TradeX API Conventions:** `@TradeX Knowledge/API Standards/tradex-api-conventions.md`
- **Order API Standards:** Rule `@tradex-order-api-response-standards`
- **Equity Transfer (Reference):** TradeX MCP Knowledge `/rest-proxy-main/src/app/routes/api/equity/*/Transfer.ts`

---

## Questions & Issues

### Open Questions

1. **DRACC-019 URL:** Specific endpoint needs confirmation (currently marked as `[Root URL APIKEY]`)
2. **Maximum Date Range:** Confirm allowed date range for history query (3 months? 6 months?)
3. **Cross-Account Transfer:** Requires additional authorization? What are the rules?
4. **Page Size:** What's the default/maximum page size for history query?

### Known Issues

None at this stage (planning phase).

---

## Contact

- **BA Team:** For business requirements and API mapping questions
- **Backend Team:** For implementation and technical questions

---

**Prepared By:** BA Team  
**Document Version:** 1.0  
**Last Review:** February 9, 2026
