# Regular Orders API Specification

**Document Type:** API Specification  
**Category:** Orders - Regular Orders (Lệnh Thường)  
**Project:** Derivatives + Equity  
**Date:** February 4, 2026  
**Version:** 2.1

> **Note:** This spec follows **TradeX API Conventions**  
> See: `@TradeX Knowledge/API Standards/tradex-api-conventions.md`

---

## Table of Contents

1. [Overview](#overview)
2. [TradeX API Standards](#tradex-api-standards)
3. [Derivatives Regular Orders](#derivatives-regular-orders)
4. [Equity Regular Orders](#equity-regular-orders)
5. [Error Codes Reference](#error-codes-reference)
6. [Appendix](#appendix)

---

## Overview

### Purpose

Tài liệu này mô tả chi tiết các API đặt lệnh thường (Regular Orders) của TradeX cho cả Equity và Derivatives.

### Scope

| Operation | Equity Endpoint | Derivatives Endpoint |
|-----------|-----------------|---------------------|
| Buy | `POST /api/v1/equity/order` | `POST /api/v1/derivatives/order` |
| Sell | `POST /api/v1/equity/order` | `POST /api/v1/derivatives/order` |
| Cancel | `PUT /api/v1/equity/order/cancel` | `PUT /api/v1/derivatives/order/cancel` |
| Modify | `PUT /api/v1/equity/order/modify` | `PUT /api/v1/derivatives/order/modify` |
| Query Unmatch | `GET /api/v1/equity/order/todayUnmatch` | `GET /api/v1/derivatives/order/todayUnmatch` |

### Architecture

```
Client/App → rest-proxy → lotte-bridge/tuxedo → Lotte Tuxedo API
```

---

## TradeX API Standards

> **📘 Complete Standards:** See `@TradeX Knowledge/API Standards/tradex-api-conventions.md`

This section provides a quick reference. For complete details including:
- Auto-populated fields (sourceIp, userId, etc.)
- Error format standards
- Language handling
- Common request fields

Please refer to the main conventions document.

### Required Headers

| Header | Required | Description | Example |
|--------|----------|-------------|---------|
| `Authorization` | **Yes** | JWT Bearer token | `Bearer eyJhbGciOi...` |
| `Content-Type` | **Yes** | Content type | `application/json` |
| `Accept-Language` | No | Response language | `vi` / `en` (default: `vi`) |

> **Note:** `deviceUniqueId` và `sourceIp` được truyền trong **request body**, không phải header.

### Language Mapping

| Accept-Language | Lotte lang_code | Description |
|-----------------|-----------------|-------------|
| `vi` | `V` | Tiếng Việt |
| `en` | `E` | English |
| `ko` | `K` | 한국어 |

### JWT Token

Token được lấy từ API Login và chứa:

| Field | Description |
|-------|-------------|
| `userId` | User ID (max 15 chars) |
| `accountNumber` | Số tài khoản |
| `subNumber` | Số tiểu khoản (equity) |
| `sessionId` | Session ID |
| `exp` | Token expiry time |

### Standard Response Format

**Success:**
```json
{
  "success": true,
  "code": "0000",
  "message": "Đặt lệnh thành công",
  "data": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "Error message",
  "errors": [
    { "field": "fieldName", "code": "CODE", "message": "Detail" }
  ]
}
```

### HTTP Status Codes

| Status | Use Case |
|--------|----------|
| `200` | Success |
| `400` | Missing/invalid fields |
| `401` | Invalid/expired JWT |
| `403` | Account not authorized |
| `422` | Business rule violation |
| `500` | Server/Lotte API error |

---

## Derivatives Regular Orders

### 1. Buy Order (Đặt Lệnh Mua)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `POST /api/v1/derivatives/order` |
| **Lotte Endpoint** | `[RootURL]/tuxsvc/der/order/dr-buy-by-user` |
| **Lotte Doc Code** | DRORD-029 |
| **Authentication** | JWT + API KEY |

#### Request

```http
POST /api/v1/derivatives/order HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
Accept-Language: vi

{
  "accountNumber": "0001234567",
  "code": "VN30F2502",
  "orderQuantity": 5,
  "orderPrice": 1285.5,
  "orderType": "LO",
  "sellBuyType": "BUY",
  "deviceUniqueId": "A1B2C3D4E5F6",
  "sourceIp": "192.168.1.100"
}
```

#### Request Fields & Lotte Mapping

| No | TradeX Field | Type | Required | Description | Lotte Field | Lotte Type | Sample | Note |
|----|--------------|------|----------|-------------|-------------|------------|--------|------|
| 1 | `accountNumber` | String | **Yes** | Số tài khoản | `acnt_no` | string | `"0001234567"` | FE must provide |
| 2 | `code` | String | **Yes** | Mã hợp đồng phái sinh | `ft_code` | string | `"VN30F2502"` | FE must provide |
| 3 | `orderQuantity` | Number | **Yes** | Khối lượng đặt (≥1) | `ord_qty` | number | `5` | FE must provide |
| 4 | `orderPrice` | Number | **Yes** | Giá đặt (trong biên độ) | `lm_ord_price` | number | `1285.5` | FE must provide |
| 5 | `orderType` | String | **Yes** | Loại lệnh | `ord_type` | string | `"LO"` → `2` | FE must provide |
| 6 | `sellBuyType` | String | **Yes** | Mua/Bán | *(endpoint)* | - | `"BUY"` / `"SELL"` | FE must provide (quyết định endpoint) |
| 7 | `deviceUniqueId` | String | **Yes** | Device ID | `cli_mac_addr` | string | `"A1B2C3D4E5F6"` | **FE must provide** |
| 8 | `sourceIp` | String | **Yes** | Client IP | `cli_ip_addr` | string | `"192.168.1.100"` | **Auto-populated by rest-proxy** |
| 9 | *(from JWT)* | - | - | Username | `user_id` | string | - | **Auto-populated from JWT** |
| 10 | *(from JWT)* | - | - | Tên người dùng | `hts_user_nm` | string | - | **Auto-populated from JWT** |
| 11 | *(from JWT)* | - | - | CMND/CCCD | `idno` | string | - | **Auto-populated from JWT** |
| 12 | *(from header)* | - | - | Ngôn ngữ | `lang_code` | string | `"V"`/`"E"`/`"K"` | **Auto-populated from Accept-Language** |

**🔑 Auto-Populated Fields:**
- `sourceIp` - Extracted from request IP by `rest-proxy`
- `user_id`, `hts_user_nm`, `idno` - Extracted from JWT Token by `lotte-bridge`
- `lang_code` - Mapped from `Accept-Language` header (`vi`→`V`, `en`→`E`, `ko`→`K`)

**📝 FE Must Provide:**
- All fields except auto-populated ones
- `deviceUniqueId` is **critical** - cannot be determined server-side

**Order Types:**

| Code | Lotte | Tên | Session |
|------|-------|-----|---------|
| `LO` | 2 | Lệnh giới hạn | All |
| `ATO` | 3 | Lệnh mở cửa | 08:45-09:00 |
| `ATC` | 6 | Lệnh đóng cửa | 14:30-14:45 |
| `MTL` | 9 | Market To Limit | Continuous |
| `MOK` | 5 | Market Or Kill | Continuous |
| `MAK` | 4 | Market At Kill | Continuous |

#### Response - Success (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Đặt lệnh mua thành công",
  "data": {
    "orderNumber": "2025020300012",
    "code": "VN30F2502",
    "codeName": "HĐ Tương lai VN30 Tháng 02/2025",
    "orderType": "LO",
    "orderTypeName": "Lệnh giới hạn",
    "orderPrice": 1285.5,
    "orderQuantity": 5,
    "sellBuyType": "BUY",
    "sellBuyTypeName": "Mua",
    "status": "PENDING",
    "statusName": "Chờ khớp",
    "orderTime": "2025-02-03T09:15:30.000Z"
  }
}
```

#### Response - Validation Error (400)

**Error Code:** `INVALID_PARAMETER`

```json
{
  "success": false,
  "code": "INVALID_PARAMETER",
  "message": "Dữ liệu không hợp lệ",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "orderPrice",
      "messageParams": ["orderPrice"]
    },
    {
      "code": "INVALID_VALUE",
      "param": "orderQuantity",
      "messageParams": ["orderQuantity", "0", "≥1"]
    }
  ]
}
```

**Common Validation Codes:**
- `FIELD_IS_REQUIRED` - Missing required field
- `INVALID_VALUE` - Value out of range or invalid format
- `INVALID_ACCOUNT` - Account validation failed

#### Response - Unauthorized (401)

```json
{
  "success": false,
  "code": "UNAUTHORIZED",
  "message": "Token không hợp lệ hoặc đã hết hạn",
  "errors": [
    {
      "code": "TOKEN_EXPIRED",
      "message": "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"
    }
  ]
}
```

#### Response - Forbidden (403)

```json
{
  "success": false,
  "code": "FORBIDDEN",
  "message": "Không có quyền truy cập",
  "errors": [
    {
      "code": "UNAUTHORIZED_ACCOUNT",
      "message": "Tài khoản không thuộc quyền sở hữu của bạn"
    }
  ]
}
```

#### Response - Business Error (422) - Pass-Through from Lotte

**Use Case:** Business rule violations, insufficient margin, market closed, etc.

**Error Format:** `{OPERATION}_{LOTTE_ERROR_CODE}`

```json
{
  "success": false,
  "code": "ORDER_PLACE_1005",
  "message": "[V3120] Không đủ ký quỹ để đặt lệnh"
}
```

**Examples:**

| Lotte Error Code | TradeX Code | Message Example |
|------------------|-------------|-----------------|
| `1005` | `ORDER_PLACE_1005` | `[V3120] Không đủ ký quỹ` |
| `1234` | `ORDER_PLACE_1234` | `[V0456] Thị trường đã đóng cửa` |
| `5678` | `ORDER_PLACE_5678` | `[V1111] Giá vượt biên độ` |

**Note:** 
- Message is passed through from Lotte as-is (including `[CODE]` prefix)
- Language matches `Accept-Language` header sent to Lotte
- See `@TradeX Knowledge/API Standards/tradex-api-conventions.md` for complete error handling patterns

#### Response - Server Error (500)

**Use Case:** Internal server errors, Lotte API connection failures

```json
{
  "success": false,
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

**Note:** Generic error for unexpected system failures

---

### 2. Sell Order (Đặt Lệnh Bán)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `POST /api/v1/derivatives/order` |
| **Lotte Endpoint** | `[RootURL]/tuxsvc/der/order/dr-sell-by-user` |
| **Lotte Doc Code** | DRORD-030 |
| **Authentication** | JWT + API KEY |

#### Request

```http
POST /api/v1/derivatives/order HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
Accept-Language: vi

{
  "accountNumber": "0001234567",
  "code": "VN30F2502",
  "orderQuantity": 3,
  "orderPrice": 1290.0,
  "orderType": "LO",
  "sellBuyType": "SELL",
  "deviceUniqueId": "A1B2C3D4E5F6",
  "sourceIp": "192.168.1.100"
}
```

#### Request Fields

Giống Buy Order, với `sellBuyType: "SELL"`

#### Response - Success (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Đặt lệnh bán thành công",
  "data": {
    "orderNumber": "2025020300013",
    "code": "VN30F2502",
    "codeName": "HĐ Tương lai VN30 Tháng 02/2025",
    "orderType": "LO",
    "orderTypeName": "Lệnh giới hạn",
    "orderPrice": 1290.0,
    "orderQuantity": 3,
    "sellBuyType": "SELL",
    "sellBuyTypeName": "Bán",
    "status": "PENDING",
    "statusName": "Chờ khớp",
    "orderTime": "2025-02-03T09:18:45.000Z"
  }
}
```

#### Error Responses

Giống Buy Order

---

### 3. Cancel Order (Hủy Lệnh)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `PUT /api/v1/derivatives/order/cancel` |
| **Lotte Endpoint** | `[RootURL]/tuxsvc/der/order/dr-can-by-user` |
| **Lotte Doc Code** | DRORD-031 |
| **Authentication** | JWT + API KEY |

#### Request

```http
PUT /api/v1/derivatives/order/cancel HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
Accept-Language: vi

{
  "accountNumber": "0001234567",
  "orderNumber": "2025020300012",
  "code": "VN30F2502",
  "deviceUniqueId": "A1B2C3D4E5F6",
  "sourceIp": "192.168.1.100"
}
```

#### Request Fields & Lotte Mapping

| No | TradeX Field | Type | Required | Description | Lotte Field | Lotte Type | Sample | Note |
|----|--------------|------|----------|-------------|-------------|------------|--------|------|
| 1 | `accountNumber` | String | **Yes** | Số tài khoản | `acnt_no` | string | `"0001234567"` | FE must provide |
| 2 | `orderNumber` | String | **Yes** | Số hiệu lệnh cần hủy | `ord_no` | string | `"2025020300012"` | FE must provide |
| 3 | `code` | String | **Yes** | Mã hợp đồng | `ft_code` | string | `"VN30F2502"` | FE must provide |
| 4 | `deviceUniqueId` | String | **Yes** | Device ID | `cli_mac_addr` | string | `"A1B2C3D4E5F6"` | **FE must provide** |
| 5 | `sourceIp` | String | **Yes** | Client IP | `cli_ip_addr` | string | `"192.168.1.100"` | **Auto-populated by rest-proxy** |
| 6 | *(from JWT)* | - | - | Username | `user_id` | string | - | **Auto-populated from JWT** |
| 7 | *(hardcoded)* | - | - | Validity | `validity` | string | `"0"` (DAY) | **Auto-populated by backend** |

**🔑 Auto-Populated Fields:**
- `sourceIp` - From request IP
- `user_id` - From JWT Token
- `validity` - Always `"0"` (DAY) for cancel operations

#### Response - Success (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Hủy lệnh thành công",
  "data": {
    "orderNumber": "2025020300012",
    "code": "VN30F2502",
    "status": "CANCELLED",
    "statusName": "Đã hủy",
    "cancelTime": "2025-02-03T09:20:15.000Z"
  }
}
```

#### Response - Order Already Matched (422) - Pass-Through from Lotte

```json
{
  "success": false,
  "code": "ORDER_CANCEL_1005",
  "message": "[V3456] Lệnh đã khớp hoàn toàn, không thể hủy"
}
```

#### Response - Order Not Found (422) - Pass-Through from Lotte

```json
{
  "success": false,
  "code": "ORDER_CANCEL_1234",
  "message": "[V7890] Không tìm thấy lệnh"
}
```

**Note:** Error codes follow pattern `ORDER_CANCEL_{LOTTE_ERROR_CODE}`

---

### 4. Modify Order (Sửa Lệnh)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `PUT /api/v1/derivatives/order/modify` |
| **Lotte Endpoint** | `[RootURL]/tuxsvc/der/order/dr-mod-by-user` |
| **Lotte Doc Code** | DRORD-032 |
| **Authentication** | JWT + API KEY |

#### Request

```http
PUT /api/v1/derivatives/order/modify HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
Accept-Language: vi

{
  "accountNumber": "0001234567",
  "orderNumber": "2025020300012",
  "code": "VN30F2502",
  "orderQuantity": 3,
  "orderPrice": 1290.0,
  "unmatchedQuantity": 5,
  "deviceUniqueId": "A1B2C3D4E5F6",
  "sourceIp": "192.168.1.100"
}
```

#### Request Fields & Lotte Mapping

| No | TradeX Field | Type | Required | Description | Lotte Field | Lotte Type | Sample | Note |
|----|--------------|------|----------|-------------|-------------|------------|--------|------|
| 1 | `accountNumber` | String | **Yes** | Số tài khoản | `acnt_no` | string | `"0001234567"` | FE must provide |
| 2 | `orderNumber` | String | **Yes** | Số hiệu lệnh cần sửa | `ord_no` | string | `"2025020300012"` | FE must provide |
| 3 | `code` | String | **Yes** | Mã hợp đồng | `ft_code` | string | `"VN30F2502"` | FE must provide |
| 4 | `orderQuantity` | Number | **Yes** | Khối lượng mới | `ord_qty` | number | `3` | FE must provide |
| 5 | `orderPrice` | Number | **Yes** | Giá mới | `ord_price` | number | `1290.0` | FE must provide |
| 6 | `unmatchedQuantity` | Number | **Yes** | KL chưa khớp (lệnh gốc) | `un_mth_qty` | number | `5` | FE must provide |
| 7 | `deviceUniqueId` | String | **Yes** | Device ID | `cli_mac_addr` | string | `"A1B2C3D4E5F6"` | **FE must provide** |
| 8 | `sourceIp` | String | **Yes** | Client IP | `cli_ip_addr` | string | `"192.168.1.100"` | **Auto-populated by rest-proxy** |
| 9 | *(from JWT)* | - | - | Username | `user_id` | string | - | **Auto-populated from JWT** |
| 10 | *(hardcoded)* | - | - | Validity | `validity` | string | `"0"` (DAY) | **Auto-populated by backend** |

**🔑 Auto-Populated Fields:**
- `sourceIp` - From request IP
- `user_id` - From JWT Token
- `validity` - Always `"0"` (DAY) for modify operations

#### Response - Success (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Sửa lệnh thành công",
  "data": {
    "oldOrderNumber": "2025020300012",
    "newOrderNumber": "2025020300015",
    "code": "VN30F2502",
    "codeName": "HĐ Tương lai VN30 Tháng 02/2025",
    "orderType": "LO",
    "orderTypeName": "Lệnh giới hạn",
    "orderPrice": 1290.0,
    "orderQuantity": 3,
    "sellBuyType": "BUY",
    "sellBuyTypeName": "Mua",
    "status": "PENDING",
    "statusName": "Chờ khớp",
    "modifyTime": "2025-02-03T09:25:30.000Z"
  }
}
```

#### Response - Cannot Modify (422) - Pass-Through from Lotte

```json
{
  "success": false,
  "code": "ORDER_MODIFY_1005",
  "message": "[V3456] Lệnh đã khớp hoàn toàn, không thể sửa"
}
```

**Note:** Error codes follow pattern `ORDER_MODIFY_{LOTTE_ERROR_CODE}`

---

### 5. Query Unmatch Orders (Tra cứu Lệnh Chờ)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `GET /api/v1/derivatives/order/todayUnmatch` |
| **Lotte Endpoint** | `[RootURL]/tuxsvc/der/order/dr-nmth-order` |
| **Lotte Doc Code** | DRORD-011 |
| **Authentication** | JWT + API KEY |

#### Request

```http
GET /api/v1/derivatives/order/todayUnmatch?accountNumber=0001234567 HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept-Language: vi
```

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | **Yes** | Số tài khoản |

#### Response - Success (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Lấy danh sách lệnh chờ thành công",
  "data": {
    "totalCount": 2,
    "orders": [
      {
        "orderNumber": "2025020300012",
        "accountNumber": "0001234567",
        "accountName": "NGUYEN VAN A",
        "code": "VN30F2502",
        "codeName": "HĐ Tương lai VN30 Tháng 02/2025",
        "sellBuyType": "BUY",
        "sellBuyTypeName": "Mua",
        "orderType": "LO",
        "orderTypeName": "Lệnh giới hạn",
        "orderPrice": 1285.5,
        "orderQuantity": 5,
        "matchedQuantity": 0,
        "unmatchedQuantity": 5,
        "status": "PENDING",
        "statusName": "Chờ khớp",
        "orderTime": "2025-02-03T09:15:30.000Z",
        "canCancel": true,
        "canModify": true
      },
      {
        "orderNumber": "2025020300008",
        "accountNumber": "0001234567",
        "accountName": "NGUYEN VAN A",
        "code": "VN30F2502",
        "codeName": "HĐ Tương lai VN30 Tháng 02/2025",
        "sellBuyType": "SELL",
        "sellBuyTypeName": "Bán",
        "orderType": "LO",
        "orderTypeName": "Lệnh giới hạn",
        "orderPrice": 1295.0,
        "orderQuantity": 3,
        "matchedQuantity": 1,
        "unmatchedQuantity": 2,
        "status": "PARTIAL",
        "statusName": "Khớp một phần",
        "orderTime": "2025-02-03T09:10:15.000Z",
        "canCancel": true,
        "canModify": true
      }
    ]
  }
}
```

#### Lotte Response Mapping

| Lotte Field | TradeX Field | Description |
|-------------|--------------|-------------|
| `jmno` | `orderNumber` | Số hiệu lệnh |
| `acno` | `accountNumber` | Số TK |
| `acnm` | `accountName` | Tên TK |
| `code` | `code` | Mã hợp đồng |
| `mdms` | `sellBuyType` | 1→BUY, 2→SELL |
| `type` | `orderType` | 2→LO, etc. |
| `jprc` | `orderPrice` | Giá |
| `jqty` | `orderQuantity` | KL đặt |
| `cqty` | `matchedQuantity` | KL khớp |
| `mqty` | `unmatchedQuantity` | KL chưa khớp |

#### Response - Empty List (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Không có lệnh chờ",
  "data": {
    "totalCount": 0,
    "orders": []
  }
}
```

---

## Equity Regular Orders

### 1. Buy Order (Đặt Lệnh Mua Cổ Phiếu)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `POST /api/v1/equity/order` |
| **Lotte Endpoint** | `[RootURL]/tsol/apikey/tuxsvc/order/ord-buy` |
| **Service** | lotte-bridge |
| **Authentication** | OAuth2 + API KEY |

#### Request

```http
POST /api/v1/equity/order HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
Accept-Language: vi

{
  "accountNumber": "0001234567",
  "subNumber": "01",
  "stockCode": "VCB",
  "orderQuantity": 100,
  "orderPrice": 95500,
  "orderType": "LO",
  "sellBuyType": "BUY",
  "securitiesType": "STOCK",
  "deviceUniqueId": "A1B2C3D4E5F6",
  "sourceIp": "192.168.1.100"
}
```

#### Request Fields & Lotte Mapping

| TradeX Field | Type | Required | Lotte Field | Description / Values |
|--------------|------|----------|-------------|----------------------|
| `accountNumber` | String | **Yes** | `acnt_no` | Số tài khoản (10 chars) |
| `subNumber` | String | **Yes** | `sub_no` | Số tiểu khoản (default: "01") |
| `stockCode` | String | **Yes** | `stock_no` | Mã chứng khoán |
| `orderQuantity` | Number | **Yes** | `ord_qty` | Khối lượng (bội số 100) |
| `orderPrice` | Number | **Yes** | `ord_pri` | Giá đặt (VND) |
| `orderType` | String | **Yes** | `stk_ord_tp` | `LO`→LO, `ATO`→ATO, `ATC`→ATC, `MP`→MP |
| `sellBuyType` | String | **Yes** | *(endpoint)* | `BUY`→ord-buy, `SELL`→ord-sell |
| `securitiesType` | String | **Yes** | - | `STOCK` / `FUND` / `BOND` |
| `deviceUniqueId` | String | **Yes** | `cli_mac_addr` | Device ID |
| `sourceIp` | String | **Yes** | `cli_ip_addr` | Client IP address |
| *(from JWT)* | - | - | `hts_user_id` | Username |
| *(from JWT)* | - | - | `hts_user_nm` | User full name |
| *(from JWT)* | - | - | `idno` | Identifier number |
| *(from header)* | - | - | `lang_code` | Accept-Language → `V`/`E`/`K` |

**Order Types (Equity):**

| Code | Tên | Session (HOSE) |
|------|-----|----------------|
| `LO` | Lệnh giới hạn | All |
| `ATO` | Lệnh mở cửa | 09:00-09:15 |
| `ATC` | Lệnh đóng cửa | 14:30-14:45 |
| `MP` | Lệnh thị trường | Continuous |
| `MOK` | Market Or Kill | Continuous |
| `MAK` | Market At Kill | Continuous |

#### Response - Success (200)

```json
{
  "success": true,
  "code": "0000",
  "message": "Đặt lệnh mua thành công",
  "data": {
    "orderNumber": "2025020300025",
    "stockCode": "VCB",
    "stockName": "Ngân hàng TMCP Ngoại thương Việt Nam",
    "orderType": "LO",
    "orderTypeName": "Lệnh giới hạn",
    "orderPrice": 95500,
    "orderQuantity": 100,
    "sellBuyType": "BUY",
    "sellBuyTypeName": "Mua",
    "status": "PENDING",
    "statusName": "Chờ khớp",
    "orderTime": "2025-02-03T09:30:00.000Z"
  }
}
```

### 2. Sell Order (Equity)

Giống Buy Order với `sellBuyType: "SELL"` và Lotte endpoint `ord-sell`

### 3. Cancel Order (Equity)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `PUT /api/v1/equity/order/cancel` |
| **Lotte Endpoint** | `[RootURL]/tsol/apikey/tuxsvc/order/ord-cancel` |
| **Service** | lotte-bridge |

#### Request

```http
PUT /api/v1/equity/order/cancel HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "accountNumber": "0001234567",
  "subNumber": "01",
  "orderNumber": "2025020300025",
  "branchCode": "001",
  "deviceUniqueId": "A1B2C3D4E5F6",
  "sourceIp": "192.168.1.100"
}
```

#### Request Fields & Lotte Mapping

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | **Yes** | `acnt_no` | Số tài khoản |
| `subNumber` | String | **Yes** | `sub_no` | Số tiểu khoản |
| `orderNumber` | String | **Yes** | `ord_no` | Số hiệu lệnh cần hủy |
| `branchCode` | String | **Yes** | `bank_cd` | Mã chi nhánh |
| `deviceUniqueId` | String | **Yes** | `cli_mac_addr` | Device ID |
| `sourceIp` | String | **Yes** | `cli_ip_addr` | Client IP |

---

### 4. Modify Order (Equity)

#### API Info

| Property | Value |
|----------|-------|
| **TradeX Endpoint** | `PUT /api/v1/equity/order/modify` |
| **Lotte Endpoint** | `[RootURL]/tsol/apikey/tuxsvc/order/ord-modify` |
| **Service** | lotte-bridge |

#### Request

```http
PUT /api/v1/equity/order/modify HTTP/1.1
Host: nhsvpro.nhsv.vn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "accountNumber": "0001234567",
  "subNumber": "01",
  "orderNumber": "2025020300025",
  "orderPrice": 96000,
  "orderQuantity": 200,
  "branchCode": "001",
  "deviceUniqueId": "A1B2C3D4E5F6",
  "sourceIp": "192.168.1.100"
}
```

#### Request Fields & Lotte Mapping

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | **Yes** | `acnt_no` | Số tài khoản |
| `subNumber` | String | **Yes** | `sub_no` | Số tiểu khoản |
| `orderNumber` | String | **Yes** | `ord_no` | Số hiệu lệnh cần sửa |
| `orderPrice` | Number | **Yes** | `ord_pri` | Giá mới (VND) |
| `orderQuantity` | Number | **Yes** | `ord_qty` | Khối lượng mới |
| `branchCode` | String | **Yes** | `brch_cd` | Mã chi nhánh |
| `deviceUniqueId` | String | **Yes** | `cli_mac_addr` | Device ID |
| `sourceIp` | String | **Yes** | `cli_ip_addr` | Client IP |

### Key Differences: Equity vs Derivatives

| Feature | Equity | Derivatives |
|---------|--------|-------------|
| **Service** | lotte-bridge | tuxedo |
| **Auth** | OAuth2 + API KEY | API KEY only |
| **Sub-account** | Required (`subNumber`) | Not used |
| **Symbol field** | `stockCode` | `code` (ft_code) |
| **Price unit** | VND | Points (điểm) |
| **Lot size** | 100 | 1 |
| **Buy/Sell** | Single endpoint | Separate endpoints |

---

## Error Codes Reference

### Validation Errors (400)

| Code | Message (VI) | Message (EN) |
|------|--------------|--------------|
| `VALIDATION_ERROR` | Dữ liệu không hợp lệ | Invalid request data |
| `REQUIRED` | {field} là bắt buộc | {field} is required |
| `INVALID_FORMAT` | {field} không đúng định dạng | Invalid format |
| `INVALID_VALUE` | {field} có giá trị không hợp lệ | Invalid value |

### Authentication Errors (401)

| Code | Message (VI) | Message (EN) |
|------|--------------|--------------|
| `UNAUTHORIZED` | Chưa đăng nhập | Unauthorized |
| `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Session expired |
| `TOKEN_INVALID` | Token không hợp lệ | Invalid token |

