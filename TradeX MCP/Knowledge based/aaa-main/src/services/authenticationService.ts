/* tslint:disable:cyclomatic-complexity */
import { Errors, Kafka, Logger, Utils } from "tradex-common";
import { Connection, doJobInTransaction } from "../db/async";
import { findOrGet } from "./ClientService";
import { findLoginMethods } from "./LoginMethodService";
import MultipleLoginMethodError from "../errors/MultipleLoginMethodError";
import NoLoginMethodError from "../errors/NoLoginMethodError";
import conf from "../conf";
import { scopeService } from "./ScopeService";
import { GrantType } from "../constants/GrantType";
import ILoginReq from "../models/request/ILoginReq";
import Client from "../models/db/Client";
import LoginMethod from "../models/db/LoginMethod";
import ILoginRes from "../models/response/ILoginRes";
import ILoginFacebookReq from "../models/ILoginFacebookReq";
import ILoginTechxReq from "../models/ILoginTechxReq";
import LoginMethodScopeGroupMap from "../models/db/LoginMethodScopeGroupMap";
import ICommonLoginRes from "../models/ICommonLoginRes";
import ILoginGoogleReq from "../models/ILoginGoogleReq";
import loginPasswordCA from "./authen/loginPasswordCA";
import createToken from "./authen/createToken";
import loginClientCredentials from "./authen/loginClientCredential";
import NoGrantTypeError from "../errors/NoGrantTypeError";
import loginAccountDemoService from "./authen/loginAccountDemoService";
import { loginViaThirdParty } from "./authen/commonLoginThirdParty";
import { loginBiometric, loginBiometricOtp } from "./BiometricService";
import { loginPassword } from "./authen/loginPassword";
import { loginPasswordWithOtp } from "./authen/loginPasswordOtp";
import { loginAccessDomain } from "./authen/accessDomain";
import ILoginAppleReq from '../models/ILoginAppleReq';
import Cache from '../utils/Cache';
import ILoginSocialPaaveReq from '../models/ILoginSocialPaaveReq';
import loginLinkAccount from "./authen/loginLinkAccount";
import { versionCompare } from "../utils/utils";
import { loginOrganization } from "./OrganizationService";
import { loginOtp } from "./authen/loginOtp";

const { ForwardError, InvalidIdSecretError, InvalidParameterError, GeneralError } = Errors;
const { validate } = Utils;
const logger = Logger;

const loginMethodCache: Cache<LoginMethod> = new Cache();

/**
 * step 1: check clientid, client secret
 * step 2: check scope if any (for now. no)
 * step 3: select login_methods. then check the grant_type
 * step 4: delegate to correct
 * @param request
 * @returns {*}
 */
export async function authenticate(
  request: ILoginReq,
  msg: Kafka.IMessage
): Promise<ILoginRes> {
  const invalidParams = new InvalidParameterError();
  validate(request.client_id, "client_id")
    .setRequire()
    .throwValid(invalidParams);
  validate(request.client_secret, "client_secret")
    .setRequire()
    .throwValid(invalidParams);
  validate(request.grant_type, "grant_type")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  return authenticateTx(request, msg);
}

