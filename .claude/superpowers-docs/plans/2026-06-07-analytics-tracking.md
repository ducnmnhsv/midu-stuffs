# Analytics Tracking System — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Xây dựng hệ thống analytics tracking toàn diện cho NHSV Pro dùng Firebase GA4, bao gồm centralized service, eKYC funnel đầy đủ, trading events, và retention signals.

**Architecture:** Tất cả tracking calls đi qua `analyticsService.ts` — file duy nhất import Firebase Analytics. Các screens/sagas gọi service thay vì gọi Firebase trực tiếp. GA4 Standard (free tier) được dùng làm platform duy nhất.

**Tech Stack:** React Native, `@react-native-firebase/analytics ^18.8.0`, TypeScript, Redux Saga, `react-native-onesignal 5.2.14`

**Repo:** `nhsv-mts-rn` tại `/Users/ducnguyen/Documents/project/nhsv-mts-rn`

**Spec:** `docs/superpowers/specs/2026-06-07-analytics-tracking-design.md`

> ⚠️ **Quan trọng:** Plan này được viết trong `tradex-monitoring` nhưng toàn bộ implementation thực hiện trong `nhsv-mts-rn`. Mọi path file đều relative từ root của `nhsv-mts-rn`.

---

## File Map

### Tạo mới
```
src/utils/analytics/
├── events.ts               ← Event name constants + param types (TypeScript)
├── userProperties.ts       ← User property setters + user_type logic
├── ekycTracking.ts         ← eKYC timestamp tracking helpers
└── analyticsService.ts     ← Public API duy nhất — mọi tracking đi qua đây
```

### Chỉnh sửa
```
src/globals/index.tsx                                    ← Xóa ANALYTICS_EVENT enum cũ
src/navigation/index.tsx                                 ← Refactor logScreenView + thêm tab_switched
src/reduxs/sagas/Authentication/VerifyOTP.ts             ← Thêm setUserId + setUserProperties
src/reduxs/sagas/EKYC/VerifyOTP.ts                      ← Thay ekyc_screen_view → mới
src/reduxs/sagas/EKYC/EKYCScanIdDone.ts                 ← Thay ekyc_error/ekyc_scan_id_success
src/reduxs/sagas/EKYC/ConfirmPersonalInformationScreen.ts ← Cập nhật events
src/reduxs/sagas/EKYC/OnPressNextInRegisterMoreServiceScreen.ts ← Cập nhật events
src/reduxs/sagas/EKYC/OnPressNextInConfirmPolicyScreen.ts ← Thay ekyc_complete → ekyc_completed
src/screens/EKYCOTPScreen/index.tsx                      ← Thay ekyc_start → ekyc_started
src/screens/EKYCChooseIDTypeScreen/index.tsx             ← Fix drop_off + step tracking
src/screens/EKYCScanFaceIntroScreen/index.tsx            ← Fix drop_off + step tracking
src/screens/EKYCRegisterAnotherInformationScreen/index.tsx ← Fix drop_off + step tracking
src/screens/EKYCRegisterMoreServicesScreen/index.tsx     ← Fix drop_off + step tracking
src/screens/EKYCSignContractScreen/index.tsx             ← Fix drop_off + step tracking
src/reduxs/sagas/Trade/PlaceOrder.ts                    ← Thêm order_initiated + order_confirmed
src/screens/AssetTab/index.tsx (hoặc portfolio screen)   ← Thêm portfolio_viewed
src/screens/CashFlowScreen/index.tsx                     ← Thêm cash_flow_viewed
src/utils/OneSignal.ts                                   ← Thêm notification tracking
src/App.tsx (hoặc navigation root)                       ← Thêm app_opened + session_ended
```

---

## SPRINT 1 — P0: Foundation + eKYC fix + Trading (bắt buộc)

---

### Task 1: Tạo events.ts — Event names và TypeScript types

**Files:**
- Tạo: `src/utils/analytics/events.ts`

- [ ] **Bước 1: Tạo file events.ts với toàn bộ event constants và param types**

