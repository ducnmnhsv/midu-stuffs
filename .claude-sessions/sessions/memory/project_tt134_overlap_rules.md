---
name: project-tt134-overlap-rules
description: "TT134 cross-issue implementation rules — prevent duplicate middleware, migration, and tasks when discussing any TT134 issue"
metadata: 
  node_type: memory
  type: project
  originSessionId: b525457b-b18a-41af-bee9-f6387777cae4
---

Các rule ngăn overlap/duplicate khi làm issue TT134. Được ghi nhận sau khi phân tích dependency giữa STT5 (Session Auth) và STT35 (Device ID Logging).

**Why:** Không có cross-issue rules → team tạo 2 migration scripts riêng, 2 middlewares riêng, assign dev riêng cho STT14a (đã merged) → double effort.

**How to apply:** Trước khi viết/thảo luận bất kỳ issue TT134 mới, chạy qua checklist và rules dưới đây. Full rules ở `TT134 - UBCK/README.md` Section 7.

---

## Rules tóm tắt

**R1 — DB Migration: 1 script duy nhất**
STT5 + STT35 đều modify `t_order_log` + `t_withdrawal_log`. Luôn gộp thành 1 migration script. Tên: `V{n}__tt134_auth_device_logging.sql`

**R2 — Logging Middleware: không tạo 2 cái**
BE-SA-3 (STT5) + BE-1 (STT35) → cùng 1 middleware, cùng endpoint group (equity/order, derivatives/order, orders/cancel).
BE-SA-4 (STT5) + BE-2 (STT35) → cùng 1 middleware cho /cash/withdraw/confirm.

**R3 — STT14a đã merged, không assign riêng**
Rút tiền <10M hoàn toàn covered bởi STT5 Session Auth. Đóng STT14a khi STT5 done. Không tạo task mới.

**R4 — BE-SA-1 = 07_BE_Task Task 4**
Embed sAm/sAt tại verifyOTP là phần mở rộng của Smart OTP Login task. Merge vào cùng PR của 07_BE_Task, không tạo PR riêng.

**R5 — Unblocking sequence**
Không bắt đầu spec Audit Log / Alert System / Session Management (P1) trước khi STT5 + STT35 Phase 3 xong.

**R6 — Checklist issue mới (4 câu hỏi)**
1. Issue modify t_order_log/t_withdrawal_log? → gộp migration
2. Issue liên quan order logging pipeline? → dùng middleware đã có
3. Issue bị cover bởi STT5 hoặc STT35? → merge/close, không implement riêng
4. Issue phụ thuộc auth_method/device_unique_id trong logs? → chờ Phase 3

---

## Issues đã resolved (không implement riêng)

- ✅ stt14a (Rút <10M) → merged vào stt5 BE-SA-4
- ✅ Double OTP trong order flow → FE-SA-4 removes OTP from ConfirmOrdersScreen
- ✅ POST /api/v1/session/auth endpoint → loại bỏ hoàn toàn

## Issues unblocked sau STT5 + STT35 Phase 3

- Audit Log (P1, Điều 5/10)
- Alert System (P1, Điều 8)
- Session Management (P1, Điều 7) — sAt provides context
- Compliance Reporting (P2, Điều 13)
