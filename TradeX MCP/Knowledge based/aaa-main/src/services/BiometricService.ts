import { connectAndDo, Connection, getConnectAndQuery } from "../db/async";
import { Query, QueryData } from "../models/db/BaseModel";
import Biometric from "../models/db/Biometric";
import {
  IBiometricRegisterKisRequest,
  IBiometricRegisterRequest,
  validateRegisterKisRequest,
  validateRegisterRequest,
} from "../models/request/IBiometricRegisterRequest";
import { Errors, Kafka, Logger, Models, Utils } from "tradex-common";
import {
  IBiometricLoginRequest,
  validateLoginRequest,
} from "../models/request/IBiometricLoginRequest";
import * as crypto from "crypto";
import {
  BIOMETRIC_KEYS,
  BIOMETRIC_LOGIN_ERROR,
  BIOMETRIC_CANCEL_REASON,
  BIOMETRIC_STATUS,
} from "../constants/biometricKeys";
import loginDirectToService from "./authen/loginDirectToService";
import IServiceLoginReq from "../models/IServiceLoginReq";
import LoginMethod from "../models/db/LoginMethod";
import { findLoginMethods } from "./LoginMethodService";
import Client from "../models/db/Client";
import { getClientById } from "./ClientService";
import NoLoginMethodError from "../errors/NoLoginMethodError";
import MultipleLoginMethodError from "../errors/MultipleLoginMethodError";
import ILoginRes from "../models/response/ILoginRes";
import conf from "../conf";
import IServiceLoginRes from "../models/IServiceLoginRes";
import { IBiometricOTPVerifyRequest } from "../models/request/IBiometricOTPVerifyRequest";
import { IBiometricPasswordVerifyRequest } from "../models/request/IBiometricPasswordVerifyRequest";
import { rsaPublicKey } from "../utils/rsa";
import { IBiometricStatusRequest } from "../models/request/IBiometricStatusRequest";
import * as crypt3 from "apache-crypt";
import { findPartner, queryTradexApi } from "./linkAccountService";
import Partner from "../models/db/Partner";
import { IGenerateOtpTokenReq } from "../models/request/IGenerateOtpTokenReq";
import { URL } from 'url';
import { scopeService, specialScopes } from "./ScopeService";
import LoginMethodScopeGroupMap from "../models/db/LoginMethodScopeGroupMap";
import createToken from "./authen/createToken";
import { ITokenResult } from "./TokenService";
import ILoginReq from "../models/request/ILoginReq";
import { loginMethodCache } from "./VerifyOtp";


const { validate } = Utils;
const { InvalidIdSecretError, InvalidParameterError } = Errors;

async function registerBiometricValidation(
  username: string,
  publicKey: string,
  request?: IBiometricRegisterRequest
) {
  const results: any[] = await connectAndDo((con: Connection) =>
    findBiometricByUsername(username, false, con)
  );
  if (results.length > 0) {
    Logger.info(results[0]);
    if (results[0].publicKey === publicKey) {
      throw new Errors.GeneralError(BIOMETRIC_KEYS.PUBLIC_KEY_EXISTED);
    }
    Logger.info(
      "already exist biometric register for account, start unactivated this biometric "
    );
    await connectAndDo((con: Connection) =>
      updateBiometric(
        results[0],
        con,
        true,
        BIOMETRIC_CANCEL_REASON.CHANGE_DEVICE
      )
    );
    Logger.info("finish update biometric");
  }
  if (conf.rsa.enableEncryptPassword) {
    if (request && request.password) {
      request.password = Utils.rsaEncrypt(request.password, rsaPublicKey);
    }
  }
}


