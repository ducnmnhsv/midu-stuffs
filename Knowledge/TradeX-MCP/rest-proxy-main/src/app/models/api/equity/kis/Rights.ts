/**
 * @swagger
 * components:
 *   schemas:
 *     EquityRightAvailableResponse:
 *       type: object
 *       properties:
 *         stockCode:
 *           type: string
 *           description: the stock code of this right
 *         stockName:
 *           type: string
 *           description: the stock name of this right
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this deal
 *         baseDate:
 *           type: string
 *           description: last registration date, format is 'yyyyMMdd'
 *         rightStatus:
 *           type: string
 *           description: the current status of this right
 *         startDate:
 *           type: string
 *           description: start right date, format is 'yyyyMMdd'
 *         endDate:
 *           type: string
 *           description: end right date, format is 'yyyyMMdd'
 *         issuePrice:
 *           type: number
 *           format: double
 *           description: the right's issue price
 *         availableQuantity:
 *           type: integer
 *           description: the right's available quantity
 *         note:
 *           type: string
 *     EquityRightOthersResponse:
 *       type: object
 *       properties:
 *         stockCode:
 *           type: string
 *           description: the stock code of this right
 *         rightName:
 *           type: string
 *           description: the right name of this right
 *         baseDate:
 *           type: string
 *           description: last registration date, format is 'yyyyMMdd'
 *         receiptQuantity:
 *           type: integer
 *           description: the receipt quantity of this right
 *         receiptDate:
 *           type: string
 *           description: the receipt date of this right, format is 'yyyyMMdd'
 *         dividendAmount:
 *           type: number
 *           format: double
 *           description: the dividend amount of this right
 *         dividendDate:
 *           type: string
 *           description: the dividend date of this right, format is 'yyyyMMdd'
 *     EquityRightDetailResponse:
 *       type: object
 *       properties:
 *         issuePrice:
 *           type: number
 *           format: double
 *           description: the right's issue price
 *         standardQuantity:
 *           type: integer
 *           description: the right's standard quantity
 *         availableQuantity:
 *           type: integer
 *           description: the right's available quantity
 *         quantity:
 *           type: integer
 *           description: the right's quantity
 *         amount:
 *           type: number
 *           format: double
 *           description: the right's amount
 *         availableAmount:
 *           type: number
 *           format: double
 *           description: available amount to purchase rights
 *         tradeNumber:
 *           type: string
 *           description: the trade number of this deal
 *         bankApproveWaitingQuantity:
 *           type: integer
 *           description: Bank's approve waiting quantity
 *         bankCancelWaitingQuantity:
 *           type: integer
 *           description: Bank's cancel waiting quantity
 *         approveWaitingQuantity:
 *           type: integer
 *           description: Approve waiting quantity
 *         startDate:
 *           type: string
 *           description: start right date, format is 'yyyyMMdd'
 *         endDate:
 *           type: string
 *           description: end right date, format is 'yyyyMMdd'
 *         processStatusCode:
 *           type: integer
 *           description: the code of current process status of this deal
 *         processStatusName:
 *           type: string
 *           description: the name of current process status of this deal
 *         rightType:
 *           type: string
 *           description: the right type of this deal
 *     EquityRightHistoryResponse:
 *       type: object
 *       properties:
 *         stockCode:
 *           type: string
 *           description: the stock code of this right
 *         registrationDate:
 *           type: string
 *           description: the date that customer registered this right, format is 'yyyyMMdd'
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this right
 *         baseDate:
 *           type: string
 *           description: the last date that customer can register this right, format is 'yyyyMMdd'
 *         quantity:
 *           type: integer
 *           description: the right's quantity
 *         amount:
 *           type: number
 *           format: double
 *           description: the right's amount
 *         staff:
 *           type: string
 *           description: the SEC's staff that process this right for account
 *         executionTime:
 *           type: string
 *           description: the date time that SEC's staff processed, format is 'yyyyMMdd'hhmmss
 *         jobType:
 *           type: string
 *           description: the type of the process
 *         approvalStatus:
 *           type: boolean
 *           description: the approval status of this deal
 *     EquityRightRegisterRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number to register this right
 *         subNumber:
 *           type: string
 *           description: the sub number to register this right, default is 00 if not set
 *         stockCode:
 *           type: string
 *           description: the stock code of this right
 *         baseDate:
 *           type: string
 *           description: the last date that customer can register this right, format is 'yyyyMMdd'
 *         quantity:
 *           type: integer
 *           description: the right's quantity
 *         amount:
 *           type: number
 *           format: double
 *           description: the right's amount
 *         tradeNumber:
 *           type: string
 *           description: the trade number of this deal
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this right
 *         bankCode:
 *           type: string
 *           description: the code of the bank used for registering this right, default is 9999 if not set
 *         bankAccount:
 *           type: string
 *           description: the bank account used for registering this right, can be empty if bankCode is 9999
 *         rightType:
 *           type: string
 *           enum:
 *             - ADDITIONAL_STOCK
 *             - BOND
 *           description: the type of the right
 *     EquityRightCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number of the deal that to be cancelled
 *         subNumber:
 *           type: string
 *           description: the sub number of the deal that to be cancelled, default is 00 if not set
 *         stockCode:
 *           type: string
 *           description: the stock code of the right
 *         baseDate:
 *           type: string
 *           description: the last date that customer can register this right, format is 'yyyyMMdd'
 *         quantity:
 *           type: integer
 *           description: the quantity of the deal that to be cancelled
 *         amount:
 *           type: number
 *           format: double
 *           description: the amount of the deal that to be cancelled
 *         tradeNumber:
 *           type: integer
 *           description: the trade number of this deal
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this right
 *         bankCode:
 *           type: string
 *           description: the code of the bank used for cancelling this right, default is 9999 if not set
 *         bankAccount:
 *           type: string
 *           description: the bank account used for cancelling this right, can be empty if bankCode is 9999
 *         rightType:
 *           type: string
 *           enum:
 *             - ADDITIONAL_STOCK
 *             - BOND
 *           description: the type of the right
 */
