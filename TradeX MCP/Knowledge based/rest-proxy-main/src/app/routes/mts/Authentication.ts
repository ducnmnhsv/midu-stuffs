/**
 * @swagger
 * /login:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Login to get access token for TradeX
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               grant_type:
 *                 description: the specific grant type to authenticate API system, **password_tradex** for login with TradeX account
 *                 type: string
 *                 enum:
 *                   - password_tradex
 *               client_id:
 *                 description: this value is unique, linking with MTS and only provide for access MTS API system
 *                 type: string
 *               client_secret:
 *                 description: this value is unique, linking with MTS, only provide for access MTS API system and pair with **client_id**
 *                 type: string
 *               username:
 *                 description: TradeX username
 *                 type: string
 *               password:
 *                 description: TradeX password
 *                 type: string
 *                 format: password
 *             required:
 *               - grant_type
 *               - client_id
 *               - client_secret
 *               - username
 *               - password
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
 * /login/social:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Social login to get access token for TradeX
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               grant_type:
 *                 description: the specific grant type to authenticate using social login
 *                 type: string
 *                 enum:
 *                   - access_facebook
 *                   - access_google
 *                   - access_wechat
 *               client_id:
 *                 description: >-
 *                   this value is unique, linking with MTS and only provide for
 *                   access MTS API system
 *                 type: string
 *               client_secret:
 *                 description: >-
 *                   this value is unique, linking with MTS, only provide for
 *                   access MTS API system and pair with **client_id**
 *                 type: string
 *               access_token:
 *                 description: >-
 *                   the access token received from Social Network to verify user
 *                   info
 *                 type: string
 *               id_token:
 *                 description: the id token using for Google OAuth2
 *                 type: string
 *             required:
 *               - grant_type
 *               - client_id
 *               - client_secret
 *               - access_token
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
 * /login/domain:
 *   post:
 *     tags:
 *       - Authentication
 *     summary: Login SEC Domain using access token from SEC
 *     requestBody:
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               grant_type:
 *                 description: the specific grant type to authenticate API system, **access_domain** for login with SEC access token
 *                 type: string
 *                 enum:
 *                   - access_domain
 *               client_id:
 *                 description: this value is unique, linking with MTS and only provide for access MTS API system
 *                 type: string
 *               client_secret:
 *                 description: this value is unique, linking with MTS, only provide for access MTS API system and pair with **client_id**
 *                 type: string
 *               access_token:
 *                 description: the access token received from SEC Domain to verify user info
 *                 type: string
 *               domain:
 *                 description: the domain used for decoding access_token
 *             required:
 *               - grant_type
 *               - client_id
 *               - client_secret
 *               - access_token
 *               - domain
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
 *                 description: the specific grant type to renew access token
 *                 type: string
 *                 enum:
 *                   - refresh_token
 *               client_id:
 *                 description: this value is unique, linking with MTS and only provide for access MTS API system
 *                 type: string
 *               client_secret:
 *                 description: this value is unique, linking with MTS, only provide for access MTS API system and pair with **client_id**
 *                 type: string
 *               refresh_token:
 *                 description: token to renew access_token when it's expired
 *                 type: string
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
 *               refresh_token:
 *                 description: the refresh token need to be invalidated
 *                 type: string
 *             required:
 *               - refresh_token
 *     responses:
 *       200:
 *         description: Invalidated Refresh Token Successfully
 *       401:
 *         description: Your access token is invalid or expired
 */
