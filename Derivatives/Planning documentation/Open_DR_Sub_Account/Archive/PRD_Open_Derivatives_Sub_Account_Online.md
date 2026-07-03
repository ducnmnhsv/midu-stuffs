# PRD — Mở tiểu khoản phái sinh (sub 80) online

**Product:** NHSV Pro / TradeX Derivatives  
**Feature:** Đăng ký mở tiểu khoản phái sinh trực tuyến  
**Version:** 1.0  
**Last Updated:** April 9, 2026  
**Audience:** PM, BA, Tech Lead, Compliance, Operations

---

## 1. Tóm tắt điều hành

Khách hàng đã có tài khoản chứng khoán cơ sở có thể **tự phục vụ** đăng ký mở **tiểu khoản phái sinh (sub 80)** trên kênh online. Hệ thống **chặn** các trường hợp không đủ điều kiện ngay khi khởi động luồng; với khách đủ điều kiện, hệ thống **khởi tạo hợp đồng điện tử** qua **FPT eContract** (ký ảnh, xác thực **OTP email**, hiệu lực **90 ngày**), đồng thời **ghi nhận và quản lý trạng thái** trên **trang admin** cho vận hành và hỗ trợ khách hàng.

**Ghi chú phạm vi:** Các bước **gọi Lotte** để tạo sub 80, **mapping khoản vụ thanh toán**, và **thông báo sau khi provisioning hoàn tất** được **coi là ngoài phạm vi chi tiết** của PRD này cho đến khi có tài liệu/API từ Lotte; PRD vẫn nêu **điểm nối nghiệp vụ** để roadmap không bị đứt đoạn.

---

## 2. Vấn đề & cơ hội

| Vấn đề hiện tại | Cơ hội |
|-----------------|--------|
| Mở sub phái sinh phụ thuộc nhiều kênh thủ công, thời gian xử lý dài | Giảm ma sát, tăng tỷ lệ chuyển đổi sang giao dịch phái sinh |
| Thiếu minh bạch trạng thái cho nội bộ và CSKH | Admin thống nhất trạng thái: chờ ký, đã ký, hết hạn, lỗi |
| Rủi ro mở nhầm cho nhóm khách không đủ điều kiện | Rule kiểm tra tập trung, thông báo lý do rõ ràng cho KH |

---

## 3. Mục tiêu sản phẩm & chỉ số (đề xuất)

| Mục tiêu | Chỉ số gợi ý (cần baseline) |
|----------|-----------------------------|
| Cho phép KH đủ điều kiện hoàn tất đăng ký HĐ online | Tỷ lệ hoàn thành ký từ bước “Chờ ký” |
| Giảm ticket hỗ trợ “không biết đang ở đâu” | Số ticket liên quan trạng thái mở sub (sau/before) |
| Đảm bảo tuân thủ điện tử & truy vết | Đủ audit trail trên admin; thời điểm OTP, tạo HĐ, ký |

---

## 4. Định nghĩa & giả định

| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| Sub 80 | Tiểu khoản phái sinh theo quy ước nội bộ / Lotte |
| USR 002 | API tra cứu thông tin dùng cho rule **tài khoản liên kết bank** |
| EKY 007 | API tra cứu trạng thái **đã ký hợp đồng mở TKCK** |
| TKCK | Số tài khoản chứng khoán cơ sở |
| HDGDPS | Hợp đồng / phiếu đăng ký mở sub phái sinh (tên theo nghiệp vụ) |

**Giả định:** Output USR 002 và EKY 007 đủ để áp dụng các rule dưới đây; nếu thiếu field hoặc timeout, có chính sách lỗi thống nhất (không im lặng fail).

---

## 5. Phạm vi (In / Out)

| In scope | Out of scope (phiên bản PRD này hoặc giai đoạn sau) |
|----------|-----------------------------------------------------|
| Kiểm tra điều kiện khi KH bắt đầu luồng | Chi tiết thiết kế API TradeX (sẽ thuộc Specifications) |
| OTP gửi **email**, policy thử lại / khóa | Tích hợp SMS thay thế (có thể mở rộng sau) |
| Khởi tạo envelope / phiếu FPT, tên tài liệu, ký ảnh, TTL 90 ngày | Mapping từng biến template FPT theo file đính kèm (do BA/Pháp chế bổ sung) |
| Nhận tín hiệu từ FPT (callback hoặc cơ chế đồng bộ đã chọn) để cập nhật trạng thái ký | Chi tiết bảo mật webhook (ký số, IP allowlist) — level Technical Requirements |
| Trang **admin**: danh sách, chi tiết, trạng thái, hỗ trợ vận hành | Phân quyền chi tiết từng role (cần workshop IAM) |
| **Điểm nối sau ký:** “sẵn sàng cho bước Lotte” (trạng thái nội bộ) | **Đặc tả API Lotte** tạo sub 80, KV, push notification |