export async function registerBiometricKis(
  request: IBiometricRegisterKisRequest,
  msg: Kafka.IMessage
): Promise<any> {
  const invalidParams = new InvalidParameterError();
  validateRegisterKisRequest(request, invalidParams);
  invalidParams.throwErr();
  const username =
    request.headers.token.serviceUsername != null
      ? request.headers.token.serviceUsername.trim()
      : request.headers.token.userData.username.trim();
  await registerBiometricValidation(username, request.publicKey);
  const client: Client = await getClientById(request.headers.token.clientId);
  const partner: Partner = await findPartner("kis");
  const baseUrl = partner.initLinkUrl.get().substring(0, partner.initLinkUrl.get().indexOf('api') - 1)
  const uri = new URL(`${baseUrl}/api/v1/verifyAndSaveOTP`)
  const requestOTP: IGenerateOtpTokenReq = {
    verifyType: "MATRIX_CARD",
    wordMatrixId: request.wordMatrixId,
    wordMatrixValue: request.wordMatrixValue,
  };
  const response: string = await queryTradexApi<string>(uri.href, {
    method: 'POST',
    body: JSON.stringify(requestOTP),
    headers: {
      "Content-type": "application/json",
      "Authorization": `jwt ${request.kisToken}`,
    }
  }, `${msg.transactionId}`);
  if (response) {
    const serviceBiometric: Biometric = new Biometric({});
    const today = new Date();
    serviceBiometric.publicKey.set(request.publicKey);
    serviceBiometric.username.set(username.toUpperCase());
    serviceBiometric.grantType.set(
      conf.isEnableBiometric ? "biometric" : request.headers.token.grantType
    );
    serviceBiometric.userId.set(request.headers.token.userData.id);
    // serviceBiometric.grantType.set(request.headers.token.grantType);
    serviceBiometric.clientId.set(request.headers.token.clientId);

    serviceBiometric.platform.set(request.headers.token.platform);
    serviceBiometric.osVersion.set(request.headers.token.osVersion);
    serviceBiometric.appVersion.set(request.headers.token.appVersion);
    serviceBiometric.clientSecret.set(client.clientSecret.get());
    serviceBiometric.sourceIp.set(request.sourceIp);
    serviceBiometric.isDeleted.set(false);
    serviceBiometric.createdAt.set(today);
    serviceBiometric.updatedAt.set(today);
    serviceBiometric.biometricType.set(request.biometricType);
    serviceBiometric.status.set(
      conf.isEnableBiometric ? BIOMETRIC_STATUS.ACTIVE : BIOMETRIC_STATUS.INACTIVE
    );
    serviceBiometric.deviceId.set(request.deviceId || request.device_id || "");

    const queryResult = await getConnectAndQuery(
      new Query(serviceBiometric).insert(),
      serviceBiometric.getRow()
    );
    return { index: null, biometricId: queryResult.left.insertId };
  }
}

