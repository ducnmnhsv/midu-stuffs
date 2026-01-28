import { Connection, connectAndDo, getConnectAndQuery } from "../db/async";
import { IInfo } from "../models/db/Otp";
import { Query } from "../models/db/BaseModel";
import {
  Errors,
  Kafka,
  Logger,
  TradexNotification,
  Utils,
} from 'tradex-common';
import * as crypt3 from "apache-crypt";
import { VERIFY_TYPE_NOT_FOUND, WRONG_MOBILE_OTP, WRONG_OTP } from '../constants/errors';
import MobileOtpNotification
  from '../models/notification/MobileOtpNotification';
import { notifyOneSignal, notifyOneSignalNhsv, sendSms } from './NotificationService';
import conf from '../conf';
import INotifyMobileOtpReq from '../models/request/INotifyMobileOtpReq';
import {
  findOrCreateServiceUser,
} from './UserService';
import SmsOtp from '../models/notification/SmsOtp';
import IPhoneNumberReq from '../models/request/IPhoneNumberReq';
import Biometric from "../models/db/Biometric";
import INotifyBiometricMobileOtpReq from "../models/request/INotifyBiometricMobileOtpReq";
import { IGenerateOtpTokenReq } from "../models/request/IGenerateOtpTokenReq";
import * as jwt from "jsonwebtoken";
import { IMfaUserData } from "../models/mfa/IMfaUserData";
import INotifyMobileOtpKisTtlReq from "../models/request/INotifyMobileOtpKisTtlReq";
import { rsaPrivateKey } from "../utils/rsa";
import { loginMethodCache } from "./VerifyOtp";
import { findLoginMethodById } from "./LoginMethodService";
import NoLoginMethodError from "../errors/NoLoginMethodError";
import { BASE_OTP_URL } from "./authen/loginOtp";
import { Messages } from "../constants/messages";
import { GeneralError } from "tradex-common/build/src/modules/errors";
import { Constants } from "../constants/Constants";
import * as _ from "lodash";
import * as moment from "moment";
import * as redis from './redis/Redis';

export const TYPE = {
  LOTTLE: "LOTTLE",
};

export function matchOtp(refreshTokenId: number, otpValue: string, mobileOtp: string, mfaDataKey: string): IInfo {
  if (otpValue == null && mobileOtp == null) {
    throw new Errors.GeneralError(WRONG_OTP);
  }

  const mfaData: IMfaUserData = JSON.parse(Utils.rsaDecrypt(mfaDataKey, rsaPrivateKey)) as IMfaUserData;
  if (mfaData.otpType === TYPE.LOTTLE) {
    if (!Utils.isEmpty(otpValue)) {
      const checksum: string = mfaData.otpValue;
      if (crypt3(`${otpValue}`, checksum) !== checksum) {
        throw new Errors.GeneralError(WRONG_OTP);
      }
    }
    if (!Utils.isEmpty(mobileOtp)) {
      if (mobileOtp !== mfaData.mobileOtpValue) {
        throw new Errors.GeneralError(WRONG_MOBILE_OTP);
      }
    }
    return {
      userInfo: mfaData.userInfo,
      userData: mfaData.userData,
      registerMobileOtp: mfaData.registerMobileOtp,
    };
  } else {
    throw new Errors.GeneralError();
  }
}

export async function notifyMobileBiometricOtp(request: INotifyBiometricMobileOtpReq, transactionId: string): Promise<void> {
  const query: Query<Biometric> = new Query(new Biometric());
  query.where((model: Biometric) => model.id, "=?");
  const results: any = await getConnectAndQuery(query.select(), [request.biometricId]);
  if (results.left.length === 0) {
    throw new Errors.ObjectNotFoundError();
  }
  const biometric = new Biometric(results.left[0]);
  let mobileOtp;
  mobileOtp = decodeOtp(biometric.otpValue.get());
  const mobileOtpNotification: MobileOtpNotification = new MobileOtpNotification();
  mobileOtpNotification.smartOtp = mobileOtp;
  mobileOtpNotification.username = biometric.username.get();
  const extraFilters: TradexNotification.IFilter[] = [];
  const filterByDeviceType: TradexNotification.IFilter = {
    field: 'tag',
    key: 'deviceType',
    relation: '=',
    value: 'mobile',
  };
  const filterByTagMobileOtp: TradexNotification.IFilter = {
    field: 'tag',
    key: 'mobileOTP',
    relation: '!=',
    value: 'false',
  };
  extraFilters.push(filterByDeviceType);
  extraFilters.push(filterByTagMobileOtp);
  notifyOneSignal(mobileOtpNotification, biometric.username.get(), null, conf.domain, extraFilters);
}



