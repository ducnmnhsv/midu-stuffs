# Real-time Order Availability via WebSocket

**Document Type:** Technical Analysis  
**Category:** Order Availability - Performance Optimization  
**Version:** 1.0  
**Date:** February 10, 2026

> **Related Documents:**
> - [Order_Availability_Check_API_Spec.md](./Order_Availability_Check_API_Spec.md) - REST API spec
> - [Order_Availability_Response_Design.md](./Order_Availability_Response_Design.md) - API design rationale
> - [Market Data Channels](../../../../TradeX%20Knowledge/System/market-data-channels.md) - WebSocket infrastructure

---

## Question

**User Request:** "Có cách nào để TradeX trả về được sức mua này real-time qua WebSocket mà không ảnh hưởng nhiều tới performance không?"

---

## 1. Context

### Current Implementation (REST API Only)

**Endpoint:** `GET /api/v1/derivatives/order/checkAvailability`

**Flow:**
```
User opens order form
  ↓
FE calls REST API
  ↓
BE calls Lotte DRORD-028
  ↓
Returns availability (200-500ms)
  ↓
Display max quantity
```

**Limitations:**
- ❌ Stale data (availability changes when balance/position updates)
- ❌ Need to manually refresh
- ❌ Additional API call on each refresh

### Real-time Requirements

**When does availability change?**

| Event | Impact on Availability | Frequency |
|-------|------------------------|-----------|
| Order executed | ✅ Balance changes → Availability changes | High (every trade) |
| Order placed | ✅ Balance locked → Availability decreases | High |
| Order cancelled | ✅ Balance unlocked → Availability increases | Medium |
| Deposit/Withdrawal | ✅ Balance changes → Availability changes | Low |
| Position closed | ✅ Margin freed → Availability increases | Medium |
| Market price moves | ❌ No direct impact (margin rate fixed) | Very High |

**Key Insight:** Availability is **balance-driven**, not **market-driven**.

---

## 2. Technical Options Analysis

### Option 1: ❌ Dedicated WebSocket Channel (NOT RECOMMENDED)

**Channel:** `account.availability.{accountNumber}.{symbol}`

**Implementation:**
```
Client subscribes:
  ws.subscribe('account.availability.0001234567.VN30F2402')

Backend:
  - Listen to balance/position change events
  - Call Lotte DRORD-028 on each change
  - Push update to subscribed clients
```

**Pros:**
- ✅ True real-time updates
- ✅ No manual refresh needed
- ✅ Client always has latest data

**Cons:**
- ❌ **High Lotte API load** - Call DRORD-028 on every balance change
- ❌ **Complex event system** - Need to track balance/position changes
- ❌ **Scalability issues** - N users × M symbols = N×M channels
- ❌ **Lotte rate limiting** - May hit API limits
- ❌ **Over-engineering** - Availability doesn't change THAT frequently

**Performance Impact:** ⚠️ **HIGH** - Significant load on Lotte API

**Verdict:** ❌ **Rejected** - Cons outweigh benefits

---

### Option 2: ❌ Piggyback on Account Balance Channel (NOT OPTIMAL)

**Channel:** `account.balance.{accountNumber}` (hypothetical)

**Implementation:**
```
Client subscribes:
  ws.subscribe('account.balance.0001234567')

Backend pushes:
  {
    "cashBalance": 5000000000,
    "marginAvailable": 3000000000,
    "positionValue": 2000000000,
    "VN30F2402": {
      "buy": { "availableQuantity": 100, "availableLiquidity": 150 },
      "sell": { "availableQuantity": 50, "availableLiquidity": 200 }
    }
  }
```

**Pros:**
- ✅ Leverage existing account data stream
- ✅ No additional channel overhead

**Cons:**
- ❌ **Still calls Lotte API per symbol** - Need DRORD-028 for each symbol
- ❌ **Large payload** - If user tracks 10 symbols = 10× Lotte calls
- ❌ **Unnecessary data** - Most symbols not actively trading
- ❌ **Complex aggregation** - Merge balance + availability from multiple sources

**Performance Impact:** ⚠️ **MEDIUM-HIGH** - Multiple Lotte calls per balance update

**Verdict:** ❌ **Not Recommended** - Complexity vs benefit ratio poor

---

### Option 3: ✅ Event-Based Trigger + Client-Side Refresh (RECOMMENDED)

**Channel:** `account.balance.{accountNumber}` (lightweight events)

