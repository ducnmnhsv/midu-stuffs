# PRD: eKYC Attempt History — Tra cứu Hành trình & Dashboard Analytics

**Version:** 2.2 | **Date:** 2026-07-06 | **Status:** Updated Draft — Phase 1 scope locked

> **Changelog v2.2 (2026-07-06) — Chốt scope Phase 1:**
> - **Phase 1 giờ chỉ gồm backend/DB logging, KHÔNG có bất kỳ màn hình admin nào.** Loại khỏi Phase 1: sub-feature `02_Admin_Attempt_History` (toàn bộ 3 màn hình tra cứu), mục 4.7 (cột "Lần thử" trong Danh sách eKYC), mục 4.8 (tab "Lịch sử thử" trong Modal detail) — tất cả chuyển sang Phase 2 cùng với `03_Admin_Dashboard_Analytics` và `04_MRZ_Validation` (đã defer từ trước).
> - **Compliance Journey Log (4.10) chốt lại chi tiết storage:** ghi log real-time; tiêu chí thành công = `POST /lotte/ekycs` trả HTTP 200 kèm `eKycId` + `status: "success"`; hành trình không đạt tiêu chí này (fail rõ ràng hoặc bỏ dở quá ngưỡng timeout) → **xóa hoàn toàn**, không giữ lại như bản nháp trước (từng đề xuất giữ tạm 7 ngày).
> - **Quyết định kỹ thuật mới: Journey Log dùng bảng riêng `ekyc_journey_log`, KHÔNG mở rộng chung bảng `ekyc_attempt_log`.** Lý do: `ekyc_attempt_log` (sub-feature 01) phải append-only, không bao giờ xóa (Goal 1, mục 5 NFR) — nếu Journey Log share bảng và áp dụng chính sách xóa-khi-fail, sẽ xóa luôn dữ liệu biometric fail cần giữ vĩnh viễn. Việc tách bảng giải quyết dứt điểm mâu thuẫn này (trước đây là Open Question chưa chốt ở 4.10).
> - Ghi nhận đánh đổi: mục tiêu phân tích friction/fraud-pattern từ Journey Log không còn khả thi do dữ liệu fail bị xóa (xem 4.10).
>
> **Changelog v2.1:**
> - Thêm **Compliance Journey Log** (section 4.10) — log 11 API call trong hành trình mở tài khoản, lưu DB vĩnh viễn, phục vụ audit/compliance, không hiển thị admin UI
>
> **Changelog v2.0:**
> - Thêm **Dashboard Analytics** vào phạm vi (chuyển từ v2-backlog → v1)
> - Thêm yêu cầu **lưu ảnh OCR** mỗi lần thử (front/back image URL per attempt)
> - Bổ sung **trường VNPT mở rộng** từ phân tích App flow (liveness results, face compare confidence, nationality…)
> - Bổ sung **API ghi nhận thất bại phía App** (pre-submit failure logging)
> - Cập nhật UI reference → demo tương tác đã xây dựng

---

## 1. Bối cảnh & Vấn đề

### 1.1 Luồng eKYC hiện tại (App → BE)

Dựa trên phân tích source code `nhsv-mts-rn`:

```
User chọn loại giấy tờ
  → App gọi NativeModules.EkycBridge.startEkycOcr(idType)
  → VNPT SDK chụp ảnh mặt trước / mặt sau / selfie
  → SDK trả về JSON gồm 8 khóa (LOG_OCR, LOG_LIVENESS_*, LOG_COMPARE, LOG_PATH_IMAGE_*)
  → App validate & lưu Redux
  → User xác nhận thông tin + chính sách
  → POST /lotte/ekycs  ← điểm duy nhất App gửi dữ liệu lên BE
  → User verify OTP → Tài khoản được mở
```

**File xử lý chính:**
- `src/reduxs/sagas/EKYC/EKYCScanIdDone.ts` — parse & validate SDK output
- `src/reduxs/sagas/EKYC/OnPressNextInConfirmPolicyScreen.ts` — gửi lên BE

### 1.2 Vấn đề cốt lõi

| Vấn đề | Tác động |
|--------|---------|
| `CustomEKycService.java:211` xóa record PENDING cũ mỗi khi retry | Lịch sử fail **mất vĩnh viễn** |
| App không gửi dữ liệu VNPT khi fail bước pre-submit (blur, liveness…) | **Không trace được** fail reason cho nhóm lỗi này |
| `ekyc_ext.raw_data` lưu full VNPT JSON nhưng không structured | **Không query được** theo fraud flags hay quality scores |
| Ảnh mặt trước / mặt sau CCCD embedded trong VNPT response chưa được lưu riêng | **Không xem được** ảnh khi tra cứu case |
| Không có Dashboard tổng hợp | Ops/BA không có **số liệu** để ra quyết định |

---

## 2. Mục tiêu

| # | Mục tiêu | Phạm vi | Phase |
|---|---------|--------|-------|
| 1 | **Lưu trữ đầy đủ** mọi lần thử eKYC (fail và thành công) | Backend | **Phase 1** |
| 2 | **Lưu ảnh OCR** (mặt trước + mặt sau CCCD) mỗi lần thử | Backend + Storage | Phase 2 (phụ thuộc MinIO/S3) |
| 3 | **Admin tra cứu hành trình** theo CCCD/SĐT | Backend + Admin UI | Phase 2 (không có màn hình admin ở Phase 1) |
| 4 | **Dashboard Analytics** — tỉ lệ lỗi, breakdown nguyên nhân, fraud detection | Admin UI | Phase 2 |
| 5 | **Link bản ghi thành công** (`e_kyc`) với toàn bộ lần thử trước | Backend | **Phase 1** |
| 6 | **Compliance Journey Log** — audit trail 11 API call, chỉ giữ vĩnh viễn cho hành trình thành công, phục vụ truy vết pháp lý | Backend (DB only, không có admin UI) | **Phase 1** |

