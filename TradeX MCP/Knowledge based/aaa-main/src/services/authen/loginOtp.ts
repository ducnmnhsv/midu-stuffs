import * as moment from "moment";
import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import ILoginReq from "../../models/request/ILoginReq";
import ILoginRes from "../../models/response/ILoginRes";
import IServiceLoginReq from "../../models/IServiceLoginReq";
import { Errors, Kafka, Logger, Models, Utils } from "tradex-common";
import conf from "../../conf";
import IServiceLoginRes from "../../models/IServiceLoginRes";
import { scopeService, specialScopes } from "../ScopeService";
import LoginMethodScopeGroupMap from "../../models/db/LoginMethodScopeGroupMap";
import createToken from "./createToken";
import { IGenerateTokenParams, ITokenResult, generateToken } from "../TokenService";
import IVerifyOtpReq from "../../models/request/IVerifyOtpReq";
import { WRONG_OTP } from "../../constants/errors";
import { loginMethodCache } from "../VerifyOtp";
import { Connection, connectAndDo, doJobInTransaction } from "../../db/async";
import { findLoginMethodById } from "../LoginMethodService";
import NoLoginMethodError from "../../errors/NoLoginMethodError";
import { ILoginVerifyOtpReq } from "../../models/ILoginVerifyOtpReq";
import { registerMobileOtp } from "../UserService";
import RefreshToken from "../../models/db/RefreshToken";
import { getRefreshTokenById } from "../../dao/RefreshTokenDao";

const { validate } = Utils;

export const BASE_OTP_URL = "post:/api/v1/login/otp";

export async function loginOtp(
  txId: string,
  client: Client,
  loginMethod: LoginMethod,
  request: ILoginReq
): Promise<ILoginRes> {
  const invalidParams = new Errors.InvalidParameterError();
  validate(request.username, "username")
    .setRequire()
    .throwValid(invalidParams);
  validate(request.password, "password")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  request.username = request.username.trim();
  const loginData: IServiceLoginReq = {
    username: request.username,
    password: request.password,
    systemName: loginMethod.serviceCode.get(),
    headers: {
      token: {
        platform: request.platform,
      },
    },
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };

  const uri: string = loginMethod.msUri.getDefault(BASE_OTP_URL);
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    loginMethod.msName.get(),
    uri,
    loginData,
    conf.timeouts.loginPasswordOtp
  );
  if (msg.data.status) {
    throw new Errors.ForwardError(msg.data.status);
  }

  const loginResult: IServiceLoginRes = msg.data.data;
  return executeLoginWithOtp(txId, client, loginMethod, request, loginResult);
}

export async function loginVerifyOtp(request: IVerifyOtpReq, orgMsg: Kafka.IMessage) {
  const txId: string = orgMsg.transactionId as string;
  if (request.otp_value == null && request.mobile_otp == null) {
    throw new Errors.GeneralError(WRONG_OTP);
  }

  const loginMethod = await loginMethodCache.findOrget(
    `${request.headers.token.loginMethod}`,
    () => connectAndDo((con: Connection) => findLoginMethodById(request.headers.token.loginMethod, con)),
    conf.cacheTimeout
  );
  if (loginMethod == null) {
    throw new NoLoginMethodError();
  }
  let uri: string = loginMethod.msUri.getDefault(BASE_OTP_URL);
  uri = `${uri}/verify`;
  const req: ILoginVerifyOtpReq = {
    headers: request.headers,
    otpKey: request.headers.token.userData.mfaData,
    otpValue: request.otp_value,
  };
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(txId, loginMethod.msName.get(), uri, req, conf.timeouts.loginPasswordOtp);
  if (msg.data.status) {
    throw Errors.createFromStatus(msg.data.status);
  }

  const loginResult: IServiceLoginRes = msg.data.data;
  return await doJobInTransaction<ILoginRes>(async (connection: Connection) => {
    if (request.headers.token.platform != null && (request.headers.token.platform.toLowerCase() === 'android' || request.headers.token.platform.toLowerCase() === 'ios')) {
      await registerMobileOtp(request, connection);
    }

    const scopeGroups: LoginMethodScopeGroupMap[] = await scopeService.findScopeGroupsAsync(request.headers.token.loginMethod, connection);
    const scopeGroupIds: number[] = scopeGroups.map((lsg: LoginMethodScopeGroupMap) => lsg.groupId.get());
    const refreshToken: RefreshToken = await getRefreshTokenById(request.headers.token.refreshTokenId, connection);
    const params: IGenerateTokenParams = {
      txId: txId,
      scopeGroupIds: scopeGroupIds,
      loginMethodId: request.headers.token.loginMethod,
      userId: request.headers.token.userId,
      clientId: request.headers.token.clientId,
      refreshTokenTtl: refreshToken.getExtendData().rTtl,
      accessTokenTtl: refreshToken.getExtendData().aTtl,
      sourceIp: request.sourceIp,
      deviceType: request.deviceType,
      connection: connection,
      roles: [],
      parentId: refreshToken.parentId.get(),
      userData: loginResult.userData,
      platform: request.headers.token.platform,
      grantType: request.headers.token.grantType,
      osVersion: request.headers.token.osVersion,
      appVersion: request.headers.token.appVersion,
      sessionId: refreshToken.getExtendData().sId,
      request: undefined,
      step: undefined,
      extraData: loginMethod.extraData.get(),
      refExpiredTime: refreshToken.expiredAt.get().getTime(),
      accExpiredTime: Math.min(moment().add(refreshToken.getExtendData().aTtl, "second").toDate().getTime(), refreshToken.expiredAt.get().getTime()),
    };
    const data: ITokenResult = await generateToken(params);
    return {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      userInfo: loginResult.userInfo,
      accExpiredTime: params.accExpiredTime,
      refExpiredTime: params.refExpiredTime,
    };
  });
}


/* tslint:disable:max-func-body-length cyclomatic-complexity */
async function executeLoginWithOtp(
  txId: string,
  client: Client,
  loginMethod: LoginMethod,
  request: ILoginReq,
  loginResult: IServiceLoginRes,
): Promise<ILoginRes> {
  Logger.info(`loginPasswordWithOtp result: ${JSON.stringify(loginResult)}`);
  let scopeGroupIds: number[] = specialScopes.verifyOtp
    ? [specialScopes.verifyOtp.id.get()]
    : [];
  const userData: Models.IUserData = loginResult.userData;

  return createToken({
    txId,
    request,
    loginResult,
    clientId: client.id.get(),
    scopeGroupIds,
    userId: request?.headers?.token?.userId,
    loginMethod,
    roles: [],
    parentId: request?.headers?.token?.refreshTokenId,
    userData,
    addedUserInfo: {},
  },
    null,
    (tokenResult: ITokenResult) => ({
      accessToken: tokenResult.accessToken,
      refreshToken: tokenResult.refreshToken,
      otpIndex: loginResult.otpIndex as string,
      userLevel: loginResult.userData.userLevel,
      accExpiredTime: tokenResult.accExpiredTime,
      refExpiredTime: tokenResult.refExpiredTime,
    })
  );
}