export async function notifyMobileOtp(request: INotifyMobileOtpReq, transactionId: string): Promise<void> {
  let mobileOtp: string = "";
  let phoneNumber: string = "";
  if (conf.enableHandleOtp) {
    const mfaData: IMfaUserData = JSON.parse(Utils.rsaDecrypt(request.headers.token.userData.mfaData, rsaPrivateKey)) as IMfaUserData;
    mobileOtp = decodeOtp(mfaData.otpValue);
    const accountNumber = request.headers.token.userData.accountNumbers[0];
    const phoneNumberRequest: IPhoneNumberReq = {
      accountNumber: accountNumber,
      headers: request.headers,
      macAddress: request.macAddress,
      platform: request.platform,
      osVersion: request.osVersion,
      appVersion: request.appVersion,
      sourceIp: request.sourceIp,
    };
    // query tuxedo to get phoneNumber
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      transactionId,
      'tuxedo',
      '/api/v1/equity/account/mobile',
      phoneNumberRequest,
      conf.timeouts.otpService
    );
    if (msg.data.status) {
      throw new Errors.ForwardError(msg.data.status);
    }
    phoneNumber = msg.data.data.phoneNumber;
  } else {
    const loginMethod = await loginMethodCache.findOrget(
      `${request.headers.token.loginMethod}`, 
      () => connectAndDo((con: Connection) => findLoginMethodById(request.headers.token.loginMethod, con)), 
      conf.cacheTimeout
    );
    if (loginMethod == null) {
      throw new NoLoginMethodError();
    }
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      transactionId,
      loginMethod.msName.get(),
      loginMethod.msUri.getDefault(BASE_OTP_URL).replace("post:", "get:"),
      {
        headers: request.headers, 
        otpKey: request.headers.token.userData.mfaData
      },
      conf.timeouts.otpService
    );
    if (msg.data.status) {
      throw new Errors.ForwardError(msg.data.status);
    }
    mobileOtp = msg.data.data.otpValue;
    phoneNumber = msg.data.data.phoneNumber;
  }

  const userLevel: string = request.headers.token.userData.userLevel;
  if (userLevel === 'BROKER' || request.forceSMS !== true) {
    // send oneSignal
    sendOneSignalOtp(mobileOtp, request.headers.token.userData.username)
  } else {
    await sendSmsOtp(mobileOtp, phoneNumber, request.headers['accept-language'])
    // await registerMobileOtp(request)
  }
}

