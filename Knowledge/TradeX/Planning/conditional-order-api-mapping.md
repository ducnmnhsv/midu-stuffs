# TradeX Conditional Order API Mapping

**Document Type:** API Analysis & Mapping
**Category:** Orders - Conditional Orders (Lệnh Điều Kiện)
**Service:** `order-v2`
**Date:** June 30, 2026
**Version:** 1.0

---

## Table of Contents

1. [Overview](#overview)
2. [Conditional vs Regular Orders](#conditional-vs-regular-orders)
3. [Stop Order](#stop-order)
4. [OCO Order](#oco-order)
5. [Trailing Order](#trailing-order)
6. [Bull/Bear Order](#bullbear-order)
7. [API Reference Table](#api-reference-table)
8. [Enums Reference](#enums-reference)
9. [Multi-Broker Domain](#multi-broker-domain)
10. [Data Model Summary](#data-model-summary)
11. [Auto-Populated Fields](#auto-populated-fields)
12. [Source Files Reference](#source-files-reference)

---

## Overview

### Purpose

This document maps the **conditional order** (lệnh điều kiện) APIs handled by the `order-v2` service. Conditional orders are advanced orders that TradeX stores and monitors itself, then sends a *real* order to the broker only when a price/market condition is met.

> **Companion document:** [regular-order-api-mapping.md](./regular-order-api-mapping.md) — covers the *regular* orders (Buy/Sell/Cancel/Modify) handled by `lotte-bridge` and `rest-proxy → tuxedo`. Conditional orders below are a **separate domain** owned by `order-v2`.

### Scope

Four families of conditional orders:

- **Stop Order** (Lệnh dừng / cắt lỗ) — STOP and STOP_LIMIT
- **OCO Order** (One-Cancels-Other) — chốt lời / cắt lỗ tự động
- **Trailing Order** (Lệnh trailing stop) — stop price trượt theo giá
- **Bull/Bear Order** — đặt lệnh kèm quản lý chốt lời / cắt lỗ tự động

### Architecture Flow

```
Client/App → rest-proxy → Kafka (topic: order) → order-v2
                                                     │
                                                     ▼
                                          Store in TradeX DB (MySQL)
                                                     │
                                          Monitor real-time quotes
                                          (QuoteHandler + OrderTriggerService)
                                                     │
                          [Khi điều kiện giá khớp] → place lệnh thật
                                                     │
                                                     ▼
                                          lotte-bridge / tuxedo → Lotte
```

- **Tech stack:** Spring Boot 3.1.1 · Java 17 · MySQL · Kafka · Redis · JPA/Hibernate
- **Kafka topic:** `order` (`spring.application.name = order`; `requestListener = clusterId`)
- **Dual-mode:** service chạy ở 2 mode — `ENGINE` (ghi: place/modify/cancel) và `QUERY` (đọc: history/today/detail). Request sai mode được tự động forward sang cluster đúng.

---

## Conditional vs Regular Orders

| Đặc điểm | Regular Order | Conditional Order (order-v2) |
|----------|---------------|------------------------------|
| Service | `lotte-bridge`, `tuxedo` | `order-v2` |
| Integration type | **lotte-integrated** (gọi thẳng Lotte) | **tradex-native** (lưu DB TradeX, monitor, rồi mới gọi broker khi trigger) |
| Định danh lệnh | `orderNumber` (số hiệu lệnh sàn) | `id` (ID nội bộ TradeX, kiểu Long) |
| Place response | `{ message, orderNumber }` | `{ id }` (TradeX-native) |
| Trigger | Khớp ngay tại sàn | Theo điều kiện giá real-time |
| Endpoint base | `/api/v1/equity/order`, `/api/v1/derivatives/order` | `/api/v1/stopOrder`, `/ocoOrder`, `/trailingOrder`, `/bullBear` |

> **Lưu ý naming:** order-v2 dùng tên TradeX (`accountNumber`, `code`, `sellBuyType`, `orderQuantity`...), KHÔNG dùng tên field Lotte. Việc map sang field Lotte chỉ xảy ra ở bước trigger (đặt lệnh thật qua bridge) — xem [regular-order-api-mapping.md](./regular-order-api-mapping.md) cho bảng mapping field Lotte.

---

## Stop Order

Lệnh kích hoạt khi giá thị trường chạm `stopPrice`. Khi trigger, thực thi như lệnh thị trường (`STOP`) hoặc lệnh giới hạn tại `orderPrice` (`STOP_LIMIT`).

### 1. Place Stop Order

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/stopOrder` |
| **Service** | `order-v2` → `StopOrderService.placeStopOrder()` |
| **Mode** | ENGINE |

#### Request Structure

```typescript
{
  accountNumber: string,        // Bắt buộc — phải thuộc về user (validate)
  subNumber?: string,           // Tiểu khoản
  code: string,                 // Bắt buộc — mã CK / mã hợp đồng
  orderQuantity: number,        // Bắt buộc — khối lượng (Long)
  sellBuyType: "BUY" | "SELL",  // Bắt buộc
  orderType: "STOP" | "STOP_LIMIT", // Bắt buộc
  stopPrice: number,            // Bắt buộc — phải > 0
  orderPrice?: number,          // Bắt buộc nếu orderType = STOP_LIMIT
  fromDate?: string,            // Optional — hiệu lực từ
  toDate?: string,              // Optional — hiệu lực đến
  remark?: string,
  macAddress?: string,
  deviceUniqueId?: string,
  bankCode?: string,            // Chỉ dùng cho domain lotte
  bankAccount?: string,         // Chỉ dùng cho domain lotte
  bankName?: string             // Chỉ dùng cho domain lotte
}
```

#### Response Structure

```typescript
{
  id: number                    // ID stop order trong DB TradeX
}
```

#### Business Rules

1. `accountNumber` bắt buộc và **phải nằm trong danh sách tài khoản của user** (token), nếu không → `UNABLE_TO_STOP_ORDERS_OF_ANOTHER_ACCOUNT`.
2. `stopPrice` phải > 0 → nếu không, `STOP_PRICE_MUST_BE_SET`.
3. Nếu `orderType = STOP_LIMIT` → `orderPrice` bắt buộc.
4. Trigger condition: **BUY** trigger khi `stopPrice >= currentPrice`; **SELL** trigger khi `stopPrice <= currentPrice`.
5. Trạng thái khởi tạo: `PENDING`.

### 2. Modify Stop Order

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/stopOrder/modify` |
| **Method** | `StopOrderService.modifyStopOrder()` |

```typescript
{
  stopOrderId: number,          // Bắt buộc
  orderQuantity: number,        // Bắt buộc
  stopPrice: number,            // Bắt buộc
  orderPrice?: number,          // Optional (validate khi > 0)
  fromDate: string,             // Bắt buộc
  toDate: string                // Bắt buộc
}
```

### 3. Cancel Stop Order

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/stopOrder/cancel` |
| **Method** | `StopOrderService.cancelStopOrder()` |

```typescript
{ stopOrderId: number }
```

### 4. Cancel Multiple Stop Orders

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/stopOrder/cancel/multi` |
| **Method** | `StopOrderService.cancelMultiStopOrders()` |

```typescript
{ idList: number[] }            // Không rỗng, nếu không → INVALID_PARAMETER
```

### 5. Speed Modify Stop Order

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/stopOrder/speedModify` |
| **Method** | `StopOrderService.modifySpeedStopOrder()` |

Sửa nhanh stop price theo định danh logic (account + code + side + giá), không cần `stopOrderId`.

```typescript
{
  accountNumber: string,
  subNumber?: string,
  code: string,
  sellBuyType: "BUY" | "SELL",
  stopPrice: number,            // Giá dừng hiện tại
  newStopPrice: number          // Giá dừng mới
}
```

### 6. Speed Cancel Stop Order

| Property | Value |
|----------|-------|
| **Endpoint** | `PUT /api/v1/stopOrder/speedCancel` |
| **Method** | `StopOrderService.cancelSpeedStopOrder()` |

```typescript
{
  accountNumber: string,
  subNumber?: string,
  sellBuyType: "BUY" | "SELL",
  code: string,
  stopPrice: number
}
```

### 7. Query Stop Order History

| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/stopOrder/history` |
| **Mode** | QUERY |

```typescript
{
  accountNumber: string,
  subNumber?: string,
  code?: string,
  sellBuyType?: string,
  orderType?: string,
  status?: string,
  lastStopOrderId?: number,     // Phân trang theo id
  fetchCount?: number,          // Default 20, max 100
  fromDate?: string,
  toDate?: string
}
```

### 8. Get Stop Order Last Update

| Property | Value |
|----------|-------|
| **Endpoint** | `GET /api/v1/stopOrder/lastUpdate` |
| **Mode** | QUERY |

```typescript
{ fromTime: string }            // Format: yyyyMMddHHmmss
```

---

## OCO Order

OCO (One-Cancels-Other): tạo cặp lệnh chốt lời + cắt lỗ. Khi một lệnh thực thi, lệnh còn lại tự huỷ. Nội bộ gồm `BullBearOrder` (lệnh chính) + `OcoOrder` (lệnh đối ứng) + `ProfitLossOrder`.

### 1. Place OCO Order

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/ocoOrder` |
| **Method** | `OcoOrderService.addOcoOrder()` |
| **Mode** | ENGINE |

```typescript
{
  accountNumber: string,        // Bắt buộc
  code: string,                 // Bắt buộc
  quantity: number,             // Bắt buộc
  sellBuyType: "BUY" | "SELL",  // Bắt buộc
  profitPrice: number,          // Bắt buộc — giá chốt lời
  triggerLossPrice: number,     // Bắt buộc — giá kích hoạt cắt lỗ
  toler: number                 // Bắt buộc — tolerance/slippage cho lệnh cắt lỗ
}
```

#### Response Structure

```typescript
{ id: number }
```

### 2. Cancel OCO Order

`PUT /api/v1/ocoOrder/cancel` — `{ ocoOrderId: number }`

### 3. Query OCO (History / Today / Detail)

| Endpoint | Request fields |
|----------|----------------|
| `GET /api/v1/ocoOrder/history` | `accountNumber, code?, sellBuyType?, status?` + `fetchCount?, fromDate?, toDate?` |
| `GET /api/v1/ocoOrder/today` | `accountNumber, code?, sellBuyType?, status?` + `fetchCount?` |
| `GET /api/v1/ocoOrder/detail` | `{ id: number }` |

#### History Response (per item)

```typescript
{
  id, accountNumber, code,
  orderQuantity, sellBuyType,
  profitPrice, triggerLossPrice, toler,
  status,
  matchedQuantity, unmatchedQuantity,
  createTime, orderTime, cancelTime,    // format dd/MM/yyyy HH:mm:ss
  errorMessage
}
```

---

## Trailing Order

Stop order có `stopPrice` trượt theo giá khi giá đi thuận chiều.

### 1. Place Trailing Order

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/trailingOrder` |
| **Method** | `TrailingOrderService.addTrailingOrder()` |
| **Mode** | ENGINE |

```typescript
{
  accountNumber: string,        // Bắt buộc
  code: string,                 // Bắt buộc
  quantity: number,             // Bắt buộc
  sellBuyType: "BUY" | "SELL",  // Bắt buộc
  trailingAmount: number,       // Bắt buộc — biên độ trượt theo giá
  limitOffset: number           // Bắt buộc — offset từ stop price cho lệnh limit
}
```

#### Response Structure

```typescript
{ trailingOrderId: number }     // Lưu ý: tên field khác các loại khác (không phải `id`)
```

#### Stop Price Calculation

- **BUY:** `stopPrice = currentPrice − trailingAmount` (chỉ tăng khi giá tăng)
- **SELL:** `stopPrice = currentPrice + trailingAmount` (chỉ giảm khi giá giảm)

### 2. Cancel Trailing Order

`PUT /api/v1/trailingOrder/cancel` — `{ trailingOrderId: number }`

### 3. Query Trailing Order History

`GET /api/v1/trailingOrder/history`

```typescript
{
  accountNumber, code?, sellBuyType?, status?,
  name?, lastTrailingOrderId?,
  fetchCount?, fromDate?, toDate?
}
```

---

## Bull/Bear Order

Đặt lệnh ban đầu kèm tham số chốt lời / cắt lỗ. Khi lệnh khớp → tự tạo `ProfitLossOrder` (TAKE_PROFIT / CUT_LOSS), chỉ một trong hai thực thi.

### 1. Place Bull/Bear Order

| Property | Value |
|----------|-------|
| **Endpoint** | `POST /api/v1/bullBear` |
| **Method** | `BullBearOrderService.addBullBearOrder()` |
| **Mode** | ENGINE |

```typescript
{
  accountNumber: string,        // Bắt buộc
  code: string,                 // Bắt buộc
  quantity: number,             // Bắt buộc
  sellBuyType: "BUY" | "SELL",  // Bắt buộc
  price: number,                // Bắt buộc — giá đặt ban đầu
  profitPrice: number,          // Bắt buộc — giá chốt lời
  triggerLossPrice: number,     // Bắt buộc — giá kích hoạt cắt lỗ
  toler: number                 // Bắt buộc — tolerance/slippage
}
```

#### Response Structure

```typescript
{ id: number }
```

### 2. Cancel Bull/Bear Order

`PUT /api/v1/bullBear/cancel` — `{ bullBearOrderId: number }`

### 3. Query Bull/Bear (History / Today / Detail)

| Endpoint | Request fields |
|----------|----------------|
| `GET /api/v1/bullBear/history` | `accountNumber, code?, sellBuyType?, status?` + `fetchCount?, fromDate?, toDate?` |
| `GET /api/v1/bullBear/today` | `accountNumber, code?, sellBuyType?, status?` + `fetchCount?` |
| `GET /api/v1/bullBear/detail` | `{ id: number }` |

---

## API Reference Table

| Operation | Method | Endpoint | Mode | Service Method |
|-----------|--------|----------|------|----------------|
| **STOP ORDER** | | | | |
| Place | POST | `/api/v1/stopOrder` | ENGINE | `placeStopOrder` |
| Modify | PUT | `/api/v1/stopOrder/modify` | ENGINE | `modifyStopOrder` |
| Cancel | PUT | `/api/v1/stopOrder/cancel` | ENGINE | `cancelStopOrder` |
| Cancel Multi | PUT | `/api/v1/stopOrder/cancel/multi` | ENGINE | `cancelMultiStopOrders` |
| Speed Modify | PUT | `/api/v1/stopOrder/speedModify` | ENGINE | `modifySpeedStopOrder` |
| Speed Cancel | PUT | `/api/v1/stopOrder/speedCancel` | ENGINE | `cancelSpeedStopOrder` |
| History | GET | `/api/v1/stopOrder/history` | QUERY | `queryStopOrderHistory` |
| Last Update | GET | `/api/v1/stopOrder/lastUpdate` | QUERY | `queryStopOrderLastUpdate` |
| **OCO ORDER** | | | | |
| Place | POST | `/api/v1/ocoOrder` | ENGINE | `addOcoOrder` |
| Cancel | PUT | `/api/v1/ocoOrder/cancel` | ENGINE | `cancelOcoOrder` |
| History | GET | `/api/v1/ocoOrder/history` | QUERY | `queryOcoOrderHistory` |
| Today | GET | `/api/v1/ocoOrder/today` | QUERY | `queryOcoOrderToday` |
| Detail | GET | `/api/v1/ocoOrder/detail` | QUERY | `queryOcoOrderDetail` |
| **TRAILING ORDER** | | | | |
| Place | POST | `/api/v1/trailingOrder` | ENGINE | `addTrailingOrder` |
| Cancel | PUT | `/api/v1/trailingOrder/cancel` | ENGINE | `cancelTrailingOrder` |
| History | GET | `/api/v1/trailingOrder/history` | QUERY | `queryTrailingOrderHistory` |
| **BULL/BEAR ORDER** | | | | |
| Place | POST | `/api/v1/bullBear` | ENGINE | `addBullBearOrder` |
| Cancel | PUT | `/api/v1/bullBear/cancel` | ENGINE | `cancelBullBearOrder` |
| History | GET | `/api/v1/bullBear/history` | QUERY | `queryBullBearOrderHistory` |
| Today | GET | `/api/v1/bullBear/today` | QUERY | `queryBullBearOrderToday` |
| Detail | GET | `/api/v1/bullBear/detail` | QUERY | `queryBullBearOrderDetail` |

---

## Enums Reference

> Định nghĩa enum nằm ở `tradex-common-java`. Giá trị dưới đây lấy trực tiếp từ code (đã verify).

### SellBuyTypeEnum
`BUY`, `SELL`

### StopOrderTypeEnum
`STOP` (khớp như lệnh thị trường khi chạm stopPrice), `STOP_LIMIT` (khớp như lệnh giới hạn tại orderPrice)

### Status Enums

| Enum | Giá trị |
|------|---------|
| `StopOrderStatusEnum` | PENDING, COMPLETED, CANCELLED, FAILED, SENDING |
| `TrailingOrderStatusEnum` | PENDING, COMPLETED, CANCELLED, FAILED, SENDING |
| `BullBearOrderStatusEnum` | PENDING, COMPLETED, CANCELLED, FAILED, SENDING |
| `OcoOrderStatusEnum` | PENDING, COMPLETED, CANCELLED, FAILED, **EXPIRED** |
| `ProfitLossOrderStatusEnum` | SENDING, PENDING, COMPLETED, CANCELLED, FAILED, EXPIRED |

> ⚠️ `OcoOrderStatusEnum` **không có** giá trị `ACTIVATED` (doc nội bộ `ORDER_V2_DOCUMENTATION.md` ghi sai). OCO dùng `EXPIRED` thay vì `SENDING`.

---

## Multi-Broker Domain

`order-v2` không phải single-backend. Constants định nghĩa nhiều domain broker:

| Domain | Constant | Implementation |
|--------|----------|----------------|
| MAS | `MAS_DOMAIN = "mas"` | `MasBullBearOrderService`, `MasOcoOrderService`, `MasTrailingOrderService` |
| KBSV | `KBSV_DOMAIN = "kbsv"` | (các service impl tương ứng) |
| Lotte | — | model `LotteOrderPlaceRequest/Response`; stop order có field `bankCode/bankAccount/bankName` "only for lotte" |

Ngoài ra còn các model `MasBosOrderPlace/CancelRequest`, `TtlBosOrderPlaceRequest` phục vụ tích hợp đặt lệnh thật theo từng broker khi conditional order được trigger.

> Đây là điểm doc nội bộ chưa phản ánh — order-v2 đã tiến hoá thành kiến trúc đa broker.

---

## Data Model Summary

| Entity (table) | Vai trò | Field chính |
|----------------|---------|-------------|
| `t_stop_order` | Stop / Stop-Limit | code, quantity, sellBuyType, stopPrice, orderPrice, orderType, status, fromDate, toDate |
| `t_trailing_order` | Trailing stop | trailingAmount, limitOffset, currentPrice, stopPrice, status |
| `t_bull_bear_order` | Lệnh chính (OCO + Bull/Bear) | orderPrice, profitPrice, triggerLossPrice, toler, matchQuantity, status |
| `t_oco_order` | Lệnh đối ứng OCO | bullBearId (FK), profitPrice, triggerLossPrice, matchQuantity, unmatchQuantity |
| `t_profit_loss_order` | Lệnh chốt lời / cắt lỗ | bullBearId, ocoId, profitLossType (OPEN_POSITION / TAKE_PROFIT / CUT_LOSS), orderPrice |

Quan hệ: `BullBearOrder ↔ OcoOrder` (1-1), `BullBearOrder ↔ ProfitLossOrder` (1-1 open position), `OcoOrder ↔ ProfitLossOrder` (1-n).

### Order Lifecycle

```
PENDING → SENDING → COMPLETED   (khi giá trigger và lệnh thật khớp)
PENDING → SENDING → FAILED      (đặt lệnh thật thất bại)
PENDING → CANCELLED             (user huỷ hoặc hết hạn fromDate/toDate)
```

---

## Auto-Populated Fields

| TradeX Field | Nguồn | Ghi chú |
|--------------|-------|---------|
| `username` | JWT token (`userData.username`) | Không nhận từ body |
| `sourceIp` | Request IP | |
| `status` | Hệ thống | Khởi tạo `PENDING` |
| `matchQuantity` / `unmatchQuantity` | Hệ thống | OCO khởi tạo match=0, unmatch=quantity |

---

## Convention Check (vs tradex-api-conventions.md)

Đối chiếu với `API Standards/tradex-api-conventions.md`:

| Mục | Kết quả | Ghi chú |
|-----|---------|---------|
| URL pattern | ✅ PASS | `/api/v1/{resource}` (camelCase resource, khớp code) |
| DTO Request naming | ✅ PASS | `{Resource}{Action}Request` (StopOrderPlaceRequest...) |
| DTO Response naming | ⚠️ WARN | Phần lớn đúng `{Resource}{Action}Response`; riêng `TrailingOrderAddResponse` dùng "Add" thay vì "Place" |
| Mutation response (tradex-native) | ⚠️ WARN | Stop/OCO/BullBear trả `{ id }` ✅; **Trailing trả `{ trailingOrderId }`** — lệch chuẩn `{ id }` |
| Query response (tradex-native) | ⚠️ WARN | Code trả **`List<Response>` (mảng thuần)**, **chưa** theo envelope chuẩn `{ totalCount, orders }` |
| Error code style | ✅ PASS | tradex-native SCREAMING_SNAKE_CASE (`STOP_PRICE_MUST_BE_SET`, `INVALID_PARAMETER`) |
| HTTP status semantics | ✅ PASS | Không dùng `success`/`code: "0000"` trong body |

> Hai điểm WARN về response (Trailing `{trailingOrderId}` và query trả mảng thuần) là **deviation có thật trong code order-v2**, không phải lỗi tài liệu. Ghi nhận tại đây để team cân nhắc chuẩn hoá khi build BE Derivatives.

---

## Source Files Reference

| Component | File Path (trong `Knowledge/TradeX-MCP/order-v2-main`) |
|-----------|--------------------------------------------------------|
| Routing (Kafka URI) | `src/main/java/com/techx/tradex/order/consumers/RequestHandler.java` |
| Quote / trigger | `consumers/QuoteHandler.java`, `services/OrderTriggerService.java` |
| Order match | `consumers/OrderMatchHandler.java` |
| Services | `services/StopOrderService.java`, `OcoOrderService.java`, `TrailingOrderService.java`, `BullBearOrderService.java` |
| Broker impl | `services/impl/MasBullBearOrderService.java`, `MasOcoOrderService.java`, `MasTrailingOrderService.java`, `StopOrderServiceImpl.java` |
| Request models | `model/request/*Request.java` |
| Response models | `model/response/*Response.java` |
| Constants | `constants/Constants.java` |
| Enums | `tradex-common-java-main` (SellBuyType, StopOrderType, *StatusEnum) |
| Internal doc | `ORDER_V2_DOCUMENTATION.md` (v1.0, Dec 2024 — một số chỗ đã lỗi thời, xem cảnh báo ở trên) |

---

## Related Documents

- [Regular Order API Mapping](./regular-order-api-mapping.md) — lệnh thường (lotte-bridge / tuxedo)
- [TradeX API Conventions](../API%20Standards/tradex-api-conventions.md)
- [TradeX Knowledge Index](../_index.md)

---

**Document Status:** ✅ Complete | For: PM / BA / BE Developer | Next Steps: Đối chiếu lại khi có bản source order-v2 mới (hiện chưa có trong Downloads/Repo)
**Last Updated:** June 30, 2026
**Source:** Code analysis của `order-v2-main` (Knowledge-first, verified against source)
