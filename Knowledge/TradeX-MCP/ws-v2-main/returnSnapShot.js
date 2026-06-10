const { getSymbolInfo } = require("./redis");
const { convertSymbolInfoV2 } = require("./parser");
const conf = require("./conf");


const {
  cacheInfo,
  cacheDeal,
  cacheTheme,
  cacheAdvertise,
  cacheOddlotInfo,
  cacheStatistic,
  cacheMarketStatus,
  getCacheInfo,
  setCacheInfo,
} = require('./cache');


const {
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
  TOPIC_V2_MARKET_STATISTIC_LENGTH,
  TOPIC_V2_MARKET_THEME_LENGTH,
} = require('./constants');
const { Logger } = require("tradex-common");

const cacheInfoGettingDb = {};

async function returnSymbolSnapShotInfo(code) {
  const time = new Date().getTime();
  const cache = getCacheInfo();
  let currentCacheData = cache[code];
  if (currentCacheData == null || currentCacheData.meta.dbAt == null || time - currentCacheData.meta.dbAt > conf.returnSnapshot.ttl) {
    // do query redis to get latest data  
    return new Promise((resolve, reject) => {
      const cb = {
        resolve,
        reject,
      };
      if (cacheInfoGettingDb[code] == null) {
        cacheInfoGettingDb[code] = [cb];
        getSymbolInfo(code).then(info => {
          Logger.warn('finish getting info', code);
          convertedData = convertSymbolInfoV2(info)
          currentCacheData = cache[code];
          if (currentCacheData == null) {                        
            currentCacheData = {
              data: convertedData,
              meta: {
                dbAt: new Date().getTime(),
              },
            };
            setCacheInfo(code, currentCacheData);
          } else {
            currentCacheData.data = Object.assign(convertedData, currentCacheData.data);
            currentCacheData.meta.dbAt = new Date().getTime();
          }
          try {
            const allCbs = cacheInfoGettingDb[code];
            delete cacheInfoGettingDb[code];
            allCbs.forEach(it => it.resolve(currentCacheData.data));
          } catch (err) {
            // swallow
          }
        }).catch(err => {
          Logger.error("fail to query symbol info", code, err);        
          cacheInfoGettingDb[code].forEach(it => it.reject(err));
          delete cacheInfoGettingDb[code];
        });
      } else {                    
        cacheInfoGettingDb[code].push(cb);
      }
    });
    
  } else {
    return currentCacheData.data;
  }
}

function returnSnapShotInfo(req, code) {
  const returnChannel = conf.returnSnapshot.callbackChannel || `${req.channel}.cb`;
  const data = returnSymbolSnapShotInfo(code).then(data => req.socket.emit(returnChannel, data)).catch(err => {});
}

function checkingReturnSnapShotInfo(req) {
  if (conf.returnSnapshot.enable) {
    if (req.channel.startsWith(TOPIC_V2_MARKET_EXTRA)) {
      const code = req.channel.substring(TOPIC_V2_MARKET_EXTRA_LENGTH);
      returnSnapShotInfo(req, code);
    } else if (req.channel.startsWith(TOPIC_V2_MARKET_QUOTE)) {
      const code = req.channel.substring(TOPIC_V2_MARKET_QUOTE_LENGTH);
      returnSnapShotInfo(req, code);
    // } 
    // else if (req.channel.startsWith(TOPIC_V2_MARKET_QUOTEODDLOT)) {

    } else if (req.channel.startsWith(TOPIC_V2_MARKET_BIDOFFER)) {
      const code = req.channel.substring(TOPIC_V2_MARKET_BIDOFFER_LENGTH);
      returnSnapShotInfo(req, code);
    // } else if (req.channel.startsWith(TOPIC_V2_MARKET_BIDOFFERODDLOT)) {

    // } else if (req.channel.startsWith(TOPIC_V2_MARKET_PUTTHROUGH_ADVERTISE)) {

    // } else if (req.channel.startsWith(TOPIC_V2_MARKET_PUTTHROUGH_DEAL)) {

    // } else if (req.channel.startsWith(TOPIC_V2_MARKET_STATISTIC)) {

    // } else if (req.channel === 'market.status') {

    // } else if (req.channel.startsWith(TOPIC_V2_MARKET_THEME)) {

    }
  }
}

module.exports = {
  checkingReturnSnapShotInfo,
  returnSymbolSnapShotInfo,
};