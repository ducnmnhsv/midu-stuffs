# Lệnh TP/SL (Chốt Lời / Cắt Lỗ)

**Loại tài liệu:** Yêu cầu nghiệp vụ  
**Phiên bản:** 2.0  
**Ngày:** 5 tháng 2, 2026

> **📘 Tài liệu kỹ thuật:** `../Specifications/TPSL_Orders_API_Spec.md`

---

## 1. Khái Niệm

**TP/SL (Take Profit / Stop Loss)** là lệnh đóng vị thế tự động nhằm bảo toàn lợi nhuận hoặc giới hạn rủi ro.

- **TP (Take Profit - Chốt lời):** Tự động đóng vị thế khi giá đạt mức lợi nhuận mong muốn
- **SL (Stop Loss - Cắt lỗ):** Tự động đóng vị thế khi giá giảm đến mức chấp nhận được

---

## 2. Mục Đích

✅ Chốt lời/cắt lỗ tự động trên vị thế mở có sẵn  
✅ Không cần theo dõi màn hình liên tục  
✅ Kỷ luật giao dịch, không để cảm xúc chi phối  
✅ Quản lý rủi ro hiệu quả

---

## 3. Các Bước Đặt Lệnh

### Bước 1: Mở màn hình Vị thế

Vào màn hình **Danh mục Vị thế mở** → Chọn vị thế cần đặt TP/SL

### Bước 2: Chọn phương thức đặt lệnh

TradeX hỗ trợ **2 phương thức** đặt lệnh TP/SL:

#### Phương Thức 1: Price-Based (Theo Giá)

**Khái niệm:** Đặt giá kích hoạt cụ thể

**Ví dụ:**
```
Vị thế: MUA 10 VN30F2602 @ 1250.0

Đặt lệnh:
  - TP @ 1280.0 (kích hoạt khi giá đạt 1280)
  - SL @ 1220.0 (kích hoạt khi giá xuống 1220)
```

**Ưu điểm:**
- ✅ Chính xác, biết rõ giá kích hoạt
- ✅ Dễ tính toán lãi/lỗ

**Nhược điểm:**
- ❌ Nếu giá vào lệnh thay đổi (modify/khớp thêm), cần điều chỉnh lại

#### Phương Thức 2: Offset-Based (Theo Khoảng Cách)

**Khái niệm:** Đặt biên trượt cố định theo số điểm so với giá vào lệnh

**Ví dụ:**
```
Vị thế: MUA VN30F2602 @ 1250.0
Đặt biên trượt: 30 điểm

Kết quả:
  - TP @ 1280.0 (= 1250 + 30)
  - SL @ 1220.0 (= 1250 - 30)
```

**Ưu điểm:**
- ✅ Dễ dàng áp dụng chiến lược R:R ratio cố định (ví dụ: luôn TP/SL = 30 điểm)
- ✅ Không cần tính toán giá cụ thể, chỉ cần biết khoảng cách

**Nhược điểm:**
- ❌ Giá kích hoạt phụ thuộc vào giá vào lệnh
- ❌ Khi vào lệnh ở nhiều giá khác nhau, TP/SL sẽ khác nhau

#### So Sánh 2 Phương Thức

| Tiêu Chí | Price-Based | Offset-Based |
|----------|-------------|--------------|
| **Cách đặt** | Nhập giá cụ thể | Nhập số điểm chênh lệch |
| **Giá kích hoạt** | Cố định, độc lập với giá vào | Phụ thuộc vào giá vào lệnh |
| **Tính toán** | Cần biết target giá | Chỉ cần biết khoảng cách |
| **Use case** | Trader biết rõ target giá | Trader dùng R:R ratio cố định |

**Ví dụ minh họa sự khác biệt:**

```
Trader A (Price-Based):
  - Vào lệnh @ 1250 → Đặt TP @ 1280, SL @ 1220
  - Vào thêm @ 1255 → Vẫn giữ TP @ 1280, SL @ 1220
  → TP/SL không đổi dù giá vào thay đổi

Trader B (Offset-Based):  
  - Vào lệnh @ 1250 với offset 30 điểm
    → TP @ 1280 (1250 + 30), SL @ 1220 (1250 - 30)
  - Vào thêm @ 1255 với offset 30 điểm
    → TP @ 1285 (1255 + 30), SL @ 1225 (1255 - 30)
  → Mỗi lần vào lệnh, TP/SL tính theo giá vào đó
```

