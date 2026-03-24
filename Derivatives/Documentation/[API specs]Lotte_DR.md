# TÀI LIỆU ĐẶC TẢ KỸ THUẬT API GATEWAY 2.0

**Nguồn:** Tài liệu đặc tả API 2.0 Tsolution Detail – NHSV Derivatives (Lotte HPT)  
**Cập nhật từ file:** `18032026_Tai_lieu_dac_ta_API2.0_Tsolution-Detail-NHSV_Derivaties.pdf` (18/03/2026)

---

## LOTTE HPT — Đặc tả kỹ thuật T-API Gateway 2.0

**LỊCH SỬ THAY ĐỔI TÀI LIỆU**

| Ngày       | Phiên bản | Người thực hiện | Nội dung |
|------------|-----------|-----------------|----------|
| 2026-03-18 | Doc sync  | Cursor          | Đồng bộ từ PDF 18/03/2026: Thêm **Mục 4 – WebSocket REALTIME** (4.1 Cấu trúc dữ liệu REALTIME: Order events, Account events); đánh số lại Mục quy tắc chung thành **5** (5.1 Cấu trúc dữ liệu, 5.2 Bảng mã) |
| 2026-03-05 | Doc sync  | Cursor          | Đồng bộ từ PDF 04/03/2026: URL DRACC-009/019, tham số snd_acnt/rcv_acnt, is_acnt_no, is_cnte; DRACC-032 tsol; DRACC-031 net_assets; response scrt_err_msg |
| 2026-03-04 | Doc sync  | Cursor          | Đồng bộ từ bản spec 27/02/2026: thêm DRACC-035/036/037, DRORD-033; bổ sung URL DRORD-025/026, DRORD-028 |

## 1. MỤC ĐÍCH
- Tài liệu mô tả API Thị trường
- Các quy tắc chung

## 2. DANH SÁCH CÁC API PHÁI SINH

### 2.1 TÀI KHOẢN PHÁI SINH

#### 2.1.1 DRACC-003: Danh sách vị thế mở
- **URL**: `[Root URL APIKEY] /tuxsvc/der/account/dr-open-positions`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY] (apiKey sẽ được cung cấp)
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `acnt`: String, Y, Số TK
    - `next_data`: String, Y, 0 (Không được phép bỏ trống; data nhập có thể là ký tự số bất kỳ; tra cứu trang tiếp thì nhập đúng next_data trả trong output lần trước)
    - `hts_user_id`: String, Y, lhptgwapi (hts_user_id của TK tra cứu)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse (Xem Object Types)
- **Object Types (DataResponse)**:
    - `acnt`: String, Tài khoản
    - `acnt_name`: String, Tên tài khoản
    - `contract_code`: String, Mã hợp đồng
    - `type`: String, Loại Mua/ Bán (1. Mua, 2.Bán)
    - `volume`: String, Khối lượng
    - `previous_volume`: String, Khối lượng trước
    - `average_price`: String, Giá trung bình
    - `current_price`: String, Giá hiện tại
    - `unrealized_profit_loss`: String, Lãi lỗ chưa thực hiện
    - `available_qty_closed`: String, Số lượng có thể đóng
    - `next_data`: String, next data

#### 2.1.2 DRACC-018: Tra cứu số dư khả dụng
- **URL**: `[Root URL APIKEY]/tuxsvc/der/account/dr-available-balance`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `account`: String, Y, Số TK
    - `sub`: String, Y, Số Sub
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `security_code`: String, sec_Cd
    - `avail_deposit`: String, Số dư hiện tại
    - `blockamt_for_deposit`: String, Số tiền block
    - `waiting_amt_for_withdraw`: String, Số tiền chờ rút
    - `total_deposit`: String, Số dư khả dụng

#### 2.1.3 DRACC-022: Tra cứu tổng hợp lãi lỗ và phí
- **URL**: `[Root URL APIKEY] /tuxsvc/der/account/dr-pl-summary`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `account`: String, Y, Số TK
    - `date_fr`: String, Y, Từ ngày
    - `date_to`: String, Y, Tới ngày
    - `hts_user_id`: String, Y, lhptgwapi (User ID)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `realized_profit_loss`: String, Lãi/Lỗ đã thực hiện
    - `unrealized_profit_loss`: String, Lãi/Lỗ chưa thực hiện
    - `fee`: String, Phí
    - `tax`: String, Thuế
    - `net_profit_loss`: String, Net P/L
    - `account`: String, Tài khoản
    - `account_name`: String, Tên tài khoản
    - `hnx_fee`: String, Phí giao dịch HNX
    - `position_management_fee`: String, Phí quản lý vị thế

#### 2.1.4 DRACC-023: Lịch sử thanh toán
- **URL**: `[Root URL APIKEY] /tuxsvc/der/account/dr-payment-history`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `acnt`: String, Y, Số tài khoản
    - `next_data`: String, N, "0" (Next data)
    - `date_fr`: String, Y, Từ ngày
    - `date_to`: String, Y, Tới ngày
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `trade_date`: String, Ngày giao dịch
    - `settle_date`: String, Ngày thanh toán
    - `account`: String, Số tài khoản
    - `account_name`: String, Tên tài khoản
    - `account_no`: String, Số tiểu khoản
    - `loss_profit`: String, Lỗ/ Lãi
    - `fee`: String, Phí
    - `tax`: String, Thuế
    - `available_cash`: String, Số tiền mặt khả dụng
    - `amount_miss`: String, Số tiền thiếu
    - `vm_payment_status`: String, Trạng thái thanh toán lỗ/lãi
    - `fee_payment_status`: String, Trạng thái thanh toán phí
    - `tax_payment_status`: String, Trạng thái thanh toán thuế
    - `next_data`: String, next data
    - `bank_fee`: String, Phí chuyển khoản ngân hàng

