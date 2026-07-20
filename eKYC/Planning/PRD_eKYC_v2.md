# PRD: Ghi nhận Lịch sử Xác thực & Nhật ký Hành trình Mở tài khoản eKYC

**Phiên bản:** 2.6 | **Ngày:** 2026-07-15 | **Trạng thái:** Đã chốt scope Phase 1 — mục 4.5 (Nhật ký Hành trình) vừa redesign

**Lịch sử thay đổi chính:**

| Ngày | Thay đổi |
|---|---|
| 2026-07-15 | Redesign toàn diện Nhật ký Hành trình (mục 4.5) — đổi từ ghi log theo API nghiệp vụ sang ghi log theo từng màn hình (App chủ động gửi), lưu đầy đủ dữ liệu thay vì chỉ tóm tắt; gộp mục 4.4 (Xác nhận điều khoản) vào chung nhật ký hành trình — không còn là mục riêng. Tài liệu kỹ thuật: `Scope_1/Specifications/BE_Spec.md` (Phần B), thay thế `Journey_API_Reference.md` bên dưới |
| 2026-07-08 (b) | ⚠️ *Đã được thay thế bởi redesign 2026-07-15 ở trên.* Tạo tài liệu kỹ thuật `07_Compliance_Journey_Log/Specifications/Journey_API_Reference.md` — danh sách đầy đủ 11 API cần log trong hành trình mở tài khoản, đã đối chiếu trực tiếp với source code app thực tế (không phải suy đoán). Đây là phần chi tiết kỹ thuật lẽ ra phải được chuyển sang tài liệu riêng khi PRD viết lại prose-only (2026-07-07) nhưng bị bỏ sót cho mục Nhật ký Hành trình — nay đã bổ sung |
| 2026-07-08 (a) | Bổ sung mô tả các tín hiệu chống gian lận mới phát hiện được khi đối chiếu với sample log thực tế từ đối tác VNPT (đối chiếu QR trên CCCD, phát hiện nhiều khuôn mặt trong ảnh, phát hiện đổi mặt/deepfake) vào mục 4.2 và 4.3 — không đổi phạm vi hay Phase, chỉ làm rõ hơn dữ liệu đã có sẵn từ VNPT mà mục 4.2 bản trước mô tả chung chung |
| 2026-07-07 | Viết lại toàn bộ tài liệu theo hướng dễ đọc cho người không chuyên kỹ thuật — chuyển chi tiết kỹ thuật (schema, API, field mapping) sang tài liệu tham chiếu riêng, chỉ giữ yêu cầu nghiệp vụ ở đây |
| 2026-07-06 | Chốt scope Phase 1 chỉ gồm lưu trữ dữ liệu (không có màn hình admin nào); chốt quy tắc lưu/xóa Nhật ký Hành trình; quyết định tách riêng nơi lưu trữ cho hai loại nhật ký |
| 2026-07-04 | Thêm yêu cầu Nhật ký Hành trình mở tài khoản (phục vụ compliance) |
| 2026-06-xx | Phiên bản gốc — lưu lịch sử lần thử xác thực, dashboard thống kê, tra cứu hành trình |

---

## Tóm tắt

Hiện tại, khi khách hàng mở tài khoản qua eKYC nhưng thất bại giữa chừng (ảnh mờ, không xác thực được khuôn mặt, hồ sơ bị từ chối...), hệ thống **không giữ lại bằng chứng nào** — dữ liệu bị xóa ngay khi khách thử lại. Hệ quả: Ops không trả lời được khách "tại sao tôi không mở được tài khoản", và công ty không có đủ bằng chứng khi cơ quan quản lý yêu cầu truy vết một hồ sơ.

Ở giai đoạn này (**Phase 1**), chúng ta xây dựng **hai loại nhật ký lưu dưới database**, phục vụ mục đích audit/truy vết — chưa có màn hình nào để xem trực tiếp:

1. **Nhật ký từng lần thử xác thực khuôn mặt/giấy tờ** — giữ lại mọi lần thử, kể cả thất bại, vĩnh viễn.
2. **Nhật ký hành trình mở tài khoản** (mới) — ghi lại các bước khách đã đi qua trong một lần mở tài khoản, nhưng **chỉ giữ lại nếu khách mở tài khoản thành công**; nếu không, dữ liệu sẽ tự động bị xóa.

