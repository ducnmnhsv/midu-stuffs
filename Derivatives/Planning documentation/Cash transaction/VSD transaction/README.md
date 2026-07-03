# VSD Transaction - Derivatives Cash Transaction

> **Module:** VSD Transaction  
> **Category:** Cash Transaction  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** February 9, 2026  
> **Status:** Planning

---

## Overview

VSD Transaction module enables margin deposit/withdrawal to/from VSD (Vietnam Securities Depository) for derivatives trading.

### Key Features

| Feature | Lotte APIs | Status | Description |
|---------|-----------|--------|-------------|
| Get Bank List | DRACC-032 | 📋 Planning | Query available bank accounts |
| Get VSD Balance | DRACC-031 | 📋 Planning | Query VSD cash balance & withdrawable amount |
| Calculate Fee | DRACC-033 | 📋 Planning | Calculate deposit transaction fee |
| Deposit Margin (C05) | DRACC-034 | 📋 Planning | Transfer cash from NHSV to VSD |
| Withdraw Margin (C10) | DRACC-009 | 📋 Planning | Transfer cash from VSD to NHSV |
| Query History | DRACC-021 | 📋 Planning | Query deposit/withdraw history |

---

## Quick Access

| Document | Description | Audience |
|----------|-------------|----------|
| [VSD_Transaction_API_Spec.md](./VSD_Transaction_API_Spec.md) | Complete API specification for DRACC-009, 021, 032, 033, 034 | BA, Developers |

---

## API Summary

### DRACC-032: Get Bank List

**TradeX Endpoint:** `GET /api/v1/derivatives/transfer/vsd/banks`

**Purpose:** Query list of available bank accounts for VSD transactions

**Response:**
- Bank account list with `.R` (NHSV) and `.C` (VSD custody) types

---

### DRACC-031: Get VSD Balance

**TradeX Endpoint:** `GET /api/v1/derivatives/transfer/vsd/balance`

**Purpose:** Query VSD cash balance and withdrawable amount

**Request Parameters:**
- `accountNumber` - Account number
- `inquiryDate` - Query date (yyyyMMdd)

**Response:**
```json
{
  "vsdCashBalance": 50000000,
  "withdrawableCash": 30000000
}
```

---

### DRACC-033: Calculate Deposit Fee

**TradeX Endpoint:** `POST /api/v1/derivatives/transfer/vsd/deposit/fee`

**Purpose:** Calculate transaction fee before deposit

**Response:**
- `feeAmount` - Transaction fee
- `adjustedAmount` - Adjusted amount
- `receivedAmount` - Amount after fees
- `feeType` - Fee calculation type

---

### DRACC-034: Deposit Margin (C05)

**TradeX Endpoint:** `POST /api/v1/derivatives/transfer/vsd/deposit`

**Purpose:** Transfer cash from NHSV to VSD (nộp ký quỹ)

**Direction:** NHSV (`.R`) → VSD (`.C`)

**Response:**
```json
{
  "success": true
}
```

**Note:** 
- Must call DRACC-033 first to get fee information
- Returns boolean only (no message or transaction details)

---

### DRACC-009: Withdraw Margin (C10)

**TradeX Endpoint:** `POST /api/v1/derivatives/transfer/vsd/withdraw`

**Purpose:** Transfer cash from VSD to NHSV (rút ký quỹ)

**Direction:** VSD (`.C`) → NHSV (`.R`)

**Response:**
```json
{
  "success": true
}
```

**Note:** 
- Returns boolean only (no message or transaction details)
- Method is POST (not GET) for mutation operation

---

### DRACC-021: Query History

**TradeX Endpoint:** `GET /api/v1/derivatives/transfer/vsd/history`

**Purpose:** Query VSD transaction history with pagination

**Filters:**
- `transactionType`: `C05` (Deposit), `C10` (Withdraw), `%` (All)
- Date range

---

## Key Concepts

### Transaction Types

| Code | Name | Direction | Description |
|------|------|-----------|-------------|
| `C05` | Deposit | NHSV → VSD | Nộp tiền ký quỹ vào VSD |
| `C10` | Withdraw | VSD → NHSV | Rút tiền ký quỹ từ VSD về NHSV |

