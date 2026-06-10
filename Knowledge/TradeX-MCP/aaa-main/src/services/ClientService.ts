import { Errors, Kafka, Models } from "tradex-common";
import { Query, QueryData } from "../models/db/BaseModel";
import Client from "../models/db/Client";
import { Connection, getConnectAndQuery } from "../db/async";
import Cache from '../utils/Cache';
import { FieldInfo } from "mysql";
import conf from "../conf";
import { IChangeClientSecret } from "../models/request/IChangeClientSecret";
import RefreshToken from "../models/db/RefreshToken";
import { IUpdateAppVersionRequest } from "../models/request/IUpdateAppVersionRequest";
import { IClient } from "../models/request/IClient";

export const clientCache: Cache<Client> = new Cache();

export function findOrGet(clientId: string): Promise<Client> {
  return clientCache.findOrget(clientId, getClientAndRelease, conf.cacheTimeout);
}


export async function getClient(clientId: string, connection: Connection): Promise<Client> {
  const queryBuilder: Query<Client> = new Query<Client>(new Client({}));
  queryBuilder.where((model: Client) => model.clientId, "= ?");
  const result: Models.Pair<any[], FieldInfo[]> = await connection.query(queryBuilder.select(), [clientId]);
  if (result.left == null || result.left.length === 0) {
    throw new Errors.InvalidIdSecretError();
  }
  return new Client(result.left[0]);
}

export async function getClientAndRelease(clientId: string): Promise<Client> {
  const queryBuilder: Query<Client> = new Query<Client>(new Client({}));
  queryBuilder.where((model: Client) => model.clientId, "= ?");
  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), [clientId]);
  if (result.left == null || result.left.length === 0) {
    throw new Errors.InvalidIdSecretError();
  }
  return new Client(result.left[0]);
}

export async function getClientById(clientId: number): Promise<Client> {
  const queryBuilder: Query<Client> = new Query<Client>(new Client({}));
  queryBuilder.where((model: Client) => model.id, '= ?');
  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(
    queryBuilder.select(),
    [clientId],
  );
  if (result.left == null || result.left.length === 0) {
    throw new Errors.InvalidIdSecretError();
  }
  //tslint:disable
  console.log(result.left);
  return new Client(result.left[0]);
}

export async function changeClientSecret(request: IChangeClientSecret): Promise<object> {
  // send request to configuration
  const client = await getClientAndRelease(request.clientId);
  await Kafka.getInstance().sendRequestAsync(`${new Date().getTime()}`, 'configuration', 'put:/api/v1/client/{id}/changeSecret', request);
  client.clientSecret.set(request.clientSecret);
  const queryBuilder: Query<Client> = new Query<Client>(new Client(client.getRow()));
  queryBuilder.where((model: Client) => model.clientId, `='${request.clientId}'`);
  const queryData: QueryData = queryBuilder.update();
  const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
  await getConnectAndQuery(queryData.query, params);
  if (request.logoutAll) {
    const deleteRTQuery: Query<RefreshToken> = new Query<RefreshToken>(new RefreshToken({}));
    deleteRTQuery.where((model: RefreshToken) => model.clientId, `=?`);
    await getConnectAndQuery(deleteRTQuery.delete(), [client.id.get()]);
  }
  return {};
}

export async function updateAppVersion(request: IUpdateAppVersionRequest): Promise<object> {
  // send request to configuration
  let clients = request.clientIds ? request.clientIds : request.paaveClientId;
  clients = clients.includes(',') ? clients.split(',') : clients;
  clients = Array.isArray(clients) ? clients : [clients];
  // update db appVersion
  clients.forEach(async (id: string) => {
    const client = await getClientAndRelease(id);
    let android = request.appVersion.listAndroid;
    android = android.includes(',') ? android.split(',') : android;
    android = Array.isArray(android) ? android : [android];
    let ios = request.appVersion.listIos;
    ios = ios.includes(',') ? ios.split(',') : ios;
    ios = Array.isArray(ios) ? ios : [ios];
    let appVersion = {};
    android.forEach((platform: string) => {
      appVersion[platform] = request.appVersion.android;
    });
    ios.forEach((platform: string) => {
      appVersion[platform] = request.appVersion.ios;
    });
    const req: IClient = {
      id: client.id.get(),
      appVersion: JSON.stringify(appVersion),
    };
    await Kafka.getInstance().sendRequestAsync(`${new Date().getTime()}`, 'configuration', '/api/v1/client/{id}/update', req);
    const queryBuilder: Query<Client> = new Query<Client>(new Client(client.getRow()));
    queryBuilder.where((model: Client) => model.clientId, `='${id}'`);
    const queryData: QueryData = queryBuilder.update();
    queryData.data.app_version = JSON.stringify(appVersion);
    const params: any[] = Object.keys(queryData.data).map(
      (key: string) => queryData.data[key]
    );
    await getConnectAndQuery(queryData.query, params);
  });

  return {};
}