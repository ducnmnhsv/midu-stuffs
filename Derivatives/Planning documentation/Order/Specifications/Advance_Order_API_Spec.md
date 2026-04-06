# Advance Order API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Orders - Advance Orders (Lệnh đặt trước)  
**Version:** 1.1  
**Date:** March 30, 2026

> **Note:** Lotte-integrated APIs for **Derivatives only**. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) — §2.3.14 DRORD-034, §2.3.15 DRORD-035, §2.3.16 DRORD-036, §2.3.17 DRORD-037, §2.3.18 DRORD-038. So sánh lệnh thường: [Regular_Orders_API_Spec.md](./Regular_Orders_API_Spec.md) (DRORD-029–033).

---

## 1. Overview

### 1.1 Purpose

Advance Order (lệnh đặt trước) là lệnh đăng ký trước cho **phiên giao dịch** (`msec`: ATO, KLLT sáng/chiều, ATC) và **ngày hiệu lực** (`sdate`), với tổ hợp **MP/LO** (`jtyp`) và **hiệu lực** (`jmgb`: Day, ATO, MAK, MOK, ATC, MTL). Khác **Regular Order** (DRORD-029/030): không phải chỉ đặt lệnh “ngay trong phiên hiện tại” theo luồng by-user thông thường.

Luồng huỷ: client tra cứu **lệnh đặt trước có thể huỷ** (DRORD-038) để lấy **`sequenceNumber`** (`seqn` / `os_seqn`) và **`tradingSession`** (`msec`) trước khi gọi **huỷ** (DRORD-036). Theo Lotte_DR (Tsolution Derivaties 1), DRORD-036 dùng **`dr-adv-can`** (POST), không dùng chung path với DRORD-034.

### 1.2 API Endpoints

| Operation | Method | Endpoint | Lotte |
|-----------|--------|----------|-------|
| Place Advance Order | POST | `/api/v1/derivatives/advanceOrder` | DRORD-034 (Buy), DRORD-035 (Sell) |
| Cancel Advance Order | PUT | `/api/v1/derivatives/advanceOrder/cancel` | DRORD-036 |
| Query Advance Order History | GET | `/api/v1/derivatives/advanceOrder/history` | DRORD-037 — §5 bên dưới |
| Query Cancellable Advance Orders | GET | `/api/v1/derivatives/advanceOrder/cancellable` | DRORD-038 — §6 bên dưới |

### 1.3 Response Format Standards

**Success (Mutation):**
```
{
  "message": "[V0350] Message from Lotte",
  "orderNumber": "2026033000001"
}
```

**Success (Query):**
```
{
  "orders": [ ... ]
}
```

*(Pagination: có thể dùng response header `X-Next-Key` hoặc field trong phần tử cuối — tương tự tinh thần §8 Regular Orders / Order History.)*

**Error:**
```
{
  "code": "ERROR_CODE",
  "message": "Error message"
}
```

or

