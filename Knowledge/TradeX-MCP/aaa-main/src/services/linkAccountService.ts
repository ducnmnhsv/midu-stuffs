import { Errors, Kafka, Logger, Models, Utils } from 'tradex-common';
import fetch, { RequestInit, Response } from "node-fetch";
import { ILinkAccountRequest, validateLinkAccountRequest } from "../models/request/ILinkAccountRequest";
import { ILinkAccountResponse } from "../models/response/ILinkAccountResponse";
import LinkAccount from '../models/db/LinkAccount';
import { AndCondition, OrCondition, Query, QueryData } from '../models/db/BaseModel';
import { FieldInfo } from 'mysql';
import { Connection, doJobInTransaction, getConnectAndQuery } from '../db/async';
import LinkAccountDraft from '../models/db/LinkAccountDraft';
import * as uuid from 'uuid';
import Partner from '../models/db/Partner';
import { ILinkAccountConfirmRequest, validateLinkAccountConfirmRequest } from '../models/request/ILinkAccountConfirmRequest';
import { ILinkAccountCreatorRequest } from '../models/request/ILinkAccountCreatorRequest';
import { sign, verifySign } from '../utils/sign';
import { ILinkAccountLoginRequest } from '../models/request/ILinkAccountLoginRequest';
import ILoginReq from '../models/request/ILoginReq';
import { GrantType } from '../constants/GrantType';
import { IListPartnerResponse } from '../models/response/IListPartnerResponse';
import { ILinkAccountListResponse } from '../models/response/ILinkAccountListResponse';
import { ILinkAccountDeleteRequest } from '../models/request/ILinkAccountDeleteRequest';
import { ILinkAccountUnlinkRequest } from '../models/request/ILinkAccountUnlinkRequest';
import conf from '../conf';
import { INotifyOtpPartner } from '../models/request/INotifyOtpPartner';
import INotifyOtpFromPartnerReq from '../models/request/INotifyOtpFromPartnerReq';
import { INotifyOtpFromPartnerRes } from '../models/response/INotifyOtpFromPartnerRes';
import { sendOneSignalOtp, sendSmsOtp } from './otpService';
import IChangeUserNameReq from '../models/request/IChangeUserNameReq';
import LinkAccountHistory from '../models/db/LinkAccountHistory';
import IInternalLeaderboardSettingRequest from '../models/request/IInternalLeaderboardSettingRequest';
import IChangeInfoAccessGrantedRequest from '../models/request/IChangeInfoAccessGrantedRequest';
import { ILinkAccountApproveRequest, validateLinkAccountApproveRequest } from "../models/request/ILinkAccountApproveRequest";
import ILoginRes from '../models/response/ILoginRes';
import { IGetUserIdFromPartnerResponse } from '../models/response/IGetUserIdFromPartnerResponse';

/*
* this service is to link between account of source system to account of destination system
*/

/*
* client -> destination system to get authCode
*/
export async function initLinkAccount(request: ILinkAccountRequest): Promise<ILinkAccountResponse> {
  const invalidParams = new Errors.InvalidParameterError();
  validateLinkAccountRequest(request, invalidParams);
  invalidParams.throwErr();
  // const client: Client = await findOrGet(request.headers!.token!.clientId!);
  // if (client == null) {
  //   throw new InvalidFieldValueError("clientId", request.clientId);
  // }
  const username = request.headers.token.userData.username;
  const userId: number | undefined = (request.headers.token.userData as any).id;
  const linkAccount: LinkAccount = await findCurrentLinkAccount(request.partnerId, username, userId);
  if (linkAccount != null) {
    throw new Errors.GeneralError("LINK_ACCOUNT_ALREADY_EXISTED");
  }
  const partner: Partner = await findPartner(request.partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("PARTNER_NOT_FOUND");
  }

  const draft = new LinkAccountDraft({});
  draft.authCode.set(uuid());
  const expirationTime = new Date(new Date().getTime() + 300000);
  draft.expirationAt.set(expirationTime);
  draft.request.set(JSON.stringify(request));
  draft.username.set(username);
  draft.partnerId.set(request.partnerId);
  draft.partnerUsername.set(request.partnerUsername);
  if (request.partnerUserId != null) {
    draft.partnerUserId.set(request.partnerUserId);
  }
  const saveQuery: Query<LinkAccountDraft> = new Query<LinkAccountDraft>(new LinkAccountDraft({}))
  await getConnectAndQuery(saveQuery.insert(), draft.getRow());
  return {
    authCode: draft.authCode.get(),
  };
}

