# Smart OTP Login Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tích hợp Smart OTP vào luồng đăng nhập NHSV Pro, backward-compatible với user chưa kích hoạt, xử lý case cài lại app.

**Architecture:** BE (aaa-main + lotte-rest-bridge) thêm `sotpStatus`/`otpType` vào login Step 1 response và routing Smart OTP vào verifyOTP. FE (nhsv-mts-rn) đọc `otpType` từ server, check local `sotpKey`, branch UI theo 3 cases.

**Tech Stack:** TypeScript/Node.js (aaa-main, lotte-rest-bridge), React Native + Redux Saga (nhsv-mts-rn)

**Design spec:** [`docs/superpowers/specs/2026-06-04-smart-otp-login-design.md`](../specs/2026-06-04-smart-otp-login-design.md)

> ⚠️ **Scope note:** Plan này gồm 2 subsystems độc lập (BE + FE). Có thể implement song song sau Task 2.

---

## File Structure

### BE — `aaa-main`

| File | Thay đổi |
|---|---|
| `src/models/IServiceLoginRes.ts` | Thêm `sotpStatus?: string` |
| `src/models/response/ILoginRes.ts` | Thêm `sotpStatus?: string`, `otpType?: string` |
| `src/models/request/IVerifyOtpReq.ts` | Thêm `otpType?: string` |
| `src/services/authen/loginPasswordOtp.ts` | Expose `sotpStatus` + derive `otpType` trong response |
| `src/services/authen/loginOtp.ts` | Same expose + Smart OTP routing trong `loginVerifyOtp` |
| `src/services/VerifyOtp.ts` | Smart OTP routing trong `verifyOtp` (lotte-rest-bridge path) |
| `src/constants/OtpTypeUriMap.ts` | Thêm entry `nhsv` với `SMART_OTP` URI |

### BE — `lotte-rest-bridge`

| File | Thay đổi |
|---|---|
| `src/models/ILoginResponse.ts` (hoặc tương đương) | Thêm `sotpStatus?: string` map từ `sotp_stat` |
| `src/services/LoginService.ts` (hoặc tương đương) | Extract `sotp_stat` → `sotpStatus` từ Lotte response |

> ℹ️ lotte-rest-bridge source không có đầy đủ trong knowledge base. Task 2 mô tả pattern cần làm — dev cần xác định file thực tế trong repo.

### FE — `nhsv-mts-rn`

| File | Thay đổi |
|---|---|
| `src/config/api.ts` | Không đổi (endpoints đã có) |
| `src/interfaces/login.ts` (hoặc tương đương) | Thêm `sotpStatus`, `otpType` vào `LoginStep1Response` |
| `src/utils/smartOtpStorage.ts` | **Tạo mới** — get/set/clear `sotpKey` từ secure storage |
| `src/reduxs/sagas/Login/LoginSaga.ts` (hoặc tương đương) | Branch logic: không gọi `notifyMobileOtpNhsv` khi `SMART_OTP` |
| `src/screens/Login/OtpScreen.tsx` (hoặc tương đương) | Branch UI: Case A / Case B / Case C |
| `src/screens/Login/SmartOtpReinstallScreen.tsx` | **Tạo mới** — Special screen Case C |
| `src/hooks/useSmartOtpLoginBranch.ts` | **Tạo mới** — Decision logic hook |
| `src/screens/Home/HomeScreen.tsx` (hoặc tương đương) | Thêm Phase 1 soft gate banner |

---

## BE Tasks

### Task 1: Thêm `sotpStatus` vào BE models

**Files:**
- Modify: `aaa-main/src/models/IServiceLoginRes.ts`
- Modify: `aaa-main/src/models/response/ILoginRes.ts`

- [ ] **Step 1.1 — Thêm `sotpStatus` vào `IServiceLoginRes`**

File: `aaa-main/src/models/IServiceLoginRes.ts`

```typescript
export default interface IServiceLoginRes {
  conId?: IConnectionIdentifier;
  userInfo: IUserInfo;
  otpIndex?: string | number;
  otpValue?: string;
  sessionId?: string;
  userData: IUserData;
  sotpStatus?: string;   // ← thêm: "Y" | "N" từ Lotte sotp_stat
}
```

