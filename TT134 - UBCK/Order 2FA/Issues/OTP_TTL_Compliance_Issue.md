# [TT134-P0-01] OTP TTL Compliance — SMS / Thẻ ma trận / Soft OTP

**TT134 Reference:** Điều 8 — Xác thực bằng mã khóa dùng một lần (OTP)  
**Priority:** 🔴 P0  
**Deadline:** 14/08/2026  
**Sub-group:** Order 2FA  
**Phụ thuộc C06:** Không — xử lý nội bộ  

---

## 1. Bối cảnh

TT134 **Điều 8** quy định thời hạn hiệu lực (TTL) tối đa và các yêu cầu kỹ thuật cho từng loại OTP. Ngoài TTL, Điều 8.5 còn quy định chi tiết về enrollment, device binding, lockout cho Soft OTP (Smart OTP). Hiện tại, các cấu hình này trong hệ thống (Lotte Core + TradeX SMS + Smart OTP) **chưa được kiểm tra và confirm** theo đúng yêu cầu TT134.

---

## 2. Yêu cầu TT134 — Điều 8 (văn bản chính thức)

| Loại OTP | TTL theo Điều 8 TT134 | Ghi chú |
|---|---|---|
| SMS OTP | **5 phút** kể từ lúc hệ thống tạo | Kèm thông báo mục đích OTP |
| Voice OTP | **3 phút** kể từ lúc cuộc gọi thiết lập thành công | Kèm thông báo mục đích OTP |
| Email OTP | **5 phút** kể từ lúc hệ thống tạo | Kèm thông báo mục đích OTP |
| Thẻ ma trận (Matrix card) | Request window **2 phút** kể từ lúc hệ thống tạo yêu cầu · Card validity tối đa **1 năm** | Hết hạn card → yêu cầu đăng ký lại |
| Soft OTP / Smart OTP | Code hiệu lực tối đa **2 phút** kể từ lúc hệ thống tạo | ≠ TOTP 30s standard — TT134 quy định riêng |
| Token OTP | **2 phút** kể từ lúc hệ thống tạo | — |

> **Lưu ý quan trọng:** Soft OTP / Smart OTP TTL là **2 phút** theo TT134, KHÔNG phải 30s theo chuẩn TOTP thông thường. Cần xác nhận lại cách implement Smart OTP hiện tại có compliant không.

---

## 3. Scope of Work

### 3.1 BE Tasks — TTL Compliance

| # | Task | Service | Ưu tiên |
|---|---|---|---|
| BE-1 | Audit cấu hình SMS OTP TTL hiện tại → phải ≤ 5 phút (300s) (Điều 8.1b) | lotte-bridge / Lotte Core | Cao |
| BE-2 | Audit Thẻ ma trận TTL → request window phải ≤ 2 phút; card validity tối đa 1 năm (Điều 8.4) | Core Lotte | Cao |
| BE-3 | Audit Soft OTP / Smart OTP code TTL → phải expire sau ≤ 2 phút kể từ lúc tạo (Điều 8.5đ) | Smart OTP service | **🔴 Critical** |
| BE-4 | Xác nhận SMS/Voice/Email OTP response kèm thông tin mục đích (Điều 8.1a, 8.2a, 8.3a) | TradeX SMS service | Cao |
| BE-5 | Update SMS/Voice OTP template: kèm mục đích + "Mã OTP có hiệu lực trong X phút" | TradeX SMS service | Cao |
| BE-6 | Log TTL expiry events: otp_generated_at, otp_expires_at, otp_used_at | AAA / audit service | Trung bình |

### 3.2 BE Tasks — Soft OTP / Smart OTP Requirements (Điều 8.5)

