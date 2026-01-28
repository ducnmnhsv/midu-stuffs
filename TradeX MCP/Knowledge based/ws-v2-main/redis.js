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

let readyCallbacks = [];

async function createClient() {
  if (client != null) return;
  client = await redis.createClient(conf.redis).
    on('error', err => {
      Logger.error("error on redis connection", err.message);
    }).
    on('reconnecting', err => {
      Logger.warn("redis connection is reconnecting");
    }).
    on('ready', () => {
      Logger.info("redis connection is ready", readyCallbacks.length);
      const cbs = [...readyCallbacks];
      readyCallbacks = [];
      cbs.forEach(cb => cb());
    }).
    connect();
}

function waitForClientReady(timeout = 5000) {
  if (client.isReady) {
    return Promise.resolve(null);
  }
  return new Promise((resolve, reject) => {
    let finish = false;
    const timeOutHandler = setTimeout(() => {
      if (finish) {
        return;
      }
      finish = true;
      reject(new Error("redis connection is not ready"));
    }, timeout);
    readyCallbacks.push(() => {
      if (finish) {
        return;
      }
      finish = true;
      try {
        resolve();
      } catch (err) {
        Logger.error('fail to resolve result for waitd redis ready', err);
      }
      try {
        clearTimeout(timeOutHandler);
      } catch (err) {
      }
    });
  });
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
  await createClient();
  await waitForClientReady();
  Logger.info('redis is ready. start query symbol info', code);
  const data = await client.HGET(REDIS_KEY.SYMBOL_INFO, code);
  Logger.info('finish query symbol info', code);
  return parseRedisData(data);
}

module.exports = {
  REDIS_KEY,
  KeyNotExist,
  getSymbolInfo,
};