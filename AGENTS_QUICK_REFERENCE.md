# TradeX Monitoring - Agent Quick Reference

> Tool: **Claude Code**. Skills tự động kích hoạt — không cần gọi tay.

## Skills (4)

| Skill | Khi nào Claude Code tự dùng |
|-------|-----------------------------|
| **tradex-orchestrator** | Tạo spec/doc/issue, phân tích API, cập nhật tài liệu → entry point chính |
| **tradex-analyst** | Hỏi về API/service/Kafka flow, trace hệ thống |
| **tradex-creator** | Được orchestrator gọi — tạo API Spec, PM doc, FE Issue |
| **tradex-validator** | Được orchestrator gọi — validate convention sau khi tạo |

## Luồng pipeline điển hình

```
"Tạo spec cho API X"
→ orchestrator tạo team: analyst → creator → validator
→ output: Derivatives/Planning documentation/.../Specifications/X_API_Spec.md
```

## Knowledge-First Rule

Đọc theo thứ tự trước khi scan code:
1. `CLAUDE.md` → hot cache services, Kafka topics
2. `Knowledge/TradeX/API Standards/tradex-api-conventions.md` → naming, response format
3. `Knowledge/TradeX/Planning/` → order API mapping

## TradeX Services Quick Lookup

| Cần gì | Service |
|--------|---------|
| Auth / OTP / Smart OTP | `aaa` |
| Lệnh equity thường | `lotte-bridge` |
| Lệnh Derivatives thường | `rest-proxy` → tuxedo |
| Lệnh điều kiện (Stop/OCO/Trailing/Bull-Bear) | `order-v2` |
| Market data | `market-query-v2` (topic: `market-v2`) |
| Notification push | `notification` |
| eKYC | `ekyc-admin` |

*Last Updated: 2026-06-30*