> Phase 1 hiện tại = **thuần backend/DB**, không triển khai bất kỳ màn hình admin nào (kể cả tra cứu, dashboard, hay cải tiến hiển thị badge/log trên trang admin hiện có). Xem mục 7 Out of scope.

---

## 3. Người dùng mục tiêu

> **Lưu ý Phase 1:** chưa có màn hình admin nào — cột "Màn hình sử dụng" dưới đây là đích đến ở **Phase 2**. Ở Phase 1, các nhu cầu này chỉ được đáp ứng qua **SQL trực tiếp / tool nội bộ DBA** (xem 4.10).

| Người dùng | Nhu cầu chính | Màn hình sử dụng (Phase 2) |
|-----------|-------------|----------------|
| **Team Ops / CS** | Tra cứu case lỗi, hỗ trợ khách hàng | Danh sách eKYC + Detail modal |
| **BA / PM** | Phân tích tỷ lệ fail, identify cải tiến UX | Dashboard + Danh sách |
| **QA** | Verify fix, kiểm tra regression | Danh sách + Detail |
| **Dev** | Debug production issues, xem raw VNPT data | Detail → Additional Info → VNPT Log |
| **Security** | Phát hiện fraud patterns, review case nghi ngờ | Dashboard Fraud + Detail |

---

## 4. Yêu cầu nghiệp vụ

### 4.1 Lưu trữ lịch sử attempt

- Mỗi lần submit eKYC tạo **1 bản ghi** trong `ekyc_attempt_log` — không bao giờ xóa, không bao giờ update
- Ghi nhận: thứ tự lần thử (`attempt_number`), thời điểm, outcome, failure step, toàn bộ dữ liệu VNPT structured
- Logic `e_kyc` hiện tại (duplicate handling, delete PENDING cũ) **giữ nguyên** — không break

**Hai luồng ghi nhận failure:**

| Loại fail | Thời điểm fail | Cơ chế ghi nhận |
|----------|--------------|----------------|
| **Pre-submit** | Trong SDK (blur, liveness, fake detection) | App gọi `POST /ekycs/attempt-log` mới ngay sau khi SDK trả kết quả |
| **Post-submit** | Sau khi BE gửi lên Lotte (Lotte reject, timeout) | BE tự ghi khi xử lý `/lotte/ekycs` |

### 4.2 Lưu ảnh OCR mỗi lần thử

**Cơ chế lưu ảnh:**

VNPT SDK trả về ảnh CCCD dưới dạng base64 trong response (`imgs.img_front`, `imgs.img_back` — model `VNPTDataBase64.Img`). BE đã nhận được dữ liệu này.

```
VNPT Response
  └── imgs.img_front  (base64 JPG/PNG mặt trước CCCD)
  └── imgs.img_back   (base64 JPG/PNG mặt sau CCCD)
```

**Luồng lưu trữ:**
1. BE nhận VNPT response → extract base64 images
2. Upload lên **MinIO / S3** với path: `ekyc-attempts/{identifier_id}/{attempt_number}/{front|back}.jpg`
3. Lưu URL vào `ekyc_attempt_log.image_front_url` và `image_back_url`
4. Admin UI có thể load ảnh trực tiếp từ URL

**Với pre-submit failure (fail trước khi reach BE):**
- App gửi base64 images qua API `POST /ekycs/attempt-log` cùng với dữ liệu VNPT
- BE upload và lưu URL tương tự

**Lưu ý:**
- Ảnh cần được **access-controlled** (chỉ admin có thể xem)
- Retention policy: giữ tối thiểu 90 ngày

### 4.3 Dữ liệu VNPT mở rộng cần capture

Từ phân tích App flow, các trường sau hiện đang được dùng để **validate phía App nhưng không gửi lên BE**. Cần capture vào `ekyc_attempt_log`:

| Nhóm | Trường SDK | Tên field DB | Lý do cần |
|------|-----------|-------------|----------|
| **Liveness** | `LOG_LIVENESS_CARD_FRONT.object.liveness` | `liveness_card_front_result` | Kết quả pass/fail liveness mặt trước |
| **Liveness** | `LOG_LIVENESS_CARD_REAR.object.liveness` | `liveness_card_rear_result` | Kết quả pass/fail liveness mặt sau |
| **Liveness** | `LOG_LIVENESS_FACE.object.liveness` | `liveness_face_result` | Kết quả selfie liveness |
| **Liveness** | `LOG_MASK_FACE.object.mask_result` | `face_mask_result` | Có đeo khẩu trang không |
| **Liveness** | `fake_liveness_prob` | `fake_liveness_prob` | Xác suất ảnh giả (0-1) |
| **Liveness** | `fake_print_photo_prob` | `fake_print_photo_prob` | Xác suất ảnh in / màn hình |
| **Face Compare** | `object.msg` | `face_compare_msg` | MATCH / NOMATCH |
| **Face Compare** | `object.prob` | `face_compare_prob` | Confidence score AI |
| **OCR** | `object.nationality` | `vnpt_nationality` | Quốc tịch (cho FATCA/AML) |
| **OCR** | `object.citizen_id` | `vnpt_citizen_id_chip` | Mã định danh chip CCCD |

### 4.4 Phân loại kết quả (outcome)

