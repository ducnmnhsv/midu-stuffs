/**
 * @swagger
 * /derivatives/account/summary:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Derivatives Account Summary info
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesAccountSummaryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/account/equity:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Usable Equity Asset For Derivatives
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesAccountEquityResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/account/balance:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Query derivatives daily account balance
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
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesAccountBalanceResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/account/profitLoss:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Query derivatives account profit loss
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
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesAccountProfitLossResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/account/profitLoss/cumulative:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Query derivatives account cumulative profit loss
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
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesAccountCumulativeProfitLossResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/account/riskRatio:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Query derivatives account risk ratio
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: accountNumber
 *         required: true
 *         description: account number to query
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesAccountRiskRatioResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/account/openPosition:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Query derivatives account open position of today
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
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesAccountOpenPositionResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/account/tradingLimit:
 *   get:
 *     tags:
 *       - Derivatives Account
 *     summary: Query derivatives account trading limit
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
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesAccountTradingLimitResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
