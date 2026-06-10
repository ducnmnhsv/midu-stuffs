# BE Task — Smart OTP Login Integration

## Reference

- Design spec: [`docs/superpowers/specs/2026-06-04-smart-otp-login-design.md`](../../docs/superpowers/specs/2026-06-04-smart-otp-login-design.md)
- PRD: [`Smart OTP - multi channels/Issues/06_PRD_Login_SmartOTP.md`](./06_PRD_Login_SmartOTP.md)
- API Mapping: [`Smart OTP - multi channels/Specifications/SmartOTP_API_Mapping.md`](../Specifications/SmartOTP_API_Mapping.md)

## Objective

Mở rộng login flow để hỗ trợ Smart OTP xác thực trực tiếp trên NHSV Pro app. Không tạo endpoint mới — chỉ sửa 2 endpoint hiện có và thêm routing logic.

## ⚠️ Confirm Trước Khi Code

| OQ | Câu hỏi | Cần từ |
|---|---|---|
| OQ-1 | Temp accessToken từ Step 1 login có đủ scope để gọi `/otp/send` và `/smartOtp/register`? | Security/BE Lead |
| OQ-2 | Enum chính xác của `sotp_stat` từ Lotte (xác nhận là `"Y"` / `"N"` hay value khác)? | BE Lead / Lotte doc |
| OQ-4 | Rate limit cho `otpType=SMART_OTP` trong `verifyOTP` — sai bao nhiêu lần thì lock? | Product |

---

## Task 1 — `aaa-main`: Thêm models

**Files:**
- `src/models/IServiceLoginRes.ts`
- `src/models/response/ILoginRes.ts`
- `src/models/request/IVerifyOtpReq.ts`

### 1.1 `IServiceLoginRes.ts` — Thêm `sotpStatus`

```typescript
export default interface IServiceLoginRes {
  conId?: IConnectionIdentifier;
  userInfo: IUserInfo;
  otpIndex?: string | number;
  otpValue?: string;
  sessionId?: string;
  userData: IUserData;
  sotpStatus?: string;   // "Y" | "N" — map từ Lotte sotp_stat
}
```

### 1.2 `ILoginRes.ts` — Thêm `sotpStatus` + `otpType`

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
  sotpStatus?: string;   // "Y" | "N"
  otpType?: string;      // "SMART_OTP" | "SMS_OTP"
}
```

### 1.3 `IVerifyOtpReq.ts` — Thêm `otpType`

```typescript
export default interface IVerifyOtpReq extends Models.IDataRequest {
  otp_value?: string;
  mobile_otp?: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
  otpType?: string;   // optional, default: "SMS_OTP" (backward compatible)
}
```

---

## Task 2 — `lotte-rest-bridge`: Pass `sotp_stat` qua response

**Files:** Handler map login response từ Lotte (xác định file trong lotte-rest-bridge repo)

### 2.1 Tìm file

```bash
grep -r "sotp_stat\|loginWithOtp\|IServiceLoginRes" src/ --include="*.ts" | head -10
```

### 2.2 Extract `sotp_stat` → `sotpStatus`

Trong handler map response từ Lotte, thêm field `sotpStatus`:

```typescript
const response: IServiceLoginRes = {
  userInfo: mapUserInfo(lotteData),
  userData: mapUserData(lotteData),
  otpIndex: lotteData.otp_index,
  otpValue: lotteData.otp_value,
  sotpStatus: lotteData.sotp_stat ?? 'N',  // ← thêm dòng này
};
```

> Confirm enum `sotp_stat` với Lotte trước khi code (OQ-2).

---

## Task 3 — `aaa-main`: Expose `sotpStatus` + `otpType` trong login Step 1 response

**Files:**
- `src/services/authen/loginPasswordOtp.ts`
- `src/services/authen/loginOtp.ts`

### 3.1 Helper function (thêm vào cả 2 file hoặc extract ra utils)

```typescript
function deriveOtpType(sotpStatus: string | undefined): string {
  return sotpStatus === 'Y' ? 'SMART_OTP' : 'SMS_OTP';
}
```

### 3.2 `loginPasswordOtp.ts` — `executeLoginWithOtp` callback

Tìm callback của `createToken` (dòng ~138), thêm 2 fields:

```typescript
(tokenResult: ITokenResult) => ({
  accessToken: tokenResult.accessToken,
  refreshToken: tokenResult.refreshToken,
  otpIndex: loginResult.otpIndex as string,
  userLevel: loginResult.userData.userLevel,
  registerMobileOtp: serviceUser.registerMobileOtp.get(),
  accExpiredTime: tokenResult.accExpiredTime,
  refExpiredTime: tokenResult.refExpiredTime,
  sotpStatus: loginResult.sotpStatus,               // ← thêm
  otpType: deriveOtpType(loginResult.sotpStatus),   // ← thêm
})
```

### 3.3 `loginOtp.ts` — `executeLoginWithOtp` callback

Tương tự, trong callback `createToken` (dòng ~180+):

```typescript
(tokenResult: ITokenResult) => ({
  accessToken: tokenResult.accessToken,
  refreshToken: tokenResult.refreshToken,
  otpIndex: loginResult.otpIndex as string,
  userLevel: loginResult.userData?.userLevel,
  accExpiredTime: tokenResult.accExpiredTime,
  refExpiredTime: tokenResult.refExpiredTime,
  sotpStatus: loginResult.sotpStatus,               // ← thêm
  otpType: deriveOtpType(loginResult.sotpStatus),   // ← thêm
})
```

### 3.4 Test với Postman

```
POST /rest/api/v1/login
body: { grant_type: "password_otp", username, password, client_id, client_secret }

