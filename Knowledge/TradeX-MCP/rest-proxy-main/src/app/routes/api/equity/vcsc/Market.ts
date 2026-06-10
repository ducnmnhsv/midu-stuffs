/**
 * @swagger
 * /market/stock/{stockCode}/estimateCeilingFloor:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query estimated price data of stock
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: stockCode
 *         in: path
 *         description: stock code to query info
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EstimateCeilingFloorResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /market/cw/{cwCode}/estimateCeilingFloor:
 *   get:
 *     tags:
 *       - Market
 *     summary: Query estimated price data of cw
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: cwCode
 *         in: path
 *         description: cw code to query info
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EstimateCeilingFloorResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
