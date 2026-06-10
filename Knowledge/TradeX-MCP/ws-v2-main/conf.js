const uuid = require('uuid');
const TradexCommon = require('tradex-common');
const fs = require('fs');
const vm = require('vm');

let domain = process.env.TRADEX_ENV_DOMAIN == null ? TradexCommon.Utils.TRADEX_DOMAIN : process.env.TRADEX_ENV_DOMAIN;
domain = 'nhsv'
let conf = {
  port: 8000,
  domain: domain,
  // https://github.com/redis/node-redis/blob/master/docs/client-configuration.md
  redis: {
    url: 'redis://172.33.30.22:6379'
  },
  wsEngine: 'ws',
  wsPath: '/socketcluster/',
  handshakeTimeout: 10000,
  ackTimeout: 10000,
  pingTimeout: 20000,
  pingInterval: 8000,
  origins: "*:*",
  logger: {
    config: {
      appenders: { application: { type: 'dateFile', filename: '/logs/application.log', maxLogSize: 100000000 } },
      categories: { default: { appenders: ['application'], level: 'info' } },
    },
  },
  clusterId: 'ws-v2',
  clientId: process.env.TRADEX_ENV_INSTANCE_ID == null || process.env.TRADEX_ENV_INSTANCE_ID === '' ? process.env.TRADEX_ENV_NODE_ID : `${process.env.TRADEX_ENV_NODE_ID}.${process.env.TRADEX_ENV_INSTANCE_ID}`,  
  kafkaUrls: [
    '172.33.30.21:9092'
  ],
  kafkaCommonOptions: {},
  kafkaConsumerOptions: {},
  kafkaProducerOptions: {},
  kafkaTopicOptions: {},
  jwt: {
    publicKeyFile: `keys/aaa/${domain}/jwt-public.key`,
    domains: {
      [domain]: {
        publicKeyFile: `keys/aaa/${domain}/jwt-public.key`
      }
    }
  },
  rsa: {
    publicKeyFile: `keys/aaa/${domain}/rsa-public.key`,
    publicKey: '',
  },
  accessToken: {
    expiredInSeconds: 30000
  },
  refreshToken: {
    expiredInSeconds: 86400,
    expiredInSecondsWithRememberMe: 2592000
  },
  i18nNamespaceList: ['message', 'field'],
  scopes: {
    specialScopes: {
      verifyOtpScope: 'VERIFY_OTP',
      unAuthenticated: 'PUBLIC',
    },
    loadFrom: {
      topic: "configuration",
      uris: {
        scope: "/api/v1/admin/scope",
        scopeGroup: "/api/v1/admin/scopeGroup",
        pageSize: 100,
      },
    },
    maximumAliveTime: 86400000,
  },
  authKey: "7b3db5832f3c559a12494374d7e2",
  channelLimit: 4000,
  getIpFromXForwardedFor: false,
  serviceDataDir: "serviceData",
  authenticatedChannels: [
    {
      pattern: "tradex.notify.global",
      type: "exact",
    },
    {
      pattern: /domain\.notify\.account\..*/,
      type: "regex",
    },
    {
      pattern: /tradex\.notify\.user\..*/,
      type: "regex",
    }
  ],
  scopeCachedFile: "/data/scopesCached.json",
  defaultLogoutIfGetStatusCodes: [
    "INVALID_CLIENT_CREDENTIAL",
  ],
  enableLoggingOutIfQueryGetCodes: false,
  allowOnly1SessionPerUser: false,
  allowOnly1SessionPerUserConf: {
    verifyType: "checkTokenId",
    verifyApis: {
      checkTokenId: {
        uri: "/api/v1/equity/checkTokenId",
        topic: "fss-rest-bridge",
      }
    }
  },
  isHandlerKafkaV2: process.env.TX_ENV_HANDLER_KAFKA_V2 === 'true' || process.env.IS_HANDLER_KAFKA_V2 === 'Y',
  enableEncryptPassword: process.env.TRADEX_ENV_ENABLE_ENCRYPT_PASSWORD === 'true',
  // only apply for market v2
  returnSnapshot: {
    enable: true,
    ttl: 300000, // time that latest cache from Db alive
    callbackChannel: 'market.returnSnapshot'
  },
  communicationTopic: null, // will be set automatically if not set
  marketApi: {
    maxNumberOfSymbols: 15,
  }
};

global.systemConf = conf;

try {
  const configFileStr = fs.readFileSync("env.js", "utf8");
  const script = new vm.Script(configFileStr);
  script.runInNewContext({conf: conf});
  TradexCommon.Logger.create(conf.logger.config, true);
} catch (e) {
  TradexCommon.Logger.create(conf.logger.config, true);
  TradexCommon.Logger.error("fail to load external configuration", e);
}

TradexCommon.Logger.warn('final conf', conf);

conf.kafkaConsumerOptions = {
  ...(conf.kafkaCommonOptions ? conf.kafkaCommonOptions : {}),
  ...(conf.kafkaConsumerOptions ? conf.kafkaConsumerOptions : {}),
};

conf.kafkaProducerOptions = {
  ...(conf.kafkaCommonOptions ? conf.kafkaCommonOptions : {}),
  ...(conf.kafkaProducerOptions ? conf.kafkaProducerOptions : {}),
};

TradexCommon.Utils.processJwtKey(conf);
conf.rsa.publicKey = fs.readFileSync(conf.rsa.publicKeyFile, "utf8");
if (conf.communicationTopic == null) {
  conf.communicationTopic = `${conf.clusterId}.update`;
}

if (conf.isHandlerKafkaV2 !== true) {
  TradexCommon.Logger.warn("disable return snapshot since market is not V2");
  conf.returnSnapshot.enable = false;
}

module.exports = conf;
