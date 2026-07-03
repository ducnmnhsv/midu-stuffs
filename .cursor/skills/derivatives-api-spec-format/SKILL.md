# SKILL: Derivatives API Specification Format

**Skill ID:** derivatives-api-spec-format  
**Version:** 1.0  
**Created:** February 10, 2026  
**Purpose:** Standard format for all Derivatives API specification documents

---

## When to Use This Skill

Use this skill when:
- Creating API specification for ANY Derivatives API (Orders, Account, Market Data, etc.)
- Analyzing Lotte API and mapping to TradeX
- Writing technical documentation for developers
- Ensuring consistency across all API specs

---

## Standard Document Structure

### Header Format

```markdown
# {API Name} API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives {Category} - {Subcategory}  
**Version:** {X.X}  
**Date:** {Month Day, Year}

> **Note:** {Brief description and Lotte API codes}

---
```

**Example:**
```markdown
# Regular Orders API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Orders - Regular Buy/Sell Orders  
**Version:** 2.0  
**Date:** February 5, 2026

> **Note:** Lotte-integrated APIs for **Derivatives only**. For Equity orders, see `@Knowledge/TradeX/Planning/regular-order-api-mapping.md`

---
```

### Section Structure (Required Sections)

```markdown
## 1. Overview
### 1.1 Purpose
### 1.2 API Endpoints
### 1.3 Response Format Standards

## 2. Business Rules
### 2.1 {Specific Rules}
### 2.2 {Specific Rules}
### 2.3 Language Mapping

## 3. API: {First API Name}
### 3.1 Request
### 3.2 Request Mapping
### 3.3 Response Mapping
### 3.4 Error Mapping

## 4. API: {Second API Name} (if applicable)
### 4.1 Request
### 4.2 Request Mapping
### 4.3 Response Mapping
### 4.4 Error Mapping

... (repeat for each API)

## N. Error Handling Summary
### N.1 Error Response Format
### N.2 Error Code Patterns
### N.3 Common Lotte Error Codes

## N+1. Implementation Notes
### (N+1).1 Service Architecture
### (N+1).2 Key Principles
### (N+1).3 {Other Considerations}

## N+2. Related APIs

## N+3. Testing Scenarios (optional)

## N+4. Implementation Checklist (optional)

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team  
**Estimated Effort:** {X-Y days}
```

---

## 1. Overview Section

### 1.1 Purpose

**Format:**
```markdown
### 1.1 Purpose

{1-2 paragraphs explaining what the API does}

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| {Operation Name} | {HTTP Method} | `{/api/v1/...}` |
```

**Example:**
```markdown
### 1.1 Purpose

Regular Orders (Derivatives) là các lệnh mua/bán thông thường (LO, ATO, ATC, MOK, MAK) được đẩy trực tiếp sang Lotte Core.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place Order | POST | `/api/v1/derivatives/order` |
| Modify Order | PUT | `/api/v1/derivatives/order/modify` |
| Cancel Order | PUT | `/api/v1/derivatives/order/cancel` |
```

### 1.3 Response Format Standards

**CRITICAL:** Show TradeX response format (NOT Lotte format)

