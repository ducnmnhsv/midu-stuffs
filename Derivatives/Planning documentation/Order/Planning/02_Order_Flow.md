# Order Flow - Architecture & Data Flow

**Document Type:** Planning (PM-Friendly, NO CODE)  
**Category:** Orders - System Architecture  
**Version:** 1.1  
**Date:** February 4, 2026

> **📘 API Standards:** This follows TradeX API conventions  
> Technical details: `@Knowledge/TradeX/API Standards/tradex-api-conventions.md`  
> API Specs: `../Specifications/Regular_Orders_API_Spec.md`

---

## 📋 Overview

This document explains **how orders flow through the TradeX system** from the user's perspective, focusing on business logic and data movement.

---

## 🎯 High-Level Order Flow

```
┌─────────────────────────────────────────────────────────────┐
│  STEP 1: User Places Order                                  │
│  Trader → NHSV Pro App → Fill order form → Click "Buy/Sell"│
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 2: Validation                                          │
│  System checks: Margin, Price, Quantity, Session            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 3: Send to Exchange                                    │
│  Order sent to Lotte backend → Exchange order book          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 4: Order Status                                        │
│  Pending → Partially Matched → Fully Matched / Cancelled    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  STEP 5: User Notification                                   │
│  App shows: Order Number, Status, Matched Quantity           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ System Architecture (Business View)

### Components

```
┌──────────────┐
│   Trader     │  → User using NHSV Pro App
│  (Client)    │
└──────┬───────┘
       │
       │ 1. Order Request
       ▼
┌──────────────┐
│  API Gateway │  → Routes requests, checks authentication
│ (rest-proxy) │
└──────┬───────┘
       │
       │ 2. Validated Request
       ▼
┌──────────────┐
│    Order     │  → Processes regular orders
│  Processor   │  → Handles business logic
│  (tuxedo)    │
└──────┬───────┘
       │
       │ 3. Exchange Request
       ▼
┌──────────────┐
│   Lotte API  │  → Exchange backend
│  (Exchange)  │  → Matches orders
└──────┬───────┘
       │
       │ 4. Order Status Updates
       ▼
┌──────────────┐
│   Message    │  → Real-time updates
│   Queue      │  → Notifications
│   (Kafka)    │
└──────┬───────┘
       │
       │ 5. Status Notification
       ▼
┌──────────────┐
│   Client     │  → Receives updates
│    (App)     │
└──────────────┘
```

### Component Responsibilities

| Component | Role | Business Function |
|-----------|------|-------------------|
| **Client App** | User interface | Order entry, display results |
| **API Gateway** | Security, routing | Authentication, authorization |
| **Order Processor** | Business logic | Validation, order management |
| **Lotte API** | Exchange integration | Submit to exchange, get results |
| **Message Queue** | Real-time updates | Notify order status changes |

---

## 📊 Order Operations Flow

### 1. Buy Order Flow

```
User Action: Click "Buy VN30F2501"
     ↓
Fill Form:
  - Quantity: 10 contracts
  - Price: 1,250 points
  - Order Type: LO (Limit Order)
     ↓
System Validation:
  ✓ Account has margin?
  ✓ Price valid (tick size, price limit)?
  ✓ Market open?
  ✓ Symbol active?
     ↓
Send to Exchange:
  → Lotte receives order
  → Assigns order number
  → Places in order book
     ↓
Order Status:
  - PENDING (waiting to match)
  - PARTIAL (matched 5 of 10)
  - FILLED (all 10 matched)
     ↓
User Sees:
  - Order Number: 123456789
  - Status: PENDING → FILLED
  - Matched: 10 contracts at 1,250
```

---

### 2. Cancel Order Flow

```
User Action: Click "Cancel" on pending order
     ↓
System Check:
  ✓ Order exists?
  ✓ Order not fully matched?
  ✓ Order from today?
     ↓
Send Cancel Request:
  → Lotte receives cancel
  → Removes order from book
     ↓
Result:
  - Order Status: CANCELLED
  - Pending Quantity: 0
  - Matched Quantity: (unchanged)
     ↓
User Sees:
  - "Order #123456789 cancelled successfully"
  - Order removed from pending list
```

---

### 3. Modify Order Flow

```
User Action: Click "Modify" on pending order
     ↓
Change Values:
  - Old Price: 1,250
  - New Price: 1,255
  - Old Qty: 10
  - New Qty: 15
     ↓
