# Stop Orders API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Orders - Stop Order (Lệnh Điều Kiện)  
**Version:** 1.0  
**Date:** February 11, 2026

> **Note:** Lotte-integrated APIs. Mapping: 1 URI cho mỗi operation (Place/Modify/Cancel), route theo sellBuyType giống Regular Order.

---

## 1. Overview

### 1.1 Purpose

Stop Order (Lệnh điều kiện) là lệnh mua/bán phái sinh được kích hoạt khi giá thị trường đạt mức trigger. TradeX expose **3 endpoints**, route nội bộ đến Lotte API DRORD-005, 006, 023, 024, 025, 026.

### 1.2 Smart Mapping Summary

| TradeX Operation | TradeX Endpoint | Method | Lotte APIs | Routing Logic |
|-----------------|-----------------|--------|------------|---------------|
| **Place** | `/api/v1/derivatives/stopOrder` | POST | DRORD-005, 006 | `sellBuyType=BUY` → dr-stop-order-buy<br>`sellBuyType=SELL` → dr-stop-order-sell |
| **Modify** | `/api/v1/derivatives/stopOrder/modify` | PUT | DRORD-023, 024 | 1 URL chung `dr-replace-stop-order` |
| **Cancel** | `/api/v1/derivatives/stopOrder/cancel` | PUT | DRORD-025, 026 | 1 URL chung (cần xác nhận với Lotte) |

### 1.3 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place Stop Order | POST | `/api/v1/derivatives/stopOrder` |
| Modify Stop Order | PUT | `/api/v1/derivatives/stopOrder/modify` |
| Cancel Stop Order | PUT | `/api/v1/derivatives/stopOrder/cancel` |

### 1.4 Response Format Standards

**Success (Mutation - Place/Modify/Cancel):**
```json
{
  "message": "[V0307] Message from Lotte",
  "orderNumber": "20260211-001234"
}
```

**Note:** `orderNumber` = composite `{date}-{seq_no}` (Stop order dùng date+seq từ Lotte). Format giống Regular Order để FE xử lý thống nhất.

**Error:**
```json
{
  "code": "STOP_ORDER_PLACE_1005",
  "message": "[V3120] Lotte error message"
}
```

**Principles:** Giống Regular Orders (xem `Regular_Orders_API_Spec.md` §1.3) - HTTP status = success indicator, NO `success: true/false`, pass-through Lotte messages AS-IS, Mutation = minimal (message + orderNumber).

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

**Note:** Mapping 1-1 với Lotte. Logic UX (user nhập gì, FE tính gì) → [FE Issue](../../../FE%20Implementation/Order/Issues/Derivatives_Stop_Order_Integration.md).

**Note:** Business rules (margin, price limits) validated by Lotte Core.

### 2.3 Language Mapping

| Accept-Language | Lotte lang_code |
|-----------------|-----------------|
| `vi` | `V` |
| `en` | `E` |
| `ko` | `K` |

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
| `data_list` (seq_no, date) | `orderNumber` | Composite `{date}-{seq_no}` | Chuẩn TradeX: message + orderNumber |

**Note:** Lotte DRORD-005/006 DataResponse không mô tả chi tiết. Cần xác nhận field trả về (seq_no, date). TradeX format: `orderNumber` = `"{date}-{seq_no}"`.

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

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | ✅ | `acno` | Số tài khoản |
| `orderNumber` | String | ✅ | `date`, `seqn` | Composite `{date}-{seq_no}` - BE parse để gọi Lotte |
| `orderPrice` | Number | ✅ | `jprc` | Direct | Giá mới |
| `orderQuantity` | Number | ✅ | `jqty` | Direct | Khối lượng mới |
| `priceBand` | Number | ✅ | `bprc` | Direct | Bước giá mới |
| `validFromDate` | String | ❌ | `sdate` | Mặc định = date (parse từ orderNumber) |
| `validToDate` | String | ❌ | `edate` | Mặc định = sdate (sdate = edate) |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto |

**Order Identification:** Stop order Lotte dùng `(date, seqn)`. TradeX thống nhất dùng `orderNumber` = `{date}-{seq_no}` - FE truyền 1 field, BE parse thành date + seqn cho Lotte.

### 4.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field |
|-------------|--------------|
| `error_desc` | `message` |
| - | `orderNumber` | Echo lại từ request (hoặc composite từ Lotte nếu có) |

**Error (422):** Prefix `STOP_ORDER_MODIFY_*`

---

## 5. API: Cancel Stop Order

### 5.1 Request

