import config from './config';
import 'reflect-metadata';
import { connectToMongo } from './utils/dbConnection';
import { Kafka, Logger, TradexNotification } from 'tradex-common';
import { Container } from 'typedi';
import RequestHandler from './consumers/RequestHandler';
import RedisService from './services/RedisService';
import CacheService from './services/CacheService';

Logger.info('Starting...');

async function init() {
  await connectToMongo();
  Logger.info('connected to database!');
  const redisService = Container.get(RedisService);
  await redisService.init();
  Logger.info('connected to redis!');
  const topicConf = {
    ...config.kafkaTopicOptions,
    'auto.offset.reset': 'earliest',
  };
  Kafka.create(config, config.kafkaConsumerOptions, true, topicConf, config.kafkaProducerOptions);
  TradexNotification.create(Kafka.getInstance());
  Logger.info(`Init cache done!`);
  const requestHandler = Container.get(RequestHandler);
  requestHandler.init();
  const cacheService = Container.get(CacheService);
  await cacheService.init();
}

init()
  .then()
  .catch((error: any) => {
    Logger.error(error);
    process.exit(1);
  });
