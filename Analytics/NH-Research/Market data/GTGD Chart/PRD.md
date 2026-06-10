# PRD — Màn hình Giá trị Giao dịch (GTGD Chart)

> **Loại tài liệu:** Product Requirements Document  
> **Ngày tạo:** 2026-06-08  
> **Phạm vi:** Màn hình mới — Market GTGD Comparison  

---

## 1. Vấn đề

Trader hiện tại không có cách nào theo dõi **sức mua/bán của thị trường theo thời gian thực** trong phiên giao dịch. Họ chỉ thấy một con số GTGD tổng — không biết thanh khoản đang tăng hay giảm so với hôm qua, và tại thời điểm nào trong ngày thị trường sôi động nhất.

---

## 2. Mục tiêu

Cung cấp một màn hình chuyên biệt để trader:
- Xem GTGD hôm nay theo từng phút, cập nhật liên tục
- So sánh trực quan với GTGD phiên trước tại cùng thời điểm
- Chủ động chọn sàn muốn xem: HOSE, HNX hoặc UPCOM

---

## 3. Người dùng mục tiêu

Trader quan tâm đến thanh khoản thị trường — thường là trader ngắn hạn, người giao dịch khối lớn, hoặc trader theo dõi dòng tiền.

---

## 4. Mô tả tính năng

### 4.1 Màn hình mới: Giá trị Giao dịch

Một màn hình riêng, truy cập từ khu vực Market của app.

**Dropdown chọn sàn** ở đầu màn hình:
- VN-Index (HOSE)
- HNX
- UPCOM

Mặc định: VN-Index (HOSE).

---

### 4.2 Chart so sánh 2 phiên

Biểu đồ diện tích hiển thị từ 09:30 đến 15:00 với 2 đường:

| Đường | Màu | Nội dung |
|---|---|---|
| GTGD hôm nay | Cam | Tự động cập nhật khi có lệnh khớp mới |
| GTGD phiên trước | Xám | Cố định — để so sánh |

Trục dọc: Giá trị (tỷ VND), tự scale theo dữ liệu.  
Trục ngang: Thời gian trong phiên (09:30 → 15:00).

---

### 4.3 Tóm tắt số liệu

Phía trên chart, hiển thị 3 chỉ số:

| Chỉ số | Mô tả |
|---|---|
| Hôm nay | GTGD tích lũy tính đến thời điểm hiện tại |
| Phiên trước | Tổng GTGD phiên trước tại cùng mốc thời gian |
| So sánh (%) | Chênh lệch giữa 2 phiên — xanh nếu hôm nay cao hơn, đỏ nếu thấp hơn |

---

### 4.4 Tooltip khi chạm vào chart

Khi người dùng chạm và kéo ngón tay trên chart:
- Xuất hiện đường dọc di chuyển theo ngón tay
- Tooltip hiện: thời gian đang chỉ + GTGD hôm nay + GTGD phiên trước + % chênh lệch tại mốc đó
- 3 chỉ số tóm tắt phía trên cũng cập nhật theo thời điểm đang hover
- Nhả ngón tay → trở về hiển thị giá trị real-time

---

### 4.5 Trạng thái ngoài giờ giao dịch

- Trong giờ giao dịch: hiện badge **"Live"** + cập nhật tự động
- Ngoài giờ: chart vẫn hiển thị dữ liệu của phiên gần nhất, không có badge Live

---

## 5. Hành vi khi đổi sàn

Khi người dùng chọn sàn khác từ dropdown:
- Chart và số liệu reset về 0
- Tải lại dữ liệu cho sàn mới
- Trạng thái Live/không Live cập nhật theo giờ giao dịch của sàn đó

---

## 6. Điều kiện hoàn thành (Definition of Done)

- [ ] Màn hình hiển thị đúng chart cho cả 3 sàn: HOSE, HNX, UPCOM
- [ ] Đường GTGD hôm nay tự cập nhật trong giờ giao dịch — không cần refresh tay
- [ ] Đường GTGD phiên trước đầy đủ từ mở cửa đến đóng cửa
- [ ] Chạm và kéo trên chart → tooltip hiện đúng thông tin tại từng mốc
- [ ] 3 chỉ số tóm tắt cập nhật khi hover và khi có dữ liệu mới
- [ ] Dropdown chuyển sàn hoạt động đúng
- [ ] Không có dữ liệu → hiển thị trạng thái rõ ràng, không crash

---

## 7. Ngoài phạm vi (Out of scope)

- So sánh nhiều hơn 2 phiên (ví dụ: 5 phiên gần nhất)
- Dữ liệu từng cổ phiếu riêng lẻ (chỉ áp dụng cho toàn sàn)
- Export dữ liệu

---

**Document Status:** Ready for dev  
**For:** PM, FE Team, BE Team  
**Next Steps:** BE Issue → FE Issue → Development → QA