### Bước 3: Nhập thông tin

Nhấn nút **"Đặt TP/SL"** → Chọn phương thức → Nhập thông tin:

**Với Price-Based:**

| Trường | Yêu Cầu | Ví Dụ |
|--------|---------|-------|
| **Khối lượng** | ≤ Khối lượng vị thế | 10 hợp đồng |
| **Giá TP** | > Giá vào (LONG) | 1280.0 |
| **Giá SL** | < Giá vào (LONG) | 1220.0 |

**Với Offset-Based:**

| Trường | Yêu Cầu | Ví Dụ |
|--------|---------|-------|
| **Khối lượng** | ≤ Khối lượng vị thế | 10 hợp đồng |
| **Offset (Biên trượt)** | Số điểm dương | 30 điểm |

**Lưu ý về Offset:**
- Chỉ nhập **1 giá trị offset**
- Hệ thống tự động tính cả TP và SL:
  - **TP** = Giá vào + Offset (với LONG)
  - **SL** = Giá vào - Offset (với LONG)
  - Với SHORT, ngược lại
- Ví dụ: Vào @ 1250 với offset 30 → TP @ 1280, SL @ 1220

### Bước 4: Xác nhận đặt lệnh

Nhấn **"Xác nhận"** → Hệ thống hiển thị: **"Đặt lệnh TP/SL thành công"**

**Hiển thị thông tin:**
- **Price-Based:** Hiển thị giá kích hoạt cố định (VD: TP @ 1280.0)
- **Offset-Based:** Hiển thị offset + giá kích hoạt hiện tại (VD: TP +30 điểm @ 1280.0)

### Bước 5: Kiểm tra lệnh

Xem lệnh vừa đặt tại:
- **Danh mục Vị thế mở:** Hiển thị giá TP/SL hoặc offset bên cạnh vị thế
- **Sổ lệnh TP/SL:** Xem chi tiết tất cả lệnh đang hoạt động

### Bước 6: Sửa/Hủy lệnh (nếu cần)

Nhấn vào **giá TP/SL** hoặc icon **"Cài đặt"** → Popup chi tiết → Chọn:
- **Sửa lệnh:** Thay đổi giá TP/SL
- **Hủy lệnh:** Hủy hoàn toàn

---

## 4. Quy Tắc Validation

### 4.1 Với Vị Thế LONG (Mua) - Price-Based

| Loại | Quy Tắc | Ví Dụ |
|------|---------|-------|
| **Giá TP** | Phải **>** giá vào lệnh | Mua @ 1250 → TP phải > 1250 |
| **Giá SL** | Phải **<** giá vào lệnh | Mua @ 1250 → SL phải < 1250 |

**Ví dụ đúng:**
```
Vị thế: MUA 10 VN30F2602 @ 1250.0

✅ Đặt TP @ 1280.0 (cao hơn 1250)
✅ Đặt SL @ 1220.0 (thấp hơn 1250)
```

**Ví dụ sai:**
```
Vị thế: MUA 10 VN30F2602 @ 1250.0

❌ Đặt TP @ 1240.0 → Lỗi: "TP phải cao hơn giá vào lệnh"
❌ Đặt SL @ 1260.0 → Lỗi: "SL phải thấp hơn giá vào lệnh"
```

### 4.2 Với Vị Thế SHORT (Bán) - Price-Based

| Loại | Quy Tắc | Ví Dụ |
|------|---------|-------|
| **Giá TP** | Phải **<** giá vào lệnh | Bán @ 1250 → TP phải < 1250 |
| **Giá SL** | Phải **>** giá vào lệnh | Bán @ 1250 → SL phải > 1250 |

### 4.3 Offset-Based (Cả LONG và SHORT)

**Quy tắc chung:**
- ✅ Offset phải là **số dương** (> 0)
- ✅ Hệ thống tự động tính TP và SL dựa vào giá vào lệnh

**Công thức:**

**Với LONG (Mua):**
- TP = Giá vào + Offset
- SL = Giá vào - Offset

**Với SHORT (Bán):**
- TP = Giá vào - Offset  
- SL = Giá vào + Offset

