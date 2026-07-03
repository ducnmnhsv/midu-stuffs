# Analyst Findings — `va` field in `/tradingview/history`

**Analyst:** Claude (analyst subagent)
**Date:** 2026-07-03
**Scope:** Trace luồng dữ liệu market data để trả lời câu hỏi có thể thêm `va` (trading value) vào `/tradingview/history` không, và nếu có thì BE cần sửa gì.

**Verdict tổng:** ✅ Feasible. `tradingValue` (nguồn của WS `va`) **đã có sẵn** ở cả 3 tầng (Kafka minute payload, Redis SymbolQuoteMinute, và model MongoDB `ISymbolQuoteMinutes` interface trong query service). Cần chỉnh 2 chỗ ở code + 1 verify ở model Java realtime-v2.

---

## 1. `/tradingview/history` — Luồng hiện tại

### 1.1 Endpoint

| Property | Value |
|---|---|
| **URI** | `/api/v2/tradingview/history` |
| **Service** | `market-query-v2` |
| **File** | `src/consumers/RequestHandler.ts` (line 149) → `FeedService.queryTradingViewHistory()` |
| **Kafka topic** | `market-v2` |

### 1.2 Branching theo resolution

`FeedService.queryTradingViewHistory()` (`src/services/FeedService.ts:50-61`) split theo `RESOLUTION_MINUTE = ['1','3','5','10','15','30','60']`:

- **Intraday (`1..60`):** → `getQuoteMinuteHistory()` → `CommonService.actualQueryQuoteMinuteHistory()` → **Redis + Mongo** (`c_symbol_quote_minute`)
- **Daily/Weekly/Monthly (`1D/1W/1M/6M`):** → `getDailyPeriodHistory()` → Mongo `c_symbol_daily` (+ Redis overlay cho hôm nay)

Vì use case GTGD Chart = per-minute, chỉ quan tâm nhánh intraday.

### 1.3 Luồng đọc theo ngày (nhánh `getQuoteMinuteHistory`)

Trong `CommonService.actualQueryQuoteMinuteHistory()` (`src/services/common/CommonService.ts:137-271`), logic quyết định Redis vs Mongo dựa trên **so sánh ngày**:

```
fromTimeDayBase = floor(fromTime / 86400000)   // day epoch của from
nowDayBase      = floor(now / 86400000)         // day epoch của hôm nay
toTimeDayBase   = floor(newToTime / 86400000)   // day epoch của to (clamp về now)

nếu toTimeDayBase == nowDayBase:
   → đọc REDIS trước: LRANGE  realtime_listQuoteMinute_{code}  0 -1
                       filter by (item.date <= toTime && item.date >= fromTime)

nếu fromTimeDayBase < nowDayBase  hoặc  finalResults.length < countBack:
   → query MONGO: c_symbol_quote_minute  (SYMBOL_QUOTE_MINUTES repo)
                  filter: { date: { $lte: min(newToTime, startOfToday),
                                    $gte: fromTime },
                             code: symbol }
```

Tóm tắt:

| Query range | Data source |
|---|---|
| Ngày HÔM NAY | **Redis** `realtime_listQuoteMinute_{code}` (`SYMBOL_QUOTE_MINUTE` prefix trong `constants/index.ts:32` + `services/RedisService.ts:27`) |
| Ngày QUÁ KHỨ | **MongoDB** collection `c_symbol_quote_minute` (`COLLECTIONS_NAME.SYMBOL_QUOTE_MINUTE`) |
| Mixed range (chứa cả hôm nay + hôm qua) | Redis (cho phần hôm nay) + Mongo (cho phần quá khứ) — merge trong `processItem()` |

### 1.4 Response format hiện tại

`parseSymbolQuoteMinuteList()` (`src/utils/ResponseUtils.ts:725-764`):

