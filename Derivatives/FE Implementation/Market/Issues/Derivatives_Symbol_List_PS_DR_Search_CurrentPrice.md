# [Epic DR-FE-MKT] Story MKT.S1: Hiển thị danh sách mã Derivatives với Index name PS/DR (Search & Current price)

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-MKT – Derivatives Market FE  
> **Module:** Market  
> **Screens:** Search (tìm mã), Current price (giá hiện tại)  
> **Priority:** P0  
> **Status:** 📋 Ready for FE  
> **Created:** 2026-02-10

---

## User Story

**As a** trader / user  
**I want to** thấy index name đúng (PS/DR cho Index futures, TPCP/GB cho Gov Bond) khi tìm mã và xem giá hiện tại mã Derivatives  
**So that** tôi phân biệt rõ mã thuộc thị trường phái sinh chỉ số hay trái phiếu chính phủ.

---

## Acceptance Criteria

- [ ] **AC-01 – Nhận diện Derivatives**  
  Mã được coi là Derivatives nếu `t === "FUTURES"` **hoặc** `m in ["INDEX","BOND"]` trong dữ liệu symbol (symbol_static / symbolInfo / latest). *(Cùng field `m`; cơ sở = HOSE/HNX/UPCOM, phái sinh = INDEX/BOND.)*

- [ ] **AC-02 – Index name theo `m` và ngôn ngữ**  
  Dùng field **`m`** (market) từ BE — cùng field đang dùng cho HOSE/HNX/UPCOM:
  - `m === "INDEX"`: VI → **PS**, EN → **DR**.
  - `m === "BOND"`: VI → **TPCP**, EN → **GB** (hoặc theo quy ước PM đã xác nhận).
  - `m` null/khác (cho Futures): fallback **PS**/**DR** (hoặc theo quy ước).

- [ ] **AC-03 – Màn Search**  
  Khi user tìm mã và kết quả có mã Derivatives: trong danh sách kết quả, cột/vị trí “sàn”/index name hiển thị theo **`m` + ngôn ngữ** (PS/DR cho INDEX, TPCP/GB cho BOND).

- [ ] **AC-04 – Màn Current price**  
  Khi user mở một mã Derivatives: phần header/subtitle hiển thị index name theo **`m` + ngôn ngữ**.

- [ ] **AC-05 – Không ảnh hưởng Equity**  
  Mã cơ sở (HOSE, HNX, UPCOM) vẫn hiển thị đúng như hiện tại (không đổi logic `m` cho equity).

---

## Tasks (Implementation)

- [ ] **T1** Thêm/cập nhật helper map `m` (INDEX/BOND) → index name (PS/DR, TPCP/GB) theo ngôn ngữ (e.g. `getSymbolIndexName(symbol, lang)`).
- [ ] **T2** Cập nhật Search: `SearchScreen` / `SearchSymbolScreen` – danh sách kết quả hiển thị index name theo `m` + lang cho mã Derivatives.
- [ ] **T3** Cập nhật Current price: `CurrentPriceScreen` – header/subtitle hiển thị index name theo `m` + lang cho mã Derivatives.
- [ ] **T4** Đảm bảo nhận diện Derivatives nhất quán (`t === "FUTURES"` hoặc `m in ["INDEX","BOND"]`) tại mọi nơi dùng (Search, Current price, watchlist, dropdown).

---

## Background / Context

Dữ liệu đầu ngày (init job) trả ra danh sách mã trong `symbol_static.json` (và các API symbol/latest, symbolInfo). Có **hai loại** hợp đồng phái sinh, BE dùng **cùng field `m`** (market): **`m`** = **"INDEX"** hoặc **"BOND"** (FE đã dùng `m` cho HOSE/HNX/UPCOM).

| Loại hợp đồng | Quy tắc mã | `m` | Index name (VI) | Index name (EN) |
|---------------|------------|-----|-----------------|-----------------|
| HĐ tương lai chỉ số phái sinh | 41**I**xxxxxx | `"INDEX"` | **PS** | **DR** |
| HĐTL trái phiếu chính phủ | 41**B**xxxxxx | `"BOND"` | **TPCP** | **GB** |

Hai vị trí áp dụng: **Màn Search** (kết quả tìm mã), **Màn Current price** (chi tiết mã).

---

## Data Source

- **Nguồn:** Init job → `symbol_static.json`; API `/api/v2/market/symbol/latest`, `/api/v2/market/symbolInfo`.
- **Planning:** [Market/Planning/01_Integration_Plan.md](../../../Planning%20documentation/Market/Planning/01_Integration_Plan.md) – 4.1.2.1, format `symbol_static.json`, field `t`, **`m`**.
- **Nhận diện Derivatives:** `t === "FUTURES"` **hoặc** `m in ["INDEX","BOND"]`.

Ví dụ item Derivatives:

```json
{
  "s": "VN30F2502",
  "m": "INDEX",
  "n1": "HĐ Tương lai VN30 Tháng 02/2025",
  "n2": "VN30 Index Futures Feb 2025",
  "t": "FUTURES"
}
```

(HĐTL trái phiếu: `"s": "41Bxxxxxx"`, `"m": "BOND"`.)

---

## Screens & Components (FE reference)

| Vị trí | Màn hình | Component / Logic |
|--------|----------|-------------------|
| Search | `SearchScreen` | Kết quả tìm mã – với Derivatives map `m` (INDEX \| BOND) sang PS/DR, TPCP/GB theo ngôn ngữ. |
| Search | `SearchSymbolScreen` | Tương tự: danh sách mã dùng **`m`** để hiển thị index name. |
| Current price | `CurrentPriceScreen` | Header/subtitle: với mã Derivatives dùng **`m`** để hiển thị PS/DR hoặc TPCP/GB theo ngôn ngữ. |

**FE repo (read-only):**  
Search: `src/screens/SearchScreen/`, `src/screens/SearchScreen/components/ItemSymbol/index.tsx` (hiện dùng `dataSymbol?.m`).  
Current price: `src/screens/CurrentPriceScreen/index.tsx` (dùng `currentSymbol`: `s`, `m`, `n1`, `n2`).

---

## Technical Notes

- Helper gợi ý: `getSymbolIndexName(symbol, lang)` — nếu Equity (`m` in ["HOSE","HNX","UPCOM"]) → trả về `symbol.m`; nếu Derivatives dùng **`symbol.m`** → INDEX ? (lang === VI ? "PS" : "DR") : (lang === VI ? "TPCP" : "GB"); fallback khi `m` null: "PS"/"DR".
- Áp dụng cùng logic index name ở mọi nơi hiển thị “market”/index: Search, Current price, watchlist, dropdown.

---

## References

- **Figma – Current price:** [node 40004829-278373](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-278373)
- **Planning:** [Market/Planning/01_Integration_Plan.md](../../../Planning%20documentation/Market/Planning/01_Integration_Plan.md)
