# Open Positions API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - Open Positions  
**Version:** 1.0  
**Date:** February 13, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Query open positions with real-time P&L. **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/Lotte_DR_API_Specs.md) (27/02/2026) §2.1.1.

---

## 1. Overview

### 1.1 Purpose

Open Positions API tra cứu danh sách vị thế mở (positions) đang nắm giữ, bao gồm thông tin khối lượng, giá, lãi/lỗ chưa thực hiện.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Open Positions | GET | `/api/v1/derivatives/asset/openPositions` |

### 1.3 Response Format Standards

**Success (Query):**
```json
{
  "profitLoss": 0,
  "totalOpenPosition": 0,
  "positions": [...]
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
  "params": [ ... ]
}
```

**Principles:**
- HTTP status = success indicator (200 = success, 4xx/5xx = error)
- NO `success: true/false` field
- Query: Rich data arrays
- Pass-through Lotte messages AS-IS (including `[CODE]` prefix)

---

## 2. Business Rules

### 2.1 Position Types (sellBuyType)

| sellBuyType | Description | P&L Calculation |
|-------------|-------------|-----------------|
| `BUY` | Mua/Nắm giữ | (currentPrice - averagePrice) × quantity |
| `SELL` | Bán khống | (averagePrice - currentPrice) × quantity |

### 2.2 Quantity Fields

| Field | Description |
|-------|-------------|
| `quantity` | Khối lượng vị thế hiện tại |
| `previousQuantity` | Khối lượng kỳ trước / trước giao dịch |
| `closableQuantity` | KL khả dụng đóng (available_qty_closed) |

### 2.3 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber (bắt buộc; subNumber tùy chọn theo spec) | `FIELD_IS_REQUIRED` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.4 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Open Positions

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/openPositions`

**Lotte (DRACC-003):** [Lotte_DR.md §2.1.1](../../../Documentation/Lotte_DR_API_Specs.md) — URL: `[Root URL APIKEY]/tuxsvc/der/account/dr-open-positions`; Method: POST; Request body: `acnt`, `next_data`, `hts_user_id`.

**Query Parameters (TradeX):**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountNumber` | String | ✅ | Số tài khoản phái sinh → gửi sang Lotte `acnt` |
| `subNumber` | String | ❌ | Tiểu khoản (Lotte DRACC-003 không có field tương ứng; dùng cho validation/display nếu cần) |
| `nextKey` | String | ❌ | Pagination: lần đầu gửi `"0"`; trang tiếp gửi đúng giá trị nhận từ response trước. Map sang Lotte `next_data`. |

### 3.2 Request Mapping (TradeX → Lotte DRACC-003)

**Theo Lotte_DR.md §2.1.1 — Request Data (JSON) chỉ có 3 field:**

| Lotte Field | Type | Required | Mô tả (Lotte_DR) |
|-------------|------|----------|-------------------|
| `acnt` | String | Y | Số TK |
| `next_data` | String | Y | 0 (không bỏ trống); tra cứu trang tiếp thì nhập đúng next_data trả trong output lần trước |
| `hts_user_id` | String | Y | hts_user_id của TK tra cứu (vd. lhptgwapi) |

**Mapping TradeX → Lotte:**

| TradeX (query / context) | Lotte Field | Transform |
|--------------------------|-------------|-----------|
| `accountNumber` | `acnt` | Direct |
| `nextKey` (hoặc không truyền) | `next_data` | Lần đầu gửi `"0"`; lần sau gửi đúng `nextKey` nhận từ response (vd. từ `positions[].nextKey` cuối hoặc từ Lotte) |
| User đăng nhập (JWT/session) | `hts_user_id` | Auto — lấy từ user đang gọi API |

*Lotte DRACC-003 không có:* `sub`, `sym`, `row_count` / `fetchCount`. Các tham số này không gửi xuống Lotte; nếu TradeX vẫn nhận thì dùng cho validation hoặc filter phía TradeX.

### 3.3 Response Mapping