Expect response có thêm:
  "sotpStatus": "Y" | "N"
  "otpType": "SMART_OTP" | "SMS_OTP"
```

---

## Task 4 — `aaa-main`: Smart OTP routing trong verifyOTP

**Files:**
- `src/constants/OtpTypeUriMap.ts`
- `src/services/authen/loginOtp.ts` (function `loginVerifyOtp`)
- `src/services/VerifyOtp.ts` (function `verifyOtp`)

### 4.1 `OtpTypeUriMap.ts` — Thêm entry `nhsv`

```typescript
export const OTP_TYPE_URI_MAP = {
  mas: { /* unchanged */ },
  kis: { /* unchanged */ },
  nhsv: {
    SMART_OTP: '/api/v1/smartOtp/verify',
    TOPIC: {
      SMART_OTP: 'paave-real-trading',  // ← confirm topic đúng với BE Lead
    }
  }
};
```

### 4.2 `loginOtp.ts` — `loginVerifyOtp`: thêm Smart OTP branch

Sau khi lấy `loginMethod`, thêm block trước phần xử lý hiện tại:

```typescript
export async function loginVerifyOtp(request: IVerifyOtpReq, orgMsg: Kafka.IMessage) {
  const txId = orgMsg.transactionId as string;
  if (request.otp_value == null && request.mobile_otp == null) {
    throw new Errors.GeneralError(WRONG_OTP);
  }

  const loginMethod = await loginMethodCache.findOrget(/* ... */);
  if (loginMethod == null) throw new NoLoginMethodError();

  // ← Thêm block Smart OTP routing
  if (request.otpType === 'SMART_OTP') {
    return verifySmartOtpLogin(request, orgMsg, loginMethod);
  }
  // ← Kết thúc block

  // Phần còn lại không đổi
  let uri = loginMethod.msUri.getDefault(BASE_OTP_URL);
  uri = `${uri}/verify`;
  // ...
}
```

Thêm function `verifySmartOtpLogin`:

```typescript
async function verifySmartOtpLogin(
  request: IVerifyOtpReq,
  orgMsg: Kafka.IMessage,
  loginMethod: LoginMethod,
): Promise<ILoginRes> {
  const txId = orgMsg.transactionId as string;
  const topic = OTP_TYPE_URI_MAP.nhsv.TOPIC.SMART_OTP;
  const uri = OTP_TYPE_URI_MAP.nhsv.SMART_OTP;

  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId, topic, uri,
    { headers: request.headers, otpCode: request.otp_value },
    conf.timeouts.loginPasswordOtp
  );
  if (msg.data.status) throw Errors.createFromStatus(msg.data.status);

  const loginResult: IServiceLoginRes = msg.data.data;
  return doJobInTransaction<ILoginRes>(async (connection: Connection) => {
    if (request.headers.token.platform?.toLowerCase() === 'android' ||
        request.headers.token.platform?.toLowerCase() === 'ios') {
      await registerMobileOtp(request, connection);
    }
    const scopeGroups = await scopeService.findScopeGroupsAsync(
      request.headers.token.loginMethod, connection
    );
    const scopeGroupIds = scopeGroups.map(lsg => lsg.groupId.get());
    const refreshToken = await getRefreshTokenById(
      request.headers.token.refreshTokenId, connection
    );
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
    const data = await generateToken(params);
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

### 4.3 `VerifyOtp.ts` — `verifyOtp`: thêm Smart OTP branch trong lotte-rest-bridge path

Trong function `verifyOtp`, sau check `lotte-rest-bridge` (dòng ~120):

```typescript
if (loginMethod.msName.get() === 'lotte-rest-bridge') {
  if (request.otpType === 'SMART_OTP') {              // ← thêm branch
    return verifySmartOtpLogin(request, originMsg, loginMethod);
  }
  return verifyOtpLotteRestBridge(request, originMsg, loginMethod.extraData.get());
}
```

> `verifySmartOtpLogin` có thể extract ra shared module để dùng chung giữa `loginOtp.ts` và `VerifyOtp.ts`.

### 4.4 Test backward compatibility

```
// Test app cũ không truyền otpType
POST /rest/api/v1/login/sec/verifyOTP
body: { otpValue: "1234" }   ← không có otpType
Expected: login thành công như cũ (không bị break)

// Test Smart OTP
POST /rest/api/v1/login/sec/verifyOTP
Authorization: jwt <temp_token_with_sotpStatus_Y>
body: { otpValue: "123456", otpType: "SMART_OTP" }
Expected: login thành công với final token
```

---

## QA Scenarios (BE)

| Scenario | Expected |
|---|---|
| Login user có `sotp_stat=Y` từ Lotte | Response có `sotpStatus:"Y"`, `otpType:"SMART_OTP"` |
| Login user có `sotp_stat=N` từ Lotte | Response có `sotpStatus:"N"`, `otpType:"SMS_OTP"` |
| verifyOTP với `otpType:"SMART_OTP"`, mã đúng | Login thành công |
| verifyOTP với `otpType:"SMART_OTP"`, mã sai | Error `WRONG_OTP` hoặc SmartOTP service error |
| verifyOTP không có `otpType` | Flow SMS hiện tại, không break |
| verifyOTP với `otpType:"SMS_OTP"` rõ ràng | Flow SMS hiện tại |

---

**Document Status:** Ready for BE development
**For:** BE Developer, BE Lead
**Next Steps:** Confirm OQ-1 (temp token scope) + OQ-2 (sotp_stat enum) → start Task 1 → Task 2 → Task 3 → Task 4
