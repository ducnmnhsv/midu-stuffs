# SymbolInfo API — Hướng dẫn các field (Field Reference)

> **Part of:** [TradeX Knowledge Base](../_index.md)  
> **API:** `GET /api/v2/market/symbolInfo` (và tương đương `GET /api/v2/market/symbol/latest`)  
> **Purpose:** Tra cứu nhanh ý nghĩa từng field trong response — dùng cho PM, FE, QA  
> **Last Updated:** 2026-03-19

---

## Liên quan

| Document | Mối quan hệ |
|----------|-------------|
| [symbol-info-api.md](./symbol-info-api.md) | Kiến trúc aggregation, data flow, Redis |
| [_index.md](../_index.md) | TradeX Knowledge tổng quan |

---

## 1. API & Request

| Mục | Giá trị |
|-----|---------|
| **Endpoint** | `GET /api/v2/market/symbolInfo` |
| **Query** | `symbolList` — danh sách mã (ví dụ: `symbolList=VCB,VNM,VIX`) |
| **Response** | Mảng object — mỗi phần tử là một SymbolInfo (abbreviated fields) |

Response dùng **tên field rút gọn** (ví dụ `s`, `c`, `ce`) để tiết kiệm băng thông. Tài liệu này giải thích từng field.

---

## 2. Bảng tổng hợp theo nhóm

### 2.1 IDENTIFICATION — Định danh mã

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `s` | symbol | string | Mã chứng khoán | `"VIX"`, `"TCB"` |
| `t` | type | string | Loại sản phẩm | `STOCK`, `ETF`, `CW`, `INDEX`, `FUTURES` |
| `m` | market | string | Sàn (equity) hoặc nhóm (derivatives) | `HOSE`, `HNX`, `UPCOM`, `INDEX`, `BOND` |
| `n1` | name1 | string | Tên tiếng Việt | `"Công ty Cổ phần Chứng khoán VIX"` |
| `n2` | name2 | string | Tên tiếng Anh | `"VIX Securities Joint Stock Company"` |

**Ghi chú:** Với **phái sinh**, `m` = `"INDEX"` hoặc `"BOND"` (để FE hiển thị đúng tên chỉ số/trái phiếu).

---

### 2.2 TIME — Thời gian

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `ti` | time | string | Thời gian cập nhật quote (UTC), HHmmss | `"074500"` |
| `bot` | bidOfferTime | string | Thời gian cập nhật sổ lệnh (UTC), HHmmss | `"074500"` |
| `lt` | lastTradingTime | string | Thời gian giao dịch cuối — lần khớp lệnh cuối (UTC), HHmmss | `"170000"` |
| `ss` | session | string | Phiên giao dịch | `ATO`, `LO`, `ATC`, `PLO`, `CLOSED` |

---

### 2.3 PRICE DATA — Giá

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `o` | open | number | Giá mở cửa | `16350` |
| `h` | high | number | Giá cao nhất trong ngày | `16450` |
| `l` | low | number | Giá thấp nhất trong ngày | `15950` |
| `c` | current / close | number | Giá hiện tại (giá khớp gần nhất) | `16200` |
| `ch` | change | number | Thay đổi giá so với giá tham chiếu | `-400` |
| `ra` | rate | number | % thay đổi giá | `-2.4096` |
| `a` | averagePrice | number | Giá trung bình trong ngày | `16200` |

**Đơn vị:** Cổ phiếu/ETF/CW = VND (đồng); Hợp đồng tương lai = điểm (point).

---

### 2.4 REFERENCE PRICES — Giá tham chiếu / trần sàn

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `ce` | ceilingPrice | number | Giá trần | `17750` |
| `fl` | floorPrice | number | Giá sàn | `15450` |
| `re` | referencePrice | number | Giá tham chiếu | `16600` |

---

### 2.5 VOLUME DATA — Khối lượng & giá trị

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `vo` | volume | number | Khối lượng giao dịch trong ngày | `30442100` |
| `va` | value | number | Giá trị giao dịch (VND) | `492667560000` |
| `mv` | matchingVolume | number | Khối lượng khớp lệnh cuối (lệnh vừa khớp) | `500` |
| `mb` | matchedBy | string | Bên chủ động khớp lệnh cuối | `"ASK"`, `"BID"` |
| `tor` | turnoverRate | number | Tỷ lệ quay vòng (vo / lq), thường % | `1.9878` |

**matchedBy:** ASK = bên bán chủ động (giá khớp từ bên bán); BID = bên mua chủ động.

---

### 2.6 BID/OFFER DATA — Sổ lệnh

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `tb` | totalBidVolume | number | Tổng khối lượng dư mua | `1321200` |
| `to` | totalOfferVolume | number | Tổng khối lượng dư bán | `443300` |
| `bb` | bestBids | array | Các mức giá mua (3–10 levels) | `[{p,v,c}, ...]` |
| `bo` | bestOffers | array | Các mức giá bán (3–10 levels) | `[{p,v,c}, ...]` |

**Cấu trúc phần tử trong `bb` / `bo`:**

| Sub-field | Ý nghĩa | Kiểu |
|-----------|---------|------|
| `p` | price — Giá | number |
| `v` | volume — Khối lượng | number |
| `c` | change — Thay đổi KL so với lần trước (optional) | number \| null |

---

### 2.7 EXPECTED DATA — Giá dự kiến (ATO/ATC)

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `ep` | expectedPrice | number | Giá dự kiến khớp (trong phiên ATO/ATC) | `16200` |

Có ý nghĩa chủ yếu trong phiên **ATO** (mở cửa) và **ATC** (đóng cửa).

---

