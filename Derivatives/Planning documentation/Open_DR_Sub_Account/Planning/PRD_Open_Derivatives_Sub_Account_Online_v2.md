# PRD v2.0 — Mở tiểu khoản phái sinh (sub 80) online trên NHSV Pro

**Product:** NHSV Pro / TradeX Derivatives
**Feature:** Đăng ký mở tiểu khoản phái sinh trực tuyến (end-to-end)
**Version:** 2.0 (phiên bản chi tiết kỹ thuật — kế thừa [v1.0](./PRD_Open_Derivatives_Sub_Account_Online.md))
**Last Updated:** April 22, 2026
**Audience:** PM, BA, Tech Lead FE/BE, Compliance, Ops, CSKH
**Lotte reference:** [DRACC-038 — `dr-create-sub-account`](../../../Documentation/%5BAPI%20specs%5DLotte_DR.md) §2.1.9
**FPT eContract reference:** `services/envelope/api/external/v1/template/structue`, `services/excall/api/excall`

---

## 1. Tóm tắt điều hành

Khách hàng cá nhân đã có **tài khoản chứng khoán cơ sở (TKCK) tại NHSV** có thể **tự phục vụ mở tiểu khoản phái sinh (sub `80`)** ngay trên app NHSV Pro. Hệ thống chặn các trường hợp không đủ điều kiện từ cả **FE (pre-flight)** lẫn **BE (authoritative)**; với khách đủ điều kiện, BE **khởi tạo hợp đồng điện tử** qua **FPT eContract** theo template `DERIVATIVES` (ký ảnh, TTL 90 ngày), chờ FPT callback xác nhận **KH đã ký**, rồi gọi **Lotte DRACC-038** để tạo sub `80`. Sau khi Lotte trả `error_code = 0000`, hệ thống **push OneSignal** + **gửi email** thông báo thành công.

**Nguyên tắc kiến trúc:** FE chỉ là **lớp UX pre-check**, BE là **nguồn sự thật** (SSOT) về eligibility, trạng thái hợp đồng, và provisioning sub. FPT callback là **duy nhất một trigger** để gọi Lotte — không gọi Lotte ngay sau khi tạo envelope.

---

## 2. Mục tiêu & chỉ số

| Mục tiêu | KPI gợi ý |
|---------|-----------|
| Tăng tỷ lệ khách đủ điều kiện hoàn tất mở sub 80 online | `completed_sub_80 / started_flow` ≥ mục tiêu (cần baseline) |
| Rút ngắn thời gian từ “Bắt đầu” → “Sub sẵn sàng giao dịch” | p50 ≤ 10 phút, p95 ≤ 24 giờ (chờ KH ký) |
| Giảm tải CSKH cho luồng mở sub | Số ticket trạng thái “Chờ ký / Lỗi” giảm QoQ |
| Không sai sót eligibility | Không có case lọt rule → 0 case mở sub sai điều kiện |

---

## 3. Định nghĩa

| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| Sub 80 | Tiểu khoản phái sinh (`subNumber = "80"`) |
| TKCK | Số tài khoản chứng khoán, format `039CXXXXXX` (6 chữ số cuối) |
| `/api/v1/login` | API login trả `accountSubs[]`, `username`, `bankCode` |
| `/api/v1/equity/account/contractStatus` | API trạng thái HĐ mở TKCK (`contractStatus`: `COMPLETED` / `PROCESSING` / …) |
| envelope | Hợp đồng điện tử phía FPT (có `envelopeId`) |
| DRACC-038 | API Lotte tạo sub phái sinh — `dr-create-sub-account` |
| OneSignal | Push notification provider |

---

## 4. Phạm vi

| In scope | Out of scope |
|----------|--------------|
| FE pre-check eligibility (4 rule) và error dialog | Flow mở TKCK cơ sở (redirect tới luồng sẵn có) |
| BE eligibility check với 4 error code | Thay đổi giao diện login / account switcher |
| Khởi tạo envelope FPT bằng template `DERIVATIVES` | Thay đổi nội dung template FPT (thuộc Pháp chế / BA) |
| Lưu envelopeId + refId vào DB TradeX | Quản lý long-term chứng từ (đã có hệ thống lưu trữ) |
| Xử lý callback FPT (Signed / Rejected / Voided / Overdue) | Luồng ký hộ / đại diện pháp lý (không nằm trong MVP) |
| Gọi Lotte DRACC-038 **sau khi KH ký thành công** | Cấu hình nhóm `fee_group`, `margin_ratio_group`, `warning_group` (dùng default `""`) |
| Gửi push OneSignal + email khi tạo sub thành công | Thông báo SMS |
| **Batch job bù callback FPT (Job A)** và **retry Lotte (Job B)** chạy 24/7 mỗi 5 phút | Hệ thống scheduler chung (dùng cơ chế cron sẵn có của TradeX) |
| **Lưu file HĐ trên Minio** và expose qua presigned URL cho admin | Triển khai Minio cluster (đã có infra) |
| **Trang admin** `https://tnhsvpro.nhsv.vn/nhsv-admin/admin`: list + filter + detail + view PDF | Phân quyền chi tiết role admin (workshop IAM riêng) |

---

## 5. Personas & stakeholders

| Vai trò | Nhu cầu chính |
|---------|---------------|
| KH retail | Nhanh, rõ ràng lý do nếu bị chặn, ký trên mobile dễ |
| CSKH | Biết trạng thái từng bước: Chờ ký / Đã ký / Provision fail |
| Ops / Compliance | Audit trail đầy đủ từ init envelope → Lotte response |
| BE / FE engineers | Contract API + error code rõ ràng, idempotent retry |

