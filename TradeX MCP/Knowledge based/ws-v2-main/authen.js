const TradexCommon = require('tradex-common');
const { respondCode, respondErrorObject, getTxId } = require('./query');

const { decodeAccessToken } = require('./jwt');
const { getIp } = require('./ip');
const conf = require('./conf');
const { REQUEST_ID } = require('./constants');

function onlogOut(socket, data, respond) {
  TradexCommon.Logger.info(socket.id, 'logging out');
  try {
    respond(null, null);
    var authData = socket.getAuthToken();
    if (authData && authData.refreshToken && authData.refreshToken !== '') {
      TradexCommon.Kafka.getInstance().sendMessage(getTxId(socket.id, data[REQUEST_ID]), 'aaa', 'post:/api/v1/revokeToken', {
        refresh_token: authData.refreshToken,
        headers: data.headers,
        sourceIp: getIp(data, socket),
      });
    }
    socket.deauthenticate();
  } catch (e) {
    TradexCommon.Logger.error("error in logout", data, e);
  }
}

function setLoggedIn(socket) {
  const authData = socket.getAuthToken();
  setImmediate(() => socket.emit('loggedIn', authData));
}


async function verifyOtp(socket, request, respond) {
  TradexCommon.Logger.info(socket.id, 'verify otp', request);
  const lang = TradexCommon.Utils.getLanguageCode(request.headers['accept-language']);
  try {
    const data = request.body;
    data.headers = request.headers ? request.headers : {};
    data.sourceIp = getIp(data, socket);
    const authData = socket.getAuthToken();
    const token = authData ? authData.token : null;
    if (token == null) {
      respondCode(respond, ERROR_CODES.UNAUTHORIZED, lang);
    }
    data.headers.token = token;
    var txId = getTxId(socket.id, data[REQUEST_ID]);
    var msg = await TradexCommon.Kafka.getInstance().sendRequestAsync(txId, 'aaa', 'post:/api/v1/login/sec/verifyOTP', data);
    if (!msg.data.status) {
      var payload = await decodeAccessToken(msg.data.data.accessToken);
      var state = authData.s;
      if (state != null) {
        state.otp = true;
      } else {
        state = { otp: true };
      }
      socket.setAuthToken({
        s: state,
        exp: payload.exp,
        token: payload,
        accessToken: msg.data.data.accessToken,
        refreshToken: msg.data.data.refreshToken,
        userInfo: msg.data.data.userInfo
      });
      setLoggedIn(socket);
      TradexCommon.Logger.info(socket.id, 'successfully verify otp');
      respond(null, msg.data);
    } else {
      TradexCommon.Logger.warn(socket.id, 'failed to verify otp', msg.data.status.code);
      respondErrorObject(respond, msg.data, lang);
    }
  } catch (e) {
    TradexCommon.Logger.error(socket.id, "error in verify Otp", request, e);
    respondCode(respond, "INTERNAL_SERVER_ERROR", lang);
  }
}

