import {Errors, Kafka, Logger, Models, Utils} from 'tradex-common';
import ILoginRes from "../models/response/ILoginRes";
import IVerifyOtpReq from "../models/request/IVerifyOtpReq";
import UnAuthenticationError from "../errors/UnAuthenticationError";
import {IGenerateTokenParams, generateToken} from "./TokenService";
import IServiceLoginRes, {IUserInfo} from "../models/IServiceLoginRes";
import {scopeService} from "./ScopeService";
import {connectAndDo, Connection, doJobInTransaction} from "../db/async";
import LoginMethodScopeGroupMap from "../models/db/LoginMethodScopeGroupMap";
import {getRefreshTokenById} from "../dao/RefreshTokenDao";
import conf from "../conf";
import RefreshToken from "../models/db/RefreshToken";
import {matchOtp} from './otpService';
import {IInfo} from "../models/db/Otp";
import {WRONG_OTP} from "../constants/errors";
import {registerMobileOtp} from './UserService';
import {findLoginMethodById} from './LoginMethodService';
import NoLoginMethodError from '../errors/NoLoginMethodError';
import Cache from '../utils/Cache';
import LoginMethod from '../models/db/LoginMethod';
import { IMfaUserData } from '../models/mfa/IMfaUserData';
import { versionCompare } from '../utils/utils';
import * as moment from 'moment';

export const loginMethodCache: Cache<LoginMethod> = new Cache();

export declare interface ITokenResult {
  accessToken: string;
  refreshToken: string;
}

async function verifyOtpHtsBridge(request: IVerifyOtpReq, originMsg: Kafka.IMessage, extraData?: string): Promise<ILoginRes> {
  const conId: Models.IConnectionIdentifier = {
    serviceName: request.headers.token.serviceName,
    serviceId: request.headers.token.serviceId,
    connectionId: request.headers.token.connectionId,
  };
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    originMsg.transactionId as string,
    `${request.headers.token.serviceName}.${request.headers.token.serviceId}`,
    "/api/v1/verifyOtp", {
      conId,
      otp_value: request.otp_value,
      macAddress: request.macAddress,
      platform: request.platform,
      osVersion: request.osVersion,
      appVersion: request.appVersion,
      sourceIp: request.sourceIp,
    }, conf.defaultKafkaTimeout);
  if (msg.data.status) {
    throw Errors.createFromStatus(msg.data.status);
  }
  const loginResponse: IServiceLoginRes = msg.data.data;
  return handleCheckOtpResult(`${originMsg.transactionId}`, loginResponse.userInfo, request, conId, extraData);
}

async function verifyOtpLotteRestBridge(request: IVerifyOtpReq, originMsg: Kafka.IMessage, extraData?: string) {
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    originMsg.transactionId as string,
    `lotte-rest-bridge`,
    "/api/v1/verifyOtp", {
      headers: {
        token: request.headers.token
      },
      value: request.otp_value,
      macAddress: request.macAddress,
      platform: request.platform,
      osVersion: request.osVersion,
      appVersion: request.appVersion,
      sourceIp: request.sourceIp,
    }, conf.defaultKafkaTimeout);
  if (msg.data.status) {
    throw Errors.createFromStatus(msg.data.status);
  }
  const mfaData: IMfaUserData = JSON.parse(Utils.rsaDecrypt(request.headers.token.userData.mfaData, conf.rsa.privateKey)) as IMfaUserData;
  return doJobInTransaction<ILoginRes>(async (connection: Connection) => handleCheckOtpResultInTransaction(`${originMsg.transactionId}`, connection, mfaData.userInfo, mfaData.userData, request, extraData));
}

// async function verifyOtpInService(request: IVerifyOtpReq, originMsg: Kafka.IMessage) {
//   const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
//     originMsg.transactionId as string,
//     `lotte-rest-bridge`,
//     "/api/v1/verifyOtp", {
//       headers: {
//         token: request.headers.token
//       },
//       value: request.otp_value,
//       macAddress: request.macAddress,
//       platform: request.platform,
//       osVersion: request.osVersion,
//       appVersion: request.appVersion,
//       sourceIp: request.sourceIp,
//     }, conf.defaultKafkaTimeout);
//   if (msg.data.status) {
//     throw Errors.createFromStatus(msg.data.status);
//   }
//   const mfaData: IMfaUserData = JSON.parse(Utils.rsaDecrypt(request.headers.token.userData.mfaData, conf.rsa.privateKey)) as IMfaUserData;
//   return doJobInTransaction<ILoginRes>(async (connection: Connection) => handleCheckOtpResultInTransaction(`${originMsg.transactionId}`, connection, mfaData.userInfo, mfaData.userData, request));
// }