#### 2.1.5 DRACC-031: Tra cứu thông tin tài sản và chứng khoán
- **URL**: `[Root URL APIKEY]/tuxsvc/der/account/dr-balance-securities-info`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `account_no`: String, Y, Số tài khoản
    - `inquiry_date`: String, Y, yyyymmdd (Ngày tra cứu)
    - `hts_user_id`: String, Y, lhptgwapi (hts_user_id của TK tra cứu của công ty được phân quyền)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `available_balance`: String, Số dư tiền mặt khả dụng
    - `current_balance`: String, Số dư tiền mặt hiện có
    - `quantity_stock`: String, Số lượng chứng khoán khả dụng
    - `value_stock`: String, Giá trị chứng khoán khả dụng
    - `current_quantity_stock`: String, Số lượng chứng khoán hiện có
    - `current_value_stock`: String, Giá trị chứng khoán hiện có
    - `margin_asset_fee`: String, Phí quản lý TSKQ
    - `shortfall_amount`: String, Số tiền thiếu
    - `position_fee`: String, Phí vị thế
    - `hnx_fee`: String, Phí HNX
    - `begin_margin_balance`: String, Số dư tiền ký quỹ đầu ngày
    - `margin_deposit_withdrawal`: String, Tiền ký quỹ nộp / rút
    - `current_margin_balance`: String, Số dư tiền ký quỹ hôm nay
    - `margin_withdrawal`: String, Tiền ký quỹ rút
    - `value_margin_securities`: String, Giá trị chứng khoán ký quỹ
    - `pending_value_withdrawal_margin_securities`: String, Giá trị chứng khoán ký quỹ chờ rút
    - `accepted_collateral_assets`: String, Tài sản đảm bảo được chấp nhận
    - `realized_interest`: String, Lãi thật thực
    - `fees`: String, Phí
    - `unrealized_interest`: String, Lãi chưa ghi nhận
    - `unmatched_order_request_index`: String, Gửi yêu cầu của lệnh chưa khớp (có thể lệ/chỉ số liên quan đến MR)
    - `margin_shortfall`: String, Thâm hụt ký quỹ
    - `withdrawable_collateral_assets`: String, Tài sản đảm bảo có thể rút
    - `value_withdrawable_collateral_assets`: String, Tài sản đảm bảo có thể rút
    - `withdrawable_margin_securities`: String, Chứng khoán ký quỹ có thể rút
    - `accepted_margin_securities_values`: String, Giá trị chứng khoán ký quỹ được chấp nhận
    - `tax`: String, Thuế
    - `field_margin_cash_deposit_withdrawal`: String, Tiền ký quỹ nộp rút (hoặc một field liên quan đến Cash)
    - `margin_cash_deposit_withdrawal`: String, Tiền ký quỹ nộp rút
    - `margin_deposit_withdrawal_vsd`: String, Tiền ký quỹ nộp rút tại VSD
    - `begin_margin_cash_balance_nhsv`: String, Số dư tiền ký quỹ đầu ngày tại NHSV
    - `withdrawable_margin_securities_value`: String, Giá trị chứng khoán ký quỹ có thể rút
    - `withdrawable_margin_cash`: String, Tiền ký quỹ có thể rút
    - `pending_margin_cash_withdrawal_balance_nhsv`: String, Số dư tiền ký quỹ chờ rút tại NHSV
    - `begin_margin_cash_balance`: String, Số dư tiền ký quỹ đầu ngày
    - `pending_margin_cash_withdrawal_nhsv`: String, Tiền ký quỹ chờ rút tại NHSV
    - `pending_margin_cash_withdrawal_vsd`: String, Tiền ký quỹ chờ rút tại VSD
    - `accepted_collateral_balance_nhsv`: String, Số dư đảm bảo được chấp nhận tại NHSV
    - `accepted_margin_securities_value_nhsv`: String, Giá trị CK KQ được chấp nhận tại NHSV
    - `accepted_collateral_balance_vsd`: String, Số dư đảm bảo được chấp nhận tại VSD
    - `margin_cash_balance_nhsv`: String, Số dư tiền ký quỹ tại NHSV
    - `pending_withdrawal_margin_securities_nhsv`: String, Giá trị CK KQ chờ rút tại NHSV
    - `margin_cash_balance_vsd`: String, Số dư tiền ký quỹ tại VSD
    - `pending_withdrawal_margin_securities_vsd`: String, Giá trị CK KQ chờ rút tại VSD
    - `accepted_margin_securities_vsd`: String, Giá trị CK KQ được chấp nhận tại VSD
    - `margin_securities_value_vsd`: String, Giá trị chứng khoán ký quỹ tại VSD
    - `value_required_vsd`: String, Giá trị VSD yêu cầu
    - `margin_asset_utilization_ratio`: String, Tỷ lệ sử dụng tài sản ký quỹ
    - `deposit_nhsv_require`: String, Số tiền ký quỹ NHSV yêu cầu
    - `net_assets`: String, Tài sản ròng

#### 2.1.6 DRACC-035: Cung cấp các giao dịch tiền phát sinh trên tài khoản nhà đầu tư
- **URL**: `[Root URL APIKEY]/tuxsvc/der/account/dr-monetary-transaction`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `start_date`: String, Y, F: YYYYMMDD (Từ ngày)
    - `end_date`: String, Y, F: YYYYMMDD (Tới ngày)
    - `account_no`: String, Y, Số tài khoản
    - `sub_no`: String, Y, Tiểu khoản
    - `bank_code`: String, Y, Mã ngân hàng cho vay
    - `type`: String, Y, Phân loại tra cứu
    - `next_key`: String, Y, Default: "0" (Biến next)
    - `hts_user_id`: String, Y, Default: lthpt01 (hts_user_id của TK tra cứu của công ty được phân quyền)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `trans_date`: String, Ngày phát sinh
    - `trans_id`: String, Số thứ tự giao dịch
    - `trans_type`: String, Phân loại nghiệp vụ
    - `money_increase`: String, Tiền phát sinh tăng
    - `money_decrease`: String, Tiền phát sinh giảm
    - `cumulative`: String, Lũy kế
    - `description`: String, Diễn giải
    - `date_and_id_trans`: String, Ngày và số thứ tự giao dịch
    - `business_code`: String, Mã nghiệp vụ
    - `start_balance`: String, Số dư đầu kỳ
    - `end_balance`: String, Số dư cuối kỳ
    - `start_date_trans`: String, Ngày đầu tiên phát sinh giao dịch
    - `pending_balance`: String, Số dư chờ thanh toán
    - `end_date_trans`: String, Ngày cuối cùng phát sinh giao dịch
    - `bank_name`: String, Tên ngân hàng vay
    - `deposit`: String, Tiền ký quỹ

