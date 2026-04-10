# Derivatives Planning Documentation

> **Purpose:** Business planning and requirements for TradeX Derivatives Integration  
> **Audience:** PM, BA, Stakeholders (NO implementation code)  
> **Last Updated:** April 9, 2026

---

## 📁 Categories

| Category | Status | Focus Area |
|----------|--------|------------|
| **[Market/](./Market/)** | ✅ Complete | Real-time quotes, charts, WebSocket |
| **[Order/](./Order/)** | ✅ Complete | Order placement (regular & conditional) |
| **[Account/](./Account/)** | 📋 Planned | Account info, balance, positions |
| **[Asset/](./Asset/)** | 📋 Spec Ready | Vị thế mở (DRACC-003), portfolio |
| **[Open_DR_Sub_Account/](./Open_DR_Sub_Account/)** | 📋 Planning | Mở tiểu khoản phái sinh online (eligibility, FPT, admin) |

---

## 🎯 Quick Start

### For PM/BA (You!)

1. **Navigate to category** - Click folder above (Market/, Order/, etc.)
2. **Read README.md** - Category overview and status
3. **Check Planning/ folder** - Business requirements (**NO CODE**, PM-friendly!)
4. **Skip Specifications/** - For developers only

### For Developers

1. **Read category README.md** - Context
2. **Review Planning/03_Technical_Requirements.md** - High-level tech
3. **Read Specifications/** - Implementation details with code

---

## 📋 Documentation Standard

### PM Documentation (Planning/ folders)

✅ **YES - Include:**
- Business context & value
- Data flow diagrams
- High-level architecture  
- Acceptance criteria
- User stories

❌ **NO - Exclude:**
- Code blocks (Java, TypeScript)
- Implementation details
- Class/method names
- Technical specifics

**Rule Reference:** `.cursor/rules/derivatives-pm-documentation.mdc`

---

## 🏗️ Category Structure

Each category follows this standard:

```
{Category}/
├── README.md              ← Entry point (overview, status, links)
│
├── Planning/              ← PM-FRIENDLY (NO CODE!)
│   ├── 01_Integration_Plan.md
│   ├── 02_Business_Requirements.md
│   ├── 03_Technical_Requirements.md (HIGH-LEVEL)
│   └── 04_Use_Cases_Testing.md
│
├── Specifications/        ← FOR DEVELOPERS (code OK)
│   └── {Feature}_Spec.md
│
├── Issues/                ← Active tasks
│   └── {Feature}_Implementation.md
│       ├── Executive Summary (PM reads)
│       └── Technical Details (Developers read)
│
└── Archive/               ← Historical documents
```

---

## 📖 How to Use

**TradeX API Index:** Tất cả endpoint TradeX cho Derivatives (đã đặc tả trong các Spec) được tổng hợp tại [TradeX_Derivatives_API_Index.md](../Documentation/TradeX_Derivatives_API_Index.md) — dùng để tra cứu nhanh và mapping Lotte.

### Planning a New Feature (PM/BA)

1. Navigate to appropriate category folder
2. Create or update Planning/ documents
3. Focus on **business value** and **user impact**
4. Use **diagrams** for data flow (not code)
5. Leave implementation to developers

### Creating Implementation Issue

1. **FE (Frontend) issues:** Tạo và lưu tại **[Derivatives/FE Implementation/](../FE%20Implementation/README.md)** (theo module: Order, Market, Cash transaction). Issues trong Planning documentation/Issues/ có thể là redirect hoặc link tới FE Implementation.
2. **BE / khác:** Create in category Issues/ folder (e.g. Order/Issues/, Market/Issues/).
3. **Section 1: Executive Summary** - For PM (no code)
4. **Section 2+: Technical Details** - For developers (code OK)
5. Clear separation between business and technical

---

## 🎓 Skills & Rules

| Resource | Purpose | Location |
|----------|---------|----------|
| **PM Documentation Rule** | NO CODE in PM docs | `.cursor/rules/derivatives-pm-documentation.mdc` |
| **Structure Skill** | Category template | `.cursor/skills/derivatives-doc-structure/SKILL.md` |
| **Project Rules** | Overall guidelines | `AGENTS.md` |

---

## 🚀 Next Steps

### Immediate
- Market/ - Implement Chart API (issue created)
- Order/ - Document conditional orders

### Future
- Account/ - Plan account management features
- Asset/ - Open Position API spec ✅ | Implement & expand

---

**Maintained By:** BA/PM Team  
**Review:** After each major milestone  
**Questions:** Contact BA team
