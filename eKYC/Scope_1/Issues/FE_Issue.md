# FE Issue: App eKYC — Ghi nhận Compliance Journey Log

**App:** NHSV Pro (nhsv-mts-rn)
**Priority:** High
**Type:** Instrumentation (không có UI mới, khách hàng không thấy thay đổi)
**Spec liên quan:** `../Specifications/BE_Spec.md` (Phần B)
**Blocked by:** BE endpoint `POST /api/v1/ekycs/journey-log` phải live trước (xem `BE_Issue.md` Phần B)

---

## Bối cảnh

Khi có tranh chấp hoặc audit compliance về một tài khoản mở qua eKYC, hiện không có cách nào tái dựng lại chính xác những gì khách hàng đã thấy và xác nhận ở từng bước trong hành trình — nhiều thông tin (nghề nghiệp, mã số thuế, toàn văn nội dung điều khoản khách đã đồng ý) chỉ tồn tại tạm thời trên App và không được gửi lên hệ thống nào cho tới bước nộp hồ sơ cuối cùng. BE đã chuẩn bị 1 API log riêng để nhận đầy đủ dữ liệu này tại đúng thời điểm từng bước xảy ra.

---

## User Story

> As a compliance/PO, tôi muốn mỗi bước quan trọng trong hành trình mở tài khoản qua eKYC được ghi lại đầy đủ dữ liệu khách đã nhập/xác nhận tại đúng thời điểm đó, để khi có tranh chấp hoặc audit, có thể tái dựng chính xác toàn bộ hành trình của khách hàng.

---

## Yêu cầu chức năng

App gọi API `POST /api/v1/ekycs/journey-log` ngay khi khách hoàn tất mỗi bước dưới đây — **kể cả khi bước đó thất bại** (gửi `status: FAILED` kèm lý do nếu có), không chỉ gọi lúc thành công. Mọi lần gọi dùng chung 1 `sessionId` do App tự sinh khi khách bắt đầu hành trình, giữ nguyên xuyên suốt tới bước cuối.

1. **Gửi OTP xác thực SĐT** — ngay sau khi App gửi OTP thành công.
2. **Xác thực OTP** — ngay sau khi khách nhập đúng mã OTP.
3. **Xem hướng dẫn chụp CCCD** — sau khi khách đã nhập SĐT, email, quốc tịch, nghề nghiệp và xem xong hướng dẫn — gửi kèm toàn bộ các field này.
4. **Quét khuôn mặt & CCCD** — ngay sau khi nhận đầy đủ kết quả từ VNPT SDK (OCR, xác minh khuôn mặt trực tiếp, so khớp khuôn mặt) — gửi **toàn bộ** dữ liệu SDK trả về bao gồm cả ảnh (base64), không lọc bớt field nào.
5. **Xác nhận thông tin cá nhân** — sau khi khách xác nhận ngày sinh, địa chỉ, thông tin FATCA...
6. **Thông tin tài khoản** — loại tài khoản, chi nhánh, các cờ margin/phái sinh khách chọn.
7. **Thông tin ngân hàng** — tài khoản ngân hàng nhận tiền khách khai.
8. **Thông tin đầu tư** — mục tiêu đầu tư, khẩu vị rủi ro khách chọn.
9. **Xác nhận điều khoản hợp đồng** — ngay khi khách tick "Tôi đã đọc và đồng ý" — gửi kèm **toàn văn** nội dung điều khoản khách đã đọc, không chỉ trạng thái đồng ý.
10. **Hoàn tất mở tài khoản** — mốc cuối cùng của hành trình, đánh dấu hành trình đã thành công.

**Không cần App xử lý gì thêm cho bước ký hợp đồng điện tử** — bước này (`ECONTRACT_SIGN_COMPLETED`) do BE tự ghi nhận qua webhook FPT khi khách ký xong, độc lập với phiên làm việc của App.

**Business rule:**
- Việc gọi log **không được** làm chậm hoặc chặn luồng chính mở tài khoản — không hiển thị loading chờ log hoàn tất, không rollback bước hiện tại nếu request log thất bại.
- Nếu 1 bước thất bại (khách nhập sai, SDK trả lỗi...), App vẫn phải gọi log cho bước đó với `status: FAILED` — mục tiêu là ghi lại toàn cảnh hành trình, kể cả chỗ khách bị chặn lại.

---

## Acceptance Criteria

- [ ] App sinh 1 `sessionId` duy nhất khi bắt đầu hành trình mở tài khoản, dùng xuyên suốt cho tới bước cuối cùng (không đổi giữa chừng).
- [ ] Cả 9 bước App-facing ở trên đều gọi log đúng thời điểm ngay sau khi bước đó hoàn tất (thành công hoặc thất bại).
- [ ] Bước Quét khuôn mặt & CCCD gửi kèm đầy đủ ảnh (base64) và toàn bộ dữ liệu SDK trả về, không cắt bớt field.
- [ ] Bước Xác nhận điều khoản gửi kèm toàn văn nội dung điều khoản khách đã đọc, không chỉ cờ đồng ý.
- [ ] Việc gọi log không gây chậm trễ hoặc gián đoạn cảm nhận được cho khách hàng ở bất kỳ bước nào.
- [ ] Request log thất bại (lỗi mạng, BE trả lỗi...) không làm gián đoạn hành trình mở tài khoản của khách — khách không thấy bất kỳ thông báo lỗi nào liên quan tới việc log.
- [ ] QA verify được: sau khi 1 khách mở tài khoản thành công, tra được đầy đủ log của cả 10 bước App-facing tương ứng đúng `sessionId`, đúng thứ tự thời gian.

---

## Cần confirm thêm

- [ ] FE Lead xác nhận effort thực tế cần sửa bao nhiêu màn hình (BE ước tính ~9 màn dựa trên luồng hiện tại của App).
- [ ] Hành vi khi mất kết nối mạng giữa chừng 1 bước — có cần retry log sau đó không, hay bỏ qua vĩnh viễn cho bước đó?
- [ ] PDPD/compliance xác nhận việc gửi ảnh sinh trắc học (base64) qua log riêng này có cần thêm biện pháp bảo vệ gì ở tầng App (ví dụ: chỉ gửi qua kết nối đã xác thực) hay không.

---

**Document Status:** 📋 Pending | For: FE Lead, App Dev (nhsv-mts-rn) | Next Steps: FE Lead confirm effort → chờ BE endpoint `/ekycs/journey-log` live → App implement gọi log tại 9 bước
