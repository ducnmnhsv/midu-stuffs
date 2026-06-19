# PRD — NH Research Content Tab (A-04)

**Product:** NHSV Pro · Mobile App  
**Feature ID:** A-04  
**PM:** Midu (Nguyễn Minh Đức)  
**Status:** 📋 Draft  
**Version:** 1.0 · 2026-06-16

---

## 1. Bối cảnh & Vấn đề

Phòng Phân tích NHSV sản xuất đều đặn các báo cáo phân tích thị trường và doanh nghiệp — nhưng hiện tại kênh phân phối duy nhất là email nội bộ và trang web NHSV. User của NHSV Pro không có cách xem báo cáo này trực tiếp trong app.

Hậu quả: nội dung phân tích chất lượng cao do NHSV sản xuất không tiếp cận được user đúng lúc họ đang trading, và team Phân tích mất đi một kênh phân phối hiệu quả đến tệp khách hàng của mình.

Qua khảo sát cạnh tranh với KIS, KBSV, và SSI, cả ba đối thủ đều có tab nội dung phân tích in-app với PDF viewer. NHSV Pro hiện chưa có.

---

## 2. Mục tiêu

| Mục tiêu | Chỉ số đo lường |
|---|---|
| User tiếp cận được báo cáo NHSV ngay trong app | % user active xem ít nhất 1 bài/tháng sau release |
| Giảm thời gian Phòng Phân tích publish nội dung | Thời gian từ lúc có file PDF → user thấy trên app (mục tiêu: < 5 phút) |
| Tăng session length trên NHSV Pro | So sánh avg. session duration trước/sau 30 ngày release |

Mục tiêu phụ: chuẩn bị nền tảng nội dung để kích hoạt push notification (phụ thuộc X-03), tăng daily open rate.

---

## 3. Đối tượng người dùng

**Primary:** Retail investor dài hạn, quan tâm phân tích cơ bản. Đây là nhóm user cao cấp của NHSV Pro — thường là khách hàng đã mở tài khoản, có danh mục đang hold.

**Secondary:** Day trader muốn nắm bắt diễn biến thị trường và tin tức doanh nghiệp để ra quyết định ngắn hạn.

---

## 4. Phạm vi tính năng (In Scope)

### 4.1 Mobile App — Trải nghiệm user

**Tab NH Research trong NHSV Channel**

Tab này nằm trong Kênh tin tức NHSV, thay thế vị trí hiện tại của Tin tức. Sau khi A-03 (Tab Reorder) hoàn thành, thứ tự tab sẽ là: Khuyến nghị → NH Research → Tin tức → Thị trường.

Trong tab NH Research, user thấy 3 bộ lọc danh mục: Thị trường (mặc định), Doanh nghiệp, Vĩ mô. Mỗi danh mục tương ứng một call API riêng để lọc kết quả.

**Danh sách bài viết**

Mỗi bài viết hiển thị dạng card với: nhãn danh mục (màu sắc phân biệt từng loại), ngày publish, tiêu đề đậm, và đoạn mô tả ngắn. Nếu bài có đính kèm PDF, card hiển thị thêm chỉ báo file.

User tap card → mở màn hình chi tiết bài viết. Danh sách tải tiếp khi scroll đến cuối (infinite scroll hoặc nút "Xem thêm" — Mobile team quyết định).

**Chi tiết bài viết và PDF Viewer**

Màn hình chi tiết hiển thị toàn bộ nội dung bài viết. Nếu có PDF, user thấy tên file + dung lượng + nút "Mở xem". Tap vào mở PDF Viewer in-app hỗ trợ pinch-to-zoom và điều hướng trang. PDF được cache trong session — mở lần hai không tải lại.

**Push Notification (phụ thuộc X-03)**

Khi Phòng Phân tích publish bài mới, user nhận notification. Tap vào notification deeplink thẳng vào tab NH Research (`nhsvpro://channel/nh-research?category={category}`). Tính năng này chỉ available sau khi X-03 (Push Notification Infrastructure) hoàn thành.

### 4.2 Admin Tool — Phòng Phân tích

**Upload và publish bài viết**

Người dùng Admin Tool (Phòng Phân tích) có thể tạo bài viết mới bằng cách điền tiêu đề, chọn danh mục, nhập nội dung, và tùy chọn đính kèm file PDF. Bài viết được publish ngay khi submit (không có approval workflow trong v1).

**Quản lý bài đã đăng**

Danh sách bài viết có bộ lọc theo danh mục và trạng thái. Admin có thể ẩn bài (disable) mà không cần xóa, hoặc xóa mềm khi cần. Chỉnh sửa bài đã đăng được hỗ trợ — bao gồm thay file PDF.

**Publish & Notify (phụ thuộc X-03)**

Khi tạo hoặc chỉnh sửa bài, form có toggle "Gửi push notification". Mặc định là bật. Khi publish, hệ thống tự gửi notification đến toàn bộ user opted-in và hiển thị xác nhận số thiết bị đã nhận.

---

## 5. Ngoài phạm vi (Out of Scope)

- Phân quyền user trong Admin Tool (xem Open Question 4) — dùng rule hiện có của nhsv-admin
- Notification Settings cho user (màn hình opt-in/opt-out) — sẽ làm sau trong sprint riêng
- Like, comment, share bài viết — backlog
- Stock tag enrichment (gắn mã CK vào bài và hiển thị % thay đổi) — backlog, A-06
- Tìm kiếm trong NH Research — A-07 (Search feature)
- Approval workflow cho bài viết — v1 publish ngay, không duyệt

---

## 6. User Stories (tóm tắt)

