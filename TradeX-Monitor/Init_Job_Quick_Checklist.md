# TradeX Init Job - Quick Checklist (Manual Monitor)

> **Epic:** [NHMTS-642](https://nhsv-vn.atlassian.net/browse/NHMTS-642) - TradeX Stability Monitor  
> **Story:** [NHMTS-657](https://nhsv-vn.atlassian.net/browse/NHMTS-657) - Init job monitoring & management  
> **Mục đích:** Checklist check thủ công trước/sau Init Job (~8:30 AM), dùng cho đến khi Grafana/Loki sẵn sàng.

---

## Liên kết với Subtasks NHMTS-657

| Subtask | Nội dung | Checklist tương ứng |
|---------|----------|---------------------|
| [NHMTS-658](https://nhsv-vn.atlassian.net/browse/NHMTS-658) | Job metrics collection | Mục 1, 2 – check status, duration |
| [NHMTS-659](https://nhsv-vn.atlassian.net/browse/NHMTS-659) | Job monitoring dashboard | Mục 3 – status OK/FAILED/RUNNING |
| [NHMTS-660](https://nhsv-vn.atlassian.net/browse/NHMTS-660) | Job alerting rules | Mục 4 – các tình huống cần cảnh báo |
| [NHMTS-661](https://nhsv-vn.atlassian.net/browse/NHMTS-661) | Job retry mechanism | Mục 5 – manual retry |
| [NHMTS-662](https://nhsv-vn.atlassian.net/browse/NHMTS-662) | Job execution logs | Mục 6 – xem log ở đâu |

---

## 1. Thời điểm check

| Thời điểm | Nội dung |
|-----------|----------|
| **Trước 8:30** | Services chạy, socket sẵn sàng |
| **8:30–9:00** | Init Job chạy – theo dõi log |
| **Sau 9:00** | Xác nhận symbol_static.json, market data stream |

---

## 2. Init Job – Checklist chính

*Jobs cần monitor: Market data (Lotte), Vietstock rights, Historical job.*

| # | Check | Cách kiểm tra | Status |
|---|-------|---------------|--------|
| 1 | **market-collector-lotte** | Process/container running | ☐ OK ☐ FAILED |
| 2 | **Init Job chạy đúng giờ** (~8:30) | Log có entry thời điểm 8:30 | ☐ OK ☐ FAILED |
| 3 | **Init Job hoàn thành** | Log không có exception/error | ☐ OK ☐ FAILED |
| 4 | **symbol_static.json** | Download được, có ~2000 symbols | ☐ OK ☐ FAILED |
| 5 | **Redis / MongoDB** | Có data symbol (nếu có quyền check) | ☐ OK ☐ N/A |

**symbol_static.json:**  
Prod: `https://file.nhsvpro.nhsv.vn/market/symbol_static.json` | UAT: `https://file.tnhsvpro.nhsv.vn/market/symbol_static.json`

---

## 3. Status tổng hợp (Dashboard)

*Tương ứng NHMTS-659: Status indicators OK / FAILED / RUNNING / WAITING*

| Hạng mục | Kết quả |
|----------|---------|
| Init Job (Lotte market data) | ☐ OK ☐ FAILED ☐ RUNNING ☐ WAITING |
| Socket/Market data feed | ☐ OK ☐ Có lỗi |
| symbol_static.json | ☐ OK ☐ Lỗi |

---

## 4. Các tình huống cần cảnh báo (Alerting)

*Tương ứng NHMTS-660: Job failed, duration > 2x baseline, job không chạy đúng giờ*

| Tình huống | Hành động |
|------------|-----------|
| Init Job FAILED | Xem log → Retry (mục 5) hoặc check Lotte API |
| Init Job chạy quá lâu (>2x bình thường) | Check Kafka lag, realtime-v2, Lotte |
| Init Job không chạy đúng giờ | Check cron, holiday config, coordinator lock |
| symbol_static.json không update | Check realtime-v2, MinIO/S3 |

---

## 5. Manual Retry

*Tương ứng NHMTS-661: Retry từ UI. Tạm thời dùng Kafka trigger.*

**Kafka trigger:** Topic `market-collector-lotte`, Message `{"uri": "downloadSymbol"}`

---

## 6. Xem Log (Job execution logs)

*Tương ứng NHMTS-662: Logs aggregated in Loki, filter by job name, time, level*

| Nguồn | Đường dẫn / Cách |
|-------|------------------|
| Log init job | `/var/log/tradex/jobs/*.log` hoặc log `market-collector-lotte` |
| Grafana + Loki | *(khi NHMTS-662 hoàn thành)* Filter: job name, time range, log level |

---

## 7. Quick Verification (Client)

| Check | Cách |
|-------|------|
| API SymbolInfo | `GET /api/v2/market/symbol/latest` với `{"symbolList": ["VCB", "VNM"]}` |
| symbol_static.json | `curl -s https://file.nhsvpro.nhsv.vn/market/symbol_static.json | jq length` → ~2000 |

---

## Template ghi chú phiên check

```markdown
**Ngày:** ____/____/____  **Người check:** _______________

| Hạng mục | Kết quả |
|----------|---------|
| Init Job | ☐ OK ☐ FAILED |
| symbol_static.json | ☐ OK ☐ Lỗi |
| Market data feed | ☐ OK ☐ Lỗi |

**Vấn đề:** _______________________________
```

---

*Tạo: 2025-02-24 | NHMTS-657 | Tham chiếu: [init-job.md](../Knowledge/TradeX/System/init-job.md)*

---

Document Status: 📋 | For: PM/Dev | Next Steps: Review nội dung, cập nhật status trên Tracking/tasks.js
