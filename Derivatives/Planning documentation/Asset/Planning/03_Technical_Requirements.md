# Asset - Technical Requirements

**Status:** TBD  
**Last Updated:** February 13, 2026

---

## Overview

Yêu cầu kỹ thuật cho tích hợp Asset với Lotte.

**Core Requirements:**
- REQ-1: API tra cứu vị thế mở (DRACC-003) – chi tiết xem [Open Position List API Spec](../Specifications/Open_Position_List_API_Spec.md)
- REQ-2: API tra cứu tài sản phái sinh (DRACC-018) – chi tiết xem [Asset Info API Spec](../Specifications/Asset_Info_API_Spec.md)
- REQ-3: API xem lịch sử giao dịch (DRACC-023) – chi tiết xem [Transaction History API Spec](../Specifications/Transaction_History_API_Spec.md)
- REQ-4: API xem lãi lỗ chưa thực hiện (DRACC-031) – chi tiết xem [Unrealized PnL API Spec](../Specifications/Unrealized_PnL_API_Spec.md)
- REQ-5: Tất cả API sử dụng GET method với query parameters
- REQ-6: Phân trang với next_data
- REQ-7: Map Lotte response → TradeX format (camelCase, type enum)

**API Naming Convention:**
- Base: `GET /api/v1/derivatives/asset/{resource}`
- Resource names: camelCase (VD: `openPositions`, `transactionHistory`, `unrealizedPnl`)
- NO kebab-case (gạch ngang)
