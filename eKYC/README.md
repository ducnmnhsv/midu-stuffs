# eKYC v3.0 — Tổng quan

Hệ thống lưu trữ biometric log + compliance journey log cho quá trình mở tài khoản qua eKYC (VNPT). **Scope 1 (đang triển khai) thuần backend/DB — không có màn hình admin nào.** Tra cứu hành trình, dashboard analytics, MRZ validation và lưu ảnh CCCD đều để Scope 2.

**Vấn đề cốt lõi:** `CustomEKycService.java:211` xóa records PENDING cũ khi user retry → không có audit trail, không trace được lý do fail.

---

## Cấu trúc Sub-feature

> Folder nhóm theo **Scope** (2026-07-15), và bên trong mỗi Scope nhóm tiếp theo **loại tài liệu** (`Specifications/` / `Issues/` / `demos/`) dùng chung cho mọi sub-feature — thay vì mỗi sub-feature có cây thư mục riêng (tránh phân mảnh nhiều folder con chỉ có 1 file).
>
> **Naming convention theo Scope (2026-07-20):**
> - **Scope_1** (đã gộp hoàn toàn theo loại tài liệu) — tên file chỉ còn `BE_Spec.md` / `BE_Issue.md` / `FE_Issue.md`, không còn số thứ tự sub-feature ở đầu tên (sub-feature nào nằm trong Phần A/Phần B đọc trong file). Ưu tiên nhận biết BE/FE ngay từ tên file khi list folder.
> - **Scope_2** (chưa gộp — mỗi sub-feature vẫn 1 issue/spec riêng) — vẫn giữ số thứ tự sub-feature (01, 02...) ở đầu tên file, xuyên suốt lịch sử review log, để biết file nào thuộc sub-feature nào.

```
eKYC/
├── Planning/                                      ← Dùng chung mọi sub-feature/scope
│   ├── PRD_eKYC_v2.md                              ← Yêu cầu nghiệp vụ đầy đủ
│   └── eKYC-summary.html                           ← Tổng hợp visual (HTML)
│
├── Scope_1/                                        🔄 Đang triển khai — thuần backend/DB, KHÔNG có admin UI
│   ├── Specifications/
│   │   └── BE_Spec.md          ← 1 file BE spec duy nhất — Phần A (canonical DB schema/service/REST cho 01) + Phần B (redesign v2.0 cho 07). Gộp 2026-07-20, thay thế 01_Biometric_Attempt_Log_Backend_Spec.md + 07_Compliance_Journey_Log_Backend_Spec.md cũ
│   ├── Issues/
│   │   ├── BE_Issue.md         ← 14 BE tasks — gộp chung 01 + 07 (thay thế 01_BE_Issue_Biometric_Log_Storage.md cũ)
│   │   └── FE_Issue.md         ← App gọi journey-log API tại 9 màn hình (mới 2026-07-16). Chỉ cover sub-feature 07 — xem gap ghi chú bên dưới
│   └── demos/
│       └── Overview.html       ← Tổng hợp yêu cầu BE + FE của Scope 1 (cập nhật 2026-07-20 — trước đó chỉ là phân tích gap VNPT SDK)
│
└── Scope_2/                                        ⏸ Sau — chưa triển khai, mọi thứ hiển thị lên admin page (+ Story vãng lai chưa thiết kế)
    ├── Specifications/
    │   └── 03_Dashboard_API_Spec.md                       ← GET /api/admin/ekyc/dashboard spec
    ├── Issues/
    │   ├── 02_FE_Issue_Admin_Attempt_History.md           ← 3 màn hình: Search → Timeline → Detail
    │   ├── 03_FE_Issue_Dashboard_Analytics.md             ← 7 KPI cards + 4 charts
    │   └── 04_FE_Issue_MRZ_Validation_CrossCheck.md       ← Phased: chờ Scope 2
    └── demos/
        ├── admin-ui-demo.html                             ← UI prototype (covers sub-feature 02 + 03)
        └── 04_FE_eKYC_MRZ.html                            ← MRZ analysis demo
```

**Sub-feature không có file vật lý (chỉ ghi nhận trong README):**
- **05 Contract Terms Checkbox Log** — ĐÃ XÓA (2026-07-15). Gộp vào `BE_Spec.md` (Phần B) — step `TERMS_AND_CONDITIONS_CONFIRMATION` đã bao phủ trọn vẹn mục đích của 05 (lưu `isAgree` + timestamp). Xem Review Log bên dưới.
- **06 Image Storage** (Scope 2) — deferred, phụ thuộc MinIO/S3 infra, chưa có tài liệu.
- **08 Incomplete Journey Log** (Scope 2) — placeholder cho Story vãng lai (hành trình chưa hoàn tất + retry eKYC), chưa brainstorm, chưa có tài liệu.

