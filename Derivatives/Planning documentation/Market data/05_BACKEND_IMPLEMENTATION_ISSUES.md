# Issue Requirements - Market Data Phái Sinh

> **Document Type:** Backend Implementation Issues  
> **Created:** 2025-01-30  
> **For:** Backend Development Team  
> **Approach:** Business-focused, Flow-based

---

## Overview

Tài liệu này chia nhỏ dự án "Tích hợp Market Data Phái sinh" thành các issues độc lập để BE team thực thi. Mỗi issue tập trung vào **WHAT** (làm gì) và **WHY** (tại sao), không đi sâu vào HOW (cách làm).

---

## Epic: Market Data Phái Sinh

**Goal:** Người dùng có thể xem thông tin giá và giao dịch của các mã phái sinh (Futures VN30) trên app NHSV Pro.

**Success Criteria:**
- App hiển thị danh sách mã phái sinh
- Giá cập nhật real-time
- Không ảnh hưởng hệ thống cơ sở hiện tại

---

## Issue 1: Cập nhật danh sách mã phái sinh đầu ngày

### Priority: P0 (Blocker)

### As a

System Administrator / Scheduler

### I want

Hệ thống tự động lấy danh sách mã phái sinh và thông tin giá đầu mỗi ngày giao dịch

### So that

- App có danh sách mã phái sinh để hiển thị
- Người dùng có thông tin tham chiếu (giá trần, sàn, TC) khi vào app

### Context

Hiện tại, init job chỉ lấy mã cơ sở. Cần mở rộng để lấy thêm mã phái sinh nhưng KHÔNG được làm ảnh hưởng flow hiện tại.

### Requirements

#### R1: Lấy danh sách mã phái sinh từ Lotte

**Input:**
- API Lotte: `/tuxsvc/market/dr/stock-board`
- Method: POST
- Không cần parameters (lấy tất cả)

**Output:**
- Danh sách 4-8 mã VN30F (VN30F2501, VN30F2502, VN30F2503, VN30F2506)
- Mỗi mã có: giá hiện tại, thay đổi, trần/sàn/TC, khối lượng, OI

#### R2: Lấy thông tin chi tiết từng mã

**Input:**
- API Lotte: `/tuxsvc/market/dr/stock-price?code={mã}`
- Gọi cho từng mã trong danh sách

**Output:**
- Thông tin chi tiết: tên đầy đủ, sổ lệnh, mã cơ sở (VN30), ngày đáo hạn

#### R3: Lưu vào file symbol_static.json

**Format:**
```json
{
    "s": "VN30F2501",
    "m": "derivatives",
    "n1": "HĐ Tương lai VN30 Tháng 01/2025",
    "n2": "VN30 Index Futures Jan 2025",
    "t": "FUTURES",
    "re": 1273.0,
    "ce": 1350.0,
    "fl": 1220.0,
    "lq": 0,
    "bc": "VN30",
    "ed": "20250130",
    "rd": 15
}
```

**Key identifier:** `m: "derivatives"` - để phân biệt với cơ sở

### Business Rules

| Rule | Description |
|------|-------------|
| BR-1 | Chỉ chạy vào ngày giao dịch (không chạy T7, CN, lễ) |
| BR-2 | Chạy trước 8:30 sáng (trước giờ mở cửa) |
| BR-3 | Nếu phái sinh lỗi → Log warning, tiếp tục với cơ sở |
| BR-4 | File `symbol_static.json` phải chứa CẢ cơ sở VÀ phái sinh |

### Flow Diagram

```
[Bắt đầu]
    │
    ▼
[Kiểm tra ngày giao dịch]
    │
    ├─→ [Ngày nghỉ] → [Kết thúc]
    │
    ▼
[Lấy mã CƠ SỞ] (existing flow)
    │
    ▼
[TRY: Lấy mã PHÁI SINH]
    │
    ├─→ [Thành công] → [Gộp danh sách]
    │                        │
    └─→ [Thất bại] → [Log warning]
                            │
                            └─→ [Chỉ lưu cơ sở]
                                    │
                                    ▼
                        [Lưu vào Redis, MongoDB, S3]
                                    │
                                    ▼
                                [Kết thúc]
```

