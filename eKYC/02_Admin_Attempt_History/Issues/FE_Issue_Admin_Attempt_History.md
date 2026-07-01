# FE Issue: Admin eKYC — Tra cứu Hành trình

**Service:** ekyc-admin (Admin UI)
**Priority:** Medium
**Type:** New Feature
**Blocked by:** Sub-feature 1 (Biometric Attempt Log) BE endpoints phải live trước

---

## Executive Summary

### Problem Statement

Team vận hành không có công cụ tra cứu lịch sử eKYC của khách hàng. Khi có case hỗ trợ "tại sao khách không mở được TK?", Ops phải hỏi Dev để tra DB thủ công — tốn thời gian và không scalable.

### Current vs Target

| Hiện tại | Sau khi hoàn thành |
|---------|-------------------|
| Không có màn tra cứu lịch sử eKYC | Màn hình tìm kiếm theo CCCD/SĐT |
| Ops phải hỏi Dev tra DB | Ops tự tra cứu trong < 1 phút |
| Không biết user fail mấy lần, lý do gì | Xem timeline đầy đủ từng lần thử |
| Không thấy lý do fail chi tiết | Chi tiết: blur_score, face_compare_prob, fraud flags |

### Success Criteria

- [ ] Ops tìm được hành trình của bất kỳ khách hàng nào trong < 1 phút
- [ ] Thấy được lý do fail cụ thể (ví dụ: "ảnh mặt trước quá mờ, blur_score = 0.23")
- [ ] Link được sang thông tin tài khoản đã mở thành công
- [ ] Admin search theo CCCD/SĐT trả kết quả < 2s

---

## Technical Background

**Base endpoint:** `GET /api/admin/ekyc/attempts`

| Endpoint | Mô tả |
|---------|-------|
| `GET /search?identifierId=xxx` | Tìm kiếm theo CCCD/SĐT |
| `GET /{identifierId}` | Lấy danh sách lần thử |
| `GET /{identifierId}/{attemptNumber}` | Chi tiết một lần thử |

Chi tiết request/response: xem `01_Biometric_Attempt_Log/Specifications/Backend_Spec.md` Section 4.

---

## Detailed Requirements

### Màn hình 1: Tìm kiếm

**Route:** `/admin/ekyc/attempts`

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│ Tra cứu hành trình eKYC                                 │
├──────────────────────────┬──────────────────────────────┤
│ Số CCCD/CMND             │ Số điện thoại                │
│ [________________]       │ [________________]           │
│                    [Tìm kiếm]                           │
└─────────────────────────────────────────────────────────┘
```

**Kết quả:**

| Trường | Nguồn |
|--------|-------|
| Số CCCD | `identifierId` |
| Họ tên | `fullName` từ `e_kyc` |
| SĐT | `phoneNo` |
| Tổng lần thử | `totalAttempts` |
| Trạng thái TK | `accountStatus` + `accountNumber` |
| Thời gian mở TK | `accountOpenedAt` |

**Nút action:** [Xem hành trình →] → navigate sang Màn hình 2
**Empty state:** "Không tìm thấy khách hàng với thông tin này"
**Validation:** Phải nhập ít nhất 1 trong 2 trường (CCCD hoặc SĐT)

---

### Màn hình 2: Customer Journey (Timeline)

**Route:** `/admin/ekyc/attempts/:identifierId`

**Header:**
```
Khách hàng: Nguyễn Văn A
CCCD: 038xxx | SĐT: 09xxx
Tổng lần thử: 3 (2 thất bại, 1 thành công)
Thời gian: 15/05/2025 → 18/05/2025 (3 ngày)
```

**Tài khoản đã mở (nếu có):**
```
✅ Tài khoản: 039C123456
[Xem chi tiết tài khoản →]
```

**Timeline items — mỗi lần thử là 1 item:**
```
[Lần 1 — 15/05/2025 09:23] ❌ Thất bại
  Lý do: Ảnh mờ (VNPT_OCR)
  Chất lượng ảnh trước: Mờ (0.23 / 1.0)
  [Xem chi tiết →]

