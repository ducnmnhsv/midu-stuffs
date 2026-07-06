# eKYC Journey Logging — Planning

**Document Type:** Planning (PRD-level, prose only)
**Feature Area:** eKYC — Journey Logging (mở rộng phạm vi Biometric Attempt Log v2)
**Owner:** Midu (PO)
**Date:** 2026-07-04
**Version:** 1.0 Draft

---

## 1. Mục đích và phạm vi

### 1.1 Bối cảnh

Hiện tại NHSV Pro đã có (hoặc đang triển khai) hai loại log liên quan đến hành vi user trong hành trình mở tài khoản eKYC:

- **VNPT biometric log** — dữ liệu SDK trả về sau khi quét CCCD và nhận diện khuôn mặt (đã có kế hoạch lưu structured qua sub-feature `01_Biometric_Attempt_Log`).
- **Terms & Conditions checkbox log** — thời điểm user tick từng checkbox điều khoản (đã có kế hoạch qua sub-feature `05_Contract_Terms_Checkbox_Log`).

Tuy nhiên, giữa các bước sinh ra hai loại log trên còn có nhiều API khác mà user tương tác trong suốt hành trình. Hôm nay hệ thống không lưu lại toàn bộ chuỗi lời gọi này. Khi cần điều tra một case (ví dụ user báo lỗi giữa chừng, hoặc phòng compliance cần truy vết một hồ sơ khả nghi), team Ops không có bằng chứng đầy đủ về việc user đã đi qua những bước nào, gọi API nào, khi nào, có thành công hay không.

### 1.2 Mục đích

Xây dựng cơ chế **log toàn bộ hành trình eKYC của user** — không chỉ hai điểm dữ liệu đã có, mà bao gồm mọi lời gọi API dọc theo luồng mở tài khoản, để phục vụ:

- Truy vết case khi có sự cố hoặc khiếu nại.
- Cung cấp bằng chứng compliance khi cơ quan quản lý (UBCK, ngân hàng nhà nước) yêu cầu.
- Phân tích friction point trong luồng (bước nào user hay drop-off, API nào hay fail).
- Phát hiện fraud pattern (ví dụ nhiều CCCD khác nhau cùng một device, retry bất thường).

### 1.3 Phạm vi

Log 11 API trong hành trình mở tài khoản eKYC:

1. `POST /api/v1/lotte/ekycs/create` — tạo eKycId (application number).
2. `POST /api/v1/ekyc-admin/sendOtp` — gửi OTP tới số điện thoại.
3. `POST /api/v1/ekyc-admin/verifyOtp` — xác thực OTP.
4. `POST /api/v1/lotte/ekycs` — submit form eKYC hoàn chỉnh.
5. `GET /api/v1/ekycs/banks` — lấy danh sách ngân hàng.
6. `GET /api/v1/ekycs/branch` — lấy danh sách chi nhánh NHSV.
7. `GET /api/v1/ekycs/banks/{id}/branches` — lấy chi nhánh của ngân hàng đã chọn.
8. `GET /api/v1/ekycs/partner` — validate NHSV staff (referral CTV).
9. `GET /api/v1/ekycs/account/exist` — check trạng thái mở account tại Lotte.
10. `POST /api/v1/equity/account/checkNationalId` — kiểm tra CCCD chưa có tài khoản NHSV.
11. `GET /api/v1/equity/account/contracts` — lấy webView URL của FPT eContract.

Ngoài phạm vi: các API sau khi tài khoản đã được mở (poll `contractStatus`, `vsdStatus`), các API không thuộc hành trình mở tài khoản (login, market data, order).

### 1.4 Liên hệ với sub-feature hiện có

- **01_Biometric_Attempt_Log** đang thiết kế bảng `ekyc_attempt_log` cho VNPT SDK data — Journey Logging có thể mở rộng bảng này (hoặc dùng bảng liên quan) để tránh phân tán dữ liệu về một user.
- **05_Contract_Terms_Checkbox_Log** đã đề xuất thêm cột `terms_agreed_at` vào cùng bảng — Journey Logging tiếp tục theo hướng tập trung dữ liệu.

Mục tiêu là **một hành trình eKYC = một tập bản ghi được liên kết bằng identifier chung** (eKycId, hoặc phoneNo + timestamp khi chưa có eKycId), để admin/BA có thể truy xuất toàn bộ hoạt động của user chỉ với một khóa tìm kiếm.

---

## 2. Hành trình mở tài khoản (các bước)