| # | Task | Service | Ưu tiên |
|---|---|---|---|
| BE-7 | **[Điều 8.5b]** Khi Soft OTP dùng lần đầu hoặc trên thiết bị mới → bắt buộc verify SMS OTP + biometric trước khi activate | Smart OTP service | **🔴 Critical** |
| BE-8 | **[Điều 8.5c]** Activation code chỉ kích hoạt được trên 1 thiết bị duy nhất; activation code có TTL hiệu lực | Smart OTP service | Cao |
| BE-9 | **[Điều 8.5d]** Lockout sau ≤ 10 lần nhập sai liên tiếp → khóa **vĩnh viễn** cho đến khi KH yêu cầu mở khóa qua CSH. CSH phải verify danh tính KH trước khi mở. | Smart OTP service / CSH flow | **🔴 Critical** |

### 3.3 FE Tasks

| # | Task | Screen | Ưu tiên |
|---|---|---|---|
| FE-1 | Hiển thị countdown timer (5 phút) trên màn hình nhập SMS OTP | SMSOTPInput | Cao |
| FE-2 | Disable "Gửi lại" (resend) button trong thời gian TTL còn hiệu lực | SMSOTPInput | Cao |
| FE-3 | Hiển thị error message rõ ràng khi OTP hết hạn ("OTP đã hết hạn, vui lòng yêu cầu mã mới") | SMSOTPInput | Cao |
| FE-4 | Soft OTP screen: hiển thị countdown 2 phút (TTL theo TT134). Code tự refresh mỗi 30s theo chu kỳ TOTP, nhưng session challenge expire sau 2 phút | SmartOTPInput | Trung bình |
| FE-5 | **[Điều 8.5b]** Khi activate Soft OTP trên thiết bị mới → redirect to SMS OTP + biometric verification screen trước | SmartOTP EnrollScreen | **🔴 Critical** |
| FE-6 | **[Điều 8.5d]** Sau lockout Soft OTP → hiển thị màn hình "Smart OTP bị khóa. Vui lòng liên hệ bộ phận hỗ trợ để mở khóa." (không có countdown, không auto-unlock) | SmartOTPInput / LockScreen | Cao |

---

## 4. Acceptance Criteria

**TTL Compliance (Điều 8)**
```
AC-1: SMS OTP expire sau ≤ 5 phút (300s) kể từ lúc hệ thống tạo (Điều 8.1b)
AC-2: Voice OTP expire sau ≤ 3 phút kể từ lúc cuộc gọi thiết lập thành công (Điều 8.2b)
AC-3: Email OTP expire sau ≤ 5 phút kể từ lúc hệ thống tạo (Điều 8.3b)
AC-4: Thẻ ma trận — yêu cầu xác thực expire sau ≤ 2 phút (Điều 8.4b); card validity ≤ 1 năm (Điều 8.4a)
AC-5: Soft OTP / Smart OTP code expire sau ≤ 2 phút kể từ lúc tạo (Điều 8.5đ)
AC-6: SMS/Voice/Email OTP content có thông tin mục đích (VD: "Mã xác thực rút tiền: XXXXXX. Hiệu lực 5 phút.") (Điều 8.1a)
AC-7: Nhập OTP sau khi hết hạn → trả lỗi OTP_EXPIRED (HTTP 400)
AC-8: FE hiển thị countdown timer đồng bộ với TTL server-side
AC-9: Log đầy đủ: otp_generated_at, otp_expires_at, otp_used_at
```

**Soft OTP / Smart OTP Requirements (Điều 8.5)**
```
AC-10: [Điều 8.5b] Khi KH dùng Soft OTP lần đầu HOẶC trên thiết bị mới → hệ thống yêu cầu xác thực SMS OTP + biometric trước khi cho phép activate
AC-11: [Điều 8.5c] 1 activation code chỉ kích hoạt được trên đúng 1 thiết bị di động
AC-12: [Điều 8.5c] Activation code có TTL hiệu lực xác định (NHSV quy định, VD: 10 phút)
AC-13: [Điều 8.5d] Nhập sai liên tiếp quá N lần (N ≤ 10, NHSV quy định) → Soft OTP bị khóa vĩnh viễn
AC-14: [Điều 8.5d] Chỉ CSH mới có thể mở khóa Soft OTP, sau khi KH yêu cầu và CSH đã verify danh tính KH
AC-15: [Điều 8.5d] Không có cơ chế auto-unlock theo thời gian — phải qua CSH
```

