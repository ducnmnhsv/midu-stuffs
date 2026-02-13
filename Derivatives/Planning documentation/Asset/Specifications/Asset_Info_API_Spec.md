# Asset Info API Specification (Derivatives)

**Document Type:** API Specification  
**Category:** Derivatives Asset - Account Information  
**Version:** 1.0  
**Date:** February 13, 2026

> **Note:** Lotte-integrated API for **Derivatives only**. Map 1-1 với Lotte DRACC-031 (dr-balance-securities-info) – không tổng hợp, không thêm/bớt field.

---

## 1. Overview

### 1.1 Purpose

Asset Info API tra cứu thông tin tài sản và chứng khoán (DRACC-031). Response map 1-1 từ Lotte `data_list` – mỗi Lotte field tương ứng đúng 1 TradeX field (camelCase).

### 1.2 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Asset Info | GET | `/api/v1/derivatives/asset/info` |

### 1.3 Response Format Standards

**Success (Query):** Trả về `dataList` – mỗi item map 1-1 từ Lotte DataResponse (5 fields).

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
- Response: `dataList` array, mỗi item map 1-1 từ Lotte (5 fields)
- Pass-through Lotte messages AS-IS

---

## 2. Business Rules

### 2.1 Lotte DataResponse (1-1, không tổng hợp)

Lotte trả về `data_list` – mỗi item có đầy đủ fields sau (theo [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) §2.1.5 DRACC-031):

