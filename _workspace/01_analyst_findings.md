# eKYC Journey Logging — Analyst Findings

**Analyst:** Claude (tradex-pipeline)
**Date:** 2026-07-04
**Sources verified:**
- `Knowledge/TradeX-MCP/lotte-bridge-main/src/services/EkycService.ts`
- `Knowledge/TradeX-MCP/lotte-bridge-main/src/consumers/RequestHandler.ts`
- `Knowledge/TradeX-MCP/ekyc-admin/src/main/java/com/techx/tradex/ekycadmin/consumers/RequestHandler.java`
- `Knowledge/TradeX-MCP/ekyc-admin/src/main/java/com/techx/tradex/ekycadmin/models/request/*.java`
- `Knowledge/TradeX-MCP/documents-main/API_spec_docs/TradeX API - Addons.yaml`

---

## 1. Service Mapping

| # | API | HTTP | Owner Service | Kafka Topic | Backend Method | Notes |
|---|-----|------|---------------|-------------|----------------|-------|
| 1 | `/api/v1/lotte/ekycs/create` | POST | **ekyc-admin** | `ekyc-admin` | `LotteEKycService.createEKycLotte()` | Kafka URI in code is `/api/v1/ekycs/create` — rest-proxy chuyển từ `/lotte/ekycs/create` → topic ekyc-admin. Trả `eKycId`. |
| 2 | `/api/v1/ekyc-admin/sendOtp` | POST | **ekyc-admin** | `ekyc-admin` | `OTPService.generateAndSendOtp()` | OTP tới phoneNo |
| 3 | `/api/v1/ekyc-admin/verifyOtp` | POST | **ekyc-admin** | `ekyc-admin` | `OTPService.VerifyOtp()` | Trả `otpKey` để confirm bước sau |
| 4 | `/api/v1/lotte/ekycs` | POST | **ekyc-admin** | `ekyc-admin` | Kafka URI `/api/v1/ekycs` → `customEKycService.addEKyc()` (chain sang Lotte + FPT eContract) | Submit toàn bộ form eKYC |
| 5 | `/api/v1/ekycs/banks` | GET | **lotte-bridge** | `lotte-bridge` | `EkycService.getBankList()` | Query Lotte code_tp=`bank_cd_off` |
| 6 | `/api/v1/ekycs/branch` | GET | **lotte-bridge** | `lotte-bridge` | `EkycService.getListBranch()` | Query Lotte code_tp=`brch_cd` (NHSV branches) |
| 7 | `/api/v1/ekycs/banks/{id}/branches` | GET | **lotte-bridge** | `lotte-bridge` | `EkycService.getBanksListBranch()` | Query Lotte code_tp=`bank_brch`, bank_code=id |
| 8 | `/api/v1/ekycs/partner` | GET | **lotte-bridge** | `lotte-bridge` | `EkycService.getPartnerName()` | Validate NHSV staff (partnerId) |
| 9 | `/api/v1/ekycs/account/exist` | GET | **lotte-bridge** | `lotte-bridge` | `EkycService.checkAccountOpeningStatus()` | Check trạng thái mở account tại Lotte (không có trong Addons.yaml — chỉ có trong source) |
| 10 | `/api/v1/equity/account/checkNationalId` | POST | **lotte-bridge** | `lotte-bridge` | `EkycService.checkNationalId()` | 2 bước: query `ekyc-admin` (internal) rồi Lotte |
| 11 | `/api/v1/equity/account/contracts` | GET | **ekyc-admin** | `ekyc-admin` | `EContractCustomService.signEContract()` | Trả FPT eContract webView URL (envelopeId, cookieValue…) |

**Rest-proxy** đóng vai gateway: nhận REST → publish Kafka message tới topic tương ứng. Route/topic mapping xác định bằng URI của message.

---

## 2. API Details

