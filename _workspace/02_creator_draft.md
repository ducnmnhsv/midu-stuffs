# BE Issue — GTGD Chart: Expose `va` (cumulative trading value) trên `/tradingview/history`

> **Loại tài liệu:** BE Technical Issue
> **Ngày tạo:** 2026-06-08
> **Ngày cập nhật:** 2026-07-03 (revised sau analyst trace)
> **Liên quan:** [PRD.md](./PRD.md) · [Issues/FE_GTGD_Chart_Market_Watch.md](./Issues/FE_GTGD_Chart_Market_Watch.md)

---

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

Để vẽ đường "GTGD phiên trước" trên chart, FE cần dữ liệu GTGD (giá trị giao dịch tích lũy) từng phút của ngày hôm trước. Endpoint `/tradingview/history` hiện tại **đã có** dữ liệu volume từng phút (`v[]`) nhưng **chưa expose** field GTGD ra client — dù dữ liệu này đã có sẵn trong Redis và MongoDB.

### Current vs Target

| | Hiện tại | Sau khi implement |
|---|---|---|
| MongoDB `c_symbol_quote_minute` | Đã lưu `tradingValue` per-minute ✅ (không cần đổi) | Không đổi |
| API `/tradingview/history` response | Có `v[]` (volume), thiếu `va[]` (GTGD) | Thêm `va[]` bên cạnh `v[]` |
| Line "GTGD phiên trước" | Không vẽ được (thiếu data) | Vẽ được từ `va[]` ngày hôm qua |
| Line "GTGD hôm nay" | Đã có (WS `market.quote.{s}` field `va`) | Không đổi |
| `v[]` semantic (volume) | Volume delta per-minute | **KHÔNG đổi** — giữ nguyên chuẩn TradingView |

### Solution Approach (HIGH-LEVEL)

**Chỉ sửa 1 service** — `market-query-v2`. Thêm mảng `va[]` (cumulative trading value) vào response của `/tradingview/history`, đọc trực tiếp từ field `tradingValue` đã có sẵn trong Redis (hôm nay) và MongoDB (ngày quá khứ).

**KHÔNG cần sửa `realtime-v2`** — EOD flush job đã lưu `tradingValue` xuống MongoDB từ trước; analyst đã verify field này persist thành công (bằng chứng: `ChartService.ts:230` đang đọc `quoteMinute.tradingValue` từ Mongo cho luồng GTGD Chart cũ).

**KHÔNG cần Mongo schema migration** — field đã tên `tradingValue` trong Mongo.

### Timeline

| Hạng mục | Effort |
|---|---|
| Update `parseSymbolQuoteMinuteList` — thêm `va[]` (intraday) | 0.5 ngày |
| Update `parseTradingviewDailyPeriodList` — thêm `va[]` (daily/weekly/monthly) | 0.5 ngày |
| Update type `TradingViewHistoryResponse` trong `tradex-models-market` | 0.5 ngày |
| Verify runtime MongoDB có `tradingValue` non-null cho ngày quá khứ | 0.5 ngày |
| Test + regression check TradingView SDK chart | 0.5 ngày |
| **Tổng** | **~2 ngày** |

### Success Criteria

- [ ] Verify MongoDB `c_symbol_quote_minute` đã có `tradingValue > 0` cho index symbols (VNINDEX/VN, HNXINDEX, UPCOM) — ngày quá khứ.
- [ ] Gọi `/tradingview/history` với `from/to` là ngày hôm qua → response có mảng `va[]` với giá trị **tăng dần** (cumulative), không rỗng.
- [ ] Gọi `/tradingview/history` cho ngày hôm nay → response có mảng `va[]` real-time, giá trị lấy từ Redis, tăng dần theo phút.
- [ ] Mảng `v[]` (volume) **KHÔNG bị thay đổi** — vẫn là `periodTradingVolume` delta per-minute, đúng chuẩn TradingView SDK.
- [ ] Response backward-compatible — client cũ không đọc `va` vẫn hoạt động bình thường.
- [ ] Data đúng cho cả 3 sàn: HOSE (VN-Index), HNX, UPCOM.
- [ ] Record cũ (nếu có) thiếu `tradingValue` → `va` trả `0` (fallback, không throw).

---

## 🔍 Technical Background (PM CAN SKIP)

