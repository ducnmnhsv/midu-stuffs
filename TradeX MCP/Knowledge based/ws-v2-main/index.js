const conf = require('./conf');
const ServerTradexWs = require('./server');
const options = {
    wsEngine: conf.wsEngine || 'ws',
    port: Number(process.env.SOCKETCLUSTER_PORT) || conf.port | 8000,
    allowClientPublish: true,
    appName: conf.clusterId || process.env.SOCKETCLUSTER_APP_NAME || 'tradex-ws-new',
    socketChannelLimit: Number(process.env.SOCKETCLUSTER_SOCKET_CHANNEL_LIMIT) || conf.channelLimit || 4000,
    authKey: conf.authKey || process.env.SCC_AUTH_KEY || '7b3db5832f3c559a12494374d7e2',
    handshakeTimeout: conf.handshakeTimeout || 10000,
    ackTimeout: conf.ackTimeout || 10000,
    pingTimeout: conf.pingTimeout || 20000,
    pingInterval: conf.pingInterval || 8000,
    pingTimeoutDisabled: false,
    origins: conf.origins || '*:*',
    path: conf.wsPath || '/socketcluster/',
    authPrivateKey: null,
    authPublicKey: null,
    authAlgorithm: null,
    authVerifyAlgorithms: null,
    authSignAsync: false,
    authVerifyAsync: true,
    authDefaultExpiry: 86400,
    middlewareEmitWarnings: true,
    pubSubBatchDuration: null,
    perMessageDeflate: undefined,
    maxPayload: undefined,
    wsEngineServerOptions: undefined 
};

console.log("starting......", conf);
try {
    new ServerTradexWs(options).start()
} catch (e) {
    console.error(e);
}

