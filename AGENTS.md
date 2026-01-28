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
├── planning-artifacts/    # PRD, Product Brief
├── implementation-artifacts/  # Architecture, Stories
├── test-artifacts/        # Test design, reports
└── ba-artifacts/          # API analysis, BRD
```

## Documentation Standards

- Use CommonMark strict compliance
- ATX-style headers only (`#`)
- Fenced code blocks with language identifiers
- Reference files with `@filename` instead of copying content