Chi tiết end-to-end đã trace được từ source code trong analyst findings. Tóm lược ở góc độ user và API call sequence:

**Bước 0 — Khởi tạo phiên.** App login bằng client_credentials (tài khoản demo dùng chung cho toàn bộ user chưa có tài khoản NHSV) để có JWT truy cập nhóm API eKYC.

**Bước 1 — Nhập thông tin liên hệ và xác thực OTP.** User nhập số điện thoại, email, chọn cá nhân hay tổ chức. App gọi `POST /lotte/ekycs/create` để tạo `eKycId` (đây là identifier chính của hành trình). Sau đó `POST /ekyc-admin/sendOtp` gửi OTP tới số điện thoại. User nhập 6 chữ số vào ứng dụng, App gọi `POST /ekyc-admin/verifyOtp` để nhận `otpKey` dùng cho các bước sau.

**Bước 2 — Quét giấy tờ và nhận diện khuôn mặt (VNPT SDK).** Chạy phía client, không có API TradeX tương ứng cho đến khi submit. VNPT SDK sinh ra các log ID (OCR, card liveness, face compare, face liveness, mask check) và các URL ảnh đã upload lên S3 của VNPT. Dữ liệu này chỉ được đẩy về server ở bước submit. Đây là điểm sub-feature `01_Biometric_Attempt_Log` đề xuất bổ sung endpoint `POST /ekycs/attempt-log` để log từng lần thử (kể cả retry).

**Bước 3 — Kiểm tra CCCD đã có tài khoản chưa.** App gọi `POST /equity/account/checkNationalId` với CCCD lấy từ OCR. Backend query song song hai nguồn: DB ekyc-admin (case pending) và Lotte Core (case đã mở). Nếu Lotte trả code V3101 → user đã có tài khoản, chặn luồng.

**Bước 4 — Điền form và chọn ngân hàng.** App load các dropdown: `GET /ekycs/banks` (danh sách ngân hàng), `GET /ekycs/branch` (chi nhánh NHSV), `GET /ekycs/banks/{id}/branches` (chi nhánh của ngân hàng đã chọn). Nếu user chọn referral = "Nhân viên/CTV" → App gọi `GET /ekycs/partner` để validate mã nhân viên và hiển thị tên. User điền các trường còn thiếu, chọn dịch vụ (margin, phái sinh, SMS, thông báo email), thêm bank account (tối đa 3), điền câu hỏi kinh nghiệm đầu tư và các thông tin compliance (public coop, blockholder, FATCA, taxNo, beneficiary owner).

**Bước 5 — Chấp nhận điều khoản.** User tick các checkbox Terms & Conditions. Sub-feature `05_Contract_Terms_Checkbox_Log` sẽ log thời điểm tick từng checkbox.

**Bước 6 — Submit hồ sơ.** App gọi `POST /lotte/ekycs` gửi toàn bộ form + VNPT metadata + image URLs + deviceUniqueId. Backend flow: lưu entity `EKyc` vào DB ekyc-admin → gọi Lotte upload ảnh và tạo hồ sơ trên Lotte → gọi FPT tạo envelope hợp đồng điện tử. Response `{status: "success"}`.

**Bước 7 — Ký hợp đồng điện tử.** App gọi `GET /equity/account/contracts?eKycId=xxx` để nhận webView URL + JWT cookie. App mở iframe FPT, user ký. Sau khi ký, App poll trạng thái hợp đồng và trạng thái VSD (ngoài phạm vi log).

**Bước 8 — Chờ approval.** Ops duyệt hồ sơ, VSD cấp số tài khoản chính thức. Ngoài phạm vi log.

---

## 3. Những gì cần log (theo từng bước / API)

Với mỗi lời gọi API trong phạm vi, cần ghi nhận đầy đủ để có thể tái hiện lại điều gì đã xảy ra. Các trường log áp dụng chung cho mọi API:

