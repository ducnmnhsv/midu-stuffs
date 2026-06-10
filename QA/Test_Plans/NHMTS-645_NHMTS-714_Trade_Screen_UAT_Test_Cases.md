# Test Cases – Màn Trade Derivatives (KHÔNG phải NHMTS-645 / NHMTS-714)

> **Lưu ý:** NHMTS-645 và NHMTS-714 thực tế là **Quản lý TK ngân hàng** và **Màn Rút tiền**. Test cases cho 645 & 714: [NHMTS-645_NHMTS-714_Bank_Account_Withdraw_UAT_Test_Cases.md](NHMTS-645_NHMTS-714_Bank_Account_Withdraw_UAT_Test_Cases.md).  
> File này giữ lại test cases cho **màn Trade (Phái sinh)** – có thể dùng cho issue khác hoặc tham chiếu.

**Đối tượng:** Phòng nghiệp vụ (UAT – non-tech)  
**Phạm vi:** NHSV Pro App – Tab Trade (Phái sinh), tính năng màn Trade (Lệnh thường / Lệnh nhanh) và Max buy / Max sell  
**Nguồn:** Tham chiếu màn Trade – không phải issue 645/714

---

## Bảng theo dõi nhanh (Master Checklist)

*Dùng bảng này để đánh dấu Pass/Fail khi test; chi tiết từng case xem các bảng bên dưới.*

| STT | ID | Nhóm | Mô tả ngắn | Kết quả mong đợi (tóm tắt) | Pass/Fail | Người test | Ngày test | Ghi chú |
|-----|-----|------|-------------|-----------------------------|-----------|------------|-----------|---------|
| 1 | TC-645-01 | 645 | Hiển thị Max buy & Max sell khi vào màn Trade | Thấy Max buy dưới nút Mua, Max sell dưới nút Bán; số hợp lý | | | | |
| 2 | TC-645-02 | 645 | Đổi mã CK → Max buy/Max sell cập nhật | Giá trị thay đổi theo mã mới | | | | |
| 3 | TC-645-03 | 645 | Đổi tài khoản → Max buy/Max sell cập nhật | Giá trị thay đổi theo tài khoản mới | | | | |
| 4 | TC-645-04 | 645 | Nhập số lượng Mua > Max buy → báo lỗi | Toast "Vượt quá sức mua khả dụng"; không cho đặt lệnh | | | | |
| 5 | TC-645-05 | 645 | Nhập số lượng Bán > Max sell → báo lỗi | Toast "Vượt quá sức bán khả dụng"; không cho đặt lệnh | | | | |
| 6 | TC-645-06 | 645 | Số lượng Mua ≤ Max buy → cho phép đặt lệnh | Không lỗi; có thể gửi/xác nhận lệnh | | | | |
| 7 | TC-645-07 | 645 | Số lượng Bán ≤ Max sell → cho phép đặt lệnh | Không lỗi; có thể gửi/xác nhận lệnh | | | | |
| 8 | TC-645-08 | 645 | Max buy/Max sell trong Lệnh nhanh | Vẫn hiển thị đúng dưới nút Mua/Bán | | | | |
| 9 | TC-714-01 | 714 | Giao diện Lệnh thường đầy đủ | Đủ: header, mã CK, CE/REF/FL, sổ lệnh, Open position, form, tab, nút Mua/Bán + Max | | | | |
| 10 | TC-714-02 | 714 | Giao diện Lệnh nhanh gọn | Có bảng giá, form, Open position, nút Mua/Bán + Max; không TP/SL | | | | |
| 11 | TC-714-03 | 714 | Chuyển Lệnh thường ↔ Lệnh nhanh | Mã, giá, số lượng, loại lệnh giữ nguyên | | | | |
| 12 | TC-714-04 | 714 | Dialog xác nhận lệnh (Mua) | Dialog hiện symbol, Mua, loại lệnh, giá, SL; có Xác nhận & Hủy | | | | |
| 13 | TC-714-05 | 714 | Dialog xác nhận lệnh (Bán) | Dialog hiện symbol, Bán, loại lệnh, giá, SL; có Xác nhận & Hủy | | | | |
| 14 | TC-714-06 | 714 | Hủy dialog → không gửi lệnh | Dialog đóng; không có lệnh gửi | | | | |
| 15 | TC-714-07 | 714 | Xác nhận dialog → gửi lệnh thành công | Thông báo thành công + mã lệnh; dialog đóng | | | | |
| 16 | TC-714-08 | 714 | Giá trong khung CE/REF/FL | Cho phép đặt lệnh, không báo lỗi giá | | | | |
| 17 | TC-714-09 | 714 | Giá ngoài khung | Báo "Giá đặt ngoài khung giá cho phép"; không gửi lệnh | | | | |
| 18 | TC-714-10 | 714 | Tap CE/REF/FL hoặc Bid/Ask → điền ô Giá | Ô Giá tự điền đúng giá tương ứng | | | | |
| 19 | TC-714-11 | 714 | Số lượng = 0 hoặc trống | Không cho đặt; có thông báo lỗi | | | | |
| 20 | TC-714-12 | 714 | Real-time price bật (mặc định) | Ô Giá tự cập nhật theo thị trường | | | | |
| 21 | TC-714-13 | 714 | Tắt Real-time price → nhập giá tay | Giá không tự nhảy; giữ giá user nhập | | | | |
| 22 | TC-714-14 | 714 | Chỉnh giá tay → Real-time tự tắt | Công tắc Real-time chuyển sang tắt | | | | |
| 23 | TC-714-15 | 714 | Tắt Real-time, chờ 10s → tự bật lại | Công tắc tự bật lại; giá lại real-time | | | | |
| 24 | TC-714-16 | 714 | Block Open position & Unrealized PnL | Hiển thị đúng mã đang chọn, ngay dưới Bid/Ask | | | | |
| 25 | TC-714-17 | 714 | Tab Positions – danh sách vị thế | Có B/S, mã, SL, Unrealized PnL, Reverse/Close; không TP/SL | | | | |
| 26 | TC-714-18 | 714 | Đổi ngôn ngữ → thông báo lỗi đúng ngôn ngữ | Lỗi hiển thị bằng ngôn ngữ đã chọn (vd: EN) | | | | |

