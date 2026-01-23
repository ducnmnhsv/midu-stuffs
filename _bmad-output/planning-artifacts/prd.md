---
stepsCompleted: ["step-01-init", "step-02-discovery", "step-03-success", "step-04-journeys", "step-05-domain", "step-06-innovation", "step-07-project-type", "step-08-scoping", "step-09-functional", "step-10-nonfunctional", "step-11-polish", "step-e-01-discovery", "step-e-02-review", "step-e-03-edit"]
inputDocuments: []
workflowType: 'prd'
documentCounts:
  briefs: 0
  research: 0
  brainstorming: 0
  projectDocs: 0
classification:
  projectType: mobile_app
  domain: fintech
  complexity: High
  projectContext: greenfield
lastEdited: '2026-01-23'
editHistory:
  - date: '2026-01-23'
    changes: 'Applied BMAD NFR templates, converted Compliance into a Matrix table, and added Fraud Prevention Strategy.'
---

# Product Requirements Document - Debug Logging Feature

**Author:** Ducnguyen
**Date:** 2026-01-23

## Executive Summary
To drastically reduce Bug Resolution Time (MTTR) for non-crash behavioral issues in the Tradex Monitoring mobile ecosystem. This feature enables a secure, opt-in mechanism for capturing high-fidelity user journeys—including navigation sequences and redacted API traffic—allowing engineers to reproduce elusive logic errors within minutes rather than days.

## Success Criteria

### User Success
- **Trust & Control:** 100% of users feel in control of their data via a clear opt-in/opt-out mechanism.
- **Transparent Experience:** Zero perceived latency or performance impact on trade execution when logging is active.
- **Resolution Speed:** Users receive confirmed bug fixes significantly faster due to the high-fidelity data provided.

### Business Success
- **Reduced MTTR:** Target a 50% reduction in average resolution time for non-crash bugs within the first 3 months.
- **Improved Bug Quality:** Increase the "Reliably Reproducible" rate of customer-reported issues to >90%.

### Technical Success
- **Zero Privacy Leakage:** Mandatory on-device Redaction Layer ensures no PII or sensitive financial secrets (passwords, tokens) are ever transmitted.
- **Storage Safety:** Circular ring buffer ensures local log storage never exceeds 2MB, protecting device health.

## Product Scope

### Phase 1: MVP (The Resolution Foundation)
- Opt-In toggle in App Settings.
- Automated capture of Navigation, API Request Params, and API Responses.
- "Shake to Report" and "Long-press version" hidden triggers.
- Basic Key-Value redaction logic.
- Manual upload to server with unique 6-digit Investigation ID generation.

### Phase 2: Growth (The Efficiency Layer)
- Smart Uploads (Wi-Fi priority).
- Advanced RegEx-based redaction engine.
- Automated 30-day data retention (TTL).

### Phase 3: Vision (The Predictive Future)
- Proactive on-device anomaly detection.
- Full "User Journey Replay" visualizer for support staff.

## User Journeys

### Journey 1: The Support-Guided Report
A trader (Minh) notices an incorrect profit display and contacts support. Support (Linh) asks him to enable "Detailed Logging." Minh reproduces the error and performs a "Shake-to-Report" gesture. A non-intrusive snackbar confirms the capture. He sends the 6-digit ID to the support agent, feeling confident in the professional resolution process.

### Journey 2: The Engineering Hand-off
Support receives the Investigation ID and escalates it. The Developer (Nam) uses the ID to retrieve the redacted JSON log. By seeing the exact sequence of screens and API responses leading to the error, he identifies the logic flaw immediately and pushes a fix without needing further user input.

## Domain-Specific Requirements (Fintech)

### Compliance Matrix

| Requirement | Description | Status | Notes |
| :--- | :--- | :--- | :--- |
| **Consent Audit Trail** | Mandatory logging of user opt-in/opt-out events with timestamps. | **Met** | Traceable via Audit logs. |
| **Data Residency** | Ensure logs are stored and processed in compliance with local laws. | **Met** | Vietnam-based storage mandated. |
| **Data Encryption** | AES-256 at rest (on-device) and TLS 1.3 in transit. | **Met** | Mandatory for all Fintech data. |
| **Fintech Redaction** | Masking of account numbers, trade volumes, and prices. | **Met** | Enforcement in redaction engine. |
| **Access Control** | MFA-enabled authorized internal account access only. | **Met** | Least privilege enforcement. |

