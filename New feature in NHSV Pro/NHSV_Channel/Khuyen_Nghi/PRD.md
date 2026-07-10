# PRD — Khuyến nghị Tab (A-05)

**Product:** NHSV Pro · Mobile App
**Feature ID:** A-05
**PM:** Midu (Nguyễn Minh Đức)
**Status:** 📋 Draft
**Version:** 1.1 · 2026-07-10

---

## 1. Bối cảnh & Vấn đề

Phòng Phân tích NHSV và Phòng QTRR sản xuất đều đặn 3 loại nội dung khuyến nghị giá trị cho nhà đầu tư: **danh mục cổ phiếu khuyến nghị dài hạn** (giá kỳ vọng, % tiềm năng), **bảng đánh giá rating cổ phiếu** (S/A/B/C/D, dùng cho margin), và **khuyến nghị kỹ thuật ngắn hạn** (vùng mua, mục tiêu, cắt lỗ). Hiện tại các nội dung này chỉ phân phối qua email và website — không có trên NHSV Pro.

Hệ quả: (1) Khách hàng có tài khoản NHSV nhưng không thấy giá trị tư vấn của NHSV ngay trong app khi đang trading. (2) Phòng PT và Phòng QTRR không tận dụng được kênh phân phối in-app. (3) NHSV thua thiệt so với các CTCK hàng đầu (SSI, KBSV, VNDIRECT, TCBS, MBS) — đều đã có tab khuyến nghị trên mobile app.

Tab Khuyến nghị giải quyết: tập hợp 3 luồng nội dung khuyến nghị vào 1 entry point thống nhất trong NHSV Channel, với UX scannable, tabular, có disclaimer pháp lý rõ ràng.

---

## 2. Mục tiêu

| Mục tiêu | Chỉ số đo lường |
|---|---|
| Đưa giá trị tư vấn NHSV đến user ngay khi đang trading | % user active mở tab Khuyến nghị ít nhất 1 lần/tuần sau release |
| Giảm thời gian Phòng PT publish khuyến nghị mới | Từ lúc có khuyến nghị → user thấy trên app (mục tiêu: < 5 phút cho Danh mục/Kỹ thuật, < 30 phút cho Rating) |
| Tăng tin cậy & độ trust của NHSV trong mắt khách hàng | NPS survey sau 60 ngày release |
| Chuẩn bị nền tảng cho automation Vietstock | Field `dataSource` ready từ v1, sẵn sàng v2 mà không cần migration |

Mục tiêu phụ: tăng daily active rate trên NHSV Pro, giảm churn khách hàng dài hạn.

---

## 3. Đối tượng người dùng

**Primary:** Retail investor dài hạn (giá trị danh mục > 100M VND) — quan tâm đến phân tích cơ bản và rating chất lượng. Đây là nhóm khách hàng cao cấp, thường đã có nhiều tài khoản môi giới và so sánh giữa các CTCK.

**Secondary:** Day trader / swing trader — chủ yếu sử dụng tab Kỹ thuật hằng ngày để tham khảo vùng mua/cắt lỗ trong phiên.

**Tertiary:** Nhà đầu tư mới — dùng rating S/A/B/C/D như filter đầu vào để lọc danh sách cổ phiếu khi chưa có kinh nghiệm phân tích.

---

## 4. Phạm vi tính năng (In Scope)

### 4.1 Mobile App — Trải nghiệm user

**Tab Khuyến nghị trong NHSV Channel**

Thay thế vị trí "Tin tức tổng hợp" hiện tại. Sau khi A-03 (Tab Reorder) hoàn thành, thứ tự tab trong Kênh NHSV sẽ là: **Khuyến nghị → NH Research → Tin tức → Thị trường**.

