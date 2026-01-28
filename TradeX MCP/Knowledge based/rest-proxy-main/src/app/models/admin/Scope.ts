/**
 * @swagger
 * components:
 *   schemas:
 *     ScopeResponse:
 *       type: object
 *       allOf:
 *         - properties:
 *             id:
 *               type: integer
 *               description: the primary key of scope
 *             name:
 *               type: string
 *               description: the unique name of the scope to display
 *             uriPattern:
 *               type: string
 *               description: the uri to filter by AAA system
 *             forwardType:
 *               type: string
 *               description: the forward type of the scope
 *             forwardData:
 *               type: object
 *               description: the forward data of the scope
 *         - $ref: '#/components/schemas/BaseObjectResponse'
 *     ScopeSaveRequest:
 *       type: object
 *       properties:
 *         name:
 *           type: string
 *           description: the name of the scope
 *         uriPattern:
 *           type: string
 *           description: the uri pattern of the scope
 *         forwardType:
 *           type: string
 *           description: the forward type of the scope
 *           enum:
 *             - CONNECTION
 *             - SERVICE
 *         forwardData:
 *           type: object
 *           description: the forward data of the scope
 *     ScopeGroupResponse:
 *       type: object
 *       allOf:
 *         - properties:
 *             id:
 *               type: integer
 *               description: the primary key of scope group
 *             scopeGroupName:
 *               type: string
 *               description: the name of the scope group to display
 *         - $ref: '#/components/schemas/BaseObjectResponse'
 */