**Ví dụ với LONG:**
```
Vị thế: MUA VN30F2602 @ 1250.0
Offset: 30 điểm

Kết quả:
✅ TP @ 1280.0 (= 1250 + 30)
✅ SL @ 1220.0 (= 1250 - 30)

❌ Offset = 0 → Lỗi: "Offset phải lớn hơn 0"
❌ Offset = -30 → Lỗi: "Offset phải là số dương"
```

**Ví dụ với SHORT:**
```
Vị thế: BÁN VN30F2602 @ 1250.0  
Offset: 30 điểm

Kết quả:
✅ TP @ 1220.0 (= 1250 - 30)
✅ SL @ 1280.0 (= 1250 + 30)
```

---

## 5. Nguyên Tắc Kích Hoạt

### 5.1 Điều Kiện Kích Hoạt

**Với vị thế LONG (Mua):**
- **TP kích hoạt:** Giá thị trường **≥** Giá TP
- **SL kích hoạt:** Giá thị trường **≤** Giá SL

**Với vị thế SHORT (Bán):**
- **TP kích hoạt:** Giá thị trường **≤** Giá TP
- **SL kích hoạt:** Giá thị trường **≥** Giá SL

### 5.2 Xử Lý Khi Kích Hoạt

**Bước 1:** Hệ thống phát hiện giá đạt điều kiện  
**Bước 2:** Tự động tạo lệnh MTL (Market To Limit) gửi vào Core  
**Bước 3:** Lệnh được khớp trên sàn  
**Bước 4:** Cập nhật trạng thái = "Đã kích hoạt"  
**Bước 5:** Gửi thông báo cho khách hàng (WebSocket + Push notification)

**Loại lệnh gửi vào Core:** **MTL (Market To Limit)**
- Ưu tiên khớp nhanh như lệnh thị trường
- Phần chưa khớp chuyển thành lệnh giới hạn
- Bảo vệ khỏi trượt giá quá mức

### 5.3 So Sánh Với Vị Thế Hiện Tại

**Quy tắc:** Khi kích hoạt, khối lượng đẩy = **Min(KL vị thế hiện tại, KL lệnh TP/SL)**

**Mục đích:** Đảm bảo không làm đảo chiều vị thế

---

## 6. Ví Dụ Minh Họa

### Ví Dụ 1: TP Kích Hoạt Thành Công

**Setup:**
```
Vị thế: MUA 10 VN30F2602 @ 1250.0
Đặt lệnh: TP @ 1280.0
```

**Diễn biến:**
```
10:30 - Giá thị trường lên 1280.0
      → Hệ thống phát hiện: 1280.0 ≥ 1280.0 ✓
      → Tự động gửi lệnh BÁN MTL, 10 hợp đồng
      → Lệnh khớp @ 1280.0
      → Vị thế đã đóng, lãi = (1280 - 1250) × 10 = +300 điểm
      → Thông báo: "🎯 TP đã kích hoạt"
```

### Ví Dụ 2: SL Bảo Vệ Khỏi Lỗ Lớn

**Setup:**
```
Vị thế: MUA 5 VN30F2602 @ 1250.0
Đặt lệnh: SL @ 1220.0
```

**Diễn biến:**
```
14:15 - Tin xấu, giá giảm nhanh
14:16 - Giá chạm 1220.0
      → Hệ thống phát hiện: 1220.0 ≤ 1220.0 ✓
      → Tự động gửi lệnh BÁN MTL, 5 hợp đồng
      → Lệnh khớp @ 1220.0
      → Vị thế đã đóng, lỗ = (1220 - 1250) × 5 = -150 điểm
      → Lỗ được kiểm soát ở mức chấp nhận
```

### Ví Dụ 3: So Sánh Price-Based vs Offset-Based

**Trader A dùng Price-Based:**
```
Lần 1: Mua 10 VN30F2602 @ 1250.0
       → Đặt TP @ 1280.0, SL @ 1220.0

Lần 2: Mua thêm 5 VN30F2602 @ 1255.0
       → Vẫn giữ TP @ 1280.0, SL @ 1220.0 (không đổi)
       
Kết quả:
  - Vị thế: 15 hợp đồng, giá vào TB = 1251.67
  - TP @ 1280.0: Lãi trung bình = (1280 - 1251.67) × 15 = +425 điểm
  - SL @ 1220.0: Lỗ trung bình = (1220 - 1251.67) × 15 = -475 điểm
  - R:R không cân bằng (do giá vào thay đổi)
```

