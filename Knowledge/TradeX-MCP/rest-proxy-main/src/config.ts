/* tslint:disable */
import { v4 as uuid } from 'uuid';
import * as fs from 'fs';
import { Logger, Utils } from 'tradex-common';
import { exit } from 'process';

const nodeId = process.env.TRADEX_ENV_NODE_ID
  ? (process.env.TRADEX_ENV_INSTANCE_ID ? `${process.env.TRADEX_ENV_NODE_ID}-${process.env.TRADEX_ENV_INSTANCE_ID}` : process.env.TRADEX_ENV_NODE_ID)
  : uuid();
const domain = Utils.getEnvStr('TRADEX_ENV_DOMAIN', Utils.TRADEX_DOMAIN);
const basePath = '/api/v1';

let config: any = {
  domain: domain,
  subDomains: Utils.getEnvArr('TRADEX_ENV_DOMAINS'),
  scopes: {
    publicScopeGroups: ['PUBLIC'],
  },
  enableTranslation: false,
  responseCode: {
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    URI_NOT_FOUND: 404,
    INTERNAL_SERVER_ERROR: 500,
    REQUEST_TIMEOUT: 504,
    SERVICE_DOWN: 500,
  },
  fieldType: {
    integer: [], // process.env.TRADEX_ENV_REST_PROXY_FIELD_TYPE_INTEGERS.split(";"),
    boolean: [], // process.env.TRADEX_ENV_REST_PROXY_FIELD_TYPE_BOOLEANS.split(";"),
    array: [], // process.env.TRADEX_ENV_REST_PROXY_FIELD_TYPE_ARRAYS.split(";"),
  },
  instance: [], // process.env.TRADEX_ENV_REST_PROXY_INSTANCE,// Can be "admin" for Admin API, or "api" for Client API
  jwt: {
    publicKeyFile: `keys/aaa/${domain}/jwt-public.key`,
    domains: {
      [domain]: {
        publicKeyFile: `keys/aaa/${domain}/jwt-public.key`,
      },
    },
  },
  encryptPassword: {
    '/post/api/v1/login': ['password'],
    // '/post/api/v1/user': ['password'],
  },
  basePath: basePath,
  port: 3000,
  timeout: 20000, //Milliseconds
  topic: {
    configuration: 'configuration',
  },
  uri: {
    getAllScopes: '/api/v1/admin/scope',
    queryOpenApiList: '/api/v1/openApi/list',
    queryUnmatchedOpenApi: '/api/v1/openApi/unmatched/list',
    scopeGroup: '/api/v1/admin/scopeGroup',
  },
  fileDir: {
    scope: '/data/scopeData.json',
    openApi: '/data/openApi.json',
  },
  cors: {},
  key: "",
  clusterId: 'rest-proxy',
  clientId: nodeId,
  kafkaUrls: Utils.getEnvArr('TRADEX_ENV_KAFKA_URLS'),
  kakfa: {
    requestTopic: 'aaa',
  },

  logger: {
    config: {
      appenders: {
        application: { type: 'console' },
        file: {
          type: 'file',
          filename: '/logs/application.log',
          compression: true,
          maxLogSize: 104857600,
          backups: 10,
        },
      },
      categories: {
        default: { appenders: ['application', 'file'], level: 'info' },
      },
    },
  },
  enableEncryptPassword:
    process.env.TRADEX_ENV_ENABLE_ENCRYPT_PASSWORD === 'true',
  rsa: {
    publicKeyFile: `keys/aaa/${domain}/rsa-public.key`,
    publicKey: `this will be set later`,
  },
  apiEncryptList: [
    '/post/api/v1/kbfina/user/login',
    '/post/api/v1/kbfina/user/verifyPin',
  ],
  fptCallBackApi: ['/post/api/v1/kbfina/notification/update-result'],
  fptAuthToken:
    'mVPo2BTrH8SHlF80Zn36v0fhFRTrHBlBfEKwWzW1Pkc5qRxEolErCeFmx6IaAD1z',
  overriedAPI: true,
  enableDebug: false,
  isMaintain: false,
  allowUser: ['0945486357', '0977549678'],
  apiKey: '123',
  forwards: [
    {
      pattern: '/kis-forward/api/v1/eqt/order',
      method: 'post',
      type: 'kafka',
      topic: 'paave-real-trading',
      uri: 'post:/api/v1/real-trading/kis/eqt/order',
      secToken: 'decode', // decode, verify
    },
    { // check all config here: https://github.com/chimurai/http-proxy-middleware
      logger: console,
      pattern: '/kis-forward/',
      type: 'http',
      target: 'https://202.87.214.110:8443/rest',
      pathRewrite: {'^/kis-forward/' : '/'},
      changeOrigin : true,
      secure: false,
    }
  ],
};

// delete above forwards configure. let's it there for reference in source code
config.forwards = [];

const initLogger = () => {
  try {
    Logger.create(config.logger.config, true);
  } catch(err) {
    console.error('fail to init Logger', err);
    exit(1);
  }
};

try {
  if (fs.existsSync('env.js')) {
    const configFileStr = fs.readFileSync("env.js", "utf8");
    const vm = require("node:vm");
    const script = new vm.Script(configFileStr);
    script.runInNewContext({
      conf: config,
      config: config,
      process,
      console,
    });
  } else if (fs.existsSync('env.json')) {
    let externalConfigContent = fs.readFileSync("env.json", "utf-8");
    let externalConfig = JSON.parse(externalConfigContent);
    Utils.override(externalConfig, config);
  }
  initLogger();
} catch (e) {
  initLogger();
  Logger.error("fail to load external configuration", e);
}
// when injecting from VM. some object become not plain any more ifusing isPlainObject lib
config = JSON.parse(JSON.stringify(config));


Utils.processJwtKey(config);
config.rsa.publicKey = fs.readFileSync(config.rsa.publicKeyFile, 'utf8');

export default config;
