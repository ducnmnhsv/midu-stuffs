/**
 * @swagger
 * components:
 *   schemas:
 *     MarketSessionStatusResponse:
 *       type: object
 *       properties:
 *         market:
 *           type: string
 *           description: market name
 *           enum:
 *             - HOSE
 *             - HNX
 *             - UPCOM
 *         type:
 *           type: string
 *           description: system type
 *           enum:
 *             - EQUITY
 *             - DERIVATIVES
 *         time:
 *           type: string
 *           description: the time that updated data, format is `HHmmss`
 *         status:
 *           type: string
 *           description: the status of market session
 *           enum:
 *             - ATO
 *             - LO
 *             - INTERMISSION
 *             - ATC
 *             - PLO
 *             - RUNOFF
 *             - CLOSED
 *     ListedStockResponse:
 *       type: object
 *       properties:
 *         market:
 *           type: string
 *           description: market name
 *         securitiesType:
 *           type: string
 *           description: securities type of the stock data
 *         code:
 *           type: string
 *           description: stock code
 *         companyName:
 *           type: string
 *           description: company name that issues the stock
 *         companyNameEn:
 *           type: string
 *           description: English company name that issues the stock
 *     IndustryResponse:
 *       type: object
 *       properties:
 *         industryCode:
 *           type: string
 *           description: industry code
 *         industryName:
 *           type: string
 *           description: industry name
 *     MarketItemResponse:
 *       type: object
 *       properties:
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
 *           type: number
 *           format: double
 *           description: the total trading value of this item
 *     MarketItemIntervalResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this item
 *         time:
 *           type: string
 *           description: the time of data, format is 'yyyyMMddhhmmss'
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
 *         tradingVolume:
 *           type: integer
 *           description: the total trading volume of this item
 *         tradingValue:
 *           type: number
 *           format: double
 *           description: the total trading value of this item
 *         periodTradingVolume:
 *           type: integer
 *           description: the trading volume during each interval
 *         lastValue:
 *           type: number
 *           format: double
 *           description: the last value during 1 interval period (can be sequence or milliseconds using to compare with stock code)
 *     QuoteResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemResponse'
 *         - properties:
 *             time:
 *               type: string
 *               description: the time of this quote, format is 'hhmmss'
 *     IndexQuoteResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/QuoteResponse'
 *         - properties:
 *             code:
 *               type: string
 *               description: the code of the index
 *             isHighlight:
 *               type: boolean
 *               description: the top index that want to show first
 *             market:
 *               type: string
 *               description: the market of the index
 *             indexName:
 *               type: string
 *               description: index Name
 *             indexNameEn:
 *               type: string
 *               description: index Name by English
 *             priorVolume:
 *               type: integer
 *               description: the trading volume from previous trading day
 *             ptVolume:
 *               type: integer
 *               description: the put-through volume of this index
 *             upCount:
 *               type: integer
 *               description: total number of rising stock
 *             ceilingCount:
 *               type: integer
 *               description: total number of stock that rising to ceiling price
 *             downCount:
 *               type: integer
 *               description: total number of decreasing stock
 *             floorCount:
 *               type: integer
 *               description: total number of stock that decreasing to floor price
 *             unchangedCount:
 *               type: integer
 *               description: total number of of unchanged stock
 *             date:
 *               type: string
 *               description: the latest trading date, format is 'yyyyMMdd'
 *             sessions:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/SessionResponse'
 *               description: the info of all sessions
 *     StockInfoResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this stock
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
 *           description: the market of the stock
 *         industry:
 *           type: string
 *           description: the industry of the stock
 *         time:
 *           type: string
 *           description: the last updated time of the stock, format is 'yyyyMMddhhmmss'
 *         lastTradetime:
 *           type: string
 *           description: the last trade time of the stock, format is 'yyyyMMddhhmmss'
 *         ceilingPrice:
 *           type: number
 *           format: double
 *           description: the ceiling price of the stock
 *         floorPrice:
 *           type: number
 *           format: double
 *           description: the floor price of the stock
 *         referencePrice:
 *           type: number
 *           format: double
 *           description: the reference price of the stock
 *         averagePrice:
 *           type: number
 *           format: double
 *           description: the average price of the stock
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
 *           description: turnover rate of this stock
 *         parValue:
 *           type: number
 *           format: double
 *           description: par value of this stock
 *         listedQuanity:
 *           type: integer
 *           description: the quantity of listed stock
 *         foreignerTotalRoom:
 *           type: integer
 *           description: the foreigner total allowed volume of this stock
 *         foreignerCurrentRoom:
 *           type: integer
 *           description: the foreigner current allowed volume of this stock
 *         foreignerBuyVolume:
 *           type: integer
 *           description: the foreigner buy volume of this stock
 *         foreignerSellVolume:
 *           type: integer
 *           description: the foreigner sell volume of this stock
 *         bidPrice:
 *           description: the best bid price of this stock
 *           type: number
 *           format: double
 *         offerPrice:
 *           description: the best offer price of this stock
 *           type: number
 *           format: double
 *         bidOfferList:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/BidOfferResponse'
 *           description: the list of 3 first bid/offer
 *         totalOfferVolume:
 *           type: integer
 *           description: the accumulate offer volume of this stock for today
 *         totalOfferCount:
 *           type: integer
 *           description: the accumulate offer count of this stock for today
 *         totalBidVolume:
 *           type: integer
 *           description: the accumulate bid volume of this stock for today
 *         totalBidCount:
 *           type: integer
 *           description: the accumulate bid count of this stock for today
 *         rights:
 *           type: string
 *           description: the rights info of this stock
 *         highPrice52Weeks:
 *           type: number
 *           format: double
 *           description: the highest price of the stock within 52 weeks
 *         lowPrice52Weeks:
 *           type: number
 *           format: double
 *           description: the lowest price of the stock within 52 weeks
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
 *     StockQuoteResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this stock
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
 *         sequence:
 *           type: number
 *           description: the sequence number of this quote
 *     MarketItemPeriodResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemResponse'
 *         - properties:
 *             date:
 *               type: string
 *               description: the date of this data, format is 'yyyyMMdd'
 *             dayCount:
 *               type: integer
 *               description: the total trading days during the period
 *     MarketIndexPeriodResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemPeriodResponse'
 *         - properties:
 *             foreignerBuyVolume:
 *               description: the foreigner buy volume of all stocks belong to this index for today
 *               type: integer
 *             foreignerSellVolume:
 *               description: the foreigner sell volume of this stock all stocks belong to this index for today
 *               type: integer
 *     MarketStockPeriodResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemPeriodResponse'
 *         - properties:
 *             holdVolume:
 *               description: the foreigner hold volume of this stock
 *               type: integer
 *             holdRatio:
 *               description: the foreigner hold ratio of this stock
 *               type: number
 *               format: double
 *             buyableRatio:
 *               description: the foreigner buyable ratio of this stock
 *               type: number
 *               format: double
 *     IndexResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemResponse'
 *         - properties:
 *             code:
 *               type: string
 *               description: index Code
 *     IndexItemResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: index Code
 *         market:
 *           type: string
 *           description: the market of the index
 *         indexName:
 *           type: string
 *           description: index Name
 *         indexNameEn:
 *           type: string
 *           description: index Name by English
 *         isHighlight:
 *           type: boolean
 *           description: the top index that want to show first
 *     SessionResponse:
 *       type: object
 *       properties:
 *         last:
 *           type: number
 *           format: double
 *           description: the last value of this session
 *         change:
 *           type: number
 *           format: double
 *           description: the change value of this session
 *         rate:
 *           type: number
 *           format: double
 *           description: the change rate of this session
 *         tradingVolume:
 *           type: integer
 *           description: the total trading volume of this session
 *         tradingValue:
 *           type: number
 *           format: double
 *           description: the total trading value of this session
 *     IndexInfoResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemResponse'
 *         - properties:
 *             priorVolume:
 *               type: integer
 *               description: the trading volume from previous trading day
 *             ptVolume:
 *               type: integer
 *               description: the put-through volume of this index
 *             upCount:
 *               type: integer
 *               description: total number of rising stock
 *             ceilingCount:
 *               type: integer
 *               description: total number of stock that rising to ceiling price
 *             downCount:
 *               type: integer
 *               description: total number of decreasing stock
 *             floorCount:
 *               type: integer
 *               description: total number of stock that decreasing to floor price
 *             unchangedCount:
 *               type: integer
 *               description: total number of of unchanged stock
 *             date:
 *               type: string
 *               description: the latest trading date, format is 'yyyyMMdd'
 *             sessions:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/SessionResponse'
 *               description: the info of all sessions
 *     BidOfferResponse:
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
 *     StockPriceResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemResponse'
 *         - properties:
 *             code:
 *               type: string
 *               description: the code of the stock
 *             companyName:
 *               type: string
 *               description: company name that issues the stock
 *             companyNameEn:
 *               type: string
 *               description: English company name that issues the stock
 *             market:
 *               type: string
 *               description: the market of the stock
 *             industry:
 *               type: string
 *               description: the industry of the stock
 *             time:
 *               type: string
 *               description: the last updated time of the stock, format is 'hhmmss'
 *             ceilingPrice:
 *               type: number
 *               format: double
 *               description: the ceiling price of the stock
 *             floorPrice:
 *               type: number
 *               format: double
 *               description: the floor price of the stock
 *             referencePrice:
 *               type: number
 *               format: double
 *               description: the reference price of the stock
 *             averagePrice:
 *               type: number
 *               format: double
 *               description: the average price of the stock
 *             highTime:
 *               type: string
 *               description: the time of the high price, format is 'hhmmss'
 *             lowTime:
 *               type: string
 *               description: the time of the high price, format is 'hhmmss'
 *             normalVolume:
 *               type: integer
 *               description: normal trading volume
 *             normalValue:
 *               type: number
 *               format: double
 *               description: normal trading value
 *             ptVolume:
 *               type: integer
 *               description: put-through trading volume
 *             ptValue:
 *               type: number
 *               format: double
 *               description: put-through trading value
 *             priorVolume:
 *               type: integer
 *               description: trading volume from previous trading day
 *             turnoverRate:
 *               type: number
 *               format: double
 *               description: turnover rate of this stock
 *             parValue:
 *               type: number
 *               format: double
 *               description: par value of this stock
 *             listedQuanity:
 *               type: integer
 *               description: the quantity of listed stock
 *             foreignerTotalRoom:
 *               type: integer
 *               description: the foreigner total allowed volume of this stock
 *             foreignerCurrentRoom:
 *               type: integer
 *               description: the foreigner current allowed volume of this stock
 *             foreignerBuyVolume:
 *               type: integer
 *               description: the foreigner buy volume of this stock
 *             foreignerSellVolume:
 *               type: integer
 *               description: the foreigner sell volume of this stock
 *             bidPrice:
 *               description: the best bid price of this stock
 *               type: number
 *               format: double
 *             offerPrice:
 *               description: the best offer price of this stock
 *               type: number
 *               format: double
 *             bidOfferList:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/BidOfferResponse'
 *               description: the list of 3 first bid/offer
 *             totalOfferVolume:
 *               type: integer
 *               description: the accumulate offer volume of this stock for today
 *             totalOfferCount:
 *               type: integer
 *               description: the accumulate offer count of this stock for today
 *             totalBidVolume:
 *               type: integer
 *               description: the accumulate bid volume of this stock for today
 *             totalBidCount:
 *               type: integer
 *               description: the accumulate bid count of this stock for today
 *             rights:
 *               type: string
 *               description: the rights info of this stock
 *             highPrice52Weeks:
 *               type: number
 *               format: double
 *               description: the highest price of the stock within 52 weeks
 *             lowPrice52Weeks:
 *               type: number
 *               format: double
 *               description: the lowest price of the stock within 52 weeks
 *             expectedPrice:
 *               type: number
 *               format: double
 *               description: the expected price during ATO & ATC session
 *             session:
 *               type: string
 *               description: the market session
 *             estimatedData:
 *               type: object
 *               $ref: '#/components/schemas/EstimateCeilingFloorResponse'
 *               description: the estimated data for next session
 *     StockResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/MarketItemResponse'
 *         - properties:
 *             code:
 *               type: string
 *               description: stock code
 *             bidPrice1:
 *               type: number
 *               format: double
 *               description: the bid price 1 of this stock
 *             bidVolume1:
 *               type: integer
 *               description: the bid volume 1 of this stock
 *             bidPrice2:
 *               type: number
 *               format: double
 *               description: the bid price 2 of this stock
 *             bidVolume2:
 *               type: integer
 *               description: the bid volume 2 of this stock
 *             bidPrice3:
 *               type: number
 *               format: double
 *               description: the bid price 3 of this stock
 *             bidVolume3:
 *               type: integer
 *               description: the bid volume 3 of this stock
 *             offerPrice1:
 *               type: number
 *               format: double
 *               description: the offer price 1 of this stock
 *             offerVolume1:
 *               type: integer
 *               description: the offer volume 1 of this stock
 *             offerPrice2:
 *               type: integer
 *               description: the offer price 2 of this stock
 *             offerVolume2:
 *               type: integer
 *               description: the offer volume 2 of this stock
 *             offerPrice3:
 *               type: integer
 *               description: the offer price 3 of this stock
 *             offerVolume3:
 *               type: integer
 *               description: the offer volume 3 of this stock
 *             totalBidVolume:
 *               type: integer
 *               description: the accumulate bid volume of this stock for today
 *             totalBidCount:
 *               type: integer
 *               description: the accumulate bid count of this stock for today
 *             totalOfferVolume:
 *               type: integer
 *               description: the accumulate offer volume of this stock for today
 *             totalOfferCount:
 *               type: integer
 *               description: the accumulate offer count of this stock for today
 *             foreignerBuyVolume:
 *               type: integer
 *               description: the foreigner buy volume of this stock
 *             foreignerSellVolume:
 *               type: integer
 *               description: the foreigner sell volume of this stock
 *             averagePrice:
 *               type: number
 *               format: double
 *               description: the average price of this stock
 *     PutThroughAdvertiseResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this stock
 *         time:
 *           type: string
 *           description: the time that received this data, format is `hhmmss`
 *         secId:
 *           type: string
 *           description: the Securities Company Id of this put-through advertisement
 *         traderId:
 *           type: string
 *           description: the trader Id of this put-through advertisement
 *         sellBuyType:
 *           type: string
 *           description: sell or buy type of this put-through advertisement
 *         price:
 *           type: number
 *           format: double
 *           description: price of this put-through advertisement
 *         quantity:
 *           type: integer
 *           description: quantity of this put-through advertisement
 *         ptVolume:
 *           type: integer
 *           description: the total put-through volume of this stock
 *         ptValue:
 *           type: number
 *           format: double
 *           description: the total put-through value of this stock
 *         contact:
 *           type: string
 *           description: the contact of this put-through advertisement
 *         isCancel:
 *           type: boolean
 *           description: the status of this put-through advertisement
 *     PutThroughDealResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this stock
 *         time:
 *           type: string
 *           description: the time that received this data, format is `hhmmss`
 *         confirmNumber:
 *           type: string
 *           description: the contract number of the put-through deal
 *         matchPrice:
 *           type: number
 *           format: double
 *           description: the match volume of the put-through deal
 *         matchVolume:
 *           type: integer
 *           description: the match price of the put-through deal
 *         ptVolume:
 *           type: integer
 *           description: the total put-through volume of this stock
 *         ptValue:
 *           type: number
 *           format: double
 *           description: the total put-through value of this stock
 *         isCancel:
 *           type: boolean
 *           description: the status of this put-through deal
 *     ETFDailyResponse:
 *       type: object
 *       properties:
 *         etfCode:
 *           type: string
 *           description: the code of this ETF
 *         date:
 *           type: string
 *           description: the date of this data, format is `yyyyMMdd`
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
 *     ForeignerHoldResponse:
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
 *             foreignerTotalRoom:
 *               type: integer
 *               description: the foreigner total allowed volume of this stock
 *             foreignerCurrentRoom:
 *               type: integer
 *               description: the foreigner current allowed volume of this stock
 *             foreignerBuyableRatio:
 *               type: number
 *               format: double
 *               description: the buyable ratio of the foreigner
 *             foreignerHoldRatio:
 *               type: number
 *               format: double
 *               description: the hold ratio of the foreigner
 *             foreignerChangeVolume:
 *               type: integer
 *               description: the foreigner changed volume of this stock
 *             foreignerHoldVolume:
 *               type: integer
 *               description: the foreigner hold volume of this stock
 *             foreignerTradingVolume:
 *               type: integer
 *               description: the foreigner trading volume of this stock
 *     MarketStockRankingUpDownResponse:
 *       type: object
 *       properties:
 *         HOSE:
 *           type: object
 *           $ref: '#/components/schemas/MarketStockRankingUpDownItemResponse'
 *           description: the up down ranking for HOSE
 *         HNX:
 *           type: object
 *           $ref: '#/components/schemas/MarketStockRankingUpDownItemResponse'
 *           description: the up down ranking for HNX
 *         UPCOM:
 *           type: object
 *           $ref: '#/components/schemas/MarketStockRankingUpDownItemResponse'
 *           description: the up down ranking for UPCOM
 *     MarketStockRankingUpDownItemResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this stock
 *         last:
 *           type: number
 *           format: double
 *           description: the last value of this stock at the end of period
 *         open:
 *           type: number
 *           format: double
 *           description: the open value of this stock at the end of period
 *         high:
 *           type: number
 *           format: double
 *           description: the high value of this stock at the end of period
 *         low:
 *           type: number
 *           format: double
 *           description: the low value of this stock at the end of period
 *         change:
 *           type: number
 *           format: double
 *           description: the change value of this stock
 *         rate:
 *           type: number
 *           format: double
 *           description: the change rate of this stock
 *         tradingVolume:
 *           type: integer
 *           description: the today trading volume of this stock
 *         upDownChange:
 *           type: number
 *           format: double
 *           description: the up/down change of this stock during query period
 *         upDownRate:
 *           type: number
 *           format: double
 *           description: the up/down rate of this stock during query period
 *         startPrice:
 *           type: number
 *           format: double
 *           description: the price of this stock at the start of query period
 *         endPrice:
 *           type: number
 *           format: double
 *           description: the price of this stock at the end of query period
 *         ceilingFloorEqual:
 *            type: string
 *            description: the price at the end of period is equal to Ceiling or Floor or just null
 *            enum:
 *              - CEILING
 *              - FLOOR
 *     MarketStockRankingTopTradeResponse:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the code of this stock
 *         last:
 *           type: number
 *           format: double
 *           description: the last value of this stock
 *         change:
 *           type: number
 *           format: double
 *           description: the change value of this stock
 *         rate:
 *           type: number
 *           format: double
 *           description: the change rate of this stock
 *         tradingVolume:
 *           type: integer
 *           description: the today trading volume of this stock
 *         tradingValue:
 *           type: integer
 *           description: the today trading value of this stock
 *         turnoverRate:
 *           type: number
 *           format: double
 *           description: turnover rate of this stock
 */
