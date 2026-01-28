/**
 * @swagger
 * /equity/rights/available:
 *   get:
 *     tags:
 *       - Equity Rights
 *     summary: Query Available Right of 1 account
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
 *         name: rightType
 *         description: right type to query
 *         schema:
 *           type: string
 *           enum:
 *             - ADDITIONAL_STOCK
 *             - BOND
 *       - in: query
 *         name: lastSequenceNumber
 *         description: the last sequence number from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastStockCode
 *         description: the last stock code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastBaseDate
 *         description: the last base date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
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
 *         description: Available Right
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityRightAvailableResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/rights/others:
 *   get:
 *     tags:
 *       - Equity Rights
 *     summary: Query other Rights of 1 account
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
 *         name: lastStockCode
 *         description: the last stock code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastBaseDate
 *         description: the last base date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastRightType
 *         description: the last right type from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
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
 *         description: Other Rights
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityRightOthersResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/rights/detail:
 *   get:
 *     tags:
 *       - Equity Rights
 *     summary: Query Detail information of 1 Right of 1 account
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
 *         description: sub number to query, default is **00**if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: baseDate
 *         required: true
 *         description: query by the last registration date of right, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: rightType
 *         required: true
 *         description: right type to query
 *         schema:
 *           type: string
 *           enum:
 *             - ADDITIONAL_STOCK
 *             - BOND
 *       - in: query
 *         name: sequenceNumber
 *         required: true
 *         description: the sequence number of this right
 *         schema:
 *           type: integer
 *       - in: query
 *         name: bankCode
 *         description: the code of the bank that using for the deal, default is **9999** if not set
 *         schema:
 *           type: string
 *       - in: query
 *         name: bankName
 *         description: bank name to query cash balance, no need to set if bankCode is **9999**
 *         schema:
 *           type: string
 *       - in: query
 *         name: bankAccount
 *         description: the bank account that using for the deal
 *         schema:
 *           type: string
 *     responses:
 *       '200':
 *         description: Detail Right Info
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityRightDetailResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/rights/register:
 *   post:
 *     tags:
 *       - Equity Rights
 *     summary: Register to purchase right
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityRightRegisterRequest'
 *       description: Right Register object
 *       required: true
 *     responses:
 *       200:
 *         description: Register OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/rights/cancel:
 *   put:
 *     tags:
 *       - Equity Rights
 *     summary: Cancel right purchase
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityRightCancelRequest'
 *       description: Right Cancel object
 *       required: true
 *     responses:
 *       200:
 *         description: Register OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