### 1. POST `/api/v1/lotte/ekycs/create`
- **Service:** `ekyc-admin` — `LotteEKycService.createEKycLotte(txId, EKycAddReq)`
- **Kafka URI (in-service):** `/api/v1/ekycs/create`
- **Purpose:** Tạo eKycId mới (application number) trên Lotte Core; validate phoneNo chưa tồn tại.
- **Auth:** JWT — thường dùng `grant_type=client_credentials` (demo account, chưa có user thật)
- **Request:**
  | Field | Type | Required | Ghi chú |
  |---|---|---|---|
  | `groupType` | enum `idv`/`org` | ✅ | idv=cá nhân, org=tổ chức |
  | `phoneNo` | string | ✅ | Số điện thoại (dùng cho OTP) |
  | `email` | string | ✅ | Email KH |
- **Response:** `{ eKycId: string }`
- **Errors:** 400 nếu phoneNo đã tồn tại (Lotte trả message tiếng Việt).

### 2. POST `/api/v1/ekyc-admin/sendOtp`
- **Service:** `ekyc-admin` — `OTPService.generateAndSendOtp(SendOtpRequest)`
- **Purpose:** Trigger gửi OTP tới phone.
- **Request:**
  | Field | Type | Required | Enum |
  |---|---|---|---|
  | `id` | string | ✅ | phone number, format ví dụ `+849198139801` |
  | `idType` | enum | ✅ | `PHONE_NO` |
  | `txType` | enum | ✅ | `E_KYC` |
- **Response:** `{ otpId: string, expiredTime: string (yyyyMMddHHmmss) }`
- **Errors:** `PHONENO_LOCK_INCORRECT_OTP_MAX`

### 3. POST `/api/v1/ekyc-admin/verifyOtp`
- **Service:** `ekyc-admin` — `OTPService.VerifyOtp(VerifyOtpRequest)`
- **Request:**
  | Field | Type | Required |
  |---|---|---|
  | `otpId` | string | ✅ (từ sendOtp) |
  | `otpValue` | string | ✅ (6 digit) |
- **Response:** `{ otpKey: string, expiredTime: string }`
- **Errors:** `INCORRECT_OTP`, `OTP_EXPIRED`, `INCORRECT_OTP_MAX`

### 4. POST `/api/v1/lotte/ekycs`
- **Service:** `ekyc-admin` — Kafka URI `/api/v1/ekycs` → `customEKycService.addEKyc(txId, EKycAddReq)` (chain: lưu DB `EKyc` entity → gọi Lotte add → gọi FPT eContract create envelope)
- **Purpose:** Submit form eKYC hoàn chỉnh sau khi user đã pass biometric (VNPT), verify OTP, chọn ngân hàng, tick TnC.
- **Request (fields chính, xem `EKycAddReq.java`):**
  - Identity: `eKycId`, `identifierId` (CCCD), `fullName`, `birthDay`, `gender`, `type` (CC/CMND), `issueDate`, `issuePlace`
  - Contact: `phoneNo`, `email`, `permanentAddress`, `contactAddress`, `permanentProvince/District`, `contactProvince/District`
  - Business: `branch` (NHSV branch code), `marginInclued`, `onlineTrading`, `authenMethod` (otp/token), `otpReceiveMethod` (email/express), `advancedCashIncluded`, `smsMethod` (basic/advanced), `emailNotification`, `derivativesIncluded`
  - Bank list: `bankList[]` — max 3 items: `{ bankId, bankName, bankAccNo, ownerName, branchId }`
  - Referral: `referral` (1-4), `partnerId`, `partnerName`, `customerSupport`, `csPartnerId`, `csName`
  - Images (VNPT upload URLs): `frontImageUrl`, `backImageUrl`, `portraitImageUrl`, `signatureImageUrl`, `tradingCodeImageUrl`
  - VNPT metadata: `matchingRate` (biometric score, min 80), `dataSign`, `dataBase64`, `ocrLogId`, `cardLivenessLogId`, `cardRearLogId`, `compareLogId`, `faceLivenessLogId`, `faceMaskLogId`
  - Compliance: `fatca`, `taxNo`, `beneficiaryOwner`, `investmentExperience` (goal/risk/experienced + publicCoop + blockholder)
  - Device: `deviceUniqueId`
