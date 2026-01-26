---
stepsCompleted: [1, 2, 3, 4]
inputDocuments: ["/Users/ducnguyen/Documents/project/tradex-monitoring/_bmad-output/planning-artifacts/prd.md", "/Users/ducnguyen/Documents/project/tradex-monitoring/_bmad-output/planning-artifacts/ux-design-specification.md"]
---

# tradex-monitoring - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for tradex-monitoring, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Users can toggle detailed debug logging in Settings.
FR2: The system captures the sequence of the last 50 navigation events.
FR3: The system intercepts and log all API request parameters and response bodies.
FR4: Every captured event must be timestamped with millisecond precision.
FR5: On-device redaction must mask all sensitive PII and Fintech-specific keys.
FR6: "Deny-by-Default" logic must be applied to sensitive API data objects.
FR7: Local log blobs must be encrypted and managed in a 2MB circular buffer.
FR8: Detection of a high-intensity shake triggers a non-blocking "Snackbar" notification.
FR9: Snackbar must auto-dismiss after 5 seconds if no user action is taken.
FR10: Long-pressing the app version in the Side Menu serves as a secondary manual trigger.
FR11: Successfully uploaded logs generate a unique, searchable 6-digit Investigation ID.

### NonFunctional Requirements

NFR1: Smooth UI interaction <5ms main-thread overhead per event.
NFR2: Low battery/thermal impact <2% delta in CPU usage.
NFR3: AES-256 at rest (on-device) and TLS 1.3 in transit.
NFR4: <10ms redact-to-write latency.
NFR5: MFA and session-token verification for Support Portal log access.
NFR6: 0 app crashes attributed to logging service.
NFR7: Hard limit of 2MB per circular buffer.
NFR8: 100% recovery of pending reports after network transition.

### Additional Requirements

- Compliance Target: WCAG 2.1 Level AA.
- Haptic Signatures: Heartbeat pulse for Trigger, Impact pulse for Completion.
- Mobile Layout: Bottom Sheet (75% height) for ID presentation.
- Tablet Layout: Modal Dialog (400px width) instead of full-width sheet.
- Typography: Monospace fonts for diagnostic data and Investigation ID.
- Trigger Alternative: Trigger button in Settings for users with limited mobility.

### FR Coverage Map

- FR1: Epic 1 - Settings Toggle.
- FR2: Epic 2 - Navigation Capture.
- FR3: Epic 2 - API Interception.
- FR4: Epic 2 - Millisecond Precision.
- FR5: Epic 1 - Redaction Engine.
- FR6: Epic 1 - Deny-by-Default Privacy.
- FR7: Epic 1 - Encrypted Local Buffer.
- FR8: Epic 3 - Shake-to-Report Trigger.
- FR9: Epic 3 - Feedback Snackbar.
- FR10: Epic 3 - Side Menu Alternative Trigger.
- FR11: Epic 3 - Investigation ID Generation.

## Epic List

### Epic 1: User Control & Privacy Configuration
Allow users to opt-in to the diagnostic ecosystem with full transparency, ensuring all captured data is redacted and encrypted before local storage.
**FRs covered:** FR1, FR5, FR6, FR7.

### Epic 2: Silent Watchdog Capture Engine
Implement the background capture mechanism that monitors navigation events and API traffic with millisecond precision and zero performance overhead.
**FRs covered:** FR2, FR3, FR4.

### Epic 3: Gesture Trigger & Investigation ID Hand-off
Provide the bridge between a detected bug and the engineering team through physical triggers and unique Investigation ID generation.
**FRs covered:** FR8, FR9, FR10, FR11.

---

## Epic 1: User Control & Privacy Configuration

Allow users to opt-in to the diagnostic ecosystem with full transparency, ensuring all captured data is redacted and encrypted before local storage.

### Story 1.1: Settings Toggle & Consent Persistence
As an active trader,
I want to enable or disable detailed debug logging in my app settings,
So that I have full control over when my diagnostic data is being recorded.

**Acceptance Criteria:**
*   **Given** I am on the "Privacy & Security" settings screen
*   **When** I toggle the "Detailed Debug Logging" switch to ON
*   **Then** the system must record the opt-in event with a millisecond timestamp
*   **And** the state must persist across app restarts using local storage.

### Story 1.2: The Redaction Engine (Core Logic)
As a privacy-conscious user,
I want the system to automatically mask my PII and financial tokens before saving any logs,
So that my sensitive data never leaves my device.

