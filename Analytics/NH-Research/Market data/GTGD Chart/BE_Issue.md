# BE Issue — GTGD Chart: Lưu dữ liệu per-minute vào MongoDB

> **Loại tài liệu:** BE Technical Issue  
> **Ngày tạo:** 2026-06-08  
> **Liên quan:** [PRD.md](./PRD.md)

---

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

Để vẽ đường "GTGD phiên trước" trên chart, app cần dữ liệu GTGD từng phút của ngày hôm trước. Hiện tại, hệ thống BE chỉ lưu **tổng GTGD cuối ngày** vào database — không có breakdown theo từng phút, nên không thể phục vụ tính năng này.

### Current vs Target

| | Hiện tại | Sau khi implement |
|---|---|---|
| Database (MongoDB) | Chỉ lưu 1 giá trị GTGD tổng / ngày | Lưu GTGD tích lũy theo từng phút |
| API phiên trước | Không có data | Trả về đủ dữ liệu từng phút cho ngày bất kỳ |
| API hôm nay | Đang hoạt động ✅ | Không thay đổi |

### Solution Approach (HIGH-LEVEL)

Sửa **job tự động chạy lúc 15:30 hàng ngày** (khi phiên giao dịch kết thúc): thay vì chỉ lưu tổng GTGD, lưu thêm toàn bộ chuỗi GTGD theo từng phút trong ngày vào database.

Sau đó, cập nhật logic query để API có thể truy xuất chuỗi này khi FE yêu cầu dữ liệu ngày hôm trước.

**Không cần tạo API endpoint mới** — reuse endpoint `/tradingview/history` đã có.

### Timeline

| Hạng mục | Effort |
|---|---|
| Sửa EOD flush job (realtime-v2) | 1–2 ngày |
| Cập nhật query logic (market-query-v2) | 1 ngày |
| Test + verify data | 1 ngày |
| **Tổng** | **3–4 ngày** |

### Success Criteria

- [ ] Sau 15:30 hàng ngày, database có đủ dữ liệu GTGD từng phút cho ngày đó
- [ ] Gọi `/tradingview/history` với `from/to` là ngày hôm qua → trả về đúng chuỗi GTGD theo phút
- [ ] Data đúng cho cả 3 sàn: HOSE (VN-Index), HNX, UPCOM
- [ ] Nếu job fail → không ảnh hưởng luồng giao dịch chính, chỉ miss data ngày đó

---

## 🔍 Technical Background (PM CAN SKIP)

### Luồng hiện tại

```
Trong ngày:
  WebSocket (Lotte) → Kafka → realtime-v2 → Redis SYMBOL_QUOTE_MINUTE_{code}
  FE gọi /tradingview/history → market-query-v2 query Redis → trả về data ✅

Cuối ngày (15:30):
  realtime-v2 EOD Flush Job:
    → Đọc Redis SYMBOL_QUOTE_MINUTE_{code}
    → Lưu vào MongoDB symbolQuoteMinutes (chỉ OHLCV, KHÔNG có tradingValue)
    → Clear Redis key
```

### Vấn đề kỹ thuật

Mỗi record trong `symbolQuoteMinutes` hiện có: `{code, time, open, high, low, close, volume}` — **thiếu field `tradingValue` (GTGD tích lũy tại phút đó)**.

Field `tradingValue` tồn tại trong Redis (real-time data) nhưng không được lưu khi flush xuống MongoDB.

### Thay đổi cần làm

#### Service 1: `realtime-v2`

**File:** EOD Flush Job (scheduled 15:30)

Thêm field `cv` (cumulative value = GTGD tích lũy) vào mỗi minute record khi write MongoDB:

```
Trước: { code, time, open, high, low, close, volume }
Sau:   { code, time, open, high, low, close, volume, cv: <GTGD tích lũy tại phút đó> }
```

`cv` lấy từ field `tradingValue` đã có trong Redis minute record.

#### Service 2: `market-query-v2`

**File:** `FeedService` — method `getQuoteMinuteHistory()`

Khi query MongoDB `symbolQuoteMinutes` cho ngày trước, map field `cv` → `v` trong TradingView response:

```
Response v[] = cv[] từ MongoDB (thay vì volume như hiện tại)
```

> **Lưu ý:** `v` trong context này là GTGD (trading value), không phải volume — đây là convention riêng của TradeX, đã được PM confirm.

#### MongoDB Schema

Collection: `symbolQuoteMinutes`  
Thêm field: `cv: Number` (cumulative trading value tại phút đó, đơn vị VND)

---

## 📝 Detailed Requirements

### REQ-BE-GTGD-01: EOD Flush — Lưu tradingValue

**Service:** `realtime-v2`  
**Trigger:** Scheduled job 15:30, thứ 2 – thứ 6  
**Phạm vi:** Tất cả symbols có trong Redis, bao gồm index symbols (VNINDEX, HNXINDEX, HNXUpcomIndex — dev confirm exact codes)

**Logic:**
1. Đọc toàn bộ records từ Redis `SYMBOL_QUOTE_MINUTE_{code}`
2. Với mỗi record, lấy thêm field `tradingValue`
3. Ghi vào MongoDB `symbolQuoteMinutes` kèm field `cv = tradingValue`
4. Clear Redis key (như hiện tại)

**Error handling:** Nếu `tradingValue` null hoặc 0 cho 1 record → vẫn ghi record, `cv = 0`. Không được bỏ record.

---

### REQ-BE-GTGD-02: FeedService — Query tradingValue từ MongoDB

**Service:** `market-query-v2`  
**Method:** `getQuoteMinuteHistory()`  
**Điều kiện áp dụng:** Khi `from/to` là ngày trong quá khứ (query MongoDB, không phải Redis)

**Logic:**
- Query MongoDB `symbolQuoteMinutes` như hiện tại
- Map `cv` → trường `v` trong TradingView response
- Nếu record cũ (không có field `cv`) → `v = 0`

**Response format giữ nguyên:**
```json
{ "s": "ok", "t": [...], "v": [...], "c": [...], "o": [...], "h": [...], "l": [...] }
```

---

### Kiểm tra sau khi deploy

| Test Case | Input | Expected |
|---|---|---|
| Query ngày hôm qua (sau 15:30) | `symbol=VNINDEX, from=yesterday 09:30, to=yesterday 15:00, resolution=1` | Trả về mảng `v[]` có giá trị tăng dần, không toàn 0 |
| Query hôm nay | Như hiện tại | Không thay đổi |
| Query ngày xa hơn (trước khi deploy) | `from/to` = 2 ngày trước | Trả về `v[]` toàn 0 hoặc `no_data` — chấp nhận được |
| Job fail 1 ngày | Giả lập lỗi flush | Ngày hôm sau query ngày bị miss → `v[]` toàn 0, các ngày khác không ảnh hưởng |

---

**Document Status:** Ready for development  
**For:** BE Team (realtime-v2, market-query-v2)  
**Next Steps:** Implement REQ-BE-GTGD-01 → REQ-BE-GTGD-02 → Test → Notify FE team