- [ ] **Step 1.2 — Thêm `sotpStatus` + `otpType` vào `ILoginRes`**

File: `aaa-main/src/models/response/ILoginRes.ts`

```typescript
export default interface ILoginRes {
  accessToken: string;
  refreshToken: string;
  userInfo?: IUserInfo;
  otpIndex?: string;
  userLevel?: string;
  accExpiredTime?: number;
  refExpiredTime?: number;
  registerMobileOtp?: boolean;
  sotpStatus?: string;   // ← thêm: "Y" | "N"
  otpType?: string;      // ← thêm: "SMART_OTP" | "SMS_OTP"
}
```

- [ ] **Step 1.3 — Commit**

```bash
git add src/models/IServiceLoginRes.ts src/models/response/ILoginRes.ts
git commit -m "feat(aaa): add sotpStatus and otpType fields to login response models"
```

---

### Task 2: lotte-rest-bridge — Pass `sotp_stat` qua response

**Files:**
- Modify: file xử lý Lotte login response trong `lotte-rest-bridge` (xác định chính xác trong repo)

- [ ] **Step 2.1 — Tìm file xử lý Lotte login response**

Trong repo `lotte-rest-bridge`, tìm nơi gọi Lotte login API và map response:
```bash
grep -r "sotp_stat\|loginWithOtp\|post.*login" src/ --include="*.ts" | head -20
```

- [ ] **Step 2.2 — Extract `sotp_stat` → `sotpStatus`**

Trong handler map response từ Lotte, thêm:
```typescript
// Lotte trả về: { sotp_stat: "Y" | "N", sotp_sec: "...", ... }
const loginResponse: IServiceLoginRes = {
  userInfo: mapUserInfo(lotteResponse),
  userData: mapUserData(lotteResponse),
  otpIndex: lotteResponse.otp_index,
  otpValue: lotteResponse.otp_value,
  sotpStatus: lotteResponse.sotp_stat ?? "N",  // ← thêm dòng này
};
return loginResponse;
```

> ⚠️ Enum Lotte cho `sotp_stat` cần confirm (xem OQ-2 trong design spec). Assume `"Y"` = đã đăng ký, `"N"` = chưa.

- [ ] **Step 2.3 — Commit**

```bash
git add src/  # thêm đúng file đã sửa
git commit -m "feat(lotte-bridge): pass sotp_stat as sotpStatus in login response"
```

---

### Task 3: aaa-main — Expose `sotpStatus` + `otpType` trong login Step 1 response

**Files:**
- Modify: `aaa-main/src/services/authen/loginPasswordOtp.ts`
- Modify: `aaa-main/src/services/authen/loginOtp.ts`

- [ ] **Step 3.1 — Thêm helper derive `otpType`**

Thêm function vào `aaa-main/src/services/authen/loginPasswordOtp.ts` (trước `loginPasswordWithOtp`):

```typescript
function deriveOtpType(sotpStatus: string | undefined): string {
  return sotpStatus === 'Y' ? 'SMART_OTP' : 'SMS_OTP';
}
```

- [ ] **Step 3.2 — Expose trong `loginPasswordOtp.ts` `executeLoginWithOtp`**

Trong `loginPasswordOtp.ts`, tìm callback của `createToken` (dòng ~138), sửa:

```typescript
return createToken(
  { /* ... params không đổi ... */ },
  null,
  (tokenResult: ITokenResult) => ({
    accessToken: tokenResult.accessToken,
    refreshToken: tokenResult.refreshToken,
    otpIndex: loginResult.otpIndex as string,
    userLevel: loginResult.userData.userLevel,
    registerMobileOtp: serviceUser.registerMobileOtp.get(),
    accExpiredTime: tokenResult.accExpiredTime,
    refExpiredTime: tokenResult.refExpiredTime,
    sotpStatus: loginResult.sotpStatus,                           // ← thêm
    otpType: deriveOtpType(loginResult.sotpStatus),              // ← thêm
  })
);
```

