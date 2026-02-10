# Xử lý lỗi ACCOUNT_CREATED từ API eKYC mở tài khoản

## Giải thích đơn giản (non-tech)

**Tình huống:** Khách hàng điền form eKYC để mở tài khoản chứng khoán, bấm gửi.

**Luồng xử lý:**

1. **App/Web** gửi dữ liệu lên **TradeX** (hệ thống NHSV).
2. **TradeX** chuyển tiếp sang **Core** (hệ thống Lotte – nghiệp vụ mở tài khoản).
3. **Core** kiểm tra và trả lời: *“Số CCCD này đã có tài khoản rồi, không mở thêm được.”*  
   Core gửi kèm:
   - **Mã lỗi** trong ngoặc vuông: `[ ACCOUNT_CREATED]`
   - **Câu thông báo** (nếu có), ví dụ: *“Tài khoản đã được tạo với số CCCD này.”*
4. **TradeX** nhận câu trả lời từ Core, lấy ra:
   - **Mã lỗi:** `ACCOUNT_CREATED`
   - **Câu thông báo:** phần chữ sau ngoặc vuông (nếu Core gửi)
5. **TradeX** trả về cho App: **mã lỗi** `ACCOUNT_CREATED` (và có thể kèm **message** từ Core).
6. **App** hiển thị cho khách: ví dụ *“Số CCCD đã được dùng mở tài khoản. Bạn có thể đăng nhập hoặc liên hệ 1900 1055.”*

**Tóm lại:**

- **Core gửi gì:** Mã lỗi dạng `[ ACCOUNT_CREATED]` + (tuỳ) câu thông báo bằng chữ.
- **TradeX làm gì:** Đọc mã + message từ Core, chuyển nguyên hoặc chuẩn hoá rồi trả lại App dưới dạng **code** (và **message** nếu có).
- **App làm gì:** Nhận **code = ACCOUNT_CREATED**, hiển thị thông báo thân thiện cho người dùng (có thể dùng **message** từ TradeX nếu muốn hiển thị đúng câu chữ từ Core).

---

## Tổng quan

Khi **POST /api/v1/lotte/ekycs** (submit thông tin eKYC để hoàn tất mở tài khoản) trả về lỗi **"ACCOUNT_CREATED"**, đây là trường hợp Lotte báo rằng **tài khoản với CCCD/identifierId đó đã được tạo rồi** (trùng hoặc đã mở tài khoản trước đó).

## Nguồn lỗi

- **Lotte API** (phía Lotte Securities) trả response lỗi với format chuẩn:
  - `error_code`: mã không phải `"0000"`
  - `error_desc`: chuỗi dạng **`"[ ACCOUNT_CREATED] Nội dung thông báo"`** (code nằm trong dấu `[ ]`)

## Cơ chế xử lý trong lotte-bridge

### 1. Parse response Lotte (`parseMessages`)

**File:** `lotte-bridge/src/utils/lotte.ts`

```ts
export function parseMessages(errorDesc: string, errorCode: string): { codes: string; messages: string } {
  let codes: string = null;
  let messages: string = null;
  if (errorDesc.length > 0) {
    const startIndex = errorDesc.indexOf('[');
    const endIndex = errorDesc.indexOf(']');
    if (startIndex >= 0 && endIndex > 0) {
      codes = errorDesc.substring(startIndex + 2, endIndex);  // "[ " -> bỏ 2 ký tự
      messages = errorDesc.substring(endIndex + 1);
    }
    // ...
  }
  return { codes, messages };
}
```

- Từ `error_desc = "[ ACCOUNT_CREATED] Thông báo"` → **`codes = "ACCOUNT_CREATED"`**, **`messages = " Thông báo"`**.

### 2. Throw lỗi ra client

Các service trong **lotte-bridge** (ví dụ `EkycService`, `OrderService`, …) khi xử lý response Lotte lỗi thường:

- Gọi `parseMessages(lotteRes.error_desc, lotteRes.error_code)`.
- Nếu không phải success (`error_code !== '0000'`):  
  **`throw new GeneralError(\`${codes}\`)`** (từ `tradex-common`).

→ Client nhận response lỗi với **`code: "ACCOUNT_CREATED"`** (và thường kèm HTTP 4xx từ rest-proxy).

### 3. Luồng eKYC liên quan trong lotte-bridge

Trong **lotte-bridge** (repo **nhsv-dev/lotte-bridge**, project TRDX):

| API | Handler | Ghi chú |
|-----|---------|--------|
| GET /api/v1/ekycs/banks | EkycService.getBankList | Danh sách ngân hàng |
| GET /api/v1/ekycs/branch | EkycService.getListBranch | Chi nhánh NHSV |
| GET /api/v1/ekycs/banks/{id}/branches | EkycService.getBanksListBranch | Chi nhánh ngân hàng |
| POST /api/v1/equity/account/checkNationalId | EkycService.checkNationalId | Kiểm tra CCCD đã dùng chưa |
| GET /api/v1/ekycs/partner | EkycService.getPartnerName | Tên CTV/Nhân viên |
| GET /api/v1/ekycs/account/exist | EkycService.checkAccountOpeningStatus | Kiểm tra đã mở tài khoản chưa (stk-acc-info) |