async function login(socket, request, respond) {
  TradexCommon.Logger.info(socket.id, 'do login');
  const lang = TradexCommon.Utils.getLanguageCode(request.headers['accept-language']);
  try {
    const data = request.body;
    data.headers = request.headers ? request.headers : {};
    data.sourceIp = getIp(data, socket);
    if (data.grant_type === 'refresh_token') {
      const txId = getTxId(socket.id, data[REQUEST_ID]);
      const msg = await TradexCommon.Kafka.getInstance().sendRequestAsync(txId, 'aaa', 'post:/api/v1/refreshToken', data);
      if (msg.data.data) {
        const payload = await decodeAccessToken(msg.data.data.accessToken);
        socket.setAuthToken({
          s: {
            gt: data.grant_type,
          },
          exp: payload.exp,
          token: payload,
          accessToken: msg.data.data.accessToken,
          refreshToken: data.refresh_token
        });
        setLoggedIn(socket);
        TradexCommon.Logger.info(socket.id, 'login successfully with refresh token type');
        respond(null, msg.data);
      } else {
        TradexCommon.Logger.error(socket.id, 'login failed with refresh token');
        respondErrorObject(respond, msg.data, lang);
      }
    } else if (data.grant_type === 'access_token') {
      let confJwt = null;
      if (data.domain) {
        confJwt = conf.getJwt(data.domain);
        if (!confJwt) {
          respondCode(respond, 'NO_KEY_FOR_DOMAIN', lang);
        }
      }
      try {
        const payload = await decodeAccessToken(data.access_token, confJwt);
        socket.setAuthToken({
          s: {
            gt: data.grant_type,
          },
          exp: payload.exp,
          token: payload,
          accessToken: data.access_token
        });
        setLoggedIn(socket);
        TradexCommon.Logger.info(socket.id, 'login successfully with access_token type');
        respond(null, null);
      } catch (e) {
        TradexCommon.Logger.error(socket.id, 'login failed with access_token');
        respondCode(respond, 'INVALID_TOKEN', lang);
      }
    } else {
      const txId = getTxId(socket.id, data[REQUEST_ID]);
      if (conf.enableEncryptPassword === true && data.password != null) {
        data.password = TradexCommon.Utils.rsaEncrypt(data.password, conf.rsa.publicKey);
      }

      const msg = await TradexCommon.Kafka.getInstance().sendRequestAsync(txId, 'aaa', 'post:/api/v1/login', data);
      if (!msg.data.status) {
        const payload = await decodeAccessToken(msg.data.data.accessToken);
        socket.setAuthToken({
          s: {
            gt: data.grant_type,
          },
          exp: payload.exp,
          token: payload,
          accessToken: msg.data.data.accessToken,
          refreshToken: msg.data.data.refreshToken,
          userInfo: msg.data.data.userInfo
        });
        setLoggedIn(socket);
        const userId = request.username;
        try {
          userId = socket.authToken.token.userData.username;
        } catch (e) {
          try {
            userId = socket.authToken.token.userInfo.username;
          } catch (e) {
          }
        }

        if (data.grant_type !== "demo") {
          if (userId != null && conf.allowOnly1SessionPerUser) {
            TradexCommon.Logger.info(socket.id, txId, "notify login", userId);
            TradexCommon.Kafka.getInstance().sendRequestAsync(txId, conf.communicationTopic, communicationTypes.userLogin, {
              type: communicationTypes.userLogin,
              userId,
              socketId: socket.id,
            }).then().catch(err => TradexCommon.Logger.error(`fail to notify user login ${userId}`));
          }
          loginMap[socket.id] = userId;
        }
        TradexCommon.Logger.info(socket.id, txId, data.username, "successfully login");
        respond(null, msg.data);
      } else {
        TradexCommon.Logger.error(socket.id, txId, data.username, "fail to login");
        respondErrorObject(respond, msg.data, lang);
      }
    }
  } catch (e) {
    TradexCommon.Logger.error(socket.id, "error in login", request, e);
    respondCode(respond, "INTERNAL_SERVER_ERROR");
  }
}



async function systemLogin(socket, request, respond) {
  TradexCommon.Logger.info(socket.id, "systemLogin");
  try {
    const data = request.body;
    if (data.grant_type === 'system_login') {
      let confJwt = null;
      confJwt = conf.getJwt();
      if (!confJwt) {
        respondCode(respond, 'NO_KEY_FOR_DOMAIN', 'en');
        return;
      }
      let payload = null;
      try {
        payload = await decodeAccessToken(data.access_token, confJwt);
      } catch (e) {
        respondCode(respond, 'INVALID_TOKEN', 'en');
        return;
      }
      socket.setAuthToken({
        s: {
          gt: 'system',
        },
        token: payload,
        type: LOGIN_TYPES.SYSTEM
      });
      TradexCommon.Logger.info(socket.id, "systemLogin success");
      respond(null, null);
    }
  } catch (e) {
    TradexCommon.Logger.error(socket.id, "error in system login", e);
    respondCode(respond, 'INTERNAL_SERVER_ERROR', 'en');
  }
}


module.exports = {
  verifyOtp,
  login,
  onlogOut,
  setLoggedIn,
  systemLogin,
};