---

## 6. Luồng nghiệp vụ tổng thể

```
FE pre-check (4 rules)
        │
        ▼
BE eligibility (4 error codes)
        │
        ▼
FPT create envelope (template DERIVATIVES)  ──▶  DB: PENDING_SIGN
        │
        ▼
    KH ký HĐ trên FPT
        │
        ├───(callback)─────────────▶  DB: SIGNED ────▶  Job C: download PDF → Minio
        │                                  │
Job A (5'/24/7) poll FPT ──────▶ (bù callback)   │
                                                 ▼
                                           Lotte DRACC-038
                                                 │
                      ┌──────────────────────────┤
                      ▼                          ▼
                 SUCCESS (0000)           FAILED (1005 / 5xx)
                      │                          │
                      ▼                          ▼
              OneSignal + Email         Job B (5'/24/7) retry (cap 48)
                      │                          │
                      ▼                          ▼
                 NOTIFIED                NEEDS_VERIFICATION (Ops)

Admin portal https://tnhsvpro.nhsv.vn/nhsv-admin/admin
  ├── list + filter (date range / account_no / status)
  ├── detail: metadata + timeline
  └── View contract PDF (presigned Minio URL, TTL 5 phút)
```

---

## 7. Bước 1 — FE pre-check (khi KH mở màn hình “Mở TK phái sinh”)

**Mục tiêu:** Chặn sớm để KH không phải chờ round-trip BE/FPT/Lotte nếu chắc chắn không đủ điều kiện.

### 7.1 Ma trận rule (thứ tự kiểm tra)

| # | Điều kiện chặn | Nguồn dữ liệu | Thông điệp hiển thị | Hành động dialog |
|---|----------------|---------------|----------------------|------------------|
| 1 | Tài khoản liên kết bank ngoài NHSV | `/api/v1/login.accountSubs[].bankCode` — **nếu mọi sub có `bankCode ≠ "9999"`** | “Tài khoản liên kết bank không được phép mở tiểu khoản phái sinh.” | `Close` |
| 2 | KH đã có sub 80 | `/api/v1/login.accountSubs[].subNumber` — **tồn tại `"80"`** | “Tài khoản đã có tiểu khoản phái sinh.” | `Close` |
| 3 | TKCK không lưu ký tại NHSV | `/api/v1/login.username` — **3 ký tự đầu ≠ `"039C"`** | “Tài khoản không lưu ký tại NHSV, không được phép mở tiểu khoản phái sinh.” | `Close` |
| 4 | Chưa ký HĐ mở TKCK | `/api/v1/equity/account/contractStatus.contractStatus ≠ "COMPLETED"` | “Tài khoản chưa ký HĐ mở tài khoản. Vui lòng hoàn thiện HĐ mở tài khoản trước khi mở tiểu khoản phái sinh.” | `Close` hoặc `Ký HĐ` (điều hướng tới màn ký HĐ cơ sở) |

**Quy ước:**
- Kiểm tra tuần tự, **dừng ở rule đầu tiên fail**, hiển thị đúng một dialog.
- Copy text và button label phải **chính xác như bảng trên** (Pháp chế / PM duyệt trước release).
- Sau khi **cả 4 rule pass** → FE gọi BE `POST /api/v1/derivatives/sub-account/initiate` để vào bước 2.

### 7.2 UX state khi API login / contractStatus lỗi

| Tình huống | Xử lý |
|------------|-------|
| `/api/v1/login` lỗi / timeout | Dialog chuẩn network error + `Retry`; không vào rule check |
| `/api/v1/equity/account/contractStatus` lỗi | Không bypass rule — hiển thị “Không xác định được trạng thái HĐ mở TKCK, vui lòng thử lại” |

---

## 8. Bước 2 — BE authoritative eligibility check

**Endpoint (đề xuất):** `POST /api/v1/derivatives/sub-account/initiate`

**Nguyên tắc:** BE phải **lặp lại toàn bộ 4 rule của FE** bằng nguồn dữ liệu nội bộ / core, không tin FE. BE trả về **mã lỗi nghiệp vụ** để FE re-map sang dialog (hoặc bypass và hiển thị theo error message BE trả).

### 8.1 Bộ mã lỗi

| Error code | HTTP | Rule tương ứng | User message gợi ý |
|------------|------|----------------|---------------------|
| `BANK_ACCOUNT_INVALID` | 422 | Bank ≠ 9999 cho toàn bộ sub | Theo rule 1 §7.1 |
| `SUB_ALREADY_EXISTED` | 409 | Đã có sub 80 | Theo rule 2 §7.1 |
| `ACCOUNT_NOT_AT_NHSV` | 422 | `username` không prefix `039C` | Theo rule 3 §7.1 |
| `CONTRACT_NOT_COMPLETED` | 422 | `contractStatus ≠ COMPLETED` | Theo rule 4 §7.1 |
| `INTERNAL_ERROR` | 500 | Lỗi không mong đợi | Thông báo chung + retry |

> BA chốt: 4 mã lỗi nghiệp vụ là **bắt buộc**; HTTP status có thể điều chỉnh theo convention BE hiện hữu.

### 8.2 Success response (vào bước khởi tạo HĐ)

Khi cả 4 rule pass, BE tiếp tục sang §9 và trả cho FE:

```json
{
  "status": "READY_TO_SIGN",
  "refId": "req-uuid-a1b2c3d4",
  "envelopeId": "000011JlIB6PI4Ogw3mOKFQb",
  "signUrl": "<URL ký trên FPT>",
  "expiredAt": "2026-07-21T10:00:00+07:00"
}
```

---

## 9. Bước 3 — Khởi tạo hợp đồng điện tử FPT

