# Feature Improvements — NHSV Pro Q2 2026

Tài liệu ghi lại các cải tiến được PM approve thêm vào sprint sau buổi competitive review ngày 2026-06-15. Các mục này bổ sung vào scope ban đầu (A-01 → B-05, X-01 → X-02) và đã được cập nhật vào `List of issues.html`.

---

## Nguồn gốc

Buổi competitive review đối chiếu NHSV Pro với các app cùng phân khúc (KIS, KBSV, SSI) trên các màn hình Home và Market Watch. Kết quả xác định 4 điểm còn thiếu so với đối thủ và 1 hạ tầng cross-cutting cần thêm.

---

## Các cải tiến được confirm

### 1. Push Notification — Event Calendar (A-02)

Khi app phát hiện sự kiện GDKHQ sắp đến trong vòng 3 ngày, hệ thống tự động gửi push notification để nhắc nhở user. Đây là tính năng phổ biến trên KIS và KBSV nhưng NHSV Pro hiện chưa có.

Giá trị business: tăng tỷ lệ user quay lại app trước ngày GDKHQ, hỗ trợ quyết định mua/bán kịp thời.

### 2. P&L Since Recommendation Date — Danh mục cơ bản (A-05.1)

Bổ sung thêm cột P&L tính từ ngày khuyến nghị vào danh sách Danh mục cơ bản. User nhìn thấy ngay hiệu quả thực tế của từng khuyến nghị so với giá thị trường hiện tại, không cần tự tính.

Giá trị business: tăng độ tin cậy vào khuyến nghị của Phòng Phân tích, giúp user ra quyết định nhanh hơn.

### 3. Sort/Filter theo Rating Level — NHSV Rating (A-05.2)

Bổ sung bộ lọc theo mức xếp hạng (S / A / B / C / D) vào màn hình NHSV Rating. Hiện tại user phải tự đọc từng dòng để tìm cổ phiếu tốt. Với sort/filter, user có thể xem ngay nhóm S và A mà không cần scroll toàn bộ danh sách.

Tham khảo: KBSV KB Rating có UX tương tự, phản hồi user rất tốt.

### 4. Status Indicator — Kỹ thuật hằng ngày (A-05.3)

Bổ sung trạng thái hiệu lực cho từng khuyến nghị kỹ thuật: Còn hiệu lực / Đạt target / Đã cắt lỗ. Hiện tại các khuyến nghị cũ vẫn hiển thị lẫn với khuyến nghị mới, gây nhầm lẫn cho user.

Giá trị business: tăng tính minh bạch và trách nhiệm giải trình của Phòng Phân tích, cải thiện trải nghiệm đọc.

---

## Work item mới — X-03 Push Notification Infrastructure

Các improvements liên quan đến push notification (mục 1, và push khi có bài mới ở A-04/A-05) đều cần một hạ tầng chung thay vì build riêng lẻ cho từng tính năng. X-03 được thêm vào sprint để tránh duplicate effort.

X-03 bao gồm ba phần: BE notification service với scheduler và trigger, FE deeplink handler để điều hướng user vào đúng màn hình sau khi tap notification, và phần mở rộng Admin Tool thêm nút "Publish & Notify" khi upload bài.

Chi tiết kỹ thuật: xem `Push_Notification/Specifications/X03_Push_Notification_Infrastructure.md`.

---

## Backlog — Chưa vào sprint

Các mục dưới đây được ghi nhận từ competitive review nhưng chưa được PM approve vào sprint hiện tại. Xem xét lại ở planning Q3.

**Market Watch:**
- GTGD Chart: so sánh đa phiên T-1 / T-5 / T-20 và surge indicator
- Market Leaders: tách view Contributors và Gainers/Losers riêng biệt
- Foreign Trading: rolling 5-phiên trend và header summary tổng hợp
- Sector Performance: dùng Treemap thay bar chart, có drill down theo ngành

**UX cross-cutting:**
- "NEW" badge trên tab mới để tăng discoverability
- Global search ở header NHSV Channel
- Portfolio overlay trên Market Watch
- Share card khuyến nghị ra ngoài app

---

## Tác động đến estimate

| Hạng mục | Layer bị ảnh hưởng | Ghi chú |
|---|---|---|
| P&L since recommendation | BE + FE | BE tính P&L realtime, FE hiển thị thêm 1 column |
| Sort/filter NHSV Rating | FE | Filter client-side, không cần BE mới |
| Status indicator | BE + FE | BE cần thêm field `status` vào content API |
| Push notification (tất cả) | BE + FE + Admin | X-03 estimate riêng, blocker cho A-02/A-04/A-05 |

---

Document Status: ✅ Confirmed | For: PM, IT Lead, Dev team | Next Steps: Đưa vào sprint backlog và estimate cùng X-03 open questions (Q1–Q3 trong spec)
