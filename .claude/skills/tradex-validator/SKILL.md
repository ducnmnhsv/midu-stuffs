---
name: tradex-validator
description: "Kiểm tra convention TradeX — validate naming, response format, cấu trúc spec/doc sau khi tạo. Dùng khi: 'validate', 'check convention', 'review spec', 'spec có đúng không', 'kiểm tra convention'. Được tradex-orchestrator gọi tự động ở Phase 3."
---

# tradex-validator Skill

Mọi kiểm tra đều theo checklist. Ghi rõ PASS/FAIL/WARN cho từng mục.

## Checklist kiểm tra

### API Spec
- [ ] URL: đúng pattern `/api/v1/{resource}` (Derivatives: `/api/v1/derivatives/{resource}`)
- [ ] DTO Request: `{Resource}{Action}Request`
- [ ] DTO Response: `{Resource}{Action}Response`
- [ ] Order API (lotte-integrated): response có `{ "message": "...", "orderNumber": "..." }`
- [ ] Order API (tradex-native): mutation → `{ "id": number }`, query → `{ "totalCount", "orders" }`
- [ ] Mã lỗi: lotte-integrated → `{OP}_{CODE}`, tradex-native → `SCREAMING_SNAKE_CASE`
- [ ] TradeX-native errors: dùng `messageParams`, KHÔNG có field `message`
- [ ] Có section "Trường tự động điền"
- [ ] Có document footer (`**Document Status:** | **For:** | **Next Steps:**`)

### Tài liệu Planning/
- [ ] Không có code block (` ``` `)
- [ ] Không có class/method signature
- [ ] Dữ liệu flow dùng diagram text
- [ ] Có document footer

### Tài liệu Issues/
- [ ] Có section Executive Summary (không có code)
- [ ] Có section Technical Details
- [ ] Có document footer

### FE Issue
- [ ] Tiêu đề có `[FE]`
- [ ] Có đường dẫn `src/...` trong Affected Screens/Components
- [ ] Có document footer

## Phạm vi tự sửa

**Tự sửa được:**
- Thiếu document footer → thêm vào
- Lỗi DTO naming rõ ràng (ví dụ: `OrderRequest` → `DerivativeOrderPlaceRequest`)
- Code block đơn giản trong Planning/ → chuyển sang text

**Chỉ báo cáo (không tự sửa):**
- Thay đổi cấu trúc response (cần phán đoán nghiệp vụ)
- Tên DTO mơ hồ (resource name không rõ)
- Tạo thư mục category mới

## Định dạng báo cáo

Lưu vào `_workspace/03_validator_report.md`:

```markdown
# Validator Report

**Kết quả:** PASS | PASS_WITH_WARNINGS | FAIL

## Chi tiết kiểm tra
- [PASS] URL naming: /api/v1/derivatives/orders ✅
- [FAIL] DTO naming: OrderRequest → DerivativeOrderPlaceRequest
- [WARN] Thiếu Language mapping table (cần cho Lotte-integrated)

## Đã sửa trong draft
- Đã sửa DTO naming
- Đã thêm footer

## Vấn đề còn lại (Cần user xác nhận)
- Response format chưa rõ — lotte-integrated hay tradex-native?

## File đã lưu
{đường dẫn thực tế}
```

Sau khi viết report: lưu tài liệu cuối cùng vào Target Path từ `_workspace/02_creator_meta.md`.