**Implementation:**
```typescript
// Backend: Push lightweight event when balance changes
{
  "event": "balance_changed",
  "accountNumber": "0001234567",
  "timestamp": 1707566400000,
  "trigger": "ORDER_EXECUTED" | "DEPOSIT" | "WITHDRAWAL" | "POSITION_CLOSED"
}

// Frontend: React to event
ws.on('account.balance.*', async (event) => {
  if (orderFormIsOpen && event.trigger === 'ORDER_EXECUTED') {
    // Refresh availability via REST API
    const availability = await checkAvailability(account, symbol, sellBuyType);
    updateUI(availability);
  }
});
```

**Pros:**
- ✅ **Minimal backend load** - Just push event, no Lotte call
- ✅ **On-demand API calls** - Only when form is open
- ✅ **Scalable** - Single event channel per account
- ✅ **Flexible** - Client decides when to refresh
- ✅ **Simple to implement** - Reuse existing REST API

**Cons:**
- ❌ Small delay (event → API call → update) ~500ms
- ❌ Not "true" real-time (but good enough)

**Performance Impact:** ✅ **LOW** - Event stream is lightweight, API calls only when needed

**Verdict:** ✅ **RECOMMENDED** - Best balance of real-time experience vs performance

---

### Option 4: ✅ Smart Polling with Debounce (SIMPLE ALTERNATIVE)

**No WebSocket needed** - Pure client-side optimization

**Implementation:**
```typescript
// Frontend: Smart polling strategy
const AvailabilityMonitor = () => {
  const [availability, setAvailability] = useState(null);
  
  useEffect(() => {
    let pollInterval;
    
    if (orderFormIsOpen) {
      // Initial fetch
      fetchAvailability();
      
      // Poll every 10 seconds (only when form is visible)
      pollInterval = setInterval(() => {
        fetchAvailability();
      }, 10000); // 10s interval
    }
    
    return () => clearInterval(pollInterval);
  }, [orderFormIsOpen, symbol, account]);
  
  // Also refresh on user actions
  const handleOrderPlaced = () => {
    fetchAvailability(); // Immediate refresh after order
  };
};
```

**Pros:**
- ✅ **Zero backend changes** - Pure FE implementation
- ✅ **Simple** - No WebSocket infrastructure needed
- ✅ **Predictable load** - Fixed polling interval
- ✅ **Works today** - No new features required

**Cons:**
- ❌ Fixed delay (max 10s stale data)
- ❌ Unnecessary polls (if no balance change)
- ❌ Battery drain (mobile)

**Performance Impact:** ✅ **LOW** - Predictable, controlled load

**Verdict:** ✅ **Good Fallback** - If WebSocket infrastructure not ready

---

## 3. Recommended Implementation: Event-Based Trigger

### Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         EVENT FLOW                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [Order Executed] ──► tuxedo ──► Event Publisher ──► Kafka Topic       │
│                                          │              account.events  │
│  [Deposit]        ──► lotte-bridge ──────┘                             │
│                                                                         │
│  [Position Closed] ──► order-v2 ─────────┘                             │
│                                                                         │
│                                                                         │
│  Kafka Topic ──► ws-v2 ──► WebSocket ──► Client                        │
│  account.events                                                         │
│                                                                         │
│                                                                         │
│  Client receives event ──► Calls REST API ──► GET /checkAvailability   │
│  (if order form is open)                                                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Event Schema

```typescript
interface AccountBalanceChangedEvent {
  event: 'balance_changed';
  accountNumber: string;
  timestamp: number;
  trigger: 
    | 'ORDER_EXECUTED'
    | 'ORDER_PLACED'
    | 'ORDER_CANCELLED'
    | 'DEPOSIT'
    | 'WITHDRAWAL'
    | 'POSITION_CLOSED'
    | 'MARGIN_CALL';
  delta?: {
    cashChange?: number;      // Optional: Show how much changed
    marginChange?: number;
  };
}
```

### Client Implementation

```typescript
// 1. Subscribe to account events
ws.subscribe(`account.balance.${accountNumber}`);

// 2. Listen to events
ws.on('account.balance.*', async (event: AccountBalanceChangedEvent) => {
  // Only refresh if order form is visible
  if (!isOrderFormVisible()) return;
  
  // Debounce: If multiple events in 2s, only call once
  debouncedRefreshAvailability();
});

// 3. Debounced refresh (avoid spam)
const debouncedRefreshAvailability = debounce(async () => {
  const availability = await api.checkAvailability({
    accountNumber,
    symbol: currentSymbol,
    sellBuyType: currentTab // 'BUY' or 'SELL' or undefined (both)
  });
  
  // Update UI
  setAvailability(availability);
  
  // Optional: Show notification
  toast.info('Sức mua đã được cập nhật');
}, 2000); // Wait 2s for multiple events
```

### Backend Changes Required

#### 1. Event Publisher (New Service or Module)

**Location:** `tuxedo` or new `event-publisher` service