**Quan trọng:** Phase 1 **không có bất kỳ màn hình nào cho Ops/Admin xem**. Mọi tra cứu ở giai đoạn này đều do Dev/DBA thực hiện thủ công khi cần điều tra case. Xây màn hình tra cứu, dashboard thống kê, và các cải tiến khác được để dành cho **Phase 2**.

---

## 1. Vấn đề hiện tại

| Vấn đề | Ảnh hưởng |
|---|---|
| Mỗi khi khách thử lại, hệ thống xóa luôn hồ sơ thử trước đó | Mất toàn bộ lịch sử — không biết khách đã fail bao nhiêu lần, vì lý do gì |
| Khi khách fail ngay từ bước quét giấy tờ/khuôn mặt (trước khi hồ sơ được gửi lên hệ thống), thông tin bước đó không được gửi về server | Không có dữ liệu để tra khi khách khiếu nại |
| Dữ liệu xác thực từ đối tác VNPT lưu dạng thô, chưa được sắp xếp | Không tra cứu/thống kê được theo nguyên nhân fail hay dấu hiệu gian lận |
| Ảnh CCCD của khách chưa được lưu lại theo từng lần thử | Không xem lại được ảnh khi cần xác minh case cụ thể |
| Không có công cụ thống kê tổng hợp | Ops/BA không có số liệu để đánh giá và cải thiện |
| Không ghi lại các bước khách đã đi qua trong hành trình mở tài khoản | Thiếu bằng chứng khi cơ quan quản lý (UBCK, NHNN) yêu cầu truy vết, hoặc khi khách khiếu nại về quy trình |

---

## 2. Mục tiêu & Phạm vi theo giai đoạn

| Mục tiêu | Giai đoạn |
|---|---|
| Lưu lại đầy đủ mọi lần thử xác thực (thành công và thất bại) | **Phase 1** |
| Ghi lại toàn bộ hành trình mở tài khoản, phục vụ compliance/audit | **Phase 1** |
| Liên kết các lần thử trước đó với tài khoản đã mở thành công | **Phase 1** |
| Lưu ảnh CCCD của từng lần thử | Phase 2 (cần chuẩn bị hạ tầng lưu trữ ảnh) |
| Màn hình cho Ops tra cứu lịch sử theo CCCD/SĐT | Phase 2 |
| Dashboard thống kê tỉ lệ lỗi, hỗ trợ phát hiện gian lận | Phase 2 |

**Phase 1 là một dự án thuần "hạ tầng dữ liệu"** — không có màn hình mới nào cho khách hàng hay cho Ops. Mục tiêu là đảm bảo dữ liệu được ghi đúng, đủ, và an toàn trước; việc hiển thị/khai thác dữ liệu đó để dành cho giai đoạn sau.

---

## 3. Ai liên quan & sẽ được hưởng lợi khi nào

| Vai trò | Nhu cầu | Đáp ứng ở Phase 1 | Đáp ứng đầy đủ ở |
|---|---|---|---|
| Ops / CS | Tra cứu lý do khách không mở được tài khoản | Nhờ Dev/DBA tra giúp thủ công | Phase 2 — có màn hình tự tra cứu |
| BA / PM | Phân tích tỉ lệ thất bại, đề xuất cải tiến UX | Chưa có | Phase 2 — Dashboard |
| Compliance / Legal | Có bằng chứng khi cơ quan quản lý yêu cầu | **Có** — dữ liệu được lưu đầy đủ dưới DB | — |
| Dev | Debug lỗi production | **Có** — tra cứu trực tiếp trong DB | Phase 2 — có màn hình |
| Security | Phát hiện dấu hiệu gian lận | Chưa có (xem mục 4.5 — đánh đổi đã ghi nhận) | Cần mở lại scope riêng |

---

## 4. Yêu cầu nghiệp vụ

### 4.1 Lưu lại mọi lần thử xác thực — không bao giờ xóa

Mỗi lần khách thử mở tài khoản (thành công hoặc thất bại) tạo ra **một bản ghi riêng**, được đánh số thứ tự lần thử. Bản ghi này **không bao giờ bị xóa hoặc sửa** sau khi tạo — kể cả khi khách thử lại nhiều lần, lịch sử các lần trước vẫn còn nguyên.

