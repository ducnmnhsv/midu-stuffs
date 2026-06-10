# VietStock API Gap Analysis — TradeX vs VietStock Demo Spec

**Document Status:** Draft  
**For:** BA/PM, Backend Team  
**Next Steps:** Confirm missing APIs với team, prioritize implementation roadmap

---

## Tóm tắt

Tài liệu này so sánh toàn bộ **40 VietStock APIs** trong spec `_API_VietStock_Demo_Document_V1.docx` với những gì TradeX hiện đang implement. Kết quả:

| Trạng thái | Số lượng |
|-----------|---------|
| ✅ TradeX đang dùng từ VietStock | 1 API |
| 🔄 TradeX có equivalent (từ Lotte/internal) | ~20 API |
| ❓ Có thể dùng VietStock (chưa xác nhận source) | 6 API |
| ❌ Chưa có trong TradeX | 27+ API |

---

## Phần I — API TradeX đang thực sự dùng từ VietStock

### ✅ 1. Chart History (OHLCV) — `/ta/history`

**VietStock endpoint (production):** `https://api.vietstock.vn/ta/history`  
**VietStock endpoint (demo):** `https://api-demo.vietstock.vn/demo/history`

**Mô tả:** Dữ liệu lịch sử giá (Open/High/Low/Close/Volume) theo khung thời gian.

**TradeX implementation:**
- File: `market-query-v2-main/src/services/CrawlDataService.ts` + `market-query-v2-main/src/config.ts:85`
- Config: `url: 'https://api.vietstock.vn/ta/history?symbol={code}&resolution={resolution}&from={from}&to={to}'`
- Dùng để **crawl dữ liệu chart** cho tất cả symbol: cổ phiếu, chỉ số (VNINDEX, HNXINDEX, UPCOMINDEX), phái sinh (VN30F1M, VN30F2M, VN30F1Q, VN30F2Q), chứng quyền
- Data được lưu vào MongoDB (`symbol_daily` collection), phục vụ TradingView chart

**Coverage:**
- Covers: `II.1 Deal history index`, `III.1 Deal history stock`, `IV.5 Deal history derivative`, `V.7 Deal history CW`

---

## Phần II — TradeX có Equivalent (từ Lotte, không phải VietStock)

TradeX lấy dữ liệu thị trường **real-time từ Lotte Securities** (qua `market-collector-lotte` → Kafka → `realtime-v2`). Các API dưới đây có equivalent trong TradeX nhưng **không dùng VietStock**.

### 🔄 Stock Market (Section I của VietStock)

| VietStock API | VietStock Endpoint | TradeX Equivalent |
|--------------|-------------------|------------------|
| Market trading result (Chỉ số thị trường) | `/exchangehistory` | `/market/index/{indexCode}/period/{periodType}` |
| Tick data (Khớp lệnh theo lô) | `/stockdeals` | `/market/stock/{stockCode}/quote` |
| Order history (Lịch sử đặt lệnh) | `/orderhistory` | `/market/stock/{stockCode}/period/{periodType}` |
| Stock best price (Giá tốt nhất 3 giá) | `/stockbestprice` | `/market/stock/{stockCode}` (bid/offer included) |
| Stock Trading Info | `/stocktrading` | `/market/stock/{stockCode}` |
| Foreign trading history (Nước ngoài) | `/foreignhistory` | `/market/stock/{stockCode}/foreigner` |

### 🔄 Master Index (Section II của VietStock — chart phần)

| VietStock API | VietStock Endpoint | TradeX Equivalent |
|--------------|-------------------|------------------|
| Deal history index (OHLCV) | `/history` (symbol=VNINDEX) | ✅ VietStock `/ta/history` (via CrawlDataService) |

### 🔄 Master Stock (Section III của VietStock — chart phần)

| VietStock API | VietStock Endpoint | TradeX Equivalent |
|--------------|-------------------|------------------|
| Deal history stock (OHLCV) | `/history` (symbol=VNM) | ✅ VietStock `/ta/history` (via CrawlDataService) |

### 🔄 Futures (Section IV của VietStock)