async function authenticateTx(
  request: ILoginReq,
  msg: Kafka.IMessage
): Promise<ILoginRes> {
  const invalidParams = new InvalidParameterError();
  const client: Client = await findOrGet(request.client_id);
  if (client == null || client.clientSecret.get() !== request.client_secret) {
    throw new InvalidIdSecretError();
  }
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
  const txId: string = `${msg.transactionId}`;
  const grantType: string = loginMethod.grantType.get();
  if (loginMethod.grantType.get() === GrantType.CLIENT_CREDENTIALS) {
    return loginClientCredentials(request, txId, client, loginMethod);
  } else if (loginMethod.grantType.get() === GrantType.DEMO) {
    return loginAccountDemo(request, txId, client, loginMethod);
  } else if (
    loginMethod.grantType.get() === GrantType.PASSWORD ||
    loginMethod.grantType.get() === GrantType.KB_FINA
  ) {
    return loginPassword(request, txId, client, loginMethod);
  } else if (loginMethod.grantType.get() === GrantType.PASSWORD_OTP) {
    if (conf.enableHandleOtp) {
      return loginPasswordWithOtp(
        msg.transactionId as string,
        client,
        loginMethod,
        request
      );
    } else {
      return loginOtp(
        msg.transactionId as string,
        client,
        loginMethod,
        request
      );
    }
  } else if (grantType === GrantType.PASSWORD_TRADEX) {
    return loginPasswordTechx(request, txId, client, loginMethod);
  } else if (grantType === GrantType.ACCESS_FACEBOOK) {
    return loginFacebook(request, txId, client, loginMethod);
  } else if (grantType === GrantType.ACCESS_GOOGLE) {
    return loginGoogle(request, txId, client, loginMethod);
  } else if (grantType === GrantType.ACCESS_DOMAIN) {
    return loginAccessDomain(request, client, loginMethod, msg);
  } else if (grantType === GrantType.PASSWORD_CA) {
    return loginToPasswordCA(request, client, loginMethod, msg);
  } else if (grantType === GrantType.LOGIN_BIOMETRIC) {
    return loginBiometric(msg.data, msg);
  } else if (grantType === GrantType.ACCESS_APPLE) {
    return loginApple(request, txId, client, loginMethod);
  } else if (
    grantType === GrantType.SOCIAL_LOGIN ||
    grantType === GrantType.ORGANIZATION_SOCIAL_LOGIN
  ) {
    return loginSocialPaave(request, txId, client, loginMethod);
  } else if (grantType === GrantType.LINK_ACCOUNT) {
    return loginLinkAccount(request, txId, client, loginMethod);
  } else if (grantType === GrantType.ORGANIZATION_LOGIN) {
    return loginOrganization(request, txId, client, loginMethod);
  } else if (grantType === GrantType.BIOMETRIC_OTP) {
    return loginBiometricOtp(msg.data, msg);
  }
  throw new NoGrantTypeError();
}

async function loginSocialPaave(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const invalidParams = new InvalidParameterError();
  logger.info('login loginSocialPaave');
  validate(request.login_social_token, 'login_social_token')
    .setRequire()
    .throwValid(invalidParams);
  validate(request.login_social_type, 'login_social_type')
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  if (request.username != null) {
    request.username = request.username.trim();
  }
  const loginData: ILoginSocialPaaveReq = {
    username: request.username,
    socialToken: request.login_social_token,
    socialType: request.login_social_type,
    deviceId: request.device_id,
    systemName: request.sec_code,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
    organization: request.organization
  };
  const uri: string = loginMethod.msUri.getDefault('/api/v1/login');
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    loginMethod.msName.get(),
    uri,
    loginData,
    conf.timeouts.loginTechx
  );

  if (msg.data.status) {
    throw new ForwardError(msg.data.status);
  }

  return loginViaThirdParty(msg.transactionId as string, msg.data.data, request, client, loginMethod);
}

async function loginToPasswordCA(
  request: ILoginReq,
  client: Client,
  loginMethod: LoginMethod,
  msg: Kafka.IMessage
): Promise<ILoginRes> {
  const invalidParams = new InvalidParameterError();
  validate(request.data, "data")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  return loginPasswordCA(request, `${msg.transactionId}`, client, loginMethod);
}

async function loginFacebook(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<any> {
  const invalidParams = new InvalidParameterError();
  logger.info("login with facebook");
  const loginData: ILoginFacebookReq = {
    accessToken: request.access_token,
    device_id: request.device_id,
    socialType: "FACEBOOK",
    password: request.password,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };
  if (loginMethod.serviceCode.get() !== "kbfinance") {
    validate(request.access_token, "access_token")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
  }
  logger.info("send login facebook request");
  const uri: string = loginMethod.msUri.getDefault("/api/v1/authFacebook");
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    loginMethod.msName.get(),
    uri,
    loginData,
    conf.timeouts.loginFacebook
  );
  logger.info("login facebook result", msg);

  if (msg.data.status) {
    throw new ForwardError(msg.data.status);
  }

  return loginViaThirdParty(msg.transactionId as string, msg.data.data, request, client, loginMethod);
}