Có hai thời điểm một lần thử được ghi nhận:
- **Thất bại sớm** — ngay khi khách quét giấy tờ/khuôn mặt mà kết quả không đạt (ảnh mờ, không nhận diện được khuôn mặt, nghi ngờ giả mạo...), trước khi hồ sơ được gửi lên hệ thống chính.
- **Thất bại muộn hoặc thành công** — sau khi hồ sơ đã được gửi lên hệ thống chính (bị từ chối, hoặc được chấp nhận).

Cách xử lý hồ sơ hiện tại (khi khách thử lại, hồ sơ "đang chờ" cũ được dọn để tạo hồ sơ mới) **vẫn giữ nguyên như hiện tại** — thay đổi này không ảnh hưởng đến trải nghiệm khách hàng hay logic mở tài khoản, chỉ thêm một lớp ghi log song song.

### 4.2 Ghi nhận đầy đủ kết quả xác thực từ đối tác (VNPT)

Hiện có một số thông tin đối tác VNPT trả về nhưng ứng dụng chỉ dùng để kiểm tra ngay trên điện thoại, chưa gửi về hệ thống lưu trữ — ví dụ: kết quả kiểm tra khuôn mặt thật (chống giả mạo bằng ảnh in/ảnh chụp màn hình), xác suất ảnh giả, độ khớp khi so sánh khuôn mặt, quốc tịch và mã định danh chip trên CCCD. Các thông tin này cần được gửi về và lưu lại đầy đủ — đây chính là dữ liệu quan trọng nhất để xác định *tại sao* một lần thử thất bại, và để phát hiện dấu hiệu gian lận sau này.

Sau khi đối chiếu với dữ liệu thực tế đối tác trả về, phát hiện thêm ba nhóm tín hiệu chống gian lận sẵn có nhưng chưa được ghi nhận: (1) **đối chiếu mã QR trên CCCD với thông tin đọc từ mặt thẻ** — nếu hai nguồn lệch nhau là dấu hiệu nghi ngờ CCCD bị làm giả một phần; (2) **phát hiện ảnh có nhiều hơn một khuôn mặt** — cả ở bước quét khuôn mặt trực tiếp lẫn bước so khớp khuôn mặt với CCCD; (3) **phát hiện đổi mặt/deepfake** trên ảnh CCCD. Ba tín hiệu này nên được lưu cùng nhóm dữ liệu ở mục này, không cần thêm luồng xử lý hay màn hình riêng.

Ảnh mặt trước/mặt sau CCCD của mỗi lần thử cũng nên lưu lại để xem lại khi điều tra case — **việc lưu ảnh chính thức (lên hạ tầng lưu trữ file) để Phase 2**, vì cần chuẩn bị hạ tầng riêng.

### 4.3 Phân loại rõ nguyên nhân của mỗi lần thử

Mỗi lần thử được gắn một trong các nhãn kết quả sau, giúp việc tra cứu và (ở Phase 2) thống kê được nhất quán:

| Kết quả | Ý nghĩa |
|---|---|
| Xác thực VNPT thất bại | Đối tác VNPT trả lỗi ở bước đọc giấy tờ, kiểm tra khuôn mặt sống, chất lượng ảnh, hoặc phát hiện dấu hiệu giả mạo (QR không khớp với mặt thẻ, đổi mặt/deepfake, nhiều khuôn mặt trong ảnh) |
| So khớp khuôn mặt thất bại | Độ khớp giữa ảnh selfie và ảnh CCCD dưới ngưỡng cho phép |
| Hệ thống Lotte từ chối | Hồ sơ đã gửi lên nhưng bị hệ thống đối tác từ chối |
| Khách bỏ dở | Hồ sơ được chấp nhận nhưng khách không ký hợp đồng điện tử trong 48 giờ |
| Thành công | Tài khoản được mở thành công |

### 4.4 Ghi nhận thời điểm khách đồng ý điều khoản hợp đồng

Khi khách tick vào ô "Tôi đã đọc và đồng ý với điều khoản hợp đồng" ở bước xác nhận cuối cùng, hệ thống ghi lại **thời điểm chính xác** khách thực hiện việc này, cùng phiên bản nội dung điều khoản tại thời điểm đó. Đây là yêu cầu compliance — dùng để chứng minh khách đã đọc và đồng ý điều khoản khi có tranh chấp pháp lý về sau.

