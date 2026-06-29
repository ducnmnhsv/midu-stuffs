# Validator Report — BE Market Quote Fields Enhancement

## Result: PASS_WITH_WARNINGS

Draft validated against CLAUDE.md conventions and the authoritative `TradeX Knowledge/System/symbolinfo-api-fields-guide.md`. One correctness bug found and fixed; remaining items are open clarify-questions for BE grooming (not doc defects).

---

## Checklist

### Structure
- [x] Issues/ template: Executive Summary present with all 5 subsections (Problem Statement, Current vs Target, Solution Approach, Timeline, Success Criteria)
- [x] Executive Summary: NO code blocks (prose + tables only) — C3 PM-Readability Gate passed
- [x] Technical sections marked "(PM CAN SKIP)" — both Technical Background and Detailed Requirements
- [x] Footer present: `**Document Status:** | **For:** | **Next Steps:**`

### Content
- [x] Issue 1 (basis/`bs`): 41I1→VN30, 41I2→VN100 mapping present; ATO/ATC handling (expectedPrice vs currentPrice) covered; field name `bs` confirmed against guide
- [x] Issue 2 (asv/abv): aggressor logic, session reset, REST+socket all covered
- [x] Acceptance criteria present for both issues (Success Criteria 1–7)
- [x] No Lotte internal terms exposed without explanation (raw `parts[24]` only in Technical section, explained)

### Naming
- [x] Filename PascalCase, no brackets, no special prefixes: `BE_Market_Quote_Fields_Enhancement.md`
- [x] Field names verified against `symbolinfo-api-fields-guide.md`: `bs`=basis, `asv`=accumulatedSellVolume, `abv`=accumulatedBidVolume, `bc`=baseCode — all correct
- [x] Brief's `asb` → correctly mapped to convention `abv` (flagged for FE)

---

## Issues found and FIXED

### 🔴 CRITICAL (fixed): Aggressor-side mapping was reversed
- **Draft had:** `mb=ASK → mua chủ động → abv`, `mb=BID → bán chủ động → asv`
- **Authoritative guide (line 94) says:** `ASK = bên bán chủ động`, `BID = bên mua chủ động`
- **Correct mapping:** `mb=ASK → asv` (aggressive sell), `mb=BID → abv` (aggressive buy)
- **Fix applied:** Corrected the aggressor-side table; added an inline warning noting the FE brief had it backwards and that the Knowledge guide is authoritative (C4 Knowledge-First).
- **Follow-on:** Added a new clarify-question (#2) so FE is explicitly asked to confirm direction; renumbered subsequent questions (now 7 total); updated header count and Next Steps footer.

### Minor (fixed): Status line
- Changed footer status from "🟡 Draft — chờ validator review" to "✅ Validated — sẵn sàng cho BE grooming".

---

## Warnings (no action — open questions for BE, correctly surfaced in doc)

These are legitimately unresolved and are listed as clarify-questions, not doc errors:
1. `41I1`/`41I2` → VN30/VN100 prefix rule unverified in TradeX Knowledge (came from FE spec).
2. `market.quote.dr` channel suffix not in Knowledge.
3. VN100 futures existence unconfirmed.
4. REST derivatives field-group exposure unconfirmed.

FE codebase `nhsv-mts-rn` was not accessible during analysis — FE references unverified (already disclosed in the doc's source note).

C2 (Order API response standards) correctly NOT applied — this is market data, not an Order API.

---

## Final file path

`/Users/ducnguyen/Documents/project/tradex-monitoring/Derivatives/Planning documentation/Market data/Issues/BE_Market_Quote_Fields_Enhancement.md`
