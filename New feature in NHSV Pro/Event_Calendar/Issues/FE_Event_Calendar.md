# [FE] Event Calendar — Lịch sự kiện cổ tức (A-02)

## Bối cảnh

Màn hình Lịch sự kiện (danh sách + chi tiết) hiện đang là bản dựng tạm với dữ liệu giả, chưa nối API thật. Issue này yêu cầu hoàn thiện tính năng để user xem được lịch sự kiện cổ tức/quyền thực tế của các mã chứng khoán, cả từ màn hình riêng lẫn từ Home, và có thể mở thẳng vào sự kiện khi nhận push notification.

---

## User Story

**As a** nhà đầu tư dùng NHSV Pro
**I want to** xem được lịch các sự kiện cổ tức/quyền (chia tiền, chia cổ phiếu, phát hành thêm) sắp diễn ra, lọc theo sàn, và xem chi tiết từng sự kiện
**So that** tôi nắm được lịch quyền lợi cổ đông và ra quyết định giao dịch kịp thời

---

## Yêu cầu chức năng

### 1. Danh sách sự kiện (màn hình riêng + section trên Home)

Màn hình gọi API `GET /api/v1/eventCalendar/upcoming?exchange={exchange}` để lấy danh sách sự kiện sắp tới, gọi lại mỗi khi user đổi sàn lọc.

- Hiển thị danh sách sự kiện cổ tức/quyền sắp tới, nhóm theo **ngày giao dịch không hưởng quyền** (`gdkhqDate`), sắp xếp gần nhất trước.
- Cho phép lọc theo sàn: Tất cả (mặc định), HOSE, HNX, Upcom — dùng field `exchange` của từng sự kiện để lọc.
- Ngày trong danh sách cần phân biệt rõ dựa trên `gdkhqDate` so với `asOfDate` (ngày hệ thống trả về từ API, không dùng ngày trên máy user): hôm nay, ngày mai, và các ngày còn lại (kèm số ngày nữa).
- Mỗi sự kiện hiển thị: `stockCode` (mã CK), `eventTypeLabel` (loại sự kiện, tiếng Việt), `gdkhqDate` (ngày GDKHQ), `rateDisplay` (tỷ lệ/giá trị quyền lợi, hiển thị đúng chuỗi BE trả về). Sự kiện có `isToday = true` cần có dấu hiệu nổi bật để dễ nhận ra.
- Tap vào một sự kiện → mở màn hình chi tiết của đúng `eventId` đó.
- Cần có trạng thái chờ tải, trạng thái không có dữ liệu (khi `events` rỗng, kèm thông báo phù hợp), và trạng thái lỗi có cho phép thử lại.
- Trên Home, thêm một section riêng cho Lịch sự kiện (vị trí cụ thể trên Home cần confirm lại với PM), dùng chung API/dữ liệu với màn hình danh sách. Nếu section này lỗi thì chỉ ảnh hưởng riêng nó, không kéo lỗi các phần khác của Home.
- Khi user quay lại danh sách từ màn hình chi tiết, bộ lọc sàn đang chọn phải được giữ nguyên, không reset về mặc định.

### 2. Chi tiết sự kiện

Màn hình nhận `eventId` từ danh sách (hoặc từ deep link) và gọi API `GET /api/v1/eventCalendar/{eventId}` khi mở màn.

- Hiển thị đầy đủ thông tin của một sự kiện: `stockCode`/`companyName`, `exchange`, `eventTypeLabel`, `gdkhqDate` (và `ndkccDate` — ngày đăng ký cuối cùng — nếu khác null), `rateDisplay`, `titleEvent` (mô tả sự kiện) nếu khác null.
- Với sự kiện dạng phát hành thêm (`eventType = RIGHTS_ISSUE`), cần hiển thị thêm giá quyền mua từ field `priceDisplay`.
- Các field trả về `null` (`ndkccDate`, `priceDisplay`, `titleEvent`, `fileUrl`) thì ẩn nguyên dòng/khối tương ứng, không hiển thị placeholder trống.
- Nếu `fileUrl` khác null, cho phép user mở xem tài liệu chính thức đó trong app.
- Có ghi chú nhỏ về nguồn dữ liệu (Vietstock) và khuyến nghị user xác nhận lại tại nguồn chính thức.
- Có nút để đi thẳng đến màn hình giao dịch của mã CK đang xem (dùng `stockCode`).
- Nếu API trả lỗi "không tìm thấy" (sự kiện đã qua ngày và bị hệ thống dọn dữ liệu — trường hợp hay gặp khi mở từ notification cũ), hiển thị thông báo "Sự kiện này đã kết thúc." và cho phép quay lại thay vì lỗi trắng màn hình.

### 3. Mở từ thông báo đẩy (push notification)

- Khi user nhấn vào push notification liên quan đến một sự kiện cổ tức/quyền, app phải mở thẳng vào màn hình chi tiết của đúng sự kiện đó.
- Hành vi này cần hoạt động dù app đang mở sẵn, chạy nền, hay đã tắt hẳn.

---

## Acceptance Criteria

- [ ] Danh sách sự kiện hiển thị dữ liệu thật, không còn dữ liệu mẫu
- [ ] Lọc theo sàn (Tất cả/HOSE/HNX/Upcom) hoạt động đúng
- [ ] Sự kiện diễn ra hôm nay được đánh dấu rõ ràng, dễ phân biệt với các ngày khác
- [ ] Danh sách xử lý đủ 3 trạng thái: đang tải, không có dữ liệu, lỗi (có thử lại)
- [ ] Tap vào sự kiện mở đúng màn hình chi tiết tương ứng
- [ ] Màn hình chi tiết hiển thị đúng thông tin theo từng loại sự kiện (cổ tức tiền mặt / cổ tức cổ phiếu / phát hành thêm)
- [ ] Sự kiện phát hành thêm hiển thị giá quyền mua khi có dữ liệu
- [ ] Các trường không có dữ liệu được ẩn hoàn toàn, không hiển thị rỗng
- [ ] Có thể mở tài liệu chính thức khi sự kiện có đính kèm
- [ ] Nút đi đến màn hình giao dịch hoạt động đúng mã CK
- [ ] Push notification mở đúng sự kiện ở mọi trạng thái app (đang mở, chạy nền, đã tắt)
- [ ] Quay lại danh sách từ chi tiết giữ nguyên bộ lọc sàn đang chọn
- [ ] Section Lịch sự kiện trên Home lỗi không ảnh hưởng các section khác

---

## Cần confirm thêm

- Vị trí chính xác của section Lịch sự kiện trên Home (đang đề xuất đặt sau phần "Hoạt động tích cực") — confirm với PM.
- Định dạng dữ liệu sự kiện đính kèm trong payload push notification — confirm với BE trước khi implement.

---

Document Status: 📋 Draft | For: Mobile FE Developer | Next Steps: Confirm vị trí section trên Home với PM, sync với BE về format payload notification