**Cách đánh dấu:** ✅ Pass | ❌ Fail — Ghi cột **Người test**, **Ngày test**, **Ghi chú** (build, thiết bị, ảnh) khi có lỗi.

---

## Hướng dẫn sử dụng bảng test

| Cột | Ý nghĩa |
|-----|---------|
| **ID** | Mã test case (để tra cứu, báo lỗi) |
| **Nhóm** | 645 = Max buy/Max sell; 714 = Màn Trade (giao diện, đặt lệnh, validation) |
| **Mô tả** | Tóm tắt nội dung cần test |
| **Điều kiện** | Cần có gì trước khi test (đăng nhập, tài khoản, mã CK, v.v.) |
| **Các bước** | Thao tác thực hiện (theo thứ tự) |
| **Kết quả mong đợi** | Khi Pass thì màn hình/hệ thống thể hiện như thế nào |
| **Pass/Fail** | Tester đánh dấu ✅ Pass hoặc ❌ Fail sau khi test |
| **Ghi chú** | Ghi thêm (lỗi, ảnh chụp màn hình, build/version) |

---

## Bảng 1 – Max buy / Max sell (NHMTS-645)

| ID | Nhóm | Mô tả | Điều kiện | Các bước | Kết quả mong đợi | Pass/Fail | Ghi chú |
|----|------|--------|-----------|----------|-------------------|-----------|---------|
| TC-645-01 | 645 | Hiển thị Max buy và Max sell khi vào màn Trade | Đã đăng nhập; đã chọn **tài khoản** và **mã chứng khoán** (ví dụ mã HĐTL) | 1. Vào Tab Trade (Phái sinh). 2. Chọn tài khoản và mã CK. 3. Quan sát khu vực dưới hai nút **Mua (Long)** và **Bán (Short)**. | Thấy **Max buy** (số lượng) ngay dưới nút Mua; **Max sell** (số lượng) ngay dưới nút Bán. Số liệu hợp lý (không âm, không lỗi). | | |
| TC-645-02 | 645 | Cập nhật Max buy / Max sell khi đổi mã CK | Đang ở màn Trade, đã có Max buy/Max sell hiển thị | 1. Đổi sang **mã CK khác** (tìm và chọn mã khác). 2. Quan sát lại Max buy và Max sell. | Max buy và Max sell **thay đổi** theo mã mới (có thể khác so với mã cũ). | | |
| TC-645-03 | 645 | Cập nhật Max buy / Max sell khi đổi tài khoản | Đang ở màn Trade, đã có Max buy/Max sell | 1. Đổi **tài khoản** (nếu có nhiều TK). 2. Quan sát Max buy và Max sell. | Max buy và Max sell **thay đổi** theo tài khoản mới. | | |
| TC-645-04 | 645 | Cảnh báo khi nhập số lượng Mua vượt Max buy | Max buy hiển thị (ví dụ 100). Đang chọn chiều **Mua** | 1. Nhập **Số lượng** lớn hơn Max buy (vd: 150 khi Max buy = 100). 2. Thử gửi lệnh hoặc chuyển ô nhập. | Xuất hiện **thông báo lỗi**: "Vượt quá sức mua khả dụng". Không cho đặt lệnh (hoặc phải sửa số lượng trước khi đặt). | | |
| TC-645-05 | 645 | Cảnh báo khi nhập số lượng Bán vượt Max sell | Max sell hiển thị (ví dụ 50). Đang chọn chiều **Bán** | 1. Nhập **Số lượng** lớn hơn Max sell (vd: 60 khi Max sell = 50). 2. Thử gửi lệnh hoặc chuyển ô nhập. | Xuất hiện **thông báo lỗi**: "Vượt quá sức bán khả dụng". Không cho đặt lệnh (hoặc phải sửa số lượng trước khi đặt). | | |
| TC-645-06 | 645 | Cho phép đặt lệnh khi số lượng ≤ Max buy (Mua) | Max buy = 100 | 1. Chọn Mua. 2. Nhập số lượng = 100 hoặc nhỏ hơn (vd: 50). 3. Điền giá, loại lệnh và bấm Mua → Xác nhận. | Không hiện lỗi "Vượt quá sức mua khả dụng"; lệnh được gửi (hoặc đến bước xác nhận đặt lệnh). | | |
| TC-645-07 | 645 | Cho phép đặt lệnh khi số lượng ≤ Max sell (Bán) | Max sell = 50 | 1. Chọn Bán. 2. Nhập số lượng = 50 hoặc nhỏ hơn (vd: 30). 3. Điền giá, loại lệnh và bấm Bán → Xác nhận. | Không hiện lỗi "Vượt quá sức bán khả dụng"; lệnh được gửi (hoặc đến bước xác nhận đặt lệnh). | | |
| TC-645-08 | 645 | Max buy/Max sell trong Lệnh nhanh | Đã chọn tài khoản và mã CK | 1. Chuyển sang chế độ **Lệnh nhanh**. 2. Quan sát khu vực nút Mua/Bán. | Vẫn thấy **Max buy** và **Max sell** tương ứng dưới nút Mua và Bán; giá trị giống với khi ở Lệnh thường (cùng TK, cùng mã). | | |

