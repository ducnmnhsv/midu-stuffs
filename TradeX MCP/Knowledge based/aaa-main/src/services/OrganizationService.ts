/* tslint:disable:cyclomatic-complexity */
import { Utils, Errors, Logger, Kafka } from "tradex-common";
import { Connection, doJobInTransaction } from "../db/async";
import conf from "../conf";
import Client from "../models/db/Client";
import LoginMethod from "../models/db/LoginMethod";
import ILoginReq from "../models/request/ILoginReq";
import IServiceLoginRes from "../models/IServiceLoginRes";
import { executeLoginDirectToService } from "./authen/loginDirectToService";
import ILoginOrgRequest from "../models/request/ILoginOrgRequest";

const { validate } = Utils;
const { ForwardError } = Errors;

export async function loginOrganization(
  request: ILoginReq,
  msgId: string,
  client: Client,
  loginMethod: LoginMethod
) {
  const invalidParams = new Errors.InvalidParameterError();
  validate(request.orgLoginToken, "orgLoginToken")
    .setRequire()
    .throwValid(invalidParams);
  validate(request.organization, "organization")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  const uri: string = loginMethod.msUri.getDefault('post:/api/v1/virtualCore/login');
  const loginOrgRequest: ILoginOrgRequest = {
    organizationId: request.organization,
    orgLoginToken: request.orgLoginToken,
    deviceId: request.device_id,
    rid: (request as any).rid,
  }
  const msgResponse: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    msgId, 
    loginMethod.msName.get(), 
    uri, 
    loginOrgRequest,
    conf.timeouts.loginTechx);
  if (msgResponse.data.status) {
    throw new ForwardError(msgResponse.data.status);
  }
  const loginResult: IServiceLoginRes = msgResponse.data.data;
  Logger.info(msgId, "login and generate token");
  return doJobInTransaction((con: Connection) => executeLoginDirectToService(msgId, {
    grant_type: request.grant_type,
    client_id: request.client_id,
    client_secret: request.client_secret,
    username: loginResult.userData.username,
    password: '',
    session_time_in_minute: request.session_time_in_minute,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
    device_id: request.device_id,
  }, client, loginMethod, con, loginResult, loginResult.userInfo), msgId);
}
