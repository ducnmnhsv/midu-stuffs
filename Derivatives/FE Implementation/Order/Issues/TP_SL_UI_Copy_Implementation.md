# FE Issue: Áp dụng TP/SL UI Copy & Validation Messages (Derivatives)

> **Module:** Order  
> **Screens:** Màn thiết lập TP/SL (Take Profit / Stop Loss)  
> **Priority:** P1  
> **Status:** 🔴 Blocked (chờ BE/Core order lifecycle)  
> **Created:** 2026-02-10

---

## 1. Background

Tính năng **TP/SL (Take Profit / Stop Loss)** cho Derivatives sẽ cho phép trader thiết lập giá hoặc offset để tự động đóng vị thế. Nội dung UI (tooltip, validation, warning, confirmation) đã được chuẩn hóa trong tài liệu **TP/SL UI Copy**. FE cần áp dụng đúng bản copy và quy tắc hiển thị (tooltip / warning / error) khi màn TP/SL được triển khai.

**Lưu ý:** Issue này **bị block** bởi quyết định nghiệp vụ và BE: cơ chế track order lifecycle (hủy/sửa lệnh ảnh hưởng TP/SL) đang thảo luận. Xem [TPSL_Tracking_Mechanism_Discussion](../../../Planning%20documentation/Order/Issues/TPSL_Tracking_Mechanism_Discussion.md). FE có thể chuẩn bị UI copy và validation trước, triển khai màn hình khi BE/Core sẵn sàng.

---

## 2. Data Source

- **UI Copy (nguồn chính):** [TP_SL_UI_Copy.md](../../../Planning%20documentation/Order/Specifications/TP_SL_UI_Copy.md)
- **Planning:** [04_TPSL_Orders_Business](../../../Planning%20documentation/Order/Planning/04_TPSL_Orders_Business.md)
- **Blocking issue:** [TPSL_Tracking_Mechanism_Discussion](../../../Planning%20documentation/Order/Issues/TPSL_Tracking_Mechanism_Discussion.md)

Nội dung trong TP_SL_UI_Copy gồm:

- **I. Tooltip** – Giải thích TP/SL, price-based, offset-based, market order, volatility (EN/VI).
- **II. Validation Error** – Chặn xác nhận: TP = Entry, Invalid TP/SL (Long/Short), Current price passed TP/SL, Offset = 0, Invalid offset sign, Tick size, Price out of range, Market halt.
- **III. Warning** – Cảnh báo không chặn: TP/SL near market price, High volatility, Avg entry change, Large offset, Low liquidity.
- **IV. Inline Hint** – Hướng dẫn nhập: TP/SL price (Long/Short), TP/SL offset, Offset-based.
- **V. Confirmation / Summary** – Trước khi xác nhận: Summary header, Execution note, Auto cancel, Final note.
- **VI. Pro-level Warning** – Price gap, Immediate trigger, Network delay.
- **VII. UI Usage** – Tooltip ⓘ; Warning màu vàng; Error màu đỏ; Pro warnings trong “Nâng cao” / “Tìm hiểu thêm”.

---

## 3. Screens & Components (FE reference)

| Vị trí | Mô tả |
|--------|--------|
| **TP/SL setup screen** | Màn thiết lập TP/SL (price-based hoặc offset-based). Các ô nhập, nút, và vùng summary cần dùng đúng copy EN/VI theo tài liệu. |
| **Tooltips** | Icon ⓘ cạnh label/block: hiển thị nội dung Tooltip (I) theo context (general, price-based, offset-based, …). |
| **Validation** | Khi user nhập sai (TP = entry, invalid price, offset = 0, …): hiển thị message Error (II) tương ứng, màu đỏ, chặn submit. |
| **Warnings** | Cảnh báo (III): hiển thị màu vàng, không chặn thao tác. |
| **Inline hint** | Gợi ý dưới ô nhập (IV) theo loại vị thế (Long/Short) và loại nhập (price / offset). |
| **Confirmation** | Trước khi xác nhận: Summary (V) – header, execution note, auto cancel, final note. |
| **Pro warnings** | Phần “Nâng cao” / “Tìm hiểu thêm”: nội dung (VI). |

**FE repo (read-only):**  
Màn TP/SL có thể nằm trong `src/screens/` (ví dụ TPSLScreen, PositionDetailScreen với tab TP/SL). Copy nên đưa vào file i18n (vi/en) hoặc constants, tham chiếu key từ TP_SL_UI_Copy.

---

## 4. Acceptance Criteria

- [ ] **AC1 – Tooltip**  
  Tất cả tooltip TP/SL dùng đúng nội dung (EN/VI) trong mục I của [TP_SL_UI_Copy](../../../Planning%20documentation/Order/Specifications/TP_SL_UI_Copy.md). Hiển thị bằng icon ⓘ.

- [ ] **AC2 – Validation Error**  
  Các trường hợp validation (II) hiển thị đúng message tương ứng và chặn xác nhận. Style: error (màu đỏ).

- [ ] **AC3 – Warning**  
  Các cảnh báo (III) hiển thị đúng message, style warning (màu vàng), không chặn thao tác.

- [ ] **AC4 – Inline Hint**  
  Gợi ý nhập liệu (IV) hiển thị đúng theo loại vị thế (Long/Short) và loại giá/offset.

- [ ] **AC5 – Confirmation / Summary**  
  Màn xác nhận trước khi gửi TP/SL hiển thị đúng nội dung (V): summary header, execution note, auto cancel, final note.

- [ ] **AC6 – Pro-level Warning**  
  Phần “Nâng cao” / “Tìm hiểu thêm” hiển thị nội dung (VI): price gap, immediate trigger, network delay.

- [ ] **AC7 – Ngôn ngữ**  
  Tất cả copy hỗ trợ VI và EN theo tài liệu; chuyển đổi theo cài đặt ngôn ngữ app.

---

## 5. Figma & References

- **UI Copy:** [TP_SL_UI_Copy.md](../../../Planning%20documentation/Order/Specifications/TP_SL_UI_Copy.md)
- **Business:** [04_TPSL_Orders_Business.md](../../../Planning%20documentation/Order/Planning/04_TPSL_Orders_Business.md)
- **Blocking:** [TPSL_Tracking_Mechanism_Discussion.md](../../../Planning%20documentation/Order/Issues/TPSL_Tracking_Mechanism_Discussion.md)

---

## 6. Notes for Dev

- Có thể bắt đầu **chuẩn bị** (thêm key i18n, component tooltip/warning/error) ngay; **triển khai đầy đủ màn TP/SL** chỉ sau khi BE/Core có quyết định về tracking mechanism và API TP/SL sẵn sàng.
- Khi unblock: cập nhật status issue thành 📋 Ready for FE và bổ sung AC liên quan tới gọi API (nếu có spec API TP/SL).
