# FE Issue: MRZ Validation & OCR Cross-Check

**Service:** nhsv-mts-rn (React Native App)  
**Type:** Security Enhancement  
**Priority:** High  
**Date:** 2026-05-24

---

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

SDK VNPT trả về 4 trường dữ liệu MRZ (Machine Readable Zone — vùng mã hóa ở mặt sau CCCD) nhưng **App hiện tại không đọc, không validate, không gửi lên BE** bất kỳ trường nào trong số đó. Ngoài ra, App cũng không thực hiện cross-check độc lập giữa MRZ và dữ liệu OCR visual.

MRZ là cơ chế bảo mật mạnh nhất trên CCCD: chứa **checksum tích hợp**, rất khó làm giả. Bỏ qua MRZ đồng nghĩa bỏ qua một lớp bảo vệ quan trọng chống giả mạo danh tính.

### Current vs Target

| Điểm | Hiện tại | Mục tiêu |
|------|---------|----------|
| MRZ data | SDK trả về, App bỏ qua hoàn toàn | Validate + gửi lên BE |
| Cross-check MRZ ↔ OCR | Không thực hiện | Thực hiện, block nếu mismatch |
| `mrz_valid_score` | Không kiểm tra | Block nếu dưới ngưỡng |
| `default: break` | Cho phép tiếp tục với unknown warning | Block + log analytics |
| Dữ liệu audit trail | MRZ không có trong BE logs | Đầy đủ trong `ekyc_attempt_log` |

### Solution Approach (HIGH-LEVEL)

1. **Đọc 4 trường MRZ** từ SDK response và lưu vào Redux state
2. **Parse MRZ line 2** để lấy CCCD, ngày sinh, giới tính, ngày hết hạn
3. **Cross-check** 4 trường MRZ parsed với OCR visual — block nếu có sai lệch
4. **Validate `mrz_valid_score`** — block nếu dưới ngưỡng (cần confirm với VNPT)
5. **Gửi MRZ data lên BE** trong request payload để lưu vào `ekyc_attempt_log`
6. **Fix `default: break`** — đổi sang block + analytics log

### Timeline

| Phase | Nội dung | Estimate | Phụ thuộc |
|-------|---------|---------|-----------|
| Phase 1 | Fix `default: break` → block + analytics | 0.5 ngày | ✅ Không phụ thuộc — **có thể bắt đầu ngay** |
| Phase 2 | Đọc & validate MRZ (`valid_score` + cross-check) | 1 ngày | 🔴 **Chờ VNPT confirm** `mrz_valid_score` threshold (Q1) |
| Phase 3 | Gửi MRZ data lên BE (`POST /ekycs/attempt-log`) | 0.5 ngày | 🟡 Chờ BE deploy endpoint |

> **Khuyến nghị:** Phase 1 nên làm ngay trong sprint này vì không có dependency. Phase 2 & 3 chờ unblock.

**Tổng:** ~2 ngày dev + 1 ngày test

### Success Criteria

- [ ] `mrz_valid_score` dưới ngưỡng → user bị chặn, thông báo "Giấy tờ không hợp lệ"
- [ ] MRZ CCCD ≠ OCR CCCD → user bị chặn
- [ ] MRZ ngày sinh ≠ OCR ngày sinh → user bị chặn
- [ ] Unknown warning code → bị chặn (không còn `break` im lặng)
- [ ] BE nhận được `mrzLine1`, `mrzLine2`, `mrzProb`, `mrzValidScore`, `mrzCrossCheckResult`
- [ ] Admin page hiển thị MRZ cross-check trong Attempt Detail

---

## 🔍 Technical Background (PM CAN SKIP)

### MRZ là gì?

Machine Readable Zone — 2 dòng ký tự ở mặt sau CCCD, chuẩn ICAO 9303. Mỗi nhóm dữ liệu trong MRZ có **checksum tích hợp** — nếu checksum sai, dữ liệu bị thay đổi. Đây là cơ chế bảo vệ chống giả mạo độc lập với AI OCR visual.

