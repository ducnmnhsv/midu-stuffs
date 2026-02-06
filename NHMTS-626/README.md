# NHMTS-626: Security Enhancement for eKYC and Storage Service

**Issue Type:** 🔒 Security - Critical IDOR Vulnerability  
**Status:** 🔄 In Review  
**Priority:** P0 (Critical)

---

## 📋 Issue Summary

Critical security vulnerability fix addressing **IDOR (Insecure Direct Object Reference)** in eKYC/eContract flows and public S3 bucket exposure.

### Security Impact

| Vulnerability | CVSS | Status |
|---------------|------|--------|
| IDOR in eKYC Access | 8.1 (High) | ✅ Fixed |
| Public S3 Bucket | 7.5 (High) | ✅ Fixed |

---

## 📁 Documentation

```
NHMTS-626/
├── README.md              # This file - Overview & quick guide
├── Test_Cases.md          # Complete test suite (27 test cases)
├── Implementation.md      # Technical details & code changes
└── FE_Requirements.md     # Frontend changes required
```

---

## 🔍 Problem & Solution

### Problem 1: IDOR Vulnerability

**Before:**
```http
GET /api/v1/equity/account/contracts?ekycId=ekyc_999
Authorization: Bearer {user_a_token}
```
❌ User A could access User B's contracts by guessing `ekycId`

**Solution:** Session-based access control với Redis
```
Redis: refresh_token_id → ekyc_id mapping (TTL: 1 hour)
```

### Problem 2: Public S3 Bucket

**Before:**
```bash
curl https://ekyc_images.s3.amazonaws.com/user_cmnd.jpg
```
❌ Anyone could download sensitive documents

**Solution:** Presigned URLs với action parameter
```http
# Upload
GET /api/v1/aws?serviceName=ekyc&key=file.jpg&action=upload

# Download  
GET /api/v1/aws?serviceName=ekyc&key=file.jpg&action=download
```

---

## 🚀 Implementation

### Backend Changes (Complete ✅)

**Repositories:**
- `ekyc-admin`: [c813d860](https://bitbucket.org/nhsv-dev/ekyc-admin/commits/c813d860a59bcabc25f552f286e56a74c8fd5510)
- `configuration`: [c25a051e](https://bitbucket.org/nhsv-dev/configuration/commits/c25a051e9a224c20597a0fb9deb687cd7369ad7d)

**Key Changes:**
1. Redis session management
2. Session-based authorization 
3. Presigned URL with `action` parameter
4. S3 bucket policy - block public access
5. Security audit logging

See: [`Implementation.md`](./Implementation.md) for details

### Frontend Changes (Required ⚠️)

**Impact:** FE must update image upload/download logic

See: [`FE_Requirements.md`](./FE_Requirements.md) for complete guide

**Quick Summary:**
```typescript
// OLD (No action parameter)
const url = await api.get('/aws?serviceName=ekyc&key=cmnd.jpg');

// NEW (Add action parameter)
// For upload
const uploadUrl = await api.get('/aws?serviceName=ekyc&key=cmnd.jpg&action=upload');

// For download
const downloadUrl = await api.get('/aws?serviceName=ekyc&key=cmnd.jpg&action=download');
```

---

## 🧪 Testing

### Test Coverage

- **Total:** 27 test cases
- **P0 (Critical):** 15 tests
- **P1 (High):** 7 tests  
- **P2 (Medium):** 5 tests

### Critical Tests

| ID | Description | Priority |
|----|-------------|----------|
| TC_A1.2 | Block unauthorized eKYC access | P0 ⭐ |
| TC_B2.4 | Block direct S3 access | P0 ⭐ |
| TC_B1.3 | Upload via presigned URL | P0 ⭐ |
| TC_F1 | Backward compatibility | P0 ⭐ |

**Run tests:**
```bash
npm run test -- --suite=NHMTS-626
```

See: [`Test_Cases.md`](./Test_Cases.md) for full suite

---

## 📊 Deployment Status

### Backend
- [x] Code changes complete
- [x] PR created: [#3](https://bitbucket.org/nhsv-dev/configuration/pull-requests/3)
- [ ] Code review (Lee Boram, Nguyễn Trung Hiếu)
- [ ] Security review
- [ ] QA testing (UAT)
- [ ] Production deployment

### Frontend
- [ ] FE requirements shared
- [ ] FE implementation
- [ ] FE testing
- [ ] FE deployment

### Infrastructure
- [ ] S3 bucket policy applied
- [ ] Redis cluster configured
- [ ] Monitoring setup

---

## 🔐 Security Validation

**Pre-Deployment:**
- [ ] SAST scan (no critical issues)
- [ ] DAST scan
- [ ] Penetration testing
- [ ] OWASP Top 10 compliance

**Post-Deployment (24h monitoring):**
- [ ] No unauthorized access attempts
- [ ] S3 403 errors within threshold
- [ ] Performance metrics acceptable
- [ ] Backward compatibility verified

---

## 📈 Monitoring

### Metrics
- Unauthorized eKYC access attempts
- Direct S3 access attempts (403 errors)
- API response time (P95 < 500ms)
- Redis operations (< 50ms P99)

### Alerts
- **Critical:** Redis failures, S3 policy changes
- **High:** Spike in 403 errors, slow responses
- **Medium:** Daily security summary

---

## 📞 Contacts

**Development Team:**
- Developer: Lê Văn Tí Nho
- Reviewers: Lee Boram, Nguyễn Trung Hiếu

**Support:**
- Slack: #nhmts-626
- Email: security@nhsv.vn

---

## 🔗 Links

| Resource | Link |
|----------|------|
| **PR** | [Configuration #3](https://bitbucket.org/nhsv-dev/configuration/pull-requests/3) |
| **Commits** | [ekyc-admin](https://bitbucket.org/nhsv-dev/ekyc-admin/commits/c813d860a59bcabc25f552f286e56a74c8fd5510) · [configuration](https://bitbucket.org/nhsv-dev/configuration/commits/c25a051e9a224c20597a0fb9deb687cd7369ad7d) |
| **Tests** | [Test_Cases.md](./Test_Cases.md) |
| **Implementation** | [Implementation.md](./Implementation.md) |
| **FE Guide** | [FE_Requirements.md](./FE_Requirements.md) |

---

**Last Updated:** 2026-02-06  
**Next Review:** After production deployment
