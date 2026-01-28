import {
  Kafka,
  Logger,
  TradexNotification,
} from 'tradex-common';
import conf from "./conf";
import {init as initDb} from "./db/connection";
import {scopeService} from "./services/ScopeService";
import {updateScopeService} from "./services/UpdateScopeService";
import {init as initConsumer} from "./consumer";

const startupState = {
  kafka: false,
  scope: false,
  consumer: false,
};
Logger.create(conf.logger.config, true);

const initScopes = (firstTime?: boolean) => {
  scopeService.init(conf.retryTimes, () => {
    if (firstTime === true) {
      startupState.scope = true;
      finish();
    }
  }).then().catch((err: Error) => {
    Logger.error("fail to init scope", err);
  });
};

initDb(conf.db);
Kafka.create(conf, conf.kafkaConsumerOptions, true,
  conf.kafkaTopicOptions, conf.kafkaProducerOptions, () => {
    Logger.info(`finish init kafka`);
    startupState.kafka = true;
  });
TradexNotification.create(Kafka.getInstance());
initScopes(true);
initConsumer(() => {
  startupState.consumer = true;
  finish();
});
Logger.info(`starting with...`);
const finish = () => {
  if (startupState.scope && startupState.kafka && startupState.consumer) {
    updateScopeService.update(conf.retryTimes)
      .then(initScopes)
      .catch((err: Error) => Logger.error("cannot update scope", err));
  }
  Logger.info("startup finish");
};
