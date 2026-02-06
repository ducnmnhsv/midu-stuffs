# Test Cases - NHMTS-626

**Issue:** NHMTS-626 - Security Enhancement for eKYC and Storage Service  
**Test Lead:** QA Team  
**Created:** 2026-02-06  
**Status:** Ready for Execution

---

## 📊 Test Summary

| Metric | Value |
|--------|-------|
| **Total Test Cases** | 27 |
| **P0 (Critical)** | 15 |
| **P1 (High)** | 7 |
| **P2 (Medium)** | 5 |
| **Automated** | 12 |
| **Manual** | 15 |

---

## 🎯 Test Coverage by Category

| Category | Count | P0 | P1 | P2 |
|----------|-------|----|----|-----|
| **Part A: eKYC Security** | 8 | 4 | 2 | 2 |
| **Part B: Storage Security** | 11 | 7 | 2 | 2 |
| **Part C: Integration** | 2 | 2 | 0 | 0 |
| **Part D: Security** | 4 | 0 | 3 | 1 |
| **Part E: Performance** | 2 | 0 | 0 | 2 |

---

## ⚙️ Test Environment

| Item | Value |
|------|-------|
| **UAT API** | `https://tnhsvpro.nhsv.vn/rest` |
| **Production API** | `https://nhsvpro.nhsv.vn/rest` |
| **S3 Bucket** | `ekyc_images` |
| **Region** | `ap-southeast-1` |
| **Test User A** | Phone: 0901234567, ID: ID001 |
| **Test User B** | Phone: 0909876543, ID: ID002 |

---

## 📋 Part A: eKYC & Contract Security (8 TCs)

| Test Case ID | Test Case Name | Description | Preconditions | Test Steps | Expected Results | Actual Result | Status | Priority | Create Issue | Notes |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|----------|--------------|-------|
| **TC-A1** | Valid session access eKYC contract | Kiểm tra user có session hợp lệ có thể access contract của mình | - User hoàn thành eKYC flow<br>- Redis có mapping: `refresh_token_123` → `ekyc_456`<br>- User có valid client_credential token | 1. Login với `grant_type=client_credential`<br>2. Lấy `refresh_token_id` = `refresh_token_123`<br>3. Verify Redis: `GET refresh_token_123` → `ekyc_456`<br>4. Call `GET /api/v1/equity/account/contracts?ekycId=ekyc_456` | - HTTP Status: `200 OK`<br>- Response chứa contract list<br>- Log: "Session accessed successfully" | | | P0 ⭐ | | |
| **TC-A2** | Block unauthorized eKYC access | Kiểm tra user A KHÔNG thể access eKYC của user B | - User A: `refresh_token_123` → `ekyc_456`<br>- User B: `refresh_token_789` → `ekyc_999`<br>- User A đã login | 1. Login as User A (`refresh_token_123`)<br>2. Attempt: `GET /contracts?ekycId=ekyc_999`<br>3. Verify Redis: `GET refresh_token_123` → `ekyc_456`<br>4. Check security log | - HTTP Status: `403 Forbidden`<br>- Error code: `ACCESS_DENIED`<br>- Security log ghi nhận unauthorized attempt<br>- Không có data leakage | | | P0 ⭐ | | |
| **TC-A3** | Session expired in Redis | Kiểm tra xử lý khi session đã hết hạn | - Session `refresh_token_123` đã expire/bị xóa khỏi Redis | 1. Verify Redis: `GET refresh_token_123` → `(nil)`<br>2. Call `GET /contracts?ekycId=ekyc_456` | - HTTP Status: `401 Unauthorized`<br>- Error: `SESSION_EXPIRED`<br>- Message: "Please login again" | | | P1 | | |
| **TC-A4** | Missing ekycId parameter | Kiểm tra validation khi thiếu ekycId (client_credential flow) | - User login với client_credential | 1. Call `GET /contracts` (không truyền ekycId) | - HTTP Status: `400 Bad Request`<br>- Error code: `INVALID_PARAMETER`<br>- Error message: "ekycId is required" | | | P2 | | |
| **TC-A5** | User access own contracts (password_otp) | Kiểm tra user với password_otp access contracts | - User có tài khoản với `identifierId=ID001`<br>- User có OTP hợp lệ | 1. Login với `grant_type=password_otp`<br>2. Decode JWT → verify `identifierId=ID001`<br>3. Call `GET /contracts` (không cần ekycId) | - HTTP Status: `200 OK`<br>- Response chứa contracts của `identifierId=ID001`<br>- Không check Redis session | | | P0 | | |
| **TC-A6** | Biometric OTP authentication | Kiểm tra authentication với biometric | - User đã enable biometric<br>- Device có FaceID/TouchID | 1. Request biometric challenge<br>2. Verify biometric (FaceID/TouchID)<br>3. Login với `grant_type=biometric_otp`<br>4. Call `GET /contracts` | - Biometric authentication thành công<br>- JWT token chứa đúng identifierId<br>- Contracts retrieved successfully | | | P0 | | |
| **TC-A7** | Invalid JWT token | Kiểm tra xử lý JWT token không hợp lệ | - Có expired/invalid JWT token | **Scenario 1:** Expired token<br>**Scenario 2:** Invalid signature<br>**Scenario 3:** Malformed token | - HTTP Status: `401 Unauthorized`<br>- Error code: `INVALID_TOKEN`<br>- Message: "Invalid or expired token" | | | P1 | | |
| **TC-A8** | Grant type validation | Kiểm tra validation grant type không hợp lệ | - User đã login | 1. Gửi request với grant_type không hợp lệ | - HTTP Status: `400 Bad Request`<br>- Error: "Invalid grant type" | | | P2 | | |

