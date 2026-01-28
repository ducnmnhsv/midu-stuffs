const jwt = require('jsonwebtoken');
const conf = require('./conf');

async function decodeOtpToken(accessToken, jwtConf = null) {
  jwtConf = jwtConf ? jwtConf : conf.getJwt();
  return new Promise((resolve, reject) => {
    jwt.verify(accessToken, jwtConf.publicKey, {
      algorithm: 'RS256'
    }, (err, payload) => {
      if (err) {
        reject(err);
      } else {
        resolve({
          rId: payload.rId,
        });
      }
    });
  });
}

async function decodeAccessToken(accessToken, jwtConf = null) {
  jwtConf = jwtConf ? jwtConf : conf.getJwt();
  return new Promise((resolve, reject) => {
    jwt.verify(accessToken, jwtConf.publicKey, {
      algorithm: 'RS256'
    }, (err, payload) => {
      if (err) {
        reject(err);
      } else {
        resolve({
          domain: payload.dm,
          userId: payload.uId,
          serviceCode: payload.sc,
          connectionId: payload.conId ? payload.conId.connectionId : null,
          serviceId: payload.conId ? payload.conId.serviceId : null,
          serviceName: payload.conId ? payload.conId.serviceName : null,
          clientId: payload.cId,
          serviceUserId: payload.suId,
          loginMethod: payload.lm,
          refreshTokenId: payload.rId,
          scopeGroupIds: payload.sgIds,
          serviceUsername: payload.su,
          userData: payload.ud,
          platform: payload.pl,
          grantType: payload.gt,
          appVersion: payload.appV,
          osVersion: payload.osV,
          exp: payload.exp,
        });
      }
    });
  });
}

module.exports = {
  decodeOtpToken,
  decodeAccessToken,
};