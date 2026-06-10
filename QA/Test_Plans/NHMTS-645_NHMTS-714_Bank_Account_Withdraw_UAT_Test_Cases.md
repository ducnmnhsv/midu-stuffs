# Test Cases – Quản lý tài khoản ngân hàng & Màn rút tiền (NHMTS-645 & NHMTS-714)

**Đối tượng:** Phòng nghiệp vụ (UAT – non-tech)  
**Phạm vi:** NHSV Pro App – Cài đặt (Danh sách ngân hàng thụ hưởng), Đăng ký/Xóa tài khoản ngân hàng, OTP; Màn Rút tiền (Ngân hàng nhận mặc định)  
**Nguồn:** [NHMTS-645](https://nhsv-vn.atlassian.net/browse/NHMTS-645) | [NHMTS-714](https://nhsv-vn.atlassian.net/browse/NHMTS-714)

---

## Hướng dẫn cột bảng

| Column | Ý nghĩa |
|--------|---------|
| **Test case no** | Mã test case (vd: TC-645-01) – dùng khi báo lỗi |
| **Test case name** | Tên ngắn của kịch bản test |
| **Description** | Mô tả nội dung cần test |
| **Preconditions** | Điều kiện cần có trước khi test (đăng nhập, dữ liệu, v.v.) |
| **Test steps** | Các bước thao tác theo thứ tự |
| **Expected results** | Kết quả mong đợi khi Pass |
| **Actual result** | Tester điền kết quả thực tế khi chạy test (để trống trước khi test) |
| **Status** | Pass / Fail / Blocked (tester đánh dấu sau khi test) |

---

## NHMTS-645 – Quản lý tài khoản ngân hàng (đăng ký, xóa, OTP)

### Bảng 1 – Danh sách & màn Thêm TKNH

| Test case no | Test case name | Description | Preconditions | Test steps | Expected results | Actual result | Status |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|
| TC-645-01 | Truy cập danh sách ngân hàng thụ hưởng | Kiểm tra vào màn và hiển thị danh sách TKNH từ API | Đã đăng nhập | 1. Vào **More**. 2. Chọn **Beneficial bank lists**. 3. Quan sát màn hình. | Gọi API GET /api/v1/equity/withdraw/banks; hiển thị danh sách TKNH. Giao diện theo [Figma 40004615-251847](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004615-251847). | | |
| TC-645-02 | Nút Next disabled khi form trống | Kiểm tra nút Tiếp theo bị tắt khi chưa nhập gì | Đang ở màn Thêm tài khoản ngân hàng | 1. Không chọn/không nhập Bank, Branch, Account number. 2. Quan sát nút **Next** (Tiếp theo). | Nút **Next** disabled (không bấm được). | | |
| TC-645-03 | Bắt buộc Bank, Branch, Account number | Kiểm tra chỉ enable Next khi đủ 3 trường (cơ chế Bank/Branch giống eKYC) | Đang ở màn Thêm TKNH | 1. Chọn **Bank** (giống eKYC). 2. Chọn **Branch** (giống eKYC). 3. Nhập **Account number**. 4. Kiểm tra có thể bấm Next. | Chỉ khi đủ Bank, Branch, Account number thì nút Next mới enable. Thiếu bất kỳ → Next disabled. | | |
| TC-645-04 | Số TK trùng – báo lỗi | Kiểm tra thông báo khi nhập số TK đã đăng ký | Đã có ít nhất 1 TKNH với số TK X | 1. Chọn Bank, Branch. 2. Nhập **Account number** trùng với TK đã có (X). 3. Thử Next/tiếp tục. | Hiển thị lỗi: **"This account is already registered, try a different one"**. Không cho đăng ký. | | |

### Bảng 2 – Dialog xác nhận & luồng OTP

| Test case no | Test case name | Description | Preconditions | Test steps | Expected results | Actual result | Status |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|
| TC-645-05 | Dialog xác nhận – Hủy | Bấm Hủy đóng dialog, không gửi | Dialog xác nhận thông tin TKNH đang mở | 1. Bấm **Cancel** (Hủy). | Dialog đóng; không gửi request; quay lại màn nhập. [Figma 40004667-258570](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004667-258570) | | |
| TC-645-06 | Dialog xác nhận – Thêm → OTP | Bấm Thêm chuyển sang màn OTP | Dialog xác nhận đang mở | 1. Bấm **Add** (Thêm). | Chuyển màn **OTP verification**. Gọi /api/v1/otp/send. [Figma 40004718-259346](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004718-259346) | | |
| TC-645-07 | Resend OTP – 30s, tối đa 3 lần | Kiểm tra gửi lại OTP mỗi 30s, tối đa 3 lần | Đang ở màn OTP | 1. Chờ 30s (hoặc đến khi nút Gửi lại enable). 2. Bấm **Gửi lại OTP**. 3. Lặp tối đa 3 lần. | Cho phép gửi lại OTP mỗi **30 giây**; tối đa **3 lần**. | | |
| TC-645-08 | OTP_INCORRECT_MAX / OTP_LOCKED | Kiểm tra màn lỗi khi API trả OTP sai quá số lần hoặc khóa | Có thể nhập sai OTP nhiều lần / đợi lock | 1. Nhập sai OTP (hoặc đợi lock). 2. API trả **OTP_INCORRECT_MAX** hoặc **OTP_LOCKED**. | Hiển thị màn thông báo lỗi theo [Figma 40005969-305849](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005969-305849). | | |
| TC-645-09 | OTP_INVALID_MAX (verify) | Kiểm tra màn lỗi khi verify sai quá số lần | Gọi /api/v1/otp/verify sai nhiều lần đến khi trả OTP_INVALID_MAX | 1. Nhập sai OTP và xác minh nhiều lần. 2. API trả **OTP_INVALID_MAX**. | Hiển thị màn thông báo theo [Figma 40005969-305822](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005969-305822). | | |
| TC-645-10 | OTP đúng – đăng ký TKNH thành công | Kiểm tra gọi API đăng ký và thông báo thành công | Đã nhập đúng OTP; verify thành công | 1. Nhập đúng mã OTP. 2. Bấm Xác nhận. 3. App gọi /api/v1/account/bankAccount (POST). | API success → hiển thị **"Bank account added successfully"**. Danh sách TKNH cập nhật (có thêm TK mới). | | |
| TC-645-11 | API đăng ký TKNH thất bại | Kiểm tra thông báo khi API lỗi | Có thể mô phỏng API lỗi (mạng/backend) | 1. Thực hiện đến bước gọi /api/v1/account/bankAccount. 2. API trả lỗi. | Hiển thị **"Request sent fail, please try again"**. | | |

### Bảng 3 – Giới hạn 5 TKNH & Xóa TKNH

| Test case no | Test case name | Description | Preconditions | Test steps | Expected results | Actual result | Status |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|
| TC-645-12 | Tối đa 5 TKNH – disable Thêm | Đã có 5 TKNH thì không cho thêm | Tài khoản đã đăng ký **đủ 5** TKNH | 1. Vào **More → Beneficial bank lists**. 2. Quan sát nút **Add bank account**. | Nút **Add bank account** bị **disable**. | | |
| TC-645-13 | Chỉ 1 TKNH – disable Xóa | Chỉ còn 1 TKNH thì không cho xóa | Danh sách chỉ còn **1** TKNH | 1. Vào Danh sách ngân hàng thụ hưởng. 2. Quan sát nút **Delete** tại dòng TKNH. | Nút **Delete** **disabled** (bắt buộc giữ ít nhất 1 TK). | | |
| TC-645-14 | Bấm Xóa → dialog xác nhận | Có >1 TKNH thì bấm Xóa hiện dialog | Có **ít nhất 2** TKNH | 1. Bấm **Delete** (Xóa) tại một TKNH. | Hiển thị **dialog xác nhận xóa** [Figma 40004669-259101](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004669-259101). Có Cancel và Delete. | | |
| TC-645-15 | Dialog xóa – Hủy | Bấm Hủy đóng dialog, không xóa | Dialog xác nhận xóa đang mở | 1. Bấm **Cancel** (Hủy). | Dialog đóng; TKNH không bị xóa; danh sách không đổi. | | |
| TC-645-16 | Dialog xóa – Xóa → gọi API | Bấm Xóa gọi API xóa TKNH | Dialog xác nhận xóa đang mở | 1. Bấm **Delete** (Xóa). | Gọi API **DELETE /api/v1/account/bankAccount**. Thành công → TKNH biến mất khỏi danh sách. | | |

---

## NHMTS-714 – Màn Rút tiền (ngân hàng nhận mặc định)

| Test case no | Test case name | Description | Preconditions | Test steps | Expected results | Actual result | Status |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|
| TC-714-01 | Ngân hàng nhận mặc định | Màn Rút tiền hiển thị mục đầu tiên từ API làm mặc định | Đã đăng nhập; API /api/v1/equity/withdraw/banks trả danh sách | 1. Vào màn **Withdraw** (Rút tiền). 2. Quan sát ô **Receiving bank account** (Ngân hàng nhận). | Hiển thị **mục đầu tiên** (item 1) từ GET /api/v1/equity/withdraw/banks làm ngân hàng nhận **mặc định**. [Figma 40005994-227051](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005994-227051) | | |
| TC-714-02 | Dropdown – danh sách đầy đủ | Dropdown hiển thị tất cả ngân hàng từ API | Đang ở màn Withdraw | 1. Bấm dropdown **Ngân hàng nhận** (Receiving bank account). | Dropdown hiển thị **tất cả** ngân hàng từ API /api/v1/equity/withdraw/banks. | | |
| TC-714-03 | Chọn 1 ngân hàng – hiển thị bankName & bankAccountName | Chọn 1 ngân hàng thì hiển thị đúng tên NH và tên TK | Đang ở màn Withdraw, dropdown mở | 1. Chọn **một** ngân hàng trong danh sách. | Chỉ chọn được **1** ngân hàng. Hiển thị đúng **bankName** và **bankAccountName** tương ứng. | | |

---

## Summary

| Issue | Nội dung | Số TC | Danh sách ID |
|-------|----------|-------|--------------|
| NHMTS-645 | Quản lý TK ngân hàng (Settings, đăng ký, xóa, OTP, max 5) | 16 | TC-645-01 → TC-645-16 |
| NHMTS-714 | Màn Rút tiền – ngân hàng nhận mặc định, dropdown | 3 | TC-714-01 → TC-714-03 |

**Status:** Pass / Fail / Blocked — Điền **Actual result** khi có lỗi hoặc khác Expected results; báo lỗi qua Jira.

---

**Document Status:** ✅ Ready for UAT  
**For:** Phòng nghiệp vụ (non-tech), QA  
**Next Steps:** Phân công tester; điền Actual result & Status sau khi test; báo lỗi qua Jira (NHMTS-645, NHMTS-714).
