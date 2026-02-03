# TradeX Knowledge Base

> **Purpose:** Tài liệu tổng hợp về cơ chế hoạt động của TradeX Backend  
> **Audience:** Product Manager, Business Analyst  
> **System:** TradeX - Backend của NHSV Pro App

---

## Quick Navigation

| Topic | Document | Description |
|-------|----------|-------------|
| **Market Data** | [market-data-channels.md](./market-data-channels.md) | WebSocket channels, data flow, field mappings |
| **Symbol Info API** | [symbol-info-api.md](./symbol-info-api.md) | API symbolInfo, data aggregation mechanism |
| **Init Job** | [init-job.md](./init-job.md) | Daily init job, symbol_static.json generation |
| **Regular Orders** | [regular-order-api-mapping.md](./regular-order-api-mapping.md) | TradeX ↔ Lotte API mapping for equity/derivatives orders |
| *(coming soon)* | authentication.md | Login, token, session management |
| *(coming soon)* | conditional-orders.md | Stop orders, OCO, Trailing, Bull/Bear |
| *(coming soon)* | notification.md | Push notification, SMS, email |

---

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NHSV PRO APP                                      │
│                    (iOS / Android / Web)                                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
          ┌─────────────────┐             ┌─────────────────┐
          │   REST API      │             │   WebSocket     │
          │  (rest-proxy)   │             │    (ws-v2)      │
          └────────┬────────┘             └────────┬────────┘
                   │                               │
                   ▼                               │
          ┌─────────────────────────────────────────────────┐
          │                    KAFKA                        │
          └─────────────────────────────────────────────────┘
                   │
     ┌─────────────┼─────────────┬─────────────┬─────────────┐
     ▼             ▼             ▼             ▼             ▼
┌─────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐
│   aaa   │ │  order-v2 │ │realtime-v2│ │ market-   │ │notification│
│ (auth)  │ │ (trading) │ │ (market)  │ │ query-v2  │ │  (alert)  │
└─────────┘ └───────────┘ └───────────┘ └───────────┘ └───────────┘
                   │             │             │
                   ▼             ▼             ▼
          ┌─────────────────────────────────────────────────┐
          │           LOTTE SECURITIES API                  │
          │      (Trading + Market Data Provider)           │
          └─────────────────────────────────────────────────┘
```

---

## Core Services

| Service | Tech | Kafka Topic | Role |
|---------|------|-------------|------|
| `rest-proxy` | Node.js | - | API Gateway, routing |
| `aaa` | Node.js | `aaa` | Authentication, authorization |
| `lotte-bridge` | Node.js | `paave-real-trading` | Trading API integration |
| `market-collector-lotte` | Java | - | Collect market data from Lotte WS |
| `realtime-v2` | Java | - | Process real-time data |
| `market-query-v2` | Node.js | `market-query-v2` | Market data queries |
| `ws-v2` | Node.js | - | WebSocket server for clients |
| `order-v2` | Java | `order-v2` | Order processing |
| `notification` | Java | `notification` | Push, SMS, email |

---

## Data Sources

| Provider | Purpose | Protocol |
|----------|---------|----------|
| **Lotte Securities** | Trading API, Market Data | REST, WebSocket |
| **VietStock** | Company info, News, Financial | REST API |
| **FPT** | SMS Gateway (OTP) | REST API |
| **OneSignal** | Push Notifications | REST API |

---

## Environments

| Environment | URL | Purpose |
|-------------|-----|---------|
| **Production** | https://nhsvpro.nhsv.vn | Live users |
| **UAT** | https://tnhsvpro.nhsv.vn | Testing |

---

## Trading Sessions (HOSE)

| Session | Time | Order Types | Description |
|---------|------|-------------|-------------|
| Pre-open | 08:30-09:00 | LO | Đặt lệnh trước giờ |
| **ATO** | 09:00-09:15 | ATO, LO | Khớp lệnh mở cửa |
| Morning | 09:15-11:30 | LO, MP | Phiên liên tục sáng |
| Break | 11:30-13:00 | - | Nghỉ trưa |
| Afternoon | 13:00-14:30 | LO, MP | Phiên liên tục chiều |
| **ATC** | 14:30-14:45 | ATC, LO | Khớp lệnh đóng cửa |
| **PLO** | 14:45-15:00 | PLO | Post Limit Order |

---

## Knowledge Documents

### [Market Data Channels](./market-data-channels.md)

Tất cả về real-time market data:
- WebSocket channels: `market.quote`, `market.bidoffer`
- Data flow từ Lotte → App
- Field mappings & abbreviations
- Session types & control codes
- Business interpretation

**Related:** Khi làm feature liên quan đến bảng giá, biểu đồ, watchlist

---

### [Symbol Info API](./symbol-info-api.md)

Cơ chế tổng hợp thông tin mã chứng khoán:
- API `/api/v2/market/symbol/latest`
- Data aggregation từ nhiều sources (Quote, BidOffer, Extra)
- SymbolInfo data structure
- Redis storage mechanism
- Update flow & business logic

**Related:** Khi làm feature cần lấy thông tin đầy đủ của mã (watchlist, portfolio, search)

---

### [Init Job](./init-job.md)

Cơ chế lấy giá đầu ngày (Daily Init):
- Download symbol list từ Lotte API
- Query giá & thông tin cho tất cả mã
- Lưu vào Redis/MongoDB
- Upload `symbol_static.json` lên MinIO/S3

**Related:** Khi cần hiểu tại sao app không có data khi mới mở, hoặc debug init issues

---

### [Regular Order API Mapping](./regular-order-api-mapping.md)

API mapping cho lệnh thường (Equity & Derivatives):
- Buy/Sell/Cancel/Modify orders
- TradeX → Lotte field mappings
- Request/Response structures
- Lotte DRORD codes (DRORD-029/030/031/032/011)
- Order types & validity codes

**Related:** Khi làm feature đặt lệnh, sửa/hủy lệnh, hoặc debug order flow

---

## How to Use This Knowledge Base

### For PM

1. **Trước khi họp với Dev**: Đọc document liên quan để hiểu context
2. **Khi viết requirement**: Tham khảo field names, data structure
3. **Khi debug issue**: Hiểu data flow để xác định service có vấn đề

### For AI Agent

1. **Đọc `_index.md` trước** để biết có những knowledge nào
2. **Navigate đến document cụ thể** theo topic
3. **Chỉ scan codebase** khi knowledge chưa cover

---

## Document Status

| Document | Status | Last Updated |
|----------|--------|--------------|
| `_index.md` | ✅ Active | 2026-02-03 |
| `market-data-channels.md` | ✅ Active | 2025-01-28 |
| `symbol-info-api.md` | ✅ Active | 2025-01-28 |
| `init-job.md` | ✅ Active | 2025-01-28 |
| `regular-order-api-mapping.md` | ✅ Active | 2026-02-03 |

---

## Contributing

Khi AI phân tích thêm về TradeX:
1. Tạo file mới trong folder này
2. Cập nhật bảng Navigation ở trên
3. Thêm links liên kết giữa các documents

---

*This knowledge base is maintained through AI-assisted analysis of TradeX codebase.*
