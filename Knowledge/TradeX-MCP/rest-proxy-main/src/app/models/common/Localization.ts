/**
 * @swagger
 * components:
 *   schemas:
 *     LangResourceFileResponse:
 *       type: object
 *       properties:
 *         namespace:
 *           type: string
 *           description: the namespace of the file
 *         url:
 *           type: string
 *           description: the file url
 *     LangResourceResponse:
 *       type: object
 *       properties:
 *         msName:
 *           type: string
 *           description: the service name of the resource
 *         latestVersion:
 *           type: string
 *           description: the latest version of the resource
 *         lang:
 *           type: string
 *           description: the language of the resource
 *         files:
 *           type: array
 *           description: list of resource files
 *           items:
 *             $ref: '#/components/schemas/LangResourceFileResponse'
 */