| Lotte Field | TradeX Field | Type | Transform | Mô tả (Lotte) |
|-------------|--------------|------|-----------|---------------|
| `available_balance` | `availableBalance` | String | Direct | Số dư tiền mặt khả dụng |
| `current_balance` | `currentBalance` | String | Direct | Số dư tiền mặt hiện có |
| `quantity_stock` | `quantityStock` | String | Direct | Số lượng chứng khoán khả dụng |
| `value_stock` | `valueStock` | String | Direct | Giá trị chứng khoán khả dụng |
| `current_quantity_stock` | `currentQuantityStock` | String | Direct | Số lượng chứng khoán hiện có |
| `current_value_stock` | `currentValueStock` | String | Direct | Giá trị chứng khoán hiện có |
| `margin_asset_fee` | `marginAssetFee` | String | Direct | Phí quản lý TSKQ |
| `shortfall_amount` | `shortfallAmount` | String | Direct | Số tiền thiếu |
| `position_fee` | `positionFee` | String | Direct | Phí vị thế |
| `hnx_fee` | `hnxFee` | String | Direct | Phí HNX |
| `begin_margin_balance` | `beginMarginBalance` | String | Direct | Số dư tiền ký quỹ đầu ngày |
| `margin_deposit_withdrawal` | `marginDepositWithdrawal` | String | Direct | Tiền ký quỹ nộp / rút |
| `current_margin_balance` | `currentMarginBalance` | String | Direct | Số dư tiền ký quỹ hôm nay |
| `margin_withdrawal` | `marginWithdrawal` | String | Direct | Tiền ký quỹ rút |
| `value_margin_securities` | `valueMarginSecurities` | String | Direct | Giá trị chứng khoán ký quỹ |
| `pending_value_withdrawal_margin_securities` | `pendingValueWithdrawalMarginSecurities` | String | Direct | Giá trị chứng khoán ký quỹ chờ rút |
| `accepted_collateral_assets` | `acceptedCollateralAssets` | String | Direct | Tài sản đảm bảo được chấp nhận |
| `realized_interest` | `realizedInterest` | String | Direct | Lãi thật thực |
| `fees` | `fees` | String | Direct | Phí |
| `unrealized_interest` | `unrealizedInterest` | String | Direct | Lãi chưa ghi nhận |
| `unmatched_order_request_index` | `unmatchedOrderRequestIndex` | String | Direct | Gửi yêu cầu của lệnh chưa khớp |
| `margin_shortfall` | `marginShortfall` | String | Direct | Thâm hụt ký quỹ |
| `withdrawable_collateral_assets` | `withdrawableCollateralAssets` | String | Direct | Tài sản đảm bảo có thể rút |
| `value_withdrawable_collateral_assets` | `valueWithdrawableCollateralAssets` | String | Direct | Giá trị tài sản đảm bảo có thể rút |
| `withdrawable_margin_securities` | `withdrawableMarginSecurities` | String | Direct | Chứng khoán ký quỹ có thể rút |
| `accepted_margin_securities_values` | `acceptedMarginSecuritiesValues` | String | Direct | Giá trị chứng khoán ký quỹ được chấp nhận |
| `tax` | `tax` | String | Direct | Thuế |
| `field_margin_cash_deposit_withdrawal` | `fieldMarginCashDepositWithdrawal` | String | Direct | Tiền ký quỹ nộp rút |
| `margin_cash_deposit_withdrawal` | `marginCashDepositWithdrawal` | String | Direct | Tiền ký quỹ nộp rút |
| `margin_deposit_withdrawal_vsd` | `marginDepositWithdrawalVsd` | String | Direct | Tiền ký quỹ nộp rút tại VSD |
| `begin_margin_cash_balance_nhsv` | `beginMarginCashBalanceNhsv` | String | Direct | Số dư tiền ký quỹ đầu ngày tại NHSV |
| `withdrawable_margin_securities_value` | `withdrawableMarginSecuritiesValue` | String | Direct | Giá trị chứng khoán ký quỹ có thể rút |
| `withdrawable_margin_cash` | `withdrawableMarginCash` | String | Direct | Tiền ký quỹ có thể rút |
| `pending_margin_cash_withdrawal_balance_nhsv` | `pendingMarginCashWithdrawalBalanceNhsv` | String | Direct | Số dư tiền ký quỹ chờ rút tại NHSV |
| `begin_margin_cash_balance` | `beginMarginCashBalance` | String | Direct | Số dư tiền ký quỹ đầu ngày |
| `pending_margin_cash_withdrawal_nhsv` | `pendingMarginCashWithdrawalNhsv` | String | Direct | Tiền ký quỹ chờ rút tại NHSV |
| `pending_margin_cash_withdrawal_vsd` | `pendingMarginCashWithdrawalVsd` | String | Direct | Tiền ký quỹ chờ rút tại VSD |
| `accepted_collateral_balance_nhsv` | `acceptedCollateralBalanceNhsv` | String | Direct | Số dư đảm bảo được chấp nhận tại NHSV |
| `accepted_margin_securities_value_nhsv` | `acceptedMarginSecuritiesValueNhsv` | String | Direct | Giá trị CK KQ được chấp nhận tại NHSV |
| `accepted_collateral_balance_vsd` | `acceptedCollateralBalanceVsd` | String | Direct | Số dư đảm bảo được chấp nhận tại VSD |
| `margin_cash_balance_nhsv` | `marginCashBalanceNhsv` | String | Direct | Số dư tiền ký quỹ tại NHSV |
| `pending_withdrawal_margin_securities_nhsv` | `pendingWithdrawalMarginSecuritiesNhsv` | String | Direct | Giá trị CK KQ chờ rút tại NHSV |
| `margin_cash_balance_vsd` | `marginCashBalanceVsd` | String | Direct | Số dư tiền ký quỹ tại VSD |
| `pending_withdrawal_margin_securities_vsd` | `pendingWithdrawalMarginSecuritiesVsd` | String | Direct | Giá trị CK KQ chờ rút tại VSD |
| `accepted_margin_securities_vsd` | `acceptedMarginSecuritiesVsd` | String | Direct | Giá trị CK KQ được chấp nhận tại VSD |
| `margin_securities_value_vsd` | `marginSecuritiesValueVsd` | String | Direct | Giá trị chứng khoán ký quỹ tại VSD |
| `value_required_vsd` | `valueRequiredVsd` | String | Direct | Giá trị VSD yêu cầu |

### 2.2 Validation Rules (TradeX)

| Rule | Description | Error Code |
|------|-------------|------------|
| Required Fields | accountNumber | `FIELD_IS_REQUIRED` |
| Account Format | 7-digit account number | `INVALID_ACCOUNT_FORMAT` |
| Account Ownership | Account must belong to authenticated user | `UNAUTHORIZED_ACCOUNT` |

### 2.3 Default Values

| Parameter | Default | Description |
|-----------|---------|-------------|
| `inquiryDate` | Today (yyyyMMdd) | Ngày tra cứu - tự động set nếu không truyền |

### 2.4 Language Mapping

| Accept-Language | Lotte lang_code | Message Example |
|-----------------|-----------------|-----------------|
| `vi` | `V` | `"[V3120] Lỗi đã xảy ra..."` |
| `en` | `E` | `"[E3120] Error occurred..."` |
| `ko` | `K` | `"[K3120] 오류 발생..."` |

---

## 3. API: Get Asset Info

### 3.1 Request

**Endpoint:** `GET /api/v1/derivatives/asset/info`

**Lotte Endpoint:** `[Root URL APIKEY]/tuxsvc/der/account/dr-balance-securities-info` (DRACC-031)

**Lotte Doc:** [Lotte_DR.md](../../../Documentation/[API%20specs]Lotte_DR.md) §2.1.5

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `accountNumber` | String | ✅ | - | Số tài khoản phái sinh |
| `inquiryDate` | String | ❌ | Today | Ngày tra cứu (yyyyMMdd) |