---

## 📋 Part B: Storage Service Security (11 TCs)

| Test Case ID | Test Case Name | Description | Preconditions | Test Steps | Expected Results | Actual Result | Status | Priority | Create Issue | Notes |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|----------|--------------|-------|
| **TC-B1** | Get upload URL (backward compatible) | Kiểm tra API không có action parameter vẫn hoạt động (backward compatible) | None | 1. Call `GET /api/v1/aws?serviceName=ekyc&key=test.jpg` (không có action)<br>2. Parse response URL<br>3. Verify URL có query params | - HTTP Status: `200 OK`<br>- Response: `{"url": "https://..."}`<br>- URL có: AWSAccessKeyId, Signature, Expires<br>- URL dùng cho upload (putObject) | | | P0 | | |
| **TC-B2** | Get upload URL (action=upload) | Kiểm tra API với action=upload | None | 1. Call `GET /aws?serviceName=ekyc&key=test.jpg&action=upload`<br>2. Verify response | - HTTP Status: `200 OK`<br>- URL behavior giống TC-B1<br>- URL dùng cho upload | | | P0 | | |
| **TC-B3** | Upload image via presigned URL | Kiểm tra upload image thành công qua presigned URL | - Có test image: `cmnd_front.jpg` (2MB, JPEG) | 1. Get presigned upload URL<br>2. Upload: `PUT {presignedUrl}` với image file<br>3. Verify: `aws s3 ls s3://ekyc_images/test.jpg`<br>4. Check file metadata | - Upload success: `200 OK` hoặc `204 No Content`<br>- File tồn tại trong S3<br>- Metadata đúng: 2MB, Content-Type: image/jpeg | | | P0 ⭐ | | |
| **TC-B4** | Presigned upload URL expiration | Kiểm tra presigned URL hết hạn sau 15 phút | - Có presigned upload URL | 1. Get presigned upload URL<br>2. Wait 16 minutes (hoặc modify Expires param)<br>3. Attempt upload với expired URL | - Upload failed<br>- HTTP Status: `403 Forbidden`<br>- S3 Error: "Request has expired" | | | P1 | | |
| **TC-B5** | Get download URL | Kiểm tra lấy presigned URL để download | - File `test.jpg` đã tồn tại trong S3 | 1. Call `GET /aws?serviceName=ekyc&key=test.jpg&action=download`<br>2. Parse response | - HTTP Status: `200 OK`<br>- Response: `{"url": "https://..."}`<br>- URL dùng cho getObject (download)<br>- URL có TTL 15 phút | | | P0 | | |
| **TC-B6** | Download image via presigned URL | Kiểm tra download image qua presigned URL | - File exists trong S3<br>- Có presigned download URL | 1. Get presigned download URL<br>2. Download: `GET {presignedUrl}`<br>3. Verify image data<br>4. Compare MD5 hash với original | - HTTP Status: `200 OK`<br>- Content-Type: image/jpeg<br>- Downloaded image = original image (same hash) | | | P0 ⭐ | | |
| **TC-B7** | Presigned download URL expiration | Kiểm tra download URL hết hạn | - Có presigned download URL | 1. Get presigned download URL<br>2. Wait 16 minutes<br>3. Attempt download | - HTTP Status: `403 Forbidden`<br>- S3 Error: "Request has expired" | | | P1 | | |
| **TC-B8** | Block direct S3 access | Kiểm tra chặn direct access vào S3 bucket | None | 1. Direct access: `curl https://ekyc_images.s3.amazonaws.com/test.jpg`<br>2. Verify bucket policy<br>3. Test in browser<br>4. Test via wget/IDM | - HTTP Status: `403 Forbidden`<br>- Error: "Access Denied"<br>- Không có image data trong response<br>- CloudWatch alarm triggered | | | P0 ⭐ | | |
| **TC-B9** | Invalid action parameter | Kiểm tra validation action parameter | None | **Scenario 1:** `action=delete`<br>**Scenario 2:** `action=""`<br>**Scenario 3:** `action=upload'; DROP TABLE` | - HTTP Status: `400 Bad Request`<br>- Error code: `INVALID_PARAMETER`<br>- Message: "Invalid action. Allowed: upload, download" | | | P2 | | |
| **TC-B10** | S3 public access block enabled | Kiểm tra S3 public access block config | - AWS CLI configured | 1. Check: `aws s3api get-public-access-block --bucket ekyc_images`<br>2. Check bucket ACL<br>3. Check bucket policy | - BlockPublicAcls: `true`<br>- IgnorePublicAcls: `true`<br>- BlockPublicPolicy: `true`<br>- RestrictPublicBuckets: `true`<br>- Bucket ACL: Private | | | P0 | | |
| **TC-B11** | Presigned URL cannot be reused for different file | Kiểm tra presigned URL chỉ dùng cho 1 file cụ thể | - Có presigned URL cho `file1.jpg` | 1. Get presigned URL for `file1.jpg`<br>2. Upload `file1.jpg` successfully<br>3. Attempt upload `file2.jpg` using same URL | - Upload failed hoặc uploaded với wrong name<br>- S3 enforces key matching | | | P2 | | |

