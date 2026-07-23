# BA Diagram Selection Guide — NHSV Pro / TradeX

> Mục đích: rule cho Claude (Chat/Code) tự động **detect ngữ cảnh** công việc của Midu (PO/BA) và **chủ động đề xuất/generate đúng loại diagram** phù hợp, thay vì chỉ generate 1 loại mặc định (thường là flowchart).
> Nguồn: tổng hợp từ "7 diagram BA thường dùng" + áp dụng vào context TradeX/NHSV Pro.

---

## 1. Bảng tra cứu nhanh — 7 loại diagram

| # | Diagram | Trả lời câu hỏi | Dùng khi | Audience | Ký hiệu chính |
|---|---|---|---|---|---|
| 1 | **Flowchart** | Quy trình diễn ra như thế nào? | User flow đơn giản, tuần tự, ít actor | PM, mọi người (dễ đọc nhất) | Start/End (oval), Process (rect), Decision (diamond), Arrow |
| 2 | **BPMN** | Bộ phận nào làm gì? | Process phức tạp, nhiều phòng ban/actor, cần phân trách nhiệm (swimlane) | PM + Cross-team (Customer/System/eKYC/Bank Officer/Core) | Swimlane, Start/End event (tròn), User task/System task, Gateway (◇), Sequence flow, Message flow (nét đứt) |
| 3 | **Use Case Diagram** | Người dùng làm được gì? Actor là ai? System hỗ trợ gì? | Giai đoạn phân tích requirement, scoping feature/PRD | PM, Stakeholder | Actor (stick figure), Use case (oval), Association, `<<include>>` (bắt buộc), `<<extend>>` (tùy chọn) |
| 4 | **Sequence Diagram** | Ai gọi ai trước? Hệ thống tương tác ra sao theo thời gian? | Cần thấy rõ luồng gọi API giữa các service | Dev (BE/FE cực thích) | Lifeline (nét đứt dọc), Message (mũi tên liền = request), Return message (nét đứt = response), Activation bar, `alt` block (nhánh điều kiện) |
| 5 | **Activity Diagram** | Logic xử lý như thế nào? | Mô tả logic nội bộ hệ thống, có decision rẽ nhánh, có xử lý song song (fork/join) | Dev, BA phân tích logic | Initial node (●), Action (rounded rect), Decision (◇), Fork/Join (thanh ngang), Swimlane theo actor/service, Final node (◉) |
| 6 | **ERD** | Dữ liệu liên kết thế nào? | Làm việc với database, thiết kế/rà soát schema | Dev (BE), DBA | Entity (bảng), PK (khóa chính), FK (khóa ngoại), quan hệ 1–1, 1–N, N–N |
| 7 | **State (Machine) Diagram** | Đối tượng thay đổi trạng thái ra sao? | Khi 1 object có nhiều trạng thái (vòng đời), đặc biệt approval workflow | Dev + PM (map business rule về status) | Start (●) / End (◉), State (rounded rect), Transition (mũi tên có label = event/action), Optional transition (nét đứt) |

---

## 2. Auto-detect: heuristic map ngữ cảnh → diagram

Claude cần scan nội dung request/spec/issue để tìm **tín hiệu ngữ cảnh** dưới đây, rồi tự đề xuất diagram tương ứng (có thể đề xuất **nhiều loại cùng lúc** nếu 1 feature chạm nhiều khía cạnh):

