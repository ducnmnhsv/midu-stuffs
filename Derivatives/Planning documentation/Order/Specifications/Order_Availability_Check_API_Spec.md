# Order Availability Check API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Orders - Pre-Order Validation  
**Version:** 1.2  
**Date:** March 23, 2026

> **Note:** Check maximum order quantity before placing order (DRORD-028). Response đồng thời bổ sung **buying power** từ DRACC-031 (`value_withdrawable_collateral_assets`). **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) §2.1.5 (DRACC-031), §2.3.8 (DRORD-028).

---

## 1. Overview

### 1.1 Purpose

Order Availability Check cho phép trader kiểm tra **khả năng đặt lệnh tối đa** trước khi submit order, dựa trên:
- Margin khả dụng (available margin)
- Position limits
- Thanh khoản thị trường (market liquidity)

Cùng một response trả thêm **`buyingPower`** (giá trị tài sản đảm bảo có thể rút theo Lotte — map từ DRACC-031) để FE không phải gọi thêm API asset khi đang ở màn đặt lệnh.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Check Availability | GET | `/api/v1/derivatives/order/checkAvailability` |

*(Trên gateway có thể expose dưới dạng `GET /rest/api/v1/derivatives/order/checkAvailability` — cùng contract.)*

### 1.3 Response Format Standards

**Success (Single Type):**
```json
{
  "availableQuantity": 100,
  "availableLiquidity": 150,
  "buyingPower": 125000000
}
```

**Success (Both Types - when sellBuyType is omitted):**
```json
{
  "buy": {
    "availableQuantity": 100,
    "availableLiquidity": 150
  },
  "sell": {
    "availableQuantity": 50,
    "availableLiquidity": 200
  },
  "buyingPower": 125000000
}
```

**Error:**
```json
{
  "code": "ERROR_CODE",
  "message": "Error message"
}
```

or

```json
{
  "code": "INVALID_PARAMETER",
  "params": [...]
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Return only Lotte-provided values (no calculated fields); `buyingPower` map trực tiếp từ DRACC-031 (cùng nguyên tắc parse số)
- If `sellBuyType` provided → flat response cho quantity/liquidity + **root-level** `buyingPower`
- If `sellBuyType` omitted → nested `buy` / `sell` + **root-level** `buyingPower` (một giá trị theo tài khoản, không tách Buy/Sell)
- **Composite (DRORD-028 + DRACC-031):** xem **§3.3.1** — thiếu data / lỗi không có payload hợp lệ → field tương ứng **`null`** (200 nếu còn nguồn kia dùng được); lỗi Core **có** `error_desc` → **pass-through** message (422), không trả partial success cho nhánh đó

---

## 2. Business Rules

### 2.1 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, symbol | `FIELD_IS_REQUIRED` |
| Valid Sell/Buy Type | If provided, sellBuyType must be `BUY` or `SELL` | `INVALID_VALUE` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |
| Valid Symbol | Symbol must be valid derivatives contract | Lotte validates |

**Note:** Business rules (margin, position limits) are calculated by Lotte Core.

### 2.2 Availability Calculation

**Available Order Quantity (`availableQuantity`):**

Calculated by Lotte based on:
- Available margin in account
- Margin per contract (from symbol info)
- Current open positions
- Position limits (max long/short)
- Risk constraints

**Available Liquidity (`availableLiquidity`):**

Real-time market liquidity:
- Aggregate of best bid/offer volumes
- Changes rapidly during trading session
- Indicates market depth for large orders

### 2.3 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V1005] Không đủ ký quỹ"` |
| `en` | `E` | `"[E1005] Insufficient margin"` |
| `ko` | `K` | `"[K1005] 마진 부족"` |

---

