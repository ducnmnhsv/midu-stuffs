# Cash Statement - Derivatives Cash Transaction

> **Module:** Cash Statement  
> **Category:** Cash Transaction  
> **Project:** TradeX Derivatives Integration  
> **Last Updated:** February 23, 2026  
> **Status:** Planning

---

## Overview

Cash Statement module cho phép tra cứu **lịch sử thanh toán** (payment/settlement history) phái sinh theo ngày, bao gồm lãi/lỗ, phí, thuế và trạng thái thanh toán.

### Key Features

| Feature | Lotte API | Status | Description |
|---------|-----------|--------|-------------|
| Payment History | DRACC-023 | 📋 Planning | Tra cứu lịch sử thanh toán theo khoảng thời gian |

---

## Quick Access

| Document | Description | Audience |
|----------|-------------|----------|
| [Cash_Statement_API_Spec.md](./Cash_Statement_API_Spec.md) | API specification cho DRACC-023 | BA, Developers |

---

## API Summary

### DRACC-023: Lịch sử thanh toán (Payment History)

**TradeX Endpoint:** `GET /api/v1/derivatives/cash/statement`

**Purpose:** Tra cứu lịch sử thanh toán phái sinh theo ngày giao dịch và ngày thanh toán

**Key Request Parameters:**
- `accountNumber` - Số tài khoản phái sinh
- `fromDate` - Từ ngày (yyyyMMdd)
- `toDate` - Đến ngày (yyyyMMdd)
- `nextKey` - Token phân trang – load more (optional)
- `fetchCount` - Số bản ghi mỗi trang (default: 20, max: 100)

**Response:** HTTP 200, body = array of statement objects. Empty = `[]`. Pagination token in header `X-Next-Key`.

**Key Response Fields (per item):**
- `tradeDate`, `settleDate` - Ngày giao dịch, ngày thanh toán
- `realizedPnL` - Lãi/Lỗ đã thực hiện
- `fee`, `tax` - Phí, Thuế
- `availableCash` - Số tiền mặt khả dụng
- `shortfallAmount` - Số tiền thiếu
- `realizedPnLPaymentStatus`, `feePaymentStatus`, `taxPaymentStatus` - Đã thanh toán: `YES` \| `NO` (map từ Lotte Y/N)
- `bankFee` - Phí chuyển khoản ngân hàng

---

## Use Cases

### UC-1: Xem sao kê thanh toán hàng ngày

**Scenario:** User mở màn hình "Sao kê tiền" và xem lịch sử thanh toán trong 7 ngày gần nhất

**Expected:**
- Hiển thị danh sách theo ngày giao dịch
- Mỗi dòng: ngày, lãi/lỗ, phí, thuế, số dư khả dụng, trạng thái thanh toán
- Hỗ trợ phân trang khi có nhiều dữ liệu

---

### UC-2: Kiểm tra trạng thái thanh toán

**Scenario:** User cần xác nhận đã thanh toán phí/thuế/lỗ-lãi chưa

**Expected:**
- Các trường `realizedPnLPaymentStatus`, `feePaymentStatus`, `taxPaymentStatus` trả `YES`/`NO` (đã/chưa thanh toán)
- User có thể filter hoặc sort theo trạng thái (client-side)

---

## Business Context

### Khác biệt với Transaction History (Asset)

| API | Mục đích | Dữ liệu trả về |
|-----|----------|----------------|
| **Cash Statement (DRACC-023)** | Lịch sử thanh toán / Sao kê tiền | Ngày thanh toán, lãi/lỗ, phí, thuế, số dư, trạng thái thanh toán |
| **Transaction History** | Lịch sử giao dịch | Chi tiết từng giao dịch (mua/bán, khối lượng, giá, v.v.) |

**Cash Statement** = Sao kê theo ngày (tổng hợp P/L, phí, thuế)  
**Transaction History** = Chi tiết từng lệnh/giao dịch

---

## Related Documents

| Document | Relationship |
|----------|--------------|
| [Cash Transaction README](../README.md) | Parent module |
| [Lotte API Specs - DRACC-023](../../../Documentation/Lotte_DR_API_Specs.md) | Nguồn API Lotte (Section 2.1.4) |
| [VSD Transaction](../VSD%20transaction/README.md) | Giao dịch nộp/rút VSD – có thể xuất hiện trong sao kê |
| [Internal Transfer](../Internal%20transfer/README.md) | Chuyển nội bộ – có thể xuất hiện trong sao kê |

---

**Prepared By:** BA Team  
**Last Review:** February 23, 2026  
**Document Version:** 1.0
