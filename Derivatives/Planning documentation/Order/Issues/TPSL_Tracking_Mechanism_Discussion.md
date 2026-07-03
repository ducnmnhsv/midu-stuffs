# TP/SL Tracking Mechanism - Open Discussion

**Status:** 🔴 BLOCKED - Chờ Core cung cấp order lifecycle events  
**Date:** February 5, 2026  
**Category:** Technical Design Decision

---

## 1. Vấn Đề Cần Giải Quyết

### 1.1 Business Requirement

**Quy tắc nghiệp vụ:**
- ✅ 1 mã chỉ có 1 vị thế đang mở duy nhất
- ✅ 1 mã chỉ có 1 lệnh TP/SL active duy nhất
- ✅ Khi lệnh gốc bị hủy → TP/SL phải tự động hủy

### 1.2 Technical Challenge

**TradeX hiện tại chỉ track được:**
- ✅ Position (vị thế) - Query từ Core
- ❌ Order lifecycle (modify, cancel events)

**Tại sao?**
- Core (Lotte) không có callback/webhook về order events
- TradeX chỉ biết kết quả cuối cùng (position), không biết quá trình (orders)

### 1.3 Kịch Bản Vấn Đề

```
Scenario: User modify lệnh gốc

Core xử lý:
  1. Order cũ 2026020500012 → Hủy
  2. Order mới 2026020500099 → Tạo mới

TradeX biết được gì?
  ❌ KHÔNG biết order cũ bị hủy
  ❌ KHÔNG biết order mới được tạo  
  ❌ KHÔNG biết mối liên hệ giữa 2 orders
  
Vấn đề:
  → TP/SL đang track order 2026020500012
  → Order này bị hủy nhưng TradeX không biết
  → TP/SL không thể tự động chuyển sang order mới
```

---

## 2. Các Approach Đã Thảo Luận

### Approach 1: Order-Based (Lý tưởng nhưng không khả thi)

**Concept:**
- TP/SL gắn với `orderNumber` cụ thể
- Track order lifecycle để xử lý events

**Yêu cầu:**
- ✅ Core phải cung cấp order events:
  - Order matched
  - Order cancelled  
  - Order modified
- ✅ TradeX lắng nghe events và update TP/SL

**Pros:**
- ✅ Accurate - Biết chính xác order nào bị hủy/modify
- ✅ Real-time - Phản ứng ngay lập tức
- ✅ No polling - Không cần query liên tục

**Cons:**
- ❌ **BLOCKED** - Core hiện không cung cấp events
- ❌ Phụ thuộc vào Core implement tính năng mới

### Approach 2: Position-Based (Khả thi với hạn chế)

**Concept:**
- TP/SL chỉ gắn với vị thế: `accountNumber` + `symbolCode`
- KHÔNG lưu `orderNumber`
- Polling position định kỳ để check

**Implementation:**
```
Trigger TP/SL khi:
  ✅ Position > 0: Continue monitoring
  ❌ Position = 0: Auto-cancel TP/SL
  
Polling frequency: Mỗi 5-10 giây query position từ Core
```

**Pros:**
- ✅ Khả thi với infrastructure hiện tại
- ✅ Không phụ thuộc Core thay đổi
- ✅ Đơn giản, dễ implement
- ✅ Giống Binance (industry standard)

**Cons:**
- ⚠️ Delay 5-10 giây khi phát hiện position = 0
- ⚠️ Không biết lý do (order cancelled vs position closed by another order)
- ⚠️ Tốn resource (polling liên tục)

### Approach 3: Hybrid (Kết hợp cả 2)

**Concept:**
- Lưu cả `orderNumber` (reference) và position info
- Polling position + Query unmatch orders để detect cancel

**Implementation:**
```
Mỗi 5-10 giây:
  1. Query position → Check vị thế còn không
  2. Query unmatch orders → Check order gốc còn không
  
  IF (position = 0) OR (order not found):
    → Cancel TP/SL
```

**Pros:**
- ✅ Detect được order cancelled (query unmatch orders)
- ✅ Biết rõ lý do hủy

**Cons:**
- ⚠️ Vẫn có delay 5-10 giây
- ⚠️ 2 API calls mỗi lần poll (tốn resource hơn)

---

## 3. Offset-Based Mechanism

### 3.1 Câu Hỏi Chưa Trả Lời

**Offset-Based có tự động update giá kích hoạt không?**

**Option A: Cố định (như Binance)**
```
Đặt @ 1250 với offset 30
  → TP @ 1280, SL @ 1220 (snapshot)
  
Mua thêm @ 1255:
  → Giá vào TB = 1251.67
  → TP vẫn @ 1280 (KHÔNG ĐỔI)
  → SL vẫn @ 1220 (KHÔNG ĐỔI)
  → User phải SỬA LỆNH thủ công nếu muốn update
```

**Option B: Dynamic (tự động update)**
```
Đặt @ 1250 với offset 30
  → TP @ 1280, SL @ 1220
  
Mua thêm @ 1255:
  → Giá vào TB = 1251.67
  → TP TỰ ĐỘNG @ 1281.67 (1251.67 + 30)
  → SL TỰ ĐỘNG @ 1221.67 (1251.67 - 30)
  → Luôn duy trì R:R ratio
```