#### 2.1.7 DRACC-036: Cung cấp trạng thái tài khoản và số tiền cần bổ sung khi vi phạm tỷ lệ MU
- **URL**: `[Root URL APIKEY]/tuxsvc/der/account/dr-mu-breach-account`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `query_type`: String, Y, "0": Query mới, "else": Query tiếp (Phân loại tra cứu)
    - `account_no`: String, Y, Số tài khoản
    - `settlement_status`: String, Y, 0: Tất cả, 1: Non-margin call, 2: Margin call, 3: Force sell (Trạng thái tất toán)
    - `branch`: String, Y, Default: "%" (Chi nhánh)
    - `department`: String, Y, Default: "%" (Phòng giao dịch)
    - `customer_type`: String, Y, "0": Netting account, "1": Non-netting account, "9": Tất cả (Phân loại khách hàng)
    - `warning_type`: String, Y, "%": Tất cả, "1": Cảnh báo 1, "2": Cảnh cáo 2, "3": Cảnh cáo 3 (Phân loại cảnh báo)
    - `next_key`: String, Y, Default: "0" (Biến next)
    - `hts_user_id`: String, Y, Default: lthpt01 (hts_user_id của TK tra cứu của công ty được phân quyền)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `account_no`: String, Số tài khoản
    - `account_name`: String, Tên tài khoản
    - `product_code`: String, Mã sản phẩm
    - `contract_code`: String, Mã hợp đồng
    - `contract_name`: String, Tên hợp đồng
    - `sales_type_code`: String, Mã phân loại mua bán
    - `sales_type_name`: String, Tên phân loại mua bán
    - `positions_count`: String, Số lượng vị thế
    - `settlement_rate`: String, Tỷ lệ tất toán
    - `settlement_quantity`: String, Số lượng tất toán
    - `settlement_status_code`: String, Mã trạng thái tất toán
    - `settlement_status_name`: String, Tên trạng thái tất toán
    - `next_key`: String, Biến key
    - `mu_ratio`: String, Tỷ lệ MU
    - `warning_type`: String, Mã trạng thái tài khoản
    - `value`: String, Giá trị cần bù
    - `amount`: String, Số tiền cần bổ sung

#### 2.1.8 DRACC-037: Lãi/lỗ theo ngày
- **URL**: `[Root URL APIKEY]/tuxsvc/der/account/dr-daily-profit-loss`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `account_no`: String, Y, Tên tài khoản
    - `password`: String, Y, Mật khẩu đã mã hóa
    - `start_date`: String, Y, F: YYYYMMDD (Từ ngày)
    - `end_date`: String, Y, F: YYYYMMDD (Tới ngày)
    - `product_code`: String, Y, Mã sản phẩm
    - `search_type`: Boolean, Y, true: Tra cứu toàn bộ, false: Tra cứu từng TK (Loại tra cứu)
    - `next_key`: String, Y, Default: "0" (Biến next)
    - `branch`: String, Y, Default: "%" (Chi nhánh)
    - `department`: String, Y, Default: "%" (Phòng giao dịch)
    - `contract_code`: String, Y, Default: "%" (Mã hợp đồng)
    - `hts_user_id`: String, Y, Default: lthpt01 (hts_user_id của TK tra cứu của công ty được phân quyền)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `date`: String, Ngày giao dịch
    - `account_no`: String, Số tài khoản
    - `account_name`: String, Tên tài khoản
    - `product_code`: String, Mã sản phẩm
    - `contract_code`: String, Mã hợp đồng
    - `product_name`: String, Tên sản phẩm
    - `realized_profit_loss`: String, Lãi lỗ đã thực hiện
    - `unrealized_profit_loss`: String, Lãi lỗ chưa thực hiện
    - `fee`: String, Phí
    - `net_profit_loss`: String, Net lãi lỗ
    - `next_key`: String, Biến key
    - `tax`: String, Thuế

### 2.2 GIAO DỊCH TIỀN PHÁI SINH

#### 2.2.1 DRACC-009: Rút tiền ký quỹ
- **URL**: `[RootURL]/tsol/apikey/tuxsvc/der/account/dr-withdrawal-deposit`
- **Method**: GET
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, user_id
    - `acnt_no`: String, Y, Tài khoản
    - `src_acnt`: String, Y, Tài khoản NH chuyển
    - `des_acnt`: String, Y, Tài khoản NH nhận
    - `trd_amt`: String, Y, Số tiền rút
    - `trd_tp`: String, Y, "C10" rút tiền (Phân loại)
    - `cnte`: String, Y, Diễn giải
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `scrt_err_msg`: String, Message thực hiện thành công

#### 2.2.2 DRACC-019: Chuyển khoản nội bộ phái sinh
- **URL**: `[Root URL APIKEY]/tsol/apikey/tuxsvc/der/account/dr-transfer-cash`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `snd_acnt`: String, Y, Số TK chuyển
    - `snd_sub`: String, Y, Sub chuyển
    - `rcv_acnt`: String, Y, Số TK nhận
    - `rcv_sub`: String, Y, Sub nhận
    - `amount`: Number, Y, Số tiền
    - `remark`: String, Y, Nội dung
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `date`: String, Ngày giao dịch
    - `no`: String, Số thứ tự giao dịch trong ngày của sub nguồn
    - `balance_before_transfer`: String, Số dư tiền của sub nguồn trước khi chuyển
    - `balance_after_transfer`: String, Số dư tiền của sub nguồn sau khi chuyển
    - `no_receive`: String, Số thứ tự giao dịch trong ngày của sub đích
    - `balance_before_transfer_receive`: String, Số dư tiền của sub đích trước khi chuyển
    - `balance_after_transfer_receive`: String, Số dư tiền của sub đích sau khi chuyển

#### 2.2.3 DRACC-020: Tra cứu lịch sử chuyển tiền nội bộ cơ sở phái sinh
- **URL**: `[Root URL APIKEY]/tsol/apikey/tuxsvc/der/account/dr-fund-transfer-history`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `account`: String, Y, Số TK
    - `next_data`: String, Y, Nếu không có thì bỏ trống (Next data)
    - `date_fr`: String, Y, Từ ngày
    - `date_to`: String, Y, Tới ngày
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `trade_date`: String, Ngày GD
    - `send_acc`: String, Số tài khoản chuyển
    - `send_acc_sub`: String, Số sub tài khoản chuyển
    - `send_acc_name`: String, Tên tài khoản chuyển
    - `seq_no`: String, Số thứ tự giao dịch
    - `amount`: String, Số tiền chuyển
    - `recv_acc`: String, Số tài khoản nhận
    - `recv_acc_sub`: String, Số sub nhận
    - `recv_acc_name`: String, Tên tài khoản nhận
    - `remarks`: String, Nội dung chuyển khoản
    - `trading_channel`: String, Kênh giao dịch
    - `iscanceled`: String, Trạng thái huỷ
    - `next_data`: String, next key

