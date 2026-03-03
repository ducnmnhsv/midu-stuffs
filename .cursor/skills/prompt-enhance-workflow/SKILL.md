---
name: prompt-enhance-workflow
description: Automatically enhance user prompts at conversation start using prompt-optimizer skill. Triggers when user begins a new conversation or explicitly requests prompt enhancement. Presents optimized version for confirmation before execution. Use for PM/BA working on TradeX projects who want clear, structured instructions.
---

# Prompt Enhancement Workflow

Auto-enhances user prompts at conversation start, ensuring clear, structured instructions before execution.

## When to Trigger

**Auto-trigger at conversation start when:**
- User provides initial request in new conversation
- Request appears vague or lacks structure
- User is PM/BA role (inferred from context or request type)
- Request involves: documentation, analysis, API specs, planning

**Explicit trigger when user says:**
- "enhance my prompt first"
- "optimize before executing"
- "make this clearer before starting"
- "restructure my request"

**Do NOT trigger for:**
- Simple, already-clear instructions
- Follow-up questions in ongoing conversation
- Quick lookups ("what is X?", "show me Y")
- Emergency/urgent requests

## Workflow Steps

### Step 1: Detect & Analyze

When conversation starts, quickly assess:

```
Is this prompt:
- [ ] Vague or unclear?
- [ ] Missing context?
- [ ] Lacking structure?
- [ ] Would benefit from examples?
- [ ] PM/BA domain task?

If YES to 2+ → Proceed to Step 2
If NO → Execute directly
```

### Step 2: Enhance with Prompt Optimizer

Apply prompt-optimizer skill to restructure:

```markdown
## 📋 Your Original Request
[User's input - exactly as provided]

## ✨ Enhanced Prompt (Recommended)
[Optimized version with structure]

## 🔍 Key Improvements
- Added: [what was added]
- Clarified: [what was clarified]
- Structured: [how organized]

## ⚡ Action Required
Would you like me to proceed with:
1. ✅ Enhanced version (recommended)
2. 📝 Original version (as-is)
3. ✏️  Let me refine further

Please confirm to continue.
```

### Step 3: Wait for Confirmation

**User options:**
- Confirm → Proceed with enhanced prompt
- Modify → User adjusts, re-enhance if needed
- Reject → Use original prompt
- Quick approval phrases: "yes", "ok", "proceed", "go ahead", "tiếp tục"

### Step 4: Execute

Once confirmed, execute with the chosen prompt version.

**Include in execution context:**
```
[Using enhanced prompt as instruction]

Original request: [user's words]
Optimized instruction: [enhanced version]

[Proceed with task...]
```

## Enhancement Criteria

### Enhance These Requests

**Documentation tasks:**
```
❌ "Write API docs"
✅ Enhance → Add template, audience, standards
```

**Analysis tasks:**
```
❌ "Analyze this feature"
✅ Enhance → Add analysis type, format, criteria
```

**Planning tasks:**
```
❌ "Plan this implementation"
✅ Enhance → Add scope, constraints, deliverables
```

**API specs:**
```
❌ "Create API spec cho đặt lệnh"
✅ Enhance → Add mapping structure, error handling, examples
```

### Don't Enhance These

**Already clear:**
```
✅ "Follow @TradeX Knowledge/API Standards/ to create 
    Order Cancellation API spec with field mappings"
```

**Simple queries:**
```
✅ "What is the init job?"
✅ "Show me market data flow"
```

**Quick requests:**
```
✅ "Read this file"
✅ "List files in folder"
```

## Language Handling

### Vietnamese Input

```
User: "Tạo tài liệu API"

Enhanced (English with VN context):
## Role & Context
Create Vietnamese API documentation for TradeX...

## Requirements
- Field names: English
- Descriptions: Vietnamese
- Examples: Vietnamese business terms
```

### English Input

Keep enhanced prompt in English unless output needs Vietnamese.

## Presentation Format

```markdown
---

## 🎯 Prompt Enhancement Suggestion

### Your Original Request
```
[exact user input]
```

### Recommended Enhanced Version
```
[structured, clear prompt]
```

### What I Added
- **Context**: [what context added]
- **Structure**: [how organized]
- **Clarity**: [what clarified]
- **Examples**: [if added]

### Next Steps
Reply with:
- **"yes"** / **"ok"** → I'll proceed with enhanced version
- **"no"** → I'll use your original wording
- **"refine"** → Tell me what to adjust

---
```

## Integration with TradeX Context

When enhancing prompts, automatically include relevant context:

**For API tasks:**
- Reference: `@TradeX Knowledge/API Standards/`
- Template: `tradex-api-spec-template.md`
- Standards: `tradex-api-conventions.md`

**For system understanding:**
- Reference: `@TradeX Knowledge/System/`

**For planning:**
- Reference: `@TradeX Knowledge/Planning/`

## Example Workflows

### Example 1: API Documentation

**User input:**
```
Viết docs cho API query lệnh chưa khớp
```

**Workflow:**