**Format:**
```markdown
### 1.3 Response Format Standards

**Success (Mutation):**
```json
{
  "message": "...",
  "orderNumber": "..."
}
```

**Success (Query):**
```json
{
  "totalCount": 10,
  "items": [...]
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
- Mutation: Minimal response
- Query: Rich data arrays
- **GET APIs:** TradeX có 2 query params optional (cả hai **không required**): **fetchCount** → Core `row_count`, **nextKey** → Core `next_data` (hoặc `next_key` tùy API). Xem TradeX Knowledge API Standards § Common Request Fields — GET API optional parameters.
- Pass-through Lotte messages AS-IS
```

---

## 2. Business Rules Section

### 2.1 Specific Rules

**Format:**
```markdown
### 2.1 {Rule Category}

| Rule | Description | Error Code |
|------|-------------|------------|
| {Rule Name} | {Description} | `{ERROR_CODE}` |

**Note:** Business rules (business logic) are validated by Lotte Core.
```

**Example:**
```markdown
### 2.1 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, symbolCode, sellBuyType, orderType, orderQuantity | `FIELD_IS_REQUIRED` |
| Price for LO | orderPrice MUST be provided if orderType = LO | `FIELD_IS_REQUIRED` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

**Note:** Business rules (price limits, margin, position limits) are validated by Lotte Core.
```

### 2.3 Language Mapping (Always Include)

**Format:**
```markdown
### 2.3 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V0307] Bạn đã thực hiện lệnh Mua..."` |
| `en` | `E` | `"[E0307] Order placed successfully..."` |
| `ko` | `K` | `"[K0307] 주문 성공..."` |
```

---

## 3. API Detail Section

### 3.1 Request

**Format:**
```markdown
### 3.1 Request

**Endpoint:** `{HTTP METHOD} {TradeX Endpoint}`

**Lotte Endpoint:** `{Lotte URL}` ({LOTTE-CODE})

**Lotte Doc:** {LOTTE-CODE}

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)
```

**Example:**
```markdown
### 3.1 Request

**Endpoint:** `POST /api/v1/derivatives/order`

**Lotte Endpoint:** `[RootURL]/tuxsvc/der/order/dr-buy-by-user` (DRORD-029)

**Lotte Doc:** DRORD-029

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)
```

### 3.2 Request Mapping

**CRITICAL – TradeX naming (không dùng tên/giá trị Core):**

- **Tên tham số TradeX** phải do TradeX định nghĩa, có nghĩa (vd. `orderSendFilter`, `sellBuyType`). **Không** dùng tên field Core (vd. `sent`, `sell_buy_tp`) làm tên param API TradeX.
- **Giá trị (enum)** dùng giá trị có nghĩa (vd. `ALL`, `SENT`, `PENDING`, `BUY`, `SELL`). **Không** expose mã Core (0, 1, 2) làm giá trị hợp lệ của API.
- **Bắt buộc** có bảng mapping: **TradeX parameter + values** → **Core field + codes**. Xem TradeX Knowledge § Common Request Fields — TradeX naming cho request/query parameters.

**CRITICAL:** Use table format with columns:
- TradeX Field (tên TradeX, không phải tên Lotte)
- Type
- Required (✅/❌)
- Lotte Field (tên field Core khi gửi sang Lotte)
- Transform (Direct hoặc Map với bảng value mapping)
- Description

**Format:**
```markdown
### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `{field}` | String | ✅ | `{lotte_field}` | Direct | {Description} |
| `{field}` | Number | ❌ | `{lotte_field}` | Map (see below) | {Description} |
| *(Request IP)* | - | - | `ip_addr` | Auto | Client IP |
| *(JWT)* `userId` | - | - | `user_id` | Auto | Username from token |
| *(Header)* | - | - | `lang_code` | Map (§2.3) | Language code |
| - | - | - | `row_count` | Fixed: 500 | {Description} |

**{Transformation Details}:** (if needed)

| TradeX `{field}` | Lotte `{field}` | Note |
|------------------|-----------------|------|
| `{value}` | `{mapped_value}` | {Note} |
```

**Example:**
```markdown
### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `symbolCode` | String | ✅ | `ft_code` | Direct | Mã hợp đồng (VN30F2502) |
| `sellBuyType` | String | ✅ | - | **Routing only** | `BUY` → dr-buy-by-user<br>`SELL` → dr-sell-by-user |
| `orderType` | String | ✅ | `ord_type` | Map (see below) | Loại lệnh |
| *(Request IP)* | - | - | `ip_addr` | Auto | Client IP |
| *(JWT)* `userId` | - | - | `user_id` | Auto | Username from token |

**Order Type Mapping:**

