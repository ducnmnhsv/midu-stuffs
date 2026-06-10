# eKYC Attempt History — Tra cứu Hành trình Mở tài khoản

## Mục tiêu

Lưu trữ toàn bộ lịch sử các lần thử eKYC của từng khách hàng (kể cả fail), cho phép team vận hành tra cứu và phân tích nguyên nhân thất bại, theo dõi hành trình từ lần thử đầu tiên đến khi mở tài khoản thành công.

## Vấn đề hiện tại

Hệ thống hiện tại **xóa** các lần thử PENDING cũ khi user submit lại eKYC. Kết quả: không thể biết user fail bao nhiêu lần, fail ở bước nào, lý do cụ thể là gì.

## Phạm vi

| Thành phần | Thay đổi |
|-----------|---------|
| DB | Bảng mới `ekyc_attempt_log` (46 cột) + 2 cột mới trong `e_kyc` |
| Backend (ekyc-admin) | Lưu log mỗi lần submit, upload ảnh S3/MinIO, update link khi mở TK thành công |
| App (nhsv-mts-rn) | Gọi `POST /ekycs/attempt-log` khi pre-submit failure (SDK thất bại trước khi gọi BE) |
| Admin UI | Dashboard KPI + 3 màn hình tra cứu: Search → Journey → Detail |

## Tính năng nổi bật (v2.0)

- **Lưu ảnh CCCD mỗi lần thử** — upload lên S3/MinIO, Admin có thể xem lại ảnh từng lần fail
- **Capture pre-submit failures** — App gọi API mới khi liveness/blur/face-compare thất bại, trước khi đến BE
- **Dashboard eKYC** — tỉ lệ lỗi theo bước, top failure reasons, 7-day trend, fraud detection metrics
- **Extended VNPT fields** — nationality, citizenIdChip, liveness results, face compare score

## Trạng thái

| Hạng mục | Trạng thái |
|---------|-----------|
| PRD / Planning (v2.0) | ✅ Hoàn thành |
| DB Specification (v2.0) | ✅ Hoàn thành |
| Backend Specification (v2.0) | ✅ Hoàn thành |
| FE Issue | ✅ Hoàn thành |
| Admin UI Demo | ✅ [admin-ui-demo.html](demos/admin-ui-demo.html) |
| Implementation | 🔲 Chưa bắt đầu |

## Documents

- [01_PRD_eKYC_Attempt_History.md](Planning/01_PRD_eKYC_Attempt_History.md) — Yêu cầu nghiệp vụ (v2.0)
- [Backend_Spec.md](Specifications/Backend_Spec.md) — DB Schema + Code changes + API endpoints + Image upload service
- [FE_Issue_Admin_UI.md](Issues/FE_Issue_Admin_UI.md) — FE issue: Dashboard Analytics + Tra cứu Hành trình
- [FE_Issue_MRZ_Validation_CrossCheck.md](Issues/FE_Issue_MRZ_Validation_CrossCheck.md) — FE issue: MRZ Validation (App)
- [demos/](demos/) — UI prototypes & analysis HTML files

**Document Status:** Draft v2.0 | **For:** Dev Team + BA | **Next Steps:** Review với team backend trước khi implement
