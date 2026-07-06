# Validator Report

**Date:** 2026-07-03
**Draft reviewed:** `_workspace/02_creator_draft.md`
**Target file:** `New feature in NHSV Pro/Market_Watch/GTGD_Chart/BE_Issue.md` (UPDATE existing)

---

## Status: PASS_WITH_WARNINGS

---

## Checklist Results

### 1. Technical accuracy — PASS

- `va` semantic mô tả đúng: **cumulative day trading value** (từ đầu phiên đến `t[i]`), khớp analyst findings (Lotte `parts[21]` snapshot cumulative, không phải per-minute delta).
- Draft phân biệt rõ:
  - `v[]` = `periodTradingVolume` = **volume delta per-minute** (giữ nguyên chuẩn TradingView SDK).
  - `va[]` = `tradingValue` = **cumulative** (khớp WS `market.quote.{s}` field `va`).
- Fallback `null → 0` cho record cũ (line 210, 225) — hợp lý, tránh throw error.

### 2. Backward compatibility — PASS

- `v[]` không đổi semantic (lines 21, 24, 50, 96, 124).
- `va[]` là field mới, additive → client cũ ignore field lạ, không break.
- TradingView Advanced Charts SDK chỉ đọc `s/t/o/h/l/c/v` → an toàn (line 257).
- Type `TradingViewHistoryResponse` bổ sung `va?: number[]` optional (line 151) — backward-compat.

### 3. Consistency với FE Issue — PASS_WITH_WARNINGS

- FE Issue (line 106) đã dùng `va` từ WS `market.quote.{s}` với semantic "cumulative trading value trong ngày — đã cumulative sẵn, không cần cumsum". **Khớp với BE draft** cho luồng realtime.
- BE draft (line 232-233) ghi rõ: "FE KHÔNG cần cumsum khi dùng `va` từ history" và naming consistency giữa history API + WS.

**⚠️ WARNING (out-of-scope for validator, ghi nhận để team-lead xử lý):**

- FE Issue line 90 vẫn ghi `v: FE cumsum` — nay đã lỗi thời sau khi BE thêm `va[]`. FE nên cập nhật để đọc `va[]` từ history response thay vì cumsum từ `v[]`.
- FE Issue line 98 note đề xuất endpoint mới `/tradingValue/intraday` — BE draft chọn cách khác (bổ sung `va` vào endpoint hiện có). Note này nên xoá/cập nhật khi FE Issue được revise.

**Không sửa FE Issue trong scope task này** — chỉ flag để team-lead quyết định.

### 4. Format — PASS

- **C3 (PM-readability):** Executive Summary (lines 10-54) KHÔNG có code block, chỉ prose + bảng ✅. Code blocks nằm trong "Technical Background" và "Detailed Requirements" (có header note "PM CAN SKIP").
- **C5 (Derivatives doc footer):** Lines 293-295 có đủ `Document Status | For | Next Steps` ✅.
- **Markdown ATX headers:** Toàn bộ dùng `#`, `##`, `###` ✅.
- **Fenced code blocks có language ID:** `typescript`, `json`, plain text — ✅ đủ.

### 5. Completeness — PASS

- Response JSON example (lines 237-248) có cả `v` (volume delta) và `va` (cumulative) ✅.
- BE changes rõ ràng:
  - `market-query-v2`: sửa `parseSymbolQuoteMinuteList` + `parseTradingviewDailyPeriodList` (line 103-131).
  - `realtime-v2`: **verify only, không sửa code** (line 159-167) — hợp lệ vì `tradingValue` đã persist.
- Symbol code inconsistency (`VNINDEX` vs `VN`) đã note ở REQ-BE-GTGD-01 (line 200-208) — analyst gap được surface đúng.

### 6. Convention TradeX naming — PASS

- Dùng field name TradeX: `tradingValue`, `periodTradingVolume`, `accountNumber` (không có ở đây), `va` (TradeX-native).
- KHÔNG có field name Lotte (`ord_qty`, `stk_cd`, `acnt_no`, ...).
- Naming `va` khớp WS channel `market.quote.{s}` → consistency chéo giữa 2 nguồn (history + realtime).

---

## Issues found

**None (blocking).**

**Non-blocking warnings:**

1. FE Issue (`Issues/FE_GTGD_Chart_Market_Watch.md`) line 90, 98 chưa cập nhật để phản ánh cách mới đọc `va` từ history response. **Recommendation:** team-lead giao ticket riêng cho tradex-creator update FE Issue sau khi BE_Issue này được approve.

---

## Changes made to draft

**None.**

Draft đạt chất lượng finalize, không cần chỉnh sửa nội dung. Ghi thẳng vào `BE_Issue.md`.

---

**Document Status:** ✅ Validated (PASS_WITH_WARNINGS) | For: team-lead | Next Steps: team-lead xử lý warning về FE Issue update
