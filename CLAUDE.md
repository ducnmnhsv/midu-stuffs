# CLAUDE.md — tradex-monitoring

> Hot cache cho Claude Code, Claude Chat, và Cowork.
> Khi cần thêm chi tiết → xem `Knowledge/TradeX/` hoặc `Derivatives/Planning documentation/`.

---

## Me

**Midu** (Nguyễn Minh Đức) — Product Owner / PM tại **NHSV** (NH Securities Vietnam).
Sản phẩm chính: **NHSV Pro** — mobile trading app cho retail investors.
Daily stack: Figma · Jira (NHMTS) · Sentry · tradex-monitoring repo · Claude.

---

## Active Projects

| Codename | Jira | Status | Ghi chú |
|---|---|---|---|
| **Derivatives** | NHMTS-682 | 🔄 In Progress | Go-live milestone; docs ở `Derivatives/Planning documentation/` |
| **Smart OTP** | — | 🔄 Pending handoff | Multi-channel OTP; design done, chờ 3rd party integration |
| **Finlens** | — | 📋 Personal | AI agent phân tích stock từ screenshot (midu-path repo) |

---

## Terms & Acronyms

| Term | Meaning |
|---|---|
| **NHSV** | NH Securities Vietnam |
| **NHSV Pro** | Mobile trading app (sản phẩm chính) |
| **TradeX** | Backend trading API system |
| **NHMTS** | Jira project key của NHSV |
| **Core / Core Lotte** | Hệ thống core của Lotte (third-party backend) |
| **lotte-bridge** | Service bridge kết nối TradeX ↔ Core Lotte |
| **Derivatives / Phái sinh** | Derivatives trading — hợp đồng tương lai |
| **Smart OTP** | Multi-channel OTP feature trong NHSV Pro |
| **eKYC** | Electronic KYC — tích hợp VNPT |
| **eContract** | Electronic contract — tích hợp FPT |
| **VSD** | Vietnam Securities Depository |
| **FE / BE** | Frontend / Backend |
| **PRD** | Product Requirements Document |
| **PO** | Product Owner (Midu) |

---

## ⚠️ CRITICAL — nhsv-mts-rn (READ-ONLY)

`/Users/ducnguyen/Documents/project/nhsv-mts-rn` là FE codebase của **NHSV Pro** (React Native).

- **TUYỆT ĐỐI KHÔNG** chỉnh sửa, tạo file, hoặc thay đổi bất kỳ thứ gì trong repo này
- Chỉ dùng để **đọc và tham chiếu** (read-only reference)
- Khi tạo FE issue: tham chiếu `src/screens/`, `src/components/`, `src/reduxs/`, `src/interfaces/` → viết issue vào `tradex-monitoring`

---

## Rules — áp dụng mọi lúc

### API & Naming
- **C1 — TradeX naming:** Tất cả endpoint, DTO, service method → check `Knowledge/TradeX/API Standards/tradex-api-conventions.md` trước khi finalize.
- **C1 — No Core names:** Request/query params phải dùng tên TradeX, KHÔNG dùng tên field của Core (Lotte). Luôn kèm bảng TradeX → Core mapping.
- **C2 — Order API response:** Check `Knowledge/TradeX/API Standards/tradex-api-conventions.md` (section: Response Format Standards) trước khi finalize bất kỳ Order API response doc nào.

### Documentation
- **C3 — PM-readability gate:** Folder `Planning/` → NO code blocks, prose only.
- **C4 — Knowledge-first:** Trước khi scan codebase cho TradeX system knowledge → check `Knowledge/TradeX/` trước.
- **C5 — Derivatives doc footer:** Mọi spec/issue Derivatives phải kết thúc bằng:
  ```
  Document Status: ✅/📋/🔄 | For: [audience] | Next Steps: [action]
  ```
- **Markdown standard:** CommonMark strict · ATX headers (`#`) · fenced code blocks có language ID · reference file bằng `@filename`.
- **Derivatives doc naming:** PascalCase + underscore (vd: `Chart_API_Spec.md`) · `README.md` là entry point · không dùng brackets `[ISSUE]` hay prefix đặc biệt.
- **Folder structure:** `Planning/` (no code) · `Specs/` · `Issues/` · `Archive/` — tách biệt rõ ràng.

