# FE Overview - SmartOTP End-To-End Flow

## Reference

- Logic source: `Smart OTP - multi channels/Quy_trinh_S_OTP.md`
- Scope analysis: `Smart OTP - multi channels/SmartOTP_WTS_HTS_Scope_Analysis.md`
- Smart OTP menu design: [Figma - Smart OTP](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40008664-236501&t=oC0STJTkSr41WfqM-11)

## Purpose

This document gives FE developers the full SmartOTP flow before reading each detailed issue.

Phase 1 goal:

- NHSV Pro app is the SmartOTP registration and code generation device.
- WTS/HTS consumes the generated SmartOTP code.
- NHSV Pro app login by SmartOTP is not included in this phase.

## Core Rules

- SmartOTP is integrated by SDK, not direct REST API calls from FE.
- NHSV does not have SDK source code yet, so SDK method names, payloads, error codes, and storage behavior must be confirmed with partner.
- One account can activate SmartOTP on only one MTS device at a time.
- FE must detect SmartOTP activation status using `sotpStatus` / `sotpKey` returned by TradeX `login` / `verifyOTP` (when available) and compare with `localSotpKey` in secure storage to infer **active on this device vs another device**.
- Before login, user can only use `Lấy mã Smart OTP`.
- After login, user can access full Smart OTP screen from `More`.
- `Kích hoạt Smart OTP`, `Đổi PIN Smart OTP`, `Reset PIN Smart OTP`, and `Kích hoạt lại SmartOTP` require user to be logged in.
- Reset PIN does not create a new PIN directly. After reset success, app navigates directly to activation flow.

## Status Detection Rule (New Requirement)

TradeX login response provides 2 fields (mapped from Lotte):

- `userInfo.sotpStatus` (example: `"Y"` / `"N"`)
- `userInfo.sotpKey` (server key)

FE secure storage provides:

- `localSotpKey` (stored on device after successful activation on this device)

Decision:

| Condition | Interpretation |
| --- | --- |
| `sotpStatus !== "Y"` | Not activated |
| `sotpStatus === "Y"` AND `localSotpKey === sotpKey` | Activated on current device |
| `sotpStatus === "Y"` AND `localSotpKey` missing OR mismatch | Activated on another device (or this device lost local state) |

### Detection Flow Diagram

```mermaid
flowchart TD
  A[After login: receive userInfo] --> B[Read sotpStatus, sotpKey]
  B --> C[Read localSotpKey from secure storage]
  C --> D{Is sotpStatus == Y?}
  D -->|No| E[Not activated]
  D -->|Yes| F{localSotpKey exists and matches sotpKey?}
  F -->|Yes| G[Activated on current device]
  F -->|No| H[Activated on another device OR lost local state]
```

## Smart OTP Screen

Access after login:

1. User logs in by the current supported method.
2. User opens `More`.
3. User taps `Smart OTP`.
4. App displays Smart OTP screen with 4 main functions:
   - `Kích hoạt Smart OTP`
   - `Lấy mã Smart OTP`
   - `Đổi PIN Smart OTP`
   - `Reset PIN Smart OTP`

Figma also shows `Authenticate via biometric`, but it is not part of the 4 SmartOTP issues unless product confirms separate scope.

## Issue Map

| Issue | Function | Login required | Main purpose |
| --- | --- | --- | --- |
| `01_FE_Issue_Kich_Hoat_SmartOTP.md` | Kích hoạt Smart OTP | Yes | Register current MTS device |
| `02_FE_Issue_Lay_Ma_SmartOTP.md` | Lấy mã Smart OTP | No for get-code entry, Yes for More menu entry | Generate SmartOTP code for WTS/HTS |
| `03_FE_Issue_Reset_PIN_SmartOTP.md` | Reset PIN Smart OTP | Yes | Reset/deactivate current SmartOTP and navigate to activation |
| `04_FE_Issue_Change_PIN_SmartOTP.md` | Đổi PIN Smart OTP | Yes | Change SmartOTP PIN on active device |
| `05_FE_Issue_Kich_Hoat_Lai_SmartOTP.md` | Kích hoạt lại SmartOTP | Yes | Re-register after reset, locked, reinstall, or device transfer |

## End-To-End Flowchart

