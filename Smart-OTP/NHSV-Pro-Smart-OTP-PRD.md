# PRD — Smart OTP cho NHSV Pro

**Loại tài liệu:** Product Requirements Document (PRD)  
**Sản phẩm:** NHSV Pro (React Native) + TradeX Backend + dịch vụ OTP + Push (FCM/APNs)  
**Trạng thái:** ⏳ Chờ approve — đã alignment BA/FE/BE/Security  
**Cập nhật:** 2026-04-06  
**Approved by:** _________________ | Date: __________  

**Ghi chú thuật ngữ:** Trong tài liệu này, **PRD** là bản yêu cầu sản phẩm (đôi khi gõ nhầm **PDR**).

---

## Mục lục

1. [Tóm tắt điều hành](#1-tóm-tắt-điều-hành)
2. [OTP và Smart OTP — giải thích đơn giản cho stakeholder](#2-otp-và-smart-otp--giải-thích-đơn-giản-cho-stakeholder)
3. [Tầm nhìn: NHSV Pro là app đăng ký thiết bị và cấp mã đa kênh](#3-tầm-nhìn-nhsv-pro-là-app-đăng-ký-thiết-bị-và-cấp-mã-đa-kênh)
4. [Bối cảnh và hiện trạng (As-Is)](#4-bối-cảnh-và-hiện-trạng-as-is)
5. [Mục tiêu và KPI](#5-mục-tiêu-và-kpi)
6. [Phạm vi](#6-phạm-vi)
7. [User stories](#7-user-stories)
8. [Yêu cầu chức năng](#8-yêu-cầu-chức-năng)
9. [Yêu cầu phi chức năng](#9-yêu-cầu-phi-chức-năng)
10. [Luồng nghiệp vụ (mô tả)](#10-luồng-nghiệp-vụ-mô-tả)
11. [Sequence diagrams](#11-sequence-diagrams)
12. [Phụ thuộc và stakeholder](#12-phụ-thuộc-và-stakeholder)
13. [Rủi ro và giảm thiểu](#13-rủi-ro-và-giảm-thiểu)
14. [Lộ trình giao](#14-lộ-trình-giao)
15. [Câu hỏi mở — Đã alignment](#15-câu-hỏi-mở--đã-alignment)

---

## 1. Tóm tắt điều hành

| Mục | Nội dung |
|-----|----------|
| **Hiện trạng** | Thay cho việc nhập OTP từ **thẻ cứng / token vật lý**, hệ thống hiện **gửi mã OTP tới điện thoại khách hàng chủ yếu qua push notification** (kèm nhánh **SMS** khi không có quyền thông báo hoặc user ép SMS — theo cấu hình app). Đây **tiện hơn thẻ cứng** (không mang thêm vật lý, không bị **kẹt khi mất thẻ**), nhưng **chưa phải Smart OTP** đủ chặt theo nghĩa sản phẩm bảo mật. |
| **Vấn đề bảo mật & thông tin** | **Push "thuần"** để chứa OTP dễ **kém bảo mật**: mã có thể **lộ trên màn hình khóa**, bị **đọc trộm** khi máy để bàn, **đồng bộ thông báo** sang thiết bị khác, hoặc thiết bị bị **chiếm quyền điều khiển** — khó kiểm soát như **trong app (két có khóa)**. **SMS** vẫn mang rủi ro **SIM swap** và kênh ngoài app. Mục tiêu là **siết chặt**: đăng ký thiết bị rõ ràng, ưu tiên **xem mã trong app**, giới hạn nội dung push, bind giao dịch, và **dự phòng có kiểm soát**. |
| **Hướng đi** | Triển khai **Smart OTP**: **không quay lại** phụ thuộc thẻ cứng cho đại đa số user; **nâng cấp** từ "OTP trong push lỏng" lên **thiết bị đã đăng ký + nhận mã trong app / push an toàn**; **SMS** (và các kênh khác) **chỉ là lớp dự phòng** khi quy định hoặc khi Smart không khả dụng. |
| **Tầm nhìn** | Trong tương lai, **NHSV Pro** là **app trung tâm** để khách hàng **đăng ký thiết bị nhận Smart OTP** và **lấy mã OTP** để **xác thực thao tác trên các kênh giao dịch khác** (web, tổng đài, kênh khác trong hệ sinh thái) — không giới hạn chỉ trong app. |
| **Kết quả kỳ vọng** | **An toàn hơn push/SMS hiện tại**, **tiện hơn thẻ cứng** (không mất là "đứng" giao dịch vì mất vật lý), **một nơi quản lý thiết bị tin cậy**, và **giám sát / audit** theo kênh và phiên giao dịch. |

---

## 2. OTP và Smart OTP — giải thích đơn giản cho stakeholder

### 2.1 Một câu cho mỗi loại

| | Một câu dễ nhớ |
|---|----------------|
| **OTP qua SMS** | "Mã được **nhắn tin** về **số điện thoại**; ai kiểm soát được SIM/tin nhắn có thể thấy mã." |
| **OTP qua push (hiện tại, dạng phổ biến)** | "Mã được **đẩy thông báo** lên điện thoại — **không cần thẻ cứng**, nhưng dễ **lộ trên khóa màn hình** hoặc khi máy không thuộc quyền kiểm soát chặt." |
| **Smart OTP (mục tiêu)** | "Mã gắn **thiết bị / app đã đăng ký**; ưu tiên **xem trong app**, push **hạn chế lộ mã**; có **dự phòng** (SMS…) có kiểm soát." |

### 2.2 So sánh ngắn (cho slide họp)

| Tiêu chí | Thẻ cứng / OTP token vật lý | OTP qua **push** (hiện trạng gần đây) | OTP **SMS** | Smart OTP (mục tiêu) |
|----------|-----------------------------|--------------------------------------|-------------|----------------------|
| **Cách nhận mã** | Đọc trên **thiết bị riêng** | **Thông báo** trên điện thoại | **Tin nhắn** | **Trong app** (ưu tiên) + push an toàn / dự phòng |
| **Tiện lợi** | Phải **mang thẻ**; **mất thẻ** thì **bất tiễn / khó giao dịch** | Không mang thêm vật lý | Phụ thuộc sóng nhà mạng | Cân bằng: **không thẻ cứng**, vẫn **an toàn hơn push lỏng** |
| **Bảo mật điển hình** | Tách khỏi điện thoại (nhưng **mất = rủi ro** nếu kẻ khác lấy được) | **Dễ yếu** nếu mã **full text trên push**, lock screen, đồng bộ notif | **SIM swap**, xem SMS ngoài app | **Siết** hiển thị, bind thiết bị, bind giao dịch, audit |
| **Gắn với** | Vật lý + thường bind tài khoản tại quầy | Thiết bị nhận push (chưa chặt nếu chưa đăng ký đủ) | **Số điện thoại** | **Tài khoản + thiết bị đã onboarding** |

---

## 5. Mục tiêu và KPI

### 5.1 Mục tiêu sản phẩm

1. **Nâng mức bảo mật** so với **OTP trên push (và SMS)** hiện tại — **không quay lại** phụ thuộc **thẻ cứng** cho đại đa số user.  
2. Người dùng **bật và hiểu** Smart OTP; NHSV Pro là **cổng đăng ký thiết bị** rõ ràng.  
3. **Chuẩn bị** luồng **OTP hiển thị trên Pro, dùng trên kênh khác** (theo lộ trình).  
4. **Luôn có** đường thoát **SMS** khi quy định hoặc khi Smart không khả dụng.

### 5.2 KPI đề xuất

| KPI | Mô tả |
|-----|--------|
| Smart OTP adoption | % user hoàn tất đăng ký thiết bị |
| OTP channel mix | % phiên Smart vs push "legacy" vs SMS |
| Sự cố bảo mật / gian lận liên quan OTP | Theo quý (nội bộ) |
| Time to complete | Từ yêu cầu OTP đến verify thành công |
| Ticket CS | "Không nhận OTP" — theo kênh |

**Done MVP (giai đoạn 1):** onboarding + Smart mặc định khi đủ điều kiện + fallback SMS + telemetry.  

---

## 6. Phạm vi

### 6.1 In scope — Giai đoạn 1 (MVP trong app)

**Scope MVP:** Đăng nhập + Đặt lệnh

- Màn hướng dẫn + trạng thái Smart OTP (Bật / Chưa bật / Cần làm mới).  
- Đăng ký thiết bị (`device/register` theo contract BE).  
- Xin OTP qua OTP service; push / in-app; verify theo luồng hiện có.  
- Fallback SMS (`notifyMobileOtpNhsv`) — **user chủ động chọn**, không tự động.  
- Rate limit riêng: Smart OTP vs SMS.  
- Copy nhấn mạnh **lợi ích bảo mật** (dễ hiểu, không kỹ thuật hóa).

### 6.2 Out of scope (backlog)

- Loại bỏ hoàn toàn SMS nếu luật / đối tác chưa cho phép.  
- Thay thế toàn bộ phương thức xác thực khác (sinh trắc, v.v.) — ngoài phạm vi PRD OTP.

---

## 8. Yêu cầu chức năng

| ID | Yêu cầu | Ghi chú |
|----|---------|---------|
| FR-1 | Onboarding Smart OTP | Giải thích **bảo mật + tiện ích** → notification → `device/register` |
| FR-2 | Trạng thái Smart OTP | Đã bật / Chưa bật / Cần đăng ký lại |
| FR-3 | Ưu tiên kênh | Smart khi khả dụng; SMS fallback có kiểm soát |
| FR-4 | Tùy chọn SMS | Trong giới hạn rate limit; có cảnh báo rủi ro kênh SMS |
| FR-5 | Push / in-app | Nội dung an toàn (NFR) |
| FR-6 | Verify | Theo từng luồng nghiệp vụ |
| FR-7 | Lỗi | Map mã lỗi + hướng dẫn (thử lại / SMS / kiểm tra Pro) |
| FR-8 | Đổi thiết bị | Reset + SMS verification — Vào Cài đặt > Bật lại Smart OTP > Xác minh qua SMS |

---

## 9. Yêu cầu phi chức năng

- **Bảo mật thông tin:**
  - Push notification **không chứa mã OTP** — chỉ hiện "Có mã xác thực mới, mở app để xem"
  - Mã OTP ưu tiên **hiển thị trong app** (sau khi unlock)
  - Giảm phơi mã qua **SMS** — cảnh báo rủi ro kênh SMS cho user
  - Rate limit riêng: Smart OTP vs SMS (không chung)
- **Tuân thủ:** Giữ SMS khi quy định bắt buộc (lần đầu đăng nhập app).
- **Hiệu năng:** Timeout, retry có backoff; push không đến sau 10s → user có thể chọn SMS.
- **Quan sát:** Metric theo **kênh phát hành** (Smart/SMS) và **loại giao dịch**.
- **Sẵn sàng:** Fallback khi OTP service lỗi.

---

## 15. Câu hỏi mở — Đã alignment

| # | Câu hỏi | Quyết định | Ghi chú |
|---|---------|------------|---------|
| 1 | Số thiết bị Smart OTP tối đa mỗi user? | **1 thiết bị** | Security best practice; TCBS/SSI model |
| 2 | Verify chỗ nào? | **BE xử lý** | App gọi API nghiệp vụ, BE verify qua OTP service |
| 3 | Push có full mã hay chỉ "mở app"? | **Không chứa mã** | Push chỉ thông báo, mã xem trong app |
| 4 | Kênh/nghiệp vụ nào bắt buộc SMS? | **Lần đầu đăng nhập app** | Xác minh chủ tài khoản |
| 5 | Rate limit Smart vs SMS? | **Riêng** | Smart OTP: 5 lần/ngày; SMS: 5 lần/ngày (đề xuất) |
| 6 | Đổi thiết bị xử lý thế nào? | **Reset + SMS verification** | Vào Cài đặt > Bật lại Smart OTP > Xác minh qua SMS |
| 7 | Fallback khi push không đến? | **User chủ động** | Hiện nút "Gửi qua SMS" - không tự động |
| 8 | Scope MVP - những luồng nào? | **Đăng nhập + Đặt lệnh** | Ưu tiên theo business |

---

## Phụ lục: Tham chiếu mã nguồn (NHSV Pro RN)

| Nội dung | Đường dẫn (trong repo nhsv-mts-rn) |
|----------|-------------------------------------|
| API list OTP | `src/config/api.ts` (`sendOTP`, `sendNewOTP`, `registerOTPDevice`, `getOTP`) |
| Chọn nhánh gửi OTP | `src/reduxs/sagas/SendOTP/SendOTP.ts` |

---

**Document status:** ✅ Chờ approve — đã alignment BA/FE/BE/Security  
**For:** PM, BA, FE, BE, Security  
**Next steps:** Sign-off → API contract → Wireframe → Implementation
