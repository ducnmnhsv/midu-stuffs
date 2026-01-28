import { Inject, Service } from "typedi";
import config from "../config";
import { Errors, Kafka } from "tradex-common";
import { LANG_KEY_SYNC_TOPIC } from "../constants/updateTopics";
import LanguageService from "../services/admin/LanguageService";

@Service()
export default class LangKeySyncHandler {
  @Inject()
  private languageService: LanguageService;

  private apiMap: {
    [k: string]: (data: any, message?: Kafka.IMessage) => Promise<any>;
  }; // tslint:disable-line

  public init() {
    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      [LANG_KEY_SYNC_TOPIC],
      (message: any) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions,
    );

    this.apiMap = {
      add: (data: any) => this.languageService.addNewKey(data),
      update: (data: any) => this.languageService.updateLangByKey(data),
      delete: (data: any) => this.languageService.deleteKey(data),
    };
  }

  private handleRequest: Kafka.Handle = (message: Kafka.IMessage) => {
    if (message == null || message.data == null) {
      return Promise.reject(new Errors.SystemError());
    }
    const func: (data: any, msg: Kafka.IMessage) => Promise<any> =
      this.apiMap[message.uri]; // tslint:disable-line
    if (func != null) {
      return func(message.data, message);
    } else {
      return false;
    }
  };
}
