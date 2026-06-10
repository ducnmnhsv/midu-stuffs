/**
 * @swagger
 * /derivatives/order/available:
 *   get:
 *     tags:
 *       - Derivatives Order
 *     summary: Query Derivatives Available Quantity
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
 *         name: code
 *         required: true
 *         description: query filter by 1 contract
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
 *         name: orderPrice
 *         description: limit price to query available quantity
 *         schema:
 *           type: number
 *           format: double
 *       - in: query
 *         name: orderType
 *         description: query filter by buy or sell type
 *         schema:
 *           type: string
 *           enum:
 *             - LO
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *     responses:
 *       200:
 *         description: Derivatives Order Available Quantity
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesOrderAvailableQuantity'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/unmatchPosition:
 *   get:
 *     tags:
 *       - Derivatives Order
 *     summary: Query Derivatives Unmatch Position
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
 *         name: code
 *         description: query filter by 1 contract
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
 *     responses:
 *       200:
 *         description: Derivatives Order Unmatch Position
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesOrderUnmatchPosition'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order:
 *   post:
 *     tags:
 *       - Derivatives Order
 *     summary: Place Normal Derivatives Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderRequest'
 *       description: Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Place Derivatives Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesOrderResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/modify:
 *   put:
 *     tags:
 *       - Derivatives Order
 *     summary: Modify Derivatives Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderModifyRequest'
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
 * /derivatives/order/cancel:
 *   put:
 *     tags:
 *       - Derivatives Order
 *     summary: Cancel Derivatives Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderCancelRequest'
 *       description: Derivatives Order Cancel object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel Derivatives Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/cancel/all:
 *   put:
 *     tags:
 *       - Derivatives Order
 *     summary: Cancel All Orders
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderCancelAllRequest'
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
 * /derivatives/order/todayUnmatch:
 *   get:
 *     tags:
 *       - Derivatives Order
 *     summary: Query Derivatives Today Unmatch
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
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Derivatives Order Today Unmatch
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesOrderTodayUnmatchResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/history:
 *   get:
 *     tags:
 *       - Derivatives Order
 *     summary: Query Derivatives Order History
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
 *         name: date
 *         description: query order that place after or on this date, format is **yyyyMMdd**, default is **19700101**
 *         schema:
 *           type: string
 *       - in: query
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Order History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesOrderHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/advance:
 *   post:
 *     tags:
 *       - Derivatives Order
 *     summary: Place Advance Derivatives Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderAdvanceRequest'
 *       description: Derivatives Advance Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Place Derivatives Advance Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DerivativesOrderResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/advance/cancel:
 *   put:
 *     tags:
 *       - Derivatives Order
 *     summary: Cancel Derivatives Advance Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderAdvanceCancelRequest'
 *       description: Derivatives Order Advance Cancel object
 *       required: true
 *     responses:
 *       200:
 *         description: Cancel Derivatives Advance Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/advance/history:
 *   get:
 *     tags:
 *       - Derivatives Order
 *     summary: Query Derivatives Advance Order History
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
 *         name: code
 *         description: query filter by 1 contract
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
 *         name: matchedStatus
 *         description: query filter by matched status of order
 *         schema:
 *           type: string
 *           enum:
 *             - PARTIAL_MATCHED
 *             - FULLY_MATCHED
 *             - UNMATCHED
 *       - in: query
 *         name: advanceOrderType
 *         schema:
 *           type: string
 *           enum:
 *             - AO
 *             - CAO
 *         description: type of advance order (Advance Order or Conditional Advance Order)
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
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Order History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesOrderAdvanceHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/stop:
 *   post:
 *     tags:
 *       - Derivatives Order
 *     summary: Place Derivatives Stop Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderStopRequest'
 *       description: Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Place Derivatives Stop Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/stop/modify:
 *   put:
 *     tags:
 *       - Derivatives Order
 *     summary: Modify Derivatives Stop Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderStopModifyRequest'
 *       description: Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Modify Derivatives Stop Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/stop/cancel:
 *   put:
 *     tags:
 *       - Derivatives Order
 *     summary: Cancel Derivatives Stop Order
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/DerivativesOrderStopCancelRequest'
 *       description: Order object
 *       required: true
 *     responses:
 *       200:
 *         description: Modify Derivatives Stop Order Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/MessageResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /derivatives/order/stop/history:
 *   get:
 *     tags:
 *       - Derivatives Order
 *     summary: Query Derivatives Stop Order History
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
 *         name: code
 *         description: query filter by 1 contract
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
 *         name: isSent
 *         description: filter by send status
 *         schema:
 *           type: boolean
 *       - in: query
 *         name: isRegistered
 *         description: filter by register status
 *         schema:
 *           type: boolean
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
 *         name: lastNextKey
 *         description: the last next data from previous query used to load more data
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
 *         description: Derivatives Stop Order History
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DerivativesOrderStopHistoryResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
