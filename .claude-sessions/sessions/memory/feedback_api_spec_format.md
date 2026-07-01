---
name: feedback-api-spec-format
description: Chuẩn trình bày API Spec cho NHSV Pro — format cần follow khi viết/rewrite spec bất kỳ
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 8e446bb3-d061-49bf-8eb6-f1e758956642
---

Khi tạo hoặc reformat API Spec, follow đúng cấu trúc của `Derivatives/Planning documentation/Asset/Specifications/Asset_Info_API_Spec.md`.

**Why:** Midu yêu cầu "follow theo cách trình bày" của file reference đó — đây là chuẩn spec kỹ thuật của dự án.

**How to apply:** Áp dụng cho mọi file `*_Spec.md` hoặc `*_API_Spec.md` trong repo.

## Cấu trúc bắt buộc

### Document header (trước Section 1)
```
# [Feature] API Specification

**Document Type:** API Specification  
**Category:** [Domain — Sub-domain]  
**Version:** x.x  
**Date:** [Date]

> **Note:** [Integration type note — Lotte-integrated / TradeX-native / Vietstock proxy]
```

### Section 1: Overview
- **1.1 Purpose** — mô tả mục đích, business context
- **1.2 API Endpoints** — bảng Method + Endpoint (tất cả operations)
- **1.3 Response Format Standards** — JSON code block cho success + error, kèm principles (no `success` field, HTTP status rule)

### Section 2: Business Rules
- Enum mapping tables (exchange → code, type → code, direction, v.v.)
- Validation Rules table (Rule, Field, Error Code, Condition)
- Default Values table
- Language mapping nếu có (Accept-Language → Lotte lang_code)

### Section 3: API per Endpoint
- **3.1 Request** — Endpoint, Upstream, Auth, Default call example, Query Parameters table với cột Required dùng ✅/❌
- **3.2 Request Mapping** — TradeX Field | Type | Required | Upstream Field | Transform | Description
- **3.3 Response Mapping** — Upstream Field | TradeX Field | Type | Transform | Mô tả
- **3.4 Error Mapping** — tách theo source (Validation / Auth / Business)

### Section 4: Error Handling Summary
- **4.1 Error Response Format** — JSON code blocks cho từng loại (400, 401/403, 422, 500, empty 200)
- **4.2 Error Code Patterns** — bảng Error Source | Code Pattern | Example | HTTP

### Section 5: UI/UX Behavior (nếu là Feature Spec)
- Layout, Component, Interactions (table), Data→UI Mapping (table), Acceptance Criteria (checkbox list)

### Section 6: Edge Cases
- Bảng Case | Behavior

### Section 7: Implementation Notes
- **7.1 Service Architecture** — bảng Component | Role
- **7.2 Key Principles** — numbered list: validation strategy, data passthrough, caching, auto-population

## Điều không được làm
- KHÔNG dùng "User Story" (as a ... I want ... so that) trong technical spec — đây là PM artifact, không phải spec format
- KHÔNG để bảng thiếu cột Type cho request/response params
- KHÔNG dùng HTTP status text inline (dùng table riêng)
- KHÔNG thiếu JSON example code block trong §1.3 và §4.1

## Key patterns từ reference
- Required column dùng ✅ / ❌ (không phải "Yes/No" hay "Y/N")
- Enum mapping → Section 2 riêng, không lồng vào request params
- Response Mapping table luôn có cột Transform (Direct / Rename / Enum map / camelCase)
- Footer: `**Document Status:** ✅/📋/🔄 | **For:** [audience] | **Next Steps:** [action]`
