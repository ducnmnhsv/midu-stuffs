# NHSV Pro App — Danh sách cURL cho Active API

Tài liệu này liệt kê toàn bộ API đang được gọi từ app NHSV Pro (repo `nhsv-mts-rn`), dưới dạng cURL. Base URL dùng **production**.

**Chung:**
- Base REST (v1): `https://nhsvpro.nhsv.vn/rest/api/v1`
- Market (v2): `https://nhsvpro.nhsv.vn/rest/api/v2`
- Domain: `https://nhsvpro.nhsv.vn`
- NHSV API: `https://nhsvpro-sv74-prod-api.nhsv.vn`
- OTP: `https://noti-api.nhsv.vn/otp/api/v1`

**Header cho API cần đăng nhập:**  
`-H "Authorization: jwt <ACCESS_TOKEN>"`  
`-H "accept-language: vi"`  
Với POST/PUT có body: `-H "Content-Type: application/json" -d '{"key":"value"}'`

Placeholder trong URL: thay `{...}` bằng giá trị thực (ví dụ `{symbol}` → `VNM`).

**Quy ước tài liệu:** Mỗi curl có đủ **query parameter** (GET) hoặc **body** (POST/PUT) theo interface trong app; giá trị mẫu dạng `<placeholder>` hoặc ví dụ cụ thể.

---

## 1. Auth & Account

```bash
# Refresh token — body (POST): grant_type, client_id, client_secret, refresh_token (từ store)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/refreshToken" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi" \
  -H "Content-Type: application/json" \
  -d '{"grant_type":"refresh_token","client_id":"nhsv","client_secret":"nhsv","refresh_token":"<REFRESH_TOKEN>"}'

# Revoke token — body (POST)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/revokeToken" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi" \
  -H "Content-Type: application/json" \
  -d '{}'

# Login — body (POST). ILoginRequest: platform, appVersion?, grant_type, client_id, client_secret, username, password?, device_id, rememberMe?, session_time_in_minute; với password_otp thêm sec_code sau bước OTP
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/login" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{
    "platform": "NHSV_MTS_IOS",
    "appVersion": "1.0.0",
    "grant_type": "password_otp",
    "client_id": "nhsv",
    "client_secret": "nhsv",
    "username": "<USERNAME>",
    "password": "<PASSWORD>",
    "device_id": "<DEVICE_UNIQUE_ID>",
    "rememberMe": true,
    "session_time_in_minute": 120
  }'

# Login (non-login / client_credentials) — body
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/login" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{
    "grant_type": "client_credentials",
    "client_id": "nhsv",
    "client_secret": "nhsv",
    "device_id": "<DEVICE_UNIQUE_ID>",
    "platform": "NHSV_MTS_IOS",
    "session_time_in_minute": 480,
    "appVersion": "1.0.0"
  }'

# Verify OTP (sau login) — body: otp
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/login/sec/verifyOTP" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"otp": "<OTP_CODE>"}'

# Biometric register — body: ILoginBiometricRequest (signatureValue, username, rememberMe, grant_type, client_id, client_secret, device_id?, appVersion, platform, session_time_in_minute)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/biometricRegister" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"signatureValue":"<SIGNATURE>","username":"<USERNAME>","rememberMe":true,"grant_type":"biometric_otp","client_id":"nhsv","client_secret":"nhsv","appVersion":"1.0.0","platform":"NHSV_MTS_IOS","session_time_in_minute":120}'

# Query biometric status — GET, không query param bắt buộc
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/queryBiometricStatus" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Biometric unregister — POST, body rỗng
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/unregisterBiometric" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{}'

# Change PIN — PUT, body: oldPin, newPin (kiểu tương ứng backend)
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/changePin" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"oldPin":"","newPin":""}'

# Change password — PUT, body: oldPassword, newPassword
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/changePassword" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"oldPassword":"","newPassword":""}'

# Request reset password — POST. IResetPasswordRequest.params: accountNumber, phoneNumber, identifierNumber
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/account/resetPassword/init" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"accountNumber":"","phoneNumber":"","identifierNumber":""}'

# Verify OTP reset password — POST. IVerifyOTPResetPassword.params: otpKey, otpValue
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/account/resetPassword/verifyOtp" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"otpKey":"","otpValue":""}'

# Reset password — POST. IResetPassword.params: otpKey, password
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/account/resetPassword" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"otpKey":"","password":""}'
```

---

## 2. Cash flow (Rút tiền, chuyển tiền, số dư)