### Authorization Errors (403)

| Code | Message (VI) | Message (EN) |
|------|--------------|--------------|
| `FORBIDDEN` | Không có quyền truy cập | Access denied |
| `UNAUTHORIZED_ACCOUNT` | Tài khoản không thuộc quyền sở hữu | Account not authorized |
| `ACCOUNT_LOCKED` | Tài khoản đã bị khóa | Account locked |

### Business Errors (422)

| Code | Message (VI) | Message (EN) |
|------|--------------|--------------|
| `MARKET_CLOSED` | Thị trường đã đóng cửa | Market closed |
| `PRICE_OUT_OF_RANGE` | Giá vượt quá biên độ | Price out of range |
| `INVALID_QUANTITY` | Khối lượng không hợp lệ | Invalid quantity |
| `INSUFFICIENT_BALANCE` | Không đủ tiền/ký quỹ | Insufficient balance |
| `INSUFFICIENT_STOCK` | Không đủ chứng khoán | Insufficient stock |
| `ORDER_NOT_FOUND` | Không tìm thấy lệnh | Order not found |
| `ORDER_ALREADY_MATCHED` | Lệnh đã khớp hoàn toàn | Order fully matched |
| `ORDER_ALREADY_CANCELLED` | Lệnh đã bị hủy | Order cancelled |
| `INVALID_ORDER_TYPE` | Loại lệnh không phù hợp | Invalid order type |
| `SYMBOL_NOT_FOUND` | Mã CK không tồn tại | Symbol not found |
| `SYMBOL_SUSPENDED` | Mã CK đang tạm ngừng GD | Symbol suspended |

