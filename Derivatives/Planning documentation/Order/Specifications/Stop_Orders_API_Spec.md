# Stop Orders API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Orders - Stop Order (Lệnh Điều Kiện)  
**Version:** 1.0  
**Date:** February 11, 2026

> **Note:** Lotte-integrated APIs. Mapping: 1 URI cho mỗi operation (Place/Modify/Cancel), route theo sellBuyType giống Regular Order.

---

## 1. Overview

### 1.1 Purpose

Stop Order (Lệnh điều kiện) là lệnh mua/bán phái sinh được kích hoạt khi giá thị trường đạt mức trigger. TradeX expose **4 nhóm API**: Place, Modify, Cancel (Lotte DRORD-005, 006, 023, 024, 025, 026) và **Query Stop Order History** (Lotte DRORD-016).

### 1.2 Smart Mapping Summary

| TradeX Operation | TradeX Endpoint | Method | Lotte APIs | Routing Logic |
|-----------------|-----------------|--------|------------|---------------|
| **Place** | `/api/v1/derivatives/stopOrder` | POST | DRORD-005, 006 | `sellBuyType=BUY` → dr-stop-order-buy<br>`sellBuyType=SELL` → dr-stop-order-sell |
| **Modify** | `/api/v1/derivatives/stopOrder/modify` | PUT | DRORD-023, 024 | 1 URL chung `dr-replace-stop-order` |
| **Cancel** | `/api/v1/derivatives/stopOrder/cancel` | PUT | DRORD-025, 026 | 1 URL chung: `dr-cancel-stop-order` |
| **Query History** | `/api/v1/derivatives/stopOrder/history` | GET | DRORD-016 | Tra cứu lịch sử lệnh điều kiện trong ngày |

### 1.3 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place Stop Order | POST | `/api/v1/derivatives/stopOrder` |
| Modify Stop Order | PUT | `/api/v1/derivatives/stopOrder/modify` |
| Cancel Stop Order | PUT | `/api/v1/derivatives/stopOrder/cancel` |
| Query Stop Order History | GET | `/api/v1/derivatives/stopOrder/history` |

### 1.4 Response Format Standards

**Success (Mutation - Place/Modify/Cancel):**
```json
{
  "message": "[V0307] Message from Lotte",
  "orderDate": "20260211",
  "orderSeqNo": "001234"
}
```

**Note:** TradeX map 1-1 theo Core: định danh Stop order bằng hai field `orderDate` (yyyyMMdd) + `orderSeqNo` (số hiệu). Xem §2.4.

**Success (Query - Stop Order History):**
```json
{
  "totalCount": 10,
  "orders": [ { "orderDate": "20260211", "orderSeqNo": "00001", "symbolCode": "VN30F2502", ... } ]
}
```

**Error:**
```json
{
  "code": "STOP_ORDER_PLACE_1005",
  "message": "[V3120] Lotte error message"
}
```

**Principles:** Giống Regular Orders (xem `Regular_Orders_API_Spec.md` §1.3) - HTTP status = success indicator, NO `success: true/false`, pass-through Lotte messages AS-IS, Mutation = minimal (message + orderDate + orderSeqNo).

---

## 2. Business Rules

### 2.1 Stop Order Characteristics

| TradeX Field | Lotte Field | Mô tả |
|--------------|-------------|-------|
| `orderPrice` | `ord_pri` | Giá |
| `priceBand` | `ord_band_pri` | Bước giá |
| - | `from_dt`, `end_dt` | Ngày (cùng giá trị) |

### 2.2 Validation Rules (TradeX BE)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber, symbolCode, sellBuyType, orderPrice, orderQuantity, priceBand | `FIELD_IS_REQUIRED` |
| sellBuyType | BUY hoặc SELL only | `INVALID_VALUE` |
| orderPrice | > 0, tick size compliant | `INVALID_VALUE` |
| orderQuantity | > 0, integer | `INVALID_VALUE` |
| priceBand | Required, gửi lên Lotte | `FIELD_IS_REQUIRED` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

**Note:** Mapping 1-1 với Lotte. Logic UX (user nhập gì, FE tính gì) → FE Issues: [Derivatives_Stop_Order_Integration](../../../FE%20Implementation/Order/Issues/Derivatives_Stop_Order_Integration.md), [Stop_Order_Screen_FE_Requirement](../../../FE%20Implementation/Order/Issues/Stop_Order_Screen_FE_Requirement.md).

**Note:** Business rules (margin, price limits) validated by Lotte Core.

### 2.3 Language Mapping

