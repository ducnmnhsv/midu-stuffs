# Claude Chat / Cowork — System Instructions
# tradex-monitoring · NHSV Pro · Midu

> Paste toàn bộ file này vào đầu session Claude Chat hoặc Cowork.
> Không cần giải thích thêm — Claude sẽ tự hiểu context và rules.

---

## 1. Về tôi

**Midu** (Nguyễn Minh Đức) — Product Owner / PM tại **NHSV** (NH Securities Vietnam).

- Sản phẩm chính: **NHSV Pro** — mobile trading app cho retail investors (React Native)
- Daily stack: Figma · Jira (NHMTS) · Sentry · Claude
- Role trong task: phân tích API/system, viết spec cho BE dev, tạo FE issues, tạo PM documentation

**Deliverable audiences:**
- **PM** — không có code, dùng diagrams + prose
- **BE Developer** — code OK trong Specifications/
- **FE Developer** — issues với screen/component reference rõ ràng

---

## 2. Active Projects

| Codename | Jira | Status | Ghi chú |
| --- | --- | --- | --- |
| **Derivatives** | NHMTS-682 | 🔄 In Progress | Go-live milestone — hợp đồng tương lai VN30F |
| **Smart OTP** | — | 🔄 Pending handoff | Multi-channel OTP; design done, chờ 3rd party |
| **Finlens** | — | 📋 Personal | AI agent phân tích stock từ screenshot |

---

## 3. Glossary — Bảng từ vựng

### Công ty & sản phẩm

| Term | Meaning |
| --- | --- |
| **NHSV** | NH Securities Vietnam — công ty |
| **NHSV Pro** | Mobile trading app (sản phẩm chính) |
| **TradeX** | Backend trading API system của NHSV Pro |
| **NHMTS** | Jira project key — mọi issue đều prefix NHMTS-xxx |
| **Core / Core Lotte** | Hệ thống core nghiệp vụ của Lotte Securities (third-party backend) |
| **lotte-bridge** | Microservice bridge kết nối TradeX ↔ Core Lotte |
| **Derivatives / Phái sinh** | Derivatives trading — hợp đồng tương lai VN30F |
| **Smart OTP** | Multi-channel OTP feature (SMS + app) |
| **eKYC** | Electronic KYC — tích hợp VNPT |
| **eContract** | Electronic contract — tích hợp FPT |
| **VSD** | Vietnam Securities Depository — lưu ký chứng khoán |

### Kỹ thuật

| Term | Meaning |
| --- | --- |
| **FE / BE** | Frontend (React Native) / Backend (Node.js + Java) |
| **DTO** | Data Transfer Object — object dùng trong API request/response |
| **PRD** | Product Requirements Document |
| **PO** | Product Owner (Midu) |
| **rest-proxy** | API Gateway service của TradeX (Node.js/Express) |
| **order-v2** | Order service (Java) |
| **realtime-v2** | Real-time market data service (Java + Redis) |
| **ws-v2** | WebSocket server (Node.js) |

### Shorthand hàng ngày

| Shorthand | Meaning |
| --- | --- |
| **derivatives** | Derivatives epic — NHMTS-682 |
| **lotte** | Core Lotte / lotte-bridge integration |
| **tradex** | TradeX API system (không phải app) |
| **pro** | NHSV Pro app |
| **the bridge** | lotte-bridge service |
| **smart otp / sotp** | Smart OTP feature |

### Thị trường chứng khoán Việt Nam

| Term | Meaning |
| --- | --- |
| **HOSE / HNX / UPCOM** | 3 sàn giao dịch Việt Nam |
| **VN30 / VN30F** | Rổ 30 cổ phiếu vốn hóa lớn / Hợp đồng tương lai chỉ số VN30 |
| **Lệnh LO / MP / ATO / ATC** | Limit / Market / At-The-Open / At-The-Close orders |
| **T+2 / T+3** | Settlement cycle |
| **Margin** | Giao dịch ký quỹ |
| **Room nước ngoài** | Foreign ownership limit |
| **Biên độ dao động** | Price fluctuation limit (±7% HOSE, ±10% HNX) |

---

## 4. TradeX System Architecture

**Message Flow:**
```
Client → rest-proxy → Kafka Topic → Backend Service → Response
```

**Core Microservices:**

| Service | Tech | Role |
| --- | --- | --- |
| `rest-proxy` | Node.js, Express | API Gateway — nhận request, route vào Kafka |
| `aaa` | Node.js, MySQL, Redis | Authentication |
| `lotte-bridge` | Node.js | Bridge TradeX ↔ Core Lotte — xử lý order, account |
| `order-v2` | Java | TradeX-native orders (TP/SL, Stop, OCO) |
| `realtime-v2` | Java, Redis | Real-time market data processing |
| `ws-v2` | Node.js | WebSocket server cho client |
| `market-query-v2` | Node.js, MongoDB | Market data queries |

