import { Service } from "typedi";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { LoginMethodRepository } from "../../repositories/LoginMethodRepository";
import * as validationUtils from "../../utils/validationUtils";
import { Errors, Kafka, Utils } from "tradex-common";
import LoginMethod, {
  parseToLoginMethodResponse,
} from "../../models/db/LoginMethod";
import ScopeGroup from "../../models/db/ScopeGroup";
import { ScopeGroupRepository } from "../../repositories/ScopeGroupRepository";
import { v4 as uuid } from "uuid";
import { LOGIN_METHOD_UPDATE_TOPIC } from "../../constants/updateTopics";

@Service()
export default class LoginMethodService {
  public async findAllLoginMethod(
    request: TradexModelsConfiguration.QueryLoginMethodRequest,
  ): Promise<TradexModelsConfiguration.QueryLoginMethodResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateFetchCount(request.fetchCount, invalidParams);
    validationUtils.validateLastSequence(request.lastSequence, invalidParams);
    invalidParams.throwErr();

    const loginMethodList: LoginMethod[] =
      await LoginMethodRepository.findByLoginMethodRequest(request);
    return loginMethodList.map(parseToLoginMethodResponse);
  }

  public async queryLoginMethodForUpdate(
    request: TradexModelsConfiguration.QuerySystemLoginMethodRequest,
  ): Promise<TradexModelsConfiguration.QuerySystemLoginMethodResponse> {
    const loginMethodList: LoginMethod[] =
      await LoginMethodRepository.queryLoginMethodForUpdateRequest(request);
    return loginMethodList.map(parseToLoginMethodResponse);
  }

  public async findLoginMethodById(
    request: TradexModelsConfiguration.QueryLoginMethodByIdRequest,
  ): Promise<TradexModelsConfiguration.QueryLoginMethodByIdResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateId(request.id, invalidParams);
    invalidParams.throwErr();

    const loginMethod: LoginMethod =
      await LoginMethodRepository.findByLoginMethodId(request);
    if (loginMethod == null) {
      throw new Errors.ObjectNotFoundError();
    }
    return parseToLoginMethodResponse(loginMethod);
  }

  public async addNewLoginMethod(
    request: TradexModelsConfiguration.PostLoginMethodRequest,
  ): Promise<TradexModelsConfiguration.PostLoginMethodResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.grantType, "grantType")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.serviceCode, "serviceCode")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.msName, "msName")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();

    const loginMethod = new LoginMethod();
    loginMethod.grantType = request.grantType;
    loginMethod.serviceCode = request.serviceCode;
    loginMethod.msName = request.msName;
    loginMethod.isDefault =
      request.isDefault != null ? request.isDefault : false;
    loginMethod.msUri = request.msUri;

    if (request.scopeGroupIds != null && request.scopeGroupIds.length > 0) {
      const scopeGroups: ScopeGroup[] = await ScopeGroupRepository.findByIds(
        request.scopeGroupIds,
      );
      if (scopeGroups.length < request.scopeGroupIds.length) {
        throw new Errors.InvalidFieldValueError(
          "scopeGroups",
          "SCOPE_GROUP_NOT_FOUND",
        );
      }
      loginMethod.scopeGroups = scopeGroups;
    }

    await LoginMethodRepository.save(loginMethod);

    Kafka.getInstance().sendMessage(
      uuid(),
      LOGIN_METHOD_UPDATE_TOPIC,
      "newLoginMethod",
      request,
    );

    return {};
  }

  public async updateLoginMethod(
    request: TradexModelsConfiguration.PutLoginMethodRequest,
  ): Promise<TradexModelsConfiguration.PutLoginMethodResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateId(request.id, invalidParams);
    invalidParams.throwErr();

    const loginMethod: LoginMethod = await LoginMethodRepository.findOneBy({
      id: request.id,
    });
    if (loginMethod == null) {
      throw new Errors.ObjectNotFoundError();
    }
    loginMethod.grantType =
      request.grantType != null ? request.grantType : loginMethod.grantType;
    loginMethod.serviceCode =
      request.serviceCode != null
        ? request.serviceCode
        : loginMethod.serviceCode;
    loginMethod.msName =
      request.msName != null ? request.msName : loginMethod.msName;
    loginMethod.isDefault =
      request.isDefault != null ? request.isDefault : loginMethod.isDefault;
    loginMethod.msUri =
      request.msUri != null ? request.msUri : loginMethod.msUri;

    if (request.scopeGroupIds != null && request.scopeGroupIds.length > 0) {
      const scopeGroups: ScopeGroup[] = await ScopeGroupRepository.findByIds(
        request.scopeGroupIds,
      );
      if (scopeGroups.length < request.scopeGroupIds.length) {
        throw new Errors.InvalidFieldValueError(
          "scopeGroupIds",
          "SCOPE_GROUP_NOT_FOUND",
        );
      }
      loginMethod.scopeGroups = scopeGroups;
    }

    await LoginMethodRepository.save(loginMethod);

    Kafka.getInstance().sendMessage(
      uuid(),
      LOGIN_METHOD_UPDATE_TOPIC,
      "updateLoginMethod",
      request,
    );

    return {};
  }

  public async deleteLoginMethod(
    request: TradexModelsConfiguration.DeleteLoginMethodRequest,
  ): Promise<TradexModelsConfiguration.DeleteLoginMethodResponse> {
    const invalidParams = new Errors.InvalidParameterError();
    validationUtils.validateId(request.id, invalidParams);
    invalidParams.throwErr();

    const loginMethod = await LoginMethodRepository.findOneBy({
      id: request.id,
    });
    if (loginMethod == null) {
      throw new Errors.ObjectNotFoundError();
    }

    await LoginMethodRepository.createQueryBuilder()
      .delete()
      .from(LoginMethod)
      .where("id = :id", { id: request.id })
      .execute();

    Kafka.getInstance().sendMessage(
      uuid(),
      LOGIN_METHOD_UPDATE_TOPIC,
      "deleteLoginMethod",
      request,
    );

    return {};
  }
}