| Accept-Language | Lotte lang_code |
|-----------------|-----------------|
| `vi` | `V` |
| `en` | `E` |
| `ko` | `K` |

### 2.4 Order Identification (Stop Order) — Map 1-1 theo Core

Core (Lotte) định danh Stop order bằng **hai field**: `date` (yyyyMMdd) + `seqn` (số hiệu). **TradeX map 1-1** — không dùng composite:

| TradeX Field | Type | Lotte Field | Mô tả |
|--------------|------|-------------|-------|
| `orderDate` | String | `date` | Ngày đặt lệnh (yyyyMMdd) |
| `orderSeqNo` | String | `seqn` | Số hiệu lệnh (sequence) |

**Áp dụng:** Place response trả `orderDate` + `orderSeqNo`; Modify/Cancel request nhận `orderDate` + `orderSeqNo`; Query History mỗi item có `orderDate` + `orderSeqNo`. BE pass-through trực tiếp sang Lotte, không parse/composite.

---

## 3. API: Place Stop Order

### 3.1 Request

**Endpoint:** `POST /api/v1/derivatives/stopOrder`

**Lotte Endpoints (routed by `sellBuyType`):**
- **Buy:** `[RootURL]/tuxsvc/der/order/dr-stop-order-buy` (DRORD-005)
- **Sell:** `[RootURL]/tuxsvc/der/order/dr-stop-order-sell` (DRORD-006)

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional)

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `symbolCode` | String | ✅ | `stk_cd` | Direct | Mã hợp đồng (VN30F2502) |
| `sellBuyType` | String | ✅ | - | **Routing only** | `BUY` → dr-stop-order-buy<br>`SELL` → dr-stop-order-sell |
| `orderPrice` | Number | ✅ | `ord_pri` | Direct | Giá |
| `orderQuantity` | Number | ✅ | `ord_qty` | String | Khối lượng |
| `priceBand` | Number | ✅ | `ord_band_pri` | String | Bước giá |
| `orderDate` | String | ❌ | `from_dt`, `end_dt` | yyyyMMdd | Mặc định: today |
| `deviceUniqueId` | String | ❌ | `cli_mac_addr` | Direct | Device ID |
| *(Request IP)* | - | - | - | - | Lotte có thể dùng |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | Username from token |
| *(Header)* | - | - | `lang_code` | Map (§2.3) | Language |
| - | - | - | `mdm_tp` | **Derived** | Kênh thực hiện – derive từ platform/channel (giống Equity) |

