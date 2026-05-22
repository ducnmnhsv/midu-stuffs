# Analyze TradeX API

Phân tích một API endpoint trong hệ thống TradeX theo 4 bước chuẩn.

## Process

**Step 1 — Identify Endpoint**
Tìm route trong `rest-proxy-main/src/app/routes/` (hoặc theo path user cung cấp).

**Step 2 — Trace Kafka Flow**
Kiểm tra `rest-proxy-main/src/data/scopeData.json`:
- URI pattern
- Forward topic (Kafka)
- Token type / scope required

**Step 3 — Find Consumer Service**
Trace từ Kafka topic đến service xử lý (xem Core Services table trong CLAUDE.md).

**Step 4 — Document API**
Output theo template:

```yaml
API_ID: [SERVICE]_[DOMAIN]_[ACTION]
Endpoint:
  Method: [GET/POST/PUT/DELETE]
  Path: /api/v1/[path]
Authentication:
  Type: JWT Bearer
  Scope: [scope_name]
Kafka:
  Topic: [topic]
  Consumer: [service]
Request:
  Headers: [...]
  Parameters: [...]
  Body: [...]
Response:
  Success: [...]
  Errors: [...]
Business Rules:
  - [rule 1]
  - [rule 2]
```

## Important

- **Knowledge-first:** Check `TradeX Knowledge/System/` và `TradeX Knowledge/API Standards/` TRƯỚC khi scan codebase
- **Naming check:** Verify conventions sau khi phân tích (C1)
- **Save findings:** Sau phân tích mới → lưu vào `TradeX Knowledge/System/` và update `_index.md`
- **Order API:** Nếu là Order API → áp dụng `tradex-order-api-response-standards` (CLAUDE.md)
