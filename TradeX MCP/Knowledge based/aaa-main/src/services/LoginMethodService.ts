import {Models} from 'tradex-common';
import {Connection, getConnectAndQuery} from "../db/async";
import LoginMethod from "../models/db/LoginMethod";
import {AndCondition, FieldCondition, Query} from "../models/db/BaseModel";
import ClientLoginMethodMap from "../models/db/ClientLoginMethodMap";
import {FieldInfo} from "mysql";

export async function findLoginMethods(
  clientId: number,
  grantType: string,
  secCode: string,
): Promise<LoginMethod[]> {
  const params: any[] = [grantType, clientId];
  if (secCode && secCode !== "") {
    params.push(secCode);
  }
  const query: Query<LoginMethod> = new Query<LoginMethod>(new LoginMethod({}));
  const where: AndCondition = query.whereAnd((model: LoginMethod) => model.grantType, "= ?")
    .addCondition(
      query
        .join(new ClientLoginMethodMap({}), (lm: LoginMethod) => lm.id
          , (clm: ClientLoginMethodMap) => clm.loginMethodId)
        .fieldCondition((clm: ClientLoginMethodMap) => clm.clientId, "= ?"),
    );
  if (secCode && secCode !== "") {
    where.addCondition(new FieldCondition(query.model.serviceCode, "= ?", query.getAlias()));
  }

  const results: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(query.select(), params);
  return results.left.map((row: any) => new LoginMethod(row));
}


export async function findLoginMethodById(id: number, connection: Connection): Promise<LoginMethod> {
  const query: Query<LoginMethod> = new Query<LoginMethod>(new LoginMethod({}));
  query.where((model: LoginMethod) => model.id, "=?");
  const results: any = await connection.queryResult(query.select(), [id]);
  return new LoginMethod(results[0]);
}
