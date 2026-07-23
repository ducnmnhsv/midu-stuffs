# PRD — Khuyến nghị (A-05)

> **(v1.2)** Từ bản 1.2, Khuyến nghị không còn là 1 tab trong NHSV Channel — đã tách thành khu vực riêng với entry point trên Home. Tên gọi "Khuyến nghị" giữ nguyên cho nhất quán tracking (Jira/`tasks.js`), nhưng "tab" trong các đoạn v1.1 bên dưới nay hiểu là "sub-tab bên trong full-screen Khuyến nghị", không phải tab của NHSV Channel.

> **(v1.3)** Cập nhật sau buổi rà soát edge case 2026-07-23 (`Edge_Cases_Review.html`): bỏ `expiryDate` khỏi Danh mục cơ bản (Q1 mở lại — xem Section 10), Segment tự động do BE xác định thay vì admin nhập tay, thêm badge trạng thái động (đã đạt mục tiêu/cắt lỗ) cho cả Cơ bản và Kỹ thuật, ràng buộc 1 mã/1 ngày cho Kỹ thuật, rolling 14 ngày tự bỏ qua Thứ 7/CN, và **tạm thời bỏ yêu cầu đăng nhập** cho toàn bộ API mobile Khuyến nghị (cần revisit trước GA — xem Q11 mới).

**Product:** NHSV Pro · Mobile App
**Feature ID:** A-05
**PM:** Midu (Nguyễn Minh Đức)
**Status:** 📋 Draft
**Version:** 1.3 · 2026-07-23

---

## 1. Bối cảnh & Vấn đề

Phòng Phân tích NHSV và Phòng QTRR sản xuất đều đặn 3 loại nội dung khuyến nghị giá trị cho nhà đầu tư: **danh mục cổ phiếu khuyến nghị dài hạn** (giá kỳ vọng, % tiềm năng), **bảng đánh giá rating cổ phiếu** (S/A/B/C/D, dùng cho margin), và **khuyến nghị kỹ thuật ngắn hạn** (vùng mua, mục tiêu, cắt lỗ). Hiện tại các nội dung này chỉ phân phối qua email và website — không có trên NHSV Pro.

Hệ quả: (1) Khách hàng có tài khoản NHSV nhưng không thấy giá trị tư vấn của NHSV ngay trong app khi đang trading. (2) Phòng PT và Phòng QTRR không tận dụng được kênh phân phối in-app. (3) NHSV thua thiệt so với các CTCK hàng đầu (SSI, KBSV, VNDIRECT, TCBS, MBS) — đều đã có tab khuyến nghị trên mobile app.

Tab Khuyến nghị giải quyết: tập hợp 3 luồng nội dung khuyến nghị vào 1 entry point thống nhất, với UX scannable, tabular, có disclaimer pháp lý rõ ràng.

**Cập nhật 2026-07-20 (v1.2):** Sau khi rà soát lại, Khuyến nghị được nâng cấp thành **1 khu vực riêng ngay trên màn Home** thay vì nằm ẩn bên trong NHSV Channel — lý do: đây là nội dung giá trị cao, cạnh tranh trực tiếp với SSI/KBSV/VNDIRECT/TCBS/MBS, nên cần vị trí nổi bật hơn thay vì bị "chìm" sau 3 tab khác trong Kênh NHSV. Đồng thời điều chỉnh model dữ liệu Danh mục cơ bản (bỏ giá snapshot lúc khuyến nghị, thêm lịch sử nhiều mốc theo mã) và cách hiển thị Kỹ thuật hằng ngày (rolling 14 ngày thay vì chỉ hôm nay). Xem chi tiết từng mục bên dưới — các đoạn có thay đổi được đánh dấu **(v1.2)**.

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

**Entry point trên Home (v1.2 — thay thế hoàn toàn cách vào cũ qua NHSV Channel)**

Khuyến nghị **không còn là tab con trong NHSV Channel**. Thay vào đó là 1 card riêng ngay trên màn Home, vị trí: sau "Lịch sự kiện" (A-02), trước "Kênh tin tức NHSV" — thứ tự Home: Biểu đồ → Hoạt động tích cực → Lịch sự kiện → **Khuyến nghị (mới)** → Kênh tin tức NHSV → Tin mới nhất.

Card Home hiển thị dạng carousel ngang: tiêu đề "🎯 Khuyến nghị NHSV" + link "Xem tất cả", bên dưới là carousel mini-card Top pick lấy từ **Danh mục cơ bản** (mã CK + % tiềm năng, tối đa 5-6 mã, sort theo % tiềm năng giảm dần). Tap 1 mini-card hoặc "Xem tất cả" → mở full-screen Khuyến nghị.

> Sau khi Khuyến nghị rời đi, NHSV Channel còn lại: NH Research → Tin tức → Thị trường.

**Full-screen Khuyến nghị**