**External integrations:**

| Partner | Purpose |
| --- | --- |
| Lotte Securities | Trading API, account management (Core) |
| FPT | SMS Gateway (OTP) |
| VietStock | Company info, news, financial data |
| OneSignal | Push notifications |
| VNPT | eKYC |

---

## 5. TradeX API Standards (CRITICAL — áp dụng mọi khi viết spec)

### 5.1 Naming Convention

**Nguyên tắc:** TradeX API dùng **tên và giá trị do TradeX định nghĩa**, KHÔNG dùng tên field hoặc mã số của Core (Lotte).

| ❌ Sai | ✅ Đúng |
| --- | --- |
| Param `sent` với giá trị `0`, `1`, `2` (tên Lotte) | Param `orderSendFilter`: `ALL`, `SENT`, `PENDING` |
| Param `sell_buy_tp` (tên Lotte) | Param `sellBuyType`: `ALL`, `BUY`, `SELL` |
| Mô tả "0=all, 1=đã gửi" không kèm TradeX name | Định nghĩa TradeX name + value, sau đó bảng mapping |

**Quy tắc:**
1. **Tên param:** camelCase, có nghĩa (vd. `orderSendFilter`, `sellBuyType`) — không dùng snake_case của Lotte
2. **Giá trị enum:** Dùng chuỗi có nghĩa (vd. `ALL`, `SENT`, `BUY`) — không expose mã số Lotte
3. **Bảng mapping bắt buộc** — mỗi spec phải có:

| TradeX param | TradeX values | Core (Lotte) field | Core values |
| --- | --- | --- | --- |
| `orderSendFilter` | `ALL`, `SENT`, `PENDING` | `sent` | `0`, `1`, `2` |
| `sellBuyType` | `ALL`, `BUY`, `SELL` | `sell_buy_tp` | `0`, `1`, `2` |

### 5.2 Auto-Populated Fields (FE không cần truyền)

| TradeX Field | Source | Lotte Field |
| --- | --- | --- |
| `sourceIp` | Request IP (auto by rest-proxy) | `cli_ip_addr` |
| `userId` | JWT Token | `user_id` / `hts_user_nm` |
| `identifierNumber` | JWT Token | `idno` |
| `name` | JWT Token (userData) | `hts_user_nm` |
| `mdm_tp` | Derived từ platform/channel | `mdm_tp` |

**FE vẫn phải truyền:** `deviceUniqueId` (→ `cli_mac_addr`), `accountNumber` (→ `acnt_no`)

### 5.3 GET API — Pagination Parameters

| TradeX (query param) | Required | Core (Lotte) field | Ghi chú |
| --- | --- | --- | --- |
| `fetchCount` | ❌ No | `row_count` | Số bản ghi/trang |
| `nextKey` | ❌ No | `next_data` / `next_key` | Pagination token; trang đầu = "" |

### 5.4 Validation Philosophy

**TradeX validates ONLY:** required fields present, data types, basic format, length limits

**Core validates ALL business rules:** price range, margin, trading hours, account permissions

→ Nếu TradeX validation pass → forward to Core → Core error = pass-through as 422

### 5.5 Error Format Standards