```bash
# Danh sách ngân hàng rút tiền — GET, không query param
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/withdraw/banks" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Lịch sử rút tiền — GET (có thể có fetchCount, nextKey tùy backend)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/withdraw/history" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Yêu cầu rút tiền — POST, body: accountNumber, subNumber?, amount, bankCode, ... (theo spec TradeX)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/withdraw/request" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"accountNumber":"<ACCOUNT>","subNumber":"<SUB>","amount":0,"bankCode":""}'

# Hủy yêu cầu rút tiền — PUT, body: requestId hoặc tương đương
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/equity/withdraw/cancel" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{}'

# Lịch sử chuyển tiền — GET (có thể fromDate, toDate, fetchCount, nextKey)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/cash/history" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Chuyển tiền — POST, body: accountNumber, subNumber?, amount, receivingAccount, ...
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/cash" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"accountNumber":"<ACCOUNT>","subNumber":"<SUB>","amount":0}'

# Số dư tiền — GET. Query: accountNumber, subNumber (app gửi từ selectedAccount)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/cashBalance?accountNumber=<ACCOUNT>&subNumber=<SUB>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Lịch sử nộp tiền (cash statement) — GET (có thể fromDate, toDate, fetchCount, nextKey)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/cash/deposit/history" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"
```

---

## 3. Margin / Loan (Lotte)

```bash
# Chi tiết khoản vay
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/loan/detail" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Hạn mức vay khả dụng
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/loan/available" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Đăng ký vay
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/loan/register" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Lịch sử vay
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/loan/history" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Phí vay ước tính
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/loan/estimatedFee" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Lịch sử vay margin
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/loanHistory" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 4. Chuyển khoản chứng khoán

```bash
# Lịch sử chuyển CK
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/stock/history" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Yêu cầu chuyển CK
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/stock" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Số dư chuyển CK
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/stock/balance" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 5. Xác nhận lệnh / vay / chuyển tiền-CK

Tất cả API trong mục này dùng base `https://nhsvpro.nhsv.vn/rest/api/v1`, header `Authorization: jwt <ACCESS_TOKEN>` và `accept-language: vi`. GET: query string; POST: body JSON.

**Tham chiếu type:** `screens/ConfirmOrdersScreen/ConfirmOrdersType.ts`, sagas trong `reduxs/sagas/ConfirmOrders/`.

```bash
# ----- Lệnh cần xác nhận (Order confirm) -----

# Lấy danh sách lệnh cần xác nhận — GET. IGetConfirmOrdersParams: accountNumber, subNumber?, stockCode?, fromDate, toDate, confirmStatus?, marketType?, cancelType?, sellBuyType?, mediaType?, fetchCount?, nextKey?
# confirmStatus: Y|N|D (CONFIRMED|NOT_CONFIRMED|EXPIRED), marketType: 1|2|3 (HOSE|HNX|UPCOM), cancelType: 1|2|3
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Load more (thêm nextKey từ item cuối response)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20&nextKey=<NEXT_KEY>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Xác nhận lệnh — POST. IConfirmOrdersRequestParams: accountNumber, subNumber?, orders: [{ orderDate, orderNumber }]
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/confirm" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{
    "accountNumber": "<ACCOUNT>",
    "subNumber": "<SUB>",
    "orders": [
      { "orderNumber": "<ORDER_NUMBER>", "orderDate": "20250312" },
      { "orderNumber": "<ORDER_NUMBER_2>", "orderDate": "20250312" }
    ]
  }'

# ----- Vay (Loan) cần xác nhận -----

# Lấy danh sách khoản vay cần xác nhận — GET. IGetOnlineConfirmParams: accountNumber, subNumber?, fromDate, toDate, status? (Y|N|ALL), fetchCount, nextKey?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/loan/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/loan/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20&nextKey=<NEXT_KEY>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Xác nhận vay — POST. IPostOnlineLoanConfirmParams: accountNumber, subNumber, sequenceNumber, loanDate, matchDate, username, loanBankCode, loanAmount (số tiền)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/loan/confirm" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{
    "accountNumber": "<ACCOUNT>",
    "subNumber": "<SUB>",
    "sequenceNumber": "<SEQUENCE_NUMBER>",
    "loanDate": "20250312",
    "matchDate": "20250312",
    "username": "<USERNAME>",
    "loanBankCode": "<BANK_CODE>",
    "loanAmount": 10000000
  }'

# ----- Chuyển tiền (Cash transfer) cần xác nhận -----

# Lấy danh sách chuyển tiền cần xác nhận — GET. IGetOnlineConfirmParams: accountNumber, subNumber?, fromDate, toDate, status?, fetchCount, nextKey? (app xóa stockCode trước khi gọi)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/cash/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/cash/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20&nextKey=<NEXT_KEY>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Xác nhận chuyển tiền — POST. Body: IPostOnlineConfirmCashItem + accountNumber, subNumber → sequenceNumber, date, accountNumber, subNumber
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/cash/confirm" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{
    "accountNumber": "<ACCOUNT>",
    "subNumber": "<SUB>",
    "sequenceNumber": "<SEQUENCE_NUMBER>",
    "date": "20250312"
  }'

# ----- Chuyển CK (Stock transfer) cần xác nhận -----

# Lấy danh sách chuyển CK cần xác nhận — GET. IGetOnlineConfirmParams: accountNumber, subNumber?, stockCode?, fromDate, toDate, status?, fetchCount, nextKey?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/stock/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/stock/confirm?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250201&toDate=20250312&fetchCount=20&nextKey=<NEXT_KEY>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Xác nhận chuyển CK — POST. Body: IPostOnlineConfirmStockItem + accountNumber → sequenceNumber, date, stockCode, accountNumber (app không gửi subNumber)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/transfer/stock/confirm" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{
    "accountNumber": "<ACCOUNT>",
    "sequenceNumber": "<SEQUENCE_NUMBER>",
    "date": "20250312",
    "stockCode": "VNM"
  }'
```

