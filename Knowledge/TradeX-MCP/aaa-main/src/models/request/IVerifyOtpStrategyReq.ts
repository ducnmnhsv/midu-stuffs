import { Models } from "tradex-common";

export default interface IVerifyOtpStrategyReq extends Models.IDataRequest {
  otpId: string;
  otpValue: string;
}
