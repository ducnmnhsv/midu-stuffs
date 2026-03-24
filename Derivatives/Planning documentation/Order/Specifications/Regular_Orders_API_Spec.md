# Regular Orders API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Orders - Regular Buy/Sell Orders  
**Version:** 2.0  
**Date:** February 5, 2026

> **Note:** Lotte-integrated APIs for **Derivatives only**. For Equity orders, see `@TradeX Knowledge/Planning/regular-order-api-mapping.md`. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) (27/02/2026) — §2.3.9–2.3.12, 2.3.3, 2.3.4.

---

## 1. Overview

### 1.1 Purpose

Regular Orders (Derivatives) là các lệnh mua/bán thông thường (LO, ATO, ATC, MOK, MAK) được đẩy trực tiếp sang Lotte Core.

### 1.2 API Endpoints

| Operation | Method | Endpoint | Lotte |
|-----------|--------|----------|-------|
| Place Order | POST | `/api/v1/derivatives/order` | DRORD-029, 030 |
| Modify Order | PUT | `/api/v1/derivatives/order/modify` | DRORD-032 |
| Cancel Order | PUT | `/api/v1/derivatives/order/cancel` | DRORD-031 |
| Query Unmatch | GET | `/api/v1/derivatives/order/todayUnmatch` | DRORD-011 |
| Query OrderBook (trong ngày T0) | GET | `/api/v1/derivatives/order/orderBook` | DRORD-010 |
| Query History (lịch sử từ ngày – đến ngày) | GET | `/api/v1/derivatives/order/history` | DRORD-033 — §8 bên dưới |

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
- **GET APIs** (e.g. Order History): client có thể truyền thêm **fetchCount** hoặc **nextKey** (cả hai **không required**)
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
| Required Fields | accountNumber, symbolCode, sellBuyType, orderType, orderQuantity, **deviceUniqueId** | `FIELD_IS_REQUIRED` |
| Price for LO | orderPrice MUST be provided if orderType = LO | `FIELD_IS_REQUIRED` |
| Price for Market | orderPrice MUST be null if orderType in [MOK, MAK, MTL] | `INVALID_VALUE` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

**Note:** Business rules (price limits, margin, position limits) are validated by Lotte Core.

**Lotte requirement — Device identifier (Place Order):**  
Lotte API yêu cầu field **`mac_addr`** (Core) **bắt buộc** khi đặt lệnh (DRORD-029 Buy, DRORD-030 Sell). TradeX expose thành field **`deviceUniqueId`**; **FE bắt buộc truyền** `deviceUniqueId` trong request body. TradeX map `deviceUniqueId` → Lotte `mac_addr`.

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
| `deviceUniqueId` | String | ✅ | `mac_addr` | Direct | **Bắt buộc.** Lotte Core yêu cầu mac_addr; FE truyền deviceUniqueId, TradeX map sang Lotte `mac_addr`. |
| *(Request IP)* | - | - | `ip_addr` | Auto | Client IP |
| *(JWT)* `userId` | - | - | `user_id` | Auto | Username from token (max 15 chars) |
| *(Header)* | - | - | `lang_code` | Map (§2.3) | Language code (V/E/K) |
| - | - | - | `row_count` | Fixed: 500 | Số lượng bản ghi trả về |
| - | - | - | `next_key` | Fixed: "" | Empty for first request |
| - | - | - | `mdm_tp` | **Derived** | Kênh thực hiện – derive từ platform/channel (giống Equity) |