Bên trong có 3 sub-tab (secondary tabs với underline #028D96), **đổi thứ tự so với v1.1**: **Cơ bản** (mặc định) → **Kỹ thuật** → **Rating** (Rating lùi xuống cuối).

**Sub-tab 1 — Cơ bản**

Danh sách cổ phiếu được Phòng Phân tích khuyến nghị mua nắm giữ dài hạn. User thấy 3 filter pill (Tất cả mặc định / Bluechip / Midcap) và icon search.

Mỗi card hiển thị: mã CK (lớn, đậm), tên công ty, sàn (HOSE/HNX/UPCOM), % tiềm năng (xanh nếu dương, đỏ nếu âm), grid 3 ô (Giá hiện tại / Giá kỳ vọng / Ngày KN), segment badge (BLUECHIP/MIDCAP) và nút "Báo cáo" nếu có report đính kèm (PDF riêng hoặc link bài NH Research — xem 4.2).

% tiềm năng được tính trên app real-time = (targetPrice / currentPrice - 1) × 100 — **công thức không đổi so với v1.1**. Giá hiện tại được BE enrich từ market data service (snapshot phiên là đủ — không cần realtime tick). Ngoài giờ giao dịch: hiển thị giá đóng cửa phiên gần nhất. **(v1.2)** Không còn field "giá tại ngày KN" (snapshot giá lúc tạo khuyến nghị) — admin chỉ nhập Giá kỳ vọng, % luôn so với giá hiện tại live, không cần điểm neo lịch sử.

**(v1.3) Giá hiện tại đã vượt Giá kỳ vọng (% tiềm năng ≤ 0):** card vẫn hiển thị bình thường với % màu đỏ, kèm badge "Đã đạt mục tiêu" cạnh % tiềm năng. Trên Admin 1, mã ở trạng thái này được highlight để Phòng PT chủ động review (giữ nguyên / ra mốc mới / Archive) — hệ thống **không** tự động archive theo giá.

**(v1.3) currentPrice null/0** (mã mới niêm yết chưa khớp lệnh, lỗi enrichment): ẩn % tiềm năng, hiển thị "Đang cập nhật giá" thay vì NaN/lỗi.

**(v1.2) Nhiều mốc khuyến nghị theo thời gian:** 1 mã có thể có nhiều lần khuyến nghị (mốc) theo thời gian — thường mỗi lần Phòng PT ra cập nhật theo nhịp báo cáo tài chính (BCTC) quý của doanh nghiệp. Khi Phòng PT tạo mốc mới cho 1 mã đã có khuyến nghị active, mốc mới trở thành **current** (hiển thị trên list card + card Home), mốc cũ tự động chuyển thành lịch sử — **không xoá**, vẫn truy xuất được ở màn chi tiết mã (xem IA bên dưới).

**(v1.3, thay thế đoạn v1.1/v1.2 về `expiryDate`)** Danh mục cơ bản **không có ngày hết hiệu lực** — mã khuyến nghị chỉ kết thúc khi Phòng PT tự tay Archive. Field `expiryDate` **ẩn khỏi form Admin 1** (không xóa khỏi schema, nhưng luôn null cho Cơ bản) — không còn scheduled job auto-archive theo ngày cho mục này (xem Q1 mở lại tại Section 10).

Tap card → push navigate sang màn chi tiết khuyến nghị. **(v1.2)** IA màn chi tiết: hero card với code tile, price compare card (**so sánh Giá hiện tại vs Giá kỳ vọng** — không còn "giá tại ngày KN" để so sánh như v1.1), big % potential block màu xanh, **Luận điểm đầu tư** (của mốc mới nhất), info card ngày KN, **Danh sách các báo cáo gần nhất** (liệt kê các mốc/report cũ của mã này theo thời gian, mỗi dòng: ngày + tên report + tap để mở PDF hoặc bài NH Research), CTA "Xem cổ phiếu {code}" để navigate sang stock detail screen.

**Sub-tab 2 — Kỹ thuật**

**(v1.2)** Khuyến nghị kỹ thuật ngắn hạn (vài ngày → vài tuần) cho từng phiên giao dịch. Bỏ mô hình "mặc định chỉ hôm nay + empty state nếu chưa upload". Thay bằng **1 list cuộn, group theo ngày (header ngày dạng "Hôm nay", "Hôm qua", "18/07"...), gộp toàn bộ entry Published trong 14 ngày gần nhất** — không có date picker để lùi xa hơn 14 ngày trên mobile (Admin vẫn quản lý được mọi ngày trong quá khứ, xem 4.2). Nếu 14 ngày gần nhất không có entry nào → empty state với icon đồng hồ + thông điệp "Phòng Phân tích thường cập nhật khuyến nghị kỹ thuật trong phiên sáng. Vui lòng quay lại sau."

**(v1.3)** Nhóm ngày tự động bỏ qua Thứ 7 & Chủ nhật (theo giờ Việt Nam) — không hiển thị header ngày cho 2 ngày này dù có hay không có entry.

**(v1.2)** Tiêu chí khuyến nghị: enum hệ thống có đủ `BUY`/`SELL`/`HOLD` (chuẩn bị mở rộng), nhưng v1 chỉ dùng `BUY` — admin không tạo được entry `SELL`/`HOLD` (validation chặn). Badge trên card luôn là "MUA +X%".

Mỗi card: mã CK + tên + sàn (left), badge "MUA +X%" nổi bật xanh `#07A461` (right). Bên dưới: **(v1.2)** dòng 1 gộp **Vùng mua** (xanh nhạt `#E6F6EF`) + **Cắt lỗ** (đỏ nhạt `#FFEBE6`) chung 1 row 2 cột; dòng 2 vẫn giữ **Mục tiêu** (teal nhạt `#E0F7F4`) + **Upsize** (xám nhạt `#F2F6FB`, ẨN ô nếu null) — giữ đủ 4 field như v1.1, chỉ đổi cách nhóm hiển thị cho gọn hơn.

- **Vùng mua** — range "75,50 – 76,50" nếu có 2 mức, hoặc "≥ X" nếu chỉ 1 mức
- **Cắt lỗ** — stop loss
- **Mục tiêu** — target price
- **Upsize** — optional

**(v1.3)** Badge trạng thái động: khi giá hiện tại (real-time) đã chạm/vượt **Mục tiêu** hoặc **Cắt lỗ**, card hiển thị thêm badge tương ứng — "Đã đạt mục tiêu" (xanh) hoặc "Đã chạm cắt lỗ" (đỏ) — cạnh badge "MUA +X%". Cần enrich currentPrice cho `dailyTechnical` tương tự Cơ bản (xem NFR + API mục 8).

Tap card → màn chi tiết với hero (mã + tech grid), phân tích kỹ thuật (textarea từ admin), info card, PDF attachment (nếu có), CTA xem cổ phiếu.

**Sub-tab 3 — NHSV Rating**

Bảng đánh giá rating cổ phiếu do Phòng QTRR cung cấp định kỳ (~1 lần/tháng), dùng làm cơ sở cho danh mục ký quỹ NHSV. Search bar luôn visible trên đầu (search theo mã CK).

Bảng 4 cột: **Mã CK** | **Cơ bản** | **TT (Thị trường)** | **Tổng**. Rating badge 5 mức:
- **S** — Xuất sắc — `#FFF4D1` bg + `#B47800` text
- **A** — Tốt — `#E6F6EF` bg + `#07A461` text
- **B** — Trung bình — `#E3F2FD` bg + `#23A3E9` text
- **C** — Kém — `#FFF3E0` bg + `#C76E00` text
- **D** — Yếu — `#FFEBE6` bg + `#DA1004` text

Sort mặc định: Rating tổng giảm dần. Tap header để sort theo cột khác. Tap row → navigate thẳng sang stock detail screen (KHÔNG có rating detail riêng).

Header có badge "Cập nhật: MM/YYYY" lấy từ `batchUpdatedAt` (ngày upload batch active). Cuối màn hiển thị legend giải thích 5 mức rating.

**Search**

Cả 3 sub-tab đều có search theo mã CK. Search highlight substring (ví dụ user gõ "VC" → highlight phần "VC" trong VCB/VCG/VCI). Debounce 250ms.

**Disclaimer (bắt buộc pháp lý)**

Mọi screen của tab Khuyến nghị đều phải có footer disclaimer ở cuối list. Wording bên dưới là **draft đã bổ sung 3 yếu tố pháp lý chuẩn** còn thiếu ở bản v1.0 (không phải lời chào mua/bán, không phải tư vấn đầu tư, miễn trừ trách nhiệm cho NHSV) — vẫn cần Legal duyệt chính thức (xem Q6):
- Danh mục cơ bản & Kỹ thuật: "Nội dung khuyến nghị do Phòng Phân tích NHSV cung cấp, chỉ mang tính chất tham khảo, không phải là lời chào mua/bán hoặc tư vấn đầu tư. NHSV không chịu trách nhiệm đối với quyết định giao dịch dựa trên thông tin này. Quý khách cần tự đánh giá và cân nhắc kỹ trước khi thực hiện giao dịch."
- NHSV Rating: "Rating do Phòng Quản trị Rủi ro NHSV xây dựng, cập nhật định kỳ hàng tháng, dùng cho mục đích quản lý danh mục ký quỹ, chỉ mang tính chất tham khảo và không phải là tư vấn đầu tư. NHSV không chịu trách nhiệm đối với quyết định giao dịch dựa trên thông tin này."

### 4.2 Admin Tool — Phòng Phân tích & Phòng QTRR

URL gốc: `tnhsvpro.nhsv.vn/nhsv-admin`. Menu Khuyến nghị có 3 sub-page tương ứng 3 sub-tab trên mobile.

**Admin 1 — Danh mục cơ bản Management**

Table với filter Bluechip/Midcap + search mã + filter status (Active/Archived). Cột: Mã CK | Segment (**(v1.3)** auto, read-only) | Giá Kỳ vọng | Ngày KN | Report | Trạng thái | Thao tác. **(v1.2)** Bỏ cột "Giá KN" (không còn snapshot giá lúc khuyến nghị). **(v1.3)** Mã có % tiềm năng ≤ 0 (giá hiện tại đã vượt giá kỳ vọng) được highlight badge cảnh báo trong bảng để Phòng PT dễ nhận diện cần review.

Nút "+ Thêm khuyến nghị" → form: Mã CK (autocomplete — **(v1.3)** Segment tự động xác định bởi BE theo mã, không còn là input admin nhập tay), Giá kỳ vọng (auto-calculate % tiềm năng live so với giá hiện tại), Ngày KN, Luận điểm (textarea optional), **Report đính kèm** — chọn 1 trong 2 cách: (a) tìm & link 1 bài NH Research đã publish có tag mã này (autocomplete theo `GET /api/v1/nhResearch/articles?stockCode=X`, xem A-06), hoặc (b) Upload PDF riêng (drag-drop, dùng chung pipeline `/admin/api/upload/pdf`). **(v1.2)** Bỏ field "Giá tại ngày KN" khỏi form — % tiềm năng luôn tính live so với giá hiện tại, không cần điểm neo giá lúc tạo. **(v1.3)** Bỏ field "Ngày hết hiệu lực" khỏi form (xem giải thích ở trên) và bỏ field "Segment" (auto).

**(v1.2) Mốc & lịch sử:** nếu mã đã có 1 khuyến nghị active, bấm "+ Thêm khuyến nghị" cho cùng mã này sẽ tạo **mốc mới**, tự động chuyển mốc hiện tại thành lịch sử (không xoá, giữ nguyên trong DB, hiển thị ở "Danh sách báo cáo gần nhất" trên mobile). Mốc mới trở thành current, hiển thị trên list card + Home.

Thao tác: Sửa (sửa mốc current đang active) | Archive (soft delete: status → ARCHIVED, không xóa cứng).

**Admin 2 — NHSV Rating Upload (Excel-based)**

Đây là flow đặc biệt của Khuyến nghị. Phòng QTRR gửi file Excel hàng tháng, admin upload thay vì nhập tay từng mã.

Flow:
1. Admin nhận file Excel từ Phòng QTRR (4 cột bắt buộc: `Mã`, `Cơ bản`, `Thị trường`, `NHSV Rating`. Giá trị: S/A/B/C/D case-insensitive)
2. Admin drag-drop file lên page → gọi `POST /admin/api/recommendations/ratingUpload`
3. BE parse → validate → return preview (10 rows đầu + total + error list nếu có). **(v1.3)** Preview bổ sung: (a) warning cho admin nếu phát hiện mã trùng lặp trong file, (b) warning liệt kê N mã có ở batch active hiện tại nhưng không xuất hiện trong file mới (có thể bị thiếu sót khi Phòng QTRR làm file)
4. Admin xem preview, nếu OK → bấm "Xác nhận publish" → gọi `POST /admin/api/recommendations/ratingPublish`. **(v1.3)** Nếu file còn bất kỳ dòng lỗi nào (mã không tồn tại/sai định dạng), nút "Xác nhận publish" bị disable cho đến khi admin sửa và re-upload file sạch 100% — không cho phép publish một phần (skip dòng lỗi)
5. BE tạo batch mới, INSERT all records, SET `is_active=true` cho batch mới và `false` cho batch cũ (transaction nguyên tử)
6. App đọc batch mới ngay lập tức — không cần cache clear

Page hiển thị header "Batch hiện tại: MM/YYYY · N mã" + upload area + preview table + lịch sử các batch cũ (không xóa — để audit trail).

**Admin 3 — Kỹ thuật hằng ngày Management**

Date picker để chọn ngày (default: hôm nay) — **chọn được bất kỳ ngày trong quá khứ** (cập nhật 2026-07-10), không giới hạn hôm nay. **(v1.2)** Lưu ý: giới hạn "chỉ hiển thị 14 ngày gần nhất" chỉ áp dụng cho màn hình **mobile** (sub-tab Kỹ thuật, mục 4.1) — Admin vẫn quản lý/xem/sửa được entry của bất kỳ ngày nào trong quá khứ như v1.1, không bị giới hạn 14 ngày. **(v1.3)** Nếu admin chọn ngày áp dụng quá 14 ngày trước hiện tại, hiển thị cảnh báo "Entry cho ngày này sẽ không hiển thị trên mobile (ngoài rolling 14 ngày)" — vẫn cho phép lưu (phục vụ audit/lưu trữ), chỉ cảnh báo, không chặn. Table hiển thị toàn bộ entry của ngày đã chọn, **gồm cả Nháp và đã Publish**. Table: Mã CK | Vùng mua | Mục tiêu | Cắt lỗ | Upsize | PDF | Nguồn | **Trạng thái** | Thao tác. Cột "Nguồn" có badge "MANUAL" (v1) hoặc "AUTO" (v2 — Vietstock automation).

Ngày không có entry nào (kể cả Nháp) → API trả `{"tradeDate":"...","totalCount":0,"technicals":[]}`, **HTTP 200** (không phải 404) — admin FE hiển thị empty state rõ ràng, không phải màn lỗi.

**Draft → Publish workflow (cập nhật 2026-07-10):** Admin không nhập từng mã một rồi hiện ngay trên app. Thay vào đó:
1. Bấm "+ Thêm entry" → mở form batch: chọn **1 Ngày áp dụng**, sau đó thêm **nhiều dòng mã** (mỗi dòng: Mã CK, Vùng mua from/to, Mục tiêu, Cắt lỗ, Upsize optional) qua "+ Thêm dòng". **(v1.3)** Ràng buộc: mỗi mã chỉ có tối đa 1 entry/ngày — nếu admin thêm dòng với mã đã có entry cho ngày đang chọn, hệ thống chặn và yêu cầu sửa entry hiện có (`UNIQUE(stockCode, tradeDate)`) thay vì tạo mới.
2. Bấm "Lưu nháp" → toàn bộ mã vừa nhập lưu với trạng thái **Nháp (DRAFT)** — chưa hiển thị trên app, admin có thể tiếp tục sửa/thêm dòng khác cho cùng ngày.
3. Khi đã soạn xong cho ngày đó, bấm **"Publish ngày [ngày đang chọn]"** → toàn bộ entry Nháp của ngày đó chuyển **Còn hiệu lực (PUBLISHED)** cùng lúc — app đọc thấy ngay, không cần chờ.
4. Sửa từng dòng riêng (Luận điểm, PDF, số liệu) vẫn dùng được cho cả entry Nháp và đã Publish.

Delete: HARD DELETE (khác với Danh mục cơ bản — dữ liệu kỹ thuật ngắn hạn, không cần giữ lâu), áp dụng cho cả Nháp và đã Publish.

**(v1.2) Tiêu chí khuyến nghị:** schema có field `recommendationType` đủ 3 giá trị `BUY`/`SELL`/`HOLD` (chuẩn bị mở rộng cho tương lai), nhưng v1 form chỉ cho phép chọn `BUY` — BE validate chặn `SELL`/`HOLD` (trả `INVALID_PARAMETER` nếu cố gửi). Không hiển thị field này trên form nếu chỉ có 1 lựa chọn khả dụng.

**Automation-ready**

Schema và endpoint đã accept `dataSource: 'VIETSTOCK_AUTO'` với `createdBy: null`. Khi pipeline Vietstock được build trong tương lai (v2), chỉ cần điểm POST vào API hiện có — không cần migration DB.

---

## 5. Ngoài phạm vi (Out of Scope)

- **Auto pipeline từ Vietstock** — schema ready, nhưng implementation pipeline để v2
- **Track record / accuracy rate** của từng khuyến nghị (đã đạt target chưa, % hit rate Phòng PT)
- **Alert push** khi giá đạt target price hoặc stop loss
- **(v1.2, thay đổi so với v1.1)** Lịch sử Danh mục cơ bản (nhiều mốc/mã) **đã có trong scope** — không còn out-of-scope. Riêng Kỹ thuật hằng ngày vẫn giới hạn hiển thị 14 ngày trên mobile (Admin không giới hạn, xem 4.2)
- **So sánh nhiều mã** cùng lúc
- **Portfolio P&L** dựa trên khuyến nghị (nếu user mua theo)
- **Approval workflow nhiều bước** — v1 admin publish ngay, không có review/duyệt
- **Notification settings** — sẽ làm trong sprint riêng (cùng với A-04 Notification Settings)
- **Rating chi tiết phân tích** — v1 chỉ hiển thị chữ cái + legend, không có break-down vì sao S/A/B/C/D
- **(v1.2)** Khuyến nghị kỹ thuật loại SELL/HOLD — schema ready nhưng chưa mở cho admin dùng ở v1
- **(v1.2)** Tùy chỉnh vị trí/nội dung card Home theo từng user (personalization) — v1 hiển thị cùng 1 danh sách Top pick cho mọi user
- **(v1.3)** Xử lý riêng khi mã bị hủy niêm yết/đình chỉ giao dịch trong lúc đang có khuyến nghị active — chấp nhận rủi ro hiển thị giá cũ/lỗi hiếm gặp, không triển khai xử lý riêng ở v1
- **(v1.3)** Tự động điều chỉnh giá kỳ vọng theo sự kiện chia tách/cổ tức (GDKHQ) — không triển khai ở v1, Phòng PT tự nhận biết và sửa thủ công nếu cần

---

## 6. User Stories (tóm tắt)

| ID | Vai trò | Nhu cầu | Lý do |
|---|---|---|---|
| US-01 | Investor | Xem danh sách cổ phiếu NHSV khuyến nghị mua dài hạn | Tìm mã có tiềm năng cao theo Phòng PT |
| US-02 | Investor | Filter Bluechip/Midcap | Lọc theo risk profile cá nhân |
| US-03 | Investor | Xem giá hiện tại + % tiềm năng realtime | Quyết định mua khi % còn đủ hấp dẫn |
| US-04 | Investor | Đọc luận điểm + danh sách báo cáo gần nhất của mã | Hiểu lý do Phòng PT khuyến nghị, xem cả các mốc cập nhật trước |
| US-05 | Investor | Xem rating S→D của cổ phiếu | Cross-check trước khi quyết định |
| US-06 | Day trader | Xem khuyến nghị kỹ thuật trong 14 ngày gần nhất với vùng mua / mục tiêu / cắt lỗ | Đặt lệnh theo plan của Phòng PT, xem lại vài phiên gần đây nếu bỏ lỡ |
| US-07 | Investor | Tap mã CK → xem stock detail screen | Đặt lệnh ngay sau khi đọc khuyến nghị |
| US-08 | Analyst (Phòng PT) | Tạo nhanh khuyến nghị mới qua admin | Phân phối nội dung không phụ thuộc IT |
| US-09 | Analyst (Phòng PT) | Sửa khuyến nghị đã đăng + thay report | Cập nhật khi có info mới |
| US-10 | QTRR | Upload Excel batch rating mỗi tháng | Thay thế toàn bộ rating cũ trong 1 thao tác |
| US-11 *(v1.2)* | Investor | Thấy preview Top pick Khuyến nghị ngay trên Home | Không cần vào sâu NHSV Channel mới biết NHSV có khuyến nghị gì |
| US-12 *(v1.2)* | Analyst (Phòng PT) | Tạo mốc khuyến nghị mới cho mã đã có, mốc cũ tự lưu vào lịch sử | Cập nhật theo BCTC quý mà không mất dữ liệu cũ |
| US-13 *(v1.2)* | Analyst (Phòng PT) | Link 1 bài NH Research có sẵn làm report cho khuyến nghị, thay vì upload lại | Tránh trùng lặp nội dung đã có trên NH Research |

---

## 7. Yêu cầu phi chức năng

| Hạng mục | Yêu cầu |
|---|---|
| Hiệu năng API list | Danh mục/Rating/Kỹ thuật load < 2 giây ở mạng 4G |
| *(v1.2)* Home preview API | `topPicks` load < 1 giây — API riêng, nhẹ (≤ 6 records), không load chung với list đầy đủ để tránh làm chậm màn Home |
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

Base URL Mobile: `/api/v1` · Auth: **(v1.3)** Tạm thời không yêu cầu đăng nhập (Bearer token optional) cho toàn bộ 6 API mobile Khuyến nghị — quyết định tạm thời tại buổi rà soát edge case 2026-07-23, cần revisit trước GA (xem Q11, Section 10)
Base URL Admin: `/admin/api` · Auth: Admin session

**Mobile endpoints (6 — thêm 2 so với v1.1):**

| Endpoint | Mục đích | Note |
|---|---|---|
| **(v1.2)** `GET /recommendations/portfolio/topPicks` | Preview Top pick cho card Home | ≤ 6 records, sort % tiềm năng giảm dần (**(v1.3)** tie-break: ngày KN gần nhất ưu tiên trước); response nhẹ (mã, %, segment) |
| `GET /recommendations/portfolio` | List Cơ bản (full-screen) | Filter segment, stockCode; enrich currentPrice; **(v1.2)** bỏ `recommendedPrice` (giá tại ngày KN) khỏi response |
| `GET /recommendations/portfolio/{id}` | Detail | Trả `shortDesc` (luận điểm mốc hiện tại); **(v1.2)** thêm `reportHistory[]` (Danh sách báo cáo gần nhất — mỗi item: `recommendedDate`, `title`, `reportUrl`, `source`: `NH_RESEARCH` \| `UPLOAD`). **(v1.3)** Bỏ `expiryDate` khỏi response (Cơ bản không còn dùng field này). Nếu `reportUrl` trỏ tới bài NH Research đã unpublish/xóa → FE chỉ hiển thị "No data" tại dòng đó thay vì mở link lỗi |
| `GET /recommendations/rating` | NHSV Rating active batch | Sort by OVERALL/FUNDAMENTAL/MARKET; trả batchUpdatedAt — không đổi |
| **(v1.2)** `GET /recommendations/dailyTechnical` | Kỹ thuật 14 ngày gần nhất | Trả list group theo `tradeDate` (thay vì 1 ngày); mặc định `fromDate = today-13`, `toDate = today`; empty array nếu không có data trong khoảng |
| `GET /recommendations/dailyTechnical/{id}` | Detail kỹ thuật | Trả thêm shortDesc, pdfUrl — không đổi |

**Admin endpoints (chính):**

| Endpoint | Mục đích |
|---|---|
| `POST /admin/api/recommendations/portfolio` | Tạo mốc mới Cơ bản. **(v1.2)** Body bỏ `recommendedPrice`; thêm `reportSource: "NH_RESEARCH" \| "UPLOAD"` + `nhResearchArticleId` (nếu chọn link) hoặc `pdfUrl` (nếu upload riêng). Nếu mã đã có mốc active → mốc cũ tự chuyển sang lịch sử (không cần call riêng) |
| `PUT /admin/api/recommendations/portfolio/{id}` | Sửa mốc hiện tại |
| `DELETE /admin/api/recommendations/portfolio/{id}` | Soft archive |
| **(v1.2)** `GET /admin/api/nhResearch/articles?stockCode={code}` | Tìm bài NH Research đã tag mã này để link (reuse API 8.1 của A-04/A-06, xem `NH_Research/PRD.md`) |
| `POST /admin/api/recommendations/ratingUpload` | Upload Excel + return preview/errors |
| `POST /admin/api/recommendations/ratingPublish` | Activate batch (idempotent) |
| `POST /admin/api/recommendations/dailyTechnical` | Tạo kỹ thuật. **(v1.2)** `recommendationType` mặc định/chỉ nhận `BUY` (validate chặn `SELL`/`HOLD`) |
| `DELETE /admin/api/recommendations/dailyTechnical/{id}` | Hard delete |
| `POST /admin/api/upload/pdf` | Reuse từ A-04 NH Research |

Chi tiết request/response/error: xem `Spec.html` mục 5 & 6 (**cần cập nhật lại theo v1.2** — hiện Spec.html còn phản ánh model v1.1, xem Open Question mới ở mục 10).

---

## 9. Phụ thuộc

| Phụ thuộc | Loại | Ghi chú |
|---|---|---|
| **(v1.2)** ~~A-03 — Tab Reorder~~ | Không còn áp dụng | Khuyến nghị rời khỏi NHSV Channel nên không phụ thuộc thứ tự tab trong Channel nữa |
| **(v1.2) A-02 — Event Calendar (Home layout)** | Trước | Card Khuyến nghị cần vào đúng vị trí (sau Lịch sự kiện) trong thứ tự Home mới |
| **A-04 — NH Research** | Đồng thời | Tái dùng PDF viewer component + `/upload/pdf` endpoint; **(v1.2)** thêm phụ thuộc mới: reuse API filter theo `stockCode` (A-06 Stock Tag Enrichment) để admin link bài NH Research |
| **X-01 — Admin Tool infra** | Cùng sprint | nhsv-admin platform để add 3 sub-page mới |
| **Market data service** | Có sẵn | BE enrich currentPrice — cần confirm latency với IT |
| **Stock detail screen** | Có sẵn | Tap mã CK → navigate tới screen này (deep link) |
| **Phòng Phân tích** | Stakeholder | Cung cấp content + content guidelines |
| **Phòng QTRR** | Stakeholder | Cung cấp file Excel rating hàng tháng + format spec |

---

## 10. Open Questions

| # | Câu hỏi | Tác động | Owner | Deadline |
|---|---|---|---|---|
| ~~Q1~~ | ✅ **Đã trả lời (2026-07-10):** Có expire, set theo từng mã (optional). Không set = không hết hạn. → Thêm `expiry_date` (nullable) + scheduled job BE-11 auto-archive. | Resolved *(⚠️ mở lại — xem Q1b)* | PM | — |
| Q1b *(v1.3)* | ⚠️ **Reopened 2026-07-23** (rà soát edge case, xem `Edge_Cases_Review.html` case 2 & case 18): quyết định **bỏ `expiryDate` cho Cơ bản** — thay bằng badge "Đã đạt mục tiêu" + admin tự review/archive thủ công. Field ẩn khỏi Admin form; scheduled job BE-11 không còn cần thiết cho Cơ bản. | Resolved — thay thế Q1 | PM | — |
| ~~Q2~~ | ✅ **Đã trả lời (2026-07-20, v1.2):** Không cần date picker — mobile hiển thị rolling 14 ngày, group theo ngày. Admin vẫn tự do chọn ngày bất kỳ trong quá khứ. | Resolved | PM | — |
| Q3 | Vietstock automation — timeline build pipeline? Channel nào (email/SFTP/API)? Ai xử lý raw data? | v2 scope — không ảnh hưởng v1 nhưng cần roadmap | IT + Phòng PT | Trước v1 release |
| Q4 | Rating: nếu user tap mã trong rating table — navigate tới stock detail hay tạo rating detail screen riêng? | UX + estimate scope | PM | Trước MOB-04 |
| Q5 | Excel format Rating — nếu Phòng QTRR thay đổi cột (thêm/đổi tên), BE handle thế nào? Phải fix code hay config được? | Maintainability | PM + Phòng QTRR | Trước BE-07 |
| Q6 | Disclaimer pháp lý — wording cuối cùng cần Legal duyệt không? Có cần thêm số GP/SBV ko? | Compliance | PM + Legal | Trước release |
| Q7 *(v1.2)* | Card Home cần cache/refresh theo tần suất nào (mỗi lần mở app? polling? theo phiên)? Ảnh hưởng đến load Home nếu gọi live mỗi lần | Performance Home screen | PM + IT | Trước MOB-Home-01 |
| Q8 *(v1.2)* | "Danh sách báo cáo gần nhất" ở màn chi tiết mã — giới hạn bao nhiêu mốc/report hiển thị (5? 10? tất cả)? | UX + pagination | PM | Trước MOB-04 |
| Q9 *(v1.2)* | A-06 (Stock Tag Enrichment, NH Research) hiện đang cùng trạng thái backlog — cần xác nhận A-06 build xong trước khi Admin Khuyến nghị có thể link bài NH Research, nếu không sẽ tạm thời chỉ dùng nhánh Upload PDF riêng | Sequencing 2 feature | PM + IT | Trước ADM-01 (v1.2) |
| Q10 *(v1.2)* | `Design.html`/`Spec.html`/`admin-demo.html` hiện vẫn phản ánh model v1.1 (giá tại ngày KN, tab order cũ, Kỹ thuật chỉ hôm nay) — cần cập nhật lại theo v1.2/v1.3 trước khi đưa cho IT estimate chi tiết | Doc consistency | PM | Trước khi giao IT estimate |
| Q11 *(v1.3)* | Đăng nhập: hiện tạm thời mở toàn bộ 6 API mobile Khuyến nghị không cần login — có nên giữ vĩnh viễn (tăng conversion, cạnh tranh SSI/VNDIRECT/KBSV bằng preview không cần đăng nhập) hay bắt buộc login trở lại trước GA (bảo vệ nội dung premium)? | Business decision — ảnh hưởng chiến lược phân phối nội dung + bảo mật | PM + IT | Trước GA |

---

## 11. Estimate sơ bộ

| Layer | Stories | Estimate (đại) |
|---|---|---|
| Backend | 11 stories (BE-01 → BE-11) + **(v1.2)** ~3 story mới (mốc/lịch sử Cơ bản, link NH Research, topPicks API) | ~3-4 sprint (2 tuần/sprint) |
| Mobile FE | 8 stories (MOB-01 → MOB-08) + **(v1.2)** ~2 story mới (card Home, list 14 ngày) | ~2-3 sprint |
| Admin FE | 6 stories (ADM-01 → ADM-06) + **(v1.2)** ~1 story mới (chọn link NH Research vs upload) | ~2 sprint |
| QA | 6 stories (QA-01 → QA-06) | ~1 sprint |

Tổng v1.1: ~31 stories. **(v1.2)** thêm ~6 story do mở rộng scope (Home entry point, mốc lịch sử, tích hợp NH Research) — con số chính xác cần breakdown lại sau khi Q7–Q10 được confirm. Q1, Q2 đã resolved — không còn blocker. Blocker lớn nhất còn lại: Q5 (Excel format), Q6 (Legal duyệt wording disclaimer), Q9 (sequencing với A-06 NH Research), và availability của market data service cho price enrichment.

Excel upload flow (BE-07) là phần unique và phức tạp nhất — cần POC trước khi commit estimate chi tiết.

---

## 12. Tiêu chí release

Tính năng được coi là ready to release khi:

**Mobile App:**
- **(v1.2)** Card Khuyến nghị hiển thị đúng vị trí trên Home (sau Lịch sự kiện), carousel Top pick load đúng dữ liệu, tap vào mở đúng full-screen Khuyến nghị
- User mở full-screen Khuyến nghị, chuyển đổi mượt giữa 3 sub-tab theo đúng thứ tự mới: Cơ bản → Kỹ thuật → Rating
- Cơ bản: filter Bluechip/Midcap hoạt động, % tiềm năng tính đúng với màu sắc đúng, report mở được in-app (cả 2 nhánh: link NH Research và PDF riêng); **(v1.2)** màn chi tiết hiển thị đúng "Danh sách báo cáo gần nhất" theo thứ tự thời gian
- NHSV Rating: bảng render đúng 5 mức rating, search theo mã, "Cập nhật MM/YYYY" hiển thị đúng batch active
- **(v1.2)** Kỹ thuật: list cuộn đúng 14 ngày gần nhất, group theo ngày, card gộp đúng dòng Vùng mua–Cắt lỗ, badge "MUA +X%" nổi bật, empty state đúng khi cả 14 ngày không có data
- Disclaimer hiển thị đúng cuối mọi screen
- Tap mã CK → navigate stock detail với stockCode đúng

**Admin Tool:**
- Phòng PT tạo & publish khuyến nghị Danh mục cơ bản trong < 5 phút; **(v1.2)** tạo mốc mới cho mã đã có tự động chuyển mốc cũ vào lịch sử đúng, không mất dữ liệu
- **(v1.2)** Phòng PT link được bài NH Research có sẵn (nếu đã tag mã) hoặc upload PDF riêng khi tạo khuyến nghị Cơ bản
- Phòng QTRR upload Excel rating thành công trong < 5 phút, có preview + error handling
- Phòng PT nhập nhiều mã Kỹ thuật hằng ngày + Publish trong < 3 phút (batch entry, không phải tạo lần lượt từng mã); **(v1.2)** chỉ tạo được loại BUY (SELL/HOLD bị chặn)
- Sửa/Archive/Delete hoạt động đúng theo từng loại

**Pháp lý / Compliance:**
- Disclaimer được Legal duyệt (Q6 resolved)
- Wording phù hợp với quy định SBV về tư vấn đầu tư

**Technical:**
- Price enrichment latency < 500ms ở môi trường staging
- Excel parsing 500 mã < 3 giây
- PDF viewer tái dùng được từ A-04 (không build mới)
- Schema có field `dataSource` ready cho v2 automation
- **(v1.2)** `GET /recommendations/portfolio/topPicks` load < 1 giây; `GET /recommendations/dailyTechnical` trả đúng dữ liệu group theo 14 ngày

---

Document Status: 📋 Draft | For: IT Lead, Mobile FE, Admin FE, Phòng Phân tích, Phòng QTRR, Legal | Next Steps: v1.3 (2026-07-23) chốt xử lý edge case từ `Edge_Cases_Review.html` (bỏ expiryDate Cơ bản, Segment auto, badge động, unique mã/ngày Kỹ thuật, tạm mở API không cần login) — cần confirm Q3–Q6 (còn treo từ v1.1), Q7–Q10 (từ v1.2), Q11 (mới, quyết định login trước GA), đồng thời cập nhật lại `Design.html`/`Spec.html`/`admin-demo.html` theo v1.3 trước khi giao IT estimate chi tiết theo sprint
