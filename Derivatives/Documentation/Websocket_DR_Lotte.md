# Websocket DR

## 7.2.2.5 RMK-011 Future quote

### - Register data

`# Đăng ký nhận dữ liệu stock quote sub/pro.pub.auto.dr.qt./VN30F2404`

Danh sách các mã CK muốn nhận stockquote data, cách nhau bởi dấu `|`

### - Receive data struct

`#Output`: Chuỗi ký tự, các giá trị cách nhau bởi dấu `|`. Vị trí bắt đầu từ 0

| Order | FieldName          | Format | Type   | Description                              |
|-------|--------------------|--------|--------|------------------------------------------|
| [0]   | service            | String |        | Tên service                              |
| [1]   | success            | String |        |                                          |
| [2]   | time               | String |        | Thời gian                                |
| [3]   | code               | String |        | Mã CK                                    |
| [4]   | highTime           | String |        | Thời gian giá cao nhất                   |
| [5]   | lowTime            | String |        | Thời gian giá thấp nhất                  |
| [6]   | open.value         | String |        | Giá mở cửa                               |
| [7]   | open.type          | String |        | Trạng thái                               |
| [8]   | high.value         | String |        | Giá cao nhất                             |
| [9]   | high.type          | String |        | Trạng thái                               |
| [10]  | low.value          | String |        | Giá thấp nhất                            |
| [11]  | low.type           | String |        | Trạng thái                               |
| [12]  | last.value         | String |        | Giá hiện tại                             |
| [13]  | last.type          | String |        | Trạng thái                               |
| [14]  | change.value       | String |        | Thay đổi                                 |
| [15]  | changeRate         | String |        | Tỉ lệ KL giao dịch trong ngày            |
| [16]  | averagePrice       | String |        | Giá trung bình                           |
| [17]  | referencePrice     | String |        | Giá tham chiếu                           |
| [18]  | value              | String |        | Giá trị                                  |
| [19]  | volume             | String |        | Tổng khối lượng                          |
| [20]  | matchedVolume.value| String |        | Khối lượng khớp                          |
| [21]  | matchedVolume.type | String |        | Trạng thái: Mô tả khớp mua hoặc bán<br/>+ S: Sell<br/>+ B: Buy |
| [22]  | bid.value          | String |        | Giá Mua                                  |
| [23]  | bid.type           | String |        | Trạng thái giá                           |
| [24]  | offer.value        | String |        | Giá Bán                                  |
| [25]  | offer.type         | String |        | Trạng thái giá                           |
| [26]  | bid_size           | String |        | KL mua                                   |
| [27]  | offer_size         | String |        | KL bán                                   |
| [28]  | total_bid_size     | String |        | Tổng KL mua                              |
| [29]  | total_offer_size   | String |        | Tổng KL bán                              |
| [30]  | total_bid_count    | String |        | Tổng số lệnh mua                         |
| [31]  | total_offer_count  | String |        | Tổng số lệnh bán                         |
| [32]  | foreignerBuySize   | String |        | Nước ngoài mua                           |
| [33]  | foreignerSellSize  | String |        | Nước ngoài bán                           |

## 7.2.2.6 RMK-012 Future Bid/Offer

### - Register data

`# Đăng ký nhận dữ liệu stock quote sub/pro.pub.auto.dr.bo./VN30F2404`

Danh sách các mã CK muốn nhận stockquote data, cách nhau bởi dấu `|`

### - Receive data struct

`#Output`: Chuỗi ký tự, các giá trị cách nhau bởi dấu `|`. Vị trí bắt đầu từ 0

| Order | FieldName          | Format | Type   | Description                              |
|-------|--------------------|--------|--------|------------------------------------------|
| [0]   | service            | String |        | Tên service                              |
| [1]   | success            | String |        |                                          |
| [2]   | time               | String |        | Thời gian                                |
| [3]   | code               | String |        | Mã CK                                    |
| [4]   | control_code       | String |        | Trạng thái TT                            |
| [5]   | project_open.value | String |        | Giá khớp dự kiến các phiên ATO/ATC       |
| [6]   | project_open.type  | String |        | Trạng thái giá                           |
| [7]   | bid.value          | String |        | Giá mua                                  |
| [8]   | bid.type           | String |        | Trạng thái giá                           |
| [9]   | bidSize            | String |        | KL mua                                   |
| [10]  | offer.value        | String |        | Giá bán                                  |
| [11]  | offer.type         | String |        | Trạng thái giá                           |
| [12]  | offerSize          | String |        | KL bán                                   |

**Thông tin 10 bước giá. Từ field [13] đến [72]. Mỗi bước giá tương ứng 6 field như bên dưới**

