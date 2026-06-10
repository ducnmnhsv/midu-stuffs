/**
 * @swagger
 * components:
 *   schemas:
 *     EquityWithdrawBankAccountResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/BankAccountResponse'
 *         - properties:
 *             bankAccountNumber:
 *               type: string
 *               description: the bank account number to transfer money
 *             bankAccountName:
 *               type: string
 *               description: the bank account name to transfer money
 *     EquityWithdrawHistoryResponse:
 *       type: object
 *       properties:
 *         transactionDate:
 *           type: string
 *           description: the date that implemented money withdraw, format is 'yyyyMMdd'
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this money withdraw
 *         transactionType:
 *           type: string
 *           description: the type of this transaction
 *         transactionCode:
 *           type: string
 *           description: the code of this transaction
 *         bankCode:
 *           type: string
 *           description: the code of bank that received money
 *         bankName:
 *           type: string
 *           description: the name of bank that received money
 *         bankAccount:
 *           type: string
 *           description: the bank account that received money
 *         amount:
 *           type: number
 *           format: double
 *           description: the withdraw amount
 *         approver:
 *           type: string
 *           description: the SEC's staff who approve this transaction
 *         approvalDate:
 *           type: string
 *           description: the date that SEC's staff approved this transaction, format is 'yyyyMMdd'
 *         isCancel:
 *           type: boolean
 *           description: the transaction is cancelled or not
 *         note:
 *           type: string
 *           description: the note of this transaction
 *     EquityWithdrawRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number for money withdraw
 *         subNumber:
 *           type: string
 *           description: the sub number for money withdraw, default is 00 if not set
 *         amount:
 *           type: number
 *           format: double
 *           description: the withdraw amount
 *         bankCode:
 *           type: string
 *           description: the code of the bank that received money
 *         bankAccount:
 *           type: string
 *           description: the bank account that received money
 *         note:
 *           type: string
 *           description: the note of this transaction
 *     EquityWithdrawCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number used for money withdraw
 *         subNumber:
 *           type: string
 *           description: the sub number used for money withdraw, default is 00 if not set
 *         transactionType:
 *           type: string
 *           description: the type of this transaction
 *         transactionCode:
 *           type: string
 *           description: the code of this transaction
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this money withdraw
 *         amount:
 *           type: number
 *           format: double
 *           description: the withdraw amount
 *         bankCode:
 *           type: string
 *           description: the code of the bank that received money
 *         bankAccount:
 *           type: string
 *           description: the bank account that received money
 *         note:
 *           type: string
 *           description: the note of this cancel request
 */
