# BE Issue — Foreign Trading Summary: Đổi `foreignerSummary` thành API tổng hợp theo sàn

> **Loại tài liệu:** BE Technical Issue
> **Ngày tạo:** 2026-07-21
> **Liên quan:** [Knowledge/TradeX/System/foreigner-trading-api-mapping.md](../../../Knowledge/TradeX/System/foreigner-trading-api-mapping.md)

---

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

Hệ thống hiện có 4 API trả dữ liệu giao dịch khối ngoại, nhưng **tất cả đều ở mức từng mã chứng khoán** — không có API nào trả về con số đã **cộng dồn ở cấp sàn** (1 số tổng cho HOSE, 1 số cho HNX, 1 số cho UPCOM). Muốn hiển thị "Tổng giá trị GD khối ngoại sàn HOSE hôm nay" cho user, hiện tại phải gọi API list toàn bộ mã rồi tự SUM ở phía client — tốn băng thông, tốn compute ở FE, và không có caching ở server.

**Theo PO xác nhận:** API `foreignerSummary` hiện tại **chưa được FE/client nào sử dụng** — nên đề xuất **thay thế hẳn** I/O của URI này (không giữ song song, không thêm param mới) thay vì tạo route mới, tránh phát sinh thêm 1 endpoint cần BE maintain. ⚠️ BE cần tự xác nhận lại việc "chưa ai dùng" trước khi đổi breaking, vì đây là input từ phía PO chứ chưa phải kết quả audit access log phía BE.

### Current vs Target

| Hạng mục | Hiện tại (Current) | Mục tiêu (Target) |
|---|---|---|
| URI | `GET /api/v2/market/symbol/foreignerSummary` | **Giữ nguyên URI**, đổi hẳn response shape |
| Response | List đầy đủ mã theo `marketType`, mỗi phần tử là 1 mã (`bva`/`sva`/`nva` per-symbol) | 1 object/sàn (HOSE/HNX/UPCOM) — **KHÔNG còn trả list theo mã** |
| Nội dung trả về | Volume + value per-symbol | Volume + value **đã tổng hợp (SUM)** theo sàn |
| Vị trí tính toán | Client tự SUM nếu cần số theo sàn (chưa ai làm) | Server-side tính sẵn |

### Solution Approach (HIGH-LEVEL)

**Không tạo route mới.** Sửa trực tiếp `queryForeignerSummary`/`toForeignerSummaryResponse` ở `market-query-v2` để SUM `foreignerBuyVolume`/`foreignerSellVolume`/`foreignerBuyValue`/`foreignerSellValue` của tất cả mã thuộc từng `marketType`, trả về response mới ở cấp sàn thay vì list per-symbol. Đây là **breaking change** về response shape của URI hiện có — cần BE xác nhận không có consumer nào đang phụ thuộc format cũ trước khi merge.

### Timeline

Cần BE xác nhận trong buổi grooming. Ước lượng sơ bộ: nhỏ (tái sử dụng data source có sẵn, chỉ đổi logic build response — không cần thêm route).

### Success Criteria

1. `foreignerSummary` trả về đủ 3 sàn (HOSE/HNX/UPCOM) — mỗi sàn gồm: tổng KL mua/bán/ròng NN, tổng giá trị mua/bán/ròng NN.
2. Số liệu tổng khớp với kết quả SUM thủ công từ dữ liệu per-symbol gốc (verify trước khi đổi response) tại cùng thời điểm.
3. Response cập nhật theo tick quote (không phải batch cuối ngày).
4. Không suy giảm hiệu năng của service `market-query-v2`.
5. BE xác nhận không có consumer nào đang dùng response format cũ (per-symbol) trước khi merge — nếu có, cần coordinate deprecation thay vì đổi thẳng.

---

## 🔍 Technical Background (PM CAN SKIP)

### Data source hiện có

Tất cả field cần SUM đã có sẵn per-symbol trong `ISymbolInfo` (Redis), đang được đọc bởi `foreignerSummary` (`SymbolService.ts:1095`, builder `ResponseUtils.ts:913`):

| Field nguồn (ISymbolInfo) | Field response hiện tại (`foreignerSummary`) |
|---|---|
| `foreignerBuyVolume` / `foreignerSellVolume` | `bvo` / `svo` |
| `foreignerBuyValue` / `foreignerSellValue` | `bva` / `sva` |

### Vì sao không có sẵn API tổng cấp sàn

Đã trace toàn bộ hệ thống (xem chi tiết: [foreigner-trading-api-mapping.md](../../../Knowledge/TradeX/System/foreigner-trading-api-mapping.md) §7):

- `market-query-v2-main/src/services/` không có `MarketService`/`IndexService`/`BoardService` nào xử lý foreigner aggregate.
- Model `Index*Response.java` trong `market-collector-lotte-main` (nguồn index/board) hoàn toàn không có field foreigner nào — root cause là ngay từ Lotte, foreigner data chỉ cấp theo từng mã, không có ở cấp index.
- 4 API foreigner hiện có (`foreignerSummary`, `{symbolCode}/foreigner`, `topForeignerTrading`, `ranking/foreigner`) đều per-symbol/list.

