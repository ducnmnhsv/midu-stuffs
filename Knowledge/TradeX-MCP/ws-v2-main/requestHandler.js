const { Errors, Logger } = require('tradex-common');

const { 
  getV2SymbolInfo, 
} = require('./marketApi');

/**
 * 
 * After process. if success the response data will have format:
 * {
 *   data: <response data>
 * }
 * If fail:
 * {
 *   status: {
 *     code: "",
 *     params: <each field error>,
 *     messageParams: []
 *   }
 * }
 * @param {*} req socket cluster request
 * @param {*} requestData request data in emit function. requestData should have format
 * {
 *   headers: {
 *      rid: <requestId>,
 *      ...
 *   },
 *   body: <request body>,
 * }
 * @param {*} res socket cluster response object
 * 
 */

function requestHandler(api, socket, requestData, res) {
  const headers = requestData.headers;
  const body = requestData.body;
  const rid = headers?.rid;
  const txId = getTxId(socket, rid);
  try {
    let response = null;
    if (api === '/api/v2/market/symbolInfo') {
      response = getV2SymbolInfo(txId, rid, headers, body);
    } else {
      throw new Errors.UriNotFound();
    }
    Promise.resolve(response).then(realReponse => res(null, {  data: realReponse })).catch(e => handleErr(txId, api, res, e));
  } catch(e) {
    handleErr(txId, api, res, e);
  }
}

function handleErr(txId, api, res, e) {
  Logger.error(txId, 'fail to handle api', api, e);
    if (e instanceof Errors.GeneralError) {
      res(null, { status: e.toStatus() });
    } else {
      res(null, { status: {
        code: 'INTERNAL_SERVER_ERROR'
      } });
    }
}


let msgId = 0;

function getTxId(socket, rid) {
  msgId++;
  return rid ? `${socket.id}-${msgId}-${rid}` : `${new Date().getTime()}-${msgId}`;
}

module.exports = {
  requestHandler,
}