**Quan trọng:** Trong **RequestHandler.ts** (cả bản local và Bitbucket) **không có** handler cho **`post:/api/v1/lotte/ekycs`** (submit form eKYC hoàn tất mở tài khoản). Request tới URI này có thể:

- Được **rest-proxy** forward sang **topic khác** (ví dụ **ekyc-admin**), routing lấy từ **configuration** (openApi list / DB), hoặc  
- Do một phiên bản/instance khác của lotte-bridge hoặc service khác xử lý (cần kiểm tra cấu hình deployment và DB `t_open_api`).

## Cách client/FE nên xử lý ACCOUNT_CREATED

1. **Nhận diện:** Response lỗi có **`code === "ACCOUNT_CREATED"`**.
2. **Ý nghĩa:** CCCD này đã có tài khoản tại NHSV (đã tạo trước đó).
3. **Hành vi gợi ý:**
   - Hiển thị thông báo dạng: “Số CCCD này đã được sử dụng để mở tài khoản. Nếu đã có tài khoản, vui lòng đăng nhập hoặc liên hệ hotline 1900 1055.”
   - Cho phép chuyển sang màn hình đăng nhập hoặc hỗ trợ (hotline/chat).

## Phân biệt path: /api/v1/lotte/ekycs vs /api/v1/ekycs

| TradeX API (client gọi) | Method | Mục đích | Handler (lotte-bridge) | Lotte API (LotteAPI.ts) |
|------------------------|--------|----------|------------------------|--------------------------|
| **/api/v1/lotte/ekycs** | POST | Submit/update form eKYC (hoàn tất mở TK) | ❌ Không có trong RequestHandler | ❌ Không có trong LotteAPI.ts |
| **/api/v1/lotte/ekycs/create** | POST | Tạo mã đơn eKYC (phoneNo, email) → eKycId | ❌ Không có trong RequestHandler | ❌ Không có trong LotteAPI.ts |
| **/api/v1/ekycs/banks** | GET | Danh sách ngân hàng | EkycService.getBankList | `tsol/apikey/tuxsvc/ekyc/bank-branch` (code_tp: bank_cd_off) |
| **/api/v1/ekycs/branch** | GET | Chi nhánh NHSV | EkycService.getListBranch | `tsol/apikey/tuxsvc/ekyc/bank-branch` (code_tp: brch_cd) |
| **/api/v1/ekycs/banks/{id}/branches** | GET | Chi nhánh theo ngân hàng | EkycService.getBanksListBranch | `tsol/apikey/tuxsvc/ekyc/bank-branch` (code_tp: bank_brch) |
| **/api/v1/ekycs/partner** | GET | Tên CTV/Nhân viên (emp-check) | EkycService.getPartnerName | `tsol/apikey/tuxsvc/ekyc/emp-check` |
| **/api/v1/ekycs/account/exist** | GET | Đã mở TK chưa (stk-acc-info) | EkycService.checkAccountOpeningStatus | `tsol/apikey/tuxsvc/ekyc/stk-acc-info` |
| **/api/v1/equity/account/checkNationalId** | POST | Kiểm tra CCCD đã dùng chưa | EkycService.checkNationalId | `tsol/apikey/tuxsvc/ekyc/check-exist` |

- **Đúng path cho API submit eKYC (có thể trả ACCOUNT_CREATED):** **POST /api/v1/lotte/ekycs** (có prefix **lotte**).
- **POST /api/v1/lotte/ekycs** và **POST /api/v1/lotte/ekycs/create** không được map trong **lotte-bridge** (không có trong RequestHandler và LotteAPI.ts); nhiều khả năng do **ekyc-admin** hoặc service khác xử lý và gọi Lotte qua endpoint khác (chưa có trong repo này).

## Tài liệu tham khảo

- **API spec:** `TradeX API - Addons.yaml` → `POST /api/v1/lotte/ekycs` (Update information in eKYC application), `POST /api/v1/lotte/ekycs/create`, và các GET `/api/v1/ekycs/*`.
- **Luồng eKYC (kiến trúc):** `NHSV_Complete_Architecture_Documentation.md` (phần eKYC, EKYC → LB → Lotte).
- **Lotte error format:** Các service trong `lotte-bridge-main/src/services/` dùng chung `parseMessages` và `GeneralError(codes)`.

## Cần kiểm tra thêm (ngoài repo lotte-bridge)

- **ekyc-admin** (Bitbucket: nhsv-dev): Service có thể nhận **POST /api/v1/lotte/ekycs** qua Kafka và gọi Lotte; nếu có, logic xử lý (và pass-through) **ACCOUNT_CREATED** sẽ nằm ở đây.
- **configuration** (DB `t_open_api`): Xem bản ghi **`post:/api/v1/lotte/ekycs`** map vào **topic** nào để xác định service thực sự xử lý API này.
