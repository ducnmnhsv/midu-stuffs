/**
 * @swagger
 * /equity/order:
 *   post:
 *     tags:
 *       - Equity Order
 *     summary: Place Normal Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderRequest'
 *       description: Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Place Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityOrderResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/modify:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Modify Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderModifyRequest'
 *       description: Order Modify object
 *       required: true
 *     responses:
 *       200:
 *         description: Modify Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/modify/all:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Modify All Orders
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderModifyAllRequest'
 *       description: Order Modify All object
 *       required: true
 *     responses:
 *       200:
 *         description: Modify All Orders Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/cancel:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Cancel Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderCancelRequest'
 *       description: Order Cancel object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/cancel/all:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Cancel All Orders
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderCancelAllRequest'
 *       description: Order Cancel All object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel All Orders Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/todayUnmatch:
 *   get:
 *     tags:
 *       - Equity Order
 *     summary: Query Today Unmatch Orders
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
 *         name: stockCode
 *         description: query filter by 1 stock
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastOrderPrice
 *         description: the last order price from previous query used to load more data
 *         schema:
 *           type: number
 *           format: double
 *       - in: query
 *         name: lastBranchCode
 *         description: the last branch code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastOrderNumber
 *         description: the last order number from previous query used to load more data
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
 *         description: Today Unmatch Order
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityOrderTodayUnmatchResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/history:
 *   get:
 *     tags:
 *       - Equity Order
 *     summary: Query Order History
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
 *         name: marketType
 *         description: the market to query info
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: fromDate
 *         description: query order that place after or on this date, format is **yyyyMMdd**, default is **19700101**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toDate
 *         description: query order that place before or on this date, format is **yyyyMMdd**, default is **today**
 *         schema:
 *           type: string
 *       - in: query
 *         name: stockCode
 *         description: query filter by 1 stock
 *         schema:
 *           type: string
 *       - in: query
 *         name: sellBuyType
 *         description: query filter by buy or sell type
 *         schema:
 *           type: string
 *           enum:
 *             - SELL
 *             - BUY
 *       - in: query
 *         name: matchType
 *         description: query filter by match status of order
 *         schema:
 *           type: string
 *           enum:
 *             - MATCHED
 *             - UNMATCHED
 *       - in: query
 *         name: sortType
 *         description: sorting direction by time for result
 *         schema:
 *           type: string
 *           enum:
 *             - ASC
 *             - DESC
 *       - in: query
 *         name: lastOrderDate
 *         description: the last order date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastBranchCode
 *         description: the last branch code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastOrderNumber
 *         description: the last order number from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastMatchPrice
 *         description: the last match price from previous query used to load more data
 *         schema:
 *           type: number
 *           format: double
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
 *         description: Order History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityOrderHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/oddlot/sellable:
 *   get:
 *     tags:
 *       - Equity Order
 *     summary: Get all sellable stocks to place Oddlot order
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
 *       '200':
 *         description: All sellable stocks to place oddlot order
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityOrderOddlotSellableResponse'
 *       '401':
 *         description: Your access token is expired
 */

/**
 * @swagger
 * /equity/order/oddlot:
 *   post:
 *     tags:
 *       - Equity Order
 *     summary: Place Oddlot Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderOddlotRequest'
 *       description: Order Oddlot object
 *       required: true
 *     responses:
 *       '200':
 *         description: Place Oddlot Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityOrderResponse'
 *       '401':
 *         description: Your access token is expired
 */

/**
 * @swagger
 * /equity/order/oddlot/cancel:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Cancel Oddlot Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderOddlotCancelRequest'
 *       description: Cancel Order Oddlot object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel Oddlot Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is expired
 */

/**
 * @swagger
 * /equity/order/oddlot/todayUnmatch:
 *   get:
 *     tags:
 *       - Equity Order
 *     summary: Query list of unmatched oddlot order
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
 *         name: lastBranchCode
 *         description: the last branch code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastOrderNumber
 *         description: the last order number from previous query used to load more data
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
 *       '200':
 *         description: List of unmatch oddlot orders
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityOrderOddlotUnmatchResponse'
 *       '401':
 *         description: Your access token is expired
 */

/**
 * @swagger
 * /equity/order/oddlot/history:
 *   get:
 *     tags:
 *       - Equity Order
 *     summary: Query list of oddlot orders
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
 *         name: matchType
 *         description: query filter by match status of order
 *         schema:
 *           type: string
 *           enum:
 *             - MATCHED
 *             - UNMATCHED
 *       - in: query
 *         name: lastBranchCode
 *         description: the last branch code from previous query used to load more data
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastOrderNumber
 *         description: the last order number from previous query used to load more data
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
 *       '200':
 *         description: List of oddlot orders
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityOrderOddlotHistoryResponse'
 *       '401':
 *         description: Your access token is expired
 */

/**
 * @swagger
 * /equity/order/advance:
 *   post:
 *     tags:
 *       - Equity Order
 *     summary: Place Advance Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderAdvanceRequest'
 *       description: Advance Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Place Advance Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/EquityAdvanceOrderResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/advance/cancel:
 *   put:
 *     tags:
 *       - Equity Order
 *     summary: Cancel Advance Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/EquityOrderAdvanceCancelRequest'
 *       description: Order Advance Cancel object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel Advance Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /equity/order/advance/history:
 *   get:
 *     tags:
 *       - Equity Order
 *     summary: Query Advance Order History
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
 *         name: marketType
 *         description: the market to query info
 *         schema:
 *           type: string
 *           enum:
 *             - ALL
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *       - in: query
 *         name: stockCode
 *         description: query filter by 1 stock
 *         schema:
 *           type: string
 *       - in: query
 *         name: sellBuyType
 *         description: query filter by buy or sell type
 *         schema:
 *           type: string
 *           enum:
 *             - SELL
 *             - BUY
 *       - in: query
 *         name: lastOrderDate
 *         description: the last order date from previous query used to load more data, format is **yyyyMMdd**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastOrderNumber
 *         description: the last order number from previous query used to load more data
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
 *         description: Advance Order History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/EquityAdvanceOrderHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