- **Định danh hành trình.** `eKycId` (nếu đã có), `phoneNo` (dùng làm khóa phụ ở bước 1 trước khi có eKycId), `identifierId` (CCCD, từ bước 3 trở đi). Tối thiểu một trong ba để liên kết bản ghi về cùng một hành trình.
- **Thời điểm.** Timestamp lời gọi (ms precision) — cho phép sắp xếp trình tự và tính thời lượng từng bước.
- **Endpoint và method.** Ví dụ `POST /api/v1/lotte/ekycs/create`.
- **Bước trong hành trình.** Enum thứ tự bước (`CREATE_APPLICATION`, `SEND_OTP`, `VERIFY_OTP`, `CHECK_NATIONAL_ID`, `LOAD_BANKS`, `LOAD_NHSV_BRANCH`, `LOAD_BANK_BRANCHES`, `VALIDATE_PARTNER`, `CHECK_ACCOUNT_EXIST`, `SUBMIT_FORM`, `LOAD_ECONTRACT`). Giúp query group theo bước.
- **Outcome.** Success / Failure + HTTP status + error code (nếu có) + error message tóm tắt.
- **Request payload (đã redact).** Không log toàn bộ raw — chỉ giữ những trường có ý nghĩa nghiệp vụ (xem chi tiết bên dưới). Loại bỏ dữ liệu nhạy cảm hoặc dữ liệu trùng lặp với entity `EKyc` (đã lưu ở DB chính).
- **Response payload (tóm tắt).** Chỉ trường có ý nghĩa (eKycId trả về, otpId, exist true/false, envelopeId…). Không log webView URL đầy đủ chứa cookie.
- **Context.** Device ID, IP nguồn, user-agent, phiên bản App (nếu App gửi). Tương thích với các trường đã dùng ở entity `EKyc` (`deviceUniqueId`, `sourceIp`).

Chi tiết trường quan trọng theo từng API:

**API 1 — create.** Log groupType (idv/org), phoneNo, email, eKycId trả về.

**API 2 — sendOtp.** Log id (phone), idType, txType, otpId trả về, expiredTime. Không log giá trị OTP thực (không có trong response API này). Đếm số lần user request resend OTP trong cùng phiên.

**API 3 — verifyOtp.** Log otpId, kết quả (verified/failed), error code (`INCORRECT_OTP`, `OTP_EXPIRED`, `INCORRECT_OTP_MAX`). Không log otpValue (6 chữ số). Đếm số lần thử.

**API 4 — submit /lotte/ekycs.** Đây là bản ghi lớn nhất. Không cần log lại toàn bộ form vì đã có ở entity `EKyc` — chỉ log link tới `EKyc.id`, deviceUniqueId, matchingRate, tất cả VNPT log IDs (ocrLogId, cardLivenessLogId, cardRearLogId, compareLogId, faceLivenessLogId, faceMaskLogId), số lượng bank account, referral, partnerId. Outcome: success / error code.

**API 5, 6, 7 — banks / branch / bank-branches.** Log request (bankCode nếu có), số lượng record trả về. Không log toàn bộ list. Mục đích chính là ghi nhận user đã truy cập bước chọn ngân hàng.

**API 8 — partner.** Log partnerId đã tra cứu, kết quả (found / not_found), tên partner trả về. Đây là điểm compliance quan trọng — chứng minh user chọn CTV nào.

**API 9 — account/exist.** Log identifierId (CCCD), exist true/false.

**API 10 — checkNationalId.** Log identifierId, kết quả 2 nguồn (ekyc-admin có pending record hay không, Lotte có tài khoản hay không), error code cuối cùng.

**API 11 — contracts.** Log eKycId, envelopeId, envelopeStatus, recipientStatus, contractNo. Không log webView URL đầy đủ (chứa cookie có thời hạn), chỉ ghi nhận thời điểm đã trả webView cho App.

---

## 4. Storage strategy

### 4.1 Trigger — chỉ log khi thành công hay tất cả?

PM đề xuất cân nhắc chỉ lưu full log khi mở tài khoản THÀNH CÔNG để tiết kiệm storage. Đề xuất phương án dung hòa:

**Ghi log theo hai chế độ tùy trạng thái hành trình:**

- **Chế độ hot (in-progress).** Mọi lời gọi API trong phạm vi được ghi vào bảng log dưới dạng bản ghi tạm thời (partition theo ngày). Bản ghi tạm này lưu tối đa **90 ngày** rồi tự động dọn dẹp nếu hành trình không kết thúc thành công.
- **Chế độ cold (finalized).** Khi hành trình kết thúc thành công (bước 6 submit thành công, hoặc bước 8 VSD APPROVED — cần chốt điểm nào là "kết thúc" ở phần Open Questions), toàn bộ bản ghi thuộc `eKycId` đó được đánh dấu `finalized = true` và giữ vô thời hạn theo yêu cầu PM. Các bản ghi liên kết cùng phoneNo/identifierId nhưng khác eKycId (nếu user tạo lại nhiều lần) cũng được đánh dấu retention lâu dài nếu có ít nhất một hành trình thành công cho cùng CCCD.

