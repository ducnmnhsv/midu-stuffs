import { Inject, Service } from "typedi";
import config from "../config";
import { Errors, Kafka } from "tradex-common";
import ScopeGroupService from "../services/admin/ScopeGroupService";
import { SCOPE_GROUP_SYNC_TOPIC } from "../constants/updateTopics";

@Service()
export default class ScopeGroupSyncHandler {
  @Inject()
  private scopeGroupService: ScopeGroupService;

  private apiMap: {
    [k: string]: (data: any, message?: Kafka.IMessage) => Promise<any>;
  }; // tslint:disable-line

  public init() {
    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      [SCOPE_GROUP_SYNC_TOPIC],
      (message: any) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions,
    );

    this.apiMap = {
      newScopeGroup: (data: any) =>
        this.scopeGroupService.addNewScopeGroup(data),
      updateScopeGroup: (data: any) =>
        this.scopeGroupService.updateScopeGroup(data),
      deleteScopeGroup: (data: any) =>
        this.scopeGroupService.deleteScopeGroup(data),
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
