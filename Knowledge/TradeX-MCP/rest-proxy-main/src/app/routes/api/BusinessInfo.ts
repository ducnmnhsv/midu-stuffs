/**
 * @swagger
 * /businessInfo:
 *   get:
 *     tags:
 *       - Business Info
 *     summary: Get latest business info of 1 company
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: code
 *         schema:
 *           type: string
 *         description: code of company
 *         required: true
 *     responses:
 *       '200':
 *         description: successful operation
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/BusinessInfoResponse'
 *       '401':
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /businessInfo/year:
 *   get:
 *     tags:
 *       - Business Info
 *     summary: Filter yearly data of company
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: code
 *         schema:
 *           type: string
 *         description: code of company
 *         required: true
 *       - in: query
 *         name: fromYear
 *         schema:
 *           type: integer
 *         description: the data should be equal or after this year, default is **this year**
 *       - in: query
 *         name: toYear
 *         description: the data should be equal or before this year, default is **this year**
 *         schema:
 *           type: integer
 *     responses:
 *       '200':
 *         description: successful operation
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/BusinessInfoResponse'
 *       '401':
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /businessInfo/quarter:
 *   get:
 *     tags:
 *       - Business Info
 *     summary: Filter quarterly data of company
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: code
 *         schema:
 *           type: string
 *         description: code of company
 *         required: true
 *       - in: query
 *         name: fromYear
 *         schema:
 *           type: integer
 *         description: the data should be equal or after this year, default is **this year**
 *       - in: query
 *         name: fromQuarter
 *         schema:
 *           type: integer
 *           enum:
 *           - 1
 *           - 2
 *           - 3
 *           - 4
 *         description: the data should be equal or after this quarter of from year, default is **1**
 *       - in: query
 *         name: toYear
 *         description: the data should be equal or before this year, default is **this year**
 *         schema:
 *           type: integer
 *       - in: query
 *         name: toQuarter
 *         description: the data should be equal or before this quarter of from year, default is **4**
 *         schema:
 *           type: integer
 *           enum:
 *           - 1
 *           - 2
 *           - 3
 *           - 4
 *     responses:
 *       '200':
 *         description: successful operation
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/BusinessInfoResponse'
 *       '401':
 *         description: Your access token is invalid or expired
 */
