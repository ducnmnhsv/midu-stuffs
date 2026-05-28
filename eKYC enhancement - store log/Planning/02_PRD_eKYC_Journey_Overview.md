# PRD: eKYC Attempt History — Tổng quan Luồng & Hệ thống Thu thập Dữ liệu

**Version:** 1.0 | **Date:** 2026-05-28 | **Author:** BA Team  
**Status:** Draft | **For:** PM / BA / Dev Lead / QA

---

## 1. Bối cảnh & Vấn đề

### 1.1 eKYC là gì trong hệ thống NHSV Pro?

Quy trình eKYC là cửa ngõ bắt buộc để khách hàng mở tài khoản chứng khoán. Khách hàng phải:
1. Chụp ảnh CCCD hai mặt (xử lý bởi VNPT AI SDK)
2. Quét khuôn mặt để xác minh liveness
3. So khớp khuôn mặt với ảnh CCCD
4. Ký hợp đồng điện tử
5. Lotte Securities phê duyệt → tài khoản được tạo

### 1.2 Vấn đề hiện tại

**Vấn đề cốt lõi:** Hệ thống hiện tại **xóa lịch sử mỗi khi khách hàng thử lại**. Khi user submit eKYC lần 2, lần 1 bị xóa. Kết quả:

- Không biết khách hàng đã thử bao nhiêu lần
- Không biết fail ở bước nào, vì lý do gì
- Không thể phân tích nguyên nhân để cải thiện UX
- Không có bằng chứng cho compliance/audit khi cần điều tra

**Quy mô ảnh hưởng:**
- Mỗi ngày có N khách hàng thử eKYC — không có baseline data về tỉ lệ fail
- Team CS phải hỏi thủ công từng case khi khách hàng phản ánh
- Không thể đo được "bottleneck" nằm ở bước nào để prioritize cải tiến

---

## 2. Mục tiêu

| Mục tiêu | Đo lường thành công |
|----------|-------------------|
| Lưu lại **mọi lần thử** eKYC, kể cả fail | 100% attempt có record trong DB |
| Admin tra cứu được hành trình của từng khách hàng | Tìm theo CCCD/SĐT → xem N lần thử |
| Có dashboard để đo tỉ lệ fail theo từng bước | Dashboard refresh < 5 phút, số liệu chính xác |
| Lưu ảnh CCCD của các lần OCR fail | Admin xem được ảnh → hỗ trợ điều tra |
| Phát hiện sớm xu hướng bất thường | Alert khi tỉ lệ fail tăng đột biến |

**Ngoài scope:**
- Thay đổi luồng mở tài khoản của user (UX không đổi)
- Tích hợp với Lotte (chỉ thêm logging, không sửa Lotte integration)

---

## 3. Đối tượng sử dụng

| Người dùng | Nhu cầu |
|-----------|--------|
| **CS Team** | Tra cứu khi khách hàng phản ánh "đăng ký mãi không được" |
| **Product/BA** | Đo conversion rate, tìm bottleneck UX để cải thiện |
| **Compliance** | Audit trail khi có yêu cầu từ cơ quan quản lý |
| **Tech Lead** | Monitor lỗi SDK/VNPT/Lotte để phát hiện issue hệ thống |

---

## 4. Luồng eKYC Chi tiết

### 4.1 Tổng quan các bước

```
[B1] User nhập SĐT + Email          ← dữ liệu có từ đây
        ↓
[B2] Xác thực OTP                   ← user đã có session
        ↓
[B3] Chụp ảnh CCCD mặt trước        ← VNPT SDK xử lý
        ↓
[B4] Chụp ảnh CCCD mặt sau + MRZ    ← VNPT SDK đọc MRZ
        ↓
[B5] VNPT OCR & Fraud Check         ← App đọc kết quả SDK
        ↓ nếu fail → DROP [★]
[B6] Quét khuôn mặt (Liveness)      ← VNPT SDK
        ↓ nếu fail → DROP [★]
[B7] So khớp khuôn mặt (Face Compare) ← VNPT SDK
        ↓ nếu fail → DROP [★]
[B8] Xác nhận thông tin             ← User review
        ↓
[B9] Gọi Lotte (/lotte/ekycs)       ← Backend → Lotte
        ↓ nếu reject → DROP [★]
[B10] Ký hợp đồng điện tử          ← User ký
        ↓ nếu bỏ qua sau 48h → ABANDONED [★]
[B11] Tài khoản được mở            ← SUCCESS ✅
```

> **[★]** = Các điểm fail cần được log vào hệ thống TradeX

### 4.2 Dữ liệu có tại mỗi điểm fail