- [ ] **Step 3.3 — Expose trong `loginOtp.ts` `executeLoginWithOtp`**

Trong `loginOtp.ts`, tìm callback của `createToken` (dòng ~180+), thêm tương tự:

```typescript
(tokenResult: ITokenResult) => ({
  accessToken: tokenResult.accessToken,
  refreshToken: tokenResult.refreshToken,
  otpIndex: loginResult.otpIndex as string,
  userLevel: loginResult.userData?.userLevel,
  accExpiredTime: tokenResult.accExpiredTime,
  refExpiredTime: tokenResult.refExpiredTime,
  sotpStatus: loginResult.sotpStatus,                           // ← thêm
  otpType: deriveOtpType(loginResult.sotpStatus),              // ← thêm
})
```

> `deriveOtpType` có thể extract ra `aaa-main/src/utils/otpUtils.ts` để dùng chung.

- [ ] **Step 3.4 — Manual test với Postman**

```
POST /rest/api/v1/login
body: { grant_type: "password_otp", username: "<test_user>", password: "...", client_id: "...", client_secret: "..." }

Expected response:
{
  "accessToken": "temp_xxx",
  "sotpStatus": "Y" | "N",   ← phải có
  "otpType": "SMART_OTP" | "SMS_OTP"  ← phải có
}
```

- [ ] **Step 3.5 — Commit**

```bash
git add src/services/authen/loginPasswordOtp.ts src/services/authen/loginOtp.ts
git commit -m "feat(aaa): expose sotpStatus and otpType in login Step 1 response"
```

---

### Task 4: aaa-main — Smart OTP routing trong verifyOTP

**Files:**
- Modify: `aaa-main/src/models/request/IVerifyOtpReq.ts`
- Modify: `aaa-main/src/constants/OtpTypeUriMap.ts`
- Modify: `aaa-main/src/services/authen/loginOtp.ts` (function `loginVerifyOtp`)
- Modify: `aaa-main/src/services/VerifyOtp.ts` (function `verifyOtp`)

- [ ] **Step 4.1 — Thêm `otpType` vào `IVerifyOtpReq`**

File: `aaa-main/src/models/request/IVerifyOtpReq.ts`

```typescript
import { Models } from "tradex-common";

export default interface IVerifyOtpReq extends Models.IDataRequest {
  otp_value?: string;
  mobile_otp?: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
  otpType?: string;   // ← thêm: "SMART_OTP" | "SMS_OTP", default SMS_OTP
}
```

- [ ] **Step 4.2 — Thêm `nhsv` entry vào `OtpTypeUriMap`**

File: `aaa-main/src/constants/OtpTypeUriMap.ts`

```typescript
export const OTP_TYPE_URI_MAP = {
  mas: { /* không đổi */ },
  kis: { /* không đổi */ },
  nhsv: {
    SMART_OTP: '/api/v1/smartOtp/verify',   // ← URI của SmartOTP verify service
    TOPIC: {
      SMART_OTP: 'paave-real-trading',       // ← Kafka topic cho SmartOTP verify
    }
  }
};
```

> ℹ️ Confirm `TOPIC` chính xác với BE lead — cần biết SmartOTP verify được xử lý bởi service/topic nào.

- [ ] **Step 4.3 — Routing Smart OTP trong `loginVerifyOtp` (loginOtp.ts)**

Trong `loginOtp.ts`, function `loginVerifyOtp`, sau khi lấy `loginMethod`, thêm branch:

```typescript
export async function loginVerifyOtp(request: IVerifyOtpReq, orgMsg: Kafka.IMessage) {
  const txId: string = orgMsg.transactionId as string;
  if (request.otp_value == null && request.mobile_otp == null) {
    throw new Errors.GeneralError(WRONG_OTP);
  }

  const loginMethod = await loginMethodCache.findOrget(/* ... */);
  if (loginMethod == null) { throw new NoLoginMethodError(); }

  // ← Thêm block này
  if (request.otpType === 'SMART_OTP') {
    return verifySmartOtpLogin(request, orgMsg, loginMethod);
  }
  // ← Kết thúc block thêm

  // Phần còn lại giữ nguyên: send Kafka tới loginMethod.msName...
  let uri: string = loginMethod.msUri.getDefault(BASE_OTP_URL);
  uri = `${uri}/verify`;
  // ...
}

async function verifySmartOtpLogin(
  request: IVerifyOtpReq,
  orgMsg: Kafka.IMessage,
  loginMethod: LoginMethod,
): Promise<ILoginRes> {
  const txId: string = orgMsg.transactionId as string;
  const otpTypeMap = OTP_TYPE_URI_MAP.nhsv;
  const topic = otpTypeMap.TOPIC.SMART_OTP;
  const uri = otpTypeMap.SMART_OTP;

  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    topic,
    uri,
    {
      headers: request.headers,
      otpCode: request.otp_value,
    },
    conf.timeouts.loginPasswordOtp
  );

  if (msg.data.status) {
    throw Errors.createFromStatus(msg.data.status);
  }

  const loginResult: IServiceLoginRes = msg.data.data;
  return await doJobInTransaction<ILoginRes>(async (connection: Connection) => {
    if (request.headers.token.platform != null &&
        (request.headers.token.platform.toLowerCase() === 'android' ||
         request.headers.token.platform.toLowerCase() === 'ios')) {
      await registerMobileOtp(request, connection);
    }
    const scopeGroups = await scopeService.findScopeGroupsAsync(request.headers.token.loginMethod, connection);
    const scopeGroupIds = scopeGroups.map((lsg) => lsg.groupId.get());
    const refreshToken = await getRefreshTokenById(request.headers.token.refreshTokenId, connection);
    const params: IGenerateTokenParams = {
      txId,
      scopeGroupIds,
      loginMethodId: request.headers.token.loginMethod,
      userId: request.headers.token.userId,
      clientId: request.headers.token.clientId,
      refreshTokenTtl: refreshToken.getExtendData().rTtl,
      accessTokenTtl: refreshToken.getExtendData().aTtl,
      sourceIp: request.sourceIp,
      deviceType: request.deviceType,
      connection,
      roles: [],
      parentId: refreshToken.parentId.get(),
      userData: loginResult.userData,
      platform: request.headers.token.platform,
      grantType: request.headers.token.grantType,
      osVersion: request.headers.token.osVersion,
      appVersion: request.headers.token.appVersion,
      sessionId: refreshToken.getExtendData().sId,
      request: undefined,
      step: undefined,
      extraData: loginMethod.extraData.get(),
      refExpiredTime: refreshToken.expiredAt.get().getTime(),
      accExpiredTime: Math.min(
        moment().add(refreshToken.getExtendData().aTtl, 'second').toDate().getTime(),
        refreshToken.expiredAt.get().getTime()
      ),
    };
    const data: ITokenResult = await generateToken(params);
    return {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      userInfo: loginResult.userInfo,
      accExpiredTime: params.accExpiredTime,
      refExpiredTime: params.refExpiredTime,
    };
  });
}
```

- [ ] **Step 4.4 — Routing Smart OTP trong `verifyOtp` (VerifyOtp.ts)**

Trong `VerifyOtp.ts`, function `verifyOtp`, ngay sau check `lotte-rest-bridge` (dòng ~120), thêm:

```typescript
if (loginMethod.msName.get() === 'lotte-rest-bridge') {
  // Kiểm tra Smart OTP trước khi gọi lotte-rest-bridge OTP verify
  if (request.otpType === 'SMART_OTP') {
    return verifySmartOtpForLotteBridge(request, originMsg, loginMethod);
  }
  return verifyOtpLotteRestBridge(request, originMsg, loginMethod.extraData.get());
}
```

Thêm function `verifySmartOtpForLotteBridge` với logic tương tự `verifySmartOtpLogin` ở Task 4.3.

- [ ] **Step 4.5 — Thêm import `OTP_TYPE_URI_MAP` và `moment` vào `loginOtp.ts`**

