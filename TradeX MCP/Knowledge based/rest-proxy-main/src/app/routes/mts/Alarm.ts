/**
 * @swagger
 * /alarm:
 *   get:
 *     tags:
 *       - Alarm
 *     summary: Get all alarm settings
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
 *                 $ref: '#/components/schemas/AlarmSettingResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /alarm:
 *   post:
 *     tags:
 *       - Alarm
 *     summary: add 1 alarm setting
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/AlarmSettingRequest'
 *       description: Alarm Setting object
 *       required: true
 *     responses:
 *       200:
 *         description: Add alarm setting successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/AlarmSettingResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /alarm/{alarmId}:
 *   put:
 *     tags:
 *       - Alarm
 *     summary: update 1 alarm setting
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: path
 *         name: alarmId
 *         required: true
 *         description: id of the alarm setting to be updated
 *         schema:
 *           type: integer
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/AlarmSettingUpdateRequest'
 *       description: Alarm Setting object
 *       required: true
 *     responses:
 *       200:
 *         description: Update alarm setting successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/AlarmSettingResponse'
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /alarm:
 *   delete:
 *     tags:
 *       - Alarm
 *     summary: Delete 1 or many alarm setting
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
 *         description: Delete alarm setting list successfully
 *       401:
 *         description: Your access token is invalid or expired
 */
