# Order Types & Validation Rules

**Document Type:** Planning (PM-Friendly, NO CODE)  
**Category:** Orders - Business Rules  
**Version:** 1.1  
**Date:** February 4, 2026

> **📘 API Standards:** This follows TradeX API conventions  
> Technical details: `@Knowledge/TradeX/API Standards/tradex-api-conventions.md`  
> API Specs: `../Specifications/Regular_Orders_API_Spec.md`

---

## 📋 Overview

This document explains all order types, validity types, and validation rules for derivatives regular orders from a **business perspective**.

---

## 🎯 Regular Order Types

### 1. LO (Limit Order) - Lệnh Giới Hạn

**Code:** `2`  
**Session:** All sessions  
**Most Common:** ✅ Yes (80%+ of orders)

**Business Logic:**
- Trader specifies exact price
- Only executes at that price or better
- "Better" means: Lower for Buy, Higher for Sell

**Example - Buy:**
```
Place LO Buy at 1,250
→ Will execute at 1,250 or lower (e.g., 1,248 is OK)
→ Will NOT execute at 1,252 (too high)
```

**Example - Sell:**
```
Place LO Sell at 1,250
→ Will execute at 1,250 or higher (e.g., 1,252 is OK)
→ Will NOT execute at 1,248 (too low)
```

**Use Case:** When trader wants price control

---

### 2. ATO (At-The-Opening) - Lệnh Khớp Mở Cửa

**Code:** `3`  
**Session:** Opening only (09:00-09:15)  
**Popularity:** Moderate (10-15%)

**Business Logic:**
- Executes during opening auction
- Price determined by order book balance
- All ATO orders get same opening price

**How It Works:**
```
09:00 - 09:15  → Traders place ATO orders
09:15 exactly  → Exchange calculates opening price
09:15+         → All ATO orders execute at opening price
```

**Use Case:** 
- Traders who want opening price
- Don't care about exact price, just want to enter at open
- Avoid pre-market speculation

**Price Discovery:**
- Exchange aggregates all ATO buy/sell orders
- Finds price that maximizes matched volume
- That becomes the opening price

---

### 3. ATC (At-The-Close) - Lệnh Khớp Đóng Cửa

**Code:** `6`  
**Session:** Closing only (14:30-14:45)  
**Popularity:** Moderate (10-15%)

**Business Logic:**
- Executes during closing auction
- Price determined by order book balance
- All ATC orders get same closing price

**How It Works:**
```
14:30 - 14:45  → Traders place ATC orders
14:45 exactly  → Exchange calculates closing price
14:45+         → All ATC orders execute at closing price
```

**Use Case:**
- Position adjustments before market close
- Index fund rebalancing
- Avoid end-of-day price manipulation

---

### 4. MOK (Market Or Kill) - Lệnh Thị Trường Hoặc Hủy

**Code:** `5`  
**Session:** Continuous trading only  
**Popularity:** Low (< 5%)

**Business Logic:**
- Execute **completely** at best available price **immediately**
- If cannot fill completely → Cancel entire order
- "All or Nothing"

**Example:**
```
Place MOK Buy 50 contracts
  Available in order book: 30 contracts at 1,250
                          + 15 contracts at 1,251
                          = 45 contracts total
→ Result: Order CANCELLED (cannot fill all 50)
```

**Use Case:**
- Large orders where partial fill is not acceptable
- Institutions needing specific position size
- Rare for retail traders

**Risk:** High rejection rate if order book thin

---

### 5. MAK (Market At Kill) - Lệnh Thị Trường Và Hủy

**Code:** `4`  
**Session:** Continuous trading only  
**Popularity:** Moderate (5-10%)

**Business Logic:**
- Execute **as much as possible** at best available price **immediately**
- Cancel whatever cannot be filled
- "Fill what you can"

**Example:**
```
Place MAK Buy 50 contracts
  Available in order book: 30 contracts at 1,250
                          + 15 contracts at 1,251
                          = 45 contracts total
→ Result: 
  - Matched: 45 contracts (30@1,250 + 15@1,251)
  - Cancelled: 5 contracts (unfilled portion)
```

**Use Case:**
- Quick entry/exit
- Don't want to wait in queue
- Willing to pay market price
- Accept partial fills

**Risk:** May not get desired quantity

---

