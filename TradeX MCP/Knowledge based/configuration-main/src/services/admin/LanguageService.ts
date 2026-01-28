import { Inject, Service } from "typedi";
import { EntityManager } from "typeorm";
import * as FormData from "form-data";
import { Errors, Kafka, Utils } from "tradex-common";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { LangResourceRepository } from "../../repositories/LangResourceRepository";
import { LangKeyRepository } from "../../repositories/LangKeyRepository";
import { LangNamespaceRepository } from "../../repositories/LangNamespaceRepository";
import AdminAmazonWebService from "./AdminAmazonWebService";
import LangResource, {
  parseToLocaleNamespaces,
  parseToLocaleResponse,
} from "../../models/db/LangResource";
import LangKey from "../../models/db/LangKey";
import LangTranslate from "../../models/db/LangTranslate";
import LangNamespace from "../../models/db/LangNamespace";
import LangResourceFile from "../../models/db/LangResourceFile";
import LangResourceVersion from "../../models/db/LangResourceVersion";
import {
  KEY_ALREADY_EXISTED,
  UPLOAD_LANGUAGE_RESOURCE_FAILED,
} from "../../constants/errors";
import config from "../../config";
import { v4 as uuid } from "uuid";
import { LANG_KEY_SYNC_TOPIC } from "../../constants/updateTopics";
import { AppDataSource } from "../../AppDataSource";

@Service()
export default class LanguageService {
  @Inject()
  private readonly awsService: AdminAmazonWebService;

  public async getAllResources(
    request: TradexModelsConfiguration.QueryLocaleRequest,
  ): Promise<TradexModelsConfiguration.QueryLocaleResponse> {
    const langResources: LangResource[] =
      await LangResourceRepository.getAllResources();
    if (langResources == null) {
      throw new Errors.ObjectNotFoundError();
    }
    return parseToLocaleNamespaces(langResources);
  }

  public async getAllKeysByNamespace(
    request: TradexModelsConfiguration.QueryLocaleKeyByNameSpaceRequest,
  ): Promise<TradexModelsConfiguration.QueryLocaleKeyByNameSpaceResponse> {
    const langKeys: LangKey[] = await LangKeyRepository.findAllKeysByNamespace(
      request.namespaceId,
      request.lastKey,
      request.fetchCount,
      request.keyword,
    );
    return langKeys.map(parseToLocaleResponse);
  }

  public async updateLangByKey(
    request: TradexModelsConfiguration.PutLocaleKeyTranslateRequest,
  ): Promise<TradexModelsConfiguration.PutLocaleKeyTranslateResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.keyId, "keyId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.lang, "lang").setRequire().throwValid(invalidParams);
    invalidParams.throwErr();

    const langKey: LangKey = await LangKeyRepository.findById(request.keyId);
    if (langKey == null) {
      throw new Errors.ObjectNotFoundError();
    }
    const langTranslate =
      langKey.langTranslates == null
        ? null
        : langKey.langTranslates.find(
            (langTranslate: LangTranslate) =>
              langTranslate.lang === request.lang,
          );