### Bank Account Types

Bank accounts are identified by `bicCodeBankType` suffix:

| Type | Suffix | Purpose | Account Name Example |
|------|--------|---------|---------------------|
| **Registered** | `.R` | NHSV account | "CONG TY CHUNG KHOAN NH" |
| **Custody** | `.C` | VSD custody account | "VSD KY QUY KHACH HANG NH C" |

**Routing Rules:**

```
Deposit (C05):  Source = .R bank  →  Destination = .C bank
Withdraw (C10): Source = .C bank  →  Destination = .R bank
```

---

## Use Cases

### UC-1: Deposit Margin to VSD

**Scenario:** User deposits 10,000,000 VND to VSD for derivatives trading

**Flow:**

1. **Check Current Balance** (DRACC-031 - Optional)
   ```json
   Request: {
     accountNumber: "0001234567",
     inquiryDate: "20260209"
   }
   
   Response: {
     vsdCashBalance: 40000000,
     withdrawableCash: 25000000
   }
   ```

2. **Frontend Request** (DRACC-033 - Calculate Fee)
   ```json
   Request: {
     accountNumber: "0001234567",
     amount: 10000000
   }
   ```

3. **Backend Processing:**
   - Call DRACC-032 to get bank list
   - Auto-select banks: `.R` bank (NHSV) and `.C` bank (VSD)
   - Call DRACC-033 with selected banks
   
   ```json
   Response: {
     feeAmount: 50000,
     receivedAmount: 9950000,
     feeType: "01"
   }
   ```

4. **Frontend Request** (DRACC-034 - Execute Deposit)
   ```json
   Request: {
     accountNumber: "0001234567",
     amount: 10000000,
     note: "Deposit margin",      // Optional, can be empty
     feeAmount: 50000,             // From step 3
     adjustedAmount: 9950000,      // From step 3
     receivedAmount: 9950000,      // From step 3
     feeType: "01"                 // From step 3
   }
   ```

5. **Backend Processing:**
   - Call DRACC-032 to get bank list
   - Auto-select banks again (same as step 3)
   - Call DRACC-034 with selected banks
   
   ```json
   Response: {
     success: true
   }
   ```

**Note:** 
- FE does NOT send bank account numbers. BE handles bank selection automatically.
- Response is simple boolean, no message or transaction details
- Step 1 (Check Balance) is optional but recommended

---

### UC-2: Withdraw Margin from VSD

**Scenario:** User withdraws 5,000,000 VND from VSD back to NHSV

**Flow:**

1. **Check Available Balance** (DRACC-031)
   ```json
   Request: {
     accountNumber: "0001234567",
     inquiryDate: "20260209"
   }
   
   Response: {
     vsdCashBalance: 50000000,
     withdrawableCash: 30000000  // Can withdraw up to 30M
   }
   ```

2. **Frontend Request** (DRACC-009)
   ```json
   Request: {
     accountNumber: "0001234567",
     amount: 5000000,
     note: "Withdraw margin"  // Optional, can be empty
   }
   ```

3. **Backend Processing:**
   - Call DRACC-032 to get bank list
   - Auto-select banks (reverse of deposit):
     - Source: `.C` bank (VSD account)
     - Destination: `.R` bank (NHSV account)
   - Call DRACC-009 with selected banks
   
   ```json
   Response: {
     success: true
   }
   ```

**Note:** 
- FE does NOT send bank account numbers. BE handles bank selection automatically (reverse direction from deposit).
- Response is simple boolean, no message or transaction details
- Step 1 (Check Balance) is recommended to validate withdrawable amount

---

### UC-3: View Transaction History

**Scenario:** User views all VSD transactions in last month

**API Call:** DRACC-021

```json
Request: {
  accountNumber: "0001234567",
  transactionType: "%",  // All transactions
  fromDate: "20260101",
  toDate: "20260209"
}
```

---

## Business Rules

### Deposit Rules (C05)

1. **Two-Step Process:** Must call DRACC-033 first, then DRACC-034
2. **Bank Direction:** `.R` → `.C`
3. **Fee Deduction:** Fee deducted from deposit amount
4. **VSD Approval:** Transaction requires VSD system approval

