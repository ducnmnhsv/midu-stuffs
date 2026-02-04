# Regular Orders - Business Requirements

**Document Type:** Business Requirements (PM-Friendly, NO CODE)  
**Category:** Orders - Regular Orders  
**Version:** 1.1  
**Date:** February 4, 2026

> **📘 API Standards:** This follows TradeX API conventions  
> Technical details: `@TradeX Knowledge/API Standards/tradex-api-conventions.md`  
> API Specs: `../Specifications/Regular_Orders_API_Spec.md`

---

## 📋 Overview

### Business Goal

Enable traders to place, modify, and cancel regular derivatives orders through NHSV Pro App, providing a seamless trading experience comparable to competitors.

### Scope

**In Scope:**
- ✅ Buy orders (Long positions)
- ✅ Sell orders (Short positions)
- ✅ Cancel pending orders
- ✅ Modify pending orders
- ✅ Query cancellable/modifiable orders

**Out of Scope:**
- ❌ Conditional orders (Stop, OCO, Trailing) - Separate feature
- ❌ Order history queries - Future enhancement
- ❌ Batch order operations - Future enhancement

---

## 🎯 Business Value

### For Users (Traders)

1. **Fast Order Execution**
   - Place orders in < 2 seconds
   - Real-time order confirmation
   - Quick modify/cancel operations

2. **Flexibility**
   - Multiple order types (LO, ATO, ATC, MOK, MAK, MTL)
   - Modify price/quantity before match
   - Cancel unwanted orders instantly

3. **Control**
   - View all pending orders
   - Know which orders can be modified/cancelled
   - Clear order status updates

### For Business

1. **Competitive Advantage**
   - Match competitor features
   - Derivatives trading capability
   - User retention

2. **Trading Volume**
   - Enable derivatives trading
   - Increase active traders
   - Revenue from trading fees

3. **Platform Completeness**
   - Complete trading ecosystem (Equity + Derivatives)
   - Professional trading platform
   - Attract institutional traders

---

## 👥 User Stories

### US-1: Place Buy Order

**As a** trader  
**I want to** place a buy order for VN30F  
**So that** I can open a long position

**Acceptance Criteria:**
- Can select futures contract (e.g., VN30F2501)
- Can choose order type (LO, ATO, ATC, MOK, MAK, MTL)
- Can specify quantity and price
- Receive immediate confirmation with order number
- Order appears in pending orders list

**Success Metric:**
- Order placement time < 2 seconds
- 99.9% successful order submission

---

### US-2: Place Sell Order

**As a** trader  
**I want to** place a sell order for VN30F  
**So that** I can open a short position

**Acceptance Criteria:**
- Same as US-1, but for SELL direction
- System validates sufficient margin
- Order number returned immediately

---

### US-3: Cancel Pending Order

**As a** trader  
**I want to** cancel my pending order  
**So that** I can avoid unwanted execution

**Acceptance Criteria:**
- Can see list of cancellable orders
- Can cancel by clicking "Cancel" button
- Receive immediate confirmation
- Order removed from pending list
- Cannot cancel already-matched orders

**Success Metric:**
- Cancel operation < 1 second
- Clear error message if cancel fails

---

### US-4: Modify Pending Order

**As a** trader  
**I want to** modify my pending order's price/quantity  
**So that** I can adjust to market conditions

**Acceptance Criteria:**
- Can see list of modifiable orders
- Can change price and/or quantity
- System validates new price/quantity
- New order number issued after modify
- Old order automatically cancelled

**Business Rule:**
- Can only modify unmatched quantity
- New price must follow tick size rules
- Modify = Cancel + New Order (atomic)

---

### US-5: View Cancellable/Modifiable Orders

**As a** trader  
**I want to** see which orders I can cancel or modify  
**So that** I can manage my pending orders

**Acceptance Criteria:**
- List shows today's unmatch orders only
- Display: order number, symbol, side (buy/sell), quantity, price, matched qty, pending qty
- Auto-refresh every 3 seconds
- Clear indication of order status

---

## 🎯 Success Criteria

### Performance

| Metric | Target | Measurement |
|--------|--------|-------------|
| Order placement | < 2s | 95th percentile |
| Cancel operation | < 1s | 95th percentile |
| Modify operation | < 2s | 95th percentile |
| Query unmatch | < 1s | 95th percentile |
| System uptime | 99.9% | Monthly |

### Accuracy

| Metric | Target |
|--------|--------|
| Order submission success | > 99% |
| Cancel success (if valid) | > 99% |
| Modify success (if valid) | > 99% |
| No duplicate orders | 100% |
| Correct order status | 100% |

### User Experience

- [ ] Intuitive order form
- [ ] Clear error messages
- [ ] Real-time order status
- [ ] Fast response time
- [ ] No data loss on network interruption

---

## 📊 Order Types - Business Perspective

