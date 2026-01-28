import {Kafka, Logger} from "tradex-common";
import fetch from "node-fetch";
import {Connection, doJobInTransaction} from "../db/async";
import Client from "../models/db/Client";
import LoginMethod from "../models/db/LoginMethod";
import ClientLoginMethodMap from "../models/db/ClientLoginMethodMap";
import LoginMethodScopeGroupMap from "../models/db/LoginMethodScopeGroupMap";
import {Query} from "../models/db/BaseModel";
import Scope from "../models/db/Scope";
import ScopeGroup from "../models/db/ScopeGroup";
import ScopeScopeGroupMap from "../models/db/ScopeScopeGroupMap";
import {scopeService} from "./ScopeService";
import conf from "../conf";
import IImportRequest from "../models/request/IImportRequest";

class ImportDbJsonService {

  public async jsonDbImport(request: IImportRequest, msg: Kafka.IMessage) {
    const data = await fetch(request.url);
    await this.loadDatas(JSON.parse(`${await data.text()}`));
    try {
      await scopeService.init(conf.retryTimes, () => {
        return;
      });
    } catch (e) {
      Logger.error("fail to init scope", e);
    }
  }

  private updateDb = async (
    con: Connection,
    clientIds: number[],
    loginMethodIds: number[],
    clients: Client[],
    loginMethods: LoginMethod[],
    scopes: Scope[],
    scopeGroups: ScopeGroup[],
    clientLoginMethodMaps: ClientLoginMethodMap[],
    loginMethodScopeGroupMaps: LoginMethodScopeGroupMap[],
    scopeIds: number[],
    scopeGroupIds: number[],
    scopeScopeGroupMaps: ScopeScopeGroupMap[],
  ): Promise<any> => {
    Logger.info("remove all mapping loginMethodScopeGroup");
    const deleteLoginMethodScopeGroup: Query<LoginMethodScopeGroupMap> = new Query(new LoginMethodScopeGroupMap({}));
    deleteLoginMethodScopeGroup.where((model: LoginMethodScopeGroupMap) => model.loginMethodId, "in (?)");
    await con.query(deleteLoginMethodScopeGroup.delete(), [loginMethodIds]);
    Logger.info("remove all mapping clientLoginMethod");
    const deleteClientLoginMethod: Query<ClientLoginMethodMap> = new Query(new ClientLoginMethodMap({}));
    deleteClientLoginMethod.where((model: ClientLoginMethodMap) => model.clientId, "in (?)");
    await con.query(deleteClientLoginMethod.delete(), [clientIds]);
    Logger.info("remove all login methods");
    const deleteLoginMethod: Query<LoginMethod> = new Query(new LoginMethod({}));
    deleteLoginMethod.where((model: LoginMethod) => model.id, "in (?)");
    await con.query(deleteLoginMethod.delete(), [loginMethodIds]);
    Logger.info("remove all clients");
    const deleteClient: Query<Client> = new Query(new Client({}));
    await con.query(deleteClient.delete(), [clientIds]);
    deleteClient.where((model: Client) => model.id, "in (?)");
    Logger.info("remove all scope");
    const deleteScope: Query<Scope> = new Query(new Scope({}));
    await con.query(deleteScope.delete(), [scopeIds]);
    deleteScope.where((model: Scope) => model.id, "in (?)");
    Logger.info("remove all scopeGroup");
    const deleteScopeGroup: Query<ScopeGroup> = new Query(new ScopeGroup({}));
    await con.query(deleteScopeGroup.delete(), [scopeGroupIds]);
    deleteScopeGroup.where((model: ScopeGroup) => model.id, "in (?)");
    Logger.info("remove all scopeGroupMap");
    const deleteScopeGroupMap: Query<ScopeScopeGroupMap> = new Query(new ScopeScopeGroupMap({}));
    deleteScopeGroupMap.where((model: ScopeScopeGroupMap) => model.scopeId, "in (?)");
    await con.query(deleteScopeGroupMap.delete(), [scopeIds]);

    Logger.info("insert clients");
    await con.insertBulk(new Query(new Client({})), clients);
    Logger.info("insert scopes");
    await con.insertBulk(new Query(new Scope({})), scopes);
    Logger.info("insert scopeGroups");
    await con.insertBulk(new Query(new ScopeGroup({})), scopeGroups);
    Logger.info("insert login methods");
    await con.insertBulk(new Query(new LoginMethod({})), loginMethods);
    Logger.info("insert client login method map");
    await con.insertBulk(new Query(new ClientLoginMethodMap({})), clientLoginMethodMaps);
    Logger.info("insert login method scope group map");
    await con.insertBulk(new Query(new LoginMethodScopeGroupMap({})), loginMethodScopeGroupMaps);
    Logger.info("insert scope scope group map");
    await con.insertBulk(new Query(new ScopeScopeGroupMap({})), scopeScopeGroupMaps);
    return;
  };

