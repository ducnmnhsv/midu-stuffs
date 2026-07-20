# FE Issue: App eKYC — Ghi nhận Attempt Log & Compliance Journey Log

**App:** NHSV Pro (nhsv-mts-rn)
**Priority:** High
**Feature:** Sub-feature 01 (Biometric Attempt Log) + Sub-feature 07 (Compliance Journey Log) — gộp chung 1 issue FE vì cùng cách vận hành (App gọi log riêng tại đúng thời điểm, không chặn luồng chính mở tài khoản)
**Type:** Instrumentation cho hầu hết bước (không có UI mới) + **1 UI mới** — màn hình đồng ý xử lý dữ liệu cá nhân (Phần B, bước 1)
**Spec liên quan:** `../Specifications/BE_Spec.md` (Phần A = Sub-feature 01, Phần B = Sub-feature 07)
**Blocked by:** BE endpoint `POST /ekycs/attempt-log` (Phần A) và `POST /api/v1/ekycs/journey-log` (Phần B) — xem `BE_Issue.md`

---

## Bối cảnh chung

Khi có tranh chấp, audit compliance, hoặc điều tra gian lận về một tài khoản mở qua eKYC, hiện không có cách nào tái dựng lại chính xác (1) kết quả xác thực sinh trắc học/giấy tờ ở từng lần thử, và (2) những gì khách hàng đã thấy/xác nhận ở từng bước trong hành trình — nhiều dữ liệu này chỉ tồn tại tạm thời trên App (qua VNPT SDK hoặc local state) và không được gửi lên hệ thống nào. BE đã chuẩn bị 2 API log riêng, độc lập với luồng mở tài khoản chính (`/lotte/ekycs`), để nhận đầy đủ dữ liệu này tại đúng thời điểm xảy ra:

- **Sub-feature 01 — Biometric Attempt Log:** ghi lại **mọi lần thử** xác thực sinh trắc học/giấy tờ, kể cả thất bại — phục vụ audit fraud/biometric, giữ vĩnh viễn.
- **Sub-feature 07 — Compliance Journey Log:** ghi lại **toàn bộ hành trình** mở tài khoản theo từng màn hình — phục vụ bằng chứng compliance, chỉ giữ nếu hành trình thành công.

---

# Phần A — Sub-feature 01: Biometric Attempt Log

## User Story

> As a compliance/audit officer, tôi muốn có đầy đủ log của MỌI lần khách thử xác thực CCCD/khuôn mặt qua VNPT SDK — kể cả những lần thất bại và chưa từng chạm tới hệ thống Lotte — để điều tra gian lận hoặc tra soát khi có tranh chấp.

## Yêu cầu chức năng

App gọi API `POST /ekycs/attempt-log` tại 1 trong 2 tình huống sau, tùy theo kết quả xác thực SDK VNPT:

1. **Lần thử thất bại trước khi gửi hồ sơ (pre-submit fail)** — SDK VNPT trả kết quả không đạt (đọc CCCD lỗi, xác minh khuôn mặt trực tiếp thất bại, MRZ không khớp...) **trước khi** App gọi `/lotte/ekycs`. App gọi `attempt-log` với kết quả thất bại + toàn bộ dữ liệu SDK đã nhận được tới thời điểm đó, rồi dừng luồng — **không** gọi `/lotte/ekycs`.
   - Nếu thất bại ngay ở bước đọc CCCD (OCR): **bắt buộc gửi kèm ảnh mặt trước/mặt sau CCCD** (base64) để phục vụ điều tra.
2. **Lần thử đã gửi hồ sơ (post-submit)** — SDK VNPT pass hết, App vừa gọi `/lotte/ekycs` xong (**API này giữ nguyên, không đổi gì**) → App gọi thêm `attempt-log` với kết quả cuối cùng (thành công hoặc bị hệ thống Lotte từ chối) kèm toàn bộ dữ liệu SDK (kết quả đọc MRZ, xác minh khuôn mặt trực tiếp, so khớp khuôn mặt, và các mã log VNPT App đã có sẵn).

Cả 2 tình huống đều gửi **toàn bộ** dữ liệu SDK trả về, nguyên văn — không tự lọc/rút gọn field nào, để BE audit được cả những field chưa có cột riêng.

## Quy tắc nghiệp vụ

- `POST /lotte/ekycs` **không đổi** — chỉ gửi những gì hệ thống Lotte cần như hiện tại.
- Ảnh CCCD (base64) **bắt buộc** khi lần thử thất bại ngay ở bước đọc CCCD; không bắt buộc ở các trường hợp thất bại khác (liveness, so khớp khuôn mặt).
- Việc gọi `attempt-log` **không được** làm chậm hoặc chặn luồng chính mở tài khoản — không hiển thị loading chờ log hoàn tất, không rollback bước hiện tại nếu request log thất bại.
- Xem quy tắc retry khi mất kết nối mạng ở mục "Business rule chung" cuối tài liệu — áp dụng cho cả `attempt-log` và `journey-log`.