### 9.1 Lấy cấu trúc template

- **Request:** `GET services/envelope/api/external/v1/template/structue?alias=DERIVATIVES`
- **Mục đích:** Lấy `templateId` mới nhất (ví dụ `000010oJivAQgioQ4rXAxEioJw`) và các `id` field nội bộ template để ghép payload ở §9.2.
- **Caching:** BE có thể cache `templateId` theo TTL (ví dụ 1 giờ) để giảm tải FPT; nhưng **phải invalidate khi FPT update template**.

### 9.2 Khởi tạo envelope

- **Request:** `POST services/excall/api/excall`
- **Top-level body:**

| Field | Giá trị | Ghi chú |
|-------|---------|---------|
| `id` | `""` | Mặc định rỗng |
| `refId` | UUID nội bộ (ví dụ `req-uuid-a1b2c3d4`) | Dùng idempotency & truy soát |
| `selector` | `"flow_start_nhsv_create_econtract_from_template_integrate"` | Hardcode theo FPT spec |
| `lookup` | **Bằng** `refId` | Truy soát nghiệp vụ |
| `attrs` | `null` | Mặc định |
| `payload` | `"PLHD"` | Hardcode |
| `body[]` | Danh sách object chi tiết — xem §9.3 | 1 phần tử cho mỗi envelope |

### 9.3 Payload `body[0]` — các field chính

**Envelope header:**

| `id` | `name` | `value` | Nguồn dữ liệu |
|------|--------|---------|----------------|
| `envName` | Tên tài liệu | `HĐGDPS <TKCK>-<HỌ TÊN KH>` (ví dụ `HĐGDPS 039C200327-NGUYEN MINH DUC`) | Ghép từ TKCK + họ tên — chuẩn hoá unicode, upper, bỏ ký tự đặc biệt |
| `envNo` | Số tài liệu | `""` | FPT tự sinh |
| `envDate` | Ngày ký | `""` | FPT cập nhật khi ký |
| `envSubmittedFrom` | Được gửi từ | `/1899/5511` | Hardcode |

**Party 1 — NHSV:**

| Field | Value |
|-------|-------|
| `p_001.name_party` | `CÔNG TY TRÁCH NHIỆM HỮU HẠN CHỨNG KHOÁN NH VIỆT NAM` |
| `p_001_r_001.name_recipient` | `NHSV` |
| `p_001_r_001.mail_recipient` | UAT: `mietftu@gmail.com` / Prod: `support@nhsv.vn` |
| `p_001_r_001.phone_recipient` | `""` |
| `p_001_r_001.contact_recipient` | `null` |

**Party 2 — Khách hàng:**

| Field | Value | Nguồn |
|-------|-------|-------|
| `p_002.name_party` | `individual` | Hardcode |
| `p_002_r_001.name_recipient` | Họ tên KH | Core — KYC |
| `p_002_r_001.mail_recipient` | Email KH | Core — KYC |
| `p_002_r_001.phone_recipient` | SĐT KH | Core — KYC |
| `p_002_r_001.contact_recipient` | `null` | Mặc định |

**Requester fields (giá trị hiển thị trên HĐ):**

| Field `name` | Nguồn |
|--------------|-------|
| `full_name` | Tên KH |
| `id_no` | Số CCCD |
| `issue_date` | Ngày cấp CCCD — format `dd/MM/yyyy` |
| `issue_place` | Nơi cấp CCCD |
| `no_1` … `no_6` | **6 chữ số cuối** của TKCK, mỗi ô một số (chú thích Untitled-1 dòng 254 có typo “thứ 2” cho `no_1` — BE chốt ký hiệu `no_n = chữ số thứ n tính từ đâu`) |
| `dr_register_date` | Thời điểm BE khởi tạo HĐ — format chốt với FPT |
| `dr_yn` | Luôn là `"x"` |

**Envelope control:**

| Field | Value |
|-------|-------|
| `dueDays` | `90` |
| `refId` (envelope-level) | Giá trị `reference_id` nội bộ (có thể trùng top-level `refId` hoặc có convention riêng — BE chốt) |

### 9.4 Response thành công từ FPT

```json
{
  "id": "62e94917-ba0e-48f3-8bbc-66202ed654af",
  "refId": "req-uuid-a1b2c3d4",
  "code": "0",
  "message": "Gửi thông tin sang econtract thành công xem chi tiết tại response",
  "result": null,
  "response": {
    "envelopeId": "000011JlIB6PI4Ogw3mOKFQb"
  }
}
```

**Quy tắc lưu DB:** Khi **cả hai** điều kiện đúng:
- `code == "0"`, **và**
- `response.envelopeId != null`

→ Lưu record vào bảng `derivatives_sub_registration` với state `PENDING_SIGN`. Các field tối thiểu:

