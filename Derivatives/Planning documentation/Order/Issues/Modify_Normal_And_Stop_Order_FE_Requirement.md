# Modify Normal Order & Stop Order – FE Implementation

**Issue Type:** Feature Request (Frontend)  
**Priority:** High  
**Component:** NHSV Pro – Derivatives Orderbook → Modify Order screens  
**Related:** [Stop_Order_Screen_FE_Requirement](Stop_Order_Screen_FE_Requirement.md), [Order_Availability_Check_Integration](Order_Availability_Check_Integration.md)  
**Created:** March 10, 2026  
**Status:** 📋 Ready for FE Dev

---

## Executive Summary

Xây dựng **hai màn hình sửa lệnh Derivatives** cho NHSV Pro: **Modify normal order** và **Modify stop order**. Từ màn **Orderbook** (lịch sử/sổ lệnh Derivatives), khi user bấm nút **Edit** sẽ điều hướng tới màn tương ứng (sửa lệnh thường hoặc sửa lệnh điều kiện). Màn hình hiển thị theo Figma, tích hợp API **order/orderBook** (sổ lệnh trong ngày), **order/checkAvailability** và **order/modify** của Derivatives; validation giá (trần–sàn cho lệnh thường, CE–RE cho lệnh điều kiện), số lượng tối đa, và với stop order thêm trigger price và ngày hiệu lực (không chọn quá khứ, chỉ một ngày). Sau khi gọi modify thành công, hiển thị message và orderNumber trả về từ API.

**Phạm vi:** Chỉ **sửa lệnh (Modify)** từ Orderbook Derivatives; entry point là nút Edit trên màn Orderbook.

---

## Background

- User cần **sửa lệnh** đã đặt (normal order hoặc stop order) từ màn Orderbook.
- Click **Edit** trên một dòng lệnh → mở màn **Modify normal order** hoặc **Modify stop order** tùy loại lệnh.
- Backend cung cấp: `/api/v1/derivatives/order/orderBook` (sổ lệnh trong ngày — chi tiết lệnh), `/api/v1/derivatives/order/checkAvailability` (max quantity), `/api/v1/derivatives/order/modify` (gửi sửa lệnh).
- FE hiện có tham chiếu: **Equity** màn `ModifyOrderBook` (`src/screens/ModifyOrderBook`), **Equity** màn `ModifyStopOrder` (`src/screens/ModifyStopOrder`) – có thể tham khảo cấu trúc, nhưng Derivatives dùng API và endpoint riêng.

---

## Design Reference (Figma)

| Màn hình | Mô tả | Figma Link | Node ID |
|----------|--------|------------|--------|
| **Modify normal order** | Form sửa lệnh thường – Bid/Ask & Chart | [NHSV-Pro – Modify order](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104&t=Hkbonf9r1expHBzf-11) | `40005008-236104` |
| **Modify order – Tab Chart** | Tab Chart thay cho Bid/Ask | [NHSV-Pro – Modify order Chart](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683&t=Hkbonf9r1expHBzf-11) | `40006583-230683` |

**File Key:** `7KYJfVHawWie4n8v12JtXm` (NHSV Pro)

---

## FE Repo Reference (nhsv-mts-rn)

| Mục đích | Đường dẫn / Ghi chú |
|----------|---------------------|
| Màn Orderbook (entry point) | Cần xác định màn Orderbook **Derivatives** – có thể mới hoặc mở rộng từ `src/screens/OrderHistory` (equity). Nút **Edit** trên từng dòng lệnh → navigate tới Modify screen với params (order detail hoặc orderId). |
| Tham khảo Modify (Equity) | `src/screens/ModifyOrderBook/` – cấu trúc màn, Price/Quantity/Max, Cancel/Modify button. |
| Tham khảo Modify Stop (Equity) | `src/screens/ModifyStopOrder/` – Trigger price, Order price, Quantity, From/To date, Confirm. |
| Navigation / Screen params | `src/navigation/ScreenParamList.ts` – có `ModifyOrderBook`, `ModifyStopOrder` (equity). Derivatives có thể thêm screen mới hoặc reuse với context Derivatives. |
| Redux / API | `src/config/api.ts` – hiện equity: `/lotte/equity/order/...`. Derivatives: dùng endpoint `/api/v1/derivatives/order/...` (orderBook, checkAvailability, modify). |

