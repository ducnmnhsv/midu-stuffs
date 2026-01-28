import {Kafka, Logger, Utils} from "tradex-common";
import Scope from "../models/db/Scope";
import conf from "../conf";
import IScopeResponse from "../models/response/IScopeResponse";
import ScopeGroup from "../models/db/ScopeGroup";
import IScopeGroupResponse from "../models/response/IScopeGroupResponse";
import {Connection, doJobInTransaction} from "../db/async";
import {Query} from "../models/db/BaseModel";
import ScopeScopeGroupMap from "../models/db/ScopeScopeGroupMap";
import {updateClientService} from "./UpdateClientService";

class UpdateScopeService {

  public async update(retryTime: number = 0): Promise<any> {
    Logger.info("start loading scope");
    const scopes: Scope[] = [];
    const scopeMap: Map<number, Scope> = new Map();
    const scopeGroupMap: Map<number, Scope[]> = new Map();
    await Utils.asyncWithRetry(() => this.loadScopes(scopes, scopeMap, scopeGroupMap), retryTime);
    Logger.info("load scopes", scopes.length, scopes.map((scope: Scope) => scope.id.get()).join(","));
    const scopeGroups: ScopeGroup[] = [];
    await Utils.asyncWithRetry(() => this.loadScopeGroups(scopeGroups), retryTime);
    Logger.info("load scope groups", scopeGroups.length);
    const job: (con: Connection) => Promise<any> = (con: Connection) =>
      this.updateDb(con, scopes, scopeGroups, scopeGroupMap);
    await Utils.asyncWithRetry(() => doJobInTransaction(job), retryTime);
    return updateClientService.update(retryTime);
  }

  private updateDb = async (con: Connection, scopes: Scope[],
                            scopeGroups: ScopeGroup[],
                            scopeGroupMap: Map<number, Scope[]>): Promise<any> => {
    const deletAllMappingQuery: Query<ScopeScopeGroupMap> = new Query(new ScopeScopeGroupMap({}));
    await con.query(deletAllMappingQuery.delete(), []);
    const deletAllScopeQuery: Query<Scope> = new Query(new Scope({}));
    await con.query(deletAllScopeQuery.delete(), []);
    const deletAllScopeGroupQuery: Query<ScopeGroup> = new Query(new ScopeGroup({}));
    await con.query(deletAllScopeGroupQuery.delete(), []);
    await con.insertBulk(new Query(new Scope({})), scopes);
    await con.insertBulk(new Query(new ScopeGroup({})), scopeGroups);
    const map: ScopeScopeGroupMap[] = [];
    scopeGroupMap.forEach((value: Scope[], key: number) => {
      value.forEach((scope: Scope) => {
        map.push(new ScopeScopeGroupMap({
          "scope_id": scope.id.get(),
          "scope_group_id": key,
        }));
      });
    });
    return con.insertBulk(new Query(new ScopeScopeGroupMap({})), map);
  };

  private loadScopes = async (
    scopes: Scope[], scopeMap: Map<number, Scope>,
    scopeGroupMap: Map<number, Scope[]>,
    lastSequence?: number, pageSize?: number,
  ): Promise<any> => {
    const fetchCount = pageSize == null ? 100 : pageSize;
    if (lastSequence == null) {
      scopes.length = 0;
      scopeMap.clear();
      scopeGroupMap.clear();
    }
    const scopeResponseMessage: Kafka.IMessage =
      await Kafka.getInstance().sendRequestAsync("",
        conf.scopes.loadFrom.topic, conf.scopes.loadFrom.uris.scope, {
          fetchCount: fetchCount,
          lastSequence: lastSequence,
        }, conf.timeouts.loadScope);
    const data: IScopeResponse[] = Kafka.getResponse(scopeResponseMessage);
    Logger.info("loading scopes", data.length);
    data.forEach((value: IScopeResponse) => {
      const scope: Scope = new Scope({});
      scope.id.set(value.id);
      scope.name.set(value.name);
      scope.uriPattern.set(value.uriPattern);
      scope.forwardType.set(value.forwardType);
      scope.setForwardData(value.forwardData);
      scopes.push(scope);
      scopeMap[scope.id.get()] = scope;
      if (value.scopeGroupIds && value.scopeGroupIds.length > 0) {
        value.scopeGroupIds.forEach((scopeGroupId: number) => {
          if (scopeGroupMap.has(scopeGroupId)) {
            scopeGroupMap.get(scopeGroupId).push(scope);
          } else {
            scopeGroupMap.set(scopeGroupId, [scope]);
          }
        });
      }
    });
    if (data.length >= fetchCount) {
      return this.loadScopes(scopes, scopeMap, scopeGroupMap,
        data[data.length - 1].id, fetchCount);
    }
    return null;
  };

  private loadScopeGroups = async (scopeGroups: ScopeGroup[], lastSequence?: number, pageSize?: number): Promise<any> => {
    const fetchCount = pageSize == null ? 100 : pageSize;
    if (lastSequence == null) {
      scopeGroups.length = 0;
    }
    const scopeGroupResponseMessage: Kafka.IMessage =
      await Kafka.getInstance().sendRequestAsync("",
        conf.scopes.loadFrom.topic, conf.scopes.loadFrom.uris.scopeGroup, {
          fetchCount: fetchCount,
          lastSequence: lastSequence,
        }, conf.timeouts.loadScope);
    const data: IScopeGroupResponse[] = Kafka.getResponse(scopeGroupResponseMessage);
    Logger.info("loading scope groups", data.length);
    data.forEach((value: IScopeGroupResponse) => {
      const scopeGroup: ScopeGroup = new ScopeGroup({});
      scopeGroup.id.set(value.id);
      scopeGroup.name.set(value.scopeGroupName);
      scopeGroups.push(scopeGroup);
    });
    if (data.length >= fetchCount) {
      return this.loadScopeGroups(scopeGroups, data[data.length - 1].id, fetchCount);
    }
    return null;
  };
}

const updateScopeService: UpdateScopeService = new UpdateScopeService();

export {
  UpdateScopeService,
  updateScopeService,
};
