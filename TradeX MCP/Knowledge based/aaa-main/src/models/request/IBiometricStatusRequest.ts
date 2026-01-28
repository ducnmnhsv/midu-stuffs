import { Models } from "tradex-common";

export interface IBiometricStatusRequest extends Models.IDataRequest {
  publicKey?: string;
  userId?: number;
  deviceId?: string;
}