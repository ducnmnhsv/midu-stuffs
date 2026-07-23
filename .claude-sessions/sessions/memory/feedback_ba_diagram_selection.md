---
name: feedback-ba-diagram-selection
description: "Khi tạo spec/BRD/PRD/issue TradeX phải auto-detect và đề xuất đúng loại diagram (trong 7 loại BA chuẩn), không mặc định chỉ dùng flowchart"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 621ac3ea-0a5e-4acb-bb16-ab8cab3d0c28
  modified: 2026-07-23T15:06:12.566Z
---

Khi tạo hoặc review spec/BRD/PRD/issue liên quan TradeX/NHSV Pro, phải quét ngữ cảnh để chủ động đề xuất đúng loại diagram trong 7 loại BA chuẩn (Flowchart, BPMN, Use Case, Sequence, Activity, ERD, State Machine) — thay vì mặc định generate flowchart cho mọi trường hợp.

Chi tiết đầy đủ (heuristic map ngữ cảnh → diagram, quy tắc kết hợp theo loại document, quy trình áp dụng) nằm ở `Knowledge/BA-Diagram-Standards/BA_Diagram_Selection_Guide.md` trong repo tradex-monitoring (đã tách khỏi `Knowledge/TradeX/` vì đây là rule chung, không riêng TradeX).

**Why:** User (Midu, PO/BA) nhận thấy agent thường chỉ generate 1 loại diagram mặc định dù ngữ cảnh cần loại khác (vd: BPMN cho multi-actor process, Sequence cho luồng gọi API giữa service, State cho lifecycle/status, ERD cho schema). Muốn agent thông minh hơn, tự chọn đúng loại theo tín hiệu trong yêu cầu.

**How to apply:**
- Trước khi viết spec/BRD/PRD/issue có liên quan đến flow/process/API/schema/status, đọc file guide trên (nếu đang làm trong repo tradex-monitoring) để lấy bảng heuristic.
- Nếu 1 feature chạm nhiều khía cạnh → đề xuất combo diagram, thứ tự: Flowchart/BPMN (business) → Use Case (nếu đang scope) → Sequence (nếu có API) → ERD (nếu có schema) → State (nếu có lifecycle) → Activity (nếu có logic phức tạp).
- `Planning/` docs (PM đọc) chỉ dùng diagram cấp business (Flowchart/BPMN/Use Case), không chèn diagram kỹ thuật — khớp rule [[feedback_tradex_knowledge_first]] và C3 (PM-readability gate) trong CLAUDE.md.
- Nếu ngữ cảnh mơ hồ, hỏi tối đa 1 câu clarify thay vì tự chọn đại.
