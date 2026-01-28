/**
 * @swagger
 * /derivatives/transfer/cash:
 *   get:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives Cash Transfer info
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
 *               $ref: '#/components/schemas/DerivativesCashTransferInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/cash:
 *   post:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives Cash Transfer
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesCashTransferRequest'
 *       description: Derivatives Cash Transfer Request object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesCashTransferResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/cash/withdraw:
 *   get:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives Cash Withdraw info
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
 *               $ref: '#/components/schemas/DerivativesCashWithdrawInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/cash/withdraw:
 *   post:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives Cash Withdraw
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesCashWithdrawRequest'
 *       description: Derivatives Cash Transfer Request object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesCashWithdrawResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/im/bank:
 *   get:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Query Bank using for deposit/withdraw Initial Margin
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
 *         name: type
 *         required: true
 *         description: the type to query info
 *         schema:
 *           type: string
 *           enum:
 *             - DEPOSIT_FROM
 *             - DEPOSIT_TO
 *             - WITHDRAW_FROM
 *             - WITHDRAW_TO
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesTransferBankResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/im/fee:
 *   get:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Query Derivatives Deposit/Withdraw Fee
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
 *         name: sendingBank
 *         required: true
 *         description: the bank to send IM
 *         schema:
 *           type: string
 *       - in: query
 *         name: receivingBank
 *         required: true
 *         description: the bank to receive IM
 *         schema:
 *           type: string
 *       - in: query
 *         name: amount
 *         required: true
 *         description: the amount to transfer
 *         schema:
 *           type: number
 *           format: double
 *       - in: query
 *         name: type
 *         required: true
 *         description: the type to query info
 *         schema:
 *           type: string
 *           enum:
 *             - DEPOSIT
 *             - WITHDRAW
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesTransferFeeResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/im/deposit:
 *   get:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives IM Deposit info
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
 *               $ref: '#/components/schemas/DerivativesIMDepositInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/im/deposit:
 *   post:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives Deposit Initial Margin
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesIMDepositRequest'
 *       description: Derivatives IM Deposit Request object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/im/withdraw:
 *   get:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives IM Withdraw info
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
 *               $ref: '#/components/schemas/DerivativesIMWithdrawInfoResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/im/withdraw:
 *   post:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Derivatives Withdraw Initial Margin
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesIMWithdrawRequest'
 *       description: Derivatives IM Withdraw Request object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/transfer/im/history:
 *   get:
 *     tags:
 *       - Derivatives Transfer
 *     summary: Query Derivatives Transfer IM History
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
 *         name: type
 *         required: true
 *         description: the type to query info
 *         schema:
 *           type: string
 *           enum:
 *             - DEPOSIT
 *             - WITHDRAW
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
 *         name: lastTransactionDate
 *         description: the last transaction date from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastSequenceNumber
 *         description: the last sequence number from previous query used to load more data
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
 *         description: Transfer IM History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesTransferIMResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