  private loadDatas = async (
    value: any
  ): Promise<any> => {
    const clients: Client[] = [];
    const loginMethods: LoginMethod[] = [];
    const clientLoginMethodMaps: ClientLoginMethodMap[] = [];
    const loginMethodScopeGroupMaps: LoginMethodScopeGroupMap[] = [];
    const scopeScopeGroupMaps: ScopeScopeGroupMap[] = [];
    const clientIds: number[] = [];
    const loginMethodIds: number[] = [];
    const scopeIds: number[] = [];
    const scopeGroupIds: number[] = [];
    const scopes: Scope[] = [];
    const scopeGroups: ScopeGroup[] = [];
    Logger.info("checking clients", value.clientMap.length);

    const map = {};
    value.scopeGroupMap.forEach((data: any) => map[data.name] = data.id);

    value.clientMap.forEach((data: IClientResponse) => {
      const client: Client = new Client({});
      client.id.set(data.id);
      client.userId.set(data.userId);
      client.clientId.set(data.clientId);
      client.clientSecret.set(data.clientSecret);
      client.desciption.set(data.description);
      client.status.set(data.status);
      clients.push(client);
      clientIds.push(data.id);
    });
    value.loginMap.forEach((loginMethodResponse: any) => {
      const loginMethod: LoginMethod = new LoginMethod({});
      loginMethod.id.set(loginMethodResponse.id);
      loginMethod.grantType.set(loginMethodResponse.grantType);
      loginMethod.isDefault.set(loginMethodResponse.isDefault === true ? 1 : 0);
      loginMethod.msName.set(loginMethodResponse.msName);
      loginMethod.msUri.set(loginMethodResponse.msUri);
      loginMethod.serviceCode.set(loginMethodResponse.serviceCode);
      loginMethod.extraData.set(loginMethodResponse.extraData);
      loginMethods.push(loginMethod);
      loginMethodIds.push(loginMethodResponse.id);
    });
    value.scopeMap.forEach((scopeResponse: any) => {
      const scope: Scope = new Scope({});
      scope.id.set(scopeResponse.id);
      scope.name.set(scopeResponse.name);
      scope.uriPattern.set(scopeResponse.uriPattern);
      scope.createdAt.set(scopeResponse.createdAt);
      scope.forwardType.set(scopeResponse.forwardType);
      scope.forwardDataJson.set(JSON.stringify(scopeResponse.forwardData));
      scopes.push(scope);
      scopeIds.push(scopeResponse.id)
    });
    value.scopeGroupMap.forEach((scopeGroupResponse: any) => {
      const scopeGroup: ScopeGroup = new ScopeGroup({});
      scopeGroup.id.set(scopeGroupResponse.id);
      scopeGroup.parentId.set(scopeGroupResponse.parentId);
      scopeGroup.name.set(scopeGroupResponse.name);
      scopeGroups.push(scopeGroup);
      scopeGroupIds.push(scopeGroupResponse.id);
    });

    value.clientMap.map((obj: any) => {
      obj.loginMethods.map((loginList: any) => {
        const clientLoginMethodMap = new ClientLoginMethodMap({});
        clientLoginMethodMap.clientId.set(obj.id);
        clientLoginMethodMap.loginMethodId.set(loginList);
        clientLoginMethodMaps.push(clientLoginMethodMap);
      });
    });

    value.scopeMap.map((obj: any) => {
      obj.scopeGroups.map((scopeGroupList: any) => {
        const scopeScopeGroupMap = new ScopeScopeGroupMap({});
        scopeScopeGroupMap.scopeId.set(obj.id);
        scopeScopeGroupMap.groupId.set(map[scopeGroupList]);
        scopeScopeGroupMaps.push(scopeScopeGroupMap);
      });
    });

    value.loginMap.map((obj: any) => {
      obj.scopeGroups.map((scopeGroupList: any) => {
        const loginMethodScopeGroupMap = new LoginMethodScopeGroupMap({});
        loginMethodScopeGroupMap.loginMethodId.set(obj.id);
        loginMethodScopeGroupMap.groupId.set(map[scopeGroupList]);
        loginMethodScopeGroupMaps.push(loginMethodScopeGroupMap);
      });
    });

    return doJobInTransaction((connection: Connection) => this.updateDb(connection, clientIds, loginMethodIds,
      clients, loginMethods, scopes, scopeGroups, clientLoginMethodMaps, loginMethodScopeGroupMaps, scopeIds, scopeGroupIds, scopeScopeGroupMaps));
  };
}

const importDbJsonService: ImportDbJsonService = new ImportDbJsonService();

export {
  ImportDbJsonService,
  importDbJsonService
}