```ts
return {
  t: [],  // Unix timestamp / 1000  (từ item.date)
  o: [],  // item.open
  h: [],  // item.high
  l: [],  // item.low
  c: [],  // item.last                ← NOTE: field DB tên là "last", không phải "close"
  v: [],  // item.periodTradingVolume ← per-minute VOLUME delta (không phải value!)
  s: 'ok' | 'no_data',
  nextTime,
  noData,
};
```

- **`t`** = Unix timestamp seconds (giây), zero-out ms + seconds → align đến phút.
- **`v`** = `periodTradingVolume` = **volume delta trong phút** (đã accumulate qua nhiều update trong cùng phút, xem `processItem` line 174: `placeHolderRecord.periodTradingVolume = placeHolderRecord.periodTradingVolume + current.periodTradingVolume`).
- **KHÔNG có `va` (trading value)** trong response, dù model `ISymbolQuoteMinutes` (line 9) đã có field `tradingValue`.

⚠️ **Điểm quan trọng**: BE_Issue.md hiện tại nói *"v trong response chính là trading value"* — điều này **KHÔNG đúng với code hiện tại**. Code đang set `v = periodTradingVolume` (volume, không phải value). Cần fix mô tả trong doc hoặc BE cần đổi hành vi.

---

## 2. EOD Flush Job (realtime-v2)

### 2.1 Trigger

| Property | Value |
|---|---|
| **File** | `src/main/java/com/techx/tradex/realtime/services/JobService.java` (line 88-91) |
| **Method** | `JobService.saveRedisToDatabase()` |
| **Schedule** | `@Scheduled(cron = "${app.schedulers.saveRedisToDatabase}")` — cron string từ config, **không hard-code 15:30 trong code**. Team ops confirm value trong config Vault/environment (Knowledge docs nói "15:30"). |
| **Ngoài scheduler** | Có thể trigger manual qua Kafka: `/job/saveRedisToDatabase` (`JobHandler.java:39`) |

### 2.2 Đọc gì từ Redis

`RedisService.saveRedisToDatabase()` (`src/main/java/.../services/RedisService.java:126`, block `if (isSaveQuoteMinute)` line 242-254):

```java
for (int i = 0; i < size; i++) {
    SymbolInfo symbolInfo = symbolInfoList.get(i);
    List<SymbolQuoteMinute> symbolQuoteMinuteList =
        redisDao.getAllSymbolQuoteMinute(symbolInfo.getCode());
    MongoBulkUtils.updateInBulk(mongoTemplate, 200, symbolQuoteMinuteList, SymbolQuoteMinute.class);
}
```

- Loop qua **tất cả `symbolInfo`** (bao gồm index như `VNINDEX`, `HNXINDEX`, `HNXUpcomIndex` — index cũng là symbol trong `SymbolInfo` list).
- `redisDao.getAllSymbolQuoteMinute(code)` = `LRANGE realtime_listQuoteMinute_{code} 0 -1` (theo constant `Constants.REDIS_KEY_SYMBOL_QUOTE_MINUTE = "realtime_listQuoteMinute"`).
- `MongoBulkUtils.updateInBulk(...)` upsert **toàn bộ list** thẳng vào collection `symbolQuoteMinutes` (tên MongoDB Java-mapping — tương ứng `c_symbol_quote_minute` trong query service).

### 2.3 Lưu gì xuống MongoDB

Toàn bộ **object `SymbolQuoteMinute`** được persist. Class `SymbolQuoteMinute` không nằm trong mirror (thuộc package third-party `com.difisoft.market.common`), nhưng dựa vào:

- **Model Kafka tương đương** `MarketStockQuoteMinuteResponse.java` (`tradex-common-java-main/.../MarketStockQuoteMinuteResponse.java`):

```java
private String code;
private String time;
private int last;
private int open;
private int high;
private int low;
private long tradingVolume;
private long tradingValue;       // ← có
private long lastValue;
private long periodTradingVolume;
```

- **Model tương đương ở query service** `ISymbolQuoteMinutes.ts:1-14`:

