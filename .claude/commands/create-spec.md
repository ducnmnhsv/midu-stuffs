# Create API Specification (Derivatives)

Tạo API specification chuẩn cho Derivatives theo TradeX conventions.

## Activation Sequence

1. **Naming check** — URL, DTO, service method theo convention
2. **Document structure** — Dùng template chuẩn Derivatives
3. **Order API?** — Nếu có liên quan order: apply tradex-order-api-response-standards
4. **Error formats** — Auto-populated fields, error codes, language mapping

## Output Requirements

### URL Convention
- Pattern: `/api/v1/{resource}` hoặc `/api/v1/{resource}/{sub-resource}`
- Derivatives: `/api/v1/derivatives/{resource}`

### DTO Naming
- Request: `{Resource}{Action}Request`
- Response: `{Resource}{Action}Response`
- Ví dụ: `DerivativeOrderPlaceRequest`, `DerivativeOrderPlaceResponse`

### Required Sections

```markdown
# {Feature} API Specification

## Overview
- Endpoint, method, version
- Purpose (1-2 câu)

## Authentication
- JWT Bearer, scope required

## Request
### Headers
### Path Parameters (nếu có)
### Query Parameters (nếu có)
### Request Body (nếu có)

## Response
### Success (200)
### Validation Error (400)
### Unauthorized (401)
### Forbidden (403)
### Business Error (422)

## Auto-Populated Fields
(Fields từ JWT/IP — FE không cần truyền)

## Business Rules
(Logic, edge cases)

## Mapping to Core (Lotte)
(Bảng mapping TradeX → Lotte field names/values)

---
**Document Status:** 📋 Draft
**For:** BE Developer / FE Developer
**Next Steps:** [action]
```

## Auto-Populated Fields (Always include if applicable)

| TradeX Field | Source | Lotte Field |
|--------------|--------|-------------|
| `sourceIp` | Request IP | `cli_ip_addr` |
| `userId` | JWT Token | `user_id` |
| `name` | JWT Token | `hts_user_nm` |
| `identifierNumber` | JWT Token | `idno` |

## Language Mapping (for Lotte-integrated APIs)

| Accept-Language | Lotte lang_code |
|-----------------|-----------------|
| `vi` | `V` |
| `en` | `E` |
| `ko` | `K` |

## Template Reference
`TradeX Knowledge/API Standards/tradex-api-spec-template.md`
