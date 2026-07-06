---
name: feedback-fe-issue-format
description: "Chuẩn viết FE issue cho NHSV Pro — góc nhìn PO/yêu cầu, không chi tiết implementation code"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 9afc6ec7-3813-450d-a41d-8800170eea10
---

Khi viết hoặc rewrite FE issue (bất kỳ feature nào, không chỉ Derivatives), viết theo góc nhìn **PO/yêu cầu** — không đi sâu vào chi tiết code implementation. Phần "làm thế nào" (Redux/state management, tên file, component structure, navigation params...) để dev FE tự quyết.

**Why:** Midu là PO, không phải dev — issue cần mô tả đúng "cái gì cần làm và tại sao", dev tự lo phần "làm thế nào". Xem ví dụ rewrite tại `New feature in NHSV Pro/Event_Calendar/Issues/FE_Event_Calendar.md`.

**How to apply:** Áp dụng cho mọi file issue FE trong `Derivatives/Planning documentation/{Category}/Issues/` và `New feature in NHSV Pro/{FeatureArea}/Issues/`. Liên quan [[feedback_api_spec_format]] (áp dụng cho API Spec — spec thì giữ chi tiết kỹ thuật, còn issue thì bỏ chi tiết code) và [[feedback_doc_only]].

## Cấu trúc

1. **Bối cảnh** — hiện trạng ngắn gọn (không liệt kê file path, không mô tả code hiện tại chi tiết), lý do cần làm issue này.
2. **User Story** — As a / I want to / So that.
3. **Yêu cầu chức năng** — chia theo nhóm màn hình/luồng, mô tả **hành vi và kết quả mong đợi**, không mô tả cách code. Được phép nêu:
   - Màn nào gọi API nào (endpoint cụ thể) — vì đây là hợp đồng dữ liệu, không phải cách implement.
   - Field nào trong response dùng để hiển thị/quyết định gì (ví dụ: `isToday=true` → hiện badge; field `null` → ẩn dòng tương ứng).
   - Business rule (validation, trạng thái loading/empty/error, cách nhóm/sắp xếp dữ liệu).
   Không được nêu: tên Redux slice/saga, file path trong FE repo, tên component, cấu trúc params navigation, "Mobile team tự quyết approach nào đó".
4. **Acceptance Criteria** — checkbox, kiểm chứng được bằng mắt/test case, không phải task kỹ thuật.
5. **Cần confirm thêm** — các điểm chưa chốt cần hỏi PM/BE trước khi dev bắt đầu.
6. Footer: `Document Status: 📋/✅/🔄 | For: [audience] | Next Steps: [action]`

## Điều không được làm
- KHÔNG liệt kê bảng "File | Trạng thái hiện tại" với đường dẫn code cụ thể trong phần Bối cảnh.
- KHÔNG có mục "Task N — Redux/API integration", "Task N — Cập nhật ScreenParamList" kiểu chia theo layer kỹ thuật.
- KHÔNG paste nguyên khối JSON request/response đầy đủ kiểu spec kỹ thuật (nếu cần field, gọi tên field inline trong câu mô tả yêu cầu, không tách thành code block riêng section "API Response Types").
- KHÔNG hướng dẫn cách lưu state (Redux vs local state), cách tránh double-fetch, hay chi tiết kiến trúc FE.