/*
* client -> source system to get token of destination system
*/
export async function loginLinkAccount(request: ILinkAccountLoginRequest, msgId: string): Promise<ILoginRes> {
  const username = request.headers.token.userData.username;
  const userId: number | undefined = (request.headers.token.userData as any).id;
  const infoAccessGranted = request.infoAccessGranted;
  if (infoAccessGranted !== true && infoAccessGranted !== false && infoAccessGranted != null) {
    throw new Errors.InvalidParameterError("infoAccessGranted");
  }
  const partner: Partner = await findPartner(request.partnerId);
  const linkAccount: LinkAccount = await findCurrentLinkAccount(request.partnerId, username, userId);
  if (linkAccount == null) {
    throw new Errors.GeneralError("LINK_ACCOUNT_NOT_FOUND");
  }
  const loginParams: ILoginReq = {
    grant_type: GrantType.LINK_ACCOUNT,
    client_id: partner.loginClientId.get(),
    client_secret: partner.loginClientSecret.get(),
    username: `${partner.targetPartnerId.get()}:${linkAccount.partnerUsername.get()}`,
    password: sign(partner.privateKey.get(), linkAccount.partnerUsername.get()),
    platform: request.headers.token.platform,
    osVersion: request.headers.token.osVersion,
    appVersion: request.headers.token.appVersion,
    sourceIp: request.sourceIp,
    session_time_in_minute: request.session_time_in_minute || request.sessionTimeInMinute,
    // device_id: request., we can get from refresh token but later
  };
  const createLinkAccountReques: IChangeInfoAccessGrantedRequest = {
    userId: linkAccount.userId.get(),
    partnerId: linkAccount.partnerId.get(),
    changeInfoAccessGranted: infoAccessGranted,
  };
  if ((infoAccessGranted == null && !linkAccount.infoAccessGranted.get()) || infoAccessGranted === false) {
    throw new Errors.GeneralError("INFO_ACCESS_NOT_GRANTED");
  }
  if (infoAccessGranted === true && !linkAccount.infoAccessGranted.get()) {
    await changeInfoAccessGranted(createLinkAccountReques, msgId);
  }
  return queryTradexApi<ILoginRes>(partner.loginUrl.get(), {
    method: 'POST',
    body: JSON.stringify(loginParams),
    headers: {
      "Content-type": "application/json",
      "rid": request.rid || `${msgId}`
    }
  }, msgId);
}

/*
* client -> source system: to get all partner that can integrated
*/
export async function findAllPartners(): Promise<IListPartnerResponse[]> {
  const results: Models.Pair<any, FieldInfo[]> = await getConnectAndQuery(new Query<Partner>(new Partner({})).select(), []);
  return (results.left as any[]).map((it: any) => {
    const partner: Partner = new Partner(it);
    return {
      partnerId: partner.id.get(),
      targetPartnerId: partner.targetPartnerId.get(),
      name: partner.name.get(),
      description: partner.description.get(),
      iconUrl: partner.iconUrl.get(),
    };
  });
}

/*
* client -> source to get all linked accounts
*/
export async function findAllLinkAccount(request: Models.IDataRequest): Promise<ILinkAccountListResponse[]> {
  const query: Query<LinkAccount> = new Query(new LinkAccount({}));
  const username = request.headers.token.userData.username;
  const userId = (request.headers.token.userData as any).id;
  const params = [];
  if (userId != null) {
    query.where((model: LinkAccount) => model.userId, "= ?");
    params.push(userId);
  } else {
    query.where((model: LinkAccount) => model.username, "= ?");
    params.push(username);
  }

  const results: Models.Pair<any, FieldInfo[]> = await getConnectAndQuery(query.select(), params);
  return (results.left as any[]).map((it: any) => {
    const linkAccount: LinkAccount = new LinkAccount(it);
    return {
      partnerId: linkAccount.partnerId.get(),
      partnerUsername: linkAccount.partnerUsername.get(),
    };
  });
}

/**
 * client -> src
 */
