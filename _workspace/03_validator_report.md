# Validator Report

**Kết quả:** PASS_WITH_WARNINGS

Subject: Sub Account Withdrawal API Specification (DRACC-039/040/041) — derivatives external bank withdrawal.

## Chi tiết kiểm tra

- [PASS] Integration type declared: `**Integration type:** Lotte-integrated (via lotte-bridge → Core Lotte)` — present at top per convention §Integration Type.
- [PASS] URL naming: `/api/v1/derivatives/transfer/cash/withdraw{,/history,/balance}` — camelCase path segments, correctly nested under the existing `transfer/cash` namespace (sibling to Internal Transfer's `POST /transfer/cash` + `GET /transfer/cash/history`) without colliding with those routes. Matches the pre-existing `rest-proxy-main` swagger stub path (flagged as Open Question #5 for PM, not a convention violation).
- [PASS] Response format — mutation (Execute Withdrawal): `{ "success": true }`. This is the documented VSD Withdraw/Deposit exception to the "no success field" rule, correctly cited and reused (not a new deviation).
- [PASS] Response format — query (History): `{ items: [...], nextData }`, matches VSD History pattern exactly.
- [PASS] Response format — query (Balance): flat single object (not wrapped in `items`/`totalCount`), justified as "always 1 row/account+sub" — matches VSD Balance's own flat-object precedent (§4.3 of VSD spec), so this is not a new deviation from convention, it's consistent with the established sibling exception.
- [PASS] No-Core-names rule: `status` filter uses `PENDING`/`REJECTED`/`APPROVED` with an explicit mapping table to Lotte `proc_tp` (`i`/`j`/`k`); no raw Lotte codes exposed as TradeX values.
- [PASS] Error code style (Lotte-integrated): `DERIVATIVES_CASH_WITHDRAW_{HISTORY|BALANCE}_{code}` — matches the `DERIVATIVES_VSD_{OP}_{code}` pattern established by the sibling VSD spec and the parent README's own example (`DERIVATIVES_CASH_TRANSFER_1005`).
- [PASS] DTO naming pattern `{Resource}{Action}Request/Response`: `DerivativesCashWithdrawHistoryRequest/Response`, `DerivativesCashWithdrawExecuteRequest/Response`, `DerivativesCashWithdrawBalanceRequest/Response` all follow Resource+Action structurally.
- [WARN] DTO naming — domain token style: creator flagged that VSD uses a neutral domain token (`Derivatives<Vsd><Action>`) while this spec folds the action into the domain token itself (`Derivatives<CashWithdraw><Action>`), producing a mild stutter in `DerivativesCashWithdrawExecuteRequest` ("CashWithdraw" + "Execute"). This is judged **acceptable as-is**, not fixed: unlike VSD (5 operations: deposit/withdraw/balance/banks/fee sharing one neutral domain), this spec's domain genuinely *is* "cash withdrawal" with only 3 operations, one of which (Execute) is the eponymous action — there's no clean neutral-domain split available. Renaming (e.g. dropping "Execute" so the primary mutation is just `DerivativesCashWithdrawRequest/Response`) is a legitimate alternative but is a naming-scheme decision, not a violation, so left for PM/dev judgment per the "chỉ báo cáo" scope of this checklist.
- [FIXED] Missing DTO: `DerivativesCashWithdrawBalanceRequest` was absent from the DRACC-041 implementation checklist (§8.3) — VSD's equivalent (`DerivativesVsdBalanceRequest` + `Response`) creates both Request and Response DTOs; this spec only listed the Response DTO. Added `DerivativesCashWithdrawBalanceRequest` to the checklist for consistency.
- [FIXED] Markdown — fenced code block missing language ID: the "Response Logic" pseudo-code block in §4.3 (Execute Withdrawal) was a bare ` ``` ` fence; the sibling VSD spec tags the equivalent block ` ```typescript `. Added the `typescript` language tag.
- [PASS] Document footer (C5): API Spec ends with `**Document Status:** | **For:** | **Next Steps:**` in the required format.
- [PASS] README footer style: sub-module README ends with `Prepared By / Document Version / Last Review` — no C5-style footer, but this matches the existing style of all 3 sibling READMEs (Internal transfer, VSD transaction, and the parent Cash Transaction README itself use the same non-C5 footer). C5 applies to specs/issues; READMEs in this folder are not held to that format elsewhere, so this is consistent, not a violation.
- [PASS] Markdown conventions: ATX headers throughout, JSON example blocks all tagged ` ```json `, ASCII flow-diagram blocks left untagged (consistent with existing convention docs' own usage for non-code text diagrams, e.g. regular-order-api-mapping.md's Architecture Flow).
- [PASS] Planning-doc-prose-only rule (C3): N/A — this is a Specifications-type file, JSON/code examples are expected and correctly present per task note.
- [PASS] "Trường tự động điền" content: present inline (§8.2 point 6 "Auto-Population": userId/sourceIp/lang_code) rather than as a dedicated top-level section — matches the sibling VSD spec's own style (§11.2 point 5), not a template requirement violation.
- [INFO — not fixed, PM decision required] 6 Open Questions (§10) remain unresolved by design; Document Status correctly kept at 🔄 In Progress rather than ✅, per creator/task instruction. Not a convention issue — flagged here only so the PASS_WITH_WARNINGS verdict is understood as "convention-clean but business-incomplete," not "convention violations found."

## Đã sửa trong draft

- Added `DerivativesCashWithdrawBalanceRequest` DTO to the DRACC-041 implementation checklist (§8.3), matching VSD's Request+Response DTO pairing.
- Added `typescript` language tag to the untagged fenced code block in §4.3 "Response Logic".

## Vấn đề còn lại (Cần user xác nhận)

- DTO domain-token naming style (`CashWithdraw` + action vs VSD's neutral `Vsd` + action) — cosmetic, left as-is; PM/dev can rename before implementation if cross-spec uniformity is desired.
- All 6 Open Questions in §10 of the spec (Lotte field discrepancies, `bankAccount` source, `bankAcc` format, enum tables, stub-path reuse, fee) — unchanged, still require PM confirmation with HuongLT/VanND before implementation. Not validator-actionable.

## File đã lưu

- `Derivatives/Planning documentation/Cash transaction/External withdrawal/Sub_Account_Withdrawal_API_Spec.md`
- `Derivatives/Planning documentation/Cash transaction/External withdrawal/README.md`
- `Derivatives/Planning documentation/Cash transaction/README.md` (updated: Scope table Out-of-Scope line removed/replaced with in-scope entry, new "4. External Withdrawal" sub-module section, Implementation Status row, Documentation Map row)