**mdm_tp Logic (Áp dụng cho mọi API Derivatives cần mdm_tp):**
- FE **không** gửi `mdm_tp` trong request body.
- Backend derive từ: `request.channel` → `token.platform` → default.
- Map qua `getPlatformValueCore(platform)` → 31 (IOS), 32 (Android), 42 (PAAVE/DIFISOFT), `%` (default).
- Chi tiết: [TradeX API Conventions - mdm_tp](../../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md#11-mdm_tp-kênh-thực-hiện--derived-fe-không-truyền)

**orderDate Logic:**
- Nếu không truyền: dùng ngày hiện tại (yyyyMMdd)
- `from_dt` = `end_dt` = `orderDate` (Lotte yêu cầu cùng giá trị)

### 3.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | - | Check = `"0000"` | Success indicator |
| `error_desc` | `message` | Pass-through AS-IS | Lotte message |
| `data_list` (date, seq_no) | `orderDate`, `orderSeqNo` | Direct 1-1 | Map 1-1 theo Core (§2.4) |

**Note:** Lotte DRORD-005/006 DataResponse không mô tả chi tiết. Cần xác nhận field trả về (date, seq_no). TradeX trả đúng hai field `orderDate`, `orderSeqNo` như Lotte.

**Error (422):** Prefix `STOP_ORDER_PLACE_{LOTTE_CODE}`

### 3.4 Error Mapping

**Validation Error (400):**

| Field | Error Code | Condition |
|-------|------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | Missing |
| `symbolCode` | `FIELD_IS_REQUIRED` | Missing |
| `sellBuyType` | `FIELD_IS_REQUIRED` | Missing |
| `sellBuyType` | `INVALID_VALUE` | Not BUY/SELL |
| `orderPrice` | `FIELD_IS_REQUIRED` | Missing |
| `orderPrice` | `INVALID_VALUE` | ≤ 0 hoặc không đúng tick size |
| `orderQuantity` | `FIELD_IS_REQUIRED` | Missing |
| `priceBand` | `FIELD_IS_REQUIRED` | Missing |

---

## 4. API: Modify Stop Order

### 4.1 Request

**Endpoint:** `PUT /api/v1/derivatives/stopOrder/modify`

**Lotte Endpoint:** `[RootURL]/tuxsvc/der/order/dr-replace-stop-order` (DRORD-023, 024)

**Single URL** cho cả Mua và Bán - Lotte xử lý theo seqn.

### 4.2 Request Mapping

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acno` | Direct | Số tài khoản |
| `orderDate` | String | ✅ | `date` | Direct | Ngày đặt lệnh (yyyyMMdd) |
| `orderSeqNo` | String | ✅ | `seqn` | Direct | Số hiệu lệnh |
| `orderPrice` | Number | ✅ | `jprc` | Direct | Giá mới |
| `orderQuantity` | Number | ✅ | `jqty` | Direct | Khối lượng mới |
| `priceBand` | Number | ✅ | `bprc` | Direct | Bước giá mới |
| `validFromDate` | String | ❌ | `sdate` | Default = orderDate | Ngày bắt đầu (sdate = edate) |
| `validToDate` | String | ❌ | `edate` | Default = validFromDate | Ngày kết thúc |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | Username from token |

**Order Identification:** Map 1-1 theo Core (§2.4). FE gửi `orderDate` + `orderSeqNo`; BE chuyển thẳng sang Lotte `date`, `seqn`.

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field |
|-------------|--------------|
| `error_desc` | `message` |
| - | Echo lại `orderDate`, `orderSeqNo` từ request (map 1-1) |

**Error (422):** Prefix `STOP_ORDER_MODIFY_*`

---

## 5. API: Cancel Stop Order

### 5.1 Request

**Endpoint:** `PUT /api/v1/derivatives/stopOrder/cancel`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/order/dr-cancel-stop-order` (DRORD-025, 026) — xem [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) §2.3.7

**Request body:** accountNumber, orderDate, orderSeqNo.

### 5.2 Request Mapping

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | ✅ | `acno` | Số tài khoản |
| `orderDate` | String | ✅ | `date` | Ngày đặt lệnh (yyyyMMdd) |
| `orderSeqNo` | String | ✅ | `seqn` | Số hiệu lệnh |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto |

### 5.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field |
|-------------|--------------|
| `error_desc` | `message` |
| - | Echo lại `orderDate`, `orderSeqNo` từ request (map 1-1) |

**Error (422):** Prefix `STOP_ORDER_CANCEL_*`

---

## 6. API: Query Stop Order History (DRORD-016)

**Lotte API:** DRORD-016 — Tra cứu Lịch sử lệnh điều kiện trong ngày.  
**Lotte URL:** `[RootURL]/tsol/apikey/tuxsvc/der/order/dr-condition-ord-in-day` (Lotte_DR §2.3.5). Method: POST hoặc GET. Auth: OAuth2 + API KEY.

### 6.1 Request

**Endpoint:** `GET /api/v1/derivatives/stopOrder/history`

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional)

**Query Parameters:**

Đối với API GET, TradeX hỗ trợ thêm **fetchCount** và **nextKey** (cả hai **không required**). Xem TradeX Knowledge § GET API optional params.

**TradeX dùng tên và giá trị có nghĩa;** mapping sang Lotte (tên field + mã) nằm ở §6.2. Không dùng tên/ mã Core làm param TradeX.

| Parameter | Type | Required | Default | Description (TradeX) |
| --------- | ---- | -------- | ------- | -------------------- |
| `accountNumber` | String | ✅ | - | Số tài khoản |
| `symbolCode` | String | ❌ | "" | Mã CK; rỗng = tra cứu tất cả |
| `orderSendFilter` | String | ❌ | `ALL` | Lọc theo trạng thái gửi lệnh: `ALL` \| `SENT` \| `PENDING` (xem §6.2 mapping → Lotte `sent`) |
| `sellBuyType` | String | ❌ | `ALL` | Lọc chiều: `ALL` \| `BUY` \| `SELL` (xem §6.2 mapping → Lotte `sell_buy_tp`) |
| `fetchCount` | Number | ❌ | - | Số bản ghi mỗi trang (optional) |
| `nextKey` | String | ❌ | `"0"` | Pagination token |

**Lotte request fields (DRORD-016):** `hts_user_id`, `acnt_no`, `ctr_cd`, `sent` (0/1/2), `sell_buy_tp` (0/1/2), `next_data`. TradeX map tên/giá trị có nghĩa sang các field này.

