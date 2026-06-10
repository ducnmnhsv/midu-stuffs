/**
 * @swagger
 * /common/dataview:
 *   get:
 *     tags:
 *       - Common
 *     summary: Get data from data view
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: code
 *         in: query
 *         description: the code of the data view
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
 *       - name: filter
 *         in: query
 *         description: the data used to filter dataview, such as **lang**
 *         schema:
 *           type: object
 *     responses:
 *       200:
 *         description: the data from data view
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 oneOf:
 *                   - $ref: '#/components/schemas/ViewSelectResponse'
 */
