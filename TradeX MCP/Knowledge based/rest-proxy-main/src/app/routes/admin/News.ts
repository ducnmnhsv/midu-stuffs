/**
 * @swagger
 * /news/report:
 *   get:
 *     tags:
 *       - News
 *     summary: Get the list of report
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: lang
 *         in: query
 *         description: the lang to filter report
 *         schema:
 *           type: string
 *       - name: sourceId
 *         in: query
 *         description: the source id to filter report
 *         schema:
 *           type: string
 *       - name: symbolCode
 *         in: query
 *         description: the symbol code to filter report
 *         schema:
 *           type: string
 *       - name: keyword
 *         in: query
 *         description: the keyword to filter report by title
 *         schema:
 *           type: string
 *       - name: lastSequence
 *         in: query
 *         description: the last sequence from previous query used to load more data
 *         schema:
 *           type: string
 *       - name: fetchCount
 *         in: query
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
 *         description: the list of report
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 oneOf:
 *                   - $ref: '#/components/schemas/NewsResponse'
 */

/**
 * @swagger
 * /news/report:
 *   post:
 *     tags:
 *       - News
 *     summary: Add a new report
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ReportRequest'
 *       description: Add Report Object
 *       required: true
 *     responses:
 *       200:
 *         description: the created report object
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/NewsResponse'
 */

/**
 * @swagger
 * /news/report/{reportId}:
 *   put:
 *     tags:
 *       - News
 *     summary: Modify a report
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         name: reportId
 *         required: true
 *         description: id of the report to be updated
 *         schema:
 *           type: string
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ReportRequest'
 *       description: Modify Report Object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 */

/**
 * @swagger
 * /news/report/{reportId}:
 *   delete:
 *     tags:
 *       - News
 *     summary: Delete a report
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         name: string
 *         required: true
 *         description: id of the report to be deleted
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: OK
 */
