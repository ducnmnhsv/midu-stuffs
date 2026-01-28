import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import {Errors, Kafka, Utils} from "tradex-common";
import ILoginRes from "../../models/response/ILoginRes";
import loginDomain from "./loginDomain";


export async function loginAccessDomain(
  request: ILoginReq,
  client: Client,
  loginMethod: LoginMethod,
  kMsg: Kafka.IMessage
): Promise<ILoginRes> {
  const invalidParams = new Errors.InvalidParameterError();
  Utils.validate(request.access_token, "access_token")
    .setRequire()
    .throwValid(invalidParams);
  Utils.validate(request.domain, "domain")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  return loginDomain(
    request,
    `${kMsg.transactionId}`,
    client,
    loginMethod
  );
}