#### 2.2.4 DRACC-021: Tra cứu thông tin lịch sử nộp/rút VSD
- **URL**: (Không có trong tài liệu)
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `acnt`: String, Y, Số TK
    - `type`: String, Y, C05 - Nộp ký quỹ / C10 - Rút ký quỹ / % - Cả 2 (Loại)
    - `date_fr`: String, Y, Từ ngày
    - `date_to`: String, Y, Tới ngày
    - `next_data`: String, Y, 000000000000000 (Next data)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `acnt`: String, Tài khoản
    - `acnt_sub`: String, Số sub
    - `type`: String, Phân loại
    - `amount`: String, Số tiền giao dịch
    - `target_acnt`: String, Tài khoản đích
    - `note`: String, Ghi chú
    - `user_executes`: String, User thực hiện
    - `status_vtb`: String, Trạng thái VTB
    - `status_bos`: String, Trạng thái BOS
    - `status_vsd`: String, Trạng thái VSD
    - `trading_channel`: String, Kênh giao dịch
    - `source_actn`: String, Tài khoản nguồn
    - `reg_date`: String, Ngày đăng ký
    - `fees`: String, Phí
    - `amount_received`: String, Số tiền thực nhận
    - `next_data`: String, Next data

#### 2.2.5 DRACC-032: API trả danh sách ngân hàng đang có hiệu lực trên 15701
- **URL**: `[Root URL APIKEY]/tsol/apikey/tuxsvc/der/account/list_sec_bank_actn_dr`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data**: Không có (empty JSON object `{}`)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `os_brch_code`: String, Mã chi nhánh
    - `os_bank_code`: String, Mã chi nhánh ngân hàng
    - `os_bank_acc_num`: String, Số tài khoản ngân hàng
    - `os_bank_acc_nm`: String, Tên tài khoản ngân hàng
    - `os_bank_type_nm`: String, Loại tài khoản ngân hàng
    - `os_biccode_bank_type`: String, Mã Biccode banktype

#### 2.2.6 DRACC-033: API trả ra phí nộp tiền ký quỹ
- **URL**: `[Root URL APIKEY] /tuxsvc/der/account/get_trd_fee`
- **Method**: POST
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `is_acnt_no`: String, Y, Tài khoản
    - `is_sub_no`: String, Y, Sub
    - `is_recv_bank`: String, N, Tài khoản ngân hàng nhận
    - `is_send_bank`: String, Y, Tài khoản ngân hàng chuyển
    - `is_trd_amt`: String, Y, Số tiền rút
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `trade_fee_amt`: String, Phí chuyển khoản
    - `adjusted_amt`: String, Số tiền điều chỉnh
    - `real_trd_amt`: String, Số tiền thực nhận
    - `fee_type`: String, Phân loại tính phí

#### 2.2.7 DRACC-034: API thực hiện nộp tiền ký quỹ
- **URL**: `[Root URL APIKEY]/tuxsvc/der/account/dr_cw_cash_trans`
- **Method**: POST
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `dept_no1`: String, Y, Mã chi nhánh
    - `hts_user_id`: String, Y, User ID
    - `is_acnt_no`: String, Y, Tài khoản
    - `is_sub_no`: String, Y, Sub
    - `is_dpo_block`: String, Y, Số tiền nộp ký quỹ
    - `is_in_bank_src`: String, Y, Tài khoản ngân hàng nhận
    - `is_cnte`: String, Y, Diễn giải
    - `is_in_bank_dest`: String, Y, Tài khoản ngân hàng chuyển
    - `is_fee_amt`: String, Y, Phí chuyển khoản
    - `is_adj_amt`: String, Y, Số tiền điều chỉnh
    - `is_acc_amt`: String, Y, Số thực nhận
    - `is_fee_calc_tp`: String, Y, Phân loại tính phí lấy từ API DRACC-033
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**: Không có field mô tả cụ thể

### 2.3 LỆNH PHÁI SINH

#### 2.3.1 DRORD-005: Lệnh điều kiện MUA
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-stop-order-buy`
- **Method**: POST
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, Tài khoản thực hiện
    - `acnt_no`: String, Y, Tài khoản
    - `mdm_tp`: String, N, Kênh thực hiện
    - `stk_cd`: String, Y, Mã CK
    - `ord_qty`: String, Y, Số lượng
    - `ord_pri`: String, Y, Giá
    - `ord_band_pri`: String, Y, Bước giá
    - `from_dt`: String, Y, yyyyMMdd (Ngày bắt đầu và Ngày kết thúc phải cùng giá trị)
    - `end_dt`: String, Y, yyyyMMdd (Ngày kết thúc)
    - `mac_addr`: String, N, Địa chỉ MAC
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**: Không có field mô tả cụ thể

#### 2.3.2 DRORD-006: Lệnh điều kiện BÁN
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-stop-order-sell`
- **Method**: POST
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, Tài khoản thực hiện
    - `acnt_no`: String, Y, Tài khoản
    - `mdm_tp`: String, N, Kênh thực hiện
    - `stk_cd`: String, Y, Mã CK
    - `ord_qty`: String, Y, Số lượng
    - `ord_pri`: String, Y, Giá
    - `ord_band_pri`: String, Y, Bước giá
    - `from_dt`: String, Y, yyyyMMdd (Ngày bắt đầu và Ngày kết thúc phải cùng giá trị)
    - `end_dt`: String, Y, yyyyMMdd (Ngày kết thúc)
    - `mac_addr`: String, N, Địa chỉ MAC
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**: Không có field mô tả cụ thể

