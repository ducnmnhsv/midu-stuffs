# Derivatives Agent Rules (TEMPO)

> **Status:** TEMPO - Planning phase  
> **Purpose:** Lưu trữ patterns đã phân tích để tránh scan lại codebase  
> **Note:** Các rules này có thể bị xoá hoặc update khi implement thực tế

---

## Project Structure

```
Derivatives/
├── Agent based rules (tempo)/    ← Bạn đang ở đây
│   └── Patterns đã phân tích
│
├── Documentation/
│   ├── [API specs]Lotte_DR.md    ← Lotte API specs
│   └── Websocket_DR_Lotte.md     ← Lotte WS specs
│
└── Planning documentation/
    ├── _index.md                 ← Navigation
    ├── Market data/              ← Module 1 (P0)
    ├── Orders/                   ← Module 2 (Future)
    ├── Cash transaction/         ← Module 3 (Future)
    └── Account/                  ← Module 4 (Future)
```

---

## Danh sách Rules

### Module: Market Data

| File | Description | Status |
|------|-------------|--------|
| [symbolinfo-api-baseline.md](./symbolinfo-api-baseline.md) | Phân tích API SymbolInfo cơ sở (44 fields) | ✅ Done |
| [derivatives-symbolinfo-design.md](./derivatives-symbolinfo-design.md) | Design SymbolInfo cho Derivatives | ✅ Done |
| [websocket-channel-mapping.md](./websocket-channel-mapping.md) | Mapping WS channels: Lotte → TradeX | ✅ Done |

### Module: Orders (Future)

| File | Description | Status |
|------|-------------|--------|
| (coming soon) | Order API mapping | 📋 Not Started |

### Module: Cash Transaction (Future)

| File | Description | Status |
|------|-------------|--------|
| (coming soon) | Cash API mapping | 📋 Not Started |

---

## Quick Links

| Need | Go to |
|------|-------|
| Planning docs | [Planning documentation/_index.md](../Planning%20documentation/_index.md) |
| Market Data module | [Planning documentation/Market data/](../Planning%20documentation/Market%20data/) |
| Lotte API specs | [Documentation/[API specs]Lotte_DR.md](../Documentation/[API%20specs]Lotte_DR.md) |
| Lotte WS specs | [Documentation/Websocket_DR_Lotte.md](../Documentation/Websocket_DR_Lotte.md) |

---

## Quick Reference - Market Data Module

### SymbolInfo API Fields Count

| Category | Equity | Derivatives |
|----------|--------|-------------|
| Total fields | 44 | 51 (+7 new) |
| Fields set null | 0 | 6 |
| New fields | - | 7 |

### Derivatives-Specific Fields (từ Lotte API)

```
TradeX    Lotte Source              Description
------    ------------              -----------
oi     → oi / open_interest        (Số hợp đồng mở)
bc     → base_code                 (Mã cơ sở: VN30)
ftd    → first_trd_date            (Ngày GD đầu tiên)
ed     → end_trd_date / exp_date   (Ngày đáo hạn)
rd     → remain_date               (Số ngày còn lại)
tp     → theory_price              (Giá lý thuyết)
bs     → theory_basis              (Basis lý thuyết)
```

### Key Identifiers

| Field | Equity | Derivatives |
|-------|--------|-------------|
| `m` (market) | HOSE/HNX/UPCOM | `"DERIVATIVES"` |
| `t` (type) | STOCK/ETF/CW/INDEX | `"DERIVATIVES"` |

### WebSocket Channel Mapping (market.quote)

| TradeX | Lotte `auto.dr.qt` | Index |
|--------|-------------------|-------|
| `s` | code | [3] |
| `ti` | time | [2] |
| `o` | open.value | [6] |
| `h` | high.value | [8] |
| `l` | low.value | [10] |
| `c` | last.value | [12] |
| `ch` | change.value | [14] |
| `ra` | changeRate | [15] |
| `vo` | volume | [19] |
| `va` | value | [18] |
| `mv` | matchedVolume.value | [20] |
| `a` | averagePrice | [16] |
| `mb` | matchedVolume.type | [21] B→BID, S→ASK |
| `tb` | total_bid_size | [28] |
| `to` | total_offer_size | [29] |
| `fr.bv` | foreignerBuySize | [32] |
| `fr.sv` | foreignerSellSize | [33] |

---

## Usage

Khi làm việc với Derivatives, đọc các files trong folder này trước để:
1. Tránh scan lại codebase
2. Có baseline để so sánh
3. Giảm token usage

---

*Last updated: 2025-01-30*
