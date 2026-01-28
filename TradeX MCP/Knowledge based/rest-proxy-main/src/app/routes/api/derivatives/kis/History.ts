/**
 * @swagger
 * /derivatives/history/trade:
 *   get:
 *     tags:
 *       - Derivatives History
 *     summary: Query Derivatives Trade History
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
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
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Derivatives Trade History
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesTradeHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/history/position:
 *   get:
 *     tags:
 *       - Derivatives History
 *     summary: Query Derivatives Position History
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
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
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Position History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesPositionHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/history/marginCall:
 *   get:
 *     tags:
 *       - Derivatives History
 *     summary: Query Derivatives Margin Call History
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
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
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Margin Call History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesMarginCallHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/history/settlement:
 *   get:
 *     tags:
 *       - Derivatives History
 *     summary: Query Derivatives Settlement History
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
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
 *         name: lastTradingDate
 *         description: the last trading date from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastSettleDate
 *         description: the last settle date from previous query used to load more data
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
 *         description: Settlement History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesSettlementHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/history/closedPosition:
 *   get:
 *     tags:
 *       - Derivatives History
 *     summary: Query Derivatives Closed Position History
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
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
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Derivatives Trade Closed Position History
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesClosedPositionResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
