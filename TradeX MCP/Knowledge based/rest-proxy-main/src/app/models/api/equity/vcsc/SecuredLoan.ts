/**
 * @swagger
 * components:
 *   schemas:
 *     EquityLoanBankResponse:
 *       type: object
 *       properties:
 *         bankCode:
 *           type: string
 *           description: the code of the loan bank
 *         bankName:
 *           type: string
 *           description: the name of the loan bank
 *     EquityLoanAvailableResponse:
 *       type: object
 *       properties:
 *         matchDate:
 *           type: string
 *           description: the matching date of the order that used for secured loan, format is 'yyyyMMdd'
 *         settleDate:
 *           type: string
 *           description: the settle date of the order that used for secured loan, format is 'yyyyMMdd'
 *         matchAmount:
 *           type: number
 *           format: double
 *           description: the match amount of the order that used for secured loan
 *         tradingFee:
 *           type: number
 *           format: double
 *           description: the trading fee of the order that used for secured loan
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of the order that used for secured loan
 *         adjustAmount:
 *           type: number
 *           format: double
 *           description: the adjust amount for doing secured loan
 *         loanPeriod:
 *           type: integer
 *           description: the available loan period
 *         feeRate:
 *           type: number
 *           format: double
 *           description: the fee rate charged for this secured loan
 *         estimatedFee:
 *           type: number
 *           format: double
 *           description: the estimated fee charged for this secured loan
 *         possibleAmount:
 *           type: number
 *           format: double
 *           description: the maximum amount for lending
 *         settleBankCode:
 *           type: string
 *           description: the code of the settle bank
 *         settleBankName:
 *           type: string
 *           description: the name of the settle bank
 *         loanOrderType:
 *           type: string
 *           description: the loan type of this secured loan
 *         loanOrderName:
 *           type: string
 *           description: the loan type name of this secured loan
 *         contractNumber:
 *           type: string
 *           description: the contract number used for this secured loan
 *     EquityLoanDetailResponse:
 *       type: object
 *       properties:
 *         matchDate:
 *           type: string
 *           description: the matching date of the order that used for secured loan, format is 'yyyyMMdd'
 *         settleDate:
 *           type: string
 *           description: the settle date of the order that used for secured loan, format is 'yyyyMMdd'
 *         stockCode:
 *           type: string
 *           description: the stock code of the order that used for secured loan
 *         matchQuantity:
 *           type: integer
 *           description: the match quantity of the order that used for secured loan
 *         matchAmount:
 *           type: number
 *           format: double
 *           description: the match amount of the order that used for secured loan
 *         tradingFee:
 *           type: number
 *           format: double
 *           description: the trading fee of the order that used for secured loan
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of the order that used for secured loan
 *         adjustAmount:
 *           type: number
 *           format: double
 *           description: the adjust amount for doing secured loan
 *         possibleAmount:
 *           type: number
 *           format: double
 *           description: the maximum amount for lending
 *         settleBankCode:
 *           type: string
 *           description: the code of the settle bank
 *         settleBankName:
 *           type: string
 *           description: the name of the settle bank
 *         loanOrderType:
 *           type: string
 *           description: the loan type name of this secured loan
 *     EquityLoanHistoryResponse:
 *       type: object
 *       properties:
 *         loanDate:
 *           type: string
 *           description: the date that registered this secured loan, format is 'yyyyMMdd'
 *         matchDate:
 *           type: string
 *           description: the matching date of the order that used for secured loan, format is 'yyyyMMdd'
 *         stockCode:
 *           type: string
 *           description: the stock code of the order that used for secured loan
 *         matchQuantity:
 *           type: integer
 *           description: the match quantity of the order that used for secured loan
 *         matchAmount:
 *           type: number
 *           format: double
 *           description: the match amount of the order that used for secured loan
 *         loanAmount:
 *           type: number
 *           format: double
 *           description: the loan amount of this secured loan
 *         loanRepayAmount:
 *           type: number
 *           format: double
 *           description: the repay amount of this secured loan
 *         loanRemainAmount:
 *           type: number
 *           format: double
 *           description: the remaining amount of this secured loan
 *         status:
 *           type: string
 *           description: the process status of this secured loan
 *         loanBankCode:
 *           type: string
 *           description: the code of the settle bank
 *         loanBankName:
 *           type: string
 *           description: the name of the settle bank
 *     EquityLoanRegisterRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number used for registering secured loan
 *         subNumber:
 *           type: string
 *           description: the sub number used for registering secured loan, default is 00 if not set
 *         loanBankCode:
 *           type: string
 *           description: the code of the lending bank
 *         settleBankCode:
 *           type: string
 *           description: the code of the bank used for paying loan
 *         matchDate:
 *           type: string
 *           description: the matching date of the order that used for secured loan, format is 'yyyyMMdd'
 *         settleDate:
 *           type: string
 *           description: the settle date of the order that used for secured loan, format is 'yyyyMMdd'
 *         stockCode:
 *           type: string
 *           description: the stock code of the order that used for secured loan
 *         matchQuantity:
 *           type: integer
 *           description: the matching quantity of the order that used for secured loan
 *         matchAmount:
 *           type: number
 *           format: double
 *           description: the matching amount of the order that used for secured loan
 *         tradingFee:
 *           type: number
 *           format: double
 *           description: the trading fee of the order that used for secured loan
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of the order that used for secured loan
 *         adjustAmount:
 *           type: number
 *           format: double
 *           description: the adjust amount for doing secured loan
 *         possibleAmount:
 *           type: number
 *           format: double
 *           description: the maximum amount for lending
 *         loanAmount:
 *           type: number
 *           format: double
 *           description: the loan amount of each contract
 *         feeRate:
 *           type: number
 *           format: double
 *           description: the fee rate charged for this secured loan
 *         loanOrderType:
 *           type: string
 *           description: the loan type of this secured loan
 *         contractNumber:
 *           type: string
 *           description: the contract number used for this secured loan
 *         totalLoanAmount:
 *           type: number
 *           format: double
 *           description: the total secured loan amount
 *     EquityLoanCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number used for registering secured loan
 *         subNumber:
 *           type: string
 *           description: the sub number used for registering secured loan, default is 00 if not set
 *         stockCode:
 *           type: string
 *           description: the stock code of the order that used for secured loan
 *         matchDate:
 *           type: string
 *           description: the matching date of the order that used for secured loan, format is 'yyyyMMdd'
 *         expireDate:
 *           type: string
 *           description: the expired date of this secured loan, format is 'yyyyMMdd'
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this secured loan
 *         loanBankCode:
 *           type: string
 *           description: the code of the lending bank
 */
