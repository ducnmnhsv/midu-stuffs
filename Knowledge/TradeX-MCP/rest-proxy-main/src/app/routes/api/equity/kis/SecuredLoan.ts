/**
 * @swagger
 * /equity/loan/banks:
 *   get:
 *     tags:
 *       - Equity Secured Loan
 *     summary: Get list of loan bank
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: lastBankCode
 *         description: the last bank code from previous query used to load more data
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
 *         description: Available Loan Bank
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityLoanBankResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/loan/available:
 *   get:
 *     tags:
 *       - Equity Secured Loan
 *     summary: Query Available Loan of 1 account
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
 *         name: loanBankCode
 *         description: available loan bank code
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastSettlementDate
 *         description: the last settlement date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastLoanBankCode
 *         description: the last loan bank code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastMatchDate
 *         description: the last match date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastLoanOrderType
 *         description: the last loan order type from previous query used to load more data
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
 *         description: Available Secured Loan
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityLoanAvailableResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/loan/detail:
 *   get:
 *     tags:
 *       - Equity Secured Loan
 *     summary: Query Detail information of 1 available loan of 1 account
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
 *         name: loanBankCode
 *         description: loan bank code
 *         schema:
 *           type: string
 *         required: true
 *       - in: query
 *         name: settleBankCode
 *         description: settle bank code of the account, default is **9999** if not set
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: matchDate
 *         required: true
 *         description: the matching date of the order that used for secured loan, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: settleDate
 *         required: true
 *         description: the settle date of the order that used for secured loan, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: loanOrderType
 *         required: true
 *         description: the loan type of this secured loan
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastSettleBankCode
 *         description: the last settle bank code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastStockCode
 *         description: the last stock code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastLoanOrderType
 *         description: the last loan order type from previous query used to load more data
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
 *         description: Detail Loan Info
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityLoanDetailResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/loan/history:
 *   get:
 *     tags:
 *       - Equity Secured Loan
 *     summary: Query Secured Loan Registration History of 1 account
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
 *         name: loanBankCode
 *         description: loan bank code
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastLoanDate
 *         description: the last loan date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastLoanBankCode
 *         description: the last loan bank code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastMatchDate
 *         description: the last match date from previous query used to load more data, format is **yyyyMMdd**
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
 *         description: List of Secured Loan Registration History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityLoanHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/loan/register:
 *   post:
 *     tags:
 *       - Equity Secured Loan
 *     summary: Register secured loan
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: array
 *             items:
 *               $ref: '#/components/schemas/EquityLoanRegisterRequest'
 *       description: Secured Loan Register object
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
