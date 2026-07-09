# Analyst Findings: Rút tiền Sub Phái sinh (DRACC-039 / DRACC-040 / DRACC-041)

**Nguồn Lotte doc:** `derivatives_api_doc.txt` — §3.2 GIAO DỊCH TIỀN PHÁI SINH, lines 4079–4786 (converted from docx). Changelog: "6/7/2026 - HuongLT - Thêm mới DRACC-039,040,041" → xác nhận đây là 3 API **mới hoàn toàn** phía Lotte.

---

## Tổng quan API

| API | Ý nghĩa | Kafka Topic | Consumer Service | Integration Type |
|-----|---------|-------------|-------------------|-------------------|
| DRACC-039 | Lịch sử rút tiền sub phái sinh (query) | `lotte-bridge` | `lotte-bridge` | lotte-integrated |
| DRACC-040 | Thực hiện rút tiền (mutation) | `lotte-bridge` | `lotte-bridge` | lotte-integrated |
| DRACC-041 | Tra cứu số dư khả dụng rút (query) | `lotte-bridge` | `lotte-bridge` | lotte-integrated |

**Nguồn:** knowledge-doc (Cash transaction README + VSD_Transaction_API_Spec.md) + codebase-scan xác nhận (`Knowledge/TradeX-MCP/rest-proxy-main`, `lotte-bridge-main`).

Đây là nhóm tính năng **"Rút tiền sub phái sinh về ngân hàng"** — khác với VSD Transaction (DRACC-009, nộp/rút margin NHSV↔VSD) và Internal Transfer (DRACC-019/020, chuyển giữa các sub). README hiện tại của Cash Transaction ghi rõ "❌ External bank transfers" là **Out of Scope** → xác nhận đây là **domain mới**, không trùng lặp công việc đã làm.

---

## ⚠️ PHÁT HIỆN QUAN TRỌNG: Đã có route "vaporware" trong rest-proxy khớp một phần

Khi scan `Knowledge/TradeX-MCP/rest-proxy-main/src/app/routes/api/derivatives/vcsc/Transfer.ts` (route dùng thực tế cho NHSV theo quy ước đã xác nhận ở `regular-order-api-mapping.md`), đã tồn tại **swagger stub** (chỉ có JSDoc, không tìm thấy service implementation trong `lotte-bridge-main`):

```
GET  /derivatives/transfer/cash/withdraw   → DerivativesCashWithdrawInfoResponse
POST /derivatives/transfer/cash/withdraw   → DerivativesCashWithdrawRequest / DerivativesCashWithdrawResponse
```

**Đã grep toàn bộ `lotte-bridge-main` cho `CashWithdraw`/`cash-withdraw` → 0 kết quả.** Không có `ILotteXxx` request/response model, không có method trong `AccountService.ts`/`BalanceService.ts`. Kết luận: đây là **spec đã viết trước (aspirational), chưa từng được nối với API Lotte thật** — hợp lý vì DRACC-039/040/041 chỉ mới được Lotte bổ sung 6/7/2026 (dữ liệu này trước đó không tồn tại).

### So khớp field — GET info stub ≈ DRACC-041 (rất khớp)

| Existing TradeX field (`DerivativesCashWithdrawInfoResponse`) | DRACC-041 Lotte field | Khớp? |
|---|---|---|
| `depositAmount` | `deposit` (số dư tiền) | ✅ |
| `totalBlockAmount` | `dpo_block` (tiền phong toả) | ✅ (gần đúng) |
| `waitingAmount` | `waiting_amt_for_withdraw` (tiền chờ rút) | ✅ |
| `withdrawableAmount` | `tot_out_psbamt` (**số tiền khả dụng rút** — field mấu chốt) | ✅ |
| `depositBlockAmount` | `dpo_coll_blf` (tiền phong toả ký quỹ) | ✅ (gần đúng) |
| `fillingLossBlockAmount` | `collect_ver_lisr` (tiền phong toả chờ nộp lỗ) | ✅ (tên rất khớp ý nghĩa "filling loss") |
| `maturityPaymentBlockAmount` | `dpo_block_ast` (tiền phong toả chờ thanh toán đáo hạn) | ✅ (tên rất khớp ý nghĩa "maturity payment") |
| *(không có field tương ứng)* | `dpo_fee_coll` (tiền phong toả phí QLTS ký quỹ) | ❌ thiếu — cần thêm field mới, đề xuất `managementFeeBlockAmount` |
| *(không có field tương ứng)* | `security_code` (mã CTCK) | Không cần expose ra FE (metadata nội bộ) |