| Outcome | Ý nghĩa | Failure Step |
|---------|---------|-------------|
| `VNPT_FAILED` | VNPT trả lỗi (OCR, liveness, quality) | `VNPT_OCR` / `VNPT_LIVENESS` / `VNPT_QUALITY` |
| `MRZ_FAILED` | MRZ checksum sai hoặc MRZ ↔ OCR không khớp | `MRZ_VALIDATION` / `MRZ_CROSS_CHECK` |
| `FACE_COMPARE_FAILED` | Matching rate dưới ngưỡng (< 80%) | `FACE_COMPARE` |
| `LOTTE_REJECTED` | Lotte từ chối khi submit | `LOTTE_SUBMIT` |
| `USER_ABANDONED` | Không ký HĐ trong 48h sau APPROVED | `CONTRACT_SIGN` |
| `SUCCESS` | Tài khoản mở thành công | — |

### 4.5 API mới: Pre-submit Failure Logging

App cần gọi endpoint mới **sau mỗi lần VNPT SDK hoàn thành** (dù thành công hay thất bại ở bước pre-submit):

```
POST /ekycs/attempt-log
Authorization: Bearer {token}

{
  "identifierId": "038094001234",
  "attemptNumber": 1,
  "outcome": "VNPT_FAILED",
  "failureStep": "VNPT_QUALITY",
  "failureCode": "VNPT_IMAGE_BLURRED",
  "failureMessage": "blur_score = 0.23 (required >= 0.50)",

  // VNPT OCR data (từ LOG_OCR)
  "vnptStatusCode": 0,
  "vnptRawData": "{ ...full VNPT JSON... }",

  // Ảnh base64 từ LOG_PATH_IMAGE_*
  "imageFrontBase64": "data:image/jpeg;base64,...",
  "imageBackBase64": "data:image/jpeg;base64,...",

  // Liveness results (từ LOG_LIVENESS_*)
  "livenessCardFrontResult": "success",
  "livenessCardRearResult": "success",
  "livenessFaceResult": "failure",
  "fakeLivenessProb": 0.72,

  // Face compare (từ LOG_COMPARE)
  "faceCompareMsg": "NOMATCH",
  "faceCompareProb": 0.74
}
```

> **Khi nào App gọi endpoint này?**
> - Ngay sau `EKYCScanIdDone.ts` nhận kết quả SDK
> - Nếu bước pre-submit pass → App vẫn gọi, outcome = `PRE_SUBMIT_PASSED` (để tạo attempt record trước)
> - Khi submit lên `/lotte/ekycs` → BE cập nhật outcome = `SUCCESS` hoặc `LOTTE_REJECTED`

### 4.6 Link hành trình khách hàng

- Khi tài khoản được mở thành công, BE cập nhật `final_ekyc_id` trong tất cả attempt records của cùng `identifier_id`
- `e_kyc` table bổ sung: `total_attempts`, `first_attempt_at`

### 4.7 Admin: Danh sách eKYC (cải tiến) — ⏸ Phase 2

> **Out of scope Phase 1 (2026-07-06):** đây là một hình thức "hiển thị log trên admin page" — PM đã chốt không triển khai bất kỳ hiển thị nào lên admin UI ở Phase 1. Nội dung dưới đây giữ lại làm tham chiếu cho Phase 2.

Ngoài các cột hiện có, thêm cột **Lần thử** hiển thị:
- Badge tổng số lần thử (xanh)
- Badge số lần fail (đỏ, nếu > 0)
- Badge "⚠ Fraud" nếu có fraud flags (vàng)

Chi tiết xem màn hình demo: `eKYC enhancement - store log/admin-ui-demo.html` → page "Danh sách eKYC"

### 4.8 Admin: Modal chi tiết (cải tiến) — ⏸ Phase 2

> **Out of scope Phase 1 (2026-07-06):** cùng lý do với 4.7 — hiển thị log lên admin UI, chuyển sang Phase 2.

Modal 4 tab cho từng bản ghi:

| Tab | Nội dung | Cải tiến v2 |
|-----|---------|------------|
| **Thông tin cá nhân** | Dữ liệu OCR, ảnh CCCD 2 mặt | Hiển thị ảnh thực từ URL storage |
| **Thông tin bổ sung** | Tùy chọn tài khoản + VNPT's eKYC Log | VNPT Log mở rộng 6 section structured |
| **Hợp đồng** | Contract info, chữ ký | Giữ nguyên |
| **🕐 Lịch sử thử** | Timeline hành trình, expandable per-attempt | **Tab mới v2** |

**VNPT's eKYC Log (6 sections):**
1. OCR — Thông tin đọc từ CCCD (số CCCD, tên, loại thẻ, confidence scores, MRZ)
2. Fraud Detection — Tampering, ID fake, dob/address/issuedate fake, duplication
3. Image Quality — Blur score + luminance score mặt trước / sau (progress bar)
4. Card Integrity — Recaptured, edited_prob, photocopied mặt trước / sau
5. Cross Validation — Khớp số CCCD, tên, ngày sinh, ngày hết hạn giữa 2 mặt
6. Log IDs — OCR / Liveness / Face Compare / Face Mask log IDs + server version

**Lịch sử thử (tab mới):**
- Banner tổng kết (APPROVED / PENDING)
- Journey timeline dots (1→2→3)
- Expandable per-attempt: OCR results, Image Quality, Fraud Flags, fail reason
- Ảnh CCCD từng lần thử

### 4.9 Lưu trữ timestamp đồng ý điều khoản (Compliance)

Ghi lại thời điểm khách hàng tick checkbox "Tôi đã đọc và đồng ý với các điều khoản Hợp đồng mở và sử dụng tài khoản tại Chứng khoán NH Việt Nam" tại màn hình **Xác nhận điều khoản hợp đồng** (bước 4/4 trong flow eKYC).