| Column | Kiểu | Ghi chú |
|--------|------|---------|
| `id` (PK) | UUID | |
| `customer_id` | FK Core | |
| `account_no` | String | Ví dụ `039C003131` |
| `hts_user_id` | String | Dùng khi gọi Lotte |
| `ref_id` | String | `refId` top-level gửi FPT |
| `fpt_transaction_id` | String | Field `id` từ FPT response |
| `envelope_id` | String | `response.envelopeId` |
| `template_id` | String | `000010oJivAQgioQ4rXAxEioJw` hoặc mới nhất |
| `customer_full_name` | String | **Denormalized** từ Core KYC — phục vụ admin list query (tránh N+1) |
| `contract_status` | Enum | `PENDING_SIGN` / `SIGNED` / `REJECTED` / `VOIDED` / `OVERDUE` |
| `lotte_status` | Enum | `NOT_YET` / `CALLING` / `SUCCESS` / `FAILED` / `NEEDS_VERIFICATION` |
| `lotte_error_code` | String | VD `0000` hoặc `1005` |
| `lotte_error_desc` | String | |
| `lotte_retry_count` | Int | Số lần Job B đã retry (xem §12A); cap ví dụ 48 lần = 4h |
| `lotte_last_attempt_at` | Timestamp | Thời điểm lần gọi Lotte gần nhất — hỗ trợ backoff |
| `minio_bucket` | String | Ví dụ `derivatives-contracts` |
| `minio_object_key` | String | Ví dụ `derivatives-sub-registration/2026/04/{id}.pdf` |
| `contract_file_status` | Enum | `NOT_AVAILABLE` / `DOWNLOADING` / `STORED_IN_MINIO` / `DOWNLOAD_FAILED` |
| `created_at`, `updated_at`, `expired_at` | Timestamp | `expired_at = created_at + 90 ngày` |

### 9.5 Idempotency

- **Double-tap bảo vệ:** BE dùng `refId` làm khoá duy nhất cho 1 phiên `initiate`. Nếu client retry với cùng `refId` trong TTL ngắn (ví dụ 60s), BE trả lại envelope đã tạo thay vì tạo mới.
- **Không tạo envelope thứ 2** nếu đã có record `PENDING_SIGN` chưa hết hạn cho cùng `customer_id` + `account_no`. Trả `409 ALREADY_PENDING_SIGN` với `signUrl` của envelope hiện hữu.

---

## 10. Bước 4 — Callback ký hợp đồng

### 10.1 Các sự kiện cần xử lý

| Sự kiện FPT | `contract_status` (DB) | Next action |
|-------------|-------------------------|-------------|
| `contactId = Khách hàng` + `contactIdAction = Signed` | `SIGNED` | **Trigger** gọi Lotte DRACC-038 (§11) |
| `contractStatus = rejected` | `REJECTED` | Gửi email KH “HĐ bị từ chối” (template riêng), không gọi Lotte |
| `contractStatus = voided` | `VOIDED` | Đóng record, KH phải tạo lại từ đầu |
| `contractStatus = overdue` | `OVERDUE` | Đóng record sau 90 ngày, KH có thể tạo lại |
| `contractStatus = processing` | Giữ `PENDING_SIGN` | Cập nhật timestamp, không trigger thêm |
| `contractStatus = completed` (envelope hoàn tất toàn bộ bên) | Đồng bộ bổ sung nếu chưa `SIGNED` | |

### 10.2 Ràng buộc bắt buộc

- **Chỉ gọi Lotte khi `contract_status` chuyển sang `SIGNED`** (KH đã ký thành công). Đây là **điều kiện kiên quyết** — không được rút ngắn.
- Callback **idempotent**: lưu `event_id` / `(envelope_id, status, timestamp)` để không double-trigger Lotte khi FPT retry callback.
- **Race condition:** nếu nhận `SIGNED` 2 lần liên tiếp → chỉ gọi Lotte 1 lần (guard bằng `lotte_status IN ('CALLING', 'SUCCESS')`).
- **Bảo mật webhook:** verify signature FPT (chi tiết ở Technical Requirements); reject nếu invalid.

---

## 11. Bước 5 — Gọi Lotte DRACC-038

### 11.1 Wrapper BE → Lotte

**Endpoint nội bộ TradeX:** `POST /api/v1/lotte/create-derivative-sub`

**Request body BE → Lotte (map sang DRACC-038):**

```json
{
  "account_no": "039C003131",
  "hts_user_id": "039c003131"
}
```

> Mặc định không gửi `fee_group`, `margin_ratio_group`, `warning_group` (Lotte dùng default). Khi nghiệp vụ cần tuỳ biến — cập nhật PRD v2.1.

**Mapping sang Lotte (DRACC-038 §2.1.9):**

- URL: `[Root URL APIKEY]/tsol/apikey/tuxsvc/der/account/dr-create-sub-account`
- Method: `POST`
- Auth: OAuth2 Bearer + API KEY (theo chuẩn lotte-bridge)
- Body: `{ hts_user_id, account_no }` (+ các group optional để `""`)

### 11.2 Xử lý response

**Success (theo sample Lotte):**

```json
{
  "error_code": "0000",
  "error_desc": "[V0010]Đã xử lí xong một cách bình thường",
  "success": true,
  "total_record": "",
  "data_list": []
}
```

Khi `error_code == "0000"` **và** `success == true`:
- Set `lotte_status = SUCCESS`, `lotte_error_code = "0000"`.
- Trigger **§12 thông báo** (OneSignal + Email) — trong cùng transaction hoặc qua outbox pattern.
- Refresh cache `accountSubs` cho KH (để lần login tiếp theo thấy sub 80).

**Failure:**

| Tình huống | `lotte_status` | Retry policy |
|------------|----------------|--------------|
| `error_code = "1005"` / business error | `FAILED` | **Không auto-retry**; tạo ticket Ops kiểm tra thủ công |
| HTTP 5xx / timeout | `FAILED` (tạm) | Retry tối đa 3 lần với backoff 30s/2m/10m; sau đó `FAILED` + alert |
| Lotte trả response lạ / parse lỗi | `FAILED` | Log raw payload, alert ngay |

**Quan trọng — khoảng trống đặc tả (xem §8 phân tích DRACC-038 trong [Lotte_DR.md](../../../Documentation/%5BAPI%20specs%5DLotte_DR.md)):** `data_list` có thể **rỗng** trong sample. **Không** giả định `sub_no` mới trả trong payload. Thay vào đó:
- Sau khi Lotte báo thành công, BE **gọi lại DRACC-018 / đồng bộ danh sách sub từ Core** để lấy số sub mới (hoặc poll mỗi 5s tối đa 30s).
- Nếu sau timeout vẫn không thấy sub 80 → đánh `lotte_status = NEEDS_VERIFICATION` để Ops xác minh.

