/**
 * @swagger
 * components:
 *   schemas:
 *     OrderStopRequest:
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
 *           description: stock code to place order
 *         bankCode:
 *           type: string
 *           description: bank using to place order, default is **9999** if not set
 *         bankAccount:
 *           type: string
 *           description: bank account using to place order
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         securitiesType:
 *           type: string
 *           enum:
 *             - STOCK
 *             - FUND
 *             - ETF
 *             - BOND
 *             - CW
 *           description: the securities type to place order
 *         sellBuyType:
 *           type: string
 *           enum:
 *             - BUY
 *             - SELL
 *           description: indicate sell or buy order
 *         orderType:
 *           type: string
 *           description: the type of the stop order
 *           enum:
 *             - STOP
 *             - STOP_LIMIT
 *         stopPrice:
 *           type: number
 *           format: double
 *           description: stop price of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price to place stop limit order
 *     OrderStopModifyRequest:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/OrderStopRequest'
 *         - properties:
 *             newStopPrice:
 *               type: number
 *               format: double
 *               description: new stop price to be updated
 *             newOrderPrice:
 *               type: number
 *               format: double
 *               description: new order price to be updated
 *     OrderStopCancelRequest:
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
 *           description: stock code to place order
 *         bankCode:
 *           type: string
 *           description: bank using to place order, default is **9999** if not set
 *         bankAccount:
 *           type: string
 *           description: bank account using to place order
 *         sellBuyType:
 *           type: string
 *           enum:
 *             - BUY
 *             - SELL
 *           description: indicate sell or buy order
 *         orderType:
 *           type: string
 *           description: the type of the stop order
 *           enum:
 *             - STOP
 *             - STOP_LIMIT
 *         stopPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *     OrderStopCancelAllRequest:
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
 *           description: stock code to place order
 *         bankCode:
 *           type: string
 *           description: bank using to place order, default is **9999** if not set
 *         bankAccount:
 *           type: string
 *           description: bank account using to place order
 *         sellBuyType:
 *           type: string
 *           enum:
 *             - BUY
 *             - SELL
 *           description: indicate sell or buy order
 *         orderType:
 *           type: string
 *           description: the type of the stop order
 *           enum:
 *             - STOP
 *             - STOP_LIMIT
 *     OrderStopHistoryResponse:
 *       type: object
 *       properties:
 *         sequence:
 *           type: number
 *           description: the unique sequence number of each stop order
 *         stockCode:
 *           type: string
 *           description: stock code to place order
 *         bankCode:
 *           type: string
 *           description: bank code used to place order
 *         bankAccount:
 *           type: string
 *           description: bank account used to place order
 *         orderQuantity:
 *           type: integer
 *           description: quantity of order
 *         sellBuyType:
 *           type: string
 *           enum:
 *             - BUY
 *             - SELL
 *           description: indicate sell or buy order
 *         orderType:
 *           type: string
 *           description: the type of the stop order
 *           enum:
 *             - STOP
 *             - STOP_LIMIT
 *         stopPrice:
 *           type: number
 *           format: double
 *           description: stop price of order
 *         orderPrice:
 *           type: number
 *           format: double
 *           description: price of order
 *         status:
 *           type: string
 *           description: the status of the stop order
 *         orderNumber:
 *           type: string
 *           description: the real order number from SEC System
 *         createTime:
 *           type: string
 *           description: the time that created the stop order
 *         orderTime:
 *           type: string
 *           description: the time that placed order, format is 'yyyyMMddhhmmss'
 *         cancelTime:
 *           type: string
 *           description: the time that cancelled order, format is 'yyyyMMddhhmmss'
 *         errorMessage:
 *           type: string
 *           description: the error message if place real order failed
 */
