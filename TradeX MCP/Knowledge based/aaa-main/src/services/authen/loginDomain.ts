import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import ILoginDomainReq from "../../models/ILoginDomainReq";
import {Errors, Kafka, Logger} from "tradex-common";
import conf from "../../conf";
import {verifyToken} from "../TokenService";
import {IAccessToken} from "../../models/IAccessToken";
import ICommonLoginRes from "../../models/ICommonLoginRes";
import {loginViaThirdParty} from "./commonLoginThirdParty";
import ILoginRes from "../../models/response/ILoginRes";

const {ForwardError} = Errors;

/*
this method is to login to another domain.
scenario case is:
vscs user login by vcsc account in vcsc domain. Now he want to use some functions in tradex domain.
this method help him to loged in tradex domain by using access token from vcsc domain.
this method return a promise
 */
export default async function loginDomain(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod,
): Promise<ILoginRes> {
  const jwtConfig = conf.getJwt(request.domain);
  if (!jwtConfig) {
    throw new Errors.InvalidFieldValueError("domain", request.domain);
  }
  const accessToken: IAccessToken = await verifyToken(request.access_token, jwtConfig.publicKey);
  const username: string = accessToken.ud != null ? accessToken.ud.username : accessToken.su;
  const accountNumbers: string[] = accessToken.ud != null ?
    accessToken.ud.accountNumbers :
    [];
  if (username == null) {
    Logger.info("no username data in token", accessToken);
    throw new Errors.GeneralError("INVALID_TOKEN");
  }
  const loginData: ILoginDomainReq = {
    username: username,
    domain: request.domain,
    accountNumbers: accountNumbers,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };
  Logger.info("send login domain request", loginData);
  const uri: string = loginMethod.msUri.getDefault("/api/v1/loginDomain");
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(txId, loginMethod.msName.get(), uri, loginData, conf.timeouts.loginDomain);
  Logger.info("login domain result", msg);

  if (msg.data.status) {
    throw new ForwardError(msg.data.status);
  }
  const result: ICommonLoginRes = msg.data.data as ICommonLoginRes;

  return loginViaThirdParty(txId, result, request, client, loginMethod, accessToken.ud);
}