[Lần 3 — 18/05/2025 10:05] ✅ Thành công
  Matching rate: 92%
  Tài khoản: 039C123456
  [Xem chi tiết →]
```

**Outcome badge colors:** SUCCESS → green | VNPT_FAILED → red | LOTTE_REJECTED → orange | USER_ABANDONED → gray

**Outcome labels:**

| outcome | Hiển thị |
|---------|---------|
| `VNPT_FAILED` | Thất bại — Lỗi xác thực ảnh |
| `LOTTE_REJECTED` | Thất bại — Lỗi từ hệ thống |
| `USER_ABANDONED` | Bỏ dở — Chưa ký hợp đồng |
| `SUCCESS` | Thành công |

**failureStep labels:**

| failureStep | Hiển thị |
|------------|---------|
| `VNPT_OCR` | Đọc thông tin CCCD |
| `VNPT_LIVENESS` | Xác minh khuôn mặt trực tiếp |
| `FACE_COMPARE` | So sánh khuôn mặt |
| `LOTTE_SUBMIT` | Gửi thông tin lên hệ thống |
| `CONTRACT_SIGN` | Ký hợp đồng điện tử |

---

### Màn hình 3: Chi tiết lần thử

**Route:** `/admin/ekyc/attempts/:identifierId/:attemptNumber`
**Breadcrumb:** Tìm kiếm → Nguyễn Văn A → Lần thử #1

**Section 1: Thông tin chung**
```
Lần thử #1 | 15/05/2025 09:23:14
Kết quả: ❌ Thất bại — Ảnh mờ
```

**Section 2: Thông tin OCR (VNPT đọc được)**

| Trường | Giá trị | So sánh với user nhập |
|--------|---------|----------------------|
| Số CCCD | 038xxx | ✓ Khớp |
| Họ tên | NGUYEN VAN A | ✓ Khớp |
| Loại thẻ | CCCD mới | — |
| Confidence số CCCD | 95% | — |
| Điểm MRZ | 8/10 | — |

**Section 3: Chất lượng ảnh**

| Mặt | Độ nét | Độ sáng | Đánh giá |
|-----|--------|---------|---------|
| Mặt trước | 0.23 / 1.0 | 0.75 | ❌ Quá mờ |
| Mặt sau | 0.72 / 1.0 | 0.80 | ✓ Đạt |

> Ngưỡng chấp nhận: blur_score ≥ 0.5, luminance_score ≥ 0.4

**Section 4: Kiểm tra giả mạo**

| Kiểm tra | Kết quả |
|---------|---------|
| Chỉnh sửa ảnh (mặt trước) | ✓ Không phát hiện (1%) |
| Chụp màn hình (mặt trước) | ✓ Ảnh gốc |
| Photocopy (mặt trước) | ✓ Ảnh gốc |
| Tampering tổng thể | ✓ Hợp lệ |
| Số CCCD giả | ✓ Không phát hiện (2%) |
| Trùng lặp CCCD | ✓ Không |

**Section 5: Khớp 2 mặt**

| Trường | Kết quả |
|--------|---------|
| Số CCCD | ✓ Khớp |
| Họ tên | ✓ Khớp |
| Ngày sinh | ✓ Khớp |
| Ngày hết hạn | ✓ Khớp |

**Section 6: Lý do thất bại** (highlight màu đỏ)
```
❌ Ảnh mặt trước quá mờ
   blur_score: 0.23 (yêu cầu ≥ 0.5)
```

---

## Edge Cases

- Chưa có data `ekyc_attempt_log` (khách mở TK trước khi feature go-live): hiển thị "Không có lịch sử lần thử"
- `vnptRawData` null (lỗi trước bước VNPT): ẩn các section VNPT, chỉ hiển thị `outcome` và `failureCode`
- `final_ekyc_id` null (chưa mở TK thành công): ẩn section "Tài khoản đã mở"

---

**Document Status:** 📋 Pending | For: FE Dev (ekyc-admin UI) | Next Steps: Chờ Sub-feature 1 BE endpoints live → implement 3 màn hình
