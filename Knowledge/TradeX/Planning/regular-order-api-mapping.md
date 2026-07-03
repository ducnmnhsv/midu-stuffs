# TradeX Regular Order API Mapping

**Document Type:** API Analysis & Mapping  
**Category:** Orders - Regular Orders (Lệnh Thường)  
**Date:** February 3, 2026  
**Version:** 1.0

---

## Table of Contents

1. [Overview](#overview)
2. [Equity Regular Orders](#equity-regular-orders)
3. [Derivatives Regular Orders](#derivatives-regular-orders)
4. [API Mapping Reference](#api-mapping-reference)
5. [Field Mappings](#field-mappings)
6. [Order Types Reference](#order-types-reference)

---

## Overview

### Purpose

This document maps TradeX regular order APIs to Lotte backend APIs for both:
- **Equity Orders** (Cổ phiếu - Lệnh Thường)
- **Derivatives Orders** (Phái sinh - Lệnh Thường)

### Scope

Regular orders (Lệnh Thường) include:
- **Buy** (Mua)
- **Sell** (Bán)
- **Cancel** (Hủy)
- **Modify** (Sửa)

### Architecture Flow

```
Client/App → rest-proxy → lotte-bridge → Lotte Tuxedo API
```

---

## Equity Regular Orders

### 1. Buy Order (Lệnh Mua)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/equity/order` |
| **Service** | `lotte-bridge` |
| **File** | `lotte-bridge-main/src/services/OrderService.ts` (lines 108-200) |
| **Method** | `OrderService.enterNormalOrder()` |

#### Lotte API

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tsol/apikey/tuxsvc/order/ord-buy` |
| **Method** | POST |
| **Authentication** | OAuth2 + API KEY |
| **DAO Method** | `LotteOrderDao.enterBuyNormalOrder()` |

#### Request Structure

```typescript
{
  // Account Information
  accountNumber: string,        // Số tài khoản
  subNumber: string,            // Số tiểu khoản
  
  // Order Details
  stockCode: string,            // Mã chứng khoán
  orderQuantity: number,        // Khối lượng đặt
  orderPrice: number,           // Giá đặt
  orderType: string,            // Loại lệnh: LO, MP, ATO, ATC, MOK, MAK
  sellBuyType: "BUY",          // Loại mua/bán
  
  // Additional Info
  securitiesType: string,       // Loại chứng khoán
  bankCode: string,             // Mã ngân hàng
  deviceUniqueId: string,       // Device ID
  sourceIp: string              // IP nguồn
}
```

#### Response Structure

```typescript
{
  message: string,              // Thông báo kết quả
  orderNumber: string           // Số hiệu lệnh
}
```

#### Lotte Request Mapping

| TradeX Field | Lotte Field | Type | Required | Notes |
|--------------|-------------|------|----------|-------|
| `accountNumber` | `acnt_no` | String | Y | Số tài khoản |
| `subNumber` | `sub_no` | String | Y | Số tiểu khoản |
| `stockCode` | `stk_cd` | String | Y | Mã chứng khoán |
| `orderQuantity` | `ord_qty` | Number | Y | Khối lượng |
| `orderPrice` | `ord_pri` | Number | Y | Giá đặt |
| `orderType` | `ord_tp` | String | Y | LO/MP/ATO/ATC/MOK/MAK |
| `bankCode` | `bank_cd` | String | N | Mã ngân hàng |
| `deviceUniqueId` | `cli_mac_addr` | String | N | MAC address |
| `sourceIp` | `cli_ip_addr` | String | N | IP address |

---

### 2. Sell Order (Lệnh Bán)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/equity/order` |
| **Service** | `lotte-bridge` |
| **File** | `lotte-bridge-main/src/services/OrderService.ts` (lines 108-200) |
| **Method** | `OrderService.enterNormalOrder()` |

#### Lotte API

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tsol/apikey/tuxsvc/order/ord-sell` |
| **Method** | POST |
| **Authentication** | OAuth2 + API KEY |
| **DAO Method** | `LotteOrderDao.enterSellNormalOrder()` |

#### Request Structure

Same as Buy Order, with `sellBuyType: "SELL"`

#### Response Structure

Same as Buy Order

---

### 3. Cancel Order (Hủy Lệnh)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/equity/order/cancel` |
| **Service** | `lotte-bridge` |
| **File** | `lotte-bridge-main/src/services/OrderService.ts` (lines 202-249) |
| **Method** | `OrderService.cancelNormalOrder()` |

#### Lotte API

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tsol/apikey/tuxsvc/order/ord-can` |
| **Method** | PUT/POST |
| **Authentication** | OAuth2 + API KEY |
| **DAO Method** | `LotteOrderDao.cancelNormalOrder()` |

#### Request Structure

```typescript
{
  accountNumber: string,        // Số tài khoản
  subNumber: string,            // Số tiểu khoản
  orderNumber: string,          // Số hiệu lệnh cần hủy
  branchCode: string,           // Mã chi nhánh
  deviceUniqueId: string,       // Device ID
  sourceIp: string              // IP nguồn
}
```

#### Response Structure

```typescript
{
  orderNumber: string           // Số hiệu lệnh đã hủy
}
```

#### Lotte Request Mapping

| TradeX Field | Lotte Field | Type | Required | Notes |
|--------------|-------------|------|----------|-------|
| `accountNumber` | `acnt_no` | String | Y | Số tài khoản |
| `subNumber` | `sub_no` | String | Y | Số tiểu khoản |
| `orderNumber` | `ord_no` | String | Y | Số hiệu lệnh |
| `branchCode` | `brch_cd` | String | Y | Mã chi nhánh |
| `deviceUniqueId` | `cli_mac_addr` | String | N | MAC address |
| `sourceIp` | `cli_ip_addr` | String | N | IP address |

---

### 4. Modify Order (Sửa Lệnh)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/equity/order/modify` |
| **Service** | `lotte-bridge` |
| **File** | `lotte-bridge-main/src/services/OrderService.ts` (lines 251-306) |
| **Method** | `OrderService.modifyNormalOrder()` |

#### Lotte API

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tsol/apikey/tuxsvc/order/ord-mod` |
| **Method** | PUT/POST |
| **Authentication** | OAuth2 + API KEY |
| **DAO Method** | `LotteOrderDao.modifyNormalOrder()` |

#### Request Structure

```typescript
{
  accountNumber: string,        // Số tài khoản
  subNumber: string,            // Số tiểu khoản
  orderNumber: string,          // Số hiệu lệnh cần sửa
  orderPrice: number,           // Giá mới
  orderQuantity: number,        // Khối lượng mới
  branchCode: string,           // Mã chi nhánh
  deviceUniqueId: string,       // Device ID
  sourceIp: string              // IP nguồn
}
```

#### Response Structure

```typescript
{
  orderNumber: string           // Số hiệu lệnh mới (sau khi sửa)
}
```

#### Lotte Request Mapping

| TradeX Field | Lotte Field | Type | Required | Notes |
|--------------|-------------|------|----------|-------|
| `accountNumber` | `acnt_no` | String | Y | Số tài khoản |
| `subNumber` | `sub_no` | String | Y | Số tiểu khoản |
| `orderNumber` | `ord_no` | String | Y | Số hiệu lệnh |
| `orderPrice` | `ord_pri` | Number | Y | Giá mới |
| `orderQuantity` | `ord_qty` | Number | Y | Khối lượng mới |
| `branchCode` | `brch_cd` | String | Y | Mã chi nhánh |
| `deviceUniqueId` | `cli_mac_addr` | String | N | MAC address |
| `sourceIp` | `cli_ip_addr` | String | N | IP address |

---

## Derivatives Regular Orders

### 1. Buy Order (Lệnh Mua)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/derivatives/order` |
| **Service** | `rest-proxy` → `tuxedo` |
| **Route File** | `rest-proxy-main/src/app/routes/api/derivatives/vcsc/Order.ts` |

#### Lotte API (DRORD-029)

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tuxsvc/der/order/dr-buy-by-user` |
| **Method** | POST |
| **Authentication** | API KEY |
| **Lotte Doc** | `Lotte_DR_API_Specs.md` lines 628-653 |

#### Request Structure

```typescript
{
  // Account Information
  acnt_no: string,              // Số tài khoản
  
  // Order Details
  ft_code: string,              // Mã hợp đồng phái sinh
  ord_qty: number,              // Khối lượng đặt lệnh
  lm_ord_price: number,         // Giá đặt
  ord_type: string,             // 2:LO, 3:ATO, 4:MAK, 5:MOK, 6:ATC, 9:MTL
  
  // Additional Info
  ip_addr: string,              // IP
  user_id: string,              // User ID (max 15 chars)
  cli_mac_addr: string,         // MAC address
  
  // Optional
  lang_code?: string,           // V/E/K (default: V)
  row_count?: number,           // Default: 500
  next_key?: string             // Pagination
}
```

#### Response Structure

```typescript
{
  error_code: string,           // "0000": Success, "1005": Failed
  error_desc: string,           // Error description
  success: boolean,             // true/false
  data_list: {
    order_no: string            // Số hiệu lệnh
  }
}
```

#### TradeX to Lotte Field Mapping

| TradeX Field | Lotte Field | Type | Required | Notes |
|--------------|-------------|------|----------|-------|
| `accountNumber` | `acnt_no` | String | Y | Số tài khoản |
| `code` | `ft_code` | String | Y | Mã hợp đồng (VN30F2402, etc.) |
| `orderQuantity` | `ord_qty` | Double | Y | Khối lượng |
| `orderPrice` | `lm_ord_price` | Float | Y | Giá đặt |
| `orderType` | `ord_type` | String | Y | 2:LO, 3:ATO, 4:MAK, 5:MOK, 6:ATC, 9:MTL |
| `sellBuyType` | N/A | String | N/A | Implicit (BUY endpoint) |
| N/A | `user_id` | String | Y | From session/auth |
| `sourceIp` | `ip_addr` | String | Y | Client IP |
| `deviceUniqueId` | `cli_mac_addr` | String | Y | MAC address |

---

### 2. Sell Order (Lệnh Bán)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/derivatives/order` |
| **Service** | `rest-proxy` → `tuxedo` |

#### Lotte API (DRORD-030)

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tuxsvc/der/order/dr-sell-by-user` |
| **Method** | POST |
| **Authentication** | API KEY |
| **Lotte Doc** | `Lotte_DR_API_Specs.md` lines 655-680 |

#### Request Structure

Same as Buy Order (DRORD-029)

#### Response Structure

Same as Buy Order

---

### 3. Cancel Order (Hủy Lệnh)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/derivatives/order/cancel` |
| **Service** | `rest-proxy` → `tuxedo` |

#### Lotte API (DRORD-031)

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tuxsvc/der/order/dr-can-by-user` |
| **Method** | POST |
| **Authentication** | API KEY |
| **Lotte Doc** | `Lotte_DR_API_Specs.md` lines 682-706 |

#### Request Structure

```typescript
{
  // Account Information
  acnt_no: string,              // Số tài khoản
  
  // Order Identification
  ord_no: string,               // Số hiệu lệnh
  ft_code: string,              // Mã hợp đồng
  validity: string,             // 0:DAY
  
  // Additional Info
  ip_addr: string,              // IP
  user_id: string,              // User ID (max 15 chars)
  cli_mac_addr?: string,        // MAC address (Optional)
  
  // Optional
  lang_code?: string,           // V/E/K (default: V)
  row_count?: number,           // Default: 500
  next_key?: string             // Pagination
}
```

#### Response Structure

```typescript
{
  error_code: string,           // "0000": Success, "1005": Failed
  error_desc: string,
  success: boolean,
  data_list: {
    order_no: string            // Số hiệu lệnh
  }
}
```

#### TradeX to Lotte Field Mapping

| TradeX Field | Lotte Field | Type | Required | Notes |
|--------------|-------------|------|----------|-------|
| `accountNumber` | `acnt_no` | String | Y | Số tài khoản |
| `orderNumber` | `ord_no` | String | Y | Số hiệu lệnh cần hủy |
| `code` | `ft_code` | String | Y | Mã hợp đồng |
| N/A | `validity` | String | Y | Always "0" (DAY) |
| `sourceIp` | `ip_addr` | String | Y | Client IP |
| N/A | `user_id` | String | Y | From session/auth |
| `deviceUniqueId` | `cli_mac_addr` | String | N | MAC address |

---

### 4. Modify Order (Sửa Lệnh)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/derivatives/order/modify` |
| **Service** | `rest-proxy` → `tuxedo` |

#### Lotte API (DRORD-032)

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tuxsvc/der/order/dr-mod-by-user` |
| **Method** | POST |
| **Authentication** | API KEY |
| **Lotte Doc** | `Lotte_DR_API_Specs.md` lines 708-735 |

#### Request Structure

```typescript
{
  // Account Information
  acnt_no: string,              // Số tài khoản
  
  // Order Identification
  ord_no: string,               // Số hiệu lệnh
  ft_code: string,              // Mã hợp đồng
  validity: string,             // 0:DAY
  
  // New Order Details
  ord_qty: number,              // Khối lượng mới
  ord_price: number,            // Giá mới
  un_mth_qty: number,           // Khối lượng chưa khớp
  
  // Additional Info
  ip_addr: string,              // IP
  user_id: string,              // User ID (max 15 chars)
  cli_mac_addr?: string,        // MAC address (Optional)
  
  // Optional
  lang_code?: string,           // V/E/K (default: V)
  row_count?: number,           // Default: 500
  next_key?: string             // Pagination
}
```

#### Response Structure

```typescript
{
  error_code: string,           // "0000": Success, "1005": Failed
  error_desc: string,
  success: boolean,
  data_list: {
    order_no: string            // Số hiệu lệnh (mới)
  }
}
```

#### TradeX to Lotte Field Mapping

| TradeX Field | Lotte Field | Type | Required | Notes |
|--------------|-------------|------|----------|-------|
| `accountNumber` | `acnt_no` | String | Y | Số tài khoản |
| `orderNumber` | `ord_no` | String | Y | Số hiệu lệnh cần sửa |
| `code` | `ft_code` | String | Y | Mã hợp đồng |
| `orderQuantity` | `ord_qty` | Double | Y | Khối lượng mới |
| `orderPrice` | `ord_price` | Float | Y | Giá mới |
| `unmatchedQuantity` | `un_mth_qty` | Double | Y | KL chưa khớp |
| N/A | `validity` | String | Y | Always "0" (DAY) |
| `sourceIp` | `ip_addr` | String | Y | Client IP |
| N/A | `user_id` | String | Y | From session/auth |
| `deviceUniqueId` | `cli_mac_addr` | String | N | MAC address |

---

### 5. Query Unmatch Orders (Lệnh có thể hủy/sửa)

#### TradeX API

| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/derivatives/order/todayUnmatch` |
| **Service** | `rest-proxy` → `tuxedo` |

#### Lotte API (DRORD-011)

| Property | Value |
|----------|-------|
| **Endpoint** | `[RootURL]/tuxsvc/der/order/dr-nmth-order` |
| **Method** | POST |
| **Authentication** | API KEY |
| **Lotte Doc** | `Lotte_DR_API_Specs.md` lines 483-513 |

#### Request Structure

```typescript
{
  is_actn_no: string            // Số tài khoản
}
```

#### Response Structure

```typescript
{
  error_code: string,
  error_desc: string,
  success: boolean,
  data_list: [
    {
      acno: string,             // Số TK
      acnm: string,             // Tên TK
      jmno: string,             // Số hiệu lệnh
      stat: string,             // New Order / Edit Order
      mtst: string,             // 1: Lệnh Mới
      type: string,             // 1:MTL, 2:LO
      code: string,             // Mã hợp đồng
      mdms: string,             // Buy/Sell (1/2)
      jqty: string,             // Khối lượng
      jprc: string,             // Giá
      cqty: string,             // KL khớp
      mqty: string,             // KL chưa khớp
      jmgb: string,             // 0:DAY, 2:ATO, 3:IOC, 4:FOK, 7:ATC
      ord_style: string,        // Không dùng
      os_next_key: string       // Không dùng
    }
  ]
}
```

#### Purpose

Returns list of orders that can be cancelled or modified (lệnh có thể hủy/sửa).

---

## API Mapping Reference

### Quick Reference Table

| Operation | TradeX Endpoint | Lotte Endpoint | Lotte Code |
|-----------|----------------|----------------|------------|
| **EQUITY** | | | |
| Buy | `POST /api/v1/equity/order` | `tsol/.../ord-buy` | N/A |
| Sell | `POST /api/v1/equity/order` | `tsol/.../ord-sell` | N/A |
| Cancel | `PUT /api/v1/equity/order/cancel` | `tsol/.../ord-can` | N/A |
| Modify | `PUT /api/v1/equity/order/modify` | `tsol/.../ord-mod` | N/A |
| **DERIVATIVES** | | | |
| Buy | `POST /api/v1/derivatives/order` | `.../dr-buy-by-user` | DRORD-029 |
| Sell | `POST /api/v1/derivatives/order` | `.../dr-sell-by-user` | DRORD-030 |
| Cancel | `PUT /api/v1/derivatives/order/cancel` | `.../dr-can-by-user` | DRORD-031 |
| Modify | `PUT /api/v1/derivatives/order/modify` | `.../dr-mod-by-user` | DRORD-032 |
| Query Unmatch | `GET /api/v1/derivatives/order/todayUnmatch` | `.../dr-nmth-order` | DRORD-011 |

---

## Field Mappings

### Common Field Abbreviations (Lotte API)

| Abbreviation | Full Name | Vietnamese | Type |
|--------------|-----------|------------|------|
| `acnt_no` / `acnt` | Account Number | Số tài khoản | String |
| `sub_no` | Sub Account | Số tiểu khoản | String |
| `ord_no` / `jmno` | Order Number | Số hiệu lệnh | String |
| `ord_qty` / `jqty` | Order Quantity | Khối lượng đặt | Number |
| `ord_pri` / `jprc` | Order Price | Giá đặt | Number |
| `ord_tp` / `ord_type` | Order Type | Loại lệnh | String |
| `stk_cd` | Stock Code | Mã chứng khoán | String |
| `ft_code` / `code` | Futures Code | Mã hợp đồng phái sinh | String |
| `brch_cd` | Branch Code | Mã chi nhánh | String |
| `bank_cd` | Bank Code | Mã ngân hàng | String |
| `mdms` | Market Side | Buy/Sell | String |
| `cqty` / `cmqt` | Matched Quantity | Khối lượng khớp | Number |
| `mqty` | Unmatched Quantity | KL chưa khớp | Number |
| `un_mth_qty` | Unmatched Quantity | KL chưa khớp | Number |
| `cli_mac_addr` | MAC Address | Địa chỉ MAC | String |
| `cli_ip_addr` / `ip_addr` | IP Address | Địa chỉ IP | String |
| `user_id` / `hts_user_id` | User ID | Tài khoản người thực hiện | String |
| `validity` / `jmgb` | Validity Type | Hiệu lực lệnh | String |

---

## Order Types Reference

### Equity Order Types

| Code | Full Name | Vietnamese | Description |
|------|-----------|------------|-------------|
| `LO` | Limit Order | Lệnh giới hạn | Order at specified price |
| `MP` | Market Price | Lệnh thị trường | Order at best market price |
| `ATO` | At-The-Opening | Lệnh khớp mở cửa | Match at opening session |
| `ATC` | At-The-Close | Lệnh khớp đóng cửa | Match at closing session |
| `MOK` | Market Or Kill | Lệnh thị trường hoặc hủy | Market order or cancel |
| `MAK` | Market At Kill | Lệnh thị trường và hủy | Market order and cancel remainder |
| `PLO` | Post Limit Order | Lệnh thỏa thuận | Post-trading session limit order |

### Derivatives Order Types (Lotte Code)

| Lotte Code | Order Type | Vietnamese | Description |
|------------|------------|------------|-------------|
| `2` | LO | Lệnh giới hạn | Limit Order |
| `3` | ATO | Lệnh khớp mở cửa | At-The-Opening |
| `4` | MAK | Market At Kill | Market At Kill |
| `5` | MOK | Market Or Kill | Market Or Kill |
| `6` | ATC | Lệnh khớp đóng cửa | At-The-Close |
| `9` | MTL | Market To Limit | Market To Limit |

### Validity Types (jmgb)

| Code | Full Name | Vietnamese | Description |
|------|-----------|------------|-------------|
| `0` | DAY | Lệnh ngày | Valid for current trading day |
| `2` | ATO | Mở cửa | At-The-Opening |
| `3` | IOC | Immediate Or Cancel | Khớp ngay hoặc hủy |
| `4` | FOK | Fill Or Kill | Khớp hết hoặc hủy |
| `7` | ATC | Đóng cửa | At-The-Close |

### Sell/Buy Type

| Code | TradeX | Lotte (mdms) | Vietnamese |
|------|--------|--------------|------------|
| BUY | `"BUY"` | `"1"` | Mua |
| SELL | `"SELL"` | `"2"` | Bán |

---

## Implementation Notes

### Service Architecture

**Equity Orders**:
- Service: `lotte-bridge` (TypeScript/Node.js)
- Flow: `rest-proxy` → `lotte-bridge` → Lotte Tuxedo API
- Authentication: OAuth2 + API KEY

**Derivatives Orders**:
- Service: `rest-proxy` → `tuxedo`
- Flow: `rest-proxy` → `tuxedo` service → Lotte Tuxedo API
- Authentication: API KEY only

### Error Handling

All Lotte APIs return:
```typescript
{
  error_code: string,    // "0000": Success, "1005": Failed
  error_desc: string,    // Error description in Vietnamese/English
  success: boolean       // true/false
}
```

### Key Differences: Equity vs Derivatives

| Feature | Equity | Derivatives |
|---------|--------|-------------|
| Service | lotte-bridge | tuxedo |
| Auth | OAuth2 + API KEY | API KEY only |
| Sell/Buy distinction | Single endpoint + `sellBuyType` field | Separate endpoints (dr-buy/dr-sell) |
| Symbol field | `stk_cd` (stock code) | `ft_code` (futures contract) |
| Sub-account | Required (`sub_no`) | Not used |
| Lotte Doc Codes | No specific codes | DRORD-029/030/031/032/011 |

---

## Source Files Reference

### Equity Orders

| Component | File Path |
|-----------|-----------|
| Service | `lotte-bridge-main/src/services/OrderService.ts` |
| DAO | `lotte-bridge-main/src/daos/LotteOrderDao.ts` |
| API Constants | `lotte-bridge-main/src/constants/LotteAPI.ts` |

### Derivatives Orders

| Component | File Path |
|-----------|-----------|
| Route (VCSC) | `rest-proxy-main/src/app/routes/api/derivatives/vcsc/Order.ts` |
| Route (KIS) | `rest-proxy-main/src/app/routes/api/derivatives/kis/Order.ts` |
| Model | `rest-proxy-main/src/app/models/api/derivatives/kis/Order.ts` |
| Lotte Documentation | `Derivatives/Documentation/Lotte_DR_API_Specs.md` |

### Documentation

| Document | Lines | Content |
|----------|-------|---------|
| Lotte DR API | 628-653 | DRORD-029: Buy Order |
| Lotte DR API | 655-680 | DRORD-030: Sell Order |
| Lotte DR API | 682-706 | DRORD-031: Cancel Order |
| Lotte DR API | 708-735 | DRORD-032: Modify Order |
| Lotte DR API | 483-513 | DRORD-011: Query Unmatch Orders |

---

## Related Documents

- [TradeX Knowledge Index](../_index.md)
- [Market Data Channels](../System/market-data-channels.md)
- [Order V2 Documentation](../../TradeX-MCP/order-v2-main/ORDER_V2_DOCUMENTATION.md) (Conditional Orders)
- [Lotte DR API Specs](../../../Derivatives/Documentation/Lotte_DR_API_Specs.md)

---

**Document Status:** ✅ Complete  
**Last Updated:** February 3, 2026  
**Reviewed By:** PM - TradeX Analytics  
**Next Review:** When new order types are added
