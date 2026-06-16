---
name: feedback-tradex-knowledge-first
description: Luôn đọc Knowledge/TradeX/ trước khi bắt đầu bất kỳ spec/PRD/API/issue nào liên quan đến TradeX system
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 31e81b51-279f-4772-ba2e-ef22d28773f1
---

Trước khi viết hoặc review bất kỳ tài liệu nào liên quan đến TradeX system — spec, PRD, API spec, Jira issue — phải đọc các file trong `Knowledge/TradeX/` trước.

**Why:** Bỏ qua bước này dẫn đến vi phạm convention (sai pagination params, sai HTTP status code, sai error format, sai naming case) phải fix lại nhiều lần — tốn token và output không chính xác ngay từ đầu. Đã xảy ra trong NH Research spec: `pageSize` thay vì `fetchCount`, HTTP 201 thay vì 200, snake_case thay vì camelCase, pagination echo trong response body.

**How to apply:**
- Bắt đầu mọi task spec/PRD/API/issue → đọc `Knowledge/TradeX/API Standards/tradex-api-conventions.md` trước tiên
- Nếu task liên quan đến API template → đọc thêm `Knowledge/TradeX/API Standards/tradex-api-spec-template.md`
- Chỉ sau khi đã load conventions mới bắt đầu draft nội dung
- Áp dụng cho cả review ("check API này có đúng convention không") — đọc conventions trước khi nhận xét

Link: [[project-nhsv-new-features]]