```typescript
import { OTP_TYPE_URI_MAP } from '../../constants/OtpTypeUriMap';
import * as moment from 'moment';
```

- [ ] **Step 4.6 — Manual test Smart OTP verify với Postman**

```
// Step 1: Login
POST /rest/api/v1/login → lấy temp accessToken (sotpStatus = "Y")

// Step 2: Verify với Smart OTP
POST /rest/api/v1/login/sec/verifyOTP
Authorization: jwt <temp_accessToken>
body: { otpValue: "<6-digit TOTP>", otpType: "SMART_OTP" }

Expected: { accessToken: "final_xxx", refreshToken: "...", userInfo: {...} }

// Backward compat test
POST /rest/api/v1/login/sec/verifyOTP
body: { otpValue: "<4-digit SMS OTP>" }  ← không có otpType
Expected: login vẫn thành công như cũ
```

- [ ] **Step 4.7 — Commit**

```bash
git add src/models/request/IVerifyOtpReq.ts \
        src/constants/OtpTypeUriMap.ts \
        src/services/authen/loginOtp.ts \
        src/services/VerifyOtp.ts
git commit -m "feat(aaa): route SMART_OTP to smartOtp verify service in verifyOTP"
```

---

## FE Tasks

> ℹ️ Tất cả paths dưới đây là trong repo `nhsv-mts-rn`. Confirm paths thực tế với FE dev.

### Task 5: Thêm types + secure storage helper

**Files:**
- Modify: `src/interfaces/login.ts` (hoặc file chứa login response type)
- Create: `src/utils/smartOtpStorage.ts`

- [ ] **Step 5.1 — Thêm `sotpStatus` và `otpType` vào login Step 1 response type**

Tìm interface `LoginStep1Response` (hoặc tương đương) và thêm:

```typescript
export interface LoginStep1Response {
  accessToken: string;
  otpIndex?: string;
  userLevel?: string;
  registerMobileOtp?: boolean;
  sotpStatus?: 'Y' | 'N';   // ← thêm
  otpType?: 'SMART_OTP' | 'SMS_OTP';  // ← thêm
}
```

- [ ] **Step 5.2 — Tạo `smartOtpStorage.ts`**

File: `src/utils/smartOtpStorage.ts`

```typescript
import * as Keychain from 'react-native-keychain';  // hoặc thư viện secure storage hiện tại

const SOTP_KEY_SERVICE = 'nhsv_sotp_key';

export async function saveSotpKey(sotpKey: string): Promise<void> {
  await Keychain.setGenericPassword('sotpKey', sotpKey, { service: SOTP_KEY_SERVICE });
}

export async function getSotpKey(): Promise<string | null> {
  const result = await Keychain.getGenericPassword({ service: SOTP_KEY_SERVICE });
  if (result === false) return null;
  return result.password;
}

export async function clearSotpKey(): Promise<void> {
  await Keychain.resetGenericPassword({ service: SOTP_KEY_SERVICE });
}
```

> ℹ️ Confirm thư viện secure storage đang dùng trong project (`react-native-keychain`, `@react-native-async-storage/async-storage`, hoặc khác).

- [ ] **Step 5.3 — Commit**

```bash
git add src/interfaces/login.ts src/utils/smartOtpStorage.ts
git commit -m "feat(fe): add sotpStatus types and smartOtpStorage helper"
```

---

### Task 6: Decision hook `useSmartOtpLoginBranch`

**Files:**
- Create: `src/hooks/useSmartOtpLoginBranch.ts`

- [ ] **Step 6.1 — Tạo hook**

File: `src/hooks/useSmartOtpLoginBranch.ts`