```ts
{ code, last, open, high, low, tradingVolume, tradingValue?, periodTradingVolume?, date, milliseconds, refCode }
```

- **Bằng chứng runtime**: `ConvertUtils.fromSymbolQuote(newSymbolQuoteMinute, symbolQuote)` (`QuoteService.java:364`) copy từ `StockQuoteUpdate.tradingValue` (line 24 model) sang `SymbolQuoteMinute`. Ngoài ra `ChartService.ts:230` **đã đang** đọc `quoteMinute.tradingValue` từ Mongo cho GTGD Chart hôm-nay-trước đó → chứng minh field này persist thành công.

### 2.4 Kết luận `tradingValue` trong MongoDB

✅ **Field `tradingValue` ĐÃ được lưu** xuống MongoDB `symbolQuoteMinutes` — cả nhánh Redis (real-time write bởi QuoteService) và nhánh EOD flush (đơn giản dump toàn bộ Redis object sang Mongo).

Tên field trong MongoDB = **`tradingValue`** (không phải `cv` như BE_Issue.md draft đang đề xuất). ChartService của market-query-v2 đọc trực tiếp field này (`symbolQuoteMinutes.tradingValue` → `[minute, quoteMinute.tradingValue]`).

⚠️ **Cần verify runtime**: mở MongoDB check một document sample của `c_symbol_quote_minute` từ ngày hôm qua → confirm field `tradingValue` > 0 và increment theo phút. Nếu record cũ không có field này, các record trước ngày deploy có thể `tradingValue = null / 0`.

---

## 3. WS quote channel — field `va`

### 3.1 Nguồn

- WS channel: `market.quote.{symbol}` (topic Kafka: `quoteUpdate`).
- Publisher: `ws-v2-main` (Node.js) — `parser.js` compress field names khi push ra client.

### 3.2 Mapping

`ws-v2-main/parser.js` line 287 và 354:

```js
if (data.tradingValue != null) result.va = data.tradingValue;
```

Cả 2 dòng đều nằm trong function convert quote/futures snapshot ra shape publish V2.

### 3.3 Bản chất cumulative hay per-minute?

**Cumulative day-level (tích lũy từ 09:00).**

Bằng chứng:

1. **Nguồn Lotte** (`market-data-channels.md` section 3.4): `va` = "Giá trị giao dịch (VND)" — Lotte cung cấp cumulative day trading value trên channel `auto.qt`. Không có concept per-tick value.
2. **Mapping raw** (`market-data-channels.md` section 3.5): `parts[21] → tradingValue` (không phải delta).
3. **StockUpdateData.java** (`market-collector-lotte-main/.../StockUpdateData.java:79`): `this.setTradingValue(stockAutoItem.getValue().getValue() * 1000000)` — set trực tiếp giá trị Lotte, không tính diff.
4. **StockQuoteUpdate** Kafka model (`tradex-common-java-main/.../StockQuoteUpdate.java:24`): `private long tradingValue;` — snapshot cumulative.
5. **Trong minute record**: `ConvertUtils.updateByQuote(symbolQuoteMinute, symbolQuote)` (`QuoteService.java:367`) — mỗi tick trong cùng phút overwrite `tradingValue` bằng cumulative mới nhất. Điều này còn thể hiện rõ ở `CommonService.ts:180-181`: khi merge nhiều record cùng phút, `tradingValue = current.tradingValue` (lấy latest, KHÔNG cộng dồn) — vì đã là cumulative.

Đối chiếu với `periodTradingVolume` — field per-minute delta — được cộng dồn (`CommonService.ts:174`, `MarketStockQuoteMinuteResponse.java:26-28` set method `Math.abs` để normalize). Đây là bằng chứng contrast: `tradingValue` không có set method special, không được cộng dồn → khẳng định cumulative.

### 3.4 Tên đầy đủ

- WS output field: `va` (compressed)
- Kafka + Redis + Mongo full name: `tradingValue`

---

## 4. Redis `SYMBOL_QUOTE_MINUTE_{code}`

