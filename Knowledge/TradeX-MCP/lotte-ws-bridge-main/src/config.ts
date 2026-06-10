/* tslint:disable */
import { Logger, Utils } from 'tradex-common';
import * as fs from 'fs';

const nodeId = Utils.getMsTime();

const config = {
  clusterId: 'lotte-ws-bridge',
  clientId: `lotte-ws-bridge-${nodeId}`,
  kafkaUrls: ['localhost:9092'],
  kafkaCommonOptions: {},
  kafkaConsumerOptions: {},
  kafkaProducerOptions: {},
  kafkaTopicOptions: {},
  retryTimes: 3,
  defaultAvatar: 'https://s3-ap-southeast-1.amazonaws.com/tradex-vn/avatar/default.png',
  defaultKafkaTimeout: 10000,
  logger: {
    config: {
      appenders: {
        application: { type: 'console' },
        file: { type: 'file', filename: '/logs/application.log', compression: true, maxLogSize: 10485760, backups: 10 },
      },
      categories: {
        default: { appenders: ['application', 'file'], level: 'info' },
      },
    },
  },
  lotte: {
    websocketAddress: 'ws://172.33.30.23:9900',
  },
  duplicateCheckSize: 300,
  checkInterval: 1000,
  pingCycle: 10,
  checkReceivedCycle: 3,
  checkReceivedCycleMs: 6000, // reset by checkReceivedCycle
  checkReceivedCycleDouble: 6, // reset by checkReceivedCycle
  resubscribeCycle: 900, // 0 to disable
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

config.checkReceivedCycleDouble = 2 * config.checkReceivedCycle;
config.checkReceivedCycleMs = config.checkReceivedCycleDouble * 1000;

Logger.info('configuration after injecting:', config);

config.kafkaConsumerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaConsumerOptions ? config.kafkaConsumerOptions : {}),
};
config.kafkaProducerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaProducerOptions ? config.kafkaProducerOptions : {}),
};

export default config;
