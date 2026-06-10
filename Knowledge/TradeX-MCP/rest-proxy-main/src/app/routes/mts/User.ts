/**
 * @swagger
 * /user/checkUsernameExist:
 *   get:
 *     tags:
 *       - User
 *     summary: Verify Username Existence
 *     parameters:
 *       - name: username
 *         description: username to verify existence in TradeX System
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Username existed or not
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/UserExistenceResponse'
 */

/**
 * @swagger
 * /user/register:
 *   post:
 *     tags:
 *       - User
 *     summary: Register new TradeX account
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/RegisterRequest'
 *       description: Register Object
 *       required: true
 *     responses:
 *       200:
 *         description: Register Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/UserInfoResponse'
 */

/**
 * @swagger
 * /user/activate/resend:
 *   post:
 *     tags:
 *       - User
 *     summary: Resend new activation code
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ActivateResendRequest'
 *       description: Activate Resend Object
 *       required: true
 *     responses:
 *       200:
 *         description: Resend Activate Code Successfully
 */

/**
 * @swagger
 * /user/activate:
 *   put:
 *     tags:
 *       - User
 *     summary: Activate new TradeX account
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ActivateRequest'
 *       description: Activate Account Object
 *       required: true
 *     responses:
 *       200:
 *         description: Activate Successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/LoginResponse'
 */

/**
 * @swagger
 * /user/updateProfile:
 *   put:
 *     tags:
 *       - User
 *     summary: Update TradeX profile
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/UpdateProfileRequest'
 *       description: Update Profile Object
 *       required: true
 *     responses:
 *       200:
 *         description: Update Profile Successfully
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /user/changePassword:
 *   put:
 *     tags:
 *       - User
 *     summary: Change TradeX password
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/UpdatePasswordRequest'
 *       description: Change TradeX Password object
 *       required: true
 *     responses:
 *       200:
 *         description: Change Password Successfully
 *       401:
 *         description: Your access token is invalid or expired
 */

/**
 * @swagger
 * /user/reset/forgetPassword:
 *   post:
 *     tags:
 *       - User
 *     summary: Forget TradeX password request to get Verification code
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ForgetPasswordRequest'
 *       description: Forget Password Object
 *       required: true
 *     responses:
 *       200:
 *         description: Request Forget Password Request Successfully, email with verification code sent to user
 */

/**
 * @swagger
 * /user/reset/verifyCode:
 *   get:
 *     tags:
 *       - User
 *     summary: Check Verification Code to reset password
 *     parameters:
 *       - name: username
 *         description: username to verify code
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *       - name: verificationCode
 *         description: verify code whether invalid or not
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Verification Code is valid
 */

/**
 * @swagger
 * /user/reset/resetPassword:
 *   put:
 *     tags:
 *       - User
 *     summary: Reset TradeX password
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ResetPasswordRequest'
 *       description: Reset TradeX Password object
 *       required: true
 *     responses:
 *       200:
 *         description: Reset Password Successfully
 */

/**
 * @swagger
 * /user/storeOneSignal:
 *   post:
 *     tags:
 *       - User
 *     summary: Store OneSignal Player Id into TradeX System
 *     security:
 *       - jwt: []
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/OneSignalRequest'
 *       description: OneSignal object
 *       required: true
 *     responses:
 *       200:
 *         description: Store Player Id Successfully
 *       401:
 *         description: Your access token is invalid or expired
 */
