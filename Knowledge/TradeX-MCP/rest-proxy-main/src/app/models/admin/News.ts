/**
 * @swagger
 * components:
 *   schemas:
 *     SourceResponse:
 *       type: object
 *       properties:
 *         name:
 *           type: string
 *           description: the name of the source
 *         logoUrl:
 *           type: string
 *           description: the logo url of the source
 *     NewsResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: string
 *           description: the id of the news
 *         title:
 *           type: string
 *           description: the title of the news
 *         lang:
 *           type: string
 *           description: the lang of the news
 *         link:
 *           type: string
 *           description: the link of the news
 *         imgUrl:
 *           type: string
 *           description: the image url of the news
 *         publishTime:
 *           type: string
 *           description: the publish time of the news, format is 'yyyyMMddhhmmss'
 *         category:
 *           type: string
 *           description: the category of the news
 *         source:
 *           type: object
 *           $ref: '#/components/schemas/SourceResponse'
 *           description: the source of the news
 *         stockList:
 *           type: array
 *           description: list of stock linking to this news
 *           items:
 *             type: string
 *     ReportRequest:
 *       type: object
 *       properties:
 *         title:
 *           type: string
 *           description: the title of the news
 *         link:
 *           type: string
 *           description: the link of the news
 *         sourceId:
 *           type: integer
 *           description: the source unique id
 *         stockList:
 *           type: array
 *           items:
 *             type: string
 *           description: list of stock linking to this news
 */
