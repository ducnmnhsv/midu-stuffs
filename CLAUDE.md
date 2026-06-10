# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@/Users/ducnguyen/Documents/project/nhsv-mts-rn/CLAUDE.md

---

## ⚠️ CRITICAL RULE — nhsv-mts-rn (READ-ONLY)

`/Users/ducnguyen/Documents/project/nhsv-mts-rn` là FE codebase của app **NHSV Pro** (React Native).

- **TUYỆT ĐỐI KHÔNG** chỉnh sửa, tạo file, hoặc thay đổi bất kỳ thứ gì trong repo này
- Chỉ dùng để **đọc và tham chiếu** (read-only reference)
- Khi task liên quan đến FE (component, screen, API call, navigation, type definitions...) → ưu tiên query `graphify-out/graph.json` trước, nếu không đủ thì mới scan trực tiếp codebase nhsv-mts-rn
- Mọi output (issue, spec, doc) được ghi vào `tradex-monitoring`, không phải nhsv-mts-rn

### nhsv-mts-rn Knowledge Graph (graphify)

Graph được build từ `/graphify /Users/ducnguyen/Documents/project/nhsv-mts-rn`, output lưu tại:
- `tradex-monitoring/graphify-out/graph.json` — persistent knowledge graph, query không cần re-read toàn bộ codebase
- `tradex-monitoring/graphify-out/graph.html` — interactive visualization
- `tradex-monitoring/graphify-out/GRAPH_REPORT.md` — god nodes, surprising connections, suggested questions

**Khi cần tra cứu FE:**
1. Query graph: `/graphify query "tên component hoặc concept"`
2. Nếu graph chưa có → scan trực tiếp nhsv-mts-rn (read-only)
3. Sau khi có thay đổi lớn trong nhsv-mts-rn → rebuild: `/graphify /Users/ducnguyen/Documents/project/nhsv-mts-rn --update`

---

## 하네스: TradeX Monitoring

**목표:** API 분석 → 문서 생성 → 컨벤션 검증 파이프라인 자동화

**트리거:** API 분석, Spec/Doc/FE Issue 생성, 문서 수정/재실행 요청 시 `tradex-orchestrator` 스킬을 사용하라. 단순 질문은 직접 응답 가능.

**변경 이력:**
| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-06-01 | 초기 구성 | 전체 | 신규 하네스 구축 |
| 2026-06-01 | Nâng cấp Agent Teams | orchestrator + 3 agents | Bật CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS |

---

# TradeX Monitoring — Claude Code Configuration

## Project Overview

Workspace chính cho phân tích và tài liệu hệ thống **TradeX** — Backend của **NHSV Pro** (ứng dụng chứng khoán Việt Nam của NHSV). Sản phẩm chính: Derivatives trading (hợp đồng tương lai VN30F).

**Main repos:**
- `tradex-monitoring` (this workspace) — documentation, specs, issue tracking
- `nhsv-mts-rn` — FE React Native app (read-only reference)
- `Knowledge/TradeX-MCP/` — microservice source code (reference)

---

## Ecosystem Orchestrator — Task Routing

Khi nhận task mới, tự xác định loại task và áp dụng workflow tương ứng:

### Routing Table

| Task Type | Trigger Keywords | Workflow |
|-----------|-----------------|----------|
| **API Spec** | "API spec", "tạo spec", "viết spec", "API specification" | tradex-api-naming → derivatives-api-spec-format → (if Order API) tradex-order-api-response-standards → tradex-api-conventions |
| **Documentation** | "tạo docs", "viết tài liệu", "create documentation" | derivatives-doc-structure → derivatives-pm-documentation → (if Specifications/) derivatives-api-spec-format |
| **FE Issue** | "FE issue", "frontend issue", "tạo issue FE", "Derivatives FE" | Đọc nhsv-mts-rn → viết issue trong tradex-monitoring → derivatives-doc-structure |
| **System Analysis** | "phân tích API", "trace API", "how does X work", "service nào xử lý" | Check Knowledge/TradeX/ first → tradex-analyst workflow → tradex-api-naming |
| **Order API** | "order API", "lệnh", "đặt lệnh", "hủy lệnh", "sửa lệnh", "TP/SL", "stop order", "OCO" | ALWAYS apply tradex-order-api-response-standards first |
| **Vague Request** | Request thiếu context, không rõ domain | Clarify → re-route |

### Connection Rules (Always Apply)

