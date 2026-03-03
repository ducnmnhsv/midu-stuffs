# Trade Screen (Normal Order / Quick Order) – FE Requirement

**Issue Type:** Feature Request (Frontend)  
**Priority:** High  
**Component:** NHSV Pro – Trade Tab (Derivatives)  
**Related Module:** Order – Regular Orders  
**Created:** February 24, 2026  
**Updated:** February 24, 2026 (bỏ TP/SL; bổ sung rule Price/Quantity/Real-time price; thêm Agile Developer API Review)  
**Status:** 📋 Ready for FE Dev Review

### Quantity

- **Min:** **1** (không cho phép 0).
- **Bước nhảy:** Default **1** (nút +/- tăng/giảm 1).
- **Validation & toast:** Nếu số lượng **vượt quá sức mua khả dụng** (khi Mua) hoặc **vượt quá sức bán khả dụng** (khi Bán) → hiện **toast**:
  - Chiều **Mua:** *"Vượt quá sức mua khả dụng"*
  - Chiều **Bán:** *"Vượt quá sức bán khả dụng"*  
  Sức mua/sức bán lấy từ API Order Availability Check (xem § API & Data).

### Real-time price (switch)

- **Default:** Switch **bật (enabled)** khi vào màn.
- **Khi bật:** Field **Price** tự động cập nhật theo **giá hiện tại** từ WebSocket – dùng field **`c`** (current price) từ channel `market.quote.{symbol}`.
- **Khi tắt:** Giá **không** cập nhật real-time; user có thể nhập/chỉnh giá tùy ý (vẫn trong khung [FL, CE]).
- **Tự động bật lại:** Sau **10 giây** kể từ lúc tắt → tự động **chuyển switch về bật** (realtime price lại bật).
- **Hành vi tắt khi user chỉnh giá:** Khi user **nhập giá** (hoặc dùng nút +/- để thay đổi Price) → **tự động tắt** switch Real-time price (coi như user đang chỉnh tay).

| API | Method | Endpoint | Mục đích FE |
|-----|--------|----------|-------------|
| Place Order | POST | `/api/v1/derivatives/order` | Đặt lệnh Mua/Bán. Body: `accountNumber`, `symbolCode`, `sellBuyType`, `orderType`, `orderPrice`, `orderQuantity`, `deviceUniqueId`. Success: `message`, `orderNumber`. |
| Modify Order | PUT | `/api/v1/derivatives/order/modify` | Sửa lệnh chờ. |
| Cancel Order | PUT | `/api/v1/derivatives/order/cancel` | Hủy lệnh chờ. |
| Query Unmatch | GET | `/api/v1/derivatives/order/todayUnmatch` | Danh sách lệnh chưa khớp (order book). |
| **Order Availability Check** | GET | `/api/v1/derivatives/order/checkAvailability` | **Sức mua / sức bán:** query params `accountNumber`, `symbolCode`, optional `sellBuyType`. Khi **không truyền** `sellBuyType` → response `{ buy: { availableQuantity, availableLiquidity }, sell: { ... } }`. Dùng để hiển thị **Max buy** / **Max sell** và validate quantity. |
| **Open Positions** | GET | `/api/v1/derivatives/asset/openPositions` | **Open position & Unrealized PnL** (block ngay dưới Bid/Ask): query params `accountNumber`, `subNumber`, optional `symbol` (mã đang chọn). Response: `positions[]` với `symbol`, `side`, `totalQuantity`, `unrealizedPnL`, … Dùng để hiển thị Open position và Unrealized PnL theo symbol. |
| Symbol / Market (nếu dùng REST) | GET | `/api/v2/market/symbol/latest` (hoặc tương đương) | Giá mới nhất, CE/REF/FL (ceilingPrice, floorPrice, referencePrice). |

**Order types:** LO, ATO, ATC, MOK, MAK, MTL. Price: bắt buộc cho LO; null cho MOK, MAK, MTL.  
**Language:** Header `Accept-Language: vi | en | ko` → message từ Lotte tương ứng.

