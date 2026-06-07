# Analytics Tracking System Design
**NHSV Pro — GA4 Analytics**

- **Date:** 2026-06-07
- **Author:** Duc Nguyen (BA/PM)
- **Status:** Draft — Pending Implementation
- **GA4 Property:** NHSV Pro (`properties/478227972`)

---

## 1. Objectives

Build a comprehensive analytics tracking system for NHSV Pro to support data-driven product decisions.

**Goals:**
1. **Retention & Engagement** — Understand how users return to the app and which features they use (app-level + feature-level)
2. **eKYC Funnel Optimization** — Full visibility into drop-off, errors, and time-to-complete
3. **Trading Activation** — Track the journey from registration to first trade

**Primary consumers:**
- **PM/BA team** — GA4 Explore dashboards (feature adoption, funnel analysis)
- **Management** — KPI scorecards (DAU/MAU, retention rates, conversion rates)

---

## 2. Architecture

### Approach: Centralized Analytics Service

All analytics code routes through a single service layer. No screen or saga calls Firebase directly.

```
Screen / Redux Saga
      ↓
analyticsService.trackXxx()     ← Single public API
      ↓
events.ts (type-safe names)
      ↓
Firebase GA4 Analytics
      ↓ (future)
AppsFlyer (enable by adding 1 line to service)
```

**File structure in nhsv-mts-rn:**
```
src/utils/analytics/
├── analyticsService.ts     ← Public API — all tracking calls go here
├── events.ts               ← Event name constants + TypeScript param types
├── userProperties.ts       ← User property setters
└── ekycTracking.ts         ← eKYC-specific helpers (timestamp, step tracking)
```

**Principles:**
- `analyticsService.ts` is the ONLY file that imports `@react-native-firebase/analytics`
- All existing direct `analytics().logEvent()` calls in screens/sagas are refactored to use the service
- AppsFlyer (`utils/appFlyers.ts`) can be enabled later by adding calls inside the service — zero changes to screens required

---

## 3. Event Naming Convention

**Pattern:** `{noun}_{verb}` — noun first, then action

```
✅ feature_viewed          (not view_feature)
✅ order_initiated         (not initiate_order)
✅ ekyc_step_entered       (not enter_ekyc_step)
✅ notification_opened
✅ symbol_searched
```

**Standard parameters on every event:**
```typescript
{
  screen_name: string      // current screen (reuses existing GA4 dimension)
  timestamp_ms: number     // epoch ms — used to calculate duration between events
}
```

---

## 4. Event Catalog

### 4.1 — Feature Usage (P1)

Track which features users actually engage with, not just view.

| Event | Key Parameters | Trigger |
|-------|---------------|---------|
| `feature_viewed` | `feature_name`, `screen_name` | User lands on a main tab/screen |
| `feature_action` | `feature_name`, `action_type`, `screen_name` | User performs a meaningful interaction (filter, tap stock, scroll depth) |
| `tab_switched` | `from_tab`, `to_tab` | User switches bottom navigation tab |
| `symbol_searched` | `query`, `result_count` | User types in search box |
| `symbol_selected` | `symbol`, `source` | User taps a stock symbol |
| `stock_detail_viewed` | `symbol`, `source`, `screen_name` | User opens stock detail screen |

**`feature_name` enum values:**
```
market_equity | market_derivatives | market_watchlist | market_topstocks |
market_cw | market_etf | market_index | order_trade | order_onetouch |
portfolio | asset | orderbook | news | education | broker_chat |
cash_flow | rights | margin
```

**`source` enum for symbol_selected:**
```
search | watchlist | market_table | top_stocks | recently_viewed | notification
```

---

### 4.2 — Trading Funnel (P0)

**Critical gap** — zero trading events currently tracked. These are the core business metrics.

