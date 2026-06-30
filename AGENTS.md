# AGENTS.md

**TradeX Monitoring Project - AI Agent Instructions**

> Tool chính: **Claude Code** (CLI). Không dùng Cursor/gstack/BMAD.

---

## Project Overview

Dự án tradex-monitoring — workspace PM của Midu (NHSV), phục vụ:
- Phân tích API TradeX và trace hệ thống
- Tạo API Specification, PM/BA doc, FE Issue cho Derivatives
- Tracking công việc cá nhân

**Domain:** Chứng khoán Việt Nam | **System:** TradeX Microservices Backend

---

## Skills (Claude Code)

Bốn skill chính trong `.claude/skills/`, Claude Code tự động gọi đúng skill theo mô tả:

| Skill | Trigger tự động | Vai trò |
|-------|----------------|---------|
| **tradex-orchestrator** | Mọi yêu cầu tạo/sửa spec/doc/issue, phân tích API | Điều phối pipeline analyst→creator→validator |
| **tradex-analyst** | "API hoạt động thế nào", "service nào xử lý", "trace" | Phân tích API, Kafka flow, cơ chế hệ thống |
| **tradex-creator** | "tạo spec", "viết doc", "create issue", "FE issue" | Tạo API Spec, PM doc, FE Issue |
| **tradex-validator** | "validate", "check convention", "review spec" | Kiểm tra naming, response format, convention |

**Luồng tự động (orchestrator-first):**
```
User request → tradex-orchestrator
                    │
                    ├── Phase 1 → tradex-analyst  (truy vết API/service)
                    ├── Phase 2 → tradex-creator  (tạo tài liệu)
                    └── Phase 3 → tradex-validator (kiểm tra convention)
```

Câu hỏi đơn giản ("field này là gì", "Kafka topic của X là gì") → trả lời trực tiếp, không cần pipeline.

---

## Knowledge Bases

| Knowledge Base | Location | Dùng khi |
|----------------|----------|----------|
| **TradeX source snapshot** | `Knowledge/TradeX-MCP/` | Cần đọc source code 15 services |
| **TradeX Knowledge** | `Knowledge/TradeX/` | PM knowledge — đọc trước khi scan code |

### TradeX Knowledge (Knowledge-First Rule)

Đọc `Knowledge/TradeX/` TRƯỚC khi scan codebase.

```
Knowledge/TradeX/
├── System/         # Cơ chế production đang chạy
├── API Standards/  # Convention — tradex-api-conventions.md, tradex-api-spec-template.md
└── Planning/       # Order API mapping (regular + conditional)
```

### FE Repo (Read-Only)

| Item | Value |
|------|--------|
| **Path** | `/Users/ducnguyen/Documents/project/nhsv-mts-rn` |
| **Dùng để** | Đọc cấu trúc screens/components khi tạo FE issue cho Derivatives |
| **Tuyệt đối không** | Sửa file trong repo này |

### Midu-path (Second Brain)

| Item | Value |
|------|--------|
| **Path** | `/Users/nguyenduc/Personal/Repositories/Midu-path` |
| **Khi nào dùng** | Ghi learnings, cập nhật CV/portfolio, "second brain" |
| **Không** | commit/push — chỉ đề xuất nội dung + path |

---

## File Routing

| Loại output | Destination |
|-------------|-------------|
| Derivatives API Spec | `Derivatives/Planning documentation/{Category}/Specifications/` |
| Derivatives Planning/PRD | `Derivatives/Planning documentation/{Category}/Planning/` |
| Derivatives Issue (BE/FE) | `Derivatives/Planning documentation/{Category}/Issues/` |
| Smart OTP | `Smart-OTP/{Issues\|Planning\|Specifications}/` |
| NHSV Pro new feature | `New feature in NHSV Pro/{FeatureArea}/` |
| Analytics | `Analytics/NHSV-Pro/` |

---

## Quick Reference

### Servers

| Environment | URL |
|-------------|-----|
| Production | https://nhsvpro.nhsv.vn |
| UAT | https://tnhsvpro.nhsv.vn |

### Postman Collections

| Collection | Vai trò |
|------------|---------|
| **TradeX API v2** (`34274942-d349da1f-...`) | Reference only — không tạo request test |
| **TradeX QA session** (`34274942-8fe5bddd-...`) | Test requests — folder theo issue |

### Core Services (hot cache)

| Service | Tech | Kafka Topic | Vai trò |
|---------|------|-------------|---------|
| `rest-proxy` | Node.js | `rest-proxy` | API Gateway |
| `aaa` | Node.js | `aaa` | Auth, OTP, Smart OTP |
| `lotte-bridge` | Node.js | `lotte-bridge` | Lệnh thường equity + Derivatives bridge |
| `lotte-ws-bridge` | Node.js | `lotte-ws-bridge` | Notification (margin/VM/position) |
| `market-query-v2` | Node.js | `market-v2` | Market data queries |
| `ws-v2` | Node.js | — | WebSocket server |
| `order-v2` | Java | `order` | Conditional orders (Stop/OCO/Trailing/Bull-Bear) |
| `notification` | Java | `notification` | Push/SMS/email |
| `ekyc-admin` | Java | `ekyc-admin` | eKYC admin |

Canonical đầy đủ: `Knowledge/TradeX/_index.md` · Source: `Knowledge/TradeX-MCP/`