**Responsibilities:**
- Listen to order execution events
- Listen to balance change events
- Publish to Kafka topic `account.events`

**Pseudo-code:**
```java
@Service
public class AccountEventPublisher {
  
  @Autowired
  private KafkaTemplate<String, AccountEvent> kafkaTemplate;
  
  @EventListener
  public void onOrderExecuted(OrderExecutedEvent event) {
    AccountBalanceChangedEvent balanceEvent = new AccountBalanceChangedEvent(
      "balance_changed",
      event.getAccountNumber(),
      System.currentTimeMillis(),
      "ORDER_EXECUTED"
    );
    
    kafkaTemplate.send("account.events", balanceEvent);
  }
  
  // Similar handlers for:
  // - onOrderPlaced
  // - onOrderCancelled
  // - onDeposit
  // - onWithdrawal
  // - onPositionClosed
}
```

#### 2. WebSocket Publisher (ws-v2)

**Changes to `ws-v2-main`:**

```javascript
// consumer.js - Add new topic consumer
kafka.consumer.on('message', (message) => {
  if (message.topic === 'account.events') {
    const event = JSON.parse(message.value);
    
    // Publish to WebSocket
    io.to(`account:${event.accountNumber}`).emit('balance_changed', {
      event: event.event,
      trigger: event.trigger,
      timestamp: event.timestamp,
      delta: event.delta
    });
  }
});

// socket.js - Add subscription handler
socket.on('subscribe:account', (accountNumber) => {
  socket.join(`account:${accountNumber}`);
});
```

---

## 4. Performance Analysis

### Comparison: REST Only vs Event-Based

| Metric | REST Polling (10s) | Event-Based Trigger |
|--------|-------------------|---------------------|
| **Lotte API Calls** | ~6 calls/min/user | ~1-3 calls/min/user (when balance changes) |
| **Network Traffic** | Constant (polling) | Minimal (events only) |
| **Staleness** | Up to 10s | ~500ms (event + API call) |
| **Backend Load** | Predictable | Event-driven (lower overall) |
| **User Experience** | Delayed updates | Near real-time |

### Load Estimation

**Assumptions:**
- 1,000 concurrent users trading derivatives
- Average 5 balance-changing events per user per minute (high activity)

**Event-Based Load:**
```
Events Published: 1,000 users × 5 events/min = 5,000 events/min
WebSocket Messages: 5,000 messages/min = ~83 msg/s

Lotte API Calls (optimistic): 
  - 50% of users have order form open = 500 active forms
  - Each event triggers 1 API call (debounced)
  - 500 users × 5 events/min = 2,500 calls/min = ~42 calls/s

Lotte API Calls (pessimistic - no debounce):
  - 5,000 calls/min = ~83 calls/s
```

**REST Polling Load (10s interval):**
```
API Calls: 1,000 users × 6 calls/min = 6,000 calls/min = 100 calls/s
(Constant load, even when no changes)
```

**Verdict:** ✅ Event-based reduces Lotte API load by **~40-58%** compared to polling

---

## 5. Implementation Phases

### Phase 1: Backend Event Infrastructure (2-3 weeks)

**Tasks:**
- [ ] Define `AccountBalanceChangedEvent` schema
- [ ] Create Kafka topic `account.events`
- [ ] Implement event publishers in:
  - [ ] `tuxedo` (order events)
  - [ ] `lotte-bridge` (deposit/withdrawal)
  - [ ] `order-v2` (position closed - future)
- [ ] Add consumer in `ws-v2` to relay events

**Deliverables:**
- WebSocket channel `account.balance.{accountNumber}` publishing events

### Phase 2: Frontend Integration (1 week)

**Tasks:**
- [ ] Subscribe to `account.balance.*` channel
- [ ] Implement debounced refresh logic
- [ ] Show "Sức mua đã cập nhật" notification (optional)
- [ ] Handle reconnection (re-subscribe)

**Deliverables:**
- Order form auto-refreshes availability on balance changes

### Phase 3: Optimization (1 week)

**Tasks:**
- [ ] Add client-side caching (prevent duplicate API calls)
- [ ] Implement exponential backoff (if API fails)
- [ ] Add telemetry (track refresh frequency, latency)
- [ ] Performance monitoring (Lotte API call rate)

**Deliverables:**
- Production-ready, optimized implementation

---

## 6. Alternative: Quick Win with Smart Polling

**If WebSocket infrastructure not ready immediately:**

### Interim Solution (Can Ship Today)

