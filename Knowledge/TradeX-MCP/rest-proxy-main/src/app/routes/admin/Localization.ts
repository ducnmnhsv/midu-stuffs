/**
 * @swagger
 * /locale/resource:
 *   get:
 *     tags:
 *       - Localization
 *     security:
 *       - jwt: []
 *     summary: Get all resources all TradeX system
 *     responses:
 *       200:
 *         description: List of language resource
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/LangResourceResponse'
 */

/**
 * @swagger
 * /locale/{namespaceId}/key:
 *   get:
 *     tags:
 *       - Localization
 *     security:
 *       - jwt: []
 *     summary: Get all keys of 1 namespace
 *     parameters:
 *       - in: path
 *         name: namespaceId
 *         required: true
 *         description: id of the namespace
 *         schema:
 *           type: integer
 *       - in: query
 *         name: lastKey
 *         description: the last key of the previous query, used to query next
 *         schema:
 *           type: string
 *       - name: fetchCount
 *         in: query
 *         description: the number of records return for each query, default is **20**
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *     responses:
 *       200:
 *         description: List of key
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/LangKeyResponse'
 */

/**
 * @swagger
 * /locale/{namespaceId}/key:
 *   post:
 *     tags:
 *       - Localization
 *     security:
 *       - jwt: []
 *     summary: add a new key in a namespace
 *     parameters:
 *       - in: path
 *         name: namespaceId
 *         required: true
 *         description: id of the namespace
 *         schema:
 *           type: integer
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               key:
 *                 type: string
 *                 description: the key to add to the namespace
 *       description: Add Key Object
 *       required: true
 *     responses:
 *       200:
 *         description: List of key
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/LangKeyResponse'
 */

/**
 * @swagger
 * /locale/{namespaceId}/key/{keyId}:
 *   delete:
 *     tags:
 *       - Localization
 *     security:
 *       - jwt: []
 *     summary: delete 1 key in a namespace
 *     parameters:
 *       - in: path
 *         name: namespaceId
 *         required: true
 *         description: id of the namespace
 *         schema:
 *           type: integer
 *       - in: path
 *         name: keyId
 *         required: true
 *         description: id of the key
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: OK
 */

/**
 * @swagger
 * /locale/{keyId}/{lang}:
 *   put:
 *     tags:
 *       - Localization
 *     security:
 *       - jwt: []
 *     summary: Update translation of 1 key for 1 language
 *     parameters:
 *       - in: path
 *         name: keyId
 *         required: true
 *         description: id of the key
 *         schema:
 *           type: integer
 *       - in: path
 *         name: lang
 *         required: true
 *         description: the lang to be translated
 *         schema:
 *           type: string
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/UpdateLangRequest'
 *       description: Update Lang Object
 *     responses:
 *       200:
 *         description: OK
 */

/**
 * @swagger
 * /locale/{namespaceId}/upload:
 *   put:
 *     tags:
 *       - Localization
 *     security:
 *       - jwt: []
 *     summary: Upload latest version of namespace to AWS
 *     parameters:
 *       - in: path
 *         name: namespaceId
 *         required: true
 *         description: id of the namespace
 *         schema:
 *           type: integer
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/UploadLangResourceRequest'
 *       description: Upload Language Resource Request Object
 *     responses:
 *       200:
 *         description: OK
 */