---

## 6. Tài khoản & Tài sản

```bash
# Sức mua (buyable) — GET. IBuyableInfoRequest: accountNumber, subNumber, stockCode, orderPrice
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/buyable?accountNumber=<ACCOUNT>&subNumber=<SUB>&stockCode=VNM&orderPrice=25000" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# CK có thể bán (sellable) — GET. ISellableInfoRequest: accountNumber, subNumber, stockCode, fetchCount?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/sellable?accountNumber=<ACCOUNT>&subNumber=<SUB>&stockCode=VNM" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Margin — GET. IMarginRequest: accountNumber, subNumber, symbolCode
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/margin?accountNumber=<ACCOUNT>&subNumber=<SUB>&symbolCode=VNM" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Lịch sử P/L — GET. IProfitLossHistoryRequest: accountNumber, subNumber, fromDate, toDate, fetchCount?, nextKey?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/profitLoss/history?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250101&toDate=20251231" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Thông tin tài khoản (Lotte) — GET, không query bắt buộc
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/info" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Danh sách tiểu khoản — GET
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/subAccount" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Thông tin tài sản — GET. App gửi: accountNumber, subNumber
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/assetInfo?accountNumber=<ACCOUNT>&subNumber=<SUB>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# P/L — GET. accountNumber, subNumber
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/profitLoss?accountNumber=<ACCOUNT>&subNumber=<SUB>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Ước tính tài sản/vay — GET
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/estAssetLoanInfo?accountNumber=<ACCOUNT>&subNumber=<SUB>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Danh sách ngân hàng liên kết — GET. IQueryBankAccountRequest: accountNumber, subNumber
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/banks?accountNumber=<ACCOUNT>&subNumber=<SUB>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"
```

---

## 7. Lệnh (Order) — Lotte & TradeX

