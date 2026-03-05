# TradeX Derivatives API — Chỉ mục (Index)

**Mục đích:** Tổng hợp tất cả API TradeX cho module Phái sinh (Derivatives) đã được đặc tả trong Planning documentation.  
**Đối tượng:** PM, BE, FE — tra cứu nhanh endpoint và mapping Lotte.  
**Cập nhật:** 2026-03-04 — Các spec trong Planning documentation đã đồng bộ tham chiếu Lotte theo [Lotte_DR.md](./[API%20specs]Lotte_DR.md) (27/02/2026).

---

## 1. Quy ước

| Ký hiệu | Ý nghĩa |
|--------|----------|
| **Base path** | `/api/v1/derivatives/...` (qua rest-proxy, JWT) |
| **Lotte** | API phía Lotte Tsolution (xem [\[API specs\]Lotte_DR.md](./[API%20specs]Lotte_DR.md)) |
| **TradeX-native** | Không gọi Lotte, xử lý trong TradeX |

---

## 2. Danh sách API theo nhóm

### 2.1 Order — Lệnh

| Operation | Method | TradeX Endpoint | Lotte (code) | Spec |
|-----------|--------|------------------|--------------|------|
| Đặt lệnh (Mua/Bán) | POST | `/api/v1/derivatives/order` | DRORD-029, DRORD-030 | [Regular_Orders_API_Spec](../Planning%20documentation/Order/Specifications/Regular_Orders_API_Spec.md) |
| Sửa lệnh | PUT | `/api/v1/derivatives/order/modify` | DRORD-032 |同上 |
| Hủy lệnh | PUT | `/api/v1/derivatives/order/cancel` | DRORD-031 | 同上 |
| Danh sách lệnh chưa khớp (trong ngày) | GET | `/api/v1/derivatives/order/todayUnmatch` | DRORD-011 | 同上 |
| Lịch sử đặt lệnh | GET | `/api/v1/derivatives/order/history` | DRORD-010 | 同上 |
| Tra cứu khả năng đặt lệnh | GET | `/api/v1/derivatives/order/checkAvailability` | DRORD-028 | [Order_Availability_Check_API_Spec](../Planning%20documentation/Order/Specifications/Order_Availability_Check_API_Spec.md) |
| Đặt lệnh điều kiện (Stop) | POST | `/api/v1/derivatives/stopOrder` | DRORD-005, DRORD-006 | [Stop_Orders_API_Spec](../Planning%20documentation/Order/Specifications/Stop_Orders_API_Spec.md) |
| Sửa lệnh điều kiện | PUT | `/api/v1/derivatives/stopOrder/modify` | DRORD-023, DRORD-024 | 同上 |
| Hủy lệnh điều kiện | PUT | `/api/v1/derivatives/stopOrder/cancel` | DRORD-025, DRORD-026 | 同上 |
| Đặt TP/SL | POST | `/api/v1/derivatives/tpslOrder` | — (TradeX-native) | [TPSL_Orders_API_Spec](../Planning%20documentation/Order/Specifications/TPSL_Orders_API_Spec.md) |
| Sửa TP/SL | PUT | `/api/v1/derivatives/tpslOrder/modify` | — | 同上 |
| Hủy TP/SL | PUT | `/api/v1/derivatives/tpslOrder/cancel` | — | 同上 |
| Danh sách TP/SL đang hoạt động | GET | `/api/v1/derivatives/tpslOrder/active` | — | 同上 |
| Lịch sử TP/SL | GET | `/api/v1/derivatives/tpslOrder/history` | — | 同上 |

### 2.2 Asset — Tài sản / Vị thế

| Operation | Method | TradeX Endpoint | Lotte (code) | Spec |
|-----------|--------|------------------|--------------|------|
| Vị thế mở | GET | `/api/v1/derivatives/asset/openPositions` | DRACC-003 | [Open_Position_List_API_Spec](../Planning%20documentation/Asset/Specifications/Open_Position_List_API_Spec.md) |
| Thông tin tài sản (balance, margin, …) | GET | `/api/v1/derivatives/asset/info` | DRACC-031 | [Asset_Info_API_Spec](../Planning%20documentation/Asset/Specifications/Asset_Info_API_Spec.md) |
| Lãi lỗ chưa thực hiện | GET | `/api/v1/derivatives/asset/unrealizedPnl` | DRACC-031 (dr-unrealized-pnl) | [Unrealized_PnL_API_Spec](../Planning%20documentation/Asset/Specifications/Unrealized_PnL_API_Spec.md) |
| Lịch sử giao dịch (transaction) | GET | `/api/v1/derivatives/asset/transactionHistory` | DRACC-023 / dr-transaction-history | [Transaction_History_API_Spec](../Planning%20documentation/Asset/Specifications/Transaction_History_API_Spec.md) |

### 2.3 Cash & Transfer — Tiền mặt, Chuyển khoản

