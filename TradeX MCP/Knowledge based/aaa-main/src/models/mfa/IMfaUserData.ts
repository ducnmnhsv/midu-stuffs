import {IUserInfo} from "../IServiceLoginRes";
import {Models} from "tradex-common";

export interface IMfaUserData {
  userInfo: IUserInfo,
  userData: Models.IUserData,
  registerMobileOtp: boolean;
  otpType: string;
  otpValue: string;
  refreshTokenId?: string;
  mobileOtpValue?: string;
}