export async function registerBiometric(
  request: IBiometricRegisterRequest,
  originMsg: Kafka.IMessage
): Promise<any> {
  const invalidParams = new Errors.InvalidParameterError();
  validateRegisterRequest(request, invalidParams);
  invalidParams.throwErr();
  const username =
    request.headers.token.serviceUsername != null
      ? request.headers.token.serviceUsername.trim()
      : request.headers.token.userData.username.trim();
  await registerBiometricValidation(username, request.publicKey, request);
  const client: Client = await getClientById(request.headers.token.clientId);
  const loginData: IServiceLoginReq = {
    username: username,
    password: request.password,
    systemName: request.headers.token.serviceCode,
    device_id: request.deviceId || request.device_id || "",
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };
  const methods: LoginMethod[] = await findLoginMethods(
    client.id.get(),
    request.headers.token.grantType,
    request.headers.token.serviceCode
  );
  if (methods.length === 0) {
    throw new NoLoginMethodError();
  }
  if (methods.length > 1) {
    throw new MultipleLoginMethodError();
  }
  const uri: string = request.headers.token.grantType === "social_login" ?
    "post:/api/v1/user/login" : methods[0].msUri.getDefault("/api/v1/login");
  const txId: string = `${originMsg.transactionId}`;
  let otpIndex: number = null;
  let otpValue: string = null;
  let loginResult: IServiceLoginRes = null;
  if (conf.isRegisBiometricWithPassword) {
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      txId,
      methods[0].msName.get(),
      uri,
      loginData,
      conf.timeouts.loginDirectToService
    );
    if (msg.data.status) {
      if (msg.data.status.code === "INVALID_CLIENT_CREDENTIAL") {
        throw new Errors.GeneralError("INCORRECT_PASSWORD");
      }
      throw new Errors.ForwardError(msg.data.status);
    }
    loginResult = msg.data.data;
  }
  otpIndex = Number.parseInt(`${loginResult.otpIndex}`, 10);
  otpValue = loginResult.otpValue;
  const serviceBiometric: Biometric = new Biometric({});
  const today = new Date();
  serviceBiometric.password.set(request.password);
  serviceBiometric.publicKey.set(request.publicKey);
  serviceBiometric.username.set(
    username.toUpperCase()
  );
  serviceBiometric.grantType.set(
    conf.isEnableBiometric ? "biometric" : request.headers.token.grantType
  );
  // serviceBiometric.grantType.set(request.headers.token.grantType);
  serviceBiometric.clientId.set(request.headers.token.clientId);
  serviceBiometric.userId.set(request.headers.token.userData.id);
  serviceBiometric.platform.set(request.headers.token.platform);
  serviceBiometric.osVersion.set(request.headers.token.osVersion);
  serviceBiometric.appVersion.set(request.headers.token.appVersion);
  serviceBiometric.clientSecret.set(client.clientSecret.get());
  serviceBiometric.sourceIp.set(request.sourceIp);
  serviceBiometric.isDeleted.set(false);
  serviceBiometric.createdAt.set(today);
  serviceBiometric.updatedAt.set(today);
  serviceBiometric.biometricType.set(request.biometricType);
  serviceBiometric.otpIndex.set(isNaN(otpIndex) ? null : otpIndex);
  serviceBiometric.otpValue.set(otpValue);
  serviceBiometric.status.set(
    conf.isEnableBiometric ? BIOMETRIC_STATUS.ACTIVE : BIOMETRIC_STATUS.INACTIVE
  );
  serviceBiometric.deviceId.set(request.deviceId || request.device_id || "");
  const queryResult = await getConnectAndQuery(
    new Query(serviceBiometric).insert(),
    serviceBiometric.getRow()
  );
  return { index: otpIndex, biometricId: queryResult.left.insertId };
}

export async function updateBiometric(
  result: any,
  connection: Connection,
  status: boolean,
  reason: string
): Promise<any> {
  const query: Query<Biometric> = new Query<Biometric>(new Biometric(result));
  query.where((model: Biometric) => model.id, `=${result.id}`);
  const queryData: QueryData = query.update();
  queryData.data.is_deleted = status;
  queryData.data.delete_reason =
    queryData.data.status === BIOMETRIC_STATUS.INACTIVE
      ? BIOMETRIC_CANCEL_REASON.OTP_VERIFY
      : reason;
  queryData.data.updated_at = new Date();
  queryData.data.status = BIOMETRIC_STATUS.INACTIVE;
  const params: any[] = Object.keys(queryData.data).map(
    (key: string) => queryData.data[key]
  );
  await connection.query(queryData.query, params);
  return {};
}

export async function removeBiometric(
  listIdRemove: string[],
  connection: Connection
): Promise<any> {
  const query: Query<Biometric> = new Query<Biometric>(new Biometric({}));
  query.where((model: Biometric) => model.id, `in (${listIdRemove.join(",")})`);
  const queryData = query.delete();
  await connection.query(queryData, null);
  return { message: BIOMETRIC_KEYS.BIOMETRIC_UNREGISTER_SUCCESS };
}

