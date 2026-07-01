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
├── 03_Admin_Dashboard_Analytics/               🔴 Blocked — BE API chưa implement
│   ├── Specifications/
│   │   └── Dashboard_API_Spec.md               ← GET /api/admin/ekyc/dashboard spec
│   ├── Issues/
│   │   └── FE_Issue_Dashboard_Analytics.md     ← 7 KPI cards + 4 charts
│   └── demos/
│       └── admin-ui-demo.html                  ← UI prototype (covers sub-feature 02 + 03)
│
├── 04_MRZ_Validation/                          🟡 Phase 1 ready (App)
│   ├── Issues/
│   │   └── FE_Issue_MRZ_Validation_CrossCheck.md ← Phased: Phase 1 unblocked
│   └── demos/
│       └── [FE]eKYC-MRZ.html                  ← MRZ analysis demo
│
└── 05_Image_Storage/                           ⬜ Deferred — scope TBD
    (chờ PM confirm: v1 hay v2?)
```

---

## Status Tracker

| Sub-feature | Status | Blocker | Demo |
|-------------|--------|---------|------|
| 01 Biometric Attempt Log | ✅ Spec ready | Chờ BE dev | [eKYC-analysis.html](01_Biometric_Attempt_Log/demos/[FE]eKYC-analysis.html) |
| 02 Admin Attempt History | 🔵 Blocked on BE | Sub-feature 01 phải live | [admin-ui-demo.html](03_Admin_Dashboard_Analytics/demos/admin-ui-demo.html) |
| 03 Admin Dashboard Analytics | 🔴 BE API missing | Dashboard_API_Spec cần implement | [admin-ui-demo.html](03_Admin_Dashboard_Analytics/demos/admin-ui-demo.html) |
| 04 MRZ Validation | 🟡 Phase 1 ready | Phase 2–3 cần sub-feature 01 | [[FE]eKYC-MRZ.html](04_MRZ_Validation/demos/[FE]eKYC-MRZ.html) |
| 05 Image Storage | ⬜ Deferred | PM confirm scope | — |

---

## Dependency Order

```
Sub-feature 01 (BE)
  └─→ Sub-feature 02 (FE Admin UI)
  └─→ Sub-feature 03 (FE Dashboard — cần thêm Dashboard API BE)
  └─→ Sub-feature 04 Phase 2–3 (App MRZ gửi lên BE)

Sub-feature 05 (Image Storage) — independent, deferred
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

---

**Document Status:** ✅ Updated | For: Dev Team + BA + PM | Next Steps: BE implement sub-feature 01 → unblocks 02 + 03 + 04 Phase 2
