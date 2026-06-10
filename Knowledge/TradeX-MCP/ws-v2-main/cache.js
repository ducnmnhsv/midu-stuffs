const TradexCommon = require('tradex-common');
const conf = require("./conf");

let cacheInfo = {};
const cacheDeal = {};
const cacheTheme = {};
const cacheAdvertise = {};
const cacheOddlotInfo = {};
const cacheStatistic = {};
const cacheMarketStatus = {};

const loginMap = {};


function setToCacheInfo(symbol, result, cache) {
  try {
    if (conf.returnSnapshot.enable && symbol != null && symbol !== '') {
      const time = new Date().getTime();
      let currentCacheData = cache[symbol];
      if (currentCacheData == null) {
        cache[symbol] = {
          data: result,
          meta: {
            dbAt: undefined, // query from db at time in ms
            ltd: time, // last updated to cache at
          },
        };
      } else {
        cache[symbol].data = Object.assign(currentCacheData.data, result);
        cache[symbol].meta.ltd = time;
      }
    }
  } catch(err) {
    TradexCommon.Logger.error('exception on set to cache', err);
  }
};

function releaseCacheInfo() {
  cacheInfo = {};
};

function getCacheInfo() {
  return cacheInfo;
};

function setCacheInfo(key, data) {
  cacheInfo[key] = data;
};

module.exports = {
  cacheInfo,
  cacheDeal,
  cacheTheme,
  cacheAdvertise,
  cacheOddlotInfo,
  cacheStatistic,
  cacheMarketStatus,
  loginMap,
  setToCacheInfo,
  releaseCacheInfo,
  getCacheInfo,
  setCacheInfo,
};
