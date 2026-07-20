# PRD: Ghi nhận Lịch sử Xác thực & Nhật ký Hành trình Mở tài khoản eKYC (Scope 1)

**Phiên bản:** 3.0 | **Ngày:** 2026-07-20 | **Trạng thái:** Đã chốt scope Scope 1 | **Dành cho:** PM/BA, Ops, Compliance/Legal, Dev Lead

> Tài liệu này mô tả yêu cầu nghiệp vụ cho **Scope 1** — phần đang triển khai, thuần backend/DB, không có màn hình nào cho khách hàng hay Ops. Scope 2 (màn hình tra cứu, dashboard, MRZ, lưu ảnh) sẽ có PRD riêng khi mở lại scope. Lịch sử thay đổi chi tiết từng ngày xem `../../README.md` mục Review Log — tài liệu này chỉ phản ánh trạng thái hiện hành.

---

## Tóm tắt

Hiện tại, khi khách hàng mở tài khoản qua eKYC nhưng thất bại giữa chừng (ảnh mờ, không xác thực được khuôn mặt, hồ sơ bị từ chối...), hệ thống **không giữ lại bằng chứng nào** — dữ liệu bị xóa ngay khi khách thử lại. Hệ quả: Ops không trả lời được khách "tại sao tôi không mở được tài khoản", và công ty không có đủ bằng chứng khi cơ quan quản lý yêu cầu truy vết một hồ sơ.

Scope 1 xây dựng **hai loại nhật ký lưu dưới database**, phục vụ audit/truy vết — chưa có màn hình nào để xem trực tiếp:

1. **Nhật ký từng lần thử xác thực khuôn mặt/giấy tờ** — giữ lại mọi lần thử, kể cả thất bại, vĩnh viễn.
2. **Nhật ký hành trình mở tài khoản** — ghi lại các bước khách đã đi qua trong một lần mở tài khoản, nhưng **chỉ giữ lại nếu khách mở tài khoản thành công**; nếu không, dữ liệu tự động bị xóa.

Mọi tra cứu ở Scope 1 đều do Dev/DBA thực hiện thủ công khi cần điều tra case — chưa có màn hình admin.

---

## 1. Vấn đề hiện tại

| Vấn đề | Ảnh hưởng |
|---|---|
| Mỗi khi khách thử lại, hệ thống xóa luôn hồ sơ thử trước đó | Mất toàn bộ lịch sử — không biết khách đã fail bao nhiêu lần, vì lý do gì |
| Khách fail ngay từ bước quét giấy tờ/khuôn mặt (trước khi hồ sơ được gửi lên hệ thống) — thông tin bước đó không được gửi về server | Không có dữ liệu để tra khi khách khiếu nại |
| Dữ liệu xác thực từ đối tác VNPT lưu dạng thô, chưa được sắp xếp | Không tra cứu/thống kê được theo nguyên nhân fail hay dấu hiệu gian lận |
| Không ghi lại các bước khách đã đi qua trong hành trình mở tài khoản | Thiếu bằng chứng khi cơ quan quản lý (UBCK, NHNN) yêu cầu truy vết, hoặc khi khách khiếu nại về quy trình |

---

## 2. Phạm vi Scope 1

| Có trong Scope 1 | Để sau (Scope 2) |
|---|---|
| Lưu lại đầy đủ mọi lần thử xác thực (thành công và thất bại) | Màn hình cho Ops tra cứu lịch sử theo CCCD/SĐT |
| Ghi lại toàn bộ hành trình mở tài khoản, phục vụ compliance/audit | Dashboard thống kê tỉ lệ lỗi, hỗ trợ phát hiện gian lận |
| Đồng ý xử lý dữ liệu cá nhân trước khi thu thập CCCD/sinh trắc học | Kiểm tra chéo MRZ ngay tại App |
| Liên kết các lần thử trước đó với tài khoản đã mở thành công | Lưu ảnh CCCD lên hạ tầng file, hiển thị lại trên màn hình tra cứu |

Scope 1 là một dự án thuần "hạ tầng dữ liệu" — không có màn hình mới nào cho khách hàng hay Ops. Mục tiêu là đảm bảo dữ liệu được ghi đúng, đủ, an toàn trước; việc hiển thị/khai thác dữ liệu để dành cho Scope 2.

---

## 3. Ai liên quan & được hưởng lợi khi nào