    return AppDataSource.transaction(
      async (transactionalEntityManager: EntityManager) => {
        if (langTranslate == null) {
          if (request.value != null && request.value.length > 0) {
            const langTranslate = new LangTranslate();
            langTranslate.keyId = request.keyId;
            langTranslate.lang = request.lang;
            langTranslate.value = request.value;

            await transactionalEntityManager.save(langTranslate);
          }
        } else {
          if (request.value != null && request.value.length > 0) {
            await transactionalEntityManager.update(
              LangTranslate,
              { keyId: request.keyId, lang: request.lang },
              { value: request.value },
            );
          } else {
            await transactionalEntityManager.delete(LangTranslate, {
              keyId: request.keyId,
              lang: request.lang,
            });
          }
        }
        if (config.domain === Utils.TRADEX_DOMAIN) {
          Kafka.getInstance().sendMessage(
            uuid(),
            LANG_KEY_SYNC_TOPIC,
            "update",
            request,
          );
        }
        return {};
      },
    );
  }

  public async addNewKey(
    request: TradexModelsConfiguration.PostLocaleKeyRequest,
  ): Promise<TradexModelsConfiguration.PostLocaleKeyRespone> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.namespaceId, "namespaceId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.key, "key").setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const langKey: LangKey = await LangKeyRepository.findByKeyAndNamespace(
      request.key,
      request.namespaceId,
    );
    if (langKey != null) {
      throw new Errors.GeneralError(KEY_ALREADY_EXISTED, invalidParams.params);
    }

    return AppDataSource.transaction(
      async (transactionalEntityManager: EntityManager) => {
        const langKey = new LangKey();
        langKey.namespaceId = request.namespaceId;
        langKey.key = request.key;
        await transactionalEntityManager.save(langKey);

        if (config.domain === Utils.TRADEX_DOMAIN) {
          Kafka.getInstance().sendMessage(
            uuid(),
            LANG_KEY_SYNC_TOPIC,
            "add",
            request,
          );
        }

        return parseToLocaleResponse(langKey);
      },
    );
  }

  public async deleteKey(
    request: TradexModelsConfiguration.DeleteLocaleKeyRequest,
  ): Promise<TradexModelsConfiguration.DeleteLocaleKeyResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.namespaceId, "namespaceId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.keyId, "keyId")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();

    return AppDataSource.transaction(
      async (transactionalEntityManager: EntityManager) => {
        await transactionalEntityManager.delete(LangKey, {
          id: request.keyId,
          namespaceId: request.namespaceId,
        });

        if (config.domain === Utils.TRADEX_DOMAIN) {
          Kafka.getInstance().sendMessage(
            uuid(),
            LANG_KEY_SYNC_TOPIC,
            "delete",
            request,
          );
        }
        return {};
      },
    );
  }

  public async uploadToAWS(
    request: TradexModelsConfiguration.PostUploadLocaleRequest,
  ): Promise<TradexModelsConfiguration.PostUploadLocaleResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.namespaceId, "namespaceId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.lang, "lang").setRequire().throwValid(invalidParams);
    invalidParams.throwErr();

    const langNamespace: LangNamespace = await LangNamespaceRepository.findById(
      request.namespaceId,
    );
    if (langNamespace == null) {
      throw new Errors.ObjectNotFoundError();
    }

    const langKeys: LangKey[] = await LangKeyRepository.getAllKeys(
      request.namespaceId,
      request.lang,
    );
    if (langKeys != null && langKeys.length > 0) {
      const langData = {};
      langKeys.forEach(
        (langKey: LangKey) =>
          (langData[langKey.key] = langKey.langTranslates[0].value),
      );

      const signedData: any =
        await this.awsService.getSignedDataToUploadInternal({
          key: `lang_resource/${langNamespace.langResource.msName}/${langNamespace.namespace}/${request.lang}.json`,
          serviceName: "langResource",
        });
      await this.uploadFormData(request, signedData, langData, langNamespace);
    }
    return {};
  }

  public async uploadFormData(
    request: TradexModelsConfiguration.PostUploadLocaleRequest,
    signedData: any,
    langData: object,
    langNamespace: LangNamespace,
  ): Promise<any> {
    const form = new FormData();
    Object.keys(signedData.fields).forEach((key: string) =>
      form.append(key, signedData.fields[key]),
    );

    form.append("Content-Type", "application/json");
    form.append("file", JSON.stringify(langData));

    return new Promise((resolve: any, reject: any) => {
      form.submit(signedData.url, async (err: Error, res: any) => {
        if (
          err != null ||
          (res.statusCode !== 200 &&
            res.statusCode !== 201 &&
            res.statusCode !== 204)
        ) {
          reject(
            new Errors.GeneralError(UPLOAD_LANGUAGE_RESOURCE_FAILED, null),
          );
          return;
        }
        resolve(
          await AppDataSource.transaction(
            async (transactionalEntityManager: EntityManager) => {
              let langResourceFile =
                langNamespace.langResourceFiles == null
                  ? null
                  : langNamespace.langResourceFiles.find(
                      (resourceFile: LangResourceFile) =>
                        resourceFile.lang === request.lang,
                    );
              if (langResourceFile == null) {
                langResourceFile = new LangResourceFile();
                langResourceFile.namespaceId = request.namespaceId;
                langResourceFile.lang = request.lang;
                langResourceFile.url = `${signedData.url}/${signedData.fields.key}`;

                await transactionalEntityManager.save(langResourceFile);
              } else {
                await transactionalEntityManager.update(
                  LangResourceFile,
                  { id: langResourceFile.id },
                  { url: `${signedData.url}/${signedData.fields.key}` },
                );
              }

              if (
                request.version != null &&
                request.version.trim().length > 0
              ) {
                let langResourceVersion =
                  langNamespace.langResource.langResourceVersions == null
                    ? null
                    : langNamespace.langResource.langResourceVersions.find(
                        (resourceVersion: LangResourceVersion) =>
                          resourceVersion.lang === request.lang,
                      );

                if (langResourceVersion == null) {
                  langResourceVersion = new LangResourceVersion();
                  langResourceVersion.resourceId =
                    langNamespace.langResource.id;
                  langResourceVersion.lang = request.lang;
                  langResourceVersion.version = request.version;
                  await transactionalEntityManager.save(langResourceVersion);
                } else {
                  await transactionalEntityManager.update(
                    LangResourceVersion,
                    { id: langResourceVersion.id },
                    { version: request.version },
                  );
                }
              }
              return {};
            },
          ),
        );
      });
    });
  }
}
