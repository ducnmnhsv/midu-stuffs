import { Models } from "tradex-common";

export interface ILoginVerifyOtpReq extends Models.IDataRequest {
  otpKey: string;
  otpValue: string;
}
