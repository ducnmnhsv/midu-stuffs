# Derivatives Integration Plan - TradeX Backend

> **Document ID:** DER-PLAN-001  
> **Version:** 1.0  
> **Created:** 2025-01-30  
> **Status:** Draft  
> **Author:** PM Team

**Tài liệu tham chiếu Lotte DR (mới nhất):**  
- **PDF:** `18032026_Tai_lieu_dac_ta_API2.0_Tsolution-Detail-NHSV_Derivaties.pdf` (18/03/2026)  
- **Đồng bộ trong repo:** `Derivatives/Documentation/[API specs]Lotte_DR.md`  
- Trong PDF: **Mục 4** = Kết nối WebSocket nhận dữ liệu REALTIME thị trường; **4.1** = Cấu trúc dữ liệu REALTIME (Order events `sub/bos.evt.ord.sts.*/`, Account events `sub/bos.evt.acc.inf.*/`, JSON).  
- **Market data WebSocket** (giá, sổ lệnh: `auto.dr.qt`, `auto.dr.bo`, pipe-separated) không mô tả trong mục 4.1 của PDF; định dạng trong doc này (§4.3.2) tham chiếu từ **message thực tế** Lotte gửi.

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Risk Assessment & Mitigation](#2-risk-assessment--mitigation)
3. [Work Breakdown Structure](#3-work-breakdown-structure)
4. [Technical Requirements](#4-technical-requirements)
   - 4.1 [Init Job Enhancement](#41-init-job-enhancement)
   - 4.2 [SymbolInfo API Enhancement](#42-symbolinfo-api-enhancement)
   - 4.3 [Real-time WebSocket Integration](#43-real-time-websocket-integration)
5. [Data Model Changes](#5-data-model-changes)
6. [API Specifications](#6-api-specifications)
7. [Testing Strategy](#7-testing-strategy)
8. [Rollout Plan](#8-rollout-plan)

---

## 1. Executive Summary

### 1.1 Objective

Bổ sung giao dịch phái sinh (Derivatives) vào hệ thống TradeX đang chạy ổn định với cơ sở (Equity). Đảm bảo backward compatibility và zero downtime cho hệ thống hiện tại.

### 1.2 Scope

| Item | Description | Priority |
|------|-------------|----------|
| **Init Job** | Lấy danh sách & thông tin mã phái sinh vào `symbol_static.json` | P0 |
| **SymbolInfo API** | Bổ sung mã phái sinh vào `/api/v2/market/symbolInfo` | P0 |
| **WebSocket Realtime** | Nhận giá phái sinh từ Lotte WS và publish ra client | P0 |
| **Order Integration** | Tích hợp đặt lệnh phái sinh (out of scope cho phase này) | P1 |

### 1.3 Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Market Identifier (phái sinh) | Field `"m": "INDEX"` \| `"BOND"` | Cùng field `m` như cơ sở (HOSE/HNX/UPCOM); FE dùng `m` để phân biệt sàn (cơ sở) và loại phái sinh (INDEX/BOND) |
| Symbol Type | `FUTURES` | Theo enum hiện có trong hệ thống |
| WebSocket Channel | `market.quote.dr.{code}` | Tách biệt khỏi equity channels |

---

## 2. Risk Assessment & Mitigation

### 2.1 Critical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Init Job failure** ảnh hưởng cơ sở | High | Medium | Tách riêng logic lấy derivatives, try-catch không ảnh hưởng flow chính |
| **Redis key conflict** | High | Low | Sử dụng prefix `DR_` hoặc field `m` để phân biệt |
| **WebSocket channel conflict** | High | Low | Sử dụng channel pattern riêng cho derivatives |
| **Lotte API không ổn định** | Medium | Medium | Retry mechanism, fallback to cache |
| **Client không hiểu format mới** | Medium | Low | Versioning, feature flag |

### 2.2 Mitigation Strategy - ISOLATION PRINCIPLE

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      ISOLATION ARCHITECTURE                                  │
│                                                                             │
│   ┌─────────────────────────────┐   ┌─────────────────────────────┐        │
│   │     EQUITY (Existing)       │   │     DERIVATIVES (New)       │        │
│   ├─────────────────────────────┤   ├─────────────────────────────┤        │
│   │ • Lotte API: /api/v2/*      │   │ • Lotte API: /tuxsvc/dr/*   │        │
│   │ • WS: auto.qt, auto.bo      │   │ • WS: auto.dr.qt, auto.dr.bo│        │
│   │ • Redis: SYMBOL_INFO        │   │ • Redis: SYMBOL_INFO (m=dr) │        │
│   │ • Type: STOCK, ETF, CW...   │   │ • Type: FUTURES             │        │
│   └─────────────────────────────┘   └─────────────────────────────┘        │
│                                                                             │
│   → Nếu Derivatives fail → Equity vẫn hoạt động bình thường                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 Rollback Plan

| Phase | Condition | Action |
|-------|-----------|--------|
| Init Job | Derivatives API fail | Skip derivatives, log warning, continue equity |
| WebSocket | DR channel error | Disable DR subscription, equity unaffected |
| API | DR symbols not found | Return empty array for DR, equity normal |

---

## 3. Work Breakdown Structure

### 3.1 Phase 1: Init Job Enhancement (P0)

| Task ID | Task | Service | Estimate | Dependencies |
|---------|------|---------|----------|--------------|
| INIT-001 | Tạo Lotte DR API client | market-collector-lotte | 2d | - |
| INIT-002 | Lấy danh sách mã phái sinh | market-collector-lotte | 1d | INIT-001 |
| INIT-003 | Lấy thông tin giá mã phái sinh | market-collector-lotte | 1d | INIT-001 |
| INIT-004 | Merge vào symbol_static.json | market-collector-lotte | 1d | INIT-002, INIT-003 |
| INIT-005 | Update SymbolInfo model | tradex-common-java | 1d | - |
| INIT-006 | Unit test & Integration test | market-collector-lotte | 2d | INIT-004 |

### 3.2 Phase 2: Data Aggregation (P0)

> **Lưu ý:** `market-query-v2` KHÔNG cần thay đổi. Data được aggregate bởi `realtime-v2`.

| Task ID | Task | Service | Estimate | Dependencies |
|---------|------|---------|----------|--------------|
| AGG-001 | Tạo DerivativeQuoteConsumer | realtime-v2 | 1d | INIT-005 |
| AGG-002 | Tạo DerivativeBidOfferConsumer | realtime-v2 | 1d | AGG-001 |
| AGG-003 | Aggregate data vào Redis | realtime-v2 | 0.5d | AGG-002 |
| AGG-004 | Unit test | realtime-v2 | 1d | AGG-003 |

### 3.3 Phase 3: WebSocket Integration (P0)

| Task ID | Task | Service | Estimate | Dependencies |
|---------|------|---------|----------|--------------|
| WS-001 | Subscribe Lotte DR channels | market-collector-lotte | 2d | - |
| WS-002 | Parse DR message format | market-collector-lotte | 1d | WS-001 |
| WS-003 | Publish to Kafka | market-collector-lotte | 1d | WS-002 |
| WS-004 | Update realtime-v2 consumer | realtime-v2 | 1d | WS-003 |
| WS-005 | Update ws-v2 publisher | ws-v2 | 1d | WS-004 |
| WS-006 | E2E test | All | 2d | WS-005 |

---

## 4. Technical Requirements

### 4.1 Init Job Enhancement

#### 4.1.1 Yêu cầu chức năng

**FR-INIT-001**: Hệ thống PHẢI lấy danh sách mã phái sinh từ Lotte API khi chạy daily init job.

**FR-INIT-002**: Hệ thống PHẢI lấy thông tin giá của từng mã phái sinh từ Lotte API.

**FR-INIT-003**: Hệ thống PHẢI merge mã phái sinh vào `symbol_static.json` với identifier rõ ràng.

**FR-INIT-004**: Nếu việc lấy derivatives FAIL, hệ thống PHẢI tiếp tục init job cho equity bình thường.

#### 4.1.2 Lotte APIs

**API 1: Lấy danh sách mã phái sinh**

```yaml
API_ID: DRMKT-001
Endpoint: /tuxsvc/market/dr/stock-board
Method: POST/GET
Authentication: OAuth2 + API KEY
Request: {}  # Empty

Response:
  success: boolean
  error_code: "0000" | "1005"
  data_list:
    list_items:
      - code: "VN30F2501"           # Mã hợp đồng
        last: 1285.5                 # Giá hiện tại
        change: 12.5                 # Thay đổi
        change_rate: 0.98            # % thay đổi
        vol: 125000                  # Khối lượng
        ceiling: 1350.0              # Giá trần
        floor: 1220.0                # Giá sàn
        ref_price: 1273.0            # Giá tham chiếu
        open: 1275.0                 # Giá mở cửa
        high: 1290.0                 # Giá cao nhất
        low: 1270.0                  # Giá thấp nhất
        bid1, bid2, bid3: Double     # Giá mua 1,2,3
        offer1, offer2, offer3: Double # Giá bán 1,2,3
        bid1_size, bid2_size, bid3_size: Long
        offer1_size, offer2_size, offer3_size: Long
        oi: 45000                    # Open Interest
        exp_date: 20250130           # Ngày đáo hạn
        control_code: "O"            # Trạng thái thị trường
        foreign_buy_vol: 5000
        foreign_sell_vol: 3000
```

**API 2: Thông tin chi tiết mã phái sinh**

```yaml
API_ID: DRMKT-002
Endpoint: /tuxsvc/market/dr/stock-price
Method: GET
Authentication: OAuth2 + API KEY

Request:
  code: "VN30F2501"  # Mã hợp đồng

Response:
  success: boolean
  error_code: "0000" | "1005"
  data_list:
    code: "VN30F2501"
    name: "VN30 Future Jan 2025"
    ceiling, floor, open, high, low, last, change: Double
    ref_price, average_price: Double
    volume, amount: Long
    bid, offer: Double
    bid_offer_list: [...]           # Sổ lệnh 10 bước
    total_vis_bid_size, total_vis_offer_size: Long
    open_interest: Long
    base_code: "VN30"               # Mã cơ sở
    first_trd_date: "20250101"      # Ngày giao dịch đầu tiên
    end_trd_date: "20250130"        # Ngày đáo hạn
    remain_date: 15                  # Số ngày còn lại
    theory_price, theory_basis: Double
    foreign_buy_vol, foreign_sell_vol: Long
```

#### 4.1.2.1 Phân loại Derivatives theo mã hợp đồng (41I / 41B)

Init job đầu ngày có **hai loại** hợp đồng phái sinh; FE cần đọc được để hiển thị đúng nhóm/index name.

**Thiết kế:** Dùng **cùng field `m`** (market) như cơ sở: với **cơ sở** `m` = HOSE | HNX | UPCOM (sàn); với **phái sinh** `m` = **INDEX** | **BOND** (loại phái sinh). FE đã dùng `m` để phân biệt HOSE/UPCOM/HNX nên có thể dùng tiếp `m` cho INDEX/BOND mà không cần field riêng.

| Loại hợp đồng | Quy tắc mã | Ký tự thứ 3 | Giá trị `m` | Ghi chú |
|---------------|------------|-------------|-------------|---------|
| **HĐ tương lai chỉ số phái sinh** | `41Ixxxxxx` | **I** (Index) | `"INDEX"` | VN30F, VN30, v.v. |
| **HĐTL trái phiếu chính phủ** | `41Bxxxxxx` | **B** (Bond) | `"BOND"` | Trái phiếu chính phủ |

**Quy tắc xác định (Init Job và mọi nơi trả symbol):**

- Khi merge từng mã phái sinh vào `symbol_static.json` / SymbolInfo / API response, **gán** `m` từ mã hợp đồng: **ký tự thứ 3** của mã: **I** → `m = "INDEX"`, **B** → `m = "BOND"`. Trường hợp khác → `m = null` hoặc `"OTHER"` (mở rộng sau).

**Field trả về cho FE:** **`m`** (market) — với phái sinh là `"INDEX"` hoặc `"BOND"`. FE dùng `m` để:

- Hiển thị **index name** (ví dụ: INDEX → PS/DR; BOND → TPCP/GB tùy quy ước FE).
- Lọc/nhóm danh sách mã (cơ sở: HOSE/HNX/UPCOM; phái sinh: INDEX/BOND). Filter "tất cả phái sinh" = `t === "FUTURES"` hoặc `m in ["INDEX","BOND"]`.

**Ví dụ:**

| Mã (`s`) | Ký tự thứ 3 | `m` |
|----------|-------------|-----|
| 41Ixxxxxx (VN30F...) | I | `"INDEX"` |
| 41Bxxxxxx | B | `"BOND"` |

#### 4.1.3 Implementation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      INIT JOB ENHANCED FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    LotteApiSymbolInfoService.downloadSymbol()               │
│                                                                             │
│   EXISTING FLOW (Keep unchanged):                                           │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│   │ symbolNames │  │ symbolPrices│  │ bestBidAsks │  │ indexList   │       │
│   │ (Equity)    │  │ (Equity)    │  │ (Equity)    │  │ (Equity)    │       │
│   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘       │
│          └────────────────┴────────────────┴────────────────┘              │
│                                    │                                        │
│                                    ▼                                        │
│                    ┌──────────────────────────────┐                        │
│                    │   List<SymbolInfo> EQUITY    │                        │
│                    │   (~2000 mã: Stock, ETF...)  │                        │
│                    └──────────────┬───────────────┘                        │
│                                   │                                         │
│   NEW FLOW (Additive):            │                                        │
│   ┌─────────────────────────────────────────────────────────────┐          │
│   │ TRY:                                                         │          │
│   │   ┌────────────────────┐    ┌────────────────────┐          │          │
│   │   │ /dr/stock-board    │    │ /dr/stock-price    │          │          │
│   │   │ (List derivatives) │    │ (Each derivative)  │          │          │
│   │   └─────────┬──────────┘    └─────────┬──────────┘          │          │
│   │             └────────────────────────┬┘                     │          │
│   │                                      ▼                      │          │
│   │             ┌──────────────────────────────────────┐        │          │
│   │             │   List<SymbolInfo> DERIVATIVES       │        │          │
│   │             │   (~4-8 mã: VN30F2501, VN30F2502...) │        │          │
│   │             │   WITH m="INDEX" | "BOND"            │        │          │
│   │             └──────────────┬───────────────────────┘        │          │
│   │                            │                                │          │
│   │ CATCH Exception:           │                                │          │
│   │   Log.warn("Derivatives init failed, continuing...")        │          │
│   │   Return empty list                                         │          │
│   └─────────────────────────────────────────────────────────────┘          │
│                                    │                                        │
│                                    ▼                                        │
│          ┌─────────────────────────────────────────────────┐               │
│          │         MERGE: EQUITY + DERIVATIVES             │               │
│          │         allSymbols = equityList + derivativeList│               │
│          └──────────────────────┬──────────────────────────┘               │
│                                 │                                           │
└─────────────────────────────────│───────────────────────────────────────────┘
                                  │
                                  ▼
                         [Continue existing flow]
                         Save to Redis, MongoDB, MinIO
```

#### 4.1.4 Triển khai Init Job (market-collector-lotte)

Gọi DRMKT-001 (stock-board) và DRMKT-002 (stock-price); merge danh sách symbol; với mỗi mã phái sinh gán **`m`** = "INDEX" hoặc "BOND" theo quy tắc 41I/41B (4.1.2.1). Chi tiết implementation xem Spec/Issue riêng.


#### 4.2.1 Nguyên tắc quan trọng

> **QUAN TRỌNG:** FE hiện tại đang sử dụng WebSocket để nhận giá real-time, KHÔNG sử dụng `/api/v2/market/symbol/latest`. 
> Để hạn chế thay đổi ở FE, cơ chế cho phái sinh PHẢI giống với cơ sở hiện tại.

#### 4.2.2 Cơ chế hoạt động (giữ nguyên cho cả cơ sở và phái sinh)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CƠ CHẾ HIỆN TẠI (Equity) - GIỮ NGUYÊN                    │
└─────────────────────────────────────────────────────────────────────────────┘

1. App Start:
   └─→ Download symbol_static.json (danh sách mã + thông tin tĩnh)
   
2. Real-time:
   └─→ Subscribe WebSocket channels: market.quote.{code}, market.bidoffer.{code}
   └─→ Nhận updates, merge vào local cache
   
3. API symbolInfo (nếu cần):
   └─→ GET /api/v2/market/symbolInfo?symbolList=[...]
   └─→ Data lấy từ Redis (đã được aggregate từ WebSocket)

┌─────────────────────────────────────────────────────────────────────────────┐
│                    CƠ CHẾ MỚI (Derivatives) - TƯƠNG TỰ                      │
└─────────────────────────────────────────────────────────────────────────────┘

1. App Start:
   └─→ Download symbol_static.json (bao gồm cả mã phái sinh với m="INDEX" | "BOND")
   
2. Real-time:
   └─→ Subscribe WebSocket channels: market.quote.dr.{code}, market.bidoffer.dr.{code}
   └─→ Nhận updates, merge vào local cache
   
3. API symbolInfo (nếu cần):
   └─→ GET /api/v2/market/symbolInfo?symbolList=[VN30F2501,...]
   └─→ Data lấy từ Redis (đã được aggregate từ WebSocket phái sinh)
```

#### 4.2.3 Yêu cầu chức năng

**FR-API-001**: API `/api/v2/market/symbolInfo` PHẢI hoạt động cho cả mã cơ sở và phái sinh.

**FR-API-002**: Data phái sinh trong API PHẢI được tổng hợp từ WebSocket (giống cơ sở).

**FR-API-003**: Response format cho phái sinh PHẢI giữ nguyên tất cả fields của cơ sở, chỉ thêm fields đặc thù phái sinh. Field **`m`** (market) với phái sinh = `"INDEX"` hoặc `"BOND"` (cùng cách FE dùng `m` cho HOSE/HNX/UPCOM).

**FR-API-004**: Với mọi mã derivatives, API **SymbolInfo** (và symbol_static, symbol/latest) PHẢI trả **`m`** = `"INDEX"` hoặc `"BOND"` (suy từ mã 41I / 41B) để FE hiển thị đúng index name (PS/DR vs TPCP/GB).

**FR-API-005**: FE KHÔNG cần thay đổi cơ chế gọi API.

#### 4.2.4 Phân tích Response Format Cơ sở (Baseline)

> **Reference:** Response thực tế từ API `/api/v2/market/symbolInfo` cho mã TCB (STOCK)

**Sample Response (Equity - TCB):**
```json
{
  "s": "TCB",
  "t": "STOCK",
  "ti": "074500",
  "bot": "074500",
  "o": 34850,
  "h": 35900,
  "l": 34800,
  "c": 35900,
  "ch": 1000,
  "ra": 2.8653,
  "vo": 11603200,
  "va": 409488665000,
  "mv": 300,
  "a": 35300,
  "mb": "ASK",
  "tb": 3525300,
  "to": 8077900,
  "tor": 0.1637,
  "fr": {"bv": 47000, "sv": 51500, "cr": 99, "tr": 1597139381},
  "hly": [{"h": 42500, "l": 22300, "hd": null, "ld": null}],
  "ep": 35900,
  "ce": 37300,
  "fl": 32500,
  "re": 34900,
  "lq": 7086240414,
  "m": "HOSE",
  "n1": "Ngân hàng Thương mại Cổ phần Kỹ Thương Việt Nam",
  "n2": "Vietnam Technological and Commercial Joint Stock Bank",
  "lt": "170000",
  "bb": [{"p": 35700, "v": 300, "c": null}, {"p": 35600, "v": 95000, "c": null}, {"p": 35550, "v": 80000, "c": null}],
  "bo": [{"p": 35900, "v": 195400, "c": null}, {"p": 35950, "v": 93300, "c": null}, {"p": 36000, "v": 221100, "c": null}],
  "mc": 247309790448600,
  "ss": "CLOSED"
}
```

**Bảng chi tiết tất cả Fields của Cơ sở:**

| # | Field | Full Name | Type | Description (Vietnamese) | Example |
|---|-------|-----------|------|--------------------------|---------|
| **IDENTIFICATION** |||||
| 1 | `s` | symbol | string | Mã chứng khoán | `"TCB"` |
| 2 | `t` | type | string | Loại sản phẩm: STOCK/ETF/CW/INDEX | `"STOCK"` |
| 3 | `m` | market | string | Sàn giao dịch: HOSE/HNX/UPCOM | `"HOSE"` |
| 4 | `n1` | name1 | string | Tên tiếng Việt | `"Ngân hàng TMCP Kỹ Thương VN"` |
| 5 | `n2` | name2 | string | Tên tiếng Anh | `"Vietnam Technological..."` |
| **TIME** |||||
| 6 | `ti` | time | string | Thời gian cập nhật quote (UTC, HHmmss) | `"074500"` |
| 7 | `bot` | bidOfferTime | string | Thời gian cập nhật sổ lệnh (UTC) | `"074500"` |
| 8 | `lt` | lastTradingTime | string | Thời gian giao dịch cuối (UTC) | `"170000"` |
| 9 | `ss` | session | string | Phiên giao dịch: ATO/LO/ATC/PLO/CLOSED | `"CLOSED"` |
| **PRICE DATA** |||||
| 10 | `o` | open | number | Giá mở cửa | `34850` |
| 11 | `h` | high | number | Giá cao nhất trong ngày | `35900` |
| 12 | `l` | low | number | Giá thấp nhất trong ngày | `34800` |
| 13 | `c` | current/close | number | Giá hiện tại (giá khớp gần nhất) | `35900` |
| 14 | `ch` | change | number | Thay đổi giá so với tham chiếu | `1000` |
| 15 | `ra` | rate | number | % thay đổi giá | `2.8653` |
| 16 | `a` | averagePrice | number | Giá trung bình trong ngày | `35300` |
| **REFERENCE PRICES** |||||
| 17 | `ce` | ceilingPrice | number | Giá trần | `37300` |
| 18 | `fl` | floorPrice | number | Giá sàn | `32500` |
| 19 | `re` | referencePrice | number | Giá tham chiếu | `34900` |
| **VOLUME DATA** |||||
| 20 | `vo` | volume | number | Khối lượng giao dịch trong ngày | `11603200` |
| 21 | `va` | value | number | Giá trị giao dịch (VND) | `409488665000` |
| 22 | `mv` | matchingVolume | number | KL khớp lệnh cuối | `300` |
| 23 | `mb` | matchedBy | string | Bên chủ động khớp: ASK/BID | `"ASK"` |
| 24 | `tor` | turnoverRate | number | Tỷ lệ quay vòng (vo/lq) | `0.1637` |
| **BID/OFFER DATA** |||||
| 25 | `tb` | totalBidVolume | number | Tổng KL dư mua | `3525300` |
| 26 | `to` | totalOfferVolume | number | Tổng KL dư bán | `8077900` |
| 27 | `bb` | bestBids | array | Sổ lệnh bên mua (3-10 levels) | `[{p,v,c},...]` |
| 28 | `bo` | bestOffers | array | Sổ lệnh bên bán (3-10 levels) | `[{p,v,c},...]` |
| 29 | `bb[].p` | price | number | Giá mua | `35700` |
| 30 | `bb[].v` | volume | number | KL mua | `300` |
| 31 | `bb[].c` | change | number\|null | Thay đổi KL so với trước | `null` |
| **EXPECTED DATA (ATO/ATC)** |||||
| 32 | `ep` | expectedPrice | number | Giá dự kiến khớp (ATO/ATC) | `35900` |
| **FOREIGNER DATA** |||||
| 33 | `fr` | foreigner | object | Thông tin ĐTNN | `{bv,sv,cr,tr}` |
| 34 | `fr.bv` | buyVolume | number | KL mua ĐTNN | `47000` |
| 35 | `fr.sv` | sellVolume | number | KL bán ĐTNN | `51500` |
| 36 | `fr.cr` | currentRoom | number | Room NN còn lại | `99` |
| 37 | `fr.tr` | totalRoom | number | Tổng room NN | `1597139381` |
| **STATIC DATA** |||||
| 38 | `lq` | listedQuantity | number | KL niêm yết/đăng ký giao dịch | `7086240414` |
| 39 | `mc` | marketCap | number | Vốn hóa thị trường (VND) | `247309790448600` |
| **HISTORICAL DATA** |||||
| 40 | `hly` | highLowYearly | array | Giá cao/thấp theo năm | `[{h,l,hd,ld}]` |
| 41 | `hly[].h` | high | number | Giá cao nhất năm | `42500` |
| 42 | `hly[].l` | low | number | Giá thấp nhất năm | `22300` |
| 43 | `hly[].hd` | highDate | string\|null | Ngày đạt giá cao nhất | `null` |
| 44 | `hly[].ld` | lowDate | string\|null | Ngày đạt giá thấp nhất | `null` |

#### 4.2.5 Response Format cho Derivatives

**Nguyên tắc thiết kế:**
1. **GIỮ NGUYÊN** tất cả fields của cơ sở (đảm bảo FE không cần thay đổi logic xử lý)
2. **THÊM** fields đặc thù phái sinh với prefix rõ ràng
3. **ĐIỀU CHỈNH** giá trị một số fields cho phù hợp ngữ cảnh phái sinh

**Sample Response (Derivatives - VN30F2502):**
```json
{
  "s": "VN30F2502",
  "t": "FUTURES",
  "ti": "074500",
  "bot": "074500",
  "o": 1275.0,
  "h": 1290.0,
  "l": 1270.0,
  "c": 1285.5,
  "ch": 12.5,
  "ra": 0.98,
  "vo": 125000,
  "va": 160000000000,
  "mv": 500,
  "a": 1280.0,
  "mb": "BID",
  "tb": 5000,
  "to": 4500,
  "tor": null,
  "fr": {"bv": 5000, "sv": 3000, "cr": null, "tr": null},
  "hly": null,
  "ep": 1285.5,
  "ce": 1350.0,
  "fl": 1220.0,
  "re": 1273.0,
  "lq": null,
  "m": "INDEX",
  "n1": "HĐ Tương lai VN30 Tháng 02/2025",
  "n2": "VN30 Index Futures Feb 2025",
  "lt": "150000",
  "bb": [{"p": 1285.0, "v": 1200, "c": null}, {"p": 1284.5, "v": 800, "c": null}, {"p": 1284.0, "v": 1500, "c": null}],
  "bo": [{"p": 1285.5, "v": 1000, "c": null}, {"p": 1286.0, "v": 900, "c": null}, {"p": 1286.5, "v": 1100, "c": null}],
  "mc": null,
  "ss": "LO",
  
  "oi": 45000,
  "bc": "VN30",
  "ftd": "20250101",
  "ed": "20250227",
  "rd": 28,
  "tp": 1284.0,
  "bs": 1.5
}
```

**Bảng so sánh Fields: Cơ sở vs Phái sinh:**

| Field | Cơ sở (Equity) | Phái sinh (Derivatives) | Ghi chú |
|-------|----------------|-------------------------|---------|
| **IDENTIFICATION** ||||
| `s` | `"TCB"` | `"VN30F2502"` | Mã hợp đồng |
| `t` | `"STOCK"` | `"FUTURES"` | Type mới |
| `m` | `"HOSE"` | `"INDEX"` \| `"BOND"` | Cơ sở: sàn; Phái sinh: loại (cùng field m) |
| `n1` | Tên công ty VN | Tên hợp đồng VN | |
| `n2` | Tên công ty EN | Tên hợp đồng EN | |
| **PRICE DATA** ||||
| `o,h,l,c` | VND (đồng) | Điểm (point) | Đơn vị khác |
| `ch,ra` | Giữ nguyên | Giữ nguyên | |
| `a` | Giữ nguyên | Giữ nguyên | |
| `ce,fl,re` | VND | Điểm | Đơn vị khác |
| **VOLUME DATA** ||||
| `vo,va,mv` | Giữ nguyên | Giữ nguyên | va tính theo hệ số nhân |
| `mb,tb,to` | Giữ nguyên | Giữ nguyên | |
| `tor` | % | `null` | Không áp dụng cho phái sinh |
| **BID/OFFER** ||||
| `bb,bo` | Giữ nguyên | Giữ nguyên | Giá = điểm |
| **FOREIGNER** ||||
| `fr.bv,fr.sv` | Giữ nguyên | Giữ nguyên | |
| `fr.cr,fr.tr` | Room NN | `null` | Không có room cho phái sinh |
| **STATIC DATA** ||||
| `lq` | KL niêm yết | `null` | Không áp dụng |
| `mc` | Vốn hóa | `null` | Không áp dụng |
| `hly` | Giá năm | `null` | Không áp dụng (hợp đồng ngắn hạn) |
| **DERIVATIVES-SPECIFIC (MỚI)** ||||
| `oi` | ❌ | Open Interest | Số hợp đồng mở |
| `bc` | ❌ | Base Code | Mã cơ sở (VN30) |
| `ftd` | ❌ | First Trading Date | Ngày GD đầu tiên (yyyyMMdd) |
| `ed` | ❌ | Expiry Date | Ngày đáo hạn (yyyyMMdd) |
| `rd` | ❌ | Remaining Days | Số ngày còn lại |
| `tp` | ❌ | Theory Price | Giá lý thuyết |
| `bs` | ❌ | Basis | Chênh lệch Futures - Spot |

#### 4.2.6 Chi tiết Fields Đặc thù Phái sinh

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `oi` | openInterest | number | Số lượng hợp đồng mở (chưa đóng) | `45000` |
| `bc` | baseCode | string | Mã chỉ số cơ sở | `"VN30"` |
| `ftd` | firstTradingDate | string | Ngày giao dịch đầu tiên (yyyyMMdd) | `"20250101"` |
| `ed` | expiryDate | string | Ngày đáo hạn hợp đồng (yyyyMMdd) | `"20250227"` |
| `rd` | remainingDays | number | Số ngày còn lại đến đáo hạn | `28` |
| `tp` | theoryPrice | number | Giá lý thuyết do sở tính | `1284.0` |
| `bs` | basis | number | Chênh lệch = Futures - Spot (điểm) | `1.5` |

#### 4.2.7 Data Flow cho SymbolInfo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     DERIVATIVES DATA AGGREGATION                            │
│                   (Giống cơ chế equity hiện tại)                            │
└─────────────────────────────────────────────────────────────────────────────┘

Lotte DR WebSocket
    │
    ├─► auto.dr.qt (Quote)      ──► Kafka: quoteUpdateDR
    │                                    │
    │                                    ▼
    │                              realtime-v2
    │                                    │
    │                                    ▼
    │                           ┌────────────────────┐
    │                           │  Update SymbolInfo │
    │                           │  trong Redis       │
    │                           │  (aggregate data)  │
    │                           └────────┬───────────┘
    │                                    │
    └─► auto.dr.bo (BidOffer)   ──► Kafka: bidOfferUpdateDR
                                         │
                                         ▼
                                   realtime-v2
                                         │
                                         ▼
                                ┌────────────────────┐
                                │  Update SymbolInfo │
                                │  trong Redis       │
                                │  (aggregate data)  │
                                └────────┬───────────┘
                                         │
                                         ▼
                            ┌─────────────────────────────┐
                            │   Redis: SYMBOL_INFO        │
                            │   VN30F2502: {              │
                            │     s, m, t, c, ch, ra,     │
                            │     bb, bo, tb, to,         │
                            │     oi, bc, ed, rd, tp, bs  │
                            │   }                         │
                            └─────────────┬───────────────┘
                                          │
                    ┌─────────────────────┴─────────────────────┐
                    ▼                                           ▼
         ┌──────────────────────┐                 ┌──────────────────────┐
         │  /api/v2/market/     │                 │  WebSocket ws-v2     │
         │  symbolInfo          │                 │  market.quote.dr.*   │
         │  (Read from Redis)   │                 │  market.bidoffer.dr.*│
         └──────────────────────┘                 └──────────────────────┘
```

#### 4.2.8 Thay đổi cần thiết ở Backend

| Service | Thay đổi | Mô tả |
|---------|----------|-------|
| realtime-v2 | Consumer mới | Consume `quoteUpdateDR`, `bidOfferUpdateDR` từ Kafka |
| realtime-v2 | Update logic | Aggregate data vào SymbolInfo trong Redis, bao gồm fields đặc thù DR |
| market-query-v2 | Không thay đổi | Đọc từ Redis như bình thường |

**Lưu ý:** market-query-v2 KHÔNG cần thay đổi vì nó chỉ đọc từ Redis. Data đã được aggregate bởi realtime-v2.

### 4.3 Real-time WebSocket Integration

#### 4.3.1 Yêu cầu chức năng

**FR-WS-001**: Hệ thống PHẢI subscribe Lotte DR WebSocket channels khi service start.

**FR-WS-002**: Hệ thống PHẢI parse message format của DR và transform về TradeX format.

**FR-WS-003**: Hệ thống PHẢI publish giá derivatives qua TradeX WebSocket với channel pattern riêng.

**FR-WS-004**: Nếu DR WebSocket fail, hệ thống equity WebSocket PHẢI không bị ảnh hưởng.

#### 4.3.2 Lotte DR WebSocket Channels

**Future Quote Channel (`auto.dr.qt`)**

**Subscribe:** `sub/pro.pub.auto.dr.qt./41I1FA000` (code thay đổi theo mã hợp đồng).

**Message Format (pipe-separated) — Spec [0]–[33]:**

| Index | Field (Spec) | Ví dụ trong spec | Ghi chú |
|-------|--------------|------------------|--------|
| [0] | service | "auto.dr.qt.41I1FA000" | Dạng `auto.dr.qt.{code}` |
| [1] | success | "1" | 1 = success, 0 = fail |
| [2] | time | "103025" | HHmmss |
| [3] | code | "41I1FA000" | Mã hợp đồng |
| [4] | highTime | "094532" | HHmmss |
| [5] | lowTime | "101245" | HHmmss |
| [6] | open.value | "1275.0" | Giá mở cửa (điểm) |
| [7] | open.type | "2" | Mã loại giá |
| [8] | high.value | "1290.0" | Giá cao nhất |
| [9] | high.type | "1" | Mã loại giá |
| [10] | low.value | "1270.0" | Giá thấp nhất |
| [11] | low.type | "4" | Mã loại giá |
| [12] | last.value | "1285.5" | Giá hiện tại (khớp cuối) |
| [13] | last.type | "2" | Mã loại giá |
| [14] | change.value | "12.5" | Thay đổi so với tham chiếu |
| [15] | changeRate | "0.98" | % thay đổi |
| [16] | averagePrice | "1280.0" | Giá trung bình |
| [17] | referencePrice | "1273.0" | Giá tham chiếu |
| [18] | value | "16000000000" | Giá trị GD |
| [19] | volume | "125000" | Khối lượng GD |
| [20] | matchedVolume.value | "500" | KL khớp lệnh cuối |
| [21] | matchedVolume.type | "66" | **Map TradeX:** 66 → BUY (BID), 83 → SELL (ASK) |
| [22] | bid.value | "1285.0" | Giá bid 1 |
| [23] | bid.type | "2" | Mã loại giá |
| [24] | offer.value | "1285.5" | Giá offer 1 |
| [25] | offer.type | "2" | Mã loại giá |
| [26] | bid_size | "1200" | KL bid 1 |
| [27] | offer_size | "1000" | KL offer 1 |
| [28] | total_bid_size | "5000" | Tổng KL dư mua |
| [29] | total_offer_size | "4500" | Tổng KL dư bán |
| [30] | total_bid_count | "150" | Số lệnh mua |
| [31] | total_offer_count | "120" | Số lệnh bán |
| [32] | foreignerBuySize | "5000" | ĐTNN mua |
| [33] | foreignerSellSize | "3000" | ĐTNN bán |

**Mapping Spec ↔ Real message** (đối chiếu với message thực tế đã gửi):

| Index | Field (Spec) | Giá trị trong real message |
|-------|--------------|----------------------------|
| [0] | service | auto.dr.qt.41I1FA000 |
| [1] | success | 1 |
| [2] | time | 90000 |
| [3] | code | 41I1FA000 |
| [4] | highTime | 90000 |
| [5] | lowTime | 90000 |
| [6] | open.value | 1116.00 |
| [7] | open.type | 5 |
| [8] | high.value | 1116.00 |
| [9] | high.type | 5 |
| [10] | low.value | 1116.00 |
| [11] | low.type | 5 |
| [12] | last.value | 1116.00 |
| [13] | last.type | 5 |
| [14] | change.value | -84.00 |
| [15] | changeRate | -7.00 |
| [16] | averagePrice | 0.00 |
| [17] | referencePrice | 1200.00 |
| [18] | value | 111.6 |
| [19] | volume | 1 |
| [20] | matchedVolume.value | 1.0 |
| [21] | matchedVolume.type | 66 → map **BID** (BUY) |
| [22] | bid.value | 1116.00 |
| [23] | bid.type | 5 |
| [24] | offer.value | 1200.00 |
| [25] | offer.type | 3 |
| [26] | bid_size | 530 |
| [27] | offer_size | 506 |
| [28] | total_bid_size | 1045 |
| [29] | total_offer_size | 1055 |
| [30] | total_bid_count | 1015 |
| [31] | total_offer_count | 1010 |
| [32] | foreignerBuySize | 0 |
| [33] | foreignerSellSize | 0 |

**Real message (raw) — tham chiếu:**

```
auto.dr.qt.41I1FA000|1|90000|41I1FA000|90000|90000|1116.00|5|1116.00|5|1116.00|5|1116.00|5|-84.00|-7.00|0.00|1200.00|111.6|1|1.0|66|1116.00|5|1200.00|3|530|506|1045|1055|1015|1010|0|0|0|0|0.00|0.00|0.00|0.00|0.00|0.00
```

**Fields [34]–[41]:** Message thực tế có thêm 8 field (0, 0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00). Chưa có trong spec chính thức — bỏ qua hoặc lưu raw cho đến khi Lotte xác nhận.

**Future Bid/Offer Channel (`auto.dr.bo`)**

Định dạng dưới đây **tham chiếu từ message thực tế** Lotte gửi (pipe-separated). Ưu tiên bảng này khi implement parser.

**Sample message thực tế (raw):**

```
auto.dr.bo.41I1FA000|1|104652|41I1FA000|O|1116.0|5|1125.0|5|5|1200.0|3|3614|1125.0|5|5|1200.0|3|3614|1124.0|5|4|0.0|3|0|1123.0|5|3|0.0|3|0|1122.0|5|3|0.0|3|0|1121.0|5|2|0.0|3|0|1120.0|5|1|0.0|3|0|0.0|3|0|0.0|3|0|0.0|3|0|0.0|3|0|0.0|3|0|0.0|3|0|12|3614|-3602|17|3614|-3597|18|3614|-3596|9548|9536|3614|3614
```

**Message Format (pipe-separated) — theo message thực tế (80 field, index 0–79):**

| Index | Tên field | Sample | Ghi chú |
|-------|------------|--------|--------|
| [0] | channel/service | auto.dr.bo.41I1FA000 | Dạng `auto.dr.bo.{code}` |
| [1] | success | 1 | 1 = success, 0 = fail |
| [2] | time | 104652 | HHmmss (10:46:52) |
| [3] | code | 41I1FA000 | Mã hợp đồng |
| [4] | control_code | O | Trạng thái phiên (O = LO, A = ATC, …) |
| [5] | project_open.value | 1116.0 | Giá dự kiến (ATO/ATC); có thể = best bid khi LO |
| [6] | project_open.type | 5 | Mã loại giá |
| **[7]–[66]** | **10 price levels** | *(xem bảng dưới)* | Mỗi level **6 field**: `bid_value`, `bid_type`, `bid_size`, `offer_value`, `offer_type`, `offer_size` |
| [67]–[69] | (triplet 1) | 12, 3614, -3602 | Ý nghĩa xác nhận với Lotte (VD: count/size/diff) |
| [70]–[72] | (triplet 2) | 17, 3614, -3597 | |
| [73]–[75] | (triplet 3) | 18, 3614, -3596 | |
| [76] | totalBidSize | 9548 | Tổng KL dư mua |
| [77] | totalOfferSize | 9536 | Tổng KL dư bán |
| [78] | (reserved) | 3614 | Chưa xác nhận |
| [79] | (reserved) | 3614 | Chưa xác nhận |

**Cấu trúc 10 level ([7]–[66]):** mỗi level = 6 field liên tiếp:

| Trong 1 level (6 field) | Vị trí tương đối | Sample level 1 | Sample level 2 |
|------------------------|------------------|----------------|----------------|
| bid_value | +0 | 1125.0 | 1125.0 |
| bid_type | +1 | 5 | 5 |
| bid_size | +2 | 5 | 3614 |
| offer_value | +3 | 1200.0 | 1200.0 |
| offer_type | +4 | 3 | 3 |
| offer_size | +5 | 3614 | 3614 |

- Level 1: index [7]–[12]; Level 2: [13]–[18]; …; Level 10: [61]–[66].
- Các level trống (giá = 0): offer_value/offer_type/offer_size có thể 0.0, 3, 0.

**Subscribe:** Theo tài liệu Lotte (VD: `sub/pro.pub.auto.dr.bo./VN30F2501`). Channel name trong message = [0].

#### 4.3.3 TradeX WebSocket Channel Design

**New Channels for Derivatives:**

| TradeX Channel | Source | Description |
|----------------|--------|-------------|
| `market.quote.dr.{code}` | dr.qt | Giá, KL, ĐTNN phái sinh |
| `market.bidoffer.dr.{code}` | dr.bo | Sổ lệnh 10 bước phái sinh |

**Alternative:** Sử dụng chung channel `market.quote.{code}` nhưng thêm field `m: "INDEX"` hoặc `"BOND"` trong message (phái sinh).

```json
// Option 1: Separate channel (Recommended - ISOLATION)
// Channel: market.quote.dr.VN30F2501
{
  "s": "VN30F2501",
  "m": "INDEX",
  "c": 1285.5,
  "ch": 12.5,
  // ...
}

// Option 2: Shared channel with market identifier
// Channel: market.quote.VN30F2501
{
  "s": "VN30F2501",
  "m": "INDEX",    // Differentiator (INDEX | BOND)
  "c": 1285.5,
  // ...
}
```

**Recommendation:** Use **Option 1** (Separate channels) for:
- Clear isolation
- Easy to enable/disable
- No impact on existing equity subscriptions
- Clear logging and monitoring

#### 4.3.4 Channel `market.quote.dr.{code}` - Field Mapping & Sample

**Mapping: Lotte `auto.dr.qt` (theo message thực tế §4.3.2) → TradeX `market.quote.dr`**

| TradeX Field | Lotte Index | Transform | Description |
|--------------|-------------|-----------|-------------|
| `s` | [3] | Direct | Mã hợp đồng (code) |
| `t` | - | Hardcode `"DERIVATIVES"` | Loại sản phẩm |
| `ti` | [2] | VN→UTC nếu cần | Thời gian (HHmmss) |
| `o` | [6] | parseDouble | Giá mở cửa (điểm) |
| `h` | [8] | parseDouble | Giá cao nhất |
| `l` | [10] | parseDouble | Giá thấp nhất |
| `c` | [12] | parseDouble | Giá hiện tại |
| `ch` | [14] | parseDouble | Thay đổi |
| `ra` | [15] | parseDouble | % thay đổi |
| `vo` | [19] | parseLong | Khối lượng GD (xác nhận đơn vị: lot/lệnh) |
| `va` | [18] | parseDouble/parseLong | Giá trị GD (xác nhận đơn vị với Lotte) |
| `mv` | [20] | parseDouble → long | KL khớp cuối |
| `a` | [16] | parseDouble | Giá trung bình |
| `mb` | [21] | **66** → `"BID"` (BUY), **83** → `"ASK"` (SELL) | Bên khớp (matchedVolume.type) |
| `tb` | [28] | parseLong | Tổng KL mua |
| `to` | [29] | parseLong | Tổng KL bán |
| `fr.bv` | [32] | parseLong | ĐTNN mua |
| `fr.sv` | [33] | parseLong | ĐTNN bán |

**Fields KHÔNG CÓ trong Lotte WS (bỏ hoặc set null):**

| Field | Lý do |
|-------|-------|
| `tor` | Không có trong `auto.dr.qt` |
| `fr.cr` | Không có room NN cho phái sinh |
| `fr.tr` | Không có room NN cho phái sinh |

**Sample Response - Channel: `market.quote.dr.VN30F2502`**

```json
{
  "s": "VN30F2502",
  "t": "DERIVATIVES",
  "ti": "103025",
  "o": 1275.0,
  "h": 1290.0,
  "l": 1270.0,
  "c": 1285.5,
  "ch": 12.5,
  "ra": 0.98,
  "vo": 125000,
  "va": 160000000000,
  "mv": 500,
  "a": 1280.0,
  "mb": "BID",
  "tb": 5000,
  "to": 4500,
  "fr": {
    "bv": 5000,
    "sv": 3000
  }
}
```

#### 4.3.5 Channel `market.bidoffer.dr.{code}` - Field Mapping & Sample

**Mapping: Lotte `auto.dr.bo` (theo message thực tế §4.3.2) → TradeX `market.bidoffer.dr`**

| TradeX Field | Lotte Index | Transform | Description |
|--------------|-------------|-----------|-------------|
| `s` | [3] | Direct | Mã hợp đồng |
| `t` | - | Hardcode `"DERIVATIVES"` | Loại sản phẩm |
| `ti` | [2] | VN→UTC nếu cần | Thời gian (HHmmss) |
| `ss` | [4] | Map control_code → session (bảng dưới) | Phiên GD |
| `ep` | [5] | parseDouble | Giá dự kiến (project_open.value; ATO/ATC) |
| `tb` | [76] | parseLong | Tổng KL mua (totalBidSize) |
| `to` | [77] | parseLong | Tổng KL bán (totalOfferSize) |
| `bb` | [7]–[66] | Parse 10 level (6 field/level) | Sổ lệnh mua |
| `bo` | [7]–[66] | Parse 10 level (6 field/level) | Sổ lệnh bán |

**Mapping `control_code` [4] → `ss` (session):**

| Lotte control_code | TradeX ss | Description |
|--------------------|-----------|-------------|
| `P` | `"ATO"` | Khớp lệnh mở cửa |
| `O`, `R` | `"LO"` | Phiên liên tục |
| `I` | `"INTERMISSION"` | Nghỉ trưa |
| `A` | `"ATC"` | Khớp lệnh đóng cửa |
| `C` | `"PLO"` | Post Limit Order |
| `K`, `G` | `"CLOSED"` | Đóng cửa |

**Parsing 10 bước giá (index [7]–[66], mỗi level 6 field):**

Level `i` (i = 0..9) bắt đầu tại index `base = 7 + i*6`:

| Index | Field | TradeX |
|-------|--------|--------|
| base+0 | bid_value | `bb[i].p` |
| base+1 | bid_type | (bỏ qua hoặc lưu optional) |
| base+2 | bid_size | `bb[i].v` |
| base+3 | offer_value | `bo[i].p` |
| base+4 | offer_type | (bỏ qua hoặc lưu optional) |
| base+5 | offer_size | `bo[i].v` |

**Sample Response - Channel: `market.bidoffer.dr.VN30F2502`**

```json
{
  "s": "VN30F2502",
  "t": "DERIVATIVES",
  "ti": "103025",
  "ss": "LO",
  "ep": 0,
  "tb": 5000,
  "to": 4500,
  "bb": [
    {"p": 1285.0, "v": 1200},
    {"p": 1284.5, "v": 800},
    {"p": 1284.0, "v": 1500},
    {"p": 1283.5, "v": 900},
    {"p": 1283.0, "v": 1100},
    {"p": 1282.5, "v": 700},
    {"p": 1282.0, "v": 600},
    {"p": 1281.5, "v": 500},
    {"p": 1281.0, "v": 400},
    {"p": 1280.5, "v": 300}
  ],
  "bo": [
    {"p": 1285.5, "v": 1000},
    {"p": 1286.0, "v": 900},
    {"p": 1286.5, "v": 1100},
    {"p": 1287.0, "v": 800},
    {"p": 1287.5, "v": 700},
    {"p": 1288.0, "v": 600},
    {"p": 1288.5, "v": 500},
    {"p": 1289.0, "v": 400},
    {"p": 1289.5, "v": 350},
    {"p": 1290.0, "v": 250}
  ]
}
```

#### 4.3.6 So sánh Side-by-Side: Equity vs Derivatives

**Channel `market.quote`:**

| Field | Equity | Derivatives | Note |
|-------|--------|-------------|------|
| `s` | ✅ | ✅ | |
| `t` | `"STOCK"` | `"DERIVATIVES"` | Type khác |
| `ti` | ✅ | ✅ | |
| `o,h,l,c` | ✅ (VND) | ✅ (điểm) | Đơn vị khác |
| `ch,ra` | ✅ | ✅ | |
| `vo,va,mv` | ✅ | ✅ | |
| `a` | ✅ | ✅ | |
| `mb` | ✅ | ✅ | |
| `tb,to` | ✅ | ✅ | |
| `tor` | ✅ | ❌ Không gửi | Không có trong Lotte WS |
| `fr.bv,fr.sv` | ✅ | ✅ | |
| `fr.cr,fr.tr` | ✅ | ❌ Không gửi | Không có room |

**Channel `market.bidoffer`:**

| Field | Equity | Derivatives | Note |
|-------|--------|-------------|------|
| `s` | ✅ | ✅ | |
| `t` | `"STOCK"` | `"DERIVATIVES"` | Type khác |
| `ti` | ✅ | ✅ | |
| `ss` | ✅ | ✅ | |
| `ep` | ✅ | ✅ | Chỉ có khi ATO/ATC |
| `tb,to` | ✅ | ✅ | |
| `bb,bo` | ✅ (3 levels) | ✅ (10 levels) | DR có nhiều bước giá hơn |
| `bb[].p,bb[].v` | ✅ | ✅ | |
| `bb[].c` | ✅ | ❌ Không gửi | Không có trong Lotte WS |

#### 4.3.8 Implementation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                  DERIVATIVES WEBSOCKET DATA FLOW                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      LOTTE SECURITIES WEBSOCKET                              │
│                                                                             │
│   EQUITY (Existing)                    DERIVATIVES (New)                    │
│   ┌──────────┐  ┌──────────┐          ┌──────────┐  ┌──────────┐          │
│   │ auto.qt  │  │ auto.bo  │          │auto.dr.qt│  │auto.dr.bo│          │
│   └────┬─────┘  └────┬─────┘          └────┬─────┘  └────┬─────┘          │
└────────│─────────────│─────────────────────│─────────────│─────────────────┘
         │             │                     │             │
         ▼             ▼                     ▼             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       MARKET-COLLECTOR-LOTTE                                │
│                                                                             │
│  EQUITY Handler (Existing)            DR Handler (New - Isolated)           │
│  ┌────────────────────────┐          ┌────────────────────────┐            │
│  │ EquityQuoteHandler     │          │ DerivativeQuoteHandler │            │
│  │ EquityBidOfferHandler  │          │ DerivativeBidOfferHndlr│            │
│  └───────────┬────────────┘          └───────────┬────────────┘            │
│              │                                   │                          │
│              ▼                                   ▼                          │
│  ┌────────────────────────┐          ┌────────────────────────┐            │
│  │ Parse pipe-separated   │          │ Parse DR pipe-separated│            │
│  │ Transform to DTO       │          │ Transform to DR DTO    │            │
│  └───────────┬────────────┘          └───────────┬────────────┘            │
└──────────────│───────────────────────────────────│──────────────────────────┘
               │                                   │
               ▼                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              KAFKA                                          │
│  ┌──────────────┐ ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ │
│  │ quoteUpdate  │ │bidOfferUpdate  │ │quoteUpdateDR   │ │bidOfferUpdateDR│ │
│  │  (Equity)    │ │   (Equity)     │ │ (Derivatives)  │ │ (Derivatives)  │ │
│  └──────┬───────┘ └───────┬────────┘ └───────┬────────┘ └───────┬────────┘ │
└─────────│─────────────────│──────────────────│──────────────────│───────────┘
          │                 │                  │                  │
          ▼                 ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          REALTIME-V2 (Java)                                 │
│                                                                             │
│  EQUITY Consumers (Existing)          DR Consumers (New - Isolated)         │
│  ┌────────────────────────┐          ┌────────────────────────┐            │
│  │ QuoteService           │          │ DerivativeQuoteService │            │
│  │ BidOfferService        │          │ DerivativeBOService    │            │
│  └───────────┬────────────┘          └───────────┬────────────┘            │
│              │                                   │                          │
│              ▼                                   ▼                          │
│           Update SymbolInfo             Update SymbolInfo                   │
│           (type=STOCK,ETF...)           (type=FUTURES, m=INDEX|BOND)       │
│              │                                   │                          │
│              └───────────────────┬───────────────┘                          │
│                                  ▼                                          │
│                      marketRedisDao.setSymbolInfo()                         │
└──────────────────────────────────│──────────────────────────────────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │  Redis: realtime_mapSymbolInfo│
                    └──────────────┬───────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              WS-V2 (Node.js)                                │
│                                                                             │
│   EQUITY Channels (Existing)          DR Channels (New)                     │
│   ┌────────────────────────┐          ┌────────────────────────┐           │
│   │ market.quote.{code}    │          │ market.quote.dr.{code} │           │
│   │ market.bidoffer.{code} │          │ market.bidoffer.dr.{code}│          │
│   └────────────────────────┘          └────────────────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
                          ┌────────────────┐
                          │  NHSV PRO APP  │
                          └────────────────┘
```

#### 4.3.9 WebSocket handler (market-collector-lotte)

Parse message Lotte DR (pipe-separated), map sang DTO, gửi Kafka `quoteUpdateDR` / `bidOfferUpdateDR`. Message derivatives phải mang **`m`** = "INDEX" hoặc "BOND" khi aggregate vào Redis để SymbolInfo API trả đủ cho FE. Chi tiết implementation xem Spec/Issue riêng.

---

## 5. Data Model Changes

### 5.1 SymbolInfo Model - Full Field Reference

> **Baseline:** Dựa trên response thực tế từ API `/api/v2/market/symbolInfo` cho cơ sở.  
> **Derivatives:** Mọi response trả về symbol phái sinh (SymbolInfo, symbol_static, symbol/latest) phải có **`m`** = `"INDEX"` hoặc `"BOND"` (theo quy tắc 41I/41B, 4.1.2.1) — cùng field `m` FE đang dùng cho HOSE/HNX/UPCOM.  
> Danh sách field đầy đủ và mapping sang JSON xem **Bảng 5.2** bên dưới.

### 5.2 Field Mapping: Java Model → JSON Response

| Java Field | JSON Abbr | Equity | Derivatives |
|------------|-----------|--------|-------------|
| `code` | `s` | ✅ | ✅ |
| `type` | `t` | ✅ | ✅ (FUTURES) |
| `market` | `m` | ✅ (HOSE/HNX/UPCOM) | ✅ (INDEX \| BOND) |
| `name` | `n1` | ✅ | ✅ |
| `nameEn` | `n2` | ✅ | ✅ |
| `time` | `ti` | ✅ | ✅ |
| `bidOfferTime` | `bot` | ✅ | ✅ |
| `lastTradingTime` | `lt` | ✅ | ✅ |
| `sessions` | `ss` | ✅ | ✅ |
| `open` | `o` | ✅ (VND) | ✅ (điểm) |
| `high` | `h` | ✅ | ✅ |
| `low` | `l` | ✅ | ✅ |
| `last` | `c` | ✅ | ✅ |
| `change` | `ch` | ✅ | ✅ |
| `rate` | `ra` | ✅ | ✅ |
| `averagePrice` | `a` | ✅ | ✅ |
| `ceilingPrice` | `ce` | ✅ | ✅ |
| `floorPrice` | `fl` | ✅ | ✅ |
| `referencePrice` | `re` | ✅ | ✅ |
| `tradingVolume` | `vo` | ✅ | ✅ |
| `tradingValue` | `va` | ✅ | ✅ |
| `matchingVolume` | `mv` | ✅ | ✅ |
| `matchedBy` | `mb` | ✅ | ✅ |
| `turnoverRate` | `tor` | ✅ | null |
| `totalBidVolume` | `tb` | ✅ | ✅ |
| `totalOfferVolume` | `to` | ✅ | ✅ |
| `bidOfferList` | `bb` + `bo` | ✅ | ✅ |
| `expectedPrice` | `ep` | ✅ | ✅ |
| `foreignerBuyVolume` | `fr.bv` | ✅ | ✅ |
| `foreignerSellVolume` | `fr.sv` | ✅ | ✅ |
| `foreignerCurrentRoom` | `fr.cr` | ✅ | null |
| `foreignerTotalRoom` | `fr.tr` | ✅ | null |
| `listedQuantity` | `lq` | ✅ | null |
| `marketCap` | `mc` | ✅ | null |
| `highLowYearly` | `hly` | ✅ | null |
| `openInterest` | `oi` | null | ✅ |
| `baseCode` | `bc` | null | ✅ |
| `firstTradingDate` | `ftd` | null | ✅ |
| `expiryDate` | `ed` | null | ✅ |
| `remainingDays` | `rd` | null | ✅ |
| `theoryPrice` | `tp` | null | ✅ |
| `basis` | `bs` | null | ✅ |

### 5.3 DTOs Lotte DR API

Cấu trúc dữ liệu nhận từ Lotte DRMKT-001 / DRMKT-002 (stock-board, stock-price) theo mô tả ở 4.1.2. Triển khai chi tiết xem Spec/Issue riêng.

### 5.4 symbol_static.json Format Update

> **Lưu ý:** `symbol_static.json` chỉ chứa thông tin **tĩnh** của mã, được download lúc app start.
> Thông tin **động** (giá, KL, sổ lệnh) được nhận qua WebSocket hoặc API symbolInfo.

**Current Format (Equity):**
```json
{
    "s": "TCB",
    "m": "HOSE",
    "n1": "Ngân hàng Thương mại Cổ phần Kỹ Thương Việt Nam",
    "n2": "Vietnam Technological and Commercial Joint Stock Bank",
    "t": "STOCK",
    "re": 34900.0,
    "ce": 37300.0,
    "fl": 32500.0,
    "lq": 7086240414
}
```

**New Format (Derivatives):**
```json
{
    "s": "VN30F2502",
    "m": "INDEX",
    "n1": "HĐ Tương lai VN30 Tháng 02/2025",
    "n2": "VN30 Index Futures Feb 2025",
    "t": "FUTURES",
    "re": 1273.0,
    "ce": 1350.0,
    "fl": 1220.0,
    "lq": null,
    "bc": "VN30",
    "ftd": "20250101",
    "ed": "20250227",
    "rd": 28
}
```

(Với HĐTL trái phiếu chính phủ: `"s": "41Bxxxxxx"`, `"m": "BOND"`.)

**Field Mapping (symbol_static.json):**

| Field | Full Name | Description | Equity | Derivatives |
|-------|-----------|-------------|--------|-------------|
| `s` | symbol | Mã chứng khoán | ✅ | ✅ |
| `m` | market | Thị trường / Loại phái sinh | HOSE/HNX/UPCOM | **INDEX** \| **BOND** (suy từ ký tự thứ 3 của `s`) |
| `n1` | name1 | Tên tiếng Việt | ✅ | ✅ |
| `n2` | name2 | Tên tiếng Anh | ✅ | ✅ |
| `t` | type | Loại sản phẩm | STOCK/ETF/CW | **FUTURES** |
| `re` | reference | Giá tham chiếu | ✅ (VND) | ✅ (Điểm) |
| `ce` | ceiling | Giá trần | ✅ | ✅ |
| `fl` | floor | Giá sàn | ✅ | ✅ |
| `lq` | listedQty | KL niêm yết | ✅ | null |
| `bc` | baseCode | Mã cơ sở | ❌ | ✅ (VN30) |
| `ftd` | firstTradingDate | Ngày GD đầu tiên | ❌ | ✅ (yyyyMMdd) |
| `ed` | expiryDate | Ngày đáo hạn | ❌ | ✅ (yyyyMMdd) |
| `rd` | remainDays | Số ngày còn lại | ❌ | ✅ |

**Sample Full File:**
```json
[
  {
    "s": "TCB",
    "m": "HOSE",
    "n1": "Ngân hàng Thương mại Cổ phần Kỹ Thương Việt Nam",
    "n2": "Vietnam Technological and Commercial Joint Stock Bank",
    "t": "STOCK",
    "re": 34900.0,
    "ce": 37300.0,
    "fl": 32500.0,
    "lq": 7086240414
  },
  {
    "s": "VN30F2502",
    "m": "INDEX",
    "n1": "HĐ Tương lai VN30 Tháng 02/2025",
    "n2": "VN30 Index Futures Feb 2025",
    "t": "FUTURES",
    "re": 1273.0,
    "ce": 1350.0,
    "fl": 1220.0,
    "lq": null,
    "bc": "VN30",
    "ftd": "20250101",
    "ed": "20250227",
    "rd": 28
  },
  {
    "s": "VN30F2503",
    "m": "INDEX",
    "n1": "HĐ Tương lai VN30 Tháng 03/2025",
    "n2": "VN30 Index Futures Mar 2025",
    "t": "FUTURES",
    "re": 1275.0,
    "ce": 1352.0,
    "fl": 1222.0,
    "lq": null,
    "bc": "VN30",
    "ftd": "20250101",
    "ed": "20250320",
    "rd": 49
  },
  {
    "s": "41Bxxxxxx",
    "m": "BOND",
    "n1": "HĐTL Trái phiếu Chính phủ ...",
    "n2": "Gov Bond Futures ...",
    "t": "FUTURES",
    "re": 100.5,
    "ce": 102.0,
    "fl": 99.0,
    "lq": null,
    "bc": null,
    "ftd": "20250101",
    "ed": "20250630",
    "rd": 120
  }
]
```

---

## 6. API Specifications

### 6.1 New Lotte API Integration

| API ID | Endpoint | Method | Purpose |
|--------|----------|--------|---------|
| DRMKT-001 | `/tuxsvc/market/dr/stock-board` | POST | List all derivative symbols |
| DRMKT-002 | `/tuxsvc/market/dr/stock-price` | GET | Get single derivative detail |

### 6.2 TradeX API Updates

| API | Change Type | Description |
|-----|-------------|-------------|
| `/api/v2/market/symbol/latest` | Enhancement | Include derivatives in response |
| `/api/v2/market/symbol/staticInfo` | Enhancement | Include derivatives |

### 6.3 WebSocket Channel Updates

| Channel | Type | Description |
|---------|------|-------------|
| `market.quote.dr.{code}` | NEW | Derivatives quote updates |
| `market.bidoffer.dr.{code}` | NEW | Derivatives bid/offer updates |

---

## 7. Testing Strategy

### 7.1 Unit Tests

| Test Suite | Service | Coverage |
|------------|---------|----------|
| LotteApiServiceTest | market-collector-lotte | Lotte DR API calls |
| DerivativeMergeTest | market-collector-lotte | SymbolInfo merge logic |
| DerivativeWsParserTest | market-collector-lotte | WS message parsing |
| SymbolServiceTest | market-query-v2 | API filter logic |

### 7.2 Integration Tests

| Test | Components | Scenario |
|------|------------|----------|
| InitJobIntegration | collector → realtime → redis | Full init flow with derivatives |
| WsFlowIntegration | collector → kafka → realtime → ws | Real-time data flow |
| ApiIntegration | rest-proxy → market-query → redis | API response validation |

### 7.3 E2E Test Cases

| TC ID | Description | Expected |
|-------|-------------|----------|
| E2E-DR-001 | Init job với derivatives | symbol_static.json chứa derivatives với m="INDEX" \| "BOND" |
| E2E-DR-002 | API /symbol/latest với derivatives | Response có cả equity và derivatives |
| E2E-DR-003 | WebSocket subscribe DR channel | Nhận real-time DR data |
| E2E-DR-004 | Init job khi Lotte DR API fail | Equity vẫn hoạt động, log warning |
| E2E-DR-005 | WS khi DR channel disconnect | Equity WS không ảnh hưởng |

### 7.4 Regression Tests

| Test | Purpose |
|------|---------|
| Equity init unchanged | Verify equity flow không bị ảnh hưởng |
| Equity WS unchanged | Verify equity real-time không đổi |
| API backward compatible | Old clients vẫn nhận được data đúng format |

---

## 8. Rollout Plan

### 8.1 Deployment Phases

| Phase | Environment | Duration | Actions |
|-------|-------------|----------|---------|
| Phase 0 | Dev | 1 week | Implement & unit test |
| Phase 1 | UAT | 1 week | Integration test |
| Phase 2 | Production (shadow) | 1 week | Run parallel, monitor |
| Phase 3 | Production (live) | - | Full rollout |

### 8.2 Feature Flags

```yaml
# Configuration
derivatives:
  enabled: true                    # Master switch
  initJob:
    enabled: true                  # Enable/disable DR in init job
    failSafe: true                 # Continue equity if DR fails
  websocket:
    enabled: true                  # Enable/disable DR WS subscription
    channels:
      quote: true
      bidOffer: true
  api:
    includeInLatest: true          # Include DR in /symbol/latest
```

### 8.3 Monitoring & Alerts

| Metric | Threshold | Alert |
|--------|-----------|-------|
| DR Init Success Rate | < 95% | Warning |
| DR WS Message Latency | > 500ms | Warning |
| DR API Error Rate | > 1% | Critical |
| Equity Performance Impact | > 5% | Critical |

### 8.4 Rollback Triggers

| Condition | Action |
|-----------|--------|
| Equity init fails | Disable DR in init job |
| Equity WS degraded | Disable DR WS subscription |
| API latency increases > 20% | Disable DR in API response |

---

## Appendix A: References

| Document | Location |
|----------|----------|
| Lotte DR API Specs | `Derivatives/Documentation/[API specs]Lotte_DR.md` |
| Lotte DR WebSocket | `Derivatives/Documentation/Websocket_DR_Lotte.md` |
| TradeX Init Job Knowledge | `TradeX Knowledge/init-job.md` |
| TradeX SymbolInfo API Knowledge | `TradeX Knowledge/symbol-info-api.md` |
| TradeX Market Data Channels | `TradeX Knowledge/market-data-channels.md` |

---

## Appendix B: Glossary

| Term | Definition |
|------|------------|
| **Derivatives** | Phái sinh - Hợp đồng tương lai VN30 |
| **Open Interest (OI)** | Số lượng hợp đồng mở chưa đóng |
| **Basis** | Chênh lệch giữa giá Futures và giá Spot |
| **Expiry Date** | Ngày đáo hạn của hợp đồng |
| **Control Code** | Mã trạng thái phiên giao dịch |

---

*Document End*
