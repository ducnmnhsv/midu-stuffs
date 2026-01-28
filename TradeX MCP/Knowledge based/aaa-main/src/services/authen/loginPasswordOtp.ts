import Client from "../../models/db/Client";
import LoginMethod from "../../models/db/LoginMethod";
import ILoginReq from "../../models/request/ILoginReq";
import ILoginRes, { IUserInfo } from "../../models/response/ILoginRes";
import IServiceLoginReq from "../../models/IServiceLoginReq";
import { Errors, Kafka, Logger, Models, Utils } from "tradex-common";
import conf from "../../conf";
import IServiceLoginRes from "../../models/IServiceLoginRes";
import { scopeService, specialScopes } from "../ScopeService";
import LoginMethodScopeGroupMap from "../../models/db/LoginMethodScopeGroupMap";
import createToken from "./createToken";
import { ITokenResult } from "../TokenService";
import { TYPE } from "../otpService";
import LoginMethodStepScopeGroupMap from "../../models/db/LoginMethodStepScopeGroupMap";
import { findOrCreateServiceUser } from "../UserService";
import ServiceUser from "../../models/db/ServiceUser";
import { rsaPublicKey } from "../../utils/rsa";

const { validate } = Utils;
const STEP = 1;

export async function loginPasswordWithOtp(
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

  const uri: string = loginMethod.msUri.getDefault("/api/v1/loginWithOtp");
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
  return executeLoginWithOtp(txId, client, loginMethod, request, loginResult, loginResult.userInfo);
}


/* tslint:disable:max-func-body-length cyclomatic-complexity */
async function executeLoginWithOtp(
  txId: string,
  client: Client,
  loginMethod: LoginMethod,
  request: ILoginReq,
  loginResult: IServiceLoginRes,
  userInfo: IUserInfo,
): Promise<ILoginRes> {
  Logger.info(`loginPasswordWithOtp result: ${JSON.stringify(loginResult)}`);
  let scopeGroupIds: number[] = specialScopes.verifyOtp
    ? [specialScopes.verifyOtp.id.get()]
    : [];
  const userData: Models.IUserData = loginResult.userData;
  let step: number | undefined = STEP;
  const serviceUser: ServiceUser = await findOrCreateServiceUser(request.username);
  userData.mfaData = Utils.rsaEncrypt(JSON.stringify({
    userInfo: userInfo,
    userData: userData,
    registerMobileOtp: serviceUser.registerMobileOtp.get(),
    otpType: TYPE.LOTTLE,
    // refreshTokenId: string;
    otpValue: loginResult.otpValue,
  }), rsaPublicKey);

  if (loginMethod.msName.get() === "lotte-rest-bridge") {
    loginResult.otpValue = "default";
  } else if (loginMethod.msName.get() === "tuxedo") {
    // if this account doesn't use otp -> return all group, don't need to verify OTP
    if (Utils.isEmpty(loginResult.otpValue)) {
      const scopeGroups: LoginMethodScopeGroupMap[] = await scopeService.findScopeGroupsAsync(
        loginMethod.id.get()
      );
      const tempScopeGroupIds: number[] = scopeGroups.map(
        (lsg: LoginMethodScopeGroupMap) => lsg.groupId.get()
      );
      scopeGroupIds = scopeGroupIds.concat(tempScopeGroupIds);
      step = undefined;
    }
  }

  if (step !== undefined) {
    const scopeGroups: LoginMethodStepScopeGroupMap[] = await scopeService.findScopeGroupStepsAsync(loginMethod.id.get(), step);
    if (scopeGroups.length > 0) {
      scopeGroupIds = [];
      scopeGroups.forEach((sg: LoginMethodStepScopeGroupMap) => {
        scopeGroupIds.push(sg.groupId.get());
      });
    }
  }

  return createToken({
    txId,
    request,
    loginResult,
    clientId: client.id.get(),
    scopeGroupIds,
    userId: (request.headers && request.headers.token) ? request.headers.token.userId : userInfo?.id,
    loginMethod,
    roles: [],
    parentId: request.headers && request.headers.token
      ? request.headers.token.refreshTokenId
      : null,
    userData,
    addedUserInfo: userInfo,
    step,
  },
    null,
    (tokenResult: ITokenResult) => ({
      accessToken: tokenResult.accessToken,
      refreshToken: tokenResult.refreshToken,
      otpIndex: loginResult.otpIndex as string,
      userLevel: loginResult.userData.userLevel,
      registerMobileOtp: serviceUser.registerMobileOtp.get(),
      accExpiredTime: tokenResult.accExpiredTime,
      refExpiredTime: tokenResult.refExpiredTime,
      userInfo: null,
    })
  );
}
