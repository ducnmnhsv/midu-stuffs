import {Kafka} from "tradex-common";
import {handleAAAMessage} from "./HandleAAARequest";
import conf from "./../conf";


export function init(readyCallback?: () => void) {
  const handler: Kafka.MessageHandler = new Kafka.MessageHandler();
  new Kafka.StreamHandler(conf, conf.kafkaConsumerOptions,
    [conf.clusterId],
    (message: any) => handler.handle(message, handleAAAMessage), {}, readyCallback);
}
