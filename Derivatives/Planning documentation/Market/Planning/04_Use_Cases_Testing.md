# Use Cases & Test Scenarios
## Tích hợp Giao dịch Phái sinh vào TradeX

> **Document ID:** DER-UC-001  
> **Version:** 1.0  
> **Created:** 2025-01-30  
> **For:** QA Team, BA Team

---

## Mục lục

1. [Use Cases chi tiết](#1-use-cases-chi-tiết)
2. [Test Scenarios](#2-test-scenarios)
3. [Negative Test Cases](#3-negative-test-cases)
4. [Regression Test Cases](#4-regression-test-cases)

---

## 1. Use Cases chi tiết

### UC-001: Hệ thống cập nhật danh sách mã phái sinh đầu ngày

| Mục | Nội dung |
|-----|----------|
| **Use Case ID** | UC-001 |
| **Tên** | Cập nhật danh sách mã phái sinh đầu ngày |
| **Mô tả** | Hệ thống tự động lấy danh sách và thông tin mã phái sinh vào đầu ngày giao dịch |
| **Actor** | Hệ thống (Scheduler) |
| **Trigger** | Đến giờ chạy job (08:00-08:30) |
| **Precondition** | Ngày giao dịch (không phải T7, CN, ngày lễ) |
| **Postcondition** | Danh sách mã phái sinh sẵn sàng cho người dùng truy vấn |

**Main Flow:**

| Bước | Actor | Hành động | Kết quả mong đợi |
|------|-------|-----------|------------------|
| 1 | Hệ thống | Kiểm tra ngày giao dịch | Xác định là ngày giao dịch |
| 2 | Hệ thống | Lấy danh sách mã CƠ SỞ từ Lotte | Nhận được ~2000 mã |
| 3 | Hệ thống | Lấy danh sách mã PHÁI SINH từ Lotte | Nhận được 4-8 mã VN30F |
| 4 | Hệ thống | Với mỗi mã phái sinh, lấy thông tin chi tiết | Có đầy đủ giá, OI, ngày đáo hạn |
| 5 | Hệ thống | Gộp danh sách cơ sở + phái sinh | Danh sách tổng hợp |
| 6 | Hệ thống | Đánh dấu mã phái sinh: **`m`** = "INDEX" hoặc "BOND" theo 41I/41B (cùng field `m` như HOSE/HNX/UPCOM) | Phân biệt rõ ràng; FE dùng `m` để hiển thị tên chỉ mục (PS/DR, TPCP/GB) |
| 7 | Hệ thống | Lưu vào Redis, MongoDB, S3 | Dữ liệu sẵn sàng |

**Alternative Flow - Lỗi lấy phái sinh:**

| Bước | Actor | Hành động | Kết quả mong đợi |
|------|-------|-----------|------------------|
| 3a | Hệ thống | Lấy phái sinh thất bại (timeout, error) | Exception bị catch |
| 3b | Hệ thống | Ghi log cảnh báo | Log warning |
| 3c | Hệ thống | Tiếp tục với danh sách cơ sở | Cơ sở vẫn hoạt động |
| 3d | Hệ thống | Thông báo cho Operations team | Alert notification |

**Alternative Flow - Ngày nghỉ:**

| Bước | Actor | Hành động | Kết quả mong đợi |
|------|-------|-----------|------------------|
| 1a | Hệ thống | Phát hiện ngày nghỉ/lễ | Không chạy job |
| 1b | Hệ thống | Ghi log thông tin | Log info: "TODAY IS HOLIDAY" |

---

### UC-002: Người dùng xem danh sách mã phái sinh

| Mục | Nội dung |
|-----|----------|
| **Use Case ID** | UC-002 |
| **Tên** | Xem danh sách mã phái sinh |
| **Mô tả** | Người dùng xem tất cả mã hợp đồng tương lai đang giao dịch |
| **Actor** | Nhà đầu tư (User) |
| **Trigger** | User mở màn hình thị trường hoặc tìm kiếm |
| **Precondition** | App đã được cài đặt và đăng nhập |
| **Postcondition** | User thấy danh sách mã phái sinh với giá hiện tại |

**Main Flow:**

| Bước | Actor | Hành động | Kết quả mong đợi |
|------|-------|-----------|------------------|
| 1 | User | Mở app NHSV Pro | App hiển thị |
| 2 | User | Vào mục Thị trường (Market) | Màn hình danh sách mã |
| 3 | User | Chọn tab "Phái sinh" hoặc filter | Danh sách lọc theo phái sinh |
| 4 | App | Gọi API lấy danh sách mã phái sinh | API request |
| 5 | Hệ thống | Trả về danh sách với **`m`** = "INDEX" hoặc "BOND" cho mỗi mã phái sinh | Response data |
| 6 | App | Hiển thị danh sách mã phái sinh | VN30F2501, VN30F2502, etc. |
| 7 | User | Xem thông tin tổng quan | Mã, Giá, Thay đổi, % |

**Thông tin hiển thị cho mỗi mã:**

| Trường | Ví dụ | Ghi chú |
|--------|-------|---------|
| Mã | VN30F2501 | Mã hợp đồng |
| Giá hiện tại | 1285.5 | Giá khớp gần nhất |
| Thay đổi | +12.5 | So với tham chiếu |
| % Thay đổi | +0.98% | Tỷ lệ phần trăm |
| Khối lượng | 125,000 | Số hợp đồng |
| Trạng thái | ▲ (Tăng) | Màu xanh |

---

### UC-003: Người dùng xem chi tiết mã phái sinh

| Mục | Nội dung |
|-----|----------|
| **Use Case ID** | UC-003 |
| **Tên** | Xem chi tiết mã phái sinh |
| **Mô tả** | Người dùng xem đầy đủ thông tin của một mã phái sinh cụ thể |
| **Actor** | Nhà đầu tư (User) |
| **Trigger** | User chọn (tap) vào một mã phái sinh |
| **Precondition** | User đang ở màn hình danh sách mã |
| **Postcondition** | User thấy chi tiết đầy đủ của mã được chọn |

**Main Flow:**

| Bước | Actor | Hành động | Kết quả mong đợi |
|------|-------|-----------|------------------|
| 1 | User | Chọn mã VN30F2501 từ danh sách | Navigate đến màn chi tiết |
| 2 | App | Gọi API lấy chi tiết mã | API request |
| 3 | Hệ thống | Trả về thông tin đầy đủ (phái sinh có **`m`** = INDEX \| BOND) | Response data |
| 4 | App | Hiển thị thông tin giá | Giá, Thay đổi, %, etc. |
| 5 | App | Hiển thị sổ lệnh | 10 bước giá mua/bán |
| 6 | App | Hiển thị thông tin đặc thù phái sinh | OI, Ngày đáo hạn, Basis |

**Chi tiết màn hình:**

```
┌─────────────────────────────────────────────────────────┐
│  VN30F2501                                              │
│  VN30 Future Tháng 1/2025                               │
│  Đáo hạn: 30/01/2025 (còn 15 ngày)                     │
├─────────────────────────────────────────────────────────┤
│  THÔNG TIN GIÁ                                          │
├────────────────┬────────────────┬───────────────────────┤
│ Giá hiện tại   │ Trần           │ Mở cửa               │
│ 1285.5 ▲       │ 1350.0         │ 1275.0               │
├────────────────┼────────────────┼───────────────────────┤
│ Thay đổi       │ Sàn            │ Cao nhất             │
│ +12.5 (+0.98%) │ 1220.0         │ 1290.0               │
├────────────────┼────────────────┼───────────────────────┤
│ Tham chiếu     │ KL giao dịch   │ Thấp nhất            │
│ 1273.0         │ 125,000 HĐ     │ 1270.0               │
├────────────────┴────────────────┴───────────────────────┤
│  SỔ LỆNH                                                │
├───────────────────────┬─────────────────────────────────┤
│ BÊN MUA               │ BÊN BÁN                         │
├──────────┬────────────┼────────────┬────────────────────┤
│ KL       │ Giá        │ Giá        │ KL                 │
├──────────┼────────────┼────────────┼────────────────────┤
│ 1,200    │ 1285.0     │ 1285.5     │ 1,000              │
│ 800      │ 1284.5     │ 1286.0     │ 1,100              │
│ 1,500    │ 1284.0     │ 1286.5     │ 900                │
│ ...      │ ...        │ ...        │ ...                │
├──────────┴────────────┴────────────┴────────────────────┤
│ Tổng mua: 5,000       │ Tổng bán: 4,500                │
├─────────────────────────────────────────────────────────┤
│  THÔNG TIN KHÁC                                         │
├────────────────┬────────────────┬───────────────────────┤
│ Open Interest  │ ĐTNN Mua       │ Mã cơ sở            │
│ 45,000 HĐ      │ 5,000 HĐ       │ VN30                │
├────────────────┼────────────────┼───────────────────────┤
│ Basis          │ ĐTNN Bán       │ Giá lý thuyết       │
│ -5.0           │ 3,000 HĐ       │ 1280.0              │
└────────────────┴────────────────┴───────────────────────┘
```

---

### UC-004: Người dùng theo dõi giá real-time

| Mục | Nội dung |
|-----|----------|
| **Use Case ID** | UC-004 |
| **Tên** | Theo dõi giá phái sinh real-time |
| **Mô tả** | Giá được cập nhật tự động khi có giao dịch mới |
| **Actor** | Nhà đầu tư (User), Hệ thống |
| **Trigger** | Có giao dịch mới trên sàn |
| **Precondition** | User đang ở màn hình chi tiết hoặc danh sách mã phái sinh |
| **Postcondition** | User thấy giá mới nhất |

**Main Flow:**

| Bước | Actor | Hành động | Kết quả mong đợi |
|------|-------|-----------|------------------|
| 1 | User | Mở màn hình chi tiết mã VN30F2501 | Màn hình hiển thị |
| 2 | App | Subscribe WebSocket channel `market.quote.dr.VN30F2501` | Connection established |
| 3 | Sàn | Có giao dịch mới khớp | - |
| 4 | Lotte | Gửi data qua WebSocket | Lotte → TradeX |
| 5 | TradeX | Xử lý và publish lên channel | TradeX → App |
| 6 | App | Nhận data từ WebSocket | Data received |
| 7 | App | Cập nhật UI: Giá, Thay đổi, %, KL | UI updated |
| 8 | App | Highlight giá thay đổi (animation) | Visual feedback |

**Real-time Data Flow:**

```
Sàn (HNX)
    │
    ▼
Lotte Securities WebSocket
    │ channel: pro.pub.auto.dr.qt./VN30F2501
    ▼
TradeX (market-collector-lotte)
    │
    ▼
Kafka (quoteUpdateDR)
    │
    ▼
TradeX (realtime-v2)
    │
    ▼
Redis Cache
    │
    ▼
TradeX (ws-v2)
    │ channel: market.quote.dr.VN30F2501
    ▼
NHSV Pro App
    │
    ▼
User sees updated price
```

**Dữ liệu cập nhật real-time:**

| Trường | Cập nhật real-time | Ghi chú |
|--------|-------------------|---------|
| Giá hiện tại | ✅ Có | Mỗi tick |
| Thay đổi | ✅ Có | Mỗi tick |
| % Thay đổi | ✅ Có | Mỗi tick |
| Khối lượng khớp | ✅ Có | Mỗi tick |
| Tổng KL | ✅ Có | Mỗi tick |
| Sổ lệnh | ✅ Có | Mỗi thay đổi |
| ĐTNN | ✅ Có | Mỗi tick |
| Trần/Sàn/TC | ❌ Không | Cố định trong ngày |
| Ngày đáo hạn | ❌ Không | Cố định |

---

### UC-005: Người dùng filter cơ sở/phái sinh

| Mục | Nội dung |
|-----|----------|
| **Use Case ID** | UC-005 |
| **Tên** | Filter danh sách theo loại thị trường |
| **Mô tả** | Người dùng lọc để chỉ xem cơ sở hoặc chỉ xem phái sinh |
| **Actor** | Nhà đầu tư (User) |
| **Trigger** | User chọn filter option |
| **Precondition** | User đang ở màn hình danh sách mã |
| **Postcondition** | Danh sách chỉ hiển thị loại được chọn |

**Main Flow:**

| Bước | Actor | Hành động | Kết quả mong đợi |
|------|-------|-----------|------------------|
| 1 | User | Mở filter/tab options | Hiển thị: Tất cả, Cơ sở, Phái sinh |
| 2 | User | Chọn "Phái sinh" | Filter applied |
| 3 | App | Gọi API với `marketFilter: "derivatives"` | API request |
| 4 | Hệ thống | Trả về chỉ mã có `m` in ["INDEX","BOND"] (hoặc filter theo t=FUTURES) | Filtered response |
| 5 | App | Hiển thị chỉ mã phái sinh | VN30F2501, VN30F2502, etc. |

**Filter Options:**

| Option | Giá trị | Kết quả |
|--------|---------|---------|
| Tất cả | `marketFilter: "all"` | Cả cơ sở và phái sinh |
| Cơ sở | `marketFilter: "equity"` | Chỉ STOCK, ETF, CW |
| Phái sinh | `marketFilter: "derivatives"` | Chỉ FUTURES |

---

## 2. Test Scenarios

### TS-001: Init Job - Happy Path

| Mục | Nội dung |
|-----|----------|
| **Test ID** | TS-001 |
| **Tên** | Init job thành công với cả cơ sở và phái sinh |
| **Mô tả** | Verify init job lấy được đầy đủ mã cơ sở và phái sinh |
| **Precondition** | Ngày giao dịch, Lotte API hoạt động bình thường |

**Test Steps:**

| Bước | Hành động | Dữ liệu | Kết quả mong đợi |
|------|-----------|---------|------------------|
| 1 | Trigger init job | Manual trigger hoặc đợi scheduled | Job started |
| 2 | Kiểm tra log | - | Log: "Starting equity download..." |
| 3 | Kiểm tra log | - | Log: "Starting derivative download..." |
| 4 | Kiểm tra log | - | Log: "Found X derivative symbols" |
| 5 | Kiểm tra log | - | Log: "Successfully downloaded Y symbols" |
| 6 | Verify Redis | `HGETALL realtime_mapSymbolInfo` | Có cả mã cơ sở và phái sinh |
| 7 | Verify mã phái sinh | `HGET realtime_mapSymbolInfo VN30F2501` | Có `m: "INDEX"` (hoặc "BOND" cho mã 41B) |
| 8 | Verify file S3 | Download symbol_static.json | Có mã phái sinh với format chuẩn |

**Expected format trong symbol_static.json:** Mã phái sinh có **`m`** = "INDEX" hoặc "BOND", và các trường re, ce, fl, bc, ed, rd như ví dụ trong 01_Integration_Plan (5.4).

**Expected Result:**
- ✅ ~2000 mã cơ sở
- ✅ 4-8 mã phái sinh
- ✅ Tất cả mã phái sinh có **`m`** = "INDEX" hoặc "BOND"
- ✅ Tất cả mã phái sinh có đầy đủ: OI, ngày đáo hạn, basis

---

### TS-002: Init Job - Phái sinh lỗi, cơ sở vẫn hoạt động

| Mục | Nội dung |
|-----|----------|
| **Test ID** | TS-002 |
| **Tên** | Graceful degradation khi phái sinh lỗi |
| **Mô tả** | Verify hệ thống vẫn hoạt động khi Lotte DR API lỗi |
| **Precondition** | Lotte DR API bị mock để trả về lỗi |

**Test Steps:**

| Bước | Hành động | Dữ liệu | Kết quả mong đợi |
|------|-----------|---------|------------------|
| 1 | Mock Lotte DR API | Return 500 hoặc timeout | - |
| 2 | Trigger init job | - | Job started |
| 3 | Kiểm tra log | - | Log WARN: "Derivative download failed" |
| 4 | Kiểm tra log | - | Log: "Continuing with equity only" |
| 5 | Verify Redis | `HGETALL realtime_mapSymbolInfo` | Có đầy đủ mã cơ sở |
| 6 | Verify Redis | - | KHÔNG có mã phái sinh |
| 7 | Verify job status | - | Job completed successfully |

**Expected Result:**
- ✅ Mã cơ sở được lưu đầy đủ
- ✅ Job không fail
- ✅ Có log cảnh báo
- ✅ Alert được gửi (nếu có config)

---

### TS-003: API - Lấy danh sách mã phái sinh

| Mục | Nội dung |
|-----|----------|
| **Test ID** | TS-003 |
| **Tên** | API trả về danh sách mã phái sinh |
| **Mô tả** | Verify API /symbol/latest trả về mã phái sinh với filter |
| **Precondition** | Init job đã chạy thành công |

**Test Steps:**

| Bước | Hành động | Request | Kết quả mong đợi |
|------|-----------|---------|------------------|
| 1 | Gọi API lấy tất cả | `symbolList: ["VCB", "VN30F2501"]` | Trả về cả 2 |
| 2 | Verify response VCB | - | `m: "HOSE"`, không có `dr` object |
| 3 | Verify response VN30F2501 | - | `m: "INDEX"`, có `dr` object |
| 4 | Verify dr object | - | Có: bc, ed, rd, tp, bs, oi |
| 5 | Gọi API filter derivatives | `marketFilter: "derivatives"` | Chỉ trả về mã phái sinh |
| 6 | Gọi API filter equity | `marketFilter: "equity"` | Chỉ trả về mã cơ sở |

**Sample Response - Mã phái sinh:** Có **`m`** = "INDEX" hoặc "BOND", và object `dr` chứa bc, ed, rd, tp, bs, oi. Chi tiết format xem 01_Integration_Plan.

---

### TS-004: WebSocket - Real-time giá phái sinh

| Mục | Nội dung |
|-----|----------|
| **Test ID** | TS-004 |
| **Tên** | Nhận giá phái sinh real-time qua WebSocket |
| **Mô tả** | Verify client nhận được giá cập nhật qua WS |
| **Precondition** | Đang trong phiên giao dịch |

**Test Steps:**

| Bước | Hành động | Dữ liệu | Kết quả mong đợi |
|------|-----------|---------|------------------|
| 1 | Connect WebSocket | - | Connection established |
| 2 | Subscribe channel | `market.quote.dr.VN30F2501` | Subscribed |
| 3 | Chờ update | - | Nhận message trong vòng 1 giây |
| 4 | Verify message format | - | Có: s, m, c, ch, ra, vo, oi |
| 5 | Verify m field | - | `m: "INDEX"` hoặc `"BOND"` (phái sinh) |
| 6 | Verify latency | Measure timestamp | < 1 giây từ Lotte |

**Sample WebSocket Message:**

```json
{
  "channel": "market.quote.dr.VN30F2501",
  "data": {
    "s": "VN30F2501",
    "m": "INDEX",
    "c": 1286.0,
    "ch": 13.0,
    "ra": 1.02,
    "vo": 125500,
    "mv": 500,
    "mb": "BID",
    "oi": 45100
  }
}
```

---

### TS-005: WebSocket - Sổ lệnh phái sinh

| Mục | Nội dung |
|-----|----------|
| **Test ID** | TS-005 |
| **Tên** | Nhận sổ lệnh phái sinh real-time |
| **Mô tả** | Verify client nhận được sổ lệnh 10 bước giá |
| **Precondition** | Đang trong phiên giao dịch |

**Test Steps:**

| Bước | Hành động | Dữ liệu | Kết quả mong đợi |
|------|-----------|---------|------------------|
| 1 | Subscribe channel | `market.bidoffer.dr.VN30F2501` | Subscribed |
| 2 | Chờ update | - | Nhận message |
| 3 | Verify bid/offer list | - | 10 bước giá mua, 10 bước giá bán |
| 4 | Verify mỗi bước | - | Có: price, volume |
| 5 | Verify tổng hợp | - | totalBidVolume, totalOfferVolume |
| 6 | Verify phiên ATO/ATC | Trong phiên ATO/ATC | Có expectedPrice, expectedVolume |

---

### TS-006: Thông tin đặc thù phái sinh

| Mục | Nội dung |
|-----|----------|
| **Test ID** | TS-006 |
| **Tên** | Verify thông tin đặc thù phái sinh |
| **Mô tả** | Kiểm tra các trường chỉ có ở phái sinh |
| **Precondition** | Mã phái sinh đã được init |

**Test Steps:**

| Bước | Kiểm tra | Điều kiện | Kết quả mong đợi |
|------|----------|-----------|------------------|
| 1 | Mã cơ sở (baseCode) | VN30F2501 | "VN30" |
| 2 | Ngày đáo hạn (lastTradingDate) | VN30F2501 | "20250130" (format yyyyMMdd) |
| 3 | Số ngày còn lại (remainingDays) | VN30F2501 | Số nguyên dương |
| 4 | Open Interest | - | Số nguyên >= 0 |
| 5 | Giá lý thuyết (theoryPrice) | - | Số dương |
| 6 | Basis | - | Có thể âm hoặc dương |
| 7 | So sánh với mã cơ sở | VCB | Không có các trường trên |

---

## 3. Negative Test Cases

### NTC-001: Lotte DR API timeout

| Mục | Nội dung |
|-----|----------|
| **Test ID** | NTC-001 |
| **Tên** | Handle Lotte DR API timeout |
| **Expected** | Hệ thống retry và graceful degradation |

**Steps:**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mock Lotte DR API delay 60s | - |
| 2 | Trigger init job | Job started |
| 3 | Verify retry | Log: "Retrying..." |
| 4 | After max retries | Log: "Derivative download failed" |
| 5 | Verify cơ sở | Mã cơ sở vẫn được lưu |

---

### NTC-002: Dữ liệu phái sinh không hợp lệ

| Mục | Nội dung |
|-----|----------|
| **Test ID** | NTC-002 |
| **Tên** | Handle invalid derivative data |
| **Expected** | Skip mã lỗi, tiếp tục với mã khác |

**Steps:**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mock 1 mã có giá = null | - |
| 2 | Trigger init job | Job started |
| 3 | Verify log | Log WARN cho mã lỗi |
| 4 | Verify các mã khác | Các mã hợp lệ vẫn được lưu |

---

### NTC-003: WebSocket disconnect

| Mục | Nội dung |
|-----|----------|
| **Test ID** | NTC-003 |
| **Tên** | Handle WebSocket disconnect |
| **Expected** | Auto reconnect, equity không ảnh hưởng |

**Steps:**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Disconnect Lotte DR WebSocket | - |
| 2 | Verify equity WS | Vẫn hoạt động bình thường |
| 3 | Verify auto reconnect | Log: "Reconnecting DR WebSocket..." |
| 4 | Sau reconnect | DR data resume |

---

### NTC-004: Mã phái sinh không tồn tại

| Mục | Nội dung |
|-----|----------|
| **Test ID** | NTC-004 |
| **Tên** | Query mã phái sinh không tồn tại |
| **Expected** | Trả về empty hoặc null |

**Steps:**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Gọi API | `symbolList: ["VN30F9999"]` |
| 2 | Verify response | Empty array hoặc null cho mã đó |
| 3 | Verify status code | 200 OK (không phải 404) |

---

## 4. Regression Test Cases

### RTC-001: Chức năng cơ sở không thay đổi

| Mục | Nội dung |
|-----|----------|
| **Test ID** | RTC-001 |
| **Tên** | Verify chức năng cơ sở không bị ảnh hưởng |

**Test Cases:**

| # | Test Case | Kết quả mong đợi |
|---|-----------|------------------|
| 1 | Init job cho mã cơ sở | Vẫn lấy được ~2000 mã |
| 2 | API /symbol/latest cho mã cơ sở | Response format không đổi |
| 3 | WebSocket market.quote.VCB | Vẫn nhận được data |
| 4 | Filter chỉ cơ sở | Không trả về mã phái sinh |
| 5 | Performance | Latency không tăng quá 5% |

---

### RTC-002: Backward Compatibility

| Mục | Nội dung |
|-----|----------|
| **Test ID** | RTC-002 |
| **Tên** | API backward compatible với client cũ |

**Test Cases:**

| # | Test Case | Kết quả mong đợi |
|---|-----------|------------------|
| 1 | Client không gửi marketFilter | Trả về cả cơ sở và phái sinh |
| 2 | Client cũ parse response | Không bị crash (dr object là optional) |
| 3 | Client subscribe channel cũ | market.quote.{code} vẫn hoạt động cho equity |

---

## Checklist Tổng hợp

### Trước khi Go-live

**Functional:**
- [ ] Init job lấy được mã phái sinh
- [ ] Mã phái sinh có **`m`** = "INDEX" hoặc "BOND"
- [ ] API trả về đầy đủ thông tin
- [ ] WebSocket real-time hoạt động
- [ ] Sổ lệnh 10 bước giá
- [ ] Filter cơ sở/phái sinh hoạt động

**Non-Functional:**
- [ ] Latency < 1 giây
- [ ] Graceful degradation khi DR lỗi
- [ ] Equity không bị ảnh hưởng

**Regression:**
- [ ] Tất cả chức năng cơ sở vẫn hoạt động
- [ ] Performance không giảm đáng kể

---

*Document End - Use Cases & Test Scenarios v1.0*
