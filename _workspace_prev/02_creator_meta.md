# Creator Meta

- **Target file:** `New feature in NHSV Pro/Market_Watch/GTGD_Chart/BE_Issue.md`
- **Action:** UPDATE (không tạo mới)
- **Draft location:** `_workspace/02_creator_draft.md`

## Thay đổi chính so với bản gốc

### 1. REQ-BE-GTGD-01 (EOD flush) — TỪ "sửa realtime-v2" → "verify only"

**Trước:** Yêu cầu sửa EOD flush job trong `realtime-v2` để thêm field `cv` vào MongoDB.

**Sau:** KHÔNG sửa `realtime-v2`. Chỉ verify runtime rằng `tradingValue` đã persist. Bằng chứng: `ChartService.ts:230` đã đọc từ Mongo cho luồng cũ.

**Lý do:** Analyst confirm `tradingValue` đã được EOD flush dump nguyên object xuống Mongo (`RedisService.java:249`). Không cần tạo field mới.

### 2. REQ-BE-GTGD-02 (market-query-v2) — TỪ "map cv → v" → "thêm va[] mới"

**Trước:** Map `cv → v` (overload `v` từ volume sang value). Response format giữ nguyên.

**Sau:** THÊM mảng mới `va[]` bên cạnh `v[]`. `v[]` GIỮ NGUYÊN semantic volume (`periodTradingVolume`).

**Lý do RISKY nếu overload `v`:**
- Break tất cả TradingView SDK chart client đang consume endpoint (SDK chuẩn: `v = volume`).
- Break các chart khác trong NHSV Pro (Symbol Detail, Market Mini Chart) đang đọc `v` như volume.
- `va` naming consistency với WS `market.quote.{s}` field `va` — FE mix history + realtime WS không cần convert.

### 3. MongoDB Schema — TỪ "thêm field cv" → "không đổi"

**Trước:** Đề xuất thêm field `cv: Number` vào collection `symbolQuoteMinutes`.

**Sau:** KHÔNG thay đổi schema. Field đã tên `tradingValue` (tồn tại trong `ISymbolQuoteMinutes.ts:1-14`).

### 4. Response format — TỪ "giữ nguyên" → "thêm `va[]`"

**Trước:** Format `{ s, t, v, c, o, h, l }` không đổi.

**Sau:** Bổ sung `va: number[]` — cumulative trading value tại mỗi timestamp. FE KHÔNG cần cumsum.

### 5. Effort — TỪ "3-4 ngày" → "~2 ngày"

Bỏ được phần realtime-v2 flush + Mongo migration.

### 6. Executive Summary — restructure

- Đổi từ "sửa job 15:30" → "expose field đã có sẵn ra response".
- Highlight `v[]` KHÔNG bị đổi (chống nhầm lẫn).
- Ghi rõ backward compatibility.

## Analyst gaps note vào doc

- **Symbol code inconsistency:** realtime-v2 dùng `VNINDEX`, market-query-v2 constant map `HOSE → 'VN'`. Đã note vào REQ-01 yêu cầu dev BE confirm code chính xác trong Mongo cho từng index (`db.c_symbol_quote_minute.distinct("code")`).
- **`tradingValue` cho record cũ:** Có thể null cho record trước ngày realtime-v2 hỗ trợ field. Đã handle fallback `va = 0` trong REQ-02.
- **Cron schedule 15:30:** Không hard-code trong code, lấy từ config `app.schedulers.saveRedisToDatabase`. Đã note vào REQ-01.

## Không thay đổi

- Executive Summary layout (giữ PM-first structure).
- Timeline section vẫn có (nhưng đổi content).
- Success Criteria checklist format.
- Test case table format.
- Document Status footer (theo convention C5).

## File output

- Content đầy đủ: `_workspace/02_creator_draft.md`
- Chưa ghi vào target file — chờ validator OK.