**Gap đã ghi nhận, chưa xử lý (2026-07-20):** Sub-feature 01 (Biometric Attempt Log) yêu cầu App gọi `POST /ekycs/attempt-log` kèm `sdkRawLogs`/`vnptRawData`/ảnh khi lần thử pre-submit thất bại (xem spec Phần A Section 0.5 + 7, và Issue Task 5) — nhưng chưa có FE issue nào ghi nhận việc này (khác với 07, đã có FE issue riêng). PRD mục 5 ghi "App không cần làm thêm gì", mâu thuẫn với thiết kế kỹ thuật thực tế. Quyết định 2026-07-20: **giữ nguyên scope hiện có, chưa viết FE issue cho 01** — cần PO/BE Lead quyết định trước khi implement Task 5.

---

## Status Tracker

> 📋 **Status hiện hành: xem [Tracking/kanban.html](../Tracking/kanban.html)** (filter Area = eKYC) — nguồn duy nhất `Tracking/tasks.js` (rule C7). Bảng dưới chỉ mô tả scope, KHÔNG maintain status nữa.

**Scope 1 — Đang triển khai (thuần backend/DB, không có admin UI):**

| Sub-feature | Ghi chú |
|-------------|---------|
| 01 Biometric Attempt Log (backend) | Spec: `Scope_1/Specifications/BE_Spec.md` (Phần A — canonical). Issue BE gộp chung với 07: `Scope_1/Issues/BE_Issue.md`. FE (App gọi `/ekycs/attempt-log`) chưa có issue riêng — xem gap ghi ở mục trên |
| 07 Compliance Journey Log | Redesign v2.0 — spec: `Scope_1/Specifications/BE_Spec.md` (Phần B). Issue BE gộp chung với 01 (file trên); Issue FE riêng: `Scope_1/Issues/FE_Issue.md` (App gọi API tại 9 màn hình). Chờ BE Lead + FE Lead xác nhận effort + PDPD review |

**Scope 2 — Sau (2026-07-06: mọi hiển thị log lên admin page đều defer; 2026-07-15: bổ sung Story vãng lai):**

| Sub-feature | Lý do defer | Demo |
|-------------|------------|------|
| 02 Admin Attempt History | PM quyết định defer — hiển thị log lên admin page để scope sau | (demo dùng chung với 03) |
| 03 Admin Dashboard Analytics | PM quyết định defer — triển khai sau | [admin-ui-demo.html](Scope_2/demos/admin-ui-demo.html) |
| 04 MRZ Validation (App) | PM quyết định defer — triển khai sau | [FE_eKYC_MRZ.html](Scope_2/demos/04_FE_eKYC_MRZ.html) |
| 06 Image Storage | Phụ thuộc MinIO/S3 infra setup | — |
| 08 Incomplete Journey Log (vãng lai) | Chưa brainstorm — cần quyết định lại retention policy trước khi thiết kế | — |

---

## Dependency Order

**Scope 1 (backend/DB only):**
```
Sub-feature 01 (BE — biometric log, attempt-log API)
  └─→ (độc lập) Sub-feature 07 (BE — App gọi journey-log API tại 10 màn hình + BE hook webhook FPT, bảng riêng ekyc_journey_log — xem BE_Spec.md Phần B)
```
> 05 (Contract Terms Checkbox Log) đã gộp vào 07 — không còn là dependency riêng, xem Review Log 2026-07-15.

**Scope 2 (deferred — tất cả phụ thuộc Scope 1 đã live):**
```
Sub-feature 01 (phải live)
  └─→ Sub-feature 02 (FE Admin — danh sách + detail hành trình)
  └─→ Sub-feature 03 (FE Dashboard Analytics, cần thêm BE dashboard API)
  └─→ Sub-feature 04 (App MRZ Validation)
  └─→ Sub-feature 06 (Image Storage — MinIO/S3, phục vụ hiển thị ảnh trên 02/03)

Sub-feature 07 (phải live)
  └─→ Sub-feature 08 (Incomplete Journey Log — vãng lai + retry eKYC, chưa thiết kế)
```

---

## Shared Resources

- **PRD:** [Planning/PRD_eKYC_v2.md](Planning/PRD_eKYC_v2.md) — business requirements đầy đủ cho tất cả sub-features
- **Admin UI Demo:** [Scope_2/demos/admin-ui-demo.html](Scope_2/demos/admin-ui-demo.html) — covers sub-features 02 và 03

