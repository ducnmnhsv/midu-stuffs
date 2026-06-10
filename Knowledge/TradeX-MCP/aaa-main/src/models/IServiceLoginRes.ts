import { IConnectionIdentifier } from "./IAccessToken";
import { Models } from "tradex-common";

export interface IUserBankAccount {
  bankCode: string;
  bankName: string;
}

// If have no subAccount => just return {type:EQUITY/DERIVATIVES}
export interface IUserSubAccount {
  subNumber: string;
  //type: EQUITY/DERIVATIVES
  type: string; // tslint:disable-line
  bankAccounts: IUserBankAccount[];
}

export interface IUserAccount {
  accountNumber: string;
  accountName: string;
  accountSubs: IUserSubAccount[];
}

export interface IUserInfo {
  username: string;
  identifierNumber: string;
  avatar: string;
  accounts: IUserAccount[];
}

export default interface IServiceLoginRes {
  conId?: IConnectionIdentifier;
  userInfo: IUserInfo;
  otpIndex?: string | number;
  otpValue?: string;
  sessionId?: string;
  userData: IUserData;
}

interface IUserData extends Models.IUserData {
  expireTime?: number;
  isVerifiedOtp?: true;
}
