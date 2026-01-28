/**
 * @swagger
 * /market/futures:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return the list of Futures
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
 *       - in: query
 *         name: futuresCode
 *         description: the base futures code used to query the next data, sorting by alphabet
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
 *                 $ref: '#/components/schemas/FuturesResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/futures/list:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current data of a list of futures
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: futuresList
 *         in: query
 *         description: the list of futures code to query info
 *         schema:
 *           type: array
 *           items:
 *             type: string
 *       - in: query
 *         name: futuresCode
 *         description: the base futures code used to query the next data in the futures list
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
 *                 $ref: '#/components/schemas/FuturesInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/futures/{futuresCode}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current data of a futures
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         name: futuresCode
 *         description: the base futures code used to query the next data in the futures list
 *         schema:
 *           type: string
 *         required: true
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/FuturesInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/futures/{futuresCode}/quote:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return quote list of 1 futures
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: futuresCode
 *         in: path
 *         description: futures code to query info
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
 *                 $ref: '#/components/schemas/FuturesQuoteResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/futures/{futuresCode}/period/{periodType}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query daily, weekly, monthly futures data
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: futuresCode
 *         in: path
 *         description: futures code to query info
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
 *       '200':
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/MarketFuturesPeriodResponse'
 *       '401':
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/futures/{futuresCode}/ticks:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by tick interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: futuresCode
 *         in: path
 *         description: futures code to query info
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
 * /market/futures/{futuresCode}/minutes:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by minute interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: futuresCode
 *         in: path
 *         description: futures code to query info
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
 * /market/futures/{futuresCode}/foreigner:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query trade & hold data of foreigner
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: futuresCode
 *         in: path
 *         description: futures code to query info
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
 *                 $ref: '#/components/schemas/MarketFuturesForeignerDataResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