### 6. MTL (Market To Limit) - Lệnh Chuyển Đổi

**Code:** `9`  
**Session:** Continuous trading only  
**Popularity:** Low (< 5%)

**Business Logic:**
- Try to execute as market order **first**
- Whatever doesn't fill → Converts to Limit Order at best price tried
- "Market with safety net"

**Example:**
```
Place MTL Buy 50 contracts
  Market Best Offer: 1,250 (30 contracts available)
  Next Best Offer: 1,251 (15 contracts available)

→ Immediate Match: 30 contracts at 1,250
→ Converts to LO: 20 contracts at 1,250 (pending)

Result:
  - Matched: 30 at 1,250 (immediately)
  - Pending: 20 at 1,250 (as LO)
```

**Use Case:**
- Want quick execution like market order
- But also want price protection
- Don't want to chase price up (Buy) or down (Sell)

**Advantage:** Combines speed + control

---

## 📅 Validity Types (Order Duration)

### 1. DAY - Lệnh Ngày

**Code:** `0`  
**Default:** Yes  
**Most Common:** ✅ (95%+)

**Business Logic:**
- Valid until end of current trading day
- Auto-cancelled at market close if not matched
- Most standard validity type

**Timeline:**
```
Place order: 10:00 AM
Market close: 14:45 PM
→ If not matched by 14:45, automatically cancelled
→ User notified: "Order cancelled - end of day"
```

---

### 2. ATO - Mở Cửa

**Code:** `2`  
**Use:** Only for ATO order type

**Business Logic:**
- Only valid during opening auction
- Auto-cancelled if opening auction fails
- Specific to opening session

---

### 3. IOC (Immediate Or Cancel)

**Code:** `3`  
**Rare:** Advanced traders only

**Business Logic:**
- Execute immediately what's available
- Cancel rest immediately
- Similar to MAK but even faster

---

### 4. FOK (Fill Or Kill)

**Code:** `4`  
**Rare:** Advanced traders only

**Business Logic:**
- Execute completely immediately or cancel all
- Identical to MOK
- Alternative term used in some markets

---

### 5. ATC - Đóng Cửa

**Code:** `7`  
**Use:** Only for ATC order type

**Business Logic:**
- Only valid during closing auction
- Auto-cancelled if closing auction fails
- Specific to closing session

---

## ✅ Validation Rules

### 1. Price Validation

#### Tick Size Rule

**VN30F Tick Size:** 0.1 point

**Valid Prices:**
- ✅ 1,250.0
- ✅ 1,250.5
- ✅ 1,251.0
- ❌ 1,250.25 (not multiple of 0.1)
- ❌ 1,250.15 (not multiple of 0.1)

**Business Rule:** 
> Price must be multiple of 0.1 points

#### Daily Price Limit

**VN30F:** ±7% from reference price

**Example:**
```
Reference Price: 1,250
Ceiling: 1,250 + (1,250 × 7%) = 1,337.5
Floor: 1,250 - (1,250 × 7%) = 1,162.5

Valid Range: 1,162.5 to 1,337.5
```

**Business Rule:**
> Cannot place order outside daily limit

**Exception:** Market orders (MOK, MAK, MTL) - No price limit check

---

### 2. Quantity Validation

**Minimum:** 1 contract  
**Maximum:** Exchange/Account limit

**Rules:**
- ✅ Must be positive integer
- ✅ Must be >= 1
- ✅ Must not exceed account limit
- ✅ Must not exceed exchange limit

**Example:**
```
✅ Valid: 1, 10, 50, 100
❌ Invalid: 0, -5, 0.5, 1000000
```

---

### 3. Session Validation

| Order Type | Opening (ATO) | Continuous (LO) | Closing (ATC) |
|------------|---------------|-----------------|---------------|
| **LO** | ✅ | ✅ | ✅ |
| **ATO** | ✅ | ❌ | ❌ |
| **ATC** | ❌ | ❌ | ✅ |
| **MOK** | ❌ | ✅ | ❌ |
| **MAK** | ❌ | ✅ | ❌ |
| **MTL** | ❌ | ✅ | ❌ |

**Business Rule:**
> Each order type has allowed sessions. System blocks invalid combinations.

---

### 4. Margin Validation

**Before Order Placement:**

