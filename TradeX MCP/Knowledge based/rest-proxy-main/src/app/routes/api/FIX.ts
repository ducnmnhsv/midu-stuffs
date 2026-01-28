/**
 * @swagger
 * /fix/securitiesList:
 *   get:
 *     tags:
 *       - FIX
 *     summary: Return the securities list of system
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: lastInstrumentCode
 *         description: the last instrument code from previous query that used to query the next data
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
 *                 $ref: '#/components/schemas/SecuritiesResponse'
 */

/**
 * @swagger
 * /fix/marketData/{instrumentCode}:
 *   get:
 *     tags:
 *       - FIX
 *     summary: query market data of 1 instrument
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         required: true
 *         name: instrumentCode
 *         description: the instrument code to query market data
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MarketDataResponse'
 */