| Operation | Method | TradeX Endpoint | Lotte (code) | Spec |
|-----------|--------|------------------|--------------|------|
| Số dư khả dụng (cho chuyển nội bộ) | GET | `/api/v1/derivatives/account/availableBalance` | DRACC-031 | [Internal_Transfer_API_Spec](../Planning%20documentation/Cash%20transaction/Internal%20transfer/Internal_Transfer_API_Spec.md) |
| Chuyển tiền nội bộ | POST | `/api/v1/derivatives/transfer/cash` | DRACC-019 | 同上 |
| Lịch sử chuyển tiền nội bộ | GET | `/api/v1/derivatives/transfer/cash/history` | DRACC-020 | 同上 |
| Lịch sử thanh toán (cash statement) | GET | `/api/v1/derivatives/cash/statement` | DRACC-023 | [Cash_Statement_API_Spec](../Planning%20documentation/Cash%20transaction/Cash%20statement/Cash_Statement_API_Spec.md) |

### 2.4 VSD — Nộp / Rút ký quỹ

| Operation | Method | TradeX Endpoint | Lotte (code) | Spec |
|-----------|--------|------------------|--------------|------|
| Danh sách ngân hàng liên kết | GET | `/api/v1/derivatives/transfer/vsd/banks` | DRACC-032 | [VSD_Transaction_API_Spec](../Planning%20documentation/Cash%20transaction/VSD%20transaction/VSD_Transaction_API_Spec.md) |
| Số dư VSD (balance) | GET | `/api/v1/derivatives/transfer/vsd/balance` | DRACC-031 | 同上 |
| Tính phí nộp tiền | POST | `/api/v1/derivatives/transfer/vsd/deposit/fee` | DRACC-033 | 同上 |
| Nộp tiền ký quỹ (C05) | POST | `/api/v1/derivatives/transfer/vsd/deposit` | DRACC-034 | 同上 |
| Rút tiền ký quỹ (C10) | POST | `/api/v1/derivatives/transfer/vsd/withdraw` | DRACC-009 | 同上 |
| Lịch sử nộp/rút VSD | GET | `/api/v1/derivatives/transfer/vsd/history` | DRACC-021 | 同上 |

### 2.5 Market Data — Thị trường

| Operation | Method | TradeX Endpoint | Ghi chú | Spec |
|-----------|--------|------------------|---------|------|
| Dữ liệu chart (candlestick) | GET | `/tradingview/history` | API hiện có, mở rộng cho mã phái sinh | [Chart_API_Spec](../Planning%20documentation/Market/Specifications/Chart_API_Spec.md) |

*Các API thị trường realtime (bảng giá, quote) thường qua WebSocket — xem [Websocket_DR_Lotte.md](./Websocket_DR_Lotte.md).*

---

## 3. Tổng hợp endpoint (flat list)

Để dễ grep / tích hợp:

```
POST   /api/v1/derivatives/order
PUT    /api/v1/derivatives/order/modify
PUT    /api/v1/derivatives/order/cancel
GET    /api/v1/derivatives/order/todayUnmatch
GET    /api/v1/derivatives/order/history
GET    /api/v1/derivatives/order/checkAvailability

POST   /api/v1/derivatives/stopOrder
PUT    /api/v1/derivatives/stopOrder/modify
PUT    /api/v1/derivatives/stopOrder/cancel

POST   /api/v1/derivatives/tpslOrder
PUT    /api/v1/derivatives/tpslOrder/modify
PUT    /api/v1/derivatives/tpslOrder/cancel
GET    /api/v1/derivatives/tpslOrder/active
GET    /api/v1/derivatives/tpslOrder/history

GET    /api/v1/derivatives/asset/openPositions
GET    /api/v1/derivatives/asset/info
GET    /api/v1/derivatives/asset/unrealizedPnl
GET    /api/v1/derivatives/asset/transactionHistory

GET    /api/v1/derivatives/account/availableBalance
POST   /api/v1/derivatives/transfer/cash
GET    /api/v1/derivatives/transfer/cash/history
GET    /api/v1/derivatives/cash/statement

GET    /api/v1/derivatives/transfer/vsd/banks
GET    /api/v1/derivatives/transfer/vsd/balance
POST   /api/v1/derivatives/transfer/vsd/deposit/fee
POST   /api/v1/derivatives/transfer/vsd/deposit
POST   /api/v1/derivatives/transfer/vsd/withdraw
GET    /api/v1/derivatives/transfer/vsd/history

GET    /tradingview/history   (mở rộng symbol phái sinh)
```

---

## 4. Tài liệu liên quan

| Tài liệu | Mô tả |
|----------|--------|
| [\[API specs\]Lotte_DR.md](./[API%20specs]Lotte_DR.md) | Đặc tả API Lotte Tsolution (DRACC-, DRORD-, DRMKT-) |
| [Websocket_DR_Lotte.md](./Websocket_DR_Lotte.md) | WebSocket kênh thị trường phái sinh |
| [Derivatives/Planning documentation/](../Planning%20documentation/README.md) | Thư mục gốc Planning — README, cấu trúc category |

---

**Document Status:** ✅ Index đã đồng bộ với Planning documentation  
**For:** PM / BE / FE  
**Next Steps:** Cập nhật index khi thêm spec mới trong Planning documentation; đối chiếu với implementation rest-proxy / lotte-bridge khi triển khai.
