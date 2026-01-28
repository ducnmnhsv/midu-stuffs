import {connectAndDo, Connection, getConnectAndQuery} from "../db/async";
import ObjectNotFoundError, {OBJECT_NOT_FOUND_ERROR_CODE} from "../errors/ObjectNotFoundError";
import {Errors, Logger, Models, Utils} from 'tradex-common';
import {Query, QueryData} from '../models/db/BaseModel';
import ServiceUser from "../models/db/ServiceUser";
import UsernameMapping from '../models/db/UsernameMapping';
import conf from '../conf';
import IUpdateProfileRequest from '../models/request/IUpdateProfileRequest';

export async function findOrCreateServiceUser(username: string, connection?: Connection): Promise<ServiceUser> {
    return connectAndDo((con: Connection) => findServiceUser(username, con), connection)
    .catch((e: any) => {
      if (e.code && e.code === OBJECT_NOT_FOUND_ERROR_CODE) {
        return connectAndDo((con: Connection) => createServiceUser(username, null, con), connection);
      }
      throw e;
    });
}

export async function findServiceUser(username: string, connection: Connection): Promise<ServiceUser> {
  const query: Query<ServiceUser> = new Query<ServiceUser>(new ServiceUser({}));
  query.whereAnd((model: ServiceUser) => model.username, "=?");
  const results: any[] = await connection.queryResult(query.select(), [username]);
  Logger.info("get user service", results);
  if (results == null || results.length === 0) {
    throw new ObjectNotFoundError();
  }
  return new ServiceUser(results[0]);
}

export async function createServiceUser(username: string, createdByUserId: number, connection: Connection): Promise<ServiceUser> {
  const serviceUser: ServiceUser = new ServiceUser({});
  serviceUser.username.set(username);
  serviceUser.avatar.set(conf.defaultAvatar);
  serviceUser.createdBy.set(createdByUserId);
  if (conf.domain === Utils.VCSC_DOMAIN) {
    serviceUser.registerMobileOtp.set(false);
  } else {
    serviceUser.registerMobileOtp.set(false);
  }
  const results: any = await connection.queryResult(new Query(serviceUser).insert(), serviceUser.getRow());
  serviceUser.id.set(results.insertId);
  return serviceUser;
}

export async function createUsernameMapping(username: string, accountNumbers: string[], connection: Connection): Promise<void> {
  await deleteUsernameMappingByUsername(username, connection);
  const usernameMappingList: UsernameMapping[] = [];
  for (let i = 0; i < accountNumbers.length; i++) {
    const usernameMapping: UsernameMapping = new UsernameMapping({});
    usernameMapping.username.set(username);
    usernameMapping.accountNumber.set(accountNumbers[i]);
    usernameMappingList.push(usernameMapping);
  }
  await connection.insertBulk(new Query(new UsernameMapping({})), usernameMappingList);
}

const queryUsernameMappingBy: Query<UsernameMapping> = new Query<UsernameMapping>(new UsernameMapping())
  .where((model: UsernameMapping) => model.username, "=?");

export async function deleteUsernameMappingByUsername(username: string, con: Connection): Promise<any> {
  const results: any[] = await con.queryResult(queryUsernameMappingBy.select(), [username]);
  if (results && results.length > 0) {
    await con.queryResult(queryUsernameMappingBy.delete(), [username]);
  }
}


export async function registerMobileOtp(request: Models.IDataRequest, con?: Connection): Promise<any> {
  const serviceUser: ServiceUser = await connectAndDo((connection: Connection) => findServiceUser(request.headers.token.serviceUsername || request.headers.token.userData.username , connection), con);
  serviceUser.registerMobileOtp.set(true);
  const query: Query<ServiceUser> = new Query<ServiceUser>(serviceUser);
  query.where((model: ServiceUser) => model.id, `=${serviceUser.id.get()}`);
  const queryData: QueryData = query.update();
  const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
  await connectAndDo((connection: Connection) => connection.query(queryData.query, params), con);
  return {};
}

export async function unregisterMobileOtp(request: Models.IDataRequest): Promise<any> {
  const serviceUser: ServiceUser = await connectAndDo((con: Connection) => findServiceUser(request.headers.token.serviceUsername || request.headers.token.userData.username , con));
  serviceUser.registerMobileOtp.set(false);
  const query: Query<ServiceUser> = new Query<ServiceUser>(serviceUser);
  query.where((model: ServiceUser) => model.id, `=${serviceUser.id.get()}`);
  const queryData: QueryData = query.update();
  const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
  await getConnectAndQuery(queryData.query, params);
  return {};
}

export async function updateProfile(request: IUpdateProfileRequest): Promise<any> {
  const serviceUser: ServiceUser = await connectAndDo((con: Connection) =>findServiceUser(request.headers.token.serviceUsername, con));
  if (serviceUser == null) {
    throw new Errors.ObjectNotFoundError();
  }
  serviceUser.avatar.set(request.avatar);
  const query: Query<ServiceUser> = new Query<ServiceUser>(serviceUser);
  query.where((model: ServiceUser) => model.id, `=${serviceUser.id.get()}`);
  const queryData: QueryData = query.update();
  const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
  await getConnectAndQuery(queryData.query, params);
  return {};
}
