const { Errors, Utils } = require('tradex-common');
const conf = require('./conf');
const { returnSymbolSnapShotInfo } = require('./returnSnapShot');



async function getV2SymbolInfo(txId, rid, headers, body) {
  const isArray = Array.isArray(body);
  if (body == null || (isArray && body.length === 0)) {
    return [];
  }

  if (!isArray || body.length > conf.marketApi.maxNumberOfSymbols) {
    throw new Errors.GeneralError('INVALID_SYMBOLS');
  }

  return (await Utils.allPromiseDone(body.map(returnSymbolSnapShotInfo))).filter(it => it.result != null).map(it => it.result);
}

module.exports = {
  getV2SymbolInfo,
};