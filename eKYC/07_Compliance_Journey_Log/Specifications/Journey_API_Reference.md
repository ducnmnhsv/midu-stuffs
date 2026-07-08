# Journey API Reference — Compliance Journey Log

**Document Type:** API Reference (danh sách API cần log)
**Category:** Compliance Journey Log (PRD_eKYC_v2.md mục 4.5)
**Service ghi log:** `ekyc-admin` (interceptor/aspect phía Backend, không cần App gọi thêm endpoint nào)
**Nguồn xác minh:** Đọc trực tiếp source code app `nhsv-mts-rn` (read-only reference repo) — không suy đoán từ tài liệu cũ
**Date:** 2026-07-08 | **Version:** 1.0

---

## 0. Vì sao có tài liệu này

Bản PRD gốc (trước khi viết lại theo hướng prose-only ngày 2026-07-07) có mục 4.10 liệt kê "11 API cần log", nhưng mục đó bị xóa khi PRD được viết lại để dễ đọc hơn cho người không chuyên kỹ thuật — nội dung kỹ thuật đáng lẽ phải chuyển sang tài liệu riêng nhưng bị bỏ sót cho phần Journey Log. `README.md` vẫn tham chiếu "mục 4.10" dù mục đó không còn tồn tại.

Tài liệu này thay thế mục 4.10 cũ — **và đã được đối chiếu lại với code thực tế của app** (không chỉ copy nguyên danh sách cũ), vì danh sách cũ có 1 endpoint không tồn tại trong code và thiếu 1 endpoint quan trọng.

---

## 1. Danh sách API cần log (đã xác minh qua source code app)

> Đường dẫn dưới đây theo convention TradeX API chuẩn (`/api/v1/...`, xem `Knowledge/TradeX/API Standards/tradex-api-conventions.md`). App cấu hình base URL có thêm tiền tố `/rest` ở tầng gateway/CDN (`https://nhsvpro.nhsv.vn/rest/api/v1/...`) — tiền tố này không phải một phần path logic của TradeX API, chỉ là routing prefix, không cần lưu trong `journey_step`/`endpoint`.

| # | `journey_step` (đề xuất) | Method | Endpoint | Mục đích | Evidence (file:line trong `nhsv-mts-rn`) |
|---|---|---|---|---|---|
| 1 | `EKYC_CREATE` | POST | `/api/v1/lotte/ekycs/create` | Khởi tạo hành trình — tạo `eKycId` sau khi khách nhập SĐT + email | `src/reduxs/sagas/EKYC/ConfirmPersonalInformationScreen.ts:48` |
| 2 | `EKYC_SEND_OTP` | POST | `/api/v1/ekyc-admin/sendOtp` | Gửi mã OTP | `src/reduxs/sagas/EKYC/SendOTP.ts:14` |
| 3 | `EKYC_VERIFY_OTP` | POST | `/api/v1/ekyc-admin/verifyOtp` | Xác thực mã OTP | `src/reduxs/sagas/EKYC/VerifyOTP.ts:28` |
| 4 | `EKYC_CHECK_NATIONAL_ID` | POST | `/api/v1/equity/account/checkNationalId` | Kiểm tra CCCD đã có TK NHSV chưa — chạy ngay sau khi VNPT SDK OCR xong, trước khi upload ảnh | `src/reduxs/sagas/EKYC/EKYCScanIdDone.ts:449` |
| 5 | `EKYC_IMAGE_STORAGE_URL` **(mới — chưa từng được log trước đây)** | GET | `/api/v1/aws` | Lấy URL để upload ảnh CCCD lên storage (`action=upload`), rồi lấy URL công khai để gửi kèm hồ sơ (`action=download`) | `src/reduxs/sagas/EKYC/EKYCScanIdDone.ts:572` (upload), `:589` (download) |
| 6 | `EKYC_QUERY_BANK_LIST` | GET | `/api/v1/ekycs/banks` | Danh sách ngân hàng nhận tiền | `src/reduxs/sagas/EKYC/QueryBankList.ts:27` |
| 7 | `EKYC_QUERY_BANK_BRANCH` | GET | `/api/v1/ekycs/banks/{bankCode}/branches` | Chi nhánh của ngân hàng khách đã chọn | `src/reduxs/sagas/EKYC/SetEKYCBank.ts:11` |
| 8 | `EKYC_QUERY_NHSV_BRANCH` | GET | `/api/v1/ekycs/branch` | Danh sách chi nhánh NHSV | `src/reduxs/sagas/EKYC/QueryNHSVBranch.ts:16` |
| 9 | `EKYC_VALIDATE_PARTNER` | GET | `/api/v1/ekycs/partner` | Validate mã CTV/nhân viên giới thiệu (referral) **và** validate nhân viên chăm sóc (staff-care) — 2 lần gọi khác tham số trong cùng 1 hành trình | `src/reduxs/sagas/EKYC/ValidatePresenterId.ts:43` (referral), `ValidateStaffCareId.ts:32,40,53` (staff-care) |
| 10 | `EKYC_SUBMIT` | POST | `/api/v1/lotte/ekycs` | Submit toàn bộ hồ sơ sang Lotte — **mốc chốt "thành công"** của Journey Log (xem PRD mục 4.5) | `src/reduxs/sagas/EKYC/OnPressNextInConfirmPolicyScreen.ts:183` |
| 11 | `EKYC_QUERY_ECONTRACT_URL` | GET | `/api/v1/equity/account/contracts` | Lấy webview URL FPT eContract để khách ký hợp đồng điện tử | `src/reduxs/sagas/EKYC/QueryEContractWebviewUrl.ts:14` |