## Acceptance Criteria

- [ ] App gọi đúng `attempt-log` ở cả 2 tình huống (pre-submit fail và post-submit) — không bỏ sót tình huống nào.
- [ ] Lần thử thất bại ở bước đọc CCCD gửi kèm đầy đủ ảnh mặt trước/mặt sau (base64).
- [ ] Dữ liệu SDK gửi lên đầy đủ, nguyên văn — không cắt bớt field nào so với dữ liệu VNPT SDK trả về cho App.
- [ ] Việc gọi log không gây chậm trễ hoặc gián đoạn cảm nhận được cho khách hàng.
- [ ] QA verify được: 1 lần thử thất bại (chưa từng gọi `/lotte/ekycs`) vẫn tra được đầy đủ log qua `attempt-log`.

---

# Phần B — Sub-feature 07: Compliance Journey Log

## User Story

> As a compliance/PO, tôi muốn mỗi bước quan trọng trong hành trình mở tài khoản qua eKYC được ghi lại đầy đủ dữ liệu khách đã nhập/xác nhận tại đúng thời điểm đó, để khi có tranh chấp hoặc audit, có thể tái dựng chính xác toàn bộ hành trình của khách hàng.

## Yêu cầu chức năng

App gọi API `POST /api/v1/ekycs/journey-log` ngay khi khách hoàn tất mỗi bước dưới đây — **kể cả khi bước đó thất bại** (gửi `status: FAILED` kèm lý do nếu có), không chỉ gọi lúc thành công. Mọi lần gọi dùng chung 1 `sessionId` do App tự sinh khi khách bắt đầu hành trình, giữ nguyên xuyên suốt tới bước cuối.

1. **Đồng ý xử lý dữ liệu cá nhân** — màn hình đầu tiên của luồng eKYC, ngay sau khi khách nhập SĐT, **trước khi App gửi OTP**. Hiển thị checkbox "Tôi đã đọc và đồng ý với nội dung Điều khoản và điều kiện xử lý dữ liệu cá nhân của Công ty TNHH Chứng khoán NH Việt Nam". Đây là consent **hoàn toàn khác** với bước 10 (điều khoản hợp đồng mở TK) — không gộp, không dùng lại state của bước 10. Yêu cầu này xuất phát từ PDPD (Nghị định 13/2023 về bảo vệ dữ liệu cá nhân — giải thích chi tiết ở PRD mục 6): phải xin đồng ý **trước khi** thu thập bất kỳ dữ liệu CCCD/sinh trắc học nào.
2. **Gửi OTP xác thực SĐT** — ngay sau khi App gửi OTP thành công.
3. **Xác thực OTP** — ngay sau khi khách nhập đúng mã OTP.
4. **Xem hướng dẫn chụp CCCD** — sau khi khách đã nhập SĐT, email, quốc tịch, nghề nghiệp và xem xong hướng dẫn — gửi kèm toàn bộ các field này.
5. **Quét khuôn mặt & CCCD** — ngay sau khi nhận đầy đủ kết quả từ VNPT SDK (OCR, xác minh khuôn mặt trực tiếp, so khớp khuôn mặt) — gửi **toàn bộ** dữ liệu SDK trả về bao gồm cả ảnh (base64), không lọc bớt field nào.
6. **Xác nhận thông tin cá nhân** — sau khi khách xác nhận ngày sinh, địa chỉ, thông tin FATCA...
7. **Thông tin tài khoản** — loại tài khoản, chi nhánh, các cờ margin/phái sinh khách chọn.
8. **Thông tin ngân hàng** — tài khoản ngân hàng nhận tiền khách khai.
9. **Thông tin đầu tư** — mục tiêu đầu tư, khẩu vị rủi ro khách chọn.
10. **Xác nhận điều khoản hợp đồng** — ngay khi khách tick "Tôi đã đọc và đồng ý" — gửi kèm **toàn văn** nội dung điều khoản khách đã đọc, không chỉ trạng thái đồng ý.
11. **Hoàn tất mở tài khoản** — mốc cuối cùng của hành trình, đánh dấu hành trình đã thành công.

**Không cần App xử lý gì thêm cho bước ký hợp đồng điện tử** — bước này (`ECONTRACT_SIGN_COMPLETED`) do BE tự ghi nhận qua webhook FPT khi khách ký xong, độc lập với phiên làm việc của App.

## Business rule riêng bước 1 (đồng ý xử lý dữ liệu cá nhân)

