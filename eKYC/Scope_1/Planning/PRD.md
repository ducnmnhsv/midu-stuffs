# PRD: Nhật ký Hành trình Mở tài khoản eKYC (Scope 1)

**Phiên bản:** 4.0 | **Ngày:** 2026-07-20 | **Trạng thái:** Redesign đã đồng bộ tài liệu kỹ thuật — chờ chốt câu hỏi mục 8 | **Dành cho:** PM/BA, Ops, Compliance/Legal, Dev Lead

> Tài liệu này mô tả yêu cầu nghiệp vụ cho **Scope 1** — phần đang triển khai, thuần backend/DB, không có màn hình nào cho khách hàng hay Ops. Scope 2 (màn hình tra cứu, dashboard, MRZ, lưu ảnh) sẽ có PRD riêng khi mở lại scope. Lịch sử thay đổi chi tiết từng ngày xem `../../README.md` mục Review Log — tài liệu này chỉ phản ánh trạng thái hiện hành.

> ✅ **Đồng bộ tài liệu hoàn tất (2026-07-20):** Bản PRD này chốt lại **thiết kế mới** cho Scope 1 — gộp "nhật ký từng lần thử xác thực" (trước đây giữ lại **mọi** lần thử, kể cả thất bại, vĩnh viễn — xem `../../README.md` Review Log 2026-07-01(b), 2026-07-08(a)) và "nhật ký hành trình" thành **một luồng Journey Log duy nhất**; riêng bước xác thực khuôn mặt/CCCD (VNPT) giờ **chỉ ghi log khi thành công**. Đây là thay đổi thiết kế thật, đảo ngược quyết định append-only đã chốt trước đó cho sub-feature 01. `../Tech_Spec.md` và `../../README.md` **đã được đồng bộ theo thiết kế mới này** (xem `../../README.md` Review Log 2026-07-20(j), (k)) — còn lại câu hỏi mục 8 cần Compliance/Legal chốt trước khi implement.

---

## Tóm tắt

Hiện tại, khi khách hàng mở tài khoản qua eKYC, hệ thống **không giữ lại bằng chứng nào** về hành trình khách đã thao tác — dữ liệu bị xóa ngay khi khách thử lại. Hệ quả: Ops không trả lời được khách "tại sao tôi không mở được tài khoản", và công ty không có đủ bằng chứng khi cơ quan quản lý yêu cầu truy vết một hồ sơ.

Mục đích của Scope 1 là xây dựng **một nhật ký hành trình duy nhất** (Journey Log), ghi lại toàn bộ các bước khách đã thao tác trong một lần mở tài khoản — từ lúc nhập số điện thoại tới khi ký hợp đồng điện tử:

- **Hành trình thành công** → giữ lại vĩnh viễn toàn bộ các bước, phục vụ audit/truy vết.
- **Hành trình thất bại hoặc bỏ dở** → tự động xóa sau một khoảng thời gian không hoạt động, **trừ** bằng chứng đồng ý xử lý dữ liệu cá nhân (luôn giữ vĩnh viễn theo yêu cầu pháp luật).

Riêng bước xác thực khuôn mặt/CCCD qua đối tác VNPT: khách có thể phải thử lại nhiều lần trước khi đạt yêu cầu, nhưng hệ thống **chỉ ghi nhận log khi lần thử đó thành công** — không lưu lại các lần thử thất bại của riêng bước này. Vì mục tiêu của Scope 1 là bằng chứng hành trình mở tài khoản, không phải phân tích nguyên nhân/thống kê thất bại biometric.

Mọi tra cứu ở Scope 1 đều do Dev/DBA thực hiện thủ công khi cần điều tra case — chưa có màn hình admin.

---

## 1. Vấn đề hiện tại

