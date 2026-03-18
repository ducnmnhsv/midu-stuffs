# TradeX Derivatives API Conventions

**Document Type:** Technical Conventions  
**Category:** Derivatives - All APIs  
**Audience:** Backend Developers, PM  
**Date:** February 4, 2026  
**Version:** 1.0

---

## Table of Contents

1. [Overview](#overview)
2. [Validation Strategy](#validation-strategy)
3. [Auto-Populated Fields](#auto-populated-fields)
4. [Standard Error Formats](#standard-error-formats)
5. [Language Handling](#language-handling)
6. [Common Request Fields](#common-request-fields)
7. [Response Format Standards](#response-format-standards)

---

## Overview

### Purpose

Tài liệu này định nghĩa các conventions chung cho **tất cả API Derivatives** của TradeX, đảm bảo:

- ✅ **Consistent behavior** giữa Equity và Derivatives
- ✅ **Minimal FE changes** khi migrate từ Equity sang Derivatives
- ✅ **Clear error handling** patterns
- ✅ **Transparent field mapping** giữa TradeX ↔ Lotte
- ✅ **Light validation** - Core is single source of truth

### Scope

Convention này áp dụng cho:

- Order APIs (Regular, Conditional)
- Account APIs (Portfolio, Balance, Margin)
- Market Data APIs (Quote, Chart, Symbol Info)
- **Future Derivatives APIs**

---

## Validation Strategy

### Philosophy: Light Validation at TradeX, Core Validates Business Rules

**Principle:** TradeX only validates **basic requirements**, Core (Lotte) validates **all business rules**.

```
┌─────────────────────────────────────────────────────────────┐
│  TradeX Validation (Light)                                   │
│  - Required fields present?                                  │
│  - Data types correct?                                       │
│  - Basic format valid?                                       │
│  → If YES: Forward to Core                                   │
│  → If NO: Return 400 INVALID_PARAMETER                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Core Validation (Complete)                                  │
│  - Price within trading range?                               │
│  - Sufficient margin?                                        │
│  - Market open?                                              │
│  - Account has permission?                                   │
│  - All business logic                                        │
│  → If YES: Execute order                                     │
│  → If NO: Return 422 with error code                         │
└─────────────────────────────────────────────────────────────┘
```

### Why This Approach?

**✅ Advantages:**

1. **Single Source of Truth**
   - Core owns all business rules
   - No duplicate validation logic
   - Rules change? Core handles it automatically

2. **Consistency with Equity**
   - Equity already works this way
   - Proven pattern in production
   - FE behavior unchanged

3. **Less Maintenance**
   - One place to update rules (Core)
   - TradeX code stays simple
   - Fewer bugs from sync issues

4. **Flexibility**
   - Core can add new rules anytime
   - TradeX doesn't need redeployment
   - Business rules evolve independently

**⚠️ Trade-offs:**

- Extra network call for invalid business rules
- Slightly slower error response
- **But:** These are acceptable given the benefits

### What TradeX Should Validate

| Category | Validate? | Examples | Rationale |
|----------|-----------|----------|-----------|
| **Required Fields** | ✅ YES | `accountNumber`, `orderQuantity`, `orderPrice` | Basic request integrity |
| **Data Types** | ✅ YES | `orderPrice` is number, not string | Prevent runtime errors |
| **Basic Format** | ✅ YES | Email format, phone format | Common validation patterns |
| **Length Limits** | ✅ YES | `accountNumber` ≤ 10 chars | Prevent buffer overflow |
| **Null/Empty Check** | ✅ YES | Field is not empty string | Basic data quality |
| | | | |
| **Price Ranges** | ❌ NO | Price within floor/ceiling | **Core validates** |
| **Quantity Limits** | ❌ NO | Max 1000 contracts | **Core validates** |
| **Margin Requirements** | ❌ NO | Sufficient buying power | **Core validates** |
| **Trading Hours** | ❌ NO | Market open/closed | **Core validates** |
| **Account Permissions** | ❌ NO | User owns account | **Core validates** |
| **Business Logic** | ❌ NO | Any domain-specific rules | **Core validates** |

### Code Examples

#### ✅ DO: Validate Required Fields

```typescript
// Good: Check if required field is present
if (!request.accountNumber) {
  throw new InvalidParameterError()
    .add('FIELD_IS_REQUIRED', 'accountNumber', ['accountNumber']);
}

if (!request.orderQuantity || request.orderQuantity <= 0) {
  throw new InvalidParameterError()
    .add('FIELD_IS_REQUIRED', 'orderQuantity', ['orderQuantity']);
}
```

#### ✅ DO: Validate Data Types

```typescript
// Good: Check data type
if (typeof request.orderPrice !== 'number') {
  throw new InvalidParameterError()
    .add('INVALID_TYPE', 'orderPrice', ['orderPrice', 'number']);
}
```

#### ❌ DON'T: Validate Business Rules

```typescript
// Bad: Don't validate price range in TradeX
if (request.orderPrice < floorPrice || request.orderPrice > ceilingPrice) {
  throw new Error('PRICE_OUT_OF_RANGE'); // ❌ Don't do this
}
// Instead: Let Core validate and return error

// Bad: Don't validate margin in TradeX
if (userMargin < requiredMargin) {
  throw new Error('INSUFFICIENT_MARGIN'); // ❌ Don't do this
}
// Instead: Let Core validate and return error
```

### Real-World Example: Order Placement

```typescript
// TradeX Service (lotte-bridge)
async placeOrder(request: OrderRequest) {
  // ✅ Validate only required fields
  if (!request.accountNumber) {
    throw new InvalidParameterError()
      .add('FIELD_IS_REQUIRED', 'accountNumber', ['accountNumber']);
  }
  
  if (!request.orderQuantity) {
    throw new InvalidParameterError()
      .add('FIELD_IS_REQUIRED', 'orderQuantity', ['orderQuantity']);
  }
  
  // ✅ Forward to Core (Core validates business rules)
  const lotteResponse = await this.callLotteAPI({
    acnt_no: request.accountNumber,
    ord_qty: request.orderQuantity,
    ord_price: request.orderPrice,
    // ... other fields
  });
  
  // ✅ Handle Core's business validation errors
  if (lotteResponse.error_code !== '0000') {
    // Pass-through Core's error
    throw new GeneralError(`ORDER_PLACE_${lotteResponse.error_code}`, 
                          lotteResponse.error_desc);
  }
  
  return lotteResponse.data;
}
```

### Testing Strategy

**TradeX Tests:**
```typescript
// Test required field validation
it('should return 400 when accountNumber is missing', async () => {
  const request = { orderQuantity: 5, orderPrice: 1250.0 };
  // Missing accountNumber
  
  const response = await api.post('/api/v1/derivatives/order', request);
  
  expect(response.status).toBe(400);
  expect(response.body.code).toBe('INVALID_PARAMETER');
  expect(response.body.params[0].code).toBe('FIELD_IS_REQUIRED');
});

// ❌ Don't test business rules in TradeX
// Business rule tests belong in Core/integration tests
```

**Core/Integration Tests:**
```typescript
// Test business rule validation
it('should return 422 when price exceeds ceiling', async () => {
  const request = {
    accountNumber: '0001234567',
    orderQuantity: 5,
    orderPrice: 9999.0, // Way above ceiling
    // ... other fields
  };
  
  const response = await api.post('/api/v1/derivatives/order', request);
  
  expect(response.status).toBe(422);
  expect(response.body.code).toMatch(/ORDER_PLACE_\d+/);
  expect(response.body.message).toContain('giá trần'); // Core's message
});
```

---

## Auto-Populated Fields

### 1. Fields FE KHÔNG Cần Truyền

Các field sau được **tự động điền** bởi backend, FE **KHÔNG** cần truyền:

| TradeX Field | Lotte Field | Source | Description |
|--------------|-------------|--------|-------------|
| `sourceIp` | `cli_ip_addr` | Request IP | IP address của client |
| `userId` | `user_id` / `hts_user_nm` | JWT Token | Username từ token |
| `sessionId` | Varies | JWT Token | Session ID |
| `identifierNumber` | `idno` | JWT Token | CMND/CCCD |
| `name` | `hts_user_nm` | JWT Token (userData) | Tên người dùng |
| `mdm_tp` | `mdm_tp` | **Derived** from platform/channel | Kênh thực hiện – xem §1.1 |

#### 1.1 mdm_tp (Kênh thực hiện) – Derived, FE KHÔNG truyền

Khi Lotte yêu cầu field `mdm_tp`, TradeX **derive** từ `platform`/`channel` – **client KHÔNG gửi mdm_tp trực tiếp**.

| Source (priority) | Description |
|-------------------|-------------|
| `request.channel` | Client có thể gửi trong body (optional) |
| `request.headers.token.platform` | Platform lưu trong JWT (từ login) |
| Default | `platformDifiSoft` khi client_credentials |

**Mapping (lotte-bridge config):** `getPlatformValueCore(platform)` → `config.platform[platform]`:

| platform | mdm_tp |
|----------|--------|
| `NHSV_MTS_IOS` | 31 |
| `NHSV_MTS_ANDROID` | 32 |
| `FINTECH(DIFISOFT)`, `PAAVE.*`, `M.PAAVE.*` | 42 |
| Không map được | `%` (default) |

**Áp dụng:** Equity order, Derivatives order (Stop, Regular nếu Lotte yêu cầu), và mọi API Lotte cần `mdm_tp`.

### 2. Implementation Pattern

#### Backend Service: `rest-proxy`

**File:** `src/app/middlewares/message-handler.ts`

```typescript
// Auto-populate sourceIp from request
const ip = first([
  ctx.req.headers['x-forwarded-for'],
  ctx.req.headers['x-real-ip'],
  first(ctx.req.connection.remoteAddress),
]);
if (ip != null) {
  if (!checkIfValidIPV6(ip)) {
    body.sourceIp = ip.replace(/^.*:/, ''); // Remove IPv6 prefix
  }
  body.sourceIp = ip;
}
```

#### Backend Service: `lotte-bridge`

**File:** `src/services/OrderService.ts`

```typescript
// Map TradeX fields to Lotte fields
const lotteRequest = {
  user_id: request.headers.token.userId,
  hts_user_nm: setDefault<string>(request.name, request.headers.token.userData['name']),
  idno: request.headers.token.userData.identifierNumber,
  acnt_no: request.accountNumber.toUpperCase(),
  cli_ip_addr: request.sourceIp, // Auto-populated by rest-proxy
  cli_mac_addr: request.deviceUniqueId, // FE must provide
  lang_code: LOTTE_LANG_CODE[language] || config.defaultLanguage,
  // ... other fields
};
```

### 3. Fields FE VẪN PHẢI Truyền

| TradeX Field | Lotte Field | Required | Description |
|--------------|-------------|----------|-------------|
| `deviceUniqueId` | `cli_mac_addr` | **Yes** | Device MAC address / unique ID |
| `accountNumber` | `acnt_no` | **Yes** | Số tài khoản (10 chars) |
| `subNumber` | `sub_no` | **Yes** (Equity) | Số tiểu khoản (Equity only) |

**Why deviceUniqueId must be sent by FE?**

- Device-specific identifier (MAC address, UUID)
- Cannot be determined from server side
- Used for audit trail and security

---

## Standard Error Formats

### 1. Validation Errors (400)

**Error Code:** `INVALID_PARAMETER`

**Use Case:** **ONLY** for missing required fields or basic format validation

⚠️ **Important Principle:**
- ✅ **Validate:** Required fields, data types, basic format (email, phone, etc.)
- ❌ **Don't validate:** Business rules (price range, quantity limits, margin requirements)
- 🎯 **Why:** Core is single source of truth for business rules

**Rationale:**
```
TradeX validates:     Core validates:
- accountNumber       - Price within trading range
- orderQuantity       - Sufficient margin
- orderPrice          - Order quantity limits
- deviceUniqueId      - Market status (open/closed)
                      - Account permissions
                      - All business logic
```

**Benefits:**
- ✅ Single source of truth (Core)
- ✅ No duplicate logic
- ✅ Auto-sync when Core rules change
- ✅ Consistent with Equity behavior

#### Pattern

```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "accountNumber",
      "messageParams": ["accountNumber"]
    },
    {
      "code": "INVALID_VALUE",
      "param": "orderPrice",
      "messageParams": ["orderPrice", "1000.0", "0.0-999.9"]
    }
  ]
}
```

**Note:** NO `success` or `message` field - FE constructs message from code + messageParams

#### Implementation (Java)

**File:** `order-v2/src/main/java/*/services/impl/StopOrderServiceImpl.java`

```java
import com.techx.tradex.common.exceptions.SubErrorsException;
import com.techx.tradex.order.constants.Constants;

// ✅ DO: Validate required fields
if (!StringUtils.isNotBlank(request.getDeviceUniqueId())) {
    throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
            .add(Constants.FIELD_IS_REQUIRED, "deviceUniqueId", 
                 Collections.singletonList("deviceUniqueId"));
}

// ✅ DO: Validate data type
if (request.getOrderQuantity() == null) {
    throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
            .add(Constants.FIELD_IS_REQUIRED, "orderQuantity",
                 Collections.singletonList("orderQuantity"));
}

// ❌ DON'T: Validate business rules (let Core handle)
// BAD EXAMPLE (don't do this):
// if (request.getOrderPrice() > maxPrice) {
//     throw new SubErrorsException("PRICE_OUT_OF_RANGE");
// }
```

#### Implementation (TypeScript/Node.js)

**File:** `lotte-bridge/src/consumers/RequestHandler.ts`

```typescript
import * as Errors from '../errors';

// ✅ DO: Validate required fields
const accountNumber = data?.accountNumber?.toUpperCase();
if (Utils.isEmpty(accountNumber)) {
  new Errors.InvalidParameterError()
    .add('FIELD_IS_REQUIRED', 'accountNumber', ['accountNumber'])
    .throwErr();
}

// ✅ DO: Validate basic format
if (data.deviceUniqueId && data.deviceUniqueId.length > 50) {
  new Errors.InvalidParameterError()
    .add('INVALID_FORMAT', 'deviceUniqueId', ['deviceUniqueId', '50'])
    .throwErr();
}

// ❌ DON'T: Validate business rules (let Core handle)
// BAD EXAMPLE (don't do this):
// if (data.orderPrice < minPrice || data.orderPrice > maxPrice) {
//   throw new Error('PRICE_OUT_OF_RANGE');
// }
```

**What to Validate in TradeX:**

| Category | Validate? | Example | Reason |
|----------|-----------|---------|--------|
| Required fields | ✅ YES | `accountNumber`, `orderQuantity` | Basic request integrity |
| Data types | ✅ YES | `orderPrice` is number | Prevent type errors |
| Basic format | ✅ YES | Email format, phone format | Common validation |
| Length limits | ✅ YES | `accountNumber` max 10 chars | Prevent buffer issues |
| **Business rules** | ❌ NO | Price range, quantity limits | **Core validates** |
| **Market rules** | ❌ NO | Trading hours, margin | **Core validates** |
| **User permissions** | ❌ NO | Account ownership | **Core validates** |

### 2. Pass-Through Core Errors

**Use Case:** Lỗi từ Lotte API (business rules, insufficient balance, market closed, etc.)

#### Lotte Response Format

**Success:**
```json
{
  "error_code": "0000",
  "error_desc": "",
  "data_list": [...]
}
```

**Error:**
```json
{
  "error_code": "1005",
  "error_desc": "[V3120] Lỗi đã xảy ra"
}
```

**error_desc Format:** `[{INTERNAL_CODE}] {Error Message}`
- `INTERNAL_CODE`: Lotte internal code (e.g., V3120, V0123)
- `Error Message`: User-facing message in selected language

#### TradeX Response Pattern

**When `error_code != "0000"`:**

```json
{
  "code": "ORDER_PLACE_1005",
  "message": "[V3120] Lỗi đã xảy ra"
}
```

**Format:**
- `code`: `{OPERATION}_{LOTTE_ERROR_CODE}` (e.g., `ORDER_PLACE_1005`, `TRANSFER_CASH_1234`)
- `message`: `error_desc` từ Lotte response (pass-through as-is, including `[CODE]` prefix)

#### Implementation

**File:** `lotte-bridge/src/services/OrderService.ts`

```typescript
// Parse Lotte response
const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);

// Check success: error_code = "0000" OR specific success codes
if (codes === null || codes === '0307' || codes === '0305') {
  return {
    message: lotteRes.error_desc,
    orderNumber: lotteResDataList.new_ord_no,
  };
}

// Pass-through error with operation prefix
throw new GeneralError(`${Constants.ORDER_PLACE}${codes}`);
// Result: "ORDER_PLACE_1005" if lotteRes.error_code = "1005"
```

**Alternative Pattern (Direct check):**

```typescript
// Direct error_code check (common in other services)
if (lotteRes.error_code === '0000') {
  // Success - process data_list
  return processSuccessData(lotteRes.data_list);
} else {
  // Error - pass through
  throw new GeneralError(lotteRes.error_desc);
  // OR with operation prefix:
  // throw new GeneralError(`${Constants.OPERATION}${lotteRes.error_code}`);
}
```

#### Operation Prefixes

| Operation | Constant | Prefix | Example Error Code |
|-----------|----------|--------|-------------------|
| Place Order | `ORDER_PLACE` | `ORDER_PLACE_` | `ORDER_PLACE_1005` |
| Cancel Order | `ORDER_CANCEL` | `ORDER_CANCEL_` | `ORDER_CANCEL_1234` |
| Modify Order | `ORDER_MODIFY` | `ORDER_MODIFY_` | `ORDER_MODIFY_5678` |
| Transfer Cash | `TRANSFER_CASH` | `TRANSFER_CASH_` | `TRANSFER_CASH_9999` |
| Transfer Stock | `TRANSFER_STOCK` | `TRANSFER_STOCK_` | `TRANSFER_STOCK_1111` |

#### Error Handling Notes

**✅ Current Behavior:**
- `error_desc` is passed through as-is (including `[CODE]` prefix)
- FE receives original Lotte error message
- Works well when `lang_code` matches user's language preference

**⚠️ Limitations:**
1. **Message depends on `lang_code`** - If wrong language sent to Lotte, FE gets message in wrong language
2. **[CODE] prefix visible to users** - May confuse non-technical users
3. **No custom handling** - Cannot customize messages for specific business contexts

**📋 Future Improvements (Optional):**
1. Parse and remove `[CODE]` prefix from `error_desc`
2. Map common `error_code` values to friendly messages
3. Add TradeX-specific error codes for better tracking

---

## Language Handling

### 1. Language Header

**Header:** `Accept-Language`

**Values:** `vi` (default), `en`, `ko`

### 2. Lotte Mapping

| Accept-Language | Lotte lang_code | Description |
|-----------------|-----------------|-------------|
| `vi` | `V` | Tiếng Việt |
| `en` | `E` | English |
| `ko` | `K` | 한국어 |
| *(empty)* | `V` | Default: Tiếng Việt |

### 3. Implementation

```typescript
const language = ctx.req.headers['accept-language'];
const lang_code = 
  language == null || LOTTE_LANG_CODE[language] == null 
    ? config.defaultLanguage  // 'V'
    : LOTTE_LANG_CODE[language];
```

---

## Common Request Fields

### 1. Derivatives vs Equity Differences

| Field | Equity | Derivatives | Notes |
|-------|--------|-------------|-------|
| `accountNumber` | ✅ Required | ✅ Required | 10 chars |
| `subNumber` | ✅ Required | ❌ N/A | Derivatives không có sub-account |
| `code` / `stockCode` | Stock symbol | Contract code | `SSI` vs `VN30F2502` |
| `orderPrice` | Stock price | Futures price | Different tick sizes |
| `orderQuantity` | Shares | Contracts | Different units |

### 2. Universal Fields (Both Equity & Derivatives)

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `accountNumber` | String | **Yes** | Số tài khoản |
| `deviceUniqueId` | String | **Yes** | Device ID |
| `sellBuyType` | String | **Yes** | `BUY` / `SELL` |
| `orderType` | String | **Yes** | `LO`, `ATO`, `ATC`, etc. |
| `orderQuantity` | Number | **Yes** | Số lượng (shares/contracts) |
| `orderPrice` | Number | **Yes*** | Giá đặt (*not for MOK/MAK) |

### 3. GET API optional parameters (TradeX → Core)

Đối với **các API có method = GET** (ví dụ Order History, Cash Statement, Asset list…), TradeX có **2 query parameter optional** (cả hai **không required** từ TradeX):

| TradeX (query param) | Type | Required | Core (Lotte) field | Description |
|---------------------|------|----------|---------------------|-------------|
| `fetchCount` | Number | ❌ No | `row_count` | Số bản ghi mỗi trang; không truyền thì không gửi sang Core hoặc dùng default Core |
| `nextKey` | String | ❌ No | `next_data` (hoặc `next_key` tùy API Lotte) | Pagination token; trang đầu không gửi hoặc ""; trang sau = giá trị trả về lần trước |

**Mapping khi gọi Core:**

- **fetchCount** → **row_count** (chỉ gửi sang Core khi client truyền `fetchCount`; không truyền thì không gửi hoặc để Core dùng default).
- **nextKey** → **next_data** (hoặc `next_key` theo đặc tả từng API Lotte). Trang đầu: "" hoặc khoảng trắng; trang tiếp: giá trị từ response lần trước (vd. `os_next_key` / `next_key`).

**Lưu ý:** Cả hai field **không required** từ phía TradeX — client có thể bỏ qua; khi đó TradeX không gửi `row_count` (hoặc gửi default) và gửi `next_data` rỗng cho trang đầu.

### 4. TradeX naming cho request/query parameters — không dùng tên hoặc mã Core

**Nguyên tắc:** TradeX API **phải** dùng **tên tham số và tập giá trị do TradeX định nghĩa**, dễ hiểu với client (PM, FE). **Không** dùng trực tiếp tên field hoặc mã số của Core (Lotte) làm tên/giá trị tham số TradeX.

| ❌ Sai | ✅ Đúng |
|--------|--------|
| Query param `sent` với giá trị `0`, `1`, `2` (theo Lotte) | TradeX param `orderSendFilter` với giá trị `ALL`, `SENT`, `PENDING`; mapping: ALL→0, SENT→1, PENDING→2 |
| Param `sell_buy_tp` (tên Lotte) | TradeX param `sellBuyType`: `ALL`, `BUY`, `SELL`; mapping sang Lotte `sell_buy_tp` (0/1/2) |
| Mô tả "0=all, 1=đã gửi, 2=chưa gửi" không kèm tên TradeX | Định nghĩa TradeX (tên + giá trị có nghĩa), sau đó bảng mapping TradeX → Core |

**Quy tắc:**

1. **Tên tham số:** Dùng camelCase, tên có nghĩa (ví dụ `orderSendFilter`, `sellBuyType`). Không dùng tên Core (vd. `sent`, `sell_buy_tp`, `ctr_cd`) làm tên param TradeX.
2. **Giá trị (enum):** Dùng giá trị có nghĩa (vd. `ALL`, `SENT`, `PENDING`, `BUY`, `SELL`). Không expose mã số Core (0, 1, 2) làm giá trị hợp lệ của API TradeX.
3. **Mapping bắt buộc:** Mỗi param TradeX (và mỗi giá trị) phải có **bảng mapping rõ ràng** sang Core: **TradeX parameter name** → **Core field name**, **TradeX value** → **Core value**. Ví dụ:

| TradeX parameter | TradeX values (có nghĩa) | Core (Lotte) field | Core values |
|------------------|---------------------------|----------------------|-------------|
| `orderSendFilter` | `ALL`, `SENT`, `PENDING` | `sent` | `0`, `1`, `2` |
| `sellBuyType` | `ALL`, `BUY`, `SELL` | `sell_buy_tp` | `0`, `1`, `2` |

**Áp dụng:** Mọi API spec (Order, Asset, Cash, …) khi có filter/option trùng với field Core phải định nghĩa tên và giá trị TradeX riêng, rồi mới map sang Core trong phần Request Mapping.

---

## Response Format Standards

> **IMPORTANT:** TradeX uses HTTP status code to indicate success/failure.  
> **NEVER** use `success: true/false` or `code: "0000"` fields in response body.

### 1. Success Response (200)

**Lotte-integrated (mutation):**
```json
{
  "message": "[V0307] Bạn đã thực hiện lệnh Mua...",
  "orderNumber": "123"
}
```

**TradeX-native (mutation):**
```json
{
  "id": 255
}
```

**Query APIs:**
```json
{
  "totalCount": 10,
  "orders": [ ... ]
}
```

**Key Principle:** HTTP 200 = Success, NO `success: true` or `code: "0000"` needed

### 2. Error Response Structure

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "accountNumber",
      "messageParams": ["accountNumber"]
    }
  ]
}
```

**Note:** NO `success: false` or `message` field - FE constructs from code + messageParams

**Auth Error (401/403):**
```json
{
  "code": "UNAUTHORIZED",
  "message": "Token không hợp lệ hoặc đã hết hạn"
}
```

**Business Error (422) - Lotte pass-through:**
```json
{
  "code": "ORDER_PLACE_1005",
  "message": "[V3120] Không đủ ký quỹ"
}
```

**Business Error (422) - TradeX-native:**
```json
{
  "code": "TP_PRICE_MUST_BE_HIGHER_THAN_ENTRY",
  "params": [],
  "messageParams": ["1250.0", "1200.0"]
}
```

### 3. HTTP Status Codes

| Status | Use Case | TradeX Code |
|--------|----------|-------------|
| `200` | Success | `0000` |
| `400` | Validation error (missing/invalid fields) | `INVALID_PARAMETER` |
| `401` | Authentication failed (invalid/expired token) | `UNAUTHORIZED`, `TOKEN_EXPIRED` |
| `403` | Authorization failed (insufficient permissions) | `FORBIDDEN` |
| `404` | Resource not found | `OBJECT_NOT_FOUND` |
| `422` | Business rule violation (from Lotte) | `{OPERATION}_{LOTTE_ERROR_CODE}` |
| `500` | Server/Lotte API error | `INTERNAL_SERVER_ERROR` |

### 4. Complete Error Examples

#### Example 1: Missing Required Field

**Request:**
```json
POST /api/v1/derivatives/order
{
  "code": "VN30F2502",
  "orderQuantity": 5,
  "orderPrice": 1285.5
  // Missing: accountNumber, orderType, sellBuyType, deviceUniqueId
}
```

**Response (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "accountNumber",
      "messageParams": ["accountNumber"]
    },
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "orderType",
      "messageParams": ["orderType"]
    }
  ]
}
```

#### Example 2: Lotte Business Rule Error

**Lotte API Response:**
```json
{
  "error_code": "1005",
  "error_desc": "[V3120] Không đủ tiền ký quỹ để thực hiện giao dịch",
  "data_list": []
}
```

**TradeX Response to Client (422):**
```json
{
  "code": "ORDER_PLACE_1005",
  "message": "[V3120] Không đủ tiền ký quỹ để thực hiện giao dịch"
}
```

#### Example 3: Success Response

**Lotte API Response:**
```json
{
  "error_code": "0000",
  "error_desc": "",
  "data_list": [
    {
      "new_ord_no": "2026020400123",
      "ft_code": "VN30F2502"
    }
  ]
}
```

**TradeX Response to Client (200):**
```json
{
  "message": "[V0307] Bạn đã thực hiện lệnh Mua. Hãy kiểm tra Trạng thái lệnh!",
  "orderNumber": "2026020400123"
}
```

**Note:** 
- Simple response with only message (from Lotte) and orderNumber
- NO `success`, `code`, or rich `data` wrapper
- FE queries `/todayUnmatch` for full order details if needed

#### Example 4: Token Expired

**Response (401):**
```json
{
  "code": "TOKEN_EXPIRED",
  "message": "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"
}
```

### 5. Common Lotte Error Codes

**Reference from implementation:**

| error_code | Common Scenarios | Note |
|------------|------------------|------|
| `0000` | ✅ Success | Standard success code |
| `1005` | Account/permission errors | e.g., "[V3120] Lỗi đã xảy ra" |
| `0307` | Order placed (specific case) | Treated as success |
| `0305` | Order placed (specific case) | Treated as success |
| `0320` | Order cancelled successfully | Success code for cancel |
| `0318` | Order modified successfully | Success code for modify |
| `0011` | Query successful | Success for list queries |
| `2016` | No data found | Not an error, empty result |

**Note:** Full error code mapping should be documented based on Lotte API specifications. Above codes are extracted from TradeX implementation patterns.

---

## Implementation Checklist

### For New Derivatives APIs

- [ ] **Auto-populate fields** (`sourceIp`, `userId`, etc.) in `rest-proxy`
- [ ] **Map to Lotte fields** correctly in `lotte-bridge`
- [ ] **Validate required fields** with `INVALID_PARAMETER` + `FIELD_IS_REQUIRED`
- [ ] **Pass-through Core errors** with `{OPERATION}_{CODE}` format
- [ ] **Handle language** mapping `vi/en/ko` → `V/E/K`
- [ ] **Return standard format** (`success`, `code`, `message`, `data`/`params`)
- [ ] **Document field differences** between Equity/Derivatives in spec

### Testing Checklist

- [ ] Test missing required field → `INVALID_PARAMETER` with `FIELD_IS_REQUIRED`
- [ ] Test expired token → `UNAUTHORIZED` with `TOKEN_EXPIRED`
- [ ] Test Lotte error → Pass-through with `{OPERATION}_{error_code}`
- [ ] Test Lotte success (`error_code: "0000"`) → TradeX success response
- [ ] Test language header → Correct `lang_code` sent to Lotte
- [ ] Test auto-populated fields → Verify `sourceIp`, `userId` in logs
- [ ] Test `error_desc` with `[CODE]` prefix → Verify passed through correctly
- [ ] Test business rule violations → Correct HTTP status (422) and error format

**Example Test Cases:**

| Test Case | Expected error_code | Expected TradeX Response |
|-----------|---------------------|--------------------------|
| Missing `accountNumber` | N/A (TradeX validation) | `INVALID_PARAMETER` + `FIELD_IS_REQUIRED` |
| Insufficient margin | `1005` (example) | `ORDER_PLACE_1005` + Lotte message |
| Wrong order price | Varies | `ORDER_PLACE_{code}` + Lotte message |
| Expired token | N/A (TradeX auth) | `TOKEN_EXPIRED` |
| Success order | `0000` | `success: true, code: "0000"` |
| Cancel success | `0320` | `success: true` (treated as success) |

---

## API Specification Template

### Standard Format for API Mapping Documents

Khi tạo API spec cho Derivatives APIs, sử dụng template sau để đảm bảo consistency:

#### 1. Overview Section

| Service | Action | Endpoint (TradeX) | Endpoint (Lotte) | Inbound | Outbound | Description |
|---------|--------|-------------------|------------------|---------|----------|-------------|
| `service-name` | HTTP_METHOD | `/api/v1/derivatives/...` | Lotte endpoint | Auth method | Lotte auth | Feature description |

**Example:**
```markdown
| Service | Action | Endpoint (TradeX) | Endpoint (Lotte) | Description |
|---------|--------|-------------------|------------------|-------------|
| `tuxedo` | POST | `/api/v1/derivatives/order` | `/tuxsvc/der/order/dr-buy-by-user` | Đặt lệnh mua phái sinh |
```

#### 2. Mapping Table - Input

| No | Field (TradeX) | Type | Required | Description | Field (Lotte) | Type | Sample | Note |
|----|----------------|------|----------|-------------|---------------|------|--------|------|
| 1 | `accountNumber` | string | **Y** | Số tài khoản | `acnt_no` | string | `"0001234567"` | |
| 2 | `code` | string | **Y** | Mã hợp đồng | `ft_code` | string | `"VN30F2502"` | |
| 3 | `sourceIp` | string | **Y** | Client IP | `cli_ip_addr` | string | `"192.168.1.100"` | **Auto-populated** by backend |
| 4 | `userId` | - | - | Username | `user_id` | string | - | **Auto-populated** from JWT Token |

**Note:** 
- Required = **Y** cho field bắt buộc
- **Auto-populated fields** - Backend tự động điền, FE không truyền

#### 3. Mapping Table - Output

| No | Field (Lotte) | Type | Description | Field (TradeX) | Type | Transform | Sample |
|----|---------------|------|-------------|----------------|------|-----------|--------|
| 1 | `error_code` | string | Status code | - | - | Check `"0000"` = success | `"0000"` |
| 2 | `error_desc` | string | Error message | `message` | string | Pass-through | `"[V3120] Lỗi..."` |
| 3 | `new_ord_no` | string | Order number | `orderNumber` | string | Direct mapping | `"2026020400123"` |
| 4 | `ft_code` | string | Contract code | `code` | string | Direct mapping | `"VN30F2502"` |

#### 4. Error Mapping

| Lotte Response | HTTP Status | TradeX Code | TradeX Message | Hướng xử lý |
|----------------|-------------|-------------|----------------|-------------|
| `error_code: "0000"` | 200 | `0000` | Success message | Success response with data |
| `error_code: "1005"` | 422 | `ORDER_PLACE_1005` | `error_desc` (pass-through) | Business rule violation |
| `error_code: "0307"` | 200 | `0000` | Success (order placed) | Treated as success |
| Validation fails (TradeX) | 400 | `INVALID_PARAMETER` | Validation details | Don't call Lotte |
| Token expired | 401 | `TOKEN_EXPIRED` | Token expired message | Don't call Lotte |

**Standard Error Codes:**
- `INVALID_PARAMETER` - Validation errors (missing/invalid fields)
- `{OPERATION}_{LOTTE_CODE}` - Pass-through Lotte errors
- `TOKEN_EXPIRED` - Authentication errors
- `INTERNAL_SERVER_ERROR` - System errors

#### 5. Business Rules

| No | Rule | Condition | Example | Action / HTTP | Client Response |
|----|------|-----------|---------|---------------|-----------------|
| 1 | Required fields | Missing `accountNumber` | Request without account | 400 | `INVALID_PARAMETER` + `FIELD_IS_REQUIRED` |
| 2 | Validate account | Account ≠ JWT account | Login as `A`, use account `B` | 400 | `INVALID_PARAMETER` + `INVALID_VALUE` |
| 3 | Pass-through Lotte | Lotte `error_code != "0000"` | Insufficient margin | 422 | `{OPERATION}_{error_code}` + `error_desc` |
| 4 | Auto-populate | Missing `sourceIp` | - | - | Backend auto-fills from request IP |

**Required Field Validation Format:**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "accountNumber",
      "messageParams": ["accountNumber"]
    }
  ]
}
```

**Invalid Value Validation Format:**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "INVALID_VALUE",
      "param": "accountNumber",
      "messageParams": ["accountNumber", "expected_value"]
    }
  ]
}
```

#### 6. Implementation Notes

**Auto-Populated Fields:**
- Document which fields are auto-populated
- Specify source (JWT Token, Request IP, etc.)
- FE developers don't need to provide these

**Success Code Handling:**
- Always check Lotte `error_code: "0000"` first
- Some operations have additional success codes (e.g., `0307`, `0320`)
- Document operation-specific success codes

**Language Support:**
- Specify if endpoint supports multi-language
- Document `Accept-Language` → `lang_code` mapping
- Note that `error_desc` language depends on `lang_code`

---

### Example: Complete API Spec

See **`Regular_Orders_API_Spec.md`** for a complete example following this template.

**Key sections:**
1. ✅ Overview table with endpoints
2. ✅ Request/Response field mappings
3. ✅ Error handling patterns
4. ✅ Business rules with examples
5. ✅ Auto-populated fields documentation
6. ✅ HTTP status codes reference

---

## How to Create API Specifications

### 📋 Step-by-Step Guide

#### Step 1: Copy Template

```bash
cp tradex-api-spec-template.md [Feature_Name]_API_Spec.md
```

#### Step 2: Fill Basic Information

Replace placeholders trong template.

#### Step 3: Complete Overview Section

- **Lotte Endpoint** - From Lotte API documentation
- **Lotte Doc Code** - From Lotte docs header
- **Service** - Check architecture diagram

#### Step 4: Fill Field Mapping Tables

**✅ Always document auto-populated fields:**

| TradeX Field | Lotte Field | Source | Note |
|--------------|-------------|--------|------|
| `sourceIp` | `cli_ip_addr` | Request IP | **Auto-populated** |
| `userId` | `user_id` | JWT Token | **Auto-populated** |
| `deviceUniqueId` | `cli_mac_addr` | FE request | **FE must provide** |

#### Step 5: Define Business Rules

1. **Required Fields** - Always first
2. **Account Validation** - If account-specific API
3. **Value Validation** - Enum values, ranges
4. **Pass-Through Lotte Errors** - Always last

#### Step 6: Add Examples

**Minimum 3 examples:**
1. ✅ Success Case
2. ❌ Validation Error
3. ❌ Lotte Business Error

### 🎯 Best Practices

**DO ✅**
- Document ALL auto-populated fields with source
- Include error_code mapping
- Add real examples
- Keep consistent format

**DON'T ❌**
- Don't skip Business Rules
- Don't forget auto-populated fields
- Don't mix TradeX/Lotte field names

### 🔍 Review Checklist

**Completeness:**
- [ ] All sections filled
- [ ] Field mappings complete
- [ ] Business rules documented
- [ ] At least 3 examples

**Accuracy:**
- [ ] Auto-populated fields marked
- [ ] Error codes match conventions
- [ ] HTTP status codes correct

**Consistency:**
- [ ] Follows conventions patterns
- [ ] Error format matches standard

---

## Related Documents

| Document | Location | Description |
|----------|----------|-------------|
| API Spec Template | `./tradex-api-spec-template.md` | Template for new API specs |
| Regular Orders Example | `@Derivatives/Planning documentation/Order/Specifications/Regular_Orders_API_Spec.md` | Complete API spec example |
| TradeX Common (Java) | `@TradeX MCP/Knowledge based/tradex-common-java-main/` | Exception classes |
| rest-proxy | `@TradeX MCP/Knowledge based/rest-proxy-main/` | Auto-populate logic |
| lotte-bridge | `@TradeX MCP/Knowledge based/lotte-bridge-main/` | Field mapping logic |

---

## Revision History

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2026-02-04 | 1.0 | Initial version | PM/BA Team |
| 2026-02-04 | 1.1 | Moved to TradeX Knowledge (TradeX-wide standards) | PM/BA Team |
| 2026-02-04 | 2.0 | Merged with how-to guide, removed redundant files | PM/BA Team |

---

**Questions?** Contact BA/PM Team or refer to source code examples in `lotte-bridge/src/services/`
