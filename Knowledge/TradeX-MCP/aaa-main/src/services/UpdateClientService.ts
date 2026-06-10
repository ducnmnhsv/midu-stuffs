import {Kafka, Logger, Utils} from "tradex-common";
import conf from "../conf";
import {Connection, doJobInTransaction} from "../db/async";
import {Query} from "../models/db/BaseModel";
import Client from "../models/db/Client";
import LoginMethodScopeGroupMap from "../models/db/LoginMethodScopeGroupMap";
import LoginMethod from "../models/db/LoginMethod";
import ClientLoginMethodMap from "../models/db/ClientLoginMethodMap";
import ISystemQueryRequest from "../models/request/ISystemQueryRequest";
import LoginMethodStep from "../models/db/LoginMethodStep";
import LoginMethodStepScopeGroupMap from "../models/db/LoginMethodStepScopeGroupMap";

class UpdateClientService {

  public async update(retryTime: number = 0): Promise<any> {
    Logger.info("start loading clients");
    return Utils.asyncWithRetry(() => this.loadClients(), retryTime);
  }

  private updateDb = async (
    con: Connection,
    clientIds: number[],
    loginMethodIds: number[],
    clients: Client[],
    loginMethods: LoginMethod[],
    clientLoginMethodMaps: ClientLoginMethodMap[],
    loginMethodScopeGroupMaps: LoginMethodScopeGroupMap[],
    loginMethodStepMap: Map<number, LoginMethodStep>,
    loginMethodStepScopeGroups: LoginMethodStepScopeGroupMap[],
    loginMethodStepIds: number[],
    lastUpdatedAt: string,
  ): Promise<any> => {
    if (loginMethodIds.length > 0) {
      Logger.info("remove all mapping loginMethodScopeGroup");
      const deleteLoginMethodScopeGroup: Query<LoginMethodScopeGroupMap> = new Query(new LoginMethodScopeGroupMap({}));
      deleteLoginMethodScopeGroup.where((model: LoginMethodScopeGroupMap) => model.loginMethodId, "in (?)");
      await con.query(deleteLoginMethodScopeGroup.delete(), [loginMethodIds]);
    }

    if (clientIds.length > 0) {
      Logger.info("remove all mapping clientLoginMethod");
      const deleteClientLoginMethod: Query<ClientLoginMethodMap> = new Query(new ClientLoginMethodMap({}));
      deleteClientLoginMethod.where((model: ClientLoginMethodMap) => model.clientId, "in (?)");
      await con.query(deleteClientLoginMethod.delete(), [clientIds]);
    }

    if (loginMethodStepIds.length > 0) {
      Logger.info("remove all mapping step-scopegroup");
      const deleteLoginMethodStepScopeGroup: Query<LoginMethodStepScopeGroupMap> = new Query(new LoginMethodStepScopeGroupMap({}));
      deleteLoginMethodStepScopeGroup.where((model: LoginMethodStepScopeGroupMap) => model.stepId, "in (?)");
      await con.query(deleteLoginMethodStepScopeGroup.delete(), [loginMethodStepIds]);
    }

    if (loginMethodStepIds.length > 0) {
      Logger.info("remove all login method steps");
      const deleteLoginMethodStep: Query<LoginMethodStep> = new Query(new LoginMethodStep({}));
      deleteLoginMethodStep.where((model: LoginMethodStep) => model.id, "in (?)");
      await con.query(deleteLoginMethodStep.delete(), [loginMethodStepIds]);
    }

    if (loginMethodIds.length > 0) {
      Logger.info("remove all login methods");
      const deleteLoginMethod: Query<LoginMethod> = new Query(new LoginMethod({}));
      deleteLoginMethod.where((model: LoginMethod) => model.id, "in (?)");
      await con.query(deleteLoginMethod.delete(), [loginMethodIds]);
    }

    if (clientIds.length > 0) {
      Logger.info("remove all clients");
      const deleteClient: Query<Client> = new Query(new Client({}));
      deleteClient.where((model: Client) => model.id, "in (?)");
      await con.query(deleteClient.delete(), [clientIds]);
    }

    if (clients.length > 0) {
      Logger.info("insert clients");
      await con.insertBulk(new Query(new Client({})), clients);
    }

    if (loginMethods.length > 0) {
      Logger.info("insert login methods");
      await con.insertBulk(new Query(new LoginMethod({})), loginMethods);
    }

    const loginMethodSteps = Array.from(loginMethodStepMap.values());
    if (loginMethodSteps.length > 0) {
      Logger.info("insert login method steps");
      await con.insertBulk(new Query(new LoginMethodStep({})), loginMethodSteps);
    }

    if (clientLoginMethodMaps.length > 0) {
      Logger.info("insert client login method map");
      await con.insertBulk(new Query(new ClientLoginMethodMap({})), clientLoginMethodMaps);
    }

    if (loginMethodScopeGroupMaps.length > 0) {
      Logger.info("insert login method scope group map");
      await con.insertBulk(new Query(new LoginMethodScopeGroupMap({})), loginMethodScopeGroupMaps);
    }

    if (loginMethodStepScopeGroups.length > 0) {
      Logger.info("insert login method step - scope group map");
      return con.insertBulk(new Query(new LoginMethodStepScopeGroupMap({})), loginMethodStepScopeGroups);
    }
  };