### Max buy / Max sell (Order Availability Check)

- **API:** `GET /api/v1/derivatives/order/checkAvailability`
- **Gọi mặc định:** Khi vào màn Trade (và khi đổi `accountNumber` hoặc `symbolCode`), FE gọi API với **chỉ** `accountNumber` và `symbolCode` — **không truyền** `sellBuyType` — để nhận cùng lúc sức mua và sức bán.
- **Response (không sellBuyType):**  
  `{ "buy": { "availableQuantity": n, "availableLiquidity": ... }, "sell": { "availableQuantity": m, "availableLiquidity": ... } }`
- **Mapping lên UI (Figma):**
  - **Max buy** ← `response.buy.availableQuantity` — hiển thị ngay **bên dưới nút Buy (Long)**.
  - **Max sell** ← `response.sell.availableQuantity` — hiển thị ngay **bên dưới nút Sell (Short)**.
- **Vị trí theo design:** Hai field Max buy / Max sell nằm ngay phía dưới hai nút Buy / Sell.  
  **Figma – Lệnh thường:** [NHSV-Pro – Lệnh thường](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004971-124721&t=Hkbonf9r1expHBzf-11) (node `40004971-124721`).
- **Validation quantity:** Khi user nhập quantity > Max buy (chiều Mua) hoặc > Max sell (chiều Bán) → toast tương ứng (xem § Quantity).

*Issue riêng cho tích hợp Max buy/Max sell (nếu tách Jira):* [Max_Buy_Max_Sell_Integration.md](Max_Buy_Max_Sell_Integration.md).

### Real-time (WebSocket)

| Nguồn | Channel / API | Field dùng cho màn Trade |
|-------|----------------|--------------------------|
| **Giá hiện tại (real-time)** | `market.quote.{symbol}` | **`c`** = current price (fill vào Price khi Real-time price bật). |
| **Sổ lệnh (Bid/Ask)** | `market.bidoffer.{symbol}` | **`bb`** (bestBids), **`bo`** (bestOffers) – giá từng level (`p`), KL (`v`) để hiển thị bảng Bid/Ask và **tap-to-fill** vào Price. |
| **CE / REF / FL** | Từ SymbolInfo (REST hoặc cache từ init/quote) | `ceilingPrice` (CE), `floorPrice` (FL), `referencePrice` (REF) – dùng để validate khoảng giá và hiển thị CE/REF/FL trên UI. |

Chi tiết: [Regular_Orders_API_Spec](../Specifications/Regular_Orders_API_Spec.md), [Order_Availability_Check_API_Spec](../Specifications/Order_Availability_Check_API_Spec.md), [TradeX Knowledge – Market Data Channels](../../../TradeX%20Knowledge/System/market-data-channels.md), [Symbol Info API](../../../TradeX%20Knowledge/System/symbol-info-api.md).

---

## 📋 Executive Summary

### Objective

Xây dựng màn hình **Trade** cho Derivatives (Lệnh thường) trên NHSV Pro theo design Figma, hỗ trợ **hai chế độ giao diện**:

- **Lệnh thường (Normal order)** – Giao diện đầy đủ, nhiều thông tin (phù hợp newbie).
- **Lệnh nhanh (Quick order)** – Giao diện gọn, ít dữ liệu hiển thị (phù hợp Pro user).

**Logic nghiệp vụ** (đặt lệnh, sửa/hủy, API, validation) **giống nhau** giữa hai mode; chỉ khác **UI** và **số lượng/ cách hiển thị dữ liệu**.

### Design Reference (Figma)

