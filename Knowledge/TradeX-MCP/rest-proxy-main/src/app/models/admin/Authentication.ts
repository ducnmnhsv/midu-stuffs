/**
 * @swagger
 * components:
 *   schemas:
 *     AdminLoginResponse:
 *       type: object
 *       properties:
 *         accessToken:
 *           type: string
 *           description: token to access all other APIs in system
 *         refreshToken:
 *           type: string
 *           description: token to renew access_token when it's expired
 *         userInfo:
 *           type: object
 *           $ref: '#/components/schemas/AdminInfoResponse'
 *           description: Admin user information
 *     RefreshTokenResponse:
 *       type: object
 *       properties:
 *         accessToken:
 *           type: string
 *           description: new access token to call API
 */