#### 2.3.3 DRORD-010: Lịch sử đặt lệnh
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-order-history`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, Tài khoản thực hiện
    - `acnt`: String, Y, Số TK
    - `date`: String, Y, Ngày
    - `next_data`: String, Y, Lần đầu là khoảng trắng (Next key)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `jmno`: String, Số hiệu lệnh
    - `ojno`: String, Số hiệu lệnh gốc
    - `jcgb`: String, New/Cancel/Edit
    - `code`: String, Mã HĐ
    - `mdms`: String, Buy/Sell
    - `jqty`: String, Khối lượng
    - `dqty`: String, KL Khớp tích luỹ
    - `cmqt`: String, Khối lượng khớp
    - `mqty`: String, Khối lượng chưa khớp
    - `type`: String, Loại
    - `jprc`: String, Giá
    - `time`: String, Ngày
    - `jmgb`: String, 0:DAY, 2:ATO, 3:1OC, 4:FOK, 7:ATC
    - `acnt_heg_sect`: String, NET-NON/NET
    - `user`: String, User
    - `rmsg`: String, Lý do từ chối
    - `os_next_key`: String, Next_key

#### 2.3.4 DRORD-011: Danh sách lệnh có thể hủy sửa
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-nmth-order`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `is_actn_no`: String, Y, Số TK
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `acno`: String, Số TK
    - `acnm`: String, Tên TK
    - `jmno`: String, Số hiệu lệnh
    - `stat`: String, New Order / Edit Order
    - `mtst`: String, 1- New (Lệnh Mới)
    - `type`: String, Loại lệnh : 1 MTL 2 LO
    - `code`: String, Mã HĐ
    - `mdms`: String, Buy/Sell 1/2
    - `jqty`: String, Khối lượng
    - `jprc`: String, Giá
    - `cqty`: String, Khối lượng khớp
    - `mqty`: String, Khối lượng chưa khớp
    - `jmgb`: String, 0:DAY, 2:ATO, 3:MAK, 4:MOK, 7:ATC, 9:MTL
    - `ord_style`: String, Không dùng
    - `os_next_key`: String, Không dùng

#### 2.3.5 DRORD-016: Tra cứu Lịch sử lệnh điều kiện trong ngày
- **URL**: `[RootURL]/ tsol/apikey/tuxsvc/der/order/dr-condition-ord-in-day`
- **Method**: POST, GET
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, Tài khoản thực hiện
    - `acnt_no`: String, Y, Tài khoản
    - `ctr_cd`: String, N, "": tra cứu tất cả (Mã CK)
    - `sent`: String, Y, 0: all, 1: đã gửi, 2: chưa gửi (Phân loại gửi)
    - `sell_buy_tp`: String, Y, 0: all, 1: mua, 2: bán (Phân loại mua bán)
    - `next_data`: String, N, D: 0 (Key để tra cứu data tiếp theo)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `list->date`: String, Ngày
    - `list->seq_no`: String, seq
    - `list->acnt_no`: String, Số TK
    - `list->acent_nm`: String, Tên TK
    - `list->ctr_code`: String, Mã CK
    - `list->ctr_name`: String, Tên CK
    - `list->sell_buy_tp`: String, 1: bán / 2: mua (Loại mua bán)
    - `list->ord_tp`: String, 1: MP, 2: LO, 9: MTL (loại lệnh)
    - `list->validity`: String
    - `list->qty`: String, Khối lượng
    - `list->price`: String, Giá
    - `list->band_pri`: String, Bước giá
    - `list->str_ord_dt`: String, Ngày bắt đầu
    - `list->end_ord_dt`: String, Ngày kết thúc
    - `list->medium_sect`: String, 00: BOS 03: WTS 04: HTS (Kênh đặt lệnh)
    - `list->hts_id`: String, TK người đặt
    - `list->registered`: String, Y: Đã đăng ký / N: Chưa đăng ký (Phân loại đăng ký)
    - `list->registered_dt`: String, Ngày đăng ký
    - `list->cancel_id`: String
    - `list->cancel_dt`: String
    - `list->ord_sent`: String
    - `list->ord_dt`: String
    - `list->ord_no`: String
    - `list->err_cd`: String, Mã lỗi
    - `list->err_msg`: String, Thông báo lỗi
    - `list->ip_add`: String, Địa chỉ ip
    - `list->update_emp`: String, Người cập nhật
    - `list->update_time`: String, Thời gian cập nhật
    - `list->ref_no`: String
    - `list->next_data`: String, Key để lấy bản ghi tiếp theo
    - `list->ord_amt`: String, giá trị GD

#### 2.3.6 DRORD-023, DRORD-024: Sửa lệnh Điều kiện Mua, Sửa lệnh Điều kiện Bán
- **URL**: `[Root URL APIKEY] /tuxsvc/der/order/dr-replace-stop-order`
- **Method**: POST
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, user_id
    - `date`: String, Y, Format: yyyyMMDD (Ngày đặt lệnh)
    - `seqn`: String, Y, Số hiệu lệnh
    - `acno`: String, Y, Tài khoản
    - `jqty`: String, Y, Khối lượng đặt mới
    - `jprc`: String, Y, Giá đặt mới
    - `bprc`: String, Y, Biên độ giá mới
    - `sdate`: String, Y, Format: yyyyMMDD (sdate = edate) (Ngày bắt đầu mới)
    - `edate`: String, Y, Format: yyyyMMDD (sdate = edate) (Ngày kết thúc mới)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `seqn`: String, Message thành công

#### 2.3.7 DRORD-025, DRORD-026: Hủy lệnh Điều kiện Mua, Hủy lệnh Điều kiện Bán
- **URL**: (Không có trong tài liệu)
- **Method**: (Không có trong tài liệu)
- **Authenticate**: (Không có trong tài liệu)
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, user_id
    - `date`: String, Y, Format: yyyyMMDD (Ngày đặt lệnh)
    - `seqn`: String, Y, Số hiệu lệnh
    - `acno`: String, Y, Tài khoản
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `dummy`: String, Message thành công

#### 2.3.8 DRORD-028: Tra cứu khả năng đặt lệnh
- **URL**: `[Root URL APIKEY]`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `acnt_no`: String, Y, Số TK
    - `code`: String, Y, Mã CK
    - `sell_buy_type`: Number, Y, Kiểu mua bán
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `avail_order_qty`: Số lượng có thể đặt
    - `avail_liq_qty`: Số lượng thanh khoản có sẵn