---

## 6. Người dùng & stakeholder

| Vai trò | Nhu cầu |
|---------|---------|
| Khách hàng retail | Biết có đủ điều kiện hay không; ký HĐ nhanh, rõ ràng |
| CSKH / Ops | Xem trạng thái, hỗ trợ khi kẹt “Chờ ký” / lỗi FPT |
| Compliance / Pháp chế | Tên tài liệu, hiệu lực, bằng chứng ký, lưu trữ |
| Engineering | Tách rõ eligibility, FPT, admin; idempotency và audit |

---

## 7. Luồng nghiệp vụ chi tiết

### 7.1 Bước 1 — Kiểm tra điều kiện (ngay khi KH bấm “Mở tiểu khoản phái sinh”)

**Mục tiêu:** Không cho phép vào bước HĐ nếu thuộc một trong các trường hợp sau.

| STT | Điều kiện chặn | Quy tắc nghiệp vụ | Ghi chú BA |
|-----|----------------|-------------------|------------|
| 1 | Tài khoản liên kết bank (loại không được phép) | Dùng output **USR 002**: nếu **mã ngân hàng (bank_code) khác 9999** thì coi là **tài khoản liên kết bank** → **chặn** | Cần định nghĩa chính thức ý nghĩa **9999**; xử lý null / nhiều bản ghi bank |
| 2 | Tài khoản không lưu ký tại NHSV | **Ba ký tự đầu** của số TKCK **khác 039** → **chặn** | Chuẩn hóa format TKCK (độ dài, ký tự); cùng rule trên server |
| 3 | Chưa ký hợp đồng mở TKCK | **EKY 007 = N** → **chặn** | Làm rõ bộ giá trị (N/Y/…); trạng thái đang xử lý |
| 4 | TKCK đã có sub phái sinh **active** | Có sub phái sinh đang hoạt động → **chặn** | Nguồn dữ liệu: khi chưa có API Lotte, ghi rõ **placeholder** hoặc nguồn tạm; không mâu thuẫn khi bổ sung LT sau |

**Output mong đợi về phía KH:** Cho phép tiếp tục **hoặc** từ chối kèm **thông điệp dễ hiểu** (và mã lý do nội bộ cho log / CSKH).

**Output mong đợi về phía hệ thống:** Một lần kiểm tra **tập trung** (khuyến nghị một luồng API nghiệp vụ thống nhất) để tránh lệch version giữa các client.

---

### 7.2 Bước 2 — Khởi tạo hợp đồng điện tử (FPT)

**Điều kiện vào bước:** KH đã pass bước 1.

| Hạng mục | Yêu cầu |
|----------|---------|
| Tên tài liệu | **HDGDPS_{Số TKCK}_{Họ tên KH}** — cần quy tắc chuẩn hóa chuỗi (bỏ dấu, ký tự cho phép, độ dài tối đa theo giới hạn FPT) |
| Loại chữ ký | **Ký ảnh** (cấu hình đúng trên template FPT) |
| Xác thực | **OTP gửi email** — thời điểm gửi/xác nhận cần cố định (ví dụ: trước khi tạo envelope hoặc trước khi mở webview ký); policy TTL, số lần thử, gửi lại |
| Hiệu lực | **90 ngày** — đồng bộ giữa cấu hình FPT (nếu áp dụng) và trạng thái nội bộ (hết hạn → không tiếp tục ký / yêu cầu tạo lại theo policy) |
| Sau khi FPT chấp nhận tạo phiếu | Lưu thông tin đăng ký; trên **admin** trạng thái ký = **Chờ ký** |

**Rủi ro cần quy định sản phẩm:** Trùng lệnh “tạo HĐ” (double tap), nhiều envelope cho cùng một TKCK, và hành vi khi OTP hết hạn giữa chừng.

---

### 7.3 Bước 3 — Ký hợp đồng & cập nhật hệ thống

| STT | Hành động | Kết quả mong đợi |
|-----|-----------|------------------|
| 1 | KH ký trên kênh FPT (webview / flow FPT) | FPT ghi nhận trạng thái ký |
| 2 | Hệ thống nhận **callback** (hoặc cơ chế đồng bộ đã thống nhất) từ FPT | Cập nhật trạng thái ký trong hệ thống; **hiển thị trên admin** |
| 3 | (Tương lai — ngoài chi tiết PRD) Gọi Lotte tạo sub 80 | **Tạm bỏ qua** cho đến khi có API |
| 4 | (Tương lai) Sau mapping KV thanh toán thành công | Gửi **thông báo** mở sub thành công — **tạm bỏ qua** chi tiết |

