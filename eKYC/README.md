# eKYC v2.0 — Tổng quan

Hệ thống lưu trữ biometric log, tra cứu hành trình, và dashboard analytics cho quá trình mở tài khoản qua eKYC (VNPT).

**Vấn đề cốt lõi:** `CustomEKycService.java:211` xóa records PENDING cũ khi user retry → không có audit trail, không trace được lý do fail.

---

## Cấu trúc Sub-feature

```
eKYC/
├── Planning/
│   ├── PRD_eKYC_v2.md                          ← Yêu cầu nghiệp vụ đầy đủ
│   └── eKYC-summary.html                       ← Tổng hợp visual (HTML)
│
│ ── PHASE 1 (current scope) ─────────────────────────────────────────────
│
├── 01_Biometric_Attempt_Log/                   ✅ Spec ready — chờ BE implement
│   ├── Specifications/
│   │   └── Backend_Spec.md                     ← DB schema + service + REST endpoints (canonical — bao gồm raw SDK log audit)
│   ├── Issues/
│   │   └── BE_Issue_Biometric_Log_Storage.md   ← 7 BE tasks cho developer
│   └── demos/
│       └── [FE]eKYC-analysis.html              ← Phân tích VNPT SDK data
│
├── 02_Admin_Attempt_History/                   🔵 Blocked — chờ sub-feature 01
│   └── Issues/
│       └── FE_Issue_Admin_Attempt_History.md   ← 3 màn hình: Search → Timeline → Detail
│   (demo dùng chung với 03 — xem admin-ui-demo.html)
│
├── 05_Contract_Terms_Checkbox_Log/             🔵 Blocked — chờ sub-feature 01 deploy POST /ekycs/attempt-log
│   └── Issues/
│       ├── FE_Issue_Checkbox_Analytics_Log.md  ← App ghi termsAgreedAt + gọi attempt-log API
│       └── BE_Issue_Checkbox_Consent_Storage.md ← Thêm cột terms_agreed_at vào ekyc_attempt_log
│
│ ── PHASE 2 (deferred) ──────────────────────────────────────────────────
│
├── 03_Admin_Dashboard_Analytics/               ⏸ Phase 2 — deferred
│   ├── Specifications/
│   │   └── Dashboard_API_Spec.md               ← GET /api/admin/ekyc/dashboard spec
│   ├── Issues/
│   │   └── FE_Issue_Dashboard_Analytics.md     ← 7 KPI cards + 4 charts
│   └── demos/
│       └── admin-ui-demo.html                  ← UI prototype (covers sub-feature 02 + 03)
│
├── 04_MRZ_Validation/                          ⏸ Phase 2 — deferred (App-side validation)
│   ├── Issues/
│   │   └── FE_Issue_MRZ_Validation_CrossCheck.md ← Phased: chờ Phase 2
│   └── demos/
│       └── [FE]eKYC-MRZ.html                  ← MRZ analysis demo
│
└── 06_Image_Storage/                           ⬜ Scope TBD
    (chờ PM confirm)
```

---

## Status Tracker

**Phase 1 — Current scope:**

| Sub-feature | Status | Blocker |
|-------------|--------|---------|
| 01 Biometric Attempt Log | ✅ Spec ready | Chờ BE dev |
| 02 Admin Attempt History | 🔵 Blocked on BE | Sub-feature 01 phải live |
| 05 Contract Terms Checkbox Log | 🔵 Blocked on BE | Sub-feature 01 deploy `POST /ekycs/attempt-log` trước |

**Phase 2 — Deferred:**

| Sub-feature | Lý do defer | Demo |
|-------------|------------|------|
| 03 Admin Dashboard Analytics | PM quyết định defer — triển khai phase sau | [admin-ui-demo.html](03_Admin_Dashboard_Analytics/demos/admin-ui-demo.html) |
| 04 MRZ Validation (App) | PM quyết định defer — triển khai phase sau | [[FE]eKYC-MRZ.html](04_MRZ_Validation/demos/[FE]eKYC-MRZ.html) |

**Scope TBD:**

| Sub-feature | Status |
|-------------|--------|
| 06 Image Storage | ⬜ Chờ PM confirm scope |

---

## Dependency Order

**Phase 1:**
```
Sub-feature 01 (BE — biometric log, attempt-log API)
  └─→ Sub-feature 02 (FE Admin — danh sách + detail)
  └─→ Sub-feature 05 (FE + BE — terms consent, reuse POST /ekycs/attempt-log)
```