---

# Part 1 – Modify Normal Order

## 1.1 Entry & Navigation

- Từ màn **Orderbook** (sổ lệnh / lịch sử lệnh Derivatives), khi user click nút **Edit** trên một lệnh **normal order** → điều hướng tới màn **Modify normal order**.
- Truyền thông tin lệnh cần sửa (ví dụ từ API history hoặc từ row đã chọn) làm params màn hình.

## 1.2 Layout & Content (theo Figma)

- Giao diện theo: [Modify order – Normal](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104&t=Hkbonf9r1expHBzf-11).

**Thông tin mã (symbol):**

- Giá hiện tại, change, change rate.
- Giá CE / FL / RE của mã.
- Vol, basis.

**Tabs:**

- Hai tab: **Bid/Ask** và **Chart**. Mặc định chọn **Bid/Ask**.
- **Bid/Ask:** Hiển thị bid/ask của **top 3** giao dịch.
- **Chart:** Khi chọn tab Chart, hiển thị phần chart thay cho Bid/Ask – theo [Modify order Chart](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683&t=Hkbonf9r1expHBzf-11).

**Thông tin lệnh gốc (từ API order/orderBook — sổ lệnh trong ngày):**

- Loại lệnh hiện tại: Buy / Sell.
- Giá đặt lệnh gốc.
- Số lượng (orderQuantity).
- Loại lệnh: Normal order.

