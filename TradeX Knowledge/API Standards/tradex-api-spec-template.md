# [Feature Name] API Specification

**Document Type:** API Specification  
**Category:** [Orders | Market Data | Account | Portfolio | ...]  
**Project:** [Derivatives | Equity | Common | ...]  
**Date:** [YYYY-MM-DD]  
**Version:** 1.0

> **Note:** This spec follows **TradeX API Conventions**  
> See: `@TradeX Knowledge/tradex-api-conventions.md`

---

## Table of Contents

1. [Overview](#overview)
2. [API Endpoint](#api-endpoint)
3. [Field Mapping](#field-mapping)
4. [Error Mapping](#error-mapping)
5. [Business Rules](#business-rules)
6. [Examples](#examples)

---

## Overview

### Purpose

[Brief description of what this API does]

### Scope

| Operation | TradeX Endpoint | Lotte Endpoint |
|-----------|-----------------|----------------|
| [Action] | `[HTTP_METHOD] /api/v1/derivatives/[path]` | `[Lotte path]` |

### Architecture

```
Client/App → rest-proxy → [service] → Lotte API
```

---

## API Endpoint

### 1. [Operation Name]

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `[HTTP_METHOD] /api/v1/derivatives/[path]` |
| **Lotte Endpoint** | `[RootURL]/[lotte-path]` |
| **Lotte Doc Code** | [DOC-CODE] |
| **Service** | [service-name] |
| **Authentication** | JWT + API KEY |

#### Request

```http
[HTTP_METHOD] /api/v1/derivatives/[path] HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
Accept-Language: vi

{
  "field1": "value1",
  "field2": "value2"
}
```

---

## Field Mapping

### Input (Request Body)

| No | TradeX Field | Type | Required | Description | Lotte Field | Lotte Type | Sample | Note |
|----|--------------|------|----------|-------------|-------------|------------|--------|------|
| 1 | `accountNumber` | String | **Yes** | Số tài khoản | `acnt_no` | string | `"0001234567"` | |
| 2 | `code` | String | **Yes** | Mã CK/HĐ | `stk_cd` / `ft_code` | string | `"VN30F2502"` | |
| 3 | `sourceIp` | String | **Yes** | Client IP | `cli_ip_addr` | string | `"192.168.1.100"` | **Auto-populated** |
| 4 | `deviceUniqueId` | String | **Yes** | Device ID | `cli_mac_addr` | string | `"A1B2C3D4E5F6"` | FE must provide |
| 5 | *(from JWT)* | - | - | Username | `user_id` | string | - | **Auto-populated** |
| 6 | *(from header)* | - | - | Language | `lang_code` | string | `V`/`E`/`K` | **Auto-populated** |

**Auto-Populated Fields:**
- `sourceIp` - From request IP (by `rest-proxy`)
- `user_id` - From JWT Token `userId` field
- `lang_code` - From `Accept-Language` header

### Output (Response)

#### Success Response (200)

**Lotte-integrated (mutation):**
```json
{
  "message": "[V0307] Success message from Lotte",
  "orderNumber": "2026020400123"
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
  "items": [ ... ]
}
```

**Note:** 
- NO `success: true` or `code: "0000"` - use HTTP 200 status
- Mutation responses are minimal (message + ID only)
- Query responses contain rich data arrays

#### Response Field Mapping

| No | Lotte Field | Type | Description | TradeX Field | Type | Transform | Sample |
|----|-------------|------|-------------|--------------|------|-----------|--------|
| 1 | `error_code` | string | Status | - | - | Check = `"0000"` | `"0000"` |
| 2 | `error_desc` | string | Message | `message` | string | Pass-through | - |
| 3 | `field1` | string | Data field | `field1` | string | Direct | - |

---

## Error Mapping

### Standard Error Responses

#### 1. Validation Error (400)

**Cause:** Missing/invalid required fields (TradeX validation)

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

**Note:** NO `success` or `message` field - FE constructs from code + messageParams

#### 2. Pass-Through Core Error (422)

**Cause:** Business rule violation from Lotte

**Lotte Response:**
```json
{
  "error_code": "1005",
  "error_desc": "[V3120] Lỗi từ Lotte"
}
```

**TradeX Response:**
```json
{
  "code": "[OPERATION]_1005",
  "message": "[V3120] Lỗi từ Lotte"
}
```

**Note:** Pass-through Lotte message AS-IS (including `[CODE]` prefix)

#### 3. Authentication Error (401)

```json
{
  "code": "TOKEN_EXPIRED",
  "message": "Phiên đăng nhập đã hết hạn"
}
```

### Error Mapping Table

| Lotte Response | HTTP Status | TradeX Code | TradeX Message | Note |
|----------------|-------------|-------------|----------------|------|
| `error_code: "0000"` | 200 | `0000` | Success | Standard success |
| `error_code: "[code]"` | 422 | `[OPERATION]_[code]` | Pass-through `error_desc` | Business error |
| Validation fails | 400 | `INVALID_PARAMETER` | Validation details | TradeX validation |
| Token expired | 401 | `TOKEN_EXPIRED` | Auth error message | TradeX auth |

---

## Business Rules

| No | Rule | Condition | Example | Action / HTTP | Client Response |
|----|------|-----------|---------|---------------|-----------------|
| 1 | **Required Fields** | Missing required field(s) | Request without `accountNumber` | 400 - Don't call Lotte | `INVALID_PARAMETER` + `FIELD_IS_REQUIRED` |
| 2 | **Validate Account** | `accountNumber` ≠ JWT account | Login account `A`, use account `B` | 400 - Don't call Lotte | `INVALID_PARAMETER` + `INVALID_VALUE` |
| 3 | **Validate Order Type** | Invalid `orderType` value | `orderType: "INVALID"` | 400 - Don't call Lotte | `INVALID_PARAMETER` + `INVALID_VALUE` |
| 4 | **Pass-Through Lotte** | Lotte `error_code != "0000"` | Insufficient margin, wrong price | 422 - Return Lotte error | `[OPERATION]_[code]` + `error_desc` |
| 5 | **Auto-Populate** | Missing auto-populated fields | - | - | Backend auto-fills |

### Validation Details

#### Required Field Error Format

```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "fieldName",
      "messageParams": ["fieldName"]
    }
  ]
}
```

#### Invalid Value Error Format

```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "INVALID_VALUE",
      "param": "fieldName",
      "messageParams": ["fieldName", "actual_value", "expected_values"]
    }
  ]
}
```

---

## Examples

### Example 1: Success Request

**Request:**
```json
POST /api/v1/derivatives/[path]
Authorization: Bearer [token]
Content-Type: application/json

{
  "accountNumber": "0001234567",
  "field1": "value1"
}
```

**Lotte Response:**
```json
{
  "error_code": "0000",
  "error_desc": "",
  "data_list": [
    {
      "field1": "value1"
    }
  ]
}
```

**TradeX Response:**
```json
{
  "message": "[V0307] Success message from Lotte",
  "orderNumber": "123"
}
```

### Example 2: Missing Required Field

**Request:**
```json
{
  "field1": "value1"
  // Missing: accountNumber
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
    }
  ]
}
```

### Example 3: Lotte Business Error

**Lotte Response:**
```json
{
  "error_code": "1005",
  "error_desc": "[V3120] Business error message"
}
```

**TradeX Response (422):**
```json
{
  "code": "[OPERATION]_1005",
  "message": "[V3120] Business error message"
}
```

---

## Implementation Notes

### Service: `rest-proxy`

**Responsibilities:**
- Validate JWT token
- Auto-populate `sourceIp` from request IP
- Route to appropriate service via Kafka

### Service: `[service-name]`

**Responsibilities:**
- Validate request fields
- Auto-populate fields from JWT (`user_id`, etc.)
- Map TradeX fields → Lotte fields
- Call Lotte API
- Transform Lotte response → TradeX response
- Handle errors (validation + pass-through)

### Testing Checklist

- [ ] Test missing required field → `INVALID_PARAMETER` with `FIELD_IS_REQUIRED`
- [ ] Test invalid field value → `INVALID_PARAMETER` with `INVALID_VALUE`
- [ ] Test expired token → `TOKEN_EXPIRED`
- [ ] Test Lotte success (`error_code: "0000"`) → TradeX success
- [ ] Test Lotte error → Pass-through with `[OPERATION]_[code]`
- [ ] Test auto-populated fields → Verify in logs
- [ ] Test language header → Correct `lang_code` sent to Lotte

---

## Related Documents

| Document | Location | Description |
|----------|----------|-------------|
| TradeX API Conventions | `@TradeX Knowledge/tradex-api-conventions.md` | Standard patterns for all APIs |
| API Quick Reference | `@TradeX Knowledge/api-conventions.md` | Quick lookup |
| How to Create Specs | `@TradeX Knowledge/tradex-how-to-create-api-specs.md` | Step-by-step guide |
| [Other Related Spec] | `./[Other].md` | Related feature in same project |
| Lotte API Specs | Ask team | Original Lotte documentation |

---

## Revision History

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| [YYYY-MM-DD] | 1.0 | Initial version | [Author] |

---

**Status:** [Draft | In Review | Approved | Implemented]  
**Maintained By:** BA/PM Team