### Withdraw Rules (C10)

1. **Single-Step Process:** Direct call to DRACC-009
2. **Bank Direction:** `.C` → `.R`
3. **Balance Check:** Must have sufficient margin in VSD
4. **VSD Approval:** Transaction requires VSD system approval

### History Query Rules

1. **Date Range:** Maximum range (TBD, typically 3-6 months)
2. **Transaction Filter:** `C05`, `C10`, or `%` (all)
3. **Pagination:** Use `next_data` field (default: `"000000000000000"`)
4. **Status Fields:** Three status levels (VTB, BOS, VSD)

---

## Technical Notes

### ⚠️ Important: Lotte Field Name Confusion (DRACC-034)

Lotte API DRACC-034 has **reversed** field naming:

| TradeX Concept | TradeX Field | Lotte Field | Bank Type |
|----------------|--------------|-------------|-----------|
| Source (NHSV) | `sourceBankAccountNumber` | `is_in_bank_dest` ⚠️ | `.R` |
| Destination (VSD) | `destinationBankAccountNumber` | `is_in_bank_src` ⚠️ | `.C` |

**Why?** Lotte's perspective is from VSD system:
- `is_in_bank_src` = where money comes **into VSD from** (VSD's source = TradeX's dest)
- `is_in_bank_dest` = where money goes **out to VSD** (VSD's dest = TradeX's source)

**Action:** Document this clearly in implementation!

### Bank Routing Helper

**Backend automatically handles bank selection:**

```typescript
// FE sends only: accountNumber, amount, note
// BE auto-selects banks from DRACC-032 response

For Deposit:  Source = .R bank (NHSV)  →  Dest = .C bank (VSD)
For Withdraw: Source = .C bank (VSD)   →  Dest = .R bank (NHSV)
```

**FE does NOT need to:**
- Call DRACC-032 (Get Bank List)
- Select or send bank account numbers
- Understand `.R`/`.C` bank types

**BE handles:**
- Calling DRACC-032 internally
- Filtering banks by type (`.R` or `.C`)
- Selecting appropriate banks based on transaction type
- Passing bank accounts to Lotte APIs

### Auto-populated Fields

Following TradeX conventions:

- `userId` → From JWT Token
- `dept_no1` → From configuration (branch code)
- `lang_code` → From `Accept-Language` header

---

## Implementation Status

### Current Phase: Planning

**Completed:**
- ✅ API specs analysis (DRACC-009, 021, 032, 033, 034)
- ✅ Field mapping documentation
- ✅ Bank routing logic design
- ✅ Business rules definition
- ✅ Use case identification

**Next Steps:**
1. Implement bank list query (DRACC-032) with caching
2. Implement fee calculation (DRACC-033)
3. Implement deposit flow (DRACC-034) with two-step process
4. Implement withdraw (DRACC-009)
5. Implement history query (DRACC-021) with pagination
6. Add validation & error handling
7. Write tests (unit + integration)
8. Update Swagger documentation

---

## Related Modules

| Module | Location | Relationship |
|--------|----------|--------------|
| Internal Transfer | `../Internal transfer/` | Related cash transaction feature |

---

## Reference Documents

- **Lotte API Specs:** `/Derivatives/Documentation/Lotte_DR_API_Specs.md` (Section 2.2.1, 2.2.4, 2.2.5, 2.2.6, 2.2.7)
- **TradeX API Conventions:** `@Knowledge/TradeX/API Standards/tradex-api-conventions.md`
- **Order API Standards:** Rule `@tradex-order-api-response-standards`
- **Internal Transfer (Reference):** `../Internal transfer/Internal_Transfer_API_Spec.md`

---

## Questions & Issues

### Open Questions

1. **DRACC-021 URL:** Specific endpoint URL not documented
2. **Bank List Caching:** Cache duration? Refresh strategy?
3. **Fee Validity:** How long is fee calculation valid?
4. **Transaction Polling:** How to check VSD approval status?
5. **Maximum Amounts:** Min/max deposit/withdraw limits?

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
