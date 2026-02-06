# Frontend Requirements - NHMTS-626

**Issue:** NHMTS-626 - S3 Security Enhancement  
**Impact:** 🔴 **Breaking Change** - FE must update image upload/download logic  
**Priority:** P0 (Critical)  
**Assigned To:** Frontend Team  
**Due Date:** Before backend production deployment

---

## 📋 Executive Summary

### Background

The backend has implemented security enhancements to prevent unauthorized access to eKYC/eContract images stored in S3.

**Current situation:**
- FE uploads images via presigned URLs ✅
- FE downloads images **directly from S3** ❌ (Security risk)
- S3 bucket `ekyc_images` is publicly accessible ❌

**Security issue:**
```
https://ekyc_images.s3.amazonaws.com/user_cmnd_front.jpg
```
☝️ Anyone can access this URL = **Data breach risk**

### Solution Required

Backend has added security controls. **FE must adapt** to work with the new secure API.

**What FE needs to do:**
1. Add `action` parameter to presigned URL API calls
2. Use presigned URLs for **both upload AND download**
3. Remove all direct S3 URLs from codebase

---

## 🎯 Requirements

### Requirement 1: Add `action` Parameter to API Calls

**Current API (No change to endpoint):**
```
GET /api/v1/aws?serviceName=ekyc&key=btn_backImage.jpg
```

**New Required Parameter:**
| Parameter | Type | Required | Values | Default |
|-----------|------|----------|--------|---------|
| `action` | string | No* | `upload`, `download` | `upload` |

**Behavior:**
- `action=upload` → Returns presigned URL for uploading
- `action=download` → Returns presigned URL for downloading
- No `action` or `action=""` → Returns upload URL (backward compatible)

**Response format unchanged:**
```json
{
  "url": "https://ekyc_images.s3.amazonaws.com/file.jpg?AWSAccessKeyId=xxx&Signature=xxx&Expires=xxx"
}
```

---

### Requirement 2: Use Presigned URLs for Downloads

**Current behavior (MUST CHANGE):**
```
❌ Image src points directly to S3:
src="https://ekyc_images.s3.amazonaws.com/file.jpg"
```

**Required behavior:**
```
✅ Get presigned download URL from API:
1. Call: GET /api/v1/aws?serviceName=ekyc&key=file.jpg&action=download
2. Use returned URL as image src
```

**Impact:**
- ALL image display components must be updated
- Affects: eKYC forms, document preview, profile images, etc.

---

### Requirement 3: Remove Direct S3 URLs

**FE must audit and remove:**
- Hardcoded S3 URLs in code
- Direct S3 URLs in image src attributes
- S3 URLs stored in localStorage/sessionStorage
- S3 URLs in API request/response handling

**Search patterns to find:**
- `ekyc_images.s3.amazonaws.com`
- `s3.ap-southeast-1.amazonaws.com/ekyc_images`
- Any direct S3 bucket access

---

## 📐 Functional Specifications

### User Story 1: Upload Image

**As a** user  
**I want to** upload my CMND/CCCD images  
**So that** I can complete eKYC

**Acceptance Criteria:**
1. User selects image file from device
2. FE calls API with `action=upload` to get presigned URL
3. FE uploads image to presigned URL
4. Upload success → FE stores image key (NOT URL)
5. Upload failure → Show error message, allow retry

**Technical Requirements:**
- Must call: `/api/v1/aws?serviceName=ekyc&key={filename}&action=upload`
- Upload via PUT to presigned URL
- Timeout: 30 seconds
- Max file size: 10MB
- Supported formats: JPEG, PNG

---

### User Story 2: Display Image

**As a** user  
**I want to** view my uploaded documents  
**So that** I can verify the information

**Acceptance Criteria:**
1. FE needs to display image from stored key
2. FE calls API with `action=download` to get presigned URL
3. FE displays image using presigned URL
4. Image loads within 3 seconds
5. If presigned URL expires (15 min) → Regenerate automatically

**Technical Requirements:**
- Must call: `/api/v1/aws?serviceName=ekyc&key={stored_key}&action=download`
- Use returned URL for img src
- Handle URL expiration gracefully
- Show loading placeholder while fetching URL
- Show error state if URL generation fails

---

### User Story 3: Handle URL Expiration

**As a** user  
**I want** images to reload automatically if URL expires  
**So that** I don't see broken images

