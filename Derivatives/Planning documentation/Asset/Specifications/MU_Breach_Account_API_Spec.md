# MU Breach Account API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - MU Breach Account Status  
**Version:** 1.0  
**Date:** March 9, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Tra cứu trạng thái tài khoản và số tiền cần bổ sung khi vi phạm tỷ lệ MU (margin call). **Tham chiếu Lotte:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) §2.1.7 — DRACC-036 (dr-mu-breach-account).

---

## 1. Overview

### 1.1 Purpose

MU Breach Account API tra cứu trạng thái tài khoản khi vi phạm tỷ lệ MU (margin), bao gồm thông tin vị thế, tỷ lệ tất toán, tỷ lệ MU, mức cảnh báo và **số tiền cần bổ sung** để khắc phục vi phạm.

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get MU Breach Account | GET | `/api/v1/derivatives/asset/muBreachAccount` |

### 1.3 Response Format Standards

**Success (Query):**
```json
{
  "items": [...],
  "pagination": {
    "hasMore": true,
    "nextKey": "...",
    "totalRecords": 10
  }
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
- Query: List of MU breach account records + pagination
- Pass-through Lotte messages AS-IS

---

## 2. Business Rules

### 2.1 Field Mapping (TradeX → Lotte)

Client gửi giá trị **có nghĩa** (enum/string); backend map sang mã Lotte. Nếu **không truyền** thì dùng Default.

**queryType** (TradeX → Lotte `query_type`):

| TradeX Value | Lotte Value | Mô tả |
|-------------|-------------|--------|
| `NEW_QUERY` | `0` | Query mới (trang đầu) |
| `CONTINUOUS_QUERY` | `2` | Query tiếp (phân trang) |
| *(default khi không truyền)* | `0` | NEW_QUERY |

**settlementStatus** (TradeX → Lotte `settlement_status`):

| TradeX Value | Lotte Value | Mô tả |
|-------------|-------------|--------|
| `ALL` | `0` | Tất cả |
| `NON_MARGIN` | `1` | Non-margin call |
| `MARGIN_CALL` | `2` | Margin call |
| `FORCE_SELL` | `3` | Force sell |
| *(default khi không truyền)* | `0` | ALL |

**branch** (TradeX → Lotte `branch`):

| TradeX Value | Lotte Value | Mô tả |
|-------------|-------------|--------|
| `ALL` | `%` | Tất cả chi nhánh |
| *mã chi nhánh* (string) | *pass-through* | Mã chi nhánh cụ thể (theo danh mục) |
| *(default khi không truyền)* | `%` | ALL |

**department** (TradeX → Lotte `department`):

| TradeX Value | Lotte Value | Mô tả |
|-------------|-------------|--------|
| `ALL` | `%` | Tất cả phòng giao dịch |
| *mã phòng* (string) | *pass-through* | Mã phòng giao dịch cụ thể (theo danh mục) |
| *(default khi không truyền)* | `%` | ALL |

**customerType** (TradeX → Lotte `customer_type`):

| TradeX Value | Lotte Value | Mô tả |
|-------------|-------------|--------|
| `ALL` | `9` | Tất cả |
| `NETTING` | `0` | Netting account |
| `NON_NETTING` | `1` | Non-netting account |
| *(default khi không truyền)* | `9` | ALL |

**warningType** (TradeX → Lotte `warning_type`):

| TradeX Value | Lotte Value | Mô tả |
|-------------|-------------|--------|
| `ALL` | `%` | Tất cả cảnh báo |
| `WARNING_1` | `1` | Cảnh báo 1 |
| `WARNING_2` | `2` | Cảnh cáo 2 |
| `WARNING_3` | `3` | Cảnh cáo 3 |
| *(default khi không truyền)* | `%` | ALL |

### 2.2 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber | `FIELD_IS_REQUIRED` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |
| Fetch Count | 1 ≤ fetchCount ≤ 100 (if used) | `INVALID_FETCH_COUNT` |

### 2.3 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get MU Breach Account

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/muBreachAccount`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-mu-breach-account` (DRACC-036) — Lotte_DR §2.1.7. Lotte Method: POST; TradeX exposes GET với query parameters, backend proxy chuyển sang POST body.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh |
| `queryType` | String | ❌ | NEW_QUERY | `NEW_QUERY` \| `CONTINUOUS_QUERY` → Lotte 0 \| 2 (§2.1) |
| `settlementStatus` | String | ❌ | ALL | `ALL` \| `NON_MARGIN` \| `MARGIN_CALL` \| `FORCE_SELL` → Lotte 0 \| 1 \| 2 \| 3 (§2.1) |
| `branch` | String | ❌ | ALL | `ALL` hoặc mã chi nhánh → Lotte % \| pass-through (§2.1) |
| `department` | String | ❌ | ALL | `ALL` hoặc mã phòng → Lotte % \| pass-through (§2.1) |
| `customerType` | String | ❌ | ALL | `ALL` \| `NETTING` \| `NON_NETTING` → Lotte 9 \| 0 \| 1 (§2.1) |
| `warningType` | String | ❌ | ALL | `ALL` \| `WARNING_1` \| `WARNING_2` \| `WARNING_3` → Lotte % \| 1 \| 2 \| 3 (§2.1) |
| `nextKey` | String | ❌ | "0" | Pagination token |
| `fetchCount` | Number | ❌ | 50 | Records per page (max: 100) |

### 3.2 Request Mapping

**TradeX → Lotte (backend builds POST body):**

| TradeX Field | Type | Required | Lotte Field | Transform / Default | Description |
|--------------|------|----------|-------------|---------------------|-------------|
| `accountNumber` | String | ✅ | `account_no` | Direct | Số tài khoản |
| `queryType` | String | ❌ | `query_type` | Map §2.1; default **NEW_QUERY → "0"** | NEW_QUERY \| CONTINUOUS_QUERY → 0 \| 2 |
| `settlementStatus` | String | ❌ | `settlement_status` | Map §2.1; default **ALL → "0"** | ALL \| NON_MARGIN \| MARGIN_CALL \| FORCE_SELL → 0 \| 1 \| 2 \| 3 |
| `branch` | String | ❌ | `branch` | Map §2.1; default **ALL → "%"** | ALL hoặc mã chi nhánh → % hoặc pass-through |
| `department` | String | ❌ | `department` | Map §2.1; default **ALL → "%"** | ALL hoặc mã phòng → % hoặc pass-through |
| `customerType` | String | ❌ | `customer_type` | Map §2.1; default **ALL → "9"** | ALL \| NETTING \| NON_NETTING → 9 \| 0 \| 1 |
| `warningType` | String | ❌ | `warning_type` | Map §2.1; default **ALL → "%"** | ALL \| WARNING_1 \| WARNING_2 \| WARNING_3 → % \| 1 \| 2 \| 3 |
| `nextKey` | String | ❌ | `next_key` | Default: "0" | Pagination |
| *(JWT)* | - | - | `hts_user_id` | Auto from token | hts_user_id tra cứu |

**Mapping & Default Logic (backend):**
```typescript
const QUERY_TYPE_MAP: Record<string, string> = { NEW_QUERY: '0', CONTINUOUS_QUERY: '2' };
const SETTLEMENT_STATUS_MAP: Record<string, string> = { ALL: '0', NON_MARGIN: '1', MARGIN_CALL: '2', FORCE_SELL: '3' };
const CUSTOMER_TYPE_MAP: Record<string, string> = { ALL: '9', NETTING: '0', NON_NETTING: '1' };
const WARNING_TYPE_MAP: Record<string, string> = { ALL: '%', WARNING_1: '1', WARNING_2: '2', WARNING_3: '3' };

