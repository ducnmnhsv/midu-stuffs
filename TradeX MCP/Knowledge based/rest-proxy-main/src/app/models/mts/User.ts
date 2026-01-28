/**
 * @swagger
 * components:
 *   schemas:
 *     UserExistenceResponse:
 *       type: object
 *       properties:
 *         isExisted:
 *           type: boolean
 *           description: already having TradeX account with this username or not
 *         isSocialLoggedIn:
 *           type: boolean
 *           description: already logging in using Social log-in or not
 *         socialType:
 *           type: string
 *           description: the social log in type of the username
 *     UserInfoResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the unique id of TradeX user
 *         username:
 *           type: string
 *           description: username that used for logging in TradeX System
 *         displayName:
 *           type: string
 *           description: display name that displayed for another TradeX user
 *         email:
 *           type: string
 *           description: email used for contact
 *         avatar:
 *           type: string
 *           description: avatar image url of TradeX user
 *         phoneNumber:
 *           type: string
 *           description: phone number of TradeX user
 *         birthday:
 *           type: string
 *           description: the birthday of TradeX user, format is 'yyyyMMdd'
 *         status:
 *           type: string
 *           enum:
 *             - ACTIVE
 *             - INACTIVE
 *             - LOCK
 *           description: the status of user
 *     RegisterRequest:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *           description: username that used for logging in TradeX System
 *         password:
 *           type: string
 *           format: password
 *           description: TradeX password that used for authenticate in TradeX System
 *         displayName:
 *           type: string
 *           description: display name that displayed for another TradeX user
 *         email:
 *           type: string
 *           description: email used for contact
 *         avatar:
 *           type: string
 *           description: avatar image url of TradeX user
 *         phoneCode:
 *           type: string
 *           description: phone code of TradeX user
 *         phoneNumber:
 *           type: string
 *           description: phone number of TradeX user
 *         birthday:
 *           type: string
 *           description: the birthday of TradeX user, format is 'yyyyMMdd'
 *     ActivateRequest:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *           description: username that used for logging in TradeX System
 *         activationCode:
 *           type: string
 *           description: the code used to activate account
 *     ActivateResendRequest:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *           description: username that used for logging in TradeX System
 *     UpdateProfileRequest:
 *       type: object
 *       properties:
 *         displayName:
 *           type: string
 *           description: display name that displayed for another TradeX user
 *         email:
 *           type: string
 *           description: email used for contact
 *         avatar:
 *           type: string
 *           description: avatar image url of TradeX user
 *         phoneNumber:
 *           type: string
 *           description: phone number of TradeX user
 *         birthday:
 *           type: string
 *           description: the birthday of TradeX user, format is 'yyyyMMdd'
 *     UpdatePasswordRequest:
 *       type: object
 *       properties:
 *         oldPassword:
 *           type: string
 *           format: password
 *           description: old TradeX password that used for authenticate in TradeX System
 *         newPassword:
 *           type: string
 *           format: password
 *           description: new TradeX password that used for authenticate in TradeX System
 *     ForgetPasswordRequest:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *           description: username that used for logging in TradeX System
 *     ResetPasswordRequest:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *           description: username that used for logging in TradeX System
 *         verificationCode:
 *           type: string
 *           format: password
 *           description: the verification code that sent to user email
 *         newPassword:
 *           type: string
 *           format: password
 *           description: new TradeX password that used for authenticate in TradeX System
 *     OneSignalRequest:
 *       type: object
 *       properties:
 *         playerId:
 *           type: string
 *           description: the player ID retrieved from OneSignal system
 */