## 3. API: Check Order Availability

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/order/checkAvailability`

**Lotte endpoints (composite):**

| Lotte API | URL (pattern) | Role |
|-----------|----------------|------|
| **DRORD-028** | `[Root URL APIKEY]/tuxsvc/der/order/dr-available-order-qty` | `avail_order_qty`, `avail_liq_qty` — Lotte_DR §2.3.8. POST body: `acnt_no`, `code`, `sell_buy_type` (1: Buy, 2: Sell). |
| **DRACC-031** | `[Root URL APIKEY]/tuxsvc/der/account/dr-balance-securities-info` | `value_withdrawable_collateral_assets` → TradeX `buyingPower` — Lotte_DR §2.1.5. POST body: `account_no`, `inquiry_date` (yyyymmdd), `hts_user_id`. |

Mỗi request TradeX `checkAvailability` gọi **DRACC-031 đúng một lần** (theo tài khoản + ngày tra cứu), song song hoặc sau khi có đủ tham số; gọi **DRORD-028** một hoặc hai lần như hiện tại.

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 3.2 Request Mapping

**TradeX Query Params → Lotte Request Body:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `symbol` | String | ✅ | `code` | Direct | Mã hợp đồng (VN30F2402) |
| `sellBuyType` | String | ❌ | `sell_buy_type` | Enum map | `BUY`→1, `SELL`→2 (optional) |

**Sell/Buy Type Mapping:**

| TradeX `sellBuyType` | Lotte `sell_buy_type` | Note |
|----------------------|-----------------------|------|
| `BUY` | `1` | Check buy availability only |
| `SELL` | `2` | Check sell availability only |
| *(omitted)* | Both 1 and 2 | **Call Lotte twice**, return nested response |

**Implementation Logic:**

```
Parse query parameters from URL
  ↓
Start DRACC-031 (account balance/securities) in parallel with DRORD-028 flow
  - DRACC-031: account_no ← accountNumber, inquiry_date ← server date (trade day yyyymmdd), hts_user_id per Lotte/bridge convention
  ↓
IF sellBuyType is provided:
  - Call DRORD-028 once with specified type
  - Merge: flat availability + buyingPower from DRACC-031
  
ELSE (sellBuyType omitted):
  - Call DRORD-028 twice (BUY=1, SELL=2) — prefer Promise.all with DRACC-031
  - Aggregate buy/sell
  - Return nested response + root-level buyingPower
```

**Note:**
- Lotte APIs use POST with JSON body
- TradeX converts query params → Lotte JSON body
- `sellBuyType` empty string ("") treated as omitted (get both)
- **DRACC-031** chỉ phụ thuộc `accountNumber` (và ngày tra cứu), không phụ thuộc `symbol` / `sellBuyType`

**Request Example:**

**Case 1: Check single type (BUY only)**

TradeX Request:
```
GET /api/v1/derivatives/order/checkAvailability?accountNumber=0001234567&symbol=VN30F2402&sellBuyType=BUY
```

Lotte Request:
```json
{
  "acnt_no": "0001234567",
  "code": "VN30F2402",
  "sell_buy_type": 1
}
```

**Case 2: Check both types (omit sellBuyType)**

TradeX Request:
```
GET /api/v1/derivatives/order/checkAvailability?accountNumber=0001234567&symbol=VN30F2402
```

Lotte Requests (2 calls):
```json
// Call 1 - Check BUY
{
  "acnt_no": "0001234567",
  "code": "VN30F2402",
  "sell_buy_type": 1
}