export async function notifyOtpPartner(request: INotifyOtpPartner, txid: string): Promise<object> {
  const partner: Partner = await findPartner(request.partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("PARTNER_NOT_FOUND");
  }
  const username = request.headers.token.userData.username;
  const userId = (request.headers.token.userData as any).id;
  const linkAccount = await findCurrentLinkAccount(request.partnerId, username, userId);
  if (linkAccount == null) {
    throw new Errors.GeneralError("LINK_ACCOUNT_NOT_FOUND");
  }
  const partnerUsername = linkAccount.partnerUsername.get();
  const signData = sign(partner.privateKey.get(), `${request.matrixId}_${partnerUsername}_${request.partnerId}`);
  const noty: INotifyOtpFromPartnerReq = {
    matrixId: request.matrixId,
    partnerId: partner.targetPartnerId.get(),
    sign: signData,
    username: partnerUsername,
    registerMobileOtp: !request.forceSMS,
  };
  const response: INotifyOtpFromPartnerRes = await queryTradexApi<INotifyOtpFromPartnerRes>(partner.notyOtpUrl.get(), {
    method: 'POST',
    body: JSON.stringify(noty),
    headers: {
      "Content-type": "application/json",
      "rid": request.rid || `${txid}`
    }
  }, txid);
  if (!request.forceSMS) {
    sendOneSignalOtp(response.otp, username, userId)
  }
  return {};
}

/**
 * src -> dst
 */

export async function changeUsername(request: IChangeUserNameReq): Promise<object> {
  const linkAccount: LinkAccount = await findLinkAccount(request.partnerId, request.oldUsername, request.userId);
  if (linkAccount) {
    const query: Query<LinkAccount> = new Query<LinkAccount>(linkAccount);
    query.where((model: LinkAccount) => model.id, `=${linkAccount.id.get()}`);
    const queryData: QueryData = query.update();
    queryData.data.username = request.newUsername;
    const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
    await getConnectAndQuery(queryData.query, params);
  }
  return {}
}

export async function changeInfoAccessGranted(request: IChangeInfoAccessGrantedRequest, txid: string): Promise<object> {
  Logger.info(txid, " send update link account");
  const linkAccount: LinkAccount = await findLinkAccount(request.partnerId, null, request.userId);
  if (linkAccount) {
    const query: Query<LinkAccount> = new Query<LinkAccount>(linkAccount);
    query.where((model: LinkAccount) => model.id, `=${linkAccount.id.get()}`);
    const queryData: QueryData = query.update();
    queryData.data.info_access_granted = request.changeInfoAccessGranted;
    const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
    await getConnectAndQuery(queryData.query, params);
  }
  return {}
}

export async function notifyOtpFromPartner(request: INotifyOtpFromPartnerReq, transactionId: string): Promise<INotifyOtpFromPartnerRes> {
  const partner: Partner = await findPartner(request.partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("PARTNER_NOT_FOUND");
  }
  if (!verifySign(partner.publicKey.get(), `${request.matrixId}_${request.username}_${partner.targetPartnerId.get()}`, request.sign)) {
    throw new Errors.GeneralError("INVALID_SIGNATURE");
  }
  const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
    transactionId,
    'mas-rest-bridge',
    '/api/v1/auth/matrix/getKisPaaveCardData',
    {
      matrixId: request.matrixId,
      username: request.username,
      headers: request.headers,
    },
    conf.timeouts.otpService
  );
  if (msg.data.status) {
    throw new Errors.ForwardError(msg.data.status);
  }
  const mobileOtp = msg.data.data.matrixValue
  const phoneNumber = msg.data.data.phoneNumber
  if (!request.registerMobileOtp) {
    await sendSmsOtp(mobileOtp, phoneNumber, request.headers['accept-language'])
  }
  return {
    otp: mobileOtp,
  }
}