**Trạng thái sau ký trên nội bộ:** Ít nhất phân biệt: **Đã ký** vs **Chờ ký** vs **Hết hạn** vs **Lỗi / Hủy**; chi tiết bảng trạng thái có thể mở rộng khi có workshop Ops.

---

## 8. Yêu cầu chức năng theo đầu mục lớn

### 8.1 Verify tài khoản có đủ điều kiện không

- Thực hiện **đồng bộ** khi KH khởi động luồng (theo 7.1).
- Trả về **đủ/không đủ** và **lý do** (mapping với từng rule).
- Ghi **nhật ký** phục vụ CSKH (không nhất thiết hiển thị hết cho KH).

### 8.2 Khởi tạo HĐ với FPT

- Sau OTP email (theo policy), khởi tạo phiếu theo template đã chốt với tên tài liệu và ký ảnh.
- Cung cấp cho client **đường dẫn / cách truy cập** ký theo chuẩn FPT.
- Lưu **mã tham chiếu** FPT và metadata tối thiểu để đối soát.

### 8.3 Quản lý trên admin

- **Danh sách** đăng ký với lọc theo trạng thái, thời gian, TKCK / mã KH (theo policy bảo mật).
- **Chi tiết** từng hồ sơ: trạng thái ký, thời điểm tạo, hết hạn, mã FPT, lịch sử thay đổi trạng thái (audit).
- (Tùy chọn) Thao tác vận hành: **xem lại trạng thái từ FPT**, **retry** khi lỗi khởi tạo, ghi chú nội bộ — cần phân quyền.

---

## 9. Luồng dữ liệu (mức PM)

Mô tả tuần tự: **App** → **TradeX (eligibility)** → từ chối hoặc tiếp tục → **OTP email** → **TradeX + FPT (tạo phiếu)** → **Lưu DB + Admin (Chờ ký)** → **KH ký FPT** → **Callback FPT → TradeX** → cập nhật **Admin (Đã ký)** → *(sau này)* Lotte + KV + Noti.

---

## 10. Phi chức năng (yêu cầu khung)

| Lĩnh vực | Yêu cầu |
|----------|---------|
| Bảo mật | Bảo vệ OTP; giới hạn thử; truy vết thao tác admin |
| Tuân thủ | Lưu trữ chứng từ điện tử theo policy công ty (thời gian lưu, truy cập) |
| Khả dụng | Thông báo lỗi thân thiện khi USR/EKY/FPT timeout |
| Idempotency | Tránh tạo trùng envelope / trùng bản ghi đăng ký — cần quy tắc sản phẩm rõ |

---

## 11. Phụ thuộc & rủi ro

| Phụ thuộc | Rủi ro |
|-----------|--------|
| USR 002, EKY 007 ổn định và đúng nghĩa field | Sai rule → chặn/sai sót nhóm KH |
| FPT: template, quota, SLA callback | Kẹt “Chờ ký” lâu → tăng tải CSKH |
| Chưa có API Lotte | Trạng thái “Đã ký” chưa đồng nghĩa “đã giao dịch được” — cần thông điệp KH và nội bộ thống nhất |

---

## 12. Tiêu chí chấp nhận (mức sản phẩm)

- KH không đủ điều kiện theo **bất kỳ** rule 7.1 → **không** vào được bước tạo HĐ; thông báo đúng nhóm lý do.
- KH đủ điều kiện → sau OTP hợp lệ có thể **khởi tạo** HĐ FPT; admin thấy trạng thái **Chờ ký** trong thời gian hợp lý sau khi FPT chấp nhận tạo phiếu.
- Khi FPT báo đã ký → hệ thống và admin phản ánh **Đã ký** (hoặc tương đương); không mất sự kiện khi callback lặp.
- Hết 90 ngày không ký → trạng thái **Hết hạn** (hoặc tương đương) và có hướng dẫn KH/ops theo policy.

---

## 13. Câu hỏi mở (cần workshop)

- Định nghĩa chính thức **9999** và các mã bank trong USR 002.
- Một TKCK có **nhiều** phiếu “Chờ ký” hay chỉ **một** phiếu active tại một thời điểm?
- Sau **Đã ký**, thông điệp cho KH là gì khi **chưa** có Lotte (tránh hiểu nhầm đã giao dịch được)?
- Phân quyền admin chi tiết và yêu cầu mask dữ liệu nhạy cảm.

---

**Document Status:** 📋 Planning  
**For:** PM, BA, Tech Lead, Ops, Compliance  
**Next Steps:** Workshop chốt OTP timing + bảng trạng thái admin + quy tắc trùng envelope; Pháp chế chốt template FPT và tên tài liệu; khi có API Lotte — bổ sung epic Provisioning vào PRD v2 hoặc Specifications.