- **C1 — Naming Consistency:** Mọi API output phải check naming conventions (URL `/api/v1/{resource}`, DTO `{Resource}{Action}Request/Response`)
- **C2 — Response Format:** Order API → luôn check tradex-order-api-response-standards trước
- **C3 — PM-Readability Gate:** Viết vào `Planning/` folder → NO code blocks
- **C4 — Knowledge-First:** Check `Knowledge/TradeX/` TRƯỚC khi scan codebase
- **C5 — Document Footer:** Mọi spec/issue document phải kết thúc bằng: `**Document Status:** | **For:** | **Next Steps:**`

### Anti-Patterns

| ❌ Never | ✅ Always |
|----------|----------|
| Tạo spec không check tradex-api-naming | Check naming first |
| Viết code trong Planning/ docs | Dùng diagrams/flow |
| Mix Lotte-integrated & TradeX-native response | Check tradex-order-api-response-standards |
| Scan codebase trước khi check Knowledge/TradeX | Knowledge-first |
| Tạo issue không có Issues/ folder structure | Follow derivatives-doc-structure |

---

## TradeX Domain Knowledge

### System Architecture

**Message Flow:**
```
Client → rest-proxy → Kafka Topic → Backend Service → Response
```

**Core Microservices:**

| Service | Tech | Kafka Topic | Role |
|---------|------|-------------|------|
| `rest-proxy` | Node.js, Express | — | API Gateway |
| `aaa` | Node.js, MySQL, Redis | `aaa` | Authentication |
| `configuration` | Node.js, MySQL | `configuration` | Config service |
| `lotte-bridge` | Node.js, Axios | `paave-real-trading` | Lotte Securities integration |
| `market-collector-lotte` | Java | — | Collect market data from Lotte |
| `realtime-v2` | Java, Redis | — | Real-time processing, Redis cache |
| `market-query-v2` | Node.js, Redis, MongoDB | `market-query-v2` | Market query |
| `ws-v2` | Node.js | — | WebSocket server |
| `order-v2` | Node.js | — | Order service |
| `notification` | Java, Kafka | `notification` | Push notifications |

**API Routes Structure (`rest-proxy-main/src/app/routes/`):**
```
api/
├── Authentication.ts
├── equity/
│   ├── Order.ts
│   ├── Account.ts
│   └── kis/, vcsc/
├── derivatives/
└── Market.ts
```

**External Integrations:**

| Partner | Purpose |
|---------|---------|
| Lotte Securities | Trading API, account management |
| VietStock | Company info, news, financial data |
| FPT | SMS Gateway (OTP) |
| OneSignal | Push notifications |

### Market Data — WebSocket Channels

| Channel Pattern | Kafka Topic | Description |
|-----------------|-------------|-------------|
| `market.quote.{code}` | `quoteUpdate` | Giá, KL, ĐTNN |
| `market.bidoffer.{code}` | `bidOfferUpdate` | Sổ lệnh (10 levels) |
| `market.extra.{code}` | `extraUpdate` | Extended info |
| `market.status` | `marketStatus` | Trạng thái phiên |

**Data Flow:**
```
Lotte WS → market-collector-lotte → Kafka → realtime-v2 + ws-v2 → Client
```

**Field Abbreviations (market.quote):**

| Abbr | Meaning |
|------|---------|
| `s` | symbol (Mã CK) |
| `c` | current price |
| `ch` | change |
| `ra` | rate % |
| `vo` | volume (KL) |
| `va` | value (Giá trị) |
| `mb` | matchedBy (ASK/BID) |
| `tb` | totalBidVolume |
| `to` | totalOfferVolume |
| `fr.bv` | foreigner buy (ĐTNN mua) |
| `fr.sv` | foreigner sell (ĐTNN bán) |

**Field Abbreviations (market.bidoffer):**

| Abbr | Meaning |
|------|---------|
| `ss` | session (Phiên GD) |
| `bb` | bestBids |
| `bo` | bestOffers |
| `ep` | expectedPrice |
| `exv` | expectedVolume |
| `p` | price |
| `v` | volume |

### Vietnamese Securities Domain

**Sàn giao dịch:**

| Sàn | Mã | Mô tả |
|-----|----|-------|
| HOSE | HO | Sở GDCK TP.HCM |
| HNX | HA | Sở GDCK Hà Nội |
| UPCOM | UP | Thị trường đăng ký giao dịch |

**Loại lệnh:**

| Loại | Mô tả |
|------|-------|
| LO | Limit Order — Lệnh giới hạn |
| MP | Market Price — Lệnh thị trường |
| ATO | At The Open — Lệnh mở cửa |
| ATC | At The Close — Lệnh đóng cửa |
| MAK | Match And Kill |
| MOK | Match Or Kill |
| PLO | Post Limit Order |

**Phiên giao dịch (HOSE):**