| Điểm fail | SĐT | Email | Số CCCD | Tên | Ảnh CCCD | Lý do fail |
|-----------|-----|-------|---------|-----|---------|-----------|
| B5 — OCR fail (ảnh mờ, warning) | ✅ | ✅ | ⚠️ Partial | ⚠️ Partial | ✅ Có | ✅ Warning code + blur score |
| B5 — MRZ validation fail | ✅ | ✅ | ⚠️ Partial | ⚠️ Partial | ✅ Có | ✅ MRZ score + mismatch detail |
| B6 — Liveness fail | ✅ | ✅ | ✅ OCR đã đọc được | ✅ | Không cần | ✅ Liveness result |
| B7 — Face compare fail | ✅ | ✅ | ✅ | ✅ | Không cần | ✅ Compare prob |
| B9 — Lotte reject | ✅ | ✅ | ✅ | ✅ | Không cần | ✅ Lotte error code |
| B10 — Bỏ ký HĐ | ✅ | ✅ | ✅ | ✅ | Không cần | Timeout 48h |

> **Lưu ý quan trọng:** SĐT và Email **luôn có** vì user đã xác thực OTP trước khi vào luồng eKYC. Thông tin này được lưu trong session JWT/Redux state của App.

### 4.3 Trường hợp đặc biệt — OCR fail + Drop ngay

Đây là scenario khó nhất về mặt data:

```
User chụp ảnh CCCD → VNPT OCR fail (ảnh quá mờ)
→ App hiển thị thông báo lỗi
→ User đóng app / bỏ cuộc
```

**Dữ liệu FE có được tại điểm này:**

| Loại dữ liệu | Nguồn | Ghi chú |
|-------------|-------|---------|
| Số điện thoại | JWT Token / Redux state | Luôn có |
| Email | Redux state (từ bước đăng ký) | Luôn có |
| Số CCCD | SDK OCR result (`object.id`) | Có thể partial/empty nếu ảnh quá mờ |
| Họ tên | SDK OCR result (`object.name`) | Có thể partial/empty |
| Ngày sinh | SDK OCR result (`object.birth_day`) | Có thể partial/empty |
| Loại giấy tờ | SDK OCR result (`object.card_type`) | CC / CMND |
| MRZ dòng 1 & 2 | SDK OCR result (`object.mrz`) | Có thể partial |
| Ảnh mặt trước | SDK (`LOG_PATH_IMAGE_FRONT`) | **Luôn có** — local file path |
| Ảnh mặt sau | SDK (`LOG_PATH_IMAGE_BACK`) | **Luôn có** — local file path |
| Lý do fail | `general_warning[0]` + blur_score | Luôn có |

**Kết luận:** FE **đủ dữ liệu** để log attempt. Ngay cả khi OCR không đọc được số CCCD, hệ thống vẫn có SĐT + Email làm định danh chính, và ảnh gốc để Admin xem lại.

---

## 5. Kiến trúc Hệ thống Thu thập

### 5.1 Hai luồng ghi log

**Luồng A — Pre-submit failure (App tự log, không qua Lotte):**

```
App phát hiện lỗi từ SDK (OCR/liveness/MRZ/face compare)
    ↓
App gọi POST /ekycs/attempt-log {
    phone, email,
    outcome: VNPT_FAILED / MRZ_FAILED / FACE_COMPARE_FAILED,
    failureStep, failureCode, failureMessage,
    imageFrontBase64 (bắt buộc nếu OCR fail),
    imageBackBase64,
    mrzLine1, mrzLine2, mrzProb, mrzCrossCheck, ...
}
    ↓
TradeX BE → upload ảnh S3 → lưu ekyc_attempt_log
```

**Luồng B — Post-submit (App gọi Lotte rồi mới log):**

```
App → POST /lotte/ekycs  (không đổi, chỉ gửi Lotte data)
    ↓
Nhận response từ Lotte (success / reject)
    ↓
App gọi POST /ekycs/attempt-log {
    outcome: SUCCESS / LOTTE_REJECTED,
    vnptRawData: <raw data đã gửi Lotte>,  ← BE tự extract VNPT fields
    mrzCrossCheck, liveness results, face compare, ...  ← SDK-only data
}
    ↓
TradeX BE lưu ekyc_attempt_log (không upload ảnh — OCR đã pass)
```

> **Nguyên tắc thiết kế:**
> - `POST /lotte/ekycs` — **không thay đổi**, không thêm field mới
> - `POST /ekycs/attempt-log` — API TradeX mới, **hoàn toàn độc lập** với Lotte
> - Nếu API log fail → **không ảnh hưởng user** (fire-and-forget)

### 5.2 Dữ liệu lưu xuống DB

