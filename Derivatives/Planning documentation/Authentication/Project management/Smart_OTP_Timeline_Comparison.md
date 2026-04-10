# Smart OTP Implementation - Timeline Comparison

## Document Information

**Version:** 1.0  
**Created:** 2026-04-10  
**Status:** 📋 Planning  
**Purpose:** Compare implementation timeline between Smart OTP generation-only vs Smart OTP with login integration

---

## Timeline Comparison Overview

This document presents two implementation options for Smart OTP feature on NHSV Pro:

- **Option 1:** Smart OTP generation only (for transactions, no login integration)
- **Option 2:** Smart OTP with login integration (full authentication flow)

Both options assume Lotte provides Smart OTP API first, then NHSV Pro implements the integration.

---

## Option 1: Smart OTP Generation Only (No Login Integration)

**Scope:** Implement Smart OTP for transaction authorization only. Users continue using existing login methods (SMS OTP, biometric). Smart OTP is used only when confirming sensitive transactions (order placement, money transfer, etc.).

### Proposed Timeline - Option 1

| Seq. | Task                                                      | Expected / Executed | 4th week (Feb.) | 1st week (Mar.) | 2nd week (Mar.) | 3rd week (Mar.) | 4th week (Mar.) | 1st week (Apr.) | 2nd week (Apr.) |
|------|-----------------------------------------------------------|---------------------|-----------------|-----------------|-----------------|-----------------|-----------------|-----------------|-----------------|
| **Part 1: Lotte Implementation** |                                       |                     |                 |                 |                 |                 |                 |                 |                 |
| 1.1  | Receiving & Review final API specification from Lotte    | 3 days              | ████░░░░        |                 |                 |                 |                 |                 |                 |
| 1.2  | Assign staff in charge (Lotte side)                      | 1 day               | ░░░████░        |                 |                 |                 |                 |                 |                 |
| 1.3  | Review & confirm Smart OTP API contract                  | 2 days              | ░░░░░███        |                 |                 |                 |                 |                 |                 |
| 1.4  | Lotte API Development & Internal Testing                 | 7 days              |                 | ████████        |                 |                 |                 |                 |                 |
| 1.5  | Lotte provides Sandbox API for NHSV                      | 1 day               |                 | ░░░░░░░█        |                 |                 |                 |                 |                 |
| **Part 2: NHSV Implementation** |                                        |                     |                 |                 |                 |                 |                 |                 |                 |
| 2.1  | UI/UX Design for Smart OTP flows                         | 3 days              |                 |                 | ████░░░░        |                 |                 |                 |                 |
| 2.2  | Define TradeX API specifications & integration plan      | 2 days              |                 |                 | ░░░░███░        |                 |                 |                 |                 |
| 2.3  | API Implementation (integrate Lotte Smart OTP API)       | 5 days              |                 |                 | ░░░░░░██        | ███░░░░░        |                 |                 |                 |
| 2.4  | Frontend Implementation on NHSV Pro (iOS & Android)      | 7 days              |                 |                 |                 | ░░░█████        | ███░░░░░        |                 |                 |
| 2.5  | Internal Testing + Joint UAT with Lotte                  | 5 days              |                 |                 |                 |                 | ░░░█████        | ███░░░░░        |                 |
| 2.6  | Bug fixing & Optimization                                | 3 days              |                 |                 |                 |                 |                 | ░░░░████        |                 |
| 2.7  | Go-Live Smart OTP on NHSV Pro                            | 1 day               |                 |                 |                 |                 |                 | ░░░░░░░█        |                 |

### Total Duration - Option 1

- **Lotte Implementation:** 14 days (~3 weeks)
- **NHSV Implementation:** 26 days (~5 weeks)
- **Total Project Duration:** ~6 weeks (with 1 week buffer)

### Key Characteristics - Option 1

✅ **Advantages:**
- Simpler scope - no changes to login flow
- Lower risk - existing authentication remains intact
- Faster time-to-market
- Easier rollback if issues occur
- Users can gradually adopt Smart OTP for transactions

⚠️ **Limitations:**
- Users still need SMS OTP for login
- No complete passwordless experience
- Limited Smart OTP adoption initially

---

## Option 2: Smart OTP with Login Integration

**Scope:** Full Smart OTP implementation including login authentication. Users can log in using Smart OTP instead of SMS OTP. After login, Smart OTP is also used for transaction authorization. This provides complete passwordless authentication experience.

### Proposed Timeline - Option 2

