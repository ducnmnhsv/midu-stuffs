# External Withdrawal - Derivatives Cash Transaction

> **Module:** External Withdrawal (Rút tiền sub phái sinh về ngân hàng)
> **Category:** Cash Transaction
> **Project:** TradeX Derivatives Integration
> **Last Updated:** July 9, 2026
> **Status:** 🔄 Planning — pending PM confirmation of Open Questions

---

## Overview

External Withdrawal module cho phép nhà đầu tư rút tiền mặt từ sub phái sinh **về tài khoản ngân hàng ngoài** — khác với VSD Transaction (nộp/rút margin NHSV ↔ VSD) và Internal Transfer (chuyển tiền giữa các sub cùng account).

### Key Features

| Feature | Lotte API | Status | Description |
|---------|-----------|--------|--------------|
| Query Withdrawal History | DRACC-039 | 🔄 Draft spec | Tra cứu lịch sử rút tiền |
| Execute Withdrawal | DRACC-040 | 🔄 Draft spec | Thực hiện rút tiền về NH ngoài |
| Query Available Balance | DRACC-041 | 🔄 Draft spec | Tra cứu số dư khả dụng rút |

---

## Quick Access

| Document | Description | Audience |
|----------|-------------|----------|
| [Sub_Account_Withdrawal_API_Spec.md](./Sub_Account_Withdrawal_API_Spec.md) | Complete API specification for DRACC-039/040/041 | BA, Developers |

---

## API Summary

### DRACC-041: Query Available Balance

**TradeX Endpoint:** `GET /api/v1/derivatives/transfer/cash/withdraw/balance`

**Purpose:** Tra cứu số dư khả dụng rút trước khi thực hiện lệnh rút tiền

**Response:**
```json
{
  "availableBalance": 74628604,
  "withdrawableAmount": 41033834
}
```

---

### DRACC-040: Execute Withdrawal

**TradeX Endpoint:** `POST /api/v1/derivatives/transfer/cash/withdraw`

**Purpose:** Rút tiền từ sub phái sinh về tài khoản ngân hàng ngoài

**Response:**
```json
{
  "success": true
}
```

**Note:** Trả boolean thuần, không có transaction detail — Lotte không trả field nghiệp vụ nào.

---

### DRACC-039: Query Withdrawal History

**TradeX Endpoint:** `GET /api/v1/derivatives/transfer/cash/withdraw/history`

**Purpose:** Tra cứu lịch sử các lệnh rút tiền, filter theo trạng thái duyệt

**Filters:** `status` (`PENDING`/`REJECTED`/`APPROVED`), `fromDate`/`toDate`

---

## Key Concepts

### Khác biệt với VSD Transaction & Internal Transfer

| Đặc điểm | External Withdrawal | VSD Transaction | Internal Transfer |
|----------|---------------------|------------------|---------------------|
| Đích tiền | Tài khoản ngân hàng ngoài | VSD (Vietnam Securities Depository) | Sub khác cùng account |
| Lotte APIs | DRACC-039/040/041 | DRACC-009/021/032/033/034 | DRACC-019/020 |
| Bank list lookup | ❌ Không có (Open Question) | ✅ DRACC-032 | N/A |
| Fee calculation | ❌ Không có API riêng | ✅ DRACC-033 (2-step deposit) | N/A |

### Status Enum (DRACC-039 filter)

| TradeX `status` | Lotte `proc_tp` | Ý nghĩa |
|---|---|---|
| `PENDING` | `i` | Chưa duyệt |
| `REJECTED` | `j` | Từ chối |
| `APPROVED` | `k` | Duyệt |

---

## Use Cases

### UC-1: Rút tiền về ngân hàng

**Scenario:** User rút 5,000,000 VND từ sub phái sinh về tài khoản ngân hàng đã đăng ký

**Flow:**

1. **Check Available Balance** (DRACC-041)
   ```json
   Request: { accountNumber: "0001234567", subNumber: "80" }
   Response: { availableBalance: 74628604, withdrawableAmount: 41033834 }
   ```

2. **Execute Withdrawal** (DRACC-040)
   ```json
   Request: {
     accountNumber: "0001234567",
     subNumber: "80",
     amount: 5000000,
     note: "Rút tiền phái sinh",
     bankAccount: "0003xxxxxxx"
   }
   Response: { success: true }
   ```

3. **Check History** (DRACC-039 — optional, để theo dõi trạng thái duyệt)
   ```json
   Request: {
     accountNumber: "0001234567",
     subNumber: "80",
     status: "PENDING",
     fromDate: "20260701",
     toDate: "20260709"
   }
   ```

**Note:** Nguồn `bankAccount` (nhập tay hay từ danh sách đã đăng ký) **chưa xác định** — xem Open Questions trong API Spec.

---

## Business Rules

1. `accountNumber` + `subNumber` bắt buộc, phải thuộc user đã login (JWT).
2. `amount` phải > 0 (TradeX validate); không validate `amount ≤ withdrawableAmount` tại TradeX (Core validate).
3. Không có bước "Calculate Fee" trung gian (khác VSD Deposit).
4. `status` filter ở DRACC-039 dùng 3 giá trị `PENDING`/`REJECTED`/`APPROVED`.

---

## Implementation Status

### Current Phase: Draft — pending PM confirmation

**Completed:**
- ✅ API spec draft (DRACC-039/040/041)
- ✅ Field mapping documentation
- ✅ Business rules definition (draft)

**Blocked on:**
- 🔄 PM confirmation of 6 Open Questions with HuongLT/VanND (xem API Spec §10)

**Next Steps:**
1. PM xác nhận Open Questions với HuongLT/VanND
2. Validator phase — check convention
3. Finalize spec → move to `Derivatives/Planning documentation/Cash transaction/External withdrawal/`
4. Implement bank list source decision (Open Question #2)
5. Implement DRACC-039/040/041 in `rest-proxy` + `lotte-bridge`

---

## Related Modules

| Module | Location | Relationship |
|--------|----------|----------------|
| VSD Transaction | `../VSD transaction/` | Closest pattern reference (Lotte-integrated cash movement, 3-API trio) |
| Internal Transfer | `../Internal transfer/` | Related cash transaction feature (khác đích tiền) |

---

## Reference Documents

- **Lotte API Doc:** `derivatives_api_doc.txt` §3.2 GIAO DỊCH TIỀN PHÁI SINH, lines 4079–4786
- **Analyst Findings:** `_workspace/01_analyst_findings.md`
- **TradeX API Conventions:** `@Knowledge/TradeX/API Standards/tradex-api-conventions.md`
- **VSD Transaction (Reference):** `../VSD transaction/VSD_Transaction_API_Spec.md`

---

## Open Questions

See full list in [Sub_Account_Withdrawal_API_Spec.md](./Sub_Account_Withdrawal_API_Spec.md) §10. Summary:

1. DRACC-041 request field discrepancy (amount/remark/bank_account vs Input Sample)
2. Nguồn `bankAccount` cho DRACC-040 (nhập tay hay danh sách đã đăng ký?)
3. Format `bankAcc` ở History response (1 string hay 2 field?)
4. Enum đầy đủ của `trdTp`/`cnclYn` (DRACC-039)
5. Reuse stub path `rest-proxy-main` hay tạo namespace mới?
6. Có phí rút tiền không? Nếu có, lấy ở đâu?

---

**Prepared By:** BA Team (Creator phase)
**Document Version:** 0.1 (Draft)
**Last Review:** July 9, 2026
