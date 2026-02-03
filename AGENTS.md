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

Folder chứa các tài liệu tổng hợp về cơ chế TradeX, giúp PM hiểu hệ thống mà không cần scan codebase:

| Document | Content |
|----------|---------|
| `_index.md` | Overview, navigation, system architecture |
| `market-data-channels.md` | WebSocket channels (quote, bidoffer), field mappings |

> **Note:** Rule `@tradex-knowledge` tự động apply, AI sẽ đọc knowledge trước khi scan codebase.

## Available Agents

Dự án này có 2 specialized agents. Sử dụng `@` mention để activate:

| Agent | Rule File | Use Case |
|-------|-----------|----------|
| TradeX Analyst | `@tradex-analyst` | Phân tích API, nghiệp vụ chứng khoán, trace system |
| Agile Developer | `@agile-developer` | Quy trình Agile, BMAD workflows, story development |

## Quick Reference

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