| Vấn đề | Ảnh hưởng |
|---|---|
| Khi khách thử lại, hệ thống xóa luôn hồ sơ hành trình trước đó — không giữ dấu vết các bước đã đi qua | Không biết khách đã đi tới bước nào, dừng ở đâu, đã thao tác gì trước khi mở tài khoản thành công (hoặc bỏ dở) |
| Không ghi lại các bước khách đã đi qua trong hành trình mở tài khoản (nhập thông tin, xác thực OTP, quét CCCD/khuôn mặt, ký hợp đồng...) | Thiếu bằng chứng khi cơ quan quản lý (UBCK, NHNN) yêu cầu truy vết, hoặc khi khách khiếu nại về quy trình |
| Dữ liệu xác thực khuôn mặt/CCCD từ đối tác VNPT (khi thành công) lưu dạng thô, chưa gắn vào đúng bước hành trình | Khi cần xác minh 1 hồ sơ, không thấy được bối cảnh đầy đủ — kết quả VNPT tách rời khỏi các bước khác của hành trình |

---

## 2. Phạm vi Scope 1

| Có trong Scope 1 | Để sau (Scope 2) |
|---|---|
| Ghi lại toàn bộ hành trình mở tài khoản — từng bước, ngay khi xảy ra | Màn hình cho Ops tra cứu hành trình theo CCCD/SĐT |
| Ghi nhận kết quả xác thực khuôn mặt/CCCD (VNPT) khi verification thành công, gắn vào đúng bước hành trình | Dashboard thống kê tỉ lệ lỗi, hỗ trợ phát hiện gian lận |
| Đồng ý xử lý dữ liệu cá nhân trước khi thu thập CCCD/sinh trắc học — giữ vĩnh viễn | Kiểm tra chéo MRZ ngay tại App |
| Liên kết hành trình với tài khoản đã mở thành công | Lưu ảnh CCCD lên hạ tầng file, hiển thị lại trên màn hình tra cứu |

Scope 1 là một dự án thuần "hạ tầng dữ liệu" — không có màn hình mới nào cho khách hàng hay Ops. Mục tiêu là đảm bảo dữ liệu được ghi đúng, đủ, an toàn trước; việc hiển thị/khai thác dữ liệu để dành cho Scope 2.

---

## 3. Ai liên quan & được hưởng lợi khi nào

| Vai trò | Nhu cầu | Đáp ứng ở Scope 1 | Đáp ứng đầy đủ ở |
|---|---|---|---|
| Ops / CS | Tra cứu lý do khách không mở được tài khoản | Nhờ Dev/DBA tra giúp thủ công | Scope 2 — màn hình tự tra cứu |
| BA / PM | Phân tích tỉ lệ thất bại, đề xuất cải tiến UX | Chưa có | Scope 2 — Dashboard |
| Compliance / Legal | Có bằng chứng khi cơ quan quản lý yêu cầu | **Có** — hành trình thành công lưu đầy đủ dưới DB | — |
| Dev | Debug lỗi production | **Có** — tra cứu trực tiếp trong DB | Scope 2 — có màn hình |
| Security | Phát hiện dấu hiệu gian lận | Chưa có (đánh đổi đã ghi nhận, mục 4.3) | Cần mở lại scope riêng |

---

## 4. Yêu cầu nghiệp vụ

### 4.1 Ghi lại toàn bộ hành trình mở tài khoản

Mỗi lần khách thao tác mở tài khoản tạo ra một hành trình (session) riêng. Hệ thống ghi lại **từng bước** khách đã đi qua, **ngay khi bước đó xảy ra** — không chờ tới cuối hành trình.

**Hành trình gồm 11 bước**, từ lúc khách nhập số điện thoại tới khi hoàn tất gửi hồ sơ: đồng ý xử lý dữ liệu cá nhân (ngay bước đầu, trước khi thu thập CCCD/sinh trắc học) → gửi OTP → xác thực OTP → xem hướng dẫn CCCD → quét khuôn mặt & CCCD → điền thông tin cá nhân → thông tin tài khoản → thông tin ngân hàng → thông tin đầu tư → đồng ý điều khoản hợp đồng → hoàn tất mở tài khoản. Sau đó, **ký hợp đồng điện tử** được hệ thống tự động ghi nhận qua đối tác FPT — không cần khách đang mở app.

Mỗi lần ghi log đều kèm mã hành trình, thời điểm, và trạng thái thành công/thất bại của bước đó. Dữ liệu nghiệp vụ cụ thể ghi lại ở từng bước:

| # | Bước | Dữ liệu được ghi lại |
|---|---|---|
| 1 | Đồng ý xử lý dữ liệu cá nhân | Việc khách đã tick đồng ý, số điện thoại |
| 2 | Gửi OTP | Số điện thoại nhận OTP, thời điểm gửi, kết quả gửi (thành công/lỗi) |
| 3 | Xác thực OTP | Kết quả xác thực OTP (đúng/sai) |
| 4 | Xem hướng dẫn CCCD | Số điện thoại, email, quốc tịch, nghề nghiệp khách đã nhập |
| 5 | Quét khuôn mặt & CCCD (VNPT) — chỉ khi thành công | Toàn bộ kết quả VNPT trả về: thông tin đọc từ CCCD (họ tên, số CCCD, ngày sinh, quốc tịch, mã định danh chip...), ảnh mặt trước/sau CCCD, kết quả xác minh khuôn mặt, và các tín hiệu chống gian lận (chống ảnh giả/ảnh in, xác suất ảnh giả, đổi mặt/deepfake, phát hiện nhiều khuôn mặt, đối chiếu QR/chip với thông tin đọc từ mặt thẻ) — xem mục 4.2 |
| 6 | Xác nhận thông tin cá nhân | Ngày sinh, địa chỉ, thông tin FATCA và các thông tin cá nhân khác khách xác nhận |
| 7 | Thông tin tài khoản | Loại tài khoản, chi nhánh, các lựa chọn margin/phái sinh |
| 8 | Thông tin ngân hàng | Tài khoản ngân hàng nhận tiền khách khai |
| 9 | Thông tin đầu tư | Mục tiêu đầu tư, khẩu vị rủi ro khách chọn |
| 10 | Đồng ý điều khoản hợp đồng | Toàn văn nội dung điều khoản khách đã đọc và xác nhận đồng ý — không chỉ trạng thái đồng ý |
| 11 | Hoàn tất mở tài khoản | Xác nhận hồ sơ đã được hệ thống nhận đầy đủ — mốc chốt "thành công" (xem mục 4.3) |
| 12 | Ký hợp đồng điện tử (tự động qua FPT) | Xác nhận đã ký từ đối tác FPT (mã hợp đồng, thời điểm ký) — không qua App, xem mục 5 |

> Nguyên tắc chung: ghi lại **đúng những gì khách đã thấy/nhập/xác nhận tại bước đó**, không tóm tắt, không suy diễn thêm. Chi tiết field kỹ thuật cho từng bước: `../Tech_Spec.md` Section 2 và BE.5.

### 4.2 Xác thực khuôn mặt/CCCD (VNPT) — chỉ ghi nhận khi thành công

Ở bước quét khuôn mặt & CCCD, khách có thể phải thử lại nhiều lần trước khi đạt yêu cầu (ảnh mờ, không xác thực được khuôn mặt...). Khác với các bước còn lại của hành trình, hệ thống **chỉ ghi log cho bước này khi verification thành công** — các lần thử thất bại trước đó của riêng bước này **không được lưu lại**.

Khi verification thành công, hệ thống lưu đầy đủ dữ liệu VNPT trả về (không lọc bớt field), bao gồm các tín hiệu chống gian lận: chống giả mạo bằng ảnh in/ảnh chụp màn hình, xác suất ảnh giả, độ khớp khuôn mặt, quốc tịch, mã định danh chip trên CCCD, đối chiếu mã QR trên CCCD với thông tin đọc từ mặt thẻ, phát hiện ảnh có nhiều hơn một khuôn mặt, phát hiện đổi mặt/deepfake.

Lý do chỉ ghi nhận lần thử thành công: mục tiêu của Scope 1 là bằng chứng hành trình mở tài khoản, không phải phân tích nguyên nhân hay thống kê thất bại biometric — nên không cần giữ lại riêng lẻ các lần thử thất bại của bước này (đánh đổi liên quan, xem mục 4.3).

Việc lưu ảnh CCCD chính thức (lên hạ tầng lưu trữ file) để Scope 2, vì cần chuẩn bị hạ tầng riêng.