**Trader B dùng Offset-Based:**
```
Lần 1: Mua 10 VN30F2602 @ 1250.0, Offset 30 điểm
       → TP @ 1280.0 (1250 + 30)
       → SL @ 1220.0 (1250 - 30)

Lần 2: Mua thêm 5 VN30F2602 @ 1255.0, Offset 30 điểm  
       → TP @ 1285.0 (1255 + 30)
       → SL @ 1225.0 (1255 - 30)
       
Kết quả:
  - Có 2 lệnh TP/SL riêng biệt
  - Lệnh 1: 10 hợp đồng với TP @ 1280, SL @ 1220
  - Lệnh 2: 5 hợp đồng với TP @ 1285, SL @ 1225
  - Mỗi lệnh đều duy trì R:R = 1:1 (30 điểm)
```

### Ví Dụ 4: Vị Thế Giảm Trong Lúc Chờ

**Setup:**
```
Vị thế ban đầu: MUA 10 VN30F2602 @ 1250.0
Đặt lệnh: TP @ 1280.0 (KL: 10)
```

**Trường hợp 4a: Vị thế giảm một phần**
```
Khách hàng đặt lệnh BÁN thường ở ngoài, khớp 3 hợp đồng
→ Vị thế còn lại: 7 hợp đồng

Khi TP kích hoạt:
→ KL đẩy = Min(7, 10) = 7 hợp đồng
→ Chỉ đóng 7 hợp đồng (không đủ 10 như lúc đặt)
```

**Trường hợp 4b: Vị thế đóng hết**
```
Khách hàng đặt lệnh BÁN thường ở ngoài, khớp 10 hợp đồng
→ Vị thế còn lại: 0

Hệ thống xử lý:
→ Tự động HỦY lệnh TP/SL
→ Lý do: "Vị thế đã đóng hết"
```

**Trường hợp 4c: Vị thế đảo chiều**
```
Khách hàng đặt lệnh BÁN thường ở ngoài, khớp 15 hợp đồng
→ Vị thế ban đầu: MUA 10
→ Vị thế hiện tại: BÁN 5 (đã đảo chiều)

Hệ thống xử lý:
→ Tự động HỦY lệnh TP/SL
→ Lý do: "Vị thế đã đảo chiều"
```

### Ví Dụ 5: Một Trong Hai Chân Được Kích Hoạt

**Setup:**
```
Vị thế: MUA 5 VN30F2602 @ 1250.0
Đặt lệnh: 
  - TP @ 1280.0
  - SL @ 1220.0
```

**Kịch bản 5a: TP kích hoạt trước, khớp hết**
```
10:30 - Giá lên 1280.0
      → Kích hoạt TP, gửi lệnh BÁN 5 @ 1280.0
      → Lệnh khớp hết 5 hợp đồng
      → Hệ thống TỰ ĐỘNG HỦY chân SL (không cần nữa)
```

**Kịch bản 5b: TP kích hoạt, khớp một phần, sau đó SL kích hoạt**
```
10:30 - Giá lên 1280.0
      → Kích hoạt TP, gửi lệnh BÁN LO 5 @ 1280.5
      → Khớp được 2 hợp đồng, còn 3 chưa khớp

11:00 - Giá đảo chiều, giảm xuống 1220.0
      → Kích hoạt SL
      → Hủy lệnh TP đang chờ (3 hợp đồng)
      → Gửi lệnh BÁN 3 @ 1220.0 (SL)
      
Kết quả:
  - 2 hợp đồng đóng @ 1280.5 (từ TP)
  - 3 hợp đồng đóng @ 1220.0 (từ SL)
```

**Kịch bản 5c: TP với lệnh thị trường (MTL)**
```
10:30 - Giá lên 1280.0
      → Kích hoạt TP, gửi lệnh BÁN MTL 5 hợp đồng
      → Khớp 2 @ giá tốt nhất, 3 còn lại chuyển thành LO
      → Giá giảm nhanh, 3 hợp đồng không khớp được

11:00 - Giá xuống 1220.0
      → Kích hoạt SL
      → Hủy 3 hợp đồng LO còn lại từ TP
      → Gửi lệnh BÁN 3 @ 1220.0 (SL)
```

