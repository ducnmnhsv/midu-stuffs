---
name: feedback-doc-only
description: "User chỉ muốn tạo tài liệu trong tradex-monitoring, không implement code thực tế"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 64697273-9c0b-4600-92c4-13759d2f776f
---

Trong workspace tradex-monitoring, khi được giao task phân tích hoặc thiết kế tính năng, chỉ tạo tài liệu (PRD, spec, issue document) — không implement code thực sự vào source files.

**Why:** tradex-monitoring là workspace documentation/specs/issue tracking, không phải repo để sửa code. Source code thực nằm ở các repo khác (ekyc-admin, nhsv-mts-rn, v.v.).

**How to apply:** Khi plan có bước "implement code changes", chuyển thành "viết spec/code snippet tham khảo trong Specifications/ folder". Không sửa file `.java`, `.ts`, hay các source file thực tế. Code snippet trong tài liệu (Specifications/) là OK để Dev tham khảo, nhưng không phải implementation thực sự.
