# IDOR Security Remediation - Sprint Planning

> [!IMPORTANT]
> **Sprint Cycle**: 2 weeks per sprint  
> **Team Composition**: 2 Backend Devs + 1 DevOps + 1 QA + 0.5 Mobile Dev  
> **Estimated Velocity**: 20-25 story points per sprint  
> **Total Duration**: 3 sprints (6 weeks)

---

## Team Capacity & Assumptions

### Team Members
| Role | Name | Availability | Capacity (SP/Sprint) |
|------|------|--------------|---------------------|
| Backend Dev 1 | TBD | 100% | 10 SP |
| Backend Dev 2 | TBD | 100% | 10 SP |
| DevOps Engineer | TBD | 100% | 8 SP |
| QA Engineer | TBD | 100% | 5 SP |
| Mobile Dev | TBD | 50% | 3 SP |
| **Total Capacity** | | | **36 SP** |

### Velocity Adjustment
- **Buffer for meetings/reviews**: -20% = **~29 SP per sprint**
- **Security work complexity**: -15% = **~25 SP per sprint** (realistic)
- **Emergency context**: First sprint may be lower (~20 SP)

---

## Story Point Estimation Guide

### Fibonacci Scale Explanation
- **1 point**: Vài giờ, rất đơn giản (config change, documentation)
- **2 points**: Nửa ngày, đơn giản (simple API change)
- **3 points**: 1 ngày, trung bình (new endpoint with tests)
- **5 points**: 2-3 ngày, phức tạp (migration, integration)
- **8 points**: 4-5 ngày, rất phức tạp (major refactor, security implementation)
- **13 points**: 1-2 tuần, cực kỳ phức tạp (system-wide changes)

### Estimation Factors
- **Complexity**: Technical difficulty
- **Uncertainty**: Unknown requirements
- **Dependencies**: External systems
- **Testing**: QA effort required
- **Risk**: Potential production impact

---

## Sprint 1: Emergency Response (Days 1-14)
**Sprint Goal**: Deploy critical security patches to prevent immediate exploitation

**Capacity**: 20 SP (reduced due to emergency context)  
**Planned**: 18 SP (90% capacity for safety)

### Stories

#### SECURITY-001: Implement Server-Side Authorization for eKYC Endpoints
**Priority**: P0 - CRITICAL  
**Story Points**: 8  
**Assignee**: Backend Dev 1 (Lead)

**Estimation Breakdown**:
- Authorization middleware development: 2 SP
- Apply to all eKYC endpoints (8-10 endpoints): 2 SP
- Audit logging implementation: 1 SP
- Rate limiting setup: 1 SP
- Unit tests + integration tests: 1.5 SP
- Code review + fixes: 0.5 SP

**Tasks**:
- [ ] Design authorization middleware pattern (4h)
- [ ] Implement `authorizeEkycAccess` middleware (8h)
- [ ] Apply middleware to all eKYC routes (6h)
- [ ] Add audit logging for failed attempts (4h)
- [ ] Implement rate limiting (10 req/min per user) (4h)
- [ ] Write unit tests for middleware (6h)
- [ ] Write integration tests for cross-user access (6h)
- [ ] Code review and refinement (4h)
- [ ] Deploy to staging and test (2h)

**Acceptance Criteria**:
- [x] All eKYC endpoints have authorization checks
- [x] Cross-user access returns 403 Forbidden
- [x] Failed attempts logged to audit system
- [x] Rate limiting active (max 10 req/min)
- [x] Test coverage >80%
- [x] Performance impact <50ms per request

**Dependencies**: None  
**Risk**: Medium - Production deployment requires careful testing

---

#### SECURITY-002: Secure S3 File Server Access
**Priority**: P0 - CRITICAL  
**Story Points**: 5  
**Assignee**: Backend Dev 2 + DevOps

**Estimation Breakdown**:
- Remove S3 public access: 0.5 SP
- Implement pre-signed URL generation: 1.5 SP
- Add authorization before URL generation: 1 SP
- Watermarking implementation: 1 SP
- Access logging setup: 0.5 SP
- Testing: 0.5 SP

