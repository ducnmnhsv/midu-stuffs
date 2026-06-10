# [BE] Tính và trả field `asv` / `asb` — Khối lượng khớp chủ động lũy kế

## Vấn đề

API `/api/v2/market/symbol/{code}/quote` hiện không trả hai field:
- `asv` — khối lượng khớp **mua chủ động** lũy kế trong phiên
- `asb` — khối lượng khớp **bán chủ động** lũy kế trong phiên

FE cần hai field này để hiển thị trên màn hình Giá hiện tại và Đặt lệnh.

---

## Logic tính toán

Mỗi tick quote từ socket đã có sẵn field `mb` (matchedBy) và `mv` (matchingVolume — KL lệnh khớp gần nhất):

| `mb` | Hành động |
|------|-----------|
| `ASK` | `asv += mv` |
| `BID` | `asb += mv` |

Reset `asv = asb = 0` khi bắt đầu phiên mới.

---

## Nơi cần thay đổi

| Component | Việc cần làm |
|-----------|-------------|
| `realtime-v2` `QuoteUpdateHandler` | Cộng dồn `asv`/`asb` từ `mb` + `mv` mỗi tick |
| `realtime-v2` (session reset) | Reset `asv` = `asb` = 0 khi sang phiên mới |
| `ws-v2` `parser.js` | Thêm `asv`, `asb` vào payload socket publish |
| `market-query-v2` | Trả `asv`, `asb` trong REST response |

---

## Acceptance Criteria

- `asv` và `asb` có trong response `/api/v2/market/symbol/{code}/quote`
- `asv` tăng đúng khi `mb = ASK`, `asb` tăng đúng khi `mb = BID`
- Reset về 0 đầu phiên
- Cập nhật realtime qua socket
- Cross-check: `asv + asb` phải bằng `vo` (tổng KL khớp lũy kế)

---

## Cần BE xác nhận

1. Socket channel đúng là `market.quote.{symbol}` hay `market.quote.dr`?
2. `asv`/`asb` áp dụng cho tất cả mã hay chỉ phái sinh?

---

**Document Status:** Ready for BE grooming
**For:** Backend team (`realtime-v2`, `ws-v2`, `market-query-v2`)
