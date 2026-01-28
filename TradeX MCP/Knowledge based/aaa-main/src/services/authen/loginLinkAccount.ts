import {Errors, Utils} from "tradex-common";
import ILoginRes from "../../models/response/ILoginRes";
import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import IServiceLoginReq from "../../models/IServiceLoginReq";
import { verifySign } from "../../utils/sign";
import { findPartner } from "../linkAccountService";
import loginDirectToService from "./loginDirectToService";
import { rsaPrivateKey } from "../../utils/rsa";
import conf from "../../conf";

async function loginLinkAccount(request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const invalidParams = new Errors.InvalidParameterError();
  Utils.validate(request.username, "username")
    .setRequire()
    .throwValid(invalidParams);
  Utils.validate(request.password, "password")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  request.username = request.username.trim();
  const separatorIndex = request.username.indexOf(":");
  if (separatorIndex <= 0 || separatorIndex === request.username.length - 1) {
    throw new Errors.InvalidFieldValueError("username", request.username);
  }
  const partnerId = request.username.substring(0, separatorIndex);
  request.username = request.username.substring(separatorIndex + 1);
  if (conf.rsa.enableEncryptPassword) {
    request.password = Utils.rsaDecrypt(request.password, rsaPrivateKey);
  }
  const partner = await findPartner(partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("INVALID_PARTNER");
  }
  if (!verifySign(partner.publicKey.get(), request.username, request.password)) {
    throw new Errors.GeneralError("INVALID_CLIENT_CREDENTIAL");
  }
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

export default loginLinkAccount;