function toLotteBranch(v?: string) { return !v || v === 'ALL' ? '%' : v; }
function toLotteDepartment(v?: string) { return !v || v === 'ALL' ? '%' : v; }

const body = {
  query_type: QUERY_TYPE_MAP[request.queryType] ?? '0',
  account_no: request.accountNumber,
  settlement_status: SETTLEMENT_STATUS_MAP[request.settlementStatus] ?? '0',
  branch: toLotteBranch(request.branch),
  department: toLotteDepartment(request.department),
  customer_type: CUSTOMER_TYPE_MAP[request.customerType] ?? '9',
  warning_type: WARNING_TYPE_MAP[request.warningType] ?? '%',
  next_key: request.nextKey ?? '0',
  hts_user_id: getHtsUserIdFromToken(),
};
```

### 3.3 Response Mapping

**Success (200):**

**Item Object (mỗi bản ghi trạng thái MU breach):**

| Lotte Field | TradeX Field | Type | Transform | Description |
|-------------|--------------|------|-----------|-------------|
| `account_no` | `accountNumber` | String | Direct | Số tài khoản |
| `account_name` | `accountName` | String | Direct | Tên tài khoản |
| `product_code` | `productCode` | String | Direct | Mã sản phẩm |
| `contract_code` | `contractCode` | String | Direct | Mã hợp đồng |
| `contract_name` | `contractName` | String | Direct | Tên hợp đồng |
| `sales_type_code` | `salesTypeCode` | String | Direct | Mã phân loại mua bán |
| `sales_type_name` | `salesTypeName` | String | Direct | Tên phân loại mua bán |
| `positions_count` | `positionsCount` | Number | Parse int | Số lượng vị thế |
| `settlement_rate` | `settlementRate` | Number | Parse float | Tỷ lệ tất toán |
| `settlement_quantity` | `settlementQuantity` | Number | Parse float | Số lượng tất toán |
| `settlement_status_code` | `settlementStatusCode` | String | Direct | Mã trạng thái tất toán |
| `settlement_status_name` | `settlementStatusName` | String | Direct | Tên trạng thái tất toán |
| `mu_ratio` | `muRatio` | Number | Parse float | Tỷ lệ MU |
| `warning_type` | `warningType` | String | Direct | Mã trạng thái cảnh báo (1/2/3) |
| `value` | `value` | Number | Parse float | Giá trị cần bù |
| `amount` | `amount` | Number | Parse float | Số tiền cần bổ sung |

**Pagination:**

| Lotte Field | TradeX Field | Type | Description |
|-------------|--------------|------|-------------|
| (from last item or response) `next_key` | `pagination.nextKey` | String | Next page token; null nếu hết |
| (derived) | `pagination.hasMore` | Boolean | nextKey != null && nextKey != "0" |
| (if available) | `pagination.totalRecords` | Number | Tổng số records (nếu Lotte cung cấp) |

**Response Structure:**
```json
{
  "items": [
    {
      "accountNumber": "001C123456",
      "accountName": "NGUYEN VAN A",
      "productCode": "VN30",
      "contractCode": "VN30F2403",
      "contractName": "Hợp đồng tương lai VN30",
      "salesTypeCode": "1",
      "salesTypeName": "Mua",
      "positionsCount": 5,
      "settlementRate": 85.5,
      "settlementQuantity": 10,
      "settlementStatusCode": "2",
      "settlementStatusName": "Margin call",
      "muRatio": 75.2,
      "warningType": "2",
      "value": 50000000,
      "amount": 55000000
    }
  ],
  "pagination": {
    "hasMore": false,
    "nextKey": null,
    "totalRecords": 1
  }
}
```

**Empty Result:**
```json
{
  "items": [],
  "pagination": {
    "hasMore": false,
    "nextKey": null,
    "totalRecords": 0
  }
}
```

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `fetchCount` | `INVALID_FETCH_COUNT` | `["fetchCount", "1", "100"]` | < 1 or > 100 |

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
| `1005` | `MU_BREACH_ACCOUNT_1005` | Lỗi hệ thống |
| `1006` | `MU_BREACH_ACCOUNT_1006` | Tài khoản không tồn tại |

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
  "code": "MU_BREACH_ACCOUNT_{LOTTE_CODE}",
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
| TradeX Validation | `INVALID_PARAMETER`, `FIELD_IS_REQUIRED` | Missing accountNumber | 400 |
| TradeX Auth | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN` | Invalid JWT | 401/403 |
| Lotte Business | `MU_BREACH_ACCOUNT_{LOTTE_CODE}` | `MU_BREACH_ACCOUNT_1005` | 422 |
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
| `lotte-bridge` | Lotte API integration, request/response mapping (POST body from GET query) |
| `asset-v2` | Business logic, validation (if needed) |
| **Kafka** | Service communication |

