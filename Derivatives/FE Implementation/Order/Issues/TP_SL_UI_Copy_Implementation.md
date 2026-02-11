# [Epic DR-FE-ORD] Story ORD.S3: Áp dụng TP/SL UI Copy & Validation Messages (Derivatives)

> **Jira:** _(điền key khi tạo, e.g. NHMTS-xxx)_  
> **Epic:** DR-FE-ORD – Derivatives Order FE  
> **Module:** Order  
> **Screens:** Màn thiết lập TP/SL (Take Profit / Stop Loss)  
> **Priority:** P1  
> **Status:** 🔴 Blocked (chờ BE/Core order lifecycle)  
> **Created:** 2026-02-10

---

## User Story

**As a** trader  
**I want to** thấy đúng nội dung tooltip, validation, warning và confirmation theo bản copy chuẩn (EN/VI) khi thiết lập TP/SL cho Derivatives  
**So that** tôi hiểu rõ quy tắc và rủi ro trước khi xác nhận.

---

## Acceptance Criteria

- [ ] **AC-01** Tất cả tooltip TP/SL dùng đúng nội dung (EN/VI) trong mục I của [TP_SL_UI_Copy](../../../Planning%20documentation/Order/Specifications/TP_SL_UI_Copy.md). Hiển thị bằng icon ⓘ.
- [ ] **AC-02** Các trường hợp validation (II) hiển thị đúng message tương ứng và chặn xác nhận. Style: error (màu đỏ).
- [ ] **AC-03** Các cảnh báo (III) hiển thị đúng message, style warning (màu vàng), không chặn thao tác.
- [ ] **AC-04** Gợi ý nhập liệu (IV) hiển thị đúng theo loại vị thế (Long/Short) và loại giá/offset.
- [ ] **AC-05** Màn xác nhận trước khi gửi TP/SL hiển thị đúng nội dung (V): summary header, execution note, auto cancel, final note.
- [ ] **AC-06** Phần “Nâng cao” / “Tìm hiểu thêm” hiển thị nội dung (VI): price gap, immediate trigger, network delay.
- [ ] **AC-07** Tất cả copy hỗ trợ VI và EN; chuyển đổi theo cài đặt ngôn ngữ app.

---

## Tasks (Implementation)

- [ ] **T1** Đưa copy từ TP_SL_UI_Copy vào i18n (vi/en) hoặc constants; mapping key theo mục I–VII.
- [ ] **T2** Tooltips: icon ⓘ cạnh label/block; nội dung (I) theo context (general, price-based, offset-based).
- [ ] **T3** Validation errors (II): message đúng, style đỏ, chặn submit.
- [ ] **T4** Warnings (III): message đúng, style vàng, không chặn.
- [ ] **T5** Inline hints (IV) theo vị thế và loại nhập; Confirmation (V) và Pro warnings (VI) khi màn TP/SL triển khai đầy đủ.

---

## Background / Context

Tính năng **TP/SL** cho Derivatives cho phép thiết lập giá hoặc offset để tự động đóng vị thế. Nội dung UI đã chuẩn hóa trong **TP/SL UI Copy**. FE áp dụng đúng copy và quy tắc hiển thị khi màn TP/SL được triển khai.

**Blocked:** Cơ chế track order lifecycle (hủy/sửa lệnh ảnh hưởng TP/SL) đang thảo luận. Xem [TPSL_Tracking_Mechanism_Discussion](../../../Planning%20documentation/Order/Issues/TPSL_Tracking_Mechanism_Discussion.md). FE có thể chuẩn bị UI copy và validation trước; triển khai màn hình khi BE/Core sẵn sàng.

---

## Data Source

- **UI Copy:** [TP_SL_UI_Copy.md](../../../Planning%20documentation/Order/Specifications/TP_SL_UI_Copy.md)
- **Planning:** [04_TPSL_Orders_Business](../../../Planning%20documentation/Order/Planning/04_TPSL_Orders_Business.md)
- **Blocking:** [TPSL_Tracking_Mechanism_Discussion](../../../Planning%20documentation/Order/Issues/TPSL_Tracking_Mechanism_Discussion.md)

**Nội dung TP_SL_UI_Copy:**  
I. Tooltip – II. Validation Error – III. Warning – IV. Inline Hint – V. Confirmation/Summary – VI. Pro-level Warning – VII. UI Usage (ⓘ, màu vàng/đỏ).

---

## Screens & Components (FE reference)

| Vị trí | Mô tả |
|--------|--------|
| TP/SL setup screen | Ô nhập, nút, summary – copy EN/VI theo tài liệu. |
| Tooltips | Icon ⓘ, nội dung (I). |
| Validation / Warnings | Error (II) đỏ, Warning (III) vàng. |
| Inline hint (IV), Confirmation (V), Pro warnings (VI). |

**FE repo (read-only):** Màn TP/SL: `src/screens/` (TPSLScreen, PositionDetailScreen…). Copy → i18n hoặc constants.

---

## Technical Notes

- Có thể **chuẩn bị** ngay (i18n keys, component tooltip/warning/error); **triển khai đầy đủ màn TP/SL** sau khi BE/Core có quyết định và API TP/SL sẵn sàng.
- Khi unblock: cập nhật status thành 📋 Ready for FE và bổ sung AC liên quan API (nếu có spec).

---

## References

- [TP_SL_UI_Copy.md](../../../Planning%20documentation/Order/Specifications/TP_SL_UI_Copy.md)
- [04_TPSL_Orders_Business.md](../../../Planning%20documentation/Order/Planning/04_TPSL_Orders_Business.md)
- [TPSL_Tracking_Mechanism_Discussion.md](../../../Planning%20documentation/Order/Issues/TPSL_Tracking_Mechanism_Discussion.md)
