import Scope from "../models/db/Scope";
import config from "../config";
import ScopeGroup from "../models/db/ScopeGroup";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { AppDataSource } from "../AppDataSource";

export const ScopeRepository = AppDataSource.getRepository(Scope).extend({
  findById(id: number): Promise<Scope> {
    return this.findOne({
      id: id,
    });
  },

  findScope(
    name: string,
    scopeGroupId: number,
    uriPattern: string,
    forwardType: string,
    lastSequence: number,
    fetchCount: number,
  ): Promise<Scope[]> {
    let builder = this.createQueryBuilder("t1")
      .leftJoinAndSelect("t1.scopeGroups", "t2")
      .take(fetchCount == null ? config.defaultFetchCount : fetchCount);

    if (name != null) {
      builder = builder.where(`t1.name LIKE "%${name}%"`);
    }

    if (uriPattern != null) {
      builder = builder.andWhere(`t1.uri_pattern = "${uriPattern}"`);
    }

    if (forwardType != null) {
      builder = builder.andWhere(`t1.forward_type = "${forwardType}"`);
    }

    if (scopeGroupId != null) {
      builder = builder.andWhere(`t2.id = ${scopeGroupId}`);
    }

    if (lastSequence != null) {
      builder = builder.andWhere(`t1.id > ${lastSequence}`);
    }

    return builder
      .orderBy({
        "t1.id": "ASC",
      })
      .getMany();
  },

  queryScopeForUpdateRequest(
    request: TradexModelsConfiguration.QuerySystemScopeRequest,
  ): Promise<Scope[]> {
    return this.createQueryBuilder("scope")
      .leftJoinAndSelect("scope.scopeGroups", "scopeGroup")
      .where(
        request.lastQueriedTime != null
          ? `scopeGroup.updated_at > ${request.lastQueriedTime}`
          : "scopeGroup.updated_at IS NOT NULL",
      )
      .orderBy({
        "scope.id": "ASC",
      })
      .getMany();
  },

  async queryScopeMap(): Promise<Scope[]> {
    const data = await this.createQueryBuilder("scope")
      .leftJoinAndSelect("scope.scopeGroups", "scopeGroup")
      .getMany();
    data.map((obj: Scope) => {
      obj.scopeGroups = Object.assign(
        obj.scopeGroups.map((value: ScopeGroup) => value.name),
      );
    });
    return data;
  },

  async queryAllScopeByClient(clientId: number): Promise<Scope[]> {
    return this.createQueryBuilder("t_scope")
      .innerJoin(
        "t_scope_scope_group_map",
        "t_scope_scope_group_map",
        "t_scope.id = t_scope_scope_group_map.scope_id",
      )
      .innerJoin(
        "t_login_method_scope_group_map",
        "t_login_method_scope_group_map",
        "t_scope_scope_group_map.scope_group_id = t_login_method_scope_group_map.scope_group_id",
      )
      .innerJoin(
        "t_client_login_method_map",
        "t_client_login_method_map",
        "t_login_method_scope_group_map.login_method_id = t_client_login_method_map.login_method_id",
      )
      .innerJoin(
        "t_client",
        "t_client",
        "t_client.id = t_client_login_method_map.client_id",
      )
      .where(`t_client.id = :clientId`, { clientId: clientId })
      .getMany();
  },

  async queryAllScopeByScoupGroup(scopeGroup: number): Promise<Scope[]> {
    return this.createQueryBuilder("t_scope")
      .innerJoin(
        "t_scope_scope_group_map",
        "t_scope_scope_group_map",
        "t_scope.id = t_scope_scope_group_map.scope_id",
      )
      .innerJoin(
        "t_scope_group",
        "t_scope_group",
        "t_scope_scope_group_map.scope_group_id = t_scope_group.id",
      )
      .where(`t_scope_group.id = ${scopeGroup}`)
      .getMany();
  },
});