### Acceptance Criteria

**MUST:**
- [ ] File `symbol_static.json` có mã phái sinh với `m: "derivatives"`
- [ ] Mỗi mã phái sinh có đủ fields: s, m, n1, n2, t, re, ce, fl, lq, bc, ed, rd
- [ ] Khi Lotte DR API lỗi, job vẫn hoàn thành thành công với mã cơ sở

**MUST NOT:**
- [ ] Không được fail job khi phái sinh lỗi
- [ ] Không được ảnh hưởng danh sách mã cơ sở

### Testing Checklist

- [ ] Happy path: Init job với cả cơ sở và phái sinh
- [ ] Negative: Lotte DR API timeout → job vẫn thành công
- [ ] Negative: Lotte DR API trả về lỗi → job vẫn thành công
- [ ] Regression: Mã cơ sở vẫn được lấy đầy đủ như trước

### Dependencies

- Lotte API credentials (OAuth2 + API KEY)
- Ngày nghỉ/lễ được config đúng

### Estimated Effort

**5-6 days**

---

## Issue 2: Nhận giá phái sinh real-time từ WebSocket

### Priority: P0 (Blocker)

### As a

System

### I want

Subscribe WebSocket của Lotte để nhận giá phái sinh real-time và cập nhật vào Redis

### So that

- API `/api/v2/market/symbolInfo` trả về giá mới nhất
- WebSocket của TradeX có thể publish giá cho client

### Context

Hiện tại hệ thống đã subscribe WebSocket Lotte cho cơ sở (channels: `auto.qt`, `auto.bo`). Cần thêm subscribe cho phái sinh (channels: `auto.dr.qt`, `auto.dr.bo`) nhưng PHẢI cô lập để không ảnh hưởng cơ sở.

### Requirements

#### R1: Subscribe Lotte Derivatives WebSocket

**Channels cần subscribe:**

| Channel | Mô tả | Data nhận được |
|---------|-------|----------------|
| `pro.pub.auto.dr.qt./{code}` | Giá phái sinh | Giá, thay đổi, KL, ĐTNN |
| `pro.pub.auto.dr.bo./{code}` | Sổ lệnh phái sinh | 10 bước giá mua/bán |

**Subscribe cho tất cả mã:** VN30F2501, VN30F2502, VN30F2503, VN30F2506

#### R2: Parse message format

**Quote message (pipe-separated):**
```
pro.pub.auto.dr.qt|Y|103025|VN30F2501|094532|101245|1275.0|2|1290.0|...
```

**Cần extract:**
- Giá hiện tại, thay đổi, %
- Mở cửa, cao nhất, thấp nhất
- Khối lượng, tổng KL mua/bán
- ĐTNN mua/bán

**BidOffer message (pipe-separated):**
```
pro.pub.auto.dr.bo|Y|103025|VN30F2501|O|0|0|1285.0|2|1200|...
```

**Cần extract:**
- 10 bước giá (mỗi bước: giá + KL) cho mua và bán
- Tổng KL dư mua/bán
- Giá khớp dự kiến (trong phiên ATO/ATC)

#### R3: Publish vào Kafka

**Topics:**
- `quoteUpdateDR` - cho quote updates
- `bidOfferUpdateDR` - cho bid/offer updates

**Message format:** JSON với tất cả fields đã parse

### Business Rules

| Rule | Description |
|------|-------------|
| BR-1 | Nếu DR WebSocket disconnect → không ảnh hưởng equity WebSocket |
| BR-2 | Độ trễ tối đa: 1 giây (từ Lotte → Kafka) |
| BR-3 | Parse error → log warning, skip message, tiếp tục nhận message tiếp theo |

### Flow Diagram

