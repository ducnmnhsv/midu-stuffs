import { v4 } from "uuid";
import * as moment from "moment";
import {connectAndDo, Connection, doJobInTransaction} from "../db/async";
import { Errors, Logger, Models, Utils } from "tradex-common";
import { IAccessToken } from "../models/IAccessToken";
import RefreshToken, { extractExtendData } from "../models/db/RefreshToken";
import { Query } from "../models/db/BaseModel";
import * as jwt from "jsonwebtoken";
import conf from "../conf";
import IRefreshTokenReq from "../models/request/IRefreshTokenReq";
import IRefreshTokenRes from "../models/response/IRefreshTokenRes";
import { getClient } from "./ClientService";
import Client from "../models/db/Client";
import { deleteRefreshToken, deleteRefreshTokenById, getRefreshToken } from "../dao/RefreshTokenDao";
import IRevokeTokenReq from "../models/request/IRevokeTokenReq";
import ILoginReq from '../models/request/ILoginReq';
import AccessTokenHistory from "../models/db/AccessTokenHistory";

export declare interface ITokenResult {
  accessToken: string;
  accessTokenData: IAccessToken;
  refreshToken: string;
  accExpiredTime: number;
  refExpiredTime: number;
}

export async function revokeToken(request: IRevokeTokenReq): Promise<any> {
  if(request.refresh_token_id != null){
    return doJobInTransaction(async (connection: Connection) => {
      return deleteRefreshTokenById(request.refresh_token_id, connection);
    });
  }
  const invalidParams = new Errors.InvalidParameterError();
  Utils.validate(request.refresh_token, "refresh_token").setRequire().throwValid(invalidParams);
  invalidParams.throwErr();
  await doJobInTransaction(async (connection: Connection) => {
    return deleteRefreshToken(request.refresh_token, connection);
  });
  return {};
}

export async function refreshAccessToken(request: IRefreshTokenReq): Promise<IRefreshTokenRes> {
  const invalidParams = new Errors.InvalidParameterError();
  Utils.validate(request.client_id, "client_id").setRequire().throwValid(invalidParams);
  Utils.validate(request.client_secret, "client_secret").setRequire().throwValid(invalidParams);
  Utils.validate(request.grant_type, "grant_type").setRequire().throwValid(invalidParams);
  invalidParams.throwErr();
  return doJobInTransaction<IRefreshTokenRes>(async (connection: Connection) => {
    const client: Client = await getClient(request.client_id, connection);
    if (client.clientSecret.get() !== request.client_secret) {
      throw new Errors.InvalidIdSecretError();
    }
    const rf: RefreshToken = await getRefreshToken(request.refresh_token, connection);
    const expiredAt: moment.Moment = moment(rf.expiredAt.get());
    if (moment().isAfter(expiredAt)) {
      throw new Errors.TokenExpiredError();
    }
    const accExpiredTime = moment()
      .add(conf.accessToken.expiredInSeconds, "second")
      .toDate()
      .getTime();
    const accessTokenData: IAccessToken = {
      dm: conf.domain,
      conId: rf.getExtendData().conId,
      cId: rf.clientId.get(),
      sgIds: rf.getExtendData().sgIds,
      lm: rf.loginMethodId.get(),
      suId: rf.serviceUserId.get(),
      uId: rf.userId.get(),
      rId: rf.id.get(),
      sc: rf.getExtendData().sc,
      su: rf.getExtendData().su,
      ud: rf.getExtendData().ud,
      pl: rf.getExtendData().pl,
      gt: rf.getExtendData().gt,
      osV: rf.getExtendData().osV,
      appV: rf.getExtendData().appV,
    };
    await saveAccessTokenHistory(rf, connection);
    return {
      accessToken: signToken(accessTokenData),
      accExpiredTime
    };
  });
}

async function saveAccessTokenHistory(
  refreshToken: RefreshToken,
  connection: Connection
): Promise<void> {
  const now = new Date();
  const accessTokenHistoryEntity: AccessTokenHistory = new AccessTokenHistory();
  accessTokenHistoryEntity.refreshToken.set(refreshToken.token.get());
  accessTokenHistoryEntity.userId.set(refreshToken.userId.get());
  accessTokenHistoryEntity.serviceUserId.set(refreshToken.serviceUserId.get());
  accessTokenHistoryEntity.sourceIp.set(refreshToken.sourceIp.get());
  accessTokenHistoryEntity.loginMethodId.set(refreshToken.loginMethodId.get());
  accessTokenHistoryEntity.deviceType.set(refreshToken.deviceType.get());
  accessTokenHistoryEntity.extendData.set(refreshToken.extendData.get());
  accessTokenHistoryEntity.clientId.set(refreshToken.clientId.get());
  accessTokenHistoryEntity.parentId.set(refreshToken.parentId.get());
  accessTokenHistoryEntity.macAddress.set(refreshToken.macAddress.get());
  accessTokenHistoryEntity.platform.set(refreshToken.platform.get());
  accessTokenHistoryEntity.osVersion.set(refreshToken.osVersion.get());
  accessTokenHistoryEntity.appVersion.set(refreshToken.appVersion.get());
  accessTokenHistoryEntity.createdAt.set(now);
  await connection.query(
    new Query<AccessTokenHistory>(accessTokenHistoryEntity).insert(),
    accessTokenHistoryEntity.getRow());
}