### TT134 Compliance — Overlap Prevention
- **C6 — Overlap check trước:** Khi thảo luận bất kỳ issue/spec/task TT134, đọc `TT134 - UBCK/README.md` Section 7 "Cross-issue Implementation Rules" trước. Tránh tạo duplicate middleware, duplicate migration, hoặc task đã merged.
- **C6 — STT14a closed:** STT14a (Rút tiền <10M) đã merged vào STT5 Session Auth. Không assign developer riêng, không tạo implementation task mới.
- **C6 — 1 DB migration:** STT5 + STT35 phải dùng 1 script migration duy nhất cho `t_order_log` + `t_withdrawal_log`. Không tách 2 PR.
- **C6 — 1 logging middleware:** BE-SA-3 (STT5) + BE-1 (STT35) = cùng middleware. BE-SA-4 (STT5) + BE-2 (STT35) = cùng middleware. Không tạo 2 middleware riêng.
- **C6 — BE-SA-1 merge:** STT5 BE-SA-1 = Smart OTP Login 07_BE_Task Task 4. Coordinate với Smart OTP team, merge vào cùng 1 PR.
- **C6 — Unblocking sequence:** Không viết spec chi tiết Audit Log / Alert System / Session Management (P1) trước khi STT5 + STT35 Phase 3 xong. Xem Section 7.5 trong README.
- **C6 — Checklist:** Mỗi issue TT134 mới → chạy qua checklist 7.6 trong `TT134 - UBCK/README.md`.

### Task Tracking — Single Source
- **C7 — Tracking/tasks.js là nguồn duy nhất** cho status/deadline/priority của mọi task (Derivatives, TT134, Smart OTP, NHSV Pro, eKYC, TradeX-Monitor). Khi status đổi → chỉ sửa `Tracking/tasks.js`. Board: mở `Tracking/kanban.html`.
- **C7 — README không duplicate status:** README các khu vực chỉ mô tả scope/dependency + link tới board. Không maintain bảng status riêng (kể cả `TT134_Kanban.html` — đã deprecated).
- **C7 — Task mới:** Tạo doc theo File Routing bên dưới + thêm 1 entry vào `Tracking/tasks.js` (id theo format `{AREA}-{FEATURE}-{seq}`, schema xem `Tracking/README.md`).

### Tooling
- **Analytics MCP:** Chỉ dùng NHSV Pro property (`properties/478227972`, account `accounts/345830035`). Không reference property khác (BES, NHSV BES).
- **Postman:** Test requests → chỉ tạo trong collection **"TradeX QA session"**. Collection **"TradeX API v2"** là reference only — không tạo test request ở đây.
- **No auto-commit/push:** Suggest content + path; Midu review và commit thủ công.

### Writing style
- Mix Vietnamese/English: technical terms giữ tiếng Anh.
- Dùng bảng, diagram, ví dụ thực tế.
- Kèm business interpretation bên cạnh technical detail.
- Tối đa 1–2 câu hỏi clarifying, bullet format.

---

## File Routing — Output đến đúng folder

| Feature / Domain | Document type | Destination |
|---|---|---|
| Derivatives | Issue (BE/FE) | `Derivatives/Planning documentation/{Category}/Issues/` |
| Derivatives | Planning / PRD | `Derivatives/Planning documentation/{Category}/Planning/` |
| Derivatives | API Specification | `Derivatives/Planning documentation/{Category}/Specifications/` |
| Smart OTP | Any | `Smart-OTP/{Issues\|Planning\|Specifications}/` |
| TradeX monitoring / infra | Any | `TradeX-Monitor/` |
| System knowledge / API standards | Reference | `Knowledge/TradeX/` |
| Firebase / GA4 analytics reports | Reference | `Analytics/NHSV-Pro/` |
| NHSV Pro new features (specs, issues, PRDs) | Any | `New feature in NHSV Pro/{FeatureArea}/` |
| Task tracking / kanban / status | Data + board | `Tracking/` (single source: `tasks.js`) |