### 6.2 Request Mapping

**TradeX → Lotte (DRORD-016):** Tên và giá trị TradeX (có nghĩa) map sang field/code Lotte. Không dùng tên hoặc mã Core làm param TradeX.

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
| ------------ | ---- | -------- | ----------- | --------- | ----------- |
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số tài khoản |
| `symbolCode` | String | ❌ | `ctr_cd` | Default "" = all | Mã CK |
| `orderSendFilter` | String | ❌ | `sent` | Map (bảng dưới) | Trạng thái gửi lệnh — TradeX dùng giá trị có nghĩa |
| `sellBuyType` | String | ❌ | `sell_buy_tp` | Map (bảng dưới) | Chiều mua/bán — TradeX dùng giá trị có nghĩa |
| `fetchCount` | Number | ❌ | `row_count` | Optional | Số bản ghi mỗi trang (nếu Lotte hỗ trợ) |
| `nextKey` | String | ❌ | `next_data` | Default "0" (first page) | Pagination |
| *(JWT)* | - | - | `hts_user_id` | Auto | Username from token |

**Value mapping — orderSendFilter (TradeX → Lotte `sent`):**

| TradeX `orderSendFilter` | Ý nghĩa | Lotte `sent` |
| ------------------------ | ------- | ------------ |
| `ALL` | Tất cả | `0` |
| `SENT` | Đã gửi lên sàn | `1` |
| `PENDING` | Chưa gửi | `2` |

**Value mapping — sellBuyType (TradeX → Lotte `sell_buy_tp`):**

| TradeX `sellBuyType` | Ý nghĩa | Lotte `sell_buy_tp` |
| -------------------- | ------- | ------------------- |
| `ALL` | Tất cả | `0` |
| `BUY` | Mua | `1` |
| `SELL` | Bán | `2` |

**Note:** Lotte response ghi "1: bán / 2: mua" cho `sell_buy_tp`; request mapping giả định 1=mua, 2=bán (thống nhất Regular Order). Cần xác nhận với Lotte khi triển khai.

### 6.3 Response Mapping

**Success (200):** Map Lotte `data_list` (list items) → `{ totalCount, orders }`.

**Stop Order History Item (Lotte list → TradeX):**

| Lotte Field (list→) | TradeX Field | Type | Transform | Description |
| ------------------- | ------------ | ---- | --------- | ----------- |
| `date` | `orderDate` | String | Direct | Ngày đặt lệnh (yyyyMMdd) — map 1-1 |
| `seq_no` | `orderSeqNo` | String | Direct | Số hiệu lệnh — map 1-1 |
| `acnt_no` | `accountNumber` | String | Direct | Số TK |
| `acent_nm` | `accountName` | String | Direct | Tên TK |
| `ctr_code` | `symbolCode` | String | Direct | Mã CK |
| `ctr_name` | `symbolName` | String | Direct | Tên CK |
| `sell_buy_tp` | `sellBuyType` | String | **1→SELL, 2→BUY** (value mapping) | Lotte: 1=bán, 2=mua |
| `ord_tp` | `orderType` | String | 1=MP, 2=LO, 9=MTL | Loại lệnh |
| `qty` | `orderQuantity` | Number | Direct | Khối lượng |
| `price` | `orderPrice` | Number | Direct | Giá |
| `band_pri` | `priceBand` | Number | Direct | Bước giá |
| `str_ord_dt` | `validFromDate` | String | Direct | Ngày bắt đầu |
| `end_ord_dt` | `validToDate` | String | Direct | Ngày kết thúc |
| `registered` | `registered` | String | Y/N | Đã đăng ký / Chưa đăng ký |
| `ord_sent` | `sentStatus` | String | Direct | Trạng thái gửi |
| `ord_dt`, `ord_no` | (optional) | String | Direct | Ngày đặt, Số lệnh |
| `err_cd`, `err_msg` | `errorCode`, `errorMessage` | String | Direct | Mã lỗi / Thông báo lỗi (nếu có) |
| `next_data` | - | - | → Header `X-Next-Key` | Pagination |

**Pagination:** Lotte `list->next_data` → response header `X-Next-Key`; client gửi lại qua query param `nextKey`.

**Empty result:** HTTP 200, `totalCount: 0`, `orders: []`.

### 6.4 Error Mapping

**Validation Error (400):** `accountNumber` missing → `FIELD_IS_REQUIRED`.

**Auth (401/403):** Giống §7 (UNAUTHORIZED, TOKEN_EXPIRED, FORBIDDEN, UNAUTHORIZED_ACCOUNT).

