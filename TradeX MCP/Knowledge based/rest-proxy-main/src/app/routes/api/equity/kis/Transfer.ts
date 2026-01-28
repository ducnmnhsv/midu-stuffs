/**
 * @swagger
 * /equity/transfer/cash/history:
 *   get:
 *     tags:
 *       - Equity Transfer
 *     summary: Query history of cash transfering between main/sub
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
 *         name: type
 *         description: the type of the transaction (btw main/sub or different account number)
 *         required: true
 *         schema:
 *           type: string
 *           enum:
 *             - 'INTERNAL'
 *             - 'EXTERNAL'
 *       - in: query
 *         name: status
 *         description: the status of the transaction (only used when type is EXTERNAL)
 *         schema:
 *           type: string
 *           enum:
 *             - 'PENDING'
 *             - 'CANCELLED'
 *             - 'APPROVED'
 *       - in: query
 *         name: fromDate
 *         description: query transfer history that implemented after or on this date, format is **yyyyMMdd**, default is **19700101**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toDate
 *         description: query transfer history that implemented before or on this date, format is **yyyyMMdd**, default is **today**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastTransactionDate
 *         description:  the last transaction date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastTransferSequenceNumber
 *         description:  the last transfer sequence number from previous query used to load more data
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
 *         description: List of Cash Transfer History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityCashTransferHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/transfer/cash/account:
 *   get:
 *     tags:
 *       - Equity Transfer
 *     summary: Query list of internal accounts to transfer cash
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
 *     responses:
 *       200:
 *         description: List of Internal Account to be able to transfer cash
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityCashTransferAccountResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/transfer/cash:
 *   post:
 *     tags:
 *       - Equity Transfer
 *     summary: Operate cash transfer between main/sub or 2 different accounts
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityCashTransferRequest'
 *       description: Cash Transfer Request object
 *       required: true
 *     responses:
 *       200:
 *         description: Request Transfer OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/transfer/cash/cancel:
 *   put:
 *     tags:
 *       - Equity Transfer
 *     summary: Cancel cash transfer request between 2 different accounts
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityCashCancelRequest'
 *       description: Cash Transfer Cancel Request object
 *       required: true
 *     responses:
 *       200:
 *         description: Request Cancel OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/transfer/stock/balance:
 *   get:
 *     tags:
 *       - Equity Transfer
 *     summary: Query Stock Transfer Balance of Account
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
 *         description: Stock Transfer Balance Information
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityStockTransferBalanceResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/transfer/stock/history:
 *   get:
 *     tags:
 *       - Equity Transfer
 *     summary: Query history of stock transfering between main/sub
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
 *         name: receivedSubNumber
 *         description: the sub number that receive stock
 *         schema:
 *           type: string
 *       - in: query
 *         name: stockCode
 *         description: the transfering stock code
 *         schema:
 *           type: string
 *       - in: query
 *         name: fromDate
 *         description: query transfer history that implemented after or on this date, format is **yyyyMMdd**, default is **19700101**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toDate
 *         description: query transfer history that implemented before or on this date, format is **yyyyMMdd**, default is **today**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastTransactionDate
 *         description:  the last transaction date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastSequenceNumber
 *         description:  the last sequence number from previous query used to load more data
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
 *         description: List of Stock Transfer History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityStockTransferHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/transfer/stock:
 *   post:
 *     tags:
 *       - Equity Transfer
 *     summary: Operate stock transfer between main/sub
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityStockTransferRequest'
 *       description: Stock Transfer Request object
 *       required: true
 *     responses:
 *       200:
 *         description: Request Transfer OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