**Lý do không log-only-on-success:** Một hành trình có thể fail giữa chừng nhưng vẫn cần audit trail để trả lời khiếu nại (user gọi hotline hỏi "tại sao tôi không mở được tài khoản"). Nếu chỉ log khi success, mọi case failure sẽ không có bằng chứng — đi ngược lại với mục tiêu số 1 và 2 của tính năng. Cách dung hòa là **log ngay tất cả, dọn dẹp sau nếu không finalize** — vừa đảm bảo audit ngắn hạn cho case fail, vừa giữ storage bounded cho case bỏ dở.

### 4.2 Retention

- Bản ghi thuộc hành trình đã finalized: **permanent** (theo yêu cầu PM). Không xóa bao giờ.
- Bản ghi hot chưa finalized: **90 ngày** rồi tự dọn.
- Cần định nghĩa rõ: finalized = "submit thành công" hay = "VSD approved"? Trả lời ở Open Questions.

### 4.3 DB table

- Không tạo bảng mới nếu có thể tận dụng bảng đang thiết kế ở sub-feature `01_Biometric_Attempt_Log`. Ưu tiên **mở rộng bảng `ekyc_attempt_log`** (nếu tên như vậy được confirm ở BE spec) bằng cách:
  - Thêm cột phân loại bước (`journey_step`).
  - Thêm cột endpoint, http_method, http_status, response_summary (JSON).
  - Thêm cột `finalized` (boolean) + `finalized_at` (timestamp) để hỗ trợ retention policy.
- Nếu bảng attempt_log hiện tại đã cố định schema cho VNPT và không phù hợp để thêm cột, tạo bảng anh em `ekyc_journey_log` với foreign key tới `ekyc_attempt_log` hoặc chung khóa `eKycId`. Quyết định thuộc BE Lead — Planning doc chỉ đặt ràng buộc "cùng một khóa tìm kiếm cho user Ops".
- Cần index: `(eKycId)`, `(phoneNo, created_at)`, `(identifierId)`, `(finalized, created_at)` — hỗ trợ query từ admin và job dọn dẹp.

### 4.4 Ai ghi log?

- **Backend-side logging** là phương án chính. Mỗi service (ekyc-admin, lotte-bridge) ghi log ngay sau khi xử lý xong request. Ưu điểm: chính xác về outcome/error code, không phụ thuộc client. Nhược điểm: cần thay đổi ở nhiều service.
- Một số bước không có backend call rõ ràng (ví dụ user tick checkbox — bước 5) đã có phương án App gọi API `POST /ekycs/attempt-log` (sub-feature 01). Có thể mở rộng cùng endpoint này cho các event pure-client (bỏ dở, quay lại bước trước, timeout OTP).
- Vì có 2 service ghi log về cùng một bảng, cần thống nhất producer: hoặc cả hai service ghi trực tiếp DB (đơn giản, latency thấp), hoặc dùng Kafka topic chung `ekyc-journey-log` để 1 consumer duy nhất write (giảm coupling, tăng độ trễ). Quyết định BE Lead — Planning doc đặt yêu cầu về consistency và ordering, không quyết định phương án.

### 4.5 Ẩn khỏi admin page

Theo yêu cầu PM, log Journey **KHÔNG hiển thị trên admin page**. Điều này áp dụng cho UI hiện có ở sub-feature `02_Admin_Attempt_History` và `03_Admin_Dashboard_Analytics` — journey log chỉ tồn tại ở DB, truy cập qua tool nội bộ (DBA, Ops manual query) khi cần điều tra. Nếu về sau muốn mở ra admin UI, cần thảo luận riêng và có approval của compliance.

Hệ quả thiết kế: không cần thiết kế màn hình, không cần API GET public. Có thể cung cấp một API nội bộ (authorized bằng role riêng, khác admin thường) để tool nội bộ query — hoặc chỉ dùng SQL trực tiếp. Đề xuất giai đoạn 1 chỉ có SQL, giai đoạn sau bổ sung API nội bộ nếu Ops cần tự truy vấn.

---

## 5. Những gì KHÔNG cần log (scope exclusion)

