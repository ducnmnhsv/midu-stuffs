## 1. Bối cảnh & Mục tiêu

Sau Phase 2, hệ thống đã có lớp bảo vệ vững chắc (NemoClaw sandbox, guardrails) và audit trail đầy đủ. Tuy nhiên, agent vẫn hoạt động ở chế độ **phản ứng (reactive)** – chỉ làm những gì được yêu cầu. Phase 3 hướng đến việc biến agent thành **người đồng hành chủ động (proactive)** , có khả năng học hỏi, dự đoán và đề xuất giải pháp mà không cần chờ lệnh.

### 1.1 Những hạn chế của Phase 2

* Agent chỉ trả lời khi được hỏi.
* Không nhớ bài học từ các incident trước đó.
* Không thể tự động đề xuất giải pháp khi phát hiện lỗi quen thuộc.
* Chưa có khả năng phân tích nguyên nhân gốc rễ (root cause analysis) sâu.
* Chưa thể tự động tạo báo cáo xu hướng hay cảnh báo sớm.

### 1.2 Mục tiêu Phase 3

* **Học từ quá khứ**: Agent ghi nhớ cách xử lý các incident trước đó để tái sử dụng.
* **Chủ động phát hiện và đề xuất**: Khi lỗi tái diễn, agent nhận diện và đề xuất giải pháp (kèm link Jira, PR, tài liệu) và chờ phê duyệt.
* **Phân tích nguyên nhân gốc rễ**: Tự động phân tích log, code change, deployment history để xác định nguyên nhân sâu xa.
* **Cảnh báo sớm & xu hướng**: Phát hiện các pattern bất thường trước khi sự cố xảy ra.
* **Tự động hóa xử lý (có kiểm soát)**: Thực hiện các hành động khắc phục đã được phê duyệt trước đó (hoặc chờ phê duyệt lần cuối).

## 2. Phạm vi Phase 3

### 2.1 Sẽ triển khai

| Nhóm tính năng                 | Mô tả                                                                       |
| ------------------------------ | --------------------------------------------------------------------------- |
| Continual Learning (MetaAgent) | Agent học từ lịch sử tương tác, incidents, và cách xử lý.                   |
| Root Cause Analysis (RCA)      | Phân tích log, change history, deployment để tìm nguyên nhân.               |
| Proactive Incident Detection   | Phát hiện lỗi quen thuộc và đề xuất giải pháp kèm bằng chứng.               |
| Auto-Fix với Human Approval    | Đề xuất hành động sửa chữa, chờ phê duyệt (qua Teams) rồi thực thi.         |
| Knowledge Graph (CIK-LLM)      | Xây dựng đồ thị tri thức từ Jira, code, Confluence, incident history.       |
| Xu hướng & Cảnh báo sớm        | Phân tích metrics theo thời gian, dự đoán nguy cơ (disk full, memory leak). |
| Báo cáo thông minh             | Tổng hợp weekly/monthly report với insights và khuyến nghị.                 |

## 3. Kiến trúc mở rộng

```txt
flowchart TB
    subgraph EXISTING [Existing from Phase 2]
        A[OpenFang + Skills]
        B[NemoClaw Sandbox]
        C[Audit DB]
    end

    subgraph NEW [New Components - Phase 3]
        D[MetaAgent Learning Engine]
        E[Knowledge Graph Store]
        F[Root Cause Analyzer]
        G[Proactive Detector]
        H[Auto-Fix Orchestrator]
        I[Trend Analyzer]
    end

    subgraph DATA [Data Sources]
        J[Incident History]
        K[Code Repos]
        L[Jira/Confluence]
        M[Logs & Metrics]
    end

    D --> E
    F --> E
    G --> D
    H --> A
    I --> J & M

    A --> D
    A --> F
    A --> G
    A --> I
    B --> H

    J & K & L & M --> E
```

### 3.1 Các thành phần mới

#### 3.1.1 MetaAgent Learning Engine

* **Nhiệm vụ**: Học từ lịch sử tương tác, incidents, và các quyết định đã được phê duyệt.
* **Cơ chế**:
  * Mỗi khi có incident được giải quyết, MetaAgent ghi nhận: lỗi, nguyên nhân, giải pháp, người phê duyệt.
  * Xây dựng vector embeddings cho từng pattern lỗi.
  * Khi lỗi tái diễn, so sánh độ tương đồng để đề xuất giải pháp cũ.
* **Lưu trữ**: SQLite/PostgreSQL cho structured knowledge, vector store (ChromaDB) cho embeddings.

#### 3.1.2 Knowledge Graph Store (CIK-LLM)

* **Nhiệm vụ**: Lưu trữ mối quan hệ giữa các thực thể: Jira issue ↔ code change ↔ PR ↔ deployment ↔ incident.
* **Cấu trúc**:
  * Node: Issue, PR, Commit, Service, Incident, Solution, Person.
  * Edge: `fixes`, `caused_by`, `deployed_in`, `related_to`.