Bên trong tab Khuyến nghị có 3 sub-tab (secondary tabs với underline #028D96): **Danh mục cơ bản** (mặc định) → **NHSV Rating** → **Kỹ thuật hằng ngày**.

**Sub-tab 1 — Danh mục cơ bản**

Danh sách cổ phiếu được Phòng Phân tích khuyến nghị mua nắm giữ dài hạn. User thấy 3 filter pill (Tất cả mặc định / Bluechip / Midcap) và icon search.

Mỗi card hiển thị: mã CK (lớn, đậm), tên công ty, sàn (HOSE/HNX/UPCOM), % tiềm năng (xanh nếu dương, đỏ nếu âm), grid 3 ô (Giá hiện tại / Giá kỳ vọng / Ngày KN), segment badge (BLUECHIP/MIDCAP) và nút "Báo cáo" nếu có PDF đính kèm.

% tiềm năng được tính trên app real-time = (targetPrice / currentPrice - 1) × 100. Giá hiện tại được BE enrich từ market data service (snapshot phiên là đủ — không cần realtime tick). Ngoài giờ giao dịch: hiển thị giá đóng cửa phiên gần nhất.

Mỗi khuyến nghị có thể được set **ngày hết hiệu lực** (`expiryDate`) riêng theo từng mã — optional, không bắt buộc (Q1 resolved 2026-07-10). Không set = khuyến nghị không tự hết hạn, admin tự tay Archive. Có set = hệ thống tự động chuyển status → ARCHIVED sau ngày hết hiệu lực (scheduled job chạy hàng ngày), không cần admin can thiệp.

Tap card → push navigate sang màn chi tiết khuyến nghị (hero card với code tile, price compare card, big % potential block màu xanh, luận điểm đầu tư, info card ngày KN, PDF attachment, CTA "Xem cổ phiếu {code}" để navigate sang stock detail screen).

**Sub-tab 2 — NHSV Rating**

Bảng đánh giá rating cổ phiếu do Phòng QTRR cung cấp định kỳ (~1 lần/tháng), dùng làm cơ sở cho danh mục ký quỹ NHSV. Search bar luôn visible trên đầu (search theo mã CK).

Bảng 4 cột: **Mã CK** | **Cơ bản** | **TT (Thị trường)** | **Tổng**. Rating badge 5 mức:
- **S** — Xuất sắc — `#FFF4D1` bg + `#B47800` text
- **A** — Tốt — `#E6F6EF` bg + `#07A461` text
- **B** — Trung bình — `#E3F2FD` bg + `#23A3E9` text
- **C** — Kém — `#FFF3E0` bg + `#C76E00` text
- **D** — Yếu — `#FFEBE6` bg + `#DA1004` text

Sort mặc định: Rating tổng giảm dần. Tap header để sort theo cột khác. Tap row → navigate thẳng sang stock detail screen (KHÔNG có rating detail riêng).

Header có badge "Cập nhật: MM/YYYY" lấy từ `batchUpdatedAt` (ngày upload batch active). Cuối màn hiển thị legend giải thích 5 mức rating.

**Sub-tab 3 — Kỹ thuật hằng ngày**

Khuyến nghị kỹ thuật ngắn hạn (vài ngày → vài tuần) cho từng phiên giao dịch. Default load entries của ngày hôm nay; nếu Phòng PT chưa upload **hoặc đã nhập nhưng chưa Publish** (xem workflow Draft → Publish ở mục 4.2) → empty state với icon đồng hồ + thông điệp "Phòng Phân tích thường cập nhật khuyến nghị kỹ thuật trong phiên sáng. Vui lòng quay lại sau."

Mỗi card: mã CK + tên + sàn (left), badge "MUA +X%" nổi bật xanh `#07A461` (right). Bên dưới là grid 2×2:
- **Vùng mua** (xanh nhạt `#E6F6EF`) — range "75,50 – 76,50" nếu có 2 mức, hoặc "≥ X" nếu chỉ 1 mức
- **Mục tiêu** (teal nhạt `#E0F7F4`) — target price
- **Cắt lỗ** (đỏ nhạt `#FFEBE6`) — stop loss
- **Upsize** (xám nhạt `#F2F6FB`) — optional, ẨN ô nếu null (không hiển thị placeholder)

Tap card → màn chi tiết với hero (mã + tech grid), phân tích kỹ thuật (textarea từ admin), info card, PDF attachment (nếu có), CTA xem cổ phiếu.

**Search**

Cả 3 sub-tab đều có search theo mã CK. Search highlight substring (ví dụ user gõ "VC" → highlight phần "VC" trong VCB/VCG/VCI). Debounce 250ms.

**Disclaimer (bắt buộc pháp lý)**

Mọi screen của tab Khuyến nghị đều phải có footer disclaimer ở cuối list. Wording bên dưới là **draft đã bổ sung 3 yếu tố pháp lý chuẩn** còn thiếu ở bản v1.0 (không phải lời chào mua/bán, không phải tư vấn đầu tư, miễn trừ trách nhiệm cho NHSV) — vẫn cần Legal duyệt chính thức (xem Q6):
- Danh mục cơ bản & Kỹ thuật: "Nội dung khuyến nghị do Phòng Phân tích NHSV cung cấp, chỉ mang tính chất tham khảo, không phải là lời chào mua/bán hoặc tư vấn đầu tư. NHSV không chịu trách nhiệm đối với quyết định giao dịch dựa trên thông tin này. Quý khách cần tự đánh giá và cân nhắc kỹ trước khi thực hiện giao dịch."
- NHSV Rating: "Rating do Phòng Quản trị Rủi ro NHSV xây dựng, cập nhật định kỳ hàng tháng, dùng cho mục đích quản lý danh mục ký quỹ, chỉ mang tính chất tham khảo và không phải là tư vấn đầu tư. NHSV không chịu trách nhiệm đối với quyết định giao dịch dựa trên thông tin này."

### 4.2 Admin Tool — Phòng Phân tích & Phòng QTRR

URL gốc: `tnhsvpro.nhsv.vn/nhsv-admin`. Menu Khuyến nghị có 3 sub-page tương ứng 3 sub-tab trên mobile.

**Admin 1 — Danh mục cơ bản Management**

Table với filter Bluechip/Midcap + search mã + filter status (Active/Archived). Cột: Mã CK | Segment | Giá KN | Giá Kỳ vọng | Ngày KN | PDF | Trạng thái | Thao tác.

Nút "+ Thêm khuyến nghị" → form: Mã CK (autocomplete), Segment, Giá tại ngày KN, Giá kỳ vọng (auto-calculate % tiềm năng live), Ngày KN, Ngày hết hiệu lực (date picker, optional — để trống = không giới hạn thời gian), Luận điểm (textarea optional), Upload PDF (drag-drop optional).

Thao tác: Sửa | Archive (soft delete: status → ARCHIVED, không xóa cứng).

**Admin 2 — NHSV Rating Upload (Excel-based)**

Đây là flow đặc biệt của Khuyến nghị. Phòng QTRR gửi file Excel hàng tháng, admin upload thay vì nhập tay từng mã.

Flow:
1. Admin nhận file Excel từ Phòng QTRR (4 cột bắt buộc: `Mã`, `Cơ bản`, `Thị trường`, `NHSV Rating`. Giá trị: S/A/B/C/D case-insensitive)
2. Admin drag-drop file lên page → gọi `POST /admin/api/recommendations/ratingUpload`
3. BE parse → validate → return preview (10 rows đầu + total + error list nếu có)
4. Admin xem preview, nếu OK → bấm "Xác nhận publish" → gọi `POST /admin/api/recommendations/ratingPublish`
5. BE tạo batch mới, INSERT all records, SET `is_active=true` cho batch mới và `false` cho batch cũ (transaction nguyên tử)
6. App đọc batch mới ngay lập tức — không cần cache clear

Page hiển thị header "Batch hiện tại: MM/YYYY · N mã" + upload area + preview table + lịch sử các batch cũ (không xóa — để audit trail).

**Admin 3 — Kỹ thuật hằng ngày Management**

Date picker để chọn ngày (default: hôm nay) — **chọn được bất kỳ ngày trong quá khứ** (cập nhật 2026-07-10), không giới hạn hôm nay. Table hiển thị toàn bộ entry của ngày đã chọn, **gồm cả Nháp và đã Publish**. Table: Mã CK | Vùng mua | Mục tiêu | Cắt lỗ | Upsize | PDF | Nguồn | **Trạng thái** | Thao tác. Cột "Nguồn" có badge "MANUAL" (v1) hoặc "AUTO" (v2 — Vietstock automation).

Ngày không có entry nào (kể cả Nháp) → API trả `{"tradeDate":"...","totalCount":0,"technicals":[]}`, **HTTP 200** (không phải 404) — admin FE hiển thị empty state rõ ràng, không phải màn lỗi.

**Draft → Publish workflow (cập nhật 2026-07-10):** Admin không nhập từng mã một rồi hiện ngay trên app. Thay vào đó:
1. Bấm "+ Thêm entry" → mở form batch: chọn **1 Ngày áp dụng**, sau đó thêm **nhiều dòng mã** (mỗi dòng: Mã CK, Vùng mua from/to, Mục tiêu, Cắt lỗ, Upsize optional) qua "+ Thêm dòng".
2. Bấm "Lưu nháp" → toàn bộ mã vừa nhập lưu với trạng thái **Nháp (DRAFT)** — chưa hiển thị trên app, admin có thể tiếp tục sửa/thêm dòng khác cho cùng ngày.
3. Khi đã soạn xong cho ngày đó, bấm **"Publish ngày [ngày đang chọn]"** → toàn bộ entry Nháp của ngày đó chuyển **Còn hiệu lực (PUBLISHED)** cùng lúc — app đọc thấy ngay, không cần chờ.
4. Sửa từng dòng riêng (Luận điểm, PDF, số liệu) vẫn dùng được cho cả entry Nháp và đã Publish.

Delete: HARD DELETE (khác với Danh mục cơ bản — dữ liệu kỹ thuật ngắn hạn, không cần giữ lâu), áp dụng cho cả Nháp và đã Publish.

**Automation-ready**

Schema và endpoint đã accept `dataSource: 'VIETSTOCK_AUTO'` với `createdBy: null`. Khi pipeline Vietstock được build trong tương lai (v2), chỉ cần điểm POST vào API hiện có — không cần migration DB.

---

## 5. Ngoài phạm vi (Out of Scope)

- **Auto pipeline từ Vietstock** — schema ready, nhưng implementation pipeline để v2
- **Track record / accuracy rate** của từng khuyến nghị (đã đạt target chưa, % hit rate Phòng PT)
- **Alert push** khi giá đạt target price hoặc stop loss
- **Lịch sử khuyến nghị đã kết thúc** (chỉ hiển thị ACTIVE)
- **So sánh nhiều mã** cùng lúc
- **Portfolio P&L** dựa trên khuyến nghị (nếu user mua theo)
- **Approval workflow nhiều bước** — v1 admin publish ngay, không có review/duyệt
- **Notification settings** — sẽ làm trong sprint riêng (cùng với A-04 Notification Settings)
- **Rating chi tiết phân tích** — v1 chỉ hiển thị chữ cái + legend, không có break-down vì sao S/A/B/C/D

---

## 6. User Stories (tóm tắt)

| ID | Vai trò | Nhu cầu | Lý do |
|---|---|---|---|
| US-01 | Investor | Xem danh sách cổ phiếu NHSV khuyến nghị mua dài hạn | Tìm mã có tiềm năng cao theo Phòng PT |
| US-02 | Investor | Filter Bluechip/Midcap | Lọc theo risk profile cá nhân |
| US-03 | Investor | Xem giá hiện tại + % tiềm năng realtime | Quyết định mua khi % còn đủ hấp dẫn |
| US-04 | Investor | Đọc luận điểm + PDF báo cáo | Hiểu lý do Phòng PT khuyến nghị |
| US-05 | Investor | Xem rating S→D của cổ phiếu | Cross-check trước khi quyết định |
| US-06 | Day trader | Xem khuyến nghị kỹ thuật hôm nay với vùng mua / mục tiêu / cắt lỗ | Đặt lệnh theo plan của Phòng PT |
| US-07 | Investor | Tap mã CK → xem stock detail screen | Đặt lệnh ngay sau khi đọc khuyến nghị |
| US-08 | Analyst (Phòng PT) | Tạo nhanh khuyến nghị mới qua admin | Phân phối nội dung không phụ thuộc IT |
| US-09 | Analyst (Phòng PT) | Sửa khuyến nghị đã đăng + thay PDF | Cập nhật khi có info mới |
| US-10 | QTRR | Upload Excel batch rating mỗi tháng | Thay thế toàn bộ rating cũ trong 1 thao tác |

---

## 7. Yêu cầu phi chức năng

| Hạng mục | Yêu cầu |
|---|---|
| Hiệu năng API list | Danh mục/Rating/Kỹ thuật load < 2 giây ở mạng 4G |
| Price enrichment | Khi BE join market data: latency < 500ms cho 1 batch 20 records |
| PDF load | Trang đầu < 3 giây (file trung bình ~3 MB) |
| PDF cache | In-session — không tải lại lần 2 trong cùng session |
| Search debounce | Client-side 250ms cho cả 3 sub-tab |
| Excel parsing | File 500 mã parse < 3 giây ở admin BE |
| Error handling | Mọi lỗi API → UI rõ ràng (empty/error state + retry) |
| Offline | Hiển thị cache nếu có, không cache → thông báo mất kết nối |
| Storage PDF | Tái dùng pipeline upload PDF từ A-04 NH Research (cùng `/admin/api/upload/pdf`) |
| Accessibility | WCAG AA: contrast text ≥ 4.5:1, rating badge có chữ + màu (không color-only) |
| Tabular numbers | Bắt buộc `font-variant-numeric: tabular-nums` trên mọi cột giá/% |
| Locale | Vietnam: thousands `.` decimal `,` (vd: `31.800` và `+25,44%`) |
| Disclaimer | Hiển thị footer cuối mọi screen — không sticky, không thể tắt |

---

## 8. API Overview

Base URL Mobile: `/api/v1` · Auth: Bearer token
Base URL Admin: `/admin/api` · Auth: Admin session

**Mobile endpoints (4):**

| Endpoint | Mục đích | Note |
|---|---|---|
| `GET /recommendations/portfolio` | List Danh mục cơ bản | Filter segment, stockCode; enrich currentPrice |
| `GET /recommendations/portfolio/{id}` | Detail | Trả thêm recommendedPrice, shortDesc, pdfUrl |
| `GET /recommendations/rating` | NHSV Rating active batch | Sort by OVERALL/FUNDAMENTAL/MARKET; trả batchUpdatedAt |
| `GET /recommendations/dailyTechnical` | Kỹ thuật hôm nay (default) | tradeDate=CURDATE; empty array nếu chưa có data |
| `GET /recommendations/dailyTechnical/{id}` | Detail kỹ thuật | Trả thêm shortDesc, pdfUrl |

**Admin endpoints (chính):**

| Endpoint | Mục đích |
|---|---|
| `POST /admin/api/recommendations/portfolio` | Tạo Danh mục cơ bản |
| `PUT /admin/api/recommendations/portfolio/{id}` | Sửa |
| `DELETE /admin/api/recommendations/portfolio/{id}` | Soft archive |
| `POST /admin/api/recommendations/ratingUpload` | Upload Excel + return preview/errors |
| `POST /admin/api/recommendations/ratingPublish` | Activate batch (idempotent) |
| `POST /admin/api/recommendations/dailyTechnical` | Tạo kỹ thuật (dataSource = MANUAL hoặc VIETSTOCK_AUTO) |
| `DELETE /admin/api/recommendations/dailyTechnical/{id}` | Hard delete |
| `POST /admin/api/upload/pdf` | Reuse từ A-04 NH Research |

Chi tiết request/response/error: xem `Spec.html` mục 5 & 6.

---

## 9. Phụ thuộc

| Phụ thuộc | Loại | Ghi chú |
|---|---|---|
| **A-03 — Tab Reorder** | Trước | Khuyến nghị cần vào vị trí đầu tiên của NHSV Channel |
| **A-04 — NH Research** | Đồng thời | Tái dùng PDF viewer component + `/upload/pdf` endpoint |
| **X-01 — Admin Tool infra** | Cùng sprint | nhsv-admin platform để add 3 sub-page mới |
| **Market data service** | Có sẵn | BE enrich currentPrice — cần confirm latency với IT |
| **Stock detail screen** | Có sẵn | Tap mã CK → navigate tới screen này (deep link) |
| **Phòng Phân tích** | Stakeholder | Cung cấp content + content guidelines |
| **Phòng QTRR** | Stakeholder | Cung cấp file Excel rating hàng tháng + format spec |

---

## 10. Open Questions

| # | Câu hỏi | Tác động | Owner | Deadline |
|---|---|---|---|---|
| ~~Q1~~ | ✅ **Đã trả lời (2026-07-10):** Có expire, set theo từng mã (optional). Không set = không hết hạn. → Thêm `expiry_date` (nullable) + scheduled job BE-11 auto-archive. | Resolved | PM | — |
| Q2 | Kỹ thuật hằng ngày — có cần date picker để xem lịch sử các ngày trước không? | UX scope — nếu có cần thêm date picker mobile | PM | Trước MOB-05 |
| Q3 | Vietstock automation — timeline build pipeline? Channel nào (email/SFTP/API)? Ai xử lý raw data? | v2 scope — không ảnh hưởng v1 nhưng cần roadmap | IT + Phòng PT | Trước v1 release |
| Q4 | Rating: nếu user tap mã trong rating table — navigate tới stock detail hay tạo rating detail screen riêng? | UX + estimate scope | PM | Trước MOB-04 |
| Q5 | Excel format Rating — nếu Phòng QTRR thay đổi cột (thêm/đổi tên), BE handle thế nào? Phải fix code hay config được? | Maintainability | PM + Phòng QTRR | Trước BE-07 |
| Q6 | Disclaimer pháp lý — wording cuối cùng cần Legal duyệt không? Có cần thêm số GP/SBV ko? | Compliance | PM + Legal | Trước release |

---

## 11. Estimate sơ bộ

| Layer | Stories | Estimate (đại) |
|---|---|---|
| Backend | 11 stories (BE-01 → BE-11) | ~3 sprint (2 tuần/sprint) |
| Mobile FE | 8 stories (MOB-01 → MOB-08) | ~2 sprint |
| Admin FE | 6 stories (ADM-01 → ADM-06) | ~2 sprint |
| QA | 6 stories (QA-01 → QA-06) | ~1 sprint |

Tổng: ~31 stories (thêm BE-11: scheduled job auto-archive). Q1 đã resolved (2026-07-10) — không còn là blocker. Blocker lớn nhất còn lại: Q5 (Excel format), Q6 (Legal duyệt wording disclaimer), và availability của market data service cho price enrichment.

Excel upload flow (BE-07) là phần unique và phức tạp nhất — cần POC trước khi commit estimate chi tiết.

---

## 12. Tiêu chí release

Tính năng được coi là ready to release khi:

**Mobile App:**
- User mở tab Khuyến nghị, chuyển đổi mượt giữa 3 sub-tab
- Danh mục cơ bản: filter Bluechip/Midcap hoạt động, % tiềm năng tính đúng với màu sắc đúng, PDF mở được in-app
- NHSV Rating: bảng render đúng 5 mức rating, search theo mã, "Cập nhật MM/YYYY" hiển thị đúng batch active
- Kỹ thuật hằng ngày: empty state đúng khi chưa có data, grid 4 ô hiển thị đầy đủ (Upsize ẩn nếu null), badge "MUA +X%" nổi bật
- Disclaimer hiển thị đúng cuối mọi screen
- Tap mã CK → navigate stock detail với stockCode đúng

**Admin Tool:**
- Phòng PT tạo & publish khuyến nghị Danh mục cơ bản trong < 5 phút
- Phòng QTRR upload Excel rating thành công trong < 5 phút, có preview + error handling
- Phòng PT nhập nhiều mã Kỹ thuật hằng ngày + Publish trong < 3 phút (batch entry, không phải tạo lần lượt từng mã)
- Sửa/Archive/Delete hoạt động đúng theo từng loại

**Pháp lý / Compliance:**
- Disclaimer được Legal duyệt (Q6 resolved)
- Wording phù hợp với quy định SBV về tư vấn đầu tư

**Technical:**
- Price enrichment latency < 500ms ở môi trường staging
- Excel parsing 500 mã < 3 giây
- PDF viewer tái dùng được từ A-04 (không build mới)
- Schema có field `dataSource` ready cho v2 automation

---

Document Status: 📋 Draft | For: IT Lead, Mobile FE, Admin FE, Phòng Phân tích, Phòng QTRR, Legal | Next Steps: Q1 đã resolved (2026-07-10) — confirm Q2–Q6 (đặc biệt Q6 Legal duyệt wording disclaimer) trước khi bắt đầu estimate chi tiết theo sprint
