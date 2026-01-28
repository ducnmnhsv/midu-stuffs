/**
 * @swagger
 * /equity/withdraw/banks:
 *   get:
 *     tags:
 *       - Equity Withdraw Money
 *     summary: Query the list of registered bank that can receive money of 1 account
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
 *         description: List of Registered Bank
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityWithdrawBankAccountResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/withdraw/history:
 *   get:
 *     tags:
 *       - Equity Withdraw Money
 *     summary: Query Withdraw History of 1 account
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
 *         name: status
 *         description: the status of the transaction
 *         required: true
 *         schema:
 *           type: string
 *           enum:
 *             - 'PENDING'
 *             - 'CANCELLED'
 *             - 'APPROVED'
 *       - in: query
 *         name: fromDate
 *         description: query withdraw history that implemented after or on this date, format is **yyyyMMdd**, default is **19700101**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toDate
 *         description: query withdraw history that implemented before or on this date, format is **yyyyMMdd**, default is **today**
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
 *         description: List of Withdraw History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityWithdrawHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/withdraw/request:
 *   post:
 *     tags:
 *       - Equity Withdraw Money
 *     summary: Operate money withdraw via Bank Transfer
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityWithdrawRequest'
 *       description: Withdraw Request object
 *       required: true
 *     responses:
 *       200:
 *         description: Request withdraw OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/withdraw/cancel:
 *   put:
 *     tags:
 *       - Equity Withdraw Money
 *     summary: Cancel withdraw money request
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityWithdrawCancelRequest'
 *       description: Withdraw Cancel object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