**Acceptance Criteria:**
1. Presigned URL expires after 15 minutes
2. If image fails to load (403 error) → Regenerate URL
3. Retry automatically (max 3 attempts)
4. If all retries fail → Show error message

**Technical Requirements:**
- Detect 403 Forbidden error
- Implement exponential backoff (1s, 2s, 4s)
- Max 3 retry attempts
- Show user-friendly error after max retries

---

## 🔄 Migration Requirements

### Phase 1: Update Upload Logic

**Scope:** All components that upload images

**Changes needed:**
- Add `action=upload` parameter to API calls
- No other changes (upload logic remains same)

**Priority:** P1 (Can be done first, non-breaking)

**Affected areas:**
- eKYC form (CMND front/back upload)
- eContract signature upload
- Profile photo upload
- Any other image upload flows

---

### Phase 2: Update Download Logic (CRITICAL)

**Scope:** All components that display images

**Changes needed:**
- Replace direct S3 URLs with presigned URL API calls
- Add `action=download` parameter
- Implement URL expiration handling

**Priority:** P0 (MUST complete before backend deployment)

**Affected areas:**
- Document preview screens
- Image galleries
- Thumbnail displays
- PDF generation (if includes images)

---

### Phase 3: Cleanup (POST-DEPLOYMENT)

**Scope:** Remove all S3 references

**Changes needed:**
- Remove hardcoded S3 URLs
- Update constants/config files
- Clean up unused code

**Priority:** P2 (After successful deployment)

---

## ⚠️ Important Considerations

### 1. Presigned URL Lifespan

**Issue:** Presigned URLs expire after 15 minutes

**FE must handle:**
- Don't cache presigned URLs long-term
- Regenerate URL if image load fails
- Don't store presigned URLs in persistent storage

**Recommendation:**
- Generate URLs on-demand (when image needs display)
- Implement retry logic for expired URLs

---

### 2. Performance Impact

**Expected changes:**
- Extra API call needed to get download URL
- Slight delay before image displays (~200-500ms)

**FE should:**
- Show loading states
- Batch URL generation where possible
- Lazy load images (generate URL only when needed)

---

### 3. Error Handling

**New error scenarios:**

| Error | HTTP Code | Cause | FE Action |
|-------|-----------|-------|-----------|
| Invalid action | 400 | Wrong `action` value | Show error, log to Sentry |
| File not found | 404 | Invalid key | Show "Image not available" |
| URL expired | 403 | Presigned URL expired | Regenerate URL (auto retry) |
| Rate limited | 429 | Too many requests | Wait and retry |

---

### 4. Backward Compatibility

**During transition period:**
- Backend supports both old and new API usage
- Old API calls (no `action` param) still work
- FE can update gradually

**After backend fully deployed:**
- Direct S3 access will be **blocked** (403 Forbidden)
- FE **must** use presigned URLs or images won't load

---

## ✅ Acceptance Criteria

### Must Have (P0)

- [ ] All image uploads use `action=upload` parameter
- [ ] All image displays use `action=download` parameter
- [ ] No direct S3 URLs in codebase
- [ ] URL expiration handled gracefully
- [ ] Error states properly displayed
- [ ] Loading states shown during URL generation

### Should Have (P1)

- [ ] Performance optimized (batch requests where possible)
- [ ] Images lazy loaded
- [ ] Retry logic implemented
- [ ] All error scenarios handled

### Nice to Have (P2)

- [ ] Preload frequently accessed images
- [ ] Cache image keys (not URLs)
- [ ] Analytics for failed image loads

---

## 🧪 Testing Requirements

### Unit Tests

- [ ] API call with `action=upload` returns upload URL
- [ ] API call with `action=download` returns download URL
- [ ] Invalid `action` value throws error
- [ ] URL expiration triggers retry logic

### Integration Tests

- [ ] Upload image flow end-to-end
- [ ] Display image flow end-to-end
- [ ] URL expiration and regeneration
- [ ] Error handling for all scenarios

### E2E Tests

- [ ] Complete eKYC flow with image upload/preview
- [ ] Image display across different screens
- [ ] Retry logic on expired URLs
- [ ] Error messages display correctly

### Manual Testing Checklist

- [ ] Upload CMND front → Success
- [ ] Upload CMND back → Success
- [ ] View uploaded images → Display correctly
- [ ] Wait 16 minutes → Images reload automatically
- [ ] Test with slow network → Loading states show
- [ ] Test with network error → Error message shows
- [ ] Test upload large file (>10MB) → Proper error
- [ ] Test invalid file type → Proper error

