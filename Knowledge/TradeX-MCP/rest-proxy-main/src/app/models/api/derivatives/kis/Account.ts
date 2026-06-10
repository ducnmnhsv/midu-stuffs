/**
 * @swagger
 * components:
 *   schemas:
 *     DerivativesAccountSummaryResponse:
 *       type: object
 *       properties:
 *         date:
 *           type: string
 *           description: the date to get the account summary, format is 'yyyyMMdd'
 *         previousCashBalance:
 *           type: number
 *           format: double
 *           description: the previous day cash balance of account
 *         todayCashBalance:
 *           type: number
 *           format: double
 *           description: the today cash balance of account
 *         inOutAmount:
 *           type: number
 *           format: double
 *           description: the today in/out amount of account
 *         pendingWithdrawalAmount:
 *           type: number
 *           format: double
 *           description: the pending withdrawal amount of account
 *         CAA:
 *           type: number
 *           format: double
 *           description: the collateral asset amount of account
 *         pendingWithdrawalCAA:
 *           type: number
 *           format: double
 *           description: the pending withdrawal collateral asset amount of account
 *         assetCollateralValue:
 *           type: number
 *           format: double
 *           description: the asset collateral value of account
 *         realizedPL:
 *           type: number
 *           format: double
 *           description: the realized profit/loss of account
 *         unrealizedPL:
 *           type: number
 *           format: double
 *           description: the unrealized profit/loss of account
 *         assignedCAA:
 *           type: number
 *           format: double
 *           description: the assigned collateral asset amount of account
 *         fee:
 *           type: number
 *           format: double
 *           description: the fee amount of account
 *         marginRequirement:
 *           type: number
 *           format: double
 *           description: the VSD margin requirement amount of account
 *         vcscMarginRequirement:
 *           type: number
 *           format: double
 *           description: the VCSC margin requirement amount of account
 *         unmatchedOrderMarginRequirement:
 *           type: number
 *           format: double
 *           description: the unmatched order margin requirement amount of account
 *         marginUtilization:
 *           type: number
 *           format: double
 *           description: the utilization rate of margin
 *         marginDeficit:
 *           type: number
 *           format: double
 *           description: the margin deficit amount of account
 *         availableFundForWithdraw:
 *           type: number
 *           format: double
 *           description: the available fund amount of account to be withdrawable
 *         availableFundForOrder:
 *           type: number
 *           format: double
 *           description: the available fund amount of account to place order
 *         availableFundForWithdrawCAA:
 *           type: number
 *           format: double
 *           description: the available fund amount of account to withdraw collateral asset
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of account
 *     DerivativesAccountEquityResponse:
 *       type: object
 *       properties:
 *         availableCashBalance:
 *           type: number
 *           format: double
 *           description: the available cash balance of account
 *         totalCashBalance:
 *           type: number
 *           format: double
 *           description: the total cash balance of account
 *         availableStockQuantity:
 *           type: integer
 *           description: the available stock quantity of account
 *         availableStockAmount:
 *           type: number
 *           format: double
 *           description: the available stock amount of account
 *         totalStockQuantity:
 *           type: integer
 *           description: the total stock quantity of account
 *         totalStockAmount:
 *           type: number
 *           format: double
 *           description: the total stock amount of account
 *     DerivativesAccountBalanceResponse:
 *       type: object
 *       properties:
 *         date:
 *           type: string
 *           description: the date of the data, format is 'yyyMMdd'
 *         previousCashBalance:
 *           type: number
 *           format: double
 *           description: the previous day cash balance of account
 *         depositBalance:
 *           type: number
 *           format: double
 *           description: the deposit balance of account on the date
 *         withdrawableBalance:
 *           type: number
 *           format: double
 *           description: the withdrawable balance of account on the date
 *         cashBalance:
 *           type: number
 *           format: double
 *           description: the cash balance of account on the date
 *         previousSubstituteBalance:
 *           type: number
 *           format: double
 *           description: the previous day substitute balance of account
 *         depositSubsituteBalance:
 *           type: number
 *           format: double
 *           description: the deposit substitute balance of account on the date
 *         withdrawableSubstituteBalance:
 *           type: number
 *           format: double
 *           description: the withdrawable substitute balance of account on the date
 *         substituteBalance:
 *           type: number
 *           format: double
 *           description: the substitute balance of account on the date
 *         marginRequirement:
 *           type: number
 *           format: double
 *           description: the margin requirement of account
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *     DerivativesAccountProfitLossResponse:
 *       type: object
 *       properties:
 *         closedLongQuantity:
 *           type: integer
 *           description: total closed Long position
 *         closedShortQuantity:
 *           type: integer
 *           description: total closed Short position
 *         realizedPL:
 *           type: number
 *           format: double
 *           description: the realized profit/loss of account
 *         fee:
 *           type: number
 *           format: double
 *           description: the fee amount of account
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of account
 *         netProfitLoss:
 *           type: number
 *           format: double
 *           description: the net profit/loss of account
 *         longQuantity:
 *           type: integer
 *           description: total Long position
 *         shortQuantity:
 *           type: integer
 *           description: total Short position
 *         unrealizedPL:
 *           type: number
 *           format: double
 *           description: the unrealized profit/loss of account
 *         profitLossItems:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/DerivativesAccountProfitLossItemResponse'
 *             description: the detail info of profit loss
 *     DerivativesAccountProfitLossItemResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the derivatives code
 *         lastPrice:
 *           type: number
 *           format: double
 *           description: the last price of the derivatives
 *         closedLongQuantity:
 *           type: integer
 *           description: closed Long position
 *         closedShortQuantity:
 *           type: integer
 *           description: closed Short position
 *         realizedPL:
 *           type: number
 *           format: double
 *           description: the realized profit/loss of account
 *         fee:
 *           type: number
 *           format: double
 *           description: the fee amount of account
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of account
 *         netProfitLoss:
 *           type: number
 *           format: double
 *           description: the net profit/loss of account
 *         longQuantity:
 *           type: integer
 *           description: Long position
 *         shortQuantity:
 *           type: integer
 *           description: Short position
 *         unrealizedPL:
 *           type: number
 *           format: double
 *           description: the unrealized profit/loss of account
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *     DerivativesAccountCumulativeProfitLossResponse:
 *       type: object
 *       properties:
 *         realizedPL:
 *           type: number
 *           format: double
 *           description: the realized profit/loss of account
 *         unrealizedPL:
 *           type: number
 *           format: double
 *           description: the unrealized profit/loss of account
 *         fee:
 *           type: number
 *           format: double
 *           description: the fee amount of account
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of account
 *         netProfitLoss:
 *           type: number
 *           format: double
 *           description: the net profit/loss of account
 *         profitLossItems:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/DerivativesAccountCumulativeProfitLossItemResponse'
 *             description: the detail info of cumulative profit loss
 *     DerivativesAccountCumulativeProfitLossItemResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the derivatives code
 *         date:
 *           type: string
 *           description: the date of the item, format is 'yyyyMMdd'
 *         realizedPL:
 *           type: number
 *           format: double
 *           description: the realized profit/loss of account
 *         unrealizedPL:
 *           type: number
 *           format: double
 *           description: the unrealized profit/loss of account
 *         fee:
 *           type: number
 *           format: double
 *           description: the fee amount of account
 *         tax:
 *           type: number
 *           format: double
 *           description: the tax amount of account
 *         netProfitLoss:
 *           type: number
 *           format: double
 *           description: the net profit/loss of account
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *     DerivativesAccountRiskRatioResponse:
 *       type: object
 *       properties:
 *         acceptedCollateralValue:
 *           type: number
 *           format: double
 *           description: the accepted collateral value of account
 *         initialMargin:
 *           type: number
 *           format: double
 *           description: the initial margin of account
 *         variationMargin:
 *           type: number
 *           format: double
 *           description: the variation margin of account
 *         spreadMargin:
 *           type: number
 *           format: double
 *           description: the spread margin of account
 *         initialMarginDelivery:
 *           type: number
 *           format: double
 *           description: the initial margin delivery of account
 *         marginRequirement:
 *           type: number
 *           format: double
 *           description: the margin requirement of account
 *         marginUtilization:
 *           type: number
 *           format: double
 *           description: the utilization rate of margin
 *         position:
 *           type: integer
 *           description: total position quantity of the account
 *         marginLevel:
 *           type: string
 *           description: the margin level of the account
 *     DerivativesAccountOpenPositionResponse:
 *       type: object
 *       properties:
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
 *           description: the open position quantity
 *         previousQuantity:
 *           type: integer
 *           description: the previous open position quantity
 *         averagePrice:
 *           type: number
 *           format: double
 *           description: the average price of the derivatives
 *         currentPrice:
 *           type: number
 *           format: double
 *           description: the current price of the derivatives
 *         unrealizedPL:
 *           type: number
 *           format: double
 *           description: the unrealized profit/loss of this item
 *         closableQuantity:
 *           type: integer
 *           description: the available quantity to be closed
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 *     DerivativesAccountTradingLimitResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the derivatives code
 *         tradingLimitQuantity:
 *           type: integer
 *           description: the trading limit quantity
 *         isTradingLimit:
 *           type: boolean
 *           description: has trading limit or not
 *         tickLimitQuantity:
 *           type: number
 *           format: double
 *           description: the tick limit quantity
 *         isTickLimit:
 *           type: boolean
 *           description: has tick limit or not
 *         isOrderLimit:
 *           type: boolean
 *           description: has order limit or not
 *         nextKey:
 *           type: string
 *           description: the next key of the data
 */
