# Derivatives UAT Test Cases

## Mục đích

Bộ test case UAT dành cho phòng ban nghiệp vụ/non-tech để kiểm thử chức năng phái sinh trên app NHSV Pro.

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
| Sheet 5 | `Sheet_5_Open_Positions.tsv` | 32 | 7 |
| **Tổng** | | **262** | **63** |

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

### Sheet 5 — Open Positions (7 categories)

| Category | Range | Nội dung |
|----------|-------|----------|
| Screen & Display | POS-001 → POS-011 | Mở, empty state, mã, Long/Short, quantity, giá, P/L, closable |
| Quick Close | POS-012, POS-013, POS-016, POS-017, POS-021 → POS-024 | Close Long/Short, validation, refresh, double tap, mất mạng |
| Reverse Position | POS-014, POS-015, POS-025, POS-026 | Reverse Long/Short, validation, ngoài giờ |
| Realtime Updates | POS-027 | Vị thế thay đổi khi lệnh khớp |
| Edge Cases | POS-028 → POS-031 | P/L = 0, giá thiếu, pagination, pull-to-refresh |
| Account & Session | POS-019, POS-020, POS-032 | Quyền xem, đổi account, session expired |
| Error Handling | POS-018 | Lỗi tải dữ liệu |

## Test Cases Bổ Sung

Bộ test đã được bổ sung **30 cases mới** (từ so sánh với MTS reference):

| Sheet | Cases mới | Range | Nội dung chính |
|-------|-----------|-------|----------------|
| Market Data | +2 | MD-038, MD-039 | ĐTNN, Home chart fallback |
| Order/Trade | +20 | ORD-077 → ORD-096 | TSĐB biến động, chặn Mua/Bán cùng phiên, giá làm tròn, Stop order kích hoạt, Advance order lifecycle |
| Cash Transaction | +4 | CASH-051 → CASH-054 | Cuối tuần, ngày lễ, note mặc định, note quá dài |
| Asset/Portfolio | +4 | AST-038 → AST-041 | Daily Balance, Cumulative P/L, Margin Call, Force Sell |

## Notes

- Nội dung chỉ mô tả thao tác và kết quả trên app, không yêu cầu tester hiểu backend/API.
- Bộ test được chuyển thể từ tài liệu NHSV Pro Derivatives và file tham chiếu `Derivatives/NHSV-Phái sinh - MTS.tsv`; không copy nguyên trạng từ app khác.
- Các case phụ thuộc phiên giao dịch, trạng thái tài khoản, dữ liệu vị thế hoặc dữ liệu ký quỹ cần chuẩn bị tài khoản test phù hợp trước khi chạy UAT.
- Cột **Category** giúp tester dễ filter/nhóm test cases theo chức năng khi test trong Google Sheets.
