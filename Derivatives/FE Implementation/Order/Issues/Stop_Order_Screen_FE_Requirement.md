# Stop Order Screen – FE Implementation

**Issue Type:** Feature Request (Frontend)  
**Priority:** High  
**Component:** NHSV Pro – Trade Tab (Derivatives)  
**Related:** [Trade_Screen_FE_Requirement](Trade_Screen_FE_Requirement.md), [Max_Buy_Max_Sell_Integration](Max_Buy_Max_Sell_Integration.md)  
**Created:** March 6, 2026  
**Status:** 📋 Ready for FE Dev

---

## Executive Summary

Xây dựng màn hình **đặt lệnh Stop order** (Lệnh điều kiện) cho Derivatives trên NHSV Pro, cho phép user đặt lệnh kích hoạt khi giá thị trường đạt mức trigger. Màn hình hỗ trợ **hai chế độ giao diện** (Normal mode và Quick mode), bám sát Figma; tích hợp API Place Stop Order và tuân thủ validation trigger price so với giá hiện tại, đồng thời tái sử dụng cơ chế Real-time price và Max buy/Max sell như màn Normal order.

**Phạm vi:** Chỉ **đặt lệnh (Place)**; Sửa/Hủy lệnh Stop order có thể tham chiếu màn ModifyStopOrder hiện có (`src/screens/ModifyStopOrder`) nếu cần.

---

## Background

- Cần tính năng cho phép user **đặt lệnh Stop order** (Lệnh điều kiện) trong app NHSV Pro (Derivatives).
- Stop order được kích hoạt khi giá thị trường đạt mức **trigger price**; API Backend: `POST /api/v1/derivatives/stopOrder`.
- FE cần map đúng field lên API (orderPrice, orderQuantity, priceBand, orderDate), xử lý **biên trượt (price band)** từ trigger price phía FE, và validate trigger price so với giá hiện tại (BUY: trigger > current; SELL: trigger < current).

---

## Design Reference (Figma)

| Mode        | Mô tả           | Figma Link | Node ID        |
|------------|------------------|------------|----------------|
| **Normal** | Lệnh điều kiện – form đầy đủ | [NHSV-Pro – Lệnh điều kiện (Normal)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005497-339393&t=Hkbonf9r1expHBzf-11) | `40005497-339393` |
| **Quick**  | Lệnh điều kiện – giao diện gọn | [NHSV-Pro – Lệnh điều kiện (Quick)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005497-339637&t=Hkbonf9r1expHBzf-11) | `40005497-339637` |
| **Date picker** | Chọn ngày hiệu lực | [NHSV-Pro – Date picker](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005390-286022&t=Hkbonf9r1expHBzf-11) | `40005390-286022` |

**File Key:** `7KYJfVHawWie4n8v12JtXm` (NHSV Pro)

---

## Field Mapping (UI → API)

