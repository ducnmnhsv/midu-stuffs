import { Service } from "typedi";
import { Kafka, Logger, Utils } from "tradex-common";
import { v4 as uuid } from "uuid";
import config from "../config";
import Holiday from "../models/db/Holiday";
import { EntityManager } from "typeorm";
import InterestInfo from "../models/db/InterestInfo";
import LangResourceFile from "../models/db/LangResourceFile";
import LangResource from "../models/db/LangResource";
import LangKey from "../models/db/LangKey";
import ILangResourceRequest from "../models/request/ILangResourceRequest";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { AppDataSource } from "../AppDataSource";

@Service()
export default class SyncDataService {
  public async syncHoliday(): Promise<any> {
    const domainConnectorRequest = {
      domain: Utils.TRADEX_DOMAIN,
      topic: config.clientId,
      data: {},
    };
    const message: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      uuid(),
      config.topic.domainConnector,
      config.uri.findAllHoliday,
      domainConnectorRequest,
    );
    Logger.info(`request result: ${JSON.stringify(message)}`);
    if (message.data.status != null) {
      const dataToSave: Holiday[] = [];
      for (const element of message.data) {
        const holiday = new Holiday();
        holiday.id = element.id;
        holiday.description = element.description;
        holiday.date = element.date;
        dataToSave.push(holiday);
      }
      await AppDataSource.transaction(
        async (txEntityManager: EntityManager) => {
          await txEntityManager.save(dataToSave);
        },
      );
    }
  }

  public async syncInterestInfo(): Promise<any> {
    const domainConnectorRequest = {
      domain: Utils.TRADEX_DOMAIN,
      topic: config.clientId,
      data: {},
    };
    const message: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      uuid(),
      config.topic.domainConnector,
      config.uri.findAllInterestInfo,
      domainConnectorRequest,
    );
    Logger.info(`request result: ${JSON.stringify(message)}`);
    if (message.data.status != null) {
      const dataToSave: InterestInfo[] = [];
      for (const element of message.data) {
        const newData = new InterestInfo();
        newData.id = element.id;
        newData.value = element.value;
        newData.startDate = element.startDate;
        newData.endDate = element.endDate;
        dataToSave.push(newData);
      }
      await AppDataSource.transaction(
        async (txEntityManager: EntityManager) => {
          await txEntityManager.save(dataToSave);
        },
      );
    }
  }

  public async syncResourcesForInternal(
    request: ILangResourceRequest,
  ): Promise<any> {
    const domainConnectorRequest = {
      domain: Utils.TRADEX_DOMAIN,
      topic: config.clientId,
      data: request,
    };
    const message: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      uuid(),
      config.topic.domainConnector,
      config.uri.getAllResourcesForInternal,
      domainConnectorRequest,
    );
    Logger.info(`request result: ${JSON.stringify(message)}`);
    if (message.data.status != null) {
      const dataToSave: LangResourceFile[] = [];
      for (const element of message.data) {
        const newData = new LangResourceFile();
        newData.id = element.id;
        newData.lang = element.lang;
        newData.langNamespace = element.langNamespace;
        newData.namespaceId = element.namespaceId;
        newData.url = element.url;
        dataToSave.push(newData);
      }
      await AppDataSource.transaction(
        async (txEntityManager: EntityManager) => {
          await txEntityManager.save(dataToSave);
        },
      );
    }
  }

  public async syncLangResource(request: ILangResourceRequest): Promise<any> {
    const domainConnectorRequest = {
      domain: Utils.TRADEX_DOMAIN,
      topic: config.clientId,
      data: request,
    };
    const message: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      uuid(),
      config.topic.domainConnector,
      config.uri.getAllResources,
      domainConnectorRequest,
    );
    Logger.info(`request result: ${JSON.stringify(message)}`);
    if (message.data.status != null) {
      const dataToSave: LangResourceFile[] = [];
      for (const element of message.data) {
        const newData = new LangResourceFile();
        newData.id = element.id;
        newData.url = element.url;
        newData.namespaceId = element.namespaceId;
        newData.lang = element.lang;
        newData.langNamespace = element.langNamespace;
        dataToSave.push(newData);
      }
      await AppDataSource.transaction(
        async (txEntityManager: EntityManager) => {
          await txEntityManager.save(dataToSave);
        },
      );
    }
  }

  public async syncAdminResource(
    request: TradexModelsConfiguration.QueryLocaleRequest,
  ): Promise<any> {
    const domainConnectorRequest = {
      domain: Utils.TRADEX_DOMAIN,
      topic: config.clientId,
      data: request,
    };
    const message: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      uuid(),
      config.topic.domainConnector,
      config.uri.getAllAdminResources,
      domainConnectorRequest,
    );
    Logger.info(`request result: ${JSON.stringify(message)}`);
    if (message.data.status != null) {
      const dataToSave: LangResource[] = [];
      for (const element of message.data) {
        const newData = new LangResource();
        newData.id = element.id;
        newData.langNamespaces = element.langNamespaces;
        newData.langResourceVersions = element.langResourceVersions;
        newData.msName = element.msName;
        dataToSave.push(newData);
      }
      await AppDataSource.transaction(
        async (txEntityManager: EntityManager) => {
          await txEntityManager.save(dataToSave);
        },
      );
    }
  }

  public async syncKeysByNamespace(
    request: TradexModelsConfiguration.QueryLocaleKeyByNameSpaceRequest,
  ): Promise<any> {
    const domainConnectorRequest = {
      domain: Utils.TRADEX_DOMAIN,
      topic: config.clientId,
      data: request,
    };
    const message: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      uuid(),
      config.topic.domainConnector,
      config.uri.getAllKeysByNamespace,
      domainConnectorRequest,
    );
    Logger.info(`request result: ${JSON.stringify(message)}`);
    if (message.data.status != null) {
      const dataToSave: LangKey[] = [];
      for (const element of message.data) {
        const newData = new LangKey();
        newData.id = element.id;
        newData.key = element.key;
        newData.langNamespace = element.langNamespace;
        newData.langTranslates = element.langTranslates;
        newData.namespaceId = element.namespaceId;
        dataToSave.push(newData);
      }
      await AppDataSource.transaction(
        async (txEntityManager: EntityManager) => {
          await txEntityManager.save(dataToSave);
        },
      );
    }
  }
}
