# Glossary — tradex-monitoring

> Full decoder ring. CLAUDE.md giữ hot cache (~30 terms phổ biến nhất).
> File này giữ tất cả mọi thứ — không giới hạn dòng.

---

## Acronyms & Viết tắt

| Term | Meaning | Context |
|---|---|---|
| **NHSV** | NH Securities Vietnam | Công ty — owner của NHSV Pro |
| **NHSV Pro** | Mobile trading app cho retail investors | Sản phẩm chính |
| **NHMTS** | Jira project key | Mọi Jira issue đều prefix NHMTS-xxx |
| **TradeX** | Backend trading API system | Core API layer của NHSV Pro |
| **PO** | Product Owner | Midu |
| **PM** | Product Manager | Midu |
| **FE** | Frontend | Mobile app (React Native hoặc tương đương) |
| **BE** | Backend | API / service layer |
| **PRD** | Product Requirements Document | Spec chính thức |
| **UAT** | User Acceptance Testing | Test với end-user trước go-live |
| **SIT** | System Integration Testing | Test tích hợp giữa các service |
| **API** | Application Programming Interface | — |
| **DTO** | Data Transfer Object | Object dùng trong API request/response |
| **BRD** | Business Requirements Document | — |
| **CR** | Change Request | — |
| **P0/P1/P2** | Priority levels | P0 = critical / drop everything |
| **OTP** | One-Time Password | — |
| **KYC** | Know Your Customer | Quy trình xác minh định danh |
| **eKYC** | Electronic KYC | Tích hợp VNPT eKYC |
| **MFA** | Multi-Factor Authentication | — |
| **SSO** | Single Sign-On | — |

---

## Hệ thống & Tích hợp

| Term | Meaning | Ghi chú |
|---|---|---|
| **Core / Core Lotte** | Hệ thống core nghiệp vụ của Lotte | Third-party backend — không được đặt tên TradeX API theo Core field names |
| **lotte-bridge** | Service bridge TradeX ↔ Core Lotte | API routes qua đây |
| **tradex-monitoring** | Repo chính — monitoring + PM workspace | Nơi Claude Code hoạt động |
| **midu-path** | Personal second-brain repo | `Knowledge/` + `Career/` folders; Vietnamese content |
| **Sentry** | Error tracking & monitoring | Project: nhsv-prod |
| **Figma** | Design tool | Mọi UI/UX spec |
| **Jira** | Project management | Project key: NHMTS |
| **Postman** | API testing | Collection: "TradeX QA session" (test) · "TradeX API v2" (reference) |
| **Prometheus** | Metrics collection | Phần của TradeX Stability Monitor |
| **Grafana** | Dashboard metrics | — |
| **Loki** | Log aggregation | — |
| **AlertManager** | Alert routing | — |
| **VNPT eKYC** | eKYC provider | Tích hợp cho account opening flow |
| **FPT eContract** | eContract provider | Electronic contract signing |
| **VSD** | Vietnam Securities Depository | Lưu ký chứng khoán Việt Nam |
| **OpenFang** | AI agent component | Phần của NHSV Multi Agents proposal |

---

## Sản phẩm & Features

| Term | Meaning | Status |
|---|---|---|
| **Derivatives / Phái sinh** | Derivatives trading — hợp đồng tương lai | 🔄 Go-live milestone — NHMTS-682 |
| **Smart OTP** | Multi-channel OTP (SMS + app) trong NHSV Pro | 🔄 Pending 3rd party handoff |
| **Open Derivatives Sub Account Online** | Mở tiểu khoản phái sinh online | Sub-feature của Derivatives epic |
| **TradeX Stability Monitor** | Monitoring layer Prometheus + Grafana + Loki | Proposed — NHMTS-642 |
| **NHSV Multi Agents** | Internal AI agent layer | Proposed — chưa production |
| **Finlens** | Personal AI agent phân tích stock từ screenshot | Personal project — midu-path |

---

## Thị trường chứng khoán Việt Nam