| Phiên | Thời gian | Loại lệnh |
|-------|-----------|-----------|
| Trước mở cửa | 08:30–09:00 | LO |
| ATO | 09:00–09:15 | ATO, LO |
| Liên tục sáng | 09:15–11:30 | LO, MP |
| Nghỉ trưa | 11:30–13:00 | — |
| Liên tục chiều | 13:00–14:30 | LO, MP |
| ATC | 14:30–14:45 | ATC, LO |
| PLO | 14:45–15:00 | PLO |

**Lotte Control Codes (HOSE):**

| Code | Session |
|------|---------|
| `P` | ATO |
| `O`, `R` | LO (Continuous) |
| `I` | INTERMISSION |
| `A` | ATC |
| `C` | PLO |
| `K`, `G` | CLOSED |

---

## API Standards

### GET Optional Parameters (TradeX → Core)

| TradeX (query) | Core (Lotte) | Required |
|----------------|--------------|----------|
| `fetchCount` | `row_count` | ❌ No |
| `nextKey` | `next_data` | ❌ No |

### Naming Convention

- Request/query params dùng **tên TradeX** (không dùng tên/giá trị Lotte/Core)
- Luôn có **bảng mapping** TradeX → Core
- Ví dụ: `orderSendFilter` (ALL/SENT/PENDING) → Lotte `sent` (0/1/2)

### Auto-Populated Fields (FE không cần truyền)

| TradeX Field | Source | Lotte Field |
|--------------|--------|-------------|
| `sourceIp` | Request IP | `cli_ip_addr` |
| `userId` | JWT Token | `user_id` |
| `name` | JWT Token | `hts_user_nm` |
| `identifierNumber` | JWT Token | `idno` |

### Error Format

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [{ "code": "FIELD_IS_REQUIRED", "param": "accountNumber" }]
}
```

**Pass-Through Core Error (422):**
```json
{ "code": "ORDER_PLACE_1005", "message": "[V3120] Lỗi đã xảy ra" }
```

**HTTP Status = Success Indicator:** HTTP 200 = Success. KHÔNG dùng `success: true` hay `code: "0000"` trong response body.

### Language Mapping

| Accept-Language | Lotte lang_code |
|-----------------|-----------------|
| `vi` | `V` |
| `en` | `E` |
| `ko` | `K` |

### Order API Response Standards

**Nguyên tắc:** Integration type quyết định response format.

| Integration Type | Examples | Response Pattern |
|------------------|----------|-----------------|
| **Lotte-integrated** | Regular Orders (Buy/Sell/Modify/Cancel) | Pass-through Lotte messages |
| **TradeX-native** | TP/SL, Stop Orders, OCO | `{ "id": number }` only for mutations |

**Lotte-integrated — Success:**
```json
{ "message": "[V0307] Bạn đã thực hiện lệnh Mua...", "orderNumber": "2026020500012" }
```

**TradeX-native — Mutation Success:**
```json
{ "id": 255 }
```

**TradeX-native — Query Success:**
```json
{ "totalCount": 2, "orders": [...] }
```

**Error Codes:**
- Lotte-integrated: `{OPERATION}_{LOTTE_CODE}` (e.g., `ORDER_PLACE_1005`)
- TradeX-native: `{DESCRIPTIVE_NAME}` SCREAMING_SNAKE_CASE (e.g., `TP_PRICE_MUST_BE_HIGHER_THAN_ENTRY`)
- TradeX-native errors: NO `message` field, dùng `messageParams` array

---

## Documentation Standards (PM/BA)

### Core Principle: Business logic, NOT implementation code

**Folder structure:**
```
Derivatives/Planning documentation/{Category}/
├── README.md                    ← PM entry point
├── Planning/                    ← PM reads (NO code)
├── Specifications/              ← Developers read (code OK)
├── Issues/                      ← Executive Summary for PM + Technical Details for Dev
└── Archive/                     ← Historical reference
```

**Rules by folder:**
- `Planning/` → NO code blocks, no class/method signatures; dùng diagrams & data flow
- `Specifications/` → Code OK, implementation details OK
- `Issues/` → Executive Summary (no code) + Technical Details (code OK)
- `README.md` → Business overview, status, links — NO code snippets

**Issues/ template:**
```markdown
## 📋 Executive Summary (PM READS THIS)
### Problem Statement
### Current vs Target
### Solution Approach (HIGH-LEVEL)
### Timeline
### Success Criteria

---