| ID | Vai trò | Nhu cầu | Lý do |
|---|---|---|---|
| US-01 | Investor | Xem danh sách báo cáo phân tích theo chủ đề | Tìm nhanh báo cáo liên quan đến mã đang quan tâm |
| US-02 | Investor | Đọc nội dung bài viết đầy đủ trong app | Không cần thoát sang trình duyệt hay email |
| US-03 | Investor | Xem PDF báo cáo mà không cần tải về máy | Tiết kiệm dung lượng, xem ngay khi cần |
| US-04 | Investor | Nhận thông báo khi có báo cáo mới | Không bỏ lỡ báo cáo quan trọng |
| US-05 | Analyst (Admin) | Upload bài + PDF và publish ngay | Phân phối nội dung nhanh, không phụ thuộc IT |
| US-06 | Analyst (Admin) | Ẩn hoặc chỉnh sửa bài đã đăng | Cập nhật khi có thông tin mới hoặc sai sót |

---

## 7. Yêu cầu phi chức năng

| Hạng mục | Yêu cầu |
|---|---|
| Hiệu năng API | Danh sách bài viết load < 2 giây ở điều kiện mạng 4G |
| PDF load | Trang đầu tiên hiển thị < 3 giây (file trung bình ~3 MB) |
| Cache | PDF cache trong session; không tải lại khi mở lần 2 trong cùng session |
| Error handling | Mọi lỗi API đều có UI phản hồi rõ ràng (empty state, error state, retry) |
| Offline | Hiển thị cache nếu có; nếu không có cache, hiển thị thông báo mất kết nối |
| Storage | Dung lượng tối đa per file PDF: 20 MB (xác nhận với IT — xem Open Q #2) |
| Accessibility | Font size và contrast theo NHSV Pro Design System — không vi phạm WCAG AA |

---

## 8. API Overview

Chi tiết kỹ thuật đầy đủ trong `jira-issues.md`. Tóm tắt endpoints:

| Endpoint | Dùng bởi | Mục đích |
|---|---|---|
| `GET /api/v1/nhResearch/articles` | Mobile | Danh sách bài (có filter category, pagination) |
| `GET /api/v1/nhResearch/articles/{articleId}` | Mobile | Chi tiết bài viết + PDF metadata |
| `GET /admin/nhResearch/articles` | Admin FE | Quản lý danh sách (all statuses) |
| `POST /admin/nhResearch/articles` | Admin FE | Tạo bài mới |
| `PUT /admin/nhResearch/articles/{id}` | Admin FE | Chỉnh sửa / đổi visibility |
| `DELETE /admin/nhResearch/articles/{id}` | Admin FE | Soft delete |
| `POST /admin/nhResearch/upload/pdf` | Admin FE | Upload file PDF, trả về URL |

Danh mục (category): `MARKET` / `COMPANY` / `MACRO` — tương ứng "Thị trường" / "Doanh nghiệp" / "Vĩ mô" trên UI.

---

## 9. Phụ thuộc

| Phụ thuộc | Loại | Ghi chú |
|---|---|---|
| A-03 — Tab Reorder | Trước | NH Research cần đúng vị trí trong tab bar |
| X-01 — Admin Tool | Cùng sprint | Admin FE build trong Admin Tool |
| X-03 — Push Notification | Cùng sprint | Publish & Notify toggle chỉ hoạt động khi X-03 xong |
| Storage service (S3 hoặc NHSV server) | Infra | Quyết định trước khi bắt đầu BE-07 |

---

## 10. Open Questions

| # | Câu hỏi | Tác động | Owner | Deadline |
|---|---|---|---|---|
| Q1 | Nội dung `shortContent` giới hạn bao nhiêu ký tự? Hay không giới hạn và mobile tự cắt? | UX + DB schema | PM + BE | Trước khi BE-01 |
| Q2 | Storage provider cho PDF: S3, MinIO, hay server NHSV tự host? Max file size? | BE-07 + BE-08 scope | IT Lead | Trước khi BE-07 |
| Q3 | URL PDF là public URL hay signed URL có TTL? | Security + UX (PDF cache) — **blocker cho MOB-05**: mobile cần biết TTL để quyết định cache strategy | IT Lead | Trước khi BE-03 và MOB-05 |
| Q4 | Role nào trong nhsv-admin được phép upload NH Research? Phòng Phân tích có role riêng chưa? | Admin FE + BE access control | IT + Admin | Trước khi ADM-01 |

---

## 11. Estimate sơ bộ

| Layer | Stories | Ghi chú |
|---|---|---|
| Backend | 8 stories | Chi tiết trong `jira-issues.md` (BE-01 → BE-08) |
| Mobile FE | 6 stories | MOB-01 → MOB-06; PDF viewer có thể dùng native lib |
| Admin FE | 5 stories | ADM-01 → ADM-05; UI reference tại `Admin_Tool/admin-demo.html` |

Estimate giờ: cần IT team tự estimate sau khi Q1–Q4 được confirm. Blocker lớn nhất là Q2 (storage) và X-03 (push notification).

---

## 12. Tiêu chí release

Tính năng được coi là ready to release khi:

Về phía Mobile App: user xem được danh sách bài viết theo danh mục, đọc chi tiết bài, và mở PDF in-app mà không cần thoát sang trình duyệt.

Về phía Admin Tool: Phòng Phân tích upload và publish bài mới trong vòng 5 phút, không cần nhờ IT.

Về phía Push Notification: nếu X-03 chưa xong, feature vẫn release được — chỉ bỏ toggle Publish & Notify. Push sẽ bật sau khi X-03 merge.

---

Document Status: 📋 Draft | For: IT Lead, Mobile FE, Admin FE, Phòng Phân tích | Next Steps: Confirm Open Questions Q1–Q4 trước khi bắt đầu estimate sprint