### 4.1 Full key

- Prefix constant: `realtime_listQuoteMinute` (`RedisService.ts:27`, `Constants.java:11`)
- Full key: `realtime_listQuoteMinute_{code}` — ví dụ `realtime_listQuoteMinute_VNINDEX`, `realtime_listQuoteMinute_VCB`.
- Data type: **Redis LIST** (đọc bằng `LRANGE 0 -1`).

### 4.2 Field `tradingValue` trong record

✅ **Có**. Mỗi item trong list là 1 `SymbolQuoteMinute` object, contain:

```
{ code, date, time, milliseconds, open, high, low, last, tradingVolume, tradingValue, periodTradingVolume, ... }
```

### 4.3 Format của `tradingValue` trong record minute

**Cumulative (snapshot cuối phút).**

Logic tạo/update minute (`QuoteService.java:348-376`):

- Nếu quote mới rơi vào **phút mới** → tạo record mới, copy toàn bộ field từ `SymbolQuote` (bao gồm cumulative `tradingValue`).
- Nếu quote mới trong **cùng phút** → `ConvertUtils.updateByQuote()` overwrite các field cumulative, cộng dồn `periodTradingVolume`.

⇒ `tradingValue` trong 1 minute record = **cumulative day trading value tính đến thời điểm quote cuối cùng trong phút đó**. Cùng bản chất với WS `va`. Không phải per-minute delta.

Confirm cross-service: `market-query-v2/src/services/common/CommonService.ts:180` khi group minute cũng lấy `tradingValue = current.tradingValue` (latest), không sum.

---

## 5. Thay đổi cần thiết để thêm `va` vào `/tradingview/history` response

### 5.1 Nhận định về deltas so với BE_Issue.md hiện tại

BE_Issue.md hiện tại đang giả định:

- (a) `tradingValue` **chưa** được lưu vào MongoDB → cần sửa EOD flush.
- (b) Cần thêm field mới tên `cv` vào Mongo schema.
- (c) Cần đổi ý nghĩa của `v` trong response từ volume → value ("v là GTGD, không phải volume — convention riêng của TradeX").

**Cả 3 giả định trên đều CẦN REVIEW LẠI:**

- (a) ❌ Sai — `tradingValue` đã có trong minute object và được EOD flush upsert nguyên xi vào Mongo (`RedisService.java:249`). Xác nhận qua `ChartService.ts:230` đã đọc `quoteMinute.tradingValue` từ Mongo cho ngày quá khứ.
- (b) ❌ Không cần — field tên trong Mongo đã là `tradingValue`. Thêm `cv` sẽ tạo trùng lặp không cần thiết. Nếu muốn short name để tiết kiệm bytes network, làm ở layer response (map `tradingValue → va`) chứ đừng đổi schema.
- (c) ⚠️ Risky — hiện tại `v = periodTradingVolume` (volume). Nếu đổi ý nghĩa `v` sang value, sẽ **break** mọi client hiện đang đọc `v` như volume (bao gồm chart cũ, TradingView SDK default binding — TradingView spec chuẩn: `v = volume`). Nên **thêm field mới `va`** thay vì overload `v`.

### 5.2 Đề xuất giải pháp

**Chỉ cần sửa 1 chỗ (market-query-v2). Realtime-v2 không cần sửa gì.**

#### 5.2.1 market-query-v2

**File:** `src/utils/ResponseUtils.ts`, function `parseSymbolQuoteMinuteList()` (line 725-764)

Thêm mảng `va` và push `item.tradingValue`:

```ts
const parseSymbolQuoteMinuteList = (symbolQuoteMinuteList, nextTime = null, noData = false) => {
  const t = [], o = [], h = [], l = [], c = [], v = [];
  const va = [];   // ← ADD

  ...
  for (const item of sortedList) {
    ...
    v.push(Utils.round(item.periodTradingVolume));
    va.push(Utils.round(item.tradingValue || 0));   // ← ADD (fallback 0 cho record cũ)
  }
  ...
  return { t, o, h, l, c, v, va, s, nextTime, noData };  // ← ADD va
};
```