```bash
# Sổ lệnh (Lotte) — GET. IOrderbookRequest: accountNumber, subNumber, fromDate?, toDate?, stockCode?, sellBuyType, matchType?, sortType?, nextKey?, lastOrderDate?, fetchCount?, marketType?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/order/history?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250301&toDate=20250312&sellBuyType=ALL&fetchCount=20" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Sổ lệnh (orderBook v2) — GET. IOrderbookRequest2: accountNumber, subNumber, matchType (MATCHED|UNMATCHED|ALL), fetchCount, deviceUniqueId, sellBuyType, nextKey?, lastOrderDate?, stockCode?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/orderBook?accountNumber=<ACCOUNT>&subNumber=<SUB>&matchType=ALL&fetchCount=20&deviceUniqueId=<DEVICE_ID>&sellBuyType=ALL" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Lệnh chưa khớp trong ngày — GET. ITodayUnmatchRequest: accountNumber, subNumber, date, stockCode?, lastBranchCode?, lastOrderNumber?, lastOrderPrice?, fetchCount?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/order/todayUnmatch?accountNumber=<ACCOUNT>&subNumber=<SUB>&date=20250312" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Lịch sử lệnh ATO/ATC — GET. IAdvanceOrderRequest: accountNumber, subNumber, stockCode?, marketType?, sellBuyType?, lastOrderDate?, fetchCount?, lastOrderNumber?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/advance/history?accountNumber=<ACCOUNT>&subNumber=<SUB>" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Lịch sử lệnh stop — GET. IStopOrderRequest: accountNumber, subNumber, fromDate, toDate, fetchCount, code?, sellBuyType?, orderType?, status?, lastStopOrderId?
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/stop/history?accountNumber=<ACCOUNT>&subNumber=<SUB>&fromDate=20250301&toDate=20250312&fetchCount=20" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Đặt lệnh thường (Lotte) — POST. IPlaceNormalOrderRequest: accountNumber, stockCode, orderQuantity, securitiesType, sellBuyType, bankCode, orderType (LO|MP), deviceUniqueId, subNumber?, orderPrice? (0 nếu MP), bankName?
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/order" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{
    "accountNumber": "<ACCOUNT>",
    "subNumber": "<SUB>",
    "stockCode": "VNM",
    "orderQuantity": 100,
    "securitiesType": "STOCK",
    "sellBuyType": "BUY",
    "bankCode": "<BANK_CODE>",
    "bankName": "<BANK_NAME>",
    "orderType": "LO",
    "orderPrice": 25000,
    "deviceUniqueId": "<DEVICE_ID>"
  }'

# Đặt lệnh ATO/ATC — POST. IPlaceAdvanceOrderRequest: accountNumber, subNumber?, stockCode, orderQuantity, orderPrice, bankCode, deviceUniqueId, sellBuyType, orderType, phoneNumber, securitiesType?, advanceOrderDate?
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/order/advance" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"accountNumber":"<ACCOUNT>","subNumber":"<SUB>","stockCode":"VNM","orderQuantity":100,"orderPrice":25000,"bankCode":"","deviceUniqueId":"<DEVICE_ID>","sellBuyType":"BUY","orderType":"ATO","phoneNumber":""}'

# Đặt lệnh stop — POST. IPlaceStopOrderRequest: toDate, fromDate, bankCode, bankName, deviceUniqueId, orderType (STOP|STOP_LIMIT), code, stopPrice, subNumber, orderPrice (số hoặc "MP"), sellBuyType, orderQuantity, accountNumber
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/stop" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"toDate":"20250331","fromDate":"20250312","bankCode":"","bankName":"","deviceUniqueId":"<DEVICE_ID>","orderType":"STOP_LIMIT","code":"VNM","stopPrice":24000,"subNumber":"<SUB>","orderPrice":25000,"sellBuyType":"BUY","orderQuantity":100,"accountNumber":"<ACCOUNT>"}'

# Hủy lệnh thường — PUT. ICancelNormalOrderRequest: accountNumber, branchCode, orderNumber, subNumber?
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/order/cancel" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"accountNumber":"<ACCOUNT>","branchCode":"<BRANCH_CODE>","orderNumber":"<ORDER_NUMBER>","subNumber":"<SUB>"}'

# Hủy lệnh ATO/ATC — PUT. ICancelAdvanceOrderRequest: accountNumber, orderNumber, advanceOrderDate, subNumber?
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/order/advance/cancel" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"accountNumber":"<ACCOUNT>","orderNumber":"<ORDER_NUMBER>","advanceOrderDate":"20250312","subNumber":"<SUB>"}'

# Hủy lệnh stop — PUT. ICancelStopOrderRequest: stopOrderId
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/stop/cancel" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"stopOrderId": 12345}'

# Sửa lệnh thường — PUT. IModifyOrderbookRequest: accountNumber, branchCode, orderNumber, orderQuantity, orderPrice, subNumber?, bankCode?, bankName?, orderType?, stockCode?, sellBuyType?, securitiesType?
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/order/modify" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"accountNumber":"<ACCOUNT>","branchCode":"<BRANCH_CODE>","orderNumber":"<ORDER_NUMBER>","orderQuantity":200,"orderPrice":26000,"subNumber":"<SUB>"}'

# Sửa lệnh stop — PUT. IModifyStopOrderRequest: stopOrderId, orderQuantity, stopPrice, orderPrice (number|null), fromDate, toDate, stockCode
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/equity/order/stop/modify" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"stopOrderId":12345,"orderQuantity":100,"stopPrice":24000,"orderPrice":25000,"fromDate":"20250312","toDate":"20250331","stockCode":"VNM"}'
```

---

## 8. Virtual / Contest (domainURI)