| Vai trò | Nhu cầu | Đáp ứng ở Scope 1 | Đáp ứng đầy đủ ở |
|---|---|---|---|
| Ops / CS | Tra cứu lý do khách không mở được tài khoản | Nhờ Dev/DBA tra giúp thủ công | Scope 2 — màn hình tự tra cứu |
| BA / PM | Phân tích tỉ lệ thất bại, đề xuất cải tiến UX | Chưa có | Scope 2 — Dashboard |
| Compliance / Legal | Có bằng chứng khi cơ quan quản lý yêu cầu | **Có** — dữ liệu lưu đầy đủ dưới DB | — |
| Dev | Debug lỗi production | **Có** — tra cứu trực tiếp trong DB | Scope 2 — có màn hình |
| Security | Phát hiện dấu hiệu gian lận | Chưa có (đánh đổi đã ghi nhận, mục 5) | Cần mở lại scope riêng |

---

## 4. Yêu cầu nghiệp vụ

### 4.1 Lưu lại mọi lần thử xác thực — không bao giờ xóa

Mỗi lần khách thử mở tài khoản (thành công hoặc thất bại) tạo ra một bản ghi riêng, đánh số thứ tự lần thử. Bản ghi **không bao giờ bị xóa hoặc sửa** sau khi tạo — kể cả khi khách thử lại nhiều lần.

Một lần thử được ghi nhận ở 1 trong 2 thời điểm: **thất bại sớm** (quét giấy tờ/khuôn mặt không đạt, trước khi hồ sơ được gửi lên hệ thống chính), hoặc **thất bại muộn/thành công** (sau khi hồ sơ đã gửi lên — bị từ chối hoặc được chấp nhận).

Cách xử lý hồ sơ hiện tại khi khách thử lại **không đổi** — thay đổi này chỉ thêm một lớp ghi log song song, không ảnh hưởng trải nghiệm khách hàng.

### 4.2 Ghi nhận đầy đủ kết quả xác thực từ đối tác VNPT

Một số kết quả VNPT trả về hiện chỉ được App dùng để kiểm tra ngay trên điện thoại, chưa gửi về hệ thống lưu trữ — ví dụ: kết quả chống giả mạo bằng ảnh in/ảnh chụp màn hình, xác suất ảnh giả, độ khớp khuôn mặt, quốc tịch, mã định danh chip trên CCCD. Đây là dữ liệu quan trọng nhất để biết *tại sao* một lần thử thất bại và phát hiện gian lận.

Ba tín hiệu chống gian lận cần bổ sung: đối chiếu mã QR trên CCCD với thông tin đọc từ mặt thẻ; phát hiện ảnh có nhiều hơn một khuôn mặt; phát hiện đổi mặt/deepfake trên ảnh CCCD.

Việc lưu ảnh CCCD chính thức (lên hạ tầng lưu trữ file) để Scope 2, vì cần chuẩn bị hạ tầng riêng.

### 4.3 Phân loại rõ nguyên nhân của mỗi lần thử

| Kết quả | Ý nghĩa |
|---|---|
| Xác thực VNPT thất bại | Lỗi ở bước đọc giấy tờ, khuôn mặt sống, chất lượng ảnh, hoặc dấu hiệu giả mạo |
| So khớp khuôn mặt thất bại | Độ khớp giữa ảnh selfie và ảnh CCCD dưới ngưỡng cho phép |
| Hệ thống Lotte từ chối | Hồ sơ đã gửi lên nhưng bị hệ thống đối tác từ chối |
| Khách bỏ dở | Hồ sơ được chấp nhận nhưng khách không ký hợp đồng điện tử trong 48 giờ |
| Thành công | Tài khoản được mở thành công |

### 4.4 Nhật ký Hành trình mở tài khoản

Ngoài kết quả xác thực khuôn mặt/giấy tờ (mục 4.1–4.3), công ty cần ghi lại **toàn bộ các bước khách đã đi qua** trong một lần mở tài khoản. Mục đích: bằng chứng đầy đủ khi cơ quan quản lý (UBCK, NHNN) yêu cầu truy vết, hoặc khi khách khiếu nại.

**Hành trình gồm 11 bước**, từ lúc khách nhập số điện thoại tới khi ký hợp đồng điện tử — bao gồm: đồng ý xử lý dữ liệu cá nhân (ngay bước đầu, trước khi thu thập CCCD/sinh trắc học), xác thực OTP, quét CCCD và khuôn mặt, điền các thông tin còn thiếu, đồng ý điều khoản hợp đồng, gửi hồ sơ, và ký hợp đồng điện tử. Ký hợp đồng do hệ thống tự ghi nhận qua đối tác FPT, không cần khách đang mở app. Danh sách đầy đủ từng bước: xem mục 11.2 (FE cần làm).

