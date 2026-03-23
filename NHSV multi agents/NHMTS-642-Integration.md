# NHMTS-642: TradeX Stability Monitor — Tích hợp vào NHSV Agent

## Tổng quan

NHMTS-642 **không** phải một dự án độc lập nữa. Nó là **Infrastructure Track** của NHSV Agent — xây dựng lớp quan sát (observability layer) mà Agent sẽ sử dụng làm nguồn dữ liệu và trigger cho mọi tính năng liên quan đến monitoring.

### Quan hệ giữa hai dự án

```
NHMTS-642 (Infrastructure Track)        NHSV Agent (Intelligence Track)
────────────────────────────────         ────────────────────────────────
Prometheus ─────────────────────────────→ prometheus_query skill
Loki ────────────────────────────────────→ loki_query skill
Grafana AlertManager ────── webhook ─────→ alert_receiver + alert_enricher
Grafana Dashboards ──────────────────────→ Dữ liệu nền cho RCA (Phase 3)
```

> **Nguyên tắc:** NHMTS-642 cung cấp *dữ liệu*. NHSV Agent cung cấp *trí tuệ* — enrichment, ngữ cảnh, và thông báo thông minh qua Teams.

---

## Tại Sao Tích Hợp?

| Nếu tách biệt | Nếu tích hợp |
|---------------|--------------|
| UC-03 của Agent phải tự scan log qua SSH (cron 5 phút) | Agent nhận webhook từ AlertManager (< 1 phút) |
| Agent phải tự build Health Checker | Agent query Prometheus API trực tiếp |
| Duplicate monitoring logic ở 2 nơi | Single source of truth: NHMTS-642 |
| Raw log dump vào Teams | LLM-enriched alert với context + gợi ý |
| Log access cần SSH vào server | Loki REST API, không cần SSH |

---

## Dependency Map: NHMTS-642 → NHSV Agent Skills

Bảng sau xác định story nào của NHMTS-642 phải hoàn thành trước khi skill nào của Agent có thể hoạt động:

| Agent Skill | Phụ thuộc NHMTS-642 | Epic/Story | Timeline NHMTS-642 |
|-------------|---------------------|------------|-------------------|
| `prometheus_query` | Prometheus + Grafana setup | Story 1.1 | Tuần 1–3 |
| `health_checker` (server) | Node Exporter deployed | Story 1.5 | Tuần 1–3 |
| `health_checker` (kafka/redis) | Kafka + Redis exporters | Story 1.2, 1.3 | Tuần 1–3 |
| `loki_query` | Loki deployed + job logs | Story 2.5 | Tuần 5–6 |
| `alert_receiver` (job failure) | Job alerting rules + AlertManager webhook | Story 2.3 | Tuần 5–6 |
| `alert_receiver` (API errors) | API alerting rules | Story 3.5 | Tuần 8–9 |
| `alert_enricher` (trend context) | API historical dashboard | Story 3.3 | Tuần 8–9 |
| Auto-Fix Orchestrator (Phase 3) | Remediation webhooks | Epic 4 | Tuần 10–12 |

---

## Stories NHMTS-642 Cần Bổ Sung Để Hỗ Trợ Agent

Các story sau chưa có trong `implementation_plan.md` của NHMTS-642 nhưng cần thêm vào để integration hoạt động:

### Story A1: AlertManager Webhook Output (thêm vào Story 2.3)

**Mô tả:** Cấu hình AlertManager gửi webhook đến NHSV Agent API endpoint (bên cạnh email/Slack hiện có).

**Acceptance Criteria:**
- [ ] AlertManager có receiver `nhsv-agent-webhook`
- [ ] Webhook URL: `http://[agent-server]/api/alerts/receive`
- [ ] Payload chuẩn: AlertManager JSON format
- [ ] Retry policy: 3 lần, exponential backoff
- [ ] Alert routing: `severity=critical` → gửi webhook ngay lập tức

**Effort:** 1 ngày

---

### Story A2: Loki Internal API Access (thêm vào Story 2.5)

**Mô tả:** Đảm bảo Loki HTTP API accessible từ Agent server (internal network).

**Acceptance Criteria:**
- [ ] Loki API endpoint `http://[loki-server]:3100` accessible từ Agent server
- [ ] Agent server được thêm vào allowlist nếu có firewall rule
- [ ] API key hoặc basic auth cho Loki (nếu cần)
- [ ] Test query: `{job="init-job"}` trả về kết quả đúng

**Effort:** 0.5 ngày

---

### Story A3: Prometheus API Access (thêm vào Story 1.1)