```typescript
import { useCallback } from 'react';
import { getSotpKey } from '../utils/smartOtpStorage';

export type OtpLoginCase = 'CASE_A' | 'CASE_B' | 'CASE_C';

/**
 * CASE_A: sotpStatus=Y + local key tồn tại  → Smart OTP UI
 * CASE_B: sotpStatus=N                       → SMS OTP UI (unchanged)
 * CASE_C: sotpStatus=Y + không có local key  → Special screen (reinstall / lost device)
 */
export function useSmartOtpLoginBranch() {
  const determineCase = useCallback(async (
    sotpStatus: 'Y' | 'N' | undefined,
    otpType: 'SMART_OTP' | 'SMS_OTP' | undefined,
  ): Promise<OtpLoginCase> => {
    if (sotpStatus !== 'Y' || otpType !== 'SMART_OTP') {
      return 'CASE_B';
    }
    const localKey = await getSotpKey();
    return localKey ? 'CASE_A' : 'CASE_C';
  }, []);

  return { determineCase };
}
```

- [ ] **Step 6.2 — Commit**

```bash
git add src/hooks/useSmartOtpLoginBranch.ts
git commit -m "feat(fe): add useSmartOtpLoginBranch hook for OTP case routing"
```

---

### Task 7: Login saga — Branch `notifyMobileOtpNhsv`

**Files:**
- Modify: `src/reduxs/sagas/SendOTP/SendOTP.ts` (hoặc file login saga)

- [ ] **Step 7.1 — Tìm nơi gọi `notifyMobileOtpNhsv` trong login saga**

```bash
grep -r "notifyMobileOtpNhsv\|sendOTP\|notifyOTP" src/reduxs --include="*.ts" | head -10
```

- [ ] **Step 7.2 — Bọc call trong điều kiện**

Tìm đoạn code gọi `notifyMobileOtpNhsv` sau Step 1 login, sửa thành:

```typescript
// Trước: luôn gọi notifyMobileOtpNhsv
// await api.notifyMobileOtpNhsv(...)

// Sau:
const { sotpStatus, otpType } = loginStep1Response;
const loginCase = await determineCase(sotpStatus, otpType);

if (loginCase === 'CASE_B') {
  // SMS OTP flow: gọi notifyMobileOtpNhsv như cũ
  await api.notifyMobileOtpNhsv(/* params hiện tại */);
  yield put(loginActions.setOtpCase('CASE_B'));
} else {
  // SMART_OTP: không gọi notifyMobileOtpNhsv
  yield put(loginActions.setOtpCase(loginCase));  // 'CASE_A' hoặc 'CASE_C'
}

// Navigate tới OTP screen (giữ nguyên)
yield put(loginActions.navigateToOtpScreen());
```

- [ ] **Step 7.3 — Thêm `otpCase` vào login Redux state**

Trong login slice/reducer, thêm:
```typescript
interface LoginState {
  // ... fields hiện tại
  otpCase?: 'CASE_A' | 'CASE_B' | 'CASE_C';
}

// action
setOtpCase: (state, action) => {
  state.otpCase = action.payload;
}
```

- [ ] **Step 7.4 — Commit**

```bash
git add src/reduxs/sagas/SendOTP/SendOTP.ts src/reduxs/  # thêm đúng files đã sửa
git commit -m "feat(fe): branch notifyMobileOtpNhsv based on Smart OTP case"
```

---

### Task 8: OTP Screen — UI theo case

**Files:**
- Modify: OTP screen (tìm bằng `grep -r "notifyMobileOtpNhsv\|OtpScreen\|VerifyOTP" src/screens`)
- Create: `src/screens/Login/SmartOtpReinstallScreen.tsx`

- [ ] **Step 8.1 — Tìm OTP Screen hiện tại**

```bash
grep -r "otpValue\|verifyOTP\|OtpScreen" src/screens --include="*.tsx" -l | head -5
```

- [ ] **Step 8.2 — Branch UI trong OTP Screen**

Trong OTP Screen component, đọc `otpCase` từ Redux và branch:

```typescript
const otpCase = useSelector(state => state.login.otpCase);

// Render theo case
if (otpCase === 'CASE_C') {
  return <SmartOtpReinstallScreen />;
}

if (otpCase === 'CASE_A') {
  return (
    <View>
      <Text>Mở app → nhập PIN → lấy mã 6 chữ số</Text>
      <OtpInput
        length={6}
        onSubmit={(code) => dispatch(loginActions.verifySmartOtp(code))}
      />
      <TouchableOpacity onPress={handleCantGetCode}>
        <Text>Không lấy được mã?</Text>
      </TouchableOpacity>
    </View>
  );
}

// CASE_B: render UI hiện tại (không đổi gì)
return <ExistingSmsOtpUI />;
```