**Validation Error (400) — TradeX:**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "accountNumber",
      "messageParams": ["accountNumber"]
    }
  ]
}
```

**Business Error (422) — Lotte pass-through:**
```json
{
  "code": "ORDER_PLACE_1005",
  "message": "[V3120] Không đủ tiền ký quỹ"
}
```
Format: `{OPERATION}_{LOTTE_ERROR_CODE}` — vd. `ORDER_PLACE_`, `ORDER_CANCEL_`, `TRANSFER_CASH_`

**TradeX-native Error (422) — không có `message`, dùng `messageParams`:**
```json
{
  "code": "TP_PRICE_MUST_BE_HIGHER_THAN_ENTRY",
  "messageParams": ["1250.0", "1200.0"]
}
```

### 5.6 Success Response

**HTTP 200 = Success. KHÔNG dùng `success: true` hay `code: "0000"` trong response body.**

| Integration type | Success response pattern |
| --- | --- |
| **Lotte-integrated** (Regular Order) | `{ "message": "[V0307] ...", "orderNumber": "..." }` |
| **TradeX-native mutation** (TP/SL, Stop, OCO) | `{ "id": 255 }` |
| **Query API** | `{ "totalCount": 10, "orders": [...] }` |

### 5.7 HTTP Status Codes

| Status | Use case | TradeX code |
| --- | --- | --- |
| 200 | Success | — |
| 400 | Validation error | `INVALID_PARAMETER` |
| 401 | Auth failed | `UNAUTHORIZED`, `TOKEN_EXPIRED` |
| 403 | Permission denied | `FORBIDDEN` |
| 404 | Not found | `OBJECT_NOT_FOUND` |
| 422 | Business rule violation (Lotte) | `{OPERATION}_{LOTTE_CODE}` |
| 500 | Server error | `INTERNAL_SERVER_ERROR` |

### 5.8 Language Handling

| Accept-Language | Lotte lang_code |
| --- | --- |
| `vi` | `V` |
| `en` | `E` |
| `ko` | `K` |
| (empty) | `V` (default) |

---

## 6. Document Structure & Routing

### Folder structure

```
Derivatives/Planning documentation/{Category}/
├── README.md          ← PM entry point, business overview
├── Planning/          ← PM reads (NO code blocks, prose only)
├── Specifications/    ← Dev reads (code OK)
├── Issues/            ← FE/BE issues
└── Archive/           ← Tài liệu cũ
```

### File routing — output đến đúng folder

| Feature | Document type | Destination |
| --- | --- | --- |
| Derivatives | Issue (BE/FE) | `Derivatives/Planning documentation/{Category}/Issues/` |
| Derivatives | Planning / PRD | `Derivatives/Planning documentation/{Category}/Planning/` |
| Derivatives | API Specification | `Derivatives/Planning documentation/{Category}/Specifications/` |
| Smart OTP | Any | `Smart-OTP/{Issues\|Planning\|Specifications}/` |
| TradeX infra | Any | `TradeX-Monitor/` |
| API standards | Reference | `Knowledge/TradeX/API Standards/` |
| Market research | Reference | `Analytics/NH-Research/` |

### Derivatives category mapping

| Topic | Category folder |
| --- | --- |
| Market data, GTGD, chart, quote | `Market data/` |
| Order, TP/SL, OCO, stop order | `Order/` |
| Account, margin | `Account/` |
| Cash, nạp rút | `Cash transaction/` |
| Mở tài khoản phái sinh | `Open_DR_Sub_Account/` |

### Naming conventions

- Files: PascalCase + underscore — `Chart_API_Spec.md`, `Order_API_Implementation.md`
- Entry point: `README.md` (không dùng `_index.md` hay `00_`)
- Không dùng brackets: `[ISSUE]`, `[BRIEF]`

---

## 7. Issues/ Template Structure

```markdown
## Executive Summary (PM READS THIS)
### Problem Statement
### Current vs Target State
### Solution Approach (high-level, no code)
### Timeline
### Success Criteria

---

## Technical Background (PM CAN SKIP)

## Detailed Requirements (PM CAN SKIP)
```

---

## 8. Document Footer (bắt buộc mọi spec/issue Derivatives)

```
Document Status: ✅/📋/🔄 | For: [audience] | Next Steps: [action]
```

---

## 9. Rules — áp dụng mọi lúc

| Code | Rule |
| --- | --- |
| **C1** | Tất cả endpoint, DTO, service method → check TradeX naming trước khi finalize |
| **C1** | Request/query params dùng tên TradeX, KHÔNG dùng tên field Core (Lotte). Luôn kèm bảng mapping |
| **C2** | Trước khi finalize Order API response doc → check Response Format Standards (section 5.5–5.6) |
| **C3** | Folder `Planning/` → NO code blocks, prose + diagrams only |
| **C4** | Trước khi scan codebase cho TradeX system knowledge → check Knowledge/TradeX/ trước |
| **C5** | Mọi spec/issue Derivatives phải có document footer |

**Tooling rules:**
- **No auto-commit/push** — suggest content + path, Midu review và commit thủ công
- **Postman:** Test requests → chỉ tạo trong "TradeX QA session". "TradeX API v2" = reference only
- **Chỉ tạo tài liệu** — không implement code vào source files thực tế. Code snippet trong Specifications/ là OK để Dev tham khảo

---

## 10. How I like to work with Claude

- **Mix Vietnamese/English:** technical terms giữ tiếng Anh
- **Dùng bảng, diagram, ví dụ thực tế** — không prose dài
- **Kèm business interpretation** bên cạnh technical detail
- **Tối đa 1–2 câu hỏi clarifying**, bullet format — không hỏi nhiều
- **Effort level: high** — comprehensive analysis với đầy đủ tables, diagrams, business rules
- **Knowledge-first:** không cần hỏi lại context đã có trong instruction này

---

*Version: 2026-06 · Repo: tradex-monitoring · Owner: Midu*
*Source of truth: `CLAUDE.md` + `Knowledge/TradeX/API Standards/tradex-api-conventions.md`*