### Đề xuất thiết kế (cần BE xác nhận)

**Endpoint:** `GET /api/v2/market/symbol/foreignerSummary` (URI **giữ nguyên**, không tạo route mới)

**Request:** **Không thay đổi** — vẫn nhận `fetchCount`/`offset`/`marketType`/`sortType` như cũ. Với response mới ở cấp sàn, các param phân trang (`fetchCount`/`offset`) và `sortType` (`NET_VALUE`/`NET_VOLUME`) trở nên không còn ý nghĩa (chỉ tối đa 3-4 phần tử, không cần sort/phân trang) — BE cân nhắc giữ để tránh breaking type request, hoặc bỏ hẳn (xem clarify-question #2).

**Response mới** (mảng, 1 object/sàn — thay thế hoàn toàn response cũ per-symbol):

```json
[
  {
    "m": "HOSE",
    "bvo": 125430000,
    "svo": 98210000,
    "nvo": 27220000,
    "bva": 3521000000000,
    "sva": 2987000000000,
    "nva": 534000000000
  },
  { "m": "HNX", "bvo": "...", ... },
  { "m": "UPCOM", "bvo": "...", ... }
]
```

Giữ nguyên field name (`bvo`/`svo`/`nvo`/`bva`/`sva`/`nva`) và market type value (`HOSE`/`HNX`/`UPCOM`) — chỉ đổi **granularity** (per-symbol → per-market), không đổi semantic của từng field, để giảm rủi ro hiểu nhầm khi BE implement.

### Cách tính (đề xuất, cần BE xác nhận cách implement tối ưu)

Cách đơn giản nhất: mỗi khi có tick quote thay đổi field foreigner của 1 mã, cập nhật luôn 1 accumulator theo `marketType` (cộng dồn thay vì tính lại từ đầu mỗi request) — tương tự nguyên lý mà `foreignerSummary` đã tổng hợp theo list, nhưng lưu counter sẵn thay vì SUM runtime mỗi lần gọi API. BE có thể chọn giữa 2 hướng:

1. **Runtime SUM:** Mỗi request, loop toàn bộ mã theo `marketType` (giống `foreignerSummary` đang làm) rồi cộng dồn — đơn giản, không cần thay đổi luồng update, nhưng tốn compute mỗi request nếu gọi tần suất cao.
2. **Cached accumulator:** Giữ 1 counter theo sàn trong Redis, cộng dồn incremental mỗi khi field foreigner của 1 mã thay đổi — nhanh hơn khi đọc, nhưng cần xử lý đúng "diff" giữa giá trị cũ/mới (không phải cộng dồn tuyệt đối, vì field là cumulative theo phiên chứ không phải delta).

**Khuyến nghị:** Bắt đầu với runtime SUM (đơn giản, ít rủi ro sai số) — quyết định về cached accumulator để BE cân nhắc nếu cần tối ưu sau khi đo tải thực tế.

---

## 📝 Clarify-questions cho BE (cần chốt trong grooming)

1. **Xác nhận không có consumer nào** đang gọi `foreignerSummary` với response format cũ (per-symbol) — cần check access log / hỏi các team FE khác trước khi đổi breaking. Nếu có consumer, cần đổi hướng sang tạo route mới thay vì thay thế.
2. `fetchCount`/`offset`/`sortType` trong request — giữ nguyên (không dùng tới) hay bỏ khỏi type request cho gọn?
3. Runtime SUM hay cached accumulator — cách nào phù hợp với tải hệ thống hiện tại?
4. Có cần filter thêm theo `SecuritiesTypeEnum` (chỉ STOCK, loại trừ ETF/CW) như `topForeignerTrading` đang làm không, hay tính tất cả loại chứng khoán trong sàn?
5. Derivatives có cần tính năng tương tự không? (Hiện tại field foreigner cho derivatives đã có trong model nhưng chưa active — xem §6 trong Knowledge doc liên quan.)

---

## 🔗 References

- Knowledge (field reference đầy đủ 4 API hiện có + gap analysis): `Knowledge/TradeX/System/foreigner-trading-api-mapping.md`
- Source references:
  - `market-query-v2-main/src/services/SymbolService.ts:1095` (`queryForeignerSummary` — hàm cần sửa)
  - `market-query-v2-main/src/utils/ResponseUtils.ts:913` (`toForeignerSummaryResponse` — response builder cần sửa)
  - `market-collector-lotte-main/.../MappingHTS.java:320-327` (field foreigner derivatives — chưa active)

---

**Document Status:** 📋 Draft — chờ BE grooming
**For:** Backend team (`market-query-v2`)
**Next Steps:** BE xác nhận không có consumer nào dùng response cũ → trả lời clarify-questions → implement (đổi `queryForeignerSummary`/`toForeignerSummaryResponse`) → verify số liệu khớp SUM thủ công từ dữ liệu per-symbol gốc