| VietStock API | VietStock Endpoint | TradeX Equivalent |
|--------------|-------------------|------------------|
| List Futures | `/derivatives` | `/market/derivatives` (from Lotte) |
| Futures Info | `/derivativeinfo` | `/market/derivatives/{code}` |
| Trading Data Derivative | `/derivativetrading` | `/market/derivatives/{code}/period/{periodType}` |
| Derivative intraday | `/derivativedeals` | `/market/derivatives/{code}/quote` |
| Deal history derivative (OHLCV) | `/history` (symbol=VN30F) | ✅ VietStock `/ta/history` (via CrawlDataService) |

### 🔄 Covered Warrant (Section V của VietStock)

| VietStock API | VietStock Endpoint | TradeX Equivalent |
|--------------|-------------------|------------------|
| List CW | `/cwlist` | `/market/cw` (from Lotte) |
| CW Info | `/cwinfo` | `/market/cw/{cwCode}` |
| CW Trading | `/cwtrading` | `/market/cw/{cwCode}/period/{periodType}` |
| Tick data CW | `/stockdeals` | `/market/cw/{cwCode}/quote` |
| Deal history CW (OHLCV) | `/history` (symbol=CW) | ✅ VietStock `/ta/history` (via CrawlDataService) |

---

## Phần III — Có thể dùng VietStock (chưa xác nhận source)

Các route này **có trong rest-proxy**, nhưng không tìm thấy source code service tương ứng trong Knowledge base. Khả năng cao đang gọi VietStock thông qua `business-info` Kafka topic.

| TradeX Route | Mô tả | VietStock Equivalent |
|-------------|-------|---------------------|
| `GET /businessInfo` | Thông tin tài chính công ty | `/financeinfo` |
| `GET /businessInfo/year` | Tài chính theo năm | `/financeinfo` (termtype=N) |
| `GET /businessInfo/quarter` | Tài chính theo quý | `/financeinfo` (termtype=Q) |
| `GET /news` | Tin tức mới nhất | `/latestnews`, `/news` |
| `GET /news/filter` | Tìm kiếm tin tức | `/stocknews`, `/GetNews` |
| `GET /news/announcement` | Công bố thông tin | `/AnnouncedInformation` |

> **Action cần thiết:** Confirm với Backend team xem `/businessInfo` và `/news` đang gọi VietStock hay source khác.

---

## Phần IV — ❌ MISSING: Chưa có trong TradeX

Đây là danh sách VietStock APIs **hoàn toàn chưa được tích hợp** vào TradeX.

### 📊 Master Index — Thống kê chỉ số (Section II)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 1 | Trading Market / Index Stats | `/GetDetailIndex` | PE, PB, EPS, ROA, ROE, +/- 1W/1M/3M/6M/52W của chỉ số | Màn hình Market Overview |
| 2 | Top Stocks Affect The Market Index | `/topstockinfluence` | Cổ phiếu tác động nhiều nhất lên chỉ số (tăng/giảm) | Widget "Cổ phiếu kéo thị trường" |
| 3 | VS Sector Index | `/sectorindex` | Hiệu suất theo ngành VS | Màn hình Market Overview - Heat map ngành |
| 4 | List Stocks in VS Sector | `/GetListStockBySector` | Danh sách cổ phiếu theo ngành | Filter cổ phiếu theo ngành |
| 5 | VS Sector Market / Stats | `/GetDetailSector` | PE, PB, MarketCap, ROA/ROE theo ngành | Phân tích ngành |

### 📈 Master Stock — Thống kê cổ phiếu (Section III)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 6 | Margin Ratio | `/tradingmargin` | Tỷ lệ ký quỹ từ các CTCK | Hiển thị margin rate trên stock detail |
| 7 | Trading Status | `/tradingstatus` | IsMargin, IsFTSE, IsVNMETF, IsVN30, IsHNX30 | Badge trên stock detail |
| 8 | Stock Statistics | `/statistic` | 52-week high/low, avg volume W/M/Q/Y, thống kê theo tháng/quý/năm | Stock detail - thống kê |
| 9 | Valuation Ratios | `/stocktradinginfo` | PE, PB, EPS, FEPS, BVPS, Beta, MarketCap, Dividend, Yield | Stock detail - định giá |
| 10 | Valuation Ratios by Exchange | `/stocktradinginfobycatid` | Như trên nhưng filter theo sàn (HOSE/HNX/UPCoM/VN30/HNX30) | Screener cổ phiếu |