| TradeX `orderType` | Lotte `ord_type` | Note |
|--------------------|------------------|------|
| `LO` | `2` | Limit Order |
| `ATO` | `3` | At The Open |
```

### 3.3 Response Mapping

**CRITICAL:** Show both Lotte and TradeX formats

**Format:**
```markdown
### 3.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `error_code` | - | - | Check = `"0000"` | Success indicator |
| `{lotte_field}` | `{tradex_field}` | {Type} | {Transform} | {Description} |

**Response Example:**

Lotte Response:
```json
{
  "error_code": "0000",
  "error_desc": "[V0307] Message",
  "data_list": { ... }
}
```

TradeX Response:
```json
{
  "message": "[V0307] Message",
  "orderNumber": "..."
}
```

**Note:**
- {Any important notes about transformation}
```

### 3.4 Error Mapping

**Format:**
```markdown
### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `{field}` | `FIELD_IS_REQUIRED` | `["{field}"]` | Missing |
| `{field}` | `INVALID_VALUE` | `["{field}", "value", "{valid_values}"]` | Invalid value |

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

| Lotte Code | TradeX Code | Description (VI) |
|------------|-------------|------------------|
| `1005` | `ORDER_{OPERATION}_1005` | {Description} |
| `2010` | `ORDER_{OPERATION}_2010` | {Description} |

**Error Response Format:**

Lotte Error:
```json
{
  "error_code": "1005",
  "error_desc": "[V1005] Message",
  "success": false
}
```

TradeX Response (422):
```json
{
  "code": "ORDER_{OPERATION}_1005",
  "message": "[V1005] Message"
}
```
```

---

## 4. Error Handling Summary Section

**Format:**
```markdown
## N. Error Handling Summary

