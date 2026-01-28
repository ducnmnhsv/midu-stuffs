/* tslint:disable:cyclomatic-complexity */
import { Errors, Kafka, Utils, Logger } from 'tradex-common';
import { findOrGet } from "./ClientService";
import { findLoginMethods } from "./LoginMethodService";
import MultipleLoginMethodError from "../errors/MultipleLoginMethodError";
import NoLoginMethodError from "../errors/NoLoginMethodError";
import conf from "../conf";
import ILoginReq from "../models/request/ILoginReq";
import Client from "../models/db/Client";
import LoginMethod from "../models/db/LoginMethod";
import ILoginRes from "../models/response/ILoginRes";
import Cache from '../utils/Cache';
import { IBaseLoginRequest, ILoginPartnerCredentialRequest, ILoginRequest } from '../models/request/ILoginPartnerCredentialRequest';
import { ILoginPartnerCredentialResponse } from '../models/response/ILoginPartnerCredentialResponse';
import Partner from "../models/db/Partner";
import { changeInfoAccessGranted, createLinkAccount, findLinkAccountByPartner, findPartner, queryTradexApi } from "./linkAccountService";
import LinkAccount from "../models/db/LinkAccount";
import { IRegisterAutoUser } from '../models/user/IRegisterAutoUser';
import IServiceLoginRes from "../models/IServiceLoginRes";
import { ILinkAccountRequest } from "../models/request/ILinkAccountRequest";
import { ILinkAccountResponse } from "../models/response/ILinkAccountResponse";
import { ILinkAccountCreatorRequest } from "../models/request/ILinkAccountCreatorRequest";
import { executeLoginDirectToService } from "./authen/loginDirectToService";
import { Connection, doJobInTransaction } from "../db/async";
import { IInternalLoginRequest } from '../models/request/IInternalLoginRequest';
import ISendEmailRequest from '../models/request/ISendEmailRequest';
import IClientDetailResponse from '../models/response/IClientDetailResponse';
import { URL } from 'url';
import { versionCompare } from '../utils/utils';
import IChangeInfoAccessGrantedRequest from '../models/request/IChangeInfoAccessGrantedRequest';

const { InvalidIdSecretError, GeneralError } = Errors;
const { validate } = Utils;

const loginMethodCache: Cache<LoginMethod> = new Cache();

function validateBaseLoginRequest(invalidParams: Errors.InvalidParameterError, request: IBaseLoginRequest, prefix: string) {
  validate(request.clientId, `${prefix}.clientId`)
    .setRequire()
    .throwValid(invalidParams);
  validate(request.clientSecret, `${prefix}.clientSecret`)
    .setRequire()
    .throwValid(invalidParams);
  validate(request.grantType, `${prefix}.grantType`)
    .setRequire()
    .throwValid(invalidParams);
}

function validateLoginRequest(invalidParams: Errors.InvalidParameterError, request: ILoginRequest, prefix: string) {
  validateBaseLoginRequest(invalidParams, request, prefix);
  validate(request.username, `${prefix}.username`)
    .setRequire()
    .throwValid(invalidParams);
  validate(request.password, `${prefix}.password`)
    .setRequire()
    .throwValid(invalidParams);
}

function validatPartnerId(invalidParams: Errors.InvalidParameterError, partnerId: string) {
  validate(partnerId, `partnerId`)
    .setRequire()
    .throwValid(invalidParams);
}

function checkAppVersion(client: Client, request: ILoginPartnerCredentialRequest, invalidParams: Errors.InvalidParameterError) {
  if (client.appVersion.get() != null) {
    validate(request.platform, "platform")
      .setRequire()
      .throwValid(invalidParams);
    validate(request.appVersion, `appVersion`)
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
    const clientAppVersion = JSON.parse(client.appVersion.get())[request.platform.toUpperCase()];
    if (!clientAppVersion) {
      invalidParams.add("INVALID_PLATFORM", "platform", null);
      invalidParams.throwErr();
    }
    const resultCompare = versionCompare(request.appVersion,
      clientAppVersion,
      { zeroExtend: true });
    if (resultCompare < 0) {
      throw new GeneralError("INVALID_APP_VERSION");
    }
  }
}

