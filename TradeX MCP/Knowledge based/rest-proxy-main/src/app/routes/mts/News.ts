/**
 * @swagger
 * /news:
 *   get:
 *     tags:
 *       - News
 *     summary: Get latest news for each category
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: fetchCount
 *         required: true
 *         description: the number of records return for each category
 *         schema:
 *           type: integer
 *           enum:
 *             - '5'
 *             - '10'
 *             - '20'
 *             - '50'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/NewsResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /news/favorite:
 *   get:
 *     tags:
 *       - News
 *     summary: Get latest news for symbol inside the current favorite list
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: favoriteId
 *         required: true
 *         description: the favorite list Id to filter news
 *         schema:
 *           type: integer
 *       - in: query
 *         name: lastSequence
 *         description: the sequence number of the last news used to query next data
 *         schema:
 *           type: string
 *       - in: query
 *         name: publishTime
 *         description: the publish time of the last news used to query next data, format is **yyyyMMddhhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         required: true
 *         description: the number of records return for each category
 *         schema:
 *           type: integer
 *           enum:
 *             - '5'
 *             - '10'
 *             - '20'
 *             - '50'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/NewsResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /news/top:
 *   get:
 *     tags:
 *       - News
 *     summary: Get top news
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: fetchCount
 *         required: true
 *         description: the number of records return for each query
 *         schema:
 *           type: integer
 *           enum:
 *             - '5'
 *             - '10'
 *             - '20'
 *             - '50'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/NewsResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /news/filter:
 *   get:
 *     tags:
 *       - News
 *     summary: Find news by condition
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: keyword
 *         description: the keyword used to filter by title
 *         schema:
 *           type: string
 *       - in: query
 *         name: symbolCode
 *         description: the symbol code used to filter
 *         schema:
 *           type: string
 *       - name: symbolList
 *         in: query
 *         description: the list of symbol code to query info
 *         schema:
 *           type: array
 *           items:
 *             type: string
 *       - in: query
 *         name: category
 *         description: the category used to filter
 *         schema:
 *           type: string
 *           enum:
 *             - MARKET_NEWS
 *             - STOCK_NEWS
 *             - VSD_NEWS
 *             - ANALYSIS_REPORT
 *       - in: query
 *         name: lastSequence
 *         description: the sequence number of the last news used to query next data
 *         schema:
 *           type: string
 *       - in: query
 *         name: publishTime
 *         description: the publish time of the last news used to query next data, format is **yyyyMMddhhmmss**
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         required: true
 *         description: the number of records return for each query
 *         schema:
 *           type: integer
 *           enum:
 *             - '5'
 *             - '10'
 *             - '20'
 *             - '50'
 *             - '100'
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/NewsResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /news/notification:
 *   get:
 *     tags:
 *       - News
 *     summary: Get all news's notification
 *     security:
 *       - jwt: []
 *     responses:
 *       200:
 *         description: Return list news notification of 1 user
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: string
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /news/notification:
 *   put:
 *     tags:
 *       - News
 *     summary: Update news's notification
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/NewsNotificationUpdateRequest'
 *       description: News Notification Update object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /news/announcement:
 *   get:
 *     tags:
 *       - News
 *     summary: Get list announcement
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: source
 *         description: the source to filter announcement
 *         schema:
 *           type: string
 *       - in: query
 *         name: category
 *         description: the category used to filter
 *         schema:
 *           type: string
 *       - in: query
 *         name: fromDate
 *         description: the start date used to filter, format is **yyyyMMdd**, default is **19700101**
 *         schema:
 *           type: string
 *       - in: query
 *         name: toDate
 *         description: the end date used to filter, format is **yyyyMMdd**, default is **today**
 *         schema:
 *           type: string
 *       - in: query
 *         name: sequence
 *         description: the sequence number of the last announcement used to query next data
 *         schema:
 *           type: string
 *       - in: query
 *         name: fetchCount
 *         required: true
 *         description: the number of records return for each query
 *         schema:
 *           type: integer
 *           enum:
 *             - '5'
 *             - '10'
 *             - '20'
 *             - '50'
 *             - '100'
 *     responses:
 *       200:
 *         description: Return list announcement
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/NewsResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */
