# Regular Orders API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Orders - Regular Buy/Sell Orders  
**Version:** 2.0  
**Date:** February 5, 2026

> **Note:** Lotte-integrated APIs for **Derivatives only**. For Equity orders, see `@TradeX Knowledge/Planning/regular-order-api-mapping.md`

---

## 1. Overview

### 1.1 Purpose

Regular Orders (Derivatives) là các lệnh mua/bán thông thường (LO, ATO, ATC, MOK, MAK) được đẩy trực tiếp sang Lotte Core.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place Order | POST | `/api/v1/derivatives/order` |
| Modify Order | PUT | `/api/v1/derivatives/order/modify` |
| Cancel Order | PUT | `/api/v1/derivatives/order/cancel` |
| Query Unmatch | GET | `/api/v1/derivatives/order/todayUnmatch` |
| Query History | GET | `/api/v1/derivatives/order/history` |

### 1.3 Response Format Standards

**Success (Mutation):**
```
{
  "message": "[V0307] Message from Lotte",
  "orderNumber": "2026020500012"
}
```

**Success (Query):**
```
{
  "totalCount": 10,
  "orders": [...]
}
```

**Error:**
```
{
  "code": "ERROR_CODE",
  "message": "Error message" // For Lotte pass-through or Auth errors
}
```

or

```
{
  "code": "INVALID_PARAMETER",
  "params": [ ... ] // For validation errors
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Mutation: Minimal response (message from Lotte + orderNumber)
- Query: Rich data arrays
- Pass-through Lotte messages AS-IS (including `[CODE]` prefix)

---

## 2. Business Rules

### 2.1 Order Types

| Code | Name | Session (VN30F) | Price | Description |
|------|------|-----------------|-------|-------------|
| `LO` | Limit Order | All | Required | Lệnh giới hạn |
| `ATO` | At The Open | 09:00-09:15 | Optional (null for market) | Lệnh mở cửa |
| `ATC` | At The Close | 14:30-14:45 | Optional (null for market) | Lệnh đóng cửa |
| `MOK` | Market Or Kill | Continuous | null | Lệnh thị trường (khớp ngay hoặc hủy) |
| `MAK` | Market At Kill | Continuous | null | Lệnh market (khớp hết KL có thể) |
| `MTL` | Market To Limit | Continuous | null | Lệnh chuyển đổi (market → limit) |

### 2.2 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, symbolCode, sellBuyType, orderType, orderQuantity | `FIELD_IS_REQUIRED` |
| Price for LO | orderPrice MUST be provided if orderType = LO | `FIELD_IS_REQUIRED` |
| Price for Market | orderPrice MUST be null if orderType in [MOK, MAK, MTL] | `INVALID_VALUE` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

**Note:** Business rules (price limits, margin, position limits) are validated by Lotte Core.

### 2.3 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V0307] Bạn đã thực hiện lệnh Mua..."` |
| `en` | `E` | `"[E0307] Order placed successfully..."` |
| `ko` | `K` | `"[K0307] 주문 성공..."` |

---

## 3. API: Place Order (Buy/Sell)

### 3.1 Request

**Endpoint:** `POST /api/v1/derivatives/order`

**Lotte Endpoints (routed by `sellBuyType`):**
- **Buy:** `[RootURL]/tuxsvc/der/order/dr-buy-by-user` (DRORD-029)
- **Sell:** `[RootURL]/tuxsvc/der/order/dr-sell-by-user` (DRORD-030)

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `symbolCode` | String | ✅ | `ft_code` | Direct | Mã hợp đồng (VN30F2502) |
| `sellBuyType` | String | ✅ | - | **Routing only** | `BUY` → dr-buy-by-user<br>`SELL` → dr-sell-by-user |
| `orderType` | String | ✅ | `ord_type` | Map (see below) | Loại lệnh |
| `orderPrice` | Number | ❌ | `lm_ord_price` | Direct | Giá (null for market orders) |
| `orderQuantity` | Number | ✅ | `ord_qty` | Direct | Khối lượng |
| `deviceUniqueId` | String | ✅ | `cli_mac_addr` | Direct | Device ID |
| *(Request IP)* | - | - | `ip_addr` | Auto | Client IP |
| *(JWT)* `userId` | - | - | `user_id` | Auto | Username from token (max 15 chars) |
| *(Header)* | - | - | `lang_code` | Map (§2.3) | Language code (V/E/K) |
| - | - | - | `row_count` | Fixed: 500 | Số lượng bản ghi trả về |
| - | - | - | `next_key` | Fixed: "" | Empty for first request |

