import { Service } from "typedi";
import { EntityManager } from "typeorm";
import { Errors, Kafka, Models, Utils } from "tradex-common";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { ScopeRepository } from "../../repositories/ScopeRepository";
import { ScopeGroupRepository } from "../../repositories/ScopeGroupRepository";
import Scope, { parseToScope } from "../../models/db/Scope";
import ScopeGroup from "../../models/db/ScopeGroup";
import { v4 as uuid } from "uuid";
import {
  SCOPE_SYNC_TOPIC,
  SCOPE_UPDATE_TOPIC,
} from "../../constants/updateTopics";
import config from "../../config";
import { AppDataSource } from "../../AppDataSource";

@Service()
export default class ScopeService {
  public async getAllScopes(
    request: TradexModelsConfiguration.QueryAdminScopeRequest,
  ): Promise<TradexModelsConfiguration.QueryAdminScopeRequest> {
    const scopes: Scope[] = await ScopeRepository.findScope(
      request.name,
      request.scopeGroupId,
      request.uriPattern,
      request.forwardType,
      request.lastSequence,
      request.fetchCount,
    );
    return scopes.map(parseToScope);
  }

  public async queryScopeForUpdate(
    request: TradexModelsConfiguration.QuerySystemScopeRequest,
  ): Promise<TradexModelsConfiguration.QuerySystemScopeResponse> {
    const scopeList: Scope[] =
      await ScopeRepository.queryScopeForUpdateRequest(request);
    return scopeList.map(parseToScope);
  }

  public async saveNewScope(
    request: TradexModelsConfiguration.PostAdminScopeRequest,
  ): Promise<TradexModelsConfiguration.PostAdminScopeResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.name, "name").setRequire().throwValid(invalidParams);
    Utils.validate(request.uriPattern, "uriPattern")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();

    const response: TradexModelsConfiguration.PostAdminScopeResponse =
      await AppDataSource.transaction(async (txEntityManager: EntityManager) => {
        const scope = new Scope();
        scope.name = request.name;
        scope.uriPattern = request.uriPattern;
        scope.forwardType = Models.AAA.ForwardType[request.forwardType];
        scope.forwardData = request.forwardData;

        if (request.scopeGroupIds != null && request.scopeGroupIds.length > 0) {
          const scopeGroups: ScopeGroup[] =
            await ScopeGroupRepository.findByIds(request.scopeGroupIds);
          if (scopeGroups.length < request.scopeGroupIds.length) {
            throw new Errors.InvalidFieldValueError(
              "scopeGroupIds",
              "SCOPE_GROUP_NOT_EXIST",
            );
          }
          scope.scopeGroups = scopeGroups;
        }
        await txEntityManager.save(scope);
        return parseToScope(scope);
      });
    Kafka.getInstance().sendMessage(
      uuid(),
      SCOPE_UPDATE_TOPIC,
      "newScope",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        SCOPE_SYNC_TOPIC,
        "newScope",
        request,
      );
    }

    return response;
  }

  public async updateScope(
    request: TradexModelsConfiguration.PutAdminScopeRequest,
  ): Promise<TradexModelsConfiguration.PutAdminScopeResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.scopeId, "scopeId")
      .setRequire()
      .throwValid(invalidParams);

    const scope: Scope = await ScopeRepository.findById(request.scopeId);
    if (scope == null) {
      throw new Errors.ObjectNotFoundError();
    }

    await AppDataSource.transaction(
      async (transactionalEntityManager: EntityManager) => {
        if (request.name != null) {
          scope.name = request.name;
        }

        if (request.uriPattern != null) {
          scope.uriPattern = request.uriPattern;
        }

        if (request.forwardType !== undefined) {
          if (request.forwardType === null) {
            scope.forwardType = null;
          } else {
            scope.forwardType = Models.AAA.ForwardType[request.forwardType];
          }
        }

        if (request.forwardData !== undefined) {
          scope.forwardData = request.forwardData;
        }

        if (request.scopeGroupIds !== undefined) {
          if (
            request.scopeGroupIds != null &&
            request.scopeGroupIds.length > 0
          ) {
            const scopeGroups: ScopeGroup[] =
              await ScopeGroupRepository.findByIds(request.scopeGroupIds);
            if (scopeGroups.length < request.scopeGroupIds.length) {
              throw new Errors.InvalidFieldValueError(
                "scopeGroupIds",
                "SCOPE_GROUP_NOT_EXIST",
              );
            }
            scope.scopeGroups = scopeGroups;
          }
        }
        await transactionalEntityManager.save(Scope, scope);
      },
    );
    Kafka.getInstance().sendMessage(
      uuid(),
      SCOPE_UPDATE_TOPIC,
      "updateScope",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        SCOPE_SYNC_TOPIC,
        "updateScope",
        request,
      );
    }

    return parseToScope(scope);
  }

  public async deleteScope(
    request: TradexModelsConfiguration.DeleteAdminScopeRequest,
  ): Promise<TradexModelsConfiguration.DeleteAdminScopeResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.scopeId, "scopeId")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
    await AppDataSource.transaction(
      async (transactionalEntityManager: EntityManager) => {
        await transactionalEntityManager.delete(Scope, {
          id: request.scopeId,
        });
      },
    );
    Kafka.getInstance().sendMessage(
      uuid(),
      SCOPE_UPDATE_TOPIC,
      "deleteScope",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        SCOPE_SYNC_TOPIC,
        "deleteScope",
        request,
      );
    }

    return {};
  }
}