## Mobile App Specific Requirements

| Feature | iOS Implementation | Android Implementation |
| :--- | :--- | :--- |
| **Network Interception** | `URLSessionProtocol` / Combine | `OkHttp` Network Interceptor |
| **Shake Gesture** | `UIWindow` shake responder | `SensorManager` (Accelerometer) |
| **Local Storage** | File system (Blobs) + `UserDefaults` | Internal Storage + `SharedPreferences` |

- **Offline Resilience:** 10MB dedicated storage for pending reports with a 7-day auto-purge for failed uploads.
- **Platform Permissions:** Required access to Accelerometer and Network State.

## Functional Requirements (Capability Contract)

### Consent & Capture
- **FR1:** Users can toggle detailed debug logging in Settings.
- **FR2:** The system captures the sequence of the last 50 navigation events.
- **FR3:** The system intercepts and log all API request parameters and response bodies.
- **FR4:** Every captured event must be timestamped with millisecond precision.

### Security & Redaction
- **FR5:** On-device redaction must mask all sensitive PII and Fintech-specific keys.
- **FR6:** "Deny-by-Default" logic must be applied to sensitive API data objects.
- **FR7:** Local log blobs must be encrypted and managed in a 2MB circular buffer.

### Fraud Prevention Strategy
- **Internal Access Control:** Log packages are accessible only via a session-linked token generated when a support ticket is active.
- **Support Snoop Protection:** Support agents cannot browse log buckets; they must possess a valid 6-digit Investigation ID.
- **Watchdog Audit:** Every "ID Lookup" event in the Support Portal is logged, identifying the agent, timestamp, and purpose.

### Triggering & Submission
- **FR8:** Detection of a high-intensity shake triggers a non-blocking "Snackbar" notification.
- **FR9:** Snackbar must auto-dismiss after 5 seconds if no user action is taken.
- **FR10:** Long-pressing the app version in the Side Menu serves as a secondary manual trigger.
- **FR11:** Successfully uploaded logs generate a unique, searchable 6-digit Investigation ID.

## Non-Functional Requirements

### Performance
- [**NFR1**] **UI Responsiveness:** [Criterion] Smooth UI interaction [Metric] <5ms main-thread overhead per event [Measurement Method] Profiler hooks during QA burn-in [Context] Trading efficiency is priority.
- [**NFR2**] **Resource Efficiency:** [Criterion] Low battery/thermal impact [Metric] <2% delta in CPU usage [Measurement Method] OS-level energy metrics [Context] Device longevity.

### Security
- [**NFR3**] **Data Encryption:** [Criterion] Data confidentiality [Metric] AES-256 and TLS 1.3 [Measurement Method] Security audit of crypto-headers [Context] Fintech compliance.
- [**NFR4**] **Redaction Performance:** [Criterion] Prevent memory leakage [Metric] <10ms redact-to-write latency [Measurement Method] Unit test with timestamped hooks [Context] Privacy by design.
- [**NFR5**] **Access Control:** [Criterion] Least privilege [Metric] MFA and session-token verification [Measurement Method] Audit log of Support Portal access [Context] Data privacy.

### Reliability
- [**NFR6**] **Fail-Safe Operation:** [Criterion] System stability [Metric] 0 app crashes attributed to logging service [Measurement Method] Crashlytics stack trace analysis [Context] Trade safety.
- [**NFR7**] **Storage Limits:** [Criterion] Disk health [Metric] Hard limit of 2MB per circular buffer [Measurement Method] Automated smoke tests [Context] Low-storage device protection.
- [**NFR8**] **Resilient Uploads:** [Criterion] Data persistence [Metric] 100% recovery of pending reports after network transition [Measurement Method] Integration test for Wi-Fi/Cellular switching [Context] Field report accuracy.
