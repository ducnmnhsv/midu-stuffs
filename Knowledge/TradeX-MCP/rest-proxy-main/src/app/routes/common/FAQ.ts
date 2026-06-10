/**
 * @swagger
 * /faq/{msName}:
 *   get:
 *     tags:
 *       - FAQ
 *     summary: Get the list of faq for specified service
 *     parameters:
 *       - in: path
 *         name: msName
 *         required: true
 *         description: the service name that used to query
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Retrieve the list of faq, categorized by group
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/FaqGroupResponse'
 */

/**
 * @swagger
 * /faq/{faqId}/review/{isUseful}:
 *   post:
 *     tags:
 *       - FAQ
 *     summary: Review the faq is useful or not
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         name: faqId
 *         required: true
 *         description: the id of the faq
 *         schema:
 *           type: number
 *       - in: path
 *         name: isUseful
 *         required: true
 *         description: the review of the user
 *         schema:
 *           type: boolean
 *     responses:
 *       200:
 *         description: Review Successfully
 */
