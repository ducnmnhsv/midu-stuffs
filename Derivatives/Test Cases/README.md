# Derivatives UAT Test Cases

## Mục đích

Bộ test case UAT dành cho phòng ban nghiệp vụ/non-tech để kiểm thử chức năng phái sinh trên app NHSV Pro.

**Sheet 6** (`Sheet_6_System_Backend_Init.tsv`) dành cho **dev / QA kỹ thuật / DevOps**: init market, Redis/Mongo/Kafka, độc lập job — không dùng cho UAT nghiệp vụ thuần app.

**Sheet 7** (`Sheet_7_Open_Derivatives_Sub_Account.tsv`) dành cho **nghiệp vụ / Ops**: mở tiểu khoản phái sinh (sub 80) online — điều kiện, ký HĐ FPT, Trang chủ, thông báo, tra cứu app và cổng Admin (nếu có quyền UAT).

**Sheet 8** (`Sheet_8_End_User_Happy_Path.tsv`) dành cho **end-user / smoke UAT happy path**: luồng khẳng định — mở sub thành công → đặt lệnh thường LO → xem Tài sản và Danh mục. Chỉ gồm kịch bản thành công; không dùng câu điều kiện mơ hồ (“hoặc”, “nếu có”).

## Cách dùng trong Google Sheets

1. Mở một file `.tsv` theo module cần test.
2. Select all content.
3. Copy và paste vào Google Sheets.
4. Google Sheets sẽ tự tách cột vì file dùng tab-separated format.
5. Cột `Actual results` và `Status` để tester cập nhật trong quá trình UAT.

## Sheets

| Sheet | File | Cases | Categories |
| --- | --- | --- | --- |
| Sheet 1 | `Sheet_1_Market_Data.tsv` | 39 | 10 |
| Sheet 2 | `Sheet_2_Order_Trade.tsv` | 96 | 20 |
| Sheet 3 | `Sheet_3_Cash_Transaction.tsv` | 54 | 13 |
| Sheet 4 | `Sheet_4_Asset_Portfolio.tsv` | 41 | 13 |
| Sheet 5 | `Sheet_5_Open_Positions.tsv` | 35 | 9 |
| Sheet 6 | `Sheet_6_System_Backend_Init.tsv` | 22 | 14 |
| Sheet 7 | `Sheet_7_Open_Derivatives_Sub_Account.tsv` | 40 | 9 |
| Sheet 8 | `Sheet_8_End_User_Happy_Path.tsv` | 10 | 4 |
| **Tổng** | | **337** | **92** |

## Columns

- **Category** — Nhóm chức năng (ví dụ: Đặt lệnh, Sửa lệnh, Hủy lệnh, VSD Deposit, v.v.)
- Test case no
- Test case name
- Description
- Preconditions
- Test steps
- Expected results
- Actual results
- Status

## Categories theo Sheet

### Sheet 1 — Market Data (10 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Navigation & Access | MD-001, MD-002 | Mở tab Derivatives, xem không cần đăng nhập |
| Symbol Grouping | MD-003 → MD-005 | Index futures, Gov.Bond future, toàn bộ mã |
| Price Display & Colors | MD-006, MD-007, MD-035 | Màu giá, ký tự đáo hạn, giá trần/sàn |
| Sorting | MD-008 → MD-010 | Sort theo Price, Volume, Change |
| Horizontal Price Board | MD-011 → MD-014 | Bảng giá ngang, đổi nhóm, empty/error state |
| Home Chart | MD-015, MD-036, MD-039 | Chart VN30F front month, fallback |
| Search | MD-016, MD-029, MD-030 | Tìm kiếm mã phái sinh |
| Current Price - Navigation | MD-017, MD-025 | Mở Current price, điều hướng Buy/Sell |
| Current Price - Display | MD-018 → MD-021, MD-024 | Basic info, Bid/Ask, Matched, Chart |
| Realtime Data | MD-022, MD-023, MD-038 | Giá realtime, ĐTNN |
| Error Handling & Connectivity | MD-026 → MD-028, MD-031, MD-032 | Mất mạng, loading chậm, đổi mã |
| Edge Cases | MD-033, MD-034, MD-037 | Chart không dữ liệu, đổi ngôn ngữ |