**Câu hỏi:**
- Nếu Option A: Khác gì với Price-Based? (chỉ khác UX khi nhập)
- Nếu Option B: Làm sao track giá vào trung bình? (cần query position liên tục)

### 3.2 So Sánh 2 Options

| Tiêu Chí | Option A (Static) | Option B (Dynamic) |
|----------|-------------------|-------------------|
| **Giá kích hoạt** | Cố định từ lúc đặt | Tự động cập nhật theo avg entry |
| **R:R ratio** | Thay đổi khi giá vào đổi | Luôn giữ nguyên |
| **Complexity** | Đơn giản | Phức tạp (cần track avg entry) |
| **User experience** | Phải sửa thủ công | Tự động, tiện lợi |
| **Industry standard** | Binance dùng cách này | Chưa thấy ai dùng |

---

## 4. Dependencies & Next Steps

### 4.1 Cần Request Core

**Yêu cầu gửi Core team:**

**Option 1: WebSocket Events (Lý tưởng)**
```
Topic: order.events.{accountNumber}

Events cần thiết:
  1. ORDER_MATCHED
     - orderNumber
     - matchedQuantity
     - matchedPrice
     
  2. ORDER_CANCELLED
     - orderNumber
     - cancelReason
     
  3. ORDER_MODIFIED
     - oldOrderNumber
     - newOrderNumber
     - Relationship mapping
```

**Option 2: Query API Enhancement (Tối thiểu)**
```
GET /api/order/lifecycle?orderNumber=xxx

Response:
  - status: ACTIVE / CANCELLED / MODIFIED
  - If MODIFIED:
    - newOrderNumber: xxx (order mới sau khi modify)
```

### 4.2 Decision Matrix

| Core Provides | TradeX Approach | TP/SL Tracking |
|---------------|-----------------|----------------|
| ✅ Order events (WebSocket) | Order-Based | Track orderNumber, auto-switch on modify |
| ✅ Lifecycle API | Hybrid | Poll lifecycle API + position |
| ❌ Nothing | Position-Based | Only track position, 5-10s delay |

### 4.3 Recommended Approach (Tạm thời)

**Cho đến khi Core cung cấp events:**

**Use Position-Based approach:**
1. TP/SL track: `accountNumber` + `symbolCode`
2. KHÔNG lưu `orderNumber`
3. Poll position mỗi 10 giây
4. Nếu position = 0 → Auto-cancel TP/SL

**Khi Core cung cấp events:**
- Upgrade lên Order-Based approach
- Add `orderNumber` tracking
- Listen to order events
- Real-time cancel/modify handling

---

## 5. Open Questions

### 5.1 Offset-Based Behavior

**Question:** Offset-Based có tự động update giá kích hoạt theo avg entry price không?

**Considerations:**
- Nếu Static: Value của Offset-Based là gì? (chỉ khác UX với Price-Based)
- Nếu Dynamic: Làm sao track avg entry price real-time?

**Need:** User/Stakeholder decision

### 5.2 Performance vs Accuracy

**Question:** Chấp nhận delay 5-10 giây để detect order cancelled?

**Trade-offs:**
- Fast (1-2s delay): Polling mỗi 1-2 giây → Tốn resource
- Balanced (5-10s): Polling mỗi 5-10 giây → Chấp nhận được
- Slow (30s+): Polling mỗi 30s → User experience kém

**Need:** Business decision về acceptable delay

---

## 6. References

### 6.1 Industry Research

**Binance Futures:**
- Source: https://www.binance.com/en/support/faq/how-to-place-stop-loss-and-take-profit-orders-on-binance-futures-360040016512
- Approach: Position-based
- Key finding: "Once a TP/SL order is placed, it won't be updated or canceled by the system if a position is added or partially closed"
- User action: Manual edit required

**VPBank:**
- Source: https://hdsd.vpbanks.com.vn/phai-sinh/danh-muc-vi-the/cai-dat-gia-sl-tp-tren-danh-muc
- Rule: "Yêu cầu không thể đặt nhiều lệnh TP/SL đang cho 1 mã chứng khoán phái sinh"
- Approach: Position-based (1 symbol = 1 TP/SL)

### 6.2 Related Documents

- Business Requirements: [04_TPSL_Orders_Business](../../../Planning%20documentation/Order/Planning/04_TPSL_Orders_Business.md)
- API Specification: [TPSL_Orders_API_Spec](../../../Planning%20documentation/Order/Specifications/TPSL_Orders_API_Spec.md)
- TradeX API Conventions: `@Knowledge/TradeX/API Standards/tradex-api-conventions.md`

---

## 7. Action Items

### For PM (You)

- [ ] Request Core team cung cấp order lifecycle events hoặc API
- [ ] Decide: Offset-Based behavior (Static vs Dynamic)
- [ ] Decide: Acceptable delay cho position polling (5s? 10s?)
- [ ] Prioritize: Launch với Position-Based approach trước, upgrade sau?

### For Dev Team (Sau khi có decision)

- [ ] Implement approach được chọn
- [ ] Design database schema cho TP/SL
- [ ] Implement position polling mechanism
- [ ] Implement WebSocket real-time updates (tpsl.{accountNumber})
- [ ] Implement OneSignal push notifications

---

**Last Updated:** February 5, 2026  
**Status:** 🔴 Discussion paused - Waiting for Core team response  
**Next Step:** Request Core for order lifecycle events/API