| UI Field (Figma)   | API Field     | Ghi chú |
|--------------------|---------------|---------|
| **Giá đặt (order price)** | `orderPrice`  | Giá thực hiện lệnh khi đã kích hoạt. User nhập; FE gửi trực tiếp. (Nếu form chỉ có một field “Trigger price” thì có thể gửi giá đó làm `orderPrice`.) |
| **Giá kích hoạt (trigger price)** | — | Dùng để validation (BUY: trigger > current; SELL: trigger < current) và để **tính priceBand**. Không gửi riêng field trigger; dùng trong công thức `priceBand = triggerPrice − orderPrice`. |
| **Bước giá (price band)** | `priceBand` | **FE tính:** `priceBand = giá kích hoạt − giá đặt`. Bắt buộc gửi trong request. Chi tiết: [§ priceBand – Cách tính và truyền vào API](#priceband--cách-tính-và-truyền-vào-api). |
| **Quantity**       | `orderQuantity` | Số lượng – map 1:1. Validate với Max buy/Max sell (checkAvailability) như màn Normal order. |
| **Affected date**   | `orderDate`   | Ngày hiệu lực. Format: `yyyyMMdd`. Mặc định: **today → today** (một ngày). User chọn **1 ngày** trong date picker → FE gửi `orderDate` = ngày đó. |

Các field khác theo API spec: `accountNumber`, `symbolCode`, `sellBuyType` (BUY/SELL), `deviceUniqueId` (optional). Backend tự populate từ JWT/request (userId, sourceIp, mdm_tp, v.v.).

---

## priceBand – Cách tính và truyền vào API

**Công thức (bắt buộc FE tính trước khi gọi API):**

```
priceBand = giá kích hoạt (trigger price) − giá đặt (order price)
```

- **Giá kích hoạt (trigger price):** Giá thị trường cần đạt để lệnh được kích hoạt (user nhập trên UI – có thể map vào `orderPrice` gửi API tùy spec Lotte; xem bảng Field Mapping).
- **Giá đặt (order price):** Giá thực hiện lệnh sau khi đã kích hoạt (user nhập trên UI hoặc mặc định = giá kích hoạt nếu form chỉ có một trường giá).

**Ví dụ cho dev:**

| Giá kích hoạt (trigger) | Giá đặt (order) | priceBand gửi API |
|--------------------------|-----------------|-------------------|
| 1 250                    | 1 245           | `5` (1250 − 1245) |
| 1 250                    | 1 250           | `0`               |
| 1 240                    | 1 235           | `5` (1240 − 1235) |

**Implementation gợi ý:**

1. Lấy từ form: `triggerPrice` (number), `orderPrice` (number) – nếu UI chỉ có một field “Trigger price” thì có thể mặc định `orderPrice = triggerPrice` (khi đó `priceBand = 0`).
2. Tính: `priceBand = triggerPrice - orderPrice`.
3. Gửi trong body Place Stop Order: `priceBand` (number, hoặc string theo API spec). Giá trị phải hợp lệ (≥ 0 nếu nghiệp vụ yêu cầu; nếu có âm thì theo đúng quy định sàn).

**Lưu ý:** API Lotte yêu cầu có `priceBand` (ord_band_pri). FE luôn gửi giá trị đã tính theo công thức trên, không để trống.

---

## Normal Mode – UI & Behavior

1. **Layout:** Hiển thị giao diện giống Figma [Normal – Lệnh điều kiện](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005497-339393&t=Hkbonf9r1expHBzf-11).
2. **Fields:**
   - **Giá kích hoạt / Giá đặt:** Gửi giá đặt làm `orderPrice`. Tính `priceBand = giá kích hoạt − giá đặt` và gửi trong request (xem [§ priceBand](#priceband--cách-tính-và-truyền-vào-api)).
   - **Quantity** → map `orderQuantity`.
   - **Affected date** → map `orderDate`. Default: **today → today** (một ngày duy nhất).
3. **Date picker:**
   - Khi user click vào Affected date, mở date picker theo design: [Date picker](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005390-286022&t=Hkbonf9r1expHBzf-11).
   - **Cancel:** đóng picker, **không** thay đổi ngày đã chọn.
   - **Confirm:** chọn ngày; FE gửi `orderDate` = ngày user chọn (một ngày, format `yyyyMMdd`).
4. **Real-time price:** Áp dụng mặc định giống màn Normal order (cập nhật giá hiện tại từ WebSocket `market.quote.{symbol}` field `c` khi switch bật).

---

## Quick Mode – UI & Behavior

- Khi chọn **Quick mode**, hiển thị giao diện giống Figma [Quick – Lệnh điều kiện](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005497-339637&t=Hkbonf9r1expHBzf-11).
- Logic nghiệp vụ (mapping field, validation, API) giống Normal mode; chỉ khác cách hiển thị và bố cục.

---

## Validation (FE)

| Rule | Mô tả | Khi vi phạm |
|------|--------|-------------|
| **Trigger price vs current price** | **BUY:** trigger price **phải >** giá hiện tại (current price). **SELL:** trigger price **phải <** giá hiện tại. | Hiển thị **error toast** (message rõ ràng, ví dụ: "Giá kích hoạt mua phải lớn hơn giá hiện tại" / "Giá kích hoạt bán phải nhỏ hơn giá hiện tại"). |
| **Quantity** | Không vượt quá sức mua/sức bán (Max buy / Max sell từ API checkAvailability). | Error toast như màn Normal order (xem [Max_Buy_Max_Sell_Integration](Max_Buy_Max_Sell_Integration.md)). |

Nếu không thỏa mãn validation, **không** gửi request Place Stop Order; chỉ hiện error toast.

---

## API Reference

| Operation | Method | Endpoint | Spec |
|-----------|--------|----------|------|
| **Place Stop Order** | POST | `/api/v1/derivatives/stopOrder` | [Stop_Orders_API_Spec](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md) |

**Request body (Place):**  
`accountNumber`, `symbolCode`, `sellBuyType` (BUY | SELL), `orderPrice`, `orderQuantity`, `priceBand`, `orderDate` (optional, default today), `deviceUniqueId` (optional).

**Success (200):** `message`, `orderNumber` (composite `{date}-{seq_no}`).  
**Error (4xx/422):** `code` (e.g. `STOP_ORDER_PLACE_1005`), `message` – hiển thị toast từ BE.

---

## Acceptance Criteria

| # | Criteria |
|---|----------|
| AC1 | **Normal mode:** Hiển thị giao diện đặt lệnh Stop order theo Figma (node `40005497-339393`). Các field: Trigger price (→ orderPrice), Quantity (→ orderQuantity), Affected date (→ orderDate). |
| AC2 | FE tính **priceBand** theo công thức `priceBand = giá kích hoạt − giá đặt` và gửi trong request Place Stop Order (xem [§ priceBand](#priceband--cách-tính-và-truyền-vào-api)). |
| AC3 | Affected date: mặc định **today → today**. Click vào field mở date picker theo Figma (node `40005390-286022`). **Cancel** = không chọn ngày; **Confirm** = chọn một ngày; FE gửi `orderDate` = ngày đó (yyyyMMdd). |
| AC4 | **Quick mode:** Hiển thị giao diện theo Figma (node `40005497-339637`). Cùng logic mapping và API với Normal mode. |
| AC5 | **Real-time price:** Mặc định bật, cơ chế giống màn Normal order (WebSocket `market.quote.{symbol}`, field `c`). |
| AC6 | **Validation:** BUY → trigger price > current price; SELL → trigger price < current price. Nếu không thỏa mãn → hiển thị **error toast**, không gửi API. |
| AC7 | Quantity validate với Max buy/Max sell (checkAvailability) như màn Normal order; vượt quá → error toast. |
| AC8 | Gọi `POST /api/v1/derivatives/stopOrder` với body đúng spec; xử lý success (message, orderNumber) và error (toast message từ BE). |

---

## Reference

| Document | Description |
|----------|-------------|
| [Stop_Orders_API_Spec](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md) | API Place/Modify/Cancel Stop Order |
| [Trade_Screen_FE_Requirement](Trade_Screen_FE_Requirement.md) | Màn Trade Normal/Quick order, Real-time price |
| [Max_Buy_Max_Sell_Integration](Max_Buy_Max_Sell_Integration.md) | checkAvailability, Max buy/Max sell, validate quantity |
| **FE repo (nhsv-mts-rn)** | `src/screens/TradeTab` (Trade form, mode picker); `src/screens/ModifyStopOrder` (sửa lệnh Stop – tham khảo trigger price, quantity, date nếu có) |

---

**Document Status:** 📋 Ready for FE  
**For:** FE Dev, PM  
**Next Steps:** Estimate, implement UI theo Figma; tích hợp API Place Stop Order; verify validation và date picker behavior.