---

## 5. API Impact

### Endpoint bị ảnh hưởng

| Method | Endpoint | Thay đổi |
|---|---|---|
| POST | `/api/v1/otp/send` | Thêm `expires_at` trong response |
| POST | `/api/v1/otp/verify` | Kiểm tra TTL, trả `OTP_EXPIRED` nếu quá hạn |
| POST | `/api/v1/smart-otp/verify` | Validate TOTP window, không allow drift > 1 |

### Response thay đổi — `/api/v1/otp/send`

```json
{
  "success": true,
  "data": {
    "otpType": "SMS",
    "expiresAt": "2026-08-01T10:05:00Z",
    "ttlSeconds": 300,
    "maskedPhone": "090****123",
    "purpose": "CASH_WITHDRAWAL"
  }
}
```
> `purpose` là required field theo Điều 8.1a — phải có trong response để FE hiển thị mục đích cho user.

### Error code mới

| Code | HTTP | Mô tả |
|---|---|---|
| `OTP_EXPIRED` | 400 | OTP đã hết hạn |
| `OTP_ALREADY_USED` | 400 | OTP đã được sử dụng (matrix card) |

---

## 6. SMS OTP Content Templates (Điều 8.1a)

Điều 8.1a yêu cầu nội dung SMS phải kèm **mục đích OTP** và **TTL**. Dưới đây là template chuẩn cho từng nghiệp vụ dùng SMS OTP.

### 6.1 Nghiệp vụ dùng SMS OTP

| Nghiệp vụ | `purpose` code | Template nội dung SMS |
|---|---|---|
| Đăng ký tài khoản | `ACCOUNT_REGISTRATION` | `[NHSV] Mã xác thực đăng ký tài khoản: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai.` |
| Đăng nhập lần đầu trên app | `FIRST_APP_LOGIN` | `[NHSV] Mã xác thực đăng nhập lần đầu: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai.` |
| Thay đổi người chăm sóc tài khoản | `ACCOUNT_MANAGER_CHANGE` | `[NHSV] Mã xác thực thay đổi người chăm sóc: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai.` |
| Đăng ký CTV NH Creator | `NH_CREATOR_REGISTRATION` | `[NHSV] Mã xác thực đăng ký CTV NH Creator: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai.` |

> **Lưu ý kỹ thuật:** Tiếng Việt dùng Unicode encoding → giới hạn 70 chars/SMS part. Các template trên sẽ gửi thành 2 SMS parts. Nếu cần tối ưu chi phí SMS, có thể rút ngắn bằng cách bỏ "Không chia sẻ mã này với ai" — nhưng phần `mục đích` và `hiệu lực X phút` là **bắt buộc** theo Điều 8.1a.

### 6.2 Các nghiệp vụ khác (tham chiếu)

Các nghiệp vụ đã có template trong hệ thống (cần audit để bổ sung mục đích + TTL nếu thiếu):

| Nghiệp vụ | `purpose` code | Template mẫu |
|---|---|---|
| Rút tiền | `CASH_WITHDRAWAL` | `[NHSV] Mã xác thực RÚT TIỀN: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai.` |
| Đặt lệnh | `ORDER_PLACEMENT` | `[NHSV] Mã xác thực ĐẶT LỆNH: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai.` |
| Kích hoạt Smart OTP | `SMART_OTP_ACTIVATION` | `[NHSV] Mã kích hoạt Smart OTP: {OTP}. Hiệu lực 5 phút. Không chia sẻ mã này với ai.` |

---