- **Response:** `{ status: "success" }`
- **Errors:** `INVALID_PARAMETERS`, `MAX_BANKS_3`, `PUBLIC_COOP_MAX_*`, `BLOCKHOLDER_MAX_*`

### 5. GET `/api/v1/ekycs/banks`
- **Service:** `lotte-bridge` — `EkycService.getBankList(ctx)`
- **Backend call:** `LotteBankDao.getBankList({ code_tp: 'bank_cd_off' })`
- **Response:** `Array<{ bankCode: string, bankName: string }>`
- **Ví dụ:** `[{ bankCode: "0202", bankName: "BIDV" }, ...]`

### 6. GET `/api/v1/ekycs/branch`
- **Service:** `lotte-bridge` — `EkycService.getListBranch(ctx)`
- **Backend call:** `LotteBankDao.getBankList({ code_tp: 'brch_cd' })`
- **Response:** `Array<{ branchCode: string, branchName: string }>` — danh sách chi nhánh NHSV.

### 7. GET `/api/v1/ekycs/banks/{id}/branches`
- **Service:** `lotte-bridge` — `EkycService.getBanksListBranch(request, ctx)`
- **Path param:** `id` = bankCode (từ `/ekycs/banks`)
- **Backend call:** `LotteBankDao.getBanksListBranch({ code_tp: 'bank_brch', bank_code: id })`
- **Response:** `Array<{ branchCode: string, branchName: string }>` — chi nhánh của ngân hàng đó.
- ⚠️ **Note:** Trong Addons.yaml path param được viết là `{bankCode}`, code lotte-bridge routing dùng `{id}` — nội dung vẫn là bank code.

### 8. GET `/api/v1/ekycs/partner`
- **Service:** `lotte-bridge` — `EkycService.getPartnerName(request, ctx)`
- **Query:** `partnerId` (required)
- **Backend call:** `LotteBankDao.getPartnerName({ emp_no: partnerId, [brch_cd?] })`
- **Response 200:** `{ name: string }`
- **Errors:** `PARTNER_NOT_FOUND`
- **Dùng khi:** `referral = 1` (Nhân viên/CTV) — validate NHSV staff ID.

### 9. GET `/api/v1/ekycs/account/exist`
- **Service:** `lotte-bridge` — `EkycService.checkAccountOpeningStatus(request, ctx)`
- **Query:** `identifierId` (required)
- **Backend call:** `LotteBankDao.checkAccountOpeningStatus({ idno: identifierId })`
- **Response:** `{ exist: boolean }` — `true` nếu Lotte error_code=`0000` (đã có), `false` nếu `1005` (chưa có).
- ⚠️ **Không có trong Addons.yaml** — chỉ có trong source `lotte-bridge/src/services/EkycService.ts` line 140.

### 10. POST `/api/v1/equity/account/checkNationalId`
- **Service:** `lotte-bridge` — `EkycService.checkNationalId(id, ctx)`
- **Flow:** 2 bước
  1. Kafka call sang `ekyc-admin` topic, URI `internal:/api/v1/ekycs/get` với `{ identifierId, headers }` → kiểm tra record eKYC pending trong DB ekyc-admin. Nếu status = `WAITING_CONFIRMATION` → throw `EKYC_WAITING_CONFIRMATION`.
  2. Gọi Lotte `LotteOrderDao.checkAccountExist({ idno: identifierId })`.
- **Request:** `{ identifierId: string }`
- **Response 200:** `{ exist: false }` (nếu Lotte trả `0000` — nghĩa là chưa có account, có thể đăng ký).
- **Errors:** Nếu Lotte trả code ≠ 0000 → throw code Lotte (VD `V3101` — đã có tài khoản, liên hệ hotline).

