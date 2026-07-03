# Prompt Enhancement Workflow - Quick Guide

> **Auto-enhances prompts at conversation start for better results**

## 🎯 What It Does

**Automatic workflow:**
1. You start conversation with request
2. AI detects if prompt needs enhancement
3. AI presents **enhanced version** with structure
4. You confirm → AI executes with better prompt

**Result:** Clear, comprehensive, well-structured instructions every time!

---

## 🚀 Quick Start

### Your First Enhanced Prompt

**Just type naturally:**
```
Viết docs cho API đặt lệnh
```

**AI will respond:**
```markdown
## 🎯 Prompt Enhancement Suggestion

### Your Original Request
Viết docs cho API đặt lệnh

### Recommended Enhanced Version
[Structured, detailed prompt with all sections]

### What I Added
- Context about TradeX standards
- Clear requirements and format
- Examples and references

Reply "yes" to proceed with enhanced version.
```

**You reply:** `yes`

**AI executes** with enhanced prompt → Better output!

---

## ✅ When to Use

### Auto-triggers for:
- ✅ Documentation requests
- ✅ API specifications
- ✅ Feature analysis
- ✅ Planning tasks
- ✅ Vague or unclear requests

### Skips for:
- ❌ Already clear, structured prompts
- ❌ Simple queries ("What is X?")
- ❌ Follow-up questions
- ❌ File operations ("Read this file")

---

## 💬 Confirmation Phrases

**Quick approval:**
- English: `yes`, `ok`, `proceed`, `go`
- Vietnamese: `ok`, `được`, `tiếp tục`
- Emoji: ✅ 👍

**Reject enhancement:**
- `no` → Use original prompt
- `refine` → Adjust enhancement

---

## 📋 Example Workflows

### Example 1: Simple Request → Enhanced

**You:**
```
Tạo API spec cho cancel order
```

**AI:**
```markdown
## 🎯 Enhancement Suggestion

### Recommended Version
## Role & Context
Create API specification for Order Cancellation endpoint...

## Requirements
- Endpoint: PUT /api/v1/derivatives/order/cancel
- Field mappings (TradeX → Lotte)
- Error responses
- Business rules

## Format
Follow @Knowledge/TradeX/API Standards/tradex-api-spec-template.md

Reply "yes" to proceed.
```

**You:** `yes`

**AI creates:** Complete, structured API spec! ✅

---

### Example 2: Already Clear → Skip

**You:**
```
Follow @Knowledge/TradeX/API Standards/tradex-api-spec-template.md 
to create Order Cancellation API spec with:
- Field mappings
- Error handling
- Business rules
```

**AI:**
```
✅ Your prompt is clear! Proceeding directly...
[Creates spec without enhancement step]
```

---

## 🎨 Enhancement Benefits

### Before Enhancement
```
Viết docs API
```
- Vague goal
- No context
- Missing requirements
- No format specified

### After Enhancement
```
## Role & Context
Technical writer for TradeX Derivatives...

## Objective
Create comprehensive API documentation...

## Requirements
[Detailed list]

## Format
[Template reference]

## Reasoning
[Verification steps]
```
- Clear objective
- Full context
- Structured requirements
- Quality verification

---

## 🔧 Advanced Usage

### Refine Enhancement

If enhancement doesn't match your needs:

**You:** `refine - focus more on error handling`

**AI:** 
```
[Adjusts enhancement with emphasis on errors]

Better? Reply "yes" to proceed.
```

### Skip Auto-Enhancement

For one request:
```
[No enhancement] Write simple docs for login API
```

AI will skip enhancement and execute directly.

---

## 💰 Quota-Friendly

**Smart detection:**
- Only enhances when **clearly beneficial**
- Skips for **already-clear** prompts
- **No extra cost** for simple requests

**Saves time:**
- Better prompts = Better first output
- Fewer back-and-forth iterations
- Less rework needed

---

## 🎯 Perfect For

**PM/BA roles working on:**
- TradeX API specifications
- Feature analysis
- Documentation
- Planning & requirements
- System understanding

**Languages:**
- Vietnamese input ✅
- English input ✅
- Mixed Vietnamese/English ✅

---

## 📊 Success Metrics

**With enhancement:**
- ✅ First output meets requirements
- ✅ Less back-and-forth
- ✅ Consistent structure
- ✅ Complete information

**Without enhancement:**
- ❌ Vague output
- ❌ Missing sections
- ❌ Need clarification
- ❌ Multiple revisions

---

## 🚨 Troubleshooting

### Enhancement too detailed?

**You:** `simpler version please`

**AI:** Simplifies enhancement

### Want original prompt?

**You:** `no, use original`

**AI:** Executes with your original words

### Enhancement off-topic?

**You:** `refine - focus on [specific aspect]`

**AI:** Adjusts focus

---

## 💡 Pro Tips

1. **Trust the enhancement** - Usually improves output quality
2. **Quick yes** - Just type `yes` to save time
3. **Refine if needed** - AI will adjust to your preferences
4. **Natural language** - Type normally, AI handles structure
5. **Vietnamese OK** - AI understands and optimizes

---

## 📞 Support

**Issues?**
- Enhancement not triggering? → Request explicitly: "enhance my prompt first"
- Too many enhancements? → Use clear, structured prompts
- Need help? → Ask "how do I use prompt enhancement?"

---

**Enabled by default for PM/BA conversations!** 🎉

Just start typing naturally, AI will enhance when beneficial.