Khi user nhấn "Không lấy được mã?" (Case A → Case C):
```typescript
const handleCantGetCode = async () => {
  await clearSotpKey();  // xóa local key
  dispatch(loginActions.setOtpCase('CASE_C'));
};
```

- [ ] **Step 8.3 — verifyOTP action: truyền `otpType`**

Trong action gọi `POST /rest/api/v1/login/sec/verifyOTP`:

```typescript
// Case A — Smart OTP
yield call(api.verifyOTP, {
  otpValue: action.payload.code,
  otpType: 'SMART_OTP',
});

// Case B — SMS OTP (backward compat, không truyền otpType)
yield call(api.verifyOTP, {
  otpValue: action.payload.code,
});
```

- [ ] **Step 8.4 — Tạo `SmartOtpReinstallScreen`**

File: `src/screens/Login/SmartOtpReinstallScreen.tsx`

```typescript
import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useDispatch } from 'react-redux';
import { loginActions } from '../../reduxs/slices/loginSlice';

export function SmartOtpReinstallScreen() {
  const dispatch = useDispatch();

  const handleReactivate = () => {
    // Gọi /rest/api/v1/otp/send với txType: "SMART_OTP"
    dispatch(loginActions.startSmartOtpReactivation());
  };

  const handleSmsFallback = async () => {
    // Gọi notifyMobileOtpNhsv để gửi SMS, rồi switch về Case B UI
    dispatch(loginActions.requestSmsFallback());
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Thiết bị này chưa có Smart OTP</Text>
      <Text style={styles.body}>
        Smart OTP của bạn chưa được thiết lập trên thiết bị này.
        Kích hoạt lại để tiếp tục.
      </Text>

      <TouchableOpacity style={styles.primaryButton} onPress={handleReactivate}>
        <Text style={styles.primaryText}>Kích hoạt lại Smart OTP</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.secondaryButton} onPress={handleSmsFallback}>
        <Text style={styles.secondaryText}>Đăng nhập bằng SMS OTP lần này</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: 'center' },
  title: { fontSize: 20, fontWeight: '700', marginBottom: 12 },
  body: { fontSize: 15, color: '#5e6c84', marginBottom: 32 },
  primaryButton: {
    backgroundColor: '#0052cc', borderRadius: 8,
    paddingVertical: 14, alignItems: 'center', marginBottom: 12,
  },
  primaryText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  secondaryButton: {
    borderWidth: 1, borderColor: '#0052cc', borderRadius: 8,
    paddingVertical: 14, alignItems: 'center',
  },
  secondaryText: { color: '#0052cc', fontSize: 16 },
});
```

- [ ] **Step 8.5 — Saga xử lý `startSmartOtpReactivation`**

```typescript
function* handleSmartOtpReactivation() {
  try {
    // 1. Gửi OTP kích hoạt
    yield call(api.otpSend, { txType: 'SMART_OTP' });
    // Navigate sang màn nhập SMS OTP + tạo PIN mới
    yield put(loginActions.navigateToSmartOtpActivation());
  } catch (error) {
    yield put(loginActions.setError(error.message));
  }
}

function* handleSmsFallback() {
  try {
    // Gửi SMS OTP bình thường
    yield call(api.notifyMobileOtpNhsv, /* params */);
    yield put(loginActions.setOtpCase('CASE_B'));
  } catch (error) {
    yield put(loginActions.setError(error.message));
  }
}
```

- [ ] **Step 8.6 — Sau khi activation thành công: lưu sotpKey + complete login**

```typescript
function* handleSmartOtpActivationComplete(sotpKey: string, otpCode: string) {
  // Lưu local key
  yield call(saveSotpKey, sotpKey);
  // Dispatch verifyOTP với Smart OTP để complete login
  yield put(loginActions.verifySmartOtp(otpCode));
}
```

