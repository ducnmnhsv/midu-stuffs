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
