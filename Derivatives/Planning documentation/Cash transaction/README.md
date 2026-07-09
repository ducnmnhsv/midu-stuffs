# Cash Transaction - Derivatives Integration

> **Module:** Cash Transaction  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** February 9, 2026  
> **Status:** Planning

---

## 📋 Quick Navigation

| Section | Description |
|---------|-------------|
| [Overview](#overview) | Business context and scope |
| [Sub-modules](#sub-modules) | Internal Transfer & VSD Transaction |
| [Implementation Status](#implementation-status) | Current progress |
| [Documentation Map](#documentation-map) | All docs index |
| [How to Use](#how-to-use) | Role-based guide |

---

## 🎯 Overview

### Mission

Cash Transaction module handles all money-related operations for derivatives trading, including:
- Internal transfers between sub-accounts
- VSD (Vietnam Securities Depository) transactions (deposit/withdraw margin)

### Scope

| In Scope | Out of Scope |
|----------|--------------|
| ✅ Internal cash transfers (DRACC-019, DRACC-020) | ❌ Equity cash transfers |
| ✅ VSD margin deposit/withdrawal (DRACC-009, DRACC-021) | ❌ Payment gateway integration |
| ✅ Cash statement / Lịch sử thanh toán (DRACC-023) | ❌ Bank reconciliation |
| ✅ External bank withdrawal (DRACC-039, DRACC-040, DRACC-041) | |
| ✅ Transfer history queries | |
| 📋 Transaction fee calculation | |

---

## 🏗️ Architecture

### High-Level Flow

```
User Request
    ↓
TradeX REST API (rest-proxy)
    ↓
Lotte Bridge Service (lotte-bridge)
    ↓
Lotte Core API
    ↓
VSD System (for margin transactions)
```

### Key Services

| Service | Role | Technology |
|---------|------|------------|
| `rest-proxy` | API Gateway & Request validation | Node.js + Express |
| `lotte-bridge` | Business logic & Lotte integration | Node.js |
| Lotte Core | Securities operations | Java |
| VSD System | Central depository | External |

---

## 📦 Sub-modules

### 1. Internal Transfer

> **Location:** [`./Internal transfer/`](./Internal%20transfer/)

**Purpose:** Transfer cash between derivatives sub-accounts

**APIs:**
- DRACC-019: Execute internal transfer
- DRACC-020: Query transfer history

**Status:** 📋 Planning (API mapping completed)

[→ See Internal Transfer Documentation](./Internal%20transfer/README.md)

---

### 2. VSD Transaction

> **Location:** [`./VSD transaction/`](./VSD%20transaction/)

**Purpose:** Deposit/withdraw margin to/from VSD system

**APIs:**
- DRACC-032: Get bank list
- DRACC-033: Calculate deposit fee
- DRACC-034: Deposit margin (C05)
- DRACC-009: Withdraw margin (C10)
- DRACC-021: Query VSD transaction history

**Status:** 📋 Planning (API spec completed)

[→ See VSD Transaction Documentation](./VSD%20transaction/README.md)

---

### 3. Cash Statement

> **Location:** [`./Cash statement/`](./Cash%20statement/)

**Purpose:** Tra cứu lịch sử thanh toán (sao kê tiền) phái sinh theo ngày

**APIs:**
- DRACC-023: Lịch sử thanh toán (Payment history)

**Status:** 📋 Planning (API spec completed)

[→ See Cash Statement Documentation](./Cash%20statement/README.md)

---

### 4. External Withdrawal

> **Location:** [`./External withdrawal/`](./External%20withdrawal/)

**Purpose:** Rút tiền mặt từ sub phái sinh về tài khoản ngân hàng ngoài

**APIs:**
- DRACC-039: Query withdrawal history
- DRACC-040: Execute withdrawal
- DRACC-041: Query available withdrawal balance

**Status:** 🔄 Draft (pending PM confirmation of Open Questions)

[→ See External Withdrawal Documentation](./External%20withdrawal/README.md)

---

## 📊 Implementation Status

### ✅ Completed Features

| Feature | APIs | Status | Documents |
|---------|------|--------|-----------|
| _(None yet)_ | - | - | - |

### 📋 Pending Implementation

| Feature | APIs | Status | Priority | Estimate |
|---------|------|--------|----------|----------|
| Internal Transfer | DRACC-019, DRACC-020 | 📋 API Spec Done | High | 2-3 weeks |
| VSD Transaction | DRACC-009, 021, 032, 033, 034 | 📋 API Spec Done | High | 3-4 weeks |
| Cash Statement | DRACC-023 | 📋 API Spec Done | Medium | 1-2 weeks |
| External Withdrawal | DRACC-039, 040, 041 | 🔄 Draft — pending Open Questions | High | TBD |

---

## 📚 Documentation Map

### Sub-module Documentation

| # | Module | README | API Specification | Status |
|---|--------|--------|-------------------|--------|
| 1 | Internal Transfer | [README.md](./Internal%20transfer/README.md) | [API Spec](./Internal%20transfer/Internal_Transfer_API_Spec.md) | 📋 Planning |
| 2 | VSD Transaction | [README.md](./VSD%20transaction/README.md) | [API Spec](./VSD%20transaction/VSD_Transaction_API_Spec.md) | 📋 Planning |
| 3 | Cash Statement | [README.md](./Cash%20statement/README.md) | [API Spec](./Cash%20statement/Cash_Statement_API_Spec.md) | 📋 Planning |
| 4 | External Withdrawal | [README.md](./External%20withdrawal/README.md) | [API Spec](./External%20withdrawal/Sub_Account_Withdrawal_API_Spec.md) | 🔄 Draft |

### Reference Documents

| Document | Type | Audience | Description |
|----------|------|----------|-------------|
| [Lotte API Specs](../../Documentation/Lotte_DR_API_Specs.md) | API Specs | All | Lotte Derivatives API specifications (Section 2.2) |
| TradeX API Conventions | Standards | Developers | TradeX API naming & response standards |

---

## 👥 How to Use This Documentation

### For BA/PM

**Goal:** Understand business requirements and API mappings

**Start Here:**
1. Read this README for overview
2. Navigate to specific sub-module README (Internal Transfer / VSD Transaction)
3. Review API mapping documents for detailed field mappings
4. Check business rules and use cases

**Key Documents:**
- Sub-module READMEs
- API mapping documents
- Lotte API specs (Section 2.2)

---

### For Developers

**Goal:** Implement APIs following documented mappings

**Start Here:**
1. Read sub-module README for context
2. Review API mapping document for field transformations
3. Reference Equity Transfer implementation as pattern
4. Follow TradeX API conventions for naming & structure

**Key Documents:**
- API mapping documents (detailed field mappings)
- TradeX API conventions (naming rules)
- Equity Transfer code (pattern reference)

**Implementation Pattern:**

```typescript
// 1. Create DTOs in rest-proxy
interface DerivativesCashTransferRequest { ... }
interface DerivativesCashTransferResponse { ... }

// 2. Create route in rest-proxy
POST /api/v1/derivatives/transfer/cash

// 3. Implement service in lotte-bridge
async transferCash(request: IDerivativesCashTransferRequest, ctx: IContext): Promise<IDerivativesCashTransferResponse>

// 4. Map to Lotte request
const lotteRequest = {
  snd_actn: request.accountNumber.toUpperCase(),
  // ... (see API mapping doc)
};

// 5. Transform response
return {
  transactionDate: lotteResponse.date,
  // ... (see API mapping doc)
};
```

---

### For QA

**Goal:** Design test scenarios and validate implementations

**Start Here:**
1. Read sub-module README for business context
2. Review use cases section
3. Check business rules for validation criteria
4. Review API mapping for edge cases

**Key Documents:**
- Use cases in sub-module READMEs
- Business rules sections
- API mapping documents (validation rules)

**Test Focus Areas:**
- Required field validation
- Amount validation (positive, sufficient balance)
- Date range validation (history queries)
- Pagination flow (history queries)
- Error handling (Lotte error codes)

---

## 🔗 Related Categories

| Category | Location | Relationship |
|----------|----------|--------------|
| Order | `../Order/` | Uses cash balance from transactions |
| Account | `../Account/` | Account info needed for transfers |
| Market data | `../Market data/` | No direct relationship |

---

## 📝 Key Concepts

### Sub-Account Structure

```
Account: 0001234567
├── Sub 00 (Main)
├── Sub 01 (Trading Sub 1)
├── Sub 02 (Trading Sub 2)
└── ...
```

**Internal Transfer:** Move cash between subs (00 → 01, 01 → 02, etc.)

### VSD Transactions

**VSD (Vietnam Securities Depository):** Central depository for securities in Vietnam

**Transaction Types:**
- **C05:** Deposit margin to VSD (move cash from NHSV to VSD)
- **C10:** Withdraw margin from VSD (move cash from VSD to NHSV)

**Purpose:** Margin management for derivatives trading

---

## ⚠️ Important Notes

### Derivatives vs Equity Naming

**Critical:** Derivatives uses different field names than Equity!

| Field Purpose | Derivatives | Equity |
|--------------|-------------|--------|
| Receiving account | `receivingAccountNumber` | `receivedAccountNumber` |
| Receiving sub | `receivingSubNumber` | `receivedSubNumber` |

**Action:** Always use Derivatives naming in this category. Do NOT copy Equity naming.

### Auto-populated Fields

Following TradeX conventions (rule `@tradex-order-api-response-standards`):

- `userId` → Extracted from JWT Token
- `username` → Extracted from JWT Token
- `sourceIp` → Extracted from Request IP

These fields are **NOT** in request body.

### Error Handling

**Success:** Lotte `error_code: "0000"`  
**Failure:** Any other code

**Error Format Pattern:**

```json
{
  "code": "DERIVATIVES_{OPERATION}_{LOTTE_CODE}",
  "message": "[Lotte error description]"
}
```

**Examples:**
- `DERIVATIVES_CASH_TRANSFER_1005`
- `DERIVATIVES_TRANSFER_HISTORY_1005`

---

## ❓ Open Questions

### General Questions

1. **Transaction Limits:**
   - What's the minimum/maximum transfer amount?
   - Daily transfer limit per account?

2. **Authorization:**
   - Does cross-account transfer require additional authorization?
   - Role-based access control for VSD transactions?

3. **Fees:**
   - Are there fees for internal transfers?
   - How are VSD transaction fees calculated?

4. **Business Hours:**
   - Are transfers available 24/7 or only during trading hours?
   - VSD transaction cutoff times?

### Technical Questions

1. **Pagination:**
   - Default page size for history queries?
   - Maximum page size allowed?

2. **Date Range:**
   - Maximum date range for history queries (3 months? 6 months?)?
   - Historical data retention policy?

3. **Real-time Updates:**
   - Do we need WebSocket notifications for transfers?
   - How to handle concurrent transfers?

---

## 🚀 Getting Started

### For New Team Members

**Recommended Reading Order:**

1. **Start here:** This README (overview & architecture)
2. **Deep dive:** Internal Transfer README & API Mapping
3. **Reference:** Lotte API Specs (Section 2.2)
4. **Pattern:** Equity Transfer code (for implementation pattern)
5. **Standards:** TradeX API Conventions & Order API Standards

### Quick Reference

**Key Lotte APIs:**

| API | Purpose | Document Section |
|-----|---------|------------------|
| DRACC-019 | Internal cash transfer | [API Mapping](./Internal%20transfer/Internal_Transfer_API_Mapping.md#dracc-019-internal-cash-transfer) |
| DRACC-020 | Transfer history | [API Mapping](./Internal%20transfer/Internal_Transfer_API_Mapping.md#dracc-020-transfer-history-query) |
| DRACC-023 | Cash statement / Lịch sử thanh toán | [Cash Statement Spec](./Cash%20statement/Cash_Statement_API_Spec.md) |
| DRACC-009 | VSD margin withdrawal | Lotte API Specs 2.2.1 |
| DRACC-021 | VSD transaction history | Lotte API Specs 2.2.4 |

---

## 📦 Related Folders

| Folder | Content | Status |
|--------|---------|--------|
| `/Derivatives/Planning documentation/Order/` | Order management APIs | In Progress |
| `/Derivatives/Planning documentation/Market data/` | Market data integration | Completed |
| `/Derivatives/Planning documentation/Account/` | Account information APIs | Planned |

---

**Prepared By:** BA Team  
**Last Review:** February 9, 2026  
**Document Version:** 1.0