---

## 12. Bước 6 — Thông báo thành công

Trigger khi `lotte_status` chuyển sang `SUCCESS`.

### 12.1 Push OneSignal

| Field | Giá trị |
|-------|---------|
| Template | `derivatives_sub_created_success` |
| Title | “Mở tiểu khoản phái sinh thành công” |
| Body | “Tiểu khoản phái sinh (sub 80) của bạn đã sẵn sàng. Bắt đầu giao dịch ngay!” |
| Deep link | `nhsvpro://derivatives/dashboard` |
| Target | `customer_id` |

### 12.2 Email

| Field | Giá trị |
|-------|---------|
| Template | `derivatives_sub_created_success_email` |
| To | Email KH (Core KYC) |
| Subject | “[NHSV] Mở tiểu khoản phái sinh thành công” |
| Body | Template do Marketing/Pháp chế duyệt; chèn TKCK, thời điểm tạo, hướng dẫn tiếp theo |

### 12.3 Best practices

- **Outbox pattern:** ghi event `NOTIFICATION_PENDING` trong cùng transaction với update `lotte_status = SUCCESS`; worker publish OneSignal + Email → không double-send nếu service chết giữa chừng.
- **De-dup:** OneSignal gắn `external_id = registration.id` để tránh push trùng.
- Không chặn luồng nghiệp vụ — nếu email fail, sub vẫn được coi đã tạo; chỉ log + alert.

---

## 13. Batch jobs — an toàn mạng lưới (24/7, mỗi 5 phút)

Spec Untitled-1 mô tả một batch job duy nhất, nhưng để **không vi phạm ràng buộc §10.2** (“chỉ gọi Lotte khi KH đã ký”), PRD chia làm **2 job độc lập** + 1 job phụ cho Minio.

### 13.1 Job A — FPT status poll (bù callback)

| Mục | Giá trị |
|-----|---------|
| Mục tiêu | Bảo hiểm cho trường hợp **FPT callback bị mất / chậm / bị reject do lỗi mạng** |
| Tần suất | 5 phút / lần, 24/7 |
| Input query | `SELECT … FROM derivatives_sub_registration WHERE contract_status = 'PENDING_SIGN' AND expired_at > NOW()` |
| Action mỗi record | Gọi FPT `GET envelope status` theo `envelope_id`; nếu FPT trả trạng thái khác `PENDING` → xử lý y như callback (§10.1) — update DB, trigger Lotte nếu `SIGNED` |
| Giới hạn | Batch size ≤ 200 record/lần để tránh rate-limit FPT |
| Idempotency | Dùng chung guard §10.2 — callback và poll cùng gọi vào 1 handler duy nhất |
| Alert | Nếu >50% record trong batch lỗi FPT → page on-call |

### 13.2 Job B — Lotte provisioning retry

| Mục | Giá trị |
|-----|---------|
| Mục tiêu | Retry DRACC-038 cho các HĐ đã ký mà Lotte call chưa thành công |
| Tần suất | 5 phút / lần, 24/7 |
| Input query | `WHERE contract_status = 'SIGNED' AND lotte_status IN ('FAILED', 'NEEDS_VERIFICATION') AND lotte_retry_count < 48 AND lotte_last_attempt_at < NOW() - INTERVAL '5 min'` |
| Action mỗi record | Gọi `/lotte/create-derivative-sub` (§11); tăng `lotte_retry_count`, cập nhật `lotte_last_attempt_at` |
| Dừng retry | Khi `lotte_retry_count >= 48` (≈ 4 giờ) → chuyển `lotte_status = NEEDS_VERIFICATION` và alert Ops; không auto-retry thêm |
| Thành công | Giống §11 — trigger notification + Job C (download PDF) |
| Idempotency | Guard bằng `lotte_status IN ('CALLING', 'SUCCESS')` để không gọi đồng thời với callback đầu tiên |

### 13.3 Job C — Minio PDF download (xem §14)

Chi tiết ở §14.3. Trigger mỗi 5 phút cho record `contract_status = SIGNED` AND `contract_file_status IN ('NOT_AVAILABLE', 'DOWNLOAD_FAILED')`.

### 13.4 Chiến lược chống conflict giữa callback và job

- Callback FPT đến trong lúc Job A đang poll cùng `envelope_id` → dùng **row-level lock** (`SELECT … FOR UPDATE`) hoặc **optimistic lock** theo `updated_at` để tránh update đè.
- Job B và handler callback đều có thể trigger Lotte — trước khi gọi Lotte, **CAS** (compare-and-set) `lotte_status` từ `NOT_YET/FAILED` → `CALLING`; chỉ thread thắng CAS được gọi API.

---

## 14. Lưu trữ file hợp đồng trên Minio

### 14.1 Tại sao Minio

- Chứng từ ký số phải lưu ≥ 10 năm theo quy định.
- Không phụ thuộc FPT — nếu FPT đổi retention policy hoặc hệ thống FPT down, NHSV vẫn truy xuất được.
- Admin view cần **presigned URL** có thời hạn để bảo mật.

### 14.2 Thiết kế

