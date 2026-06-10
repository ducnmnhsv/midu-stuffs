# Implementation Details - NHMTS-626

**Issue:** NHMTS-626 - Security Enhancement  
**Date:** 2026-02-06  
**Revised:** 2026-02-09 (aligned with `configuration` & `ekyc-admin` repos)

---

## 📝 Overview

This document describes technical implementation for the security fixes, **mapped to the actual codebase** in:

- **configuration:** `TradeX MCP/Knowledge based/configuration` — Kafka consumer (TypeScript), exposes logic for `/api/v1/aws` and admin AWS.
- **ekyc-admin:** `TradeX MCP/Knowledge based/ekyc-admin` — Java (JHipster), Kafka consumer for eKYC/contract flows, REST for admin/custom eKYC APIs.

---

## 🔧 Backend Architecture (current)

### configuration (TypeScript, Kafka)

- **Entry:** Kafka messages with `message.uri` (e.g. `/api/v1/aws`) → `src/consumers/RequestHandler.ts` → `apiMap[message.uri]`.
- **AWS presigned:**  
  - `/api/v1/aws` → `amazonWebService.getSignedDataToUploadPublic(data)`  
  - `/api/v1/admin/aws` → `adminAmazonWebService.getSignedDataToUploadInternal(data)`
- **Request model:** `src/models/request/IAWSGetSignedDataRequest.ts` — extends `IDataRequest`, fields: `key`, `serviceName?`. **No `action` field in current code.**
- **Services:**  
  - `src/services/AmazonWebService.ts` — `getSignedDataToUploadPublic()` only (upload; uses AWS or Minio `presignedPutObject`).  
  - `src/services/admin/AdminAmazonWebService.ts` — `getSignedDataToUploadInternal()` only.
- **Config:** `src/config.ts` — `config.aws.s3` (e.g. `public`, `langResource`, `announcement`). Client can pass `serviceName` to select bucket/conf. No Redis in configuration.

### ekyc-admin (Java, Kafka + REST)

- **Kafka:** `consumers/RequestHandler.java` handles:
  - `/api/v1/equity/account/contracts` → `eContractCustomService.signEContract(request)` (payload: `FptECSignRequest`).
  - `/api/v1/ekycs`, `/api/v1/ekyc-admin/ekyc/add`, sendOtp, verifyOtp, eContractStatus, etc.
- **REST (JHipster):**  
  - `web/rest/CustomEKycResource.java` (`@RequestMapping("/api/v1")`):  
    - `GET /ekyc-admin/ekyc/e-contract-info/{id}`, `GET /ekyc-admin/ekyc/e-contract/download/{id}`, `GET /ekyc-admin/e-kycs`, etc.  
  - `web/rest/EKycResource.java` → `/api/e-kycs`, `EContractResource` → `/api/e-contracts`.
- **Redis:** `dao/RedisDao.java` — OTP only (`KIS_E_KYC_OTP_*`, `OtpValidation`). **No session mapping (refresh_token_id → ekyc_id) in current code.**

---

## Part A: Session-Based Access Control (NHMTS-626 target)

### 1. Redis session mapping (to be added)

**Current:** ekyc-admin uses Redis for OTP only.

**Target:** Store session mapping for contract access, e.g.:

- Key: `refresh_token_id` (or equivalent from gateway/token).
- Value: `ekyc_id` (or allowed eKYC identifier).
- TTL: e.g. 1 hour.

**Suggested location:** Either in **ekyc-admin** (extend `RedisDao` or new service) or in the **API gateway** that calls ekyc-admin. If in ekyc-admin, then in `RequestHandler.java` for `/api/v1/equity/account/contracts` (or in `EContractCustomService.signEContract`):

- Resolve `refresh_token_id` from request/token.
- `RedisDao.get(sessionKey)` → allowed ekyc_id.
- If request `ekycId` (or equivalent) != allowed ekyc_id → return 403 / ACCESS_DENIED and log security event.

### 2. Contract / eKYC authorization (target)

**Current:** `RequestHandler.java` calls `eContractCustomService.signEContract(request)` for `/api/v1/equity/account/contracts` with no session/ekycId check.