### System Errors (500)

| Code | Message (VI) | Message (EN) |
|------|--------------|--------------|
| `INTERNAL_ERROR` | Lỗi hệ thống | Internal error |
| `LOTTE_API_ERROR` | Lỗi kết nối Lotte | Lotte connection error |
| `TIMEOUT` | Hết thời gian chờ | Request timeout |

### Lotte → TradeX Error Mapping

| Lotte Code | TradeX Code |
|------------|-------------|
| `0000` | Success |
| `1005` | `BUSINESS_ERROR` |
| `1006` | `INVALID_ORDER_TYPE` |
| `1007` | `PRICE_OUT_OF_RANGE` |
| `1008` | `INSUFFICIENT_BALANCE` |
| `1009` | `INSUFFICIENT_STOCK` |
| `1010` | `ORDER_NOT_FOUND` |
| `1011` | `ORDER_ALREADY_MATCHED` |
| `9999` | `INTERNAL_ERROR` |

---

## Appendix

### Field Validation Rules

| Field | Type | Min | Max | Format | Example |
|-------|------|-----|-----|--------|---------|
| `accountNumber` | String | 10 | 10 | Numeric | `"0001234567"` |
| `subNumber` | String | 2 | 2 | Numeric | `"01"` |
| `code` | String | 3 | 12 | Alphanumeric | `"VN30F2502"` |
| `stockCode` | String | 3 | 10 | Alphanumeric | `"VCB"` |
| `orderQuantity` | Number | 1 | 999999 | Integer | `5` |
| `orderPrice` | Number | >0 | - | Decimal | `1285.5` |
| `orderNumber` | String | 1 | 20 | Alphanumeric | `"2025020300012"` |
| `deviceUniqueId` | String | 1 | 50 | Alphanumeric | `"A1B2C3D4E5F6"` |
| `sourceIp` | String | 7 | 45 | IP address | `"192.168.1.100"` |