### 11. GET `/api/v1/equity/account/contracts`
- **Service:** `ekyc-admin` — `EContractCustomService.signEContract(FptECSignRequest)`
- **Purpose:** Trả webView URL của FPT eContract để user ký hợp đồng mở tài khoản.
- **Query:**
  - `eKycId` (optional) — bắt buộc khi đang trong flow eKYC (login `client_credentials`); nếu user đã login bằng NHSV account thì lấy CCCD từ accessToken.
- **Response:** Envelope + webView payload:
  ```
  {
    timestamp, status, message,
    data: {
      items: [{
        envelopeId, envelopeStatus (processing/completed/rejected/voided),
        recipientStatus (processing/signed/rejected),
        contractInfo: { contractName, contractNo, createdDate, submittedFrom },
        webView: { url, cookieName, cookieValue, expireIn, iframeUrl }
      }]
    }
  }
  ```

---

## 3. eKYC Account-Opening Journey (End-to-End)

Dựa trên spec Addons.yaml + code + `PRD_eKYC_v2.md`:

### Bước 0 — Client credentials login
- App login với `grant_type=client_credentials` (demo account, chưa có user thật) để có JWT truy cập các API eKYC.

### Bước 1 — Nhập số điện thoại + OTP
1. User nhập **phoneNo** + email + groupType (idv/org) trên form.
2. Gọi `POST /api/v1/lotte/ekycs/create` → nhận `eKycId`.
3. Gọi `POST /api/v1/ekyc-admin/sendOtp` với `{id: phone, idType: PHONE_NO, txType: E_KYC}` → nhận `otpId`.
4. User nhận OTP SMS, nhập 6-digit → `POST /api/v1/ekyc-admin/verifyOtp` → nhận `otpKey` (dùng cho các bước sau).

### Bước 2 — VNPT Biometric (client-side SDK)
- User quét CCCD/CMND mặt trước + mặt sau + chân dung (portrait) + optional signature.
- VNPT SDK chạy: OCR (`ocrLogId`), card liveness (`cardLivenessLogId`), card rear (`cardRearLogId`), face compare (`compareLogId`), face liveness (`faceLivenessLogId`), mask check (`faceMaskLogId`).
- Kết quả gồm: OCR fields (identifierId, fullName, birthDay, issueDate, issuePlace, permanentAddress...), `matchingRate` (%), `dataSign`, `dataBase64`, images upload URL (`frontImageUrl`, `backImageUrl`, `portraitImageUrl`).
- Từ `PRD_eKYC_v2.md`: `matchingRate < 80` → BE reject. `CustomEKycService.java:211` hiện tại xóa record PENDING cũ khi retry → mất audit trail (đây là core problem eKYC v2 muốn fix).

### Bước 3 — Check National ID
5. Gọi `POST /api/v1/equity/account/checkNationalId` với `{identifierId}` → verify CCCD chưa có tài khoản NHSV.
   - BE query `ekyc-admin` internal + Lotte song song. Nếu Lotte `V3101` (đã có TK) → chặn.

### Bước 4 — Điền form + chọn Bank/Partner
6. Load bank list: `GET /api/v1/ekycs/banks` → dropdown.
7. Với bank được chọn: `GET /api/v1/ekycs/banks/{bankCode}/branches` → chọn chi nhánh.
8. Load NHSV branch: `GET /api/v1/ekycs/branch`.
9. Nếu `referral=1` (Nhân viên/CTV): `GET /api/v1/ekycs/partner?partnerId=xxx` → validate và fill `partnerName`.
10. User điền form: personal info còn thiếu, address, `marginInclued`, `onlineTrading`, `authenMethod`, `otpReceiveMethod`, `smsMethod`, `advancedCashIncluded`, `emailNotification`, `derivativesIncluded`, `fatca`, `taxNo`.
11. Thêm bank account(s) (max 3): `bankId`, `bankAccNo`, `ownerName`, `branchId`.
12. Investment experience: `investmentGoal`, `risk`, `experienced` + `publicCoop[]`, `blockholder[]` (compliance UBCK).
13. Beneficiary owner (nếu có).

