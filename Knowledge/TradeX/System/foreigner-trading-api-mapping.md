# Foreigner Trading APIs — Field Reference & Gap Analysis

> **Part of:** [TradeX Knowledge Base](../_index.md)
> **Service:** `market-query-v2` (equity/stock only — derivatives not exposed)
> **Purpose:** Tra cứu API/field giao dịch khối ngoại (khối lượng + giá trị mua/bán/ròng), theo mã và theo sàn
> **Last Updated:** 2026-07-21
> **Source:** Code analysis `Knowledge/TradeX-MCP/market-query-v2-main` (Knowledge-first, verified against source)

---

## Liên quan

| Document | Mối quan hệ |
|----------|-------------|
| [symbolinfo-api-fields-guide.md](./symbolinfo-api-fields-guide.md) | Field reference chung của SymbolInfo (không có value khối ngoại — chỉ có `bv`/`sv` volume + room) |
| [New feature in NHSV Pro/Market_Watch/Foreign_Trading_Summary/BE_Issue.md](../../../New%20feature%20in%20NHSV%20Pro/Market_Watch/Foreign_Trading_Summary/BE_Issue.md) | Đề xuất BE bổ sung API tổng hợp cấp sàn (gap được ghi nhận ở đây) |

---

## 1. Tổng quan — 4 API hiện có (đều per-symbol/list, KHÔNG có API tổng cấp sàn)

| API | Method | Service method | Mục đích |
|---|---|---|---|
| `/api/v2/market/symbol/foreignerSummary` | GET | `SymbolService.ts:1095` `queryForeignerSummary` | List đầy đủ mã (theo `marketType`), có `bva`/`sva`/`nva` (value) chính xác |
| `/api/v2/market/symbol/{symbolCode}/foreigner` | GET | `SymbolService.ts:623` `querySymbolForeignerDaily` | Lịch sử theo ngày cho 1 mã |
| `/api/v2/market/topForeignerTrading` | GET | `SymbolService.ts:869` `queryTopForeignerTrading` | Top N mã GD khối ngoại mạnh nhất — value **ước lượng** |
| `/api/v2/market/ranking/foreigner` | GET | `RequestHandler.ts:132` `queryForeignerRanking` | Ranking mã theo `foreignerBuyVolume`/`SellVolume` |

**⚠️ Kết luận quan trọng:** Không có API/field nào trả **tổng cộng dồn ở cấp sàn** (1 số cho HOSE, 1 số cho HNX, 1 số cho UPCOM). Root cause: nguồn dữ liệu gốc từ Lotte (qua `market-collector-lotte`) chỉ cấp foreigner data theo từng mã (`ISymbolInfo`), không có ở cấp index (`Index*Response.java` không có field foreigner nào). Muốn có số theo sàn phải SUM thủ công từ list trả về của `foreignerSummary` (filter `marketType`).

---

## 2. `foreignerSummary` — I/O đầy đủ

**Request**

| Field | Type | Required | Default | Ý nghĩa |
|---|---|---|---|---|
| `fetchCount` | number | ❌ | 300 | Số bản ghi tối đa |
| `offset` | number | ❌ | 0 | Phân trang |
| `marketType` | enum (`ALL`,`HNX`,`HOSE`,`UPCOM`) | ❌ | `ALL` | Lọc theo sàn |
| `sortType` | enum (`CODE`,`NET_VALUE`,`NET_VOLUME`) | ❌ | `CODE` | Tiêu chí sắp xếp |

**Response** (mảng, mỗi phần tử = 1 mã) — builder `ResponseUtils.ts:913`

| Field | Ý nghĩa |
|---|---|
| `s` | Mã chứng khoán |
| `c` / `ch` / `ra` | Giá khớp gần nhất / thay đổi giá / % thay đổi |
| `m` | Market type |
| `bvo` / `svo` / `nvo` | KL mua / bán / ròng NN |
| `bva` / `sva` / `nva` | **Giá trị mua / bán / ròng NN** ✅ chính xác |
| `tr` / `cr` / `br` | Tổng room / room còn lại / % room còn lại |
| `hv` / `hr` | KL đang nắm giữ NN / % tỷ lệ nắm giữ |
| `cv` | ⚠️ Tên gây nhầm — thực chất là `tradingValue` (giá trị GD trong phiên), không liên quan NN |

---

## 3. `{symbolCode}/foreigner` — I/O đầy đủ

**Request** — `SymbolService.ts:637-640`

