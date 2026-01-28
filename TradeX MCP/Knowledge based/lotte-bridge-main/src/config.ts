/* tslint:disable */
import { Logger, Utils } from 'tradex-common';
import { LOTTE_API_CATEGORIES, API } from './constants/LotteAPI';
import * as fs from 'fs';

const nodeId = Utils.getMsTime();
export const domain: string = process.env.TRADEX_ENV_DOMAIN || ''; //mas vs kis

const config = {
  enableEncryptPassword: process.env.TRADEX_ENV_ENABLE_ENCRYPT_PASSWORD,
  rsa: {
    publicKeyFile: `keys/lotte/${domain}/rsa-public.key`,
    privateKeyFile: `keys/lotte/${domain}/rsa-private.key`,
  },
  db: {
    mysql: {
      host: '172.33.30.21',
      port: 3306,
      username: 'admin',
      password: '7TCDcWyKmxZFgW7RYSG2VWEL',
      database: 'lotte-bridge',
      poolSize: 10,
      synchronize: false,
      logging: false,
      timezone: 'Z',
      supportBigNumbers: true,
      bigNumberStrings: false,
    },
  },
  logger: {
    config: {
      appenders: {
        application: { type: 'console' },
        file: {
          type: 'file',
          filename: '/logs/application.log',
          compression: true,
          maxLogSize: 10485760,
        },
      },
      categories: {
        default: {
          appenders: ['application', 'file'],
          level: 'info',
        },
      },
    },
  },
  log: {
    serviceName: 'lotte-bridge',
    format: 'FLAT', // 'FLAT' or 'JSON'
    transport: [],
  },
  turnOnTrimPassword: true,
  clusterId: 'lotte-bridge',
  clientId: `lotte-bridge-${nodeId}`,
  nodeId: nodeId,
  kafkaUrls: ['localhost:9092'],
  kafkaCommonOptions: {},
  kafkaConsumerOptions: {},
  kafkaProducerOptions: {},
  kafkaTopicOptions: {},
  topic: {
    bidOfferOddLotUpdate: 'bidOfferOddLotUpdate',
    indexStockListUpdate: 'indexStockListUpdate',
  },
  redis: {
    port: 6379,
    host: '127.0.0.1',
    options: {},
  },
  passwordLength: 8,
  otpGenTime: 60,
  otpMaxGenTime: 5,
  queryLogLength: 16384,
  queryLogSub: 16000,
  defaultTimeOut: 60 * 60 * 1000,
  defaultShortRedisDuration: 60 * 60 * 6,
  tokenRedisDuration: 60 * 60 * 24 * 30,
  defaultLongRedisDuration: 60 * 60 * 24 * 7,
  allowMultipleLogin: true,
  defaultSubNumber: '00',
  defaultLanguage: 'V',
  defaultFromDate: '19700101',
  defaultNextDateDesc: '99999999',
  defaultNextDateAsc: '00000000',
  defaultNextKeyDesc: '99999999999999999999',
  defaultNextKeyAscOrder: '00000000000000000000',
  defaultBankCode: '9999',
  defaultStockCode: '%',
  defaultDeptCode: '100',
  defaultPlatform: '%',
  defaultFetchCount: 20,
  cacheBosExpired: 3600,
  platformDifiSoft: 'FINTECH(DIFISOFT)',
  platform: {
    'NHSV_MTS_IOS': 31,
    'NHSV_MTS_ANDROID': 32,
    'FINTECH(DIFISOFT)': 42,
    'PAAVE.ANDROID': 42,
    'PAAVE.IOS': 42,
    'M.PAAVE.OS': 42,
    'M.PAAVE.AN': 42,
  },
  otpLifeTime: {
    sms: {
      REGISTER: 600,
      RESET_PASSWORD: 600,
      NEW_DEVICE: 600,
      E_KYC: 600,
      DEFAULT_EXPIRE_TIME: 600,
      nhsv: {
        TRIGGER_TIME_INTERVAL: 30,
        MAX_DAILY_SENT: 5,
      },
    },
    email: {
      REGISTER: 900,
      RESET_PASSWORD: 900,
      NEW_DEVICE: 900,
      E_KYC: 900,
      DEFAULT_EXPIRE_TIME: 900,
    },
  },
  notiMethod: {
    SMS: 'SMS',
    EMAIL: 'EMAIL',
    PHONE_NO: 'SMS',
  },
  maxOtpNumber: 999999,
  otpLength: 6,
  lastToken: 'lastToken',
  timeQuery: 'timeQuery',
  defaultConfirmStatus: '%',
  defaultChannelType: '%',
  schedule: {
    queryOddLot: '* 2,3,4,6,7,8 * * MON-FRI',
    getIndexList: '0,15,30,55 1 * * MON-FRI',
  },
  enableQueryOddLot: true,
  lotte: {
    apis: API,
    baseUrl: {
      [LOTTE_API_CATEGORIES.LOGIN]: 'http://172.33.30.23:8100',
      [LOTTE_API_CATEGORIES.ACCOUNT]: 'http://172.33.30.23:8100',
      [LOTTE_API_CATEGORIES.ORDER]: 'http://172.33.30.23:8100',
      [LOTTE_API_CATEGORIES.BALANCE]: 'http://172.33.30.23:8100',
      [LOTTE_API_CATEGORIES.NOTIFICATION]: 'http://172.31.224.7:30595',
      [LOTTE_API_CATEGORIES.EKYC]: 'http://172.33.30.23:8100',
      [LOTTE_API_CATEGORIES.MARKET]: 'http://172.33.30.23:8100',
    },
    errorCodeSuccess: {
      equity: '0000',
    },
    errorCodeBusiness: ['1005'],
    apiKey: 'ZV0N9HEpUXsk6Xb1MFX6I5mVc18urCdm',
    isGetPhoneNumberFromTuxedo: false,
  },
  tuxedo: {
    topic: 'tuxedo',
    apis: {
      accountMobile: '/api/v1/equity/account/mobile',
    },
  },
  timeouts: {
    otpService: null,
  },
  apiNotRequireQueryHeaderTokenUserData: [
    'post:/api/v1/lotte/login',
    'post:/api/v1/login/otp',
    'get:/api/v1/login/otp',
    'post:/api/v1/lotte/login/sec/verifyOTP',
    'post:/api/v1/login/otp/verify',
    'post:/api/v1/lotte/login/verify',
    'get:/api/v1/ekycs/banks',
    'get:/api/v1/ekycs/branch',
    'get:/api/v1/ekycs/banks/{id}/branches',
    'post:/api/v1/equity/account/checkNationalId',
    'get:/api/v1/ekycs/partner',
    'get:/api/v1/ekycs/account/exist',
    'post:/api/v1/lotte/equity/account/notification/settings',
    'get:/api/v1/lotte/equity/account/notification/settings',
    'get:/api/v1/lotte/equity/account/contractStatus',
    'get:/api/v1/lotte/equity/account/vsdStatus',
    'get:/api/v2/market/symbol/oddlotLatest',
    'get:/api/v1/lotte/equity/loan/estimatedFee',
    'get:/api/v2/market/stock/ranking/period',
    'get:/api/v2/market/symbol/{symbol}/right',
    'get:/api/v2/market/cw/{symbol}/detail',
  ],
  forwards: [
    {
      pattern: 'post:/tradexStopOrderForward/api/v1/stopOrder',
      topic: 'order',
      uri: 'post:/api/v1/stopOrder',
    },
    {
      pattern: 'get:/tradexStopOrderForward/api/v1/stopOrder/history',
      topic: 'order',
      uri: 'get:/api/v1/stopOrder/history',
    },
    {
      pattern: 'put:/tradexStopOrderForward/api/v1/stopOrder/cancel',
      topic: 'order',
      uri: 'put:/api/v1/stopOrder/cancel',
    },
    {
      pattern: 'put:/tradexStopOrderForward/api/v1/stopOrder/modify',
      topic: 'order',
      uri: 'put:/api/v1/stopOrder/modify',
    },
  ],
};

try {
  const configFileStr = fs.readFileSync('env.js', 'utf8');
  const vm = require('vm');
  const script = new vm.Script(configFileStr);
  script.runInNewContext({
    conf: config,
    config: config,
    process,
  });
} catch (e) {
  console.error('fail to load external configuration', e);
  Logger.error('fail to load external configuration', e);
}

config.kafkaConsumerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaConsumerOptions ? config.kafkaConsumerOptions : {}),
};
config.kafkaProducerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaProducerOptions ? config.kafkaProducerOptions : {}),
};

export default config;