  private loadClients = async (): Promise<any> => {
    const request: ISystemQueryRequest = {
      domain: conf.domain,
    };
    const scopeResponseMessage: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      "", conf.clients.loadFrom.topic, conf.clients.loadFrom.uris.client, request, conf.timeouts.loadScope);
    const data: IClientForUpdateResponse = Kafka.getResponse(scopeResponseMessage);
    if (data.clients == null || data.clients.length === 0) {
      Logger.info("no more clients are updated");
      return null;
    }
    const clients: Client[] = [];
    const loginMethodsMap: Map<number, LoginMethod> = new Map();
    const loginMethodStepMap: Map<number, LoginMethodStep> = new Map();
    const loginMethodStepScopeGroups: LoginMethodStepScopeGroupMap[] = [];
    const clientLoginMethodMaps: ClientLoginMethodMap[] = [];
    const loginMethodScopeGroupMaps: LoginMethodScopeGroupMap[] = [];
    const clientIds: number[] = [];
    const loginMethodIds: number[] = [];
    const loginMethodStepIds: number[] = [];
    Logger.info("loading clients", data.clients.length);
    data.clients.forEach((value: IClientResponse) => {
      const client: Client = new Client({});
      client.id.set(value.id);
      client.userId.set(value.userId);
      client.clientId.set(value.clientId);
      client.clientSecret.set(value.clientSecret);
      client.desciption.set(value.description);
      client.status.set(value.status);
      client.appVersion.set(value.appVersion);
      clients.push(client);
      clientIds.push(value.id);
      if (value.loginMethods != null && value.loginMethods.length > 0) {
        value.loginMethods.forEach((loginMethodResponse: ILoginMethodResponse) => {
          const loginMethodExist: boolean = loginMethodsMap.has(loginMethodResponse.id);
          const clientLoginMethodMap: ClientLoginMethodMap = new ClientLoginMethodMap({});
          clientLoginMethodMap.clientId.set(value.id);
          clientLoginMethodMap.loginMethodId.set(loginMethodResponse.id);
          clientLoginMethodMaps.push(clientLoginMethodMap);
          if (!loginMethodExist) {
            loginMethodIds.push(loginMethodResponse.id);
            const loginMethod: LoginMethod = new LoginMethod({});
            loginMethod.id.set(loginMethodResponse.id);
            loginMethod.grantType.set(loginMethodResponse.grantType);
            loginMethod.isDefault.set(loginMethodResponse.isDefault === true ? 1 : 0);
            loginMethod.msName.set(loginMethodResponse.msName);
            loginMethod.msUri.set(loginMethodResponse.msUri);
            loginMethod.serviceCode.set(loginMethodResponse.serviceCode);
            loginMethod.accessTokenTtl.set(loginMethodResponse.accessTokenTtl);
            loginMethod.refreshTokenTtl.set(loginMethodResponse.refreshTokenTtl);
            loginMethod.refreshTokenLongTtl.set(loginMethodResponse.refreshTokenLongTtl);
            loginMethod.multiFactorTtl.set(loginMethodResponse.multiFactorTtl);
            loginMethod.extraData.set(loginMethodResponse.extraData);
            loginMethodsMap.set(loginMethodResponse.id, loginMethod);
            if (loginMethodResponse.scopeGroupIds != null && loginMethodResponse.scopeGroupIds.length > 0) {
              loginMethodResponse.scopeGroupIds.forEach((scopeGroupId: number) => {
                const loginMethodScopeGroupMap: LoginMethodScopeGroupMap = new LoginMethodScopeGroupMap({});
                loginMethodScopeGroupMap.groupId.set(scopeGroupId);
                loginMethodScopeGroupMap.loginMethodId.set(loginMethodResponse.id);
                loginMethodScopeGroupMaps.push(loginMethodScopeGroupMap);
              });
            }
            if (loginMethodResponse.steps != null && loginMethodResponse.steps.length > 0) {
              loginMethodResponse.steps.forEach((step: ILoginMethodStepResponse) => {
                let stepDb: LoginMethodStep = loginMethodStepMap.get(step.id);
                if (stepDb == null) {
                  stepDb = new LoginMethodStep({});
                  loginMethodStepIds.push(step.id);
                  stepDb.id.set(step.id);
                  stepDb.loginMethodId.set(step.loginMethodId);
                  stepDb.step.set(step.step);
                  stepDb.name.set(step.name);
                  stepDb.desc.set(step.description);
                  loginMethodStepMap.set(step.id, stepDb);
                  if (step.scopeGroupIds != null) {
                    step.scopeGroupIds.forEach((sgId: number) => {
                      const mapItem: LoginMethodStepScopeGroupMap = new LoginMethodStepScopeGroupMap({});
                      mapItem.stepId.set(step.id);
                      mapItem.groupId.set(sgId);
                      loginMethodStepScopeGroups.push(mapItem);
                    });
                  }
                }
              });
            }
          }
        });
      }
    });
    if (clientIds.length > 0) {
      return doJobInTransaction((connection: Connection) => this.updateDb(connection, clientIds, loginMethodIds,
        clients, Array.from(loginMethodsMap.values()), clientLoginMethodMaps, loginMethodScopeGroupMaps,
        loginMethodStepMap, loginMethodStepScopeGroups, loginMethodStepIds, data.lastQueriedTime));
    }
    return null;
  };
}

const updateClientService: UpdateClientService = new UpdateClientService();

export {
  UpdateClientService,
  updateClientService,
};