### Bước 5 — Terms & Conditions
14. User tick các TnC checkbox (điều khoản mở TK, cam kết thông tin đúng, cam kết TT134/UBCK...).
    - Đây là điểm eKYC v2 muốn log lại (sub-feature `05_Contract_Terms_Checkbox_Log`).

### Bước 6 — Submit
15. `POST /api/v1/lotte/ekycs` với toàn bộ form + VNPT metadata + images URL + deviceUniqueId.
    - BE flow: lưu `EKyc` entity trong DB ekyc-admin → gọi Lotte createEKyc (upload images sang Lotte per `callUploadImage`) → gọi FPT eContract create envelope.
16. Response `{ status: "success" }`.

### Bước 7 — Ký hợp đồng điện tử (FPT eContract)
17. `GET /api/v1/equity/account/contracts?eKycId=xxx` → nhận webView URL + JWT cookie.
18. App mở iframe/webView, user ký trên FPT platform.
19. Poll status: `GET /api/v1/lotte/equity/account/contractStatus` (COMPLETED / PROCESSING / REJECTED / UNKNOWN).

### Bước 8 — Chờ approval + VSD
20. `GET /api/v1/lotte/equity/account/vsdStatus?accountNumber=...&subAccount=...` (PENDING / APPROVED / REJECTED / UNKNOWN).
21. Khi APPROVED: NHSV cấp `accountNumber` chính thức, user có thể login bằng account thật thay vì client_credentials.

---

## 4. Sequence tổng quát

```
[App] ──login client_credentials──> [rest-proxy] ──> [aaa]

[App] ─POST /lotte/ekycs/create ──> [rest-proxy] ─Kafka ekyc-admin (/api/v1/ekycs/create)─> [ekyc-admin]
       ← eKycId ──────────────────────────────────────────────────────────────

[App] ─POST /ekyc-admin/sendOtp ──> [rest-proxy] ─Kafka ekyc-admin (/api/v1/ekyc-admin/sendOtp)─> [ekyc-admin]
       ← { otpId, expiredTime }
[App] ─POST /ekyc-admin/verifyOtp ──> [ekyc-admin]
       ← { otpKey, expiredTime }

[App] ─VNPT SDK biometric locally─> [VNPT] (upload images to S3, get URLs + dataSign)

[App] ─POST /equity/account/checkNationalId ──> [lotte-bridge]
                                                    │ Kafka internal /api/v1/ekycs/get ─> [ekyc-admin] DB check
                                                    │ Lotte /account-exist                             │
       ← { exist: false } (nếu OK)                  └────────────────────────────────────────────────────

[App] ─GET /ekycs/banks, /ekycs/branch, /ekycs/banks/{id}/branches ──> [lotte-bridge] ──> [Lotte Core]
[App] ─GET /ekycs/partner?partnerId=xxx ──> [lotte-bridge]
[App] ─GET /ekycs/account/exist?identifierId=xxx ──> [lotte-bridge]

[App] ─POST /lotte/ekycs (form đầy đủ) ──> [rest-proxy] ─Kafka ekyc-admin (/api/v1/ekycs)─> [ekyc-admin]
                                                        │ Save EKyc entity DB
                                                        │ Call Lotte createEKyc (with images upload)
                                                        │ Call FPT eContract create envelope
       ← { status: "success" }

[App] ─GET /equity/account/contracts?eKycId=xxx ──> [ekyc-admin] ─> [FPT] ─> webView URL
[App] ─mở iframe FPT eContract, user ký

[App] ─GET /lotte/equity/account/contractStatus ──> [lotte-bridge]
[App] ─GET /lotte/equity/account/vsdStatus ──> [lotte-bridge]
```

---

## 5. Kafka topic summary (theo tradex-monitoring CLAUDE.md)