```typescript
// Frontend only - no backend changes
const useSmartAvailability = (accountNumber, symbol, sellBuyType) => {
  const [availability, setAvailability] = useState(null);
  const [isFormVisible, setIsFormVisible] = useState(false);
  
  // Fetch immediately when form opens
  useEffect(() => {
    if (isFormVisible) {
      fetchAvailability();
    }
  }, [isFormVisible, accountNumber, symbol, sellBuyType]);
  
  // Poll while form is visible (10s interval)
  useEffect(() => {
    if (!isFormVisible) return;
    
    const interval = setInterval(() => {
      fetchAvailability();
    }, 10000); // 10s
    
    return () => clearInterval(interval);
  }, [isFormVisible]);
  
  // Refresh immediately after user places order
  const refreshAfterAction = useCallback(() => {
    fetchAvailability();
  }, [accountNumber, symbol, sellBuyType]);
  
  return { availability, refreshAfterAction };
};
```

**Benefits:**
- ✅ Ships immediately (no backend work)
- ✅ Better than manual refresh
- ✅ Predictable load

**Limitations:**
- ❌ Max 10s stale data
- ❌ Wastes API calls when no changes

---

## 7. Recommendations

### Recommended Approach: Phased Rollout

**Phase 1 (Week 1-2): Quick Win**
- ✅ Ship smart polling (FE only)
- ✅ Improves UX immediately
- ✅ No backend changes needed

**Phase 2 (Week 3-5): Event Infrastructure**
- ✅ Build event publisher + WebSocket relay
- ✅ Better performance than polling
- ✅ Scales well

**Phase 3 (Week 6): Optimization**
- ✅ Fine-tune debouncing
- ✅ Monitor performance
- ✅ A/B test if needed

### Why NOT Full Real-time (Dedicated Channel)

**Key Insight:** Availability doesn't need **instant** updates like market prices.

| Data Type | Update Frequency | Real-time Need | Approach |
|-----------|------------------|----------------|----------|
| Market Price | ~100 updates/s | ⚠️ Critical | Dedicated WS channel |
| Order Book | ~50 updates/s | ⚠️ Critical | Dedicated WS channel |
| Account Balance | ~5 updates/min | ✅ Important | Event-based trigger |
| Availability | ~5 updates/min | ✅ Important | Event-based trigger |
| Position P&L | ~1 update/min | ⚙️ Nice-to-have | Polling OK |

**Availability is balance-driven, not market-driven** → Event-based trigger is optimal.

---

## 8. Performance Impact Summary

| Approach | Backend Load | Lotte API Load | UX | Complexity | Recommendation |
|----------|-------------|----------------|-----|------------|----------------|
| **Dedicated WS Channel** | ⚠️ High | ⚠️ Very High | ⭐⭐⭐⭐⭐ | ⚠️ High | ❌ Overkill |
| **Piggyback Balance Channel** | ⚠️ Medium-High | ⚠️ High | ⭐⭐⭐⭐ | ⚠️ Medium | ❌ Not worth it |
| **Event-Based Trigger** | ✅ Low | ✅ Low-Medium | ⭐⭐⭐⭐ | ✅ Low | ✅ **BEST** |
| **Smart Polling** | ✅ Low | ⚠️ Medium | ⭐⭐⭐ | ✅ Very Low | ✅ **Quick Win** |
| **Manual Refresh** | ✅ Very Low | ✅ Very Low | ⭐⭐ | ✅ Zero | ❌ Poor UX |

---

## 9. Conclusion

### Answer to Original Question

> "Có cách nào để TradeX trả về được sức mua này real-time qua WebSocket mà không ảnh hưởng nhiều tới performance không?"

✅ **CÓ** - Sử dụng **Event-Based Trigger** approach:

**How it works:**
1. Backend pushes lightweight event when balance changes
2. Client receives event (if subscribed)
3. Client calls REST API to fetch latest availability
4. Update UI

**Performance Impact:**
- ✅ **LOW** - Reduces Lotte API calls by 40-58% vs polling
- ✅ **Scalable** - O(N users) not O(N users × M symbols)
- ✅ **Near real-time** - ~500ms delay (acceptable for availability data)

**Not Recommended:**
- ❌ Dedicated WS channel per symbol - Too expensive
- ❌ Full real-time push - Overkill for availability data

**Best Strategy:**
1. **Ship smart polling NOW** (FE only, 1 day)
2. **Build event infrastructure** (2-3 weeks)
3. **Replace polling with events** (1 week)

**Total Timeline:** ~4-5 weeks for production-ready event-based solution.

---

**Document Status:** ✅ Complete  
**For:** Technical Lead, Backend Team, Frontend Team  
**Next Steps:** Review with team → Decide on implementation timeline  
**Estimated Effort:** 
- Smart Polling (Quick Win): 1 day (FE only)
- Event-Based (Full Solution): 4-5 weeks (BE 3 weeks + FE 1 week + QA 1 week)