**Nguyên tắc lưu trữ:**

- Ghi lại từng bước **ngay khi nó xảy ra**, không chờ tới cuối hành trình.
- Hành trình được coi là **"thành công"** ngay khi hồ sơ được hệ thống xác nhận đã nhận — việc ký hợp đồng hay nhân viên duyệt hồ sơ sau đó **không ảnh hưởng** tới việc xác định "thành công" này. Vì vậy nếu nhân viên duyệt hồ sơ và từ chối, nhật ký hành trình vẫn được giữ lại vĩnh viễn theo đúng định nghĩa này.
- **Thành công → giữ vĩnh viễn.** Không thành công hoặc bỏ dở quá **8 giờ** không hoạt động → **xóa toàn bộ**.
- **Ngoại lệ:** bằng chứng đồng ý xử lý dữ liệu cá nhân (bước đầu) luôn được **giữ vĩnh viễn**, dù hành trình sau đó có thành công hay không — đây là yêu cầu bắt buộc theo pháp luật bảo vệ dữ liệu cá nhân (mục 6).

**Đánh đổi đã ghi nhận:** vì hành trình không thành công bị xóa, hai mong muốn ban đầu — phân tích khách thường "kẹt" ở bước nào, và phát hiện gian lận từ hành vi lặp lại — **không thực hiện được** với thiết kế này. PM đã chấp nhận đánh đổi này để đơn giản hóa lưu trữ ở giai đoạn này; nếu cần lại, phải mở scope riêng.

**Vì sao tách riêng khỏi nhật ký lần thử (mục 4.1)?** Nhật ký lần thử phải giữ **mọi** lần thử vĩnh viễn; nhật ký hành trình phải **xóa** hành trình không thành công. Hai quy tắc ngược nhau nên phải lưu ở hai nơi tách biệt, tránh việc xóa của một loại vô tình xóa luôn dữ liệu loại kia.

### 4.5 Liên kết dữ liệu khi tài khoản mở thành công

Khi khách mở tài khoản thành công, hệ thống liên kết toàn bộ các lần thử trước đó (nếu có fail rồi mới thành công) về tài khoản đó — giúp khi tra cứu, thấy được toàn bộ lịch sử dẫn tới việc mở tài khoản thành công.

---

## 5. Ai ghi dữ liệu, và ghi ở đâu

Việc ghi log được thực hiện **hoàn toàn tự động phía backend và App** — hai hệ thống backend cùng tham gia (một xử lý xác thực/OTP, một xử lý gửi hồ sơ sang đối tác), cần thống nhất cách ghi log nhất quán với nhau.

Toàn bộ dữ liệu Scope 1 chỉ tồn tại trong database, **không hiển thị lên bất kỳ màn hình nào**. Khi cần điều tra case, Dev/Ops tra cứu thủ công, không qua giao diện.

---

## 6. Vì sao cần "đồng ý xử lý dữ liệu cá nhân" trước khi thu thập CCCD/sinh trắc học

Pháp luật Việt Nam về bảo vệ dữ liệu cá nhân (Nghị định 13/2023/NĐ-CP) yêu cầu: phải **xin đồng ý trước khi** thu thập/xử lý dữ liệu cá nhân, đặc biệt với dữ liệu nhạy cảm như sinh trắc học và CCCD; phải **lưu được bằng chứng** đã xin và nhận được đồng ý đó; và chỉ được **giữ dữ liệu trong thời gian có mục đích hợp lý**.

Ba yêu cầu này là lý do cho 3 quyết định trong mục 4.4: (1) bước đồng ý xử lý dữ liệu cá nhân đặt **đầu tiên** trong hành trình, trước khi thu thập bất kỳ dữ liệu CCCD/sinh trắc học nào; (2) bằng chứng đồng ý được **giữ vĩnh viễn**, không theo quy tắc xóa chung của hành trình; (3) hành trình không thành công phải xóa vì không còn mục đích xử lý hợp lý để giữ lại.

Việc BE lưu trữ dữ liệu nhạy cảm (bao gồm ảnh sinh trắc học) có đáp ứng đầy đủ Nghị định 13/2023 hay không **vẫn cần Compliance/Legal rà soát trước khi go-live** (câu hỏi mở, xem mục 8).

---

## 7. Ngoài phạm vi (Scope 2 và các giai đoạn sau)

