# Planning & Future Features

> **What:** Knowledge about features in planning/design phase  
> **Status:** 📋 Planning - Not yet implemented  
> **Audience:** PM, BA

---

## Purpose

Documents về features **đang trong giai đoạn planning**, chưa được implement.

**Characteristics:**
- 📋 Not yet in production
- 🔄 May change during implementation
- 🎯 Used for planning & requirements
- ⚠️ NOT operational documentation

---

## Documents

| Document | Category | Status | Description |
|----------|----------|--------|-------------|
| [regular-order-api-mapping.md](./regular-order-api-mapping.md) | Orders | 📋 Planning | TradeX ↔ Lotte mapping for regular orders |

---

## What Belongs Here?

### ✅ DO Include

- **API mappings** being planned (not yet implemented)
- **Feature analysis** for upcoming features
- **System design** proposals
- **Integration plans** with new services
- **RFCs** (Request for Comments)

### ❌ DON'T Include

- **Implemented features** → Goes to `../System/`
- **API conventions** → Goes to `../API Standards/`
- **Project-specific planning** → Goes to project folders (e.g., `Derivatives/Planning documentation/`)

---

## Planning vs Implementation

### Planning Documents (Here)

**Characteristics:**
- General knowledge applicable across projects
- High-level API patterns
- Integration approaches
- Not tied to specific project timeline

**Example:** `regular-order-api-mapping.md`
- General pattern for order APIs
- Used by multiple projects (Derivatives, Equity)
- Not project-specific implementation

### Project Planning (Project Folders)

**Characteristics:**
- Specific to one project (Derivatives, Equity, etc.)
- Detailed requirements & user stories
- Implementation timeline & milestones
- Project-specific decisions

**Example:** `Derivatives/Planning documentation/Order/`
- Derivatives-specific order implementation
- Business requirements for derivatives orders
- Technical specifications for derivatives
- Project timeline & milestones

---

## Document Lifecycle

```
📋 Planning (This folder)
    ↓
    [Decision to implement]
    ↓
🚧 Project Planning (Project folders)
    ↓
    [Implementation]
    ↓
✅ System Knowledge (../System/)
```

**When to move:**
- Planning → Project: When project approved for implementation
- Project → System: When feature goes live in production

---

## Document Template

```markdown
# [Feature Name] - Planning

> **Status:** 📋 Planning | 🚧 In Development | ✅ Implemented  
> **Target Projects:** [Derivatives, Equity, etc.]  
> **Last Updated:** YYYY-MM-DD

## Overview
[What feature does, why needed]

## Proposed Approach
[How to implement]

## API Mapping
[TradeX ↔ Lotte field mappings]

## Open Questions
[Decisions needed]

## Next Steps
[What happens next]
```

---

## Migration Guide

### When Feature is Approved for Project

1. **Create detailed spec** in project folder
   - `[Project]/Planning documentation/`
   - Follow API Standards templates
   - Add project-specific requirements

2. **Keep planning doc** here as reference
   - Mark as "In Progress" in specific project
   - Update with link to project implementation

3. **Update regularly** as implementation progresses

### When Feature Goes Live

1. **Move to System Knowledge**
   - Create operational doc in `../System/`
   - Document actual implementation
   - Include service names, endpoints, data flows

2. **Archive planning doc**
   - Keep for historical reference
   - Mark as "Implemented, see System/[doc]"

---

## Naming Convention

**Planning Docs:** `[feature]-[category].md`

Examples:
- ✅ `regular-order-api-mapping.md`
- ✅ `conditional-order-patterns.md`
- ✅ `realtime-portfolio-design.md`

**NOT:**
- ❌ `orders.md` (too generic)
- ❌ `derivatives-orders.md` (project-specific → project folder)
- ❌ `api-conventions.md` (standards → API Standards folder)

---

## Current Planning Status

| Feature | Projects Interested | Status | Next Step |
|---------|-------------------|--------|-----------|
| Regular Orders | Derivatives ✅, Equity 📋 | Derivatives: Live | Equity planning |
| Conditional Orders | Derivatives 📋 | Planning | Requirements gathering |

---

**Last Updated:** 2026-02-04  
**Maintainer:** BA/PM Team