/*
* client -> source: to link source's account to destination's account
*/
export async function createLinkAccount(request: ILinkAccountCreatorRequest, msgId: string): Promise<object> {
  if (request.infoAccessGranted == null) {
    request.infoAccessGranted = true;
  }
  if (request.infoAccessGranted !== true && request.infoAccessGranted !== false) {
    throw new Errors.InvalidParameterError("infoAccessGranted");
  }
  if (request.infoAccessGranted === false) {
    throw new Errors.GeneralError("INFO_ACCESS_NOT_GRANTED");
  }
  const partner: Partner = await findPartner(request.partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("PARTNER_NOT_FOUND");
  }
  const username = request.headers.token.userData.username;
  const userId = (request.headers.token.userData as any).id;
  const linkAccounts: LinkAccount[] = await findLinkAccounts(request.partnerId, username, userId, request.partnerUsername);
  if (linkAccounts != null && linkAccounts.length > 0) {
    throw new Errors.GeneralError("LINK_ACCOUNT_ALREADY_EXISTED");
  }
  const signData = sign(partner.privateKey.get(), `${request.authCode}_${partner.targetPartnerId.get()}_${request.partnerUsername}_${username}`);
  const confirmRequest: ILinkAccountConfirmRequest = {
    partnerId: partner.targetPartnerId.get(),
    authCode: request.authCode,
    userId: userId,
    username: request.partnerUsername,
    partnerUsername: username,
    sign: signData,
  };
  const optBoard: boolean = request.optBoard == null ? false : request.optBoard;
  const entity: LinkAccount = new LinkAccount({});
  entity.partnerId.set(partner.id.get());
  entity.partnerUsername.set(request.partnerUsername);
  entity.username.set(username);
  entity.userId.set(userId);
  entity.joinLeaderboard.set(optBoard);
  if (optBoard) {
    entity.subAccount.set(request.subAccount);
  }
  if (request.infoAccessGranted === true) {
    entity.infoAccessGranted.set(true);
  }
  const data: any = {
    type: "ADD",
    id: 0,
    partnerId: partner.id.get(),
    partnerUsername: request.partnerUsername,
    userId: userId,
    username: username,
    joinLeaderboard: optBoard,
    subAccount: request.subAccount,
    createdAt: Utils.formatDateToDisplay(new Date(), Utils.DATETIME_DISPLAY_FORMAT),
  };
  await doJobInTransaction(async (con: Connection) => {
    const result: Models.Pair<any, FieldInfo[]> = await con.query(new Query<LinkAccount>(new LinkAccount({})).insert(), entity.getRow());
    data.id = result.left.insertId;
    await queryTradexApi(partner.confirmLinkUrl.get(), {
      method: 'POST',
      body: JSON.stringify(confirmRequest),
      headers: {
        "Content-type": "application/json",
        "rid": request.rid || `${msgId}`
      }
    }, msgId);
  }, msgId);
  await retryable<object>(msgId, () => sendMessageSyncLinkAccount(msgId, "ADD", data, "paave-real-trading"));

  const linkAccount: LinkAccount = await findCurrentLinkAccount(partner.id.get(), username, userId);
  if (linkAccount == null) {
    throw new Errors.GeneralError("LINK_ACCOUNT_NOT_FOUND");
  }
  await loginAndUpdateLinkAccount(partner, linkAccount, request, msgId)

  return {
    message: "LINK_ACCOUNT_SUCCESS",
    optBoard: optBoard
  };
}

async function loginAndUpdateLinkAccount(partner: Partner, linkAccount: LinkAccount, request: ILinkAccountCreatorRequest, msgId: string): Promise<void> {
  const loginParams: ILoginReq = createLoginParams(partner, linkAccount, request);

  let partnerRes: ILoginRes;
  try {
    partnerRes = await queryTradexApi(partner.loginUrl.get(), {
      method: 'POST',
      body: JSON.stringify(loginParams),
      headers: {
        "Content-type": "application/json",
        "rid": request.rid || `${msgId}`
      }
    }, msgId) as ILoginRes;
  } catch (e) {
    handleLoginError(e);
  }

  if (partnerRes && partnerRes.userInfo && partnerRes.userInfo.accounts[0] && partnerRes.userInfo.accounts[0].accountName) {
    const partnerFullname = partnerRes.userInfo.accounts[0].accountName;
    Logger.info(msgId, "partnerFullname :", partnerFullname);
    const query: Query<LinkAccount> = new Query<LinkAccount>(linkAccount);
    query.where((model: LinkAccount) => model.id, `=${linkAccount.id.get()}`);
    const queryData: QueryData = query.update();
    queryData.data.partner_fullname = partnerFullname;
    const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
    await getConnectAndQuery(queryData.query, params);
  }
}

function createLoginParams(partner: Partner, linkAccount: LinkAccount, request: ILinkAccountCreatorRequest): ILoginReq {
  return {
    grant_type: GrantType.LINK_ACCOUNT,
    client_id: partner.loginClientId.get(),
    client_secret: partner.loginClientSecret.get(),
    username: `${partner.targetPartnerId.get()}:${linkAccount.partnerUsername.get()}`,
    password: sign(partner.privateKey.get(), linkAccount.partnerUsername.get()),
    platform: request.headers.token.platform,
    osVersion: request.headers.token.osVersion,
    appVersion: request.headers.token.appVersion,
    sourceIp: request.sourceIp,
  };
}

