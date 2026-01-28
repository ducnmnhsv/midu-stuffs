/**
 * @swagger
 * components:
 *   schemas:
 *     SecuritiesResponse:
 *       type: object
 *       properties:
 *         instrumentCode:
 *           type: string
 *           description: the code of the instrument
 *         cfiCode:
 *           type: string
 *           description: the CFI Code of the instrument
 *         currency:
 *           type: string
 *           description: the currency of the instrument, default is 'VND'
 *         securityExchange:
 *           type: string
 *           description: the security exchange of the instrument
 *         securityDescription:
 *           type: string
 *           description: the security description of the instrument
 *         roundLot:
 *           type: integer
 *           description: the round lot of the instrument
 *         minTradeVolume:
 *           type: integer
 *           description: the min trade volume of the instrument
 *         contractMultiplier:
 *           type: integer
 *           description: the contract multiplier of the instrument
 *         maturityMonthYear:
 *           type: string
 *           description: the maturity month & year of the instrument, format is 'yyyyMM'
 *         maturityDate:
 *           type: string
 *           description: the maturity date of the instrument, format is 'yyyyMMdd'
 *     MarketDataResponse:
 *       type: object
 *       properties:
 *         instrumentCode:
 *           type: string
 *           description: the code of the instrument
 *         bidPrice:
 *           type: number
 *           format: double
 *           description: the best bid price of the instrument
 *         offerPrice:
 *           type: number
 *           format: double
 *           description: the best offer price of the instrument
 *         lastPrice:
 *           type: number
 *           format: double
 *           description: the last price of the instrument
 *         openPrice:
 *           type: number
 *           format: double
 *           description: the open price of the instrument
 *         closePrice:
 *           type: number
 *           format: double
 *           description: the close price of the instrument
 *         settlePrice:
 *           type: number
 *           format: double
 *           description: the settle (reference) price of the instrument
 *         highPrice:
 *           type: number
 *           format: double
 *           description: the highest price of the instrument
 *         lowPrice:
 *           type: number
 *           format: double
 *           description: the lowest price of the instrument
 *         tradeVolume:
 *           type: integer
 *           description: the trading volume of the instrument
 *         openInterest:
 *           type: integer
 *           description: the open interest of the instrument
 *         lastUpdatedTime:
 *           type: string
 *           description: the last updated time of the instrument, format is 'yyyyMMddhhmmss'
 */
