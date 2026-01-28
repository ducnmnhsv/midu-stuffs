/**
 * @swagger
 * components:
 *   schemas:
 *     AdminInfoResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the unique id of TradeX user
 *         username:
 *           type: string
 *           description: username that used for logging in TradeX Admin System
 *         displayName:
 *           type: string
 *           description: display name that displayed for another TradeX user
 *         avatar:
 *           type: string
 *           description: avatar image url of TradeX user
 *         createdAt:
 *           type: string
 *           description: the created time of TradeX admin, format is 'yyyyMMdd'
 *         adminRoleIds:
 *           type: array
 *           description: List role of the admin
 *           items:
 *             type: integer
 *         menuGroups:
 *           type: array
 *           description: List grouped menu that the admin can access
 *           items:
 *             $ref: '#/components/schemas/MenuGroup'
 */