Đồng thời cần cân nhắc thêm `va` vào `parseTradingviewDailyPeriodList()` (line 766-799) cho nhánh `1D/1W/1M/6M` — data source là `SymbolPeriodResponse` (đã có field `tradingValue` per period, xem `manualCalculateWeeklyMonthly` line 546-576).

**Contract type**: `TradingViewHistoryResponse` được declare trong package `tradex-models-market` (external). Cần update type để thêm optional `va?: number[]`. Nếu package do team maintain, PR update type; nếu không, dùng runtime object và cast.

#### 5.2.2 realtime-v2

**KHÔNG cần thay đổi.**

Verify checklist (không phải sửa):

- ✅ `SymbolQuoteMinute` object đã carry `tradingValue` field (xác nhận qua `MarketStockQuoteMinuteResponse.java:22` + `QuoteService.java:364`).
- ✅ EOD flush job đã upsert nguyên object vào MongoDB (`RedisService.java:249`).
- ⚠️ Cần **verify trên Mongo prod** rằng record hiện tại có non-null `tradingValue` (không bị lỗi serialization / null column). Nếu có sample record cũ với `tradingValue = null` → fallback trong response (`va.push(item.tradingValue || 0)`) sẽ handle.

### 5.3 `va` nên là cumulative hay per-minute?

**Cumulative — align với WS `va`.**

Lý do:

1. **Consistency**: WS `market.quote.{symbol}.va` là cumulative day. Nếu history trả delta, FE sẽ có 2 semantics khác nhau cho cùng field name `va` → confusion cực lớn.
2. **Data source**: Redis + Mongo hiện đang lưu `tradingValue` cumulative sẵn. Nếu cần delta, tính ở client: `delta[i] = va[i] - va[i-1]` (không tốn compute BE).
3. **Use case GTGD Chart phiên trước**: FE cần vẽ đường cumulative tăng dần → cần cumulative, không phải delta. Match với PRD "GTGD phiên trước tăng dần trong ngày".

### 5.4 Effort estimate revised

| Task | Effort | Note |
|---|---|---|
| Update `parseSymbolQuoteMinuteList` (add `va[]`) | 0.5 ngày | Trivial |
| Update `parseTradingviewDailyPeriodList` (add `va[]` cho 1D/1W/1M/6M) | 0.5 ngày | Optional — chỉ cần nếu FE dùng resolution daily |
| Update type `TradingViewHistoryResponse` trong `tradex-models-market` | 0.5 ngày | Nếu package internal thì cần PR + version bump |
| Verify MongoDB có `tradingValue` non-null cho record cũ | 0.5 ngày | Chạy `db.c_symbol_quote_minute.findOne({code: 'VNINDEX', date: {...}})` |
| Test | 0.5 ngày | |
| **Tổng** | **~2 ngày** | Bằng ~½ estimate cũ (3-4 ngày) vì bỏ được phần realtime-v2 flush + Mongo schema migration |

---

## 6. Gaps / Cần xác minh

### 6.1 Verify runtime

- [ ] Query MongoDB prod: `db.c_symbol_quote_minute.findOne({code: "VNINDEX", date: {$gte: yesterdayStart, $lt: yesterdayEnd}})` — check field `tradingValue` có value > 0 hay không.
- [ ] Query MongoDB prod cho index HNX và UPCOM (code chuẩn xác trong `MARKET_INDEX_ENUM = { HOSE: 'VN', HNX: 'HNX', UPCOM: 'UPCOM' }` — check ChartService.ts:189 dùng `MARKET_INDEX_ENUM[market]` → chỉ số HOSE lưu dưới code `'VN'` chứ không phải `'VNINDEX'`. Cần confirm với dev BE realtime-v2).
- [ ] Check cron config value `app.schedulers.saveRedisToDatabase` trong env config — confirm giờ chạy job (Knowledge docs nói 15:30, code không hard-code).