**Lotte_DR §2.1.1 — Response Data:** `error_code`, `error_desc`, `success`, `data_list`.  
**Object Types (DataResponse)** — mỗi phần tử trong `data_list`:

| Lotte Field (trong data_list[]) | Type | Mô tả (Lotte_DR) |
|---------------------------------|------|-------------------|
| `acnt` | String | Tài khoản |
| `acnt_name` | String | Tên tài khoản |
| `contract_code` | String | Mã hợp đồng |
| `type` | String | Loại Mua/Bán (1. Mua, 2. Bán) |
| `volume` | String | Khối lượng |
| `previous_volume` | String | Khối lượng trước |
| `average_price` | String | Giá trung bình |
| `current_price` | String | Giá hiện tại |
| `unrealized_profit_loss` | String | Lãi lỗ chưa thực hiện |
| `available_qty_closed` | String | Số lượng có thể đóng |
| `next_data` | String | next data |

**Lotte sample response (reference):**
```json
{
  "error_code": "0000",
  "error_desc": "",
  "success": true,
  "total_record": "",
  "data_list": [
    {
      "acnt": "039C200321",
      "acnt_name": "Nguyễn Thị Minh Phương",
      "contract_code": "41I2FC000",
      "type": "1",
      "volume": "     30",
      "previous_volume": "     30",
      "average_price": "1000.00",
      "current_price": "1000.00",
      "unrealized_profit_loss": "0.000",
      "available_qty_closed": "     30",
      "next_data": "0"
    }
  ]
}
```

**Success (200) – TradeX response:**

**Aggregate fields (BE-computed, trả ở root):**

| TradeX Field | Type | Description | Công thức BE |
|--------------|------|-------------|--------------|
| `profitLoss` | Number | Tổng lãi/lỗ chưa thực hiện | Σ `unrealizedPL` của tất cả bản ghi trong `positions` (bao gồm số dương và số âm) |
| `totalOpenPosition` | Number | Tổng khối lượng vị thế mở | Σ `quantity` của tất cả bản ghi trong `positions` |

**Position Object (Lotte `data_list[]` DataResponse → TradeX `positions[]`):**

| Lotte Field (Lotte_DR §2.1.1) | TradeX Field | Type | Transform | Description |
|-------------------------------|--------------|------|-----------|-------------|
| `contract_code` | `code` | String | Direct | Mã hợp đồng |
| `type` | `sellBuyType` | Enum | `1`→`BUY`, `2`→`SELL` (bảng dưới) | Loại Mua/Bán |
| `volume` | `quantity` | Number | Trim + parse int | Khối lượng (Lotte: string có thể có space) |
| `previous_volume` | `previousQuantity` | Number | Trim + parse int | Khối lượng trước |
| `average_price` | `averagePrice` | Number | Parse float | Giá trung bình |
| `current_price` | `currentPrice` | Number | Parse float | Giá hiện tại |
| `unrealized_profit_loss` | `unrealizedPL` | Number | Parse float | Lãi lỗ chưa thực hiện |
| `available_qty_closed` | `closableQuantity` | Number | Trim + parse int | Số lượng có thể đóng |
| `next_data` | `nextKey` | String | Direct | next data (pagination) |

*Các field Lotte không map sang TradeX (có trong response):* `acnt`, `acnt_name` — dùng nội bộ hoặc bỏ qua.

**sellBuyType mapping (Lotte `type` → TradeX):**

| Lotte type | TradeX sellBuyType | Vietnamese |
|------------|--------------------|------------|
| `1` | `BUY` | Mua/Nắm giữ |
| `2` | `SELL` | Bán/Bán khống |

*Lưu ý:* Lotte trả `volume`, `previous_volume`, `available_qty_closed` dạng string có thể có leading spaces — BE trim rồi parse số. Khi có phân trang (Lotte `next_data`), `profitLoss` và `totalOpenPosition` là tổng trên **đúng các bản ghi trong `positions` của response hiện tại**.

