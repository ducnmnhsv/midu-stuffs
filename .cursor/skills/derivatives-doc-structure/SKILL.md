# SKILL: Derivatives Documentation Structure

**Skill ID:** derivatives-doc-structure  
**Version:** 1.0  
**Created:** February 3, 2026  
**Purpose:** Standard documentation structure for all Derivatives categories

---

## When to Use This Skill

Use this skill when:
- Creating documentation for ANY Derivatives category (Market Data, Orders, Account, Asset, etc.)
- Organizing planning documents and specifications
- Creating implementation issues
- Refactoring existing Derivatives docs

---

## Standard Folder Structure

```
/Derivatives/Planning documentation/{Category}/
├── README.md                          ← Single entry point
│
├── Planning/                          ← Planning & requirements
│   ├── 01_Integration_Plan.md
│   ├── 02_Business_Requirements.md
│   ├── 03_Technical_Requirements.md
│   └── 04_Use_Cases_Testing.md
│
├── Specifications/                    ← Detailed technical specs
│   ├── {Feature}_API.md
│   ├── {Feature}_Integration.md
│   └── {Feature}_Spec.md
│
├── Issues/                            ← Active implementation issues
│   └── {Feature}_Implementation.md
│
└── Archive/                           ← Completed/historical docs
    └── {Feature}_Completion_Log.md
```

---

## File Naming Conventions

### Rules

1. **README.md** - Always at root, single entry point
2. **Folders** - PascalCase: `Planning/`, `Specifications/`, `Issues/`, `Archive/`
3. **Planning docs** - Numbered: `01_`, `02_`, `03_`, `04_`
4. **Spec files** - PascalCase with underscores: `Chart_API_Spec.md`, `SymbolInfo_API.md`
5. **Issue files** - PascalCase with underscores: `Chart_API_Implementation.md`
6. **NO brackets** - ❌ `[ISSUE]`, `[BRIEF]`, `[COMPLETION]`
7. **NO special prefixes** - ❌ `_index.md`, `00_EXECUTIVE_SUMMARY.md`

### Examples

✅ **Good:**
```
README.md
Planning/01_Integration_Plan.md
Specifications/Order_API_Spec.md
Issues/Order_Implementation.md
```

❌ **Bad:**
```
_index.md
[ISSUE] Order API.md
00_EXECUTIVE_SUMMARY.md
order-api-spec.md
```

---

## README.md Template

```markdown
# {Category} - Derivatives Integration

> **Module:** {Category Name}  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** {Date}  
> **Status:** {Planning/In Progress/Complete}

---

## 📋 Quick Navigation

| Section | Description |
|---------|-------------|
| [Overview](#overview) | Business context and architecture |
| [Implementation Status](#implementation-status) | Current progress |
| [Documentation Map](#documentation-map) | All docs index |
| [Active Issues](#active-issues) | Ready tasks |
| [How to Use](#how-to-use) | Role-based guide |

---

## 🎯 Overview

### Mission
{1-2 sentence description of what this category does}

### Scope

| In Scope | Out of Scope |
|----------|--------------|
| ✅ {Feature 1} | ❌ {Out of scope 1} |
| ✅ {Feature 2} | ❌ {Out of scope 2} |
| 📋 {Feature 3 - Issue Created} | |

---

## 🏗️ Architecture

{High-level architecture diagram and key principles}

---

## 📊 Implementation Status

### ✅ Completed Features

| Feature | Status | Services | Documents |
|---------|--------|----------|-----------|
| {Feature 1} | ✅ Live | {services} | Planning/01, Specs/{spec} |

### 📋 Pending Implementation

| Feature | Status | Issue | Priority | Estimate |
|---------|--------|-------|----------|----------|
| {Feature 2} | 📋 Issue Created | [Issues/{issue}](#) | High | X weeks |

---

## 📚 Documentation Map

### Planning & Requirements

| # | Document | Type | Audience | Description |
|---|----------|------|----------|-------------|
| 01 | [Integration Plan](./Planning/01_Integration_Plan.md) | Planning | Architect, BE Lead | Roadmap |
| 02 | [Business Requirements](./Planning/02_Business_Requirements.md) | BRD | PM, BA | Business needs |
| 03 | [Technical Requirements](./Planning/03_Technical_Requirements.md) | Spec | BE Developers | High-level tech |
| 04 | [Use Cases & Testing](./Planning/04_Use_Cases_Testing.md) | Testing | BA, QA | Test scenarios |

### Technical Specifications

| Document | Focus Area | Target Audience |
|----------|------------|-----------------|
| [{Feature} Spec](./Specifications/{Feature}_Spec.md) | Detailed spec | BE Developers |

### Active Issues

| Issue | Priority | Status | Estimate |
|-------|----------|--------|----------|
| [{Feature} Implementation](./Issues/{Feature}_Implementation.md) | High | 📋 Ready | X weeks |

---

## 👥 How to Use This Documentation

### For BA/PM
{Guide for BA/PM role}

### For Developers
{Guide for Developers role}

### For QA
{Guide for QA role}

---

## 📦 Related Folders

| Folder | Content | Status |
|--------|---------|--------|
| `/Derivatives/Planning documentation/{Other Category}/` | {Description} | {Status} |

---

**Prepared By:** BA Team  
**Last Review:** {Date}  
**Document Version:** 1.0
```