| Mode | Figma Link | Node ID |
|------|------------|---------|
| **Lệnh thường** | [NHSV-Pro – Lệnh thường](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004971-124721&t=Hkbonf9r1expHBzf-11) | `40004971-124721` |
| **Lệnh nhanh** | [NHSV-Pro – Lệnh nhanh](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005168-209023&t=Hkbonf9r1expHBzf-11) | `40005168-209023` |
| **Order confirmation – Buy** | [NHSV-Pro – Xác nhận lệnh Mua](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005409-14865&t=Hkbonf9r1expHBzf-11) | `40005409-14865` |
| **Order confirmation – Sell** | [NHSV-Pro – Xác nhận lệnh Bán](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005308-207784&t=Hkbonf9r1expHBzf-11) | `40005308-207784` |

**File Key:** `7KYJfVHawWie4n8v12JtXm` (NHSV Pro)

### Solution Approach

1. **Một màn Trade** với hai layout/component tương ứng hai mode (Normal / Quick), chuyển đổi qua control ở header (badge/button "Lệnh thường" / "Lệnh nhanh").
2. **Chia sẻ logic**: form state, validation, API gọi (Place/Modify/Cancel), error handling, order types (LO, ATO, ATC, MOK, MAK, MTL).
3. **Tách phần hiển thị**: Normal = nhiều block (sổ lệnh 3 level, reference prices, positions, v.v.); Quick = ít block hơn (price ladder + form gọn).
4. **Tái sử dụng** cấu trúc hiện có (TradeTab, mode picker, navigation) và mở rộng cho Derivatives.

**Lưu ý:** Tính năng / lệnh **TP/SL (Take Profit / Stop Loss)** **không** nằm trong scope màn Trade này – đã remove khỏi giao diện.

### Key Success Criteria

---

## 🔍 Agile Developer – API Review

*(Review theo góc nhìn Agile Developer: đủ API/data cho FE chưa.)*

### Kết luận: **Đủ** – Các API và nguồn data cần cho màn Trade (Normal/Quick order) đã được mô tả và có spec/tài liệu.

| Nhu cầu FE | API / Nguồn | Spec / Tài liệu |
|------------|-------------|------------------|
| Đặt lệnh Mua/Bán | `POST /api/v1/derivatives/order` | [Regular_Orders_API_Spec](../Specifications/Regular_Orders_API_Spec.md) |
| Sửa / Hủy lệnh | `PUT .../order/modify`, `.../order/cancel` | Cùng spec trên |
| Lệnh chờ (unmatch) | `GET .../order/todayUnmatch` | Cùng spec trên |
| **Sức mua / sức bán** (Max buy, Max sell, validate quantity) | `GET /api/v1/derivatives/order/checkAvailability` | [Order_Availability_Check_API_Spec](../Specifications/Order_Availability_Check_API_Spec.md) |
| **Giá real-time** (field Price từ giá hiện tại) | WebSocket `market.quote.{symbol}` → field **`c`** | [market-data-channels.md](../../../TradeX%20Knowledge/System/market-data-channels.md) |
| **CE / REF / FL** (khoảng giá, tap CE/REF/FL) | SymbolInfo: `ceilingPrice`, `floorPrice`, `referencePrice` (REST hoặc cache) | [symbol-info-api.md](../../../TradeX%20Knowledge/System/symbol-info-api.md) |
| **Bid/Ask** (sổ lệnh, tap giá fill Price) | WebSocket `market.bidoffer.{symbol}` → `bb`, `bo` (p, v) | [market-data-channels.md](../../../TradeX%20Knowledge/System/market-data-channels.md) |
| **Open position & Unrealized PnL** (block dưới Bid/Ask) | `GET /api/v1/derivatives/asset/openPositions` (optional `symbol`) | [Open_Position_List_API_Spec](../../Asset/Specifications/Open_Position_List_API_Spec.md) |

