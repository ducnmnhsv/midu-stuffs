# Prompt Figma Make — NHSV Pro SmartOTP (UI từ PRD v1.3)

**Mục đích:** Dán nội dung trong khối ` ``` ` bên dưới vào Figma Make để generate UI.  
**Lưu ý:** Không chỉ định styling — dùng design system có sẵn (tokens, components).

**Tham chiếu PRD:** [PRD.md](./PRD.md)

---

## Prompt (copy toàn bộ khối tiếng Anh)

```
You are generating mobile UI wireframes for NHSV Pro (iOS/Android) — SmartOTP feature. Use ONLY our existing design system components (buttons, typography, colors, spacing, navigation bars, modals, inputs). Do NOT invent a new visual style: apply tokens/components from the linked library. Focus on layout hierarchy, Vietnamese copy, states, and empty/error flows.

PRODUCT CONTEXT
- App: NHSV Pro — SmartOTP is in-app (not a separate app).
- Auth model: After username/password login, user MUST complete SmartOTP before entering the app. One successful SmartOTP verify activates a work session (2 hours). No per-transaction SmartOTP inside that session.
- Pre-auth window: 5 minutes to complete SmartOTP after login; then must re-login.
- Enrollment is mandatory (onboarding + migration). Reference competitor pattern: VPS-style PIN screen (scrambled keypad) + OTP display with circular countdown.

DELIVERABLES
Create a Figma file section "SmartOTP — NHSV Pro" with:
1) User flow map (simple frames or arrows) for: Enrollment, Post-login (Luồng I), Forgot PIN, Migration modals, Session expired, App update required.
2) All screens listed below as separate frames, mobile portrait, with component instances from design system.

GLOBAL UX RULES (apply everywhere relevant)
- No "Skip" / "Để sau" on post-login SmartOTP gate (Luồng I). Back = full logout OR disabled per PRD.
- Scrambled numeric keypad for SmartOTP PIN only; show helper text: "Vị trí các số thay đổi ngẫu nhiên để bảo vệ PIN SmartOTP"
- OTP screen: circular countdown (30s TOTP step), 6 digits spaced for readability, masked account label e.g. "TKCK: ***272", primary button "Xác nhận" (manual tap, no auto-submit), note block about one-time use per login session and do not share OTP; optional text link "Đồng bộ thời gian" for clock sync.
- Biometric alternative on PIN screen where applicable: Face ID / fingerprint entry path.

--- SCREENS TO GENERATE ---

A) ONBOARDING — SMARTOTP ENROLLMENT (Luồng A, new user)
- Frame: "Onboarding — SmartOTP — Intro" — title step e.g. "Thiết lập SmartOTP — Bảo vệ mọi giao dịch của bạn", progress indicator "Bước 3/3", short benefit text, primary CTA "Tiếp tục", no skip.
- Frame: "Enrollment — SMS verify" — explain SMS OTP for identity; masked phone; OTP input; resend timer; primary "Xác nhận".
- Frame: "Enrollment — Create PIN" — 6-dot PIN indicator; scrambled keypad; caption for scrambled keypad; optional biometric opt-in toggle or next step.
- Frame: "Enrollment — Confirm PIN" — repeat PIN; error state if mismatch.
- Frame: "Enrollment — Biometric opt-in" (optional) — enable Face ID / vân tay; primary / secondary.
- Frame: "Enrollment — Success / First OTP" — show first TOTP with countdown + "Hoàn tất" or auto-continue to home.

B) POST-LOGIN — SESSION ACTIVATION (Luồng I) — PRIMARY FLOW
- Frame: "Post-login — Nhập mã PIN SmartOTP" — nav title "Nhập mã PIN SmartOTP"; info callout: "Để bảo vệ toàn diện các giao dịch tài chính, NHSV Pro áp dụng tính năng Đăng nhập 2 lớp. Quý khách vui lòng xem chi tiết trong mục Quản lý tài khoản" (or shortened per space); 6 PIN dots; link "Quên mã PIN SmartOTP?"; scrambled keypad + caption; biometric shortcut if enabled.
- Frame: "Post-login — Lấy mã SmartOTP" — title "Lấy mã SmartOTP"; circular timer + label "Mã xác thực sẽ tự động cập nhật sau" + seconds; large 6-digit OTP; "TKCK: ***272"; primary "Xác nhận"; "Lưu ý" section: OTP valid for one login session, do not share; link đồng bộ thời gian.
- Frame: "Post-login — Verify error" — inline error after failed verify; retry count hint (max 5); secondary destructive "Đăng xuất" if needed.
- Frame: "Post-login — Pre-auth expired" — message "Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại." + CTA to login.
- Frame: "PIN locked" — after 5 wrong PINs: locked 30 min or SMS reset path (short copy).