---

## Bảng 2 – Màn Trade – Giao diện & chuyển mode (NHMTS-714)

| ID | Nhóm | Mô tả | Điều kiện | Các bước | Kết quả mong đợi | Pass/Fail | Ghi chú |
|----|------|--------|-----------|----------|-------------------|-----------|---------|
| TC-714-01 | 714 | Giao diện Lệnh thường đầy đủ | Đã đăng nhập, vào Tab Trade | 1. Chọn **Lệnh thường**. 2. Quan sát toàn màn. | Có: header (tài khoản), ô tìm mã CK, thông tin thị trường (giá, CE/REF/FL), sổ lệnh (Bid/Ask), block Open position & Unrealized PnL, form (Giá, Số lượng, Loại lệnh, Real-time price), tab Positions / Orderbook / Conditional order, nút Mua (Long) và Bán (Short) với Max buy/Max sell. | | |
| TC-714-02 | 714 | Giao diện Lệnh nhanh gọn | Đã đăng nhập, vào Tab Trade | 1. Chọn **Lệnh nhanh**. 2. Quan sát toàn màn. | Giao diện gọn: có bảng giá (price ladder) và form đặt lệnh; vẫn có Open position, Unrealized PnL, nút Mua/Bán và Max buy/Max sell. Không có TP/SL. | | |
| TC-714-03 | 714 | Chuyển Lệnh thường ↔ Lệnh nhanh không mất dữ liệu | Đã nhập mã CK, giá, số lượng, loại lệnh ở Lệnh thường | 1. Nhập mã, giá, số lượng, chọn loại lệnh. 2. Chuyển sang **Lệnh nhanh**. 3. Quan sát form. | Mã CK, giá, số lượng, loại lệnh **giữ nguyên** (không bị xóa khi đổi mode). | | |
| TC-714-04 | 714 | Hiển thị dialog xác nhận lệnh trước khi gửi (Mua) | Đã nhập đủ thông tin lệnh Mua | 1. Nhập giá, số lượng, loại lệnh. 2. Bấm **Mua (Long)**. | Mở **dialog xác nhận lệnh** (theo design): hiển thị symbol, Mua, loại lệnh, giá, số lượng; có nút **Xác nhận** và **Hủy**. Chưa gửi lệnh lên sàn. | | |
| TC-714-05 | 714 | Hiển thị dialog xác nhận lệnh trước khi gửi (Bán) | Đã nhập đủ thông tin lệnh Bán | 1. Nhập giá, số lượng, loại lệnh. 2. Bấm **Bán (Short)**. | Mở **dialog xác nhận lệnh** (theo design): hiển thị symbol, Bán, loại lệnh, giá, số lượng; có nút Xác nhận và Hủy. Chưa gửi lệnh. | | |
| TC-714-06 | 714 | Hủy dialog xác nhận – không gửi lệnh | Dialog xác nhận đang mở | 1. Bấm **Hủy** (hoặc đóng dialog). | Dialog đóng; **không** có lệnh nào được gửi. Có thể chỉnh sửa lại và thử đặt lệnh khác. | | |
| TC-714-07 | 714 | Xác nhận trong dialog – gửi lệnh thành công | Dialog xác nhận đang mở, thông tin hợp lệ | 1. Bấm **Xác nhận**. | Gửi lệnh lên sàn; hiển thị **thông báo thành công** và **mã lệnh** (order number). Dialog đóng. | | |

