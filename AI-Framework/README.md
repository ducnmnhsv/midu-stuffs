# NHSV Multi Agents — Project Overview

> **Tên dự án:** NHSV Agents
> **Mục tiêu:** Xây dựng hệ thống AI Agent nội bộ, phục vụ Developer, Tech Lead và Ban quản lý của NHSV Pro
> **Môi trường:** Air-gapped (không có internet), ngoại trừ Microsoft Teams và Atlassian Cloud
> **Team:** 2 kỹ sư (BE Dev + DevOps) + chi phí hạ tầng nội bộ

---

## Mục lục

1. [Bối cảnh & Vấn đề](#1-bối-cảnh--vấn-đề)
2. [Kiến trúc tổng thể](#2-kiến-trúc-tổng-thể)
3. [Lộ trình triển khai](#3-lộ-trình-triển-khai)
4. [Chi tiết từng Phase](#4-chi-tiết-từng-phase)
5. [Tiêu chí thành công](#5-tiêu-chí-thành-công)
6. [Tài liệu tham chiếu](#6-tài-liệu-tham-chiếu)

---

## 1. Bối cảnh & Vấn đề

### Thực trạng

Dev team đang thực hiện thủ công:
- SSH vào server đọc log, tìm lỗi
- Kiểm tra trạng thái service, job định kỳ
- Tra cứu thông tin từ Jira/Confluence
- Tổng hợp báo cáo hệ thống hàng ngày
- Onboarding nhân viên mới

### Ràng buộc kỹ thuật

> ⚠️ Toàn bộ server trong **môi trường air-gapped** — không có kết nối internet trực tiếp.

| Ngoại lệ | Ghi chú |
|----------|---------|
| Microsoft Teams | Cho phép internet — kênh giao tiếp chính |
| Atlassian Cloud (Jira/Confluence/Bitbucket) | Truy cập qua Bridge Server có kiểm soát |

### Stack công cụ hiện có

| Tool | Trạng thái | Vai trò |
|------|-----------|---------|
| Jira Cloud | ✅ | Quản lý dự án, issue tracking |
| Confluence Cloud | ✅ | Tài liệu kỹ thuật, wiki |
| Bitbucket Cloud | ✅ | Source code, PR |
| Kibana/Elasticsearch | ✅ | Log lưu trữ |
| Grafana/Prometheus/Loki | 🚧 Đang xây dựng (NHMTS-642) | Monitoring & observability |
| Jenkins | ✅ | CI/CD |
| Microsoft Teams | ✅ | Giao tiếp nội bộ |

---

## 2. Kiến trúc tổng thể

### Hai tracks song song

```
┌─────────────────────────────────────────────────────────────────────┐
│  Track A: NHMTS-642 — Observability Layer                           │
│  (DevOps)                                                           │
│                                                                     │
│  Prometheus ──→ Grafana ──→ AlertManager ──→ webhook ─────┐        │
│  Loki (log aggregation) ──────────────────────────────────┤        │
│  Node/Kafka/Redis/DB Exporters ───────────────────────────┤        │
└──────────────────────────────────────────────────────────┬┘        │
                                                           │         │
                              ┌────────────────────────────▼─────────┐
                              │  Track B: NHSV Agent — Intelligence  │
                              │  (BE Dev)                            │
                              │                                      │
                              │  Alert Receiver ──→ LLM Enrichment  │
                              │  prometheus_query skill              │
                              │  loki_query skill                    │
                              │  Jira/Confluence skills              │
                              │  Memory Keeper                       │
                              │  Code Explorer                       │
                              └───────────────┬──────────────────────┘
                                              │
                              ┌───────────────▼──────────────────────┐
                              │  Microsoft Teams                     │
                              │  (Enriched alerts + chat interface)  │
                              └──────────────────────────────────────┘
```

### Sơ đồ hạ tầng chi tiết

```
┌─────────── INTERNET ──────────────┐
│  Microsoft Teams                  │
│  Atlassian Cloud (Jira/Confluence)│
└───────────────┬───────────────────┘
                │
┌─────────── DMZ (Bastion Zone) ────┐
│  Bridge Server                    │
│  ├── Teams Bot endpoint (public)  │
│  ├── MCP Atlassian container      │
│  └── Outbound proxy (allowlist)   │
└───────────────┬───────────────────┘
                │
┌─────────── INTERNAL NETWORK (Air-gapped) ──────────────────────────┐
│                                                                     │
│  ┌─────────────────────────┐    ┌───────────────────────────────┐  │
│  │  Observability Stack    │    │  Agent Stack                  │  │
│  │  (NHMTS-642)            │    │  (NHSV Agent)                 │  │
│  │                         │    │                               │  │
│  │  Prometheus :9090       │───→│  prometheus_query skill       │  │
│  │  Loki :3100             │───→│  loki_query skill             │  │
│  │  AlertManager :9093 ────│───→│  alert_receiver endpoint      │  │
│  │  Grafana :3000          │    │  OpenFang Agent               │  │
│  │                         │    │  Local LLM (Qwen3.5-35B-A3B) │  │
│  └─────────────────────────┘    │  SQLite (Memory + Cache)      │  │
│                                 └───────────────────────────────┘  │
│                                                                     │
│  TradeX Services (Kafka, Redis, DB, App servers, Init Jobs)        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Lộ trình triển khai

### Timeline tổng hợp (16 tuần)

```
Tuần │ Track A: NHMTS-642 (Infra)          │ Track B: NHSV Agent              │ Milestone
─────┼─────────────────────────────────────┼──────────────────────────────────┼──────────────
 1   │ Prometheus + Grafana setup          │ Server + Teams Bot + Bridge      │
 2   │ Node/Kafka/Redis/DB exporters       │ Jira + Confluence skills         │
 3   │ ✅ Phase 1 done: Infra dashboards   │ Prometheus query accessible      │ 🔵 Health query live
─────┼─────────────────────────────────────┼──────────────────────────────────┼
 4   │ Job Metrics Exporter                │ LLM setup + alert_receiver       │
 5   │ Loki + Job logs + Job alert rules   │ loki_query + alert_enricher      │
 6   │ ✅ Phase 2 done: Job monitoring     │ Memory + Code Explorer + test    │ 🟡 Crisis alert live
                                                                               │ 🟢 MVP go-live
─────┼─────────────────────────────────────┼──────────────────────────────────┼
 7   │ API Metrics instrumentation         │ NemoClaw setup + guardrails      │
 8   │ API dashboards + historical         │ Skills migrate → OpenShell       │
 9   │ ✅ Phase 3 done: API monitoring     │ Audit DB + alert channel         │ 🔵 Secure MVP live
─────┼─────────────────────────────────────┼──────────────────────────────────┼
10   │ Redis auto-remediation webhook      │ Knowledge Graph + MetaAgent      │
11   │ Remediation audit trail             │ Root Cause Analyzer              │
12   │ ✅ Phase 4 done: Automation         │ Proactive Detector               │
13   │                                     │ Trend Analyzer                   │
14   │                                     │ Auto-Fix Orchestrator            │
15   │                                     │ Weekly Intelligence Report       │
16   │                                     │ E2E testing + handover           │ 🚀 Smart Agent live
```

### Milestones chính

| Mốc | Tuần | Mô tả |
|-----|------|-------|
| 🔵 Health query live | W3 | Agent query Prometheus được, health check qua chat |
| 🟡 Crisis alert live | W6 | Job failure/system crash → enriched Teams alert < 2 phút |
| 🟢 MVP go-live | W6 | Jira/Confluence/Alert/Memory/Code Explorer đầy đủ |
| 🔵 Secure MVP | W9 | NemoClaw sandbox + audit trail |
| 🚀 Smart Agent | W16 | Proactive detection, RCA, auto-fix |

---

## 4. Chi tiết từng Phase

### Phase 0: NHMTS-642 — Observability Infrastructure

> **Thực chất:** Track A chạy song song suốt dự án. Được tách thành Phase 0 để nhấn mạnh đây là **prerequisite** cho Agent.

**Owner:** DevOps
**Timeline:** Tuần 1–12 (song song với Agent development)

| Sub-phase | Tuần | Deliverable | Agent unlock |
|-----------|------|-------------|--------------|
| Infra | W1–3 | Prometheus, Grafana, Node/Kafka/Redis exporters | `prometheus_query`, `health_checker` |
| Jobs | W4–6 | Loki, Job exporter, AlertManager webhook | `loki_query`, `alert_receiver` (jobs) |
| APIs | W7–9 | API metrics, API alerting | `alert_receiver` (API errors) |
| Automation | W10–12 | Remediation webhooks | Auto-Fix integration |

**Stories bổ sung cần thêm vào NHMTS-642:**
- **Story A1:** AlertManager webhook output → Agent endpoint
- **Story A2:** Loki API accessible từ Agent server
- **Story A3:** Prometheus API accessible từ Agent server

→ Chi tiết: [NHMTS-642-Integration.md](./NHMTS-642-Integration.md)

---

### Phase 1: MVP — Core Agent + Skills

**Owner:** BE Dev (+ DevOps parallel)
**Timeline:** Tuần 1–6 (4 tuần dev agent, overlap với NHMTS-642)
**Effort:** ~2 devs × 6 tuần

#### Tuần 1–2: Nền tảng

| Task | Track | Ghi chú |
|------|-------|---------|
| Setup Ops Server (Ubuntu, Docker) | DevOps | Cùng server với NHMTS-642 nếu đủ tài nguyên |
| Cài OpenFang, test cơ bản | BE Dev | |
| Tạo Teams Bot, cấu hình webhook | DevOps | Cần Teams tenant admin approval |
| Setup Bridge Server (proxy, allowlist) | DevOps | Allowlist: `*.atlassian.net`, `auth.atlassian.com` |
| Deploy MCP Atlassian container | DevOps | `sooperset/mcp-atlassian` |
| Skeleton skills + mock test | BE Dev | Dev local với mock data trước khi infra ready |

#### Tuần 3–4: Skills

| Skill | Phụ thuộc | Mô tả |
|-------|-----------|-------|
| `jira_query` | Bridge Server | Tra cứu issue, sprint, project |
| `confluence_search` | Bridge Server | Tìm tài liệu theo keyword |
| `prometheus_query` | NHMTS-642 W3 | Query health metrics |
| `loki_query` | NHMTS-642 W6 | Query logs quanh thời điểm lỗi |
| `alert_receiver` | NHMTS-642 W6 | Nhận webhook từ AlertManager |
| `alert_enricher` | LLM + loki_query + prometheus_query | Enriched Teams alert |

#### Tuần 5–6: Hoàn thiện

| Task | Ghi chú |
|------|---------|
| `memory_keeper` (Nemori) | Session-based memory |
| `code_explorer` (Code Context MCP) | Index các repositories |
| Intent parser kết nối skills | Routing user query → skill |
| E2E testing với 5–10 users | |
| User guide | |

**Skills bị cắt khỏi MVP:** Onboarding Guideline (UC-06) → chuyển sang Phase 3

#### Kiến trúc skills MVP

```
skills/
├── atlassian/
│   ├── jira_query.py
│   └── confluence_search.py
├── monitoring/            ← Mới (thay thế UC-03 gốc)
│   ├── alert_receiver.py
│   ├── alert_enricher.py
│   ├── prometheus_query.py
│   └── loki_query.py
├── code/
│   ├── search.py
│   └── explain.py
└── memory/
    └── nemori_bridge.py
```

---

### Phase 2: Security — NemoClaw Sandbox & Audit

**Owner:** BE Dev
**Timeline:** Tuần 7–9 (3 tuần, overlap với NHMTS-642 API phase)
**Effort:** ~1 dev × 3 tuần

#### Tuần 7: NemoClaw Integration

- Cài NemoClaw trên Ops Server
- Viết guardrails cơ bản:
  - ✅ Được phép: đọc `/var/log/myapp/`, query Loki/Prometheus API, gọi Teams API
  - ❌ Không được phép: `rm`, `sudo`, `chmod`, đọc `/etc/shadow`, `/root/`
- Migrate tất cả skills → OpenShell (thay vì gọi trực tiếp)
- Test với skills quan trọng (alert_receiver, loki_query)

#### Tuần 8: Audit Trail

- Setup PostgreSQL (hoặc nâng cấp SQLite) cho audit logs
- Cấu hình NemoClaw ghi audit vào database
- Alert channel riêng cho guardrail violations (Teams #admin-alerts)
- API tra cứu audit cho admin

#### Tuần 9: Kiểm thử & Chuyển giao

- Performance test (đo độ trễ tăng do NemoClaw layer)
- Cập nhật user guide + admin runbook
- Tài liệu vận hành cho admin

#### Luồng xử lý sau Phase 2

```
User/AlertManager
      ↓
  OpenFang
      ↓
  Skill request (đọc log, query metrics)
      ↓
  NemoClaw OpenShell ──→ Guardrail check
      │                       │
  ✅ Allowed              ❌ Denied
      │                       │
  Execute                 Audit + Alert
      │                   admin channel
  Return result
      ↓
  LLM enrichment
      ↓
  Teams response
```

---

### Phase 3: Intelligence — Proactive & Learning

**Owner:** BE Dev
**Timeline:** Tuần 10–16 (7 tuần)
**Effort:** ~2 devs × 7 tuần

#### Sprint 1 — Tuần 10–11: Nền tảng học tập

- Knowledge Graph Store (PostgreSQL + pgvector)
  - Node: Incident, Solution, Service, Jira Issue, PR
  - Edge: `caused_by`, `fixes`, `deployed_in`, `related_to`
- MetaAgent Learning Engine:
  - Ghi nhận: lỗi → nguyên nhân → giải pháp → người phê duyệt
  - Vector embeddings cho pattern lỗi (Ollama + nomic-embed-text)
- Cập nhật audit trail lưu thêm learning data

#### Sprint 2 — Tuần 12–13: Phân tích & Phát hiện

- **Root Cause Analyzer:**
  - Input: log lines + deployment history + code changes gần nhất
  - LLM phân tích → tóm tắt nguyên nhân + gợi ý khắc phục
- **Proactive Detector:**
  - Chạy mỗi 10 phút, so sánh pattern với Knowledge Graph
  - Nếu match ≥ 80% → gửi đề xuất khắc phục vào Teams (tag người liên quan)
- Tích hợp Trend Analyzer với Kibana/Prometheus API

#### Sprint 3 — Tuần 14–15: Tự động hóa

- **Auto-Fix Orchestrator:**
  - Teams approval flow: [Apply] / [Ignore] / [View Details]
  - Chạy qua NemoClaw sandbox (guardrails đã được mở rộng cho auto-fix)
  - Audit toàn bộ vào Knowledge Graph
- **Weekly Intelligence Report:**
  - Gửi vào `#weekly-report` mỗi sáng thứ Hai
  - Nội dung: top incidents, MTTR, trend predictions, khuyến nghị
- **Onboarding Guideline** (UC-06 dời từ MVP):
  - Tổng hợp từ Confluence + Jira + codebase → Markdown guide

#### Sprint 4 — Tuần 16: Tối ưu & Chuyển giao

- Đánh giá độ chính xác RCA (≥70% mục tiêu)
- Tối ưu thời gian phát hiện và xử lý
- Training team sử dụng proactive agent
- Cập nhật tài liệu vận hành đầy đủ

---

## 5. Tiêu chí thành công

### MVP (Tuần 6)

| KPI | Mục tiêu |
|-----|---------|
| Crisis alert time | < 2 phút từ sự cố đến Teams notification |
| Jira/Confluence lookup | < 10 giây |
| Code search | Tìm class/function trong < 5 giây |
| Alert content | Có log context + health snapshot + possible cause |
| Agent uptime | 24/7, không crash |
| User satisfaction | ≥ 80% thấy hữu ích |

### Secure MVP (Tuần 9)

| KPI | Mục tiêu |
|-----|---------|
| Guardrail enforcement | 100% skill requests đi qua NemoClaw |
| Audit coverage | 100% actions được ghi log |
| Latency overhead | < 200ms thêm do NemoClaw layer |

### Smart Agent (Tuần 16)

| KPI | Mục tiêu |
|-----|---------|
| Proactive detection rate | ≥ 80% lỗi tái diễn được phát hiện và đề xuất |
| RCA accuracy | ≥ 70% kết luận được đánh giá chính xác/hữu ích |
| Auto-fix adoption | ≥ 50% đề xuất được approve và thực thi |
| MTTR reduction | Giảm ≥ 30% so với Phase 2 |
| Manual ops reduction | Giảm ≥ 70% thời gian log checking thủ công |

---

## 6. Tài liệu tham chiếu

| Tài liệu | Đường dẫn | Mô tả |
|----------|-----------|-------|
| Phase 1: MVP | [Phase 1.md](./Phase%201.md) | Chi tiết MVP — Use cases, kiến trúc, kế hoạch gốc |
| Phase 2: Security | [Phase 2.md](./Phase%202.md) | NemoClaw, audit trail |
| Phase 3: Intelligence | [Phase 3.md](./Phase%203.md) | Proactive, RCA, auto-fix, knowledge graph |
| NHMTS-642 Integration | [NHMTS-642-Integration.md](./NHMTS-642-Integration.md) | Cách NHMTS-642 tích hợp vào NHSV Agent |
| NHMTS-642 Plan | [../NHMTS-642: TradeX stability monitor/implementation_plan.md](../NHMTS-642:%20TradeX%20stability%20monitor/implementation_plan.md) | Implementation plan gốc của NHMTS-642 |

---

## Appendix: Công nghệ chính

| Component | Technology | Lý do chọn |
|-----------|-----------|------------|
| Agent Framework | OpenFang | Open-source, Rust, Skill-based, air-gapped friendly |
| Local LLM | Qwen3.5-35B-A3B (GGUF via llama.cpp) | 3B active params (~24GB RAM), 262K context, tool calling, Apache 2.0 |
| Teams Integration | Microsoft Bot Framework | Đã có Teams, endpoint qua Bridge Server |
| Atlassian | sooperset/mcp-atlassian | MCP container cho Jira/Confluence |
| Session Memory | Nemori | Python, lưu per-user per-session |
| Audit DB | PostgreSQL | Phase 2+ |
| Vector Store | pgvector (PostgreSQL extension) | Tránh thêm database mới, đủ cho Phase 3 |
| Security Layer | NemoClaw | Sandbox, guardrails, OpenShell |
| Observability | Prometheus + Loki + Grafana + AlertManager | NHMTS-642 cung cấp |
