/* tslint:disable */
import * as fs from "fs";
import {Logger, Utils} from "tradex-common";
import Global = global.NodeJS.Global;
import {OTP_TYPE_URI_MAP} from "./constants/OtpTypeUriMap";


declare let global: Global;

export const TRADEX_DOMAIN = Utils.TRADEX_DOMAIN;

let conf: any = {
  domain: Utils.getEnvStr("TRADEX_ENV_DOMAIN", TRADEX_DOMAIN),
  isEnableBiometric: Utils.getEnvBool("TRADEX_ENV_ENABLE_BIOMETRIC", false),
  isRegisBiometricWithPassword: Utils.getEnvBool("TRADEX_ENV_BIOMETRIC_VALIDATE_PASSWORD", false),
  clusterId: "aaa",
  clientId: `${process.env.TRADEX_ENV_NODE_ID}.${process.env.TRADEX_ENV_INSTANCE_ID}`,
  kafkaUrls: Utils.getEnvArr("TRADEX_ENV_KAFKA_URLS"),
  kafkaCommonOptions: {},
  kafkaConsumerOptions: {},
  kafkaProducerOptions: {},
  kafkaTopicOptions: {},
  retryTimes: 3,
  defaultLongRedisDuration: 60 * 60 * 24 * 7,
  defaultAvatar: "https://s3-ap-southeast-1.amazonaws.com/tradex-vn/avatar/default.png",
  defaultKafkaTimeout: 10000,
  // NHSV-193: the service connect to core may need to handle otp to lock account if they input worng many time
  // when enable this feature (like before), the otp is handle at aaa service
  // when disable this feature: otp verify will be call to msName in login method
  enableHandleOtp: true, 
  timeouts: {
    loginFacebook: 15000,
    loginGoogle: 15000,
    loginApple: 15000,
    loginTechx: 15000,
    loginPasswordOtp: 45000,
    otpService: null,
    loginDirectToService: 10000,
    loginDomain: null,
    loadScope: 30000,
  },
  rsa: {
    enableEncryptPassword: true,
    publicKey: `keys/aaa/${Utils.getEnvStr("TRADEX_ENV_DOMAIN", TRADEX_DOMAIN)}/rsa-public.key`,
    privateKey: `keys/aaa/${Utils.getEnvStr("TRADEX_ENV_DOMAIN", TRADEX_DOMAIN)}/rsa-private.key`,
  },
  db: {
    host: Utils.getEnvStr("TRADEX_ENV_MYSQL_HOST"),
    port: Utils.getEnvNum("TRADEX_ENV_MYSQL_PORT", 3306),
    user: Utils.getEnvStr("TRADEX_ENV_MYSQL_USER"),
    password: Utils.getEnvStr("TRADEX_ENV_MYSQL_PASSWORD"),
    connectionLimit: 10,
    database: "tradex-aaa",
    timezone: "UTC",
  },
  topic: {
    user: 'user',
  },
  jwt: {
    privateKeyFile: "keys/jwt-private.key",
    publicKeyFile: "keys/jwt-public.key",
    domains: {
      vcsc: {
        privateKeyFile: "keys/vcsc/jwt-private.key",
        publicKeyFile: "keys/vcsc/jwt-public.key",
      },
    },
  },
  accessToken: {
    expiredInSeconds: 900,
  },
  refreshToken: {
    expiredInSeconds: 86400,
    expiredInSecondsWithRememberMe: 2592000,
  },
  otpToken: {
    expiredInSeconds: 90,
  },
  logger: {
    config: {
      appenders: {
        application: {type: 'console'},
        file: {type: 'file', filename: '/logs/application.log', compression: true, maxLogSize: 10485760, backups: 10},
      },
      categories: {
        default: {appenders: ['application', 'file'], level: 'info'},
      },
    },
  },
  log: {
    serviceName: "aaa",
    format: "FLAT", // 'FLAT' or 'JSON'
    transport: [],
  },
  scopes: {
    verifyOtpScope: "VERIFY_OTP",
    unAuthenticated: "PUBLIC",
    loadFrom: {
      topic: "configuration",
      uris: {
        scope: "/api/v1/admin/scope",
        scopeGroup: "/api/v1/admin/scopeGroup",
        pageSize: 100,
      },
    },
  },
  clients: {
    loadFrom: {
      topic: "configuration",
      uris: {
        client: "/api/v1/system/client",
        pageSize: 100,
      },
    },
  },
  forceSecCode: null,
  checkMobileAppVersion: {
    android: {
      version: "1.0.17",
      url: "https://play.google.com/store/apps/details?id=com.tradex.vcsc",
    },
    ios: {
      version: "1.0.17",
      url: "https://apps.apple.com/us/app/v-mobile-new/id1492065191",
    },
  },
  cacheTimeout: 180000,
  otp_type_uri_map: OTP_TYPE_URI_MAP,  
  enableStrictSyncLinkAccount: false,
  redis: {
    port: 6379,
    host: '127.0.0.1',
    options: {},
  },
  nhsv: {
    OTP_SENT_KEY_MAX_LIFE_TIME: 86400,
    MAX_DAILY_SENT: 5,
    TRIGGER_TIME_INTERVAL: 30
  },
  enableForcePartnerClientId: true,
};
conf.checkMobileAppVersion = null;

global.systemConf = conf;

try {
  const configFileStr = fs.readFileSync("env.js", "utf8");
  const vm = require("node:vm");
  const script = new vm.Script(configFileStr);
  script.runInNewContext({
    conf: conf,
    config: conf,
    process,
  });
} catch (e) {
  console.error("fail to load external configuration", e);
  Logger.error("fail to load external configuration", e);
}

Logger.info('configuration after injecting:', conf);

conf.kafkaConsumerOptions = {
  ...(conf.kafkaCommonOptions ? conf.kafkaCommonOptions : {}),
  ...(conf.kafkaConsumerOptions ? conf.kafkaConsumerOptions : {}),
};
conf.kafkaProducerOptions = {
  ...(conf.kafkaCommonOptions ? conf.kafkaCommonOptions : {}),
  ...(conf.kafkaProducerOptions ? conf.kafkaProducerOptions : {}),
};

if (conf.domain !== Utils.TRADEX_DOMAIN) {
  conf.forceSecCode = conf.domain;
}
Utils.createJwtConfig(conf, conf.domain, Utils.getEnvArr("TRADEX_ENV_DOMAINS"), "keys", conf.clusterId,
  "jwt-public.key", "jwt-private.key");
Utils.processJwtKey(conf);

Object.keys(conf.timeouts).forEach(key => {
  if (conf.timeouts[key] == null) {
    conf.timeouts[key] = conf.defaultKafkaTimeout;
  }
});
export default conf;
