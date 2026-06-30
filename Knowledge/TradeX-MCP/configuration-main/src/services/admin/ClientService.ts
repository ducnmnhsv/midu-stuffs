import { Service } from "typedi";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { ClientRepository } from "../../repositories/ClientRepository";
import { Errors, Kafka, Utils } from "tradex-common";
import * as validationUtils from "../../utils/validationUtils";
import Client, { parseToClientResponse } from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import { LoginMethodRepository } from "../../repositories/LoginMethodRepository";
import { ClientStatusEnum } from "../../constants/ClientStatusEnum";
import { v4 as uuid } from "uuid";
import {
  CLIENT_SYNC_TOPIC,
  CLIENT_UPDATE_TOPIC,
} from "../../constants/updateTopics";
import config from "../../config";
import { LoginMethodStepRepository } from "../../repositories/LoginMethodStepRepository";
import LoginMethodStep from "../../models/db/LoginMethodStep";
import ScopeGroup from "../../models/db/ScopeGroup";
import { IChangeClientSecret } from "../../models/request/admin/IChangeClientSecret";
import { IQuerySystemClientResponse } from "../../models/response/IQuerySystemClientResponse";
import { IClientResponse } from "../../models/response/IClientResponse";
import { ILoginMethodResponse } from "../../models/response/ILoginMethodResponse";
import { In } from "typeorm";

@Service()
export default class ClientService {
  public async findAllClient(
    request: TradexModelsConfiguration.QueryClientRequest,
  ): Promise<TradexModelsConfiguration.QueryClientResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateDomain(request.domain, invalidParams);
    validationUtils.validateFetchCount(request.fetchCount, invalidParams);
    validationUtils.validateLastSequence(request.lastSequence, invalidParams);
    validationUtils.validateIsFullData(request.isFullData, invalidParams);
    invalidParams.throwErr();