**Order Type Mapping:**

| TradeX `orderType` | Lotte `ord_type` | Note |
|--------------------|------------------|------|
| `LO` | `2` | Limit Order |
| `ATO` | `3` | At The Open |
| `MAK` | `4` | Market At Kill |
| `MOK` | `5` | Market Or Kill |
| `ATC` | `6` | At The Close |
| `MTL` | `9` | Market To Limit |

**Note:**
- Single TradeX endpoint for both Buy/Sell
- `lotte-bridge` routes to appropriate Lotte endpoint based on `sellBuyType`
- Same pattern as Equity orders

### 3.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | - | Check = `"0000"` | Success indicator |
| `error_desc` | `message` | Pass-through AS-IS | `"[V0307] Bạn đã thực hiện lệnh Mua..."` |
| `data_list[0].order_no` | `orderNumber` | Direct | `"2026020500012"` |

**Note:** 
- Message format: `"[{LANG}{CODE}] {Message}"`
- Pass-through Lotte message exactly (NO transformation)
- Language determined by `Accept-Language` header

**Error (422) - Lotte Business Rules:**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | `code` | Prefix: `ORDER_PLACE_{code}` | `"ORDER_PLACE_1005"` |
| `error_desc` | `message` | Pass-through AS-IS | `"[V3120] Không đủ ký quỹ..."` |

**Common Lotte Error Codes:**

| Lotte Code | TradeX Code | Description (Vietnamese) |
|------------|-------------|--------------------------|
| `1005` | `ORDER_PLACE_1005` | Không đủ ký quỹ |
| `2010` | `ORDER_PLACE_2010` | Vượt hạn mức giao dịch |
| `3005` | `ORDER_PLACE_3005` | Giá vượt biên độ |
| `4001` | `ORDER_PLACE_4001` | Thị trường đã đóng |
| `5002` | `ORDER_PLACE_5002` | Mã hợp đồng không hợp lệ |

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `symbolCode` | `FIELD_IS_REQUIRED` | `["symbolCode"]` | Missing |
| `sellBuyType` | `FIELD_IS_REQUIRED` | `["sellBuyType"]` | Missing |
| `sellBuyType` | `INVALID_VALUE` | `["sellBuyType", "value", "BUY/SELL"]` | Invalid value |
| `orderType` | `FIELD_IS_REQUIRED` | `["orderType"]` | Missing |
| `orderType` | `INVALID_VALUE` | `["orderType", "value", "LO/ATO/ATC/MOK/MAK/MTL"]` | Invalid value |
| `orderPrice` | `FIELD_IS_REQUIRED` | `["orderPrice"]` | Missing for LO |
| `orderPrice` | `MUST_BE_NULL` | `["orderPrice"]` | Provided for MOK/MAK/MTL |
| `orderQuantity` | `FIELD_IS_REQUIRED` | `["orderQuantity"]` | Missing |
| `orderQuantity` | `INVALID_VALUE` | `["orderQuantity", "value", ">0"]` | ≤ 0 |

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

---

## 4. API: Modify Order

### 4.1 Request

**Endpoint:** `PUT /api/v1/derivatives/order/modify`

**Lotte Endpoint:** `[RootURL]/tuxsvc/der/order/dr-mod-by-user` (DRORD-032)

**Lotte Doc:** DRORD-032

### 4.2 Request Mapping

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Số tài khoản |
| `orderNumber` | String | ✅ | `ord_no` | Số lệnh gốc (cần sửa) |
| `symbolCode` | String | ✅ | `ft_code` | Mã hợp đồng |
| `orderPrice` | Number | ✅ | `ord_price` | Giá mới |
| `orderQuantity` | Number | ✅ | `ord_qty` | Khối lượng mới |
| `unmatchedQuantity` | Number | ✅ | `un_mth_qty` | Khối lượng chưa khớp |
| `deviceUniqueId` | String | ❌ | `cli_mac_addr` | Device ID (optional) |
| *(Request IP)* | - | - | `ip_addr` | Client IP |
| *(JWT)* `userId` | - | - | `user_id` | Username from token |
| *(Header)* | - | - | `lang_code` | Language code |
| - | - | - | `validity` | Fixed: `"0"` (DAY) |
| - | - | - | `row_count` | Fixed: 500 |
| - | - | - | `next_key` | Fixed: "" |

