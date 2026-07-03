# Thay đổi TradeX API (Planning documentation) sau cập nhật Lotte spec 04/03/2026

**Mục đích:** Liệt kê các cập nhật cần thực hiện trong Planning documentation để đồng bộ với [Lotte_DR.md](../Documentation/Lotte_DR_API_Specs.md) phiên bản mới nhất (PDF 04/03/2026, doc sync 05/03/2026).

**Tham chiếu thay đổi Lotte:** Xem bảng "LỊCH SỬ THAY ĐỔI TÀI LIỆU" trong Lotte_DR.md § đầu tài liệu.

---

## 1. Tổng quan thay đổi Lotte (đã áp dụng trong Lotte_DR.md)

| Nguồn | Nội dung |
|-------|----------|
| **2026-03-04** | Thêm DRACC-035, DRACC-036, DRACC-037, DRORD-033; bổ sung URL DRORD-025, DRORD-026, DRORD-028 |
| **2026-03-05** | URL DRACC-009/019 có prefix `/tsol/apikey/`; tham số Lotte: `snd_acnt`/`rcv_acnt` (DRACC-019), `is_acnt_no`/`is_cnte` (DRACC-033, 034); DRACC-032 URL tsol; DRACC-031 response thêm `net_assets`; DRACC-009 response thêm `scrt_err_msg` |

---

## 2. Thay đổi cần thực hiện trong Planning documentation

### 2.1 VSD Transaction — `Cash transaction/VSD transaction/VSD_Transaction_API_Spec.md`

| Vị trí | Hiện tại | Cần sửa | Ghi chú |
|--------|----------|---------|--------|
| **§7 (Withdraw) – Lotte Endpoint** | `[RootURL]/tools/vcs/der/account/dr-withdrawal-deposit` | `[Root URL]/tsol/apikey/tuxsvc/der/account/dr-withdrawal-deposit` | Theo Lotte §2.2.1 (04/03): URL đúng là **tsol/apikey**, không phải tools/vcs |
| **§7 – Response Mapping** | Chỉ mô tả `error_code`, `success` | Bổ sung: Lotte DataResponse có field `scrt_err_msg` (message thực hiện thành công) | Tùy chọn: TradeX có thể map `scrt_err_msg` → response nếu cần hiển thị message |
| **§5 (Calculate Fee) – Lotte field** | `is_act_no` | `is_acnt_no` | Lotte spec: `is_acnt_no` (Tài khoản) |
| **§6 (Deposit) – Lotte field** | `is_ante` (Diễn giải) | `is_cnte` | Lotte spec: `is_cnte` (Diễn giải) |
| **Header Note (line 8)** | Tham chiếu 27/02/2026 | Tham chiếu 04/03/2026 (hoặc "phiên bản mới nhất") | Đồng bộ với Lotte_DR.md |

**Đã đúng (không cần sửa):**
- §3 Get Bank List: Lotte Endpoint đã ghi `.../tsol/apikey/tuxsvc/der/account/list_sec_bank_actn_dr` (DRACC-032).

---

### 2.2 Internal Transfer — `Cash transaction/Internal transfer/Internal_Transfer_API_Spec.md`

| Vị trí | Hiện tại | Cần sửa | Ghi chú |
|--------|----------|---------|--------|
| **§4.1 – Lotte Endpoint (DRACC-019)** | `[Root URL APIKEY]/tuxsvc/der/account/...` | `[Root URL APIKEY]/tsol/apikey/tuxsvc/der/account/dr-transfer-cash` | Theo Lotte §2.2.2 (04/03): URL đầy đủ có prefix **tsol/apikey** |
| **§4.2 – Request Mapping** | Lotte field: `snd_actn`, `rcv_actn` | Lotte field: **`snd_acnt`**, **`rcv_acnt`** | Lotte spec 04/03 dùng **acnt** (không phải actn) |
| **§4.1 – Note trong doc** | "Lotte doc gốc có request: `snd_actn`, `snd_sub`, `rcv_actn`..." | "Lotte request: `snd_acnt`, `snd_sub`, `rcv_acnt`, `rcv_sub`, `amount`, `remark`" | Chỉnh tên field cho đúng |
| **Header Note** | 27/02/2026 | 04/03/2026 (hoặc "phiên bản mới nhất") | Đồng bộ tham chiếu |

**Đã đúng:** DRACC-020 (History) đã ghi URL có `/tsol/apikey/`.

---

### 2.3 Asset Info — `Asset/Specifications/Asset_Info_API_Spec.md`

| Vị trí | Hiện tại | Cần sửa | Ghi chú |
|--------|----------|---------|--------|
| **§2.1 & §3.3 – Response mapping** | Bảng 1-1 Lotte → TradeX chưa có `net_assets` | Thêm 1 dòng: `net_assets` → `netAssets` (String, Direct, Tài sản ròng) | Lotte 04/03 bổ sung field `net_assets` trong DRACC-031 DataResponse |
| **Header Note (line 9)** | Đã ghi "có thêm field ... `net_assets`" | Giữ nguyên, chỉ cần bổ sung vào bảng mapping trong §2.1 và §3.3 | |

---

### 2.4 Order Availability Check — `Order/Specifications/Order_Availability_Check_API_Spec.md`