### 4.3 Quy tắc giữ / xóa dữ liệu hành trình

- Hành trình được coi là **"thành công"** ngay khi hồ sơ được hệ thống xác nhận đã nhận — việc ký hợp đồng hay nhân viên duyệt hồ sơ sau đó **không ảnh hưởng** tới việc xác định "thành công" này. Vì vậy nếu nhân viên duyệt hồ sơ và từ chối, nhật ký hành trình vẫn được giữ lại vĩnh viễn theo đúng định nghĩa này.
- **Thành công → giữ vĩnh viễn.** Không thành công hoặc bỏ dở quá **8 giờ** không hoạt động → **xóa toàn bộ**.
- **Ngoại lệ:** bằng chứng đồng ý xử lý dữ liệu cá nhân (bước đầu) luôn được **giữ vĩnh viễn**, dù hành trình sau đó có thành công hay không — đây là yêu cầu bắt buộc theo pháp luật bảo vệ dữ liệu cá nhân (mục 6).

**Đánh đổi đã ghi nhận:** vì hành trình không thành công bị xóa, và các lần thử VNPT thất bại không được lưu lại, hai mong muốn ban đầu — phân tích khách thường "kẹt" ở bước nào, và phát hiện gian lận từ hành vi lặp lại — **không thực hiện được** với thiết kế này. PM đã chấp nhận đánh đổi này để đơn giản hóa lưu trữ ở giai đoạn này; nếu cần lại, phải mở scope riêng.

### 4.4 Liên kết dữ liệu khi tài khoản mở thành công

Khi khách mở tài khoản thành công, hệ thống liên kết các hành trình trước đó (nếu khách có thử lại — thất bại rồi mới thành công, trong vòng 8 giờ trước khi bị xóa) về tài khoản đó — giúp khi tra cứu, thấy được toàn bộ lịch sử dẫn tới việc mở tài khoản thành công.

---

## 5. Ai ghi dữ liệu, và ghi ở đâu

Việc ghi log được thực hiện **hoàn toàn tự động phía backend và App** — hai hệ thống backend cùng tham gia (một xử lý xác thực/OTP, một xử lý gửi hồ sơ sang đối tác), cần thống nhất cách ghi log nhất quán với nhau.

Toàn bộ dữ liệu Scope 1 chỉ tồn tại trong database, **không hiển thị lên bất kỳ màn hình nào**. Khi cần điều tra case, Dev/Ops tra cứu thủ công, không qua giao diện.

---

## 6. Vì sao cần "đồng ý xử lý dữ liệu cá nhân" trước khi thu thập CCCD/sinh trắc học

Pháp luật Việt Nam về bảo vệ dữ liệu cá nhân (Nghị định 13/2023/NĐ-CP) yêu cầu: phải **xin đồng ý trước khi** thu thập/xử lý dữ liệu cá nhân, đặc biệt với dữ liệu nhạy cảm như sinh trắc học và CCCD; phải **lưu được bằng chứng** đã xin và nhận được đồng ý đó; và chỉ được **giữ dữ liệu trong thời gian có mục đích hợp lý**.

Ba yêu cầu này là lý do cho 3 quyết định trong mục 4.3: (1) bước đồng ý xử lý dữ liệu cá nhân đặt **đầu tiên** trong hành trình, trước khi thu thập bất kỳ dữ liệu CCCD/sinh trắc học nào; (2) bằng chứng đồng ý được **giữ vĩnh viễn**, không theo quy tắc xóa chung của hành trình; (3) hành trình không thành công phải xóa vì không còn mục đích xử lý hợp lý để giữ lại.

Việc BE lưu trữ dữ liệu nhạy cảm (bao gồm ảnh sinh trắc học) có đáp ứng đầy đủ Nghị định 13/2023 hay không **vẫn cần Compliance/Legal rà soát trước khi go-live** (câu hỏi mở, xem mục 8).

---

## 7. Ngoài phạm vi (Scope 2 và các giai đoạn sau)

**Scope 2 — đã có demo/thiết kế sẵn, chờ mở lại scope:** màn hình Ops tra cứu hành trình theo CCCD/SĐT, Dashboard thống kê, kiểm tra chéo MRZ tại App, lưu ảnh CCCD lên hạ tầng file.

