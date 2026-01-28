import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import ILoginRes from "../../models/response/ILoginRes";
import IServiceLoginReq from "../../models/IServiceLoginReq";
import loginDirectToService from "./loginDirectToService";
import { Errors, Utils } from "tradex-common";
import { GrantType } from "../../constants/GrantType";

const { validate } = Utils;

export async function loginPassword(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const invalidParams = new Errors.InvalidParameterError();
  validate(request.username, "username")
    .setRequire()
    .throwValid(invalidParams);
  if (loginMethod.grantType.get() !== GrantType.KB_FINA) {
    validate(request.password, "password")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
  }
  request.username = request.username.trim();
  const loginData: IServiceLoginReq = {
    username: request.username,
    password: request.password,
    systemName: loginMethod.serviceCode.get(),
    device_id: request.device_id,
    login_social_token: request.login_social_token,
    login_social_type: request.login_social_type,
    sourceIp: request.sourceIp,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
  };
  const uri: string = loginMethod.msUri.getDefault("/api/v1/login");
  return loginDirectToService(
    uri,
    loginData,
    request,
    txId,
    client,
    loginMethod
  );
}
