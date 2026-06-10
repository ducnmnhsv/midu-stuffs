import config from './config';
import { Kafka, Logger, TradexNotification } from 'tradex-common';
import { connectSocket } from './socket/SocketClient';

Logger.create(config.logger.config, true);
Logger.info('Staring...');

async function init() {
  Kafka.create(config, config.kafkaConsumerOptions, true, config.kafkaTopicOptions, config.kafkaProducerOptions);
  TradexNotification.create(Kafka.getInstance());
  connectSocket();
}

init()
  .then()
  .catch((error: Error) => {
    Logger.error(error);
    process.exit(1);
  });