### Luồng hiện tại (đã verify qua analyst)

```
Trong ngày:
  WebSocket (Lotte) → Kafka → realtime-v2 → Redis realtime_listQuoteMinute_{code}
    (record đã có field `tradingValue` = cumulative day trading value)

  FE gọi /tradingview/history:
    → market-query-v2 → CommonService.actualQueryQuoteMinuteHistory()
       - Range chứa hôm nay → đọc Redis
       - Range quá khứ     → đọc MongoDB c_symbol_quote_minute
    → parseSymbolQuoteMinuteList() → response { t, o, h, l, c, v, s }
       (❌ chưa map `tradingValue` ra response)

Cuối ngày (schedule cron config `app.schedulers.saveRedisToDatabase`):
  realtime-v2 EOD Flush Job:
    → LRANGE realtime_listQuoteMinute_{code} 0 -1
    → MongoBulkUtils.updateInBulk() dump nguyên object SymbolQuoteMinute
       vào MongoDB (đã bao gồm `tradingValue`) ✅
    → Clear Redis key
```

### Findings quan trọng (analyst)

1. **`tradingValue` đã có sẵn ở cả 3 tầng:**
   - Kafka minute payload (`MarketStockQuoteMinuteResponse.java:22`)
   - Redis minute record (`realtime_listQuoteMinute_{code}`)
   - MongoDB `c_symbol_quote_minute` (verify qua `ChartService.ts:230` đang consume)

2. **`va` là cumulative day-level, KHÔNG phải delta per-minute:**
   - Nguồn Lotte auto.qt `parts[21]` là snapshot cumulative
   - `ConvertUtils.updateByQuote()` overwrite (không sum) khi có tick mới trong cùng phút
   - `CommonService.ts:180` khi merge record cùng phút: `tradingValue = current.tradingValue` (lấy latest, không cộng dồn)
   - Contrast với `periodTradingVolume` = delta per-minute (được cộng dồn tại `CommonService.ts:174`)

3. **KHÔNG được overload `v` → value:**
   - Hiện tại `v = periodTradingVolume` (volume delta), đúng chuẩn TradingView SDK spec.
   - Nếu đổi ý nghĩa `v` sang trading value → break tất cả TradingView chart client đang consume `/tradingview/history`.
   - Giải pháp đúng: **thêm array mới `va[]`** — non-breaking, additive.

4. **Naming consistency với WS:** WS channel `market.quote.{symbol}` cũng dùng field `va` cho cùng semantic (cumulative day trading value). Dùng chung tên field → FE không cần map giữa 2 nguồn.

### Thay đổi cần làm

#### Service: `market-query-v2`

**File:** `src/utils/ResponseUtils.ts`

##### 1. Function `parseSymbolQuoteMinuteList()` (line 725-764) — intraday `1..60m`

Thêm mảng `va` và push `item.tradingValue`:

```typescript
const parseSymbolQuoteMinuteList = (symbolQuoteMinuteList, nextTime = null, noData = false) => {
  const t = [], o = [], h = [], l = [], c = [], v = [];
  const va = [];                                        // ← ADD

  // ... existing sort logic ...

  for (const item of sortedList) {
    t.push(...);
    o.push(item.open);
    h.push(item.high);
    l.push(item.low);
    c.push(item.last);
    v.push(Utils.round(item.periodTradingVolume));      // KHÔNG đổi (volume delta)
    va.push(Utils.round(item.tradingValue || 0));       // ← ADD (cumulative, fallback 0)
  }

  return { t, o, h, l, c, v, va, s, nextTime, noData }; // ← ADD va
};
```

##### 2. Function `parseTradingviewDailyPeriodList()` (line 766-799) — daily `1D/1W/1M/6M`

Tương tự: thêm `va[]` từ `SymbolPeriodResponse.tradingValue` (đã có sẵn cho daily period, verify tại `manualCalculateWeeklyMonthly` line 546-576).

##### 3. Type update

**Package:** `tradex-models-market` — interface `TradingViewHistoryResponse`

Thêm field optional:

```typescript
interface TradingViewHistoryResponse {
  s: 'ok' | 'no_data';
  t: number[];
  o: number[];
  h: number[];
  l: number[];
  c: number[];
  v: number[];
  va?: number[];                    // ← ADD (optional, backward compat)
  nextTime?: number;
  noData?: boolean;
}
```

