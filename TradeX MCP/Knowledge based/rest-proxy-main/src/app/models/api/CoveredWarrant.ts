/**
 * @swagger
 * components:
 *   schemas:
 *     CWInfoResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this covered warrant
 *         last:
 *           type: number
 *           format: double
 *           description: the last value of this covered warrant item
 *         open:
 *           type: number
 *           format: double
 *           description: the open value of this covered warrant item
 *         high:
 *           type: number
 *           format: double
 *           description: the high value of this covered warrant item
 *         low:
 *           type: number
 *           format: double
 *           description: the low value of this covered warrant item
 *         change:
 *           type: number
 *           format: double
 *           description: the change value of this covered warrant item
 *         rate:
 *           type: number
 *           format: double
 *           description: the change rate of this covered warrant item
 *         ceilingPrice:
 *           type: number
 *           format: double
 *           description: the ceiling price of the covered warrant
 *         floorPrice:
 *           type: number
 *           format: double
 *           description: the floor price of the covered warrant
 *         ceilingFloorEqual:
 *           type: string
 *           description: the price is equal to Ceiling or Floor or just null
 *           enum:
 *             - CEILING
 *             - FLOOR
 *         referencePrice:
 *           type: number
 *           format: double
 *           description: reference price of this covered warrant item
 *         averagePrice:
 *           type: number
 *           format: double
 *           description: average price of this covered warrant item
 *         excercisePrice:
 *           type: number
 *           format: double
 *           description: the price at which an underlying security can be purchased or sold when trading a call or put option
 *         exerciseRatio:
 *           type: string
 *           description: the number of covered warrant code to get 1 underlying asset
 *         breakEvent:
 *           type: number
 *           format: double
 *           description: At that point, profits are equal to the costs.
 *         impliedVolatility:
 *           type: number
 *           format: double
 *           description: expected volatility of the covered warrant's underlying asset over the life of covered warrant
 *         maturityDate:
 *           type: string
 *           description: 'the maturity date of the covered warrant, format is ''yyyyMMdd'''
 *         lastTradingDate:
 *           type: string
 *           description: 'the last trading date of the covered warrant, format is ''yyyyMMdd'''
 *         theoreticalPrice:
 *           type: number
 *           format: double
 *           description: estimated value of covered warrant derived from a mathematical model
 *         delta:
 *           type: number
 *           format: double
 *           description: how much price will be changed when underlying asset price change.
 *         underlyingAssetCode:
 *           type: string
 *           description: the underlying asset code of covered warrant
 *         underlyingAssetPrice:
 *           type: number
 *           format: double
 *           description: the price of underlying asset
 *         underlyingAssetRate:
 *           type: number
 *           format: double
 *           description: the rate of underlying asset
 *         market:
 *           type: string
 *           description: the market of the cover warranted
 *         time:
 *           type: string
 *           description: 'the last updated time of the covered warrant, format is ''yyyyMMddhhmmss'''
 *         lastTradetime:
 *           type: string
 *           description: the last trade time of the covered warrant, format is 'yyyyMMddhhmmss'
 *         highTime:
 *           type: string
 *           description: 'the time of the high price, format is ''hhmmss'''
 *         lowTime:
 *           type: string
 *           description: 'the time of the high price, format is ''hhmmss'''
 *         tradingVolume:
 *           type: integer
 *           description: trading volume of this covered warrant item
 *         tradingValue:
 *           type: integer
 *           description: the total trading value of this covered warrant item
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
 *           description: turnover rate of this covered warrant
 *
 *         listedQuanity:
 *           type: integer
 *           description: the quantity of listed covered warrant
 *         bidPrice:
 *           description: the best bid price of this covered warrant
 *           type: number
 *           format: double
 *         offerPrice:
 *           description: the best offer price of this covered warrant
 *           type: number
 *           format: double
 *         bidOfferList:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/CWBidOfferResponse'
 *           description: the list of 3 first bid/offer
 *         totalOfferVolume:
 *           type: integer
 *           description: the accumulate offer volume of this covered warrant for today
 *         totalOfferCount:
 *           type: integer
 *           description: the accumulate offer count of this covered warrant for today
 *         totalBidVolume:
 *           type: integer
 *           description: the accumulate bid volume of this covered warrant for today
 *         totalBidCount:
 *           type: integer
 *           description: the accumulate bid count of this covered warrant for today
 *         expectedPrice:
 *           type: number
 *           format: double
 *           description: the expected price during ATO & ATC session
 *         session:
 *           type: string
 *           description: the market session
 *         estimatedData:
 *           type: object
 *           $ref: '#/components/schemas/EstimateCeilingFloorResponse'
 *           description: the estimated data for next session
 *     CWBidOfferResponse:
 *       type: object
 *       properties:
 *         bidPrice:
 *           type: number
 *           format: double
 *           description: bid price
 *         bidVolume:
 *           type: integer
 *           description: the bid volume
 *         bidVolumeChange:
 *           type: integer
 *           description: the change from bid volume
 *         offerPrice:
 *           type: number
 *           format: double
 *           description: offer price
 *         offerVolume:
 *           type: integer
 *           description: the offer volume
 *         offerVolumeChange:
 *           type: integer
 *           description: the change from offer volume
 *     CWQuoteResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this covered warrant
 *         time:
 *           type: string
 *           description: 'the time of this quote, format is ''hhmmss'''
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
 *           description: the matching volume of this covered warrant
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
 *         sequence:
 *           type: number
 *           description: the sequence number of this quote
 *     CWItemResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: covered warrant code
 *         market:
 *           type: string
 *           description: market of covered warrant
 *         cwName:
 *           type: string
 *           description: covered warrant name
 *         cwNameEn:
 *           type: string
 *           description: covered warrant name by English
 */
