import { Service } from "typedi";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { ScopeGroupRepository } from "../../repositories/ScopeGroupRepository";
import ScopeGroup, { parseScopeGroup } from "../../models/db/ScopeGroup";
import { v4 as uuid } from "uuid";
import {
  SCOPE_GROUP_SYNC_TOPIC,
  SCOPE_GROUP_UPDATE_TOPIC,
} from "../../constants/updateTopics";
import { Errors, Kafka, Utils } from "tradex-common";
import Scope from "../../models/db/Scope";
import { ScopeRepository } from "../../repositories/ScopeRepository";
import config from "../../config";

@Service()
export default class ScopeGroupService {
  public async getAllScopeGroups(
    request: TradexModelsConfiguration.QueryAdminScopeGroupRequest,
  ): Promise<TradexModelsConfiguration.QueryAdminScopeGroupResponse> {
    const scopeGroups: ScopeGroup[] = await ScopeGroupRepository.findScopeGroup(
      request.scopeGroupName,
      request.lastSequence,
      request.fetchCount,
    );
    return scopeGroups.map(parseScopeGroup);
  }

  public async queryScopeGroupForUpdate(
    request: TradexModelsConfiguration.QuerySystemScopeGroupRequest,
  ): Promise<TradexModelsConfiguration.QuerySystemScopeGroupResponse> {
    const scopeGroupList: ScopeGroup[] =
      await ScopeGroupRepository.queryScopeGroupForUpdateRequest({
        lastQueriedTime:
          request.lastQueriedTime != null
            ? Utils.convertStringToDate(
                request.lastQueriedTime,
                Utils.DATETIME_DISPLAY_FORMAT,
              )
            : null,
      });
    return scopeGroupList.map(parseScopeGroup);
  }

  public async addNewScopeGroup(
    request: TradexModelsConfiguration.PostAdminScopeGroupRequest,
  ): Promise<TradexModelsConfiguration.PostAdminScopeGroupResponse> {
    if (request.scopeGroupName == null) {
      throw new Errors.FieldRequiredError("scopeGroupName");
    }

    const scopeGroup = new ScopeGroup();
    scopeGroup.name = request.scopeGroupName;

    if (request.scopeIds != null && request.scopeIds.length > 0) {
      const scopes: Scope[] = await ScopeRepository.findByIds(request.scopeIds);
      if (scopes.length < request.scopeIds.length) {
        throw new Errors.InvalidFieldValueError("scopeIds", "SCOPE_NOT_EXIST");
      }
      scopeGroup.scopes = scopes;
    }

    await ScopeGroupRepository.save(scopeGroup);
    Kafka.getInstance().sendMessage(
      uuid(),
      SCOPE_GROUP_UPDATE_TOPIC,
      "newScopeGroup",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        SCOPE_GROUP_SYNC_TOPIC,
        "newScopeGroup",
        request,
      );
    }

    return parseScopeGroup(scopeGroup);
  }

  public async updateScopeGroup(
    request: TradexModelsConfiguration.PutAdminScopeGroupRequest,
  ): Promise<TradexModelsConfiguration.PutAdminScopeGroupResponse> {
    if (request.scopeGroupId == null) {
      throw new Errors.FieldRequiredError("scopeGroupId");
    }

    const scopeGroup: ScopeGroup =
      await ScopeGroupRepository.queryScopeGroupById({
        scopeGroupId: request.scopeGroupId,
      });
    if (scopeGroup == null) {
      throw new Errors.ObjectNotFoundError("scopeGroup");
    }

    scopeGroup.name =
      request.scopeGroupName != null ? request.scopeGroupName : scopeGroup.name;

    if (request.scopeIds != null && request.scopeIds.length > 0) {
      const scopes: Scope[] = await ScopeRepository.findByIds(request.scopeIds);
      if (scopes.length < request.scopeIds.length) {
        throw new Errors.InvalidFieldValueError("scopeIds", "SCOPE_NOT_EXIST");
      }
      scopeGroup.scopes = scopes;
    }

    await ScopeGroupRepository.save(scopeGroup);

    Kafka.getInstance().sendMessage(
      uuid(),
      SCOPE_GROUP_UPDATE_TOPIC,
      "updateScopeGroup",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        SCOPE_GROUP_SYNC_TOPIC,
        "updateScopeGroup",
        request,
      );
    }

    return parseScopeGroup(scopeGroup);
  }

  public async deleteScopeGroup(
    request: TradexModelsConfiguration.DeleteAdminScopeGroupRequest,
  ): Promise<TradexModelsConfiguration.DeleteAdminScopeGroupResponse> {
    if (request.scopeGroupId == null) {
      throw new Errors.FieldRequiredError("scopeGroupId");
    }

    const scopeGroup: ScopeGroup =
      await ScopeGroupRepository.queryScopeGroupById({
        scopeGroupId: request.scopeGroupId,
      });
    if (scopeGroup == null) {
      throw new Errors.ObjectNotFoundError("scopeGroup");
    }

    await ScopeGroupRepository.remove(scopeGroup);
    Kafka.getInstance().sendMessage(
      uuid(),
      SCOPE_GROUP_UPDATE_TOPIC,
      "deleteScopeGroup",
      request,
    );

    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        SCOPE_GROUP_SYNC_TOPIC,
        "deleteScopeGroup",
        request,
      );
    }
    return {};
  }
}