**Chưa có lịch trình:** cảnh báo tự động khi phát hiện gian lận, tự động điều chỉnh ngưỡng khớp khuôn mặt, xuất báo cáo từ Dashboard, liên kết hành trình với hệ thống khác.

---

## 8. Câu hỏi còn mở

Câu hỏi cốt lõi vẫn là: **việc lưu vĩnh viễn dữ liệu nhạy cảm (CCCD, SĐT, IP, ảnh sinh trắc học) của hành trình thành công có tuân thủ đầy đủ Nghị định 13/2023/NĐ-CP không?** Dưới đây là sơ bộ đánh giá để Compliance/Legal rà soát nhanh hơn — **đây không phải kết luận pháp lý chính thức**, chỉ là góc nhìn PM/BA dựa trên thiết kế hiện tại.

**Điểm thiết kế đã có căn cứ hợp lý:**
- Lưu vĩnh viễn hồ sơ hành trình **thành công** không phải lưu tùy tiện — hồ sơ nhận biết khách hàng (KYC) khi mở tài khoản chứng khoán thường thuộc diện phải lưu trữ theo pháp luật chuyên ngành (Luật Chứng khoán, quy định phòng chống rửa tiền), tạo mục đích/căn cứ hợp lý cho việc lưu trữ lâu dài — khác với giữ dữ liệu không có mục đích rõ ràng.
- Đồng ý xử lý dữ liệu cá nhân được xin **đúng lúc** (trước khi thu thập CCCD/sinh trắc học) và **lưu bằng chứng vĩnh viễn** — đáp ứng yêu cầu về xin đồng ý và lưu bằng chứng đồng ý.
- Hành trình **không thành công bị xóa** sau 8 giờ — đáp ứng nguyên tắc chỉ giữ dữ liệu trong thời gian có mục đích hợp lý.

**Điểm cần Compliance/Legal xác nhận cụ thể trước khi go-live:**

| # | Câu hỏi | Ai quyết định |
|---|---|---|
| 1 | Dữ liệu sinh trắc học + CCCD thuộc "dữ liệu cá nhân nhạy cảm" theo Nghị định 13/2023 — có cần lập hồ sơ đánh giá tác động xử lý dữ liệu cá nhân nhạy cảm trước khi triển khai không, và nếu có thì ai chịu trách nhiệm lập? | Compliance/Legal |
| 2 | Biện pháp bảo mật kỹ thuật hiện tại (payload lưu nguyên văn, không mã hoá field) có đáp ứng yêu cầu "biện pháp bảo vệ phù hợp" với dữ liệu nhạy cảm hay không, hay cần thêm mã hoá/kiểm soát truy cập chặt hơn riêng cho dữ liệu này? | Compliance/Legal + Dev Lead |
| 3 | "Giữ vĩnh viễn" có cần gắn với một thời hạn cụ thể (theo thời hạn lưu trữ hồ sơ KYC/phòng chống rửa tiền luật định) kèm cơ chế xóa sau khi hết nghĩa vụ lưu trữ, thay vì vĩnh viễn không giới hạn? | Compliance/Legal |
| 4 | Khách hàng có quyền yêu cầu xóa dữ liệu cá nhân theo Nghị định 13 — quyền này có bị giới hạn bởi nghĩa vụ lưu trữ hồ sơ luật định (AML/chứng khoán) đối với hồ sơ đã mở tài khoản thành công không, và nếu khách yêu cầu xóa thì xử lý thế nào? | Compliance/Legal |
| 5 | Mục đích cụ thể của việc lưu địa chỉ IP cùng hồ sơ (VD phục vụ điều tra gian lận/an ninh) cần nêu rõ trong nội dung thông báo xử lý dữ liệu cho khách hàng — nội dung thông báo hiện tại đã đủ chưa? | Compliance/Legal |

Các câu hỏi khác (ngưỡng thời gian "bỏ dở", khóa nhận diện bước ký hợp đồng, phạm vi các bước cần log, ngoại lệ giữ vĩnh viễn bằng chứng consent) đã được chốt — xem `../../README.md` Review Log để biết chi tiết quyết định và ngày chốt.