---

## 📋 Part C: Integration Tests (2 TCs)

| Test Case ID | Test Case Name | Description | Preconditions | Test Steps | Expected Results | Actual Result | Status | Priority | Create Issue | Notes |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|----------|--------------|-------|
| **TC-C1** | Complete eKYC flow with image upload | Kiểm tra toàn bộ flow eKYC từ đầu đến cuối | - User chưa có eKYC<br>- Có test images (CMND front/back) | 1. Create eKYC: `POST /api/v1/ekycs`<br>2. Get upload URL for CMND front<br>3. Upload CMND front image<br>4. Get upload URL for CMND back<br>5. Upload CMND back image<br>6. Get contracts: `GET /contracts?ekycId={id}`<br>7. Get download URLs for images<br>8. Display images in UI | - All steps complete successfully<br>- eKYC record created<br>- Images uploaded và accessible<br>- Contract data retrieved<br>- Images display correctly<br>- Total flow time < 10 seconds | | | P0 ⭐ | | |
| **TC-C2** | Sign contract with biometric after eKYC | Kiểm tra ký hợp đồng với biometric sau khi hoàn thành eKYC | - eKYC đã hoàn thành<br>- Account đã tạo với identifierId | 1. Complete eKYC flow<br>2. Account created<br>3. Login with biometric_otp<br>4. Get contracts (no ekycId needed)<br>5. Sign contract | - Biometric login successful<br>- JWT chứa correct identifierId<br>- Contracts retrieved using identifierId<br>- Contract signing successful | | | P0 | | |

