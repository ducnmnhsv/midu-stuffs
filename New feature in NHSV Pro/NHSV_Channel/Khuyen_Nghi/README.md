# Khuyến nghị (A-05)

Tab "Khuyến nghị" trên NHSV Pro — 3 luồng nội dung tư vấn của NHSV (Danh mục cổ phiếu khuyến nghị dài hạn, NHSV Rating, Khuyến nghị kỹ thuật hằng ngày) tổng hợp vào 1 entry point trên màn Home.

**Status & tracking:** xem task `NHP-KHUYENNGHI` trong [`Tracking/tasks.js`](../../../Tracking/tasks.js) / board [`Tracking/kanban.html`](../../../Tracking/kanban.html) — không duplicate trạng thái ở đây.

## Đọc theo vai trò

| Bạn là | Đọc tài liệu | Nội dung |
|---|---|---|
| **PM (Midu)** — review scope | [Planning/Overview_and_Journeys.md](./Planning/Overview_and_Journeys.md) | Tổng quan, mục tiêu, đối tượng, scope in/out, user journey, business journey, open questions cần quyết định |
| **Phòng Phân tích / Phòng QTRR** — vận hành nội dung | [Planning/Business_Process_Guide.md](./Planning/Business_Process_Guide.md) | Quy trình thao tác trên Admin Tool cho từng sub-tab, trách nhiệm mới, SLA, disclaimer pháp lý |
| **Dev (BE/Mobile FE/Admin FE)** — đánh giá khả thi & estimate | [Specifications/Feature_Specification.md](./Specifications/Feature_Specification.md) | Data model/ERD, API reference, sequence & state diagram, business rules kỹ thuật, NFR, dependencies, estimate |
| Ai cần bản gốc đầy đủ (mọi version note v1.1→v1.3) | [Planning/PRD.md](./Planning/PRD.md) | Nguồn requirement gốc — 3 tài liệu trên đều tổng hợp từ đây |

## Cấu trúc thư mục

```
Khuyen_Nghi/
├── README.md                          — file này, entry point
├── Planning/
│   ├── PRD.md                         — PRD gốc (nguồn duy nhất của requirement, v1.3)
│   ├── Overview_and_Journeys.md       — cho PM review scope
│   └── Business_Process_Guide.md      — cho Phòng PT/QTRR vận hành
├── Specifications/
│   └── Feature_Specification.md       — cho Dev (data model, API, diagrams)
├── Design/                            — mockup HTML tham khảo UI (đã xác nhận cập nhật theo v1.3)
│   ├── Design.html
│   ├── Design_Variations.html
│   └── admin-demo.html
└── Archive/                           — tài liệu lịch sử, đã merge nội dung vào Planning/Specifications
    ├── Spec.html                      — bản spec HTML gốc (đối chiếu để build Feature_Specification.md)
    └── Edge_Cases_Review.html         — 18 case rà soát 2026-07-23, quyết định đã merge vào PRD v1.3
```

## Lưu ý

- **Nguồn sự thật (source of truth):** `Planning/PRD.md` — mọi thay đổi scope cập nhật ở đây trước, rồi mới phản ánh sang 3 tài liệu tổng hợp.
- `Design/*.html` là mockup UI. PRD ghi Q10 nghi ngờ các file này còn ở model v1.1, nhưng rà soát trực tiếp (2026-07-23) xác nhận cả 3 file đã có annotation `(v1.2)`/`(v1.3)` đầy đủ — khớp với Home entry point, tab order, bỏ field cũ, badge động, rolling 14 ngày, cảnh báo Excel, ràng buộc unique mã/ngày. Vẫn có thể tham khảo layout trực tiếp từ đây.
- `Archive/*.html` không còn là tài liệu sống — giữ lại cho lịch sử quyết định, không sửa tiếp ở đây.

---

Document Status: 📋 Draft | For: Mọi vai trò liên quan A-05 | Next Steps: Q10 đã resolved (Design/*.html khớp v1.3) — còn lại Q6 (Legal duyệt disclaimer), Q9 (sequencing A-06), Q11 (chính sách login) trước khi giao IT estimate chi tiết