**Note:** 
- Lotte sẽ cancel lệnh cũ và tạo lệnh mới
- `validity` luôn = `"0"` (DAY order)

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Description |
|-------------|--------------|-------------|
| `error_desc` | `message` | `"[V0318] Bạn đã sửa lệnh thành công"` |
| `data_list[0].order_no` | `orderNumber` | Số lệnh mới (sau khi sửa) |

**Error (422):** Same pattern as Buy/Sell, prefix = `ORDER_MODIFY_*`

---

## 5. API: Cancel Order

### 5.1 Request

**Endpoint:** `PUT /api/v1/derivatives/order/cancel`

**Lotte Endpoint:** `[RootURL]/tuxsvc/der/order/dr-can-by-user` (DRORD-031)

**Lotte Doc:** DRORD-031

### 5.2 Request Mapping

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Số tài khoản |
| `orderNumber` | String | ✅ | `ord_no` | Số lệnh cần hủy |
| `symbolCode` | String | ✅ | `ft_code` | Mã hợp đồng |
| `deviceUniqueId` | String | ❌ | `cli_mac_addr` | Device ID (optional) |
| *(Request IP)* | - | - | `ip_addr` | Client IP |
| *(JWT)* `userId` | - | - | `user_id` | Username from token |
| *(Header)* | - | - | `lang_code` | Language code |
| - | - | - | `validity` | Fixed: `"0"` (DAY) |
| - | - | - | `row_count` | Fixed: 500 |
| - | - | - | `next_key` | Fixed: "" |

### 5.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Description |
|-------------|--------------|-------------|
| `error_desc` | `message` | `"[V0320] Bạn đã hủy lệnh thành công"` |
| `data_list[0].order_no` | `orderNumber` | Số lệnh đã hủy |

**Error (422):** Same pattern, prefix = `ORDER_CANCEL_*`

---

## 6. API: Query Unmatch Orders

### 6.1 Request

**Endpoint:** `GET /api/v1/derivatives/order/todayUnmatch`

**Lotte Endpoint:** `[RootURL]/tuxsvc/der/order/dr-nmth-order` (DRORD-011)

**Lotte Doc:** DRORD-011

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |

### 6.2 Request Mapping

| TradeX Field | Lotte Field | Description |
|--------------|-------------|-------------|
| `accountNumber` | `is_actn_no` | Số tài khoản |

### 6.3 Response Mapping

**Success (200):**

| TradeX Field | Type | Description |
|--------------|------|-------------|
| `totalCount` | Number | Tổng số lệnh chờ |
| `orders` | Array | Danh sách lệnh (see Order Object) |

**Order Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `jmno` | `orderNumber` | String | Direct | Số hiệu lệnh |
| `acno` | `accountNumber` | String | Direct | Số TK |
| `acnm` | `accountName` | String | Direct | Tên TK |
| `code` | `symbolCode` | String | Direct | Mã hợp đồng |
| `mdms` | `sellBuyType` | String | `1→BUY`, `2→SELL` | Chiều |
| `type` | `orderType` | String | `1→MTL`, `2→LO` | Loại lệnh |
| `jprc` | `orderPrice` | Number | Direct | Giá |
| `jqty` | `orderQuantity` | Number | Direct | KL đặt |
| `cqty` | `matchedQuantity` | Number | Direct | KL khớp |
| `mqty` | `unmatchedQuantity` | Number | Direct | KL chưa khớp |
| `stat` | `status` | String | See below | Trạng thái |
| `jdtm` | `orderTime` | String | ISO 8601 | Thời gian đặt |

**Status Mapping:**

| Lotte `stat` / `mtst` | TradeX `status` | TradeX `statusName` |
|-----------------------|-----------------|---------------------|
| `New Order` / `1` | `PENDING` | Chờ khớp |
| `Edit Order` | `PENDING_MODIFY` | Chờ sửa |

**Note:** 
- Lotte response `type` mapping: `1→MTL`, `2→LO` (khác với request!)
- Lotte không trả về full order type list (chỉ có MTL và LO trong response)
- Order types khác (ATO, ATC, MOK, MAK) có thể không xuất hiện trong unmatch list

**Empty Result:**
```
{
  "totalCount": 0,
  "orders": []
}
```

---

## 7. API: Query Order History

### 7.1 Request

**Endpoint:** `GET /api/v1/derivatives/order/history`

**Lotte Endpoint:** `[RootURL]/tuxsvc/der/order/dr-order-history` (DRORD-010)

**Lotte Doc:** DRORD-010

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản |
| `date` | String | ✅ | Ngày tra cứu (yyyymmdd) |

