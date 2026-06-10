import { Errors, Kafka, Logger } from "tradex-common";
import IServiceLoginRes from "../../models/IServiceLoginRes";
import { scopeService } from "../ScopeService";
import LoginMethodScopeGroupMap from "../../models/db/LoginMethodScopeGroupMap";
import ILoginRes from "../../models/response/ILoginRes";
import ILoginReq from "../../models/request/ILoginReq";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import { Connection, doJobInTransaction } from "../../db/async";
import createToken from "./createToken";
import ObjectNotFoundError from "../../errors/ObjectNotFoundError";
import conf from '../../conf';
import { findAccountDemo, updateAccountDemo } from './loginAccountDemo';
import AccountDemo from '../../models/db/AccountDemo';
import IServiceLoginReq from '../../models/IServiceLoginReq';


// login account demo
async function loginAccountDemoService(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const accountDemo: AccountDemo = await findAccountDemo(loginMethod.serviceCode.get());
  if (accountDemo == null) {
    throw new ObjectNotFoundError();
  }

  let loginResult: IServiceLoginRes = null;
  let msg: Kafka.IMessage = null;
  const loginServiceResultStr = accountDemo.loginResponse.get();
  if (loginServiceResultStr != null && loginServiceResultStr.length > 0) {
    loginResult = JSON.parse(loginServiceResultStr);
  } else {
    const uri: string = loginMethod.msUri.getDefault("/api/v1/login");
    const loginData: IServiceLoginReq = {
      username: accountDemo.realUsername.get(),
      password: accountDemo.realPassword.get(),
      systemName: accountDemo.domain.get(),
    };
    msg = await Kafka.getInstance().sendRequestAsync(txId, loginMethod.msName.get(), uri, loginData, conf.timeouts.loginDirectToService);
  }

  return doJobInTransaction((connection: Connection) =>
    executeLoginAccountDemoService(request, connection, client, loginMethod, msg, loginResult, accountDemo))
}

async function executeLoginAccountDemoService(
  request: ILoginReq,
  connection: Connection,
  client: Client,
  loginMethod: LoginMethod,
  msg: Kafka.IMessage,
  loginRs: IServiceLoginRes,
  accountDemo: AccountDemo
): Promise<ILoginRes> {
  let loginResult: IServiceLoginRes = null;
  if (msg && msg.data.status) {
    throw new Errors.ForwardError(msg.data.status);
  }
  if (!loginRs) {
    loginResult = msg.data.data;
  } else {
    loginResult = loginRs;
  }
  accountDemo.loginResponse.set(JSON.stringify(loginResult));
  await updateAccountDemo(accountDemo, connection);
  Logger.info(`login result ${JSON.stringify(loginResult)}`);

  const scopeGroups: LoginMethodScopeGroupMap[] = await scopeService.findScopeGroupsAsync(loginMethod.id.get(), connection);
  const scopeGroupIds: number[] = scopeGroups.map((lsg: LoginMethodScopeGroupMap) => lsg.groupId.get());

  return createToken({
    txId: msg.transactionId as string,
    request,
    loginResult,
    clientId: client.id.get(),
    scopeGroupIds,
    loginMethod,
    userData: loginResult.userData,
  }
    , connection
  );
}

export default loginAccountDemoService;
