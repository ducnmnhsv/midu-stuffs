# [Epic DR-FE-MKT] Story MKT.S1: Derivatives Market Display – Index name, Search, Current price, Home chart, Market lists, Price table

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-MKT – Derivatives Market FE  
> **Module:** Market  
> **Screens:** Search, Current price, Home, Market, Price table (ngang)  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-11  
> **Note:** Gộp từ MKT.S1 (Index name PS/DR) + MKT.S2 (Home chart, Market list, Price table)

---

## User Story

**As a** trader / user  
**I want to** thấy index name đúng (PS/DR, TPCP/GB), chart Index trên Home, danh sách Index/BOND trên Market, và bảng giá ngang Derivatives (mặc định Index futures, dropdown, error/empty state)  
**So that** tôi phân biệt rõ mã phái sinh chỉ số vs trái phiếu, theo dõi và giao dịch theo từng nhóm, và thấy trạng thái lỗi rõ ràng khi dữ liệu không tải được.

---

## Acceptance Criteria

### Phần 1: Index name (PS/DR, TPCP/GB) – nền tảng dùng chung

- [ ] **AC-01 – Nhận diện Derivatives**  
  Mã được coi là Derivatives nếu `t === "FUTURES"` **hoặc** `m in ["INDEX","BOND"]` trong dữ liệu symbol (symbol_static / symbolInfo / latest).

