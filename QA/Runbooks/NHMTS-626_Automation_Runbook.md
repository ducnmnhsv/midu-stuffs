# NHMTS-626 – Automation Test Runbook

**Ticket:** NHMTS-626 – Security Enhancement for eKYC and Storage Service  
**Collection (request test):** TradeX QA session – `34274942-8fe5bddd-fce2-4f76-bb6f-fb3f2760d40a`  
**Folder:** NHMTS-626 (tạo trong Postman UI nếu chưa có; request có thể tạm ở root). Index: `QA sessions/Postman_Index.json`.  
**API tham chiếu (main):** TradeX API v2 – `34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d`  
**Revised:** 2026-02-09

---

## Test data (cố định)

| Resource    | Value          | Usage                    |
|------------|----------------|--------------------------|
| **eKYC-ID** | `82305`        | Contracts, session tests  |
| **CCCD (identifierId)** | `010098000023` | checkNationalId, user identity |

---

## Requests đã tạo trong Postman

Tất cả request nằm trong collection **TradeX QA session** (`34274942-8fe5bddd-fce2-4f76-bb6f-fb3f2760d40a`), trong folder **NHMTS-626** (hoặc tạm ở root). Tên bắt đầu bằng **NHMTS-626 -**. Xem `QA sessions/Postman_Index.json` → `requests_by_issue.NHMTS-626` để lấy request IDs.

### Part A: eKYC & Contract Security

| Request name | Method | URL / mô tả | Test case | Expected |
|--------------|--------|-------------|-----------|----------|
| **NHMTS-626 - Contracts (eKycId=82305)** | GET | `/rest/api/v1/equity/account/contracts?eKycId=82305` | TC-A1, TC-A2 | 200 + contract list (nếu session đúng eKycId) |
| **NHMTS-626 - Contracts IDOR (wrong eKycId - expect 403)** | GET | `?eKycId=99999` | TC-A2 | 403, ACCESS_DENIED |
| **NHMTS-626 - Contracts missing eKycId (expect 400)** | GET | `/rest/api/v1/equity/account/contracts` (no query) | TC-A4 | 400, INVALID_PARAMETER |
| **Check ID** (sẵn có trong eKYC) | POST | `/rest/api/v1/equity/account/checkNationalId` | Pre-condition | Body: `{"identifierId":"010098000023"}` — dùng request "Check ID", sửa body thành CCCD trên. |

### Part B: Storage (Presigned URL)

| Request name | Method | URL / mô tả | Test case | Expected |
|--------------|--------|-------------|-----------|----------|
| **NHMTS-626 - AWS presigned backward compat (no action)** | GET | `/rest/api/v1/aws?serviceName=ekyc&key=nhmts626_test.jpg` | TC-B1 | 200, upload URL (backward compat) |
| **NHMTS-626 - AWS presigned upload (action=upload)** | GET | `.../aws?serviceName=ekyc&key=nhmts626_test_upload.jpg&action=upload` | TC-B2, TC-B3 | 200, upload URL |
| **NHMTS-626 - AWS presigned download (action=download)** | GET | `.../aws?serviceName=ekyc&key=010098000023_frontImage.jpg&action=download` | TC-B5, TC-B6 | 200, download URL (file mẫu: [010098000023_frontImage.jpg](https://tnhsvbackendpro.nhsv.vn/ekyc-images/010098000023_frontImage.jpg)) |
| **NHMTS-626 - AWS invalid action (expect 400)** | GET | `.../aws?serviceName=ekyc&key=test.jpg&action=delete` | TC-B9 | 400, INVALID_PARAMETER |

**API `/rest/api/v1/aws`:** Truyền query param **`action`** = `upload` hoặc `download`. Ví dụ: `?serviceName=ekyc&key=010098000023_frontImage.jpg&action=download`. File ảnh mẫu: `key=010098000023_frontImage.jpg` (tương ứng https://tnhsvbackendpro.nhsv.vn/ekyc-images/010098000023_frontImage.jpg).

**Sửa URL trong Postman (nếu cần):** Request **AWS presigned action=upload** có thể có typo `{}{baseUrl}}`; sửa thành `{{baseUrl}}/rest/api/v1/aws?serviceName=ekyc&key=...&action=upload`. Request **action=download** dùng `key=010098000023_frontImage.jpg`.

---

## Lấy accessToken trước khi chạy

- **Nguồn:** Collection **TradeX API v2** (main) – folder **Login** (folder id: `2afb22a3-d401-4fa6-af78-2485359ee3aa`).
- **Cách làm:** Trong Postman, mở collection **TradeX API v2** → folder **Login** → chạy request **Login** (POST `/rest/api/v1/login`) với environment **TradeX UAT** → từ response copy `accessToken` → paste vào biến `accessToken` của environment **TradeX UAT**. Sau đó chạy collection **TradeX QA session** với cùng environment.

## Thứ tự chạy gợi ý (automation)

1. **Login** (request Login trong folder Login của main collection) → lấy `accessToken`, set vào environment.
2. **NHMTS-626 - Check National ID (CCCD 010098000023)** → kiểm tra CCCD hợp lệ.
3. **NHMTS-626 - Contracts (eKycId=82305)** → kiểm tra access đúng session.
4. **NHMTS-626 - Contracts IDOR (wrong eKycId - expect 403)** → expect 403.
5. **NHMTS-626 - Contracts missing eKycId (expect 400)** → expect 400.
6. **NHMTS-626 - AWS presigned backward compat (no action)** → expect 200, body có `url`.
7. **NHMTS-626 - AWS presigned upload (action=upload)** → expect 200.
8. **NHMTS-626 - AWS presigned download (action=download)** → 200 nếu file đã upload; 404/4xx nếu chưa có file.
9. **NHMTS-626 - AWS invalid action (expect 400)** → expect 400.

---

## Environment

- **UAT:** `baseUrl` = `https://tnhsvpro.nhsv.vn`  
- **Production:** `baseUrl` = `https://nhsvpro.nhsv.vn`  
- Dùng environment **TradeX UAT** hoặc **TradeX Production** (xem `QA sessions/Environment_Setup.md`).

---

## Scripts / assertions gợi ý (Postman Tests)

- **Contracts (eKycId=82305):** `pm.test("Status 200", () => pm.response.to.have.status(200));`
- **IDOR (eKycId=99999):** `pm.test("Status 403", () => pm.response.to.have.status(403));` và kiểm tra body có `ACCESS_DENIED` hoặc tương đương.
- **Missing eKycId:** `pm.test("Status 400", () => pm.response.to.have.status(400));`
- **AWS presigned:** `pm.test("Status 200", () => pm.response.to.have.status(200));` và `pm.test("Has url", () => { const j = pm.response.json(); pm.expect(j).to.have.property('url'); });`
- **AWS invalid action:** `pm.test("Status 400", () => pm.response.to.have.status(400));`

---

## Tài liệu tham chiếu

- Test cases đầy đủ: `NHMTS-626/Test_Cases.md`
- Implementation: `NHMTS-626/Implementation.md`
- FE requirements: `NHMTS-626/FE_Requirements.md`
- Repos: **configuration** (presigned URL), **ekyc-admin** (contracts, Redis session)
