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

**Derivatives category mapping:**

| Topic | Category folder |
|---|---|
| Market data, GTGD, chart, quote | `Market data/` |
| Order, TP/SL, OCO, stop | `Order/` |
| Account, margin | `Account/` |
| Cash, nạp rút | `Cash transaction/` |
| Mở tài khoản phái sinh | `Open_DR_Sub_Account/` |

---

## Key References

@Knowledge/TradeX/API Standards/tradex-api-conventions.md
@Knowledge/TradeX/API Standards/tradex-api-spec-template.md

---

## Work Tracking Format

- Đơn vị: **quarter-based**
- Format: **Done / In Progress / Planned** + impact rõ ràng (metric hoặc kết quả cụ thể)
- Default khi không có KPI cụ thể: *"Released thành công"*

---

*Last updated: 2026-06* · *Repo: tradex-monitoring* · *Owner: Midu*
