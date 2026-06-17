---
name: feedback-figma-tooling
description: "Midu dùng figma-cli (đã cài sẵn) để thao tác Figma, không dùng Figma MCP authenticate flow"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: a4daa406-9103-4bd7-8ffd-690418a48f6c
---

Dùng figma-cli đã cài đặt sẵn để thao tác Figma (read frames, update, export). Không dùng `mcp__claude_ai_Figma__authenticate` flow.

**Why:** figma-cli đã được setup và hoạt động trong môi trường của Midu; MCP flow yêu cầu OAuth redirect mỗi session.

**How to apply:** Khi có task liên quan đến Figma (design UI, update frame, export), luôn dùng figma-cli CLI commands thay vì Figma MCP tools.