Thông tin này lưu cùng chỗ với lịch sử lần thử ở mục 4.1, không cần thêm màn hình hay luồng xử lý riêng.

> **Cập nhật 2026-07-15:** yêu cầu này giờ được đáp ứng như một phần của "Nhật ký Hành trình" (mục 4.5) — không còn là hạng mục triển khai riêng.

### 4.5 Ghi lại toàn bộ hành trình mở tài khoản — "Nhật ký Hành trình" (mới, phục vụ compliance)

Ngoài việc lưu kết quả xác thực khuôn mặt/giấy tờ (mục 4.1–4.3), công ty cũng cần ghi lại **toàn bộ các bước khách đã đi qua** trong một lần mở tài khoản — không chỉ riêng bước xác thực khuôn mặt. Mục đích: cung cấp bằng chứng đầy đủ khi cơ quan quản lý (UBCK, Ngân hàng Nhà nước) yêu cầu truy vết một hồ sơ, hoặc khi khách khiếu nại về quá trình mở tài khoản.

**Hành trình mở tài khoản gồm các bước sau:**

1. Khách nhập số điện thoại/email, khởi tạo hồ sơ
2. Hệ thống gửi mã OTP
3. Khách xác thực mã OTP
4. Khách quét CCCD và xác thực khuôn mặt (chi tiết ở mục 4.1–4.3 — có nhật ký riêng, không lặp lại ở đây)
5. Hệ thống kiểm tra CCCD của khách đã có tài khoản NHSV chưa
6. Khách điền thông tin còn thiếu, chọn ngân hàng nhận tiền, chọn nhân viên tư vấn (nếu có)
7. Khách đồng ý điều khoản hợp đồng (mục 4.4)
8. **Khách gửi hồ sơ hoàn chỉnh lên hệ thống** — bước quyết định hồ sơ "thành công" hay không (xem bên dưới)
9. Khách ký hợp đồng điện tử (đối tác FPT) — *bổ sung vào phạm vi ghi log từ 2026-07-15*: hệ thống tự động ghi nhận khi phía FPT xác nhận khách đã ký xong, kể cả khi việc ký diễn ra muộn hơn (khách quay lại ký ở phiên/thiết bị khác) — không cần khách đang mở app cùng lúc mở tài khoản
10. Hồ sơ chờ nhân viên duyệt, cấp số tài khoản chính thức — *nằm ngoài phạm vi ghi log này*

**Nguyên tắc lưu trữ (đã chốt):**

- Hệ thống ghi lại từng bước **ngay khi nó xảy ra** — không chờ đến cuối hành trình mới ghi một lần.
- **Một hành trình được coi là "thành công"** khi bước 8 (gửi hồ sơ hoàn chỉnh) được hệ thống xác nhận đã nhận và xử lý thành công. Việc ký hợp đồng (bước 9) hay được nhân viên duyệt (bước 10) **không ảnh hưởng** đến việc xác định "thành công" ở đây.
- **Nếu hành trình thành công → toàn bộ nhật ký các bước được giữ lại vĩnh viễn.**
- **Nếu hành trình không thành công** (lỗi rõ ràng ở một bước nào đó, hoặc khách bỏ dở không hoàn tất) **→ toàn bộ nhật ký các bước của lần đó sẽ bị xóa**, không giữ lại. Với trường hợp khách bỏ dở (không có lỗi rõ ràng, chỉ đơn giản là không tiếp tục), hệ thống chờ **8 giờ** không có hoạt động mới trước khi coi là "bỏ dở" và xóa dữ liệu (chốt 2026-07-15 — khớp đúng thời gian hết hạn phiên eKYC thực tế của hệ thống, thay cho khoảng đề xuất 24–48 giờ ban đầu).

> ⚠️ **Lưu ý:** vì mốc "thành công" chốt ngay ở bước gửi hồ sơ (bước 8), nếu sau đó nhân viên duyệt hồ sơ lại **từ chối** (bước 10), nhật ký hành trình vẫn được coi là "thành công" theo định nghĩa này và giữ lại vĩnh viễn — vì mốc chốt nằm ở bước gửi hồ sơ, không phải bước duyệt cuối cùng.