### 3.2 Request Mapping

**TradeX → Lotte (1-1):**

| TradeX Field | Type | Required | Lotte Field | Transform | Description |
|--------------|------|----------|-------------|-----------|-------------|
| `accountNumber` | String | ✅ | `account_no` | Direct | Số tài khoản |
| `inquiryDate` | String | ✅ | `inquiry_date` | Default: today (yyyyMMdd) | Ngày tra cứu |
| *(JWT)* `userId` | - | - | `hts_user_id` | Auto | hts_user_id của TK tra cứu |
| - | - | - | `apiKey` | Header | API KEY |

**Note:** Lotte request: `account_no`, `inquiry_date`, `hts_user_id` (cả 3 Y). TradeX default `inquiryDate` = today nếu không truyền.

### 3.3 Response Mapping

**Success (200):** Map 1-1 từ Lotte `data_list`. Mỗi Lotte field → 1 TradeX field (camelCase). Không tổng hợp. Lotte trả String → TradeX giữ String.

| Lotte Field | TradeX Field | Mô tả (Lotte) |
|-------------|--------------|---------------|
| `available_balance` | `availableBalance` | Số dư tiền mặt khả dụng |
| `current_balance` | `currentBalance` | Số dư tiền mặt hiện có |
| `quantity_stock` | `quantityStock` | Số lượng chứng khoán khả dụng |
| `value_stock` | `valueStock` | Giá trị chứng khoán khả dụng |
| `current_quantity_stock` | `currentQuantityStock` | Số lượng chứng khoán hiện có |
| `current_value_stock` | `currentValueStock` | Giá trị chứng khoán hiện có |
| `margin_asset_fee` | `marginAssetFee` | Phí quản lý TSKQ |
| `shortfall_amount` | `shortfallAmount` | Số tiền thiếu |
| `position_fee` | `positionFee` | Phí vị thế |
| `hnx_fee` | `hnxFee` | Phí HNX |
| `begin_margin_balance` | `beginMarginBalance` | Số dư tiền ký quỹ đầu ngày |
| `margin_deposit_withdrawal` | `marginDepositWithdrawal` | Tiền ký quỹ nộp / rút |
| `current_margin_balance` | `currentMarginBalance` | Số dư tiền ký quỹ hôm nay |
| `margin_withdrawal` | `marginWithdrawal` | Tiền ký quỹ rút |
| `value_margin_securities` | `valueMarginSecurities` | Giá trị chứng khoán ký quỹ |
| `pending_value_withdrawal_margin_securities` | `pendingValueWithdrawalMarginSecurities` | Giá trị CK ký quỹ chờ rút |
| `accepted_collateral_assets` | `acceptedCollateralAssets` | Tài sản đảm bảo được chấp nhận |
| `realized_interest` | `realizedInterest` | Lãi thật thực |
| `fees` | `fees` | Phí |
| `unrealized_interest` | `unrealizedInterest` | Lãi chưa ghi nhận |
| `unmatched_order_request_index` | `unmatchedOrderRequestIndex` | Gửi yêu cầu lệnh chưa khớp |
| `margin_shortfall` | `marginShortfall` | Thâm hụt ký quỹ |
| `withdrawable_collateral_assets` | `withdrawableCollateralAssets` | Tài sản đảm bảo có thể rút |
| `value_withdrawable_collateral_assets` | `valueWithdrawableCollateralAssets` | Giá trị TĐB có thể rút |
| `withdrawable_margin_securities` | `withdrawableMarginSecurities` | Chứng khoán ký quỹ có thể rút |
| `accepted_margin_securities_values` | `acceptedMarginSecuritiesValues` | Giá trị CK KQ được chấp nhận |
| `tax` | `tax` | Thuế |
| `field_margin_cash_deposit_withdrawal` | `fieldMarginCashDepositWithdrawal` | Tiền ký quỹ nộp rút |
| `margin_cash_deposit_withdrawal` | `marginCashDepositWithdrawal` | Tiền ký quỹ nộp rút |
| `margin_deposit_withdrawal_vsd` | `marginDepositWithdrawalVsd` | Tiền KQ nộp rút tại VSD |
| `begin_margin_cash_balance_nhsv` | `beginMarginCashBalanceNhsv` | Số dư KQ đầu ngày tại NHSV |
| `withdrawable_margin_securities_value` | `withdrawableMarginSecuritiesValue` | Giá trị CK KQ có thể rút |
| `withdrawable_margin_cash` | `withdrawableMarginCash` | Tiền ký quỹ có thể rút |
| `pending_margin_cash_withdrawal_balance_nhsv` | `pendingMarginCashWithdrawalBalanceNhsv` | Số dư KQ chờ rút tại NHSV |
| `begin_margin_cash_balance` | `beginMarginCashBalance` | Số dư tiền ký quỹ đầu ngày |
| `pending_margin_cash_withdrawal_nhsv` | `pendingMarginCashWithdrawalNhsv` | Tiền KQ chờ rút tại NHSV |
| `pending_margin_cash_withdrawal_vsd` | `pendingMarginCashWithdrawalVsd` | Tiền KQ chờ rút tại VSD |
| `accepted_collateral_balance_nhsv` | `acceptedCollateralBalanceNhsv` | Số dư đảm bảo tại NHSV |
| `accepted_margin_securities_value_nhsv` | `acceptedMarginSecuritiesValueNhsv` | Giá trị CK KQ tại NHSV |
| `accepted_collateral_balance_vsd` | `acceptedCollateralBalanceVsd` | Số dư đảm bảo tại VSD |
| `margin_cash_balance_nhsv` | `marginCashBalanceNhsv` | Số dư tiền KQ tại NHSV |
| `pending_withdrawal_margin_securities_nhsv` | `pendingWithdrawalMarginSecuritiesNhsv` | Giá trị CK KQ chờ rút tại NHSV |
| `margin_cash_balance_vsd` | `marginCashBalanceVsd` | Số dư tiền KQ tại VSD |
| `pending_withdrawal_margin_securities_vsd` | `pendingWithdrawalMarginSecuritiesVsd` | Giá trị CK KQ chờ rút tại VSD |
| `accepted_margin_securities_vsd` | `acceptedMarginSecuritiesVsd` | Giá trị CK KQ tại VSD |
| `margin_securities_value_vsd` | `marginSecuritiesValueVsd` | Giá trị CK ký quỹ tại VSD |
| `value_required_vsd` | `valueRequiredVsd` | Giá trị VSD yêu cầu |