- Checkbox **chưa tick** → nút "Tiếp theo" phải ở trạng thái **disabled** (xem mockup PM cung cấp). Chỉ enable khi khách đã tick.
- Khác với các bước 2-11 (chỉ log, không chặn luồng), bước này **là điều kiện chặn** để qua màn kế — nhưng việc **gọi log** vẫn áp dụng đúng rule chung: gọi ngay sau khi khách tick + bấm "Tiếp theo", không block chờ log trả response trước khi chuyển màn.
- **[Đơn giản hoá 2026-07-20]** Payload chỉ cần ghi nhận việc khách đã tick — `{ isAgree: true, phoneNo }` là đủ, **không cần** gửi kèm toàn văn/version nội dung điều khoản (khác với bước 10 — 2 bước có yêu cầu payload khác nhau, không áp dụng chung 1 rule).
- **Bằng chứng consent này được BE giữ vĩnh viễn**, không bị xóa dù hành trình sau đó bỏ dở (quyết định 2026-07-20, xem PRD mục 4.4 ("Nguyên tắc lưu trữ", ngoại lệ giữ vĩnh viễn bằng chứng consent)) — App không cần xử lý gì thêm cho việc này, chỉ cần đảm bảo gọi log đúng 1 lần khi khách tick.

## Acceptance Criteria

- [ ] App sinh 1 `sessionId` duy nhất khi bắt đầu hành trình mở tài khoản, dùng xuyên suốt cho tới bước cuối cùng (không đổi giữa chừng).
- [ ] Cả 11 bước App-facing ở trên đều gọi log đúng thời điểm ngay sau khi bước đó hoàn tất (thành công hoặc thất bại).
- [ ] Màn hình đồng ý xử lý dữ liệu cá nhân: nút "Tiếp theo" ở trạng thái disabled khi checkbox chưa tick; enable ngay khi khách tick.
- [ ] Khách tick checkbox đồng ý xử lý dữ liệu cá nhân → App gọi log step `PERSONAL_DATA_PROCESSING_CONSENT` — verify được bằng cách tra thấy đúng 1 row cho step này, `status = SUCCESS`.
- [ ] Bước Quét khuôn mặt & CCCD gửi kèm đầy đủ ảnh (base64) và toàn bộ dữ liệu SDK trả về, không cắt bớt field.
- [ ] Bước Xác nhận điều khoản hợp đồng (bước 10) gửi kèm toàn văn nội dung điều khoản khách đã đọc, không chỉ cờ đồng ý.
- [ ] Việc gọi log không gây chậm trễ hoặc gián đoạn cảm nhận được cho khách hàng ở bất kỳ bước nào.
- [ ] QA verify được: sau khi 1 khách mở tài khoản thành công, tra được đầy đủ log của cả 11 bước App-facing tương ứng đúng `sessionId`, đúng thứ tự thời gian.

---

## Business rule chung cho cả Phần A + Phần B

- **Không chặn luồng chính:** việc gọi log (cả `attempt-log` và `journey-log`) không được làm chậm hoặc chặn luồng chính mở tài khoản — không hiển thị loading chờ log hoàn tất, không rollback bước hiện tại nếu request log thất bại.
- **Log cả khi bước thất bại:** nếu 1 bước/lần thử thất bại (khách nhập sai, SDK trả lỗi...), App vẫn phải gọi log cho bước đó với `status: FAILED` — mục tiêu là ghi lại toàn cảnh, kể cả chỗ khách bị chặn lại.
- **[MỚI] Retry khi mất kết nối mạng giữa chừng:** áp dụng cho **tất cả các màn hình/bước** ở cả Phần A và Phần B — nếu request log thất bại do mất kết nối mạng (không phải do BE trả lỗi nghiệp vụ), App phải **tự động retry** gửi lại log đó sau đó (không bỏ qua vĩnh viễn), để không mất dữ liệu compliance/audit. Retry chạy nền, không hiển thị gì cho khách, không chặn khách tiếp tục luồng chính. Chi tiết cơ chế retry (số lần, thời điểm — ví dụ: retry khi có mạng trở lại, hoặc khi App mở lại) do FE Lead quyết định khi implement, miễn đảm bảo log cuối cùng vẫn tới được BE.

---

## Cần confirm thêm

- [ ] FE Lead xác nhận effort thực tế cần sửa bao nhiêu màn hình cho cả 2 sub-feature (BE ước tính ~10-11 màn cho Phần B + các điểm gọi `attempt-log` ở luồng quét CCCD/khuôn mặt cho Phần A).
- [ ] PDPD/compliance xác nhận việc gửi ảnh sinh trắc học (base64) qua 2 log này có cần thêm biện pháp bảo vệ gì ở tầng App (ví dụ: chỉ gửi qua kết nối đã xác thực) hay không — xem giải thích PDPD ở PRD mục 6.
- [ ] Xác nhận link/văn bản chính xác của "Điều khoản và điều kiện xử lý dữ liệu cá nhân" để App hiển thị đúng trên màn hình (chỉ cần cho UI hiển thị — payload log không cần gửi kèm, xem "Đơn giản hoá 2026-07-20" ở trên).

---

**Document Status:** 📋 Pending | For: FE Lead, App Dev (nhsv-mts-rn) | Next Steps: FE Lead confirm effort → chờ BE endpoint `/ekycs/attempt-log` (Phần A) + `/ekycs/journey-log` (Phần B) live → App implement gọi log tại tất cả điểm nêu trên, kèm retry khi mất mạng