**Cấu trúc MRZ dòng 2 (CCCD Việt Nam):**
```
Pos  0- 8 : Số CCCD (9 ký tự đầu)
Pos  9    : Check digit 1 (checksum số CCCD)
Pos 10-15 : Ngày sinh (YYMMDD)
Pos 16    : Check digit 2 (checksum ngày sinh)
Pos 17    : Giới tính (M/F)
Pos 18-23 : Ngày hết hạn (YYMMDD)
Pos 24    : Check digit 3 (checksum ngày hết hạn)
Pos 25-27 : Quốc tịch (VNM)
Pos 28-34 : Dự phòng / số phụ
Pos 35    : Check digit tổng (composite checksum)
```

### SDK trả về 4 trường MRZ

**File:** `src/interfaces/authentication.ts:862–865`

```typescript
export interface IEKYCOCRResult {
  object: {
    // ❌ Không được đọc ở bất kỳ đâu trong EKYCScanIdDone.ts:
    mrz: string[];           // ["IDVNM...", "9001151M300..."] — 2 dòng MRZ thô
    mrz_prob: number;        // 0.97 — độ tin cậy tổng thể (0-1)
    mrz_probs: number[];     // [0.99, 0.99, 0.45, ...] — độ tin cậy từng ký tự
    mrz_valid_score: number; // checksum tổng hợp — thấp = CCCD có vấn đề
  }
}
```

**Kết quả grep trong EKYCScanIdDone.ts:**
```
$ grep -n "mrz" EKYCScanIdDone.ts
(no results)
```
Từ khóa `mrz` không xuất hiện dù một lần trong file xử lý eKYC.

### Lỗ hổng `default: break`

```typescript
// EKYCScanIdDone.ts ~ line 183
switch (dataConverted.logOcr.object.general_warning[0]) {
  case 'chat_luong_anh_dau_vao_khong_dat_chuan': { /* chặn */ return; }
  case 'anh_dau_vao_mo_nhoe': { /* chặn */ return; }
  // ... 13 cases khác ...

  default: {
    break; // ← ⚠️ BUG: bất kỳ warning code không biết đều cho pass qua
  }
}
```

---

## 📝 Detailed Requirements (PM CAN SKIP)

### Task 1 — Fix `default: break` → Block + Analytics

**File:** `src/sagas/authentication/EKYCScanIdDone.ts` (~ line 334)

**Thay đổi:**
```typescript
// TRƯỚC:
default: {
  break;
}

// SAU:
default: {
  // Log để track warning codes mới từ SDK
  analytics().logEvent('ekyc_unknown_warning_code', {
    warning_code: dataConverted.logOcr.object.general_warning[0] ?? 'empty',
    screen: 'EKYCScanIdDone',
  });
  // Chặn an toàn với thông báo chung
  yield put(setWarning({
    content: i18n.t('EKYC.WARNING.CONTENT24'), // "Đã có lỗi xảy ra. Vui lòng chụp lại"
    button: i18n.t('EKYC.WARNING.BUTTON'),
  }));
  return;
}
```

---

### Task 2 — Đọc và validate MRZ

**File:** `src/sagas/authentication/EKYCScanIdDone.ts`

#### 2a. Thêm MRZ vào Redux (nếu chưa có)

**File:** `src/reduxs/authentication/ekyc/reducer.ts` (hoặc tương đương)
```typescript
// Thêm vào EKYCState:
mrzLine1: string | null;
mrzLine2: string | null;
mrzProb: number | null;
mrzValidScore: number | null;
mrzCrossCheckResult: 'PASS' | 'FAIL' | 'SKIP' | null;
mrzCrossCheckDetails: {
  idMatch: boolean | null;
  dobMatch: boolean | null;
  genderMatch: boolean | null;
  expiryMatch: boolean | null;
} | null;
```

#### 2b. Validate `mrz_valid_score`