### 1.1 Đối chiếu với danh sách cũ (11 API, trước 2026-07-08)

| Thay đổi | Chi tiết |
|---|---|
| ❌ **Gỡ bỏ** | `GET /api/v1/ekycs/account/exist` — tìm kiếm toàn bộ codebase app, không có endpoint này. Khả năng cao việc kiểm tra "đã có tài khoản chưa" hiện được xử lý phía server bên trong response lỗi của bước 1 (`EKYC_CREATE`) — `ConfirmPersonalInformationScreen.ts:64-73` có xử lý riêng cho lỗi "đã có tài khoản liên kết SĐT này". Cần BE Lead xác nhận trước khi loại hẳn khỏi thiết kế. |
| ✅ **Bổ sung** | `GET /api/v1/aws` (bước 5) — endpoint lấy URL upload/download ảnh CCCD lên storage. Đây là bước **bắt buộc** trong hành trình (không có bước này thì ảnh CCCD không đến được backend), nhưng danh sách cũ bỏ sót hoàn toàn. |
| 🔧 **Sửa path param** | Bước 7: path param thực tế là `{bankCode}`, không phải `{id}` như danh sách cũ ghi. |
| Tổng số bước | Vẫn là 11 (trùng hợp) — nhưng thành phần khác: -1 (account/exist) +1 (aws) so với danh sách cũ. |

---

## 2. Lưu ý khi implement — endpoint dùng chung ngoài luồng eKYC

Một số endpoint trong bảng trên **không phải riêng cho eKYC** — cùng 1 endpoint được tái sử dụng ở tính năng khác của app. Khi log, cần phân biệt bằng **context màn hình gọi** (hoặc thêm 1 field `journeyContext`), tránh lẫn dữ liệu:

| Endpoint | Dùng lại ở tính năng khác | File |
|---|---|---|
| `GET /api/v1/ekycs/partner` | **Đổi tư vấn viên** (`ChangeConsultantScreen`, `RegisterConsultantScreen`) — cho tài khoản đã mở, không phải trong hành trình mở TK mới | `src/reduxs/sagas/AccountConsultant/GetEkycPartner.ts:22` |
| `GET /api/v1/ekycs/banks/{bankCode}/branches` | **Beneficial Bank** — thêm tài khoản ngân hàng rút tiền cho account đã tồn tại | `src/reduxs/sagas/BeneficialBank/getBranchByBankCode.ts:9` |
| `GET /api/v1/aws` | Endpoint upload/download file dùng chung toàn app (không chỉ eKYC) — cần filter theo `serviceName: 'ekyc'` trong request param khi log, không log toàn bộ traffic `/aws` | `EKYCScanIdDone.ts:572,589` (param `serviceName: 'ekyc'`) |

