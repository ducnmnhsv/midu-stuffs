# [FE] Event Calendar — Lịch sự kiện cổ tức (A-02)

## Tóm tắt

EventScreen và EventDetailScreen đã được scaffold sẵn (navigation đã đăng ký, route params đã khai báo) nhưng toàn bộ là hardcoded/placeholder. Issue này yêu cầu implement đầy đủ hai màn hình, tích hợp API thực từ BE, thêm section Event Calendar vào Home, và xử lý deep link từ push notification.

---

## Hiện trạng trong codebase

| File | Trạng thái hiện tại |
|---|---|
| `src/screens/EventScreen/index.tsx` | Hardcoded DATA array với 3 ngày giả, không có API call, không có filter sàn |
| `src/screens/EventScreen/components/ListEventOfDay/index.tsx` | Hardcoded 4 EventItem cùng mã ACB, có "Xem thêm" button nhưng không navigate |
| `src/screens/EventDetailScreen/index.tsx` | Chỉ hiển thị `title` và `content` dạng text thô, không có UI thực |
| `src/navigation/ScreenParamList.ts` | `EventDetailScreen` nhận `{ title, content }` — cần đổi sang `{ eventId }` |
| `src/navigation/Linking.tsx` | Chưa có config deeplink cho EventScreen hoặc EventDetailScreen |
| Redux | Chưa có slice/saga nào cho Event Calendar |

---

## Công việc cần thực hiện

### Task 1 — Redux + API integration

Tạo Redux slice và saga cho Event Calendar. Gọi hai endpoint từ BE:

- `GET /api/v1/eventCalendar/upcoming?exchange={exchange}` — dùng cho danh sách (EventScreen)
- `GET /api/v1/eventCalendar/{eventId}` — dùng cho chi tiết (EventDetailScreen)

State cần quản lý: danh sách events, trạng thái loading/error, `asOfDate` từ response (dùng để tính badge "HÔM NAY" và date header), và exchange filter đang chọn.

Khi filter sàn thay đổi, re-fetch hoặc re-filter client-side từ data đã cache — Mobile team tự quyết approach tối ưu hơn.

### Task 2 — EventScreen: Màn hình danh sách

Thay thế toàn bộ hardcoded data. Màn hình này có thể được mở standalone hoặc embed từ Home section (xem Task 3).

**Filter bar sàn:** 4 pills — Tất cả (mặc định, nền navy), HOSE, HNX, Upcom. Khi chọn sàn: active pill đổi màu navy, các pill còn lại về trạng thái mặc định.

**Danh sách events nhóm theo ngày:** Dữ liệu trả từ API đã sort theo `gdkhqDate ASC`. FE group theo ngày và render header cho mỗi nhóm. Logic header:
- `gdkhqDate === asOfDate` → header teal: "HÔM NAY · DD/MM"
- `gdkhqDate === asOfDate + 1` → "Ngày mai · DD/MM"
- Còn lại → xám: "Tx · DD/MM · N ngày nữa" (tính diff ngày từ asOfDate)

**Event card:** ô mã CK (nền navy), `eventTypeLabel` tiếng Việt, `gdkhqDate` format DD/MM/YYYY, `rateDisplay` màu xanh lá. Nếu `isToday = true`: hiện badge đỏ "HÔM NAY". Tap card → navigate đến EventDetailScreen với `{ eventId }`.

**States:** Loading skeleton (3–4 card), empty state "Không có sự kiện cổ tức trong 14 ngày tới.", error state với nút thử lại.

**Cập nhật ScreenParamList:** `EventDetailScreen` đổi params từ `{ title, content }` sang `{ eventId: string }`.

### Task 3 — Event Calendar section trên HomeTab

Thêm section "📅 Lịch sự kiện" vào một trong các tab của HomeTab, đặt ở vị trí sau phần "Hoạt động tích cực" (confirm vị trí chính xác với PM). Section này tái sử dụng data và component từ Task 2 nhưng hiển thị dạng inline, không phải màn hình riêng.

Khi section bị lỗi API, chỉ section đó hiển thị error — không ảnh hưởng các section khác trên Home.

### Task 4 — EventDetailScreen: Màn hình chi tiết

