# AGENTS.md

**TradeX Monitoring Project - AI Agent Instructions**

---

## Project Overview

Đây là dự án TradeX Monitoring - hệ thống giám sát cho TradeX Backend (NHSV Pro App).

**Domain:** Chứng khoán Việt Nam (Vietnamese Securities)
**System:** TradeX - Microservices Backend Platform

## Knowledge Bases

| Knowledge Base | Location | Description |
|----------------|----------|-------------|
| TradeX System | `@TradeX MCP/Knowledge based/` | Source code & documentation |
| BMAD Framework | `@BMAD/_bmad/` | Agile development workflows |
| **TradeX Knowledge** | `@TradeX Knowledge/` | PM knowledge about TradeX mechanisms |

### TradeX Knowledge (for PM)

Folder chứa các tài liệu tổng hợp về cơ chế TradeX, được tổ chức theo 3 categories:

**Structure:**
```
TradeX Knowledge/
├── System/              # ✅ Production - What's actually running
├── API Standards/       # 📘 Conventions - Universal standards
└── Planning/            # 📋 Future - Features in planning
```

| Category | Documents | Purpose |
|----------|-----------|---------|
| **System/** | market-data-channels.md, symbol-info-api.md, init-job.md | Understand live production mechanisms |
| **API Standards/** | api-conventions.md, tradex-api-conventions.md, templates | Follow API standards when creating specs |
| **Planning/** | regular-order-api-mapping.md | General patterns for future features |

> **Note:** Rule `@tradex-knowledge` tự động apply, AI sẽ đọc knowledge trước khi scan codebase.

### FE Repo for Derivatives Issues (Read-Only)

| Item | Value |
|------|--------|
| **Repo** | NHSV Pro React Native (TradeX app) |
| **Path** | `/Users/ducnguyen/Documents/project/nhsv-mts-rn` |
| **Usage** | **Chỉ đọc** – dùng làm cơ sở tạo issue cho FE dev (Derivatives). Không sửa code trong repo này từ workspace tradex-monitoring. |
| **Rule** | `.cursor/rules/fe-repo-derivatives-issues.mdc` |

Khi tạo issue FE cho Derivatives: đọc cấu trúc/screens/components trong `nhsv-mts-rn` để viết issue chính xác; artifact (issue text, AC) lưu trong tradex-monitoring hoặc Jira/Bitbucket.

### gstack (engineering workflow)

| Item | Value |
|------|--------|
| **Checkout** | `gstack/` tại root repo (clone [garrytan/gstack](https://github.com/garrytan/gstack)) |
| **Legacy path** | `.agents/skills/gstack` → symlink tới `gstack/` (preamble Codex / công cụ cũ) |
| **Setup** | `cd gstack && ./setup --host codex` (hoặc `--host auto` nếu dùng nhiều agent) |

Skill paths cho Cursor: `gstack/<skill>/SKILL.md` (ví dụ `gstack/codex/SKILL.md`).

### Second brain & Midu-path

| Item | Value |
|------|--------|
| **Repo** | Midu-path (cá nhân) — learnings + CV + Portfolio |
| **Path** | `/Users/nguyenduc/Personal/Repositories/Midu-path` |
| **Rule** | `.cursor/rules/midu-path-second-brain.mdc` |

Khi user yêu cầu ghi vào Midu-path, cập nhật CV/portfolio, hoặc "second brain": đề xuất nội dung và đường dẫn trong Midu-path — **Career/Profile.md** (SSOT), **Career/Case_Studies/** (case studies), **Knowledge/** (learnings); không commit/push.

## Skill/Rule Ecosystem

> **Orchestrator Rule:** `.cursor/rules/ecosystem-orchestrator.mdc` — Always applied. Routes every task to the correct skill/rule combination.

### Ecosystem Map

```
Task arrives
│
├─ API Spec ──────────→ tradex-api-naming → derivatives-api-spec-format
│   └─ Order API ────→ + tradex-order-api-response-standards
│
├─ Documentation ─────→ derivatives-doc-structure → derivatives-pm-documentation
│
├─ FE Issue ──────────→ fe-repo-derivatives-issues → derivatives-doc-structure
│
├─ System Analysis ───→ tradex-analyst → tradex-knowledge
│
└─ Vague request ─────→ prompt-enhance-workflow → re-route above
```

### Connection Rules (enforced by Orchestrator)

| Rule | What it ensures |
|------|----------------|
| **C1: Naming Consistency** | `tradex-api-naming` checked for ALL API-related output |
| **C2: Response Format** | `tradex-order-api-response-standards` checked for ALL Order API responses |
| **C3: PM-Readability Gate** | `derivatives-pm-documentation` enforced for ALL `Planning/` folder content |
| **C4: Knowledge-First** | `TradeX Knowledge/` checked before scanning codebase |
| **C5: Document Footer** | All Derivatives specs/issues end with status + next steps |

### Available Skills (5)

Dự án này có 5 specialized skills. Cursor tự động phát hiện hoặc mention `@skill-name`:

| Skill | Location | Auto-trigger | Use Case | Connected To |
|-------|----------|--------------|----------|--------------|
| **derivatives-doc-structure** | `.cursor/skills/derivatives-doc-structure/` | ✅ | Create/organize Derivatives docs | derivatives-pm-documentation, derivatives-api-spec-format |
| **derivatives-api-spec-format** | `.cursor/skills/derivatives-api-spec-format/` | ✅ | Standard format for API specs | tradex-api-naming, tradex-order-api-response-standards |
| **tradex-api-naming** | `.cursor/skills/tradex-api-naming/` | ✅ | Enforce API naming conventions | derivatives-api-spec-format |
| **prompt-optimizer** | `.cursor/skills/prompt-optimizer/` | Manual | Optimize individual prompts | prompt-enhance-workflow |
| **prompt-enhance-workflow** | `.cursor/skills/prompt-enhance-workflow/` | ✅ Auto | Auto-enhance + route to correct skill | All skills |

### Prompt Enhancement Workflow

**New conversation auto-enhancement:**

```
You: "Viết docs cho API đặt lệnh"
     ↓
AI detects: Vague request
     ↓
AI presents: Enhanced structured prompt
     ↓
You: "yes"
     ↓
AI executes: With optimized prompt ✅
```

**Features:**
- ✅ Auto-triggers for vague/unclear requests
- ✅ Skips for already-clear prompts
- ✅ Vietnamese & English support
- ✅ PM/BA optimized
- ✅ Quota-friendly (smart detection)

**Quick approval:** Just type `yes`, `ok`, `được`, `tiếp tục`

**Learn more:** See `.cursor/skills/prompt-enhance-workflow/QUICK_GUIDE.md`

## Agent model (orchestrator-first)

Mục tiêu: **một điểm vào** điều phối công việc, các agent còn lại là **chuyên gia** chỉ được gọi khi cần — tránh cảm giác “quá nhiều agent” phải chọn tay.

### Orchestrator (điểm vào mặc định)

| Vai trò | Cách hoạt động trong repo này |
|---------|-------------------------------|
| **Điều phối** | Rule `.cursor/rules/ecosystem-orchestrator.mdc` (**always apply**) đóng vai trò orchestrator: map loại task → skill/rule cần bật. Bạn **không cần** liệt kê hết agent khi giao việc thông thường. |
| **Hành vi mong muốn** | Session (hoặc bạn) mô tả mục tiêu; orchestrator/kỹ thuật routing quyết định có cần `@tradex-analyst`, BMAD, QA, v.v. hay chỉ dùng skill derivatives. |
| **Đề xuất mở rộng** | Persona `@orchestrator` và output `/_orchestrator-output/` trong `.cursor/rules/agent-architecture-proposal.md` — dùng khi cần workflow đa bước rõ ràng. |

### Specialist agents (gọi có chủ đích)

Chỉ `@` khi task **thuộc đúng domain** của agent đó, hoặc khi orchestrator đã chỉ ra cần chuyên sâu.

| Agent | Rule / activation | Khi nào dùng |
|-------|-------------------|---------------|
| **TradeX Analyst** | `@tradex-analyst` | Phân tích API, nghiệp vụ CK, trace hệ thống TradeX |
| **Agile Developer** | `@agile-developer` | Agile, BMAD, story / epic |
| **TradeX QA Postman** | `@tradex-qa-postman` | Test API qua Postman MCP; collection “TradeX QA session”; docs trong `QA sessions/` |
| **Documentation** (đề xuất) | `@documentation-agent` * | Runbook, kỹ thuật doc, index tài liệu |
| **Test** (đề xuất) | `@test-agent` * | Test design, automation, validation ngoài Postman |

\* Chưa có rule riêng trong repo — tham chiếu kiến trúc đầy đủ: `.cursor/rules/agent-architecture-proposal.md`

**Quick reference tương tác:** `AGENTS_QUICK_REFERENCE.md`

### So sánh với Claude Code Agent Teams

Nếu bạn dùng **Claude Code** (CLI) thay vì chỉ Cursor: Anthropic có chế độ **agent teams** — một session **team lead** spawn nhiều teammate, task list chung, teammate nhắn trực tiếp (khác subagent chỉ báo cáo về main). Tính năng **experimental**, cần bật `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1` trong [settings / env](https://code.claude.com/docs/en/agent-teams); yêu cầu Claude Code **v2.1.32+**.

| | Cursor (repo này) | Claude Code agent teams |
|--|-------------------|---------------------------|
| Điều phối | `ecosystem-orchestrator.mdc` + hội thoại một session | Team lead + task list + nhiều session song song |
| Song song | Một context; phân vai bằng `@` / skill | Nhiều instance Claude; token cost cao hơn |
| Tài liệu | [Agent teams](https://code.claude.com/docs/en/agent-teams) | Best practice: tách file/workstream để tránh conflict |

**Gợi ý:** Với TradeX Monitoring, giữ **orchestrator (rule) làm mặc định** trong Cursor; cân nhắc agent teams trên Claude Code khi task **nghiên cứu / review song song** (ví dụ nhiều góc PR) — đúng tinh thần “parallel exploration” trong tài liệu Anthropic.

## Quick Reference

### Postman collections (QA agent)

| Collection | UID | Vai trò |
|------------|-----|--------|
| **TradeX API v2** (main) | `34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d` | API chính – tham chiếu; không tạo request test. |
| **TradeX QA session** | `34274942-8fe5bddd-fce2-4f76-bb6f-fb3f2760d40a` | Request test – folder theo issue (ví dụ NHMTS-626). Index: `QA sessions/Postman_Index.json`. |

### Servers

| Environment | URL |
|-------------|-----|
| Production | https://nhsvpro.nhsv.vn |
| UAT | https://tnhsvpro.nhsv.vn |

### Core Services

| Service | Description |
|---------|-------------|
| `rest-proxy` | API Gateway |
| `aaa` | Authentication |
| `lotte-bridge` | Lotte Securities integration |
| `market-query-v2` | Market data queries |
| `ws-v2` | WebSocket server |

### Output Directories

```
/_bmad-output/
├── planning-artifacts/       # PRD, Product Brief
├── implementation-artifacts/ # Architecture, Stories
├── test-artifacts/           # Test design, reports
└── ba-artifacts/             # API analysis, BRD, PM Knowledge
    └── tradex-*-knowledge.md # TradeX mechanism knowledge docs

/QA sessions/                 # QA & test docs – sync với Postman "TradeX QA session"
├── README.md                 # Quy ước sync repo ↔ Postman
├── Session_Notes/            # (tùy chọn) Ghi chú phiên test
├── Test_Plans/               # (tùy chọn) Test plan theo feature
└── Runbooks/                 # (tùy chọn) Hướng dẫn chạy test API

/Derivatives/Planning documentation/
├── Market data/              # Market data integration docs
│   ├── README.md            # Single entry point
│   ├── Planning/            # Planning & requirements docs
│   ├── Specifications/      # Technical specs (< 500 lines each)
│   ├── Issues/              # Active implementation issues
│   └── Archive/             # Completed/historical docs
├── Orders/                   # Order-related analysis & planning
│   ├── _index.md            # Orders documentation index
│   └── [NN]_*.md            # Numbered order documents
└── [Other categories]/       # Future categories (Account, Asset, Portfolio, etc.)
    └── [Follow Market data structure]
```

**Important Rules for Derivatives Documentation:**

1. **Use Standard Structure** (see `.cursor/skills/derivatives-doc-structure/SKILL.md`):
   ```
   {Category}/
   ├── README.md              ← Single entry point (mandatory)
   ├── Planning/              ← 01_Integration_Plan.md, 02_Business_Requirements.md, etc.
   ├── Specifications/        ← {Feature}_API.md, {Feature}_Spec.md
   ├── Issues/                ← {Feature}_Implementation.md
   └── Archive/               ← Completed docs
   ```

2. **Naming Conventions:**
   - ✅ PascalCase with underscores: `Chart_API_Spec.md`, `Order_API_Implementation.md`
   - ✅ README.md at category root (not `_index.md` or `00_EXECUTIVE_SUMMARY.md`)
   - ❌ NO brackets: `[ISSUE]`, `[BRIEF]`, `[COMPLETION]`
   - ❌ NO special prefixes: `_index.md`, `00_`

3. **File Organization:**
   - Planning docs: Numbered 01-04 in `Planning/` folder
   - Specifications: In `Specifications/` folder, max ~500 lines each
   - Active issues: In `Issues/` folder, one per feature
   - Completed: Move to `Archive/` folder

4. **Key Principles:**
   - Single entry point per category (README.md)
   - Clear separation: Planning vs Specs vs Issues vs Archive
   - Scalable structure for adding new features
   - Consistent across all Derivatives categories

5. **Before Creating New Category:**
   - Read skill: `.cursor/skills/derivatives-doc-structure/SKILL.md`
   - Copy `Market data/` structure as template
   - Follow checklist in skill document

## Documentation Standards

- Use CommonMark strict compliance
- ATX-style headers only (`#`)
- Fenced code blocks with language identifiers
- Reference files with `@filename` instead of copying content
