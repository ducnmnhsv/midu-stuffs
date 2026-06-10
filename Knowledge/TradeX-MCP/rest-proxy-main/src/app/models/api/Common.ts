/**
 * @swagger
 * components:
 *   schemas:
 *     MessageResponse:
 *       type: object
 *       properties:
 *         message:
 *           type: string
 *           description: the message response return after calling API
 *     SECUserInfoResponse:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *           description: hts id from HTS System
 *         identifierNumber:
 *           type: string
 *           description: the identifier number of this user
 *         accounts:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/AccountResponse'
 *           description: list of account that managed by this user
 *     AccountResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number
 *         accountName:
 *           type: string
 *           description: account owner's name
 *         accountSubs:
 *           type: array
 *           description: list of sub-account
 *           items:
 *             $ref: '#/components/schemas/AccountSubResponse'
 *     AccountSubResponse:
 *       type: object
 *       properties:
 *         subNumber:
 *           type: string
 *           description: sub number of sub-account
 *         type:
 *           type: string
 *           description: the type of the sub number
 *           enum:
 *             - EQUITY
 *             - DERIVATIVES
 *         bankAccounts:
 *           type: array
 *           description: list of bank accounts linking to this sub-account
 *           items:
 *             $ref: '#/components/schemas/BankAccountResponse'
 *     BankAccountResponse:
 *       type: object
 *       properties:
 *         bankCode:
 *           type: string
 *           description: the bank code of the bank
 *         bankName:
 *           type: string
 *           description: the name of the bank
 */
