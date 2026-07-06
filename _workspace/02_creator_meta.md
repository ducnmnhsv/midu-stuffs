# Creator Metadata — eKYC Journey Logging

**Destination path:** `/Users/ducnguyen/Documents/project/tradex-monitoring/eKYC/Planning/eKYC_Journey_Logging.md`

**Doc type:** Planning (PRD-level, prose only per Rule C3)

**Title:** eKYC Journey Logging — Planning

**Filename convention:** PascalCase + underscore (per CLAUDE.md Derivatives doc naming rule, applied consistently across areas).

**Folder rationale:**
- eKYC feature area already exists at repo root (`/eKYC/`), with a `Planning/` subfolder containing `PRD_eKYC_v2.md`.
- Sub-features 01, 02, 05 (biometric log, admin history, terms checkbox log) are the closest siblings — Journey Logging is a broader umbrella that intersects with all three.
- Placed at the top-level `eKYC/Planning/` (not under a sub-feature) because Journey Logging is cross-cutting: it spans multiple existing sub-features and 11 different APIs across 2 services.
- Alternative considered: `Smart-OTP/Planning/` — rejected because Smart-OTP is a separate feature (multi-channel OTP), not the eKYC journey. eKYC uses ekyc-admin service (OTPService inside ekyc-admin), which is distinct from the Smart-OTP feature.

**Rules applied:**
- C3 (PM-readability): NO code blocks, prose only. ✅
- C4 (Knowledge-first): Analyst findings sourced from `Knowledge/TradeX-MCP/` and `Knowledge/TradeX/`. ✅
- C5 (Doc footer): Ends with `Document Status: 📋 Draft | For: PM / BE Lead / Compliance | Next Steps: ...`. ✅ (applied even though this is not Derivatives — good hygiene)
- Markdown standard: CommonMark strict, ATX headers (`#`). ✅
- Naming: PascalCase + underscore (`eKYC_Journey_Logging.md`). ✅
- No brackets or prefix. ✅

**Sections included (all 6 required by team lead):**
1. Mục đích và phạm vi
2. Hành trình mở tài khoản (các bước)
3. Những gì cần log (theo từng bước/API)
4. Storage strategy (trigger, retention, DB table, producer, admin visibility)
5. Những gì KHÔNG cần log (scope exclusion)
6. Open questions / quyết định còn chờ

**Cross-references to existing sub-features:**
- `01_Biometric_Attempt_Log` — VNPT SDK structured log (companion, potentially same table).
- `05_Contract_Terms_Checkbox_Log` — Terms checkbox timestamp (companion, same journey).
- `02_Admin_Attempt_History` — noted explicitly that Journey Logging does NOT surface on this admin UI (per PM requirement).
- `03_Admin_Dashboard_Analytics` — same note.

**Known deviations / caveats to flag to validator:**
- Doc references sub-feature schemas (`ekyc_attempt_log`) without pinning exact column names — Planning-level is prose only and does not attempt DB spec. Backend_Spec.md (separate doc, next phase) will finalize schema.
- Storage numbers in Open Question #8 are order-of-magnitude estimates, not from analytics — labeled as "ước lượng" to be clear.
- Rule C5 says footer required for Derivatives. eKYC is not Derivatives; footer added anyway for consistency.