export async function queryBiometricStatus(
  request: IBiometricStatusRequest
): Promise<Record<string, boolean>> {
  const query: Query<Biometric> = new Query<Biometric>(new Biometric({}));
  if (request.publicKey) {
    const username =
      request.headers.token.serviceUsername != null
        ? request.headers.token.serviceUsername.trim()
        : request.headers.token.userData.username.trim();
    const params = [username, false, BIOMETRIC_STATUS.ACTIVE, request.publicKey];
    query
      .whereAnd((model: Biometric) => model.username, "=?")
      .add(query.model.isDeleted, "=?", query.getAlias())
      .add(query.model.status, "=?", query.getAlias())
      .add(query.model.publicKey, "=?", query.getAlias());
    const results = await getConnectAndQuery(query.select(), params);
    return { isEnable: results.left.length === 1 };
  } else {
    const params = [request.userId, request.deviceId, false, BIOMETRIC_STATUS.ACTIVE];
    query
      .whereAnd((model: Biometric) => model.userId, "=?")
      .add(query.model.deviceId, "=?", query.getAlias())
      .add(query.model.isDeleted, "=?", query.getAlias())
      .add(query.model.status, "=?", query.getAlias())
    const results = await getConnectAndQuery(query.select(), params);
    return { isEnable: results.left.length === 1 };
  }
}

export async function verifyBiometricOTP(
  request: IBiometricOTPVerifyRequest
): Promise<any> {
  const username =
    request.headers.token.serviceUsername != null
      ? request.headers.token.serviceUsername.trim()
      : request.headers.token.userData.username.trim();
  const results = await connectAndDo((con: Connection) =>
    findBiometricByUsername(username, false, con)
  );
  if (results == null || results.length === 0) {
    throw new Errors.GeneralError(BIOMETRIC_KEYS.NOT_FOUND);
  }
  const checksum: string = results[results.length - 1].otp_value;
  const encryptedValue = crypt3(`${request.otpValue}`, checksum);
  Logger.info("match biometric otp: original value", request.otpValue, "real OTP: ", checksum, encryptedValue);
  if (encryptedValue === checksum) {
    if (results.length > 1) {
      const listIdRemove: string[] = results.map((obj: any) => obj.id);
      listIdRemove.pop();
      Logger.info(
        `found ${listIdRemove.length} record has duplicated, updating status...`
      );
      await connectAndDo((con: Connection) =>
        removeBiometric(listIdRemove, con)
      );
      Logger.info(`finish update status of duplicate record`);
    }
    const query: Query<Biometric> = new Query<Biometric>(
      new Biometric(results[results.length - 1])
    );
    query.where(
      (model: Biometric) => model.id,
      `=${results[results.length - 1].id}`
    );
    const queryData: QueryData = query.update();
    queryData.data.status = BIOMETRIC_STATUS.ACTIVE;
    queryData.data.updated_at = new Date();
    const params: any[] = Object.keys(queryData.data).map(
      (key: string) => queryData.data[key]
    );
    await getConnectAndQuery(queryData.query, params);
    return {};
  } else {
    throw new Errors.GeneralError(BIOMETRIC_KEYS.OTP_VERIFY_FAILED);
  }
}

export async function verifyPwdBiometric(
  request: IBiometricPasswordVerifyRequest,
  msg: Kafka.IMessage
): Promise<boolean> {
  const username =
    request.headers.token.serviceUsername != null
      ? request.headers.token.serviceUsername.trim()
      : request.headers.token.userData.username.trim();
  const results = await connectAndDo((con: Connection) =>
    findBiometricByUsername(username, false, con)
  );
  if (results == null || results.length === 0) {
    throw new Errors.GeneralError(BIOMETRIC_KEYS.NOT_FOUND);
  }
  if (results.length > 1) {
    const listIdRemove: string[] = results.map((obj: any) => obj.id);
    listIdRemove.pop();
    Logger.info(
      `found ${listIdRemove.length} record has duplicated, updating status...`
    );
    await connectAndDo((con: Connection) =>
      removeBiometric(listIdRemove, con)
    );
    Logger.info(`finish update status of duplicate record`);
  }
  let publicKey = `-----BEGIN PUBLIC KEY-----\n{key}\n-----END PUBLIC KEY-----`;
  const verify = crypto.createVerify("RSA-SHA256");
  publicKey = publicKey.replace(/{key}/g, results[0].public_key);
  verify.update(username.toUpperCase());
  Logger.info(results[0]);
  if (verify.verify(publicKey, request.signature, "base64")) {
    const verifyResult = await Kafka.getInstance().sendRequestAsync(
      `${msg.transactionId}`,
      'kbfinance',
      '/api/v1/kbfina/user/verifyPassword',
      {
        pinCode: results[0].password,
        pinType: "BIOMETRIC",
        headers: request.headers,
        macAddress: request.macAddress,
        platform: request.platform,
        osVersion: request.osVersion,
        appVersion: request.appVersion,
        sourceIp: request.sourceIp,
      },
      conf.defaultKafkaTimeout
    );
    return verifyResult.data.data;
  } else {
    throw new Errors.GeneralError(BIOMETRIC_KEYS.VERIFY_BIOMETRIC_FAILED);
  }
}