| Seq. | Task                                                      | Expected / Executed | 4th week (Feb.) | 1st week (Mar.) | 2nd week (Mar.) | 3rd week (Mar.) | 4th week (Mar.) | 1st week (Apr.) | 2nd week (Apr.) |
|------|-----------------------------------------------------------|---------------------|-----------------|-----------------|-----------------|-----------------|-----------------|-----------------|-----------------|
| **Part 1: Lotte Implementation** |                                       |                     |                 |                 |                 |                 |                 |                 |                 |
| 1.1  | Receiving & Review final API specification from Lotte    | 3 days              | ████░░░░        |                 |                 |                 |                 |                 |                 |
| 1.2  | Assign staff in charge (Lotte side)                      | 1 day               | ░░░████░        |                 |                 |                 |                 |                 |                 |
| 1.3  | Review & confirm Smart OTP API contract (incl. login)    | 3 days              | ░░░░░███        | █░░░░░░░        |                 |                 |                 |                 |                 |
| 1.4  | Lotte API Development & Internal Testing                 | 10 days             |                 | ░████████       | ███░░░░░        |                 |                 |                 |                 |
| 1.5  | Lotte provides Sandbox API for NHSV                      | 1 day               |                 |                 | ░░░█░░░░        |                 |                 |                 |                 |
| **Part 2: NHSV Implementation** |                                        |                     |                 |                 |                 |                 |                 |                 |                 |
| 2.1  | UI/UX Design for Smart OTP flows (login + transaction)  | 5 days              |                 |                 | ░░░░████        | ███░░░░░        |                 |                 |                 |
| 2.2  | Define TradeX API specifications & integration plan      | 3 days              |                 |                 |                 | ░░░███░░        |                 |                 |                 |
| 2.3  | API Implementation - Login flow with Smart OTP           | 5 days              |                 |                 |                 | ░░░░░███        | ██░░░░░░        |                 |                 |
| 2.4  | API Implementation - Transaction authorization           | 4 days              |                 |                 |                 |                 | ░░████░░        |                 |                 |
| 2.5  | Frontend Implementation - Login screens (iOS & Android)  | 5 days              |                 |                 |                 |                 | ░░░░░███        | ██░░░░░░        |                 |
| 2.6  | Frontend Implementation - Transaction flows              | 5 days              |                 |                 |                 |                 |                 | ░░█████░        |                 |
| 2.7  | Session management & security hardening                  | 3 days              |                 |                 |                 |                 |                 | ░░░░░░██        | █░░░░░░░        |
| 2.8  | Internal Testing + Joint UAT with Lotte                  | 7 days              |                 |                 |                 |                 |                 |                 | ░████████       |
| 2.9  | Bug fixing & Optimization                                | 5 days              |                 |                 |                 |                 |                 |                 | ░░░░░░░░        |
| 2.10 | Go-Live Smart OTP on NHSV Pro                            | 1 day               |                 |                 |                 |                 |                 |                 | ░░░░░░░█        |

### Total Duration - Option 2

- **Lotte Implementation:** 18 days (~3.5 weeks)
- **NHSV Implementation:** 43 days (~8.5 weeks)
- **Total Project Duration:** ~10 weeks (with 1.5 week buffer)

### Key Characteristics - Option 2

✅ **Advantages:**
- Complete passwordless authentication experience
- Higher user satisfaction - single OTP method for everything
- Modern security standard
- Competitive advantage over other trading apps
- Better brand positioning

⚠️ **Considerations:**
- Longer implementation time (+4 weeks vs Option 1)
- Higher complexity in session management
- More extensive testing required
- Requires careful rollout strategy (phased launch recommended)
- Need fallback mechanisms for Smart OTP failures

---

## Side-by-Side Comparison

| Aspect                          | Option 1 (Transaction Only)      | Option 2 (Login + Transaction)   |
|---------------------------------|----------------------------------|----------------------------------|
| **Total Duration**              | ~6 weeks                         | ~10 weeks                        |
| **Lotte Implementation**        | 14 days                          | 18 days                          |
| **NHSV Implementation**         | 26 days                          | 43 days                          |
| **Complexity**                  | ⭐⭐ (Medium)                     | ⭐⭐⭐⭐ (High)                    |
| **Risk Level**                  | 🟢 Low                           | 🟡 Medium                        |
| **User Impact**                 | ⭐⭐⭐ (Partial improvement)      | ⭐⭐⭐⭐⭐ (Full experience)       |
| **Business Value**              | ⭐⭐⭐ (Incremental)              | ⭐⭐⭐⭐⭐ (Strategic)             |
| **Testing Effort**              | ⭐⭐ (Standard)                   | ⭐⭐⭐⭐ (Extensive)               |
| **Rollback Complexity**         | 🟢 Easy                          | 🟡 Moderate                      |

---

## Recommendation

### Strategic Approach: Phased Implementation

Consider implementing in 2 phases:

1. **Phase 1 (Option 1):** Launch Smart OTP for transactions first
   - Time: 6 weeks
   - Lower risk entry point
   - Users get familiar with Smart OTP concept
   - Team gains operational experience

2. **Phase 2 (Option 2 delta):** Add login integration later
   - Time: +4 weeks after Phase 1 stable
   - Builds on proven foundation
   - Can leverage user feedback from Phase 1
   - Lower perceived risk for stakeholders

### OR: Direct to Option 2 if:

- Business priority is competitive differentiation
- Marketing wants "full passwordless" message
- Willing to invest 10 weeks for complete solution
- Risk tolerance is adequate for larger scope

---

## Critical Success Factors

**For Either Option:**

1. **Lotte API Quality:** Stable, well-documented API is critical
2. **Joint UAT:** Must have dedicated time for cross-company testing
3. **Fallback Mechanisms:** SMS OTP must remain available as backup
4. **User Education:** In-app tutorials and support materials
5. **Monitoring:** Real-time alerts for Smart OTP failures
6. **Gradual Rollout:** Consider A/B testing or percentage rollout

---

## Next Steps

- [ ] Review timelines with engineering teams (BE, iOS, Android)
- [ ] Validate Lotte's capacity and timeline commitment
- [ ] Assess current sprint capacity for this project
- [ ] Decide: Option 1, Option 2, or Phased approach
- [ ] If Option 2: Define detailed rollout & rollback plan
- [ ] Create detailed API specification document
- [ ] Schedule kickoff meeting with Lotte counterpart

---

**Document Status:** 📋 Planning  
**For:** Product Team, Engineering Team, Business Stakeholders  
**Next Steps:** Decision meeting to select implementation approach