**Mục đích:** Compliance / audit trail — đáp ứng yêu cầu pháp lý về việc chứng minh khách hàng đã đọc và đồng ý điều khoản hợp đồng tại thời điểm mở tài khoản.

**Luồng:**

```
User tick checkbox
  → App ghi timestamp ISO 8601 UTC vào local state
  → User bấm "Tiếp theo"
  → App gọi POST /ekycs/attempt-log  { identifierId, termsAgreedAt }
        (reuse endpoint sub-feature 01 — không tạo API mới)
  → BE update terms_agreed_at trên attempt record hiện có của user
  → App gọi POST /lotte/ekycs như bình thường (không thay đổi)
```

**DB changes (bảng `ekyc_attempt_log`):**

| Column | Type | Ghi chú |
|--------|------|---------|
| `terms_agreed_at` | DATETIME NULL | Timestamp UTC khi user tick đồng ý |
| `terms_version` | VARCHAR(20) DEFAULT 'v1' | Phiên bản nội dung điều khoản |

**Đặc điểm kỹ thuật:**
- Reuse `POST /ekycs/attempt-log` — không tạo endpoint mới, không đụng `POST /lotte/ekycs`
- Lưu vào `ekyc_attempt_log` (bảng audit trail) thay vì `e_kyc` — toàn bộ lịch sử eKYC ở 1 nơi
- BE phân biệt "terms-only call" vs "VNPT-log call" qua sự vắng mặt của field `outcome`
- `terms_version = 'v1'` — cập nhật thủ công khi nội dung điều khoản thay đổi

Chi tiết implementation:
- [FE Issue](../05_Contract_Terms_Checkbox_Log/Issues/FE_Issue_Checkbox_Analytics_Log.md) — ghi timestamp + gọi attempt-log API
- [BE Issue](../05_Contract_Terms_Checkbox_Log/Issues/BE_Issue_Checkbox_Consent_Storage.md) — thêm cột + mở rộng service attempt-log

---

### 4.10 Compliance Journey Log — 11 API call trong hành trình mở tài khoản (mới)

#### Mục đích

Ghi lại toàn bộ chuỗi lời gọi API trong hành trình mở tài khoản eKYC — ngoài VNPT biometric log (sub-feature 01) và TnC checkbox log (sub-feature 05) đã có kế hoạch. Mục tiêu chính là **compliance và audit trail**: cung cấp bằng chứng đầy đủ khi cơ quan quản lý (UBCK, NHNN) yêu cầu truy vết hồ sơ, hoặc khi khách hàng khiếu nại về quá trình mở tài khoản. Log này **không hiển thị trên admin UI** — chỉ tồn tại trong DB, truy cập thủ công qua SQL hoặc tool nội bộ khi cần điều tra.

> **⚠️ Đánh đổi đã ghi nhận (2026-07-06):** vì hành trình không thành công bị xóa (xem Storage strategy bên dưới), hai khả năng vốn được kỳ vọng ban đầu — **phân tích friction/drop-off** và **phát hiện fraud pattern** dựa trên dữ liệu Journey Log — **không còn khả thi**, do dữ liệu nguồn không còn tồn tại để phân tích. Đây là đánh đổi được PM chấp nhận để đơn giản hóa scope; nếu cần lại các phân tích này, phải mở scope riêng ở giai đoạn sau.

#### Hành trình mở tài khoản (bối cảnh từng bước)

Tóm lược góc độ user và trình tự API call, giúp hiểu rõ 11 API dưới đây khớp vào đâu trong luồng thực tế:

- **Bước 0 — Khởi tạo phiên.** App login bằng `client_credentials` (tài khoản demo dùng chung cho user chưa có tài khoản NHSV) để lấy JWT truy cập nhóm API eKYC.
- **Bước 1 — Nhập liên hệ, xác thực OTP.** User nhập SĐT/email → `POST /lotte/ekycs/create` tạo `eKycId` (identifier chính) → `POST /ekyc-admin/sendOtp` → user nhập OTP → `POST /ekyc-admin/verifyOtp` trả `otpKey`.
- **Bước 2 — Quét giấy tờ + nhận diện khuôn mặt (VNPT SDK).** Chạy phía client, không có API TradeX tương ứng cho tới bước submit — dữ liệu VNPT (OCR, liveness, face compare) chỉ đẩy về server ở bước 6. Đây là phạm vi sub-feature 01.
- **Bước 3 — Kiểm tra CCCD đã có tài khoản chưa.** `POST /equity/account/checkNationalId` — query song song DB `ekyc-admin` (case pending) và Lotte Core (case đã mở).
- **Bước 4 — Điền form, chọn ngân hàng.** `GET /ekycs/banks`, `GET /ekycs/branch`, `GET /ekycs/banks/{id}/branches`; nếu có referral CTV → `GET /ekycs/partner`.
- **Bước 5 — Chấp nhận điều khoản.** User tick checkbox T&C — phạm vi sub-feature 05 (`terms_agreed_at`).
- **Bước 6 — Submit hồ sơ.** `POST /lotte/ekycs` gửi toàn bộ form + VNPT metadata + `deviceUniqueId`. Đây là **điểm chốt "thành công"** của Journey Log (xem Storage strategy bên dưới).
- **Bước 7 — Ký hợp đồng điện tử.** `GET /equity/account/contracts` lấy webView FPT. Sau ký, App poll trạng thái hợp đồng/VSD — **ngoài phạm vi log**.
- **Bước 8 — Chờ approval.** Ops duyệt, VSD cấp số tài khoản chính thức — **ngoài phạm vi log**.

#### 11 API cần log

