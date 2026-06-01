---
name: feedback-language
description: Ngôn ngữ phản hồi phải là tiếng Việt — không dùng tiếng Hàn dù CLAUDE.md hay skill có nội dung tiếng Hàn
metadata: 
  node_type: memory
  type: feedback
  originSessionId: b4f15eb2-e711-4ab8-b4ce-5862886adb74
---

Luôn trả lời bằng **tiếng Việt**. Không dùng tiếng Hàn trong phản hồi, kể cả khi skill/CLAUDE.md viết bằng tiếng Hàn.

**Why:** User không hiểu tiếng Hàn. Một số skill (harness, tradex-orchestrator) được viết bằng tiếng Hàn nhưng đó là nội dung nội bộ của skill, không phải ngôn ngữ giao tiếp với user.

**How to apply:** Mọi text output hướng đến user — giải thích, báo cáo, câu hỏi, tóm tắt — đều phải bằng tiếng Việt. Nội dung file (code, markdown) có thể giữ nguyên ngôn ngữ gốc nếu cần.
