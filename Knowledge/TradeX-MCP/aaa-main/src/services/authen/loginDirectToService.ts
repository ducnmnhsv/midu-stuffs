import { Errors, Kafka, Logger } from "tradex-common";
import IServiceLoginRes from "../../models/IServiceLoginRes";
import { scopeService } from "../ScopeService";
import LoginMethodScopeGroupMap from "../../models/db/LoginMethodScopeGroupMap";
import ILoginRes, { IUserInfo } from "../../models/response/ILoginRes";
import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import { Connection, doJobInTransaction } from "../../db/async";
import createToken from "./createToken";
import conf from "../../conf";
import IServiceLoginReq from "../../models/IServiceLoginReq";
import ILoginCAReq from "../../models/ILoginCAReq";
import Cache from "../../utils/Cache";
import ServiceUser from "../../models/db/ServiceUser";
import { findOrCreateServiceUser } from "../UserService";


// const mapUserInfo: {[k: string]: Kafka.IMessage} = {};
const scopeGroupsCache: Cache<number[]> = new Cache<number[]>();

// login to the company directly
async function loginDirectToService(
  uri: string,
  loginData: IServiceLoginReq | ILoginCAReq,
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    loginMethod.msName.get(),
    uri,
    loginData,
    conf.timeouts.loginDirectToService
  );

  if (msg.data.status) {
    throw new Errors.ForwardError(msg.data.status);
  }
  const serviceUser: ServiceUser = await findOrCreateServiceUser(request.username);
  const loginResult: IServiceLoginRes = msg.data.data;
  return doJobInTransaction((con: Connection) =>
    executeLoginDirectToService(txId, request, client, loginMethod, con, loginResult, serviceUser.toUserInfo())
  );
}

export async function executeLoginDirectToService(
  txId: string,
  request: ILoginReq,
  client: Client,
  loginMethod: LoginMethod,
  connection: Connection,
  loginResult: IServiceLoginRes,
  userInfo?: IUserInfo
): Promise<ILoginRes> {
  Logger.info(`login result ${JSON.stringify(loginResult)}`);
  const loginMethodId = loginMethod.id.get();
  const scopeGroupIds: number[] = await scopeGroupsCache.findOrget(`grant-${loginMethodId}`, async () => {
    const scopeGroups: LoginMethodScopeGroupMap[] = await scopeService.findScopeGroupsAsync(
      loginMethodId,
      connection
    );
    return scopeGroups.map(
      (lsg: LoginMethodScopeGroupMap) => lsg.groupId.get()
    );
  }, conf.cacheTimeout);
  return createToken({
    txId,
    request,
    loginResult,
    clientId: client.id.get(),
    scopeGroupIds,
    userId: userInfo == null ? null : userInfo.id,
    loginMethod,
    roles: [],
    userData: loginResult.userData,
    addedUserInfo: userInfo,
  }, connection
  );
}

export default loginDirectToService;