| Vị trí | Hiện tại | Cần sửa | Ghi chú |
|--------|----------|---------|--------|
| **§ Lotte Endpoint (DRORD-028)** | `[Root URL APIKEY]/tuxsvc/der/order/dr-available-order-qty` | Kiểm tra Lotte_DR §2.3.8: Lotte chỉ ghi URL `[Root URL APIKEY]` (path không đầy đủ trong spec). Nếu Lotte xác nhận path đầy đủ thì cập nhật; nếu không thì giữ + ghi chú "path theo Lotte_DR phiên bản mới nhất" | Lotte 27/02 sync có "bổ sung URL DRORD-028" — cần đối chiếu với Lotte_DR §2.3.8 |
| **Header Note** | 27/02/2026 | 04/03/2026 (hoặc "phiên bản mới nhất") | Đồng bộ tham chiếu |

---

### 2.5 Stop Orders — `Order/Specifications/Stop_Orders_API_Spec.md`

| Vị trí | Hiện tại | Cần sửa | Ghi chú |
|--------|----------|---------|--------|
| **§ Lotte URL DRORD-025, 026** | Đã ghi `dr-cancel-stop-order` | Lotte_DR §2.3.7: URL có thể "(Không có trong tài liệu)" hoặc đã bổ sung — kiểm tra Lotte_DR phiên bản 04/03; nếu có URL đầy đủ thì cập nhật vào spec | Bản 27/02 đã "bổ sung URL DRORD-025/026" |
| **Footer / Related** | "Lotte URLs theo Lotte_DR (27/02/2026)" | "Lotte URLs theo Lotte_DR (04/03/2026 hoặc phiên bản mới nhất)" | Đồng bộ tham chiếu |

---

### 2.6 TradeX Derivatives API Index — `../Documentation/TradeX_Derivatives_API_Index.md`

| Vị trí | Hiện tại | Cần sửa | Ghi chú |
|--------|----------|---------|--------|
| **Dòng "Cập nhật"** | 2026-03-04 — đồng bộ tham chiếu Lotte theo Lotte_DR (27/02/2026) | 2026-03-05 — đồng bộ tham chiếu Lotte theo Lotte_DR (04/03/2026). Cập nhật URL/field theo [CHANGES_AFTER_LOTTE_SPEC_04032026.md](../Planning%20documentation/CHANGES_AFTER_LOTTE_SPEC_04032026.md) | Cho PM/BE biết spec Planning đã align với Lotte mới nhất |
| **§2.4 VSD** | Đã liệt kê DRACC-009, 032, 033, 034, 021 | Không đổi danh sách; implementation cần gọi đúng URL (tsol/apikey) và field (is_acnt_no, is_cnte) theo Lotte 04/03 | Có thể thêm 1 câu: "URL Lotte cho DRACC-009, 032: xem Lotte_DR phiên bản 04/03 (path tsol/apikey)." |

---

## 3. API mới từ Lotte (chưa có trong Planning documentation)

Các API sau **đã có trong Lotte_DR** (sync 27/02 & 04/03) nhưng **chưa có spec TradeX** trong Planning documentation:

| Lotte API | Mô tả (Lotte) | Gợi ý Planning |
|-----------|----------------|----------------|
| **DRACC-035** | Cung cấp các giao dịch tiền phát sinh trên tài khoản NĐT — dr-monetary-transaction | Có thể thêm vào Cash transaction (ví dụ: Monetary Transaction / Cash statement mở rộng) hoặc tạo spec riêng nếu product cần |
| **DRACC-036** | Cung cấp trạng thái tài khoản và số tiền cần bổ sung khi vi phạm tỷ lệ MU — dr-mu-breach-account | Có thể thuộc Asset hoặc Risk/Compliance — cần product quyết định có expose TradeX API không |
| **DRACC-037** | (Xem Lotte_DR §2.1.8) | Tương tự — quyết định theo nghiệp vụ |
| **DRORD-033** | (Xem Lotte_DR §2.3) | Nếu là API lệnh mới, có thể bổ sung vào Order/Specifications và TradeX_Derivatives_API_Index |

**Hành động:** Product/BA quyết định có đưa DRACC-035/036/037 và DRORD-033 vào roadmap TradeX hay không; nếu có thì tạo spec tương ứng và cập nhật Index.

---

## 4. Checklist thực hiện

- [ ] **VSD_Transaction_API_Spec.md:** Sửa URL DRACC-009, field is_acnt_no (DRACC-033), is_cnte (DRACC-034); bổ sung scrt_err_msg (optional); cập nhật tham chiếu 04/03.
- [ ] **Internal_Transfer_API_Spec.md:** Sửa URL DRACC-019 (tsol/apikey), mapping snd_acnt/rcv_acnt; cập nhật tham chiếu 04/03.
- [ ] **Asset_Info_API_Spec.md:** Thêm net_assets → netAssets vào bảng mapping §2.1 và §3.3.
- [ ] **Order_Availability_Check_API_Spec.md:** Đối chiếu DRORD-028 URL với Lotte_DR §2.3.8; cập nhật tham chiếu 04/03.
- [ ] **Stop_Orders_API_Spec.md:** Đối chiếu URL DRORD-025/026 với Lotte_DR; cập nhật tham chiếu 04/03.
- [ ] **TradeX_Derivatives_API_Index.md:** Cập nhật dòng "Cập nhật" và (tùy chọn) ghi chú URL tsol/apikey.
- [ ] **API mới (DRACC-035/036/037, DRORD-033):** Product quyết định có spec TradeX → tạo spec + cập nhật Index.

---

**Document Status:** 📋 Checklist  
**For:** PM / BA / Dev  
**Next Steps:** Thực hiện từng mục trong §4; sau khi sửa, cập nhật ngày trong từng file spec và trong Index.
