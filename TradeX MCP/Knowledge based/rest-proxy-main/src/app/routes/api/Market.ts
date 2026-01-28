/**
 * @swagger
 * /market/sessionStatus:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return the list of market session status
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: market
 *         description: the market to query
 *         schema:
 *           type: string
 *           enum:
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: type
 *         description: the type to query info
 *         schema:
 *           type: string
 *           enum:
 *             - EQUITY
 *             - DERIVATIVES
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketSessionStatusResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/index:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return the list of index code
 *     security:
 *       - jwt: []
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/IndexItemResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/index/list:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current data of a list of index
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: indexList
 *         in: query
 *         description: the list of index code to query info
 *         schema:
 *           type: array
 *           items:
 *             type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/IndexQuoteResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/index/{indexCode}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current info of 1 index
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: indexCode
 *         in: path
 *         description: index code to query info
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/IndexInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/index/{indexCode}/quote:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return quote list of 1 index
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: indexCode
 *         in: path
 *         description: index code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: baseTime
 *         description: the baseTime that used to query the next data. format is **hhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/QuoteResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/index/{indexCode}/period/{periodType}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query daily, weekly, monthly index data
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: indexCode
 *         in: path
 *         description: index code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - name: periodType
 *         in: path
 *         description: the period type to query info
 *         required: true
 *         schema:
 *           type: string
 *           enum:
 *             - DAILY
 *             - WEEKLY
 *             - MONTHLY
 *       - in: query
 *         name: baseDate
 *         description: the date to query the period data, default is **today**, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketIndexPeriodResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/index/{indexCode}/ticks:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by tick interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: indexCode
 *         in: path
 *         description: index code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - name: tickUnit
 *         in: query
 *         description: the tick interval to query info
 *         required: true
 *         schema:
 *           type: integer
 *           enum:
 *             - '1'
 *             - '5'
 *             - '10'
 *             - '30'
 *       - in: query
 *         name: sequence
 *         description: the sequence of the data that used to query next
 *         schema:
 *           type: number
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketItemIntervalResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/index/{indexCode}/minutes:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by minute interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: indexCode
 *         in: path
 *         description: index code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - name: minuteUnit
 *         in: query
 *         description: the minute interval to query info
 *         required: true
 *         schema:
 *           type: integer
 *           enum:
 *             - '1'
 *             - '5'
 *             - '10'
 *             - '30'
 *       - in: query
 *         name: fromTime
 *         description: the start time that used to query the data. format is **yyyyMMddhhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toTime
 *         description: the end time that used to query the data. format is **yyyyMMddhhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketItemIntervalResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/list:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current data of a list of stock
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockList
 *         in: query
 *         description: the list of stock code to query info
 *         schema:
 *           type: array
 *           items:
 *             type: string
 *       - in: query
 *         name: stockCode
 *         description: the base stock code used to query the next data in the stock list
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/StockInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/listed:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return the list of Listed Stock
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: marketType
 *         description: the market to query summary info
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: securitiesType
 *         description: the securities type to query summary info
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - STOCK
 *             - FUND
 *             - ETF
 *       - in: query
 *         name: stockCode
 *         description: the base stock code used to query the next data, sorting by alphabet
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/ListedStockResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/{stockCode}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current price info of 1 stock
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockCode
 *         in: path
 *         description: stock code to query info
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/StockPriceResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/{stockCode}/quote:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return quote list of 1 stock
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockCode
 *         in: path
 *         description: stock code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: baseTime
 *         description: the baseTime that used to query the next data. format is **hhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: sequence
 *         description: the last sequence number that used to query the next data
 *         schema:
 *           type: number
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/StockQuoteResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/{stockCode}/period/{periodType}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query daily, weekly, monthly stock data
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockCode
 *         in: path
 *         description: stock code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - name: periodType
 *         in: path
 *         description: the period type to query info
 *         required: true
 *         schema:
 *           type: string
 *           enum:
 *             - DAILY
 *             - WEEKLY
 *             - MONTHLY
 *       - in: query
 *         name: baseDate
 *         description: the date to query the period data, default is **today**, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - name: isAdjusted
 *         in: query
 *         description: the price data is adjusted or not
 *         schema:
 *           type: boolean
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketStockPeriodResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/{stockCode}/ticks:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by tick interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockCode
 *         in: path
 *         description: stock code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - name: tickUnit
 *         in: query
 *         description: the tick interval to query info
 *         required: true
 *         schema:
 *           type: integer
 *           enum:
 *             - '1'
 *             - '5'
 *             - '10'
 *             - '30'
 *       - in: query
 *         name: sequence
 *         description: the sequence of the data that used to query next
 *         schema:
 *           type: number
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**'
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketItemIntervalResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/{stockCode}/minutes:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by minute interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockCode
 *         in: path
 *         description: stock code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - name: minuteUnit
 *         in: query
 *         description: the minute interval to query info
 *         required: true
 *         schema:
 *           type: integer
 *           enum:
 *             - '1'
 *             - '5'
 *             - '10'
 *             - '30'
 *       - in: query
 *         name: fromTime
 *         description: the start time that used to query the data. format is **yyyyMMddhhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toTime
 *         description: the end time that used to query the data. format is **yyyyMMddhhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketItemIntervalResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/{stockCode}/foreigner:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query trade & hold data of foreigner
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockCode
 *         in: path
 *         description: stock code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: baseDate
 *         description: the date to query the period data, default is **today**, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/ForeignerHoldResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/ranking/upDown:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query up/down stock ranking
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: marketType
 *         required: true
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: upDownType
 *         required: true
 *         description: the type of the query
 *         schema:
 *           type: string
 *           enum:
 *             - UP
 *             - DOWN
 *       - in: query
 *         name: fromDate
 *         description: the start date to query the data, default is **the beginning of today**, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toDate
 *         description: the end date to query the data, default is **today**, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: offset
 *         description: the offset position used to query next data
 *         schema:
 *           type: number
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MarketStockRankingUpDownResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/stock/ranking/trade:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query today top trade stock ranking
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: marketType
 *         required: true
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: sortType
 *         required: true
 *         description: the sort type of the query
 *         schema:
 *           type: string
 *           enum:
 *             - TURNOVER_RATE
 *             - TRADING_VOLUME
 *             - TRADING_VALUE
 *       - in: query
 *         name: offset
 *         description: the offset position used to query next data
 *         schema:
 *           type: number
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketStockRankingTopTradeResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/putthrough/advertise:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query the list of put-through advertisement
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: marketType
 *         description: the market to query info
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: offset
 *         description: the offset position used to query next data
 *         schema:
 *           type: number
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/PutThroughAdvertiseResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/putthrough/deal:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query the list of put-through deal
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: marketType
 *         description: the market to query info
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: offset
 *         description: the offset position used to query next data
 *         schema:
 *           type: number
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/PutThroughDealResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/etf/{etfCode}/nav/daily:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query the NAV daily data of 1 ETF
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: etfCode
 *         in: path
 *         description: ETF code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: baseDate
 *         description: the date to query the period data, default is **today**, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/ETFDailyResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/etf/{etfCode}/index/daily:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query the Index daily data of 1 ETF
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: etfCode
 *         in: path
 *         description: ETF code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: baseDate
 *         description: the date to query the period data, default is **today**, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/ETFDailyResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
