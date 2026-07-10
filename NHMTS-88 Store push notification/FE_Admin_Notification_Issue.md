# [FE] Admin Portal — Gửi thông báo tới app NHSV Pro (NHMTS-88)

**Document Type:** FE Issue
**Jira:** NHMTS-88 (Epic NHMTS-74 — Notifications)
**Repo:** `nhsv-admin-fe` (Vite + React + Ant Design)
**Spec liên quan:** `Admin_Notification_API_Spec.md` (cùng folder)
**Date:** 2026-07-09

---

## User Story

> Là **admin NHSV**, tôi muốn **soạn và gửi push notification (khuyến mãi, tin tức, báo cáo, nhắc nhở) tới toàn bộ người dùng app NHSV Pro ngay trong Admin Portal**, để **không phải đăng nhập OneSignal Dashboard mỗi lần cần gửi thông báo**.

---

## Bối cảnh

- Hiện tại admin muốn gửi notification phải thao tác trên OneSignal Dashboard — ngoài hệ thống NHSV, không có audit trail nội bộ, dễ gửi nhầm.
- Phase 1 chỉ gửi cho **Tất cả subscriber** — UI không có lựa chọn segment (đã chốt, xem spec).
- BE cung cấp 4 API admin-facing (send / cancel / danh sách / chi tiết) — xem spec Section "API — Admin-facing".

---

## Yêu cầu chức năng

### 1. Menu & phân quyền

- Thêm mục **"Gửi thông báo"** vào menu sidebar của Admin Portal.
- Chỉ hiển thị và truy cập được với role **ADMIN / SUPER_ADMIN** (giống các trang quản trị hiện có: Quản lý người dùng, NH Research...).
- Trang gồm 2 tab (hoặc 2 view chuyển đổi): **"Soạn tin mới"** và **"Lịch sử đã gửi"**.

### 2. Luồng soạn tin mới — 4 bước

**Bước 1 — Chọn loại thông báo** (5 card chọn nhanh):

| Loại | Nhãn hiển thị | Có HTML content? |
|---|---|---|
| `PROMOTION` | 🏷️ Khuyến mãi | ✅ |
| `NEWS` | 📰 Tin tức | ✅ |
| `DAILY_REPORT` | 📊 Báo cáo ngày | ❌ |
| `REMINDER` | ⏰ Nhắc nhở | ❌ |
| `NORMAL` | 🔔 Mặc định | ❌ |

**Bước 2 — Soạn nội dung** (form động theo loại đã chọn):

- Khi chọn loại tin ở bước 1, form **prefill template song ngữ** tương ứng (bảng Template trong spec) — admin sửa placeholder rồi gửi, template chỉ là gợi ý.
- Title + Message: nhập song ngữ EN/VI — bắt buộc **ít nhất 1 ngôn ngữ** cho mỗi trường (validate trước khi cho qua bước 3).
- **Mọi loại tin** đều có: **Launch URL** (deeplink/web URL, tuỳ chọn) + **Nhãn nút CTA** song ngữ (tuỳ chọn — mặc định "Xem chi tiết"/"View details" khi có URL). Nhập nhãn CTA mà không có URL → báo lỗi tại field.
- Chỉ khi loại là PROMOTION/NEWS mới hiện thêm: **HTML Content** — nhập bằng **rich text HTML editor (WYSIWYG)**, KHÔNG phải textarea HTML thô. Yêu cầu editor: toolbar tối thiểu bold/italic/underline + chèn emoji, admin thấy trực tiếp kết quả định dạng khi gõ, output là HTML (thư viện do FE team chọn — vd Quill/TipTap, tương thích React 18 + Ant Design). Và **Image URL** (banner).
- Loại còn lại: hiển thị ghi chú "không có HTML/ảnh banner", vẫn có Launch URL + CTA.

**Bước 3 — Lịch gửi:**

- Đối tượng nhận hiển thị **cố định "Tất cả subscriber"** kèm số lượng ước tính — không cho chọn (ghi chú "chưa hỗ trợ chọn segment ở Phase 1").
- Chọn **"Gửi ngay"** hoặc **"Hẹn giờ"** (date-time picker; thời gian phải ở tương lai — validate).
- Priority hiển thị read-only, tự suy theo loại (Khuyến mãi/Tin tức = High, còn lại = Normal) — không chỉnh được.

