import {connectAndDo, Connection, doJobInTransaction, getConnectAndQuery} from "../db/async";
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

/**
 * Lưu device binding cho Smart OTP.
 * Logic: invalidate (status=0) tất cả row ACTIVE cũ của user, sau đó INSERT row mới với status=1.
 * Đảm bảo audit trail đầy đủ trong bảng t_sotp_device.
 */
export async function saveSmartOtpDevice(
  request: { username: string; deviceUniqueId: string },
): Promise<{ success: boolean }> {
  if (!request.username || !request.deviceUniqueId) {
    Logger.warn(`saveSmartOtpDevice -- missing params: username=${request.username}, deviceUniqueId=${request.deviceUniqueId}`);
    return { success: false };
  }

  return doJobInTransaction(async (connection: Connection) => {
    // Step 1: Invalidate tất cả row ACTIVE cũ của user
    await connection.query(
      'UPDATE t_sotp_device SET status = 0, updated_at = CURRENT_TIMESTAMP WHERE username = ? AND status = 1',
      [request.username],
    );

    // Step 2: Insert row mới với status = 1 (ACTIVE)
    await connection.query(
      'INSERT INTO t_sotp_device (username, device_unique_id, status) VALUES (?, ?, 1)',
      [request.username, request.deviceUniqueId],
    );

    Logger.info(`saveSmartOtpDevice -- ${request.username} -> ${request.deviceUniqueId} (invalidated old, inserted new)`);
    return { success: true };
  });
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