// Call 2 - Check SELL
{
  "acnt_no": "0001234567",
  "code": "VN30F2402",
  "sell_buy_type": 2
}
```

### 3.3 Response Mapping

**Success (200) - Single Type:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `error_code` | - | - | Check = `"0000"` | Success indicator (DRORD-028) |
| `data_list.avail_order_qty` | `availableQuantity` | Number | String→Number | Số lượng có thể đặt |
| `data_list.avail_liq_qty` | `availableLiquidity` | Number | String→Number | Thanh khoản khả dụng |

**Success (200) - Buying power (DRACC-031, mọi response thành công):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `error_code` | - | - | Check = `"0000"` | Success indicator (DRACC-031) |
| `data_list.value_withdrawable_collateral_assets` | `buyingPower` | Number | String→Number | Giá trị TSKQ có thể rút (hiển thị nghiệp vụ dưới nhãn *Buying power* / sức mua tương ứng theo app) |

**Success (200) - Both Types:**

When `sellBuyType` is omitted, backend calls DRORD-028 twice and aggregates:

| Response Structure | Description |
|--------------------|-------------|
| `buy.availableQuantity` | Available quantity for BUY orders |
| `buy.availableLiquidity` | Market liquidity for BUY |
| `sell.availableQuantity` | Available quantity for SELL orders |
| `sell.availableLiquidity` | Market liquidity for SELL |
| `buyingPower` | Một field ở **root**, từ DRACC-031 (không lồng trong `buy`/`sell`) |

**Response Examples:**

**Case 1: Single Type Request**

Lotte Response:
```json
{
  "error_code": "0000",
  "error_desc": "Success",
  "success": true,
  "data_list": {
    "avail_order_qty": "100",
    "avail_liq_qty": "150"
  }
}
```

TradeX Response:
```json
{
  "availableQuantity": 100,
  "availableLiquidity": 150,
  "buyingPower": 125000000
}
```

**Case 2: Both Types Request**

Lotte Response 1 (BUY):
```json
{
  "error_code": "0000",
  "data_list": {
    "avail_order_qty": "100",
    "avail_liq_qty": "150"
  }
}
```

Lotte Response 2 (SELL):
```json
{
  "error_code": "0000",
  "data_list": {
    "avail_order_qty": "50",
    "avail_liq_qty": "200"
  }
}
```

TradeX Response (aggregated):
```json
{
  "buy": {
    "availableQuantity": 100,
    "availableLiquidity": 150
  },
  "sell": {
    "availableQuantity": 50,
    "availableLiquidity": 200
  },
  "buyingPower": 125000000
}
```

**Note:**
- Lotte returns strings, convert to numbers (DRORD-028 và DRACC-031)
- **Performance:** Both types = 2× DRORD-028 + 1× DRACC-031; chạy **DRACC-031 song song** với (các) DRORD-028 để latency gần max(2 DRORD, 1 DRACC) thay vì cộng tuần tự
- **Partial vs pass-through:** xử lý lỗi từng nhánh theo **§3.3.1** (không còn mở option “fail cả request hay null” — đã cố định trong bảng lớp A/B)

### 3.3.1 Composite corner cases (DRORD-028 + DRACC-031)

Hai nguồn độc lập. Mỗi response Lotte được xếp vào một trong hai **lớp**:

| Lớp | Ý nghĩa | Hành vi TradeX |
|-----|---------|----------------|
| **A — Không có data / không dùng được payload** | Timeout, lỗi network/bridge, body rỗng, thiếu `data_list`, `error_code === "0000"` nhưng thiếu field cần map hoặc không parse được số, v.v. — tức **không** có lỗi nghiệp vụ Core kèm `error_desc` theo lớp B. | **HTTP 200** nếu nhánh còn lại **không** ở lớp B. Phần dữ liệu từ API hỏng = **`null`**: single type → `availableQuantity` / `availableLiquidity` và/hoặc `buyingPower`; both types → `buy` hoặc `sell` = **`null`** nếu đúng một trong hai lần DRORD gặp A; `buyingPower` = **`null`** nếu DRACC gặp A. |
| **B — Core trả lỗi nghiệp vụ có message** | `error_code !== "0000"` **và** có `error_desc` từ Lotte cho nhánh đó. | **Pass-through** `error_desc` **nguyên văn**, **HTTP 422**, `code` theo §3.4 (DRORD: `ORDER_AVAILABILITY_{code}`; DRACC: `DERIVATIVES_ACCOUNT_BALANCE_{code}`). **Không** trả body success 200 cho request đó. |

**Cả hai nhánh đều lỗi B:** trả **một** response **422**, ưu tiên `message` / context từ **DRORD-028**; log đầy đủ cả DRACC.

**Both types (bỏ `sellBuyType`):** xét riêng từng lần DRORD (BUY / SELL). Một lần **B** → **422** pass-through từ lần đó (fail-fast khuyến nghị). Một lần **A** → `buy` hoặc `sell` tương ứng = **`null`**, lần kia map bình thường; `buyingPower` xử lý độc lập theo DRACC (A → `null` trong 200; B → **422** cho toàn request).

**Ví dụ nhanh**

| DRORD | DRACC | HTTP | TradeX (rút gọn) |
|-------|-------|------|------------------|
| OK | OK | 200 | Đủ field số |
| OK | A | 200 | Có qty/liquidity; `buyingPower: null` |
| A | OK | 200 | `availableQuantity` / `availableLiquidity` = `null` (single) hoặc `buy`/`sell` = `null` (both); có `buyingPower` |
| OK | B | 422 | `code`: `DERIVATIVES_ACCOUNT_BALANCE_{code}`, `message`: `error_desc` DRACC |
| B | * | 422 | `code`: `ORDER_AVAILABILITY_{code}`, `message`: `error_desc` DRORD |
| B | B | 422 | Một response; ưu tiên message/context **DRORD-028**; log cả DRACC |

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `symbol` | `FIELD_IS_REQUIRED` | `["symbol"]` | Missing |
| `sellBuyType` | `INVALID_VALUE` | `["sellBuyType", "value", "BUY/SELL"]` | Invalid value (if provided) |

**Auth Error (401):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `UNAUTHORIZED` | Token không hợp lệ hoặc đã hết hạn | Invalid token |
| `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Token expired |