| Hạng mục | Quy ước |
|----------|---------|
| Bucket | `derivatives-contracts` (1 bucket duy nhất, versioning bật) |
| Object key | `derivatives-sub-registration/{YYYY}/{MM}/{registration_id}.pdf` |
| Source file | PDF đã ký (fully signed) từ FPT API (ví dụ: `services/envelope/api/external/v1/envelope/{envelopeId}/download`) |
| Trigger upload | Ngay sau khi `contract_status` chuyển `SIGNED` (qua callback hoặc Job A) |
| Access | Admin request → BE generate **presigned GET URL** TTL 5 phút; không public bucket |
| Metadata object | `customer_id`, `account_no`, `envelope_id`, `ref_id`, `signed_at` — lưu vào Minio object metadata để tiện compliance scan |
| Lifecycle | Bucket policy: không auto-delete, archive sau 2 năm sang class lạnh (nếu Minio cluster hỗ trợ) |

### 14.3 Job C — PDF download worker

```
if contract_status = SIGNED AND contract_file_status IN ('NOT_AVAILABLE', 'DOWNLOAD_FAILED'):
    set contract_file_status = DOWNLOADING
    try:
        pdf_bytes = fpt.download_envelope(envelope_id)
        minio.put(bucket, object_key, pdf_bytes, metadata=...)
        set contract_file_status = STORED_IN_MINIO, minio_object_key = ..., minio_bucket = ...
    except:
        set contract_file_status = DOWNLOAD_FAILED
        increment retry_count (cap 10, sau đó alert Ops)
```

**Ghi chú:** Job C **KHÔNG** block Lotte provisioning — 2 luồng song song. Nếu Lotte đã SUCCESS nhưng Minio chưa có file, KH vẫn giao dịch được; admin chỉ thiếu file PDF tạm thời.

---

## 15. Trang admin — theo dõi mở sub phái sinh

**URL:** `https://tnhsvpro.nhsv.vn/nhsv-admin/admin` (section **Derivatives Sub Registration**)

### 15.1 Filter

| Filter | Kiểu | Default | Ghi chú |
|--------|------|---------|---------|
| Date range | From / To (date picker) | **1 tháng gần nhất** (today − 30 → today) | Filter theo `created_at` |
| Account number | Text input | Rỗng | Exact match `account_no` (cho phép partial match theo convention admin hiện hữu) |
| Status | Single-select dropdown | `All` | 5 giá trị — xem §15.2 |

### 15.2 Status filter mapping (union 2 chiều)

| Filter value (admin) | Ý nghĩa nghiệp vụ | SQL condition |
|----------------------|--------------------|---------------|
| `Contract Initiated` | Khởi tạo HĐ thành công (chờ ký) | `contract_status = 'PENDING_SIGN'` |
| `Contract Signed` | KH đã ký thành công (đồng thời chưa/đang provision) | `contract_status = 'SIGNED' AND lotte_status <> 'SUCCESS'` |
| `Contract Rejected` | KH từ chối / voided / overdue (gom nhóm) | `contract_status IN ('REJECTED', 'VOIDED', 'OVERDUE')` |
| `Processing` | Đang xử lý mở sub — đã gọi / đang retry Lotte | `contract_status = 'SIGNED' AND lotte_status IN ('NOT_YET', 'CALLING', 'FAILED', 'NEEDS_VERIFICATION')` |
| `Completed` | Mở sub thành công | `lotte_status = 'SUCCESS'` |

> **Lưu ý BA:** `Contract Signed` và `Processing` có vùng SQL gần trùng (đều là `SIGNED` + lotte chưa SUCCESS). Đề xuất gộp hai state này trong UX admin — nhưng vì spec yêu cầu **tách riêng**, PRD giữ nguyên, filter `Contract Signed` hiển thị **toàn bộ** record đã ký chưa provision SUCCESS, `Processing` là **subset** có lotte_status khác `NOT_YET` (tức BE đã bắt đầu gọi Lotte). Engineer chốt cuối với BA trước implementation.

### 15.3 Table columns

| Column | Source | Display rule |
|--------|--------|--------------|
| Request ID | `id` (UUID) | Rút gọn 8 ký tự đầu + tooltip full |
| Full name | `customer_full_name` (denormalized) | Full text |
| Account number | `account_no` | `039CXXXXXX` |
| Overall status | Computed từ `lotte_status` | `lotte_status = SUCCESS` → **Completed**; else → **Processing** |
| Contract status | Computed từ `contract_status` | `PENDING_SIGN` → **Contract Initiated**; `SIGNED` → **Contract Signed**; `REJECTED/VOIDED/OVERDUE` → **Contract Rejected** |
| Created at | `created_at` | Format `dd/MM/yyyy HH:mm` GMT+7 |
| Updated at | `updated_at` | Format `dd/MM/yyyy HH:mm` GMT+7 |
| Action `View` | Button | Xem chi tiết (§15.4) |

### 15.4 Detail view

Sidebar hoặc modal chi tiết khi click `View`:

- Toàn bộ column của table + metadata:
  - `envelope_id`, `ref_id`, `fpt_transaction_id`, `template_id`
  - `lotte_error_code`, `lotte_error_desc`, `lotte_retry_count`, `lotte_last_attempt_at`
  - `contract_file_status`
  - Timeline các state transition (kéo từ bảng audit log)
- **Nút “Xem file HĐ”:** chỉ enable khi `contract_file_status = STORED_IN_MINIO`.
  - Click → BE tạo **presigned URL** TTL 5 phút → browser mở tab mới tới URL đó.
  - Log action vào audit (`actor = admin_user_id`, `action = VIEW_CONTRACT_PDF`).
- **(Tuỳ chọn) Ops action:**
  - `Retry Lotte` — chỉ enable với state `LOTTE_FAILED` / `NEEDS_VERIFICATION`; chỉ role `ops_derivatives` được dùng.
  - `Resend notification` — gửi lại email + push khi KH báo không nhận được.

