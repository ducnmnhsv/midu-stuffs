---
name: prompt-optimizer
description: Optimize and enhance user prompts using best practices from prompt engineering. Restructure vague or casual prompts into clear, comprehensive instructions. Use when user asks to improve, optimize, rewrite, or enhance their prompt, or when they mention prompt engineering, better prompts, or clearer instructions. Works with Vietnamese or English input.
---

# Prompt Optimizer

Enhances user prompts by applying prompt engineering best practices, adding structure, clarity, and context.

## When to Use

Apply this skill when:
- User asks to "improve my prompt", "optimize this", "make it better"
- User mentions "prompt engineering", "better instructions"
- User provides vague or unclear requests
- User wants to rewrite or enhance existing prompts

## Core Optimization Process

### Step 1: Analyze Input Prompt

Identify prompt weaknesses:
- Missing context or background
- Unclear objectives or success criteria
- Lack of structure or organization
- Vague requirements or constraints
- No examples or expected format
- Missing role or persona specification

### Step 2: Apply Optimization Framework

Transform prompt using this structure:

```markdown
## Role & Context
[Define AI role and situation context]

## Objective
[Clear, specific goal with success criteria]

## Requirements
[Structured list of must-haves]

## Constraints
[Limitations, boundaries, what to avoid]

## Format
[Expected output structure/template]

## Examples
[Concrete examples if helpful]

## Reasoning
[Include "think step-by-step" or chain of thought]
```

### Step 3: Enhance Writing Quality

Apply these principles:
- **Comprehensive**: Cover all aspects
- **Succinct**: Remove fluff, keep essentials
- **Concise**: Short sentences, clear language
- **Simple**: Avoid jargon unless necessary
- **Clear**: Unambiguous instructions

## Optimization Patterns

### Pattern 1: Vague Request → Structured Task

**Before:**
```
Viết một cái gì đó về API
```

**After:**
```
## Role & Context
You are a technical writer documenting TradeX API specifications for PM/BA audience.

## Objective
Create API documentation that helps non-technical stakeholders understand 
the API behavior, field mappings, and error handling.

## Requirements
- Include API endpoint and HTTP method
- Map TradeX fields to Lotte fields with descriptions
- Document all error responses with examples
- Explain auto-populated fields

## Format
Use standard TradeX API spec template from @TradeX Knowledge/API Standards/

## Reasoning
Walk through each section systematically, explaining your choices.
```

### Pattern 2: Add Missing Context

If user request lacks context, add relevant background:

**Before:**
```
Check this code
```

**After:**
```
## Role & Context
You are reviewing code for [specify: security/performance/style] 
in a [specify tech stack] project.

## Objective
Identify [specific issues] and provide actionable feedback.

## Requirements
- Check for [specific patterns]
- Follow [specific standards]
- Consider [specific constraints]

## Output Format
- 🔴 Critical: Must fix
- 🟡 Suggestion: Should improve  
- 🟢 Nice-to-have: Optional

## Reasoning
For each issue found, explain WHY it's a problem and HOW to fix it.
```

### Pattern 3: Add Examples for Clarity

**Before:**
```
Format this data nicely
```

**After:**
```
## Objective
Transform raw data into readable markdown table format.

## Input Format
[Show example of input data structure]

## Expected Output
[Show exact table format desired]

## Requirements
- Sort by [criteria]
- Include [specific columns]
- Format numbers as [pattern]

## Example
Input: `{"name": "A", "value": 100}`
Output:
| Name | Value |
|------|-------|
| A    | 100   |
```

## Writing Tone Guidelines

### For PM/BA Audience

- Use business language, not technical jargon
- Focus on "what" and "why", less on "how"
- Include Vietnamese terms for domain concepts
- Mix English for technical terms (API, endpoint, etc.)

### Sentence Structure

**✅ Good (concise):**
```
Create API spec following TradeX conventions.
```

**❌ Avoid (verbose):**
```
I would like you to please create an API specification 
document that follows all of the TradeX conventions and 
standards that we have established.
```

### Organization

**✅ Good (structured):**
```
## Requirements
1. Field mapping table
2. Error handling section
3. Business rules
```

**❌ Avoid (unstructured):**
```
Include field mappings and also error handling, 
and don't forget business rules
```

## Add Chain of Thought

For complex tasks, add reasoning instructions:

```markdown
## Reasoning Process
Before providing the answer:
1. Analyze the requirements
2. Identify potential issues or edge cases
3. Consider alternative approaches
4. Verify your solution against requirements
5. Explain your reasoning
```

Or use shorthand:
```markdown
Think step-by-step and verify your output before responding.
```

## Language Handling

### Vietnamese Input

When user provides Vietnamese prompt:
1. Understand the intent
2. Create optimized prompt in **English** (better for AI)
3. Keep Vietnamese for:
   - Business domain terms
   - User-facing content
   - Field descriptions if needed

**Example:**
```
User: "Tạo tài liệu về API đặt lệnh"

Optimized:
## Role & Context
Create Vietnamese documentation for Order Placement API...

## Requirements
- Document endpoint: POST /api/v1/derivatives/order
- Field names in English, descriptions in Vietnamese
- Include examples: "Đặt lệnh mua", "Đặt lệnh bán"
```

### English Input

Keep optimized prompt in English unless user explicitly needs Vietnamese output.

## Output Format

Present optimized prompt as:

```markdown
## 📋 Original Prompt
[User's original prompt]

## ✨ Optimized Prompt
[Restructured, enhanced version]

## 🔍 Key Improvements
- Added: [what was added]
- Clarified: [what was made clearer]
- Structured: [how it was organized]
```

## Verification Checklist

Before presenting optimized prompt, verify:

- [ ] Clear role and context specified
- [ ] Specific, measurable objective
- [ ] Requirements listed explicitly
- [ ] Constraints and boundaries defined
- [ ] Output format or template provided
- [ ] Examples included (if helpful)
- [ ] Reasoning/verification step added
- [ ] Writing is concise and clear
- [ ] Appropriate for PM/BA audience
- [ ] Language choice justified

## Common PM/BA Scenarios

### Scenario 1: Document Creation

User: "Write docs for this API"

Optimize to:
- Specify document type (spec, guide, reference)
- Define audience (PM, BA, Dev, stakeholders)
- Clarify structure (follow template or custom)
- Include examples of good docs
- Mention relevant standards

### Scenario 2: Analysis Request

User: "Analyze this feature"

Optimize to:
- Specify analysis type (technical, business, competitive)
- Define what aspects to analyze
- Clarify output format (report, summary, bullet points)
- Mention constraints or focus areas
- Include success criteria

### Scenario 3: Review Request

User: "Review this"

Optimize to:
- Specify review type (quality, completeness, accuracy)
- Define review criteria or standards
- Clarify feedback format
- Mention what to focus on or skip
- Include examples of good feedback

## Quick Tips

**Add context:**
```
❌ "Create a table"
✅ "Create comparison table of Equity vs Derivatives order types"
```

**Add constraints:**
```
❌ "Write brief summary"
✅ "Write 2-3 sentence summary, max 150 words"
```

**Add format:**
```
❌ "List the items"
✅ "List as markdown table with columns: Name, Type, Required"
```

**Add verification:**
```
❌ "Calculate the total"
✅ "Calculate total and verify math before responding"
```

## Related Resources

For PM/BA domain knowledge:
- TradeX conventions: `@TradeX Knowledge/API Standards/`
- System mechanisms: `@TradeX Knowledge/System/`
- Planning patterns: `@TradeX Knowledge/Planning/`

---

**Usage:** Mention "optimize my prompt" or "improve this request" to trigger this skill.
