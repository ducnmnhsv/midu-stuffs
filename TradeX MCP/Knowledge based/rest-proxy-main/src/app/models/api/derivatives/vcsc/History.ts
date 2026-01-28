/**
 * @swagger
 * components:
 *   schemas:
 *     DerivativesTradeHistoryResponse:
 *       type: object
 *       properties:
 *         totalBuyQuantity:
 *           type: integer
 *           description: total buy quantity
 *         totalSellQuantity:
 *           type: integer
 *           description: total sell quantity
 *         totalQuantity:
 *           type: integer
 *           description: total trading quantity
 *         totalFee:
 *           type: number
 *           format: double
 *           description: total trading fee
 *         totalTax:
 *           type: number
 *           format: double
 *           description: total trading tax
 *         tradingItems:
 *           type: array
 *           description: the list of trade history
 *           items:
 *             $ref: '#/components/schemas/DerivativesTradeHistoryItemResponse'
 *     DerivativesTradeHistoryItemResponse:
 *       type: object
 *       properties:
 *         tradingDate:
 *           type: string
 *           description: the trading date, format is 'yyyyMMdd'
 *         tradingTime:
 *           type: string
 *           description: the time that placed real order, format is `hhmmss`
 *         orderNumber:
 *           type: string
 *           description: the order number got from Exchange
 *         originalOrderNumber:
 *           type: string
 *           description: the order number generated from System
 *         code:
 *           type: string
 *           description: the derivatives code
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
 *           description: the matched price of the position
 *         matchedAmount:
 *           type: number
 *           format: double
 *           description: the matched amount of the order
 *         fee:
 *           type: number
 *           format: double
 *           description: the trading fee of the order
 *         tax:
 *           type: number
 *           format: double
 *           description: the trading tax of the order
 *         username:
 *           type: string
 *           description: username that place the order
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *     DerivativesPositionHistoryResponse:
 *       type: object
 *       properties:
 *         tradingDate:
 *           type: string
 *           description: the trading date of the position, format is 'yyyyMMdd'
 *         maturityDate:
 *           type: string
 *           description: the maturity date of the position, format is 'yyyyMMdd'
 *         code:
 *           type: string
 *           description: the derivatives code
 *         sellBuyType:
 *           type: string
 *           description: sell or buy order
 *           enum:
 *             - BUY
 *             - SELL
 *         quantity:
 *           type: integer
 *           description: the quantity of the position
 *         matchedPrice:
 *           type: number
 *           format: double
 *           description: the matched price of the position
 *         lastPrice:
 *           type: number
 *           format: double
 *           description: the last price of the derivatives
 *         unrealizedPL:
 *           type: number
 *           format: double
 *           description: the unrealized profit/loss of account
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *     DerivativesMarginCallHistoryResponse:
 *       type: object
 *       properties:
 *         date:
 *           type: string
 *           description: the date of margin call, format is 'yyyyMMdd'
 *         marginRequirement:
 *           type: number
 *           format: double
 *           description: the margin requirement of account
 *         previousDepositBalance:
 *           type: number
 *           format: double
 *           description: the previous day deposit balance of account
 *         previousAssignedCAA:
 *           type: number
 *           format: double
 *           description: the previous day assigned collateral amount of account
 *         previousMarginDeficit:
 *           type: number
 *           format: double
 *           description: the previous day margin deficit amount of account
 *         depositBalance:
 *           type: number
 *           format: double
 *           description: the deposit balance of account on the date
 *         assignedCAA:
 *           type: number
 *           format: double
 *           description: the assigned collateral amount of account on the date
 *         marginAmount:
 *           type: number
 *           format: double
 *           description: the margin amount of account on the date
 *         netMarginCall:
 *           type: number
 *           format: double
 *           description: the net margin call amount of account on the date
 *         isResolved:
 *           type: boolean
 *           description: the margin call is resolved or not
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *     DerivativesSettlementHistoryResponse:
 *       type: object
 *       properties:
 *         tradingDate:
 *           type: string
 *           description: the trading date of the settlement, format is 'yyyyMMdd'
 *         settleDate:
 *           type: string
 *           description: the settle date of the settlement, format is 'yyyyMMdd'
 *         variationMargin:
 *           type: number
 *           format: double
 *           description: the variation margin of settlement
 *         fee:
 *           type: number
 *           format: double
 *           description: the fee amount of settlement
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of settlement
 *         depositBalance:
 *           type: number
 *           format: double
 *           description: the deposit balance of account
 *         deficitAmount:
 *           type: number
 *           format: double
 *           description: the deficit amount of account
 *         variationMarginStatus:
 *           type: boolean
 *           description: the variation margin status of settlement
 *         feeStatus:
 *           type: boolean
 *           description: the fee status of settlement
 *         taxStatus:
 *           type: boolean
 *           description: the tax status of settlement
 *         totalFee:
 *           type: number
 *           format: double
 *           description: the total fee of the settlement of account
 *     DerivativesClosedPositionResponse:
 *       type: object
 *       properties:
 *         totalQuantity:
 *           type: integer
 *           description: total closed position quantity
 *         totalRealizedPL:
 *           type: number
 *           format: double
 *           description: total realized profit/loss
 *         totalFee:
 *           type: number
 *           format: double
 *           description: total trading fee
 *         totalNetProfitLoss:
 *           type: number
 *           format: double
 *           description: total net profit/loss
 *         closedPositionItems:
 *           type: array
 *           description: the list of trade history
 *           items:
 *             $ref: '#/components/schemas/DerivativesClosedPositionItemResponse'
 *     DerivativesClosedPositionItemResponse:
 *       type: object
 *       properties:
 *         closeDate:
 *           type: string
 *           description: the close date of the position, format is 'yyyyMMdd'
 *         code:
 *           type: string
 *           description: the derivatives code
 *         quantity:
 *           type: integer
 *           description: the closed position quantity
 *         buyingPrice:
 *           type: number
 *           format: double
 *           description: the buying price of the position
 *         sellingPrice:
 *           type: number
 *           format: double
 *           description: the selling price of the position
 *         realizedPL:
 *           type: number
 *           format: double
 *           description: the realized profit/loss of the position
 *         fee:
 *           type: number
 *           format: double
 *           description: the trading fee of the position
 *         netProfitLoss:
 *           type: number
 *           format: double
 *           description: the net profit/loss of the position
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *
 */
