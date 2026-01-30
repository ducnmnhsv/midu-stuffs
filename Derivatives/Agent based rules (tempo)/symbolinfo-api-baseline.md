# SymbolInfo API Baseline - Cơ sở (Equity)

> **Status:** TEMPO - Có thể thay đổi khi implement  
> **Purpose:** Lưu trữ pattern đã phân tích để tránh scan lại  
> **Created:** 2025-01-30

---

## 1. Actual Response từ API `/api/v2/market/symbolInfo`

**Sample: TCB (STOCK) - HOSE**

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

---

## 2. Full Field Reference (44 fields)

### IDENTIFICATION (5 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `s` | symbol | string | Mã chứng khoán | `"TCB"` |
| `t` | type | string | STOCK/ETF/CW/INDEX | `"STOCK"` |
| `m` | market | string | HOSE/HNX/UPCOM | `"HOSE"` |
| `n1` | name1 | string | Tên tiếng Việt | `"Ngân hàng TMCP..."` |
| `n2` | name2 | string | Tên tiếng Anh | `"Vietnam Tech..."` |

### TIME (4 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `ti` | time | string | Thời gian cập nhật quote (UTC HHmmss) | `"074500"` |
| `bot` | bidOfferTime | string | Thời gian cập nhật sổ lệnh | `"074500"` |
| `lt` | lastTradingTime | string | Thời gian giao dịch cuối | `"170000"` |
| `ss` | session | string | Phiên: ATO/LO/ATC/PLO/CLOSED | `"CLOSED"` |

### PRICE DATA (7 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `o` | open | number | Giá mở cửa | `34850` |
| `h` | high | number | Giá cao nhất ngày | `35900` |
| `l` | low | number | Giá thấp nhất ngày | `34800` |
| `c` | current/close | number | Giá hiện tại | `35900` |
| `ch` | change | number | Thay đổi giá | `1000` |
| `ra` | rate | number | % thay đổi | `2.8653` |
| `a` | averagePrice | number | Giá trung bình | `35300` |

### REFERENCE PRICES (3 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `ce` | ceilingPrice | number | Giá trần | `37300` |
| `fl` | floorPrice | number | Giá sàn | `32500` |
| `re` | referencePrice | number | Giá tham chiếu | `34900` |

### VOLUME DATA (5 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `vo` | volume | number | Khối lượng GD | `11603200` |
| `va` | value | number | Giá trị GD (VND) | `409488665000` |
| `mv` | matchingVolume | number | KL khớp cuối | `300` |
| `mb` | matchedBy | string | Bên khớp: ASK/BID | `"ASK"` |
| `tor` | turnoverRate | number | Tỷ lệ quay vòng | `0.1637` |

### BID/OFFER DATA (6 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `tb` | totalBidVolume | number | Tổng KL dư mua | `3525300` |
| `to` | totalOfferVolume | number | Tổng KL dư bán | `8077900` |
| `bb` | bestBids | array | Sổ lệnh mua | `[{p,v,c},...]` |
| `bo` | bestOffers | array | Sổ lệnh bán | `[{p,v,c},...]` |
| `bb[].p` / `bo[].p` | price | number | Giá | `35700` |
| `bb[].v` / `bo[].v` | volume | number | Khối lượng | `300` |
| `bb[].c` / `bo[].c` | change | number\|null | Thay đổi KL | `null` |

### EXPECTED DATA (1 field)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `ep` | expectedPrice | number | Giá dự kiến (ATO/ATC) | `35900` |

### FOREIGNER DATA (5 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `fr` | foreigner | object | Object ĐTNN | `{bv,sv,cr,tr}` |
| `fr.bv` | buyVolume | number | KL mua ĐTNN | `47000` |
| `fr.sv` | sellVolume | number | KL bán ĐTNN | `51500` |
| `fr.cr` | currentRoom | number | Room còn lại | `99` |
| `fr.tr` | totalRoom | number | Tổng room | `1597139381` |

### STATIC DATA (2 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `lq` | listedQuantity | number | KL niêm yết | `7086240414` |
| `mc` | marketCap | number | Vốn hóa (VND) | `247309790448600` |

### HISTORICAL DATA (5 fields)

| Field | Full Name | Type | Description | Example |
|-------|-----------|------|-------------|---------|
| `hly` | highLowYearly | array | Giá cao/thấp năm | `[{h,l,hd,ld}]` |
| `hly[].h` | high | number | Giá cao nhất năm | `42500` |
| `hly[].l` | low | number | Giá thấp nhất năm | `22300` |
| `hly[].hd` | highDate | string\|null | Ngày đạt giá cao | `null` |
| `hly[].ld` | lowDate | string\|null | Ngày đạt giá thấp | `null` |

---

## 3. Fields CHƯA có trong TradeX Knowledge (Mới phát hiện)

| Field | Full Name | Description | Cần update Knowledge? |
|-------|-----------|-------------|----------------------|
| `hly` | highLowYearly | Giá cao/thấp theo năm | ✅ Sau khi implement |
| `lt` | lastTradingTime | Thời gian GD cuối | ✅ Sau khi implement |
| `mc` | marketCap | Vốn hóa thị trường | ✅ Sau khi implement |

---

## 4. Quick Lookup - Abbreviation → Full Name

```
s    → symbol              m    → market
t    → type                n1   → name1 (VN)
n2   → name2 (EN)          ti   → time
bot  → bidOfferTime        lt   → lastTradingTime
ss   → session             o    → open
h    → high                l    → low
c    → current/close       ch   → change
ra   → rate                a    → averagePrice
ce   → ceilingPrice        fl   → floorPrice
re   → referencePrice      vo   → volume
va   → value               mv   → matchingVolume
mb   → matchedBy           tor  → turnoverRate
tb   → totalBidVolume      to   → totalOfferVolume
bb   → bestBids            bo   → bestOffers
ep   → expectedPrice       fr   → foreigner
lq   → listedQuantity      mc   → marketCap
hly  → highLowYearly
```

---

*TEMPO - Sẽ được review và update khi implement thực tế*
