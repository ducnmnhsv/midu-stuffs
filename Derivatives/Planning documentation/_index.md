# Derivatives Planning Documentation

> **Project:** TradeX Derivatives Integration  
> **Status:** Planning Phase  
> **Last Updated:** 2025-01-30

---

## Structure

Tài liệu được tổ chức theo **Modules** để dễ dàng quản lý và mở rộng:

```
Derivatives/Planning documentation/
├── _index.md                          ← Bạn đang ở đây
├── 01_DERIVATIVES_INTEGRATION_PLAN.md ← Master Plan (deprecated, use modules)
│
├── Market data/                       ← Module 1: Market Data
│   ├── 00_EXECUTIVE_SUMMARY.md
│   ├── 01_DERIVATIVES_INTEGRATION_PLAN.md
│   ├── 02_BE_REQUIREMENTS_SPEC.md
│   ├── 03_BA_BUSINESS_REQUIREMENTS.md
│   └── 04_USE_CASES_AND_TEST_SCENARIOS.md
│
├── Orders/                            ← Module 2: Orders (Future)
│   └── (coming soon)
│
├── Cash transaction/                  ← Module 3: Cash (Future)
│   └── (coming soon)
│
└── Account/                           ← Module 4: Account (Future)
    └── (coming soon)
```

---

## Modules Overview

| Module | Scope | Status | Priority |
|--------|-------|--------|----------|
| **Market data** | SymbolInfo, WebSocket, Init Job | ✅ Planning Complete | P0 |
| **Orders** | Buy/Sell, Cancel, Modify | 📋 Not Started | P1 |
| **Cash transaction** | Deposit, Withdraw, Transfer | 📋 Not Started | P1 |
| **Account** | Balance, Positions, P&L | 📋 Not Started | P2 |

---

## Module 1: Market Data

**Scope:** Hiển thị thông tin giá phái sinh cho user (không có giao dịch)

| Document | Description | Audience |
|----------|-------------|----------|
| [00_EXECUTIVE_SUMMARY.md](./Market%20data/00_EXECUTIVE_SUMMARY.md) | Tổng quan module | PO, Tech Lead |
| [01_DERIVATIVES_INTEGRATION_PLAN.md](./Market%20data/01_DERIVATIVES_INTEGRATION_PLAN.md) | Technical Plan chi tiết | Architect, BE Lead |
| [02_BE_REQUIREMENTS_SPEC.md](./Market%20data/02_BE_REQUIREMENTS_SPEC.md) | Specs cho BE Developer | BE Developers |
| [03_BA_BUSINESS_REQUIREMENTS.md](./Market%20data/03_BA_BUSINESS_REQUIREMENTS.md) | Business Requirements | PM, BA |
| [04_USE_CASES_AND_TEST_SCENARIOS.md](./Market%20data/04_USE_CASES_AND_TEST_SCENARIOS.md) | Use Cases & Test | BA, QA |

**Key Features:**
- Init job lấy danh sách mã phái sinh
- SymbolInfo API (aggregate từ WebSocket)
- Real-time WebSocket channels
- symbol_static.json update

---

## Module 2: Orders (Future)

**Scope:** Đặt/Sửa/Hủy lệnh phái sinh

**Lotte APIs (Reference):**
- DRORD-029: Lệnh Mua
- DRORD-030: Lệnh Bán
- DRORD-031: Hủy lệnh
- DRORD-032: Sửa lệnh
- DRORD-005/006: Lệnh điều kiện

---

## Module 3: Cash Transaction (Future)

**Scope:** Nộp/Rút tiền ký quỹ, Chuyển khoản nội bộ

**Lotte APIs (Reference):**
- DRACC-009: Rút tiền ký quỹ
- DRACC-019: Chuyển khoản nội bộ
- DRACC-034: Nộp tiền ký quỹ

---

## Module 4: Account (Future)

**Scope:** Tra cứu tài khoản, vị thế, lãi lỗ

**Lotte APIs (Reference):**
- DRACC-003: Danh sách vị thế mở
- DRACC-018: Số dư khả dụng
- DRACC-022: Tổng hợp lãi lỗ
- DRACC-031: Thông tin tài sản

---

## Related Resources

| Resource | Location | Description |
|----------|----------|-------------|
| Lotte API Specs | `Derivatives/Documentation/[API specs]Lotte_DR.md` | Full API documentation |
| Lotte WebSocket | `Derivatives/Documentation/Websocket_DR_Lotte.md` | WS channels specs |
| Agent Rules (TEMPO) | `Derivatives/Agent based rules (tempo)/` | Patterns đã phân tích |
| TradeX Knowledge | `TradeX Knowledge/` | Hiểu biết về hệ thống hiện tại |

---

## Quick Navigation

### Đang làm Market Data?
→ [Market data/00_EXECUTIVE_SUMMARY.md](./Market%20data/00_EXECUTIVE_SUMMARY.md)

### Cần xem API Lotte?
→ [Documentation/[API specs]Lotte_DR.md](../Documentation/[API%20specs]Lotte_DR.md)

### Cần xem patterns đã phân tích?
→ [Agent based rules (tempo)/_index.md](../Agent%20based%20rules%20(tempo)/_index.md)

---

*Last updated: 2025-01-30*