    const clientList: Client[] =
      await ClientRepository.findByClientRequest(request);
    return clientList.map(parseToClientResponse);
  }

  public async changeClientSecret(
    request: IChangeClientSecret,
  ): Promise<object> {
    const client: Client = await ClientRepository.findByClientId(
      request.clientId,
    );
    if (client == null) {
      throw new Errors.ObjectNotFoundError();
    }
    client.clientSecret = request.clientSecret;
    await ClientRepository.save(client);
    return {};
  }

  public async queryClientForUpdate(
    request: TradexModelsConfiguration.QuerySystemClientRequest,
  ): Promise<IQuerySystemClientResponse> {
    const error: Errors.InvalidParameterError =
      new Errors.InvalidParameterError();
    Utils.validate(request.domain, "domain").setRequire().throwValid(error);
    error.throwErr();
    const clientList: Client[] =
      await ClientRepository.queryClientForSystemRequest({
        domain: request.domain,
        lastQueriedTime:
          request.lastQueriedTime != null
            ? Utils.convertStringToDate(
                request.lastQueriedTime,
                Utils.DATETIME_DISPLAY_FORMAT,
              )
            : null,
      });
    const loginMethodIds: Set<number> = new Set();
    clientList.forEach((client: Client) => {
      client.loginMethods.forEach((lg: LoginMethod) => {
        loginMethodIds.add(lg.id);
      });
    });
    const steps: LoginMethodStep[] =
      await LoginMethodStepRepository.queryStepsIn(Array.from(loginMethodIds));
    const stepMaps: {
      [s: number]: TradexModelsConfiguration.LoginMethodStepResponse[];
    } = {};
    steps.forEach((step: LoginMethodStep) => {
      let list: TradexModelsConfiguration.LoginMethodStepResponse[] =
        stepMaps[step.loginMethodId];
      if (list == null) {
        list = [];
        stepMaps[step.loginMethodId] = list;
      }
      const stepConverse: TradexModelsConfiguration.LoginMethodStepResponse = {
        description: step.description,
        id: step.id,
        loginMethodId: step.loginMethodId,
        name: step.name,
        step: step.step,
      };
      stepConverse.scopeGroupIds = step.scopeGroups.map(
        (item: ScopeGroup) => item.id,
      );
      list.push(stepConverse);
    });
    const clients: IClientResponse[] = clientList.map((item: Client) => {
      const client: IClientResponse = parseToClientResponse(item);
      client.loginMethods.forEach((lg: ILoginMethodResponse) => {
        lg.steps = stepMaps[lg.id];
      });
      return client;
    });
    const lastQueriedTime: string = Utils.formatDateToDisplay(
      new Date(),
      Utils.DATETIME_DISPLAY_FORMAT,
    );
    return {
      clients: clients,
      lastQueriedTime: lastQueriedTime,
    };
  }

  public async findClientById(
    request: TradexModelsConfiguration.QueryClientByIdRequest,
  ): Promise<TradexModelsConfiguration.QueryClientByIdResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateId(request.id, invalidParams);
    invalidParams.throwErr();

    const client: Client = await ClientRepository.findById(request.id);
    if (client == null) {
      throw new Errors.ObjectNotFoundError();
    }
    return parseToClientResponse(client);
  }

  public async addClient(
    request: TradexModelsConfiguration.PostClientRequest,
  ): Promise<any> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.userId, "userId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.clientId, "clientId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.clientSecret, "clientSecret")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.domain, "domain")
      .setRequire()
      .throwValid(invalidParams);

    invalidParams.throwErr();

    const client = new Client();
    client.userId = request.userId;
    client.clientId = request.clientId;
    client.clientSecret = request.clientSecret;
    client.domain = request.domain;
    client.status = ClientStatusEnum.ENABLED;
    client.description = request.description != null ? request.description : "";
    client.appVersion = request.appVersion != null ? request.appVersion : "";

    if (request.loginMethodIds != null && request.loginMethodIds.length > 0) {
      const loginMethods: LoginMethod[] = await LoginMethodRepository.findByIds(
        request.loginMethodIds,
      );
      if (loginMethods.length < request.loginMethodIds.length) {
        throw new Errors.InvalidFieldValueError(
          "loginMethodIds",
          "LOGIN_METHOD_NOT_FOUND",
        );
      }
      client.loginMethods = loginMethods;
    }

    await ClientRepository.save(client);

    Kafka.getInstance().sendMessage(
      uuid(),
      CLIENT_UPDATE_TOPIC,
      "newClient",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        CLIENT_SYNC_TOPIC,
        "newClient",
        request,
      );
    }

    return;
  }

  public async updateClient(
    request: TradexModelsConfiguration.PutClientRequest,
  ): Promise<TradexModelsConfiguration.PutClientResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateId(request.id, invalidParams);
    invalidParams.throwErr();

    const client = await ClientRepository.findOne({
      where: { id: request.id },
    });
    if (client == null) {
      throw new Errors.ObjectNotFoundError();
    }
    client.userId = request.userId != null ? request.userId : client.userId;
    client.clientId =
      request.clientId != null ? request.clientId : client.clientId;
    client.clientSecret =
      request.clientSecret != null ? request.clientSecret : client.clientSecret;
    client.description =
      request.description != null ? request.description : client.description;
    client.domain = request.domain != null ? request.domain : client.domain;
    client.appVersion = request.appVersion != null ? request.appVersion : "";

    if (request.loginMethodIds != null && request.loginMethodIds.length > 0) {
      const loginMethods: LoginMethod[] = await LoginMethodRepository.findBy({
        id: In(request.loginMethodIds),
      });
      if (loginMethods.length < request.loginMethodIds.length) {
        throw new Errors.InvalidFieldValueError(
          "loginMethodIds",
          "LOGIN_METHOD_NOT_FOUND",
        );
      }
      client.loginMethods = loginMethods;
    }

    await ClientRepository.save(client);

    Kafka.getInstance().sendMessage(
      uuid(),
      CLIENT_UPDATE_TOPIC,
      "updateClient",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        CLIENT_SYNC_TOPIC,
        "updateClient",
        request,
      );
    }

    return {};
  }

  public async deleteClient(
    request: TradexModelsConfiguration.DeleteClientRequest,
  ): Promise<TradexModelsConfiguration.DeleteClientResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateId(request.id, invalidParams);
    invalidParams.throwErr();

    const client = await ClientRepository.findById(request.id);
    if (client == null) {
      throw new Errors.ObjectNotFoundError();
    }
    await ClientRepository.createQueryBuilder()
      .update(Client)
      .set({ status: ClientStatusEnum.DISABLED })
      .where("id = :id", { id: request.id })
      .execute();

    Kafka.getInstance().sendMessage(
      uuid(),
      CLIENT_UPDATE_TOPIC,
      "deleteClient",
      request,
    );
    if (config.domain === Utils.TRADEX_DOMAIN) {
      Kafka.getInstance().sendMessage(
        uuid(),
        CLIENT_SYNC_TOPIC,
        "deleteClient",
        request,
      );
    }

    return {};
  }
}