| Field | Type | Required | Default | Ý nghĩa |
|---|---|---|---|---|
| `symbol` | string | ✅ (path) | — | Mã chứng khoán |
| `fetchCount` | number | ❌ | 100 | Số bản ghi tối đa |
| `baseDate` | string | ❌ | ngày mai | Mốc điều kiện `date < baseDate` |
| `toDate` | string | ❌ | hôm nay | Ngày kết thúc |
| `fromDate` | string | ❌* | `1970101` | Ngày bắt đầu — ⚠️ **bug**: chỉ áp dụng nếu `toDate` cũng được truyền (dòng 640 check `request.toDate != null` thay vì `request.fromDate != null`) |

**Response** (mảng theo ngày) — builder `ResponseUtils.ts:606`

| Field | Ý nghĩa |
|---|---|
| `d` | Ngày |
| `bvo` / `svo` / `nvo` | KL mua / bán / ròng NN trong ngày |
| `bva` | ⚠️ **BUG** (`ResponseUtils.ts:612-613`): field tên "buy value" nhưng bị ghi đè bởi dòng gán tiếp theo → giá trị thực trả về là **sell value**. Không có field `sva` riêng. |
| `nva` | Giá trị ròng — tính đúng, độc lập với bug trên |
| `br` / `hr` | % được phép mua / % đang nắm giữ |
| `cr` / `tr` / `hv` / `cv` | Room còn lại / tổng room / KL nắm giữ / KL thay đổi room |

> Type annotation `Promise<ForeignerDailyResponse>` (số ít) không khớp giá trị trả về thực tế (`ForeignerDailyResponse[]`, `SymbolService.ts:660`).

---

## 4. `topForeignerTrading` — I/O đầy đủ

**Request** — `SymbolService.ts:875-878`

| Field | Type | Required | Default | Ý nghĩa |
|---|---|---|---|---|
| `fetchCount` | number | ❌ | 10 | Số bản ghi trả về |
| `offset` | number | ❌ | 0 | Phân trang |
| `marketType` | enum | ❌ | `ALL` | Lọc theo sàn |
| `upDownType` | enum (`UP`,`DOWN`) | ❌ | `DOWN` | Hướng sort theo giá trị ròng NN |

**Response** (mảng, top N mã, chỉ `SecuritiesTypeEnum.STOCK`) — builder `ResponseUtils.ts:990`

| Field | Ý nghĩa |
|---|---|
| `s` | Mã chứng khoán |
| `o`/`h`/`l`/`c`/`ch`/`ra` | OHLC + thay đổi giá |
| `vo` | Tổng khối lượng giao dịch |
| `mt` | Market type |
| `fbv` / `fsv` / `fnv` | Giá trị mua/bán/ròng NN — **ước lượng** = `foreignerBuyVolume/SellVolume × last`, KHÔNG phải giá trị khớp lệnh thực |

---

## 5. `ranking/foreigner`

Route `RequestHandler.ts:132`, action `queryForeignerRanking`. Ranking mã theo `foreignerBuyVolume`/`foreignerSellVolume` — vẫn per-symbol, chưa trace chi tiết I/O (chưa có nhu cầu sử dụng tại thời điểm viết tài liệu này).

---

## 6. Derivatives — có field trong model nhưng KHÔNG active

`market-collector-lotte-main/.../MappingHTS.java:320-327` map từ Lotte HTS: `NormalForeignerBuyValue/SellValue`, `NormalForeignerBuyVolume/SellVolume`, `PtForeignerTotalBuyValue/SellValue` (giao dịch thỏa thuận). Nhưng dòng gán tương ứng vào `symbolInfo` derivatives tại `LotteApiSymbolInfoService.java:382-383, 420-426` **đã bị comment out** → không có API nào expose field này cho phái sinh hiện tại.

---

## 7. Gap — API tổng hợp cấp sàn (chưa có)

Không tìm thấy API/field nào trả tổng khối lượng + giá trị mua/bán/ròng khối ngoại **cộng dồn theo sàn** (HOSE/HNX/UPCOM). Đã kiểm tra: `Knowledge/TradeX/`, toàn bộ service trong `market-query-v2-main/src/services/`, route table `RequestHandler.ts`, `realtime-v2` (không có field foreigner nào), và model `Index*Response.java` trong `market-collector-lotte-main` (không có field foreigner).

**Đề xuất BE** để lấp gap này: xem [BE_Issue.md](../../../New%20feature%20in%20NHSV%20Pro/Market_Watch/Foreign_Trading_Summary/BE_Issue.md) — hướng đề xuất (theo PO): **thay thế hẳn** response của `foreignerSummary` (per-symbol → per-market aggregate), không tạo route mới, vì API này hiện chưa có consumer. ⚠️ Cần BE tự xác nhận lại trước khi đổi breaking.

---

**Document Status:** ✅ Complete | **For:** PM / BA / BE Developer | **Next Steps:** Cập nhật khi BE triển khai API tổng hợp cấp sàn (nếu có)