### Lot Size Rules

| Market | Lot Size | Example |
|--------|----------|---------|
| HOSE | 100 | 100, 200, 300... |
| HNX | 100 | 100, 200, 300... |
| Derivatives | 1 | 1, 2, 3... |

### Trading Sessions

**Equity (HOSE):**

| Session | Time | Order Types |
|---------|------|-------------|
| Pre-open | 08:30-09:00 | LO, ATO |
| ATO | 09:00-09:15 | LO, ATO |
| Continuous | 09:15-11:30, 13:00-14:30 | LO, MP, MOK, MAK |
| ATC | 14:30-14:45 | LO, ATC |
| PLO | 14:45-15:00 | LO |

**Derivatives:**

| Session | Time | Order Types |
|---------|------|-------------|
| ATO | 08:45-09:00 | LO, ATO |
| Continuous | 09:00-11:30, 13:00-14:30 | LO, MTL, MOK, MAK |
| ATC | 14:30-14:45 | LO, ATC |

### Source Files Reference

| Component | File |
|-----------|------|
| Derivatives Route | `rest-proxy-main/src/app/routes/api/derivatives/vcsc/Order.ts` |
| Equity Service | `lotte-bridge-main/src/services/OrderService.ts` |
| Equity DAO | `lotte-bridge-main/src/daos/LotteOrderDao.ts` |
| Lotte DR Docs | `Derivatives/Documentation/[API specs]Lotte_DR.md` |

---

**Document Status:** ✅ Complete  
**Version:** 2.1  
**Last Updated:** January 28, 2026  

**Changelog:**
- v2.1: Merged Request Fields + Order Types + Lotte Mapping into single table per API
- v2.0: Restructured - Merged request/response samples into each API section
- v1.1: Added TradeX API Standards, Request/Response Samples, Error Codes
- v1.0: Initial API mapping document