**Bước 4 — Xác nhận & Gửi:**

- **Preview mô phỏng trên điện thoại có 2 chế độ chuyển đổi**: (a) **Push banner** — như notification trên lock screen; (b) **Thẻ trong app** — như tab Thông báo trong app (banner + title + rich body + nút CTA, style tham chiếu inbox SSI iBoard). Cập nhật live theo nội dung đã nhập từ bước 2.
- Bảng tóm tắt: loại, đối tượng, lịch gửi, priority.
- Nút "Gửi ngay" / "Lên lịch" → gọi `POST /api/v1/admin/notifications/send`.
- Gửi thành công: toast xác nhận (kèm số người nhận ước tính) → chuyển sang tab "Lịch sử đã gửi".
- Gửi thất bại (422 `NOTIFICATION_SEND_FAILED`): hiển thị lỗi rõ ràng, **giữ nguyên nội dung đã soạn** để admin thử lại — không reset form.

> Tham khảo prototype: bản demo HTML 4 bước + preview phone đã được PO duyệt luồng (demo đính kèm khi tạo Jira ticket).

### 3. Màn "Lịch sử đã gửi"

- Bảng danh sách từ `GET /api/v1/admin/notifications`, cột: Loại (icon + tên) · Tiêu đề (VI) · Trạng thái (badge "Đã gửi" xanh / "Đang chờ" cam / "Đã hủy" xám) · Người nhận (ước tính) · Thời gian (sentAt hoặc scheduledAt) · Người gửi.
- Bộ lọc: theo Loại (`type`) và Trạng thái (`status` = SENT/SCHEDULED/CANCELLED).
- Phân trang theo chuẩn `fetchCount`/`nextKey` của API.
- Click 1 dòng → popup chi tiết (`GET /api/v1/admin/notifications/{id}`): title/body đầy đủ 2 ngôn ngữ, HTML content render an toàn, ảnh, deeplink, người gửi, thời gian — kèm preview mô phỏng như bước 4. Tin đã hủy hiển thị thêm người hủy + thời gian hủy.

### 4. Hủy tin đã hẹn giờ

- Tin trạng thái **"Đang chờ"** (SCHEDULED) có nút **"Hủy gửi"** trong popup chi tiết (và/hoặc nút nhanh trên dòng của bảng).
- Bấm Hủy gửi → **dialog xác nhận** (hiển thị tiêu đề tin + giờ hẹn gửi) → xác nhận → gọi `PUT /api/v1/admin/notifications/{id}/cancel`.
- Thành công: badge chuyển "Đã hủy" (xám), toast xác nhận. Tin SENT/CANCELLED **không** có nút hủy.
- Lỗi `NOTIFICATION_ALREADY_SENT` (bấm hủy sát giờ gửi, tin đã đi): hiển thị thông báo "Tin đã được gửi, không thể hủy" và refresh lại trạng thái dòng đó thành "Đã gửi".

### 5. NH Research — trigger tự động (điểm chạm với trang NH Research hiện có)

- Trang NH Research đã có toggle "Gửi push notification khi publish" (theo spec NHSV Channel). Khi publish thành công + toggle ON, **BE tự gửi push** — FE trang NH Research không gọi thêm API gửi nào.
- FE cần xử lý 2 trạng thái trả về từ API publish: (a) publish + push đều OK → toast bình thường; (b) publish OK nhưng push thất bại → hiển thị cảnh báo "Bài đã đăng nhưng gửi thông báo thất bại — có thể gửi lại từ màn Gửi thông báo".
- Tin tự động cũng xuất hiện trong "Lịch sử đã gửi" như tin gửi tay.

### 6. Xử lý lỗi & trạng thái

- 400 `INVALID_PARAMETER`: map `params[].param` về đúng field trên form để highlight lỗi tại chỗ.
- 401: theo cơ chế redirect login chung của Admin Portal hiện tại.
- Loading state khi gọi API gửi (nút disable + spinner) — chống double-submit.