```markdown
## 🎯 Prompt Enhancement Suggestion

### Your Original Request
Viết docs cho API query lệnh chưa khớp

### Recommended Enhanced Version

## Role & Context
Create API specification document for "Query Unmatch Orders" 
endpoint in Derivatives project, following TradeX standards.

## Objective
Document the API that retrieves pending/cancellable orders 
for a trading account.

## Requirements
1. API endpoint mapping (TradeX → Lotte)
   - TradeX: GET /api/v1/derivatives/order/todayUnmatch
   - Lotte: /tuxsvc/der/order/query-unmatch (DRORD-011)

2. Field mappings
   - Input fields with auto-populated indicators
   - Output fields with transformations

3. Error handling
   - Validation errors (INVALID_PARAMETER)
   - Pass-through Lotte errors

4. Business rules
   - Account validation
   - Required fields
   - Success criteria

## Format
Follow template: @TradeX Knowledge/API Standards/tradex-api-spec-template.md

## Reference
Similar spec: @Derivatives/.../Regular_Orders_API_Spec.md

## Output Language
Vietnamese for descriptions, English for technical terms

## Reasoning
Verify all sections complete before presenting.

### What I Added
- **Context**: Specified project, standards, audience
- **Structure**: Organized into clear sections
- **Clarity**: Defined exact endpoints and requirements
- **Examples**: Referenced similar working spec

### Next Steps
Reply "yes" to proceed with enhanced version.
```

### Example 2: Feature Analysis

**User input:**
```
Phân tích feature conditional orders
```

**Workflow:**

```markdown
## 🎯 Prompt Enhancement Suggestion

### Your Original Request
Phân tích feature conditional orders

### Recommended Enhanced Version

## Role & Context
Business Analyst analyzing Conditional Orders feature 
for TradeX Derivatives project.

## Objective
Provide comprehensive feature analysis covering business 
value, technical requirements, and implementation approach.

## Analysis Scope
1. **Business Analysis**
   - Market demand and user needs
   - Business value and ROI
   - Competitive positioning

2. **Technical Analysis**
   - Integration with existing order system
   - Required Lotte APIs (DRORD-005, DRORD-006, etc.)
   - Data storage and state management
   - Real-time monitoring requirements

3. **Implementation Analysis**
   - High-level architecture
   - Service interactions
   - Risks and mitigation strategies
   - Success criteria

## Reference Materials
- Lotte API specs: Check DRORD-005, DRORD-006, DRORD-023-026
- Existing patterns: @TradeX Knowledge/Planning/regular-order-api-mapping.md
- System architecture: @TradeX Knowledge/System/

## Output Format
### Feature Analysis: Conditional Orders

#### Business Value
[Vietnamese for stakeholders]

#### Technical Requirements
[English terms, Vietnamese explanations]

#### Implementation Approach
[Structured breakdown]

#### Risks & Mitigation
[Identified risks with solutions]

## Reasoning
Consider both business and technical perspectives.
Verify completeness before presenting.

### What I Added
- **Context**: BA role, specific feature scope
- **Structure**: 3-part analysis (business/technical/implementation)
- **Clarity**: Referenced specific Lotte APIs
- **Examples**: Linked to existing patterns

### Next Steps
Reply "yes" to proceed with this analysis.
```

## Quick Approval Phrases

Accept these as confirmation:
- English: "yes", "ok", "sure", "proceed", "go", "continue", "looks good"
- Vietnamese: "ok", "được", "tiếp tục", "đồng ý", "thực hiện"
- Emoji: ✅, 👍

## Edge Cases

### Already Enhanced Prompt

If user provides already well-structured prompt:

```markdown
✅ Your prompt is already clear and well-structured!

Proceeding directly with your request...

[Execute immediately without enhancement step]
```

### User Rejects Enhancement

```markdown
Understood. Proceeding with your original request as-is.

[Execute with original prompt]
```

### Multiple Refinements

If user asks to refine multiple times:

```markdown
## Current Version (Iteration N)
[Latest enhanced version]

Please tell me specifically what to adjust:
- Add more context about [what]?
- Change focus to [what]?
- Remove [what]?
- Other changes?
```

## Performance Optimization

**To save quota:**
- Only enhance when benefit is clear (vague → structured)
- Skip enhancement for follow-up questions
- Reuse enhanced prompts for similar requests
- Cache common enhancement patterns

## Related Skills & Ecosystem Integration

This skill is part of the **TradeX Skill/Rule Ecosystem**. After enhancement and user confirmation, route to:

| Enhanced prompt type | Next skill/rule to activate |
|---------------------|-----------------------------|
| API spec task | `tradex-api-naming` → `derivatives-api-spec-format` |
| Documentation task | `derivatives-doc-structure` → `derivatives-pm-documentation` |
| Order API task | `tradex-order-api-response-standards` |
| FE issue task | `fe-repo-derivatives-issues` → `derivatives-doc-structure` |
| System analysis | `tradex-analyst` → `tradex-knowledge` |

> **Orchestrator:** See `.cursor/rules/ecosystem-orchestrator.mdc` for full routing logic.

- **prompt-optimizer**: Core optimization logic
- **tradex-api-naming**: API naming conventions

---

**Usage:** This workflow triggers automatically at conversation start for PM/BA requests, or explicitly with "enhance my prompt first".

**Quota-friendly:** Only enhances when clearly beneficial, skips for already-clear requests.
