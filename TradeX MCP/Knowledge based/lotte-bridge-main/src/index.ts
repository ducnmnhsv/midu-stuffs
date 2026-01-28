import config from './config';
import 'reflect-metadata';
import { Kafka, Logger, TradexNotification } from 'tradex-common';
import RedisService from './init/RedisService';
import { Container } from 'typedi';
import RequestHandler from './consumers/RequestHandler';
// import { OddLotService } from './services/OddLotService';
import connection from './init/dbConnection';
import { IndexService } from './services/IndexService';

Logger.create(config.logger.config, true);
Logger.info('Staring...');

async function init() {
  Kafka.create(config, config.kafkaConsumerOptions, true, config.kafkaTopicOptions, config.kafkaProducerOptions);

  const redisService = Container.get(RedisService);
  await redisService.init();
  Logger.info('connected to redis!');

  await connection;
  Logger.info('connected to database!');

  TradexNotification.create(Kafka.getInstance());

  const requestHandler = Container.get(RequestHandler);
  requestHandler.init();
  // const oddLotService = Container.get(OddLotService);
  // await oddLotService.queryOddLot();
  const indexService = Container.get(IndexService);
  await indexService.getIndexList();
}

init()
  .then()
  .catch((error: Error) => {
    Logger.error(error);
    process.exit(1);
  });