---

## Tiêu chí chấp nhận (AC)

- [ ] Menu "Gửi thông báo" chỉ hiện với ADMIN/SUPER_ADMIN; role khác truy cập trực tiếp URL bị chặn.
- [ ] Không thể qua bước 3 nếu thiếu cả Title EN+VI hoặc cả Message EN+VI.
- [ ] Field HTML Content/Image URL **chỉ** xuất hiện với loại PROMOTION/NEWS; Launch URL + nhãn CTA xuất hiện với **mọi** loại.
- [ ] HTML Content nhập qua WYSIWYG editor: bôi đen + bấm Bold thấy chữ đậm ngay trong editor (không thấy tag `<b>` thô); nội dung định dạng phản ánh đúng sang preview "Thẻ trong app".
- [ ] Nhập nhãn CTA mà không có Launch URL → báo lỗi tại field, không gọi API.
- [ ] Preview chuyển đổi được giữa 2 chế độ Push banner / Thẻ trong app; thẻ trong app hiển thị đúng banner + rich body + nút CTA với nhãn đã nhập.
- [ ] Hẹn giờ với thời gian trong quá khứ → báo lỗi tại field, không gọi API.
- [ ] Preview ở bước 4 khớp đúng nội dung đã nhập (title/body ưu tiên bản VI, fallback EN).
- [ ] Gửi thành công hiện toast + record mới xuất hiện đầu danh sách "Lịch sử đã gửi" với đúng trạng thái (SENT/SCHEDULED).
- [ ] Gửi thất bại: hiện thông báo lỗi, nội dung form không bị mất.
- [ ] Lọc lịch sử theo Loại và Trạng thái (gồm cả "Đã hủy") hoạt động độc lập và kết hợp được.
- [ ] Popup chi tiết hiển thị đầy đủ 2 ngôn ngữ + HTML content render đúng (không vỡ layout, không thực thi script).
- [ ] Double-click nút gửi không tạo 2 notification.
- [ ] Chọn loại tin → form prefill đúng template song ngữ của loại đó; đổi loại tin → prefill lại theo loại mới.
- [ ] Nút "Hủy gửi" chỉ hiện với tin Đang chờ; hủy thành công → badge "Đã hủy", tin biến mất khỏi filter "Đang chờ".
- [ ] Hủy tin đã gửi (race sát giờ) → hiển thị "Tin đã được gửi, không thể hủy" + trạng thái dòng cập nhật thành "Đã gửi".
- [ ] Publish NH Research với toggle ON → push tự động xuất hiện trong "Lịch sử đã gửi"; push fail → cảnh báo đúng thông điệp, bài viết vẫn publish.

---

## Ngoài phạm vi (Phase 1)

- Chọn segment / danh sách user nhận cụ thể (API đã chừa sẵn `audienceType` nhưng UI không hiển thị).
- **Sửa nội dung** notification đã tạo (hủy tin hẹn giờ thì ĐÃ trong scope — xem mục 4; muốn đổi nội dung tin hẹn giờ: hủy rồi tạo lại).
- Thu hồi tin **đã gửi** (push đã tới máy user — không thu hồi được, giới hạn của push notification).
- Action buttons, Collapse ID.

---

## Ghi chú cho Developer

- ⚠️ **Chốt convention API với BE trước khi code:** spec định nghĩa `POST /api/v1/admin/notifications/send` (camelCase, response TradeX format), nhưng các API admin hiện có trong `nhsv-admin` (vd NH Research) đang dùng base `/api/admin/<kebab-case>` + envelope `GenericResponse { status, message, data, pageData }`. Xem Open Question Q4 trong spec — FE cần biết đáp án để viết API client đúng ngay từ đầu.
- Luồng soạn tin song ngữ EN/VI đã có pattern tương tự ở trang NH Research (tab chuyển ngôn ngữ) — nên thống nhất trải nghiệm.

---

Document Status: 📋 Draft | For: FE Developer (Admin Portal) | Next Steps: Midu review → chốt Q4 (convention API) với BE Lead → tạo Jira subtask FE dưới NHMTS-88
