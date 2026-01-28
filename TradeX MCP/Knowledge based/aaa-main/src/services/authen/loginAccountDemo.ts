import {Connection, getConnectAndQuery} from "../../db/async";
import {Query, QueryData} from "../../models/db/BaseModel";
import AccountDemo from "../../models/db/AccountDemo";

export async function findAccountDemo(domain: string): Promise<AccountDemo> {
  const query: Query<AccountDemo> = new Query(new AccountDemo());
  query.where((model: AccountDemo) => model.domain, "=?");
  const results: any = await getConnectAndQuery(
    query.select(),
    [domain]
  );
  if (results.left == null || results.left.length === 0) {
    return null;
  }
  return new AccountDemo(results.left[0]);
}

export async function updateAccountDemo(
  accountDemo: AccountDemo,
  connection: Connection
): Promise<any> {
  const query: Query<AccountDemo> = new Query<AccountDemo>(accountDemo);
  query.where((model: AccountDemo) => model.id, `=${accountDemo.id.get()}`);
  const queryData: QueryData = query.update();
  const params: any[] = Object.keys(queryData.data).map(
    (key: string) => queryData.data[key]
  );
  await connection.query(queryData.query, params);
  return {};
}