export async function notifyMobileOtpNhsv(
  request: INotifyMobileOtpReq,
  transactionId: string
): Promise<string> {
  let mobileOtp: string = "";
  let phoneNumber: string = "";
  const accountNumber: string = request.headers.token.userData.accountNumbers[0];
  const prefixLog = `ctxId: ${transactionId}, accountNumber: ${accountNumber}`;
  if (_.isEmpty(accountNumber)) {
    throw new GeneralError(`${prefixLog} -- ${Constants.ACCOUNT_NUMBER_IS_EMPTY}`);
  }
  const otpKey: string = request.headers.token.userData.mfaData;
  if (_.isEmpty(otpKey)) {
    throw new GeneralError(`${prefixLog} -- ${Constants.TOKEN_REQUEST_MFA_DATA_FIELD_IS_EMPTY}`);
  }

  const userLevel: string = request.headers.token.userData.userLevel;

  const otpInProcessKey = `${accountNumber}_IN_PROCESSING`;
  let isExistsOtpInProcessKey = false;
  if (request.forceSMS && userLevel !== "BROKER") {
    const otpSentNumberKey = `${moment(new Date()).format('YYYYMMDD')}_${accountNumber}_SENT`;
    Logger.info(`${prefixLog} -- otpSentNumberKey: ${otpSentNumberKey}`);
    const isExistsOtpSentNumberKey = await redis.exists(`${Constants.NHSV_OTP}_${otpSentNumberKey}`);
    Logger.info(`${prefixLog} -- isExistsOtpSentNumberKey = ${isExistsOtpSentNumberKey}`);
    if (isExistsOtpSentNumberKey) {
      const value: number = await redis.get(Constants.NHSV_OTP, otpSentNumberKey, true);
      Logger.info(`${prefixLog} -- otpSentNumber value = ${value}`);
      if (value >= conf.nhsv.MAX_DAILY_SENT) {
        throw new GeneralError(Messages.OTP_LIMIT_GENERATE);
      }
    }

    isExistsOtpInProcessKey = await redis.exists(`${Constants.NHSV_OTP}_${otpInProcessKey}`);
    Logger.info(`${prefixLog} -- isExistsOtpInProcessKey = ${isExistsOtpInProcessKey}`);
    if (isExistsOtpInProcessKey) {
      throw new GeneralError(Messages.OTP_GENERATE_TO_FAST);
    }
  }

  if (conf.enableHandleOtp) {
    return `Not support in case enableHandleOtp is true`;
  } else {
    const loginMethod = await loginMethodCache.findOrget(
      `${request.headers.token.loginMethod}`,
      () =>
        connectAndDo((con: Connection) =>
          findLoginMethodById(request.headers.token.loginMethod, con)
        ),
      conf.cacheTimeout
    );
    if (loginMethod == null) {
      throw new NoLoginMethodError();
    }
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      transactionId,
      loginMethod.msName.get(),
      loginMethod.msUri.getDefault(BASE_OTP_URL).replace("post:", "get:"),
      {
        headers: request.headers,
        otpKey: otpKey,
      },
      conf.timeouts.otpService
    );
    Logger.info(`${prefixLog} -- otpSentNumberKey: ${JSON.stringify(msg)}`);
    if (msg.data.status) {
      throw new Errors.ForwardError(msg.data.status);
    }
    mobileOtp = msg.data.data.otpValue;
    phoneNumber = msg.data.data.phoneNumber;
    if (userLevel === "BROKER" || request.forceSMS !== true) {
      // send oneSignal
      const result = sendOneSignalOtpNhsv(
        mobileOtp,
        request.headers.token.userData.username,
        null,
        accountNumber
      );
      Logger.info(`${prefixLog} -- send oneSignal success: ${JSON.stringify(result)}`);
    } else {
      const result = await sendSmsOtp(
        mobileOtp,
        phoneNumber,
        request.headers["accept-language"]
      );
      Logger.info(`${prefixLog} -- send sms success: ${JSON.stringify(result)}`);

      if (!isExistsOtpInProcessKey) {
        await redis.set(Constants.NHSV_OTP, otpInProcessKey, null, conf.nhsv.TRIGGER_TIME_INTERVAL, true);
      }

      const otpSentNumberKey = `${moment(new Date()).format('YYYYMMDD')}_${accountNumber}_SENT`;
      Logger.info(`${prefixLog} -- otpSentNumberKey: ${otpSentNumberKey}`);
      const isExistsOtpSentNumberKey = await redis.exists(`${Constants.NHSV_OTP}_${otpSentNumberKey}`);
      Logger.info(`${prefixLog} -- isExistsOtpSentNumberKey = ${isExistsOtpSentNumberKey}`);
      if (isExistsOtpSentNumberKey) {
        const value: number = await redis.get(Constants.NHSV_OTP, otpSentNumberKey, true);
        const updateVal = value + 1;
        Logger.info(`${prefixLog} -- otpSentNumber: value = ${value}, updateVal: ${updateVal}`);
        if (value < 5) {
          await redis.set(Constants.NHSV_OTP, otpSentNumberKey, updateVal, conf.nhsv.OTP_SENT_KEY_MAX_LIFE_TIME, true);
        }
      } else {
        await redis.set(Constants.NHSV_OTP, otpSentNumberKey, 1, conf.nhsv.OTP_SENT_KEY_MAX_LIFE_TIME, true);
      }
    }
  }
  return Messages.SUCCESS;
}

export function sendOneSignalOtpNhsv(mobileOtp: string, username: string, userId?: number, accountNumber?: string): void {
  const mobileOtpNotification: MobileOtpNotification = new MobileOtpNotification();
  mobileOtpNotification.smartOtp = mobileOtp;
  mobileOtpNotification.username = username;
  mobileOtpNotification.userId = userId;
  mobileOtpNotification.accountNumber = accountNumber;

  const extraFilters: TradexNotification.IFilter[] = [];

  const filterByAccountNumber: TradexNotification.IFilter = {
    field: 'tag',
    key: 'accountNumber',
    relation: '=',
    value: accountNumber,
  };
  const filterByTagMobileOtp: TradexNotification.IFilter = {
    field: 'tag',
    key: 'mobileOTP',
    relation: '=',
    value: 'true',
  };
  extraFilters.push(filterByAccountNumber);
  extraFilters.push(filterByTagMobileOtp);
  notifyOneSignalNhsv(mobileOtpNotification, conf.domain, extraFilters);
}