→ **6/7 field khớp gần như 1-1** theo ngữ nghĩa. Rất có khả năng route `GET /derivatives/transfer/cash/withdraw` **chính là hợp đồng TradeX đã được thiết kế sẵn cho DRACC-041**, chỉ chờ Lotte cung cấp API thật (giờ đã có). **Đề xuất:** tái sử dụng route này, đổi tên response schema (giữ nguyên field names đã có, thêm `managementFeeBlockAmount`), và triển khai service trong `lotte-bridge`.

### So khớp field — POST execute stub vs DRACC-040 (KHÔNG khớp — cần đơn giản hoá)

| Existing TradeX field | DRACC-040 Lotte field thật | Nhận xét |
|---|---|---|
| `accountNumber` | `acnt_no` | ✅ |
| *(thiếu)* `subNumber` | `sub_no` (**Y** bắt buộc) | ❌ stub thiếu field bắt buộc |
| `amount` | `amount` | ✅ |
| `note` | `remark` | ✅ (đổi tên) |
| `bankAccountNumber` + `beneficiaryBankAccountNumber` + `beneficiaryBankAccountName` + `beneficiaryBankBranch` (4 field) | `bank_account` (**1 field duy nhất**) | ❌ stub có 4 field ngân hàng, Lotte chỉ nhận 1 field số tài khoản — **không có API "Get Bank List" nào trong bộ DRACC-039/040/041** để tra cứu tên/chi nhánh NH như VSD (DRACC-032). Đây là **open question** cho PM. |
| **Response:** `transactionDate`, `sequenceNumber`, `previousCashBalance`, `cashBalance`, `fee`, `receivedCash` | **Response thật:** `data_list: []` (rỗng khi thành công), chỉ có `error_code`/`error_desc`/`success` | ❌ Lotte KHÔNG trả về bất kỳ field nghiệp vụ nào — response stub hiện tại **không thể implement được**, phải bỏ hết, chỉ còn boolean success (giống pattern VSD Withdraw DRACC-009 / VSD Deposit DRACC-034). |

**Kết luận:** Route `POST /derivatives/transfer/cash/withdraw` có thể tái sử dụng **path**, nhưng **request/response schema phải viết lại hoàn toàn** theo field thật của DRACC-040 — không giữ được thiết kế cũ.

### DRACC-039 (Lịch sử rút tiền) — chưa có route nào

Không tìm thấy route `/derivatives/transfer/cash/withdraw/history` hay tương đương trong `vcsc/History.ts` (chỉ có trade/position/marginCall/settlement/closedPosition history — không có withdraw history). **Đây là endpoint hoàn toàn mới, cần tạo từ đầu.**

---

## Đề xuất TradeX Endpoint Mapping

| Operation | Method | Endpoint đề xuất | Lotte Endpoint | Service | Trạng thái route hiện tại |
|-----------|--------|-------------------|-----------------|---------|---------------------------|
| DRACC-039 History | GET | `/api/v1/derivatives/transfer/cash/withdraw/history` | `POST .../der/account/cw_dr_acnt_history` | `rest-proxy` → `lotte-bridge` | 🆕 Chưa tồn tại — tạo mới |
| DRACC-040 Execute | POST | `/api/v1/derivatives/transfer/cash/withdraw` | `POST .../der/account/dr-withdrawal` | `rest-proxy` → `lotte-bridge` | ♻️ Path đã stub sẵn (swagger-only) — viết lại request/response schema |
| DRACC-041 Balance | GET | `/api/v1/derivatives/transfer/cash/withdraw` (info) hoặc `/api/v1/derivatives/transfer/cash/withdraw/balance` (đề xuất tách rõ, xem Open Question) | `GET .../der/account/dr-avai-withdrawl` | `rest-proxy` → `lotte-bridge` | ♻️ Path đã stub sẵn, field mapping khớp ~86% |

Naming pattern đối chiếu VSD Transaction module (`/api/v1/derivatives/transfer/vsd/{balance,withdraw,history}`) — Rút tiền sub phái sinh nên đặt cùng cấp `transfer/cash/withdraw/*` (đã có tiền lệ do stub cũ), giữ nhất quán với style `transfer/vsd/*`.

