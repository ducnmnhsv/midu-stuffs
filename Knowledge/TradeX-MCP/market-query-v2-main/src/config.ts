/* tslint:disable */
import * as fs from 'fs';
import { RedisClientOptions } from 'redis';
import { Appender } from 'log4js';
import { Logger } from 'tradex-common';

export interface IConfDb {
  client: string,
  connection: {
    url: string;
    database: string;
  },
  options: {},
}

export interface IConf {
  domain: string;
  tradex_rest_api_host: string;
  db: IConfDb;
  redis: RedisClientOptions,
  logger: {
    config: {
      appenders: { [k: string]: Appender},
      categories: {
        [k: string]: { 
          appenders: string[];
          level: string;
        };
      };
    };
  };
  topic: { [s: string]: string};
  crawlingChart: {
    url: string;
    defaultFrom: number;
  };
  holidays: string[];
  clusterId: string;
  clientId: string;
  kafkaUrls: string[];
  kafkaCommonOptions: {[k: string]: string};
  kafkaConsumerOptions: {[k: string]: string};
  kafkaProducerOptions: {[k: string]: string};
  kafkaTopicOptions: {[k: string]: string};
  requestHandlerTopics: string[];
}

let config: IConf = {
  domain: '${TRADEX_ENV_DOMAIN}',
  tradex_rest_api_host: 'https://rest-api.tradex.vn',
  db: {
    client: 'mongodb',
    connection: {
      url: 'mongodb://localhost:27017/tradex-market',
      database: 'tradex-market',
    },
    options: {},
  },
  redis: {
    url: 'redis://localhost:6379'
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
          backups: 100,
        },
      },
      categories: {
        default: { appenders: ['application', 'file'], level: 'info' },
      },
    },
  },
  topic: {
    marketJob: 'marketJob-v2',
    businessInfo: 'business-info',
    marketDataCrawler: 'market-data-crawler',
  },
  crawlingChart: {
    url: 'https://api.vietstock.vn/ta/history?symbol={code}&resolution={resolution}&from={from}&to={to}',
    defaultFrom: 949338000,
  },
  holidays: [
    '20210101',
    '20210210',
    '20210211',
    '20210212',
    '20210215',
    '20210216',
    '20210421',
    '20210430',
    '20210503',
    '20210902',
  ],
  clusterId: 'market-v2',
  clientId: process.env.TRADEX_ENV_INSTANCE_ID != null ? `${process.env.TRADEX_ENV_NODE_ID}${process.env.TRADEX_ENV_INSTANCE_ID}` : process.env.TRADEX_ENV_NODE_ID,
  kafkaUrls: ['localhost:9092'],
  kafkaCommonOptions: {},
  kafkaConsumerOptions: {},
  kafkaProducerOptions: {},
  kafkaTopicOptions: {},
  requestHandlerTopics: [],
};

function initLogger() {
  try {
    Logger.create(config.logger.config, true);
  } catch(err) {
    console.error('fail to init logger');
    process.exit(1);
  }
}

try {
  const configFileStr = fs.readFileSync("env.js", "utf8");
  const vm = require("node:vm");
  const script = new vm.Script(configFileStr);
  script.runInNewContext({
    conf: config,
    config: config,
    process,
    console,
  });
  initLogger();
} catch (e) {
  initLogger();
  Logger.error("fail to load external configuration", e);
}

config.kafkaConsumerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaConsumerOptions ? config.kafkaConsumerOptions : {}),
};
config.kafkaProducerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaProducerOptions ? config.kafkaProducerOptions : {}),
};

if (config.requestHandlerTopics.length === 0) {
  config.requestHandlerTopics.push(config.clusterId);
}

export default config;
