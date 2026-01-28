const TradexCommon = require('tradex-common');
const { getUserId } = require('./query');
const conf = require('./conf');


function scAuthenHandler(req, next) {
  try {
    const id = getUserId(req.socket);
    TradexCommon.Logger.info(`socket reauthenticate socket.id: ${req.socket.id}, user: ${id}`);
    if (conf.allowOnly1SessionPerUser === true) {
      if (req.socket.authState === req.socket.AUTHENTICATED) {
        TradexCommon.Logger.info("MIDDLEWARE_AUTHENTICATE");
        checkConnection(req.socket, req.authToken, req.signedAuthToken);
      }
    }
    next();
  } catch (e) {
    TradexCommon.Logger.error("fail to handle subscribe", req, e);
  }
}



function checkConnection(socket, authToken, signedAuthToken, retryIndex = 0) {
  if (retryIndex === 0) {
    const checkingMap = checkConnections[signedAuthToken];
    if (checkingMap != null) {
      checkingMap[socket.id] = socket;
      return;
    } else {
      checkConnections[signedAuthToken] = {
        [socket.id]: socket
      };
    }
  }
  this.kafkaInstance.sendRequestAsync(socket.id, verifyConf.topic, verifyConf.uri, {
    headers: {
      token: socket.getAuthToken().token
    }
  }).then(
    msg => {
      if (msg.data === true || (msg.data != null && msg.data.data === true)) {
        TradexCommon.Logger.info(`${socket.id} successfully authenticated`);
      } else {
        TradexCommon.Logger.warn(`${socket.id} fail to authenticate. de-authenticate`);
        Object.values(checkConnections[signedAuthToken]).forEach(socket => {
          try {
            socket.deauthenticate();
          } catch (e) {
            //swallow
          }
        });
      }
      delete checkConnections[signedAuthToken];
    }
  ).catch(err => {
    TradexCommon.Logger.error(`fail to check token ${socket.id} in ${retryIndex + 1} times`, err);
    if (retryIndex === 3) {
      delete checkConnections[signedAuthToken];
      return;
    }
    this.checkConnection(socket, authToken, signedAuthToken, retryIndex++);
  });
}

module.exports = {
  scAuthenHandler,
  checkConnection,
};