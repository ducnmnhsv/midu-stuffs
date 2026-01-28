import { Models } from "tradex-common";

export interface IBiometricPasswordVerifyRequest extends Models.IDataRequest {
  signature: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}