/**
 * @swagger
 * /favorite:
 *   get:
 *     tags:
 *       - Favorite
 *     summary: Get all favourite list
 *     security:
 *       - jwt: []
 *     responses:
 *       200:
 *         description: OK
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/FavoriteListResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /favorite:
 *   post:
 *     tags:
 *       - Favorite
 *     summary: add 1 favorite list
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/FavoriteListRequest'
 *       description: Favorite List object
 *       required: true
 *     responses:
 *       200:
 *         description: Add favorite list successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/FavoriteListResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /favorite:
 *   put:
 *     tags:
 *       - Favorite
 *     summary: Update one or many favorite list
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: array
 *             items:
 *               $ref: '#/components/schemas/FavoriteListUpdateRequest'
 *       description: Favorite List Update object
 *       required: true
 *     responses:
 *       200:
 *         description: Update all favorite list successfully
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /favorite:
 *   delete:
 *     tags:
 *       - Favorite
 *     summary: Delete 1 or many favorite list
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: items
 *         required: true
 *         description: the list of id to be deleted
 *         schema:
 *           type: array
 *           items:
 *             type: integer
 *     responses:
 *       200:
 *         description: Delete favorite list successfully
 *       401:
 *         description: Your access token is invalid or expired
 */