| STT | Endpoint | Mô tả bước | Trường log quan trọng |
|-----|----------|-----------|----------------------|
| 1 | `POST /api/v1/lotte/ekycs/create` | Tạo eKycId (khởi tạo hành trình) | groupType, phoneNo, email, eKycId trả về |
| 2 | `POST /api/v1/ekyc-admin/sendOtp` | Gửi OTP | otpId, expiredTime, số lần resend |
| 3 | `POST /api/v1/ekyc-admin/verifyOtp` | Xác thực OTP | otpId, outcome, error code, số lần thử |
| 4 | `POST /api/v1/lotte/ekycs` | Submit toàn bộ hồ sơ | link tới EKyc.id, VNPT log IDs, matchingRate, partnerId |
| 5 | `GET /api/v1/ekycs/banks` | Lấy danh sách ngân hàng | số bản ghi trả về |
| 6 | `GET /api/v1/ekycs/branch` | Lấy chi nhánh NHSV | số bản ghi trả về |
| 7 | `GET /api/v1/ekycs/banks/{id}/branches` | Chi nhánh của ngân hàng đã chọn | bankCode, số bản ghi |
| 8 | `GET /api/v1/ekycs/partner` | Validate CTV/nhân viên referral | partnerId, tên partner (audit điểm compliance) |
| 9 | `GET /api/v1/ekycs/account/exist` | Kiểm tra trạng thái mở TK tại Lotte | identifierId, exist true/false |
| 10 | `POST /api/v1/equity/account/checkNationalId` | Kiểm tra CCCD chưa có TK NHSV | identifierId, kết quả 2 nguồn |
| 11 | `GET /api/v1/equity/account/contracts` | Lấy webView FPT eContract | eKycId, envelopeId, recipientStatus |

#### Trường log chung cho mỗi bản ghi

Mỗi API call được ghi với các trường: `eKycId` (identifier chính), `phoneNo` (khóa phụ ở bước trước khi có eKycId), `identifierId` (CCCD từ bước 3), `journey_step` (enum tên bước), endpoint + HTTP method, timestamp (ms), HTTP status, outcome (success/failure), error code nếu có, deviceUniqueId, sourceIp.

Không log: OTP value, webView URL đầy đủ kèm cookie, raw base64 ảnh VNPT, toàn bộ form eKYC (đã có ở entity `EKyc`).

#### Storage strategy (chốt 2026-07-06)

**Ghi log real-time.** Mọi lời gọi trong 11 API được ghi ngay tại tầng backend xử lý request đó — không buffer rồi flush cuối luồng (tránh mất dữ liệu nếu service crash giữa chừng).

**Tiêu chí "thành công" (điểm chốt duy nhất).** `POST /api/v1/lotte/ekycs` (API 4 — submit form) trả về **HTTP 200** kèm response chứa `eKycId` + `"status": "success"`. Không phụ thuộc bước ký hợp đồng điện tử hay VSD APPROVED (đều ngoài phạm vi log).

> ⚠️ Hệ quả cần lưu ý: một hồ sơ sau đó bị **Ops reject** ở bước duyệt (ngoài phạm vi log) vẫn được tính "thành công" theo tiêu chí này và giữ log vĩnh viễn — vì mốc chốt nằm ở bước submit, không phải bước duyệt cuối.

**Nếu không đạt tiêu chí thành công → xóa toàn bộ log của session/`eKycId` đó**, không giữ lại:
- **Fail rõ ràng** (submit trả lỗi, hoặc bị chặn luồng trước đó, vd. CCCD đã có tài khoản) → xóa ngay sau khi xác định outcome.
- **Bỏ dở (abandon)** — user không bao giờ gọi submit, hoặc gọi rồi thoát trước khi có response → cần **ngưỡng timeout** để hệ thống coi như fail. Đề xuất **24–48 giờ không có API call mới trên cùng `eKycId`** (ngắn hơn nhiều so với đề xuất 7 ngày ở bản nháp trước, vì mục tiêu giờ chỉ là "chờ xác nhận outcome" chứ không phải "giữ để điều tra case fail" — mục tiêu đó đã bị loại theo đánh đổi ở trên). **BE Lead cần chốt số giờ cụ thể.**

Hệ quả: log chỉ tồn tại vĩnh viễn cho hành trình thành công. Case fail/bỏ dở giữa chừng không có audit trail ở Journey Log — nếu cần trace VNPT biometric attempt riêng lẻ (không phụ thuộc outcome mở tài khoản), đã có sub-feature 01 xử lý độc lập (xem điểm DB bên dưới).

#### DB — bắt buộc dùng bảng riêng, không share với `ekyc_attempt_log`

**Quyết định (2026-07-06):** Journey Log dùng bảng mới **`ekyc_journey_log`**, liên kết `ekyc_attempt_log` / `e_kyc` qua khóa `eKycId` — **không mở rộng chung bảng `ekyc_attempt_log`** như đề xuất ở bản nháp trước.

**Lý do:** `ekyc_attempt_log` (sub-feature 01) phải là **append-only, không bao giờ UPDATE/DELETE** (xem Goal 1 mục 2, NFR mục 5) — nó có nhiệm vụ giữ lại *mọi* lần thử VNPT kể cả fail, vĩnh viễn, để trace nguyên nhân fail. Nếu Journey Log mở rộng chung bảng đó và áp dụng chính sách "xóa khi hành trình không thành công", thao tác xóa sẽ **xóa luôn dữ liệu biometric fail** mà sub-feature 01 bắt buộc phải giữ — vi phạm trực tiếp nguyên tắc gốc của PRD. Tách bảng loại bỏ hoàn toàn xung đột này.

