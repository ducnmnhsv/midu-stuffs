# Order Status WebSocket Specification (Derivatives)

**Document Type:** Technical Specification — WebSocket  
**Category:** Derivatives Orders — Real-time Order Status  
**Project:** TradeX Derivatives Integration  
**Date:** 2026-05-11  
**Version:** 1.4  
**Author:** BA/PM Team

> **Tham chiếu Lotte:** `Derivatives/Documentation/[API specs]Lotte_DR.md` — §3.1.1 Order events, event_code `F15303`  
> **Tham chiếu WS pattern:** `TradeX Knowledge/System/market-data-channels.md`

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture & Data Flow](#2-architecture--data-flow) *(§2.3: REST snapshot `todayUnmatch` + `history`)*
3. [Lotte Source — Event F15303](#3-lotte-source--event-f15303)
4. [BE Specification](#4-be-specification)
5. [WebSocket Contract (Channel & Payload)](#5-websocket-contract-channel--payload)
6. [FE Specification](#6-fe-specification) *(§6.7 refresh field sau WS; §6.8 nút Hủy/Sửa)*
7. [Field Mapping](#7-field-mapping)
8. [Status & Code Mapping](#8-status--code-mapping)
9. [Examples](#9-examples)
10. [Error Handling & Reconnection](#10-error-handling--reconnection)
11. [Testing Checklist](#11-testing-checklist)

---

## 1. Overview

### 1.1 Vấn đề

Hiện tại, để xem trạng thái mới nhất của **lệnh thường** phái sinh trên app, NHSV Pro **không dùng một API “orderBook” đơn lẻ** mà kết hợp **hai REST snapshot** (chi tiết §2.3): danh sách lệnh chưa khớp hết / còn hiệu lực, và lịch sử lệnh trong ngày. Khi không có WebSocket, app phải **poll hoặc refresh** các API này để thấy thay đổi — điều này dẫn đến:

- Trạng thái lệnh **chậm trễ** so với thực tế
- Tốn băng thông khi gọi lại toàn bộ danh sách lệnh
- Trải nghiệm người dùng kém khi theo dõi lệnh real-time

### 1.2 Giải pháp

App **subscribe WebSocket channel** dành riêng cho tài khoản phái sinh. Khi có thay đổi trạng thái lệnh, BE **đẩy sự kiện** xuống app ngay lập tức. FE **merge event vào đúng dòng lệnh** trong state đã load từ **todayUnmatch** và/hoặc **history** (khớp theo `orderNumber` / quan hệ lệnh gốc — §2.3, §6.2) với các giá trị mới nhất: KL đã khớp, KL còn lại, giá khớp, trạng thái, loại thao tác lifecycle — **không cần** gọi lại toàn bộ REST sau mỗi tick (vẫn giữ REST làm snapshot và khi reconnect — §2.2).

### 1.3 Phạm vi

| Hạng mục | Mô tả |
|----------|-------|
| **Loại sự kiện** | Chỉ F15303 (trạng thái sổ lệnh) — xem §3 |
| **Loại lệnh** | Tất cả lệnh phái sinh: Regular Orders (LO, ATO, ATC, MOK, MAK, MTL) |
| **Màn hình FE** | Lệnh thường phái sinh: **Today Unmatch** + **History (trong ngày)** — snapshot từ hai REST API §2.3 |
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
    │ Merge by orderNumber → Update UI dòng lệnh
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
│  - On event: merge by orderNumber → update order row in state      │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Quan hệ với REST API

| Action | Cơ chế | Lý do |
|--------|--------|-------|
| **Vào màn lệnh / sau login** | Gọi REST snapshot theo §2.3 (`todayUnmatch` + `history` trong ngày) | Lấy snapshot đầy đủ cho hai danh sách app đang dùng |
| **Đang xem lệnh** | WS events F15303 (TradeX channel §5) | Cập nhật incremental — khớp **`orderNumber`** payload WS ↔ **`orderNumber`** REST §7.2 |
| **Reconnect WS** | Gọi lại REST snapshot §2.3 | Đồng bộ trạng thái bị miss |
| **App từ background lên** | Gọi lại REST snapshot §2.3 | Đảm bảo không lệch state |

> **Nguyên tắc:** WS là **incremental update**, REST là **snapshot source of truth**. Không thể dùng WS thay thế hoàn toàn REST.

### 2.3 REST snapshot trên app — lệnh thường phái sinh

App NHSV Pro hiện **hiển thị / cho phép hủy–sửa** lệnh thường dựa trên **hai nguồn**:

| API | Vai trò | Ghi chú nghiệp vụ |
|-----|---------|-------------------|
| `GET /api/v1/derivatives/order/todayUnmatch` | Danh sách lệnh **chưa khớp hết** hoặc **vẫn còn hiệu lực** | Field `status` dùng cho nhãn/lifecycle cùng hệ với `operation` ở history (§6.7). **Nút Hủy/Sửa** không phải lúc nào cũng bật — xem rule theo `orderType` §6.8 |
| `GET /rest/api/v1/derivatives/order/history` | **Toàn bộ lệnh** trong ngày (theo cách app đang gọi) | App đang dùng `fromDate = toDate = today`, `orderType = ALL`, `matchType = ALL` để lấy snapshot **trong ngày** |

**Ví dụ response — todayUnmatch:**

```json
{
  "orderNumber": 1000006,
  "symbolCode": "41I1FB000",
  "sellBuyType": "SELL",
  "orderType": "LO",
  "status": "New Order waiting",
  "orderPrice": 1473,
  "orderQuantity": 1,
  "matchedQuantity": 0,
  "unmatchedQuantity": 1
}
```

**Ví dụ response — history:**

```json
{
  "orderNumber": 1000008,
  "originalOrderNumber": 1000005,
  "symbolCode": "41I1FB000",
  "sellBuyType": "SELL",
  "orderPrice": 1400.1,
  "matchedPrice": 0,
  "orderQuantity": 1,
  "matchedQuantity": 0,
  "unmatchedQuantity": 0,
  "operation": "MODIFY_ORDER",
  "orderType": "LO",
  "orderTime": "20251029 105719",
  "rejectReason": " ",
  "orderStatus": "CONFIRMED",
  "nextKey": "0"
}
```

**Mục tiêu WebSocket đối với hai nguồn này**

- Khi nhận event trạng thái lệnh (sau normalize §4), FE **render lại UI** đúng các cột đã thống nhất trong §6.7 (history vs todayUnmatch), rồi **áp lại rule nút** §6.8.
- Tối thiểu đồng bộ: **KL đã khớp**, **KL chưa khớp**, **giá khớp**, **`orderStatus`**, **`operation`** — cùng **tên field / enum** với REST history (`/api/v1/derivatives/order/history`) để FE không đổi mapping — §6.7, §8.1.
- **Khóa khớp:** payload WS dùng **`orderNumber`** (string) — đối chiếu **`orderNumber`** trên REST (so sánh sau normalize kiểu).
- **Lệnh gốc:** Lotte `evt_orgOrdNo` → TradeX **`originalOrderNumber`** trên WS (§5.2). FE merge theo **`orderNumber`** hoặc **`originalOrderNumber`** theo rule BA.

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
| `evt_orgOrdNo` | String | | Số hiệu lệnh gốc (khi sửa/hủy; `0` nếu không áp dụng) |
| `evt_action` | String | | Hành động sự kiện — **mã số**, xem §3.4 và §8.4 |
| `evt_account` | String | | Số tài khoản (theo sự kiện) |
| `evt_code` | String | | Mã hợp đồng (VD: `41I16600`, `VN30F2506`) |
| `evt_side` | String | | Chiều lệnh — **mã số** `1` MUA / `2` BÁN (xem §3.4, §8.3); một số luồng có thể gửi dạng chữ `Mua`/`Bán` |
| `evt_ordType` | String | | Loại lệnh (mã Core): `0` LO, `2` ATO, `3` MAK, `4` MOK, `7` ATC, `9` MTL |
| `evt_price` | String | | Giá đặt (lệnh thị trường / bước chuyển giá có thể là `0`) |
| `evt_qty` | String | | Khối lượng đặt |
| `evt_status` | String | | Trạng thái lệnh — **mã số** `0`–`5` hoặc chữ `R` / `X` — xem §3.4 và §8.1 |
| `evt_matchQty` | String | | Khối lượng đã khớp |
| `evt_remQty` | String | | Khối lượng còn lại chưa khớp |
| `evt_matchPrice` | String | | Giá khớp (có thể `0` trên event tiếp theo sau khi Core đã “bơm” giá vào `evt_price` — xem §4.1.4 bên dưới) |

### 3.4 Ngữ nghĩa mã trong `eventBody` (Lotte WebSocket)

Giá trị dưới đây là **chuẩn vận hành** từ Core qua kênh order status (`F15303`). BE TradeX map sang enum TradeX theo §8.

#### `evt_side` — chiều lệnh

| Giá trị Lotte | Ý nghĩa |
|---------------|--------|
| `1` | MUA |
| `2` | BÁN |

#### `evt_action` — loại thay đổi

| Giá trị Lotte | Ý nghĩa |
|---------------|--------|
| `1` | Lệnh mới |
| `2` | Lệnh sửa |
| `3` | Lệnh hủy |

#### `evt_status` — trạng thái / kết quả

| Giá trị Lotte | Ý nghĩa |
|---------------|--------|
| `0` | Tiếp nhận |
| `1` | Chuyển |
| `2` | Xác nhận lệnh |
| `3` | Xác nhận tiếp nhận |
| `4` | Khớp toàn bộ |
| `5` | Khớp một phần |
| `R` | Từ chối (NHSV từ chối) |
| `X` | Từ chối (Sở từ chối) |

> **Ghi chú tài liệu hóa:** Bảng mapping TradeX `status` (§8.1) dùng các mã trên. Nếu gặp mã legacy dạng chữ (`A`/`B`/…) từ nguồn khác, cần bảng map riêng hoặc xác nhận lại với Core — không trộn với bảng `0`–`5`/`R`/`X` mà không có quyết định BA.

### 3.5 Phân biệt F15302 vs F15303

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
| `date` + `evt_time` | `eventTime` | Chuỗi **`yyyymmdd HH:mm:ss`** (cùng style ví dụ history / PM), VD `"20260511 14:48:22"` — hoặc ISO 8601 nếu BE/FE đã thống nhất (ghi rõ trong release notes) |
| `acnt_no` | `accountNumber` | |
| `evt_ordNo` | `orderNumber` | Khóa merge FE — **cùng tên** REST history |
| `evt_code` | `symbolCode` | |
| `evt_side` | `sellBuyType` | Map `"1"`/`"Mua"` → `"BUY"`; `"2"`/`"Bán"` → `"SELL"` — §8.3 |
| `evt_action` | `operation` | Map `1`/`2`/`3` → `NEW_ORDER` / `MODIFY_ORDER` / `CANCEL_ORDER` — §8.4 (**nguồn Lotte là `evt_action`**, không phải `evt_status`) |
| `evt_ordType` | `orderType` | Map mã Core → TradeX: §8.2 |
| `evt_price` | `price` | String → Number |
| `evt_qty` | `quantity` | String → Number |
| `evt_status` | `orderStatus` | Map Lotte → enum TradeX **đồng nhất history** — §8.1 |
| `evt_matchQty` | `matchedQuantity` | String → Number |
| `evt_remQty` | `remainingQuantity` | String → Number |
| `evt_matchPrice` | `matchedPrice` | Áp dụng §4.1.4 (lệnh ≠ LO) trước khi parse |
| `evt_orgOrdNo` | `originalOrderNumber` | `"0"` / rỗng → `null`; khác → string — khớp REST history |

#### 4.1.4 Giá khớp (`matchedPrice`) — lệnh **không phải LO** (bắt buộc TradeX BE)

Áp dụng khi `orderType` sau map **≠** `LO` (VD: MTL, MOK, MAK, ATO, ATC).

| Điều kiện (sau parse số) | Hành vi TradeX BE |
|-------------------------|-------------------|
| `evt_price == 0` **và** `evt_matchPrice != 0` | **Giá khớp hiệu lực** = `evt_matchPrice`. Lưu vào state tạm theo **`orderNumber`** (`lastMatchPriceByOrderNumber`). |
| Event **tiếp theo** cùng **`orderNumber`**: `evt_price != 0` **và** `evt_matchPrice == 0` (hoặc `"0"`) | **Không** ghi đè `matchedPrice` trên payload TradeX thành `0`/`null` chỉ vì Lotte gửi `evt_matchPrice = 0`. Giữ `lastMatchPriceByOrderNumber` đến khi có event đổi giá có nghiệp vụ hoặc kết thúc lệnh. |

**Lý do nghiệp vụ:** Core có thể gửi **hai (hoặc nhiều) event** cho cùng một lệnh thị trường: event đầu mang giá khớp ở `evt_matchPrice` khi `evt_price` còn 0; event sau “bơm” giá giới hạn vào `evt_price` nhưng tắt `evt_matchPrice` về 0. FE chỉ cần một `matchedPrice` ổn định trên kênh TradeX.

#### 4.1.5 Publish Kafka

- **Topic:** `order-status-events`
- **Key:** `accountNumber` (dùng để partition và route)
- **Payload:** JSON theo TradeX WS contract (§5.2)

#### 4.1.6 Bảo mật — phân tách theo tài khoản

> Đây là yêu cầu **bắt buộc** về bảo mật.

- Mỗi event F15303 chứa `acnt_no` — đây là tài khoản phái sinh của nhà đầu tư
- ws-v2 chỉ đẩy event xuống **session đã đăng nhập với account tương ứng** (`accountNumber` match với JWT)
- **Tuyệt đối không** broadcast event của account A sang session của account B

#### 4.1.7 Sequence number monitoring (khuyến nghị)

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

### 5.2 Payload — TradeX WS Event (**đồng nhất pattern REST history**)

Tên field và giá trị enum **`orderStatus`**, **`operation`** phải **cùng convention** với response `GET .../derivatives/order/history` để FE tái dùng mapper hiện có — không dùng `orderId` / `side` / `status` / `lifecycleAction` trên kênh TradeX này.

```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000123",
  "eventTime": "20260511 14:48:22",
  "accountNumber": "039C200327",
  "orderNumber": "1000007",
  "symbolCode": "41I1G3000",
  "sellBuyType": "BUY",
  "orderType": "LO",
  "price": 1280.0,
  "quantity": 10,
  "orderStatus": "FILLED",
  "matchedQuantity": 5,
  "remainingQuantity": 5,
  "matchedPrice": 1279.5,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

- **`eventTime`:** định dạng **`yyyymmdd HH:mm:ss`** như ví dụ (hoặc format đã thống nhất với FE — ghi trong ticket triển khai).
- **`orderNumber`:** string — khớp `orderNumber` REST (Lotte `evt_ordNo`).
- **`sellBuyType`:** `BUY` | `SELL` — khớp history (Lotte `evt_side`, §8.3).
- **`orderStatus`:** enum TradeX — map từ Lotte **`evt_status`** — §8.1.
- **`operation`:** `NEW_ORDER` | `MODIFY_ORDER` | `CANCEL_ORDER` — map từ Lotte **`evt_action`** (mã `1`/`2`/`3`) — §8.4. **Không** lấy `operation` từ `evt_status`.
- **`originalOrderNumber`:** `null` hoặc string; Lotte `evt_orgOrdNo` = `"0"` → `null`.

> **Tính nhất quán nội dung:** BE phải đảm bảo **`orderStatus`** thống nhất với **`matchedQuantity`** / **`remainingQuantity`** (ví dụ `FILLED` ↔ `remainingQuantity === 0` khi đã khớp hết). Ví dụ JSON phía trên minh họa **cấu trúc field**; nếu `orderStatus` là `PARTIALLY_FILLED` thì thường `remainingQuantity > 0`.

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
| Mount màn **lệnh thường phái sinh** (Today Unmatch / History trong ngày — có gọi REST §2.3) | Subscribe `order.status.{accountNumber}` |
| Unmount màn | Unsubscribe |
| App từ background → foreground | Gọi lại REST snapshot §2.3 → re-subscribe |
| WS reconnect thành công | Gọi lại REST snapshot §2.3 → re-subscribe |
| Đăng xuất | Unsubscribe tất cả channel order.status |

### 6.2 Xử lý event nhận được

```
Nhận message từ channel order.status.{accountNumber}
    │
    ├─ Parse JSON
    ├─ Kiểm tra eventType === "ORDER_STATUS"?
    │       └─ Không? → Bỏ qua (guard cho tương lai)
    │
    ├─ Lấy orderNumber (và originalOrderNumber nếu có) từ payload
    ├─ Tìm dòng trong state đã load từ todayUnmatch và/hoặc history:
    │       REST orderNumber === WS orderNumber (normalize kiểu string)
    │       hoặc (optional) originalOrderNumber === WS originalOrderNumber theo rule BA
    │
    ├─ Tìm thấy → Cập nhật các trường §6.7:
    │       orderStatus, operation, matchedQuantity, remainingQuantity,
    │       matchedPrice, price, sellBuyType, orderType, …
    │
    └─ Không tìm thấy → Không tự thêm mới; gọi REST snapshot §2.3 (silent refresh)
```

> **Không tự thêm dòng mới vào state khi chỉ có WS event.** Luôn dùng REST snapshot §2.3 để có danh sách ban đầu. **Hai collection** (unmatch + history) có thể cùng chứa một lệnh ở giai đoạn khác — policy “xóa khỏi unmatch khi hết điều kiện” nên dựa trên **`orderStatus` + `remainingQuantity`** sau merge, sau đó đồng bộ với kết quả REST khi refresh.

**Đối chiếu mục tiêu cập nhật với REST** — chi tiết §6.7.

| Cập nhật từ WS | todayUnmatch | history |
|----------------|-------------|---------|
| `matchedQuantity` | ✓ §6.7 | ✓ §6.7 |
| `remainingQuantity` | → `unmatchedQuantity` §6.7 | → `unmatchedQuantity` §6.7 |
| `matchedPrice` | Theo UI | ✓ §6.7 |
| `orderStatus` | Map vào text `status` (§6.7) | `orderStatus` — **cùng enum** §8.1 |
| `operation` | Đồng bộ nhãn với history | `operation` §6.7 |
| Rule nút Hủy/Sửa | §6.8 | §6.8 |

### 6.3 Cập nhật state theo `orderStatus`

Payload WS dùng **`orderStatus`** (đồng nhất REST history). FE merge vào row state và format UI theo mapper hiện có.

| `orderStatus` nhận được | Hành động trong state | Gợi ý UI |
|-------------------------|-------------------------|-----------|
| `RECEIVED` | Cập nhật row theo payload | Tiếp nhận |
| `CONFIRMED` | Cập nhật row theo payload | Đã xác nhận |
| `PARTIALLY_FILLED` | Cập nhật KL + giá theo payload | Khớp một phần (§6.4) |
| `FILLED` | Cập nhật KL + giá; thường `remainingQuantity === 0` | Khớp hết |
| `REJECTED` | Cập nhật `orderStatus`; hiển thị lý do nếu có từ REST sau refresh | Từ chối |

### 6.4 Logic khớp một phần vs khớp toàn bộ

```
orderStatus === "PARTIALLY_FILLED"
    │  → remainingQuantity > 0 (thông thường)
orderStatus === "FILLED"
    │  → remainingQuantity === 0 (thông thường); đối chiếu matchedQuantity / quantity
```

### 6.5 Deduplicate

- Nếu nhận nhiều event cho cùng **`orderNumber`**: dùng `seqNo` để chỉ apply event **có seqNo lớn hơn** (tránh apply event cũ sau event mới do network delay)
- Nếu `seqNo` không available hoặc không parseable: apply event mới nhất theo thứ tự nhận

### 6.6 UI feedback

| Trường hợp | Phản hồi UI |
|------------|------------|
| Status thay đổi (bất kỳ) | Highlight nhẹ dòng lệnh (animation 0.5s) |
| `PARTIALLY_FILLED` / `FILLED` | Badge xanh / toast nếu app đang background |
| `REJECTED` / `operation === CANCEL_ORDER` | Badge đỏ / trạng thái hủy |
| WS mất kết nối | Hiển thị indicator "Đang kết nối lại..." |
| WS reconnect xong | Tắt indicator, refresh silently |

### 6.7 Đồng bộ field sau WebSocket (render lại UI)

Sau khi merge payload WS vào state (§6.2), FE **render lại UI** — field WS đã **cùng tên / cùng enum** với history nơi có thể (`orderStatus`, `operation`, …).

**API `GET /rest/api/v1/derivatives/order/history` — cập nhật các field:**

| Field REST | Nguồn từ WS |
|------------|-------------|
| `operation` | **`operation`** trực tiếp (`NEW_ORDER` / `MODIFY_ORDER` / `CANCEL_ORDER`). |
| `orderStatus` | **`orderStatus`** trực tiếp (§8.1). |
| `matchedPrice` | `matchedPrice`. |
| `matchedQuantity` | `matchedQuantity`. |
| `unmatchedQuantity` | `remainingQuantity`. |

**API `GET /api/v1/derivatives/order/todayUnmatch` — cập nhật các field:**

| Field REST | Nguồn từ WS |
|------------|-------------|
| `matchedQuantity` | `matchedQuantity`. |
| `unmatchedQuantity` | `remainingQuantity`. |

**Đồng nhất `status` (todayUnmatch) và `operation` / `orderStatus` (history)**

- Field **`status`** trên todayUnmatch (text hiển thị) và **`operation`** + **`orderStatus`** trên history **mô tả cùng một lệnh** — sau WS, FE map **`orderStatus`** / **`operation`** vào đúng rule format **`status`** text trên unmatch (mapper hiện có), để hai danh sách không lệch.

### 6.8 Logic enable nút **Hủy** / **Sửa**

Áp dụng **sau** mỗi lần merge WS (và khi render từ REST snapshot). Thứ tự ưu tiên: **rule chung** → **rule theo loại lệnh** (`orderType`).

#### Rule chung (mọi loại lệnh)

| Điều kiện (sau merge / từ state) | Hủy / Sửa |
|----------------------------------|-----------|
| **Đã khớp hết** (`orderStatus === FILLED` và/hoặc `remainingQuantity === 0` theo §6.4) | **Không** enable |
| **Đã hủy / từ chối** (`operation === CANCEL_ORDER` hoặc `orderStatus === REJECTED`, hoặc rule UI tương đương) | **Không** enable |

#### Rule theo loại lệnh (TradeX `orderType`)

Các rule dưới **chỉ áp dụng khi rule chung chưa tắt nút** (ví dụ vẫn còn KL chưa khớp và lệnh chưa ở trạng thái hủy).

| `orderType` | Điều kiện | Hủy / Sửa |
|-------------|-----------|-----------|
| **MOK** | **Chưa** nhận được (qua WS/state sau merge) **tín hiệu xác định khớp toàn bộ** theo nghiệp vụ MOK | **Không** enable |
| **MAK** | **Khớp một phần** (`matchedQuantity > 0` và `remainingQuantity > 0`) **hoặc** **đã khớp hết** | **Không** enable |
| **MTL** | **Chỉ khớp một phần** (`matchedQuantity > 0` và `remainingQuantity > 0`) | **Vẫn enable** (trừ khi rule chung đã tắt nút) |

> **Ghi chú MOK:** Nếu literal “chưa nhận tín hiệu khớp toàn bộ → không enable” kết hợp với rule chung “đã khớp hết → không enable” làm **không còn cửa sổ** bật nút, **BA/BE cần xác nhận** lại nghiệp vụ (ví dụ MOK có cho hủy khi đang chờ kết quả hay chỉ sau Or Kill). FE implement đúng bảng trên cho đến khi có điều chỉnh BA.

> **Ghi chú MAK:** Chỉ trong các trạng thái **chưa phát sinh khớp** (`matchedQuantity === 0`) và chưa vi phạm rule chung, nút mới có thể enable — hoặc theo đúng bảng: có khớp (dù một phần) hoặc khớp hết thì **không** enable.

**Các loại lệnh khác (LO, ATO, ATC, …)** không có rule riêng trong tài liệu này — chỉ áp **rule chung** §6.8 và các quy tắc UI/REST hiện hành của app.

---

## 7. Field Mapping

### 7.1 Lotte F15303 → TradeX WS Payload (đầy đủ)

| # | Lotte Field | Lotte Type | TradeX WS Field | TradeX Type | Transform | Ghi chú |
|---|-------------|------------|-----------------|-------------|-----------|---------|
| 1 | `event_code` | String | `eventType` | String | Fixed: `"ORDER_STATUS"` | Không expose raw `F15303` ra FE |
| 2 | `event_seqno` | String | `seqNo` | String | Pass-through | Dùng để dedupe |
| 3 | `date` + `evt_time` | String | `eventTime` | String (ISO 8601) | Ghép và format: `yyyymmdd` + `HH:mm:ss` → `yyyy-MM-ddTHH:mm:ss+07:00` | |
| 4 | `acnt_no` | String | `accountNumber` | String | Pass-through | Route key |
| 5 | `evt_ordNo` | String | `orderNumber` | String | Pass-through | **Khóa merge FE** — cùng REST |
| 6 | `evt_code` | String | `symbolCode` | String | Pass-through | |
| 7 | `evt_side` | String | `sellBuyType` | Enum String | `"1"`/`"2"` / `"Mua"`/`"Bán"` → `"BUY"`/`"SELL"` | §8.3 |
| 8 | `evt_action` | String | `operation` | Enum String | `1`/`2`/`3` → `NEW_ORDER`/`MODIFY_ORDER`/`CANCEL_ORDER` — §8.4 |
| 9 | `evt_ordType` | String | `orderType` | Enum String | Xem §8.2 | |
| 10 | `evt_price` | String | `price` | Number/null | Parse to Number | |
| 11 | `evt_qty` | String | `quantity` | Number | Parse to Number | |
| 12 | `evt_status` | String | `orderStatus` | Enum String | §8.1 | |
| 13 | `evt_matchQty` | String | `matchedQuantity` | Number | Parse to Number | |
| 14 | `evt_remQty` | String | `remainingQuantity` | Number | Parse to Number | |
| 15 | `evt_matchPrice` | String | `matchedPrice` | Number/null | §4.1.4 nếu `orderType ≠ LO` | |
| 16 | `evt_orgOrdNo` | String | `originalOrderNumber` | String/null | `"0"` → `null` | REST history |

### 7.2 TradeX WS → cột cập nhật trên REST (lệnh thường)

Dùng cho FE sau khi đã map BE (§4) — **một dòng** trong state tương ứng một bản ghi `orderNumber` trên API. **Set field bắt buộc refresh sau WS** đã chốt tại **§6.7**.

| TradeX WS (§5.2) | `todayUnmatch` | `history` |
|------------------|----------------|---------|
| `orderNumber` | Key merge | Key merge |
| `originalOrderNumber` | Theo BA | `originalOrderNumber` |
| `orderStatus` | Map → text `status` §6.7 | `orderStatus` (cùng enum §8.1) |
| `operation` | Map → nhãn / `status` §6.7 | `operation` |
| `matchedQuantity` | `matchedQuantity` | `matchedQuantity` |
| `remainingQuantity` | `unmatchedQuantity` | `unmatchedQuantity` |
| `matchedPrice` | Theo UI | `matchedPrice` |
| `price` | `orderPrice` | `orderPrice` |
| `sellBuyType` | `sellBuyType` | `sellBuyType` |
| `orderType` | Rule nút §6.8 | Rule nút §6.8 |

---

## 8. Status & Code Mapping

### 8.1 Lotte `evt_status` → TradeX **`orderStatus`** (WebSocket & REST history)

Enum **`orderStatus`** trên payload TradeX WebSocket **phải trùng tập giá trị** với field `orderStatus` trên `GET .../derivatives/order/history` để FE dùng chung formatter.

| Lotte `evt_status` | TradeX `orderStatus` | Ghi chú |
|--------------------|------------------------|---------|
| `0` | `RECEIVED` | Tiếp nhận |
| `3` | `CONFIRMED` | Đã xác nhận (đồng nhất app/history) |
| `4` | `FILLED` | Khớp toàn bộ |
| `5` | `PARTIALLY_FILLED` | Khớp một phần |
| `R` hoặc `X` | `REJECTED` | Từ chối (CTCK / Sở — không tách enum trên WS nếu history gộp một `REJECTED`) |

**Mã Lotte `1`, `2` (§3.4 — Chuyển / Xác nhận lệnh):** BE map sang **`CONFIRMED`** hoặc **`RECEIVED`** theo rule đã dùng khi build response history — **thống nhất một đường** trong collector + REST.

> **Tham chiếu ý nghĩa gốc Core:** bảng mô tả Lotte trong §3.4 vẫn dùng cho hiểu nghiệp vụ; **emit ra TradeX** luôn qua bảng trên để khớp FE.

### 8.2 Order Type: Lotte `evt_ordType` → TradeX `orderType`

| Lotte `evt_ordType` | TradeX `orderType` | Mô tả |
|---------------------|-------------------|-------|
| `0` | `LO` | Lệnh giới hạn |
| `2` | `ATO` | Lệnh mở cửa |
| `3` | `MAK` | Market At Kill |
| `4` | `MOK` | Market Or Kill |
| `7` | `ATC` | Lệnh đóng cửa |
| `9` | `MTL` | Market To Limit |

### 8.3 Chiều lệnh: Lotte `evt_side` → TradeX `sellBuyType`

Payload WS dùng **`sellBuyType`** (cùng REST history), không dùng tên `side`.

| Lotte `evt_side` | TradeX `sellBuyType` |
|--------------------|------------------------|
| `1` | `BUY` |
| `2` | `SELL` |
| `Mua` | `BUY` |
| `Bán` | `SELL` |

### 8.4 Loại thao tác: Lotte **`evt_action`** → TradeX **`operation`**

**Nguồn Lotte là `evt_action`** (không phải `evt_status`). Trong tài liệu góp ý, nếu ghi nhầm `operation` ← `evt_status` thì **bỏ qua** — chỉ dùng bảng dưới. Giá trị WS **trùng** field `operation` trên history.

| Lotte `evt_action` | TradeX `operation` |
|--------------------|---------------------|
| `1` | `NEW_ORDER` |
| `2` | `MODIFY_ORDER` |
| `3` | `CANCEL_ORDER` |

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
  "evt_action": "1",
  "evt_status": "4",
  "evt_matchQty": "10",
  "evt_remQty": "0",
  "evt_matchPrice": "1279.5",
  "evt_orgOrdNo": "0"
}
```

**TradeX WS payload → FE nhận được** (đồng nhất §5.2):

```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000123",
  "eventTime": "20260511 14:48:22",
  "accountNumber": "0001234567",
  "orderNumber": "20260511000456",
  "symbolCode": "VN30F2506",
  "sellBuyType": "BUY",
  "orderType": "LO",
  "price": 1280.0,
  "quantity": 10,
  "orderStatus": "FILLED",
  "matchedQuantity": 10,
  "remainingQuantity": 0,
  "matchedPrice": 1279.5,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

**FE action:** Tìm `orderNumber` trong state → cập nhật **`orderStatus`**, KL, giá — **cùng mapper** REST history → Highlight dòng.

---

### 9.2 Lệnh khớp một phần

**TradeX WS payload:**
```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000124",
  "eventTime": "20260511 14:49:05",
  "accountNumber": "0001234567",
  "orderNumber": "20260511000457",
  "symbolCode": "VN30F2506",
  "sellBuyType": "SELL",
  "orderType": "LO",
  "price": 1285.0,
  "quantity": 20,
  "orderStatus": "PARTIALLY_FILLED",
  "matchedQuantity": 8,
  "remainingQuantity": 12,
  "matchedPrice": 1285.0,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

**FE action:** `orderStatus = PARTIALLY_FILLED` → hiển thị khớp một phần (8/20). Chờ event tiếp.

---

### 9.3 Lệnh hủy (thao tác CANCEL)

**TradeX WS payload** (`orderStatus` sau hủy **đồng nhất REST history** — ví dụ minh họa):

```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000125",
  "eventTime": "20260511 15:01:30",
  "accountNumber": "0001234567",
  "orderNumber": "20260511000457",
  "symbolCode": "VN30F2506",
  "sellBuyType": "SELL",
  "orderType": "LO",
  "price": 1285.0,
  "quantity": 20,
  "orderStatus": "CONFIRMED",
  "matchedQuantity": 8,
  "remainingQuantity": 0,
  "matchedPrice": null,
  "operation": "CANCEL_ORDER",
  "originalOrderNumber": null
}
```

**FE action:** `operation = CANCEL_ORDER` → cập nhật UI / rule nút §6.8.

---

### 9.4 MTL — khớp một phần: hai event liên tiếp từ Core (F15303)

Core gửi **cùng `evt_ordNo`**, `evt_status = 5` → TradeX `orderStatus = PARTIALLY_FILLED` (§8.1).

**TradeX BE (§4.1.4):** sau event 1, lưu `lastMatchPriceByOrderNumber["10000913"] = 2265.8`. Sau event 2, emit `matchedPrice: 2265.8` (không phát `0`).

**TradeX WS → FE** (bổ sung đủ field §5.2; rút gọn trích dẫn):

```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "943174",
  "eventTime": "20251127 13:28:52",
  "accountNumber": "039C110257",
  "orderNumber": "10000913",
  "symbolCode": "41I16600",
  "sellBuyType": "BUY",
  "orderType": "MTL",
  "price": 0,
  "quantity": 3,
  "orderStatus": "PARTIALLY_FILLED",
  "matchedQuantity": 1,
  "remainingQuantity": 2,
  "matchedPrice": 2265.8,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "943175",
  "eventTime": "20251127 13:28:52",
  "accountNumber": "039C110257",
  "orderNumber": "10000913",
  "symbolCode": "41I16600",
  "sellBuyType": "BUY",
  "orderType": "MTL",
  "price": 2265.9,
  "quantity": 3,
  "orderStatus": "PARTIALLY_FILLED",
  "matchedQuantity": 1,
  "remainingQuantity": 2,
  "matchedPrice": 2265.8,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

**FE action:** Merge theo **`orderNumber`**; `seqNo` tăng dần. Giữ **giá khớp 2265.8** đến khi có event đổi giá có nghiệp vụ hoặc refresh REST.

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
| Reconnect thành công | Gọi REST snapshot §2.3 để lấy snapshot mới; re-subscribe channel; tắt indicator |
| App ra background | Giữ state; khi vào foreground: gọi REST snapshot §2.3 → re-subscribe |
| Timeout reconnect (> 30s) | Hiển thị thông báo "Không có kết nối — Kéo để làm mới" |

### 10.3 FE — Event không tìm thấy `orderNumber`

| Tình huống | Hành động FE |
|------------|-------------|
| `orderNumber` trong payload WS không có trong state | Không tự thêm dòng; gọi REST snapshot §2.3 (silent refresh) |
| Event trùng seqNo | Bỏ qua event trùng |
| Event seqNo nhỏ hơn seqNo đã apply | Bỏ qua (stale event) |

---

## 11. Testing Checklist

### 11.1 BE

- [ ] Subscribe `sub/bos.evt.ord.sts.*/` thành công sau khi kết nối Lotte WS
- [ ] Chỉ forward event có `event_code === "F15303"` — bỏ qua F15302 và các mã khác
- [ ] Normalize đúng tất cả fields theo §7.1
- [ ] Map đúng `evt_status` → **`orderStatus`** — §8.1 (mã Lotte `1`/`2` → rule đã thống nhất với REST history; legacy chữ `A`/`B`/… nếu Core còn gửi)
- [ ] Với lệnh ≠ LO: áp dụng logic `matchedPrice` §4.1.4 (hai bước `evt_price`/`evt_matchPrice`)
- [ ] Map đúng `evt_action` → **`operation`** — §8.4
- [ ] Map đúng `evt_ordType` 0/2/3/4/7/9 → TradeX enum §8.2
- [ ] Publish đúng Kafka topic `order-status-events` với key = `accountNumber`
- [ ] ws-v2 chỉ push event xuống session có `accountNumber` khớp (kiểm tra bảo mật)
- [ ] Reconnect Lotte WS khi bị ngắt — re-subscribe sau khi kết nối lại
- [ ] Log WARNING khi phát hiện gap sequence

### 11.2 FE

- [ ] Subscribe `order.status.{accountNumber}` khi mount màn sổ lệnh
- [ ] Unsubscribe khi unmount màn sổ lệnh
- [ ] Parse payload đúng và lọc `eventType === "ORDER_STATUS"`
- [ ] Merge đúng theo **`orderNumber`** — không tạo dòng mới khi chưa có trong state
- [ ] Deduplicate theo `seqNo` — bỏ qua event cũ hơn
- [ ] Sau mỗi event WS: render lại đúng field §6.7 — **history:** `operation`, `orderStatus`, `matchedPrice`, `matchedQuantity`, `unmatchedQuantity`; **todayUnmatch:** `matchedQuantity`, `unmatchedQuantity`
- [ ] Payload WS dùng **`orderStatus`** + **`operation`** (§5.2); đồng bộ text `status` trên todayUnmatch theo mapper hiện có (§6.7)
- [ ] Áp rule nút Hủy/Sửa §6.8 (chung + MOK / MAK / MTL); đặc biệt MTL khớp một phần vẫn enable; MAK có khớp (một phần hoặc hết) thì không enable
- [ ] Cập nhật đúng `orderStatus` (§6.3: `RECEIVED`, `CONFIRMED`, `PARTIALLY_FILLED`, `FILLED`, `REJECTED`), `matchedQuantity`, `remainingQuantity`, `matchedPrice`
- [ ] Với lệnh ≠ LO: tin `matchedPrice` từ BE sau §4.1.4; kiểm tra scenario §9.4 (MTL hai event)
- [ ] Hiển thị khớp một phần khi `orderStatus = PARTIALLY_FILLED` (và/hoặc `remainingQuantity > 0`)
- [ ] Gọi REST snapshot §2.3 khi: reconnect WS, app foreground, `orderNumber` không tìm thấy
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
3. Confirm payload WS **`orderNumber`** = REST **`orderNumber`**; **`originalOrderNumber`** khi có  
4. Confirm string **`orderStatus`** / **`operation`** trên WS **trùng** contract REST history (§8.1, §8.4)  
5. Sign-off → đưa vào sprint planning
