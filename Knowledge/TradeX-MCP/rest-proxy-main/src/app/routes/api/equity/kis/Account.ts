/**
 * @swagger
 * /equity/account/changePassword:
 *   put:
 *     tags:
 *       - Equity Account
 *     summary: Change Order password of account
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderPasswordRequest'
 *       description: Change Order Password object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/changeHTSPassword:
 *   put:
 *     tags:
 *       - Equity Account
 *     summary: Change HTS password
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityHTSPasswordRequest'
 *       description: Change HTS Password object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/info:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Query account info
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
 *         name: subNumber
 *         required: true
 *         description: sub number to query
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityAccountInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/banks:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Get the bank info linking to the account and sub number
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
 *         name: subNumber
 *         required: true
 *         description: sub number to query
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityAccountBankResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/mobile:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Get the mobile phone number registered with the account
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
 *               $ref: '#/components/schemas/EquityAccountMobileResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/cashBalance:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Query Cash Balance of Account
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
 *         name: subNumber
 *         description: sub number to query, default is **00** if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: bankCode
 *         description: bank account to query cash balance, default is **9999** if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: bankName
 *         description: bank name to query cash balance, no need to set if bankCode is **9999**
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Cash Balance Information
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityCashBalanceResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/profitLoss:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Query Profit Loss Balance of Account
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
 *         name: bankName
 *         description: bank name to query cash balance, no need to set if bankCode is **9999**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastStockCode
 *         description: the last stock code from previous query used to load more data
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
 *         description: Profit Loss Balance Information
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityProfitLossResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/buyable:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Query Buyable Quantity
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
 *         name: bankName
 *         description: bank name to query cash balance, no need to set if bankCode is **9999**
 *         schema:
 *           type: string
 *       - in: query
 *         name: stockCode
 *         description: query filter by 1 stock
 *         schema:
 *           type: string
 *       - in: query
 *         name: securitiesType
 *         description: the securities type to query info
 *         schema:
 *           type: string
 *           enum:
 *             - STOCK
 *             - FUND
 *             - ETF
 *             - BOND
 *       - in: query
 *         name: marketType
 *         description: the market type to query info
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: orderPrice
 *         required: true
 *         description: using to calculate buyable quantity
 *         schema:
 *           type: number
 *           format: double
 *       - in: query
 *         name: orderQuantity
 *         required: true
 *         description: using to calculate lack amount
 *         schema:
 *           type: integer
 *     responses:
 *       '200':
 *         description: Buyable Quantity Information
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityBuyableQuantityResponse'
 *       '401':
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/sellable:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Query Sellable Stocks
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
 *         name: subNumber
 *         description: sub number to query, default is **00** if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: date
 *         description: query sellable till this value, format is **yyyyMMdd**, default is **today**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastStockCode
 *         description: the last stock code from previous query used to load more data
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
 *         description: Sellable Stock Information
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquitySellableStockResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/account/dailyProfit:
 *   get:
 *     tags:
 *       - Equity Account
 *     summary: Query Daily Profit of each sub account
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
 *         name: subNumber
 *         description: sub number to query, default is **00** if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: baseDate
 *         description: query daily profit till this value, format is **yyyyMMdd**, default is **today**
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
 *         description: Daily Profit Information
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityDailyProfitResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
