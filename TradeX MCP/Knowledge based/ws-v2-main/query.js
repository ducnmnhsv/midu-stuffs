const TradexCommon = require('tradex-common');
const { REQUEST_ID } = require('./constants');
const { getIp } = require('./ip');
const conf = require('./conf');

const {
  decodeOtpToken,
  decodeAccessToken,
} = require('./jwt');

const { loginMap } = require('./cache');


const { getScopes, unAuthenticatedScopes } = require('./scope');

async function doQuery(socket, data, res) {
  const authData = socket.getAuthToken();
  const lang = TradexCommon.Utils.getLanguageCode(data && data.headers ? data.headers['accept-language'] : null);
  try {
    if (!authData) { // non authenticated use nonauthenticated scope
      await forwardQuery(socket, data, res, unAuthenticatedScopes, lang);
    } else {
      const scopes = getScopes(authData.token.scopeGroupIds);
      if (!scopes) { // authenticated but noscope yet. forward to aaa
        logger.info('forward to aaa', socket.id, data[REQUEST_ID], authData);
        data.body.headers = data.headers;
        data.body.sourceIp = getIp(data, socket);
        data.body.headers.token = authData.token;
        await reallyForward({
          uri: data.uri,
          topic: 'aaa',
        }, res, data.body, lang, socket, data[REQUEST_ID]);
      } else {// authenticated and have scope, forward directly
        const scope = scopes.find(scope => scope.uriPattern === data.uri);
        if (scope != null && scope.forwardData.tokenType === 'VERIFIED') {
          if (data.headers.otpToken == null) {
            logger.error("error in doQuery:", socket.id, data);
            respondCode(res, "OTP_TOKEN_IS_REQUIRED", lang);
            return;
          }
          let payload;
          try {
            payload = await decodeOtpToken(data.headers.otpToken);
          } catch (e) {
            respondCode(res, "OTP_TOKEN_IS_EXPIRED", lang);
            return;
          }
          if (authData.token.refreshTokenId !== payload.rId) {
            respondCode(res, "OTP_TOKEN_IS_REQUIRED", lang);
            return;
          }
        }
        await forwardQuery(socket, data, res, scopes);
      }
    }
  } catch (e) {
    logger.error("error in doQuery:", socket.id, data, e);
    respondCode(res, "INTERNAL_SERVER_ERROR", lang);
  }
}

async function forwardQuery(socket, data, res, scopes, lang) {
  for (const element of scopes) {
    const scope = element;
    const match = nanomatch(data.uri, scope.uriPattern);
    if (match && match.length > 0) {
      const authData = socket.getAuthToken();
      const token = authData ? authData.token : null;
      const forwardResult = TradexCommon.Utils.getForwardUri(data, scope, token, (serviceName) => {
        return true;
      }, {});
      if (!forwardResult.topic) {
        respondCode(res, ERROR_CODES.UNAUTHORIZED, lang);
      } else {
        const body = data.body;
        if (forwardResult.conId) {
          body.conId = forwardResult.conId;
        }
        body.headers = data.headers;
        body.sourceIp = getIp(data, socket);
        body.headers.token = token;
        if (body.headers.secToken) {
          if (!body.headers.secDomain) {
            respondCode(res, ERROR_CODES.UNAUTHORIZED, lang);
            return;
          }
          if (typeof body.headers.secToken === 'string') {
            let token = null;
            try {
              token = await decodeAccessToken(body.headers.secToken, conf.getJwt(body.headers.secDomain));
            } catch (e) {
              respondCode(res, ERROR_CODES.UNAUTHORIZED, lang);
              return;
            }
            body.headers.secToken = token;
            return reallyForward(forwardResult, res, body, lang, socket, data[REQUEST_ID]);
          } else {
            respondCode(res, ERROR_CODES.UNAUTHORIZED, lang);
          }
        }
        return reallyForward(forwardResult, res, body, lang, socket, data[REQUEST_ID]);
      }
    }
  }
  // no scope match
  respondCode(res, ERROR_CODES.UNAUTHORIZED, lang);
}


async function reallyForward(forwardResult, res, body, lang, socket, requestId) {
  const t = new Date().getTime();
  const txId = getTxId(socket.id, requestId);
  const msg = await TradexCommon.Kafka.getInstance().sendRequestAsync(txId, forwardResult.topic, forwardResult.uri, body);
  logger.info(`${txId} request ${forwardResult.uri} took ${new Date().getTime() - t} ms`);
  if (msg.data.status) {
    if (conf.enableLoggingOutIfQueryGetCodes) {
      if (conf.defaultLogoutIfGetStatusCodes.indexOf(msg.data.status.code) > -1) {
        logger.info(socket.id, loginMap[socket.id], "Seem the session is destroyed. logging user out");
        socket.deauthenticate();
        delete loginMap[socket.id];
      }
    }
    respondErrorObject(res, msg.data, lang);
  } else {
    res(null, msg.data);
  }
}


function respondCode(res, code, lang) {
  respondErrorObject(res, {
    status: {
      code: code
    }
  }, lang);
}

function respondErrorObject(res, object, lang) {
  let status = null;
  if (object) {
    if (object.code) {
      status = object;
    } else if (object.status != null && object.status.code != null) {
      status = object.status;
    }
  }
  try {
    res(TradexCommon.Utils.translateErrorMessage(status, lang));
  } catch (e) {
    logger.error("fail to translate error", status, lang, object, e);
    respondCode(res, "INTERNAL_SERVER_ERROR", lang)
  }
}

function getTxId(socketId, requestId) {
  if (requestId == null) {
    return `${socketId}-${new Date().getTime()}`;
  }
  return `${socketId}-${new Date().getTime()}-${requestId}`;
}



function getUserId(socket) {
  const authData = socket.getAuthToken();
  let id = null;
  if (authData != null && authData.token != null) {
    const token = authData.token;
    if (token.userId != null) {
      id = token.userId;
    } else if (token.userData != null) {
      id = token.userData.username;
    }
  }
  return id;
}

module.exports = {
  doQuery,
  forwardQuery,
  reallyForward,
  respondCode,
  respondErrorObject,
  getTxId,
  getUserId,
};