---

## 9. Định nghĩa hoàn thành

**Scope 1 hoàn thành khi:**
- Toàn bộ 11 bước trong hành trình mở tài khoản được ghi lại real-time, liên kết được với nhau qua cùng một hành trình.
- Bước xác thực khuôn mặt/CCCD chỉ ghi log khi VNPT xác thực thành công, kèm đầy đủ dữ liệu VNPT (bao gồm các tín hiệu chống gian lận).
- Khách không thể qua màn hình đầu tiên của eKYC nếu chưa đồng ý xử lý dữ liệu cá nhân; bằng chứng đồng ý được giữ vĩnh viễn dù hành trình sau đó có thành công hay không.
- Hành trình thành công được giữ vĩnh viễn; thất bại/bỏ dở quá 8 giờ được xóa đúng quy tắc.
- Dev/DBA tra cứu được toàn bộ lịch sử một hành trình trong vài phút khi cần điều tra, không cần màn hình admin.

**Scope 2 (bổ sung sau):** Ops tự tra cứu lý do thất bại qua màn hình admin; xem được ảnh CCCD của hành trình thành công; Dashboard hiển thị tỉ lệ thất bại theo nguyên nhân.

---

## 10. Trách nhiệm FE vs BE — tổng quan

| Hạng mục | FE (App nhsv-mts-rn) | BE (ekyc-admin) |
|---|---|---|
| Hành trình mở tài khoản | Gọi API ghi log tại từng bước, ngay khi khách hoàn tất bước đó, gửi đầy đủ dữ liệu khách đã nhập | Nhận và lưu real-time; tự xóa hành trình không thành công sau 8h; tự ghi bước ký hợp đồng qua webhook FPT |
| Xác thực khuôn mặt/CCCD (VNPT) | Chỉ gọi API ghi log khi verification **thành công**, kèm toàn bộ dữ liệu SDK VNPT trả về | Nhận và lưu dữ liệu VNPT gắn vào đúng bước hành trình; tự liên kết các hành trình trước đó về tài khoản khi mở thành công |
| Đồng ý xử lý dữ liệu cá nhân (bước đầu hành trình) | Xây màn hình mới: checkbox + chặn nút "Tiếp theo" tới khi khách tick | Lưu bằng chứng consent vĩnh viễn, không theo quy tắc xóa chung của hành trình |
| Mất kết nối mạng giữa chừng khi gọi log | Tự động thử lại, không bỏ qua vĩnh viễn | — |

---

## 11. FE cần làm (App — nhsv-mts-rn)

### 11.1 Đồng ý xử lý dữ liệu cá nhân

- Xây **1 màn hình mới**: hiển thị ngay sau khi khách nhập số điện thoại, trước khi gửi OTP; nút "Tiếp theo" ở trạng thái disabled tới khi khách tick checkbox.
- Gọi API ghi log ngay khi khách tick — đây là bước đầu tiên của hành trình.

### 11.2 Ghi log các bước còn lại của hành trình

- Duy trì **1 mã hành trình** (session) xuyên suốt từ bước đầu tới bước cuối, không đổi giữa chừng.
- Tại các bước còn lại (gửi OTP, xác thực OTP, xem hướng dẫn CCCD, điền thông tin cá nhân/tài khoản/ngân hàng/đầu tư, đồng ý điều khoản hợp đồng, hoàn tất mở tài khoản): gọi API ghi log ngay khi khách hoàn tất bước đó, **kể cả khi bước đó thất bại** (ví dụ hồ sơ bị hệ thống đối tác từ chối).
- Không cần xử lý gì thêm cho bước ký hợp đồng điện tử — BE tự ghi nhận qua đối tác FPT.

### 11.3 Xác thực khuôn mặt & CCCD (VNPT)

- Chỉ gọi API ghi log cho bước này khi VNPT xác thực **thành công** — không gọi log cho các lần thử thất bại trước đó của bước này, dù khách có thể phải quét lại nhiều lần.
- Khi thành công, gửi kèm toàn bộ dữ liệu SDK VNPT trả về (không lọc bớt field) để BE lưu phục vụ audit/điều tra sau này.