**Auth Error (403):**

| Error Code | Message | Condition |
|------------|---------|-----------|
| `FORBIDDEN` | Không có quyền truy cập | No permission |
| `UNAUTHORIZED_ACCOUNT` | Tài khoản không thuộc quyền sở hữu của bạn | Account ownership check failed |

**Business Error (422) - Lotte Pass-Through:**

**Từ DRORD-028 (availability):**

| Lotte Code | TradeX Code | Description (VI) |
|------------|-------------|------------------|
| `1005` | `ORDER_AVAILABILITY_1005` | Tài khoản không hợp lệ hoặc bị khóa |
| `5002` | `ORDER_AVAILABILITY_5002` | Mã hợp đồng không hợp lệ |

*(Các mã khác từ DRORD: `ORDER_AVAILABILITY_{error_code}` — `message` luôn = `error_desc` Lotte nguyên văn.)*

**Từ DRACC-031 (buying power) — lớp B, §3.3.1:**

| Lotte `error_code` | TradeX Code | Ghi chú |
|--------------------|-------------|---------|
| *bất kỳ* | `DERIVATIVES_ACCOUNT_BALANCE_{error_code}` | `message` = `error_desc` AS-IS |

**Error Response Format:**

Lotte Error:
```json
{
  "error_code": "1005",
  "error_desc": "[V1005] Tài khoản không hợp lệ",
  "success": false
}
```

TradeX Response (422) — DRORD:
```json
{
  "code": "ORDER_AVAILABILITY_1005",
  "message": "[V1005] Tài khoản không hợp lệ"
}
```

TradeX Response (422) — DRACC (khi DRORD thành công, DRACC lớp B):
```json
{
  "code": "DERIVATIVES_ACCOUNT_BALANCE_1005",
  "message": "[V1005] …"
}
```

---

## 4. Use Cases

### 4.1 Use Case: Check Single Type (BUY only)

**Scenario:** User wants to buy VN30F2402, check max quantity

**Flow:**
```
1. User selects symbol: VN30F2402
2. User selects sell/buy type: BUY
3. System calls API /checkAvailability with sellBuyType=BUY
4. Display: "Max: 100 lots"
5. User enters quantity ≤ 100
6. Place order with confidence
```

**Request:**
```
GET /api/v1/derivatives/order/checkAvailability?accountNumber=0001234567&symbol=VN30F2402&sellBuyType=BUY
```

**Response:**
```json
{
  "availableQuantity": 100,
  "availableLiquidity": 150,
  "buyingPower": 125000000
}
```