```mermaid
flowchart TD
  A[User opens NHSV Pro app] --> B{User logged in?}

  B -->|No| C[Login screen]
  C --> D[Only expose Lấy mã Smart OTP]
  D --> E[User taps Lấy mã Smart OTP]
  E --> F[Read local eligible account list]
  F --> G{Has eligible account?}
  G -->|No| H[Show empty state: login and activate SmartOTP first]
  G -->|Yes| I[Show account selector]
  I --> J[User selects account]
  J --> K[SDK check SmartOTP status]

  B -->|Yes| L[Home / More]
  L --> M[Tap Smart OTP]
  M --> N[Smart OTP screen with 4 functions]
  N --> O{Selected function}

  O -->|Kích hoạt| P[SDK check SmartOTP status]
  O -->|Lấy mã| K
  O -->|Đổi PIN| Q[SDK check SmartOTP status]
  O -->|Reset PIN| R[SDK check SmartOTP status]

  P --> P1{Activation status}
  P1 -->|Active current device| P2[Show already activated warning]
  P1 -->|Active another device| P3[Show transfer device confirmation]
  P3 --> P4{User confirms transfer?}
  P4 -->|No| N
  P4 -->|Yes| P5[Activation input flow]
  P1 -->|Not activated| P5
  P5 --> P6[SDK send SMS OTP]
  P6 --> P7[Input OTP + new PIN + confirm PIN]
  P7 --> P8{OTP and PIN valid?}
  P8 -->|OTP wrong under 5| P9[Show incorrect OTP message]
  P8 -->|OTP wrong 5 times| P10[Show warning and logout/back to login]
  P8 -->|OTP expired 60s| P11[Allow resend OTP]
  P8 -->|Valid| P12[SDK activate/reactivate SmartOTP]
  P12 --> P13[Save secure local SmartOTP state]
  P13 --> P14[Inactive old device if any]
  P14 --> P15[Show activation success]
  P15 --> N

  K --> K1{Get-code status}
  K1 -->|Not activated| K2[Show activate SmartOTP required popup]
  K1 -->|Active another device| K3[Show get code on registered device popup]
  K1 -->|Locked/need reactivation| K4[Guide user to login and reactivate]
  K1 -->|Active current device| K5[Show PIN input]
  K5 --> K6{PIN result}
  K6 -->|Wrong 1-4 times| K7[Show incorrect PIN message]
  K6 -->|Wrong 5 times| K8[Show warning and require reactivation]
  K6 -->|Correct| K9[SDK generate SmartOTP code]
  K9 --> K10[Display code + 60s countdown]
  K10 --> K11{Code expires?}
  K11 -->|After-login flow| K12[Show generate new code button]
  K11 -->|Pre-login flow| K13[Auto-generate new code]
  K10 --> K14[User enters code on WTS/HTS]
  K14 --> K15{WTS/HTS verify}
  K15 -->|Success| K16[Authentication success on WTS/HTS]
  K15 -->|Failed/expired/used| K17[WTS/HTS shows backend error]
  K15 -->|Wrong over 5 times| K18[Backend/SDK requires reactivation]

  Q --> Q1{Change PIN status}
  Q1 -->|Not activated| Q2[Show activate SmartOTP required popup]
  Q1 -->|Active another device| Q3[Show registered-device-only warning]
  Q1 -->|Locked/need reactivation| Q4[Start reactivation flow after user confirms]
  Q1 -->|Active current device| Q5[Show Change PIN form]
  Q5 --> Q6[Input current PIN + new PIN + confirm PIN]
  Q6 --> Q7{Input valid?}
  Q7 -->|Invalid| Q8[Show validation error]
  Q7 -->|Valid| Q9[SDK verify current PIN]
  Q9 --> Q10{Current PIN correct?}
  Q10 -->|No| Q11[Show incorrect PIN message]
  Q10 -->|Yes| Q12[SDK change PIN]
  Q12 --> Q13{Change PIN success?}
  Q13 -->|No| Q14[Show SDK error]
  Q13 -->|Yes| Q15[Show change PIN success]
  Q15 --> N

  R --> R1{Reset status}
  R1 -->|Not activated| R2[Show activate SmartOTP required popup]
  R1 -->|Active another device| R3[Show SDK status message, do not clear local state]
  R1 -->|Active current device| R4[Show reset confirmation]
  R4 --> R5{User confirms reset?}
  R5 -->|No| N
  R5 -->|Yes| R6[SDK reset/deactivate SmartOTP]
  R6 --> R7{Reset success?}
  R7 -->|No| R8[Show SDK error, keep local state]
  R7 -->|Yes| R9[Delete local SmartOTP state]
  R9 --> R10{Delete local success?}
  R10 -->|No| R11[Show technical error]
  R10 -->|Yes| P5

  Q4 --> P5
  K4 --> C
  K8 --> C
  K18 --> C
```

## SDK Contract Checklist

Ask partner to provide SDK contract before FE implementation:

- Check SmartOTP status:
  - Inputs: account, device ID, channel/context.
  - Outputs: not activated, active current device, active another device, locked, reset, need reactivation.
- Activation/reactivation:
  - Send SMS OTP.
  - Verify SMS OTP.
  - Activate/reactivate current device.
  - Inactive old device.
- Generate SmartOTP:
  - Verify/unlock PIN.
  - Generate code.
  - Countdown behavior.
  - Auto-regenerate behavior.
  - One-time-use behavior.
- PIN management:
  - Change PIN.
  - Reset/deactivate.
  - Incorrect PIN counter.
- Errors:
  - OTP incorrect.
  - OTP expired.
  - PIN incorrect.
  - Locked/need reactivation.
  - Active on another device.
  - SDK timeout/network/internal failure.
- Local storage:
  - Whether SDK fully owns secure storage.
  - Whether app must store eligible account list.
  - Whether app must clear local data after reset.

## Global Error Rules

| Case | Global behavior |
| --- | --- |
| Before login and user wants SmartOTP function other than get-code | Do not expose the function |
| Not activated | Show activate SmartOTP required message |
| Active on another device | Show registered-device or transfer warning depending on flow |
| OTP SMS expires after 60 seconds | Allow resend OTP |
| OTP SMS incorrect 5 times | Show warning and logout/back to login |
| PIN incorrect 1-4 times | Show incorrect PIN message |
| PIN incorrect 5 times | Require login with current method and SmartOTP reactivation |
| Reset success | Navigate directly to activation flow |
| Reinstall app | Treat as new device and require reactivation |
| SDK failure | Show SDK error and do not mark flow as success |

## Detailed Issues

Read these issue files for implementation detail:

1. `01_FE_Issue_Kich_Hoat_SmartOTP.md`
2. `02_FE_Issue_Lay_Ma_SmartOTP.md`
3. `03_FE_Issue_Reset_PIN_SmartOTP.md`
4. `04_FE_Issue_Change_PIN_SmartOTP.md`
5. `05_FE_Issue_Kich_Hoat_Lai_SmartOTP.md`

---

Document Status: 📋 | For: PM/Dev | Next Steps: Review nội dung, cập nhật status trên Tracking/tasks.js
