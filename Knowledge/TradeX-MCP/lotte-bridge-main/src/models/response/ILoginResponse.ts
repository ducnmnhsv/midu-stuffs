export enum UserType {
  ADMIN = 'ADMIN',
  BROKER = 'BROKER',
  USER = 'USER',
  USER_CHANGE_PASSWORD_REQUIRED = 'USER_CHANGE_PASSWORD_REQUIRED',
}

export interface IUserBankAccount {
  bankCode?: string;
  bankName?: string;
}

export interface IUserSubAccount {
  subNumber?: string;
  bankAccounts?: IUserBankAccount[];
  type?: string;
}

export interface IUserAccount {
  accountNumber?: string;
  accountName?: string;
  accountSubs?: IUserSubAccount[];
}

export interface IUserInfo {
  username?: string;
  identifierNumber?: string;
  userLevel?: string;
  accounts?: IUserAccount[];
  branchCode?: string;
  requireChangePassword?: boolean;
  sotpStatus?: string;
  sotpKey?: string;

}

export interface IUserData {
  username?: string;
  identifierNumber?: string;
  deptCode?: string;
  branchCode?: string;
  mngDeptCode?: string;
  agencyNumber?: string;
  userType?: string;
  userLevel?: string;
  name?: string;
  mfaData?: string;
  accountNumbers?: string[];
  bankInfo?: {};
}

export interface ILoginResponse {
  userInfo?: IUserInfo;
  otpIndex?: string | number;
  otpValue?: string;
  userData?: IUserData;
  sessionId?: string;
  registerMobileOtp?: boolean;
}
