# [Epic DR-FE-ORD] Story ORD.S4: Tích hợp Lệnh Stop Order Derivatives

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-ORD – Derivatives Order FE  
> **Module:** Order  
> **Screens:** Đặt lệnh Stop, Sửa/Hủy lệnh Stop  
> **Priority:** P1  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-11

---

## API Spec (tham chiếu)

**Full spec:** [Stop_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md)

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Place | POST | `/api/v1/derivatives/stopOrder` |
| Modify | PUT | `/api/v1/derivatives/stopOrder/modify` |
| Cancel | PUT | `/api/v1/derivatives/stopOrder/cancel` |

---

## Acceptance Criteria

### 1. Add the new feature – Đặt lệnh Stop Order

- **Vị trí:** Màn đặt lệnh Derivatives – thêm loại lệnh **Stop Order** (lệnh điều kiện).
- **Figma UI:** [Stop Order screen](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005497-339393&t=7ET4YMgEP2r0vrEW-11)

---

### 2. Form fields & default values

| Field | API Key | Type | Required | Default | Ghi chú |
|-------|---------|------|----------|---------|---------|
| Số tài khoản | `accountNumber` | String | ✅ | Account đang chọn (lấy từ session/sub đã chọn) | - |
| Mã CK | `symbolCode` | String | ✅ | Symbol đang chọn | VD: VN30F2502 |
| Loại (Mua/Bán) | `sellBuyType` | String | ✅ | `BUY` hoặc `SELL` | Theo tab/user chọn |
| Giá đặt lệnh | `orderPrice` | Number | ✅ | - | User nhập, giá lệnh khi kích hoạt |
| Trigger price | _(không gửi API)_ | Number | ✅ | - | User nhập, dùng để validate + tính `priceBand` |
| Affected date | `orderDate` | String | ❌ | **today** | Format `yyyyMMdd` |
| Khối lượng | `orderQuantity` | Number | ✅ | - | User nhập, số hợp đồng |
| `deviceUniqueId` | `deviceUniqueId` | String | ❌ | Device ID hiện tại | Theo flow Equity (nếu có) |

---

### 3. Validation – trước khi gọi Place API

1. **Required fields:** `accountNumber`, `symbolCode`, `sellBuyType`, `orderPrice`, `orderQuantity` – nếu thiếu → hiển thị lỗi, không gọi API.

2. **Trigger price vs current price** (bắt buộc khi user đã nhập Trigger price):
   - Lấy `currentPrice` từ WebSocket `market.quote.{symbol}` hoặc API giá hiện tại.
   - Nếu chưa có `currentPrice` → hiển thị "Đang tải giá..." hoặc disable nút Đặt lệnh, không cho submit.
   - **Buy Stop:** `triggerPrice` **phải >** `currentPrice`. Nếu không → hiển thị: *"Giá kích hoạt phải lớn hơn giá hiện tại"*.
   - **Sell Stop:** `triggerPrice` **phải <** `currentPrice`. Nếu không → hiển thị: *"Giá kích hoạt phải nhỏ hơn giá hiện tại"*.

3. **orderPrice, orderQuantity:**
   - `orderPrice` > 0, đúng tick size (VN30F = 0.1).
   - `orderQuantity` > 0, integer.

4. **orderDate (Affected date):**
   - Không cho chọn ngày **trong quá khứ**.
   - Chỉ cho chọn **today** và **các ngày trong tương lai**.
   - Nếu user không chọn → dùng today, format `yyyyMMdd` khi gửi API.

---

### 4. Tính `priceBand` – bắt buộc trước khi gọi Place

- **Công thức:** `priceBand = triggerPrice (user nhập) − orderPrice (user nhập)`
- **Buy order:** Kết quả là số **dương** (VD: trigger 1006, order 1005 → priceBand = 1).
- **Sell order:** Kết quả là số **âm** (VD: trigger 998, order 999 → priceBand = −1).

---

### 5. Gọi Place API – khi user bấm Đặt lệnh

1. **API:** `POST /api/v1/derivatives/stopOrder`

2. **Headers:**
   - `Authorization: Bearer {access_token}`
   - `Content-Type: application/json`
   - `Accept-Language: vi` (hoặc theo cài đặt app)