### 🏢 Company Information (Section VI)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 11 | Company Information | `/companyinfo` | Thông tin công ty: tên, địa chỉ, lĩnh vực, ban lãnh đạo tóm tắt, links | Stock detail - hồ sơ công ty |
| 12 | Shareholder | `/stockshareholders` | Cổ đông lớn (tên, tỷ lệ) | Stock detail - cổ đông lớn |
| 13 | Labor Structure | `/stocklaborstructure` | Cơ cấu lao động | Stock detail - về công ty |
| 14 | Board of Management | `/stockboardofmanagement` | Ban lãnh đạo (tên, chức vụ, kinh nghiệm) | Stock detail - ban lãnh đạo |
| 15 | Associate | `/stockassociate` | Công ty con và liên kết | Stock detail - về công ty |
| 16 | Ownership | `/stockownership` | Cơ cấu sở hữu (cổ đông nhà nước, nước ngoài, khác) | Stock detail - cổ đông |
| 17 | Charter Capital | `/stockchartercapital` | Lịch sử thay đổi vốn điều lệ | Stock detail - về công ty |
| 18 | Stock Document | `/stockdocument` | Tài liệu cổ đông (BCTC, BCCB, Nghị quyết ĐHCĐ) | Stock detail - tài liệu |
| 19 | Document Type | `/documenttype` | Danh mục loại tài liệu | Dropdown filter tài liệu |

### 💰 Fundamentals (Section VII)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 20 | Financial Plan | `/ctkhinfo` | Chỉ tiêu kế hoạch doanh thu, lợi nhuận, cổ tức | Stock detail - kế hoạch tài chính |

### 📰 News (Section VIII — thiếu)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 21 | News by Channel/Sector | `/NewsByChannel` | Tin tức theo ngành (channel ID) | Trang tin tức ngành |
| 22 | News Same Branch | `/NewsSameBranch` | Tin tức cùng ngành với một mã | Stock detail - tin tức liên quan |

### 📅 Events (Section IX)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 23 | Stock Events | `/stockevents` | Sự kiện doanh nghiệp: cổ tức, phát hành, ĐHCĐ, ngày ĐKCC | Stock detail - sự kiện / Calendar |
| 24 | Internal Trading | `/transferdata` | Giao dịch nội bộ: ban lãnh đạo mua/bán cổ phiếu | Stock detail - giao dịch nội bộ |

### 🌍 Covered Warrant — Thiếu tính năng (Section V)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 25 | CW Relate | `/cwrelate` | Chứng quyền liên quan cùng cổ phiếu cơ sở | CW detail - CW liên quan |
| 26 | Black Scholes | `/BlackSchole` | Định giá lý thuyết CW (Fair Value) | CW detail - định giá |

### 🌐 Macro & Global Markets (không có section riêng trong doc)

| # | API | VietStock Endpoint | Mô tả | Potential Use Case |
|---|-----|-------------------|-------|-------------------|
| 27 | World Index History | `/indexquotehistory` | Lịch sử chỉ số thế giới (Dow Jones, S&P500, ...) | Màn hình thị trường thế giới |
| 28 | World Index Quote | `/indexquote` | Chỉ số thế giới theo ngày | Market Overview - global |
| 29 | World Index Current | `/quote` | Giá hiện tại chỉ số thế giới | Market Overview - global |
| 30 | Interbank Interest Rates | `/interbankinterestrates` | Lãi suất liên ngân hàng (VNIBOR) | Macro data |
| 31 | Currency History | `/currencyhistory` | Lịch sử tỷ giá (USD/VND, THB/VND, ...) | Macro data - tỷ giá |
| 32 | Gold Price History | `/xauhistory` | Lịch sử giá vàng | Macro data - giá vàng |
| 33 | Goods Price | `/goodsprice` | Giá hàng hóa (dầu, vàng, bạc, ...) | Macro data - hàng hóa |
| 34 | Current Currency | `/getcurrency` | Tỷ giá hiện tại | Macro data - tỷ giá |
| 35 | Macro Overview | `/macrooverview` | Tổng quan kinh tế vĩ mô | Dashboard macro |
| 36 | Top Trading | `/toptrading` | Top mua/bán nước ngoài, top tăng/giảm theo ngành | Market Ranking |
| 37 | Top Stock Financial | `/topstockfin` | Top cổ phiếu theo PE, PB thấp/cao nhất | Screener / Top Picks |