## 7. Testing Notes

**TTL:**
- SMS OTP: gửi, chờ > 300s → nhập → expect OTP_EXPIRED
- Thẻ ma trận: tạo request, chờ > 120s → nhập → expect OTP_EXPIRED
- Soft OTP: nhập code sau khi challenge session > 120s → expect OTP_EXPIRED
- Soft OTP TOTP: nhập code của window T-4 trở về trước → expect invalid (drift > 2 phút)

**Soft OTP Lockout (Điều 8.5d):**
- Nhập sai N lần liên tiếp (N = giá trị NHSV config) → Smart OTP locked
- Sau lock: nhập đúng code → vẫn bị reject (không auto-unlock)
- Mở khóa: chỉ CSH với verified KH identity
- Test: lock rồi thử tất cả valid codes → không được unlock tự động

**Soft OTP Activation (Điều 8.5b/c):**
- Activate Smart OTP trên thiết bị mới → phải qua SMS OTP + biometric
- Cùng activation code activate trên 2 thiết bị → lần 2 phải fail
- Activation code sau khi hết TTL → phải fail

**SMS Content:**
- Nội dung SMS có ghi mục đích (VD: "Mã xác thực RÚT TIỀN: XXXXXX. Hiệu lực 5 phút.")

---

## 7. Core Requirements — Web Trading Channel

> TT134 áp dụng cho **mọi kênh online**, bao gồm web trading của Core. Các yêu cầu dưới đây cần được raise chính thức với Core team để đảm bảo web trading tuân thủ song song với NHSV Pro.

### 7.1 Core Tasks

| # | Yêu cầu với Core | Điều TT134 | Ưu tiên |
|---|---|---|---|
| CORE-1 | Audit và confirm SMS OTP TTL trên web trading ≤ 5 phút (300s) kể từ lúc hệ thống tạo. Cấu hình phải do server enforce, không phải UI | Điều 8.1b | 🔴 Critical |
| CORE-2 | Audit Thẻ ma trận trên web trading: request window phải ≤ 2 phút; card validity ≤ 1 năm. Sau khi hết hạn card → yêu cầu KH đăng ký lại | Điều 8.4 | 🔴 Critical |
| CORE-3 | Update nội dung SMS OTP gửi qua kênh web phải kèm mục đích giao dịch và thời hạn hiệu lực — ví dụ: `"Mã xác thực ĐẶT LỆNH: XXXXXX. Hiệu lực 5 phút."` | Điều 8.1a | 🔴 Critical |
| CORE-4 | Khi OTP hết hạn → web trading phải trả lỗi rõ ràng và không xử lý giao dịch. Không được silent accept OTP quá hạn | Điều 8.1b | Cao |
| CORE-5 | Nếu Core có Soft OTP độc lập cho web: TTL phải ≤ 2 phút kể từ lúc server tạo (≠ TOTP 30s standard). Lockout sau ≤ 10 lần sai liên tiếp, chỉ mở khóa qua CSH | Điều 8.5đ, 8.5d | Cao |

### 7.2 Cách raise với Core

1. PM/BA gửi yêu cầu bằng văn bản (email/Jira) với tham chiếu cụ thể Điều 8 TT134
2. Core confirm cấu hình TTL hiện tại và kế hoạch update (nếu cần)
3. NHSV test trên môi trường web trading của Core trước deadline 14/08/2026

---

## 8. Dependencies

| Dependency | Owner | Status |
|---|---|---|
| Lotte Core — config SMS OTP TTL | IT / Lotte support | ❓ Cần confirm |
| Smart OTP service config | BE Lead | 🔄 Pending Smart OTP go-live |
| TradeX SMS template update | BE Lead | 📋 Chưa bắt đầu |

---

Document Status: 📋 Draft | For: BE / FE / QA | Next Steps: BE-1 audit TTL config ngay, FE-1/FE-2 sau khi confirm TTL value
