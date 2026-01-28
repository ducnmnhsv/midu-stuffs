/**
 * @swagger
 * components:
 *   schemas:
 *     DerivativesOrderAvailableQuantity:
 *       type: object
 *       properties:
 *         availableQuantity:
 *           type: integer
 *           description: the available quantity to place order
 *         openPosition:
 *           type: integer
 *           description: the current open position of the account
 *     DerivativesOrderUnmatchPosition:
 *       type: object
 *       properties:
 *         nonSettledBuyQuantity:
 *           type: integer
 *           description: the buy quantity is not seltted
 *         nonSettledSellQuantity:
 *           type: integer
 *           description: the sell quantity is not seltted
 *         unmatchedBuyQuantity:
 *           type: integer
 *           description: the unmatched buy quantity
 *         unmatchedSellQuantity:
 *           type: integer
 *           description: the unmatched sell quantity
 *     DerivativesOrderResponse:
 *       type: object
 *       properties:
 *         orderNumber:
 *           type: string
 *           description: the order number returned from system when placing successfully
 *     DerivativesOrderTodayUnmatchResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of the derivatives
 *         orderNumber:
 *           type: string
 *           description: the order number of the order
 *         orderStatus:
 *           type: string
 *           description: status of the order
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
 *           description: price of order
 *         orderType:
 *           type: string
 *           description: type of order
 *           enum:
 *             - LO
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *         matchedQuantity:
 *           type: integer
 *           description: the matched quantity of order
 *         unmatchedQuantity:
 *           type: integer
 *           description: the unmatched quantity of order
 *         validity:
 *           type: string
 *           description: the validity of the order
 *           enum:
 *             - DAY
 *             - GTC
 *             - GTD
 *         nextKey:
 *           type: string
 *           description: the next key of the order data
 *     DerivativesOrderHistoryResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of the derivatives
 *         orderNumber:
 *           type: string
 *           description: the order number got from Exchange
 *         originalOrderNumber:
 *           type: string
 *           description: the order number generated from System
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
 *           description: price of order
 *         orderDate:
 *           type: string
 *           description: the date that placed order, format is `yyyyMMdd`
 *         orderTime:
 *           type: string
 *           description: the time that placed order, format is `hhmmss`
 *         orderType:
 *           type: string
 *           description: type of order
 *           enum:
 *             - LO
 *             - ATO
 *             - ATC
 *             - MOK
 *             - MAK
 *             - MTL
 *         matchedQuantity:
 *           type: integer
 *           description: the matched quantity of order
 *         unmatchedQuantity:
 *           type: integer
 *           description: the unmatched quantity of order
 *         validity:
 *           type: string
 *           description: the validity of the order
 *           enum:
 *             - DAY
 *             - GTC
 *             - GTD
 *         rejectMessage:
 *           type: string
 *           description: the message if rejection
 *         nextKey:
 *           type: string
 *           description: the next key of the order data
 *     DerivativesOrderCommonRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         code:
 *           type: string
 *           description: code to place order
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *     DerivativesOrderRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/DerivativesOrderCommonRequest'
 *         - properties:
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
 *                 - ATO
 *                 - ATC
 *                 - MOK
 *                 - MAK
 *                 - MTL
 *               description: type of order
 *     DerivativesOrderAdvanceRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/DerivativesOrderRequest'
 *         - properties:
 *             orderType:
 *               type: string
 *               enum:
 *                 - LO
 *                 - ATC
 *               description: type of order
 *             advanceOrderType:
 *               type: string
 *               enum:
 *                 - AO
 *                 - CAO
 *               description: type of advance order (Advance Order or Conditional Advance Order)
 *             marketSession:
 *               type: string
 *               enum:
 *                 - ATO
 *                 - MORNING
 *                 - AFTERNOON
 *                 - ATC
 *               description: the market session to place advance order
 *             fromDate:
 *               type: string
 *               description: the date to start to place order in the future, format is `yyyyMMdd`
 *             toDate:
 *               type: string
 *               description: the date to stop to place order in the future, format is `yyyyMMdd`
 *     DerivativesOrderModifyRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/DerivativesOrderCommonRequest'
 *         - properties:
 *             orderNumber:
 *               type: string
 *               description: the number of the order that will be modified
 *             unmatchedQuantity:
 *               type: integer
 *               description: the unmatched quantity of order
 *             orderType:
 *               type: string
 *               enum:
 *                 - LO
 *                 - MOK
 *                 - MAK
 *               description: type of order
 *     DerivativesOrderModifyAllRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         futuresCode:
 *           type: string
 *           description: the futures code used to place order (optional)
 *         sellBuyType:
 *           type: string
 *           description: the sell buy type to place order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: the order price used to place order, corresponding to the futures code
 *         newOrderPrice:
 *           type: number
 *           format: double
 *           description: the new order price to place order, corresponding to the futures code
 *     DerivativesOrderCancelRequest:
 *       allOf:
 *         - $ref: '#/components/schemas/DerivativesOrderCommonRequest'
 *         - properties:
 *             orderNumber:
 *               type: string
 *               description: the number of the order that will be modified
 *             unmatchedQuantity:
 *               type: integer
 *               description: the unmatched quantity of order
 *             orderType:
 *               type: string
 *               enum:
 *                 - LO
 *                 - MOK
 *                 - MAK
 *               description: type of order
 *     DerivativesOrderCancelAllRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         futuresCode:
 *           type: string
 *           description: the futures code used to place order (optional)
 *         sellBuyType:
 *           type: string
 *           description: the sell buy type to place order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: the order price used to place order, corresponding to the futures code
 *     DerivativesOrderAdvanceCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         code:
 *           type: string
 *           description: the code of the derivatives
 *         orderNumber:
 *           type: string
 *           description: the number of the order that will be cancelled
 *         tradingDate:
 *           type: string
 *           description: the date to start to place order in the future, format is `yyyyMMdd`
 *         marketSession:
 *           type: string
 *           enum:
 *             - ATO
 *             - MORNING
 *             - AFTERNOON
 *             - ATC
 *           description: the market session to place advance order
 *     DerivativesOrderAdvanceHistoryResponse:
 *       type: object
 *       properties:
 *         date:
 *           type: string
 *           description: the created date of the order, format is `yyyyMMdd`
 *         code:
 *           type: string
 *           description: the code of the contract
 *         sequenceNumber:
 *           type: string
 *           description: the sequence number got from System
 *         orderType:
 *           type: string
 *           enum:
 *             - LO
 *             - ATC
 *           description: type of order
 *         advanceOrderType:
 *           type: string
 *           enum:
 *             - AO
 *             - CAO
 *           description: type of advance order (Advance Order or Conditional Advance Order)
 *         marketSession:
 *           type: string
 *           enum:
 *             - ATO
 *             - MORNING
 *             - AFTERNOON
 *             - ATC
 *           description: the market session to place advance order
 *         fromDate:
 *           type: string
 *           description: the date to start to place order in the future, format is `yyyyMMdd`
 *         toDate:
 *           type: string
 *           description: the date to stop to place order in the future, format is `yyyyMMdd`
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
 *           description: price of order
 *         orderNumber:
 *           type: string
 *           description: the real order number in case order is placed
 *         registeredDate:
 *           type: string
 *           description: the date time that placed advance order, format is `yyyyMMddHHmmss`
 *         operatingTime:
 *           type: string
 *           description: the date time that placed/cancel advance order, format is `yyyyMMddHHmmss`
 *         matchedQuantity:
 *           type: integer
 *           description: the matched quantity of order
 *         unmatchedQuantity:
 *           type: integer
 *           description: the unmatched quantity of order
 *         isSent:
 *           type: boolean
 *           description: order is sent or not
 *         isRegistered:
 *           type: boolean
 *           description: order is registered or not
 *         orderSendStatus:
 *           type: string
 *           description: the send status of order
 *           enum:
 *             - SENT
 *             - NOT_SENT
 *             - CANCELLED
 *             - REJECTED
 *         matchedStatus:
 *           type: string
 *           description: the matched status of order
 *           enum:
 *             - PARTIAL_MATCHED
 *             - FULLY_MATCHED
 *             - UNMATCHED
 *         averageMatchedPrice:
 *           type: number
 *           format: double
 *           description: the average price of matched quantity
 *         validity:
 *           type: string
 *           description: the validity of the order
 *           enum:
 *             - DAY
 *             - GTC
 *             - GTD
 *         username:
 *           type: string
 *           description: username that place this advance order
 *         cancelUsername:
 *           type: string
 *           description: username that cancel this advance order
 *         cancelDateTime:
 *           type: string
 *           description: the date that cancelled the order, format is `yyyyMMddhhmmss`
 *         isValid:
 *           type: boolean
 *           description: valid status of the order
 *         modifyCancelQuantity:
 *           type: integer
 *           description: Has value if it's modify/cancel order
 *         errorCode:
 *           type: string
 *           description: the error code of the order
 *         errorMessage:
 *           type: string
 *           description: the error message of the order
 *         nextKey:
 *           type: string
 *           description: the next key of the order data
 *     DerivativesOrderStopRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         code:
 *           type: string
 *           description: code to place order
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         stopPrice:
 *           type: number
 *           format: double
 *           description: stop price to place order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of limit order if place stop limit order
 *         fromDate:
 *           type: string
 *           description: the start of the period that order is being monitored, format is `yyyyMMdd`
 *         toDate:
 *           type: string
 *           description: the end of the period that order is being monitored, format is `yyyyMMdd`
 *     DerivativesOrderStopCancelRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         sequenceNumber:
 *           type: string
 *           description: sequence number to cancel
 *         createdDate:
 *           type: string
 *           description: the date that created the stop order, format is `yyyyMMdd`
 *     DerivativesOrderStopModifyRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number to place order
 *         sequenceNumber:
 *           type: string
 *           description: sequence number to cancel
 *         createdDate:
 *           type: string
 *           description: the date that created the stop order, format is `yyyyMMdd`
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         stopPrice:
 *           type: number
 *           format: double
 *           description: stop price to place order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of limit order if place stop limit order
 *         fromDate:
 *           type: string
 *           description: the start of the period that order is being monitored, format is `yyyyMMdd`
 *         toDate:
 *           type: string
 *           description: the end of the period that order is being monitored, format is `yyyyMMdd`
 *     DerivativesOrderStopHistoryResponse:
 *       type: object
 *       properties:
 *         date:
 *           type: string
 *           description: the date that created the stop order, format is `yyyyMMdd`
 *         code:
 *           type: string
 *           description: the code of the contract
 *         sequenceNumber:
 *           type: string
 *           description: the sequence number of the stop order
 *         orderType:
 *           type: string
 *           enum:
 *             - LO
 *             - ATC
 *           description: type of order
 *         fromDate:
 *           type: string
 *           description: the date to start to place order in the future, format is `yyyyMMdd`
 *         toDate:
 *           type: string
 *           description: the date to stop to place order in the future, format is `yyyyMMdd`
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         stopPrice:
 *           type: number
 *           format: double
 *           description: stop price to place order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of limit order if place stop limit order
 *         orderNumber:
 *           type: string
 *           description: the order number when placing real order
 *         tradingDate:
 *           type: string
 *           description: the date that placed real order, format is `yyyyMMdd`
 *         username:
 *           type: string
 *           description: username that place this advance order
 *         cancelUsername:
 *           type: string
 *           description: username that cancel this advance order
 *         cancelDateTime:
 *           type: string
 *           description: the date that cancelled the order, format is `yyyyMMddhhmmss`
 *         isSent:
 *           type: boolean
 *           description: order is sent or not
 *         errorCode:
 *           type: string
 *           description: the error code of the order
 *         errorMessage:
 *           type: string
 *           description: the error message of the order
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 */
