const TradexCommon = require('tradex-common');
const conf = require('./conf');

function checkNullChannel(req, next) {
  if (req == null) {
    next(new Error(ERROR_CODES.NULL_CHANNEL));
    return false;
  }
  if (req.channel == null) {
    TradexCommon.Logger.warn('channel is null');
    next(new Error(ERROR_CODES.NULL_CHANNEL));
    return false;
  }
  if (req.request != null) {
    TradexCommon.Logger.warn("request data", req.remoteAddress, req.request);
  }
  return true;
}

function checkSystemChannel(req, next) {
  if (req.channel.indexOf(TradexCommon.Constants.SYSTEM_PREFIX_SC_CHANNEL) === 0) { // system channel. need to be authenticated as system
    const authData = req.socket.getAuthToken();
    if (authData == null || authData.type !== LOGIN_TYPES.SYSTEM) {
      next(new Error(ERROR_CODES.UNAUTHORIZED));
      TradexCommon.Logger.warn("checked system channel", req.channel);
      return false;
    }
  }
  return true;
}

function checkAuthenticatedChannels(req, next) {
  let state = null;
  for (const element of conf.authenticatedChannels) {
    const filter = element;
    if (filter.type === "exact") {
      if (filter.pattern === req.channel) {
        state = {};
        break;
      }
    } else if (filter.type === "regex") {
      if (filter.pattern.test(req.channel)) {
        state = {};
        break;
      }
    }
  }
  if (state != null) {
    const authData = req.socket.getAuthToken();
    if (authData == null || authData.type) {
      next(new Error(ERROR_CODES.UNAUTHORIZED));
      TradexCommon.Logger.info("not allow subscribe of non authenticated", filter, req.channel);
      return false;
    }
  }
  return true;
}

module.exports = {
  checkNullChannel,
  checkSystemChannel,
  checkAuthenticatedChannels,
};