**Scope 2 — đã có demo/thiết kế sẵn, chờ mở lại scope:** màn hình Ops tra cứu lịch sử theo CCCD/SĐT, Dashboard thống kê, kiểm tra chéo MRZ tại App, lưu ảnh CCCD lên hạ tầng file.

**Chưa có lịch trình:** cảnh báo tự động khi phát hiện gian lận, tự động điều chỉnh ngưỡng khớp khuôn mặt, xuất báo cáo từ Dashboard, liên kết hành trình với hệ thống khác.

---

## 8. Câu hỏi còn mở

| # | Câu hỏi | Ai quyết định |
|---|---|---|
| 1 | Việc lưu vĩnh viễn dữ liệu nhạy cảm (CCCD, SĐT, IP, ảnh sinh trắc học) của hồ sơ thành công có tuân thủ đầy đủ Nghị định 13/2023 không? | Compliance/Legal |

Các câu hỏi khác (ngưỡng thời gian "bỏ dở", khóa nhận diện bước ký hợp đồng, phạm vi các bước cần log, ngoại lệ giữ vĩnh viễn bằng chứng consent) đã được chốt — xem `../../README.md` Review Log để biết chi tiết quyết định và ngày chốt.

---

## 9. Định nghĩa hoàn thành

**Scope 1 hoàn thành khi:**
- Mọi lần thử mở tài khoản (thành công và thất bại) được ghi lại đầy đủ, không mất khi khách thử lại.
- Khách không thể qua màn hình đầu tiên của eKYC nếu chưa đồng ý xử lý dữ liệu cá nhân; bằng chứng đồng ý được giữ vĩnh viễn.
- Toàn bộ các bước trong hành trình mở tài khoản được ghi lại, liên kết được với nhau qua cùng một hồ sơ.
- Nhật ký hành trình của trường hợp thành công được giữ vĩnh viễn; thất bại/bỏ dở được xóa đúng quy tắc.
- Dev/DBA tra cứu được toàn bộ lịch sử một hồ sơ trong vài phút khi cần điều tra, không cần màn hình admin.

**Scope 2 (bổ sung sau):** Ops tự tra cứu lý do thất bại qua màn hình admin; xem được ảnh CCCD từng lần thử; Dashboard hiển thị tỉ lệ thất bại theo nguyên nhân.

---

## 10. Trách nhiệm FE vs BE — tổng quan

| Hạng mục | FE (App nhsv-mts-rn) | BE (ekyc-admin) |
|---|---|---|
| Lần thử xác thực khuôn mặt/giấy tờ (sub-feature 01) | Gọi API ghi log ngay khi 1 lần thử fail (trước khi nộp hồ sơ) hoặc ngay sau khi nộp hồ sơ (thành công/bị từ chối), gửi kèm toàn bộ dữ liệu SDK VNPT | Nhận và lưu vĩnh viễn từng lần thử; tự liên kết về tài khoản khi mở thành công |
| Hành trình mở tài khoản (sub-feature 07) | Gọi API ghi log tại 11 bước của hành trình, gửi đầy đủ dữ liệu khách đã nhập ở đúng bước đó | Nhận và lưu real-time; tự xóa hành trình không thành công sau 8h; tự ghi bước ký hợp đồng qua webhook FPT |
| Đồng ý xử lý dữ liệu cá nhân (bước đầu hành trình) | Xây màn hình mới: checkbox + chặn nút "Tiếp theo" tới khi khách tick | Lưu bằng chứng consent vĩnh viễn, không theo quy tắc xóa chung của hành trình |
| Mất kết nối mạng giữa chừng khi gọi log | Tự động thử lại, không bỏ qua vĩnh viễn | — |

---

## 11. FE cần làm (App — nhsv-mts-rn)

### 11.1 Sub-feature 01 — Biometric Attempt Log

- Khi 1 lần thử xác thực **thất bại trước khi gửi hồ sơ** (CCCD/khuôn mặt không đạt): gọi ngay API ghi log lần thử, kèm ảnh CCCD nếu thất bại ở bước đọc giấy tờ.
- Khi 1 lần thử **đã gửi hồ sơ** (thành công hoặc bị hệ thống đối tác từ chối): gọi thêm API ghi log lần thử, kèm toàn bộ dữ liệu xác thực SDK VNPT trả về (không lọc bớt field).
- Việc gọi log không được làm chậm hoặc chặn luồng chính mở tài khoản.

### 11.2 Sub-feature 07 — Compliance Journey Log

