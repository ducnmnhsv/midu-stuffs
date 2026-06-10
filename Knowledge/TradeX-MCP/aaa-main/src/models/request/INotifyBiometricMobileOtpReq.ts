import { Models } from "tradex-common";

export default interface INotifyBiometricMobileOtpReq extends Models.IDataRequest {
  biometricId: string;
}