```
{
  "code": "INVALID_PARAMETER",
  "params": [ ... ]
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Mutation: Minimal response (message from Lotte + orderNumber)
- Query: Rich data arrays; wrapper dùng **`orders`** (đồng nhất Regular Orders / OrderBook / History)
- **GET APIs:** client có thể truyền thêm **fetchCount** hoặc **nextKey** (cả hai **không required**) — map sang Lotte `row_count`, `next_key` / `next_data` tùy API
- Pass-through Lotte messages AS-IS (including `[CODE]` prefix)

---

## 2. Business Rules

### 2.1 Trading session (`msec`)

| Lotte `msec` | Meaning |
|--------------|---------|
| `1` | ATO |
| `2` | KLLT sáng |
| `3` | KLLT chiều |
| `4` | ATC |

**TradeX → Lotte (`tradingSession` enum gợi ý):**

| TradeX `tradingSession` | Lotte `msec` |
|------------------------|--------------|
| `ATO` | `1` |
| `MORNING_KLLT` | `2` |
| `AFTERNOON_KLLT` | `3` |
| `ATC` | `4` |

#### 2.1.1 TradeX — Validate `tradingSession` theo `orderType` (Place)

TradeX **bắt buộc** kiểm tra tổ hợp trước khi gọi Lotte (400 `INVALID_VALUE` nếu vi phạm):

| `orderType` | `tradingSession` hợp lệ |
|-------------|-------------------------|
| **LO** | `ATO`, `MORNING_KLLT`, `AFTERNOON_KLLT`, `ATC` (bất kỳ một trong bốn) |
| **ATO** | Chỉ **`ATO`** |
| **ATC** | Chỉ **`ATC`** |
| **MAK**, **MOK**, **MTL** | Chỉ **`MORNING_KLLT`** hoặc **`AFTERNOON_KLLT`** |
| **DAY** | Chỉ **`MORNING_KLLT`** hoặc **`AFTERNOON_KLLT`** (cùng nhóm phiên KLLT với MAK/MOK/MTL; `jmgb` = 0 gắn `msec` 2 hoặc 3 theo Lotte_DR) |

**Ghi chú:** Quy tắc này **khớp** với ràng buộc Lotte `jtyp`/`jmgb`/`msec` trong §2.2: ATO↔`msec`=1, ATC↔`msec`=4, MAK/MOK/MTL↔`msec`∈{2,3}, LO cho phép mọi `msec` 1–4.

### 2.2 `orderType` — cùng logic Regular Order (OrderBook §7.3 / History §8.3)

Trên **response** Regular Orders, TradeX suy ra `orderType` từ **`os_type`** + **`os_jmgb`**. Với **Advance Order** (Lotte request/response), trường tương đương **`os_type`** là **`jtyp`** (1 = MP, 2 = LO).

**Lotte → TradeX (decode)** — một field `orderType` duy nhất:

| Điều kiện Lotte | TradeX `orderType` |
|-----------------|---------------------|
| `jtyp` = `2` | **LO** (không quan tâm `jmgb` để đổi nhãn; Lotte ràng buộc LO luôn `jmgb` = `0`) |
| `jtyp` = `1` | Đọc `jmgb` và map: **0→DAY**, **2→ATO**, **3→MAK**, **4→MOK**, **7→ATC**, **9→MTL** |

*Tóm tắt:* `jtyp` = 2 → **LO**; `jtyp` = 1 → `orderType` = f(`jmgb`) như bảng trên — thống nhất tinh thần **Normal order** (type = 2 → LO; type = 1 → đọc jmgb).

**TradeX → Lotte (encode — Place Request):** client chỉ gửi **`orderType`** (cùng literal `LO`, `ATO`, `MAK`, `MOK`, `ATC`, `MTL` như [Regular §2.1](./Regular_Orders_API_Spec.md); thêm **`DAY`** khi MP Day). BE suy ra `jtyp` + `jmgb`:

| TradeX `orderType` | Lotte `jtyp` | Lotte `jmgb` |
|--------------------|--------------|--------------|
| `LO` | `2` | `0` |
| `DAY` | `1` | `0` |
| `ATO` | `1` | `2` |
| `MAK` | `1` | `3` |
| `MOK` | `1` | `4` |
| `ATC` | `1` | `7` |
| `MTL` | `1` | `9` |

**Tham chiếu ý nghĩa mã Lotte:**

| Lotte `jtyp` | Meaning |
|--------------|---------|
| `1` | Market (MP) |
| `2` | Limit (LO) |

| Lotte `jmgb` | Meaning |
|--------------|---------|
| `0` | Day |
| `2` | ATO |
| `3` | MAK |
| `4` | MOK |
| `7` | ATC |
| `9` | MTL |

**Ràng buộc (Lotte_DR):**
- Nếu **`jtyp` = 2 (Limit)** → **`jmgb` = 0** và **`msec` ∈ {1,2,3,4}**.
- Nếu **`jtyp` = 1 (Market)**:
  - `jmgb` = 2 ↔ `msec` = 1  
  - `jmgb` = 7 ↔ `msec` = 4  
  - `jmgb` ∈ {3,4,9} ↔ `msec` ∈ {2,3}

**DAY (`jmgb` = 0):** Lotte gói trong nhánh MP; về `msec`, thực tế tích hợp cần **`msec` ∈ {2,3}** (KLLT) — TradeX thể hiện qua §2.1.1.

Nghiệp vụ chi tiết (margin, phiên, giá) do **Lotte Core** xác thực; TradeX vẫn validate sớm theo **§2.1.1** để tránh request không khớp nghiệp vụ.

### 2.3 Effective date (`sdate`)

| API | Rule |
|-----|------|
| DRORD-034 (Buy) | `sdate` **bắt buộc**, yyyyMMdd; theo Lotte_DR phải **trùng ngày làm việc hiện tại**. |
| DRORD-035 (Sell) | Bảng Lotte có thể optional — **cần xác nhận UAT**; sample tài liệu vẫn có `sdate`. |

### 2.4 User field trên Lotte (khác nhau theo API)

| Lotte API | Field user |
|-----------|------------|
| DRORD-034 | `hts_user_id` |
| DRORD-035 | `user_id` |
| DRORD-036 | `user_id` hoặc `hts_user_id` *(sample UAT dùng `hts_user_id`)* |

TradeX: derive từ JWT; client **không** truyền tên field Lotte — BE map đúng field (`user_id` vs `hts_user_id`) theo endpoint và cấu hình gateway.

### 2.5 Cancel endpoint (DRORD-036)

- **URL Lotte:** `POST [Root URL APIKEY]/tuxsvc/der/order/dr-adv-can`.
- Payload huỷ: `acnt_no`, `code`, `msec`, `seqn`, `date` (yyyyMMdd), `user_id` hoặc `hts_user_id` (theo gateway), … (`seqn` / `msec` / `date` đối chiếu DRORD-038 khi cần).

### 2.6 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required (Place) | `accountNumber`, `symbolCode`, `sellBuyType`, `tradingSession`, `orderType`, `orderQuantity`, `effectiveDate` | `FIELD_IS_REQUIRED` |
| Required (Cancel) | `accountNumber`, `symbolCode`, `tradingSession`, `sequenceNumber`, `orderDate` | `FIELD_IS_REQUIRED` |
| Price for LO | `orderPrice` MUST be provided nếu `orderType` = **LO** (giống Regular) | `FIELD_IS_REQUIRED` |
| `tradingSession` vs `orderType` | Phải thỏa **§2.1.1** (gộp cả khớp `msec` sau khi map `tradingSession`) | `INVALID_VALUE` |
| Combo `jtyp`/`jmgb`/`msec` (sau encode) | Nếu sót lệnh validate, Lotte từ chối | `INVALID_VALUE` (TradeX) hoặc 422 Lotte |
| Account Ownership | TK thuộc user đăng nhập | `UNAUTHORIZED_ACCOUNT` |

**Note:** `deviceUniqueId` / `cli_mac_addr`: Lotte DRORD-034/035 ghi **optional** (`N`) — không giống Regular (mac_addr bắt buộc DRORD-029/030). FE có thể gửi nếu policy chung với gateway.

### 2.7 Language Mapping

| Accept-Language | Lotte `lang_code` |
|-----------------|-------------------|
| `vi` | `V` |
| `en` | `E` |
| `ko` | `K` |

---

## 3. API: Place Advance Order (Buy/Sell)

### 3.1 Request

**Endpoint:** `POST /api/v1/derivatives/advanceOrder`

**Lotte Endpoints (routed by `sellBuyType`):**
- **Buy:** `[RootURL]/tuxsvc/der/order/dr-adv-buy` (DRORD-034)
- **Sell:** `[RootURL]/tuxsvc/der/order/dr-adv-sell` (DRORD-035)

**Headers:**
- `Authorization: Bearer {JWT}`
- `Content-Type: application/json`
- `Accept-Language: vi` (optional, default: vi)

**Body — TradeX:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số TK |
| `symbolCode` | String | ✅ | Mã hợp đồng |
| `sellBuyType` | String | ✅ | `BUY` / `SELL` — route DRORD-034 / 035 |
| `tradingSession` | String | ✅ | Map → `msec` (§2.1) |
| `orderType` | String | ✅ | `LO`, `DAY`, `ATO`, `MAK`, `MOK`, `ATC`, `MTL` — encode → `jtyp` + `jmgb` (§2.2) |
| `orderQuantity` | Number | ✅ | → `jqty` |
| `orderPrice` | Number | ❌* | → `jprc`; *bắt buộc khi **LO** |
| `effectiveDate` | String | ✅ | yyyyMMdd → `sdate` |
| `deviceUniqueId` | String | ❌ | → `cli_mac_addr` (nếu gửi) |

### 3.2 Request Mapping

**TradeX → Lotte:**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Direct | Số TK |
| `symbolCode` | String | ✅ | `code` | Direct | Mã HĐ |
| `sellBuyType` | String | ✅ | — | **Routing only** | `BUY` → dr-adv-buy; `SELL` → dr-adv-sell |
| `tradingSession` | String | ✅ | `msec` | Map (§2.1) | Phiên |
| `orderType` | String | ✅ | `jtyp` + `jmgb` | **Encode** bảng §2.2 (một field → hai field Lotte) | Cùng literal Regular + `DAY` |
| `orderQuantity` | Number | ✅ | `jqty` | Stringify nếu Lotte string | KL |
| `orderPrice` | Number | ❌* | `jprc` | Stringify nếu Lotte string | Giá đặt |
| `effectiveDate` | String | ✅ | `sdate` | Direct yyyyMMdd | Ngày hiệu lực |
| `deviceUniqueId` | String | ❌ | `cli_mac_addr` | Direct | MAC / device |
| *(JWT)* | — | — | `hts_user_id` **hoặc** `user_id` | Auto | **Buy → `hts_user_id`; Sell → `user_id`** |
| *(Header)* | — | — | `lang_code` | Map (§2.7) | V/E/K |
| — | — | — | `row_count` | Optional / default | Theo Lotte |
| — | — | — | `next_key` | Optional / empty first | Theo Lotte |
| — | — | — | `mdm_tp` | **Derived** | Nếu Lotte yêu cầu — giống [Regular §3.2 — mdm_tp](../../../../TradeX%20Knowledge/API%20Standards/tradex-api-conventions.md#11-mdm_tp-kênh-thực-hiện--derived-fe-không-truyền) |

### 3.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Transform | Description |
|-------------|--------------|-----------|-------------|
| `error_code` | — | Check = `"0000"` | Success |
| `error_desc` | `message` | Pass-through AS-IS | Ví dụ `[V0350]...` |
| `data_list[0].order_no` | `orderNumber` | Direct | DRORD-035 (Sell) |
| `data_list[0].registerd` | `orderNumber` | Direct / normalize | DRORD-034 (Buy) — tên field theo spec nguồn Lotte |

**Ví dụ Lotte (Buy) — raw:**
```json
{
  "error_code": "0000",
  "error_desc": "[V0350]Lệnh đặt trước đã được hoàn thành",
  "success": true,
  "total_record": "",
  "data_list": [{ "registerd": "" }]
}
```

**Ví dụ Lotte (Sell) — raw (pattern):**
```json
{
  "error_code": "0000",
  "error_desc": "[V0350]Lệnh đặt trước đã được hoàn thành",
  "success": true,
  "total_record": "",
  "data_list": [{ "order_no": "1000016" }]
}
```

**Error (422) — Lotte:**

| Lotte Field | TradeX Field | Transform |
|-------------|--------------|-----------|
| `error_code` | `code` | Prefix: `ORDER_PLACE_{code}` |
| `error_desc` | `message` | Pass-through AS-IS |

**Common pattern:** `ORDER_PLACE_1005` và các mã khác — cùng tinh thần [Regular §3.3](./Regular_Orders_API_Spec.md).

### 3.4 Error Mapping

**Validation Error (400) — TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `symbolCode` | `FIELD_IS_REQUIRED` | `["symbolCode"]` | Missing |
| `sellBuyType` | `FIELD_IS_REQUIRED` / `INVALID_VALUE` | — | Missing / not BUY or SELL |
| `tradingSession` | `FIELD_IS_REQUIRED` / `INVALID_VALUE` | — | Missing / unknown session |
| `orderType` | `FIELD_IS_REQUIRED` / `INVALID_VALUE` | — | Missing / not trong tập LO,DAY,ATO,MAK,MOK,ATC,MTL |
| `orderQuantity` | `FIELD_IS_REQUIRED` | — | Missing |
| `orderPrice` | `FIELD_IS_REQUIRED` | — | Missing khi **LO** |
| `orderPrice` | `MUST_BE_NULL` / `INVALID_VALUE` | — | Có giá khi MOK/MAK/MTL (theo policy giống Regular §3.4) |
| `effectiveDate` | `FIELD_IS_REQUIRED` / `INVALID_DATE_FORMAT` | — | Missing / not yyyyMMdd |
| `tradingSession` | `INVALID_VALUE` | — | Không thuộc tập hợp hợp lệ cho `orderType` đã gửi (**§2.1.1**) |
| — | `INVALID_VALUE` | — | Tổ hợp `orderType` + `tradingSession` không khớp Lotte sau encode (**§2.2**) |

**Auth Error (401):** `UNAUTHORIZED`, `TOKEN_EXPIRED`  
**Auth Error (403):** `FORBIDDEN`, `UNAUTHORIZED_ACCOUNT`

**Business Error (422):** `ORDER_PLACE_{LOTTE_CODE}` — pass-through `message`

---

## 4. API: Cancel Advance Order

### 4.1 Request

**Endpoint:** `PUT /api/v1/derivatives/advanceOrder/cancel`

**Lotte Endpoint:** `POST [RootURL]/tuxsvc/der/order/dr-adv-can` (DRORD-036); payload có `seqn`, `date` (yyyyMMdd), user field theo gateway.

**Lotte Doc:** DRORD-036

**Headers:** `Authorization: Bearer {JWT}`, `Content-Type: application/json`, `Accept-Language` (optional)

**Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số TK |
| `symbolCode` | String | ✅ | Mã HĐ |
| `tradingSession` | String | ✅ | Phiên — map `msec`, phải khớp lệnh (từ §6) |
| `sequenceNumber` | String | ✅ | `seqn` từ DRORD-038 (`os_seqn`) |
| `orderDate` | String | ✅ | **yyyyMMdd** — map Lotte `date`; thường đồng bộ với ngày lệnh (vd. `os_date` từ DRORD-038 khi khớp nghiệp vụ) |

### 4.2 Request Mapping

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | ✅ | `acnt_no` | Số TK |
| `symbolCode` | String | ✅ | `code` | Mã HĐ |
| `tradingSession` | String | ✅ | `msec` | Map §2.1 |
| `sequenceNumber` | String | ✅ | `seqn` | Seq huỷ |
| `orderDate` | String | ✅ | `date` | yyyyMMdd |
| *(JWT)* | — | — | `user_id` **hoặc** `hts_user_id` | Auto — theo gateway (sample UAT dùng `hts_user_id`) |
| *(Header)* | — | — | `lang_code` | Map §2.7 |
| — | — | — | `row_count`, `next_key` | Optional theo Lotte |

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field | Description |
|-------------|--------------|-------------|
| `error_code` `0000` | *(success envelope)* | Chuẩn Lotte |
| `error_desc` | `message` | Pass-through (vd. `[V0320]Bạn đã thực hiện lệnh Huỷ…`) |
| `data_list[0].dummy` | *(thường không map)* | Mẫu thực tế `"Y"` — placeholder; **không** là số hiệu lệnh |

**Error (422):** Prefix gợi ý `ORDER_CANCEL_{LOTTE_CODE}` — pass-through `message`.

### 4.4 Error Mapping

**Validation (400):** thiếu `accountNumber`, `symbolCode`, `tradingSession`, `sequenceNumber`, `orderDate` → `FIELD_IS_REQUIRED`

**Auth (401/403):** giống §3.4

**Business (422):** `ORDER_CANCEL_1005`, …

### 4.5 Sample I/O — Lotte (DRORD-036, tham khảo UAT)

**Request**

```json
{
    "acnt_no": "039C110257",
    "hts_user_id": "039c110257",
    "msec": "3",
    "code": "41I1FB000",
    "seqn": "1280000004",
    "date": "20251028"
}
```

**Response**

```json
{
    "error_code": "0000",
    "error_desc": "[V0320]Bạn đã thực hiện lệnh Huỷ, hãy kiểm tra trạng thái Phân loại Huỷ/Sửa!",
    "success": true,
    "total_record": "",
    "data_list": [
        {
            "dummy": "Y"
        }
    ]
}
```

---

## 5. API: Query Advance Order History

API này map **DRORD-037** — tra cứu **danh sách lệnh đặt trước** theo khoảng ngày và bộ lọc. **Lotte:** Method **GET**, URL `get-dr-order-adv-history`; tài liệu mô tả body JSON — BE **xác nhận** query vs body với Lotte trước khi chốt FE.

### 5.1 Request

**Endpoint:** `GET /api/v1/derivatives/advanceOrder/history`

**Lotte (DRORD-037):** [Lotte_DR.md §2.3.17](../../../Documentation/[API%20specs]Lotte_DR.md) — `[Root URL APIKEY]/tuxsvc/der/order/get-dr-order-adv-history`

**Headers:** `Authorization: Bearer {JWT}`, `Content-Type: application/json`, `Accept-Language` (optional)

**Query Parameters (TradeX — tên có nghĩa nghiệp vụ; BE map sang field Lotte):**

| Parameter | Type | Required | Description | Lotte (proxy) |
|-----------|------|----------|-------------|---------------|
| `accountNumber` | String | ❌ | Số tài khoản cần tra cứu *(cùng naming Regular Order History §8)* | `acnt_no` |
| `symbolCode` | String | ❌ | Mã hợp đồng; bỏ trống hoặc khoảng trắng = tất cả | `code` |
| `fromDate` | String | ✅ | Đầu khoảng ngày tra cứu **yyyyMMdd** *(đồng nhất `fromDate` DRORD-033)* | `sdate` |
| `toDate` | String | ✅ | Cuối khoảng ngày tra cứu **yyyyMMdd** | `edate` |
| `orderSide` | String | ❌ | Lọc chiều lệnh: `ALL` \| `BUY` \| `SELL` — xem bảng enum §5.1.1 | `mdms` |
| `processingStage` | String | ❌ | Trạng thái xử lý trên Core: `ALL` \| `PROCESSED` \| `UNPROCESSED` \| `REJECTED` \| `CANCELLED` — §5.1.1 | `sdgb` |
| `orderMatchScope` | String | ❌ | Phân loại khớp; mặc định BE gửi `0` (theo Lotte). Giá trị TradeX gợi ý: `ALL` → `0` *(mở rộng khi Lotte có thêm mã)* | `mtch` |
| `orderValidity` | String | ❌ | Hiệu lực lệnh: `ALL` \| `ACTIVE` \| `EXPIRED` — §5.1.1 | `vali` |
| `branchCode` | String | ❌ | Mã chi nhánh; bỏ trống = tất cả | `brcd` |
| `tradingDeskCode` | String | ❌ | Mã phòng giao dịch; bỏ trống = tất cả | `agcd` |
| `historyPageMode` | String | ❌ | Phân trang: `INITIAL` (lần đầu) \| `CONTINUATION` (trang sau) — §5.1.1 | `qry_tp` |
| `htsInquiryUserId` | String | ❌ | User tra cứu trên Lotte (override); thường BE derive từ JWT | `hts_user_id` |
| `fetchCount` | Number | ❌ | Số bản ghi tối đa mỗi lần gọi (Lotte max **90**) | `row_count` |
| `nextKey` | String | ❌ | Khóa trang tiếp; lần đầu bỏ trống | `next_key` |

**Ghi chú:** Sample Lotte có thể dùng `account_no`, `from_dt`, `to_dt` — BE vẫn map từ tên TradeX chuẩn ở trên sang `acnt_no`, `sdate`, `edate`.

#### 5.1.1 Enum TradeX → mã Lotte (history query)

**`orderSide` → `mdms`**

| TradeX `orderSide` | Lotte `mdms` | Ý nghĩa |
|--------------------|--------------|---------|
| `ALL` *(mặc định nếu bỏ qua)* | `0` | Tất cả |
| `BUY` | `1` | Mua |
| `SELL` | `2` | Bán |

**`processingStage` → `sdgb`**

| TradeX `processingStage` | Lotte `sdgb` | Ý nghĩa |
|--------------------------|--------------|---------|
| `ALL` *(mặc định)* | `0` | Tất cả |
| `PROCESSED` | `1` | Đã xử lý |
| `UNPROCESSED` | `2` | Chưa xử lý |
| `REJECTED` | `3` | Từ chối |
| `CANCELLED` | `4` | Huỷ |

**`orderValidity` → `vali`**

| TradeX `orderValidity` | Lotte `vali` | Ý nghĩa |
|------------------------|--------------|---------|
| `ALL` *(mặc định)* | `0` | Tất cả |
| `ACTIVE` | `1` | Còn hiệu lực |
| `EXPIRED` | `2` | Hết hiệu lực |

**`orderMatchScope` → `mtch`**

| TradeX `orderMatchScope` | Lotte `mtch` |
|--------------------------|--------------|
| `ALL` *(mặc định)* | `0` |

*Khi Lotte bổ sung mã `mtch` khác, mở rộng bảng và enum TradeX.*

**`historyPageMode` → `qry_tp`**

| TradeX `historyPageMode` | Lotte `qry_tp` |
|--------------------------|----------------|
| `INITIAL` *(mặc định lần đầu)* | `Q` |
| `CONTINUATION` | `N` |

### 5.2 Request Mapping

**TradeX (query) → Lotte (DRORD-037):**

| TradeX parameter | Lotte field | Transform |
|------------------|-------------|-----------|
| `accountNumber` | `acnt_no` | Direct |
| `symbolCode` | `code` | Direct; “tất cả” → khoảng trắng theo Lotte |
| `fromDate` | `sdate` | Direct yyyyMMdd |
| `toDate` | `edate` | Direct yyyyMMdd |
| `orderSide` | `mdms` | Enum §5.1.1 → số hoặc chuỗi Lotte |
| `processingStage` | `sdgb` | Enum §5.1.1 |
| `orderMatchScope` | `mtch` | Enum §5.1.1 (mặc định `0`) |
| `orderValidity` | `vali` | Enum §5.1.1 |
| `branchCode` | `brcd` | Direct; “tất cả” → khoảng trắng |
| `tradingDeskCode` | `agcd` | Direct; “tất cả” → khoảng trắng |
| `historyPageMode` | `qry_tp` | Enum §5.1.1 |
| `htsInquiryUserId` | `hts_user_id` | Direct; nếu bỏ trống → BE điền từ JWT khi cần |
| `fetchCount` | `row_count` | Direct; giới hạn 90 |
| `nextKey` | `next_key` | Direct |
| *(JWT)* | `hts_user_id` | Auto nếu không truyền `htsInquiryUserId` và Lotte yêu cầu |

### 5.3 Response Mapping

**Lotte envelope:** `error_code`, `error_desc`, `success`, `total_record`, `data_list` (prefix `os_*`).

**Lotte `data_list` item (theo Lotte_DR §2.3.17):**

| Lotte Field | Type | Description |
|-------------|------|-------------|
| `os_date` | String | Ngày xử lý (yyyymmdd) |
| `os_sdate`, `os_edate` | String | Ngày bắt đầu / kết thúc |
| `os_seqn` | String | Số hiệu lệnh đặt |
| `os_msec` | String | Phiên (1–4) |
| `os_acno`, `os_acnm` | String | TK / Chủ TK |
| `os_code`, `os_name` | String | Mã / Tên HĐ |
| `os_mdms` | String | 1 Mua / 2 Bán |
| `os_jtyp` | String | 1 MP / 2 LO |
| `os_jmgb` | String | Hiệu lực |
| `os_jqty`, `os_jprc` | String | KL / Giá |
| `os_mdtp` | String | Kênh đặt |
| `os_user` | String | User đặt |
| `os_sdgb` | String | Phân loại xử lý |
| `os_mtch` | String | Phân loại khớp |
| `os_cqty`, `os_fprc`, `os_uqty`, `os_cmqty` | String | KL khớp / giá TB / chờ / huỷ |
| `os_cmtime`, `os_cmid` | String | Thời gian huỷ / ID |
| `os_vali` | String | Hiệu lực |
| `os_jmno` | String | SHL xử lý |
| `os_ercd`, `os_ermsg` | String | Mã / lý do từ chối |
| `os_reg_date`, `os_ipaddr`, `os_udat` | String | TG đặt / IP / cập nhật |
| `os_next_key` | String | Gợi ý phân trang |

**Order object (Lotte → TradeX)** — tên field **cùng họ** với Regular Order History và với query §5.1 (`orderSide`, `processingStage`, …):

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `os_acno` | `accountNumber` | String | Direct | Số TK |
| `os_acnm` | `accountName` | String | Direct | Tên chủ TK |
| `os_date` | `orderDate` | String | Direct yyyyMMdd | Ngày xử lý / ngày dòng lệnh |
| `os_seqn` | `sequenceNumber` | String | Direct | Seq lệnh đặt trước (huỷ §4) |
| `os_jmno` | `orderNumber` | String | Direct | Số hiệu lệnh xử lý (nếu có) |
| `os_code` | `symbolCode` | String | Direct | Mã HĐ |
| `os_name` | `symbolName` | String | Direct | Tên HĐ |
| `os_mdms` | `sellBuyType` | String | 1→**BUY**, 2→**SELL** | Chiều lệnh *(cùng ý query `orderSide` §5.1)* |
| `os_msec` | `tradingSession` | String | Map ngược §2.1 | Phiên ATO / KLLT / ATC |
| `os_jtyp` + `os_jmgb` | `orderType` | String | Decode §2.2 | LO / DAY / ATO / … |
| `os_jqty` | `orderQuantity` | Number | Parse | KL đặt |
| `os_jprc` | `orderPrice` | Number | Parse | Giá đặt |
| `os_sdgb` | `processingStage` | String | **Decode §5.3.1** | Trạng thái xử lý |
| `os_vali` | `orderValidity` | String | **Decode §5.3.1** | ALL / ACTIVE / EXPIRED |
| `os_mtch` | `orderMatchScope` | String | **Decode §5.3.1** hoặc raw | Phân loại khớp |
| `os_cqty` | `matchedQuantity` | Number | Parse | KL khớp |
| `os_fprc` | `averageMatchedPrice` | Number | Parse | Giá khớp TB |
| `os_uqty` | `pendingQuantity` | Number | Parse | KL chờ |
| `os_cmqty` | `cancelledQuantity` | Number | Parse | KL huỷ |
| `os_cmtime` | `cancelledAt` | String | Direct | Thời điểm huỷ (nếu có) |
| `os_ercd` | `rejectCode` | String | Direct | Mã từ chối |
| `os_ermsg` | `rejectReason` | String | Trim; rỗng → null | Lý do từ chối |
| `os_reg_date` | `placedAt` | String | Direct | Thời điểm đặt (theo Lotte) |
| `os_user` | `placedByUserId` | String | Direct | User đặt |
| `os_ipaddr` | `clientIp` | String | Direct | IP *(tuỳ policy hiển thị)* |
| `os_mdtp` | `orderChannelCode` | String | Direct | Kênh đặt |
| `os_sdate` | `effectiveFrom` | String | Direct yyyyMMdd | Đầu hiệu lực |
| `os_edate` | `effectiveTo` | String | Direct yyyyMMdd | Cuối hiệu lực |
| `os_next_key` | *(pagination)* | — | `X-Next-Key` / envelope | Trang sau |

**§5.3.1 Lotte → TradeX (chuỗi có nghĩa)** — đối xứng **§5.1.1**:

| `os_sdgb` | `processingStage` |
|-----------|---------------------|
| `0` | `ALL` |
| `1` | `PROCESSED` |
| `2` | `UNPROCESSED` |
| `3` | `REJECTED` |
| `4` | `CANCELLED` |

| `os_vali` | `orderValidity` |
|-----------|-------------------|
| `0` | `ALL` |
| `1` | `ACTIVE` |
| `2` | `EXPIRED` |

| `os_mtch` | `orderMatchScope` |
|-----------|---------------------|
| `0` | `ALL` |

*Mã Lotte khác — bổ sung khi có tài liệu.*

**Success (200) — body (ví dụ):**
```json
{
  "orders": [
    {
      "accountNumber": "039C222333",
      "accountName": "Nguyen Van A",
      "orderDate": "20251008",
      "sequenceNumber": "12345",
      "symbolCode": "VN30F2702",
      "symbolName": "VN30F2702",
      "sellBuyType": "BUY",
      "tradingSession": "MORNING_KLLT",
      "orderType": "LO",
      "orderQuantity": 1,
      "orderPrice": 1500,
      "processingStage": "PROCESSED",
      "orderValidity": "ACTIVE",
      "orderMatchScope": "ALL",
      "effectiveFrom": "20251008",
      "effectiveTo": "20251008",
      "placedByUserId": "039c222333",
      "rejectReason": null
    }
  ]
}
```

**Empty:** `{ "orders": [] }`

### 5.4 Error Mapping

**Validation (400):** `fromDate`, `toDate` required / format / range; `INVALID_DATE_RANGE`; giá trị enum không thuộc **§5.1.1** (`orderSide`, `processingStage`, …) → `INVALID_VALUE`

**Auth (401/403):** giống trên

**Business (422):** prefix gợi ý `ORDER_HISTORY_1005` hoặc `ORDER_ADVANCE_HISTORY_1005` — thống nhất team

---

## 6. API: Query Cancellable Advance Orders

API map **DRORD-038** — danh sách lệnh đặt trước **có thể huỷ**; dùng trước §4.

### 6.1 Request

**Endpoint:** `GET /api/v1/derivatives/advanceOrder/cancellable`

**Lotte (DRORD-038):** [Lotte_DR.md §2.3.18](../../../Documentation/[API%20specs]Lotte_DR.md) — `[Root URL APIKEY]/tuxsvc/der/order/get-dr-order-adv-able-can`

**Headers:** giống §5.1

**Query Parameters (TradeX — cùng quy ước tên có nghĩa với §5.1):**

| Parameter | Type | Required | Description | Lotte (proxy) |
|-----------|------|----------|-------------|---------------|
| `accountNumber` | String | ✅ | Số tài khoản | `account_no` *(DRORD-038: **`account_no`**, không dùng `acnt_no`)* |
| `fromDate` | String | ✅ | Đầu khoảng **yyyyMMdd** | `from_dt` |
| `toDate` | String | ✅ | Cuối khoảng **yyyyMMdd** | `to_dt` |
| `symbolCode` | String | ❌ | Mã HĐ; bỏ trống = tất cả | `code` |
| `orderSide` | String | ❌ | `ALL` \| `BUY` \| `SELL` — enum **§5.1.1** | `mdms` |
| `historyPageMode` | String | ❌ | `INITIAL` \| `CONTINUATION` — **§5.1.1** | `qry_tp` |
| `htsInquiryUserId` | String | ❌ | User tra cứu Lotte (override) | `hts_user_id` |
| `fetchCount` | Number | ❌ | Số bản ghi (tối đa theo Lotte) | `row_count` |
| `nextKey` | String | ❌ | Trang tiếp | `next_key` |

### 6.2 Request Mapping

| TradeX parameter | Lotte field | Transform |
|------------------|-------------|-----------|
| `accountNumber` | `account_no` | Direct |
| `fromDate` | `from_dt` | yyyyMMdd |
| `toDate` | `to_dt` | yyyyMMdd |
| `symbolCode` | `code` | Direct |
| `orderSide` | `mdms` | Enum §5.1.1 |
| `historyPageMode` | `qry_tp` | Enum §5.1.1 |
| `htsInquiryUserId` | `hts_user_id` | Direct; hoặc JWT |
| `fetchCount` | `row_count` | Direct |
| `nextKey` | `next_key` | Direct |

### 6.3 Response Mapping

**Lotte `data_list`:** các field `os_*` theo §2.3.18 Lotte_DR (tương tự history nhưng tập nhỏ hơn; **bắt buộc** có `os_seqn`, `os_msec`, `os_code` cho luồng huỷ).

**Order object (TradeX):** cùng hướng map như §5.3; **ưu tiên** trả `sequenceNumber` (= `os_seqn`) và `tradingSession` cho FE gọi §4.

**Success (200):** `{ "orders": [ ... ] }`

### 6.4 Error Mapping

Giống §5.4 — prefix có thể `ORDER_CANCELLABLE_LIST_1005` hoặc dùng chung `ORDER_HISTORY_1005` nếu gateway thống nhất một family code.

---

## 7. Error Handling Summary

### 7.1 Error Response Format

**Validation Error (400):**
```
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "sequenceNumber", "messageParams": [...] }
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