```typescript
// src/utils/analytics/events.ts

export const AnalyticsEvents = {
  // Feature Usage
  FEATURE_VIEWED: 'feature_viewed',
  FEATURE_ACTION: 'feature_action',
  TAB_SWITCHED: 'tab_switched',
  SYMBOL_SEARCHED: 'symbol_searched',
  SYMBOL_SELECTED: 'symbol_selected',
  STOCK_DETAIL_VIEWED: 'stock_detail_viewed',

  // Trading Funnel
  ORDER_INITIATED: 'order_initiated',
  ORDER_CONFIRMED: 'order_confirmed',
  ORDER_CANCELLED: 'order_cancelled',
  PORTFOLIO_VIEWED: 'portfolio_viewed',
  CASH_FLOW_VIEWED: 'cash_flow_viewed',

  // eKYC Full Funnel
  EKYC_STARTED: 'ekyc_started',
  EKYC_STEP_ENTERED: 'ekyc_step_entered',
  EKYC_STEP_COMPLETED: 'ekyc_step_completed',
  EKYC_DROP_OFF: 'ekyc_drop_off',
  EKYC_SCAN_ATTEMPTED: 'ekyc_scan_attempted',
  EKYC_SCAN_RESULT: 'ekyc_scan_result',
  EKYC_COMPLETED: 'ekyc_completed',

  // Retention Signals
  APP_OPENED: 'app_opened',
  SESSION_ENDED: 'session_ended',
  NOTIFICATION_RECEIVED: 'notification_received',
  NOTIFICATION_OPENED: 'notification_opened',
  NOTIFICATION_PERMISSION: 'notification_permission',
} as const;

export type AnalyticsEventName = typeof AnalyticsEvents[keyof typeof AnalyticsEvents];

// --- Enums ---

export type FeatureName =
  | 'market_equity' | 'market_derivatives' | 'market_watchlist' | 'market_topstocks'
  | 'market_cw' | 'market_etf' | 'market_index' | 'order_trade' | 'order_onetouch'
  | 'portfolio' | 'asset' | 'orderbook' | 'news' | 'education' | 'broker_chat'
  | 'cash_flow' | 'rights' | 'margin';

export type OrderType = 'LO' | 'MP' | 'ATO' | 'ATC' | 'MAK' | 'MOK' | 'PLO' | 'TP' | 'SL' | 'OCO' | 'STOP';
export type MarketType = 'equity' | 'derivatives';
export type OrderSide = 'buy' | 'sell';
export type EkycStepName =
  | 'otp_verification' | 'document_selection' | 'scan_id_front' | 'scan_id_back'
  | 'scan_face' | 'personal_info' | 'additional_services' | 'bank_registration'
  | 'policy_confirm' | 'sign_contract';
export type DocType = 'id_front' | 'id_back' | 'face';
export type OpenSource = 'direct' | 'push_order_match' | 'push_otp' | 'push_news' | 'push_promo' | 'widget';
export type NotificationType = 'order_match' | 'otp' | 'news' | 'promo' | 'system';
export type TransactionType = 'withdraw' | 'internal_transfer' | 'cash_in_advance';
export type ViewMode = 'request' | 'history';
export type SymbolSource = 'search' | 'watchlist' | 'market_table' | 'top_stocks' | 'recently_viewed' | 'notification';

// --- Param types per event ---

export interface BaseEventParams {
  screen_name: string;
  timestamp_ms: number;
}

export interface FeatureViewedParams extends BaseEventParams {
  feature_name: FeatureName;
}

export interface FeatureActionParams extends BaseEventParams {
  feature_name: FeatureName;
  action_type: string;
}

export interface TabSwitchedParams extends BaseEventParams {
  from_tab: string;
  to_tab: string;
}

export interface SymbolSearchedParams extends BaseEventParams {
  query: string;
  result_count: number;
}

export interface SymbolSelectedParams extends BaseEventParams {
  symbol: string;
  source: SymbolSource;
}

export interface StockDetailViewedParams extends BaseEventParams {
  symbol: string;
  source: SymbolSource;
}

export interface OrderInitiatedParams extends BaseEventParams {
  order_type: OrderType;
  side: OrderSide;
  symbol: string;
  market: MarketType;
}

export interface OrderConfirmedParams extends BaseEventParams {
  order_type: OrderType;
  side: OrderSide;
  symbol: string;
  market: MarketType;
}

export interface OrderCancelledParams extends BaseEventParams {
  symbol: string;
  source: 'user_action' | 'system';
}

export interface PortfolioViewedParams extends BaseEventParams {
  account_type: string;
  view_mode: string;
}

export interface CashFlowViewedParams extends BaseEventParams {
  transaction_type: TransactionType;
  view_mode: ViewMode;
}

export interface EkycStartedParams extends BaseEventParams {
  entry_point: string;
}

export interface EkycStepEnteredParams extends BaseEventParams {
  step_name: EkycStepName;
  step_index: number;
}

export interface EkycStepCompletedParams extends BaseEventParams {
  step_name: EkycStepName;
  step_index: number;
  duration_ms: number;
}

export interface EkycDropOffParams extends BaseEventParams {
  step_name: EkycStepName;
  step_index: number;
  time_spent_ms: number;
}

export interface EkycScanAttemptedParams extends BaseEventParams {
  doc_type: DocType;
  attempt_number: number;
}

export interface EkycScanResultParams extends BaseEventParams {
  doc_type: DocType;
  success: boolean;
  attempt_number: number;
  error_code?: string;
}

export interface EkycCompletedParams extends BaseEventParams {
  total_duration_ms: number;
  retry_count: number;
}

export interface AppOpenedParams extends BaseEventParams {
  open_source: OpenSource;
  notification_type?: NotificationType;
}

export interface SessionEndedParams extends BaseEventParams {
  session_duration_ms: number;
  last_feature: string;
}

export interface NotificationReceivedParams extends BaseEventParams {
  notification_type: NotificationType;
}

export interface NotificationOpenedParams extends BaseEventParams {
  notification_type: NotificationType;
  deep_link_target: string;
}

export interface NotificationPermissionParams extends BaseEventParams {
  granted: boolean;
}
```

- [ ] **Bước 2: Verify file không có lỗi TypeScript**

```bash
cd /path/to/nhsv-mts-rn
npx tsc --noEmit src/utils/analytics/events.ts 2>&1 | head -20
```

Expected: không có lỗi (hoặc chỉ có lỗi về missing modules chưa tạo).

- [ ] **Bước 3: Commit**

```bash
git add src/utils/analytics/events.ts
git commit -m "feat(analytics): add event constants and TypeScript param types"
```

---

### Task 2: Tạo ekycTracking.ts — Timestamp helper cho eKYC steps

**Files:**
- Tạo: `src/utils/analytics/ekycTracking.ts`

- [ ] **Bước 1: Tạo file ekycTracking.ts**

```typescript
// src/utils/analytics/ekycTracking.ts
// Tracks entry timestamps for each eKYC step to calculate duration.
// Usage: call enterStep() when screen mounts, completeStep() or dropOff()
// when screen unmounts or user navigates away.

import { EkycStepName } from './events';

interface StepRecord {
  stepName: EkycStepName;
  stepIndex: number;
  enteredAt: number; // epoch ms
  scanAttempts: number;
}

class EkycTracker {
  private currentStep: StepRecord | null = null;
  private ekycStartedAt: number | null = null;
  private totalRetryCount: number = 0;

  startSession(): void {
    this.ekycStartedAt = Date.now();
    this.totalRetryCount = 0;
    this.currentStep = null;
  }

  enterStep(stepName: EkycStepName, stepIndex: number): void {
    this.currentStep = {
      stepName,
      stepIndex,
      enteredAt: Date.now(),
      scanAttempts: 0,
    };
  }

  recordScanAttempt(): number {
    if (!this.currentStep) return 1;
    this.currentStep.scanAttempts += 1;
    this.totalRetryCount += 1;
    return this.currentStep.scanAttempts;
  }

  getDurationMs(): number {
    if (!this.currentStep) return 0;
    return Date.now() - this.currentStep.enteredAt;
  }

  getTotalDurationMs(): number {
    if (!this.ekycStartedAt) return 0;
    return Date.now() - this.ekycStartedAt;
  }

  getTotalRetryCount(): number {
    return this.totalRetryCount;
  }

  getCurrentStep(): StepRecord | null {
    return this.currentStep;
  }

  clearSession(): void {
    this.currentStep = null;
    this.ekycStartedAt = null;
    this.totalRetryCount = 0;
  }
}

// Singleton — dùng chung xuyên suốt eKYC flow
export const ekycTracker = new EkycTracker();
```

- [ ] **Bước 2: Commit**

```bash
git add src/utils/analytics/ekycTracking.ts
git commit -m "feat(analytics): add eKYC step timestamp tracker"
```

---

### Task 3: Tạo userProperties.ts — User property setters

**Files:**
- Tạo: `src/utils/analytics/userProperties.ts`

- [ ] **Bước 1: Tạo file userProperties.ts**

