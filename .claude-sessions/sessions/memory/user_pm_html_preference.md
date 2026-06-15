---
name: user-pm-html-preference
description: "User là PM, ưa dùng HTML files cho tracking/overview vì dễ nhìn và bao quát"
metadata: 
  node_type: memory
  type: user
  originSessionId: 6a875e6f-3dd0-49c1-8a43-0bea0b7403e7
---

User là PM (Product Manager). Khi làm việc với tracking docs, overview, feature breakdown:

- **Ưu tiên HTML** cho các file PM đọc (tracking, overview, feature list, issue summary)
- HTML dễ nhìn, bao quát hơn Markdown với PM
- **Giữ lại các HTML files** hiện có, không xóa hay replace bằng .md
- Markdown vẫn OK cho dev docs (specs, BE issues, technical details)

**Why:** PM đọc file HTML hiệu quả hơn — layout visual, không cần render tool.  
**How to apply:** Khi tạo PM-facing docs (tracking, overview, feature list), ưu tiên HTML. Khi tạo dev-facing docs (spec, issue), dùng .md theo CLAUDE.md convention.
