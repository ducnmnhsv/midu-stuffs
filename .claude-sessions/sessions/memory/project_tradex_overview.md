---
name: project-tradex-overview
description: "TradeX system architecture, microservices, and core concepts for NHSV Pro"
metadata: 
  node_type: memory
  type: project
  originSessionId: 9a68fa6c-766a-4b45-858d-3338ee7aa7ff
---

TradeX là backend của NHSV Pro — ứng dụng chứng khoán Việt Nam. Sản phẩm chính đang phát triển: **Derivatives trading** (hợp đồng tương lai VN30F).

**Why:** User làm BA/PM phân tích hệ thống, viết spec, tạo issue, và tài liệu cho team dev/PM.

**How to apply:** Luôn frame analysis trong context microservices TradeX (rest-proxy → Kafka → service). Ưu tiên check TradeX Knowledge/ trước khi scan codebase.

**Core Flow:** `Client → rest-proxy → Kafka → Backend Service → Response`

**Key services:** rest-proxy (gateway), aaa (auth), lotte-bridge (trading), market-collector-lotte, realtime-v2, ws-v2, order-v2

**External partners:** Lotte Securities (trading core), FPT (OTP), VietStock (data), OneSignal (push)

**Jira:** Project NHMTS, epic NHMTS-682 (main work epic)
