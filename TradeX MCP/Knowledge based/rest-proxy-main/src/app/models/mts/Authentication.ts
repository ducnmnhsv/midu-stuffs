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
 *         userInfo:
 *           type: object
 *           $ref: '#/components/schemas/UserInfoResponse'
 *           description: TradeX user information
 *     LoginSECResponse:
 *       type: object
 *       properties:
 *         accessToken:
 *           type: string
 *           description: access token for the first factor of SEC Authentication
 *         otpIndex:
 *           type: string
 *           description: the OTP Index in case grant_type is 'password_otp'
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
 *     RefreshTokenResponse:
 *       type: object
 *       properties:
 *         accessToken:
 *           type: string
 *           description: new access token to call API
 */