**Phase 2 (deferred):**
```
Sub-feature 01 (phải live) + Sub-feature 03 BE API
  └─→ Sub-feature 03 (FE Dashboard Analytics)
Sub-feature 01 (phải live)
  └─→ Sub-feature 04 (App MRZ Validation phase 2–3)
```

---

## Shared Resources

- **PRD:** [Planning/PRD_eKYC_v2.md](Planning/PRD_eKYC_v2.md) — business requirements đầy đủ cho tất cả sub-features
- **Admin UI Demo:** [03_Admin_Dashboard_Analytics/demos/admin-ui-demo.html](03_Admin_Dashboard_Analytics/demos/admin-ui-demo.html) — covers sub-features 02 và 03

---

## Review Log

- **2026-07-01 (a):** Reorganized từ type-based (Planning/Specs/Issues) sang feature-based structure. Xóa `FE_Issue_Admin_UI.md` (file gốc trước khi split — nội dung đã được bảo toàn 100% trong `FE_Issue_Dashboard_Analytics.md` + `FE_Issue_Admin_Attempt_History.md`).
- **2026-07-01 (b):** Phát hiện `Biometric_Log_Spec.md` xung đột schema với `Backend_Spec.md` (model `attempt_result` riêng biệt, không có `outcome`/`failure_step`/`phone_no`/`image_*_url` — các cột mà `Dashboard_API_Spec.md` và `FE_Issue_Admin_Attempt_History.md` đều phụ thuộc vào). Đã xóa `Biometric_Log_Spec.md`, chọn `Backend_Spec.md` làm canonical, và rewrite `BE_Issue_Biometric_Log_Storage.md` để align đúng schema.
- **2026-07-01 (c):** Bổ sung yêu cầu audit đầy đủ — thêm cột `sdk_raw_logs` (LONGTEXT) vào `Backend_Spec.md` để lưu nguyên văn 7 SDK log key (liveness x3, mask, compare, image paths) ngoài `vnpt_raw_data` (LOG_OCR). Cập nhật `FE_Issue_MRZ_Validation_CrossCheck.md` Task 3d — App phải gửi `sdkRawLogs` trong mọi lần gọi `POST /ekycs/attempt-log`.
- **2026-07-01 (d):** Fix gap "khách chưa mở TK thành công không tra được trên Admin page" — search endpoint trước đây giả định luôn có `e_kyc` row để lấy `fullName`, sai với case fail ngay ở pre-submit (chưa từng gọi Lotte → không có `e_kyc`). Đã sửa `Backend_Spec.md` Section 4.1: nguồn chính là `ekyc_attempt_log` (luôn có data), `e_kyc` chỉ join thêm khi tồn tại; thêm `accountStatus = NOT_SUBMITTED` suy ra từ outcome. Cập nhật `FE_Issue_Admin_Attempt_History.md`: badge trạng thái + banner "chưa mở được TK" thay vì ẩn thông tin.
- **2026-07-01 (e):** Tách Màn hình 1 (`FE_Issue_Admin_Attempt_History.md`) từ pure search-by-ID thành **danh sách phân trang + 2 tab**: "Đã mở TK thành công" / "Chưa mở TK thành công" — để Ops track tổng quan không cần biết trước CCCD. Thêm endpoint mới `GET /api/admin/ekyc/attempts/list?accountStatus=` (`Backend_Spec.md` Section 4.1b, `BE_Issue_Biometric_Log_Storage.md` Task 7). Cập nhật demo `admin-ui-demo.html` — thêm tab UI + 2 row mẫu minh họa case NOT_SUBMITTED (chưa từng chạm Lotte, không có `e_kyc`).
- **2026-07-02 (a):** Thêm sub-feature 05 `Contract_Terms_Checkbox_Log` — lưu `terms_agreed_at` (compliance) khi user tick checkbox "Tôi đã đọc và đồng ý". App ghi timestamp → gọi `POST /ekycs/attempt-log` (reuse sub-feature 01, không tạo endpoint mới) → BE update `terms_agreed_at` trên `ekyc_attempt_log`. Thêm 2 cột `terms_agreed_at` / `terms_version` vào `ekyc_attempt_log` schema (`Backend_Spec.md`). Đổi số thứ tự: Image Storage → 06.
- **2026-07-02 (b):** Scope phase 1 — defer sub-feature 03 (Admin Dashboard Analytics) và sub-feature 04 (MRZ Validation App-side) sang Phase 2 theo quyết định PM. Phase 1 chỉ gồm: 01 Biometric Log + 02 Admin Attempt History + 05 Terms Consent.

---

**Document Status:** ✅ Updated | For: Dev Team + BA + PM | Next Steps: BE implement sub-feature 01 → unblocks 02 + 03 + 04 Phase 2