function handleLoginError(e: any): void {
  if (e instanceof Errors.GeneralError) {
    e.code = `LOGIN_PARTNER_ERROR.${e.code}`;
  }
  throw e;
}


/*
* client -> source: unlink an linked account
*/
export async function deleteLinkAccount(request: ILinkAccountDeleteRequest, msgId: string): Promise<object> {
  const partner: Partner = await findPartner(request.partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("PARTNER_NOT_FOUND");
  }
  const username = request.headers.token.userData.username;
  const userId = (request.headers.token.userData as any).id;
  const linkAccount = await findCurrentLinkAccount(request.partnerId, username, userId);
  if (linkAccount == null) {
    throw new Errors.GeneralError("LINK_ACCOUNT_NOT_FOUND");
  }

  const signData = sign(partner.privateKey.get(), `${partner.targetPartnerId.get()}_${linkAccount.partnerUsername.get()}`);
  const unlinkRequest: ILinkAccountUnlinkRequest = {
    partnerId: partner.targetPartnerId.get(),
    username: linkAccount.partnerUsername.get(),
    sign: signData,
  };
  await doJobInTransaction(async (connection: Connection) => {
    const entity: LinkAccountHistory = new LinkAccountHistory({});
    entity.id.set(linkAccount.id.get());
    entity.partnerId.set(linkAccount.partnerId.get());
    entity.partnerUserId.set(linkAccount.partnerUserId.get());
    entity.partnerUsername.set(linkAccount.partnerUsername.get());
    entity.userId.set(linkAccount.userId.get());
    entity.username.set(linkAccount.username.get());
    entity.joinLeaderboard.set(linkAccount.joinLeaderboard.get());
    entity.subAccount.set(linkAccount.subAccount.get());
    entity.deletedAt.set(new Date());
    entity.createdAt.set(linkAccount.createdAt.get());
    await connection.query(new Query<LinkAccountHistory>(new LinkAccountHistory({})).insert(), entity.getRow());

    const query: Query<LinkAccount> = new Query<LinkAccount>(new LinkAccount({}));
    query.where((model: LinkAccount) => model.id, "= ?");
    await connection.query(query.delete(), [linkAccount.id.get()]);
    await queryTradexApi(`${partner.unlinkUrl.get()}`, {
      method: 'POST',
      body: JSON.stringify(unlinkRequest),
      headers: {
        "Content-type": "application/json",
        "rid": request.rid || `${msgId}`
      }
    }, msgId);
  }, msgId);
  await retryable<object>(msgId, () => sendMessageSyncLinkAccount(msgId, "DEL", {
    type: "DEL",
    id: linkAccount.id.get(),
  }, "paave-real-trading"));
  return {};
}

/*
* source -> dest: to delete record in destination system
*/
export async function unlink(request: ILinkAccountUnlinkRequest, msgId: string): Promise<object> {
  const partner: Partner = await findPartner(request.partnerId);
  if (partner == null) {
    throw new Errors.GeneralError("PARTNER_NOT_FOUND");
  }
  if (!verifySign(partner.publicKey.get(), `${request.partnerId}_${request.username}`, request.sign)) {
    throw new Errors.GeneralError("INVALID_SIGNATURE");
  }
  const linkAccount = await findCurrentLinkAccount(request.partnerId, request.username, request.userId);
  if (linkAccount == null) {
    throw new Errors.GeneralError("LINK_ACCOUNT_NOT_FOUND");
  }
  const query: Query<LinkAccount> = new Query<LinkAccount>(new LinkAccount({}));
  query.where((model: LinkAccount) => model.id, "= ?");
  await getConnectAndQuery(query.delete(), [linkAccount.id.get()]);
  await retryable<object>(msgId, () => sendMessageSyncLinkAccount(msgId, "DEL", {
    type: "DEL",
    id: linkAccount.id.get(),
  }, "paave-real-trading"));
  return {};
}

