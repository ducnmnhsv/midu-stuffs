/**
 * @swagger
 * /tradingview/config:
 *   get:
 *     tags:
 *       - TradingView
 *     summary: Return configuration for TradingView chart
 *     security:
 *       - jwt: []
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/TVConfigResponse'
 */

/**
 * @swagger
 * /tradingview/symbols:
 *   get:
 *     tags:
 *       - TradingView
 *     summary: Return symbol info for TradingView chart
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: symbol
 *         in: query
 *         description: the name of symbol
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/TVSymbolInfoResponse'
 */

/**
 * @swagger
 * /tradingview/search:
 *   get:
 *     tags:
 *       - TradingView
 *     summary: Search symbol info for TradingView chart
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: query
 *         in: query
 *         description: Text typed by the user in the Symbol Search edit box
 *         schema:
 *           type: string
 *       - name: type
 *         in: query
 *         description: One of the symbol types supported by your back-end
 *         schema:
 *           type: string
 *           enum:
 *             - stock
 *             - index
 *             - futures
 *       - name: exchange
 *         in: query
 *         description: One of the exchanges supported by your back-end
 *         schema:
 *           type: string
 *           enum:
 *             - HOSE
 *             - HNX
 *             - UPCOM
 *       - name: limit
 *         in: query
 *         description: The maximum number of symbols in a response
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/TVSymbolSearchResponse'
 */

/**
 * @swagger
 * /tradingview/history:
 *   get:
 *     tags:
 *       - TradingView
 *     summary: Return history data for TradingView chart
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: symbol
 *         in: query
 *         description: symbol name or ticker
 *         schema:
 *           type: string
 *       - name: from
 *         in: query
 *         description: unix timestamp (UTC) of leftmost required bar
 *         schema:
 *           type: integer
 *       - name: to
 *         in: query
 *         description: unix timestamp (UTC) of rightmost required bar
 *         schema:
 *           type: integer
 *       - name: resolution
 *         in: query
 *         description: the history period
 *         schema:
 *           type: string
 *           enum:
 *             - D
 *             - W
 *             - M
 *       - name: isAdjusted
 *         in: query
 *         description: the price data is adjusted or not
 *         schema:
 *           type: boolean
 *     responses:
 *       '200':
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/TVHistoryResponse'
 */

/**
 * @swagger
 * /tradingview/{version}/charts:
 *   get:
 *     tags:
 *       - TradingView
 *     summary: Return list saved chart or 1 saved chart for TradingView chart
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: version
 *         in: path
 *         description: the version of storage api
 *         required: true
 *         schema:
 *           type: string
 *       - name: client
 *         in: query
 *         description: client id set for Trading View
 *         schema:
 *           type: string
 *       - name: chart
 *         in: query
 *         description: chart id to load data
 *         schema:
 *           type: string
 *     responses:
 *       '200':
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/TVChartListResponse'
 */

/**
 * @swagger
 * /tradingview/{version}/charts:
 *   post:
 *     tags:
 *       - TradingView
 *     summary: Save a chart for TradingView chart
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 description: name of the chart
 *                 type: string
 *               content:
 *                 description: content of the chart
 *                 type: object
 *               symbol:
 *                 description: symbol of the chart
 *                 type: string
 *               resolution:
 *                 description: resolution of the chart
 *                 type: string
 *     parameters:
 *       - name: version
 *         in: path
 *         description: the version of storage api
 *         required: true
 *         schema:
 *           type: string
 *       - name: client
 *         in: query
 *         description: client id set for Trading View
 *         schema:
 *           type: string
 *       - name: chart
 *         in: query
 *         description: chart id to update
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/TVChartSaveResponse'
 */

/**
 * @swagger
 * /tradingview/{version}/charts:
 *   delete:
 *     tags:
 *       - TradingView
 *     summary: delete a saved chart for TradingView chart
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: version
 *         in: path
 *         description: the version of storage api
 *         required: true
 *         schema:
 *           type: string
 *       - name: client
 *         in: query
 *         description: client id set for Trading View
 *         schema:
 *           type: string
 *       - name: chart
 *         in: query
 *         description: chart id to delete
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/TVChartDeleteResponse'
 */
