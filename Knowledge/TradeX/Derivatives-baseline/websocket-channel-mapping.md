# WebSocket Channel Mapping - Derivatives

> **Status:** TEMPO - Planning phase  
> **Purpose:** Mapping fields giữa Lotte WS → TradeX WS cho Derivatives  
> **Created:** 2025-01-30  
> **Source:** `Websocket_DR_Lotte.md` (RMK-011, RMK-012)

---

## 1. Channel `market.quote` - Cơ sở (Baseline)

**Sample message (Equity - VND):**
```json
{
  "s": "VND",
  "t": "STOCK",
  "ti": "070250",
  "o": 16050,
  "h": 16050,
  "l": 13950,
  "c": 16050,
  "ch": 1050,
  "ra": 0.7,
  "vo": 1345100,
  "va": 20677070000,
  "mv": 100,
  "a": 15350,
  "mb": "BID",
  "tor": 0.0884,
  "fr": {
    "bv": 20016,
    "sv": 520710,
    "cr": 1350538255,
    "tr": 1522299908
  }
}
```

**Field Reference (Equity):**

| Field | Full Name | Description |
|-------|-----------|-------------|
| `s` | symbol | Mã CK |
| `t` | type | Loại: STOCK |
| `ti` | time | Thời gian (UTC HHmmss) |
| `o` | open | Giá mở cửa |
| `h` | high | Giá cao nhất |
| `l` | low | Giá thấp nhất |
| `c` | current | Giá hiện tại |
| `ch` | change | Thay đổi giá |
| `ra` | rate | % thay đổi |
| `vo` | volume | Khối lượng GD |
| `va` | value | Giá trị GD |
| `mv` | matchingVolume | KL khớp cuối |
| `a` | averagePrice | Giá trung bình |
| `mb` | matchedBy | Bên khớp: BID/ASK |
| `tor` | turnoverRate | Tỷ lệ quay vòng |
| `fr.bv` | foreignerBuyVolume | KL mua ĐTNN |
| `fr.sv` | foreignerSellVolume | KL bán ĐTNN |
| `fr.cr` | foreignerCurrentRoom | Room còn lại |
| `fr.tr` | foreignerTotalRoom | Tổng room |

---

## 2. Channel `market.quote.dr` - Phái sinh (Proposed)

### 2.1 Lotte Source: `auto.dr.qt` (RMK-011)

| Lotte Index | Lotte Field | Available |
|-------------|-------------|-----------|
| [2] | time | ✅ |
| [3] | code | ✅ |
| [6] | open.value | ✅ |
| [8] | high.value | ✅ |
| [10] | low.value | ✅ |
| [12] | last.value | ✅ |
| [14] | change.value | ✅ |
| [15] | changeRate | ✅ |
| [16] | averagePrice | ✅ |
| [17] | referencePrice | ✅ |
| [18] | value | ✅ |
| [19] | volume | ✅ |
| [20] | matchedVolume.value | ✅ |
| [21] | matchedVolume.type | ✅ (S=Sell, B=Buy) |
| [28] | total_bid_size | ✅ |
| [29] | total_offer_size | ✅ |
| [32] | foreignerBuySize | ✅ |
| [33] | foreignerSellSize | ✅ |

### 2.2 Mapping: Lotte → TradeX

| TradeX Field | Lotte Source | Index | Transform | Note |
|--------------|--------------|-------|-----------|------|
| `s` | code | [3] | Direct | Mã hợp đồng |
| `t` | - | - | Hardcode | `"DERIVATIVES"` |
| `ti` | time | [2] | VN→UTC | Thời gian |
| `o` | open.value | [6] | parseDouble | Giá mở cửa (điểm) |
| `h` | high.value | [8] | parseDouble | Giá cao nhất |
| `l` | low.value | [10] | parseDouble | Giá thấp nhất |
| `c` | last.value | [12] | parseDouble | Giá hiện tại |
| `ch` | change.value | [14] | parseDouble | Thay đổi |
| `ra` | changeRate | [15] | parseDouble | % thay đổi |
| `vo` | volume | [19] | parseLong | Khối lượng |
| `va` | value | [18] | parseLong | Giá trị |
| `mv` | matchedVolume.value | [20] | parseLong | KL khớp cuối |
| `a` | averagePrice | [16] | parseDouble | Giá TB |
| `mb` | matchedVolume.type | [21] | B→BID, S→ASK | Bên khớp |
| `tb` | total_bid_size | [28] | parseLong | Tổng KL mua |
| `to` | total_offer_size | [29] | parseLong | Tổng KL bán |
| `fr.bv` | foreignerBuySize | [32] | parseLong | ĐTNN mua |
| `fr.sv` | foreignerSellSize | [33] | parseLong | ĐTNN bán |

### 2.3 Fields KHÔNG CÓ trong Lotte WS (set null hoặc bỏ)

| Field | Lý do |
|-------|-------|
| `tor` | Không có trong `auto.dr.qt` |
| `fr.cr` | Không có room NN cho phái sinh |
| `fr.tr` | Không có room NN cho phái sinh |

### 2.4 Sample Message (Derivatives - VN30F2502)