### 11.4 Yêu cầu chung

- Nếu request ghi log thất bại do **mất kết nối mạng**, App phải tự động thử lại, không bỏ qua vĩnh viễn — đảm bảo không mất dữ liệu audit/compliance.
- Việc ghi log là instrumentation nền — khách hàng không thấy, không bị ảnh hưởng nếu việc ghi log gặp lỗi; không được làm chậm hoặc chặn luồng chính mở tài khoản.

> Chi tiết đầy đủ (API, request/response, business rule, Acceptance Criteria): `../Tech_Spec.md` — Phần FE.

---

## 12. BE cần làm (ekyc-admin)

- Tạo 1 bảng lưu nhật ký hành trình, ghi **real-time** từng bước ngay khi App gọi lên — không chờ tới cuối hành trình.
- Xây **1 API duy nhất** nhận log hành trình từ App, dùng chung cho toàn bộ các bước — không cần API hay bảng riêng cho "lần thử xác thực" như thiết kế trước đây.
- Khi nhận log bước xác thực khuôn mặt/CCCD: lưu đầy đủ dữ liệu SDK VNPT gửi lên, kể cả field chưa từng dùng tới trong nghiệp vụ hiện tại (phục vụ audit/điều tra sau này). Chỉ nhận log này khi App gửi lên kết quả **thành công**.
- Tự động ghi nhận bước ký hợp đồng điện tử qua webhook của đối tác FPT — không qua App, không phụ thuộc App có đang mở hay không.
- Khi tài khoản mở thành công: tự động liên kết các hành trình trước đó (nếu có fail rồi mới thành công, trong vòng 8h) về tài khoản đó.
- Xây cơ chế tự động **xóa** hành trình không đạt mốc "thành công" sau 8 giờ không hoạt động — **trừ** bằng chứng đồng ý xử lý dữ liệu cá nhân, luôn giữ vĩnh viễn.
- Công cụ nội bộ (không phải màn hình sống, không public) để Dev/Compliance tra soát 1 hành trình cụ thể khi có yêu cầu audit.

> Chi tiết đầy đủ (schema, API, code mẫu): `../Tech_Spec.md` — Phần BE.

---

## Tài liệu tham chiếu (chi tiết kỹ thuật)

Tài liệu này tập trung vào yêu cầu nghiệp vụ, không đi sâu kỹ thuật. Chi tiết database, API, mapping field — xem trong cùng thư mục `eKYC/Scope_1/` (✅ đã đồng bộ theo mục 4 của PRD này, 2026-07-20):

- **../Tech_Spec.md** — thiết kế kỹ thuật đầy đủ cho cả FE và BE (schema, API, payload reference, task, acceptance criteria) — **1 file duy nhất** (gộp từ `Specifications/BE_Spec.md` + `Issues/BE_Issue.md` + `Issues/FE_Issue.md` cũ ngày 2026-07-20, theo yêu cầu PO giảm phân mảnh), có "Phần FE" và "Phần BE" riêng biệt trong cùng file
- **../demos/Overview.html** — tổng hợp trực quan cho ai muốn xem nhanh — ⚠️ chưa được cập nhật trong lần sync này, vẫn phản ánh thiết kế cũ (2 luồng riêng + 3 file kỹ thuật riêng)
- **../../README.md** — cấu trúc toàn bộ tài liệu eKYC, trạng thái từng phần, lịch sử quyết định (xem Review Log 2026-07-20(j), (k), (l))

---

**Bước tiếp theo:** ~~Đồng bộ lại `BE_Spec.md`, `BE_Issue.md`, `FE_Issue.md`, `README.md` theo thiết kế mới ở mục 4~~ (hoàn tất 2026-07-20) → ~~Gộp 3 file kỹ thuật thành `Tech_Spec.md`~~ (hoàn tất 2026-07-20) → Chốt câu hỏi ở mục 8 → FE Lead + BE Lead đọc `Tech_Spec.md` → estimate effort → lên kế hoạch Sprint. Scope 2 chờ mở lại sau khi Scope 1 go-live.