C) FORGOT PIN (Luồng D) + SMS FEE NUDGE (reference VPS modal)
- Frame: "Forgot PIN — Modal" — headline "Quên mã PIN? Vui lòng xác thực qua SMS OTP"; warning line "Lưu ý: Phí SMS OTP được tính trên từng tin nhắn, vui lòng chuyển sang SmartOTP sau khi đăng nhập."; buttons "Đồng ý" / "Đóng".
- Frame: "Forgot PIN — SMS OTP input" — enter SMS code; confirm.
- Frame: "Forgot PIN — Set new PIN" — new + confirm PIN with scrambled keypad.

D) MIGRATION & GATES (Luồng F, Phase 1–2)
- Frame: "Migration — Modal lần 1" — copy: "Bảo vệ tài khoản của bạn tốt hơn. Kích hoạt SmartOTP — không cần chờ SMS, xác thực an toàn hơn trong 2 phút."; primary "Kích hoạt ngay"; secondary "Để sau" (only for migration modal, not post-login gate).
- Frame: "Migration — Hard gate" — full screen: "Kích hoạt SmartOTP để tiếp tục sử dụng NHSV Pro. Chỉ mất 2 phút."; single path to enrollment.
- Frame: "Home — Banner countdown" — persistent banner: "Còn [X] ngày — Kích hoạt SmartOTP trước [date] để tránh gián đoạn giao dịch." + CTA "Kích hoạt ngay".
- Frame: "Feature gate — Đặt lệnh blocked" — intercept: "Tính năng này yêu cầu SmartOTP. Kích hoạt ngay để tiếp tục." → enrollment.

E) SESSION EXPIRED (Luồng C edge)
- Frame: "Session expired" — dialog or full screen: "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại."; CTA to login (then Luồng I again).
- Optional: small indicator on home "Phiên hết hạn lúc HH:MM" (if PRD allows session countdown in UI — label as optional).

F) APP UPDATE REQUIRED (Phase 2)
- Frame: "Force update" — "Vui lòng cập nhật ứng dụng để tiếp tục sử dụng SmartOTP."; link to store.

G) SETTINGS — DEVICE INFO (FR-001.3)
- Frame: "Cài đặt — SmartOTP" — device name, registeredAt, lastUsedAt, isActive; actions: unregister device (with confirmation + re-auth note).

H) REMOTE DEACTIVATION / CS RESET (user-facing message)
- Frame: "Notification — Device deactivated" — "Thiết bị SmartOTP của bạn đã bị vô hiệu hóa." + next steps to re-enroll.

I) WTS (WEB) — MINIMAL SET (Luồng B)
- Frame: "WTS — Nhập mã SmartOTP" — 6-digit input; helper text: open NHSV Pro app tab SmartOTP; error state OTP already used on another channel with channel + time; session expiry redirect note.

EDGE STATES TO VARIANT
- Loading on verify; empty OTP; wrong OTP; OTP_ALREADY_USED with params; network error; biometric failed → fallback to PIN.

ORGANIZE
- Group frames by flow: Enrollment | Post-login | Migration | Errors | Settings | Web.
- Name layers in English for dev handoff; keep visible Vietnamese strings as specified.

END PROMPT
```

---

## Gợi ý sử dụng

- **MVP nhanh:** Chỉ paste nhóm A + B trong prompt (Enrollment + Post-login).
- **Figma Make hay over-detail:** Thêm một dòng đầu prompt: `Wireframe fidelity: low–mid; use placeholders for icons.`
- **Design system:** Trong Figma Make, attach/link file library NHSV Pro trước khi chạy prompt.

---

**Document status:** Bản lưu theo hội thoại — đồng bộ với PRD SmartOTP v1.3.