**Derivatives category mapping:**

| Topic | Category folder |
|---|---|
| Market data, GTGD, chart, quote | `Market data/` |
| Order, TP/SL, OCO, stop | `Order/` |
| Account, margin | `Account/` |
| Asset, PnL, position | `Asset/` |
| Cash, nạp rút | `Cash transaction/` |
| Mở tài khoản phái sinh | `Open_DR_Sub_Account/` |

> Không còn folder `Derivatives/FE Implementation/` — FE issues nằm trong `{Category}/Issues/` cùng BE issues. Chuẩn issue: `Derivatives/Planning documentation/ISSUE_STANDARD.md`.

---

## TradeX Services — hot cache (tránh scan lại code)

> Snapshot source đồng bộ tại `Knowledge/TradeX-MCP/` (verified 2026-06-30). Kafka topic = `clusterId` / `spring.application.name` lấy từ config. **Canonical đầy đủ:** `Knowledge/TradeX/_index.md` (Core Services).

| Service (folder TradeX-MCP) | Tech | Kafka Topic | Role |
|---|---|---|---|
| `rest-proxy` | Node.js | `rest-proxy` (route theo scope) | API Gateway |
| `aaa` | Node.js | `aaa` | Auth, OTP, Smart OTP |
| `lotte-bridge` | Node.js | `lotte-bridge` | Lotte: order/account/balance/transfer/change-broker/smart-OTP |
| `lotte-ws-bridge` | Node.js | `lotte-ws-bridge` | Notification (gồm Derivatives margin/VM/position) |
| `market-query-v2` | Node.js | `market-v2` | Market data queries (pkg `tradex-market`) |
| `ws-v2` | Node.js | — (WebSocket) | WS server cho client |
| `configuration` | Node.js | `configuration` | Config service |
| `order-v2` | Java 17 / Spring Boot 3.1 | `order` | Conditional orders: Stop/OCO/Trailing/Bull-Bear |
| `realtime-v2` | Java | `realtime-v2` | Real-time + EOD snapshot/backfill |
| `market-collector-lotte` | Java | `market-collector-lotte` | Collect market data từ Lotte WS |
| `notification` | Java | `notification` | Push/SMS/email + templates |
| `ekyc-admin` | Java (JHipster) | `ekyc-admin` | eKYC admin: Lotte upload + FPT eContract + OTP |
| `nhsv-admin` | Java (JHipster) | `nhsv-admin` | Admin web tool |
| `tradex-common-java` | Java lib | — | Shared models/enums/kafka utils |
| `documents` | docs only | — | API specs / BA / QA / DB docs |

⚠️ Topic cũ hay nhầm: `lotte-bridge` KHÔNG phải `paave-real-trading`; `market-query-v2` topic là `market-v2` (không phải `market-query-v2`); `order-v2` topic là `order`.

**Order domain:**
- Lệnh thường (Buy/Sell/Cancel/Modify) → `lotte-bridge` (equity) / `rest-proxy→tuxedo` (derivatives) → spec `Knowledge/TradeX/Planning/regular-order-api-mapping.md`
- Lệnh điều kiện (Stop/OCO/Trailing/Bull-Bear) → service `order-v2` (tradex-native, response `{id}`) → spec `Knowledge/TradeX/Planning/conditional-order-api-mapping.md`

---

## Key References

@Knowledge/TradeX/API Standards/tradex-api-conventions.md
@Knowledge/TradeX/API Standards/tradex-api-spec-template.md
@Knowledge/TradeX/Planning/regular-order-api-mapping.md
@Knowledge/TradeX/Planning/conditional-order-api-mapping.md
@Knowledge/TradeX/Diagram-Standards/BA_Diagram_Selection_Guide.md

---

## Work Tracking Format

- Đơn vị: **quarter-based**
- Format: **Done / In Progress / Planned** + impact rõ ràng (metric hoặc kết quả cụ thể)
- Default khi không có KPI cụ thể: *"Released thành công"*

---

*Last updated: 2026-06-30* · *Repo: tradex-monitoring* · *Owner: Midu* · *Knowledge/TradeX-MCP synced từ source mới nhất 2026-06-30*
