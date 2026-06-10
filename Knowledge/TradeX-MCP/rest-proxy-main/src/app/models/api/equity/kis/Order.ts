/**
 * @swagger
 * components:
 *   schemas:
 *     EquityOrderResponse:
 *       type: object
 *       properties:
 *         orderNumber:
 *           type: string
 *           description: the order number returned from system when placing successfully
 *     EquityAdvanceOrderResponse:
 *       type: object
 *       properties:
 *         tempOrderNumber:
 *           type: string
 *           description: the order number returned from system when placing advance order successfully
 *     EquityOrderTodayUnmatchResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number that used to place order
 *         subNumber:
 *           type: string
 *           description: sub number that used to place order
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         marketType:
 *           type: string
 *           enum:
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *           description: the market of the stock code
 *         orderDate:
 *           type: string
 *           description: the date that placed order, format is 'yyyyMMdd'
 *         orderTime:
 *           type: string
 *           description: the time that placed order, format is 'hhmmss'
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderType:
 *           type: string
 *           description: type of order
 *           enum:
 *             - LO
 *             - MP
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *         matchedQuantity:
 *           type: integer
 *           description: the matched quantity of order
 *         matchedPrice:
 *           type: number
 *           format: double
 *           description: the matched price of order
 *         matchedAmount:
 *           type: number
 *           format: double
 *           description: the matched amount of order
 *         unmatchedQuantity:
 *           type: integer
 *           description: the unmatched quantity of order
 *         orderStatus:
 *           type: string
 *           description: the status of the order
 *           enum:
 *             - RECEIPT
 *             - SEND
 *             - ORDER_CONFIRM
 *             - RECEIPT_CONFIRM
 *             - PARTIAL_FILLED
 *             - REJECT
 *         orderNumber:
 *           type: string
 *           description: the order number got from Exchange
 *         originalOrderNumber:
 *           type: string
 *           description: the order number generated from System
 *         username:
 *           type: string
 *           description: username that place this order
 *         branchCode:
 *           type: string
 *           description: the branch that place this order
 *         bankCode:
 *           type: string
 *           description: the code of the bank ussed to place this order
 *         bankName:
 *           type: string
 *           description: the name of bank used to place this order
 *     EquityOrderHistoryResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number that used to place order
 *         subNumber:
 *           type: string
 *           description: sub number that used to place order
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         orderDate:
 *           type: string
 *           description: the date that placed order, format is 'yyyyMMdd'
 *         orderTime:
 *           type: string
 *           description: the time that placed order, format is 'hhmmss'
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderType:
 *           type: string
 *           description: type of order
 *           enum:
 *             - LO
 *             - MP
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *         matchedQuantity:
 *           type: integer
 *           description: the matched quantity of order
 *         matchedPrice:
 *           type: number
 *           format: double
 *           description: the matched price of order
 *         matchedAmount:
 *           type: number
 *           format: double
 *           description: the matched amount of order
 *         unmatchedQuantity:
 *           type: integer
 *           description: the unmatched quantity of order
 *         modifyCancelType:
 *           type: string
 *           description: Has value if it's modify/cancel order
 *           enum:
 *             - SELL
 *             - BUY
 *             - MODIFY_OF_SELL
 *             - MODIFY_OF_BUY
 *             - CANCEL_OF_SELL
 *             - CANCEL_OF_BUY
 *         modifyCancelQuantity:
 *           type: integer
 *           description: Has value if it's modify/cancel order
 *         orderStatus:
 *           type: string
 *           description: the status of the order
 *           enum:
 *             - RECEIPT
 *             - SEND
 *             - ORDER_CONFIRM
 *             - RECEIPT_CONFIRM
 *             - FULL_FILLED
 *             - PARTIAL_FILLED
 *             - REJECT
 *         orderNumber:
 *           type: string
 *           description: the order number got from Exchange
 *         originalOrderNumber:
 *           type: string
 *           description: the order number generated from System
 *         username:
 *           type: string
 *           description: username that place this order
 *         branchCode:
 *           type: string
 *           description: the branch that place this order
 *         bankName:
 *           type: string
 *           description: the name of the bank ussed to place this order
 *     EquityAdvanceOrderHistoryResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number that used to place order
 *         subNumber:
 *           type: string
 *           description: sub number that used to place order
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         orderDate:
 *           type: string
 *           description: the date that placed order, format is 'yyyyMMdd'
 *         orderTime:
 *           type: string
 *           description: the time that placed order, format is 'hhmmss'
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderType:
 *           type: string
 *           description: type of order
 *           enum:
 *             - LO
 *             - MP
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *         orderStatus:
 *           type: string
 *           description: the status of the order
 *           enum:
 *             - RECEIPT
 *             - SEND
 *             - ORDER_CONFIRM
 *             - RECEIPT_CONFIRM
 *             - FULL_FILLED
 *             - PARTIAL_FILLED
 *             - REJECT
 *         orderNumber:
 *           type: string
 *           description: the order number got from Exchange
 *         username:
 *           type: string
 *           description: username that place this order
 *         branchCode:
 *           type: string
 *           description: the branch that place this order
 *         phoneNumber:
 *           type: string
 *           description: the phone number using to receive SMS when system place order
 *     EquityOrderCommonRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         subNumber:
 *           type: string
 *           description: sub number to place order, default is **00** if not set
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *     EquityOrderRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/EquityOrderCommonRequest'
 *         - properties:
 *             stockCode:
 *               type: string
 *               description: stock code to place order
 *             bankCode:
 *               type: string
 *               description: bank using to place order, default is **9999** if not set
 *             bankName:
 *               type: string
 *               description: bank name using to place order
 *             bankAccount:
 *               type: string
 *               description: bank account using to place order
 *             sellBuyType:
 *               type: string
 *               enum:
 *                 - BUY
 *                 - SELL
 *               description: indicate sell or buy order
 *             orderType:
 *               type: string
 *               enum:
 *                 - LO
 *                 - MP
 *                 - ATO
 *                 - ATC
 *                 - MOK
 *                 - MAK
 *                 - MTL
 *               description: type of order
 *             securitiesType:
 *               type: string
 *               enum:
 *                 - STOCK
 *                 - FUND
 *                 - ETF
 *                 - BOND
 *                 - CW
 *               description: the securities type to place order
 *     EquityOrderAdvanceRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/EquityOrderRequest'
 *         - properties:
 *             phoneNumber:
 *               type: string
 *               description: the phone number using to receive SMS when system place order
 *             advanceOrderDate:
 *               type: string
 *               description: the date to place order in the future, format is 'yyyyMMdd'
 *             securitiesType:
 *               type: string
 *               enum:
 *                 - STOCK
 *                 - FUND
 *                 - ETF
 *                 - BOND
 *                 - CW
 *               description: the securities type to place order
 *     EquityOrderOddlotRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/EquityOrderCommonRequest'
 *         - properties:
 *             stockCode:
 *               type: string
 *               description: stock code to place order
 *             securitiesType:
 *               type: string
 *               enum:
 *                 - STOCK
 *                 - FUND
 *                 - ETF
 *                 - BOND
 *                 - CW
 *               description: the securities type to place order
 *     EquityOrderModifyRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/EquityOrderCommonRequest'
 *         - properties:
 *             orderNumber:
 *               type: string
 *               description: the number of the order that will be modified
 *             branchCode:
 *               type: string
 *               description: the branch that modify order
 *             bankCode:
 *               type: string
 *               description: the bank code to place order
 *             bankName:
 *               type: string
 *               description: the bank name to place order
 *             bankAccount:
 *               type: string
 *               description: the bank account to place order
 *             sellBuyType:
 *               type: string
 *               description: sell or buy order
 *               enum:
 *                 - BUY
 *                 - SELL
 *             orderType:
 *               type: string
 *               description: type of order
 *               enum:
 *                 - LO
 *                 - MP
 *                 - ATO
 *                 - ATC
 *                 - MOK
 *                 - MAK
 *                 - MTL
 *             marketType:
 *               type: string
 *               enum:
 *                 - HNX
 *                 - HOSE
 *                 - UPCOM
 *               description: the market to modify order
 *     EquityOrderCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         subNumber:
 *           type: string
 *           description: sub number to place order, default is **00** if not set
 *         orderNumber:
 *           type: string
 *           description: the number of the order that will be cancelled
 *         branchCode:
 *           type: string
 *           description: the branch that cancel order
 *     EquityOrderCancelAllRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         subNumber:
 *           type: string
 *           description: sub number to place order, default is **00** if not set
 *         stockCode:
 *           type: string
 *           description: the stock code used to place order (optional)
 *         sellBuyType:
 *           type: string
 *           description: the sell buy type to place order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: the order price used to place order, corresponding to the stock code
 *         orderType:
 *           type: string
 *           description: type of order
 *           enum:
 *             - LO
 *             - MP
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *     EquityOrderModifyAllRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         subNumber:
 *           type: string
 *           description: sub number to place order, default is **00** if not set
 *         stockCode:
 *           type: string
 *           description: the stock code used to place order (optional)
 *         sellBuyType:
 *           type: string
 *           description: the sell buy type to place order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: the order price used to place order, corresponding to the stock code
 *         newOrderPrice:
 *           type: number
 *           format: double
 *           description: the new order price used to place order, corresponding to the stock code
 *         orderType:
 *           type: string
 *           description: type of order
 *           enum:
 *             - LO
 *             - MP
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *         securitiesType:
 *           type: string
 *           enum:
 *             - STOCK
 *             - FUND
 *             - ETF
 *             - BOND
 *             - CW
 *           description: the securities type to place order
 *         marketType:
 *           type: string
 *           enum:
 *             - HNX
 *             - HOSE
 *             - UPCOM
 *           description: the market to modify order
 *         bankAccount:
 *           type: string
 *           description: bank account using to place order
 *     EquityOrderOddlotCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         subNumber:
 *           type: string
 *           description: sub number to place order, default is **00** if not set
 *         orderNumber:
 *           type: string
 *           description: the number of the order that will be cancelled
 *         branchCode:
 *           type: string
 *           description: the branch that cancel order
 *         rejectNote:
 *           type: string
 *           description: the reject note of cancelling oddlot order
 *     EquityOrderAdvanceCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         subNumber:
 *           type: string
 *           description: sub number to place order, default is **00** if not set
 *         orderNumber:
 *           type: string
 *           description: the number of the order that will be cancelled
 *         advanceOrderDate:
 *           type: string
 *           description: the date to place order in the future, format is 'yyyyMMdd'
 *     EquityOrderOddlotSellableResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         subNumber:
 *           type: string
 *           description: sub number to place order, default is **00** if not set
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         balanceQuantity:
 *           type: integer
 *           description: the quantity of stock in your account
 *         sellableQuantity:
 *           type: integer
 *           description: the quantity of stock that be able to sell
 *         todayOrder:
 *           type: integer
 *           description: the quantity of stock that place order today
 *         todayBuy:
 *           type: integer
 *           description: the quantity of stock that bought today
 *         todaySell:
 *           type: integer
 *           description: the quantity of stock that sold today
 *         t1Buy:
 *           type: integer
 *           description: the quantity of stock that bought from the previous trading day
 *         t1Sell:
 *           type: integer
 *           description: the quantity of stock that sold from the previous trading day
 *         t2Buy:
 *           type: integer
 *           description: the quantity of stock that bought 2 trading days ago
 *         t2Sell:
 *           type: integer
 *           description: the quantity of stock that sold 2 trading days ago
 *     EquityOrderOddlotHistoryResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number that used to place order
 *         subNumber:
 *           type: string
 *           description: sub number that used to place order
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         orderNumber:
 *           type: string
 *           description: the number of the order
 *         orderDate:
 *           type: string
 *           description: the date that placed order, format is 'yyyyMMdd'
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         matchedQuantity:
 *           type: integer
 *           description: the matched quantity of order
 *         matchedPrice:
 *           type: number
 *           format: double
 *           description: the matched price of order
 *         unmatchedQuantity:
 *           type: integer
 *           description: the unmatched quantity of order
 *         modifyCancelType:
 *           type: string
 *           description: Has value if it's cancelled order
 *           enum:
 *             - CANCEL_OF_SELL
 *             - CANCEL_OF_BUY
 *         matchType:
 *           type: string
 *           description: the match type of the order
 *           enum:
 *             - MATCHED
 *             - UNMATCHED
 *         branchCode:
 *           type: string
 *           description: the branch that place this order
 *         cancelReason:
 *           type: string
 *           description: the cancel reason of the cancelled order
 *     EquityOrderOddlotUnmatchResponse:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number that used to place order
 *         subNumber:
 *           type: string
 *           description: sub number that used to place order
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         orderNumber:
 *           type: string
 *           description: the number of the order
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: the order price of order
 *         branchCode:
 *           type: string
 *           description: the branch that place this order
 *         status:
 *           type: string
 *           description: the status of the order
 *         bankName:
 *           type: string
 *           description: the bank name used to place this order
 */