---

## Planning Documents

### 01_Integration_Plan.md

**Purpose:** Detailed technical roadmap  
**Audience:** Architect, Backend Lead  
**Content:**
- Overall integration approach
- Service-by-service breakdown
- Data flow diagrams
- Timeline and phases

### 02_Business_Requirements.md

**Purpose:** Business Requirements Document (BRD)  
**Audience:** PM, BA, Stakeholders  
**Content:**
- Business goals
- User stories
- Success metrics
- Scope (in/out)
- Risks and constraints

### 03_Technical_Requirements.md

**Purpose:** High-level technical requirements  
**Audience:** Backend Developers  
**Content:**
- Core requirements (REQ-1, REQ-2, etc.)
- Services affected
- Configuration
- Performance requirements
- Links to detailed specs

### 04_Use_Cases_Testing.md

**Purpose:** Test scenarios and acceptance criteria  
**Audience:** BA, QA Team  
**Content:**
- Use case descriptions
- Test scenarios (TC-1, TC-2, etc.)
- Expected results
- Edge cases
- Regression tests

---

## Specifications Folder

### Purpose

Detailed technical specifications, each focusing on ONE feature/API.

### Guidelines

1. **One spec per feature** - Don't create monolithic specs
2. **Max ~500 lines** - If longer, split into multiple files
3. **Code examples** - Include implementation snippets
4. **API contracts** - Request/response structures
5. **Data models** - Schemas, DTOs, entities

### Naming Pattern

- `{Feature}_API.md` - API endpoint specifications
- `{Feature}_Integration.md` - Integration with external systems
- `{Feature}_Spec.md` - General feature specifications
- `Data_Storage.md` - Database/cache schemas

---

## Issues Folder

### Purpose

Active implementation issues ready for development.

### Issue Template

```markdown
# {Feature} Implementation for Derivatives

**Issue Type:** Feature Request / Enhancement  
**Priority:** {High/Medium/Low}  
**Component:** {Component Name}  
**Related Module:** {Services}  
**Created:** {Date}  
**Status:** 📋 Ready for Dev Team Review

---

## 📋 Executive Summary

### Problem Statement
{1-2 paragraphs describing the problem}

### Current vs Target Behavior
{Code examples showing current and expected behavior}

### Solution Approach
{3-5 step high-level approach}

### Timeline & Effort
{Estimated timeline with phase breakdown}

### Key Success Criteria
{Bullet list of acceptance criteria}

---

## 📊 Business Context
{Detailed business context}

---

## 🔍 Technical Background
{Current implementation, data flow diagrams}

---

## 📝 Detailed Requirements

### REQ-1: {Requirement Name}
{Detailed requirement with code examples}

### REQ-2: {Requirement Name}
{Detailed requirement with code examples}

---

## 🧪 Test Scenarios

### Test Case 1: {Scenario Name}
{Request, expected response, validation criteria}

---

## 💡 Implementation Approaches
{Different approaches with pros/cons}

---

## ❓ Key Questions for Dev Team
{Questions to be clarified}

---

## ✅ Acceptance Criteria (Summary)
{Consolidated checklist}

---

## 📚 Reference Documents
{Links to related docs}

---

**Issue Status:** 📋 Ready for Dev Review  
**Next Step:** {Next action}
```

---

## Archive Folder

### Purpose

Store completed or historical documents for reference.

### What to Archive

- ✅ Completion logs and summaries
- ✅ Old versions of specs (when major refactor)
- ✅ Implementation issue logs
- ✅ Meeting notes and decisions (optional)

### What NOT to Archive

- ❌ Active planning docs
- ❌ Current specifications
- ❌ Open issues

---

## Document Maintenance Rules

### When to Update

1. **New feature completed** → Update README.md status tables
2. **New issue created** → Add to README.md Active Issues section
3. **Requirements changed** → Update relevant docs + README
4. **Testing complete** → Update status, add findings

### Version Control

- README.md shows "Last Updated" date
- Planning docs use inline version (v1.0, v1.1, etc.)
- Major changes warrant version bump + changelog

---

## Key Principles

### 1. Single Entry Point

- README.md is THE starting point
- All navigation goes through README
- No need to hunt for index files

### 2. Clear Separation

- Planning vs Specs vs Issues are distinct
- Each folder has clear purpose
- No mixed content types

### 3. Consistent Naming

- PascalCase for files (except README)
- Underscores for multi-word names
- No special characters or brackets

### 4. Scalable Structure

- Easy to add new features
- New category? Copy template
- Predictable organization

### 5. Size Management

- No file > 700 lines
- Split large specs into focused docs
- One issue per file

### 6. PM-Friendly Documentation (CRITICAL!)

**Rule:** `.cursor/rules/derivatives-pm-documentation.mdc`

