/**
 * @swagger
 * components:
 *   schemas:
 *     ViewSelectResponse:
 *       type: object
 *       properties:
 *         id:
 *           oneOf:
 *             - type: string
 *             - type: integer
 *           description: the unique id of data
 *         text:
 *           type: string
 *           description: the text of the data
 *     MenuGroup:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the unique id of menu group
 *         groupName:
 *           type: string
 *           description: the name of menu group
 *         menus:
 *           type: array
 *           description: the list of menu
 *           items:
 *             $ref: '#/components/schemas/Menu'
 *     Menu:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the unique id of menu group
 *         title:
 *           type: string
 *           description: the title of menu
 *         order:
 *           type: integer
 *           description: the order of the menu
 *         parent:
 *           type: integer
 *           description: the id of the parent menu
 *         href:
 *           type: string
 *           description: the href of the menu
 *         screenCode:
 *           type: string
 *           description: the screen code of the menu
 *         isLeaf:
 *           type: boolean
 *           description: the menu is leaf or not
 *     OperatorResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the primary key of user
 *         username:
 *           type: string
 *           description: the username of user
 *     BaseObjectResponse:
 *       type: object
 *       properties:
 *         createdBy:
 *           type: object
 *           $ref: '#/components/schemas/OperatorResponse'
 *           description: the user who created this data
 *         createdAt:
 *           type: string
 *           description: the time that this data is created, format is 'yyyyMMddhhmmss'
 *         updatedBy:
 *           type: object
 *           $ref: '#/components/schemas/OperatorResponse'
 *           description: the last user who updated this data
 *         updatedAt:
 *           type: string
 *           description: the last time that this data is updated, format is 'yyyyMMddhhmmss'
 */