---

## 📊 Definition of Done

### Code Complete

- [ ] All upload components updated
- [ ] All display components updated
- [ ] Direct S3 URLs removed
- [ ] Error handling implemented
- [ ] Loading states implemented

### Quality Assurance

- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] E2E tests pass
- [ ] Manual testing complete
- [ ] No console errors
- [ ] No broken images

### Documentation

- [ ] Code comments added
- [ ] API usage documented
- [ ] Component props documented
- [ ] Known issues documented

### Deployment Ready

- [ ] Code review approved
- [ ] QA sign-off received
- [ ] Performance acceptable
- [ ] No regressions found

---

## 📅 Timeline

| Phase | Tasks | Duration | Status |
|-------|-------|----------|--------|
| **Phase 1: Analysis** | Review requirements, identify affected components | 1 day | ⏳ Pending |
| **Phase 2: Implementation** | Update upload/download logic | 2-3 days | ⏳ Pending |
| **Phase 3: Testing** | Unit + Integration + E2E tests | 1-2 days | ⏳ Pending |
| **Phase 4: UAT** | Deploy to UAT, manual testing | 1 day | ⏳ Pending |
| **Phase 5: Production** | Deploy to production | 1 day | ⏳ Pending |

**Total estimated time:** 6-8 business days

**⚠️ Critical deadline:** Must complete before backend production deployment

---

## 🚨 Risks & Mitigation

### Risk 1: Breaking Changes in Production

**Risk:** If FE not updated before backend deploys, images won't load

**Mitigation:**
- Backend provides backward compatibility period
- Coordinate deployment timing
- Have rollback plan ready

### Risk 2: Performance Degradation

**Risk:** Extra API call for download URL may slow down image loading

**Mitigation:**
- Implement lazy loading
- Batch URL generation
- Show loading states
- Monitor performance metrics

### Risk 3: URL Expiration Issues

**Risk:** Users may see broken images if URL expires

**Mitigation:**
- Implement automatic retry
- Clear error messages
- Regenerate URL on 403 error

---

## 📞 Support & Questions

### Technical Contacts

**Backend Team:**
- Developer: Lê Văn Tí Nho
- Backend API questions → Slack #nhmts-626

**Product Team:**
- PM: [Name]
- Requirements clarification → Slack #product

**QA Team:**
- QA Lead: [Name]
- Testing coordination → Slack #qa

### Communication Channels

- **Daily updates:** Slack #nhmts-626
- **Blockers:** Tag @backend-team or @pm
- **Questions:** Post in #nhmts-626 or DM Lê Văn Tí Nho

---

## 📚 Additional Resources

### API Documentation

- Endpoint: `/api/v1/aws`
- Environment: 
  - UAT: `https://tnhsvpro.nhsv.vn/rest`
  - Production: `https://nhsvpro.nhsv.vn/rest`

### Backend References

- Backend PR: [#3](https://bitbucket.org/nhsv-dev/configuration/pull-requests/3)
- Implementation details: See `Implementation.md` in NHMTS-626 folder
- Test cases: See `Test_Cases.md` in NHMTS-626 folder

### External Documentation

- [AWS S3 Presigned URLs](https://docs.aws.amazon.com/AmazonS3/latest/userguide/PresignedUrlUploadObject.html)
- [OWASP Secure Storage](https://cheatsheetseries.owasp.org/cheatsheets/Secure_Cloud_Architecture_Cheatsheet.html)

---

## ✍️ Sign-off

### Frontend Team

- [ ] Requirements reviewed and understood
- [ ] Scope and timeline agreed
- [ ] Technical approach approved
- [ ] Ready to start implementation

**FE Lead:** _________________ Date: _______

### Product Team

- [ ] Requirements approved
- [ ] Acceptance criteria agreed
- [ ] Timeline acceptable

**PM:** _________________ Date: _______

### QA Team

- [ ] Test requirements reviewed
- [ ] Test plan approved
- [ ] Resources allocated

**QA Lead:** _________________ Date: _______

---

**Document Version:** 2.0 (Requirements Only)  
**Created:** 2026-02-06  
**Status:** ✅ Ready for FE Implementation  
**Next Review:** After FE estimates provided
