# [FE] GTGD Chart — Market Watch (Thanh khoản khớp lệnh intraday)

## 📋 Executive Summary (PM READS THIS)

### Problem
Retail investor mở tab **Overall** của Market Watch chưa có cách quan sát nhanh **xu hướng thanh khoản trong ngày** của từng sàn (VNIndex/HNX/UPCOM), càng không có baseline so sánh với phiên trước để phán đoán "chợ hôm nay có sôi động hơn hôm qua không".

### Impact
- **Who:** Toàn bộ user NHSV Pro (retail) mở màn Market Watch — tab Overall.
- **How:** Thiếu tín hiệu "thanh khoản đang mạnh/yếu so với hôm qua" → user phải mở các nguồn ngoài (Fireant, VietstockFinance, VNDirect) để so sánh. Giảm engagement và độ tin cậy của Market Watch như "one-stop" market monitor.

### Solution (HIGH-LEVEL)
Chèn **line chart 2 đường** ("Thanh khoản khớp lệnh") vào tab Overall của Market Watch, phía trên bảng giá:

- **Line 1 — "Hôm nay":** đường tích luỹ trading value khớp lệnh trong phiên hiện tại (teal solid), append điểm mới qua WebSocket realtime.
- **Line 2 — "Phiên trước":** đường tích luỹ của phiên giao dịch T-1 gần nhất (mustard/amber, static baseline).
- **Trục X:** 09:00 → 15:00 (mốc giờ trong phiên VN).
- **Trục Y:** giá trị tích luỹ, format `K` (nghìn tỷ đồng — cần confirm design).
- **Dropdown chọn index:** `VNIndex` / `HNX` / `UPCOM` (dùng lại bottom modal `ModalBottom` có sẵn).
- **Tooltip crosshair:** kéo/chạm → hiện popup 2 giá trị "Hôm nay" vs "Phiên trước" tại timestamp.

---

## 🎨 Figma Reference

- **File:** [NHSV-Pro — GTGD Chart](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40009887-207055&t=6J5Irxu1W5xcoXXi-11)
- **Node ID:** `40009887-207055`
- **Tokens áp dụng (NHSV Design System):**
  - Line "Hôm nay": teal (primary market up-trend), 2px solid.
  - Line "Phiên trước": mustard/amber, 2px solid.
  - Card background: `#FFFFFF` trên nền `#EBECF0`, radius theo design tokens.
  - Font: **Lato** (toàn bộ label, tooltip, số liệu).
  - Tooltip: có 2 variant (light + dark).

---

## 🧩 UI Components

### Affected Screens / Components (nhsv-mts-rn — READ-ONLY reference)

| Path | Change |
|------|--------|
| `src/screens/MarketScreen/index.tsx` | Chèn `IntradayValueChart` vào tab Overall, phía trên `marketPriceBoard` |
| `src/screens/MarketScreen/components/marketTableHeader/index.tsx` | Không sửa; dropdown chọn index tái dùng logic hiện có |
| `src/screens/MarketScreen/components/bottomModal/ModalBottom.tsx` | Không sửa; reuse cho dropdown VNIndex/HNX/UPCOM |
| `src/screens/MarketScreen/marketScreen.type.ts` | Thêm helper map `IMarket` → symbol code (`VNINDEX`/`HNXINDEX`/`UPINDEX`) nếu chưa có |

### New Components

| Component | Path | Vai trò |
|-----------|------|---------|
| `IntradayValueChart` | `src/components/Chart/IntradayValueChart/index.tsx` | Container chart — nhận props `{ indexSymbol, todaySeries, previousSeries, loading, error }`, render 2 line + trục + tooltip crosshair |
| `IntradayValueChartTooltip` | `src/components/Chart/IntradayValueChart/Tooltip.tsx` | Popup crosshair — 2 giá trị "Hôm nay" / "Phiên trước" tại timestamp |
| `IntradayValueChartHeader` | `src/components/Chart/IntradayValueChart/Header.tsx` | Title "Thanh khoản khớp lệnh" + dropdown index (reuse `ModalBottom`) |

### Pattern Reference (đã có trong repo)

- `src/components/Chart/MiniChartRealtime/index.tsx` — pattern line chart + WS append.
- `src/components/Chart/ProfitLossAreaChart/index.tsx` — pattern 2 series comparison + tooltip.
- Library: `react-native-svg-charts@5.4.0` (đã có). **KHÔNG add lib mới** (không dùng `victory-native`).

---

## 🔌 API Integration

### Endpoint (recommend reuse — verify với BE spec)

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **Endpoint** | `/api/v2/tradingview/history` |
| **Service** | `market-query-v2` |
| **Auth** | Public (không cần token) |

### Request Params

