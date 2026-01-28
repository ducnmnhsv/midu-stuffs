import { Models } from "tradex-common";

export default interface INotifyOtpFromPartnerReq extends Models.IDataRequest {
  matrixId: number;
  sign: string;
  partnerId: string;
  registerMobileOtp: boolean;
  username: string;
}