**Tasks**:
- [ ] Audit current S3 bucket permissions (2h)
- [ ] Remove public access from S3 bucket (1h)
- [ ] Implement pre-signed URL service (6h)
- [ ] Add authorization check before URL generation (4h)
- [ ] Implement image watermarking with user ID (4h)
- [ ] Configure S3 access logging (2h)
- [ ] Write tests for URL generation (4h)
- [ ] Test cross-user access attempts (2h)
- [ ] Verify watermarking on images (2h)
- [ ] Deploy and monitor (2h)

**Acceptance Criteria**:
- [x] S3 bucket is private (no public access)
- [x] Pre-signed URLs expire after 15 minutes
- [x] Authorization check before URL generation
- [x] All CCCD images have watermark with user ID
- [x] Access logs enabled and monitored
- [x] Cross-user access blocked

**Dependencies**: None  
**Risk**: Low - Mostly configuration changes

---

#### SECURITY-011: Forensic Investigation of Existing Breach
**Priority**: P0 - CRITICAL  
**Story Points**: 5  
**Assignee**: DevOps + QA (parallel work)

**Estimation Breakdown**:
- Log analysis setup: 1 SP
- Pattern detection and analysis: 2 SP
- Report generation: 1 SP
- Legal/compliance coordination: 1 SP

**Tasks**:
- [ ] Collect API access logs (last 6 months) (4h)
- [ ] Collect S3 access logs (last 6 months) (4h)
- [ ] Write script to detect suspicious patterns (8h)
- [ ] Analyze logs for cross-user access (8h)
- [ ] Identify potentially affected users (4h)
- [ ] Generate forensic report (6h)
- [ ] Coordinate with legal team (4h)
- [ ] Prepare user notification (if needed) (2h)

**Acceptance Criteria**:
- [x] All access logs analyzed (6 months)
- [x] Suspicious patterns identified
- [x] List of affected users (if any)
- [x] Forensic report completed
- [x] Legal team briefed
- [x] User notification prepared (if needed)

**Dependencies**: Access to production logs  
**Risk**: High - May discover actual breach

---

### Sprint 1 Summary
| Story | SP | Assignee | Days |
|-------|-----|----------|------|
| SECURITY-001 | 8 | Backend Dev 1 | 5 |
| SECURITY-002 | 5 | Backend Dev 2 + DevOps | 3 |
| SECURITY-011 | 5 | DevOps + QA | 3 |
| **Total** | **18** | | **~7 days** |

**Sprint 1 Deliverables**:
- ✅ Authorization checks deployed to production
- ✅ S3 bucket secured with pre-signed URLs
- ✅ Forensic investigation completed
- ✅ Emergency patches live

**Sprint 1 Risks**:
- Production deployment may require maintenance window
- Forensic investigation may reveal actual breach
- Team may need to work overtime for critical fixes

---

## Sprint 2: Integration Security & UUID Migration (Days 15-28)
**Sprint Goal**: Secure eContract integration and migrate to UUID-based identifiers

**Capacity**: 25 SP (normal velocity)  
**Planned**: 24 SP

### Stories

#### SECURITY-003: Implement UUID-Based Resource Identifiers
**Priority**: P0 - CRITICAL  
**Story Points**: 13  
**Assignee**: Backend Dev 1 + Backend Dev 2

**Estimation Breakdown**:
- Database schema changes: 2 SP
- Migration script development: 3 SP
- API endpoint updates: 3 SP
- Mobile app coordination: 2 SP
- Testing and validation: 2 SP
- Deployment and monitoring: 1 SP

**Tasks**:
- [ ] Design UUID migration strategy (4h)
- [ ] Add UUID column to eKYC table (2h)
- [ ] Write migration script (zero downtime) (12h)
- [ ] Test migration on staging data (8h)
- [ ] Update all API endpoints to accept UUID (12h)
- [ ] Maintain backward compatibility (both formats) (8h)
- [ ] Add database indexes for UUID (4h)
- [ ] Update mobile app to use UUID (8h - Mobile Dev)
- [ ] Write comprehensive tests (12h)
- [ ] Performance test UUID lookups (4h)
- [ ] Deploy migration to production (4h)
- [ ] Monitor and verify (4h)