/*
* source -> dest: to verify authCode and add record in destination system
*/
export async function confirmLinkAccount(request: ILinkAccountConfirmRequest, msgId: string): Promise<object> {
  const invalidParams = new Errors.InvalidParameterError();
  validateLinkAccountConfirmRequest(request, invalidParams);
  invalidParams.throwErr();
  const draft: LinkAccountDraft = await findLinkAccountByAuthCode(request.authCode);
  if (draft == null) {
    throw new Errors.GeneralError("NO_AUTH_CODE_FOUND");
  }
  if (draft.partnerId.get() !== request.partnerId) {
    throw new Errors.InvalidFieldValueError("partnerId", request.partnerId);
  }
  if (draft.partnerUsername.get() !== request.partnerUsername) {
    throw new Errors.InvalidFieldValueError("partnerUsername", request.partnerUsername);
  }
  if (draft.username.get() !== request.username) {
    throw new Errors.InvalidFieldValueError("username", request.username);
  }
  if (draft.expirationAt.get().getTime() < new Date().getTime()) {
    throw new Errors.GeneralError("AUTH_CODE_EXPIRED");
  }
  const partner: Partner = await findPartner(request.partnerId);
  if (!verifySign(partner.publicKey.get(), `${request.authCode}_${request.partnerId}_${request.username}_${request.partnerUsername}`, request.sign)) {
    throw new Errors.GeneralError("");
  }
  const linkAccount: LinkAccount = new LinkAccount({});
  linkAccount.partnerId.set(draft.partnerId.get());
  linkAccount.partnerUserId.set(draft.partnerUserId.get());
  linkAccount.partnerUsername.set(draft.partnerUsername.get());
  linkAccount.username.set(draft.username.get());
  linkAccount.initRequest.set(draft.request.get());
  linkAccount.confirmRequest.set(JSON.stringify(request));
  const data: any = {
    type: "ADD",
    id: -1,
    partnerId: partner.id.get(),
    partnerUsername: request.partnerUsername,
    username: linkAccount.username,
    joinLeaderboard: linkAccount.joinLeaderboard,
    subAccount: linkAccount.subAccount,
    createdAt: Utils.formatDateToDisplay(new Date(), Utils.DATETIME_DISPLAY_FORMAT),
  };
  await doJobInTransaction(async (con: Connection) => {
    const result: Models.Pair<any, FieldInfo[]> = await con.query(new Query<LinkAccount>(new LinkAccount({})).insert(), linkAccount.getRow());
    data.id = result.left.insertId;
    const deleteDraft: Query<LinkAccountDraft> = new Query(new LinkAccountDraft({}));
    deleteDraft.where((model: LinkAccountDraft) => model.id, "= ?");
    await con.query(deleteDraft.delete(), [draft.id.get()]);
  }, msgId);
  await retryable<object>(msgId, () => sendMessageSyncLinkAccount(msgId, "ADD", data, "paave-real-trading"));
  return {};
}

export async function findPartner(partnerId: string): Promise<Partner> {
  const queryBuilder: Query<Partner> = new Query<Partner>(new Partner({}));
  queryBuilder.where((model: Partner) => model.id, "= ?");
  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), [partnerId]);
  if (result.left == null || result.left.length === 0) {
    return null;
  }
  return new Partner(result.left[0]);
}

export async function findCurrentLinkAccount(partnerId: string, username: string, userId?: number): Promise<LinkAccount> {
  const queryBuilder: Query<LinkAccount> = new Query<LinkAccount>(new LinkAccount({}));
  const andCondition: AndCondition = queryBuilder.whereAnd((model: LinkAccount) => model.partnerId, "= ?");
  const params: any[] = [partnerId];
  if (userId != null) {
    andCondition.add(queryBuilder.model.userId, "= ?", queryBuilder.getAlias());
    params.push(userId);
  } else {
    andCondition.add(queryBuilder.model.username, "= ?", queryBuilder.getAlias());
    params.push(username);
  }

  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), params);
  if (result.left == null || result.left.length === 0) {
    return null;
  }
  return new LinkAccount(result.left[0]);
}

export async function findLinkAccount(partnerId: string, username: string, userId?: number): Promise<LinkAccount> {
  const queryBuilder: Query<LinkAccount> = new Query<LinkAccount>(new LinkAccount({}));
  const andCondition: AndCondition = queryBuilder.whereAnd((model: LinkAccount) => model.partnerId, "= ?");
  const params: any[] = [partnerId];
  if (userId != null) {
    andCondition.add(queryBuilder.model.userId, "= ?", queryBuilder.getAlias());
    params.push(userId);
  } else {
    andCondition.add(queryBuilder.model.username, "= ?", queryBuilder.getAlias());
    params.push(username);
  }

  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), params);
  if (result.left == null || result.left.length === 0) {
    return null;
  }
  return new LinkAccount(result.left[0]);
}

