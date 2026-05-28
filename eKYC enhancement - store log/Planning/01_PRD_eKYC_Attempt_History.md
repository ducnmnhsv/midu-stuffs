# PRD: eKYC Attempt History — Tra cứu Hành trình & Dashboard Analytics

**Version:** 2.0 | **Date:** 2026-05-24 | **Status:** Updated Draft

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

| # | Mục tiêu | Phạm vi |
|---|---------|--------|
| 1 | **Lưu trữ đầy đủ** mọi lần thử eKYC (fail và thành công) | Backend |
| 2 | **Lưu ảnh OCR** (mặt trước + mặt sau CCCD) mỗi lần thử | Backend + Storage |
| 3 | **Admin tra cứu hành trình** theo CCCD/SĐT | Backend + Admin UI |
| 4 | **Dashboard Analytics** — tỉ lệ lỗi, breakdown nguyên nhân, fraud detection | Admin UI |
| 5 | **Link bản ghi thành công** (`e_kyc`) với toàn bộ lần thử trước | Backend |

---

## 3. Người dùng mục tiêu

| Người dùng | Nhu cầu chính | Màn hình sử dụng |
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

### 4.7 Admin: Danh sách eKYC (cải tiến)

Ngoài các cột hiện có, thêm cột **Lần thử** hiển thị:
- Badge tổng số lần thử (xanh)
- Badge số lần fail (đỏ, nếu > 0)
- Badge "⚠ Fraud" nếu có fraud flags (vàng)

Chi tiết xem màn hình demo: `eKYC enhancement - store log/admin-ui-demo.html` → page "Danh sách eKYC"

### 4.8 Admin: Modal chi tiết (cải tiến)

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

### 4.9 Admin: Dashboard Analytics (mới)

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

| Yêu cầu | Mức độ | Chi tiết |
|--------|--------|---------|
| Backward compatible | Bắt buộc | Không break luồng user hiện tại |
| Log bền vững | Bắt buộc | Ghi log ngay cả khi Lotte call fail |
| Query performance | Khuyến nghị | Admin search < 2s cho CCCD/SĐT lookup |
| Image storage | Bắt buộc | Access-controlled, retention ≥ 90 ngày |
| Dashboard load | Khuyến nghị | Dashboard metrics < 3s (có thể cache) |
| Append-only | Bắt buộc | `ekyc_attempt_log` không bao giờ UPDATE/DELETE |

---

## 6. Phạm vi ảnh hưởng

### Backend (ekyc-admin service)
- **Thêm mới:** Entity `EKycAttemptLog` + Repository + Service
- **Thêm mới:** Liquibase migration cho bảng `ekyc_attempt_log`
- **Thêm mới:** `POST /ekycs/attempt-log` — nhận pre-submit failure từ App
- **Thêm mới:** 4 Admin API endpoints (search, journey, attempt detail, dashboard metrics)
- **Sửa:** `CustomEKycService.java` — gọi `attemptLogService.save()` trước block duplicate handling
- **Sửa:** Provisioning handler — cập nhật `final_ekyc_id` khi APPROVED
- **Tích hợp:** MinIO/S3 upload service cho ảnh CCCD

### App Mobile (nhsv-mts-rn)
- **Sửa:** `EKYCScanIdDone.ts` — gọi `POST /ekycs/attempt-log` sau khi nhận kết quả SDK
- **Sửa:** `OnPressNextInConfirmPolicyScreen.ts` — gửi thêm các trường mở rộng (liveness results, face compare fields, nationality, citizen_id)

### Admin Frontend (ekyc-admin UI)
- **Thêm mới:** Dashboard page (KPI cards + charts)
- **Sửa:** Danh sách eKYC — thêm cột Lần thử
- **Sửa:** Modal detail — thêm tab "Lịch sử thử", mở rộng VNPT Log section
- **Tích hợp:** Hiển thị ảnh CCCD từ storage URL

---

## 7. Out of scope (v1)

- Alert tự động khi phát hiện fraud pattern (threshold-based rule engine) — v3
- Re-evaluate ngưỡng matching rate tự động — v3
- Export báo cáo Excel/PDF từ Dashboard — v2
- Customer journey cross-service (link eKYC → order history) — v3

---

## 8. Định nghĩa thành công

- [ ] Ops team tra cứu được lý do fail của bất kỳ case nào sau go-live
- [ ] Mọi lần submit eKYC được ghi lại, không mất data
- [ ] Ảnh CCCD của từng lần thử có thể xem từ Admin page
- [ ] Dashboard hiển thị tỉ lệ fail theo nguyên nhân trong 7 ngày
- [ ] Admin search theo CCCD/SĐT trả kết quả < 2s

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

**Document Status:** Updated Draft v2.0
**For:** Dev Team (Backend + Frontend + App), BA, Ops
**Next Steps:** Review với Dev Lead → estimate effort → phân task Sprint
