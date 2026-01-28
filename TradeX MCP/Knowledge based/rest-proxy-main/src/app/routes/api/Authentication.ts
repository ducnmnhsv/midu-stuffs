/**
 * @swagger
 * /login:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Authentication to get access token for API
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               grant_type:
 *                 type: string
 *                 description: the specific grant type to authenticate API system, **password_otp** for login with SEC account, **client_credentials** for login without SEC account that can only access Market API
 *                 enum:
 *                   - password_otp
 *                   - client_credentials
 *               client_id:
 *                 type: string
 *                 description: this value is unique, linking with your TradeX account and only provide for access API system
 *               client_secret:
 *                 description: this value is unique, linking with your TradeX account, only provide for access API system and pair with **client_id**
 *                 type: string
 *               username:
 *                 type: string
 *                 description: it is hts id that used for logging in HTS System, required if grant_type is **password_otp**
 *               password:
 *                 type: string
 *                 format: password
 *                 description: it is hts password that used for authenticate in HTS System, required if grant_type is **password_otp**
 *             required:
 *               - grant_type
 *               - client_id
 *               - client_secret
 *     responses:
 *       200:
 *         description: Authenticate Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/LoginResponse'
 */

/**
 * @swagger
 * /login/sec/verifyOTP:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Verify OTP
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               otp_value:
 *                 type: string
 *                 description: the otp value to verify
 *             required:
 *               - otp_value
 *     responses:
 *       200:
 *         description: Verify OTP Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/VerifyOTPResponse'
 */

/**
 * @swagger
 * /loginCA:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Authentication using certificate to get access token for API
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               grant_type:
 *                 type: string
 *                 description: the specific grant type to authenticate API system, **password_ca** for login with SEC account
 *                 enum:
 *                   - password_ca
 *               client_id:
 *                 type: string
 *                 description: this value is unique, linking with your TradeX account and only provide for access API system
 *               client_secret:
 *                 description: this value is unique, linking with your TradeX account, only provide for access API system and pair with **client_id**
 *                 type: string
 *               data:
 *                 type: string
 *                 description: the base64 string data signed by CA, including username
 *             required:
 *               - grant_type
 *               - client_id
 *               - client_secret
 *               - data
 *     responses:
 *       200:
 *         description: Authenticate Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/LoginResponse'
 */

/**
 * @swagger
 * /ca/register:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Register CA for authentication
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               data:
 *                 type: string
 *                 format: base64
 *                 description: the base64 string data signed by CA to register, including username of HTS user
 *       description: Register CA Object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 */

/**
 * @swagger
 * /ca/update:
 *   put:
 *     tags:
 *       - Authentication
 *     summary: Update CA for authentication
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               data:
 *                 type: string
 *                 format: base64
 *                 description: the base64 string data signed by CA to update, including username of HTS user
 *       description: Update CA Object
 *       required: true
 *     responses:
 *       200:
 *         description: OK
 */

/**
 * @swagger
 * /ca/unregister:
 *   put:
 *     tags:
 *       - Authentication
 *     summary: Unregister CA for authentication
 *     security:
 *       - jwt: []
 *     responses:
 *       200:
 *         description: OK
 */

/**
 * @swagger
 * /refreshToken:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Renew Access Token
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               grant_type:
 *                 type: string
 *                 enum:
 *                   - refresh_token
 *                 description: the specific grant type to renew access token
 *               client_id:
 *                 type: string
 *                 description: this value is unique, linking with your TradeX account and only provide for access API system
 *               client_secret:
 *                 type: string
 *                 description: this value is unique, linking with your TradeX account, only provide for access API system and pair with **client_id**
 *               refresh_token:
 *                 type: string
 *                 description: token to renew access_token when it's expired
 *             required:
 *               - grant_type
 *               - client_id
 *               - client_secret
 *               - refresh_token
 *     responses:
 *       200:
 *         description: Refresh Token Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/RefreshTokenResponse'
 *       401:
 *         description: Your refresh token is invalid or expired
 */

/**
 * @swagger
 * /revokeToken:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Invalidate your refresh token
 *     description: Invalidate your refresh token that you cannot use it to get access token anymore, so you need to login again to get new refresh token & access token
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               refreshToken:
 *                 description: the refresh token need to be invalidated
 *                 type: string
 *             required:
 *               - refreshToken
 *     responses:
 *       200:
 *         description: Invalidated Refresh Token Successfully
 *       401:
 *         description: Your access token is invalid or expired
 */
