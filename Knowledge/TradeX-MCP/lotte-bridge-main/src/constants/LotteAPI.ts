import { IAPI } from '../models/IAPI';

export const LOTTE_API_CATEGORIES = {
  LOGIN: 'login',
  ACCOUNT: 'account',
  ORDER: 'order',
  BALANCE: 'balance',
  NOTIFICATION: 'notification',
  EKYC: 'ekyc',
  MARKET: 'market',
  SOTP: 'sotp'
};

export const API: { [k: string]: IAPI } = {
  verifyUser: {
    api: 'tsol/apikey/tuxsvc/account/user/verify',
    category: LOTTE_API_CATEGORIES.LOGIN,
  },
  verifyOtp: {
    api: 'tsol/apikey/tuxsvc/account/user/urs-otp-verify',
    category: LOTTE_API_CATEGORIES.LOGIN,
  },
  changePassword: {
    api: 'tsol/apikey/tuxsvc/account/user/urs-change-password',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  changePin: {
    api: 'tsol/apikey/tuxsvc/account/user/urs-change-pin',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  resetPassword: {
    api: 'tsol/apikey/tuxsvc/account/user/get-resset-password',
    category: LOTTE_API_CATEGORIES.LOGIN,
  },
  getAccountInfo: {
    api: 'tsol/apikey/tuxsvc/account/user/get-account-information',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getAssetInfo: {
    api: 'tsol/apikey/tuxsvc/account/get-asset-info',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getDebtInfo: {
    api: 'tsol/apikey/tuxsvc/account/inquiry-debt-info',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getMarginInfo: {
    api: 'tsol/apikey/tuxsvc/account/margin-info-retrieving',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getBalanceDetails: {
    api: 'tsol/apikey/tuxsvc/account/balance-retrieve',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getBuyable: {
    api: 'tsol/apikey/tuxsvc/account/inquiry-buyable-info',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getCashBalance: {
    api: 'tsol/apikey/tuxsvc/account/cash-blc',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  registerSmartOtp: {
    api: 'tsol/apikey/tuxsvc/account/user/register-sotp',
    category: LOTTE_API_CATEGORIES.SOTP,
  },
  verifySmartOtp: {
    api: 'tsol/apikey/sotp/v2/verify-totp',
    category: LOTTE_API_CATEGORIES.SOTP,
  },
  buyNormalOrder: {
    api: 'tsol/apikey/tuxsvc/order/ord-buy',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  sellNormalOrder: {
    api: 'tsol/apikey/tuxsvc/order/ord-sell',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  cancelNormalOrder: {
    api: 'tsol/apikey/tuxsvc/order/ord-can',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  modifyNormalOrder: {
    api: 'tsol/apikey/tuxsvc/order/ord-mod',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  getTodayUnmatchOrder: {
    api: 'tsol/apikey/tuxsvc/account/get-unmatch-order',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  confirmOrder: {
    api: 'tsol/apikey/tuxsvc/order/order-confirmation',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  searchOrderConfirm: {
    api: 'tsol/apikey/tuxsvc/account/ord-confirm-history',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  getHistoryOrder: {
    api: 'tsol/apikey/tuxsvc/account/get-order-history',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  buyAdvanceOrder: {
    api: 'tsol/apikey/tuxsvc/order/adv-buy',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  sellAdvanceOrder: {
    api: 'tsol/apikey/tuxsvc/order/adv-sell',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  cancelAdvanceOrder: {
    api: 'tsol/apikey/tuxsvc/order/adv-can',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  getHistoryAdvanceOrder: {
    api: 'tsol/apikey/tuxsvc/account/adv-order-history',
    category: LOTTE_API_CATEGORIES.ORDER,
  },
  getNotification: {
    api: 'tsol/apikey/tuxsvc/account/get-account-notification',
    category: LOTTE_API_CATEGORIES.NOTIFICATION,
  },
  getMaintenanceNotification: {
    api: 'tsol/apikey/tuxsvc/account/maintenance-noti',
    category: LOTTE_API_CATEGORIES.NOTIFICATION,
  },
  getAccountLoanHistory: {
    api: 'tsol/apikey/tuxsvc/account/detail-ln',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getSellable: {
    api: 'tsol/apikey/tuxsvc/account/sellable',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getProfitLossHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/profit-loss-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getStockBalance: {
    api: 'tsol/apikey/tuxsvc/account/balance/stk-balance',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getBankList: {
    api: 'tsol/apikey/tuxsvc/account/balance/get-reg-bankaccount-list',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getWithdrawHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/get-withdrawal-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  requestWithdraw: {
    api: 'tsol/apikey/tuxsvc/account/balance/get-withdrawal',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  transferStock: {
    api: 'tsol/apikey/tuxsvc/account/balance/stock-transfer',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  transferCash: {
    api: 'tsol/apikey/tuxsvc/account/balance/get-money-transfer',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getTransferStockBalance: {
    api: 'tsol/apikey/tuxsvc/account/balance/stock-avail',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getTransferStockHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/stock-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getTransferCashHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/get-transfer-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  cancelWithdraw: {
    api: 'tsol/apikey/tuxsvc/account/balance/cancel-withdraw',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  registerLoan: {
    api: 'tsol/apikey/tuxsvc/account/balance/registering-cash-in-advance',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryLoanAvailable: {
    api: 'tsol/apikey/tuxsvc/account/balance/sell-secured-loan-available',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryLoanHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/sell-secured-loan-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryLoanDetail: {
    api: 'tsol/apikey/tuxsvc/account/balance/sell-secured-loan-detail',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightAvailable: {
    api: 'tsol/apikey/tuxsvc/account/balance/avai-subscription',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightDetail: {
    api: 'tsol/apikey/tuxsvc/account/balance/subscription-info',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/query-right-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistoryOther: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-history-a',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistoryIssue: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-history-1',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistoryBonusShares: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-history-2',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistoryDividend: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-history-3',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistoryBond: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-history-4',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistoryConversion: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-history-8',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryRightHistoryBondInterest: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-history-9',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  queryUpcomingRights: {
    api: '/tsol/apikey/tuxsvc/account/balance/query-right-expected',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  registerRight: {
    api: 'tsol/apikey/tuxsvc/account/balance/registering-right',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  cancelRight: {
    api: 'tsol/apikey/tuxsvc/account/balance/cancelling-right',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getOddLot: {
    api: '/tsol/apikey/tuxsvc/market/oddlot',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  balanceRetrieving: {
    api: 'tsol/apikey/tuxsvc/account/balance-retrieve',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  inquriyStockBalance: {
    api: 'tsol/apikey/tuxsvc/account/balance/stk-balance',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getBankBranchs: {
    api: 'tsol/apikey/tuxsvc/ekyc/bank-branch',
    category: LOTTE_API_CATEGORIES.EKYC,
  },
  checkAccountExist: {
    api: 'tsol/apikey/tuxsvc/ekyc/check-exist',
    category: LOTTE_API_CATEGORIES.EKYC,
  },
  getIndexList: {
    api: 'tsol/apikey/tuxsvc/market/indexs-list',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  getIndexStockList: {
    api: 'tsol/apikey/tuxsvc/market/symbols',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  marginRatio: {
    api: 'tsol/apikey/tuxsvc/account/margin-ratio',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getPartnerName: {
    api: 'tsol/apikey/tuxsvc/ekyc/emp-check',
    category: LOTTE_API_CATEGORIES.EKYC,
  },
  getVsdStatus: {
    api: 'tsol/apikey/tuxsvc/ekyc/vsd-stat',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getStockRankingPeriod: {
    api: 'tsol/apikey/tuxsvc/market/rise-fall-stock-rank',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  getMarketRightInfo: {
    api: 'tsol/apikey/tuxsvc/market/get-right-information',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  checkAccountOpeningStatus: {
    api: 'tsol/apikey/tuxsvc/ekyc/stk-acc-info',
    category: LOTTE_API_CATEGORIES.EKYC,
  },
  getContractStatus: {
    api: 'tsol/apikey/tuxsvc/ekyc/query-status-contract',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  postNotificationStatus: {
    api: 'tsol/apikey/tuxsvc/account/update-delivery-status-notification',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getNotificationStatus: {
    api: 'tsol/apikey/tuxsvc/account/query-delivery-status-notification',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getOddlotLatest: {
    api: 'tsol/apikey/tuxsvc/market/stock-board',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  loanEstimatedFee: {
    api: 'tsol/apikey/tuxsvc/account/adv-payment-fee',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  stockTransferConfirm: {
    api: 'tsol/apikey/tuxsvc/account/balance/stk-transfer-confirm',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getStockTransferConfirm: {
    api: 'tsol/apikey/tuxsvc/account/balance/main-sub-stock-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  cashTransferConfirm: {
    api: 'tsol/apikey/tuxsvc/account/balance/cash-transfer-confirm',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getCashTransferConfirm: {
    api: 'tsol/apikey/tuxsvc/account/balance/main-sub-cash-history',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  loanConfirm: {
    api: 'tsol/apikey/tuxsvc/account/balance/secured-loan-confirm',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getLoanConfirm: {
    api: 'tsol/apikey/tuxsvc/account/balance/secured-loan-info',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  orderBook: {
    api: 'tsol/apikey/tuxsvc/account/get-order-book',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  inquiryMarginRate: {
    api: 'tsol/apikey/tuxsvc/account/inquiry-margin-rate',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  subList: {
    api: '/tsol/apikey/tuxsvc/account/balance/sub-list',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  navHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/net-asset-value',
    category: LOTTE_API_CATEGORIES.BALANCE,
  },
  getMarketStockLatest: {
    api: 'tsol/apikey/tuxsvc/market/securities-price',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  getMarketStockBidOffer: {
    api: 'tsol/apikey/tuxsvc/market/best-bid-offer',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  getMarketCwDetail: {
    api: '/tsol/apikey/tuxsvc/market/cw-market-detail',
    category: LOTTE_API_CATEGORIES.MARKET,
  },
  getCashDepositHistory: {
    api: 'tsol/apikey/tuxsvc/account/balance/deposit-history',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  changeBroker: {
    api: 'tsol/apikey/tuxsvc/account/change-broker',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  brokerHistory: {
    api: 'tsol/apikey/tuxsvc/account/broker-history',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  getEmployeeInfo: {
    api: 'tsol/apikey/tuxsvc/account/user/emp_info',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  registerBankAccount: {
    api: 'tsol/apikey/tuxsvc/account/reg-bank-acc',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
  deleteBankAccount: {
    api: 'tsol/apikey/tuxsvc/account/del-bank-acc',
    category: LOTTE_API_CATEGORIES.ACCOUNT,
  },
};