- Xây **1 màn hình mới**: đồng ý xử lý dữ liệu cá nhân — hiển thị ngay sau khi khách nhập số điện thoại, trước khi gửi OTP; nút "Tiếp theo" ở trạng thái disabled tới khi khách tick checkbox.
- Tại đủ **11 bước** của hành trình mở tài khoản (đồng ý xử lý dữ liệu cá nhân → gửi OTP → xác thực OTP → xem hướng dẫn CCCD → quét khuôn mặt & CCCD → thông tin cá nhân → thông tin tài khoản → thông tin ngân hàng → thông tin đầu tư → đồng ý điều khoản hợp đồng → hoàn tất mở tài khoản): gọi API ghi log ngay khi khách hoàn tất bước đó, **kể cả khi bước đó thất bại**.
- Duy trì **1 mã hành trình** (session) xuyên suốt từ bước đầu tới bước cuối, không đổi giữa chừng.
- Không cần xử lý gì thêm cho bước ký hợp đồng điện tử — BE tự ghi nhận qua đối tác FPT.

### 11.3 Áp dụng cho cả 2 sub-feature

- Nếu request ghi log thất bại do **mất kết nối mạng**, App phải tự động thử lại, không bỏ qua vĩnh viễn — đảm bảo không mất dữ liệu audit/compliance.
- Việc ghi log là instrumentation nền — khách hàng không thấy, không bị ảnh hưởng nếu việc ghi log gặp lỗi.

> Chi tiết đầy đủ (API, request/response, business rule, Acceptance Criteria): `../Issues/FE_Issue.md`.

---

## 12. BE cần làm (ekyc-admin)

### 12.1 Sub-feature 01 — Biometric Attempt Log

- Tạo 1 bảng lưu lịch sử lần thử — **append-only**, không cho phép update hay xóa.
- Xây API nhận log lần thử từ App, lưu đầy đủ dữ liệu SDK gửi lên, kể cả field chưa từng dùng tới trong nghiệp vụ hiện tại (phục vụ audit/điều tra sau này).
- Khi tài khoản mở thành công: tự động liên kết toàn bộ các lần thử trước đó (nếu có fail rồi mới thành công) về tài khoản đó.
- Khi hồ sơ bị đối tác từ chối: tự động cập nhật kết quả của lần thử tương ứng.
- Chuẩn bị sẵn API tra cứu/tìm kiếm theo CCCD/SĐT — phục vụ màn hình Admin ở Scope 2, dù Scope 1 chưa có màn hình nào dùng tới.

### 12.2 Sub-feature 07 — Compliance Journey Log

- Tạo 1 bảng lưu nhật ký hành trình, ghi **real-time** từng bước ngay khi App gọi lên — không chờ tới cuối hành trình.
- Xây API nhận log hành trình từ App, dùng chung cho cả 11 bước.
- Tự động ghi nhận bước ký hợp đồng điện tử qua webhook của đối tác FPT — không qua App, không phụ thuộc App có đang mở hay không.
- Xây cơ chế tự động **xóa** hành trình không đạt mốc "thành công" sau 8 giờ không hoạt động — **trừ** bằng chứng đồng ý xử lý dữ liệu cá nhân, luôn giữ vĩnh viễn.
- Công cụ nội bộ (không phải màn hình sống, không public) để Dev/Compliance tra soát 1 hành trình cụ thể khi có yêu cầu audit.

> Chi tiết đầy đủ (schema, API, code mẫu): `../Specifications/BE_Spec.md`. Danh sách công việc: `../Issues/BE_Issue.md`.

---

## Tài liệu tham chiếu (chi tiết kỹ thuật)

Tài liệu này tập trung vào yêu cầu nghiệp vụ, không đi sâu kỹ thuật. Chi tiết database, API, mapping field — xem trong cùng thư mục `eKYC/Scope_1/`:

- **../Specifications/BE_Spec.md** — thiết kế kỹ thuật đầy đủ cho cả 2 nhật ký (schema, API, code mẫu)
- **../Issues/BE_Issue.md**, **../Issues/FE_Issue.md** — việc cần làm cho Dev Backend và App
- **../demos/Overview.html** — tổng hợp trực quan cho ai muốn xem nhanh
- **../../README.md** — cấu trúc toàn bộ tài liệu eKYC, trạng thái từng phần, lịch sử quyết định

---

**Bước tiếp theo:** Chốt câu hỏi ở mục 8 → Dev Lead đọc tài liệu kỹ thuật tham chiếu → estimate effort → lên kế hoạch Sprint. Scope 2 chờ mở lại sau khi Scope 1 go-live.
