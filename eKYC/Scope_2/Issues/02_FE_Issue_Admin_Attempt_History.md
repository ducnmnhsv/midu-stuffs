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
| Không có màn tra cứu lịch sử eKYC | Danh sách + tra cứu theo CCCD/SĐT/Họ tên |
| Ops phải hỏi Dev tra DB | Ops tự tra cứu trong < 1 phút |
| Không biết user fail mấy lần, lý do gì | Xem timeline đầy đủ từng lần thử |
| Không thấy lý do fail chi tiết | Chi tiết: blur_score, face_compare_prob, fraud flags |
| Không phân biệt được ai đã mở TK / ai chưa | **2 tab riêng: "Đã mở TK thành công" / "Chưa mở TK thành công"** — track tổng quan không cần tra từng CCCD |
| Khách fail ngay từ SDK (chưa chạm Lotte) không tra được | Tra được đầy đủ — nguồn là `ekyc_attempt_log`, không phụ thuộc `e_kyc` |

### Success Criteria

- [ ] Ops tìm được hành trình của bất kỳ khách hàng nào trong < 1 phút
- [ ] Thấy được lý do fail cụ thể (ví dụ: "ảnh mặt trước quá mờ, blur_score = 0.23")
- [ ] Link được sang thông tin tài khoản đã mở thành công
- [ ] Admin search theo CCCD/SĐT trả kết quả < 2s
- [ ] **Ops browse được toàn bộ danh sách khách "chưa mở TK thành công" qua 1 tab riêng, không cần biết trước CCCD** — dùng để track case cần follow-up
- [ ] Khách chưa từng chạm tới Lotte (chưa có `e_kyc`) vẫn xuất hiện đầy đủ trong tab "Chưa mở TK thành công"

---

## Technical Background

**Base endpoint:** `GET /api/admin/ekyc/attempts`

| Endpoint | Mô tả |
|---------|-------|
| `GET /search?identifierId=xxx` | Tìm kiếm theo CCCD/SĐT |
| `GET /{identifierId}` | Lấy danh sách lần thử |
| `GET /{identifierId}/{attemptNumber}` | Chi tiết một lần thử |

Chi tiết request/response: xem `../../Scope_1/Specifications/BE_Spec.md` (Phần A) Section 4.

---

## Detailed Requirements

### Màn hình 1: Danh sách + Tra cứu (2 tab)

**Route:** `/admin/ekyc/attempts`

> **Thiết kế 2026-07-01:** Đổi từ pure search-by-ID sang **danh sách phân trang + 2 tab**, để Ops dễ track tổng quan mà không cần biết trước CCCD/SĐT cần tra. Search box vẫn giữ, hoạt động như filter trong tab hiện tại.

**Layout:**
```
┌───────────────────────────────────────────────────────────────────┐
│ Tra cứu hành trình eKYC                                          │
├───────────────────────────────┬───────────────────────────────────┤
│ ✅ Đã mở TK thành công (1,089) │ ⚠️ Chưa mở TK thành công (158)     │  ← 2 tab
├───────────────────────────────┴───────────────────────────────────┤
│ [Từ ngày] [Đến ngày] [Tìm kiếm: CCCD/SĐT/Họ tên______] [Làm mới] │
├───────────────────────────────────────────────────────────────────┤
│  (bảng danh sách — nội dung khác nhau theo tab, xem dưới)         │
└───────────────────────────────────────────────────────────────────┘
```

**API:** `GET /api/admin/ekyc/attempts/list?accountStatus={APPROVED|NOT_APPROVED}&keyword=&fromDate=&toDate=&page=&size=` — xem `../../Scope_1/Specifications/BE_Spec.md` (Phần A) Section 4.1b.

**Tab 1 — "Đã mở TK thành công" (`accountStatus=APPROVED`):**

| Cột | Nguồn |
|-----|-------|
| Số CCCD | `identifierId` |
| Họ tên | `fullName` (luôn có — từ `e_kyc.full_name`) |
| SĐT | `phoneNo` |
| Tổng lần thử | `totalAttempts` |
| Số TK | `accountNumber` |
| Thời gian mở TK | `accountOpenedAt` |
| Hành trình | [Xem →] → Màn hình 2 |

**Tab 2 — "Chưa mở TK thành công" (`accountStatus=NOT_APPROVED`):**

| Cột | Nguồn |
|-----|-------|
| Số CCCD | `identifierId` |
| Họ tên | `fullName` — từ OCR (`vnpt_name`) nếu chưa có `e_kyc`, có thể `null` → hiển thị **"Chưa xác định (chưa đọc được OCR)"** |
| SĐT | `phoneNo` |
| Tổng lần thử | `totalAttempts` |
| Trạng thái | Badge màu theo `accountStatus` (xem bảng dưới) |
| Dừng ở bước | `lastFailureStep` label + `lastFailureMessage` — hiển thị ngay trong bảng, không cần click vào chi tiết |
| Cập nhật gần nhất | `lastUpdatedAt` |
| Hành trình | [Xem →] → Màn hình 2 (luôn khả dụng, bất kể chưa có `e_kyc`) |