**Đánh đổi cần ghi nhận:** vì nhật ký của các hành trình không thành công sẽ bị xóa, hai mong muốn ban đầu — (1) phân tích khách hàng thường bị "kẹt" ở bước nào, và (2) phát hiện dấu hiệu gian lận từ hành vi lặp lại nhiều lần dựa trên dữ liệu hành trình — **sẽ không thực hiện được** với thiết kế này, vì dữ liệu cần để phân tích đã bị xóa. Đây là đánh đổi đã được PM chấp nhận để đơn giản hóa việc lưu trữ ở giai đoạn này; nếu sau này cần lại hai phân tích này, phải mở một scope riêng.

**Vì sao tách riêng khỏi nhật ký ở mục 4.1?** Nhật ký lần thử xác thực khuôn mặt (mục 4.1) phải giữ lại **mọi** lần thử, kể cả thất bại, **vĩnh viễn** — để có thể tra được lý do fail của bất kỳ lần thử nào. Nhật ký hành trình ở mục này lại cần **xóa** dữ liệu của những hành trình không thành công. Hai quy tắc lưu trữ này ngược nhau, nên hai loại nhật ký được lưu ở hai nơi tách biệt — nếu dùng chung một nơi lưu trữ, việc xóa của nhật ký hành trình sẽ vô tình xóa luôn dữ liệu mà nhật ký lần thử bắt buộc phải giữ.

### 4.6 Liên kết dữ liệu khi tài khoản mở thành công

Khi một khách hàng mở tài khoản thành công, hệ thống cập nhật để toàn bộ các lần thử trước đó (nếu có fail rồi mới thành công) được liên kết về tài khoản đó — giúp khi tra cứu một tài khoản, thấy được toàn bộ lịch sử các lần thử dẫn tới việc mở tài khoản thành công.

---

## 5. Ai ghi dữ liệu, và ghi ở đâu

Việc ghi log được thực hiện **hoàn toàn tự động phía hệ thống backend** — khách hàng và ứng dụng không cần biết hay làm thêm gì. Có hai hệ thống backend cùng tham gia ghi log (một xử lý phần xác thực/OTP, một xử lý phần gửi hồ sơ sang đối tác) — đội kỹ thuật cần thống nhất cách hai hệ thống này ghi log nhất quán với nhau, tránh xung đột hoặc thiếu dữ liệu (chi tiết kỹ thuật xem tài liệu tham chiếu cuối trang).

Toàn bộ dữ liệu ở Phase 1 chỉ tồn tại trong database, **không hiển thị lên bất kỳ màn hình nào**. Khi Dev hoặc Ops cần điều tra một case cụ thể, việc tra cứu được thực hiện thủ công, không qua giao diện.

---

## 6. Ngoài phạm vi (Phase 2 và các giai đoạn sau)

**Phase 2 — đã có demo/thiết kế sẵn, chờ mở lại scope:**
- Màn hình cho Ops tra cứu lịch sử mở tài khoản theo CCCD/SĐT (bao gồm các cải tiến nhỏ trên trang admin hiện có, ví dụ hiển thị số lần thử)
- Dashboard thống kê: tỉ lệ thành công/thất bại, nguyên nhân thất bại phổ biến, dấu hiệu gian lận
- Kiểm tra chéo vùng đọc máy (MRZ) trên CCCD ngay tại ứng dụng, để chặn sớm giấy tờ giả
- Lưu ảnh CCCD của từng lần thử lên hạ tầng lưu trữ file, hiển thị lại được trên màn hình tra cứu

**Chưa có lịch trình:**
- Cảnh báo tự động khi phát hiện dấu hiệu gian lận
- Tự động điều chỉnh ngưỡng độ khớp khuôn mặt
- Xuất báo cáo Excel/PDF từ Dashboard
- Liên kết hành trình khách hàng với các hệ thống khác (ví dụ: lịch sử giao dịch)

---

## 7. Câu hỏi còn chờ quyết định