**Target:** Before returning contract data (or signing):

- For client_credential (or similar) flow: require `ekycId` and validate against Redis session mapping.
- For password_otp / biometric: use identifier from JWT (e.g. identifierId) and do not rely on client-supplied ekycId alone.

Audit log: log `UNAUTHORIZED_EKYC_ACCESS` (requestedEkycId, allowedEkycId, sessionId).

---

## Part B: S3 / Presigned URL (NHMTS-626 target)

### 1. Presigned URL with `action` parameter

**Current (configuration):**

- `AmazonWebService.getSignedDataToUploadPublic(request)` — upload only.
- `IAWSGetSignedDataRequest`: `key`, `serviceName` (optional).

**Target:**

- Add optional `action` to request (or query): `upload` | `download`.
- In `RequestHandler.ts` apiMap for `/api/v1/aws`: pass `action` through to service.
- In `AmazonWebService` (and/or admin service if used for ekyc):
  - `action === 'download'` (or missing and key exists) → generate **getObject** presigned URL.
  - Otherwise → keep current **putObject** (upload) behavior.
- Validate `action` enum; reject invalid values with 400.
- Key validation: length, no `..`, no `<>"'&` (path traversal / XSS).

**File-level changes (configuration):**

- `src/models/request/IAWSGetSignedDataRequest.ts` — add optional `action?: 'upload' | 'download'`.
- `src/services/AmazonWebService.ts` — branch on `action`, call AWS/Minio for getObject when `download`.
- `src/consumers/RequestHandler.ts` — ensure `data.action` passed from message to service.

(If presigned URLs are exposed via **rest-proxy** rather than directly from configuration, the same contract — `action=upload` / `action=download` — should be implemented in the layer that calls configuration.)

### 2. S3 bucket policy and public access block

**Current:** configuration uses `config.aws.s3` (e.g. bucket names, region). No Terraform in the configuration repo under review.

**Target:**

- For eKYC/S3 buckets (e.g. ekyc_images):  
  - Block public access (block_public_acls, ignore_public_acls, block_public_policy, restrict_public_buckets).  
  - Deny s3:GetObject for principal `*` unless via presigned URL (no public reads).
- Apply via Terraform (or existing infra repo) if that’s where S3 is managed; otherwise document required bucket policy and public-access-block settings.

---

## 🔒 Security (target)

- **Input validation:** serviceName whitelist; key regex/length/no `..`/no `<>"'&`; action enum.
- **Audit logging:** log security events (e.g. UNAUTHORIZED_EKYC_ACCESS, invalid action).
- **Rate limiting:** optional on presigned URL generation and session creation (e.g. at gateway or in configuration/ekyc-admin if applicable).

---

## 📦 Dependencies

- **configuration:** Already uses AWS SDK / Minio; if getObject presigner is not present, add the minimal dependency for presigned getObject.
- **ekyc-admin:** Redis already used (RedisDao); add only if new session keys are stored in ekyc-admin.

---

## 🌍 Environment / config

- **configuration:** `config.aws`, `config.aws.s3.*` (bucket, region, expires). Optional: `S3_PRESIGNED_URL_EXPIRES` (e.g. 900).
- **ekyc-admin:** Redis already configured for OTP; if session mapping is in ekyc-admin, reuse same Redis (with distinct key prefix).

---

## 🚀 Deployment

- **configuration:** Build and deploy as usual (Kafka consumer); ensure message payload includes `action` when FE/gateway adopts new API.
- **ekyc-admin:** Build and deploy; if session check is added, ensure gateway passes token/session info and that Redis is available.
- **Infrastructure:** Apply S3 bucket policy and public access block (e.g. Terraform in infra repo) before or with backend release.

---

## 📊 Performance (target)

- Presigned URL generation: keep P95 within acceptable range (e.g. &lt; 500 ms).
- Redis session lookup: &lt; 50 ms P99 when used.

---

## 🔄 Rollback

- Revert configuration/ekyc-admin commits and redeploy.
- Revert S3 policy changes if direct public access was re-enabled for emergency rollback (not recommended long-term).

---

**Last Updated:** 2026-02-09 (aligned with configuration & ekyc-admin codebase)
