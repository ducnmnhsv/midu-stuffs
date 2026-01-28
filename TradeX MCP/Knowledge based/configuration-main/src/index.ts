import config from "./config";
import "reflect-metadata";
import { AppDataSource } from "./AppDataSource";
import { AWS, Kafka, Logger } from "tradex-common";
import { Container } from "typedi";
import RequestHandler from "./consumers/RequestHandler";
import { DataSource } from "typeorm";
// import ClientSyncHandler from './consumers/ClientSyncHandler';
// import LangKeySyncHandler from './consumers/LangKeySyncHandler';
// import ScopeSyncHandler from './consumers/ScopeSyncHandler';
// import ScopeGroupSyncHandler from './consumers/ScopeGroupSyncHandler';
// import DbSyncService from './services/DbSyncService';
// import DbSyncHandler from './consumers/DbSyncHandler';

Logger.create(config.logger.config, true);
Logger.info("staring...");

AppDataSource.initialize()
  .then(async (_: DataSource) => {
    Kafka.create(
      config,
      config.kafkaConsumerOptions,
      true,
      config.kafkaTopicOptions,
      config.kafkaProducerOptions,
    );
    const requestHandler = Container.get(RequestHandler);
    requestHandler.init();

    AWS.loadCredentials(config.aws);

    // if (config.domain !== Utils.TRADEX_DOMAIN) {
    //   const clientSyncHandler = Container.get(ClientSyncHandler);
    //   clientSyncHandler.init();
    //   const langKeySyncHandler = Container.get(LangKeySyncHandler);
    //   langKeySyncHandler.init();
    //   const scopeGroupSyncHandler = Container.get(ScopeGroupSyncHandler);
    //   scopeGroupSyncHandler.init();
    //   const scopeSyncHandler = Container.get(ScopeSyncHandler);
    //   scopeSyncHandler.init();
    //   const dbSyncHandler = Container.get(DbSyncHandler);
    //   dbSyncHandler.init();
    // } else {
    // }
  })
  .catch((error: any) => Logger.error(error));