**Endpoint:** `PUT /api/v1/derivatives/stopOrder/cancel`

**Lotte Endpoint:** DRORD-025, 026 (URL chưa có trong tài liệu - cần xác nhận Lotte)

**Request body:** Chỉ cần accountNumber, orderNumber.

### 5.2 Request Mapping

| TradeX Field | Type | Required | Lotte Field | Description |
|--------------|------|----------|-------------|-------------|
| `accountNumber` | String | ✅ | `acno` | Số tài khoản |
| `orderNumber` | String | ✅ | `date`, `seqn` | Composite `{date}-{seq_no}` |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto |

### 5.3 Response Mapping

**Success (200):**

| Lotte Field | TradeX Field |
|-------------|--------------|
| `error_desc` | `message` |
| - | `orderNumber` | Echo lại từ request |

**Error (422):** Prefix `STOP_ORDER_CANCEL_*`

---

## 6. Lotte API Mapping Reference

### 6.1 Summary Table

| Lotte Code | Name | Lotte URL | TradeX Operation |
|------------|------|-----------|------------------|
| DRORD-005 | Lệnh điều kiện MUA | dr-stop-order-buy | Place (sellBuyType=BUY) |
| DRORD-006 | Lệnh điều kiện BÁN | dr-stop-order-sell | Place (sellBuyType=SELL) |
| DRORD-023 | Sửa lệnh Điều kiện Mua | dr-replace-stop-order | Modify |
| DRORD-024 | Sửa lệnh Điều kiện Bán | dr-replace-stop-order | Modify |
| DRORD-025 | Hủy lệnh Điều kiện Mua | (URL cần xác nhận) | Cancel |
| DRORD-026 | Hủy lệnh Điều kiện Bán | (URL cần xác nhận) | Cancel |

### 6.2 Field Abbreviations (Lotte)

| Lotte Field | Meaning |
|-------------|---------|
| `stk_cd` | Mã CK (contract code) |
| `ord_pri` | Giá |
| `ord_band_pri` | Bước giá |
| `from_dt`, `end_dt` | Ngày bắt đầu/kết thúc (phải cùng) |
| `seqn` | Số hiệu lệnh (sequence) |
| `ctr_cd` | Mã CK (trong query) |

---

## 7. Error Handling Summary

### 7.1 Error Code Patterns

| Error Source | Code Pattern | Example |
|--------------|--------------|---------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing accountNumber |
| Lotte Place | `STOP_ORDER_PLACE_{CODE}` | STOP_ORDER_PLACE_1005 |
| Lotte Modify | `STOP_ORDER_MODIFY_{CODE}` | STOP_ORDER_MODIFY_1005 |
| Lotte Cancel | `STOP_ORDER_CANCEL_{CODE}` | STOP_ORDER_CANCEL_1005 |

### 7.2 Common Lotte Error Codes

| Code | Description (VI) |
|------|------------------|
| `1005` | Không đủ ký quỹ / Lỗi nghiệp vụ |
| `2010` | Vượt hạn mức giao dịch |
| `3005` | Giá vượt biên độ |
| `4001` | Thị trường đã đóng |
| `5002` | Mã không hợp lệ |
| `6001` | Không tìm thấy lệnh |

---

## 8. Implementation Notes

### 8.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation |
| `lotte-bridge` hoặc `tuxedo` | Lotte API integration, routing by sellBuyType |
| **Kafka** | (Optional) Order events |

### 8.2 Key Principles

1. **Single URI per Operation:** FE chỉ cần gọi 1 endpoint cho Place (giống Regular), 1 cho Modify, 1 cho Cancel, 1 cho Query.
2. **Routing by sellBuyType:** Place: BUY → DRORD-005, SELL → DRORD-006.
3. **Order Number:** Stop order Lotte dùng (date, seqn). TradeX thống nhất `orderNumber` = `{date}-{seq_no}` - cùng format với Regular Order để FE xử lý thống nhất.
4. **Message Pass-Through:** Lotte messages AS-IS, language via Accept-Language.

### 8.3 Open Items

| Item | Action |
|------|--------|
| DRORD-005/006 response structure | Xác nhận field trả về (seq_no, date) |
| DRORD-025/026 URL | Tài liệu Lotte chưa có URL - cần confirm |

---

**Document Status:** ✅ Draft Complete  
**For:** BA/Dev  
**Next Steps:** Confirm DRORD-025/026 URL với Lotte, implement lotte-bridge routing  
**Estimated Effort:** 3-5 days (BE) + 2-3 days (FE) + 2 days (QA)