| Param | Type | Required | Value cho GTGD chart |
|-------|------|----------|----------------------|
| `symbol` | string | Y | `VNINDEX` / `HNXINDEX` / `UPINDEX` (map từ `IMarket` enum) |
| `resolution` | string | Y | `"1"` (1 phút) |
| `from` | number (epoch s) | Y | 09:00 VN của ngày cần query |
| `to` | number (epoch s) | Y | 15:00 VN của ngày cần query (hoặc `now` cho line hôm nay) |
| `fetchCount` | number | N | Default 300 |

### Response (fields FE dùng)

| Field | Type | Cách dùng |
|-------|------|-----------|
| `t` | `number[]` (epoch s) | Trục X (thời gian) |
| `v` | `number[]` (VND per bucket 1 phút) | **FE cumsum** → line tích luỹ trên trục Y |
| `s` | `"ok"` \| `"no_data"` | Guard cho empty state |

### Gọi cho 2 line

- **"Hôm nay":** `from = start-of-today 09:00 VN`, `to = now`.
- **"Phiên trước":** `from = start-of-previous-trading-day 09:00`, `to = end 15:00`. FE tự map timestamp T-1 sang cùng trục X hôm nay (09:00–15:00) để 2 line so sánh cùng khung giờ.

> **Note:** Nếu BE quyết build endpoint mới `/api/v2/market/index/{symbol}/tradingValue/intraday?includePreviousSession=true` (trả sẵn `today.accValue` + `previous.accValue`) → FE bỏ bước cumsum, saga chỉ cần gọi 1 lần. Xem BE_Issue.md cùng folder.

### Realtime (WebSocket)

| Property | Value |
|----------|-------|
| **Channel** | `market.quote.{indexSymbol}` |
| **Service** | `ws-v2` (Node.js) |
| **Field FE dùng** | `va` (cumulative trading value trong ngày — đã cumulative sẵn, không cần cumsum) |
| **Hành vi** | Mỗi push → append điểm cuối vào line "Hôm nay" |
| **Phiên trước** | KHÔNG subscribe (load 1 lần lúc mount) |

Reference: `Knowledge/TradeX/System/market-data-channels.md` §3.4.

---

## 🗃️ State Management (Redux + Saga)

### New Slice

**File:** `src/reduxs/global-reducers/MarketIntradayValue.ts`

```typescript
interface IIntradayValueState {
  [symbol: string]: {                    // VNINDEX / HNXINDEX / UPINDEX
    today:    { t: number[]; accValue: number[] };
    previous: { t: number[]; accValue: number[] };
    loading: boolean;
    error?: string;
    lastUpdate: number;
  };
}
```

**Actions:**
- `fetchIntradayValue({ symbol })` — trigger saga load cả today + previous.
- `fetchIntradayValueSuccess({ symbol, today, previous })`
- `fetchIntradayValueFailure({ symbol, error })`
- `appendRealtimeValuePoint({ symbol, t, va })` — WS handler dispatch.

### New Saga

**File:** `src/reduxs/sagas/Market/GetIntradayValue.ts`

- Watch `fetchIntradayValue`.
- Parallel 2 calls tới `APIList.getTradingviewHistory` (endpoint có sẵn tại `src/config/api.ts:575`) — today + previous session.
- Resolve "previous trading date": FE tự tính T-1 skip weekend (bước 1); **về lâu dài đề xuất BE trả kèm** (xem BE_Issue.md).
- Sau khi 2 calls thành công → cumsum array `v` → dispatch success.

### Enum bổ sung

- `src/constants/enum.ts` — thêm `PERIOD_TYPE.INTRADAY` nếu muốn thống nhất pattern; hoặc để riêng resolution string.

### WS Subscribe

- Trong `MarketScreen/index.tsx` (hoặc container mới `IntradayValueChartContainer`) — `useEffect` subscribe `market.quote.{currentSymbol}`, dispatch `appendRealtimeValuePoint` khi có message. Unsubscribe khi unmount / đổi symbol.

---

## 🛠️ Implementation Notes

### Data Flow

```
Mount / đổi index
   │
   ├─ dispatch fetchIntradayValue({ symbol })
   │       │
   │       └─ saga → 2× GET /tradingview/history (today + previous)
   │             → cumsum v → success
   │
   └─ subscribe WS market.quote.{symbol}
         └─ mỗi message va → appendRealtimeValuePoint
                             → reducer push (t, va) vào today.accValue
                             → chart re-render append point cuối
```

### Format & Units

- Giá trị VND → chia `10^9` → "K" trên trục Y (Figma ghi "23.017K"). **CONFIRM với design team đơn vị chính xác** (K = nghìn tỷ hay tỷ) trước khi implement — xem Gap #2.
- Timestamp → `dayjs(t*1000).tz('Asia/Ho_Chi_Minh').format('HH:mm')`.
- Chart clamp trục X: 09:00–15:00 VN, không show ngoài phiên.

### Edge Cases