```bash
# Buyable (virtual) — GET. IBuyableInfoVirtualRequest: stockCode, orderPrice, subAccount
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/account/buyable?subAccount=<SUB>&stockCode=VNM&orderPrice=25000" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Sellable (virtual) — GET. ISellableInfoVirtualRequest: subAccount, stockCode
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/account/sellable?subAccount=<SUB>&stockCode=VNM" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Real trading NHSV contests
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Contests booked
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/booked" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Contests listed
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/listed" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Contests expired
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/expired" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Join contest — POST. Path: {contestId}. Body (nếu có): theo spec backend
curl -X POST "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/<CONTEST_ID>/join" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{}'

# Book contest — POST. Path: {contestId}
curl -X POST "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/<CONTEST_ID>/book" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{}'

# Leaderboard settings (GET/PUT)
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/leaderboard/settings" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X PUT "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/leaderboard/settings" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Ranking — GET. Path: {contestId}. IRealContestRankingListRequest: contestId, period? (WEEK|ALL), withCondition?, pageNumber?, pageSize?
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/<CONTEST_ID>/ranking?period=ALL&withCondition=false&pageNumber=0&pageSize=20" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"
# Ranking history — GET. IRealContestRankingHistoryListRequest: contestId, date, period, withCondition?, pageNumber?, pageSize?
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/<CONTEST_ID>/rankingHistory?date=20250312&period=ALL" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"
# Current ranking — GET. IRealContestCurrentRankingRequest: contestId, period (WEEK|ALL), withCondition
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/real-trading/nhsv/contests/<CONTEST_ID>/currentRanking?period=ALL&withCondition=false" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# P/L đã thực hiện (virtual)
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v2/equity/account/realizedProfitLoss" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Sổ lệnh virtual — GET. IOrderbookVirtualRequest: subAccount, fromDate?, toDate?, pageSize, pageNumber?, sellBuyType?, status?
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/order/history?subAccount=<SUB>&fromDate=20250312&toDate=20250312&pageSize=20&pageNumber=0" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "accept-language: vi"

# Đặt lệnh virtual — POST. IPlaceNormalOrderVirtualRequest: subAccount, code, quantity, price? (undefined nếu MP), orderCommand (LO|MP|ATO|ATC), action (BUY|SELL)
curl -X POST "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/order" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"subAccount":"<SUB>","code":"VNM","quantity":100,"price":25000,"orderCommand":"LO","action":"BUY"}'

# Hủy lệnh virtual — POST. ICancelNormalOrderVirtualRequest: orderId (number)
curl -X POST "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/cancel" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"orderId": 12345}'

# Sửa lệnh virtual — POST. IModifyOrderbookVirtualRequest: orderId, newPrice, newQuantity
curl -X POST "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/modify" \
  -H "Authorization: jwt <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "accept-language: vi" \
  -d '{"orderId":12345,"newPrice":26000,"newQuantity":200}'

# P/L virtual
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/account/profitLoss" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# VirtualCore sub-accounts
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/subAccounts" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# VirtualCore contests
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests/listed" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests/expired" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests/booked" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# VirtualCore contest by id (thay {contestId})
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests/{contestId}/ranking" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests/{contestId}/currentRanking" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X POST "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests/{contestId}/join" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'
curl -X POST "https://nhsvpro.nhsv.vn/virtual/api/v1/virtualCore/contests/{contestId}/book" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Leaderboard investing
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/leaderboard/investing" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/leaderboard/currentInvestingInfo" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Following P/L (copy trading)
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/account/followingAccumulativePL" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/profile/tradingHistory" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/account/followingProfitLoss" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X GET "https://nhsvpro.nhsv.vn/virtual/api/v1/equity/account/followingDailyProfitLoss" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 9. Market data (marketUrl v2 — không bắt buộc auth)

```bash
# Symbol odd lot — GET, không query param
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbol/oddlotLatest" \
  -H "accept-language: vi"

# Nước ngoài mua/bán theo mã — GET. Path: {symbol}
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbol/VNM/foreigner" \
  -H "accept-language: vi"

# Quote theo mã — GET. Path: {symbol}
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbol/VNM/quote" \
  -H "accept-language: vi"

# Thống kê theo mã — GET. Path: {symbol}
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbol/VNM/statistic" \
  -H "accept-language: vi"

# Danh sách index
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/index/list" \
  -H "accept-language: vi"

# Lịch sử chart (TradingView) — GET. Query: symbol, resolution (1D|1W|...), from (unix), to (unix)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/tradingview/history?symbol=VNM&resolution=1D&from=0&to=9999999999" \
  -H "accept-language: vi"

# Kỳ (period) theo mã — GET. Path: {symbol}, {periodType}. Query (nếu có): from, to, ...
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbol/VNM/period/1D" \
  -H "accept-language: vi"

# Xếp hạng cổ phiếu theo kỳ
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/stock/ranking/period" \
  -H "accept-language: vi"

# Xếp hạng giao dịch
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/ranking/trade" \
  -H "accept-language: vi"

# Xếp hạng ĐTNN
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/ranking/foreigner" \
  -H "accept-language: vi"

# Danh sách mã theo index (thay {indexCode})
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/indexStockList/{indexCode}" \
  -H "accept-language: vi"

# Giá index mới nhất
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbol/latest" \
  -H "accept-language: vi"

# CW chi tiết (cần auth)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/cw/{symbol}/detail" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Sự kiện giá (right)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbol/{symbol}/right" \
  -H "accept-language: vi"
```

---

## 10. Market (baseURI v1 & symbolInfo v2)

```bash
# Symbol info (bulk — dùng trong saga QuerySymbolData, GetTopStocks, v.v.)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v2/market/symbolInfo" \
  -H "accept-language: vi"

# Stock latest (symbol info)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/market/stock/latest" \
  -H "accept-language: vi"

# Bid/offer theo mã
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/market/symbol/bidOffer/{symbol}" \
  -H "accept-language: vi"