Cột đề xuất cho `ekyc_journey_log`: `eKycId`, `phoneNo`, `identifierId`, `journey_step` (enum), `endpoint`, `http_method`, `http_status`, `response_summary` (JSON), `created_at`. Không cần cột `finalized`/`finalized_at` dạng đánh dấu — vì bản ghi không đạt tiêu chí thành công sẽ bị xóa thẳng thay vì đánh dấu.

Log **không hiển thị trên bất kỳ màn hình admin nào** (Phase 1 không có admin UI nào cho cả 01, 05, và 4.10 — xem mục 7 Out of scope). Không cần API GET public ở Phase 1. Truy cập bằng SQL hoặc tool nội bộ (role riêng biệt với admin thường) khi Ops/Legal cần điều tra.

#### Ai ghi log? (producer)

- **Backend-side logging** là phương án chính — mỗi service (`ekyc-admin` Java, `lotte-bridge` Node.js) tự ghi log ngay sau khi xử lý xong request thuộc 1 trong 11 API. Ưu điểm: chính xác về outcome/error code, không phụ thuộc client.
- Một số bước không có backend call rõ ràng (vd. bước 5 — tick checkbox) đã có phương án App gọi `POST /ekycs/attempt-log` (sub-feature 01) — có thể mở rộng cùng endpoint này cho event pure-client.
- **Vì 2 service cùng ghi vào 1 bảng `ekyc_journey_log`, cần thống nhất producer** — trực tiếp DB (đơn giản, latency thấp, nhưng 2 service phải cùng chuẩn schema) hay qua Kafka topic chung (giảm coupling, tăng độ trễ, cần thêm consumer). Quyết định thuộc BE Lead.

#### Open questions

- ~~Định nghĩa "thành công"~~ → **Đã chốt:** `POST /lotte/ekycs` trả HTTP 200 kèm `eKycId` + `status: "success"`.
- ~~Bản ghi không thành công giữ bao lâu~~ → **Đã chốt:** không giữ, xóa hoàn toàn (fail rõ ràng: xóa ngay; bỏ dở: xóa sau ngưỡng timeout).
- ~~Dùng chung hay tách bảng với `ekyc_attempt_log`~~ → **Đã chốt:** tách bảng riêng `ekyc_journey_log` (xem trên).
- **Ngưỡng timeout xác định "bỏ dở" chính xác là bao nhiêu giờ?** Đề xuất 24–48h — cần BE Lead chốt số cụ thể.
- **Định nghĩa "cùng một hành trình" khi user retry sau khi đóng app.** Ảnh hưởng trực tiếp đến độ chính xác của job xóa — xóa nhầm nếu không nhận diện đúng session còn hoạt động. Cần trace behavior App hoặc hỏi FE team.
- **Producer ghi log: direct-DB-write hay Kafka?** `ekyc-admin` (Java) và `lotte-bridge` (Node.js) đều tham gia ghi vào `ekyc_journey_log` — cần thống nhất cơ chế để đảm bảo consistency/ordering (xem "Ai ghi log?" ở trên). Ảnh hưởng độ trễ và độ phức tạp vận hành.
- API GET dropdown (5, 6, 7) có thực sự cần log? Đề xuất: chỉ log lần đầu mỗi session, hoặc bỏ qua nếu Ops không thấy giá trị.
- Compliance review Nghị định 13/2023 (PDPD) cho case thành công (lưu vĩnh viễn CCCD, SĐT, IP) — rủi ro cho case fail đã giảm nhiều vì log các case đó giờ bị xóa thay vì giữ vĩnh viễn.

---

### 4.11 Admin: Dashboard Analytics (mới)

**KPI Cards (hàng 1 — 4 cards):**

| Card | Metric | Mô tả |
|------|--------|-------|
| Tổng eKYC | Count (7 ngày) | Tổng submissions |
| Tỉ lệ thành công | % APPROVED | Số case APPROVED / tổng |
| Thất bại / Pending | Count | Số case fail + pending |
| Số lần thử TB | Avg | TB số lần thử / TK thành công |

**KPI Cards (hàng 2 — 3 cards):**

| Card | Metric | Mô tả |
|------|--------|-------|
| Phát hiện gian lận | Count | Case có fraud flags |
| Thời gian xử lý TB | Minutes | Submit → APPROVED |
| Tỉ lệ ký HĐ | % | Ký HĐ sau khi APPROVED |

**Charts:**

| Chart | Type | Dữ liệu |
|-------|------|--------|
| Tỉ lệ kết quả eKYC | Donut | APPROVED / REJECTED / PENDING |
| Nguyên nhân thất bại | Horizontal bar | Breakdown theo failure_step |
| Xu hướng theo ngày | Line chart | Tổng vs Thành công (7 ngày) |
| Fraud Detection | Bar + counter | Breakdown theo loại fraud flag |

**Bảng case nhiều lần thử:**
- Top N khách hàng có số lần thử cao nhất trong kỳ
- Cột: Họ tên, CCCD, Số lần thử, Kết quả, Nguyên nhân fail chính

---

## 5. Yêu cầu phi chức năng

| Yêu cầu | Mức độ | Chi tiết | Phase |
|--------|--------|---------|-------|
| Backward compatible | Bắt buộc | Không break luồng user hiện tại | Phase 1 |
| Log bền vững | Bắt buộc | Ghi log ngay cả khi Lotte call fail | Phase 1 |
| Append-only | Bắt buộc | `ekyc_attempt_log` (sub-feature 01) không bao giờ UPDATE/DELETE | Phase 1 |
| Xóa đúng hạn | Bắt buộc | `ekyc_journey_log` (mục 4.10) phải xóa sạch bản ghi của hành trình không thành công — không rò rỉ dữ liệu qua ngưỡng timeout | Phase 1 |
| Query performance | Khuyến nghị | Admin search < 2s cho CCCD/SĐT lookup | Phase 2 (không có admin API ở Phase 1) |
| Image storage | Bắt buộc | Access-controlled, retention ≥ 90 ngày | Phase 2 (phụ thuộc MinIO/S3) |
| Dashboard load | Khuyến nghị | Dashboard metrics < 3s (có thể cache) | Phase 2 |

