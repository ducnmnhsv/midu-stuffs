# Lead Brief — eKYC Biometric Log Storage

## Yêu cầu
Lưu **toàn bộ raw response sinh trắc học từ VNPT** vào DB khi mở tài khoản thành công qua eKYC.
- Mục đích: audit trail
- Admin page: hiển thị từng trường riêng lẻ (không chỉ JSON blob)
- Output: BE issue + Spec document

## Output cần tạo
1. **BE Issue** — cho developer implement
2. **Spec document** — mô tả DB schema + API + flow

## Context
- Service xử lý eKYC: `ekyc-admin` (Java JHipster)
- VNPT là third-party eKYC provider
- Biometric data từ VNPT response: liveness score, face match score, transaction ID, timestamps, raw JSON
- Admin cần view từng trường riêng

## Task cho Analyst
Tìm hiểu trong `ekyc-admin`:
1. Flow mở tài khoản eKYC — endpoint nào gọi VNPT, response trả về gì
2. VNPT biometric response structure (các field cụ thể)
3. DB schema hiện tại của ekyc-admin liên quan đến eKYC records
4. Admin page hiện tại hiển thị gì (nếu có)
5. Xác định điểm inject: chỗ nào trong code nên lưu log

## Task cho Creator
Dựa vào findings của analyst, tạo:
1. **Spec document**: DB schema mới/alter, flow, API admin endpoint
2. **BE Issue**: migration script, entity, repository, service logic, admin API

## Task cho Validator
- Kiểm tra convention TradeX (naming, response format)
- Lưu file cuối vào đúng folder