```

---

## 11. Tin tức & Thông báo

```bash
# Notices (domainURI)
curl -X GET "https://nhsvpro.nhsv.vn/rest/nhsv-api/new-mts/get-notice" \
  -H "accept-language: vi"

# Tin mới nhất
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/news/latest" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Tin theo watchlist
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/news/getLatestNewsByWatchList" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Tin theo danh sách mã
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/news/getLatestNewsByStocks" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Chi tiết tin
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/news/newsDetail" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Tin theo mã (stock news)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/news/stockNews" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Thông báo bảo trì (không auth)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/notification/maintenance" \
  -H "accept-language: vi"

# Thông báo tài khoản
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/notification" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 12. Đổi consultant & Chat broker

```bash
# Lịch sử đổi TVV
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/account/changeBroker/history" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Khởi tạo đổi TVV
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/account/changeBroker/init" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Danh sách phòng chat
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/brokerChat/chatRooms" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Chi tiết phòng chat (thay {id})
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/brokerChat/chatRooms/{id}" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Danh sách TVV
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/brokerChat/brokerProfile" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Hồ sơ TVV (thay {id})
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/brokerChat/brokerProfile/{id}" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 13. Watchlist / Favorite

```bash
# Tạo watchlist
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/watchlist" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Danh sách watchlist
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/watchlist" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Sửa watchlist
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/watchlist" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Xóa watchlist
curl -X DELETE "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/watchlist" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Sắp xếp thứ tự watchlist
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/watchlist/order" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Danh sách mã trong watchlist
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/symbol" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Thêm mã vào watchlist
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/symbol" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Xóa mã khỏi watchlist
curl -X DELETE "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/symbol" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Kiểm tra mã có trong watchlist
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/favorite/symbol/include" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 14. Copy trading

```bash
# Thông tin subscriber
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/subscriber/subscriberInformation" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Tăng trưởng subscriber
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/marketLeader/subscriberGrowthRate" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Profile market leader (thay {marketLeaderId})
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/marketLeader/profile/{marketLeaderId}" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Portfolio hiện tại
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/marketLeader/currentPortfolio" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Portfolio lịch sử
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/marketLeader/historicalPortfolio" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Toàn bộ mã lịch sử
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/marketLeader/historicalPortfolio/allStocks" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Danh sách market leader
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/marketLeader/list" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Đăng ký theo dõi
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/subscriber/subscribe" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Hủy theo dõi
curl -X DELETE "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/subscriber/subscribe" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# P/L theo ngày
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/copyTrading/marketLeader/dailyProfitLossRatio" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 15. eKYC & HĐĐT

```bash
# Tạo eKYC (Lotte)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/ekycs" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Gửi OTP eKYC
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/ekyc-admin/sendOtp" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Xác minh OTP eKYC
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/ekyc-admin/verifyOtp" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Chi nhánh NHSV
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/ekycs/branch" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Danh sách ngân hàng
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/ekycs/banks" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Chi nhánh ngân hàng (thay {bankCode})
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/ekycs/banks/{bankCode}/branches" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Đối tác
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/ekycs/partner" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Tạo eKYC (create)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/ekycs/create" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Kiểm tra CCCD
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/checkNationalId" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# URL webview HĐĐT
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/contracts" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Trạng thái hợp đồng (Lotte)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/contractStatus" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Trạng thái VSD
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/vsdStatus" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# URL upload ảnh (AWS)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/aws" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Gửi phản hồi (không bắt buộc auth)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/equity/account/feedback" \
  -H "Content-Type: application/json" -H "accept-language: vi" -d '{}'
```

---

## 16. Quyền mua (Rights)

```bash
# Quyền mua khả dụng
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/rights/available" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Chi tiết quyền
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/rights/detail" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Đăng ký quyền
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/rights/register" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Lịch sử quyền (thay {type})
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/rights/history/{type}" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Hủy đăng ký quyền
curl -X PUT "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/rights/cancel" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Quyền sắp diễn ra
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/equity/rights/upcoming" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 17. Cài đặt thông báo & OTP

```bash
# Cài đặt thông báo (GET/POST)
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/notification/settings" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/lotte/equity/account/notification/settings" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Gửi OTP (in-app)
curl -X POST "https://nhsvpro.nhsv.vn/rest/api/v1/notifyMobileOtpNhsv" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# OTP mới (device get-otp) — full URI
curl -X POST "https://noti-api.nhsv.vn/otp/api/v1/device/get-otp" \
  -H "Content-Type: application/json" -d '{}'

# Đăng ký thiết bị OTP
curl -X POST "https://noti-api.nhsv.vn/otp/api/v1/device/register" \
  -H "Content-Type: application/json" -d '{}'

# Lấy OTP (GET)
curl -X GET "https://noti-api.nhsv.vn/otp/api/v1/device/get-otp" \
  -H "accept-language: vi"
```

---

## 18. Công ty & Tài chính

