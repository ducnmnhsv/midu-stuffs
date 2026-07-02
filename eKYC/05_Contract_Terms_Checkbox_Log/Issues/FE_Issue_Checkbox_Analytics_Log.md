# [FE] eKYC — Ghi nhận timestamp đồng ý điều khoản và gửi lên BE

**Feature:** eKYC / 05_Contract_Terms_Checkbox_Log
**Type:** FE (App) — Compliance
**Priority:** Medium
**Screen:** EKYCConfirmPolicyScreen — "Xác nhận điều khoản hợp đồng"
**Mục đích:** Lưu thời điểm khách hàng đồng ý điều khoản vào `ekyc_attempt_log` (audit trail)
**API dùng:** `POST /ekycs/attempt-log` (reuse endpoint đã có trong sub-feature 01 — không thêm API mới)

---

## Mô tả

Khi khách hàng tick checkbox và bấm "Tiếp theo", App gọi thêm một lần `POST /ekycs/attempt-log` với payload chỉ chứa `identifierId` + `termsAgreedAt`. BE cập nhật `terms_agreed_at` trên attempt record hiện có của người dùng này.

Không thay đổi gì trên `POST /lotte/ekycs` (API tạo tài khoản).

---

## Hiện trạng code

### EKYCConfirmPolicyScreen/index.tsx — dòng 203–205

```typescript
const toggleAgree = () => {
  setAgreePolicy(!agreePolicy);  // không ghi timestamp
};
```

### OnPressNextInConfirmPolicyScreen.ts — dòng 183

```typescript
// Saga hiện chỉ gọi 1 API:
yield call(query, APIList.ekycCreateAccount, params);
```

`ekycId` (số CCCD) đã sẵn có trong Redux store tại dòng 38: `const ekycId: string = yield select(...)`.

---

## Yêu cầu thay đổi

### File 1: `src/interfaces/ekyc.ts`

Thêm field `termsAgreedAt` vào `IEKYCConfirmPolicy`:

```typescript
export interface IEKYCConfirmPolicy {
  // ... các field hiện có giữ nguyên ...
  time_spent: number;
  termsAgreedAt: string;  // ISO 8601 UTC — "2026-07-02T07:00:00.000Z"
}
```

---

### File 2: `src/screens/EKYCConfirmPolicyScreen/index.tsx`

**Bước 2a — Thêm state lưu timestamp:**

```typescript
// Bên cạnh: const [agreePolicy, setAgreePolicy] = useState(false);
const [termsAgreedAt, setTermsAgreedAt] = useState<string | null>(null);
```

**Bước 2b — Cập nhật `toggleAgree`:**

```typescript
const toggleAgree = () => {
  const newValue = !agreePolicy;
  setAgreePolicy(newValue);
  setTermsAgreedAt(newValue ? new Date().toISOString() : null);
};
```

**Bước 2c — Truyền vào dispatch:**

```typescript
const onPressNext = () => {
  dispatch(onPressNextInConfirmPolicyScreen({
    payload: {
      ...props.route.params,
      time_spent: time_on_screen,
      termsAgreedAt: termsAgreedAt!,  // luôn có vì nút disabled khi chưa tick
    }
  }));
};
```

---

### File 3: `src/reduxs/sagas/EKYC/OnPressNextInConfirmPolicyScreen.ts`

Thêm call `POST /ekycs/attempt-log` ngay **trước** `ekycCreateAccount` (dòng ~183):

```typescript
// ── Ghi nhận thời điểm đồng ý điều khoản (compliance) ──
yield call(query, APIList.ekycAttemptLog, {
  identifierId: ekycId,
  termsAgreedAt: request.payload.termsAgreedAt,
});

// ── Tạo tài khoản (hiện có — không thay đổi) ──
yield call(query, APIList.ekycCreateAccount, params);
```

> `APIList.ekycAttemptLog` là endpoint mới `POST /ekycs/attempt-log` đang được build trong sub-feature 01. FE cần đợi BE hoàn thành endpoint này trước khi tích hợp.

---

## Acceptance Criteria

- [ ] Timestamp ghi đúng tại thời điểm user **tick** checkbox (không phải khi bấm "Tiếp theo")
- [ ] Nếu user tick → bỏ tick → tick lại: timestamp cập nhật theo lần tick **cuối cùng**
- [ ] Request `POST /ekycs/attempt-log` được gửi trước `POST /lotte/ekycs`
- [ ] Payload chỉ chứa `identifierId` + `termsAgreedAt` (không gửi lại dữ liệu VNPT)
- [ ] Verify qua Charles / mitmproxy: 2 request liên tiếp, đúng thứ tự
- [ ] Không ảnh hưởng đến flow hiện tại khi endpoint chưa live (error handling: silent fail hoặc retry)

---

## Files cần thay đổi

| File | Thay đổi |
|------|----------|
| `src/interfaces/ekyc.ts` | Thêm `termsAgreedAt: string` vào `IEKYCConfirmPolicy` |
| `src/screens/EKYCConfirmPolicyScreen/index.tsx` | Thêm state `termsAgreedAt`, cập nhật `toggleAgree` và `onPressNext` |
| `src/reduxs/sagas/EKYC/OnPressNextInConfirmPolicyScreen.ts` | Thêm call `POST /ekycs/attempt-log` trước `ekycCreateAccount` |

## Không thay đổi

- `POST /lotte/ekycs` params — không đụng đến
- `e_kyc` table — không đụng đến
- Các analytics GA4 hiện có

---

## Dependency

- **Blocked by:** Sub-feature 01 BE — `POST /ekycs/attempt-log` phải live trước
- **BE Issue:** [BE_Issue_Checkbox_Consent_Storage.md](BE_Issue_Checkbox_Consent_Storage.md)

---

Document Status: 📋 Ready for Dev | For: FE Developer | Next Steps: Implement sau khi sub-feature 01 BE deploy `POST /ekycs/attempt-log`