**Mô tả:** Đảm bảo Prometheus HTTP API accessible từ Agent server.

**Acceptance Criteria:**
- [ ] Prometheus API `http://[prometheus-server]:9090/api/v1/query` accessible từ Agent
- [ ] Test query: `up{job="node-exporter"}` trả về kết quả đúng

**Effort:** 0.5 ngày

---

## Skills Mới Của Agent (thay thế UC-03 gốc)

### `alert_receiver` — Webhook endpoint

```
POST /api/alerts/receive
Body: AlertManager JSON payload

→ Trích xuất: alertname, service, severity, startsAt
→ Gọi loki_query để lấy log context
→ Gọi prometheus_query để lấy health snapshot
→ Chuyển sang alert_enricher
```

**Thay thế:** Cron job scan file `error.log`
**Effort:** 1 ngày

---

### `loki_query` — Query log từ Loki

```python
def query_loki(service: str, time_from: datetime, lines: int = 30) -> list[str]:
    # GET http://loki:3100/loki/api/v1/query_range
    # params: query='{service="<service>"}', start=..., limit=30
    # return: list of log lines
```

**Thay thế:** SSH + tail log file
**Effort:** 1 ngày

---

### `prometheus_query` — Query metrics từ Prometheus

```python
def query_prometheus(promql: str) -> dict:
    # GET http://prometheus:9090/api/v1/query
    # params: query=<promql>
    # return: metric value + labels

# Ví dụ sử dụng:
query_prometheus('up{job="payment-service"}')           # Service alive?
query_prometheus('kafka_consumer_lag_sum')              # Kafka lag
query_prometheus('redis_memory_used_bytes')             # Redis memory
query_prometheus('job_status{name="market-data-init"}') # Job status
```

**Thay thế:** Custom health check scripts
**Effort:** 1 ngày

---

### `alert_enricher` — LLM enrichment cho alert

```
Input:
  - Alert payload từ AlertManager
  - Log lines từ loki_query (30 dòng)
  - Health snapshot từ prometheus_query

LLM Prompt:
  "Analyze this alert and logs, identify likely cause and suggest action"

Output (Teams message):
  🚨 [SEVERITY] — [Alert Name]
  Service: [service]
  Time: [timestamp]

  📋 Recent logs:
  [top 5 relevant log lines]

  📊 System health snapshot:
  [key metrics]

  💡 Possible cause: [LLM analysis]

  [View Grafana] [View Jira] [Retry Job?]
```

**Effort:** 2–3 ngày

---

## Scenarios Khủng Hoảng Được Cover

Sau khi tích hợp, các tình huống sau sẽ tự động trigger alert đến Teams < 2 phút:

| Scenario | Trigger | NHMTS-642 Story | Agent Response |
|----------|---------|-----------------|----------------|
| Init job failed | `job_status == FAIL` | Story 2.3 | Alert + Loki logs + retry option |
| Init job không chạy đúng giờ | `time() - last_start > interval` | Story 2.3 | Alert + timeline context |
| API error rate > 5% | Grafana rule | Story 3.5 | Alert + affected endpoint + error samples |
| Redis memory > 90% | Grafana rule | Story 1.3 | Alert + current usage + auto-fix option |
| Kafka consumer lag tăng đột biến | Grafana rule | Story 1.2 | Alert + lag metrics |
| Service down | `up == 0` | Story 1.5 | Alert + last known status |
| Post-deploy error spike | Rate comparison | Story 3.5 | Alert + deployment context (Phase 3) |

---

## Phân Công Track

| Track | Ai làm | Deliverable |
|-------|--------|-------------|
| **NHMTS-642 (Infra)** | DevOps | Prometheus, Grafana, Loki, AlertManager, exporters |
| **NHSV Agent (App)** | BE Dev | Teams Bot, skills, LLM, alert enrichment |

Hai track **chạy song song**. Agent có thể bắt đầu development với mock data trước khi NHMTS-642 sẵn sàng.

---

## Integration Milestone

| Mốc | Điều kiện | Thời điểm (ước tính) |
|-----|-----------|----------------------|
| 🔵 First health query | NHMTS-642 Story 1.1 + Story A3 | Tuần 3 |
| 🟡 Crisis alert go-live | Story 2.3 + Story A1 + A2 + Agent Week 3 | Tuần 5–6 |
| 🟢 Full alert coverage | Story 3.5 hoàn thành | Tuần 9 |
| 🚀 Auto-fix integrated | Epic 4 + Phase 3 Agent | Tuần 12–16 |