export async function findLinkAccounts(partnerId: string, username?: string, userId?: number, partnerUsername?: string, partnerUserId?: number): Promise<LinkAccount[]> {
  const queryBuilder: Query<LinkAccount> = new Query<LinkAccount>(new LinkAccount({}));
  const andCondition: AndCondition = queryBuilder.whereAnd((model: LinkAccount) => model.partnerId, "= ?");
  const params: any[] = [partnerId];
  const orCondition: OrCondition = new OrCondition();
  if (userId != null) {
    orCondition.add(queryBuilder.model.userId, "= ?", queryBuilder.getAlias());
    params.push(userId);
  } else {
    orCondition.add(queryBuilder.model.username, "= ?", queryBuilder.getAlias());
    params.push(username);
  }

  if (partnerUserId != null) {
    orCondition.add(queryBuilder.model.partnerUserId, "= ?", queryBuilder.getAlias());
    params.push(partnerUserId);
  } else {
    orCondition.add(queryBuilder.model.partnerUsername, "= ?", queryBuilder.getAlias());
    params.push(partnerUsername);
  }

  andCondition.addCondition(orCondition);

  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), params);
  if (result.left == null || result.left.length === 0) {
    return null;
  }
  return result.left.map((it: any) => new LinkAccount(it));
}

export async function findLinkAccountByPartner(partnerId: string, partnerUserId?: number, partnerUsername?: string): Promise<LinkAccount> {
  const queryBuilder: Query<LinkAccount> = new Query<LinkAccount>(new LinkAccount({}));
  const condition: AndCondition = queryBuilder.whereAnd((model: LinkAccount) => model.partnerId, "= ?");
  const params: any[] = [partnerId];
  if (partnerUserId != null) {
    condition.add(queryBuilder.model.partnerUserId, "= ?", queryBuilder.getAlias());
    params.push(partnerUserId);
  }
  if (partnerUsername != null) {
    condition.add(queryBuilder.model.partnerUsername, "= ?", queryBuilder.getAlias());
    params.push(partnerUsername);
  }

  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), params);
  if (result.left == null || result.left.length === 0) {
    return null;
  }
  return new LinkAccount(result.left[0]);
}

async function findLinkAccountByAuthCode(authCode: string) {
  const queryBuilder: Query<LinkAccountDraft> = new Query<LinkAccountDraft>(new LinkAccountDraft({}));
  queryBuilder.where((model: LinkAccountDraft) => model.authCode, "= ?");
  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), [authCode]);
  if (result.left == null || result.left.length === 0) {
    return null;
  }
  return new LinkAccountDraft(result.left[0]);
}

export async function queryTradexApi<T>(input: string, init: RequestInit, msgId: string): Promise<T> {
  try {
    const res: T = await fetch(input, init).then((response: Response) => {
      if (response.status === 200) {
        return response.json() as T;
      } else {
        throw response.json() as T;
      }
    });
    Logger.info(msgId, 'request', input, 'response', res);
    return res;
  } catch (e) {
    const newError = await getFinalError(e);
    Logger.error(msgId, 'request', input, 'response error', newError);
    throw newError;
  }
}

async function getFinalError(e: any | unknown): Promise<any | unknown> {
  if (e instanceof Promise) {
    const newError = await e;
    return getFinalError(newError);
  }
  if ((e as any).code != null) {
    return Errors.createFromStatus(e as Models.IStatus);
  }
  return e;
}

async function sendMessageSyncLinkAccount(msgId: string, updateType: string, data: any, topic: string) {
  if (conf.enableStrictSyncLinkAccount) {
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(msgId, topic, "internal:/api/v1/linkAccounts/update", data);
    if (msg.data.status) {
      throw new Errors.ForwardError(msg.data.status);
    }
  }
  else {
    Kafka.getInstance().sendMessage(msgId, "linkAccountUpdate", "Update", {
      type: updateType,
      data: data
    });
  }
  return {};
}

async function retryable<T>(
  transactionId: string,
  fn: () => Promise<T>,
  functionRun?: string,
  currRetry: number = 1,
  maxTry: number = 3,
): Promise<T> {
  try {
    return await fn();
  } catch (e) {
    Logger.info(`${transactionId} ${functionRun} Retry ${currRetry} failed.`);
    if (currRetry >= maxTry) {
      Logger.error(`${transactionId} ${functionRun} All ${maxTry} retry attempts exhausted`);
      throw e;
    }
    return retryable(transactionId, fn, functionRun, maxTry, currRetry + 1);
  }
}