```bash
# Cổ đông lớn
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/company/majorShareholders" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Cơ cấu sở hữu
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/company/ownershipStructure" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Hồ sơ công ty
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/company/profile" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Lịch sử giao dịch công ty
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/company/transactionHistory" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Báo cáo thu nhập
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/finance/incomeStatement" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Bảng cân đối
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/finance/balanceSheet" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Dòng tiền
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/finance/cashFlow" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Chỉ số tài chính mới nhất
curl -X GET "https://nhsvpro.nhsv.vn/rest/api/v1/financial/latestFinancialRatio" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 19. Margin mở (nhsvURI)

```bash
# Gói margin
curl -X GET "https://nhsvpro-sv74-prod-api.nhsv.vn/margin/v1/margin/packages" \
  -H "accept-language: vi"

# Đăng ký auth (margin)
curl -X POST "https://nhsvpro-sv74-prod-api.nhsv.vn/auth/auth-service/register" \
  -H "Content-Type: application/json" -d '{}'

# Lấy OTP (margin)
curl -X POST "https://nhsvpro-sv74-prod-api.nhsv.vn/auth/auth-service/get-otp" \
  -H "Content-Type: application/json" -d '{}'

# Xác minh OTP (margin)
curl -X POST "https://nhsvpro-sv74-prod-api.nhsv.vn/auth/auth-service/verify-otp" \
  -H "Content-Type: application/json" -d '{}'

# Refresh token (margin)
curl -X POST "https://nhsvpro-sv74-prod-api.nhsv.vn/auth/auth-service/refresh-token" \
  -H "Content-Type: application/json" -d '{}'

# Khởi tạo hợp đồng margin
curl -X POST "https://nhsvpro-sv74-prod-api.nhsv.vn/margin/v1/margin/init-contract" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Link ký hợp đồng
curl -X GET "https://nhsvpro-sv74-prod-api.nhsv.vn/margin/v1/margin/sign-links" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Thông tin tài khoản margin
curl -X GET "https://nhsvpro-sv74-prod-api.nhsv.vn/margin/v1/margin/account-information" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Danh sách tiểu khoản margin
curl -X GET "https://nhsvpro-sv74-prod-api.nhsv.vn/margin/v1/margin/sub-accounts" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## 20. Creator / Agent (nh-partner)

```bash
# Thông tin đại lý
curl -X GET "https://nhsvpro-sv74-prod-api.nhsv.vn/nh-partner/api/v1/agent/get-agent-info" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Kiểm tra trước khi ký HĐ
curl -X POST "https://nhsvpro-sv74-prod-api.nhsv.vn/nh-partner/api/v1/agent/validate-before-contract" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Tạo hợp đồng creator
curl -X POST "https://nhsvpro-sv74-prod-api.nhsv.vn/nh-partner/api/v1/agent/create-contract" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "Content-Type: application/json" -H "accept-language: vi" \
  -d '{}'

# Link hợp đồng
curl -X GET "https://nhsvpro-sv74-prod-api.nhsv.vn/nh-partner/api/v1/agent/get-contract-link" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"

# Danh sách ngân hàng (agent)
curl -X GET "https://nhsvpro-sv74-prod-api.nhsv.vn/nh-partner/api/v1/agent/get-bank-list" \
  -H "Authorization: jwt <ACCESS_TOKEN>" -H "accept-language: vi"
```

---

## Ghi chú

- **UAT:** Thay `nhsvpro.nhsv.vn` bằng `tnhsvpro.nhsv.vn`, `nhsvpro-sv74-prod-api.nhsv.vn` bằng `uat-edt-cust-info.nhsv.vn` (và path agent: `agent-nginx` thay `nh-partner`, auth margin: `open-margin-sub` thay `auth`) nếu test UAT.
- **Symbol Info (bulk):** App còn gọi `GET .../api/v2/market/symbolInfo` (từ saga) — URL đầy đủ: `https://nhsvpro.nhsv.vn/rest/api/v2/market/symbolInfo`.
- **BIDV (nạp tiền):** `getBidvAccessToken`, `getBidvVietQR` dùng `config.apiUrl.bidvURI` (trong config để trống); cần cấu hình `bidvURI` để dùng.
- **YouTube:** `getVideoList` dùng `youtube.googleapis.com` — không liệt kê trong tài liệu TradeX này.

---

## Phụ lục: Query params & Body theo interface (tham chiếu nhanh)