**Acceptance Criteria**:
- [x] UUID column added to database
- [x] All existing records have UUIDs
- [x] API accepts both integer and UUID (transition)
- [x] Mobile app updated to use UUID
- [x] Zero downtime during migration
- [x] Performance maintained (<100ms lookup)
- [x] Rollback plan tested

**Dependencies**: SECURITY-001 (authorization must be in place first)  
**Risk**: High - Database migration with zero downtime

---

#### SECURITY-004: Secure eContract Third-Party Integration
**Priority**: P0 - CRITICAL  
**Story Points**: 8  
**Assignee**: Backend Dev 2

**Estimation Breakdown**:
- Server-to-server token exchange: 3 SP
- Token validation and binding: 2 SP
- CSRF protection: 1 SP
- Testing: 2 SP

**Tasks**:
- [ ] Design secure token exchange flow (4h)
- [ ] Implement server-side token generation (8h)
- [ ] Bind tokens to user ID and IP (4h)
- [ ] Implement token expiration (30 min) (2h)
- [ ] Add token revocation mechanism (4h)
- [ ] Implement CSRF protection for WebView (6h)
- [ ] Coordinate with eContract team (4h)
- [ ] Write integration tests (8h)
- [ ] Test token expiration and IP binding (4h)
- [ ] Load test token generation (4h)
- [ ] Deploy and monitor (2h)

**Acceptance Criteria**:
- [x] Server-to-server token exchange implemented
- [x] Tokens bound to user ID and IP address
- [x] Token expiration set to 30 minutes
- [x] CSRF protection active
- [x] Cross-user token usage blocked
- [x] Load tested (1000 tokens/min)

**Dependencies**: Coordination with eContract team  
**Risk**: Medium - Third-party integration complexity

---

#### SECURITY-005: Implement Request Signing for eContract API
**Priority**: P0 - CRITICAL  
**Story Points**: 3  
**Assignee**: Backend Dev 1

**Estimation Breakdown**:
- HMAC-SHA256 implementation: 1 SP
- Signature validation: 1 SP
- Testing and key rotation: 1 SP

**Tasks**:
- [ ] Implement HMAC-SHA256 signing (4h)
- [ ] Add timestamp to prevent replay attacks (2h)
- [ ] Implement signature validation (4h)
- [ ] Add signature key rotation (4h)
- [ ] Write tests for signature validation (4h)
- [ ] Test replay attack prevention (2h)
- [ ] Performance test signing overhead (2h)
- [ ] Deploy and monitor (2h)

**Acceptance Criteria**:
- [x] HMAC-SHA256 request signing implemented
- [x] Timestamp included in signature
- [x] Invalid signatures rejected
- [x] Replay attacks prevented
- [x] Key rotation mechanism working
- [x] Performance overhead <10ms

**Dependencies**: SECURITY-004  
**Risk**: Low - Standard cryptographic implementation

---

### Sprint 2 Summary
| Story | SP | Assignee | Days |
|-------|-----|----------|------|
| SECURITY-003 | 13 | Backend Dev 1 + 2 | 8 |
| SECURITY-004 | 8 | Backend Dev 2 | 5 |
| SECURITY-005 | 3 | Backend Dev 1 | 2 |
| **Total** | **24** | | **~10 days** |

**Sprint 2 Deliverables**:
- ✅ UUID migration completed
- ✅ eContract integration secured
- ✅ Request signing implemented
- ✅ All P0 security fixes deployed

**Sprint 2 Risks**:
- UUID migration complexity
- Third-party coordination delays
- Backward compatibility issues

---

## Sprint 3: Security Audit & Monitoring (Days 29-42)
**Sprint Goal**: Complete security audit, implement monitoring, and establish long-term security practices

**Capacity**: 25 SP  
**Planned**: 25 SP

