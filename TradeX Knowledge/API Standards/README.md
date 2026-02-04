# API Standards

> **What:** TradeX API conventions & templates  
> **Scope:** ALL TradeX APIs (Equity, Derivatives, Account, Portfolio, etc.)  
> **Status:** ✅ Standards - No changes unless system-wide update needed

---

## Purpose

**Centralized API standards** for all TradeX projects. These conventions are:
- ✅ **Immutable** (unless system-wide decision to change)
- ✅ **Universal** (apply to ALL APIs)
- ✅ **Mandatory** (must follow when creating new APIs)

---

## Documents

| Document | Type | Description | Lines |
|----------|------|-------------|-------|
| [tradex-api-conventions.md](./tradex-api-conventions.md) | **Complete Guide** | Full standards, examples, patterns + How-to guide | ~750 |
| [tradex-api-spec-template.md](./tradex-api-spec-template.md) | **Template** | Copy for new API specs | ~370 |

**Why only 2 files?**
- ✅ **Single source of truth** - All conventions in one place
- ✅ **No duplication** - Merged quick ref + how-to into main guide
- ✅ **Save quota** - Fewer files to read
- ✅ **Easier to maintain** - Update once, not 3-4 times

---

## Key Standards

### 1. Auto-Populated Fields

Fields backend tự động điền (FE không cần truyền):

| TradeX Field | Lotte Field | Source |
|--------------|-------------|--------|
| `sourceIp` | `cli_ip_addr` | Request IP |
| `userId` | `user_id` | JWT Token |
| `name` | `hts_user_nm` | JWT userData |
| *(language)* | `lang_code` | Accept-Language |

**Exception:** `deviceUniqueId` → FE MUST send (device-specific)

### 2. Error Formats

**Validation Error (400):**
```json
{
  "code": "INVALID_PARAMETER",
  "params": [
    {
      "code": "FIELD_IS_REQUIRED",
      "param": "accountNumber"
    }
  ]
}
```

**Pass-Through Lotte Error (422):**
```json
{
  "code": "{OPERATION}_{LOTTE_ERROR_CODE}",
  "message": "[CODE] Lotte message"
}
```

**Success:**
- Lotte `error_code: "0000"` = Success
- TradeX `code: "0000"`, `success: true`

### 3. Language Mapping

| Accept-Language | Lotte lang_code |
|-----------------|-----------------|
| `vi` | `V` |
| `en` | `E` |
| `ko` | `K` |

---

## Usage Workflow

### For New API Spec

```
Step 1: Read standards
└─> tradex-api-conventions.md (understand patterns + how-to)

Step 2: Copy template
└─> cp tradex-api-spec-template.md [Project]/[Feature]_API_Spec.md

Step 3: Fill template
└─> Follow conventions + how-to guide (in same doc)
```

### For Quick Lookup

Use `api-conventions.md` when you just need to:
- Check auto-populated fields list
- Verify error format
- Look up language codes
- See HTTP status codes

---

## When to Update Standards

**Reasons to update:**
- ✅ System-wide architectural decision
- ✅ New TradeX-wide requirement
- ✅ Core (Lotte) changes affecting all APIs
- ✅ Security/compliance requirements

**NOT reasons to update:**
- ❌ Project-specific needs → Handle in project
- ❌ One-off cases → Document as exception
- ❌ Preference → Standards > preferences

**Process:**
1. Discuss with architecture team
2. Update all 4 standard documents
3. Communicate to all projects
4. Create migration guide if needed

---

## Document Relationships

```
tradex-api-conventions.md
    ↓ (provides standards + how-to)
tradex-api-spec-template.md
    ↑ (guided by)
tradex-api-conventions.md
```

**Simple Flow:**
1. Read: `tradex-api-conventions.md` (includes how-to)
2. Copy: `tradex-api-spec-template.md`
3. Done!

---

## Projects Using These Standards

- ✅ **Derivatives** - Order APIs (Buy, Sell, Cancel, Modify)
- 📋 **Equity** - Future
- 📋 **Account** - Future
- 📋 **Portfolio** - Future
- 📋 **All future APIs** - Mandatory

---

**Last Updated:** 2026-02-04  
**Maintainer:** Architecture Team, BA/PM Team  
**Change Approval:** Required for all updates