**Response Structure:** Lotte `data_list` thường 1 item (1 tài khoản, 1 ngày). TradeX trả nguyên `dataList` array.

### 3.4 Error Mapping

**Validation Error (400) - TradeX:**

| Field | Error Code | messageParams | Condition |
|-------|------------|---------------|-----------|
| `accountNumber` | `FIELD_IS_REQUIRED` | `["accountNumber"]` | Missing |
| `accountNumber` | `INVALID_ACCOUNT_FORMAT` | `["accountNumber"]` | Wrong format (not 7 digits) |

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
| `1005` | `ASSET_INFO_1005` | Lỗi hệ thống |
| `1006` | `ASSET_INFO_1006` | Tài khoản không tồn tại |

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
  "code": "ASSET_INFO_{LOTTE_CODE}",
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
| Lotte Business | `ASSET_INFO_{LOTTE_CODE}` | `ASSET_INFO_1005` | 422 |
| System Error | `INTERNAL_ERROR` | Lotte API down | 500 |

### 4.3 Common Lotte Error Codes

| Code | Description (VI) | Description (EN) |
|------|------------------|------------------|
| `1005` | Lỗi hệ thống | System error |
| `1006` | Tài khoản không tồn tại | Account not found |

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
- Lotte validates: Business rules (account exists)
- NO duplicate business logic

**2. Message Pass-Through:**
- Success: Return asset info
- Error: Pass-through Lotte `error_desc` AS-IS
- TradeX NEVER transforms or translates Lotte messages
- Language controlled via `Accept-Language` → Lotte `lang_code`

**3. Error Code Mapping:**
- TradeX validation: Direct error code (`INVALID_PARAMETER`)
- Lotte business: Prefix format `ASSET_INFO_{LOTTE_CODE}`
- Example: Lotte `1005` → TradeX `ASSET_INFO_1005`

**4. Auto-Population:**
- `userId`, `name`, `identifierNumber` → From JWT token
- `sourceIp` → From request IP
- `lang_code` → From `Accept-Language` header

**5. Default Values:**
- `inquiryDate`: Default today (yyyyMMdd) if not provided

**6. 1-1 Mapping:**
- Không tổng hợp field
- Không tính toán thêm (margin ratio, NAV, etc.)
- Chỉ rename: snake_case → camelCase

### 5.3 Data Transformation (1-1, không parse)

**Rule:** Map từng field trực tiếp. Lotte trả String → TradeX giữ String. Chỉ convert snake_case → camelCase cho toàn bộ 44 fields.

---

**Document Status:** ✅ Complete  
**For:** BA/Dev  
**Next Steps:** Implementation by Dev team