### LO (Limit Order) - Most Common

**Use Case:** Trader wants specific price  
**Example:** Buy VN30F2501 at 1,250 points  
**Behavior:** Only executes at 1,250 or better (lower for buy)

### ATO (At-The-Opening)

**Use Case:** Trader wants opening price  
**Example:** Buy VN30F2501 at opening  
**Behavior:** Executes during opening auction (09:00-09:15)

### ATC (At-The-Close)

**Use Case:** Trader wants closing price  
**Example:** Sell VN30F2501 at close  
**Behavior:** Executes during closing auction (14:30-14:45)

### MOK (Market Or Kill)

**Use Case:** Execute all or nothing  
**Example:** Buy 50 contracts - all or none  
**Behavior:** Execute completely immediately or cancel

### MAK (Market At Kill)

**Use Case:** Execute as much as possible  
**Example:** Buy 50 contracts - get what's available  
**Behavior:** Execute available quantity, cancel rest

### MTL (Market To Limit)

**Use Case:** Market order with price protection  
**Example:** Buy at market, but becomes LO if not filled  
**Behavior:** Try market first, convert to LO at best price

---

## ⚠️ Business Rules

### Order Validation

1. **Account Validation**
   - Must have active derivatives account
   - Sufficient margin available
   - No account restrictions

2. **Symbol Validation**
   - Must be active futures contract
   - Not expired
   - Not suspended

3. **Price Validation**
   - Must follow tick size (0.1 point for VN30F)
   - Within daily price limit (±7% for VN30F)
   - Cannot be zero (except for market orders)

4. **Quantity Validation**
   - Minimum: 1 contract
   - Maximum: Per account/exchange limits
   - Must be integer

5. **Session Validation**
   - ATO orders: Only during opening session
   - ATC orders: Only during closing session
   - LO/MOK/MAK/MTL: Continuous trading session

### Cancel Rules

- Can only cancel unmatched orders
- Cannot cancel already-matched orders
- Cannot cancel orders from previous days
- Cancel during trading hours only

### Modify Rules

- Can only modify unmatched quantity
- Modify = Cancel old order + Place new order
- New order gets new order number
- Cannot modify to invalid price/quantity
- Matched quantity cannot be modified

---

## 🚫 Error Scenarios (Business Perspective)

### Insufficient Margin

**Scenario:** Trader tries to buy but not enough margin  
**Business Rule:** Block order, display clear message  
**User Action:** Deposit more funds or reduce quantity

### Invalid Price

**Scenario:** Price outside daily limit or wrong tick size  
**Business Rule:** Block order, show valid price range  
**User Action:** Adjust price to valid range

### Market Closed

**Scenario:** Try to place order outside trading hours  
**Business Rule:** Block order, show next trading session time  
**User Action:** Wait for market open

### Order Already Matched

**Scenario:** Try to cancel/modify already-matched order  
**Business Rule:** Block operation, show current status  
**User Action:** None - order executed

### Duplicate Order Prevention

**Scenario:** User clicks "Buy" multiple times quickly  
**Business Rule:** Process once, ignore duplicates  
**User Action:** System handles automatically

---

## 📈 Business Impact

### Trading Volume (Expected)

- **Target:** 20% of equity trading volume
- **Timeline:** 6 months after launch
- **Key Driver:** VN30F is most liquid derivative

### User Adoption

- **Phase 1:** Professional traders (Month 1-3)
- **Phase 2:** Active retail traders (Month 4-6)
- **Phase 3:** General users (Month 7+)

### Revenue Impact

- Trading fee per contract
- Increased platform usage
- Competitive positioning

---

## 🔄 Integration Requirements

### With Other Features

1. **Account Management**
   - Margin calculation
   - Balance updates
   - Position tracking

2. **Market Data**
   - Real-time quotes for price validation
   - SymbolInfo for contract details
   - Market status for session validation

3. **Risk Management**
   - Pre-trade risk check
   - Position limits
   - Margin requirements

4. **Notifications**
   - Order confirmation
   - Match notification
   - Cancel/Modify confirmation

---

## 🎯 Key Metrics to Track

### Business Metrics

- Daily active traders
- Order volume (contracts/day)
- Average order size
- Order type distribution (LO vs Market)
- Cancel/Modify rate

### Quality Metrics

- Order success rate
- Average order response time
- Error rate by type
- User complaints

### Technical Metrics

- API response time
- System availability
- Peak concurrent orders
- Backend latency

---

## 🚀 Future Enhancements

### Phase 2 (After Launch)

- Order history and reports
- Bulk order operations
- Order templates
- One-click trading

### Phase 3

- Conditional orders (Stop, OCO, Trailing)
- Advanced order types
- Algorithmic trading support
- API for third-party integration

---

**Document Status:** ✅ Complete  
**Approved By:** PM Team  
**Next Review:** After regular orders go live