**Bảng `ekyc_attempt_log`** — append-only, không update, không delete:

| Nhóm | Thông tin lưu |
|------|--------------|
| Định danh | Số CCCD, SĐT, Email |
| Lần thử | Số thứ tự lần thử, thời điểm, kết quả |
| VNPT OCR | Số CCCD đọc được, tên, ngày sinh, loại thẻ, confidence score |
| Fraud Detection | Tampering, fake warning, duplication, photocopy detection |
| Chất lượng ảnh | Blur score, luminance score (mặt trước + mặt sau) |
| MRZ | 2 dòng MRZ thô, overall confidence, valid score, cross-check 4 trường |
| Liveness | Kết quả từng bước (card front/rear, face, mask) |
| Face Compare | MATCH/NOMATCH, similarity score |
| Ảnh | URL ảnh mặt trước/sau trên S3 (chỉ khi OCR fail) |
| Link kết quả | FK → `e_kyc.id` khi tài khoản được mở thành công |

---

## 6. Admin Dashboard — Metrics & Tra cứu

### 6.1 Dashboard KPI

| Metric | Ý nghĩa | Alert condition |
|--------|---------|----------------|
| **Tổng attempt hôm nay** | Lượng người đang thử mở TK | Giảm đột ngột > 50% |
| **Tỉ lệ thành công** | % attempt → account opened | < 60% → alert |
| **Tỉ lệ fail theo bước** | Bottleneck ở OCR / Liveness / Face / Lotte | Bước nào > 30% → review |
| **Số lần thử trung bình** | Người dùng retry bao nhiêu lần | > 2 lần → UX issue |
| **Thời gian hoàn thành trung bình** | Từ lần thử đầu → mở TK thành công | Tăng > 20% → investigate |
| **Fraud flags** | % attempt có fraud warning từ VNPT | Tăng đột biến → alert compliance |
| **Tỉ lệ MRZ mismatch** | % lần thử có MRZ không khớp OCR | > 5% → investigate |

### 6.2 Màn hình tra cứu Customer Journey

**Tìm kiếm:** CCCD hoặc SĐT → xem hành trình

```
Khách hàng: Nguyễn Văn A | CCCD: 038xxx | SĐT: 09xx
Tổng số lần thử: 3 | Thời gian: 3 ngày (15/05 → 18/05)

Lần 1 — 15/05  ❌ VNPT_FAILED — Ảnh mờ (blur 0.23)
                  [Xem ảnh mặt trước] [Xem ảnh mặt sau]

Lần 2 — 16/05  ❌ LOTTE_REJECTED — Matching rate 74%

Lần 3 — 18/05  ✅ THÀNH CÔNG — TK: 039C123456
```

**Chi tiết mỗi lần thử:** OCR data, fraud flags, image quality scores, MRZ cross-check, ảnh OCR (nếu có).

### 6.3 Ảnh OCR trong Admin

- Chỉ lưu ảnh khi **OCR fail** (không cần lưu ảnh khi OCR pass)
- Lưu tại S3/MinIO, private bucket
- Admin xem qua **presigned URL** có TTL giới hạn (1 giờ)
- Mục đích: CS/Compliance xem lại ảnh để hiểu lý do fail

---

## 7. Metrics Framework — GA4 vs BE

### 7.1 So sánh tổng quan

| Tiêu chí | GA4 | BE (TradeX) |
|----------|-----|-------------|
| **Độ chính xác** | Có sampling ở volume cao | Chính xác 100% |
| **Delay** | 24–48h cho một số report | Real-time (hoặc < 5 phút) |
| **PII** | ❌ Không được gửi PII | ✅ Đầy đủ, secure |
| **Correlation với business data** | Khó (cần BigQuery join) | ✅ Native — join trực tiếp với TK |
| **Compliance / Audit trail** | ❌ Không phù hợp | ✅ Bắt buộc |
| **Chi phí phát triển** | Thấp (SDK có sẵn) | Cao hơn (cần build dashboard) |
| **Funnel visualization** | ✅ Built-in, đẹp | Cần tự build |
| **A/B testing** | ✅ Hỗ trợ tốt | Cần tự implement |
| **Cohort / Retention** | ✅ Built-in | Cần tự build |
| **Alert khi metric đột biến** | Hạn chế | ✅ Flexible |

### 7.2 Khuyến nghị: Dùng cả hai, phân vai rõ ràng

**BE (TradeX) — phụ trách:**
- Tất cả business metrics trên admin dashboard (tỉ lệ success/fail, breakdown theo bước)
- Compliance audit trail — bắt buộc, không thể dùng GA4 thay thế
- Alert hệ thống khi tỉ lệ fail bất thường
- Tra cứu customer journey cho CS team
- Dữ liệu có chứa PII (CCCD, SĐT, tên)

