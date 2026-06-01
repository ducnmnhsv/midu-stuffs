---
name: tradex-analyst
description: "TradeX API 분석 전문 스킬 — API 엔드포인트 추적, Kafka 흐름 분석, TradeX Knowledge 기반 시스템 조사. 'API 분석', 'analyze', 'trace', 'API 추적', 'how does X work', 'which service handles', 'Kafka flow', 'TradeX 시스템 분석' 요청 시 반드시 이 스킬을 사용. tradex-orchestrator가 Phase 1에서 자동 호출."
---

# tradex-analyst Skill

Phân tích API theo thứ tự Knowledge-first. Xem code là lãng phí công sức nếu đã có tài liệu.

## Quy trình phân tích (4 bước)

### Bước 1 — Kiểm tra TradeX Knowledge
Đọc `TradeX Knowledge/System/` và `TradeX Knowledge/API Standards/` trước tiên.
- Đã có tài liệu → trích dẫn, không scan code
- Có một phần → dùng tài liệu làm điểm khởi đầu, chỉ xác nhận phần còn thiếu

### Bước 2 — Xác định Endpoint
Tìm trong `TradeX MCP/Knowledge based/rest-proxy-main/src/app/routes/`:
- `derivatives/` — Derivatives endpoints
- `equity/` — Cổ phiếu thông thường
- `Market.ts` — Market data

Kiểm tra `scopeData.json` → URI pattern, Kafka forward topic, scope yêu cầu

### Bước 3 — Truy vết Consumer Service
Dùng bảng Core Microservices trong CLAUDE.md để map Kafka topic → service:
- `aaa` → Authentication
- `paave-real-trading` → lotte-bridge → Lotte Securities
- `market-query-v2` → market-query-v2 service
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
1. Lưu file mới vào `TradeX Knowledge/System/`
2. Cập nhật `TradeX Knowledge/_index.md`

Không lưu nếu chỉ là kết quả phân tích thông thường đã biết.