### 15.5 Pagination & performance

- Mặc định **20 record / page**, support 50, 100.
- Query có index trên `(created_at)`, `(account_no)`, `(contract_status, lotte_status)`.
- Full-text search theo `customer_full_name` là nice-to-have (không bắt buộc MVP).

---

## 16. Trạng thái tổng hợp (state machine)

```
INIT
  │  (BE eligibility OK + FPT envelope created)
  ▼
PENDING_SIGN ──────────────────▶ (Job A poll mỗi 5 phút)
  │ │
  │ │───(timeout 90d)───────────▶ OVERDUE ─┐
  │ ├───(callback/poll rejected)▶ REJECTED ┼─▶ (terminal — hiển thị "Contract Rejected")
  │ └───(callback/poll voided)──▶ VOIDED ──┘
  │
  ▼ (callback/poll signed)
SIGNED ────────────────────────▶ (Job C download PDF → Minio, song song)
  │
  │ (trigger Lotte DRACC-038, guard CAS lotte_status)
  ▼
LOTTE_CALLING
  │
  ├─(0000 + success=true)──▶ PROVISIONED ──▶ NOTIFIED (terminal success)
  │
  ├─(1005 / 5xx)───────────▶ LOTTE_FAILED
  │                              │ (Job B retry mỗi 5 phút, cap 48 lần)
  │                              ▼
  │                         (nếu retry thành công) → PROVISIONED
  │                              │ (cap exhausted)
  │                              ▼
  └──────────────────────────▶ NEEDS_VERIFICATION (terminal, Ops xử lý thủ công)
```

**Ánh xạ sang trạng thái admin UI:**

- **Contract status:** `PENDING_SIGN → Initiated`, `SIGNED → Signed`, `REJECTED/VOIDED/OVERDUE → Rejected`.
- **Overall status:** `lotte_status = SUCCESS → Completed`, tất cả trường hợp khác → `Processing`.

---

## 17. Yêu cầu phi chức năng

| Lĩnh vực | Yêu cầu |
|----------|---------|
| Bảo mật | Verify FPT callback signature; giới hạn IP nếu FPT cung cấp; mã hóa `hts_user_id` ở rest; presigned URL Minio TTL 5 phút |
| Tuân thủ | Lưu toàn bộ FPT request/response ≥ 10 năm; file HĐ trên Minio không auto-delete, versioning bật |
| Audit trail | Log mọi state transition với `actor`, `timestamp`, `source` (`FE`, `FPT_CALLBACK`, `FPT_POLL`, `LOTTE`, `LOTTE_RETRY`, `MINIO`, `OPS`) |
| Idempotency | `refId` unique, callback dedupe, Lotte CAS guard, Job A/B lock theo `id` (xem §9.5, §10.2, §11, §13.4) |
| Observability | Metric: count by state, Lotte latency, FPT latency, callback error rate, **Job A/B/C success ratio**, Minio upload p95. Alert: `LOTTE_FAILED` > 5/giờ, Job B backlog > 100 record, Job C fail rate > 10% |
| Khả dụng | Timeout BE→FPT: 15s; BE→Lotte: 10s; BE→Minio: 30s (upload PDF); retry theo §11.2 và §13 |
| Throughput | Mỗi job xử lý ≤ 200 record/run, tổng ≤ 2400 record/giờ — đủ cho 10× volume dự kiến năm đầu |
| I18n | Copy text tiếng Việt chuẩn (Pháp chế duyệt); admin UI hỗ trợ tiếng Việt + tiếng Anh cho label |

---

## 18. Phụ thuộc

| Phụ thuộc | Rủi ro | Mitigation |
|-----------|--------|------------|
| `/api/v1/login` trả đầy đủ `accountSubs[].bankCode`, `subNumber` | Rule 1, 2 không chạy được | BE fallback: query Core API trực tiếp |
| `/api/v1/equity/account/contractStatus` | Rule 4 không chạy được | BE retry + cache 5s |
| FPT eContract SLA callback, quota template, download envelope API | KH kẹt “Chờ ký”, lag cập nhật, mất file PDF | Job A polling 5 phút; Job C retry download |
| Lotte DRACC-038 availability | KH không thể mở sub dù đã ký | Job B retry 5 phút cap 48 lần, circuit breaker, KH thấy “Đang xử lý” |
| OneSignal / SMTP | Không nhận được noti | Outbox pattern + retry; không block provisioning |
| **Minio cluster** availability | File HĐ không tải được, admin view báo lỗi | Job C retry download; admin có thể fallback tải trực tiếp từ FPT (role cao hơn) |
| **Admin portal `tnhsvpro.nhsv.vn/nhsv-admin`** sẵn có layout | Tích hợp tốn thời gian nếu layout lạ | Coordinate sớm với team admin để thống nhất component UI |

---

## 19. Tiêu chí chấp nhận

**FE pre-check:**
- [ ] KH vi phạm rule 1/2/3/4 thấy đúng dialog với copy như §7.1, không gọi BE `initiate`.
- [ ] KH pass cả 4 rule → FE gọi BE `initiate` và chuyển sang màn ký FPT.

**BE authoritative check:**
- [ ] BE trả 4 mã lỗi nghiệp vụ đúng rule, FE không phụ thuộc FE-check vẫn chặn được.
- [ ] Bỏ qua FE (gọi BE trực tiếp bằng Postman) với KH không đủ điều kiện → vẫn bị BE chặn.