```typescript
// src/utils/analytics/userProperties.ts
import analytics from '@react-native-firebase/analytics';

export type UserType = 'registered_only' | 'ekyc_completed' | 'active_trader';
export type AccountType = 'none' | 'normal' | 'margin';
export type PreferredMarket = 'equity' | 'derivatives' | 'both' | 'unknown';

interface UserProperties {
  user_type: UserType;
  account_type: AccountType;
  has_derivatives_account: 'true' | 'false';
  preferred_market: PreferredMarket;
}

// Tracks order history for preferred_market calculation
const orderCounts = { equity: 0, derivatives: 0 };

export function setUserId(username: string): void {
  // GA4 accepts max 256 chars. Username is already a non-sensitive identifier.
  analytics().setUserId(username.toUpperCase());
}

export function clearUserId(): void {
  analytics().setUserId(null);
}

export function setUserProperties(props: Partial<UserProperties>): void {
  // GA4 setUserProperties accepts string values only
  const stringProps: Record<string, string> = {};
  Object.entries(props).forEach(([k, v]) => {
    if (v !== undefined) stringProps[k] = String(v);
  });
  analytics().setUserProperties(stringProps);
}

export function upgradeUserType(newType: UserType): void {
  // Only upgrade, never downgrade: registered_only → ekyc_completed → active_trader
  setUserProperties({ user_type: newType });
}

export function recordOrderForMarketPreference(market: 'equity' | 'derivatives'): void {
  orderCounts[market] += 1;
  const total = orderCounts.equity + orderCounts.derivatives;
  if (total < 3) return; // Minimum 3 orders before switching from unknown

  const equityRatio = orderCounts.equity / total;
  const derivRatio = orderCounts.derivatives / total;

  let preferred: PreferredMarket;
  if (equityRatio >= 0.7) preferred = 'equity';
  else if (derivRatio >= 0.7) preferred = 'derivatives';
  else preferred = 'both';

  setUserProperties({ preferred_market: preferred });
}

export function resetOrderCounts(): void {
  orderCounts.equity = 0;
  orderCounts.derivatives = 0;
}
```

- [ ] **Bước 2: Commit**

```bash
git add src/utils/analytics/userProperties.ts
git commit -m "feat(analytics): add user property setters with market preference logic"
```

---

### Task 4: Tạo analyticsService.ts — Public API duy nhất

**Files:**
- Tạo: `src/utils/analytics/analyticsService.ts`

- [ ] **Bước 1: Tạo file analyticsService.ts**

```typescript
// src/utils/analytics/analyticsService.ts
// PUBLIC API: mọi tracking call trong app đi qua đây.
// KHÔNG import file này vào events.ts, ekycTracking.ts, userProperties.ts.
import analytics from '@react-native-firebase/analytics';
import { AnalyticsEvents } from './events';
import type {
  FeatureViewedParams, FeatureActionParams, TabSwitchedParams,
  SymbolSearchedParams, SymbolSelectedParams, StockDetailViewedParams,
  OrderInitiatedParams, OrderConfirmedParams, OrderCancelledParams,
  PortfolioViewedParams, CashFlowViewedParams,
  EkycStartedParams, EkycStepEnteredParams, EkycStepCompletedParams,
  EkycDropOffParams, EkycScanAttemptedParams, EkycScanResultParams, EkycCompletedParams,
  AppOpenedParams, SessionEndedParams,
  NotificationReceivedParams, NotificationOpenedParams, NotificationPermissionParams,
} from './events';
import { ekycTracker } from './ekycTracking';
import type { EkycStepName } from './events';

function now(): number {
  return Date.now();
}

function log(event: string, params: Record<string, unknown>): void {
  analytics().logEvent(event, { ...params, timestamp_ms: now() });
}

// ─── Feature Usage ─────────────────────────────────────────────────────────

export function trackFeatureViewed(params: Omit<FeatureViewedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.FEATURE_VIEWED, params);
}

export function trackFeatureAction(params: Omit<FeatureActionParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.FEATURE_ACTION, params);
}

export function trackTabSwitched(params: Omit<TabSwitchedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.TAB_SWITCHED, params);
}

export function trackSymbolSearched(params: Omit<SymbolSearchedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.SYMBOL_SEARCHED, params);
}

export function trackSymbolSelected(params: Omit<SymbolSelectedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.SYMBOL_SELECTED, params);
}

export function trackStockDetailViewed(params: Omit<StockDetailViewedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.STOCK_DETAIL_VIEWED, params);
}

// ─── Trading Funnel ─────────────────────────────────────────────────────────

export function trackOrderInitiated(params: Omit<OrderInitiatedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.ORDER_INITIATED, params);
}

export function trackOrderConfirmed(params: Omit<OrderConfirmedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.ORDER_CONFIRMED, params);
}

export function trackOrderCancelled(params: Omit<OrderCancelledParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.ORDER_CANCELLED, params);
}

export function trackPortfolioViewed(params: Omit<PortfolioViewedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.PORTFOLIO_VIEWED, params);
}

export function trackCashFlowViewed(params: Omit<CashFlowViewedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.CASH_FLOW_VIEWED, params);
}

// ─── eKYC Funnel ────────────────────────────────────────────────────────────

export function trackEkycStarted(params: Omit<EkycStartedParams, 'timestamp_ms'>): void {
  ekycTracker.startSession();
  log(AnalyticsEvents.EKYC_STARTED, params);
}

export function trackEkycStepEntered(stepName: EkycStepName, stepIndex: number, screenName: string): void {
  ekycTracker.enterStep(stepName, stepIndex);
  log(AnalyticsEvents.EKYC_STEP_ENTERED, { step_name: stepName, step_index: stepIndex, screen_name: screenName });
}

export function trackEkycStepCompleted(screenName: string): void {
  const step = ekycTracker.getCurrentStep();
  if (!step) return;
  const duration_ms = ekycTracker.getDurationMs();
  log(AnalyticsEvents.EKYC_STEP_COMPLETED, {
    step_name: step.stepName,
    step_index: step.stepIndex,
    duration_ms,
    screen_name: screenName,
  });
}

export function trackEkycDropOff(screenName: string): void {
  const step = ekycTracker.getCurrentStep();
  if (!step) return;
  const time_spent_ms = ekycTracker.getDurationMs();
  log(AnalyticsEvents.EKYC_DROP_OFF, {
    step_name: step.stepName,
    step_index: step.stepIndex,
    time_spent_ms,
    screen_name: screenName,
  });
}

export function trackEkycScanAttempted(params: Omit<EkycScanAttemptedParams, 'timestamp_ms'>): void {
  ekycTracker.recordScanAttempt();
  log(AnalyticsEvents.EKYC_SCAN_ATTEMPTED, params);
}

export function trackEkycScanResult(params: Omit<EkycScanResultParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.EKYC_SCAN_RESULT, params);
}

export function trackEkycCompleted(screenName: string): void {
  const total_duration_ms = ekycTracker.getTotalDurationMs();
  const retry_count = ekycTracker.getTotalRetryCount();
  log(AnalyticsEvents.EKYC_COMPLETED, { total_duration_ms, retry_count, screen_name: screenName });
  ekycTracker.clearSession();
}

// ─── Retention Signals ───────────────────────────────────────────────────────

export function trackAppOpened(params: Omit<AppOpenedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.APP_OPENED, params);
}

export function trackSessionEnded(params: Omit<SessionEndedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.SESSION_ENDED, params);
}

export function trackNotificationReceived(params: Omit<NotificationReceivedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.NOTIFICATION_RECEIVED, params);
}

export function trackNotificationOpened(params: Omit<NotificationOpenedParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.NOTIFICATION_OPENED, params);
}

export function trackNotificationPermission(params: Omit<NotificationPermissionParams, 'timestamp_ms'>): void {
  log(AnalyticsEvents.NOTIFICATION_PERMISSION, params);
}
```

- [ ] **Bước 2: Verify TypeScript**

