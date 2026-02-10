# Bổ sung: Logic phân biệt Index và Bond (Market Data)

> **Mục đích:** Tài liệu bổ sung cho Confluence / wiki Market Data.  
> **Nội dung:** Logic phân biệt hai loại phái sinh (Chỉ số vs Trái phiếu CP) và nơi áp dụng.  
> **Nguồn:** [01_Integration_Plan.md](../Planning/01_Integration_Plan.md) (mục 4.1.2.1, 4.2, 5.x).

---

## 1. Tại sao cần phân biệt Index và Bond?

Hệ thống TradeX hỗ trợ **hai loại** hợp đồng phái sinh từ Lotte:

| Loại | Mô tả | Ví dụ mã |
|------|--------|----------|
| **HĐ tương lai chỉ số phái sinh** | Chỉ số (VN30, v.v.) | VN30F2501, VN30F2502 |
| **HĐTL trái phiếu chính phủ** | Trái phiếu chính phủ | 41Bxxxxxx |

Ứng dụng (NHSV Pro) cần hiển thị **tên chỉ mục** khác nhau cho từng loại (ví dụ: **PS/DR** cho chỉ số, **TPCP/GB** cho trái phiếu CP). Để làm được điều đó, backend phải cung cấp một **field phân loại** cho mỗi mã phái sinh.

---

## 2. Quy tắc xác định: 41I vs 41B

Phân loại được **suy ra từ mã hợp đồng** (symbol code), cụ thể là **ký tự thứ 3**:

| Loại hợp đồng | Quy tắc mã | Ký tự thứ 3 | Giá trị field `dc` |
|---------------|------------|-------------|---------------------|
| HĐ tương lai **chỉ số** phái sinh | `41Ixxxxxx` | **I** (Index) | `"INDEX"` |
| HĐTL **trái phiếu chính phủ** | `41Bxxxxxx` | **B** (Bond) | `"BOND"` |

**Quy tắc:**
- Ký tự thứ 3 = **I** → `dc = "INDEX"`.
- Ký tự thứ 3 = **B** → `dc = "BOND"`.
- Trường hợp khác (nếu có sau này) → `dc = null` hoặc `"OTHER"` (mở rộng sau).

**Ví dụ:**

| Mã (`s`) | Ký tự thứ 3 | `dc` |
|----------|-------------|------|
| VN30F2501, 41Ixxxxxx | I | `"INDEX"` |
| 41Bxxxxxx | B | `"BOND"` |

---

## 3. Field trả về cho Frontend: `dc` (derivative category)

- **Tên field (JSON):** `dc`
- **Ý nghĩa:** Derivative category – loại phái sinh (INDEX | BOND).
- **Cách dùng ở FE:** Dựa vào `dc` để hiển thị đúng **index name**:
  - `dc === "INDEX"` → Hiển thị nhãn kiểu **PS** (VI) / **DR** (EN) (Chỉ số).
  - `dc === "BOND"` → Hiển thị nhãn kiểu **TPCP** (VI) / **GB** (EN) (Trái phiếu CP).

*(Tên hiển thị cụ thể do FE quy ước; backend chỉ cung cấp `dc`.)*

---

## 4. Nơi áp dụng logic Index/Bond

Logic **phải** được áp dụng tại **mọi nơi** trả về thông tin symbol phái sinh để FE luôn nhận được `dc`:

| Nơi | Mô tả |
|-----|--------|
| **Init Job** | Khi merge danh sách mã phái sinh vào `symbol_static.json`, mỗi mã phải có `dc` suy từ 41I/41B. |
| **SymbolInfo API** | `/api/v2/market/symbolInfo` (và các API aggregate từ SymbolInfo) trả về mỗi symbol phái sinh kèm `dc`. |
| **symbol_static.json** | File tĩnh tải lúc app start: mỗi item phái sinh có field `dc` (INDEX \| BOND). |
| **symbol/latest** | API lấy danh sách symbol mới nhất: response có `dc` cho mọi mã phái sinh. |
| **WebSocket / Real-time** | Khi aggregate message DR vào Redis/API, message/phản hồi có `dc` để client hiển thị đúng. |

**Tóm tắt:** Bất kỳ response hoặc payload nào chứa **symbol phái sinh** đều phải có **`dc`** = `"INDEX"` hoặc `"BOND"` (suy từ ký tự thứ 3 của mã).

---

## 5. Ví dụ format có `dc`

**symbol_static.json / SymbolInfo (phái sinh chỉ số):**
```json
{
  "s": "VN30F2501",
  "m": "derivatives",
  "dc": "INDEX",
  "n1": "HĐ Tương lai VN30 Tháng 01/2025",
  "n2": "VN30 Index Futures Jan 2025",
  "t": "FUTURES",
  "re": 1273.0,
  "ce": 1350.0,
  "fl": 1220.0,
  "bc": "VN30",
  "ed": "20250130",
  "rd": 15
}
```

**Phái sinh trái phiếu CP:**
```json
{
  "s": "41Bxxxxxx",
  "m": "derivatives",
  "dc": "BOND",
  "n1": "HĐTL Trái phiếu Chính phủ ...",
  "n2": "Gov Bond Futures ...",
  "t": "FUTURES",
  "re": 100.5,
  "ce": 102.0,
  "fl": 99.0
}
```

---

## 6. Tóm tắt cho Confluence

- **Hai loại phái sinh:** Chỉ số (41I) và Trái phiếu CP (41B), phân biệt bằng **ký tự thứ 3** của mã.
- **Field chuẩn:** `dc` = `"INDEX"` | `"BOND"` trong mọi nơi trả symbol phái sinh (Init job, SymbolInfo API, symbol_static, symbol/latest, real-time).
- **Mục đích:** FE dùng `dc` để hiển thị đúng tên chỉ mục (PS/DR cho INDEX, TPCP/GB cho BOND).

---

*Tài liệu tham chiếu đầy đủ: [01_Integration_Plan.md](../Planning/01_Integration_Plan.md) – mục 4.1.2.1, 4.2 (FR-API-004), 5.1–5.4.*