| # | Câu hỏi | Ai quyết định | Mức độ ảnh hưởng |
|---|---|---|---|
| 1 | ✅ **Đã chốt 2026-07-15:** 8 giờ (khớp thời gian hết hạn phiên eKYC thực tế) | Dev Lead | — |
| 2 | ✅ **Đã chốt 2026-07-15 — riêng cho bước ký hợp đồng:** dùng ID tài khoản (đã có từ lúc gửi hồ sơ thành công) làm khóa nhận diện, không phụ thuộc phiên/thiết bị. Các bước còn lại trong hành trình vẫn dựa vào phiên do App khởi tạo — nếu khách đóng app giữa chừng (trước khi gửi hồ sơ) và mở lại, đây được tính là hành trình mới | Team ứng dụng di động + Dev Lead | Đã resolve cho bước ký hợp đồng; các bước khác giữ nguyên hành vi cũ |
| 3 | ✅ **Đã chốt 2026-07-15:** không log các bước tra cứu thông tin thuần túy (danh sách ngân hàng, chi nhánh...) — chỉ ghi lại các bước khách thực sự thao tác/xác nhận | Dev Lead / Ops | — |
| 4 | Việc lưu vĩnh viễn dữ liệu nhạy cảm (CCCD, số điện thoại, IP, ảnh sinh trắc học) của các hồ sơ thành công có tuân thủ đầy đủ Nghị định 13/2023 về bảo vệ dữ liệu cá nhân không? | Compliance/Legal | Cần rà soát trước khi go-live — vẫn còn mở |

---

## 8. Định nghĩa hoàn thành

**Phase 1 hoàn thành khi:**
- Mọi lần thử mở tài khoản (thành công và thất bại) đều được ghi lại đầy đủ, không bị mất khi khách thử lại
- Thời điểm khách đồng ý điều khoản hợp đồng có mặt trên mọi hồ sơ mở tài khoản thành công
- Toàn bộ các bước trong hành trình mở tài khoản được ghi lại, liên kết được với nhau qua cùng một hồ sơ
- Nhật ký hành trình của các trường hợp thành công được giữ lại vĩnh viễn; của các trường hợp thất bại/bỏ dở được xóa đúng theo quy tắc đã thống nhất
- Dev/DBA có thể tra cứu được toàn bộ lịch sử của một hồ sơ trong vài phút khi cần điều tra, không cần màn hình admin

**Phase 2 (bổ sung sau):**
- Ops tự tra cứu được lý do thất bại qua màn hình admin, không cần hỏi Dev
- Xem được ảnh CCCD của từng lần thử từ màn hình admin
- Dashboard hiển thị tỉ lệ thất bại theo nguyên nhân

---

## Tài liệu tham chiếu (chi tiết kỹ thuật)

Tài liệu này tập trung vào yêu cầu nghiệp vụ, không đi sâu vào cách triển khai kỹ thuật. Chi tiết kỹ thuật (cấu trúc database, API, mapping field) được đội Dev lưu riêng ở các tài liệu sau, trong cùng thư mục eKYC của repo:

- Thiết kế kỹ thuật đầy đủ cho nhật ký lần thử xác thực (mục 4.1–4.3) — nằm trong hồ sơ đặc tả của sub-feature "Biometric Attempt Log"
- Danh sách task triển khai cho developer của sub-feature trên
- Thiết kế kỹ thuật đầy đủ cho Nhật ký Hành trình (mục 4.5, đã redesign 2026-07-15 — gộp luôn việc ghi thời điểm đồng ý điều khoản ở mục 4.4, không còn là sub-feature riêng) — `Scope_1/Specifications/BE_Spec.md` (Phần B), đã đối chiếu trực tiếp với source code app
- Bản tổng hợp trực quan (sơ đồ luồng, bảng dữ liệu) cho ai muốn xem nhanh cả góc nhìn kỹ thuật
- Tài liệu tổng quan (README) mô tả cấu trúc toàn bộ các sub-feature và trạng thái từng phần

---

**Trạng thái tài liệu:** Đã chốt scope Phase 1 (v2.6) — mục 4.5 (Nhật ký Hành trình) redesign 2026-07-15: gộp mục 4.4, bổ sung bước ký hợp đồng điện tử vào phạm vi ghi log, chốt ngưỡng "bỏ dở" = 8 giờ, chốt câu hỏi 1-3 ở mục 7. Câu hỏi 4 (PDPD) vẫn còn mở
**Dành cho:** PM/BA, Ops, Compliance/Legal, Dev Lead
**Bước tiếp theo:** Chốt 4 câu hỏi ở mục 7 → Dev Lead đọc chi tiết kỹ thuật ở tài liệu tham chiếu → estimate effort → lên kế hoạch Sprint. Phase 2 (màn hình admin, Dashboard, MRZ, lưu ảnh) chờ mở lại scope riêng sau khi Phase 1 go-live.