```bash
npx tsc --noEmit 2>&1 | grep "analytics" | head -20
```

Expected: Không có lỗi từ các file analytics mới tạo.

- [ ] **Bước 3: Commit**

```bash
git add src/utils/analytics/analyticsService.ts
git commit -m "feat(analytics): add centralized analytics service — single Firebase wrapper"
```

---

### Task 5: Xóa ANALYTICS_EVENT enum cũ trong globals/index.tsx

**Files:**
- Chỉnh sửa: `src/globals/index.tsx`

> **Context:** File `src/globals/index.tsx` tại dòng 868–883 định nghĩa `enum ANALYTICS_EVENT`. Enum này sẽ được thay bằng `AnalyticsEvents` trong `events.ts`. Cần xóa để tránh nhầm lẫn.

- [ ] **Bước 1: Xóa enum ANALYTICS_EVENT trong src/globals/index.tsx**

Tìm và xóa block này (khoảng dòng 868–883):
```typescript
// XÓA TOÀN BỘ BLOCK NÀY:
enum ANALYTICS_EVENT {
  view_personal_info_page = 'view_personal_info_page',
  otp_submission = 'otp_submission',
  select_document_type = 'select_document_type',
  face_authentication = 'face_authentication',
  additional_services_registration = 'additional_services_registration',
  confirm_policy = 'confirm_policy',
  econtract_signing = 'econtract_signing',
  EKYC_START = 'ekyc_start',
  EKYC_SCREEN_VIEW = 'ekyc_screen_view',
  EKYC_DROP_OFF = 'ekyc_drop_off',
  EKYC_COMPLETE = 'ekyc_complete',
  EKYC_ERROR = 'ekyc_error',
  EKYC_SCAN_ID_SUCCESS = 'ekyc_scan_id_success',
  EKYC_SCAN_FACE_SUCCESS = 'ekyc_scan_face_success',
}
```

Cũng xóa export nếu có: `export { ANALYTICS_EVENT }` hoặc `export enum ANALYTICS_EVENT`.

- [ ] **Bước 2: Kiểm tra không còn reference nào đến ANALYTICS_EVENT**

```bash
grep -rn "ANALYTICS_EVENT" src/ --include="*.ts" --include="*.tsx"
```

Expected: Không có kết quả nào (hoặc chỉ trong file vừa chỉnh sửa — đã xóa).

- [ ] **Bước 3: Commit**

```bash
git add src/globals/index.tsx
git commit -m "refactor(analytics): remove deprecated ANALYTICS_EVENT enum"
```

---

### Task 6: Wire up setUserId + setUserProperties khi login thành công

**Files:**
- Chỉnh sửa: `src/reduxs/sagas/Authentication/VerifyOTP.ts`

> **Context:** Đây là saga xử lý OTP login. Khi `response.data` có userInfo, đó là thời điểm login thành công. `response.data.userInfo.username` là user identifier. Cần thêm tracking ngay sau khi xác nhận login thành công.

- [ ] **Bước 1: Thêm import vào VerifyOTP.ts**

Thêm vào đầu file, sau các import hiện có:
```typescript
import { setUserId, setUserProperties } from 'utils/analytics/userProperties';
```

- [ ] **Bước 2: Thêm tracking ngay sau khi login thành công**

Tìm đoạn code (khoảng dòng 43–57):
```typescript
yield doSetGlobalAccountListFunction(response.data.userInfo.accounts);
if (response.data.userInfo.userLevel !== 'USER_CHANGE_PASSWORD_REQUIRED') {
  yield put(setLoginStatus(true));
}
```

Thêm tracking ngay sau `yield put(setAuthToken(authToken))`:
```typescript
// Analytics: identify user và set initial properties
setUserId(response.data.userInfo.username);

// Xác định user_type dựa trên userLevel
// USER_CHANGE_PASSWORD_REQUIRED = chưa hoàn tất onboarding
const userType = response.data.userInfo.userLevel === 'USER_CHANGE_PASSWORD_REQUIRED'
  ? 'registered_only'
  : 'registered_only'; // Sẽ upgrade lên ekyc_completed khi hoàn thành eKYC

const accounts = response.data.userInfo.accounts ?? [];
const hasDerivatives = accounts.some(
  (acc: { accountType?: string }) => acc.accountType === 'DERIVATIVE'
);

setUserProperties({
  user_type: userType,
  account_type: accounts.length > 0 ? 'normal' : 'none',
  has_derivatives_account: hasDerivatives ? 'true' : 'false',
  preferred_market: 'unknown',
});
```

- [ ] **Bước 3: Build để check lỗi TypeScript**

```bash
npx tsc --noEmit 2>&1 | grep "VerifyOTP" | head -10
```

Expected: Không có lỗi.

- [ ] **Bước 4: Commit**

```bash
git add src/reduxs/sagas/Authentication/VerifyOTP.ts
git commit -m "feat(analytics): set userId and user properties on login success"
```

---

### Task 7: Sửa bug ekyc_drop_off + nâng cấp toàn bộ eKYC tracking

> **Context bug:** `ekyc_drop_off` chỉ fire ~22% trường hợp (6/27 expected). Nguyên nhân: event chỉ được gắn vào một số màn hình và không có listener cho `AppState` change (user bấm Home button). Fix: thêm `AppState` listener + `navigation.beforeRemove` listener vào MỖI màn hình eKYC.

**Tạo hook tái sử dụng cho drop-off detection:**

**Files:**
- Tạo: `src/hooks/useEkycDropOffDetection.ts`

- [ ] **Bước 1: Tạo hook useEkycDropOffDetection.ts**

```typescript
// src/hooks/useEkycDropOffDetection.ts
// Hook này gắn vào mọi màn hình eKYC.
// Fire ekyc_drop_off khi: user bấm back/navigate away HOẶC app xuống background.

import { useEffect, useRef } from 'react';
import { AppState, AppStateStatus } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { trackEkycDropOff } from 'utils/analytics/analyticsService';
import type { EkycStepName } from 'utils/analytics/events';

interface Options {
  stepName: EkycStepName;
  stepIndex: number;
  screenName: string;
}

export function useEkycDropOffDetection({ stepName, stepIndex, screenName }: Options): void {
  const navigation = useNavigation();
  const hasDroppedOff = useRef(false);

  function fireDropOff(): void {
    if (hasDroppedOff.current) return; // Chỉ fire 1 lần
    hasDroppedOff.current = true;
    trackEkycDropOff(screenName);
  }

  useEffect(() => {
    // Listener: user bấm back hoặc navigation pop
    const unsubscribeNav = navigation.addListener('beforeRemove', () => {
      fireDropOff();
    });

    // Listener: app xuống background (bấm Home button)
    const handleAppStateChange = (nextState: AppStateStatus) => {
      if (nextState === 'background' || nextState === 'inactive') {
        fireDropOff();
      }
    };
    const appStateSub = AppState.addEventListener('change', handleAppStateChange);

    return () => {
      unsubscribeNav();
      appStateSub.remove();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
}
```