---

## 6. Phạm vi ảnh hưởng

### Backend (ekyc-admin service) — Phase 1
- **Thêm mới:** Entity `EKycAttemptLog` + Repository + Service *(sub-feature 01)*
- **Thêm mới:** Liquibase migration cho bảng `ekyc_attempt_log` (bao gồm cột `terms_agreed_at`, `terms_version` — sub-feature 05)
- **Thêm mới:** Liquibase migration cho bảng **riêng** `ekyc_journey_log` (`eKycId`, `phoneNo`, `identifierId`, `journey_step`, `endpoint`, `http_method`, `http_status`, `response_summary`) — **không** thêm các cột này vào `ekyc_attempt_log` (xem 4.10 lý do tách bảng)
- **Thêm mới:** `POST /ekycs/attempt-log` — nhận VNPT log + xử lý terms-consent update *(sub-feature 01 + 05)*
- **Thêm mới:** Journey log interceptor/aspect — ghi log real-time 11 API call vào `ekyc_journey_log`, không phụ thuộc client *(4.10)*
- **Thêm mới:** Cleanup job — xóa bản ghi `ekyc_journey_log` của hành trình không đạt tiêu chí thành công sau ngưỡng timeout (đề xuất 24–48h, chờ BE Lead chốt) *(4.10)*
- **Sửa:** `CustomEKycService.java` — gọi `attemptLogService.save()` trước block duplicate handling
- **Sửa:** Provisioning handler — cập nhật `final_ekyc_id` khi APPROVED
- **Sửa:** `EkycAttemptLogService.java` — xử lý terms-only call (update `terms_agreed_at`) *(sub-feature 05)*
- ~~Tích hợp MinIO/S3~~ *(Phase 2)*
- ~~Admin API endpoints: search, journey, attempt detail, dashboard metrics~~ *(Phase 2 — không có admin UI nào ở Phase 1, xem mục 7)*

### App Mobile (nhsv-mts-rn) — Phase 1
- **Sửa:** `EKYCScanIdDone.ts` — gọi `POST /ekycs/attempt-log` sau khi nhận kết quả SDK
- **Sửa:** `OnPressNextInConfirmPolicyScreen.ts` — gửi thêm các trường mở rộng (liveness results, face compare fields, nationality, citizen_id)
- **Sửa:** `EKYCConfirmPolicyScreen/index.tsx` — ghi `termsAgreedAt` timestamp khi tick checkbox *(sub-feature 05)*
- **Sửa:** `OnPressNextInConfirmPolicyScreen.ts` — call `POST /ekycs/attempt-log` với `{identifierId, termsAgreedAt}` *(sub-feature 05)*
- **Sửa:** `src/interfaces/ekyc.ts` — thêm `termsAgreedAt: string` vào `IEKYCConfirmPolicy` *(sub-feature 05)*
- ~~MRZ Validation cross-check~~ *(Phase 2 — sub-feature 04)*

### Admin Frontend (ekyc-admin UI) — ⏸ Toàn bộ chuyển sang Phase 2

**Không có bất kỳ thay đổi admin UI nào ở Phase 1** (quyết định 2026-07-06). Danh sách dưới đây đều là Phase 2:
- ~~Danh sách eKYC — thêm cột Lần thử, 2 tab trạng thái~~ *(mục 4.7)*
- ~~Modal detail — thêm tab "Lịch sử thử", mở rộng VNPT Log section~~ *(mục 4.8)*
- ~~3 màn hình Tra cứu Hành trình (Tìm kiếm / Journey Timeline / Chi tiết lần thử)~~ *(sub-feature 02)*
- ~~Dashboard page (KPI cards + charts)~~ *(sub-feature 03)*
- ~~Hiển thị ảnh CCCD từ storage URL~~ *(phụ thuộc MinIO/S3, Phase 2)*

---

## 7. Out of scope

### Phase 1 (không triển khai lần này) — chốt lại 2026-07-06

- **Admin Dashboard Analytics** (sub-feature 03) — KPI cards, charts, fraud detection panel → Phase 2
- **MRZ Validation từ App** (sub-feature 04) — cross-check MRZ vs OCR phía client → Phase 2
- **Hiển thị log trên bất kỳ trang admin nào** — bao gồm:
  - Sub-feature `02_Admin_Attempt_History` — cả 3 màn hình (Tìm kiếm/Danh sách, Journey Timeline, Chi tiết lần thử)
  - Mục 4.7 — cột "Lần thử" trong Danh sách eKYC hiện có
  - Mục 4.8 — tab "Lịch sử thử" trong Modal detail hiện có
  - Bất kỳ API GET admin nào phục vụ các màn hình trên (`/api/admin/ekyc/attempts/*`)
  → tất cả để **scope sau**; Phase 1 chỉ lưu dữ liệu dưới DB, truy cập qua SQL/tool nội bộ khi cần điều tra
- **Lưu ảnh CCCD lên S3/MinIO** — phụ thuộc infrastructure setup → Phase 2
- **Dashboard API endpoint** (`GET /api/admin/ekyc/dashboard`) → Phase 2