Thay thế hoàn toàn UI placeholder. Màn hình nhận `eventId` từ params, gọi `GET /api/v1/eventCalendar/{eventId}` khi mount.

Layout gồm 3 phần: Hero card (ô mã CK 36×36, tên công ty, exchange badge), Dates card (ngày GDKHQ kèm badge "HÔM NAY" nếu `isToday=true`, ngày ĐKCC nếu không null), Info card (loại cổ tức, tỷ lệ, sàn giao dịch).

Variant theo `eventType`:
- `CASH_DIVIDEND`: pill teal, label "Cổ tức tiền mặt"
- `STOCK_DIVIDEND`: pill violet, label "Cổ phiếu thưởng" hoặc "Cổ tức bằng cổ phiếu" (theo `eventTypeLabel`)

Cuối màn hình: disclaimer text nhỏ "Dữ liệu từ Vietstock. Vui lòng xác nhận tại nguồn chính thức." và CTA button "Xem cổ phiếu {stockCode} →" navigate đến màn hình giao dịch của mã đó.

Xử lý 404: khi API trả `OBJECT_NOT_FOUND` (sự kiện đã qua ngày và bị cleanup), hiển thị thông báo "Sự kiện này đã kết thúc." và nút quay lại.

### Task 5 — Deep link handler (push notification)

Cấu hình Linking.tsx để nhận deep link `nhsvpro://event-calendar/{eventId}` — mở thẳng EventDetailScreen với `eventId` tương ứng. Handler phải hoạt động ở cả 3 trạng thái: foreground, background, và cold start.

### Task 6 — Filter state persistence

Khi user đang ở EventScreen với filter sàn là HOSE → tap vào event card → vào EventDetailScreen → back về EventScreen: filter phải vẫn là HOSE, không reset về "Tất cả". Lưu filter state trong Redux slice (không dùng local state để mất khi unmount).

---

## API Response Types (tham chiếu)

```
// GET /eventCalendar/upcoming
{
  asOfDate: string        // "YYYY-MM-DD"
  events: Event[]
  totalCount: number
}

// Event object
{
  eventId: string
  stockCode: string
  companyName: string | null
  exchange: "HOSE" | "HNX" | "UPCOM"
  eventType: "CASH_DIVIDEND" | "STOCK_DIVIDEND"
  eventTypeLabel: string
  gdkhqDate: string       // "YYYY-MM-DD"
  rateDisplay: string     // "10%" hoặc "1,500 VNĐ/CP"
  isToday: boolean
}

// GET /eventCalendar/{eventId}
{
  ...Event,
  ndkccDate: string | null  // "YYYY-MM-DD"
}
```

---

## Acceptance Criteria

- [ ] EventScreen hiển thị dữ liệu thực từ API, không còn hardcoded
- [ ] Filter sàn hoạt động đúng (Tất cả / HOSE / HNX / Upcom)
- [ ] Event card hôm nay hiển thị badge đỏ "HÔM NAY"
- [ ] Date header hiển thị đúng format và màu (teal cho hôm nay, xám cho ngày khác)
- [ ] Loading skeleton, empty state, error state hoạt động đúng
- [ ] Tap card → navigate EventDetailScreen với đúng eventId
- [ ] EventDetailScreen hiển thị đầy đủ thông tin, đúng variant CASH vs STOCK
- [ ] ndkccDate null → ẩn dòng Ngày ĐKCC
- [ ] CTA "Xem cổ phiếu →" navigate đúng màn hình giao dịch
- [ ] Deep link `nhsvpro://event-calendar/{eventId}` mở đúng detail ở cả 3 trạng thái app
- [ ] Back từ detail về list giữ nguyên filter state đang chọn
- [ ] Lỗi API trên Home section không crash các section khác

---

## Notes

Section trên HomeTab và màn hình EventScreen standalone đều cần dùng chung data source — nên dispatch action từ cùng 1 Redux slice, tránh double fetch.

Deep link cần BE đính kèm `eventId` (dạng string) vào OneSignal notification data payload — confirm với BE trước khi implement handler.

---

Document Status: 📋 Draft | For: Mobile FE Developer | Next Steps: Confirm vị trí section trên HomeTab với PM, sync với BE về eventId format trong OneSignal payload