> ⚠️ **Tab 2 bao gồm khách chưa từng gọi tới Lotte** — fail ngay ở bước OCR/liveness phía SDK, **không có `e_kyc` row nào**. Nguồn dữ liệu là `ekyc_attempt_log`, không phụ thuộc việc đã có `e_kyc` hay chưa. Đây chính là gap đã fix — trước đây các khách này **không tra được** trên admin page.

**`accountStatus` badge (dùng trong Tab 2 + Màn hình 2/3):**

| accountStatus | Badge | Màu | Ý nghĩa |
|---------------|-------|-----|---------|
| `REJECTED` | ❌ Bị từ chối | Đỏ | Đã gửi Lotte, bị reject |
| `PENDING` | ⏳ Đang chờ duyệt | Vàng | `e_kyc.status = PENDING` — thường do fraud flags, chờ Admin duyệt tay |
| `ABANDONED` | ⏸ Bỏ dở ký HĐ | Xám | Lotte approve nhưng chưa ký HĐ |
| `NOT_SUBMITTED` | ⚠️ Chưa gửi hồ sơ | Xám đậm | Fail ngay từ SDK — **chưa từng chạm tới Lotte** |

**Search box:** filter theo CCCD/SĐT/Họ tên **trong tab hiện tại**. Nếu khách tìm thấy ở tab khác (VD: gõ CCCD của khách đã APPROVED trong lúc đang ở Tab 2) → hiển thị gợi ý: *"Không tìm thấy trong tab này. [Khách hàng này đã mở TK thành công — xem ở tab Đã mở TK →]"*

**Empty state mỗi tab:** "Không có khách hàng nào trong khoảng thời gian này"
**Date filter:** áp dụng theo `attempt_at` của lần thử gần nhất — mặc định 30 ngày qua

---

### Màn hình 2: Customer Journey (Timeline)

**Route:** `/admin/ekyc/attempts/:identifierId`

**Header — khách đã mở TK thành công:**
```
Khách hàng: Nguyễn Văn A
CCCD: 038xxx | SĐT: 09xxx
Tổng lần thử: 3 (2 thất bại, 1 thành công)
Thời gian: 15/05/2025 → 18/05/2025 (3 ngày)
```

**Header — khách CHƯA mở TK (toàn bộ attempts đều fail, không có `e_kyc`):**
```
Khách hàng: NGUYEN VAN B  (từ OCR — chưa xác thực chính thức)
CCCD: 038yyy | SĐT: 09yyy
Tổng lần thử: 2 (2 thất bại, 0 thành công)
Thời gian: 30/06/2026 → 30/06/2026 (đang trong ngày)
⚠️ Chưa mở được tài khoản — dừng ở bước: Xác minh khuôn mặt trực tiếp
```

> Nếu `fullName` là `null` (OCR chưa từng đọc được tên — VD: fail ngay vì ảnh mờ trước khi VNPT xử lý), hiển thị `"Chưa xác định (chưa đọc được OCR)"` thay cho tên.

**Tài khoản đã mở (nếu có):**
```
✅ Tài khoản: 039C123456
[Xem chi tiết tài khoản →]
```

**Nếu chưa có tài khoản** — ẩn hoàn toàn block "Tài khoản đã mở", thay bằng banner cảnh báo màu vàng ngay dưới header (đã thể hiện ở ví dụ header phía trên: dòng "⚠️ Chưa mở được tài khoản..."). Không hiển thị section trống hoặc "N/A".

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
- `final_ekyc_id` null (chưa mở TK thành công): ẩn section "Tài khoản đã mở", hiển thị banner "⚠️ Chưa mở được tài khoản" thay thế — **không phải lỗi hay empty state, đây là trạng thái hợp lệ và phải xem được đầy đủ journey**
- `fullName` null (OCR chưa từng đọc được — fail trước cả bước OCR): hiển thị "Chưa xác định (chưa đọc được OCR)" thay tên, vẫn hiển thị đầy đủ `identifierId` + timeline
- Khách có nhiều lần thử fail liên tiếp, tất cả đều `NOT_SUBMITTED` (chưa từng chạm Lotte): Journey timeline vẫn hiển thị đầy đủ từng lần, không có item "thành công" nào — đây là dữ liệu hữu ích để Ops biết khách bị kẹt ở đâu

---

**Document Status:** 📋 Pending | For: FE Dev (ekyc-admin UI) | Next Steps: Chờ Sub-feature 1 BE endpoints live → implement 3 màn hình