#### 2.3.9 DRORD-029: Lệnh Mua By User
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-buy-by-user`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `lang_code`: String, N, Default: V (V: Tiếng Việt, E: Tiếng Anh, K: Tiếng Hàn) (Loại ngôn ngữ trả về của API)
    - `row_count`: Int, N, Default: 500 (Số lượng bản ghi trả về)
    - `next_key`: String, N, Mặc định request đầu tiên để trống, những request sau, lấy giá trị của output.next_key
    - `acnt_no`: String, Y, Số TK
    - `ft_code`: String, Y, Mã hợp đồng
    - `ord_type`: String, Y, 2:LO, 3:ATO, 4: MAK, 5: MOK, 6: ATC, 9: MTL
    - `ord_qty`: double, Y, Khối lượng đặt lệnh
    - `lm_ord_price`: float, Y, Giá đặt
    - `ip_addr`: String, Y, IP
    - `user_id`: String, Y, Max 15 kí tự, nhập đúng user được phân quyền đặt lệnh cho TK (user id)
    - `mac_addr`: String, **Y (bắt buộc)**, Địa chỉ MAC / device unique ID (TradeX: FE truyền `deviceUniqueId` → map sang field này)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `order_no`: String, Số hiệu lệnh

#### 2.3.10 DRORD-030: Lệnh Bán By User
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-sell-by-user`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `lang_code`: String, N, Default: V (V: Tiếng Việt, E: Tiếng Anh, K: Tiếng Hàn) (Loại ngôn ngữ trả về của API)
    - `row_count`: Int, N, Default: 500 (Số lượng bản ghi trả về)
    - `next_key`: String, N, Mặc định request đầu tiên để trống, những request sau, lấy giá trị của output.next_key
    - `acnt_no`: String, Y, Số TK
    - `ft_code`: String, Y, Mã hợp đồng
    - `ord_type`: String, Y, 2:LO, 3:ATO, 4: MAK, 5: MOK, 6: ATC, 9: MTL
    - `ord_qty`: double, Y, Khối lượng đặt lệnh
    - `lm_ord_price`: float, Y, Giá đặt
    - `ip_addr`: String, Y, IP
    - `user_id`: String, Y, Max 15 kí tự, nhập đúng user được phân quyền đặt lệnh cho TK (user id)
    - `mac_addr`: String, **Y (bắt buộc)**, Địa chỉ MAC / device unique ID (TradeX: FE truyền `deviceUniqueId` → map sang field này)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `order_no`: String, Số hiệu lệnh

