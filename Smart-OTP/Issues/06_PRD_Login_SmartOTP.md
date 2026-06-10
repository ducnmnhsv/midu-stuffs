# PRD — Smart OTP Login Integration (Phase 2)

## 📋 Executive Summary (PM READS THIS)

### Problem Statement

Hiện tại, NHSV Pro app luôn dùng SMS/Push OTP để xác thực đăng nhập. Smart OTP đã được triển khai cho WTS/HTS (Phase 1), nhưng login app bản thân vẫn dùng kênh cũ — tức là user đã kích hoạt Smart OTP trên app vẫn phải chờ SMS khi login. Điều này tạo ra trải nghiệm không nhất quán và bỏ lỡ giá trị bảo mật của Smart OTP.

### Current vs Target

| | Hiện tại | Sau triển khai |
|---|---|---|
| User đã có Smart OTP | Vẫn phải nhận SMS/Push OTP khi login | Tự gen mã trong app, không cần chờ SMS |
| User chưa có Smart OTP | Login bình thường | Login bình thường + gợi ý kích hoạt Smart OTP |
| User cài lại app | Không có flow xử lý riêng | Được hướng dẫn kích hoạt lại ngay trong luồng login |

### Solution Approach

Sau khi user nhập mật khẩu (Step 1), backend trả về `otpType` cho FE biết nên hiển thị loại OTP nào. FE phân nhánh UI:
- **Smart OTP**: user tự gen mã 6 chữ số từ app (không gửi SMS)
- **SMS OTP**: flow hiện tại, không thay đổi gì
- **Cài lại app**: màn hình đặc biệt, hướng dẫn kích hoạt lại Smart OTP trong luồng login

Sau login, user chưa kích hoạt Smart OTP sẽ thấy banner gợi ý (Phase 1 soft gate). Trong tương lai có thể nâng lên hard gate bắt buộc kích hoạt (Phase 2).

### Timeline

| Milestone | Nội dung |
|---|---|
| Phase 1 | BE + FE implement 3 cases + soft gate banner |
| Phase 2 | Bật hard gate (chặn app đến khi kích hoạt Smart OTP) — timeline confirm với PM |

### Success Criteria

- User đã kích hoạt Smart OTP login được bằng mã 6 chữ số (không cần SMS)
- User chưa kích hoạt thấy gợi ý sau login
- User cài lại app được hướng dẫn kích hoạt lại, không bị kẹt
- App cũ (không truyền `otpType`) vẫn login bình thường

---

## 🔍 Technical Background (PM CAN SKIP)

### Context

Smart OTP Phase 1 đã hoàn thành: app NHSV Pro là nơi kích hoạt và sinh mã Smart OTP dùng cho WTS/HTS. Phase này (Phase 2) mở rộng: dùng Smart OTP để xác thực chính luồng login app.

### Login Flow Hiện Tại (Baseline)

```
POST /rest/api/v1/login  → temp accessToken
POST /rest/api/v1/notifyMobileOtpNhsv  → gửi SMS OTP
POST /rest/api/v1/login/sec/verifyOTP  → final token
```

### Thay Đổi Tối Thiểu

Không tạo endpoint mới. Chỉ:
1. Thêm `sotpStatus` + `otpType` vào login Step 1 response
2. Thêm optional `otpType` vào verifyOTP request
3. BE route Smart OTP sang dịch vụ verify đúng

### 3 Cases

| Case | Điều kiện | UX |
|---|---|---|
| **A** | `sotpStatus=Y` + local key khớp | Smart OTP UI, user tự gen mã |
| **B** | `sotpStatus=N` | SMS OTP UI hiện tại, không đổi gì |
| **C** | `sotpStatus=Y` + không có local key | Màn hình đặc biệt: kích hoạt lại hoặc dùng SMS |

### Quyết định Đã Alignment

| Quyết định | Kết quả |
|---|---|
| Có tạo `grant_type` mới không? | Không — dùng `password_otp` hiện tại |
| Fallback SMS khi mất thiết bị | Nút "Đăng nhập bằng SMS OTP" nổi trên màn hình Case C |
| Post-login với user chưa kích hoạt | Phase 1: banner dismiss được; Phase 2: hard gate |
| User cài lại app | Primary: kích hoạt lại trong flow; Secondary: SMS fallback + force re-activate |

---

## 📝 Detailed Requirements (PM CAN SKIP)

### Functional Requirements

| ID | Yêu cầu |
|---|---|
| FR-1 | Step 1 login response phải trả `sotpStatus` + `otpType` |
| FR-2 | FE không gọi `notifyMobileOtpNhsv` khi `otpType=SMART_OTP` |
| FR-3 | FE check local `sotpKey` để phân biệt Case A vs Case C |
| FR-4 | Case A: hiển thị input 6 chữ số + link "Không lấy được mã?" |
| FR-5 | Case B: giữ nguyên UI + flow SMS OTP hiện tại |
| FR-6 | Case C: màn hình đặc biệt với PRIMARY kích hoạt lại + SECONDARY SMS fallback |
| FR-7 | Case C PRIMARY: dùng `/otp/send` (txType=SMART_OTP) → `/otp/verify` → `/smartOtp/register` → lưu local key |
| FR-8 | Case C SECONDARY: gửi SMS, login xong bắt buộc redirect sang kích hoạt lại |
| FR-9 | verifyOTP truyền `otpType="SMART_OTP"` cho Case A; không truyền cho Case B (backward compat) |
| FR-10 | Phase 1 post-login: banner gợi ý kích hoạt Smart OTP cho Case B |

### Non-Functional Requirements

- Backward compatible: app cũ không truyền `otpType` vẫn login bình thường
- Không tăng latency Step 1 login (chỉ thêm 2 fields vào response)
- Secure storage: local `sotpKey` phải lưu trong Keychain/Keystore

### Endpoint Inventory (Không tạo mới)

| Endpoint | Thay đổi |
|---|---|
| `POST /rest/api/v1/login` | Thêm `sotpStatus` + `otpType` vào response |
| `POST /rest/api/v1/login/sec/verifyOTP` | Thêm optional `otpType` vào request |
| `POST /rest/api/v1/notifyMobileOtpNhsv` | Không đổi |
| `POST /rest/api/v1/otp/send` | Dùng lại với `txType: "SMART_OTP"` |
| `POST /rest/api/v1/otp/verify` | Không đổi |
| `POST /rest/api/v1/smartOtp/register` | Không đổi |

### Open Questions

| # | Câu hỏi | Owner |
|---|---|---|
| OQ-1 | Temp accessToken từ login Step 1 có đủ permission gọi `/otp/send` và `/smartOtp/register`? | BE Lead |
| OQ-2 | Enum chính xác của `sotp_stat` từ Lotte (`Y`/`N` hay value khác)? | BE Lead |
| OQ-3 | Timeline Phase 2 hard gate? | PM |
| OQ-4 | Rate limit cho Smart OTP verify trong `verifyOTP` (sai bao nhiêu lần lock)? | BE Lead |

---

**Document Status:** Ready for sign-off
**For:** PM, BA, BE Lead, FE Lead, QA
**Next Steps:** Confirm OQ-1 + OQ-2 với BE → sign-off → assign task BE + FE → dev sprint
