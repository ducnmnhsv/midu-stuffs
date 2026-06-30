const redis = require('redis');
const conf = require('./conf');
const { Logger } = require('tradex-common');

const DATA_TYPE = {
  UNDEFINED: 'a',
  NULL: 'b',
  BOOLEAN: '0',
  STRING: '1',
  NUMBER: '2',
  DATE: '3',
  OBJECT: '4',
};

const REDIS_KEY = {
  SYMBOL_INFO: 'realtime_mapSymbolInfo',
  SYMBOL_INFO_ODD_LOT: 'realtime_mapSymbolInfoOddLot',
  SYMBOL_DAILY: 'realtime_mapSymbolDaily',
  FOREIGNER_DAILY: 'realtime_mapForeignerDaily',
  SYMBOL_QUOTE: 'realtime_listQuote',
  SYMBOL_STATISTICS: 'realtime_mapSymbolStatistic',
  SYMBOL_BID_OFFER: 'realtime_listBidOffer',
  SYMBOL_QUOTE_MINUTE: 'realtime_listQuoteMinute',
  DEAL_NOTICE: 'realtime_listDealNotice',
  ADVERTISED: 'realtime_listAdvertised',
  MARKET_STATUS: 'realtime_mapMarketStatus',
  SYMBOL_INFO_EXTEND: 'symbolInfoExtend',
  STOCK_RANKING_PERIOD: 'market_rise_false_stock_ranking',
  SYMBOL_STOCK_RIGHT: 'market_right_info',
  NOTIFICATION: 'notice_',
};

class KeyNotExist extends Error {
  constructor(key) {
    super(`key ${key} is not existed`);
  }
}

let client = null;
let clientConnectPromise = null;

async function createClient() {
  if (client?.isReady) return client;
  if (clientConnectPromise) return clientConnectPromise;

  if (!client) {
    client = redis.createClient(conf.redis)
      .on('error', err => Logger.error('error on redis connection', err.message))
      .on('reconnecting', () => Logger.warn('redis connection is reconnecting'));
  }

  const currentClient = client;
  clientConnectPromise = currentClient.connect()
    .then(() => currentClient)
    .catch(err => {
      try { currentClient.disconnect?.(); } catch (_) {}
      if (client === currentClient) client = null;
      throw err;
    })
    .finally(() => {
      clientConnectPromise = null;
    });

  return clientConnectPromise;
}

function parseRedisData(value) {
  const type = value[0];
  if (type === DATA_TYPE.NULL) {
    return null;
  } else if (type === DATA_TYPE.UNDEFINED) {
    return undefined;
  } else if (typeof type === DATA_TYPE.BOOLEAN) {
    const content = value.substr(1);
    return content === '1';
  } else if (typeof type === DATA_TYPE.STRING) {
    const content = value.substr(1);
    return content;
  } else if (typeof type === DATA_TYPE.NUMBER) {
    const content = value.substr(1);
    return Number(content);
  } else if (type === DATA_TYPE.DATE) {
    const content = value.substr(1);
    return new Date(Number(content));
  } else {
    const content = value.substr(1);
    return JSON.parse(content, this.receiver);
  }
}

async function getSymbolInfo(code) {
  const currentClient = await createClient();
  Logger.info('redis is ready. start query symbol info', code);
  const data = await currentClient.HGET(REDIS_KEY.SYMBOL_INFO, code);
  Logger.info('finish query symbol info', code);
  return parseRedisData(data);
}

module.exports = {
  REDIS_KEY,
  KeyNotExist,
  getSymbolInfo,
};