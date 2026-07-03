# Max buy / Max sell – Tích hợp checkAvailability trên màn Trade

**Issue Type:** Feature Request (Frontend)  
**Priority:** High  
**Component:** NHSV Pro – Trade Tab (Derivatives)  
**Related:** [Trade_Screen_FE_Requirement](Trade_Screen_FE_Requirement.md)  
**Created:** February 27, 2026  
**Status:** 📋 Ready for FE Dev

---

## Executive Summary

Tích hợp API **Order Availability Check** (`GET /api/v1/derivatives/order/checkAvailability`) trên màn **Trade** (Lệnh thường / Lệnh nhanh) để:

- Gọi API **mặc định** khi vào màn (và khi đổi tài khoản/mã) với `accountNumber` + `symbolCode`, **không truyền** `sellBuyType`, để nhận cùng lúc sức mua và sức bán.
- Hiển thị **Max buy** và **Max sell** ngay phía dưới hai nút **Buy (Long)** và **Sell (Short)** theo Figma, map từ response API.

**Phạm vi:** API và logic Max buy / Max sell **áp dụng cho tất cả các loại lệnh** — **Normal order**, **Advance order**, **Stop order**. Validation Quantity (so với response API) và error toast áp dụng chung cho mọi loại lệnh.

Có thể triển khai như **FE task riêng** (Jira riêng) hoặc gộp vào implementation màn Trade; trên Jira hiện tách thành **2 issue** (màn Trade tổng thể + issue này).

---

## Objective

- Khi user vào màn Trade Derivatives và đã có `accountNumber` + `symbolCode`, FE gọi `checkAvailability` **không truyền** `sellBuyType`.
- Nhận response dạng `{ buy: { availableQuantity, availableLiquidity }, sell: { ... } }`.
- Hiển thị **Max buy** = `buy.availableQuantity` ngay dưới nút Buy; **Max sell** = `sell.availableQuantity` ngay dưới nút Sell.
- Dùng giá trị này để validate Quantity cho **mọi loại lệnh** (Normal / Advance / Stop); khi vượt sức mua/sức bán → hiển thị **error toast**.

---

## UI / Figma

| Vị trí | Mô tả |
|--------|--------|
| **Max buy** | Ngay **bên dưới nút Buy (Long)**; giá trị từ `response.buy.availableQuantity`. |
| **Max sell** | Ngay **bên dưới nút Sell (Short)**; giá trị từ `response.sell.availableQuantity`. |

**Figma – Lệnh thường (vị trí Max buy / Max sell):**  
[NHSV-Pro – Lệnh thường](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004971-124721&t=Hkbonf9r1expHBzf-11) — Node ID: `40004971-124721`.

Áp dụng cho cả hai mode: **Lệnh thường** và **Lệnh nhanh**, và cho **tất cả loại lệnh**: Normal order, Advance order, Stop order.

---

## Phạm vi loại lệnh (Scope)

| Loại lệnh | Áp dụng checkAvailability & Max buy/Max sell |
|-----------|-----------------------------------------------|
| **Normal order** | ✅ Có |
| **Advance order** | ✅ Có |
| **Stop order** | ✅ Có |

Cùng một API và cùng giá trị Max buy / Max sell dùng để hiển thị và validate Quantity cho mọi loại lệnh trên màn Trade.

---

## Validation Quantity & Error toast

- Khi user nhập **Quantity** (trong bất kỳ form đặt lệnh nào: Normal / Advance / Stop), FE so sánh với giá trị từ API:
  - **Chiều Mua:** Nếu `quantity > response.buy.availableQuantity` (Max buy) → hiển thị **error toast**: *"Vượt quá sức mua khả dụng"*.
  - **Chiều Bán:** Nếu `quantity > response.sell.availableQuantity` (Max sell) → hiển thị **error toast**: *"Vượt quá sức bán khả dụng"*.
- Toast là **error** (error toast message), không chỉ hint; user cần sửa quantity trước khi submit.

---

## API & Mapping

| Item | Nội dung |
|------|----------|
| **Endpoint** | `GET /api/v1/derivatives/order/checkAvailability` |
| **Query params (mặc định)** | `accountNumber`, `symbolCode` — **không** `sellBuyType` |
| **Response** | `{ "buy": { "availableQuantity": n, "availableLiquidity": ... }, "sell": { "availableQuantity": m, "availableLiquidity": ... } }` |
| **Max buy** | `response.buy.availableQuantity` |
| **Max sell** | `response.sell.availableQuantity` |

**Spec:** [Order_Availability_Check_API_Spec](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md).

---

## Acceptance Criteria

| # | Criteria |
|---|----------|
| AC1 | Khi vào màn Trade (đã có account + symbol), gọi `checkAvailability` với chỉ `accountNumber` và `symbolCode` (không `sellBuyType`). |
| AC2 | Hiển thị **Max buy** ngay dưới nút Buy (Long), giá trị = `response.buy.availableQuantity`. |
| AC3 | Hiển thị **Max sell** ngay dưới nút Sell (Short), giá trị = `response.sell.availableQuantity`. |
| AC4 | Khi đổi `accountNumber` hoặc `symbolCode`, gọi lại API và cập nhật Max buy / Max sell. |
| AC5 | **Quantity validation:** Nếu user nhập Quantity > Max buy (khi Mua) hoặc > Max sell (khi Bán) → hiển thị **error toast**: "Vượt quá sức mua khả dụng" / "Vượt quá sức bán khả dụng". Áp dụng cho Normal order, Advance order, Stop order. |
| AC6 | Nếu API lỗi (4xx/5xx): hiển thị message từ BE; không block màn; có thể thử lại hoặc ẩn Max buy/Max sell. |

---

## Reference

| Document | Description |
|----------|-------------|
| [Trade_Screen_FE_Requirement](Trade_Screen_FE_Requirement.md) | Màn Trade tổng thể, có section Max buy / Max sell |
| [Order_Availability_Check_API_Spec](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md) | Spec API checkAvailability |
| Figma – Lệnh thường | [Link](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004971-124721&t=Hkbonf9r1expHBzf-11) |

---

**Document Status:** 📋 Ready for FE  
**For:** FE Dev, PM  
**Next Steps:** Estimate, implement; verify UI với Figma (Max buy / Max sell dưới 2 nút Buy/Sell).
