# Story Point Estimation — TradeX / NHSV Pro

> Team ước lượng effort phát triển bằng story point để hỗ trợ PO lên kế hoạch sprint, áp dụng cho mọi task trong `Tracking/tasks.js` (Derivatives, TT134, Smart OTP, NHSV Pro, eKYC, TradeX-Monitor).

Estimation có thể do một dev đảm nhiệm (thường là Tech Lead) hoặc do cả dev team cùng thực hiện.

---

## Estimation Process

Quy trình ước lượng story point đi qua các bước sau:

1. PO/BA làm rõ scope với stakeholder và viết spec/issue trong `tradex-monitoring` theo đúng File Routing.
2. Dev team estimate story — qua một buổi planning poker ngắn — trước khi task được chuyển `status: "ready"` trong `tasks.js`.
3. Giao tiếp giữa PO và dev team phải được duy trì xuyên suốt sprint; nếu scope thay đổi, estimate phải được xem lại.

Theo convention agile phổ biến, **bug, hotfix, và chore không được gán điểm** — các loại ticket này không thể ước lượng effort một cách đáng tin cậy.

---

## Estimation Guidance

### Scale

Team dùng dãy Fibonacci `1`, `2`, `3`, `5`, `8`, `13` để estimate story. Story được đánh giá vượt quá 13 điểm phải được tách nhỏ trước khi đưa vào sprint.

### Effort Assessment

| Points | Complexity | Mức độ hiểu | Mental Model |
|:---:|---|---|---|
| **1** | Low | Đã biết rõ, không có unknown | Thay đổi nhỏ trong 1 service, theo pattern đã làm nhiều lần |
| **2** | Low | Đã quen thuộc | "Việc này mình từng làm rồi" — một vài coding session |
| **3** | Medium | Biết phần lớn, còn vài unknown | "Đã làm cái tương tự, nhưng không hoàn toàn giống" — cần research nhỏ trước khi code |
| **5** | Medium–High | Scope rõ nhưng cần phối hợp nhiều bên | 2 service phối hợp, không có unknown lớn nhưng cần điều phối kỹ |
| **8** | High | Nhiều unknown, phụ thuộc lẫn nhau | Cross-service, bị chặn hoặc phụ thuộc bởi ticket khác trong cùng nhóm |
| **13** | High | Phần lớn chưa biết | "Chưa từng làm việc này" — cần nhiều vòng research và xác nhận với bên ngoài |

**Anchor tham chiếu** — dùng để so sánh khi estimate, không phải định nghĩa tuyệt đối:

| Point | Ví dụ đã làm trong repo |
|:---:|---|
| 1 | Đổi 1 field mapping trong API response theo `tradex-api-conventions.md` |
| 2 | Thêm 1 validation mới vào flow có sẵn |
| 3 | Thêm 1 conditional order type mới trong `order-v2` (theo pattern Stop/OCO/Trailing/Bull-Bear) |
| 5 | Merge logic vào flow có sẵn — vd STT14a merge vào STT5 Session Auth |
| 8 | 1 issue trong nhóm TT134 STT5/STT35 — migration và middleware dùng chung |
| 13 | Tích hợp mới với eKYC (VNPT) hoặc eContract (FPT) |

---

## Impact Factors

Effort Assessment ở trên đo complexity code thuần. Nhưng kiến trúc TradeX có hai đặc thù khiến effort thực tế lệch khỏi complexity: **phụ thuộc 3rd-party** (Lotte Core, VNPT, FPT, Smart OTP vendor) và **kiến trúc cross-service qua Kafka**. Hai yếu tố này phải được ghi nhận — nhưng **không cộng thêm vào điểm**. Thay vào đó, gắn như Risk Flag riêng biệt.

> **Vì sao không cộng điểm như một impact factor thông thường?** Nếu quy đổi rủi ro thành điểm cộng thêm, dev có xu hướng đẩy điểm lên để "phòng thủ" khi chưa chắc chắn — điểm bị lạm phát dần theo thời gian, và sprint planning mất khả năng phân biệt "ticket to" với "ticket rủi ro lịch". Tách riêng giữ cho Complexity Point phản ánh đúng effort, còn Risk Flag cho sprint planning thấy rủi ro schedule một cách tường minh.

| Flag | Kích hoạt khi | Vì sao quan trọng |
|---|---|---|
| 🔌 `3rd-party` | Phụ thuộc vendor ngoài, team không kiểm soát tốc độ phản hồi | Lotte Core, VNPT eKYC, FPT eContract, Smart OTP vendor — timeline phụ thuộc bên thứ ba |
| 🗄️ `shared-migration` | Đụng migration/schema dùng chung với ticket khác | TT134 C6 — một script migration cho STT5 + STT35, lệch một bên ảnh hưởng cả bên kia |
| ⚖️ `compliance` | Sai không chỉ là bug — là vấn đề pháp lý/quy định | TT134 UBCK — cần review kỹ hơn mức thông thường |
| 🕸️ `cross-service` | Cần từ 2 service trở lên phối hợp qua Kafka | Derivatives: order-v2 + rest-proxy + lotte-bridge + realtime-v2 |
| 🧩 `cross-team` | Cần coordinate hoặc merge chung PR với team khác | STT5 BE-SA-1 = Smart OTP Login Task 4 |