#### 2.3.11 DRORD-031: Lệnh Hủy By User
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-can-by-user`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `lang_code`: String, N, Default: V (V: Tiếng Việt, E: Tiếng Anh, K: Tiếng Hàn) (Loại ngôn ngữ trả về của API)
    - `row_count`: Int, N, Default: 500 (Số lượng bản ghi trả về)
    - `next_key`: String, N, Mặc định request đầu tiên để trống, những request sau, lấy giá trị của output.next_key
    - `acnt_no`: String, Y, Số TK
    - `ord_no`: String, Y, Số hiệu lệnh
    - `ft_code`: String, Y, Mã hợp đồng
    - `validity`: String, Y, 0:DAY
    - `ip_addr`: String, Y, IP
    - `user_id`: String, Y, Max 15 kí tự, nhập đúng user được phân quyền đặt lệnh cho TK (user id)
    - `mac_addr`: String, N, Địa chỉ MAC
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `order_no`: String, Số hiệu lệnh

#### 2.3.12 DRORD-032: Lệnh Sửa By User
- **URL**: `[Root URL APIKEY] /tuxsvc/der/order/dr-mod-by-user`
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `lang_code`: String, N, Default: V (V: Tiếng Việt, E: Tiếng Anh, K: Tiếng Hàn) (Loại ngôn ngữ trả về của API)
    - `row_count`: Int, N, Default: 500 (Số lượng bản ghi trả về)
    - `next_key`: String, N, Mặc định request đầu tiên để trống, những request sau, lấy giá trị của output.next_key
    - `acnt_no`: String, Y, Số TK
    - `ord_no`: String, Y, Số hiệu lệnh
    - `ft_code`: String, Y, Mã hợp đồng
    - `validity`: String, Y, 0:DAY
    - `ord_qty`: double, Y, Khối lượng đặt lệnh
    - `ord_price`: float, Y, Giá đặt
    - `un_mth_qty`: double, Y, Khối lượng chưa khớp
    - `ip_addr`: String, Y, IP
    - `user_id`: String, Y, Max 15 kí tự, nhập đúng user được phân quyền đặt lệnh cho TK (user_id)
    - `mac_addr`: String, N, Địa chỉ MAC
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `order_no`: String, Số hiệu lệnh

#### 2.3.13 DRORD-033: Tra cứu lịch sử đặt lệnh thường (từ ngày – đến ngày)
- **URL**: `[Root URL APIKEY]/tuxsvc/der/order/dr-order-history-range` (hoặc theo tài liệu Lotte cung cấp)
- **Method**: POST
- **Authenticate**: API KEY
- **Request Header**:
    - `apiKey`: Y, [API KEY]
    - `Content-Type`: Y, `application/json`
- **Request Data (JSON object)**:
    - `hts_user_id`: String, Y, Tài khoản thực hiện
    - `acnt`: String, Y, Số TK
    - `date_fr`: String, Y, Từ ngày (yyyyMMdd)
    - `date_to`: String, Y, Đến ngày (yyyyMMdd)
    - `next_data`: String, Y, Lần đầu khoảng trắng hoặc "0" (Next key)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `total_record`: String, N (có thể rỗng; TradeX không cần trả total items)
    - `data_list`: DataResponse (array of Object Types)
- **Object Types (DataResponse)** — mỗi phần tử trong `data_list` (response thực tế dùng prefix `os_`):
    - `os_date`: String, Ngày (yyyyMMdd)
    - `os_jcgb`: String, Loại thao tác (1: New, …; map New/Cancel/Edit)
    - `os_mdtp`: String, Mã loại (vd. 06)
    - `os_fcm_ord_no`: String, Số lệnh FCM
    - `os_mth_atm`: String, (nội bộ)
    - `os_mprc`: String, Giá khớp (có thể space)
    - `os_acno`: String, Số TK
    - `os_acnm`: String, Tên TK
    - `os_jmno`: String, Số hiệu lệnh
    - `os_ojno`: String, Số hiệu lệnh gốc (có thể có leading space)
    - `os_code`: String, Mã HĐ
    - `os_mdms`: String, Buy/Sell (1: Mua, 2: Bán)
    - `os_jqty`: String, Khối lượng (có thể có leading space)
    - `os_cqty`: String, Khối lượng khớp (có thể có leading space)
    - `os_mqty`: String, Khối lượng chưa khớp (có thể có leading space)
    - `os_cncl_qty`: String, Khối lượng đã hủy
    - `os_type`: String, Loại lệnh (1: xem thêm os_jmgb → ATO/MAK/MOK/ATC/MTL; 2: LO, bỏ qua os_jmgb)
    - `os_jprc`: String, Giá đặt
    - `os_time`: String, Giờ (HHMMSS)
    - `os_user`: String, User
    - `os_jmgb`: String, 0:DAY, 2:ATO, 3:MAK, 4:MOK, 7:ATC, 9:MTL (dùng khi os_type=1)
    - `os_ord_style`: String, (nội bộ)
    - `os_ipad`: String, IP
    - `os_msg_rsn`: String, Lý do từ chối (có thể space)
    - `os_ord_stat`: String, Trạng thái lệnh (0: Tiếp nhận; 1: Xác nhận tiếp nhận; 2: Khớp 1 phần; 3: Khớp toàn bộ; 4: Từ chối)
    - `os_next_key`: String, Next key (pagination)

### 2.4 DERIVATIVE MARKET DATA

#### 2.4.1 DRMKT-001: Lấy ds các mã phái sinh
- **URL**: `[RootURL]/tsol/apikey/tuxsvc/market/dr/stock-board`
- **Method**: POST, GET
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data**: Không có (empty JSON object `{}`)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `next_data`: String
    - `list_items`: Array of objects, mỗi object chứa:
        - `code`: String, Mã CK
        - `last`: Double, Giá hiện tại
        - `change`: Double, Thay đổi
        - `vol`: Long, Khối lượng giao dịch
        - `ceiling`: Double, Giá trần
        - `floor`: Double, Giá sàn
        - `open`: Double, Giá mở cửa
        - `high`: Double, Giá cao nhất
        - `low`: Double, Giá thấp nhất
        - `bid1`, `bid2`, `bid3`: Double, Giá mua 1,2,3
        - `offer1`, `offer2`, `offer3`: Double, Giá bán 1,2,3
        - `value`: Double, Giá trị giao dịch
        - `oi`: Long, KL mở (OI)
        - `change_rate`: Double, Tỷ lệ thay đổi
        - `total_vol`: Long, Tổng KL Khớp
        - `pt_vol`: Long, Tổng KL thỏa thuận
        - `matched_vol`: Long, KL Khớp
        - `ref_price`: Double, Giá tham chiếu
        - `exp_date`: Int, Tháng đáo hạn
        - `ceiling_status`, `open_status`, `high_status`, `low_status`, `bid1_status`, `bid2_status`, `bid3_status`, `offer1_status`, `offer2_status`, `offer3_status`: Byte, Mục "Quy định bảng mã" -> Mã trạng thái giá - price status
        - `bid1_size`, `bid2_size`, `bid3_size`, `offer1_size`, `offer2_size`, `offer3_size`: Long, Khối lượng giá mua/bán
        - `control_code`: String, Trạng thái thị trường - controlCode
        - `foreign_buy_vol`, `foreign_sell_vol`: Long, Khối lượng nước ngoài mua/bán
        - `oi_status`: Byte, Thay đổi OI (%)

#### 2.4.2 DRMKT-002: Thông tin Giá của mã phái sinh
- **URL**: `[RootURL]/tsol/apikey/tuxsvc/market/dr/stock-price`
- **Method**: GET
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `code`: String, Y, Mã chứng khoán
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `code`: String, Mã chứng khoán
    - `name`: String, Tên chứng khoán
    - `time`: String, Thời gian
    - `ceiling`, `floor`, `open`, `high`, `low`, `last`, `change`: Double
    - `status`: Byte
    - `volume`, `amount`: Long
    - `bid`, `offer`, `basis`, `disparate`, `ref_price`, `average_price`: Double
    - `open_status`, `high_status`, `low_status`, `last_status`, `change_rate`: Byte/Double
    - `pt_volume`, `tot_volume`, `prev_volume`: Long
    - `turnover_ratio`: Double
    - `pt_value`, `total_value`: Long
    - `par_value`: Double
    - `list_stk_qty`: Double
    - `foreign_buy_vol`, `foreign_sell_vol`: Long
    - `for_tot_room`, `for_cur_room`: Long
    - `project_open`: Double
    - `project_open_status`: Byte
    - `control_code`: String
    - `high52`, `low52`: Double
    - `high_low_list`: List of objects
    - `bid_status`, `offer_status`: Byte
    - `bid_offer_list`: List of objects
    - `total_vis_bid_size`, `total_vis_offer_size`, `total_visible_bid_size_change`, `total_visible_offer_size_change`, `vis_bid_offer_size_diff`, `total_bid_size`, `total_bid_count`, `total_offer_size`, `total_offer_count`: Long
    - `base_code`: String
    - `open_interest`: Long
    - `open_int_chg`: Byte
    - `normal_fore_buy_vol`, `normal_fore_buy_val`, `normal_fore_sell_vol`, `normal_fore_sell_val`, `pt_fore_tot_buy_vol`, `pt_fore_tot_buy_val`, `pt_fore_tot_sell_vol`, `pt_fore_tot_sell_val`: Long/Double
    - `first_trd_date`, `end_trd_date`, `remain_date`: Int
    - `theory_price`, `theory_basis`, `market_basis`, `disparate_rate`: Double
    - `start_date`, `end_date`: String

#### 2.4.3 DRMKT-003: Future Quote By Minute
- **URL**: `[RootURL]/tsol/apikey/tuxsvc/market/dr/minutely-derivatives`
- **Method**: POST, GET
- **Authenticate**: Oauth2, API KEY
- **Request Header**:
    - `Authorization`: Y, bearer access_token (lấy từ API mục 4.1)
    - `apiKey`: Y, [API KEY]
- **Request Data (JSON object)**:
    - `code`: String, Y, Mã chứng khoán
    - `time_unit`: String, N, Default: "0" (0: 1 phút, 1: 5 phút, 2: 10 phút, 3: 30 phút) (Bước thời gian)
    - `base_time`: String, N, Default: "30000101" (key để lấy các bản ghi trước đó)
    - `next_time`: String, N, Default: "235959" (key để lấy các bản ghi trước đó)
- **Response Data**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công)
    - `error_desc`: String, Y
    - `success`: boolean, Y (true/false)
    - `data_list`: DataResponse
- **Object Types (DataResponse)**:
    - `has_next`: Boolean
    - `next_key`: String
    - `list_items`: Array of objects, mỗi object chứa:
        - `time`: String
        - `last`: Double
        - `change`: Double
        - `open`: Double
        - `high`: Double
        - `low`: Double
        - `volume`: Long
        - `value`: Double
        - `last_status`, `change_status`, `open_status`, `high_status`, `low_status`: Byte (Mục "Quy định bảng mã" -> Mã trạng thái giá - price status)
        - `change_rate`: Double

## 3. KẾT NỐI WEB SOCKET ĐỂ NHẬN DỮ LIỆU REALTIME THỊ TRƯỜNG

*(Theo tài liệu 18/03/2026 – Mục 4 trong PDF)*

### 3.1 Cấu trúc dữ liệu REALTIME data

#### 3.1.1 Order events

- **Đăng ký nhận dữ liệu:** `sub/bos.evt.ord.sts.*/`
- **Cấu trúc dữ liệu sự kiện:** JSON

| FieldName    | FieldType | Valid Values | Format   | Description           |
|--------------|-----------|--------------|----------|------------------------|
| event_code   | String    | F15302       |          | Mã sự kiện (Khớp lệnh phái sinh) |
| event_seqno  | String    |              |          | Số sequence           |
| date         | String    |              | yyyymmdd | Ngày sự kiện          |
| acnt_no      | String    |              |          | Số TK                 |
| series       | String    |              |          | Mã hợp đồng           |
| sb_tp        | String    |              |          | Phân loại lệnh mua bán |
| mth_qty      | String    |              |          | Số lượng khớp lệnh    |
| mth_pri      | String    |              |          | Giá khớp              |

#### 3.1.2 Account events

- **Đăng ký nhận dữ liệu:** `sub/bos.evt.acc.inf.*/`
- **Cấu trúc dữ liệu sự kiện:** JSON (nhiều loại event_code)

| event_code | Mô tả |
|------------|--------|
| F15102 | Đóng tài khoản phái sinh |
| F15201 | Cảnh báo ngưỡng giới hạn vị thế 1 |
| F15202 | Cảnh báo ngưỡng giới hạn vị thế 2 |
| F15401 | Thanh toán lãi VM |
| F15402 | Thanh toán lỗ VM |
| F15403 | Nộp tiền bổ sung khi chạm cảnh báo 2 |
| F15404 | Nộp tiền bổ sung khi TK chạm ngưỡng W3 |
| F15405 | Nộp tiền bổ sung khi TK chạm ngưỡng W3 (short_dpo, target_dt) |
| F15701 | Nộp tiền ký quỹ |
| F15702 | Rút tiền ký quỹ |

Các field chung: `event_code`, `event_seqno`, `date`, `acnt_no`; mỗi event có thêm field riêng (trd_amt, trd_dt, new_vsd_dpo, W2, commd_cd, CU, short_dpo, target_dt, v.v.) — chi tiết xem PDF trang 128–132.

---

**Lưu ý:** Dữ liệu **market data realtime** (giá, sổ lệnh phái sinh) qua WebSocket **auto.dr.qt** / **auto.dr.bo** (format pipe-separated) không nằm trong mục 4.1 của tài liệu này; định dạng đang dùng theo message thực tế và được mô tả trong **Derivatives/Planning documentation/Market/Planning/01_Integration_Plan.md** (§4.3.2).

---

## 4. MỤC QUY TẮC CHUNG

*(Trong PDF 18/03/2026 là Mục 5)*

### 4.1 CẤU TRÚC DỮ LIỆU
- **Cấu trúc chung của request**:
    - Request JSON: `{ [header-fields] [function-fields] }`
    - **Danh sách header-fields (JSON object)**:
        - `lang_code`: String, N, Mục “Quy định bằng mã” -> Ngôn ngữ - lang_code (Mã ngôn ngữ)
        - `hts_user_id`: String, N, Tài khoản người thực hiện
        - `hts_user_nm`: String, N, Tên người thực hiện
        - `cli_ip_addr`: String, N, Địa chỉ IP người thực hiện
        - `mac_addr`: String, N, Địa chỉ MAC người thực hiện. Giá trị này sẽ sử dụng cho định danh thiết bị sau này.
        - `row_count`: String, N, 40 (Số lượng bản ghi trả về)
        - `mdm_tp`: String, N, Không bắt buộc, hoặc phải nhập các giá trị bên dưới:
            - 9: Dùng cho đối tác FTL của SHS
            - 10: Dùng cho đối tác FTL của SHS
            - 11: Dùng cho đối tác FTL của SHS
            - 12: Dùng cho đối tác FTL của SHS
- **Cấu trúc chung của response**:
    - `error_code`: String, Y (0000: Thành công, 1005: Không thành công) (Mã lỗi thực hiện API)
    - `error_desc`: String, Y (Nội dung mã lỗi)
    - `success`: boolean, Y (true: Xử lý nghiệp vụ thành công, false: Quá trình xử lý có lỗi) (Trạng thái thực hiện API)
    - `data_list`: DataResponse (Xem mô tả struct của object ở Object Types của API này) (Cấu trúc data trả về)
- **Response Example**:
    ```json
    {
      "error_code": "0000",
      "error_desc": "",
      "success": true,
      "total_record": "",
      "data_list": [{}]
    }
    ```

### 4.2 QUY ĐỊNH BẢNG MÃ
- **Ngôn ngữ - lang_code**:
    - `V`: Tiếng Việt
    - `E`: Tiếng Anh
    - `K`: Tiếng Hàn
- **Trạng thái thị trường - controlCode**:
    - **Sản Hose**:
        - `P = 0`: ATO
        - `O, R = 1`: Phiên liên tục
        - `I = 2`: Ngưng nghỉ trưa
        - `A = 3`: ATC
        - `C = 4`: PLO = GDTT
        - `K = 5`: Đóng cửa
        - `G`: Sau 15h
    - **Sản HNX và UPCOM**:
        - `P.O = 1`: Phiên liên tục
        - `2 = 2`: Ngưng nghî trưa
        - `A = 3`: ATC
        - `C = 4`: PLO = GDTT
        - `13,97 = 5`: Đóng cửa
- **Mã trạng thái giá - price status**:
    - `0, 3`: Tham chiếu
    - `1`: Trần
    - `2`: Tăng
    - `4`: Sàn
    - `5`: Giảm