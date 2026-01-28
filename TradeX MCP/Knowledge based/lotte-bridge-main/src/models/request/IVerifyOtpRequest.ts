import { Models } from 'tradex-common';

export interface IVerifyOtpRequest extends Models.IDataRequest {
  otpValue: string;
  otpId: string;
}
