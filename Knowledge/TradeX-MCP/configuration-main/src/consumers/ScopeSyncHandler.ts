import { Inject, Service } from "typedi";
import config from "../config";
import { Errors, Kafka } from "tradex-common";
import ScopeService from "../services/admin/ScopeService";
import { SCOPE_SYNC_TOPIC } from "../constants/updateTopics";

@Service()
export default class ScopeSyncHandler {
  @Inject()
  private scopeService: ScopeService;

  private apiMap: {
    [k: string]: (data: any, message?: Kafka.IMessage) => Promise<any>;
  }; // tslint:disable-line

  public init() {
    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      [SCOPE_SYNC_TOPIC],
      (message: any) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions,
    );

    this.apiMap = {
      newScope: (data: any) => this.scopeService.saveNewScope(data),
      updateScope: (data: any) => this.scopeService.updateScope(data),
      deleteScope: (data: any) => this.scopeService.deleteScope(data),
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