async function loginGoogle(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const invalidParams = new InvalidParameterError();
  logger.info("login with google");
  if (loginMethod.serviceCode.get() !== "kbfinance") {
    validate(request.access_token, "access_token")
      .setRequire()
      .throwValid(invalidParams);
    validate(request.id_token, "id_token")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
  }
  const loginData: ILoginGoogleReq = {
    accessToken: request.access_token,
    device_id: request.device_id,
    idToken: request.id_token,
    socialType: "GOOGLE",
    password: request.password,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };
  logger.info("send login google request");
  const uri: string = loginMethod.msUri.getDefault("/api/v1/authGoogle");
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    loginMethod.msName.get(),
    uri,
    loginData,
    conf.timeouts.loginGoogle
  );
  logger.info("login google result", msg);

  if (msg.data.status) {
    throw new ForwardError(msg.data.status);
  }

  return loginViaThirdParty(msg.transactionId as string, msg.data.data, request, client, loginMethod);
}

async function loginApple(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  logger.info("login with apple");
  const loginData: ILoginAppleReq = {
    device_id: request.device_id,
    socialType: "APPLE",
    password: request.password,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };
  logger.info("send login apple request");
  const uri: string = loginMethod.msUri.getDefault("/api/v1/authApple");
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    loginMethod.msName.get(),
    uri,
    loginData,
    conf.timeouts.loginApple
  );
  logger.info("login apple result", msg);

  if (msg.data.status) {
    throw new ForwardError(msg.data.status);
  }

  return loginViaThirdParty(msg.transactionId as string, msg.data.data, request, client, loginMethod);
}

async function loginPasswordTechx(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  const invalidParams = new InvalidParameterError();
  logger.info("login loginPasswordTechx");
  validate(request.username, "username")
    .setRequire()
    .throwValid(invalidParams);
  validate(request.password, "password")
    .setRequire()
    .throwValid(invalidParams);
  invalidParams.throwErr();
  request.username = request.username.trim();
  const loginData: ILoginTechxReq = {
    username: request.username,
    password: request.password,
    macAddress: request.macAddress,
    platform: request.platform,
    osVersion: request.osVersion,
    appVersion: request.appVersion,
    sourceIp: request.sourceIp,
  };
  const uri: string = loginMethod.msUri.getDefault("/api/v1/login");
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    txId,
    loginMethod.msName.get(),
    uri,
    loginData,
    conf.timeouts.loginTechx
  );
  return doJobInTransaction((connection: Connection) =>
    executeLoginPasswordTechx(request, client, loginMethod, connection, msg)
  );
}

async function executeLoginPasswordTechx(
  request: ILoginReq,
  client: Client,
  loginMethod: LoginMethod,
  connection: Connection,
  msg: Kafka.IMessage
): Promise<ILoginRes> {
  if (msg.data.status) {
    throw new ForwardError(msg.data.status);
  }
  const loginResult: ICommonLoginRes = msg.data.data;
  const scopeGroups: LoginMethodScopeGroupMap[] = await scopeService.findScopeGroupsAsync(
    loginMethod.id.get(),
    connection
  );
  const scopeGroupIds: number[] = scopeGroups.map(
    (lsg: LoginMethodScopeGroupMap) => lsg.groupId.get()
  );
  return createToken({
    txId: msg.transactionId as string,
    request,
    loginResult,
    clientId: client.id.get(),
    scopeGroupIds,
    userId: loginResult.id,
    loginMethod,
    roles: loginResult.userRoles,
  },
    connection,
  );
}

async function loginAccountDemo(
  request: ILoginReq,
  txId: string,
  client: Client,
  loginMethod: LoginMethod
): Promise<ILoginRes> {
  return loginAccountDemoService(request, txId, client, loginMethod);
}