export async function cancelBiometricRegister(
  request: Models.IDataRequest
): Promise<any> {
  const username =
    request.headers.token.serviceUsername != null
      ? request.headers.token.serviceUsername.trim()
      : request.headers.token.userData.username.trim();
  const invalidParams = new InvalidParameterError();
  validate(username, "username")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  const results = await connectAndDo((con: Connection) =>
    findBiometricByUsername(username, false, con)
  );
  if (results == null || results.length === 0) {
    throw new Errors.GeneralError(BIOMETRIC_KEYS.NOT_FOUND);
  }
  return connectAndDo((con: Connection) =>
    updateBiometric(results[0], con, true, BIOMETRIC_CANCEL_REASON.CANCEL)
  );
}

async function findBiometricByUsername(
  username: string,
  isDeleted: boolean,
  connection: Connection,
  status?: string
): Promise<any[]> {
  const query: Query<Biometric> = new Query<Biometric>(new Biometric({}));
  const params = [username.toUpperCase(), isDeleted];
  if (status != null && status !== "") {
    query
      .whereAnd((model: Biometric) => model.username, "=?")
      .add(query.model.isDeleted, "=?", query.getAlias())
      .add(query.model.status, "=?", query.getAlias());
    params.push(status);
  } else {
    query
      .whereAnd((model: Biometric) => model.username, "=?")
      .add(query.model.isDeleted, "=?", query.getAlias());
  }
  return connection.queryResult(query.select(), params);
}

async function findBiometricByUsernameAndDeviceId(
  username: string,
  deviceId: string,
  isDeleted: boolean,
  connection: Connection,
  status?: string
): Promise<any[]> {
  const query: Query<Biometric> = new Query<Biometric>(new Biometric({}));
  const params = [username.toUpperCase(), deviceId, isDeleted];
  if (status != null && status !== "") {
    query
      .whereAnd((model: Biometric) => model.username, "=?")
      .add(query.model.deviceId, "=?", query.getAlias())
      .add(query.model.isDeleted, "=?", query.getAlias())
      .add(query.model.status, "=?", query.getAlias());
    params.push(status);
  } else {
    query
      .whereAnd((model: Biometric) => model.username, "=?")
      .add(query.model.deviceId, "=?", query.getAlias())
      .add(query.model.isDeleted, "=?", query.getAlias());
  }
  return connection.queryResult(query.select(), params);
}

async function findBiometricByDeviceId(
  username: string,
  isDeleted: boolean,
  deviceId: string,
  deleteReason: string,
  connection: Connection,
): Promise<any[]> {
  const query: Query<Biometric> = new Query<Biometric>(new Biometric({}));
  const params = [username.toUpperCase(), isDeleted, deviceId, deleteReason];
  query
    .whereAnd((model: Biometric) => model.username, "=?")
    .add(query.model.isDeleted, "=?", query.getAlias())
    .add(query.model.deviceId, "=?", query.getAlias())
    .add(query.model.deleteReason, "=?", query.getAlias());
  return connection.queryResult(query.select(), params);
}


