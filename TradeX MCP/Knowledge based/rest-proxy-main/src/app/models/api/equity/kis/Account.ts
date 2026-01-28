/**
 * @swagger
 * components:
 *   schemas:
 *     EquityAccountMobileResponse:
 *       type: object
 *       properties:
 *         phoneNumber:
 *           type: string
 *           description: phone number registered with the account
 *     EquityAccountBankResponse:
 *       type: object
 *       allOf:
 *         - $ref: '#/components/schemas/BankAccountResponse'
 *         - properties:
 *             bankAccount:
 *               type: string
 *               description: bank account linking to the account sub number
 *     EquityAccountInfoResponse:
 *       type: object
 *       properties:
 *         customerName:
 *           type: string
 *           description: the customer name of the account
 *         identifierNumber:
 *           type: string
 *           description: the identifier number of the investor
 *         identifierIssueDate:
 *           type: string
 *           description: the identifier issue date of the investor
 *         identifierIssuePlace:
 *           type: string
 *           description: the identifier issue place of the investor
 *         agencyCode:
 *           type: string
 *           description: the agency that manage for the customer
 *         email:
 *           type: string
 *           description: the email of the customer
 *         address:
 *           type: string
 *           description: the address of the customer
 *     EquityOrderPasswordRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: account number that you want to change your order password
 *         subNumber:
 *           type: string
 *           description: sub number that you want to change your order password
 *         oldPassword:
 *           type: string
 *           format: password
 *           description: your old order password (should be 4-digits number)
 *         newPassword:
 *           type: string
 *           format: password
 *           description: your new order password (should be 4-digits number)
 *     EquityHTSPasswordRequest:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *           description: hts_id that used for logging in HTS System
 *         oldPassword:
 *           type: string
 *           format: password
 *           description: old hts password that used for authenticate in HTS System
 *         newPassword:
 *           type: string
 *           format: password
 *           description: new hts password that used for authenticate in HTS System
 *     EquityCashBalanceResponse:
 *       type: object
 *       properties:
 *         depositAmount:
 *           type: number
 *           format: double
 *           description: the cash balance of account
 *         depositBlockAmount:
 *           type: number
 *           format: double
 *           description: Block because of another reason rather than placing order
 *         orderBlockAmount:
 *           type: number
 *           format: double
 *           description: Block amount because of placing Order
 *         stockEvaluationAmount:
 *           type: number
 *           format: double
 *           description: Evaluation amount of own stocks
 *         withdrawableAmount:
 *           type: number
 *           format: double
 *           description: the cash amount that customer can withdraw
 *         waitSellAmount:
 *           type: number
 *           format: double
 *           description: the wait selling amount of account
 *         virtualDeposit:
 *           type: number
 *           format: double
 *           description: available virtual deposit amount
 *         usedVirtualDeposit:
 *           type: number
 *           format: double
 *           description: used virtual deposit amount
 *         marginLoanAmount:
 *           type: number
 *           format: double
 *           description: the margin loan amount
 *         securedLoanAmount:
 *           type: number
 *           format: double
 *           description: secured loan amount
 *         expiredLoanAmount:
 *           type: number
 *           format: double
 *           description: the loan amount that is over the re-paid date
 *     EquityBuyableQuantityResponse:
 *       type: object
 *       properties:
 *         depositAmount:
 *           type: number
 *           format: double
 *           description: the cash balance of account
 *         virtualDepositAmount:
 *           type: number
 *           format: double
 *           description: available virtual deposit amount
 *         buyableQuantity:
 *           type: number
 *           format: double
 *           description: the quantity of stock that be able to buy
 *         buyingPower:
 *           type: number
 *           format: double
 *           description: the buying power of account
 *         stockValuationAmount:
 *           type: number
 *           format: double
 *           description: valuation amount of own stocks
 *         assetValuationAmount:
 *           type: number
 *           format: double
 *           description: valuation amount of total assets
 *         orderBlockAmount:
 *           type: number
 *           format: double
 *           description: Block amount because of placing order
 *         totalBlockAmount:
 *           type: number
 *           format: double
 *           description: Total block amount
 *         marginLimitation:
 *           type: number
 *           format: double
 *           description: Margin limitation of the account
 *         lackAmount:
 *           type: number
 *           format: double
 *           description: lacking amount of buying power
 *     EquitySellableStockResponse:
 *       type: object
 *       properties:
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         balanceQuantity:
 *           type: integer
 *           description: the quantity of stock in your account
 *         sellableQuantity:
 *           type: integer
 *           description: the quantity of stock that be able to sell
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
 *     EquityProfitLossItemResponse:
 *       type: object
 *       properties:
 *         stockCode:
 *           type: string
 *           description: the code of the stock
 *         balanceQuantity:
 *           type: integer
 *           description: the quantity of stock in your account
 *         sellableQuantity:
 *           type: integer
 *           description: the quantity of stock that be able to sell
 *         currentPrice:
 *           type: number
 *           format: double
 *           description: the current price of stock
 *         buyingQuantity:
 *           type: integer
 *           description: the quantity of stock that bought
 *         buyingPrice:
 *           type: number
 *           format: double
 *           description: the price of stock that bought
 *         buyingAmount:
 *           type: number
 *           format: double
 *           description: the buying amount that bought
 *         evaluationAmount:
 *           type: number
 *           format: double
 *           description: the evaluation amount that calculated by current price
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
 *         profitLoss:
 *           type: number
 *           format: double
 *           description: the profit loss amount
 *         profitLossRate:
 *           type: number
 *           format: double
 *           description: the profit loss ratio
 *     EquityProfitLossResponse:
 *       type: object
 *       properties:
 *         t1Deposit:
 *           type: number
 *           format: double
 *           description: total selling amount of stock that placed from the previous trading day
 *         t2Deposit:
 *           type: number
 *           format: double
 *           description: total selling amount of stock that placed 2 trading days ago
 *         depositAmount:
 *           type: number
 *           format: double
 *           description: the cash balance of account
 *         totalBuyAmount:
 *           type: number
 *           format: double
 *           description: the total amount that spent for buying stock
 *         totalEvaluationAmount:
 *           type: number
 *           format: double
 *           description: the total evaluation amount of all stock calculated by current price of stock
 *         totalProfitLoss:
 *           type: number
 *           format: double
 *           description: total profit loss of 1 account
 *         totalProfitLossRate:
 *           type: number
 *           format: double
 *           description: profit loss ratio of 1 account
 *         netAsset:
 *           type: number
 *           format: double
 *           description: the net asset of 1 account
 *         profitLossItems:
 *           type: array
 *           description: Profit loss info of each stock
 *           items:
 *             $ref: '#/components/schemas/EquityProfitLossItemResponse'
 *     EquityDailyProfitResponse:
 *       type: object
 *       properties:
 *         date:
 *           type: string
 *           description: the date of the item, format is 'yyyyMMdd'
 *         sellingAmount:
 *           type: number
 *           format: double
 *           description: the selling amount on the date
 *         buyingAmount:
 *           type: number
 *           format: double
 *           description: the buying amount on the date
 *         totalSellingAmount:
 *           type: number
 *           format: double
 *           description: the accumulated selling amount till the date
 *         totalBuyingAmount:
 *           type: number
 *           format: double
 *           description: the accumulated buying amount till the date
 *         sellingProfit:
 *           type: number
 *           format: double
 *           description: the profit coming from selling activities on the date
 *         buyingProfit:
 *           type: number
 *           format: double
 *           description: the profit coming from buying activities on the date
 *         totalSellingProfit:
 *           type: number
 *           format: double
 *           description: the accumulated profit coming from selling activities till the date
 *         totalBuyingProfit:
 *           type: number
 *           format: double
 *           description: the accumulated profit coming from buying activities till the date
 *         evaluatedAmount:
 *           type: number
 *           format: double
 *           description: the evaluated amount of stock balance at the end of the date
 *         priorEvaluatedAmount:
 *           type: number
 *           format: double
 *           description: the evaluated amount of stock balance at the beginning of the date
 *         stockBalanceProfit:
 *           type: number
 *           format: double
 *           description: the stock balance profit on the date
 *         totalStockBalanceProfit:
 *           type: number
 *           format: double
 *           description: the accumulated stock balance profit till the date
 *         dailyProfit:
 *           type: number
 *           format: double
 *           description: the daily profit of the sub account on the date
 *         totalProfit:
 *           type: number
 *           format: double
 *           description: the accumulated profit of the sub account till the beginning of calculation
 *         profitRatio:
 *           type: number
 *           format: double
 *           description: the profit ratio on the date
 */