**Gợi ý cho FE:**  
- Gọi **checkAvailability** khi vào màn hoặc đổi symbol/chiều (buy/sell) để hiển thị Max buy / Max sell và validate "vượt quá sức mua/bán khả dụng".  
- Subscribe **market.quote.{symbol}** để lấy `c` cho Real-time price; subscribe **market.bidoffer.{symbol}** cho sổ lệnh và tap-to-fill.  
- CE/REF/FL có thể lấy từ SymbolInfo (REST `/api/v2/market/symbol/latest` hoặc tương đương theo codebase hiện tại).  
- **Open position & Unrealized PnL** (block ngay dưới Bid/Ask): gọi `GET /api/v1/derivatives/asset/openPositions` với `symbol` = mã đang chọn; lọc/lấy tổng theo symbol để hiển thị Open position và Unrealized PnL.

---

## ✅ Acceptance Criteria (Summary)

| # | Criteria | Mode |
|---|----------|------|
| AC1 | Giao diện Lệnh thường khớp Figma (node 40004971-124721): header, symbol, market data, sổ lệnh 3 level, form (không có TP/SL), tabs, positions, nút Buy/Sell. | Normal |
| AC2 | Giao diện Lệnh nhanh khớp Figma (node 40005168-209023): header, symbol, 2 cột (price ladder + form, không TP/SL), tabs, positions, nút Buy/Sell. | Quick |
| AC3 | Chuyển đổi giữa Lệnh thường và Lệnh nhanh qua control ở header; không mất symbol, price, quantity, order type. | Both |
| AC4 | Khi bấm Buy/Sell → hiển thị **Order confirmation dialog** theo Figma (Buy: node 40005409-14865, Sell: node 40005308-207784); user xác nhận mới gọi `POST /api/v1/derivatives/order`. Hiển thị message và orderNumber khi thành công. | Both |
| AC5 | Validation: required fields; orderPrice null cho MOK/MAK/MTL; orderPrice bắt buộc cho LO. Hiển thị lỗi theo format API (code, message/params). | Both |
| AC6 | Hỗ trợ Accept-Language (vi/en/ko); message từ Lotte hiển thị đúng (pass-through). | Both |
| AC7 | Max buy / Max sell hiển thị đúng theo API Order Availability Check; validate quantity không vượt sức mua/sức bán. | Both |
| AC7b | **Open position & Unrealized PnL** (block ngay dưới Bid/Ask): data từ `GET /api/v1/derivatives/asset/openPositions` (optional `symbol` = mã đang chọn). Hiển thị Open position và Unrealized PnL theo symbol; không nhầm với tab Positions bên dưới. | Both |
| AC8 | **Price:** Chỉ cho phép giá trong [FL, CE]. Tap CE/REF/FL hoặc ô giá Bid/Ask → fill vào Price. Bước giá 0.1. Ngoài khung → toast "Giá đặt ngoài khung giá cho phép". | Both |
| AC9 | **Quantity:** Min 1, bước 1. Vượt sức mua → toast "Vượt quá sức mua khả dụng"; vượt sức bán → toast "Vượt quá sức bán khả dụng". | Both |
| AC10 | **Real-time price:** Default bật; khi bật, Price = `c` từ WebSocket. Khi tắt, user nhập tùy ý. Sau 10s tự bật lại. Khi user chỉnh Price (+/- hoặc nhập) → tắt Real-time price. | Both |
| AC11 | Positions list: B/S, symbol, quantity, Unrealized PnL, entry price, actions (Reverse, Close) theo design – không TP/SL. | Both |

---

## 📚 Reference Documents