---

## Review Log

- **2026-07-01 (a):** Reorganized từ type-based (Planning/Specs/Issues) sang feature-based structure. Xóa `FE_Issue_Admin_UI.md` (file gốc trước khi split — nội dung đã được bảo toàn 100% trong `FE_Issue_Dashboard_Analytics.md` + `FE_Issue_Admin_Attempt_History.md`).
- **2026-07-01 (b):** Phát hiện `Biometric_Log_Spec.md` xung đột schema với `Backend_Spec.md` (model `attempt_result` riêng biệt, không có `outcome`/`failure_step`/`phone_no`/`image_*_url` — các cột mà `Dashboard_API_Spec.md` và `FE_Issue_Admin_Attempt_History.md` đều phụ thuộc vào). Đã xóa `Biometric_Log_Spec.md`, chọn `Backend_Spec.md` làm canonical, và rewrite `BE_Issue_Biometric_Log_Storage.md` để align đúng schema.
- **2026-07-01 (c):** Bổ sung yêu cầu audit đầy đủ — thêm cột `sdk_raw_logs` (LONGTEXT) vào `Backend_Spec.md` để lưu nguyên văn 7 SDK log key (liveness x3, mask, compare, image paths) ngoài `vnpt_raw_data` (LOG_OCR). Cập nhật `FE_Issue_MRZ_Validation_CrossCheck.md` Task 3d — App phải gửi `sdkRawLogs` trong mọi lần gọi `POST /ekycs/attempt-log`.
- **2026-07-01 (d):** Fix gap "khách chưa mở TK thành công không tra được trên Admin page" — search endpoint trước đây giả định luôn có `e_kyc` row để lấy `fullName`, sai với case fail ngay ở pre-submit (chưa từng gọi Lotte → không có `e_kyc`). Đã sửa `Backend_Spec.md` Section 4.1: nguồn chính là `ekyc_attempt_log` (luôn có data), `e_kyc` chỉ join thêm khi tồn tại; thêm `accountStatus = NOT_SUBMITTED` suy ra từ outcome. Cập nhật `FE_Issue_Admin_Attempt_History.md`: badge trạng thái + banner "chưa mở được TK" thay vì ẩn thông tin.
- **2026-07-01 (e):** Tách Màn hình 1 (`FE_Issue_Admin_Attempt_History.md`) từ pure search-by-ID thành **danh sách phân trang + 2 tab**: "Đã mở TK thành công" / "Chưa mở TK thành công" — để Ops track tổng quan không cần biết trước CCCD. Thêm endpoint mới `GET /api/admin/ekyc/attempts/list?accountStatus=` (`Backend_Spec.md` Section 4.1b, `BE_Issue_Biometric_Log_Storage.md` Task 7). Cập nhật demo `admin-ui-demo.html` — thêm tab UI + 2 row mẫu minh họa case NOT_SUBMITTED (chưa từng chạm Lotte, không có `e_kyc`).
- **2026-07-02 (a):** Thêm sub-feature 05 `Contract_Terms_Checkbox_Log` — lưu `terms_agreed_at` (compliance) khi user tick checkbox "Tôi đã đọc và đồng ý". App ghi timestamp → gọi `POST /ekycs/attempt-log` (reuse sub-feature 01, không tạo endpoint mới) → BE update `terms_agreed_at` trên `ekyc_attempt_log`. Thêm 2 cột `terms_agreed_at` / `terms_version` vào `ekyc_attempt_log` schema (`Backend_Spec.md`). Đổi số thứ tự: Image Storage → 06.
- **2026-07-02 (b):** Scope phase 1 — defer sub-feature 03 (Admin Dashboard Analytics) và sub-feature 04 (MRZ Validation App-side) sang Phase 2 theo quyết định PM. Phase 1 chỉ gồm: 01 Biometric Log + 02 Admin Attempt History + 05 Terms Consent.
- **2026-07-04:** Thêm Compliance Journey Log (PRD_eKYC_v2.md mục 4.10) — log 11 API call trong hành trình mở tài khoản, mục tiêu compliance/audit, không hiển thị admin UI.
- **2026-07-06 (a):** Chốt storage strategy Journey Log — ghi log real-time; tiêu chí thành công = `POST /lotte/ekycs` trả HTTP 200 kèm `eKycId` + `status: success`; hành trình không đạt tiêu chí → xóa hoàn toàn (không giữ tạm 7 ngày như đề xuất trước). Ghi nhận đánh đổi: bỏ khả năng phân tích friction/fraud-pattern vì dữ liệu fail bị xóa.
- **2026-07-06 (b):** Chốt kỹ thuật — Journey Log dùng bảng riêng `ekyc_journey_log`, KHÔNG mở rộng chung `ekyc_attempt_log`, vì bảng đó phải append-only/never-delete cho sub-feature 01 (xung đột trực tiếp với chính sách xóa của Journey Log nếu share bảng).
- **2026-07-06 (c):** Thu hẹp scope Phase 1 thêm một bước — **loại sub-feature 02 (Admin Attempt History) khỏi Phase 1**, chuyển sang Phase 2 cùng 03 và 04. Lý do: PM chốt Phase 1 không triển khai bất kỳ hiển thị nào lên admin page (kể cả tra cứu hành trình, dashboard, hay cột/tab log trên trang admin hiện có — mục 4.7/4.8 trong PRD). Phase 1 giờ thuần backend/DB: 01 + 05 + Compliance Journey Log.
- **2026-07-08 (a):** Đối chiếu `Backend_Spec.md` với sample log thực tế production do dev gửi (OCR CCCD 2 mặt VNPT, liveness, face compare) — phát hiện & sửa vài chỗ mapping sai tên field JSON gốc (`vnpt_citizen_id` phải lấy từ `object.id` chứ không phải `object.citizenId` không tồn tại; `vnpt_citizen_id_chip` lấy từ `object.dict_qr.SoCCCD` chứ không phải field không tồn tại `citizenIdChip`; cột `vnpt_match_valid_date` đổi thành `vnpt_match_sex` vì field cũ không tồn tại; `fake_liveness_prob`/`fake_print_photo_prob` tách theo mặt trước/sau vì nằm sai chỗ trong bản trước). Bổ sung field mới mà data thực tế có nhưng spec bỏ sót: `mrz_line3` (MRZ CCCD gắn chip có 3 dòng), `vnpt_old_citizen_id`, `vnpt_qr_match_summary` (đối chiếu QR chip vs OCR), phát hiện đổi mặt/deepfake (`face_swapping`), phát hiện nhiều khuôn mặt (`multiple_faces`), và 6 cột VNPT logID để tra soát chéo khi audit/tranh chấp (App đã có sẵn các ID này trong luồng `/lotte/ekycs` hiện tại). Thêm Section 0.6 vào `Backend_Spec.md` — sample thực tế đã ẩn danh, cho dev tham khảo khi implement. Cập nhật `BE_Issue_Biometric_Log_Storage.md` Task 1 + Acceptance Criteria tương ứng.
- **2026-07-08 (b):** Tạo sub-feature **07 Compliance Journey Log** (`Journey_API_Reference.md`) — thay thế PRD mục 4.10 cũ (đã bị xóa khi PRD rewrite prose-only ngày 2026-07-07, để lại tham chiếu chết trong README). Danh sách 11 API đối chiếu trực tiếp với source code app `nhsv-mts-rn` (read-only): gỡ `GET /ekycs/account/exist` (không tồn tại trong code), bổ sung `GET /aws` (upload/download ảnh CCCD — bị bỏ sót hoàn toàn ở danh sách cũ), sửa path param bước lấy chi nhánh ngân hàng (`{bankCode}` không phải `{id}`). Ghi chú thêm 3 endpoint dùng chung ngoài luồng eKYC (partner, bank-branches, aws) cần phân biệt context khi log.
- **2026-07-15 (a):** PM brainstorm lại sub-feature 07 dựa trên sample thực tế (`result.json`/`journey.html`) — phát hiện thiết kế cũ (API-call-based, interceptor, `response_summary`) không bắt được các field chỉ tồn tại ở local state App (`occupation`, `tax_cd`, `isAgree` text đầy đủ). **Redesign toàn diện (v2.0):** đổi sang screen-based (App gọi `POST /ekycs/journey-log` riêng tại mỗi bước, gửi full payload kể cả base64 ảnh); bổ sung 2 step OTP (`EKYC_SEND_OTP`/`EKYC_VERIFY_OTP`); bổ sung step `ECONTRACT_SIGN_COMPLETED` — ghi qua webhook FPT có sẵn trong `EContractCustomServiceImpl.getEContractStatus()` (không qua App, khóa liên kết `e_kyc_id` để không phụ thuộc session/device khi khách ký muộn hơn). Giữ nguyên quyết định 2026-07-06(a): chỉ lưu hành trình thành công. Xóa `Journey_API_Reference.md`, thay bằng `Backend_Spec.md` (theo đúng convention của sub-feature 01). Chi tiết: `Scope_1/Specifications/BE_Spec.md` (Phần B).
- **2026-07-15 (b):** **Xóa sub-feature 05 (Contract Terms Checkbox Log)** — step `TERMS_AND_CONDITIONS_CONFIRMATION` trong Journey Log redesign (07) đã bao phủ trọn vẹn mục đích của 05 (lưu `isAgree` + timestamp), không cần cột riêng `terms_agreed_at`/`terms_version` trên `ekyc_attempt_log` nữa. Xóa folder `05_Contract_Terms_Checkbox_Log/` và 2 task tương ứng trong `Tracking/tasks.js` (`EKYC-05-BE`, `EKYC-05-FE`).
- **2026-07-15 (c):** **Tái cấu trúc thư mục theo Scope** — nhóm vật lý 01 + 07 vào `Scope_1/` (đang triển khai), 02 + 03 + 04 + 06 vào `Scope_2/` (sau). Giữ nguyên số thứ tự sub-feature xuyên suốt lịch sử review log (không renumber). Thêm placeholder `08_Incomplete_Journey_Log` (Scope 2) cho Story 2 — hành trình vãng lai/chưa hoàn tất + retry eKYC — nêu trong buổi brainstorm PM nhưng chủ động chưa thiết kế chi tiết. Cập nhật path tương ứng trong `Tracking/tasks.js`.
- **2026-07-16:** **Gộp BE Issue 01 + 07 thành 1 file** (`BE_Issue.md`) — xóa `01_BE_Issue_Biometric_Log_Storage.md` cũ, nội dung được bảo toàn 100% ở Phần A của file mới; thêm Phần B (Task 8-15) cho sub-feature 07 dựa trên spec `07_Compliance_Journey_Log_Backend_Spec.md`. Đồng thời tạo `FE_Issue.md` — issue cho App (nhsv-mts-rn) gọi API `POST /ekycs/journey-log` tại 9 màn hình, đóng gap "07 chưa có Issue doc" nêu ở review log trước.
- **2026-07-15 (d):** **Gộp tiếp theo loại tài liệu** — thay vì mỗi sub-feature có `Specifications/`/`Issues/`/`demos/` riêng (nhiều folder con chỉ có 1 file, phân mảnh), mỗi Scope giờ chỉ có 1 bộ `Specifications/`/`Issues/`/`demos/` dùng chung cho mọi sub-feature bên trong, phân biệt bằng tiền tố số ở đầu tên file (vd `01_Biometric_Attempt_Log_Backend_Spec.md`, `03_Dashboard_API_Spec.md`). File demo dùng chung 02+03 (`admin-ui-demo.html`) giữ tên không tiền tố. Cập nhật toàn bộ link chéo trong `Planning/PRD_eKYC_v2.md`, `Planning/eKYC-summary.html`, các file `Issues/`/`Specifications/` còn lại, và `Tracking/tasks.js`.
- **2026-07-20:** **Gộp 2 file BE Spec (01 + 07) thành 1** (`BE_Spec.md`) — xóa `01_Biometric_Attempt_Log_Backend_Spec.md` + `07_Compliance_Journey_Log_Backend_Spec.md` cũ, nội dung bảo toàn 100% ở Phần A + Phần B của file mới, cùng convention với Issue file đã gộp từ 2026-07-16. Specifications/Issues nay đúng 1 file BE + 1 file FE mỗi loại cho Scope 1. Cập nhật toàn bộ link chéo (README, PRD, eKYC-summary.html, `Scope_2/Specifications/03_Dashboard_API_Spec.md`, `Scope_2/Issues/02_FE_Issue_Admin_Attempt_History.md`, `Tracking/tasks.js`). **Ghi nhận gap chưa xử lý** (xem mục "Sub-feature không có file vật lý" trên): sub-feature 01 thiếu FE issue cho việc App gọi `/ekycs/attempt-log` — quyết định giữ nguyên scope hiện có, không tự thêm task mới, chờ PO xác nhận. Đồng thời cập nhật `Scope_1/demos/Overview.html` từ phân tích gap VNPT SDK (cũ, 24/05/2026) thành tổng hợp yêu cầu BE + FE hiện hành của Scope 1.

---

**Document Status:** ✅ Updated | For: Dev Team + BA + PM | Next Steps: BE Lead + FE Lead xác nhận `Scope_1/Specifications/BE_Spec.md` (Phần B) Section 10 → implement Scope 1 (01 + 07) → go-live → mở lại Scope 2 (02 + 03 + 04 + 06 + 08). PO quyết định có cần viết FE issue cho sub-feature 01 (attempt-log) trước khi implement Task 5 hay không.