export function sendOneSignalOtp(mobileOtp: string, username: string, userId?: number): void {
  const mobileOtpNotification: MobileOtpNotification = new MobileOtpNotification();
  mobileOtpNotification.smartOtp = mobileOtp;
  mobileOtpNotification.username = username;
  mobileOtpNotification.userId = userId;

  const extraFilters: TradexNotification.IFilter[] = [];

  const filterByDeviceType: TradexNotification.IFilter = {
    field: 'tag',
    key: 'deviceType',
    relation: '=',
    value: 'mobile',
  };
  const filterByTagMobileOtp: TradexNotification.IFilter = {
    field: 'tag',
    key: 'mobileOTP',
    relation: '!=',
    value: 'false',
  };
  extraFilters.push(filterByDeviceType);
  extraFilters.push(filterByTagMobileOtp);
  notifyOneSignal(mobileOtpNotification, username, userId, conf.domain, extraFilters);
}

export async function sendSmsOtp(mobileOtp: string, phoneNumber: string, locale: string): Promise<void> {
  const smsOtp: SmsOtp = new SmsOtp();
  smsOtp.otp = mobileOtp;
  sendSms(smsOtp, phoneNumber, conf.domain, locale);
}

export async function notifyMobileOtpKisTtl(request: INotifyMobileOtpKisTtlReq, transactionId: string): Promise<object> {
  //send mas-rest-bridge to get mobileOtp + phoneNumber
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    transactionId,
    'mas-rest-bridge',
    '/api/v1/auth/matrix/getKisCardData',
    {
      matrixId: request.matrixId,
      headers: request.headers,
      macAddress: request.macAddress,
      platform: request.platform,
      osVersion: request.osVersion,
      appVersion: request.appVersion,
      sourceIp: request.sourceIp,
    },
    conf.timeouts.otpService
  );
  if (msg.data.status) {
    throw new Errors.ForwardError(msg.data.status);
  }
  const mobileOtp = msg.data.data.matrixValue
  const phoneNumber = msg.data.data.phoneNumber
  const username =
    request.headers &&
    request.headers.token &&
    request.headers.token.userData &&
    request.headers.token.userData.username;
  const userService = await findOrCreateServiceUser(username);
  if (userService.registerMobileOtp.get() && request.forceSMS !== true) {
    // send oneSignal
    sendOneSignalOtp(mobileOtp, username)
  } else {
    // send SMS
    await sendSmsOtp(mobileOtp, phoneNumber, request.headers['accept-language'])
  }
  return {}
}

export function decodeOtp(checksum: string): string {
  for (let i = 0; i < 9999; i++) {
    const plainText = Utils.leftPad(`${i}`, 4, '0');
    if (crypt3(plainText, checksum) === checksum) {
      return plainText;
    }
  }
  Logger.error(`cannot decode otp: ${checksum}`);
  return null;
}

export async function generateSaveOtpToken(request: IGenerateOtpTokenReq, mesage: Kafka.IMessage) {
  const uri = conf.otp_type_uri_map[conf.domain][request.verifyType];
  if (uri == null || uri === "") {
    throw new Errors.GeneralError(VERIFY_TYPE_NOT_FOUND);
  }
  const txId: string = `${mesage.transactionId}`;
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    conf.otp_type_uri_map[conf.domain].TOPIC[request.verifyType],
    uri,
    { ...request, username: request.clientID || request.headers.token.userData.username.toUpperCase() },
    conf.timeouts.loginDirectToService
  );
  Logger.info("generate token");
  if (msg.data.data == null) {
    throw new Errors.GeneralError(msg.data.status.code);
  }
  const rId = mesage.data.headers.token.refreshTokenId;
  const otpToken = signOtpToken({ rId }, request.expireTime);
  Logger.warn("generated token {}", otpToken);
  return {
    otpToken,
  };
}

function signOtpToken(tokenData: Record<string, string | number>, expiredTime: number): string {
  return jwt.sign(
    tokenData,
    conf.getJwt().privateKey,
    {
      expiresIn: expiredTime == null ? conf.otpToken.expiredInSeconds : `${expiredTime}h`,
      algorithm: "RS256",
    });
}
