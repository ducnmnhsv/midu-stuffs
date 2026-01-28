/**
 * @swagger
 * components:
 *   schemas:
 *     FuturesResponse:
 *       type: object
 *       properties:
 *         market:
 *           type: string
 *           description: market name
 *         code:
 *           type: string
 *           description: futures code
 *         futuresName:
 *           type: string
 *           description: the name of the futures
 *         futuresNameEn:
 *           type: string
 *           description: the English name of the futures
 *         baseCode:
 *           type: string
 *           description: the base code from equity market relate to this futures
 *         baseCodeSecuritiesType:
 *           type: string
 *           description: securities type of the base code
 *     FuturesInfoResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this futures
 *         last:
 *           type: number
 *           format: double
 *           description: the last value of this item
 *         open:
 *           type: number
 *           format: double
 *           description: the open value of this item
 *         high:
 *           type: number
 *           format: double
 *           description: the high value of this item
 *         low:
 *           type: number
 *           format: double
 *           description: the low value of this item
 *         change:
 *           type: number
 *           format: double
 *           description: the change value of this item
 *         rate:
 *           type: number
 *           format: double
 *           description: the change rate of this item
 *         tradingVolume:
 *           type: integer
 *           description: the total trading volume of this item
 *         tradingValue:
 *           type: integer
 *           description: the total trading volume of this item
 *         ceilingFloorEqual:
 *           type: string
 *           description: the price is equal to Ceiling or Floor or just null
 *           enum:
 *             - CEILING
 *             - FLOOR
 *         market:
 *           type: string
 *           description: the market of the futures
 *         time:
 *           type: string
 *           description: the last updated time of the futures, format is 'yyyyMMddhhmmss'
 *         lastTradetime:
 *           type: string
 *           description: the last trade time of the futures, format is 'yyyyMMddhhmmss'
 *         ceilingPrice:
 *           type: number
 *           format: double
 *           description: the ceiling price of the futures
 *         floorPrice:
 *           type: number
 *           format: double
 *           description: the floor price of the futures
 *         referencePrice:
 *           type: number
 *           format: double
 *           description: the reference price of the futures
 *         averagePrice:
 *           type: number
 *           format: double
 *           description: the average price of the futures
 *         highTime:
 *           type: string
 *           description: the time of the high price, format is 'hhmmss'
 *         lowTime:
 *           type: string
 *           description: the time of the high price, format is 'hhmmss'
 *         normalVolume:
 *           type: integer
 *           description: normal trading volume
 *         normalValue:
 *           type: number
 *           format: double
 *           description: normal trading value
 *         ptVolume:
 *           type: integer
 *           description: put-through trading volume
 *         ptValue:
 *           type: number
 *           format: double
 *           description: put-through trading value
 *         priorVolume:
 *           type: integer
 *           description: trading volume from previous trading day
 *         turnoverRate:
 *           type: number
 *           format: double
 *           description: turnover rate of this futures
 *         parValue:
 *           type: number
 *           format: double
 *           description: par value of this futures
 *         foreignerBidVolume:
 *           type: integer
 *           description: the foreigner bid volume of this futures
 *         foreignerBidValue:
 *           type: number
 *           format: double
 *           description: the foreigner bid value of this futures
 *         foreignerOfferVolume:
 *           type: integer
 *           description: the foreigner offer volume of this futures
 *         foreignerOfferValue:
 *           type: number
 *           format: double
 *           description: the foreigner offer value of this futures
 *         foreignerBidPtVolume:
 *           type: integer
 *           description: the foreigner put-through bid volume of this futures
 *         foreignerBidPtValue:
 *           type: number
 *           format: double
 *           description: the foreigner put-through bid value of this futures
 *         foreignerOfferPtVolume:
 *           type: integer
 *           description: the foreigner put-through offer volume of this futures
 *         foreignerOfferPtValue:
 *           type: number
 *           format: double
 *           description: the foreigner put-through offer value of this futures
 *         bidPrice:
 *           description: the best bid price of this future
 *           type: number
 *           format: double
 *         offerPrice:
 *           description: the best offer price of this future
 *           type: number
 *           format: double
 *         bidOfferList:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/BidOfferResponse'
 *           description: the list of 5 first bid/offer
 *         totalOfferVolume:
 *           type: integer
 *           description: the accumulate offer volume of this futures for today
 *         totalOfferCount:
 *           type: integer
 *           description: the accumulate offer count of this futures for today
 *         totalBidVolume:
 *           type: integer
 *           description: the accumulate bid volume of this futures for today
 *         totalBidCount:
 *           type: integer
 *           description: the accumulate bid count of this futures for today
 *         highPrice52Weeks:
 *           type: number
 *           format: double
 *           description: the highest price of the futures within 52 weeks
 *         lowPrice52Weeks:
 *           type: number
 *           format: double
 *           description: the lowest price of the futures within 52 weeks
 *         firstTradeDate:
 *           type: string
 *           description: the first trade date of the futures, format is 'yyyyMMdd'
 *         endTradeDate:
 *           type: string
 *           description: the end trade date of the futures, format is 'yyyyMMdd'
 *         remainingDays:
 *           type: integer
 *           description: the remaining day to trade of the futures
 *         mBasis:
 *           description: the market basis of this futures
 *           type: number
 *           format: double
 *         tBasis:
 *           description: the theoretical basis of this futures
 *           type: number
 *           format: double
 *         tPrice:
 *           description: the theoretical price of this futures
 *           type: number
 *           format: double
 *         disparity:
 *           description: the disparity of this futures
 *           type: number
 *           format: double
 *         disparityRate:
 *           description: the disparity rate of this futures
 *           type: number
 *           format: double
 *         openInterest:
 *           description: the open interest of this futures
 *           type: integer
 *         openInterestChange:
 *           description: the open interest change of this futures
 *           type: integer
 *         baseCode:
 *           description: the base code of the futures
 *           type: string
 *         expectedPrice:
 *           type: number
 *           format: double
 *           description: the expected price during ATO & ATC session
 *         session:
 *           type: string
 *           description: the market session
 *     FuturesQuoteResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this futures
 *         time:
 *           type: string
 *           description: the time of this quote, format is 'hhmmss'
 *         last:
 *           type: number
 *           format: double
 *           description: the last value of this item
 *         open:
 *           type: number
 *           format: double
 *           description: the open value of this item
 *         high:
 *           type: number
 *           format: double
 *           description: the high value of this item
 *         low:
 *           type: number
 *           format: double
 *           description: the low value of this item
 *         change:
 *           type: number
 *           format: double
 *           description: the change value of this item
 *         rate:
 *           type: number
 *           format: double
 *           description: the change rate of this item
 *         tradingVolume:
 *           type: integer
 *           description: the total trading volume of this item
 *         matchingVolume:
 *           type: integer
 *           description: the matching volume of this stock
 *         matchedBy:
 *           type: string
 *           description: this quote matched by Bid or Offer order
 *           enum:
 *             - BID
 *             - OFFER
 *         ceilingFloorEqual:
 *           type: string
 *           description: the price is equal to Ceiling or Floor or just null
 *           enum:
 *             - CEILING
 *             - FLOOR
 *         mBasis:
 *           description: the market basis of this futures
 *           type: number
 *           format: double
 *         tBasis:
 *           description: the theoretical basis of this futures
 *           type: number
 *           format: double
 *         sequence:
 *           type: number
 *           description: the sequence number of this quote
 *     MarketFuturesPeriodResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemPeriodResponse'
 *         - properties:
 *             mBasis:
 *               description: the market basis of this futures
 *               type: number
 *               format: double
 *             tBasis:
 *               description: the theoretical basis of this futures
 *               type: number
 *               format: double
 *             tPrice:
 *               description: the theoretical price of this futures
 *               type: number
 *               format: double
 *             openInterest:
 *               description: the open interest of this futures
 *               type: integer
 *     MarketFuturesForeignerDataResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemResponse'
 *         - properties:
 *             date:
 *               type: string
 *               description: the date of the data, format is 'yyyyMMdd'
 *             foreignerBuyVolume:
 *               type: integer
 *               description: the buy volume from foreigner
 *             foreignerSellVolume:
 *               type: integer
 *               description: the sell volume from foreigner
 */