Nếu package do team maintain → PR + version bump. Nếu frozen → cast object trước khi return.

#### Service: `realtime-v2`

**KHÔNG cần thay đổi.**

Verify checklist (không phải sửa code):

- ✅ `SymbolQuoteMinute` object đã carry `tradingValue` (`MarketStockQuoteMinuteResponse.java:22` + `QuoteService.java:364`).
- ✅ EOD flush upsert nguyên object (`RedisService.java:249`).
- ⚠️ Verify prod MongoDB: sample record ngày hôm qua có `tradingValue` non-null.

#### MongoDB Schema

**KHÔNG thay đổi.** Field `tradingValue` đã tồn tại trong collection `c_symbol_quote_minute` (`ISymbolQuoteMinutes.ts:1-14`).

---

## 📝 Detailed Requirements

### REQ-BE-GTGD-01: Verify MongoDB `tradingValue` đã persist cho ngày quá khứ

**Service:** `realtime-v2` (verify only, không sửa code)
**Trigger:** Manual verification trước khi FE tích hợp

**Logic verify:**

1. Đợi qua ít nhất 1 lần EOD flush job chạy (~15:30 theo config).
2. Query MongoDB prod với sample query:

   ```
   db.c_symbol_quote_minute.findOne({
     code: "VNINDEX",
     date: { $gte: <yesterday 09:00 epoch ms>, $lt: <yesterday 15:00 epoch ms> }
   })
   ```

3. Confirm field `tradingValue`:
   - Non-null
   - Value > 0 (giả sử phiên hôm qua có giao dịch)
   - Tăng dần theo `date` (cumulative)

4. Lặp lại cho code HNX và UPCOM.

**Lưu ý về symbol code trong MongoDB:**

Có convention khác biệt giữa 2 service (analyst đã note):

- **realtime-v2 / Kafka / Redis:** `VNINDEX`, `HNXINDEX`, `HNXUpcomIndex`
- **market-query-v2 constant `MARKET_INDEX_ENUM`:** `{ HOSE: 'VN', HNX: 'HNX', UPCOM: 'UPCOM' }` (ChartService.ts:189)

→ Dev BE cần **confirm chính xác code lưu trong Mongo** cho từng index (chạy `db.c_symbol_quote_minute.distinct("code")` filter theo index) trước khi FE gọi API.

**Error handling:** Nếu record cũ có `tradingValue = null` → `parseSymbolQuoteMinuteList` fallback `va = 0` (không throw). Sau ngày deploy, record mới sẽ đầy đủ.

---

### REQ-BE-GTGD-02: Expose `va[]` trong `/tradingview/history` response

**Service:** `market-query-v2`
**File:** `src/utils/ResponseUtils.ts`
**Function:** `parseSymbolQuoteMinuteList()` (line 725-764), `parseTradingviewDailyPeriodList()` (line 766-799)

**Điều kiện áp dụng:** Tất cả request `/tradingview/history` cho mọi resolution (`1..60`, `1D`, `1W`, `1M`, `6M`) và mọi range (hôm nay từ Redis, ngày quá khứ từ MongoDB).

**Logic:**

1. Trong loop build response, đọc `item.tradingValue` và push vào mảng mới `va[]`.
2. Fallback: nếu `item.tradingValue == null` (record cũ, đặc biệt trước ngày deploy realtime-v2 đã hỗ trợ `tradingValue`) → push `0`.
3. Không đổi logic `v[]` — vẫn là `Utils.round(item.periodTradingVolume)`.
4. Return object bổ sung `va` trong shape.

**Semantic quan trọng — ghi rõ vào doc cho FE:**

- `va[i]` = **cumulative trading value** (đơn vị VND) tính từ đầu phiên (09:00) đến `t[i]`.
- **FE KHÔNG cần cumsum** — data đã cumulative sẵn (khác với cách xử lý cũ khi cumsum từ `v[]` volume).
- Đây là **cùng semantic với WS `market.quote.{symbol}` field `va`** → naming consistency, FE có thể mix data từ 2 nguồn (history + realtime WS) mà không cần convert.

**Response format mới:**