3. **Request body – gửi đủ các field:**
   ```json
   {
     "accountNumber": "0001234567",
     "symbolCode": "VN30F2502",
     "sellBuyType": "BUY",
     "orderPrice": 1005,
     "orderQuantity": 10,
     "priceBand": 1,
     "orderDate": "20260211"
   }
   ```
   - `orderDate`: Nếu user chọn today → gửi `yyyyMMdd`; nếu không gửi → BE dùng today.
   - `priceBand`: FE **tính trước** từ `triggerPrice − orderPrice`.

4. **Success (200):**
   - Response: `{ "message": "[V0307] ...", "orderNumber": "20260211-001234" }`
   - Hiển thị `message` (pass-through từ Lotte) và `orderNumber` cho user.
   - **Không** dựa vào field `success` – dùng HTTP status.

5. **Error (4xx/5xx):**
   - `{ "code": "STOP_ORDER_PLACE_1005", "message": "..." }` → hiển thị `message` (pass-through Lotte).
   - `{ "code": "INVALID_PARAMETER", "params": [...] }` → hiển thị lỗi theo từng field trong `params`.

---

### 6. Affected date – Date picker behavior

1. **Default:** Khi mở form, Affected date = **today** (ngày hiện tại).

2. **Click vào field Affected date:** Mở popup **Date picker**.
   - **Figma Date picker:** [node-id=40005390-286022](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005390-286022&t=7ET4YMgEP2r0vrEW-11)

3. **Ràng buộc Date picker:**
   - **Không** cho chọn ngày trong quá khứ (min date = today).
   - Chỉ cho chọn **today** và **các ngày trong tương lai**.
   - Nếu user chọn ngày → format `yyyyMMdd` khi gửi API (VD: `"20260211"`).

---

### 7. Modify Stop Order

1. **API:** `PUT /api/v1/derivatives/stopOrder/modify`

2. **Request body:**
   ```json
   {
     "accountNumber": "0001234567",
     "orderNumber": "20260211-001234",
     "orderPrice": 1006,
     "orderQuantity": 10,
     "priceBand": 1
   }
   ```
   - `orderNumber`: Lấy từ response Place (format `yyyyMMdd-seq`).
   - `orderPrice`, `orderQuantity`, `priceBand`: Giá trị mới (FE tính `priceBand` giống Place).

3. **Success (200):** Hiển thị `message` + `orderNumber`.
4. **Error:** Hiển thị `code`, `message` hoặc `params`.

---

### 8. Cancel Stop Order

1. **API:** `PUT /api/v1/derivatives/stopOrder/cancel`

2. **Request body:**
   ```json
   {
     "accountNumber": "0001234567",
     "orderNumber": "20260211-001234"
   }
   ```

3. **Success (200):** Hiển thị `message` + `orderNumber`.
4. **Error:** Hiển thị `code`, `message`.

---

### 9. Error handling – chung cho Place / Modify / Cancel

| Response | Cách xử lý |
|----------|------------|
| HTTP 200 | Success – hiển thị `message`, `orderNumber` |
| HTTP 400 | Validation – hiển thị `params` (field + message) |
| HTTP 401/403 | Auth – redirect login hoặc thông báo hết phiên |
| HTTP 422 | Lotte error – hiển thị `message` (pass-through, VD: "[V3120] Không đủ ký quỹ") |
| HTTP 500 | Lỗi hệ thống – thông báo chung |

**Lưu ý:** Không dùng field `success` trong response – xác định success bằng HTTP status code.

---

## Data flow tóm tắt (cho dev)

```
User nhập: orderPrice, triggerPrice, orderQuantity, orderDate (default today)
    ↓
FE validate: triggerPrice vs currentPrice (Buy > / Sell <)
    ↓
FE tính: priceBand = triggerPrice − orderPrice
    ↓
POST /api/v1/derivatives/stopOrder
Body: { accountNumber, symbolCode, sellBuyType, orderPrice, orderQuantity, priceBand, orderDate? }
    ↓
Success → Hiển thị message + orderNumber
Error → Hiển thị code / message / params
```

---

## References

| Loại | Link |
|------|------|
| **API Spec** | [Stop_Orders_API_Spec.md](../../../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md) |
| **Figma – Stop Order UI** | [node-id=40005497-339393](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005497-339393&t=7ET4YMgEP2r0vrEW-11) |
| **Figma – Date picker** | [node-id=40005390-286022](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005390-286022&t=7ET4YMgEP2r0vrEW-11) |
