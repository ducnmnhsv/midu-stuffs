# [Epic DR-FE-CASH] Story CASH.S1: VSD Transaction – Balance, Deposit, Withdraw, History

> **Jira:** _(NHMTS-xxx)_ | **Epic:** DR-FE-CASH | **Module:** Cash (VSD) | **Priority:** P0

Add the VSD flow in Cash Transaction (Derivatives): **VSD Balance** + **Deposit** (nộp tiền ký quỹ) + **Withdraw** (rút tiền ký quỹ) + **History**.

**Figma:** [Deposit Form+Fee](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005124-206373) · [Deposit Confirm](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005124-206526) · [Withdraw](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005260-222646) · [History](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005084-245190)  
**API Spec:** [VSD_Transaction_API_Spec](../VSD%20transaction/VSD_Transaction_API_Spec.md)

---

## Acceptance criteria

**VSD Balance**

- When user enters VSD screen (Deposit or Withdraw), call GET `/api/v1/derivatives/transfer/vsd/balance`
- Query params: `accountNumber` (selected account), `inquiryDate` (yyyyMMdd, e.g. today)
- Display: **Cash balance at VSD** ← `margin_cash_balance_vsd`, **Withdrawable from VSD** ← `withdrawable_margin_cash`
- Format numbers with thousand separators (e.g. 50,000,000)
- Refresh balance again after successful Deposit or Withdraw

**Deposit (Nộp tiền ký quỹ)**

- Add feature in VSD section: Deposit form (display as Figma – Deposit Form+Fee)
- **Amount:** User can type amount or tap **"All"** → set amount = **Cash balance at VSD** (`margin_cash_balance_vsd`)
- After user enters/selects amount (e.g. on blur or after "All"), call POST `/api/v1/derivatives/transfer/vsd/deposit/fee`  
  Body: `accountNumber`, `subNumber` (default `"00"`), `amount`
- Display estimated fee from response: `feeAmount`, `adjustedAmount`, `receivedAmount`, `feeType` (store for submit)
- If fee API fails → show error message, do not allow Deposit submit
- **Deposit button:** Validate required: `accountNumber`, `amount`, and fee fields from above. If invalid → show inline error (e.g. "Field is required"), do not open dialog
- If valid → show **Confirmation dialog** (as Figma – Deposit Confirm). Dialog shows summary (Account, Amount, Fee, Received amount, etc.)
- On **Confirm** in dialog: POST `/api/v1/derivatives/transfer/vsd/deposit` with body: `accountNumber`, `subNumber`, `amount`, `note` (optional), `feeAmount`, `adjustedAmount`, `receivedAmount`, `feeType` (from fee API response)
- Loading: show spinner on Confirm, disable Cancel/Confirm to avoid double submit
- Success (200) → close dialog, toast success, refresh VSD balance, reset form
- Error (4xx/5xx) → close dialog, show response `message`, keep form data

**Withdraw (Rút tiền ký quỹ)**

- Add feature in VSD section: Withdraw form (display as Figma – Withdraw)
- **Amount:** User can type or tap **"All"** → set amount = **Withdrawable from VSD** (`withdrawable_margin_cash`)
- No fee API for Withdraw
- **Withdraw button:** Validate required: `accountNumber`, `amount`; `amount` > 0; `amount` ≤ `withdrawable_margin_cash`. If invalid → show error
- If valid → show Confirmation dialog (as Figma – Withdraw). On **Confirm**: POST `/api/v1/derivatives/transfer/vsd/withdraw` with `accountNumber`, `amount`, `note` (optional)
- Success → close dialog, toast, refresh VSD balance, reset form. Error → close dialog, show `message`

**VSD History**

- Add History in VSD section (display as Figma – History)
- When user opens History tab/section, call GET `/api/v1/derivatives/transfer/vsd/history`
- Query params: `accountNumber`, `transactionType` (C05 = Deposit, C10 = Withdraw, % = All), `fromDate`, `toDate` (yyyyMMdd), `nextData` (for next page)
- **Filter**
  - Default: date range e.g. last 7 days or 90 days (confirm with product); `transactionType` = ALL (%)
  - If user selects `fromDate` > `toDate` → set `fromDate` = `toDate`
  - Do not allow future date (toDate ≤ today)
- Response: `items` (list), `nextData` (pagination). Map fields per API spec §8 (accountNumber, transactionType, amount, note, registrationDate, feeAmount, receivedAmount, statusVTB, statusBOS, statusVSD, etc.). Format amounts with thousand separators
- If `nextData` is not empty → "Load more" or infinite scroll: call same API with same params + `nextData` from previous response, append to list
- If `items` is empty → show empty state (as Figma)
- If API error → show response `message`, do not clear already loaded list

**General**

- Account/sub: use the account (and sub if needed) that user selected before entering this feature
- All APIs: use JWT (Authorization header). See [VSD_Transaction_API_Spec](../VSD%20transaction/VSD_Transaction_API_Spec.md) for request/response and error format. FE does not send bank accounts; BE resolves them.

---

## References

- **API spec:** [VSD_Transaction_API_Spec.md](../VSD%20transaction/VSD_Transaction_API_Spec.md)
- **FE repo (reference):** `nhsv-mts-rn` → `src/screens/CashFlowScreen/` (HeaderCashFlow, BodyCashFlow, InternalTransferRequest, WithdrawRequest, WithdrawHistory). Add VSD tab/section and VSD Balance, Deposit, Withdraw, History components.
