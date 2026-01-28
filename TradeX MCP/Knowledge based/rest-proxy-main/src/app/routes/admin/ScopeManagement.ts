/**
 * @swagger
 * /scope/group:
 *   get:
 *     tags:
 *       - Scope Management
 *     summary: Return the list of scope groups
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: scopeGroupName
 *         in: query
 *         description: filter scope group list by scope group name using LIKE operator
 *         schema:
 *           type: string
 *       - name: lastSequence
 *         in: query
 *         description: the last sequence from previous query used to load more data
 *         schema:
 *           type: integer
 *       - name: fetchCount
 *         in: query
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *         description: the number of records return for each query, default is **20**
 *     responses:
 *       200:
 *         description: List of Scope Group
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/ScopeGroupResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /scope:
 *   get:
 *     tags:
 *       - Scope Management
 *     summary: Return the list of scope
 *     security:
 *       - jwt: []
 *     parameters:
 *       - name: scopeGroupId
 *         in: query
 *         schema:
 *           type: integer
 *         description: filter scope by scope group id
 *       - name: name
 *         in: query
 *         schema:
 *           type: string
 *         description: filter scope by name using **LIKE** operator
 *       - name: uriPattern
 *         in: query
 *         schema:
 *           type: string
 *         description: filter scope by uri using **=** operator
 *       - name: forwardType
 *         in: query
 *         schema:
 *           type: string
 *           enum:
 *             - CONNECTION
 *             - SERVICE
 *         description: filter scope by forward type using **=** operator
 *       - name: lastSequence
 *         in: query
 *         description: the last sequence from previous query used to load more data
 *         schema:
 *           type: integer
 *       - name: fetchCount
 *         in: query
 *         schema:
 *           type: integer
 *           enum:
 *             - '20'
 *             - '40'
 *             - '60'
 *             - '80'
 *             - '100'
 *         description: the number of records return for each query, default is **20**
 *     responses:
 *       200:
 *         description: List of Scope
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/ScopeResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /scope:
 *   post:
 *     tags:
 *       - Scope Management
 *     summary: Add new scope
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/SaveScopeRequest'
 *       description: Add Scope Object
 *       required: true
 *     responses:
 *       200:
 *         description: the new scope object
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ScopeResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /scope/{scopeId}:
 *   put:
 *     tags:
 *       - Scope Management
 *     summary: Update a scope
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         name: scopeId
 *         required: true
 *         description: id of the scope
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/SaveScopeRequest'
 *       description: Update Scope Object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /scope/{scopeId}:
 *   delete:
 *     tags:
 *       - Scope Management
 *     summary: Delete a scope
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         name: scopeId
 *         required: true
 *         description: id of the scope
 *     responses:
 *       200:
 *         description: OK
 *       401:
 *         description: Your access token is invalid or expired
 */