*(Chi tiết response API `/api/v1/derivatives/order/orderBook` xem [Regular_Orders_API_Spec §7](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md#7-api-query-orderbook-trong-ngày-t0).)*

**Khu vực nhập thông tin sửa lệnh:**

| Field | Rule | Error / Ghi chú |
|-------|------|------------------|
| **Price** | Bắt buộc nằm trong khoảng **trần – sàn** của mã. | Null/trống → báo "không hợp lệ". Ngoài khoảng trần–sàn → toast "Giá nằm ngoài khoảng trần - sàn" (giống error toast hiện tại). |
| **Quantity** | ≥ 0; tối đa = số lượng từ API **checkAvailability**. | Chỉ được nhập tối đa theo kết quả checkAvailability (Buy/Sell theo lệnh gốc). |
| **Max quantity** | Chỉ hiển thị (read-only). | Hiển thị sau khi gọi `/api/v1/derivatives/order/checkAvailability`; loại (Buy/Sell) theo lệnh gốc. |

## 1.3 Buttons & Actions

- **Cancel:** Thoát màn Modify order (không gửi API).
- **Modify:** Chỉ **enable** khi có thay đổi so với lệnh gốc (user đã nhập thông tin sửa). Khi bấm:
  - Gọi API `POST /api/v1/derivatives/order/modify`.
  - API trả về `message` và `orderNumber`. Hiển thị **message** và **orderNumber** trên màn hình (theo design/UX quy định – ví dụ toast hoặc inline).

## 1.4 APIs (Normal Order)

| Mục đích | Method | Endpoint |
|----------|--------|----------|
| Chi tiết lệnh gốc (sổ lệnh trong ngày) | GET | `/api/v1/derivatives/order/orderBook` |
| Số lượng tối đa (max quantity) | GET | `/api/v1/derivatives/order/checkAvailability` |
| Gửi sửa lệnh | POST | `/api/v1/derivatives/order/modify` |

*(Request/response chi tiết do Backend cung cấp sau.)*

---

# Part 2 – Modify Stop Order

## 2.1 Entry & Navigation

- Từ màn **Orderbook**, khi user click **Edit** trên một lệnh **stop order** → điều hướng tới màn **Modify stop order**.
- Phần layout chung (symbol info, tab Bid/Ask & Chart) giống Modify normal order; **chỉ khác** nội dung form sửa lệnh (các field dưới).

## 2.2 Form sửa lệnh (khác với Normal)

**Các field:**

| Field | Rule | Error / Ghi chú |
|-------|------|------------------|
| **Trigger price** | Theo spec Derivatives. | Validation theo quy định (ví dụ so với giá hiện tại nếu có). |
| **Order price** | Giá phải nằm trong khoảng **CE – RE** của mã. | Nếu nằm ngoài range → báo lỗi giống các chỗ khác (toast "Giá nằm ngoài khoảng …"). |
| **Quantity** | ≥ 0; tối đa từ API **checkAvailability**. | Giống Normal – không vượt quá số lượng từ checkAvailability; loại Buy/Sell theo lệnh gốc. |
| **Affected date** | Không chọn ngày trong **quá khứ**; chỉ chọn ngày **hiện tại hoặc tương lai**. **Một ngày**: ngày bắt đầu = ngày kết thúc. | Khi chọn → mở **date picker modal** (theo design). |
| **Max quantity** | Chỉ hiển thị. | Sau khi gọi `/api/v1/derivatives/order/checkAvailability`; Buy/Sell theo lệnh gốc. |

## 2.3 Buttons & Actions

- **Cancel:** Thoát màn.
- **Modify:** Enable khi có thay đổi. Khi bấm:
  - Gọi API `POST /api/v1/derivatives/order/modify`.
  - Hiển thị **message** và **orderNumber** trả về từ API.

## 2.4 Tab Chart

- Khi chọn tab **Chart**, hiển thị phần chart thay cho Bid/Ask – cùng design [Modify order Chart](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683&t=Hkbonf9r1expHBzf-11).

## 2.5 APIs (Stop Order)

- Cùng nhóm endpoint: **order/orderBook**, **order/checkAvailability**, **order/modify** (request body khác cho stop order theo spec Backend).

---

## Acceptance Criteria (Summary)

### Modify Normal Order

- [ ] Click **Edit** trên Orderbook (normal order) → mở màn Modify normal order với đúng params.
- [ ] Hiển thị đúng layout Figma (symbol info, CE/FL/RE, vol, basis; tab Bid/Ask mặc định).
- [ ] Tab Bid/Ask: top 3 bid/ask; Tab Chart: hiển thị chart theo Figma.
- [ ] Thông tin lệnh gốc lấy từ API order/orderBook (Buy/Sell, giá đặt, orderQuantity, Normal order).
- [ ] Price: validate trần–sàn; null → "không hợp lệ"; ngoài range → toast "Giá nằm ngoài khoảng trần - sàn".
- [ ] Quantity: ≥ 0; max từ checkAvailability (Buy/Sell theo lệnh gốc); hiển thị Max quantity sau khi gọi checkAvailability.
- [ ] Cancel → thoát màn. Modify enable khi có thay đổi; click Modify → gọi order/modify; hiển thị message và orderNumber.

### Modify Stop Order

- [ ] Click **Edit** trên Orderbook (stop order) → mở màn Modify stop order.
- [ ] Form có Trigger price, Order price (validate CE–RE), Quantity (max từ checkAvailability), Affected date (date picker, không quá khứ, một ngày), Max quantity.
- [ ] Cancel → thoát. Modify → gọi order/modify; hiển thị message và orderNumber.
- [ ] Tab Chart giống Normal (chart thay Bid/Ask).

### Chung

- [ ] APIs: derivatives/order/orderBook, derivatives/order/checkAvailability, derivatives/order/modify (request/response theo spec Backend khi có).

---

## Reference Documents

- [Stop Orders API Spec](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md) – Modify/Cancel stop order.
- [Order Availability Check API Spec](../../../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md) – checkAvailability.
- [Regular Orders API Spec](../../../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md) – context regular order/modify.

---

**Document Status:** 📋 Ready for FE Dev  
**For:** FE Developers, PM, QA  
**Next Steps:** Backend cung cấp chi tiết API order/orderBook (request/response) cho từng loại lệnh; FE triển khai màn và tích hợp API.
