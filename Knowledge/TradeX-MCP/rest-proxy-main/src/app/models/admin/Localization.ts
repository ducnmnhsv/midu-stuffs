/**
 * @swagger
 * components:
 *   schemas:
 *     LangNamespaceResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: number
 *           description: the unique id of the namespace
 *         namespace:
 *           type: string
 *           description: the namespace of the namespace
 *     LangResourceResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: number
 *           description: the unique id of the resource
 *         msName:
 *           type: string
 *           description: the service name of the resource
 *         namespaces:
 *           type: array
 *           description: list of namespaces
 *           items:
 *             $ref: '#/components/schemas/LangNamespaceResponse'
 *     LangKeyResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: number
 *           description: the unique id of the key
 *         key:
 *           type: string
 *           description: the key to be translated
 *     UpdateLangRequest:
 *       type: object
 *       properties:
 *         value:
 *           type: string
 *           description: the value to be update into translation
 *     UploadLangResourceRequest:
 *       type: object
 *       properties:
 *         lang:
 *           type: string
 *           description: the lang to upload the namespace
 *         version:
 *           type: string
 *           description: the version to update for the resource
 */