**Lưu ý naming trùng lặp:** path `GET /derivatives/transfer/cash/withdraw` (info) hiện dùng chung path với `POST` (execute) — đây là REST convention hợp lệ (khác method), tương tự VSD (`GET /vsd/balance` riêng path, không trùng). Đề xuất **tách hẳn thành 2 path riêng** để tránh nhầm lẫn khi thêm `history`:
- `GET /api/v1/derivatives/transfer/cash/withdraw/balance` (DRACC-041)
- `POST /api/v1/derivatives/transfer/cash/withdraw` (DRACC-040)
- `GET /api/v1/derivatives/transfer/cash/withdraw/history` (DRACC-039)

---

## Cấu trúc Request (đề xuất TradeX field, đã tuân thủ "no Core names")

### DRACC-039 — History

| TradeX Field | Type | Bắt buộc | Auto-populated | Lotte Field |
|---|---|---|---|---|
| `accountNumber` | String | ✅ | | `acnt_no` |
| `subNumber` | String | ✅ | | `sub_no` |
| `status` | String enum: `PENDING`, `REJECTED`, `APPROVED` | ✅ | | `proc_tp` (`i`/`j`/`k`) — **đổi tên theo rule "không dùng mã Core"**, xem bảng mapping dưới |
| `fromDate` | String (yyyyMMdd) | ✅ | | `from_dt` |
| `toDate` | String (yyyyMMdd) | ✅ | | `to_dt` |
| `nextKey` | String | ❌ | | `next_key` (trang đầu để trống) |
| *(JWT)* `userId` | — | | ✅ Auto từ token | `hts_user_id` |

**Mapping `status` (TradeX → Lotte `proc_tp`)** — tuân thủ rule §4 `tradex-api-conventions.md` (không expose mã ký tự Lotte làm giá trị TradeX):

| TradeX `status` | Lotte `proc_tp` | Ý nghĩa |
|---|---|---|
| `PENDING` | `i` | Chưa duyệt |
| `REJECTED` | `j` | Từ chối |
| `APPROVED` | `k` | Duyệt |

### DRACC-040 — Execute Withdrawal

| TradeX Field | Type | Bắt buộc | Auto-populated | Lotte Field |
|---|---|---|---|---|
| `accountNumber` | String | ✅ | | `acnt_no` |
| `subNumber` | String | ✅ | | `sub_no` |
| `amount` | Number | ✅ | | `amount` (convert → String) |
| `note` | String | ✅ | | `remark` |
| `bankAccount` | String | ✅ | | `bank_account` |
| *(JWT)* `userId` | — | | ✅ Auto từ token | `hts_user_id` |

### DRACC-041 — Query Available Withdrawal Balance

| TradeX Field | Type | Bắt buộc | Auto-populated | Lotte Field |
|---|---|---|---|---|
| `accountNumber` | String | ✅ | | `acnt_no` |
| `subNumber` | String | ✅ | | `sub_no` |
| *(JWT)* `userId` | — | | ✅ Auto từ token | `hts_user_id` |

⚠️ **KHÔNG đưa `amount`/`note`(remark)/`bankAccount` vào request DRACC-041** — xem Open Question #1 bên dưới. Bảng field trong doc Lotte cho DRACC-041 (yêu cầu amount/remark/bank_account) **mâu thuẫn với Input Sample thực tế** (chỉ có acnt_no/sub_no/hts_user_id).

---

## Cấu trúc Response

### DRACC-039 — History (Query)

Theo pattern Query của `tradex-api-conventions.md` §Response Format Standards (`{ items: [...] }` — không dùng `success`/`code:"0000"`) và tương tự VSD History (`{ items, nextData }`):

```json
{
  "items": [
    {
      "transactionDate": "20260703",
      "transactionType": "...",
      "bankAccount": "0003.Ngân hàng ABC",
      "transactionCode": "...",
      "transactionName": "...",
      "content": "...",
      "amount": 5000000,
      "transactionSequenceNumber": "...",
      "approvalDate": "20260704",
      "sequenceNumber": "...",
      "status": "APPROVED",
      "receivedTime": "...",
      "receivingBankAccount": "...",
      "feeAmount": 5500
    }
  ],
  "nextData": "..."
}
```

**Response field mapping (Lotte `DataResponse` → TradeX):**

