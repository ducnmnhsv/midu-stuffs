# Business Requirements Document (BRD)
## Tích hợp Giao dịch Phái sinh vào TradeX

> **Document ID:** DER-BRD-001  
> **Version:** 1.0  
> **Created:** 2025-01-30  
> **Author:** Business Analyst Team  
> **Status:** Draft - Pending Approval

---

## Mục lục

1. [Bối cảnh & Mục tiêu](#1-bối-cảnh--mục-tiêu)
2. [Phạm vi dự án](#2-phạm-vi-dự-án)
3. [Các bên liên quan](#3-các-bên-liên-quan)
4. [Yêu cầu nghiệp vụ](#4-yêu-cầu-nghiệp-vụ)
5. [User Stories](#5-user-stories)
6. [Quy tắc nghiệp vụ](#6-quy-tắc-nghiệp-vụ)
7. [Yêu cầu dữ liệu](#7-yêu-cầu-dữ-liệu)
8. [Quy trình nghiệp vụ](#8-quy-trình-nghiệp-vụ)
9. [Yêu cầu phi chức năng](#9-yêu-cầu-phi-chức-năng)
10. [Tiêu chí chấp nhận](#10-tiêu-chí-chấp-nhận)
11. [Phụ lục](#11-phụ-lục)

---

## 1. Bối cảnh & Mục tiêu

### 1.1 Bối cảnh

Hiện tại, ứng dụng NHSV Pro (TradeX Backend) đang cung cấp dịch vụ giao dịch **chứng khoán cơ sở** (cổ phiếu, ETF, chứng quyền) cho khách hàng của NHSV. Hệ thống đã hoạt động ổn định và phục vụ hàng nghìn người dùng hàng ngày.

Để mở rộng dịch vụ và đáp ứng nhu cầu của nhà đầu tư, NHSV cần bổ sung tính năng **giao dịch phái sinh** (Hợp đồng tương lai - Futures) vào ứng dụng.

### 1.2 Vấn đề cần giải quyết

| # | Vấn đề | Tác động |
|---|--------|----------|
| 1 | Khách hàng không thể xem giá phái sinh trên app | Mất cơ hội kinh doanh |
| 2 | Không có thông tin mã phái sinh | Khách hàng phải dùng app khác |
| 3 | Không có giá real-time phái sinh | Trải nghiệm không nhất quán |

### 1.3 Mục tiêu dự án

| Mục tiêu | Đo lường | Target |
|----------|----------|--------|
| **MT-01**: Hiển thị danh sách mã phái sinh | Số mã phái sinh trên app | 100% mã đang giao dịch |
| **MT-02**: Cung cấp giá real-time | Độ trễ dữ liệu | < 1 giây |
| **MT-03**: Không ảnh hưởng hệ thống cơ sở | Uptime hệ thống cơ sở | 99.9% |

### 1.4 Lợi ích kỳ vọng

**Cho khách hàng:**
- Xem được giá phái sinh trên cùng ứng dụng NHSV Pro
- Theo dõi biến động giá real-time
- Đầy đủ thông tin để ra quyết định đầu tư

**Cho NHSV:**
- Mở rộng đối tượng khách hàng
- Chuẩn bị nền tảng cho giao dịch phái sinh trong tương lai
- Tăng tính cạnh tranh với các công ty chứng khoán khác

---

## 2. Phạm vi dự án

### 2.1 Trong phạm vi (In Scope)

| # | Hạng mục | Mô tả |
|---|----------|-------|
| 1 | **Danh sách mã phái sinh** | Hiển thị tất cả mã hợp đồng tương lai đang giao dịch |
| 2 | **Thông tin chi tiết mã** | Giá, khối lượng, open interest, ngày đáo hạn |
| 3 | **Giá real-time** | Cập nhật giá liên tục trong phiên giao dịch |
| 4 | **Sổ lệnh (Bid/Offer)** | Hiển thị 10 bước giá mua/bán tốt nhất |
| 5 | **Phân biệt cơ sở/phái sinh** | Người dùng dễ dàng nhận biết loại chứng khoán |

### 2.2 Ngoài phạm vi (Out of Scope)

| # | Hạng mục | Lý do | Giai đoạn sau |
|---|----------|-------|---------------|
| 1 | Đặt lệnh phái sinh | Cần tích hợp thêm API trading | Phase 2 |
| 2 | Quản lý tài khoản phái sinh | Cần nghiệp vụ riêng | Phase 2 |
| 3 | Tính toán lãi/lỗ vị thế | Phức tạp, cần spec riêng | Phase 2 |
| 4 | Ký quỹ (Margin) | Liên quan đến tiền | Phase 2 |
| 5 | Thay đổi giao diện app | FE team chịu trách nhiệm | Parallel |
| 6 | Thay đổi cơ chế gọi API của FE | Giữ nguyên cơ chế hiện tại | N/A |

### 2.3 Nguyên tắc quan trọng - Giảm thiểu thay đổi FE

> **QUAN TRỌNG:** FE hiện tại đang sử dụng WebSocket để nhận giá real-time.
> Cơ chế cho phái sinh PHẢI tương tự cơ sở để FE không cần thay đổi nhiều.

**Cơ chế hiện tại của FE (giữ nguyên):**

```
1. App Start:
   └─→ Download symbol_static.json (danh sách mã + thông tin tĩnh)
   
2. Real-time:
   └─→ Subscribe WebSocket channels
   └─→ Nhận updates, merge vào local cache
   
3. API symbolInfo (nếu cần):
   └─→ Gọi API, data đã được aggregate từ WebSocket
```

**Thay đổi tối thiểu cho FE:**

| Hạng mục | Thay đổi | Chi tiết |
|----------|----------|----------|
| symbol_static.json | Tự động có thêm mã phái sinh | FE chỉ cần filter theo `m` |
| WebSocket channels | Thêm channels mới | `market.quote.dr.{code}`, `market.bidoffer.dr.{code}` |
| API symbolInfo | Không đổi | Trả về data đã aggregate |
| Cơ chế gọi API | Không đổi | Giữ nguyên flow hiện tại |

### 2.3 Giả định (Assumptions)

| # | Giả định | Rủi ro nếu sai |
|---|----------|----------------|
| A1 | Lotte cung cấp API phái sinh ổn định | Cần fallback mechanism |
| A2 | Format dữ liệu từ Lotte không thay đổi | Cần versioning |
| A3 | Hệ thống hiện tại đủ capacity | Cần load test |
| A4 | Client app đã hỗ trợ hiển thị mã phái sinh | Cần confirm với FE |

### 2.4 Ràng buộc (Constraints)

| # | Ràng buộc | Tác động |
|---|-----------|----------|
| C1 | Không được ảnh hưởng hệ thống cơ sở đang chạy | Cần thiết kế isolation |
| C2 | Sử dụng API của Lotte Securities | Phụ thuộc vào Lotte |
| C3 | Timeline: Hoàn thành trong Q1/2025 | ~3 tuần development |

---

## 3. Các bên liên quan

### 3.1 Stakeholder Matrix

| Stakeholder | Vai trò | Mối quan tâm | Mức độ ảnh hưởng |
|-------------|---------|--------------|------------------|
| **Product Owner** | Quyết định | ROI, timeline, scope | Cao |
| **Khách hàng (NĐT)** | Người dùng cuối | Dễ sử dụng, chính xác | Cao |
| **BE Team** | Triển khai | Spec rõ ràng, khả thi | Trung bình |
| **FE/Mobile Team** | Triển khai UI | Data format, API contract | Trung bình |
| **QA Team** | Kiểm thử | Test cases, acceptance criteria | Trung bình |
| **Operations** | Vận hành | Monitoring, rollback plan | Trung bình |
| **Lotte Securities** | Đối tác API | API availability | Cao |

### 3.2 RACI Matrix

| Hoạt động | PO | BA | BE Lead | FE Lead | QA |
|-----------|----|----|---------|---------|-----|
| Phê duyệt yêu cầu | A | R | C | C | I |
| Thiết kế giải pháp | I | C | A/R | C | I |
| Phát triển | I | C | A/R | R | I |
| Kiểm thử | I | C | C | C | A/R |
| Triển khai | A | I | R | R | C |

*R = Responsible, A = Accountable, C = Consulted, I = Informed*

---

## 4. Yêu cầu nghiệp vụ

### 4.1 Yêu cầu chức năng (Functional Requirements)

#### FR-001: Hiển thị danh sách mã phái sinh

| Thuộc tính | Nội dung |
|------------|----------|
| **ID** | FR-001 |
| **Tên** | Hiển thị danh sách mã phái sinh |
| **Mô tả** | Hệ thống phải hiển thị tất cả các mã hợp đồng tương lai (Futures) đang được giao dịch trên sàn |
| **Độ ưu tiên** | Cao (Must have) |
| **Nguồn yêu cầu** | Product Owner |

**Chi tiết yêu cầu:**

| # | Yêu cầu chi tiết |
|---|------------------|
| FR-001.1 | Hệ thống phải lấy danh sách mã phái sinh từ nguồn dữ liệu Lotte |
| FR-001.2 | Danh sách phải được cập nhật đầu mỗi ngày giao dịch (trước 9:00) |
| FR-001.3 | Mỗi mã phải có đầy đủ thông tin: Mã, Tên, Ngày đáo hạn |
| FR-001.4 | Danh sách phải phân biệt rõ ràng với mã cơ sở |

**Dữ liệu yêu cầu:**

| Trường | Bắt buộc | Mô tả | Ví dụ |
|--------|----------|-------|-------|
| Mã hợp đồng | Có | Mã giao dịch | VN30F2501 |
| Tên hợp đồng | Có | Tên đầy đủ | VN30 Future Tháng 1/2025 |
| Loại | Có | Phân loại sản phẩm | FUTURES |
| Thị trường | Có | Định danh thị trường | derivatives |
| Ngày đáo hạn | Có | Ngày cuối cùng giao dịch | 30/01/2025 |
| Mã cơ sở | Có | Index cơ sở | VN30 |

---

#### FR-002: Hiển thị thông tin giá mã phái sinh

| Thuộc tính | Nội dung |
|------------|----------|
| **ID** | FR-002 |
| **Tên** | Hiển thị thông tin giá mã phái sinh |
| **Mô tả** | Hệ thống phải cung cấp đầy đủ thông tin giá của mã phái sinh |
| **Độ ưu tiên** | Cao (Must have) |
| **Nguồn yêu cầu** | Product Owner |

**Chi tiết yêu cầu:**

| # | Yêu cầu chi tiết |
|---|------------------|
| FR-002.1 | Hiển thị giá hiện tại (Last) |
| FR-002.2 | Hiển thị thay đổi giá (Change) so với tham chiếu |
| FR-002.3 | Hiển thị % thay đổi (Change Rate) |
| FR-002.4 | Hiển thị giá trần, sàn, tham chiếu |
| FR-002.5 | Hiển thị giá mở cửa, cao nhất, thấp nhất trong ngày |
| FR-002.6 | Hiển thị khối lượng giao dịch |
| FR-002.7 | Hiển thị Open Interest (số hợp đồng mở) |

**Dữ liệu yêu cầu:**

| Trường | Bắt buộc | Mô tả | Đơn vị |
|--------|----------|-------|--------|
| Giá hiện tại | Có | Giá khớp gần nhất | Điểm |
| Thay đổi | Có | Chênh lệch vs tham chiếu | Điểm |
| % Thay đổi | Có | Tỷ lệ thay đổi | % |
| Giá trần | Có | Giá cao nhất được phép | Điểm |
| Giá sàn | Có | Giá thấp nhất được phép | Điểm |
| Giá tham chiếu | Có | Giá tham chiếu đầu ngày | Điểm |
| Giá mở cửa | Có | Giá mở cửa | Điểm |
| Giá cao nhất | Có | Giá cao nhất trong ngày | Điểm |
| Giá thấp nhất | Có | Giá thấp nhất trong ngày | Điểm |
| Khối lượng | Có | Tổng KL giao dịch | Hợp đồng |
| Open Interest | Có | Số HĐ chưa đóng | Hợp đồng |
| ĐTNN Mua | Có | KL mua của ĐTNN | Hợp đồng |
| ĐTNN Bán | Có | KL bán của ĐTNN | Hợp đồng |

---

#### FR-003: Hiển thị sổ lệnh (Bid/Offer)

| Thuộc tính | Nội dung |
|------------|----------|
| **ID** | FR-003 |
| **Tên** | Hiển thị sổ lệnh phái sinh |
| **Mô tả** | Hệ thống phải hiển thị sổ lệnh với 10 bước giá mua/bán tốt nhất |
| **Độ ưu tiên** | Cao (Must have) |
| **Nguồn yêu cầu** | Product Owner |

**Chi tiết yêu cầu:**

| # | Yêu cầu chi tiết |
|---|------------------|
| FR-003.1 | Hiển thị 10 bước giá MUA tốt nhất (Bid 1-10) |
| FR-003.2 | Hiển thị 10 bước giá BÁN tốt nhất (Offer 1-10) |
| FR-003.3 | Mỗi bước giá hiển thị: Giá, Khối lượng |
| FR-003.4 | Hiển thị tổng khối lượng dư mua/bán |
| FR-003.5 | Trong phiên ATO/ATC: Hiển thị giá khớp dự kiến |

**Dữ liệu sổ lệnh:**

| Trường | Mô tả |
|--------|-------|
| Giá mua (Bid Price) | Giá mua tốt nhất tại bước giá |
| KL mua (Bid Volume) | Khối lượng chờ mua tại bước giá |
| Giá bán (Offer Price) | Giá bán tốt nhất tại bước giá |
| KL bán (Offer Volume) | Khối lượng chờ bán tại bước giá |
| Tổng KL dư mua | Tổng khối lượng chờ mua |
| Tổng KL dư bán | Tổng khối lượng chờ bán |
| Giá khớp dự kiến | Giá dự kiến khớp (ATO/ATC) |
| KL khớp dự kiến | KL dự kiến khớp (ATO/ATC) |

---

#### FR-004: Cập nhật giá Real-time

| Thuộc tính | Nội dung |
|------------|----------|
| **ID** | FR-004 |
| **Tên** | Cập nhật giá phái sinh real-time |
| **Mô tả** | Hệ thống phải cập nhật giá phái sinh liên tục trong phiên giao dịch |
| **Độ ưu tiên** | Cao (Must have) |
| **Nguồn yêu cầu** | Product Owner |

**Chi tiết yêu cầu:**

| # | Yêu cầu chi tiết |
|---|------------------|
| FR-004.1 | Giá phải được cập nhật ngay khi có giao dịch mới |
| FR-004.2 | Sổ lệnh phải được cập nhật khi có thay đổi |
| FR-004.3 | Thời gian trễ tối đa: 1 giây |
| FR-004.4 | Hiển thị rõ bên khớp (Mua chủ động / Bán chủ động) |
| FR-004.5 | Cập nhật liên tục trong các phiên: ATO, LO, ATC |

---

#### FR-005: Phân biệt cơ sở và phái sinh

| Thuộc tính | Nội dung |
|------------|----------|
| **ID** | FR-005 |
| **Tên** | Phân biệt mã cơ sở và phái sinh |
| **Mô tả** | Hệ thống phải cung cấp cách phân biệt rõ ràng giữa mã cơ sở và phái sinh |
| **Độ ưu tiên** | Cao (Must have) |
| **Nguồn yêu cầu** | Business Analyst |

**Chi tiết yêu cầu:**

| # | Yêu cầu chi tiết |
|---|------------------|
| FR-005.1 | Mỗi mã phải có trường xác định loại thị trường |
| FR-005.2 | Mã cơ sở: Không có trường market hoặc market khác "derivatives" |
| FR-005.3 | Mã phái sinh: Có trường market = "derivatives" |
| FR-005.4 | App có thể filter chỉ lấy mã cơ sở hoặc chỉ lấy mã phái sinh |

---

#### FR-006: Thông tin riêng của phái sinh

| Thuộc tính | Nội dung |
|------------|----------|
| **ID** | FR-006 |
| **Tên** | Thông tin đặc thù của mã phái sinh |
| **Mô tả** | Hệ thống phải cung cấp các thông tin chỉ có ở phái sinh |
| **Độ ưu tiên** | Trung bình (Should have) |
| **Nguồn yêu cầu** | Product Owner |

**Chi tiết yêu cầu:**

| # | Yêu cầu chi tiết | Mô tả |
|---|------------------|-------|
| FR-006.1 | Mã cơ sở (Base Code) | Index cơ sở của hợp đồng (VN30) |
| FR-006.2 | Ngày bắt đầu giao dịch | Ngày đầu tiên HĐ được GD |
| FR-006.3 | Ngày đáo hạn | Ngày cuối cùng HĐ được GD |
| FR-006.4 | Số ngày còn lại | Số ngày đến khi đáo hạn |
| FR-006.5 | Giá lý thuyết | Giá tính theo công thức |
| FR-006.6 | Basis | Chênh lệch giá Futures - Spot |
| FR-006.7 | Open Interest | Số HĐ mở chưa đóng |

---

### 4.2 Tổng hợp yêu cầu chức năng

| ID | Tên yêu cầu | Độ ưu tiên | Trạng thái |
|----|-------------|------------|------------|
| FR-001 | Hiển thị danh sách mã phái sinh | Must have | Draft |
| FR-002 | Hiển thị thông tin giá mã phái sinh | Must have | Draft |
| FR-003 | Hiển thị sổ lệnh (Bid/Offer) | Must have | Draft |
| FR-004 | Cập nhật giá Real-time | Must have | Draft |
| FR-005 | Phân biệt cơ sở và phái sinh | Must have | Draft |
| FR-006 | Thông tin riêng của phái sinh | Should have | Draft |

---

## 5. User Stories

### Epic: Xem thông tin giao dịch phái sinh

#### US-001: Xem danh sách mã phái sinh

```
AS A     nhà đầu tư
I WANT   xem được danh sách các mã hợp đồng tương lai đang giao dịch
SO THAT  tôi biết được có những mã nào để theo dõi và đầu tư
```

**Acceptance Criteria:**

| # | Tiêu chí | Kết quả mong đợi |
|---|----------|------------------|
| AC1 | Mở app và vào mục Market | Hiển thị được danh sách mã phái sinh |
| AC2 | Danh sách bao gồm | Tất cả mã VN30F đang giao dịch (thường 4 mã) |
| AC3 | Mỗi mã hiển thị | Mã, Tên, Giá, Thay đổi, % Thay đổi |
| AC4 | Phân biệt với mã cơ sở | Có nhãn hoặc section riêng cho phái sinh |

---

#### US-002: Xem chi tiết giá mã phái sinh

```
AS A     nhà đầu tư
I WANT   xem chi tiết giá của một mã hợp đồng tương lai
SO THAT  tôi có đầy đủ thông tin để ra quyết định đầu tư
```

**Acceptance Criteria:**

| # | Tiêu chí | Kết quả mong đợi |
|---|----------|------------------|
| AC1 | Chọn một mã phái sinh | Hiển thị màn hình chi tiết |
| AC2 | Thông tin giá | Giá hiện tại, Thay đổi, % Thay đổi |
| AC3 | Giá tham chiếu | Trần, Sàn, Tham chiếu |
| AC4 | Giá trong ngày | Mở cửa, Cao nhất, Thấp nhất |
| AC5 | Khối lượng | KL giao dịch, Open Interest |
| AC6 | ĐTNN | KL mua, KL bán của ĐTNN |

---

#### US-003: Xem sổ lệnh phái sinh

```
AS A     nhà đầu tư
I WANT   xem được sổ lệnh của mã hợp đồng tương lai
SO THAT  tôi biết được cung cầu thị trường để đặt lệnh hợp lý
```

**Acceptance Criteria:**

| # | Tiêu chí | Kết quả mong đợi |
|---|----------|------------------|
| AC1 | Sổ lệnh hiển thị | 10 bước giá mua và 10 bước giá bán |
| AC2 | Mỗi bước giá | Giá + Khối lượng |
| AC3 | Tổng hợp | Tổng KL dư mua, Tổng KL dư bán |
| AC4 | Phiên ATO/ATC | Hiển thị giá khớp dự kiến |

---

#### US-004: Theo dõi giá real-time

```
AS A     nhà đầu tư
I WANT   giá phái sinh được cập nhật real-time
SO THAT  tôi có thể theo dõi biến động giá và ra quyết định kịp thời
```

**Acceptance Criteria:**

| # | Tiêu chí | Kết quả mong đợi |
|---|----------|------------------|
| AC1 | Trong phiên giao dịch | Giá cập nhật ngay khi có lệnh khớp |
| AC2 | Độ trễ | Không quá 1 giây |
| AC3 | Sổ lệnh | Cập nhật khi có lệnh mới vào sổ |
| AC4 | Hiệu ứng | Highlight khi giá thay đổi |

---

#### US-005: Xem thông tin đáo hạn

```
AS A     nhà đầu tư
I WANT   xem được thông tin về ngày đáo hạn của hợp đồng
SO THAT  tôi biết được còn bao lâu đến khi hợp đồng đáo hạn
```

**Acceptance Criteria:**

| # | Tiêu chí | Kết quả mong đợi |
|---|----------|------------------|
| AC1 | Ngày đáo hạn | Hiển thị ngày đáo hạn của HĐ |
| AC2 | Số ngày còn lại | Hiển thị số ngày đến đáo hạn |
| AC3 | Mã cơ sở | Hiển thị index cơ sở (VN30) |

---

#### US-006: Filter cơ sở/phái sinh

```
AS A     nhà đầu tư
I WANT   có thể lọc chỉ xem cơ sở hoặc chỉ xem phái sinh
SO THAT  tôi tập trung vào loại sản phẩm tôi quan tâm
```

**Acceptance Criteria:**

| # | Tiêu chí | Kết quả mong đợi |
|---|----------|------------------|
| AC1 | Không filter | Hiển thị cả cơ sở và phái sinh |
| AC2 | Filter cơ sở | Chỉ hiển thị mã cơ sở |
| AC3 | Filter phái sinh | Chỉ hiển thị mã phái sinh |

---

### Tổng hợp User Stories

| ID | User Story | Priority | Story Points |
|----|------------|----------|--------------|
| US-001 | Xem danh sách mã phái sinh | High | 5 |
| US-002 | Xem chi tiết giá mã phái sinh | High | 5 |
| US-003 | Xem sổ lệnh phái sinh | High | 5 |
| US-004 | Theo dõi giá real-time | High | 8 |
| US-005 | Xem thông tin đáo hạn | Medium | 3 |
| US-006 | Filter cơ sở/phái sinh | Medium | 3 |
| **Total** | | | **29** |

---

## 6. Quy tắc nghiệp vụ

### 6.1 Quy tắc về dữ liệu

| ID | Quy tắc | Mô tả |
|----|---------|-------|
| BR-001 | Phân biệt thị trường | Mã phái sinh PHẢI có trường `market = "derivatives"` |
| BR-002 | Loại sản phẩm | Mã phái sinh PHẢI có trường `type = "FUTURES"` |
| BR-003 | Mã cơ sở | Mỗi mã phái sinh PHẢI có mã cơ sở tương ứng (VN30) |
| BR-004 | Ngày đáo hạn | Mỗi mã phái sinh PHẢI có ngày đáo hạn |

### 6.2 Quy tắc về thời gian

| ID | Quy tắc | Mô tả |
|----|---------|-------|
| BR-005 | Cập nhật đầu ngày | Danh sách mã PHẢI được cập nhật trước 8:30 sáng |
| BR-006 | Real-time | Độ trễ dữ liệu real-time KHÔNG ĐƯỢC vượt quá 1 giây |
| BR-007 | Ngày nghỉ | Hệ thống KHÔNG cập nhật vào ngày nghỉ/lễ |

### 6.3 Quy tắc về tính toàn vẹn

| ID | Quy tắc | Mô tả |
|----|---------|-------|
| BR-008 | Isolation | Lỗi phái sinh KHÔNG ĐƯỢC ảnh hưởng đến cơ sở |
| BR-009 | Graceful Degradation | Nếu dữ liệu phái sinh lỗi, hệ thống PHẢI tiếp tục hoạt động bình thường với cơ sở |
| BR-010 | Data Validation | Giá PHẢI nằm trong khoảng [sàn, trần] |

### 6.4 Phiên giao dịch phái sinh

| Phiên | Thời gian | Loại lệnh | Đặc điểm |
|-------|-----------|-----------|----------|
| **ATO** | 08:45 - 09:00 | ATO, LO | Khớp giá mở cửa |
| **Liên tục sáng** | 09:00 - 11:30 | LO, MTL, MOK, MAK | Khớp liên tục |
| **Nghỉ trưa** | 11:30 - 13:00 | - | Không giao dịch |
| **Liên tục chiều** | 13:00 - 14:30 | LO, MTL, MOK, MAK | Khớp liên tục |
| **ATC** | 14:30 - 14:45 | ATC, LO | Khớp giá đóng cửa |

---

## 7. Yêu cầu dữ liệu

### 7.1 Data Dictionary - Mã phái sinh

| Trường | Tên hiển thị | Kiểu | Bắt buộc | Mô tả | Ví dụ |
|--------|--------------|------|----------|-------|-------|
| code | Mã | String | Có | Mã hợp đồng | VN30F2501 |
| name | Tên | String | Có | Tên đầy đủ | VN30 Future Jan 2025 |
| type | Loại | Enum | Có | Loại sản phẩm | FUTURES |
| market | Thị trường | String | Có | Định danh thị trường | derivatives |
| marketType | Phân loại | String | Có | Phân loại chi tiết | VN30F |

### 7.2 Data Dictionary - Thông tin giá

| Trường | Tên hiển thị | Kiểu | Đơn vị | Mô tả |
|--------|--------------|------|--------|-------|
| last | Giá hiện tại | Decimal | Điểm | Giá khớp gần nhất |
| change | Thay đổi | Decimal | Điểm | Chênh lệch vs tham chiếu |
| rate | % Thay đổi | Decimal | % | Tỷ lệ thay đổi |
| ceilingPrice | Giá trần | Decimal | Điểm | Giá cao nhất được phép |
| floorPrice | Giá sàn | Decimal | Điểm | Giá thấp nhất được phép |
| referencePrice | Giá tham chiếu | Decimal | Điểm | Giá tham chiếu đầu ngày |
| open | Giá mở cửa | Decimal | Điểm | Giá mở cửa trong ngày |
| high | Giá cao nhất | Decimal | Điểm | Giá cao nhất trong ngày |
| low | Giá thấp nhất | Decimal | Điểm | Giá thấp nhất trong ngày |
| tradingVolume | Khối lượng | Long | Hợp đồng | Tổng KL giao dịch |
| openInterest | Open Interest | Long | Hợp đồng | Số HĐ mở chưa đóng |

### 7.3 Data Dictionary - Thông tin đặc thù phái sinh

| Trường | Tên hiển thị | Kiểu | Mô tả |
|--------|--------------|------|-------|
| baseCode | Mã cơ sở | String | Index cơ sở (VN30) |
| firstTradingDate | Ngày bắt đầu | Date | Ngày đầu tiên GD |
| lastTradingDate | Ngày đáo hạn | Date | Ngày cuối cùng GD |
| remainingDays | Ngày còn lại | Integer | Số ngày đến đáo hạn |
| theoryPrice | Giá lý thuyết | Decimal | Giá tính theo công thức |
| basis | Basis | Decimal | Futures - Spot |

### 7.4 Data Dictionary - Sổ lệnh

| Trường | Tên hiển thị | Kiểu | Mô tả |
|--------|--------------|------|-------|
| bidPrice | Giá mua | Decimal | Giá mua tốt nhất |
| bidVolume | KL mua | Long | Khối lượng chờ mua |
| offerPrice | Giá bán | Decimal | Giá bán tốt nhất |
| offerVolume | KL bán | Long | Khối lượng chờ bán |
| totalBidVolume | Tổng KL mua | Long | Tổng KL dư mua |
| totalOfferVolume | Tổng KL bán | Long | Tổng KL dư bán |
| expectedPrice | Giá dự kiến | Decimal | Giá khớp dự kiến (ATO/ATC) |
| expectedVolume | KL dự kiến | Long | KL khớp dự kiến |

### 7.5 Ánh xạ trạng thái giá

| Mã | Trạng thái | Màu hiển thị |
|----|------------|--------------|
| 0, 3 | Tham chiếu | Vàng |
| 1 | Trần | Tím |
| 2 | Tăng | Xanh |
| 4 | Sàn | Xanh dương |
| 5 | Giảm | Đỏ |

### 7.6 Ánh xạ trạng thái thị trường

| Mã | Phiên | Mô tả |
|----|-------|-------|
| P | ATO | Phiên mở cửa |
| O, R | LO | Phiên liên tục |
| I | Nghỉ trưa | Intermission |
| A | ATC | Phiên đóng cửa |
| C | PLO | Post Limit Order |
| K, G | Đóng cửa | Closed |

---

## 8. Quy trình nghiệp vụ

### 8.1 Quy trình cập nhật dữ liệu đầu ngày

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                   QUY TRÌNH CẬP NHẬT DỮ LIỆU ĐẦU NGÀY                       │
└─────────────────────────────────────────────────────────────────────────────┘

    [Bắt đầu]
        │
        ▼
    ┌─────────────────────────────┐
    │ Kiểm tra ngày giao dịch     │
    │ (Không chạy T7, CN, Lễ)     │
    └─────────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
    [Ngày nghỉ]        [Ngày GD]
        │                   │
        ▼                   ▼
    [Kết thúc]         ┌─────────────────────────────┐
                       │ Lấy danh sách mã CƠ SỞ     │
                       │ (Quy trình hiện tại)        │
                       └─────────────┬───────────────┘
                                     │
                                     ▼
                       ┌─────────────────────────────┐
                       │ Lấy danh sách mã PHÁI SINH  │
                       │ (Quy trình mới)             │
                       └─────────────┬───────────────┘
                                     │
                            ┌────────┴────────┐
                            │                 │
                            ▼                 ▼
                       [Thành công]      [Thất bại]
                            │                 │
                            │                 ▼
                            │            ┌─────────────────────────┐
                            │            │ Ghi log cảnh báo        │
                            │            │ Tiếp tục với danh sách  │
                            │            │ cơ sở (không dừng hệ    │
                            │            │ thống)                   │
                            │            └─────────────┬───────────┘
                            │                         │
                            └───────────┬─────────────┘
                                        │
                                        ▼
                       ┌─────────────────────────────┐
                       │ Gộp danh sách:              │
                       │ Cơ sở + Phái sinh           │
                       └─────────────┬───────────────┘
                                     │
                                     ▼
                       ┌─────────────────────────────┐
                       │ Lưu vào hệ thống:           │
                       │ • Cache (Redis)             │
                       │ • Database (MongoDB)        │
                       │ • File (MinIO/S3)           │
                       └─────────────┬───────────────┘
                                     │
                                     ▼
                                [Kết thúc]
```

**Thời điểm chạy:** 08:00 - 08:30 hàng ngày (trước giờ mở cửa)

**Kết quả mong đợi:**
- File `symbol_static.json` chứa cả mã cơ sở và phái sinh
- Mã phái sinh có trường `market: "derivatives"`
- Hệ thống sẵn sàng phục vụ khi thị trường mở cửa

---

### 8.2 Quy trình cập nhật giá Real-time

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                   QUY TRÌNH CẬP NHẬT GIÁ REAL-TIME                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                         NGUỒN DỮ LIỆU (Lotte)                               │
│                                                                             │
│   [Giá cơ sở]                              [Giá phái sinh]                  │
│       │                                         │                           │
└───────│─────────────────────────────────────────│───────────────────────────┘
        │                                         │
        ▼                                         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     HỆ THỐNG XỬ LÝ (TradeX)                                 │
│                                                                             │
│   ┌─────────────────────┐        ┌─────────────────────┐                   │
│   │ Xử lý dữ liệu       │        │ Xử lý dữ liệu       │                   │
│   │ Cơ sở               │        │ Phái sinh           │                   │
│   │ (Quy trình hiện tại)│        │ (Quy trình mới)     │                   │
│   └──────────┬──────────┘        └──────────┬──────────┘                   │
│              │                              │                               │
│              │    ┌─────────────────────┐   │                               │
│              └───►│ Lưu vào Cache       │◄──┘                               │
│                   │ (Redis)             │                                   │
│                   └──────────┬──────────┘                                   │
│                              │                                              │
└──────────────────────────────│──────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     PHÂN PHỐI ĐẾN CLIENT                                    │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                    WebSocket Server                                  │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│              │                                    │                         │
│              ▼                                    ▼                         │
│   ┌──────────────────────┐          ┌──────────────────────┐              │
│   │ Channel: market.quote│          │ Channel: market.quote│              │
│   │ .{mã cơ sở}          │          │ .dr.{mã phái sinh}   │              │
│   └──────────────────────┘          └──────────────────────┘              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ỨNG DỤNG CLIENT                                     │
│                                                                             │
│   [App NHSV Pro - Màn hình bảng giá]                                        │
│   • Hiển thị giá cập nhật real-time                                         │
│   • Highlight khi giá thay đổi                                              │
│   • Phân biệt mã cơ sở và phái sinh                                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Đảm bảo:**
- Độ trễ < 1 giây
- Lỗi phái sinh không ảnh hưởng cơ sở
- Cập nhật liên tục trong phiên giao dịch

---

### 8.3 Quy trình xem thông tin mã (User Flow)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                   HÀNH TRÌNH NGƯỜI DÙNG XEM GIÁ PHÁI SINH                   │
└─────────────────────────────────────────────────────────────────────────────┘

    [Mở app NHSV Pro]
            │
            ▼
    ┌───────────────────────────────┐
    │ Vào mục Thị trường / Market   │
    └───────────────┬───────────────┘
                    │
                    ▼
    ┌───────────────────────────────┐
    │ Chọn tab Phái sinh            │
    │ (hoặc filter = derivatives)   │
    └───────────────┬───────────────┘
                    │
                    ▼
    ┌───────────────────────────────┐
    │ Xem danh sách mã phái sinh    │
    │ • VN30F2501 (Jan 2025)        │
    │ • VN30F2502 (Feb 2025)        │
    │ • VN30F2503 (Mar 2025)        │
    │ • VN30F2506 (Jun 2025)        │
    └───────────────┬───────────────┘
                    │
                    ▼
    ┌───────────────────────────────┐
    │ Chọn một mã (VN30F2501)       │
    └───────────────┬───────────────┘
                    │
                    ▼
    ┌───────────────────────────────────────────────────────────────────────┐
    │                    MÀN HÌNH CHI TIẾT MÃ PHÁI SINH                      │
    │                                                                        │
    │   ┌────────────────────────────────────────────────────────────────┐  │
    │   │  VN30F2501 - VN30 Future Jan 2025                              │  │
    │   │  Đáo hạn: 30/01/2025 (còn 15 ngày)                             │  │
    │   └────────────────────────────────────────────────────────────────┘  │
    │                                                                        │
    │   ┌─────────────────────┬─────────────────────────────────────────┐   │
    │   │     THÔNG TIN GIÁ   │              SỔ LỆNH                    │   │
    │   ├─────────────────────┼─────────────────────────────────────────┤   │
    │   │ Giá hiện tại: 1285.5│  MUA          |          BÁN            │   │
    │   │ Thay đổi: +12.5     │  1,200 | 1285.0 | 1285.5 | 1,000       │   │
    │   │ % Thay đổi: +0.98%  │    800 | 1284.5 | 1286.0 | 1,100       │   │
    │   │                     │  1,500 | 1284.0 | 1286.5 |   900       │   │
    │   │ Trần: 1350.0        │    ...         |         ...           │   │
    │   │ Sàn: 1220.0         │                                         │   │
    │   │ TC: 1273.0          │  Tổng mua: 5,000 | Tổng bán: 4,500     │   │
    │   │                     │                                         │   │
    │   │ Mở: 1275.0          ├─────────────────────────────────────────┤   │
    │   │ Cao: 1290.0         │           THÔNG TIN KHÁC                │   │
    │   │ Thấp: 1270.0        │  Open Interest: 45,000 HĐ               │   │
    │   │                     │  Mã cơ sở: VN30                         │   │
    │   │ KL: 125,000 HĐ      │  Basis: -5.0                            │   │
    │   │ ĐTNN mua: 5,000     │  Giá lý thuyết: 1280.0                  │   │
    │   │ ĐTNN bán: 3,000     │                                         │   │
    │   └─────────────────────┴─────────────────────────────────────────┘   │
    │                                                                        │
    └────────────────────────────────────────────────────────────────────────┘
```

---

## 9. Yêu cầu phi chức năng

### 9.1 Hiệu năng (Performance)

| ID | Yêu cầu | Target | Cách đo |
|----|---------|--------|---------|
| NFR-001 | Độ trễ real-time | < 1 giây | Thời gian từ Lotte → App |
| NFR-002 | Thời gian load danh sách | < 2 giây | Thời gian API response |
| NFR-003 | Không ảnh hưởng cơ sở | < 5% latency increase | So sánh trước/sau |

### 9.2 Độ tin cậy (Reliability)

| ID | Yêu cầu | Target | Cách đo |
|----|---------|--------|---------|
| NFR-004 | Uptime hệ thống cơ sở | 99.9% | Monitoring |
| NFR-005 | Graceful degradation | 100% | Khi phái sinh lỗi, cơ sở vẫn hoạt động |
| NFR-006 | Data accuracy | 100% | Giá phải khớp với nguồn Lotte |

### 9.3 Khả năng mở rộng (Scalability)

| ID | Yêu cầu | Target |
|----|---------|--------|
| NFR-007 | Số mã phái sinh | Hỗ trợ đến 20 mã |
| NFR-008 | Số người dùng đồng thời | Không giảm so với hiện tại |

### 9.4 Bảo trì (Maintainability)

| ID | Yêu cầu | Target |
|----|---------|--------|
| NFR-009 | Feature toggle | Có thể bật/tắt phái sinh qua config |
| NFR-010 | Logging | Đầy đủ log để debug |
| NFR-011 | Monitoring | Alert khi có lỗi phái sinh |

---

## 10. Tiêu chí chấp nhận

### 10.1 Tiêu chí chấp nhận tổng thể

| # | Tiêu chí | Điều kiện pass |
|---|----------|----------------|
| 1 | Danh sách mã phái sinh | Hiển thị đủ tất cả mã đang giao dịch |
| 2 | Thông tin giá | Đầy đủ các trường theo Data Dictionary |
| 3 | Sổ lệnh | 10 bước giá mua/bán |
| 4 | Real-time | Cập nhật trong vòng 1 giây |
| 5 | Phân biệt cơ sở/phái sinh | Có trường `market = "derivatives"` |
| 6 | Không ảnh hưởng cơ sở | Hệ thống cơ sở hoạt động bình thường khi phái sinh lỗi |

### 10.2 Checklist kiểm thử

**Functional Testing:**

- [ ] Danh sách mã phái sinh hiển thị đúng
- [ ] Thông tin giá hiển thị đầy đủ và chính xác
- [ ] Sổ lệnh hiển thị 10 bước giá
- [ ] Giá cập nhật real-time
- [ ] Phân biệt được cơ sở và phái sinh
- [ ] Filter theo market type hoạt động

**Non-Functional Testing:**

- [ ] Độ trễ < 1 giây
- [ ] API response < 2 giây
- [ ] Hệ thống cơ sở không bị ảnh hưởng
- [ ] Graceful degradation khi Lotte API lỗi

**Regression Testing:**

- [ ] Tất cả chức năng cơ sở hiện tại vẫn hoạt động
- [ ] Performance không giảm đáng kể

---

## 11. Phụ lục

### 11.1 Danh sách mã phái sinh tham khảo

| Mã | Tên | Tháng đáo hạn | Mã cơ sở |
|----|-----|---------------|----------|
| VN30F2501 | VN30 Future Jan 2025 | 01/2025 | VN30 |
| VN30F2502 | VN30 Future Feb 2025 | 02/2025 | VN30 |
| VN30F2503 | VN30 Future Mar 2025 | 03/2025 | VN30 |
| VN30F2506 | VN30 Future Jun 2025 | 06/2025 | VN30 |

**Quy tắc đặt tên:**
- VN30F = VN30 Future
- 25 = năm 2025
- 01 = tháng 1

### 11.2 So sánh cơ sở vs phái sinh

| Đặc điểm | Cơ sở (Cổ phiếu) | Phái sinh (Futures) |
|----------|------------------|---------------------|
| Loại (t) | STOCK, ETF, CW | FUTURES |
| Thị trường (m) | HOSE, HNX, UPCOM | derivatives |
| Đơn vị giao dịch | Cổ phiếu | Hợp đồng |
| Đơn vị giá | VND | Điểm |
| Open Interest | Không có | Có |
| Ngày đáo hạn | Không có | Có |
| Mã cơ sở | Không có | Có (VN30) |

### 11.3 Format symbol_static.json

**Mã cơ sở (hiện tại):**
```json
{
    "s": "A32",
    "m": "UPCOM",
    "n1": "CTCP 32",
    "n2": "32 Joint Stock Company",
    "t": "STOCK",
    "re": 34800.0,
    "ce": 40000.0,
    "fl": 29600.0,
    "lq": 6800000
}
```

**Mã phái sinh (mới):**
```json
{
    "s": "VN30F2501",
    "m": "derivatives",
    "n1": "HĐ Tương lai VN30 Tháng 01/2025",
    "n2": "VN30 Index Futures Jan 2025",
    "t": "FUTURES",
    "re": 1273.0,
    "ce": 1350.0,
    "fl": 1220.0,
    "lq": 0,
    "bc": "VN30",
    "ed": "20250130",
    "rd": 15
}
```

**Field mapping:**

| Field | Tên đầy đủ | Mô tả | Cơ sở | Phái sinh |
|-------|------------|-------|-------|-----------|
| s | symbol | Mã chứng khoán | ✅ | ✅ |
| m | market | Thị trường | HOSE/HNX/UPCOM | derivatives |
| n1 | name1 | Tên tiếng Việt | ✅ | ✅ |
| n2 | name2 | Tên tiếng Anh | ✅ | ✅ |
| t | type | Loại | STOCK/ETF/CW | FUTURES |
| re | reference | Giá tham chiếu | ✅ | ✅ |
| ce | ceiling | Giá trần | ✅ | ✅ |
| fl | floor | Giá sàn | ✅ | ✅ |
| lq | listedQty | KL niêm yết | ✅ | 0 (không áp dụng) |
| bc | baseCode | Mã cơ sở | ❌ | ✅ (VN30) |
| ed | endDate | Ngày đáo hạn | ❌ | ✅ (yyyyMMdd) |
| rd | remainDays | Số ngày còn lại | ❌ | ✅ |

### 11.3 Glossary

| Thuật ngữ | Tiếng Việt | Giải thích |
|-----------|------------|------------|
| Futures | Hợp đồng tương lai | Sản phẩm phái sinh dựa trên VN30 |
| Open Interest (OI) | Số hợp đồng mở | Số HĐ chưa đóng vị thế |
| Basis | Basis | Chênh lệch Futures - Spot |
| Expiry Date | Ngày đáo hạn | Ngày cuối cùng giao dịch HĐ |
| Bid | Giá mua | Giá người mua sẵn sàng trả |
| Offer/Ask | Giá bán | Giá người bán yêu cầu |
| ATO | At The Open | Phiên khớp lệnh mở cửa |
| ATC | At The Close | Phiên khớp lệnh đóng cửa |
| LO | Limit Order | Phiên khớp lệnh liên tục |

### 11.4 Tài liệu tham khảo

| Tài liệu | Vị trí |
|----------|--------|
| Lotte API Specs (Phái sinh) | `Derivatives/Documentation/[API specs]Lotte_DR.md` |
| Lotte WebSocket (Phái sinh) | `Derivatives/Documentation/Websocket_DR_Lotte.md` |
| TradeX Init Job Knowledge | `TradeX Knowledge/init-job.md` |
| TradeX SymbolInfo API | `TradeX Knowledge/symbol-info-api.md` |
| Technical Requirements | `Derivatives/Planning documentation/02_BE_REQUIREMENTS_SPEC.md` |

---

## Approval

| Vai trò | Họ tên | Ngày | Chữ ký |
|---------|--------|------|--------|
| Product Owner | | | |
| Tech Lead | | | |
| QA Lead | | | |
| BA | | | |

---

*Document End - Business Requirements Document v1.0*