- [ ] **AC-02 – Index name theo `m` và ngôn ngữ**  
  Dùng field **`m`** (market) từ BE:
  - `m === "INDEX"`: VI → **PS**, EN → **DR**.
  - `m === "BOND"`: VI → **TPCP**, EN → **GB**.
  - `m` null/khác: fallback **PS**/**DR**.

- [ ] **AC-03 – Màn Search**  
  Kết quả tìm mã có Derivatives: cột "sàn"/index name hiển thị theo **`m` + ngôn ngữ** (PS/DR, TPCP/GB).

- [ ] **AC-04 – Màn Current price**  
  Header/subtitle hiển thị index name theo **`m` + ngôn ngữ**.

- [ ] **AC-05 – Không ảnh hưởng Equity**  
  Mã cơ sở (HOSE, HNX, UPCOM) vẫn hiển thị đúng như hiện tại.

### Phần 2: Home – Chart Index

- [ ] **AC-06** Chart Index derivatives được hiển thị trên Home. Mỗi chart (VN30, VN100) dùng **mã có ngày đáo hạn gần nhất** (front month contract).
- [ ] **AC-07** **VN30 chart:** Hiển thị chart của mã **41I1xxxx** với `ed` (ngày đáo hạn) **gần nhất** ≥ ngày hôm nay. (Thực tế có nhiều mã 41I1xxxx với ngày đáo hạn khác nhau – VD: VN30F2501, VN30F2502, VN30F2503…).
- [ ] **AC-08** **VN100 chart:** Tương tự, hiển thị chart của mã **41I2xxxx** với `ed` gần nhất ≥ today.

### Phần 3: Market – List Index, BOND, tab Derivatives

- [ ] **AC-09** Có khu vực/list riêng cho **Index** (`m === "INDEX"`): chỉ hiển thị các mã có `m === "INDEX"`.
- [ ] **AC-10** Có khu vực/list riêng cho **Gov.Bond future** (`m === "BOND"`): chỉ hiển thị các mã có `m === "BOND"`.
- [ ] **AC-11** Tab **Derivatives** hiển thị tất cả mã phái sinh từ `symbol_static.json`.

### Phần 4: Bảng giá ngang Derivatives

- [ ] **AC-12** Default selection của bảng giá ngang Derivatives là **Index futures** (`m === "INDEX"`).
- [ ] **AC-13** Khi click dropdown/selector, hiển thị danh sách option (Index futures, Gov Bond, …) theo Figma 40005162-208618.
- [ ] **AC-14** Khi xảy ra **error loading**: hiển thị state error theo Figma 40005162-206999 (copy/retry).
- [ ] **AC-15** **Empty state:** Khi không có symbol nào sau filter: hiển thị *"Không có dữ liệu"* / *"No data"*; không hiển thị bảng rỗng không có message.

---

## Tasks (Implementation)

- [ ] **T1** Helper `getSymbolIndexName(symbol, lang)` – map `m` (INDEX/BOND) → PS/DR, TPCP/GB. Đảm bảo nhận diện Derivatives nhất quán (`t === "FUTURES"` hoặc `m in ["INDEX","BOND"]`).
- [ ] **T2** Search: `SearchScreen` / `SearchSymbolScreen` – danh sách kết quả hiển thị index name theo `m` + lang cho mã Derivatives. **FE path:** `src/screens/SearchScreen/`, `SearchSymbolScreen/`.
- [ ] **T3** Current price: `CurrentPriceScreen` – header/subtitle hiển thị index name. **FE path:** `src/screens/CurrentPriceScreen/`.
- [ ] **T4** Home: Thêm block chart Index derivatives; **chọn mã front month** (ngày đáo hạn gần nhất) cho 41I1 (VN30) và 41I2 (VN100); tích hợp component chart. **FE path:** `src/screens/HomeTab/components/MarketInfoTab/components/IndexOverView/`; chart: `src/components/Chart/`.
- [ ] **T5** Market: List Index (filter `m === "INDEX"`), list Gov Bond (filter `m === "BOND"`); tab Derivatives = toàn bộ symbol phái sinh. **FE path:** `src/screens/MarketScreen/` – marketPriceBoard, bodyForm.
- [ ] **T6** Bảng giá ngang: Default = Index futures; dropdown theo Figma; xử lý error + empty state. **FE path:** `src/screens/HorizontalPriceBoardScreen/` – BodyHorizontalTable, pickerMarket.
- [ ] **T7** Áp dụng index name (PS/DR, TPCP/GB) tại Market list và Price table (nếu design có cột index name).

---

## Background / Context

Dữ liệu từ `symbol_static.json` và API symbolInfo/latest. Nhận diện Derivatives qua `m === "INDEX"` hoặc `m === "BOND"` (và `t === "FUTURES"`).

| Loại hợp đồng | Quy tắc mã | `m` | Index name (VI) | Index name (EN) |
|---------------|------------|-----|-----------------|-----------------|
| HĐTL chỉ số phái sinh | 41**I**xxxxxx | `"INDEX"` | **PS** | **DR** |
| HĐTL trái phiếu CP | 41**B**xxxxxx | `"BOND"` | **TPCP** | **GB** |

| Màn | Yêu cầu |
|-----|---------|
| Search | Index name PS/DR, TPCP/GB trong kết quả tìm mã |
| Current price | Index name trong header/subtitle |
| Home | Chart Index – mã front month (41I1=VN30, 41I2=VN100, ed gần nhất) |
| Market | List Index, list BOND, tab Derivatives |
| Price table | Default Index futures; dropdown; error/empty |

---

## Data Source & Conventions

- **Nguồn:** Init job → `symbol_static.json`; API `/api/v2/market/symbol/latest`, `/api/v2/market/symbolInfo`.
- **Nhận diện Derivatives:** `t === "FUTURES"` hoặc `m in ["INDEX","BOND"]`.
- **Planning:** [Market/Planning/01_Integration_Plan.md](../Planning/01_Integration_Plan.md) — 4.1.2.1, 5.4.

---

## Home Chart – API & logic chọn mã front month (ngày đáo hạn gần nhất)

### API lấy danh sách symbol Derivatives

| API | Method | Mô tả |
|-----|--------|-------|
| **symbol_static.json** | - | File init job upload lên MinIO/S3; app tải đầu ngày. Chứa toàn bộ symbol (equity + derivatives). |
| **GET /api/v2/market/symbolInfo** | GET | `?symbolList=...` – lấy chi tiết symbol (có thể dùng khi cần query theo list cụ thể). |
| **GET /api/v2/market/symbol/latest** | GET | Body `{ "symbolList": ["VN30F2502", ...] }` – lấy giá mới nhất + metadata. |

**Khuyến nghị:** Dùng **symbol_static.json** đã load trong app (hoặc cache từ init). Không cần gọi API riêng nếu app đã có danh sách derivatives.

### Response field cần dùng – chọn mã front month

| Field | Type | Mô tả | VD |
|-------|------|-------|-----|
| `s` | string | Mã CK | `"VN30F2502"` |
| `m` | string | Market (INDEX \| BOND) | `"INDEX"` |
| `ed` | string | **Ngày đáo hạn** (yyyyMMdd) | `"20250227"` |
| `bc` | string | Mã cơ sở (VN30, VN100) | `"VN30"` |

**Lưu ý:** Nếu API chưa trả `ed`/`bc`, có thể suy từ mã: `41I1` = VN30 (bc), `41I2` = VN100; ngày đáo hạn có thể parse từ mã (VD: VN30F**2502** = tháng 02/2025). Xác nhận với BE format thực tế.

### Logic chọn mã cho chart (pseudo-code)

```txt
Input: Danh sách symbol từ symbol_static / symbolInfo (m === "INDEX")

Cho VN30 chart (41I1 / bc === "VN30"):
  1. Lọc: m === "INDEX" VÀ (s bắt đầu "41I1" hoặc bc === "VN30")
  2. Lọc: ed >= today (bỏ mã đã đáo hạn)
  3. Sắp xếp: theo ed tăng dần
  4. Chọn: phần tử đầu tiên (ed gần nhất)
  5. Dùng symbol đó (s) cho chart
  6. Fallback: Nếu tất cả mã đã đáo hạn (không có ed >= today) → chọn mã có ed lớn nhất (gần đáo hạn nhất trong quá khứ)

Cho VN100 chart (41I2 / bc === "VN100"):
  Tương tự, lọc 41I2 hoặc bc === "VN100"
```

### Ví dụ

| Mã (s) | m | ed | bc | Kết quả (today = 20250211) |
|--------|---|-----|-----|----------------------------|
| VN30F2501 | INDEX | 20250130 | VN30 | ❌ Đã đáo hạn |
| VN30F2502 | INDEX | 20250227 | VN30 | ✅ **Chọn** – ed gần nhất |
| VN30F2503 | INDEX | 20250327 | VN30 | Bỏ qua (xa hơn) |
| VN100F2502 | INDEX | 20250227 | VN100 | ✅ **Chọn** cho VN100 chart |

### Chart data & WebSocket

Sau khi có `symbolCode` (VD: VN30F2502):

| Nhu cầu | Nguồn |
|---------|-------|
| Giá real-time | WebSocket `market.quote.dr.VN30F2502` |
| Sổ lệnh | WebSocket `market.bidoffer.dr.VN30F2502` |
| Dữ liệu chart (candle) | API chart hiện có (TradingView / market-query-v2) – route theo symbol |

---

### Bảng giá ngang – Column mapping (market.quote / symbolInfo)

| API Field | Mô tả |
|-----------|-------|
| `s` | Mã CK |
| `c` | Giá hiện tại |
| `ch` | Thay đổi |
| `ra` | Tỷ lệ % |
| `vo` | Khối lượng |
| `va` | Giá trị |
| `tb`, `to` | Tổng KL mua/bán |

Nguồn: WebSocket `market.quote.dr.{code}` hoặc symbolInfo/quote API. Thứ tự cột theo Figma.

---

## References

| Loại | Link |
|------|------|
| **Figma – Current price** | [node-id=40004829-278373](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-278373) |
| **Figma – Search result row** | _(bổ sung node-id khi có)_ |
| **Figma – Home** | [node-id=40004829-276489](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-276489) |
| **Figma – Market** | [node-id=40004829-277238](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-277238) |
| **Figma – Price table** | [node-id=40005162-207007](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-207007) |
| **Figma – Price table options** | [node-id=40005162-208618](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-208618) |
| **Figma – Price table Error** | [node-id=40005162-206999](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-206999) |
| **Planning** | [Market/Planning/01_Integration_Plan.md](../Planning/01_Integration_Plan.md) |