| API (key trong api.ts) | Method | Query params (GET) | Body (POST/PUT) |
|------------------------|--------|---------------------|-----------------|
| equityBuyable | GET | accountNumber, subNumber, stockCode, orderPrice | — |
| equitySellable | GET | accountNumber, subNumber, stockCode, fetchCount? | — |
| equityMargin | GET | accountNumber, subNumber, symbolCode | — |
| getOrderbook2 | GET | accountNumber, subNumber, matchType, fetchCount, deviceUniqueId, sellBuyType, nextKey?, lastOrderDate?, stockCode? | — |
| getOrderbook (Lotte) | GET | accountNumber, subNumber, fromDate?, toDate?, sellBuyType, matchType?, nextKey?, lastOrderDate?, fetchCount? | — |
| getOrderbookVirtual | GET | subAccount, fromDate?, toDate?, pageSize, pageNumber?, sellBuyType?, status? | — |
| placeNormalOrder | POST | — | IPlaceNormalOrderRequest |
| placeNormalOrderVirtual | POST | — | subAccount, code, quantity, price?, orderCommand, action |
| placeAdvanceOrder | POST | — | IPlaceAdvanceOrderRequest |
| placeStopOrder | POST | — | IPlaceStopOrderRequest |
| cancelNormalOrder | PUT | — | accountNumber, branchCode, orderNumber, subNumber? |
| cancelNormalOrderVirtual | POST | — | orderId (number) |
| cancelStopOrder | PUT | — | stopOrderId |
| modifyNormalOrder | PUT | — | IModifyOrderbookRequest |
| modifyNormalOrderVirtual | POST | — | orderId, newPrice, newQuantity |
| modifyStopOrder | PUT | — | stopOrderId, orderQuantity, stopPrice, orderPrice, fromDate, toDate, stockCode |
| getCashBalance | GET | accountNumber, subNumber | — |
| getAssetInfo | GET | accountNumber, subNumber | — |
| getAssetInfoVirtual | GET | subAccount | — |
| getProfitLoss | GET | accountNumber, subNumber | — |
| login | POST | — | ILoginRequest (platform, grant_type, client_id, client_secret, username, password?, device_id, session_time_in_minute, ...) |
| verifyOTP | POST | — | { otp } |
| getRealContestRankingList | GET | contestId (path), period?, withCondition?, pageNumber?, pageSize? | — |
| getRealContestCurrentRanking | GET | contestId (path), period, withCondition | — |
| getContestRanking | GET | contestId (path), period?, withCondition?, pageNumber?, pageSize? | — |
| getCurrentRanking | GET | contestId (path), period? | — |
| getIndexMarketLatest | GET | (không param bắt buộc) | — |
| getDataChart (tradingview/history) | GET | symbol, resolution, from, to | — |
| getSymbolBidOffer | GET | symbol (path) | — |
| ekycQueryBankBranchList | GET | bankCode (path) | — |
| ekycSendOTP | POST | — | id, idType: 'PHONE_NO', txType: 'E_KYC' |
| ekycVerifyOTP | POST | — | otpId, otpValue |
| getWatchlistList | GET | (không param) | — |
| postCreateWatchlist | POST | — | watchlist name / body theo backend |
| getSymbolListWatchlist | GET | (có thể watchlistId) | — |
| postAddSymbolListWatchlist | POST | — | watchlistId?, symbolCode? |
| deleteSymbolListWatchlist | DELETE | — | watchlistId?, symbolCode? (hoặc query) |
| getChatRoomDetail | GET | id (path) | — |
| getBrokerProfile | GET | id (path) | — |
| marketLeaderProfile | GET | marketLeaderId (path) | — |
| getRightsHistory | GET | type (path) | — |
| getConfirmOrders | GET | accountNumber, subNumber?, fromDate, toDate, confirmStatus?, marketType?, cancelType?, sellBuyType?, fetchCount?, nextKey? | — |
| confirmOrdersRequest | POST | — | accountNumber, subNumber?, orders: [{ orderNumber, orderDate }] |
| getConfirmLoansOnline | GET | accountNumber, subNumber?, fromDate, toDate, status?, fetchCount, nextKey? | — |
| confirmLoansRequestOnline | POST | — | accountNumber, subNumber, sequenceNumber, loanDate, matchDate, username, loanBankCode, loanAmount |
| getConfirmCashTransfersOnline | GET | accountNumber, subNumber?, fromDate, toDate, status?, fetchCount, nextKey? | — |
| confirmCashTransfersRequestOnline | POST | — | accountNumber, subNumber, sequenceNumber, date |
| getConfirmStockTransfersOnline | GET | accountNumber, subNumber?, fromDate, toDate, status?, fetchCount, nextKey?, stockCode? | — |
| confirmStockTransfersOnline | POST | — | accountNumber, sequenceNumber, date, stockCode |

*Các API khác: xem từng section trên hoặc `src/interfaces/authentication.ts`, `src/config/api.ts`, `screens/ConfirmOrdersScreen/ConfirmOrdersType.ts`.*

**Nguồn:** `nhsv-mts-rn` → `src/config/api.ts`, `src/config/index.tsx`, `src/interfaces/authentication.ts` (production).
