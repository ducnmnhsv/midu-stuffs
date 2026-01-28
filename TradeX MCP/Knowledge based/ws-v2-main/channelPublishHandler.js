const TradexCommon = require('tradex-common');

const {
  checkNullChannel,
  checkSystemChannel,
} = require('./channelChecker');


function channelPublishHandler(req, next) {
  try {
    if (checkNullChannel(req, next)) {
      if (checkSystemChannel(req, next)) {
        next();
      }
    }
  } catch (e) {
    TradexCommon.Logger.error("fail to handle subscribe", req, e);
  }
}

module.exports = channelPublishHandler;