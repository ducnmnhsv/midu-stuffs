/**
 * @swagger
 * components:
 *   schemas:
 *     TVExchange:
 *       type: object
 *       properties:
 *         value:
 *           type: string
 *           description: Exchange Value
 *         name:
 *           type: string
 *           description: Exchange Name
 *         desc:
 *           type: string
 *           description: Exchange description
 *     TVSymbolType:
 *       type: object
 *       properties:
 *         value:
 *           type: string
 *           description: Symbol Type Value
 *         name:
 *           type: string
 *           description: Symbol Type Name
 *     TVConfigResponse:
 *       type: object
 *       properties:
 *         exchanges:
 *           type: array
 *           description: An array of exchange descriptors. Exchange descriptor is an object {value, name, desc}
 *           items:
 *             $ref: '#/components/schemas/TVExchange'
 *         symbols_types:
 *           type: array
 *           description: An array of filter descriptors
 *           items:
 *             $ref: '#/components/schemas/TVSymbolType'
 *         supported_resolutions:
 *           type: string
 *           description: Supported Resolution
 *           enum:
 *             - '1'
 *             - '5'
 *             - '15'
 *             - '30'
 *             - '60'
 *             - 1D
 *             - 1W
 *             - 1M
 *         supports_group_request:
 *           type: boolean
 *           description:  Set it to `true` if your data feed provides full information on symbol group only and is not able to perform symbol search or individual symbol resolve
 *         supports_marks:
 *           type: boolean
 *           description: Boolean showing whether your datafeed supports marks on bars or not
 *         supports_search:
 *           type: boolean
 *           description: Set it to `true` if your data feed supports symbol search and individual symbol resolve logic
 *         supports_timescale_marks:
 *           type: boolean
 *           description: Boolean showing whether your datafeed supports timescale marks or not
 *         supports_time:
 *           type: boolean
 *           description: Set this one to `true` if your datafeed provides server time (unix time). It is used to adjust Countdown on the Price scale
 *         futures_regex:
 *           type: string
 *           description: Set it if you want to group futures in the symbol search. This REGEX should divide an instrument name into 2 parts (a root and an expiration)
 *     TVSymbolGroupResponse:
 *       type: object
 *       properties:
 *         symbol:
 *           type: array
 *           description: the list of symbol code
 *           items:
 *             type: string
 *         description:
 *           type: array
 *           description: the list of symbol description
 *           items:
 *             type: string
 *         exchange-listed:
 *           type: string
 *           description: the listed exchange
 *         exchange-traded:
 *           type: string
 *           description: the traded exchange
 *         minmovement:
 *           type: number
 *           format: double
 *           description: the amount of price precision steps for 1 tick
 *         minmovement2:
 *           type: number
 *           format: double
 *           description: used for formatting purpose
 *         fractional:
 *           type: boolean
 *           description: used for fractional price
 *         pricescale:
 *           type: number
 *           format: double
 *           description: defines the number of decimal places
 *         has-intraday:
 *           type: boolean
 *           description: Boolean value showing whether the symbol includes intraday (minutes) historical data
 *         has-no-volume:
 *           type: boolean
 *           description: Boolean showing whether the symbol includes volume data or not
 *         type:
 *           type: array
 *           description: the list of symbol type
 *           items:
 *             type: string
 *         ticker:
 *           type: array
 *           description: the list of symbol unique identifier
 *           items:
 *             type: string
 *         timezone:
 *           type: string
 *           description: the symbol timezone
 *         session-regular:
 *           type: array
 *           description: the list of symbol session
 *           items:
 *             type: string
 *         supported-resolutions:
 *           type: string
 *           description: Supported Resolution
 *           enum:
 *             - '1'
 *             - '5'
 *             - '15'
 *             - '30'
 *             - '60'
 *             - 1D
 *             - 1W
 *             - 1M
 *         force-session-rebuild:
 *           type: boolean
 *           description: the boolean value showing whether the library should filter bars using the current trading session
 *         has-daily:
 *           type: boolean
 *           description: The boolean value showing whether data feed has its own daily resolution bars or not
 *         intraday-multipliers:
 *           type: array
 *           description: Array of resolutions (in minutes) supported directly by the data feed
 *           items:
 *             type: string
 *         volume_precision:
 *           type: integer
 *           description: Integer showing typical volume value decimal places for a particular symbol. 0 means volume is always an integer. 1 means that there might be 1 numeric character after the comma
 *         has-weekly-and-monthly:
 *           type: boolean
 *           description: The boolean value showing whether data feed has its own weekly and monthly resolution bars or not
 *         has-empty-bars:
 *           type: boolean
 *           description: The boolean value showing whether the library should generate empty bars in the session when there is no data from the data feed for this particular time
 *     TVSymbolInfoResponse:
 *       type: object
 *       properties:
 *         name:
 *           type: string
 *           description: the symbol code
 *         ticker:
 *           type: string
 *           description: the symbol unique identifier
 *         description:
 *           type: string
 *           description: the symbol description
 *         type:
 *           type: string
 *           description: the symbol type
 *           enum:
 *             - stock
 *             - index
 *             - futures
 *         session:
 *           type: string
 *           description: the symbol session
 *         exchange:
 *           type: string
 *           description: short name of the exchange where this symbol is traded
 *         listed_exchange:
 *           type: string
 *           description: short name of the exchange where this symbol is listed
 *         timezone:
 *           type: string
 *           description: the symbol timezone
 *         minmov:
 *           type: number
 *           format: double
 *           description: the amount of price precision steps for 1 tick
 *         pricescale:
 *           type: number
 *           format: double
 *           description: defines the number of decimal places
 *         minmove2:
 *           type: number
 *           format: double
 *           description: used for formatting purpose
 *         fractional:
 *           type: boolean
 *           description: used for fractional price
 *         has_intraday:
 *           type: boolean
 *           description: Boolean value showing whether the symbol includes intraday (minutes) historical data
 *         supported_resolutions:
 *           type: string
 *           description: Supported Resolution
 *           enum:
 *             - '1'
 *             - '5'
 *             - '15'
 *             - '30'
 *             - '60'
 *             - 1D
 *             - 1W
 *             - 1M
 *         intraday_multipliers:
 *           type: array
 *           description: Array of resolutions (in minutes) supported directly by the data feed
 *           items:
 *             type: string
 *         has_seconds:
 *           type: boolean
 *           description: Boolean value showing whether the symbol includes seconds in the historical data
 *         seconds_multipliers:
 *           type: array
 *           description: It is an array containing resolutions that include seconds (excluding postfix) that the data feed provides
 *           items:
 *             type: string
 *         has_daily:
 *           type: boolean
 *           description: The boolean value showing whether data feed has its own daily resolution bars or not
 *         has_weekly_and_monthly:
 *           type: boolean
 *           description: The boolean value showing whether data feed has its own weekly and monthly resolution bars or not
 *         has_empty_bars:
 *           type: boolean
 *           description: The boolean value showing whether the library should generate empty bars in the session when there is no data from the data feed for this particular time
 *         force_session_rebuild:
 *           type: boolean
 *           description: the boolean value showing whether the library should filter bars using the current trading session
 *         has_no_volume:
 *           type: boolean
 *           description: Boolean showing whether the symbol includes volume data or not
 *         volume_precision:
 *           type: integer
 *           description: Integer showing typical volume value decimal places for a particular symbol. 0 means volume is always an integer. 1 means that there might be 1 numeric character after the comma
 *         data_status:
 *           type: string
 *           description: The status code of a series with this symbol. The status is shown in the upper right corner of a chart
 *           enum:
 *             - streaming
 *             - endofday
 *             - pulsed
 *             - delayed_streaming
 *         expired:
 *           type: boolean
 *           description: Boolean value showing whether this symbol is an expired futures contract or not
 *         expiration_date:
 *           type: integer
 *           description: Unix timestamp of the expiration date. One must set this value when expired = true
 *         sector:
 *           type: string
 *           description: Sector for stocks to be displayed in the Symbol Info
 *         industry:
 *           type: string
 *           description: Industry for stocks to be displayed in the Symbol Info
 *         currency_code:
 *           type: string
 *           description: Currency to be displayed in the Symbol Info
 *     TVSymbolSearchResponse:
 *       type: object
 *       properties:
 *         symbol:
 *           type: string
 *           description: short symbol name
 *         full_name:
 *           type: string
 *           description: full symbol name
 *         description:
 *           type: string
 *           description: the symbol description
 *         type:
 *           type: string
 *           description: the symbol type
 *           enum:
 *             - stock
 *             - index
 *             - futures
 *         exchange:
 *           type: string
 *           description: short name of the exchange where this symbol is traded
 *         ticker:
 *           type: string
 *           description: the symbol unique identifier
 *     TVHistoryResponse:
 *       type: object
 *       properties:
 *         s:
 *           type: string
 *           description: Status Code
 *         t:
 *           type: array
 *           description: Bar time. Unix timestamp (UTC)
 *           items:
 *             type: integer
 *         o:
 *           type: array
 *           description: Opening price (optional)
 *           items:
 *             type: number
 *             format: double
 *         h:
 *           type: array
 *           description: High price (optional)
 *           items:
 *             type: number
 *             format: double
 *         l:
 *           type: array
 *           description: Low price (optional)
 *           items:
 *             type: number
 *             format: double
 *         c:
 *           type: array
 *           description: Closing price
 *           items:
 *             type: number
 *             format: double
 *         v:
 *           type: array
 *           description: Volume (optional)
 *           items:
 *             type: integer
 *     TVMarksResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: array
 *           description: unique mark ID
 *           items:
 *             type: string
 *         time:
 *           type: array
 *           description: unix time, UTC
 *           items:
 *             type: integer
 *         color:
 *           type: array
 *           description: red, green, blue, yellow, or css style
 *           items:
 *             type: string
 *         text:
 *           type: array
 *           description: mark popup text. HTML supported
 *           items:
 *             type: string
 *         label:
 *           type: array
 *           description: a letter to be printed on a mark. Single character
 *           items:
 *             type: string
 *         labelFontColor:
 *           type: array
 *           description: color of a letter on a mark
 *           items:
 *             type: string
 *         minSize:
 *           type: array
 *           description: minimum mark size (diameter, pixels) (default value is 5)
 *           items:
 *             type: integer
 *     TVTimescaleMarksResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: array
 *           description: unique mark ID
 *           items:
 *             type: string
 *         time:
 *           type: array
 *           description: unix time, UTC
 *           items:
 *             type: integer
 *         color:
 *           type: array
 *           description: rgba color
 *           items:
 *             type: string
 *         tooltip:
 *           type: array
 *           description: tooltip text
 *           items:
 *             type: string
 *         label:
 *           type: array
 *           description: a letter to be displayed in a circle
 *           items:
 *             type: string
 *     TVQuoteData:
 *       type: object
 *       properties:
 *         ch:
 *           type: number
 *           format: double
 *           description: price change (usually counts as an open price on a particular day)
 *         chp:
 *           type: number
 *           format: double
 *           description: price change percentage
 *         short_name:
 *           type: string
 *           description: short name of the symbol
 *         exchange:
 *           type: string
 *           description: the exchange name
 *         description:
 *           type: string
 *           description: short description of the symbol
 *         lp:
 *           type: number
 *           format: double
 *           description: last traded price
 *         ask:
 *           type: number
 *           format: double
 *           description: ask price
 *         bid:
 *           type: number
 *           format: double
 *           description: bid price
 *         spread:
 *           type: number
 *           format: double
 *           description: spread
 *         open_price:
 *           type: number
 *           format: double
 *           description: today's open
 *         high_price:
 *           type: number
 *           format: double
 *           description: today's high
 *         low_price:
 *           type: number
 *           format: double
 *           description: today's low
 *         prev_close_price:
 *           type: number
 *           format: double
 *           description: yesterday's close
 *         volume:
 *           type: integer
 *           description: today's volume
 *     TVQuoteItemData:
 *       type: object
 *       properties:
 *         s:
 *           type: string
 *           description: Status code for the request
 *           enum:
 *             - ok
 *             - error
 *         n:
 *           type: string
 *           description: Symbol name. This value must be exactly the same as in the request
 *         v:
 *           type: object
 *           $ref: '#/components/schemas/TVQuoteData'
 *           description: object, symbol quote itself
 *     TVQuotesResponse:
 *       type: object
 *       properties:
 *         s:
 *           type: string
 *           description: Status code for the request
 *           enum:
 *             - ok
 *             - error
 *         errmsg:
 *           type: string
 *           description: Error message. Should be present only when s = 'error'
 *         d:
 *           type: array
 *           description: Bar time. Unix timestamp (UTC)
 *           items:
 *             $ref: '#/components/schemas/TVQuoteItemData'
 *     TVChart:
 *       type: object
 *       properties:
 *         timestamp:
 *           type: string
 *           description: UNIX time when the chart was saved (example, 1449084321)
 *         symbol:
 *           type: string
 *           description: base symbol of the chart (example, "AA")
 *         resolution:
 *           type: string
 *           description: resolution of the chart (example, "D")
 *         id:
 *           type: string
 *           description: unique integer identifier of the chart (example, 9163)
 *         name:
 *           type: string
 *           description: chart name (example, "Test")
 *         content:
 *           type: object
 *           description: content of the chart
 *     TVChartListResponse:
 *       type: object
 *       properties:
 *         status:
 *           type: string
 *           description: Status code for the request
 *           enum:
 *             - ok
 *             - error
 *         data:
 *           type: array
 *           description: array of saved chart
 *           items:
 *             $ref: '#/components/schemas/TVChart'
 *     TVChartSaveResponse:
 *       type: object
 *       properties:
 *         status:
 *           type: string
 *           description: Status code for the request
 *           enum:
 *             - ok
 *             - error
 *         id:
 *           type: string
 *           description: unique integer identifier of the chart (example, 9163)
 *     TVChartDeleteResponse:
 *       type: object
 *       properties:
 *         status:
 *           type: string
 *           description: Status code for the request
 *           enum:
 *             - ok
 *             - error
 */