### 2.8 FOREIGNER DATA — Đầu tư nước ngoài (ĐTNN)

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `fr` | foreigner | object | Object chứa các chỉ số ĐTNN | `{bv, sv, cr, tr}` |
| `fr.bv` | buyVolume | number | Khối lượng mua ĐTNN trong ngày | `66030` |
| `fr.sv` | sellVolume | number | Khối lượng bán ĐTNN trong ngày | `782615` |
| `fr.cr` | currentRoom | number | Room nước ngoài còn lại | `1420010520` |
| `fr.tr` | totalRoom | number | Tổng room nước ngoài | `1531429858` |

**Phái sinh:** `fr.cr` và `fr.tr` thường là `null` (không áp dụng room).

---

### 2.9 STATIC / METADATA — Dữ liệu tĩnh

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `lq` | listedQuantity | number | Khối lượng niêm yết / đăng ký giao dịch | `1531429858` |
| `mc` | marketCap | number | Vốn hóa thị trường (VND) | `25421735642800` |

**Phái sinh:** `lq`, `mc` thường là `null`.

---

### 2.10 AGGRESSIVE MATCHED — Khớp chủ động (phiên)

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `abv` | accumulatedBidVolume | number | Tổng KL khớp với bên mua chủ động trong phiên (aggressive buy) | `20130400` |
| `asv` | accumulatedSellVolume | number | Tổng KL khớp với bên bán chủ động trong phiên (aggressive sell) | `10311700` |

Dùng cho màn **Current price** (thống kê “Aggressive matched”). Có thể suy từ `mb` + `mv` tích lũy trong phiên nếu BE chưa trả sẵn.

---

### 2.11 HISTORICAL DATA — Giá cao/thấp theo năm

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `hly` | highLowYearly | array | Giá cao nhất / thấp nhất theo năm | `[{h,l,hd,ld}, ...]` |
| `hly[].h` | high | number | Giá cao nhất năm | `40450` |
| `hly[].l` | low | number | Giá thấp nhất năm | `10450` |
| `hly[].hd` | highDate | string \| null | Ngày đạt giá cao nhất | `null` |
| `hly[].ld` | lowDate | string \| null | Ngày đạt giá thấp nhất | `null` |

**Phái sinh:** `hly` thường là `null` (hợp đồng ngắn hạn).

---

## 3. Fields đặc thù phái sinh (Derivatives)

Chỉ có trong response khi mã là **FUTURES**, **CW**, v.v. (tùy triển khai).

| Field | Full name | Kiểu | Ý nghĩa (Vietnamese) | Ví dụ |
|-------|-----------|------|------------------------|--------|
| `oi` | openInterest | number | Số hợp đồng mở (chưa đóng) | `45000` |
| `bc` | baseCode | string | Mã chỉ số cơ sở | `"VN30"` |
| `ftd` | firstTradingDate | string | Ngày giao dịch đầu tiên, yyyyMMdd | `"20250101"` |
| `ed` | expiryDate | string | Ngày đáo hạn, yyyyMMdd | `"20250227"` |
| `rd` | remainingDays | number | Số ngày còn lại đến đáo hạn | `28` |
| `tp` | theoryPrice | number | Giá lý thuyết (sở tính) | `1284.0` |
| `bs` | basis | number | Chênh lệch = Giá HĐTL − Spot (điểm) | `1.5` |

---

## 4. Quick lookup — Abbreviation → Full name

```
s    → symbol                 m    → market
t    → type                   n1   → name1 (VN)
n2   → name2 (EN)             ti   → time
bot  → bidOfferTime           lt   → lastTradingTime
ss   → session               o    → open
h    → high                   l    → low
c    → current/close         ch   → change
ra   → rate                  a    → averagePrice
ce   → ceilingPrice          fl   → floorPrice
re   → referencePrice        vo   → volume
va   → value                 mv   → matchingVolume
mb   → matchedBy             tor  → turnoverRate
tb   → totalBidVolume        to   → totalOfferVolume
bb   → bestBids              bo   → bestOffers
ep   → expectedPrice         fr   → foreigner
lq   → listedQuantity        mc   → marketCap
hly  → highLowYearly         abv  → accumulatedBidVolume
asv  → accumulatedSellVolume

// Derivatives
oi   → openInterest          bc   → baseCode
ftd  → firstTradingDate     ed   → expiryDate
rd   → remainingDays         tp   → theoryPrice
bs   → basis
```

---

## 5. Session (`ss`) — Giá trị thường gặp

| Giá trị | Ý nghĩa |
|---------|---------|
| `ATO` | Khớp lệnh mở cửa |
| `LO` | Phiên liên tục |
| `ATC` | Khớp lệnh đóng cửa |
| `PLO` | Post limit order |
| `CLOSED` | Đã đóng cửa |

---

## 6. Nguồn dữ liệu (tóm tắt)

| Nhóm field | Nguồn cập nhật (realtime) |
|------------|----------------------------|
| Giá, KL, ĐTNN, abv/asv | Quote (Kafka: quoteUpdate) |
| Sổ lệnh (bb, bo, tb, to), ss, bot | BidOffer (Kafka: bidOfferUpdate) |
| ep (expected) | Extra (Kafka: extraUpdate, phiên ATO/ATC) |
| Tên, ce/fl/re, lq, hly, mc | Init / static (symbol_static, symbolInfoUpdate) |

Chi tiết luồng: [symbol-info-api.md](./symbol-info-api.md).

---

**Document Status:** ✅ Active  
**For:** PM, FE, QA — tra cứu nhanh field API SymbolInfo  
**Next Steps:** Cập nhật khi BE bổ sung field mới hoặc đổi tên.