## 🔍 Technical Background (PM CAN SKIP)
## 📝 Detailed Requirements (PM CAN SKIP)
```

---

## FE Repo Reference

**nhsv-mts-rn** (React Native app):
- Path: `/Users/ducnguyen/Documents/project/nhsv-mts-rn`
- Role: App mobile consume TradeX APIs (read-only reference)
- **KHÔNG** sửa code trong repo này

**Structure tham chiếu:**
- `src/screens/` — Màn hình
- `src/components/` — Components
- `src/reduxs/` — Redux slices, API calls
- `src/hooks/` — Custom hooks
- `src/interfaces/` — Types, API request/response
- `src/utils/` — Helpers

Khi tạo FE issue: đọc nhsv-mts-rn để reference đúng path/component → viết issue trong tradex-monitoring.

---

## Knowledge Base Locations

| Folder | Content | Priority |
|--------|---------|----------|
| `Knowledge/TradeX/System/` | Live production mechanisms | Read first |
| `Knowledge/TradeX/API Standards/` | Conventions, templates | Read first |
| `Knowledge/TradeX/Planning/` | Future features | For planning |
| `Knowledge/TradeX-MCP/` | Microservice source code | Scan if needed |

**Always check `Knowledge/TradeX/` BEFORE scanning codebase.**

**After new analysis:** Save findings to appropriate folder and update `_index.md`.

---

## Derivatives Documentation — File Conventions

**Naming:**
- ✅ PascalCase with underscores: `Chart_API_Spec.md`, `Order_API_Implementation.md`
- ✅ `README.md` tại category root (không dùng `_index.md` hay `00_EXECUTIVE_SUMMARY.md`)
- ❌ NO brackets: `[ISSUE]`, `[BRIEF]`, `[COMPLETION]`
- ❌ NO special prefixes: `_index.md`, `00_`

**File organization:**
- Planning docs: Numbered 01–04 trong `Planning/` folder
- Specifications: `Specifications/` folder, max ~500 lines mỗi file
- Active issues: `Issues/` folder, one per feature
- Completed: Move to `Archive/`

**Template category:** `Derivatives/Planning documentation/Market data/` làm template khi tạo category mới.

---

## File Routing — Auto-assign Output to Correct Folder

Khi tạo bất kỳ file nào, **xác định destination trước khi write**:

### Feature routing

| Feature / Domain | Document type | Destination |
|---|---|---|
| Derivatives (futures, VN30F, market data, order, account, cash) | Issue (BE/FE) | `Derivatives/Planning documentation/{Category}/Issues/` |
| Derivatives | Planning / PRD | `Derivatives/Planning documentation/{Category}/Planning/` |
| Derivatives | API Specification | `Derivatives/Planning documentation/{Category}/Specifications/` |
| Smart OTP | Any | `Smart-OTP/{Issues\|Planning\|Specifications}/` |
| eKYC | Any | `eKYC/{Issues\|Planning\|Specifications}/` |
| TradeX monitoring / infra | Any | `TradeX-Monitor/` |
| System knowledge / API standards | Reference | `Knowledge/TradeX/` |
| Market analysis / research | Reference | `Analytics/NH-Research/` |

### Derivatives category mapping

| Topic | Category folder |
|---|---|
| Market data, GTGD, chart, quote, price | `Market data/` |
| Order, lệnh, TP/SL, OCO, stop | `Order/` |
| Account, tài khoản, margin | `Account/` |
| Cash, tiền mặt, nạp rút | `Cash transaction/` |
| Mở tài khoản phái sinh, sub-account | `Open_DR_Sub_Account/` |
| Market (general market info, index) | `Market/` |

### _workspace/ rule

Temp staging cho orchestrator pipeline — bị overwrite mỗi lần pipeline chạy. Không lưu output vĩnh viễn vào đây.

---

## Quick Reference — Servers & Tools

**Environments:**

| Environment | URL |
|-------------|-----|
| Production | `https://nhsvpro.nhsv.vn` |
| UAT | `https://tnhsvpro.nhsv.vn` |

**Postman Collections:**

| Collection | UID | Role |
|------------|-----|------|
| TradeX API v2 (main) | `34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d` | API reference — không tạo request test |
| TradeX QA session | `34274942-8fe5bddd-fce2-4f76-bb6f-fb3f2760d40a` | Request test — folder theo issue (e.g., NHMTS-626) |

**Output directories:**
```
/Archive/_bmad-output/ba-artifacts/  ← API analysis, BRD, PM Knowledge
/QA/                                 ← Test docs, sync với Postman "TradeX QA session"
/Derivatives/Planning documentation/{Category}/  ← Specs, issues, planning
```

---

## Custom Slash Commands

| Command | Purpose |
|---------|---------|
| `/analyze-api` | 4-step API analysis: Identify → Trace Kafka → Find Consumer → Document |
| `/create-spec` | Tạo API specification theo chuẩn Derivatives |
| `/create-doc` | Tạo PM/BA documentation (no code, business logic) |
| `/create-fe-issue` | Tạo FE issue cho nhsv-mts-rn Derivatives |
| `/personal-tracking` | Weekly summary, capability update, work tracking |

