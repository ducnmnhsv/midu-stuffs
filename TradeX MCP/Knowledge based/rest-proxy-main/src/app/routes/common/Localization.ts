/**
 * @swagger
 * /locale:
 *   get:
 *     tags:
 *       - Localization
 *     summary: Get latest version data of all Language Resource File for 1 system
 *     parameters:
 *       - in: query
 *         name: msNames
 *         required: true
 *         description: the list of service name that used language resource file
 *         schema:
 *           type: array
 *           items:
 *             type: string
 *     responses:
 *       200:
 *         description: List of language resource file
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/LangResourceResponse'
 */