**Không có trong app (đã xác nhận không tồn tại, không cần scope cho log):**
- Không có endpoint riêng cho "Gửi lại OTP" — app tái dùng `EKYC_SEND_OTP` (`sendOtp`) cho cả gửi lần đầu và gửi lại (`EKYCOTPResendCountDown/index.tsx:47`). Log 2 trường hợp này chung 1 `journey_step`, phân biệt qua số lần gọi liên tiếp nếu cần.
- Không có API riêng cho tra cứu tỉnh/thành/quận/huyện — địa chỉ lấy trực tiếp từ kết quả OCR CCCD (free-text), không qua API dropdown.
- Không có API kiểm tra trùng SĐT/email phía server ở bước nhập liệu — chỉ validate regex phía client.
- Không có API "xác nhận ký hợp đồng" riêng — việc ký diễn ra ngay trong webview FPT; app chỉ poll lại `EKYC_QUERY_ECONTRACT_URL` để lấy trạng thái mới nhất, không có call riêng để confirm.

---

## 3. Field cần ghi cho mỗi dòng log (giữ nguyên từ thiết kế PRD cũ)

Mỗi lần gọi 1 trong 11 API trên → 1 dòng vào `ekyc_journey_log`:

| Field | Nguồn |
|---|---|
| `eKycId` | Khóa liên kết chính — có từ bước 1 (`EKYC_CREATE`) trở đi |
| `phoneNo` | Khóa phụ cho các bước trước khi có `eKycId` (bước 1) |
| `identifierId` | Số CCCD — có từ bước 4 (`EKYC_CHECK_NATIONAL_ID`) trở đi |
| `journey_step` | Enum theo cột 2 của bảng Section 1 |
| `endpoint` + `http_method` | Theo bảng Section 1 |
| `http_status` | Response thực tế |
| `response_summary` | Tóm tắt kết quả (không lưu full response nhạy cảm) |
| `created_at` | Timestamp ghi log (real-time, không buffer) |

Chi tiết storage strategy (thành công giữ vĩnh viễn / fail-abandon tự xóa, ngưỡng timeout, bảng `ekyc_journey_log` riêng biệt với `ekyc_attempt_log`): xem `Planning/PRD_eKYC_v2.md` mục 4.5 và `eKYC-summary.html` Section "Compliance Journey Log".

---

## 4. Việc cần chốt trước khi implement

- [ ] BE Lead xác nhận: bỏ hẳn `EKYC_ACCOUNT_EXIST` khỏi thiết kế, hay cần thêm 1 bước log riêng cho response lỗi "đã có tài khoản" trong `EKYC_CREATE`?
- [ ] Bước 5 (`EKYC_IMAGE_STORAGE_URL`) gọi 4 lần/lượt scan (upload + download × mặt trước + mặt sau) — log cả 4 lần hay gộp thành 1 dòng "đã upload ảnh CCCD"? (khuyến nghị: gộp, vì 4 dòng riêng lẻ không thêm giá trị audit)
- [ ] Bước 9 (`EKYC_VALIDATE_PARTNER`) — cần phân biệt 2 lần gọi (referral vs. staff-care) bằng field nào? Đề xuất thêm `journeyContext: "referral" | "staffCare"` cùng `journey_step`.
- [ ] Bước 11 (`EKYC_QUERY_ECONTRACT_URL`) được poll lặp lại nhiều lần (đầu tiên sau submit ~10s, sau đó ở màn hình danh sách/ký hợp đồng) — log mọi lần poll hay chỉ lần đầu + lần cuối (khi trạng thái đổi)?

---

**Document Status:** ✅ Complete | **For:** Backend Dev (ekyc-admin team), BE Lead | **Next Steps:** Chốt 4 câu hỏi Section 4 → implement interceptor/aspect ghi log theo danh sách Section 1