**For Planning/ folder (PM reads):**
- ❌ **NO CODE BLOCKS** (Java, TypeScript, etc.)
- ❌ **NO implementation details** (class names, methods, etc.)
- ✅ **YES to diagrams** (data flow, architecture)
- ✅ **YES to business logic** (why, what, acceptance criteria)
- ✅ **YES to API contracts** (request/response at abstract level)

**For Specifications/ folder (Developers read):**
- ✅ Code examples OK
- ✅ Implementation details OK
- ✅ Technical architecture OK

**For Issues/ folder:**
- Executive Summary = PM-friendly (no code)
- Technical sections = Developer-friendly (code OK)

---

## Examples

### Good Structure (Market Data)

```
Market data/
├── README.md                              ✅
├── Planning/
│   ├── 01_Integration_Plan.md            ✅
│   ├── 02_Business_Requirements.md       ✅
│   ├── 03_Technical_Requirements.md      ✅
│   └── 04_Use_Cases_Testing.md           ✅
├── Specifications/
│   ├── SymbolInfo_API.md                 ✅
│   ├── WebSocket_Integration.md          ✅
│   ├── Chart_API_Spec.md                 ✅
│   └── Data_Storage.md                   ✅
├── Issues/
│   └── Chart_API_Implementation.md       ✅
└── Archive/
    └── Implementation_Issues_Log.md      ✅
```

### Bad Structure (Old Way)

```
Market data/
├── _index.md                              ❌ Underscore prefix
├── 00_EXECUTIVE_SUMMARY.md                ❌ Redundant with README
├── [ISSUE] Chart API.md                   ❌ Bracket prefix
├── [BRIEF] Chart API.md                   ❌ Should merge into issue
├── [COMPLETION] Log.md                    ❌ Should be in Archive
├── 02_BE_REQUIREMENTS_SPEC.md             ❌ Monolithic (1345 lines!)
└── ... (all files flat)                   ❌ No organization
```

---

## Checklist for New Category

When creating documentation for a new Derivatives category:

- [ ] Create folder: `/Derivatives/Planning documentation/{Category}/`
- [ ] Create README.md using template above
- [ ] Create subfolders: `Planning/`, `Specifications/`, `Issues/`, `Archive/`
- [ ] Create Planning docs (01-04)
- [ ] Create Specifications (as needed, < 500 lines each)
- [ ] Create Issues (one per feature/task)
- [ ] Update README.md with all links
- [ ] Cross-reference with other categories in "Related Folders"
- [ ] Follow naming conventions strictly
- [ ] No files > 700 lines
- [ ] All links working
- [ ] README.md is comprehensive entry point

---

## Common Pitfalls to Avoid

1. ❌ **Creating monolithic specs** → Split into focused files
2. ❌ **Mixing planning and specs** → Use folder structure
3. ❌ **Inconsistent naming** → Follow PascalCase rules
4. ❌ **Multiple summaries** → One README.md only
5. ❌ **Flat structure** → Use subfolders for organization
6. ❌ **Outdated links** → Update README when files change
7. ❌ **No clear entry point** → README.md must be comprehensive

---

## Success Criteria

A well-structured Derivatives category documentation should:

✅ Have a comprehensive README.md that anyone can use as entry point  
✅ Clearly separate planning, specs, issues, and archive  
✅ Use consistent PascalCase naming throughout  
✅ Have no file > 700 lines  
✅ Have working links in README.md to all docs  
✅ Be easy for new team members to navigate  
✅ Scale easily when adding new features  
✅ Follow same structure as other categories  

---

## Skill Usage

### For AI Agent

When asked to create or refactor Derivatives documentation:

1. **Read this skill first** - Understand the standard structure
2. **Apply the template** - Use README.md template
3. **Follow naming rules** - Strictly follow PascalCase conventions
4. **Organize properly** - Use subfolder structure
5. **Cross-reference** - Link to other categories in README
6. **Validate** - Check against checklist and success criteria

### For Human (BA/PM)

When planning a new Derivatives category:

1. **Copy Market data/ structure** - Use as reference
2. **Follow this skill** - Apply all rules
3. **Customize README.md** - Fill in category-specific content
4. **Create planning docs** - Start with 01-04
5. **Add specs as needed** - One per feature
6. **Review checklist** - Ensure nothing missed

---

## Ecosystem Integration

This skill is part of the **TradeX Skill/Rule Ecosystem**. When activated, also check:

| Context | Rule/Skill | Why |
|---------|------------|-----|
| Always | `derivatives-pm-documentation` | PM-readability gate for Planning/ folder |
| For Specifications/ content | `derivatives-api-spec-format` | API spec structure within the folder |
| For FE Issues | `fe-repo-derivatives-issues` | Read nhsv-mts-rn before writing Issues/ |
| For any API content | `tradex-api-naming` | Consistent naming in spec filenames and content |

> **Orchestrator:** See `.cursor/rules/ecosystem-orchestrator.mdc` for full routing logic.

---

**Skill Status:** Active  
**Last Updated:** February 27, 2026  
**Maintained By:** BA Team  
**Used By:** All Derivatives category documentation