**Response Structure (TradeX):**
```json
{
  "profitLoss": 2360000,
  "totalOpenPosition": 20,
  "positions": [
    {
      "code": "41I1G3000",
      "sellBuyType": "BUY",
      "quantity": 10,
      "previousQuantity": 8,
      "averagePrice": 1285.5,
      "currentPrice": 1290.2,
      "unrealizedPL": 47000,
      "closableQuantity": 10,
      "nextKey": "sample 123"
    }
  ]
}
```

**Empty Result:**
```json
{
  "profitLoss": 0,
  "totalOpenPosition": 0,
  "positions": []
}
```

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |

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

| Lotte Code | TradeX Code | Description |
|------------|-------------|-------------|
| `1005` | `OPEN_POSITIONS_1005` | Lỗi hệ thống |
| `1006` | `OPEN_POSITIONS_1006` | Tài khoản không tồn tại |
| `2001` | `OPEN_POSITIONS_2001` | Không có dữ liệu |

---

## 4. Error Handling Summary

### 4.1 Error Response Format

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    { "code": "FIELD_IS_REQUIRED", "param": "accountNumber", "messageParams": ["accountNumber"] }
  ]
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
  "code": "OPEN_POSITIONS_{LOTTE_CODE}",
  "message": "[V3120] Lotte error message"
}
```

**Server Error (500):**
```json
{
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống, vui lòng thử lại sau"
}
```

### 4.2 Error Code Patterns

| Error Source | Code Pattern | Example | HTTP |
|--------------|--------------|---------|------|
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing required field | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business | `OPEN_POSITIONS_{LOTTE_CODE}` | `OPEN_POSITIONS_1005` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

### 4.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|------------------|
| `1005` | Lỗi hệ thống | System error |
| `1006` | Tài khoản không tồn tại | Account not found |
| `2001` | Không có dữ liệu | No data available |

---

## 5. Implementation Notes

### 5.1 Service Architecture

| Component | Role |
|-----------|------|
| `rest-proxy` | API Gateway, JWT validation, routing |
| `lotte-bridge` | Lotte API integration, request/response mapping |
| `asset-v2` | Business logic, validation (if needed) |
| **Kafka** | Service communication |

### 5.2 Key Principles

**1. Validation Strategy:**
- TradeX validates: Required fields, data types, format, account ownership
- Lotte validates: Business rules (positions exist, margin)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Return position data
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `OPEN_POSITIONS_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `OPEN_POSITIONS_1005`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

**5. Pagination:**
- Lotte trả `next_data` trong từng phần tử `data_list` (cursor cho trang/record tiếp theo). TradeX map sang `nextKey` trong từng object `positions[]`.
- Root response không có object `pagination`; phân trang (nếu có) dựa trên `nextKey` của record cuối hoặc theo quy ước Lotte.

**6. Real-time Data:**
- `currentPrice`: Lấy từ Lotte hoặc market data service.
- `unrealizedPL`: Lotte trả `unrealized_profit_loss`; BE có thể tính lại real-time nếu cần.

### 5.3 Data Transformation

**Lotte data_list item → TradeX position:**
```typescript
const sellBuyTypeMap = { '1': 'BUY', '2': 'SELL' };
const toPosition = (item: LotteDataListItem) => ({
  code: item.contract_code,
  sellBuyType: sellBuyTypeMap[item.type] ?? item.type,
  quantity: parseInt(String(item.volume).trim(), 10),
  previousQuantity: parseInt(String(item.previous_volume).trim(), 10),
  averagePrice: parseFloat(item.average_price),
  currentPrice: parseFloat(item.current_price),
  unrealizedPL: parseFloat(item.unrealized_profit_loss),
  closableQuantity: parseInt(String(item.available_qty_closed).trim(), 10),
  nextKey: item.next_data ?? null,
});
```

**Aggregate (BE-computed):**
```typescript
profitLoss: positions.reduce((sum, p) => sum + (p.unrealizedPL ?? 0), 0),
totalOpenPosition: positions.reduce((sum, p) => sum + (p.quantity ?? 0), 0),
```

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