---

## Bảng 3 – Validation giá, số lượng, Real-time price (NHMTS-714)

| ID | Nhóm | Mô tả | Điều kiện | Các bước | Kết quả mong đợi | Pass/Fail | Ghi chú |
|----|------|--------|-----------|----------|-------------------|-----------|---------|
| TC-714-08 | 714 | Giá trong khung CE/REF/FL – cho phép | Đã chọn mã CK (có CE, REF, FL) | 1. Nhập **Giá** nằm trong khoảng [sàn, trần] (giữa FL và CE). 2. Đặt lệnh. | Không báo lỗi về giá; có thể đến bước xác nhận/gửi lệnh. | | |
| TC-714-09 | 714 | Giá ngoài khung – báo lỗi | Đã chọn mã CK | 1. Nhập **Giá** ngoài khung (vd: dưới FL hoặc trên CE). 2. Thử đặt lệnh hoặc rời ô nhập. | Hiện thông báo: **"Giá đặt ngoài khung giá cho phép"**. Không cho gửi lệnh (hoặc phải sửa giá). | | |
| TC-714-10 | 714 | Tap CE/REF/FL hoặc ô giá Bid/Ask → điền vào ô Giá | Đang ở màn Trade, có sổ lệnh/CE/REF/FL | 1. Chạm vào **CE** (hoặc REF, FL, hoặc một ô giá trong bảng Bid/Ask). | Ô **Giá** tự động được điền đúng giá tương ứng (CE/REF/FL hoặc giá level đó). | | |
| TC-714-11 | 714 | Số lượng tối thiểu = 1 | Ở form đặt lệnh | 1. Nhập số lượng = **0** hoặc để trống (nếu cho phép). 2. Thử gửi lệnh. | Không cho đặt lệnh; có thông báo lỗi (ví dụ bắt buộc nhập, hoặc tối thiểu 1). | | |
| TC-714-12 | 714 | Real-time price: mặc định bật, giá tự cập nhật | Đã chọn mã CK, đang Lệnh thường hoặc Lệnh nhanh | 1. Kiểm tra công tắc **Real-time price** (giá theo thời gian thực). 2. Quan sát ô **Giá** trong vài giây. | Công tắc **bật** (mặc định). Ô Giá **tự động thay đổi** theo giá hiện tại trên thị trường. | | |
| TC-714-13 | 714 | Tắt Real-time price – có thể nhập/chỉnh giá tay | Đang ở màn Trade | 1. **Tắt** công tắc Real-time price. 2. Nhập hoặc chỉnh **Giá** bằng tay (trong khung FL–CE). | Giá **không** tự nhảy theo thị trường; giữ nguyên giá user nhập. Có thể đặt lệnh với giá đó. | | |
| TC-714-14 | 714 | Chỉnh giá bằng tay → Real-time price tự tắt | Real-time price đang bật | 1. Dùng nút +/- hoặc nhập giá trực tiếp để **thay đổi Giá**. | Công tắc Real-time price **tự chuyển sang tắt** (user đang chỉnh tay). | | |
| TC-714-15 | 714 | Sau 10 giây tắt Real-time price → tự bật lại | Real-time price đã tắt | 1. Tắt công tắc. 2. **Chờ 10 giây** không thao tác. | Công tắc **tự bật lại**; giá lại cập nhật theo thời gian thực. | | |