---

## Phần V — Tổng hợp & Phân loại ưu tiên

### Priority Matrix

| Priority | API Group | Lý do |
|----------|-----------|-------|
| 🔴 **P1 — High Impact** | Stock Statistics, Valuation Ratios, Stock Events | Core features cho Stock Detail screen — FE có thể cần ngay |
| 🔴 **P1 — High Impact** | Company Info, Shareholders, Board of Management | Hồ sơ công ty — cơ bản của mọi app chứng khoán |
| 🟡 **P2 — Medium** | Sector Index, VS Sector Market, Top Stocks Influence | Market Overview screen enhancement |
| 🟡 **P2 — Medium** | CW Black Scholes, CW Relate | CW feature completeness |
| 🟢 **P3 — Low** | Macro data (Currency, Gold, Goods) | Nice-to-have, không core |
| 🟢 **P3 — Low** | Internal Trading, Labor Structure | Niche use cases |

### APIs nên confirm với Backend

1. `/businessInfo*` → Đang gọi VietStock `/financeinfo` hay source khác?
2. `/news*` → Đang gọi VietStock news APIs hay source khác?
3. TradingView chart → Đang dùng VietStock `/ta/history` hay đã migrate sang Lotte?

---

## Phần VI — Mapping đầy đủ VietStock → TradeX