### Stories

#### SECURITY-006: Complete API Security Audit
**Priority**: P1 - HIGH  
**Story Points**: 8  
**Assignee**: Backend Dev 1 + QA

**Estimation Breakdown**:
- Endpoint inventory: 1 SP
- Authorization audit: 3 SP
- Security test suite: 3 SP
- Report generation: 1 SP

**Tasks**:
- [ ] Create inventory of all API endpoints (4h)
- [ ] Audit each endpoint for authorization (12h)
- [ ] Identify sequential ID usage (4h)
- [ ] Document authorization model (6h)
- [ ] Create security test suite (12h)
- [ ] Run automated security tests (4h)
- [ ] Generate audit report (6h)
- [ ] Prioritize findings (2h)

**Acceptance Criteria**:
- [x] All endpoints inventoried
- [x] Authorization audit completed
- [x] Security test suite created
- [x] Audit report delivered
- [x] Remediation roadmap created

**Dependencies**: All P0 fixes deployed  
**Risk**: Low

---

#### SECURITY-007: Implement Automated Security Testing
**Priority**: P1 - HIGH  
**Story Points**: 5  
**Assignee**: QA + DevOps

**Estimation Breakdown**:
- OWASP ZAP integration: 2 SP
- CI/CD pipeline setup: 2 SP
- Documentation: 1 SP

**Tasks**:
- [ ] Install and configure OWASP ZAP (4h)
- [ ] Integrate ZAP into CI/CD pipeline (8h)
- [ ] Create IDOR test cases (6h)
- [ ] Add authorization tests (6h)
- [ ] Configure security gate (4h)
- [ ] Set up dependency scanning (4h)
- [ ] Create documentation (4h)
- [ ] Test and validate (4h)

**Acceptance Criteria**:
- [x] OWASP ZAP integrated in CI/CD
- [x] Automated IDOR tests running
- [x] Security gate blocks vulnerable code
- [x] Dependency scanning active
- [x] Documentation completed

**Dependencies**: SECURITY-006  
**Risk**: Low

---

#### SECURITY-008: Security Awareness Training
**Priority**: P1 - HIGH  
**Story Points**: 2  
**Assignee**: Security Team + All Devs

**Estimation Breakdown**:
- Training material creation: 1 SP
- Workshop delivery: 1 SP

**Tasks**:
- [ ] Create IDOR training materials (4h)
- [ ] Prepare OWASP Top 10 workshop (4h)
- [ ] Create secure coding guidelines (4h)
- [ ] Conduct training workshop (4h)
- [ ] Create code review checklist (2h)
- [ ] Track completion (2h)

**Acceptance Criteria**:
- [x] Training materials created
- [x] Workshop conducted
- [x] Secure coding guidelines published
- [x] Code review checklist in use
- [x] 100% team completion

**Dependencies**: None  
**Risk**: Low

---

#### SECURITY-009: Implement Security Monitoring & Alerting
**Priority**: P1 - HIGH  
**Story Points**: 8  
**Assignee**: DevOps + Backend Dev 2

**Estimation Breakdown**:
- Audit logging: 2 SP
- Alert configuration: 2 SP
- Dashboard creation: 2 SP
- SIEM integration: 2 SP

**Tasks**:
- [ ] Implement comprehensive audit logging (8h)
- [ ] Set up alert rules (8h)
- [ ] Create security dashboard in Grafana (8h)
- [ ] Integrate with SIEM (Splunk/ELK) (8h)
- [ ] Configure PagerDuty alerts (4h)
- [ ] Test alert conditions (4h)
- [ ] Document monitoring setup (4h)

**Acceptance Criteria**:
- [x] Audit logging for all auth failures
- [x] Alerts for suspicious activities
- [x] Security dashboard created
- [x] SIEM integration complete
- [x] PagerDuty alerts configured

**Dependencies**: SECURITY-001  
**Risk**: Low

---

#### SECURITY-010: Incident Response Plan
**Priority**: P1 - HIGH  
**Story Points**: 2  
**Assignee**: Security Team + Management