| Event | Key Parameters | Trigger |
|-------|---------------|---------|
| `order_initiated` | `order_type`, `side`, `symbol`, `market`, `screen_name` | User opens order form / starts filling |
| `order_confirmed` | `order_type`, `side`, `symbol`, `market`, `is_first_order` | User taps confirm — order sent to API |
| `order_cancelled` | `symbol`, `source` (user_action/system), `screen_name` | Order cancelled |
| `portfolio_viewed` | `account_type`, `view_mode`, `screen_name` | User opens portfolio tab |
| `cash_flow_viewed` | `screen_name` | User opens cash flow / transaction history |

**`order_type` enum:** `LO | MP | ATO | ATC | MAK | MOK | PLO | TP | SL | OCO | STOP`
**`market` enum:** `equity | derivatives`
**`side` enum:** `buy | sell`

**`is_first_order`** (boolean): Set `true` on the first `order_confirmed` ever for this user. Used as the **Trading Activation** milestone — the most important KPI after eKYC completion.

> **Implementation note:** Detect via AsyncStorage flag `"analytics_has_placed_order"`. On first `order_confirmed`, if flag is absent → set `is_first_order: true` and write flag. Subsequent orders → `is_first_order: false`.

---

### 4.3 — eKYC Full Funnel (P0)

**Replace and extend** existing eKYC events. All old events are deprecated in favor of these.

| New Event | Replaces | Key Parameters |
|-----------|---------|---------------|
| `ekyc_started` | `ekyc_start` | `entry_point`, `timestamp_ms` |
| `ekyc_step_entered` | `ekyc_screen_view` | `step_name`, `step_index`, `timestamp_ms` |
| `ekyc_step_completed` | *(new)* | `step_name`, `step_index`, `duration_ms` |
| `ekyc_drop_off` | `ekyc_drop_off` (**broken**) | `step_name`, `step_index`, `time_spent_ms` |
| `ekyc_scan_attempted` | *(new)* | `doc_type`, `attempt_number` |
| `ekyc_scan_result` | `ekyc_scan_id_success` + `ekyc_error` | `doc_type`, `success`, `error_code?`, `attempt_number` |
| `ekyc_completed` | `ekyc_complete` | `total_duration_ms`, `retry_count` |

**`step_name` enum (ordered):**
```
otp_verification → document_selection → scan_id_front → scan_id_back →
scan_face → personal_info → additional_services → bank_registration →
policy_confirm → sign_contract
```

**`doc_type` enum:** `id_front | id_back | face`

**Timestamp logic (implemented in `ekycTracking.ts`):**
```typescript
// On step_entered: record start time
ekycTracking.enterStep('scan_id_front')

// On step_completed: calculate duration automatically
ekycTracking.completeStep('scan_id_front')
// → fires ekyc_step_completed with duration_ms calculated internally
```

