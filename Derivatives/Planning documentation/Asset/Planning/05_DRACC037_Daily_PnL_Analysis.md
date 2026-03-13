# Phân tích API DRACC-037: Lãi/lỗ theo ngày

**Document Type:** Planning – API Analysis  
**Category:** Derivatives Asset  
**Lotte Code:** DRACC-037  
**Date:** March 13, 2026

---

## 1. Tổng quan

| Mục | Nội dung |
|-----|----------|
| **Tên Lotte** | DRACC-037: Lãi/lỗ theo ngày |
| **Lotte URL** | `[Root URL APIKEY]/tuxsvc/der/account/dr-daily-profit-loss` |
| **Lotte Method** | POST (JSON body) |
| **TradeX Endpoint** | `GET /api/v1/derivatives/asset/dailyPnl` |
| **Mục đích** | Tra cứu lãi/lỗ theo từng ngày trong khoảng thời gian: đã thực hiện, chưa thực hiện, phí, thuế, net P&L (theo tài khoản/sản phẩm/hợp đồng). |

**Luồng:** Client gọi GET (query) → TradeX (rest-proxy / asset-v2) → lotte-bridge chuyển sang POST body → Lotte `dr-daily-profit-loss`.

---

## 2. Request – Lotte (gốc)

| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|--------|
| `account_no` | String | Y | Tên/số tài khoản |
| `password` | String | Y | Mật khẩu đã mã hóa |
| `start_date` | String | Y | Từ ngày (YYYYMMDD) |
| `end_date` | String | Y | Tới ngày (YYYYMMDD) |
| `product_code` | String | Y | Mã sản phẩm |
| `search_type` | Boolean | Y | true: toàn bộ TK, false: từng TK |
| `next_key` | String | Y | Default "0" (pagination) |
| `branch` | String | Y | Default "%" |
| `department` | String | Y | Default "%" |
| `contract_code` | String | Y | Default "%" |
| `hts_user_id` | String | Y | hts_user_id tra cứu (default lthpt01) |

**Lưu ý:** Lotte dùng POST; TradeX không nhận `password` từ client — backend lấy từ session/secure store khi gọi Lotte.

---

## 3. Request – TradeX (GET query)

| Parameter | Type | Required | Default | Map Lotte |
|-----------|------|----------|---------|-----------|
| `accountNumber` | String | ✅ | - | `account_no` |
| `dateFrom` | String | ✅ | - | `start_date` (yyyyMMdd) |
| `dateTo` | String | ✅ | - | `end_date` (yyyyMMdd) |
| `productCode` | String | ✅ | - | `product_code` |
| `searchAllAccounts` | Boolean | ❌ | false | `search_type` |
| `contractCode` | String | ❌ | "%" | `contract_code` |
| `branch` | String | ❌ | "%" | `branch` |
| `department` | String | ❌ | "%" | `department` |
| `nextKey` | String | ❌ | "0" | `next_key` |
| `fetchCount` | Number | ❌ | 50 | (TradeX pagination; Lotte không có row_count trong spec) |

**Tự động:** `userId` / `hts_user_id` từ JWT; `password` từ backend (không từ client).

---

## 4. Response – Lotte (gốc)

- **Chung:** `error_code`, `error_desc`, `success`, `data_list`.
- **DataResponse (từng dòng):**

| Lotte field | Ý nghĩa |
|-------------|---------|
| `date` | Ngày giao dịch |
| `account_no` | Số tài khoản |
| `account_name` | Tên tài khoản |
| `product_code` | Mã sản phẩm |
| `contract_code` | Mã hợp đồng |
| `product_name` | Tên sản phẩm |
| `realized_profit_loss` | Lãi lỗ đã thực hiện |
| `unrealized_profit_loss` | Lãi lỗ chưa thực hiện |
| `fee` | Phí |
| `net_profit_loss` | Net lãi lỗ |
| `next_key` | Token trang tiếp theo |
| `tax` | Thuế |

---

## 5. Response – TradeX (mapping)

**Thành công (200):**

- **items:** Mảng object, mỗi object map từ 1 DataResponse (snake_case → camelCase, số → parse float).
- **pagination:** `hasMore`, `nextKey` (từ `next_key` item cuối hoặc response), `totalRecords` (nếu Lotte có).

