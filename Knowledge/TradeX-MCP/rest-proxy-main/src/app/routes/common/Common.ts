/**
 * @swagger
 * /common/services:
 *   get:
 *     tags:
 *       - Common
 *     summary: Get the list of external services integrating with TradeX system
 *     security:
 *       - jwt: []
 *     responses:
 *       200:
 *         description: Retrieve the list of external service
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/ServiceResponse'
 */