**mdm_tp Logic (khi Lotte yêu cầu):**
- FE **không** gửi `mdm_tp` trong request body.
- Backend derive từ: `request.channel` → `token.platform` → default.
- Map qua `getPlatformValueCore(platform)` → 31 (IOS), 32 (Android), 42 (PAAVE/DIFISOFT), `%` (default).
- Chi tiết: [TradeX API Conventions - mdm_tp](../../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md#11-mdm_tp-kênh-thực-hiện--derived-fe-không-truyền)

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
| `data_list[0].order_no` | `orderNumber` | Direct | Số hiệu lệnh (ví dụ `"1000016"`) |

**Ví dụ response Lotte khi đặt lệnh thành công (raw):**
```json
{
    "error_code": "0000",
    "error_desc": "[V0307]Bạn đã thực hiện lệnh Mua. Hãy kiểm tra Trạng thái lệnh!",
    "success": true,
    "total_record": "",
    "data_list": [
        {
            "order_no": "1000016"
        }
    ]
}
```

**TradeX success response (200):**  
`message` = Lotte `error_desc` (pass-through); `orderNumber` = `data_list[0].order_no`.

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
| `deviceUniqueId` | `FIELD_IS_REQUIRED` | `["deviceUniqueId"]` | Missing (Lotte Core yêu cầu `mac_addr` bắt buộc; FE truyền `deviceUniqueId`) |

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
| `deviceUniqueId` | String | ❌ | `mac_addr` | Device ID (optional) |
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
| `deviceUniqueId` | String | ❌ | `mac_addr` | Device ID (optional) |
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

**Lotte (DRORD-011) — envelope:** `error_code`, `error_desc`, `success`, `data_list`. Thành công: `error_code` = `0000`, `data_list` là mảng các object theo bảng dưới.

**Success (200) — TradeX body:**

| TradeX Field | Type | Description |
|--------------|------|-------------|
| `totalCount` | Number | `data_list.length` (hoặc tổng số bản ghi sau khi BE chuẩn hóa) |
| `orders` | Array | Map từ `data_list` — xem Order object |

**Lotte DRORD-011 — `data_list` item (Object Types, theo Lotte DR):**

| Field | Type | Valid values / format | Description |
|-------|------|------------------------|-------------|
| `acno` | String | — | Số TK |
| `acnm` | String | — | Tên TK |
| `jmno` | String | — | Số hiệu lệnh |
| `stat` | String | `New Order`, `Edit Order`, … | Phân loại dòng lệnh (new vs edit) |
| `mtst` | String | `1` = New (lệnh mới); giá trị khác — cần xác nhận với Lotte nếu mở rộng | Mã trạng thái bổ sung |
| `type` | String | `1` = MTL, `2` = LO | Loại lệnh (theo field `type` của response) |
| `code` | String | — | Mã HĐ |
| `mdms` | String | `1` / `2` | Buy / Sell |
| `jqty` | String | — | Khối lượng đặt |
| `jprc` | String | — | Giá đặt |
| `cqty` | String | — | Khối lượng khớp |
| `mqty` | String | — | Khối lượng chưa khớp |
| `jmgb` | String | Xem bảng `jmgb` bên dưới | Điều kiện / kiểu lệnh chi tiết (kết hợp với `type`) |
| `ord_style` | String | — | Không dùng — TradeX không map |
| `os_next_key` | String | — | Không dùng trong DRORD-011 — TradeX không map |

**`jmgb` (DRORD-011 — theo Lotte DR):**

| Giá trị | Ý nghĩa |
|--------|---------|
| `0` | DAY |
| `2` | ATO |
| `3` | MAK |
| `4` | MOK |
| `7` | ATC |
| `9` | MTL |

**Order object (Lotte → TradeX):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `jmno` | `orderNumber` | String | Direct | Số hiệu lệnh |
| `acno` | `accountNumber` | String | Direct | Số TK |
| `acnm` | `accountName` | String | Direct | Tên TK |
| `code` | `symbolCode` | String | Direct | Mã hợp đồng |
| `mdms` | `sellBuyType` | String | **1→BUY, 2→SELL** | Chiều lệnh |
| `type` + `jmgb` | `orderType` | String | **Xem logic `orderType` bên dưới** | LO / MTL / ATO / … |
| `jprc` | `orderPrice` | Number | Parse số | Giá đặt |
| `jqty` | `orderQuantity` | Number | Parse số | KL đặt |
| `cqty` | `matchedQuantity` | Number | Parse số | KL khớp |
| `mqty` | `unmatchedQuantity` | Number | Parse số | KL chưa khớp |
| `stat` + `mtst` | `status` | String | **Xem Status mapping** | Mã trạng thái chuẩn TradeX |
| `stat` + `mtst` | `statusName` | String | **Xem Status mapping** | Nhãn hiển thị (tiếng Việt) |

*Field thời gian:* tài liệu DRORD-011 **không** liệt kê field thời gian trong item `data_list`. Nếu response thực tế có thêm field (ví dụ `jdtm`), BE map sang `orderTime` (ISO 8601) và bổ sung dòng mapping tại đây.

**Value mapping — `sellBuyType`:**

| Lotte `mdms` | TradeX `sellBuyType` |
|--------------|------------------------|
| `1` | `BUY` |
| `2` | `SELL` |

**`orderType` (DRORD-011):** xử lý giống tinh thần OrderBook §7.3 — đọc `type` trước, sau đó `jmgb` khi `type` = **1**:

| Điều kiện Lotte | TradeX `orderType` |
|-----------------|---------------------|
| `type` = **2** | **LO** (không dùng `jmgb` để đổi nhãn LO) |
| `type` = **1** | Đọc `jmgb`: **0→DAY**, **2→ATO**, **3→MAK**, **4→MOK**, **7→ATC**, **9→MTL** |

*Tóm tắt:* `type=2` → LO; `type=1` → `orderType` = f(`jmgb`) theo bảng trên.

**Status mapping:**

| Lotte `stat` | Lotte `mtst` | TradeX `status` | TradeX `statusName` |
|--------------|--------------|-----------------|---------------------|
| `New Order` | `1` | `PENDING` | Chờ khớp |
| `Edit Order` | *(bất kỳ / tùy Lotte)* | `PENDING_MODIFY` | Chờ sửa |

*Ghi chú:* Kết hợp `stat` / `mtst` nếu Lotte trả thêm giá trị — cần bổ sung hàng trong bảng khi có spec Lotte.

**Fields không map sang TradeX:** `ord_style`, `os_next_key` (theo DR: không dùng).

**Note (khác request đặt lệnh):** Trong **response** DRORD-011, `type` là **`1` = MTL**, **`2` = LO** — quy ước riêng của API tra cứu; không trộn với bảng `type`/`jmgb` của DRORD-010 nếu khác ngữ cảnh.

**Empty Result:**
```
{
  "totalCount": 0,
  "orders": []
}
```

---

## 7. API: Query OrderBook (trong ngày T0)

API này map **DRORD-010** — dùng cho **sổ lệnh (orderBook)**: tra cứu lệnh **chỉ trong một ngày** (T0). Không hỗ trợ khoảng ngày (from–to). Để tra cứu lịch sử lệnh theo khoảng ngày (từ ngày – đến ngày), dùng `GET /api/v1/derivatives/order/history` — xem §8 (DRORD-033).

### 7.1 Request

**Endpoint:** `GET /api/v1/derivatives/order/orderBook`

**Lotte (DRORD-010):** [Lotte_DR.md §2.3.3](../../../Documentation/[API%20specs]Lotte_DR.md) — URL: `[Root URL APIKEY]/tuxsvc/der/order/dr-order-history`; Method: POST. Request body: `hts_user_id`, `acnt`, `date`, `next_data`. **Chỉ tra cứu trong ngày** (một `date` duy nhất).

**Headers:**

- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

**Query Parameters:**

Đối với API GET (Order History), TradeX có thể nhận thêm **fetchCount** và **nextKey** (cả hai **không required**).

| Parameter | Type | Required | Default | Description |
| --------- | ---- | -------- | ------- | ----------- |
| `accountNumber` | String | ✅ | - | Số tài khoản (→ Lotte `acnt`) |
| `date` | String | ❌ | **Today** | Ngày tra cứu (yyyyMMdd) → Lotte `date`; không truyền → TradeX gửi Lotte = **today** |
| `fetchCount` | Number | ❌ | - | Số bản ghi mỗi trang (optional; map sang Lotte nếu API hỗ trợ, không truyền = dùng default) |
| `nextKey` | String | ❌ | "" | Pagination; lần đầu không gửi hoặc "" (→ Lotte `next_data`). Optional. |

### 7.2 Request Mapping

**TradeX → Lotte (DRORD-010):**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
| ------------ | ---- | -------- | ----------- | --------- | ----------- |
| `accountNumber` | String | ✅ | `acnt` | Direct | Số tài khoản |
| `date` | String | ❌ | `date` | **Default: today** (yyyyMMdd) | Ngày tra cứu |
| `fetchCount` | Number | ❌ | (nếu Lotte hỗ trợ) | Optional | Số bản ghi mỗi trang; không truyền = dùng default Lotte/TradeX |
| `nextKey` | String | ❌ | `next_data` | Default: "" (first page) | Pagination; trang sau = giá trị `os_next_key` lần trước. Optional. |
| *(JWT)* | - | - | `hts_user_id` | Auto | Username from token |

**Default Logic (khi TradeX gọi Lotte):**

- `date`: client không truyền → mặc định **today** (yyyyMMdd) gửi sang Lotte.
- `fetchCount`: không required; nếu client gửi thì TradeX có thể map sang Lotte (nếu API hỗ trợ) hoặc dùng cho giới hạn phía TradeX.
- `next_data`: client không gửi `nextKey` hoặc rỗng → gửi "" hoặc khoảng trắng (trang đầu); có `nextKey` → gửi nguyên giá trị (trang tiếp).

### 7.3 Response Mapping

**Success (200):** Map Lotte `data_list` (DRORD-010 DataResponse) → wrapper `{ totalCount, orders }`.

**Order History Object (Lotte → TradeX) — map giá trị có nghĩa (không trả raw code Lotte):**

| Lotte Field | TradeX Field | Type | Transform | Description |
| ----------- | ------------ | ---- | --------- | ----------- |
| `jmno` | `orderNumber` | String | Direct | Số hiệu lệnh |
| `ojno` | `originalOrderNumber` | String | Direct | Số lệnh gốc (nếu sửa/hủy) |
| `code` | `symbolCode` | String | Direct | Mã hợp đồng |
| `mdms` | `sellBuyType` | String | **1→BUY, 2→SELL** | Chiều lệnh (value mapping) |
| `jprc` | `orderPrice` | Number | Direct | Giá |
| `jqty` | `orderQuantity` | Number | Direct | KL đặt |
| `cmqt` | `matchedQuantity` | Number | Direct | KL khớp |
| `mqty` | `unmatchedQuantity` | Number | Direct | KL chưa khớp |
| `dqty` | `cumulativeQuantity` | Number | Direct | KL khớp tích lũy |
| `jcgb` | `operation` | String | **1→NEW, 2→MODIFY, 3→CANCEL** (chuỗi Lotte; trim nếu cần) | Phân loại thao tác lệnh (mới / sửa / hủy) |
| `type` + `jmgb` | `orderType` | String | **Xem logic bên dưới** (phụ thuộc `type`) | Loại lệnh — BE xử lý theo §7.3 Value mapping orderType |
| `jmgb` | (dùng khi type=1) | String | Chỉ dùng khi `type` = 1 | 2→ATO, 3→MAK, 4→MOK, 7→ATC, 9→MTL |
| `time` | `orderTime` | String | ISO 8601 | Thời gian |
| `user` | `user` | String | Direct | User đặt lệnh |
| `rmsg` | `rejectReason` | String | Direct | Lý do từ chối (nếu có) |

**Value mapping (response):**

| Lotte value | TradeX (có nghĩa) |
| ----------- | ------------------ |
| `mdms`: 1 | `sellBuyType`: **BUY** |
| `mdms`: 2 | `sellBuyType`: **SELL** |
| `jcgb`: 1 | `operation`: **NEW** (lệnh mới) |
| `jcgb`: 2 | `operation`: **MODIFY** (lệnh sửa) |
| `jcgb`: 3 | `operation`: **CANCEL** (lệnh hủy) |

**orderType (chỉ áp dụng cho OrderBook DRORD-010):** TradeX xử lý theo `type` trước, sau đó mới dùng `jmgb` nếu cần:

| Điều kiện Lotte | TradeX `orderType` |
|-----------------|---------------------|
| `type` = **2** | **LO** (không quan tâm field `jmgb`) |
| `type` = **1** | Đọc field `jmgb` và map: **2→ATO**, **3→MAK**, **4→MOK**, **7→ATC**, **9→MTL** |

*Tóm tắt:* type=2 → LO; type=1 → orderType = f(jmgb): 2=ATO, 3=MAK, 4=MOK, 7=ATC, 9=MTL.

**Pagination:**

| Lotte Field | TradeX | Description |
| ----------- | ------ | ----------- |
| `os_next_key` (trong response) | Response header `X-Next-Key` | Token trang tiếp — client gửi lại qua query param `nextKey`. Absent hoặc "" = hết trang. |

**Response structure (body):**

```json
{
  "totalCount": 10,
  "orders": [
    {
      "orderNumber": "2026020500012",
      "originalOrderNumber": "",
      "symbolCode": "VN30F2502",
      "sellBuyType": "BUY",
      "orderType": "LO",
      "orderPrice": 1250.5,
      "orderQuantity": 10,
      "matchedQuantity": 0,
      "unmatchedQuantity": 10,
      "cumulativeQuantity": 0,
      "operation": "NEW",
      "orderTime": "2026-02-05T09:15:00.000Z",
      "user": "USER01",
      "rejectReason": null
    }
  ]
}
```

**Empty result (no data from Lotte):** HTTP 200, `totalCount: 0`, `orders: []`.

**Load more:** Client gửi `nextKey` (từ header `X-Next-Key` lần trước). Không truyền hoặc "" = trang đầu.

### 7.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
| ----- | ---------- | ------------- | --------- |
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `date` | `INVALID_DATE_FORMAT` | `["date"]` | Wrong format (not yyyyMMdd) |

**Auth Error (401):**

| Error Code | Message | Condition |
| ---------- | ------- | --------- |
| `UNAUTHORIZED` | Token không hợp lệ hoặc đã hết hạn | Invalid token |
| `TOKEN_EXPIRED` | Phiên đăng nhập đã hết hạn | Token expired |

**Auth Error (403):**

| Error Code | Message | Condition |
| ---------- | ------- | --------- |
| `FORBIDDEN` | Không có quyền truy cập | No permission |
| `UNAUTHORIZED_ACCOUNT` | Tài khoản không thuộc quyền sở hữu của bạn | Account ownership check failed |

**Business Error (422) - Lotte Pass-Through:**

| Lotte Code | TradeX Code | Description |
| ---------- | ----------- | ----------- |
| `1005` | `ORDER_HISTORY_1005` | Lỗi hệ thống / không thành công |

---

## 8. API: Query Order History (từ ngày – đến ngày)

API này map **DRORD-033** — tra cứu **lịch sử đặt lệnh thường** theo **khoảng ngày** (fromDate – toDate). Dùng khi cần xem lệnh đã đặt trong nhiều ngày (vd. báo cáo, lịch sử giao dịch). Trong ngày (T0) dùng §7 OrderBook.

### 8.1 Request

**Endpoint:** `GET /api/v1/derivatives/order/history`

**Lotte (DRORD-033):** [Lotte_DR.md §2.3.13](../../../Documentation/[API%20specs]Lotte_DR.md) — Tra cứu lịch sử đặt lệnh thường (fromdate – todate). Method: POST. Request body: `hts_user_id`, `acnt`, `date_fr`, `date_to`, `next_data`.

**Headers:** `Authorization: Bearer {JWT}`, `Content-Type: application/json`, `Accept-Language: vi` (optional)

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản (→ Lotte `acnt`) |
| `fromDate` | String | ✅ | Từ ngày (yyyyMMdd) → Lotte `date_fr` |
| `toDate` | String | ✅ | Đến ngày (yyyyMMdd) → Lotte `date_to` |
| `nextKey` | String | ❌ | Pagination; lần đầu không gửi hoặc "". Trang tiếp = giá trị nhận từ response (vd. header `X-Next-Key`). → Lotte `next_data`. |
| `fetchCount` | Number | ❌ | Số bản ghi mỗi trang (optional; nếu Lotte hỗ trợ) |

### 8.2 Request Mapping (TradeX → Lotte DRORD-033)

**Theo Lotte_DR §2.3.13:**

| Lotte Field | Type | Required | Mô tả |
|-------------|------|----------|-------|
| `hts_user_id` | String | Y | Tài khoản thực hiện |
| `acnt` | String | Y | Số TK |
| `date_fr` | String | Y | Từ ngày (yyyyMMdd) |
| `date_to` | String | Y | Đến ngày (yyyyMMdd) |
| `next_data` | String | Y | Lần đầu khoảng trắng hoặc "0"; trang tiếp = giá trị `os_next_key` lần trước |

**Mapping:**

| TradeX (query) | Lotte Field | Transform |
|----------------|-------------|-----------|
| `accountNumber` | `acnt` | Direct |
| `fromDate` | `date_fr` | Direct (yyyyMMdd) |
| `toDate` | `date_to` | Direct (yyyyMMdd) |
| `nextKey` (hoặc empty) | `next_data` | Lần đầu "" hoặc "0"; lần sau = `nextKey` từ response |
| JWT / session | `hts_user_id` | Auto |

### 8.3 Response Mapping

**Lotte_DR §2.3.13 — Response thực tế:** `error_code`, `error_desc`, `success`, `total_record` (có thể rỗng), `data_list`.  
**Object Types (DataResponse)** — mỗi phần tử trong `data_list` dùng prefix `os_` (theo response thực tế DRORD-033):

| Lotte Field | Type | Mô tả (Lotte) |
|-------------|------|----------------|
| `os_date` | String | Ngày (yyyyMMdd) |
| `os_jcgb` | String | Phân loại kiểu lệnh / thao tác: **1** = lệnh mới (sinh ra từ thao tác đặt); **2** = lệnh sửa (modify); **3** = lệnh hủy (cancel) |
| `os_mdtp` | String | Mã loại (vd. 06) |
| `os_fcm_ord_no` | String | Số lệnh FCM |
| `os_mth_atm` | String | (nội bộ) |
| `os_mprc` | String | Giá khớp (có thể space) |
| `os_acno` | String | Số TK |
| `os_acnm` | String | Tên TK |
| `os_jmno` | String | Số hiệu lệnh |
| `os_ojno` | String | Số hiệu lệnh gốc (có thể leading space) |
| `os_code` | String | Mã HĐ |
| `os_mdms` | String | Buy/Sell (1: Mua, 2: Bán) |
| `os_jqty` | String | Khối lượng (có thể leading space) |
| `os_cqty` | String | Khối lượng khớp |
| `os_mqty` | String | Khối lượng chưa khớp |
| `os_cncl_qty` | String | Khối lượng đã hủy |
| `os_type` | String | Loại lệnh (vd. 2 = LO) |
| `os_jprc` | String | Giá đặt |
| `os_time` | String | Giờ (HHMMSS) |
| `os_user` | String | User |
| `os_jmgb` | String | 0:DAY, 2:ATO, 3:1OC, 4:FOK, 7:ATC |
| `os_ord_style` | String | (nội bộ) |
| `os_ipad` | String | IP |
| `os_msg_rsn` | String | Lý do từ chối (có thể space) |
| `os_ord_stat` | String | Trạng thái lệnh (0: Tiếp nhận; 1: Xác nhận tiếp nhận; 2: Khớp 1 phần; 3: Khớp toàn bộ; 4: Từ chối) |
| `os_next_key` | String | Next key (pagination) |

**Success (200):** Map Lotte `data_list` → wrapper `{ "orders": [...] }`. **TradeX không trả total items** (bỏ qua Lotte `total_record`).

**Order object (Lotte → TradeX) — mapping theo response thực tế DRORD-033:**

| Lotte Field | TradeX Field | Type | Transform | Description |
| ----------- | ------------ | ---- | --------- | ----------- |
| `os_jmno` | `orderNumber` | String | Direct | Số hiệu lệnh |
| `os_ojno` | `originalOrderNumber` | String | Trim | Số lệnh gốc (nếu sửa/hủy) |
| `os_code` | `symbolCode` | String | Direct | Mã hợp đồng |
| `os_mdms` | `sellBuyType` | String | **1→BUY, 2→SELL** | Chiều lệnh |
| `os_jprc` | `orderPrice` | Number | Parse float | Giá đặt |
| `os_jqty` | `orderQuantity` | Number | Trim + parse int | KL đặt |
| `os_cqty` | `matchedQuantity` | Number | Trim + parse int | KL khớp |
| `os_mqty` | `unmatchedQuantity` | Number | Trim + parse int | KL chưa khớp |
| `os_jcgb` | `operation` | String | **1→NEW, 2→MODIFY, 3→CANCEL** (chuỗi Lotte; trim nếu cần) | Phân loại thao tác lệnh (mới / sửa / hủy) |
| `os_type` + `os_jmgb` | `orderType` | String | **Xem logic orderType bên dưới** (giống OrderBook §7.3) | Loại lệnh — os_type=2→LO; os_type=1→map os_jmgb |
| `os_date` + `os_time` | `orderTime` | String | Ghép os_date + os_time → ISO 8601 hoặc format hiển thị (vd. yyyyMMdd HHMMSS) | Thời gian |
| `os_user` | `user` | String | Direct | User đặt lệnh |
| `os_msg_rsn` | `rejectReason` | String | Trim; rỗng/space → null | Lý do từ chối |
| `os_ord_stat` | `orderStatus` | String | **0→Tiếp nhận, 1→…, 4→Từ chối** (xem value mapping) | Trạng thái lệnh |

*Các field Lotte không map sang TradeX (có trong response):* `os_acno`, `os_acnm`, `os_mdtp`, `os_fcm_ord_no`, `os_mth_atm`, `os_mprc`, `os_cncl_qty`, `os_ord_style`, `os_ipad` — dùng nội bộ hoặc bỏ qua.

**Value mapping (response):**

| Lotte value | TradeX (có nghĩa) |
| ----------- | ------------------ |
| `os_mdms`: 1 | `sellBuyType`: **BUY** |
| `os_mdms`: 2 | `sellBuyType`: **SELL** |
| `os_jcgb`: 1 | `operation`: **NEW** (lệnh mới) |
| `os_jcgb`: 2 | `operation`: **MODIFY** (lệnh sửa) |
| `os_jcgb`: 3 | `operation`: **CANCEL** (lệnh hủy) |
| `os_ord_stat`: 0 | `orderStatus`: **Tiếp nhận** |
| `os_ord_stat`: 1 | `orderStatus`: **Xác nhận tiếp nhận** |
| `os_ord_stat`: 2 | `orderStatus`: **Khớp 1 phần** |
| `os_ord_stat`: 3 | `orderStatus`: **Khớp toàn bộ** |
| `os_ord_stat`: 4 | `orderStatus`: **Từ chối** |

**orderType (DRORD-033 — giống logic OrderBook §7.3):** TradeX xử lý theo `os_type` trước, sau đó mới dùng `os_jmgb` nếu cần:

| Điều kiện Lotte | TradeX `orderType` |
|-----------------|---------------------|
| `os_type` = **2** | **LO** (không quan tâm field `os_jmgb`) |
| `os_type` = **1** | Đọc field `os_jmgb` và map: **2→ATO**, **3→MAK**, **4→MOK**, **7→ATC**, **9→MTL** |

*Tóm tắt:* os_type=2 → LO; os_type=1 → orderType = f(os_jmgb): 2=ATO, 3=MAK, 4=MOK, 7=ATC, 9=MTL.

**Pagination:**

| Lotte Field | TradeX | Description |
| ----------- | ------ | ----------- |
| `os_next_key` (trong từng item hoặc response) | Response header `X-Next-Key` hoặc field trong order cuối | Token trang tiếp — client gửi lại qua query param `nextKey`. "0" hoặc rỗng = hết trang. |

**Response structure (body) — TradeX không trả totalCount/total items:**
```json
{
  "orders": [
    {
      "orderNumber": "1000007",
      "originalOrderNumber": "0",
      "symbolCode": "41I1FA000",
      "sellBuyType": "BUY",
      "orderType": "LO",
      "orderStatus": "Xác nhận tiếp nhận",
      "orderPrice": 1200,
      "orderQuantity": 1,
      "matchedQuantity": 0,
      "unmatchedQuantity": 0,
      "operation": "NEW",
      "orderTime": "2025-10-06T14:38:07.000Z",
      "user": "039c666777",
      "rejectReason": null
    }
  ]
}
```

**Empty result:** HTTP 200, `{ "orders": [] }`.

### 8.4 Error Mapping (Order History DRORD-033)

**Validation Error (400):**

| Field | Error Code | Condition |
|-------|------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | Missing |
| `fromDate` | `FIELD_IS_REQUIRED` / `INVALID_DATE_FORMAT` | Missing hoặc không đúng yyyyMMdd |
| `toDate` | `FIELD_IS_REQUIRED` / `INVALID_DATE_FORMAT` | Missing hoặc không đúng yyyyMMdd |
| — | `INVALID_DATE_RANGE` | fromDate > toDate |

**Auth (401/403):** `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN`, `UNAUTHORIZED_ACCOUNT`

**Business (422) — Lotte pass-through:** `ORDER_HISTORY_1005` (vd. Lotte 1005 → TradeX `ORDER_HISTORY_1005` hoặc prefix `ORDER_HISTORY_RANGE_` tùy quy ước)

**Server (500):** `INTERNAL_ERROR`

---

## 9. Error Handling Summary

### 9.1 Error Response Format

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

### 9.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business | `ORDER_{OP}_{LOTTE_CODE}` | `ORDER_PLACE_1005`, `ORDER_MODIFY_6001` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

**Note:** `{OP}` values: `PLACE`, `MODIFY`, `CANCEL`, `HISTORY` (e.g. `ORDER_HISTORY_1005`)

### 9.3 Common Lotte Error Codes

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

## 10. Implementation Notes

### 10.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| `order-v2` | Business logic, validation (for TradeX-native features) |
| **Kafka** | Service communication |

### 10.2 Key Principles

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
