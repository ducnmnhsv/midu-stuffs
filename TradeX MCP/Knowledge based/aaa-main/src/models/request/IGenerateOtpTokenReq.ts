import { Models } from "tradex-common";

export interface IGenerateOtpTokenReq extends Models.IDataRequest {
  clientID?: string;
  otp?: string;
  wordMatrixValue01?: string;
  wordMatrixId?: number;
  wordMatrixValue02?: string;
  wordMatrixValue?: string;
  expireTime?: number;
  verifyType: string;
  key?: string;
}