```
[WebSocket Connected]
        │
        ▼
[Subscribe tất cả mã DR]
        │
        ▼
[Nhận message từ Lotte]
        │
        ├─→ [auto.dr.qt] → [Parse Quote] → [Publish to quoteUpdateDR]
        │
        └─→ [auto.dr.bo] → [Parse BidOffer] → [Publish to bidOfferUpdateDR]

[Nếu error]
    │
    └─→ [Log warning] → [Continue listening]
```

### Acceptance Criteria

**MUST:**
- [ ] Subscribe thành công tất cả DR channels
- [ ] Message được parse đúng format
- [ ] Publish vào Kafka topics mới

**MUST NOT:**
- [ ] Không được ảnh hưởng equity WebSocket khi DR WebSocket lỗi
- [ ] Không được block equity message processing

### Testing Checklist

- [ ] Happy path: Nhận và parse message thành công
- [ ] Negative: WebSocket disconnect → auto reconnect
- [ ] Negative: Parse error → skip message, continue
- [ ] Regression: Equity WebSocket vẫn hoạt động bình thường

### Dependencies

- Issue 1 (cần có danh sách mã để subscribe)
- Kafka topics được tạo: `quoteUpdateDR`, `bidOfferUpdateDR`

### Estimated Effort

**4-5 days**

---

## Issue 3: Aggregate giá phái sinh vào Redis

### Priority: P0 (Blocker)

### As a

Backend System

### I want

Consume Kafka messages và aggregate dữ liệu phái sinh vào Redis

### So that

API `/api/v2/market/symbolInfo` tự động trả về giá mới nhất mà không cần thay đổi code

### Context

Đây là mechanism hiện tại của equity:
```
WebSocket → Kafka → realtime-v2 consume → Aggregate vào Redis → API đọc từ Redis
```

Cần áp dụng cơ chế tương tự cho derivatives để FE KHÔNG cần thay đổi cách gọi API.

### Requirements

#### R1: Consume từ Kafka

**Topics cần consume:**
- `quoteUpdateDR`
- `bidOfferUpdateDR`

**Group ID:** `realtime-v2-dr`

#### R2: Update SymbolInfo trong Redis

**Redis key:** `realtime_mapSymbolInfo` (CÙNG key với equity)

**Update logic:**

**Từ Quote message:**
- Cập nhật: last, change, rate, open, high, low
- Cập nhật: tradingVolume, matchingVolume
- Cập nhật: totalBidVolume, totalOfferVolume
- Cập nhật: foreignerBuyVolume, foreignerSellVolume
- Cập nhật: time, updatedAt

**Từ BidOffer message:**
- Cập nhật: bidOfferList (10 bước giá)
- Cập nhật: totalBidVolume, totalOfferVolume
- Cập nhật: sessions (ATO/LO/ATC)
- Cập nhật: expectedPrice (nếu có, trong ATO/ATC)

#### R3: Maintain data structure

**SymbolInfo trong Redis phải chứa:**
- Tất cả fields từ init job (s, m, n1, n2, t, re, ce, fl, bc, ed, rd)
- Fields được update real-time từ WebSocket (c, ch, ra, vo, tb, to, bb, bo, fr)

### Business Rules

| Rule | Description |
|------|-------------|
| BR-1 | Nếu mã chưa có trong cache → log warning, skip |
| BR-2 | Update phải atomic (không bị race condition) |
| BR-3 | Mỗi update phải có timestamp |

### Flow Diagram

```
[Kafka Message Received]
        │
        ▼
[Lấy SymbolInfo từ cache]
        │
        ├─→ [Không tìm thấy] → [Log warning] → [Skip]
        │
        ▼
[Tìm thấy] → [Update fields từ message]
        │
        ▼
[Save vào Redis]
        │
        ▼
[Done]
```

### Acceptance Criteria

**MUST:**
- [ ] Data được consume từ Kafka
- [ ] Redis được update với data mới nhất
- [ ] API `/api/v2/market/symbolInfo` trả về data đã aggregate

