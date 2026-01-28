const TradexCommon = require("tradex-common");
const conf = require("./conf");

const { mapTopicToPublishV2, mapTopicToPublish } = require("./constants");

const {
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
} = require("./cache");

const {
  convertDataPublishV2Theme,
  convertDataPublishV2Static,
  convertDataPublishV2Advertise,
  convertDataPublishV2Deal,
  convertDataPublishV2BidOfferOddLot,
  convertDataPublishV2BidOffer,
  convertDataPublishV2Extra,
  convertDataPublishV2Quote,
} = require("./parser");

const statisticTopics = {};
const unknowTopics = {};

function addStatistic(topic) {
  let data = statisticTopics[topic];
  if (data == null) {
    statisticTopics[topic] = 1;
  } else {
    statisticTopics[topic] = data + 1;
  }
}

function processDataPublishV2Statistic(channel, data, scExchange) {
  const publishData = convertDataPublishV2Static(data);
  setToCacheInfo(data.code, publishData, cacheStatistic);
  scExchange.publish(channel, publishData);
}

function processDataPublishV2Extra(channel, data, scExchange) {
  let result = convertDataPublishV2Extra(data);
  setToCacheInfo(data.code, result, getCacheInfo());
  scExchange.publish(channel, result);
}

function processDataPublishV2Quote(channel, data, scExchange) {
  const publishData = convertDataPublishV2Quote(data);
  const cache = getCacheInfo();
  const currentCacheData = cache[data.code];
  if (currentCacheData == null || currentCacheData.data.vo < publishData.vo) {
    setToCacheInfo(data.code, publishData, cache);
  }
  scExchange.publish(channel, publishData);
}

function processDataPublishV2QuoteOddLot(channel, data, scExchange) {
  const publishData = convertDataPublishV2Quote(data);
  setToCacheInfo(data.code, publishData, cacheOddlotInfo);
  scExchange.publish(channel, publishData);
}

function processDataPublishV2Bidoffer(channel, data, scExchange) {
  let result = convertDataPublishV2BidOffer(data);
  setToCacheInfo(data.code, result, getCacheInfo());
  scExchange.publish(channel, result);
}

function processDataPublishV2BidOfferOddLot(channel, data, scExchange) {
  const result = convertDataPublishV2BidOfferOddLot(data);
  setToCacheInfo(data.code, result, cacheOddlotInfo);
  scExchange.publish(channel, result);
}

function processDataPublishV2Deal(channel, data, scExchange) {
  const result = convertDataPublishV2Deal(data);
  setToCacheInfo(data.code, result, cacheDeal);
  scExchange.publish(channel, result);
}

function processDataPublishV2Advertise(channel, data, scExchange) {
  const result = convertDataPublishV2Advertise(data);
  setToCacheInfo(data.code, result, cacheAdvertise);
  scExchange.publish(channel, result);
}

function processDataPublishV2Theme(channel, data, scExchange) {
  const result = convertDataPublishV2Theme(data);
  setToCacheInfo(result.n, result, cacheTheme);
  scExchange.publish(channel, result);
}

function clearCacheInfo(channel, data, scExchange) {
  console.log("clearCacheInfo");
  releaseCacheInfo();
  console.log("cacheInfo released", getCacheInfo());
  scExchange.publish(channel, data);
}

function processDataPublishV2(msg, scExchange) {
  try {
    let topic = msg.topic;
    addStatistic(topic);
    let message = JSON.parse(msg.value.toString());
    let data = message.data;
    let channel = mapTopicToPublishV2[topic];
    if (topic === "dealNoticeUpdate" || topic === "advertisedUpdate") {
      channel = channel + "." + data.marketType;
    }
    if (
      topic == "statisticUpdate" ||
      topic === "extraUpdate" ||
      topic === "quoteUpdate" ||
      topic === "bidOfferUpdate" ||
      topic === "bidOfferOddLotUpdate"
    ) {
      channel = channel + "." + data.code;
    }
    if (topic === "themeUpdate") {
      channel = channel + "." + data.themeCode;
    }
    if (topic === "quoteUpdate") {
      processDataPublishV2Quote(channel, data, scExchange);
    } else if (topic === "quoteOddLotUpdate") {
      processDataPublishV2Extra(channel, data, scExchange);
    } else if (topic === "extraUpdate") {
      processDataPublishV2Extra(channel, data, scExchange);
    } else if (topic === "calExtraUpdate") {
      processDataPublishV2Extra(channel, data, scExchange);
    } else if (topic === "bidOfferUpdate") {
      processDataPublishV2Bidoffer(channel, data, scExchange);
    } else if (topic === "bidOfferOddLotUpdate") {
      processDataPublishV2BidOfferOddLot(channel, data, scExchange);
    } else if (topic === "dealNoticeUpdate") {
      processDataPublishV2Deal(channel, data, scExchange);
    } else if (topic === "advertisedUpdate") {
      processDataPublishV2Advertise(channel, data, scExchange);
    } else if (topic === "statisticUpdate") {
      processDataPublishV2Statistic(channel, data, scExchange);
    } else if (topic === "themeUpdate") {
      processDataPublishV2Theme(channel, data, scExchange);
    } else if (topic === "marketStatus") {
      if (conf.returnSnapshot.enable) {
        setToCacheInfo(`${data.market}-${marketType}`, data, cacheMarketStatus);
      }
      scExchange.publish(channel, data);
    } else if (topic === "refreshData") {
      clearCacheInfo(channel, data, scExchange);
    } else {
      unknowTopics[topic] == true;
      scExchange.publish(channel, data);
    }
  } catch (e) {
    TradexCommon.Logger.error(`error on market data msg "${msg}"`, e);
  }
}

function processDataPublishV1(msg, scExchange) {
  try {
    let topic = msg.topic;
    addStatistic(topic);
    let message = JSON.parse(msg.value.toString());
    let data = message.data;
    if (Array.isArray(data)) {
      for (let i in data) {
        let channel =
          topic === "marketStatus" || topic === "refreshData"
            ? mapTopicToPublish[topic]
            : mapTopicToPublish[topic] + "." + data[i].code;
        scExchange.publish(channel, data[i]);
      }
    } else {
      let channel =
        topic === "marketStatus" || topic === "refreshData"
          ? mapTopicToPublish[topic]
          : mapTopicToPublish[topic] + "." + data.code;
      scExchange.publish(channel, data);
    }
  } catch (e) {
    TradexCommon.Logger.error(`error on market data msg "${msg}"`, e);
  }
}

function createMarketProcess(isV2, scExchange) {
  setInterval(() => {
    const unknownTopics = Object.keys(unknowTopics);
    if (unknownTopics.length > 0) {
      TradexCommon.Logger.error("sunknown topics", unknownTopics);
    }
    TradexCommon.Logger.error("statictic", statisticTopics);
  }, 60000);
  if (isV2) {
    return (msg) => processDataPublishV2(msg, scExchange);
  }
  return (msg) => processDataPublishV1(msg, scExchange);
}

module.exports = {
  processDataPublishV2Statistic,
  processDataPublishV2Extra,
  processDataPublishV2Quote,
  processDataPublishV2QuoteOddLot,
  processDataPublishV2Bidoffer,
  processDataPublishV2BidOfferOddLot,
  processDataPublishV2Deal,
  processDataPublishV2Advertise,
  processDataPublishV2Theme,
  processDataPublishV2,
  processDataPublishV1,
  createMarketProcess,
};