Thêm vào `EKYCScanIdDone.ts` sau khi xử lý `general_warning`:
```typescript
const MRZ_VALID_SCORE_THRESHOLD = 5; // TBD — cần confirm với VNPT (range 0-10)

const mrzValidScore = dataConverted.logOcr.object?.mrz_valid_score;
if (mrzValidScore != null && mrzValidScore < MRZ_VALID_SCORE_THRESHOLD) {
  analytics().logEvent('ekyc_mrz_invalid', { score: mrzValidScore });
  yield put(setWarning({
    content: i18n.t('EKYC.WARNING.MRZ_INVALID'), // "Giấy tờ không hợp lệ. Vui lòng chụp lại mặt sau"
    button: i18n.t('EKYC.WARNING.BUTTON'),
  }));
  return;
}
```

> ⚠️ **Action required:** Liên hệ VNPT để xác nhận range và threshold hợp lệ của `mrz_valid_score` trước khi set ngưỡng cứng.

#### 2c. Parse MRZ line 2 và cross-check với OCR

```typescript
// Helper parse MRZ dòng 2
function parseMrzLine2(mrzLine2: string) {
  if (!mrzLine2 || mrzLine2.length < 36) return null;
  const pad = (s: string) => s.replace(/</g, '');
  return {
    citizenId: pad(mrzLine2.substring(0, 9)),       // pos 0-8
    dob: mrzLine2.substring(10, 16),                 // pos 10-15 (YYMMDD)
    gender: mrzLine2[17],                            // pos 17 (M/F)
    expiry: mrzLine2.substring(18, 24),              // pos 18-23 (YYMMDD)
  };
}

// Cross-check trong saga, sau khi lưu OCR fields vào state:
const mrz = dataConverted.logOcr.object?.mrz;
if (mrz && mrz.length >= 2) {
  const parsed = parseMrzLine2(mrz[1]);
  const ocrId  = dataConverted.logOcr.object?.id;         // OCR citizen ID
  const ocrDob = dataConverted.logOcr.object?.birth_day;  // DD/MM/YYYY từ OCR

  if (parsed && ocrId && ocrDob) {
    // So khớp số CCCD (9 ký tự đầu trong MRZ vs full CCCD từ OCR)
    const mrzId = parsed.citizenId;
    const idMatch = ocrId.startsWith(mrzId) || ocrId.includes(mrzId);

    // So khớp ngày sinh: MRZ YYMMDD → DD/MM/YYYY
    const [dd, mm, yyyy] = ocrDob.split('/');
    const ocrDobMrz = `${yyyy.slice(2)}${mm}${dd}`; // chuyển sang YYMMDD
    const dobMatch = parsed.dob === ocrDobMrz;

    const crossCheckDetails = { idMatch, dobMatch };

    if (!idMatch || !dobMatch) {
      analytics().logEvent('ekyc_mrz_ocr_mismatch', { idMatch, dobMatch });
      yield put(setWarning({
        content: i18n.t('EKYC.WARNING.MRZ_OCR_MISMATCH'),
        // "Thông tin trên giấy tờ không khớp. Vui lòng chụp lại"
        button: i18n.t('EKYC.WARNING.BUTTON'),
      }));
      return;
    }

    // Lưu kết quả vào state để gửi lên BE
    yield put(setMrzData({
      mrzLine1: mrz[0],
      mrzLine2: mrz[1],
      mrzProb: dataConverted.logOcr.object?.mrz_prob ?? null,
      mrzValidScore: mrzValidScore ?? null,
      mrzCrossCheckResult: 'PASS',
      mrzCrossCheckDetails: crossCheckDetails,
    }));
  }
}
```

---

### Task 3 — Tích hợp `POST /ekycs/attempt-log` (API TradeX mới)

> **Nguyên tắc quan trọng:**
> - `POST /lotte/ekycs` — **KHÔNG THAY ĐỔI**. Giữ nguyên hoàn toàn.
> - `POST /ekycs/attempt-log` — API TradeX mới. App gọi riêng để lưu log vào hệ thống TradeX.

