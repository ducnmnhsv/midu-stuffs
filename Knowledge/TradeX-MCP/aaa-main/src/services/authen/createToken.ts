import ILoginReq from "../../models/request/ILoginReq";
import ILoginRes, { IUserInfo } from "../../models/response/ILoginRes";
import IServiceLoginRes from "../../models/IServiceLoginRes";
import ICommonLoginRes from "../../models/ICommonLoginRes";
import LoginMethod from "../../models/db/LoginMethod";
import { Models } from "tradex-common";
import * as moment from "moment";
import { generateToken, ITokenResult } from "../TokenService";
import conf from "../../conf";
import { Connection } from "../../db/async";

// export type ILoginRes = IServiceLoginRes & ICommonLoginRes;

export interface ICreateToken {
  txId: string;
  request: ILoginReq;
  loginMethod: LoginMethod;
  clientId: number;
  scopeGroupIds: number[],
  loginResult?: IServiceLoginRes | ICommonLoginRes;
  userId?: number;
  roles?: string[];
  parentId?: number;
  userData?: Models.IUserData;
  addedUserInfo?: IUserInfo,
  step?: number,
}

export default async function createToken(
  option: ICreateToken,
  connection?: Connection,
  createRespond?: (result: ITokenResult) => ILoginRes,
  createRespondAsync?: (result: ITokenResult) => Promise<ILoginRes>,
): Promise<ILoginRes> {
  let userInfo: any =
    option.loginResult != null && (option.loginResult as IServiceLoginRes).userInfo != null
      ? (option.loginResult as IServiceLoginRes).userInfo
      : (option.loginResult as ICommonLoginRes);
  const sessionId: string =
    option.loginResult != null && (option.loginResult as IServiceLoginRes).sessionId != null
      ? (option.loginResult as IServiceLoginRes).sessionId
      : null;
  const refreshTokenTtl = setRefreshTokenExpireTime(option);
  const accessTokenDefaultExpiredTime = option.loginMethod.accessTokenTtl.get() || conf.accessToken.expiredInSeconds;
  const accessTokenTtl = Math.min(accessTokenDefaultExpiredTime, refreshTokenTtl);
  const refExpiredTime = moment()
    .add(refreshTokenTtl, "second")
    .toDate()
    .getTime();
  const accExpiredTime = moment()
    .add(accessTokenTtl, "second")
    .toDate()
    .getTime();
  const result: ITokenResult = await generateToken({
    txId: option.txId,
    scopeGroupIds: option.scopeGroupIds,
    loginMethodId: option.loginMethod.id.get(),
    userId: option.userId,
    clientId: option.clientId,
    refreshTokenTtl,
    accessTokenTtl,
    connection: connection,
    sourceIp: option.request.sourceIp,
    deviceType: option.request.deviceType,
    roles: option.roles,
    parentId: option.parentId,
    userData: option.userData,
    platform: option.request.platform,
    grantType: option.request.grant_type,
    osVersion: option.request.osVersion,
    appVersion: option.request.appVersion,
    sessionId: sessionId,
    request: option.request,
    step: option.step,
    extraData: option.loginMethod.extraData.get(),
    refExpiredTime,
    accExpiredTime,
  });
  if (createRespond != null) {
    return createRespond(result);
  } else if (createRespondAsync != null) {
    return createRespondAsync(result);
  }
  if (option.addedUserInfo != null) {
    userInfo = { ...option.addedUserInfo, ...userInfo };
  }
  return createILoginRes(result, userInfo, accExpiredTime, refExpiredTime);
}

export function setRefreshTokenExpireTime(option: ICreateToken): number {
  //third party may issue expireTime in their response. if so, refreshToken can only last that long
  const userLoginExpireTime: number =
    option.loginResult != null &&
      (option.loginResult as IServiceLoginRes) != null &&
      (option.loginResult as IServiceLoginRes).userData != null &&
      (option.loginResult as IServiceLoginRes).userData.expireTime != null
      ? Math.floor((option.loginResult as IServiceLoginRes).userData.expireTime)
      : undefined;
  //use when user when to set a certain amount of time logging in
  const userDesiredLoginTime: number =
    (option.request.session_time_in_minute &&
      option.request.session_time_in_minute * 60) ||
    (option.request.sessionTimeInMinute &&
      option.request.sessionTimeInMinute * 60);
  //use when user want to remember me
  const rememberMeLoginTime: number =
    option.loginMethod.refreshTokenLongTtl.get() ||
    conf.refreshToken.expiredInSecondsWithRememberMe;
  //default login time when user not set in request
  const defaultLoginTime: number = option.request.remember_me ? rememberMeLoginTime :
    (option.loginMethod.refreshTokenTtl.get() || conf.refreshToken.expiredInSeconds);
  //min time of all above
  let min: number = userDesiredLoginTime || defaultLoginTime;
  if (userLoginExpireTime && userLoginExpireTime < min) {
    min = userLoginExpireTime;
  }
  if (defaultLoginTime && defaultLoginTime < min) {
    min = defaultLoginTime;
  }
  return min;
}

export function createILoginRes(
  result: ITokenResult,
  userInfo: IUserInfo,
  accExpiredTime: number,
  refExpiredTime: number
): ILoginRes {
  return {
    accessToken: result.accessToken,
    refreshToken: result.refreshToken,
    userInfo,
    accExpiredTime,
    refExpiredTime,
  };
}