### Sheet 2 — Order/Trade (20 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Screen & Layout | ORD-001, ORD-002, ORD-047, ORD-048 | Mở Trade, lệnh thường/nhanh, chưa có tài khoản |
| Realtime Price | ORD-003 → ORD-005 | Giá realtime bật/tắt/tự bật lại |
| Price Input | ORD-006, ORD-007, ORD-014 → ORD-018, ORD-051, ORD-054, ORD-055, ORD-089 | CE/REF/FL, Bid/Ask, bước giá, trần/sàn, làm tròn |
| Quantity Input | ORD-008 → ORD-013, ORD-052, ORD-053, ORD-056 → ORD-058 | Max buy/sell, validation quantity |
| Đặt lệnh thường | ORD-019, ORD-020 | Buy/Sell LO thành công |
| Đặt lệnh theo phiên | ORD-024 → ORD-026 | ATO, ATC, MOK/MAK/MTL theo phiên |
| Chặn Mua/Bán cùng phiên | ORD-084 → ORD-088 | Không mua+bán cùng phiên ATO/ATC |
| Dialog xác nhận | ORD-021 → ORD-023 | Confirm dialog, double submit |
| Orderbook & Danh sách lệnh | ORD-027, ORD-028, ORD-066, ORD-067 | Orderbook hiển thị, partial fill |
| Sửa lệnh | ORD-029 → ORD-032, ORD-065, ORD-070, ORD-071 | Sửa giá/quantity, validation |
| Hủy lệnh | ORD-033, ORD-034 | Hủy lệnh, chặn hủy đã khớp |
| Stop Order - Đặt lệnh | ORD-035, ORD-036 | Đặt Stop Buy/Sell |
| Stop Order - Validation | ORD-037, ORD-038, ORD-072, ORD-073 | Trigger price, ngày quá khứ |
| Stop Order - Sửa/Hủy | ORD-039, ORD-040 | Sửa/hủy Stop order |
| Stop Order - Kích hoạt | ORD-092 → ORD-094 | Trigger activation, hết hiệu lực |
| Lệnh đặt trước - Đặt lệnh | ORD-041 | Đặt advance order |
| Lệnh đặt trước - Validation | ORD-042, ORD-074 | Ngày nghỉ, phiên không hợp lệ |
| Lệnh đặt trước - Kích hoạt | ORD-090, ORD-091 | Kích hoạt đúng ngày, ngày nghỉ |
| Lệnh đặt trước - Sửa/Hủy | ORD-043, ORD-095, ORD-096 | Hủy/sửa advance order |
| Xem lịch sử lệnh | ORD-044 → ORD-046, ORD-075, ORD-076 | History thường/stop/advance |
| TSĐB biến động | ORD-077 → ORD-083 | TSĐB sau đặt/hủy/sửa/khớp lệnh |
| Race Conditions | ORD-068, ORD-069 | Sửa/hủy khi lệnh đang khớp |
| Mạng & Xử lý lỗi | ORD-049, ORD-050, ORD-059 → ORD-062 | Symbol halt, mất mạng, back khỏi dialog |
| Phiên giao dịch | ORD-063, ORD-064 | Nghỉ trưa, ngoài giờ |

### Sheet 3 — Cash Transaction (13 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Navigation & Access | CASH-001, CASH-030 | Mở Cash Transaction, chưa có sub 80 |
| Internal Transfer - Form | CASH-002 → CASH-005 | Layout, Available amount, Receiving account, All |
| Internal Transfer - Validation | CASH-006, CASH-007, CASH-031 → CASH-035 | Amount, âm, ký tự chữ, note, trùng account |
| Internal Transfer - Confirm | CASH-008 → CASH-010, CASH-036, CASH-037 | Dialog, thành công, lỗi, double tap, mất mạng |
| Internal Transfer - History | CASH-011 → CASH-013, CASH-039 | Mặc định, lọc ngày, load more, filter sai |
| Internal Transfer - Note handling | CASH-053, CASH-054 | Note mặc định, note quá dài |
| VSD Deposit | CASH-014 → CASH-019, CASH-040 → CASH-043 | Mở, balance, All, phí, confirm, validation |
| VSD Withdraw | CASH-020 → CASH-023, CASH-044, CASH-045 | All, vượt số tiền, confirm, ngoài giờ |
| VSD History | CASH-024 → CASH-026, CASH-046, CASH-047 | Mặc định, lọc Deposit/Withdraw, validation |
| Cash Statement | CASH-027 → CASH-029, CASH-048, CASH-049 | Mở, trạng thái, empty/error, khoảng ngày |
| Mạng & Session | CASH-038, CASH-050 | Lỗi tải số dư, session expired |
| Cuối tuần/Ngày lễ | CASH-051, CASH-052 | Giao dịch cuối tuần, ngày lễ |