---

## Bảng 4 – Open position, Positions, đa ngôn ngữ (NHMTS-714)

| ID | Nhóm | Mô tả | Điều kiện | Các bước | Kết quả mong đợi | Pass/Fail | Ghi chú |
|----|------|--------|-----------|----------|-------------------|-----------|---------|
| TC-714-16 | 714 | Block Open position & Unrealized PnL ngay dưới Bid/Ask | Có position với mã CK đang chọn | 1. Chọn mã CK mà tài khoản đang có position. 2. Xem khu vực **ngay dưới** bảng Bid/Ask, **trên** form đặt lệnh. | Thấy **Open position** (vd: +5 / -5 / 0) và **Unrealized PnL** (lãi/lỗ chưa thực hiện) của **đúng mã đang chọn**. | | |
| TC-714-17 | 714 | Tab Positions – danh sách vị thế | Có ít nhất một position | 1. Mở tab **Positions**. | Hiển thị danh sách: B/S (Mua/Bán), mã CK, số lượng, Unrealized PnL, giá vào, hành động (Reverse, Close). **Không** có cột TP/SL. | | |
| TC-714-18 | 714 | Đổi ngôn ngữ – thông báo lỗi đúng ngôn ngữ | App hỗ trợ đổi ngôn ngữ (vi/en/ko) | 1. Đổi ngôn ngữ app sang **Tiếng Anh**. 2. Gây lỗi (vd: số lượng vượt Max buy). | Thông báo lỗi hiển thị bằng **Tiếng Anh** (nội dung tương đương "Vượt quá sức mua khả dụng"). | | |

---

## Bảng 5 – Tổng hợp theo dõi (Summary)

| Issue | Số TC | Danh sách ID | Link Jira |
|-------|-------|--------------|-----------|
| NHMTS-645 (Max buy/Max sell) | 8 | TC-645-01 → TC-645-08 | [NHMTS-645](https://nhsv-vn.atlassian.net/browse/NHMTS-645) |
| NHMTS-714 (Màn Trade) | 18 | TC-714-01 → TC-714-18 | [NHMTS-714](https://nhsv-vn.atlassian.net/browse/NHMTS-714) |

**Theo dõi nhanh:** Dùng **Bảng theo dõi nhanh (Master Checklist)** ở đầu tài liệu để tick Pass/Fail, ghi Người test & Ngày test.  
**Chi tiết từng bước:** Dùng Bảng 1–4 bên dưới để xem Điều kiện, Các bước, Kết quả mong đợi đầy đủ.

**Cách đánh dấu:** ✅ Pass | ❌ Fail — Ghi rõ trong cột Ghi chú (build, thiết bị, ảnh) khi Fail.

---

**Document Status:** ✅ Ready for UAT  
**For:** Phòng nghiệp vụ (non-tech), QA  
**Next Steps:** Phân công tester, chạy test trên UAT; ghi lại Pass/Fail và ghi chú vào bảng; báo lỗi qua Jira (NHMTS-645, NHMTS-714).
