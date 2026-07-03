# Tracking — Single-source Task Management

> **Nguồn dữ liệu duy nhất** cho status/deadline/priority của mọi task trong repo.
> Rule C7 trong `CLAUDE.md`: status đổi → **chỉ sửa `tasks.js`**, README các khu vực không duplicate status.

## Cách dùng

| Việc | Làm gì |
|---|---|
| Xem board | Mở `Tracking/kanban.html` bằng browser (double-click hoặc `open Tracking/kanban.html`) |
| Đổi status task | Sửa field `status` của task trong `tasks.js` → reload trang |
| Thêm task mới | Tạo doc theo File Routing (CLAUDE.md) + thêm 1 entry vào `tasks.js` |
| Task xong | Đổi `status: "done"` — KHÔNG xoá entry (giữ lịch sử trên cột Done) |
| Theo dõi deadline | Tab **Timeline** trên board — group theo tháng; chip đỏ = quá hạn, cam = còn ≤ 14 ngày |

## Schema `tasks.js`

```js
{
  id: "TT134-STT7",          // {AREA}-{FEATURE|STT}-{seq} — KHÔNG đổi sau khi tạo (dependency trỏ theo id)
  title: "…",
  area: "TT134",             // Derivatives | TT134 | Smart-OTP | NHSV-Pro | eKYC | TradeX-Monitor
  feature: "Order 2FA",      // sub-category trong area
  type: "BE",                // BE | FE | PRD | Spec | Decision | Project
  status: "ready",           // backlog | ready | in_progress | blocked | done
  priority: "P0",            // P0 | P1 | P2 | P3 | GATE | null
  deadline: "2026-08-14",    // ISO YYYY-MM-DD hoặc null
  owner: "BE Lead",
  jira: "NHMTS-682",         // hoặc null
  doc: "TT134 - UBCK/…",     // path tương đối từ repo root
  blocks: ["TT134-STT14A"],  // task này chặn các id khác
  blockedBy: [],             // id hoặc mô tả external blocker ("Vendor TBD")
  note: "…",                 // note bắt đầu bằng "⚠️" sẽ được highlight trên card
}
```

### Ý nghĩa status

| Status | Nghĩa |
|---|---|
| `backlog` | Chưa sẵn sàng làm (thiếu PRD/spec, chưa groom, plan sau) |
| `ready` | Đã đủ điều kiện bắt đầu — spec/issue ready for dev |
| `in_progress` | Đang làm (dev đang code / PO đang iterate spec) |
| `blocked` | Bị chặn — xem `blockedBy` |
| `done` | Xong / released / merged scope |

## Tracker cũ (deprecated)

- `TT134 - UBCK/TT134_Kanban.html` — deprecated, data đã migrate sang `tasks.js`. Giữ lại vì có dependency graph chi tiết + task breakdown per card (BE/FE/Core tasks).
- Bảng status trong README các khu vực — đã thay bằng link tới board này.

---

*Tạo: 2026-07-03 · Kèm theo: [Audit_2026-07-03.md](./Audit_2026-07-03.md) — báo cáo audit + reorg toàn repo.*