export async function loginBiometric(
  request: IBiometricLoginRequest,
  originMsg: Kafka.IMessage
): Promise<ILoginRes> {
  validateLoginRequest(request);
  request.username = request.username.toUpperCase().trim();
  let publicKey = `-----BEGIN PUBLIC KEY-----\n{key}\n-----END PUBLIC KEY-----`;
  const verify = crypto.createVerify("RSA-SHA256");
  const results: any[] = await connectAndDo((con: Connection) =>
    findBiometricByUsername(request.username, false, con)
  );
  if (results == null || results.length === 0) {
    throw new Errors.GeneralError(BIOMETRIC_KEYS.NOT_FOUND);
  }
  const queryResponse = results[0];
  if (queryResponse.status === BIOMETRIC_STATUS.INACTIVE) {
    await connectAndDo((con: Connection) =>
      updateBiometric(results[0], con, true, BIOMETRIC_CANCEL_REASON.OTP_VERIFY)
    );
    throw new Errors.GeneralError(BIOMETRIC_KEYS.OTP_NOT_VERIFIED);
  }
  publicKey = publicKey.replace(/{key}/g, queryResponse.public_key);
  verify.update(request.username.toUpperCase());
  const signature = request.signatureValue == null || request.signatureValue === '' ? request.password : request.signatureValue;
  if (signature == null || signature === '') {
    throw new InvalidParameterError("INVALID_SIGNAGURE");
  }
  if (!verify.verify(publicKey, signature, "base64")) {
    const biometricAnotherDivice: any[] = await connectAndDo((con: Connection) =>
      findBiometricByDeviceId(request.username, true, request.device_id, BIOMETRIC_CANCEL_REASON.CHANGE_DEVICE, con)
    );
    if (biometricAnotherDivice != null && biometricAnotherDivice.length > 0) {
      throw new Errors.GeneralError(BIOMETRIC_KEYS.BIOMETRIC_ACTIVE_ON_ANOTHER_DEVICE);
    }
    throw new Errors.GeneralError(BIOMETRIC_KEYS.VERIFY_FAILED);
  }
  const client: Client = await getClientById(queryResponse.client_id);
  if (request.client_secret !== queryResponse.client_secret) {
    throw new InvalidIdSecretError();
  }
  if (conf.forceSecCode && !request.sec_code) {
    request.sec_code = conf.forceSecCode;
  }
  const methods: LoginMethod[] = await findLoginMethods(
    client.id.get(),
    queryResponse.grant_type,
    request.sec_code
  );
  if (methods.length === 0) {
    throw new NoLoginMethodError();
  }
  if (methods.length > 1) {
    throw new MultipleLoginMethodError();
  }
  const loginData: IServiceLoginReq = {
    username: request.username,
    password: queryResponse.password,
    systemName: methods[0].serviceCode.get(),
    platform: request.platform,
    device_id: request.device_id
  };
  const uri: string = methods[0].msUri.getDefault("/api/v1/login");
  const txId: string = `${originMsg.transactionId}`;
  try {
    return await loginDirectToService(
      uri,
      loginData,
      {
        client_id: request.client_id,
        client_secret: request.client_secret,
        grant_type: request.grant_type,
        password: queryResponse.password,
        sourceIp: request.sourceIp,
        sec_code: methods[0].serviceCode.get(),
        appVersion: request.appVersion,
        platform: request.platform,
        osVersion: request.osVersion,
        username: request.username,
        headers: request.headers == null ? {} : request.headers,
        macAddress: request.macAddress,
        session_time_in_minute: request.session_time_in_minute,
        sessionTimeInMinute: request.sessionTimeInMinute,
      },
      txId,
      client,
      methods[0]
    );
  } catch (e) {
    if (e.status != null && e.status.code === BIOMETRIC_LOGIN_ERROR[conf.domain]) {
      await connectAndDo((con: Connection) => updateBiometric(queryResponse, con, true, BIOMETRIC_CANCEL_REASON.CHANGE_PASS));
      throw new Errors.GeneralError(BIOMETRIC_KEYS.PASSWORD_NOT_MATCH);
    }
    throw e;
  }
}