### 5.2 Key Principles

**1. Meaningful mapping (TradeX enum → Lotte code):**
- `queryType`: NEW_QUERY → 0, CONTINUOUS_QUERY → 2 (default: NEW_QUERY)
- `settlementStatus`: ALL → 0, NON_MARGIN → 1, MARGIN_CALL → 2, FORCE_SELL → 3 (default: ALL)
- `branch`: ALL → %, hoặc mã chi nhánh pass-through (default: ALL)
- `department`: ALL → %, hoặc mã phòng pass-through (default: ALL)
- `customerType`: ALL → 9, NETTING → 0, NON_NETTING → 1 (default: ALL)
- `warningType`: ALL → %, WARNING_1 → 1, WARNING_2 → 2, WARNING_3 → 3 (default: ALL)

Backend map giá trị có nghĩa (TradeX) sang mã Lotte theo §2.1; không truyền thì dùng default.

**2. Validation Strategy:**
- TradeX validates: accountNumber required, fetch count range
- Lotte validates: Business rules (account exists, MU data)

**3. Message Pass-Through:**
- Success: Return items + pagination
- Error: Pass-through Lotte `error_desc` AS-IS
- Language controlled via `Accept-Language` → Lotte `lang_code`

**4. Error Code Mapping:**
- Lotte business: Prefix format `MU_BREACH_ACCOUNT_{LOTTE_CODE}`