### 7.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing field | 400 |
| TradeX Auth | `UNAUTHORIZED`, … | Invalid JWT | 401/403 |
| Lotte Business | `ORDER_{OP}_{LOTTE_CODE}` | `ORDER_PLACE_1005`, `ORDER_CANCEL_1005` | 422 |
| System | `INTERNAL_ERROR` | Timeout Core | 500 |

**Note:** `{OP}` gợi ý: `PLACE`, `CANCEL`, `HISTORY` (hoặc tên cụ thể advance nếu team tách code).

### 7.3 Common Lotte Error Codes

| Code | Description (VI) |
|------|------------------|
| `1005` | Không thành công / lỗi nghiệp vụ (chi tiết trong `message`) |
| *(khác)* | Bổ sung khi Lotte cung cấp bảng lỗi riêng advance order |

---

## 8. Implementation Notes

### 8.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | Gateway, JWT, routing |
| `lotte-bridge` | Map TradeX ↔ Lotte DRORD-034–038 |

### 8.2 Key Principles

1. **Field user:** Buy (034) `hts_user_id`; Sell (035) `user_id`; Cancel (036) map JWT sang `user_id` hoặc `hts_user_id` theo gateway (sample UAT Cancel dùng `hts_user_id`).  
2. **Huỷ `dr-adv-can`:** integration test có đủ `seqn`, `msec`, **`date` (yyyyMMdd)** khớp ngữ cảnh lệnh / DRORD-038; response success có `data_list[0].dummy` = `"Y"` (không coi là `orderNumber`).  
3. **GET 037 + JSON:** chốt contract truyền tham số (query vs body) trước implement FE.  
4. **Chuẩn hoá `orderNumber`:** DRORD-034 có thể trả `registerd` — align sau UAT.  
5. **Không trùng business validation** giữa TradeX và Lotte cho margin/phiên — chỉ format + ownership + combo §2.2 phía TradeX nếu cần UX sớm.

---

## 9. Related APIs

| Document | Relation |
|----------|----------|
| [Regular_Orders_API_Spec.md](./Regular_Orders_API_Spec.md) | Lệnh thường, `mdm_tp`, pattern mutation/query |
| [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) | Source Lotte §2.3.14–18 |

---

**Document Status:** ✅ Complete  
**For:** BA / Dev  
**Next Steps:** Xác nhận Lotte transport cho DRORD-037; chốt prefix mã lỗi advance; triển khai gateway + test E2E đặt/huỷ.  
**Estimated Effort:** 1–2 tuần BE + 1 tuần FE (sau khi contract chốt)