```json
{
  "s": "VN30F2502",
  "t": "DERIVATIVES",
  "ti": "070250",
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

---

## 3. Channel `market.bidoffer.dr` - Phái sinh (Proposed)

### 3.1 Lotte Source: `auto.dr.bo` (RMK-012)

| Lotte Index | Lotte Field | Available |
|-------------|-------------|-----------|
| [2] | time | ✅ |
| [3] | code | ✅ |
| [4] | control_code | ✅ |
| [5] | project_open.value | ✅ |
| [13-72] | 10 price levels | ✅ |
| [73] | totalBidSize | ✅ |
| [74] | totalOfferSize | ✅ |

### 3.2 Mapping: Lotte → TradeX

| TradeX Field | Lotte Source | Index | Transform | Note |
|--------------|--------------|-------|-----------|------|
| `s` | code | [3] | Direct | Mã hợp đồng |
| `t` | - | - | Hardcode | `"DERIVATIVES"` |
| `ti` | time | [2] | VN→UTC | Thời gian |
| `ss` | control_code | [4] | Mapping | Phiên GD |
| `ep` | project_open.value | [5] | parseDouble | Giá dự kiến |
| `tb` | totalBidSize | [73] | parseLong | Tổng KL mua |
| `to` | totalOfferSize | [74] | parseLong | Tổng KL bán |
| `bb` | bid1-10 | [13-72] | Parse | Sổ lệnh mua |
| `bo` | offer1-10 | [13-72] | Parse | Sổ lệnh bán |

### 3.3 Mapping `control_code` → `ss`

| Lotte | TradeX ss | Description |
|-------|-----------|-------------|
| `P` | `"ATO"` | Khớp lệnh mở cửa |
| `O`, `R` | `"LO"` | Phiên liên tục |
| `I` | `"INTERMISSION"` | Nghỉ trưa |
| `A` | `"ATC"` | Khớp lệnh đóng cửa |
| `C` | `"PLO"` | Post Limit Order |
| `K`, `G` | `"CLOSED"` | Đóng cửa |

### 3.4 Parsing 10 bước giá

Index [13] đến [72], mỗi bước 6 fields:
```
[13 + i*6] bid[i].value      → bb[i].p
[14 + i*6] bid[i].type       → (skip)
[15 + i*6] bid[i]Size        → bb[i].v
[16 + i*6] offer[i].value    → bo[i].p
[17 + i*6] offer[i].type     → (skip)
[18 + i*6] offer[i]Size      → bo[i].v
```

### 3.5 Sample Message (Derivatives BidOffer)

**Channel: `market.bidoffer.dr.VN30F2502`**

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

---

## 4. So sánh Side-by-Side

### 4.1 market.quote

| Field | Equity | Derivatives | Source |
|-------|--------|-------------|--------|
| `s` | ✅ | ✅ | [3] code |
| `t` | `"STOCK"` | `"DERIVATIVES"` | Hardcode |
| `ti` | ✅ | ✅ | [2] time |
| `o` | ✅ (VND) | ✅ (điểm) | [6] open.value |
| `h` | ✅ | ✅ | [8] high.value |
| `l` | ✅ | ✅ | [10] low.value |
| `c` | ✅ | ✅ | [12] last.value |
| `ch` | ✅ | ✅ | [14] change.value |
| `ra` | ✅ | ✅ | [15] changeRate |
| `vo` | ✅ | ✅ | [19] volume |
| `va` | ✅ | ✅ | [18] value |
| `mv` | ✅ | ✅ | [20] matchedVolume.value |
| `a` | ✅ | ✅ | [16] averagePrice |
| `mb` | ✅ | ✅ | [21] matchedVolume.type |
| `tb` | ✅ | ✅ | [28] total_bid_size |
| `to` | ✅ | ✅ | [29] total_offer_size |
| `tor` | ✅ | ❌ null | Không có trong WS |
| `fr.bv` | ✅ | ✅ | [32] foreignerBuySize |
| `fr.sv` | ✅ | ✅ | [33] foreignerSellSize |
| `fr.cr` | ✅ | ❌ null | Không có room |
| `fr.tr` | ✅ | ❌ null | Không có room |

### 4.2 Kết luận

**Giữ nguyên tên field** của cơ sở để FE không cần thay đổi logic xử lý.

**Khác biệt chính:**
1. `t` = `"DERIVATIVES"` thay vì `"STOCK"`
2. `tor`, `fr.cr`, `fr.tr` = null hoặc không gửi
3. Đơn vị giá = điểm (point) thay vì VND

---

## 5. Implementation Reference

### 5.1 Parser Code (Pseudo)

```java
// Lotte WS message: pipe-separated string
String[] parts = message.split("\\|");

DerivativeQuote quote = new DerivativeQuote();
quote.setCode(parts[3]);                           // s
quote.setType("DERIVATIVES");                      // t
quote.setTime(convertToUTC(parts[2]));            // ti
quote.setOpen(parseDouble(parts[6]));             // o
quote.setHigh(parseDouble(parts[8]));             // h
quote.setLow(parseDouble(parts[10]));             // l
quote.setLast(parseDouble(parts[12]));            // c
quote.setChange(parseDouble(parts[14]));          // ch
quote.setRate(parseDouble(parts[15]));            // ra
quote.setVolume(parseLong(parts[19]));            // vo
quote.setValue(parseLong(parts[18]));             // va
quote.setMatchingVolume(parseLong(parts[20]));    // mv
quote.setAveragePrice(parseDouble(parts[16]));    // a
quote.setMatchedBy(parts[21].equals("B") ? "BID" : "ASK");  // mb
quote.setTotalBidVolume(parseLong(parts[28]));    // tb
quote.setTotalOfferVolume(parseLong(parts[29]));  // to
quote.setForeignerBuyVolume(parseLong(parts[32])); // fr.bv
quote.setForeignerSellVolume(parseLong(parts[33])); // fr.sv
```

---

*TEMPO - Mapping có thể thay đổi khi implement thực tế*
