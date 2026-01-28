import { Inject, Service } from "typedi";
import config from "../config";
import { Errors, Kafka } from "tradex-common";
import ClientService from "../services/admin/ClientService";
import { CLIENT_SYNC_TOPIC } from "../constants/updateTopics";

@Service()
export default class ClientSyncHandler {
  @Inject()
  private clientService: ClientService;

  private apiMap: {
    [k: string]: (data: any, message?: Kafka.IMessage) => Promise<any>;
  };

  public init() {
    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      [CLIENT_SYNC_TOPIC],
      (message: any) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions,
    );

    this.apiMap = {
      newClient: (data: any) => this.clientService.addClient(data),
      updateClient: (data: any) => this.clientService.updateClient(data),
      deleteClient: (data: any) => this.clientService.deleteClient(data),
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
