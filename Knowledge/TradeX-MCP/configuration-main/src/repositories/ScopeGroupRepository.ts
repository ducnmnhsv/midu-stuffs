import ScopeGroup from "../models/db/ScopeGroup";
import config from "../config";
import { ISystemQueryUpdateTime } from "../models/request/admin/ISystemQueryRequest";
import { IFindByIdScopeGroupRequest } from "../models/request/admin/IScopeGroupsRequest";
import Scope from "../models/db/Scope";
import LoginMethod from "../models/db/LoginMethod";
import { AppDataSource } from "../AppDataSource";

export const ScopeGroupRepository = AppDataSource.getRepository(
  ScopeGroup,
).extend({
  findScopeGroup(
    name: string,
    lastSequence: number,
    fetchCount: number,
  ): Promise<ScopeGroup[]> {
    let builder = this.createQueryBuilder("scopeGroup")
      .leftJoinAndSelect("scopeGroup.scopes", "scope")
      .take(fetchCount == null ? config.defaultFetchCount : fetchCount);

    if (name != null) {
      builder = builder.where(`scopeGroup.name LIKE "%${name}%"`);
    }

    if (lastSequence != null) {
      builder = builder.andWhere(`scopeGroup.id > ${lastSequence}`);
    }

    return builder
      .orderBy({
        "scopeGroup.id": "ASC",
      })
      .getMany();
  },

  queryScopeGroupForUpdateRequest(
    request: ISystemQueryUpdateTime,
  ): Promise<ScopeGroup[]> {
    let queryBuilder = this.createQueryBuilder("scopeGroup").leftJoinAndSelect(
      "scopeGroup.scopes",
      "scope",
    );
    if (request.lastQueriedTime != null) {
      queryBuilder = queryBuilder.where(
        "scopeGroup.updated_at > :lastQueriedTime or scope.updated_at > :lastQueriedTime",
        { lastQueriedTime: request.lastQueriedTime },
      );
    }
    return queryBuilder
      .orderBy({
        "scopeGroup.id": "ASC",
      })
      .getMany();
  },

  queryScopeGroupById(
    request: IFindByIdScopeGroupRequest,
  ): Promise<ScopeGroup> {
    return this.findOne({
      id: request.scopeGroupId,
    });
  },

  async queryScopeGrounpMap(): Promise<any> {
    const data = await this.createQueryBuilder("scopeGroup")
      .leftJoinAndSelect("scopeGroup.loginMethods", "loginMethod")
      .leftJoinAndSelect("scopeGroup.scopes", "scope")
      .getMany();
    data.map((obj: ScopeGroup) => {
      obj.loginMethods = Object.assign(
        obj.loginMethods.map((value: LoginMethod) => value.id),
      );
    });
    data.map((obj: ScopeGroup) => {
      obj.scopes = Object.assign(obj.scopes.map((value: Scope) => value.name));
    });
    return data;
  },
});