#### 3a. Khi nào App gọi `POST /ekycs/attempt-log`?

**Case 1 — Pre-submit failure (SDK thất bại, không gọi Lotte):**
```
VNPT SDK trả lỗi (blur / warning / MRZ fail / liveness fail)
    ↓
App gọi POST /ekycs/attempt-log {
  outcome: "VNPT_FAILED" | "MRZ_FAILED" | "FACE_COMPARE_FAILED",
  failureStep: "VNPT_OCR" | "MRZ_VALIDATION" | ...,
  imageFrontBase64: required khi failureStep = VNPT_OCR,
  imageBackBase64: required khi failureStep = VNPT_OCR,
  mrzLine1, mrzLine2, mrzProb, mrzCrossCheck, ...
}
    ↓
KHÔNG gọi /lotte/ekycs
```

**Case 2 — Post-submit (App gọi Lotte sau khi SDK pass):**
```
SDK pass → App gọi POST /lotte/ekycs  (giữ nguyên, không thêm field)
    ↓
Chờ Lotte response (success / reject)
    ↓
App gọi POST /ekycs/attempt-log {
  outcome: "SUCCESS" | "LOTTE_REJECTED",
  vnptRawData: "<same base64 rawData đã gửi cho Lotte>",  ← BE dùng để extract VNPT fields
  mrzLine1, mrzLine2, mrzProb, mrzCrossCheck, ...,  ← SDK-only, BE không có từ rawData
  livenessFaceResult, faceCompareMsg, faceCompareProb, ...
  // Ảnh: KHÔNG cần (OCR đã pass, không cần lưu ảnh fail)
}
```

#### 3b. Implementation — Pre-submit failure

**File:** `src/sagas/authentication/EKYCScanIdDone.ts`

Khi phát hiện lỗi từ SDK (sau Task 1 và 2), gọi API trước khi `return`:
```typescript
// Trong các block yield put(setWarning(...)); return; — thêm API call trước:
async function logAttemptToTradeX(payload: EKycAttemptLogRequest) {
  try {
    await apiService.post('/ekycs/attempt-log', payload);
  } catch (err) {
    // Fire-and-forget: không block user flow nếu log API fail
    analytics().logEvent('ekyc_attempt_log_failed', { error: err?.message });
  }
}

// Ví dụ khi OCR fail (blur):
yield call(logAttemptToTradeX, {
  outcome: 'VNPT_FAILED',
  failureStep: 'VNPT_OCR',
  failureCode: 'IMAGE_BLURRED',
  failureMessage: `blur_score front: ${blurScore}`,
  imageFrontBase64: await fileToBase64(logPathImageFront),  // từ LOG_PATH_IMAGE_FRONT
  imageBackBase64:  await fileToBase64(logPathImageBack),
  mrzLine1:    mrzData?.line1,
  mrzLine2:    mrzData?.line2,
  mrzProb:     mrzData?.prob,
  mrzValidScore: mrzData?.validScore,
  mrzCrossCheck: 'SKIPPED',  // chưa thực hiện cross-check vì OCR fail trước
});
yield put(setWarning({ content: i18n.t('EKYC.WARNING.BLUR'), ... }));
return;

// Ví dụ khi MRZ cross-check fail:
yield call(logAttemptToTradeX, {
  outcome: 'MRZ_FAILED',
  failureStep: 'MRZ_CROSS_CHECK',
  failureCode: 'MRZ_OCR_ID_MISMATCH',
  imageFrontBase64: await fileToBase64(logPathImageFront),
  imageBackBase64:  await fileToBase64(logPathImageBack),
  mrzLine1, mrzLine2, mrzProb, mrzValidScore,
  mrzCrossCheck: 'FAIL',
  mrzCheckId: 'MISMATCH',
  mrzCheckDob: 'MATCH',
  mrzCheckGender: 'MATCH',
  mrzCheckExpiry: 'MATCH',
});
```