export async function putLeaderboardSetting(request: IInternalLeaderboardSettingRequest, msgId: string): Promise<object> {
  const username: string = request.headers.token.userData.username;
  const userId: number | undefined = (request.headers.token.userData as any).id;
  Logger.info(`${msgId} putLeaderboardSetting user ${userId} ${request.optBoard}`)
  const linkAccount: LinkAccount = await findLinkAccount(request.partnerId, username, userId);
  Logger.info(`${msgId} putLeaderboardSetting user ${userId} ${request.optBoard} linkAccount ${linkAccount}`)
  if (linkAccount) {
    const query: Query<LinkAccount> = new Query<LinkAccount>(linkAccount);
    query.where((model: LinkAccount) => model.id, `=${linkAccount.id.get()}`);
    const queryData: QueryData = query.update();
    queryData.data.join_leaderboard = request.optBoard;
    queryData.data.sub_account = request.optBoard ? request.subAccount : null;
    const params: any[] = Object.keys(queryData.data).map((key: string) => queryData.data[key]);
    await getConnectAndQuery(queryData.query, params);
  }
  return {}
}

export async function createLinkAccountApprove(request: ILinkAccountApproveRequest, msgId: string): Promise<object> {
  const invalidParams = new Errors.InvalidParameterError();
  validateLinkAccountApproveRequest(request, invalidParams);
  invalidParams.throwErr();

  const partner: Partner = await findPartner(request.partnerId);
  if (!partner) {
    throw new Errors.GeneralError("PARTNER_NOT_FOUND");
  }

  const linkAccounts: LinkAccount[] = await findLinkAccounts(request.partnerId, request.username, request.userId, request.partnerUsername);
  if (linkAccounts && linkAccounts.length > 0) {
    throw new Errors.GeneralError("LINK_ACCOUNT_ALREADY_EXISTED");
  }

  const entity: LinkAccount = new LinkAccount({});
  entity.partnerId.set(partner.id.get());
  entity.partnerUsername.set(request.partnerUsername);
  entity.username.set(request.username);
  entity.userId.set(request.userId);
  entity.subAccount.set(request.subAccount);
  entity.joinLeaderboard.set(request.joinLeaderboard);
  entity.infoAccessGranted.set(true);

  const data: any = {
    type: "ADD",
    id: 0,
    partnerId: partner.id.get(),
    partnerUsername: request.partnerUsername,
    userId: request.userId,
    username: request.username,
    joinLeaderboard: request.joinLeaderboard,
    subAccount: request.subAccount,
    createdAt: Utils.formatDateToDisplay(new Date(), Utils.DATETIME_DISPLAY_FORMAT),
  };
  await doJobInTransaction(async (con: Connection) => {
    const result: Models.Pair<any, FieldInfo[]> = await con.query(new Query<LinkAccount>(new LinkAccount({})).insert(), entity.getRow());
    data.id = result.left.insertId;
  }, msgId);
  await retryable<object>(msgId, () => sendMessageSyncLinkAccount(msgId, "ADD", data, "paave-real-trading"));
  return {
    message: "LINK_ACCOUNT_SUCCESS",
  };
}

export async function getUserIdByPartnerName(request: ILinkAccountRequest, msgId: string): Promise<IGetUserIdFromPartnerResponse> {
  Logger.info(msgId, "{}: getUserIdByPartnerName: {}", request);
  const queryBuilder: Query<LinkAccount> = new Query<LinkAccount>(new LinkAccount({}));
  const andCondition: AndCondition = queryBuilder.whereAnd((model: LinkAccount) => model.partnerId, "= ?");
  const params: any[] = [request.partnerId];
  const orCondition: OrCondition = new OrCondition();
  orCondition.add(queryBuilder.model.partnerUsername, "= ?", queryBuilder.getAlias());
  params.push(request.partnerUsername);
  andCondition.addCondition(orCondition);
  const result: Models.Pair<any[], FieldInfo[]> = await getConnectAndQuery(queryBuilder.select(), params);
  if (result.left == null || result.left.length === 0) {
    return null;
  }
  Logger.info(msgId, "{}: getUserIdByPartnerName: {}", result.left[0]);
  return {
    userId: result.left[0].user_id,
    username: result.left[0].username,
  }
}