**FPT flow:**
- [ ] BE tạo envelope thành công, lưu `envelope_id` + `ref_id`; admin thấy trạng thái `Contract Initiated`.
- [ ] Callback `Signed` → `SIGNED`, **đúng một** trigger Lotte dù FPT retry callback hoặc Job A chạy cùng lúc.
- [ ] Callback `rejected` / `voided` / `overdue` → Lotte **không** được gọi.

**Lotte DRACC-038:**
- [ ] Lotte trả `0000` → `lotte_status = SUCCESS`, OneSignal + email gửi **đúng một lần**.
- [ ] Lotte trả `1005` → `LOTTE_FAILED`, Job B retry định kỳ; không double-provision.
- [ ] Lotte timeout 3 lần trong 1 attempt → `LOTTE_FAILED`; sau `lotte_retry_count ≥ 48` → `NEEDS_VERIFICATION` + alert Ops.

**Batch jobs:**
- [ ] Job A chạy 24/7 mỗi 5 phút, **kill switch** được để tạm dừng khi FPT bảo trì.
- [ ] Job B không gọi Lotte khi `contract_status ≠ SIGNED` (không vi phạm §10.2).
- [ ] Job C upload PDF thành công cho 100% record `SIGNED` trong vòng 15 phút (SLA nội bộ).

**Minio:**
- [ ] File PDF được upload đúng object key convention §14.2.
- [ ] Presigned URL hết hạn sau 5 phút; admin click lại → sinh URL mới.
- [ ] Bucket có versioning; thử xoá object thủ công → vẫn khôi phục được.

**Notifications:**
- [ ] KH nhận 1 push + 1 email sau mỗi lần tạo sub thành công.
- [ ] Không có duplicate noti khi service restart giữa chừng.

**Admin:**
- [ ] Default filter date range = 1 tháng gần nhất; không để trống cả từ & đến.
- [ ] Filter `Account number` + `Status` + date range kết hợp ra đúng record.
- [ ] Mỗi row hiển thị đủ 7 column theo §15.3; nút `View` mở detail với trạng thái nút “Xem file HĐ” đúng theo `contract_file_status`.
- [ ] Hành động `Retry Lotte` chỉ enable với state `LOTTE_FAILED` / `NEEDS_VERIFICATION` và chỉ role Ops thấy nút.

---

## 20. Câu hỏi mở (cần workshop trước implementation)

1. `no_1 … no_6` map thứ tự ký tự chính xác như thế nào? (Spec Untitled-1 dòng 254 có typo — BE cần sample từ FPT mẫu).
2. Production email NHSV là `support@nhsv.vn` — xác nhận chính thức và cơ chế đổi theo môi trường.
3. Template FPT `DERIVATIVES` — `templateId` có đổi định kỳ không? Nếu có → BE cần subscribe webhook từ FPT hoặc luôn re-fetch.
4. Khi Lotte báo `0000` nhưng Core chưa thấy sub 80 trong 30s → flow Ops cụ thể là gì?
5. Có cần thêm **OTP email** trước khi tạo envelope (như PRD v1.0 đề cập) hay authentication đăng nhập app đã đủ?
6. Khi KH có `REJECTED`/`VOIDED`/`OVERDUE` — có giới hạn số lần tạo lại không (chống spam)?
7. Admin có quyền **huỷ envelope đang `PENDING_SIGN`** thay mặt KH không?
8. **Batch job query gốc (Untitled-1 dòng 333)** nói “chưa ký” — xác nhận nghiệp vụ thực là Job A (poll FPT) + Job B (retry Lotte) như PRD §13 đề xuất, hay còn job thứ 3?
9. **Admin filter `Contract Signed` vs `Processing`** — gộp thành 1 state hay giữ tách (xem §15.2 note)?
10. **Quyền xem file HĐ PDF** — role nào được xem? Có cần ghi 2FA / approval 4 mắt trước khi sinh presigned URL cho data nhạy cảm?
11. **Minio bucket tồn tại** hay cần provision mới? Nếu mới — SRE/DevOps có SLA provisioning không?
12. `Request ID` trên admin hiển thị `id` (UUID) hay `ref_id` (UUID do BE sinh, dễ nhớ hơn)?

---

## 21. Tham chiếu chéo

| Tài liệu | Mục đích |
|----------|----------|
| [PRD v1.0](./PRD_Open_Derivatives_Sub_Account_Online.md) | Framework planning — scope, stakeholder, state tổng quan |
| [Lotte_DR.md §2.1.9 DRACC-038](../../../Documentation/%5BAPI%20specs%5DLotte_DR.md) | Spec Lotte API — `dr-create-sub-account` |
| TradeX Derivatives API Index | Sẽ bổ sung entry `POST /api/v1/derivatives/sub-account/initiate` và `POST /api/v1/lotte/create-derivative-sub` |
| TradeX Knowledge — FPT eContract (eKYC) | Pattern callback, idempotency, audit tham khảo |

---

**Document Status:** 🟢 Ready for Engineering Review
**For:** PM, BA, Tech Lead FE/BE, Ops, Compliance, SRE/DevOps (Minio + Admin portal)
**Next Steps:**
1. Workshop trả lời **12 câu hỏi mở** ở §20.
2. BE design schema `derivatives_sub_registration` + API contract chi tiết (chuyển sang thư mục `Specifications/`).
3. Pháp chế duyệt copy dialog + template email/push + template email rejected/overdue.
4. FE mock API response để làm UX trước khi BE ready.
5. **SRE/DevOps** provision Minio bucket + IAM policy cho TradeX BE + Admin portal.
6. **Team Admin Portal** confirm component library để tích hợp màn Derivatives Sub Registration.
7. Định nghĩa **runbook Ops** cho trạng thái `NEEDS_VERIFICATION` (khi retry Lotte đã cap).
