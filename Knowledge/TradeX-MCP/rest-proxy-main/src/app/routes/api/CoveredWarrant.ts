/**
 * @swagger
 * /market/cw:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return the list of covered warrant code
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
 *                 $ref: '#/components/schemas/CWItemResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/cw/list:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current data of covered warrant list
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: cwList
 *         in: query
 *         description: the list of covered warrant to query info
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
 *                 $ref: '#/components/schemas/CWInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/cw/{cwCode}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return current price info of 1 covered warrant
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: cwCode
 *         in: path
 *         description: covered warrant code to query info
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/CWInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/cw/{cwCode}/quote:
 *   get:
 *     tags:
 *       - Market
 *     summary: Return quote list of 1 covered warrant
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: cwCode
 *         in: path
 *         description: covered warrant code to query info
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
 *                 $ref: '#/components/schemas/CWQuoteResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/cw/{cwCode}/period/{periodType}:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query daily, weekly, monthly covered warrant data
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: cwCode
 *         in: path
 *         description: cw code to query info
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
 *                 $ref: '#/components/schemas/MarketItemPeriodResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/cw/{cwCode}/ticks:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by tick interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: cwCode
 *         in: path
 *         description: covered warrant code to query info
 *         required: true
 *         schema:
 *           type: string
 *       - name: tickUnit
 *         in: query
 *         description: the tick interval to query info of 1 covered warrant
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
 * /market/cw/{cwCode}/minutes:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query data by minute interval
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: cwCode
 *         in: path
 *         description: covered warrant code to query info
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