### 7.2 Request Mapping

| TradeX Field | Lotte Field | Description |
|--------------|-------------|-------------|
| `accountNumber` | `acnt` | Số tài khoản |
| `date` | `date` | Ngày (yyyymmdd) |
| *(JWT)* `userId` | `hts_user_id` | Username from token |
| - | `next_data` | Fixed: "" (empty for first request) |

### 7.3 Response Mapping

**Success (200):**

| TradeX Field | Type | Description |
|--------------|------|-------------|
| `totalCount` | Number | Tổng số lệnh |
| `orders` | Array | Danh sách lệnh (see Order Object) |

**Order Object:**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `jmno` | `orderNumber` | String | Direct | Số hiệu lệnh |
| `ojno` | `originalOrderNumber` | String | Direct | Số lệnh gốc (nếu là sửa/hủy) |
| `code` | `symbolCode` | String | Direct | Mã hợp đồng |
| `mdms` | `sellBuyType` | String | `1→BUY`, `2→SELL` | Chiều |
| `type` | `orderType` | String | See below | Loại lệnh |
| `jprc` | `orderPrice` | Number | Direct | Giá |
| `jqty` | `orderQuantity` | Number | Direct | KL đặt |
| `cmqt` | `matchedQuantity` | Number | Direct | KL khớp |
| `mqty` | `unmatchedQuantity` | Number | Direct | KL chưa khớp |
| `dqty` | `cumulativeQuantity` | Number | Direct | KL khớp tích lũy |
| `jcgb` | `operation` | String | `New`/`Cancel`/`Edit` | Loại thao tác |
| `jmgb` | `validity` | String | See below | Loại lệnh (validity) |
| `time` | `orderTime` | String | ISO 8601 | Thời gian |
| `user` | `user` | String | Direct | User đặt lệnh |
| `rmsg` | `rejectReason` | String | Direct | Lý do từ chối (nếu có) |

**Order Type Mapping (from `jmgb`):**

| Lotte `jmgb` | TradeX `orderType` | Description |
|--------------|-------------------|-------------|
| `0` | `LO` | Day Order (Limit) |
| `2` | `ATO` | At The Open |
| `3` | `MOK` | 1OC (Market Or Kill) |
| `4` | `MAK` | FOK (Market At Kill) |
| `7` | `ATC` | At The Close |

**Note:** 
- `type` field in response có thể khác với request (Lotte internal mapping)
- Recommend dùng `jmgb` để map order type (consistent với request)

---

## 8. Error Handling Summary

### 8.1 Error Response Format

**Validation Error (400):**
```
{
  "code": "INVALID_PARAMETER",
  "params": [ 
    { "code": "FIELD_IS_REQUIRED", "param": "accountNumber", "messageParams": [...] }
  ]
}
```

**Auth Error (401/403):**
```
{
  "code": "UNAUTHORIZED" / "TOKEN_EXPIRED" / "FORBIDDEN" / "UNAUTHORIZED_ACCOUNT",
  "message": "Error message"
}
```

**Business Error (422) - Lotte Pass-Through:**
```
{
  "code": "ORDER_{OPERATION}_{LOTTE_CODE}",
  "message": "[V3120] Lotte error message"
}
```

**Server Error (500):**
```
{
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

### 8.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business | `ORDER_{OP}_{LOTTE_CODE}` | `ORDER_PLACE_1005`, `ORDER_MODIFY_6001` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

**Note:** `{OP}` values: `PLACE`, `MODIFY`, `CANCEL`

### 8.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|------------------|
| `1005` | Không đủ ký quỹ/tiền | Insufficient margin/funds |
| `2010` | Vượt hạn mức giao dịch | Exceeded trading limit |
| `3005` | Giá vượt biên độ | Price out of range |
| `4001` | Thị trường đã đóng | Market closed |
| `5002` | Mã không hợp lệ | Invalid symbol |
| `6001` | Không tìm thấy lệnh | Order not found |
| `7001` | Lệnh không thể sửa | Order cannot be modified |
| `7002` | Lệnh không thể hủy | Order cannot be cancelled |

---

## 9. Implementation Notes

### 9.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| `order-v2` | Business logic, validation (for TradeX-native features) |
| **Kafka** | Service communication |

### 9.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, data types, format, account ownership
- Lotte validates: Business rules (margin, price limits, trading hours)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Pass-through Lotte `error_desc` AS-IS (including `[CODE]` prefix)
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `ORDER_{OPERATION}_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `ORDER_BUY_1005`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
