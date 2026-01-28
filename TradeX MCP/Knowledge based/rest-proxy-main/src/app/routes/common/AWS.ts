/**
 * @swagger
 * /aws:
 *   get:
 *     tags:
 *       - AWS
 *     summary: Get the signed data to upload image
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: key
 *         required: true
 *         description: the file name included the path that the file will be uploaded to
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Retrieve the signed data to upload image
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/SignedDataResponse'
 */