- [ ] **Step 8.7 — Commit**

```bash
git add src/screens/Login/ src/reduxs/
git commit -m "feat(fe): add OTP screen branching for Smart OTP cases A/B/C"
```

---

### Task 9: Post-login Phase 1 — Soft gate banner (Case B)

**Files:**
- Modify: Home screen hoặc App navigator

- [ ] **Step 9.1 — Tìm Home Screen**

```bash
grep -r "HomeScreen\|home.*screen\|Dashboard" src/screens --include="*.tsx" -l | head -5
```

- [ ] **Step 9.2 — Show banner khi `sotpStatus = "N"` sau login thành công**

```typescript
// Trong Home Screen component
const sotpStatus = useSelector(state => state.login.sotpStatus);
const [bannerDismissed, setBannerDismissed] = useState(false);

const showSmartOtpBanner = sotpStatus === 'N' && !bannerDismissed;

// Render
{showSmartOtpBanner && (
  <View style={styles.banner}>
    <Text>Kích hoạt Smart OTP để bảo mật hơn</Text>
    <TouchableOpacity onPress={() => navigation.navigate('SmartOtpActivation')}>
      <Text>Kích hoạt ngay</Text>
    </TouchableOpacity>
    <TouchableOpacity onPress={() => setBannerDismissed(true)}>
      <Text>Bỏ qua</Text>
    </TouchableOpacity>
  </View>
)}
```

- [ ] **Step 9.3 — Lưu `sotpStatus` vào Redux state sau login success**

Trong login success reducer/saga:
```typescript
// Sau verifyOTP thành công
yield put(loginActions.setSotpStatus(loginStep1Response.sotpStatus));
```

- [ ] **Step 9.4 — Hard redirect re-activation sau Case C SECONDARY login**

Trong login success saga, sau khi verifyOTP thành công với `otpType: "SMS_OTP"` VÀ trước đó user đã từng ở Case C (SECONDARY path):

```typescript
function* handleVerifyOtpSuccess(loginResponse: LoginFinalResponse) {
  const prevOtpCase = yield select(state => state.login.otpCase);

  if (prevOtpCase === 'CASE_C') {
    // User vừa dùng SMS fallback sau khi bị Case C
    // Hard redirect bắt buộc sang re-activation — không vào home
    yield put(loginActions.setReactivationRequired(true));
    yield call(navigateToSmartOtpActivation);
    return;
  }

  // Flow bình thường
  yield put(loginActions.loginSuccess(loginResponse));
  yield call(navigateToHome);
}
```

- [ ] **Step 9.5 — Commit**

```bash
git add src/screens/  src/reduxs/
git commit -m "feat(fe): add Phase 1 soft gate banner + Case C hard redirect re-activation"
```

---

## QA Checklist

Sau khi implement xong, verify theo [test cases trong design spec](../specs/2026-06-04-smart-otp-login-design.md#7-qa-test-cases):

- [ ] TC-01: Login Smart OTP đúng thiết bị — không gọi `notifyMobileOtpNhsv`, 6-digit code
- [ ] TC-02: Login SMS OTP (chưa kích hoạt) — flow không đổi, banner Phase 1
- [ ] TC-03: Case C PRIMARY — kích hoạt lại + Smart OTP login
- [ ] TC-04: Case C SECONDARY — SMS fallback + hard redirect re-activation
- [ ] TC-05/06: Smart OTP sai mã / hết hạn
- [ ] TC-07/08: Activation OTP sai / hết hạn trong Case C PRIMARY
- [ ] TC-09: `smartOtp/register` lỗi
- [ ] TC-10: Backward compat — app cũ không truyền `otpType`

---

**Open Questions (xem design spec Section 9):**
- OQ-1: Temp token permission cho `/otp/send` + `/smartOtp/register` — confirm với BE lead trước Task 8
- OQ-2: Enum chính xác `sotp_stat` từ Lotte — confirm trước Task 2
- OQ-3: Timeline Phase 2 hard gate — PM
- OQ-4: Rate limit Smart OTP verify — BE lead
