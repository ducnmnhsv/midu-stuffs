# Story Point — Cheat Sheet

> **Dùng file này khi estimate task hàng ngày.** Bản đầy đủ (lý do, quy trình, calibration) xem tại [README.md](./README.md).

---

## 3 bước estimate nhanh

1. **So ticket với bảng Anchor** bên dưới → chọn Point giống nhất.
2. **Check Risk Flag** nào áp dụng → gắn tag, **không cộng thêm điểm**.
3. **Chưa rõ scope / chờ 3rd-party chưa chốt?** → Đừng point. Tạo **Spike** (timebox, không điểm) trước.

---

## Bảng Anchor — Complexity Point

| Point | Hỏi mình | Ví dụ |
|:---:|---|---|
| **1** | Sửa nhỏ, đã hiểu rõ 100%, 1 service | Đổi 1 field mapping trong API response |
| **2** | Thêm nhỏ vào flow có sẵn, không đổi contract | Thêm 1 validation mới |
| **3** | Logic mới nhưng theo pattern đã có, 1 service | Thêm 1 conditional order type mới (order-v2) |
| **5** | 2 service phối hợp, scope đã rõ, không unknown | Merge logic vào flow có sẵn (vd STT14a → STT5) |
| **8** | Cross-service, có dependency chặn bởi ticket khác | 1 issue trong nhóm TT134 STT5/STT35 |
| **13** | 3rd-party chưa quen, doc/API chưa đầy đủ | Tích hợp mới với eKYC (VNPT) / eContract (FPT) |
| **21+** | — | **Dừng lại** — tách nhỏ hoặc chuyển thành Spike |

---

## Risk Flag — gắn tag, không cộng điểm

| Flag | Gắn khi nào |
|---|---|
| 🔌 `3rd-party` | Phụ thuộc vendor ngoài (Lotte, VNPT, FPT, Smart OTP vendor) |
| 🗄️ `shared-migration` | Đụng migration/schema dùng chung với ticket khác |
| ⚖️ `compliance` | Sai là vấn đề pháp lý/quy định (TT134/UBCK), không chỉ là bug |
| 🕸️ `cross-service` | Cần ≥2 service phối hợp qua Kafka |
| 🧩 `cross-team` | Cần coordinate / merge chung PR với team khác |
| ❓ `unclear-scope` | Requirement hoặc 3rd-party spec chưa chốt → **tách Spike, đừng point** |

---

## Tự hỏi trước khi estimate

- [ ] Ticket này giống anchor nào nhất ở trên?
- [ ] Có vendor ngoài liên quan không?
- [ ] Có đụng migration/schema dùng chung không?
- [ ] Sai thì có phải vấn đề pháp lý không?
- [ ] Cần mấy service phối hợp?
- [ ] Cần team khác cùng làm không?
- [ ] Scope đã rõ 100% chưa — hay cần Spike trước?

---

## 3 điều KHÔNG được làm

- ❌ Để priority (P0–P3) ảnh hưởng điểm — gấp ≠ to.
- ❌ Quy điểm ra giờ/ngày cố định (vd "1 điểm = 4h").
- ❌ Đẩy điểm cao để phòng thủ rủi ro — dùng Risk Flag, không dùng điểm.

---

**Document Status:** ✅ Ready to publish | For: Toàn bộ dev team (BE/FE) | Next Steps: Pin/share file này cho team dùng khi estimate, xem [README.md](./README.md) nếu cần giải thích thêm