> **Tóm lại:** Phase 1 = thuần backend/DB (sub-feature 01 + 05 + Compliance Journey Log mục 4.10). Không có bất kỳ màn hình hoặc API GET admin-facing nào.

### Backlog (chưa có timeline)
- Alert tự động khi phát hiện fraud pattern (threshold-based rule engine)
- Re-evaluate ngưỡng matching rate tự động
- Export báo cáo Excel/PDF từ Dashboard
- Customer journey cross-service (link eKYC → order history)

---

## 8. Định nghĩa thành công

### Phase 1 (thuần backend/DB — không có tiêu chí phụ thuộc admin UI)
- [ ] Mọi lần submit eKYC được ghi lại vào `ekyc_attempt_log`, không mất data khi retry (append-only, verify bằng SQL trực tiếp)
- [ ] `terms_agreed_at` có mặt trên mọi record mở tài khoản thành công sau ngày go-live
- [ ] Toàn bộ 11 API call trong hành trình mở tài khoản được ghi vào `ekyc_journey_log` với `eKycId` làm khóa liên kết
- [ ] Bản ghi Journey Log của hành trình thành công (submit trả HTTP 200 + `eKycId` + `status: success`) được giữ vĩnh viễn, không bao giờ bị xóa
- [ ] Bản ghi Journey Log của hành trình fail rõ ràng hoặc bỏ dở quá ngưỡng timeout bị xóa hoàn toàn — verify bằng SQL sau go-live (không còn record nào quá ngưỡng mà chưa xóa)
- [ ] Dev/DBA tra cứu được toàn bộ hành trình của một `eKycId` qua SQL trong vài phút khi cần điều tra case (không cần màn hình admin)

### Phase 2 (bổ sung sau)
- [ ] Ops tự tra cứu được lý do fail qua Admin page (theo CCCD/SĐT), không cần hỏi Dev
- [ ] Ảnh CCCD của từng lần thử có thể xem từ Admin page
- [ ] Dashboard hiển thị tỉ lệ fail theo nguyên nhân trong 7 ngày

---

## Appendix A — Phân tích App Flow

### SDK Output (8 keys)

| Key | Nội dung | Mapping BE |
|-----|---------|-----------|
| `LOG_OCR` | JSON — thông tin OCR giấy tờ | `ocrLogId` + tất cả trường OCR |
| `LOG_LIVENESS_CARD_FRONT` | JSON — liveness mặt trước | `cardLivenessLogId` |
| `LOG_LIVENESS_CARD_REAR` | JSON — liveness mặt sau | `cardRearLogId` |
| `LOG_COMPARE` | JSON — face compare selfie vs CCCD | `compareLogId`, `matchingRate` |
| `LOG_LIVENESS_FACE` | String — selfie liveness | `faceLivenessLogId` |
| `LOG_MASK_FACE` | String — mask detection | `faceMaskLogId` |
| `LOG_PATH_IMAGE_FRONT` | Path — file ảnh mặt trước | **Chưa gửi BE** → ảnh embedded trong LOG_OCR.imgs.img_front |
| `LOG_PATH_IMAGE_BACK` | Path — file ảnh mặt sau | **Chưa gửi BE** → ảnh embedded trong LOG_OCR.imgs.img_back |

### Gap Analysis — Trường SDK có, BE chưa lưu

| Trường | Nhóm | Priority | Kế hoạch |
|--------|------|---------|---------|
| `imgs.img_front / img_back` | Ảnh CCCD | 🔴 Cao | Upload S3, lưu URL per attempt |
| `liveness` (card front/rear/face) | Liveness | 🔴 Cao | Thêm vào `ekyc_attempt_log` |
| `fake_liveness_prob` | Fraud | 🔴 Cao | Thêm vào `ekyc_attempt_log` |
| `face_compare.msg` | Face Compare | 🔴 Cao | Thêm `face_compare_msg` |
| `face_compare.prob` | Face Compare | 🟡 Trung bình | Thêm `face_compare_prob` |
| `nationality` | OCR | 🔴 Cao | Thêm cho FATCA/AML |
| `citizen_id` (chip) | OCR | 🔴 Cao | Thêm cho VNeID integration |
| `tampering / general_warning` | Fraud | 🟡 Trung bình | Đã có `vnpt_is_tampered` |
| `quality_front.blur_score` | Image Quality | 🟡 Trung bình | Đã có trong schema |
| `mrz / mrz_probs` | Cross-validation | 🟢 Thấp | Có thể thêm sau |

---

## Appendix B — UI Demo Reference

Demo tương tác: `eKYC enhancement - store log/admin-ui-demo.html`

Xem trực tiếp tại: `http://localhost:4321/admin-ui-demo.html`

**Các màn hình demo:**
- **Dashboard** — KPI cards, donut chart, bar chart nguyên nhân fail, line chart xu hướng, fraud detection panel, bảng case nhiều lần thử
- **Danh sách eKYC** — filter bar, table với badges lần thử, pill status
- **Modal detail** — 4 tabs: Thông tin cá nhân, Thông tin bổ sung (VNPT Log 6 sections), Hợp đồng, Lịch sử thử (timeline + expandable per-attempt)

---

**Document Status:** Updated Draft v2.2 — Phase 1 scope locked (thuần backend/DB, không có admin UI)
**For:** Dev Team (Backend + App), BA, Ops, Compliance/Legal
**Next Steps:** Chốt các open question còn lại ở 4.10 (ngưỡng timeout bỏ dở, định nghĩa "cùng hành trình" khi retry, PDPD compliance review) → Review với Dev Lead → estimate effort → phân task Sprint. Phase 2 (Admin UI, Dashboard, MRZ, Image Storage) chờ mở lại scope riêng sau khi Phase 1 go-live.