**MUST NOT:**
- [ ] Không được ảnh hưởng equity data aggregation

### Testing Checklist

- [ ] Happy path: Message được consume và update Redis
- [ ] Verify: API trả về data mới nhất
- [ ] Negative: Message format sai → log warning, skip
- [ ] Regression: Equity aggregation vẫn hoạt động

### Dependencies

- Issue 2 (cần có Kafka messages)
- Issue 1 (cần có SymbolInfo trong cache từ init job)

### Estimated Effort

**3-4 days**

---

## Issue 4: Publish giá phái sinh qua TradeX WebSocket

### Priority: P0 (Blocker)

### As a

Mobile App / Web Client

### I want

Subscribe WebSocket của TradeX để nhận giá phái sinh real-time

### So that

Người dùng thấy giá cập nhật liên tục trên app

### Context

Client hiện tại subscribe:
- `market.quote.{code}` - cho giá cơ sở
- `market.bidoffer.{code}` - cho sổ lệnh cơ sở

Cần thêm channels mới cho phái sinh:
- `market.quote.dr.{code}` - cho giá phái sinh
- `market.bidoffer.dr.{code}` - cho sổ lệnh phái sinh

### Requirements

#### R1: Subscribe Kafka topics

**Topics:**
- `quoteUpdateDR`
- `bidOfferUpdateDR`

#### R2: Publish ra WebSocket channels

**Channel pattern:**
- `market.quote.dr.{code}` - ví dụ: `market.quote.dr.VN30F2501`
- `market.bidoffer.dr.{code}` - ví dụ: `market.bidoffer.dr.VN30F2501`

**Message format (giống equity, thêm field `m`):**

```json
{
  "channel": "market.quote.dr.VN30F2501",
  "data": {
    "s": "VN30F2501",
    "m": "derivatives",
    "c": 1286.0,
    "ch": 13.0,
    "ra": 1.02,
    "vo": 125500,
    "tb": 5100,
    "to": 4600,
    "fr": {"bv": 5200, "sv": 3100},
    "oi": 45100
  }
}
```

### Business Rules

| Rule | Description |
|------|-------------|
| BR-1 | Client phải subscribe channel đúng tên mới nhận được data |
| BR-2 | Message format tương tự equity (để FE dễ xử lý) |
| BR-3 | Nếu không có subscriber → không publish (tiết kiệm resource) |

### Flow Diagram

```
[Kafka Message Received]
        │
        ▼
[Check: Có ai subscribe channel này?]
        │
        ├─→ [Không có] → [Skip]
        │
        ▼
[Có subscriber] → [Format message]
        │
        ▼
[Emit to WebSocket channel]
        │
        ▼
[Client nhận được update]
```

### Acceptance Criteria

**MUST:**
- [ ] Client có thể subscribe `market.quote.dr.{code}`
- [ ] Client nhận được message real-time
- [ ] Message format đúng chuẩn

**MUST NOT:**
- [ ] Không được ảnh hưởng equity WebSocket channels

### Testing Checklist

- [ ] Happy path: Subscribe và nhận được updates
- [ ] Verify: Độ trễ < 1 giây
- [ ] Negative: Unsubscribe → không còn nhận message
- [ ] Regression: Equity channels vẫn hoạt động

### Dependencies

- Issue 3 (cần có data trong Redis/Kafka)

### Estimated Effort

**2-3 days**

---

## Issue 5: Thêm fields phái sinh vào SymbolInfo model

### Priority: P0 (Blocker - cần làm trước tất cả)

### As a

Developer

### I want

Model SymbolInfo có thêm các fields đặc thù phái sinh

### So that

Có thể lưu trữ và truyền tải thông tin phái sinh

### Requirements

#### R1: Thêm fields vào model