| Lotte | TradeX |
|-------|--------|
| `date` | `date` |
| `account_no` | `accountNumber` |
| `account_name` | `accountName` |
| `product_code` | `productCode` |
| `contract_code` | `contractCode` |
| `product_name` | `productName` |
| `realized_profit_loss` | `realizedProfitLoss` (number) |
| `unrealized_profit_loss` | `unrealizedProfitLoss` (number) |
| `fee` | `fee` (number) |
| `net_profit_loss` | `netProfitLoss` (number) |
| `tax` | `tax` (number) |
| `next_key` | `pagination.nextKey` |

**Chuẩn TradeX:** Không dùng field `success: true/false`; dùng HTTP status để biết thành công/lỗi.

---

## 6. Business rules & validation (TradeX)

| Rule | Giá trị / Mô tả |
|------|-----------------|
| Định dạng ngày | yyyyMMdd (vd: 20260301) |
| Khoảng ngày | dateFrom ≤ dateTo |
| Giới hạn khoảng | Tối đa 90 ngày (cần xác nhận với Lotte) |
| fetchCount | 1–100 (default 50) |
| Tra cứu | searchAllAccounts = false → từng TK; true → toàn bộ (cần quyền back-office) |
| Sở hữu tài khoản | Chỉ được tra cứu tài khoản thuộc user đăng nhập |

**Lỗi validation (400):** Thiếu required, sai format ngày, dateFrom > dateTo, range > 90 ngày, fetchCount ngoài 1–100 → INVALID_PARAMETER / FIELD_IS_REQUIRED / INVALID_DATE_FORMAT / INVALID_DATE_RANGE / DATE_RANGE_EXCEEDED / INVALID_FETCH_COUNT.

---

## 7. Error mapping (Lotte → TradeX)

| Nguồn | HTTP | Code TradeX | Ghi chú |
|-------|------|-------------|--------|
| Validation TradeX | 400 | INVALID_PARAMETER, FIELD_IS_REQUIRED, … | Thiếu/sai tham số, ngày, range |
| Auth | 401/403 | UNAUTHORIZED, TOKEN_EXPIRED, FORBIDDEN, UNAUTHORIZED_ACCOUNT | Token/sở hữu TK |
| Lotte business | 422 | DAILY_PNL_{LOTTE_CODE} (vd: DAILY_PNL_1005, DAILY_PNL_1006) | Pass-through error_desc |
| Hệ thống | 500 | INTERNAL_ERROR | Lotte down / lỗi nội bộ |

Lotte thường dùng: 1005 (lỗi hệ thống), 1006 (tài khoản không tồn tại), 2001 (không có dữ liệu).

---

## 8. Kiến trúc & triển khai

| Thành phần | Vai trò |
|------------|---------|
| rest-proxy | Gateway, JWT, route GET `/api/v1/derivatives/asset/dailyPnl` |
| lotte-bridge | Đổi GET query → POST body Lotte, gọi dr-daily-profit-loss, map response |
| asset-v2 | (Tùy thiết kế) Validation, orchestration |

**Cần làm rõ với Lotte:**

- Giới hạn chính xác cho start_date/end_date (vd. max 90 ngày).
- Có trả totalRecords hay không; nếu có thì field nào.
- Vị trí next_key (trong từng item hay chỉ ở top-level).

---

## 9. Tóm tắt

- **DRACC-037** = Lotte API Lãi/lỗ theo ngày (dr-daily-profit-loss), POST, có auth API KEY và body gồm account, password, khoảng ngày, product, search_type, next_key, branch, department, contract_code, hts_user_id.
- **TradeX** expose GET `/api/v1/derivatives/asset/dailyPnl` với query (accountNumber, dateFrom, dateTo, productCode, searchAllAccounts, contractCode, branch, department, nextKey, fetchCount); backend tự điền hts_user_id và password khi gọi Lotte.
- Response: danh sách dòng P&L theo ngày (realized, unrealized, fee, tax, net) + pagination; số từ Lotte parse sang number; lỗi Lotte trả nguyên dạng với code DAILY_PNL_{LOTTE_CODE}.

---

## Tham chiếu

| Tài liệu | Đường dẫn |
|----------|-----------|
| Spec TradeX (Daily P&L API) | [Daily_PnL_API_Spec](../Specifications/Daily_PnL_API_Spec.md) |
| Lotte DR §2.1.8 | `Derivatives/Documentation/[API specs]Lotte_DR.md` |

---

**Document Status:** ✅ Complete  
**For:** PM / BA  
**Next Steps:** Triển khai theo Daily_PnL_API_Spec; xác nhận với Lotte giới hạn khoảng ngày và cách trả totalRecords/next_key.
