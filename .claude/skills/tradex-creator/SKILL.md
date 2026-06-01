---
name: tradex-creator
description: "TradeX 문서 작성 전문 스킬 — API Specification, PM/BA 문서, FE Issue 생성. 'create spec', 'API spec 만들어', 'spec 작성', 'create doc', '문서 작성', 'create issue', 'FE issue', 'Derivatives 문서', 'Derivatives spec', 'Derivatives issue' 요청 시 반드시 이 스킬을 사용. tradex-orchestrator Phase 2에서 자동 호출."
---

# tradex-creator Skill

Chọn đúng template và đường dẫn theo loại yêu cầu. Luôn đọc `_workspace/01_analyst_findings.md` trước nếu có.

## Phân loại yêu cầu

| Từ khóa | Loại output | Thư mục |
|---------|------------|---------|
| spec, specification, API spec | API Spec | `Specifications/` |
| doc, tài liệu, planning | PM Doc | `Planning/` hoặc `Issues/` |
| issue (BE/chung) | BE Issue | `Issues/` |
| FE issue, frontend issue | FE Issue | `Issues/` |

**Đường dẫn gốc:** `Derivatives/Planning documentation/{Category}/`

Category: `Order management/`, `Market data/`, `Account management/`, `eKYC/`, hoặc tạo mới.

## Template API Spec

```markdown
# {Tính năng} API Specification

## Tổng quan
- **Endpoint:** [METHOD] /api/v1/{resource}
- **Phiên bản:** v1
- **Mục đích:** {1-2 câu}

## Xác thực
- **Loại:** JWT Bearer
- **Scope:** {scope}

## Request

### Headers
| Header | Giá trị | Bắt buộc |
|--------|---------|----------|
| Authorization | Bearer {token} | ✅ |
| Accept-Language | vi \| en \| ko | ❌ |
| Content-Type | application/json | POST/PUT |

### Request Body / Parameters
| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|

## Response

### Thành công (200)
\`\`\`json
{}
\`\`\`

### Lỗi validation (400)
\`\`\`json
{ "code": "INVALID_PARAMETER", "params": [{ "code": "FIELD_IS_REQUIRED", "param": "fieldName" }] }
\`\`\`

### Lỗi nghiệp vụ (422)
\`\`\`json
{ "code": "ERROR_CODE", "message": "..." }
\`\`\`

## Trường tự động điền
| TradeX Field | Nguồn | Lotte Field |
|--------------|-------|-------------|
| sourceIp | Request IP | cli_ip_addr |
| userId | JWT Token | user_id |

## Business Rules
1.

## Mapping sang Lotte
| TradeX | Lotte | Giá trị |
|--------|-------|---------|

---
**Document Status:** 📋 Draft
**For:** BE Developer / FE Developer
**Next Steps:**
```

## Quy tắc tài liệu PM/BA (Planning/)

Tuyệt đối không viết vào Planning/:
- Code block (``` 3 backtick)
- Tên class, method signature
- DB query, JSON schema chi tiết

Dữ liệu flow dùng dạng:
```
Client → /api/v1/... → rest-proxy → Kafka → service → Lotte
```

## Template Issues/

```markdown
# {Tính năng} Implementation

## 📋 Tóm tắt (PM ĐỌC PHẦN NÀY)

### Vấn đề
### Hiện tại vs Mục tiêu
### Hướng giải quyết (TỔNG QUAN)
### Timeline
### Tiêu chí thành công
- [ ]

---

## 🔍 Chi tiết kỹ thuật (PM CÓ THỂ BỎ QUA)

## 📝 Yêu cầu chi tiết (PM CÓ THỂ BỎ QUA)

---
**Document Status:** 📋 Draft
**For:**
**Next Steps:**
```

## Template FE Issue

Trước khi viết: đọc READ-ONLY `/Users/ducnguyen/Documents/project/nhsv-mts-rn` để tìm file bị ảnh hưởng.

```markdown
# [FE] {Tên tính năng} — Derivatives

## 📋 Tóm tắt (PM ĐỌC PHẦN NÀY)
### Vấn đề
### Ảnh hưởng
### Hướng giải quyết (TỔNG QUAN)

---

## 🔍 Chi tiết kỹ thuật

### Màn hình/Component bị ảnh hưởng
- `src/screens/{path}` — {thay đổi gì}
- `src/components/{path}` — {thay đổi gì}

### Thay đổi API cần thiết
| Endpoint | Thay đổi |
|----------|---------|

### Tiêu chí chấp nhận
- [ ]

### Ghi chú cho Developer

---
**Document Status:** 📋 Draft
**For:** FE Developer
**Next Steps:** Review với FE team
```

## Định dạng output

1. `_workspace/02_creator_draft.md` — bản nháp
2. `_workspace/02_creator_meta.md` — loại output, đường dẫn đích, lưu ý cho validator
