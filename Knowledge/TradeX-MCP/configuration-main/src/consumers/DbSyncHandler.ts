import { Service as SV } from "typedi";
import config from "../config";
import { Kafka, Logger, Errors } from "tradex-common";
import { getConnection, Repository } from "typeorm";
import AdminRole from "../models/db/AdminRole";
import Client from "../models/db/Client";
import DataView from "../models/db/DataView";
import Faq from "../models/db/Faq";
import FaqGroup from "../models/db/FaqGroup";
import FaqReview from "../models/db/FaqReview";
import Holiday from "../models/db/Holiday";
import InterestInfo from "../models/db/InterestInfo";
import LangKey from "../models/db/LangKey";
import LangNamespace from "../models/db/LangNamespace";
import LangResource from "../models/db/LangResource";
import LangResourceFile from "../models/db/LangResourceFile";
import LangResourceVersion from "../models/db/LangResourceVersion";
import LangTranslate from "../models/db/LangTranslate";
import LoginMethod from "../models/db/LoginMethod";
import Menu from "../models/db/Menu";
import MenuGroup from "../models/db/MenuGroup";
import MenuRole from "../models/db/MenuRole";
import OpenApi from "../models/db/OpenApi";
import Scope from "../models/db/Scope";
import ScopeGroup from "../models/db/ScopeGroup";
import Service from "../models/db/Service";
import TemplateResource from "../models/db/TemplateResource";

@SV()
export default class DbSyncHandler {
  private apiMap: {
    [k: string]: (data: any, message?: Kafka.IMessage) => Promise<any>;
  }; // tslint:disable-line

  public init() {
    Logger.info(`DbSyncHandler Init`);
    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      [config.topic.configurationSync],
      (message: any) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions,
    );
    this.apiMap = {
      [config.uri.configurationSync]: (data: any) => this.syncDataToDb(data),
    };
  }

  private async syncDataToDb(data: any) {
    Logger.info(`syncDataToDb: `, data);
    const aa =
      data.name === "AdminRole"
        ? AdminRole
        : data.name === "Client"
          ? Client
          : data.name === "DataView"
            ? DataView
            : data.name === "Faq"
              ? Faq
              : data.name === "FaqGroup"
                ? FaqGroup
                : data.name === "FaqReview"
                  ? FaqReview
                  : data.name === "Holiday"
                    ? Holiday
                    : data.name === "InterestInfo"
                      ? InterestInfo
                      : data.name === "LangKey"
                        ? LangKey
                        : data.name === "LangNamespace"
                          ? LangNamespace
                          : data.name === "LangResource"
                            ? LangResource
                            : data.name === "LangResourceFile"
                              ? LangResourceFile
                              : data.name === "LangResourceVersion"
                                ? LangResourceVersion
                                : data.name === "LangTranslate"
                                  ? LangTranslate
                                  : data.name === "LoginMethod"
                                    ? LoginMethod
                                    : data.name === "Menu"
                                      ? Menu
                                      : data.name === "MenuGroup"
                                        ? MenuGroup
                                        : data.name === "MenuRole"
                                          ? MenuRole
                                          : data.name === "OpenApi"
                                            ? OpenApi
                                            : data.name === "Scope"
                                              ? Scope
                                              : data.name === "ScopeGroup"
                                                ? ScopeGroup
                                                : data.name === "Service"
                                                  ? Service
                                                  : data.name ===
                                                      "TemplateResource"
                                                    ? TemplateResource
                                                    : null;
    if (aa != null) {
      const repository: Repository<any> = getConnection().getRepository(aa);
      for (const ob of data.data) {
        const value = await repository.findOneBy({ id: ob.id });
        if (!value) {
          //create
          await repository
            .createQueryBuilder()
            .insert()
            .into(aa)
            .values(ob)
            .execute();
        } else {
          //update
          if (
            Object.entries(value).toString() !== Object.entries(ob).toString()
          ) {
            await repository.save(data.data);
          }
        }
      }
    }
  }

  private handleRequest: Kafka.Handle = (message: Kafka.IMessage) => {
    Logger.info(`DbSyncHandler handleRequest:`, message);
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