| # | Section | VietStock API | VietStock Endpoint | TradeX Status | TradeX Route |
|---|---------|--------------|-------------------|--------------|-------------|
| 1 | I | Market trading result | `/exchangehistory` | 🔄 Lotte | `/market/index/{code}/period/{type}` |
| 2 | I | Tick data | `/stockdeals` | 🔄 Lotte | `/market/stock/{code}/quote` |
| 3 | I | Order history | `/orderhistory` | 🔄 Lotte | `/market/stock/{code}/period/{type}` |
| 4 | I | Stock best price | `/stockbestprice` | 🔄 Lotte | `/market/stock/{code}` |
| 5 | I | Stock Trading Info | `/stocktrading` | 🔄 Lotte | `/market/stock/{code}` |
| 6 | I | Foreign trading | `/foreignhistory` | 🔄 Lotte | `/market/stock/{code}/foreigner` |
| 7 | II | Deal history index (OHLCV) | `/history` | ✅ VietStock `/ta/history` | CrawlDataService → TradingView |
| 8 | II | Index statistics (PE/PB/ROE) | `/GetDetailIndex` | ❌ Missing | — |
| 9 | II | Top stocks influence | `/topstockinfluence` | ❌ Missing | — |
| 10 | II | VS Sector Index | `/sectorindex` | ❌ Missing | — |
| 11 | II | Stocks by Sector | `/GetListStockBySector` | ❌ Missing | — |
| 12 | II | Sector Market stats | `/GetDetailSector` | ❌ Missing | — |
| 13 | III | Deal history stock (OHLCV) | `/history` | ✅ VietStock `/ta/history` | CrawlDataService → TradingView |
| 14 | III | Margin Ratio | `/tradingmargin` | ❌ Missing | — |
| 15 | III | Trading Status | `/tradingstatus` | ❌ Missing | — |
| 16 | III | Stock Statistics | `/statistic` | ❌ Missing | — |
| 17 | III | Valuation Ratios | `/stocktradinginfo` | ❌ Missing | — |
| 18 | III | Valuation Ratios by CatId | `/stocktradinginfobycatid` | ❌ Missing | — |
| 19 | IV | List Futures | `/derivatives` | 🔄 Lotte | `/market/derivatives` |
| 20 | IV | Futures Info | `/derivativeinfo` | 🔄 Lotte | `/market/derivatives/{code}` |
| 21 | IV | Trading Data Derivative | `/derivativetrading` | 🔄 Lotte | `/market/derivatives/{code}/period` |
| 22 | IV | Derivative intraday | `/derivativedeals` | 🔄 Lotte | `/market/derivatives/{code}/quote` |
| 23 | IV | Deal history derivative (OHLCV) | `/history` | ✅ VietStock `/ta/history` | CrawlDataService → TradingView |
| 24 | V | List CW | `/cwlist` | 🔄 Lotte | `/market/cw` |
| 25 | V | CW Info | `/cwinfo` | 🔄 Lotte | `/market/cw/{code}` |
| 26 | V | CW Trading | `/cwtrading` | 🔄 Lotte | `/market/cw/{code}/period` |
| 27 | V | CW Relate | `/cwrelate` | ❌ Missing | — |
| 28 | V | Black Scholes | `/BlackSchole` | ❌ Missing | — |
| 29 | V | Tick data CW | `/stockdeals` | 🔄 Lotte | `/market/cw/{code}/quote` |
| 30 | V | Deal history CW (OHLCV) | `/history` | ✅ VietStock `/ta/history` | CrawlDataService → TradingView |
| 31 | VI | Company Information | `/companyinfo` | ❌ Missing | — |
| 32 | VI | Shareholder | `/stockshareholders` | ❌ Missing | — |
| 33 | VI | Labor Structure | `/stocklaborstructure` | ❌ Missing | — |
| 34 | VI | Board of Management | `/stockboardofmanagement` | ❌ Missing | — |
| 35 | VI | Associate | `/stockassociate` | ❌ Missing | — |
| 36 | VI | Ownership | `/stockownership` | ❌ Missing | — |
| 37 | VI | Charter Capital | `/stockchartercapital` | ❌ Missing | — |
| 38 | VI | Stock Document | `/stockdocument` | ❌ Missing | — |
| 39 | VI | Document Type | `/documenttype` | ❌ Missing | — |
| 40 | VII | Financial Info | `/financeinfo` | ❓ Likely via `/businessInfo` | Confirm needed |
| 41 | VII | Financial Plan | `/ctkhinfo` | ❌ Missing | — |
| 42 | VIII | News by ID | `/article` | ❓ Likely via `/news` | Confirm needed |
| 43 | VIII | News by Stock | `/stocknews` | ❓ Likely via `/news/filter` | Confirm needed |
| 44 | VIII | News by Category/Date | `/news` | ❓ Likely via `/news` | Confirm needed |
| 45 | VIII | Latest News | `/latestnews` | ❓ Likely via `/news/top` | Confirm needed |
| 46 | VIII | News by Category (slug) | `/newsbycategory` | ❓ Likely via `/news/filter` | Confirm needed |
| 47 | VIII | Announcement | `/AnnouncedInformation` | ❓ Likely via `/news/announcement` | Confirm needed |
| 48 | VIII | News by Channel/Sector | `/NewsByChannel` | ❌ Missing | — |
| 49 | VIII | News Same Branch | `/NewsSameBranch` | ❌ Missing | — |
| 50 | IX | Stock Events | `/stockevents` | ❌ Missing | — |
| 51 | IX | Internal Trading | `/transferdata` | ❌ Missing | — |

---

## Ghi chú kỹ thuật

**VietStock Demo vs Production:**
- Demo base: `https://api-demo.vietstock.vn/demo/`
- Production chart: `https://api.vietstock.vn/ta/history` (TradingView-compatible format)
- Production (các API khác): Cần confirm URL production với VietStock

**TradeX Architecture:**
```
VietStock /ta/history ──► CrawlDataService ──► MongoDB ──► TradingView chart
Lotte Securities WS   ──► market-collector ──► Kafka ──► realtime-v2 ──► REST/WS APIs
? (business-info)     ──► market-query-v2  ──► Redis ──► /businessInfo, /news
```