**Estimation Breakdown**:
- Documentation: 1 SP
- Drill and validation: 1 SP

**Tasks**:
- [ ] Document incident response procedures (4h)
- [ ] Define severity levels (2h)
- [ ] Create communication templates (4h)
- [ ] Establish team roles (2h)
- [ ] Create runbooks (4h)
- [ ] Conduct incident response drill (4h)

**Acceptance Criteria**:
- [x] Incident response playbook created
- [x] Communication templates ready
- [x] Team roles assigned
- [x] Runbooks documented
- [x] Drill completed

**Dependencies**: None  
**Risk**: Low

---

### Sprint 3 Summary
| Story | SP | Assignee | Days |
|-------|-----|----------|------|
| SECURITY-006 | 8 | Backend Dev 1 + QA | 5 |
| SECURITY-007 | 5 | QA + DevOps | 3 |
| SECURITY-008 | 2 | All Team | 1 |
| SECURITY-009 | 8 | DevOps + Backend Dev 2 | 5 |
| SECURITY-010 | 2 | Security + Management | 1 |
| **Total** | **25** | | **~10 days** |

**Sprint 3 Deliverables**:
- ✅ Complete security audit
- ✅ Automated security testing in CI/CD
- ✅ Team trained on security
- ✅ Monitoring and alerting active
- ✅ Incident response plan ready

---

## Overall Timeline Summary

```
┌─────────────────────────────────────────────────────────────┐
│                    6-Week Timeline                           │
├─────────────────────────────────────────────────────────────┤
│ Sprint 1 (Weeks 1-2): Emergency Response                    │
│ ├─ SECURITY-001: Authorization (8 SP)                       │
│ ├─ SECURITY-002: S3 Security (5 SP)                         │
│ └─ SECURITY-011: Forensics (5 SP)                           │
│ Total: 18 SP                                                 │
├─────────────────────────────────────────────────────────────┤
│ Sprint 2 (Weeks 3-4): Integration & Migration               │
│ ├─ SECURITY-003: UUID Migration (13 SP)                     │
│ ├─ SECURITY-004: eContract Security (8 SP)                  │
│ └─ SECURITY-005: Request Signing (3 SP)                     │
│ Total: 24 SP                                                 │
├─────────────────────────────────────────────────────────────┤
│ Sprint 3 (Weeks 5-6): Audit & Monitoring                    │
│ ├─ SECURITY-006: API Audit (8 SP)                           │
│ ├─ SECURITY-007: Automated Testing (5 SP)                   │
│ ├─ SECURITY-008: Training (2 SP)                            │
│ ├─ SECURITY-009: Monitoring (8 SP)                          │
│ └─ SECURITY-010: Incident Response (2 SP)                   │
│ Total: 25 SP                                                 │
└─────────────────────────────────────────────────────────────┘

Total Story Points: 67 SP (P0 + P1 only)
Total Duration: 6 weeks (3 sprints)
```

---

## Sprint Ceremonies

### Sprint Planning (Day 1 of each sprint - 2 hours)
**Agenda**:
1. Review sprint goal (15 min)
2. Review and clarify stories (45 min)
3. Break down stories into tasks (30 min)
4. Assign stories to team members (15 min)
5. Identify dependencies and risks (15 min)

**Attendees**: All team members + Product Owner

---

### Daily Standup (Every day - 15 minutes)
**Format**:
- What did I complete yesterday?
- What will I work on today?
- Any blockers or dependencies?

**Time**: 9:30 AM daily  
**Location**: Team room / Zoom

---

### Sprint Review (Last day of sprint - 1 hour)
**Agenda**:
1. Demo completed stories (30 min)
2. Show security improvements (15 min)
3. Gather stakeholder feedback (15 min)

**Attendees**: Team + Stakeholders + Management

---

### Sprint Retrospective (Last day of sprint - 1 hour)
**Format**:
1. What went well? (20 min)
2. What could be improved? (20 min)
3. Action items for next sprint (20 min)

**Attendees**: Team only

---

## Risk Management