export async function loginBiometricOtp(
  request: IBiometricLoginRequest,
  originMsg: Kafka.IMessage
): Promise<ILoginRes> {
  validateLoginRequest(request);
  request.username = request.username.toUpperCase().trim();
  let publicKey = `-----BEGIN PUBLIC KEY-----\n{key}\n-----END PUBLIC KEY-----`;
  const verify = crypto.createVerify("RSA-SHA256");
  const results: any[] = await connectAndDo((con: Connection) =>
    findBiometricByUsernameAndDeviceId(request.username, request.device_id, false, con)
  );
  if (results == null || results.length === 0) {
    throw new Errors.GeneralError(BIOMETRIC_KEYS.NOT_FOUND);
  }
  const queryResponse = results[0];
  if (queryResponse.status === BIOMETRIC_STATUS.INACTIVE) {
    await connectAndDo((con: Connection) =>
      updateBiometric(results[0], con, true, BIOMETRIC_CANCEL_REASON.OTP_VERIFY)
    );
    throw new Errors.GeneralError(BIOMETRIC_KEYS.OTP_NOT_VERIFIED);
  }
  publicKey = publicKey.replace(/{key}/g, queryResponse.public_key);
  verify.update(request.username.toUpperCase());
  const signature = request.signatureValue == null || request.signatureValue === '' ? request.password : request.signatureValue;
  if (signature == null || signature === '') {
    throw new InvalidParameterError().add("FIELD_IS_REQUIRED", "signatureValue", ["signatureValue"]);
  }
  if (!verify.verify(publicKey, signature, "base64")) {
    const biometricAnotherDivice: any[] = await connectAndDo((con: Connection) =>
      findBiometricByDeviceId(request.username, true, request.device_id, BIOMETRIC_CANCEL_REASON.CHANGE_DEVICE, con)
    );
    if (biometricAnotherDivice != null && biometricAnotherDivice.length > 0) {
      throw new Errors.GeneralError(BIOMETRIC_KEYS.BIOMETRIC_ACTIVE_ON_ANOTHER_DEVICE);
    }
    throw new Errors.GeneralError(BIOMETRIC_KEYS.VERIFY_FAILED);
  }
  const client: Client = await getClientById(queryResponse.client_id);
  if (request.client_secret !== queryResponse.client_secret) {
    throw new InvalidIdSecretError();
  }
  if (conf.forceSecCode && !request.sec_code) {
    request.sec_code = conf.forceSecCode;
  }
  const loginMethod: LoginMethod = await loginMethodCache.findOrget(`${request.client_id}-${request.grant_type}`,
    async () => {
      const methods: LoginMethod[] = await findLoginMethods(
        client.id.get(),
        request.grant_type,
        request.sec_code
      );
      if (methods.length === 0) {
        throw new NoLoginMethodError();
      } else if (methods.length > 1) {
        throw new MultipleLoginMethodError();
      } else {
        return methods[0];
      }
    },
    conf.cacheTimeout
  );
  const loginData: IServiceLoginReq = {
    username: request.username.trim(),
    password: queryResponse.password,
    systemName: loginMethod.serviceCode.get(),
    headers: {
      token: {
        platform: request.platform,
      },
    },
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
    device_id: request.device_id
  };
  const uri: string = loginMethod.msUri.getDefault("/api/v1/login");
  const txId: string = `${originMsg.transactionId}`;
  try {
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
    return executeLoginWithOtp(txId, client, loginMethod, request as ILoginReq, loginResult);
  } catch (e) {
    if (e.status != null && e.status.code === BIOMETRIC_LOGIN_ERROR[conf.domain]) {
      await connectAndDo((con: Connection) => updateBiometric(queryResponse, con, true, BIOMETRIC_CANCEL_REASON.CHANGE_PASS));
      throw new Errors.GeneralError(BIOMETRIC_KEYS.PASSWORD_NOT_MATCH);
    }
    throw e;
  }
}

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
  const scopeGroups: LoginMethodScopeGroupMap[] = await scopeService.findScopeGroupsAsync(
    loginMethod.id.get()
  );
  const tempScopeGroupIds: number[] = scopeGroups.map(
    (lsg: LoginMethodScopeGroupMap) => lsg.groupId.get()
  );
  scopeGroupIds = scopeGroupIds.concat(tempScopeGroupIds);

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