| Lotte Field | TradeX Field | Transform |
|---|---|---|
| `trdDt` | `transactionDate` | Direct/chuẩn hoá yyyyMMdd |
| `trdTp` | `transactionType` | Direct — **cần bảng giá trị từ HuongLT/VanND** (chưa có enum rõ trong doc) |
| `bankAcc` | `bankAccount` | Direct (giữ format "code.name" hoặc tách 2 field — xem Open Question #3) |
| `rmrkCd` | `transactionCode` | Direct |
| `rmrkNm` | `transactionName` | Direct |
| `cnte` | `content` | Direct |
| `trdAmt` | `amount` | Parse String → Number |
| `trdSeqNo` | `transactionSequenceNumber` | Direct |
| `cnfmDt` | `approvalDate` | Chuẩn hoá yyyyMMdd |
| `seqNo` | `sequenceNumber` | Direct |
| `cnclYn` | `status` | Cần mapping giá trị TradeX (SCREAMING_SNAKE_CASE) — **giá trị gốc `cnclYn` chưa rõ enum, open question** |
| `bookTime` | `receivedTime` | Direct |
| `bankAcntNo` | `receivingBankAccount` | Direct |
| `feeAmt` | `feeAmount` | Parse String → Number |
| `next` | *(root)* `nextData` | Pagination |

### DRACC-040 — Execute Withdrawal (Mutation)

**Quyết định:** theo pattern **VSD Withdraw (DRACC-009)** đã có tiền lệ trong VSD_Transaction_API_Spec.md §6.3/§7.3 — Lotte KHÔNG trả object nghiệp vụ (`data_list: []`), chỉ trả `error_code`/`error_desc`/`success`. TradeX response phải là **boolean thuần**, KHÔNG dùng pattern order-v2's `{id}` (đó là tradex-native pattern, domain này là lotte-integrated):

```json
{ "success": true }
```

Response logic (giống VSD): chỉ trả `{success: true}` khi Lotte `error_code === "0000" && success === true`; ngược lại throw lỗi 422.

**Không dùng** rich schema `transactionDate/sequenceNumber/previousCashBalance/cashBalance/fee/receivedCash` của stub cũ trong `rest-proxy-main` — Lotte không cung cấp field nào trong số đó cho DRACC-040.

### DRACC-041 — Query Available Balance

Query trả object đơn (không phải array ở tầng TradeX, vì luôn 1 dòng theo account/sub):

```json
{
  "availableBalance": 74628604,
  "blockAmount": 0,
  "waitingWithdrawAmount": 0,
  "withdrawableAmount": 41033834,
  "marginBlockAmount": 0,
  "fillingLossBlockAmount": 0,
  "maturityPaymentBlockAmount": 0,
  "managementFeeBlockAmount": 0
}
```

**Response field mapping:**

| Lotte Field | TradeX Field | Transform |
|---|---|---|
| `security_code` | *(không trả ra FE — nội bộ)* | — |
| `deposit` | `availableBalance` | Parse String → Number |
| `dpo_block` | `blockAmount` | Parse String → Number |
| `waiting_amt_for_withdraw` | `waitingWithdrawAmount` | Parse String → Number |
| `tot_out_psbamt` | `withdrawableAmount` | Parse String → Number — **field quan trọng nhất, FE dùng để giới hạn input amount ở DRACC-040** |
| `collect_ver_lisr` | `fillingLossBlockAmount` | Parse String → Number |
| `dpo_coll_blf` | `marginBlockAmount` | Parse String → Number |
| `dpo_block_ast` | `maturityPaymentBlockAmount` | Parse String → Number |
| `dpo_fee_coll` | `managementFeeBlockAmount` | Parse String → Number |

**Ghi chú:** field name TradeX đề xuất ở trên **giữ tương thích tối đa** với schema `DerivativesCashWithdrawInfoResponse` đã có sẵn trong `rest-proxy-main` (đổi `depositAmount→availableBalance`, `totalBlockAmount→blockAmount`, `waitingAmount→waitingWithdrawAmount`, `withdrawableAmount` giữ nguyên, `depositBlockAmount→marginBlockAmount`, giữ nguyên `fillingLossBlockAmount`/`maturityPaymentBlockAmount`, thêm mới `managementFeeBlockAmount`) — team dev có thể quyết định giữ tên cũ hoàn toàn nếu muốn zero-diff với stub hiện có; creator phase nên chốt 1 bộ tên duy nhất.

### Lỗi (400/401/422)

Theo chuẩn chung `tradex-api-conventions.md`:
- 400 `INVALID_PARAMETER` — thiếu field bắt buộc (TradeX tự validate).
- 401/403 — token.
- 422 `{OPERATION}_{LOTTE_CODE}` pass-through — ví dụ `DERIVATIVES_CASH_WITHDRAW_1005`, `DERIVATIVES_CASH_WITHDRAW_HISTORY_1005`, `DERIVATIVES_CASH_WITHDRAW_BALANCE_1005` (theo pattern operation-prefix đã dùng ở VSD: `DERIVATIVES_VSD_{OPERATION}_{code}`).

---

## Business Rules

1. `accountNumber` + `subNumber` bắt buộc, phải thuộc user đã login (JWT) — TradeX validate light, Lotte/Core validate business rule đầy đủ (số dư, hạn mức...).
2. `amount` phải > 0 (TradeX validate) — KHÔNG validate `amount ≤ withdrawableAmount` tại TradeX (Core validate, theo Validation Strategy philosophy).
3. FE nên gọi DRACC-041 (Query Balance) **trước** khi cho user nhập `amount` ở DRACC-040, để hiển thị `withdrawableAmount` — tương tự flow VSD (Balance → Withdraw), nhưng ở đây KHÔNG có bước "Calculate Fee" trung gian (không giống Deposit VSD 2-step; DRACC-040 không có fee input field).
4. `status` filter ở DRACC-039 dùng 3 giá trị `PENDING`/`REJECTED`/`APPROVED` — map sang Lotte `proc_tp` (`i`/`j`/`k`).
5. `bankAccount` ở DRACC-040 — KHÔNG có API "Get Bank List" nào trong bộ 3 API này (khác VSD có DRACC-032). Cần xác nhận nguồn `bankAccount` — có thể FE tự nhập tay, hoặc tái sử dụng danh sách bank đã đăng ký từ 1 API khác (equity `/equity/withdraw/banks`? hay 1 API derivatives account/profile riêng?). **→ Open Question #1.**

---

## Kiểm tra Convention

- **URL naming:** ✅ OK — camelCase, theo pattern `transfer/cash/withdraw/{action}` nhất quán với `transfer/vsd/{action}` đã có.
- **DTO naming:** ⚠️ VẤN ĐỀ — schema cũ `DerivativesCashWithdrawRequest`/`Response` trong `rest-proxy-main` (stub) không khớp field thật DRACC-040; cần viết lại (xem phần "So khớp field" trên). Đề xuất tên mới: `DerivativesCashWithdrawExecuteRequest/Response`, `DerivativesCashWithdrawBalanceResponse`, `DerivativesCashWithdrawHistoryRequest/Response` — hoặc giữ tên cũ nhưng thay đổi field bên trong (creator phase quyết định).
- **Response format:** ✅ OK theo hướng dùng pattern **lotte-integrated + boolean success** (giống VSD Withdraw DRACC-009/DRACC-034) cho DRACC-040 — **không** dùng pattern order-v2's tradex-native `{id}` (domain này gọi thẳng Lotte qua lotte-bridge, không lưu DB TradeX trước, nên không có khái niệm `id` nội bộ). DRACC-039/041 dùng pattern Query chuẩn (`items[]`/object đơn), không có `success`/`code:"0000"` trong body.
- **Integration type label:** Phải khai báo rõ **"Lotte-integrated (via lotte-bridge → Core Lotte)"** ở đầu spec, theo rule §Integration Type của `tradex-api-conventions.md`.
- **No Core names rule:** ⚠️ Cần đổi `proc_tp` (i/j/k) → `status` (PENDING/REJECTED/APPROVED) — đã áp dụng ở trên. Tương tự cần rà lại `trdTp`/`cnclYn` khi có đủ thông tin enum giá trị thật (hiện doc Lotte chưa liệt kê hết giá trị của 2 field này).

---

## Open Questions / Discrepancies — cần PM xác nhận với HuongLT/VanND

1. **[Quan trọng] DRACC-041 request field discrepancy:** Bảng field trong doc liệt kê `amount`, `remark`, `bank_account` là **Required (Y)**, nhưng **Input Sample chỉ có** `acnt_no`, `sub_no`, `hts_user_id`. Đây rất có thể là lỗi copy-paste từ DRACC-040 (bảng field bị paste nhầm). **Đã lấy Input Sample làm ground truth** cho phần Request Structure ở trên. Cần VanND/HuongLT xác nhận lại chính thức trước khi PM finalize spec — nếu `amount`/`remark`/`bank_account` thực sự cần cho API tra cứu số dư (bất thường về mặt nghiệp vụ — 1 API GET/query thường không cần amount/bank_account) thì phải sửa lại toàn bộ Request mapping của DRACC-041.

2. **Nguồn `bankAccount` cho DRACC-040:** Không có API "Get Bank List" nào trong bộ DRACC-039/040/041 (khác VSD có DRACC-032 riêng). Cần hỏi: (a) FE tự nhập tay số tài khoản NH mỗi lần rút, hay (b) có sẵn danh sách bank đã đăng ký ở 1 API khác (derivatives account profile, hoặc share với equity `/equity/withdraw/banks`)? Ảnh hưởng trực tiếp UX và tới quyết định có cần thêm 1 GET banks endpoint mới không.

3. **`bankAcc` format ở response DRACC-039 History:** Field ví dụ `"0003.Ngân hàng ABC"` — gộp mã + tên NH trong 1 string (giống style Lotte "code.name" đã thấy ở VSD `bankTypeName`/`os_bank_type_nm`). Hỏi: TradeX nên giữ nguyên 1 string, hay tách thành `bankCode` + `bankName` 2 field riêng (dễ dùng cho FE hơn, và đúng convention field rõ nghĩa)?

4. **Enum giá trị `trdTp` (loại giao dịch) và `cnclYn` (trạng thái duyệt/hủy) ở DRACC-039:** Doc Lotte chưa liệt kê đầy đủ tập giá trị hợp lệ của 2 field này (chỉ có tên field + mô tả chung). Cần bảng mapping đầy đủ (giống bảng `statusBOS` trong VSD spec) để định nghĩa enum TradeX tương ứng — không thể tạm map "Direct" cho 2 field này khi viết spec chính thức.

5. **Reuse existing stub path hay tạo path mới hoàn toàn?** `rest-proxy-main` đã có sẵn `GET/POST /api/v1/derivatives/transfer/cash/withdraw` (vcsc + kis) nhưng schema không backed bởi Lotte thật. PM cần quyết định: (a) tái sử dụng path này, viết lại schema theo DRACC-039/040/041 thật (khuyến nghị — tận dụng route đã đặt sẵn, tránh 2 route "withdraw" trùng ý nghĩa), hoặc (b) tạo hẳn namespace mới (vd. `derivatives/cashWithdrawal/...`) để tránh nhầm với stub cũ nếu stub đó thực ra dành cho 1 tính năng khác đã bị bỏ quên. Khuyến nghị (a) vì field GET-info stub khớp DRACC-041 rất cao (6/7 field), khó là trùng hợp.

6. **Fee ở DRACC-040:** Không thấy field `fee`/phí trong request/response thật của DRACC-040 (không giống VSD Deposit cần gọi Calculate Fee trước — DRACC-033). Cần hỏi: rút tiền sub phái sinh có tính phí không? Nếu có, phí lấy ở đâu (trừ ngầm phía Lotte, không hiện trong API)?

---

## Phát hiện mới (đáng lưu vào Knowledge)

- **Route "vaporware" đã tồn tại trong `rest-proxy-main`** (`/derivatives/transfer/cash/withdraw`, vcsc + kis) mà không có `lotte-bridge` implementation — đây là spec-only stub, chưa từng chạy được. Đáng ghi chú lại trong `Knowledge/TradeX/System/` để tránh nhầm lẫn "endpoint đã tồn tại = đã hoạt động" trong các lần rà soát sau. Đề xuất PM quyết định việc lưu này ở bước creator/sau khi spec DRACC-039/040/041 hoàn thiện, để gộp thành 1 ghi chú duy nhất "derivatives cash withdrawal (sub → external bank)".

---

**Document Status:** 🔄 In Progress | For: Creator phase (viết API Specification chính thức) | Next Steps: PM xác nhận Open Questions #1–#6 với HuongLT/VanND trước khi creator phase chốt field cuối cùng; sau đó viết `Derivatives/Planning documentation/Cash transaction/{Category}/Issues|Specifications`.