### N.1 Error Response Format

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [...]
}
```

**Auth Error (401/403):**
```json
{
  "code": "UNAUTHORIZED" / "TOKEN_EXPIRED" / "FORBIDDEN" / "UNAUTHORIZED_ACCOUNT",
  "message": "Error message"
}
```

**Business Error (422) - Lotte Pass-Through:**
```json
{
  "code": "{PREFIX}_{LOTTE_CODE}",
  "message": "[V1005] Lotte error message"
}
```

**Server Error (500):**
```json
{
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

### N.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business | `{PREFIX}_{LOTTE_CODE}` | `ORDER_PLACE_1005` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

### N.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|------------------|
| `1005` | {Description VI} | {Description EN} |
| `2010` | {Description VI} | {Description EN} |
```

---

## 5. Implementation Notes Section

**Format:**
```markdown
## N. Implementation Notes

### N.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `{service}` | {Role} |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| **Kafka** | Service communication |

### N.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, data types, format, account ownership
- Lotte validates: Business rules
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Pass-through Lotte `error_desc` AS-IS (including `[CODE]` prefix)
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `{PREFIX}_{LOTTE_CODE}`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header
```

---

## 6. Related APIs Section

**Format:**
```markdown
## N. Related APIs

### N.1 {Category}

| API Code | Name | Relationship |
|----------|------|--------------|
| {LOTTE-CODE} | {Name} | {How they relate} |
```

---

## Key Principles

### 1. Tables Over Code Blocks

✅ **GOOD - Use tables:**
```markdown
| TradeX Field | Type | Required | Lotte Field |
|--------------|------|----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` |
```

❌ **BAD - Don't use code blocks for mapping:**
```markdown
```typescript
const mapping = {
  accountNumber: "acnt_no",
  symbol: "code"
};
```
```

### 2. Show Both Formats (Lotte and TradeX)

Always show:
1. **Request Example:** TradeX Request → Lotte Request
2. **Response Example:** Lotte Response → TradeX Response

### 3. Emphasize TradeX Response Format

**CRITICAL:** Section 1.3 shows TradeX format (what developers build)
- NOT Lotte format
- Focus on TradeX API contract

### 4. Error Mapping Detail

Always include:
- Validation errors (400)
- Auth errors (401/403)
- Lotte pass-through errors (422)
- Server errors (500)

### 5. Use Consistent Terminology

| Term | Meaning |
|------|---------|
| **Direct** | No transformation needed |
| **Map** | Enum or value mapping |
| **Auto** | Auto-populated by backend |
| **Fixed** | Hardcoded value |
| **Calculated** | Computed value |

### 6. Auto-Populated Fields Notation

Use special notation for auto-populated fields:
- `*(Request IP)*` - From HTTP request
- `*(JWT)* userId` - From JWT token
- `*(Header)*` - From HTTP headers

### 7. Document Footer

Always end with:
```markdown
---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team  
**Estimated Effort:** {X-Y days} (BE) + {X-Y days} (FE) + {X days} (QA)
```

---

## Examples

### Example 1: Mutation API (Place Order)

See: `Regular_Orders_API_Spec.md` Section 3

**Key Elements:**
- Request/Response mapping tables
- Error mapping with all 4 categories (400/401/403/422)
- Both Lotte and TradeX examples
- Clear transformation rules

### Example 2: Query API (Query History)

See: `Regular_Orders_API_Spec.md` Section 7

**Key Elements:**
- Response object mapping (array of objects)
- Pagination support
- Empty result handling

### Example 3: Single API (Availability Check)

See: `Order_Availability_Check_API_Spec.md`

**Key Elements:**
- Simple request/response
- Calculated fields (marginPerContract, estimatedMarginRequired)
- Use cases section

---

## Common Pitfalls to Avoid

1. ❌ **Showing only Lotte format** → Show both Lotte AND TradeX
2. ❌ **Using code blocks for mapping** → Use tables
3. ❌ **Missing error mapping** → Include all 4 error types
4. ❌ **Not showing examples** → Always show request/response examples
5. ❌ **Inconsistent field names** → Follow TradeX naming conventions
6. ❌ **Missing auto-populated fields** → Document `*(JWT)*`, `*(Request IP)*`
7. ❌ **No transformation details** → Explain enum/value mappings
8. ❌ **Mixing Lotte and TradeX concepts** → Clearly separate sections

---

## Validation Checklist

Before finalizing an API spec, verify:

- [ ] Header format correct (Document Type, Category, Version, Date)
- [ ] All required sections present (1. Overview → N. Implementation Notes)
- [ ] Section 1.3 shows TradeX response format (NOT Lotte)
- [ ] All mapping tables use correct columns
- [ ] Both Lotte and TradeX examples shown
- [ ] Error mapping includes 400/401/403/422/500
- [ ] Auto-populated fields marked with `*()` notation
- [ ] Language mapping section (§2.3) present
- [ ] Implementation Notes section includes "Key Principles"
- [ ] Document footer present with status/effort

---

## Skill Usage

### For AI Agent

When asked to create or analyze an API spec:

1. **Read this skill first** - Understand the standard format
2. **Read existing specs** - See examples (Regular_Orders, Internal_Transfer, VSD_Transaction)
3. **Apply the template** - Follow section structure exactly
4. **Use tables consistently** - No code blocks for mapping
5. **Show both formats** - Lotte AND TradeX examples
6. **Validate** - Check against checklist

### For Human (BA/Dev)

When writing a new API spec:

1. **Copy structure** - Use existing spec as template
2. **Follow this skill** - Apply all rules
3. **Focus on mapping** - Clear transformation rules
4. **Include examples** - Request/response for each API
5. **Review checklist** - Ensure nothing missed

---

## Ecosystem Integration

This skill is part of the **TradeX Skill/Rule Ecosystem**. When activated, also check:

| Step | Rule/Skill | Why |
|------|------------|-----|
| Before | `tradex-api-naming` | Validate all endpoint URLs, DTO names |
| During (Order APIs) | `tradex-order-api-response-standards` | Correct response format per integration type |
| During (error format) | `tradex-knowledge` → `tradex-api-conventions` | Error code standards, auto-populated fields |
| After | `derivatives-doc-structure` | Save file in correct folder with correct name |

> **Orchestrator:** See `.cursor/rules/ecosystem-orchestrator.mdc` for full routing logic.

---

**Skill Status:** Active  
**Last Updated:** February 27, 2026  
**Maintained By:** BA Team  
**Used By:** All Derivatives API specification documents
