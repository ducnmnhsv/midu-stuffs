/**
 * @swagger
 * components:
 *   schemas:
 *     SignedDataResponse:
 *       type: object
 *       properties:
 *         key:
 *           type: string
 *           description: the file name included the path that the file will be uploaded to
 *         bucket:
 *           type: string
 *           description: the bucket that file will be uploaded to
 *         Policy:
 *           type: string
 *           description: the post policy of the request
 *         X-Amz-Signature:
 *           type: string
 *           description: data return from S3 Service
 *         X-Amz-Date:
 *           type: string
 *           description: data return from S3 Service
 *         X-Amz-Credential:
 *           type: string
 *           description: data return from S3 Service
 *         X-Amz-Algorithm:
 *           type: string
 *           description: data return from S3 Service
 */