- [ ] **Bước 2: Commit hook**

```bash
git add src/hooks/useEkycDropOffDetection.ts
git commit -m "feat(analytics): add reusable eKYC drop-off detection hook"
```

**Cập nhật EKYCOTPScreen — step 1: otp_verification:**

**Files:**
- Chỉnh sửa: `src/screens/EKYCOTPScreen/index.tsx`

- [ ] **Bước 3: Cập nhật EKYCOTPScreen**

Tìm import analytics hiện tại và thay bằng:
```typescript
import { trackEkycStarted, trackEkycStepEntered, trackEkycStepCompleted } from 'utils/analytics/analyticsService';
import { useEkycDropOffDetection } from 'hooks/useEkycDropOffDetection';
```

Tìm đoạn gọi `analytics().logEvent(ANALYTICS_EVENT.EKYC_START, ...)` và thay bằng:
```typescript
// Khi component mount (eKYC bắt đầu từ màn hình OTP)
useEffect(() => {
  trackEkycStarted({ entry_point: 'otp_screen', screen_name: '[eKYC]_OTPverification' });
  trackEkycStepEntered('otp_verification', 0, '[eKYC]_OTPverification');
}, []);
```

Thêm hook drop-off trong component (trước return):
```typescript
useEkycDropOffDetection({
  stepName: 'otp_verification',
  stepIndex: 0,
  screenName: '[eKYC]_OTPverification',
});
```

Khi OTP verify thành công (trước khi navigate sang màn tiếp theo):
```typescript
trackEkycStepCompleted('[eKYC]_OTPverification');
```

- [ ] **Bước 4: Commit**

```bash
git add src/screens/EKYCOTPScreen/index.tsx
git commit -m "feat(analytics): upgrade ekyc OTP screen tracking — step 1/10"
```

**Cập nhật EKYCChooseIDTypeScreen — step 2: document_selection:**

**Files:**
- Chỉnh sửa: `src/screens/EKYCChooseIDTypeScreen/index.tsx`

- [ ] **Bước 5: Cập nhật EKYCChooseIDTypeScreen**

Xóa import analytics cũ. Thêm:
```typescript
import { trackEkycStepEntered, trackEkycStepCompleted } from 'utils/analytics/analyticsService';
import { useEkycDropOffDetection } from 'hooks/useEkycDropOffDetection';
```

Thêm trong component:
```typescript
// Mount: enter step
useEffect(() => {
  trackEkycStepEntered('document_selection', 1, '[eKYC]_DocumentSelection');
}, []);

// Drop-off detection
useEkycDropOffDetection({
  stepName: 'document_selection',
  stepIndex: 1,
  screenName: '[eKYC]_DocumentSelection',
});
```

Khi user chọn loại giấy tờ và nhấn Next (trước khi navigate):
```typescript
trackEkycStepCompleted('[eKYC]_DocumentSelection');
```

- [ ] **Bước 6: Commit**

```bash
git add src/screens/EKYCChooseIDTypeScreen/index.tsx
git commit -m "feat(analytics): upgrade ekyc document selection screen tracking — step 2/10"
```

**Cập nhật EKYCScanFaceIntroScreen — scan_face:**

**Files:**
- Chỉnh sửa: `src/screens/EKYCScanFaceIntroScreen/index.tsx`

- [ ] **Bước 7: Cập nhật EKYCScanFaceIntroScreen**

Xóa import analytics cũ (`face_authentication`, `EKYC_SCAN_FACE_SUCCESS`, `EKYC_DROP_OFF`). Thêm:
```typescript
import {
  trackEkycStepEntered,
  trackEkycStepCompleted,
  trackEkycScanAttempted,
  trackEkycScanResult,
} from 'utils/analytics/analyticsService';
import { useEkycDropOffDetection } from 'hooks/useEkycDropOffDetection';
```

```typescript
useEffect(() => {
  trackEkycStepEntered('scan_face', 4, '[eKYC]_FaceAuthenticationInstruction');
}, []);

useEkycDropOffDetection({
  stepName: 'scan_face',
  stepIndex: 4,
  screenName: '[eKYC]_FaceAuthenticationInstruction',
});
```

Khi bắt đầu scan:
```typescript
trackEkycScanAttempted({ doc_type: 'face', attempt_number: 1, screen_name: '[eKYC]_FaceAuthenticationInstruction' });
```

Khi scan thành công (thay `ekyc_scan_face_success`):
```typescript
trackEkycScanResult({ doc_type: 'face', success: true, attempt_number: 1, screen_name: '[eKYC]_FaceAuthenticationInstruction' });
trackEkycStepCompleted('[eKYC]_FaceAuthenticationInstruction');
```

Khi scan thất bại (thay `ekyc_error`):
```typescript
trackEkycScanResult({
  doc_type: 'face',
  success: false,
  attempt_number: ekycTracker.getCurrentStep()?.scanAttempts ?? 1,
  error_code: String(errorCode),
  screen_name: '[eKYC]_FaceAuthenticationInstruction',
});
```

- [ ] **Bước 8: Commit**

```bash
git add src/screens/EKYCScanFaceIntroScreen/index.tsx
git commit -m "feat(analytics): upgrade ekyc face scan screen tracking — step 5/10"
```

**Cập nhật các saga EKYC còn lại:**

**Files:**
- Chỉnh sửa: `src/reduxs/sagas/EKYC/EKYCScanIdDone.ts`
- Chỉnh sửa: `src/reduxs/sagas/EKYC/VerifyOTP.ts`
- Chỉnh sửa: `src/reduxs/sagas/EKYC/OnPressNextInConfirmPolicyScreen.ts`

- [ ] **Bước 9: Cập nhật EKYCScanIdDone.ts (scan ID front + back)**

Xóa import analytics cũ. Thêm:
```typescript
import { trackEkycScanAttempted, trackEkycScanResult } from 'utils/analytics/analyticsService';
import { ekycTracker } from 'utils/analytics/ekycTracking';
```

Tìm đoạn gọi `analytics().logEvent(ANALYTICS_EVENT.EKYC_SCAN_ID_SUCCESS, ...)` và thay:
```typescript
// Scan ID thành công
trackEkycScanResult({
  doc_type: isScanningFront ? 'id_front' : 'id_back',
  success: true,
  attempt_number: ekycTracker.getCurrentStep()?.scanAttempts ?? 1,
  screen_name: isScanningFront ? '[eKYC]_ScanIdFront' : '[eKYC]_ScanIdBack',
});
```

Tìm đoạn gọi `analytics().logEvent(ANALYTICS_EVENT.EKYC_ERROR, ...)` và thay:
```typescript
// Scan ID thất bại
trackEkycScanResult({
  doc_type: isScanningFront ? 'id_front' : 'id_back',
  success: false,
  attempt_number: ekycTracker.getCurrentStep()?.scanAttempts ?? 1,
  error_code: String(dataConverted.logLiveNessCardRear?.statusCode ?? ''),
  screen_name: isScanningFront ? '[eKYC]_ScanIdFront' : '[eKYC]_ScanIdBack',
});
```

