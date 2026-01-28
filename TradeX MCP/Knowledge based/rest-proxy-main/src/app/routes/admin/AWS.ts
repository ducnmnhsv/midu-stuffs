/**
 * @swagger
 * /aws:
 *   get:
 *     tags:
 *       - AWS
 *     summary: Get the signed data to upload internal
 *     security:
 *       - jwt: []
 *     parameters:
 *       - in: query
 *         name: serviceName
 *         required: true
 *         description: the service name corresponding to the uploading file
 *         schema:
 *           type: string
 *       - in: query
 *         name: key
 *         required: true
 *         description: the file name included the path that the file will be uploaded to
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Retrieve the signed data to upload internal
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/SignedDataResponse'
 */
