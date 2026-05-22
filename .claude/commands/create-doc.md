# Create PM/BA Documentation (Derivatives)

Tạo tài liệu PM/BA-friendly cho Derivatives. Focus: business logic & data flow, KHÔNG phải implementation code.

## Core Principle

**PM/BA documentation = Business context + Data flow + High-level architecture**
**NOT:** code blocks, class names, method signatures, DB query syntax

## Folder Routing

Xác định loại document cần tạo:

| Folder | Audience | Code OK? |
|--------|----------|----------|
| `Planning/` | PM reads | ❌ NO |
| `Specifications/` | Developers read | ✅ YES |
| `Issues/` | Both (split sections) | Executive Summary: ❌ / Technical: ✅ |
| `README.md` | PM entry point | ❌ NO |

**Base path:** `Derivatives/Planning documentation/{Category}/`

## Planning/ Documents

### 01_Integration_Plan.md
- Phases & milestones
- Dependencies
- Timeline
- Services affected (high-level names only)

### 02_Business_Requirements.md
- User stories (`As a trader, I want to... So that...`)
- Business value
- Scope (in/out)
- Success metrics

### 03_Technical_Requirements.md (HIGH-LEVEL ONLY)
- Services affected (names, not code)
- APIs needed (abstract)
- Data storage (abstract: "Store in MongoDB", not schema)
- Performance requirements

### 04_Use_Cases_Testing.md
- User scenarios
- Test cases (input/output)
- Expected behavior
- Edge cases

## Issues/ Template

```markdown
# {Feature} Implementation

## 📋 Executive Summary (PM READS THIS)

### Problem Statement
{Business problem in plain language}

### Current vs Target
{What works now vs what should work}

### Solution Approach (HIGH-LEVEL)
1. Collect data from X
2. Store in Y
3. API returns Z

### Timeline
{Phases and estimates}

### Success Criteria
- [ ] Feature works for {use case}
- [ ] Response time < {target}
- [ ] No regression on {existing feature}

---

## 🔍 Technical Background (PM CAN SKIP)

{Implementation details for developers}

---

## 📝 Detailed Requirements (PM CAN SKIP)

{Technical requirements — code OK here}
```

## Data Flow Diagram Format (for PM)

```
Service A → Service B → Storage → Client
```
Or sequence:
```
Client → /api/endpoint → rest-proxy → Kafka → order-v2 → Lotte
```

## Review Checklist

Before finalizing:
- [ ] No code blocks in Planning/ folder
- [ ] No implementation details in README.md
- [ ] Data flow explained with diagrams, not code
- [ ] Business value clearly stated
- [ ] Acceptance criteria understandable by PM
- [ ] Issues/ have Executive Summary (no code in first section)
- [ ] Document footer: **Document Status:** | **For:** | **Next Steps:**
