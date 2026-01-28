import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import {Logger} from "tradex-common";
import ILoginCAReq from "../../models/ILoginCAReq";
import loginDirectToService from "./loginDirectToService";
import ILoginRes from "../../models/response/ILoginRes";

/*
this method is to login using ca token.
scenario case is:
vscs user has registered ca to therr account and want to login with certificate instead of password.
this method return a promise
 */
export default async function loginPasswordCA(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod,
): Promise<ILoginRes> {
  const loginData: ILoginCAReq = {
    data: request.data,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };
  Logger.info("send login ca request", loginData);
  const uri: string = loginMethod.msUri.getDefault("/api/v1/ca/login");
  return loginDirectToService(uri, loginData, request, txId, client, loginMethod);
}