System Process:
  → Cancel old order (#123456789)
  → Place new order (same parameters, new price/qty)
  → Get new order number (#987654321)
     ↓
Result:
  - Old Order: CANCELLED
  - New Order: PENDING
  - New Order Number: 987654321
     ↓
User Sees:
  - "Order modified successfully"
  - New order number in pending list
  - Old order removed
```

**Important:** Modify is NOT an edit. It's a cancel + new order operation.

---

### 4. Query Unmatch Orders Flow

```
User Action: View "Pending Orders" screen
     ↓
System Query:
  → Request today's unmatch orders from Lotte
  → Filter: account number, today's date
     ↓
Lotte Returns:
  - Order Number
  - Symbol
  - Buy/Sell
  - Quantity (total)
  - Price
  - Matched Quantity
  - Pending Quantity
  - Status
     ↓
User Sees:
  - List of all pending orders
  - Auto-refresh every 3 seconds
  - Can click Cancel or Modify on each
```

---

## 🔄 Order States

### Order Lifecycle

```
NEW → PENDING → PARTIAL → FILLED
            ↓
         CANCELLED
```

| State | Description | User Can |
|-------|-------------|----------|
| **NEW** | Just submitted | Wait |
| **PENDING** | In order book, waiting to match | Cancel, Modify |
| **PARTIAL** | Partially matched | Cancel (remaining qty), Modify (remaining qty) |
| **FILLED** | Fully matched | Nothing (complete) |
| **CANCELLED** | User cancelled | Nothing (terminal) |
| **REJECTED** | Exchange rejected | Check error, resubmit |

---

## ⏱️ Order Sessions & Timing

### Trading Sessions (VN30F - HOSE)

```
09:00 - 09:15   ATO (At-The-Opening)
                ↓
09:15 - 11:30   Continuous Trading (Morning)
                ↓
11:30 - 13:00   BREAK (Intermission)
                ↓
13:00 - 14:30   Continuous Trading (Afternoon)
                ↓
14:30 - 14:45   ATC (At-The-Close)
                ↓
14:45+          Market Closed
```

### Order Type Restrictions

| Order Type | Allowed Sessions |
|------------|------------------|
| **LO** | All sessions |
| **ATO** | Opening only (09:00-09:15) |
| **ATC** | Closing only (14:30-14:45) |
| **MOK/MAK/MTL** | Continuous only (09:15-11:30, 13:00-14:30) |

---

## 📡 Real-Time Updates

### How Users Get Order Updates

```
Exchange → Lotte → Kafka → WebSocket → Client App
   ↓
Order Match
   ↓
User sees notification:
  "Order #123456789 matched 5 contracts at 1,250"
```

### Update Frequency

- **Order Status** - Real-time (< 1 second)
- **Match Notifications** - Immediate
- **Pending Orders List** - Refresh every 3 seconds
- **Account Balance** - After each match

---

## 🎯 Validation Points

### Pre-Submit Validation (Client Side)

```
Before sending to server:
  ✓ All required fields filled
  ✓ Price format correct (numeric, tick size)
  ✓ Quantity > 0
  ✓ Order type selected
```

### Server Validation (Backend)

```
Before sending to exchange:
  ✓ User authenticated
  ✓ Account active
  ✓ Sufficient margin
  ✓ Symbol valid and active
  ✓ Price within daily limit
  ✓ Quantity within limits
  ✓ Session allows this order type
```

### Exchange Validation (Lotte)

```
Exchange checks:
  ✓ Account registered
  ✓ No regulatory blocks
  ✓ Order parameters valid
  ✓ Risk limits OK
```

---

## ⚠️ Error Handling

### Common Errors (Business Perspective)

| Error | Cause | User Action |
|-------|-------|-------------|
| **Insufficient Margin** | Not enough balance | Deposit or reduce quantity |
| **Invalid Price** | Outside price limit or wrong tick | Adjust price |
| **Market Closed** | Outside trading hours | Wait for market open |
| **Symbol Suspended** | Contract halted | Wait or choose different symbol |
| **Order Not Found** | Trying to cancel/modify old order | Refresh order list |
| **Already Matched** | Order executed before cancel/modify | None - order complete |

### Error Response Flow

```
Error Occurs
   ↓
System Identifies Error Type
   ↓
Returns Error Message (Vietnamese + English)
   ↓
App Displays to User
   ↓
User Takes Action (if needed)
```

---

## 🔒 Security & Authorization

### Authentication Flow

```
User Login → Verify Credentials → Generate Session Token
                                          ↓
                          User makes order request with token
                                          ↓
                          API Gateway validates token
                                          ↓
                          Process order if valid
```

### Order Authorization

- User can only see their own orders
- User can only cancel/modify their own orders
- Sub-account restrictions apply
- Trading permissions checked

---

## 📊 Data Storage (Business View)

### What Gets Stored

| Data | Purpose | Duration |
|------|---------|----------|
| **Order Details** | Record of all orders | Permanent |
| **Match History** | Execution records | Permanent |
| **Order Status** | Current order state | Until order complete |
| **Pending Orders** | Active orders cache | Real-time |

### Data Retention

- Active orders → Real-time cache (Redis)
- Historical orders → Database (permanent)
- Order reports → 7 years (regulatory requirement)

---

## 🎯 Performance Expectations

### Response Times (Target)

| Operation | Target Time | Notes |
|-----------|-------------|-------|
| Submit Order | < 2 seconds | 95th percentile |
| Cancel Order | < 1 second | 95th percentile |
| Modify Order | < 2 seconds | 95th percentile |
| Query Unmatch | < 1 second | First load |
| Match Notification | < 1 second | Real-time |

### Capacity

- **Concurrent Orders** - 1,000+ orders/second
- **Peak Trading** - Handle market open/close surges
- **User Capacity** - 10,000+ active traders

---

## 🔄 Integration Points

### What Order System Needs

1. **From Market Data**
   - Current price (for validation)
   - SymbolInfo (contract details)
   - Market status (open/closed)

2. **From Account Service**
   - Margin balance
   - Position details
   - Risk limits

3. **From Notification Service**
   - Send order confirmations
   - Push match notifications
   - Alert user of errors

4. **To Position Service**
   - Update positions after match
   - Calculate P&L
   - Update margin used

---

## 🚀 Key Takeaways

### For PM

- Order flow is: Submit → Validate → Send to Exchange → Match → Notify
- Modify = Cancel + New Order (not an edit)
- Real-time updates are critical for user experience
- Validation happens at 3 levels (Client, Server, Exchange)

### For Business

- Fast response times = Better user experience
- Clear error messages = Less support tickets
- Real-time notifications = Trader confidence
- Order lifecycle tracking = Transparency

---

**Document Status:** ✅ Complete  
**Audience:** PM, BA, Business Stakeholders  
**Next Review:** After system goes live