**5. GET vs POST:**
- Client gọi GET với query parameters (có thể gửi queryType, settlementStatus, branch, department, customerType, warningType hoặc bỏ qua để dùng default)
- Backend map TradeX query params → Lotte POST body; field không có thì gán default.

### 5.3 Data Transformation

**Request (TradeX → Lotte):** Dùng map §2.1 và logic §3.2 (QUERY_TYPE_MAP, SETTLEMENT_STATUS_MAP, CUSTOMER_TYPE_MAP, WARNING_TYPE_MAP; branch/department ALL → "%" hoặc pass-through mã).

**Response (Lotte → TradeX):**
```typescript
return {
  accountNumber: lotteItem.account_no,
  accountName: lotteItem.account_name,
  productCode: lotteItem.product_code,
  contractCode: lotteItem.contract_code,
  contractName: lotteItem.contract_name,
  salesTypeCode: lotteItem.sales_type_code,
  salesTypeName: lotteItem.sales_type_name,
  positionsCount: parseInt(lotteItem.positions_count) || 0,
  settlementRate: parseFloat(lotteItem.settlement_rate) || 0,
  settlementQuantity: parseFloat(lotteItem.settlement_quantity) || 0,
  settlementStatusCode: lotteItem.settlement_status_code,
  settlementStatusName: lotteItem.settlement_status_name,
  muRatio: parseFloat(lotteItem.mu_ratio) || 0,
  warningType: lotteItem.warning_type,
  value: parseFloat(lotteItem.value) || 0,
  amount: parseFloat(lotteItem.amount) || 0,
};
```

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team; backend áp dụng default khi request không có queryType, settlementStatus, branch, department, customerType, warningType.