- **API sau khi mở tài khoản.** `contractStatus`, `vsdStatus` — thuộc giai đoạn approval, không thuộc hành trình user tương tác.
- **Login / auth.** client_credentials login, refresh token — thuộc phạm vi aaa/audit chung, không nằm trong journey.
- **VNPT SDK data raw.** Đã có kế hoạch riêng ở sub-feature 01 (structured VNPT log). Journey Logging chỉ trỏ tới `attempt_id` chứ không duplicate.
- **Toàn bộ form eKYC ở bước submit.** Đã lưu ở entity `EKyc`. Journey log chỉ tham chiếu, không copy lại.
- **Các API GET dropdown khác không thuộc phạm vi 11 API.** Ví dụ tỉnh/huyện, quốc gia — nếu không nằm trong list, không log.
- **PII nhạy cảm không cần thiết cho audit.** Không log OTP value, không log full webView URL kèm cookie, không log dataBase64/dataSign VNPT (đã lưu ở nơi khác).
- **Client-only navigation.** Không cần log user chuyển tab, cuộn màn hình, v.v. — chỉ log các sự kiện có API call hoặc có ý nghĩa nghiệp vụ (tick checkbox).
- **Admin/BA tra cứu.** Journey log ghi hoạt động của user cuối, không ghi hoạt động của admin/Ops truy vấn dữ liệu.

---

## 6. Open questions / quyết định còn chờ

1. **Định nghĩa "finalized" cho retention policy.** Là thời điểm submit `POST /lotte/ekycs` thành công, hay là VSD APPROVED? Chọn submit → retention bắt đầu sớm, có nguy cơ giữ vĩnh viễn cả case sau đó bị Ops reject. Chọn VSD APPROVED → cần listen event VSD (hiện chưa rõ có event Kafka hay chỉ poll trạng thái).

2. **Consistency giữa 2 service ghi log.** ekyc-admin (Java) và lotte-bridge (Node.js) đều tham gia. Chọn direct-DB-write hay Kafka producer/consumer? Ảnh hưởng đến độ trễ và độ phức tạp vận hành.

3. **Sub-feature nào là "canonical" của bảng log?** Nếu sub-feature 01 (Biometric Attempt Log) đã chốt schema chỉ tập trung VNPT SDK, có nên mở rộng cùng bảng hay tạo bảng anh em `ekyc_journey_log`? Cần BE Lead đọc lại spec 01 và phản hồi.

4. **Bước 0 (client_credentials login) có cần log không?** Về nghiệp vụ không cần biết vì mọi user dùng cùng tài khoản demo. Nhưng có thể muốn ghi nhận session start timestamp để tính thời lượng hành trình. Đề xuất: log tối giản (chỉ deviceUniqueId + timestamp) như event `SESSION_START`, không tính là API log.

5. **API 5, 6, 7 (dropdown) có thực sự cần log?** Đây là các GET không thay đổi trạng thái, có thể lặp nhiều lần. Nếu log tất cả sẽ tạo nhiều bản ghi noise. Đề xuất: chỉ log lần đầu user gọi mỗi endpoint trong phiên (dùng cache client hoặc dedupe server), hoặc bỏ qua nếu Ops không thấy giá trị audit.

6. **Định nghĩa "cùng một hành trình" khi user retry.** Nếu user drop giữa chừng và mở lại App sau vài giờ, có tạo `eKycId` mới không? Nếu có, làm sao liên kết với phiên cũ (dùng phoneNo? deviceUniqueId? cả hai?). Cần trace behavior thực tế của App hoặc hỏi FE team.

7. **Compliance approval.** Do lưu vĩnh viễn dữ liệu nhạy cảm (CCCD, phoneNo, IP), cần review với legal/compliance để đảm bảo tuân thủ Nghị định 13/2023 (Bảo vệ dữ liệu cá nhân). Có thể cần thêm cơ chế xóa theo yêu cầu (right to erasure) — mâu thuẫn với "permanent retention" — cần làm rõ policy.

8. **Kích thước bảng dài hạn.** Ước lượng: 1000 hành trình/ngày × 11 API × 365 ngày ≈ 4 triệu bản ghi/năm. Với retention vĩnh viễn cho case success (giả sử 30% success), sau 5 năm bảng có ~6-7 triệu bản ghi. Cần cân nhắc partitioning theo tháng/năm và archival strategy nếu bảng vượt ngưỡng.

---

Document Status: 📋 Draft | For: PM / BE Lead / Compliance | Next Steps: Chốt 8 open questions với BE Lead và compliance, sau đó hand-off spec chi tiết (Backend_Spec.md) cho developer