| Document | Description |
|----------|-------------|
| [Regular_Orders_API_Spec](../Specifications/Regular_Orders_API_Spec.md) | API Place/Modify/Cancel, request/response, error format |
| [Order_Availability_Check_API_Spec](../Specifications/Order_Availability_Check_API_Spec.md) | Sức mua/sức bán (checkAvailability) |
| [Open_Position_List_API_Spec](../../Asset/Specifications/Open_Position_List_API_Spec.md) | Open position & Unrealized PnL (block dưới Bid/Ask) |
| [01_Regular_Orders_Business](../Planning/01_Regular_Orders_Business.md) | Business rules, user stories |
| [Order README](../README.md) | Tổng quan Order, implementation status |
| [market-data-channels.md](../../../TradeX%20Knowledge/System/market-data-channels.md) | WebSocket quote/bidoffer, field `c`, `bb`, `bo` |
| [symbol-info-api.md](../../../TradeX%20Knowledge/System/symbol-info-api.md) | CE/REF/FL (ceilingPrice, floorPrice, referencePrice) |
| Figma – Lệnh thường | [Link](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004971-124721&t=Hkbonf9r1expHBzf-11) |
| Figma – Lệnh nhanh | [Link](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005168-209023&t=Hkbonf9r1expHBzf-11) |
| Figma – Order confirmation (Buy) | [Link](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005409-14865&t=Hkbonf9r1expHBzf-11) |
| Figma – Order confirmation (Sell) | [Link](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005308-207784&t=Hkbonf9r1expHBzf-11) |

---

**Issue Status:** 📋 Ready for FE Dev Review  
**Next Step:** FE team estimate & implement; PM/BA review UI vs Figma trước UAT.

- [ ] UI bám đúng Figma cho cả hai mode (Lệnh thường & Lệnh nhanh).
- [ ] Chuyển đổi giữa hai mode không làm mất state form (symbol, price, quantity, order type).
- [ ] Đặt lệnh (Buy/Sell) dùng API Derivatives; hiển thị message/orderNumber theo spec.
- [ ] Validation (required fields, price null cho MOK/MAK/MTL, v.v.) thống nhất hai mode.
- [ ] Hỗ trợ đa ngôn ngữ (Accept-Language) và hiển thị message từ Lotte đúng format.

Cùng một luồng đặt lệnh (Regular Orders) nhưng hai trải nghiệm giao diện tùy đối tượng.

### In-Scope (This Issue)

