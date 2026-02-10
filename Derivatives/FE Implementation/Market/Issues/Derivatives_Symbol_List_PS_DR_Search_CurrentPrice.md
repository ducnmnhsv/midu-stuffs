# FE Issue: Hiển thị danh sách mã Derivatives với Index name PS/DR (Search & Current price)

> **Module:** Market  
> **Screens:** Search (tìm mã), Current price (giá hiện tại)  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-10

---

## 1. Background

Dữ liệu đầu ngày (init job) trả ra danh sách mã trong `symbol_static.json` (và các API symbol/latest, symbolInfo). Có **hai loại** hợp đồng phái sinh, BE phân biệt bằng field **`dc`** (derivative category):

| Loại hợp đồng | Quy tắc mã | `dc` | Index name (VI) | Index name (EN) |
|---------------|------------|------|-----------------|-----------------|
| HĐ tương lai chỉ số phái sinh | 41**I**xxxxxx | `"INDEX"` | **PS** (Phái sinh) | **DR** (Derivatives) |
| HĐTL trái phiếu chính phủ | 41**B**xxxxxx | `"BOND"` | **TPCP** (Trái phiếu CP) | **GB** (Gov Bond) *(PM có thể xác nhận lại label)* |

Trên app, mỗi mã Derivatives cần hiển thị **index name** theo `dc` và ngôn ngữ (VI/EN). Hai vị trí cần áp dụng:

1. **Màn Search** – khi user tìm mã, kết quả có mã Derivatives thì hiển thị index name tương ứng (PS/DR cho INDEX, TPCP/GB cho BOND).
2. **Màn Current price** – khi xem chi tiết mã Derivatives, phần “sàn”/index name hiển thị theo `dc` + ngôn ngữ.

---

## 2. Data Source

- **Nguồn:** Dữ liệu đầu ngày từ BE (init job) → `symbol_static.json` và/hoặc API `/api/v2/market/symbol/latest`, `/api/v2/market/symbolInfo`.
- **Planning doc:** [Market/Planning/01_Integration_Plan.md](../../../Planning%20documentation/Market/Planning/01_Integration_Plan.md) – Init Job, **4.1.2.1 Phân loại Derivatives (41I/41B)**, format `symbol_static.json`, field `t`, `m`, **`dc`**.
- **Nhận diện Derivatives:** `t === "FUTURES"` **hoặc** `m === "derivatives"`.
- **Phân loại cho index name:** dùng field **`dc`** (derivative category): `"INDEX"` | `"BOND"`. BE suy từ mã (ký tự thứ 3: I → INDEX, B → BOND).

Ví dụ item Derivatives trong danh sách mã:

```json
{
  "s": "VN30F2502",
  "m": "derivatives",
  "dc": "INDEX",
  "n1": "HĐ Tương lai VN30 Tháng 02/2025",
  "n2": "VN30 Index Futures Feb 2025",
  "t": "FUTURES",
  "re": 1273.0,
  "ce": 1350.0,
  "fl": 1220.0,
  "bc": "VN30",
  "ed": "20250227",
  "rd": 28
}
```

(Với HĐTL trái phiếu chính phủ: `"s": "41Bxxxxxx"`, `"dc": "BOND"`.)

---

## 3. Screens & Components (FE reference)

| Vị trí | Màn hình | Component / Logic |
|--------|----------|-------------------|
| **Search** | `SearchScreen` | Kết quả tìm mã (symbol list). Hiện đang hiển thị `dataSymbol.m` (market) cạnh mã – cần map `m === "derivatives"` (hoặc `t === "FUTURES"`) → hiển thị **PS** (VI) hoặc **DR** (EN). |
| **Search** | `SearchSymbolScreen` | Tương tự: danh sách mã tìm được cần dùng index name PS/DR cho mã Derivatives. |
| **Current price** | `CurrentPriceScreen` | Header/subtitle hiển thị symbol và “sàn”/index. Với mã Derivatives cần hiển thị **PS** (VI) hoặc **DR** (EN) thay cho raw `"derivatives"`. |

**FE repo (read-only):**

- Search: `src/screens/SearchScreen/`, `src/screens/SearchScreen/components/ItemSymbol/index.tsx` (hiện dùng `dataSymbol?.m`).
- Current price: `src/screens/CurrentPriceScreen/index.tsx` (dùng `currentSymbol`: `s`, `m`, `n1`, `n2`).

---

## 4. Acceptance Criteria

- [ ] **AC1 – Nhận diện Derivatives**  
  Mã được coi là Derivatives nếu `t === "FUTURES"` **hoặc** `m === "derivatives"` trong dữ liệu symbol (symbol_static / symbolInfo / latest).

- [ ] **AC2 – Index name theo `dc` và ngôn ngữ**  
  Dùng field **`dc`** từ BE để chọn index name:
  - `dc === "INDEX"`: VI → **PS**, EN → **DR**.
  - `dc === "BOND"`: VI → **TPCP**, EN → **GB** (hoặc theo quy ước PM đã xác nhận).
  - `dc` null/khác: fallback **PS**/ **DR** (hoặc theo quy ước).

- [ ] **AC3 – Màn Search**  
  Khi user tìm mã và kết quả có mã Derivatives:  
  - Trong danh sách kết quả, cột/vị trí “sàn”/index name hiển thị theo **`dc` + ngôn ngữ** (PS/DR cho INDEX, TPCP/GB cho BOND), không hiển thị raw `"derivatives"`.

- [ ] **AC4 – Màn Current price**  
  Khi user mở một mã Derivatives:  
  - Phần header/subtitle hiển thị index name theo **`dc` + ngôn ngữ**, không hiển thị raw `"derivatives"`.

- [ ] **AC5 – Không ảnh hưởng Equity**  
  Mã cơ sở (HOSE, HNX, UPCOM) vẫn hiển thị đúng như hiện tại (không đổi logic cho `m` khác `"derivatives"`).

---

## 5. Figma & References

- **Current price (UI tham chiếu):**  
  [Figma – NHSV Pro, Current price (node 40004829-278373)](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-278373)

- **Planning (data source):**  
  [Market/Planning/01_Integration_Plan.md](../../../Planning%20documentation/Market/Planning/01_Integration_Plan.md) – Init job, symbol_static.json, format Derivatives.

---

## 6. Notes for Dev

- Helper gợi ý: `getSymbolIndexName(symbol, lang)`:
  - Nếu không phải Derivatives (`m !== "derivatives"`) → trả về `symbol.m` (HOSE/HNX/UPCOM) như hiện tại.
  - Nếu Derivatives: dùng **`symbol.dc`** → `dc === "INDEX"` ? (lang === VI ? "PS" : "DR") : (lang === VI ? "TPCP" : "GB"); fallback khi `dc` null: "PS"/"DR".
- BE sẽ trả field **`dc`** (INDEX | BOND) trong symbol_static / symbolInfo / latest sau khi init job cập nhật theo [Planning 4.1.2.1](../../../Planning%20documentation/Market/Planning/01_Integration_Plan.md). FE cần đọc `dc` thay vì chỉ dựa vào `m === "derivatives"` để phân biệt hai loại.
- Áp dụng cùng logic index name (theo `dc` + lang) ở mọi nơi hiển thị “market”/index: Search, Current price, watchlist, dropdown, v.v.