### 6.2 Contract & compatibility

- [ ] Kiểm tra `tradex-models-market` package: nếu package do team maintain (chung monorepo) → thêm `va?: number[]` vào interface `TradingViewHistoryResponse`. Nếu third-party frozen → phải trả object với extra property + type assertion.
- [ ] TradingView SDK expect gì? Endpoint `/tradingview/history` phục vụ cả `TradingView Advanced Charts SDK` (chuẩn: `s, t, o, h, l, c, v`) và custom GTGD chart. Nếu chỉ chart GTGD dùng `va` → OK. Nếu SDK chart cũng consume → SDK sẽ ignore field lạ, an toàn.

### 6.3 Điều chỉnh BE_Issue.md

BE_Issue.md hiện tại có 2 điểm cần sửa:

1. **Section "Thay đổi cần làm" - Service 1 (realtime-v2)**: Bỏ hoàn toàn phần thêm field `cv` — không cần thay đổi realtime-v2. Thay bằng "Verify MongoDB đã có tradingValue cho ngày quá khứ".
2. **Section "Thay đổi cần làm" - Service 2 (market-query-v2)**: Thay logic *"map cv → v"* bằng *"thêm mảng `va` mới bên cạnh `v`, không đè `v`"*. Kèm warning về TradingView SDK convention `v = volume`.
3. **Section "Response format giữ nguyên"**: Không giữ nguyên — cần thêm `va: number[]`. Cập nhật example JSON.
4. **Success criteria checkbox 1**: đổi từ "database có đủ dữ liệu GTGD từng phút" (đã có sẵn) → "verify database đã có dữ liệu GTGD từng phút cho ngày quá khứ".

---

## Appendix: Source File Reference

| Concern | File | Lines |
|---|---|---|
| `/tradingview/history` route | `market-query-v2/src/consumers/RequestHandler.ts` | 149 |
| Query entrypoint | `market-query-v2/src/services/FeedService.ts` | 50–61 |
| Intraday branch (Redis+Mongo) | `market-query-v2/src/services/FeedService.ts` | 63–122 |
| Redis vs Mongo split logic | `market-query-v2/src/services/common/CommonService.ts` | 137–271 |
| Response shape | `market-query-v2/src/utils/ResponseUtils.ts` | 725–764 (minute), 766–799 (daily) |
| Redis key constant | `market-query-v2/src/services/RedisService.ts` | 27 |
| Mongo collection constant | `market-query-v2/src/constants/index.ts` | 32 |
| Minute record type (query) | `market-query-v2/src/models/db/ISymbolQuoteMinutes.ts` | 1–14 |
| Kafka minute payload type | `tradex-common-java-main/.../MarketStockQuoteMinuteResponse.java` | 9–29 |
| Kafka quote payload type | `tradex-common-java-main/.../StockQuoteUpdate.java` | 24 |
| Redis write (minute create/update) | `realtime-v2/src/main/java/.../services/QuoteService.java` | 348–376 |
| Mongo flush (EOD) | `realtime-v2/src/main/java/.../services/RedisService.java` | 242–254 |
| Flush schedule | `realtime-v2/src/main/java/.../services/JobService.java` | 88–91 |
| Redis key constant (realtime) | `realtime-v2/src/main/java/.../constants/Constants.java` | 11 |
| WS `va` compression | `ws-v2-main/parser.js` | 287, 354 |
| Lotte raw → tradingValue | `market-collector-lotte-main/.../StockUpdateData.java` | 79 |
| Existing GTGD Chart consumer (Mongo tradingValue) | `market-query-v2/src/services/ChartService.ts` | 180–236 |

---

**Document Status:** ✅ Complete | For: Creator (next in pipeline) | Next Steps: Creator update `BE_Issue.md` để reflect findings — bỏ phần realtime-v2 flush, thay `cv` → `va` (thêm array mới, không overload `v`).
