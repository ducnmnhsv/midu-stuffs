/**
 * @swagger
 * components:
 *   schemas:
 *     FavoriteListRequest:
 *       type: object
 *       properties:
 *         name:
 *           type: string
 *           description: the name of the favorite list
 *     FavoriteListUpdateRequest:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the id of favorite list
 *         name:
 *           type: string
 *           description: the name of the favorite list
 *         order:
 *           type: integer
 *           description: the order of the favorite list
 *         symbolList:
 *           type: array
 *           items:
 *             type: string
 *           description: the list of symbol code of this favorite list
 *     FavoriteListResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the id of favorite list
 *         name:
 *           type: string
 *           description: the name of the favorite list
 *         order:
 *           type: integer
 *           description: the order of the favorite list
 *         count:
 *           type: integer
 *           description: current number of symbol in the favorite list
 *         maxCount:
 *           type: integer
 *           description: maximum number of symbol that can be in the favorite list
 *         symbolList:
 *           type: array
 *           items:
 *             type: string
 *           description: the list of symbol code of this favorite list
 */
