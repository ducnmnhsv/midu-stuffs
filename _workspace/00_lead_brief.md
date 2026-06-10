# Lead Brief — BE Issues: Basis + asv/asb Fields

## Yêu cầu

Tạo **2 BE issue documents** trong cùng một file hoặc hai file riêng (theo judgment của creator):

---

### Issue 1: [BE] Calculate & Return `bs` (Basis) Field for Derivative Symbols

**Context:**
- Hiện tại BE không trả field `bs` trong market quote response
- FE phải tự tính, nhưng yêu cầu mới là BE cần tính toán và trả sẵn field này
- Basis = Futures Price − Underlying Index Price

**Symbol Mapping:**
| Derivative Prefix | Underlying Index |
|-------------------|-----------------|
| 41I1xxxxx | VN30 |
| 41I2xxxxx | VN100 |

**Acceptance Criteria từ FE spec:**
- 41I1xxxxx → Basis = futures price − VN30 price, correct sign (+/−)
- 41I2xxxxx → Basis = futures price − VN100 price, correct sign (+/−)
- Initial value available via `/api/v2/market/symbolInfo`
- Updates in real-time via socket `market.quote` when either price changes
- Calculated correctly during ATO/ATC sessions (dùng đúng reference price cho từng session)
- No performance degradation

**Câu hỏi cần BE trả lời / clarify trong implementation:**
- Field name trong response: `bs` hay `basis`?
- `bs` có được trả trong `/api/v2/market/symbol/{code}/quote` không hay chỉ trong socket?
- ATO/ATC session: dùng `expectedPrice` hay `currentPrice` cho tính toán?

---

### Issue 2: [BE] Calculate & Return `asv` & `asb` Fields (Active Sell/Buy Volume) in Real-time

**Context:**
- API `/rest/api/v2/market/symbol/41I1G6000/quote` hiện không trả `asv` và `asb`
- `asv` = khớp lũy kế lệnh mua chủ động (active sell — aggressor is buyer)
- `asb` = khớp lũy kế lệnh bán chủ động (active buy — aggressor is seller)
- Data source: socket `market.quote.dr`, field `mb` (matchedBy)
  - `mb = ASK` → mua chủ động → cộng vào `asv`
  - `mb = BID` → bán chủ động → cộng vào `asb`

**Business Logic:**
- Reset `asv` và `asb` về 0 khi bắt đầu phiên mới (session reset)
- Tích lũy theo từng matched trade dựa trên `mb`
- Trả ra trong REST API response và giữ cập nhật qua socket

**Acceptance Criteria:**
- `asv` & `asb` xuất hiện trong `/api/v2/market/symbol/{code}/quote` response
- Giá trị chính xác so với tổng KL khớp theo chiều mua/bán chủ động
- Reset đúng tại đầu phiên giao dịch
- Realtime update qua socket quote
- Không ảnh hưởng performance

---

## Output

Tạo file issue tại:
`/Users/ducnguyen/Documents/project/tradex-monitoring/Derivatives/Planning documentation/Market data/Issues/BE_Market_Quote_Fields_Enhancement.md`

Tuân thủ Issues/ template (Executive Summary + Technical Details).
Không có code blocks trong Executive Summary section.
Kết thúc bằng Document Status footer.