### Ví Dụ 6: Lệnh TP/SL Thất Bại (Core Từ Chối)

**Setup:**
```
Vị thế: MUA 10 VN30F2602 @ 1250.0
Đặt lệnh: TP @ 1280.0
```

**Diễn biến:**
```
10:30 - Giá lên 1280.0
      → TP kích hoạt, gửi lệnh BÁN MTL vào Core
      
Core phản hồi:
      → Lỗi: "Không đủ ký quỹ"
      → Lệnh bị TỪ CHỐI
      
Hệ thống xử lý:
      → Cập nhật trạng thái TP/SL = "Thất bại"
      → GỬI THÔNG BÁO KHẨN: "🔴 TP/SL không thể thực hiện"
      → Khách hàng cần xử lý ngay (nạp thêm ký quỹ hoặc đóng vị thế thủ công)
```

---

## 7. Thông Báo Realtime

### 7.1 Khi Nào Nhận Thông Báo?

| Sự Kiện | Nhận Thông Báo? | Loại |
|---------|-----------------|------|
| **TP/SL đã kích hoạt** | ✅ CÓ | WebSocket + Push |
| **Lệnh gốc bị hủy** (TP/SL tự động hủy) | ✅ CÓ | WebSocket + Push |
| **Core từ chối lệnh** | ✅ CÓ | WebSocket + Push |
| Tự hủy lệnh | ❌ KHÔNG | - |
| Hết hạn cuối ngày | ❌ KHÔNG | - |

### 7.2 Ví Dụ Thông Báo

**TP đã kích hoạt:**
```
🎯 TP/SL đã kích hoạt

Lệnh TP cho VN30F2602 đã kích hoạt tại 1280.0
Vị thế đã đóng

[Xem Chi Tiết]
```

**Core từ chối:**
```
🔴 TP/SL không thể thực hiện

Lệnh TP cho VN30F2602 bị lỗi
Lý do: Không đủ ký quỹ

Cần xử lý ngay!

[Xem Chi Tiết] [Đóng Vị Thế]
```

---

## 8. Lưu Ý Quan Trọng

### Giờ Không Kích Hoạt

❌ Lệnh TP/SL **KHÔNG kích hoạt** trong:
- Phiên khớp lệnh định kỳ ATO (09:00-09:15)
- Phiên khớp lệnh định kỳ ATC (14:30-14:45)
- Phiên nghỉ trưa (11:30-13:00)

✅ Chỉ kích hoạt trong **phiên liên tục** (LO: 09:15-11:30, 13:00-14:30)

### Hiệu Lực

- **Trong ngày:** Lệnh tự động hết hiệu lực lúc 15:00
- **Tự động hủy khi:**
  - Vị thế đã đóng hết
  - Vị thế đảo chiều
  - Hợp đồng hết hạn

### Giới Hạn

- Một vị thế có thể có nhiều lệnh TP/SL
- Mỗi lệnh TP/SL có thể có cả TP và SL
- Khi một chân kích hoạt và khớp hết → Chân còn lại tự động hủy

---

## 9. So Sánh Với Các Công Ty Khác

| Công Ty | Có TP/SL? | Loại Lệnh Khi Trigger | Thông Báo Realtime |
|---------|-----------|----------------------|-------------------|
| **NHSV** | ✅ Có | MTL | WebSocket + Push |
| SSI | ✅ Có | MOK/MAK | Push |
| VPS | ✅ Có | MOK | Push |
| HSC | ✅ Có | MOK | Push |
| VPBank | ✅ Có | MOK/MAK/MTL | - |

**Ưu điểm NHSV:**
- ✅ Dùng MTL: Cân bằng tốc độ và bảo vệ giá
- ✅ WebSocket: Cập nhật tức thời khi app đang mở
- ✅ Push: Thông báo ngay cả khi app đã tắt

---

**Tóm tắt:**
- TP/SL giúp tự động chốt lời/cắt lỗ
- Hệ thống tự động gửi lệnh MTL khi kích hoạt
- Nhận thông báo realtime qua WebSocket + Push
- Không cần theo dõi màn hình liên tục
