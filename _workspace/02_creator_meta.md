# Creator Meta: Sub Account Withdrawal (DRACC-039/040/041)

## Output Type

**Primary:** API Specification (technical spec, no User Story format, ✅/❌ required markers, JSON examples) — per `tradex-creator` skill "API Spec" classification and project convention (`Knowledge Locations`, `feedback_api_spec_format` memory).

**Secondary:** README.md for the new sub-module folder (pattern: `VSD transaction/README.md` / `Internal transfer/README.md`) — brand-new feature folder, no existing README. Kept secondary per task instructions; embedded as a clearly-delineated second block inside `02_creator_draft.md` (fenced under `# [SECONDARY DRAFT] README.md ...` heading), NOT a separate workspace file, to match the skill's 2-file output format.

## Destination Paths (for validator phase — do NOT write here yet)

This is a **brand-new sub-module** under `Derivatives/Planning documentation/Cash transaction/`, sibling to `Internal transfer/`, `VSD transaction/`, `Cash statement/`.

**Chosen folder name:** `External withdrawal` — Title Case + space, consistent with sibling naming style (`Cash statement`, `Internal transfer`, `VSD transaction`: first word capitalized, rest lowercase). Chosen over literal "Rút tiền" translation to stay in English per existing folder naming convention, and over "Withdrawal" alone to disambiguate from VSD's own "withdraw" (margin, not external bank).

| Draft section | Final destination path | Final filename |
|---|---|---|
| Main API Spec (top of `02_creator_draft.md`, sections 1–10) | `Derivatives/Planning documentation/Cash transaction/External withdrawal/` | `Sub_Account_Withdrawal_API_Spec.md` |
| README (bottom of `02_creator_draft.md`, fenced block) | `Derivatives/Planning documentation/Cash transaction/External withdrawal/` | `README.md` |

**File naming rationale:** PascalCase + underscore, consistent with `VSD_Transaction_API_Spec.md` / `Internal_Transfer_API_Spec.md`. Named `Sub_Account_Withdrawal_API_Spec.md` (not `External_Withdrawal_...`) because the feature is fundamentally about *sub-account* cash withdrawal (matches Jira/analyst naming "Rút tiền sub phái sinh" and DRACC field `sub_no`), while the *folder* is named "External withdrawal" to disambiguate from VSD at the navigation level. Validator should confirm this file/folder name split is acceptable, or unify both to the same term if preferred.

## Parent README Update Needed (not drafted here — flag for validator/PM)

`Derivatives/Planning documentation/Cash transaction/README.md` currently lists only 3 sub-modules (Internal Transfer, VSD Transaction, Cash Statement) in its "Sub-modules" and "Documentation Map" sections, and states "❌ External bank transfers" as **Out of Scope** in its Scope table (line ~34). Once this spec is finalized and moved, that parent README needs:
1. A new "4. External Withdrawal" entry in Sub-modules + Documentation Map sections.
2. Remove/update the "❌ External bank transfers — Out of Scope" line (now IN scope).
This was NOT edited in this creator pass — out of scope for this task per instructions (draft only goes to `_workspace/`), but the validator/PM should action it when moving the final spec.

## Key Decisions Made During Creator Pass

1. **Integration type:** Lotte-integrated (via `lotte-bridge` → Core Lotte) — declared per convention §Integration Type.
2. **Response format:**
   - DRACC-040 (Execute/mutation): `{ success: true }` boolean-only — matches VSD Withdraw (DRACC-009) precedent, NOT order-v2's tradex-native `{id}` pattern (this domain doesn't store to TradeX DB first).
   - DRACC-039 (History/query): `{ items: [...], nextData }` — matches VSD History pattern.
   - DRACC-041 (Balance/query): single flat object (not array, not wrapped in `items`) — because it's always exactly 1 row per account+sub.
3. **Endpoints used exactly as specified in the task/analyst proposal** (no deviation):
   - `GET /api/v1/derivatives/transfer/cash/withdraw/history` (DRACC-039)
   - `POST /api/v1/derivatives/transfer/cash/withdraw` (DRACC-040)
   - `GET /api/v1/derivatives/transfer/cash/withdraw/balance` (DRACC-041)
4. **DRACC-041 request fields:** Used analyst's "Input Sample as ground truth" decision (`accountNumber`, `subNumber` only) — explicitly flagged as Open Question #1 (doc-vs-sample discrepancy), NOT resolved by creator phase.
5. **`status` filter (DRACC-039):** Marked `fromDate`/`toDate`/`status` as Required (Y) per the analyst's field table sourced from Lotte doc — flagged in a callout that this differs from VSD History's optional+default-today pattern, in case PM wants UX consistency (bundled into Open Question #4 discussion).
6. **Did NOT reuse the stale `rest-proxy-main` stub schema** (`DerivativesCashWithdrawRequest`/`Response`, 4-field bank object) for DRACC-040 — fully rewritten per analyst's field-mismatch findings. DID reuse ~6/7 field names from the stub's `DerivativesCashWithdrawInfoResponse` for DRACC-041 balance response (per analyst recommendation), adding new `managementFeeBlockAmount`.
7. **Doc status set to 🔄 (In Progress)**, not ✅ — per task instruction, since all 6 Open Questions from analyst findings are carried forward verbatim into a dedicated §10 "Open Questions" section, unresolved.

## Notes for Validator Phase

- **Check camelCase URL convention:** all 3 endpoints use camelCase path segments (`transfer/cash/withdraw/history`, `/balance`) — consistent with `transfer/vsd/{balance,withdraw,history}` sibling pattern. Should PASS convention check.
- **Check DTO naming:** proposed new DTO names `DerivativesCashWithdrawHistoryRequest/Response`, `DerivativesCashWithdrawExecuteRequest/Response`, `DerivativesCashWithdrawBalanceResponse` — verify against `{Resource}{Action}Request/Response` pattern used elsewhere (e.g., `DerivativesVsdBalanceRequest`, `DerivativesVsdDepositRequest`). Slight naming inconsistency risk: VSD uses `Derivatives<Vsd><Action>` while this spec uses `Derivatives<CashWithdraw><Action>` — check if `<Vsd>`-equivalent domain token should be inserted (e.g., `DerivativesCashWithdrawal...`) for cross-spec consistency. Flagging for validator judgment, not auto-fixed here.
- **No Core names rule:** verified — `status` (PENDING/REJECTED/APPROVED) used instead of raw `proc_tp` (i/j/k); mapping table provided per §4 of `tradex-api-conventions.md`.
- **Response format standards:** verified against `tradex-api-conventions.md` §Response Format Standards — no stray `success`/`code:"0000"` in query responses; mutation uses documented VSD-precedent boolean exception.
- **6 Open Questions are carried forward verbatim** from `01_analyst_findings.md` into spec §10 — validator should NOT attempt to resolve these; they require PM → HuongLT/VanND confirmation outside this pipeline.
- **This spec must NOT be moved to final location yet** — draft only, pending Open Questions resolution and validator convention check.
