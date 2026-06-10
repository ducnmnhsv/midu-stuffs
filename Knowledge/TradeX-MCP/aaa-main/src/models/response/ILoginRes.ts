export interface IUserBankAccount {
  bankCode: string;
  bankName: string;
}

export interface IUserSubAccount {
  subNumber: string;
  bankAccounts: IUserBankAccount[];
}

export interface IUserAccount {
  accountNumber: string;
  accountName: string;
  accountSubs: IUserSubAccount[];
}

export interface IUserInfo {
  username?: string;
  identifierNumber?: string;
  accounts?: IUserAccount[];
  otpIndex?: string | number;
  caThumbprint?: string;
  userLevel?: string;
  // tradex -side
  displayName?: string;
  email?: string;
  avatar?: string;
  phoneCode?: string;
  phoneNumber?: string;
  birthday?: string;
  id?: number;
  userId?: number;
  //kbfina info
  surname?: string;
  givenName?: string;
}

export default interface ILoginRes {
  accessToken: string;
  refreshToken: string;
  userInfo?: IUserInfo;
  otpIndex?: string;
  userLevel?: string;
  accExpiredTime?: number;
  refExpiredTime?: number;
  registerMobileOtp?: boolean;
}