| Order | FieldName     | Format | Type   | Description      |
|-------|---------------|--------|--------|------------------|
| [13]  | bid1.value    | String |        | Giá mua          |
| [14]  | bid1.type     | String |        | Trạng thái giá   |
| [15]  | bid1Size      | String |        | KL mua           |
| [16]  | offer1.value  | String |        | Giá bán          |
| [17]  | offer1.type   | String |        | Trạng thái giá   |
| [18]  | offer1Size    | String |        | KL bán           |

*Lưu ý: Phải lập lại 10 lần cấu trúc trên để lấy đủ 10 bước giá. Các field tiếp theo bắt đầu từ [73]*

| Order | FieldName           | Format | Type   | Description                              |
|-------|---------------------|--------|--------|------------------------------------------|
| [73]  | totalBidSize        | String |        | Tổng KL Mua 3 bước giá đầu               |
| [74]  | totalOfferSize      | String |        | Tổng KL Bán 3 bước giá đầu               |
| [75]  | bidOfferSizeDiff    | String |        | Chênh lệch KL mua-bán 3 bước giá đầu     |
| [76]  | totalBidSize5       | String |        | Tổng KL Mua 5 bước giá đầu               |
| [77]  | totalOfferSize5     | String |        | Tổng KL Bán 5 bước giá đầu               |
| [78]  | bidOfferSizeDiff5   | String |        | Chênh lệch KL mua-bán 5 bước giá đầu     |
| [79]  | totalBidSize10      | String |        | Tổng KL Mua 10 bước giá đầu              |


## 9.2 QUY ĐỊNH BẢNG MÃ

### Trạng thái lệnh - accp_tp

| Giá trị | Diễn giải                               |
|---------|-----------------------------------------|
| 0       | Tiếp nhận                               |
| 1       | Chuyển                                  |
| 2       | Xác nhận lệnh                           |
| 3       | Xác nhận tiếp nhận                      |
| 4       | Khớp toàn bộ                            |
| 5       | Khớp một phần                           |
| 6       | SO actived - Không sử dụng              |
| 7       | IO actived - Không sử dụng              |
| R       | Từ chối                                 |
| X       | Từ chối                                 |
| %       | Tất cả                                  |


### Phân loại lệnh - stk_ord_tp

| Giá trị | Diễn giải                |
|---------|--------------------------|
| 01      | LO - Giới hạn            |
| 02      | MP                       |
| 03      | ATO                      |
| 04      | ATC                      |
| 05      | AON – Không sử dụng      |
| 06      | Thỏa thuận lô lớn        |
| 07      | MOK                      |
| 08      | MAK                      |
| 09      | MTL                      |
| 13      | SBO                      |
| 14      | OBO                      |
| 15      | PLO                      |
| 18      | BuyIn                    |
| %       | Tất cả                   |


### Ngôn ngữ - lang_code

| Giá trị | Diễn giải   |
|---------|-------------|
| V       | Tiếng Việt  |
| E       | Tiếng Anh   |
| K       | Tiếng Hàn   |

### Kênh thực hiện - mdm_tp

| Giá trị | Diễn giải             |
|---------|-----------------------|
| 00      | Quầy (BOS)            |
| 01      | Phone                 |
| 03      | WTS                   |
| 04      | HTS                   |
| 06      | MTS - iOS             |
| 07      | MTS-iPad              |
| 08      | MTS - Android         |
| 20      | OMS Order             |
| 30      | API                   |
| 31      | New MTS IOS           |
| 32      | New MTS Android       |
| 33      | Copy Trading          |
| 41      | FinTech(Stag)         |
| 42      | FinTech(DifiSoft)     |
| %       | Tất cả                |

### Trạng thái thị trường - controlCode

**Sản Hose:**

| Giá trị | Diễn giải               |
|---------|-------------------------|
| P = 0   | ATO                     |
| O, R = 1| Phiên liên tục          |
| I = 2   | Ngưng nghỉ trưa         |
| A = 3   | ATC                     |
| C = 4   | PLO = GDTT              |
| K = 5   | Đóng cửa                |
| G       | Sau 15h                 |

**Sản HNX và UPCOM:**

| Giá trị | Diễn giải               |
|---------|-------------------------|
| P.O = 1 | Phiên liên tục          |
| 2 = 2   | Ngưng nghỉ trưa         |
| A = 3   | ATC                     |
| C = 4   | PLO = GDTT              |
| K = 5   | Đóng cửa                |

### Mã trạng thái giá - price status

| Giá trị | Diễn giải   |
|---------|-------------|
| 0, 3    | Tham chiếu  |
| 1       | Trần        |
| 2       | Tăng        |
| 4       | Sàn         |
| 5       | Giảm        |