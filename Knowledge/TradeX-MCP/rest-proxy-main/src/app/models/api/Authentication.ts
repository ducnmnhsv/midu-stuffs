/**
 * @swagger
 * components:
 *   schemas:
 *     LoginResponse:
 *       type: object
 *       properties:
 *         accessToken:
 *           type: string
 *           description: token to access all other APIs in system
 *         refreshToken:
 *           type: string
 *           description: token to renew access_token when it's expired
 *         otpIndex:
 *           type: string
 *           description: the otp index to verify OTP from OTP Matrix
 *     RefreshTokenResponse:
 *       type: object
 *       properties:
 *         accessToken:
 *           type: string
 *           description: new access token to call API
 *     VerifyOTPResponse:
 *       type: object
 *       properties:
 *         accessToken:
 *           type: string
 *           description: token to access all other SEC APIs
 *         refreshToken:
 *           type: string
 *           description: token to renew access_token when it's expired
 *         userInfo:
 *           type: object
 *           $ref: '#/components/schemas/SECUserInfoResponse'
 *           description: SEC user information if login using SEC account
 */