```
Required Margin = Contract Value × Margin Rate × Quantity

Example:
  VN30F2501 = 1,250 points
  Margin Rate = 10%
  Quantity = 10 contracts
  Contract Multiplier = 100,000 VND per point

Required Margin = 1,250 × 100,000 × 10% × 10
                = 125,000,000 VND

If Available Margin < 125,000,000 → Block Order
```

**Business Rule:**
> Must have sufficient margin to cover order

---

### 5. Symbol Validation

**Checks:**
- ✅ Symbol exists in system
- ✅ Symbol is active (not expired)
- ✅ Symbol not suspended
- ✅ Symbol in derivatives category

**Example:**
```
✅ Valid: VN30F2501, VN30F2502
❌ Invalid: VN30F2101 (expired), HPG (not futures)
```

---

### 6. Cancel/Modify Validation

**Can Cancel/Modify IF:**
- ✅ Order exists
- ✅ Order from today
- ✅ Order is PENDING or PARTIAL
- ✅ Has unmatched quantity > 0
- ✅ User owns the order

**Cannot Cancel/Modify IF:**
- ❌ Order fully matched
- ❌ Order from previous day
- ❌ Order already cancelled
- ❌ Not user's order

---

## ⚠️ Business Edge Cases

### 1. Price Gaps (GAP)

**Scenario:** Market opens with gap (e.g., from 1,250 to 1,300)

**LO Orders:**
- Buy LO at 1,260 → Executes at opening price (1,300) ❌ Not better
- Actually → Stays pending or cancelled

**Market Orders (MOK/MAK):**
- Execute at new market price (1,300)
- Trader might get worse price than expected

**Business Rule:**
> LO orders respect price limit even with gaps

---

### 2. Partial Fills

**Scenario:** Order partially matched

**User Options:**
- Wait for rest to match
- Cancel remaining quantity
- Modify remaining quantity

**Example:**
```
Place: Buy 50 @ 1,250
Match: 30 @ 1,250
Status: 
  - Matched: 30
  - Pending: 20

Actions Available:
  ✅ Cancel remaining 20
  ✅ Modify remaining 20 (new price/qty)
  ❌ Cannot cancel the matched 30
```

---

### 3. Order Rejection

**Common Reasons:**
1. Insufficient margin → User deposits more or reduces qty
2. Invalid price → User adjusts to valid price
3. Market closed → User waits for open
4. Symbol suspended → User chooses different symbol
5. Duplicate order → System blocks (prevents double entry)

**Business Rule:**
> Clear error messages guide user to fix and resubmit

---

## 📊 Order Type Selection Guide

### For PM: When to recommend which order type?

| Trader Need | Recommended Order Type | Reason |
|-------------|------------------------|--------|
| **Specific price control** | LO | Exact price, no surprises |
| **Quick entry at open** | ATO | Get opening price |
| **Quick exit at close** | ATC | Get closing price |
| **Fast execution, any price** | MAK | Speed over price |
| **All-or-nothing** | MOK | Full quantity or nothing |
| **Fast + some control** | MTL | Market speed + limit safety |

---

## 🎯 Key Business Rules Summary

### Placement Rules

1. **Margin Check** - Must have sufficient margin
2. **Price Check** - Tick size + price limit
3. **Quantity Check** - Min 1, max account/exchange limit
4. **Session Check** - Order type valid for current session
5. **Symbol Check** - Active and not suspended

### Modification Rules

1. **Modify = Cancel + New** - Not an edit
2. **Can Only Modify Pending** - Not matched portion
3. **New Order Number** - New order gets new ID
4. **All Validations Apply** - New order follows all rules

### Cancellation Rules

1. **Can Cancel Pending** - Full or partial
2. **Cannot Cancel Matched** - Already executed
3. **Cancel Today Only** - Not previous days
4. **Immediate Effect** - Removed from order book

---

## 📈 Usage Statistics (Expected)

| Order Type | Expected % | User Level |
|------------|------------|------------|
| **LO** | 75-80% | All users |
| **ATO** | 8-10% | Active traders |
| **ATC** | 8-10% | Active traders |
| **MAK** | 3-5% | Experienced |
| **MOK** | 1-2% | Experienced |
| **MTL** | 1-2% | Experienced |

---

**Document Status:** ✅ Complete  
**Audience:** PM, BA, Product Designers  
**Next Review:** After user testing