/* tslint:disable: max-func-body-length */
export async function verifyOtp(request: IVerifyOtpReq, originMsg: Kafka.IMessage): Promise<ILoginRes> {
  if (request?.headers?.token == null) {
    throw new UnAuthenticationError();
  }

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

  if (loginMethod.msName.get() === 'lotte-rest-bridge') {
    return verifyOtpLotteRestBridge(request, originMsg, loginMethod.extraData.get());
  }

  let result: ILoginRes | null = null;
  if (request.headers.token.serviceName === "htsbr") {
    result = await verifyOtpHtsBridge(request, originMsg, loginMethod.extraData.get());
  } else { // normal verify otp flow
    Logger.info(originMsg.transactionId, `___________verify OTP for account: ${request.headers.token.userData.username}, otpValue: ${request.otp_value}`);
    const userInfo: IInfo = matchOtp(request.headers.token.refreshTokenId, request.otp_value, request.mobile_otp, request.headers.token.userData.mfaData);
    result = await doJobInTransaction<ILoginRes>(async (connection: Connection) => {
      if (request.headers.token.platform != null && (request.headers.token.platform.toLowerCase() === 'android' || request.headers.token.platform.toLowerCase() === 'ios')) {
        await registerMobileOtp(request, connection);
      }
      return handleCheckOtpResultInTransaction(`${originMsg.transactionId}`, connection, userInfo.userInfo as IUserInfo, userInfo.userData, request, loginMethod.extraData.get());
    });
  }
  // check mobile app version
  if (conf.checkMobileAppVersion != null && request.headers.token.platform != null && request.headers.token.appVersion != null) {
    const config = conf.checkMobileAppVersion[request.headers.token.platform];
    if (config != null) {
      if (versionCompare(config.version, request.headers.token.appVersion) > 0) {
        const oneSignalConfig: any = {
          contents: {},
          headings: {},
          subtitle: {},
          data: {},
          ios_attachments: {},
          android_group_message: {},
          adm_group_message: {},
          domain: conf.domain,
          url: config.url,
          filters:
            [ { field: 'tag',
              key: 'deviceType',
              relation: '=',
              value: 'mobile' },
              { field: 'tag',
                key: 'username',
                relation: '=',
                value: request.headers.token.userData.username.toLowerCase() } ],
        };
        if (request.headers.token.platform === 'android') {
          oneSignalConfig.isAndroid = true;
        } else if (request.headers.token.platform === 'ios') {
          oneSignalConfig.isIos = true;
        }
        Kafka.getInstance().sendMessage(
          originMsg.transactionId as string,
          "notification",
          "",
          {
            "method": "ONESIGNAL",
            "domain": conf.domain,
            "template": {
              "update_mobile_app": {
                "url": config.url,
              }
            },
            "configuration": JSON.stringify(oneSignalConfig),
          }
        );
      }
    }
  }
  return result;
}
/* tslint:enable: max-func-body-length */

async function handleCheckOtpResultInTransaction(
  txId: string,
  connection: Connection,
  userInfo: IUserInfo,
  userData: Models.IUserData,
  request: IVerifyOtpReq,
  extraData?: string,
): Promise<ILoginRes> {
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
    userData: userData,
    platform: request.headers.token.platform,
    grantType: request.headers.token.grantType,
    osVersion: request.headers.token.osVersion,
    appVersion: request.headers.token.appVersion,
    sessionId: refreshToken.getExtendData().sId,
    request: undefined,
    step: undefined,
    extraData: extraData,
    refExpiredTime: refreshToken.expiredAt.get().getTime(),
    accExpiredTime: Math.min(moment().add(refreshToken.getExtendData().aTtl, "second").toDate().getTime(), refreshToken.expiredAt.get().getTime()),
  };
  const data: ITokenResult = await generateToken(params);
  return {
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    userInfo,
    accExpiredTime: params.accExpiredTime,
    refExpiredTime: params.refExpiredTime,
  };
}

async function handleCheckOtpResult(
  txId: string,
  userInfo: IUserInfo,
  request: IVerifyOtpReq,
  conId: Models.IConnectionIdentifier,
  extraData?: string,
): Promise<ILoginRes> {
  return doJobInTransaction((connection: Connection) =>
    handleCheckOtpResultInTransaction(txId, connection, userInfo, null, request, extraData));
}