#### 3c. Implementation — Post-submit

**File:** `src/sagas/authentication/OnPressNextInConfirmPolicyScreen.ts`

```typescript
// Bước 1: Gọi Lotte (không thay đổi)
const lotteResponse = yield call(apiService.post, '/lotte/ekycs', {
  rawData: vnptRawData,
  identifierId: cccd,
  // ... các field hiện tại, KHÔNG thêm gì mới
});

// Bước 2: Gọi TradeX để lưu log (fire-and-forget, không block UX)
yield call(logAttemptToTradeX, {
  outcome: lotteResponse.success ? 'SUCCESS' : 'LOTTE_REJECTED',
  failureStep: lotteResponse.success ? null : 'LOTTE_SUBMIT',
  failureCode: lotteResponse.errorCode ?? null,
  vnptRawData: vnptRawData,   // BE dùng để extract VNPT fields
  // SDK-specific fields (BE không có từ rawData):
  mrzLine1, mrzLine2, mrzProb,
  mrzValidScore, mrzCrossCheck,
  mrzCheckId, mrzCheckDob, mrzCheckGender, mrzCheckExpiry,
  livenessCardFrontResult, livenessCardRearResult,
  livenessFaceResult, faceMaskResult,
  fakeLivenessProb, fakePrintPhotoProb,
  faceCompareMsg, faceCompareProb,
  // Ảnh: KHÔNG cần (SDK đã pass — OCR image quality đủ tốt)
});
```

> **Fire-and-forget pattern:** Lỗi từ `POST /ekycs/attempt-log` **không được throw lên UX**.
> User không biết / không bị ảnh hưởng nếu log API tạm thời fail.
> Nên wrap trong try-catch và log analytics.

---

### i18n Keys cần thêm

**File:** `src/i18n/vi.json` (và `en.json`)

```json
{
  "EKYC": {
    "WARNING": {
      "MRZ_INVALID": "Giấy tờ không hợp lệ. Vui lòng kiểm tra và chụp lại mặt sau CCCD.",
      "MRZ_OCR_MISMATCH": "Thông tin trên giấy tờ không khớp nhau. Vui lòng chụp lại.",
      "MRZ_LOW_CONFIDENCE": "Không thể đọc rõ thông tin giấy tờ. Vui lòng chụp lại mặt sau CCCD."
    }
  }
}
```

---

### Files cần thay đổi

| File | Loại thay đổi |
|------|--------------|
| `src/sagas/authentication/EKYCScanIdDone.ts` | Fix `default: break`, thêm MRZ validation + cross-check |
| `src/sagas/authentication/OnPressNextInConfirmPolicyScreen.ts` | Thêm MRZ fields vào request payload |
| `src/reduxs/authentication/ekyc/reducer.ts` (hoặc tương đương) | Thêm MRZ state fields |
| `src/interfaces/authentication.ts` | Thêm MRZ fields vào request interface |
| `src/i18n/vi.json` + `en.json` | Thêm 3 warning keys mới |

---

### Open Questions

| # | Câu hỏi | Người trả lời | Mức độ chặn |
|---|---------|--------------|-------------|
| Q1 | `mrz_valid_score` range là gì? Ngưỡng hợp lệ là bao nhiêu? | VNPT SDK team | 🔴 Chặn Phase 2 |
| Q2 | Khi cross-check MRZ CCCD vs OCR CCCD — CCCD 12 số vs 9 số trong MRZ: startsWith đủ chưa hay cần so sánh full? | BE/VNPT | 🟡 Cần làm rõ |
| Q3 | `mrz_probs` (mảng per-character confidence) có cần lưu vào BE không? Hay chỉ `mrz_prob` tổng là đủ? | BE team | 🟢 Không chặn |

---

**Document Status:** Draft | **For:** FE Dev (nhsv-mts-rn) | **Next Steps:** Confirm MRZ threshold với VNPT → implement Phase 1 trước (fix `default: break`, không phụ thuộc VNPT)
