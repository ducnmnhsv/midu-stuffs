/**
 * @swagger
 * /equity/order/stop:
 *   post:
 *     tags:
 *       - Equity Order
 *     summary: Place Stop or Stop Limit Order
 *     security:
 *       - jwt: []
 *       - secToken: []
 *       - secDomain: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/OrderStopRequest'
 *       description: Order Stop object
 *       required: true
 *     responses:
 *       '200':
 *         description: Place Order Successfully
 *       '401':
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/stop/modify:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Modify Stop or Stop Limit Order
 *     security:
 *       - jwt: []
 *       - secToken: []
 *       - secDomain: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/OrderStopModifyRequest'
 *       description: Order Stop object
 *       required: true
 *     responses:
 *       '200':
 *         description: Modify Stop Order Successfully
 *       '401':
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/stop/cancel:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Cancel Stop or Stop Limit Order
 *     security:
 *       - jwt: []
 *       - secToken: []
 *       - secDomain: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/OrderStopCancelRequest'
 *       description: Cancel Stop Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel Stop Order Successfully
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/stop/cancel/all:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Cancel Multiple Stop or Stop Limit Order
 *     security:
 *       - jwt: []
 *       - secToken: []
 *       - secDomain: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/OrderStopCancelAllRequest'
 *       description: Cancel All Stop Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel All Stop Orders Successfully
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/stop/history:
 *   get:
 *     tags:
 *       - Equity Order
 *     summary: Query Stop Order History
 *     security:
 *       - jwt: []
 *       - secToken: []
 *       - secDomain: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
 *       - in: query
 *         name: subNumber
 *         description: sub number to query, default is **00** if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: bankCode
 *         description: bank code to query, default is **9999** if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: bankAccount
 *         description: bank account to query
 *         schema:
 *           type: string
 *       - in: query
 *         name: fromDate
 *         description: query order that place after or on this date, format is **yyyyMMdd**, default is **19700101**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toDate
 *         description: query order that place before or on this date, format is **yyyyMMdd**, default is **today**
 *         schema:
 *           type: string
 *       - in: query
 *         name: stockCode
 *         description: query filter by 1 stock
 *         schema:
 *           type: string
 *       - in: query
 *         name: sellBuyType
 *         description: query filter by buy or sell type
 *         schema:
 *           type: string
 *           enum:
 *             - SELL
 *             - BUY
 *       - in: query
 *         name: status
 *         description: query filter by status of stop order
 *         schema:
 *           type: string
 *           enum:
 *             - PENDING
 *             - COMPLETED
 *             - CANCELLED
 *             - FAILED
 *       - in: query
 *         name: orderType
 *         description: query filter by order type
 *         schema:
 *           type: string
 *           enum:
 *             - STOP
 *             - STOP_LIMIT
 *       - in: query
 *         name: sequence
 *         description: the sequence number of the last record, using to query next data
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
 *         description: Stop Order History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/OrderStopHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