export async function refreshAccessTokenInternal(refreshToken: string): Promise<string> {
  return doJobInTransaction<string>(async (connection: Connection) => {
    const rf: RefreshToken = await getRefreshToken(refreshToken, connection);
    const accessTokenData: IAccessToken = {
      dm: conf.domain,
      conId: rf.getExtendData().conId,
      cId: rf.clientId.get(),
      sgIds: rf.getExtendData().sgIds,
      lm: rf.loginMethodId.get(),
      suId: rf.serviceUserId.get(),
      uId: rf.userId.get(),
      rId: rf.id.get(),
      sc: rf.getExtendData().sc,
      su: rf.getExtendData().su,
      ud: rf.getExtendData().ud,
      pl: rf.getExtendData().pl,
      gt: rf.getExtendData().gt,
      osV: rf.getExtendData().osV,
      appV: rf.getExtendData().appV,
    };

    return signToken(accessTokenData)
  });
}

export interface IGenerateTokenParams {
  txId: string;
  scopeGroupIds: number[];
  loginMethodId: number;
  userId: number;
  clientId: number;
  refreshTokenTtl: number;
  accessTokenTtl: number;
  sourceIp: string;
  deviceType: string;
  connection: Connection | null | undefined;
  roles: string[];
  parentId?: number;
  userData?: Models.IUserData;
  platform?: string;
  grantType?: string;
  osVersion?: string;
  appVersion?: string;
  sessionId?: string;
  request?: ILoginReq;
  step?: number
  extraData?: string;
  refExpiredTime: number;
  accExpiredTime: number;
}

export async function generateToken(params: IGenerateTokenParams): Promise<ITokenResult> {
  let userData: Models.IUserData = params.userData;
  if (params.extraData != null && userData != null) {
    Object.assign(userData, JSON.parse(params.extraData));
  } else if (params.extraData != null) {
    userData = JSON.parse(params.extraData);
  }
  const accessTokenData: IAccessToken = {
    dm: conf.domain,
    cId: params.clientId,
    sgIds: params.scopeGroupIds,
    lm: params.loginMethodId,
    uId: params.userId,
    rId: null,
    ud: userData,
    rls: params.roles,
    pl: params.platform,
    gt: params.grantType,
    osV: params.osVersion,
    appV: params.appVersion,
    sId: params.sessionId,
    step: params.step,
    madr: params.request?.macAddress,
  };
  Logger.info(params.txId, "generate token");
  const refreshToken: RefreshToken = await createRefreshToken({
    ...params,
    accessToken: accessTokenData,
  });
  accessTokenData.rId = refreshToken.id.get();
  const accessToken: string = signToken(accessTokenData, params.accessTokenTtl);
  Logger.warn(params.txId, "generated token {} {} {}", accessTokenData.rId, refreshToken.token.get(), accessToken);
  return {
    accessToken,
    accessTokenData,
    refreshToken: refreshToken.token.get(),
    accExpiredTime: params.accExpiredTime,
    refExpiredTime: params.refExpiredTime,
  };
}

export interface ICreateRefreshToken {
  txId: string;
  clientId: number;
  userId: number;
  loginMethodId: number;
  refreshTokenTtl: number;
  accessTokenTtl: number;
  sourceIp: string;
  deviceType: string;
  accessToken: IAccessToken;
  connection?: Connection;
  parentId?: number;
  request?: ILoginReq;
}

export async function createRefreshToken(params: ICreateRefreshToken): Promise<RefreshToken> {
  Logger.info(params.txId, "create refresh token");
  const refreshToken: string = v4();
  const refreshTokenEntity = new RefreshToken({});
  refreshTokenEntity.token.set(refreshToken);
  refreshTokenEntity.clientId.set(params.clientId);
  refreshTokenEntity.userId.set(params.userId);
  refreshTokenEntity.loginMethodId.set(params.loginMethodId);
  refreshTokenEntity.sourceIp.set(params.sourceIp);
  refreshTokenEntity.deviceType.set(params.deviceType);
  refreshTokenEntity.expiredAt.set(moment().add(params.refreshTokenTtl, "second").toDate());
  refreshTokenEntity.parentId.set(params.parentId);
  refreshTokenEntity.macAddress.set(params.request?.macAddress);
  refreshTokenEntity.platform.set(params.request?.platform);
  refreshTokenEntity.osVersion.set(params.request?.osVersion);
  refreshTokenEntity.appVersion.set(params.request?.appVersion);
  const extraData = extractExtendData(params.accessToken);
  extraData.rTtl = params.refreshTokenTtl;
  extraData.aTtl = params.accessTokenTtl;
  refreshTokenEntity.setExtendData(extraData);
  const results: any = await connectAndDo( (con: Connection) => con.queryResult(new Query(refreshTokenEntity).insert(), refreshTokenEntity.getRow()), params.connection);
  Logger.info(params.txId, "create refresh token result", results);
  refreshTokenEntity.id.set(results.insertId);
  return refreshTokenEntity;
}

export function signToken(accessTokenData: IAccessToken, expiredInSeconds?: number): string {
  return jwt.sign(
    accessTokenData,
    conf.getJwt().privateKey,
    {
      expiresIn: expiredInSeconds || conf.accessToken.expiredInSeconds,
      algorithm: "RS256",
    });
}

export function verifyToken(token: string, publicKey: string, options: object = {}): Promise<IAccessToken> {
  return Utils.promise((resolve: (data: any) => void, reject: (err: Error) => void) => {
    jwt.verify(token, publicKey, options, ((err: Error, decoded: any) => {
      if (err != null) {
        reject(err);
      } else {
        resolve(decoded);
      }
    }));
  });
}