### Sheet 4 — Asset/Portfolio (13 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Asset Screen - Overview | AST-001 → AST-009 | Mở, tổng quan, ký quỹ, P/L, refresh, empty state |
| Asset Screen - Format | AST-025, AST-026 | Giá trị âm, số lớn |
| Asset Screen - Refresh | AST-027 | Pull-to-refresh |
| Asset Screen - Error Handling | AST-021 → AST-023 | Lỗi tải, không nhầm cơ sở, chưa có sub 80 |
| Asset Screen - Account | AST-024 | Đổi account |
| Asset Screen - Session | AST-037 | Session expired |
| Portfolio - Display | AST-010 → AST-014 | Mở, mã/chiều, quantity/avg price, realtime, màu P/L |
| Portfolio - Navigation | AST-015 | Điều hướng sang Trade |
| Portfolio - Edge Cases | AST-028 → AST-030 | Empty state, mất realtime, đổi mã nhanh |
| Daily P/L | AST-016 → AST-018, AST-031 → AST-033 | Mở, lọc, validation, empty, pagination |
| Transaction History | AST-019, AST-034 | Lịch sử giao dịch, validation ngày |
| Risk/MU Warning | AST-020, AST-035, AST-036, AST-040, AST-041 | Cảnh báo, bình thường, lỗi, Margin Call, Force Sell |
| Daily Balance | AST-038 | Số dư hàng ngày |
| Cumulative P/L | AST-039 | Lãi/Lỗ cộng dồn |

### Sheet 5 — Open Positions (9 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Screen & Display | POS-001 → POS-011 | Mở, empty state, mã, Long/Short, quantity, giá, P/L, closable |
| Quick Close | POS-012, POS-013, POS-016, POS-017, POS-021 → POS-024 | Close Long/Short, validation, refresh, double tap, mất mạng |
| Reverse Position | POS-014, POS-015, POS-025, POS-026 | Reverse Long/Short, validation, ngoài giờ |
| Realtime Updates | POS-027 | Vị thế thay đổi khi lệnh khớp |
| Edge Cases | POS-028 → POS-031 | P/L = 0, giá thiếu, pagination, pull-to-refresh |
| Account & Session | POS-019, POS-020, POS-032 | Quyền xem, đổi account, session expired |
| Error Handling | POS-018 | Lỗi tải dữ liệu |
| Navigation | POS-033 | Trade → See more → Portfolio |
| Order Book Check | POS-034, POS-035 | Close/Reverse — sổ lệnh MTL |

### Sheet 6 — System / Backend Init (14 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Scope & Naming | SYS-001 | Phạm vi Init Job đúng init-job.md; pipeline khác tách tài liệu |
| Init Pipeline | SYS-002 → SYS-005 | Lotte merge, Redis, MongoDB, symbol_static.json |
| Kafka Path | SYS-006, SYS-007 | Topic symbolInfoUpdate, enableInitMarket direct |
| Derivatives vs Base | SYS-008 | FUTURES trong cùng batch init (~merge), không tách job mặc định |
| Job Independence | SYS-009, SYS-010 | Index constituents (lotte-bridge) vs init symbol; isolation deploy |
| Failure Handling | SYS-011 → SYS-014 | Kafka fail continue, Redis/Mongo abort, threshold, API retry |
| Scheduling | SYS-015 | Holiday/weekend skip |
| Concurrency | SYS-016 | enableMultipleInstance / coordinator |
| Performance | SYS-017 | Load ~2000 mã, publish Kafka |
| Recovery | SYS-018 | Daily Recovery sau init fail |
| Observability | SYS-019 | Metric/alert |
| Manual Ops | SYS-020 | Trigger thủ công downloadSymbol |
| Contract Test | SYS-021 | API indexStockList vs job bridge |
| Data Quality | SYS-022 | Cross-check app Derivatives vs backend FUTURES |

Tham chiếu kiến trúc: `TradeX Knowledge/System/init-job.md`.

### Sheet 7 — Mở tiểu khoản phái sinh (sub 80) — 9 categories

| Category | Range | Nội dung |
|----------|-------|----------|
| Điều kiện mở & chặn sớm | DSA-001 → DSA-006, DSA-037 | Đủ điều kiện vào luồng; chặn theo NH liên kết, đã có sub 80, không lưu ký NHSV, HĐ TKCK cơ sở chưa xong, đang có yêu cầu; copy lỗi dễ hiểu |
| Khởi tạo yêu cầu | DSA-007 → DSA-010, DSA-038 | Tiếp nhận yêu cầu, mở webview ký, link chưa sẵn sàng — ký sau từ Trang chủ, lỗi mạng, chống bấm đúp |
| Ký hợp đồng FPT | DSA-011 → DSA-013, DSA-039 | Ký thành công; từ chối/không ký; đóng webview giữa chừng; đối chiếu thông tin trên HĐ |
| Trang chủ — cảnh báo ký HĐ | DSA-014 → DSA-019 | Không request / chờ ký có CTA / bấm CTA mở lại ký / đã ký không còn nhắc ký / hoàn tất / trạng thái cuối không nhắc ký |
| Hoàn tất & sử dụng dịch vụ | DSA-020, DSA-021 | Sau thành công vào được chức năng phái sinh; giai đoạn chờ Core không gây hiểu nhầm |
| Thông báo (Push / Email) | DSA-022 → DSA-024, DSA-040 | Đã tiếp nhận; thành công; không hoàn tất; nội dung email hoàn tất |
| Tra cứu trạng thái trên app | DSA-025 → DSA-027 | Chờ ký; đã ký đang xử lý; hoàn tất / không hoàn tất |
| Admin / Ops (cổng quản trị) | DSA-028 → DSA-031 | Lọc danh sách; chi tiết timeline; xem PDF; hủy HĐ chờ ký (nếu được phép) |
| Biên & trải nghiệm | DSA-032 → DSA-036 | Đổi tài khoản; đăng xuất giữa chừng; app nền; đổi ngôn ngữ; quá hạn HĐ (kịch bản đặc biệt) |

