import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import ILoginRes from "../../models/response/ILoginRes";
import createToken from "./createToken";
import { scopeService } from "../ScopeService";
import LoginMethodScopeGroupMap from "../../models/db/LoginMethodScopeGroupMap";
import { Connection, doJobInTransaction } from "../../db/async";

export default async function loginClientCredentials(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const scopeGroups: LoginMethodScopeGroupMap[] = await scopeService.findScopeGroupsAsync(
    loginMethod.id.get()
  );
  return doJobInTransaction((connection: Connection) =>
    createToken({
      txId,
      request,
      clientId: client.id.get(),
      scopeGroupIds: scopeGroups.map((item: LoginMethodScopeGroupMap) => item.groupId.get()),
      userId: client.userId.get(),
      loginMethod,
    }, connection
    )
  );
}