| Case | Behavior |
|------|----------|
| Loading (chưa có data) | Skeleton line placeholder trong card, giữ chiều cao |
| `s === "no_data"` cho phiên trước | Chỉ vẽ line "Hôm nay", hide line "Phiên trước", show hint "Không có dữ liệu phiên trước" nhỏ dưới legend |
| Phiên trước rơi vào ngày nghỉ (Tết, cuối tuần) | Saga skip lùi tiếp — lấy phiên giao dịch hợp lệ gần nhất; hoặc dùng field BE trả (nếu có) |
| Ngoài giờ giao dịch (sau 15:00 / trước 09:00) | Line "Hôm nay" đóng băng tại điểm cuối, không subscribe WS |
| Đổi index khi WS đang stream | Unsubscribe channel cũ → subscribe channel mới, clear state cũ để tránh mix data |
| Error API (timeout / 5xx) | Show error state trong card với nút "Thử lại"; không crash Market Watch |
| Số điểm quá lớn (>300) | fetchCount cap 300 (đủ 270 phút phiên); FE downsample nếu cần cho performance |

### Perf

- Line render dùng `react-native-svg-charts` (SVG) — với ~270 điểm/line không cần optimize thêm.
- Redux selector: memoize per-symbol để tránh re-render toàn Market Watch khi WS push.

### Gaps cần làm rõ (from Analyst)

Xem 7 gaps trong `_workspace/01_analyst_findings.md` — creator kế thừa nguyên. Đáng chú ý:

1. **Endpoint quyết định** — reuse `/tradingview/history` (FE cumsum) vs new endpoint (BE trả sẵn cumulative). Recommend reuse cho v1, đề xuất new endpoint cho v2 nếu payload lớn.
2. **Đơn vị K** — nghìn tỷ hay tỷ? Cần design team xác nhận.
3. **Previous trading date resolution** — FE tính hay BE trả. Recommend BE trả kèm ở endpoint mới.
5. **Refresh** — dùng WS `market.quote.{s}` field `va` (đã có), không cần polling.
6. **Scope index** — v1 chỉ VNIndex/HNX/UPCOM theo Figma. VN30/HNX30 để v2.
8. **Symbol mapping** — verify `IMarket.HOSE` → `VNINDEX` qua `Global.stockList` hoặc `constants/main.ts`.

---

## ✅ Acceptance Criteria

- [ ] Tab **Overall** của Market Watch hiển thị card "Thanh khoản khớp lệnh" phía trên bảng giá.
- [ ] Card có title "Thanh khoản khớp lệnh" + dropdown chọn `VNIndex` / `HNX` / `UPCOM`.
- [ ] Chart render đúng 2 line theo Figma: "Hôm nay" (teal solid) và "Phiên trước" (mustard).
- [ ] Trục X hiển thị mốc `09:00`, `10:00`, `11:00`, `12:00`, `13:00`, `14:00`, `15:00` (VN timezone).
- [ ] Trục Y format ngắn kiểu `23.017K` khớp Figma (sau khi confirm đơn vị).
- [ ] Kéo/chạm chart → tooltip crosshair hiện 2 giá trị "Hôm nay" / "Phiên trước" tại timestamp.
- [ ] Đổi index (VNIndex → HNX) → chart clear state cũ, load lại đúng data cho index mới.
- [ ] Line "Hôm nay" cập nhật realtime qua WS `market.quote.{symbol}` field `va` — điểm mới append mỗi khi có push.
- [ ] Line "Phiên trước" load 1 lần lúc mount, không subscribe WS, không đổi cho tới khi remount.
- [ ] Loading state: skeleton placeholder giữ chiều cao card, không jump layout.
- [ ] Error state: hiện thông báo lỗi + nút "Thử lại" khi API fail; Market Watch không crash.
- [ ] `s === "no_data"` cho phiên trước → chỉ vẽ line "Hôm nay" + hint "Không có dữ liệu phiên trước".
- [ ] Phiên trước rơi vào ngày nghỉ → resolve về phiên giao dịch hợp lệ gần nhất (không bị null).
- [ ] Ngoài giờ giao dịch: line "Hôm nay" đóng băng, không subscribe WS.
- [ ] Không regression các tính năng hiện có trên Market Watch (bảng giá, ranking, tab switcher).
- [ ] Không add thư viện chart mới (chỉ dùng `react-native-svg-charts` sẵn có).

---

## 📎 Related Docs

- Analyst findings: `_workspace/01_analyst_findings.md`
- PRD: `New feature in NHSV Pro/Market_Watch/GTGD_Chart/PRD.md`
- BE Issue (endpoint mới đề xuất): `New feature in NHSV Pro/Market_Watch/GTGD_Chart/BE_Issue.md`
- WS channel spec: `Knowledge/TradeX/System/market-data-channels.md` §3.4
- FE repo (READ-ONLY): `/Users/ducnguyen/Documents/project/nhsv-mts-rn`

---

**Document Status:** 📋 Draft | **For:** FE Developer | **Next Steps:** Review & implement