**UI Display:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Đặt lệnh MUA VN30F2402

Buying power: 125,000,000 (theo label app)
Số lượng tối đa: 100 hợp đồng
Thanh khoản TT: 150 hợp đồng

[ Nhập số lượng: ____ ]  (Max: 100)

[Đặt lệnh]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 4.2 Use Case: Check Both Types (Optimized - 1 API Call)

**Scenario:** User opens order form, need to display both BUY and SELL max quantities

**Flow:**
```
1. User selects symbol: VN30F2402
2. System calls API once (without sellBuyType)
3. Get both BUY and SELL data
4. Display both max quantities
5. User switches between BUY/SELL tabs (no API call needed)
```

**Request:**
```
GET /api/v1/derivatives/order/checkAvailability?accountNumber=0001234567&symbol=VN30F2402
```

**Response:**
```json
{
  "buy": {
    "availableQuantity": 100,
    "availableLiquidity": 150
  },
  "sell": {
    "availableQuantity": 50,
    "availableLiquidity": 200
  },
  "buyingPower": 125000000
}
```

**UI Display:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Đặt lệnh VN30F2402

Buying power: 125,000,000
[MUA ✓]  [BÁN]

Số lượng tối đa: 100 hợp đồng
Thanh khoản TT: 150 hợp đồng

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

User clicks [BÁN] tab → No API call, just switch to:

[MUA]  [BÁN ✓]

Số lượng tối đa: 50 hợp đồng
Thanh khoản TT: 200 hợp đồng
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Benefits:**
- ✅ 1 API call instead of 2
- ✅ Instant tab switching (no loading)
- ✅ Better UX (no delay when changing BUY↔SELL)
- ✅ Reduced backend load

### 4.3 Use Case: Check Before Sell Order (Single Type)

**Scenario:** User wants to sell existing long position

**Request:**
```
GET /api/v1/derivatives/order/checkAvailability?accountNumber=0001234567&symbol=VN30F2402&sellBuyType=SELL
```

**Response:**
```json
{
  "availableQuantity": 50,
  "availableLiquidity": 200,
  "buyingPower": 125000000
}
```

**Interpretation:**
- Can sell up to 50 lots (current long position or margin for short)
- Market has good liquidity (200 lots)
- `buyingPower` reflects DRACC-031 `value_withdrawable_collateral_assets` (account-level, same as BUY case)

### 4.4 Use Case: Zero Available Quantity

**Scenario:** Account has no margin or position limit reached

**Request:**
```
GET /api/v1/derivatives/order/checkAvailability?accountNumber=0001234567&symbol=VN30F2402&sellBuyType=BUY
```

**Response:**
```json
{
  "availableQuantity": 0,
  "availableLiquidity": 150,
  "buyingPower": 125000000
}
```

**UI Display:**
```
⚠️ Không thể đặt lệnh
Lý do: Không đủ margin hoặc đạt hạn mức vị thế
Khả năng đặt lệnh: 0 hợp đồng

→ Vui lòng nộp thêm margin hoặc đóng vị thế hiện tại
```

### 4.5 Use Case: Real-Time Update

**Scenario:** Dynamic check when user changes symbol or sell/buy type

**Flow:**
```
User opens order form
  ↓
Select symbol: VN30F2402 → Call API (sellBuyType: BUY)
  ↓
Display: Max 100 lots
  ↓
User changes to SELL → Call API (sellBuyType: SELL)
  ↓
Display: Max 50 lots (updated)
  ↓
User enters quantity → Validate against max
  ↓
Place order
```

**Implementation:**
- Call on symbol change (without sellBuyType to get both)
- Call on sellBuyType change ONLY if not using "both types" approach
- Debounce to avoid excessive calls (300ms)
- Cache result for 30 seconds
- **Recommended:** Use "both types" approach (4.2) to eliminate sellBuyType change calls

---

## 5. Error Handling Summary