**`ekyc_drop_off` fix (Bug #1):**

Current bug: event fires in only ~3 of 10 expected drop-off scenarios. Fix requires attaching the event to `AppState` change (app backgrounded) + navigation `beforeRemove` listener for ALL eKYC screens — not just the ones currently instrumented.

---

### 4.4 — Retention Signals (P1)

| Event | Key Parameters | Trigger |
|-------|---------------|---------|
| `app_opened` | `open_source`, `notification_type?` | App comes to foreground |
| `session_ended` | `session_duration_ms`, `last_feature` | App goes to background |
| `notification_received` | `notification_type` | Push notification arrives |
| `notification_opened` | `notification_type`, `deep_link_target` | User taps notification |
| `notification_permission` | `granted` (boolean) | After permission prompt |

**`open_source` enum:** `direct | push_order_match | push_otp | push_news | push_promo | widget`
**`notification_type` enum:** `order_match | otp | news | promo | system`

---

### 4.5 — User Properties (set once, segment everything)

User properties allow filtering ANY report by user segment. Set on login and update when relevant data changes.

```typescript
// Called after login + after eKYC completion
analytics().setUserId(sha256(userId))  // hashed — never send raw ID

analytics().setUserProperties({
  user_type: 'registered_only' | 'ekyc_completed' | 'active_trader',
  account_type: 'normal' | 'margin' | 'none',
  has_derivatives_account: 'true' | 'false',
  preferred_market: 'equity' | 'derivatives' | 'both' | 'unknown',
  // days_since_first_trade: updated each session open (as string)
})
```

**Update triggers:**
- `user_type` → upgrade to `ekyc_completed` on `ekyc_complete`, upgrade to `active_trader` on first `order_confirmed`
- `preferred_market` → recalculate after `order_confirmed`: equity ≥ 70% of total → `equity`; derivatives ≥ 70% → `derivatives`; else → `both`. Minimum 3 orders before switching from `unknown`

---

## 5. GA4 Configuration

### 5.1 — Existing Dimensions (Reuse / Fix)

| Dimension | Action | Notes |
|-----------|--------|-------|
| `error_code` | ✅ Reuse | Used in `ekyc_scan_result`, `order_*` errors |
| `error_type` | ✅ Reuse + Fix UI name | UI currently shows `"error_typ"` — fix typo in GA4 Admin |
| `screen_name` | ✅ Reuse | UI currently shows `"dropoff_screen"` — rename to `"screen_name"` |
| `ekyc_status` | ✅ Reuse | Keep for `ekyc_started` |
| `broker_manager` | ➡️ Keep as-is | Not in scope |
| `referrer_type` | ➡️ Keep as-is | Not in scope |

### 5.2 — New Dimensions to Register

**Event-scoped:**
| Param name | UI name | Used in |
|------------|---------|---------|
| `feature_name` | Feature Name | `feature_viewed`, `feature_action` |
| `order_type` | Order Type | `order_initiated`, `order_confirmed` |
| `ekyc_step` | eKYC Step | `ekyc_step_entered`, `ekyc_step_completed`, `ekyc_drop_off` |
| `open_source` | App Open Source | `app_opened` |
| `notification_type` | Notification Type | `notification_*` |

**User-scoped (new — currently none exist):**
| Property name | UI name | Values |
|---------------|---------|--------|
| `user_type` | User Type | registered_only / ekyc_completed / active_trader |
| `account_type` | Account Type | none / normal / margin |
| `has_derivatives_account` | Has Derivatives | true / false |
| `preferred_market` | Preferred Market | equity / derivatives / both / unknown |

### 5.3 — Existing Metrics (Reuse)

| Metric | Type | Used for |
|--------|------|---------|
| `time_spent` | SECONDS | Per-step duration in eKYC |
| `total_time_spent` | SECONDS | Total eKYC completion time |
| `total_attempts` | INTEGER | Scan retry count |

> **Note:** New events send duration in `_ms` (milliseconds) in the event param, but GA4 stores it via the existing `time_spent` metric registered as SECONDS. The service layer handles the conversion.

---

## 6. Bugs to Fix (Immediate)

| # | Bug | Impact | Fix |
|---|-----|--------|-----|
| 1 | `ekyc_drop_off` fires on only ~22% of actual drop-offs | Cannot trust funnel data | Add `AppState` + `navigation.beforeRemove` listeners to ALL eKYC screens |
| 2 | `error_type` GA4 UI name typo (`"error_typ"`) | Reports show wrong label | Fix in GA4 Admin → Custom definitions |
| 3 | `screen_name` GA4 UI name is `"dropoff_screen"` | Misleading in reports | Rename in GA4 Admin → Custom definitions |
| 4 | No `setUserId()` call anywhere | Cannot track user journeys across sessions | Add to login success handler |

---

## 7. GA4 Reports & Dashboards

### For PM/BA — GA4 Explore

**Report 1: Feature Adoption**
- Visualization: Bar chart — `feature_viewed` count by `feature_name`, weekly
- Secondary: Funnel — `feature_viewed` → `feature_action` (shows passive vs active users)
- Segment by `user_type`: active_trader vs registered_only — see which features each segment uses

**Report 2: eKYC Funnel**
- Visualization: Funnel — 10 steps from `ekyc_started` → `ekyc_completed`
- Table: Average `time_spent` per step — flag steps >120 seconds
- Chart: `ekyc_scan_result` success rate by `doc_type` — ID vs face scan failure rates
- Trend: Drop-off rate by step week-over-week (measures UX improvement after each release)

**Report 3: Trading Activation**
- Funnel: `ekyc_completed` → `order_initiated` → `order_confirmed` (filter `is_first_order: true`)
- Breakdown by `order_type` and `market` — what do new traders use first?

### For Management — KPI Scorecard

| KPI | Source | Target |
|-----|--------|--------|
| DAU / MAU | Active Users (built-in) | Track weekly trend |
| D1 / D7 / D30 Retention | User Retention report (built-in) | Baseline after implementation |
| eKYC Conversion | `ekyc_completed` / `ekyc_started` | Current: 58% — target 70% |
| Trading Activation Rate | users(`order_confirmed`) / users(`ekyc_completed`) | Baseline TBD |
| Feature Engagement Rate | users(`feature_action`) / users(`feature_viewed`) | Baseline TBD |
| Push Notification CTR | `notification_opened` / `notification_received` | Baseline TBD |

---

## 8. Implementation Rollout

### Sprint 1 — P0 (Foundation)
- [ ] Create `src/utils/analytics/` folder structure
- [ ] Implement `analyticsService.ts` with Firebase wrapper
- [ ] Implement `events.ts` with all event name constants and param types
- [ ] Add `setUserId()` in login success handler
- [ ] Set user properties on login + eKYC completion
- [ ] Fix `ekyc_drop_off` bug (AppState + beforeRemove listeners on all eKYC screens)
- [ ] Add `ekyc_step_entered` + `ekyc_step_completed` with timestamp tracking (`ekycTracking.ts`)
- [ ] Add `order_initiated` + `order_confirmed` (with `is_first_order` detection)

### Sprint 2 — P1 (Engagement)
- [ ] Add `feature_viewed` + `feature_action` to all major screens
- [ ] Add `tab_switched` to bottom navigation
- [ ] Add `app_opened` with `open_source` detection
- [ ] Add `session_ended` with duration + `last_feature`
- [ ] Add `notification_received` + `notification_opened` to OneSignal handlers
- [ ] Add `symbol_searched` + `symbol_selected` to search flow

### Sprint 3 — P2 (GA4 & Dashboards)
- [ ] Fix GA4 custom dimension UI names (`error_typ` → `error_type`, `dropoff_screen` → `screen_name`)
- [ ] Register 5 new event-scoped dimensions in GA4 Admin
- [ ] Register 4 user-scoped properties in GA4 Admin
- [ ] Build Feature Adoption report in GA4 Explore
- [ ] Build eKYC Funnel report in GA4 Explore
- [ ] Build Trading Activation funnel in GA4 Explore
- [ ] Build Management KPI scorecard

---

## 9. Scope & Constraints

**In scope:**
- nhsv-mts-rn (NHSV Pro mobile app)
- Firebase GA4 as primary analytics platform
- eKYC + Retention + Trading funnel tracking

**Out of scope:**
- AppsFlyer activation (infrastructure ready, enable in future sprint)
- BigQuery export / Looker Studio (Phase 2 when team needs SQL-level queries)
- Web analytics
- Backend event tracking

**GA4 Standard limits (current usage vs limit):**
- Event-scoped custom dimensions: 6 used + 5 new = **11 / 50** ✅
- User-scoped custom dimensions: 0 used + 4 new = **4 / 25** ✅
- Custom metrics: 3 used + 0 new = **3 / 50** ✅

---

**Document Status:** Draft
**For:** PM/BA, Mobile Dev Team
**Next Steps:** Dev team review → Sprint 1 planning → Implementation
