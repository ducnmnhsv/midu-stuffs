# Order Status WebSocket Specification (Derivatives)

**Document Type:** Technical Specification — WebSocket  
**Category:** Derivatives Orders — Real-time Order Status  
**Project:** TradeX Derivatives Integration  
**Date:** 2026-05-11  
**Version:** 1.0  
**Author:** BA/PM Team

> **Tham chiếu Lotte:** `Derivatives/Documentation/[API specs]Lotte_DR.md` — §3.1.1 Order events, event_code `F15303`  
> **Tham chiếu WS pattern:** `TradeX Knowledge/System/market-data-channels.md`

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture & Data Flow](#2-architecture--data-flow)
3. [Lotte Source — Event F15303](#3-lotte-source--event-f15303)
4. [BE Specification](#4-be-specification)
5. [WebSocket Contract (Channel & Payload)](#5-websocket-contract-channel--payload)
6. [FE Specification](#6-fe-specification)
7. [Field Mapping](#7-field-mapping)
8. [Status & Code Mapping](#8-status--code-mapping)
9. [Examples](#9-examples)
10. [Error Handling & Reconnection](#10-error-handling--reconnection)
11. [Testing Checklist](#11-testing-checklist)

---

## 1. Overview

### 1.1 Vấn đề

Hiện tại, để xem trạng thái mới nhất của lệnh trên sổ lệnh phái sinh, app phải **gọi API** (`GET /api/v1/derivatives/order/orderBook`) mỗi khi có thay đổi (hoặc poll định kỳ). Điều này dẫn đến:

- Trạng thái lệnh **chậm trễ** so với thực tế
- Tốn băng thông khi gọi lại toàn bộ danh sách lệnh
- Trải nghiệm người dùng kém khi theo dõi lệnh real-time

### 1.2 Giải pháp

App **subscribe WebSocket channel** dành riêng cho tài khoản phái sinh. Khi có thay đổi trạng thái lệnh, BE **đẩy sự kiện** xuống app ngay lập tức. App **cập nhật đúng dòng lệnh** trên sổ mà không cần refresh toàn bộ danh sách.

### 1.3 Phạm vi

| Hạng mục | Mô tả |
|----------|-------|
| **Loại sự kiện** | Chỉ F15303 (trạng thái sổ lệnh) — xem §3 |
| **Loại lệnh** | Tất cả lệnh phái sinh: Regular Orders (LO, ATO, ATC, MOK, MAK, MTL) |
| **Màn hình FE** | Sổ lệnh phái sinh (Order Book screen), Today Unmatch |
| **Không thuộc scope** | F15302 (khớp lệnh — event riêng), Conditional Orders (spec riêng), Account events F15xxx |

### 1.4 Luồng tổng quan

```
Lotte Core
    │ (WebSocket subscription)
    │  sub/bos.evt.ord.sts.*/
    ▼
order-event-collector (BE service — NEW)
    │ Filter F15303
    │ Normalize payload
    │ Route by accountNumber
    ▼
ws-v2 (TradeX WebSocket server)
    │ Channel: order.status.{accountNumber}
    ▼
NHSV Pro App (FE)
    │ Merge by orderId → Update UI dòng lệnh
    ▼
Sổ lệnh cập nhật real-time ✅
```

---

## 2. Architecture & Data Flow

### 2.1 Sơ đồ hệ thống

```
┌─────────────────────────────────────────────────────────────────┐
│                          Lotte Core                             │
│  WebSocket endpoint: wss://[lotte-ws-host]/                     │
│  Subscription: sub/bos.evt.ord.sts.*/                           │
│  Events: F15302 (match) | F15303 (order status) | ...           │
└──────────────────────────┬──────────────────────────────────────┘
                           │ JSON push per order event
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│           order-event-collector  (BE — NEW component)            │
│                                                                  │
│  1. Maintain persistent WS session to Lotte                      │
│  2. Parse JSON → check event_code                                │
│  3. Filter: only forward F15303                                  │
│  4. Normalize: Lotte fields → TradeX WS payload                  │
│  5. Publish to Kafka topic: order-status-events                  │
└──────────────────────────┬───────────────────────────────────────┘
                           │ Kafka: order-status-events
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                  ws-v2  (TradeX WebSocket server)                │
│                                                                  │
│  - Consume Kafka topic: order-status-events                      │
│  - Route event → channel: order.status.{accountNumber}          │
│  - Push to all active sessions subscribed to that channel        │
└──────────────────────────┬──────────────────────────────────────┘
                           │ WebSocket (SocketCluster protocol)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NHSV Pro App  (FE)                            │
│                                                                  │
│  - Connected via socketCluster (existing pattern)                │
│  - Subscribe: order.status.{accountNumber}                       │
│  - On event: merge by orderId → update order row in state        │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Quan hệ với REST API

| Action | Cơ chế | Lý do |
|--------|--------|-------|
| **Vào màn sổ lệnh** | Gọi REST API orderBook | Lấy snapshot đầy đủ |
| **Đang xem sổ lệnh** | WS events F15303 | Cập nhật incremental từng dòng |
| **Reconnect WS** | Gọi lại REST API orderBook | Đồng bộ trạng thái bị miss |
| **App từ background lên** | Gọi lại REST API orderBook | Đảm bảo không lệch state |

> **Nguyên tắc:** WS là **incremental update**, REST API là **snapshot source of truth**. Không thể dùng WS thay thế hoàn toàn REST.

---

## 3. Lotte Source — Event F15303

### 3.1 Subscription

- **URL:** `wss://[lotte-ws-host]/`  
- **Subscribe message:** `sub/bos.evt.ord.sts.*/`
- **Format dữ liệu:** JSON
- **Xác thực:** Oauth2 / API KEY (Lotte)

### 3.2 Cấu trúc payload Lotte F15303

| Field | Type | Format | Mô tả |
|-------|------|--------|-------|
| `event_code` | String | `F15303` | Mã sự kiện — bộ lọc chính |
| `event_seqno` | String | | Số sequence (dùng để detect missing events) |
| `date` | String | `yyyymmdd` | Ngày sự kiện |
| `acnt_no` | String | | Số tài khoản phái sinh |
| `evt_time` | String | `HH:mm:ss` | Thời điểm sự kiện |
| `evt_ordNo` | String | | Số hiệu lệnh (khóa đối chiếu sổ lệnh) |
| `evt_account` | String | | Số tài khoản (theo sự kiện) |
| `evt_code` | String | | Mã hợp đồng (VD: `VN30F2506`) |
| `evt_side` | String | | Chiều lệnh: `Mua` / `Bán` |
| `evt_ordType` | String | | Loại lệnh (mã Core): `0` LO, `2` ATO, `3` MAK, `4` MOK, `7` ATC, `9` MTL |
| `evt_price` | String | | Giá đặt |
| `evt_qty` | String | | Khối lượng đặt |
| `evt_status` | String | | Trạng thái lệnh: `A/B/C/D/R` — xem §8 |
| `evt_matchQty` | String | | Khối lượng đã khớp |
| `evt_remQty` | String | | Khối lượng còn lại chưa khớp |
| `evt_matchPrice` | String | | Giá khớp |

### 3.3 Phân biệt F15302 vs F15303

| | F15302 | F15303 |
|-|--------|--------|
| **Mục đích** | Thông báo **khớp lệnh** (match event) | Thông báo **thay đổi trạng thái** lệnh (lifecycle) |
| **Trigger** | Khi lệnh khớp một phần / toàn bộ | Khi lệnh thay đổi state: mới / khớp / hủy / sửa / từ chối |
| **Field đặc trưng** | `series`, `sb_tp`, `mth_qty`, `mth_pri` | `evt_ordNo`, `evt_status`, `evt_matchQty`, `evt_remQty` |
| **Dùng cho** | Notification "lệnh vừa khớp" | **Cập nhật sổ lệnh real-time** ← scope này |

> **Kết luận:** BE chỉ cần forward **F15303** cho scope này. Nếu sau này cần notification khớp lệnh (F15302), xử lý trong spec riêng.

---

## 4. BE Specification

### 4.1 Component mới: `order-event-collector`

> Có thể implement là module mới trong service hiện có (ví dụ `lotte-bridge` hoặc `market-collector-lotte`) hoặc service độc lập — tùy quyết định kiến trúc của team BE.

#### 4.1.1 Kết nối Lotte WS

| Yêu cầu | Chi tiết |
|---------|---------|
| Endpoint | `wss://[lotte-ws-host]/` (lấy từ config/secret) |
| Xác thực | Oauth2 token hoặc API KEY theo quy định Lotte |
| Subscribe | Gửi `sub/bos.evt.ord.sts.*/` sau khi kết nối thành công |
| Reconnect | Exponential backoff: 5s → 10s → 20s → tối đa 120s |
| Keepalive | Gửi ping/heartbeat theo yêu cầu Lotte |

#### 4.1.2 Xử lý message

```
Nhận JSON từ Lotte
    │
    ├─ Parse JSON
    ├─ Đọc field event_code
    ├─ event_code == "F15303"? → Tiếp tục xử lý
    └─ event_code != "F15303"? → Bỏ qua (log DEBUG nếu cần)
                │
                ▼ (chỉ F15303)
        Validate acnt_no có trong danh sách tài khoản hợp lệ?
                │
                ├─ Có → Normalize (§4.1.3) → Publish Kafka
                └─ Không → Bỏ qua, log WARNING
```

#### 4.1.3 Normalize payload (Lotte → TradeX)

Mục đích: FE không phụ thuộc vào naming convention của Lotte Core.

| Lotte Field | TradeX WS Field | Ghi chú |
|-------------|-----------------|---------|
| `event_code` | `eventType` | Giá trị: `"ORDER_STATUS"` (fixed, không dùng raw `F15303`) |
| `event_seqno` | `seqNo` | Giữ nguyên string |
| `date` + `evt_time` | `eventTime` | Ghép thành ISO 8601: `"2026-05-11T14:48:22+07:00"` |
| `acnt_no` | `accountNumber` | |
| `evt_ordNo` | `orderId` | Khóa chính để FE merge |
| `evt_code` | `symbolCode` | |
| `evt_side` | `side` | Map: `"Mua"` → `"BUY"`, `"Bán"` → `"SELL"` |
| `evt_ordType` | `orderType` | Map mã Core → TradeX: xem §8.2 |
| `evt_price` | `price` | String → Number (parse, giữ null nếu rỗng) |
| `evt_qty` | `quantity` | String → Number |
| `evt_status` | `status` | Map A/B/C/D/R → TradeX enum: xem §8.1 |
| `evt_matchQty` | `matchedQuantity` | String → Number |
| `evt_remQty` | `remainingQuantity` | String → Number |
| `evt_matchPrice` | `matchedPrice` | String → Number (null nếu chưa khớp) |

#### 4.1.4 Publish Kafka

- **Topic:** `order-status-events`
- **Key:** `accountNumber` (dùng để partition và route)
- **Payload:** JSON theo TradeX WS contract (§5.2)

#### 4.1.5 Bảo mật — phân tách theo tài khoản

> Đây là yêu cầu **bắt buộc** về bảo mật.

- Mỗi event F15303 chứa `acnt_no` — đây là tài khoản phái sinh của nhà đầu tư
- ws-v2 chỉ đẩy event xuống **session đã đăng nhập với account tương ứng** (`accountNumber` match với JWT)
- **Tuyệt đối không** broadcast event của account A sang session của account B

#### 4.1.6 Sequence number monitoring (khuyến nghị)

- Theo dõi `seqNo` để phát hiện **missing events** (gap trong sequence)
- Nếu phát hiện gap: log WARNING; tùy nghiệp vụ có thể trigger pull API để bù lại

---

### 4.2 Cập nhật ws-v2

| Yêu cầu | Chi tiết |
|---------|---------|
| Consume Kafka | Subscribe topic `order-status-events` |
| Channel naming | `order.status.{accountNumber}` |
| Auth check | Verify JWT session có quyền với `accountNumber` trong channel |
| Push | Đẩy TradeX WS payload xuống tất cả client đang subscribe channel đó |

---

## 5. WebSocket Contract (Channel & Payload)

### 5.1 Channel

```
order.status.{accountNumber}

Ví dụ: order.status.0001234567
```

- `accountNumber` là số tài khoản phái sinh (10 chữ số, theo format TradeX)

### 5.2 Payload — TradeX WS Event

```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000123",
  "eventTime": "2026-05-11T14:48:22+07:00",
  "accountNumber": "0001234567",
  "orderId": "20260511000456",
  "symbolCode": "VN30F2506",
  "side": "BUY",
  "orderType": "LO",
  "price": 1280.0,
  "quantity": 10,
  "status": "MATCHED",
  "matchedQuantity": 5,
  "remainingQuantity": 5,
  "matchedPrice": 1279.5
}
```

### 5.3 Subscribe / Unsubscribe (SocketCluster protocol)

**Subscribe (gửi sau khi app vào màn sổ lệnh):**
```json
{
  "event": "#subscribe",
  "data": {
    "channel": "order.status.0001234567"
  }
}
```

**Unsubscribe (khi rời màn sổ lệnh):**
```json
{
  "event": "#unsubscribe",
  "data": "order.status.0001234567"
}
```

> **Lưu ý:** App đang dùng `socketCluster` — pattern này nhất quán với cách subscribe market data channel hiện tại.

---

## 6. FE Specification

### 6.1 Khi nào subscribe / unsubscribe

| Thời điểm | Action |
|-----------|--------|
| Mount màn sổ lệnh (OrderBook screen) | Subscribe `order.status.{accountNumber}` |
| Unmount màn sổ lệnh | Unsubscribe |
| App từ background → foreground | Gọi REST API orderBook → re-subscribe |
| WS reconnect thành công | Gọi REST API orderBook → re-subscribe |
| Đăng xuất | Unsubscribe tất cả channel order.status |

### 6.2 Xử lý event nhận được

```
Nhận message từ channel order.status.{accountNumber}
    │
    ├─ Parse JSON
    ├─ Kiểm tra eventType === "ORDER_STATUS"?
    │       └─ Không? → Bỏ qua (guard cho tương lai)
    │
    ├─ Lấy orderId từ payload
    ├─ Tìm dòng lệnh tương ứng trong state hiện tại
    │
    ├─ Tìm thấy → Cập nhật các trường: status, matchedQuantity, remainingQuantity, matchedPrice
    │
    └─ Không tìm thấy → Không tự thêm mới; gọi REST API refresh để lấy snapshot đầy đủ
```

> **Không tự thêm dòng mới vào state khi chỉ có WS event.** Luôn dùng REST API để lấy danh sách đầy đủ ban đầu.

### 6.3 Cập nhật state theo từng status

| status nhận được | Hành động trong state | Hiển thị |
|------------------|-----------------------|----------|
| `NEW` | Cập nhật row: status = NEW | "Chờ khớp" (nếu row đã có từ API) |
| `MATCHED` | Cập nhật: status, matchedQty, remQty, matchedPrice | "Đã khớp" / khớp một phần |
| `CANCELLED` | Cập nhật: status = CANCELLED, remQty = 0 | "Đã hủy" |
| `MODIFIED` | Cập nhật: status, price, quantity | "Đã sửa" |
| `REJECTED` | Cập nhật: status = REJECTED | "Từ chối" |

### 6.4 Logic khớp một phần vs khớp toàn bộ

```
status === "MATCHED"
    │
    ├─ remainingQuantity > 0 → Khớp một phần (hiển thị matchedQty / totalQty)
    └─ remainingQuantity === 0 → Khớp toàn bộ
```

### 6.5 Deduplicate

- Nếu nhận nhiều event cho cùng `orderId`: dùng `seqNo` để chỉ apply event **có seqNo lớn hơn** (tránh apply event cũ sau event mới do network delay)
- Nếu `seqNo` không available hoặc không parseable: apply event mới nhất theo thứ tự nhận

### 6.6 UI feedback

| Trường hợp | Phản hồi UI |
|------------|------------|
| Status thay đổi (bất kỳ) | Highlight nhẹ dòng lệnh (animation 0.5s) |
| MATCHED | Badge xanh / toast nếu app đang background |
| CANCELLED / REJECTED | Badge đỏ |
| WS mất kết nối | Hiển thị indicator "Đang kết nối lại..." |
| WS reconnect xong | Tắt indicator, refresh silently |

---

## 7. Field Mapping

### 7.1 Lotte F15303 → TradeX WS Payload (đầy đủ)

| # | Lotte Field | Lotte Type | TradeX WS Field | TradeX Type | Transform | Ghi chú |
|---|-------------|------------|-----------------|-------------|-----------|---------|
| 1 | `event_code` | String | `eventType` | String | Fixed: `"ORDER_STATUS"` | Không expose raw `F15303` ra FE |
| 2 | `event_seqno` | String | `seqNo` | String | Pass-through | Dùng để dedupe |
| 3 | `date` + `evt_time` | String | `eventTime` | String (ISO 8601) | Ghép và format: `yyyymmdd` + `HH:mm:ss` → `yyyy-MM-ddTHH:mm:ss+07:00` | |
| 4 | `acnt_no` | String | `accountNumber` | String | Pass-through | Route key |
| 5 | `evt_ordNo` | String | `orderId` | String | Pass-through | **Khóa merge FE** |
| 6 | `evt_code` | String | `symbolCode` | String | Pass-through | |
| 7 | `evt_side` | String | `side` | Enum String | `"Mua"` → `"BUY"` / `"Bán"` → `"SELL"` | |
| 8 | `evt_ordType` | String | `orderType` | Enum String | Xem §8.2 | |
| 9 | `evt_price` | String | `price` | Number/null | Parse to Number; rỗng/null → `null` | Lệnh market price có thể null |
| 10 | `evt_qty` | String | `quantity` | Number | Parse to Number | |
| 11 | `evt_status` | String | `status` | Enum String | Xem §8.1 | |
| 12 | `evt_matchQty` | String | `matchedQuantity` | Number | Parse to Number; `0` nếu chưa khớp | |
| 13 | `evt_remQty` | String | `remainingQuantity` | Number | Parse to Number | |
| 14 | `evt_matchPrice` | String | `matchedPrice` | Number/null | Parse to Number; rỗng/null → `null` | null khi chưa có khớp |

---

## 8. Status & Code Mapping

### 8.1 Order Status: Lotte evt_status → TradeX

| Lotte `evt_status` | TradeX `status` | Mô tả | Hiển thị UI (gợi ý) |
|--------------------|-----------------|-------|---------------------|
| `A` | `NEW` | Lệnh mới — đã tiếp nhận | "Chờ khớp" |
| `B` | `MATCHED` | Khớp (một phần hoặc toàn bộ) | "Đã khớp" / "Khớp một phần" |
| `C` | `CANCELLED` | Đã hủy | "Đã hủy" |
| `D` | `MODIFIED` | Đã sửa | "Đã sửa" |
| `R` | `REJECTED` | Bị từ chối | "Từ chối" |

### 8.2 Order Type: Lotte evt_ordType → TradeX

| Lotte `evt_ordType` | TradeX `orderType` | Mô tả |
|---------------------|-------------------|-------|
| `0` | `LO` | Lệnh giới hạn |
| `2` | `ATO` | Lệnh mở cửa |
| `3` | `MAK` | Market At Kill |
| `4` | `MOK` | Market Or Kill |
| `7` | `ATC` | Lệnh đóng cửa |
| `9` | `MTL` | Market To Limit |

### 8.3 Side: Lotte evt_side → TradeX

| Lotte `evt_side` | TradeX `side` |
|------------------|--------------|
| `Mua` | `BUY` |
| `Bán` | `SELL` |

---

## 9. Examples

### 9.1 Lệnh khớp toàn bộ

**Lotte F15303 payload (raw):**
```json
{
  "event_code": "F15303",
  "event_seqno": "20260511000123",
  "date": "20260511",
  "acnt_no": "0001234567",
  "evt_time": "14:48:22",
  "evt_ordNo": "20260511000456",
  "evt_account": "0001234567",
  "evt_code": "VN30F2506",
  "evt_side": "Mua",
  "evt_ordType": "0",
  "evt_price": "1280",
  "evt_qty": "10",
  "evt_status": "B",
  "evt_matchQty": "10",
  "evt_remQty": "0",
  "evt_matchPrice": "1279.5"
}
```

**TradeX WS payload → FE nhận được:**
```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000123",
  "eventTime": "2026-05-11T14:48:22+07:00",
  "accountNumber": "0001234567",
  "orderId": "20260511000456",
  "symbolCode": "VN30F2506",
  "side": "BUY",
  "orderType": "LO",
  "price": 1280.0,
  "quantity": 10,
  "status": "MATCHED",
  "matchedQuantity": 10,
  "remainingQuantity": 0,
  "matchedPrice": 1279.5
}
```

**FE action:** Tìm `orderId = "20260511000456"` trong state → cập nhật `status = MATCHED`, `matchedQuantity = 10`, `remainingQuantity = 0`, `matchedPrice = 1279.5` → Highlight dòng.

---

### 9.2 Lệnh khớp một phần

**TradeX WS payload:**
```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000124",
  "eventTime": "2026-05-11T14:49:05+07:00",
  "accountNumber": "0001234567",
  "orderId": "20260511000457",
  "symbolCode": "VN30F2506",
  "side": "SELL",
  "orderType": "LO",
  "price": 1285.0,
  "quantity": 20,
  "status": "MATCHED",
  "matchedQuantity": 8,
  "remainingQuantity": 12,
  "matchedPrice": 1285.0
}
```

**FE action:** `status = MATCHED`, hiển thị `"Khớp một phần: 8/20"`. Tiếp tục chờ event tiếp theo.

---

### 9.3 Lệnh bị hủy

**TradeX WS payload:**
```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000125",
  "eventTime": "2026-05-11T15:01:30+07:00",
  "accountNumber": "0001234567",
  "orderId": "20260511000457",
  "symbolCode": "VN30F2506",
  "side": "SELL",
  "orderType": "LO",
  "price": 1285.0,
  "quantity": 20,
  "status": "CANCELLED",
  "matchedQuantity": 8,
  "remainingQuantity": 0,
  "matchedPrice": null
}
```

**FE action:** `status = CANCELLED`, `remainingQuantity = 0` → Badge đỏ.

---

## 10. Error Handling & Reconnection

### 10.1 BE — Mất kết nối Lotte WS

| Tình huống | Hành động BE |
|------------|-------------|
| Lotte WS ngắt kết nối | Log ERROR; bắt đầu reconnect với exponential backoff |
| Reconnect thành công | Re-subscribe `sub/bos.evt.ord.sts.*/`; log INFO |
| Reconnect thất bại liên tục (> 5 lần) | Alert / alarm hệ thống; không crash service |
| Gap sequence phát hiện | Log WARNING với `(seqNo_before, seqNo_after)` |

### 10.2 FE — Mất kết nối WS TradeX

| Tình huống | Hành động FE |
|------------|-------------|
| WS ngắt kết nối | Hiển thị indicator "Đang kết nối lại..."; không xóa state |
| Reconnect thành công | Gọi REST API orderBook để lấy snapshot mới; re-subscribe channel; tắt indicator |
| App ra background | Giữ state; khi vào foreground: gọi REST API → re-subscribe |
| Timeout reconnect (> 30s) | Hiển thị thông báo "Không có kết nối — Kéo để làm mới" |

### 10.3 FE — Event không tìm thấy orderId

| Tình huống | Hành động FE |
|------------|-------------|
| orderId trong event không có trong state | Không tự thêm dòng; gọi REST API refresh (silent) |
| Event trùng seqNo | Bỏ qua event trùng |
| Event seqNo nhỏ hơn seqNo đã apply | Bỏ qua (stale event) |

---

## 11. Testing Checklist

### 11.1 BE

- [ ] Subscribe `sub/bos.evt.ord.sts.*/` thành công sau khi kết nối Lotte WS
- [ ] Chỉ forward event có `event_code === "F15303"` — bỏ qua F15302 và các mã khác
- [ ] Normalize đúng tất cả fields theo §7.1
- [ ] Map đúng `evt_status` A/B/C/D/R → TradeX enum §8.1
- [ ] Map đúng `evt_ordType` 0/2/3/4/7/9 → TradeX enum §8.2
- [ ] Publish đúng Kafka topic `order-status-events` với key = `accountNumber`
- [ ] ws-v2 chỉ push event xuống session có `accountNumber` khớp (kiểm tra bảo mật)
- [ ] Reconnect Lotte WS khi bị ngắt — re-subscribe sau khi kết nối lại
- [ ] Log WARNING khi phát hiện gap sequence

### 11.2 FE

- [ ] Subscribe `order.status.{accountNumber}` khi mount màn sổ lệnh
- [ ] Unsubscribe khi unmount màn sổ lệnh
- [ ] Parse payload đúng và lọc `eventType === "ORDER_STATUS"`
- [ ] Merge đúng theo `orderId` — không tạo dòng mới khi orderId không có trong state
- [ ] Deduplicate theo `seqNo` — bỏ qua event cũ hơn
- [ ] Cập nhật đúng `status`, `matchedQuantity`, `remainingQuantity`, `matchedPrice`
- [ ] Hiển thị "khớp một phần" khi `status = MATCHED` và `remainingQuantity > 0`
- [ ] Gọi REST API refresh khi: reconnect WS, app foreground, orderId không tìm thấy
- [ ] Hiển thị indicator khi WS mất kết nối
- [ ] Tắt indicator và refresh silently sau khi reconnect thành công

---

## Related Documents

| Document | Location |
|----------|----------|
| Lotte DR API Specs (F15303) | `Derivatives/Documentation/[API specs]Lotte_DR.md` §3.1.1 |
| Websocket DR Lotte (market data channels) | `Derivatives/Documentation/Websocket_DR_Lotte.md` |
| Regular Orders API Spec | `Derivatives/Planning documentation/Order/Specifications/Regular_Orders_API_Spec.md` |
| TradeX API Conventions | `TradeX Knowledge/API Standards/tradex-api-conventions.md` |
| Market Data Channels (WS pattern tham chiếu) | `TradeX Knowledge/System/market-data-channels.md` |

---

**Document Status:** 📋 Ready for Review  
**For:** BE Developers, FE Developers, QA  
**Next Steps:**  
1. BE review §4 (component design và Kafka integration với kiến trúc hiện tại)  
2. FE review §6 (subscribe pattern với socketCluster hiện tại)  
3. Confirm `orderId` format từ Lotte F15303 = `orderId` trong REST API orderBook response  
4. Sign-off → đưa vào sprint planning