```json
{
  "s": "ok",
  "t": [1719964800, 1719964860, 1719964920, 1719964980, ...],
  "o": [1280.5, 1281.2, 1281.8, 1282.1, ...],
  "h": [1281.5, 1282.0, 1282.5, 1282.8, ...],
  "l": [1280.2, 1281.0, 1281.5, 1281.9, ...],
  "c": [1281.2, 1281.8, 1282.1, 1282.5, ...],
  "v": [125000, 87000, 143000, 92000, ...],
  "va": [23017000000000, 23104000000000, 23247000000000, 23339000000000, ...]
}
```

Trong ví dụ trên:
- `v[i]` = volume delta trong phút `t[i]` (per-minute, không tích lũy)
- `va[i]` = cumulative trading value từ 09:00 → `t[i]` (đơn vị VND, tăng dần đến cuối phiên)

**Backward compatibility:**

- Client cũ không đọc `va` → không ảnh hưởng.
- TradingView Advanced Charts SDK chỉ đọc `s/t/o/h/l/c/v` → field lạ `va` sẽ bị SDK ignore, an toàn.
- Không break bất kỳ chart hiện có nào (Market Overview MiniChart, Symbol Detail Chart, ...).

---

### Kiểm tra sau khi deploy

| Test Case | Input | Expected |
|---|---|---|
| Query ngày hôm qua (intraday 1m) | `symbol=VNINDEX, from=yesterday 09:00, to=yesterday 15:00, resolution=1` | `va[]` non-empty, giá trị tăng dần từ đầu đến cuối, không toàn 0. `v[]` giữ nguyên semantic volume. |
| Query hôm nay (intraday 1m, in-session) | `symbol=VNINDEX, from=today 09:00, to=now, resolution=1` | `va[]` từ Redis, tăng dần theo phút. Value cuối cùng ≈ WS `va` snapshot cùng thời điểm. |
| Query daily (`1D`) | `symbol=VNINDEX, resolution=1D, from=7 days ago` | `va[]` cho mỗi ngày = tổng GTGD ngày đó. |
| Query 3 sàn | `HNX`, `UPCOM` (code chính xác theo dev BE confirm) | `va[]` non-empty, semantic cumulative giống VNINDEX. |
| TradingView SDK regression | Chart cũ trong app (Symbol Detail) | Vẫn render đúng — không đọc `va`, không bị `v` semantic đổi. |
| Record cũ thiếu `tradingValue` | Ngày trước khi realtime-v2 lưu field | `va[i] = 0` (fallback), không throw error, response vẫn hợp lệ. |
| Range mixed (hôm qua + hôm nay) | `from=yesterday 09:00, to=now, resolution=1` | Merge Redis (hôm nay) + Mongo (hôm qua). `va[]` liên tục, không jump/reset tại boundary 00:00. |

---

## 🔗 References

- Analyst findings: `_workspace/01_analyst_findings.md`
- FE Issue (consumer): `Issues/FE_GTGD_Chart_Market_Watch.md`
- PRD: `PRD.md`
- WS channel spec: `Knowledge/TradeX/System/market-data-channels.md` §3.4 (field `va`)
- Source references:
  - `market-query-v2/src/utils/ResponseUtils.ts:725-799` (target file để sửa)
  - `market-query-v2/src/services/ChartService.ts:230` (proof of Mongo `tradingValue` availability)
  - `market-query-v2/src/services/common/CommonService.ts:137-271` (Redis vs Mongo split logic)
  - `market-query-v2/src/models/db/ISymbolQuoteMinutes.ts:1-14` (Mongo record type)
  - `realtime-v2/src/main/java/.../services/RedisService.java:242-254` (EOD flush)
  - `tradex-common-java-main/.../MarketStockQuoteMinuteResponse.java:22` (Kafka minute payload with `tradingValue`)
  - `ws-v2-main/parser.js:287,354` (WS `va` field compression)

---

**Document Status:** ✅ Ready for development (revised)
**For:** BE Team (`market-query-v2` — 1 file, ~2 ngày)
**Next Steps:** Verify MongoDB (REQ-01) → Implement `parseSymbolQuoteMinuteList` + `parseTradingviewDailyPeriodList` (REQ-02) → Update `TradingViewHistoryResponse` type → Test regression TradingView SDK → Notify FE team
