# Derivatives SymbolInfo Design - TEMPO

> **Status:** PLANNING - Chưa implement  
> **Purpose:** Design pattern cho Derivatives SymbolInfo API  
> **Created:** 2025-01-30  
> **Depends on:** `symbolinfo-api-baseline.md`

---

## 1. Nguyên tắc thiết kế

| Nguyên tắc | Mô tả |
|------------|-------|
| **GIỮ NGUYÊN** | Tất cả 44 fields của cơ sở |
| **THÊM** | 7 fields đặc thù phái sinh |
| **SET NULL** | Fields không áp dụng cho phái sinh |
| **ĐƠN VỊ** | Giá = điểm (point) thay vì VND |

---

## 2. Sample Response (Derivatives - VN30F2502)

```json
{
  "s": "VN30F2502",
  "t": "DERIVATIVES",
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
  "m": "DERIVATIVES",
  "n1": "HĐ Tương lai VN30 Tháng 02/2025",
  "n2": "VN30 Index Futures Feb 2025",
  "lt": "150000",
  "bb": [{"p": 1285.0, "v": 1200, "c": null}, {"p": 1284.5, "v": 800, "c": null}],
  "bo": [{"p": 1285.5, "v": 1000, "c": null}, {"p": 1286.0, "v": 900, "c": null}],
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

---

## 3. Fields đặc thù Derivatives - Mapping từ Lotte API

> **Source:** `[API specs]Lotte_DR.md` - DRMKT-001 & DRMKT-002

### 3.1 Từ DRMKT-001 (`/dr/stock-board`)

| TradeX Field | Lotte Field | Type | Description | Có trong API? |
|--------------|-------------|------|-------------|---------------|
| `oi` | `oi` | Long | Open Interest (KL mở) | ✅ DRMKT-001 |
| `ed` | `exp_date` | Int | Tháng đáo hạn | ✅ DRMKT-001 |

### 3.2 Từ DRMKT-002 (`/dr/stock-price`)

| TradeX Field | Lotte Field | Type | Description | Có trong API? |
|--------------|-------------|------|-------------|---------------|
| `oi` | `open_interest` | Long | Open Interest | ✅ DRMKT-002 |
| `bc` | `base_code` | String | Mã cơ sở (VN30) | ✅ DRMKT-002 |
| `ftd` | `first_trd_date` | Int | Ngày GD đầu tiên | ✅ DRMKT-002 |
| `ed` | `end_trd_date` | Int | Ngày đáo hạn | ✅ DRMKT-002 |
| `rd` | `remain_date` | Int | Số ngày còn lại | ✅ DRMKT-002 |
| `tp` | `theory_price` | Double | Giá lý thuyết | ✅ DRMKT-002 |
| `bs` | `theory_basis` | Double | Basis lý thuyết | ✅ DRMKT-002 |

### 3.3 Tổng hợp Fields đặc thù (7 fields)

| Field | Full Name | Type | Lotte Source | Example |
|-------|-----------|------|--------------|---------|
| `oi` | openInterest | number | `oi` / `open_interest` | `45000` |
| `bc` | baseCode | string | `base_code` | `"VN30"` |
| `ftd` | firstTradingDate | string | `first_trd_date` | `"20250101"` |
| `ed` | expiryDate | string | `end_trd_date` | `"20250227"` |
| `rd` | remainingDays | number | `remain_date` | `28` |
| `tp` | theoryPrice | number | `theory_price` | `1284.0` |
| `bs` | basis | number | `theory_basis` | `1.5` |

---

## 4. So sánh Fields: Cơ sở vs Phái sinh

### Fields GIỐNG nhau (giữ nguyên logic)

| Field | Cơ sở | Phái sinh | Note |
|-------|-------|-----------|------|
| `s` | `"TCB"` | `"VN30F2502"` | Mã khác |
| `t` | `"STOCK"` | `"DERIVATIVES"` | Type khác |
| `m` | `"HOSE"` | `"DERIVATIVES"` | Market khác |
| `n1`, `n2` | Tên công ty | Tên hợp đồng | Từ `name` (DRMKT-002) |
| `ti`, `bot`, `lt`, `ss` | ✅ | ✅ | Giữ nguyên |
| `o`, `h`, `l`, `c` | VND | Điểm | Đơn vị khác |
| `ch`, `ra`, `a` | ✅ | ✅ | Giữ nguyên |
| `ce`, `fl`, `re` | VND | Điểm | Đơn vị khác |
| `vo`, `va`, `mv`, `mb` | ✅ | ✅ | Giữ nguyên |
| `tb`, `to`, `bb`, `bo` | ✅ | ✅ | Giữ nguyên |
| `ep` | ✅ | ✅ | Từ `project_open` |
| `fr.bv`, `fr.sv` | ✅ | ✅ | Từ `foreign_buy_vol`, `foreign_sell_vol` |

### Fields SET NULL cho Phái sinh

| Field | Lý do |
|-------|-------|
| `tor` | Không có KL niêm yết để tính turnover |
| `fr.cr`, `fr.tr` | Không có room NN cho phái sinh |
| `lq` | Không có KL niêm yết |
| `mc` | Không có vốn hóa |
| `hly` | Hợp đồng ngắn hạn, không cần giá năm |

---

## 5. symbol_static.json Format

### Cơ sở (hiện tại)
```json
{
  "s": "TCB",
  "m": "HOSE",
  "n1": "Ngân hàng TMCP Kỹ Thương VN",
  "n2": "Vietnam Technological...",
  "t": "STOCK",
  "re": 34900.0,
  "ce": 37300.0,
  "fl": 32500.0,
  "lq": 7086240414
}
```

### Phái sinh (mới)
```json
{
  "s": "VN30F2502",
  "m": "derivatives",
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

---

## 6. Quick Abbreviation - Derivatives Only

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

---

## 7. Lotte API Source Reference

### 7.1 DRMKT-001: List derivatives (`/dr/stock-board`)

**Response fields có thể dùng:**
```
code, last, change, change_rate, vol, total_vol, matched_vol
ceiling, floor, open, high, low, ref_price
bid1/2/3, offer1/2/3, bid1/2/3_size, offer1/2/3_size
value, oi, exp_date, control_code
foreign_buy_vol, foreign_sell_vol
```

### 7.2 DRMKT-002: Single derivative (`/dr/stock-price`)

**Response fields có thể dùng:**
```
code, name, time
ceiling, floor, open, high, low, last, change, ref_price, average_price
volume, amount, pt_volume, tot_volume
bid, offer, basis, disparate
bid_offer_list, total_vis_bid_size, total_vis_offer_size
foreign_buy_vol, foreign_sell_vol, for_tot_room, for_cur_room
project_open, control_code
base_code, open_interest
first_trd_date, end_trd_date, remain_date
theory_price, theory_basis, market_basis
```

### 7.3 WebSocket Channels (Websocket_DR_Lotte.md)

**RMK-011 Future Quote (`auto.dr.qt`):**
```
[0] service, [2] time, [3] code
[6] open.value, [8] high.value, [10] low.value, [12] last.value
[14] change.value, [15] changeRate, [16] averagePrice, [17] referencePrice
[18] value, [19] volume, [20] matchedVolume.value, [21] matchedVolume.type
[22] bid.value, [24] offer.value, [26] bid_size, [27] offer_size
[28] total_bid_size, [29] total_offer_size
[32] foreignerBuySize, [33] foreignerSellSize
```

**RMK-012 Future Bid/Offer (`auto.dr.bo`):**
```
[0] service, [2] time, [3] code, [4] control_code
[5] project_open.value (giá dự kiến ATO/ATC)
[7] bid.value, [9] bidSize, [10] offer.value, [12] offerSize
[13-72] 10 bước giá (mỗi bước 6 fields)
[73] totalBidSize, [74] totalOfferSize, [75] bidOfferSizeDiff
```

---

## 8. Implementation Notes

### Backend Changes (dự kiến)

| Service | Change | Description |
|---------|--------|-------------|
| `tradex-common-java` | Add fields | 7 fields mới trong SymbolInfo model |
| `market-collector-lotte` | New API client | Gọi DRMKT-001, DRMKT-002 |
| `market-collector-lotte` | WS handlers | Subscribe `auto.dr.qt`, `auto.dr.bo` |
| `realtime-v2` | New consumers | Consume `quoteUpdateDR`, `bidOfferUpdateDR` |
| `market-query-v2` | Không thay đổi | Đọc từ Redis như bình thường |

### FE Impact

- **Không cần thay đổi** cơ chế gọi API
- Chỉ cần handle thêm fields mới khi `m === "DERIVATIVES"`

---

*TEMPO - Design có thể thay đổi khi implement thực tế*