**Business Error (422) - Lotte Pass-Through:** `STOP_ORDER_HISTORY_{LOTTE_CODE}` (vd. `STOP_ORDER_HISTORY_1005`).

---

## 7. Lotte API Mapping Reference

### 7.1 Summary Table

| Lotte Code | Name | Lotte URL | TradeX Operation |
|------------|------|-----------|------------------|
| DRORD-005 | Lệnh điều kiện MUA | dr-stop-order-buy | Place (sellBuyType=BUY) |
| DRORD-006 | Lệnh điều kiện BÁN | dr-stop-order-sell | Place (sellBuyType=SELL) |
| DRORD-023 | Sửa lệnh Điều kiện Mua | dr-replace-stop-order | Modify |
| DRORD-024 | Sửa lệnh Điều kiện Bán | dr-replace-stop-order | Modify |
| DRORD-025 | Hủy lệnh Điều kiện Mua | dr-cancel-stop-order | Cancel |
| DRORD-026 | Hủy lệnh Điều kiện Bán | dr-cancel-stop-order | Cancel |
| **DRORD-016** | **Tra cứu Lịch sử lệnh điều kiện trong ngày** | **dr-condition-ord-in-day** | **Query Stop Order History** |

### 7.2 Field Abbreviations (Lotte)

| Lotte Field | Meaning |
|-------------|---------|
| `stk_cd` | Mã CK (contract code) |
| `ord_pri` | Giá |
| `ord_band_pri` | Bước giá |
| `from_dt`, `end_dt` | Ngày bắt đầu/kết thúc (phải cùng) |
| `seqn` | Số hiệu lệnh (sequence) |
| `ctr_cd` | Mã CK (trong query DRORD-016) |
| `sent` | Phân loại gửi: 0=all, 1=đã gửi, 2=chưa gửi (DRORD-016) |
| `sell_buy_tp` | 0=all, 1=mua, 2=bán (request); response: 1=bán, 2=mua (DRORD-016) |
| `next_data` | Key pagination (DRORD-016) |

---

## 8. Error Handling Summary

### 8.1 Error Code Patterns

| Error Source | Code Pattern | Example |
|--------------|--------------|---------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing accountNumber |
| Lotte Place | `STOP_ORDER_PLACE_{CODE}` | STOP_ORDER_PLACE_1005 |
| Lotte Modify | `STOP_ORDER_MODIFY_{CODE}` | STOP_ORDER_MODIFY_1005 |
| Lotte Cancel | `STOP_ORDER_CANCEL_{CODE}` | STOP_ORDER_CANCEL_1005 |
| Lotte Query History | `STOP_ORDER_HISTORY_{CODE}` | STOP_ORDER_HISTORY_1005 |

### 8.2 Common Lotte Error Codes

| Code | Description (VI) |
|------|------------------|
| `1005` | Không đủ ký quỹ / Lỗi nghiệp vụ |
| `2010` | Vượt hạn mức giao dịch |
| `3005` | Giá vượt biên độ |
| `4001` | Thị trường đã đóng |
| `5002` | Mã không hợp lệ |
| `6001` | Không tìm thấy lệnh |

---

## 9. Implementation Notes

### 9.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation |
| `lotte-bridge` hoặc `tuxedo` | Lotte API integration, routing by sellBuyType |
| **Kafka** | (Optional) Order events |

### 9.2 Key Principles

1. **Single URI per Operation:** FE chỉ cần gọi 1 endpoint cho Place (giống Regular), 1 cho Modify, 1 cho Cancel, 1 cho Query Stop Order History (GET).
2. **Routing by sellBuyType:** Place: BUY → DRORD-005, SELL → DRORD-006.
3. **Order Identification:** Stop order map 1-1 theo Core: `orderDate` + `orderSeqNo` ↔ Lotte `date` + `seqn` (§2.4).
4. **Message Pass-Through:** Lotte messages AS-IS, language via Accept-Language.

### 9.3 Open Items

| Item | Action |
|------|--------|
| DRORD-005/006 response structure | Xác nhận field trả về (seq_no, date) |
| DRORD-016 request sell_buy_tp | Xác nhận 1=mua/2=bán (request) vs response 1=bán/2=mua |

---

**Document Status:** ✅ Draft Complete  
**For:** BA/Dev  
**Next Steps:** Implement lotte-bridge routing; Lotte URLs theo [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) (27/02/2026).  
**Estimated Effort:** 3-5 days (BE) + 2-3 days (FE) + 2 days (QA)
