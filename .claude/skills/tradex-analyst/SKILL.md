---
name: tradex-analyst
description: "Phân tích API TradeX — truy vết endpoint, Kafka flow, service nào xử lý, cơ chế hệ thống. Dùng khi: 'API này hoạt động thế nào', 'service nào xử lý X', 'trace API', 'Kafka flow', 'analyze', 'how does X work', 'which service handles'. Được tradex-orchestrator gọi tự động ở Phase 1."
---

# tradex-analyst Skill

Phân tích API theo thứ tự Knowledge-first. Xem code là lãng phí công sức nếu đã có tài liệu.

## Quy trình phân tích (4 bước)

### Bước 1 — Kiểm tra TradeX Knowledge
Đọc `Knowledge/TradeX/System/` và `Knowledge/TradeX/API Standards/` trước tiên.
- Đã có tài liệu → trích dẫn, không scan code
- Có một phần → dùng tài liệu làm điểm khởi đầu, chỉ xác nhận phần còn thiếu

### Bước 2 — Xác định Endpoint
Tìm trong `Knowledge/TradeX-MCP/rest-proxy-main/src/app/routes/`:
- `derivatives/` — Derivatives endpoints
- `equity/` — Cổ phiếu thông thường
- `Market.ts` — Market data

Kiểm tra `scopeData.json` → URI pattern, Kafka forward topic, scope yêu cầu

### Bước 3 — Truy vết Consumer Service
Dùng bảng Core Microservices trong CLAUDE.md để map Kafka topic → service:
- `aaa` → Authentication
- `lotte-bridge` → lotte-bridge service → Lotte Securities
- `market-v2` → market-query-v2 service
- `notification` → notification service

### Bước 4 — Phân loại Order API (nếu liên quan đến lệnh)

| Dấu hiệu | Integration Type |
|----------|----------------|
| Gọi thẳng Lotte API, response có `message` | lotte-integrated |
| Chỉ lưu vào TradeX DB, response là `{ "id" }` | tradex-native |
| Không rõ | Ghi cả hai khả năng |

## Định dạng Findings

Lưu vào `_workspace/01_analyst_findings.md`:

```markdown
# Analyst Findings: [Tên API/Tính năng]

## Tổng quan API
- Endpoint: [METHOD] /api/v1/...
- Kafka Topic: [topic] (N/A nếu gọi service trực tiếp)
- Consumer Service: [service]
- Integration Type: [lotte-integrated | tradex-native | N/A]
- Nguồn: [knowledge-doc | codebase-scan]

## Cấu trúc Request
| Field | Type | Bắt buộc | Auto-populated | Lotte Field |
|-------|------|----------|---------------|-------------|

## Cấu trúc Response
### Thành công (200)
[format]
### Lỗi (400/401/422)
[patterns]

## Business Rules
1. [rule]

## Mapping (TradeX → Lotte)
| TradeX | Lotte | Ghi chú |
|--------|-------|---------|

## Kiểm tra Convention
- URL naming: [OK | VẤN ĐỀ: mô tả]
- DTO naming: [OK | VẤN ĐỀ: mô tả]
- Response format: [OK | VẤN ĐỀ: mô tả]

## Phát hiện mới (cần lưu vào Knowledge)
[Nội dung mới chưa có trong TradeX Knowledge]
```

## Quy tắc lưu phát hiện mới

Nếu tìm thấy cơ chế chưa được document:
1. Lưu file mới vào `Knowledge/TradeX/System/`
2. Cập nhật `Knowledge/TradeX/_index.md`

Không lưu nếu chỉ là kết quả phân tích thông thường đã biết.
