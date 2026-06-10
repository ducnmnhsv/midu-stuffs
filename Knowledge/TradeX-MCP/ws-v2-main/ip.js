const TradexCommon = require('tradex-common');
const ipaddr = require('ipaddr.js');
const conf = require("./conf");

function getIp(data, socket) {
  if (conf.getIpFromXForwardedFor) {
    try {
      if (socket.request != null && socket.request.headers != null) {
        let forwardedChains = socket.request.headers['x-forwarded-for'].split(",")
        if (forwardedChains.length > 0) {
          return forwardedChains[0]
        }
      }
    } catch (e) {
      TradexCommon.Logger.error('cannot acquire x-forwarded-for');
    }
  }
  if (data.sourceIp != null && data.sourceIp !== "") {
    return data.sourceIp;
  }
  if (data.body != null && data.body.sourceIp != null && data.body.sourceIp !== "") {
    return data.body.sourceIp;
  }
  if (socket.processedIp == null) {
    const ip = processIp(socket);
    TradexCommon.Logger.info("ip address for socket", socket.id, " is ", ip);
    socket.processedIp = ip;
  }
  return socket.processedIp;
};

function processIp(socket) {
  let ip = socket.remoteAddress;
  try {
    ip = ipaddr.parse(ip);
    if (ip instanceof ipaddr.IPv4) {
      return ip.toString();
    } else {
      if (ip.isIPv4MappedAddress) {
        return ip.toIPv4Address().toString();
      } else {
        return ip.toString();
      }
    }
  } catch (e) {
    TradexCommon.Logger.error("fail to parse ip", ip, socket.id, e);
  }
  return ip;
}

module.exports = {
  getIp,
  processIp,
};