- Màn Trade Derivatives: header (account, mode switch, settings), symbol search, market data, form đặt lệnh (price, quantity, order type, real-time price), Positions / Orderbook / Conditional order tabs, nút Buy (Long) / Sell (Short).
- Hai layout theo Figma: **Lệnh thường** (đầy đủ) và **Lệnh nhanh** (gọn).
- Tích hợp API: Place order, Modify, Cancel, query unmatch, Order Availability Check; WebSocket / SymbolInfo cho giá và CE/REF/FL (xem [§ API & Data](#-api--data-reference) và [§ Agile Developer – API Review](#-agile-developer--api-review)).

### Out of Scope

- Logic Conditional order (Stop, OCO, Trailing) – màn riêng / issue riêng.
- Order History màn chi tiết – issue riêng.
- Thay đổi backend API (chỉ consume API hiện có).

- **Header:** Account ID, badge "Lệnh thường", icon Cài đặt.
- **Symbol:** Search bar (mã CK, ví dụ 4111G2000 (DR)), icon thông tin, icon yêu thích.
- **Tabs:** Order | Chart.
- **Market data:** Giá hiện tại, thay đổi (+/-), %; CE / REF / FL; Vol, Basis.
- **Sổ lệnh (3 level):** Cột Bid Vol, Bid Price, Ask Price, Ask Vol; bar % Bid/Ask; tổng Bid/Ask volume.
- **Open position & Unrealized PnL** *(xem [§ Open position & Unrealized PnL (block dưới Bid/Ask)](#-open-position--unrealized-pnl-block-dưới-bidask))* – khối tóm tắt ngay dưới Bid/Ask, trên Form; data từ `GET /api/v1/derivatives/asset/openPositions`.
- **Form:** Buying power (+), Dropdown "Normal order", Price (+/-), Order type (ATO, LO, MTL, MOK, MAK, ATC), Quantity (+/-), Real-time price toggle. *(Không có TP/SL.)*
- **Tabs dưới:** Positions | Orderbook | Conditional order | See more.
- **Positions list:** B/S, symbol, quantity, Unrealized PnL, entry price, Reverse, Close. *(Không có TP/SL.)*
- **Cảnh báo:** Banner trạng thái tài khoản (ví dụ "Cảnh báo").
- **Actions:** Nút Buy (Long) (Max buy: n), Nút Sell (Short) (Max sell: n). Khi user bấm Buy hoặc Sell → hiển thị **Order confirmation dialog** theo Figma (xem [§ Order confirmation dialog](#-order-confirmation-dialog)) trước khi gọi API đặt lệnh.

### Mode 2: Lệnh nhanh (Quick order)

- **Header:** Account ID, **button "Lệnh nhanh"** (để chuyển sang/chỉ rõ mode), icon Cài đặt.
- **Symbol:** Giống (search, info, favorite).
- **Market data:** Gọn (Vol, Basis, CE, REF, FL).
- **Layout 2 cột:**
  - **Trái:** Price ladder (Price, Qty) – bid/ask real-time; current price nổi bật; bar Bid-Ask %.
  - **Phải:** Form gọn – Open position, Unrealized PnL *(cùng data source như [§ Open position & Unrealized PnL](#-open-position--unrealized-pnl-block-dưới-bidask))*; dropdown "Normal order", Price (+/-), Quantity (+/-), Real-time price. *(Không có TP/SL.)*
- **Tabs:** Positions | Orderbook | Conditional order | See more.
- **Positions:** Giống mode Lệnh thường (có thể rút gọn số dòng hiển thị nếu design quy định).
- **Actions:** Buy (Long) / Sell (Short) với Max buy / Max sell. Khi user bấm Buy hoặc Sell → hiển thị **Order confirmation dialog** theo Figma (xem [§ Order confirmation dialog](#-order-confirmation-dialog)) trước khi gọi API đặt lệnh.

### Chuyển đổi mode

- Từ header: chọn "Lệnh thường" hoặc "Lệnh nhanh" (hoặc badge/button tương đương trong Figma).
- Khi đổi mode: giữ symbol, price, quantity, order type; chỉ đổi layout và số lượng data hiển thị.

### Open position & Unrealized PnL (block dưới Bid/Ask)

**Vị trí:** Khối tóm tắt nằm **ngay phía dưới khu vực Bid/Ask**, **phía trên** form đặt lệnh (Price, Quantity, …). *Không nhầm với tab "Positions" ở phía dưới (danh sách chi tiết từng position).*

**Mục đích:** Hiển thị nhanh cho **mã đang chọn (symbol hiện tại)**:
- **Open position** – Vị thế ròng (hoặc Long/Short) của mã đó (ví dụ: "+5", "-5", "0").
- **Unrealized PnL** – Tổng lãi/lỗ chưa thực hiện của các position thuộc mã đó (ví dụ: "-3,500,540").

**API & data:**

| Nguồn | Endpoint | Cách dùng |
|-------|----------|-----------|
| Open Positions | `GET /api/v1/derivatives/asset/openPositions` | Query params: `accountNumber`, `subNumber`; **optional `symbol`** = mã đang chọn để chỉ lấy position của mã đó. |

**Response (Open Positions):**  
- `positions[]`: mỗi item có `symbol`, `side` (LONG/SHORT), `totalQuantity`, `unrealizedPnL`, `averagePrice`, …  
- Chi tiết: [Open_Position_List_API_Spec](../../Asset/Specifications/Open_Position_List_API_Spec.md).

**Cách hiển thị gợi ý (theo symbol hiện tại):**
- **Open position:** Lọc `positions` theo `symbol` = mã đang chọn; nếu có nhiều position (vd: LONG + SHORT) thì tổng hợp net (LONG totalQuantity − SHORT totalQuantity) hoặc hiển thị theo convention design (vd: "+5" = net long 5, "-5" = net short 5). Không có position cho mã đó → hiển thị "0" hoặc "-".
- **Unrealized PnL:** Tổng `unrealizedPnL` của tất cả position có cùng `symbol`. Format số (vd: dấu cách hàng nghìn, màu đỏ nếu âm, xanh nếu dương). Không có position → "0".

**Lưu ý:** Có thể gọi API khi vào màn Trade, khi đổi symbol, hoặc refresh; nếu có cơ chế real-time cập nhật position từ backend thì cập nhật block này tương ứng.

### Order confirmation dialog

**Thời điểm hiển thị:** Khi user bấm nút **Buy (Long)** hoặc **Sell (Short)**, cần hiển thị **dialog xác nhận lệnh** (Order confirmation dialog) **trước khi** gọi `POST /api/v1/derivatives/order`. Dialog cho user xem lại thông tin lệnh và xác nhận / hủy.

**Design Figma:**

| Loại lệnh | Figma Link | Node ID |
|-----------|------------|---------|
| **Buy order** | [NHSV-Pro – Order confirmation (Buy)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005409-14865&t=Hkbonf9r1expHBzf-11) | `40005409-14865` |
| **Sell order** | [NHSV-Pro – Order confirmation (Sell)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005308-207784&t=Hkbonf9r1expHBzf-11) | `40005308-207784` |

**Nội dung dialog (gợi ý):** Hiển thị theo đúng layout Figma; thường bao gồm symbol, side (Mua/Bán), loại lệnh, giá, khối lượng, và nút Xác nhận / Hủy. Khi user bấm **Xác nhận** → gọi API Place Order; khi **Hủy** → đóng dialog, không gửi lệnh.

**Áp dụng:** Cả hai mode (Lệnh thường và Lệnh nhanh).

---

## 📊 Business Context

### User Need

- **Newbie:** Cần giao diện rõ ràng, đủ thông tin (giá tham chiếu, sổ lệnh, vị thế) để ra quyết định.
- **Pro user:** Cần giao diện gọn, ít scroll, đặt lệnh nhanh (price ladder, form tối giản).

---

## 🎨 UI Requirements (From Figma)

### Mode 1: Lệnh thường (Normal order)

---

## 📐 Form Rules (Price, Quantity, Real-time Price)

### Price

- **Khoảng giá hợp lệ:** Chỉ cho phép nhập giá trong khoảng **FL (floor) ≤ Price ≤ CE (ceiling)**. Nguồn: CE/REF/FL từ SymbolInfo hoặc market data (xem § API & Data).
- **Tap-to-fill:** Khi user tap vào:
  - Ô **CE** hoặc **REF** hoặc **FL** trên màn hình, hoặc
  - Các **ô giá trong bảng Bid/Ask** (sổ lệnh),  
  → giá tương ứng **tự động fill** vào field **Price**.
- **Bước giá (tick):** Default **0.1**. Khi dùng nút +/- thì tăng/giảm theo bước 0.1.
- **Validation & toast:** Nếu giá nhập (hoặc sau bước nhảy) **ngoài khung [FL, CE]** → hiện **toast**: *"Giá đặt ngoài khung giá cho phép"*. Áp dụng cho cả hai chiều Mua và Bán.

---

## 🔌 API & Data (Reference)

### REST APIs

---

## 📁 FE Repo Context (Read-Only Reference)

- **Repo:** NHSV Pro React Native (`nhsv-mts-rn`).
- **Vị trí liên quan:**  
  - `src/screens/TradeTab/` – Màn Trade (TradeForm, TradeOneTouchForm, TradeOrderModePicker).  
  - `src/components/` – TradeForm, TradeExecuteButton, TradeSymbolHeader, TradeBidAskList, …  
  - `src/reduxs/` – trade mode state, API calls.  
  - `src/constants/enum.ts` – `TRADE_MODE.ORDER` | `TRADE_MODE.ONE_TOUCH_ORDER`.
- **Ghi chú:** Hiện tại đã có Order vs One-touch order; cần ánh xạ **Lệnh thường** ↔ Normal layout và **Lệnh nhanh** ↔ Quick layout đúng Figma Derivatives, và đảm bảo Derivatives dùng API `/api/v1/derivatives/order`.