### Unclear Scope → Spike

Khi ticket phụ thuộc vào điều chưa biết — 3rd-party spec chưa chốt, requirement chưa rõ — **không estimate**. Tách thành một **Spike**: một ticket riêng, có timebox (1–2 ngày), không mang story point, mục tiêu là research/POC/xác nhận với vendor. Sau khi Spike hoàn thành, quay lại estimate ticket thật bằng Effort Assessment ở trên, khi đã đủ thông tin để ước lượng chính xác.

Ví dụ áp dụng: Smart OTP đang ở trạng thái "chờ 3rd-party integration" — phần chờ này nên là một Spike, không gộp điểm vào ticket build tính năng.

---

## Best Practices

Để tránh các pitfall thường gặp, các nguyên tắc sau phải được tuân thủ:

**DO** estimate dựa trên năng lực của cả dev team.
**Do NOT** estimate chỉ dựa trên năng lực cá nhân người estimate.

👉 Người estimate (thường là Tech Lead) không phải lúc nào cũng là người trực tiếp thực thi ticket. Team có nhiều dev với kinh nghiệm và domain knowledge khác nhau — ví dụ dev đã quen Lotte integration so với dev mới join. Khi đã biết trước ai sẽ nhận ticket, estimate theo năng lực người đó; khi chưa biết, estimate theo mặt bằng chung của team.

**DO** raise concern khi được assign ticket nếu thấy điểm không hợp lý.
**Do NOT** nhận ticket mà không kiểm tra lại điểm.

👉 Người estimate cố gắng ước lượng sát nhất có thể, nhưng chỉ dev trực tiếp bắt tay vào mới biết rõ effort thật. Nếu điểm không khớp — do thiếu kinh nghiệm với phần việc đó, hoặc độ phức tạp bị đánh giá thấp — phải trao đổi lại với Tech Lead để điều chỉnh điểm hoặc re-assign, thay vì âm thầm commit vào một estimate sai.

**DO** work với PO để review ticket quá nhỏ — gộp với ticket khác, bỏ, hoặc điều chỉnh acceptance criteria.
**Do NOT** estimate ticket bằng 0 điểm.

👉 0 điểm là một anti-pattern. Nếu có việc phải làm — kể cả chỉ code review hoặc QA — thì effort không thể bằng 0. Nếu thực sự không còn việc gì, ticket không có lý do để được đưa vào sprint.

**DO** gắn Risk Flag riêng khi ticket có yếu tố 3rd-party, compliance, hoặc cross-service.
**Do NOT** đẩy Complexity Point lên cao để "phòng thủ" rủi ro.

👉 Risk Flag tồn tại chính là để việc này không cần đi qua điểm số. Đẩy điểm cao vì lo lắng khiến velocity của sprint mất chính xác, và che mất tín hiệu thật sự quan trọng: ticket nào đang rủi ro trễ vì lý do ngoài tầm kiểm soát của dev.

---

## Tích hợp vào `Tracking/tasks.js`

Thêm hai field mới vào schema hiện có — không đổi field cũ, tuân thủ rule C7 (`tasks.js` vẫn là nguồn duy nhất cho status/deadline/priority):

```js
{
  // ...các field hiện có (id, title, area, status, priority, deadline...)
  points: 5,                              // complexity point theo Effort Assessment
  riskFlags: ["3rd-party", "compliance"], // 0..n flag, rỗng nếu không có rủi ro đặc biệt
}
```

Board `kanban.html` có thể hiển thị Risk Flag như icon cạnh priority, tận dụng đúng pattern `note: "⚠️..."` đã có sẵn trong schema.

---

## Calibration định kỳ

Đây là convention áp dụng lần đầu cho team — bảng anchor ở trên chưa chắc đúng ngay từ đầu. Mỗi quý, nên có một retro nhẹ: so điểm ước lượng với effort thực tế của 5–10 ticket gần nhất. Nếu lệch nhiều — ví dụ nhiều ticket 3 điểm nhưng thực tế tốn công như ticket 8 điểm — điều chỉnh lại anchor thay vì giữ cứng bảng ban đầu.

---

**Document Status:** 📋 Draft — chờ team dev review | For: Dev team (BE/FE) + PO | Next Steps: Review với team, chốt anchor ban đầu, bắt đầu áp dụng từ sprint tới
