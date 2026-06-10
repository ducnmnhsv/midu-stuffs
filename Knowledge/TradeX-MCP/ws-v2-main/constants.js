const REQUEST_ID = "_rId";
const communicationTypes = {
  userLogin: "userLogin",
};

const LOGIN_TYPES = {
  SYSTEM: 'system'
};

const ERROR_CODES = {
  UNAUTHORIZED: 'UNAUTHORIZED',
  NULL_CHANNEL: 'NULL_CHANNEL',
};

const checkConnections = {};

const mapTopicToPublish = {
  "extraUpdate": "market.extra",
  'stockUpdate': "market.stock.quote",
  'indexUpdate': "market.index.quote",
  'bidOfferUpdate': "market.stock.bidoffer",
  'dealNoticeUpdate': "market.stock.putthrough.deal",
  'advertisedUpdate': "market.stock.putthrough.advertise",
  'futuresUpdate': "market.futures.quote",
  'futuresBidOfferUpdate': "market.futures.bidoffer",
  'marketStatus': "market.status",
  'tickerMesgUpdate': "market.tickerMesg",
  'cwUpdate': "market.cw.quote",
  'cwBidOfferUpdate': "market.cw.bidoffer",
  "refreshData": "market.refreshData",
};

const TOPIC_V2_MARKET_EXTRA = 'market.extra';
const TOPIC_V2_MARKET_QUOTE = 'market.quote';
const TOPIC_V2_MARKET_QUOTEODDLOT = 'market.quoteOddLot';
const TOPIC_V2_MARKET_BIDOFFER = 'market.bidoffer';
const TOPIC_V2_MARKET_BIDOFFERODDLOT = 'market.bidofferOddLot';
const TOPIC_V2_MARKET_PUTTHROUGH_DEAL = 'market.putthrough.deal';
const TOPIC_V2_MARKET_PUTTHROUGH_ADVERTISE = 'market.putthrough.advertise';
const TOPIC_V2_MARKET_STATUS = 'market.status';
const TOPIC_V2_MARKET_TICKERMESG = 'market.tickerMesg';
const TOPIC_V2_MARKET_REFRESHDATA = 'market.refreshData';
const TOPIC_V2_MARKET_STATISTIC = 'market.statistic';
const TOPIC_V2_MARKET_THEME = 'market.theme';

const TOPIC_V2_MARKET_EXTRA_LENGTH = TOPIC_V2_MARKET_EXTRA.length + 1;
const TOPIC_V2_MARKET_QUOTE_LENGTH = TOPIC_V2_MARKET_QUOTE.length + 1;
const TOPIC_V2_MARKET_QUOTEODDLOT_LENGTH = TOPIC_V2_MARKET_QUOTEODDLOT.length + 1;
const TOPIC_V2_MARKET_BIDOFFER_LENGTH = TOPIC_V2_MARKET_BIDOFFER.length + 1;
const TOPIC_V2_MARKET_BIDOFFERODDLOT_LENGTH = TOPIC_V2_MARKET_BIDOFFERODDLOT.length + 1;
const TOPIC_V2_MARKET_PUTTHROUGH_DEAL_LENGTH = TOPIC_V2_MARKET_PUTTHROUGH_DEAL.length + 1;
const TOPIC_V2_MARKET_PUTTHROUGH_ADVERTISE_LENGTH = TOPIC_V2_MARKET_PUTTHROUGH_ADVERTISE.length + 1;
const TOPIC_V2_MARKET_STATUS_LENGTH = TOPIC_V2_MARKET_STATUS.length + 1;
const TOPIC_V2_MARKET_TICKERMESG_LENGTH = TOPIC_V2_MARKET_TICKERMESG.length + 1;
const TOPIC_V2_MARKET_REFRESHDATA_LENGTH = TOPIC_V2_MARKET_REFRESHDATA.length + 1;
const TOPC_V2_MARKET_STATISTIC_LENGTH = TOPIC_V2_MARKET_STATISTIC.length + 1;
const TOPIC_V2_MARKET_THEME_LENGTH = TOPIC_V2_MARKET_THEME.length + 1;

const mapTopicToPublishV2 = {
  "calExtraUpdate": TOPIC_V2_MARKET_EXTRA,
  "extraUpdate": TOPIC_V2_MARKET_EXTRA,
  "quoteUpdate": TOPIC_V2_MARKET_QUOTE,
  "quoteOddLotUpdate": TOPIC_V2_MARKET_QUOTEODDLOT,
  "bidOfferUpdate": TOPIC_V2_MARKET_BIDOFFER,
  "bidOfferOddLotUpdate": TOPIC_V2_MARKET_BIDOFFERODDLOT,
  "dealNoticeUpdate": TOPIC_V2_MARKET_PUTTHROUGH_DEAL,
  "advertisedUpdate": TOPIC_V2_MARKET_PUTTHROUGH_ADVERTISE,
  "marketStatus": TOPIC_V2_MARKET_STATUS,
  "tickerMesgUpdate": TOPIC_V2_MARKET_TICKERMESG,
  "refreshData": TOPIC_V2_MARKET_REFRESHDATA,
  "statisticUpdate": TOPIC_V2_MARKET_STATISTIC,
  "themeUpdate": TOPIC_V2_MARKET_THEME,
};

module.exports = {
  REQUEST_ID,
  communicationTypes,
  LOGIN_TYPES,
  ERROR_CODES,
  checkConnections,
  mapTopicToPublish,
  TOPIC_V2_MARKET_EXTRA,
  TOPIC_V2_MARKET_QUOTE,
  TOPIC_V2_MARKET_QUOTEODDLOT,
  TOPIC_V2_MARKET_BIDOFFER,
  TOPIC_V2_MARKET_BIDOFFERODDLOT,
  TOPIC_V2_MARKET_PUTTHROUGH_DEAL,
  TOPIC_V2_MARKET_PUTTHROUGH_ADVERTISE,
  TOPIC_V2_MARKET_STATUS,
  TOPIC_V2_MARKET_TICKERMESG,
  TOPIC_V2_MARKET_REFRESHDATA,
  TOPIC_V2_MARKET_STATISTIC,
  TOPIC_V2_MARKET_THEME,
  TOPIC_V2_MARKET_EXTRA_LENGTH,
  TOPIC_V2_MARKET_QUOTE_LENGTH,
  TOPIC_V2_MARKET_QUOTEODDLOT_LENGTH,
  TOPIC_V2_MARKET_BIDOFFER_LENGTH,
  TOPIC_V2_MARKET_BIDOFFERODDLOT_LENGTH,
  TOPIC_V2_MARKET_PUTTHROUGH_DEAL_LENGTH,
  TOPIC_V2_MARKET_PUTTHROUGH_ADVERTISE_LENGTH,
  TOPIC_V2_MARKET_STATUS_LENGTH,
  TOPIC_V2_MARKET_TICKERMESG_LENGTH,
  TOPIC_V2_MARKET_REFRESHDATA_LENGTH,
  TOPC_V2_MARKET_STATISTIC_LENGTH,
  TOPIC_V2_MARKET_THEME_LENGTH,
  mapTopicToPublishV2,
};