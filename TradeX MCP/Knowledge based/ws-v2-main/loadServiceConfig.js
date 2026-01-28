const fs = require('fs');
const TradexCommon = require('tradex-common');
const { respondCode } = require('./query');
const conf = require('./conf');

function loadServiceConfig(socket, data, res) {
  TradexCommon.Logger.info(socket.id, "load service config", data);
  try {
    const serviceName = data.body.serviceName;
    const lang = TradexCommon.Utils.getLanguageCode(data.headers['accept-language']);
    const fileName = `${conf.serviceDataDir}/${serviceName}.json`;
    fs.readFile(fileName, "utf-8", (err, data) => {
      if (err != null) {
        TradexCommon.Logger.error("error while reading file", socket.id, fileName, err);
        respondCode(res, "INTERNAL_SERVER_ERROR", lang);
      } else {
        try {
          res(null, {
            data: JSON.parse(data)
          });
        } catch (e) {
          TradexCommon.Logger.error("error while parsing json from file", socket.id, fileName, data, err);
          respondCode(res, "INTERNAL_SERVER_ERROR", lang);
        }
      }
    });
  } catch (e) {
    TradexCommon.Logger.error(socket.id, "fail to load service", data, e);
    respondCode(res, "INTERNAL_SERVER_ERROR", 'en');
  }
}

module.exports = {
  loadServiceConfig,
};