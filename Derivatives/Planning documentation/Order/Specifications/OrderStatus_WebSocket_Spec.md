# Order Status WebSocket Specification (Derivatives)

**Document Type:** Technical Specification — WebSocket  
**Category:** Derivatives Orders — Real-time Order Status  
**Project:** TradeX Derivatives Integration  
**Date:** 2026-05-11  
**Version:** 1.7  
**Author:** BA/PM Team

> **Tham chiếu Lotte:** `Derivatives/Documentation/Lotte_DR_API_Specs.md` — §3.1.1 Order events, event_code `F15303`  
> **Tham chiếu WS pattern:** `Knowledge/TradeX/System/market-data-channels.md`

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture & Data Flow](#2-architecture--data-flow) *(§2.3: Order Book — `history` + `todayUnmatch`)*
3. [Lotte Source — Event F15303](#3-lotte-source--event-f15303)
4. [BE Specification](#4-be-specification)
5. [WebSocket Contract (Channel & Payload)](#5-websocket-contract-channel--payload) *(§5.3 FE login WS; §5.4 subscribe)*
6. [FE Specification](#6-fe-specification) *(§5.3 login WS; §6.0 Order Book; §6.7 mapping; §6.8 nút Sửa/Hủy)*
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

App **subscribe WebSocket channel** dành riêng cho tài khoản phái sinh. Khi có thay đổi trạng thái lệnh, BE **đẩy sự kiện** xuống app ngay lập tức. FE **cập nhật hoặc thêm dòng** trên **màn Order Book** (cả tab **Open** và **All**) theo **`orderNumber`** — **không** yêu cầu gọi lại `GET .../order/history` sau mỗi event để thấy lệnh mới (chi tiết §6.0, §6.2). Vẫn dùng REST làm snapshot khi vào màn / reconnect / background (§2.2).

### 1.3 Phạm vi

| Hạng mục | Mô tả |
|----------|-------|
| **Loại sự kiện** | Chỉ F15303 (trạng thái sổ lệnh) — xem §3 |
| **Loại lệnh** | Tất cả lệnh phái sinh: Regular Orders (LO, ATO, ATC, MOK, MAK, MTL) |
| **Màn hình FE** | **Order Book** (phái sinh) — **hai tab Open & All**; **không** còn màn **Order History** riêng; dữ liệu danh sách lệnh lấy từ REST history trong ngày + WS (§2.3, §6.0) |
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
    │ Channel: order.status.{username}
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
│  - Route event → channel: order.status.{username}               │
│  - Push to all active sessions subscribed to that channel        │
└──────────────────────────┬──────────────────────────────────────┘
                           │ WebSocket (SocketCluster protocol)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NHSV Pro App  (FE)                            │
│                                                                  │
│  - connect → login (access_token, domain nhsv) → subscribe       │
│  - Channel: order.status.{username} (§5.3–§5.4)                  │
│  - On event: merge / upsert by orderNumber → Order Book UI       │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Quan hệ với REST API

| Action | Cơ chế | Lý do |
|--------|--------|-------|
| **Vào màn lệnh / sau login** | Gọi REST snapshot theo §2.3 (`todayUnmatch` + `history` trong ngày) | Lấy snapshot đầy đủ cho hai danh sách app đang dùng |
| **Đang xem Order Book** | WS → merge/upsert state → **re-render** tab Open & All | **Không bắt buộc** gọi lại `history` sau mỗi event; lệnh **`orderNumber` mới** hiển thị ngay từ payload WS (§6.0, §6.2) |
| **Reconnect WS** | Gọi lại REST snapshot §2.3 | Đồng bộ trạng thái bị miss |
| **App từ background lên** | Gọi lại REST snapshot §2.3 | Đảm bảo không lệch state |

> **Nguyên tắc:** WS là **incremental update**, REST là **snapshot source of truth**. Không thể dùng WS thay thế hoàn toàn REST.

### 2.3 REST snapshot + Order Book (Open & All) — **bỏ màn Order History**

**Thay đổi sản phẩm:** FE **chỉ** hiển thị luồng lệnh real-time trên **màn Order Book**; **gỡ màn Order History** (màn hình riêng). Order Book có **hai tab: Open** và **All** — **cả hai** dùng chung nguồn hiển thị danh sách lệnh từ API history trong ngày; API `todayUnmatch` chỉ phục vụ **logic nút Sửa / Hủy** (NHSV Pro).

| API | Vai trò | Tab / màn |
|-----|---------|-----------|
| `GET /rest/api/v1/derivatives/order/history` | Dữ liệu **hiển thị** từng dòng lệnh (đủ field như `orderStatus`, `operation`, `matchedQuantity`, …) | **Open** và **All** |
| `GET /api/v1/derivatives/order/todayUnmatch` | Danh sách `orderNumber` lệnh **được phép** Sửa/Hủy (chưa khớp hết / còn hiệu lực theo rule backend) | Chỉ dùng để **so khớp** với từng dòng history — quyết định nút active vs disabled (§6.8.1) |

Tham số history (giữ nguyên app hiện tại): `fromDate = toDate = today`, `orderType = ALL`, `matchType = ALL`.

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

**Mục tiêu WebSocket (Order Book)**

- **Cập nhật dòng đã có:** Nếu `orderNumber` trong event WS **đã có** trong state (từ lần gọi `history` / `todayUnmatch` trước đó), FE **ghi đè** các field hiển thị bằng giá trị mới nhất từ WS (§6.7).
- **Lệnh mới (chưa có trong state):** Nếu `orderNumber` **chưa tồn tại**, FE **thêm dòng mới** vào danh sách Order Book **trực tiếp từ payload WS** — **không** gọi lại `GET .../order/history` chỉ để lấy dòng mới đó (§6.2). Các field thiếu so với model history (VD `orderTime`, `nextKey`) có thể để default / null cho đến lần snapshot REST tiếp theo (vào màn, reconnect, background).
- **`originalOrderNumber`:** Nếu WS có `originalOrderNumber`, FE **cập nhật trạng thái** cho **cả dòng lệnh gốc** (cùng chuỗi sửa/hủy) theo rule BA/FE hiện có.
- Sau merge: **re-render** tab đang mở; áp **§6.8** cho nút Sửa/Hủy.

> **Lưu ý:** `todayUnmatch` **không** tự cập nhật sau mỗi tick WS trừ khi app chủ động refresh API đó hoặc BE bổ sung cơ chế đẩy — rule nút §6.8.1 vẫn dựa trên **snapshot `todayUnmatch` mới nhất** trong state; nếu lệnh mới chỉ xuất hiện qua WS và **chưa** nằm trong `todayUnmatch`, nút Sửa/Hủy **disabled** theo so khớp `orderNumber`.

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

> **Ghi chú tài liệu hóa:** Bảng mapping TradeX **`orderStatus`** (§8.1) dùng các mã trên. Nếu gặp mã legacy dạng chữ (`A`/`B`/…) từ nguồn khác, cần bảng map riêng hoặc xác nhận lại với Core — không trộn với bảng `0`–`5`/`R`/`X` mà không có quyết định BA.

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
| `evt_price` | `orderPrice` | **§4.1.3.1** — phụ thuộc `orderType` sau map |
| `evt_qty` | `orderQuantity` | String → Number |
| `evt_status` | `orderStatus` | Map Lotte → enum TradeX **đồng nhất history** — §8.1 |
| `evt_matchQty` | `matchedQuantity` | String → Number |
| `evt_remQty` | `remainingQuantity` | String → Number |
| `evt_matchPrice` | `matchedPrice` | Áp dụng §4.1.4 (lệnh ≠ LO) trước khi parse |
| `evt_orgOrdNo` | `originalOrderNumber` | `"0"` / rỗng → `null`; khác → string — khớp REST history |

#### 4.1.3.1 Giá đặt (`orderPrice`) — xử lý bắt buộc TradeX BE

Sau khi map Lotte `evt_ordType` → TradeX **`orderType`** (§8.2), BE set **`orderPrice`** trên payload WebSocket như sau:

| Điều kiện | `orderPrice` emit ra WS | Kiểu JSON | Ghi chú |
|------------|-------------------------|------------|---------|
| `orderType === "LO"` | Parse `evt_price` (string Lotte) → **số** | **Number** | Giá giới hạn; `evt_price` rỗng / không parse được → `null` hoặc `0` theo rule đã thống nhất với REST history |
| `orderType !== "LO"` | **Cùng giá trị hiển thị với loại lệnh** — đặt `orderPrice` **bằng chính** `orderType` | **String** | VD: `MAK`, `MOK`, `MTL`, `ATO`, `ATC`. **Không** emit số từ `evt_price` cho cột “giá đặt” trên kênh WS (giá thị trường / bước chuyển vẫn xử lý qua `matchedPrice` và §4.1.4). |

**Ví dụ (MAK):** `"orderType": "MAK"`, `"orderPrice": "MAK"` — sample §9.5.

> **FE:** cột giá đặt — nếu `orderPrice` là **number** thì format theo quy tắc giá; nếu là **string** (trùng mã loại lệnh) thì **hiển thị nguyên chuỗi** (VD `MAK`, `MTL`), không parse thành số.

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
| Channel naming | `order.status.{username}` — `{username}` = mã TK phái sinh (thường trùng `acnt_no` Lotte / `accountNumber` trong payload) |
| Auth check | Chỉ socket đã **login** (§5.3) mới được subscribe channel; verify user có quyền với `{username}` trong tên channel |
| Push | Đẩy TradeX WS payload xuống tất cả client đang subscribe `order.status.{username}` tương ứng |

---

## 5. WebSocket Contract (Channel & Payload)

### 5.1 Channel

```
order.status.{username}

Ví dụ: order.status.039C110257
```

| Placeholder | Ý nghĩa |
|-------------|---------|
| `{username}` | Mã **tài khoản phái sinh** user đang đăng nhập / đang xem Order Book trên app (NHSV Pro). Thường **cùng giá trị** với `accountNumber` trong payload event và query REST — nhưng **tên channel** dùng suffix `username` theo quy ước ws-v2. |

> **BE publish:** route event tới `order.status.{username}` với `username` = `acnt_no` (sau normalize) của event F15303.

### 5.2 Payload — TradeX WS Event (**đồng nhất pattern REST history**)

Tên field và enum **`orderStatus`**, **`operation`**, **`orderQuantity`** phải **trùng** response `GET /rest/api/v1/derivatives/order/history` — không dùng `orderId` / `side` / `status` / `lifecycleAction` / `price` / `quantity` trên kênh TradeX này.

**`orderPrice` trên kênh WS:** với **`orderType === "LO"`** là **Number** (giá); với **`orderType !== "LO"`** là **String** trùng **`orderType`** (§4.1.3.1) — có thể khác kiểu/ý nghĩa so với một số bản ghi REST history (REST có thể vẫn là số); FE ưu tiên contract WS khi merge real-time.

**Ví dụ cấu trúc — LO (giá số):**

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
  "orderPrice": 1280.0,
  "orderQuantity": 10,
  "orderStatus": "PARTIALLY_FILLED",
  "matchedQuantity": 5,
  "remainingQuantity": 5,
  "matchedPrice": 1279.5,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

**Ví dụ cấu trúc — không LO (`orderPrice` = chuỗi loại lệnh):** xem §9.5.

- **`eventTime`:** định dạng **`yyyymmdd HH:mm:ss`** như ví dụ (hoặc format đã thống nhất với FE — ghi trong ticket triển khai).
- **`orderNumber`:** string — khớp `orderNumber` REST (Lotte `evt_ordNo`).
- **`sellBuyType`:** `BUY` | `SELL` — khớp history (Lotte `evt_side`, §8.3).
- **`orderStatus`:** enum TradeX — map từ Lotte **`evt_status`** — §8.1.
- **`operation`:** `NEW_ORDER` | `MODIFY_ORDER` | `CANCEL_ORDER` — map từ Lotte **`evt_action`** (mã `1`/`2`/`3`) — §8.4. **Không** lấy `operation` từ `evt_status`.
- **`originalOrderNumber`:** `null` hoặc string; Lotte `evt_orgOrdNo` = `"0"` → `null`.
- **`remainingQuantity`:** KL chưa khớp — map sang `unmatchedQuantity` trên REST history; field này **không** có tên tương ứng trong response history (FE suy ra từ WS).

> **Tính nhất quán nội dung:** BE đảm bảo **`orderStatus`** khớp **`matchedQuantity`** / **`remainingQuantity`** (ví dụ `FILLED` thường đi kèm `remainingQuantity === 0`). Ví dụ JSON minh họa **cấu trúc**; nếu `orderStatus` là `PARTIALLY_FILLED` thì thường `remainingQuantity > 0`.

### 5.3 Kết nối & xác thực WebSocket (FE — bắt buộc trước subscribe)

FE **không** được subscribe `order.status.{username}` trước khi socket đã **login** thành công trên ws-v2. Luồng chuẩn (SocketCluster — cùng server market data, ví dụ `config.apiUrl.domain.socketCluster`):

```
1. socketCluster.connect(...)
       │
       ▼ (event: connect)
2. socket.emit('login', { body: { ... }, headers?: { ... } })
       │  body.grant_type = 'access_token'
       │  body.access_token = <JWT access token từ session app>
       │  body.domain = 'nhsv'
       ▼
3. Login thành công
       │  (callback không lỗi và/hoặc event server 'loggedIn')
       ▼
4. Subscribe channel order.status.{username}
       │  username = mã TK phái sinh đang active (VD: 039C110257)
       ▼
5. Nhận event ORDER_STATUS trên channel → merge Order Book (§6.2)
```

**Payload login (logical — trường trong `body`):**

```json
{
  "grant_type": "access_token",
  "access_token": "<JWT_ACCESS_TOKEN>",
  "domain": "nhsv"
}
```

| Field | Bắt buộc | Mô tả |
|-------|----------|-------|
| `grant_type` | Có | Cố định `"access_token"` — ws-v2 xác thực JWT đã cấp sau login REST |
| `access_token` | Có | Access token hiện tại của user (cùng token dùng cho REST API) |
| `domain` | Có | Cố định `"nhsv"` — chọn JWT verification key trên ws-v2 |

**Gọi RPC (tham chiếu ws-v2):**

```javascript
socket.emit(
  'login',
  {
    body: {
      grant_type: 'access_token',
      access_token: accessToken,
      domain: 'nhsv',
    },
    headers: { /* Accept-Language, ... nếu app đang gửi cho WS */ },
  },
  (err, data) => {
    if (err) { /* xử lý UNAUTHORIZED / INVALID_TOKEN — không subscribe */ }
    else { /* tiếp tục subscribe order.status.{username} */ }
  }
);
```

**Sau login thành công:**

- Server ws-v2 gắn auth token cho socket (`setAuthToken`) và có thể emit `loggedIn`.
- FE **chỉ lúc này** mới `subscribe('order.status.{username}')`.
- Subscribe **trước** login → `subscribeFail` / `UNAUTHORIZED` (channel yêu cầu authenticated — `authenticatedChannels` trên ws-v2).

**Reconnect:** mỗi lần socket reconnect, session auth **mất** → lặp lại bước **connect → login → subscribe** (§10.2).

---

### 5.4 Subscribe / Unsubscribe (SocketCluster protocol)

**Điều kiện:** đã hoàn tất §5.3 (login thành công).

**Subscribe (khi vào màn Order Book):**

```javascript
const channelName = `order.status.${username}`; // VD: order.status.039C110257
const socketChannel = socket.subscribe(channelName);
socketChannel.watch((data) => { /* merge §6.2 */ });
```

Hoặc message-style (nếu wrapper app dùng):

```json
{
  "event": "#subscribe",
  "data": {
    "channel": "order.status.039C110257"
  }
}
```

**Unsubscribe (khi rời Order Book):**

```json
{
  "event": "#unsubscribe",
  "data": "order.status.039C110257"
}
```

> **Lưu ý:** Dùng **cùng** socketCluster instance / URL như market data; **khác** channel và **bắt buộc** login trước subscribe cho kênh `order.status.*`.

---

## 6. FE Specification

### 6.0 Phạm vi FE — **chỉ Order Book**; hiển thị & lệnh mới từ WebSocket

| Yêu cầu | Chi tiết |
|----------|----------|
| **Màn hình** | **Order Book** với hai tab **Open** và **All**. **Không** triển khai / duy trì màn **Order History** riêng cho luồng này. |
| **Vào Order Book** | Gọi **`GET /rest/api/v1/derivatives/order/history`** (hiển thị cho **cả** tab Open & All) **và** **`GET /api/v1/derivatives/order/todayUnmatch`** (phục vụ logic nút Sửa/Hủy — §6.8.1). |
| **WebSocket** | Connect → **login** §5.3 → subscribe `order.status.{username}` §5.4 khi vào Order Book. Mỗi event: **cập nhật** dòng đã có hoặc **insert** dòng mới — **không** bắt buộc gọi lại `history` để thấy `orderNumber` mới (§6.2). |
| **Tab Open vs All** | Lọc danh sách đã merge trên client theo rule sản phẩm (VD Open = lệnh còn mở); **cùng** nguồn state sau khi merge WS + snapshot REST ban đầu. |
| **REST sau event** | Chỉ khi: vào màn lần đầu, reconnect WS, app foreground, gap `seqNo`, hoặc payload thiếu field bắt buộc (§2.2, §10.3). |

### 6.1 Khi nào connect / login / subscribe / unsubscribe

| Thời điểm | Action |
|-----------|--------|
| Mount màn **Order Book** (sau REST §2.3) | Nếu socket chưa connect: `connect` → **`login`** §5.3 → **`subscribe`** `order.status.{username}` §5.4 |
| Unmount màn Order Book | **Unsubscribe** `order.status.{username}` (giữ socket market nếu dùng chung) |
| App từ background → foreground | REST snapshot §2.3 → nếu WS đã mất auth: **login** lại → **re-subscribe** |
| WS reconnect (`connect` lại) | **Login** lại (§5.3) → REST snapshot §2.3 → **re-subscribe** `order.status.{username}` |
| Đăng xuất | Unsubscribe `order.status.*`; không gửi login với token cũ |

**`username` cho channel:** mã tài khoản phái sinh đang active trên Order Book (ví dụ `039C110257`). Thường trùng `accountNumber` trong query REST / field `accountNumber` trên payload WS.

### 6.2 Xử lý event nhận được

```
Nhận message từ channel order.status.{username}
    │
    ├─ Parse JSON; eventType === "ORDER_STATUS"?
    │
    ├─ Lấy orderNumber, originalOrderNumber (nếu có)
    │
    ├─ orderNumber đã có trong state Order Book (từ history snapshot hoặc upsert trước)?
    │       ├─ Có → merge field §6.7 vào dòng đó (Open & All cùng state)
    │       └─ Không → INSERT dòng mới từ payload WS (shape giống item history) — không gọi lại GET history chỉ cho việc này
    │
    ├─ originalOrderNumber có giá trị? → Cập nhật thêm dòng lệnh gốc (cùng chuỗi) theo rule FE
    │
    ├─ Re-render Order Book; áp §6.8 (nút Sửa/Hủy)
    └─ (Tùy chọn) Refresh GET todayUnmatch nếu product muốn đồng bộ danh sách được phép Sửa/Hủy sát thời gian thực — không thuộc acceptance tối thiểu của spec WS
```

> **Hai tab Open & All:** cùng một cấu trúc state sau merge; chỉ khác **filter** hiển thị.

**Đối chiếu nhanh với REST** — chi tiết bảng §6.7.

| Cập nhật từ WS | Ghi chú |
|----------------|---------|
| `matchedQuantity`, `remainingQuantity`, `matchedPrice`, `orderStatus`, `operation`, `orderPrice`, `orderQuantity`, … | Ghi đè / điền vào model dòng Order Book |
| Rule nút | §6.8 |

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
    │  → remainingQuantity === 0 (thông thường); đối chiếu matchedQuantity / orderQuantity
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
| WS reconnect xong | Tắt indicator → **gọi REST snapshot §2.3** → re-subscribe; sau đó Order Book cập nhật chủ yếu từ WS (§6.0), **không** refetch full `history` sau mỗi event |

### 6.7 Mapping WebSocket ↔ `GET /rest/api/v1/derivatives/order/history`

Sau merge (§6.2), FE bind **trực tiếp** các cột dưới đây từ payload WS sang model giống item history (Order Book — tab Open & All).

| Websocket (TradeX) | API `history` | Ghi chú |
|--------------------|---------------|---------|
| `eventType` | N/A | Lọc `ORDER_STATUS` |
| `seqNo` | N/A | Dedupe §6.5 |
| `accountNumber` | N/A | Route / JWT |
| `orderNumber` | `orderNumber` | Key merge / upsert |
| `symbolCode` | `symbolCode` | |
| `sellBuyType` | `sellBuyType` | |
| `orderType` | `orderType` | |
| `orderPrice` | `orderPrice` | **WS:** `Number` nếu `orderType === "LO"`; **String** (= chính `orderType`, VD `MAK`) nếu không LO — §4.1.3.1. **UI:** number → format giá; string → hiển thị nguyên chuỗi loại lệnh. |
| `orderQuantity` | `orderQuantity` | §5.2 |
| `orderStatus` | `orderStatus` | §8.1 |
| `matchedQuantity` | `matchedQuantity` | |
| `remainingQuantity` | N/A | FE map → `unmatchedQuantity` khi cần đồng bộ với model history |
| `matchedPrice` | `matchedPrice` | |
| `operation` | `operation` | §8.4 |
| `originalOrderNumber` | `originalOrderNumber` | Nếu có → cập nhật **cả dòng lệnh gốc** (chuỗi sửa/hủy) |

**API `GET /api/v1/derivatives/order/todayUnmatch`** — khi có WS, có thể cập nhật tại chỗ (nếu app merge vào cache unmatch):

| Field REST | Nguồn từ WS |
|------------|-------------|
| `matchedQuantity` | `matchedQuantity` |
| `unmatchedQuantity` | `remainingQuantity` |

**Nhãn `status` trên todayUnmatch (text):** map từ `orderStatus` / `operation` theo formatter hiện có — không có field WS riêng.

### 6.8 Nút **Sửa** / **Hủy** (Order Book — áp dụng **cả** tab Open & All)

#### 6.8.1 Luồng cố định NHSV Pro (từ REST — cả hai tab)

Với **mỗi** dòng lệnh đang hiển thị (dữ liệu từ `history` đã merge WS):

1. Lấy `orderNumber` của dòng đó.
2. So sánh với danh sách `orderNumber` từ **`GET /api/v1/derivatives/order/todayUnmatch`**.
   - **Có khớp** → nút Sửa/Hủy **có thể** ở trạng thái active (tiếp tục qua §6.8.2–§6.8.3).
   - **Không khớp** → nút Sửa/Hủy **disabled** (NHSV Pro — không cho user Sửa/Hủy).

#### 6.8.2 Rule chung (sau merge WS / từ state dòng)

Nếu **bất kỳ** điều kiện dưới đây đúng → **disabled** (bất kể đã match `todayUnmatch` hay chưa).

| Điều kiện | Sửa / Hủy |
|-----------|-----------|
| `orderStatus === "FILLED"` | **Disabled** |
| `operation === "CANCEL_ORDER"` **hoặc** `orderStatus === "REJECTED"` | **Disabled** |
| `orderStatus === "RECEIVED"` | **Disabled** |

#### 6.8.3 Rule theo `orderType` (chỉ khi §6.8.1 cho phép **và** §6.8.2 chưa disable)

| `orderType` | Điều kiện **enable** Sửa/Hủy |
|-------------|------------------------------|
| **LO** | `orderStatus === "CONFIRMED"` **hoặc** `orderStatus === "PARTIALLY_FILLED"` |
| **MTL** | `orderStatus === "PARTIALLY_FILLED"` |
| **MOK**, **MAK** | **Không** cho phép Sửa/Hủy **sau khi đã đặt lệnh** — luôn **disabled** (ghi đè các rule khác nếu có nhầm từ `todayUnmatch`) |

**Các `orderType` khác (ATO, ATC, …):** BA bổ sung hoặc mặc định chỉ theo §6.8.1 + §6.8.2.

---

## 7. Field Mapping

### 7.1 Lotte F15303 → TradeX WS Payload (đầy đủ)

| # | Lotte Field | Lotte Type | TradeX WS Field | TradeX Type | Transform | Ghi chú |
|---|-------------|------------|-----------------|-------------|-----------|---------|
| 1 | `event_code` | String | `eventType` | String | Fixed: `"ORDER_STATUS"` | Không expose raw `F15303` ra FE |
| 2 | `event_seqno` | String | `seqNo` | String | Pass-through | Dùng để dedupe |
| 3 | `date` + `evt_time` | String | `eventTime` | String | `yyyymmdd HH:mm:ss` (§4.1.3) hoặc ISO nếu thống nhất | |
| 4 | `acnt_no` | String | `accountNumber` | String | Pass-through | Route key |
| 5 | `evt_ordNo` | String | `orderNumber` | String | Pass-through | **Khóa merge FE** — cùng REST |
| 6 | `evt_code` | String | `symbolCode` | String | Pass-through | |
| 7 | `evt_side` | String | `sellBuyType` | Enum String | `"1"`/`"2"` / `"Mua"`/`"Bán"` → `"BUY"`/`"SELL"` | §8.3 |
| 8 | `evt_action` | String | `operation` | Enum String | `1`/`2`/`3` → `NEW_ORDER`/`MODIFY_ORDER`/`CANCEL_ORDER` — §8.4 |
| 9 | `evt_ordType` | String | `orderType` | Enum String | Xem §8.2 | |
| 10 | `evt_price` | String | `orderPrice` | **Number** (nếu `orderType === "LO"`) **hoặc String** (nếu `orderType !== "LO"`) | **LO:** parse `evt_price` → Number / null. **Không LO:** `orderPrice` = **cùng giá trị** `orderType` (string), bỏ qua hiển thị số từ `evt_price` trên field này — §4.1.3.1 |
| 11 | `evt_qty` | String | `orderQuantity` | Number | Parse to Number | Cùng tên REST history |
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
| `orderPrice` | `orderPrice` (Number nếu LO; String loại lệnh nếu không LO — §4.1.3.1) | `orderPrice` (cùng quy ước) |
| `orderQuantity` | `orderQuantity` | `orderQuantity` |
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
  "orderPrice": 1280.0,
  "orderQuantity": 10,
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
  "orderPrice": 1285.0,
  "orderQuantity": 20,
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
  "orderPrice": 1285.0,
  "orderQuantity": 20,
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
  "orderPrice": "MTL",
  "orderQuantity": 3,
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
  "orderPrice": "MTL",
  "orderQuantity": 3,
  "orderStatus": "PARTIALLY_FILLED",
  "matchedQuantity": 1,
  "remainingQuantity": 2,
  "matchedPrice": 2265.8,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

**FE action:** Merge theo **`orderNumber`**; `seqNo` tăng dần. Giữ **giá khớp 2265.8** đến khi có event đổi giá có nghiệp vụ hoặc refresh REST. Cột **giá đặt** hiển thị **`MTL`** (chuỗi) theo §4.1.3.1 — giá số từ Core (nếu có) không đưa vào `orderPrice` trên WS.

---

### 9.5 MAK — `orderPrice` = chuỗi loại lệnh (không phải LO)

TradeX WS payload (minh họa theo contract §4.1.3.1 / §5.2):

```json
{
  "eventType": "ORDER_STATUS",
  "seqNo": "20260511000123",
  "eventTime": "20260511 14:48:22",
  "accountNumber": "039C200327",
  "orderNumber": "1000007",
  "symbolCode": "41I1G3000",
  "sellBuyType": "BUY",
  "orderType": "MAK",
  "orderPrice": "MAK",
  "orderQuantity": 10,
  "orderStatus": "FILLED",
  "matchedQuantity": 5,
  "remainingQuantity": 5,
  "matchedPrice": 1279.5,
  "operation": "NEW_ORDER",
  "originalOrderNumber": null
}
```

> **Ghi chú nghiệp vụ:** `matchedQuantity` / `remainingQuantity` / `orderStatus` trong ví dụ chỉ minh họa cấu trúc field; khi **`orderStatus === "FILLED"`** thì `remainingQuantity` thường là **0** — BE đảm bảo tính nhất quán nội dung theo §5.2 (đoạn “Tính nhất quán nội dung”).

**FE action:** Cột giá đặt hiển thị **`MAK`** (string), không parse thành số; giá khớp lấy từ **`matchedPrice`**.

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
| Reconnect thành công | **Login** lại §5.3 → REST snapshot §2.3 → **re-subscribe** `order.status.{username}`; tắt indicator |
| App ra background | Giữ state; khi vào foreground: REST §2.3 → **login** (nếu cần) → **re-subscribe** |
| Timeout reconnect (> 30s) | Hiển thị thông báo "Không có kết nối — Kéo để làm mới" |

### 10.3 FE — Event không tìm thấy `orderNumber` (hoặc chưa có trong state)

| Tình huống | Hành động FE |
|------------|-------------|
| `orderNumber` trong payload WS **chưa** có trong state Order Book | **Upsert từ WS (§6.2):** insert dòng mới từ payload — **không** gọi lại `GET .../order/history` chỉ để lấy dòng đó. Nếu payload thiếu field bắt buộc cho UI → gọi REST snapshot §2.3 **một lần** (silent) rồi tiếp tục WS. |
| Event trùng seqNo | Bỏ qua event trùng |
| Event seqNo nhỏ hơn seqNo đã apply | Bỏ qua (stale event) |

---

## 11. Testing Checklist

### 11.1 BE

- [ ] Subscribe `sub/bos.evt.ord.sts.*/` thành công sau khi kết nối Lotte WS
- [ ] Chỉ forward event có `event_code === "F15303"` — bỏ qua F15302 và các mã khác
- [ ] **`orderPrice` (§4.1.3.1):** `orderType === "LO"` → Number từ `evt_price`; `orderType !== "LO"` → string **bằng** `orderType` (VD `"MAK"`)
- [ ] Normalize đúng tất cả fields theo §7.1 — gồm **`orderPrice`** / **`orderQuantity`** (không emit `price`/`quantity` trên WS)
- [ ] Map đúng `evt_status` → **`orderStatus`** — §8.1 (mã Lotte `1`/`2` → rule đã thống nhất với REST history; legacy chữ `A`/`B`/… nếu Core còn gửi)
- [ ] Với lệnh ≠ LO: áp dụng logic `matchedPrice` §4.1.4 (hai bước `evt_price`/`evt_matchPrice`)
- [ ] Map đúng `evt_action` → **`operation`** — §8.4
- [ ] Map đúng `evt_ordType` 0/2/3/4/7/9 → TradeX enum §8.2
- [ ] Publish đúng Kafka topic `order-status-events` với key = `accountNumber`
- [ ] ws-v2 publish đúng channel `order.status.{username}` (username = `acnt_no` event)
- [ ] ws-v2 từ chối subscribe `order.status.*` khi socket chưa login (§5.3)
- [ ] Reconnect Lotte WS khi bị ngắt — re-subscribe sau khi kết nối lại
- [ ] Log WARNING khi phát hiện gap sequence

### 11.2 FE

- [ ] Luồng **connect → login** (`grant_type: access_token`, `domain: nhsv`) **trước** subscribe — §5.3
- [ ] Subscribe `order.status.{username}` chỉ sau login thành công — §5.4
- [ ] Unsubscribe khi unmount **Order Book**
- [ ] Reconnect: **login lại** rồi re-subscribe (không subscribe khi socket chưa authenticated)
- [ ] Parse payload đúng và lọc `eventType === "ORDER_STATUS"`
- [ ] Merge đúng theo **`orderNumber`**; cập nhật dòng trong state **Order Book**; nếu có cache `todayUnmatch` thì cập nhật KL tương ứng khi app merge WS vào unmatch (§6.2)
- [ ] Nếu `orderNumber` chưa có trong state: **upsert** từ WS khi đủ field (§10.3); chỉ gọi REST khi thiếu field hoặc sau reconnect/foreground (§2.2)
- [ ] Deduplicate theo `seqNo` — bỏ qua event cũ hơn
- [ ] Hiển thị **`orderPrice`:** number → format giá; string (= loại lệnh) → hiển thị text §4.1.3.1
- [ ] Sau mỗi event WS: re-render **Order Book** (Open & All) ngay — **không** gọi `history` lại giữa các event; field §6.7 gồm `orderPrice`, `orderQuantity`, `orderStatus`, `operation`, `matchedQuantity`, `remainingQuantity` → `unmatchedQuantity`, `matchedPrice`
- [ ] Payload WS dùng **`orderStatus`** + **`operation`** (§5.2); đồng bộ text `status` trên todayUnmatch theo mapper hiện có (§6.7)
- [ ] Áp rule nút §6.8.1–§6.8.3 (todayUnmatch + `FILLED` / `REJECTED` / `CANCEL_ORDER` / `RECEIVED` + LO / MTL / MOK / MAK)
- [ ] Cập nhật đúng `orderStatus` (§6.3: `RECEIVED`, `CONFIRMED`, `PARTIALLY_FILLED`, `FILLED`, `REJECTED`), `matchedQuantity`, `remainingQuantity`, `matchedPrice`
- [ ] Với lệnh ≠ LO: tin `matchedPrice` từ BE sau §4.1.4; kiểm tra scenario §9.4 (MTL hai event)
- [ ] Hiển thị khớp một phần khi `orderStatus = PARTIALLY_FILLED` (và/hoặc `remainingQuantity > 0`)
- [ ] Gọi REST snapshot §2.3 khi: reconnect WS, app foreground, gap `seqNo`, hoặc không upsert được (§10.3) — **không** dùng làm bước bắt buộc sau **mỗi** event WS khi merge đã thành công
- [ ] Hiển thị indicator khi WS mất kết nối
- [ ] Tắt indicator sau reconnect; gọi REST snapshot §2.3 rồi re-subscribe (§10.2) — không confound với refetch sau mỗi event WS

---

## Related Documents

| Document | Location |
|----------|----------|
| Lotte DR API Specs (F15303) | `Derivatives/Documentation/Lotte_DR_API_Specs.md` §3.1.1 |
| Websocket DR Lotte (market data channels) | `Derivatives/Documentation/Websocket_DR_Lotte.md` |
| Regular Orders API Spec | `Derivatives/Planning documentation/Order/Specifications/Regular_Orders_API_Spec.md` |
| TradeX API Conventions | `Knowledge/TradeX/API Standards/tradex-api-conventions.md` |
| Market Data Channels (WS pattern tham chiếu) | `Knowledge/TradeX/System/market-data-channels.md` |

---

**Document Status:** 📋 Ready for Review  
**For:** BE Developers, FE Developers, QA  
**Next Steps:**  
1. BE review §4 (component design và Kafka integration với kiến trúc hiện tại)  
2. FE review §5.3–§6.2 (login WS `access_token` → subscribe `order.status.{username}`; Order Book merge/upsert)  
3. Confirm payload WS **`orderNumber`** = REST **`orderNumber`**; **`originalOrderNumber`** khi có  
4. Confirm **`orderStatus`** / **`operation`** trên WS trùng contract REST history (§8.1, §8.4)  
5. Confirm **`orderPrice`** §4.1.3.1 (LO = Number; không LO = string = `orderType`) và **`orderQuantity`** trên WS (§5.2, §7.1)  
6. Sign-off → đưa vào sprint planning
