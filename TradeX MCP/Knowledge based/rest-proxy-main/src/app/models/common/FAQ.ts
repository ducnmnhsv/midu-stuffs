/**
 * @swagger
 * components:
 *   schemas:
 *     FaqGroupResponse:
 *       type: object
 *       properties:
 *         name:
 *           type: string
 *           description: the name of the group
 *         faqs:
 *           type: array
 *           description: list of faq belong to this group
 *           items:
 *             $ref: '#/components/schemas/FaqResponse'
 *     FaqResponse:
 *       type: object
 *       properties:
 *         question:
 *           type: string
 *           description: the question of the faq
 *         answer:
 *           type: string
 *           description: the answer of the faq
 */