### High-Priority Risks

| Risk | Impact | Probability | Mitigation | Owner |
|------|--------|-------------|------------|-------|
| Production deployment breaks existing functionality | High | Medium | Feature flags, gradual rollout, extensive testing | Backend Lead |
| UUID migration causes downtime | High | Low | Zero-downtime migration strategy, rollback plan | Backend Lead |
| Forensic investigation reveals actual breach | High | Medium | Legal team on standby, user notification prepared | Security Lead |
| eContract integration delays | Medium | Medium | Early coordination, parallel development | Backend Dev 2 |
| Team member unavailability | Medium | Low | Cross-training, documentation | PM |

---

## Definition of Done (DoD)

Each story is considered "Done" when:

- [ ] Code implemented and follows coding standards
- [ ] Peer review completed and approved
- [ ] Unit tests written and passing (>80% coverage)
- [ ] Integration tests passing in staging
- [ ] Security tests passing
- [ ] Performance tests passing (no degradation >10%)
- [ ] Documentation updated (README, API docs)
- [ ] Deployed to staging and validated
- [ ] Product Owner acceptance
- [ ] Demo prepared for sprint review

---

## Success Metrics

### Per Sprint
- **Velocity**: Actual vs planned story points
- **Quality**: Bug count, test coverage
- **Security**: Vulnerability count reduction
- **Performance**: API response time maintained

### Overall Project
- **Security**: Zero IDOR vulnerabilities
- **Performance**: <50ms authorization overhead
- **Coverage**: 100% of endpoints protected
- **Compliance**: GDPR/PDPA compliant

---

## Backlog (Future Sprints - P2)

These stories are lower priority and will be planned after Sprint 3:

| Story | SP | Priority | Description |
|-------|-----|----------|-------------|
| SECURITY-012: Rate Limiting | 5 | P2 | Comprehensive API rate limiting |
| SECURITY-013: Data Encryption at Rest | 13 | P2 | Encrypt PII in database and S3 |
| SECURITY-014: Field-Level Access Control | 8 | P2 | Granular access control for sensitive fields |

**Total Backlog**: 26 SP (~1 sprint)

---

## Communication Plan

### Daily
- Standup updates
- Slack updates in #security-remediation channel

### Weekly
- Sprint progress report to management
- Stakeholder status update (email)

### Per Sprint
- Sprint review demo
- Retrospective action items
- Next sprint planning

### Ad-hoc
- Critical issues escalated immediately
- PagerDuty alerts for security incidents

---

## Tools & Resources

### Development
- **IDE**: VS Code / IntelliJ
- **Version Control**: Git / GitHub
- **CI/CD**: Jenkins / GitHub Actions

### Testing
- **Unit Tests**: Jest / Mocha
- **Integration Tests**: Postman / Newman
- **Security Tests**: OWASP ZAP
- **Load Tests**: JMeter / k6

### Monitoring
- **Metrics**: Prometheus
- **Dashboards**: Grafana
- **Logging**: ELK Stack / Splunk
- **Alerts**: PagerDuty

### Collaboration
- **Project Management**: Jira
- **Communication**: Slack
- **Documentation**: Confluence / Notion
- **Video Calls**: Zoom / Google Meet

---

## Appendix: Story Point Reference

### Historical Data (for calibration)
Use these as reference points for estimation:

| Past Story | Actual SP | Actual Days | Notes |
|------------|-----------|-------------|-------|
| Add new API endpoint | 3 | 1 | Simple CRUD |
| Database migration | 8 | 4 | Complex schema change |
| Third-party integration | 5 | 2-3 | Moderate complexity |
| Security implementation | 8 | 4-5 | High complexity |

### Estimation Poker
Use Planning Poker for team consensus:
1. Present story
2. Team discusses
3. Each member votes (Fibonacci)
4. Discuss outliers
5. Re-vote until consensus

---

**Document Version**: 1.0  
**Created**: 2026-01-20  
**Last Updated**: 2026-01-20  
**Owner**: Product Manager  
**Next Review**: After each sprint
