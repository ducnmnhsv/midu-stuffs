const TradexCommon = require('tradex-common');
const { getUserId } = require('./query');
const { checkingReturnSnapShotInfo } = require('./returnSnapShot');

const {
  checkNullChannel,
  checkSystemChannel,
  checkAuthenticatedChannels,
} = require('./channelChecker');


function channelSubscribeHandler(req, next) {
  try {
    const id = getUserId(req.socket);
    TradexCommon.Logger.info(`ww socket.id: ${req.socket.id}, user: ${id}, channel: ${req.channel}`, req.data);
    if (checkNullChannel(req, next)) {
      if (checkAuthenticatedChannels(req, next)) {
        if (checkSystemChannel(req, next)) {
          try {
            if (req.data != null && req.data.returnSnapShot === true) {
              checkingReturnSnapShotInfo(req);
            }
          } catch(err) {
            TradexCommon.Logger.error('fail to return snapshot', req.socket.id, id, req.channel, err);
          }
          next();
        }
      }
    }
  } catch (e) {
    TradexCommon.Logger.error("fail to handle subscribe", req, e);
  }
}

module.exports = channelSubscribeHandler;