### 5.1 Error Response Format

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "accountNumber", "messageParams": [...] }
  ]
}
```

**Auth Error (401/403):**
```json
{
  "code": "UNAUTHORIZED" / "TOKEN_EXPIRED" / "FORBIDDEN" / "UNAUTHORIZED_ACCOUNT",
  "message": "Error message"
}
```

**Business Error (422) - Lotte Pass-Through (composite):**
```json
{
  "code": "ORDER_AVAILABILITY_{LOTTE_CODE}",
  "message": "[V1005] Lotte error message (DRORD)"
}
```
hoặc khi lỗi từ DRACC-031 (lớp B):
```json
{
  "code": "DERIVATIVES_ACCOUNT_BALANCE_{LOTTE_CODE}",
  "message": "Lotte error_desc AS-IS (DRACC)"
}
```

**Partial success (200) — lớp A:** một hoặc nhiều field **`null`** theo §3.3.1; không có `code`/`message` lỗi Lotte trong body success.

**Server Error (500):**
```json
{
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

### 5.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business (DRORD) | `ORDER_AVAILABILITY_{LOTTE_CODE}` | `ORDER_AVAILABILITY_1005` | 422 |
| Lotte Business (DRACC) | `DERIVATIVES_ACCOUNT_BALANCE_{LOTTE_CODE}` | `DERIVATIVES_ACCOUNT_BALANCE_1005` | 422 |
| Partial no-data (lớp A) | — | field(s) `null` trong body 200 | 200 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

### 5.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|------------------|
| `1005` | Tài khoản không hợp lệ hoặc bị khóa | Invalid or suspended account |
| `5002` | Mã hợp đồng không hợp lệ | Invalid symbol code |
| `4001` | Thị trường đã đóng | Market closed |

---

## 6. Implementation Notes

### 6.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `tuxedo` or `order-v2` | Process availability check, call Lotte |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| **Redis Cache** | Cache margin per contract (symbol_static.json) |

**Recommended Service:** `tuxedo` (pre-order validation fits order flow)

### 6.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, data types, account ownership
- Lotte calculates: Available quantity, liquidity
- NO duplicate business logic

**1b. Composite error handling (DRORD + DRACC):**
- Phân loại mỗi response Lotte vào **lớp A** (không có data / payload không dùng được) hoặc **lớp B** (`error_code` ≠ `0000` + `error_desc`) — chi tiết **§3.3.1**
- Merge vào một response TradeX: **A** → field tương ứng `null` trong **200**; **B** → **422** pass-through, không merge partial success

**2. Caching Strategy:**
- **Cache Key (Single Type):** `order_availability:{accountNumber}:{symbol}:{sellBuyType}`
- **Cache Key (Both Types):** `order_availability:{accountNumber}:{symbol}:both`
- **TTL:** 30 seconds (cùng entry cache có thể gồm `buyingPower` từ DRACC-031; hoặc cache tách `dracc031_balance:{accountNumber}:{inquiry_date}` với TTL tương tự rồi merge — tránh stale không đồng bộ giữa hai nguồn)
- **Invalidation:** After order placed, margin deposit/withdrawal, position closed
- **Rationale:** Balance and positions change frequently; `buyingPower` nhạy cảm tương tự margin — không cache lâu hơn availability nếu gộp một response

**3. Performance:**
- **Single Type:** max(DRORD-028, DRACC-031) khi song song + overhead TradeX (~250–600ms typ.)
- **Both Types:** max(2× DRORD-028 parallel, DRACC-031) + overhead — ưu tiên `Promise.all` cho cả DRACC-031 và hai DRORD-028
- **Optimization:** DRACC-031 luôn song song với luồng DRORD-028; không chờ tuần tự trừ khi bắt buộc dependency
- Pre-fetch on form open, debounce on symbol change
- **Trade-off:** Both types = slower but better UX (no tab switching delay)

**5. Error Pass-Through:**
- Pass Lotte `error_desc` AS-IS (including `[CODE]` prefix)
- TradeX NEVER transforms Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

### 6.3 Security Considerations

**Access Control:**
- User must own the account
- Validate `accountNumber` belongs to authenticated user
- Cannot check other users' accounts

**Validation Example:**
```typescript
const userAccounts = getUserAccounts(userId);
if (!userAccounts.includes(accountNumber)) {
  throw new ForbiddenError("UNAUTHORIZED_ACCOUNT");
}
```

### 6.4 Monitoring

**Key Metrics:**
- API call count (per user, per symbol)
- Success/error rate
- Latency (p50, p95, p99)
- Cache hit rate

**Alerts:**
- Error rate > 5%
- Latency > 1 second (p95)
- Lotte API down

---

## 7. Related APIs

### 7.1 Order Execution APIs

| API Code | Name | Relationship |
|----------|------|--------------|
| DRORD-029 | Lệnh Mua By User | Uses availability check before placing buy |
| DRORD-030 | Lệnh Bán By User | Uses availability check before placing sell |
| DRORD-031 | Lệnh Hủy By User | May affect available quantity |
| DRORD-032 | Lệnh Sửa By User | May affect available quantity |

### 7.2 Query APIs

| API Code | Name | Relationship |
|----------|------|--------------|
| DRORD-011 | Danh sách lệnh có thể hủy sửa | Shows pending orders affecting availability |

### 7.3 Account APIs

| API Code | Name | Relationship |
|----------|------|--------------|
| DRACC-014 | Thông tin tài khoản phái sinh | Shows margin, position data |
| **DRACC-031** | **Tra cứu tài sản & CK (balance/securities)** | **Nguồn `buyingPower` trong response checkAvailability** |
| DRACC-034 | Nộp tiền ký quỹ | Increases margin, affects availability |

---

## 8. Testing Scenarios

### 8.1 Functional Tests

| Test Case | Input | Expected Result |
|-----------|-------|-----------------|
| Valid BUY request | accountNumber, symbol, sellBuyType=BUY | Return availableQuantity > 0 |
| Valid SELL request | accountNumber, symbol, sellBuyType=SELL | Return availableQuantity (may differ from BUY) |
| Both types request | accountNumber, symbol (no sellBuyType) | Return nested { buy, sell, buyingPower } |
| buyingPower present | DRACC-031 lớp OK | Root `buyingPower` là number |
| DRACC lớp A, DRORD OK | Timeout / thiếu data DRACC | 200, `buyingPower: null`, qty/liquidity bình thường |
| DRACC lớp B, DRORD OK | `error_code` ≠ 0000 + `error_desc` DRACC | 422, `DERIVATIVES_ACCOUNT_BALANCE_{code}`, message AS-IS |
| DRORD lớp A, DRACC OK | Thiếu data một lần DRORD (both types) | 200, `buy` hoặc `sell` = `null`, phía còn lại + `buyingPower` OK |
| DRORD lớp B | Core từ chối availability | 422, `ORDER_AVAILABILITY_{code}`, message AS-IS |
| Cả DRORD B và DRACC B | Cả hai Core lỗi B | 422, ưu tiên DRORD trong `message`/`code` (§3.3.1) |
| Missing accountNumber | No accountNumber | 400 FIELD_IS_REQUIRED |
| Invalid sellBuyType | sellBuyType="INVALID" | 400 INVALID_VALUE |
| Invalid account | Not user's account | 403 UNAUTHORIZED_ACCOUNT |
| Invalid symbol | symbol="INVALID" | 422 ORDER_AVAILABILITY_5002 |
| Zero margin | Insufficient margin | availableQuantity=0 |

### 8.2 Performance Tests

| Test Case | Expected |
|-----------|----------|
| Single type API latency | < 550ms (p95) |
| Both types API latency | < 1050ms (p95) |
| Cache hit rate | > 70% |
| Concurrent requests | 100 req/s |

### 8.3 Integration Tests

| Test Case | Description |
|-----------|-------------|
| End-to-end flow | Check availability → Place order → Verify success |
| Cache invalidation | Place order → Check availability → Verify updated |
| Real-time update | Change symbol → Check availability → Verify correct data |

---

## 9. Implementation Checklist

### 9.1 Backend (BE Team)

- [ ] Create TradeX API endpoint: `GET /api/v1/derivatives/order/checkAvailability`
- [ ] Implement request mapping (query params → Lotte JSON body)
- [ ] Implement response mapping (Lotte → TradeX), gồm **DRACC-031 → `buyingPower`**
- [ ] Gọi **DRACC-031** mỗi request (hoặc merge từ cache đồng bộ TTL với entry availability)
- [ ] Handle optional `sellBuyType` parameter
- [ ] Implement "both types" logic (call Lotte twice when sellBuyType omitted)
- [ ] Use `Promise.all()` for parallel Lotte calls (DRACC-031 + DRORD-028 ×1 hoặc ×2)
- [ ] Implement caching (30s TTL, different keys for single/both)
- [ ] Add validation (required fields, account ownership)
- [ ] Implement error handling: **§3.3.1** (lớp A → `null`, lớp B → 422 pass-through; prefix `ORDER_AVAILABILITY_*` vs `DERIVATIVES_ACCOUNT_BALANCE_*`)
- [ ] Add monitoring (metrics, alerts)
- [ ] Write unit tests
- [ ] Write integration tests

### 9.2 Frontend (FE Team)

**Option A: Single Type Approach (Original)**
- [ ] Add API call on order form load with specific sellBuyType
- [ ] Display "Max Quantity" on UI
- [ ] Update max on symbol/sellBuyType change (debounced)
- [ ] Validate user input against max

**Option B: Both Types Approach (Recommended)**
- [ ] Add API call on order form load WITHOUT sellBuyType
- [ ] Parse nested response { buy: {...}, sell: {...} }
- [ ] Store both BUY and SELL data locally
- [ ] Display appropriate max based on current tab (no API call on tab switch)
- [ ] Only re-call API when symbol changes
- [ ] Validate user input against appropriate max

**Common Tasks:**
- [ ] Hiển thị **buyingPower** từ cùng response (format số tiền theo locale)
- [ ] **200 + `null`:** ẩn hoặc placeholder khi `buyingPower` / `availableQuantity` / `buy`/`sell` là `null` (lớp A — không có data)
- [ ] **422:** hiển thị `message` pass-through; phân biệt `code` prefix `ORDER_AVAILABILITY_*` (DRORD) vs `DERIVATIVES_ACCOUNT_BALANCE_*` (DRACC) nếu cần copy/telemetry
- [ ] Show liquidity indicator (optional: "High/Medium/Low" based on availableLiquidity)
- [ ] Handle zero availability (display warning)
- [ ] Error handling (user-friendly messages)
- [ ] Loading state during API call

### 9.3 QA Team

- [ ] Test single type requests (BUY/SELL separately)
- [ ] Test both types request (omit sellBuyType)
- [ ] Test validation errors (missing fields, invalid values)
- [ ] Test auth errors (invalid token, wrong account)
- [ ] Test Lotte errors (invalid symbol, account)
- [ ] Test composite §3.3.1: lớp A (null partial), lớp B DRORD, lớp B DRACC, cả hai B
- [ ] Test zero availability scenario
- [ ] Test cache behavior (30s TTL)
- [ ] Test real-time update (symbol change)
- [ ] Performance test single type (latency < 550ms)
- [ ] Performance test both types (latency < 1050ms)
- [ ] Load test (100 req/s)

### 9.4 DevOps Team

- [ ] Configure monitoring dashboard
- [ ] Set up alerts (error rate, latency)
- [ ] Configure Redis cache
- [ ] Review security (account access control)

---

**Document Status:** 🔄 Updated v1.2 — composite + corner cases lớp A/B (null vs pass-through)  
**For:** BA/Dev  
**Next Steps:** BE implement phân lớp A/B và mã `DERIVATIVES_ACCOUNT_BALANCE_*`; FE xử lý `null` vs 422  
**Estimated Effort:** +0.5–1 day (BE) so với baseline v1.0; FE +0.5 day