| Tín hiệu trong request (keyword / ý định) | → Diagram | Ví dụ trong NHSV Pro / TradeX |
|---|---|---|
| "user đi qua các bước", "flow màn hình", "user journey" | Flowchart | KH mở tài khoản phái sinh, luồng eKYC trên app |
| "nhiều bên tham gia", "phòng ban", "ai chịu trách nhiệm bước nào", "handoff giữa team" | BPMN (swimlane) | Luồng duyệt hồ sơ derivatives: Customer → System → eKYC Service → Bank Officer → Core Lotte |
| "actor nào dùng được gì", "scope feature", "PRD mới bắt đầu", "phân tích requirement" | Use Case Diagram | Scoping tính năng NH Research, Recommendations (A-05) giai đoạn đầu PRD |
| "API nào gọi API nào", "thứ tự request/response", "FE gọi BE thế nào", "trace 1 luồng qua service" | Sequence Diagram | rest-proxy → Kafka → order-v2 / lotte-bridge → Core Lotte; luồng notification NHMTS-88 (Admin Portal → OneSignal API) |
| "logic xử lý nội bộ", "có rẽ nhánh + xử lý song song", "BE decision logic" | Activity Diagram | Logic chấm điểm margin, xử lý song song validate + chấm điểm tín dụng |
| "thiết kế bảng", "schema", "quan hệ dữ liệu", "table nào FK tới table nào" | ERD | `t_notification` ↔ `t_notification_recipient_state`, schema DR sub-account |
| "trạng thái của object", "lifecycle", "status chuyển sang status khác khi nào" | State Diagram | Order status (Pending → Sent → Filled/Cancelled), Notification recipient state, Application status (Draft → Submitted → Approved/Rejected) |

**Quy tắc kết hợp theo loại document (map với routing rules hiện có):**

| Loại document | Diagram nên dùng | Lý do |
|---|---|---|
| `Planning/` (PM đọc, KHÔNG code) | Flowchart, BPMN, Use Case | Prose + diagram, không cần chi tiết kỹ thuật |
| `Specifications/` (Dev đọc, code OK) | Sequence, ERD, State, Activity | Cần chi tiết API/data/logic |
| `Issues/` (FE/BE) | Sequence (nếu liên quan API) + State (nếu liên quan status) | Dev cần trace nhanh luồng gọi + business rule status |

---

## 3. Quy trình áp dụng cho Claude (rule hành vi)

1. Khi Midu mô tả 1 feature/flow mới (spec, PRD, issue, incident, notification design...), Claude **tự quét** nội dung theo bảng mục 2.
2. Nếu phát hiện ≥1 tín hiệu rõ ràng → **chủ động đề xuất** loại diagram phù hợp kèm lý do ngắn (không hỏi lại nếu tín hiệu đã rõ).
3. Nếu 1 feature chạm nhiều khía cạnh (vd: vừa có user flow, vừa có API call, vừa có DB mới) → đề xuất **combo diagram**, sắp xếp theo thứ tự: Flowchart/BPMN (business) → Use Case (nếu đang scope) → Sequence (nếu có API) → ERD (nếu có schema) → State (nếu có lifecycle) → Activity (nếu có logic phức tạp).
4. Diagram luôn generate dưới dạng Mermaid hoặc SVG (Visualizer/Excalidraw MCP nếu có), kèm chú thích ký hiệu ngắn gọn giống style ảnh gốc.
5. Với `Planning/` docs — không được chèn diagram dạng code block kỹ thuật (theo rule C3 hiện có), chỉ dùng diagram cấp business (Flowchart/BPMN/Use Case).
6. Nếu ngữ cảnh mơ hồ (không rõ nên dùng loại nào) → hỏi tối đa 1 câu clarify kiểu: "Bạn cần mô tả *luồng người dùng* hay *luồng gọi API giữa service*?" thay vì tự chọn đại.

---

## 4. Ghi chú áp dụng riêng cho NHSV Pro / TradeX

- **Derivatives go-live**: State Diagram rất hợp cho order status, sub-account status; Sequence Diagram hợp cho luồng DR data qua realtime-v2/ws-v2/market-query-v2.
- **NHMTS-88 Notifications**: Sequence (Admin Portal → OneSignal Create Notification API) + ERD (`t_notification` / `t_notification_recipient_state`) + State (per-account recipient state: pending → sent → read).
- **Incident reports**: Sequence Diagram hữu ích để minh họa root cause (vd. luồng `market-collector-lotte` → `downloadSymbol` → MCI01/MCI02 load balancing).
- **NH Research / Recommendations (content features)**: giai đoạn PRD ban đầu ưu tiên Use Case Diagram để chốt scope actor trước khi viết Specifications chi tiết.

---

**Document Status:** ✅ Complete | For: Claude Code (spec/BRD/issue generation) | Next Steps: Áp dụng heuristic ở mục 2–3 mỗi khi tạo spec/BRD/issue TradeX
**Last Updated:** 2026-07-23