**GA4 — phụ trách:**
- UX behavior analytics: thời gian user dừng lại ở mỗi bước, tap count, scroll pattern
- Funnel drop-off visualization đẹp cho product review
- A/B test khi thay đổi UX eKYC (thay text, thay màu nút, v.v.)
- Re-engagement: user đã fail, bao lâu quay lại thử tiếp
- **Quan trọng:** Chỉ gửi **event names + anonymized metrics** (VD: `ekyc_step_failed`, `step: 'ocr'`, `attempt_number: 2`) — **KHÔNG gửi PII**

**Câu trả lời ngắn:** GA4 không thể **thay thế** BE metrics trong eKYC vì vấn đề PII và compliance. Nhưng GA4 **bổ sung** rất tốt cho phân tích UX mà BE không làm được hiệu quả.

### 7.3 Events nên gửi lên GA4 (anonymized)

| Event Name | Params (không có PII) | Mục đích |
|-----------|----------------------|---------|
| `ekyc_started` | `attempt_number` | Funnel top |
| `ekyc_ocr_failed` | `failure_code`, `blur_score_range`, `attempt_number` | Measure OCR UX |
| `ekyc_mrz_failed` | `check_failed: ['id', 'dob']`, `attempt_number` | MRZ UX |
| `ekyc_liveness_failed` | `step`, `attempt_number` | Liveness UX |
| `ekyc_lotte_rejected` | `attempt_number` | Post-submit fail |
| `ekyc_completed` | `total_attempts`, `duration_days` | Success funnel |
| `ekyc_abandoned` | `last_step`, `days_since_start` | Drop-off analysis |

---

## 8. Kế hoạch Triển khai

### 8.1 Phân chia theo team

| Team | Việc cần làm | Phụ thuộc |
|------|-------------|----------|
| **BE (ekyc-admin)** | Tạo bảng `ekyc_attempt_log`, implement `POST /ekycs/attempt-log`, image upload service, Admin UI APIs | DB migration |
| **FE App (nhsv-mts-rn)** | Gọi `POST /ekycs/attempt-log` ở mọi failure point, gửi ảnh khi OCR fail, MRZ validation & cross-check | BE API sẵn sàng |
| **FE Admin (ekyc-admin UI)** | Dashboard page, Customer Journey modal, ảnh preview | BE Admin APIs |

### 8.2 Sequence đề xuất

```
Phase 1 (Backend foundation):
  → Migration DB: tạo ekyc_attempt_log
  → Implement POST /ekycs/attempt-log
  → Image upload to S3

Phase 2 (App integration):
  → App gọi attempt-log khi pre-submit fail
  → App gọi attempt-log sau Lotte response
  → MRZ validation + cross-check

Phase 3 (Admin UI):
  → Dashboard KPIs
  → Customer journey search + detail
```

---

## 9. Rủi ro & Điểm cần làm rõ

| # | Rủi ro / Open Question | Mức độ | Người confirm |
|---|----------------------|--------|--------------|
| R1 | `mrz_valid_score` range & threshold — VNPT chưa cung cấp | 🔴 Chặn MRZ validation | VNPT SDK team |
| R2 | S3/MinIO đã có trên hệ thống chưa? Bucket nào dùng? | 🟡 Chặn image storage | Infra team |
| R3 | Retention policy cho ảnh OCR? Bao lâu xóa? | 🟡 Compliance | Compliance/Legal |
| R4 | Email có trong JWT không? Hay phải App tự gửi? | 🟡 Ảnh hưởng API design | BE/Auth team |
| R5 | Partial CCCD khi OCR fail có đủ để định danh không? | 🟢 Có SĐT làm backup | BA/Product |
| R6 | Volume ảnh — mỗi ngày bao nhiêu attempt? Ước tính storage? | 🟢 Cần estimate | Analytics/Infra |

---

## 10. Tiêu chí Thành công

- **Week 1 sau launch:** 100% attempt có record trong `ekyc_attempt_log`
- **Week 2:** Admin có thể tra cứu customer journey theo CCCD/SĐT
- **Month 1:** Dashboard hiển thị tỉ lệ fail theo từng bước với data chính xác
- **Month 1:** Team CS giảm thời gian xử lý complaint eKYC từ điều tra thủ công → tra cứu < 2 phút
- **Ongoing:** Phát hiện và alert khi tỉ lệ fail một bước tăng > 20% so với baseline 7 ngày

---

**Document Status:** Draft v1.0 | **For:** PM / Dev Lead / QA | **Next Steps:** Confirm open questions R1–R4 → kick off Phase 1