| Term | Meaning |
|---|---|
| **HOSE** | Sở Giao dịch Chứng khoán TP. Hồ Chí Minh |
| **HNX** | Sở Giao dịch Chứng khoán Hà Nội |
| **UPCOM** | Unlisted Public Company Market |
| **VN-Index** | Chỉ số thị trường HOSE |
| **HNX-Index** | Chỉ số thị trường HNX |
| **VN30** | Rổ 30 cổ phiếu vốn hóa lớn nhất HOSE |
| **T+2 / T+3** | Settlement cycle — thanh toán sau 2/3 ngày giao dịch |
| **Room nước ngoài** | Foreign ownership limit |
| **Margin** | Giao dịch ký quỹ |
| **Phái sinh** | Derivatives (hợp đồng tương lai chỉ số VN30F) |
| **VN30F** | Hợp đồng tương lai chỉ số VN30 |
| **Lệnh ATO / ATC** | At-The-Open / At-The-Close orders |
| **Lệnh LO** | Limit Order |
| **Lệnh MP** | Market Order (HNX) |
| **Biên độ dao động** | Price fluctuation limit (±7% HOSE, ±10% HNX) |

---

## TradeX API Patterns

| Term | Meaning |
|---|---|
| **TradeX → Core mapping** | Bảng ánh xạ TradeX field names → Core (Lotte) field names — bắt buộc trong mọi spec |
| **tradex-api-naming** | Rule file định nghĩa chuẩn đặt tên endpoint, DTO, service method |
| **tradex-order-api-response-standards** | Chuẩn response format cho Order API |
| **captureFailAPIToSentry** | Pattern ghi lỗi API vào Sentry trong NHSV Pro |
| **HTTP 404 — Lotte equity** | Known issue: endpoint Lotte equity account info (release 1.7.6+) |
| **release 1.7.6** | NHSV Pro release có lỗi captureFailAPIToSentry — Lotte equity account |
| **release 1.7.4** | Stable release — library upgrade regression risk đã test (VNPT eKYC + FPT eContract) |

---

## Jira Workflow

| Term | Meaning |
|---|---|
| **Backlog** | Chưa vào sprint |
| **In Progress** | Đang làm |
| **In Review** | Đang review / testing |
| **Done** | Hoàn thành |
| **Story** | User story — feature unit |
| **Task** | Technical task |
| **Bug** | Lỗi cần fix |
| **Epic** | Nhóm story lớn (vd: Derivatives epic = NHMTS-682) |
| **Sub-task** | Task con của Story/Task |

---

## Document Conventions

| Term | Meaning |
|---|---|
| **Planning/** | Folder chứa PRD, BRD, roadmap — NO code blocks |
| **Specs/** | Folder chứa API spec, technical spec |
| **Issues/** | Folder chứa Jira issue drafts |
| **Archive/** | Folder chứa tài liệu cũ/deprecated |
| **PascalCase_Underscore** | Naming convention cho Derivatives docs (vd: `Chart_API_Spec.md`) |
| **ATX headers** | `#` style headers — không dùng `===` hay `---` underline style |
| **Document footer** | `Document Status: ✅/📋/🔄 \| For: [audience] \| Next Steps: [action]` |
| **✅** | Document status: Done / Approved |
| **📋** | Document status: Draft / Pending review |
| **🔄** | Document status: In Progress / Being updated |

---

## Shorthand hàng ngày

| Shorthand | Meaning |
|---|---|
| **derivatives** | Derivatives epic — NHMTS-682 |
| **smart otp / sotp** | Smart OTP feature |
| **lotte** | Core Lotte / lotte-bridge integration |
| **tradex** | TradeX API system (không phải app) |
| **pro** | NHSV Pro app |
| **monitoring** | tradex-monitoring repo |
| **midu-path** | Personal second-brain repo |
| **finlens** | Personal stock analysis AI agent |
| **the bridge** | lotte-bridge service |

---

*Last updated: 2026-06* · *Repo: tradex-monitoring* · *Owner: Midu*
*→ Hot cache: `../CLAUDE.md`*
