import ICommonLoginRes from "../../models/ICommonLoginRes";
import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import {connectAndDo, Connection, doJobInTransaction} from "../../db/async";
import ILoginRes, {IUserInfo} from "../../models/response/ILoginRes";
import LoginMethodScopeGroupMap from "../../models/db/LoginMethodScopeGroupMap";
import {scopeService} from "../ScopeService";
import createToken from "./createToken";
import {Logger, Models} from "tradex-common";
import IServiceLoginRes from "../../models/IServiceLoginRes";

export async function loginViaThirdParty(
  txId: string,
  loginResult: IServiceLoginRes | ICommonLoginRes,
  request: ILoginReq,
  client: Client,
  loginMethod: LoginMethod,
  userData?: Models.IUserData,
  addedUserInfo?: IUserInfo,
): Promise<ILoginRes> {
  Logger.info("generate refresh token");
  const scopeGroups: LoginMethodScopeGroupMap[] = await connectAndDo((con: Connection) => scopeService.findScopeGroupsAsync(
    loginMethod.id.get(),
    con
  ));
  const scopeGroupIds: number[] = scopeGroups.map(
    (lsg: LoginMethodScopeGroupMap) => lsg.groupId.get()
  );
  return doJobInTransaction((connection: Connection) => createToken({
      txId,
      request,
      loginResult,
      clientId: client.id.get(),
      scopeGroupIds,
      userId: (loginResult as IServiceLoginRes).userData.id || (loginResult as ICommonLoginRes).id,
      loginMethod,
      userData: userData || (loginResult as IServiceLoginRes).userData,
      addedUserInfo: addedUserInfo || (loginResult as IServiceLoginRes).userInfo,
    },
    connection
  ));
}