**New fields:**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `openInterest` | Long | Số hợp đồng mở | 45000 |
| `baseCode` | String | Mã cơ sở | VN30 |
| `firstTradingDate` | String | Ngày bắt đầu GD | 20250101 |
| `lastTradingDate` | String | Ngày đáo hạn | 20250130 |
| `remainingDays` | Integer | Số ngày còn lại | 15 |
| `theoryPrice` | Double | Giá lý thuyết | 1280.0 |
| `basis` | Double | Basis | -5.0 |

**Existing fields (keep):** s, m, n1, n2, t, re, ce, fl, lq, c, ch, ra, vo, tb, to, bb, bo, fr

#### R2: Backward compatibility

Các fields mới phải:
- Optional (có thể null)
- Không ảnh hưởng equity (equity không có các fields này)

### Acceptance Criteria

**MUST:**
- [ ] Model compile thành công
- [ ] JSON serialization/deserialization hoạt động
- [ ] Equity data vẫn hoạt động bình thường (fields mới = null)

### Testing Checklist

- [ ] Unit test: Serialize/Deserialize
- [ ] Integration test: Save to Redis, read back
- [ ] Regression: Equity symbols không bị ảnh hưởng

### Dependencies

None (nên làm đầu tiên)

### Estimated Effort

**1 day**

---

## Dependencies Graph

```
Issue 5 (Model Update)
    │
    ├────────────────────────────────────┐
    │                                    │
    ▼                                    ▼
Issue 1 (Init Job)              Issue 2 (WebSocket Subscribe)
    │                                    │
    │                                    ▼
    │                            Issue 3 (Aggregate)
    │                                    │
    └────────────┬───────────────────────┘
                 │
                 ▼
         Issue 4 (Publish WS)
```

**Recommended Order:**
1. Issue 5 (Model) - 1 day
2. Issue 1 (Init Job) - 5-6 days (có thể song song với Issue 2)
3. Issue 2 (WebSocket) - 4-5 days (có thể song song với Issue 1)
4. Issue 3 (Aggregate) - 3-4 days
5. Issue 4 (Publish) - 2-3 days

**Total:** ~15-19 days

---

## Definition of Done (cho tất cả issues)

### Code Quality
- [ ] Code được review và approve
- [ ] Unit tests pass (coverage > 80%)
- [ ] Integration tests pass

### Functional
- [ ] Acceptance criteria đạt 100%
- [ ] Manual testing thành công

### Non-Functional
- [ ] Performance không giảm (latency tăng < 5%)
- [ ] Logs đầy đủ để debug
- [ ] Error handling đúng chuẩn

### Regression
- [ ] Tất cả features cơ sở vẫn hoạt động
- [ ] Existing tests vẫn pass

### Documentation
- [ ] Code có comments cho logic phức tạp
- [ ] README/Wiki được update (nếu cần)

---

## Configuration

Tất cả issues cần config sau trong `application.yaml`:

```yaml
derivatives:
  enabled: true              # Master switch
  initJob:
    enabled: true            # Enable in init job
    failSafe: true           # Continue if fails
  websocket:
    enabled: true            # Enable WS subscription
    reconnectOnError: true
  kafka:
    topics:
      quote: quoteUpdateDR
      bidOffer: bidOfferUpdateDR
```

---

## Monitoring & Alerts

### Metrics cần track

| Metric | Threshold | Action |
|--------|-----------|--------|
| DR Init Success Rate | < 95% | Alert Operations |
| DR WS Latency | > 1s | Warning |
| DR Message Parse Error Rate | > 1% | Alert Dev Team |

### Logs cần có

| Event | Level | Message Pattern |
|-------|-------|-----------------|
| Init success | INFO | "Successfully downloaded X derivative symbols" |
| Init failed | WARN | "Derivative download failed, continuing with equity only" |
| WS connected | INFO | "Connected to Lotte DR WebSocket" |
| WS error | ERROR | "Error processing DR message: {error}" |
| Aggregate success | DEBUG | "Updated derivative quote: {code}" |

---

*End of Issue Requirements Document*
