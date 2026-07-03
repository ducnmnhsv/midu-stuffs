# TP/SL – UI Copy & Validation Messages (EN / VI)

**Document Type:** UI/UX Content  
**Category:** Orders - TP/SL Feature  
**Version:** 1.0  
**Date:** February 3, 2026

> **Note:** This is NOT a technical specification.  
> This document contains UI copy, tooltips, and validation messages for TP/SL feature.

---

## I. Tooltip – Giải thích (Informational)

| Context | English | Tiếng Việt |
|------|--------|-----------|
| TP/SL (General) | TP/SL helps automatically close your position to take profit or limit losses when the trigger condition is met. | TP/SL giúp tự động đóng vị thế để chốt lời hoặc giới hạn thua lỗ khi điều kiện được kích hoạt. |
| Price-based | TP/SL is triggered when the current price reaches the specified price level. | TP/SL được kích hoạt khi giá hiện tại chạm đến mức giá đã thiết lập. |
| Offset-based | TP/SL is triggered when the price moves by the specified number of points from the entry price. | TP/SL được kích hoạt khi giá di chuyển một số điểm nhất định so với giá vào lệnh. |
| Offset-based (Avg Price) | TP/SL prices are calculated based on the average entry price. | Giá TP/SL được tính dựa trên giá vào lệnh trung bình của vị thế. |
| Market order | TP/SL orders are executed as market orders when triggered. | Khi được kích hoạt, TP/SL sẽ được thực hiện bằng lệnh thị trường. |
| Volatility | During high volatility, the execution price may differ from the TP/SL price. | Trong điều kiện biến động mạnh, giá khớp lệnh có thể khác với giá TP/SL đã đặt. |

---

## II. Validation Error – Block Action (Không cho phép xác nhận)

| Scenario | English | Tiếng Việt |
|------|--------|-----------|
| TP = Entry | TP/SL must be different from the entry price. | Giá TP/SL phải khác với giá vào lệnh. |
| Invalid TP/SL (LONG) | Invalid TP/SL price for a long position. | Giá TP/SL không hợp lệ đối với vị thế Long. |
| Invalid TP/SL (SHORT) | Invalid TP/SL price for a short position. | Giá TP/SL không hợp lệ đối với vị thế Short. |
| Current price passed TP/SL | Current price has already passed the TP/SL level. | Giá hiện tại đã vượt qua mức TP/SL đã thiết lập. |
| Offset = 0 | Offset value must be greater than zero. | Khoảng điểm TP/SL phải lớn hơn 0. |
| Invalid offset sign | Invalid offset value for the selected position. | Khoảng điểm TP/SL không phù hợp với loại vị thế. |
| Invalid tick size | Price does not match the minimum tick size. | Giá không phù hợp với bước giá tối thiểu. |
| Price out of range | Price is outside the allowed trading range. | Giá nằm ngoài biên độ giao dịch cho phép. |
| Market halt | TP/SL cannot be set while the market is halted. | Không thể thiết lập TP/SL khi thị trường đang tạm ngừng giao dịch. |

---

## III. Warning – Cảnh báo (Không chặn thao tác)

| Scenario | English | Tiếng Việt |
|------|--------|-----------|
| TP/SL near market price | TP/SL is very close to the current price and may be triggered immediately. | TP/SL đang rất gần giá thị trường và có thể được kích hoạt ngay lập tức. |
| High volatility | Market volatility is high. TP/SL execution price may differ from the trigger price. | Thị trường đang biến động mạnh. Giá khớp TP/SL có thể khác với giá kích hoạt. |
| Avg entry change | TP/SL prices will be recalculated if your average entry price changes. | Giá TP/SL sẽ được tính lại nếu giá vào lệnh trung bình thay đổi. |
| Large offset | The selected offset may place TP/SL far from the current price. | Khoảng điểm đã chọn có thể khiến TP/SL cách xa giá hiện tại. |
| Low liquidity | Low liquidity may cause slippage when TP/SL is triggered. | Thanh khoản thấp có thể gây trượt giá khi TP/SL được kích hoạt. |

---

## IV. Inline Hint – Hướng dẫn nhập liệu (Microcopy)

| Input field | English | Tiếng Việt |
|------|--------|-----------|
| TP price (LONG) | Must be higher than the entry price. | Giá TP phải cao hơn giá vào lệnh. |
| SL price (LONG) | Must be lower than the entry price. | Giá SL phải thấp hơn giá vào lệnh. |
| TP price (SHORT) | Must be lower than the entry price. | Giá TP phải thấp hơn giá vào lệnh. |
| SL price (SHORT) | Must be higher than the entry price. | Giá SL phải cao hơn giá vào lệnh. |
| TP offset | Enter a positive value for profit. | Nhập giá trị dương để chốt lời. |
| SL offset | Enter a positive value for loss protection. | Nhập giá trị dương để cắt lỗ. |
| Offset-based | TP/SL price will be calculated automatically. | Giá TP/SL sẽ được hệ thống tự động tính toán. |

---

## V. Confirmation / Summary (Trước khi xác nhận)

| Context | English | Tiếng Việt |
|------|--------|-----------|
| Summary header | Review your TP/SL settings | Kiểm tra lại thiết lập TP/SL |
| Execution note | TP/SL will close the entire position when triggered. | TP/SL sẽ đóng toàn bộ vị thế khi được kích hoạt. |
| Auto cancel | TP/SL orders will be canceled after the position is closed. | Lệnh TP/SL sẽ tự động huỷ sau khi vị thế được đóng. |
| Final note | Orders are executed at market price when triggered. | Lệnh được khớp theo giá thị trường khi được kích hoạt. |

---

## VI. Pro-level Warning (Advanced)

| Scenario | English | Tiếng Việt |
|------|--------|-----------|
| Price gap | Price gaps may cause TP/SL to be executed beyond the trigger level. | Giá nhảy gap có thể khiến TP/SL được khớp ngoài mức đã đặt. |
| Immediate trigger | TP/SL may be triggered immediately after submission due to rapid price movement. | TP/SL có thể được kích hoạt ngay sau khi đặt do giá biến động nhanh. |
| Network delay | Displayed prices may be delayed. Final validation is based on the latest market price. | Giá hiển thị có thể bị trễ. Việc xác nhận cuối cùng dựa trên giá thị trường mới nhất. |

---

## VII. UI Usage Notes (For Design & UX)

- Tooltip: ⓘ icon – dùng để giải thích
- Warning: màu vàng – không chặn thao tác
- Error: màu đỏ – chặn xác nhận
- Pro warnings: hiển thị trong phần “Nâng cao” hoặc “Tìm hiểu thêm”
---

Document Status: 📋 | For: PM/Dev | Next Steps: Review nội dung, cập nhật status trên Tracking/tasks.js