* **Lợi ích**: Cho phép truy vấn phức hợp như *"Những lỗi nào liên quan đến payment-service và đã được sửa bởi ai?"*

#### 3.1.3 Root Cause Analyzer

* **Nhiệm vụ**: Khi phát hiện lỗi mới, tự động phân tích để tìm nguyên nhân.
* **Đầu vào**:
  * Log lines xung quanh thời điểm lỗi.
  * Change history (deployment, config change) trong khoảng thời gian đó.
  * Code changes gần nhất.
* **Đầu ra**:
  * Tóm tắt nguyên nhân (ví dụ: "Lỗi do commit abc123 thay đổi schema database").
  * Đề xuất hướng khắc phục.
* **Công nghệ**: Dùng LLM (Qwen3.5) với prompt engineering kết hợp context từ knowledge graph.

#### 3.1.4 Proactive Detector

* **Nhiệm vụ**: Chủ động phát hiện các pattern lỗi quen thuộc mà không cần chờ user hỏi.
* **Cơ chế**:
  * Chạy định kỳ (mỗi 10 phút) quét log mới.
  * So sánh với knowledge graph để tìm pattern tương đồng.
  * Nếu phát hiện pattern quen thuộc → gửi đề xuất khắc phục kèm bằng chứng vào Teams channel (có tag người liên quan).

Ví dụ:

```txt
🔍 *Proactive Detection*
Phát hiện pattern lỗi "DB connection timeout" tại payment-service (14:23)
Lỗi này từng xảy ra ngày 10/3 và được sửa bởi @John qua PR #456.
Đề xuất: Tăng connection pool size.
Approve? [Yes] [No] [Xem chi tiết]
```

#### 3.1.5 Auto-Fix Orchestrator

* **Nhiệm vụ**: Thực thi các hành động sửa chữa sau khi được phê duyệt.
* **Luồng**:
  1. Proactive Detector hoặc user yêu cầu một hành động.
  2. Hành động được gửi đến Auto-Fix Orchestrator.
  3. Orchestrator gửi request phê duyệt qua Teams (có nút Approve/Reject).
  4. Nếu Approve, Orchestrator gọi skill tương ứng (chạy qua NemoClaw sandbox).
  5. Ghi nhận kết quả vào audit trail và knowledge graph.
* **Phạm vi hành động**:
  * Restart service (có thể).
  * Rollback deployment (nếu có evidence).
  * Chạy script khắc phục (đã được kiểm chứng).
  * Tạo Jira ticket tự động (nếu cần theo dõi).

#### 3.1.6 Trend Analyzer & Early Warning

* **Nhiệm vụ**: Phân tích metrics theo thời gian để phát hiện xu hướng bất thường.
* **Dữ liệu**: Từ Kibana/Elasticsearch (log volume, error rate), Grafana (CPU, RAM, disk, latency), Jenkins (build success rate).
* **Cảnh báo sớm**:
  * Disk usage tăng nhanh → dự đoán ngày hết dung lượng.
  * Error rate tăng dần trong nhiều ngày → báo động trước khi vượt ngưỡng.
  * Build success rate giảm → cảnh báo về chất lượng code.
* **Đầu ra**: Báo cáo hàng tuần, cảnh báo qua Teams khi phát hiện trend nguy hiểm.

## 4. Use Cases mới

### UC‑C01: Proactive Incident Detection & Suggestion

**Mô tả**: Agent tự động phát hiện lỗi quen thuộc và đề xuất giải pháp kèm bằng chứng (Jira issue, PR, tài liệu).

**Luồng**:

1. Proactive Detector quét log, thấy pattern "DB connection timeout".
2. Truy vấn knowledge graph → tìm thấy lỗi tương tự từng xảy ra, được fix bởi PR #456, có Jira PROJ-789.
3. Agent gửi message vào Teams channel của team:

```txt
🔁 *Recurring Incident Detected*
Service: payment-api
Error: DB connection timeout (pattern match 92%)
Previous fix: PR #456 (John, 10/3) - Tăng pool size từ 10 lên 20
Jira: PROJ-789 - "Connection pool exhaustion"

📌 *Suggested action:* Apply same fix (update pool size)
Approve? [Apply] [Ignore] [View Details]
```

1. Team lead bấm **Apply**.
2. Auto-Fix Orchestrator thực hiện thay đổi (qua NemoClaw).
3. Gửi xác nhận: ✅ \*Fix applied. New pool size \= 20. Monitoring.\*

### UC‑C02: Root Cause Analysis for New Errors

**Mô tả**: Khi phát hiện lỗi mới chưa từng thấy, agent tự phân tích nguyên nhân.

**Luồng**:

1. Log scanner phát hiện lỗi lạ: `NullPointerException at OrderService.process`.
2. Agent gọi Root Cause Analyzer.
3. RCA lấy:
   * 100 dòng log xung quanh lỗi.
   * Danh sách deployment trong 1 giờ trước lỗi.
   * Code changes liên quan đến `OrderService` trong 24h qua.
4. LLM phân tích và đưa ra kết luận:

```txt
🔍 *Root Cause Analysis*
Error: NullPointerException at OrderService.process(line 145)

🕒 Timeline:
- 13:45: Deployment #315 (commit abc123) - "Add validation for order amount"
- 13:47: First error appears

📝 Code change analysis:
The commit abc123 removed null check for `order.getPayment()` before calling `.process()`.
This matches the error location.

💡 Suggested fix: Restore null check or ensure payment is never null.
Related file: OrderService.java (line 142-150)

[Create Jira] [Show Diff] [Ignore]
```

### UC‑C03: Smart Rollback Recommendation

**Mô tả**: Sau khi deploy, nếu error rate tăng đột biến, agent đề xuất rollback.

**Luồng**:

1. Jenkins webhook báo deploy thành công lúc 14:00.
2. Agent bắt đầu theo dõi error rate từ Kibana trong 15 phút.
3. Phát hiện error rate tăng từ 0.1% lên 5% sau deploy.
4. Truy vấn knowledge graph → thấy không có incident nào khác trùng thời điểm.
5. Agent gửi cảnh báo:

```txt
🚨 *Post-Deploy Anomaly*
Deployment #316 (payment-api) at 14:00
Error rate: 5.2% (↑ from 0.1% pre-deploy)
Suspected regression.

📌 *Recommendation:* Rollback to previous version.
[Rollback Now] [Acknowledge] [Investigate Later]
```

1. Nếu chọn Rollback, agent kích hoạt Jenkins job rollback.

### UC‑C04: Weekly Intelligence Report

**Mô tả**: Tự động tổng hợp báo cáo tuần với insights và khuyến nghị.

**Nội dung**:

* Tổng quan: Số incident, MTTR, error rate trend.
* Top 3 lỗi tái diễn nhiều nhất.
* Đề xuất cải thiến (ví dụ: "Lỗi timeout xuất hiện 5 lần tuần này, nên tăng connection pool").
* Dự đoán: "Dự kiến hết disk trong 12 ngày nếu không cleanup".

**Định dạng**: Markdown, gửi vào Teams channel `#weekly-report` mỗi sáng thứ Hai.

## 5. Kế hoạch triển khai Phase 3 (8–10 tuần)

### Sprint 1 (Tuần 1–3): Nền tảng học tập

* Xây dựng Knowledge Graph Store (PostgreSQL + pgvector hoặc Neo4j).
* Triển khai vector embeddings cho incident patterns (dùng Ollama + nomic-embed-text).
* Phát triển MetaAgent Learning Engine – ghi nhận lịch sử incident và giải pháp.
* Cập nhật audit trail để lưu thêm thông tin học tập.

### Sprint 2 (Tuần 4–6): Phân tích & Phát hiện

* Xây dựng Root Cause Analyzer (tích hợp LLM với context từ KG).
* Phát triển Proactive Detector – chạy định kỳ, so sánh pattern.
* Tích hợp Trend Analyzer với Kibana/Grafana API.
* Thêm cảnh báo sớm (disk full, memory leak).

### Sprint 3 (Tuần 7–8): Tự động hóa & Báo cáo

* Xây dựng Auto-Fix Orchestrator (Teams approval flow).
* Mở rộng guardrails để cho phép auto-fix có kiểm soát.
* Phát triển Weekly Intelligence Report.
* Kiểm thử end-to-end với các tình huống thực tế.

### Sprint 4 (Tuần 9–10): Tối ưu & Chuyển giao

* Đánh giá độ chính xác của RCA (so với phân tích của con người).
* Tối ưu hiệu năng (thời gian phát hiện, xử lý).
* Cập nhật tài liệu vận hành cho admin.
* Training cho team về cách tương tác với proactive agent.

## 6. Tiêu chí thành công

| KPI                       | Mục tiêu                                                        |
| ------------------------- | --------------------------------------------------------------- |
| Proactive detection rate  | ≥80% lỗi tái diễn được phát hiện và đề xuất giải pháp           |
| RCA accuracy              | ≥70% kết luận của agent được đánh giá là chính xác hoặc hữu ích |
| Auto-fix adoption         | ≥50% đề xuất được phê duyệt và thực thi tự động                 |
| Time to resolution (MTTR) | Giảm ≥30% so với Phase 2                                        |
| Early warning accuracy    | ≥80% cảnh báo sớm (disk full, trend) là chính xác               |
| User satisfaction         | ≥80% users thấy agent chủ động hữu ích                          |