Tham chiếu nghiệp vụ: spec **NHSV Pro — Open DR sub-account** (luồng FPT eContract → xử lý nền mở sub trên Core → thông báo, Admin, lưu trữ hợp đồng). Test case viết theo góc nhìn **khách hàng / Ops**, không yêu cầu tester biết API hay tên job.

### Sheet 8 — End-user happy path (4 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Luồng trải nghiệm end-user | HAPPY-001 | Smoke E2E: mở sub 80 → đặt lệnh LO → Tài sản + Danh mục |
| Mở tiểu khoản phái sinh | HAPPY-002 → HAPPY-004 | Tiếp nhận yêu cầu → ký FPT → sub 80 thành công + thông báo |
| Đặt lệnh thường | HAPPY-005 → HAPPY-007 | Vào Trade sub 80 → Mua LO → Bán LO thành công |
| Tài sản & Danh mục | HAPPY-008 → HAPPY-010 | Tài sản tổng quan → Danh mục có vị thế → khớp sau giao dịch |

Tham chiếu: Sheet 7 (mở sub), Sheet 2 ORD-019/020 (lệnh LO), Sheet 4 AST-001/010 (Tài sản/Danh mục). Viết **khẳng định** — mỗi expected result mô tả kết quả thành công cụ thể.

## Test Cases Bổ Sung

Bộ test đã được bổ sung **30 cases mới** (từ so sánh với MTS reference), và **40 cases** cho mở tiểu khoản phái sinh (Sheet 7 — spec Open DR sub-account):

| Sheet | Cases mới | Range | Nội dung chính |
|-------|-----------|-------|----------------|
| Market Data | +2 | MD-038, MD-039 | ĐTNN, Home chart fallback |
| Order/Trade | +20 | ORD-077 → ORD-096 | TSĐB biến động, chặn Mua/Bán cùng phiên, giá làm tròn, Stop order kích hoạt, Advance order lifecycle |
| Cash Transaction | +4 | CASH-051 → CASH-054 | Cuối tuần, ngày lễ, note mặc định, note quá dài |
| Asset/Portfolio | +4 | AST-038 → AST-041 | Daily Balance, Cumulative P/L, Margin Call, Force Sell |
| Mở tiểu khoản phái sinh | +40 | DSA-001 → DSA-040 | Spec Open DR sub-account: điều kiện, FPT ký, Home warning, Core xử lý, push/email, Admin/Ops |
| End-user happy path | +10 | HAPPY-001 → HAPPY-010 | Smoke E2E end-user: mở sub → lệnh LO → Tài sản/Danh mục (chỉ happy case, câu khẳng định) |

## Notes

- **Sheet 8** là module **happy path end-user**: dùng làm smoke test đầu phiên UAT hoặc demo luồng chính trước khi chạy Sheet 1–5 (chi tiết) và Sheet 7 (biên/ngoại lệ).
- **Sheet 7** bổ sung test **mở tiểu khoản phái sinh (sub 80)** online: điều kiện, gửi yêu cầu, ký HĐ FPT, cảnh báo Trang chủ, thông báo, tra cứu trên app và **cổng Admin/Ops** (nếu UAT có quyền). Chi tiết kỹ thuật lấy theo spec nội bộ Open DR sub-account; UAT nghiệp vụ chỉ cần đối chiếu hành vi app và cổng quản trị.
- Nội dung chỉ mô tả thao tác và kết quả trên app, không yêu cầu tester hiểu backend/API.
- Bộ test được chuyển thể từ tài liệu NHSV Pro Derivatives và file tham chiếu `Derivatives/NHSV-Phái sinh - MTS.tsv`; không copy nguyên trạng từ app khác.
- Các case phụ thuộc phiên giao dịch, trạng thái tài khoản, dữ liệu vị thế hoặc dữ liệu ký quỹ cần chuẩn bị tài khoản test phù hợp trước khi chạy UAT.
- Cột **Category** giúp tester dễ filter/nhóm test cases theo chức năng khi test trong Google Sheets.