export async function loginPartnerCredential(
  request: ILoginPartnerCredentialRequest,
  msg: Kafka.IMessage
): Promise<ILoginPartnerCredentialResponse> {
  const invalidParams = new Errors.InvalidParameterError();
  validatPartnerId(invalidParams, request.partnerId);
  validateBaseLoginRequest(invalidParams, request.paave, 'paave');
  validateLoginRequest(invalidParams, request.partner, 'partner');
  invalidParams.throwErr();
  if (request.infoAccessGranted !== true && request.infoAccessGranted !== false && request.infoAccessGranted != null) {
    throw new Errors.InvalidParameterError("infoAccessGranted");
  }
  if(request.infoAccessGranted === false){
    throw new Errors.GeneralError("INFO_ACCESS_NOT_GRANTED");
  }
  const client: Client = await findOrGet(request.paave.clientId);
  if (client == null || client.clientSecret.get() !== request.paave.clientSecret) {
    throw new InvalidIdSecretError();
  }
  checkAppVersion(client, request, invalidParams);
  const loginMethod: LoginMethod = await loginMethodCache.findOrget(`${request.paave.clientId}-${request.paave.grantType}`,
    async () => {
      const methods: LoginMethod[] = await findLoginMethods(
        client.id.get(),
        request.paave.grantType,
        null
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
  const txId: string = `${msg.transactionId}`;
  // verify partner credential
  const partner: Partner = await findPartner(request.partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("PARTNER_DOES_NOT_EXIST");
  }
  const loginRequest: ILoginReq = {
    client_id: conf.enableForcePartnerClientId ? partner.loginClientId.get() : request.partner.clientId,
    client_secret: conf.enableForcePartnerClientId ? partner.loginClientSecret.get() : request.partner.clientSecret,
    grant_type: request.partner.grantType,
    username: request.partner.username,
    password: request.partner.password,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
    headers: request.headers,
    device_id: request.deviceId,
    session_time_in_minute: request.session_time_in_minute || request.sessionTimeInMinute,
  };
  let partnerRes: ILoginRes;
  try {
    partnerRes = await queryTradexApi(partner.loginUrl.get(), {
      method: 'POST',
      body: JSON.stringify(loginRequest),
      headers: {
        "Content-type": "application/json",
        "rid": request.rid || `${msg.transactionId}`
      }
    }, txId) as ILoginRes;
  } catch (e) {
    if (e instanceof Errors.GeneralError) {
      e.code = `LOGIN_PARTNER_ERROR.${e.code}`;
    }
    throw e;
  }

  const linkAccount: LinkAccount = await findLinkAccountByPartner(request.partnerId, null, request.partner.username);
  return loginPartnerCredentialReturn(request, linkAccount, request.infoAccessGranted, partner, client, loginMethod, partnerRes, msg);
}

async function loginPartnerCredentialReturn(
  request: ILoginPartnerCredentialRequest,
  linkAccount: LinkAccount,
  infoAccessGranted: boolean,
  partner: Partner,
  client: Client,
  loginMethod: LoginMethod,
  partnerRes: ILoginRes,
  msg: Kafka.IMessage
): Promise<ILoginPartnerCredentialResponse> {
  if (linkAccount == null) {
    if (infoAccessGranted == null) {
      throw new Errors.GeneralError("INFO_ACCESS_NOT_GRANTED");
    }
    if (infoAccessGranted === true) {
      return loginAndCreateAccount(request, partner, msg, client, loginMethod, partnerRes);
    }
  }
  if (linkAccount.infoAccessGranted.get()) {
    if (infoAccessGranted == null || infoAccessGranted === true) {
      return loginLinkAccount(request, linkAccount, msg, client, loginMethod, partnerRes);
    }
  }
  if (!linkAccount.infoAccessGranted.get()) {
    if (infoAccessGranted == null) {
      throw new Errors.GeneralError("INFO_ACCESS_NOT_GRANTED");
    }
    if (infoAccessGranted === true) {
      return loginLinkAccount(request, linkAccount, msg, client, loginMethod, partnerRes);
    }
  }
  return null;
}

async function loginLinkAccount(
  request: ILoginPartnerCredentialRequest,
  linkAccount: LinkAccount,
  msg: Kafka.IMessage,
  client: Client,
  loginMethod: LoginMethod,
  partnerRes: ILoginRes,
) {
  const req: IInternalLoginRequest = {
    username: linkAccount.username.get(),
    userId: linkAccount.userId.get(),
  };
  const msgResponse: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(`${msg.transactionId}`, "vinance-user", "internal:/api/v1/user/login", req);
  const loginResult: IServiceLoginRes = msgResponse.data.data;
  const loginRes: ILoginRes = await loginInternal(client, loginMethod, request, loginResult, msg);
  if (request.infoAccessGranted === true) {
    const createLinkAccountRequest: IChangeInfoAccessGrantedRequest = {
      userId: linkAccount.userId.get(),
      partnerId: linkAccount.partnerId.get(),
      changeInfoAccessGranted: request.infoAccessGranted,
    };
    await changeInfoAccessGranted(createLinkAccountRequest, `${msg.transactionId}`);
  }
  return {
    paave: loginRes,
    partner: partnerRes,
  };
}

async function loginAndCreateAccount(
  request: ILoginPartnerCredentialRequest,
  partner: Partner,
  msg: Kafka.IMessage,
  client: Client,
  loginMethod: LoginMethod,
  partnerRes: ILoginRes,
): Promise<ILoginPartnerCredentialResponse> {
  Logger.info(`${msg.transactionId}`, "will create account and login");
  const req: IRegisterAutoUser = {
    deviceId: request.deviceId,
  };
  const msgResponse: Kafka.IMessage = await retryable<Kafka.IMessage>(
    `${msg.transactionId}`,
    () => Kafka.getInstance().sendRequestAsync(`${msg.transactionId}`, "vinance-user", "internal:/api/v1/user", req),
    'createAutoUser'
  );
  const loginResult: IServiceLoginRes = msgResponse.data.data;
  const linkAccountRequest: ILinkAccountRequest = {
    partnerId: partner.targetPartnerId.get(),
    partnerUsername: loginResult.userData.username,
  };
  Logger.info(`${msg.transactionId}`, "send init link account");
  const initResponse: ILinkAccountResponse = await retryable<ILinkAccountResponse>(
    `${msg.transactionId}`,
    () => queryTradexApi(
      partner.initLinkUrl.get(), {
      method: 'POST',
      body: JSON.stringify(linkAccountRequest),
      headers: {
        "Content-type": "application/json",
        "Authorization": `jwt ${partnerRes.accessToken}`,
        "rid": request.rid || `${msg.transactionId}`
      }
    },
      `${msg.transactionId}`
    ),
    'tradex api link account'
  );
  const createLinkAccountRequest: ILinkAccountCreatorRequest = {
    authCode: initResponse.authCode,
    optBoard: false,
    partnerId: partner.id.get(),
    partnerUsername: request.partner.username,
    subAccount: null,
    infoAccessGranted: request.infoAccessGranted,
    headers: {
      token: {
        userData: loginResult.userData,
      },
    },
  };
  Logger.info(`${msg.transactionId}`, "send create link account");
  await retryable<object>(
    `${msg.transactionId}`,
    () => createLinkAccount(createLinkAccountRequest, `${msg.transactionId}`),
    'createLinkAccount'
  );
  Logger.info(`${msg.transactionId}`, "get client info");
  const baseUrl = partner.initLinkUrl.get().substring(0, partner.initLinkUrl.get().indexOf('api') - 1)
  const uri = new URL(`${baseUrl}/api/v1/services/eqt/getclientdetail`)
  uri.searchParams.append("clientID", request.partner.username);
  const clientDetailResponse: IClientDetailResponse = await queryTradexApi(uri.href, {
    method: 'GET',
    headers: {
      "Content-type": "application/json",
      "Authorization": `jwt ${partnerRes.accessToken}`,
      "rid": request.rid || `${msg.transactionId}`
    }
  }, `${msg.transactionId}`) as IClientDetailResponse;
  const sendEmailRequest: ISendEmailRequest = {
    to: clientDetailResponse.customerProfile.email,
    emailtemplate: 'welcome_to_paave_kis_acc',
    subject: 'CHÀO MỪNG ĐẾN VỚI PAAVE - WELCOME TO PAAVE',
    fullname: clientDetailResponse.customerProfile.userName
  }
  Logger.info(`${msg.transactionId}`, "update user fullname");
  Kafka.getInstance().sendMessage(
    `${msg.transactionId}`,
    'vinance-user',
    'put:/api/v1/user/updateFullname',
    {
      fullname: clientDetailResponse.customerProfile.userName,
      headers: {
        token: {
          userData: loginResult.userData,
        },
      },
    }
  );
  Logger.info(`${msg.transactionId}`, "send email welcome");
  sendEmail(`${msg.transactionId}`, sendEmailRequest);
  const loginRes: ILoginRes = await loginInternal(client, loginMethod, request, loginResult, msg);
  return {
    paave: loginRes,
    partner: partnerRes,
  };
}

async function loginInternal(
  client: Client,
  loginMethod: LoginMethod,
  request: ILoginPartnerCredentialRequest,
  loginResult: IServiceLoginRes,
  msg: Kafka.IMessage
): Promise<ILoginRes> {
  Logger.info(`${msg.transactionId}`, "login and generate token");
  return doJobInTransaction((con: Connection) => executeLoginDirectToService(msg.transactionId as string, {
    grant_type: request.paave.grantType,
    client_id: request.paave.clientId,
    client_secret: request.paave.clientSecret,
    username: loginResult.userData.username,
    password: '',
    session_time_in_minute: request.session_time_in_minute || request.sessionTimeInMinute,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
    device_id: request.deviceId,
  }, client, loginMethod, con, loginResult, loginResult.userInfo), `${msg.transactionId}`);
}

function sendEmail(transactionId: string, request: ISendEmailRequest) {
  const notificationMessage: any = {
    method: 'EMAIL',
    configuration: JSON.stringify({
      subject: request.subject,
      toList: [request.to],
    }),
    locale: 'en',
    template: {
      'welcome_to_paave_kis_acc': {
        fullname: request.fullname
      }
    }
  }
  Kafka.getInstance().sendMessage(transactionId, 'notification', '', notificationMessage);
}

async function retryable<T>(
  transactionId: string,
  fn: () => Promise<T>,
  functionRun?: string,
  currRetry: number = 1,
  maxTry: number = 3,
): Promise<T> {
  try {
    return (await fn());
  } catch (e) {
    Logger.info(`${transactionId} ${functionRun} Retry ${currRetry} failed.`);
    if (currRetry >= maxTry) {
      Logger.error(`${transactionId} ${functionRun} All ${maxTry} retry attempts exhausted`);
      throw e;
    }
    return retryable(transactionId, fn, functionRun, maxTry, currRetry + 1);
  }
}
