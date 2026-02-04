# TradeX Derivatives API Conventions

**Document Type:** Technical Conventions  
**Category:** Derivatives - All APIs  
**Audience:** Backend Developers, PM  
**Date:** February 4, 2026  
**Version:** 1.0

---

## Table of Contents

1. [Overview](#overview)
2. [Auto-Populated Fields](#auto-populated-fields)
3. [Standard Error Formats](#standard-error-formats)
4. [Language Handling](#language-handling)
5. [Common Request Fields](#common-request-fields)
6. [Response Format Standards](#response-format-standards)

---

## Overview

### Purpose

Tài liệu này định nghĩa các conventions chung cho **tất cả API Derivatives** của TradeX, đảm bảo:

- ✅ **Consistent behavior** giữa Equity và Derivatives
- ✅ **Minimal FE changes** khi migrate từ Equity sang Derivatives
- ✅ **Clear error handling** patterns
- ✅ **Transparent field mapping** giữa TradeX ↔ Lotte

### Scope

Convention này áp dụng cho:

- Order APIs (Regular, Conditional)
- Account APIs (Portfolio, Balance, Margin)
- Market Data APIs (Quote, Chart, Symbol Info)
- **Future Derivatives APIs**

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

**Use Case:** Missing required fields, invalid format, out of range

#### Pattern

```json
{
  "success": false,
  "code": "INVALID_PARAMETER",
  "message": "Dữ liệu không hợp lệ",
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

#### Implementation (Java)

**File:** `order-v2/src/main/java/*/services/impl/StopOrderServiceImpl.java`

```java
import com.techx.tradex.common.exceptions.SubErrorsException;
import com.techx.tradex.order.constants.Constants;

// Example: deviceUniqueId is required
if (!StringUtils.isNotBlank(request.getDeviceUniqueId())) {
    throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
            .add(Constants.FIELD_IS_REQUIRED, "deviceUniqueId", 
                 Collections.singletonList("deviceUniqueId"));
}
```

#### Implementation (TypeScript/Node.js)

**File:** `lotte-bridge/src/consumers/RequestHandler.ts`

```typescript
import * as Errors from '../errors';

const accountNumber = data?.accountNumber?.toUpperCase();
if (Utils.isEmpty(accountNumber)) {
  new Errors.InvalidParameterError()
    .add('FIELD_IS_REQUIRED', 'accountNumber', ['accountNumber'])
    .throwErr();
}
```

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
  "success": false,
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

---

## Response Format Standards

### 1. Success Response (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Thành công",
  "data": {
    // Response-specific data
  }
}
```

### 2. Error Response Structure

**Base Structure:**

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "Error message in selected language"
}
```

**With Validation Details (INVALID_PARAMETER):**

```json
{
  "success": false,
  "code": "INVALID_PARAMETER",
  "message": "Dữ liệu không hợp lệ",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "accountNumber",
      "messageParams": ["accountNumber"]
    }
  ]
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
  "success": false,
  "code": "INVALID_PARAMETER",
  "message": "Dữ liệu không hợp lệ",
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
  "success": false,
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
  "success": true,
  "code": "0000",
  "message": "Đặt lệnh mua thành công",
  "data": {
    "orderNumber": "2026020400123",
    "code": "VN30F2502",
    "codeName": "HĐ Tương lai VN30 Tháng 02/2026",
    "orderType": "LO",
    "orderPrice": 1285.5,
    "orderQuantity": 5,
    "sellBuyType": "BUY",
    "status": "PENDING",
    "orderTime": "2026-02-04T09:15:30.000Z"
  }
}
```

#### Example 4: Token Expired

**Response (401):**
```json
{
  "success": false,
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