- [ ] **Bước 10: Cập nhật OnPressNextInConfirmPolicyScreen.ts (ekyc_completed)**

Xóa import analytics cũ. Thêm:
```typescript
import { trackEkycCompleted } from 'utils/analytics/analyticsService';
import { upgradeUserType } from 'utils/analytics/userProperties';
```

Tìm đoạn gọi `analytics().logEvent(ANALYTICS_EVENT.EKYC_COMPLETE, ...)` và thay:
```typescript
trackEkycCompleted('[eKYC]_OpenAccountSuccessfully');
upgradeUserType('ekyc_completed');
```

- [ ] **Bước 11: Commit tất cả saga EKYC**

```bash
git add src/reduxs/sagas/EKYC/
git commit -m "feat(analytics): upgrade all eKYC sagas to use analyticsService"
```

---

### Task 8: Thêm trading events — order_initiated + order_confirmed

**Files:**
- Chỉnh sửa: `src/reduxs/sagas/Trade/PlaceOrder.ts`

> **Context:** `PlaceOrder.ts` xử lý đặt lệnh thường (LO, MP...) và lệnh nâng cao. Thành công khi `showMessage({ type: 'success', ... })` được gọi (khoảng dòng 128, 175, 213, 304, 336). `order_initiated` nên fire khi user bấm nút xác nhận lần đầu — trước khi API call. `order_confirmed` fire sau khi API trả về success.

- [ ] **Bước 1: Thêm import**

```typescript
import { trackOrderInitiated, trackOrderConfirmed } from 'utils/analytics/analyticsService';
import { recordOrderForMarketPreference } from 'utils/analytics/userProperties';
import type { OrderType, MarketType, OrderSide } from 'utils/analytics/events';
```

- [ ] **Bước 2: Thêm trackOrderInitiated trước mỗi API call đặt lệnh**

Tìm hàm saga chính trong PlaceOrder.ts (khoảng dòng 80+). Các biến đã có sẵn trong saga — thêm tracking SAU các dòng `yield select(...)` và TRƯỚC khi call API:

```typescript
// Các biến này đã được select trong saga (dòng 94–100):
// const currentSymbol: ISymbolInfo = yield select(...state.currentSymbol)
// const tradeOrderTypeValue: ORDER_TYPE = yield select(...state.tradeOrderType.value)
// const tradeSellBuyType: SELL_BUY_TYPE = yield select(...state.tradeSellBuyType)
const isDerivative: boolean = yield select(isDerivativeAccountSelector);
const market: MarketType = isDerivative ? 'derivatives' : 'equity';

trackOrderInitiated({
  order_type: tradeOrderTypeValue as OrderType,
  side: tradeSellBuyType === SELL_BUY_TYPE.BUY ? 'buy' : 'sell',
  symbol: currentSymbol.s,  // .s là mã CK (vd: "VN30F2506")
  market,
  screen_name: '[Trade]_Order',
});
```

- [ ] **Bước 3: Thêm trackOrderConfirmed tại mỗi điểm success**

Tại mỗi block `type: 'success'` (có khoảng 5 chỗ), thêm sau `showMessage(...)`:

```typescript
trackOrderConfirmed({
  order_type: orderType,
  side,
  symbol,
  market,
  screen_name: '[Trade]_Order',
});
recordOrderForMarketPreference(market);
```

- [ ] **Bước 4: Tương tự với PlaceOrderDerivative.ts**

Lặp lại bước 2 và 3 cho `src/reduxs/sagas/Trade/PlaceOrderDerivative.ts` với `market: 'derivatives'` và `screen_name: '[Trade]_Order'`.

- [ ] **Bước 5: Commit**

```bash
git add src/reduxs/sagas/Trade/PlaceOrder.ts src/reduxs/sagas/Trade/PlaceOrderDerivative.ts
git commit -m "feat(analytics): add order_initiated and order_confirmed tracking"
```

---

### Task 9: Thêm portfolio_viewed + cash_flow_viewed

**Files:**
- Chỉnh sửa: `src/screens/AssetTab/index.tsx` (hoặc file portfolio screen chính)
- Chỉnh sửa: `src/screens/CashFlowScreen/index.tsx`

- [ ] **Bước 1: Thêm portfolio_viewed vào AssetTab**

Tìm màn hình danh mục (`src/screens/AssetTab/index.tsx`). Thêm:
```typescript
import { trackPortfolioViewed } from 'utils/analytics/analyticsService';

// Trong component, khi focus:
useFocusEffect(
  useCallback(() => {
    trackPortfolioViewed({
      account_type: currentAccount?.accountType ?? 'unknown',
      view_mode: currentViewMode ?? 'portfolio',
      screen_name: '[Asset]_Portfolio',
    });
  }, [currentAccount, currentViewMode])
);
```

- [ ] **Bước 2: Thêm cash_flow_viewed vào CashFlowScreen**

Trong `src/screens/CashFlowScreen/index.tsx`, tìm chỗ user chuyển tab loại giao dịch. Thêm:
```typescript
import { trackCashFlowViewed } from 'utils/analytics/analyticsService';
import type { TransactionType, ViewMode } from 'utils/analytics/events';

// Map CashFlowOptionKeys → TransactionType
const TRANSACTION_TYPE_MAP: Record<string, TransactionType> = {
  WITHDRAW: 'withdraw',
  INTERNAL_TRANSFER: 'internal_transfer',
  CASH_IN_ADVANCE: 'cash_in_advance',
};

// Map CashFlowOptionValues → ViewMode  
const VIEW_MODE_MAP: Record<string, ViewMode> = {
  REQUEST: 'request',
  HISTORY: 'history',
};

// Khi tab thay đổi (và khi màn hình mount lần đầu):
useEffect(() => {
  trackCashFlowViewed({
    transaction_type: TRANSACTION_TYPE_MAP[selectedTab] ?? 'withdraw',
    view_mode: VIEW_MODE_MAP[selectedSubTab] ?? 'request',
    screen_name: '[Feature]_Orderbook', // dùng screen name từ ANALYTICS_TRACKING_SCREEN
  });
}, [selectedTab, selectedSubTab]);
```

- [ ] **Bước 3: Commit**

```bash
git add src/screens/AssetTab/index.tsx src/screens/CashFlowScreen/index.tsx
git commit -m "feat(analytics): add portfolio_viewed and cash_flow_viewed tracking"
```

---

## SPRINT 2 — P1: Engagement Events

---

### Task 10: Thêm tab_switched vào bottom navigation

**Files:**
- Chỉnh sửa: `src/navigation/index.tsx`

> **Context:** File `src/navigation/index.tsx` đã có `analytics().logScreenView(...)` tại dòng ~183. Bottom tab navigator cần `screenListeners` để bắt event `tabPress`.

- [ ] **Bước 1: Thêm import**

```typescript
import { trackTabSwitched } from 'utils/analytics/analyticsService';
```

- [ ] **Bước 2: Thêm tab tracking vào Bottom Tab Navigator**