---

## 📋 Part D: Security Tests (4 TCs)

| Test Case ID | Test Case Name | Description | Preconditions | Test Steps | Expected Results | Actual Result | Status | Priority | Create Issue | Notes |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|----------|--------------|-------|
| **TC-D1** | SQL injection in ekycId | Kiểm tra chống SQL injection | None | Test các payloads:<br>1. `' OR '1'='1`<br>2. `' UNION SELECT * FROM users--`<br>3. `'; DROP TABLE ekycs; --`<br>4. URL encoded payloads<br>5. Double encoded payloads | - All payloads rejected: `400 Bad Request`<br>- Error: "Invalid parameter format"<br>- Không có SQL query nào được execute<br>- WAF logs attack attempt | | | P1 | | |
| **TC-D2** | XSS in S3 key | Kiểm tra chống XSS attack | None | Test các payloads:<br>1. `<script>alert('xss')</script>.jpg`<br>2. `image.jpg" onerror="alert('xss')`<br>3. `<svg onload=alert('xss')>.jpg` | - All payloads rejected: `400 Bad Request`<br>- Error: "Key contains invalid characters"<br>- Key validation rejects: `<`, `>`, `"`, `'`, `&` | | | P1 | | |
| **TC-D3** | Path traversal in S3 key | Kiểm tra chống path traversal | None | Test các payloads:<br>1. `../../etc/passwd`<br>2. `..%2F..%2Fetc%2Fpasswd`<br>3. `%252e%252e%252f` (double encoded)<br>4. `..\..\\windows\system32` | - All payloads rejected: `400 Bad Request`<br>- Error: "Path traversal detected"<br>- Key validation rejects: `..`, `../`, `..\`, `%2e%2e` | | | P1 | | |
| **TC-D4** | Rate limiting on presigned URL generation | Kiểm tra rate limiting | None | 1. Call presigned URL API 100 times in 1 second<br>2. Count success vs rate limited responses | - First N requests: `200 OK`<br>- After threshold: `429 Too Many Requests`<br>- Error: "Rate limit exceeded"<br>- Rate limit reset sau 1 phút | | | P2 | | |

---

## 📋 Part E: Performance Tests (2 TCs)

| Test Case ID | Test Case Name | Description | Preconditions | Test Steps | Expected Results | Actual Result | Status | Priority | Create Issue | Notes |
|--------------|----------------|-------------|---------------|------------|------------------|---------------|--------|----------|--------------|-------|
| **TC-E1** | Concurrent presigned URL generation | Kiểm tra performance với nhiều concurrent requests | None | 1. Simulate 50 concurrent users<br>2. Each user requests presigned URL<br>3. Measure response times<br>4. Check for errors | - All 50 requests complete successfully<br>- Total time < 2 seconds<br>- Each URL is unique<br>- P95 response time < 500ms<br>- P99 response time < 1000ms<br>- No errors | | | P2 | | Metrics:<br>- Total time: ___ ms<br>- Avg: ___ ms<br>- P95: ___ ms<br>- P99: ___ ms |
| **TC-E2** | Redis session lookup performance | Kiểm tra Redis performance với nhiều sessions | - 10,000 session records loaded vào Redis | 1. Load 10,000 sessions vào Redis<br>2. Measure single lookup time<br>3. Test concurrent 100 lookups<br>4. Check memory usage | - Single lookup: < 10ms<br>- Average lookup: < 50ms<br>- Concurrent 100 lookups: < 100ms total<br>- No performance degradation<br>- Redis memory usage reasonable | | | P2 | | Metrics:<br>- Single: ___ ms<br>- Avg: ___ ms<br>- Concurrent: ___ ms<br>- Memory: ___ MB |

---

## 📊 Test Execution Summary

| Date | Tester | Environment | P0 Pass | P1 Pass | P2 Pass | Total Pass | Pass Rate | Status |
|------|--------|-------------|---------|---------|---------|------------|-----------|--------|
| ___ | ___ | UAT | ___/15 | ___/7 | ___/5 | ___/27 | ___% | ⏳ Pending |
| ___ | ___ | Production | ___/15 | ___/7 | ___/5 | ___/27 | ___% | ⏳ Pending |

**Pass Criteria:**
- **UAT:** All P0 tests must pass (15/15 = 100%)
- **Production:** All P0 + P1 tests must pass (22/22 = 100%)

---

## 🐛 Defects Log

| Defect ID | Test Case | Severity | Priority | Description | Status | Assigned To | Fixed Date | Retest Result |
|-----------|-----------|----------|----------|-------------|--------|-------------|------------|---------------|
| BUG-001 | ___ | Critical/High/Medium/Low | P0/P1/P2 | ___ | Open/Fixed/Closed | ___ | ___ | Pass/Fail |

---

## 📋 Test Data Reference

### Test Users

| User | Phone | identifierId | refreshTokenId | ekycId | Password |
|------|-------|--------------|----------------|--------|----------|
| User A | 0901234567 | ID001 | refresh_token_123 | ekyc_456 | Test@123 |
| User B | 0909876543 | ID002 | refresh_token_789 | ekyc_999 | Test@456 |

### Test Files

| File Name | Size | Type | MD5 Hash | Purpose |
|-----------|------|------|----------|---------|
| cmnd_front.jpg | 2MB | JPEG | abc123... | Valid upload test |
| cmnd_back.jpg | 2MB | JPEG | def456... | Valid upload test |
| large_file.jpg | 15MB | JPEG | ghi789... | Size limit test |
| invalid.txt | 1KB | TXT | jkl012... | File type validation |

### Redis Commands

```bash
# Setup test data
redis-cli SET "refresh_token_123" "ekyc_456" EX 3600
redis-cli SET "refresh_token_789" "ekyc_999" EX 3600

