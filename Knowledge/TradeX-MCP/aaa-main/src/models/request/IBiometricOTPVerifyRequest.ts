import { Models } from "tradex-common";

export interface IBiometricOTPVerifyRequest extends Models.IDataRequest {
  otpValue: string;
}