**Acceptance Criteria:**
*   **Given** the debug logging is enabled
*   **When** a log entry contains keys like `password`, `token`, `account_number`, or `price`
*   **Then** the engine must mask the values with `*******`
*   **And** the redaction must happen in-memory (<10ms latency) before being written to disk.

### Story 1.3: Encrypted Circular Buffer (2MB)
As a system engineer,
I want to manage local logs in an encrypted 2MB circular buffer,
So that the app's storage footprint remains small and data is secure at rest.

**Acceptance Criteria:**
*   **Given** a new log entry is ready to be written
*   **When** the total local log size reaches the 2MB hard limit
*   **Then** the system must purge the oldest entries to make room (Ring Buffer logic)
*   **And** all data written to disk must be encrypted using AES-256.

## Epic 2: Silent Watchdog Capture Engine

Implement the background capture mechanism that monitors navigation events and API traffic with millisecond precision and zero performance overhead.

### Story 2.1: Navigation Event Listener
As an engineer,
I want to capture the sequence of the last 50 navigation events,
So that I can see the exact path the user took before encountering an issue.

**Acceptance Criteria:**
*   **Given** logging is enabled
*   **When** the user transitions between screens or closes a modal
*   **Then** the system must record the screen name and a millisecond timestamp
*   **And** the buffer must maintain only the last 50 events.

### Story 2.2: API Traffic Interceptor
As a developer,
I want to intercept and log all API request parameters and response bodies,
So that I can reproduce logic errors caused by specific server responses.

**Acceptance Criteria:**
*   **Given** logging is enabled
*   **When** a network request is initiated by the app
*   **Then** the interceptor must capture the URL, headers (sanitized), and payload
*   **And** it must capture the response body after redaction.

### Story 2.3: Resource Efficiency & Performance
As a trader,
I want the logging engine to have zero impact on my trading execution speed,
So that I am never at a disadvantage during high market volatility.

**Acceptance Criteria:**
*   **Given** active logging is recording API traffic
*   **When** performance metrics are measured
*   **Then** the main-thread overhead must be <5ms per event
*   **And** CPU usage delta must not exceed 2%.

## Epic 3: Gesture Trigger & Investigation ID Hand-off

Provide the bridge between a detected bug and the engineering team through physical triggers and unique Investigation ID generation.

### Story 3.1: High-Intensity Shake Detection (In Settings)
As an active trader,
I want to trigger a diagnostic report by shaking my device while in the Settings screen,
So that I can initiated a support request in a controlled environment.

**Acceptance Criteria:**
*   **Given** I am on the "More > Settings" screen
*   **When** I perform a high-intensity shake gesture
*   **Then** the device must provide a "Heartbeat" haptic pulse signature
*   **And** it must instantly display a simple "Consent Popup" asking for permission to send logs.

### Story 3.2: Explicit User Consent & Redaction UI
As a user,
I want to see a clear explanation of what is being sent and give my consent,
So that I am confident that my privacy is respected.

**Acceptance Criteria:**
*   **Given** the Consent Popup is displayed
*   **When** I tap "Cho phép và Gửi"
*   **Then** the system must initiate redaction and show the "Shield" scanning animation on a non-blocking snackbar
*   **And** it must auto-dismiss after 5 seconds if not further interacted with.
*   **When** I tap "Bỏ qua"
*   **Then** the popup must close and no data should be processed or sent.

### Story 3.3: Investigation ID Generation & Bottom Sheet
As a support user,
I want to receive a unique 6-digit Investigation ID in a clear format,
So that I can share it with the support agent.

**Acceptance Criteria:**
*   **Given** the redaction and upload process is complete
*   **When** I tap the success snackbar
*   **Then** a 75% height Bottom Sheet must open showing the 6-digit ID (e.g., 824-102)
*   **And** the ID must use a high-legibility Monospace font.

### Story 3.4: Alternative Triggers & Haptic Finish
As an accessibility-conscious user,
I want alternative ways to trigger reports and receive tactile confirmation,
So that I can use the tool effectively regardless of my physical movement ability.

**Acceptance Criteria:**
*   **Given** I have limited mobility
*   **When** I navigate to Settings or long-press the app version in the Side Menu
*   **Then** I must be able to trigger the manual capture button
*   **And** all successful captures must provide a sharp "Impact" pulse upon ID generation.
