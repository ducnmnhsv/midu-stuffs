# [Epic DR-FE-ORD] Story ORD.S4: Tích hợp Lệnh Stop Order Derivatives

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-ORD – Derivatives Order FE  
> **Module:** Order  
> **Screens:** Đặt lệnh Stop, Sửa/Hủy lệnh Stop  
> **Priority:** P1  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-11

---

## User Story

**As a** trader  
**I want to** đặt lệnh Stop (lệnh điều kiện) – nhập giá đặt lệnh và giá kích hoạt – FE tự tính biên trượt và gửi đúng format  
**So that** tôi hiểu rõ: khi giá chạm mức kích hoạt thì lệnh sẽ đặt ở giá đặt lệnh.

---

## Logic UX – FE cần implement (dễ hiểu)

### 1. User nhập gì?

| Màn hình     | User nhập |
|-------------|------------|
| Đặt lệnh Stop | **Giá đặt lệnh** (orderPrice) – giá lệnh khi kích hoạt<br>**Giá kích hoạt** (triggerPrice) – giá thị trường chạm tới thì đặt lệnh |

**Ví dụ (giá hiện tại = 1000):**
- **Buy Stop:** User nhập giá đặt lệnh = 1005, giá kích hoạt = 1006 → Lệnh mua kích hoạt khi giá ≥ 1006, đặt LO ở 1005.
- **Sell Stop:** User nhập giá đặt lệnh = 999, giá kích hoạt = 998 → Lệnh bán kích hoạt khi giá ≤ 998, đặt LO ở 999.

### 2. FE tính gì?

**Công thức cố định:**
```
priceBand = triggerPrice − orderPrice
```

| Loại  | orderPrice | triggerPrice | priceBand |
|-------|------------|--------------|-----------|
| **Buy**  | 1005       | 1006         | 1         |
| **Sell** | 999        | 998          | −1        |

FE gửi API: `orderPrice`, `orderQuantity`, `priceBand`. **Không** gửi triggerPrice (BE derive từ orderPrice + priceBand).

### 3. FE validate gì? (trước khi gọi API)

| Loại lệnh   | Điều kiện hợp lệ                  | Lý do |
|-------------|-----------------------------------|-------|
| **BUY Stop**  | `triggerPrice` **>** `currentPrice` | Lệnh mua khi giá **đi lên** chạm trigger |
| **SELL Stop** | `triggerPrice` **<** `currentPrice` | Lệnh bán khi giá **đi xuống** chạm trigger |

**Lấy `currentPrice`:** WebSocket `market.quote.{symbol}` hoặc API giá hiện tại.

---

## Acceptance Criteria

- [ ] **AC-01** Màn đặt lệnh Stop: User nhập **giá đặt lệnh** và **giá kích hoạt**. FE tính `priceBand` = `triggerPrice − orderPrice` và gửi `orderPrice`, `priceBand`, `orderQuantity`.
- [ ] **AC-02** FE validate: BUY Stop → `triggerPrice` > `currentPrice`; SELL Stop → `triggerPrice` < `currentPrice`. Nếu không đúng, hiển thị lỗi, không gọi API.
- [ ] **AC-03** Place: POST `/api/v1/derivatives/stopOrder` với body `accountNumber`, `symbolCode`, `sellBuyType`, `orderPrice`, `orderQuantity`, `priceBand`. Success: hiển thị `message` + `orderNumber`.
- [ ] **AC-04** Modify: PUT `/api/v1/derivatives/stopOrder/modify` với `orderNumber` (composite `{date}-{seq_no}`), `orderPrice`, `orderQuantity`, `priceBand`.
- [ ] **AC-05** Cancel: PUT `/api/v1/derivatives/stopOrder/cancel` với `accountNumber`, `orderNumber`.
- [ ] **AC-06** Error: hiển thị `code`, `message` theo format TradeX (giống Regular Order).

---

## Tasks (Implementation)

- [ ] **T1** Form đặt lệnh Stop: 2 input (giá đặt lệnh, giá kích hoạt); tính `priceBand` = `triggerPrice − orderPrice`.
- [ ] **T2** Lấy `currentPrice` (WebSocket hoặc API) và validate: BUY → triggerPrice > currentPrice; SELL → triggerPrice < currentPrice.
- [ ] **T3** Place: gọi POST stopOrder với `orderPrice`, `priceBand`, `orderQuantity`; xử lý response (`message`, `orderNumber`).
- [ ] **T4** Modify / Cancel: gọi PUT stopOrder/modify, stopOrder/cancel; dùng `orderNumber` từ Place response.
- [ ] **T5** Error handling: hiển thị `message` pass-through từ Lotte; validation `params`.

---

## Background / Context

Stop Order (lệnh điều kiện) map Lotte DRORD-005/006/023-026. API Spec tập trung vào BE mapping. Logic UX (user nhập gì, FE tính gì, validate gì) nằm trong issue này để FE dễ implement.

---

## Data Source

- **API Spec:** [Stop_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md)
- **Planning:** [Order/README](../../../Planning%20documentation/Order/README.md)

**Endpoints:**

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place Stop Order | POST | `/api/v1/derivatives/stopOrder` |
| Modify Stop Order | PUT | `/api/v1/derivatives/stopOrder/modify` |
| Cancel Stop Order | PUT | `/api/v1/derivatives/stopOrder/cancel` |

**Request (Place):** `accountNumber`, `symbolCode`, `sellBuyType`, `orderPrice`, `orderQuantity`, `priceBand`  
*(priceBand = triggerPrice − orderPrice, FE tính trước khi gửi)*  
**Success:** `{ "message": "...", "orderNumber": "20260211-001234" }` (format giống Regular Order)

---

## References

- **Figma:** [NHSV-Pro - Stop Order UI](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005497-339393&t=7ET4YMgEP2r0vrEW-11)
- **API Spec:** [Stop_Orders_API_Spec](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md)