Tìm `<Tab.Navigator` trong navigation. Thêm `screenListeners`:

```typescript
<Tab.Navigator
  screenListeners={({ navigation }) => ({
    tabPress: e => {
      const state = navigation.getState();
      const currentRoute = state.routes[state.index]?.name ?? '';
      const targetRoute = e.target?.split('-')[0] ?? '';
      if (currentRoute !== targetRoute) {
        trackTabSwitched({
          from_tab: currentRoute,
          to_tab: targetRoute,
          screen_name: currentRoute,
        });
      }
    },
  })}
  // ... props khác giữ nguyên
>
```

- [ ] **Bước 3: Refactor logScreenView qua service**

Tìm đoạn `analytics().logScreenView(...)` (dòng ~183). Không thay đổi logic — chỉ wrap để code nhất quán. Giữ nguyên `analytics().logScreenView()` vì đây là GA4 built-in method (không phải custom event), không cần đưa vào service.

- [ ] **Bước 4: Commit**

```bash
git add src/navigation/index.tsx
git commit -m "feat(analytics): add tab_switched tracking to bottom navigation"
```

---

### Task 11: Thêm feature_viewed cho các màn hình chính

**Files:**
- Chỉnh sửa: `src/screens/HomeTab/index.tsx`
- Chỉnh sửa: `src/screens/MarketScreen/index.tsx` (hoặc tương đương)
- Chỉnh sửa: `src/screens/TradeTab/index.tsx`

> **Pattern tái sử dụng cho mọi màn hình:** Dùng `useFocusEffect` từ `@react-navigation/native` để track khi screen active.

- [ ] **Bước 1: Thêm trackFeatureViewed vào HomeTab**

```typescript
import { trackFeatureViewed } from 'utils/analytics/analyticsService';
import { useFocusEffect } from '@react-navigation/native';
import { useCallback } from 'react';

// Trong component:
useFocusEffect(
  useCallback(() => {
    trackFeatureViewed({ feature_name: 'market_equity', screen_name: '[Home]_VNMarket' });
  }, [])
);
```

- [ ] **Bước 2: Thêm trackFeatureViewed cho Market tabs**

Trong MarketScreen, khi user chuyển tab (Watchlist, Market, Derivatives...):
```typescript
const MARKET_TAB_FEATURE_MAP: Record<string, FeatureName> = {
  Watchlist: 'market_watchlist',
  Market: 'market_equity',
  Derivatives: 'market_derivatives',
  TopStocks: 'market_topstocks',
  CW: 'market_cw',
  ETF: 'market_etf',
};

// Khi tab thay đổi:
trackFeatureViewed({
  feature_name: MARKET_TAB_FEATURE_MAP[activeTab] ?? 'market_equity',
  screen_name: currentScreenName,
});
```

- [ ] **Bước 3: Thêm trackFeatureViewed cho Trade tab**

```typescript
useFocusEffect(
  useCallback(() => {
    trackFeatureViewed({ feature_name: 'order_trade', screen_name: '[Trade]_Order' });
  }, [])
);
```

- [ ] **Bước 4: Commit**

```bash
git add src/screens/HomeTab/ src/screens/MarketScreen/ src/screens/TradeTab/
git commit -m "feat(analytics): add feature_viewed tracking to main screens"
```

---

### Task 12: Thêm app_opened + session_ended

**Files:**
- Chỉnh sửa: `src/App.tsx` (hoặc root component có AppState access)

> **Context:** `src/navigation/index.tsx` đã có `AppState` listener. Đây là nơi tốt nhất để thêm app_opened và session_ended.

- [ ] **Bước 1: Thêm import**

```typescript
import { trackAppOpened, trackSessionEnded } from 'utils/analytics/analyticsService';
```

- [ ] **Bước 2: Thêm tracking vào AppState handler trong navigation/index.tsx**

Tìm `AppState.addEventListener` trong `src/navigation/index.tsx` (hoặc App.tsx). Thêm:

```typescript
const sessionStartTime = useRef<number>(Date.now());
const lastFeature = useRef<string>('unknown');

// Track app_opened khi foreground
const handleAppStateChange = (nextState: AppStateStatus) => {
  if (nextState === 'active') {
    trackAppOpened({ open_source: 'direct', screen_name: 'app' });
    sessionStartTime.current = Date.now();
  } else if (nextState === 'background') {
    const duration_ms = Date.now() - sessionStartTime.current;
    trackSessionEnded({ session_duration_ms: duration_ms, last_feature: lastFeature.current, screen_name: 'app' });
  }
};
```

- [ ] **Bước 3: Commit**

```bash
git add src/navigation/index.tsx
git commit -m "feat(analytics): add app_opened and session_ended tracking"
```

---

### Task 13: Thêm notification tracking vào OneSignal

**Files:**
- Chỉnh sửa: `src/utils/OneSignal.ts`

> **Context:** `OneSignal.ts` đã có `foregroundWillDisplay` listener. Cần thêm tracking cho `received` và `opened`. Notification types được phân biệt qua `data.type` và `data['noti-type']`.

- [ ] **Bước 1: Thêm import**

```typescript
import { trackNotificationReceived, trackNotificationOpened, trackNotificationPermission } from 'utils/analytics/analyticsService';
import type { NotificationType } from 'utils/analytics/events';
```

- [ ] **Bước 2: Thêm helper để map notification data → NotificationType**

```typescript
function getNotificationType(data: Record<string, unknown>): NotificationType {
  if (!data || !('type' in data)) return 'system';
  if (data.type === 'nhsvotp1') return 'otp';
  if (data.type === 'nhsv' && data['noti-type'] === 'order') return 'order_match';
  if (data.type === 'nhsv') return 'news';
  return 'system';
}
```

- [ ] **Bước 3: Thêm tracking vào foregroundWillDisplay listener**

Trong `OneSignal.Notifications.addEventListener('foregroundWillDisplay', ...)`, thêm ở đầu callback:

```typescript
OneSignal.Notifications.addEventListener('foregroundWillDisplay', notificationReceivedEvent => {
  const data = notificationReceivedEvent.notification.additionalData as Record<string, unknown> ?? {};
  const notifType = getNotificationType(data);
  
  // Track notification received
  trackNotificationReceived({ notification_type: notifType, screen_name: 'background' });

  // ... code hiện tại giữ nguyên
});
```

- [ ] **Bước 4: Thêm clicked listener**

Sau `foregroundWillDisplay` listener, thêm:

```typescript
OneSignal.Notifications.addEventListener('click', event => {
  const data = event.notification.additionalData as Record<string, unknown> ?? {};
  const notifType = getNotificationType(data);
  const deepLinkTarget = typeof data['screen'] === 'string' ? data['screen'] : 'unknown';
  
  trackNotificationOpened({
    notification_type: notifType,
    deep_link_target: deepLinkTarget,
    screen_name: 'notification',
  });
});
```

- [ ] **Bước 5: Thêm permission tracking**

Trong `initOneSignal()`, sau khi check permission:

```typescript
const notificationPermission = await checkNotificationPermission();
trackNotificationPermission({
  granted: notificationPermission !== 'denied',
  screen_name: 'app_init',
});
```

- [ ] **Bước 6: Commit**

```bash
git add src/utils/OneSignal.ts
git commit -m "feat(analytics): add notification received/opened/permission tracking"
```

---

## SPRINT 3 — P2: GA4 Admin Configuration (thực hiện thủ công trên GA4 UI)

> **Lưu ý:** Sprint này không có code changes trong nhsv-mts-rn. Các bước thực hiện trên GA4 Admin Console tại: https://analytics.google.com — Property: **NHSV Pro** (`properties/478227972`)

---

### Task 14: Sửa typo tên UI của custom dimensions hiện có

**Thực hiện tại:** GA4 Admin → Custom definitions → Custom dimensions

- [ ] **Bước 1: Sửa `error_type` (đang hiển thị sai là `error_typ`)**
  - Vào Custom definitions → tìm dimension `error_type`
  - Click Edit → đổi Display name từ `error_typ` → `error_type`
  - Save

- [ ] **Bước 2: Đổi tên UI của `screen_name` (đang hiển thị là `dropoff_screen`)**
  - Vào Custom definitions → tìm dimension với Display name `dropoff_screen`
  - Click Edit → đổi Display name thành `screen_name`
  - Save

---

### Task 15: Đăng ký 5 Event-scoped Custom Dimensions mới

**Thực hiện tại:** GA4 Admin → Custom definitions → Create custom dimension

Tạo lần lượt 5 dimensions sau:

- [ ] **feature_name** — Scope: Event, Event parameter: `feature_name`, Display name: `Feature Name`
- [ ] **order_type** — Scope: Event, Event parameter: `order_type`, Display name: `Order Type`
- [ ] **ekyc_step** — Scope: Event, Event parameter: `step_name`, Display name: `eKYC Step`
- [ ] **open_source** — Scope: Event, Event parameter: `open_source`, Display name: `App Open Source`
- [ ] **notification_type** — Scope: Event, Event parameter: `notification_type`, Display name: `Notification Type`

---

### Task 16: Đăng ký 4 User-scoped Custom Dimensions mới

**Thực hiện tại:** GA4 Admin → Custom definitions → Create custom dimension → chọn Scope: **User**

- [ ] **user_type** — Scope: User, User property: `user_type`, Display name: `User Type`
- [ ] **account_type** — Scope: User, User property: `account_type`, Display name: `Account Type`
- [ ] **has_derivatives_account** — Scope: User, User property: `has_derivatives_account`, Display name: `Has Derivatives`
- [ ] **preferred_market** — Scope: User, User property: `preferred_market`, Display name: `Preferred Market`

---

### Task 17: Tạo các GA4 Explore Reports

**Thực hiện tại:** GA4 → Explore → Create new exploration

- [ ] **Report 1: Feature Adoption**

  - Technique: Free form
  - Dimensions: `Feature Name`, `User Type`
  - Metrics: `Event count`, `Total users`
  - Filter: Event name = `feature_viewed`
  - Breakdown: Thêm row `feature_action` để tính engagement rate

- [ ] **Report 2: eKYC Funnel**

  - Technique: Funnel exploration
  - Steps (theo thứ tự):
    1. Event: `ekyc_started`
    2. Event: `ekyc_step_entered` với `ekyc_step = otp_verification`
    3. Event: `ekyc_step_entered` với `ekyc_step = document_selection`
    4. Event: `ekyc_step_entered` với `ekyc_step = scan_id_front`
    5. Event: `ekyc_step_entered` với `ekyc_step = scan_face`
    6. Event: `ekyc_step_entered` với `ekyc_step = personal_info`
    7. Event: `ekyc_step_entered` với `ekyc_step = policy_confirm`
    8. Event: `ekyc_completed`
  - Breakdown: `User Type`

- [ ] **Report 3: Trading Activation Funnel**

  - Technique: Funnel exploration
  - Steps:
    1. Event: `ekyc_completed`
    2. Event: `order_initiated`
    3. Event: `order_confirmed`
  - Breakdown: `Order Type`, `market`

---

## Checklist tổng kết

### Sprint 1 — P0
- [ ] `src/utils/analytics/events.ts` — Event constants + types
- [ ] `src/utils/analytics/ekycTracking.ts` — Timestamp tracker
- [ ] `src/utils/analytics/userProperties.ts` — User property setters
- [ ] `src/utils/analytics/analyticsService.ts` — Public API
- [ ] `src/globals/index.tsx` — Xóa ANALYTICS_EVENT enum cũ
- [ ] `src/reduxs/sagas/Authentication/VerifyOTP.ts` — setUserId + setUserProperties
- [ ] `src/hooks/useEkycDropOffDetection.ts` — Reusable drop-off hook
- [ ] eKYC screens — drop-off fix + upgraded events (6 màn hình, theo pattern Task 7):
  - [ ] `src/screens/EKYCOTPScreen/index.tsx` — step 1: otp_verification
  - [ ] `src/screens/EKYCChooseIDTypeScreen/index.tsx` — step 2: document_selection
  - [ ] `src/screens/EKYCScanFaceIntroScreen/index.tsx` — step 5: scan_face
  - [ ] `src/screens/EKYCRegisterAnotherInformationScreen/index.tsx` — step 6: personal_info
  - [ ] `src/screens/EKYCRegisterMoreServicesScreen/index.tsx` — step 7: additional_services
  - [ ] `src/screens/EKYCSignContractScreen/index.tsx` — step 10: sign_contract
  - (Steps 3,4: scan_id_front/back và step 8,9: bank_registration/policy_confirm được handle qua saga)
- [ ] Tất cả eKYC sagas — upgraded events
- [ ] `src/reduxs/sagas/Trade/PlaceOrder.ts` — order_initiated + order_confirmed
- [ ] `src/reduxs/sagas/Trade/PlaceOrderDerivative.ts` — order_initiated + order_confirmed
- [ ] Portfolio screen — portfolio_viewed
- [ ] `src/screens/CashFlowScreen/index.tsx` — cash_flow_viewed

### Sprint 2 — P1
- [ ] `src/navigation/index.tsx` — tab_switched
- [ ] Main screens (Home, Market, Trade) — feature_viewed
- [ ] App/Navigation root — app_opened + session_ended
- [ ] `src/utils/OneSignal.ts` — notification tracking

### Sprint 3 — P2 (GA4 Admin — manual)
- [ ] Fix typo `error_typ` → `error_type`
- [ ] Rename `dropoff_screen` → `screen_name`
- [ ] Tạo 5 event-scoped dimensions
- [ ] Tạo 4 user-scoped properties
- [ ] Tạo 3 GA4 Explore reports

---

**Document Status:** Sẵn sàng triển khai
**For:** Mobile Dev Team (nhsv-mts-rn)
**Spec:** `docs/superpowers/specs/2026-06-07-analytics-tracking-design.md`