| Topic | Service | eKYC APIs handled |
|---|---|---|
| `ekyc-admin` | ekyc-admin (Java JHipster) | `/api/v1/lotte/ekycs/create`, `/api/v1/ekyc-admin/sendOtp`, `/api/v1/ekyc-admin/verifyOtp`, `/api/v1/lotte/ekycs`, `/api/v1/equity/account/contracts`, `internal:/api/v1/ekycs/get` |
| `lotte-bridge` | lotte-bridge (Node.js) | `/api/v1/ekycs/banks`, `/api/v1/ekycs/branch`, `/api/v1/ekycs/banks/{id}/branches`, `/api/v1/ekycs/partner`, `/api/v1/ekycs/account/exist`, `/api/v1/equity/account/checkNationalId` |

**Rest-proxy** không "own" business logic — chỉ route REST → Kafka topic.

---

## 6. Gaps / Unknown

1. **`/api/v1/ekycs/account/exist`** — có trong source `lotte-bridge`, nhưng **không có** trong `TradeX API - Addons.yaml`. Cần confirm với BE lead: đây là API internal chưa được publish hay là public endpoint bị miss trong docs.
2. **Path param mismatch:** Addons.yaml viết `/api/v1/ekycs/banks/{bankCode}/branches`, code lotte-bridge routing dùng URI pattern `/api/v1/ekycs/banks/{id}/branches`. Cần đồng nhất khi tạo spec mới.
3. **rest-proxy routing files** (`src/app/routes/api/equity/Account.ts`) trong `Knowledge/TradeX-MCP/rest-proxy-main` là **file rỗng (0-1 lines)** — không tìm được implementation route lớp thứ nhất trong snapshot. Chỉ có Kafka handler ở lotte-bridge/ekyc-admin cho biết URI. Có thể snapshot rest-proxy chưa đủ, cần confirm với BE lead nếu cần chi tiết middleware auth/validation.
4. **rest-proxy chuyển URI** `POST /api/v1/lotte/ekycs/create` → Kafka topic `ekyc-admin`, URI `/api/v1/ekycs/create` (không giữ prefix `/lotte`). Tương tự `/api/v1/lotte/ekycs` → `/api/v1/ekycs`. Đây là mapping ngầm chỉ suy ra được từ RequestHandler.java của ekyc-admin. Nếu doc cần liệt kê exact mapping cần confirm rest-proxy route file thực tế.
5. **FPT eContract create envelope** (bước sau khi `POST /lotte/ekycs`) — chi tiết chưa trace trong scope này. Nếu cần cho Journey Logging, cần dive vào `LotteEKycService.createEKycLotte` + `EContractCustomService`.
6. **VNPT biometric log** (bước 2) là **client-side SDK**, không có API TradeX nào tương ứng — dữ liệu (matchingRate, logIds, dataSign) chỉ được gửi lên BE khi submit `POST /lotte/ekycs`. Đây chính là core insight của `PRD_eKYC_v2.md`: cần thêm endpoint `POST /ekycs/attempt-log` để log từng attempt biometric (kể cả retry) trước khi user submit.

---

## 7. Files cần đọc thêm khi cần chi tiết

- `Knowledge/TradeX-MCP/ekyc-admin/src/main/java/com/techx/tradex/ekycadmin/service/CustomEKycService.java` — flow `addEKyc()` (chain Lotte + FPT)
- `Knowledge/TradeX-MCP/ekyc-admin/src/main/java/com/techx/tradex/ekycadmin/service/LotteEKycService.java` — line 72-220 `createEKycLotte`, `callUploadImage`
- `Knowledge/TradeX-MCP/ekyc-admin/src/main/java/com/techx/tradex/ekycadmin/service/EContractCustomService.java` — FPT integration
- `Knowledge/TradeX-MCP/ekyc-admin/src/main/java/com/techx/tradex/ekycadmin/service/OTPService.java` — OTP generation/verify
- `eKYC/Planning/PRD_eKYC_v2.md` — yêu cầu nghiệp vụ eKYC v2 (Biometric Attempt Log)

---

**Status:** ✅ Complete | For: creator (next phase) | Next Steps: creator dùng bảng service mapping + journey flow để viết Planning doc `eKYC Journey Logging`
