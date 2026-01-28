/**
 * @swagger
 * components:
 *   schemas:
 *     EquityCashTransferHistoryResponse:
 *       type: object
 *       properties:
 *         transactionDate:
 *           type: string
 *           description: the date that implemented this transfer, format is 'yyyyMMdd'
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this transfer
 *         transferSequenceNumber:
 *           type: integer
 *           description: the transfer sequence number of this transfer
 *         sendSequenceNumber:
 *           type: integer
 *           description: the send sequence number of this transfer
 *         receiveSequenceNumber:
 *           type: integer
 *           description: the receive sequence number of this transfer
 *         accountNumber:
 *           type: string
 *           description: the account number used for this transfer
 *         subNumber:
 *           type: string
 *           description: the transfering sub number
 *         receivedAccountNumber:
 *           type: string
 *           description: the received account number
 *         receivedSubNumber:
 *           type: string
 *           description: the received sub number
 *         receivedAccountName:
 *           type: string
 *           description: the received account name
 *         amount:
 *           type: number
 *           format: double
 *           description: the transfer amount
 *         note:
 *           type: string
 *           description: the note of this transaction
 *         isCancel:
 *           type: boolean
 *           description: the transaction is cancelled or not
 *     EquityCashTransferAccountResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number to be able to receive cash
 *         subNumber:
 *           type: string
 *           description: the registered sub number
 *         accountName:
 *           type: string
 *           description: the account name of the account
 *     EquityCashTransferRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number for cash transfer
 *         subNumber:
 *           type: string
 *           description: the sub number for cash transfer, default is 00 if not set
 *         receivedAccountNumber:
 *           type: string
 *           description: the account number for receiving cash, can be NULL if same as transfering account
 *         receivedSubNumber:
 *           type: string
 *           description: the sub number for receiving cash
 *         amount:
 *           type: number
 *           format: double
 *           description: the transfer amount
 *         note:
 *           type: string
 *           description: the note of this transaction
 *     EquityCashCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number for cash transfer
 *         subNumber:
 *           type: string
 *           description: the sub number for cash transfer, default is 00 if not set
 *         sequenceNumber:
 *           type: string
 *           description: the sequence number of cash transfer request
 *         sendSequenceNumber:
 *           type: string
 *           description: the send sequence number of cash transfer request
 *         receiveSequenceNumber:
 *           type: string
 *           description: the receive sequence number of cash transfer request
 *         receivedAccountNumber:
 *           type: string
 *           description: the account number for receiving cash
 *         receivedSubNumber:
 *           type: string
 *           description: the sub number for receiving cash
 *         amount:
 *           type: number
 *           format: double
 *           description: the transfer amount
 *         note:
 *           type: string
 *           description: the note of this transaction
 *     EquityStockTransferHistoryResponse:
 *       type: object
 *       properties:
 *         transactionDate:
 *           type: string
 *           description: the date that implemented this transfer, format is 'yyyyMMdd'
 *         sequenceNumber:
 *           type: integer
 *           description: the sequence number of this transfer
 *         accountNumber:
 *           type: string
 *           description: the accountNumber used for this transfer
 *         subNumber:
 *           type: string
 *           description: the transfering sub number
 *         receivedSubNumber:
 *           type: string
 *           description: the received sub number
 *         stockCode:
 *           type: string
 *           description: the transfering stock code
 *         quantity:
 *           type: integer
 *           description: the transfer quantity of stock
 *         note:
 *           type: string
 *           description: the note of this transaction
 *     EquityStockTransferRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number for stock transfer
 *         subNumber:
 *           type: string
 *           description: the sub number for stock transfer, default is 00 if not set
 *         receivedSubNumber:
 *           type: string
 *           description: the sub number for receiving stock
 *         stockCode:
 *           type: string
 *           description: the transfering stock code
 *         quantity:
 *           type: integer
 *           description: the transfer quantity of stock
 *         limitedQuantity:
 *           type: integer
 *           description: the transfer quantity of stock but cannot trade
 *         note:
 *           type: string
 *           description: the note of this transaction
 *     EquityStockTransferBalanceResponse:
 *       type: object
 *       properties:
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         availableQuantity:
 *           type: integer
 *           description: the available quantity that can transfer
 *         limitAvailableQuantity:
 *           type: integer
 *           description: the limit available quantity that can transfer
 */