# Verify
redis-cli GET "refresh_token_123"
redis-cli TTL "refresh_token_123"

# Cleanup
redis-cli DEL "refresh_token_123"
```

### S3 Commands

```bash
# List files
aws s3 ls s3://ekyc_images/

# Upload test file
aws s3 cp test.jpg s3://ekyc_images/test.jpg

# Check file
aws s3api head-object --bucket ekyc_images --key test.jpg

# Delete test file
aws s3 rm s3://ekyc_images/test.jpg
```

---

## ✅ Pre-Test Checklist

- [ ] Test environment accessible (UAT/Production)
- [ ] Test accounts created và active
- [ ] Redis cluster accessible
- [ ] AWS CLI configured
- [ ] Test images prepared (CMND front/back)
- [ ] Postman collection imported
- [ ] Test data setup complete
- [ ] All testers briefed

---

## ✅ Post-Test Checklist

- [ ] All test cases executed
- [ ] Test results documented
- [ ] Defects logged in Jira/tracking system
- [ ] Pass/fail rates calculated
- [ ] Test summary report created
- [ ] Retesting completed for all fixes
- [ ] Sign-off received from stakeholders
- [ ] Test artifacts archived

---

## 📝 Test Sign-off

| Role | Name | Signature | Date | Comments |
|------|------|-----------|------|----------|
| **QA Lead** | ___ | ___ | ___ | All P0 tests passed: ☐ Yes ☐ No |
| **Dev Lead** | ___ | ___ | ___ | All defects fixed: ☐ Yes ☐ No |
| **Security Team** | ___ | ___ | ___ | Security tests passed: ☐ Yes ☐ No |
| **Product Manager** | ___ | ___ | ___ | Approved for deployment: ☐ Yes ☐ No |

---

**Document Version:** 3.0 (Single Table Format)  
**Last Updated:** 2026-02-06  
**Next Review:** After test execution complete
